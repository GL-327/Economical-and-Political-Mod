package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class RecipesGui {

    // ============================================================
    // MAIN MENU - Simplified working version
    // ============================================================
    public static void openMainMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("📖 Recipe Browser"));

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Text.literal(""));
        for (int i = 0; i < 27; i++) gui.setSlot(i, bg.build());

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(Text.literal("📖 Recipe Browser").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🎨 Browse crafting recipes").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("⚡ View all categories").formatted(Formatting.GRAY))
                .glow().build());

        // Link to /setrecipe for admins
        gui.setSlot(11, new GuiElementBuilder(Items.COMMAND_BLOCK)
                .setName(Text.literal("⚙ Create Recipes").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🎨 Design custom recipes").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("🔧 Admin only - opens /setrecipe").formatted(Formatting.RED))
                .addLoreLine(Text.literal("✨ Click to open recipe editor").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> {
                    player.sendMessage(Text.literal("Opening recipe editor...").formatted(Formatting.GREEN), false);
                    RecipeEditorGui.openMainMenu(player);
                })
                .build());

        // Materials category (includes recent additions)
        gui.setSlot(13, new GuiElementBuilder(Items.SLIME_BALL)
                .setName(Text.literal("📦 Materials").formatted(Formatting.GRAY, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("📦 Cores, chunks & compacted items").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ New enchanted items added!").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMaterialsMenu(player))
                .build());

        // Weapon Attributes (recent addition)
        gui.setSlot(15, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("⚔ Weapon Attributes").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⚔ Weapon attribute tokens").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ New feature!").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openWeaponAttributesMenu(player))
                .build());

        gui.open();
    }

    // ============================================================
    // MATERIALS MENU - Shows recent additions
    // ============================================================
    private static void openMaterialsMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("📦 Materials"));

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Text.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.SLIME_BALL)
                .setName(Text.literal("📦 Materials").formatted(Formatting.GRAY, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("📦 All crafting materials").formatted(Formatting.GRAY))
                .glow().build());

        // Add cores and chunks
        int slot = 10;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            if (slot > 43) break;
            
            gui.setSlot(slot++, new GuiElementBuilder(SlayerItems.createCore(type))
                    .setName(SlayerItems.createCore(type).get(DataComponentTypes.CUSTOM_NAME))
                    .addLoreLine(Text.literal("✨ Click to view").formatted(Formatting.YELLOW))
                    .setCallback((idx, type2, action) -> showItemInfo(player, SlayerItems.createCore(type)))
                    .build());
            
            if (slot <= 43) {
                gui.setSlot(slot++, new GuiElementBuilder(SlayerItems.createChunk(type))
                        .setName(SlayerItems.createChunk(type).get(DataComponentTypes.CUSTOM_NAME))
                        .addLoreLine(Text.literal("✨ Click to view").formatted(Formatting.YELLOW))
                        .setCallback((idx, type2, action) -> showItemInfo(player, SlayerItems.createChunk(type)))
                        .build());
            }
        }

        // Add compacted items
        ItemStack[] compacted = {
            SlayerItems.createCompactedIron(),
            SlayerItems.createCompactedGold(),
            SlayerItems.createCompactedDiamond(),
            SlayerItems.createCompactedEmerald(),
            SlayerItems.createCompactedNetherite()
        };

        for (ItemStack item : compacted) {
            if (slot > 43) break;
            gui.setSlot(slot++, new GuiElementBuilder(item)
                    .setName(item.get(DataComponentTypes.CUSTOM_NAME))
                    .addLoreLine(Text.literal("✨ Click to view").formatted(Formatting.YELLOW))
                    .setCallback((idx, type2, action) -> showItemInfo(player, item))
                    .build());
        }

        // Add enchanted items (recent additions)
        ItemStack[] enchanted = {
            SlayerItems.createEnchantedCobblestone(),
            SlayerItems.createEnchantedStone(),
            SlayerItems.createEnchantedOakLog(),
            SlayerItems.createEnchantedRose(),
            SlayerItems.createEnchantedDandelion(),
            SlayerItems.createEnchantedWheat(),
            SlayerItems.createEnchantedCarrot(),
            SlayerItems.createEnchantedPotato()
        };

        for (ItemStack item : enchanted) {
            if (slot > 43) break;
            gui.setSlot(slot++, new GuiElementBuilder(item)
                    .setName(item.get(DataComponentTypes.CUSTOM_NAME))
                    .addLoreLine(Text.literal("✨ NEW! Enchanted item").formatted(Formatting.GREEN))
                    .addLoreLine(Text.literal("✨ Click to view").formatted(Formatting.YELLOW))
                    .setCallback((idx, type2, action) -> showItemInfo(player, item))
                    .build());
        }

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ============================================================
    // WEAPON ATTRIBUTES MENU - Recent addition
    // ============================================================
    private static void openWeaponAttributesMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⚔ Weapon Attributes"));

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Text.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("⚔ Weapon Attributes").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⚔ Weapon attribute tokens").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ NEW FEATURE!").formatted(Formatting.GREEN))
                .glow().build());

        // Add all weapon attribute tokens
        int slot = 10;
        for (WeaponAttribute attr : WeaponAttribute.values()) {
            if (slot > 43) break;
            
            ItemStack token = SlayerItems.createWeaponAttributeToken(attr);
            gui.setSlot(slot++, new GuiElementBuilder(token)
                    .setName(token.get(DataComponentTypes.CUSTOM_NAME))
                    .addLoreLine(Text.literal("✨ Weapon attribute token").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("✨ Click to view").formatted(Formatting.YELLOW))
                    .setCallback((idx, type2, action) -> showItemInfo(player, token))
                    .build());
        }

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ============================================================
    // HELPER: Show item information
    // ============================================================
    private static void showItemInfo(ServerPlayerEntity player, ItemStack item) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("Item Info"));

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Text.literal(""));
        for (int i = 0; i < 27; i++) gui.setSlot(i, bg.build());

        // Item display
        gui.setSlot(13, new GuiElementBuilder(item)
                .setName(item.get(DataComponentTypes.CUSTOM_NAME))
                .glow().build());

        // Info
        gui.setSlot(4, new GuiElementBuilder(Items.BOOK)
                .setName(Text.literal("Item Information").formatted(Formatting.BLUE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("📖 View recipes in /setrecipe").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("⚙ Admin command: /setrecipe").formatted(Formatting.GRAY))
                .build());

        // Back button
        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ============================================================
    // LEGACY METHOD - For backward compatibility
    // ============================================================
    public static boolean openSpecificRecipe(ServerPlayerEntity player, String itemName) {
        // For now, just open the main menu
        player.sendMessage(Text.literal("Recipe lookup: " + itemName + " - Browse categories instead").formatted(Formatting.YELLOW), false);
        openMainMenu(player);
        return true;
    }
}
