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
        // Disable custom dimensions to prevent registry sync issues
        return false;
    }

    /**
     * Teleport a player to a specific fracture.
     * fracture 1 = Poolrooms, 2 = Industrial, default = Backrooms (Level 0).
     * DISABLED: Custom dimensions disabled to prevent registry sync issues
     */
    public static void teleportToFracture(ServerPlayerEntity player, int fracture) {
        player.sendMessage(Text.literal("§cCustom dimensions are currently disabled."), false);
        player.sendMessage(Text.literal("§7This feature is disabled to ensure server-side compatibility."), false);
    }

    /**
     * Teleport a player into the Backrooms via an End Gateway event.
     * Requires a Warden Core item for reliable entry; without it the chance is 1-in-1000.
     * DISABLED: Custom dimensions disabled to prevent registry sync issues
     *
     * @return true if the player was teleported (caller should cancel the original Gateway teleport)
     */
    public static boolean teleportToBackrooms(ServerPlayerEntity player, boolean hasWardenCore) {
        // Disabled to prevent registry sync issues
        return false;
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
     * DISABLED: Custom dimensions disabled
     */
    public static boolean isAtExitBeacon(BlockPos pos) {
        return false; // Disabled
    }

    /**
     * Teleports {@code player} from the Backrooms back to the overworld spawn.
     * Call this when the player activates the exit beacon.
     * DISABLED: Custom dimensions disabled
     */
    public static void escapeBackrooms(ServerPlayerEntity player) {
        // Disabled - just teleport to spawn
        com.political.SpawnManager.teleportToSpawn(player);
    }

    /**
     * Register the three chunk-generator MapCodecs so Minecraft can deserialize
     * the dimension JSON files that reference "politicalserver:backrooms" etc.
     * Call this during mod initialisation (before the server loads worlds).
     */
    public static void registerCodecs() {
        // Don't register any codecs to prevent client registry sync issues
        // We'll handle dimensions programmatically without using data pack registry system
    }
}
