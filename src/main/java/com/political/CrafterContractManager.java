package com.political;


import net.minecraft.util.Prediction;
import com.political.bounty.SlayerItems;
import com.political.economy.CoinManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.Map;
import java.util.Random;

/**
 * Paid PvE contract: kill hostiles for a random custom crafter core (T1–T3 weighted).
 */
public final class CrafterContractManager {

    public static final long ENTRY_FEE_COINS = 2_500_000L;
    public static final int KILLS_REQUIRED = 150;
    private static final Random RAND = new Random();

    private CrafterContractManager() {}

    private static Map<String, Integer> map() {
        return DataManager.getData().crafterContractKills;
    }

    public static boolean hasActive(Player player) {
        return map().containsKey(player.getUUID().toString());
    }

    public static int progress(Player player) {
        return map().getOrDefault(player.getUUID().toString(), 0);
    }

    public static boolean tryPurchase(ServerPlayer player) {
        String u = player.getUUID().toString();
        if (hasActive(player)) {
            player.sendSystemMessage(Component.literal("You already have an active crafter contract. Finish or /craftercontract abandon.").withStyle(ChatFormatting.YELLOW), false);
            return false;
        }
        if (!CoinManager.hasCoins(player, ENTRY_FEE_COINS)) {
            player.sendSystemMessage(Component.literal("Need " + String.format("%,d", ENTRY_FEE_COINS) + " coins.").withStyle(ChatFormatting.RED), false);
            return false;
        }
        if (!CoinManager.removeCoins(player, ENTRY_FEE_COINS)) {
            return false;
        }
        map().put(u, 0);
        DataManager.save(PoliticalServer.server);
        player.sendSystemMessage(Component.literal("§6Crafter contract §a— §7Slay §f" + KILLS_REQUIRED + " §7hostile mobs. Then §e/craftercontract claim§7.").withStyle(ChatFormatting.GOLD), false);
        return true;
    }

    public static void onHostileKill(ServerPlayer killer) {
        String u = killer.getUUID().toString();
        if (!map().containsKey(u)) {
            return;
        }
        int n = map().get(u) + 1;
        map().put(u, n);
        if (n == KILLS_REQUIRED || n % 25 == 0) {
            killer.sendSystemMessage(Component.literal("Crafter contract: " + n + " / " + KILLS_REQUIRED + " kills").withStyle(ChatFormatting.AQUA), true);
        }
    }

    public static void tryClaim(ServerPlayer player) {
        String u = player.getUUID().toString();
        if (!map().containsKey(u)) {
            player.sendSystemMessage(Component.literal("No active contract. Use /craftercontract buy").withStyle(ChatFormatting.RED), false);
            return;
        }
        int p = map().get(u);
        if (p < KILLS_REQUIRED) {
            player.sendSystemMessage(Component.literal("Progress: " + p + " / " + KILLS_REQUIRED).withStyle(ChatFormatting.YELLOW), false);
            return;
        }
        map().remove(u);
        DataManager.save(PoliticalServer.server);
        ItemStack core = rollCore();
        if (!player.addItem(core.copy())) {
            player.drop(core.copy(), false, Prediction.SERVER_ONLY);
        }
        player.sendSystemMessage(Component.literal("§a§lContract complete! §rYou received a crafter core.").withStyle(ChatFormatting.GREEN), false);
    }

    public static void abandon(ServerPlayer player) {
        String u = player.getUUID().toString();
        if (map().remove(u) != null) {
            DataManager.save(PoliticalServer.server);
            player.sendSystemMessage(Component.literal("Contract abandoned (no refund).").withStyle(ChatFormatting.GRAY), false);
        } else {
            player.sendSystemMessage(Component.literal("No active contract.").withStyle(ChatFormatting.RED), false);
        }
    }

    private static ItemStack rollCore() {
        float r = RAND.nextFloat();
        if (r < 0.65f) {
            return SlayerItems.createCustomCrafterCoreT1();
        }
        if (r < 0.92f) {
            return SlayerItems.createCustomCrafterCoreT2();
        }
        return SlayerItems.createCustomCrafterCoreT3();
    }
}
