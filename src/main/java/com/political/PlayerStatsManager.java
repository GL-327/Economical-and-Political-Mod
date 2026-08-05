package com.political;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.core.component.DataComponents;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hypixel-Skyblock-style player stats.
 *
 * Stats are aggregated every second from equipped gear lore lines (armor + main hand),
 * then applied three ways:
 *  - MAX_HEALTH / MOVEMENT_SPEED attribute modifiers (Health / Speed stats)
 *  - outgoing damage in {@link CustomItemHandler#calculateSlayerDamage} (Strength, Crit, Ferocity)
 *  - incoming damage in BountyDamageMixin (Defense: dmg * 100 / (100 + def))
 *
 * Lore formats accepted on gear (color codes ignored, symbol optional):
 *   "❁ Strength: +50"   "☣ Crit Chance: +10%"  "☠ Crit Damage: +40%"
 *   "❈ Defense: +30"    "❤ Health: +25"        "✦ Speed: +10"
 *   "✎ Intelligence: +50"  "⫽ Ferocity: +5"
 * ("⚔ Damage:" lines stay handled by calculateSlayerDamage as the weapon's base damage.)
 */
public final class PlayerStatsManager {

    private PlayerStatsManager() {}

    // ════════════════════════════════════════════════════════════
    // STAT DEFINITIONS
    // ════════════════════════════════════════════════════════════

    public enum Stat {
        STRENGTH("Strength", "❁", ChatFormatting.RED, 0, false),
        CRIT_CHANCE("Crit Chance", "☣", ChatFormatting.BLUE, 30, true),
        CRIT_DAMAGE("Crit Damage", "☠", ChatFormatting.BLUE, 50, true),
        DEFENSE("Defense", "❈", ChatFormatting.GREEN, 0, false),
        HEALTH("Health", "❤", ChatFormatting.RED, 0, false),
        SPEED("Speed", "✦", ChatFormatting.WHITE, 100, false),
        INTELLIGENCE("Intelligence", "✎", ChatFormatting.AQUA, 100, false),
        FEROCITY("Ferocity", "⫽", ChatFormatting.RED, 0, false),
        /**
         * Magic Find raises the chance of a rare drop.
         *
         * <p>It lives in the stat engine rather than in the drop code so it is parsed off item lore
         * like every other stat, shows in the stat sheet without special casing, and can be put on
         * any piece of gear old or new by writing one line into its tooltip.</p>
         */
        MAGIC_FIND("Magic Find", "✯", ChatFormatting.AQUA, 0, false),

        /**
         * How fast the weapon swings back, as a percentage of the vanilla rate.
         *
         * <p>Was never a stat despite being written on tooltips and promised by an armour
         * attribute, so every item that claimed to give it gave nothing. A hundred is the vanilla
         * speed and the number moves the real attribute, like Speed does.</p>
         */
        ATTACK_SPEED("Attack Speed", "⚔", ChatFormatting.YELLOW, 100, false);

        public final String displayName;
        public final String symbol;
        public final ChatFormatting color;
        public final double baseValue;
        public final boolean percent;

        Stat(String displayName, String symbol, ChatFormatting color, double baseValue, boolean percent) {
            this.displayName = displayName;
            this.symbol = symbol;
            this.color = color;
            this.baseValue = baseValue;
            this.percent = percent;
        }
    }

    /** Immutable-ish snapshot of a player's computed stats. */
    public static final class StatSheet {
        private final EnumMap<Stat, Double> values = new EnumMap<>(Stat.class);

        StatSheet() {
            for (Stat s : Stat.values()) values.put(s, s.baseValue);
        }

        public double get(Stat stat) {
            return values.getOrDefault(stat, stat.baseValue);
        }

        /**
         * Adds a bonus that did not come from an item's lore. Collections use this, so a tier bonus
         * behaves exactly like a bonus rolled on a piece of armour.
         */
        public void addExternal(Stat stat, double amount) {
            values.merge(stat, amount, Double::sum);
        }

        void add(Stat stat, double amount) {
            values.merge(stat, amount, Double::sum);
        }

        void clamp() {
            values.compute(Stat.CRIT_CHANCE, (k, v) -> Math.max(0, Math.min(100, v == null ? k.baseValue : v)));
            values.compute(Stat.SPEED, (k, v) -> Math.max(20, Math.min(400, v == null ? k.baseValue : v)));
            values.compute(Stat.FEROCITY, (k, v) -> Math.max(0, Math.min(200, v == null ? k.baseValue : v)));
            // Bounded. The incoming formula is dmg * 100 / (100 + defense), so unbounded defense
            // asymptotes to taking nothing at all. A thousand still lets nine percent through and
            // means no future item combination can quietly reach invulnerability.
            values.compute(Stat.DEFENSE, (k, v) ->
                    Math.max(0, Math.min(1000, v == null ? k.baseValue : v)));
            values.compute(Stat.STRENGTH, (k, v) ->
                    Math.max(0, Math.min(2000, v == null ? k.baseValue : v)));
            values.compute(Stat.CRIT_DAMAGE, (k, v) ->
                    Math.max(0, Math.min(800, v == null ? k.baseValue : v)));
            values.compute(Stat.MAGIC_FIND, (k, v) ->
                    Math.max(0, Math.min(400, v == null ? k.baseValue : v)));
            values.compute(Stat.ATTACK_SPEED, (k, v) ->
                    Math.max(20, Math.min(400, v == null ? k.baseValue : v)));
        }
    }

    // ════════════════════════════════════════════════════════════
    // CACHE + RECOMPUTE
    // ════════════════════════════════════════════════════════════

    private static final Map<UUID, StatSheet> statCache = new ConcurrentHashMap<>();
    private static final Set<UUID> actionBarDisabled = ConcurrentHashMap.newKeySet();

    private static final Identifier STATS_HEALTH_ID = Identifier.fromNamespaceAndPath("political", "stats_health");
    private static final Identifier STATS_SPEED_ID = Identifier.fromNamespaceAndPath("political", "stats_speed");
    private static final Identifier STATS_ATTACK_SPEED_ID =
            Identifier.fromNamespaceAndPath("political", "stats_attack_speed");

    /** Drops a cached sheet so the next read recomputes it. Used by the live reload. */
    public static void invalidate(ServerPlayer player) {
        statCache.remove(player.getUUID());
    }

    public static StatSheet getStats(ServerPlayer player) {
        StatSheet sheet = statCache.get(player.getUUID());
        if (sheet == null) {
            sheet = compute(player);
            statCache.put(player.getUUID(), sheet);
        }
        return sheet;
    }

    /** Call once per player tick (cheap: full recompute only every 20 ticks). */
    public static void tickPlayer(ServerPlayer player) {
        if (player.tickCount % 20 == 0) {
            StatSheet sheet = compute(player);
            statCache.put(player.getUUID(), sheet);
            applyAttributeModifiers(player, sheet);
        }
        if (player.tickCount % 40 == 7 && !actionBarDisabled.contains(player.getUUID())) {
            sendActionBar(player);
        }
    }

    public static void onPlayerDisconnect(UUID uuid) {
        statCache.remove(uuid);
    }

    private static StatSheet compute(ServerPlayer player) {
        StatSheet sheet = new StatSheet();
        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack worn = player.getItemBySlot(slot);
            parseItemStats(worn, sheet, true, delveScale(worn, player));
        }
        ItemStack held = player.getMainHandItem();
        parseItemStats(held, sheet, false, delveScale(held, player));
        // Progression that is not carried on an item, folded in before the clamp.
        try {
            CollectionManager.applyBonuses(player, sheet);
            CustomEnchantEffects.contributeStats(player, sheet);
        } catch (Exception ignored) {
            // A collection store that has not initialised yet must never break stats.
        }
        sheet.clamp();
        return sheet;
    }

    /**
     * Parse stat lines from an item's lore. Armor contributes every stat;
     * held items contribute offensive stats only (no Defense/Health from holding armor).
     */
    private static void parseItemStats(ItemStack stack, StatSheet sheet, boolean isArmorSlot) {
        parseItemStats(stack, sheet, isArmorSlot, 1.0);
    }

    private static void parseItemStats(ItemStack stack, StatSheet sheet, boolean isArmorSlot,
                                       double scale) {
        if (stack == null || stack.isEmpty()) return;
        ItemLore lore = stack.get(DataComponents.LORE);
        if (lore == null || lore.lines().isEmpty()) return;

        for (Component line : lore.lines()) {
            String raw = line.getString();
            if (raw == null || raw.isEmpty()) continue;
            String s = raw.replaceAll("§.", "").trim();
            String lower = s.toLowerCase(Locale.ROOT);

            for (Stat stat : Stat.values()) {
                if (!isArmorSlot && (stat == Stat.DEFENSE || stat == Stat.HEALTH || stat == Stat.SPEED)) continue;
                String name = stat.displayName.toLowerCase(Locale.ROOT);
                int idx = lower.indexOf(name + ":");
                if (idx < 0) continue;
                // Avoid matching e.g. "crit damage" inside a "crit chance" pass? Names are distinct enough,
                // but "damage:" alone must not match "crit damage:". Require start-of-line or symbol/space before.
                if (idx > 0) {
                    char before = lower.charAt(idx - 1);
                    if (Character.isLetter(before)) continue;
                }
                Double value = parseFirstNumber(s.substring(idx + name.length() + 1));
                if (value != null) {
                    sheet.add(stat, value * scale);
                }
                break;
            }
        }
    }

    /**
     * How much one item's printed stats are worth right now.
     *
     * <p>Forging, marks and sigils all multiply, and standing in a delve multiplies again. A piece
     * made in the delves gets a further bonus for being used where it belongs, which is what makes
     * delve gear feel like delve gear without making it dead weight on the surface.</p>
     */
    private static double delveScale(ItemStack stack, ServerPlayer player) {
        if (stack == null || stack.isEmpty()) return 1.0;
        try {
            return DelveForge.statMultiplier(stack, player) * DelveForge.homeBonus(stack, player);
        } catch (Exception e) {
            // Stats must never fail to compute because an upgrade system had an opinion.
            return 1.0;
        }
    }

    /**
     * Reads one stat off one tooltip line, for the self test.
     *
     * <p>Runs the same matching the real parser uses rather than a copy of it, so a check passing
     * here means the real path works and not that two implementations agree with each other.</p>
     */
    static double parseStatLineForTest(String line, Stat stat) {
        StatSheet sheet = new StatSheet();
        String plain = line.replaceAll("§.", "").trim();
        String lower = plain.toLowerCase(Locale.ROOT);
        String name = stat.displayName.toLowerCase(Locale.ROOT);
        int idx = lower.indexOf(name + ":");
        if (idx < 0) return 0;
        Double value = parseFirstNumber(plain.substring(idx + name.length() + 1));
        return value == null ? 0 : value;
    }

    private static Double parseFirstNumber(String s) {
        int i = 0;
        while (i < s.length() && (s.charAt(i) == ' ' || s.charAt(i) == '+')) i++;
        boolean negative = i < s.length() && s.charAt(i) == '-';
        if (negative) i++;
        StringBuilder num = new StringBuilder();
        boolean seenDigit = false;
        for (; i < s.length(); i++) {
            char c = s.charAt(i);
            if ((c >= '0' && c <= '9') || c == '.') {
                num.append(c);
                seenDigit = true;
            } else if (c == ',') {
                continue;
            } else {
                break;
            }
        }
        if (!seenDigit) return null;
        try {
            double v = Double.parseDouble(num.toString());
            return negative ? -v : v;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ════════════════════════════════════════════════════════════
    // ATTRIBUTE APPLICATION (Health / Speed)
    // ════════════════════════════════════════════════════════════

    private static void applyAttributeModifiers(ServerPlayer player, StatSheet sheet) {
        applyModifier(player, Attributes.MAX_HEALTH, STATS_HEALTH_ID,
                sheet.get(Stat.HEALTH), AttributeModifier.Operation.ADD_VALUE);
        double speedFraction = (sheet.get(Stat.SPEED) - 100.0) / 100.0;
        applyModifier(player, Attributes.MOVEMENT_SPEED, STATS_SPEED_ID,
                speedFraction, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        double swingFraction = (sheet.get(Stat.ATTACK_SPEED) - 100.0) / 100.0;
        applyModifier(player, Attributes.ATTACK_SPEED, STATS_ATTACK_SPEED_ID,
                swingFraction, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    private static void applyModifier(ServerPlayer player, Holder<Attribute> attribute,
                                      Identifier id, double value, AttributeModifier.Operation op) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;
        instance.removeModifier(id);
        if (value != 0) {
            instance.addTransientModifier(new AttributeModifier(id, value, op));
        }
    }

    // ════════════════════════════════════════════════════════════
    // COMBAT HOOKS
    // ════════════════════════════════════════════════════════════

    /**
     * Outgoing damage: Strength multiplier, crit roll, Ferocity extra strike.
     * Called at the end of {@link CustomItemHandler#calculateSlayerDamage}.
     */
    public static float applyOutgoingDamage(ServerPlayer attacker, LivingEntity target, float damage) {
        StatSheet sheet = getStats(attacker);

        double strength = sheet.get(Stat.STRENGTH);
        if (strength != 0) {
            damage *= (float) (1.0 + strength / 100.0);
        }

        boolean crit = attacker.getRandom().nextDouble() * 100.0 < sheet.get(Stat.CRIT_CHANCE);
        if (crit) {
            damage *= (float) (1.0 + sheet.get(Stat.CRIT_DAMAGE) / 100.0);
            if (attacker.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ENCHANTED_HIT,
                        target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(),
                        8, 0.3, 0.3, 0.3, 0.15);
                sl.playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT,
                        SoundSource.PLAYERS, 0.7f, 1.1f);
            }
        }

        // Ferocity: each 100 = one guaranteed extra strike; remainder = % chance of one more.
        double ferocity = sheet.get(Stat.FEROCITY);
        if (ferocity > 0 && attacker.level() instanceof ServerLevel sl) {
            int extraStrikes = (int) (ferocity / 100.0);
            if (attacker.getRandom().nextDouble() * 100.0 < ferocity % 100.0) extraStrikes++;
            for (int i = 0; i < extraStrikes && target.isAlive(); i++) {
                target.hurtServer(sl, attacker.damageSources().magic(), damage * 0.5f);
                // This runs inside the main hit's HEAD mixin: clear the i-frames the extra
                // strike just granted, or the main hit would be reduced to almost nothing.
                target.invulnerableTime = 0;
                sl.sendParticles(ParticleTypes.SWEEP_ATTACK,
                        target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                        1, 0.1, 0.1, 0.1, 0.0);
                sl.playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP,
                        SoundSource.PLAYERS, 0.8f, 1.6f);
            }
        }

        spawnDamageHologram(target, damage, crit);
        return damage;
    }

    /** Incoming damage reduction: dmg * 100 / (100 + defense). Void damage is never reduced. */
    public static float applyIncomingDefense(ServerPlayer player, DamageSource source, float damage) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return damage;
        double defense = getStats(player).get(Stat.DEFENSE);
        if (defense <= 0) return damage;
        return damage * (float) (100.0 / (100.0 + defense));
    }

    /** Intelligence above the 100 base adds max mana 1:1 (wired into MagicManager.getMaxMana). */
    public static int getBonusMana(String uuid) {
        try {
            StatSheet sheet = statCache.get(UUID.fromString(uuid));
            if (sheet == null) return 0;
            return (int) Math.max(0, sheet.get(Stat.INTELLIGENCE) - Stat.INTELLIGENCE.baseValue);
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    // ════════════════════════════════════════════════════════════
    // DAMAGE HOLOGRAMS
    // ════════════════════════════════════════════════════════════

    private record Hologram(ArmorStand stand, long expiryTick) {}

    private static final Deque<Hologram> holograms = new ArrayDeque<>();
    private static final int MAX_HOLOGRAMS = 24;
    private static final int HOLOGRAM_LIFETIME_TICKS = 16;

    private static void spawnDamageHologram(LivingEntity target, float damage, boolean crit) {
        if (!(target.level() instanceof ServerLevel sl)) return;
        if (holograms.size() >= MAX_HOLOGRAMS) {
            Hologram oldest = holograms.pollFirst();
            if (oldest != null) oldest.stand().discard();
        }
        ArmorStand stand = new ArmorStand(EntityType.ARMOR_STAND, sl);
        double angle = sl.getRandom().nextDouble() * Math.PI * 2;
        stand.setPos(target.getX() + Math.cos(angle) * 0.6,
                target.getY() + target.getBbHeight() * 0.75,
                target.getZ() + Math.sin(angle) * 0.6);
        stand.setInvisible(true);
        ((com.political.mixin.ArmorStandAccessor) stand).political_setMarker(true);
        stand.setNoGravity(true);
        stand.setSilent(true);
        String text = crit
                ? "§f✧§c" + formatDamage(damage) + "§f✧"
                : "§7" + formatDamage(damage);
        stand.setCustomName(Component.literal(text));
        stand.setCustomNameVisible(true);
        // Tagged so stray holograms from a crash are culled on chunk load (see PoliticalServer ENTITY_LOAD).
        stand.addTag("political_dmg_holo");
        sl.addFreshEntity(stand);
        holograms.addLast(new Hologram(stand, sl.getServer().getTickCount() + HOLOGRAM_LIFETIME_TICKS));
    }

    private static String formatDamage(float damage) {
        if (damage >= 1000) return String.format(Locale.ROOT, "%,d", Math.round(damage));
        return damage == Math.floor(damage)
                ? String.valueOf((int) damage)
                : String.format(Locale.ROOT, "%.1f", damage);
    }

    /** Call once per server tick to expire damage holograms. */
    public static void tickServer(net.minecraft.server.MinecraftServer server) {
        if (holograms.isEmpty()) return;
        long now = server.getTickCount();
        while (!holograms.isEmpty() && holograms.peekFirst().expiryTick() <= now) {
            holograms.pollFirst().stand().discard();
        }
    }

    // ════════════════════════════════════════════════════════════
    // DISPLAY
    // ════════════════════════════════════════════════════════════

    private static void sendActionBar(ServerPlayer player) {
        StatSheet sheet = getStats(player);
        int hp = Math.round(player.getHealth());
        int maxHp = Math.round(player.getMaxHealth());
        int defense = (int) sheet.get(Stat.DEFENSE);
        String uuid = player.getUUID().toString();
        int mana = (int) MagicManager.getMana(uuid);
        int maxMana = MagicManager.getMaxMana(uuid);

        StringBuilder bar = new StringBuilder();
        bar.append("§c❤ ").append(hp).append("/").append(maxHp);
        bar.append("   §a❈ ").append(defense).append(" Defense");
        bar.append("   §b✎ ").append(mana).append("/").append(maxMana).append(" Mana");
        player.sendSystemMessage(Component.literal(bar.toString()), true);
    }

    // ════════════════════════════════════════════════════════════
    // COMMANDS (/stats, /statbar)
    // ════════════════════════════════════════════════════════════

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("stats")
                .executes(ctx -> {
                    // GUI first — the chat sheet stays available as /stats text.
                    PlayerStatsGui.open(ctx.getSource().getPlayerOrException());
                    return 1;
                })
                .then(Commands.literal("text").executes(ctx -> {
                    showStats(ctx.getSource().getPlayerOrException());
                    return 1;
                })));
        dispatcher.register(Commands.literal("statbar").executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            UUID uuid = player.getUUID();
            if (actionBarDisabled.remove(uuid)) {
                player.sendSystemMessage(Component.literal("✔ Stats action bar enabled").withStyle(ChatFormatting.GREEN));
            } else {
                actionBarDisabled.add(uuid);
                player.sendSystemMessage(Component.literal("✘ Stats action bar disabled").withStyle(ChatFormatting.YELLOW));
            }
            return 1;
        }));
    }

    /** Flip the stats action bar on/off for this player (used by PlayerStatsGui). */
    public static void toggleActionBar(ServerPlayer player) {
        UUID uuid = player.getUUID();
        if (actionBarDisabled.remove(uuid)) {
            player.sendSystemMessage(Component.literal("✔ Stats action bar enabled")
                    .withStyle(ChatFormatting.GREEN));
        } else {
            actionBarDisabled.add(uuid);
            player.sendSystemMessage(Component.literal("✘ Stats action bar disabled")
                    .withStyle(ChatFormatting.YELLOW));
        }
    }

    private static void showStats(ServerPlayer player) {
        StatSheet sheet = compute(player);
        statCache.put(player.getUUID(), sheet);

        player.sendSystemMessage(Component.literal("═══════ Your Stats ═══════")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        player.sendSystemMessage(statLine(Stat.HEALTH,
                Math.round(player.getMaxHealth()) + " §7(base 20 " + withSign(sheet.get(Stat.HEALTH)) + " gear + other bonuses)"));
        for (Stat stat : new Stat[]{Stat.DEFENSE, Stat.STRENGTH, Stat.CRIT_CHANCE, Stat.CRIT_DAMAGE,
                Stat.SPEED, Stat.ATTACK_SPEED, Stat.INTELLIGENCE, Stat.FEROCITY, Stat.MAGIC_FIND}) {
            String value = trimNumber(sheet.get(stat)) + (stat.percent ? "%" : "");
            player.sendSystemMessage(statLine(stat, value));
        }
        String uuid = player.getUUID().toString();
        player.sendSystemMessage(Component.literal("  ✎ Mana: §b" + (int) MagicManager.getMana(uuid)
                + "/" + MagicManager.getMaxMana(uuid)).withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(Component.literal("§8Stats come from gear lore lines (e.g. \"❁ Strength: +50\").")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    private static Component statLine(Stat stat, String value) {
        return Component.literal("  " + stat.symbol + " " + stat.displayName + ": ")
                .withStyle(stat.color)
                .append(Component.literal(String.valueOf(value)).withStyle(ChatFormatting.WHITE));
    }

    private static String withSign(double v) {
        String n = trimNumber(Math.abs(v));
        return v >= 0 ? "+" + n : "-" + n;
    }

    private static String trimNumber(double v) {
        return v == Math.floor(v) ? String.valueOf((long) v) : String.format(Locale.ROOT, "%.1f", v);
    }
}
