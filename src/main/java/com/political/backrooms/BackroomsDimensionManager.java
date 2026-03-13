package com.political.backrooms;

import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import com.political.PoliticalServer;

import java.util.Random;
import java.util.Set;

public class BackroomsDimensionManager {

    public static final RegistryKey<net.minecraft.world.World> BACKROOMS_KEY =
        RegistryKey.of(RegistryKeys.WORLD, Identifier.of("politicalserver", "backrooms"));
    public static final RegistryKey<net.minecraft.world.World> POOLROOMS_KEY =
        RegistryKey.of(RegistryKeys.WORLD, Identifier.of("politicalserver", "poolrooms"));
    public static final RegistryKey<net.minecraft.world.World> INDUSTRIAL_KEY =
        RegistryKey.of(RegistryKeys.WORLD, Identifier.of("politicalserver", "industrial"));
    
    private static final String BACKROOMS_NAME = "backrooms";
    private static final String POOLROOMS_NAME = "poolrooms";  
    private static final String INDUSTRIAL_NAME = "industrial";
    
    /**
     * Get the display name for a dimension
     */
    public static String getDimensionDisplayName(String dimensionName) {
        return switch (dimensionName) {
            case BACKROOMS_NAME -> "The Backrooms";
            case POOLROOMS_NAME -> "Poolrooms";
            case INDUSTRIAL_NAME -> "Industrial Complex";
            default -> "Unknown Dimension";
        };
    }

    /** Returns true if the given world is one of the Backrooms dimensions. */
    public static boolean isBackroomsDimension(ServerWorld world) {
        // Check if world is overworld and player is in a simulated dimension
        // We use world name check since we're simulating dimensions in overworld
        String worldName = world.getRegistryKey().getValue().toString();
        return "minecraft:overworld".equals(worldName);
    }

    /**
     * Teleport a player to a specific fracture.
     * fracture 1 = Poolrooms, 2 = Industrial, default = Backrooms (Level 0).
     * Uses overworld simulation to avoid registry sync issues.
     */
    public static void teleportToFracture(ServerPlayerEntity player, int fracture) {
        MinecraftServer server = player.getEntityWorld().getServer();
        ServerWorld overworld = server.getOverworld();
        
        if (overworld == null) {
            player.sendMessage(Text.literal("§cOverworld not available!"), false);
            return;
        }

        String dimensionName = switch (fracture) {
            case 1 -> POOLROOMS_NAME;
            case 2 -> INDUSTRIAL_NAME;
            default -> BACKROOMS_NAME;
        };

        // Generate random coordinates far from spawn
        java.util.Random rng = new java.util.Random();
        int x = rng.nextInt(20000) - 10000;
        int z = rng.nextInt(20000) - 10000;
        int y = 200; // High in the sky

        // Teleport to overworld with dimension-specific effects
        player.teleport(overworld, x, y, z,
            java.util.Set.<PositionFlag>of(), player.getYaw(), player.getPitch(), true);
        
        player.sendMessage(Text.literal("§6⟳ You are entering Fracture " + fracture + "..."), false);
        player.sendMessage(Text.literal("§7(" + getDimensionDisplayName(dimensionName) + " - simulated in overworld)"), false);
        player.sendMessage(Text.literal("§7Coordinates: " + x + ", " + y + ", " + z), false);
        
        // Apply dimension-specific effects
        switch (fracture) {
            case 1 -> { // Poolrooms - water effects
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 6000, 0, true, false, false));
                player.sendMessage(Text.literal("§bYou feel the weight of water..."), false);
            }
            case 2 -> { // Industrial - mining effects
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 3000, 0, true, false, false));
                player.sendMessage(Text.literal("§8The industrial air fills your lungs..."), false);
            }
            default -> { // Backrooms - confusion effects
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 0, true, false, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 2000, 0, true, false, false));
                player.sendMessage(Text.literal("§8You slip through the cracks of reality..."), false);
            }
        }
    }

    /**
     * Teleport a player into the Backrooms via an End Gateway event.
     * Requires a Warden Core item for reliable entry; without it the chance is 1-in-1000.
     * Uses overworld simulation to avoid registry sync issues.
     *
     * @return true if the player was teleported (caller should cancel the original Gateway teleport)
     */
    public static boolean teleportToBackrooms(ServerPlayerEntity player, boolean hasWardenCore) {
        MinecraftServer server = player.getEntityWorld().getServer();
        ServerWorld overworld = server.getOverworld();
        
        if (overworld == null) return false;

        Random rng = new Random();
        String targetDimension;

        if (hasWardenCore) {
            // 90% chance main Backrooms, 10% Poolrooms with Warden Core
            targetDimension = (rng.nextInt(10) < 9) ? BACKROOMS_NAME : POOLROOMS_NAME;
        } else {
            // 1-in-1000 chance of accidental entry
            if (rng.nextInt(1000) != 0) return false;
            int roll = rng.nextInt(100);
            if (roll < 1)       targetDimension = POOLROOMS_NAME;
            else if (roll < 2)  targetDimension = INDUSTRIAL_NAME;
            else                targetDimension = BACKROOMS_NAME;
        }

        // Generate random coordinates far from spawn
        int x = rng.nextInt(40000) - 20000;
        int z = rng.nextInt(40000) - 20000;
        int y = 200;

        // Teleport to overworld with backrooms effects
        player.teleport(overworld, x, y, z,
            Set.<PositionFlag>of(), player.getYaw(), player.getPitch(), true);
        
        player.sendMessage(Text.literal("§8You slip through the cracks of reality..."), false);
        player.sendMessage(Text.literal("§7(" + getDimensionDisplayName(targetDimension) + " - simulated in overworld)"), false);
        
        // Apply confusion effects
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 0, true, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 2000, 0, true, false, false));
        
        return true;
    }

    /**
     * Returns the world position of the exit beacon's top block in the Backrooms dimension.
     * This is the block players must stand on to escape.
     */
    public static BlockPos getExitBeaconPos() {
        int wx = EXIT_CHUNK_X * 16 + 8; // centre of the exit chunk
        int wz = EXIT_CHUNK_Z * 16 + 8;
        return new BlockPos(wx, BackroomsChunkGenerator.EXIT_BEACON_Y, wz);
    }

    /** Chunk coordinate constants mirrored from the chunk generator for easy access. */
    private static final int EXIT_CHUNK_X = BackroomsChunkGenerator.EXIT_CHUNK_X;
    private static final int EXIT_CHUNK_Z = BackroomsChunkGenerator.EXIT_CHUNK_Z;

    /**
     * Returns true when {@code pos} is at or within 1 block horizontally of the
     * exit beacon.  Used by the server-tick exit detector.
     * ENABLED with simulation approach - players can escape by reaching high altitude
     */
    public static boolean isAtExitBeacon(BlockPos pos) {
        // In simulation mode, allow players to escape by reaching high altitude
        // This simulates finding the exit beacon in the generated world
        return pos.getY() >= 200; // Escape by reaching Y=200 or higher
    }

    /**
     * Teleports {@code player} from the Backrooms back to the overworld spawn.
     * Call this when the player activates the exit beacon.
     * Uses simulation approach.
     */
    public static void escapeBackrooms(ServerPlayerEntity player) {
        player.sendMessage(Text.literal("§a✦ You have escaped the Backrooms."), false);
        com.political.SpawnManager.teleportToSpawn(player);
    }

    /**
     * Register the three chunk-generator MapCodecs so Minecraft can deserialize
     * the dimension JSON files that reference "politicalserver:backrooms" etc.
     * Call this during mod initialisation (before the server loads worlds).
     * COMPLETELY DISABLED - no registry operations to prevent client sync issues
     */
    public static void registerCodecs() {
        // COMPLETELY DISABLED - no registry operations whatsoever
        PoliticalServer.LOGGER.info("Skipping dimension codec registration - disabled for server-side compatibility");
    }
}
