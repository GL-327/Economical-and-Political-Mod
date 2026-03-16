package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class RecipesGui {

    // Crafting grid slot positions in a 9x6 (54-slot) GUI — matches /setrecipe layout:
    // Row 0 (0-8):   bg  bg  bg  bg  HEADER bg  bg  bg  bg
    // Row 1 (9-17):  bg  S0  S1  S2  bg     bg  bg  bg  bg
    // Row 2 (18-26): bg  S3  S4  S5  bg     bg  ARR RES bg
    // Row 3 (27-35): bg  S6  S7  S8  bg     bg  bg  bg  bg
    // Row 4 (36-44): bg  bg  bg  bg  bg     bg  bg  bg  bg
    // Row 5 (45-53): BAK bg  bg  bg  bg     bg  bg  bg  bg
    private static final int[] GRID_SLOTS = {10, 11, 12, 19, 20, 21, 28, 29, 30};
    private static final int SLOT_HEADER = 4;
    private static final int SLOT_ARROW  = 23;
    private static final int SLOT_RESULT = 24;
    private static final int SLOT_BACK   = 45;

    // ============================================================
    // HELPER: Open a single recipe page (9x5 crafting grid view)
    // ============================================================
    private static void openRecipePage(ServerPlayerEntity player,
                                       String title,
                                       Text headerName,
                                       ItemStack[] ingredients, // exactly 9 slots (null = empty)
                                       ItemStack result,
                                       Runnable backAction) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal(title));

        // Background fill
        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Text.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        // Header
        gui.setSlot(SLOT_HEADER, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(headerName)
                .glow()
                .build());

        // 3x3 crafting grid — clear any background panes first
        for (int i = 0; i < 9; i++) {
            ItemStack ing = (ingredients != null && i < ingredients.length) ? ingredients[i] : null;
            if (ing != null && !ing.isEmpty()) {
                GuiElementBuilder eb = new GuiElementBuilder(ing.getItem());
                Text name = ing.get(DataComponentTypes.CUSTOM_NAME);
                if (name != null) eb.setName(name);
                if (ing.getCount() > 1) eb.setCount(ing.getCount());
                gui.setSlot(GRID_SLOTS[i], eb.build());
            } else {
                // Empty grid slot: override the background pane with air
                gui.setSlot(GRID_SLOTS[i], GuiElementBuilder.from(ItemStack.EMPTY).build());
            }
        }

        // Arrow pointing right
        gui.setSlot(SLOT_ARROW, new GuiElementBuilder(Items.SPECTRAL_ARROW)
                .setName(Text.literal("→").formatted(Formatting.YELLOW))
                .build());

        // Result item
        if (result != null && !result.isEmpty()) {
            GuiElementBuilder rb = new GuiElementBuilder(result.getItem());
            Text resultName = result.get(DataComponentTypes.CUSTOM_NAME);
            if (resultName != null) rb.setName(resultName);
            rb.glow();
            gui.setSlot(SLOT_RESULT, rb.build());
        }

        // Back button
        gui.setSlot(SLOT_BACK, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> backAction.run())
                .build());

        gui.open();
    }

    // ============================================================
    // HELPER: Create a display ItemStack with custom name and count
    // ============================================================
    private static ItemStack display(net.minecraft.item.Item item, int count, String name, Formatting color) {
        ItemStack stack = new ItemStack(item, count);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name).formatted(color));
        return stack;
    }

    private static ItemStack display(net.minecraft.item.Item item, int count, String name) {
        return display(item, count, name, Formatting.WHITE);
    }

    /**
     * Item #8 – /setrecipe ↔ /recipe sync.
     * Checks RecipeConfigManager for a custom admin recipe whose result matches
     * the custom item ID of {@code resultStack}. If one is found the recipe page
     * is opened with the admin-configured ingredients instead of the hardcoded
     * ones. Returns true when the custom recipe was shown, false when the caller
     * should fall through to its own hardcoded recipe.
     */
    private static boolean tryOpenCustomRecipe(ServerPlayerEntity player,
                                               ItemStack resultStack,
                                               String title,
                                               Runnable backAction) {
        String customId = SlayerItems.getCustomItemId(resultStack);
        if (customId == null) return false;

        for (SlayerRecipes.Recipe r : RecipeConfigManager.getCustomRecipes()) {
            String rId = SlayerItems.getCustomItemId(r.result);
            if (customId.equals(rId)) {
                Text headerName = r.result.get(DataComponentTypes.CUSTOM_NAME);
                if (headerName == null) headerName = Text.literal(r.name).formatted(Formatting.WHITE);
                openRecipePage(player, title, headerName, r.ingredients, r.result, backAction);
                return true;
            }
        }
        return false;
    }

    // ============================================================
    // MAIN MENU - Symmetric Layout
    // ============================================================
    public static void openMainMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("📖 Recipe Workshop"));

        // Decorative background with symmetric pattern
        for (int i = 0; i < 54; i++) {
            if (i % 9 == 0 || i % 9 == 8 || i < 9 || i >= 45) {
                gui.setSlot(i, new GuiElementBuilder(Items.CYAN_STAINED_GLASS_PANE)
                        .setName(Text.literal("⬛").formatted(Formatting.DARK_BLUE))
                        .build());
            } else {
                gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                        .setName(Text.literal("")).build());
            }
        }

        // Center header
        gui.setSlot(4, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(Text.literal("📖 Recipe Workshop").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🎨 Master crafting recipes").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("⚡ Create custom recipes").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("🔧 Browse all categories").formatted(Formatting.GRAY))
                .glow().build());

        // Symmetric category layout - Top row
        gui.setSlot(10, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Text.literal("⚔️ Bounty Swords (T1)").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🗡️ Tier 1 bounty weapons").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to explore").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openT1SwordsMenu(player))
                .build());

        gui.setSlot(11, new GuiElementBuilder(Items.DIAMOND_SWORD)
                .setName(Text.literal("⚔️ Bounty Swords (T2)").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🗡️ Tier 2 bounty weapons").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to explore").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openT2SwordsMenu(player))
                .build());

        gui.setSlot(12, new GuiElementBuilder(Items.BLAZE_ROD)
                .setName(Text.literal("⚡ HPEBM Weapons").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⚡ High-power energy weapons").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to explore").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openHPEBMMenu(player))
                .build());

        gui.setSlot(13, new GuiElementBuilder(Items.COMMAND_BLOCK)
                .setName(Text.literal("⚙ Create Custom Recipe").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🎨 Design your own recipes").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("🔧 Admin only feature").formatted(Formatting.RED))
                .addLoreLine(Text.literal("✨ Click to create").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCustomRecipeCreator(player))
                .build());

        gui.setSlot(14, new GuiElementBuilder(Items.NETHERITE_SWORD)
                .setName(Text.literal("🗡️ Special Weapons").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🎁 Unique special weapons").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to explore").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openSpecialWeaponsMenu(player))
                .build());

        gui.setSlot(15, new GuiElementBuilder(Items.NETHERITE_HELMET)
                .setName(Text.literal("🛡️ Special Armor").formatted(Formatting.DARK_GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🎁 Unique special armor").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to explore").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openSpecialArmorMenu(player))
                .build());

        gui.setSlot(16, new GuiElementBuilder(Items.GOLDEN_APPLE)
                .setName(Text.literal("🎁 Special Items").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🎁 Unique special items").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to explore").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openSpecialItemsMenu(player))
                .build());

        // Midas's Sword
        gui.setSlot(17, new GuiElementBuilder(SlayerItems.createMidasSword())
                .setName(SlayerItems.createMidasSword().get(DataComponentTypes.CUSTOM_NAME))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⚔️ Midas's Sword").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal("🎁 Legendary golden weapon").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Touch of Midas").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openMidasSwordRecipe(player))
                .glow()
                .build());

        // Second row - Armor categories
        gui.setSlot(19, new GuiElementBuilder(Items.IRON_CHESTPLATE)
                .setName(Text.literal("🛡️ Bounty Armor (T1)").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🛡️ Tier 1 bounty armor").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to explore").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openT1ArmorMenu(player))
                .build());

        gui.setSlot(20, new GuiElementBuilder(Items.DIAMOND_CHESTPLATE)
                .setName(Text.literal("🛡️ Bounty Armor (T2)").formatted(Formatting.DARK_AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🛡️ Tier 2 bounty armor").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to explore").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openT2ArmorMenu(player))
                .build());

        gui.setSlot(21, new GuiElementBuilder(Items.GOLDEN_CHESTPLATE)
                .setName(Text.literal("👑 Gold Armor Evolution").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("📈 5 tiers of gold armor").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to explore").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openGoldArmorMenu(player))
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.NETHERITE_HELMET)
                .setName(Text.literal("👑 Legendary Crowns").formatted(Formatting.DARK_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("👑 Premium crowns & helmets").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to explore").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openLegendaryCrownsMenu(player))
                .build());

        gui.setSlot(23, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Text.literal("👑 Piglin Collection").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("💰 Premium piglin items").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to explore").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openPiglinMenu(player))
                .build());

        gui.setSlot(24, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Text.literal("🧱 Compacted Materials").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🔨 Dense material blocks").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to explore").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCompactedMaterialsMenu(player))
                .build());

        gui.setSlot(25, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("⭐ Super Compacted").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🌟 Ultra-dense materials").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to explore").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openSuperCompactedMenu(player))
                .build());

        // Third row - Weapon Attributes (replacing armor attributes)
        gui.setSlot(31, new GuiElementBuilder(Items.ENCHANTED_BOOK)
                .setName(Text.literal("⚔️ Weapon Attributes").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🔥 Apply weapon abilities").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("⚡ 10 unique attributes").formatted(Formatting.DARK_GRAY))
                .addLoreLine(Text.literal("✨ Click to explore").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openWeaponAttributesMenu(player))
                .glow()
                .build());

        // Bottom navigation
        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("❌ Close").formatted(Formatting.DARK_RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Exit recipe workshop").formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> player.closeHandledScreen())
                .build());

        gui.open();
    }

    // ============================================================
    // HELPER METHODS - Placeholder implementations
    // ============================================================

    private static void openT1SwordsMenu(ServerPlayerEntity player) {
        // TODO: Implement T1 swords menu
        player.sendMessage(Text.literal("T1 Swords menu coming soon!").formatted(Formatting.YELLOW), false);
    }

    private static void openT2SwordsMenu(ServerPlayerEntity player) {
        // TODO: Implement T2 swords menu
        player.sendMessage(Text.literal("T2 Swords menu coming soon!").formatted(Formatting.YELLOW), false);
    }

    private static void openHPEBMMenu(ServerPlayerEntity player) {
        // TODO: Implement HPEBM menu
        player.sendMessage(Text.literal("HPEBM menu coming soon!").formatted(Formatting.YELLOW), false);
    }

    private static void openCustomRecipeCreator(ServerPlayerEntity player) {
        // TODO: Implement custom recipe creator
        player.sendMessage(Text.literal("Custom recipe creator coming soon!").formatted(Formatting.YELLOW), false);
    }

    private static void openSpecialWeaponsMenu(ServerPlayerEntity player) {
        // TODO: Implement special weapons menu
        player.sendMessage(Text.literal("Special weapons menu coming soon!").formatted(Formatting.YELLOW), false);
    }

    private static void openSpecialArmorMenu(ServerPlayerEntity player) {
        // TODO: Implement special armor menu
        player.sendMessage(Text.literal("Special armor menu coming soon!").formatted(Formatting.YELLOW), false);
    }

    private static void openSpecialItemsMenu(ServerPlayerEntity player) {
        // TODO: Implement special items menu
        player.sendMessage(Text.literal("Special items menu coming soon!").formatted(Formatting.YELLOW), false);
    }

    private static void openT1ArmorMenu(ServerPlayerEntity player) {
        // TODO: Implement T1 armor menu
        player.sendMessage(Text.literal("T1 Armor menu coming soon!").formatted(Formatting.YELLOW), false);
    }

    private static void openT2ArmorMenu(ServerPlayerEntity player) {
        // TODO: Implement T2 armor menu
        player.sendMessage(Text.literal("T2 Armor menu coming soon!").formatted(Formatting.YELLOW), false);
    }

    private static void openGoldArmorMenu(ServerPlayerEntity player) {
        // TODO: Implement gold armor menu
        player.sendMessage(Text.literal("Gold armor menu coming soon!").formatted(Formatting.YELLOW), false);
    }

    private static void openLegendaryCrownsMenu(ServerPlayerEntity player) {
        // TODO: Implement legendary crowns menu
        player.sendMessage(Text.literal("Legendary crowns menu coming soon!").formatted(Formatting.YELLOW), false);
    }

    private static void openPiglinMenu(ServerPlayerEntity player) {
        // TODO: Implement piglin menu
        player.sendMessage(Text.literal("Piglin menu coming soon!").formatted(Formatting.YELLOW), false);
    }

    private static void openCompactedMaterialsMenu(ServerPlayerEntity player) {
        // TODO: Implement compacted materials menu
        player.sendMessage(Text.literal("Compacted materials menu coming soon!").formatted(Formatting.YELLOW), false);
    }

    private static void openSuperCompactedMenu(ServerPlayerEntity player) {
        // TODO: Implement super compacted menu
        player.sendMessage(Text.literal("Super compacted menu coming soon!").formatted(Formatting.YELLOW), false);
    }

    private static void openWeaponAttributesMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⚔️ Weapon Attributes"));

        // Decorative background
        for (int i = 0; i < 54; i++) {
            if (i % 9 == 0 || i % 9 == 8 || i < 9 || i >= 45) {
                gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                        .setName(Text.literal("⬛").formatted(Formatting.DARK_GRAY))
                        .build());
            } else {
                gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                        .setName(Text.literal("")).build());
            }
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.ENCHANTED_BOOK)
                .setName(Text.literal("⚔️ Weapon Attributes").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🔥 Apply weapon abilities").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("⚡ 10 unique attributes").formatted(Formatting.DARK_GRAY))
                .glow()
                .build());

        // Weapon attribute items
        int[] slots = {10, 11, 12, 19, 20, 21, 28, 29, 30};
        
        for (int i = 0; i < 10 && i < slots.length; i++) {
            WeaponAttribute attr = WeaponAttribute.values()[i];
            ItemStack token = SlayerItems.createWeaponAttributeToken(attr);
            
            List<Text> lore = new ArrayList<>();
            lore.add(Text.literal(""));
            lore.add(Text.literal(attr.displayName).formatted(Formatting.GOLD));
            lore.add(Text.literal(""));
            lore.add(Text.literal(attr.buffSummary).formatted(Formatting.GREEN));
            lore.add(Text.literal(""));
            if (!attr.debuffSummary.isEmpty()) {
                lore.add(Text.literal("⚠️ " + attr.debuffSummary).formatted(Formatting.RED));
            }
            lore.add(Text.literal(""));
            lore.add(Text.literal("💰 Cost: " + attr.xpCost + " XP").formatted(Formatting.YELLOW));
            
            gui.setSlot(slots[i], new GuiElementBuilder(token)
                    .setName(Text.literal(attr.displayName).formatted(Formatting.GOLD, Formatting.BOLD))
                    .setLore(lore)
                    .glow()
                    .build());
        }

        // Navigation
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
        // TODO: Implement specific recipe lookup
        player.sendMessage(Text.literal("Specific recipe lookup coming soon!").formatted(Formatting.YELLOW), false);
        return false;
    }

    // ============================================================
    // MIDAS SWORD RECIPE
    // ============================================================
    private static void openMidasSwordRecipe(ServerPlayerEntity player) {
        ItemStack result = SlayerItems.createMidasSword();
        String pageTitle = "⚔ Midas's Sword";
        if (tryOpenCustomRecipe(player, result, pageTitle, () -> openMainMenu(player))) return;
        
        // Default recipe if no custom recipe found
        ItemStack[] ingredients = {
            display(Items.GOLDEN_SWORD, 1, "Golden Sword"),
            display(Items.GOLD_BLOCK, 8, "Gold Blocks"),
            display(Items.GOLD_INGOT, 4, "Gold Ingots"),
            display(Items.NETHER_STAR, 1, "Nether Star")
        };
        
        openRecipePage(player, pageTitle, 
            Text.literal("⚔ Midas's Sword").formatted(Formatting.YELLOW, Formatting.BOLD),
            ingredients, result, () -> openMainMenu(player));
    }
}
