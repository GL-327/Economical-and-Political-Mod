package com.political;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Coin Totem Manager
 * 
 * Handles one-time use totems that prevent coin loss on death.
 * - Totems are consumed on death (unless killed by another player)
 * - Prevents the 50% coin loss penalty
 * - Does NOT prevent the actual death - player still dies
 * - Works with Totem of Undying item in inventory
 */
public class CoinTotemManager {

    // Track players who have used their coin totem (for this death)
    private static final Set<UUID> usedCoinTotem = ConcurrentHashMap.newKeySet();

    /**
     * Called when a player dies to check if they should lose coins.
     * Returns true if the player has a totem and it should be consumed.
     * 
     * @param player The dying player
     * @param source The damage source that killed them
     * @return true if coin totem was used (coins should be protected)
     */
    public static boolean tryUseCoinTotem(ServerPlayer player, DamageSource source) {
        UUID playerId = player.getUUID();
        
        // Check if killed by another player - totem doesn't work in PvP
        if (source.getEntity() instanceof ServerPlayer) {
            return false; // PvP death - no totem protection
        }
        
        // Check if player has a totem in inventory
        int totemSlot = findTotemSlot(player);
        if (totemSlot == -1) {
            return false; // No totem found
        }
        
        // Consume the totem
        player.getInventory().getItem(totemSlot).shrink(1);
        usedCoinTotem.add(playerId);
        
        // Visual and message feedback
        player.sendSystemMessage(Component.literal("☠ Your Totem of Undying protected your coins!")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        
        // Spawn totem particles
        ServerLevel world = (ServerLevel) player.level();
        world.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                player.getX(), player.getY() + 1, player.getZ(),
                8, 0.6, 0.6, 0.6, 0.5);
        
        return true;
    }

    /**
     * Check if a player's coins should be protected from death loss.
     * Called by DeathPenaltyManager.
     */
    public static boolean isCoinLossProtected(ServerPlayer player) {
        return usedCoinTotem.contains(player.getUUID());
    }

    /**
     * Clear the totem protection after death handling is complete.
     * Should be called after death penalty processing.
     */
    public static void clearProtection(ServerPlayer player) {
        usedCoinTotem.remove(player.getUUID());
    }

    /**
     * Find a totem in the player's inventory.
     * @return slot index or -1 if not found
     */
    private static int findTotemSlot(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Count how many totems a player has.
     */
    public static int countTotems(ServerPlayer player) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                count += stack.getCount();
            }
        }
        return count;
    }
}
