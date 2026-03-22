package com.political;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.mob.PiglinBruteEntity;
import net.minecraft.entity.mob.PiglinEntity;
import com.political.ArmourAttribute;

import java.util.*;

public class SlayerManager {

    // ============================================================
    // SLAYER TYPES - Ordered by difficulty progression
    // ============================================================
    public enum SlayerType {
        ZOMBIE("Zombie", "The Undying Outlaw", Formatting.DARK_GREEN, Items.ROTTEN_FLESH, 1.0),
        SPIDER("Spider", "The Venomous Bandit", Formatting.DARK_RED, Items.SPIDER_EYE, 1.8),
        SKELETON("Skeleton", "The Bone Desperado", Formatting.WHITE, Items.BONE, 3.0),
        SLIME("Slime", "The Gelatinous Rustler", Formatting.GREEN, Items.SLIME_BALL, 5.0),
        ENDERMAN("Enderman", "The Void Phantom", Formatting.DARK_PURPLE, Items.ENDER_PEARL, 10.0),
        PIGLIN("Piglin", "The Gilded Ravager", Formatting.GOLD, Items.GOLD_NUGGET, 15.0),
        IRON_GOLEM("Warden", "The Deep Dark Sentinel", Formatting.DARK_PURPLE, Items.SCULK_CATALYST, 25.0);


        public final String displayName;
        public final String bossName;
        public final Formatting color;
        public final net.minecraft.item.Item icon;
        public final double difficultyMultiplier;

        SlayerType(String displayName, String bossName, Formatting color,
                   net.minecraft.item.Item icon, double difficultyMultiplier) {
            this.displayName = displayName;
            this.bossName = bossName;
            this.color = color;
            this.icon = icon;
            this.difficultyMultiplier = difficultyMultiplier;
        }

        // Get the recommended previous slayer type for gear
        public SlayerType getPreviousSlayer() {
            int idx = this.ordinal() - 1;
            if (idx < 0) return null;
            return values()[idx];
        }

        public static SlayerType fromString(String name) {
            try {
                return valueOf(name.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    // ============================================================
    // TIER CONFIGURATION
    // ============================================================
    public static class TierConfig {
        public final int tier;
        public final int killsRequired;
        public final double baseHp;
        public final double baseDamage;
        public final int coinCost;
        public final int xpReward;

        public final int coinReward;
        public final int minLevel;
        public final double damageResistance; // % of non-slayer damage ignored
        public final int miniBossCount;


        public TierConfig(int tier, int killsRequired, double baseHp, double baseDamage,
                          int coinCost, int coinReward, int xpReward, int minLevel,
                          double damageResistance, int miniBossCount) {
            this.tier = tier;
            this.killsRequired = killsRequired;
            this.baseHp = baseHp;
            this.baseDamage = baseDamage;
            this.coinCost = coinCost;
            this.coinReward = coinReward;  // ← Now this works!
            this.xpReward = xpReward;
            this.minLevel = minLevel;
            this.damageResistance = damageResistance;
            this.miniBossCount = miniBossCount;
        }

        // Actual boss HP = baseHp * slayerType.difficultyMultiplier
        public double getActualHp(SlayerType type) {
            return baseHp * type.difficultyMultiplier;
        }

        // Actual boss damage = baseDamage * slayerType.difficultyMultiplier
        public double getActualDamage(SlayerType type) {
            return baseDamage * type.difficultyMultiplier;
        }
    }

    public static final TierConfig[] TIERS = {
            //          tier, kills, baseHP, baseDmg, cost,  coinReward, xp,   minLvl, dmgResist, miniBosses
            new TierConfig(1,  3,     100,    4,       100,   50,         15,   0,      0.0,       0),
            new TierConfig(2,  5,     500,    8,       500,   150,        75,   1,      0.15,      1),
            new TierConfig(3,  7,     2000,   15,      2000,  400,        300,  3,      0.30,      2),
            new TierConfig(4,  9,     10000,  25,      10000, 1000,       1500, 5,      0.50,      3),
            new TierConfig(5,  11,    50000,  40,      50000, 3000,       4500, 7,      0.65,      4),
    };

    public static int getKillsRequired(SlayerType type, int tier) {
        if (type == SlayerType.IRON_GOLEM) {
            return 1; // Wardens are rare, only need 1 kill for any tier
        }
        TierConfig config = getTierConfig(tier);
        return config != null ? config.killsRequired : 0;
    }

    public static TierConfig getTierConfig(int tier) {
        if (tier < 1 || tier > TIERS.length) return null;
        return TIERS[tier - 1];
    }

    // ============================================================
    // XP & LEVEL SYSTEM (Levels 1-12)
    // ============================================================
    public static final int MAX_LEVEL = 12;

    public static final long[] XP_REQUIREMENTS = {
            5,           // Level 1
            15,          // Level 2
            200,         // Level 3
            1_000,       // Level 4
            5_000,       // Level 5
            20_000,      // Level 6
            100_000,     // Level 7
            400_000,     // Level 8
            1_000_000,   // Level 9
            2_000_000,   // Level 10
            5_000_000,   // Level 11
            10_000_000,  // Level 12
    };

    public static final int[] LEVEL_CREDIT_REWARDS = {
            1,        // Level 1: 1 credit
            2,        // Level 2: 2 credits
            5,        // Level 3: 5 credits
            25,       // Level 4: 10 credits
            100,       // Level 5: 25 credits
            250,       // Level 6: 50 credits
            500,      // Level 7: 100 credits
            1000,      // Level 8: 200 credits
            2500,      // Level 9: 500 credits
            5000,     // Level 10: 1000 credits
            10000,     // Level 11: 2500 credits
            25000,     // Level 12: 5000 credits
    };

    // Returns level for given XP amount (0 if below level 1)
    public static int getLevelForXp(long xp) {
        for (int i = XP_REQUIREMENTS.length - 1; i >= 0; i--) {
            if (xp >= XP_REQUIREMENTS[i]) return i + 1;
        }
        return 0;
    }

    // Returns XP needed for next level, or -1 if maxed
    public static long getXpForNextLevel(int currentLevel) {
        if (currentLevel >= MAX_LEVEL) return -1;
        return XP_REQUIREMENTS[currentLevel]; // currentLevel is 0-indexed for next
    }

    // ============================================================
    // ACTIVE QUEST TRACKING
    // ============================================================
    public static class ActiveQuest {
        public final String playerUuid;
        public final SlayerType slayerType;
        public final int tier;
        public int killCount;
        public boolean bossSpawned;
        public boolean bossAlive;
        public UUID bossEntityUuid;
        public int miniBossesSpawned;
        public long startTime;

        public ActiveQuest(String playerUuid, SlayerType slayerType, int tier) {
            this.playerUuid = playerUuid;
            this.slayerType = slayerType;
            this.tier = tier;
            this.killCount = 0;
            this.bossSpawned = false;
            this.bossAlive = false;
            this.bossEntityUuid = null;
            this.miniBossesSpawned = 0;
            this.startTime = System.currentTimeMillis();
        }

        public TierConfig getConfig() {
            return getTierConfig(tier);
        }

        public int getKillsRequired() {
            TierConfig config = getConfig();
            return config != null ? config.killsRequired : 0;
        }

        public double getProgress() {
            int required = getKillsRequired();
            if (required == 0) return 1.0;
            return Math.min(1.0, (double) killCount / required);
        }

        public boolean isReadyForBoss() {
            return killCount >= getKillsRequired() && !bossSpawned;
        }
    }

    // Active quests per player UUID
    private static final Map<String, ActiveQuest> activeQuests = new HashMap<>();

    // Boss entity UUID -> player UUID mapping for tracking
    private static final Map<UUID, String> bossOwners = new HashMap<>();

    // Boss bars per player
    private static final Map<String, ServerBossBar> bossBars = new HashMap<>();

    // Slayer boss entities tracked for damage resistance
    private static final Set<UUID> slayerBossEntities = new HashSet<>();

    // ============================================================
    // QUEST MANAGEMENT (continued)
    // ============================================================
// Add to SlayerManager.java

    /**
     * Show boss spawn progress in action bar
     */
    public static void showSpawnProgress(ServerPlayerEntity player) {
        ActiveQuest quest = activeQuests.get(player.getUuidAsString());
        if (quest == null || quest.bossSpawned) return;

        int current = quest.killCount;
        int required = quest.getKillsRequired();
        double progress = quest.getProgress();

        // Build progress bar
        int barLength = 20;
        int filled = (int) (progress * barLength);

        StringBuilder bar = new StringBuilder();
        bar.append("§7[");
        for (int i = 0; i < barLength; i++) {
            if (i < filled) {
                bar.append("§a█");
            } else {
                bar.append("§8░");
            }
        }
        bar.append("§7]");

        // Color based on progress
        String countColor = progress >= 1.0 ? "§a" : progress >= 0.5 ? "§e" : "§c";

        String message = String.format("§6%s Bounty §7| %s %s%d§7/§a%d §7| %s",
                quest.slayerType.displayName,
                bar.toString(),
                countColor,
                current,
                required,
                progress >= 1.0 ? "§a§lREADY!" : "§7T" + quest.tier
        );

        player.sendMessage(Text.literal(message), true);
    }

    /**
     * Called when a mob is killed - show progress update
     */


    /**
     * Force check and spawn boss if ready
     */
    public static void checkBossSpawn(ServerPlayerEntity player) {
        ActiveQuest quest = activeQuests.get(player.getUuidAsString());
        if (quest == null || quest.bossSpawned) return;

        if (quest.killCount >= quest.getKillsRequired()) {
            spawnBoss(player, quest);
        }
    }

    public static boolean startQuest(ServerPlayerEntity player, SlayerType type, int tier) {
        String uuid = player.getUuidAsString();

        if (!hasUnlockedSlayer(uuid, type)) {
            String req = getUnlockRequirement(type);
            player.sendMessage(Text.literal("✖ " + req + " first!")
                    .formatted(Formatting.RED), false);
            return false;
        }
        // Check if already has active quest
        if (activeQuests.containsKey(uuid)) {
            player.sendMessage(Text.literal("✖ You already have an active bounty!")
                    .formatted(Formatting.RED), false);
            return false;
        }

        // Validate tier
        TierConfig config = getTierConfig(tier);
        if (config == null) {
            player.sendMessage(Text.literal("✖ Invalid tier!")
                    .formatted(Formatting.RED), false);
            return false;
        }

        // Check level requirement
        long playerXp = SlayerData.getSlayerXp(uuid, type);
        int playerLevel = getLevelForXp(playerXp);
        if (playerLevel < config.minLevel) {
            player.sendMessage(Text.literal("✖ Requires " + type.displayName + " Bounty Level " + config.minLevel + "!")
                    .formatted(Formatting.RED), false);
            return false;
        }

        // Check coin cost
        if (!CoinManager.hasCoins(player, config.coinCost)) {
            player.sendMessage(Text.literal("✖ Not enough coins! Need " + config.coinCost + " coins.")
                    .formatted(Formatting.RED), false);
            return false;
        }

        // Deduct coins
        CoinManager.removeCoins(player, config.coinCost);

        // Create quest
        ActiveQuest quest = new ActiveQuest(uuid, type, tier);
        activeQuests.put(uuid, quest);

        // Notify player
        player.sendMessage(Text.literal(""), false);
        player.sendMessage(Text.literal("══════════════════════════════")
                .formatted(Formatting.GOLD), false);
        player.sendMessage(Text.literal("  🎯 BOUNTY ACCEPTED 🎯")
                .formatted(type.color, Formatting.BOLD), false);
        player.sendMessage(Text.literal("══════════════════════════════")
                .formatted(Formatting.GOLD), false);
        player.sendMessage(Text.literal("  Target: ").formatted(Formatting.GRAY)
                .append(Text.literal(type.displayName).formatted(type.color)), false);
        player.sendMessage(Text.literal("  Tier: ").formatted(Formatting.GRAY)
                .append(Text.literal(String.valueOf(tier)).formatted(Formatting.YELLOW)), false);
        player.sendMessage(Text.literal("  Kills Required: ").formatted(Formatting.GRAY)
                .append(Text.literal(String.valueOf(getKillsRequired(type, tier))).formatted(Formatting.WHITE)), false);
        player.sendMessage(Text.literal(""), false);
        player.sendMessage(Text.literal("  Hunt down " + type.displayName + "s to summon the target!")
                .formatted(Formatting.GREEN), false);
        player.sendMessage(Text.literal("══════════════════════════════")
                .formatted(Formatting.GOLD), false);
        player.sendMessage(Text.literal("§8[Charadrius Protocol engaged]"), false);

        // Save data
        DataManager.save(PoliticalServer.server);

        return true;
    }

    public static void cancelQuest(ServerPlayerEntity player) {
        String uuid = player.getUuidAsString();
        ActiveQuest quest = activeQuests.remove(uuid);

        if (quest != null) {
            // Clean up boss bar
            ServerBossBar bossBar = bossBars.remove(uuid);
            if (bossBar != null) {
                bossBar.clearPlayers();
            }

            // *** FIX: DESPAWN THE BOSS ENTITY ***
            if (quest.bossEntityUuid != null) {
                // Find and remove the boss from all worlds
                for (ServerWorld world : PoliticalServer.server.getWorlds()) {
                    var entity = world.getEntity(quest.bossEntityUuid);
                    if (entity != null) {
                        entity.discard(); // Remove the entity from the world
                        break;
                    }
                }

                // Clean up tracking
                bossOwners.remove(quest.bossEntityUuid);
                slayerBossEntities.remove(quest.bossEntityUuid);
                BossAbilityManager.removeBoss(quest.bossEntityUuid);
            }

            player.sendMessage(Text.literal("✖ Bounty cancelled. Boss despawned. No refund.")
                    .formatted(Formatting.RED), false);
        }
    }

    public static ActiveQuest getActiveQuest(ServerPlayerEntity player) {
        return activeQuests.get(player.getUuidAsString());
    }

    public static ActiveQuest getActiveQuest(String uuid) {
        return activeQuests.get(uuid);
    }

    public static boolean hasActiveQuest(ServerPlayerEntity player) {
        return activeQuests.containsKey(player.getUuidAsString());
    }

    // ============================================================
    // KILL TRACKING
    // ============================================================

    public static void onMobKill(ServerPlayerEntity player, LivingEntity killed) {
        String uuid = player.getUuidAsString();
        ActiveQuest quest = activeQuests.get(uuid);

        if (quest == null || quest.bossSpawned) return;

        // Check if killed mob matches quest type
        if (!isMatchingMob(killed, quest.slayerType)) return;

        // Calculate kill value
        int killValue = 1;

        // Slayer sword bonus: 2x kills
        ItemStack weapon = player.getMainHandStack();
        if (SlayerItems.isSlayerSword(weapon)) {
            SlayerType swordType = SlayerItems.getSwordSlayerType(weapon);
            if (swordType == quest.slayerType) {
                killValue = 2;
            }
        }

        // Scaled mob bonus
        if (HealthScalingManager.isScaledMob(killed.getUuid())) {
            int bonus = HealthScalingManager.getKillBonus(killed);
            killValue += bonus;
        }

        quest.killCount += killValue;

        int required = getKillsRequired(quest.slayerType, quest.tier);
        int remaining = Math.max(0, required - quest.killCount);

        // ========== SCOREBOARD-STYLE PROGRESS DISPLAY ==========
        String progressBar = createProgressBar(quest.killCount, required, 20);
        String displayText = "§6§l☠ " + quest.slayerType.displayName + " Bounty §r§7| " +
                progressBar + " §e" + quest.killCount + "§7/§e" + required;

        // Show in action bar (above hotbar)
        player.sendMessage(Text.literal(displayText), true);

        // Bonus kill notification
        if (killValue > 1) {
            player.sendMessage(Text.literal("§a+" + killValue + " kills!"), true);
        }

        // Check if ready for boss
        if (quest.killCount >= required && !quest.bossSpawned) {
            player.sendMessage(Text.literal(""), false);
            player.sendMessage(Text.literal("§4§l☠ TARGET INCOMING! ☠")
                    .formatted(Formatting.BOLD), false);
            player.sendMessage(Text.literal("§c" + quest.slayerType.bossName + " is approaching...")
                    , false);
            player.sendMessage(Text.literal(""), false);

            spawnBoss(player, quest);
        }
    }

    // Add this helper method:
    private static String createProgressBar(int current, int max, int barLength) {
        double progress = Math.min(1.0, (double) current / max);
        int filled = (int) (progress * barLength);

        StringBuilder bar = new StringBuilder("§8[");
        for (int i = 0; i < barLength; i++) {
            if (i < filled) {
                // Color gradient based on progress
                if (progress < 0.33) bar.append("§c█");
                else if (progress < 0.66) bar.append("§e█");
                else bar.append("§a█");
            } else {
                bar.append("§7░");
            }
        }
        bar.append("§8]");
        return bar.toString();
    }

    public static void spawnBoss(ServerWorld world, ServerPlayerEntity player, SlayerType type, int tier) {
        // Your spawn logic here
        // Create and spawn the boss entity based on type and tier
    }

    public static void adminSpawnBoss(ServerPlayerEntity player, SlayerType type, int tier) {
        ServerWorld world = player.getEntityWorld();
        spawnBoss(world, player, type, tier);
    }

    private static boolean isMatchingMob(LivingEntity entity, SlayerType type) {
        return switch (type) {
            case ZOMBIE -> entity instanceof ZombieEntity;
            case SPIDER -> entity instanceof SpiderEntity;
            case SKELETON -> entity instanceof SkeletonEntity || entity instanceof StrayEntity
                    || entity instanceof WitherSkeletonEntity;
            case SLIME -> entity instanceof SlimeEntity || entity instanceof MagmaCubeEntity;
            case ENDERMAN -> entity instanceof EndermanEntity;
            case IRON_GOLEM -> entity instanceof WardenEntity;
            case PIGLIN -> entity instanceof PiglinBruteEntity || entity instanceof PiglinEntity;
        };
    }

    // ============================================================
    // BOSS SPAWNING
    // ============================================================
    public static void addHiddenEffect(LivingEntity entity, RegistryEntry<StatusEffect> effect,
                                       int duration, int amplifier) {
        entity.addStatusEffect(new StatusEffectInstance(
                effect,
                duration,
                amplifier,
                true,   // ambient - reduces particles
                false,  // showParticles
                false   // showIcon - hides from HUD
        ));
    }

    private static void spawnBoss(ServerPlayerEntity player, ActiveQuest quest) {
        if (quest.bossSpawned) return;

        ServerWorld world = player.getEntityWorld();
        TierConfig config = quest.getConfig();
        SlayerType type = quest.slayerType;

        // Find valid spawn position nearby
        BlockPos spawnPos = player.getBlockPos();

        MobEntity boss;

        // Spawn actual Warden entity
        if (type == SlayerType.IRON_GOLEM) {
            boss = (MobEntity) EntityType.IRON_GOLEM.create(world, SpawnReason.MOB_SUMMONED);
            if (boss == null) {
                player.sendMessage(Text.literal("✖ Failed to spawn boss!")
                        .formatted(Formatting.RED), false);
                return;
            }
            spawnPos = player.getBlockPos().up(2); // Spawn 2 blocks above
            // Fix: Prevent Warden from going underground
            if (boss instanceof net.minecraft.entity.mob.WardenEntity warden) {
                warden.setPersistent();
                // Force surface-only behavior
                warden.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 0, false, false));
                warden.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 1, 0, false, false));
            }
        } else {
            // Create boss entity for other types
            boss = createBossEntity(world, type, config);
            if (boss == null) {
                player.sendMessage(Text.literal("✖ Failed to spawn boss!")
                        .formatted(Formatting.RED), false);
                return;
            }
        }

        // Initialize slime boss tracking BEFORE spawning
        if (type == SlayerType.SLIME) {
            BossAbilityManager.initSlimeBoss(boss.getUuid());
        }

        // Position the boss
        boss.setPosition(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);

        // Apply boss stats
        double actualHp = config.getActualHp(type);
        double actualDamage = config.getActualDamage(type);

        // Set max health
        var healthAttr = boss.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.setBaseValue(actualHp);
            boss.setHealth((float) actualHp);
        }

        // Set damage
        var damageAttr = boss.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE);
        if (damageAttr != null) {
            damageAttr.setBaseValue(actualDamage);
        }

        // Set custom name
        String tierRoman = toRoman(quest.tier);
        boss.setCustomName(Text.literal(type.bossName + " " + tierRoman)
                .formatted(type.color, Formatting.BOLD));
        boss.setCustomNameVisible(false);

        // Add effects for visual flair
        boss.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, Integer.MAX_VALUE, 0, false, false));
        boss.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));

        // Can't pick up items
        boss.setCanPickUpLoot(false);
        boss.setPersistent();

        // Apply type-specific buffs (status effects, knockback resistance, armor, etc.)
        applyBossBuffs(boss, type, quest.tier);

        // **CRITICAL: Actually spawn the boss into the world**
        world.spawnEntity(boss);

        // Make Warden aggressive toward the player
        if (type == SlayerType.IRON_GOLEM && boss instanceof net.minecraft.entity.mob.WardenEntity warden) {
            // Set target first
            warden.setTarget(player);
            // Then increase anger to make it immediately aggressive (prevents digging)
            warden.increaseAngerAt(player, 150, true);
        } else if (type == SlayerType.IRON_GOLEM) {
            boss.setTarget(player);
        }

        // Track the boss
        quest.bossSpawned = true;
        quest.bossAlive = true;
        quest.bossEntityUuid = boss.getUuid();
        bossOwners.put(boss.getUuid(), player.getUuidAsString());
        slayerBossEntities.add(boss.getUuid());

        // Register with BossAbilityManager
        BossAbilityManager.registerBoss(boss, type, quest.tier);

        // Create boss bar
        ServerBossBar bossBar = new ServerBossBar(
                Text.literal(type.bossName + " " + tierRoman).formatted(type.color, Formatting.BOLD),
                getBossBarColor(type),
                BossBar.Style.NOTCHED_10
        );
        bossBar.addPlayer(player);
        bossBar.setPercent(1.0f);
        bossBars.put(player.getUuidAsString(), bossBar);

        // Announce
        player.sendMessage(Text.literal(""), false);
        player.sendMessage(Text.literal("☠☠☠ " + type.bossName.toUpperCase() + " HAS SPAWNED! ☠☠☠")
                .formatted(Formatting.DARK_RED, Formatting.BOLD), false);
        player.sendMessage(Text.literal("HP: " + String.format("%,.0f", actualHp) + " ❤")
                .formatted(Formatting.RED), false);
        player.sendMessage(Text.literal(""), false);

        // 20% chance Charadrius MFLUX alert
        if (world.getRandom().nextFloat() < 0.20f) {
            player.sendMessage(Text.literal("§8[MFLUX Alert: Charadrius subject detected in your area]"), false);
        }

        // Sound effect
        world.playSound(null, player.getBlockPos(),
                net.minecraft.sound.SoundEvents.ENTITY_WITHER_SPAWN,
                net.minecraft.sound.SoundCategory.HOSTILE, 1.0f, 0.5f);
    }

    private static void spawnMiniBoss(ServerPlayerEntity player, ActiveQuest quest) {
        ServerWorld world = player.getEntityWorld();
        TierConfig config = quest.getConfig();
        SlayerType type = quest.slayerType;

        BlockPos spawnPos = findSpawnPosition(world, player.getBlockPos(), 8);
        if (spawnPos == null) return;

        MobEntity miniBoss = createBossEntity(world, type, config);
        if (miniBoss == null) return;

        miniBoss.setPosition(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);

        // Mini-boss has 5% of main boss HP
        double hp = config.getActualHp(type) * 0.05;
        var healthAttr = miniBoss.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.setBaseValue(hp);
            miniBoss.setHealth((float) hp);
        }

        miniBoss.setCustomName(Text.literal("✦ " + type.displayName + " Minion ✦")
                .formatted(type.color));
        miniBoss.setCustomNameVisible(false);
        miniBoss.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, Integer.MAX_VALUE, 0, false, false));

        world.spawnEntity(miniBoss);

        player.sendMessage(Text.literal("⚠ A " + type.displayName + " Minion has spawned!")
                .formatted(Formatting.GOLD), false);
    }

    private static MobEntity createBossEntity(ServerWorld world, SlayerType type, TierConfig config) {
        EntityType<?> entityType = switch (type) {
            case ZOMBIE -> EntityType.ZOMBIE;
            case SPIDER -> EntityType.SPIDER;
            case SKELETON -> EntityType.SKELETON;
            case SLIME -> EntityType.SLIME;
            case ENDERMAN -> EntityType.ENDERMAN;
            case IRON_GOLEM -> EntityType.IRON_GOLEM;
            case PIGLIN -> EntityType.PIGLIN_BRUTE;
        };

        MobEntity mob = (MobEntity) entityType.create(world, SpawnReason.MOB_SUMMONED);

        // Set slime size to max (4) for Slime bosses
        if (type == SlayerType.SLIME && mob instanceof net.minecraft.entity.mob.SlimeEntity slime) {
            slime.setSize(4, true);
        }

        return mob;
    }

    private static BlockPos findSpawnPosition(ServerWorld world, BlockPos center, int radius) {
        Random rand = new Random();
        for (int i = 0; i < 10; i++) {
            int x = center.getX() + rand.nextInt(radius * 2) - radius;
            int z = center.getZ() + rand.nextInt(radius * 2) - radius;
            BlockPos check = new BlockPos(x, center.getY(), z);

            // Find ground level
            while (check.getY() > world.getBottomY() && !world.getBlockState(check.down()).isSolidBlock(world, check.down())) {
                check = check.down();
            }
            while (check.getY() < world.getTopYInclusive() && world.getBlockState(check).isSolidBlock(world, check)) {
                check = check.up();
            }

            // Check if valid spawn (2 blocks of air)
            if (!world.getBlockState(check).isSolidBlock(world, check) &&
                    !world.getBlockState(check.up()).isSolidBlock(world, check.up())) {
                return check;
            }
        }
        return null;
    }

    private static BossBar.Color getBossBarColor(SlayerType type) {
        return switch (type) {
            case ZOMBIE -> BossBar.Color.GREEN;
            case SPIDER -> BossBar.Color.RED;
            case SKELETON -> BossBar.Color.WHITE;
            case SLIME -> BossBar.Color.GREEN;
            case ENDERMAN -> BossBar.Color.PURPLE;
            case IRON_GOLEM -> BossBar.Color.BLUE;
            case PIGLIN -> BossBar.Color.YELLOW;
        };
    }

    private static String toRoman(int num) {
        return switch (num) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(num);
        };
    }

    // ============================================================
    // BOSS DEATH & REWARDS
    // ============================================================
// ============================================================
// ADMIN TEST BOSS SPAWNING
// ============================================================

    public static void spawnTestBoss(ServerPlayerEntity player, SlayerType type, int tier) {
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        TierConfig config = getTierConfig(tier);
        if (config == null) return;

        BlockPos spawnPos = findSpawnPosition(world, player.getBlockPos(), 5);
        if (spawnPos == null) spawnPos = player.getBlockPos();

        MobEntity boss = createBossEntity(world, type, config);
        if (boss == null) {
            player.sendMessage(Text.literal("✖ Failed to spawn boss!").formatted(Formatting.RED), false);
            return;
        }

        boss.setPosition(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);

        double actualHp = config.getActualHp(type);
        double actualDamage = config.getActualDamage(type);

        var healthAttr = boss.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.setBaseValue(actualHp);
            boss.setHealth((float) actualHp);
        }

        var damageAttr = boss.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE);
        if (damageAttr != null) {
            damageAttr.setBaseValue(actualDamage);
        }

        String tierRoman = toRoman(tier);
        boss.setCustomName(Text.literal("[TEST] " + type.bossName + " " + tierRoman)
                .formatted(type.color, Formatting.BOLD));
        boss.setCustomNameVisible(false);
        boss.setPersistent();

        world.spawnEntity(boss);

        world.playSound(null, player.getBlockPos(),
                net.minecraft.sound.SoundEvents.ENTITY_WITHER_SPAWN,
                net.minecraft.sound.SoundCategory.HOSTILE, 1.0f, 0.5f);
    }

    public static double getLevelDamageMultiplier(ServerPlayerEntity player, SlayerType bossType) {
        int level = SlayerData.getSlayerLevel(player.getUuidAsString(), bossType);
        return 1.0 + (level * DAMAGE_BONUS_PER_LEVEL);
    }
    public static double getLevelDamageReduction(ServerPlayerEntity player, SlayerType bossType) {
        int level = SlayerData.getSlayerLevel(player.getUuidAsString(), bossType);
        return level * DAMAGE_REDUCTION_PER_LEVEL; // Returns 0.0 to 0.18
    }

    public static final double DAMAGE_BONUS_PER_LEVEL = 0.02;
    public static final double DAMAGE_REDUCTION_PER_LEVEL = 0.015;
    public static void markAsSlayerBoss(UUID uuid) {
        slayerBossEntities.add(uuid);
    }
    public static void onBossDeath(LivingEntity entity, ServerPlayerEntity killer) {
        UUID bossUuid = entity.getUuid();
        String ownerUuid = bossOwners.get(bossUuid);
        if (ownerUuid == null) return;

        // Use a different variable name to avoid conflict
        ActiveQuest bossQuest = activeQuests.get(ownerUuid);
        if (bossQuest == null) return;

        SlayerType bossType = bossQuest.slayerType;
        int tier = bossQuest.tier;

        if (bossType == SlayerType.SLIME) {
            // Let BossAbilityManager handle slime splitting
            ServerWorld world = (ServerWorld) entity.getEntityWorld();
            BossAbilityManager.onSlimeBossDeath(entity, world, killer, tier);
            return; // Don't complete quest yet - onSlimeBossDeath handles it
        }

        ActiveQuest quest = activeQuests.get(ownerUuid);
        if (quest == null || !bossUuid.equals(quest.bossEntityUuid)) return;

        ServerPlayerEntity owner = PoliticalServer.server.getPlayerManager().getPlayer(UUID.fromString(ownerUuid));
        if (owner == null) return;

        TierConfig config = quest.getConfig();
        SlayerType type = quest.slayerType;

        // Award XP (with Prospector bonus + perk multiplier)
        long baseXp = config.xpReward;
        int prospectorLevel = CustomEnchantmentManager.getLevel(owner.getMainHandStack(), CustomEnchantmentManager.PROSPECTOR);
        float xpMultiplier = 1.0f + (0.10f * prospectorLevel);
        xpMultiplier *= PerkManager.getBountyXpMultiplier();
        long xpGained = (long)(baseXp * xpMultiplier);
        long oldXp = SlayerData.getSlayerXp(ownerUuid, type);
        int oldLevel = getLevelForXp(oldXp);

        SlayerData.addSlayerXp(ownerUuid, type, xpGained);
        SlayerData.updateHighestTier(ownerUuid, type, quest.tier);
        SlayerData.incrementBossesKilled(ownerUuid, type);

        long newXp = SlayerData.getSlayerXp(ownerUuid, type);
        int newLevel = getLevelForXp(newXp);

        // Victory message
        owner.sendMessage(Text.literal(""), false);
        owner.sendMessage(Text.literal("══════════════════════════════")
                .formatted(Formatting.GOLD), false);
        owner.sendMessage(Text.literal("  ✔ Bounty Completed!")
                .formatted(Formatting.GREEN, Formatting.BOLD), false);
        owner.sendMessage(Text.literal("══════════════════════════════")
                .formatted(Formatting.GOLD), false);
        owner.sendMessage(Text.literal("  +" + xpGained + " " + type.displayName + " Bounty XP")
                .formatted(Formatting.AQUA), false);
        // 20% chance of MFLUX recontainment message
        if (PoliticalServer.server.getOverworld().getRandom().nextFloat() < 0.20f) {
            owner.sendMessage(Text.literal("§8[MFLUX: Subject successfully recontained. Data logged.]"), false);
        }

        if (newLevel > oldLevel) {
            for (int lvl = oldLevel + 1; lvl <= newLevel; lvl++) {
                int creditReward = LEVEL_CREDIT_REWARDS[lvl - 1];

                // Give coins as level-up reward
                CoinManager.giveCoins(owner, creditReward);

                owner.sendMessage(Text.literal(""), false);
                owner.sendMessage(Text.literal("  ⬆ LEVEL UP! " + type.displayName + " Bounty " + lvl)
                        .formatted(Formatting.YELLOW, Formatting.BOLD), false);
                owner.sendMessage(Text.literal("  +" + creditReward + " coins")
                        .formatted(Formatting.GOLD), false);

                // Sound effect
                owner.getEntityWorld().playSound(null, owner.getBlockPos(),
                        net.minecraft.sound.SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                        net.minecraft.sound.SoundCategory.PLAYERS, 1.0f, 1.0f);
            }
        }

        // Roll for drops
        List<ItemStack> drops = rollDrops(type, quest.tier, owner);
        for (ItemStack drop : drops) {
            owner.sendMessage(Text.literal("  ✦ RARE DROP: ")
                    .formatted(Formatting.LIGHT_PURPLE)
                    .append(drop.getName().copy().formatted(Formatting.GOLD)), false);

            if (!owner.getInventory().insertStack(drop)) {
                owner.dropItem(drop, false);
            }
        }

        owner.sendMessage(Text.literal("══════════════════════════════")
                .formatted(Formatting.GOLD), false);

        // Cleanup
        cleanupQuest(ownerUuid);
        DataManager.save(PoliticalServer.server);
    }

    private static List<ItemStack> rollDrops(SlayerType type, int tier, ServerPlayerEntity player) {
        List<ItemStack> drops = new ArrayList<>();
        Random rand = new Random();
        String playerUuid = player.getUuidAsString();
        
        // Get buff bonuses for drop rates
        double bossDropBonus = com.political.PlayerBuffManager.getBossDropBonus(playerUuid);
        double rareDropBonus = com.political.PlayerBuffManager.getRareDropBonus(playerUuid);

        // RARER CHUNK DROP CHANCES
        // T1: 1%, T2: 2%, T3: 4%, T4: 6%, T5: 8%
        double chunkChance = 0.01 + (tier - 1) * 0.0175;
        chunkChance *= PerkManager.getBountyDropMultiplier();
        chunkChance *= (1.0 + bossDropBonus + rareDropBonus); // BOSS_SLAYER and LUCKY buffs
        if (rand.nextDouble() < chunkChance) {
            drops.add(SlayerItems.createChunk(type));
        }

        // RARER CORE DROP CHANCES (for legendary items)
        double coreChance = switch (type) {
            case ZOMBIE, SPIDER, SKELETON -> 0.001 + (tier * 0.002);  // 0.1% - 1.1%
            case SLIME -> 0.0008 + (tier * 0.0015);                    // 0.08% - 0.83%
            case ENDERMAN -> 0.00005 + (tier * 0.00005);               // 0.005% - 0.03%
            case PIGLIN -> 0.002 + (tier * 0.001);                     // 0.2% - 0.7%
            case IRON_GOLEM -> 0.0001 + (tier * 0.0001);                   // 0.01% - 0.06%
        };
        coreChance *= PerkManager.getBountyDropMultiplier();
        coreChance *= (1.0 + bossDropBonus + rareDropBonus); // BOSS_SLAYER and LUCKY buffs

        if (rand.nextDouble() < coreChance) {
            drops.add(SlayerItems.createCore(type));
        }

        // --- FLESH DROPS (90% base chance) ---
        double fleshDropChance = 0.90 * DataManager.getFleshDropMultiplier(); // 90% base * multiplier

        if (rand.nextDouble() < fleshDropChance) {
            if (type == SlayerType.ZOMBIE) {
                // Zombie bosses drop Rotten Flesh
                int fleshCount = 1 + tier; // T1: 2, T2: 3, T3: 4, T4: 5, T5: 6
                drops.add(new ItemStack(Items.ROTTEN_FLESH, fleshCount));
                player.sendMessage(Text.literal("✦ Rotten Flesh dropped! x" + fleshCount).formatted(Formatting.DARK_GREEN), false);
            } else if (type == SlayerType.PIGLIN) {
                // Piglin bosses drop Piglin Flesh
                drops.add(SlayerItems.createPiglinFlesh());
                player.sendMessage(Text.literal("✦ Piglin Flesh dropped!").formatted(Formatting.YELLOW), false);
            }
        }

        // Voidwalker's Crown drop (for ENDERMAN type, T4+)
        double enderHelmetChance = getArmorDropChance(type, tier);
        if (type == SlayerType.ENDERMAN && enderHelmetChance > 0 && rand.nextDouble() * 100 < enderHelmetChance) {
            drops.add(SlayerItems.createVoidwalkerCrown());
        }

        // --- NEW BOSS DROPS ---
        double bossDropChance = 0.05 + (tier * 0.02); // 7% - 15% chance
        if (rand.nextDouble() < bossDropChance) {
            switch (type) {
                case ZOMBIE -> drops.add(SlayerItems.createUndeadHeart());
                case SKELETON -> drops.add(SlayerItems.createSpectralQuiver());
                case IRON_GOLEM -> drops.add(SlayerItems.createEchoingCore());
                case ENDERMAN -> drops.add(SlayerItems.createEnderSword());
                case PIGLIN -> drops.add(SlayerItems.createAbyssalBlade());
                case SLIME -> drops.add(SlayerItems.createBouncySlime());
                case SPIDER -> drops.add(SlayerItems.createVenomousDagger());
            }
        }

        // --- ATTRIBUTE TOKEN DROPS ---
        // Higher tier slayers have better chances for attribute tokens
        if (tier >= 3) {
            double tokenChance = 0.02 + (tier - 3) * 0.01; // T3: 2%, T4: 3%, T5: 4%
            tokenChance *= PerkManager.getBountyDropMultiplier();
            
            if (rand.nextDouble() < tokenChance) {
                // Drop a random attribute token based on slayer type
                ArmourAttribute tokenAttr = getRandomAttributeForSlayerType(type, rand);
                if (tokenAttr != null) {
                    drops.add(SlayerItems.createAttributeToken(tokenAttr));
                    player.sendMessage(Text.literal("✦ " + tokenAttr.displayName + " Attribute Token dropped!")
                            .formatted(tokenAttr.color, Formatting.BOLD), false);
                }
            }
        }

        // Coins based on tier (with BOUNTY_TAX perk applied)
        int baseCoinDrop = tier * 25 * (int) type.difficultyMultiplier;
        int coinDrop = PerkManager.applyBountyTax(baseCoinDrop);
        CoinManager.giveCoinsQuiet(player, coinDrop);

        return drops;
    }

    // Helper method to get random attribute for slayer type
    private static ArmourAttribute getRandomAttributeForSlayerType(SlayerType type, Random rand) {
        // Different slayer types have different attribute pools
        return switch (type) {
            case ZOMBIE -> {
                // Zombie drops: defensive attributes
                ArmourAttribute[] attrs = {
                    ArmourAttribute.OVERGROWN, ArmourAttribute.GROUNDED, ArmourAttribute.CURSED
                };
                yield attrs[rand.nextInt(attrs.length)];
            }
            case SPIDER -> {
                // Spider drops: agility/stealth attributes
                ArmourAttribute[] attrs = {
                    ArmourAttribute.PHANTOMSTEP, ArmourAttribute.WEBBED, ArmourAttribute.VOLATILE
                };
                yield attrs[rand.nextInt(attrs.length)];
            }
            case SKELETON -> {
                // Skeleton drops: ranged/combat attributes
                ArmourAttribute[] attrs = {
                    ArmourAttribute.FRENZIED, ArmourAttribute.SIGHTLESS, ArmourAttribute.VOLATILE
                };
                yield attrs[rand.nextInt(attrs.length)];
            }
            case SLIME -> {
                // Slime drops: defensive/utility attributes
                ArmourAttribute[] attrs = {
                    ArmourAttribute.OVERGROWN, ArmourAttribute.GROUNDED, ArmourAttribute.FROST
                };
                yield attrs[rand.nextInt(attrs.length)];
            }
            case ENDERMAN -> {
                // Enderman drops: mystical/movement attributes
                ArmourAttribute[] attrs = {
                    ArmourAttribute.PHANTOMSTEP, ArmourAttribute.SIGHTLESS, ArmourAttribute.VOLATILE
                };
                yield attrs[rand.nextInt(attrs.length)];
            }
            case PIGLIN -> {
                // Piglin drops: combat/wealth attributes
                ArmourAttribute[] attrs = {
                    ArmourAttribute.FRENZIED, ArmourAttribute.CURSED, ArmourAttribute.VOLATILE
                };
                yield attrs[rand.nextInt(attrs.length)];
            }
            case IRON_GOLEM -> {
                // Warden drops: powerful defensive attributes
                ArmourAttribute[] attrs = {
                    ArmourAttribute.OVERGROWN, ArmourAttribute.GROUNDED, ArmourAttribute.BURNING,
                    ArmourAttribute.FRENZIED, ArmourAttribute.CURSED
                };
                yield attrs[rand.nextInt(attrs.length)];
            }
        };
    }

    private static void cleanupQuest(String playerUuid) {
        ActiveQuest quest = activeQuests.remove(playerUuid);
        if (quest != null && quest.bossEntityUuid != null) {
            BossAbilityManager.removeBoss(quest.bossEntityUuid);
            bossOwners.remove(quest.bossEntityUuid);
            slayerBossEntities.remove(quest.bossEntityUuid);
        }

        ServerBossBar bossBar = bossBars.remove(playerUuid);
        if (bossBar != null) {
            bossBar.clearPlayers();
        }
    }

    // ============================================================
    // DAMAGE RESISTANCE FOR SLAYER BOSSES
    // ============================================================

    public static boolean isSlayerBoss(UUID entityUuid) {
        return slayerBossEntities.contains(entityUuid);
    }

    public static double getDamageResistance(UUID entityUuid) {
        String ownerUuid = bossOwners.get(entityUuid);
        if (ownerUuid == null) return 0.0;

        ActiveQuest quest = activeQuests.get(ownerUuid);
        if (quest == null) return 0.0;

        TierConfig config = quest.getConfig();
        return config != null ? config.damageResistance : 0.0;
    }

    public static SlayerType getBossSlayerType(UUID entityUuid) {
        String ownerUuid = bossOwners.get(entityUuid);
        if (ownerUuid == null) return null;

        ActiveQuest quest = activeQuests.get(ownerUuid);
        return quest != null ? quest.slayerType : null;
    }

    // ============================================================
    // TICK - Update boss bars and check for despawns
    // ============================================================
    public static void trackBossOwner(UUID bossId, String playerUuid) {
        bossOwners.put(bossId, playerUuid);
    }
    public static String getBossOwner(UUID bossId) {
        return bossOwners.get(bossId);
    }


    public static void tick(MinecraftServer server) {
        Iterator<Map.Entry<String, ActiveQuest>> iterator = activeQuests.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, ActiveQuest> entry = iterator.next();
            String playerUuid = entry.getKey();
            ActiveQuest quest = entry.getValue();

            if (!quest.bossAlive || quest.bossEntityUuid == null) continue;

            // Find the boss entity
            LivingEntity boss = null;
            for (ServerWorld world : server.getWorlds()) {
                var entity = world.getEntity(quest.bossEntityUuid);
                if (entity instanceof LivingEntity living) {
                    boss = living;
                    break;
                }
            }
            if (server.getTicks() % 20 == 0) {
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    // Use different variable name to avoid conflict
                    ActiveQuest activeQuest = activeQuests.get(player.getUuidAsString());
                    if (activeQuest != null && !activeQuest.bossSpawned) {
                        showSpawnProgress(player);
                    }
                }
            }
            // Update boss bar
            ServerBossBar bossBar = bossBars.get(playerUuid);
            if (boss != null && boss.isAlive()) {
                if (bossBar != null) {
                    bossBar.setPercent(boss.getHealth() / boss.getMaxHealth());
                }
            } else if (boss == null || !boss.isAlive()) {
                // Boss died or despawned
                quest.bossAlive = false;

                if (bossBar != null) {
                    bossBar.clearPlayers();
                    bossBars.remove(playerUuid);
                }

                // If they killed it legitimately, onBossDeath handles cleanup
                // This handles edge cases like boss despawning
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(UUID.fromString(playerUuid));
                if (player != null && boss == null) {
                    player.sendMessage(Text.literal("✖ Your bounty has escaped! Quest failed.")
                            .formatted(Formatting.RED), false);
                    iterator.remove();
                    bossOwners.remove(quest.bossEntityUuid);
                }
            }
        }
    }

    public static boolean hasUnlockedSlayer(String playerUuid, SlayerType type) {
        // Zombie is always unlocked (first slayer)
        if (type == SlayerType.ZOMBIE) return true;

        // Get previous slayer type
        SlayerType previous = type.getPreviousSlayer();
        if (previous == null) return true;

        // Check if player has reached level 3+ of previous slayer
        long xp = SlayerData.getSlayerXp(playerUuid, previous);
        int level = getLevelForXp(xp);
        
        // Also require at least 1 boss killed of previous slayer type
        int bossesKilled = SlayerData.getBossesKilled(playerUuid, previous);
        
        return level >= 3 && bossesKilled >= 1;
    }

    public static String getUnlockRequirement(SlayerType type) {
        SlayerType previous = type.getPreviousSlayer();
        if (previous == null) return null;
        return "Reach " + previous.displayName + " Bounty Level 3 and kill 1 boss";
    }

    public static Map<String, ActiveQuest> getActiveQuests() {
        return new HashMap<>(activeQuests);
    }

    public static void loadActiveQuests(Map<String, ActiveQuest> quests) {
        activeQuests.clear();
        if (quests != null) {
            activeQuests.putAll(quests);
        }
    }
    // ============================================================
// FORCE SPAWN BOSS (for admin)
// ============================================================
    public static void forceSpawnBoss(ServerPlayerEntity player) {
        ActiveQuest quest = getActiveQuest(player);
        if (quest == null) return;

        // Set kills to required amount to trigger boss
        quest.killCount = quest.getKillsRequired();
        quest.bossSpawned = false;

        // Spawn the boss
        spawnBoss(player, quest);
    }

    // ============================================================
// SPAWN UPGRADED MOB (for admin/testing)
// ============================================================
    public static void spawnUpgradedMob(ServerWorld world, Vec3d pos, SlayerType type) {
        LivingEntity mob = createUpgradedMob(world, type);
        if (mob != null) {
            mob.setPosition(pos.x, pos.y, pos.z);
            if (mob instanceof net.minecraft.entity.mob.MobEntity mobEntity) {
                mobEntity.setPersistent();

            }
            world.spawnEntity(mob);
        }
    }

    private static LivingEntity createUpgradedMob(ServerWorld world, SlayerType type) {
        LivingEntity mob = switch (type) {
            case ZOMBIE -> EntityType.ZOMBIE.create(world, SpawnReason.COMMAND);
            case SPIDER -> EntityType.SPIDER.create(world, SpawnReason.COMMAND);
            case SKELETON -> EntityType.SKELETON.create(world, SpawnReason.COMMAND);
            case SLIME -> EntityType.SLIME.create(world, SpawnReason.COMMAND);
            case ENDERMAN -> EntityType.ENDERMAN.create(world, SpawnReason.COMMAND);
            case IRON_GOLEM -> EntityType.IRON_GOLEM.create(world, SpawnReason.COMMAND);
            case PIGLIN -> EntityType.PIGLIN_BRUTE.create(world, SpawnReason.COMMAND);
        };

        if (mob != null) {
            mob.setCustomName(Text.literal("Upgraded " + type.displayName)
                    .formatted(type.color, Formatting.BOLD));
            mob.setCustomNameVisible(false);

            var healthAttr = mob.getAttributeInstance(EntityAttributes.MAX_HEALTH);
            if (healthAttr != null) {
                healthAttr.setBaseValue(healthAttr.getBaseValue() * 3);
                mob.setHealth(mob.getMaxHealth());
            }
        }
        return mob;
    }
    // Called when slime boss and all its splits are defeated
// Called when slime boss and all its splits are defeated
    public static void completeSlayerQuest(ServerPlayerEntity player, SlayerType type) {
        String uuid = player.getUuidAsString();
        ActiveQuest quest = activeQuests.get(uuid);

        if (quest == null || quest.slayerType != type) return;

        // Get tier config for rewards
        TierConfig config = getTierConfig(quest.tier);
        if (config == null) return;

        // Clean up boss tracking using quest's stored boss UUID
        if (quest.bossEntityUuid != null) {
            bossOwners.remove(quest.bossEntityUuid);
            slayerBossEntities.remove(quest.bossEntityUuid);
            BossAbilityManager.removeBoss(quest.bossEntityUuid);
        }

        // Give XP reward
        long currentXp = SlayerData.getSlayerXp(uuid, type);
        int oldLevel = getLevelForXp(currentXp);
        SlayerData.addSlayerXp(uuid, type, config.xpReward);
        int newLevel = getLevelForXp(currentXp + config.xpReward);

        // Level up check
        if (newLevel > oldLevel) {
            int credits = LEVEL_CREDIT_REWARDS[newLevel - 1];
            CoinManager.giveCoins(player, credits);  // Use CoinManager instead of SlayerData
            player.sendMessage(Text.literal("⬆ LEVEL UP! " + type.displayName + " Bounty Level " + newLevel + "!")
                    .formatted(Formatting.GOLD, Formatting.BOLD), false);
            player.sendMessage(Text.literal("  +" + credits + " Bonus Coins!")
                    .formatted(Formatting.AQUA), false);
        }

        // Success message
        player.sendMessage(Text.literal(""), false);
        player.sendMessage(Text.literal("══════════════════════════════════").formatted(Formatting.GOLD), false);
        player.sendMessage(Text.literal("  ☠ BOUNTY COMPLETE! ☠").formatted(type.color, Formatting.BOLD), false);
        player.sendMessage(Text.literal("══════════════════════════════════").formatted(Formatting.GOLD), false);
        player.sendMessage(Text.literal("  +" + config.xpReward + " " + type.displayName + " XP").formatted(Formatting.GREEN), false);
        player.sendMessage(Text.literal(""), false);

        // Clean up quest
        activeQuests.remove(uuid);

        // Remove boss bar if exists
        ServerBossBar bossBar = bossBars.remove(uuid);
        if (bossBar != null) {
            bossBar.clearPlayers();
        }
    }
    // In SlayerManager.java - add/update this method

    private static void applyBossBuffs(LivingEntity boss, SlayerType type, int tier) {
        float baseHealth = switch (type) {
            case ZOMBIE -> 100.0f;
            case SKELETON -> 80.0f;
            case SPIDER -> 70.0f;
            case SLIME -> 120.0f;
            case ENDERMAN -> 90.0f;
            case IRON_GOLEM -> 250.0f;  // Nerfed from 400
            case PIGLIN -> 100.0f;  // Nerfed from 150
        };

        float baseDamage = switch (type) {
            case ZOMBIE -> 10.0f;
            case SKELETON -> 8.0f;
            case SPIDER -> 6.0f;
            case SLIME -> 8.0f;
            case ENDERMAN -> 12.0f;
            case IRON_GOLEM -> 25.0f;
            case PIGLIN -> 15.0f;
        };

        // Reduced health scaling: 1.0 + (tier * 0.4) instead of 0.6
        float healthMult = 1.0f + (tier * 0.4f);
        healthMult *= PerkManager.getBountyBossHealthMultiplier();  // WEAKENED_QUARRY / HARDENED_QUARRY
        float damageMult = 1.0f + (tier * 0.8f);

        // Apply health - use getAttributeInstance with the registry entry
        var healthAttr = boss.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (healthAttr != null) {
            float finalHealth = baseHealth * healthMult;
            healthAttr.setBaseValue(finalHealth);
            boss.setHealth(finalHealth);
        }

        // Apply damage
        var damageAttr = boss.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE);
        if (damageAttr != null) {
            damageAttr.setBaseValue(baseDamage * damageMult);
        }

        // Knockback resistance
        var kbAttr = boss.getAttributeInstance(EntityAttributes.KNOCKBACK_RESISTANCE);
        if (kbAttr != null) {
            kbAttr.setBaseValue(0.5 + (tier * 0.15));
        }

        // Type-specific buffs
        switch (type) {
            case ZOMBIE -> {
                boss.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, true, false, false));
                boss.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, Integer.MAX_VALUE, tier - 1, true, false, false));
            }
            case SKELETON -> {
                boss.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, Integer.MAX_VALUE, tier, true, false, false));
            }
            case SPIDER -> {
                boss.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, Integer.MAX_VALUE, 1, true, false, false));
                boss.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, Integer.MAX_VALUE, 2, true, false, false));
            }
            case ENDERMAN -> {
                boss.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, Integer.MAX_VALUE, 2, true, false, false));
                boss.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, Integer.MAX_VALUE, tier, true, false, false));
            }
            case IRON_GOLEM -> {
                boss.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, Integer.MAX_VALUE, 1, true, false, false));
                boss.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, Integer.MAX_VALUE, 2, true, false, false));
                boss.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, Integer.MAX_VALUE, 0, true, false, false));
                boss.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, true, false, false));

                var armorAttr = boss.getAttributeInstance(EntityAttributes.ARMOR);
                if (armorAttr != null) {
                    armorAttr.setBaseValue(25.0 + (tier * 5.0));
                }
            }
            case SLIME -> {
                // Slime handled by size
            }
            case PIGLIN -> {
                boss.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, Integer.MAX_VALUE, tier, true, false, false));
                boss.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, true, false, false));
                // Prevent zombification in Overworld
                if (boss instanceof net.minecraft.entity.mob.PiglinBruteEntity piglin) {
                    piglin.setImmuneToZombification(true);
                }
            }
        }

        boss.setGlowing(true);
    }

    // Add these methods to SlayerManager.java

    public static double getCoreDropChance(int tier) {
        return switch (tier) {
            case 1 -> 3.0;
            case 2 -> 7.0;
            case 3 -> 12.0;
            case 4 -> 18.0;
            case 5 -> 25.0;
            default -> 0.0;
        };
    }

    public static double getChunkDropChance(int tier) {
        return switch (tier) {
            case 1 -> 10.0;
            case 2 -> 20.0;
            case 3 -> 35.0;
            case 4 -> 50.0;
            case 5 -> 70.0;
            default -> 0.0;
        };
    }

    public static double getSwordDropChance(int tier) {
        return switch (tier) {
            case 1, 2 -> 0.0;  // No sword drops T1-T2
            case 3 -> 2.0;
            case 4 -> 5.0;
            case 5 -> 10.0;
            default -> 0.0;
        };
    }

    public static double getArmorDropChance(SlayerType type, int tier) {
        if (tier < 4) return 0.0;  // No special armor drops below T4

        double baseChance = switch (tier) {
            case 4 -> 1.0;
            case 5 -> 3.0;
            default -> 0.0;
        };

        // Warden has rarer drops
        if (type == SlayerType.IRON_GOLEM) {
            baseChance *= 0.5;
        }

        return baseChance;
    }

    // ============================================================
    // ADMIN UTILITY METHODS
    // ============================================================

    /**
     * Kill all active bounty bosses - returns count of bosses killed
     */
    public static int killAllBosses() {
        int killed = 0;
        for (ServerWorld world : PoliticalServer.server.getWorlds()) {
            for (UUID bossUuid : new HashSet<>(slayerBossEntities)) {
                net.minecraft.entity.Entity entity = world.getEntity(bossUuid);
                if (entity instanceof LivingEntity living && living.isAlive()) {
                    living.kill(world);
                    killed++;
                }
            }
        }
        return killed;
    }

    /**
     * Get all active boss entries (boss UUID -> player UUID string)
     */
    public static Map<UUID, String> getActiveBosses() {
        return new HashMap<>(bossOwners);
    }
}