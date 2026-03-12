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

    /** Returns true if the given world is one of the Backrooms dimensions. */
    public static boolean isBackroomsDimension(ServerWorld world) {
        RegistryKey<net.minecraft.world.World> key = world.getRegistryKey();
        return key.equals(BACKROOMS_KEY) || key.equals(POOLROOMS_KEY) || key.equals(INDUSTRIAL_KEY);
    }

    /**
     * Teleport a player to a specific fracture.
     * fracture 1 = Poolrooms, 2 = Industrial, default = Backrooms (Level 0).
     */
    public static void teleportToFracture(ServerPlayerEntity player, int fracture) {
        MinecraftServer server = player.getEntityWorld().getServer();

        RegistryKey<net.minecraft.world.World> targetKey = switch (fracture) {
            case 1 -> POOLROOMS_KEY;
            case 2 -> INDUSTRIAL_KEY;
            default -> BACKROOMS_KEY;
        };

        ServerWorld targetWorld = server.getWorld(targetKey);
        if (targetWorld == null) {
            player.sendMessage(Text.literal("§cFracture " + fracture + " is currently unreachable."), false);
            return;
        }

        Random rng = new Random();
        int x = rng.nextInt(2000) - 1000;
        int z = rng.nextInt(2000) - 1000;

        player.teleport(targetWorld, x, 2.0, z,
            Set.<PositionFlag>of(), player.getYaw(), player.getPitch(), true);
        player.sendMessage(Text.literal("§6⟳ You are entering Fracture " + fracture + "..."), false);
    }

    /**
     * Teleport a player into the Backrooms via an End Gateway event.
     * Requires a Warden Core item for reliable entry; without it the chance is 1-in-1000.
     *
     * @return true if the player was teleported (caller should cancel the original Gateway teleport)
     */
    public static boolean teleportToBackrooms(ServerPlayerEntity player, boolean hasWardenCore) {
        MinecraftServer server = player.getEntityWorld().getServer();

        Random rng = new Random();

        RegistryKey<net.minecraft.world.World> targetKey;
        if (hasWardenCore) {
            // 90% chance main Backrooms, 10% Poolrooms with Warden Core
            targetKey = (rng.nextInt(10) < 9) ? BACKROOMS_KEY : POOLROOMS_KEY;
        } else {
            // 1-in-1000 chance of accidental entry
            if (rng.nextInt(1000) != 0) return false;
            int roll = rng.nextInt(100);
            if (roll < 1)       targetKey = POOLROOMS_KEY;
            else if (roll < 2)  targetKey = INDUSTRIAL_KEY;
            else                targetKey = BACKROOMS_KEY;
        }

        ServerWorld targetWorld = server.getWorld(targetKey);
        if (targetWorld == null) return false;

        int x = rng.nextInt(4000) - 2000;
        int z = rng.nextInt(4000) - 2000;

        player.teleport(targetWorld, x, 2.0, z,
            Set.<PositionFlag>of(), player.getYaw(), player.getPitch(), true);
        player.sendMessage(Text.literal("§8You slip through the cracks of reality..."), false);
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
     */
    public static boolean isAtExitBeacon(BlockPos pos) {
        BlockPos exit = getExitBeaconPos();
        return pos.getY() == exit.getY()
            && Math.abs(pos.getX() - exit.getX()) <= 1
            && Math.abs(pos.getZ() - exit.getZ()) <= 1;
    }

    /**
     * Teleports {@code player} from the Backrooms back to the overworld spawn.
     * Call this when the player activates the exit beacon.
     */
    public static void escapeBackrooms(ServerPlayerEntity player) {
        player.sendMessage(Text.literal("§a✦ You have escaped the Backrooms."), false);
        com.political.SpawnManager.teleportToSpawn(player);
    }

    /**
     * Register the three chunk-generator MapCodecs so Minecraft can deserialize
     * the dimension JSON files that reference "politicalserver:backrooms" etc.
     * Call this during mod initialisation (before the server loads worlds).
     */
    public static void registerCodecs() {
        Registry.register(
            Registries.CHUNK_GENERATOR,
            Identifier.of(PoliticalServer.MOD_ID, "backrooms"),
            BackroomsChunkGenerator.CODEC);
        Registry.register(
            Registries.CHUNK_GENERATOR,
            Identifier.of(PoliticalServer.MOD_ID, "poolrooms"),
            PoolroomsChunkGenerator.CODEC);
        Registry.register(
            Registries.CHUNK_GENERATOR,
            Identifier.of(PoliticalServer.MOD_ID, "industrial"),
            IndustrialChunkGenerator.CODEC);
    }
}
