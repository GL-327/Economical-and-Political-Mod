package com.political.stats;
import com.political.MasteryHeartTree;
import com.political.PoliticalServer;
import com.political.WorldBossManager;



import com.political.bounty.SlayerManager;
import com.political.economy.CoinManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.Mob;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import java.util.Iterator;
import net.minecraft.ChatFormatting;
import java.util.Random;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;

public class HealthScalingManager {
    private static final java.util.Random javaRandom = new java.util.Random();
    private static final double SPAWN_CHANCE = 0.3; // Add this at top of class
        // ============================================================
    // CONFIGURATION
    // ============================================================
    public static void tickNameTagVisibility(MinecraftServer server) {
        if (scaledEntities.isEmpty() && wallOcclusionNameTagMobs.isEmpty()) return;

        for (ServerLevel world : server.getAllLevels()) {
            updateSetForWorld(world, scaledEntities);
            updateSetForWorld(world, wallOcclusionNameTagMobs);
        }
    }

    private static void updateSetForWorld(ServerLevel world, Set<UUID> uuids) {
        Iterator<UUID> it = uuids.iterator();
        while (it.hasNext()) {
            UUID uuid = it.next();
            Entity entity = world.getEntity(uuid);
            if (entity instanceof Mob mob) {
                updateNameTagVisibility(mob, world);
            }
        }
    }
    private static final Map<UUID, Long> upgradedMobSpawnTimes = new HashMap<>();

    public static void trackUpgradedMob(UUID uuid, long worldTime) {
        upgradedMobSpawnTimes.put(uuid, worldTime);
    }

    public static void tickUpgradedMobDespawn(ServerLevel world) {
        if (upgradedMobSpawnTimes.isEmpty()) return;

        long currentTime = world.getServer().getTickCount();
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

    private static void updateNameTagVisibility(Mob mob, ServerLevel world) {
        boolean anyPlayerCanSee = false;
        for (ServerPlayer player : world.players()) {
            if (canPlayerSeeMob(player, mob)) {
                anyPlayerCanSee = true;
                break;
            }
        }
        mob.setCustomNameVisible(anyPlayerCanSee);
    }

    private static boolean canPlayerSeeMob(net.minecraft.server.level.ServerPlayer player, Mob mob) {
        long maxDistSq = maxNameTagDistanceSq(mob);
        if (player.distanceToSqr(mob) > maxDistSq) {
            return false;
        }

        // Raycast from player eyes to mob center
        Vec3 playerEyes = player.getEyePosition();
        Vec3 mobCenter = new Vec3(mob.getX(), mob.getY() + mob.getBbHeight() / 2, mob.getZ());

        ClipContext context = new ClipContext(
                playerEyes,
                mobCenter,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        );

        BlockHitResult result = player.level().clip(context);

        // If raycast hit nothing, player can see it
        if (result.getType() == HitResult.Type.MISS) {
            return true;
        }

        // Check if hit position is at or beyond mob
        double hitDist = result.getLocation().distanceToSqr(playerEyes);
        double mobDist = mobCenter.distanceToSqr(playerEyes);

        return hitDist >= mobDist * 0.9; // 10% tolerance
    }

    /** Enhanced through Legendary: long range but still blocked by walls. Higher tiers: shorter range. */
    private static long maxNameTagDistanceSq(Mob mob) {
        if (mob.entityTags().contains("magic_creature")) {
            return 384L * 384L;
        }
        int tier = getMobTier(mob);
        if (tier >= 0 && tier <= ScalingTier.LEGENDARY.ordinal()) {
            return 384L * 384L;
        }
        return 96L * 96L;
    }
    
    public enum ScalingTier {
        ENHANCED   (1.5,  2.5,  "Enhanced",   ChatFormatting.GREEN,        7000),
        REINFORCED (2.0,  3.5,  "Reinforced", ChatFormatting.YELLOW,       1700),
        ELITE      (2.5,  5.0,  "Elite",      ChatFormatting.GOLD,         1000),
        CHAMPION   (5.0,  8.0,  "Champion",   ChatFormatting.RED,           200),
        LEGENDARY  (7.5,  12.0, "LEGENDARY",  ChatFormatting.LIGHT_PURPLE,  100),
        APEX       (10.0, 16.0, "APEX",       ChatFormatting.DARK_RED,       40),
        MYTHIC     (15.0, 22.0, "MYTHIC",     ChatFormatting.DARK_PURPLE,    15),
        ASCENDED   (22.0, 30.0, "ASCENDED",   ChatFormatting.DARK_AQUA,       5),
        DIVINE     (35.0, 42.0, "DIVINE",     ChatFormatting.AQUA,            2),
        ETERNAL    (50.0, 60.0, "ETERNAL",    ChatFormatting.DARK_BLUE,       1);

        public final double healthMultiplier;
        public final double damageMultiplier;
        public final String displayName;
        public final ChatFormatting color;
        public final int weight;

        ScalingTier(double healthMult, double damageMult, String displayName, ChatFormatting color, int weight) {
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

    /** Command tag marking a mob as already scaled; survives server restarts. */
    private static final String SCALED_TAG = "political_scaled";
    /** Command tag prefix storing the rolled tier ordinal (e.g. political_tier_3). */
    private static final String TIER_TAG_PREFIX = "political_tier_";

    /** Re-register a scaled mob loaded from disk (health/name already persisted on the entity). */
    private static void restoreScaledMob(Mob mob) {
        UUID uuid = mob.getUUID();
        scaledEntities.add(uuid);
        for (String tag : mob.entityTags()) {
            if (tag.startsWith(TIER_TAG_PREFIX)) {
                try {
                    mobTiers.put(uuid, Integer.parseInt(tag.substring(TIER_TAG_PREFIX.length())));
                } catch (NumberFormatException ignored) {}
                break;
            }
        }
        mob.setCustomNameVisible(false);
    }

    // Track scaled entities
    private static final Set<UUID> scaledEntities = new HashSet<>();
    private static final Set<UUID> checkedEntities = new HashSet<>();
    /** Mobs whose name tags follow line-of-sight rules (scaled tiers, magic creatures, etc.). */
    private static final Set<UUID> wallOcclusionNameTagMobs = new HashSet<>();

    public static void registerWallOcclusionNameTag(Mob mob) {
        if (mob == null) return;
        wallOcclusionNameTagMobs.add(mob.getUUID());
        mob.setCustomNameVisible(false);
    }

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
            EntityType.BREEZE,
            EntityType.BOGGED,

            // End
            EntityType.ENDERMAN,
            EntityType.SHULKER,
            EntityType.ENDER_DRAGON,

            // Ocean
            EntityType.GUARDIAN,
            EntityType.ELDER_GUARDIAN,

            // Deep Dark
            EntityType.WARDEN
    );

    // ============================================================
    // MAIN SCALING METHOD
    // ============================================================

    private static boolean isCustomBossOrNpc(LivingEntity entity) {
        if (SlayerManager.isSlayerBoss(entity.getUUID())) return true;
        if (WorldBossManager.isWorldBoss(entity.getUUID())) return true;
        if (!entity.hasCustomName()) return false;
        String name = entity.getCustomName().getString();
        return name.contains("Outlaw") || name.contains("Bandit") ||
               name.contains("Desperado") || name.contains("Rustler") ||
               name.contains("Phantom") || name.contains("Terror") ||
               name.contains("[TEST]") || name.contains("Auction") ||
               name.contains("Master") || name.contains("Underground") ||
               name.contains("NPC") || name.contains("Sentinel") ||
               name.contains("Voidreaver") || name.contains("Skulltide") ||
               name.contains("King of") || name.contains("World Boss") ||
               name.contains("✦ ") || name.contains("Minion") ||
               name.contains("Boss") || name.contains("Bounty");
    }

    public static void tryScaleMob(LivingEntity entity) {
        if (entity == null) return;
        if (entity.level().isClientSide()) return;
        if (!(entity instanceof Mob mob)) return;

        if (isCustomBossOrNpc(entity)) return;

        UUID uuid = entity.getUUID();

        if (scaledEntities.contains(uuid) || checkedEntities.contains(uuid)) return;

        // Already scaled in a previous session (tags persist with the entity): restore
        // tracking state instead of rolling again, otherwise restarts stack multipliers.
        if (mob.entityTags().contains(SCALED_TAG)) {
            restoreScaledMob(mob);
            return;
        }

        if (!SCALABLE_MOBS.contains(entity.getType())) {
            checkedEntities.add(uuid);
            return;
        }

        if (entity.getHealth() < entity.getMaxHealth()) {
            checkedEntities.add(uuid);
            return;
        }

        if (entity.getRandom().nextDouble() > SPAWN_CHANCE) {
            checkedEntities.add(uuid);
            return;
        }

        ScalingTier tier = ScalingTier.rollTier(javaRandom);
        applyScaling(mob, tier);
        setMobTier(mob, tier.ordinal());
    }



    public static void spawnUpgradedMob(ServerPlayer player, EntityType<?> entityType) {
        ServerLevel world = player.level();

        Entity entity = entityType.spawn(world, player.blockPosition(), EntitySpawnReason.TRIGGERED);

        if (entity instanceof Mob mob) {
            // Use Mob instead of LivingEntity
            ScalingTier tier = ScalingTier.ELITE;
            applyScaling(mob, tier);
            setMobTier(mob, tier.ordinal());
            trackUpgradedMob(entity.getUUID(), world.getServer().getTickCount());
        }
    }


    private static void applyScaling(Mob mob, ScalingTier tier) {
        try {
            // Scale health
            var healthAttr = mob.getAttribute(Attributes.MAX_HEALTH);
            if (healthAttr != null) {
                double baseHealth = healthAttr.getBaseValue();
                double newHealth = baseHealth * tier.healthMultiplier;
                healthAttr.setBaseValue(newHealth);
                mob.setHealth((float) newHealth);
            }

            // Scale damage
            var damageAttr = mob.getAttribute(Attributes.ATTACK_DAMAGE);
            if (damageAttr != null) {
                double baseDamage = damageAttr.getBaseValue();
                damageAttr.setBaseValue(baseDamage * tier.damageMultiplier);
            }

            // Set name with tier
            String mobName = mob.getType().getDescription().getString();
            String stars = getStars(tier);
            mob.setCustomName(Component.literal(stars + " " + tier.displayName + " " + mobName)
                    .withStyle(tier.color));
            mob.setCustomNameVisible(false);
            scaledEntities.add(mob.getUUID());
            // Persist so a server restart restores tracking instead of re-scaling.
            mob.addTag(SCALED_TAG);
            mob.addTag(TIER_TAG_PREFIX + tier.ordinal());

        } catch (Exception e) {
            // Silently fail if attributes don't exist
            checkedEntities.add(mob.getUUID());
        }
    }

    private static String getStars(ScalingTier tier) {
        return switch (tier) {
            case ENHANCED   -> "✦";
            case REINFORCED -> "✦✦";
            case ELITE      -> "✦✦✦";
            case CHAMPION   -> "✦✦✦✦";
            case LEGENDARY  -> "✦✦✦✦✦";
            case APEX       -> "✦✦✦✦✦✦";
            case MYTHIC     -> "✧✦✧✦✧✦✧";
            case ASCENDED   -> "✦✧✦✧✦✧✦✧";
            case DIVINE     -> "❂✦❂✦❂✦❂✦❂";
            case ETERNAL    -> "❂❂❂✦❂❂❂✦❂❂❂";
        };
    }
    public static void markForDespawn(LivingEntity mob) {
        // Store spawn time in scoreboard or custom tag
        mob.tickCount = 0; // Reset tick to track time alive
    }
    // ============================================================
    // REWARDS
    // ============================================================

    public static void onScaledMobKill(LivingEntity entity, ServerPlayer killer) {
        if (!scaledEntities.contains(entity.getUUID())) return;

        int bonus = getKillBonus(entity);
        if (bonus > 0) {
            long scaled = (long) Math.ceil(bonus * MasteryHeartTree.coinKillBonusMultiplier(killer.getUUID().toString()));
            CoinManager.giveCoinsQuiet(killer, scaled);
            String tierName = entity.getCustomName() != null ? entity.getCustomName().getString() : "Scaled mob";
            killer.sendSystemMessage(Component.literal("§6+" + scaled + " coins §7(" + tierName + "§7)")
                    .withStyle(ChatFormatting.GOLD), true);
        }

        cleanup(entity.getUUID());
    }

    private static final int[] TIER_KILL_BONUS = {1, 2, 4, 6, 8, 16, 30, 50, 80, 120};

    public static int getKillBonus(LivingEntity entity) {
        if (!scaledEntities.contains(entity.getUUID())) return 0;

        Integer tier = mobTiers.get(entity.getUUID());
        if (tier != null && tier >= 0 && tier < TIER_KILL_BONUS.length) {
            return TIER_KILL_BONUS[tier];
        }

        Component name = entity.getCustomName();
        if (name == null) return 0;
        String nameStr = name.getString();

        if (nameStr.contains("ETERNAL"))  return 120;
        if (nameStr.contains("DIVINE"))   return 80;
        if (nameStr.contains("ASCENDED")) return 50;
        if (nameStr.contains("MYTHIC"))   return 30;
        if (nameStr.contains("APEX"))     return 16;
        if (nameStr.contains("LEGENDARY")) return 8;
        if (nameStr.contains("Champion")) return 6;
        if (nameStr.contains("Elite"))    return 4;
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
        wallOcclusionNameTagMobs.remove(uuid);
    }

    public static void clearAll() {
        scaledEntities.clear();
        checkedEntities.clear();
        wallOcclusionNameTagMobs.clear();
    }
    private static final Map<UUID, Integer> mobTiers = new HashMap<>();

    public static void setMobTier(LivingEntity mob, int tier) {
        mobTiers.put(mob.getUUID(), tier);
    }

    public static int getMobTier(LivingEntity mob) {
        return mobTiers.getOrDefault(mob.getUUID(), 0);
    }

    public static void onMobDeath(LivingEntity entity) {
        if (entity == null) return;
        UUID uuid = entity.getUUID();
        scaledEntities.remove(uuid);
        checkedEntities.remove(uuid);
        wallOcclusionNameTagMobs.remove(uuid);
        extraDropsCount.remove(uuid);
        mobTiers.remove(uuid);
        upgradedMobSpawnTimes.remove(uuid);
    }
    private static int cleanupTickCounter = 0;

    public static void tickCleanup() {
        cleanupTickCounter++;
        if (cleanupTickCounter < 1200) return;
        cleanupTickCounter = 0;
        // Sweep all UUID-keyed maps and remove entries for entities that no longer
        // exist in any loaded world.
        MinecraftServer server = PoliticalServer.server;
        if (server == null) {
            checkedEntities.clear();
            return;
        }
        java.util.function.Predicate<UUID> isDead = uuid -> {
            for (ServerLevel world : server.getAllLevels()) {
                Entity e = world.getEntity(uuid);
                if (e != null && e.isAlive()) return false;
            }
            return true;
        };
        scaledEntities.removeIf(isDead);
        checkedEntities.removeIf(isDead);
        wallOcclusionNameTagMobs.removeIf(isDead);
        mobTiers.keySet().removeIf(isDead);
        extraDropsCount.keySet().removeIf(isDead);
        upgradedMobSpawnTimes.keySet().removeIf(isDead);
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