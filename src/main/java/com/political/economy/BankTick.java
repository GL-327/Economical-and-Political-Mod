package com.political.economy;

import com.political.DataManager;
import com.political.MasteryHeartTree;
import com.political.PlayerBuffManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.UUID;

/**
 * Hourly bank interest and loan collection. Chair-set rates tick here.
 */
public final class BankTick {

    private BankTick() {}

    public static void applyHourly(MinecraftServer server) {
        for (String uuid : DataManager.getAllBankAccountUuids()) {
            UUID playerUUID;
            try {
                playerUUID = UUID.fromString(uuid);
            } catch (IllegalArgumentException e) {
                continue;
            }

            boolean isChairOrJudge = uuid.equals(DataManager.getChair())
                    || uuid.equals(DataManager.getJudge());
            boolean isViceChair = uuid.equals(DataManager.getViceChair());

            long main = DataManager.getBankMain(uuid);
            if (main > 0 && !isChairOrJudge) {
                double mainRate;
                if (isViceChair) {
                    mainRate = 0.01 + PlayerBuffManager.getBankInterestBonus(uuid);
                } else {
                    mainRate = DataManager.getMainAccountInterestRate() + PlayerBuffManager.getBankInterestBonus(uuid);
                }
                mainRate = Math.min(0.15, mainRate + MasteryHeartTree.bankInterestBonus(uuid));
                long interest = (long) Math.floor(main * mainRate);
                if (interest > 0) {
                    DataManager.setBankMain(uuid, main + interest);
                }
            }

            long savings = DataManager.getBankSavings(uuid);
            if (savings > 0 && !isChairOrJudge) {
                double savingsRate;
                if (isViceChair) {
                    savingsRate = 0.05 + PlayerBuffManager.getBankInterestBonus(uuid);
                } else {
                    savingsRate = DataManager.getSavingsAccountInterestRate() + PlayerBuffManager.getBankInterestBonus(uuid);
                }
                savingsRate = Math.min(0.15, savingsRate + MasteryHeartTree.bankInterestBonus(uuid));
                long savingsInterest = (long) Math.floor(savings * savingsRate);
                if (savingsInterest > 0) {
                    DataManager.setBankSavings(uuid, savings + savingsInterest);
                }
            }

            List<DataManager.LoanEntry> loans = DataManager.getLoans(uuid);
            if (loans.isEmpty()) continue;

            for (int i = 0; i < loans.size(); i++) {
                DataManager.LoanEntry loan = loans.get(i);
                int loanInterest = (int) Math.ceil(loan.amount * loan.interestRate / 24.0);
                DataManager.setLoanAmount(uuid, i, loan.amount + loanInterest);
            }

            ServerPlayer onlinePlayer = server.getPlayerList().getPlayer(playerUUID);
            if (onlinePlayer == null) continue;

            for (int attempt = 0; attempt < DataManager.getLoans(uuid).size() + 1; attempt++) {
                List<DataManager.LoanEntry> currentLoans = DataManager.getLoans(uuid);
                if (currentLoans.isEmpty()) break;

                int highestIdx = 0;
                for (int i = 1; i < currentLoans.size(); i++) {
                    if (currentLoans.get(i).interestRate > currentLoans.get(highestIdx).interestRate) {
                        highestIdx = i;
                    }
                }
                int owed = currentLoans.get(highestIdx).amount;
                if (owed <= 0) {
                    DataManager.removeLoan(uuid, highestIdx);
                    continue;
                }
                long mainBal = DataManager.getBankMain(uuid);
                if (mainBal >= owed) {
                    DataManager.setBankMain(uuid, mainBal - owed);
                    DataManager.removeLoan(uuid, highestIdx);
                    onlinePlayer.sendSystemMessage(Component.literal("Loan of " + owed + " coins auto-collected from your Main Account.")
                            .withStyle(ChatFormatting.YELLOW), false);
                    continue;
                } else if (mainBal > 0) {
                    owed -= (int) Math.min(mainBal, owed);
                    DataManager.setBankMain(uuid, 0);
                    DataManager.setLoanAmount(uuid, highestIdx, owed);
                }
                long savingsBal = DataManager.getBankSavings(uuid);
                if (savingsBal >= owed) {
                    DataManager.setBankSavings(uuid, savingsBal - owed);
                    DataManager.removeLoan(uuid, highestIdx);
                    onlinePlayer.sendSystemMessage(Component.literal("Loan of " + owed + " coins auto-collected from your Savings Account.")
                            .withStyle(ChatFormatting.YELLOW), false);
                    continue;
                } else if (savingsBal > 0) {
                    owed -= (int) Math.min(savingsBal, owed);
                    DataManager.setBankSavings(uuid, 0);
                    DataManager.setLoanAmount(uuid, highestIdx, owed);
                }
                long purse = CoinManager.getCoinsLong(onlinePlayer);
                if (purse >= owed) {
                    CoinManager.removeCoins(onlinePlayer, owed);
                    DataManager.removeLoan(uuid, highestIdx);
                    onlinePlayer.sendSystemMessage(Component.literal("Loan of " + owed + " coins auto-collected from your purse.")
                            .withStyle(ChatFormatting.YELLOW), false);
                } else if (purse > 0) {
                    int fromPurse = (int) Math.min(purse, owed);
                    CoinManager.removeCoins(onlinePlayer, fromPurse);
                    DataManager.setLoanAmount(uuid, highestIdx, owed - fromPurse);
                    onlinePlayer.sendSystemMessage(Component.literal("Partial loan payment of " + fromPurse
                            + " coins collected. Remaining: " + (owed - fromPurse) + " coins.")
                            .withStyle(ChatFormatting.RED), false);
                }
                break;
            }
        }

        DataManager.save(server);
    }
}
