package com.political.backrooms.fracture3;

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
import net.minecraft.network.packet.s2c.play.PositionFlag;

import java.util.Random;
import java.util.Set;

/**
 * Fracture 3 Dimension Manager
 * 
 * Handles dimension registration, teleportation, and exit logic for the Backrooms dimension.
 */
public class Fracture3DimensionManager {

    // Dimension registry key
    public static final RegistryKey<net.minecraft.world.World> FRACTURE_3_KEY =
        RegistryKey.of(RegistryKeys.WORLD, Identifier.of("politicalserver", "fracture_3"));

    // Spawn coordinates (entry room at origin)
    public static final BlockPos SPAWN_POS = new BlockPos(0, 65, 0);

    /**
     * Teleport player to Fracture 3 (Backrooms)
     * Called when portal interception triggers
     */
    public static void teleportToFracture3(ServerPlayerEntity player) {
        MinecraftServer server = player.getEntityWorld().getServer();
        if (server == null) return;

        ServerWorld fractureWorld = server.getWorld(FRACTURE_3_KEY);
        if (fractureWorld == null) {
            player.sendMessage(Text.literal("§cThe void refuses you..."), false);
            return;
        }

        // Teleport to spawn
        player.teleport(fractureWorld, SPAWN_POS.getX() + 0.5, SPAWN_POS.getY(), SPAWN_POS.getZ() + 0.5,
            Set.<PositionFlag>of(), player.getYaw(), player.getPitch(), true);

        // Apply nausea on arrival
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 1, false, false, true));

        // No message - silence is the point
    }

    /**
     * Teleport player back to overworld
     * Called by /overworld command
     */
    public static void teleportToOverworld(ServerPlayerEntity player) {
        MinecraftServer server = player.getEntityWorld().getServer();
        if (server == null) return;

        ServerWorld overworld = server.getWorld(net.minecraft.world.World.OVERWORLD);
        if (overworld == null) {
            player.sendMessage(Text.literal("§cCannot find overworld!"), false);
            return;
        }

        // Get spawn point - use world spawn or (0,100,0) as fallback
        BlockPos spawnPos = new BlockPos(0, 100, 0);

        // Teleport
        player.teleport(overworld, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
            Set.<PositionFlag>of(), player.getYaw(), player.getPitch(), true);

        player.sendMessage(Text.literal("§aYou emerge from the void..."), false);
    }

    /**
     * Check if player is in Fracture 3
     */
    public static boolean isInFracture3(ServerPlayerEntity player) {
        return player.getEntityWorld().getRegistryKey().equals(FRACTURE_3_KEY);
    }

    /**
     * Check if a world is Fracture 3
     */
    public static boolean isFracture3Dimension(ServerWorld world) {
        return world.getRegistryKey().equals(FRACTURE_3_KEY);
    }
}
