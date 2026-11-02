package com.political;


import com.political.economy.CoinManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.HashSet;
import java.util.Set;

/**
 * Late-game sink: pay 100M+ coins to join a syndicate that boosts coin income from drops and sales.
 */
public final class DeepstrikeSyndicateManager {

    public static final long JOIN_COST_COINS = 100_000_000L;
    private static final double INCOME_MULT = 1.18;

    public static Set<String> membersView() {
        var m = DataManager.getData().deepstrikeSyndicate;
        return m == null ? Set.of() : new HashSet<>(m);
    }

    public static boolean isMember(String uuid) {
        var m = DataManager.getData().deepstrikeSyndicate;
        return m != null && m.contains(uuid);
    }

    public static boolean tryJoin(ServerPlayer player) {
        String u = player.getUUID().toString();
        if (isMember(u)) {
            player.sendSystemMessage(Component.literal("You are already a syndicate member.").withStyle(ChatFormatting.YELLOW), false);
            return false;
        }
        if (!CoinManager.hasCoins(player, JOIN_COST_COINS)) {
            player.sendSystemMessage(Component.literal("You need " + String.format("%,d", JOIN_COST_COINS) + " coins.").withStyle(ChatFormatting.RED), false);
            return false;
        }
        if (!CoinManager.removeCoins(player, JOIN_COST_COINS)) {
            return false;
        }
        var list = DataManager.getData().deepstrikeSyndicate;
        if (list == null) {
            list = new java.util.ArrayList<>();
            DataManager.getData().deepstrikeSyndicate = list;
        }
        list.add(u);
        DataManager.save(PoliticalServer.server);
        player.sendSystemMessage(Component.literal("§6§lDeepstrike Syndicate §r§a— joined! §7+" + (int) ((INCOME_MULT - 1) * 100) + "% coin income from common sources."), false);
        return true;
    }

    /** Multiplier applied in CoinManager for online earners. */
    public static double getCoinIncomeMultiplier(ServerPlayer player) {
        if (player == null) return 1.0;
        return isMember(player.getUUID().toString()) ? INCOME_MULT : 1.0;
    }
}
