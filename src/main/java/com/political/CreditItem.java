package com.political;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class CreditItem {

    // Get a player's credit balance
    public static int countCredits(ServerPlayerEntity player) {
        return DataManager.getCredits(player.getUuidAsString());
    }

    // Get credits by UUID (for offline players)
    public static int countCredits(String uuid) {
        return DataManager.getCredits(uuid);
    }

    // Give credits to a player
    public static void giveCredits(ServerPlayerEntity player, int amount) {
        if (amount <= 0) return;
        DataManager.addCredits(player.getUuidAsString(), amount);
        DataManager.save(PoliticalServer.server);
        player.sendMessage(Text.literal("+" + amount + " credits").formatted(Formatting.GREEN));
    }

    // Give credits silently (no message)
    public static void giveCreditsQuiet(ServerPlayerEntity player, int amount) {
        if (amount <= 0) return;
        DataManager.addCredits(player.getUuidAsString(), amount);
        DataManager.save(PoliticalServer.server);
    }

    // Give credits by UUID (for offline players)
    public static void giveCredits(String uuid, int amount) {
        if (amount <= 0) return;
        DataManager.addCredits(uuid, amount);
        DataManager.save(PoliticalServer.server);
    }

    // Remove credits from a player (returns true if successful)
    public static boolean removeCredits(ServerPlayerEntity player, int amount) {
        if (amount <= 0) return true;
        if (DataManager.removeCredits(player.getUuidAsString(), amount)) {
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
    public static void setCredits(ServerPlayerEntity player, int amount) {
        DataManager.setCredits(player.getUuidAsString(), amount);
        DataManager.save(PoliticalServer.server);
    }

    // Set credits by UUID
    public static void setCredits(String uuid, int amount) {
        DataManager.setCredits(uuid, amount);
        DataManager.save(PoliticalServer.server);
    }

    // Check if player has enough credits
    public static boolean hasCredits(ServerPlayerEntity player, int amount) {
        return countCredits(player) >= amount;
    }

    // Check by UUID
    public static boolean hasCredits(String uuid, int amount) {
        return countCredits(uuid) >= amount;
    }
}