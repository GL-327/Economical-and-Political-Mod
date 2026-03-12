package com.political;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.*;

public class VanishManager {

    private static final Set<UUID> vanishedPlayers = new HashSet<>();

    public static boolean isVanished(UUID uuid) {
        return vanishedPlayers.contains(uuid);
    }

    public static boolean isVanished(ServerPlayerEntity player) {
        return isVanished(player.getUuid());
    }

    public static void toggleVanish(ServerPlayerEntity player) {
        if (isVanished(player)) {
            unvanish(player);
        } else {
            vanish(player);
        }
    }

    public static void vanish(ServerPlayerEntity player) {
        vanishedPlayers.add(player.getUuid());

        // Apply infinite invisibility (no particles, no icon)
        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.INVISIBILITY, Integer.MAX_VALUE, 0, true, false, false));

        // Remove this player from other players' tab list
        MinecraftServer server = PoliticalServer.server;
        if (server != null) {
            PlayerRemoveS2CPacket removePacket = new PlayerRemoveS2CPacket(
                    Collections.singletonList(player.getUuid()));
            for (ServerPlayerEntity other : server.getPlayerManager().getPlayerList()) {
                if (other == player) continue;
                other.networkHandler.sendPacket(removePacket);
            }
        }

        player.sendMessage(Text.literal("§aYou are now §l§aVANISHED§r§a. Other players cannot see you."), false);
    }

    public static void unvanish(ServerPlayerEntity player) {
        vanishedPlayers.remove(player.getUuid());

        // Remove invisibility effect
        player.removeStatusEffect(StatusEffects.INVISIBILITY);

        // Add this player back to other players' tab list
        MinecraftServer server = PoliticalServer.server;
        if (server != null) {
            PlayerListS2CPacket addPacket = new PlayerListS2CPacket(
                    EnumSet.of(
                            PlayerListS2CPacket.Action.ADD_PLAYER,
                            PlayerListS2CPacket.Action.UPDATE_GAME_MODE,
                            PlayerListS2CPacket.Action.UPDATE_LISTED,
                            PlayerListS2CPacket.Action.UPDATE_LATENCY,
                            PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME
                    ),
                    Collections.singletonList(player)
            );
            for (ServerPlayerEntity other : server.getPlayerManager().getPlayerList()) {
                if (other == player) continue;
                other.networkHandler.sendPacket(addPacket);
            }
        }

        player.sendMessage(Text.literal("§cYou are no longer vanished."), false);
    }

    /**
     * Called when a player joins - hide all currently vanished players from them,
     * and if they are vanished, remove them from everyone else's tab list.
     */
    public static void onPlayerJoin(ServerPlayerEntity joining) {
        MinecraftServer server = PoliticalServer.server;
        if (server == null) return;

        // If joining player is not vanished, hide all currently vanished players from them
        if (!isVanished(joining)) {
            List<UUID> vanishedUuids = new ArrayList<>(vanishedPlayers);
            if (!vanishedUuids.isEmpty()) {
                joining.networkHandler.sendPacket(new PlayerRemoveS2CPacket(vanishedUuids));
            }
        }

        // If joining player is vanished, remove them from everyone else's tab list
        if (isVanished(joining)) {
            PlayerRemoveS2CPacket removePacket = new PlayerRemoveS2CPacket(
                    Collections.singletonList(joining.getUuid()));
            for (ServerPlayerEntity other : server.getPlayerManager().getPlayerList()) {
                if (other == joining) continue;
                other.networkHandler.sendPacket(removePacket);
            }
            // Re-apply invisibility
            joining.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.INVISIBILITY, Integer.MAX_VALUE, 0, true, false, false));
        }
    }

    private static int vanishTickCounter = 0;

    /**
     * Called every server tick to maintain invisibility on vanished players.
     * Only runs the check every 40 ticks (~2 seconds) since the effect duration is very long.
     */
    public static void tick(MinecraftServer server) {
        vanishTickCounter++;
        if (vanishTickCounter < 40) return;
        vanishTickCounter = 0;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (isVanished(player)) {
                StatusEffectInstance effect = player.getStatusEffect(StatusEffects.INVISIBILITY);
                if (effect == null || effect.getDuration() < 200) {
                    player.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.INVISIBILITY, Integer.MAX_VALUE, 0, true, false, false));
                }
            }
        }
    }
}
