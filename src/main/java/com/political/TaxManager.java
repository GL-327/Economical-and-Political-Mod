package com.political;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;

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
        lastTaxTime = data.lastTaxTime;
        playerTaxOwed = data.playerTaxOwed != null ? new HashMap<>(data.playerTaxOwed) : new HashMap<>();
    }

    public static void saveToData(DataManager.SaveData data) {
        data.taxEnabled = taxEnabled;
        data.dailyTaxAmount = dailyTaxAmount;
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
        dailyTaxPercentage = Math.max(0.01f, Math.min(1.0f, percentage)); // 1% to 100%
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

    public static boolean payTax(ServerPlayerEntity player, int amount) {
        String uuid = player.getUuidAsString();
        int owed = getTaxOwed(uuid);
        int credits = CreditItem.countCredits(player);

        int toPay = Math.min(amount, Math.min(owed, credits));

        if (toPay > 0 && CreditItem.removeCredits(player, toPay)) {
            setTaxOwed(uuid, owed - toPay);

            if (getTaxOwed(uuid) < 50 && player.interactionManager.getGameMode() == GameMode.ADVENTURE) {
                player.changeGameMode(GameMode.SURVIVAL);
                player.sendMessage(Text.literal("Your debt is below 50! You are no longer restricted.").formatted(Formatting.GREEN));
            }

            return true;
        }
        return false;
    }

    public static boolean payTaxWithCoins(ServerPlayerEntity player, int amount) {
        String uuid = player.getUuidAsString();
        int owed = getTaxOwed(uuid);
        int coins = CoinManager.getCoins(uuid);

        int toPay = Math.min(amount, Math.min(owed, coins));

        if (toPay > 0 && CoinManager.removeCoins(player, toPay)) {
            setTaxOwed(uuid, owed - toPay);

            if (getTaxOwed(uuid) < 50 && player.interactionManager.getGameMode() == GameMode.ADVENTURE) {
                player.changeGameMode(GameMode.SURVIVAL);
                player.sendMessage(Text.literal("Your debt is below 50! You are no longer restricted.").formatted(Formatting.GREEN));
            }

            return true;
        }
        return false;
    }

    public static void pardonPlayer(String uuid) {
        playerTaxOwed.remove(uuid);

        if (PoliticalServer.server != null) {
            ServerPlayerEntity player = PoliticalServer.server.getPlayerManager().getPlayer(java.util.UUID.fromString(uuid));
            if (player != null) {
                if (player.interactionManager.getGameMode() == GameMode.ADVENTURE) {
                    player.changeGameMode(GameMode.SURVIVAL);
                }
                player.sendMessage(Text.literal("Your taxes have been pardoned!").formatted(Formatting.GREEN));
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
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(java.util.UUID.fromString(uuid));
                if (player != null) {
                    player.sendMessage(Text.literal("Daily tax deducted: " + taxAmount + " coins (" +
                            String.format("%.1f", dailyTaxPercentage * 100) + "%)").formatted(Formatting.GOLD));
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

                ServerPlayerEntity player = server.getPlayerManager().getPlayer(java.util.UUID.fromString(uuid));
                if (player != null) {
                    player.sendMessage(Text.literal("Tax applied: " + taxAmount + " coins - added to debt!").formatted(Formatting.RED));

                    // Check if they need to be put in adventure mode
                    if (currentOwed + taxAmount >= 50) {
                        player.changeGameMode(GameMode.ADVENTURE);
                        player.sendMessage(Text.literal("Your tax debt has reached 50+! You are now restricted to Adventure mode.").formatted(Formatting.RED, Formatting.BOLD));
                    }
                }
            }
        }

        DataManager.save(server);
    }

    public static void checkPlayerJoin(ServerPlayerEntity player) {
        if (!taxEnabled) {
            return;
        }

        int owed = getTaxOwed(player.getUuidAsString());
        if (owed > 0) {
            player.sendMessage(Text.literal("═══════════════════════════════════").formatted(Formatting.RED));
            player.sendMessage(Text.literal("    ⚠ TAX REMINDER ⚠").formatted(Formatting.RED, Formatting.BOLD));
            player.sendMessage(Text.literal("═══════════════════════════════════").formatted(Formatting.RED));
            player.sendMessage(Text.literal("You owe: " + owed + " coins").formatted(Formatting.GOLD));
            player.sendMessage(Text.literal("Use /tax to pay your taxes!").formatted(Formatting.YELLOW));
            player.sendMessage(Text.literal("═══════════════════════════════════").formatted(Formatting.RED));

            if (owed >= 50) {
                player.changeGameMode(GameMode.ADVENTURE);
                player.sendMessage(Text.literal("You are restricted to Adventure mode until debt is below 50!").formatted(Formatting.DARK_RED));
            }
        }
    }
}