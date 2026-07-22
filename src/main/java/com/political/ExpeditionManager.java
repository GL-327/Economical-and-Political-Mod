package com.political;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.levelgen.Heightmap;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Expedition System - Pillar 2
 * 
 * High-risk, high-reward zones with custom ore nodes and special drops.
 * Players enter with a party, clear waves, and mine nodes for rewards.
 * Lockout system prevents spam-running.
 */
public class ExpeditionManager {

    // ════════════════════════════════════════════════════════════
    // EXPEDITION ZONES
    // ════════════════════════════════════════════════════════════
    
    public enum ExpeditionZone {
        ASHEN_MINES("Ashen Mines", "⛏", ChatFormatting.RED, 2, 
                "A scorching mine deep beneath the nether wastes",
                new String[]{"nether", "fire", "ore"}),
        DROWNED_ARCHIVES("Drowned Archives", "📜", ChatFormatting.DARK_AQUA, 4,
                "Ancient library flooded by cursed waters",
                new String[]{"ocean", "water", "knowledge"}),
        GLACIAL_SPIRE("Glacial Spire", "❄", ChatFormatting.AQUA, 5,
                "A frozen tower reaching into the clouds",
                new String[]{"ice", "frost", "height"}),
        VOID_RIFT("Void Rift", "◌", ChatFormatting.DARK_PURPLE, 7,
                "A tear in reality leading to the void",
                new String[]{"end", "void", "chaos"}),
        DEEP_SEAM("Deep Seam", "💎", ChatFormatting.LIGHT_PURPLE, 10,
                "The deepest ore vein, guarded by ancient horrors",
                new String[]{"deep", "rare", "mastery"});

        public final String displayName;
        public final String icon;
        public final ChatFormatting color;
        public final int minMasteryLevel;
        public final String description;
        public final String[] tags;

        ExpeditionZone(String displayName, String icon, ChatFormatting color, int minMasteryLevel,
                      String description, String[] tags) {
            this.displayName = displayName;
            this.icon = icon;
            this.color = color;
            this.minMasteryLevel = minMasteryLevel;
            this.description = description;
            this.tags = tags;
        }
    }

    // ════════════════════════════════════════════════════════════
    // EXPEDITION TIERS
    // ════════════════════════════════════════════════════════════
    
    public enum ExpeditionTier {
        TIER_1(1, "Normal", 1.0, 5, 10, 300),      // 5 min lockout
        TIER_2(2, "Hard", 1.5, 7, 15, 600),       // 10 min lockout
        TIER_3(3, "Nightmare", 2.0, 10, 25, 1200), // 20 min lockout
        TIER_4(4, "Hell", 3.0, 15, 50, 3600);     // 60 min lockout

        public final int tier;
        public final String displayName;
        public final double difficultyMultiplier;
        public final int waveCount;
        public final int nodeCount;
        public final int lockoutSeconds;

        ExpeditionTier(int tier, String displayName, double difficultyMultiplier,
                      int waveCount, int nodeCount, int lockoutSeconds) {
            this.tier = tier;
            this.displayName = displayName;
            this.difficultyMultiplier = difficultyMultiplier;
            this.waveCount = waveCount;
            this.nodeCount = nodeCount;
            this.lockoutSeconds = lockoutSeconds;
        }
    }

    // ════════════════════════════════════════════════════════════
    // NODE TYPES (Custom Ore Blocks)
    // ════════════════════════════════════════════════════════════
    
    public enum NodeType {
        COAL_NODE("Coal Node", Items.COAL, 5, 10),
        IRON_NODE("Iron Node", Items.RAW_IRON, 10, 20),
        GOLD_NODE("Gold Node", Items.RAW_GOLD, 25, 50),
        DIAMOND_NODE("Diamond Node", Items.DIAMOND, 50, 100),
        EMERALD_NODE("Emerald Node", Items.EMERALD, 75, 150),
        VOID_CRYSTAL("Void Crystal", Items.END_CRYSTAL, 100, 200),
        ANCIENT_ORE("Ancient Ore", Items.NETHERITE_SCRAP, 150, 300),
        MASTERY_CORE("Mastery Core", Items.NETHER_STAR, 200, 500);

        public final String displayName;
        public final Item dropItem;
        public final int minAmount;
        public final int maxAmount;

        NodeType(String displayName, Item dropItem, int minAmount, int maxAmount) {
            this.displayName = displayName;
            this.dropItem = dropItem;
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
        }
    }

    // ════════════════════════════════════════════════════════════
    // PLAYER DATA
    // ════════════════════════════════════════════════════════════
    
    // UUID -> Zone -> last completion timestamp
    private static final Map<String, Map<String, Long>> playerLockouts = new ConcurrentHashMap<>();
    
    // UUID -> Zone -> best tier cleared
    private static final Map<String, Map<String, Integer>> playerBestTier = new ConcurrentHashMap<>();
    
    // Active expeditions: party leader UUID -> ExpeditionRun
    private static final Map<String, ExpeditionRun> activeExpeditions = new ConcurrentHashMap<>();
    
    // Party invites: invited UUID -> leader UUID
    private static final Map<String, String> partyInvites = new ConcurrentHashMap<>();
    
    // Party membership: member UUID -> leader UUID
    private static final Map<String, String> partyMembers = new ConcurrentHashMap<>();
    
    private static Path dataFile;

    public static class ExpeditionRun {
        public ExpeditionZone zone;
        public ExpeditionTier tier;
        public String leaderUuid;
        public Set<String> members = new HashSet<>();
        public int currentWave = 0;
        public int nodesRemaining = 0;
        public BlockPos centerPos;
        public String worldId;
        public long startTime;
        public Set<BlockPos> nodePositions = new HashSet<>();
        
        public ExpeditionRun(ExpeditionZone zone, ExpeditionTier tier, String leaderUuid) {
            this.zone = zone;
            this.tier = tier;
            this.leaderUuid = leaderUuid;
            this.members.add(leaderUuid);
            this.startTime = System.currentTimeMillis();
        }
    }

    // ════════════════════════════════════════════════════════════
    // INITIALIZATION & PERSISTENCE
    // ════════════════════════════════════════════════════════════
    
    public static void initialize(Path dataDir) {
        dataFile = dataDir.resolve("expedition_data.json");
        loadData();
    }
    
    private static void loadData() {
        if (dataFile == null || !Files.exists(dataFile)) return;
        
        try (Reader reader = Files.newBufferedReader(dataFile)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> loaded = gson.fromJson(reader, type);
            
            if (loaded != null) {
                // Load lockouts
                @SuppressWarnings("unchecked")
                Map<String, Map<String, Double>> lockouts = (Map<String, Map<String, Double>>) loaded.get("lockouts");
                if (lockouts != null) {
                    for (Map.Entry<String, Map<String, Double>> entry : lockouts.entrySet()) {
                        Map<String, Long> zoneMap = new HashMap<>();
                        for (Map.Entry<String, Double> zone : entry.getValue().entrySet()) {
                            zoneMap.put(zone.getKey(), zone.getValue().longValue());
                        }
                        playerLockouts.put(entry.getKey(), zoneMap);
                    }
                }
                
                // Load best tiers
                @SuppressWarnings("unchecked")
                Map<String, Map<String, Double>> best = (Map<String, Map<String, Double>>) loaded.get("bestTier");
                if (best != null) {
                    for (Map.Entry<String, Map<String, Double>> entry : best.entrySet()) {
                        Map<String, Integer> zoneMap = new HashMap<>();
                        for (Map.Entry<String, Double> zone : entry.getValue().entrySet()) {
                            zoneMap.put(zone.getKey(), zone.getValue().intValue());
                        }
                        playerBestTier.put(entry.getKey(), zoneMap);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[ExpeditionManager] Failed to load data: " + e.getMessage());
        }
    }
    
    public static void saveData() {
        if (dataFile == null) return;
        
        try {
            Files.createDirectories(dataFile.getParent());
            
            Map<String, Object> toSave = new HashMap<>();
            toSave.put("lockouts", playerLockouts);
            toSave.put("bestTier", playerBestTier);
            
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (Writer writer = Files.newBufferedWriter(dataFile)) {
                gson.toJson(toSave, writer);
            }
        } catch (Exception e) {
            System.err.println("[ExpeditionManager] Failed to save data: " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════
    // ACCESS CHECKS
    // ════════════════════════════════════════════════════════════
    
    public static boolean canAccessZone(String uuid, ExpeditionZone zone) {
        return MasteryManager.canAccessExpeditionZone(uuid, zone.name().toLowerCase());
    }
    
    public static boolean isOnLockout(String uuid, ExpeditionZone zone, ExpeditionTier tier) {
        Map<String, Long> zoneLockouts = playerLockouts.get(uuid);
        if (zoneLockouts == null) return false;
        
        String key = zone.name() + "_" + tier.tier;
        Long lockoutEnd = zoneLockouts.get(key);
        if (lockoutEnd == null) return false;
        
        return System.currentTimeMillis() < lockoutEnd;
    }
    
    public static long getLockoutRemaining(String uuid, ExpeditionZone zone, ExpeditionTier tier) {
        Map<String, Long> zoneLockouts = playerLockouts.get(uuid);
        if (zoneLockouts == null) return 0;
        
        String key = zone.name() + "_" + tier.tier;
        Long lockoutEnd = zoneLockouts.get(key);
        if (lockoutEnd == null) return 0;
        
        return Math.max(0, lockoutEnd - System.currentTimeMillis());
    }
    
    public static void applyLockout(String uuid, ExpeditionZone zone, ExpeditionTier tier) {
        Map<String, Long> zoneLockouts = playerLockouts.computeIfAbsent(uuid, k -> new HashMap<>());
        String key = zone.name() + "_" + tier.tier;
        long lockoutEnd = System.currentTimeMillis() + (tier.lockoutSeconds * 1000L);
        zoneLockouts.put(key, lockoutEnd);
        saveData();
    }
    
    public static int getBestTier(String uuid, ExpeditionZone zone) {
        Map<String, Integer> best = playerBestTier.get(uuid);
        if (best == null) return 0;
        return best.getOrDefault(zone.name(), 0);
    }
    
    public static void setBestTier(String uuid, ExpeditionZone zone, int tier) {
        Map<String, Integer> best = playerBestTier.computeIfAbsent(uuid, k -> new HashMap<>());
        int current = best.getOrDefault(zone.name(), 0);
        if (tier > current) {
            best.put(zone.name(), tier);
            saveData();
        }
    }

    // ════════════════════════════════════════════════════════════
    // PARTY SYSTEM
    // ════════════════════════════════════════════════════════════
    
    public static boolean isInParty(String uuid) {
        return partyMembers.containsKey(uuid);
    }
    
    public static String getPartyLeader(String uuid) {
        return partyMembers.get(uuid);
    }
    
    public static Set<String> getPartyMembers(String leaderUuid) {
        Set<String> members = new HashSet<>();
        for (Map.Entry<String, String> entry : partyMembers.entrySet()) {
            if (entry.getValue().equals(leaderUuid)) {
                members.add(entry.getKey());
            }
        }
        members.add(leaderUuid);
        return members;
    }
    
    public static void inviteToParty(ServerPlayer leader, ServerPlayer invited) {
        String leaderUuid = leader.getUUID().toString();
        String invitedUuid = invited.getUUID().toString();
        
        // Check if already in a party
        if (isInParty(invitedUuid)) {
            leader.sendSystemMessage(Component.literal("That player is already in a party!").withStyle(ChatFormatting.RED), false);
            return;
        }
        
        partyInvites.put(invitedUuid, leaderUuid);
        leader.sendSystemMessage(Component.literal("Invited " + invited.getName().getString() + " to your party!").withStyle(ChatFormatting.GREEN), false);
        invited.sendSystemMessage(Component.literal(leader.getName().getString() + " invited you to their expedition party!").withStyle(ChatFormatting.AQUA), false);
        invited.sendSystemMessage(Component.literal("Type /expedition accept to join, or /expedition decline to refuse.").withStyle(ChatFormatting.GRAY), false);
        
        // Auto-decline after 60 seconds
        new Thread(() -> {
            try {
                Thread.sleep(60000);
                if (partyInvites.remove(invitedUuid) != null && partyInvites.get(invitedUuid) == null) {
                    PoliticalServer.server.execute(() -> {
                        leader.sendSystemMessage(Component.literal("Party invite to " + invited.getName().getString() + " expired.").withStyle(ChatFormatting.YELLOW), false);
                    });
                }
            } catch (InterruptedException ignored) {}
        }).start();
    }
    
    public static boolean acceptInvite(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        String leaderUuid = partyInvites.remove(uuid);
        
        if (leaderUuid == null) {
            player.sendSystemMessage(Component.literal("You have no pending party invites!").withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        partyMembers.put(uuid, leaderUuid);
        player.sendSystemMessage(Component.literal("You joined the party!").withStyle(ChatFormatting.GREEN), false);
        
        // Notify leader
        ServerPlayer leader = PoliticalServer.server.getPlayerList().getPlayer(UUID.fromString(leaderUuid));
        if (leader != null) {
            leader.sendSystemMessage(Component.literal(player.getName().getString() + " joined your party!").withStyle(ChatFormatting.GREEN), false);
        }
        
        return true;
    }
    
    public static void declineInvite(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        String leaderUuid = partyInvites.remove(uuid);
        
        if (leaderUuid == null) {
            player.sendSystemMessage(Component.literal("You have no pending party invites!").withStyle(ChatFormatting.RED), false);
            return;
        }
        
        player.sendSystemMessage(Component.literal("You declined the party invite.").withStyle(ChatFormatting.YELLOW), false);
    }
    
    public static void leaveParty(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        String leaderUuid = partyMembers.remove(uuid);
        
        if (leaderUuid == null) {
            player.sendSystemMessage(Component.literal("You are not in a party!").withStyle(ChatFormatting.RED), false);
            return;
        }
        
        player.sendSystemMessage(Component.literal("You left the party.").withStyle(ChatFormatting.YELLOW), false);
        
        // Notify leader
        ServerPlayer leader = PoliticalServer.server.getPlayerList().getPlayer(UUID.fromString(leaderUuid));
        if (leader != null) {
            leader.sendSystemMessage(Component.literal(player.getName().getString() + " left your party.").withStyle(ChatFormatting.YELLOW), false);
        }
    }
    
    public static void disbandParty(ServerPlayer leader) {
        String leaderUuid = leader.getUUID().toString();
        
        // Remove all members
        partyMembers.entrySet().removeIf(entry -> entry.getValue().equals(leaderUuid));
        
        // Cancel any active expedition
        activeExpeditions.remove(leaderUuid);
        
        leader.sendSystemMessage(Component.literal("Your party has been disbanded.").withStyle(ChatFormatting.YELLOW), false);
    }

    // ════════════════════════════════════════════════════════════
    // EXPEDITION START
    // ════════════════════════════════════════════════════════════
    
    public static boolean startExpedition(ServerPlayer leader, ExpeditionZone zone, ExpeditionTier tier) {
        String leaderUuid = leader.getUUID().toString();
        
        // Check mastery access
        if (!canAccessZone(leaderUuid, zone)) {
            leader.sendSystemMessage(Component.literal("You need higher mastery to access this zone!").withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        // Check lockout
        if (isOnLockout(leaderUuid, zone, tier)) {
            long remaining = getLockoutRemaining(leaderUuid, zone, tier);
            String timeStr = formatTime(remaining);
            leader.sendSystemMessage(Component.literal("You are on lockout for " + timeStr + "!").withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        // Check for all party members
        Set<String> members = getPartyMembers(leaderUuid);
        for (String memberUuid : members) {
            if (!canAccessZone(memberUuid, zone)) {
                ServerPlayer member = PoliticalServer.server.getPlayerList().getPlayer(UUID.fromString(memberUuid));
                String name = member != null ? member.getName().getString() : memberUuid;
                leader.sendSystemMessage(Component.literal(name + " doesn't have access to this zone!").withStyle(ChatFormatting.RED), false);
                return false;
            }
            if (isOnLockout(memberUuid, zone, tier)) {
                ServerPlayer member = PoliticalServer.server.getPlayerList().getPlayer(UUID.fromString(memberUuid));
                String name = member != null ? member.getName().getString() : memberUuid;
                long remaining = getLockoutRemaining(memberUuid, zone, tier);
                leader.sendSystemMessage(Component.literal(name + " is on lockout for " + formatTime(remaining) + "!").withStyle(ChatFormatting.RED), false);
                return false;
            }
        }
        
        // Create expedition run
        ExpeditionRun run = new ExpeditionRun(zone, tier, leaderUuid);
        run.members.addAll(members);
        run.nodesRemaining = tier.nodeCount;
        
        // Generate expedition area (in a separate dimension or isolated area)
        ServerLevel world = generateExpeditionArea(leader, zone, tier, run);
        if (world == null) {
            leader.sendSystemMessage(Component.literal("Failed to create expedition area!").withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        // Teleport all members
        for (String memberUuid : members) {
            ServerPlayer member = PoliticalServer.server.getPlayerList().getPlayer(UUID.fromString(memberUuid));
            if (member != null) {
                PlayerTeleportHelper.teleport(member, world, run.centerPos.getX() + 0.5, run.centerPos.getY(), run.centerPos.getZ() + 0.5, 0, 0);
                member.sendSystemMessage(Component.literal("Expedition started! " + zone.displayName + " - " + tier.displayName).withStyle(ChatFormatting.GOLD), false);
            }
        }
        
        // Start first wave
        activeExpeditions.put(leaderUuid, run);
        startWave(leader, run, 1);
        
        return true;
    }
    
    private static ServerLevel generateExpeditionArea(ServerPlayer player, ExpeditionZone zone, ExpeditionTier tier, ExpeditionRun run) {
        // Get or create expedition dimension
        ServerLevel world = getOrCreateExpeditionWorld(PoliticalServer.server, zone);
        if (world == null) return null;
        
        // Find a safe spawn location
        BlockPos spawnPos = findSafeSpawnLocation(world, zone);
        run.centerPos = spawnPos;
        run.worldId = world.dimension().identifier().toString();
        
        // Generate arena
        generateArena(world, spawnPos, zone, tier);
        
        // Generate ore nodes
        generateNodes(world, spawnPos, zone, tier, run);
        
        return world;
    }
    
    private static ServerLevel getOrCreateExpeditionWorld(net.minecraft.server.MinecraftServer server, ExpeditionZone zone) {
        // Use the end or nether based on zone type, or create isolated areas in overworld
        return switch (zone) {
            case ASHEN_MINES -> server.getLevel(net.minecraft.world.level.Level.NETHER);
            case DROWNED_ARCHIVES -> server.getLevel(net.minecraft.world.level.Level.OVERWORLD);
            case GLACIAL_SPIRE -> server.getLevel(net.minecraft.world.level.Level.OVERWORLD);
            case VOID_RIFT -> server.getLevel(net.minecraft.world.level.Level.END);
            case DEEP_SEAM -> server.getLevel(net.minecraft.world.level.Level.OVERWORLD);
        };
    }
    
    private static BlockPos findSafeSpawnLocation(ServerLevel world, ExpeditionZone zone) {
        // Find a location far from spawn
        Random random = new Random();
        int x = random.nextInt(20000) - 10000;
        int z = random.nextInt(20000) - 10000;
        
        BlockPos topPos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
        
        // Adjust Y based on zone type
        int y = switch (zone) {
            case ASHEN_MINES -> Math.min(topPos.getY(), 30); // Deep underground
            case DROWNED_ARCHIVES -> Math.max(topPos.getY(), 62); // Underwater
            case GLACIAL_SPIRE -> Math.max(topPos.getY() + 50, 100); // High up
            case VOID_RIFT -> topPos.getY() - 20; // Underground
            case DEEP_SEAM -> Math.min(topPos.getY(), -30); // Very deep
        };
        
        return new BlockPos(x, y, z);
    }
    
    private static void generateArena(ServerLevel world, BlockPos center, ExpeditionZone zone, ExpeditionTier tier) {
        int radius = 20 + (tier.tier * 5);
        
        // Clear area
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = -10; y <= 10; y++) {
                    BlockPos pos = center.offset(x, y, z);
                    if (x * x + z * z <= radius * radius) {
                        // Floor and ceiling
                        if (y == -10 || y == 10) {
                            world.setBlock(pos, getZoneBlock(zone).defaultBlockState(), Block.UPDATE_ALL);
                        } else {
                            world.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                        }
                    }
                }
            }
        }
        
        // Add zone-specific features
        addZoneFeatures(world, center, zone, tier);
    }
    
    private static Block getZoneBlock(ExpeditionZone zone) {
        return switch (zone) {
            case ASHEN_MINES -> Blocks.NETHERRACK;
            case DROWNED_ARCHIVES -> Blocks.PRISMARINE;
            case GLACIAL_SPIRE -> Blocks.PACKED_ICE;
            case VOID_RIFT -> Blocks.END_STONE;
            case DEEP_SEAM -> Blocks.DEEPSLATE;
        };
    }
    
    private static void addZoneFeatures(ServerLevel world, BlockPos center, ExpeditionZone zone, ExpeditionTier tier) {
        // Add decorative features based on zone
        // This is simplified - in a full implementation, you'd have more complex generation
    }
    
    private static void generateNodes(ServerLevel world, BlockPos center, ExpeditionZone zone, ExpeditionTier tier, ExpeditionRun run) {
        Random random = new Random();
        int nodeCount = tier.nodeCount;
        
        for (int i = 0; i < nodeCount; i++) {
            int angle = random.nextInt(360);
            int distance = 5 + random.nextInt(15);
            int x = center.getX() + (int) (Math.cos(Math.toRadians(angle)) * distance);
            int z = center.getZ() + (int) (Math.sin(Math.toRadians(angle)) * distance);
            int y = center.getY() + random.nextInt(10) - 5;
            
            BlockPos nodePos = new BlockPos(x, y, z);
            
            // Place node block
            NodeType nodeType = getRandomNodeType(zone, tier, random);
            world.setBlock(nodePos, getBlockForNode(nodeType).defaultBlockState(), Block.UPDATE_ALL);
            
            run.nodePositions.add(nodePos);
        }
    }
    
    private static NodeType getRandomNodeType(ExpeditionZone zone, ExpeditionTier tier, Random random) {
        double roll = random.nextDouble() * tier.difficultyMultiplier;
        
        if (roll > 2.5) return NodeType.MASTERY_CORE;
        if (roll > 2.0) return NodeType.ANCIENT_ORE;
        if (roll > 1.5) return NodeType.VOID_CRYSTAL;
        if (roll > 1.0) return NodeType.EMERALD_NODE;
        if (roll > 0.7) return NodeType.DIAMOND_NODE;
        if (roll > 0.4) return NodeType.GOLD_NODE;
        if (roll > 0.2) return NodeType.IRON_NODE;
        return NodeType.COAL_NODE;
    }
    
    private static Block getBlockForNode(NodeType type) {
        return switch (type) {
            case COAL_NODE -> Blocks.COAL_ORE;
            case IRON_NODE -> Blocks.IRON_ORE;
            case GOLD_NODE -> Blocks.GOLD_ORE;
            case DIAMOND_NODE -> Blocks.DIAMOND_ORE;
            case EMERALD_NODE -> Blocks.EMERALD_ORE;
            case VOID_CRYSTAL -> Blocks.AMETHYST_CLUSTER;
            case ANCIENT_ORE -> Blocks.ANCIENT_DEBRIS;
            case MASTERY_CORE -> Blocks.BEACON;
        };
    }

    // ════════════════════════════════════════════════════════════
    // WAVE SYSTEM
    // ════════════════════════════════════════════════════════════
    
    private static void startWave(ServerPlayer player, ExpeditionRun run, int wave) {
        run.currentWave = wave;
        
        // Notify players
        for (String memberUuid : run.members) {
            ServerPlayer member = PoliticalServer.server.getPlayerList().getPlayer(UUID.fromString(memberUuid));
            if (member != null) {
                member.sendSystemMessage(Component.literal("§c⚔ Wave " + wave + "/" + run.tier.waveCount + " starting!").withStyle(ChatFormatting.RED), false);
            }
        }
        
        // Spawn mobs based on zone and tier
        spawnWaveMobs(player, run, wave);
    }
    
    private static void spawnWaveMobs(ServerPlayer player, ExpeditionRun run, int wave) {
        ServerLevel world = PoliticalServer.server.getLevel(net.minecraft.world.level.Level.OVERWORLD);
        if (world == null) return;
        
        // Determine mob type based on zone
        EntityType<? extends Monster> mobType = getZoneMobType(run.zone);
        
        int mobCount = (int) (3 + wave * run.tier.difficultyMultiplier);
        
        Random random = new Random();
        for (int i = 0; i < mobCount; i++) {
            int angle = random.nextInt(360);
            int distance = 15 + random.nextInt(10);
            double x = run.centerPos.getX() + Math.cos(Math.toRadians(angle)) * distance;
            double z = run.centerPos.getZ() + Math.sin(Math.toRadians(angle)) * distance;
            double y = run.centerPos.getY();
            
            BlockPos spawnPos = new BlockPos((int) x, (int) y, (int) z);
            
            mobType.spawn(world, spawnPos, EntitySpawnReason.EVENT);
        }
    }
    
    private static EntityType<? extends Monster> getZoneMobType(ExpeditionZone zone) {
        return switch (zone) {
            case ASHEN_MINES -> EntityType.BLAZE;
            case DROWNED_ARCHIVES -> EntityType.DROWNED;
            case GLACIAL_SPIRE -> EntityType.STRAY;
            case VOID_RIFT -> EntityType.ENDERMITE;
            case DEEP_SEAM -> EntityType.WARDEN;
        };
    }
    
    public static void onWaveComplete(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        ExpeditionRun run = activeExpeditions.get(getPartyLeader(uuid));
        if (run == null) run = activeExpeditions.get(uuid);
        if (run == null) return;
        
        if (run.currentWave >= run.tier.waveCount) {
            // All waves complete - mining phase
            for (String memberUuid : run.members) {
                ServerPlayer member = PoliticalServer.server.getPlayerList().getPlayer(UUID.fromString(memberUuid));
                if (member != null) {
                    member.sendSystemMessage(Component.literal("§a✓ All waves cleared! Mine the nodes before time runs out!").withStyle(ChatFormatting.GREEN), false);
                }
            }
        } else {
            // Start next wave
            startWave(player, run, run.currentWave + 1);
        }
    }
    
    public static void onNodeMined(ServerPlayer player, BlockPos pos) {
        String uuid = player.getUUID().toString();
        ExpeditionRun run = activeExpeditions.get(getPartyLeader(uuid));
        if (run == null) run = activeExpeditions.get(uuid);
        if (run == null) return;
        
        if (run.nodePositions.remove(pos)) {
            run.nodesRemaining--;
            
            // Give rewards
            giveNodeReward(player, run.zone, run.tier);
            
            // Check if all nodes mined
            if (run.nodesRemaining <= 0) {
                completeExpedition(player, run);
            }
        }
    }
    
    private static void giveNodeReward(ServerPlayer player, ExpeditionZone zone, ExpeditionTier tier) {
        Random random = new Random();
        NodeType type = getRandomNodeType(zone, tier, random);
        
        int amount = type.minAmount + random.nextInt(type.maxAmount - type.minAmount + 1);
        amount = (int) (amount * tier.difficultyMultiplier);
        
        ItemStack drop = new ItemStack(type.dropItem, amount);
        player.addItem(drop);
        
        player.sendSystemMessage(Component.literal("§6+ " + amount + " " + type.displayName).withStyle(ChatFormatting.GOLD), false);
        
        // Award mastery XP
        MasteryManager.addXp(player, MasteryManager.MasteryFamily.GATHERING, 20 * tier.tier);
    }
    
    private static void completeExpedition(ServerPlayer player, ExpeditionRun run) {
        // Apply lockouts
        for (String memberUuid : run.members) {
            applyLockout(memberUuid, run.zone, run.tier);
            setBestTier(memberUuid, run.zone, run.tier.tier);
        }
        
        // Teleport out
        for (String memberUuid : run.members) {
            ServerPlayer member = PoliticalServer.server.getPlayerList().getPlayer(UUID.fromString(memberUuid));
            if (member != null) {
                PlayerTeleportHelper.teleport(member, PoliticalServer.server.overworld(), 0, 64, 0, 0, 0);
                member.sendSystemMessage(Component.literal("§6★ Expedition Complete! " + run.zone.displayName + " - " + run.tier.displayName).withStyle(ChatFormatting.GOLD), false);
            }
        }
        
        // Remove active expedition
        activeExpeditions.remove(run.leaderUuid);
    }

    // ════════════════════════════════════════════════════════════
    // UTILITY
    // ════════════════════════════════════════════════════════════
    
    private static String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        } else {
            return seconds + "s";
        }
    }
    
    public static ExpeditionRun getActiveExpedition(String uuid) {
        String leader = getPartyLeader(uuid);
        if (leader != null) {
            return activeExpeditions.get(leader);
        }
        return activeExpeditions.get(uuid);
    }
    
    public static boolean isPlayerInExpedition(String uuid) {
        return getActiveExpedition(uuid) != null;
    }
}
