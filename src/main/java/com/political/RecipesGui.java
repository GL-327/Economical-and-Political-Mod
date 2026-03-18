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

        // Search button
        gui.setSlot(0, new GuiElementBuilder(Items.SPYGLASS)
                .setName(Text.literal("🔍 Search Recipes").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to search items by name").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Supports partial matches!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openSearchGui(player))
                .build());

        // Gem & Block Armor category (Emerald, Lapis, and Block armors merged) - shifted right
        gui.setSlot(11, new GuiElementBuilder(Items.EMERALD)
                .setName(Text.literal("💎 Gem & Block Armor").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("💎 Emerald, Lapis, and Block armors").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openGemAndBlockArmorMenu(player, 0))
                .build());

        // T1 Swords category - shifted right
        gui.setSlot(12, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Text.literal("⚔ T1 Bounty Swords").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⚔ Tier 1 slayer swords").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openT1SwordsMenu(player))
                .build());

        // T2 Swords category - shifted right
        gui.setSlot(13, new GuiElementBuilder(Items.DIAMOND_SWORD)
                .setName(Text.literal("⚔ T2 Bounty Swords").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⚔ Tier 2 slayer swords").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openT2SwordsMenu(player))
                .build());

        // Materials category - shifted right
        gui.setSlot(14, new GuiElementBuilder(Items.SLIME_BALL)
                .setName(Text.literal("📦 Materials").formatted(Formatting.GRAY, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("📦 Cores, chunks & compacted items").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ New enchanted items added!").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMaterialsMenu(player))
                .build());

        // T1 Armor category - shifted right
        gui.setSlot(15, new GuiElementBuilder(Items.IRON_CHESTPLATE)
                .setName(Text.literal("🛡 T1 Bounty Armor").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🛡 Tier 1 slayer armor").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openT1ArmorMenu(player))
                .build());

        // T2 Armor category - shifted right
        gui.setSlot(16, new GuiElementBuilder(Items.DIAMOND_CHESTPLATE)
                .setName(Text.literal("🛡 T2 Bounty Armor").formatted(Formatting.DARK_AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🛡 Tier 2 slayer armor").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openT2ArmorMenu(player))
                .build());

        // Special/Legendary Armor category - shifted right
        gui.setSlot(17, new GuiElementBuilder(Items.NETHERITE_HELMET)
                .setName(Text.literal("🛡 Special Armor").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🛡 Legendary & special armor pieces").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openSpecialArmorMenu(player))
                .build());

        // Row 2: More categories - shifted right
        // Special Items category
        gui.setSlot(20, new GuiElementBuilder(Items.STICK)
                .setName(Text.literal("🎁 Special Items").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🎁 Special unique items").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openSpecialItemsMenu(player))
                .build());

        // Piglin Items category
        gui.setSlot(21, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Text.literal("👑 Piglin Items").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("👑 Piglin-themed items").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openPiglinItemsMenu(player))
                .build());

        // Gold Armor Evolution category
        gui.setSlot(22, new GuiElementBuilder(Items.GOLDEN_CHESTPLATE)
                .setName(Text.literal("👑 Gold Armor Evolution").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Gold armor tiers (Pure → Gilded)").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openGoldArmorMenu(player))
                .build());

        // Attribute Tokens category
        gui.setSlot(23, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("✧ Attribute Tokens").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Armor attribute tokens").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openAttributeTokensMenu(player))
                .build());

        // Weapon Attributes category
        gui.setSlot(24, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("⚔ Weapon Attributes").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⚔ Weapon attribute tokens").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openWeaponAttributesMenu(player))
                .build());

        // Row 3: Block Tools category - shifted right
        gui.setSlot(29, new GuiElementBuilder(Items.DIAMOND_PICKAXE)
                .setName(Text.literal("⛏ Block Tools").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⛏ Block-themed tools (pickaxe, axe, shovel, hoe)").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openBlockToolsMenu(player, 0))
                .build());

        // Block Weapons category
        gui.setSlot(30, new GuiElementBuilder(Items.QUARTZ)
                .setName(Text.literal("⚔ Block Weapons").formatted(Formatting.WHITE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⚔ Block-themed swords").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openBlockWeaponsMenu(player, 0))
                .build());

        gui.open();
    }

    // ============================================================
    // CATEGORY MENU METHODS - All custom items
    // ============================================================

    private static void openT1SwordsMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("⚔ T1 Bounty Swords"));

        // Background with dark theme
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Text.literal("⚔ T1 Bounty Swords").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Tier 1 slayer swords").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click any sword to view recipe").formatted(Formatting.GRAY))
                .glow().build());

        // Swords in a grid pattern
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23};
        int idx = 0;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            if (idx >= slots.length) break;
            ItemStack sword = SlayerItems.createSlayerSword(type);
            gui.setSlot(slots[idx++], new GuiElementBuilder(sword)
                    .setName(sword.get(DataComponentTypes.CUSTOM_NAME))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✨ Click to view recipe").formatted(Formatting.YELLOW))
                    .setCallback((i, clickType, action) -> showItemInfo(player, SlayerItems.createSlayerSword(type)))
                    .build());
        }

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Menu").formatted(Formatting.YELLOW))
                .setCallback((slot, clickType, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ============================================================
    // GEM & BLOCK ARMOR MENU - Organized sets with matching weapons
    // ============================================================
    private static void openGemAndBlockArmorMenu(ServerPlayerEntity player, int page) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("💎 Gem & Block Armor Sets"));

        // Background with dark theme
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.EMERALD)
                .setName(Text.literal("💎 Gem & Block Armor Sets").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Block → Armor Set → Weapon").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Complete sets with matching weapons!").formatted(Formatting.YELLOW))
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
                    .setName(Text.literal(set.name + " Set").formatted(Formatting.YELLOW, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Material:").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(set.name).formatted(Formatting.WHITE))
                    .glow()
                    .build());
            
            // Gap slot (shifted armor 1 to the right)
            gui.setSlot(baseSlot + 1, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
            
            // Armor pieces (middle - shifted 1 slot right)
            gui.setSlot(baseSlot + 2, createArmorElement(set.helmet, player));
            gui.setSlot(baseSlot + 3, createArmorElement(set.chestplate, player));
            gui.setSlot(baseSlot + 4, createArmorElement(set.leggings, player));
            gui.setSlot(baseSlot + 5, createArmorElement(set.boots, player));
            
            // Weapon (right side)
            gui.setSlot(baseSlot + 6, new GuiElementBuilder(set.sword)
                    .setName(set.sword.get(DataComponentTypes.CUSTOM_NAME))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✨ Click for recipe").formatted(Formatting.YELLOW))
                    .setCallback((idx, type, action) -> showItemInfo(player, set.sword))
                    .build());
            
            // Add separator lore below
            Text setName = Text.literal("━━ " + set.name + " Set ━━").formatted(Formatting.DARK_GRAY);
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
                .setName(Text.literal("← Back to Menu").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openMainMenu(player))
                .build());

        // Pagination
        if (clampedPage > 0) {
            final int prev = clampedPage - 1;
            gui.setSlot(46, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("← Previous Page").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("Page " + (clampedPage) + " / " + totalPages))
                    .setCallback((slot, type, action) -> openGemAndBlockArmorMenu(player, prev))
                    .build());
        }
        if (clampedPage < totalPages - 1) {
            final int next = clampedPage + 1;
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("Next Page →").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("Page " + (clampedPage + 2) + " / " + totalPages))
                    .setCallback((slot, type, action) -> openGemAndBlockArmorMenu(player, next))
                    .build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("Page " + (clampedPage + 1) + " / " + totalPages).formatted(Formatting.WHITE))
                .build());

        gui.open();
    }
    
    private static GuiElementBuilder createArmorElement(ItemStack armor, ServerPlayerEntity player) {
        Text name = armor.get(DataComponentTypes.CUSTOM_NAME);
        return new GuiElementBuilder(armor)
                .setName(name)
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click for recipe").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> showItemInfo(player, armor));
    }
    
    // Helper class for organizing armor set display
    private static class ArmorSetDisplay {
        final net.minecraft.item.Item blockItem;
        final String name;
        final ItemStack sword, helmet, chestplate, leggings, boots;
        
        ArmorSetDisplay(net.minecraft.item.Item blockItem, String name,
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
        final net.minecraft.item.Item blockItem;
        final String name;
        final ItemStack pickaxe, axe, shovel, hoe;
        
        ToolSetDisplay(net.minecraft.item.Item blockItem, String name,
                       ItemStack pickaxe, ItemStack axe, ItemStack shovel, ItemStack hoe) {
            this.blockItem = blockItem;
            this.name = name;
            this.pickaxe = pickaxe;
            this.axe = axe;
            this.shovel = shovel;
            this.hoe = hoe;
        }
    }

    // ============================================================
    // BLOCK TOOLS MENU
    // ============================================================

    private static void openBlockToolsMenu(ServerPlayerEntity player, int page) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⛏ Block Tools"));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.DIAMOND_PICKAXE)
                .setName(Text.literal("⛏ Block Tools").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Block-themed tools with special abilities").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click any tool to view recipe").formatted(Formatting.GRAY))
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
                    .setName(Text.literal(set.name + " Tools").formatted(Formatting.YELLOW, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Material:").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(set.name).formatted(Formatting.WHITE))
                    .glow()
                    .build());
            
            // Tools (middle to right)
            gui.setSlot(baseSlot + 1, createToolElement(set.pickaxe, player));
            gui.setSlot(baseSlot + 2, createToolElement(set.axe, player));
            gui.setSlot(baseSlot + 3, createToolElement(set.shovel, player));
            gui.setSlot(baseSlot + 4, createToolElement(set.hoe, player));
            
            // Add separator lore below
            Text setName = Text.literal("━━ " + set.name + " Tools ━━").formatted(Formatting.DARK_GRAY);
            for (int sep = baseSlot + 9; sep < baseSlot + 14 && sep < 54; sep++) {
                gui.setSlot(sep, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                        .setName(setName)
                        .build());
            }
        }

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Menu").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openMainMenu(player))
                .build());

        // Pagination
        if (clampedPage > 0) {
            final int prev = clampedPage - 1;
            gui.setSlot(46, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("← Previous Page").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("Page " + (clampedPage) + " / " + totalPages))
                    .setCallback((slot, type, action) -> openBlockToolsMenu(player, prev))
                    .build());
        }
        if (clampedPage < totalPages - 1) {
            final int next = clampedPage + 1;
            gui.setSlot(48, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("Next Page →").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("Page " + (clampedPage + 2) + " / " + totalPages))
                    .setCallback((slot, type, action) -> openBlockToolsMenu(player, next))
                    .build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("Page " + (clampedPage + 1) + " / " + totalPages).formatted(Formatting.WHITE))
                .build());

        gui.open();
    }

    // ============================================================
    // BLOCK WEAPONS MENU
    // ============================================================

    private static void openBlockWeaponsMenu(ServerPlayerEntity player, int page) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("⚔ Block Weapons"));

        // Background with dark theme
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.QUARTZ)
                .setName(Text.literal("⚔ Block Weapons").formatted(Formatting.WHITE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Block-themed swords").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click any weapon to view recipe").formatted(Formatting.GRAY))
                .glow().build());

        // All block swords
        ItemStack[] weapons = {
            BlockWeapons.createGlassSword(),
            BlockWeapons.createObsidianSword(),
            BlockWeapons.createQuartzSword(),
            BlockWeapons.createGlowstoneSword(),
            BlockWeapons.createRedstoneSword(),
            BlockWeapons.createNetherrackSword(),
            BlockWeapons.createEndstoneSword(),
            BlockWeapons.createIceSword(),
            BlockWeapons.createPrismarineSword(),
            BlockWeapons.createTerracottaSword(),
            BlockWeapons.createMossySword(),
            BlockWeapons.createSoulSandSword(),
            BlockWeapons.createMagmaSword(),
            BlockWeapons.createSandstoneSword(),
            BlockWeapons.createAmethystSword(),
            BlockWeapons.createCoalSword()
        };

        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29};
        int idx = 0;
        for (ItemStack weapon : weapons) {
            if (idx >= slots.length) break;
            gui.setSlot(slots[idx++], new GuiElementBuilder(weapon)
                    .setName(weapon.get(DataComponentTypes.CUSTOM_NAME))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✨ Click for recipe").formatted(Formatting.YELLOW))
                    .setCallback((i, clickType, action) -> showItemInfo(player, weapon))
                    .build());
        }

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Menu").formatted(Formatting.YELLOW))
                .setCallback((slot, clickType, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    private static GuiElementBuilder createToolElement(ItemStack tool, ServerPlayerEntity player) {
        Text name = tool.get(DataComponentTypes.CUSTOM_NAME);
        return new GuiElementBuilder(tool)
                .setName(name)
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click for recipe").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> showItemInfo(player, tool));
    }

    private static void openT2SwordsMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("⚔ T2 Bounty Swords"));

        // Background with dark theme
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.DIAMOND_SWORD)
                .setName(Text.literal("⚔ T2 Bounty Swords").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Tier 2 slayer swords").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click any sword to view recipe").formatted(Formatting.GRAY))
                .glow().build());

        // Swords in a grid pattern
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23};
        int idx = 0;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            if (idx >= slots.length) break;
            ItemStack sword = SlayerItems.createUpgradedSlayerSword(type);
            gui.setSlot(slots[idx++], new GuiElementBuilder(sword)
                    .setName(sword.get(DataComponentTypes.CUSTOM_NAME))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✨ Click to view recipe").formatted(Formatting.YELLOW))
                    .setCallback((i, clickType, action) -> showItemInfo(player, SlayerItems.createUpgradedSlayerSword(type)))
                    .build());
        }

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Menu").formatted(Formatting.YELLOW))
                .setCallback((slot, clickType, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    private static void openT1ArmorMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("🛡 T1 Bounty Armor"));

        // Background with dark border
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.IRON_CHESTPLATE)
                .setName(Text.literal("🛡 T1 Bounty Armor").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Select a slayer type below").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("to view the full armor set").formatted(Formatting.GRAY))
                .glow().build());

        // Slayer type buttons - row 2 and 3
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23};
        int idx = 0;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            if (idx >= slots.length) break;
            gui.setSlot(slots[idx++], new GuiElementBuilder(type.icon)
                    .setName(Text.literal("§ " + type.displayName + " Set").formatted(type.color, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("▸ Helmet").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("▸ Chestplate").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("▸ Leggings").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("▸ Boots").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                    .setCallback((i, clickType, action) -> openT1ArmorSetMenu(player, type))
                    .build());
        }

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Menu").formatted(Formatting.YELLOW))
                .setCallback((i, clickType, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    private static void openT1ArmorSetMenu(ServerPlayerEntity player, SlayerManager.SlayerType slayerType) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("🛡 " + slayerType.displayName + " T1 Set"));

        // Background
        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header with slayer type info
        gui.setSlot(4, new GuiElementBuilder(slayerType.icon)
                .setName(Text.literal(slayerType.displayName + " T1 Armor").formatted(slayerType.color, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Full armor set for " + slayerType.displayName).formatted(Formatting.GRAY))
                .glow().build());

        // Armor pieces in a row
        gui.setSlot(10, new GuiElementBuilder(SlayerItems.createSlayerHelmet(slayerType, 1))
                .setName(SlayerItems.createSlayerHelmet(slayerType, 1).get(DataComponentTypes.CUSTOM_NAME))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to view recipe").formatted(Formatting.YELLOW))
                .setCallback((i, clickType, action) -> showItemInfo(player, SlayerItems.createSlayerHelmet(slayerType, 1)))
                .build());

        gui.setSlot(11, new GuiElementBuilder(SlayerItems.createSlayerChestplate(slayerType, 1))
                .setName(SlayerItems.createSlayerChestplate(slayerType, 1).get(DataComponentTypes.CUSTOM_NAME))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to view recipe").formatted(Formatting.YELLOW))
                .setCallback((i, clickType, action) -> showItemInfo(player, SlayerItems.createSlayerChestplate(slayerType, 1)))
                .build());

        gui.setSlot(12, new GuiElementBuilder(SlayerItems.createSlayerLeggings(slayerType, 1))
                .setName(SlayerItems.createSlayerLeggings(slayerType, 1).get(DataComponentTypes.CUSTOM_NAME))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to view recipe").formatted(Formatting.YELLOW))
                .setCallback((i, clickType, action) -> showItemInfo(player, SlayerItems.createSlayerLeggings(slayerType, 1)))
                .build());

        gui.setSlot(13, new GuiElementBuilder(SlayerItems.createSlayerBoots(slayerType, 1))
                .setName(SlayerItems.createSlayerBoots(slayerType, 1).get(DataComponentTypes.CUSTOM_NAME))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to view recipe").formatted(Formatting.YELLOW))
                .setCallback((i, clickType, action) -> showItemInfo(player, SlayerItems.createSlayerBoots(slayerType, 1)))
                .build());

        // Back button
        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to T1 Armor").formatted(Formatting.YELLOW))
                .setCallback((i, clickType, action) -> openT1ArmorMenu(player))
                .build());

        gui.open();
    }

    private static void openT2ArmorMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("🛡 T2 Bounty Armor"));

        // Background with dark border
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.DIAMOND_CHESTPLATE)
                .setName(Text.literal("🛡 T2 Bounty Armor").formatted(Formatting.DARK_AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Select a slayer type below").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("to view the full armor set").formatted(Formatting.GRAY))
                .glow().build());

        // Slayer type buttons - row 2 and 3
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23};
        int idx = 0;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            if (idx >= slots.length) break;
            gui.setSlot(slots[idx++], new GuiElementBuilder(type.icon)
                    .setName(Text.literal("§ " + type.displayName + " Set").formatted(type.color, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("▸ Helmet").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("▸ Chestplate").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("▸ Leggings").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("▸ Boots").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                    .setCallback((i, clickType, action) -> openT2ArmorSetMenu(player, type))
                    .build());
        }

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Menu").formatted(Formatting.YELLOW))
                .setCallback((i, clickType, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    private static void openT2ArmorSetMenu(ServerPlayerEntity player, SlayerManager.SlayerType slayerType) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("🛡 " + slayerType.displayName + " T2 Set"));

        // Background
        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header with slayer type info
        gui.setSlot(4, new GuiElementBuilder(slayerType.icon)
                .setName(Text.literal(slayerType.displayName + " T2 Armor").formatted(slayerType.color, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Full armor set for " + slayerType.displayName).formatted(Formatting.GRAY))
                .glow().build());

        // Armor pieces in a row
        gui.setSlot(10, new GuiElementBuilder(SlayerItems.createSlayerHelmet(slayerType, 2))
                .setName(SlayerItems.createSlayerHelmet(slayerType, 2).get(DataComponentTypes.CUSTOM_NAME))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to view recipe").formatted(Formatting.YELLOW))
                .setCallback((i, clickType, action) -> showItemInfo(player, SlayerItems.createSlayerHelmet(slayerType, 2)))
                .build());

        gui.setSlot(11, new GuiElementBuilder(SlayerItems.createSlayerChestplate(slayerType, 2))
                .setName(SlayerItems.createSlayerChestplate(slayerType, 2).get(DataComponentTypes.CUSTOM_NAME))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to view recipe").formatted(Formatting.YELLOW))
                .setCallback((i, clickType, action) -> showItemInfo(player, SlayerItems.createSlayerChestplate(slayerType, 2)))
                .build());

        gui.setSlot(12, new GuiElementBuilder(SlayerItems.createSlayerLeggings(slayerType, 2))
                .setName(SlayerItems.createSlayerLeggings(slayerType, 2).get(DataComponentTypes.CUSTOM_NAME))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to view recipe").formatted(Formatting.YELLOW))
                .setCallback((i, clickType, action) -> showItemInfo(player, SlayerItems.createSlayerLeggings(slayerType, 2)))
                .build());

        gui.setSlot(13, new GuiElementBuilder(SlayerItems.createSlayerBoots(slayerType, 2))
                .setName(SlayerItems.createSlayerBoots(slayerType, 2).get(DataComponentTypes.CUSTOM_NAME))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to view recipe").formatted(Formatting.YELLOW))
                .setCallback((i, clickType, action) -> showItemInfo(player, SlayerItems.createSlayerBoots(slayerType, 2)))
                .build());

        // Back button
        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to T2 Armor").formatted(Formatting.YELLOW))
                .setCallback((i, clickType, action) -> openT2ArmorMenu(player))
                .build());

        gui.open();
    }

    private static void openSpecialItemsMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("🎁 Special Items"));

        // Background with dark theme
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("🎁 Special Items").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Unique and legendary items").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click any item to view recipe").formatted(Formatting.GRAY))
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
            gui.setSlot(slots[idx++], new GuiElementBuilder(item)
                    .setName(item.get(DataComponentTypes.CUSTOM_NAME))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✨ Click to view recipe").formatted(Formatting.YELLOW))
                    .setCallback((slot, type2, action) -> showItemInfo(player, item))
                    .build());
        }

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Menu").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ============================================================
    // SPECIAL ARMOR MENU - Legendary & special armor pieces
    // ============================================================
    private static void openSpecialArmorMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("🛡 Special Armor"));

        // Background with dark theme
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.NETHERITE_HELMET)
                .setName(Text.literal("🛡 Special Armor").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Legendary & special armor pieces").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click any item to view recipe").formatted(Formatting.GRAY))
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
            gui.setSlot(slots[idx++], new GuiElementBuilder(item)
                    .setName(item.get(DataComponentTypes.CUSTOM_NAME))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✨ Click to view recipe").formatted(Formatting.YELLOW))
                    .setCallback((slot, type2, action) -> showItemInfo(player, item))
                    .build());
        }

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Menu").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ============================================================
    // BLOCK ARMOR MENU - Armor Sets in Rows Format
    // Each row: [Block] [Sword] [Helmet] [Chest] [Legs] [Boots]
    // ============================================================
    private static void openBlockArmorMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("💎 Gem & Block Armor"));

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
                .glow().build());

        // Define armor sets: Block Item, Name, Sword, Armor pieces
        ArmorSetDisplay[] sets = {
            new ArmorSetDisplay(Items.GLASS, "Glass", BlockWeapons.createGlassSword(),
                BlockArmor.createGlassHelmet(), BlockArmor.createGlassChestplate(),
                BlockArmor.createGlassLeggings(), BlockArmor.createGlassBoots()),
            new ArmorSetDisplay(Items.OBSIDIAN, "Obsidian", BlockWeapons.createObsidianSword(),
                BlockArmor.createObsidianHelmet(), BlockArmor.createObsidianChestplate(),
                BlockArmor.createObsidianLeggings(), BlockArmor.createObsidianBoots()),
            new ArmorSetDisplay(Items.QUARTZ_BLOCK, "Quartz", BlockWeapons.createQuartzSword(),
                BlockArmor.createQuartzHelmet(), BlockArmor.createQuartzChestplate(),
                BlockArmor.createQuartzLeggings(), BlockArmor.createQuartzBoots()),
            new ArmorSetDisplay(Items.GLOWSTONE, "Glowstone", BlockWeapons.createGlowstoneSword(),
                BlockArmor.createGlowstoneHelmet(), BlockArmor.createGlowstoneChestplate(),
                BlockArmor.createGlowstoneLeggings(), BlockArmor.createGlowstoneBoots()),
            new ArmorSetDisplay(Items.REDSTONE_BLOCK, "Redstone", BlockWeapons.createRedstoneSword(),
                BlockArmor.createRedstoneHelmet(), BlockArmor.createRedstoneChestplate(),
                BlockArmor.createRedstoneLeggings(), BlockArmor.createRedstoneBoots()),
            new ArmorSetDisplay(Items.NETHERRACK, "Netherrack", BlockWeapons.createNetherrackSword(),
                BlockArmor.createNetherrackHelmet(), BlockArmor.createNetherrackChestplate(),
                BlockArmor.createNetherrackLeggings(), BlockArmor.createNetherrackBoots()),
            new ArmorSetDisplay(Items.END_STONE, "End Stone", BlockWeapons.createEndstoneSword(),
                BlockArmor.createEndstoneHelmet(), BlockArmor.createEndstoneChestplate(),
                BlockArmor.createEndstoneLeggings(), BlockArmor.createEndstoneBoots()),
            new ArmorSetDisplay(Items.PACKED_ICE, "Ice", BlockWeapons.createIceSword(),
                BlockArmor.createIceHelmet(), BlockArmor.createIceChestplate(),
                BlockArmor.createIceLeggings(), BlockArmor.createIceBoots()),
            new ArmorSetDisplay(Items.PRISMARINE, "Prismarine", BlockWeapons.createPrismarineSword(),
                BlockArmor.createPrismarineHelmet(), BlockArmor.createPrismarineChestplate(),
                BlockArmor.createPrismarineLeggings(), BlockArmor.createPrismarineBoots()),
            new ArmorSetDisplay(Items.TERRACOTTA, "Terracotta", BlockWeapons.createTerracottaSword(),
                BlockArmor.createTerracottaHelmet(), BlockArmor.createTerracottaChestplate(),
                BlockArmor.createTerracottaLeggings(), BlockArmor.createTerracottaBoots()),
            new ArmorSetDisplay(Items.MOSSY_COBBLESTONE, "Mossy", BlockWeapons.createMossySword(),
                BlockArmor.createMossyHelmet(), BlockArmor.createMossyChestplate(),
                BlockArmor.createMossyLeggings(), BlockArmor.createMossyBoots()),
            new ArmorSetDisplay(Items.SOUL_SAND, "Soul Sand", BlockWeapons.createSoulSandSword(),
                BlockArmor.createSoulSandHelmet(), BlockArmor.createSoulSandChestplate(),
                BlockArmor.createSoulSandLeggings(), BlockArmor.createSoulSandBoots()),
            new ArmorSetDisplay(Items.MAGMA_BLOCK, "Magma", BlockWeapons.createMagmaSword(),
                BlockArmor.createMagmaHelmet(), BlockArmor.createMagmaChestplate(),
                BlockArmor.createMagmaLeggings(), BlockArmor.createMagmaBoots()),
            new ArmorSetDisplay(Items.SANDSTONE, "Sandstone", BlockWeapons.createSandstoneSword(),
                BlockArmor.createSandstoneHelmet(), BlockArmor.createSandstoneChestplate(),
                BlockArmor.createSandstoneLeggings(), BlockArmor.createSandstoneBoots()),
            new ArmorSetDisplay(Items.AMETHYST_BLOCK, "Amethyst", BlockWeapons.createAmethystSword(),
                BlockArmor.createAmethystHelmet(), BlockArmor.createAmethystChestplate(),
                BlockArmor.createAmethystLeggings(), BlockArmor.createAmethystBoots()),
            new ArmorSetDisplay(Items.COAL_BLOCK, "Coal", BlockWeapons.createCoalSword(),
                BlockArmor.createCoalHelmet(), BlockArmor.createCoalChestplate(),
                BlockArmor.createCoalLeggings(), BlockArmor.createCoalBoots()),
        };

        // Display armor sets in rows (4 sets per page)
        final int SETS_PER_PAGE = 4;
        int totalPages = (sets.length + SETS_PER_PAGE - 1) / SETS_PER_PAGE;
        int clampedPage = 0; // For now, just show page 0

        int startIdx = clampedPage * SETS_PER_PAGE;
        for (int i = 0; i < SETS_PER_PAGE && (startIdx + i) < sets.length; i++) {
            ArmorSetDisplay set = sets[startIdx + i];
            int row = i + 1; // Rows 1-4 (slots 9-44)

            // Row layout: [0:Block] [1:gap] [2:Sword] [3:Helmet] [4:Chest] [5:Legs] [6:Boots]
            int[] slots = { row * 9, row * 9 + 1, row * 9 + 2, row * 9 + 3, row * 9 + 4, row * 9 + 5, row * 9 + 6 };

            // Block (decorative background)
            gui.setSlot(slots[0], new GuiElementBuilder(set.blockItem)
                    .setName(Text.literal("§8" + set.blockItem.toString().replace("_", " ")).formatted(Formatting.DARK_GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Armor Set Material").formatted(Formatting.GRAY))
                    .build());
            
            // Gap slot (shifted armor 1 to the right)
            gui.setSlot(slots[1], new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());

            // Sword
            Text swordName = set.sword.get(DataComponentTypes.CUSTOM_NAME);
            gui.setSlot(slots[2], new GuiElementBuilder(set.sword.getItem())
                    .setName(swordName != null ? swordName : Text.literal("Sword"))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✨ Click to view recipe").formatted(Formatting.YELLOW))
                    .setCallback((s, t, a) -> showItemInfo(player, set.sword))
                    .build());

            // Armor pieces: Helmet, Chestplate, Leggings, Boots (shifted 1 slot right)
            ItemStack[] armorPieces = { set.helmet, set.chestplate, set.leggings, set.boots };
            for (int j = 0; j < 4; j++) {
                ItemStack armor = armorPieces[j];
                Text armorName = armor.get(DataComponentTypes.CUSTOM_NAME);
                gui.setSlot(slots[3 + j], new GuiElementBuilder(armor.getItem())
                        .setName(armorName != null ? armorName : Text.literal("Armor"))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("✨ Click to view recipe").formatted(Formatting.YELLOW))
                        .setCallback((s, t, a) -> showItemInfo(player, armor))
                        .build());
            }
        }

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Main Menu").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player))
                .build());

        // Pagination buttons (if needed in future)
        if (totalPages > 1) {
            gui.setSlot(49, new GuiElementBuilder(Items.PAPER)
                    .setName(Text.literal("Page 1 / " + totalPages))
                    .build());
        }

        gui.open();
    }

    // ============================================================
    // GOLD ARMOR EVOLUTION MENU - All gold armor tiers
    // ============================================================
    private static void openGoldArmorMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("👑 Gold Armor Evolution"));

        // Background with dark theme
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.GOLDEN_CHESTPLATE)
                .setName(Text.literal("👑 Gold Armor Evolution").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Pure → Polished → Shiny → Glistening → Gilded").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click any item to view recipe").formatted(Formatting.GRAY))
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
            gui.setSlot(slots[idx++], new GuiElementBuilder(item)
                    .setName(item.get(DataComponentTypes.CUSTOM_NAME))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✨ Click to view recipe").formatted(Formatting.YELLOW))
                    .setCallback((slot, type2, action) -> showItemInfo(player, item))
                    .build());
        }

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Menu").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ============================================================
    // ATTRIBUTE TOKENS MENU - Armor attribute tokens
    // ============================================================
    private static void openAttributeTokensMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("✧ Attribute Tokens"));

        // Background with dark theme
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("✧ Attribute Tokens").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Armor attribute tokens for crafting").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click any token to view recipe").formatted(Formatting.GRAY))
                .glow().build());

        // Add all attribute tokens in a grid
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23};
        int idx = 0;
        for (ArmourAttribute attr : ArmourAttribute.values()) {
            if (idx >= slots.length) break;
            
            ItemStack token = SlayerItems.createAttributeToken(attr);
            gui.setSlot(slots[idx++], new GuiElementBuilder(token)
                    .setName(token.get(DataComponentTypes.CUSTOM_NAME))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✨ Click to view recipe").formatted(Formatting.YELLOW))
                    .setCallback((slot, type2, action) -> showItemInfo(player, token))
                    .build());
        }

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Menu").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    private static void openPiglinItemsMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("👑 Piglin Items"));

        // Background with dark theme
        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Text.literal("👑 Piglin Items").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Piglin-themed items").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click any item to view recipe").formatted(Formatting.GRAY))
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
            gui.setSlot(slots[idx++], new GuiElementBuilder(item)
                    .setName(item.get(DataComponentTypes.CUSTOM_NAME))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✨ Click to view recipe").formatted(Formatting.YELLOW))
                    .setCallback((slot, type2, action) -> showItemInfo(player, item))
                    .build());
        }

        // Back button
        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Menu").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ============================================================
    // MATERIALS MENU - Shows recent additions
    // ============================================================
    private static void openMaterialsMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("📦 Materials"));

        // Background with dark theme
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.SLIME_BALL)
                .setName(Text.literal("📦 Materials").formatted(Formatting.GRAY, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("All crafting materials").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click any item to view recipe").formatted(Formatting.GRAY))
                .glow().build());

        // Add cores and chunks in a grid
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23};
        int idx = 0;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            if (idx >= slots.length) break;
            
            gui.setSlot(slots[idx++], new GuiElementBuilder(SlayerItems.createCore(type))
                    .setName(SlayerItems.createCore(type).get(DataComponentTypes.CUSTOM_NAME))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✨ Click to view recipe").formatted(Formatting.YELLOW))
                    .setCallback((slot, type2, action) -> showItemInfo(player, SlayerItems.createCore(type)))
                    .build());
        }

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Menu").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ============================================================
    // WEAPON ATTRIBUTES MENU - Recent addition
    // ============================================================
    private static void openWeaponAttributesMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("⚔ Weapon Attributes"));

        // Background with dark theme
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("⚔ Weapon Attributes").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Weapon attribute tokens").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click any token to view recipe").formatted(Formatting.GRAY))
                .glow().build());

        // Add all weapon attribute tokens in a grid
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23};
        int idx = 0;
        for (WeaponAttribute attr : WeaponAttribute.values()) {
            if (idx >= slots.length) break;
            
            ItemStack token = SlayerItems.createWeaponAttributeToken(attr);
            gui.setSlot(slots[idx++], new GuiElementBuilder(token)
                    .setName(token.get(DataComponentTypes.CUSTOM_NAME))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✨ Click to view recipe").formatted(Formatting.YELLOW))
                    .setCallback((slot, type2, action) -> showItemInfo(player, token))
                    .build());
        }

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Menu").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ============================================================
    // HELPER: Show item information with actual recipe
    // ============================================================
    private static void showItemInfo(ServerPlayerEntity player, ItemStack resultItem) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("Recipe View"));

        // Background with dark theme
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header - Item name
        Text displayName = resultItem.get(DataComponentTypes.CUSTOM_NAME);
        if (displayName == null) {
            displayName = Text.literal(resultItem.getItem().toString());
        }
        gui.setSlot(4, new GuiElementBuilder(resultItem.getItem())
                .setName(displayName)
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Crafting Recipe").formatted(Formatting.GRAY))
                .glow().build());

        // Recipe grid slots (3x3)
        int[] gridSlots = {10, 11, 12, 19, 20, 21, 28, 29, 30};
        
        // Try to find the recipe from RecipeConfigManager (includes custom recipes)
        SlayerRecipes.Recipe recipe = findRecipeForOutput(resultItem);
        
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
                            .setName(Text.literal("Empty").formatted(Formatting.GRAY))
                            .build());
                }
            }
            
            // Arrow indicator
            gui.setSlot(23, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("→").formatted(Formatting.YELLOW))
                    .build());
            
            // Result
            gui.setSlot(24, new GuiElementBuilder(resultItem)
                    .setName(displayName)
                    .glow().build());
        } else {
            // No recipe found - show message
            gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                    .setName(Text.literal("No recipe found").formatted(Formatting.RED))
                    .addLoreLine(Text.literal("This item may be obtained"))
                    .addLoreLine(Text.literal("through other means."))
                    .build());
        }

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Menu").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openMainMenu(player))
                .build());

        gui.open();
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
                Text recipeName = recipe.result.get(DataComponentTypes.CUSTOM_NAME);
                Text outputName = output.get(DataComponentTypes.CUSTOM_NAME);
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
    private static void openSearchGui(ServerPlayerEntity player) {
        eu.pb4.sgui.api.gui.SignGui signGui = new eu.pb4.sgui.api.gui.SignGui(player) {
            @Override
            public void onClose() {
                String searchTerm = this.getLine(0).getString().trim();
                if (!searchTerm.isEmpty()) {
                    showSearchResults(player, searchTerm);
                } else {
                    openMainMenu(player);
                }
            }
        };
        signGui.setLine(0, Text.literal(""));
        signGui.setLine(1, Text.literal("^^^^^^^^^^^^^^").formatted(Formatting.YELLOW));
        signGui.setLine(2, Text.literal("Enter item name").formatted(Formatting.AQUA));
        signGui.setLine(3, Text.literal("(partial ok)").formatted(Formatting.GRAY));
        signGui.open();
    }
    
    private static void showSearchResults(ServerPlayerEntity player, String searchTerm) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("🔍 Search: " + searchTerm));
        
        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }
        
        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.SPYGLASS)
                .setName(Text.literal("🔍 Search Results").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Search: \"" + searchTerm + "\"").formatted(Formatting.YELLOW))
                .glow().build());
        
        // Search through all recipes
        java.util.List<ItemStack> results = new java.util.ArrayList<>();
        String searchLower = searchTerm.toLowerCase();
        
        // Check all recipes
        for (SlayerRecipes.Recipe recipe : RecipeConfigManager.getRecipes()) {
            if (recipe.result != null && !recipe.result.isEmpty()) {
                Text nameText = recipe.result.get(DataComponentTypes.CUSTOM_NAME);
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
            
            Text name = result.get(DataComponentTypes.CUSTOM_NAME);
            if (name == null) name = Text.literal(result.getItem().toString());
            
            gui.setSlot(slots[slotIdx++], new GuiElementBuilder(result)
                    .setName(name)
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✨ Click for recipe").formatted(Formatting.YELLOW))
                    .setCallback((idx, type, action) -> showItemInfo(player, result))
                    .build());
        }
        
        if (results.isEmpty()) {
            gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                    .setName(Text.literal("No results found").formatted(Formatting.RED))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Try a different search term").formatted(Formatting.GRAY))
                    .build());
        }
        
        // Back and search again buttons
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Menu").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openMainMenu(player))
                .build());
        
        gui.setSlot(46, new GuiElementBuilder(Items.SPYGLASS)
                .setName(Text.literal("🔍 Search Again").formatted(Formatting.AQUA))
                .setCallback((slot, type, action) -> openSearchGui(player))
                .build());
        
        gui.open();
    }

    // ============================================================
    // SPECIFIC RECIPE LOOKUP - Search by item name
    // ============================================================
    public static boolean openSpecificRecipe(ServerPlayerEntity player, String itemName) {
        String searchLower = itemName.toLowerCase().replace(" ", "_");
        
        // Search through all recipes for a match
        for (SlayerRecipes.Recipe recipe : RecipeConfigManager.getRecipes()) {
            if (recipe.result == null || recipe.result.isEmpty()) continue;
            
            Text nameText = recipe.result.get(DataComponentTypes.CUSTOM_NAME);
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
        player.sendMessage(Text.literal("✖ No recipe found for: " + itemName).formatted(Formatting.RED), false);
        player.sendMessage(Text.literal("Use /recipe to browse all categories").formatted(Formatting.GRAY), false);
        openMainMenu(player);
        return false;
    }
}
