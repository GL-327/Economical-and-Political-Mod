package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.Set;

/**
 * Admin GUI for managing player buffs.
 * Accessible via /playerbuffadmin command (admin only).
 */
public class PlayerBuffAdminGui {

    /**
     * Opens the player selection menu.
     */
    public static void openPlayerSelect(ServerPlayer admin) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, admin, false);
        gui.setTitle(Component.literal("✦ Player Buff Admin - Select Player"));

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Component.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.COMMAND_BLOCK)
                .setName(Component.literal("✦ Player Buff Admin").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Select a player to manage their buffs").withStyle(ChatFormatting.GRAY))
                .glow().build());

        // List online players
        MinecraftServer server = PoliticalServer.server;
        if (server != null) {
            int slot = 10;
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (slot > 43) break;
                if (slot == 18 || slot == 27 || slot == 36) slot++;

                int buffCount = PlayerBuffManager.getBuffObjects(player.getUUID().toString()).size();
                
                gui.setSlot(slot++, new GuiElementBuilder(Items.PLAYER_HEAD)
                        .setName(Component.literal(player.getName().getString()).withStyle(ChatFormatting.YELLOW))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("Buffs: " + buffCount).withStyle(ChatFormatting.GREEN))
                        .setCallback((idx, type, containerInput, slotGui) -> openBuffManage(admin, player))
                        .build());
            }
        }

        gui.open();
    }

    /**
     * Opens the buff management menu for a specific player.
     */
    public static void openBuffManage(ServerPlayer admin, ServerPlayer target) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, admin, false);
        gui.setTitle(Component.literal("✦ Manage Buffs: " + target.getName().getString()));

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Component.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        // Header - Target player info
        gui.setSlot(4, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal(target.getName().getString()).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Manage this player's buffs").withStyle(ChatFormatting.GRAY))
                .build());

        // Show all buffs with enable/disable buttons
        PlayerBuffManager.PlayerBuff[] buffs = PlayerBuffManager.PlayerBuff.values();
        int slot = 10;
        
        for (PlayerBuffManager.PlayerBuff buff : buffs) {
            if (slot > 43) break;
            if (slot == 18 || slot == 27 || slot == 36) slot++;
            
            boolean hasBuff = PlayerBuffManager.hasBuff(target.getUUID().toString(), buff);
            
            GuiElementBuilder builder = new GuiElementBuilder(buff.icon)
                    .setName(Component.literal(buff.colorCode + "◆ " + buff.displayName)
                            .withStyle(hasBuff ? ChatFormatting.BOLD : ChatFormatting.RESET))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Effect: ").withStyle(ChatFormatting.AQUA)
                            .append(Component.literal(buff.effect).withStyle(ChatFormatting.WHITE)))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal(hasBuff ? "● ENABLED" : "○ DISABLED")
                            .withStyle(hasBuff ? ChatFormatting.GREEN : ChatFormatting.RED))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to " + (hasBuff ? "disable" : "enable")).withStyle(ChatFormatting.YELLOW));
            
            if (hasBuff) {
                builder.glow();
            }
            
            final PlayerBuffManager.PlayerBuff buffRef = buff;
            builder.setCallback((idx, type, containerInput, slotGui) -> {
                if (hasBuff) {
                    PlayerBuffManager.removeBuff(target.getUUID().toString(), buffRef.id);
                    DataManager.save(PoliticalServer.server);
                    admin.sendSystemMessage(Component.literal("§c✓ Removed " + buffRef.displayName + " from " + target.getName().getString()), false);
                } else {
                    PlayerBuffManager.grantBuff(target, buffRef);
                    admin.sendSystemMessage(Component.literal("§a✓ Added " + buffRef.displayName + " to " + target.getName().getString()), false);
                }
                // Refresh the GUI
                openBuffManage(admin, target);
            });
            
            gui.setSlot(slot++, builder.build());
        }

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back to Player List").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openPlayerSelect(admin))
                .build());

        gui.open();
    }
}
