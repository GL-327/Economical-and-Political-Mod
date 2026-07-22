package com.political;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Death penalty rules:
 * <ul>
 *   <li><b>All deaths:</b> Lose 50% of purse (on-hand coins).</li>
 *   <li><b>PvP kills:</b> Victim additionally loses 10% of total wealth
 *       (purse + bank + savings), split proportionally from each source.
 *       The killer receives that 10%. Total wealth is calculated BEFORE
 *       the 50% purse loss is applied.</li>
 *   <li>CoinTotem protection skips ALL coin loss.</li>
 * </ul>
 */
public class DeathPenaltyManager {

    private static final Set<UUID> RECENT_DEATHS = ConcurrentHashMap.newKeySet();

    public static void clearRecentDeaths() { RECENT_DEATHS.clear(); }

    // ════════════════════════════════════════════════════════════
    // DEATH HANDLING
    // ════════════════════════════════════════════════════════════

    public static void onPlayerDeath(ServerPlayer victim, DamageSource source) {
        if (!RECENT_DEATHS.add(victim.getUUID())) return;
        String victimUuid = victim.getUUID().toString();

        boolean totemProtected = CoinTotemManager.isCoinLossProtected(victim);

        long purseBefore = CoinManager.getCoinsLong(victimUuid);
        long bankBefore  = DataManager.getBankMain(victimUuid);
        long savingsBefore = DataManager.getBankSavings(victimUuid);
        long totalWealth = purseBefore + bankBefore + savingsBefore;

        if (totemProtected) {
            victim.sendSystemMessage(Component.literal("§6☠ Your Totem protected you from coin loss!"), false);
            CoinTotemManager.clearProtection(victim);

            if (source.getEntity() instanceof ServerPlayer killer) {
                PoliticalServer.LOGGER.info("[DeathPenalty] PvP kill " + killer.getName().getString()
                        + " -> " + victim.getName().getString() + " (totem protected)");
            }
            DataManager.save(PoliticalServer.server);
            return;
        }

        // 50% purse loss (all deaths)
        long purseLoss = purseBefore / 2;
        if (purseLoss > 0) {
            CoinManager.removeCoins(victim, purseLoss);
        }

        victim.sendSystemMessage(Component.literal(
                "§c☠ You died! Lost §e" + formatCoins(purseLoss) + " §ccoins (50% purse)"), false);
        BuffProgressManager.onDeath(victim, purseLoss);

        CoinTotemManager.clearProtection(victim);

        if (source.getEntity() instanceof ServerPlayer killer) {
            handlePlayerKill(victim, killer, purseBefore, bankBefore, savingsBefore, totalWealth);
        }

        DataManager.save(PoliticalServer.server);
    }

    /**
     * PvP: killer receives 10% of victim's total wealth (purse + bank + savings),
     * deducted proportionally from each source. Calculated on pre-death-penalty totals.
     */
    private static void handlePlayerKill(ServerPlayer victim, ServerPlayer killer,
                                         long purseBefore, long bankBefore,
                                         long savingsBefore, long totalWealth) {
        String victimUuid = victim.getUUID().toString();
        String killerUuid = killer.getUUID().toString();

        if (totalWealth <= 0) {
            PoliticalServer.LOGGER.info("[DeathPenalty] PvP kill " + killer.getName().getString()
                    + " -> " + victim.getName().getString() + " (no wealth to syphon)");
            return;
        }

        long syphonTotal = totalWealth / 10;
        if (syphonTotal <= 0) {
            PoliticalServer.LOGGER.info("[DeathPenalty] PvP kill " + killer.getName().getString()
                    + " -> " + victim.getName().getString() + " (syphon rounds to 0)");
            return;
        }

        // Split proportionally across purse, bank, savings
        long fromPurse   = (totalWealth > 0) ? (syphonTotal * purseBefore   / totalWealth) : 0;
        long fromBank    = (totalWealth > 0) ? (syphonTotal * bankBefore    / totalWealth) : 0;
        long fromSavings = syphonTotal - fromPurse - fromBank; // remainder keeps sum exact

        // Deduct from each source (purse already had 50% removed, deduct from what remains)
        long purseNow = CoinManager.getCoinsLong(victimUuid);
        long actualFromPurse = Math.min(fromPurse, purseNow);
        if (actualFromPurse > 0) {
            CoinManager.removeCoins(victim, actualFromPurse);
        }

        long actualFromBank = Math.min(fromBank, bankBefore);
        if (actualFromBank > 0) {
            long currentBank = DataManager.getBankMain(victimUuid);
            DataManager.setBankMain(victimUuid, (int) Math.max(0, currentBank - actualFromBank));
        }

        long actualFromSavings = Math.min(fromSavings, savingsBefore);
        if (actualFromSavings > 0) {
            long currentSavings = DataManager.getBankSavings(victimUuid);
            DataManager.setBankSavings(victimUuid, (int) Math.max(0, currentSavings - actualFromSavings));
        }

        long actualSyphon = actualFromPurse + actualFromBank + actualFromSavings;
        if (actualSyphon > 0) {
            CoinManager.giveCoins(killerUuid, actualSyphon);
            BuffProgressManager.onSyphon(killer, actualSyphon);
        }

        victim.sendSystemMessage(Component.literal(
                "§c☠ " + killer.getName().getString() + " syphoned §e" + formatCoins(actualSyphon)
                        + " §ccoins (10% of your total wealth)"), false);

        killer.sendSystemMessage(Component.literal(
                "§6⚔ Received §e" + formatCoins(actualSyphon) + " §6from "
                        + victim.getName().getString() + " (10% wealth syphon)"), false);

        PoliticalServer.LOGGER.info("[DeathPenalty] PvP " + killer.getName().getString()
                + " killed " + victim.getName().getString()
                + " syphon=" + actualSyphon
                + " (purse=" + actualFromPurse + " bank=" + actualFromBank
                + " savings=" + actualFromSavings + ")");
    }

    // ════════════════════════════════════════════════════════════
    // UTILITY
    // ════════════════════════════════════════════════════════════

    private static String formatCoins(long coins) {
        if (coins >= 1_000_000_000) {
            return String.format("%.1fB", coins / 1_000_000_000.0);
        } else if (coins >= 1_000_000) {
            return String.format("%.1fM", coins / 1_000_000.0);
        } else if (coins >= 1_000) {
            return String.format("%.1fK", coins / 1_000.0);
        }
        return String.valueOf(coins);
    }

    /**
     * Coins lost on any death: 50% of purse.
     */
    public static long getPotentialDeathLoss(String uuid) {
        return CoinManager.getCoinsLong(uuid) / 2;
    }

    /**
     * Total coins lost on a PvP death: 50% purse + 10% total wealth.
     */
    public static long getPotentialPKDeathLoss(String uuid) {
        long purse   = CoinManager.getCoinsLong(uuid);
        long bank    = DataManager.getBankMain(uuid);
        long savings = DataManager.getBankSavings(uuid);
        long totalWealth = purse + bank + savings;
        return (purse / 2) + (totalWealth / 10);
    }

    /**
     * Coins a killer receives from a PvP kill: 10% of victim's total wealth.
     */
    public static long getPotentialSyphon(String uuid) {
        long purse   = CoinManager.getCoinsLong(uuid);
        long bank    = DataManager.getBankMain(uuid);
        long savings = DataManager.getBankSavings(uuid);
        return (purse + bank + savings) / 10;
    }
}
