package com.political;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class InvseeGui {
    
    public static void open(ServerPlayerEntity admin, ServerPlayerEntity target, String type) {
        switch (type.toLowerCase()) {
            case "inventory":
                openInventory(admin, target);
                break;
            case "ender":
                openEnderChest(admin, target);
                break;
            case "bank":
                openBank(admin, target);
                break;
            case "coins":
                openCoins(admin, target);
                break;
            default:
                admin.sendMessage(Text.literal("Usage: /invsee <player> <inventory|ender|bank|coins>")
                    .formatted(Formatting.RED), false);
        }
    }
    
    private static void openInventory(ServerPlayerEntity admin, ServerPlayerEntity target) {
        admin.sendMessage(Text.literal("Viewing " + target.getName().getString() + "'s inventory")
            .formatted(Formatting.GREEN), false);
        
        // For now, just show inventory info - can be enhanced later
        admin.sendMessage(Text.literal("Main hand: " + target.getMainHandStack().getItem().getName().getString())
            .formatted(Formatting.GRAY), false);
        admin.sendMessage(Text.literal("Off hand: " + target.getOffHandStack().getItem().getName().getString())
            .formatted(Formatting.GRAY), false);
    }
    
    private static void openEnderChest(ServerPlayerEntity admin, ServerPlayerEntity target) {
        admin.sendMessage(Text.literal("Viewing " + target.getName().getString() + "'s ender chest")
            .formatted(Formatting.GREEN), false);
        
        // For now, just show ender chest info
        int slots = target.getEnderChestInventory().size();
        int filled = 0;
        for (int i = 0; i < slots; i++) {
            if (!target.getEnderChestInventory().getStack(i).isEmpty()) {
                filled++;
            }
        }
        admin.sendMessage(Text.literal("Ender chest: " + filled + "/" + slots + " slots used")
            .formatted(Formatting.GRAY), false);
    }
    
    private static void openBank(ServerPlayerEntity admin, ServerPlayerEntity target) {
        admin.sendMessage(Text.literal("Viewing " + target.getName().getString() + "'s bank")
            .formatted(Formatting.GREEN), false);
        
        // Open the bank GUI but with target's data
        BankGui.openForAdmin(admin, target);
    }
    
    private static void openCoins(ServerPlayerEntity admin, ServerPlayerEntity target) {
        long coins = CoinManager.getCoins(target.getUuidAsString());
        admin.sendMessage(Text.literal(target.getName().getString() + " has " + coins + " coins")
            .formatted(Formatting.GREEN), false);
        
        // Simple coin modification via chat commands
        admin.sendMessage(Text.literal("Use /setcoins " + target.getName().getString() + " <amount> to set coins")
            .formatted(Formatting.YELLOW), false);
    }
}
