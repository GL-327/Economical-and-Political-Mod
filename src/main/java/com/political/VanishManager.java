package com.political;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import java.util.*;

public class VanishManager {

    private static final Set<UUID> vanishedPlayers = new HashSet<>();

    public static boolean isVanished(UUID uuid) {
        return vanishedPlayers.contains(uuid);
    }

    public static boolean isVanished(ServerPlayer player) {
        return isVanished(player.getUUID());
    }

    public static void toggleVanish(ServerPlayer player) {
        if (isVanished(player)) {
            unvanish(player);
        } else {
            vanish(player);
        }
    }

    public static void vanish(ServerPlayer player) {
        vanishedPlayers.add(player.getUUID());

        // Apply infinite invisibility (no particles, no icon)
        player.addEffect(new MobEffectInstance(
                MobEffects.INVISIBILITY, Integer.MAX_VALUE, 0, true, false, false));

        // Remove this player from other players' tab list
        MinecraftServer server = PoliticalServer.server;
        if (server != null) {
            ClientboundPlayerInfoRemovePacket removePacket = new ClientboundPlayerInfoRemovePacket(
                    Collections.singletonList(player.getUUID()));
            for (ServerPlayer other : server.getPlayerList().getPlayers()) {
                if (other == player) continue;
                other.connection.send(removePacket);
            }
        }

        player.sendSystemMessage(Component.literal("§aYou are now §l§aVANISHED§r§a. Other players cannot see you."), false);
    }

    public static void unvanish(ServerPlayer player) {
        vanishedPlayers.remove(player.getUUID());

        // Remove invisibility effect
        player.removeEffect(MobEffects.INVISIBILITY);

        // Add this player back to other players' tab list
        MinecraftServer server = PoliticalServer.server;
        if (server != null) {
            ClientboundPlayerInfoUpdatePacket addPacket =
                    ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(Collections.singletonList(player));
            for (ServerPlayer other : server.getPlayerList().getPlayers()) {
                if (other == player) continue;
                other.connection.send(addPacket);
            }
        }

        player.sendSystemMessage(Component.literal("§cYou are no longer vanished."), false);
    }

    /**
     * Called when a player joins - hide all currently vanished players from them,
     * and if they are vanished, remove them from everyone else's tab list.
     */
    public static void onPlayerJoin(ServerPlayer joining) {
        MinecraftServer server = PoliticalServer.server;
        if (server == null) return;

        // If joining player is not vanished, hide all currently vanished players from them
        if (!isVanished(joining)) {
            List<UUID> vanishedUuids = new ArrayList<>(vanishedPlayers);
            if (!vanishedUuids.isEmpty()) {
                joining.connection.send(new ClientboundPlayerInfoRemovePacket(vanishedUuids));
            }
        }

        // If joining player is vanished, remove them from everyone else's tab list
        if (isVanished(joining)) {
            ClientboundPlayerInfoRemovePacket removePacket = new ClientboundPlayerInfoRemovePacket(
                    Collections.singletonList(joining.getUUID()));
            for (ServerPlayer other : server.getPlayerList().getPlayers()) {
                if (other == joining) continue;
                other.connection.send(removePacket);
            }
            // Re-apply invisibility
            joining.addEffect(new MobEffectInstance(
                    MobEffects.INVISIBILITY, Integer.MAX_VALUE, 0, true, false, false));
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
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (isVanished(player)) {
                MobEffectInstance effect = player.getEffect(MobEffects.INVISIBILITY);
                if (effect == null || effect.getDuration() < 200) {
                    player.addEffect(new MobEffectInstance(
                            MobEffects.INVISIBILITY, Integer.MAX_VALUE, 0, true, false, false));
                }
            }
        }
    }
}
