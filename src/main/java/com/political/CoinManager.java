package com.political;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public class CoinManager {

    private static int clampLongToInt(long value) {
        if (value > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        if (value < Integer.MIN_VALUE) return Integer.MIN_VALUE;
        return (int) value;
    }

    public static int getCoins(ServerPlayer player) {
        return clampLongToInt(DataManager.getCoins(player.getUUID().toString()));
    }

    public static int getCoins(String uuid) {
        return clampLongToInt(DataManager.getCoins(uuid));
    }

    /**
     * Long-precision coin getter for large bid/prices (e.g. underground auction).
     */
    public static long getCoinsLong(ServerPlayer player) {
        return DataManager.getCoins(player.getUUID().toString());
    }

    public static long getCoinsLong(String uuid) {
        return DataManager.getCoins(uuid);
    }

    /**
     * Apply Emerald armor coin bonus (+5% per piece worn).
     */
    private static long applyEmeraldBonus(ServerPlayer player, long amount) {
        if (player == null) return amount;
        double bonus = GemArmor.getEmeraldCoinBonus(player);
        if (bonus > 0) {
            long bonusAmount = (long) Math.ceil(amount * bonus);
            return amount + bonusAmount;
        }
        return amount;
    }

    private static long applySyndicateBonus(ServerPlayer player, long amount) {
        if (player == null || amount <= 0) return amount;
        double m = DeepstrikeSyndicateManager.getCoinIncomeMultiplier(player);
        if (m <= 1.0) return amount;
        return (long) Math.ceil(amount * m);
    }

    private static long applyPartyPerkCoinBonus(ServerPlayer player, long amount) {
        if (player == null || amount <= 0) return amount;
        double m = PartyPerkEffects.coinIncomeMultiplier(player.getUUID().toString());
        if (m <= 1.0) return amount;
        return (long) Math.ceil(amount * m);
    }

    public static void giveCoins(ServerPlayer player, long amount) {
        if (amount <= 0) return;
        long totalAmount = applyPartyPerkCoinBonus(player, applySyndicateBonus(player, applyEmeraldBonus(player, amount)));
        String uuid = player.getUUID().toString();
        DataManager.addCoins(uuid, totalAmount);
        BuffProgressManager.onCoinsEarned(player, totalAmount);
        CivilCraftAchievements.trackCoinsEarned(player, totalAmount);
        DataManager.save(PoliticalServer.server);
        player.sendSystemMessage(Component.literal("+" + totalAmount + " coins").withStyle(ChatFormatting.YELLOW));
    }
    public static void setCoins(String uuid, long amount) {
        DataManager.setCoins(uuid, Math.max(0, amount));
    }
    public static void giveCoinsQuiet(ServerPlayer player, long amount) {
        if (amount <= 0) return;
        long totalAmount = applyPartyPerkCoinBonus(player, applySyndicateBonus(player, applyEmeraldBonus(player, amount)));
        DataManager.addCoins(player.getUUID().toString(), totalAmount);
        BuffProgressManager.onCoinsEarned(player, totalAmount);
        CivilCraftAchievements.trackCoinsEarned(player, totalAmount);
        DataManager.save(PoliticalServer.server);
    }

    public static void giveCoins(String uuid, long amount) {
        if (amount <= 0) return;
        DataManager.addCoins(uuid, amount);
        if (PoliticalServer.server != null) {
            ServerPlayer pl = PoliticalServer.server.getPlayerList().getPlayer(java.util.UUID.fromString(uuid));
            if (pl != null) {
                BuffProgressManager.onCoinsEarned(pl, amount);
                CivilCraftAchievements.trackCoinsEarned(pl, amount);
            } else {
                DataManager.addTotalCoinsEarned(uuid, amount);
            }
        }
        DataManager.save(PoliticalServer.server);
    }

    public static boolean removeCoins(ServerPlayer player, long amount) {
        if (amount <= 0) return true;
        if (DataManager.removeCoins(player.getUUID().toString(), amount)) {
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

    public static boolean hasCoins(ServerPlayer player, long amount) {
        return getCoinsLong(player) >= amount;
    }

    public static boolean hasCoins(String uuid, long amount) {
        return getCoinsLong(uuid) >= amount;
    }
    // Convert 1000 coins to 1 credit

    public static boolean convertToCredits(ServerPlayer player, long creditsToGet) {
        long coinsNeeded = 1000L * creditsToGet;
        if (!hasCoins(player, coinsNeeded)) {
            return false;
        }
        removeCoins(player, coinsNeeded);
        CreditItem.giveCredits(player, (int) creditsToGet);
        return true;
    }

    // Convert 1 credit to 1000 coins
    public static boolean convertToCoins(ServerPlayer player, int creditsToSpend) {
        if (!CreditItem.hasCredits(player, creditsToSpend)) {
            return false;
        }
        CreditItem.removeCredits(player, creditsToSpend);
        giveCoins(player, creditsToSpend * 1000);
        return true;
    }
}
