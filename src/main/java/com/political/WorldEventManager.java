package com.political;


import com.political.bounty.SlayerManager;
import com.political.economy.ShopManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * World Events System - Pillar 3
 * 
 * Server-wide events that trigger periodically and affect all players.
 * Events can be positive (bonuses) or negative (challenges).
 * Players can vote to trigger or suppress certain events.
 */
public class WorldEventManager {

    // ════════════════════════════════════════════════════════════
    // EVENT TYPES
    // ════════════════════════════════════════════════════════════
    
    public enum WorldEvent {
        // POSITIVE EVENTS
        PROSPERITY_SURGE("Prosperity Surge", "💰", ChatFormatting.GOLD, true, 600,
                "All shop sell prices increased by 50% for 10 minutes!"),
        MINING_BOOM("Mining Boom", "⛏", ChatFormatting.AQUA, true, 600,
                "Double ore drops for 10 minutes!"),
        BOUNTY_BONUS("Bounty Bonus", "⚔", ChatFormatting.RED, true, 600,
                "Bounty rewards doubled for 10 minutes!"),
        XP_FRENZY("XP Frenzy", "✨", ChatFormatting.LIGHT_PURPLE, true, 600,
                "Double XP from all sources for 10 minutes!"),
        LOOT_FEST("Loot Fest", "🎁", ChatFormatting.YELLOW, true, 600,
                "Increased rare loot drops for 10 minutes!"),
        STOCK_RALLY("Stock Rally", "📈", ChatFormatting.GREEN, true, 600,
                "All stocks gain +10% value over 10 minutes!"),
        
        // NEGATIVE EVENTS (Challenges)
        MONSTER_SWARM("Monster Swarm", "👹", ChatFormatting.DARK_RED, false, 300,
                "Increased hostile mob spawns for 5 minutes!"),
        ECONOMIC_CRASH("Economic Crash", "📉", ChatFormatting.DARK_GRAY, false, 300,
                "Shop buy prices increased by 25% for 5 minutes!"),
        CURSED_LANDS("Cursed Lands", "☠", ChatFormatting.DARK_PURPLE, false, 300,
                "Mobs deal +25% damage for 5 minutes!"),
        RESOURCE_DROUGHT("Resource Drought", "🌵", ChatFormatting.DARK_GREEN, false, 300,
                "Reduced ore spawn rates for 5 minutes!"),
        
        // SPECIAL EVENTS
        METEOR_SHOWER("Meteor Shower", "☄", ChatFormatting.LIGHT_PURPLE, false, 180,
                "Meteors fall from the sky! Collect rare resources!"),
        TREASURE_HUNT("Treasure Hunt", "🗺", ChatFormatting.GOLD, false, 600,
                "Hidden treasure chests spawn around the world!"),
        BOSS_INVASION("Boss Invasion", "🐉", ChatFormatting.DARK_RED, false, 600,
                "World bosses spawn in random locations!"),
        VOID_RIFT_EVENT("Void Rift", "🌀", ChatFormatting.DARK_PURPLE, false, 300,
                "Void creatures invade from rifts! Defeat them for rewards!");

        public final String displayName;
        public final String icon;
        public final ChatFormatting color;
        public final boolean isPositive;
        public final int durationSeconds;
        public final String description;

        WorldEvent(String displayName, String icon, ChatFormatting color, boolean isPositive,
                  int durationSeconds, String description) {
            this.displayName = displayName;
            this.icon = icon;
            this.color = color;
            this.isPositive = isPositive;
            this.durationSeconds = durationSeconds;
            this.description = description;
        }
    }

    // ════════════════════════════════════════════════════════════
    // ACTIVE EVENT DATA
    // ════════════════════════════════════════════════════════════
    
    public static class ActiveEvent {
        public WorldEvent event;
        public long startTime;
        public long endTime;
        public Map<String, Integer> playerContributions = new ConcurrentHashMap<>();
        public boolean isVoting = false;
        public int votesFor = 0;
        public int votesAgainst = 0;
        public Set<String> votedPlayers = new HashSet<>();
        
        public ActiveEvent(WorldEvent event) {
            this.event = event;
            this.startTime = System.currentTimeMillis();
            this.endTime = startTime + (event.durationSeconds * 1000L);
        }
        
        public int getRemainingSeconds() {
            long remaining = endTime - System.currentTimeMillis();
            return Math.max(0, (int) (remaining / 1000));
        }
        
        public boolean isActive() {
            return System.currentTimeMillis() < endTime;
        }
    }
    
    // Currently active event
    private static ActiveEvent currentEvent = null;
    
    // Event history: event name -> count
    private static Map<String, Integer> eventHistory = new ConcurrentHashMap<>();
    
    // Event vote pending
    private static ActiveEvent pendingVote = null;
    
    // File path for persistence
    private static Path dataFile;
    
    // Event scheduler
    private static Timer eventTimer;
    
    // Minimum interval between events (in milliseconds)
    private static final long MIN_EVENT_INTERVAL = 30 * 60 * 1000L; // 30 minutes
    private static long lastEventTime = 0;

    // ════════════════════════════════════════════════════════════
    // INITIALIZATION & PERSISTENCE
    // ════════════════════════════════════════════════════════════
    
    public static void initialize(Path dataDir) {
        dataFile = dataDir.resolve("world_events.json");
        loadData();
        startEventScheduler();
    }
    
    private static void loadData() {
        if (dataFile == null || !Files.exists(dataFile)) return;
        
        try (Reader reader = Files.newBufferedReader(dataFile)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> loaded = gson.fromJson(reader, type);
            
            if (loaded != null) {
                @SuppressWarnings("unchecked")
                Map<String, Double> history = (Map<String, Double>) loaded.get("history");
                if (history != null) {
                    for (Map.Entry<String, Double> entry : history.entrySet()) {
                        eventHistory.put(entry.getKey(), entry.getValue().intValue());
                    }
                }
                
                Double lastEvent = (Double) loaded.get("lastEventTime");
                if (lastEvent != null) {
                    lastEventTime = lastEvent.longValue();
                }
            }
        } catch (Exception e) {
            System.err.println("[WorldEventManager] Failed to load data: " + e.getMessage());
        }
    }
    
    public static void saveData() {
        if (dataFile == null) return;
        
        try {
            Files.createDirectories(dataFile.getParent());
            
            Map<String, Object> toSave = new HashMap<>();
            toSave.put("history", eventHistory);
            toSave.put("lastEventTime", lastEventTime);
            
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (Writer writer = Files.newBufferedWriter(dataFile)) {
                gson.toJson(toSave, writer);
            }
        } catch (Exception e) {
            System.err.println("[WorldEventManager] Failed to save data: " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════
    // EVENT SCHEDULER
    // ════════════════════════════════════════════════════════════
    
    private static void startEventScheduler() {
        if (eventTimer != null) {
            eventTimer.cancel();
        }
        
        eventTimer = new Timer("WorldEventScheduler", true);
        
        // Check every minute for event triggers
        eventTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (PoliticalServer.server == null) return;
                
                PoliticalServer.server.execute(() -> {
                    // Check if current event has ended
                    if (currentEvent != null && !currentEvent.isActive()) {
                        endCurrentEvent();
                    }
                    
                    // Maybe trigger a random event
                    if (currentEvent == null && shouldTriggerEvent()) {
                        triggerRandomEvent();
                    }
                });
            }
        }, 60000, 60000); // Check every minute
    }
    
    private static boolean shouldTriggerEvent() {
        // Check minimum interval
        if (System.currentTimeMillis() - lastEventTime < MIN_EVENT_INTERVAL) {
            return false;
        }
        
        // Random chance (5% per check = roughly every 20 minutes on average)
        return new Random().nextDouble() < 0.05;
    }
    
    private static void triggerRandomEvent() {
        WorldEvent[] events = WorldEvent.values();
        WorldEvent randomEvent = events[new Random().nextInt(events.length)];
        
        triggerEvent(randomEvent);
    }

    // ════════════════════════════════════════════════════════════
    // EVENT TRIGGERING
    // ════════════════════════════════════════════════════════════
    
    public static void triggerEvent(WorldEvent event) {
        if (currentEvent != null && currentEvent.isActive()) {
            return; // Already an event running
        }
        
        currentEvent = new ActiveEvent(event);
        lastEventTime = System.currentTimeMillis();
        
        // Update history
        eventHistory.merge(event.name(), 1, Integer::sum);
        saveData();
        
        // Broadcast to all players
        broadcastEventStart(event);
        
        // Apply event effects
        applyEventEffects(event);
    }
    
    private static void broadcastEventStart(WorldEvent event) {
        if (PoliticalServer.server == null) return;
        
        String prefix = event.isPositive ? "§a✦ POSITIVE EVENT" : "§c✦ CHALLENGE EVENT";
        
        for (ServerPlayer player : PoliticalServer.server.getPlayerList().getPlayers()) {
            player.sendSystemMessage(Component.literal(""), false);
            player.sendSystemMessage(Component.literal(prefix + " §r" + event.icon + " §l" + event.displayName)
                    .withStyle(event.color), false);
            player.sendSystemMessage(Component.literal(event.description).withStyle(ChatFormatting.GRAY), false);
            player.sendSystemMessage(Component.literal("Duration: " + formatTime(event.durationSeconds * 1000L))
                    .withStyle(ChatFormatting.YELLOW), false);
            player.sendSystemMessage(Component.literal(""), false);
        }
    }
    
    private static void endCurrentEvent() {
        if (currentEvent == null) return;
        
        WorldEvent event = currentEvent.event;
        
        // Broadcast end
        if (PoliticalServer.server != null) {
            for (ServerPlayer player : PoliticalServer.server.getPlayerList().getPlayers()) {
                player.sendSystemMessage(Component.literal("§7✦ Event ended: " + event.icon + " " + event.displayName)
                        .withStyle(ChatFormatting.GRAY), false);
            }
        }
        
        // Remove effects
        removeEventEffects(event);
        
        currentEvent = null;
    }

    // ════════════════════════════════════════════════════════════
    // EVENT EFFECTS
    // ════════════════════════════════════════════════════════════
    
    private static void applyEventEffects(WorldEvent event) {
        switch (event) {
            case PROSPERITY_SURGE -> {
                // Handled in ShopManager.getSellPrice()
            }
            case MINING_BOOM -> {
                // Handled in block break handler
            }
            case BOUNTY_BONUS -> {
                // Handled in SlayerManager
            }
            case XP_FRENZY -> {
                // Handled in MasteryManager
            }
            case LOOT_FEST -> {
                // Handled in drop handlers
            }
            case STOCK_RALLY -> {
                // Handled in StockBrokerManager
            }
            case MONSTER_SWARM -> {
                // Spawn extra mobs
                scheduleMobSpawns();
            }
            case ECONOMIC_CRASH -> {
                // Handled in ShopManager.getBuyPrice()
            }
            case CURSED_LANDS -> {
                // Handled in damage handler
            }
            case RESOURCE_DROUGHT -> {
                // Handled in ore generation
            }
            case METEOR_SHOWER -> {
                scheduleMeteorShower();
            }
            case TREASURE_HUNT -> {
                spawnTreasureChests();
            }
            case BOSS_INVASION -> {
                spawnWorldBosses();
            }
            case VOID_RIFT_EVENT -> {
                spawnVoidRifts();
            }
        }
    }
    
    private static void removeEventEffects(WorldEvent event) {
        // Most effects are time-based and handled by checking isActive()
        // Just need to clean up any spawned entities or special states
    }

    // ════════════════════════════════════════════════════════════
    // SPECIAL EVENT HANDLERS
    // ════════════════════════════════════════════════════════════
    
    private static void scheduleMobSpawns() {
        if (PoliticalServer.server == null) return;
        
        Timer mobTimer = new Timer("MonsterSwarm", true);
        mobTimer.scheduleAtFixedRate(new TimerTask() {
            int spawns = 0;
            final int maxSpawns = 10;
            
            @Override
            public void run() {
                if (currentEvent == null || currentEvent.event != WorldEvent.MONSTER_SWARM || spawns >= maxSpawns) {
                    mobTimer.cancel();
                    return;
                }
                
                PoliticalServer.server.execute(() -> {
                    ServerLevel world = PoliticalServer.server.overworld();
                    if (world == null) return;
                    
                    // Spawn mobs near each player
                    for (ServerPlayer player : world.players()) {
                        Random random = new Random();
                        int angle = random.nextInt(360);
                        int distance = 20 + random.nextInt(20);
                        double x = player.getX() + Math.cos(Math.toRadians(angle)) * distance;
                        double z = player.getZ() + Math.sin(Math.toRadians(angle)) * distance;
                        
                        BlockPos spawnPos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                                new BlockPos((int) x, 0, (int) z));
                        
                        EntityType<? extends Monster> mobType = random.nextBoolean() ? EntityType.ZOMBIE : EntityType.SKELETON;
                        mobType.spawn(world, spawnPos, EntitySpawnReason.EVENT);
                    }
                });
                spawns++;
            }
        }, 30000, 30000); // Every 30 seconds
    }
    
    private static void scheduleMeteorShower() {
        if (PoliticalServer.server == null) return;
        
        Timer meteorTimer = new Timer("MeteorShower", true);
        meteorTimer.scheduleAtFixedRate(new TimerTask() {
            int meteors = 0;
            final int maxMeteors = 20;
            
            @Override
            public void run() {
                if (currentEvent == null || currentEvent.event != WorldEvent.METEOR_SHOWER || meteors >= maxMeteors) {
                    meteorTimer.cancel();
                    return;
                }
                
                PoliticalServer.server.execute(() -> {
                    ServerLevel world = PoliticalServer.server.overworld();
                    if (world == null) return;
                    
                    Random random = new Random();
                    int x = random.nextInt(2000) - 1000;
                    int z = random.nextInt(2000) - 1000;
                    
                    // Create meteor impact
                    BlockPos impactPos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                            new BlockPos(x, 0, z));
                    
                    // Drop rare items
                    ItemStack drop = switch (random.nextInt(5)) {
                        case 0 -> new ItemStack(Items.DIAMOND, 1 + random.nextInt(3));
                        case 1 -> new ItemStack(Items.EMERALD, 1 + random.nextInt(5));
                        case 2 -> new ItemStack(Items.GOLD_INGOT, 3 + random.nextInt(7));
                        case 3 -> new ItemStack(Items.IRON_INGOT, 5 + random.nextInt(10));
                        default -> new ItemStack(Items.NETHERITE_SCRAP, 1);
                    };
                    
                    net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(
                            world, impactPos.getX(), impactPos.getY() + 1, impactPos.getZ(), drop);
                    world.addFreshEntity(itemEntity);
                    
                    // Notify nearby players
                    for (ServerPlayer player : world.players()) {
                        if (player.blockPosition().distManhattan(impactPos) < 200) {
                            player.sendSystemMessage(Component.literal("☄ Meteor landed nearby! Check coordinates: " +
                                    impactPos.getX() + ", " + impactPos.getZ()).withStyle(ChatFormatting.LIGHT_PURPLE), false);
                        }
                    }
                });
                meteors++;
            }
        }, 15000, 15000); // Every 15 seconds
    }
    
    private static void spawnTreasureChests() {
        if (PoliticalServer.server == null) return;
        
        ServerLevel world = PoliticalServer.server.overworld();
        if (world == null) return;
        
        Random random = new Random();
        int chestCount = 10 + random.nextInt(10);
        
        for (int i = 0; i < chestCount; i++) {
            int x = random.nextInt(5000) - 2500;
            int z = random.nextInt(5000) - 2500;
            
            BlockPos chestPos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    new BlockPos(x, 0, z));
            
            // Place chest
            world.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
            
            // Fill with loot
            if (world.getBlockEntity(chestPos) instanceof net.minecraft.world.level.block.entity.ChestBlockEntity chest) {
                // Add random valuable items
                chest.setItem(0, new ItemStack(Items.DIAMOND, 1 + random.nextInt(5)));
                chest.setItem(1, new ItemStack(Items.GOLD_INGOT, 5 + random.nextInt(10)));
                chest.setItem(2, new ItemStack(Items.EMERALD, 3 + random.nextInt(7)));
                
                if (random.nextDouble() < 0.1) {
                    chest.setItem(3, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 1));
                }
            }
        }
        
        // Broadcast hint
        for (ServerPlayer player : PoliticalServer.server.getPlayerList().getPlayers()) {
            player.sendSystemMessage(Component.literal("🗺 Treasure chests have been hidden around the world! Find them for rewards!")
                    .withStyle(ChatFormatting.GOLD), false);
        }
    }
    
    private static void spawnWorldBosses() {
        if (PoliticalServer.server == null) return;
        
        ServerLevel world = PoliticalServer.server.overworld();
        if (world == null) return;
        
        Random random = new Random();
        int bossCount = 3;
        
        for (int i = 0; i < bossCount; i++) {
            int x = random.nextInt(2000) - 1000;
            int z = random.nextInt(2000) - 1000;
            
            BlockPos spawnPos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    new BlockPos(x, 0, z));
            
            // Spawn a boss-level mob
            EntityType<? extends Monster> bossType = switch (random.nextInt(4)) {
                case 0 -> EntityType.RAVAGER;
                case 1 -> EntityType.WARDEN;
                case 2 -> EntityType.WITHER;
                default -> EntityType.ELDER_GUARDIAN;
            };
            
            var boss = bossType.spawn(world, spawnPos, EntitySpawnReason.EVENT);
            
            // Boost boss stats
            if (boss != null) {
                // Apply buffs
                // In a full implementation, you'd add custom attributes here
            }
        }
        
        // Broadcast
        for (ServerPlayer player : PoliticalServer.server.getPlayerList().getPlayers()) {
            player.sendSystemMessage(Component.literal("🐉 World bosses have spawned! Defeat them for legendary rewards!")
                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), false);
        }
    }
    
    private static void spawnVoidRifts() {
        if (PoliticalServer.server == null) return;
        
        ServerLevel world = PoliticalServer.server.overworld();
        if (world == null) return;
        
        Random random = new Random();
        int riftCount = 5;
        
        for (int i = 0; i < riftCount; i++) {
            int x = random.nextInt(1000) - 500;
            int z = random.nextInt(1000) - 500;
            
            BlockPos riftPos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    new BlockPos(x, 0, z));
            
            // Create end portal frame as rift marker
            world.setBlock(riftPos, Blocks.END_PORTAL_FRAME.defaultBlockState(), Block.UPDATE_ALL);
            
            // Spawn endermen around it
            for (int j = 0; j < 5; j++) {
                EntityType.ENDERMAN.spawn(world, riftPos.offset(random.nextInt(10) - 5, 0, random.nextInt(10) - 5), EntitySpawnReason.EVENT);
            }
        }
        
        // Broadcast
        for (ServerPlayer player : PoliticalServer.server.getPlayerList().getPlayers()) {
            player.sendSystemMessage(Component.literal("🌀 Void rifts have opened! End creatures are invading!")
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), false);
        }
    }

    // ════════════════════════════════════════════════════════════
    // EVENT VOTING
    // ════════════════════════════════════════════════════════════
    
    public static void startVote(WorldEvent event) {
        if (pendingVote != null || currentEvent != null) return;
        
        pendingVote = new ActiveEvent(event);
        pendingVote.isVoting = true;
        
        // Broadcast vote
        if (PoliticalServer.server != null) {
            for (ServerPlayer player : PoliticalServer.server.getPlayerList().getPlayers()) {
                player.sendSystemMessage(Component.literal(""), false);
                player.sendSystemMessage(Component.literal("§e⚖ VOTE STARTED: " + event.icon + " " + event.displayName)
                        .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD), false);
                player.sendSystemMessage(Component.literal(event.description).withStyle(ChatFormatting.GRAY), false);
                player.sendSystemMessage(Component.literal("Use /event vote yes or /event vote no")
                        .withStyle(ChatFormatting.AQUA), false);
                player.sendSystemMessage(Component.literal(""), false);
            }
        }
        
        // End vote after 60 seconds
        new Timer("EventVote", true).schedule(new TimerTask() {
            @Override
            public void run() {
                if (pendingVote == null) return;
                
                PoliticalServer.server.execute(() -> {
                    if (pendingVote.votesFor > pendingVote.votesAgainst) {
                        triggerEvent(pendingVote.event);
                    } else {
                        for (ServerPlayer player : PoliticalServer.server.getPlayerList().getPlayers()) {
                            player.sendSystemMessage(Component.literal("§7Vote failed for " + pendingVote.event.displayName)
                                    .withStyle(ChatFormatting.GRAY), false);
                        }
                    }
                    pendingVote = null;
                });
            }
        }, 60000);
    }
    
    public static boolean vote(ServerPlayer player, boolean yes) {
        if (pendingVote == null || !pendingVote.isVoting) {
            player.sendSystemMessage(Component.literal("No vote is currently active!").withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        String uuid = player.getUUID().toString();
        if (pendingVote.votedPlayers.contains(uuid)) {
            player.sendSystemMessage(Component.literal("You have already voted!").withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        pendingVote.votedPlayers.add(uuid);
        if (yes) {
            pendingVote.votesFor++;
            player.sendSystemMessage(Component.literal("You voted YES!").withStyle(ChatFormatting.GREEN), false);
        } else {
            pendingVote.votesAgainst++;
            player.sendSystemMessage(Component.literal("You voted NO!").withStyle(ChatFormatting.RED), false);
        }
        
        return true;
    }

    // ════════════════════════════════════════════════════════════
    // EVENT BONUSES (Called by other systems)
    // ════════════════════════════════════════════════════════════
    
    public static double getSellPriceMultiplier() {
        if (currentEvent != null && currentEvent.isActive() && currentEvent.event == WorldEvent.PROSPERITY_SURGE) {
            return 1.5;
        }
        return 1.0;
    }
    
    public static double getBuyPriceMultiplier() {
        if (currentEvent != null && currentEvent.isActive() && currentEvent.event == WorldEvent.ECONOMIC_CRASH) {
            return 1.25;
        }
        return 1.0;
    }
    
    public static double getBountyRewardMultiplier() {
        if (currentEvent != null && currentEvent.isActive() && currentEvent.event == WorldEvent.BOUNTY_BONUS) {
            return 2.0;
        }
        return 1.0;
    }
    
    public static double getXpMultiplier() {
        if (currentEvent != null && currentEvent.isActive() && currentEvent.event == WorldEvent.XP_FRENZY) {
            return 2.0;
        }
        return 1.0;
    }
    
    public static double getLootChanceBonus() {
        if (currentEvent != null && currentEvent.isActive() && currentEvent.event == WorldEvent.LOOT_FEST) {
            return 0.25; // +25% chance
        }
        return 0;
    }
    
    public static double getMobDamageMultiplier() {
        if (currentEvent != null && currentEvent.isActive() && currentEvent.event == WorldEvent.CURSED_LANDS) {
            return 1.25;
        }
        return 1.0;
    }
    
    public static double getMiningDropMultiplier() {
        if (currentEvent != null && currentEvent.isActive() && currentEvent.event == WorldEvent.MINING_BOOM) {
            return 2.0;
        }
        return 1.0;
    }
    
    public static double getStockGrowthBonus() {
        if (currentEvent != null && currentEvent.isActive() && currentEvent.event == WorldEvent.STOCK_RALLY) {
            return 0.10;
        }
        return 0;
    }

    // ════════════════════════════════════════════════════════════
    // ACCESSORS
    // ════════════════════════════════════════════════════════════
    
    public static ActiveEvent getCurrentEvent() {
        return currentEvent;
    }
    
    public static boolean isEventActive() {
        return currentEvent != null && currentEvent.isActive();
    }
    
    public static WorldEvent getActiveEventType() {
        return currentEvent != null ? currentEvent.event : null;
    }
    
    public static Map<String, Integer> getEventHistory() {
        return new HashMap<>(eventHistory);
    }
    
    public static ActiveEvent getPendingVote() {
        return pendingVote;
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
}
