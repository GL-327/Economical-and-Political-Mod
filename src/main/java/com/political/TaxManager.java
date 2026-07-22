package com.political;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.GameType;

import java.util.HashMap;
import java.util.Map;

public class TaxManager {

    private static boolean taxEnabled = false;
    private static int dailyTaxAmount = 5;
    private static float dailyTaxPercentage = 0.05f; // 5% default
    private static long lastTaxTime = 0;
    private static Map<String, Integer> playerTaxOwed = new HashMap<>();

    private static final long DAY_MS = 24L * 60L * 60L * 1000L;

    public static void loadFromData(DataManager.SaveData data) {
        taxEnabled = data.taxEnabled;
        dailyTaxAmount = data.dailyTaxAmount;
        dailyTaxPercentage = data.dailyTaxPercentage > 0 ? data.dailyTaxPercentage : 0.05f;
        lastTaxTime = data.lastTaxTime;
        playerTaxOwed = data.playerTaxOwed != null ? new HashMap<>(data.playerTaxOwed) : new HashMap<>();
    }

    public static void saveToData(DataManager.SaveData data) {
        data.taxEnabled = taxEnabled;
        data.dailyTaxAmount = dailyTaxAmount;
        data.dailyTaxPercentage = dailyTaxPercentage;
        data.lastTaxTime = lastTaxTime;
        data.playerTaxOwed = new HashMap<>(playerTaxOwed);
    }

    public static boolean isTaxEnabled() {
        return taxEnabled;
    }

    public static void setTaxEnabled(boolean enabled) {
        taxEnabled = enabled;
        if (enabled) {
            lastTaxTime = System.currentTimeMillis();
        }
        DataManager.save(PoliticalServer.server);
    }

    public static void setDailyTaxAmount(int amount) {
        dailyTaxAmount = amount;
        DataManager.save(PoliticalServer.server);
    }

    public static int getDailyTaxAmount() {
        return dailyTaxAmount;
    }

    public static void setDailyTaxPercentage(float percentage) {
        dailyTaxPercentage = Math.max(0.01f, Math.min(1.0f, percentage)); // Minimum 1%, max 100%
        DataManager.save(PoliticalServer.server);
    }

    public static float getDailyTaxPercentage() {
        return dailyTaxPercentage;
    }

    public static int getTaxOwed(String uuid) {
        return playerTaxOwed.getOrDefault(uuid, 0);
    }

    public static void setTaxOwed(String uuid, int amount) {
        if (amount <= 0) {
            playerTaxOwed.remove(uuid);
        } else {
            playerTaxOwed.put(uuid, amount);
        }
        DataManager.save(PoliticalServer.server);
    }

    public static Map<String, Integer> getAllTaxOwed() {
        return new HashMap<>(playerTaxOwed);
    }

    public static boolean payTax(ServerPlayer player, int amount) {
        String uuid = player.getUUID().toString();
        int owed = getTaxOwed(uuid);
        int credits = CreditItem.countCredits(player);

        int toPay = Math.min(amount, Math.min(owed, credits));

        if (toPay > 0 && CreditItem.removeCredits(player, toPay)) {
            setTaxOwed(uuid, owed - toPay);
            PoliticalPartyManager.distributeTaxToParties(toPay);
            BuffProgressManager.onTaxPaid(player, toPay);

            if (getTaxOwed(uuid) < 50 && player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE) {
                player.setGameMode(GameType.SURVIVAL);
                player.sendSystemMessage(Component.literal("Your debt is below 50! You are no longer restricted.").withStyle(ChatFormatting.GREEN));
            }

            return true;
        }
        return false;
    }

    public static boolean payTaxWithCoins(ServerPlayer player, int amount) {
        String uuid = player.getUUID().toString();
        int owed = getTaxOwed(uuid);
        int coins = CoinManager.getCoins(uuid);

        int toPay = Math.min(amount, Math.min(owed, coins));

        if (toPay > 0 && CoinManager.removeCoins(player, toPay)) {
            setTaxOwed(uuid, owed - toPay);
            PoliticalPartyManager.distributeTaxToParties(toPay);
            BuffProgressManager.onTaxPaid(player, toPay);

            if (getTaxOwed(uuid) < 50 && player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE) {
                player.setGameMode(GameType.SURVIVAL);
                player.sendSystemMessage(Component.literal("Your debt is below 50! You are no longer restricted.").withStyle(ChatFormatting.GREEN));
            }

            return true;
        }
        return false;
    }

    public static void pardonPlayer(String uuid) {
        playerTaxOwed.remove(uuid);

        if (PoliticalServer.server != null) {
            ServerPlayer player = PoliticalServer.server.getPlayerList().getPlayer(java.util.UUID.fromString(uuid));
            if (player != null) {
                if (player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE) {
                    player.setGameMode(GameType.SURVIVAL);
                }
                player.sendSystemMessage(Component.literal("Your taxes have been pardoned!").withStyle(ChatFormatting.GREEN));
            }
        }

        DataManager.save(PoliticalServer.server);
    }

    public static void tick(MinecraftServer server) {
        if (!taxEnabled) {
            return;
        }

        long now = System.currentTimeMillis();

        if (now - lastTaxTime >= DAY_MS) {
            lastTaxTime = now;
            applyDailyTax(server);
        }
    }

    private static void applyDailyTax(MinecraftServer server) {
        String chair = DataManager.getChair();
        String viceChair = DataManager.getViceChair();
        String dictator = DictatorManager.getDictatorUuid();

        for (String uuid : DataManager.getAllPlayers().keySet()) {
            // Skip Chair, Vice Chair, and Dictator
            if (uuid.equals(chair) || uuid.equals(viceChair) || uuid.equals(dictator)) {
                continue;
            }

            // Calculate tax based on their coin balance
            int coins = CoinManager.getCoins(uuid);
            int taxAmount = (int) Math.ceil(coins * dailyTaxPercentage);

            // Remove coins directly
            if (CoinManager.removeCoins(uuid, taxAmount)) {
                PoliticalPartyManager.distributeTaxToParties(taxAmount);
                ServerPlayer player = server.getPlayerList().getPlayer(java.util.UUID.fromString(uuid));
                if (player != null) {
                    player.sendSystemMessage(Component.literal("Daily tax deducted: " + taxAmount + " coins (" +
                            String.format("%.1f", dailyTaxPercentage * 100) + "%)").withStyle(ChatFormatting.GOLD));
                }
            } else {
                // If they can't pay, add to debt
                int currentOwed = getTaxOwed(uuid);

                // Apply interest to existing debt
                if (currentOwed > 0) {
                    int interest = (int) Math.ceil(currentOwed * 0.5);
                    currentOwed += interest;
                }

                // Add new tax amount to debt
                setTaxOwed(uuid, currentOwed + taxAmount);

                ServerPlayer player = server.getPlayerList().getPlayer(java.util.UUID.fromString(uuid));
                if (player != null) {
                    player.sendSystemMessage(Component.literal("Tax applied: " + taxAmount + " coins - added to debt!").withStyle(ChatFormatting.RED));

                    // Check if they need to be put in adventure mode
                    if (currentOwed + taxAmount >= 50) {
                        player.setGameMode(GameType.ADVENTURE);
                        player.sendSystemMessage(Component.literal("Your tax debt has reached 50+! You are now restricted to Adventure mode.").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                    }
                }
            }
        }

        DataManager.save(server);
    }

    public static void checkPlayerJoin(ServerPlayer player) {
        if (!taxEnabled) {
            return;
        }

        int owed = getTaxOwed(player.getUUID().toString());
        if (owed > 0) {
            player.sendSystemMessage(Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.RED));
            player.sendSystemMessage(Component.literal("    ⚠ TAX REMINDER ⚠").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
            player.sendSystemMessage(Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.RED));
            player.sendSystemMessage(Component.literal("You owe: " + owed + " coins").withStyle(ChatFormatting.GOLD));
            player.sendSystemMessage(Component.literal("Use /tax to pay your taxes!").withStyle(ChatFormatting.YELLOW));
            player.sendSystemMessage(Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.RED));

            if (owed >= 50) {
                player.setGameMode(GameType.ADVENTURE);
                player.sendSystemMessage(Component.literal("You are restricted to Adventure mode until debt is below 50!").withStyle(ChatFormatting.DARK_RED));
            }
        }
    }
}