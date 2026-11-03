package com.political;
import com.political.core.Clock;
import com.political.mixin.LivingEntityMixin;



import com.political.bounty.SlayerArmorHandler;
import com.political.bounty.SlayerItems;
import com.political.bounty.T2ArmorAbilityHandler;
import com.political.items.CustomItemHandler;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles tick-based effects for bounty armor pieces that require runtime application.
 * Each individual T1/T2 armor piece applies its own unique buff — no full set bonus needed.
 * Called every server tick from PoliticalServer.
 */
public class BountyArmorHandler {

    /** Minimum horizontal speed when walking through cobwebs with Spider Leggings. */
    private static final double SPIDER_LEGGINGS_MIN_WALK_SPEED = 0.25;
    /** Maximum downward velocity cap when passing through cobwebs with Spider Leggings. */
    private static final double SPIDER_LEGGINGS_MAX_FALL_SPEED = -0.05;

    /** Absorption cooldown per player for Slime T2 Chestplate (30 seconds). */
    private static final Map<UUID, Long> absorptionCooldown = new HashMap<>();
    private static final long ABSORPTION_COOLDOWN_MS = 30_000L;

    public static void tick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            // Crown of Greed: disable all bounty buffs
            if (SlayerItems.isCrownOfGreed(player.getItemBySlot(EquipmentSlot.HEAD))) continue;

            tickSpiderLeggingsEffects(player);   // legendary Venomous Crawler Leggings
            tickZombieArmorEffects(player);
            tickSpiderStandardArmorEffects(player);
            tickSlimeArmorEffects(player);
            tickEndermanArmorEffects(player);
            tickWardenArmorEffects(player);
            tickPiglinArmorEffects(player);
            tickSkeletonArmorEffects(player);
        }
    }

    // =========================================================
    // ZOMBIE ARMOR (Undying Outlaw)
    // =========================================================

    private static void tickZombieArmorEffects(ServerPlayer player) {
        // Helmet: T1 = Hunger immunity | T2 = Hunger immunity + Fire Resistance
        String helmetId = SlayerItems.getCustomItemId(player.getItemBySlot(EquipmentSlot.HEAD));
        if ("zombie_t1_helmet".equals(helmetId) || "zombie_t2_helmet".equals(helmetId)) {
            if (player.hasEffect(MobEffects.HUNGER)) {
                player.removeEffect(MobEffects.HUNGER);
            }
            if ("zombie_t2_helmet".equals(helmetId)) {
                if (!player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                    player.addEffect(new MobEffectInstance(
                            MobEffects.FIRE_RESISTANCE, 40, 0, true, false, false));
                }
            }
        }

        // Chestplate: T2 = +4 Hearts (applied via SlayerArmorHandler attributes)

        // Leggings: T1 = -10% Damage from Undead Mobs
        String legsId = SlayerItems.getCustomItemId(player.getItemBySlot(EquipmentSlot.LEGS));
        if ("zombie_t1_leggings".equals(legsId)) {
            // This would need a mixin to damage calculation to properly implement
            // For now, we'll add a small resistance effect when near undead
            if (player.level().getEntitiesOfClass(net.minecraft.world.entity.monster.zombie.Zombie.class, 
                    player.getBoundingBox().inflate(8), entity -> entity.isAlive()).size() > 0) {
                if (!player.hasEffect(MobEffects.RESISTANCE)) {
                    player.addEffect(new MobEffectInstance(
                            MobEffects.RESISTANCE, 40, 0, true, false, false));
                }
            }
        }

        // Boots: T1 = +10% Knockback Resistance
        String bootsId = SlayerItems.getCustomItemId(player.getItemBySlot(EquipmentSlot.FEET));
        if ("zombie_t1_boots".equals(bootsId)) {
            // Knockback resistance is handled via attributes in SlayerArmorHandler
        }

        // Boots: T2 = Speed I when below 50% health
        if ("zombie_t2_boots".equals(bootsId)) {
            float maxHealth = player.getMaxHealth();
            float currentHealth = player.getHealth();
            if (currentHealth / maxHealth < 0.5f) {
                if (!player.hasEffect(MobEffects.SPEED) ||
                        player.getEffect(MobEffects.SPEED).getAmplifier() < 0) {
                    player.addEffect(new MobEffectInstance(
                            MobEffects.SPEED, 40, 0, true, false, false));
                }
            }
        }
    }

    // =========================================================
    // SPIDER ARMOR — Standard T1/T2 (Venomous Bandit)
    // =========================================================

    private static void tickSpiderStandardArmorEffects(ServerPlayer player) {
        // Helmet: T1 = +5% Movement Speed
        String helmetId = SlayerItems.getCustomItemId(player.getItemBySlot(EquipmentSlot.HEAD));
        if ("spider_t1_helmet".equals(helmetId)) {
            if (!player.hasEffect(MobEffects.SPEED) ||
                    player.getEffect(MobEffects.SPEED) == null ||
                    player.getEffect(MobEffects.SPEED).getAmplifier() < 0) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.SPEED, 40, 0, true, false, false));
            }
        }

        // Chestplate: T1 = Weakness immunity | T2 = Weakness immunity
        String chestId = SlayerItems.getCustomItemId(player.getItemBySlot(EquipmentSlot.CHEST));
        if ("spider_t1_chestplate".equals(chestId) || "spider_t2_chestplate".equals(chestId)) {
            if (player.hasEffect(MobEffects.WEAKNESS)) {
                player.removeEffect(MobEffects.WEAKNESS);
            }
        }

        // Leggings: T1 = Poison immunity | T2 = Poison immunity + Speed I
        String legsId = SlayerItems.getCustomItemId(player.getItemBySlot(EquipmentSlot.LEGS));
        if ("spider_t1_leggings".equals(legsId)) {
            if (player.hasEffect(MobEffects.POISON)) {
                player.removeEffect(MobEffects.POISON);
            }
        }
        if ("spider_t2_leggings".equals(legsId)) {
            if (player.hasEffect(MobEffects.POISON)) {
                player.removeEffect(MobEffects.POISON);
            }
            if (!player.hasEffect(MobEffects.SPEED) ||
                    player.getEffect(MobEffects.SPEED) == null ||
                    player.getEffect(MobEffects.SPEED).getAmplifier() < 0) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.SPEED, 40, 0, true, false, false));
            }
        }

        // Boots: T1 = +10% Movement Speed
        String bootsId = SlayerItems.getCustomItemId(player.getItemBySlot(EquipmentSlot.FEET));
        if ("spider_t1_boots".equals(bootsId)) {
            if (!player.hasEffect(MobEffects.SPEED) ||
                    player.getEffect(MobEffects.SPEED) == null ||
                    player.getEffect(MobEffects.SPEED).getAmplifier() < 0) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.SPEED, 40, 0, true, false, false));
            }
        }
    }

    // =========================================================
    // SLIME ARMOR (Gelatinous Rustler)
    // =========================================================

    private static void tickSlimeArmorEffects(ServerPlayer player) {
        // Helmet: T2 = Saturation boost
        String helmetId = SlayerItems.getCustomItemId(player.getItemBySlot(EquipmentSlot.HEAD));
        if ("slime_t2_helmet".equals(helmetId)) {
            if (!player.hasEffect(MobEffects.SATURATION)) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.SATURATION, 40, 0, true, false, false));
            }
        }

        // Chestplate: T2 = Absorption I every 30 seconds
        String chestId = SlayerItems.getCustomItemId(player.getItemBySlot(EquipmentSlot.CHEST));
        if ("slime_t2_chestplate".equals(chestId)) {
            UUID uuid = player.getUUID();
            long now = Clock.gameMillis();
            Long last = absorptionCooldown.get(uuid);
            if (last == null || (now - last) >= ABSORPTION_COOLDOWN_MS) {
                if (!player.hasEffect(MobEffects.ABSORPTION)) {
                    player.addEffect(new MobEffectInstance(
                            MobEffects.ABSORPTION, 600, 0, true, false, false));
                    absorptionCooldown.put(uuid, now);
                }
            }
        }

        // Leggings: T1 = Jump Boost I | T2 = Jump Boost II
        String legsId = SlayerItems.getCustomItemId(player.getItemBySlot(EquipmentSlot.LEGS));
        if ("slime_t1_leggings".equals(legsId)) {
            if (!player.hasEffect(MobEffects.JUMP_BOOST) ||
                    player.getEffect(MobEffects.JUMP_BOOST) == null ||
                    player.getEffect(MobEffects.JUMP_BOOST).getAmplifier() < 0) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.JUMP_BOOST, 40, 0, true, false, false));
            }
        }
        if ("slime_t2_leggings".equals(legsId)) {
            if (!player.hasEffect(MobEffects.JUMP_BOOST) ||
                    player.getEffect(MobEffects.JUMP_BOOST) == null ||
                    player.getEffect(MobEffects.JUMP_BOOST).getAmplifier() < 1) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.JUMP_BOOST, 40, 1, true, false, false));
            }
        }

        // Boots: T1 = No Fall Damage | T2 = Jump Boost II (no fall damage handled in LivingEntityMixin)
        String bootsId = SlayerItems.getCustomItemId(player.getItemBySlot(EquipmentSlot.FEET));
        if ("slime_t1_boots".equals(bootsId)) {
            // T1 boots should have no fall damage - this would need a mixin to properly implement
            // For now, we'll give slow falling as a approximation
            if (player.fallDistance > 2.0f) {
                if (!player.hasEffect(MobEffects.SLOW_FALLING)) {
                    player.addEffect(new MobEffectInstance(
                            MobEffects.SLOW_FALLING, 40, 0, true, false, false));
                }
            }
        }
        if ("slime_t2_boots".equals(bootsId)) {
            if (!player.hasEffect(MobEffects.JUMP_BOOST) ||
                    player.getEffect(MobEffects.JUMP_BOOST) == null ||
                    player.getEffect(MobEffects.JUMP_BOOST).getAmplifier() < 1) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.JUMP_BOOST, 40, 1, true, false, false));
            }
        }
    }

    // =========================================================
    // ENDERMAN ARMOR — T1 individual effects
    // (T2 set effects are handled by T2ArmorAbilityHandler)
    // =========================================================

    private static void tickEndermanArmorEffects(ServerPlayer player) {
        // T1 and T2 Helmet: Night Vision
        String helmetId = SlayerItems.getCustomItemId(player.getItemBySlot(EquipmentSlot.HEAD));
        if ("enderman_t1_helmet".equals(helmetId) || "enderman_t2_helmet".equals(helmetId)) {
            if (!player.hasEffect(MobEffects.NIGHT_VISION) ||
                    player.getEffect(MobEffects.NIGHT_VISION) == null ||
                    player.getEffect(MobEffects.NIGHT_VISION).getDuration() < 30) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.NIGHT_VISION, 400, 0, true, false, false));
            }
        }
    }

    // =========================================================
    // WARDEN ARMOR (Sculk Terror)
    // =========================================================

    private static void tickWardenArmorEffects(ServerPlayer player) {
        // T1 and T2 Helmet: Darkness immunity (remove Darkness effect), suppressed during Ender Phase Mode
        String helmetId = SlayerItems.getCustomItemId(player.getItemBySlot(EquipmentSlot.HEAD));
        if ("warden_t1_helmet".equals(helmetId) || "warden_t2_helmet".equals(helmetId)) {
            if (player.hasEffect(MobEffects.DARKNESS) && !CustomItemHandler.isInEnderPhaseMode(player.getUUID())) {
                player.removeEffect(MobEffects.DARKNESS);
            }
        }

        // T2 Chestplate: Resistance I
        String chestId = SlayerItems.getCustomItemId(player.getItemBySlot(EquipmentSlot.CHEST));
        if ("warden_t2_chestplate".equals(chestId)) {
            if (!player.hasEffect(MobEffects.RESISTANCE) ||
                    player.getEffect(MobEffects.RESISTANCE) == null ||
                    player.getEffect(MobEffects.RESISTANCE).getAmplifier() < 0) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.RESISTANCE, 40, 0, true, false, false));
            }
        }

        // T1 Boots: Vibration sense (8 blocks), T2 Boots: wider vibration sense (12 blocks)
        String bootsId = SlayerItems.getCustomItemId(player.getItemBySlot(EquipmentSlot.FEET));
        if ("warden_t1_boots".equals(bootsId) || "warden_t2_boots".equals(bootsId)) {
            int vibrationRange = "warden_t2_boots".equals(bootsId) ? 12 : 8;
            ServerLevel world = player.level();
            if (world.getServer().getTickCount() % 20 == 0) {
                AABB searchBox = player.getBoundingBox().inflate(vibrationRange);
                for (LivingEntity entity : world.getEntitiesOfClass(LivingEntity.class, searchBox,
                        e -> e instanceof Monster)) {
                    entity.addEffect(new MobEffectInstance(
                            MobEffects.GLOWING, 60, 0, true, false, false)); // 3s, expires naturally
                }
            }
        }
    }

    // =========================================================
    // PIGLIN ARMOR (Gilded Ravager)
    // =========================================================

    private static void tickPiglinArmorEffects(ServerPlayer player) {
        // T1 Helmet & T2 Helmet: Fire Resistance
        String helmetId = SlayerItems.getCustomItemId(player.getItemBySlot(EquipmentSlot.HEAD));
        if ("piglin_t1_helmet".equals(helmetId) || "piglin_t2_helmet".equals(helmetId)) {
            if (!player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.FIRE_RESISTANCE, 40, 0, true, false, false));
            }
        }

        // T1 Boots & T2 Boots: Fire Resistance
        String bootsId = SlayerItems.getCustomItemId(player.getItemBySlot(EquipmentSlot.FEET));
        if ("piglin_t1_boots".equals(bootsId) || "piglin_t2_boots".equals(bootsId)) {
            if (!player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.FIRE_RESISTANCE, 40, 0, true, false, false));
            }
        }
    }

    // =========================================================
    // LEGENDARY: Venomous Crawler Leggings
    // =========================================================

    private static void tickSpiderLeggingsEffects(ServerPlayer player) {
        ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
        if (!SlayerItems.isSpiderLeggings(leggings)) return;
        if (!SlayerItems.canUseSpiderLeggings(player)) return;

        // Poison immunity: continuously remove poison
        if (player.hasEffect(MobEffects.POISON)) {
            player.removeEffect(MobEffects.POISON);
        }

        // Speed boost: keep Speed II active while wearing spider leggings
        if (!player.hasEffect(MobEffects.SPEED) ||
                player.getEffect(MobEffects.SPEED) == null ||
                player.getEffect(MobEffects.SPEED).getAmplifier() < 1) {
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 1, false, false, false));
        }

        // Web immunity: if standing in a cobweb, restore velocity to walk through freely
        ServerLevel world = player.level();
        BlockPos pos = player.blockPosition();
        if (world.getBlockState(pos).is(Blocks.COBWEB)) {
            Vec3 vel = player.getDeltaMovement();
            double minSpeed = SPIDER_LEGGINGS_MIN_WALK_SPEED;
            double vx = Math.abs(vel.x) < minSpeed && vel.x != 0 ? Math.signum(vel.x) * minSpeed : vel.x;
            double vz = Math.abs(vel.z) < minSpeed && vel.z != 0 ? Math.signum(vel.z) * minSpeed : vel.z;
            double vy = vel.y < SPIDER_LEGGINGS_MAX_FALL_SPEED ? SPIDER_LEGGINGS_MAX_FALL_SPEED : vel.y;
            player.setDeltaMovement(vx, vy, vz);
        }
    }

    // =========================================================
    // SKELETON ARMOR (Bone Marksman) - T1 Effects
    // =========================================================

    private static void tickSkeletonArmorEffects(ServerPlayer player) {
        // Leggings: T1 = +5% Movement Speed
        String legsId = SlayerItems.getCustomItemId(player.getItemBySlot(EquipmentSlot.LEGS));
        if ("skeleton_t1_leggings".equals(legsId)) {
            if (!player.hasEffect(MobEffects.SPEED) ||
                    player.getEffect(MobEffects.SPEED) == null ||
                    player.getEffect(MobEffects.SPEED).getAmplifier() < 0) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.SPEED, 40, 0, true, false, false));
            }
        }

        // Boots: T1 = +5% Movement Speed
        String bootsId = SlayerItems.getCustomItemId(player.getItemBySlot(EquipmentSlot.FEET));
        if ("skeleton_t1_boots".equals(bootsId)) {
            if (!player.hasEffect(MobEffects.SPEED) ||
                    player.getEffect(MobEffects.SPEED) == null ||
                    player.getEffect(MobEffects.SPEED).getAmplifier() < 0) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.SPEED, 40, 0, true, false, false));
            }
        }
    }

    // =========================================================
    // STATIC HELPERS — used by mixins for damage/effect checks
    // =========================================================

    /**
     * Returns the damage multiplier when player takes damage from an undead mob.
     * Zombie T1 Leggings: 0.90 (-10%) | T2 Leggings: 0.80 (-20%)
     */
    public static float getUndeadDamageReduction(ServerPlayer player) {
        if (SlayerItems.isCrownOfGreed(player.getItemBySlot(EquipmentSlot.HEAD))) return 1.0f;
        String legsId = SlayerItems.getCustomItemId(player.getItemBySlot(EquipmentSlot.LEGS));
        if ("zombie_t2_leggings".equals(legsId)) return 0.80f;
        if ("zombie_t1_leggings".equals(legsId)) return 0.90f;
        return 1.0f;
    }

    /**
     * Returns true if the player's Spider T2 Chestplate should poison melee attackers.
     */
    public static boolean hasPoisonThorns(ServerPlayer player) {
        if (SlayerItems.isCrownOfGreed(player.getItemBySlot(EquipmentSlot.HEAD))) return false;
        String chestId = SlayerItems.getCustomItemId(player.getItemBySlot(EquipmentSlot.CHEST));
        return "spider_t2_chestplate".equals(chestId);
    }

    /**
     * Returns the projectile damage output multiplier for the player based on skeleton helmet.
     * Skeleton T1 Helmet: 1.10 (+10%) | T2 Helmet: 1.20 (+20%)
     */
    public static float getProjectileDamageBoost(ServerPlayer player) {
        if (SlayerItems.isCrownOfGreed(player.getItemBySlot(EquipmentSlot.HEAD))) return 1.0f;
        String helmetId = SlayerItems.getCustomItemId(player.getItemBySlot(EquipmentSlot.HEAD));
        if ("skeleton_t2_helmet".equals(helmetId)) return 1.20f;
        if ("skeleton_t1_helmet".equals(helmetId)) return 1.10f;
        return 1.0f;
    }

    /**
     * Returns true if the player has Skeleton T2 Helmet equipped (arrows apply Glowing to targets).
     */
    public static boolean hasGlowingArrows(ServerPlayer player) {
        if (SlayerItems.isCrownOfGreed(player.getItemBySlot(EquipmentSlot.HEAD))) return false;
        String helmetId = SlayerItems.getCustomItemId(player.getItemBySlot(EquipmentSlot.HEAD));
        return "skeleton_t2_helmet".equals(helmetId);
    }

    /**
     * Returns the projectile damage INCOMING multiplier for the player based on skeleton T1 chestplate.
     * Skeleton T1 Chestplate: 0.90 (-10%).
     * T2 chestplate resistance is handled by T2ArmorAbilityHandler.getProjectileDamageReduction().
     *
     * <p>Crown of Greed suppresses all bounty-gear bonuses, including this resistance.
     */
    public static float getProjectileDamageResistance(ServerPlayer player) {
        if (SlayerItems.isCrownOfGreed(player.getItemBySlot(EquipmentSlot.HEAD))) return 1.0f;
        String chestId = SlayerItems.getCustomItemId(player.getItemBySlot(EquipmentSlot.CHEST));
        if ("skeleton_t1_chestplate".equals(chestId)) return 0.90f;
        return 1.0f;
    }
    
    // ════════════════════════════════════════════════════════════
    // BOUNTY PROTECTION EXTENSION (Coin Sink Integration)
    // ════════════════════════════════════════════════════════════
    
    private static final Map<String, Long> bountyProtectionExpirations = new HashMap<>();
    
    public static void extendProtection(String uuid, long durationMs) {
        long current = bountyProtectionExpirations.getOrDefault(uuid, 0L);
        bountyProtectionExpirations.put(uuid, Math.max(current, Clock.gameMillis()) + durationMs);
    }
    
    public static boolean hasBountyProtection(String uuid) {
        Long exp = bountyProtectionExpirations.get(uuid);
        return exp != null && exp > Clock.gameMillis();
    }
    
    public static long getProtectionRemaining(String uuid) {
        Long exp = bountyProtectionExpirations.get(uuid);
        if (exp == null) return 0;
        return Math.max(0, exp - Clock.gameMillis());
    }
}
