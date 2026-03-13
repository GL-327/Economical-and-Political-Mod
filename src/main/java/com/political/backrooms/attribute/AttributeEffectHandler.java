package com.political.backrooms.attribute;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OPTIMIZED: Handles tick-based armor attribute effects for all online players.
 * Performance improvements to reduce server lag.
 */
public class AttributeEffectHandler {

    private static final Identifier NIGHTSTALKER_SPEED_ID =
        Identifier.of("politicalserver", "nightstalker_speed");
    private static final Identifier HEAVYWEIGHT_SPEED_ID =
        Identifier.of("politicalserver", "heavyweight_speed");

    /** Each armor piece contributes this fraction of the stated bonus. (4 pieces = full bonus) */
    private static final double PER_PIECE_SCALE = 0.25;
    
    // Performance optimization: Cache player states to avoid redundant calculations
    private static final Map<String, PlayerAttributeCache> playerCache = new ConcurrentHashMap<>();
    private static long lastCleanupTime = 0;
    
    // Performance optimization: Process every 100 ticks (5 seconds) instead of every second
    private static final int PROCESS_INTERVAL = 100;

    public static void onServerTick(MinecraftServer server) {
        long tick = server.getTicks();
        // Process every 100 ticks (5 seconds) to reduce overhead
        if (tick % PROCESS_INTERVAL != 0) return;

        // Cleanup cache every 5 minutes (6000 ticks)
        if (tick - lastCleanupTime > 6000) {
            cleanupCache();
            lastCleanupTime = tick;
        }

        for (ServerWorld world : server.getWorlds()) {
            List<ServerPlayerEntity> players = world.getPlayers();
            for (ServerPlayerEntity player : players) {
                processPlayerOptimized(player, world, tick);
            }
        }
    }

    private static void processPlayerOptimized(ServerPlayerEntity player, ServerWorld world, long tick) {
        String playerKey = player.getUuidAsString();
        PlayerAttributeCache cache = playerCache.computeIfAbsent(playerKey, k -> new PlayerAttributeCache());
        
        // --- NIGHTSTALKER ---
        int nightstalkerPieces = ArmorAttributeManager.countArmorWithAttribute(player, ArmorAttribute.NIGHTSTALKER);
        if (nightstalkerPieces > 0) {
            int lightLevel = world.getLightLevel(player.getBlockPos());
            
            // Only update if light level or armor count changed
            if (cache.lastNightstalkerPieces != nightstalkerPieces || cache.lastLightLevel != lightLevel) {
                EntityAttributeInstance speedInst = player.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
                if (speedInst != null) {
                    speedInst.removeModifier(NIGHTSTALKER_SPEED_ID);
                    double modifier = lightLevel <= 7
                        ? 0.20 * nightstalkerPieces * PER_PIECE_SCALE  // +20%/piece in darkness
                        : -0.15 * nightstalkerPieces * PER_PIECE_SCALE; // -15%/piece in bright light
                    speedInst.addTemporaryModifier(new EntityAttributeModifier(
                        NIGHTSTALKER_SPEED_ID, modifier, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                }
                
                cache.lastNightstalkerPieces = nightstalkerPieces;
                cache.lastLightLevel = lightLevel;
            }
        }

        // --- HEAVYWEIGHT ---
        int heavyweightPieces = ArmorAttributeManager.countArmorWithAttribute(player, ArmorAttribute.HEAVYWEIGHT);
        if (heavyweightPieces > 0) {
            // Only update if armor count changed
            if (cache.lastHeavyweightPieces != heavyweightPieces) {
                EntityAttributeInstance speedInst = player.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
                if (speedInst != null) {
                    speedInst.removeModifier(HEAVYWEIGHT_SPEED_ID);
                    double modifier = -0.20 * heavyweightPieces * PER_PIECE_SCALE;
                    speedInst.addTemporaryModifier(new EntityAttributeModifier(
                        HEAVYWEIGHT_SPEED_ID, modifier, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                }
                
                cache.lastHeavyweightPieces = heavyweightPieces;
            }
        }

        // --- GLUTTON: extra food effects (less frequent) ---
        int gluttonPieces = ArmorAttributeManager.countArmorWithAttribute(player, ArmorAttribute.GLUTTON);
        if (gluttonPieces > 0) {
            // Apply saturation boost every 25 seconds (500 ticks) instead of every second
            if (tick % 500 == 0) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 25, gluttonPieces - 1, false, false));
            }
        }
    }
    
    private static void cleanupCache() {
        // Remove offline players from cache
        playerCache.entrySet().removeIf(entry -> {
            // Simple cleanup - remove entries older than 5 minutes
            return entry.getValue().lastUpdateTime < System.currentTimeMillis() - 300000;
        });
    }
    
    /**
     * Cache for player attribute states to reduce redundant calculations
     */
    private static class PlayerAttributeCache {
        int lastNightstalkerPieces = -1;
        int lastHeavyweightPieces = -1;
        int lastLightLevel = -1;
        long lastUpdateTime = System.currentTimeMillis();
    }
}
