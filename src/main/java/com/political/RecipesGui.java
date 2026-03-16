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

        // T1 Swords category
        gui.setSlot(11, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Text.literal("⚔ T1 Bounty Swords").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⚔ Tier 1 slayer swords").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openT1SwordsMenu(player))
                .build());

        // T2 Swords category
        gui.setSlot(12, new GuiElementBuilder(Items.DIAMOND_SWORD)
                .setName(Text.literal("⚔ T2 Bounty Swords").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⚔ Tier 2 slayer swords").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openT2SwordsMenu(player))
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

        // T1 Armor category
        gui.setSlot(14, new GuiElementBuilder(Items.IRON_CHESTPLATE)
                .setName(Text.literal("🛡 T1 Bounty Armor").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🛡 Tier 1 slayer armor").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openT1ArmorMenu(player))
                .build());

        // Special Items category
        gui.setSlot(22, new GuiElementBuilder(Items.STICK)
                .setName(Text.literal("🎁 Special Items").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🎁 Special unique items").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openSpecialItemsMenu(player))
                .build());

        // Piglin Items category
        gui.setSlot(24, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Text.literal("� Piglin Items").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("� Piglin-themed items").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openPiglinItemsMenu(player))
                .build());

        gui.open();
    }

    // ============================================================
    // CATEGORY MENU METHODS - All custom items
    // ============================================================

    private static void openT1SwordsMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⚔ T1 Bounty Swords"));

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Text.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        // Add all T1 swords
        int slot = 10;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            if (slot > 43) break;
            gui.setSlot(slot++, new GuiElementBuilder(SlayerItems.createSlayerSword(type))
                    .setName(SlayerItems.createSlayerSword(type).get(DataComponentTypes.CUSTOM_NAME))
                    .addLoreLine(Text.literal("✨ Click to view").formatted(Formatting.YELLOW))
                    .setCallback((idx, type2, action) -> showItemInfo(player, SlayerItems.createSlayerSword(type)))
                    .build());
        }

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    private static void openT2SwordsMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⚔ T2 Bounty Swords"));

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Text.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        // Add all T2 swords
        int slot = 10;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            if (slot > 43) break;
            gui.setSlot(slot++, new GuiElementBuilder(SlayerItems.createUpgradedSlayerSword(type))
                    .setName(SlayerItems.createUpgradedSlayerSword(type).get(DataComponentTypes.CUSTOM_NAME))
                    .addLoreLine(Text.literal("✨ Click to view").formatted(Formatting.YELLOW))
                    .setCallback((idx, type2, action) -> showItemInfo(player, SlayerItems.createUpgradedSlayerSword(type)))
                    .build());
        }

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    private static void openT1ArmorMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("🛡 T1 Bounty Armor"));

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Text.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        // Add all T1 armor pieces
        int slot = 10;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            if (slot > 43) break;
            
            gui.setSlot(slot++, new GuiElementBuilder(SlayerItems.createSlayerHelmet(type, 1))
                    .setName(SlayerItems.createSlayerHelmet(type, 1).get(DataComponentTypes.CUSTOM_NAME))
                    .addLoreLine(Text.literal("✨ Click to view").formatted(Formatting.YELLOW))
                    .setCallback((idx, type2, action) -> showItemInfo(player, SlayerItems.createSlayerHelmet(type, 1)))
                    .build());
            
            if (slot <= 43) {
                gui.setSlot(slot++, new GuiElementBuilder(SlayerItems.createSlayerChestplate(type, 1))
                        .setName(SlayerItems.createSlayerChestplate(type, 1).get(DataComponentTypes.CUSTOM_NAME))
                        .addLoreLine(Text.literal("✨ Click to view").formatted(Formatting.YELLOW))
                        .setCallback((idx, type2, action) -> showItemInfo(player, SlayerItems.createSlayerChestplate(type, 1)))
                        .build());
            }
            
            if (slot <= 43) {
                gui.setSlot(slot++, new GuiElementBuilder(SlayerItems.createSlayerLeggings(type, 1))
                        .setName(SlayerItems.createSlayerLeggings(type, 1).get(DataComponentTypes.CUSTOM_NAME))
                        .addLoreLine(Text.literal("✨ Click to view").formatted(Formatting.YELLOW))
                        .setCallback((idx, type2, action) -> showItemInfo(player, SlayerItems.createSlayerLeggings(type, 1)))
                        .build());
            }
            
            if (slot <= 43) {
                gui.setSlot(slot++, new GuiElementBuilder(SlayerItems.createSlayerBoots(type, 1))
                        .setName(SlayerItems.createSlayerBoots(type, 1).get(DataComponentTypes.CUSTOM_NAME))
                        .addLoreLine(Text.literal("✨ Click to view").formatted(Formatting.YELLOW))
                        .setCallback((idx, type2, action) -> showItemInfo(player, SlayerItems.createSlayerBoots(type, 1)))
                        .build());
            }
        }

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    private static void openT2ArmorMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("🛡 T2 Bounty Armor"));

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Text.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        // Add all T2 armor pieces
        int slot = 10;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            if (slot > 43) break;
            
            gui.setSlot(slot++, new GuiElementBuilder(SlayerItems.createSlayerHelmet(type, 2))
                    .setName(SlayerItems.createSlayerHelmet(type, 2).get(DataComponentTypes.CUSTOM_NAME))
                    .addLoreLine(Text.literal("✨ Click to view").formatted(Formatting.YELLOW))
                    .setCallback((idx, type2, action) -> showItemInfo(player, SlayerItems.createSlayerHelmet(type, 2)))
                    .build());
            
            if (slot <= 43) {
                gui.setSlot(slot++, new GuiElementBuilder(SlayerItems.createSlayerChestplate(type, 2))
                        .setName(SlayerItems.createSlayerChestplate(type, 2).get(DataComponentTypes.CUSTOM_NAME))
                        .addLoreLine(Text.literal("✨ Click to view").formatted(Formatting.YELLOW))
                        .setCallback((idx, type2, action) -> showItemInfo(player, SlayerItems.createSlayerChestplate(type, 2)))
                        .build());
            }
            
            if (slot <= 43) {
                gui.setSlot(slot++, new GuiElementBuilder(SlayerItems.createSlayerLeggings(type, 2))
                        .setName(SlayerItems.createSlayerLeggings(type, 2).get(DataComponentTypes.CUSTOM_NAME))
                        .addLoreLine(Text.literal("✨ Click to view").formatted(Formatting.YELLOW))
                        .setCallback((idx, type2, action) -> showItemInfo(player, SlayerItems.createSlayerLeggings(type, 2)))
                        .build());
            }
            
            if (slot <= 43) {
                gui.setSlot(slot++, new GuiElementBuilder(SlayerItems.createSlayerBoots(type, 2))
                        .setName(SlayerItems.createSlayerBoots(type, 2).get(DataComponentTypes.CUSTOM_NAME))
                        .addLoreLine(Text.literal("✨ Click to view").formatted(Formatting.YELLOW))
                        .setCallback((idx, type2, action) -> showItemInfo(player, SlayerItems.createSlayerBoots(type, 2)))
                        .build());
            }
        }

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    private static void openSpecialItemsMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("🎁 Special Items"));

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Text.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        // Add special items
        ItemStack[] specialItems = {
            SlayerItems.createMidasSword(),
            SlayerItems.createCrownOfGreed(),
            SlayerItems.createCrownOfMidas(),
            SlayerItems.createVoidwalkerCrown(),
            CustomItemHandler.createHermesShoes(),
            CustomItemHandler.createTheGavel(),
            CustomItemHandler.createHarveysStick()
        };

        int slot = 10;
        for (ItemStack item : specialItems) {
            if (slot > 43) break;
            gui.setSlot(slot++, new GuiElementBuilder(item)
                    .setName(item.get(DataComponentTypes.CUSTOM_NAME))
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

    private static void openPiglinItemsMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("👑 Piglin Items"));

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Text.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        // Add piglin items
        ItemStack[] piglinItems = {
            SlayerItems.createPiglinCore(),
            SlayerItems.createPiglinFlesh(),
            SlayerItems.createCrownOfGreed(),
            SlayerItems.createCrownOfMidas(),
            SlayerItems.createMidasSword()
        };

        int slot = 10;
        for (ItemStack item : piglinItems) {
            if (slot > 43) break;
            gui.setSlot(slot++, new GuiElementBuilder(item)
                    .setName(item.get(DataComponentTypes.CUSTOM_NAME))
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
    // HELPER: Show item information with actual recipe
    // ============================================================
    private static void showItemInfo(ServerPlayerEntity player, ItemStack resultItem) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("Recipe View"));

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Text.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        // Header - Item name
        Text displayName = resultItem.get(DataComponentTypes.CUSTOM_NAME);
        if (displayName == null) {
            displayName = Text.literal(resultItem.getItem().toString());
        }
        gui.setSlot(4, new GuiElementBuilder(resultItem.getItem())
                .setName(displayName)
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7Crafting Recipe").formatted(Formatting.GRAY))
                .glow().build());

        // Recipe grid slots (3x3)
        int[] gridSlots = {10, 11, 12, 19, 20, 21, 28, 29, 30};
        
        // Try to find the recipe from SlayerRecipes
        SlayerRecipes.Recipe recipe = SlayerRecipes.getRecipeForOutput(resultItem);
        
        if (recipe != null && recipe.ingredients != null) {
            // Display the actual recipe
            for (int i = 0; i < 9 && i < recipe.ingredients.length; i++) {
                ItemStack input = recipe.ingredients[i];
                if (input != null && !input.isEmpty()) {
                    Text inputName = input.get(DataComponentTypes.CUSTOM_NAME);
                    if (inputName == null) {
                        inputName = Text.literal(input.getItem().toString());
                    }
                    gui.setSlot(gridSlots[i], new GuiElementBuilder(input.getItem())
                            .setName(inputName)
                            .setCount(input.getCount())
                            .build());
                } else {
                    // Empty slot indicator
                    gui.setSlot(gridSlots[i], new GuiElementBuilder(Items.LIGHT_GRAY_STAINED_GLASS_PANE)
                            .setName(Text.literal("§7Empty"))
                            .build());
                }
            }
            
            // Arrow indicator
            gui.setSlot(23, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("§e→").formatted(Formatting.YELLOW))
                    .build());
            
            // Result
            gui.setSlot(24, new GuiElementBuilder(resultItem)
                    .setName(displayName)
                    .glow().build());
        } else {
            // No recipe found - show message
            gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                    .setName(Text.literal("§cNo recipe found"))
                    .addLoreLine(Text.literal("§7This item may be obtained"))
                    .addLoreLine(Text.literal("§7through other means."))
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
    // LEGACY METHOD - For backward compatibility
    // ============================================================
    public static boolean openSpecificRecipe(ServerPlayerEntity player, String itemName) {
        // For now, just open the main menu
        player.sendMessage(Text.literal("Recipe lookup: " + itemName + " - Browse categories instead").formatted(Formatting.YELLOW), false);
        openMainMenu(player);
        return true;
    }
}
