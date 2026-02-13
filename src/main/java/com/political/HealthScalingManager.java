package com.political;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import java.util.Iterator;
import net.minecraft.util.Formatting;
import java.util.Random;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class HealthScalingManager {
    private static final java.util.Random javaRandom = new java.util.Random();
    private static final double SPAWN_CHANCE = 0.3; // Add this at top of class
    private static final Random random = new Random();
    // ============================================================
    // CONFIGURATION
    // ============================================================
    public static void tickNameTagVisibility(MinecraftServer server) {
        if (scaledEntities.isEmpty()) return;

        for (ServerWorld world : server.getWorlds()) {
            // Use getEntity(UUID) for O(1) lookup instead of iterating all entities
            Iterator<UUID> it = scaledEntities.iterator();
            while (it.hasNext()) {
                UUID uuid = it.next();
                Entity entity = world.getEntity(uuid);

                if (entity instanceof MobEntity mob) {
                    updateNameTagVisibility(mob, world);
                }
            }
        }
    }
    private static final Map<UUID, Long> upgradedMobSpawnTimes = new HashMap<>();

    public static void trackUpgradedMob(UUID uuid, long worldTime) {
        upgradedMobSpawnTimes.put(uuid, worldTime);
    }

    public static void tickUpgradedMobDespawn(ServerWorld world) {
        if (upgradedMobSpawnTimes.isEmpty()) return;

        long currentTime = world.getTime();
        Iterator<Map.Entry<UUID, Long>> it = upgradedMobSpawnTimes.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<UUID, Long> entry = it.next();
            if (currentTime - entry.getValue() > 6000) {
                Entity entity = world.getEntity(entry.getKey());
                if (entity instanceof LivingEntity living) {
                    living.discard();
                }
                it.remove();
            }
        }
    }
    private static final Map<UUID, Integer> extraDropsCount = new HashMap<>();

    private static void updateNameTagVisibility(MobEntity mob, ServerWorld world) {
        boolean anyPlayerCanSee = false;

        for (var player : world.getPlayers()) {
            if (canPlayerSeeMob(player, mob)) {
                anyPlayerCanSee = true;
                break;
            }
        }

        mob.setCustomNameVisible(anyPlayerCanSee);
    }

    private static boolean canPlayerSeeMob(net.minecraft.server.network.ServerPlayerEntity player, MobEntity mob) {
        // Distance check (50 blocks max)
        if (player.squaredDistanceTo(mob) > 2500) {
            return false;
        }

        // Raycast from player eyes to mob center
        Vec3d playerEyes = player.getEyePos();
        Vec3d mobCenter = new Vec3d(mob.getX(), mob.getY() + mob.getHeight() / 2, mob.getZ());

        RaycastContext context = new RaycastContext(
                playerEyes,
                mobCenter,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                player
        );

        BlockHitResult result = player.getEntityWorld().raycast(context);

        // If raycast hit nothing, player can see it
        if (result.getType() == HitResult.Type.MISS) {
            return true;
        }

        // Check if hit position is at or beyond mob
        double hitDist = result.getPos().squaredDistanceTo(playerEyes);
        double mobDist = mobCenter.squaredDistanceTo(playerEyes);

        return hitDist >= mobDist * 0.9; // 10% tolerance
    }
    private static final double SCALING_CHANCE = 0.10; // 10% of mobs

    public enum ScalingTier {
        ENHANCED(1.5, 1.5, "Enhanced", Formatting.GREEN, 70),
        REINFORCED(2.0, 2.0, "Reinforced", Formatting.YELLOW, 17),
        ELITE(2.5, 2.5, "Elite", Formatting.GOLD, 10),
        CHAMPION(5.0, 4.0, "Champion", Formatting.RED, 2),
        LEGENDARY(7.5, 5.0, "LEGENDARY", Formatting.LIGHT_PURPLE, 1);

        public final double healthMultiplier;
        public final double damageMultiplier;
        public final String displayName;
        public final Formatting color;
        public final int weight;

        ScalingTier(double healthMult, double damageMult, String displayName, Formatting color, int weight) {
            this.healthMultiplier = healthMult;
            this.damageMultiplier = damageMult;
            this.displayName = displayName;
            this.color = color;
            this.weight = weight;
        }

        public static ScalingTier rollTier(Random random) {
            int totalWeight = 0;
            for (ScalingTier tier : values()) {
                totalWeight += tier.weight;
            }

            int roll = random.nextInt(totalWeight);
            int cumulative = 0;

            for (ScalingTier tier : values()) {
                cumulative += tier.weight;
                if (roll < cumulative) {
                    return tier;
                }
            }
            return ENHANCED;
        }
    }

    // Track scaled entities
    private static final Set<UUID> scaledEntities = new HashSet<>();
    private static final Set<UUID> checkedEntities = new HashSet<>();

    // Scalable mob types
    private static final Set<EntityType<?>> SCALABLE_MOBS = Set.of(
            // Undead
            EntityType.ZOMBIE,
            EntityType.SKELETON,
            EntityType.ZOMBIE_VILLAGER,
            EntityType.HUSK,
            EntityType.DROWNED,
            EntityType.STRAY,
            EntityType.WITHER_SKELETON,
            EntityType.PHANTOM,

            // Arthropods
            EntityType.SPIDER,
            EntityType.CAVE_SPIDER,
            EntityType.SILVERFISH,
            EntityType.ENDERMITE,

            // Nether
            EntityType.BLAZE,
            EntityType.GHAST,
            EntityType.MAGMA_CUBE,
            EntityType.PIGLIN,
            EntityType.PIGLIN_BRUTE,
            EntityType.HOGLIN,
            EntityType.ZOGLIN,
            EntityType.WITHER,

            // Overworld hostile
            EntityType.CREEPER,
            EntityType.SLIME,
            EntityType.WITCH,
            EntityType.VINDICATOR,
            EntityType.PILLAGER,
            EntityType.RAVAGER,
            EntityType.EVOKER,
            EntityType.VEX,

            // End
            EntityType.ENDERMAN,
            EntityType.SHULKER,
            EntityType.ENDER_DRAGON,

            // Ocean
            EntityType.GUARDIAN,
            EntityType.ELDER_GUARDIAN,

            // Deep Dark
            EntityType.WARDEN,

            // Passive
            EntityType.PIG,
            EntityType.COW,
            EntityType.SHEEP,
            EntityType.CHICKEN,
            EntityType.IRON_GOLEM,
            EntityType.ALLAY,
            EntityType.ARMADILLO,
            EntityType.AXOLOTL,
            EntityType.BAT,
            EntityType.CAMEL,
            EntityType.CAT,
            EntityType.COD,
            EntityType.DONKEY,
            EntityType.FOX,
            EntityType.FROG,
            EntityType.GLOW_SQUID,
            EntityType.HORSE,
            EntityType.MOOSHROOM,
            EntityType.MULE,
            EntityType.NAUTILUS,
            EntityType.OCELOT,
            EntityType.ZOMBIE_NAUTILUS,
            EntityType.PARROT,
            EntityType.PUFFERFISH,
            EntityType.RABBIT,
            EntityType.SALMON,
            EntityType.SKELETON_HORSE,
            EntityType.SNIFFER,
            EntityType.SNOW_GOLEM,
            EntityType.SQUID,
            EntityType.STRIDER,
            EntityType.TADPOLE,
            EntityType.TROPICAL_FISH,
            EntityType.TURTLE,
            EntityType.VILLAGER,
            EntityType.WANDERING_TRADER
    );

    // ============================================================
    // MAIN SCALING METHOD
    // ============================================================

    public static void tryScaleMob(LivingEntity entity) {
        if (SlayerManager.isSlayerBoss(entity.getUuid())) {
            return;
        }

        // Also check custom name for boss indicators
        if (entity.hasCustomName()) {
            String name = entity.getCustomName().getString();
            if (name.contains("Outlaw") || name.contains("Bandit") ||
                    name.contains("Desperado") || name.contains("Rustler") ||
                    name.contains("Phantom") || name.contains("Terror") ||
                    name.contains("[TEST]")) {
                return; // Don't scale boss entities
            }
        }
        if (entity == null) return;
        if (entity.getEntityWorld().isClient()) return;
        if (!(entity instanceof MobEntity mob)) return;

        UUID uuid = entity.getUuid();

        // Already processed - CHECK FIRST!
        if (scaledEntities.contains(uuid) || checkedEntities.contains(uuid)) return;

        // Not a scalable type
        if (!SCALABLE_MOBS.contains(entity.getType())) {
            checkedEntities.add(uuid);
            return;
        }

        // Don't scale slayer bosses
        if (SlayerManager.isSlayerBoss(uuid)) {
            checkedEntities.add(uuid);
            return;
        }

        // Don't scale damaged mobs (prevents bugs)
        if (entity.getHealth() < entity.getMaxHealth()) {
            checkedEntities.add(uuid);
            return;
        }

        // Roll for scaling - only 30% chance
        if (entity.getRandom().nextDouble() > SPAWN_CHANCE) {
            checkedEntities.add(uuid);
            return;
        }

        // NOW apply scaling after all checks pass
        ScalingTier tier = ScalingTier.rollTier(javaRandom);
        applyScaling(mob, tier);

        int tierValue = tier.ordinal();
        if (tierValue > 0) {
            setMobTier(mob, tierValue);
        }
    }



    public static void spawnUpgradedMob(ServerPlayerEntity player, EntityType<?> entityType) {
        ServerWorld world = player.getEntityWorld();

        Entity entity = entityType.spawn(world, player.getBlockPos(), SpawnReason.TRIGGERED);

        if (entity instanceof MobEntity mob) {
            // Use MobEntity instead of LivingEntity
            ScalingTier tier = ScalingTier.ELITE;
            applyScaling(mob, tier);
            setMobTier(mob, tier.ordinal());
            trackUpgradedMob(entity.getUuid(), world.getTime());
        }
    }


    private static void applyScaling(MobEntity mob, ScalingTier tier) {
        try {
            // Scale health
            var healthAttr = mob.getAttributeInstance(EntityAttributes.MAX_HEALTH);
            if (healthAttr != null) {
                double baseHealth = healthAttr.getBaseValue();
                double newHealth = baseHealth * tier.healthMultiplier;
                healthAttr.setBaseValue(newHealth);
                mob.setHealth((float) newHealth);
            }

            // Scale damage
            var damageAttr = mob.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE);
            if (damageAttr != null) {
                double baseDamage = damageAttr.getBaseValue();
                damageAttr.setBaseValue(baseDamage * tier.damageMultiplier);
            }

            // Set name with tier
            String mobName = mob.getType().getName().getString();
            String stars = getStars(tier);
            mob.setCustomName(Text.literal(stars + " " + tier.displayName + " " + mobName)
                    .formatted(tier.color));
            mob.setCustomNameVisible(false);
            mob.setCustomNameVisible(false);
            scaledEntities.add(mob.getUuid());  // ADD THIS LINE


        } catch (Exception e) {
            // Silently fail if attributes don't exist
            checkedEntities.add(mob.getUuid());
        }
    }

    private static String getStars(ScalingTier tier) {
        return switch (tier) {
            case ENHANCED -> "✦";
            case REINFORCED -> "✦✦";
            case ELITE -> "✦✦✦";
            case CHAMPION -> "✦✦✦✦";
            case LEGENDARY -> "✦✦✦✦✦";
        };
    }
    public static void markForDespawn(LivingEntity mob) {
        // Store spawn time in scoreboard or custom tag
        mob.age = 0; // Reset age to track time alive
    }
    // ============================================================
    // REWARDS
    // ============================================================

    public static void onScaledMobKill(LivingEntity entity, ServerPlayerEntity killer) {
        if (!scaledEntities.contains(entity.getUuid())) return;

        int bonus = getKillBonus(entity);
        if (bonus > 0) {
            CoinManager.giveCoinsQuiet(killer, bonus);
            killer.sendMessage(Text.literal("")
                    .formatted(Formatting.GOLD), true);
        }

        cleanup(entity.getUuid());
    }

    public static int getKillBonus(LivingEntity entity) {
        if (!scaledEntities.contains(entity.getUuid())) return 0;

        Text name = entity.getCustomName();
        if (name == null) return 0;
        String nameStr = name.getString();

        if (nameStr.contains("LEGENDARY")) return 8;
        if (nameStr.contains("Champion")) return 6;
        if (nameStr.contains("Elite")) return 4;
        if (nameStr.contains("Reinforced")) return 2;
        if (nameStr.contains("Enhanced")) return 1;
        return 0;
    }

    // ============================================================
    // UTILITY
    // ============================================================

    public static boolean isScaledMob(UUID uuid) {
        return scaledEntities.contains(uuid);
    }

    public static void cleanup(UUID uuid) {
        scaledEntities.remove(uuid);
        checkedEntities.remove(uuid);
    }

    public static void clearAll() {
        scaledEntities.clear();
        checkedEntities.clear();
    }
    private static final Map<UUID, Integer> mobTiers = new HashMap<>();

    public static void setMobTier(LivingEntity mob, int tier) {
        mobTiers.put(mob.getUuid(), tier);
    }

    public static int getMobTier(LivingEntity mob) {
        return mobTiers.getOrDefault(mob.getUuid(), 0);
    }

    public static void onMobDeath(LivingEntity entity) {
        if (entity == null) return;
        UUID uuid = entity.getUuid();
        scaledEntities.remove(uuid);
        checkedEntities.remove(uuid);  // ADD THIS LINE
        extraDropsCount.remove(uuid);  // ADD THIS LINE
        mobTiers.remove(uuid);         // ADD THIS LINE if you have mobTiers map
        upgradedMobSpawnTimes.remove(uuid); // ADD THIS LINE
    }
    private static int cleanupTickCounter = 0;

    public static void tickCleanup() {
        cleanupTickCounter++;
        if (cleanupTickCounter >= 6000) {
            cleanupTickCounter = 0;
            checkedEntities.clear();
        }
    }

    public static void setExtraDrops(UUID uuid, int count) {
        extraDropsCount.put(uuid, count);
    }

    public static int getExtraDrops(UUID uuid) {
        return extraDropsCount.getOrDefault(uuid, 0);
    }

    public static void clearExtraDrops(UUID uuid) {
        extraDropsCount.remove(uuid);
    }

}