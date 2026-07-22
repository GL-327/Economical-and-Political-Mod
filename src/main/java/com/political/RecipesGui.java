package com.political;
// Updated: 2026-03-18

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.List;

public class RecipesGui {

    // Track category stack for back navigation (allows multiple back presses)
    private static final java.util.Stack<CustomItemsGui.Category> categoryStack = new java.util.Stack<>();
    
    // ============================================================
    // MAIN MENU - Double chest organized layout
    // ============================================================
    public static void openMainMenu(ServerPlayer player) {
        openMainMenu(player, false);
    }
    
    public static void openMainMenu(ServerPlayer player, boolean fromRecipe) {
        // If coming from recipe view and we have categories in stack, go back to that category
        if (fromRecipe && !categoryStack.isEmpty()) {
            CustomItemsGui.Category cat = categoryStack.peek();
            CustomItemsGui.openCategoryPage(player, cat, 0,
                (p, item) -> CustomItemsGui.showItemInfo(p, item, RecipesGui::openMainMenuFromCategory),
                RecipesGui::openMainMenu);
            return;
        }
        categoryStack.clear(); // Clear when going to main menu directly
        doOpenMainMenu(player);
    }
    
    private static void openMainMenuFromCategory(ServerPlayer player) {
        openMainMenu(player, true);
    }
    
    // Push category to stack when navigating to a category page
    public static void pushCategory(CustomItemsGui.Category cat) {
        categoryStack.push(cat);
    }
    
    private static void doOpenMainMenu(ServerPlayer player) {
        try {
            SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
            gui.setTitle(Component.literal("Recipe Browser").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        RecipeGuiTheme.fillDoubleChestCatalog(gui);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(Component.literal("Recipe Browser").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Browse by category — same layout as /customitems").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Pick an item to see its crafting recipe").withStyle(ChatFormatting.DARK_GRAY))
                .glow().build());

        // Search button
        gui.setSlot(1, new GuiElementBuilder(Items.SPYGLASS)
                .setName(Component.literal("🔍 Search Recipes").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to search items by name").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Supports partial matches!").withStyle(ChatFormatting.GREEN))
                .setCallback((idx, type, containerInput, slotGui) -> openSearchGui(player))
                .build());

        // Row 1: Armor categories - spread across more slots
        gui.setSlot(10, new GuiElementBuilder(Items.EMERALD)
                .setName(Component.literal("💎 Custom Armor").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Emerald, Lapis armors").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("✨ Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> { pushCategory(CustomItemsGui.Category.CUSTOM_ARMOR); CustomItemsGui.openGemBlockGearPage(player, 0,
                        (p, item) -> CustomItemsGui.showItemInfo(p, item, RecipesGui::openMainMenuFromCategory),
                        RecipesGui::openMainMenu); })
                .build());

        gui.setSlot(20, new GuiElementBuilder(Items.DRAGON_HEAD)
                .setName(Component.literal("🐉 Dragon chestplates").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("All dragon plates & cores").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("✨ Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> { pushCategory(CustomItemsGui.Category.DRAGON_CHESTPLATES); CustomItemsGui.openCategoryPage(player, CustomItemsGui.Category.DRAGON_CHESTPLATES, 0,
                        (p, item) -> CustomItemsGui.showItemInfo(p, item, RecipesGui::openMainMenuFromCategory),
                        RecipesGui::openMainMenu); })
                .build());

        gui.setSlot(12, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("⚔ Bounty Sets").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("T1 & T2 Armor + Swords").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("✨ Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> { pushCategory(CustomItemsGui.Category.SPECIAL_ARMOR); CustomItemsGui.openBountySetsPage(player, 0,
                        (p, item) -> CustomItemsGui.showItemInfo(p, item, RecipesGui::openMainMenuFromCategory),
                        RecipesGui::openMainMenu); })
                .build());

        gui.setSlot(14, new GuiElementBuilder(Items.NETHERITE_HELMET)
                .setName(Component.literal("🛡 Special Armor").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Legendary armor").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("✨ Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> { pushCategory(CustomItemsGui.Category.SPECIAL_ARMOR); CustomItemsGui.openCategoryPage(player, CustomItemsGui.Category.SPECIAL_ARMOR, 0,
                        (p, item) -> CustomItemsGui.showItemInfo(p, item, RecipesGui::openMainMenuFromCategory),
                        RecipesGui::openMainMenu); })
                .build());

        gui.setSlot(16, new GuiElementBuilder(Items.GOLDEN_CHESTPLATE)
                .setName(Component.literal("👑 Gold Armor").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Gold armor tiers").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("✨ Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> { pushCategory(CustomItemsGui.Category.GOLD_ARMOR); CustomItemsGui.openCategoryPage(player, CustomItemsGui.Category.GOLD_ARMOR, 0,
                        (p, item) -> CustomItemsGui.showItemInfo(p, item, RecipesGui::openMainMenuFromCategory),
                        RecipesGui::openMainMenu); })
                .build());

        gui.setSlot(28, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Component.literal("👑 Piglin Items").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Piglin-themed items").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("✨ Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> { pushCategory(CustomItemsGui.Category.PIGLIN); CustomItemsGui.openCategoryPage(player, CustomItemsGui.Category.PIGLIN, 0,
                        (p, item) -> CustomItemsGui.showItemInfo(p, item, RecipesGui::openMainMenuFromCategory),
                        RecipesGui::openMainMenu); })
                .build());

        // Row 2: Weapons and Tools
        gui.setSlot(22, new GuiElementBuilder(Items.NETHERITE_PICKAXE)
                .setName(Component.literal("⛏ Tool Sets").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Block + Legendary tools").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("✨ Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> { pushCategory(CustomItemsGui.Category.CUSTOM_TOOLS); CustomItemsGui.openToolSetsPage(player, 0,
                        (p, item) -> CustomItemsGui.showItemInfo(p, item, RecipesGui::openMainMenuFromCategory),
                        RecipesGui::openMainMenu); })
                .build());

        gui.setSlot(24, new GuiElementBuilder(Items.STICK)
                .setName(Component.literal("🎁 Special Items").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Special unique items").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("✨ Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> { pushCategory(CustomItemsGui.Category.SPECIAL_ITEMS); CustomItemsGui.openCategoryPage(player, CustomItemsGui.Category.SPECIAL_ITEMS, 0,
                        (p, item) -> CustomItemsGui.showItemInfo(p, item, RecipesGui::openMainMenuFromCategory),
                        RecipesGui::openMainMenu); })
                .build());

        // Row 3: Materials and Tokens
        gui.setSlot(30, new GuiElementBuilder(Items.SLIME_BALL)
                .setName(Component.literal("📦 Materials").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Cores & chunks").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("✨ Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> { pushCategory(CustomItemsGui.Category.MATERIALS); CustomItemsGui.openCategoryPage(player, CustomItemsGui.Category.MATERIALS, 0,
                        (p, item) -> CustomItemsGui.showItemInfo(p, item, RecipesGui::openMainMenuFromCategory),
                        RecipesGui::openMainMenu); })
                .build());

        // Row 4: Additional categories
        gui.setSlot(38, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("✧ Attribute Tokens").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Armor attributes").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("✨ Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> { pushCategory(CustomItemsGui.Category.ATTRIBUTE_TOKENS); CustomItemsGui.openCategoryPage(player, CustomItemsGui.Category.ATTRIBUTE_TOKENS, 0,
                        (p, item) -> CustomItemsGui.showItemInfo(p, item, RecipesGui::openMainMenuFromCategory),
                        RecipesGui::openMainMenu); })
                .build());

        gui.setSlot(40, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("⚔ Weapon Tokens").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Weapon attributes").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("✨ Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> { pushCategory(CustomItemsGui.Category.WEAPON_ATTRIBUTES); CustomItemsGui.openCategoryPage(player, CustomItemsGui.Category.WEAPON_ATTRIBUTES, 0,
                        (p, item) -> CustomItemsGui.showItemInfo(p, item, RecipesGui::openMainMenuFromCategory),
                        RecipesGui::openMainMenu); })
                .build());

        gui.setSlot(42, new GuiElementBuilder(Items.BLAZE_ROD)
                .setName(Component.literal("⚡ HPEBM Weapons").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Energy beam weapons").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("✨ Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> { pushCategory(CustomItemsGui.Category.HPEBM); CustomItemsGui.openCategoryPage(player, CustomItemsGui.Category.HPEBM, 0,
                        (p, item) -> CustomItemsGui.showItemInfo(p, item, RecipesGui::openMainMenuFromCategory),
                        RecipesGui::openMainMenu); })
                .build());

        gui.setSlot(34, new GuiElementBuilder(Items.WITHER_SKELETON_SKULL)
                .setName(Component.literal("💀 Mob Drops").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Bounty, World Boss & Enhanced drops").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("💀 Click to browse recipes").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> {
                    pushCategory(CustomItemsGui.Category.MOB_DROPS);
                    CustomItemsGui.openMobDropsHub(player,
                            (p, item) -> CustomItemsGui.showItemInfo(p, item, RecipesGui::openMainMenuFromCategory),
                            RecipesGui::openMainMenu);
                })
                .build());

        gui.setSlot(36, new GuiElementBuilder(Items.TIPPED_ARROW)
                .setName(Component.literal("➵ Custom Arrows").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("All custom arrow types").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("✨ Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> { pushCategory(CustomItemsGui.Category.CUSTOM_ARROWS); CustomItemsGui.openCategoryPage(player, CustomItemsGui.Category.CUSTOM_ARROWS, 0,
                        (p, item) -> CustomItemsGui.showItemInfo(p, item, RecipesGui::openMainMenuFromCategory),
                        RecipesGui::openMainMenu); })
                .build());

        gui.setSlot(46, new GuiElementBuilder(Items.PRISMARINE_SHARD)
                .setName(Component.literal("⬆ Ascension Materials").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Shards & fragments for ascension").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("✨ Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> { pushCategory(CustomItemsGui.Category.ASCENSION_MATERIALS); CustomItemsGui.openCategoryPage(player, CustomItemsGui.Category.ASCENSION_MATERIALS, 0,
                        (p, item) -> CustomItemsGui.showItemInfo(p, item, RecipesGui::openMainMenuFromCategory),
                        RecipesGui::openMainMenu); })
                .build());

        gui.setSlot(48, new GuiElementBuilder(Items.DRAGON_BREATH)
                .setName(Component.literal("✧ Ascension Relics").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Rare relics for ascension").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("✨ Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> { pushCategory(CustomItemsGui.Category.ASCENSION_RELICS); CustomItemsGui.openCategoryPage(player, CustomItemsGui.Category.ASCENSION_RELICS, 0,
                        (p, item) -> CustomItemsGui.showItemInfo(p, item, RecipesGui::openMainMenuFromCategory),
                        RecipesGui::openMainMenu); })
                .build());

        gui.setSlot(44, new GuiElementBuilder(Items.FISHING_ROD)
                .setName(Component.literal("🎣 Fishing Rods").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Custom fishing rods").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("✨ Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> { pushCategory(CustomItemsGui.Category.FISHING_RODS); CustomItemsGui.openCategoryPage(player, CustomItemsGui.Category.FISHING_RODS, 0,
                        (p, item) -> CustomItemsGui.showItemInfo(p, item, RecipesGui::openMainMenuFromCategory),
                        RecipesGui::openMainMenu); })
                .build());

        gui.setSlot(53, new GuiElementBuilder(Items.BLAZE_ROD)
                .setName(Component.literal("✦ Magic Staves").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Magic staff weapons").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("✨ Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> { pushCategory(CustomItemsGui.Category.MAGIC_STAVES); CustomItemsGui.openCategoryPage(player, CustomItemsGui.Category.MAGIC_STAVES, 0,
                        (p, item) -> CustomItemsGui.showItemInfo(p, item, RecipesGui::openMainMenuFromCategory),
                        RecipesGui::openMainMenu); })
                .build());

        // Close button
        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Close").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .setCallback((slot, clicktype, containerInput, slotGui) -> gui.close())
                .build());

        gui.open();
        } catch (Exception e) {
            System.err.println("[RecipesGui] Error: " + e.getMessage());
            e.printStackTrace();
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Error: " + e.getMessage()).withStyle(ChatFormatting.RED), false);
        }
    }

    // Helper class for bounty set display
    public static class BountySetDisplay {
        final SlayerManager.SlayerType type;
        final net.minecraft.world.item.Item icon;
        final String name;
        final ChatFormatting color;
        final ItemStack t1Sword, t1Helmet, t1Chestplate, t1Leggings, t1Boots;
        final ItemStack t2Sword, t2Helmet, t2Chestplate, t2Leggings, t2Boots;
        
        BountySetDisplay(SlayerManager.SlayerType type, net.minecraft.world.item.Item icon, String name, ChatFormatting color,
                        ItemStack t1Sword, ItemStack t1Helmet, ItemStack t1Chestplate, ItemStack t1Leggings, ItemStack t1Boots,
                        ItemStack t2Sword, ItemStack t2Helmet, ItemStack t2Chestplate, ItemStack t2Leggings, ItemStack t2Boots) {
            this.type = type;
            this.icon = icon;
            this.name = name;
            this.color = color;
            this.t1Sword = t1Sword;
            this.t1Helmet = t1Helmet;
            this.t1Chestplate = t1Chestplate;
            this.t1Leggings = t1Leggings;
            this.t1Boots = t1Boots;
            this.t2Sword = t2Sword;
            this.t2Helmet = t2Helmet;
            this.t2Chestplate = t2Chestplate;
            this.t2Leggings = t2Leggings;
            this.t2Boots = t2Boots;
        }
    }
    
    // T1 Bounty Set Submenu
    private static void openT1BountySetMenu(ServerPlayer player, String setName) {
        SlayerManager.SlayerType slayerType = null;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            if (type.displayName.equals(setName)) {
                slayerType = type;
                break;
            }
        }
        if (slayerType == null) return;
        
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("🛡 T1 " + setName + " Set"));

        RecipeGuiTheme.fillChest3Hub(gui);
        
        // Header
        gui.setSlot(4, new GuiElementBuilder(slayerType.icon)
                .setName(Component.literal("T1 " + setName + " Set").withStyle(slayerType.color, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Tier 1 (Iron) Bounty Equipment").withStyle(ChatFormatting.GRAY))
                .glow().build());
        
        // T1 items in a row
        gui.setSlot(10, createArmorElement(SlayerItems.createSlayerHelmet(slayerType, 1), player));
        gui.setSlot(11, createArmorElement(SlayerItems.createSlayerChestplate(slayerType, 1), player));
        gui.setSlot(12, createArmorElement(SlayerItems.createSlayerLeggings(slayerType, 1), player));
        gui.setSlot(13, createArmorElement(SlayerItems.createSlayerBoots(slayerType, 1), player));
        
        ItemStack t1Sword = SlayerItems.createSlayerSword(slayerType);
        gui.setSlot(14, new GuiElementBuilder(t1Sword)
                .setName(t1Sword.get(DataComponents.CUSTOM_NAME))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("✨ T1 Sword").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> showItemInfo(player, t1Sword))
                .build());
        
        // Back button
        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back to Bounty Sets").withStyle(ChatFormatting.YELLOW))
                .setCallback((slot, type, containerInput, slotGui) -> openMainMenu(player))
                .build());
        
        gui.open();
    }
    
    // T2 Bounty Set Submenu
    private static void openT2BountySetMenu(ServerPlayer player, String setName) {
        SlayerManager.SlayerType slayerType = null;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            if (type.displayName.equals(setName)) {
                slayerType = type;
                break;
            }
        }
        if (slayerType == null) return;
        
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("🛡 T2 " + setName + " Set"));

        RecipeGuiTheme.fillChest3Hub(gui);
        
        // Header
        gui.setSlot(4, new GuiElementBuilder(slayerType.icon)
                .setName(Component.literal("T2 " + setName + " Set").withStyle(slayerType.color, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Tier 2 (Diamond) Bounty Equipment").withStyle(ChatFormatting.GRAY))
                .glow().build());
        
        // T2 items in a row
        gui.setSlot(10, createArmorElement(SlayerItems.createSlayerHelmet(slayerType, 2), player));
        gui.setSlot(11, createArmorElement(SlayerItems.createSlayerChestplate(slayerType, 2), player));
        gui.setSlot(12, createArmorElement(SlayerItems.createSlayerLeggings(slayerType, 2), player));
        gui.setSlot(13, createArmorElement(SlayerItems.createSlayerBoots(slayerType, 2), player));
        
        ItemStack t2Sword = SlayerItems.createUpgradedSlayerSword(slayerType);
        gui.setSlot(14, new GuiElementBuilder(t2Sword)
                .setName(t2Sword.get(DataComponents.CUSTOM_NAME))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("✨ T2 Sword").withStyle(ChatFormatting.AQUA))
                .setCallback((idx, type, containerInput, slotGui) -> showItemInfo(player, t2Sword))
                .build());
        
        // Back button
        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back to Bounty Sets").withStyle(ChatFormatting.YELLOW))
                .setCallback((slot, type, containerInput, slotGui) -> openMainMenu(player))
                .build());
        
        gui.open();
    }

    // ============================================================
    // GEM & BLOCK ARMOR MENU - Organized sets with matching weapons
    // ============================================================
    private static void openGemAndBlockArmorMenu(ServerPlayer player, int page) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("💎 Custom Armor Sets"));

        // Background with dark theme
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Component.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.EMERALD)
                .setName(Component.literal("💎 Custom Armor Sets").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Block → Armor Set → Weapon").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Complete sets with matching weapons!").withStyle(ChatFormatting.YELLOW))
                .glow().build());

        // Define armor sets: Block item, Armor pieces, Weapon
        java.util.List<ArmorSetDisplay> sets = new java.util.ArrayList<>();
        
        // Gem Armors
        sets.add(new ArmorSetDisplay(Items.EMERALD_BLOCK, "Emerald", 
            GemGear.createEmeraldSword(), GemGear.createEmeraldHelmet(), GemGear.createEmeraldChestplate(), 
            GemGear.createEmeraldLeggings(), GemGear.createEmeraldBoots()));
        sets.add(new ArmorSetDisplay(Items.LAPIS_BLOCK, "Lapis",
            GemGear.createLapisSword(), GemGear.createLapisHelmet(), GemGear.createLapisChestplate(),
            GemGear.createLapisLeggings(), GemGear.createLapisBoots()));
        
        // Block Armors with matching weapons
        sets.add(new ArmorSetDisplay(Items.GLASS, "Glass",
            BlockWeapons.createGlassSword(), BlockArmor.createGlassHelmet(), BlockArmor.createGlassChestplate(),
            BlockArmor.createGlassLeggings(), BlockArmor.createGlassBoots()));
        sets.add(new ArmorSetDisplay(Items.OBSIDIAN, "Obsidian",
            BlockWeapons.createObsidianSword(), BlockArmor.createObsidianHelmet(), BlockArmor.createObsidianChestplate(),
            BlockArmor.createObsidianLeggings(), BlockArmor.createObsidianBoots()));
        sets.add(new ArmorSetDisplay(Items.QUARTZ_BLOCK, "Quartz",
            BlockWeapons.createQuartzSword(), BlockArmor.createQuartzHelmet(), BlockArmor.createQuartzChestplate(),
            BlockArmor.createQuartzLeggings(), BlockArmor.createQuartzBoots()));
        sets.add(new ArmorSetDisplay(Items.GLOWSTONE, "Glowstone",
            BlockWeapons.createGlowstoneSword(), BlockArmor.createGlowstoneHelmet(), BlockArmor.createGlowstoneChestplate(),
            BlockArmor.createGlowstoneLeggings(), BlockArmor.createGlowstoneBoots()));
        sets.add(new ArmorSetDisplay(Items.REDSTONE_BLOCK, "Redstone",
            BlockWeapons.createRedstoneSword(), BlockArmor.createRedstoneHelmet(), BlockArmor.createRedstoneChestplate(),
            BlockArmor.createRedstoneLeggings(), BlockArmor.createRedstoneBoots()));
        sets.add(new ArmorSetDisplay(Items.NETHERRACK, "Netherrack",
            BlockWeapons.createNetherrackSword(), BlockArmor.createNetherrackHelmet(), BlockArmor.createNetherrackChestplate(),
            BlockArmor.createNetherrackLeggings(), BlockArmor.createNetherrackBoots()));
        sets.add(new ArmorSetDisplay(Items.END_STONE, "End Stone",
            BlockWeapons.createEndstoneSword(), BlockArmor.createEndstoneHelmet(), BlockArmor.createEndstoneChestplate(),
            BlockArmor.createEndstoneLeggings(), BlockArmor.createEndstoneBoots()));
        sets.add(new ArmorSetDisplay(Items.PACKED_ICE, "Ice",
            BlockWeapons.createIceSword(), BlockArmor.createIceHelmet(), BlockArmor.createIceChestplate(),
            BlockArmor.createIceLeggings(), BlockArmor.createIceBoots()));
        sets.add(new ArmorSetDisplay(Items.PRISMARINE, "Prismarine",
            BlockWeapons.createPrismarineSword(), BlockArmor.createPrismarineHelmet(), BlockArmor.createPrismarineChestplate(),
            BlockArmor.createPrismarineLeggings(), BlockArmor.createPrismarineBoots()));
        sets.add(new ArmorSetDisplay(Items.TERRACOTTA, "Terracotta",
            BlockWeapons.createTerracottaSword(), BlockArmor.createTerracottaHelmet(), BlockArmor.createTerracottaChestplate(),
            BlockArmor.createTerracottaLeggings(), BlockArmor.createTerracottaBoots()));
        sets.add(new ArmorSetDisplay(Items.MOSSY_COBBLESTONE, "Mossy",
            BlockWeapons.createMossySword(), BlockArmor.createMossyHelmet(), BlockArmor.createMossyChestplate(),
            BlockArmor.createMossyLeggings(), BlockArmor.createMossyBoots()));
        sets.add(new ArmorSetDisplay(Items.SOUL_SAND, "Soul Sand",
            BlockWeapons.createSoulSandSword(), BlockArmor.createSoulSandHelmet(), BlockArmor.createSoulSandChestplate(),
            BlockArmor.createSoulSandLeggings(), BlockArmor.createSoulSandBoots()));
        sets.add(new ArmorSetDisplay(Items.MAGMA_BLOCK, "Magma",
            BlockWeapons.createMagmaSword(), BlockArmor.createMagmaHelmet(), BlockArmor.createMagmaChestplate(),
            BlockArmor.createMagmaLeggings(), BlockArmor.createMagmaBoots()));
        sets.add(new ArmorSetDisplay(Items.SANDSTONE, "Sandstone",
            BlockWeapons.createSandstoneSword(), BlockArmor.createSandstoneHelmet(), BlockArmor.createSandstoneChestplate(),
            BlockArmor.createSandstoneLeggings(), BlockArmor.createSandstoneBoots()));
        sets.add(new ArmorSetDisplay(Items.AMETHYST_BLOCK, "Amethyst",
            BlockWeapons.createAmethystSword(), BlockArmor.createAmethystHelmet(), BlockArmor.createAmethystChestplate(),
            BlockArmor.createAmethystLeggings(), BlockArmor.createAmethystBoots()));
        sets.add(new ArmorSetDisplay(Items.COAL_BLOCK, "Coal",
            BlockWeapons.createCoalSword(), BlockArmor.createCoalHelmet(), BlockArmor.createCoalChestplate(),
            BlockArmor.createCoalLeggings(), BlockArmor.createCoalBoots()));

        // 36+ NEW BLOCK ARMOR SETS
        sets.add(new ArmorSetDisplay(Items.DIAMOND_BLOCK, "Diamond Block",
            BlockWeapons.createDiamondBlockSword(), BlockArmor.createDiamondBlockHelmet(), BlockArmor.createDiamondBlockChestplate(),
            BlockArmor.createDiamondBlockLeggings(), BlockArmor.createDiamondBlockBoots()));
        sets.add(new ArmorSetDisplay(Items.EMERALD_BLOCK, "Emerald Block",
            BlockWeapons.createEmeraldBlockSword(), BlockArmor.createEmeraldBlockHelmet(), BlockArmor.createEmeraldBlockChestplate(),
            BlockArmor.createEmeraldBlockLeggings(), BlockArmor.createEmeraldBlockBoots()));
        sets.add(new ArmorSetDisplay(Items.GOLD_BLOCK, "Gold Block",
            BlockWeapons.createGoldBlockSword(), BlockArmor.createGoldBlockHelmet(), BlockArmor.createGoldBlockChestplate(),
            BlockArmor.createGoldBlockLeggings(), BlockArmor.createGoldBlockBoots()));
        sets.add(new ArmorSetDisplay(Items.IRON_BLOCK, "Iron Block",
            BlockWeapons.createIronBlockSword(), BlockArmor.createIronBlockHelmet(), BlockArmor.createIronBlockChestplate(),
            BlockArmor.createIronBlockLeggings(), BlockArmor.createIronBlockBoots()));
        sets.add(new ArmorSetDisplay(Items.LAPIS_BLOCK, "Lapis Block",
            BlockWeapons.createLapisBlockSword(), BlockArmor.createLapisBlockHelmet(), BlockArmor.createLapisBlockChestplate(),
            BlockArmor.createLapisBlockLeggings(), BlockArmor.createLapisBlockBoots()));
        sets.add(new ArmorSetDisplay(Items.COPPER_BLOCK, "Copper Block",
            BlockWeapons.createCopperBlockSword(), BlockArmor.createCopperBlockHelmet(), BlockArmor.createCopperBlockChestplate(),
            BlockArmor.createCopperBlockLeggings(), BlockArmor.createCopperBlockBoots()));
        sets.add(new ArmorSetDisplay(Items.ANCIENT_DEBRIS, "Ancient Debris",
            BlockWeapons.createAncientDebrisSword(), BlockArmor.createAncientDebrisHelmet(), BlockArmor.createAncientDebrisChestplate(),
            BlockArmor.createAncientDebrisLeggings(), BlockArmor.createAncientDebrisBoots()));
        sets.add(new ArmorSetDisplay(Items.BASALT, "Basalt",
            BlockWeapons.createBasaltSword(), BlockArmor.createBasaltHelmet(), BlockArmor.createBasaltChestplate(),
            BlockArmor.createBasaltLeggings(), BlockArmor.createBasaltBoots()));
        sets.add(new ArmorSetDisplay(Items.BLACKSTONE, "Blackstone",
            BlockWeapons.createBlackstoneSword(), BlockArmor.createBlackstoneHelmet(), BlockArmor.createBlackstoneChestplate(),
            BlockArmor.createBlackstoneLeggings(), BlockArmor.createBlackstoneBoots()));
        sets.add(new ArmorSetDisplay(Items.BONE_BLOCK, "Bone Block",
            BlockWeapons.createBoneBlockSword(), BlockArmor.createBoneBlockHelmet(), BlockArmor.createBoneBlockChestplate(),
            BlockArmor.createBoneBlockLeggings(), BlockArmor.createBoneBlockBoots()));
        sets.add(new ArmorSetDisplay(Items.BRICKS, "Brick",
            BlockWeapons.createBrickSword(), BlockArmor.createBrickHelmet(), BlockArmor.createBrickChestplate(),
            BlockArmor.createBrickLeggings(), BlockArmor.createBrickBoots()));
        sets.add(new ArmorSetDisplay(Items.CACTUS, "Cactus",
            BlockWeapons.createCactusSword(), BlockArmor.createCactusHelmet(), BlockArmor.createCactusChestplate(),
            BlockArmor.createCactusLeggings(), BlockArmor.createCactusBoots()));
        sets.add(new ArmorSetDisplay(Items.CALCITE, "Calcite",
            BlockWeapons.createCalciteSword(), BlockArmor.createCalciteHelmet(), BlockArmor.createCalciteChestplate(),
            BlockArmor.createCalciteLeggings(), BlockArmor.createCalciteBoots()));
        sets.add(new ArmorSetDisplay(Items.DEEPSLATE, "Deepslate",
            BlockWeapons.createDeepslateSword(), BlockArmor.createDeepslateHelmet(), BlockArmor.createDeepslateChestplate(),
            BlockArmor.createDeepslateLeggings(), BlockArmor.createDeepslateBoots()));
        sets.add(new ArmorSetDisplay(Items.DRIPSTONE_BLOCK, "Dripstone",
            BlockWeapons.createDripstoneSword(), BlockArmor.createDripstoneHelmet(), BlockArmor.createDripstoneChestplate(),
            BlockArmor.createDripstoneLeggings(), BlockArmor.createDripstoneBoots()));
        sets.add(new ArmorSetDisplay(Items.HAY_BLOCK, "Hay",
            BlockWeapons.createHaySword(), BlockArmor.createHayHelmet(), BlockArmor.createHayChestplate(),
            BlockArmor.createHayLeggings(), BlockArmor.createHayBoots()));
        sets.add(new ArmorSetDisplay(Items.HONEYCOMB_BLOCK, "Honeycomb",
            BlockWeapons.createHoneycombSword(), BlockArmor.createHoneycombHelmet(), BlockArmor.createHoneycombChestplate(),
            BlockArmor.createHoneycombLeggings(), BlockArmor.createHoneycombBoots()));
        sets.add(new ArmorSetDisplay(Items.LILY_PAD, "Lily Pad",
            BlockWeapons.createLilyPadSword(), BlockArmor.createLilyPadHelmet(), BlockArmor.createLilyPadChestplate(),
            BlockArmor.createLilyPadLeggings(), BlockArmor.createLilyPadBoots()));
        sets.add(new ArmorSetDisplay(Items.MELON, "Melon",
            BlockWeapons.createMelonSword(), BlockArmor.createMelonHelmet(), BlockArmor.createMelonChestplate(),
            BlockArmor.createMelonLeggings(), BlockArmor.createMelonBoots()));
        sets.add(new ArmorSetDisplay(Items.MOSS_BLOCK, "Moss Block",
            BlockWeapons.createMossBlockSword(), BlockArmor.createMossBlockHelmet(), BlockArmor.createMossBlockChestplate(),
            BlockArmor.createMossBlockLeggings(), BlockArmor.createMossBlockBoots()));
        sets.add(new ArmorSetDisplay(Items.MYCELIUM, "Mycelium",
            BlockWeapons.createMyceliumSword(), BlockArmor.createMyceliumHelmet(), BlockArmor.createMyceliumChestplate(),
            BlockArmor.createMyceliumLeggings(), BlockArmor.createMyceliumBoots()));
        sets.add(new ArmorSetDisplay(Items.NETHER_BRICKS, "Nether Brick",
            BlockWeapons.createNetherBrickSword(), BlockArmor.createNetherBrickHelmet(), BlockArmor.createNetherBrickChestplate(),
            BlockArmor.createNetherBrickLeggings(), BlockArmor.createNetherBrickBoots()));
        sets.add(new ArmorSetDisplay(Items.PUMPKIN, "Pumpkin",
            BlockWeapons.createPumpkinSword(), BlockArmor.createPumpkinHelmet(), BlockArmor.createPumpkinChestplate(),
            BlockArmor.createPumpkinLeggings(), BlockArmor.createPumpkinBoots()));
        sets.add(new ArmorSetDisplay(Items.PURPUR_BLOCK, "Purpur",
            BlockWeapons.createPurpurSword(), BlockArmor.createPurpurHelmet(), BlockArmor.createPurpurChestplate(),
            BlockArmor.createPurpurLeggings(), BlockArmor.createPurpurBoots()));
        sets.add(new ArmorSetDisplay(Items.SAND, "Sand",
            BlockWeapons.createSandSword(), BlockArmor.createSandHelmet(), BlockArmor.createSandChestplate(),
            BlockArmor.createSandLeggings(), BlockArmor.createSandBoots()));
        sets.add(new ArmorSetDisplay(Items.SCULK, "Sculk",
            BlockWeapons.createSculkSword(), BlockArmor.createSculkHelmet(), BlockArmor.createSculkChestplate(),
            BlockArmor.createSculkLeggings(), BlockArmor.createSculkBoots()));
        sets.add(new ArmorSetDisplay(Items.SHROOMLIGHT, "Shroomlight",
            BlockWeapons.createShroomlightSword(), BlockArmor.createShroomlightHelmet(), BlockArmor.createShroomlightChestplate(),
            BlockArmor.createShroomlightLeggings(), BlockArmor.createShroomlightBoots()));
        sets.add(new ArmorSetDisplay(Items.SLIME_BLOCK, "Slime",
            BlockWeapons.createSlimeSword(), BlockArmor.createSlimeHelmet(), BlockArmor.createSlimeChestplate(),
            BlockArmor.createSlimeLeggings(), BlockArmor.createSlimeBoots()));
        sets.add(new ArmorSetDisplay(Items.SMOOTH_STONE, "Smooth Stone",
            BlockWeapons.createSmoothStoneSword(), BlockArmor.createSmoothStoneHelmet(), BlockArmor.createSmoothStoneChestplate(),
            BlockArmor.createSmoothStoneLeggings(), BlockArmor.createSmoothStoneBoots()));
        sets.add(new ArmorSetDisplay(Items.SNOW_BLOCK, "Snow",
            BlockWeapons.createSnowSword(), BlockArmor.createSnowHelmet(), BlockArmor.createSnowChestplate(),
            BlockArmor.createSnowLeggings(), BlockArmor.createSnowBoots()));
        sets.add(new ArmorSetDisplay(Items.SOUL_SOIL, "Soul Soil",
            BlockWeapons.createSoulSoilSword(), BlockArmor.createSoulSoilHelmet(), BlockArmor.createSoulSoilChestplate(),
            BlockArmor.createSoulSoilLeggings(), BlockArmor.createSoulSoilBoots()));
        sets.add(new ArmorSetDisplay(Items.SPONGE, "Sponge",
            BlockWeapons.createSpongeSword(), BlockArmor.createSpongeHelmet(), BlockArmor.createSpongeChestplate(),
            BlockArmor.createSpongeLeggings(), BlockArmor.createSpongeBoots()));
        sets.add(new ArmorSetDisplay(Items.TARGET, "Target",
            BlockWeapons.createTargetSword(), BlockArmor.createTargetHelmet(), BlockArmor.createTargetChestplate(),
            BlockArmor.createTargetLeggings(), BlockArmor.createTargetBoots()));
        sets.add(new ArmorSetDisplay(Items.TNT, "TNT",
            BlockWeapons.createTntSword(), BlockArmor.createTntHelmet(), BlockArmor.createTntChestplate(),
            BlockArmor.createTntLeggings(), BlockArmor.createTntBoots()));
        sets.add(new ArmorSetDisplay(Items.WARPED_STEM, "Warped",
            BlockWeapons.createWarpedSword(), BlockArmor.createWarpedHelmet(), BlockArmor.createWarpedChestplate(),
            BlockArmor.createWarpedLeggings(), BlockArmor.createWarpedBoots()));
        sets.add(new ArmorSetDisplay(Items.WET_SPONGE, "Wet Sponge",
            BlockWeapons.createWetSpongeSword(), BlockArmor.createWetSpongeHelmet(), BlockArmor.createWetSpongeChestplate(),
            BlockArmor.createWetSpongeLeggings(), BlockArmor.createWetSpongeBoots()));

        // ===== NEW ARMOR SETS =====
        // Stripped Log Armor
        sets.add(new ArmorSetDisplay(Items.STRIPPED_SPRUCE_LOG, "Stripped Spruce Log",
            BlockWeapons.createStrippedSpruceLogSword(), BlockArmor.createStrippedSpruceLogHelmet(), BlockArmor.createStrippedSpruceLogChestplate(),
            BlockArmor.createStrippedSpruceLogLeggings(), BlockArmor.createStrippedSpruceLogBoots()));
        sets.add(new ArmorSetDisplay(Items.STRIPPED_BIRCH_LOG, "Stripped Birch Log",
            BlockWeapons.createStrippedBirchLogSword(), BlockArmor.createStrippedBirchLogHelmet(), BlockArmor.createStrippedBirchLogChestplate(),
            BlockArmor.createStrippedBirchLogLeggings(), BlockArmor.createStrippedBirchLogBoots()));
        sets.add(new ArmorSetDisplay(Items.STRIPPED_DARK_OAK_LOG, "Stripped Dark Oak Log",
            BlockWeapons.createStrippedDarkOakLogSword(), BlockArmor.createStrippedDarkOakLogHelmet(), BlockArmor.createStrippedDarkOakLogChestplate(),
            BlockArmor.createStrippedDarkOakLogLeggings(), BlockArmor.createStrippedDarkOakLogBoots()));
        sets.add(new ArmorSetDisplay(Items.STRIPPED_JUNGLE_LOG, "Stripped Jungle Log",
            BlockWeapons.createStrippedJungleLogSword(), BlockArmor.createStrippedJungleLogHelmet(), BlockArmor.createStrippedJungleLogChestplate(),
            BlockArmor.createStrippedJungleLogLeggings(), BlockArmor.createStrippedJungleLogBoots()));
        sets.add(new ArmorSetDisplay(Items.STRIPPED_ACACIA_LOG, "Stripped Acacia Log",
            BlockWeapons.createStrippedAcaciaLogSword(), BlockArmor.createStrippedAcaciaLogHelmet(), BlockArmor.createStrippedAcaciaLogChestplate(),
            BlockArmor.createStrippedAcaciaLogLeggings(), BlockArmor.createStrippedAcaciaLogBoots()));
        sets.add(new ArmorSetDisplay(Items.STRIPPED_MANGROVE_LOG, "Stripped Mangrove Log",
            BlockWeapons.createStrippedMangroveLogSword(), BlockArmor.createStrippedMangroveLogHelmet(), BlockArmor.createStrippedMangroveLogChestplate(),
            BlockArmor.createStrippedMangroveLogLeggings(), BlockArmor.createStrippedMangroveLogBoots()));
        sets.add(new ArmorSetDisplay(Items.STRIPPED_CHERRY_LOG, "Stripped Cherry Log",
            BlockWeapons.createStrippedCherryLogSword(), BlockArmor.createStrippedCherryLogHelmet(), BlockArmor.createStrippedCherryLogChestplate(),
            BlockArmor.createStrippedCherryLogLeggings(), BlockArmor.createStrippedCherryLogBoots()));
        sets.add(new ArmorSetDisplay(Items.BAMBOO_BLOCK, "Bamboo Block",
            BlockWeapons.createStrippedBambooBlockSword(), BlockArmor.createStrippedBambooBlockHelmet(), BlockArmor.createStrippedBambooBlockChestplate(),
            BlockArmor.createStrippedBambooBlockLeggings(), BlockArmor.createStrippedBambooBlockBoots()));
        sets.add(new ArmorSetDisplay(Items.STRIPPED_CRIMSON_STEM, "Stripped Crimson Stem",
            BlockWeapons.createStrippedCrimsonStemSword(), BlockArmor.createStrippedCrimsonStemHelmet(), BlockArmor.createStrippedCrimsonStemChestplate(),
            BlockArmor.createStrippedCrimsonStemLeggings(), BlockArmor.createStrippedCrimsonStemBoots()));
        sets.add(new ArmorSetDisplay(Items.STRIPPED_WARPED_STEM, "Stripped Warped Stem",
            BlockWeapons.createStrippedWarpedStemSword(), BlockArmor.createStrippedWarpedStemHelmet(), BlockArmor.createStrippedWarpedStemChestplate(),
            BlockArmor.createStrippedWarpedStemLeggings(), BlockArmor.createStrippedWarpedStemBoots()));
        sets.add(new ArmorSetDisplay(Items.CRIMSON_STEM, "Crimson Stem",
            BlockWeapons.createCrimsonStemSword(), BlockArmor.createCrimsonStemHelmet(), BlockArmor.createCrimsonStemChestplate(),
            BlockArmor.createCrimsonStemLeggings(), BlockArmor.createCrimsonStemBoots()));
        sets.add(new ArmorSetDisplay(Items.CRYING_OBSIDIAN, "Crying Obsidian",
            BlockWeapons.createCryingObsidianSword(), BlockArmor.createCryingObsidianHelmet(), BlockArmor.createCryingObsidianChestplate(),
            BlockArmor.createCryingObsidianLeggings(), BlockArmor.createCryingObsidianBoots()));
        sets.add(new ArmorSetDisplay(Items.GILDED_BLACKSTONE, "Gilded Blackstone",
            BlockWeapons.createGildedBlackstoneSword(), BlockArmor.createGildedBlackstoneHelmet(), BlockArmor.createGildedBlackstoneChestplate(),
            BlockArmor.createGildedBlackstoneLeggings(), BlockArmor.createGildedBlackstoneBoots()));
        sets.add(new ArmorSetDisplay(Items.GRANITE, "Granite",
            BlockWeapons.createGraniteSword(), BlockArmor.createGraniteHelmet(), BlockArmor.createGraniteChestplate(),
            BlockArmor.createGraniteLeggings(), BlockArmor.createGraniteBoots()));
        sets.add(new ArmorSetDisplay(Items.DIORITE, "Diorite",
            BlockWeapons.createDioriteSword(), BlockArmor.createDioriteHelmet(), BlockArmor.createDioriteChestplate(),
            BlockArmor.createDioriteLeggings(), BlockArmor.createDioriteBoots()));
        sets.add(new ArmorSetDisplay(Items.ANDESITE, "Andesite",
            BlockWeapons.createAndesiteSword(), BlockArmor.createAndesiteHelmet(), BlockArmor.createAndesiteChestplate(),
            BlockArmor.createAndesiteLeggings(), BlockArmor.createAndesiteBoots()));
        sets.add(new ArmorSetDisplay(Items.POLISHED_GRANITE, "Polished Granite",
            BlockWeapons.createPolishedGraniteSword(), BlockArmor.createPolishedGraniteHelmet(), BlockArmor.createPolishedGraniteChestplate(),
            BlockArmor.createPolishedGraniteLeggings(), BlockArmor.createPolishedGraniteBoots()));
        sets.add(new ArmorSetDisplay(Items.POLISHED_DIORITE, "Polished Diorite",
            BlockWeapons.createPolishedDioriteSword(), BlockArmor.createPolishedDioriteHelmet(), BlockArmor.createPolishedDioriteChestplate(),
            BlockArmor.createPolishedDioriteLeggings(), BlockArmor.createPolishedDioriteBoots()));
        sets.add(new ArmorSetDisplay(Items.POLISHED_ANDESITE, "Polished Andesite",
            BlockWeapons.createPolishedAndesiteSword(), BlockArmor.createPolishedAndesiteHelmet(), BlockArmor.createPolishedAndesiteChestplate(),
            BlockArmor.createPolishedAndesiteLeggings(), BlockArmor.createPolishedAndesiteBoots()));
        sets.add(new ArmorSetDisplay(Items.PACKED_ICE, "Packed Ice",
            BlockWeapons.createPackedIceSword(), BlockArmor.createPackedIceHelmet(), BlockArmor.createPackedIceChestplate(),
            BlockArmor.createPackedIceLeggings(), BlockArmor.createPackedIceBoots()));
        sets.add(new ArmorSetDisplay(Items.BLUE_ICE, "Blue Ice",
            BlockWeapons.createBlueIceSword(), BlockArmor.createBlueIceHelmet(), BlockArmor.createBlueIceChestplate(),
            BlockArmor.createBlueIceLeggings(), BlockArmor.createBlueIceBoots()));
        sets.add(new ArmorSetDisplay(Items.NETHER_GOLD_ORE, "Nether Gold Ore",
            BlockWeapons.createNetherGoldOreSword(), BlockArmor.createNetherGoldOreHelmet(), BlockArmor.createNetherGoldOreChestplate(),
            BlockArmor.createNetherGoldOreLeggings(), BlockArmor.createNetherGoldOreBoots()));
        sets.add(new ArmorSetDisplay(Items.DARK_OAK_LOG, "Dark Oak Log",
            BlockWeapons.createDarkOakLogSword(), BlockArmor.createDarkOakLogHelmet(), BlockArmor.createDarkOakLogChestplate(),
            BlockArmor.createDarkOakLogLeggings(), BlockArmor.createDarkOakLogBoots()));
        sets.add(new ArmorSetDisplay(Items.JUNGLE_LOG, "Jungle Log",
            BlockWeapons.createJungleLogSword(), BlockArmor.createJungleLogHelmet(), BlockArmor.createJungleLogChestplate(),
            BlockArmor.createJungleLogLeggings(), BlockArmor.createJungleLogBoots()));
        sets.add(new ArmorSetDisplay(Items.ACACIA_LOG, "Acacia Log",
            BlockWeapons.createAcaciaLogSword(), BlockArmor.createAcaciaLogHelmet(), BlockArmor.createAcaciaLogChestplate(),
            BlockArmor.createAcaciaLogLeggings(), BlockArmor.createAcaciaLogBoots()));
        sets.add(new ArmorSetDisplay(Items.MANGROVE_LOG, "Mangrove Log",
            BlockWeapons.createMangroveLogSword(), BlockArmor.createMangroveLogHelmet(), BlockArmor.createMangroveLogChestplate(),
            BlockArmor.createMangroveLogLeggings(), BlockArmor.createMangroveLogBoots()));
        sets.add(new ArmorSetDisplay(Items.CHERRY_LOG, "Cherry Log",
            BlockWeapons.createCherryLogSword(), BlockArmor.createCherryLogHelmet(), BlockArmor.createCherryLogChestplate(),
            BlockArmor.createCherryLogLeggings(), BlockArmor.createCherryLogBoots()));
        sets.add(new ArmorSetDisplay(Items.BAMBOO_BLOCK, "Bamboo Block",
            BlockWeapons.createBambooBlockSword(), BlockArmor.createBambooBlockHelmet(), BlockArmor.createBambooBlockChestplate(),
            BlockArmor.createBambooBlockLeggings(), BlockArmor.createBambooBlockBoots()));
        sets.add(new ArmorSetDisplay(Items.TUFF, "Tuff",
            BlockWeapons.createTuffSword(), BlockArmor.createTuffHelmet(), BlockArmor.createTuffChestplate(),
            BlockArmor.createTuffLeggings(), BlockArmor.createTuffBoots()));
        sets.add(new ArmorSetDisplay(Items.POLISHED_TUFF, "Polished Tuff",
            BlockWeapons.createPolishedTuffSword(), BlockArmor.createPolishedTuffHelmet(), BlockArmor.createPolishedTuffChestplate(),
            BlockArmor.createPolishedTuffLeggings(), BlockArmor.createPolishedTuffBoots()));
        sets.add(new ArmorSetDisplay(Items.NETHER_WART_BLOCK, "Nether Wart Block",
            BlockWeapons.createNetherWartBlockSword(), BlockArmor.createNetherWartBlockHelmet(), BlockArmor.createNetherWartBlockChestplate(),
            BlockArmor.createNetherWartBlockLeggings(), BlockArmor.createNetherWartBlockBoots()));
        // Plank Armor
        sets.add(new ArmorSetDisplay(Items.SPRUCE_PLANKS, "Spruce Planks",
            BlockWeapons.createSprucePlanksSword(), BlockArmor.createSprucePlanksHelmet(), BlockArmor.createSprucePlanksChestplate(),
            BlockArmor.createSprucePlanksLeggings(), BlockArmor.createSprucePlanksBoots()));
        sets.add(new ArmorSetDisplay(Items.BIRCH_PLANKS, "Birch Planks",
            BlockWeapons.createBirchPlanksSword(), BlockArmor.createBirchPlanksHelmet(), BlockArmor.createBirchPlanksChestplate(),
            BlockArmor.createBirchPlanksLeggings(), BlockArmor.createBirchPlanksBoots()));
        sets.add(new ArmorSetDisplay(Items.JUNGLE_PLANKS, "Jungle Planks",
            BlockWeapons.createJunglePlanksSword(), BlockArmor.createJunglePlanksHelmet(), BlockArmor.createJunglePlanksChestplate(),
            BlockArmor.createJunglePlanksLeggings(), BlockArmor.createJunglePlanksBoots()));
        sets.add(new ArmorSetDisplay(Items.ACACIA_PLANKS, "Acacia Planks",
            BlockWeapons.createAcaciaPlanksSword(), BlockArmor.createAcaciaPlanksHelmet(), BlockArmor.createAcaciaPlanksChestplate(),
            BlockArmor.createAcaciaPlanksLeggings(), BlockArmor.createAcaciaPlanksBoots()));
        sets.add(new ArmorSetDisplay(Items.DARK_OAK_PLANKS, "Dark Oak Planks",
            BlockWeapons.createDarkOakPlanksSword(), BlockArmor.createDarkOakPlanksHelmet(), BlockArmor.createDarkOakPlanksChestplate(),
            BlockArmor.createDarkOakPlanksLeggings(), BlockArmor.createDarkOakPlanksBoots()));
        sets.add(new ArmorSetDisplay(Items.MANGROVE_PLANKS, "Mangrove Planks",
            BlockWeapons.createMangrovePlanksSword(), BlockArmor.createMangrovePlanksHelmet(), BlockArmor.createMangrovePlanksChestplate(),
            BlockArmor.createMangrovePlanksLeggings(), BlockArmor.createMangrovePlanksBoots()));
        sets.add(new ArmorSetDisplay(Items.CHERRY_PLANKS, "Cherry Planks",
            BlockWeapons.createCherryPlanksSword(), BlockArmor.createCherryPlanksHelmet(), BlockArmor.createCherryPlanksChestplate(),
            BlockArmor.createCherryPlanksLeggings(), BlockArmor.createCherryPlanksBoots()));
        sets.add(new ArmorSetDisplay(Items.BAMBOO_PLANKS, "Bamboo Planks",
            BlockWeapons.createBambooPlanksSword(), BlockArmor.createBambooPlanksHelmet(), BlockArmor.createBambooPlanksChestplate(),
            BlockArmor.createBambooPlanksLeggings(), BlockArmor.createBambooPlanksBoots()));
        sets.add(new ArmorSetDisplay(Items.CRIMSON_PLANKS, "Crimson Planks",
            BlockWeapons.createCrimsonPlanksSword(), BlockArmor.createCrimsonPlanksHelmet(), BlockArmor.createCrimsonPlanksChestplate(),
            BlockArmor.createCrimsonPlanksLeggings(), BlockArmor.createCrimsonPlanksBoots()));
        sets.add(new ArmorSetDisplay(Items.WARPED_PLANKS, "Warped Planks",
            BlockWeapons.createWarpedPlanksSword(), BlockArmor.createWarpedPlanksHelmet(), BlockArmor.createWarpedPlanksChestplate(),
            BlockArmor.createWarpedPlanksLeggings(), BlockArmor.createWarpedPlanksBoots()));
        sets.add(new ArmorSetDisplay(Items.OAK_PLANKS, "Oak Planks",
            BlockWeapons.createOakPlanksSword(), BlockArmor.createOakPlanksHelmet(), BlockArmor.createOakPlanksChestplate(),
            BlockArmor.createOakPlanksLeggings(), BlockArmor.createOakPlanksBoots()));
        // Stone/Mud/Log Armor
        sets.add(new ArmorSetDisplay(Items.OAK_LOG, "Oak Log",
            BlockWeapons.createOakLogSword(), BlockArmor.createOakLogHelmet(), BlockArmor.createOakLogChestplate(),
            BlockArmor.createOakLogLeggings(), BlockArmor.createOakLogBoots()));
        sets.add(new ArmorSetDisplay(Items.SPRUCE_LOG, "Spruce Log",
            BlockWeapons.createSpruceLogSword(), BlockArmor.createSpruceLogHelmet(), BlockArmor.createSpruceLogChestplate(),
            BlockArmor.createSpruceLogLeggings(), BlockArmor.createSpruceLogBoots()));
        sets.add(new ArmorSetDisplay(Items.BIRCH_LOG, "Birch Log",
            BlockWeapons.createBirchLogSword(), BlockArmor.createBirchLogHelmet(), BlockArmor.createBirchLogChestplate(),
            BlockArmor.createBirchLogLeggings(), BlockArmor.createBirchLogBoots()));
        sets.add(new ArmorSetDisplay(Items.STONE_BRICKS, "Stone Bricks",
            BlockWeapons.createStoneBricksSword(), BlockArmor.createStoneBricksHelmet(), BlockArmor.createStoneBricksChestplate(),
            BlockArmor.createStoneBricksLeggings(), BlockArmor.createStoneBricksBoots()));
        sets.add(new ArmorSetDisplay(Items.COBBLESTONE, "Cobblestone",
            BlockWeapons.createCobblestoneSword(), BlockArmor.createCobblestoneHelmet(), BlockArmor.createCobblestoneChestplate(),
            BlockArmor.createCobblestoneLeggings(), BlockArmor.createCobblestoneBoots()));
        sets.add(new ArmorSetDisplay(Items.MOSSY_COBBLESTONE, "Mossy Cobblestone",
            BlockWeapons.createMossyCobblestoneSword(), BlockArmor.createMossyCobblestoneHelmet(), BlockArmor.createMossyCobblestoneChestplate(),
            BlockArmor.createMossyCobblestoneLeggings(), BlockArmor.createMossyCobblestoneBoots()));
        sets.add(new ArmorSetDisplay(Items.COBBLED_DEEPSLATE, "Cobbled Deepslate",
            BlockWeapons.createCobbledDeepslateSword(), BlockArmor.createCobbledDeepslateHelmet(), BlockArmor.createCobbledDeepslateChestplate(),
            BlockArmor.createCobbledDeepslateLeggings(), BlockArmor.createCobbledDeepslateBoots()));
        sets.add(new ArmorSetDisplay(Items.MUD_BRICKS, "Mud Bricks",
            BlockWeapons.createMudBricksSword(), BlockArmor.createMudBricksHelmet(), BlockArmor.createMudBricksChestplate(),
            BlockArmor.createMudBricksLeggings(), BlockArmor.createMudBricksBoots()));
        sets.add(new ArmorSetDisplay(Items.MANGROVE_ROOTS, "Mangrove Roots",
            BlockWeapons.createMangroveRootsSword(), BlockArmor.createMangroveRootsHelmet(), BlockArmor.createMangroveRootsChestplate(),
            BlockArmor.createMangroveRootsLeggings(), BlockArmor.createMangroveRootsBoots()));
        sets.add(new ArmorSetDisplay(Items.MUDDY_MANGROVE_ROOTS, "Muddy Mangrove Roots",
            BlockWeapons.createMuddyMangroveRootsSword(), BlockArmor.createMuddyMangroveRootsHelmet(), BlockArmor.createMuddyMangroveRootsChestplate(),
            BlockArmor.createMuddyMangroveRootsLeggings(), BlockArmor.createMuddyMangroveRootsBoots()));
        // Copper/Netherite/Froglight/Basalt Armor
        sets.add(new ArmorSetDisplay(Items.NETHERITE_BLOCK, "Netherite Block",
            BlockWeapons.createNetheriteBlockSword(), BlockArmor.createNetheriteBlockHelmet(), BlockArmor.createNetheriteBlockChestplate(),
            BlockArmor.createNetheriteBlockLeggings(), BlockArmor.createNetheriteBlockBoots()));
        sets.add(new ArmorSetDisplay(Items.CHISELED_COPPER, "Chiseled Copper",
            BlockWeapons.createChiseledCopperSword(), BlockArmor.createChiseledCopperHelmet(), BlockArmor.createChiseledCopperChestplate(),
            BlockArmor.createChiseledCopperLeggings(), BlockArmor.createChiseledCopperBoots()));
        sets.add(new ArmorSetDisplay(Items.CUT_COPPER, "Cut Copper",
            BlockWeapons.createCutCopperSword(), BlockArmor.createCutCopperHelmet(), BlockArmor.createCutCopperChestplate(),
            BlockArmor.createCutCopperLeggings(), BlockArmor.createCutCopperBoots()));
        sets.add(new ArmorSetDisplay(Items.EXPOSED_COPPER, "Exposed Copper",
            BlockWeapons.createExposedCopperSword(), BlockArmor.createExposedCopperHelmet(), BlockArmor.createExposedCopperChestplate(),
            BlockArmor.createExposedCopperLeggings(), BlockArmor.createExposedCopperBoots()));
        sets.add(new ArmorSetDisplay(Items.WEATHERED_COPPER, "Weathered Copper",
            BlockWeapons.createWeatheredCopperSword(), BlockArmor.createWeatheredCopperHelmet(), BlockArmor.createWeatheredCopperChestplate(),
            BlockArmor.createWeatheredCopperLeggings(), BlockArmor.createWeatheredCopperBoots()));
        sets.add(new ArmorSetDisplay(Items.OXIDIZED_COPPER, "Oxidised Copper",
            BlockWeapons.createOxidisedCopperSword(), BlockArmor.createOxidisedCopperHelmet(), BlockArmor.createOxidisedCopperChestplate(),
            BlockArmor.createOxidisedCopperLeggings(), BlockArmor.createOxidisedCopperBoots()));
        sets.add(new ArmorSetDisplay(Items.WAXED_CUT_COPPER, "Waxed Cut Copper",
            BlockWeapons.createWaxedCutCopperSword(), BlockArmor.createWaxedCutCopperHelmet(), BlockArmor.createWaxedCutCopperChestplate(),
            BlockArmor.createWaxedCutCopperLeggings(), BlockArmor.createWaxedCutCopperBoots()));
        sets.add(new ArmorSetDisplay(Items.POLISHED_BASALT, "Polished Basalt",
            BlockWeapons.createPolishedBasaltSword(), BlockArmor.createPolishedBasaltHelmet(), BlockArmor.createPolishedBasaltChestplate(),
            BlockArmor.createPolishedBasaltLeggings(), BlockArmor.createPolishedBasaltBoots()));
        sets.add(new ArmorSetDisplay(Items.VERDANT_FROGLIGHT, "Verdant Froglight",
            BlockWeapons.createVerdantFroglightSword(), BlockArmor.createVerdantFroglightHelmet(), BlockArmor.createVerdantFroglightChestplate(),
            BlockArmor.createVerdantFroglightLeggings(), BlockArmor.createVerdantFroglightBoots()));
        sets.add(new ArmorSetDisplay(Items.PEARLESCENT_FROGLIGHT, "Pearlescent Froglight",
            BlockWeapons.createPearlescentFroglightSword(), BlockArmor.createPearlescentFroglightHelmet(), BlockArmor.createPearlescentFroglightChestplate(),
            BlockArmor.createPearlescentFroglightLeggings(), BlockArmor.createPearlescentFroglightBoots()));
        sets.add(new ArmorSetDisplay(Items.OCHRE_FROGLIGHT, "Ochre Froglight",
            BlockWeapons.createOchreFroglightSword(), BlockArmor.createOchreFroglightHelmet(), BlockArmor.createOchreFroglightChestplate(),
            BlockArmor.createOchreFroglightLeggings(), BlockArmor.createOchreFroglightBoots()));
        // Material Item Armor
        sets.add(new ArmorSetDisplay(Items.IRON_NUGGET, "Iron Nugget",
            BlockWeapons.createIronNuggetSword(), BlockArmor.createIronNuggetHelmet(), BlockArmor.createIronNuggetChestplate(),
            BlockArmor.createIronNuggetLeggings(), BlockArmor.createIronNuggetBoots()));
        sets.add(new ArmorSetDisplay(Items.GOLD_NUGGET, "Gold Nugget",
            BlockWeapons.createGoldNuggetSword(), BlockArmor.createGoldNuggetHelmet(), BlockArmor.createGoldNuggetChestplate(),
            BlockArmor.createGoldNuggetLeggings(), BlockArmor.createGoldNuggetBoots()));
        sets.add(new ArmorSetDisplay(Items.COPPER_INGOT, "Copper Ingot",
            BlockWeapons.createCopperIngotSword(), BlockArmor.createCopperIngotHelmet(), BlockArmor.createCopperIngotChestplate(),
            BlockArmor.createCopperIngotLeggings(), BlockArmor.createCopperIngotBoots()));
        sets.add(new ArmorSetDisplay(Items.EMERALD, "Emerald",
            BlockWeapons.createEmeraldSword(), BlockArmor.createEmeraldHelmet(), BlockArmor.createEmeraldChestplate(),
            BlockArmor.createEmeraldLeggings(), BlockArmor.createEmeraldBoots()));
        sets.add(new ArmorSetDisplay(Items.LAPIS_LAZULI, "Lapis Lazuli",
            BlockWeapons.createLapisLazuliSword(), BlockArmor.createLapisLazuliHelmet(), BlockArmor.createLapisLazuliChestplate(),
            BlockArmor.createLapisLazuliLeggings(), BlockArmor.createLapisLazuliBoots()));
        sets.add(new ArmorSetDisplay(Items.AMETHYST_SHARD, "Amethyst Shard",
            BlockWeapons.createAmethystShardSword(), BlockArmor.createAmethystShardHelmet(), BlockArmor.createAmethystShardChestplate(),
            BlockArmor.createAmethystShardLeggings(), BlockArmor.createAmethystShardBoots()));
        sets.add(new ArmorSetDisplay(Items.FLINT, "Flint",
            BlockWeapons.createFlintSword(), BlockArmor.createFlintHelmet(), BlockArmor.createFlintChestplate(),
            BlockArmor.createFlintLeggings(), BlockArmor.createFlintBoots()));
        sets.add(new ArmorSetDisplay(Items.BONE_MEAL, "Bone Meal",
            BlockWeapons.createBoneMealSword(), BlockArmor.createBoneMealHelmet(), BlockArmor.createBoneMealChestplate(),
            BlockArmor.createBoneMealLeggings(), BlockArmor.createBoneMealBoots()));
        sets.add(new ArmorSetDisplay(Items.CHARCOAL, "Charcoal",
            BlockWeapons.createCharcoalSword(), BlockArmor.createCharcoalHelmet(), BlockArmor.createCharcoalChestplate(),
            BlockArmor.createCharcoalLeggings(), BlockArmor.createCharcoalBoots()));
        sets.add(new ArmorSetDisplay(Items.END_STONE, "End Stone",
            BlockWeapons.createEndstoneSword(), BlockArmor.createEndstoneHelmet(), BlockArmor.createEndstoneChestplate(),
            BlockArmor.createEndstoneLeggings(), BlockArmor.createEndstoneBoots()));
        sets.add(new ArmorSetDisplay(Items.SNOW_BLOCK, "Snow Block",
            BlockWeapons.createSnowBlockSword(), BlockArmor.createSnowBlockHelmet(), BlockArmor.createSnowBlockChestplate(),
            BlockArmor.createSnowBlockLeggings(), BlockArmor.createSnowBlockBoots()));
        // NEW 25 MATERIAL ARMOR SETS
        sets.add(new ArmorSetDisplay(Items.PRISMARINE_SHARD, "Raw Prismarine",
            BlockWeapons.createRawPrismarineSword(), BlockArmor.createRawPrismarineHelmet(), BlockArmor.createRawPrismarineChestplate(),
            BlockArmor.createRawPrismarineLeggings(), BlockArmor.createRawPrismarineBoots()));
        sets.add(new ArmorSetDisplay(Items.CLAY_BALL, "Clay",
            BlockWeapons.createClaySword(), BlockArmor.createClayHelmet(), BlockArmor.createClayChestplate(),
            BlockArmor.createClayLeggings(), BlockArmor.createClayBoots()));
        sets.add(new ArmorSetDisplay(Items.WHITE_GLAZED_TERRACOTTA, "Glazed Terracotta",
            BlockWeapons.createGlazedTerracottaSword(), BlockArmor.createGlazedTerracottaHelmet(), BlockArmor.createGlazedTerracottaChestplate(),
            BlockArmor.createGlazedTerracottaLeggings(), BlockArmor.createGlazedTerracottaBoots()));
        sets.add(new ArmorSetDisplay(Items.WHITE_CONCRETE, "Concrete",
            BlockWeapons.createConcreteSword(), BlockArmor.createConcreteHelmet(), BlockArmor.createConcreteChestplate(),
            BlockArmor.createConcreteLeggings(), BlockArmor.createConcreteBoots()));
        sets.add(new ArmorSetDisplay(Items.WHITE_CONCRETE_POWDER, "Concrete Powder",
            BlockWeapons.createConcretePowderSword(), BlockArmor.createConcretePowderHelmet(), BlockArmor.createConcretePowderChestplate(),
            BlockArmor.createConcretePowderLeggings(), BlockArmor.createConcretePowderBoots()));
        sets.add(new ArmorSetDisplay(Items.BOOKSHELF, "Bookshelf",
            BlockWeapons.createBookshelfSword(), BlockArmor.createBookshelfHelmet(), BlockArmor.createBookshelfChestplate(),
            BlockArmor.createBookshelfLeggings(), BlockArmor.createBookshelfBoots()));
        sets.add(new ArmorSetDisplay(Items.BARREL, "Barrel",
            BlockWeapons.createBarrelSword(), BlockArmor.createBarrelHelmet(), BlockArmor.createBarrelChestplate(),
            BlockArmor.createBarrelLeggings(), BlockArmor.createBarrelBoots()));
        sets.add(new ArmorSetDisplay(Items.SMITHING_TABLE, "Smithing Table",
            BlockWeapons.createSmithingTableSword(), BlockArmor.createSmithingTableHelmet(), BlockArmor.createSmithingTableChestplate(),
            BlockArmor.createSmithingTableLeggings(), BlockArmor.createSmithingTableBoots()));
        sets.add(new ArmorSetDisplay(Items.ANVIL, "Anvil",
            BlockWeapons.createAnvilSword(), BlockArmor.createAnvilHelmet(), BlockArmor.createAnvilChestplate(),
            BlockArmor.createAnvilLeggings(), BlockArmor.createAnvilBoots()));
        sets.add(new ArmorSetDisplay(Items.GRINDSTONE, "Grindstone",
            BlockWeapons.createGrindstoneSword(), BlockArmor.createGrindstoneHelmet(), BlockArmor.createGrindstoneChestplate(),
            BlockArmor.createGrindstoneLeggings(), BlockArmor.createGrindstoneBoots()));
        sets.add(new ArmorSetDisplay(Items.LECTERN, "Lectern",
            BlockWeapons.createLecternSword(), BlockArmor.createLecternHelmet(), BlockArmor.createLecternChestplate(),
            BlockArmor.createLecternLeggings(), BlockArmor.createLecternBoots()));
        sets.add(new ArmorSetDisplay(Items.COMPOSTER, "Composter",
            BlockWeapons.createComposterSword(), BlockArmor.createComposterHelmet(), BlockArmor.createComposterChestplate(),
            BlockArmor.createComposterLeggings(), BlockArmor.createComposterBoots()));
        sets.add(new ArmorSetDisplay(Items.STONECUTTER, "Stonecutter",
            BlockWeapons.createStonecutterSword(), BlockArmor.createStonecutterHelmet(), BlockArmor.createStonecutterChestplate(),
            BlockArmor.createStonecutterLeggings(), BlockArmor.createStonecutterBoots()));
        sets.add(new ArmorSetDisplay(Items.DRIED_KELP_BLOCK, "Dried Kelp",
            BlockWeapons.createDriedKelpSword(), BlockArmor.createDriedKelpHelmet(), BlockArmor.createDriedKelpChestplate(),
            BlockArmor.createDriedKelpLeggings(), BlockArmor.createDriedKelpBoots()));
        sets.add(new ArmorSetDisplay(Items.BRAIN_CORAL_BLOCK, "Coral Block",
            BlockWeapons.createCoralBlockSword(), BlockArmor.createCoralBlockHelmet(), BlockArmor.createCoralBlockChestplate(),
            BlockArmor.createCoralBlockLeggings(), BlockArmor.createCoralBlockBoots()));
        sets.add(new ArmorSetDisplay(Items.LANTERN, "Lantern",
            BlockWeapons.createLanternSword(), BlockArmor.createLanternHelmet(), BlockArmor.createLanternChestplate(),
            BlockArmor.createLanternLeggings(), BlockArmor.createLanternBoots()));
        sets.add(new ArmorSetDisplay(Items.IRON_BARS, "Chain",
            BlockWeapons.createChainSword(), BlockArmor.createChainHelmet(), BlockArmor.createChainChestplate(),
            BlockArmor.createChainLeggings(), BlockArmor.createChainBoots()));
        sets.add(new ArmorSetDisplay(Items.LIGHTNING_ROD, "Lightning Rod",
            BlockWeapons.createLightningRodSword(), BlockArmor.createLightningRodHelmet(), BlockArmor.createLightningRodChestplate(),
            BlockArmor.createLightningRodLeggings(), BlockArmor.createLightningRodBoots()));
        sets.add(new ArmorSetDisplay(Items.TINTED_GLASS, "Tinted Glass",
            BlockWeapons.createTintedGlassSword(), BlockArmor.createTintedGlassHelmet(), BlockArmor.createTintedGlassChestplate(),
            BlockArmor.createTintedGlassLeggings(), BlockArmor.createTintedGlassBoots()));
        sets.add(new ArmorSetDisplay(Items.CANDLE, "Candle",
            BlockWeapons.createCandleSword(), BlockArmor.createCandleHelmet(), BlockArmor.createCandleChestplate(),
            BlockArmor.createCandleLeggings(), BlockArmor.createCandleBoots()));
        sets.add(new ArmorSetDisplay(Items.SCULK_SENSOR, "Sculk Sensor",
            BlockWeapons.createSculkSensorSword(), BlockArmor.createSculkSensorHelmet(), BlockArmor.createSculkSensorChestplate(),
            BlockArmor.createSculkSensorLeggings(), BlockArmor.createSculkSensorBoots()));
        sets.add(new ArmorSetDisplay(Items.SCULK_CATALYST, "Sculk Catalyst",
            BlockWeapons.createSculkCatalystSword(), BlockArmor.createSculkCatalystHelmet(), BlockArmor.createSculkCatalystChestplate(),
            BlockArmor.createSculkCatalystLeggings(), BlockArmor.createSculkCatalystBoots()));
        sets.add(new ArmorSetDisplay(Items.SCULK_SHRIEKER, "Sculk Shrieker",
            BlockWeapons.createSculkShriekerSword(), BlockArmor.createSculkShriekerHelmet(), BlockArmor.createSculkShriekerChestplate(),
            BlockArmor.createSculkShriekerLeggings(), BlockArmor.createSculkShriekerBoots()));
        sets.add(new ArmorSetDisplay(Items.BREWING_STAND, "Brewing Stand",
            BlockWeapons.createBrewingStandSword(), BlockArmor.createBrewingStandHelmet(), BlockArmor.createBrewingStandChestplate(),
            BlockArmor.createBrewingStandLeggings(), BlockArmor.createBrewingStandBoots()));
        sets.add(new ArmorSetDisplay(Items.FURNACE, "Furnace",
            BlockWeapons.createFurnaceSword(), BlockArmor.createFurnaceHelmet(), BlockArmor.createFurnaceChestplate(),
            BlockArmor.createFurnaceLeggings(), BlockArmor.createFurnaceBoots()));

        // Pagination - 3 sets per page (each set takes 2 rows)
        int setsPerPage = 3;
        int totalPages = Math.max(1, (sets.size() + setsPerPage - 1) / setsPerPage);
        int clampedPage = Math.min(page, totalPages - 1);
        int startIdx = clampedPage * setsPerPage;
        int endIdx = Math.min(startIdx + setsPerPage, sets.size());

        // Display sets in organized rows
        for (int i = startIdx; i < endIdx; i++) {
            ArmorSetDisplay set = sets.get(i);
            int rowOffset = (i - startIdx) * 18; // 2 rows per set
            int baseSlot = 10 + rowOffset;
            
            // Row 1: Block | (gap) | Helmet | Chestplate | Leggings | Boots | Weapon
            // Row 2: Set Name separator
            
            // Block material (left side)
            gui.setSlot(baseSlot, new GuiElementBuilder(set.blockItem)
                    .setName(Component.literal(set.name + " Set").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Material:").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(set.name).withStyle(ChatFormatting.WHITE))
                    .glow()
                    .build());
            
            // Gap slot (shifted armor 1 to the right)
            gui.setSlot(baseSlot + 1, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
            
            // Armor pieces (middle - shifted 1 slot right)
            gui.setSlot(baseSlot + 2, createArmorElement(set.helmet, player));
            gui.setSlot(baseSlot + 3, createArmorElement(set.chestplate, player));
            gui.setSlot(baseSlot + 4, createArmorElement(set.leggings, player));
            gui.setSlot(baseSlot + 5, createArmorElement(set.boots, player));
            
            // Weapon (right side)
            gui.setSlot(baseSlot + 6, new GuiElementBuilder(set.sword)
                    .setName(set.sword.get(DataComponents.CUSTOM_NAME))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("✨ Click for recipe").withStyle(ChatFormatting.YELLOW))
                    .setCallback((idx, type, containerInput, slotGui) -> showItemInfo(player, set.sword))
                    .build());
            
            // Add separator lore below
            Component setName = Component.literal("━━ " + set.name + " Set ━━").withStyle(ChatFormatting.DARK_GRAY);
            for (int sep = baseSlot + 9; sep < baseSlot + 15 && sep < 54; sep++) {
                if (sep < 45) {
                    gui.setSlot(sep, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                            .setName(setName)
                            .build());
                }
            }
        }

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back to Menu").withStyle(ChatFormatting.YELLOW))
                .setCallback((slot, type, containerInput, slotGui) -> openMainMenu(player))
                .build());

        // Pagination
        if (clampedPage > 0) {
            final int prev = clampedPage - 1;
            gui.setSlot(46, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("← Previous Page").withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal("Page " + (clampedPage) + " / " + totalPages))
                    .setCallback((slot, type, containerInput, slotGui) -> openGemAndBlockArmorMenu(player, prev))
                    .build());
        }
        if (clampedPage < totalPages - 1) {
            final int next = clampedPage + 1;
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Next Page →").withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal("Page " + (clampedPage + 2) + " / " + totalPages))
                    .setCallback((slot, type, containerInput, slotGui) -> openGemAndBlockArmorMenu(player, next))
                    .build());
        }

        gui.setSlot(50, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("Page " + (clampedPage + 1) + " / " + totalPages).withStyle(ChatFormatting.WHITE))
                .build());

        gui.open();
    }
    
    private static GuiElementBuilder createArmorElement(ItemStack armor, ServerPlayer player) {
        Component name = armor.get(DataComponents.CUSTOM_NAME);
        return new GuiElementBuilder(armor)
                .setName(name)
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("✨ Click for recipe").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> showItemInfo(player, armor));
    }
    
    // Helper class for organizing armor set display
    private static class ArmorSetDisplay {
        final net.minecraft.world.item.Item blockItem;
        final String name;
        final ItemStack sword, helmet, chestplate, leggings, boots;
        
        ArmorSetDisplay(net.minecraft.world.item.Item blockItem, String name,
                        ItemStack sword, ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots) {
            this.blockItem = blockItem;
            this.name = name;
            this.sword = sword;
            this.helmet = helmet;
            this.chestplate = chestplate;
            this.leggings = leggings;
            this.boots = boots;
        }
    }

    // Helper class for organizing tool set display
    private static class ToolSetDisplay {
        final net.minecraft.world.item.Item blockItem;
        final String name;
        final ChatFormatting color;
        final ItemStack pickaxe, axe, shovel, hoe;
        
        ToolSetDisplay(net.minecraft.world.item.Item blockItem, String name,
                       ItemStack pickaxe, ItemStack axe, ItemStack shovel, ItemStack hoe) {
            this.blockItem = blockItem;
            this.name = name;
            this.color = ChatFormatting.WHITE;
            this.pickaxe = pickaxe;
            this.axe = axe;
            this.shovel = shovel;
            this.hoe = hoe;
        }
        
        ToolSetDisplay(net.minecraft.world.item.Item blockItem, String name, ChatFormatting color,
                       ItemStack pickaxe, ItemStack axe, ItemStack shovel, ItemStack hoe) {
            this.blockItem = blockItem;
            this.name = name;
            this.color = color;
            this.pickaxe = pickaxe;
            this.axe = axe;
            this.shovel = shovel;
            this.hoe = hoe;
        }
    }

    // ============================================================
    // BLOCK TOOLS MENU
    // ============================================================

    private static void openBlockToolsMenu(ServerPlayer player, int page) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("⛏ Block Tools"));

        RecipeGuiTheme.fillDoubleChestCatalog(gui);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.DIAMOND_PICKAXE)
                .setName(Component.literal("⛏ Block Tools").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Block-themed tools with special abilities").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Click any tool to view recipe").withStyle(ChatFormatting.GRAY))
                .glow().build());

        // Build tool sets list
        List<ToolSetDisplay> sets = new ArrayList<>();
        sets.add(new ToolSetDisplay(Items.GLASS, "Glass",
            BlockTools.createGlassPickaxe(), BlockTools.createGlassAxe(), BlockTools.createGlassShovel(), BlockTools.createGlassHoe()));
        sets.add(new ToolSetDisplay(Items.OBSIDIAN, "Obsidian",
            BlockTools.createObsidianPickaxe(), BlockTools.createObsidianAxe(), BlockTools.createObsidianShovel(), BlockTools.createObsidianHoe()));
        sets.add(new ToolSetDisplay(Items.QUARTZ_BLOCK, "Quartz",
            BlockTools.createQuartzPickaxe(), BlockTools.createQuartzAxe(), BlockTools.createQuartzShovel(), BlockTools.createQuartzHoe()));
        sets.add(new ToolSetDisplay(Items.GLOWSTONE, "Glowstone",
            BlockTools.createGlowstonePickaxe(), BlockTools.createGlowstoneAxe(), BlockTools.createGlowstoneShovel(), BlockTools.createGlowstoneHoe()));
        sets.add(new ToolSetDisplay(Items.REDSTONE_BLOCK, "Redstone",
            BlockTools.createRedstonePickaxe(), BlockTools.createRedstoneAxe(), BlockTools.createRedstoneShovel(), BlockTools.createRedstoneHoe()));
        sets.add(new ToolSetDisplay(Items.NETHERRACK, "Netherrack",
            BlockTools.createNetherrackPickaxe(), BlockTools.createNetherrackAxe(), BlockTools.createNetherrackShovel(), BlockTools.createNetherrackHoe()));
        sets.add(new ToolSetDisplay(Items.END_STONE, "End Stone",
            BlockTools.createEndstonePickaxe(), BlockTools.createEndstoneAxe(), BlockTools.createEndstoneShovel(), BlockTools.createEndstoneHoe()));
        sets.add(new ToolSetDisplay(Items.PACKED_ICE, "Ice",
            BlockTools.createIcePickaxe(), BlockTools.createIceAxe(), BlockTools.createIceShovel(), BlockTools.createIceHoe()));
        sets.add(new ToolSetDisplay(Items.PRISMARINE, "Prismarine",
            BlockTools.createPrismarinePickaxe(), BlockTools.createPrismarineAxe(), BlockTools.createPrismarineShovel(), BlockTools.createPrismarineHoe()));
        sets.add(new ToolSetDisplay(Items.TERRACOTTA, "Terracotta",
            BlockTools.createTerracottaPickaxe(), BlockTools.createTerracottaAxe(), BlockTools.createTerracottaShovel(), BlockTools.createTerracottaHoe()));
        sets.add(new ToolSetDisplay(Items.MOSSY_COBBLESTONE, "Mossy",
            BlockTools.createMossyPickaxe(), BlockTools.createMossyAxe(), BlockTools.createMossyShovel(), BlockTools.createMossyHoe()));
        sets.add(new ToolSetDisplay(Items.SOUL_SAND, "Soul Sand",
            BlockTools.createSoulSandPickaxe(), BlockTools.createSoulSandAxe(), BlockTools.createSoulSandShovel(), BlockTools.createSoulSandHoe()));
        sets.add(new ToolSetDisplay(Items.MAGMA_BLOCK, "Magma",
            BlockTools.createMagmaPickaxe(), BlockTools.createMagmaAxe(), BlockTools.createMagmaShovel(), BlockTools.createMagmaHoe()));
        sets.add(new ToolSetDisplay(Items.SANDSTONE, "Sandstone",
            BlockTools.createSandstonePickaxe(), BlockTools.createSandstoneAxe(), BlockTools.createSandstoneShovel(), BlockTools.createSandstoneHoe()));
        sets.add(new ToolSetDisplay(Items.AMETHYST_BLOCK, "Amethyst",
            BlockTools.createAmethystPickaxe(), BlockTools.createAmethystAxe(), BlockTools.createAmethystShovel(), BlockTools.createAmethystHoe()));
        sets.add(new ToolSetDisplay(Items.COAL_BLOCK, "Coal",
            BlockTools.createCoalPickaxe(), BlockTools.createCoalAxe(), BlockTools.createCoalShovel(), BlockTools.createCoalHoe()));

        // 36+ NEW BLOCK TOOL SETS
        sets.add(new ToolSetDisplay(Items.DIAMOND_BLOCK, "Diamond Block",
            BlockTools.createDiamondBlockPickaxe(), BlockTools.createDiamondBlockAxe(), BlockTools.createDiamondBlockShovel(), BlockTools.createDiamondBlockHoe()));
        sets.add(new ToolSetDisplay(Items.EMERALD_BLOCK, "Emerald Block",
            BlockTools.createEmeraldBlockPickaxe(), BlockTools.createEmeraldBlockAxe(), BlockTools.createEmeraldBlockShovel(), BlockTools.createEmeraldBlockHoe()));
        sets.add(new ToolSetDisplay(Items.GOLD_BLOCK, "Gold Block",
            BlockTools.createGoldBlockPickaxe(), BlockTools.createGoldBlockAxe(), BlockTools.createGoldBlockShovel(), BlockTools.createGoldBlockHoe()));
        sets.add(new ToolSetDisplay(Items.IRON_BLOCK, "Iron Block",
            BlockTools.createIronBlockPickaxe(), BlockTools.createIronBlockAxe(), BlockTools.createIronBlockShovel(), BlockTools.createIronBlockHoe()));
        sets.add(new ToolSetDisplay(Items.LAPIS_BLOCK, "Lapis Block",
            BlockTools.createLapisBlockPickaxe(), BlockTools.createLapisBlockAxe(), BlockTools.createLapisBlockShovel(), BlockTools.createLapisBlockHoe()));
        sets.add(new ToolSetDisplay(Items.COPPER_BLOCK, "Copper Block",
            BlockTools.createCopperBlockPickaxe(), BlockTools.createCopperBlockAxe(), BlockTools.createCopperBlockShovel(), BlockTools.createCopperBlockHoe()));
        sets.add(new ToolSetDisplay(Items.ANCIENT_DEBRIS, "Ancient Debris",
            BlockTools.createAncientDebrisPickaxe(), BlockTools.createAncientDebrisAxe(), BlockTools.createAncientDebrisShovel(), BlockTools.createAncientDebrisHoe()));
        sets.add(new ToolSetDisplay(Items.BASALT, "Basalt",
            BlockTools.createBasaltPickaxe(), BlockTools.createBasaltAxe(), BlockTools.createBasaltShovel(), BlockTools.createBasaltHoe()));
        sets.add(new ToolSetDisplay(Items.BLACKSTONE, "Blackstone",
            BlockTools.createBlackstonePickaxe(), BlockTools.createBlackstoneAxe(), BlockTools.createBlackstoneShovel(), BlockTools.createBlackstoneHoe()));
        sets.add(new ToolSetDisplay(Items.BONE_BLOCK, "Bone Block",
            BlockTools.createBoneBlockPickaxe(), BlockTools.createBoneBlockAxe(), BlockTools.createBoneBlockShovel(), BlockTools.createBoneBlockHoe()));
        sets.add(new ToolSetDisplay(Items.BRICKS, "Brick",
            BlockTools.createBrickPickaxe(), BlockTools.createBrickAxe(), BlockTools.createBrickShovel(), BlockTools.createBrickHoe()));
        sets.add(new ToolSetDisplay(Items.CACTUS, "Cactus",
            BlockTools.createCactusPickaxe(), BlockTools.createCactusAxe(), BlockTools.createCactusShovel(), BlockTools.createCactusHoe()));
        sets.add(new ToolSetDisplay(Items.CALCITE, "Calcite",
            BlockTools.createCalcitePickaxe(), BlockTools.createCalciteAxe(), BlockTools.createCalciteShovel(), BlockTools.createCalciteHoe()));
        sets.add(new ToolSetDisplay(Items.DEEPSLATE, "Deepslate",
            BlockTools.createDeepslatePickaxe(), BlockTools.createDeepslateAxe(), BlockTools.createDeepslateShovel(), BlockTools.createDeepslateHoe()));
        sets.add(new ToolSetDisplay(Items.DRIPSTONE_BLOCK, "Dripstone",
            BlockTools.createDripstonePickaxe(), BlockTools.createDripstoneAxe(), BlockTools.createDripstoneShovel(), BlockTools.createDripstoneHoe()));
        sets.add(new ToolSetDisplay(Items.HAY_BLOCK, "Hay",
            BlockTools.createHayPickaxe(), BlockTools.createHayAxe(), BlockTools.createHayShovel(), BlockTools.createHayHoe()));
        sets.add(new ToolSetDisplay(Items.HONEYCOMB_BLOCK, "Honeycomb",
            BlockTools.createHoneycombPickaxe(), BlockTools.createHoneycombAxe(), BlockTools.createHoneycombShovel(), BlockTools.createHoneycombHoe()));
        sets.add(new ToolSetDisplay(Items.LILY_PAD, "Lily Pad",
            BlockTools.createLilyPadPickaxe(), BlockTools.createLilyPadAxe(), BlockTools.createLilyPadShovel(), BlockTools.createLilyPadHoe()));
        sets.add(new ToolSetDisplay(Items.MELON, "Melon",
            BlockTools.createMelonPickaxe(), BlockTools.createMelonAxe(), BlockTools.createMelonShovel(), BlockTools.createMelonHoe()));
        sets.add(new ToolSetDisplay(Items.MOSS_BLOCK, "Moss Block",
            BlockTools.createMossBlockPickaxe(), BlockTools.createMossBlockAxe(), BlockTools.createMossBlockShovel(), BlockTools.createMossBlockHoe()));
        sets.add(new ToolSetDisplay(Items.MYCELIUM, "Mycelium",
            BlockTools.createMyceliumPickaxe(), BlockTools.createMyceliumAxe(), BlockTools.createMyceliumShovel(), BlockTools.createMyceliumHoe()));
        sets.add(new ToolSetDisplay(Items.NETHER_BRICKS, "Nether Brick",
            BlockTools.createNetherBrickPickaxe(), BlockTools.createNetherBrickAxe(), BlockTools.createNetherBrickShovel(), BlockTools.createNetherBrickHoe()));
        sets.add(new ToolSetDisplay(Items.PUMPKIN, "Pumpkin",
            BlockTools.createPumpkinPickaxe(), BlockTools.createPumpkinAxe(), BlockTools.createPumpkinShovel(), BlockTools.createPumpkinHoe()));
        sets.add(new ToolSetDisplay(Items.PURPUR_BLOCK, "Purpur",
            BlockTools.createPurpurPickaxe(), BlockTools.createPurpurAxe(), BlockTools.createPurpurShovel(), BlockTools.createPurpurHoe()));
        sets.add(new ToolSetDisplay(Items.SAND, "Sand",
            BlockTools.createSandPickaxe(), BlockTools.createSandAxe(), BlockTools.createSandShovel(), BlockTools.createSandHoe()));
        sets.add(new ToolSetDisplay(Items.SCULK, "Sculk",
            BlockTools.createSculkPickaxe(), BlockTools.createSculkAxe(), BlockTools.createSculkShovel(), BlockTools.createSculkHoe()));
        sets.add(new ToolSetDisplay(Items.SHROOMLIGHT, "Shroomlight",
            BlockTools.createShroomlightPickaxe(), BlockTools.createShroomlightAxe(), BlockTools.createShroomlightShovel(), BlockTools.createShroomlightHoe()));
        sets.add(new ToolSetDisplay(Items.SLIME_BLOCK, "Slime",
            BlockTools.createSlimePickaxe(), BlockTools.createSlimeAxe(), BlockTools.createSlimeShovel(), BlockTools.createSlimeHoe()));
        sets.add(new ToolSetDisplay(Items.SMOOTH_STONE, "Smooth Stone",
            BlockTools.createSmoothStonePickaxe(), BlockTools.createSmoothStoneAxe(), BlockTools.createSmoothStoneShovel(), BlockTools.createSmoothStoneHoe()));
        sets.add(new ToolSetDisplay(Items.SNOW_BLOCK, "Snow",
            BlockTools.createSnowPickaxe(), BlockTools.createSnowAxe(), BlockTools.createSnowShovel(), BlockTools.createSnowHoe()));
        sets.add(new ToolSetDisplay(Items.SOUL_SOIL, "Soul Soil",
            BlockTools.createSoulSoilPickaxe(), BlockTools.createSoulSoilAxe(), BlockTools.createSoulSoilShovel(), BlockTools.createSoulSoilHoe()));
        sets.add(new ToolSetDisplay(Items.SPONGE, "Sponge",
            BlockTools.createSpongePickaxe(), BlockTools.createSpongeAxe(), BlockTools.createSpongeShovel(), BlockTools.createSpongeHoe()));
        sets.add(new ToolSetDisplay(Items.TARGET, "Target",
            BlockTools.createTargetPickaxe(), BlockTools.createTargetAxe(), BlockTools.createTargetShovel(), BlockTools.createTargetHoe()));
        sets.add(new ToolSetDisplay(Items.TNT, "TNT",
            BlockTools.createTntPickaxe(), BlockTools.createTntAxe(), BlockTools.createTntShovel(), BlockTools.createTntHoe()));
        sets.add(new ToolSetDisplay(Items.WARPED_STEM, "Warped",
            BlockTools.createWarpedPickaxe(), BlockTools.createWarpedAxe(), BlockTools.createWarpedShovel(), BlockTools.createWarpedHoe()));
        sets.add(new ToolSetDisplay(Items.WET_SPONGE, "Wet Sponge",
            BlockTools.createWetSpongePickaxe(), BlockTools.createWetSpongeAxe(), BlockTools.createWetSpongeShovel(), BlockTools.createWetSpongeHoe()));
        // NEW 25 MATERIAL TOOL SETS
        sets.add(new ToolSetDisplay(Items.PRISMARINE_SHARD, "Raw Prismarine",
            BlockTools.createRawPrismarinePickaxe(), BlockTools.createRawPrismarineAxe(), BlockTools.createRawPrismarineShovel(), BlockTools.createRawPrismarineHoe()));
        sets.add(new ToolSetDisplay(Items.CLAY_BALL, "Clay",
            BlockTools.createClayPickaxe(), BlockTools.createClayAxe(), BlockTools.createClayShovel(), BlockTools.createClayHoe()));
        sets.add(new ToolSetDisplay(Items.WHITE_GLAZED_TERRACOTTA, "Glazed Terracotta",
            BlockTools.createGlazedTerracottaPickaxe(), BlockTools.createGlazedTerracottaAxe(), BlockTools.createGlazedTerracottaShovel(), BlockTools.createGlazedTerracottaHoe()));
        sets.add(new ToolSetDisplay(Items.WHITE_CONCRETE, "Concrete",
            BlockTools.createConcretePickaxe(), BlockTools.createConcreteAxe(), BlockTools.createConcreteShovel(), BlockTools.createConcreteHoe()));
        sets.add(new ToolSetDisplay(Items.WHITE_CONCRETE_POWDER, "Concrete Powder",
            BlockTools.createConcretePowderPickaxe(), BlockTools.createConcretePowderAxe(), BlockTools.createConcretePowderShovel(), BlockTools.createConcretePowderHoe()));
        sets.add(new ToolSetDisplay(Items.BOOKSHELF, "Bookshelf",
            BlockTools.createBookshelfPickaxe(), BlockTools.createBookshelfAxe(), BlockTools.createBookshelfShovel(), BlockTools.createBookshelfHoe()));
        sets.add(new ToolSetDisplay(Items.BARREL, "Barrel",
            BlockTools.createBarrelPickaxe(), BlockTools.createBarrelAxe(), BlockTools.createBarrelShovel(), BlockTools.createBarrelHoe()));
        sets.add(new ToolSetDisplay(Items.SMITHING_TABLE, "Smithing Table",
            BlockTools.createSmithingTablePickaxe(), BlockTools.createSmithingTableAxe(), BlockTools.createSmithingTableShovel(), BlockTools.createSmithingTableHoe()));
        sets.add(new ToolSetDisplay(Items.ANVIL, "Anvil",
            BlockTools.createAnvilPickaxe(), BlockTools.createAnvilAxe(), BlockTools.createAnvilShovel(), BlockTools.createAnvilHoe()));
        sets.add(new ToolSetDisplay(Items.GRINDSTONE, "Grindstone",
            BlockTools.createGrindstonePickaxe(), BlockTools.createGrindstoneAxe(), BlockTools.createGrindstoneShovel(), BlockTools.createGrindstoneHoe()));
        sets.add(new ToolSetDisplay(Items.LECTERN, "Lectern",
            BlockTools.createLecternPickaxe(), BlockTools.createLecternAxe(), BlockTools.createLecternShovel(), BlockTools.createLecternHoe()));
        sets.add(new ToolSetDisplay(Items.COMPOSTER, "Composter",
            BlockTools.createComposterPickaxe(), BlockTools.createComposterAxe(), BlockTools.createComposterShovel(), BlockTools.createComposterHoe()));
        sets.add(new ToolSetDisplay(Items.STONECUTTER, "Stonecutter",
            BlockTools.createStonecutterPickaxe(), BlockTools.createStonecutterAxe(), BlockTools.createStonecutterShovel(), BlockTools.createStonecutterHoe()));
        sets.add(new ToolSetDisplay(Items.DRIED_KELP_BLOCK, "Dried Kelp",
            BlockTools.createDriedKelpPickaxe(), BlockTools.createDriedKelpAxe(), BlockTools.createDriedKelpShovel(), BlockTools.createDriedKelpHoe()));
        sets.add(new ToolSetDisplay(Items.BRAIN_CORAL_BLOCK, "Coral Block",
            BlockTools.createCoralBlockPickaxe(), BlockTools.createCoralBlockAxe(), BlockTools.createCoralBlockShovel(), BlockTools.createCoralBlockHoe()));
        sets.add(new ToolSetDisplay(Items.LANTERN, "Lantern",
            BlockTools.createLanternPickaxe(), BlockTools.createLanternAxe(), BlockTools.createLanternShovel(), BlockTools.createLanternHoe()));
        sets.add(new ToolSetDisplay(Items.IRON_BARS, "Chain",
            BlockTools.createChainPickaxe(), BlockTools.createChainAxe(), BlockTools.createChainShovel(), BlockTools.createChainHoe()));
        sets.add(new ToolSetDisplay(Items.LIGHTNING_ROD, "Lightning Rod",
            BlockTools.createLightningRodPickaxe(), BlockTools.createLightningRodAxe(), BlockTools.createLightningRodShovel(), BlockTools.createLightningRodHoe()));
        sets.add(new ToolSetDisplay(Items.TINTED_GLASS, "Tinted Glass",
            BlockTools.createTintedGlassPickaxe(), BlockTools.createTintedGlassAxe(), BlockTools.createTintedGlassShovel(), BlockTools.createTintedGlassHoe()));
        sets.add(new ToolSetDisplay(Items.CANDLE, "Candle",
            BlockTools.createCandlePickaxe(), BlockTools.createCandleAxe(), BlockTools.createCandleShovel(), BlockTools.createCandleHoe()));
        sets.add(new ToolSetDisplay(Items.SCULK_SENSOR, "Sculk Sensor",
            BlockTools.createSculkSensorPickaxe(), BlockTools.createSculkSensorAxe(), BlockTools.createSculkSensorShovel(), BlockTools.createSculkSensorHoe()));
        sets.add(new ToolSetDisplay(Items.SCULK_CATALYST, "Sculk Catalyst",
            BlockTools.createSculkCatalystPickaxe(), BlockTools.createSculkCatalystAxe(), BlockTools.createSculkCatalystShovel(), BlockTools.createSculkCatalystHoe()));
        sets.add(new ToolSetDisplay(Items.SCULK_SHRIEKER, "Sculk Shrieker",
            BlockTools.createSculkShriekerPickaxe(), BlockTools.createSculkShriekerAxe(), BlockTools.createSculkShriekerShovel(), BlockTools.createSculkShriekerHoe()));
        sets.add(new ToolSetDisplay(Items.BREWING_STAND, "Brewing Stand",
            BlockTools.createBrewingStandPickaxe(), BlockTools.createBrewingStandAxe(), BlockTools.createBrewingStandShovel(), BlockTools.createBrewingStandHoe()));
        sets.add(new ToolSetDisplay(Items.FURNACE, "Furnace",
            BlockTools.createFurnacePickaxe(), BlockTools.createFurnaceAxe(), BlockTools.createFurnaceShovel(), BlockTools.createFurnaceHoe()));

        // Pagination - 3 sets per page (each set takes 2 rows)
        int setsPerPage = 3;
        int totalPages = Math.max(1, (sets.size() + setsPerPage - 1) / setsPerPage);
        int clampedPage = Math.min(page, totalPages - 1);
        int startIdx = clampedPage * setsPerPage;
        int endIdx = Math.min(startIdx + setsPerPage, sets.size());

        // Display sets in organized rows
        for (int i = startIdx; i < endIdx; i++) {
            ToolSetDisplay set = sets.get(i);
            int rowOffset = (i - startIdx) * 18; // 2 rows per set
            int baseSlot = 10 + rowOffset;
            
            // Row 1: Block | Pickaxe | Axe | Shovel | Hoe
            // Row 2: Set Name separator
            
            // Block material (left side)
            gui.setSlot(baseSlot, new GuiElementBuilder(set.blockItem)
                    .setName(Component.literal(set.name + " Tools").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Material:").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(set.name).withStyle(ChatFormatting.WHITE))
                    .glow()
                    .build());
            
            // Tools (middle to right)
            gui.setSlot(baseSlot + 1, createToolElement(set.pickaxe, player));
            gui.setSlot(baseSlot + 2, createToolElement(set.axe, player));
            gui.setSlot(baseSlot + 3, createToolElement(set.shovel, player));
            gui.setSlot(baseSlot + 4, createToolElement(set.hoe, player));
            
            // Add separator lore below
            Component setName = Component.literal("━━ " + set.name + " Tools ━━").withStyle(ChatFormatting.DARK_GRAY);
            for (int sep = baseSlot + 9; sep < baseSlot + 14 && sep < 54; sep++) {
                gui.setSlot(sep, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                        .setName(setName)
                        .build());
            }
        }

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back to Menu").withStyle(ChatFormatting.YELLOW))
                .setCallback((slot, type, containerInput, slotGui) -> openMainMenu(player))
                .build());

        // Pagination
        if (clampedPage > 0) {
            final int prev = clampedPage - 1;
            gui.setSlot(46, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("← Previous Page").withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal("Page " + (clampedPage) + " / " + totalPages))
                    .setCallback((slot, type, containerInput, slotGui) -> openBlockToolsMenu(player, prev))
                    .build());
        }
        if (clampedPage < totalPages - 1) {
            final int next = clampedPage + 1;
            gui.setSlot(48, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Next Page →").withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal("Page " + (clampedPage + 2) + " / " + totalPages))
                    .setCallback((slot, type, containerInput, slotGui) -> openBlockToolsMenu(player, next))
                    .build());
        }

        gui.setSlot(50, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("Page " + (clampedPage + 1) + " / " + totalPages).withStyle(ChatFormatting.WHITE))
                .build());

        gui.open();
    }

    private static GuiElementBuilder createToolElement(ItemStack tool, ServerPlayer player) {
        Component name = tool.get(DataComponents.CUSTOM_NAME);
        return new GuiElementBuilder(tool)
                .setName(name)
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("✨ Click for recipe").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> showItemInfo(player, tool));
    }

    private static void openT2BountySwordsMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("⚔ T2 Bounty Swords"));

        RecipeGuiTheme.fillChest4Hub(gui);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.DIAMOND_SWORD)
                .setName(Component.literal("⚔ T2 Bounty Swords").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Tier 2 slayer swords").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Click any sword to view recipe").withStyle(ChatFormatting.GRAY))
                .glow().build());

        // Swords in a grid pattern
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23};
        int idx = 0;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            if (idx >= slots.length) break;
            ItemStack sword = SlayerItems.createUpgradedSlayerSword(type);
            gui.setSlot(slots[idx++], new GuiElementBuilder(sword)
                    .setName(sword.get(DataComponents.CUSTOM_NAME))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("✨ Click to view recipe").withStyle(ChatFormatting.YELLOW))
                    .setCallback((i, clicktype, containerInput, slotGui) -> showItemInfo(player, SlayerItems.createUpgradedSlayerSword(type)))
                    .build());
        }

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back to Menu").withStyle(ChatFormatting.YELLOW))
                .setCallback((slot, clicktype, containerInput, slotGui) -> openMainMenu(player))
                .build());

        gui.open();
    }

    private static void openT1ArmorMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("🛡 T1 Bounty Armor"));

        RecipeGuiTheme.fillChest4Hub(gui);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.IRON_CHESTPLATE)
                .setName(Component.literal("🛡 T1 Bounty Armor").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Select a slayer type below").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("to view the full armor set").withStyle(ChatFormatting.GRAY))
                .glow().build());

        // Slayer type buttons - row 2 and 3
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23};
        int idx = 0;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            if (idx >= slots.length) break;
            gui.setSlot(slots[idx++], new GuiElementBuilder(type.icon)
                    .setName(Component.literal("§ " + type.displayName + " Set").withStyle(type.color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("▸ Helmet").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("▸ Chestplate").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("▸ Leggings").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("▸ Boots").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("✨ Click to browse").withStyle(ChatFormatting.YELLOW))
                    .setCallback((i, clicktype, containerInput, slotGui) -> openT1ArmorSetMenu(player, type))
                    .build());
        }

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back to Menu").withStyle(ChatFormatting.YELLOW))
                .setCallback((i, clicktype, containerInput, slotGui) -> openMainMenu(player))
                .build());

        gui.open();
    }

    private static void openT1ArmorSetMenu(ServerPlayer player, SlayerManager.SlayerType slayerType) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("🛡 " + slayerType.displayName + " T1 Set"));

        RecipeGuiTheme.fillChest3Hub(gui);

        // Header with slayer type info
        gui.setSlot(4, new GuiElementBuilder(slayerType.icon)
                .setName(Component.literal(slayerType.displayName + " T1 Set").withStyle(slayerType.color, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Armor + Weapon set").withStyle(ChatFormatting.GRAY))
                .glow().build());

        // Armor pieces in a row (slots 10-13)
        gui.setSlot(10, new GuiElementBuilder(SlayerItems.createSlayerHelmet(slayerType, 1))
                .setName(SlayerItems.createSlayerHelmet(slayerType, 1).get(DataComponents.CUSTOM_NAME))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("✨ Click to view recipe").withStyle(ChatFormatting.YELLOW))
                .setCallback((i, clicktype, containerInput, slotGui) -> showItemInfo(player, SlayerItems.createSlayerHelmet(slayerType, 1)))
                .build());

        gui.setSlot(11, new GuiElementBuilder(SlayerItems.createSlayerChestplate(slayerType, 1))
                .setName(SlayerItems.createSlayerChestplate(slayerType, 1).get(DataComponents.CUSTOM_NAME))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("✨ Click to view recipe").withStyle(ChatFormatting.YELLOW))
                .setCallback((i, clicktype, containerInput, slotGui) -> showItemInfo(player, SlayerItems.createSlayerChestplate(slayerType, 1)))
                .build());

        gui.setSlot(12, new GuiElementBuilder(SlayerItems.createSlayerLeggings(slayerType, 1))
                .setName(SlayerItems.createSlayerLeggings(slayerType, 1).get(DataComponents.CUSTOM_NAME))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("✨ Click to view recipe").withStyle(ChatFormatting.YELLOW))
                .setCallback((i, clicktype, containerInput, slotGui) -> showItemInfo(player, SlayerItems.createSlayerLeggings(slayerType, 1)))
                .build());

        gui.setSlot(13, new GuiElementBuilder(SlayerItems.createSlayerBoots(slayerType, 1))
                .setName(SlayerItems.createSlayerBoots(slayerType, 1).get(DataComponents.CUSTOM_NAME))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("✨ Click to view recipe").withStyle(ChatFormatting.YELLOW))
                .setCallback((i, clicktype, containerInput, slotGui) -> showItemInfo(player, SlayerItems.createSlayerBoots(slayerType, 1)))
                .build());

        // Sword counterpart (slot 14)
        ItemStack sword = SlayerItems.createSlayerSword(slayerType);
        gui.setSlot(14, new GuiElementBuilder(sword)
                .setName(sword.get(DataComponents.CUSTOM_NAME))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("✨ Click to view recipe").withStyle(ChatFormatting.YELLOW))
                .setCallback((i, clicktype, containerInput, slotGui) -> showItemInfo(player, SlayerItems.createSlayerSword(slayerType)))
                .build());

        // Back button
        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back to T1 Armor").withStyle(ChatFormatting.YELLOW))
                .setCallback((i, clicktype, containerInput, slotGui) -> openT1ArmorMenu(player))
                .build());

        gui.open();
    }

    private static void openT2ArmorMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("🛡 T2 Bounty Armor"));

        RecipeGuiTheme.fillChest4Hub(gui);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.DIAMOND_CHESTPLATE)
                .setName(Component.literal("🛡 T2 Bounty Armor").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Select a slayer type below").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("to view the full armor set").withStyle(ChatFormatting.GRAY))
                .glow().build());

        // Slayer type buttons - row 2 and 3
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23};
        int idx = 0;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            if (idx >= slots.length) break;
            gui.setSlot(slots[idx++], new GuiElementBuilder(type.icon)
                    .setName(Component.literal("§ " + type.displayName + " Set").withStyle(type.color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("▸ Helmet").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("▸ Chestplate").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("▸ Leggings").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("▸ Boots").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("✨ Click to browse").withStyle(ChatFormatting.YELLOW))
                    .setCallback((i, clicktype, containerInput, slotGui) -> openT2ArmorSetMenu(player, type))
                    .build());
        }

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back to Menu").withStyle(ChatFormatting.YELLOW))
                .setCallback((i, clicktype, containerInput, slotGui) -> openMainMenu(player))
                .build());

        gui.open();
    }

    private static void openT2ArmorSetMenu(ServerPlayer player, SlayerManager.SlayerType slayerType) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("🛡 " + slayerType.displayName + " T2 Set"));

        RecipeGuiTheme.fillChest3Hub(gui);

        // Header with slayer type info
        gui.setSlot(4, new GuiElementBuilder(slayerType.icon)
                .setName(Component.literal(slayerType.displayName + " T2 Set").withStyle(slayerType.color, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Armor + Weapon set").withStyle(ChatFormatting.GRAY))
                .glow().build());

        // Armor pieces in a row (slots 10-13)
        gui.setSlot(10, new GuiElementBuilder(SlayerItems.createSlayerHelmet(slayerType, 2))
                .setName(SlayerItems.createSlayerHelmet(slayerType, 2).get(DataComponents.CUSTOM_NAME))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("✨ Click to view recipe").withStyle(ChatFormatting.YELLOW))
                .setCallback((i, clicktype, containerInput, slotGui) -> showItemInfo(player, SlayerItems.createSlayerHelmet(slayerType, 2)))
                .build());

        gui.setSlot(11, new GuiElementBuilder(SlayerItems.createSlayerChestplate(slayerType, 2))
                .setName(SlayerItems.createSlayerChestplate(slayerType, 2).get(DataComponents.CUSTOM_NAME))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("✨ Click to view recipe").withStyle(ChatFormatting.YELLOW))
                .setCallback((i, clicktype, containerInput, slotGui) -> showItemInfo(player, SlayerItems.createSlayerChestplate(slayerType, 2)))
                .build());

        gui.setSlot(12, new GuiElementBuilder(SlayerItems.createSlayerLeggings(slayerType, 2))
                .setName(SlayerItems.createSlayerLeggings(slayerType, 2).get(DataComponents.CUSTOM_NAME))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("✨ Click to view recipe").withStyle(ChatFormatting.YELLOW))
                .setCallback((i, clicktype, containerInput, slotGui) -> showItemInfo(player, SlayerItems.createSlayerLeggings(slayerType, 2)))
                .build());

        gui.setSlot(13, new GuiElementBuilder(SlayerItems.createSlayerBoots(slayerType, 2))
                .setName(SlayerItems.createSlayerBoots(slayerType, 2).get(DataComponents.CUSTOM_NAME))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("✨ Click to view recipe").withStyle(ChatFormatting.YELLOW))
                .setCallback((i, clicktype, containerInput, slotGui) -> showItemInfo(player, SlayerItems.createSlayerBoots(slayerType, 2)))
                .build());

        // Sword counterpart (slot 14)
        ItemStack sword = SlayerItems.createUpgradedSlayerSword(slayerType);
        gui.setSlot(14, new GuiElementBuilder(sword)
                .setName(sword.get(DataComponents.CUSTOM_NAME))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("✨ Click to view recipe").withStyle(ChatFormatting.YELLOW))
                .setCallback((i, clicktype, containerInput, slotGui) -> showItemInfo(player, SlayerItems.createUpgradedSlayerSword(slayerType)))
                .build());

        // Back button
        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back to T2 Armor").withStyle(ChatFormatting.YELLOW))
                .setCallback((i, clicktype, containerInput, slotGui) -> openT2ArmorMenu(player))
                .build());

        gui.open();
    }

    private static void openSpecialItemsMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("🎁 Special Items"));

        RecipeGuiTheme.fillChest4Hub(gui);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("🎁 Special Items").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Unique and legendary items").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Click any item to view recipe").withStyle(ChatFormatting.GRAY))
                .glow().build());

        // Add special items in a grid
        ItemStack[] specialItems = {
            SlayerItems.createMidasSword(),
            SlayerItems.createCrownOfGreed(),
            SlayerItems.createCrownOfMidas(),
            SlayerItems.createVoidwalkerCrown(),
            CustomItemHandler.createHermesShoes(),
            CustomItemHandler.createTheGavel(),
            CustomItemHandler.createHarveysStick(),
            DanielsPickaxe.create()
        };

        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23};
        int idx = 0;
        for (ItemStack item : specialItems) {
            if (idx >= slots.length) break;
            gui.setSlot(slots[idx++], new GuiElementBuilder(CustomItemLore.forSelectPageIcon(item))
                    .setName(item.get(DataComponents.CUSTOM_NAME))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("✨ Click to view recipe").withStyle(ChatFormatting.YELLOW))
                    .setCallback((slot, type2, containerInput, slotGui) -> showItemInfo(player, item))
                    .build());
        }

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back to Menu").withStyle(ChatFormatting.YELLOW))
                .setCallback((slot, type, containerInput, slotGui) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ============================================================
    // CUSTOM TOOLS MENU - 15 Legendary Tool Sets
    // ============================================================
    private static void openCustomToolsMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("⛏ Legendary Tools"));

        RecipeGuiTheme.fillDoubleChestCatalog(gui);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.NETHERITE_PICKAXE)
                .setName(Component.literal("⛏ Legendary Tools").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("15 unique tool sets with abilities").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Pickaxe → Axe → Shovel → Hoe").withStyle(ChatFormatting.YELLOW))
                .glow().build());

        // Define tool sets: Block/Icon, Name, Color, Tools
        List<ToolSetDisplay> sets = new ArrayList<>();
        
        // Set 1: Dragon
        sets.add(new ToolSetDisplay(Items.DRAGON_HEAD, "Dragon", ChatFormatting.DARK_RED,
            LegendaryTools.createDragonPickaxe(), LegendaryTools.createDragonAxe(),
            LegendaryTools.createDragonShovel(), LegendaryTools.createDragonHoe()));
        // Set 2: Aether
        sets.add(new ToolSetDisplay(Items.ELYTRA, "Aether", ChatFormatting.AQUA,
            LegendaryTools.createAetherPickaxe(), LegendaryTools.createAetherAxe(),
            LegendaryTools.createAetherShovel(), LegendaryTools.createAetherHoe()));
        // Set 3: Void
        sets.add(new ToolSetDisplay(Items.ENDER_PEARL, "Void", ChatFormatting.DARK_PURPLE,
            LegendaryTools.createVoidPickaxe(), LegendaryTools.createVoidAxe(),
            LegendaryTools.createVoidShovel(), LegendaryTools.createVoidHoe()));
        // Set 4: Nature
        sets.add(new ToolSetDisplay(Items.OAK_SAPLING, "Nature", ChatFormatting.GREEN,
            LegendaryTools.createNaturePickaxe(), LegendaryTools.createNatureAxe(),
            LegendaryTools.createNatureShovel(), LegendaryTools.createNatureHoe()));
        // Set 5: Frost
        sets.add(new ToolSetDisplay(Items.SNOW_BLOCK, "Frost", ChatFormatting.BLUE,
            LegendaryTools.createFrostPickaxe(), LegendaryTools.createFrostAxe(),
            LegendaryTools.createFrostShovel(), LegendaryTools.createFrostHoe()));
        // Set 6: Thunder
        sets.add(new ToolSetDisplay(Items.LIGHTNING_ROD, "Thunder", ChatFormatting.YELLOW,
            LegendaryTools.createThunderPickaxe(), LegendaryTools.createThunderAxe(),
            LegendaryTools.createThunderShovel(), LegendaryTools.createThunderHoe()));
        // Set 7: Ocean
        sets.add(new ToolSetDisplay(Items.HEART_OF_THE_SEA, "Ocean", ChatFormatting.DARK_BLUE,
            LegendaryTools.createOceanPickaxe(), LegendaryTools.createOceanAxe(),
            LegendaryTools.createOceanShovel(), LegendaryTools.createOceanHoe()));
        // Set 8: Lunar
        sets.add(new ToolSetDisplay(Items.PHANTOM_MEMBRANE, "Lunar", ChatFormatting.LIGHT_PURPLE,
            LegendaryTools.createLunarPickaxe(), LegendaryTools.createLunarAxe(),
            LegendaryTools.createLunarShovel(), LegendaryTools.createLunarHoe()));
        // Set 9: Solar
        sets.add(new ToolSetDisplay(Items.FIRE_CHARGE, "Solar", ChatFormatting.GOLD,
            LegendaryTools.createSolarPickaxe(), LegendaryTools.createSolarAxe(),
            LegendaryTools.createSolarShovel(), LegendaryTools.createSolarHoe()));
        // Set 10: Terra
        sets.add(new ToolSetDisplay(Items.GRASS_BLOCK, "Terra", ChatFormatting.DARK_GREEN,
            LegendaryTools.createTerraPickaxe(), LegendaryTools.createTerraAxe(),
            LegendaryTools.createTerraShovel(), LegendaryTools.createTerraHoe()));
        // Set 11: Phantom
        sets.add(new ToolSetDisplay(Items.SPECTRAL_ARROW, "Phantom", ChatFormatting.GRAY,
            LegendaryTools.createPhantomPickaxe(), LegendaryTools.createPhantomAxe(),
            LegendaryTools.createPhantomShovel(), LegendaryTools.createPhantomHoe()));
        // Set 12: Blood
        sets.add(new ToolSetDisplay(Items.NETHER_WART, "Blood", ChatFormatting.DARK_RED,
            LegendaryTools.createBloodPickaxe(), LegendaryTools.createBloodAxe(),
            LegendaryTools.createBloodShovel(), LegendaryTools.createBloodHoe()));
        // Set 13: Celestial
        sets.add(new ToolSetDisplay(Items.NETHER_STAR, "Celestial", ChatFormatting.DARK_AQUA,
            LegendaryTools.createCelestialPickaxe(), LegendaryTools.createCelestialAxe(),
            LegendaryTools.createCelestialShovel(), LegendaryTools.createCelestialHoe()));
        // Set 14: Shadow
        sets.add(new ToolSetDisplay(Items.BLACK_DYE, "Shadow", ChatFormatting.BLACK,
            LegendaryTools.createShadowPickaxe(), LegendaryTools.createShadowAxe(),
            LegendaryTools.createShadowShovel(), LegendaryTools.createShadowHoe()));
        // Set 15: Crystal
        sets.add(new ToolSetDisplay(Items.DIAMOND, "Crystal", ChatFormatting.WHITE,
            LegendaryTools.createCrystalPickaxe(), LegendaryTools.createCrystalAxe(),
            LegendaryTools.createCrystalShovel(), LegendaryTools.createCrystalHoe()));

        // Display sets in rows (3 sets per page, each set takes 2 rows)
        int setsPerPage = 3;
        int totalPages = Math.max(1, (sets.size() + setsPerPage - 1) / setsPerPage);
        int clampedPage = 0;
        int startIdx = 0;
        int endIdx = Math.min(setsPerPage, sets.size());

        for (int i = startIdx; i < endIdx; i++) {
            ToolSetDisplay set = sets.get(i);
            int rowOffset = (i - startIdx) * 18; // 2 rows per set
            int baseSlot = 10 + rowOffset;
            
            // Row 1: Icon | Pickaxe | Axe | Shovel | Hoe
            gui.setSlot(baseSlot, new GuiElementBuilder(set.blockItem)
                    .setName(Component.literal(set.name + " Set").withStyle(set.color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Material:").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(set.name).withStyle(set.color))
                    .glow()
                    .build());
            
            // Gap slot
            gui.setSlot(baseSlot + 1, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
            
            // Tools
            gui.setSlot(baseSlot + 2, createToolElement(set.pickaxe, player));
            gui.setSlot(baseSlot + 3, createToolElement(set.axe, player));
            gui.setSlot(baseSlot + 4, createToolElement(set.shovel, player));
            gui.setSlot(baseSlot + 5, createToolElement(set.hoe, player));
            
            // Separator row
            Component separator = Component.literal("━━ " + set.name + " Tools ━━").withStyle(ChatFormatting.DARK_GRAY);
            for (int sep = 0; sep < 9; sep++) {
                int sepSlot = baseSlot + 9 + sep;
                if (sepSlot < 54) {
                    gui.setSlot(sepSlot, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                            .setName(separator)
                            .build());
                }
            }
        }

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back to Menu").withStyle(ChatFormatting.YELLOW))
                .setCallback((slot, type, containerInput, slotGui) -> openMainMenu(player))
                .build());

        if (totalPages > 1) {
            gui.setSlot(50, new GuiElementBuilder(Items.PAPER)
                    .setName(Component.literal("Page 1 / " + totalPages).withStyle(ChatFormatting.WHITE))
                    .build());
        }

        gui.open();
    }

    // ============================================================
    // SPECIAL ARMOR MENU - Legendary & special armor pieces
    // ============================================================
    private static void openSpecialArmorMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("🛡 Special Armor"));

        RecipeGuiTheme.fillChest4Hub(gui);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.NETHERITE_HELMET)
                .setName(Component.literal("🛡 Special Armor").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Legendary & special armor pieces").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Click any item to view recipe").withStyle(ChatFormatting.GRAY))
                .glow().build());

        // Add special armor pieces in a grid
        ItemStack[] specialArmor = {
            SlayerItems.createZombieBerserkerHelmet(),
            SlayerItems.createSpiderLeggings(),
            SlayerItems.createSkeletonBow(),
            SlayerItems.createSlimeBoots(),
            SlayerItems.createWardenChestplate(),
            SlayerItems.createVoidwalkerCrown(),
            SlayerItems.createEnderPhaseHelmet()
        };

        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23};
        int idx = 0;
        for (ItemStack item : specialArmor) {
            if (idx >= slots.length) break;
            gui.setSlot(slots[idx++], new GuiElementBuilder(CustomItemLore.forSelectPageIcon(item))
                    .setName(item.get(DataComponents.CUSTOM_NAME))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("✨ Click to view recipe").withStyle(ChatFormatting.YELLOW))
                    .setCallback((slot, type2, containerInput, slotGui) -> showItemInfo(player, item))
                    .build());
        }

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back to Menu").withStyle(ChatFormatting.YELLOW))
                .setCallback((slot, type, containerInput, slotGui) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ============================================================
    // GOLD ARMOR EVOLUTION MENU - All gold armor tiers
    // ============================================================
    private static void openGoldArmorMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("👑 Gold Armor Evolution"));

        RecipeGuiTheme.fillChest4Hub(gui);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.GOLDEN_CHESTPLATE)
                .setName(Component.literal("👑 Gold Armor Evolution").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Pure → Polished → Shiny → Glistening → Gilded").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Click any item to view recipe").withStyle(ChatFormatting.GRAY))
                .glow().build());

        // Add gold armor tiers in a row
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23};
        ItemStack[] goldArmor = {
            SlayerItems.createPureGoldHelmet(),
            SlayerItems.createPureGoldChestplate(),
            SlayerItems.createPureGoldLeggings(),
            SlayerItems.createPureGoldBoots(),
            SlayerItems.createPolishedGoldHelmet(),
            SlayerItems.createPolishedGoldChestplate(),
            SlayerItems.createPolishedGoldLeggings(),
            SlayerItems.createPolishedGoldBoots(),
            SlayerItems.createShinyGoldHelmet(),
            SlayerItems.createShinyGoldChestplate(),
            SlayerItems.createShinyGoldLeggings(),
            SlayerItems.createShinyGoldBoots()
        };

        int idx = 0;
        for (ItemStack item : goldArmor) {
            if (idx >= slots.length) break;
            gui.setSlot(slots[idx++], new GuiElementBuilder(CustomItemLore.forSelectPageIcon(item))
                    .setName(item.get(DataComponents.CUSTOM_NAME))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("✨ Click to view recipe").withStyle(ChatFormatting.YELLOW))
                    .setCallback((slot, type2, containerInput, slotGui) -> showItemInfo(player, item))
                    .build());
        }

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back to Menu").withStyle(ChatFormatting.YELLOW))
                .setCallback((slot, type, containerInput, slotGui) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ============================================================
    // ATTRIBUTE TOKENS MENU - Armor attribute tokens
    // ============================================================
    private static void openAttributeTokensMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("✧ Attribute Tokens"));

        RecipeGuiTheme.fillChest4Hub(gui);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("✧ Attribute Tokens").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Armor attribute tokens for crafting").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Click any token to view recipe").withStyle(ChatFormatting.GRAY))
                .glow().build());

        // Add all attribute tokens in a grid
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23};
        int idx = 0;
        for (ArmourAttribute attr : ArmourAttribute.values()) {
            if (idx >= slots.length) break;
            
            ItemStack token = SlayerItems.createAttributeToken(attr);
            gui.setSlot(slots[idx++], new GuiElementBuilder(token)
                    .setName(token.get(DataComponents.CUSTOM_NAME))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("✨ Click to view recipe").withStyle(ChatFormatting.YELLOW))
                    .setCallback((slot, type2, containerInput, slotGui) -> showItemInfo(player, token))
                    .build());
        }

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back to Menu").withStyle(ChatFormatting.YELLOW))
                .setCallback((slot, type, containerInput, slotGui) -> openMainMenu(player))
                .build());

        gui.open();
    }

    private static void openPiglinItemsMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("👑 Piglin Items"));

        RecipeGuiTheme.fillChest3Hub(gui);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Component.literal("👑 Piglin Items").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Piglin-themed items").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Click any item to view recipe").withStyle(ChatFormatting.GRAY))
                .glow().build());

        // Add piglin items in a row
        ItemStack[] piglinItems = {
            SlayerItems.createPiglinCore(),
            SlayerItems.createPiglinFlesh(),
            SlayerItems.createCrownOfGreed(),
            SlayerItems.createCrownOfMidas(),
            SlayerItems.createMidasSword()
        };

        int[] slots = {10, 11, 12, 13, 14};
        int idx = 0;
        for (ItemStack item : piglinItems) {
            if (idx >= slots.length) break;
            gui.setSlot(slots[idx++], new GuiElementBuilder(CustomItemLore.forSelectPageIcon(item))
                    .setName(item.get(DataComponents.CUSTOM_NAME))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("✨ Click to view recipe").withStyle(ChatFormatting.YELLOW))
                    .setCallback((slot, type2, containerInput, slotGui) -> showItemInfo(player, item))
                    .build());
        }

        // Back button
        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back to Menu").withStyle(ChatFormatting.YELLOW))
                .setCallback((slot, type, containerInput, slotGui) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ============================================================
    // WEAPON ATTRIBUTES MENU - Recent addition
    // ============================================================
    private static void openWeaponAttributesMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("⚔ Weapon Attributes"));

        RecipeGuiTheme.fillChest4Hub(gui);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("⚔ Weapon Attributes").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Weapon attribute tokens").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Click any token to view recipe").withStyle(ChatFormatting.GRAY))
                .glow().build());

        // Add all weapon attribute tokens in a grid
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23};
        int idx = 0;
        for (WeaponAttribute attr : WeaponAttribute.values()) {
            if (idx >= slots.length) break;
            
            ItemStack token = SlayerItems.createWeaponAttributeToken(attr);
            gui.setSlot(slots[idx++], new GuiElementBuilder(token)
                    .setName(token.get(DataComponents.CUSTOM_NAME))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("✨ Click to view recipe").withStyle(ChatFormatting.YELLOW))
                    .setCallback((slot, type2, containerInput, slotGui) -> showItemInfo(player, token))
                    .build());
        }

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back to Menu").withStyle(ChatFormatting.YELLOW))
                .setCallback((slot, type, containerInput, slotGui) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ============================================================
    // HELPER: Show item information with actual recipe
    // ============================================================
    private static void showItemInfo(ServerPlayer player, ItemStack resultItem) {
        showItemInfo(player, resultItem, 0);
    }

    private static void showItemInfo(ServerPlayer player, ItemStack resultItem, int variantIndex) {
        java.util.List<SlayerRecipes.Recipe> allRecipes = RecipeOutputIndex.listRecipesForOutput(resultItem);
        int idx = allRecipes.isEmpty() ? 0 : Math.floorMod(variantIndex, allRecipes.size());
        SlayerRecipes.Recipe recipe = allRecipes.isEmpty() ? null : allRecipes.get(idx);

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Recipe View"));

        RecipeGuiTheme.fillDoubleChestRecipeFrame(gui);

        Component displayName = resultItem.get(DataComponents.CUSTOM_NAME);
        if (displayName == null) {
            displayName = Component.literal(resultItem.getItem().toString());
        }
        GuiElementBuilder head = new GuiElementBuilder(resultItem.copy())
                .setName(displayName)
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Crafting Recipe").withStyle(ChatFormatting.GRAY));
        if (allRecipes.size() > 1) {
            head.addLoreLine(Component.literal("Variant " + (idx + 1) + " / " + allRecipes.size()).withStyle(ChatFormatting.AQUA));
            if (recipe != null) {
                head.addLoreLine(Component.literal(recipe.name).withStyle(ChatFormatting.DARK_GRAY));
            }
        }
        gui.setSlot(4, head.glow().build());

        if (allRecipes.size() > 1) {
            gui.setSlot(1, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Prev recipe").withStyle(ChatFormatting.YELLOW))
                    .setCallback((slot, type, containerInput, slotGui) -> showItemInfo(player, resultItem, idx - 1))
                    .build());
            gui.setSlot(7, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Next recipe").withStyle(ChatFormatting.YELLOW))
                    .setCallback((slot, type, containerInput, slotGui) -> showItemInfo(player, resultItem, idx + 1))
                    .build());
        }

        int[] gridSlots = {19, 20, 21, 28, 29, 30, 37, 38, 39};

        if (recipe != null && recipe.ingredients != null) {
            for (int i = 0; i < 9 && i < recipe.ingredients.length; i++) {
                ItemStack input = recipe.ingredients[i];
                if (input != null && !input.isEmpty()) {
                    ItemStack display = input.copy();
                    Component inputName = display.get(DataComponents.CUSTOM_NAME);
                    if (inputName == null) {
                        inputName = Component.literal(display.getItem().toString());
                    }
                    gui.setSlot(gridSlots[i], new GuiElementBuilder(display)
                            .setName(inputName)
                            .build());
                } else {
                    RecipeGuiTheme.setRecipeEmptyGridSlot(gui, gridSlots[i]);
                }
            }

            gui.setSlot(32, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("→").withStyle(ChatFormatting.YELLOW))
                    .build());

            gui.setSlot(33, new GuiElementBuilder(resultItem.copy())
                    .setName(displayName)
                    .glow().build());
        } else {
            gui.setSlot(31, new GuiElementBuilder(Items.BARRIER)
                    .setName(Component.literal("No recipe found").withStyle(ChatFormatting.RED))
                    .addLoreLine(Component.literal("This item may be obtained"))
                    .addLoreLine(Component.literal("through other means."))
                    .build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back to Menu").withStyle(ChatFormatting.YELLOW))
                .setCallback((slot, type, containerInput, slotGui) -> openMainMenu(player))
                .build());

        if (recipe != null && recipe.ingredients != null) {
            SlayerRecipes.Recipe recipeForCraft = recipe;
            gui.setSlot(31, new GuiElementBuilder(Items.CRAFTING_TABLE)
                    .setName(Component.literal("⚡ Autocraft").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to craft if you have materials").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("Materials will be taken from inventory").withStyle(ChatFormatting.DARK_GRAY))
                    .setCallback((slot, type, containerInput, slotGui) -> tryAutocraft(player, recipeForCraft, resultItem, idx))
                    .build());
        }

        gui.open();
    }

    // ============================================================
    // HELPER: Try to autocraft an item from player inventory
    // ============================================================
    private static void tryAutocraft(ServerPlayer player, SlayerRecipes.Recipe recipe, ItemStack result, int variantIndex) {
        if (recipe == null || recipe.ingredients == null) {
            player.sendSystemMessage(Component.literal("§cNo recipe available for autocrafting."), false);
            return;
        }

        // Enforce the same requiredSlayer/requiredLevel gate the grid-crafting path uses,
        // otherwise /recipe → Autocraft bypasses level requirements entirely.
        if (!BountyCraftingHandler.meetsRecipeGate(player, recipe)) {
            String reqType = recipe.requiredSlayer != null
                    ? recipe.requiredSlayer.name().toLowerCase() : "slayer";
            player.sendSystemMessage(Component.literal(
                    "§cYou need " + reqType + " slayer level " + recipe.requiredLevel
                    + " to craft this."), false);
            return;
        }

        // Check if player has all required ingredients
        java.util.Map<String, Integer> required = new java.util.HashMap<>();
        for (ItemStack ing : recipe.ingredients) {
            if (ing == null || ing.isEmpty()) continue;
            String key = getIngredientKey(ing);
            required.merge(key, ing.getCount(), Integer::sum);
        }

        // Count available items in player inventory
        java.util.Map<String, Integer> available = new java.util.HashMap<>();
        java.util.Map<String, java.util.List<Integer>> slotMap = new java.util.HashMap<>();
        
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            String key = getIngredientKey(stack);
            available.merge(key, stack.getCount(), Integer::sum);
            slotMap.computeIfAbsent(key, k -> new java.util.ArrayList<>()).add(i);
        }

        // Check if all requirements are met
        for (java.util.Map.Entry<String, Integer> req : required.entrySet()) {
            int have = available.getOrDefault(req.getKey(), 0);
            if (have < req.getValue()) {
                player.sendSystemMessage(Component.literal("§cMissing materials for autocrafting!"), false);
                showItemInfo(player, result, variantIndex);
                return;
            }
        }

        // Consume ingredients
        for (java.util.Map.Entry<String, Integer> req : required.entrySet()) {
            int toRemove = req.getValue();
            java.util.List<Integer> slots = slotMap.get(req.getKey());
            if (slots == null) continue;
            
            for (int slot : slots) {
                if (toRemove <= 0) break;
                ItemStack stack = player.getInventory().getItem(slot);
                int canRemove = Math.min(toRemove, stack.getCount());
                stack.shrink(canRemove);
                toRemove -= canRemove;
            }
        }

        // Give result to player
        ItemStack crafted = result.copy();
        if (!player.addItem(crafted)) {
            player.drop(crafted, false);
        }

        player.sendSystemMessage(Component.literal("§a§l✓ Autocrafted successfully!"), false);
        showItemInfo(player, result, variantIndex);
    }

    /** Returns a stable string key for a given ItemStack for recipe matching. */
    private static String getIngredientKey(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return "";
        // Prefer custom_item_id for mod items
        String customId = SlayerItems.getCustomItemId(stack);
        if (customId != null) return "political:" + customId;
        // Use vanilla registry name
        return ItemRegistryIds.asString(stack.getItem());
    }

    // ============================================================
    // HELPER: Find recipe for output item (checks custom recipes too)
    // ============================================================
    private static SlayerRecipes.Recipe findRecipeForOutput(ItemStack output) {
        if (output == null || output.isEmpty()) return null;
        
        String outputId = SlayerItems.getCustomItemId(output);
        
        // Check all recipes (hardcoded + custom)
        for (SlayerRecipes.Recipe recipe : RecipeConfigManager.getRecipes()) {
            if (recipe.result == null || recipe.result.isEmpty()) continue;
            
            // Try to match by custom item ID first
            if (outputId != null) {
                String recipeId = SlayerItems.getCustomItemId(recipe.result);
                if (outputId.equals(recipeId)) {
                    // Check if recipe has actual ingredients (not blank)
                    if (hasValidIngredients(recipe)) {
                        return recipe;
                    }
                }
            }
            
            // Fallback: match by item type and custom name
            if (recipe.result.getItem() == output.getItem()) {
                Component recipeName = recipe.result.get(DataComponents.CUSTOM_NAME);
                Component outputName = output.get(DataComponents.CUSTOM_NAME);
                if (recipeName != null && outputName != null && recipeName.equals(outputName)) {
                    if (hasValidIngredients(recipe)) {
                        return recipe;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Checks if a recipe has at least one non-empty ingredient.
     * Returns false if the recipe is blank (no ingredients or all empty).
     */
    private static boolean hasValidIngredients(SlayerRecipes.Recipe recipe) {
        if (recipe.ingredients == null) return false;
        for (ItemStack ing : recipe.ingredients) {
            if (ing != null && !ing.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    // ============================================================
    // SEARCH GUI - Sign-based search with partial matching
    // ============================================================
    private static void openSearchGui(ServerPlayer player) {
        eu.pb4.sgui.api.gui.SignGui signGui = new eu.pb4.sgui.api.gui.SignGui(player) {
            @Override
            public void onManualClose() {
                String searchTerm = this.getLine(0).getString().trim();
                if (!searchTerm.isEmpty()) {
                    showSearchResults(player, searchTerm);
                } else {
                    openMainMenu(player);
                }
            }
        };
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal("^^^^^^^^^^^^^^").withStyle(ChatFormatting.YELLOW));
        signGui.setLine(2, Component.literal("Enter item name").withStyle(ChatFormatting.AQUA));
        signGui.setLine(3, Component.literal("(partial ok)").withStyle(ChatFormatting.GRAY));
        signGui.open();
    }
    
    private static void showSearchResults(ServerPlayer player, String searchTerm) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("🔍 Search: " + searchTerm));

        RecipeGuiTheme.fillDoubleChestCatalog(gui);
        
        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.SPYGLASS)
                .setName(Component.literal("🔍 Search Results").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Search: \"" + searchTerm + "\"").withStyle(ChatFormatting.YELLOW))
                .glow().build());
        
        // Search through all recipes
        java.util.List<ItemStack> results = new java.util.ArrayList<>();
        String searchLower = searchTerm.toLowerCase();
        
        // Check all recipes
        for (SlayerRecipes.Recipe recipe : RecipeConfigManager.getRecipes()) {
            if (recipe.result != null && !recipe.result.isEmpty()) {
                Component nameText = recipe.result.get(DataComponents.CUSTOM_NAME);
                String itemName = nameText != null ? nameText.getString().toLowerCase() : 
                                  recipe.result.getItem().toString().toLowerCase();
                
                if (itemName.contains(searchLower)) {
                    results.add(recipe.result);
                }
            }
        }
        
        // Display results
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 
                       28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
        int slotIdx = 0;
        
        for (ItemStack result : results) {
            if (slotIdx >= slots.length) break;
            
            Component name = result.get(DataComponents.CUSTOM_NAME);
            if (name == null) name = Component.literal(result.getItem().toString());
            
            gui.setSlot(slots[slotIdx++], new GuiElementBuilder(result)
                    .setName(name)
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("✨ Click for recipe").withStyle(ChatFormatting.YELLOW))
                    .setCallback((idx, type, containerInput, slotGui) -> showItemInfo(player, result))
                    .build());
        }
        
        if (results.isEmpty()) {
            gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                    .setName(Component.literal("No results found").withStyle(ChatFormatting.RED))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Try a different search term").withStyle(ChatFormatting.GRAY))
                    .build());
        }
        
        // Back and search again buttons
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back to Menu").withStyle(ChatFormatting.YELLOW))
                .setCallback((slot, type, containerInput, slotGui) -> openMainMenu(player))
                .build());
        
        gui.setSlot(46, new GuiElementBuilder(Items.SPYGLASS)
                .setName(Component.literal("🔍 Search Again").withStyle(ChatFormatting.AQUA))
                .setCallback((slot, type, containerInput, slotGui) -> openSearchGui(player))
                .build());
        
        gui.open();
    }

    // ============================================================
    // ALL RECIPES INDEX - Paginated view of every JSON recipe
    // ============================================================
    public static void openAllRecipesPage(ServerPlayer player, int page) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);

        List<SlayerRecipes.Recipe> all = new ArrayList<>();
        for (SlayerRecipes.Recipe recipe : RecipeConfigManager.getRecipes()) {
            if (recipe == null || recipe.result == null || recipe.result.isEmpty()) continue;
            if (!hasValidIngredients(recipe)) continue;
            all.add(recipe);
        }

        all.sort((a, b) -> {
            String aId = SlayerItems.getCustomItemId(a.result);
            String bId = SlayerItems.getCustomItemId(b.result);
            if (aId == null) aId = a.name;
            if (bId == null) bId = b.name;
            return aId.compareToIgnoreCase(bId);
        });

        final int perPage = 45;
        final int totalPages = Math.max(1, (all.size() + perPage - 1) / perPage);
        final int safePage = Math.max(0, Math.min(page, totalPages - 1));
        final int start = safePage * perPage;
        final int end = Math.min(start + perPage, all.size());

        gui.setTitle(Component.literal("📚 All Recipes (" + all.size() + ") - Page " + (safePage + 1) + "/" + totalPages));

        RecipeGuiTheme.fillDoubleChestCatalog(gui);

        int slot = 0;
        for (int i = start; i < end; i++) {
            SlayerRecipes.Recipe recipe = all.get(i);
            ItemStack result = recipe.result.copy();

            Component name = result.get(DataComponents.CUSTOM_NAME);
            if (name == null) {
                String id = SlayerItems.getCustomItemId(result);
                name = Component.literal(id != null ? id : recipe.name);
            }

            gui.setSlot(slot++, new GuiElementBuilder(result)
                    .setName(name)
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("✨ Click for recipe").withStyle(ChatFormatting.YELLOW))
                    .setCallback((idx, type, containerInput, slotGui) -> showItemInfo(player, result))
                    .build());
        }

        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back to Menu").withStyle(ChatFormatting.YELLOW))
                .setCallback((s, t, a, __slotGui) -> openMainMenu(player))
                .build());

        if (safePage > 0) {
            gui.setSlot(48, new GuiElementBuilder(Items.SPECTRAL_ARROW)
                    .setName(Component.literal("◀ Previous Page").withStyle(ChatFormatting.AQUA))
                    .setCallback((s, t, a, __slotGui) -> openAllRecipesPage(player, safePage - 1))
                    .build());
        }

        if (safePage < totalPages - 1) {
            gui.setSlot(50, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Next Page ▶").withStyle(ChatFormatting.AQUA))
                    .setCallback((s, t, a, __slotGui) -> openAllRecipesPage(player, safePage + 1))
                    .build());
        }

        gui.setSlot(53, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("✖ Close").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .setCallback((s, t, a, __slotGui) -> gui.close())
                .build());

        gui.open();
    }

    // ============================================================
    // SPECIFIC RECIPE LOOKUP - Search by item name
    // ============================================================
    public static boolean openSpecificRecipe(ServerPlayer player, String itemName) {
        String searchLower = itemName.toLowerCase().replace(" ", "_");
        
        // Search through all recipes for a match
        for (SlayerRecipes.Recipe recipe : RecipeConfigManager.getRecipes()) {
            if (recipe.result == null || recipe.result.isEmpty()) continue;
            
            Component nameText = recipe.result.get(DataComponents.CUSTOM_NAME);
            String recipeItemName = nameText != null ? nameText.getString().toLowerCase().replace(" ", "_") : 
                                    recipe.result.getItem().toString().toLowerCase();
            
            // Check for match by name or custom item ID
            String customId = SlayerItems.getCustomItemId(recipe.result);
            boolean matches = recipeItemName.contains(searchLower) || searchLower.contains(recipeItemName) ||
                              (customId != null && customId.toLowerCase().contains(searchLower));
            
            if (matches && hasValidIngredients(recipe)) {
                showItemInfo(player, recipe.result);
                return true;
            }
        }
        
        // No recipe found - open main menu with message
        player.sendSystemMessage(Component.literal("✖ No recipe found for: " + itemName).withStyle(ChatFormatting.RED), false);
        player.sendSystemMessage(Component.literal("Use /recipe to open the recipe browser").withStyle(ChatFormatting.GRAY), false);
        openMainMenu(player);
        return false;
    }
}
