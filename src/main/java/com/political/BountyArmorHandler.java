package com.political;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

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
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            // Crown of Greed: disable all bounty buffs
            if (SlayerItems.isCrownOfGreed(player.getEquippedStack(EquipmentSlot.HEAD))) continue;

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

    private static void tickZombieArmorEffects(ServerPlayerEntity player) {
        // Helmet: T1 = Hunger immunity | T2 = Hunger immunity + Fire Resistance
        String helmetId = SlayerItems.getCustomItemId(player.getEquippedStack(EquipmentSlot.HEAD));
        if ("zombie_t1_helmet".equals(helmetId) || "zombie_t2_helmet".equals(helmetId)) {
            if (player.hasStatusEffect(StatusEffects.HUNGER)) {
                player.removeStatusEffect(StatusEffects.HUNGER);
            }
            if ("zombie_t2_helmet".equals(helmetId)) {
                if (!player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) {
                    player.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.FIRE_RESISTANCE, 40, 0, true, false, false));
                }
            }
        }

        // Chestplate: T2 = +4 Hearts (applied via SlayerArmorHandler attributes)

        // Leggings: T1 = -10% Damage from Undead Mobs
        String legsId = SlayerItems.getCustomItemId(player.getEquippedStack(EquipmentSlot.LEGS));
        if ("zombie_t1_leggings".equals(legsId)) {
            // This would need a mixin to damage calculation to properly implement
            // For now, we'll add a small resistance effect when near undead
            if (player.getEntityWorld().getEntitiesByClass(net.minecraft.entity.mob.ZombieEntity.class, 
                    player.getBoundingBox().expand(8), entity -> entity.isAlive()).size() > 0) {
                if (!player.hasStatusEffect(StatusEffects.RESISTANCE)) {
                    player.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.RESISTANCE, 40, 0, true, false, false));
                }
            }
        }

        // Boots: T1 = +10% Knockback Resistance
        String bootsId = SlayerItems.getCustomItemId(player.getEquippedStack(EquipmentSlot.FEET));
        if ("zombie_t1_boots".equals(bootsId)) {
            // Knockback resistance is handled via attributes in SlayerArmorHandler
        }

        // Boots: T2 = Speed I when below 50% health
        if ("zombie_t2_boots".equals(bootsId)) {
            float maxHealth = player.getMaxHealth();
            float currentHealth = player.getHealth();
            if (currentHealth / maxHealth < 0.5f) {
                if (!player.hasStatusEffect(StatusEffects.SPEED) ||
                        player.getStatusEffect(StatusEffects.SPEED).getAmplifier() < 0) {
                    player.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.SPEED, 40, 0, true, false, false));
                }
            }
        }
    }

    // =========================================================
    // SPIDER ARMOR — Standard T1/T2 (Venomous Bandit)
    // =========================================================

    private static void tickSpiderStandardArmorEffects(ServerPlayerEntity player) {
        // Helmet: T1 = +5% Movement Speed
        String helmetId = SlayerItems.getCustomItemId(player.getEquippedStack(EquipmentSlot.HEAD));
        if ("spider_t1_helmet".equals(helmetId)) {
            if (!player.hasStatusEffect(StatusEffects.SPEED) ||
                    player.getStatusEffect(StatusEffects.SPEED) == null ||
                    player.getStatusEffect(StatusEffects.SPEED).getAmplifier() < 0) {
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.SPEED, 40, 0, true, false, false));
            }
        }

        // Chestplate: T1 = Weakness immunity | T2 = Weakness immunity
        String chestId = SlayerItems.getCustomItemId(player.getEquippedStack(EquipmentSlot.CHEST));
        if ("spider_t1_chestplate".equals(chestId) || "spider_t2_chestplate".equals(chestId)) {
            if (player.hasStatusEffect(StatusEffects.WEAKNESS)) {
                player.removeStatusEffect(StatusEffects.WEAKNESS);
            }
        }

        // Leggings: T1 = Poison immunity | T2 = Poison immunity + Speed I
        String legsId = SlayerItems.getCustomItemId(player.getEquippedStack(EquipmentSlot.LEGS));
        if ("spider_t1_leggings".equals(legsId)) {
            if (player.hasStatusEffect(StatusEffects.POISON)) {
                player.removeStatusEffect(StatusEffects.POISON);
            }
        }
        if ("spider_t2_leggings".equals(legsId)) {
            if (player.hasStatusEffect(StatusEffects.POISON)) {
                player.removeStatusEffect(StatusEffects.POISON);
            }
            if (!player.hasStatusEffect(StatusEffects.SPEED) ||
                    player.getStatusEffect(StatusEffects.SPEED) == null ||
                    player.getStatusEffect(StatusEffects.SPEED).getAmplifier() < 0) {
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.SPEED, 40, 0, true, false, false));
            }
        }

        // Boots: T1 = +10% Movement Speed
        String bootsId = SlayerItems.getCustomItemId(player.getEquippedStack(EquipmentSlot.FEET));
        if ("spider_t1_boots".equals(bootsId)) {
            if (!player.hasStatusEffect(StatusEffects.SPEED) ||
                    player.getStatusEffect(StatusEffects.SPEED) == null ||
                    player.getStatusEffect(StatusEffects.SPEED).getAmplifier() < 0) {
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.SPEED, 40, 0, true, false, false));
            }
        }
    }

    // =========================================================
    // SLIME ARMOR (Gelatinous Rustler)
    // =========================================================

    private static void tickSlimeArmorEffects(ServerPlayerEntity player) {
        // Helmet: T2 = Saturation boost
        String helmetId = SlayerItems.getCustomItemId(player.getEquippedStack(EquipmentSlot.HEAD));
        if ("slime_t2_helmet".equals(helmetId)) {
            if (!player.hasStatusEffect(StatusEffects.SATURATION)) {
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.SATURATION, 40, 0, true, false, false));
            }
        }

        // Chestplate: T2 = Absorption I every 30 seconds
        String chestId = SlayerItems.getCustomItemId(player.getEquippedStack(EquipmentSlot.CHEST));
        if ("slime_t2_chestplate".equals(chestId)) {
            UUID uuid = player.getUuid();
            long now = System.currentTimeMillis();
            Long last = absorptionCooldown.get(uuid);
            if (last == null || (now - last) >= ABSORPTION_COOLDOWN_MS) {
                if (!player.hasStatusEffect(StatusEffects.ABSORPTION)) {
                    player.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.ABSORPTION, 600, 0, true, false, false));
                    absorptionCooldown.put(uuid, now);
                }
            }
        }

        // Leggings: T1 = Jump Boost I | T2 = Jump Boost II
        String legsId = SlayerItems.getCustomItemId(player.getEquippedStack(EquipmentSlot.LEGS));
        if ("slime_t1_leggings".equals(legsId)) {
            if (!player.hasStatusEffect(StatusEffects.JUMP_BOOST) ||
                    player.getStatusEffect(StatusEffects.JUMP_BOOST) == null ||
                    player.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier() < 0) {
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.JUMP_BOOST, 40, 0, true, false, false));
            }
        }
        if ("slime_t2_leggings".equals(legsId)) {
            if (!player.hasStatusEffect(StatusEffects.JUMP_BOOST) ||
                    player.getStatusEffect(StatusEffects.JUMP_BOOST) == null ||
                    player.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier() < 1) {
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.JUMP_BOOST, 40, 1, true, false, false));
            }
        }

        // Boots: T1 = No Fall Damage | T2 = Jump Boost II (no fall damage handled in LivingEntityMixin)
        String bootsId = SlayerItems.getCustomItemId(player.getEquippedStack(EquipmentSlot.FEET));
        if ("slime_t1_boots".equals(bootsId)) {
            // T1 boots should have no fall damage - this would need a mixin to properly implement
            // For now, we'll give slow falling as a approximation
            if (player.fallDistance > 2.0f) {
                if (!player.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
                    player.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.SLOW_FALLING, 40, 0, true, false, false));
                }
            }
        }
        if ("slime_t2_boots".equals(bootsId)) {
            if (!player.hasStatusEffect(StatusEffects.JUMP_BOOST) ||
                    player.getStatusEffect(StatusEffects.JUMP_BOOST) == null ||
                    player.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier() < 1) {
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.JUMP_BOOST, 40, 1, true, false, false));
            }
        }
    }

    // =========================================================
    // ENDERMAN ARMOR — T1 individual effects
    // (T2 set effects are handled by T2ArmorAbilityHandler)
    // =========================================================

    private static void tickEndermanArmorEffects(ServerPlayerEntity player) {
        // T1 and T2 Helmet: Night Vision
        String helmetId = SlayerItems.getCustomItemId(player.getEquippedStack(EquipmentSlot.HEAD));
        if ("enderman_t1_helmet".equals(helmetId) || "enderman_t2_helmet".equals(helmetId)) {
            if (!player.hasStatusEffect(StatusEffects.NIGHT_VISION) ||
                    player.getStatusEffect(StatusEffects.NIGHT_VISION) == null ||
                    player.getStatusEffect(StatusEffects.NIGHT_VISION).getDuration() < 30) {
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.NIGHT_VISION, 400, 0, true, false, false));
            }
        }
    }

    // =========================================================
    // WARDEN ARMOR (Sculk Terror)
    // =========================================================

    private static void tickWardenArmorEffects(ServerPlayerEntity player) {
        // T1 and T2 Helmet: Darkness immunity (remove Darkness effect), suppressed during Ender Phase Mode
        String helmetId = SlayerItems.getCustomItemId(player.getEquippedStack(EquipmentSlot.HEAD));
        if ("warden_t1_helmet".equals(helmetId) || "warden_t2_helmet".equals(helmetId)) {
            if (player.hasStatusEffect(StatusEffects.DARKNESS) && !CustomItemHandler.isInEnderPhaseMode(player.getUuid())) {
                player.removeStatusEffect(StatusEffects.DARKNESS);
            }
        }

        // T2 Chestplate: Resistance I
        String chestId = SlayerItems.getCustomItemId(player.getEquippedStack(EquipmentSlot.CHEST));
        if ("warden_t2_chestplate".equals(chestId)) {
            if (!player.hasStatusEffect(StatusEffects.RESISTANCE) ||
                    player.getStatusEffect(StatusEffects.RESISTANCE) == null ||
                    player.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() < 0) {
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.RESISTANCE, 40, 0, true, false, false));
            }
        }

        // T1 Boots: Vibration sense (8 blocks), T2 Boots: wider vibration sense (12 blocks)
        String bootsId = SlayerItems.getCustomItemId(player.getEquippedStack(EquipmentSlot.FEET));
        if ("warden_t1_boots".equals(bootsId) || "warden_t2_boots".equals(bootsId)) {
            int vibrationRange = "warden_t2_boots".equals(bootsId) ? 12 : 8;
            ServerWorld world = player.getEntityWorld();
            if (world.getTime() % 20 == 0) {
                Box searchBox = player.getBoundingBox().expand(vibrationRange);
                for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, searchBox,
                        e -> e instanceof HostileEntity)) {
                    entity.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.GLOWING, 60, 0, true, false, false)); // 3s, expires naturally
                }
            }
        }
    }

    // =========================================================
    // PIGLIN ARMOR (Gilded Ravager)
    // =========================================================

    private static void tickPiglinArmorEffects(ServerPlayerEntity player) {
        // T1 Helmet & T2 Helmet: Fire Resistance
        String helmetId = SlayerItems.getCustomItemId(player.getEquippedStack(EquipmentSlot.HEAD));
        if ("piglin_t1_helmet".equals(helmetId) || "piglin_t2_helmet".equals(helmetId)) {
            if (!player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) {
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.FIRE_RESISTANCE, 40, 0, true, false, false));
            }
        }

        // T1 Boots & T2 Boots: Fire Resistance
        String bootsId = SlayerItems.getCustomItemId(player.getEquippedStack(EquipmentSlot.FEET));
        if ("piglin_t1_boots".equals(bootsId) || "piglin_t2_boots".equals(bootsId)) {
            if (!player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) {
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.FIRE_RESISTANCE, 40, 0, true, false, false));
            }
        }
    }

    // =========================================================
    // LEGENDARY: Venomous Crawler Leggings
    // =========================================================

    private static void tickSpiderLeggingsEffects(ServerPlayerEntity player) {
        ItemStack leggings = player.getEquippedStack(EquipmentSlot.LEGS);
        if (!SlayerItems.isSpiderLeggings(leggings)) return;
        if (!SlayerItems.canUseSpiderLeggings(player)) return;

        // Poison immunity: continuously remove poison
        if (player.hasStatusEffect(StatusEffects.POISON)) {
            player.removeStatusEffect(StatusEffects.POISON);
        }

        // Speed boost: keep Speed II active while wearing spider leggings
        if (!player.hasStatusEffect(StatusEffects.SPEED) ||
                player.getStatusEffect(StatusEffects.SPEED) == null ||
                player.getStatusEffect(StatusEffects.SPEED).getAmplifier() < 1) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, 1, false, false, false));
        }

        // Web immunity: if standing in a cobweb, restore velocity to walk through freely
        ServerWorld world = player.getEntityWorld();
        BlockPos pos = player.getBlockPos();
        if (world.getBlockState(pos).isOf(Blocks.COBWEB)) {
            Vec3d vel = player.getVelocity();
            double minSpeed = SPIDER_LEGGINGS_MIN_WALK_SPEED;
            double vx = Math.abs(vel.x) < minSpeed && vel.x != 0 ? Math.signum(vel.x) * minSpeed : vel.x;
            double vz = Math.abs(vel.z) < minSpeed && vel.z != 0 ? Math.signum(vel.z) * minSpeed : vel.z;
            double vy = vel.y < SPIDER_LEGGINGS_MAX_FALL_SPEED ? SPIDER_LEGGINGS_MAX_FALL_SPEED : vel.y;
            player.setVelocity(vx, vy, vz);
            player.velocityDirty = true;
        }
    }

    // =========================================================
    // SKELETON ARMOR (Bone Marksman) - T1 Effects
    // =========================================================

    private static void tickSkeletonArmorEffects(ServerPlayerEntity player) {
        // Leggings: T1 = +5% Movement Speed
        String legsId = SlayerItems.getCustomItemId(player.getEquippedStack(EquipmentSlot.LEGS));
        if ("skeleton_t1_leggings".equals(legsId)) {
            if (!player.hasStatusEffect(StatusEffects.SPEED) ||
                    player.getStatusEffect(StatusEffects.SPEED) == null ||
                    player.getStatusEffect(StatusEffects.SPEED).getAmplifier() < 0) {
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.SPEED, 40, 0, true, false, false));
            }
        }

        // Boots: T1 = +5% Movement Speed
        String bootsId = SlayerItems.getCustomItemId(player.getEquippedStack(EquipmentSlot.FEET));
        if ("skeleton_t1_boots".equals(bootsId)) {
            if (!player.hasStatusEffect(StatusEffects.SPEED) ||
                    player.getStatusEffect(StatusEffects.SPEED) == null ||
                    player.getStatusEffect(StatusEffects.SPEED).getAmplifier() < 0) {
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.SPEED, 40, 0, true, false, false));
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
    public static float getUndeadDamageReduction(ServerPlayerEntity player) {
        if (SlayerItems.isCrownOfGreed(player.getEquippedStack(EquipmentSlot.HEAD))) return 1.0f;
        String legsId = SlayerItems.getCustomItemId(player.getEquippedStack(EquipmentSlot.LEGS));
        if ("zombie_t2_leggings".equals(legsId)) return 0.80f;
        if ("zombie_t1_leggings".equals(legsId)) return 0.90f;
        return 1.0f;
    }

    /**
     * Returns true if the player's Spider T2 Chestplate should poison melee attackers.
     */
    public static boolean hasPoisonThorns(ServerPlayerEntity player) {
        if (SlayerItems.isCrownOfGreed(player.getEquippedStack(EquipmentSlot.HEAD))) return false;
        String chestId = SlayerItems.getCustomItemId(player.getEquippedStack(EquipmentSlot.CHEST));
        return "spider_t2_chestplate".equals(chestId);
    }

    /**
     * Returns the projectile damage output multiplier for the player based on skeleton helmet.
     * Skeleton T1 Helmet: 1.10 (+10%) | T2 Helmet: 1.20 (+20%)
     */
    public static float getProjectileDamageBoost(ServerPlayerEntity player) {
        if (SlayerItems.isCrownOfGreed(player.getEquippedStack(EquipmentSlot.HEAD))) return 1.0f;
        String helmetId = SlayerItems.getCustomItemId(player.getEquippedStack(EquipmentSlot.HEAD));
        if ("skeleton_t2_helmet".equals(helmetId)) return 1.20f;
        if ("skeleton_t1_helmet".equals(helmetId)) return 1.10f;
        return 1.0f;
    }

    /**
     * Returns true if the player has Skeleton T2 Helmet equipped (arrows apply Glowing to targets).
     */
    public static boolean hasGlowingArrows(ServerPlayerEntity player) {
        if (SlayerItems.isCrownOfGreed(player.getEquippedStack(EquipmentSlot.HEAD))) return false;
        String helmetId = SlayerItems.getCustomItemId(player.getEquippedStack(EquipmentSlot.HEAD));
        return "skeleton_t2_helmet".equals(helmetId);
    }

    /**
     * Returns the projectile damage INCOMING multiplier for the player based on skeleton T1 chestplate.
     * Skeleton T1 Chestplate: 0.90 (-10%).
     * T2 chestplate resistance is handled by T2ArmorAbilityHandler.getProjectileDamageReduction().
     *
     * <p>Crown of Greed suppresses all bounty-gear bonuses, including this resistance.
     */
    public static float getProjectileDamageResistance(ServerPlayerEntity player) {
        if (SlayerItems.isCrownOfGreed(player.getEquippedStack(EquipmentSlot.HEAD))) return 1.0f;
        String chestId = SlayerItems.getCustomItemId(player.getEquippedStack(EquipmentSlot.CHEST));
        if ("skeleton_t1_chestplate".equals(chestId)) return 0.90f;
        return 1.0f;
    }
}
