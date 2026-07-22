package com.political;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public class InvseeGui {

    public static void open(ServerPlayer admin, ServerPlayer target, String type) {
        if (type == null || type.isBlank()) {
            AdminInvseeGui.openMainMenu(admin, target);
            return;
        }
        switch (type.toLowerCase()) {
            case "menu", "all", "hub" -> AdminInvseeGui.openMainMenu(admin, target);
            case "inventory", "inv" -> AdminInvseeGui.openPlayerInventory(admin, target);
            case "ender", "echest", "enderchest" -> AdminInvseeGui.openEnderChest(admin, target);
            case "bank" -> BankGui.openForAdmin(admin, target);
            case "coins", "wallet" -> AdminInvseeGui.openCoinsEditor(admin, target);
            case "crypto" -> AdminInvseeGui.openCryptoEditor(admin, target);
            case "stocks", "stock" -> AdminInvseeGui.openStockEditor(admin, target);
            case "prevchests", "chests" -> PrevChestsAdminGui.open(admin, target);
            default -> admin.sendSystemMessage(Component.literal("Usage: inventory | ender | bank | wallet | crypto | stocks | prevchests | menu")
                    .withStyle(ChatFormatting.RED), false);
        }
    }

    /**
     * Open invsee for offline player (limited to data-driven views)
     */
    public static void openOffline(ServerPlayer admin, java.util.UUID uuid, String name, String type) {
        if (type == null || type.isBlank()) {
            AdminInvseeGui.openOfflineMainMenu(admin, uuid, name);
            return;
        }
        String uuidStr = uuid.toString();
        switch (type.toLowerCase()) {
            case "menu", "all", "hub" -> AdminInvseeGui.openOfflineMainMenu(admin, uuid, name);
            case "bank" -> AdminInvseeGui.openOfflineBank(admin, uuidStr, name);
            case "coins", "wallet" -> AdminInvseeGui.openOfflineCoinsEditor(admin, uuidStr, name);
            case "crypto" -> AdminInvseeGui.openOfflineCryptoEditor(admin, uuidStr, name);
            case "stocks", "stock" -> AdminInvseeGui.openOfflineStockEditor(admin, uuidStr, name);
            default -> admin.sendSystemMessage(Component.literal("Offline player: limited to bank | wallet | crypto | stocks | menu")
                    .withStyle(ChatFormatting.RED), false);
        }
    }
}
