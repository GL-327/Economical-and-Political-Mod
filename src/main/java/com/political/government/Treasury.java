package com.political.government;

import com.political.DataManager;
import com.political.government.Perk;
import com.political.government.PerkManager;
import com.political.PoliticalServer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * One national ledger. Tax and VAT go here. Daily perk/cut spend is paid from here.
 * A negative balance is debt and accrues interest; effects stay active while in debt.
 */
public final class Treasury {

    public static final int VAT_MIN = 0;
    public static final int VAT_MAX = 30;
    public static final int VAT_DEFAULT = 5;
    public static final double DEBT_INTEREST = 0.05;
    public static final long DAY_MS = 24L * 60L * 60L * 1000L;
    public static final long DEPARTMENT_BASELINE = 25_000L;

    private Treasury() {}

    public static long balance() {
        return DataManager.getGovernmentTreasuryLong();
    }

    public static void deposit(long amount, String reason) {
        if (amount <= 0) return;
        DataManager.addGovernmentTreasury(amount);
        if (PoliticalServer.server != null) {
            DataManager.save(PoliticalServer.server);
        }
    }

    public static void charge(long amount) {
        DataManager.addGovernmentTreasury(-amount);
    }

    public static int vatPercent() {
        int vat = DataManager.getVatPercent();
        return Math.max(VAT_MIN, Math.min(VAT_MAX, vat));
    }

    public static void setVatPercent(int percent) {
        DataManager.setVatPercent(Math.max(VAT_MIN, Math.min(VAT_MAX, percent)));
        if (PoliticalServer.server != null) {
            DataManager.save(PoliticalServer.server);
        }
    }

    /** Extra coins the buyer pays on a listed price. Seller still receives {@code price}. */
    public static long vatOn(long price) {
        if (price <= 0) return 0;
        return (price * vatPercent() + 99) / 100;
    }

    public static long withVat(long price) {
        return price + vatOn(price);
    }

    public static long dailyPayroll() {
        long spend = Perk.Department.values().length * DEPARTMENT_BASELINE;
        for (String id : PerkManager.getActivePerks()) {
            Perk perk = PerkManager.getPerk(id);
            if (perk == null) continue;
            if (perk.kind == Perk.Kind.PERK) {
                spend += perk.dailyCost;
            } else if (perk.kind == Perk.Kind.CUT) {
                spend -= perk.dailySaving;
            }
        }
        return Math.max(0L, spend);
    }

    public static void tickDaily(MinecraftServer server) {
        DataManager.SaveData data = DataManager.getData();
        long now = System.currentTimeMillis();
        if (data.lastDailyBudgetMs <= 0) {
            data.lastDailyBudgetMs = now;
            return;
        }
        if (now - data.lastDailyBudgetMs < DAY_MS) {
            return;
        }
        data.lastDailyBudgetMs = now;

        long payroll = dailyPayroll();
        data.governmentTreasury -= payroll;
        if (data.governmentTreasury < 0) {
            long hole = -data.governmentTreasury;
            long interest = Math.max(1L, Math.round(hole * DEBT_INTEREST));
            data.governmentTreasury -= interest;
            broadcast(server, Component.literal("The treasury is in debt. Daily interest: "
                    + format(interest) + " coins.").withStyle(ChatFormatting.RED));
        }
        DataManager.save(server);
    }

    public static String format(long coins) {
        return String.format(java.util.Locale.ROOT, "%,d", coins);
    }

    public static Component statusLine() {
        long bal = balance();
        ChatFormatting color = bal < 0 ? ChatFormatting.RED : ChatFormatting.GREEN;
        String label = bal < 0 ? "Debt" : "Treasury";
        return Component.literal(label + ": " + format(bal) + " coins").withStyle(color);
    }

    private static void broadcast(MinecraftServer server, Component message) {
        if (server == null) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.sendSystemMessage(message);
        }
    }
}
