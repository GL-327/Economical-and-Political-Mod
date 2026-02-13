package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;import eu.pb4.sgui.api.elements.GuiElement;

public class RecipesGui {

    // ============================================================
    // CENTRALIZED RECIPE DEFINITIONS - Single source of truth
    // ============================================================

    public static class BountyRecipe {
        public final String name;
        public final String displayName;
        public final Item displayIcon;
        public final Formatting color;
        public final SlayerManager.SlayerType slayerType;
        public final int requiredLevel;
        public final Ingredient[] ingredients;
        public final String craftingMethod; // "smithing_table"

        public BountyRecipe(String name, String displayName, Item displayIcon, Formatting color,
                            SlayerManager.SlayerType slayerType, int requiredLevel,
                            Ingredient[] ingredients, String craftingMethod) {
            this.name = name;
            this.displayName = displayName;
            this.displayIcon = displayIcon;
            this.color = color;
            this.slayerType = slayerType;
            this.requiredLevel = requiredLevel;
            this.ingredients = ingredients;
            this.craftingMethod = craftingMethod;
        }
    }

    public static class Ingredient {
        public final String name;
        public final Item icon;
        public final int count;
        public final boolean isCore;

        public Ingredient(String name, Item icon, int count, boolean isCore) {
            this.name = name;
            this.icon = icon;
            this.count = count;
            this.isCore = isCore;
        }

        public static Ingredient core(String typeName, Item icon, int count) {
            return new Ingredient(typeName + " Core", icon, count, true);
        }

        public static Ingredient item(String name, Item icon, int count) {
            return new Ingredient(name, icon, count, false);
        }
    }

    // ============================================================
    // ALL BOUNTY ARMOR RECIPES - Matches BountyCraftingHandler.java
    // ============================================================

    public static final BountyRecipe ZOMBIE_BERSERKER_HELMET = new BountyRecipe(
            "zombie_berserker_helmet",
            "☠ Zombie Berserker Helmet",
            Items.ZOMBIE_HEAD,
            Formatting.DARK_GREEN,
            SlayerManager.SlayerType.ZOMBIE,
            SlayerItems.BERSERKER_HELMET_LEVEL_REQ, // 12
            new Ingredient[]{
                    Ingredient.core("Zombie", Items.ROTTEN_FLESH, 5),
                    Ingredient.item("Zombie Head", Items.ZOMBIE_HEAD, 1),
                    Ingredient.item("Rotten Flesh", Items.ROTTEN_FLESH, 3)
            },
            "smithing_table"
    );

    public static final BountyRecipe SPIDER_LEGGINGS = new BountyRecipe(
            "spider_leggings",
            "🕷 Venomous Crawler Leggings",
            Items.LEATHER_LEGGINGS,
            Formatting.DARK_RED,
            SlayerManager.SlayerType.SPIDER,
            SlayerItems.SPIDER_LEGGINGS_LEVEL_REQ, // 12
            new Ingredient[]{
                    Ingredient.core("Spider", Items.SPIDER_EYE, 5),
                    Ingredient.item("String", Items.STRING, 8),
                    Ingredient.item("Spider Eye", Items.SPIDER_EYE, 1)
            },
            "smithing_table"
    );

    public static final BountyRecipe SKELETON_BOW = new BountyRecipe(
            "skeleton_bow",
            "🏹 Bone Desperado's Longbow",
            Items.BOW,
            Formatting.WHITE,
            SlayerManager.SlayerType.SKELETON,
            SlayerItems.SKELETON_BOW_LEVEL_REQ, // 10
            new Ingredient[]{
                    Ingredient.core("Skeleton", Items.BONE, 5),
                    Ingredient.item("Bow", Items.BOW, 1),
                    Ingredient.item("Bone", Items.BONE, 3)
            },
            "smithing_table"
    );

    public static final BountyRecipe SLIME_BOOTS = new BountyRecipe(
            "slime_boots",
            "🥾 Gelatinous Rustler Boots",
            Items.LEATHER_BOOTS,
            Formatting.GREEN,
            SlayerManager.SlayerType.SLIME,
            SlayerItems.SLIME_BOOTS_LEVEL_REQ, // Need to verify this constant exists
            new Ingredient[]{
                    Ingredient.core("Slime", Items.SLIME_BALL, 5),
                    Ingredient.item("Slime Ball", Items.SLIME_BALL, 8),
                    Ingredient.item("Leather Boots", Items.LEATHER_BOOTS, 1)
            },
            "smithing_table"
    );

    public static final BountyRecipe WARDEN_CHESTPLATE = new BountyRecipe(
            "warden_chestplate",
            "💀 Sculk Terror Chestplate",
            Items.NETHERITE_CHESTPLATE,
            Formatting.DARK_AQUA,
            SlayerManager.SlayerType.WARDEN,
            SlayerItems.WARDEN_CHESTPLATE_LEVEL_REQ, // Need to verify this constant exists
            new Ingredient[]{
                    Ingredient.core("Warden", Items.SCULK, 8),
                    Ingredient.item("Netherite Chestplate", Items.NETHERITE_CHESTPLATE, 1),
                    Ingredient.item("Echo Shard", Items.ECHO_SHARD, 4)
            },
            "smithing_table"
    );

    public static final BountyRecipe[] BOUNTY_ARMOR_RECIPES = {
            ZOMBIE_BERSERKER_HELMET,
            SPIDER_LEGGINGS,
            SKELETON_BOW,
            SLIME_BOOTS,
            WARDEN_CHESTPLATE
    };

    // ============================================================
    // MAIN RECIPES MENU
    // ============================================================

    public static void openMainMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("📖 Custom Recipes"));

        fillBackground(gui, Items.GRAY_STAINED_GLASS_PANE);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(Text.literal("📖 Recipe Book").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("View all custom crafting recipes").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("for this server's unique items!").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⚒ Craft at: Smithing Table").formatted(Formatting.YELLOW))
                .glow()
                .build());

        // Category: Bounty Armor (slot 20)
        gui.setSlot(20, new GuiElementBuilder(Items.DIAMOND_CHESTPLATE)
                .setName(Text.literal("🛡 Bounty Armor").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Zombie Berserker Helmet").formatted(Formatting.DARK_GREEN))
                .addLoreLine(Text.literal("Venomous Crawler Leggings").formatted(Formatting.DARK_RED))
                .addLoreLine(Text.literal("Bone Desperado's Longbow").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("Gelatinous Rustler Boots").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("Sculk Terror Chestplate").formatted(Formatting.DARK_AQUA))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to view!").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openBountyArmorRecipes(player))
                .build());

        // Category: HPEBM Weapons (slot 22)
        gui.setSlot(22, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal("⚡ HPEBM Weapons").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("High-Powered Energy").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Beam Weapons (Mk1-Mk5)").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to view!").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openHPEBMRecipes(player))
                .build());

        // Category: Cores & Materials (slot 24)
        gui.setSlot(24, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("✦ Cores & Materials").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("How to obtain bounty").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("crafting materials").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to view!").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openMaterialsInfo(player))
                .build());

        // Close button
        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("✖ Close").formatted(Formatting.RED))
                .setCallback((idx, clickType, action) -> gui.close())
                .build());

        gui.open();
    }

    // ============================================================
    // BOUNTY ARMOR RECIPES PAGE - Dynamically generated
    // ============================================================

    public static void openBountyArmorRecipes(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("🛡 Bounty Armor Recipes"));

        fillBackground(gui, Items.GRAY_STAINED_GLASS_PANE);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.DIAMOND_CHESTPLATE)
                .setName(Text.literal("🛡 Bounty Armor").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal("Click an item to see its recipe!").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⚒ Crafted at: Smithing Table").formatted(Formatting.YELLOW))
                .build());

        // Dynamically place recipe buttons
        int[] slots = {10, 12, 14, 16, 22}; // 5 slots for 5 recipes
        for (int i = 0; i < BOUNTY_ARMOR_RECIPES.length && i < slots.length; i++) {
            BountyRecipe recipe = BOUNTY_ARMOR_RECIPES[i];
            int slot = slots[i];

            int playerLevel = SlayerData.getSlayerLevel(player.getUuidAsString(), recipe.slayerType);
            boolean canCraft = playerLevel >= recipe.requiredLevel;

            GuiElementBuilder builder = new GuiElementBuilder(recipe.displayIcon)
                    .setName(Text.literal(recipe.displayName).formatted(recipe.color, Formatting.BOLD))
                    .addLoreLine(Text.literal(""));

            // Show requirement with color based on whether player meets it
            if (canCraft) {
                builder.addLoreLine(Text.literal("✔ " + recipe.slayerType.displayName + " Bounty Lvl " + recipe.requiredLevel)
                        .formatted(Formatting.GREEN));
            } else {
                builder.addLoreLine(Text.literal("⚠ Requires: " + recipe.slayerType.displayName + " Bounty Lvl " + recipe.requiredLevel)
                        .formatted(Formatting.RED));
                builder.addLoreLine(Text.literal("  Your level: " + playerLevel)
                        .formatted(Formatting.GRAY));
            }

            builder.addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Click to view recipe!").formatted(Formatting.YELLOW));

            final BountyRecipe finalRecipe = recipe;
            builder.setCallback((idx, clickType, action) -> openRecipeDetail(player, finalRecipe));

            gui.setSlot(slot, builder.build());
        }

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openMainMenu(player))
                .build());

        // Close button
        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("✖ Close").formatted(Formatting.RED))
                .setCallback((idx, clickType, action) -> gui.close())
                .build());

        gui.open();
    }

    // ============================================================
    // RECIPE DETAIL PAGE - Shows ingredients
    // ============================================================

    public static void openRecipeDetail(ServerPlayerEntity player, BountyRecipe recipe) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal(recipe.displayName + " Recipe"));

        fillBackground(gui, Items.BLACK_STAINED_GLASS_PANE);

        int playerLevel = SlayerData.getSlayerLevel(player.getUuidAsString(), recipe.slayerType);
        boolean canCraft = playerLevel >= recipe.requiredLevel;

        // Header - Result item
        GuiElementBuilder headerBuilder = new GuiElementBuilder(recipe.displayIcon)
                .setName(Text.literal(recipe.displayName).formatted(recipe.color, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("━━━ RESULT ━━━").formatted(Formatting.GOLD));

        if (canCraft) {
            headerBuilder.addLoreLine(Text.literal("✔ You can craft this!").formatted(Formatting.GREEN));
        } else {
            headerBuilder.addLoreLine(Text.literal("✖ Locked").formatted(Formatting.RED));
        }
        headerBuilder.glow();
        gui.setSlot(4, headerBuilder.build());

        // Crafting method info
        gui.setSlot(13, new GuiElementBuilder(Items.SMITHING_TABLE)
                .setName(Text.literal("⚒ Crafting Method").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Use a Smithing Table").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("with all ingredients in").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("your inventory.").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Right-click the table to craft!").formatted(Formatting.GREEN))
                .build());

        // Level requirement
        gui.setSlot(22, new GuiElementBuilder(canCraft ? Items.LIME_DYE : Items.RED_DYE)
                .setName(Text.literal("⚠ Level Requirement").formatted(canCraft ? Formatting.GREEN : Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Required: " + recipe.slayerType.displayName + " Bounty Lvl " + recipe.requiredLevel)
                        .formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Your Level: " + playerLevel)
                        .formatted(canCraft ? Formatting.GREEN : Formatting.RED))
                .addLoreLine(Text.literal(""))
                .addLoreLine(canCraft
                        ? Text.literal("✔ Requirement met!").formatted(Formatting.GREEN)
                        : Text.literal("✖ Keep grinding bounties!").formatted(Formatting.RED))
                .build());

        // Ingredients display - Row starting at slot 28
        int ingredientSlot = 28;
        for (Ingredient ingredient : recipe.ingredients) {
            // Skip slots if we hit the middle (slot 31 is center)
            if (ingredientSlot == 31) ingredientSlot = 32;

            int playerCount = countPlayerIngredient(player, ingredient);
            boolean hasEnough = playerCount >= ingredient.count;

            GuiElementBuilder ingredientBuilder = new GuiElementBuilder(ingredient.icon)
                    .setCount(Math.min(ingredient.count, 64))
                    .setName(Text.literal(ingredient.name + " x" + ingredient.count)
                            .formatted(ingredient.isCore ? Formatting.LIGHT_PURPLE : Formatting.WHITE, Formatting.BOLD));

            ingredientBuilder.addLoreLine(Text.literal(""));

            if (ingredient.isCore) {
                ingredientBuilder.addLoreLine(Text.literal("✦ Bounty Core").formatted(Formatting.LIGHT_PURPLE));
                ingredientBuilder.addLoreLine(Text.literal("Dropped by bounty bosses").formatted(Formatting.GRAY));
            }

            ingredientBuilder.addLoreLine(Text.literal(""));
            ingredientBuilder.addLoreLine(Text.literal("You have: " + playerCount + "/" + ingredient.count)
                    .formatted(hasEnough ? Formatting.GREEN : Formatting.RED));

            if (ingredient.isCore) {
                ingredientBuilder.glow();
            }

            gui.setSlot(ingredientSlot, ingredientBuilder.build());
            ingredientSlot++;
        }

        // Arrow pointing to result
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("▶ Creates").formatted(Formatting.WHITE))
                .build());

        // Result preview in bottom right area
        gui.setSlot(34, new GuiElementBuilder(recipe.displayIcon)
                .setName(Text.literal(recipe.displayName).formatted(recipe.color, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("The crafting result").formatted(Formatting.GRAY))
                .glow()
                .build());

        // Ingredient checklist summary
        StringBuilder checklistText = new StringBuilder();
        boolean allMet = true;
        for (Ingredient ingredient : recipe.ingredients) {
            int have = countPlayerIngredient(player, ingredient);
            boolean met = have >= ingredient.count;
            if (!met) allMet = false;
        }

        gui.setSlot(40, new GuiElementBuilder(allMet && canCraft ? Items.LIME_CONCRETE : Items.RED_CONCRETE)
                .setName(Text.literal(allMet && canCraft ? "✔ Ready to Craft!" : "✖ Missing Requirements")
                        .formatted(allMet && canCraft ? Formatting.GREEN : Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(allMet && canCraft
                        ? Text.literal("Go to a Smithing Table!").formatted(Formatting.GREEN)
                        : Text.literal("Gather all ingredients first.").formatted(Formatting.GRAY))
                .build());

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openBountyArmorRecipes(player))
                .build());

        // Close button
        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("✖ Close").formatted(Formatting.RED))
                .setCallback((idx, clickType, action) -> gui.close())
                .build());

        gui.open();
    }

    // ============================================================
    // HELPER: Count player's ingredient
    // ============================================================

    private static int countPlayerIngredient(ServerPlayerEntity player, Ingredient ingredient) {
        int count = 0;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;

            if (ingredient.isCore) {
                // Check for core by custom name
                Text name = stack.get(net.minecraft.component.DataComponentTypes.CUSTOM_NAME);
                if (name != null && name.getString().equals(ingredient.name)) {
                    count += stack.getCount();
                }
            } else {
                // Check by item type
                if (stack.isOf(ingredient.icon)) {
                    count += stack.getCount();
                }
            }
        }
        return count;
    }

    // ============================================================
    // HPEBM RECIPES PAGE
    // ============================================================

    public static void openHPEBMRecipes(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⚡ HPEBM Weapon Recipes"));

        fillBackground(gui, Items.GRAY_STAINED_GLASS_PANE);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal("⚡ HPEBM Weapons").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("High-Powered Energy Beam Weapons").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Upgrade through tiers using").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("the Crafting Table.").formatted(Formatting.GRAY))
                .glow()
                .build());

        // Base HPEBM - Obtained from shop
        gui.setSlot(10, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal("HPEBM (Base)").formatted(Formatting.WHITE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("━━━ OBTAIN ━━━").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal("Purchase from the Shop").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Damage: 5").formatted(Formatting.RED))
                .build());

        // Upgrade tiers 1-5
        String[] tierNames = {"Ultra HPEBM", "Ultra HPEBM Mk1", "Ultra HPEBM Mk2", "Ultra HPEBM Mk3", "Ultra HPEBM Mk4", "Ultra HPEBM Mk5"};
        Formatting[] tierColors = {Formatting.WHITE, Formatting.WHITE, Formatting.GREEN, Formatting.YELLOW, Formatting.GOLD, Formatting.LIGHT_PURPLE};
        int[] tierDamage = {10, 15, 20, 25, 30, 35};
        int[] slots = {12, 14, 16, 28, 30, 32};

        for (int i = 0; i < tierNames.length && i < slots.length; i++) {
            final int tier = i;
            gui.setSlot(slots[i], new GuiElementBuilder(Items.END_ROD)
                    .setName(Text.literal(tierNames[i]).formatted(tierColors[i], Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("━━━ UPGRADE RECIPE ━━━").formatted(Formatting.GOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("• Nether Star x6").formatted(Formatting.AQUA))
                    .addLoreLine(Text.literal("• Warden Core x1").formatted(Formatting.LIGHT_PURPLE))
                    .addLoreLine(Text.literal("• Dragon's Breath x1").formatted(Formatting.DARK_PURPLE))
                    .addLoreLine(Text.literal("• Previous Tier Weapon").formatted(Formatting.WHITE))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Damage: " + tierDamage[i]).formatted(Formatting.RED))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("⚒ Craft at: Crafting Table").formatted(Formatting.YELLOW))
                    .setCallback((idx, clickType, action) -> openHPEBMUpgradeDetail(player, tier))
                    .build());
        }

        // Ultra Overclocked (final tier)
        gui.setSlot(34, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal("Ultra Overclocked HPEBM").formatted(Formatting.DARK_RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("━━━ FINAL UPGRADE ━━━").formatted(Formatting.DARK_RED))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("• Nether Star x3").formatted(Formatting.AQUA))
                .addLoreLine(Text.literal("• Warden Core x5").formatted(Formatting.LIGHT_PURPLE))
                .addLoreLine(Text.literal("• Ultra HPEBM Mk5").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Damage: 50").formatted(Formatting.RED))
                .addLoreLine(Text.literal("Special: AOE Explosion").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⚒ Craft at: Crafting Table").formatted(Formatting.YELLOW))
                .glow()
                .build());

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openMainMenu(player))
                .build());

        // Close button
        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("✖ Close").formatted(Formatting.RED))
                .setCallback((idx, clickType, action) -> gui.close())
                .build());

        gui.open();
    }

    // ============================================================
    // HPEBM UPGRADE DETAIL - Shows crafting grid
    // ============================================================

    public static void openHPEBMUpgradeDetail(ServerPlayerEntity player, int tier) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);

        String[] tierNames = {"Ultra HPEBM", "Ultra HPEBM Mk1", "Ultra HPEBM Mk2", "Ultra HPEBM Mk3", "Ultra HPEBM Mk4", "Ultra HPEBM Mk5"};
        String[] prevTierNames = {"HPEBM (Base)", "Ultra HPEBM", "Ultra HPEBM Mk1", "Ultra HPEBM Mk2", "Ultra HPEBM Mk3", "Ultra HPEBM Mk4"};
        String tierName = tier < tierNames.length ? tierNames[tier] : "Unknown";
        String prevTierName = tier < prevTierNames.length ? prevTierNames[tier] : "Previous Tier";

        gui.setTitle(Text.literal(tierName + " Recipe"));

        fillBackground(gui, Items.BLACK_STAINED_GLASS_PANE);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal(tierName).formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⚒ Crafting Table Recipe").formatted(Formatting.YELLOW))
                .glow()
                .build());

        // 3x3 Crafting Grid Display
        // Grid positions: 10,11,12 / 19,20,21 / 28,29,30

        // Row 1: [Star] [Warden Core] [Star]
        gui.setSlot(10, createGridItem(Items.NETHER_STAR, "Nether Star", Formatting.AQUA).build());
        gui.setSlot(11, createCoreGridItem("Warden Core", Items.SCULK, Formatting.DARK_AQUA).build());
        gui.setSlot(12, createGridItem(Items.NETHER_STAR, "Nether Star", Formatting.AQUA).build());


        // Row 2: [Star] [Previous Beam] [Star]
        gui.setSlot(19, createGridItem(Items.NETHER_STAR, "Nether Star", Formatting.AQUA).build());
        gui.setSlot(20, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal(prevTierName).formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("The previous tier weapon").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Place in center of grid").formatted(Formatting.GRAY))
                .glow()
                .build());
        gui.setSlot(21, createGridItem(Items.NETHER_STAR, "Nether Star", Formatting.AQUA).build());

        // Row 3: [Star] [Dragon Breath] [Star]
        gui.setSlot(28, createGridItem(Items.NETHER_STAR, "Nether Star", Formatting.AQUA).build());
        gui.setSlot(29, createGridItem(Items.DRAGON_BREATH, "Dragon's Breath", Formatting.DARK_PURPLE).build());
        gui.setSlot(30, createGridItem(Items.NETHER_STAR, "Nether Star", Formatting.AQUA).build());

        // Arrow
        gui.setSlot(23, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("▶ Creates").formatted(Formatting.WHITE))
                .build());

        // Result
        gui.setSlot(25, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal(tierName).formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Damage: " + (10 + tier * 5)).formatted(Formatting.RED))
                .glow()
                .build());

        // Ingredient summary
        gui.setSlot(40, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("📋 Ingredients Summary").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("• Nether Star x6").formatted(Formatting.AQUA))
                .addLoreLine(Text.literal("• Warden Core x1").formatted(Formatting.DARK_AQUA))
                .addLoreLine(Text.literal("• Dragon's Breath x1").formatted(Formatting.DARK_PURPLE))
                .addLoreLine(Text.literal("• " + prevTierName + " x1").formatted(Formatting.LIGHT_PURPLE))
                .build());

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openHPEBMRecipes(player))
                .build());

        // Close button
        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("✖ Close").formatted(Formatting.RED))
                .setCallback((idx, clickType, action) -> gui.close())
                .build());

        gui.open();
    }

    // ============================================================
    // MATERIALS INFO PAGE - How to get cores
    // ============================================================

    public static void openMaterialsInfo(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("✦ Cores & Materials"));

        fillBackground(gui, Items.GRAY_STAINED_GLASS_PANE);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("✦ Bounty Cores").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Cores are rare drops from").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("defeating Bounty Bosses!").formatted(Formatting.GRAY))
                .glow()
                .build());

        // Core types
        SlayerManager.SlayerType[] types = SlayerManager.SlayerType.values();
        int[] coreSlots = {10, 12, 14, 16, 28, 30};

        for (int i = 0; i < types.length && i < coreSlots.length; i++) {
            SlayerManager.SlayerType type = types[i];

            gui.setSlot(coreSlots[i], new GuiElementBuilder(type.icon)
                    .setName(Text.literal(type.displayName + " Core").formatted(type.color, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("━━━ HOW TO OBTAIN ━━━").formatted(Formatting.GOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("1. Start a " + type.displayName + " Bounty").formatted(Formatting.WHITE))
                    .addLoreLine(Text.literal("   Use /bounty " + type.displayName.toLowerCase()).formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("2. Kill " + type.displayName + "s").formatted(Formatting.WHITE))
                    .addLoreLine(Text.literal("   Fill the kill requirement").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("3. Defeat " + type.bossName).formatted(Formatting.WHITE))
                    .addLoreLine(Text.literal("   The boss spawns when ready").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("4. Collect Core Drop!").formatted(Formatting.GREEN))
                    .addLoreLine(Text.literal("   Higher tiers = more cores").formatted(Formatting.GRAY))
                    .glow()
                    .build());
        }

        // Tips section
        gui.setSlot(22, new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Text.literal("💡 Tips").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("• Higher tier bounties drop").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("  more cores per boss kill").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("• Use Bounty Swords for 2x").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("  damage against bosses").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("• Level up your bounty skill").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("  to unlock higher tiers").formatted(Formatting.GRAY))
                .build());

        // Other materials
        gui.setSlot(32, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("Nether Star").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Obtained by killing the Wither").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Used for HPEBM upgrades").formatted(Formatting.YELLOW))
                .build());

        gui.setSlot(34, new GuiElementBuilder(Items.DRAGON_BREATH)
                .setName(Text.literal("Dragon's Breath").formatted(Formatting.DARK_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Collected from Ender Dragon").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("breath attacks using bottles").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Used for HPEBM upgrades").formatted(Formatting.YELLOW))
                .build());

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openMainMenu(player))
                .build());

        // Close button
        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("✖ Close").formatted(Formatting.RED))
                .setCallback((idx, clickType, action) -> gui.close())
                .build());

        gui.open();
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    private static void fillBackground(SimpleGui gui, Item item) {
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(item)
                    .setName(Text.literal(""))
                    .hideTooltip()
                    .build());
        }
    }

    private static GuiElementBuilder createGridItem(Item item, String name, Formatting color) {
        return new GuiElementBuilder(item)
                .setName(Text.literal(name).formatted(color))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Required ingredient").formatted(Formatting.GRAY));
    }

    private static GuiElementBuilder createCoreGridItem(String coreName, Item icon, Formatting color) {
        return new GuiElementBuilder(icon)
                .setName(Text.literal(coreName).formatted(color, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✦ Bounty Core").formatted(Formatting.LIGHT_PURPLE))
                .addLoreLine(Text.literal("Dropped by bounty bosses").formatted(Formatting.GRAY))
                .glow();
    }

    private static GuiElementBuilder emptySlot() {
        return new GuiElementBuilder(Items.LIGHT_GRAY_STAINED_GLASS_PANE)
                .setName(Text.literal("Empty Slot").formatted(Formatting.DARK_GRAY));
    }
}