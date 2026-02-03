package com.political;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class CoinManager {

    public static int getCoins(ServerPlayerEntity player) {
        return DataManager.getCoins(player.getUuidAsString());
    }

    public static int getCoins(String uuid) {
        return DataManager.getCoins(uuid);
    }

    public static void giveCoins(ServerPlayerEntity player, int amount) {
        if (amount <= 0) return;
        DataManager.addCoins(player.getUuidAsString(), amount);
        DataManager.save(PoliticalServer.server);
        player.sendMessage(Text.literal("+" + amount + " coins").formatted(Formatting.YELLOW));
    }
    public static void setCoins(String uuid, int amount) {
        DataManager.setCoins(uuid, Math.max(0, amount));
    }
    public static void giveCoinsQuiet(ServerPlayerEntity player, int amount) {
        if (amount <= 0) return;
        DataManager.addCoins(player.getUuidAsString(), amount);
        DataManager.save(PoliticalServer.server);
    }

    public static void giveCoins(String uuid, int amount) {
        if (amount <= 0) return;
        DataManager.addCoins(uuid, amount);
        DataManager.save(PoliticalServer.server);
    }

    public static boolean removeCoins(ServerPlayerEntity player, int amount) {
        if (amount <= 0) return true;
        if (DataManager.removeCoins(player.getUuidAsString(), amount)) {
            DataManager.save(PoliticalServer.server);
            return true;
        }
        return false;
    }

    public static boolean removeCoins(String uuid, int amount) {
        if (amount <= 0) return true;
        if (DataManager.removeCoins(uuid, amount)) {
            DataManager.save(PoliticalServer.server);
            return true;
        }
        return false;
    }

    public static boolean hasCoins(ServerPlayerEntity player, int amount) {
        return getCoins(player) >= amount;
    }

    public static boolean hasCoins(String uuid, int amount) {
        return getCoins(uuid) >= amount;
    }
    // Convert 1000 coins to 1 credit

    public static boolean convertToCredits(ServerPlayerEntity player, int creditsToGet) {
        int coinsNeeded = 1000 * creditsToGet;
        if (!hasCoins(player, coinsNeeded)) {
            return false;
        }
        removeCoins(player, coinsNeeded);
        CreditItem.giveCredits(player, creditsToGet);
        return true;
    }

    // Convert 1 credit to 1000 coins
    public static boolean convertToCoins(ServerPlayerEntity player, int creditsToSpend) {
        if (!CreditItem.hasCredits(player, creditsToSpend)) {
            return false;
        }
        CreditItem.removeCredits(player, creditsToSpend);
        giveCoins(player, creditsToSpend * 1000);
        return true;
    }
}
