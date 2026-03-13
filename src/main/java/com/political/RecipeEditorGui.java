package com.political;

import com.political.backrooms.items.BackroomsItemsManager;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

/**
 * Simplified Recipe Editor GUI that works with BackroomsItemsManager
 */
public class RecipeEditorGui {

    public static void open(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("Recipe Editor").formatted(Formatting.GOLD));

        // Background fill
        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Text.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(Text.literal("📝 Backrooms Recipe Editor").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal("Create and manage backrooms recipes").formatted(Formatting.GRAY))
                .glow().build());

        // Backrooms Items Section
        gui.setSlot(13, new GuiElementBuilder(Items.CRAFTING_TABLE)
                .setName(Text.literal("🧪 Backrooms Items").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal("All backrooms consumables and tools").formatted(Formatting.GRAY))
                .glow().build());

        // Add backrooms items to grid
        List<ItemStack> backroomsItems = new ArrayList<>(BackroomsItemsManager.getAllItems().values());

        int slot = 19;
        for (ItemStack item : backroomsItems) {
            if (slot >= 45) break; // Don't overflow the grid
            gui.setSlot(slot, new GuiElementBuilder(item.getItem())
                    .setName(item.getName())
                    .addLoreLine(Text.literal("Click to select for crafting").formatted(Formatting.BLUE))
                    .build());
            slot++;
        }

        // Navigation buttons
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.RED))
                .addLoreLine(Text.literal("Return to main menu").formatted(Formatting.GRAY))
                .build());

        // Close button
        gui.setSlot(53, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("✖ Close").formatted(Formatting.RED))
                .addLoreLine(Text.literal("Close the recipe editor").formatted(Formatting.GRAY))
                .build());

        gui.open();
    }
}
