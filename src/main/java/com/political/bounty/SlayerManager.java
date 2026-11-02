package com.political.bounty;
import com.political.BossAbilityManager;
import com.political.BuffProgressManager;
import com.political.CivilCraftAchievements;
import com.political.CustomEnchantmentManager;
import com.political.DataManager;
import com.political.PlayerBuffManager;
import com.political.PoliticalServer;



import com.political.economy.CoinManager;
import com.political.government.PerkManager;
import com.political.party.PoliticalPartyManager;
import com.political.stats.HealthScalingManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.BossEvent;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.skeleton.Stray;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.monster.piglin.Piglin;
import com.political.ArmourAttribute;

import java.util.*;

public class SlayerManager {

    // ============================================================
    // SLAYER TYPES - Ordered by difficulty progression
    // ============================================================
    public enum SlayerType {
        ZOMBIE("Zombie", "The Undying Outlaw", ChatFormatting.DARK_GREEN, Items.ROTTEN_FLESH, 1.0),
        SPIDER("Spider", "The Venomous Bandit", ChatFormatting.DARK_RED, Items.SPIDER_EYE, 1.8),
        SKELETON("Skeleton", "The Bone Desperado", ChatFormatting.WHITE, Items.BONE, 3.0),
        SLIME("Slime", "The Gelatinous Rustler", ChatFormatting.GREEN, Items.SLIME_BALL, 5.0),
        ENDERMAN("Enderman", "The Void Phantom", ChatFormatting.DARK_PURPLE, Items.ENDER_PEARL, 10.0),
        PIGLIN("Piglin", "The Gilded Ravager", ChatFormatting.GOLD, Items.GOLD_NUGGET, 15.0),
        IRON_GOLEM("Warden", "The Deep Dark Sentinel", ChatFormatting.DARK_PURPLE, Items.SCULK_CATALYST, 25.0);


        public final String displayName;
        public final String bossName;
        public final ChatFormatting color;
        public final net.minecraft.world.item.Item icon;
        public final double difficultyMultiplier;

        SlayerType(String displayName, String bossName, ChatFormatting color,
                   net.minecraft.world.item.Item icon, double difficultyMultiplier) {
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

    /**
     * Multiplier on rare bounty loot (chunks, cores, type boss items, attribute tokens, crown).
     * Flesh and coin payouts are unchanged.
     */
    private static final double RARE_BOUNTY_DROP_SCALE = 0.5;

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
            5_000,         // Level 1
            15_000,        // Level 2
            50_000,        // Level 3
            150_000,       // Level 4
            500_000,       // Level 5
            1_000_000,     // Level 6
            2_500_000,     // Level 7
            5_000_000,     // Level 8
            10_000_000,    // Level 9
            25_000_000,    // Level 10
            50_000_000,    // Level 11
            100_000_000,   // Level 12
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
    private static final Map<String, ServerBossEvent> bossBars = new HashMap<>();

    // Slayer boss entities tracked for damage resistance
    private static final Set<UUID> slayerBossEntities = new HashSet<>();

    // ============================================================
    // QUEST MANAGEMENT (continued)
    // ============================================================
// Add to SlayerManager.java

    /**
     * Show boss spawn progress in action bar
     */
    public static void showSpawnProgress(ServerPlayer player) {
        ActiveQuest quest = activeQuests.get(player.getUUID().toString());
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

        player.sendSystemMessage(Component.literal(message), true);
    }

    /**
     * Called when a mob is killed - show progress update
     */


    /**
     * Force check and spawn boss if ready
     */
    public static void checkBossSpawn(ServerPlayer player) {
        ActiveQuest quest = activeQuests.get(player.getUUID().toString());
        if (quest == null || quest.bossSpawned) return;

        if (quest.killCount >= quest.getKillsRequired()) {
            spawnBoss(player, quest);
        }
    }

    public static boolean startQuest(ServerPlayer player, SlayerType type, int tier) {
        String uuid = player.getUUID().toString();

        if (!hasUnlockedSlayer(uuid, type)) {
            String req = getUnlockRequirement(type);
            player.sendSystemMessage(Component.literal("✖ " + req + " first!")
                    .withStyle(ChatFormatting.RED), false);
            return false;
        }
        // Check if already has active quest
        if (activeQuests.containsKey(uuid)) {
            player.sendSystemMessage(Component.literal("✖ You already have an active bounty!")
                    .withStyle(ChatFormatting.RED), false);
            return false;
        }

        // Validate tier
        TierConfig config = getTierConfig(tier);
        if (config == null) {
            player.sendSystemMessage(Component.literal("✖ Invalid tier!")
                    .withStyle(ChatFormatting.RED), false);
            return false;
        }

        // Check level requirement
        long playerXp = SlayerData.getSlayerXp(uuid, type);
        int playerLevel = getLevelForXp(playerXp);
        if (playerLevel < config.minLevel) {
            player.sendSystemMessage(Component.literal("✖ Requires " + type.displayName + " Bounty Level " + config.minLevel + "!")
                    .withStyle(ChatFormatting.RED), false);
            return false;
        }

        // Check coin cost
        if (!CoinManager.hasCoins(player, config.coinCost)) {
            player.sendSystemMessage(Component.literal("✖ Not enough coins! Need " + config.coinCost + " coins.")
                    .withStyle(ChatFormatting.RED), false);
            return false;
        }

        // Deduct coins
        CoinManager.removeCoins(player, config.coinCost);

        // Create quest
        ActiveQuest quest = new ActiveQuest(uuid, type, tier);
        activeQuests.put(uuid, quest);

        // Notify player
        player.sendSystemMessage(Component.literal(""), false);
        player.sendSystemMessage(Component.literal("══════════════════════════════")
                .withStyle(ChatFormatting.GOLD), false);
        player.sendSystemMessage(Component.literal("  🎯 BOUNTY ACCEPTED 🎯")
                .withStyle(type.color, ChatFormatting.BOLD), false);
        player.sendSystemMessage(Component.literal("══════════════════════════════")
                .withStyle(ChatFormatting.GOLD), false);
        player.sendSystemMessage(Component.literal("  Target: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(type.displayName).withStyle(type.color)), false);
        player.sendSystemMessage(Component.literal("  Tier: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(tier)).withStyle(ChatFormatting.YELLOW)), false);
        player.sendSystemMessage(Component.literal("  Kills Required: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(getKillsRequired(type, tier))).withStyle(ChatFormatting.WHITE)), false);
        player.sendSystemMessage(Component.literal(""), false);
        player.sendSystemMessage(Component.literal("  Hunt down " + type.displayName + "s to summon the target!")
                .withStyle(ChatFormatting.GREEN), false);
        player.sendSystemMessage(Component.literal("══════════════════════════════")
                .withStyle(ChatFormatting.GOLD), false);
        player.sendSystemMessage(Component.literal("§8[Charadrius Protocol engaged]"), false);

        // Save data
        DataManager.save(PoliticalServer.server);

        return true;
    }

    public static void cancelQuest(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        ActiveQuest quest = activeQuests.remove(uuid);

        if (quest != null) {
            // Clean up boss bar
            ServerBossEvent bossBar = bossBars.remove(uuid);
            if (bossBar != null) {
                bossBar.removeAllPlayers();
            }

            // *** FIX: DESPAWN THE BOSS ENTITY ***
            if (quest.bossEntityUuid != null) {
                // Find and remove the boss from all worlds
                for (ServerLevel world : PoliticalServer.server.getAllLevels()) {
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

            player.sendSystemMessage(Component.literal("✖ Bounty cancelled. Boss despawned. No refund.")
                    .withStyle(ChatFormatting.RED), false);
        }
    }

    public static ActiveQuest getActiveQuest(ServerPlayer player) {
        return activeQuests.get(player.getUUID().toString());
    }

    public static ActiveQuest getActiveQuest(String uuid) {
        return activeQuests.get(uuid);
    }

    public static boolean hasActiveQuest(ServerPlayer player) {
        return activeQuests.containsKey(player.getUUID().toString());
    }

    // ============================================================
    // KILL TRACKING
    // ============================================================

    public static void onMobKill(ServerPlayer player, LivingEntity killed) {
        String uuid = player.getUUID().toString();
        ActiveQuest quest = activeQuests.get(uuid);

        if (quest == null || quest.bossSpawned) return;

        // Check if killed mob matches quest type
        if (!isMatchingMob(killed, quest.slayerType)) return;

        // Calculate kill value
        int killValue = 1;

        // Slayer sword bonus: 2x kills
        ItemStack weapon = player.getMainHandItem();
        if (SlayerItems.isSlayerSword(weapon)) {
            SlayerType swordType = SlayerItems.getSwordSlayerType(weapon);
            if (swordType == quest.slayerType) {
                killValue = 2;
            }
        }

        // Scaled mob bonus
        if (HealthScalingManager.isScaledMob(killed.getUUID())) {
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
        player.sendSystemMessage(Component.literal(displayText), true);

        // Bonus kill notification
        if (killValue > 1) {
            player.sendSystemMessage(Component.literal("§a+" + killValue + " kills!"), true);
        }

        // Check if ready for boss
        if (quest.killCount >= required && !quest.bossSpawned) {
            player.sendSystemMessage(Component.literal(""), false);
            player.sendSystemMessage(Component.literal("§4§l☠ TARGET INCOMING! ☠")
                    .withStyle(ChatFormatting.BOLD), false);
            player.sendSystemMessage(Component.literal("§c" + quest.slayerType.bossName + " is approaching...")
                    , false);
            player.sendSystemMessage(Component.literal(""), false);

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

    public static void spawnBoss(ServerLevel world, ServerPlayer player, SlayerType type, int tier) {
        // Your spawn logic here
        // Create and spawn the boss entity based on type and tier
    }

    public static void adminSpawnBoss(ServerPlayer player, SlayerType type, int tier) {
        ServerLevel world = (ServerLevel) player.level();
        spawnBoss(world, player, type, tier);
    }

    private static boolean isMatchingMob(LivingEntity entity, SlayerType type) {
        return switch (type) {
            case ZOMBIE -> entity instanceof Zombie;
            case SPIDER -> entity instanceof Spider;
            case SKELETON -> entity instanceof Skeleton || entity instanceof Stray
                    || entity instanceof WitherSkeleton;
            case SLIME -> entity instanceof Slime || entity instanceof MagmaCube;
            case ENDERMAN -> entity instanceof EnderMan;
            case IRON_GOLEM -> entity instanceof Warden;
            case PIGLIN -> entity instanceof PiglinBrute || entity instanceof Piglin;
        };
    }

    // ============================================================
    // BOSS SPAWNING
    // ============================================================
    public static void addHiddenEffect(LivingEntity entity, Holder<MobEffect> effect,
                                       int duration, int amplifier) {
        entity.addEffect(new MobEffectInstance(
                effect,
                duration,
                amplifier,
                true,   // ambient - reduces particles
                false,  // showParticles
                false   // showIcon - hides from HUD
        ));
    }

    private static void spawnBoss(ServerPlayer player, ActiveQuest quest) {
        if (quest.bossSpawned) return;

        ServerLevel world = (ServerLevel) player.level();
        TierConfig config = quest.getConfig();
        SlayerType type = quest.slayerType;

        // Find valid spawn position nearby
        BlockPos spawnPos = player.blockPosition();

        Mob boss;

        // Spawn actual Warden entity
        if (type == SlayerType.IRON_GOLEM) {
            boss = (Mob) EntityType.IRON_GOLEM.create(world, EntitySpawnReason.MOB_SUMMONED);
            if (boss == null) {
                player.sendSystemMessage(Component.literal("✖ Failed to spawn boss!")
                        .withStyle(ChatFormatting.RED), false);
                return;
            }
            spawnPos = player.blockPosition().above(2); // Spawn 2 blocks above
            // Fix: Prevent Warden from going underground
            if (boss instanceof Warden warden) {
                warden.setPersistenceRequired();
                // Force surface-only behavior
                warden.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 0, false, false));
                warden.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 1, 0, false, false));
            }
        } else {
            // Create boss entity for other types
            boss = createBossEntity(world, type, config);
            if (boss == null) {
                player.sendSystemMessage(Component.literal("✖ Failed to spawn boss!")
                        .withStyle(ChatFormatting.RED), false);
                return;
            }
        }

        // Initialize slime boss tracking BEFORE spawning
        if (type == SlayerType.SLIME) {
            BossAbilityManager.initSlimeBoss(boss.getUUID());
        }

        // Position the boss
        boss.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);

        // Apply boss stats
        double actualHp = config.getActualHp(type);
        double actualDamage = config.getActualDamage(type);

        // Set max health
        var healthAttr = boss.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.setBaseValue(actualHp);
            boss.setHealth((float) actualHp);
        }

        // Set damage
        var damageAttr = boss.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageAttr != null) {
            damageAttr.setBaseValue(actualDamage);
        }

        // Set custom name
        String tierRoman = toRoman(quest.tier);
        boss.setCustomName(Component.literal(type.bossName + " " + tierRoman)
                .withStyle(type.color, ChatFormatting.BOLD));
        boss.setCustomNameVisible(false);

        // Add effects for visual flair
        boss.addEffect(new MobEffectInstance(MobEffects.GLOWING, Integer.MAX_VALUE, 0, false, false));
        boss.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));

        // Can't pick up items
        boss.setCanPickUpLoot(false);
        boss.setPersistenceRequired();

        // Apply type-specific buffs (status effects, knockback resistance, armor, etc.)
        applyBossBuffs(boss, type, quest.tier);

        // **CRITICAL: Actually spawn the boss into the world**
        world.addFreshEntity(boss);

        // Make Warden aggressive toward the player
        if (type == SlayerType.IRON_GOLEM && boss instanceof Warden warden) {
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
        quest.bossEntityUuid = boss.getUUID();
        bossOwners.put(boss.getUUID(), player.getUUID().toString());
        slayerBossEntities.add(boss.getUUID());

        // Register with BossAbilityManager
        BossAbilityManager.registerBoss(boss, type, quest.tier);

        // Create boss bar
        ServerBossEvent bossBar = new ServerBossEvent(
                UUID.randomUUID(),
                Component.literal(type.bossName + " " + tierRoman).withStyle(type.color, ChatFormatting.BOLD),
                getBossBarColor(type),
                BossEvent.BossBarOverlay.NOTCHED_10
        );
        bossBar.addPlayer(player);
        bossBar.setProgress(1.0f);
        bossBars.put(player.getUUID().toString(), bossBar);

        // Announce
        player.sendSystemMessage(Component.literal(""), false);
        player.sendSystemMessage(Component.literal("☠☠☠ " + type.bossName.toUpperCase() + " HAS SPAWNED! ☠☠☠")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), false);
        player.sendSystemMessage(Component.literal("HP: " + String.format("%,.0f", actualHp) + " ❤")
                .withStyle(ChatFormatting.RED), false);
        player.sendSystemMessage(Component.literal(""), false);

        // 20% chance Charadrius MFLUX alert
        if (world.getRandom().nextFloat() < 0.20f) {
            player.sendSystemMessage(Component.literal("§8[MFLUX Alert: Charadrius subject detected in your area]"), false);
        }

        // Sound effect
        world.playSound(null, player.blockPosition(),
                net.minecraft.sounds.SoundEvents.WITHER_SPAWN,
                net.minecraft.sounds.SoundSource.HOSTILE, 1.0f, 0.5f);
    }

    private static void spawnMiniBoss(ServerPlayer player, ActiveQuest quest) {
        ServerLevel world = (ServerLevel) player.level();
        TierConfig config = quest.getConfig();
        SlayerType type = quest.slayerType;

        BlockPos spawnPos = findSpawnPosition(world, player.blockPosition(), 8);
        if (spawnPos == null) return;

        Mob miniBoss = createBossEntity(world, type, config);
        if (miniBoss == null) return;

        miniBoss.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);

        // Mini-boss has 5% of main boss HP
        double hp = config.getActualHp(type) * 0.05;
        var healthAttr = miniBoss.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.setBaseValue(hp);
            miniBoss.setHealth((float) hp);
        }

        miniBoss.setCustomName(Component.literal("✦ " + type.displayName + " Minion ✦")
                .withStyle(type.color));
        miniBoss.setCustomNameVisible(false);
        miniBoss.addEffect(new MobEffectInstance(MobEffects.GLOWING, Integer.MAX_VALUE, 0, false, false));

        world.addFreshEntity(miniBoss);

        player.sendSystemMessage(Component.literal("⚠ A " + type.displayName + " Minion has spawned!")
                .withStyle(ChatFormatting.GOLD), false);
    }

    private static Mob createBossEntity(ServerLevel world, SlayerType type, TierConfig config) {
        EntityType<?> entityType = switch (type) {
            case ZOMBIE -> EntityType.ZOMBIE;
            case SPIDER -> EntityType.SPIDER;
            case SKELETON -> EntityType.SKELETON;
            case SLIME -> EntityType.SLIME;
            case ENDERMAN -> EntityType.ENDERMAN;
            case IRON_GOLEM -> EntityType.IRON_GOLEM;
            case PIGLIN -> EntityType.PIGLIN_BRUTE;
        };

        Mob mob = (Mob) entityType.create(world, EntitySpawnReason.MOB_SUMMONED);

        // Set slime size to max (4) for Slime bosses
        if (type == SlayerType.SLIME && mob instanceof net.minecraft.world.entity.monster.Slime slime) {
            slime.setSize(4, true);
        }

        return mob;
    }

    private static BlockPos findSpawnPosition(ServerLevel world, BlockPos center, int radius) {
        Random rand = new Random();
        for (int i = 0; i < 10; i++) {
            int x = center.getX() + rand.nextInt(radius * 2) - radius;
            int z = center.getZ() + rand.nextInt(radius * 2) - radius;
            BlockPos check = new BlockPos(x, center.getY(), z);

            // Find ground level
            while (check.getY() > world.getMinY() && !world.getBlockState(check.below()).isSolidRender()) {
                check = check.below();
            }
            while (check.getY() < world.getMaxY() && world.getBlockState(check).isSolidRender()) {
                check = check.above();
            }

            // Check if valid spawn (2 blocks of air)
            if (!world.getBlockState(check).isSolidRender() &&
                    !world.getBlockState(check.above()).isSolidRender()) {
                return check;
            }
        }
        return null;
    }

    private static BossEvent.BossBarColor getBossBarColor(SlayerType type) {
        return switch (type) {
            case ZOMBIE -> BossEvent.BossBarColor.GREEN;
            case SPIDER -> BossEvent.BossBarColor.RED;
            case SKELETON -> BossEvent.BossBarColor.WHITE;
            case SLIME -> BossEvent.BossBarColor.GREEN;
            case ENDERMAN -> BossEvent.BossBarColor.PURPLE;
            case IRON_GOLEM -> BossEvent.BossBarColor.BLUE;
            case PIGLIN -> BossEvent.BossBarColor.YELLOW;
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

    public static void spawnTestBoss(ServerPlayer player, SlayerType type, int tier) {
        ServerLevel world = (ServerLevel) player.level();
        TierConfig config = getTierConfig(tier);
        if (config == null) return;

        BlockPos spawnPos = findSpawnPosition(world, player.blockPosition(), 5);
        if (spawnPos == null) spawnPos = player.blockPosition();

        Mob boss = createBossEntity(world, type, config);
        if (boss == null) {
            player.sendSystemMessage(Component.literal("✖ Failed to spawn boss!").withStyle(ChatFormatting.RED), false);
            return;
        }

        boss.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);

        double actualHp = config.getActualHp(type);
        double actualDamage = config.getActualDamage(type);

        var healthAttr = boss.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.setBaseValue(actualHp);
            boss.setHealth((float) actualHp);
        }

        var damageAttr = boss.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageAttr != null) {
            damageAttr.setBaseValue(actualDamage);
        }

        String tierRoman = toRoman(tier);
        boss.setCustomName(Component.literal("[TEST] " + type.bossName + " " + tierRoman)
                .withStyle(type.color, ChatFormatting.BOLD));
        boss.setCustomNameVisible(false);
        boss.setPersistenceRequired();

        world.addFreshEntity(boss);

        world.playSound(null, player.blockPosition(),
                net.minecraft.sounds.SoundEvents.WITHER_SPAWN,
                net.minecraft.sounds.SoundSource.HOSTILE, 1.0f, 0.5f);
    }

    public static double getLevelDamageMultiplier(ServerPlayer player, SlayerType bossType) {
        int level = SlayerData.getSlayerLevel(player.getUUID().toString(), bossType);
        return 1.0 + (level * DAMAGE_BONUS_PER_LEVEL);
    }
    public static double getLevelDamageReduction(ServerPlayer player, SlayerType bossType) {
        int level = SlayerData.getSlayerLevel(player.getUUID().toString(), bossType);
        return level * DAMAGE_REDUCTION_PER_LEVEL; // Returns 0.0 to 0.18
    }

    public static final double DAMAGE_BONUS_PER_LEVEL = 0.02;
    public static final double DAMAGE_REDUCTION_PER_LEVEL = 0.015;
    public static void markAsSlayerBoss(UUID uuid) {
        slayerBossEntities.add(uuid);
    }
    public static void onBossDeath(LivingEntity entity, ServerPlayer killer) {
        UUID bossUuid = entity.getUUID();
        String ownerUuid = bossOwners.get(bossUuid);
        if (ownerUuid == null) return;

        // Use a different variable name to avoid conflict
        ActiveQuest bossQuest = activeQuests.get(ownerUuid);
        if (bossQuest == null) return;

        SlayerType bossType = bossQuest.slayerType;
        int tier = bossQuest.tier;

        if (bossType == SlayerType.SLIME) {
            // Let BossAbilityManager handle slime splitting
            ServerLevel world = (ServerLevel) entity.level();
            BossAbilityManager.onSlimeBossDeath(entity, world, killer, tier);
            return; // Don't complete quest yet - onSlimeBossDeath handles it
        }

        ActiveQuest quest = activeQuests.get(ownerUuid);
        if (quest == null || !bossUuid.equals(quest.bossEntityUuid)) return;

        ServerPlayer owner = PoliticalServer.server.getPlayerList().getPlayer(UUID.fromString(ownerUuid));
        if (owner == null) return;

        TierConfig config = quest.getConfig();
        SlayerType type = quest.slayerType;

        // Award XP (with Prospector bonus + perk multiplier)
        long baseXp = config.xpReward;
        int prospectorLevel = CustomEnchantmentManager.getLevel(owner.getMainHandItem(), CustomEnchantmentManager.PROSPECTOR);
        float xpMultiplier = 1.0f + (0.10f * prospectorLevel);
        xpMultiplier *= PerkManager.getBountyXpMultiplier();
        long xpGained = (long)(baseXp * xpMultiplier);
        long oldXp = SlayerData.getSlayerXp(ownerUuid, type);
        int oldLevel = getLevelForXp(oldXp);

        SlayerData.addSlayerXp(ownerUuid, type, xpGained);
        SlayerData.updateHighestTier(ownerUuid, type, quest.tier);
        SlayerData.incrementBossesKilled(ownerUuid, type);

        BuffProgressManager.onBountyQuestComplete(owner);
        BuffProgressManager.onBossKill(owner, tier, type.name());
        CivilCraftAchievements.trackBountyCompleted(owner);

        long newXp = SlayerData.getSlayerXp(ownerUuid, type);
        int newLevel = getLevelForXp(newXp);

        // Victory message
        owner.sendSystemMessage(Component.literal(""), false);
        owner.sendSystemMessage(Component.literal("══════════════════════════════")
                .withStyle(ChatFormatting.GOLD), false);
        owner.sendSystemMessage(Component.literal("  ✔ Bounty Completed!")
                .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), false);
        owner.sendSystemMessage(Component.literal("══════════════════════════════")
                .withStyle(ChatFormatting.GOLD), false);
        owner.sendSystemMessage(Component.literal("  +" + xpGained + " " + type.displayName + " Bounty XP")
                .withStyle(ChatFormatting.AQUA), false);
        // 20% chance of MFLUX recontainment message
        if (PoliticalServer.server.overworld().getRandom().nextFloat() < 0.20f) {
            owner.sendSystemMessage(Component.literal("§8[MFLUX: Subject successfully recontained. Data logged.]"), false);
        }

        if (newLevel > oldLevel) {
            for (int lvl = oldLevel + 1; lvl <= newLevel; lvl++) {
                int creditReward = LEVEL_CREDIT_REWARDS[lvl - 1];

                // Give coins as level-up reward
                CoinManager.giveCoins(owner, creditReward);

                owner.sendSystemMessage(Component.literal(""), false);
                owner.sendSystemMessage(Component.literal("  ⬆ LEVEL UP! " + type.displayName + " Bounty " + lvl)
                        .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD), false);
                owner.sendSystemMessage(Component.literal("  +" + creditReward + " coins")
                        .withStyle(ChatFormatting.GOLD), false);

                // Sound effect
                owner.level().playSound(null, owner.blockPosition(),
                        net.minecraft.sounds.SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                        net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);
            }
        }

        // Roll for drops
        List<ItemStack> drops = rollDrops(type, quest.tier, owner);
        for (ItemStack drop : drops) {
            owner.sendSystemMessage(Component.literal("  ✦ RARE DROP: ")
                    .withStyle(ChatFormatting.LIGHT_PURPLE)
                    .append(drop.getHoverName().copy().withStyle(ChatFormatting.GOLD)), false);

            if (!owner.addItem(drop)) {
                owner.drop(drop, false);
            }
        }

        owner.sendSystemMessage(Component.literal("══════════════════════════════")
                .withStyle(ChatFormatting.GOLD), false);

        // Cleanup
        cleanupQuest(ownerUuid);
        DataManager.save(PoliticalServer.server);
    }

    private static List<ItemStack> rollDrops(SlayerType type, int tier, ServerPlayer player) {
        List<ItemStack> drops = new ArrayList<>();
        Random rand = new Random();
        String playerUuid = player.getUUID().toString();
        
        // Get buff bonuses for drop rates
        double bossDropBonus = com.political.PlayerBuffManager.getBossDropBonus(playerUuid);
        double rareDropBonus = com.political.PlayerBuffManager.getRareDropBonus(playerUuid);

        // Chunk drop chances (scaled by RARE_BOUNTY_DROP_SCALE; before perks/buffs)
        // Base curve T1–T5 then halved: ~0.5%–~4%
        double chunkChance = (0.01 + (tier - 1) * 0.0175) * RARE_BOUNTY_DROP_SCALE;
        chunkChance *= PerkManager.getBountyDropMultiplier();
        chunkChance *= (1.0 + bossDropBonus + rareDropBonus); // BOSS_SLAYER and LUCKY buffs
        if (rand.nextDouble() < chunkChance) {
            drops.add(SlayerItems.createChunk(type));
        }

        double coreChance = switch (type) {
            case ZOMBIE, SPIDER, SKELETON -> 0.001 + (tier * 0.002);
            case SLIME -> 0.0008 + (tier * 0.0015);
            case ENDERMAN -> 0.00005 + (tier * 0.00005);
            case PIGLIN -> 0.002 + (tier * 0.001);
            case IRON_GOLEM -> 0.0001 + (tier * 0.0001);
        };
        coreChance *= RARE_BOUNTY_DROP_SCALE;
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
                player.sendSystemMessage(Component.literal("✦ Rotten Flesh dropped! x" + fleshCount).withStyle(ChatFormatting.DARK_GREEN), false);
            } else if (type == SlayerType.PIGLIN) {
                // Piglin bosses drop Piglin Flesh
                drops.add(SlayerItems.createPiglinFlesh());
                player.sendSystemMessage(Component.literal("✦ Piglin Flesh dropped!").withStyle(ChatFormatting.YELLOW), false);
            }
        }

        // Voidwalker's Crown drop (for ENDERMAN type, T4+)
        double enderHelmetChance = getArmorDropChance(type, tier);
        if (type == SlayerType.ENDERMAN && enderHelmetChance > 0 && rand.nextDouble() * 100 < enderHelmetChance) {
            drops.add(SlayerItems.createVoidwalkerCrown());
        }

        double bossDropChance = (0.05 + (tier * 0.02)) * RARE_BOUNTY_DROP_SCALE;
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

        if (tier >= 3) {
            double tokenChance = (0.02 + (tier - 3) * 0.01) * RARE_BOUNTY_DROP_SCALE;
            tokenChance *= PerkManager.getBountyDropMultiplier();
            
            if (rand.nextDouble() < tokenChance) {
                // Drop a random attribute token based on slayer type
                ArmourAttribute tokenAttr = getRandomAttributeForSlayerType(type, rand);
                if (tokenAttr != null) {
                    drops.add(SlayerItems.createAttributeToken(tokenAttr));
                    player.sendSystemMessage(Component.literal("✦ " + tokenAttr.displayName + " Attribute Token dropped!")
                            .withStyle(tokenAttr.color, ChatFormatting.BOLD), false);
                }
            }
        }

        // Coins based on tier (with SLAYER_TOLL + BOUNTY_TAX perks applied)
        int baseCoinDrop = tier * 25 * (int) type.difficultyMultiplier;
        baseCoinDrop = (int) (baseCoinDrop * PerkManager.getBountyCoinPayoutMultiplier());
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

        ServerBossEvent bossBar = bossBars.remove(playerUuid);
        if (bossBar != null) {
            bossBar.removeAllPlayers();
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
            for (ServerLevel world : server.getAllLevels()) {
                var entity = world.getEntity(quest.bossEntityUuid);
                if (entity instanceof LivingEntity living) {
                    boss = living;
                    break;
                }
            }
            if (server.getTickCount() % 20 == 0) {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    // Use different variable name to avoid conflict
                    ActiveQuest activeQuest = activeQuests.get(player.getUUID().toString());
                    if (activeQuest != null && !activeQuest.bossSpawned) {
                        showSpawnProgress(player);
                    }
                }
            }
            // Update boss bar
            ServerBossEvent bossBar = bossBars.get(playerUuid);
            if (boss != null && boss.isAlive()) {
                if (bossBar != null) {
                    bossBar.setProgress(boss.getHealth() / boss.getMaxHealth());
                }
            } else if (boss == null || !boss.isAlive()) {
                // Boss died or despawned
                quest.bossAlive = false;

                if (bossBar != null) {
                    bossBar.removeAllPlayers();
                    bossBars.remove(playerUuid);
                }

                // If they killed it legitimately, onBossDeath handles cleanup
                // This handles edge cases like boss despawning
                ServerPlayer player = server.getPlayerList().getPlayer(UUID.fromString(playerUuid));
                if (player != null && boss == null) {
                    player.sendSystemMessage(Component.literal("✖ Your bounty has escaped! Quest failed.")
                            .withStyle(ChatFormatting.RED), false);
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
    public static void forceSpawnBoss(ServerPlayer player) {
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
    public static void spawnUpgradedMob(ServerLevel world, Vec3 pos, SlayerType type) {
        LivingEntity mob = createUpgradedMob(world, type);
        if (mob != null) {
            mob.setPos(pos.x, pos.y, pos.z);
            if (mob instanceof net.minecraft.world.entity.Mob mobEntity) {
                mobEntity.setPersistenceRequired();

            }
            world.addFreshEntity(mob);
        }
    }

    private static LivingEntity createUpgradedMob(ServerLevel world, SlayerType type) {
        LivingEntity mob = switch (type) {
            case ZOMBIE -> EntityType.ZOMBIE.create(world, EntitySpawnReason.COMMAND);
            case SPIDER -> EntityType.SPIDER.create(world, EntitySpawnReason.COMMAND);
            case SKELETON -> EntityType.SKELETON.create(world, EntitySpawnReason.COMMAND);
            case SLIME -> EntityType.SLIME.create(world, EntitySpawnReason.COMMAND);
            case ENDERMAN -> EntityType.ENDERMAN.create(world, EntitySpawnReason.COMMAND);
            case IRON_GOLEM -> EntityType.IRON_GOLEM.create(world, EntitySpawnReason.COMMAND);
            case PIGLIN -> EntityType.PIGLIN_BRUTE.create(world, EntitySpawnReason.COMMAND);
        };

        if (mob != null) {
            mob.setCustomName(Component.literal("Upgraded " + type.displayName)
                    .withStyle(type.color, ChatFormatting.BOLD));
            mob.setCustomNameVisible(false);

            var healthAttr = mob.getAttribute(Attributes.MAX_HEALTH);
            if (healthAttr != null) {
                healthAttr.setBaseValue(healthAttr.getBaseValue() * 3);
                mob.setHealth(mob.getMaxHealth());
            }
        }
        return mob;
    }
    // Called when slime boss and all its splits are defeated
// Called when slime boss and all its splits are defeated
    public static void completeSlayerQuest(ServerPlayer player, SlayerType type) {
        String uuid = player.getUUID().toString();
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
            player.sendSystemMessage(Component.literal("⬆ LEVEL UP! " + type.displayName + " Bounty Level " + newLevel + "!")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
            player.sendSystemMessage(Component.literal("  +" + credits + " Bonus Coins!")
                    .withStyle(ChatFormatting.AQUA), false);
        }

        // Success message
        player.sendSystemMessage(Component.literal(""), false);
        player.sendSystemMessage(Component.literal("══════════════════════════════════").withStyle(ChatFormatting.GOLD), false);
        player.sendSystemMessage(Component.literal("  ☠ BOUNTY COMPLETE! ☠").withStyle(type.color, ChatFormatting.BOLD), false);
        player.sendSystemMessage(Component.literal("══════════════════════════════════").withStyle(ChatFormatting.GOLD), false);
        player.sendSystemMessage(Component.literal("  +" + config.xpReward + " " + type.displayName + " XP").withStyle(ChatFormatting.GREEN), false);
        player.sendSystemMessage(Component.literal(""), false);

        PoliticalPartyManager.progressPartyCommission(player, "COMPLETE_BOUNTY", 1);

        // Clean up quest
        activeQuests.remove(uuid);

        // Remove boss bar if exists
        ServerBossEvent bossBar = bossBars.remove(uuid);
        if (bossBar != null) {
            bossBar.removeAllPlayers();
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

        // Apply health - use getAttribute with the registry entry
        var healthAttr = boss.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            float finalHealth = baseHealth * healthMult;
            healthAttr.setBaseValue(finalHealth);
            boss.setHealth(finalHealth);
        }

        // Apply damage
        var damageAttr = boss.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageAttr != null) {
            damageAttr.setBaseValue(baseDamage * damageMult);
        }

        // Knockback resistance
        var kbAttr = boss.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (kbAttr != null) {
            kbAttr.setBaseValue(0.5 + (tier * 0.15));
        }

        // Type-specific buffs
        switch (type) {
            case ZOMBIE -> {
                boss.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, true, false, false));
                boss.addEffect(new MobEffectInstance(MobEffects.STRENGTH, Integer.MAX_VALUE, tier - 1, true, false, false));
            }
            case SKELETON -> {
                boss.addEffect(new MobEffectInstance(MobEffects.SPEED, Integer.MAX_VALUE, tier, true, false, false));
            }
            case SPIDER -> {
                boss.addEffect(new MobEffectInstance(MobEffects.SPEED, Integer.MAX_VALUE, 1, true, false, false));
                boss.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, Integer.MAX_VALUE, 2, true, false, false));
            }
            case ENDERMAN -> {
                boss.addEffect(new MobEffectInstance(MobEffects.SPEED, Integer.MAX_VALUE, 2, true, false, false));
                boss.addEffect(new MobEffectInstance(MobEffects.STRENGTH, Integer.MAX_VALUE, tier, true, false, false));
            }
            case IRON_GOLEM -> {
                boss.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, Integer.MAX_VALUE, 1, true, false, false));
                boss.addEffect(new MobEffectInstance(MobEffects.STRENGTH, Integer.MAX_VALUE, 2, true, false, false));
                boss.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, Integer.MAX_VALUE, 0, true, false, false));
                boss.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, true, false, false));

                var armorAttr = boss.getAttribute(Attributes.ARMOR);
                if (armorAttr != null) {
                    armorAttr.setBaseValue(25.0 + (tier * 5.0));
                }
            }
            case SLIME -> {
                // Slime handled by size
            }
            case PIGLIN -> {
                boss.addEffect(new MobEffectInstance(MobEffects.STRENGTH, Integer.MAX_VALUE, tier, true, false, false));
                boss.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, true, false, false));
                // Prevent zombification in Overworld
                if (boss instanceof PiglinBrute piglin) {
                    piglin.setImmuneToZombification(true);
                }
            }
        }
    }

    // --- Bounty GUI: base % matching {@link #rollDrops} (before Chair perks / Boss Slayer / Lucky) ---

    public static double getBountyTableChunkPercent(int tier) {
        return 100.0 * (0.01 + (tier - 1) * 0.0175) * RARE_BOUNTY_DROP_SCALE;
    }

    public static double getBountyTableCorePercent(SlayerType type, int tier) {
        double c = switch (type) {
            case ZOMBIE, SPIDER, SKELETON -> 0.001 + (tier * 0.002);
            case SLIME -> 0.0008 + (tier * 0.0015);
            case ENDERMAN -> 0.00005 + (tier * 0.00005);
            case PIGLIN -> 0.002 + (tier * 0.001);
            case IRON_GOLEM -> 0.0001 + (tier * 0.0001);
        };
        return 100.0 * c * RARE_BOUNTY_DROP_SCALE;
    }

    /** Rare type-specific boss item (undead heart, spectral quiver, etc.). */
    public static double getBountyTableRareBossPercent(int tier) {
        return 100.0 * (0.05 + tier * 0.02) * RARE_BOUNTY_DROP_SCALE;
    }

    public static double getBountyTableAttributeTokenPercent(int tier) {
        if (tier < 3) return 0;
        return 100.0 * (0.02 + (tier - 3) * 0.01) * RARE_BOUNTY_DROP_SCALE;
    }

    public static double getBountyTableFleshPercent() {
        return 100.0 * 0.90 * DataManager.getFleshDropMultiplier();
    }

    public static double getArmorDropChance(SlayerType type, int tier) {
        if (tier < 4) return 0.0;  // No special armor drops below T4

        double baseChance = switch (tier) {
            case 4 -> 1.0;
            case 5 -> 3.0;
            default -> 0.0;
        };

        if (type == SlayerType.IRON_GOLEM) {
            baseChance *= 0.5;
        }

        return baseChance * RARE_BOUNTY_DROP_SCALE;
    }

    // ============================================================
    // ADMIN UTILITY METHODS
    // ============================================================

    /**
     * Kill all active bounty bosses - returns count of bosses killed
     */
    public static int killAllBosses() {
        int killed = 0;
        for (ServerLevel world : PoliticalServer.server.getAllLevels()) {
            for (UUID bossUuid : new HashSet<>(slayerBossEntities)) {
                net.minecraft.world.entity.Entity entity = world.getEntity(bossUuid);
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