package com.political;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.random.Random;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class HealthScalingManager {

    // ============================================================
    // CONFIGURATION
    // ============================================================

    // Chance for a mob to spawn with scaling (0.0 - 1.0)
    private static final double SCALING_CHANCE = 0.1; // 10% of mobs

    // Scaling tiers with weights
    public enum ScalingTier {
        ENHANCED(1.5, 1.5, "§a✦ Enhanced", Formatting.GREEN, 70),      // 2x HP, 1.5x DMG
        REINFORCED(2.0, 2.0, "§e✦✦ Reinforced", Formatting.YELLOW, 17), // 3x HP, 2x DMG
        ELITE(2.5, 2.5, "§6✦✦✦ Elite", Formatting.GOLD, 10),           // 5x HP, 3x DMG
        CHAMPION(5.0, 4.0, "§c✦✦✦✦ Champion", Formatting.RED, 2),      // 7x HP, 4x DMG
        LEGENDARY(7.5, 5.0, "§d✦✦✦✦✦ LEGENDARY", Formatting.LIGHT_PURPLE, 1); // 10x HP, 5x DMG


        public final double healthMultiplier;
        public final double damageMultiplier;
        public final String prefix;
        public final Formatting color;
        public final int weight; // Higher = more common

        ScalingTier(double healthMult, double damageMult, String prefix, Formatting color, int weight) {
            this.healthMultiplier = healthMult;
            this.damageMultiplier = damageMult;
            this.prefix = prefix;
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
            return ENHANCED; // Fallback
        }
    }

    // Track which entities have been scaled (prevent double-scaling)
    private static final Set<UUID> scaledEntities = new HashSet<>();

    // Mobs that can be scaled
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
    // SCALING LOGIC
    // ============================================================

    /**
     * Attempt to scale a newly spawned mob.
     * Call this on mob spawn events.
     */
    public static void tryScaleMob(LivingEntity entity) {
        if (entity.getEntityWorld().isClient()) return;
        if (!(entity instanceof MobEntity mob)) return;
        if (!SCALABLE_MOBS.contains(entity.getType())) return;

        // IMPORTANT: Check if already scaled FIRST
        if (scaledEntities.contains(entity.getUuid())) return;

        // Don't scale if mob is already damaged (prevents respawn scaling bug)
        if (entity.getHealth() < entity.getMaxHealth()) return;

        // Don't scale slayer bosses
        if (SlayerManager.isSlayerBoss(entity.getUuid())) return;

        Random random = entity.getRandom();
        if (random.nextDouble() > SCALING_CHANCE) {
            // Mark as checked so we don't roll again
            scaledEntities.add(entity.getUuid());
            return;
        }

        ScalingTier tier = ScalingTier.rollTier(random);
        applyScaling(mob, tier);
    }

    private static void applyScaling(MobEntity mob, ScalingTier tier) {
        // Scale max health
        var healthAttr = mob.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (healthAttr != null) {
            double baseHealth = healthAttr.getBaseValue();
            double newHealth = baseHealth * tier.healthMultiplier;
            healthAttr.setBaseValue(newHealth);
            mob.setHealth((float) newHealth);
        }

        // Scale attack damage
        var damageAttr = mob.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE);
        if (damageAttr != null) {
            double baseDamage = damageAttr.getBaseValue();
            damageAttr.setBaseValue(baseDamage * tier.damageMultiplier);
        }

        // Set custom name with tier prefix
        String mobName = getMobDisplayName(mob);
        mob.setCustomName(Text.literal(tier.prefix + " " + mobName)
                .formatted(tier.color));
        mob.setCustomNameVisible(true);

        // Mark as scaled
        scaledEntities.add(mob.getUuid());

        // Make it persistent so it doesn't despawn
        mob.setPersistent();
    }

    private static String getMobDisplayName(MobEntity mob) {
        String typeName = mob.getType().getName().getString();
        return typeName;
    }

    // ============================================================
    // REWARDS - Scaled mobs drop bonus coins
    // ============================================================

    /**
     * Calculate bonus coin reward for killing a scaled mob.
     */
    public static int getBonusCoinReward(LivingEntity entity) {
        if (!scaledEntities.contains(entity.getUuid())) return 0;

        Text customName = entity.getCustomName();
        if (customName == null) return 0;

        String name = customName.getString();

        // Determine tier from name prefix
        if (name.contains("LEGENDARY")) return 70;
        if (name.contains("Champion")) return 17;
        if (name.contains("Elite")) return 10;
        if (name.contains("Reinforced")) return 2;
        if (name.contains("Enhanced")) return 1;

        return 0;
    }

    /**
     * Award bonus coins on kill. Call from death event.
     */
    public static void onScaledMobKill(LivingEntity entity, net.minecraft.server.network.ServerPlayerEntity killer) {
        int bonus = getBonusCoinReward(entity);
        if (bonus > 0) {
            CoinManager.giveCoinsQuiet(killer, bonus);
            killer.sendMessage(Text.literal("+" + bonus + " coins (scaled mob bonus)")
                    .formatted(Formatting.GOLD), true);
        }

        // Cleanup tracking
        scaledEntities.remove(entity.getUuid());
    }

    // ============================================================
    // UTILITY
    // ============================================================

    public static boolean isScaledMob(UUID entityUuid) {
        return scaledEntities.contains(entityUuid);
    }

    public static void cleanup(UUID entityUuid) {
        scaledEntities.remove(entityUuid);
    }

    /**
     * Clear all tracked entities (call on server stop)
     */
    public static void clearAll() {
        scaledEntities.clear();
    }

    public static int getKillBonus(LivingEntity entity) {
        if (!scaledEntities.contains(entity.getUuid())) return 0;

        Text customName = entity.getCustomName();
        if (customName == null) return 0;

        String name = customName.getString();

        // Bonus kills based on tier
        if (name.contains("LEGENDARY")) return 8;
        if (name.contains("Champion")) return 6;
        if (name.contains("Elite")) return 4;
        if (name.contains("Reinforced")) return 2;
        if (name.contains("Enhanced")) return 1;

        return 0;
    }
}