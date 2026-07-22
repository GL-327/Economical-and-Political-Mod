package com.political;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public class CreditItem {

    // Get a player's credit balance
    public static int countCredits(ServerPlayer player) {
        return DataManager.getCredits(player.getUUID().toString());
    }

    // Get credits by UUID (for offline players)
    public static int countCredits(String uuid) {
        return DataManager.getCredits(uuid);
    }

    // Give credits to a player
    public static void giveCredits(ServerPlayer player, int amount) {
        if (amount <= 0) return;
        DataManager.addCredits(player.getUUID().toString(), amount);
        DataManager.save(PoliticalServer.server);
        player.sendSystemMessage(Component.literal("+" + amount + " credits").withStyle(ChatFormatting.GREEN));
    }

    // Give credits silently (no message)
    public static void giveCreditsQuiet(ServerPlayer player, int amount) {
        if (amount <= 0) return;
        DataManager.addCredits(player.getUUID().toString(), amount);
        DataManager.save(PoliticalServer.server);
    }

    // Give credits by UUID (for offline players)
    public static void giveCredits(String uuid, int amount) {
        if (amount <= 0) return;
        DataManager.addCredits(uuid, amount);
        DataManager.save(PoliticalServer.server);
    }

    // Remove credits from a player (returns true if successful)
    public static boolean removeCredits(ServerPlayer player, int amount) {
        if (amount <= 0) return true;
        if (DataManager.removeCredits(player.getUUID().toString(), amount)) {
            DataManager.save(PoliticalServer.server);
            return true;
        }
        return false;
    }

    // Remove credits by UUID (for offline players)
    public static boolean removeCredits(String uuid, int amount) {
        if (amount <= 0) return true;
        if (DataManager.removeCredits(uuid, amount)) {
            DataManager.save(PoliticalServer.server);
            return true;
        }
        return false;
    }

    // Set a player's credits to a specific amount
    public static void setCredits(ServerPlayer player, int amount) {
        DataManager.setCredits(player.getUUID().toString(), amount);
        DataManager.save(PoliticalServer.server);
    }

    // Set credits by UUID
    public static void setCredits(String uuid, int amount) {
        DataManager.setCredits(uuid, amount);
        DataManager.save(PoliticalServer.server);
    }

    // Check if player has enough credits
    public static boolean hasCredits(ServerPlayer player, int amount) {
        return countCredits(player) >= amount;
    }

    // Check by UUID
    public static boolean hasCredits(String uuid, int amount) {
        return countCredits(uuid) >= amount;
    }
}