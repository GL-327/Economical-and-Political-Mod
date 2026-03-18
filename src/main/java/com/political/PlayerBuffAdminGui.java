package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Set;

/**
 * Admin GUI for managing player buffs.
 * Accessible via /playerbuffadmin command (admin only).
 */
public class PlayerBuffAdminGui {

    /**
     * Opens the player selection menu.
     */
    public static void openPlayerSelect(ServerPlayerEntity admin) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, admin, false);
        gui.setTitle(Text.literal("✦ Player Buff Admin - Select Player"));

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Text.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.COMMAND_BLOCK)
                .setName(Text.literal("✦ Player Buff Admin").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Select a player to manage their buffs").formatted(Formatting.GRAY))
                .glow().build());

        // List online players
        MinecraftServer server = PoliticalServer.server;
        if (server != null) {
            int slot = 10;
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (slot > 43) break;
                if (slot == 18 || slot == 27 || slot == 36) slot++;

                int buffCount = PlayerBuffManager.getBuffObjects(player.getUuidAsString()).size();
                
                gui.setSlot(slot++, new GuiElementBuilder(Items.PLAYER_HEAD)
                        .setName(Text.literal(player.getName().getString()).formatted(Formatting.YELLOW))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("Buffs: " + buffCount).formatted(Formatting.GREEN))
                        .setCallback((idx, type, action) -> openBuffManage(admin, player))
                        .build());
            }
        }

        gui.open();
    }

    /**
     * Opens the buff management menu for a specific player.
     */
    public static void openBuffManage(ServerPlayerEntity admin, ServerPlayerEntity target) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, admin, false);
        gui.setTitle(Text.literal("✦ Manage Buffs: " + target.getName().getString()));

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Text.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        // Header - Target player info
        gui.setSlot(4, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Text.literal(target.getName().getString()).formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Manage this player's buffs").formatted(Formatting.GRAY))
                .build());

        // Show all buffs with enable/disable buttons
        PlayerBuffManager.PlayerBuff[] buffs = PlayerBuffManager.PlayerBuff.values();
        int slot = 10;
        
        for (PlayerBuffManager.PlayerBuff buff : buffs) {
            if (slot > 43) break;
            if (slot == 18 || slot == 27 || slot == 36) slot++;
            
            boolean hasBuff = PlayerBuffManager.hasBuff(target.getUuidAsString(), buff);
            
            GuiElementBuilder builder = new GuiElementBuilder(buff.icon)
                    .setName(Text.literal(buff.colorCode + "◆ " + buff.displayName)
                            .formatted(hasBuff ? Formatting.BOLD : Formatting.RESET))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Effect: ").formatted(Formatting.AQUA)
                            .append(Text.literal(buff.effect).formatted(Formatting.WHITE)))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal(hasBuff ? "● ENABLED" : "○ DISABLED")
                            .formatted(hasBuff ? Formatting.GREEN : Formatting.RED))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Click to " + (hasBuff ? "disable" : "enable")).formatted(Formatting.YELLOW));
            
            if (hasBuff) {
                builder.glow();
            }
            
            final PlayerBuffManager.PlayerBuff buffRef = buff;
            builder.setCallback((idx, type, action) -> {
                if (hasBuff) {
                    PlayerBuffManager.removeBuff(target.getUuidAsString(), buffRef.id);
                    DataManager.save(PoliticalServer.server);
                    admin.sendMessage(Text.literal("§c✓ Removed " + buffRef.displayName + " from " + target.getName().getString()), false);
                } else {
                    PlayerBuffManager.grantBuff(target, buffRef);
                    admin.sendMessage(Text.literal("§a✓ Added " + buffRef.displayName + " to " + target.getName().getString()), false);
                }
                // Refresh the GUI
                openBuffManage(admin, target);
            });
            
            gui.setSlot(slot++, builder.build());
        }

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Player List").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openPlayerSelect(admin))
                .build());

        gui.open();
    }
}
