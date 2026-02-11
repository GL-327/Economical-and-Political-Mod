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
import net.minecraft.component.DataComponentTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

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
        WARDEN("Warden", "The Sculk Terror", Formatting.DARK_AQUA, Items.SCULK, 25.0);


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
        public final int minLevel;
        public final double damageResistance; // % of non-slayer damage ignored
        public final int miniBossCount;

        public TierConfig(int tier, int killsRequired, double baseHp, double baseDamage,
                          int coinCost, int xpReward, int minLevel,
                          double damageResistance, int miniBossCount) {
            this.tier = tier;
            this.killsRequired = killsRequired;
            this.baseHp = baseHp;
            this.baseDamage = baseDamage;
            this.coinCost = coinCost;
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
            //          tier, kills, baseHP, baseDmg, cost,    xp,  minLvl, dmgResist, miniBosses
            new TierConfig(1,   10,   100,    4,      100,     5,    0,     0.0,       0),   // Was 25
            new TierConfig(2,   20,   500,    8,      500,     25,   1,     0.15,      1),   // Was 50
            new TierConfig(3,   40,  2000,   15,     2000,    100,   3,     0.30,      2),   // Was 100
            new TierConfig(4,   60, 10000,   25,    10000,    500,   5,     0.50,      3),   // Was 150
            new TierConfig(5,  100, 50000,   40,    50000,   1500,   7,     0.65,      4),   // Was 250
    };
    public static int getKillsRequired(SlayerType type, int tier) {
        if (type == SlayerType.WARDEN) {
            return 1; // Wardens are rare, only need 1
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

            // Clean up boss tracking
            if (quest.bossEntityUuid != null) {
                bossOwners.remove(quest.bossEntityUuid);
                slayerBossEntities.remove(quest.bossEntityUuid);
            }

            player.sendMessage(Text.literal("✖ Bounty cancelled. No refund.")
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
            case WARDEN -> entity instanceof WardenEntity;
        };
    }

    // ============================================================
    // BOSS SPAWNING
    // ============================================================

    private static void spawnBoss(ServerPlayerEntity player, ActiveQuest quest) {
        if (quest.bossSpawned) return;

        ServerWorld world = player.getEntityWorld();
        Vec3d pos = new Vec3d(player.getX(), player.getY(), player.getZ());
        TierConfig config = quest.getConfig();
        SlayerType type = quest.slayerType;

        // Find valid spawn position nearby
        BlockPos spawnPos = findSpawnPosition(world, player.getBlockPos(), 5);
        if (spawnPos == null) spawnPos = player.getBlockPos();

        // Create the boss entity
        MobEntity boss = createBossEntity(world, type, config);
        if (boss == null) {
            player.sendMessage(Text.literal("✖ Failed to spawn boss!")
                    .formatted(Formatting.RED), false);
            return;
        }

        // Position and spawn
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
        boss.setCustomNameVisible(true);

        // Add effects for visual flair
        boss.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, Integer.MAX_VALUE, 0, false, false));
        boss.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));

        // NBT tag for identification
        NbtCompound nbt = new NbtCompound();
        nbt.putBoolean("SlayerBoss", true);
        nbt.putString("SlayerType", type.name());
        nbt.putInt("SlayerTier", quest.tier);
        nbt.putString("OwnerUuid", player.getUuidAsString());
        nbt.putDouble("DamageResistance", config.damageResistance);

        // Can't pick up items
        boss.setCanPickUpLoot(false);
        boss.setPersistent();

        // Spawn
        world.spawnEntity(boss);

        // Track the boss
        quest.bossSpawned = true;
        quest.bossAlive = true;
        quest.bossEntityUuid = boss.getUuid();
        bossOwners.put(boss.getUuid(), player.getUuidAsString());
        slayerBossEntities.add(boss.getUuid());

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
        miniBoss.setCustomNameVisible(true);
        miniBoss.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, Integer.MAX_VALUE, 0, false, false));

        world.spawnEntity(miniBoss);

        player.sendMessage(Text.literal("⚠ A " + type.displayName + " Minion has spawned!")
                .formatted(Formatting.GOLD), false);
    }

    private static MobEntity createBossEntity(ServerWorld world, SlayerType type, TierConfig config) {
        return switch (type) {
            case ZOMBIE -> EntityType.ZOMBIE.create(world, SpawnReason.MOB_SUMMONED);
            case SPIDER -> EntityType.SPIDER.create(world, SpawnReason.MOB_SUMMONED);
            case SKELETON -> EntityType.SKELETON.create(world, SpawnReason.MOB_SUMMONED);
            case SLIME -> {
                SlimeEntity slime = EntityType.SLIME.create(world, SpawnReason.MOB_SUMMONED);
                if (slime != null) slime.setSize(Math.min(config.tier + 2, 10), false);
                yield slime;
            }
            case ENDERMAN -> EntityType.ENDERMAN.create(world, SpawnReason.MOB_SUMMONED);
            case WARDEN -> EntityType.WARDEN.create(world, SpawnReason.MOB_SUMMONED);
        };
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
            case WARDEN -> BossBar.Color.BLUE;
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
        boss.setCustomNameVisible(true);
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

    public static void onBossDeath(LivingEntity entity, ServerPlayerEntity killer) {
        UUID bossUuid = entity.getUuid();
        String ownerUuid = bossOwners.get(bossUuid);

        if (ownerUuid == null) return;

        ActiveQuest quest = activeQuests.get(ownerUuid);
        if (quest == null || !bossUuid.equals(quest.bossEntityUuid)) return;

        ServerPlayerEntity owner = PoliticalServer.server.getPlayerManager().getPlayer(UUID.fromString(ownerUuid));
        if (owner == null) return;

        TierConfig config = quest.getConfig();
        SlayerType type = quest.slayerType;

        // Award XP
        long xpGained = config.xpReward;
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

        if (newLevel > oldLevel) {
            for (int lvl = oldLevel + 1; lvl <= newLevel; lvl++) {
                int creditReward = LEVEL_CREDIT_REWARDS[lvl - 1];

                // Give credits instead of coins
                CreditItem.giveCredits(owner, creditReward);

                owner.sendMessage(Text.literal(""), false);
                owner.sendMessage(Text.literal("  ⬆ LEVEL UP! " + type.displayName + " Bounty " + lvl)
                        .formatted(Formatting.YELLOW, Formatting.BOLD), false);
                owner.sendMessage(Text.literal("  +" + creditReward + " credits")
                        .formatted(Formatting.AQUA), false);

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

        // Chunk drop chances (for crafting slayer swords)
        // T1: 2%, T2: 5%, T3: 10%, T4: 15%, T5: 20%
        double chunkChance = 0.02 + (tier - 1) * 0.045;
        if (rand.nextDouble() < chunkChance) {
            drops.add(SlayerItems.createChunk(type));
        }

        // Core drop chances (for legendary items) - ONLY from bosses, not normal mobs
        double coreChance = switch (type) {
            case ZOMBIE, SPIDER, SKELETON -> 0.005 + (tier * 0.005);  // 0.5% - 2.5%
            case SLIME -> 0.004 + (tier * 0.004);                      // 0.4% - 2%
            case ENDERMAN -> 0.0001 + (tier * 0.0001);                 // 0.01% - 0.05%
            case WARDEN -> 0.005 + (tier * 0.001);                     // 0.5% - 1%
        };

        if (rand.nextDouble() < coreChance) {
            drops.add(SlayerItems.createCore(type));
        }

        // Guaranteed coins based on tier
        int coinDrop = tier * 50 * (int) type.difficultyMultiplier;
        CoinManager.giveCoinsQuiet(player, coinDrop);

        return drops;
    }

    private static void cleanupQuest(String playerUuid) {
        ActiveQuest quest = activeQuests.remove(playerUuid);
        if (quest != null && quest.bossEntityUuid != null) {
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
                    slayerBossEntities.remove(quest.bossEntityUuid);
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

        // Check if player completed T3 of previous slayer
        int highestTier = SlayerData.getHighestTier(playerUuid, previous);
        return highestTier >= 3;
    }

    public static String getUnlockRequirement(SlayerType type) {
        SlayerType previous = type.getPreviousSlayer();
        if (previous == null) return null;
        return "Complete " + previous.displayName + " Bounty T3";
    }


    // ============================================================
    // DATA PERSISTENCE HELPERS
    // ============================================================

    public static Map<String, ActiveQuest> getActiveQuests() {
        return new HashMap<>(activeQuests);
    }

    public static void loadActiveQuests(Map<String, ActiveQuest> quests) {
        activeQuests.clear();
        if (quests != null) {
            activeQuests.putAll(quests);
        }
    }
}