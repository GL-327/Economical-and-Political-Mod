package com.political.recipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.util.Prediction;
import com.political.ArmourAttribute;
import com.political.CustomItemLore;
import com.political.CustomItemsGui;
import com.political.DanielsPickaxe;
import com.political.ItemRegistryIds;
import com.political.RecipeGuiTheme;
import com.political.RecipeOutputIndex;
import com.political.WeaponAttribute;


import com.political.bounty.SlayerItems;
import com.political.bounty.SlayerManager;
import com.political.items.CustomItemHandler;
import com.political.items.GemGear;
import com.political.items.LegendaryTools;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(RecipesGui.class);


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
    /** The bounty sets list; a recipe opened from it goes Back to this list, not to the Special Armor category that shares its stack entry. */
    private static void openBountySets(ServerPlayer player) {
        CustomItemsGui.openBountySetsPage(player, 0,
                (p, item) -> CustomItemsGui.showItemInfo(p, item, RecipesGui::openBountySets),
                RecipesGui::openMainMenu);
    }

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
                .setCallback((idx, type, containerInput, slotGui) -> { pushCategory(CustomItemsGui.Category.SPECIAL_ARMOR); openBountySets(player); })
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
            LOGGER.error("Failed to build the recipe main menu for player {}", player.getUUID(), e);
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Error: " + e.getMessage()).withStyle(ChatFormatting.RED), false);
        }
    }

    // Helper class for bounty set display
    public static class BountySetDisplay {
        public final SlayerManager.SlayerType type;
        public final net.minecraft.world.item.Item icon;
        public final String name;
        public final ChatFormatting color;
        public final ItemStack t1Sword, t1Helmet, t1Chestplate, t1Leggings, t1Boots;
        public final ItemStack t2Sword, t2Helmet, t2Chestplate, t2Leggings, t2Boots;
        
        public BountySetDisplay(SlayerManager.SlayerType type, net.minecraft.world.item.Item icon, String name, ChatFormatting color,
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
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.black())
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

        // 36+ NEW BLOCK ARMOR SETS

        // ===== NEW ARMOR SETS =====
        // Stripped Log Armor
        // Plank Armor
        // Stone/Mud/Log Armor
        // Copper/Netherite/Froglight/Basalt Armor
        // Material Item Armor
        // NEW 25 MATERIAL ARMOR SETS

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
            gui.setSlot(baseSlot + 1, new GuiElementBuilder(Items.STAINED_GLASS_PANE.black())
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
                    gui.setSlot(sep, new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray())
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

        // 36+ NEW BLOCK TOOL SETS
        // NEW 25 MATERIAL TOOL SETS

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
                gui.setSlot(sep, new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray())
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
                .addLoreLine(Component.literal("Four legendary tool sets with abilities").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Pickaxe → Axe → Shovel → Hoe").withStyle(ChatFormatting.YELLOW))
                .glow().build());

        // Define tool sets: Block/Icon, Name, Color, Tools
        List<ToolSetDisplay> sets = new ArrayList<>();
        
        // Set 1: Dragon
        sets.add(new ToolSetDisplay(Items.DRAGON_HEAD, "Dragon", ChatFormatting.DARK_RED,
            LegendaryTools.createDragonPickaxe(), LegendaryTools.createDragonAxe(),
            LegendaryTools.createDragonShovel(), LegendaryTools.createDragonHoe()));
        // Set 2: Aether
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
        // Set 7: Ocean
        // Set 8: Lunar
        // Set 9: Solar
        // Set 10: Terra
        // Set 11: Phantom
        // Set 12: Blood
        // Set 13: Celestial
        // Set 14: Shadow
        // Set 15: Crystal

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
            gui.setSlot(baseSlot + 1, new GuiElementBuilder(Items.STAINED_GLASS_PANE.black())
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
                    gui.setSlot(sepSlot, new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray())
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
            player.drop(crafted, false, Prediction.SERVER_ONLY);
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
