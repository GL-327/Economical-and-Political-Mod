package com.political;
import com.political.government.Treasury;



import com.political.bounty.SlayerItems;
import com.political.economy.CoinManager;
import com.political.items.CustomItemManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.BossEvent;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.spider.CaveSpider;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * World Bosses System
 * 12 unique bosses with 3 tiers each
 * Spawn mechanics, loot distribution, anti-exploit
 */
public class WorldBossManager {

    // ════════════════════════════════════════════════════════════
    // BOSS TIERS
    // ════════════════════════════════════════════════════════════

    public enum BossTier {
        COMMON("Common", ChatFormatting.WHITE, 100_000, 500_000, 50),
        ELITE("Elite", ChatFormatting.YELLOW, 500_000, 5_000_000, 100),
        LEGENDARY("Legendary", ChatFormatting.GOLD, 5_000_000, 50_000_000, 250);

        public final String name;
        public final ChatFormatting color;
        public final long baseHealth;
        public final long coinReward;
        public final int skillPoints;

        BossTier(String name, ChatFormatting color, long baseHealth, long coinReward, int skillPoints) {
            this.name = name;
            this.color = color;
            this.baseHealth = baseHealth;
            this.coinReward = coinReward;
            this.skillPoints = skillPoints;
        }
    }

    // ════════════════════════════════════════════════════════════
    // BOSS DEFINITIONS - 12 BOSSES, 3 TIERS
    // ════════════════════════════════════════════════════════════

    public enum WorldBossType {
        // TIER 1: LESSER WORLD BOSSES (Spawn 30-60 min, Despawn 15 min, 1-3 players)
        HOLLOW_KNIGHT("The Hollow Knight", "⚔", EntityType.WITHER_SKELETON, BossTier.COMMON,
            "Ancient undead warrior", new String[]{"plains", "forest", "swamp"}),
        MOSSWARDEN("Mosswarden", "🌿", EntityType.IRON_GOLEM, BossTier.COMMON,
            "Guardian of the forest", new String[]{"forest", "jungle", "swamp"}),
        SCORCHFIEND("Scorchfiend", "🔥", EntityType.BLAZE, BossTier.COMMON,
            "Born from volcanic fury", new String[]{"desert", "badlands", "nether_wastes"}),
        SKULLTIDE_CAPTAIN("Skulltide Captain", "⚓", EntityType.DROWNED, BossTier.COMMON,
            "Terror of the seas", new String[]{"ocean", "river", "beach"}),
        
        // TIER 2: GREATER WORLD BOSSES (Spawn 90-120 min, Despawn 20 min, 3-6 players)
        NETHERMAW("Nethermaw", "🐗", EntityType.HOGLIN, BossTier.ELITE,
            "Beast of the nether", new String[]{"crimson_forest", "nether_wastes", "desert"}),
        WAILING_PHANTOM("The Wailing Phantom", "👻", EntityType.PHANTOM, BossTier.ELITE,
            "Spectral terror of the night", new String[]{"any"}),
        CRYSTAL_GOLEM("Crystal Golem", "💎", EntityType.IRON_GOLEM, BossTier.ELITE,
            "Animated gemstone guardian", new String[]{"mountains", "stalactite_caves", "dripstone_caves"}),
        BLIGHT_MOTHER("Blight Mother", "🕷", EntityType.SPIDER, BossTier.ELITE,
            "Mother of all vermin", new String[]{"forest", "jungle", "swamp", "cave"}),
        
        // TIER 3: APEX WORLD BOSSES (Spawn 3-5 hours, Despawn 30 min, 6-12+ players)
        VOIDREAVER("Voidreaver", "\uD83D\uDD2E", EntityType.ENDERMAN, BossTier.LEGENDARY,
            "Traveler between dimensions", new String[]{"the_end", "desert", "badlands"}),
        INFERNAL_HYDRA("Infernal Hydra", "\uD83D\uDC09", EntityType.RAVAGER, BossTier.LEGENDARY,
            "Three-headed elemental beast", new String[]{"desert", "nether_wastes", "basalt_deltas"}),
        ANCIENT_ONE("The Ancient One", "🔱", EntityType.ELDER_GUARDIAN, BossTier.LEGENDARY,
            "Primordial ocean terror", new String[]{"deep_ocean", "ocean_monument"}),
        KING_OF_ASHES("King of Ashes", "\uD83D\uDC51", EntityType.WITHER_SKELETON, BossTier.LEGENDARY,
            "Ruler of the ashen realm", new String[]{"nether_wastes", "soul_sand_valley", "basalt_deltas"});

        public final String name;
        public final String icon;
        public final EntityType<? extends LivingEntity> entityType;
        public final BossTier tier;
        public final String description;
        public final String[] validBiomes;

        WorldBossType(String name, String icon, EntityType<? extends LivingEntity> entityType,
                      BossTier tier, String description, String[] validBiomes) {
            this.name = name;
            this.icon = icon;
            this.entityType = entityType;
            this.tier = tier;
            this.description = description;
            this.validBiomes = validBiomes;
        }

        public boolean canSpawnInBiome(String biomeId) {
            for (String biome : validBiomes) {
                if (biome.equals("any") || biomeId.contains(biome)) {
                    return true;
                }
            }
            return false;
        }
    }

    // ════════════════════════════════════════════════════════════
    // ACTIVE BOSS TRACKING
    // ════════════════════════════════════════════════════════════

    public static class ActiveWorldBoss {
        public final UUID bossId;
        public final WorldBossType type;
        public final BossTier tier;
        public final ServerLevel world;
        public final BlockPos spawnPos;
        public final long spawnTime;
        public final ServerBossEvent bossBar;
        public final Map<UUID, Long> damageDealt = new ConcurrentHashMap<>();
        public final Set<UUID> participants = ConcurrentHashMap.newKeySet();
        public long lastDamageTime;
        public long nextAbilityTime;
        public boolean isDead = false;

        public ActiveWorldBoss(UUID bossId, WorldBossType type, BossTier tier, 
                               ServerLevel world, BlockPos spawnPos, ServerBossEvent bossBar) {
            this.bossId = bossId;
            this.type = type;
            this.tier = tier;
            this.world = world;
            this.spawnPos = spawnPos;
            this.spawnTime = System.currentTimeMillis();
            this.bossBar = bossBar;
            this.lastDamageTime = spawnTime;
            this.nextAbilityTime = spawnTime + 10_000;
        }
    }

    // Active bosses by UUID
    private static final Map<UUID, ActiveWorldBoss> activeBosses = new ConcurrentHashMap<>();
    
    // Boss spawn cooldowns (type -> last spawn time)
    private static final Map<WorldBossType, Long> spawnCooldowns = new ConcurrentHashMap<>();
    
    // Player damage tracking for anti-exploit
    private static final Map<UUID, Map<UUID, Long>> playerDamageTimestamps = new ConcurrentHashMap<>();
    
    // Boss kill history for loot
    private static final Map<String, Map<WorldBossType, Integer>> playerBossKills = new ConcurrentHashMap<>();

    // ════════════════════════════════════════════════════════════
    // CONFIGURATION
    // ════════════════════════════════════════════════════════════

    // Spawn timers per tier (in milliseconds)
    private static final long TIER1_SPAWN_MIN = 30 * 60 * 1000;  // 30 min
    private static final long TIER1_SPAWN_MAX = 60 * 60 * 1000;  // 60 min
    private static final long TIER2_SPAWN_MIN = 90 * 60 * 1000;  // 90 min
    private static final long TIER2_SPAWN_MAX = 120 * 60 * 1000; // 120 min
    private static final long TIER3_SPAWN_MIN = 180 * 60 * 1000; // 3 hours
    private static final long TIER3_SPAWN_MAX = 300 * 60 * 1000; // 5 hours
    
    // Despawn timers per tier
    private static final long TIER1_DESPAWN = 15 * 60 * 1000;  // 15 min
    private static final long TIER2_DESPAWN = 20 * 60 * 1000;  // 20 min
    private static final long TIER3_DESPAWN = 30 * 60 * 1000;  // 30 min
    
    private static final int MIN_SPAWN_DISTANCE = 200; // Min distance from spawn
    private static final int MAX_SPAWN_ATTEMPTS = 10;
    private static final double MIN_DAMAGE_PERCENT_FOR_LOOT = 0.01; // 1% minimum
    
    // Random spawn scheduler
    private static long nextRandomSpawnTime = 0;
    private static BossTier nextSpawnTier = BossTier.COMMON;

    // ════════════════════════════════════════════════════════════
    // DATA PERSISTENCE
    // ════════════════════════════════════════════════════════════

    private static Path dataPath;
    private static final String DATA_FILE = "world_bosses.dat";

    public static void initialize(Path savePath) {
        dataPath = savePath;
        loadData();
    }

    private static void loadData() {
        if (dataPath == null) return;
        
        File file = dataPath.resolve(DATA_FILE).toFile();
        if (!file.exists()) return;

        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
            int playerCount = dis.readInt();
            for (int i = 0; i < playerCount; i++) {
                String uuid = dis.readUTF();
                int bossCount = dis.readInt();
                Map<WorldBossType, Integer> kills = new HashMap<>();
                for (int j = 0; j < bossCount; j++) {
                    WorldBossType type = WorldBossType.values()[dis.readInt()];
                    kills.put(type, dis.readInt());
                }
                playerBossKills.put(uuid, kills);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveData() {
        if (dataPath == null) return;
        
        File file = dataPath.resolve(DATA_FILE).toFile();
        file.getParentFile().mkdirs();

        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file))) {
            dos.writeInt(playerBossKills.size());
            
            for (Map.Entry<String, Map<WorldBossType, Integer>> entry : playerBossKills.entrySet()) {
                dos.writeUTF(entry.getKey());
                dos.writeInt(entry.getValue().size());
                for (Map.Entry<WorldBossType, Integer> bossEntry : entry.getValue().entrySet()) {
                    dos.writeInt(bossEntry.getKey().ordinal());
                    dos.writeInt(bossEntry.getValue());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ════════════════════════════════════════════════════════════
    // BOSS SPAWNING
    // ════════════════════════════════════════════════════════════

    /**
     * Spawn a world boss at a random valid location
     */
    public static boolean spawnWorldBoss(ServerLevel world, WorldBossType type) {
        BlockPos spawnPos = findRandomSpawnLocation(world, type);
        if (spawnPos == null) return false;
        return spawnWorldBossAt(world, type, type.tier, spawnPos);
    }

    /**
     * Find a random valid spawn location for a boss
     */
    private static BlockPos findRandomSpawnLocation(ServerLevel world, WorldBossType type) {
        Random random = new Random();
        
        for (int attempt = 0; attempt < MAX_SPAWN_ATTEMPTS; attempt++) {
            // Random coordinates within world border
            int spawnX = random.nextInt(10000) - 5000;
            int spawnZ = random.nextInt(10000) - 5000;
            
            // Avoid spawn area (200 blocks)
            if (Math.abs(spawnX) < MIN_SPAWN_DISTANCE && Math.abs(spawnZ) < MIN_SPAWN_DISTANCE) {
                continue;
            }
            
            // Get surface position
            BlockPos surfacePos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, 
                new BlockPos(spawnX, 0, spawnZ));
            
            // Check biome
            var biome = world.getBiome(surfacePos);
            String biomeId = biome.unwrapKey().map(k -> k.identifier().toString()).orElse("");
            
            if (type.canSpawnInBiome(biomeId)) {
                return surfacePos;
            }
        }
        return null; // No valid location found
    }

    /**
     * Spawn a world boss at a specific location
     */
    public static boolean spawnWorldBossAt(ServerLevel world, WorldBossType type, BossTier tier, BlockPos pos) {
        // Create entity
        LivingEntity entity = (LivingEntity) type.entityType.create(world, net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
        if (entity == null) return false;

        // Position
        entity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        if (entity instanceof Mob mob) {
            mob.setPersistenceRequired();
        }

        // Set name
        String tierPrefix = switch (tier) {
            case COMMON -> "§f";
            case ELITE -> "§e";
            case LEGENDARY -> "§6§l";
        };
        entity.setCustomName(Component.literal(tierPrefix + type.icon + " " + type.name));
        entity.setCustomNameVisible(true);

        // Scale health based on tier
        float health = tier.baseHealth;
        var healthAttr = entity.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.setBaseValue(health);
            entity.setHealth(health);
        }

        // Add glowing effect
        entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.GLOWING, Integer.MAX_VALUE, 0, false, false));

        // Spawn entity
        world.addFreshEntity(entity);

        // Create boss bar
        ServerBossEvent bossBar = new ServerBossEvent(
            java.util.UUID.randomUUID(),
            Component.literal(tierPrefix + type.icon + " " + type.name),
            tier == BossTier.COMMON ? BossEvent.BossBarColor.WHITE :
                tier == BossTier.ELITE ? BossEvent.BossBarColor.YELLOW : BossEvent.BossBarColor.RED,
            BossEvent.BossBarOverlay.PROGRESS
        );

        // Track active boss
        ActiveWorldBoss activeBoss = new ActiveWorldBoss(
            entity.getUUID(), type, tier, world, pos, bossBar
        );
        activeBosses.put(entity.getUUID(), activeBoss);

        // Update cooldown
        spawnCooldowns.put(type, System.currentTimeMillis());

        // Announce spawn
        announceBossSpawn(world, type, tier, pos);

        return true;
    }

    private static void announceBossSpawn(ServerLevel world, WorldBossType type, BossTier tier, BlockPos pos) {
        Component announcement = Component.literal("═══════════════════════════════════")
            .withStyle(ChatFormatting.DARK_PURPLE)
            .append(Component.literal("\n        ⚠ WORLD BOSS SPAWNED ⚠\n")
                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
            .append(Component.literal("  " + type.icon + " " + tier.name + " " + type.name + "\n")
                .withStyle(tier.color))
            .append(Component.literal("  Location: " + pos.getX() + ", " + pos.getZ() + "\n")
                .withStyle(ChatFormatting.GRAY))
            .append(Component.literal("═══════════════════════════════════")
                .withStyle(ChatFormatting.DARK_PURPLE));

        for (ServerPlayer player : world.players()) {
            player.sendSystemMessage(announcement, false);
            world.playSound(player, player.blockPosition(), 
                SoundEvents.ENDER_DRAGON_GROWL, 
                net.minecraft.sounds.SoundSource.HOSTILE, 1.0f, 0.5f);
        }
    }

    // ════════════════════════════════════════════════════════════
    // DAMAGE TRACKING & ANTI-EXPLOIT
    // ════════════════════════════════════════════════════════════

    /**
     * Record damage dealt to a world boss
     */
    public static void recordDamage(UUID bossId, ServerPlayer player, float damage) {
        ActiveWorldBoss boss = activeBosses.get(bossId);
        if (boss == null || boss.isDead) return;

        UUID playerId = player.getUUID();

        // Anti-exploit: Check damage rate
        Map<UUID, Long> timestamps = playerDamageTimestamps.computeIfAbsent(bossId, k -> new HashMap<>());
        Long lastDamage = timestamps.get(playerId);
        long now = System.currentTimeMillis();

        if (lastDamage != null && now - lastDamage < 50) { // Max 20 hits/second
            return; // Too fast, likely exploit
        }
        timestamps.put(playerId, now);

        // Record damage
        long currentDamage = boss.damageDealt.getOrDefault(playerId, 0L);
        boss.damageDealt.put(playerId, currentDamage + (long) damage);
        boss.participants.add(playerId);
        boss.lastDamageTime = now;

        // Update boss bar
        if (player instanceof ServerPlayer serverPlayer) {
            boss.bossBar.addPlayer(serverPlayer);
        }
    }

    /**
     * Handle world boss death. {@code killingPlayer} is used when no damage was recorded (e.g. one-shot edge cases).
     */
    public static void onBossDeath(LivingEntity entity, ServerLevel world, ServerPlayer killingPlayer) {
        ActiveWorldBoss boss = activeBosses.get(entity.getUUID());
        if (boss == null) return;

        boss.isDead = true;

        long totalDamage = boss.damageDealt.values().stream().mapToLong(Long::longValue).sum();
        if (totalDamage <= 0 && killingPlayer != null) {
            boss.damageDealt.put(killingPlayer.getUUID(), (long) Math.max(1f, entity.getMaxHealth()));
            boss.participants.add(killingPlayer.getUUID());
            totalDamage = boss.damageDealt.values().stream().mapToLong(Long::longValue).sum();
        }

        if (totalDamage <= 0) {
            boss.bossBar.removeAllPlayers();
            activeBosses.remove(entity.getUUID());
            playerDamageTimestamps.remove(entity.getUUID());
            return;
        }

        for (Map.Entry<UUID, Long> entry : boss.damageDealt.entrySet()) {
            UUID playerId = entry.getKey();
            long playerDamage = entry.getValue();
            double damagePercent = (double) playerDamage / (double) totalDamage;

            if (damagePercent < MIN_DAMAGE_PERCENT_FOR_LOOT) continue;

            ServerPlayer player = world.getServer().getPlayerList().getPlayer(playerId);
            if (player == null) continue;

            long coinReward = (long) (boss.tier.coinReward * damagePercent);
            int skillPoints = Math.max(1, (int) (boss.tier.skillPoints * damagePercent));

            CoinManager.giveCoins(player, coinReward);
            ProgressionTreeManager.awardSkillPoints(player, skillPoints, "World Boss: " + boss.type.name);

            String uuid = player.getUUID().toString();
            playerBossKills.computeIfAbsent(uuid, k -> new HashMap<>())
                .merge(boss.type, 1, Integer::sum);

            player.sendSystemMessage(Component.literal("═══════════════════════════════════")
                .withStyle(ChatFormatting.GOLD), false);
            player.sendSystemMessage(Component.literal("    ★ WORLD BOSS DEFEATED ★")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
            player.sendSystemMessage(Component.literal("═══════════════════════════════════")
                .withStyle(ChatFormatting.GOLD), false);
            player.sendSystemMessage(Component.literal("§aYour contribution: §f" +
                String.format("%.1f%%", damagePercent * 100)), false);
            player.sendSystemMessage(Component.literal("§6Coins earned: §f" +
                String.format("%,d", coinReward)), false);
            player.sendSystemMessage(Component.literal("§bSkill points: §f+" + skillPoints), false);

            grantWorldBossLoot(player, boss, damagePercent);
        }

        boss.bossBar.removeAllPlayers();
        activeBosses.remove(entity.getUUID());
        playerDamageTimestamps.remove(entity.getUUID());

        world.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
            entity.getX(), entity.getY() + 1, entity.getZ(),
            10, 1, 1, 1, 0.5);

        saveData();
    }

    private static String trophyIdForType(WorldBossType type) {
        return switch (type) {
            case HOLLOW_KNIGHT -> "trophy_hollow_knight";
            case MOSSWARDEN -> "trophy_mosswarden";
            case SCORCHFIEND -> "trophy_scorchfiend";
            case SKULLTIDE_CAPTAIN -> "trophy_skulltide_captain";
            case NETHERMAW -> "trophy_nethermaw";
            case WAILING_PHANTOM -> "trophy_wailing_phantom";
            case CRYSTAL_GOLEM -> "trophy_crystal_golem";
            case BLIGHT_MOTHER -> "trophy_blight_mother";
            case VOIDREAVER -> "trophy_voidreaver";
            case INFERNAL_HYDRA -> "trophy_infernal_hydra";
            case ANCIENT_ONE -> "trophy_ancient_one";
            case KING_OF_ASHES -> "trophy_king_of_ashes";
        };
    }

    /**
     * Crafter core + boss trophy for everyone above the participation threshold; bonus treasury drops for higher contribution.
     */
    private static void grantWorldBossLoot(ServerPlayer player, ActiveWorldBoss boss, double damagePercent) {
        ItemStack core;
        String coreLabel;
        if (boss.tier == BossTier.LEGENDARY) {
            core = SlayerItems.createCustomCrafterCoreT3();
            coreLabel = "§6§lT3 Custom Crafter Core";
        } else if (boss.tier == BossTier.ELITE) {
            core = SlayerItems.createCustomCrafterCoreT2();
            coreLabel = "§e§lT2 Custom Crafter Core";
        } else {
            core = SlayerItems.createCustomCrafterCoreT1();
            coreLabel = "§f§lT1 Custom Crafter Core";
        }
        player.addItem(core);
        player.sendSystemMessage(Component.literal("§d★ You received a " + coreLabel + "§r§d!"), false);

        int netheriteCount = switch (boss.tier) {
            case COMMON -> 1;
            case ELITE -> 3;
            case LEGENDARY -> 5;
        };
        player.addItem(new ItemStack(Items.NETHERITE_INGOT, netheriteCount));
        player.sendSystemMessage(Component.literal("§b+" + netheriteCount + " Netherite Ingot(s)"), false);

        String trophyId = trophyIdForType(boss.type);
        CustomItemManager.CustomItem trophy = CustomItemManager.getItem(trophyId);
        if (trophy != null) {
            player.addItem(trophy.createStack(1));
            player.sendSystemMessage(Component.literal("§6★ Boss trophy added to your inventory!"), false);
        }

        CustomItemManager.CustomItem fragment = CustomItemManager.getItem("treasury_fragment");
        if (fragment != null && damagePercent >= 0.08) {
            int fragCount = boss.tier == BossTier.LEGENDARY ? 3 : boss.tier == BossTier.ELITE ? 2 : 1;
            player.addItem(fragment.createStack(fragCount));
            player.sendSystemMessage(Component.literal("§e+" + fragCount + " Treasury Fragment(s) §7(high contribution)"), false);
        }

        if (damagePercent >= 0.22) {
            CustomItemManager.CustomItem commendation = CustomItemManager.getItem("sovereign_commendation");
            if (commendation != null) {
                player.addItem(commendation.createStack(1));
                player.sendSystemMessage(Component.literal("§5★ Sovereign Commendation §7(top contributor bonus)"), false);
            }
        }
    }

    // ════════════════════════════════════════════════════════════
    // TICK PROCESSING
    // ════════════════════════════════════════════════════════════

    public static void tick(MinecraftServer server) {
        long now = System.currentTimeMillis();
        Random random = new Random();

        // Random spawn logic - check if it's time to spawn a random boss
        if (nextRandomSpawnTime == 0) {
            // Initialize first spawn timer
            scheduleNextRandomSpawn();
        } else if (now >= nextRandomSpawnTime && activeBosses.size() < 3) {
            // Time to spawn a random boss
            ServerLevel world = server.overworld();
            WorldBossType type = selectRandomBossForTier(nextSpawnTier);
            if (type != null) {
                spawnWorldBoss(world, type);
            }
            scheduleNextRandomSpawn();
        }

        // Check for boss timeouts (tier-based despawn)
        for (ActiveWorldBoss boss : new ArrayList<>(activeBosses.values())) {
            long despawnTime = switch (boss.tier) {
                case COMMON -> TIER1_DESPAWN;
                case ELITE -> TIER2_DESPAWN;
                case LEGENDARY -> TIER3_DESPAWN;
            };
            
            // Timeout check
            if (now - boss.spawnTime > despawnTime) {
                // Boss has been alive too long - despawn
                LivingEntity entity = (LivingEntity) boss.world.getEntity(boss.bossId);
                if (entity != null) {
                    entity.discard();
                }
                boss.bossBar.removeAllPlayers();
                activeBosses.remove(boss.bossId);
                continue;
            }

            // Inactivity check (no damage for 5 minutes)
            if (now - boss.lastDamageTime > 5 * 60 * 1000) {
                LivingEntity entity = (LivingEntity) boss.world.getEntity(boss.bossId);
                if (entity != null && entity.isAlive()) {
                    // Heal boss if inactive
                    entity.heal(entity.getMaxHealth() * 0.1f);
                    boss.world.sendParticles(ParticleTypes.HEART,
                        entity.getX(), entity.getY() + 2, entity.getZ(),
                        4, 0.6, 0.6, 0.6, 0.1);
                }
            }

            // Update boss bar
            LivingEntity entity = (LivingEntity) boss.world.getEntity(boss.bossId);
            if (entity != null && entity.isAlive()) {
                boss.bossBar.setProgress(entity.getHealth() / entity.getMaxHealth());

                // Keep boss bar visible to nearby players
                for (ServerPlayer p : boss.world.players()) {
                    boolean near = p.blockPosition().distSqr(entity.blockPosition()) <= 96 * 96;
                    boolean has = boss.bossBar.getPlayers().contains(p);
                    if (near && !has) {
                        boss.bossBar.addPlayer(p);
                    } else if (!near && has) {
                        boss.bossBar.removePlayer(p);
                    }
                }

                // Simple special abilities
                if (now >= boss.nextAbilityTime) {
                    performBossAbility(boss, entity, random);
                    long baseCd = switch (boss.tier) {
                        case COMMON -> 12_000;
                        case ELITE -> 9_000;
                        case LEGENDARY -> 6_000;
                    };
                    boss.nextAbilityTime = now + baseCd;
                }
            }
        }
    }

    private static void performBossAbility(ActiveWorldBoss boss, LivingEntity entity, Random random) {
        // Abilities are intentionally simple and server-safe.
        // They make bosses feel more like encounters without requiring new entity classes.
        int range = switch (boss.tier) {
            case COMMON -> 10;
            case ELITE -> 14;
            case LEGENDARY -> 18;
        };

        List<LivingEntity> targets = boss.world.getEntitiesOfClass(LivingEntity.class,
                entity.getBoundingBox().inflate(range),
                e -> e.isAlive() && e != entity);

        switch (boss.type) {
            case HOLLOW_KNIGHT -> {
                for (LivingEntity t : targets) {
                    if (t instanceof ServerPlayer sp) {
                        sp.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                net.minecraft.world.effect.MobEffects.WITHER, 60, 0));
                    }
                }
                // Summon a couple adds
                int adds = boss.tier == BossTier.LEGENDARY ? 6 : boss.tier == BossTier.ELITE ? 4 : 2;
                for (int i = 0; i < adds; i++) {
                    WitherSkeleton ws = EntityType.WITHER_SKELETON.create(boss.world, net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
                    if (ws != null) {
                        ws.teleportTo(entity.getX() + (random.nextDouble() - 0.5) * 6, entity.getY(), entity.getZ() + (random.nextDouble() - 0.5) * 6);
                        boss.world.addFreshEntity(ws);
                    }
                }
            }
            case MOSSWARDEN -> {
                entity.heal(entity.getMaxHealth() * 0.03f);
                boss.world.sendParticles(ParticleTypes.HAPPY_VILLAGER, entity.getX(), entity.getY() + 2, entity.getZ(), 5, 0.6, 0.6, 0.6, 0.1);
                for (LivingEntity t : targets) {
                    if (t instanceof ServerPlayer sp) {
                        sp.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                net.minecraft.world.effect.MobEffects.SLOWNESS, 80, 1));
                    }
                }
            }
            case SCORCHFIEND -> {
                for (LivingEntity t : targets) {
                    t.setRemainingFireTicks(100);
                    t.hurtServer(boss.world, boss.world.damageSources().onFire(), boss.tier == BossTier.LEGENDARY ? 10f : 6f);
                }
                boss.world.sendParticles(ParticleTypes.FLAME, entity.getX(), entity.getY() + 1, entity.getZ(), 8, 1, 0.6, 1, 0.02);
            }
            case NETHERMAW -> {
                for (LivingEntity t : targets) {
                    Vec3 kb = new Vec3(t.getX() - entity.getX(), 0, t.getZ() - entity.getZ()).normalize().scale(1.2);
                    t.setDeltaMovement(t.getDeltaMovement().add(kb.x, 0.5, kb.z));
                    t.hurtServer(boss.world, boss.world.damageSources().mobAttack(entity), boss.tier == BossTier.LEGENDARY ? 14f : 8f);
                }
                boss.world.sendParticles(ParticleTypes.LAVA, entity.getX(), entity.getY() + 1, entity.getZ(), 5, 0.8, 0.5, 0.8, 0.02);
            }
            case VOIDREAVER -> {
                // Short-range blink
                BlockPos blink = entity.blockPosition().offset(random.nextInt(9) - 4, 0, random.nextInt(9) - 4);
                BlockPos top = boss.world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blink);
                entity.teleportTo(top.getX() + 0.5, top.getY(), top.getZ() + 0.5);
                for (LivingEntity t : targets) {
                    if (t instanceof ServerPlayer sp) {
                        sp.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                net.minecraft.world.effect.MobEffects.DARKNESS, 60, 0));
                    }
                }
                boss.world.sendParticles(ParticleTypes.PORTAL, entity.getX(), entity.getY() + 1, entity.getZ(), 10, 0.8, 0.6, 0.8, 0.2);
            }
            case INFERNAL_HYDRA -> {
                for (LivingEntity t : targets) {
                    t.hurtServer(boss.world, boss.world.damageSources().explosion(entity, null), boss.tier == BossTier.LEGENDARY ? 18f : 10f);
                    t.setRemainingFireTicks(80);
                }
                boss.world.sendParticles(ParticleTypes.EXPLOSION, entity.getX(), entity.getY() + 1, entity.getZ(), 4, 0.6, 0.6, 0.6, 0.1);
            }
            case ANCIENT_ONE -> {
                for (LivingEntity t : targets) {
                    if (t instanceof ServerPlayer sp) {
                        sp.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                net.minecraft.world.effect.MobEffects.MINING_FATIGUE, 120, 1));
                        sp.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                net.minecraft.world.effect.MobEffects.BLINDNESS, 40, 0));
                    }
                }
                boss.world.sendParticles(ParticleTypes.BUBBLE, entity.getX(), entity.getY() + 1, entity.getZ(), 8, 1, 0.6, 1, 0.05);
            }
            case SKULLTIDE_CAPTAIN -> {
                for (LivingEntity t : targets) {
                    if (t instanceof ServerPlayer sp) {
                        sp.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                net.minecraft.world.effect.MobEffects.NAUSEA, 100, 0));
                        sp.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                net.minecraft.world.effect.MobEffects.SLOWNESS, 60, 0));
                    }
                }
                int drown = boss.tier == BossTier.LEGENDARY ? 4 : boss.tier == BossTier.ELITE ? 3 : 2;
                for (int i = 0; i < drown; i++) {
                    Mob d = EntityType.DROWNED.create(boss.world, EntitySpawnReason.MOB_SUMMONED);
                    if (d != null) {
                        d.teleportTo(entity.getX() + (random.nextDouble() - 0.5) * 5, entity.getY(), entity.getZ() + (random.nextDouble() - 0.5) * 5);
                        boss.world.addFreshEntity(d);
                    }
                }
                boss.world.sendParticles(ParticleTypes.SPLASH, entity.getX(), entity.getY() + 1, entity.getZ(), 8, 1, 0.6, 1, 0.1);
            }
            case WAILING_PHANTOM -> {
                for (LivingEntity t : targets) {
                    if (t instanceof ServerPlayer sp) {
                        sp.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                net.minecraft.world.effect.MobEffects.LEVITATION, 40, boss.tier == BossTier.LEGENDARY ? 1 : 0));
                        sp.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                net.minecraft.world.effect.MobEffects.WEAKNESS, 80, 0));
                    }
                }
                boss.world.sendParticles(ParticleTypes.CLOUD, entity.getX(), entity.getY() + 2, entity.getZ(), 8, 1, 0.6, 1, 0.05);
            }
            case CRYSTAL_GOLEM -> {
                entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.RESISTANCE, 100, boss.tier == BossTier.LEGENDARY ? 2 : 1));
                for (LivingEntity t : targets) {
                    if (t instanceof ServerPlayer sp) {
                        sp.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                net.minecraft.world.effect.MobEffects.SLOWNESS, 60, 2));
                    }
                }
                boss.world.sendParticles(ParticleTypes.ENCHANT, entity.getX(), entity.getY() + 1, entity.getZ(), 8, 0.6, 0.6, 0.6, 0.2);
            }
            case BLIGHT_MOTHER -> {
                for (LivingEntity t : targets) {
                    if (t instanceof ServerPlayer sp) {
                        sp.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                net.minecraft.world.effect.MobEffects.POISON, 100, 0));
                    }
                }
                int spiders = boss.tier == BossTier.LEGENDARY ? 5 : boss.tier == BossTier.ELITE ? 3 : 2;
                for (int i = 0; i < spiders; i++) {
                    CaveSpider cs = EntityType.CAVE_SPIDER.create(boss.world, EntitySpawnReason.MOB_SUMMONED);
                    if (cs != null) {
                        cs.teleportTo(entity.getX() + (random.nextDouble() - 0.5) * 5, entity.getY(), entity.getZ() + (random.nextDouble() - 0.5) * 5);
                        boss.world.addFreshEntity(cs);
                    }
                }
                boss.world.sendParticles(ParticleTypes.ITEM_SLIME, entity.getX(), entity.getY() + 1, entity.getZ(), 5, 0.8, 0.5, 0.8, 0.05);
            }
            case KING_OF_ASHES -> {
                for (LivingEntity t : targets) {
                    if (t instanceof ServerPlayer sp) {
                        sp.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                net.minecraft.world.effect.MobEffects.WITHER, 80, 0));
                    }
                }
                int skel = boss.tier == BossTier.LEGENDARY ? 5 : 3;
                for (int i = 0; i < skel; i++) {
                    WitherSkeleton ws = EntityType.WITHER_SKELETON.create(boss.world, EntitySpawnReason.MOB_SUMMONED);
                    if (ws != null) {
                        ws.teleportTo(entity.getX() + (random.nextDouble() - 0.5) * 6, entity.getY(), entity.getZ() + (random.nextDouble() - 0.5) * 6);
                        boss.world.addFreshEntity(ws);
                    }
                }
                boss.world.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, entity.getX(), entity.getY() + 1, entity.getZ(), 8, 1, 0.6, 1, 0.02);
            }
            default -> {
                for (LivingEntity t : targets) {
                    t.hurtServer(boss.world, boss.world.damageSources().mobAttack(entity), boss.tier == BossTier.LEGENDARY ? 8f : 4f);
                }
            }
        }
    }

    /**
     * Schedule the next random boss spawn
     */
    private static void scheduleNextRandomSpawn() {
        Random random = new Random();
        
        // Determine next tier (weighted: 60% T1, 30% T2, 10% T3)
        int roll = random.nextInt(100);
        if (roll < 60) {
            nextSpawnTier = BossTier.COMMON;
            nextRandomSpawnTime = System.currentTimeMillis() + 
                TIER1_SPAWN_MIN + random.nextLong(TIER1_SPAWN_MAX - TIER1_SPAWN_MIN);
        } else if (roll < 90) {
            nextSpawnTier = BossTier.ELITE;
            nextRandomSpawnTime = System.currentTimeMillis() + 
                TIER2_SPAWN_MIN + random.nextLong(TIER2_SPAWN_MAX - TIER2_SPAWN_MIN);
        } else {
            nextSpawnTier = BossTier.LEGENDARY;
            nextRandomSpawnTime = System.currentTimeMillis() + 
                TIER3_SPAWN_MIN + random.nextLong(TIER3_SPAWN_MAX - TIER3_SPAWN_MIN);
        }
    }

    /**
     * Select a random boss of the given tier
     */
    private static WorldBossType selectRandomBossForTier(BossTier tier) {
        List<WorldBossType> bosses = new ArrayList<>();
        for (WorldBossType type : WorldBossType.values()) {
            if (type.tier == tier) {
                bosses.add(type);
            }
        }
        if (bosses.isEmpty()) return null;
        return bosses.get(new Random().nextInt(bosses.size()));
    }

    // ════════════════════════════════════════════════════════════
    // PUBLIC API
    // ════════════════════════════════════════════════════════════

    public static boolean isWorldBoss(UUID entityId) {
        return activeBosses.containsKey(entityId);
    }

    public static ActiveWorldBoss getActiveBoss(UUID entityId) {
        return activeBosses.get(entityId);
    }

    public static List<ActiveWorldBoss> getActiveBosses() {
        return new ArrayList<>(activeBosses.values());
    }

    public static int getPlayerBossKills(String uuid, WorldBossType type) {
        Map<WorldBossType, Integer> kills = playerBossKills.get(uuid);
        return kills != null ? kills.getOrDefault(type, 0) : 0;
    }

    public static int getTotalPlayerBossKills(String uuid) {
        Map<WorldBossType, Integer> kills = playerBossKills.get(uuid);
        return kills != null ? kills.values().stream().mapToInt(Integer::intValue).sum() : 0;
    }

    public static long getTimeUntilNextSpawn() {
        if (nextRandomSpawnTime == 0) return 0;
        return Math.max(0, nextRandomSpawnTime - System.currentTimeMillis());
    }

    public static BossTier getNextSpawnTier() {
        return nextSpawnTier;
    }

    // ════════════════════════════════════════════════════════════
    // ADMIN COMMANDS
    // ════════════════════════════════════════════════════════════

    public static void forceSpawn(ServerLevel world, WorldBossType type) {
        BlockPos spawnPos = findRandomSpawnLocation(world, type);
        if (spawnPos != null) {
            spawnWorldBossAt(world, type, type.tier, spawnPos);
        } else {
            // Fallback to random position
            spawnWorldBossAt(world, type, type.tier, new BlockPos(0, 64, 0));
        }
    }

    public static void forceSpawnAt(ServerLevel world, WorldBossType type, BossTier tier, BlockPos pos) {
        spawnWorldBossAt(world, type, tier, pos);
    }

    public static void despawnAll() {
        for (ActiveWorldBoss boss : activeBosses.values()) {
            LivingEntity entity = (LivingEntity) boss.world.getEntity(boss.bossId);
            if (entity != null) {
                entity.discard();
            }
            boss.bossBar.removeAllPlayers();
        }
        activeBosses.clear();
    }
}
