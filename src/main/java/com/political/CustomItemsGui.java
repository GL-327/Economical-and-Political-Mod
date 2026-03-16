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

import java.util.ArrayList;
import java.util.List;

/**
 * GUI for the /customitems admin command.
 * Shows ALL custom items organized by categories for easy access.
 */
public class CustomItemsGui {

    // ── Category definitions ─────────────────────────────────────
    private enum Category {
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
        ATTRIBUTE_TOKENS("✧ Attribute Tokens",               Items.PAPER,              Formatting.LIGHT_PURPLE);

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
                // Add compacted materials
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
                
                // Add super compacted ores
                items.add(SlayerItems.createSuperCompactedGold());
                items.add(SlayerItems.createSuperCompactedIron());
                items.add(SlayerItems.createSuperCompactedDiamond());
                items.add(SlayerItems.createSuperCompactedEmerald());
                items.add(SlayerItems.createSuperCompactedNetherite());
                
                // Add enchanted blocks
                items.add(SlayerItems.createEnchantedCobblestone());
                items.add(SlayerItems.createEnchantedStone());
                items.add(SlayerItems.createEnchantedIronBlock());
                items.add(SlayerItems.createEnchantedGoldBlock());
                items.add(SlayerItems.createEnchantedDiamondBlock());
                items.add(SlayerItems.createEnchantedBlackstone());
                items.add(SlayerItems.createEnchantedGildedBlackstone());
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
        }
        return items;
    }

    // ──────────────────────────────────────────────────────────────
    // Page 0: Main Category Menu
    // ──────────────────────────────────────────────────────────────

    /** Entry point — opens the main category selection menu. */
    public static void open(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("§6§l/customitems — Choose Category"));

        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 27; i++) gui.setSlot(i, bg.build());

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("§6§l/customitems").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal("§7Select a category to browse items"))
                .addLoreLine(Text.literal("§7Click any item to receive it"))
                .glow().build());

        // Place category buttons (skip slot 4 = header)
        Category[] cats = Category.values();
        int[] catSlots = {0, 1, 2, 3, 5, 6, 7, 8};
        for (int i = 0; i < cats.length && i < catSlots.length; i++) {
            Category cat = cats[i];
            int slot = catSlots[i];
            gui.setSlot(slot, new GuiElementBuilder(cat.icon)
                    .setName(Text.literal("§l" + cat.label).formatted(cat.colour, Formatting.BOLD))
                    .addLoreLine(Text.literal("§7Click to browse items"))
                    .setCallback((idx, type, action) -> openCategoryPage(player, cat, 0))
                    .build());
        }

        // Row 2: More categories
        int[] row2Slots = {9, 10, 11, 12, 13, 14, 15, 16, 17};
        int extra = cats.length - catSlots.length;
        for (int i = 0; i < extra; i++) {
            Category cat = cats[catSlots.length + i];
            gui.setSlot(row2Slots[i], new GuiElementBuilder(cat.icon)
                    .setName(Text.literal("§l" + cat.label).formatted(cat.colour, Formatting.BOLD))
                    .addLoreLine(Text.literal("§7Click to browse items"))
                    .setCallback((idx, type, action) -> openCategoryPage(player, cat, 0))
                    .build());
        }

        gui.open();
    }

    // ──────────────────────────────────────────────────────────────
    // Page 1: Category Item List
    // ──────────────────────────────────────────────────────────────

    public static void openCategoryPage(ServerPlayerEntity player, Category cat, int page) {
        List<ItemStack> items = getItemsForCategory(cat);

        final int ITEMS_PER_PAGE = 28;
        int totalPages = Math.max(1, (items.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE);
        int clampedPage = Math.min(page, totalPages - 1);

        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("§6" + cat.label));

        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        // Header
        gui.setSlot(4, new GuiElementBuilder(cat.icon)
                .setName(Text.literal("§l" + cat.label).formatted(cat.colour, Formatting.BOLD))
                .addLoreLine(Text.literal("§7Click an item to receive it"))
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

            final ItemStack finalItem = item;
            GuiElementBuilder elem = new GuiElementBuilder(item.getItem())
                    .setName(displayName != null ? displayName
                            : Text.literal("§e" + item.getItem().toString()))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("§e➕ Click to receive"))
                    .setCallback((i, t, a) -> {
                        player.getInventory().insertStack(finalItem.copy());
                        String itemName = displayName != null ? displayName.getString() : item.getItem().toString();
                        player.sendMessage(Text.literal("✨ Given " + itemName + "!").formatted(Formatting.GREEN), false);
                    });
            gui.setSlot(slot, elem.build());
            shown++;
        }

        // ← Back to main menu
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Categories").formatted(Formatting.YELLOW))
                .setCallback((i, t, a) -> open(player)).build());

        // Pagination
        if (clampedPage > 0) {
            final int prev = clampedPage - 1;
            gui.setSlot(46, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("← Previous Page").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("§7Page " + clampedPage + " / " + totalPages))
                    .setCallback((i, t, a) -> openCategoryPage(player, cat, prev)).build());
        }
        if (clampedPage < totalPages - 1) {
            final int next = clampedPage + 1;
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("Next Page →").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("§7Page " + (clampedPage + 2) + " / " + totalPages))
                    .setCallback((i, t, a) -> openCategoryPage(player, cat, next)).build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("§7Page " + (clampedPage + 1) + " / " + totalPages))
                .build());

        gui.open();
    }
}
