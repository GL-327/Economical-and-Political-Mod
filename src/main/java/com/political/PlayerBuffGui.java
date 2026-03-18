package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Set;

/**
 * GUI for viewing all player buffs - their effects and how to obtain them.
 * Accessible via /playerbuff info command.
 */
public class PlayerBuffGui {

    public static void openMainMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("✦ Player Buffs Guide"));

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Text.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("✦ Player Buffs Guide").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Buffs are permanent once earned!").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Your active buffs: " + PlayerBuffManager.getBuffObjects(player.getUuidAsString()).size()).formatted(Formatting.YELLOW))
                .glow().build());

        // Show all buffs
        PlayerBuffManager.PlayerBuff[] buffs = PlayerBuffManager.PlayerBuff.values();
        int slot = 10;
        
        for (PlayerBuffManager.PlayerBuff buff : buffs) {
            if (slot > 43) break;
            if (slot == 18 || slot == 27 || slot == 36) slot++; // Skip first column of new rows
            
            boolean hasBuff = PlayerBuffManager.hasBuff(player.getUuidAsString(), buff);
            
            GuiElementBuilder builder = new GuiElementBuilder(buff.icon)
                    .setName(Text.literal(buff.colorCode + "◆ " + buff.displayName)
                            .formatted(hasBuff ? Formatting.BOLD : Formatting.RESET))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Effect: ").formatted(Formatting.AQUA)
                            .append(Text.literal(buff.effect).formatted(Formatting.WHITE)))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("How to obtain:").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("  " + buff.obtainMethod).formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("  Progress: " + BuffProgressManager.getProgressString(player.getUuidAsString(), buff)).formatted(Formatting.GREEN))
                    .addLoreLine(Text.literal(""));
            
            if (hasBuff) {
                builder.addLoreLine(Text.literal("● UNLOCKED").formatted(Formatting.GREEN, Formatting.BOLD))
                       .addLoreLine(Text.literal("  Permanent buff active!").formatted(Formatting.YELLOW))
                       .glow();
            } else {
                builder.addLoreLine(Text.literal("○ Locked").formatted(Formatting.DARK_GRAY));
            }
            
            gui.setSlot(slot++, builder.build());
        }

        // Your Active Buffs button
        gui.setSlot(49, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Text.literal("Your Active Buffs").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .setCallback((idx, type, action) -> openYourBuffsMenu(player))
                .build());

        gui.open();
    }

    public static void openYourBuffsMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("✦ Your Buffs"));

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Text.literal(""));
        for (int i = 0; i < 27; i++) gui.setSlot(i, bg.build());

        Set<PlayerBuffManager.PlayerBuff> buffs = PlayerBuffManager.getBuffObjects(player.getUuidAsString());
        
        if (buffs.isEmpty()) {
            gui.setSlot(13, new GuiElementBuilder(Items.BARRIER)
                    .setName(Text.literal("No Buffs Yet").formatted(Formatting.RED))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Complete achievements to earn buffs!").formatted(Formatting.GRAY))
                    .build());
        } else {
            int slot = 10;
            for (PlayerBuffManager.PlayerBuff buff : buffs) {
                if (slot > 16) break;
                
                GuiElementBuilder builder = new GuiElementBuilder(buff.icon)
                        .setName(Text.literal(buff.colorCode + "◆ " + buff.displayName))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("Effect: ").formatted(Formatting.AQUA)
                                .append(Text.literal(buff.effect).formatted(Formatting.WHITE)))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("● ACTIVE").formatted(Formatting.GREEN))
                        .addLoreLine(Text.literal("  Permanent").formatted(Formatting.YELLOW))
                        .glow();
                
                gui.setSlot(slot++, builder.build());
            }
        }

        // Back button
        gui.setSlot(18, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player))
                .build());

        // Info button
        gui.setSlot(22, new GuiElementBuilder(Items.BOOK)
                .setName(Text.literal("Buff Info").formatted(Formatting.AQUA))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Buffs are permanent once earned!").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Complete achievements to unlock them.").formatted(Formatting.GRAY))
                .build());

        gui.open();
    }
}
