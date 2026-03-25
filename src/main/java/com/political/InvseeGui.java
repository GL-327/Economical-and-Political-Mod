package com.political;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class InvseeGui {

    public static void open(ServerPlayerEntity admin, ServerPlayerEntity target, String type) {
        if (type == null || type.isBlank()) {
            AdminInvseeGui.openMainMenu(admin, target);
            return;
        }
        switch (type.toLowerCase()) {
            case "menu", "all", "hub" -> AdminInvseeGui.openMainMenu(admin, target);
            case "inventory", "inv" -> AdminInvseeGui.openPlayerInventory(admin, target);
            case "ender", "echest" -> AdminInvseeGui.openEnderChest(admin, target);
            case "bank" -> BankGui.openForAdmin(admin, target);
            case "coins", "wallet" -> AdminInvseeGui.openCoinsEditor(admin, target);
            case "crypto" -> AdminInvseeGui.openCryptoEditor(admin, target);
            case "stocks" -> AdminInvseeGui.openStockEditor(admin, target);
            default -> admin.sendMessage(Text.literal("Usage: inventory | ender | bank | coins | crypto | stocks | menu")
                    .formatted(Formatting.RED), false);
        }
    }
}
