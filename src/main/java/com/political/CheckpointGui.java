package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.List;

/**
 * GUI for managing player checkpoints.
 * Main view shows list of checkpoints, with options in a sub-GUI.
 */
public class CheckpointGui {
    
    public static void openMain(ServerPlayer player) {
        List<CheckpointManager.Checkpoint> checkpoints = 
            CheckpointManager.getPlayerCheckpoints(player.getUUID().toString());
        
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Your Checkpoints").withStyle(ChatFormatting.GOLD));
        
        // Fill background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray())
                    .setName(Component.literal("")).build());
        }
        
        // Header with info
        gui.setSlot(4, new GuiElementBuilder(Items.ENDER_PEARL)
                .setName(Component.literal("Checkpoints (" + checkpoints.size() + "/" + CheckpointManager.getMaxCheckpoints() + ")").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Click a checkpoint to teleport").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Teleport Cost: 1000 coins").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("Use the buttons below to manage").withStyle(ChatFormatting.GRAY))
                .glow().build());
        
        // List checkpoints (slots 10-16, 19-25 for up to 10)
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21};
        for (int i = 0; i < slots.length; i++) {
            if (i < checkpoints.size()) {
                CheckpointManager.Checkpoint cp = checkpoints.get(i);
                gui.setSlot(slots[i], new GuiElementBuilder(Items.ENDER_PEARL)
                        .setName(Component.literal(cp.name).withStyle(ChatFormatting.AQUA))
                        .addLoreLine(Component.literal("ID: " + cp.id).withStyle(ChatFormatting.GRAY))
                        .addLoreLine(Component.literal("World: " + cp.worldName.replace("minecraft:", "")).withStyle(ChatFormatting.GRAY))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("Cost: 1000 coins").withStyle(ChatFormatting.YELLOW))
                        .addLoreLine(Component.literal("Click to teleport!").withStyle(ChatFormatting.GREEN))
                        .setCallback((index, type, containerInput, slotGui) -> {
                            gui.close();
                            CheckpointManager.teleportToCheckpoint(player, cp.id);
                        })
                        .build());
            } else {
                gui.setSlot(slots[i], new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray())
                        .setName(Component.literal("Empty Slot").withStyle(ChatFormatting.DARK_GRAY))
                        .build());
            }
        }
        
        // Management buttons at bottom
        gui.setSlot(38, new GuiElementBuilder(Items.EMERALD_BLOCK)
                .setName(Component.literal("Create Checkpoint").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("Cost: " + CheckpointManager.getCreationCost() + " coins").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Use: /checkpoint create <name>").withStyle(ChatFormatting.GRAY))
                .build());
        
        gui.setSlot(40, new GuiElementBuilder(Items.REDSTONE_BLOCK)
                .setName(Component.literal("Delete Checkpoint").withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal("Refund: " + CheckpointManager.getDeletionRefund() + " coins").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> openDeleteMenu(player))
                .build());
        
        gui.setSlot(42, new GuiElementBuilder(Items.NAME_TAG)
                .setName(Component.literal("Rename Checkpoint").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("Cost: " + CheckpointManager.getRenameCost() + " coins").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Use: /checkpoint rename <id> <name>").withStyle(ChatFormatting.GRAY))
                .build());
        
        gui.open();
    }
    
    public static void openDeleteMenu(ServerPlayer player) {
        List<CheckpointManager.Checkpoint> checkpoints = 
            CheckpointManager.getPlayerCheckpoints(player.getUUID().toString());
        
        if (checkpoints.isEmpty()) {
            player.sendSystemMessage(Component.literal("You have no checkpoints to delete!")
                    .withStyle(ChatFormatting.RED));
            return;
        }
        
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("Delete Checkpoint").withStyle(ChatFormatting.RED));
        
        // Fill background
        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray())
                    .setName(Component.literal("")).build());
        }
        
        // Back button
        gui.setSlot(0, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> openMain(player))
                .build());
        
        // List checkpoints for deletion
        for (int i = 0; i < checkpoints.size() && i < 26; i++) {
            CheckpointManager.Checkpoint cp = checkpoints.get(i);
            gui.setSlot(i + 1, new GuiElementBuilder(Items.TNT)
                    .setName(Component.literal("Delete: " + cp.name).withStyle(ChatFormatting.RED))
                    .addLoreLine(Component.literal("ID: " + cp.id).withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("Refund: " + CheckpointManager.getDeletionRefund() + " coins").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        gui.close();
                        CheckpointManager.deleteCheckpoint(player, cp.id);
                    })
                    .build());
        }
        
        gui.open();
    }
}
