package com.political;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * What the enchantments actually do.
 *
 * <p>Split from the manager on purpose. The manager is the parser and never touches the world, so it
 * can be called from a menu, a command or a test without dragging combat in behind it. This class is
 * the other half: it is called from the damage mixin and the kill hook and nowhere else, and every
 * method here takes the level it should run at rather than looking it up twice.</p>
 *
 * <p>Stat shaped enchantments are deliberately absent. Vitality, Fleetfoot, Scholar, Keen Edge,
 * Devastate, Prospector, Bulwark and Second Skin all feed the stat sheet instead, in
 * {@link #contributeStats}, because a number the player can see on their own stat screen is worth
 * more than a hidden multiplier applied at the moment of the hit.</p>
 */
public final class CustomEnchantEffects {

    private CustomEnchantEffects() {}

    // Per player scratch state. Small, bounded by player count, cleared on logout.
    private static final Map<UUID, UUID> LAST_TARGET = new HashMap<>();
    private static final Map<UUID, Integer> HIT_STREAK = new HashMap<>();
    private static final Map<UUID, Integer> SWING_COUNT = new HashMap<>();
    private static final Map<UUID, Long> LIFELINE_READY = new HashMap<>();
    private static final Map<UUID, Long> SECOND_WIND_READY = new HashMap<>();
    private static final Map<UUID, Integer> CHAIN_STACKS = new HashMap<>();
    private static final Map<UUID, Long> CHAIN_UNTIL = new HashMap<>();
    private static final Map<UUID, Long> COUNTER_UNTIL = new HashMap<>();
    private static final Map<UUID, Long> LAST_REGROWTH = new HashMap<>();

    public static void forget(ServerPlayer player) {
        UUID id = player.getUUID();
        LAST_TARGET.remove(id);
        HIT_STREAK.remove(id);
        SWING_COUNT.remove(id);
        LIFELINE_READY.remove(id);
        SECOND_WIND_READY.remove(id);
        CHAIN_STACKS.remove(id);
        CHAIN_UNTIL.remove(id);
        COUNTER_UNTIL.remove(id);
        LAST_REGROWTH.remove(id);
    }

    private static long now() {
        return PoliticalServer.server == null ? 0L : PoliticalServer.server.getTickCount();
    }

    // ── Outgoing ──────────────────────────────────────────────────────────

    /**
     * Everything the held weapon does on a hit. Returns the adjusted damage.
     *
     * <p>Read once, applied once. The whole enchantment set on the weapon is parsed a single time at
     * the top rather than eighty separate lore scans, which is the difference between this being
     * free and this being the reason the server stutters in a mob farm.</p>
     */
    public static float onHit(ServerPlayer player, LivingEntity target, float damage) {
        ItemStack weapon = player.getMainHandItem();
        Map<CustomEnchant, Integer> on = CustomEnchantManager.read(weapon);
        if (on.isEmpty()) return damage;

        UUID id = player.getUUID();
        float dmg = damage;
        ServerLevel level = player.level() instanceof ServerLevel sl ? sl : null;

        // Streak tracking, shared by First Strike and Relentless.
        UUID prev = LAST_TARGET.get(id);
        int streak;
        if (target.getUUID().equals(prev)) {
            streak = HIT_STREAK.merge(id, 1, Integer::sum);
        } else {
            LAST_TARGET.put(id, target.getUUID());
            HIT_STREAK.put(id, 0);
            streak = 0;
        }
        int swing = SWING_COUNT.merge(id, 1, Integer::sum);

        for (Map.Entry<CustomEnchant, Integer> e : on.entrySet()) {
            int lv = e.getValue();
            switch (e.getKey()) {
                case EXECUTIONER -> {
                    if (target.getHealth() <= target.getMaxHealth() * 0.5f) {
                        dmg *= 1f + 0.15f * lv;
                    }
                }
                case FIRST_STRIKE -> {
                    if (streak == 0) dmg *= 1f + 0.20f * lv;
                }
                case RELENTLESS -> dmg *= 1f + Math.min(0.05f * lv * streak, 0.10f * lv);
                case GIANT_SLAYER -> {
                    float ratio = target.getMaxHealth() / Math.max(1f, player.getMaxHealth());
                    if (ratio > 1f) dmg *= 1f + Math.min(ratio - 1f, 4f) * 0.05f * lv;
                }
                case DUELIST -> {
                    if (nearby(player, 8) <= 1) dmg *= 1f + 0.08f * lv;
                }
                case SWARMBANE -> {
                    int n = nearby(player, 8);
                    if (n >= 3) dmg *= 1f + Math.min(n, 10) * 0.015f * lv;
                }
                case MOMENTUM -> {
                    double speed = player.getDeltaMovement().horizontalDistance();
                    dmg *= 1f + (float) Math.min(speed, 1.0) * 0.25f * lv;
                }
                case LAST_WORD -> {
                    if (player.getHealth() <= player.getMaxHealth() * 0.20f) {
                        dmg *= 1f + 0.35f * lv;
                    }
                }
                case OVERWHELM -> dmg *= 1f + 0.06f * lv;
                case PERFECT_FORM -> {
                    if (swing % 5 == 0) dmg *= 1f + 0.20f * lv;
                }
                case BUTCHER -> {
                    if (target.getType().getCategory() == net.minecraft.world.entity.MobCategory.CREATURE
                            || target.isInvertedHealAndHarm()) {
                        dmg *= 1f + 0.10f * lv;
                    }
                }
                case BLOODTHIRST -> player.heal(Math.min(dmg * 0.03f * lv, 6f));
                case VENOMOUS_EDGE -> target.addEffect(
                        new MobEffectInstance(MobEffects.POISON, 40 + 20 * lv, Math.min(lv - 1, 3)));
                case FROSTBRAND -> {
                    target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40 + 20 * lv,
                            Math.min(lv - 1, 3)));
                    if (lv >= 3) target.setTicksFrozen(Math.min(target.getTicksFrozen() + 60, 300));
                }
                case SOULREND -> target.addEffect(
                        new MobEffectInstance(MobEffects.WITHER, 40 + 20 * lv, Math.min(lv - 1, 2)));
                case EXPOSE -> target.addEffect(
                        new MobEffectInstance(MobEffects.WEAKNESS, 60 + 20 * lv, 0));
                case THUNDERSTRUCK -> {
                    if (level != null && player.getRandom().nextFloat() < 0.02f * lv) {
                        var bolt = EntityType.LIGHTNING_BOLT.create(level,
                                net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
                        if (bolt != null) {
                            bolt.snapTo(target.getX(), target.getY(), target.getZ());
                            level.addFreshEntity(bolt);
                        }
                    }
                }
                case CLEAVE -> {
                    if (level != null) {
                        float share = dmg * 0.10f * lv;
                        for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class,
                                target.getBoundingBox().inflate(3.0),
                                o -> o != target && o != player && o.isAlive())) {
                            other.hurtServer(level, player.damageSources().playerAttack(player), share);
                        }
                    }
                }
                case PIERCING_WILL -> {
                    if (level != null) {
                        // Deliberately a second hit through the generic source, since the point of
                        // this one is that it does not go through the target's armour maths.
                        target.hurtServer(level, player.damageSources().magic(), dmg * 0.04f * lv);
                    }
                }
                default -> { }
            }
        }

        // Chain Kill stacks, banked by the kill hook and spent here.
        Long until = CHAIN_UNTIL.get(id);
        if (until != null && until > now()) {
            int stacks = CHAIN_STACKS.getOrDefault(id, 0);
            Integer ck = on.get(CustomEnchant.CHAIN_KILL);
            if (ck != null) dmg *= 1f + Math.min(stacks, 10) * 0.02f * ck;
        }

        // Counterpoise, banked when the player was hit.
        Long counter = COUNTER_UNTIL.get(id);
        if (counter != null && counter > now()) {
            int lv = CustomEnchantManager.wornTotal(player, CustomEnchant.COUNTERPOISE);
            if (lv > 0) dmg *= 1f + 0.03f * lv;
        }

        return dmg;
    }

    // ── Incoming ──────────────────────────────────────────────────────────

    /** Everything worn armour does when the player is hit. Returns the adjusted damage. */
    public static float onHurt(ServerPlayer player, DamageSource source, float damage) {
        float dmg = damage;
        UUID id = player.getUUID();
        ServerLevel level = player.level() instanceof ServerLevel sl ? sl : null;

        int bulwark = CustomEnchantManager.wornTotal(player, CustomEnchant.BULWARK);
        if (bulwark > 0) dmg = Math.max(dmg * 0.2f, dmg - bulwark * 0.4f);

        int stoicism = CustomEnchantManager.wornTotal(player, CustomEnchant.STOICISM);
        if (stoicism > 0) {
            float missing = 1f - (player.getHealth() / Math.max(1f, player.getMaxHealth()));
            dmg *= 1f - Math.min(missing * 0.03f * stoicism, 0.40f);
        }

        int ironclad = CustomEnchantManager.wornTotal(player, CustomEnchant.IRONCLAD);
        if (ironclad > 0 && source.is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)) {
            dmg *= 1f - Math.min(0.03f * ironclad, 0.45f);
        }

        int firewalk = CustomEnchantManager.wornTotal(player, CustomEnchant.FIREWALK);
        if (firewalk > 0 && source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
            dmg *= 1f - Math.min(0.20f * firewalk, 0.90f);
        }

        int warding = CustomEnchantManager.wornTotal(player, CustomEnchant.WARDING);
        if (warding > 0 && (source.is(net.minecraft.tags.DamageTypeTags.WITCH_RESISTANT_TO)
                || source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR))) {
            dmg *= 1f - Math.min(0.04f * warding, 0.50f);
        }

        int surefooted = CustomEnchantManager.wornTotal(player, CustomEnchant.SUREFOOTED);
        if (surefooted > 0 && source.is(net.minecraft.tags.DamageTypeTags.IS_FALL)) {
            dmg *= 1f - Math.min(0.34f * surefooted, 1.0f);
        }

        // Thornmail returns a share to whoever swung, melee only.
        int thorn = CustomEnchantManager.wornTotal(player, CustomEnchant.THORNMAIL);
        if (thorn > 0 && level != null && source.getEntity() instanceof LivingEntity attacker
                && !source.is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)) {
            attacker.hurtServer(level, player.damageSources().thorns(player), dmg * 0.05f * thorn);
        }

        // Counterpoise banks a short window of extra damage.
        int counter = CustomEnchantManager.wornTotal(player, CustomEnchant.COUNTERPOISE);
        if (counter > 0) COUNTER_UNTIL.put(id, now() + 100);

        // Lifeline, on a long cooldown, heals when the hit takes the player low.
        int lifeline = CustomEnchantManager.wornTotal(player, CustomEnchant.LIFELINE);
        if (lifeline > 0 && player.getHealth() - dmg <= player.getMaxHealth() / 3f) {
            long ready = LIFELINE_READY.getOrDefault(id, 0L);
            if (now() >= ready) {
                LIFELINE_READY.put(id, now() + 2400L);
                player.heal(player.getMaxHealth() * 0.06f * lifeline);
                player.sendSystemMessage(Component.literal("§c✚ Lifeline"), true);
                if (level != null) {
                    level.playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE,
                            SoundSource.PLAYERS, 0.6f, 1.6f);
                }
            }
        }

        // Second Wind turns one killing blow a fight into a sliver of health.
        int wind = CustomEnchantManager.wornTotal(player, CustomEnchant.SECOND_WIND);
        if (wind > 0 && player.getHealth() - dmg <= 0f) {
            long ready = SECOND_WIND_READY.getOrDefault(id, 0L);
            if (now() >= ready) {
                SECOND_WIND_READY.put(id, now() + 6000L);
                player.setHealth(Math.max(1f, player.getMaxHealth() * 0.05f * wind));
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 100, 2));
                player.sendSystemMessage(Component.literal("§d✦ Second Wind"), true);
                if (level != null) {
                    level.playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE,
                            SoundSource.PLAYERS, 1.0f, 1.0f);
                }
                return 0f;
            }
        }

        return dmg;
    }

    // ── Kills ─────────────────────────────────────────────────────────────

    /** Fires once when a player kills something. */
    public static void onKill(ServerPlayer player, LivingEntity victim) {
        ItemStack weapon = player.getMainHandItem();
        Map<CustomEnchant, Integer> on = CustomEnchantManager.read(weapon);
        UUID id = player.getUUID();

        Integer tithe = on.get(CustomEnchant.TITHE);
        if (tithe != null) {
            CoinManager.giveCoinsQuiet(player, 12L * tithe);
        }

        Integer chain = on.get(CustomEnchant.CHAIN_KILL);
        if (chain != null) {
            CHAIN_STACKS.merge(id, 1, Integer::sum);
            CHAIN_UNTIL.put(id, now() + 120);
        } else {
            CHAIN_STACKS.remove(id);
        }

        int scavenger = CustomEnchantManager.wornTotal(player, CustomEnchant.SCAVENGER);
        if (scavenger > 0) {
            CoinManager.giveCoinsQuiet(player, 3L * scavenger);
        }

        Integer heads = on.get(CustomEnchant.HEADHUNTER);
        if (heads != null && player.getRandom().nextFloat() < 0.02f * heads
                && player.level() instanceof ServerLevel level) {
            ItemStack skull = headFor(victim);
            if (!skull.isEmpty()) {
                net.minecraft.world.entity.item.ItemEntity drop =
                        new net.minecraft.world.entity.item.ItemEntity(level,
                                victim.getX(), victim.getY(), victim.getZ(), skull);
                level.addFreshEntity(drop);
            }
        }
    }

    private static ItemStack headFor(LivingEntity victim) {
        var type = victim.getType();
        if (type == EntityType.ZOMBIE) return new ItemStack(net.minecraft.world.item.Items.ZOMBIE_HEAD);
        if (type == EntityType.SKELETON) return new ItemStack(net.minecraft.world.item.Items.SKELETON_SKULL);
        if (type == EntityType.CREEPER) return new ItemStack(net.minecraft.world.item.Items.CREEPER_HEAD);
        if (type == EntityType.WITHER_SKELETON) return new ItemStack(net.minecraft.world.item.Items.WITHER_SKELETON_SKULL);
        if (type == EntityType.PIGLIN) return new ItemStack(net.minecraft.world.item.Items.PIGLIN_HEAD);
        return ItemStack.EMPTY;
    }

    // ── The slow tick ─────────────────────────────────────────────────────

    /**
     * Runs on the server tick loop for the passive armour effects.
     *
     * <p>Kept to once every two seconds and to a handful of cheap lookups, because this is the only
     * part of the enchantment system that costs anything when nobody is fighting.</p>
     */
    public static void tick() {
        if (PoliticalServer.server == null) return;
        if (PoliticalServer.server.getTickCount() % 40 != 0) return;

        for (ServerPlayer player : PoliticalServer.server.getPlayerList().getPlayers()) {
            int regrowth = CustomEnchantManager.wornTotal(player, CustomEnchant.REGROWTH);
            if (regrowth > 0 && player.getHealth() < player.getMaxHealth()
                    && player.getLastHurtByMobTimestamp() + 100 < player.tickCount) {
                player.heal(0.3f * regrowth);
            }

            int night = CustomEnchantManager.wornTotal(player, CustomEnchant.NIGHT_SIGHT);
            if (night > 0) {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, true, false));
            }

            int deep = CustomEnchantManager.wornTotal(player, CustomEnchant.DEEP_BREATH);
            if (deep > 0) {
                player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 100, 0, true, false));
                if (deep >= 2) {
                    player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 100, 0, true, false));
                }
            }

            int fire = CustomEnchantManager.wornTotal(player, CustomEnchant.FIREWALK);
            if (fire >= 3) {
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100, 0, true, false));
            }

            int resil = CustomEnchantManager.wornTotal(player, CustomEnchant.RESILIENCE);
            if (resil >= 4 && player.getRandom().nextFloat() < 0.25f) {
                // Shave one harmful effect rather than clearing the lot, so this helps without
                // making status effects stop mattering entirely.
                for (MobEffectInstance eff : new java.util.ArrayList<>(player.getActiveEffects())) {
                    if (!eff.getEffect().value().isBeneficial()) {
                        player.removeEffect(eff.getEffect());
                        break;
                    }
                }
            }

            int hands = CustomEnchantManager.wornTotal(player, CustomEnchant.QUICK_HANDS);
            if (hands > 0 && player.level() instanceof ServerLevel level) {
                double r = 2.0 + hands;
                BlockPos p = player.blockPosition();
                for (var drop : level.getEntitiesOfClass(
                        net.minecraft.world.entity.item.ItemEntity.class,
                        new AABB(p).inflate(r), it -> !it.hasPickUpDelay())) {
                    drop.setDeltaMovement(player.position()
                            .subtract(drop.position()).normalize().scale(0.25));
                }
            }
        }
    }

    // ── Stats ─────────────────────────────────────────────────────────────

    /**
     * Folds the stat shaped enchantments into the player's sheet.
     *
     * <p>Called from the stat engine rather than from combat, which means the player sees the numbers
     * on their own stat screen and everything downstream that already reads the sheet gets them for
     * free.</p>
     */
    public static void contributeStats(ServerPlayer player, PlayerStatsManager.StatSheet sheet) {
        int vitality = CustomEnchantManager.wornTotal(player, CustomEnchant.VITALITY);
        if (vitality > 0) sheet.addExternal(PlayerStatsManager.Stat.HEALTH, vitality * 15.0);

        int fleet = CustomEnchantManager.wornTotal(player, CustomEnchant.FLEETFOOT);
        if (fleet > 0) sheet.addExternal(PlayerStatsManager.Stat.SPEED, fleet * 2.0);

        int scholar = CustomEnchantManager.wornTotal(player, CustomEnchant.SCHOLAR);
        if (scholar > 0) sheet.addExternal(PlayerStatsManager.Stat.INTELLIGENCE, scholar * 15.0);

        int prospector = CustomEnchantManager.wornTotal(player, CustomEnchant.PROSPECTOR);
        if (prospector > 0) sheet.addExternal(PlayerStatsManager.Stat.MAGIC_FIND, prospector * 2.0);

        int skin = CustomEnchantManager.wornTotal(player, CustomEnchant.SECOND_SKIN);
        if (skin > 0) sheet.addExternal(PlayerStatsManager.Stat.DEFENSE, skin * 8.0);

        // Weapon side. Held only, since a crit bonus on a sword in the backpack would be nonsense.
        ItemStack weapon = player.getMainHandItem();
        int keen = CustomEnchantManager.levelOf(weapon, CustomEnchant.KEEN_EDGE);
        if (keen > 0) sheet.addExternal(PlayerStatsManager.Stat.CRIT_CHANCE, keen * 3.0);

        int dev = CustomEnchantManager.levelOf(weapon, CustomEnchant.DEVASTATE);
        if (dev > 0) sheet.addExternal(PlayerStatsManager.Stat.CRIT_DAMAGE, dev * 10.0);

        int trophy = CustomEnchantManager.levelOf(weapon, CustomEnchant.TROPHY_HUNTER);
        if (trophy > 0) sheet.addExternal(PlayerStatsManager.Stat.MAGIC_FIND, trophy * 3.0);

        int unshake = CustomEnchantManager.wornTotal(player, CustomEnchant.UNSHAKEABLE);
        if (unshake > 0) sheet.addExternal(PlayerStatsManager.Stat.DEFENSE, unshake * 3.0);
    }

    /** The extra a Haggler gets paid, as a multiplier. */
    public static double sellMultiplier(ServerPlayer player) {
        int lv = CustomEnchantManager.wornTotal(player, CustomEnchant.HAGGLER);
        return 1.0 + Math.min(lv * 0.02, 0.30);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private static int nearby(ServerPlayer player, double radius) {
        if (!(player.level() instanceof ServerLevel level)) return 1;
        return level.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(radius),
                e -> e != player && e.isAlive() && !(e instanceof ServerPlayer)).size();
    }
}
