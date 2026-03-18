package com.political;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI for the /setrecipe admin command.
 *
 * Navigation hierarchy:
 *   Main Menu (category list)
 *     → Category Page (all items in that category)
 *       → Recipe Editor (3×3 grid editor, save/clear, ← Back)
 */
public class RecipeEditorGui {

    // ── Recipe-editor layout constants (9×6 chest) ──────────────
    private static final int[] GRID_SLOTS   = {10, 11, 12, 19, 20, 21, 28, 29, 30};
    private static final int SLOT_ARROW     = 23;
    private static final int SLOT_RESULT    = 24;
    private static final int SLOT_SAVE      = 33;
    private static final int SLOT_CLEAR     = 34;
    private static final int SLOT_LOCK      = 35;
    private static final int SLOT_BACK      = 45;
    private static final int SLOT_HEADER    = 4;

    // ── Category definitions ─────────────────────────────────────
    private enum Category {
        GEM_BLOCK_GEAR ("💎 Gem & Block Armor",         Items.EMERALD,            Formatting.GREEN),
        SWORDS_T1    ("⚔ Bounty Swords (T1)",               Items.IRON_SWORD,         Formatting.YELLOW),
        SWORDS_T2    ("⚔ Bounty Swords (T2)",               Items.DIAMOND_SWORD,      Formatting.AQUA),
        ARMOR_T1     ("🛡 Bounty Armor T1",                  Items.IRON_CHESTPLATE,    Formatting.GREEN),
        ARMOR_T2     ("🛡 Bounty Armor T2",                  Items.DIAMOND_CHESTPLATE, Formatting.DARK_AQUA),
        SPECIAL_ARMOR("🛡 Special / Legendary Armor",        Items.NETHERITE_HELMET,   Formatting.GOLD),
        HPEBM        ("⚡ HPEBM Weapons",                    Items.BLAZE_ROD,          Formatting.LIGHT_PURPLE),
        SPECIAL_ITEMS("🎁 Special Items",                    Items.STICK,              Formatting.GREEN),
        PIGLIN       ("👑 Piglin Items",                     Items.GOLDEN_HELMET,      Formatting.GOLD),
        GOLD_ARMOR   ("👑 Gold Armor Evolution",             Items.GOLDEN_CHESTPLATE,  Formatting.GOLD),
        MATERIALS    ("📦 Materials (Cores & Chunks)",       Items.SLIME_BALL,         Formatting.GRAY),
        COMPACTED    ("⬛ Compacted Materials",              Items.RAW_IRON,           Formatting.DARK_GRAY),
        SUPER_COMPACTED("⭐ Super Compacted",               Items.GOLD_BLOCK,         Formatting.GOLD),
        ENCHANTED_BLOCKS("✧ Enchanted Blocks",              Items.COBBLESTONE,        Formatting.LIGHT_PURPLE),
        ENCHANTED_CROPS("✧ Enchanted Crops & Flowers",      Items.WHEAT,              Formatting.GREEN),
        ATTRIBUTE_TOKENS("✧ Attribute Tokens",               Items.PAPER,              Formatting.LIGHT_PURPLE),
        WEAPON_ATTRIBUTES("⚔ Weapon Attributes",               Items.NETHER_STAR,        Formatting.RED),
        BLOCK_TOOLS   ("⛏ Block Tools",                     Items.DIAMOND_PICKAXE,    Formatting.AQUA);

        final String label;
        final net.minecraft.item.Item icon;
        final Formatting colour;

        Category(String label, net.minecraft.item.Item icon, Formatting colour) {
            this.label = label;
            this.icon  = icon;
            this.colour = colour;
        }
    }

    /** Returns all items belonging to a given category. */
    private static List<ItemStack> getItemsForCategory(Category cat) {
        List<ItemStack> items = new ArrayList<>();
        switch (cat) {
            case GEM_BLOCK_GEAR -> {
                // Emerald armor
                items.add(GemGear.createEmeraldHelmet());
                items.add(GemGear.createEmeraldChestplate());
                items.add(GemGear.createEmeraldLeggings());
                items.add(GemGear.createEmeraldBoots());
                // Emerald tools
                items.add(GemGear.createEmeraldSword());
                items.add(GemGear.createEmeraldPickaxe());
                items.add(GemGear.createEmeraldAxe());
                items.add(GemGear.createEmeraldShovel());
                items.add(GemGear.createEmeraldHoe());
                // Lapis armor
                items.add(GemGear.createLapisHelmet());
                items.add(GemGear.createLapisChestplate());
                items.add(GemGear.createLapisLeggings());
                items.add(GemGear.createLapisBoots());
                // Lapis tools
                items.add(GemGear.createLapisSword());
                items.add(GemGear.createLapisPickaxe());
                items.add(GemGear.createLapisAxe());
                items.add(GemGear.createLapisShovel());
                items.add(GemGear.createLapisHoe());
                // Block armors - Glass
                items.add(BlockArmor.createGlassHelmet());
                items.add(BlockArmor.createGlassChestplate());
                items.add(BlockArmor.createGlassLeggings());
                items.add(BlockArmor.createGlassBoots());
                // Block armors - Obsidian
                items.add(BlockArmor.createObsidianHelmet());
                items.add(BlockArmor.createObsidianChestplate());
                items.add(BlockArmor.createObsidianLeggings());
                items.add(BlockArmor.createObsidianBoots());
                // Block armors - Quartz
                items.add(BlockArmor.createQuartzHelmet());
                items.add(BlockArmor.createQuartzChestplate());
                items.add(BlockArmor.createQuartzLeggings());
                items.add(BlockArmor.createQuartzBoots());
                // Block armors - Glowstone
                items.add(BlockArmor.createGlowstoneHelmet());
                items.add(BlockArmor.createGlowstoneChestplate());
                items.add(BlockArmor.createGlowstoneLeggings());
                items.add(BlockArmor.createGlowstoneBoots());
                // Block armors - Redstone
                items.add(BlockArmor.createRedstoneHelmet());
                items.add(BlockArmor.createRedstoneChestplate());
                items.add(BlockArmor.createRedstoneLeggings());
                items.add(BlockArmor.createRedstoneBoots());
                // Block armors - Netherrack
                items.add(BlockArmor.createNetherrackHelmet());
                items.add(BlockArmor.createNetherrackChestplate());
                items.add(BlockArmor.createNetherrackLeggings());
                items.add(BlockArmor.createNetherrackBoots());
                // Block armors - End Stone
                items.add(BlockArmor.createEndstoneHelmet());
                items.add(BlockArmor.createEndstoneChestplate());
                items.add(BlockArmor.createEndstoneLeggings());
                items.add(BlockArmor.createEndstoneBoots());
                // Block armors - Ice
                items.add(BlockArmor.createIceHelmet());
                items.add(BlockArmor.createIceChestplate());
                items.add(BlockArmor.createIceLeggings());
                items.add(BlockArmor.createIceBoots());
                // Block armors - Prismarine
                items.add(BlockArmor.createPrismarineHelmet());
                items.add(BlockArmor.createPrismarineChestplate());
                items.add(BlockArmor.createPrismarineLeggings());
                items.add(BlockArmor.createPrismarineBoots());
                // Block armors - Terracotta
                items.add(BlockArmor.createTerracottaHelmet());
                items.add(BlockArmor.createTerracottaChestplate());
                items.add(BlockArmor.createTerracottaLeggings());
                items.add(BlockArmor.createTerracottaBoots());
            }
            case SWORDS_T1 -> {
                for (SlayerManager.SlayerType t : SlayerManager.SlayerType.values())
                    items.add(SlayerItems.createSlayerSword(t));
            }
            case SWORDS_T2 -> {
                for (SlayerManager.SlayerType t : SlayerManager.SlayerType.values())
                    items.add(SlayerItems.createUpgradedSlayerSword(t));
            }
            case ARMOR_T1 -> {
                for (SlayerManager.SlayerType t : SlayerManager.SlayerType.values()) {
                    items.add(SlayerItems.createSlayerHelmet(t, 1));
                    items.add(SlayerItems.createSlayerChestplate(t, 1));
                    items.add(SlayerItems.createSlayerLeggings(t, 1));
                    items.add(SlayerItems.createSlayerBoots(t, 1));
                }
            }
            case ARMOR_T2 -> {
                for (SlayerManager.SlayerType t : SlayerManager.SlayerType.values()) {
                    items.add(SlayerItems.createSlayerHelmet(t, 2));
                    items.add(SlayerItems.createSlayerChestplate(t, 2));
                    items.add(SlayerItems.createSlayerLeggings(t, 2));
                    items.add(SlayerItems.createSlayerBoots(t, 2));
                }
            }
            case SPECIAL_ARMOR -> {
                items.add(SlayerItems.createZombieBerserkerHelmet());
                items.add(SlayerItems.createSpiderLeggings());
                items.add(SlayerItems.createSkeletonBow());
                items.add(SlayerItems.createSlimeBoots());
                items.add(SlayerItems.createWardenChestplate());
                items.add(SlayerItems.createVoidwalkerCrown());
            }
            case HPEBM -> {
                for (int mk = 1; mk <= 5; mk++)
                    items.add(CustomItemHandler.createHPEBM(mk));
                items.add(CustomItemHandler.createUltraOverclockedBeam());
            }
            case SPECIAL_ITEMS -> {
                items.add(CustomItemHandler.createTheGavel());
                items.add(CustomItemHandler.createHarveysStick());
                items.add(CustomItemHandler.createHermesShoes());
                items.add(DanielsPickaxe.create());
                
                // NEW BOSS DROPS
                items.add(SlayerItems.createUndeadHeart());
                items.add(SlayerItems.createSpectralQuiver());
                items.add(SlayerItems.createEchoingCore());
                items.add(SlayerItems.createEnderSword());
                items.add(SlayerItems.createAbyssalBlade());
                items.add(SlayerItems.createBouncySlime());
                items.add(SlayerItems.createVenomousDagger());
            }
            case PIGLIN -> {
                items.add(SlayerItems.createCrownOfGreed());
                items.add(SlayerItems.createCrownOfMidas());
                items.add(SlayerItems.createMidasSword());
                items.add(SlayerItems.createPiglinCore());
                items.add(SlayerItems.createPiglinFlesh());
            }
            case GOLD_ARMOR -> {
                // Pure Gold Armor (Tier 1)
                items.add(SlayerItems.createPureGoldHelmet());
                items.add(SlayerItems.createPureGoldChestplate());
                items.add(SlayerItems.createPureGoldLeggings());
                items.add(SlayerItems.createPureGoldBoots());
                
                // Polished Gold Armor (Tier 2)
                items.add(SlayerItems.createPolishedGoldHelmet());
                items.add(SlayerItems.createPolishedGoldChestplate());
                items.add(SlayerItems.createPolishedGoldLeggings());
                items.add(SlayerItems.createPolishedGoldBoots());
                
                // Shiny Gold Armor (Tier 3)
                items.add(SlayerItems.createShinyGoldHelmet());
                items.add(SlayerItems.createShinyGoldChestplate());
                items.add(SlayerItems.createShinyGoldLeggings());
                items.add(SlayerItems.createShinyGoldBoots());
                
                // Glistening Gold Armor (Tier 4)
                items.add(SlayerItems.createGlisteningGoldHelmet());
                items.add(SlayerItems.createGlisteningGoldChestplate());
                items.add(SlayerItems.createGlisteningGoldLeggings());
                items.add(SlayerItems.createGlisteningGoldBoots());
                
                // Gilded Armor (Tier 5)
                items.add(SlayerItems.createGildedHelmet());
                items.add(SlayerItems.createGildedChestplate());
                items.add(SlayerItems.createGildedLeggings());
                items.add(SlayerItems.createGildedBoots());
                
                // Add enchanted blackstone for gilded armor crafting
                items.add(SlayerItems.createEnchantedBlackstone());
                items.add(SlayerItems.createEnchantedGildedBlackstone());
                
                // Add gilded netherite armor (ultimate tier)
                items.add(SlayerItems.createGildedNetheriteHelmet());
                items.add(SlayerItems.createGildedNetheriteChestplate());
                items.add(SlayerItems.createGildedNetheriteLeggings());
                items.add(SlayerItems.createGildedNetheriteBoots());
            }
            case MATERIALS -> {
                for (SlayerManager.SlayerType t : SlayerManager.SlayerType.values()) {
                    items.add(SlayerItems.createCore(t));
                    items.add(SlayerItems.createChunk(t));
                }
                // Add attribute cores
                for (com.political.ArmourAttribute attr : com.political.ArmourAttribute.values()) {
                    items.add(SlayerItems.createAttributeCore(attr));
                }
            }
            case COMPACTED -> {
                items.add(SlayerItems.createCompactedIron());
                items.add(SlayerItems.createCompactedGold());
                items.add(SlayerItems.createCompactedDiamond());
                items.add(SlayerItems.createCompactedEmerald());
                items.add(SlayerItems.createCompactedNetherite());
                items.add(SlayerItems.createCompactedCopper());
                items.add(SlayerItems.createCompactedCoal());
                items.add(SlayerItems.createCompactedLapis());
                items.add(SlayerItems.createCompactedRedstone());
                items.add(SlayerItems.createCompactedQuartz());
            }
            case SUPER_COMPACTED -> {
                items.add(SlayerItems.createSuperCompactedGold());
                items.add(SlayerItems.createSuperCompactedIron());
                items.add(SlayerItems.createSuperCompactedDiamond());
                items.add(SlayerItems.createSuperCompactedEmerald());
                items.add(SlayerItems.createSuperCompactedNetherite());
            }
            case ENCHANTED_BLOCKS -> {
                items.add(SlayerItems.createEnchantedCobblestone());
                items.add(SlayerItems.createEnchantedStone());
                items.add(SlayerItems.createEnchantedIronBlock());
                items.add(SlayerItems.createEnchantedGoldBlock());
                items.add(SlayerItems.createEnchantedDiamondBlock());
                items.add(SlayerItems.createEnchantedBlackstone());
                items.add(SlayerItems.createEnchantedGildedBlackstone());
                items.add(SlayerItems.createEnchantedOakLog());
            }
            case ENCHANTED_CROPS -> {
                // Enchanted flowers
                items.add(SlayerItems.createEnchantedRose());
                items.add(SlayerItems.createEnchantedDandelion());
                // Enchanted crops
                items.add(SlayerItems.createEnchantedWheat());
                items.add(SlayerItems.createEnchantedCarrot());
                items.add(SlayerItems.createEnchantedPotato());
                items.add(SlayerItems.createEnchantedSugarCane());
            }
            case ATTRIBUTE_TOKENS -> {
                // Add all 10 attribute tokens
                items.add(SlayerItems.createAttributeToken(com.political.ArmourAttribute.BURNING));
                items.add(SlayerItems.createAttributeToken(com.political.ArmourAttribute.SIGHTLESS));
                items.add(SlayerItems.createAttributeToken(com.political.ArmourAttribute.FRENZIED));
                items.add(SlayerItems.createAttributeToken(com.political.ArmourAttribute.GROUNDED));
                items.add(SlayerItems.createAttributeToken(com.political.ArmourAttribute.WEBBED));
                items.add(SlayerItems.createAttributeToken(com.political.ArmourAttribute.FROST));
                items.add(SlayerItems.createAttributeToken(com.political.ArmourAttribute.PHANTOMSTEP));
                items.add(SlayerItems.createAttributeToken(com.political.ArmourAttribute.CURSED));
                items.add(SlayerItems.createAttributeToken(com.political.ArmourAttribute.OVERGROWN));
                items.add(SlayerItems.createAttributeToken(com.political.ArmourAttribute.VOLATILE));
            }
            case WEAPON_ATTRIBUTES -> {
                // Add all weapon attribute tokens
                for (WeaponAttribute attr : WeaponAttribute.values()) {
                    items.add(SlayerItems.createWeaponAttributeToken(attr));
                }
            }
            case BLOCK_TOOLS -> {
                // Glass tools
                items.add(BlockTools.createGlassPickaxe());
                items.add(BlockTools.createGlassAxe());
                items.add(BlockTools.createGlassShovel());
                items.add(BlockTools.createGlassHoe());
                // Obsidian tools
                items.add(BlockTools.createObsidianPickaxe());
                items.add(BlockTools.createObsidianAxe());
                items.add(BlockTools.createObsidianShovel());
                items.add(BlockTools.createObsidianHoe());
                // Quartz tools
                items.add(BlockTools.createQuartzPickaxe());
                items.add(BlockTools.createQuartzAxe());
                items.add(BlockTools.createQuartzShovel());
                items.add(BlockTools.createQuartzHoe());
                // Glowstone tools
                items.add(BlockTools.createGlowstonePickaxe());
                items.add(BlockTools.createGlowstoneAxe());
                items.add(BlockTools.createGlowstoneShovel());
                items.add(BlockTools.createGlowstoneHoe());
                // Redstone tools
                items.add(BlockTools.createRedstonePickaxe());
                items.add(BlockTools.createRedstoneAxe());
                items.add(BlockTools.createRedstoneShovel());
                items.add(BlockTools.createRedstoneHoe());
                // Netherrack tools
                items.add(BlockTools.createNetherrackPickaxe());
                items.add(BlockTools.createNetherrackAxe());
                items.add(BlockTools.createNetherrackShovel());
                items.add(BlockTools.createNetherrackHoe());
                // End Stone tools
                items.add(BlockTools.createEndstonePickaxe());
                items.add(BlockTools.createEndstoneAxe());
                items.add(BlockTools.createEndstoneShovel());
                items.add(BlockTools.createEndstoneHoe());
                // Ice tools
                items.add(BlockTools.createIcePickaxe());
                items.add(BlockTools.createIceAxe());
                items.add(BlockTools.createIceShovel());
                items.add(BlockTools.createIceHoe());
                // Prismarine tools
                items.add(BlockTools.createPrismarinePickaxe());
                items.add(BlockTools.createPrismarineAxe());
                items.add(BlockTools.createPrismarineShovel());
                items.add(BlockTools.createPrismarineHoe());
                // Terracotta tools
                items.add(BlockTools.createTerracottaPickaxe());
                items.add(BlockTools.createTerracottaAxe());
                items.add(BlockTools.createTerracottaShovel());
                items.add(BlockTools.createTerracottaHoe());
                // Mossy tools
                items.add(BlockTools.createMossyPickaxe());
                items.add(BlockTools.createMossyAxe());
                items.add(BlockTools.createMossyShovel());
                items.add(BlockTools.createMossyHoe());
                // Soul Sand tools
                items.add(BlockTools.createSoulSandPickaxe());
                items.add(BlockTools.createSoulSandAxe());
                items.add(BlockTools.createSoulSandShovel());
                items.add(BlockTools.createSoulSandHoe());
                // Magma tools
                items.add(BlockTools.createMagmaPickaxe());
                items.add(BlockTools.createMagmaAxe());
                items.add(BlockTools.createMagmaShovel());
                items.add(BlockTools.createMagmaHoe());
                // Sandstone tools
                items.add(BlockTools.createSandstonePickaxe());
                items.add(BlockTools.createSandstoneAxe());
                items.add(BlockTools.createSandstoneShovel());
                items.add(BlockTools.createSandstoneHoe());
                // Amethyst tools
                items.add(BlockTools.createAmethystPickaxe());
                items.add(BlockTools.createAmethystAxe());
                items.add(BlockTools.createAmethystShovel());
                items.add(BlockTools.createAmethystHoe());
                // Coal tools
                items.add(BlockTools.createCoalPickaxe());
                items.add(BlockTools.createCoalAxe());
                items.add(BlockTools.createCoalShovel());
                items.add(BlockTools.createCoalHoe());
            }
        }
        return items;
    }


    // ──────────────────────────────────────────────────────────────
    // Page 0: Main Category Menu
    // ──────────────────────────────────────────────────────────────

    /** Entry point — opens the main category selection menu. */
    public static void openMainMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("§6§l/setrecipe — Choose Category"));

        // Background with dark theme
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(Text.literal("§6§l/setrecipe").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Select a category to view and edit recipes").formatted(Formatting.GRAY))
                .glow().build());

        // Place category buttons in a grid
        Category[] cats = Category.values();
        int[] catSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
        for (int i = 0; i < cats.length && i < catSlots.length; i++) {
            Category cat = cats[i];
            gui.setSlot(catSlots[i], new GuiElementBuilder(cat.icon)
                    .setName(Text.literal(cat.label).formatted(cat.colour, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✨ Click to browse items").formatted(Formatting.YELLOW))
                    .setCallback((slot, type, action) -> openCategoryPage(player, cat, 0))
                    .build());
        }

        gui.open();
    }

    // ──────────────────────────────────────────────────────────────
    // Page 1: Category Item List
    // ──────────────────────────────────────────────────────────────

    public static void openCategoryPage(ServerPlayerEntity player, Category cat, int page) {
        // Special handling for GEM_BLOCK_GEAR - armor sets in rows
        if (cat == Category.GEM_BLOCK_GEAR) {
            openGemBlockGearPage(player, page);
            return;
        }

        List<SlayerRecipes.Recipe> existingRecipes = RecipeConfigManager.getRecipes();
        List<ItemStack> items = getItemsForCategory(cat);

        final int ITEMS_PER_PAGE = 28;
        int totalPages = Math.max(1, (items.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE);
        int clampedPage = Math.min(page, totalPages - 1);

        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("§6" + cat.label));

        // Background with dark theme
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(cat.icon)
                .setName(Text.literal(cat.label).formatted(cat.colour, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click an item to edit its recipe").formatted(Formatting.GRAY))
                .glow().build());

        // Item slots (7 per row, rows 1-4, columns 1-7)
        int start = clampedPage * ITEMS_PER_PAGE;
        int shown = 0;
        for (int idx = start; idx < items.size() && shown < ITEMS_PER_PAGE; idx++) {
            ItemStack item = items.get(idx);
            int row = shown / 7;
            int col = shown % 7 + 1;
            int slot = row * 9 + col;

            Text displayName = item.get(DataComponentTypes.CUSTOM_NAME);
            String customId = SlayerItems.getCustomItemId(item);

            // Check for existing recipe
            boolean hasRecipe = false;
            SlayerRecipes.Recipe existingRecipe = null;
            if (customId != null) {
                for (SlayerRecipes.Recipe r : existingRecipes) {
                    if (customId.equals(SlayerItems.getCustomItemId(r.result))) {
                        hasRecipe = true;
                        existingRecipe = r;
                        break;
                    }
                }
            }

            final ItemStack finalItem = item;
            final SlayerRecipes.Recipe finalRecipe = existingRecipe;
            final String finalRecipeName = existingRecipe != null ? existingRecipe.name : null;
            final boolean finalHasRecipe = hasRecipe;
            final boolean recipeEnabled = finalRecipeName != null && RecipeConfigManager.isRecipeEnabled(finalRecipeName);

            GuiElementBuilder elem = new GuiElementBuilder(item.getItem())
                    .setName(displayName != null ? displayName
                            : Text.literal(customId != null ? customId : "Unknown"))
                    .addLoreLine(Text.literal(""));
            if (finalHasRecipe) {
                elem.addLoreLine(recipeEnabled
                        ? Text.literal("✔ Recipe ENABLED").formatted(Formatting.GREEN)
                        : Text.literal("✘ Recipe DISABLED").formatted(Formatting.RED));
                elem.addLoreLine(Text.literal("Left-click: edit  |  Right-click: toggle").formatted(Formatting.GRAY));
            } else {
                elem.addLoreLine(Text.literal("➕ No recipe — click to create").formatted(Formatting.YELLOW));
            }
            final int finalPage = clampedPage;
            elem.setCallback((s, t, a) -> {
                if (finalHasRecipe && t == eu.pb4.sgui.api.ClickType.MOUSE_RIGHT) {
                    boolean nowEnabled = RecipeConfigManager.toggleRecipe(finalRecipeName);
                    String status = nowEnabled ? "ENABLED" : "DISABLED";
                    player.sendMessage(Text.literal("Recipe '" + finalRecipeName + "' is now " + status).formatted(nowEnabled ? Formatting.GREEN : Formatting.RED), false);
                    openCategoryPage(player, cat, finalPage);
                } else {
                    openRecipeEditor(player, finalItem, finalRecipe, cat);
                }
            });
            gui.setSlot(slot, elem.build());
            shown++;
        }

        // ← Back to main menu
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Categories").formatted(Formatting.YELLOW))
                .setCallback((s, t, a) -> openMainMenu(player)).build());

        // Pagination
        if (clampedPage > 0) {
            final int prev = clampedPage - 1;
            gui.setSlot(46, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("← Previous Page").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("Page " + clampedPage + " / " + totalPages))
                    .setCallback((s, t, a) -> openCategoryPage(player, cat, prev)).build());
        }
        if (clampedPage < totalPages - 1) {
            final int next = clampedPage + 1;
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("Next Page →").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("Page " + (clampedPage + 2) + " / " + totalPages))
                    .setCallback((s, t, a) -> openCategoryPage(player, cat, next)).build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("Page " + (clampedPage + 1) + " / " + totalPages))
                .build());

        gui.open();
    }

    // ──────────────────────────────────────────────────────────────
    // GEM_BLOCK_GEAR: Armor Sets in Rows Display
    // Each row: [Block] [Sword] [Helmet] [Chest] [Legs] [Boots]
    // ──────────────────────────────────────────────────────────────

    private static void openGemBlockGearPage(ServerPlayerEntity player, int page) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("§6💎 Gem & Block Armor - Recipe Editor"));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.EMERALD)
                .setName(Text.literal("💎 Gem & Block Armor").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Armor sets displayed in rows:").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("[Block] [Sword] [Helmet] [Chest] [Legs] [Boots]").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Click items to edit recipes").formatted(Formatting.AQUA))
                .glow().build());

        // Define armor sets: Block Item, Sword, Armor pieces
        ArmorSetDisplay[] sets = {
            new ArmorSetDisplay(Items.GLASS, BlockWeapons.createGlassSword(),
                BlockArmor.createGlassHelmet(), BlockArmor.createGlassChestplate(),
                BlockArmor.createGlassLeggings(), BlockArmor.createGlassBoots()),
            new ArmorSetDisplay(Items.OBSIDIAN, BlockWeapons.createObsidianSword(),
                BlockArmor.createObsidianHelmet(), BlockArmor.createObsidianChestplate(),
                BlockArmor.createObsidianLeggings(), BlockArmor.createObsidianBoots()),
            new ArmorSetDisplay(Items.QUARTZ_BLOCK, BlockWeapons.createQuartzSword(),
                BlockArmor.createQuartzHelmet(), BlockArmor.createQuartzChestplate(),
                BlockArmor.createQuartzLeggings(), BlockArmor.createQuartzBoots()),
            new ArmorSetDisplay(Items.GLOWSTONE, BlockWeapons.createGlowstoneSword(),
                BlockArmor.createGlowstoneHelmet(), BlockArmor.createGlowstoneChestplate(),
                BlockArmor.createGlowstoneLeggings(), BlockArmor.createGlowstoneBoots()),
            new ArmorSetDisplay(Items.REDSTONE_BLOCK, BlockWeapons.createRedstoneSword(),
                BlockArmor.createRedstoneHelmet(), BlockArmor.createRedstoneChestplate(),
                BlockArmor.createRedstoneLeggings(), BlockArmor.createRedstoneBoots()),
            new ArmorSetDisplay(Items.NETHERRACK, BlockWeapons.createNetherrackSword(),
                BlockArmor.createNetherrackHelmet(), BlockArmor.createNetherrackChestplate(),
                BlockArmor.createNetherrackLeggings(), BlockArmor.createNetherrackBoots()),
            new ArmorSetDisplay(Items.END_STONE, BlockWeapons.createEndstoneSword(),
                BlockArmor.createEndstoneHelmet(), BlockArmor.createEndstoneChestplate(),
                BlockArmor.createEndstoneLeggings(), BlockArmor.createEndstoneBoots()),
            new ArmorSetDisplay(Items.PACKED_ICE, BlockWeapons.createIceSword(),
                BlockArmor.createIceHelmet(), BlockArmor.createIceChestplate(),
                BlockArmor.createIceLeggings(), BlockArmor.createIceBoots()),
            new ArmorSetDisplay(Items.PRISMARINE, BlockWeapons.createPrismarineSword(),
                BlockArmor.createPrismarineHelmet(), BlockArmor.createPrismarineChestplate(),
                BlockArmor.createPrismarineLeggings(), BlockArmor.createPrismarineBoots()),
            new ArmorSetDisplay(Items.TERRACOTTA, BlockWeapons.createTerracottaSword(),
                BlockArmor.createTerracottaHelmet(), BlockArmor.createTerracottaChestplate(),
                BlockArmor.createTerracottaLeggings(), BlockArmor.createTerracottaBoots()),
            new ArmorSetDisplay(Items.MOSSY_COBBLESTONE, BlockWeapons.createMossySword(),
                BlockArmor.createMossyHelmet(), BlockArmor.createMossyChestplate(),
                BlockArmor.createMossyLeggings(), BlockArmor.createMossyBoots()),
            new ArmorSetDisplay(Items.SOUL_SAND, BlockWeapons.createSoulSandSword(),
                BlockArmor.createSoulSandHelmet(), BlockArmor.createSoulSandChestplate(),
                BlockArmor.createSoulSandLeggings(), BlockArmor.createSoulSandBoots()),
            new ArmorSetDisplay(Items.MAGMA_BLOCK, BlockWeapons.createMagmaSword(),
                BlockArmor.createMagmaHelmet(), BlockArmor.createMagmaChestplate(),
                BlockArmor.createMagmaLeggings(), BlockArmor.createMagmaBoots()),
            new ArmorSetDisplay(Items.SANDSTONE, BlockWeapons.createSandstoneSword(),
                BlockArmor.createSandstoneHelmet(), BlockArmor.createSandstoneChestplate(),
                BlockArmor.createSandstoneLeggings(), BlockArmor.createSandstoneBoots()),
            new ArmorSetDisplay(Items.AMETHYST_BLOCK, BlockWeapons.createAmethystSword(),
                BlockArmor.createAmethystHelmet(), BlockArmor.createAmethystChestplate(),
                BlockArmor.createAmethystLeggings(), BlockArmor.createAmethystBoots()),
            new ArmorSetDisplay(Items.COAL_BLOCK, BlockWeapons.createCoalSword(),
                BlockArmor.createCoalHelmet(), BlockArmor.createCoalChestplate(),
                BlockArmor.createCoalLeggings(), BlockArmor.createCoalBoots()),
        };

        List<SlayerRecipes.Recipe> existingRecipes = RecipeConfigManager.getRecipes();

        final int SETS_PER_PAGE = 4;
        int totalPages = (sets.length + SETS_PER_PAGE - 1) / SETS_PER_PAGE;
        int clampedPage = Math.min(page, totalPages - 1);

        int startIdx = clampedPage * SETS_PER_PAGE;
        for (int i = 0; i < SETS_PER_PAGE && (startIdx + i) < sets.length; i++) {
            ArmorSetDisplay set = sets[startIdx + i];
            int row = i + 1; // Rows 1-4 (slots 9-44)

            // Row layout: [0:Block] [1:Sword] [2:Helmet] [3:Chest] [4:Legs] [5:Boots]
            int[] slots = { row * 9, row * 9 + 1, row * 9 + 2, row * 9 + 3, row * 9 + 4, row * 9 + 5 };

            // Block (decorative background)
            gui.setSlot(slots[0], new GuiElementBuilder(set.blockItem)
                    .setName(Text.literal("§8" + set.blockItem.toString().replace("_", " ")).formatted(Formatting.DARK_GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Armor Set Material").formatted(Formatting.GRAY))
                    .build());

            // Items: Sword + 4 Armor pieces
            ItemStack[] items = { set.sword, set.helmet, set.chestplate, set.leggings, set.boots };
            for (int j = 0; j < 5; j++) {
                ItemStack item = items[j];
                Text itemName = item.get(DataComponentTypes.CUSTOM_NAME);
                String customId = SlayerItems.getCustomItemId(item);

                // Check for existing recipe
                boolean hasRecipe = false;
                SlayerRecipes.Recipe existingRecipe = null;
                if (customId != null) {
                    for (SlayerRecipes.Recipe r : existingRecipes) {
                        if (customId.equals(SlayerItems.getCustomItemId(r.result))) {
                            hasRecipe = true;
                            existingRecipe = r;
                            break;
                        }
                    }
                }

                final ItemStack finalItem = item;
                final SlayerRecipes.Recipe finalRecipe = existingRecipe;
                final String finalRecipeName = existingRecipe != null ? existingRecipe.name : null;
                final boolean finalHasRecipe = hasRecipe;
                final boolean recipeEnabled = finalRecipeName != null && RecipeConfigManager.isRecipeEnabled(finalRecipeName);

                GuiElementBuilder elem = new GuiElementBuilder(item.getItem())
                        .setName(itemName != null ? itemName : Text.literal(customId != null ? customId : "Unknown"))
                        .addLoreLine(Text.literal(""));
                if (finalHasRecipe) {
                    elem.addLoreLine(recipeEnabled
                            ? Text.literal("✔ Recipe ENABLED").formatted(Formatting.GREEN)
                            : Text.literal("✘ Recipe DISABLED").formatted(Formatting.RED));
                    elem.addLoreLine(Text.literal("Left: edit  |  Right: toggle").formatted(Formatting.GRAY));
                } else {
                    elem.addLoreLine(Text.literal("➕ No recipe — click to create").formatted(Formatting.YELLOW));
                }
                final int finalPage = clampedPage;
                elem.setCallback((s, t, a) -> {
                    if (finalHasRecipe && t == eu.pb4.sgui.api.ClickType.MOUSE_RIGHT) {
                        boolean nowEnabled = RecipeConfigManager.toggleRecipe(finalRecipeName);
                        String status = nowEnabled ? "ENABLED" : "DISABLED";
                        player.sendMessage(Text.literal("Recipe '" + finalRecipeName + "' is now " + status).formatted(nowEnabled ? Formatting.GREEN : Formatting.RED), false);
                        openGemBlockGearPage(player, finalPage);
                    } else {
                        openRecipeEditor(player, finalItem, finalRecipe, Category.GEM_BLOCK_GEAR);
                    }
                });
                gui.setSlot(slots[1 + j], elem.build());
            }
        }

        // ← Back to main menu
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Categories").formatted(Formatting.YELLOW))
                .setCallback((s, t, a) -> openMainMenu(player)).build());

        // Pagination
        if (clampedPage > 0) {
            final int prev = clampedPage - 1;
            gui.setSlot(46, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("← Previous Page").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("Page " + (clampedPage + 1) + " / " + totalPages))
                    .setCallback((s, t, a) -> openGemBlockGearPage(player, prev)).build());
        }
        if (clampedPage < totalPages - 1) {
            final int next = clampedPage + 1;
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("Next Page →").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("Page " + (clampedPage + 2) + " / " + totalPages))
                    .setCallback((s, t, a) -> openGemBlockGearPage(player, next)).build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("Page " + (clampedPage + 1) + " / " + totalPages))
                .build());

        gui.open();
    }

    // Helper class for armor set display
    private record ArmorSetDisplay(Item blockItem, ItemStack sword, ItemStack helmet,
                                    ItemStack chestplate, ItemStack leggings, ItemStack boots) {}

    // ──────────────────────────────────────────────────────────────
    // Page 2: Recipe Editor  (unchanged core logic)
    // ──────────────────────────────────────────────────────────────

    /**
     * Opens the editable recipe grid for the given result item.
     *
     * @param player     the admin
     * @param resultItem the item the recipe produces
     * @param existing   an existing recipe to pre-populate (may be null)
     * @param backCat    the category to return to when ← Back is clicked (may be null → opens main menu)
     */
    public static void openRecipeEditor(ServerPlayerEntity player, ItemStack resultItem,
                                         SlayerRecipes.Recipe existing, Category backCat) {
        ItemStack[] grid = new ItemStack[9];
        boolean[] lockedSlots = new boolean[9]; // Track which slots are locked
        for (int i = 0; i < 9; i++) {
            grid[i] = (existing != null && existing.ingredients != null && existing.ingredients[i] != null)
                    ? existing.ingredients[i].copy() : ItemStack.EMPTY;
            lockedSlots[i] = false; // Start with all slots unlocked
        }

        String resultCustomId = SlayerItems.getCustomItemId(resultItem);

        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false) {
            @Override
            public boolean onAnyClick(int index, ClickType type, SlotActionType action) {
                for (int g = 0; g < 9; g++) {
                    if (index == GRID_SLOTS[g]) {
                        // Handle middle-click to toggle lock
                        if (type == ClickType.MOUSE_MIDDLE) {
                            lockedSlots[g] = !lockedSlots[g];
                            String status = lockedSlots[g] ? "§aLOCKED" : "§eUNLOCKED";
                            player.sendMessage(Text.literal("Slot " + (g + 1) + " is now " + status), false);
                            refreshGrid(this, grid, resultItem, lockedSlots);
                            return true;
                        }
                        
                        // Prevent modification if slot is locked
                        if (lockedSlots[g]) {
                            player.sendMessage(Text.literal("§cThis slot is locked! Use middle-click to unlock."), false);
                            return true;
                        }
                        
                        ItemStack cursor = player.currentScreenHandler.getCursorStack();
                        if (!cursor.isEmpty()) {
                            // Allow full count for compacted materials and other items
                            grid[g] = cursor.copy();
                        } else {
                            grid[g] = ItemStack.EMPTY;
                        }
                        refreshGrid(this, grid, resultItem, lockedSlots);
                        return true;
                    }
                }
                if (index == SLOT_SAVE) {
                    saveRecipe(player, resultItem, grid, lockedSlots);
                    player.closeHandledScreen();
                    return true;
                }
                if (index == SLOT_CLEAR) {
                    for (int g = 0; g < 9; g++) {
                        if (!lockedSlots[g]) { // Only clear unlocked slots
                            grid[g] = ItemStack.EMPTY;
                        }
                    }
                    refreshGrid(this, grid, resultItem, lockedSlots);
                    return true;
                }
                if (index == SLOT_LOCK) {
                    // Toggle all slots lock state
                    boolean allLocked = true;
                    for (boolean locked : lockedSlots) {
                        if (!locked) {
                            allLocked = false;
                            break;
                        }
                    }
                    
                    // If all are locked, unlock all. Otherwise, lock all.
                    for (int g = 0; g < 9; g++) {
                        lockedSlots[g] = !allLocked;
                    }
                    
                    String status = allLocked ? "§eAll slots UNLOCKED" : "§aAll slots LOCKED";
                    player.sendMessage(Text.literal(status), false);
                    refreshGrid(this, grid, resultItem, lockedSlots);
                    return true;
                }
                if (index == SLOT_BACK) {
                    player.closeHandledScreen();
                    if (backCat != null) openCategoryPage(player, backCat, 0);
                    else openMainMenu(player);
                    return true;
                }
                return super.onAnyClick(index, type, action);
            }
        };

        Text resultName = resultItem.get(DataComponentTypes.CUSTOM_NAME);
        gui.setTitle(Text.literal("§6Edit Recipe: §e")
                .append(resultName != null ? resultName
                        : Text.literal(resultCustomId != null ? resultCustomId : "?")));

        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        gui.setSlot(SLOT_HEADER, new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Text.literal("§6Recipe Editor"))
                .addLoreLine(Text.literal("§7Hold an item and left-click a grid slot to set it"))
                .addLoreLine(Text.literal("§7Left-click empty cursor on a slot to clear it"))
                .build());

        gui.setSlot(SLOT_ARROW, new GuiElementBuilder(Items.SPECTRAL_ARROW)
                .setName(Text.literal("→").formatted(Formatting.YELLOW)).build());

        GuiElementBuilder res = new GuiElementBuilder(resultItem.getItem())
                .setName(resultName != null ? resultName
                        : Text.literal(resultCustomId != null ? resultCustomId : "?"))
                .glow();
        gui.setSlot(SLOT_RESULT, res.build());

        gui.setSlot(SLOT_SAVE, new GuiElementBuilder(Items.LIME_DYE)
                .setName(Text.literal("§a§lSave Recipe"))
                .addLoreLine(Text.literal("§7Click to save this recipe."))
                .glow().build());

        gui.setSlot(SLOT_CLEAR, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("§c§lClear Grid"))
                .addLoreLine(Text.literal("§7Click to clear all ingredient slots."))
                .addLoreLine(Text.literal("§7Locked slots will not be cleared."))
                .build());

        gui.setSlot(SLOT_LOCK, new GuiElementBuilder(Items.TRIPWIRE_HOOK)
                .setName(Text.literal("§6§lToggle All Locks"))
                .addLoreLine(Text.literal("§7Click to lock/unlock all slots."))
                .addLoreLine(Text.literal("§7Middle-click individual slots to toggle."))
                .build());

        gui.setSlot(SLOT_BACK, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(backCat != null ? "§7Return to " + backCat.label : "§7Return to main menu"))
                .build());

        // Initial grid display
        refreshGrid(gui, grid, resultItem, lockedSlots);
        gui.open();
    }

    /** Convenience overload — back goes to main menu. */
    public static void openRecipeEditor(ServerPlayerEntity player, ItemStack resultItem,
                                         SlayerRecipes.Recipe existing) {
        openRecipeEditor(player, resultItem, existing, null);
    }

    /** Re-draws the 3×3 grid slots. */
    private static void refreshGrid(SimpleGui gui, ItemStack[] grid, ItemStack resultItem, boolean[] lockedSlots) {
        for (int g = 0; g < 9; g++) {
            ItemStack ing = grid[g];
            boolean isLocked = lockedSlots != null && lockedSlots[g];
            
            if (ing == null || ing.isEmpty()) {
                GuiElementBuilder builder = new GuiElementBuilder(
                    isLocked ? Items.RED_STAINED_GLASS_PANE : Items.LIGHT_GRAY_STAINED_GLASS_PANE)
                        .setName(Text.literal(isLocked ? "§c[Locked — middle-click to unlock]" : "§8[Empty — click with item to set]"));
                
                if (isLocked) {
                    builder.addLoreLine(Text.literal("§7Slot is locked"))
                            .addLoreLine(Text.literal("§7Middle-click to unlock"));
                } else {
                    builder.addLoreLine(Text.literal("§7Middle-click to lock slot"));
                }
                
                gui.setSlot(GRID_SLOTS[g], builder.build());
            } else {
                Text ingName = ing.get(DataComponentTypes.CUSTOM_NAME);
                GuiElementBuilder builder = new GuiElementBuilder(ing.getItem())
                        .setName(ingName != null ? ingName : Text.of(ing.getItem().toString()));
                
                // Add lock status to lore
                if (isLocked) {
                    builder.addLoreLine(Text.literal("§c§lLOCKED SLOT"));
                    builder.addLoreLine(Text.literal("§7Middle-click to unlock"));
                } else {
                    builder.addLoreLine(Text.literal("§7Click with item to replace, empty-hand to clear"));
                    builder.addLoreLine(Text.literal("§7Middle-click to lock slot"));
                }
                
                // Show count if more than 1
                if (ing.getCount() > 1) {
                    builder.setCount(ing.getCount());
                    builder.addLoreLine(Text.literal("§7Amount: " + ing.getCount()).formatted(Formatting.YELLOW));
                }
                
                // Add red dye overlay for locked slots
                if (isLocked) {
                    builder.glow();
                }
                
                gui.setSlot(GRID_SLOTS[g], builder.build());
            }
        }
    }

    /** Saves the recipe to RecipeConfigManager. */
    private static void saveRecipe(ServerPlayerEntity player, ItemStack resultItem, ItemStack[] grid, boolean[] lockedSlots) {
        Text nameTxt = resultItem.get(DataComponentTypes.CUSTOM_NAME);
        String recipeName = nameTxt != null ? nameTxt.getString() : "Custom Recipe";

        String resultId = SlayerItems.getCustomItemId(resultItem);
        RecipeConfigManager.removeCustomRecipe(recipeName);
        if (resultId != null) {
            for (SlayerRecipes.Recipe existing : new ArrayList<>(RecipeConfigManager.getCustomRecipes())) {
                if (resultId.equals(SlayerItems.getCustomItemId(existing.result))) {
                    RecipeConfigManager.removeCustomRecipe(existing.name);
                }
            }
        }

        ItemStack[] ingredientsCopy = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            ingredientsCopy[i] = (grid[i] == null || grid[i].isEmpty()) ? ItemStack.EMPTY : grid[i].copy();
        }

        SlayerRecipes.Recipe newRecipe = new SlayerRecipes.Recipe(
                recipeName, resultItem.copy(), ingredientsCopy, lockedSlots, null, 0);
        RecipeConfigManager.addCustomRecipe(newRecipe);
        player.sendMessage(Text.literal("§a✔ Recipe saved for §e" + recipeName + "§a!"), false);
    }

    // ──────────────────────────────────────────────────────────────
    // Legacy public entry-points (backwards compatibility)
    // ──────────────────────────────────────────────────────────────

    /** Opens the main category menu. Called by /setrecipe command. */
    public static void openRecipeList(ServerPlayerEntity player) {
        openMainMenu(player);
    }

    /** Retained for backward-compat — now opens the main menu (page is ignored). */
    public static void openRecipeList(ServerPlayerEntity player, int page) {
        openMainMenu(player);
    }

    // ──────────────────────────────────────────────────────────────
    // Legacy: read-only recipe view (still used by /recipes GUI)
    // ──────────────────────────────────────────────────────────────

    public static void openRecipeView(ServerPlayerEntity player, SlayerRecipes.Recipe recipe) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X5, player, false);
        gui.setTitle(Text.literal("§6Recipe: §e" + recipe.name));

        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 45; i++) gui.setSlot(i, bg.build());

        Text resultName = recipe.result.get(DataComponentTypes.CUSTOM_NAME);
        gui.setSlot(SLOT_HEADER, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(resultName != null ? resultName
                        : Text.literal(recipe.name).formatted(Formatting.GOLD))
                .glow().build());

        int[] viewGrid = {10, 11, 12, 19, 20, 21, 28, 29, 30};
        for (int i = 0; i < 9; i++) {
            ItemStack ing = (recipe.ingredients != null && i < recipe.ingredients.length)
                    ? recipe.ingredients[i] : null;
            if (ing != null && !ing.isEmpty()) {
                GuiElementBuilder eb = new GuiElementBuilder(ing.getItem());
                Text ingName = ing.get(DataComponentTypes.CUSTOM_NAME);
                if (ingName != null) eb.setName(ingName);
                if (ing.getCount() > 1) eb.setCount(ing.getCount());
                gui.setSlot(viewGrid[i], eb.build());
            }
        }

        gui.setSlot(23, new GuiElementBuilder(Items.SPECTRAL_ARROW)
                .setName(Text.literal("→").formatted(Formatting.YELLOW)).build());
        if (!recipe.result.isEmpty()) {
            GuiElementBuilder res = new GuiElementBuilder(recipe.result.getItem());
            Text rName = recipe.result.get(DataComponentTypes.CUSTOM_NAME);
            if (rName != null) res.setName(rName);
            res.glow();
            gui.setSlot(24, res.build());
        }

        String slayerName = recipe.requiredSlayer != null ? recipe.requiredSlayer.displayName : "Any";
        gui.setSlot(34, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("§eRecipe Info"))
                .addLoreLine(Text.literal("§7Type: §f" + slayerName))
                .addLoreLine(Text.literal("§7Required Level: §f" + recipe.requiredLevel)).build());

        gui.setSlot(36, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player)).build());

        gui.open();
    }
}
