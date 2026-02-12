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

public class RecipesGui {

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
                .glow()
                .build());

        // Category: Bounty Armor (slot 20)
        gui.setSlot(20, new GuiElementBuilder(Items.DIAMOND_CHESTPLATE)
                .setName(Text.literal("🛡 Bounty Armor").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Zombie Berserker Helmet").formatted(Formatting.DARK_GREEN))
                .addLoreLine(Text.literal("Spider Leggings").formatted(Formatting.DARK_RED))
                .addLoreLine(Text.literal("Skeleton Bow").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("Slime Boots").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("Warden Chestplate").formatted(Formatting.DARK_AQUA))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to view!").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openBountyArmorRecipes(player))
                .build());

        // Category: Bounty Weapons (slot 22)
        gui.setSlot(22, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Text.literal("⚔ Bounty Weapons").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Slayer swords crafted from").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("bounty boss drops").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to view!").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openBountyWeaponRecipes(player))
                .build());

        // Category: HPEBM Weapons (slot 24)
        gui.setSlot(24, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal("⚡ HPEBM Weapons").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("High-Powered Energy").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Beam Weapons (Mk1-Mk5)").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to view!").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openHPEBMRecipes(player))
                .build());

        // Category: Special Items (slot 30)
        gui.setSlot(30, new GuiElementBuilder(Items.WOODEN_AXE)
                .setName(Text.literal("✦ Special Items").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("The Gavel and other").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("unique tools").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to view!").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openSpecialItemRecipes(player))
                .build());

        // Category: Cores & Chunks (slot 32)
        gui.setSlot(32, new GuiElementBuilder(Items.NETHER_STAR)
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
    // BOUNTY ARMOR RECIPES PAGE
    // ============================================================
    public static void openBountyArmorRecipes(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("🛡 Bounty Armor Recipes"));

        fillBackground(gui, Items.GRAY_STAINED_GLASS_PANE);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.DIAMOND_CHESTPLATE)
                .setName(Text.literal("🛡 Bounty Armor").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal("Click an item to see its recipe!").formatted(Formatting.GRAY))
                .build());

        // Zombie Berserker Helmet
        gui.setSlot(10, new GuiElementBuilder(Items.ZOMBIE_HEAD)
                .setName(Text.literal("☠ Zombie Berserker Helmet").formatted(Formatting.DARK_GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Requires: Zombie Bounty Lvl 6").formatted(Formatting.RED))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to view recipe!").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openZombieBerserkerHelmetRecipe(player))
                .build());

        // Spider Leggings
        gui.setSlot(12, new GuiElementBuilder(Items.LEATHER_LEGGINGS)
                .setName(Text.literal("🕷 Venomous Crawler Leggings").formatted(Formatting.DARK_RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Requires: Spider Bounty Lvl 12").formatted(Formatting.RED))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to view recipe!").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openSpiderLeggingsRecipe(player))
                .build());

        // Skeleton Bow
        gui.setSlot(14, new GuiElementBuilder(Items.BOW)
                .setName(Text.literal("🏹 Bone Desperado's Longbow").formatted(Formatting.WHITE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Requires: Skeleton Bounty Lvl 10").formatted(Formatting.RED))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to view recipe!").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openSkeletonBowRecipe(player))
                .build());

        // Slime Boots
        gui.setSlot(16, new GuiElementBuilder(Items.LEATHER_BOOTS)
                .setName(Text.literal("🥾 Gelatinous Rustler Boots").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Requires: Slime Bounty Lvl 8").formatted(Formatting.RED))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to view recipe!").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openSlimeBootsRecipe(player))
                .build());

        // Warden Chestplate
        gui.setSlot(22, new GuiElementBuilder(Items.NETHERITE_CHESTPLATE)
                .setName(Text.literal("💀 Sculk Terror Chestplate").formatted(Formatting.DARK_AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Requires: Warden Bounty Lvl 12").formatted(Formatting.RED))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to view recipe!").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openWardenChestplateRecipe(player))
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
    // ZOMBIE BERSERKER HELMET RECIPE
    // ============================================================
    public static void openZombieBerserkerHelmetRecipe(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("☠ Zombie Berserker Helmet Recipe"));

        fillBackground(gui, Items.BLACK_STAINED_GLASS_PANE);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.ZOMBIE_HEAD)
                .setName(Text.literal("☠ Zombie Berserker Helmet").formatted(Formatting.DARK_GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal("A cursed helm that trades").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("vitality for raw power.").formatted(Formatting.GRAY))
                .glow()
                .build());

        // Crafting Grid - Row 1: [Core] [Zombie Head] [Core]
        gui.setSlot(10, createCoreItem("Zombie Core", Items.ROTTEN_FLESH, Formatting.DARK_GREEN));
        gui.setSlot(11, new GuiElementBuilder(Items.ZOMBIE_HEAD)
                .setName(Text.literal("Zombie Head").formatted(Formatting.WHITE))
                .build());
        gui.setSlot(12, createCoreItem("Zombie Core", Items.ROTTEN_FLESH, Formatting.DARK_GREEN));

        // Crafting Grid - Row 2: [Core] [Rotten Flesh x3] [Core]
        gui.setSlot(19, createCoreItem("Zombie Core", Items.ROTTEN_FLESH, Formatting.DARK_GREEN));
        gui.setSlot(20, new GuiElementBuilder(Items.ROTTEN_FLESH)
                .setCount(3)
                .setName(Text.literal("Rotten Flesh x3").formatted(Formatting.WHITE))
                .build());
        gui.setSlot(21, createCoreItem("Zombie Core", Items.ROTTEN_FLESH, Formatting.DARK_GREEN));

        // Crafting Grid - Row 3: Empty
        gui.setSlot(28, emptySlot());
        gui.setSlot(29, createCoreItem("Zombie Core", Items.ROTTEN_FLESH, Formatting.DARK_GREEN));
        gui.setSlot(30, emptySlot());

        // Arrow
        gui.setSlot(23, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("→").formatted(Formatting.GREEN, Formatting.BOLD))
                .build());

        // Result
        gui.setSlot(25, new GuiElementBuilder(Items.ZOMBIE_HEAD)
                .setName(Text.literal("☠ Zombie Berserker Helmet").formatted(Formatting.DARK_GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("❤ Health: -50%").formatted(Formatting.DARK_RED))
                .addLoreLine(Text.literal("⚔ Damage: +300%").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⚠ Requires: Zombie Lvl 6").formatted(Formatting.RED))
                .glow()
                .build());

        // Materials list
        gui.setSlot(40, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("📋 Materials Required").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("• 5x Zombie Core").formatted(Formatting.DARK_GREEN))
                .addLoreLine(Text.literal("• 1x Zombie Head").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("• 3x Rotten Flesh").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Craft at: Smithing Table").formatted(Formatting.AQUA))
                .build());

        // How to obtain cores
        gui.setSlot(42, new GuiElementBuilder(Items.ENDER_EYE)
                .setName(Text.literal("ℹ How to Obtain Cores").formatted(Formatting.LIGHT_PURPLE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Zombie Cores drop from:").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("  The Undying Outlaw (Boss)").formatted(Formatting.DARK_GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Drop Rates by Tier:").formatted(Formatting.AQUA))
                .addLoreLine(Text.literal("  T1: 2%  T2: 6%  T3: 11%").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("  T4: 16%  T5: 20%").formatted(Formatting.GRAY))
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
    // SPIDER LEGGINGS RECIPE
    // ============================================================
    public static void openSpiderLeggingsRecipe(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("🕷 Venomous Crawler Leggings Recipe"));

        fillBackground(gui, Items.BLACK_STAINED_GLASS_PANE);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.LEATHER_LEGGINGS)
                .setName(Text.literal("🕷 Venomous Crawler Leggings").formatted(Formatting.DARK_RED, Formatting.BOLD))
                .addLoreLine(Text.literal("Woven from the silk of a").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("thousand slain bounty spiders.").formatted(Formatting.GRAY))
                .glow()
                .build());

        // Crafting Grid - Row 1: [Core] [String x4] [Core]
        gui.setSlot(10, createCoreItem("Spider Core", Items.SPIDER_EYE, Formatting.DARK_RED));
        gui.setSlot(11, new GuiElementBuilder(Items.STRING)
                .setCount(4)
                .setName(Text.literal("String x4").formatted(Formatting.WHITE))
                .build());
        gui.setSlot(12, createCoreItem("Spider Core", Items.SPIDER_EYE, Formatting.DARK_RED));

        // Crafting Grid - Row 2: [Core] [Spider Eye] [Core]
        gui.setSlot(19, createCoreItem("Spider Core", Items.SPIDER_EYE, Formatting.DARK_RED));
        gui.setSlot(20, new GuiElementBuilder(Items.SPIDER_EYE)
                .setName(Text.literal("Spider Eye").formatted(Formatting.WHITE))
                .build());
        gui.setSlot(21, createCoreItem("Spider Core", Items.SPIDER_EYE, Formatting.DARK_RED));

        // Crafting Grid - Row 3: [String x4] [Core] [Empty]
        gui.setSlot(28, new GuiElementBuilder(Items.STRING)
                .setCount(4)
                .setName(Text.literal("String x4").formatted(Formatting.WHITE))
                .build());
        gui.setSlot(29, createCoreItem("Spider Core", Items.SPIDER_EYE, Formatting.DARK_RED));
        gui.setSlot(30, emptySlot());

        // Arrow
        gui.setSlot(23, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("→").formatted(Formatting.GREEN, Formatting.BOLD))
                .build());

        // Result
        gui.setSlot(25, new GuiElementBuilder(Items.LEATHER_LEGGINGS)
                .setName(Text.literal("🕷 Venomous Crawler Leggings").formatted(Formatting.DARK_RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("❤ Regen: 3x Speed").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("💨 Speed: 2x Boost").formatted(Formatting.AQUA))
                .addLoreLine(Text.literal("🛡 Armor: +8 Points").formatted(Formatting.BLUE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⚠ Requires: Spider Lvl 12").formatted(Formatting.RED))
                .glow()
                .build());

        // Materials list
        gui.setSlot(40, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("📋 Materials Required").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("• 5x Spider Core").formatted(Formatting.DARK_RED))
                .addLoreLine(Text.literal("• 8x String").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("• 1x Spider Eye").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Craft at: Smithing Table").formatted(Formatting.AQUA))
                .build());

        // Back & Close buttons
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openBountyArmorRecipes(player))
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("✖ Close").formatted(Formatting.RED))
                .setCallback((idx, clickType, action) -> gui.close())
                .build());

        gui.open();
    }

    // ============================================================
    // SKELETON BOW RECIPE
    // ============================================================
    public static void openSkeletonBowRecipe(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("🏹 Bone Desperado's Longbow Recipe"));

        fillBackground(gui, Items.BLACK_STAINED_GLASS_PANE);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.BOW)
                .setName(Text.literal("🏹 Bone Desperado's Longbow").formatted(Formatting.WHITE, Formatting.BOLD))
                .addLoreLine(Text.literal("Carved from the spine of").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("The Bone Desperado himself.").formatted(Formatting.GRAY))
                .glow()
                .build());

        // Crafting Grid - Row 1: [Core] [Bone] [Core]
        gui.setSlot(10, createCoreItem("Skeleton Core", Items.BONE, Formatting.WHITE));
        gui.setSlot(11, new GuiElementBuilder(Items.BONE)
                .setName(Text.literal("Bone").formatted(Formatting.WHITE))
                .build());
        gui.setSlot(12, createCoreItem("Skeleton Core", Items.BONE, Formatting.WHITE));

        // Crafting Grid - Row 2: [Core] [Bow] [Core]
        gui.setSlot(19, createCoreItem("Skeleton Core", Items.BONE, Formatting.WHITE));
        gui.setSlot(20, new GuiElementBuilder(Items.BOW)
                .setName(Text.literal("Bow").formatted(Formatting.WHITE))
                .build());
        gui.setSlot(21, createCoreItem("Skeleton Core", Items.BONE, Formatting.WHITE));

        // Crafting Grid - Row 3: [Bone] [Core] [Bone]
        gui.setSlot(28, new GuiElementBuilder(Items.BONE)
                .setName(Text.literal("Bone").formatted(Formatting.WHITE))
                .build());
        gui.setSlot(29, createCoreItem("Skeleton Core", Items.BONE, Formatting.WHITE));
        gui.setSlot(30, new GuiElementBuilder(Items.BONE)
                .setName(Text.literal("Bone").formatted(Formatting.WHITE))
                .build());

        // Arrow
        gui.setSlot(23, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("→").formatted(Formatting.GREEN, Formatting.BOLD))
                .build());

        // Result
        gui.setSlot(25, new GuiElementBuilder(Items.BOW)
                .setName(Text.literal("🏹 Bone Desperado's Longbow").formatted(Formatting.WHITE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🎯 Auto-Lock Arrows").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("💀 Headshot: 5x Damage").formatted(Formatting.RED))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⚠ Requires: Skeleton Lvl 10").formatted(Formatting.RED))
                .glow()
                .build());

        // Materials list
        gui.setSlot(40, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("📋 Materials Required").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("• 5x Skeleton Core").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("• 1x Bow").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("• 3x Bone").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Craft at: Smithing Table").formatted(Formatting.AQUA))
                .build());

        // Back & Close
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openBountyArmorRecipes(player))
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("✖ Close").formatted(Formatting.RED))
                .setCallback((idx, clickType, action) -> gui.close())
                .build());

        gui.open();
    }

    // ============================================================
    // SLIME BOOTS RECIPE
    // ============================================================
    public static void openSlimeBootsRecipe(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("🥾 Gelatinous Rustler Boots Recipe"));

        fillBackground(gui, Items.BLACK_STAINED_GLASS_PANE);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.LEATHER_BOOTS)
                .setName(Text.literal("🥾 Gelatinous Rustler Boots").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal("Bouncy boots made from").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("condensed slime essence.").formatted(Formatting.GRAY))
                .glow()
                .build());

        // Crafting Grid - Row 1: [Core] [Slime x4] [Core]
        gui.setSlot(10, createCoreItem("Slime Core", Items.SLIME_BALL, Formatting.GREEN));
        gui.setSlot(11, new GuiElementBuilder(Items.SLIME_BALL)
                .setCount(4)
                .setName(Text.literal("Slime Ball x4").formatted(Formatting.WHITE))
                .build());
        gui.setSlot(12, createCoreItem("Slime Core", Items.SLIME_BALL, Formatting.GREEN));

        // Crafting Grid - Row 2: [Core] [Leather Boots] [Core]
        gui.setSlot(19, createCoreItem("Slime Core", Items.SLIME_BALL, Formatting.GREEN));
        gui.setSlot(20, new GuiElementBuilder(Items.LEATHER_BOOTS)
                .setName(Text.literal("Leather Boots").formatted(Formatting.WHITE))
                .build());
        gui.setSlot(21, createCoreItem("Slime Core", Items.SLIME_BALL, Formatting.GREEN));

        // Crafting Grid - Row 3: [Slime x4] [Core] [Empty]
        gui.setSlot(28, new GuiElementBuilder(Items.SLIME_BALL)
                .setCount(4)
                .setName(Text.literal("Slime Ball x4").formatted(Formatting.WHITE))
                .build());
        gui.setSlot(29, createCoreItem("Slime Core", Items.SLIME_BALL, Formatting.GREEN));
        gui.setSlot(30, emptySlot());

        // Arrow
        gui.setSlot(23, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("→").formatted(Formatting.GREEN, Formatting.BOLD))
                .build());

        // Result
        gui.setSlot(25, new GuiElementBuilder(Items.LEATHER_BOOTS)
                .setName(Text.literal("🥾 Gelatinous Rustler Boots").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⬆ Jump Boost II").formatted(Formatting.AQUA))
                .addLoreLine(Text.literal("🛡 No Fall Damage").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("💀 Death Save (30s shrink)").formatted(Formatting.LIGHT_PURPLE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⚠ Requires: Slime Lvl 8").formatted(Formatting.RED))
                .glow()
                .build());

        // Materials list
        gui.setSlot(40, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("📋 Materials Required").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("• 5x Slime Core").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("• 8x Slime Ball").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("• 1x Leather Boots").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Craft at: Smithing Table").formatted(Formatting.AQUA))
                .build());

        // Back & Close
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openBountyArmorRecipes(player))
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("✖ Close").formatted(Formatting.RED))
                .setCallback((idx, clickType, action) -> gui.close())
                .build());

        gui.open();
    }

    // ============================================================
    // WARDEN CHESTPLATE RECIPE
    // ============================================================
    public static void openWardenChestplateRecipe(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("💀 Sculk Terror Chestplate Recipe"));

        fillBackground(gui, Items.BLACK_STAINED_GLASS_PANE);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.NETHERITE_CHESTPLATE)
                .setName(Text.literal("💀 Sculk Terror Chestplate").formatted(Formatting.DARK_AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal("Forged in the depths of the").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("ancient city from warden essence.").formatted(Formatting.GRAY))
                .glow()
                .build());

        // Crafting Grid - Row 1: [Core] [Core] [Core]
        gui.setSlot(10, createCoreItem("Warden Core", Items.ECHO_SHARD, Formatting.DARK_AQUA));
        gui.setSlot(11, createCoreItem("Warden Core", Items.ECHO_SHARD, Formatting.DARK_AQUA));
        gui.setSlot(12, createCoreItem("Warden Core", Items.ECHO_SHARD, Formatting.DARK_AQUA));

        // Crafting Grid - Row 2: [Core] [Netherite Chestplate] [Core]
        gui.setSlot(19, createCoreItem("Warden Core", Items.ECHO_SHARD, Formatting.DARK_AQUA));
        gui.setSlot(20, new GuiElementBuilder(Items.NETHERITE_CHESTPLATE)
                .setName(Text.literal("Netherite Chestplate").formatted(Formatting.WHITE))
                .build());
        gui.setSlot(21, createCoreItem("Warden Core", Items.ECHO_SHARD, Formatting.DARK_AQUA));

        // Crafting Grid - Row 3: [Core] [Echo Shards x4] [Core]
        gui.setSlot(28, createCoreItem("Warden Core", Items.ECHO_SHARD, Formatting.DARK_AQUA));
        gui.setSlot(29, new GuiElementBuilder(Items.ECHO_SHARD)
                .setCount(4)
                .setName(Text.literal("Echo Shard x4").formatted(Formatting.WHITE))
                .build());
        gui.setSlot(30, createCoreItem("Warden Core", Items.ECHO_SHARD, Formatting.DARK_AQUA));

        // Arrow
        gui.setSlot(23, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("→").formatted(Formatting.GREEN, Formatting.BOLD))
                .build());

        // Result
        gui.setSlot(25, new GuiElementBuilder(Items.NETHERITE_CHESTPLATE)
                .setName(Text.literal("💀 Sculk Terror Chestplate").formatted(Formatting.DARK_AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🛡 Armor: +16 Points").formatted(Formatting.BLUE))
                .addLoreLine(Text.literal("❤ Extra Health Bar").formatted(Formatting.RED))
                .addLoreLine(Text.literal("⚡ No Knockback").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("👁 ESP for Nearby Players").formatted(Formatting.LIGHT_PURPLE))
                .addLoreLine(Text.literal("🔇 Sculk Immunity").formatted(Formatting.DARK_AQUA))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⚠ Requires: Warden Lvl 12").formatted(Formatting.RED))
                .glow()
                .build());

        // Materials list
        gui.setSlot(40, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("📋 Materials Required").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("• 8x Warden Core").formatted(Formatting.DARK_AQUA))
                .addLoreLine(Text.literal("• 1x Netherite Chestplate").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("• 4x Echo Shard").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Craft at: Smithing Table").formatted(Formatting.AQUA))
                .build());

        // Back & Close
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openBountyArmorRecipes(player))
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("✖ Close").formatted(Formatting.RED))
                .setCallback((idx, clickType, action) -> gui.close())
                .build());

        gui.open();
    }

    // ============================================================
    // BOUNTY WEAPON RECIPES PAGE
    // ============================================================
    public static void openBountyWeaponRecipes(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⚔ Bounty Weapon Recipes"));

        fillBackground(gui, Items.BROWN_STAINED_GLASS_PANE);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Text.literal("⚔ Bounty Weapons").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal("Craft powerful slayer swords!").formatted(Formatting.GRAY))
                .build());

        int slot = 10;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            final SlayerManager.SlayerType finalType = type;
            gui.setSlot(slot, new GuiElementBuilder(Items.IRON_SWORD)
                    .setName(Text.literal(type.displayName + " Slayer Sword").formatted(type.color, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Materials:").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("  2x " + type.displayName + " Chunk").formatted(Formatting.WHITE))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("⚠ Requires: Level 3 to use").formatted(Formatting.RED))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Click to view recipe!").formatted(Formatting.YELLOW))
                    .setCallback((idx, clickType, action) -> openSlayerSwordRecipe(player, finalType))
                    .build());
            slot++;
            if (slot == 16) slot = 19;
        }

        // Back & Close
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openMainMenu(player))
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("✖ Close").formatted(Formatting.RED))
                .setCallback((idx, clickType, action) -> gui.close())
                .build());

        gui.open();
    }

    // ============================================================
    // SLAYER SWORD RECIPE (GENERIC)
    // ============================================================
    public static void openSlayerSwordRecipe(ServerPlayerEntity player, SlayerManager.SlayerType type) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⚔ " + type.displayName + " Slayer Sword Recipe"));

        fillBackground(gui, Items.BLACK_STAINED_GLASS_PANE);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Text.literal("⚔ " + type.displayName + " Slayer Sword").formatted(type.color, Formatting.BOLD))
                .glow()
                .build());

        // Crafting Grid - Simple 2 chunk recipe
        gui.setSlot(19, emptySlot());
        gui.setSlot(20, createChunkItem(type.displayName + " Chunk", type.icon, type.color));
        gui.setSlot(21, emptySlot());

        gui.setSlot(28, emptySlot());
        gui.setSlot(29, createChunkItem(type.displayName + " Chunk", type.icon, type.color));
        gui.setSlot(30, emptySlot());

        // Arrow
        gui.setSlot(23, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("→").formatted(Formatting.GREEN, Formatting.BOLD))
                .build());

        // Result
        gui.setSlot(25, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Text.literal("⚔ " + type.displayName + " Slayer Sword").formatted(type.color, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⚔ Bonus damage vs " + type.displayName + "s").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⚠ Requires: Level 3").formatted(Formatting.RED))
                .glow()
                .build());

        // Materials
        gui.setSlot(40, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("📋 Materials Required").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("• 2x " + type.displayName + " Chunk").formatted(type.color))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Craft at: Smithing Table").formatted(Formatting.AQUA))
                .build());

        // Back & Close
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openBountyWeaponRecipes(player))
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("✖ Close").formatted(Formatting.RED))
                .setCallback((idx, clickType, action) -> gui.close())
                .build());

        gui.open();
    }

    // ============================================================
    // HPEBM RECIPES PAGE
    // ============================================================
    public static void openHPEBMRecipes(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⚡ HPEBM Weapon Recipes"));

        fillBackground(gui, Items.CYAN_STAINED_GLASS_PANE);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal("⚡ HPEBM Weapons").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal("High-Powered Energy Beam Weapons").formatted(Formatting.GRAY))
                .glow()
                .build());

        // Mk1
        gui.setSlot(10, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal("HPEBM Mk1").formatted(Formatting.WHITE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Recipe:").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("  1x End Rod").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("  1x Iron Shovel").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("  1x Redstone Block").formatted(Formatting.WHITE))
                .build());

        // Mk2
        gui.setSlot(12, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal("HPEBM Mk2").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Recipe:").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("  1x HPEBM Mk1").formatted(Formatting.AQUA))
                .addLoreLine(Text.literal("  4x Diamond").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("  2x Blaze Powder").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to view recipe!").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openHPEBMMk2Recipe(player))
                .build());

        // Mk3
        gui.setSlot(14, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal("HPEBM Mk3").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Recipe:").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("  1x HPEBM Mk2").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("  2x Nether Star Fragment").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("  8x Gold Block").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to view recipe!").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openHPEBMMk3Recipe(player))
                .build());

        // Mk4
        gui.setSlot(16, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal("HPEBM Mk4").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Recipe:").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("  1x HPEBM Mk3").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("  1x Nether Star").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("  4x Netherite Ingot").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to view recipe!").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openHPEBMMk4Recipe(player))
                .build());

        // Mk5
        gui.setSlot(22, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal("HPEBM Mk5").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("LEGENDARY").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Recipe:").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("  1x HPEBM Mk4").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal("  2x Dragon Breath").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("  1x Elytra").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("  16x End Crystal").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to view recipe!").formatted(Formatting.YELLOW))
                .glow()
                .setCallback((idx, clickType, action) -> openHPEBMMk5Recipe(player))
                .build());

        // Back & Close
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openMainMenu(player))
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("✖ Close").formatted(Formatting.RED))
                .setCallback((idx, clickType, action) -> gui.close())
                .build());

        gui.open();
    }

    // ============================================================
    // HPEBM Mk1 RECIPE
    // ============================================================
    public static void openHPEBMMk1Recipe(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⚡ HPEBM Mk1 Recipe"));

        fillBackground(gui, Items.BLACK_STAINED_GLASS_PANE);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal("⚡ HPEBM Mk1").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal("Base Energy Weapon").formatted(Formatting.GRAY))
                .glow()
                .build());

        // Crafting Grid - Row 1
        gui.setSlot(10, emptySlot());
        gui.setSlot(11, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal("End Rod").formatted(Formatting.WHITE))
                .build());
        gui.setSlot(12, emptySlot());

        // Crafting Grid - Row 2
        gui.setSlot(19, emptySlot());
        gui.setSlot(20, new GuiElementBuilder(Items.IRON_SHOVEL)
                .setName(Text.literal("Iron Shovel").formatted(Formatting.WHITE))
                .build());
        gui.setSlot(21, emptySlot());

        // Crafting Grid - Row 3
        gui.setSlot(28, emptySlot());
        gui.setSlot(29, new GuiElementBuilder(Items.REDSTONE_BLOCK)
                .setName(Text.literal("Redstone Block").formatted(Formatting.WHITE))
                .build());
        gui.setSlot(30, emptySlot());

        // Arrow
        gui.setSlot(23, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("→").formatted(Formatting.GREEN, Formatting.BOLD))
                .build());

        // Result
        gui.setSlot(25, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal("⚡ HPEBM Mk1").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Base energy beam weapon").formatted(Formatting.GRAY))
                .glow()
                .build());

        // Materials
        gui.setSlot(40, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("📋 Materials Required").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("• 1x End Rod").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("• 1x Iron Shovel").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("• 1x Redstone Block").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Craft at: Crafting Table").formatted(Formatting.AQUA))
                .build());

        // Back & Close
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openHPEBMRecipes(player))
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("✖ Close").formatted(Formatting.RED))
                .setCallback((idx, clickType, action) -> gui.close())
                .build());

        gui.open();
    }

    // ============================================================
    // HPEBM Mk2 RECIPE
    // ============================================================
    public static void openHPEBMMk2Recipe(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⚡ HPEBM Mk2 Recipe"));

        fillBackground(gui, Items.BLACK_STAINED_GLASS_PANE);

        gui.setSlot(4, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal("⚡ HPEBM Mk2").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal("Enhanced Power Output").formatted(Formatting.GRAY))
                .glow()
                .build());

        // Crafting Grid
        gui.setSlot(10, new GuiElementBuilder(Items.DIAMOND)
                .setName(Text.literal("Diamond").formatted(Formatting.WHITE))
                .build());
        gui.setSlot(11, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal("HPEBM Mk1").formatted(Formatting.AQUA))
                .glow()
                .build());
        gui.setSlot(12, new GuiElementBuilder(Items.DIAMOND)
                .setName(Text.literal("Diamond").formatted(Formatting.WHITE))
                .build());

        gui.setSlot(19, new GuiElementBuilder(Items.DIAMOND)
                .setName(Text.literal("Diamond").formatted(Formatting.WHITE))
                .build());
        gui.setSlot(20, new GuiElementBuilder(Items.BLAZE_POWDER)
                .setCount(2)
                .setName(Text.literal("Blaze Powder x2").formatted(Formatting.WHITE))
                .build());
        gui.setSlot(21, new GuiElementBuilder(Items.DIAMOND)
                .setName(Text.literal("Diamond").formatted(Formatting.WHITE))
                .build());

        gui.setSlot(28, emptySlot());
        gui.setSlot(29, emptySlot());
        gui.setSlot(30, emptySlot());

        gui.setSlot(23, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("→").formatted(Formatting.GREEN, Formatting.BOLD))
                .build());

        gui.setSlot(25, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal("⚡ HPEBM Mk2").formatted(Formatting.GREEN, Formatting.BOLD))
                .glow()
                .build());

        gui.setSlot(40, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("📋 Materials Required").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("• 1x HPEBM Mk1").formatted(Formatting.AQUA))
                .addLoreLine(Text.literal("• 4x Diamond").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("• 2x Blaze Powder").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Craft at: Smithing Table").formatted(Formatting.AQUA))
                .build());

        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openHPEBMRecipes(player))
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("✖ Close").formatted(Formatting.RED))
                .setCallback((idx, clickType, action) -> gui.close())
                .build());

        gui.open();
    }

    // ============================================================
    // HPEBM Mk3, Mk4, Mk5 RECIPES (similar pattern)
    // ============================================================
    public static void openHPEBMMk3Recipe(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⚡ HPEBM Mk3 Recipe"));
        fillBackground(gui, Items.BLACK_STAINED_GLASS_PANE);

        gui.setSlot(4, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal("⚡ HPEBM Mk3").formatted(Formatting.YELLOW, Formatting.BOLD))
                .glow()
                .build());

        gui.setSlot(11, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal("HPEBM Mk2").formatted(Formatting.GREEN))
                .glow()
                .build());
        gui.setSlot(19, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setCount(8)
                .setName(Text.literal("Gold Block x8").formatted(Formatting.WHITE))
                .build());
        gui.setSlot(20, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("Nether Star Fragment x2").formatted(Formatting.WHITE))
                .build());

        gui.setSlot(23, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("→").formatted(Formatting.GREEN, Formatting.BOLD))
                .build());
        gui.setSlot(25, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal("⚡ HPEBM Mk3").formatted(Formatting.YELLOW, Formatting.BOLD))
                .glow()
                .build());

        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openHPEBMRecipes(player))
                .build());
        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("✖ Close").formatted(Formatting.RED))
                .setCallback((idx, clickType, action) -> gui.close())
                .build());

        gui.open();
    }

    public static void openHPEBMMk4Recipe(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⚡ HPEBM Mk4 Recipe"));
        fillBackground(gui, Items.BLACK_STAINED_GLASS_PANE);

        gui.setSlot(4, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal("⚡ HPEBM Mk4").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal("Experimental Prototype").formatted(Formatting.GRAY))
                .glow()
                .build());

        // Crafting Grid
        gui.setSlot(10, new GuiElementBuilder(Items.NETHERITE_INGOT)
                .setName(Text.literal("Netherite Ingot").formatted(Formatting.WHITE))
                .build());
        gui.setSlot(11, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal("HPEBM Mk3").formatted(Formatting.YELLOW))
                .glow()
                .build());
        gui.setSlot(12, new GuiElementBuilder(Items.NETHERITE_INGOT)
                .setName(Text.literal("Netherite Ingot").formatted(Formatting.WHITE))
                .build());

        gui.setSlot(19, new GuiElementBuilder(Items.NETHERITE_INGOT)
                .setName(Text.literal("Netherite Ingot").formatted(Formatting.WHITE))
                .build());
        gui.setSlot(20, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("Nether Star").formatted(Formatting.WHITE))
                .build());
        gui.setSlot(21, new GuiElementBuilder(Items.NETHERITE_INGOT)
                .setName(Text.literal("Netherite Ingot").formatted(Formatting.WHITE))
                .build());

        gui.setSlot(28, emptySlot());
        gui.setSlot(29, emptySlot());
        gui.setSlot(30, emptySlot());

        gui.setSlot(23, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("→").formatted(Formatting.GREEN, Formatting.BOLD))
                .build());

        gui.setSlot(25, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal("⚡ HPEBM Mk4").formatted(Formatting.GOLD, Formatting.BOLD))
                .glow()
                .build());

        gui.setSlot(40, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("📋 Materials Required").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("• 1x HPEBM Mk3").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("• 1x Nether Star").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("• 4x Netherite Ingot").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Craft at: Smithing Table").formatted(Formatting.AQUA))
                .build());

        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openHPEBMRecipes(player))
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("✖ Close").formatted(Formatting.RED))
                .setCallback((idx, clickType, action) -> gui.close())
                .build());

        gui.open();
    }

    // ============================================================
    // HPEBM Mk5 RECIPE
    // ============================================================
    public static void openHPEBMMk5Recipe(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⚡ HPEBM Mk5 Recipe"));
        fillBackground(gui, Items.BLACK_STAINED_GLASS_PANE);

        gui.setSlot(4, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal("⚡ HPEBM Mk5").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal("LEGENDARY").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal("Ultimate Energy Weapon").formatted(Formatting.GRAY))
                .glow()
                .build());

        // Crafting Grid
        gui.setSlot(10, new GuiElementBuilder(Items.END_CRYSTAL)
                .setCount(8)
                .setName(Text.literal("End Crystal x8").formatted(Formatting.WHITE))
                .build());
        gui.setSlot(11, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal("HPEBM Mk4").formatted(Formatting.GOLD))
                .glow()
                .build());
        gui.setSlot(12, new GuiElementBuilder(Items.END_CRYSTAL)
                .setCount(8)
                .setName(Text.literal("End Crystal x8").formatted(Formatting.WHITE))
                .build());

        gui.setSlot(19, new GuiElementBuilder(Items.DRAGON_BREATH)
                .setName(Text.literal("Dragon Breath").formatted(Formatting.WHITE))
                .build());
        gui.setSlot(20, new GuiElementBuilder(Items.ELYTRA)
                .setName(Text.literal("Elytra").formatted(Formatting.WHITE))
                .build());
        gui.setSlot(21, new GuiElementBuilder(Items.DRAGON_BREATH)
                .setName(Text.literal("Dragon Breath").formatted(Formatting.WHITE))
                .build());

        gui.setSlot(28, emptySlot());
        gui.setSlot(29, emptySlot());
        gui.setSlot(30, emptySlot());

        gui.setSlot(23, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("→").formatted(Formatting.GREEN, Formatting.BOLD))
                .build());

        gui.setSlot(25, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal("⚡ HPEBM Mk5").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("LEGENDARY WEAPON").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .glow()
                .build());

        gui.setSlot(40, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("📋 Materials Required").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("• 1x HPEBM Mk4").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal("• 2x Dragon Breath").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("• 1x Elytra").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("• 16x End Crystal").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Craft at: Smithing Table").formatted(Formatting.AQUA))
                .build());

        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openHPEBMRecipes(player))
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("✖ Close").formatted(Formatting.RED))
                .setCallback((idx, clickType, action) -> gui.close())
                .build());

        gui.open();
    }

    // ============================================================
    // SPECIAL ITEMS RECIPES PAGE
    // ============================================================
    public static void openSpecialItemRecipes(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("✦ Special Item Recipes"));

        fillBackground(gui, Items.PURPLE_STAINED_GLASS_PANE);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("✦ Special Items").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal("Unique craftable items!").formatted(Formatting.GRAY))
                .glow()
                .build());

        // The Gavel
        gui.setSlot(20, new GuiElementBuilder(Items.WOODEN_AXE)
                .setName(Text.literal("⚖ The Gavel").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("The tool of justice!").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to view recipe!").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openGavelRecipe(player))
                .build());

        // Back & Close
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openMainMenu(player))
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("✖ Close").formatted(Formatting.RED))
                .setCallback((idx, clickType, action) -> gui.close())
                .build());

        gui.open();
    }

    // ============================================================
    // GAVEL RECIPE
    // ============================================================
    public static void openGavelRecipe(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⚖ The Gavel Recipe"));

        fillBackground(gui, Items.BLACK_STAINED_GLASS_PANE);

        gui.setSlot(4, new GuiElementBuilder(Items.WOODEN_AXE)
                .setName(Text.literal("⚖ The Gavel").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal("Tool of the Chair").formatted(Formatting.GRAY))
                .glow()
                .build());

        // Crafting Grid - Row 1: [Plank] [Plank] [Plank]
        gui.setSlot(10, new GuiElementBuilder(Items.OAK_PLANKS)
                .setName(Text.literal("Oak Planks").formatted(Formatting.WHITE))
                .build());
        gui.setSlot(11, new GuiElementBuilder(Items.OAK_PLANKS)
                .setName(Text.literal("Oak Planks").formatted(Formatting.WHITE))
                .build());
        gui.setSlot(12, new GuiElementBuilder(Items.OAK_PLANKS)
                .setName(Text.literal("Oak Planks").formatted(Formatting.WHITE))
                .build());

        // Crafting Grid - Row 2: [Empty] [Stick] [Empty]
        gui.setSlot(19, emptySlot());
        gui.setSlot(20, new GuiElementBuilder(Items.STICK)
                .setName(Text.literal("Stick").formatted(Formatting.WHITE))
                .build());
        gui.setSlot(21, emptySlot());

        // Crafting Grid - Row 3: [Empty] [Stick] [Empty]
        gui.setSlot(28, emptySlot());
        gui.setSlot(29, new GuiElementBuilder(Items.STICK)
                .setName(Text.literal("Stick").formatted(Formatting.WHITE))
                .build());
        gui.setSlot(30, emptySlot());

        // Arrow
        gui.setSlot(23, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("→").formatted(Formatting.GREEN, Formatting.BOLD))
                .build());

        // Result
        gui.setSlot(25, new GuiElementBuilder(Items.WOODEN_AXE)
                .setName(Text.literal("⚖ The Gavel").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Special abilities for Chair!").formatted(Formatting.YELLOW))
                .glow()
                .build());

        // Materials
        gui.setSlot(40, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("📋 Materials Required").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("• 3x Oak Planks").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("• 2x Stick").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Craft at: Crafting Table").formatted(Formatting.AQUA))
                .build());

        // Back & Close
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openSpecialItemRecipes(player))
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("✖ Close").formatted(Formatting.RED))
                .setCallback((idx, clickType, action) -> gui.close())
                .build());

        gui.open();
    }

    // ============================================================
    // MATERIALS INFO PAGE
    // ============================================================
    public static void openMaterialsInfo(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("✦ Cores & Materials Info"));

        fillBackground(gui, Items.LIGHT_BLUE_STAINED_GLASS_PANE);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("✦ Cores & Materials").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal("How to obtain crafting materials").formatted(Formatting.GRAY))
                .glow()
                .build());

        // Zombie Core
        gui.setSlot(10, createCoreItem("Zombie Core", Items.ROTTEN_FLESH, Formatting.DARK_GREEN));
        gui.setSlot(19, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("Zombie Core Info").formatted(Formatting.DARK_GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Drops from: The Undying Outlaw").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Drop Rate: T1: 2% → T5: 20%").formatted(Formatting.GRAY))
                .build());

        // Spider Core
        gui.setSlot(11, createCoreItem("Spider Core", Items.SPIDER_EYE, Formatting.DARK_RED));
        gui.setSlot(20, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("Spider Core Info").formatted(Formatting.DARK_RED))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Drops from: The Venomous Stalker").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Drop Rate: T1: 2% → T5: 20%").formatted(Formatting.GRAY))
                .build());

        // Skeleton Core
        gui.setSlot(12, createCoreItem("Skeleton Core", Items.BONE, Formatting.WHITE));
        gui.setSlot(21, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("Skeleton Core Info").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Drops from: The Bone Desperado").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Drop Rate: T1: 2% → T5: 20%").formatted(Formatting.GRAY))
                .build());

        // Slime Core
        gui.setSlot(14, createCoreItem("Slime Core", Items.SLIME_BALL, Formatting.GREEN));
        gui.setSlot(23, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("Slime Core Info").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Drops from: The Gelatinous Rustler").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Drop Rate: T1: 2% → T5: 20%").formatted(Formatting.GRAY))
                .build());

        // Enderman Core
        gui.setSlot(15, createCoreItem("Enderman Core", Items.ENDER_PEARL, Formatting.DARK_PURPLE));
        gui.setSlot(24, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("Enderman Core Info").formatted(Formatting.DARK_PURPLE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Drops from: The Void Walker").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Drop Rate: T1: 2% → T5: 20%").formatted(Formatting.GRAY))
                .build());

        // Warden Core
        gui.setSlot(16, createCoreItem("Warden Core", Items.ECHO_SHARD, Formatting.DARK_AQUA));
        gui.setSlot(25, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("Warden Core Info").formatted(Formatting.DARK_AQUA))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Drops from: The Sculk Terror").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Drop Rate: T1: 2% → T5: 20%").formatted(Formatting.GRAY))
                .build());

        // Info panel about chunks
        gui.setSlot(40, new GuiElementBuilder(Items.BOOK)
                .setName(Text.literal("📖 About Chunks").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Chunks are more common drops").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("used for crafting Slayer Swords.").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("They drop from bounty bosses").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("at higher rates than cores.").formatted(Formatting.YELLOW))
                .build());

        // Info panel about cores
        gui.setSlot(42, new GuiElementBuilder(Items.ENCHANTED_BOOK)
                .setName(Text.literal("📖 About Cores").formatted(Formatting.LIGHT_PURPLE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Cores are rare drops used").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("for crafting legendary armor.").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Higher tier bosses have").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("better core drop rates!").formatted(Formatting.YELLOW))
                .build());

        // Back & Close
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openMainMenu(player))
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("✖ Close").formatted(Formatting.RED))
                .setCallback((idx, clickType, action) -> gui.close())
                .build());

        gui.open();
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    private static void fillBackground(SimpleGui gui, Item backgroundItem) {
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(backgroundItem)
                    .setName(Text.literal(""))
                    .build());
        }
    }

    private static GuiElementBuilder createCoreItem(String name, Item icon, Formatting color) {
        return new GuiElementBuilder(icon)
                .setName(Text.literal("✦ " + name + " ✦").formatted(color, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Rare Boss Drop").formatted(Formatting.LIGHT_PURPLE))
                .addLoreLine(Text.literal("Used for crafting").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("legendary bounty armor").formatted(Formatting.GRAY))
                .glow();
    }

    private static GuiElementBuilder createChunkItem(String name, Item icon, Formatting color) {
        return new GuiElementBuilder(icon)
                .setName(Text.literal(name).formatted(color, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Boss Drop").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal("Used for crafting").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("slayer weapons").formatted(Formatting.GRAY));
    }

    private static GuiElementBuilder emptySlot() {
        return new GuiElementBuilder(Items.LIGHT_GRAY_STAINED_GLASS_PANE)
                .setName(Text.literal(""));
    }
}