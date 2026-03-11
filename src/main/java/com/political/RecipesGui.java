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
    // MAIN MENU
    // ============================================================
    public static void openMainMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("📖 Custom Recipes"));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        gui.setSlot(4, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(Text.literal("📖 Recipe Book").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal("View all custom crafting recipes").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click a category to browse!").formatted(Formatting.GRAY))
                .glow().build());

        gui.setSlot(19, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Text.literal("⚔ Bounty Swords (T1)").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal("").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("3 Cores + Iron Sword + 2 Sticks").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click to view!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openBountySwordsT1(player)).build());

        gui.setSlot(20, new GuiElementBuilder(Items.DIAMOND_SWORD)
                .setName(Text.literal("⚔ Bounty Swords (T2)").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal("").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("5 Cores + Diamond Sword + T1 Sword").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click to view!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openBountySwordsT2(player)).build());

        gui.setSlot(21, new GuiElementBuilder(Items.DIAMOND_SHOVEL)
                .setName(Text.literal("⚡ HPEBM Weapons").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal("").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("High-Powered Energy Beam Weapons").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click to view!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openHPEBMRecipes(player)).build());

        gui.setSlot(23, new GuiElementBuilder(Items.LEATHER_CHESTPLATE)
                .setName(Text.literal("🛡 Bounty Armor").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal("").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Berserker Helmet, Venomous Leggings,").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Slime Boots, Warden Chestplate, Bow").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click to view!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openArmorRecipes(player)).build());

        gui.setSlot(25, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("✦ Auction Items").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal("").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("⚠ HPEBM T0 & The Gavel are obtained").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("ONLY from Underground Auctions!").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Click to view HPEBM upgrades!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openHPEBMRecipes(player)).build());

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("✖ Close").formatted(Formatting.RED))
                .setCallback((idx, type, action) -> player.closeHandledScreen()).build());

        gui.open();
    }

    // ============================================================
    // ADMIN CUSTOM RECIPES (from /setrecipe) - kept for backward compat
    // ============================================================
    public static void openCustomRecipes(ServerPlayerEntity player) {
        openMainMenu(player); // redirect to main menu
    }

    // ============================================================
    // BOUNTY SWORDS T1 - Category list
    // ============================================================
    public static void openBountySwordsT1(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⚔ Bounty Swords T1"));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        gui.setSlot(4, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Text.literal("⚔ Bounty Swords (T1)").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal("Recipe: 3 Cores + Iron Sword + 2 Sticks").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Requires: Bounty Level 3").formatted(Formatting.RED))
                .glow().build());

        int[] slots = {19, 20, 21, 22, 23, 24};
        SlayerManager.SlayerType[] types = SlayerManager.SlayerType.values();
        for (int i = 0; i < Math.min(types.length, slots.length); i++) {
            final SlayerManager.SlayerType type = types[i];
            ItemStack sword = SlayerItems.createSlayerSword(type);
            Text swordName = sword.get(DataComponentTypes.CUSTOM_NAME);
            GuiElementBuilder eb = new GuiElementBuilder(Items.IRON_SWORD)
                    .setName(swordName != null ? swordName : Text.literal(type.displayName + " Sword").formatted(type.color, Formatting.BOLD))
                    .addLoreLine(Text.literal("━━━ RECIPE ━━━").formatted(Formatting.GOLD))
                    .addLoreLine(Text.literal("• 3x " + type.displayName + " Core").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("• 1x Iron Sword").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("• 2x Stick").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("⚠ Requires Lvl 3").formatted(Formatting.RED))
                    .addLoreLine(Text.literal("Click to view crafting grid!").formatted(Formatting.GREEN))
                    .setCallback((idx, clickType, action) -> openT1SwordRecipe(player, type));
            gui.setSlot(slots[i], eb.build());
        }

        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player)).build());

        gui.open();
    }

    private static void openT1SwordRecipe(ServerPlayerEntity player, SlayerManager.SlayerType type) {
        ItemStack result = SlayerItems.createSlayerSword(type);
        String pageTitle = "⚔ " + type.displayName + " Sword";
        // Item #8: show admin-configured recipe if one exists for this result
        if (tryOpenCustomRecipe(player, result, pageTitle, () -> openBountySwordsT1(player))) return;

        ItemStack core = SlayerItems.createCore(type);
        // T1 Sword grid: 3 Cores + 1 Iron Sword + 2 Sticks
        // Layout: [Core, _, Core] / [ISword, Core, _] / [Stick, _, Stick]
        ItemStack[] grid = {
                core.copy(),          ItemStack.EMPTY, core.copy(),
                new ItemStack(Items.IRON_SWORD), core.copy(), ItemStack.EMPTY,
                new ItemStack(Items.STICK), ItemStack.EMPTY, new ItemStack(Items.STICK)
        };
        Text headerName = Text.literal("⚔ " + type.displayName + " Bounty Sword Recipe").formatted(type.color, Formatting.BOLD);
        openRecipePage(player, pageTitle, headerName, grid, result,
                () -> openBountySwordsT1(player));
    }

    // ============================================================
    // BOUNTY SWORDS T2 - Category list
    // ============================================================
    public static void openBountySwordsT2(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⚔ Bounty Swords T2"));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.YELLOW_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        gui.setSlot(4, new GuiElementBuilder(Items.DIAMOND_SWORD)
                .setName(Text.literal("⚔ Bounty Swords (T2)").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal("Recipe: 3 Cores + Diamond Sword + T1 Sword").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Requires: Bounty Level 6").formatted(Formatting.RED))
                .glow().build());

        int[] slots = {19, 20, 21, 22, 23, 24};
        SlayerManager.SlayerType[] types = SlayerManager.SlayerType.values();
        for (int i = 0; i < Math.min(types.length, slots.length); i++) {
            final SlayerManager.SlayerType type = types[i];
            ItemStack sword = SlayerItems.createUpgradedSlayerSword(type);
            Text swordName = sword.get(DataComponentTypes.CUSTOM_NAME);
            GuiElementBuilder eb = new GuiElementBuilder(Items.DIAMOND_SWORD)
                    .setName(swordName != null ? swordName : Text.literal(type.displayName + " Sword II").formatted(type.color, Formatting.BOLD))
                    .addLoreLine(Text.literal("━━━ RECIPE ━━━").formatted(Formatting.GOLD))
                    .addLoreLine(Text.literal("• 3x " + type.displayName + " Core").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("• 1x Diamond Sword").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("• 1x " + type.displayName + " Bounty Sword").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("⚠ Requires Lvl 6").formatted(Formatting.RED))
                    .addLoreLine(Text.literal("Click to view crafting grid!").formatted(Formatting.GREEN))
                    .setCallback((idx, clickType, action) -> openT2SwordRecipe(player, type));
            gui.setSlot(slots[i], eb.build());
        }

        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player)).build());

        gui.open();
    }

    private static void openT2SwordRecipe(ServerPlayerEntity player, SlayerManager.SlayerType type) {
        ItemStack result = SlayerItems.createUpgradedSlayerSword(type);
        String pageTitle = "⚔ T2 " + type.displayName + " Sword";
        // Item #8: show admin-configured recipe if one exists for this result
        if (tryOpenCustomRecipe(player, result, pageTitle, () -> openBountySwordsT2(player))) return;

        ItemStack core = SlayerItems.createCore(type);
        ItemStack t1Sword = SlayerItems.createSlayerSword(type);
        // T2 Sword grid: 3 Cores + 1 Diamond Sword + 1 T1 Sword = 5 items
        // Layout: [Core, Core, Core] / [DSword, T1, _] / [_, _, _]
        ItemStack[] grid = {
                core.copy(),                       core.copy(),  core.copy(),
                new ItemStack(Items.DIAMOND_SWORD), t1Sword,     ItemStack.EMPTY,
                ItemStack.EMPTY,                   ItemStack.EMPTY,  ItemStack.EMPTY
        };
        Text headerName = Text.literal("⚔ " + type.displayName + " Bounty Sword II Recipe").formatted(type.color, Formatting.BOLD);
        openRecipePage(player, pageTitle, headerName, grid, result,
                () -> openBountySwordsT2(player));
    }

    // ============================================================
    // BOUNTY ARMOR RECIPES - Category list
    // ============================================================
    public static void openArmorRecipes(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("🛡 Bounty Armor Recipes"));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.CYAN_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        gui.setSlot(4, new GuiElementBuilder(Items.LEATHER_CHESTPLATE)
                .setName(Text.literal("🛡 Bounty Armor Recipes").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal("Click an item to see its recipe").formatted(Formatting.GRAY))
                .glow().build());

        // Zombie Berserker Helmet
        gui.setSlot(19, new GuiElementBuilder(Items.LEATHER_HELMET)
                .setName(Text.literal("💀 Zombie Berserker Helmet").formatted(Formatting.DARK_GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal("3 Zombie Cores + 2 Rotten Flesh + Zombie Head").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("⚠ Requires: Zombie Lvl 5").formatted(Formatting.RED))
                .addLoreLine(Text.literal("Click to see recipe!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openZombieHelmetRecipe(player)).build());

        // Spider Venomous Leggings
        gui.setSlot(20, new GuiElementBuilder(Items.LEATHER_LEGGINGS)
                .setName(Text.literal("🕷 Venomous Crawler Leggings").formatted(Formatting.DARK_RED, Formatting.BOLD))
                .addLoreLine(Text.literal("3 Spider Cores + 2 String + 1 Spider Eye").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("⚠ Requires: Spider Lvl " + SlayerItems.SPIDER_LEGGINGS_LEVEL_REQ).formatted(Formatting.RED))
                .addLoreLine(Text.literal("Click to see recipe!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openSpiderLeggingsRecipe(player)).build());

        // Skeleton Bow
        gui.setSlot(21, new GuiElementBuilder(Items.BOW)
                .setName(Text.literal("🏹 Bone Desperado's Longbow").formatted(Formatting.WHITE, Formatting.BOLD))
                .addLoreLine(Text.literal("3 Skeleton Cores + 1 Bow + 2 Bones").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("⚠ Requires: Skeleton Lvl 10").formatted(Formatting.RED))
                .addLoreLine(Text.literal("Click to see recipe!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openSkeletonBowRecipe(player)).build());

        // Slime Boots
        gui.setSlot(22, new GuiElementBuilder(Items.LEATHER_BOOTS)
                .setName(Text.literal("🟢 Gelatinous Rustler Boots").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal("3 Slime Cores + 2 Slime Ball + 1 Leather Boots").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("⚠ Requires: Slime Lvl " + SlayerItems.SLIME_BOOTS_LEVEL_REQ).formatted(Formatting.RED))
                .addLoreLine(Text.literal("Click to see recipe!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openSlimeBootsRecipe(player)).build());

        // Warden Chestplate
        gui.setSlot(23, new GuiElementBuilder(Items.NETHERITE_CHESTPLATE)
                .setName(Text.literal("💀 Sculk Terror Chestplate").formatted(Formatting.DARK_AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal("3 Warden Cores + 2 Echo Shards + Neth. Chestplate").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("⚠ Requires: Warden Lvl " + SlayerItems.WARDEN_CHESTPLATE_LEVEL_REQ).formatted(Formatting.RED))
                .addLoreLine(Text.literal("Click to see recipe!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openWardenChestplateRecipe(player)).build());

        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player)).build());

        gui.open();
    }

    private static void openZombieHelmetRecipe(ServerPlayerEntity player) {
        ItemStack result = SlayerItems.createZombieBerserkerHelmet();
        String pageTitle = "💀 Zombie Berserker Helmet";
        if (tryOpenCustomRecipe(player, result, pageTitle, () -> openArmorRecipes(player))) return;

        ItemStack core = SlayerItems.createCore(SlayerManager.SlayerType.ZOMBIE);
        // 3 Zombie Cores + 2 Rotten Flesh + 1 Zombie Head = 6 items
        // Grid: [Core, Core, Core] / [Flesh, Head, Flesh] / [_, _, _]
        ItemStack[] grid = {
                core.copy(), core.copy(), core.copy(),
                new ItemStack(Items.ROTTEN_FLESH), new ItemStack(Items.ZOMBIE_HEAD), new ItemStack(Items.ROTTEN_FLESH),
                ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
        };
        openRecipePage(player, "💀 Zombie Berserker Helmet",
                Text.literal("💀 Zombie Berserker Helmet Recipe").formatted(Formatting.DARK_GREEN, Formatting.BOLD),
                grid, result, () -> openArmorRecipes(player));
    }

    private static void openSpiderLeggingsRecipe(ServerPlayerEntity player) {
        ItemStack result = SlayerItems.createSpiderLeggings();
        String pageTitle = "🕷 Venomous Crawler Leggings";
        if (tryOpenCustomRecipe(player, result, pageTitle, () -> openArmorRecipes(player))) return;

        ItemStack core = SlayerItems.createCore(SlayerManager.SlayerType.SPIDER);
        // 3 Spider Cores + 2 String + 1 Spider Eye = 6 items
        // Grid: [Core, Core, Core] / [String, Eye, String] / [_, _, _]
        ItemStack[] grid = {
                core.copy(), core.copy(), core.copy(),
                new ItemStack(Items.STRING), new ItemStack(Items.SPIDER_EYE), new ItemStack(Items.STRING),
                ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
        };
        openRecipePage(player, "🕷 Venomous Crawler Leggings",
                Text.literal("🕷 Venomous Crawler Leggings Recipe").formatted(Formatting.DARK_RED, Formatting.BOLD),
                grid, result, () -> openArmorRecipes(player));
    }

    private static void openSkeletonBowRecipe(ServerPlayerEntity player) {
        ItemStack result = SlayerItems.createSkeletonBow();
        String pageTitle = "🏹 Bone Desperado's Longbow";
        if (tryOpenCustomRecipe(player, result, pageTitle, () -> openArmorRecipes(player))) return;

        ItemStack core = SlayerItems.createCore(SlayerManager.SlayerType.SKELETON);
        // 3 Skeleton Cores + 1 Bow + 2 Bones = 6 items
        // Grid: [Core, Core, Core] / [Bone, Bow, Bone] / [_, _, _]
        ItemStack[] grid = {
                core.copy(), core.copy(), core.copy(),
                new ItemStack(Items.BONE), new ItemStack(Items.BOW), new ItemStack(Items.BONE),
                ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
        };
        openRecipePage(player, "🏹 Bone Desperado's Longbow",
                Text.literal("🏹 Bone Desperado's Longbow Recipe").formatted(Formatting.WHITE, Formatting.BOLD),
                grid, result, () -> openArmorRecipes(player));
    }

    private static void openSlimeBootsRecipe(ServerPlayerEntity player) {
        ItemStack result = SlayerItems.createSlimeBoots();
        String pageTitle = "🟢 Gelatinous Rustler Boots";
        if (tryOpenCustomRecipe(player, result, pageTitle, () -> openArmorRecipes(player))) return;

        ItemStack core = SlayerItems.createCore(SlayerManager.SlayerType.SLIME);
        // 3 Slime Cores + 2 Slime Balls + 1 Leather Boots = 6 items
        // Grid: [Core, Core, Core] / [SBall, Boots, SBall] / [_, _, _]
        ItemStack[] grid = {
                core.copy(), core.copy(), core.copy(),
                new ItemStack(Items.SLIME_BALL), new ItemStack(Items.LEATHER_BOOTS), new ItemStack(Items.SLIME_BALL),
                ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
        };
        openRecipePage(player, "🟢 Gelatinous Rustler Boots",
                Text.literal("🟢 Gelatinous Rustler Boots Recipe").formatted(Formatting.GREEN, Formatting.BOLD),
                grid, result, () -> openArmorRecipes(player));
    }

    private static void openWardenChestplateRecipe(ServerPlayerEntity player) {
        ItemStack result = SlayerItems.createWardenChestplate();
        String pageTitle = "💀 Sculk Terror Chestplate";
        if (tryOpenCustomRecipe(player, result, pageTitle, () -> openArmorRecipes(player))) return;

        ItemStack core = SlayerItems.createCore(SlayerManager.SlayerType.WARDEN);
        // 3 Warden Cores + 2 Echo Shards + 1 Netherite Chestplate = 6 items
        // Grid: [Core, Core, Core] / [EShard, Chest, EShard] / [_, _, _]
        ItemStack[] grid = {
                core.copy(), core.copy(), core.copy(),
                new ItemStack(Items.ECHO_SHARD), new ItemStack(Items.NETHERITE_CHESTPLATE), new ItemStack(Items.ECHO_SHARD),
                ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
        };
        openRecipePage(player, "💀 Sculk Terror Chestplate",
                Text.literal("💀 Sculk Terror Chestplate Recipe").formatted(Formatting.DARK_AQUA, Formatting.BOLD),
                grid, result, () -> openArmorRecipes(player));
    }

    // ============================================================
    // SPECIAL ITEMS RECIPES - Category list
    // ============================================================
    public static void openSpecialRecipes(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("✦ Special Item Recipes"));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("✦ Special Item Recipes").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal("Click an item to see its recipe").formatted(Formatting.GRAY))
                .glow().build());

        // The Gavel - Auction Only
        gui.setSlot(21, new GuiElementBuilder(Items.WOODEN_AXE)
                .setName(Text.literal("⚖ The Gavel").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal("⚠ UNDERGROUND AUCTION ONLY").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal("Cannot be crafted!").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Obtainable from Underground Auctions at T1.").formatted(Formatting.GRAY))
                .build());

        // Base HPEBM - Auction Only
        gui.setSlot(22, new GuiElementBuilder(Items.DIAMOND_SHOVEL)
                .setName(Text.literal("⚡ HPEBM Mk1 (Base)").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal("⚠ UNDERGROUND AUCTION ONLY").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal("Cannot be crafted!").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Obtainable from Underground Auctions at T1.").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Upgrades: right-click Smithing Table").formatted(Formatting.GRAY))
                .build());

        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player)).build());

        gui.open();
    }

    private static void openGavelRecipe(ServerPlayerEntity player) {
        ItemStack core = SlayerItems.createCore(SlayerManager.SlayerType.WARDEN);
        // 2 Warden Cores + 2 Netherite Ingots + 1 Wooden Axe
        // Grid: [_, Core, _] / [Axe, NIngot, Core] / [_, NIngot, _]
        ItemStack[] grid = {
                ItemStack.EMPTY, core.copy(), ItemStack.EMPTY,
                new ItemStack(Items.WOODEN_AXE), new ItemStack(Items.NETHERITE_INGOT), core.copy(),
                ItemStack.EMPTY, new ItemStack(Items.NETHERITE_INGOT), ItemStack.EMPTY
        };
        ItemStack result = CustomItemHandler.createTheGavel();
        openRecipePage(player, "⚖ The Gavel",
                Text.literal("⚖ The Gavel Recipe").formatted(Formatting.GOLD, Formatting.BOLD),
                grid, result, () -> openSpecialRecipes(player));
    }


    // ============================================================
    // HPEBM UPGRADE RECIPES
    // ============================================================
    public static void openHPEBMRecipes(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⚡ HPEBM Upgrade Recipes"));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.LIGHT_BLUE_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        gui.setSlot(4, new GuiElementBuilder(Items.DIAMOND_SHOVEL)
                .setName(Text.literal("⚡ HPEBM Upgrade Recipes").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal("⚠ HPEBM T0 (Base) is obtained from").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Underground Auctions ONLY!").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Upgrades: Crafting Table or Smithing Table").formatted(Formatting.GRAY))
                .glow().build());

        // Mk1 → Mk2
        gui.setSlot(19, new GuiElementBuilder(Items.IRON_BLOCK)
                .setName(Text.literal("⚡ Mk1 → Mk2").formatted(Formatting.WHITE, Formatting.BOLD))
                .addLoreLine(Text.literal("4x Iron Block + 2x Redstone Block").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("+ HPEBM Mk1 in main hand").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Right-click Smithing Table!").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Click to see crafting grid!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openHPEBMUpgradeRecipe(player, 2,
                        Items.IRON_BLOCK, 4, Items.REDSTONE_BLOCK, 2)).build());

        // Mk2 → Mk3
        gui.setSlot(20, new GuiElementBuilder(Items.DIAMOND_BLOCK)
                .setName(Text.literal("⚡ Mk2 → Mk3").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal("4x Diamond Block + 4x Redstone Block").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("+ HPEBM Mk2 in main hand").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Right-click Smithing Table!").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Click to see crafting grid!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openHPEBMUpgradeRecipe(player, 3,
                        Items.DIAMOND_BLOCK, 4, Items.REDSTONE_BLOCK, 4)).build());

        // Mk3 → Mk4
        gui.setSlot(21, new GuiElementBuilder(Items.NETHERITE_INGOT)
                .setName(Text.literal("⚡ Mk3 → Mk4").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal("2x Netherite Ingot + 4x Glowstone").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("+ HPEBM Mk3 in main hand").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Right-click Smithing Table!").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Click to see crafting grid!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openHPEBMUpgradeRecipe(player, 4,
                        Items.NETHERITE_INGOT, 2, Items.GLOWSTONE, 4)).build());

        // Mk4 → Mk5
        gui.setSlot(22, new GuiElementBuilder(Items.GLOWSTONE)
                .setName(Text.literal("⚡ Mk4 → Mk5").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal("4x Netherite Ingot + 8x Glowstone").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("+ HPEBM Mk4 in main hand").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Right-click Smithing Table!").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Click to see crafting grid!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openHPEBMUpgradeRecipe(player, 5,
                        Items.NETHERITE_INGOT, 4, Items.GLOWSTONE, 8)).build());

        // Mk5 → Ultra OC
        gui.setSlot(23, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("✦ Mk5 → Ultra Overclocked").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal("2x Warden Core + 2x Nether Star").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("+ HPEBM Mk5 in main hand").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Right-click Smithing Table!").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Click to see crafting grid!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openHPEBMUltraRecipe(player)).build());

        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player)).build());

        gui.open();
    }

    private static void openHPEBMUpgradeRecipe(ServerPlayerEntity player, int targetTier,
                                                net.minecraft.item.Item mat1, int count1,
                                                net.minecraft.item.Item mat2, int count2) {
        // Show the ingredients needed for upgrade (displayed in crafting grid for visual purposes)
        // targetTier is the Mk number of the resulting beam (e.g., 2 = Mk2, 3 = Mk3, etc.)
        ItemStack mat1Stack = new ItemStack(mat1, count1);
        ItemStack mat2Stack = new ItemStack(mat2, count2);
        // Grid: [mat1, mat1, mat2] / [mat1, previousBeam, mat2] / [mat1, mat2, mat2]
        ItemStack previousBeam = CustomItemHandler.createUltraBeamMk(targetTier - 1);
        ItemStack[] grid = {
                mat1Stack.copy(), mat1Stack.copy(), mat2Stack.copy(),
                mat1Stack.copy(), previousBeam,     mat2Stack.copy(),
                mat1Stack.copy(), mat2Stack.copy(),  mat2Stack.copy()
        };
        // Trim counts to fit (display purposes only)
        grid[0].setCount(Math.min(count1, 16));
        grid[1].setCount(Math.min(count1, 16));
        grid[3].setCount(Math.min(count1, 16));
        grid[6].setCount(Math.min(count1, 16));
        grid[2].setCount(Math.min(count2, 16));
        grid[5].setCount(Math.min(count2, 16));
        grid[7].setCount(Math.min(count2, 16));
        grid[8].setCount(Math.min(count2, 16));

        ItemStack result = CustomItemHandler.createUltraBeamMk(targetTier);
        // targetTier IS the Mk number of the result (e.g., targetTier=2 → "Mk2")
        String tierName = "Mk" + targetTier;
        openRecipePage(player, "⚡ HPEBM " + tierName,
                Text.literal("⚡ HPEBM → " + tierName + " Upgrade (Smithing Table)").formatted(Formatting.AQUA, Formatting.BOLD),
                grid, result, () -> openHPEBMRecipes(player));
    }

    private static void openHPEBMUltraRecipe(ServerPlayerEntity player) {
        ItemStack wardenCore = SlayerItems.createCore(SlayerManager.SlayerType.WARDEN);
        ItemStack mk5 = CustomItemHandler.createUltraBeamMk(5);
        // 2 Warden Cores + 2 Nether Stars + Mk5
        // Grid: [Core, Star, Core] / [Star, Mk5, _] / [_, _, _]
        ItemStack[] grid = {
                wardenCore.copy(), new ItemStack(Items.NETHER_STAR), wardenCore.copy(),
                new ItemStack(Items.NETHER_STAR), mk5, ItemStack.EMPTY,
                ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
        };
        ItemStack result = CustomItemHandler.createUltraOverclockedBeam();
        openRecipePage(player, "✦ Ultra Overclocked HPEBM",
                Text.literal("✦ Ultra Overclocked HPEBM (Smithing Table)").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD),
                grid, result, () -> openHPEBMRecipes(player));
    }
}
