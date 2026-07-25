package com.political;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Owns which relics a player has found and which are currently equipped in their accessory bag.
 *
 * <p>Only equipped relics do anything. The bag starts small and grows, so relics are a decision
 * rather than a pile of stacking buffs. Everything visible goes through {@link Vfx} so relic procs
 * match the cinematic standard.</p>
 */
public final class RelicManager {

    private RelicManager() {}

    /** Bag capacity by upgrade level, starting small on purpose. */
    private static final int[] BAG_SLOTS = {3, 4, 5, 6, 7, 8, 9};

    public static final class Profile {
        public Set<String> owned = new LinkedHashSet<>();
        public List<String> equipped = new ArrayList<>();
        public int bagLevel = 0;
    }

    private static final Map<String, Profile> DATA = new HashMap<>();
    private static Path file;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // ── Persistence ─────────────────────────────────────────────────────────

    public static void initialize(Path worldRoot) {
        file = worldRoot.resolve("relics.json");
        if (!Files.exists(file)) return;
        try (Reader r = Files.newBufferedReader(file)) {
            java.lang.reflect.Type t = new TypeToken<Map<String, Profile>>() {}.getType();
            Map<String, Profile> loaded = GSON.fromJson(r, t);
            if (loaded != null) {
                DATA.clear();
                DATA.putAll(loaded);
            }
        } catch (Exception e) {
            PoliticalServer.LOGGER.warn("Failed to load relics.json", e);
        }
    }

    public static void save() {
        if (file == null) return;
        try (Writer w = Files.newBufferedWriter(file)) {
            GSON.toJson(DATA, w);
        } catch (Exception e) {
            PoliticalServer.LOGGER.warn("Failed to save relics.json", e);
        }
    }

    public static Profile profile(String uuid) {
        return DATA.computeIfAbsent(uuid, k -> new Profile());
    }

    // ── Ownership and equipping ─────────────────────────────────────────────

    public static int slots(String uuid) {
        int lvl = Math.max(0, Math.min(BAG_SLOTS.length - 1, profile(uuid).bagLevel));
        return BAG_SLOTS[lvl];
    }

    public static boolean owns(String uuid, Relic r) {
        return profile(uuid).owned.contains(r.name());
    }

    public static boolean isEquipped(String uuid, Relic r) {
        return profile(uuid).equipped.contains(r.name());
    }

    public static List<Relic> equipped(String uuid) {
        List<Relic> out = new ArrayList<>();
        for (String n : profile(uuid).equipped) {
            Relic r = Relic.byId(n);
            if (r != null) out.add(r);
        }
        return out;
    }

    /** Records a relic as found. Returns false if it was already owned. */
    public static boolean grant(ServerPlayer player, Relic r) {
        Profile p = profile(player.getUUID().toString());
        if (!p.owned.add(r.name())) return false;
        save();
        player.sendSystemMessage(Component.literal("✦ Relic found: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(r.display).withStyle(r.colour, ChatFormatting.BOLD)));
        if (player.level() instanceof ServerLevel level) {
            Vfx.bloom(level, player.position().add(0, 1, 0));
        }
        return true;
    }

    /** Equips or unequips, respecting the bag size. */
    public static void toggle(ServerPlayer player, Relic r) {
        String uuid = player.getUUID().toString();
        Profile p = profile(uuid);
        if (!p.owned.contains(r.name())) {
            player.sendSystemMessage(Component.literal("§cYou have not found that relic yet."));
            return;
        }
        if (p.equipped.remove(r.name())) {
            player.sendSystemMessage(Component.literal("§7Unequipped " + r.colour + r.display));
        } else {
            if (p.equipped.size() >= slots(uuid)) {
                player.sendSystemMessage(Component.literal("§cYour bag is full. Unequip something first."));
                return;
            }
            p.equipped.add(r.name());
            player.sendSystemMessage(Component.literal("§7Equipped " + r.colour + r.display));
        }
        save();
    }

    /** Upgrades the bag for coins. */
    public static void upgradeBag(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        Profile p = profile(uuid);
        if (p.bagLevel >= BAG_SLOTS.length - 1) {
            player.sendSystemMessage(Component.literal("§7Your bag cannot hold any more."));
            return;
        }
        long cost = 100_000L * (p.bagLevel + 1);
        if (CoinManager.getCoinsLong(player) < cost) {
            player.sendSystemMessage(Component.literal("§cThat upgrade costs §6" + cost + " §ccoins."));
            return;
        }
        CoinManager.removeCoins(player, cost);
        p.bagLevel++;
        save();
        player.sendSystemMessage(Component.literal("§aYour accessory bag now holds §f"
                + slots(uuid) + " §arelics."));
    }

    // ── Effects ─────────────────────────────────────────────────────────────

    private static final Map<String, Long> COOLDOWNS = new HashMap<>();

    private static boolean ready(String uuid, Relic r, long ms) {
        String key = uuid + ":" + r.name();
        long now = System.currentTimeMillis();
        Long last = COOLDOWNS.get(key);
        if (last != null && now - last < ms) return false;
        COOLDOWNS.put(key, now);
        return true;
    }

    /** Ticked once per second per player for the passive relics. */
    public static void tickPlayer(ServerPlayer player) {
        if (player.tickCount % 20 != 0) return;
        String uuid = player.getUUID().toString();
        Profile p = DATA.get(uuid);
        if (p == null || p.equipped.isEmpty()) return;

        for (Relic r : equipped(uuid)) {
            if (r.trigger != Relic.Trigger.PASSIVE) continue;
            switch (r) {
                case HEARTHSTONE -> {
                    if (player.getHealth() < player.getMaxHealth()) player.heal(0.5f);
                }
                case WANDERER_BOOT -> ensure(player, MobEffects.SPEED, 0);
                case DEEP_LUNG -> ensure(player, MobEffects.WATER_BREATHING, 0);
                case NIGHT_EYE -> ensure(player, MobEffects.NIGHT_VISION, 0);
                case MINERS_LAMP -> ensure(player, MobEffects.HASTE, 0);
                case SILENT_TREAD -> ensure(player, MobEffects.INVISIBILITY, 0);
                case IRON_STOMACH -> ensure(player, MobEffects.SATURATION, 0);
                case WARDING_SIGIL, BULWARK_CREST -> ensure(player, MobEffects.RESISTANCE, 0);
                case TIDE_CHARM -> {
                    if (player.isInWater()) ensure(player, MobEffects.DOLPHINS_GRACE, 0);
                }
                case EMBERWALK -> ensure(player, MobEffects.FIRE_RESISTANCE, 0);
                case FEATHER_FALL -> {
                    if (player.fallDistance > 3.0f) ensure(player, MobEffects.SLOW_FALLING, 0);
                }
                case GOLEM_CORE -> ensure(player, MobEffects.HEALTH_BOOST, 0);
                case CURSED_MARROW -> {
                    if (player.tickCount % 200 == 0) {
                        CursedEnergyManager.earn(player, 2);
                    }
                }
                default -> { }
            }
        }
    }

    private static void ensure(ServerPlayer player, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> e, int amp) {
        MobEffectInstance cur = player.getEffect(e);
        if (cur == null || cur.getDuration() < 40) {
            player.addEffect(new MobEffectInstance(e, 120, amp, true, false, false));
        }
    }

    /** Routed from the attack path. */
    public static void onHit(ServerPlayer player, LivingEntity target) {
        String uuid = player.getUUID().toString();
        Profile p = DATA.get(uuid);
        if (p == null || p.equipped.isEmpty()) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        for (Relic r : equipped(uuid)) {
            if (r.trigger != Relic.Trigger.ON_HIT) continue;
            if (player.getRandom().nextInt(100) >= r.power && r.power < 100) continue;
            switch (r) {
                case EMBER_CORE -> {
                    target.setRemainingFireTicks(80);
                    VfxPlus.flamePillar(level, target.position());
                }
                case THUNDER_SEED -> {
                    if (ready(uuid, r, 4000)) Vfx.stormStrike(level, target.position());
                }
                case VENOM_SAC -> target.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1, true, false, true));
                case FROST_SHARD -> {
                    target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1, true, false, true));
                    VfxPlus.frostNova(level, target.position());
                }
                case RIPPER_FANG -> {
                    target.hurtServer(level, player.damageSources().magic(), 3.0f);
                    target.invulnerableTime = 0;
                }
                case SUNDERING_NAIL -> target.addEffect(
                        new MobEffectInstance(MobEffects.WEAKNESS, 100, 0, true, false, true));
                case CINDER_BRAND -> {
                    target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120, 0, true, false, true));
                    target.setRemainingFireTicks(60);
                }
                default -> { }
            }
        }
    }

    /** Routed from the kill path. */
    public static void onKill(ServerPlayer player, LivingEntity victim) {
        String uuid = player.getUUID().toString();
        Profile p = DATA.get(uuid);
        if (p == null || p.equipped.isEmpty()) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        for (Relic r : equipped(uuid)) {
            if (r.trigger != Relic.Trigger.ON_KILL) continue;
            switch (r) {
                case SOUL_LANTERN_RELIC -> {
                    CursedEnergyManager.earn(player, 3);
                    VfxPlus.soulHarvest(level, victim.position().add(0, 1, 0), player.position().add(0, 1, 0));
                }
                case GLUTTON_HEART -> player.heal(r.power / 5.0f);
                case TROPHY_CHAIN -> {
                    if (ready(uuid, r, 1500)) CoinManager.giveCoinsQuiet(player, r.power);
                }
                case MOMENTUM_COIL -> player.addEffect(
                        new MobEffectInstance(MobEffects.SPEED, r.power, 1, true, false, false));
                case WRAITH_CALL -> {
                    if (player.getRandom().nextInt(100) < r.power && ready(uuid, r, 20000)) {
                        VfxPlus.summoningCircle(level, victim.position(), Vfx.CURSED);
                    }
                }
                default -> { }
            }
        }
    }

    /** Routed from the incoming damage path. Returns a damage multiplier. */
    public static float onHurt(ServerPlayer player, float amount) {
        String uuid = player.getUUID().toString();
        Profile p = DATA.get(uuid);
        if (p == null || p.equipped.isEmpty()) return 1.0f;
        if (!(player.level() instanceof ServerLevel level)) return 1.0f;

        float mult = 1.0f;
        for (Relic r : equipped(uuid)) {
            if (r.trigger != Relic.Trigger.ON_HURT) continue;
            switch (r) {
                case THORN_HEART -> {
                    LivingEntity attacker = player.getLastHurtByMob();
                    if (attacker != null && attacker.isAlive()
                            && player.getRandom().nextInt(100) < r.power) {
                        attacker.hurtServer(level, player.damageSources().thorns(player), 4.0f);
                    }
                }
                case LAST_BREATH -> {
                    if (player.getHealth() - amount <= 2.0f && ready(uuid, r, 120_000)) {
                        player.heal(8.0f);
                        player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 100, 2, true, false, false));
                        VfxPlus.shieldDome(level, player.position().add(0, 1, 0), 2.5, Vfx.ARCANE);
                        player.sendSystemMessage(Component.literal("§6§lLAST BREATH")
                                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), true);
                    }
                }
                case STONE_SKIN -> {
                    if (player.getRandom().nextInt(100) < r.power) {
                        player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 80, 1, true, false, false));
                    }
                }
                case MIRROR_SCALE -> {
                    LivingEntity attacker = player.getLastHurtByMob();
                    if (attacker != null && attacker.isAlive()
                            && player.getRandom().nextInt(100) < r.power) {
                        attacker.hurtServer(level, player.damageSources().magic(), amount * 0.25f);
                        mult *= 0.9f;
                    }
                }
                case EMBER_WARD -> {
                    if (player.isOnFire()) {
                        player.clearFire();
                        player.heal(1.0f);
                        mult *= 0.85f;
                    }
                }
                case PANIC_STEP -> {
                    if (player.getRandom().nextInt(100) < r.power) {
                        player.addEffect(new MobEffectInstance(MobEffects.SPEED, 60, 1, true, false, false));
                    }
                }
                default -> { }
            }
        }
        return mult;
    }

    // ── Relic items, so they can drop and be held ───────────────────────────

    public static ItemStack createRelicItem(Relic r) {
        ItemStack s = new ItemStack(r.icon);
        s.set(DataComponents.CUSTOM_NAME,
                Component.literal("✦ " + r.display).withStyle(r.colour, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§8" + r.family.label + " Relic"));
        lore.add(Component.literal("§7" + r.flavor));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§7Fires: §f" + switch (r.trigger) {
            case ON_HIT -> "when you strike";
            case ON_KILL -> "when you kill";
            case ON_HURT -> "when you are hurt";
            case PASSIVE -> "always, while carried";
        }));
        lore.add(Component.literal("§8Right click to add it to your bag."));
        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, r.id());
        return s;
    }

    /** Right clicking a relic item claims it into the bag. */
    public static boolean tryClaim(ServerPlayer player, ItemStack stack) {
        String id = SlayerItems.getCustomItemId(stack);
        if (id == null || !id.startsWith("relic_")) return false;
        Relic r = Relic.byId(id);
        if (r == null) return false;
        if (grant(player, r)) {
            stack.shrink(1);
        } else {
            player.sendSystemMessage(Component.literal("§7You already carry that relic."));
        }
        return true;
    }

    /** Registers a craftable relic for each family so the system has an entry point. */
    public static void ensureRecipes() {
        List<SlayerRecipes.Recipe> out = new ArrayList<>();
        for (Relic r : Relic.values()) {
            // Only the gentler passives are craftable. The rest are found.
            if (r.trigger != Relic.Trigger.PASSIVE) continue;
            ItemStack[] grid = new ItemStack[9];
            for (int i = 0; i < 9; i++) grid[i] = ItemStack.EMPTY;
            grid[4] = new ItemStack(r.icon);
            grid[1] = new ItemStack(net.minecraft.world.item.Items.GOLD_INGOT, 16);
            grid[3] = new ItemStack(net.minecraft.world.item.Items.LAPIS_LAZULI, 24);
            grid[5] = new ItemStack(net.minecraft.world.item.Items.AMETHYST_SHARD, 12);
            grid[7] = new ItemStack(net.minecraft.world.item.Items.ECHO_SHARD, 2);
            boolean[] locked = new boolean[9];
            for (int i = 0; i < 9; i++) locked[i] = !grid[i].isEmpty();
            out.add(new SlayerRecipes.Recipe("VX Relic: " + r.display,
                    createRelicItem(r), grid, locked, null, 0));
        }
        if (!out.isEmpty()) RecipeConfigManager.upsertCustomRecipesSilent(out);
    }
}
