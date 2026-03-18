package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * GUI for managing player checkpoints.
 * Main view shows list of checkpoints, with options in a sub-GUI.
 */
public class CheckpointGui {
    
    public static void openMain(ServerPlayerEntity player) {
        List<CheckpointManager.Checkpoint> checkpoints = 
            CheckpointManager.getPlayerCheckpoints(player.getUuidAsString());
        
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("Your Checkpoints").formatted(Formatting.GOLD));
        
        // Fill background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }
        
        // Header with info
        gui.setSlot(4, new GuiElementBuilder(Items.ENDER_PEARL)
                .setName(Text.literal("Checkpoints (" + checkpoints.size() + "/" + CheckpointManager.getMaxCheckpoints() + ")").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal("Click a checkpoint to teleport").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Teleport Cost: 1000 coins").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Use the buttons below to manage").formatted(Formatting.GRAY))
                .glow().build());
        
        // List checkpoints (slots 10-16, 19-25 for up to 10)
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21};
        for (int i = 0; i < slots.length; i++) {
            if (i < checkpoints.size()) {
                CheckpointManager.Checkpoint cp = checkpoints.get(i);
                gui.setSlot(slots[i], new GuiElementBuilder(Items.ENDER_PEARL)
                        .setName(Text.literal(cp.name).formatted(Formatting.AQUA))
                        .addLoreLine(Text.literal("ID: " + cp.id).formatted(Formatting.GRAY))
                        .addLoreLine(Text.literal("World: " + cp.worldName.replace("minecraft:", "")).formatted(Formatting.GRAY))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("Cost: 1000 coins").formatted(Formatting.YELLOW))
                        .addLoreLine(Text.literal("Click to teleport!").formatted(Formatting.GREEN))
                        .setCallback((index, type, action) -> {
                            gui.close();
                            CheckpointManager.teleportToCheckpoint(player, cp.id);
                        })
                        .build());
            } else {
                gui.setSlot(slots[i], new GuiElementBuilder(Items.LIGHT_GRAY_STAINED_GLASS_PANE)
                        .setName(Text.literal("Empty Slot").formatted(Formatting.DARK_GRAY))
                        .build());
            }
        }
        
        // Management buttons at bottom
        gui.setSlot(38, new GuiElementBuilder(Items.EMERALD_BLOCK)
                .setName(Text.literal("Create Checkpoint").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("Cost: " + CheckpointManager.getCreationCost() + " coins").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Use: /checkpoint create <name>").formatted(Formatting.GRAY))
                .build());
        
        gui.setSlot(40, new GuiElementBuilder(Items.REDSTONE_BLOCK)
                .setName(Text.literal("Delete Checkpoint").formatted(Formatting.RED))
                .addLoreLine(Text.literal("Refund: " + CheckpointManager.getDeletionRefund() + " coins").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> openDeleteMenu(player))
                .build());
        
        gui.setSlot(42, new GuiElementBuilder(Items.NAME_TAG)
                .setName(Text.literal("Rename Checkpoint").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Cost: " + CheckpointManager.getRenameCost() + " coins").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Use: /checkpoint rename <id> <name>").formatted(Formatting.GRAY))
                .build());
        
        gui.open();
    }
    
    public static void openDeleteMenu(ServerPlayerEntity player) {
        List<CheckpointManager.Checkpoint> checkpoints = 
            CheckpointManager.getPlayerCheckpoints(player.getUuidAsString());
        
        if (checkpoints.isEmpty()) {
            player.sendMessage(Text.literal("You have no checkpoints to delete!")
                    .formatted(Formatting.RED));
            return;
        }
        
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("Delete Checkpoint").formatted(Formatting.RED));
        
        // Fill background
        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }
        
        // Back button
        gui.setSlot(0, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> openMain(player))
                .build());
        
        // List checkpoints for deletion
        for (int i = 0; i < checkpoints.size() && i < 26; i++) {
            CheckpointManager.Checkpoint cp = checkpoints.get(i);
            gui.setSlot(i + 1, new GuiElementBuilder(Items.TNT)
                    .setName(Text.literal("Delete: " + cp.name).formatted(Formatting.RED))
                    .addLoreLine(Text.literal("ID: " + cp.id).formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("Refund: " + CheckpointManager.getDeletionRefund() + " coins").formatted(Formatting.YELLOW))
                    .setCallback((index, type, action) -> {
                        gui.close();
                        CheckpointManager.deleteCheckpoint(player, cp.id);
                    })
                    .build());
        }
        
        gui.open();
    }
}
