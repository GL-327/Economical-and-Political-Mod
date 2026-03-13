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
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import com.political.PoliticalServer;
import com.political.backrooms.generator.AdvancedBackroomsGenerator;

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
     * Uses procedural generation in overworld to create authentic backrooms experience.
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

        // Generate coordinates for procedural backrooms area
        java.util.Random rng = new java.util.Random();
        int x = rng.nextInt(50000) - 25000; // Larger area for backrooms
        int z = rng.nextInt(50000) - 25000;
        int y = 55; // Inside the backrooms level

        // Teleport to overworld with procedural generation
        player.teleport(overworld, x, y, z,
            java.util.Set.<PositionFlag>of(), player.getYaw(), player.getPitch(), true);
        
        player.sendMessage(Text.literal("§6⟳ You are entering Fracture " + fracture + "..."), false);
        player.sendMessage(Text.literal("§7(" + getDimensionDisplayName(dimensionName) + " - procedurally generated)"), false);
        player.sendMessage(Text.literal("§7Coordinates: " + x + ", " + y + ", " + z), false);
        
        // Generate advanced backrooms structure around player
        AdvancedBackroomsGenerator.generateBackroomsStructure(
            overworld, player.getBlockPos(), 
            AdvancedBackroomsGenerator.DimensionType.MAIN_BACKROOMS);
        
        // Apply dimension-specific effects
        switch (fracture) {
            case 1 -> { // Poolrooms - water effects
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 6000, 0, true, false, false));
                player.sendMessage(Text.literal("§bYou feel the weight of water..."), false);
                generateImmediatePoolrooms(overworld, x, y, z);
            }
            case 2 -> { // Industrial - mining effects
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 3000, 0, true, false, false));
                player.sendMessage(Text.literal("§8The industrial air fills your lungs..."), false);
                generateImmediateIndustrial(overworld, x, y, z);
            }
            default -> { // Backrooms - confusion effects
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 0, true, false, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 2000, 0, true, false, false));
                player.sendMessage(Text.literal("§8You slip through the cracks of reality..."), false);
            }
        }
    }
    
    /**
     * Generate immediate backrooms structure around player position
     */
    private static void generateImmediateBackrooms(ServerWorld world, int centerX, int centerY, int centerZ, int fracture) {
        // Generate a 32x32 area around the player
        for (int x = centerX - 16; x <= centerX + 16; x++) {
            for (int z = centerZ - 16; z <= centerZ + 16; z++) {
                for (int y = 50; y < 70; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    generateBackroomsBlock(world, pos, x - centerX, z - centerZ, fracture);
                }
            }
        }
        
        // Generate exit beacon far from player
        int beaconX = centerX + 100;
        int beaconZ = centerZ + 100;
        com.political.backrooms.generator.BackroomsProceduralGenerator.generateExitBeacon(world, new BlockPos(beaconX, 68, beaconZ));
    }
    
    /**
     * Generate individual backrooms blocks
     */
    private static void generateBackroomsBlock(ServerWorld world, BlockPos pos, int relX, int relZ, int fracture) {
        int roomX = Math.floorMod(relX + 8, 10);
        int roomZ = Math.floorMod(relZ + 8, 10);
        boolean isWall = roomX < 2 || roomX >= 8 || roomZ < 2 || roomZ >= 8;
        
        Block wallBlock = switch (fracture) {
            case 1 -> Blocks.BLUE_CONCRETE; // Poolrooms
            case 2 -> Blocks.STONE_BRICKS;  // Industrial
            default -> Blocks.YELLOW_CONCRETE; // Backrooms
        };
        
        Block floorBlock = switch (fracture) {
            case 1 -> Blocks.LIGHT_BLUE_CONCRETE;
            case 2 -> Blocks.COBBLESTONE;
            default -> Blocks.LIGHT_GRAY_CONCRETE;
        };
        
        if (pos.getY() == 50) {
            world.setBlockState(pos, floorBlock.getDefaultState(), 3);
        } else if (pos.getY() == 69) {
            world.setBlockState(pos, wallBlock.getDefaultState(), 3);
            // Add lights
            if (!isWall && (roomX == 5 || roomZ == 5)) {
                world.setBlockState(pos, Blocks.SEA_LANTERN.getDefaultState(), 3);
            }
        } else if (isWall) {
            world.setBlockState(pos, wallBlock.getDefaultState(), 3);
        } else {
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
        }
    }
    
    /**
     * Generate poolrooms-specific features
     */
    private static void generateImmediatePoolrooms(ServerWorld world, int centerX, int centerY, int centerZ) {
        // Add water pools in some rooms
        java.util.Random rng = new java.util.Random(centerX + centerZ);
        for (int x = centerX - 12; x <= centerX + 12; x += 10) {
            for (int z = centerZ - 12; z <= centerZ + 12; z += 10) {
                if (rng.nextInt(3) == 0) {
                    // Create water pool
                    for (int dx = 2; dx <= 7; dx++) {
                        for (int dz = 2; dz <= 7; dz++) {
                            BlockPos waterPos = new BlockPos(x + dx, 51, z + dz);
                            world.setBlockState(waterPos, Blocks.WATER.getDefaultState(), 3);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Generate industrial-specific features
     */
    private static void generateImmediateIndustrial(ServerWorld world, int centerX, int centerY, int centerZ) {
        // Add machinery and industrial elements
        java.util.Random rng = new java.util.Random(centerX * centerZ);
        for (int x = centerX - 12; x <= centerX + 12; x += 10) {
            for (int z = centerZ - 12; z <= centerZ + 12; z += 10) {
                if (rng.nextInt(3) == 0) {
                    // Add industrial machinery
                    BlockPos machinePos = new BlockPos(x + 5, 51, z + 5);
                    world.setBlockState(machinePos, Blocks.FURNACE.getDefaultState(), 3);
                    world.setBlockState(machinePos.up(), Blocks.HOPPER.getDefaultState(), 3);
                }
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
