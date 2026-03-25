package com.political;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class CoinManager {

    private static int clampLongToInt(long value) {
        if (value > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        if (value < Integer.MIN_VALUE) return Integer.MIN_VALUE;
        return (int) value;
    }

    public static int getCoins(ServerPlayerEntity player) {
        return clampLongToInt(DataManager.getCoins(player.getUuidAsString()));
    }

    public static int getCoins(String uuid) {
        return clampLongToInt(DataManager.getCoins(uuid));
    }

    /**
     * Long-precision coin getter for large bid/prices (e.g. underground auction).
     */
    public static long getCoinsLong(ServerPlayerEntity player) {
        return DataManager.getCoins(player.getUuidAsString());
    }

    public static long getCoinsLong(String uuid) {
        return DataManager.getCoins(uuid);
    }

    /**
     * Apply Emerald armor coin bonus (+5% per piece worn).
     */
    private static long applyEmeraldBonus(ServerPlayerEntity player, long amount) {
        if (player == null) return amount;
        double bonus = GemArmor.getEmeraldCoinBonus(player);
        if (bonus > 0) {
            long bonusAmount = (long) Math.ceil(amount * bonus);
            return amount + bonusAmount;
        }
        return amount;
    }

    public static void giveCoins(ServerPlayerEntity player, long amount) {
        if (amount <= 0) return;
        long totalAmount = applyEmeraldBonus(player, amount);
        DataManager.addCoins(player.getUuidAsString(), totalAmount);
        DataManager.save(PoliticalServer.server);
        player.sendMessage(Text.literal("+" + totalAmount + " coins").formatted(Formatting.YELLOW));
    }
    public static void setCoins(String uuid, long amount) {
        DataManager.setCoins(uuid, Math.max(0, amount));
    }
    public static void giveCoinsQuiet(ServerPlayerEntity player, long amount) {
        if (amount <= 0) return;
        long totalAmount = applyEmeraldBonus(player, amount);
        DataManager.addCoins(player.getUuidAsString(), totalAmount);
        DataManager.save(PoliticalServer.server);
    }

    public static void giveCoins(String uuid, long amount) {
        if (amount <= 0) return;
        DataManager.addCoins(uuid, amount);
        DataManager.save(PoliticalServer.server);
    }

    public static boolean removeCoins(ServerPlayerEntity player, long amount) {
        if (amount <= 0) return true;
        if (DataManager.removeCoins(player.getUuidAsString(), amount)) {
            DataManager.save(PoliticalServer.server);
            return true;
        }
        return false;
    }

    public static boolean removeCoins(String uuid, long amount) {
        if (amount <= 0) return true;
        if (DataManager.removeCoins(uuid, amount)) {
            DataManager.save(PoliticalServer.server);
            return true;
        }
        return false;
    }

    public static boolean hasCoins(ServerPlayerEntity player, long amount) {
        return getCoinsLong(player) >= amount;
    }

    public static boolean hasCoins(String uuid, long amount) {
        return getCoinsLong(uuid) >= amount;
    }
    // Convert 1000 coins to 1 credit

    public static boolean convertToCredits(ServerPlayerEntity player, long creditsToGet) {
        long coinsNeeded = 1000L * creditsToGet;
        if (!hasCoins(player, coinsNeeded)) {
            return false;
        }
        removeCoins(player, coinsNeeded);
        CreditItem.giveCredits(player, (int) creditsToGet);
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
