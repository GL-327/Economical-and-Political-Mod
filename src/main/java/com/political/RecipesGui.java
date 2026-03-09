package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class RecipesGui {

    // ============================================================
    // MAIN RECIPES MENU
    // ============================================================
    public static void openMainMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("📖 Custom Recipes"));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Header (slot 4)
        gui.setSlot(4, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(Text.literal("📖 Recipe Book")
                        .formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("View all custom crafting recipes")
                        .formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("for this server's unique items!")
                        .formatted(Formatting.GRAY))
                .glow()
                .build());

        // Category: Bounty Swords T1 (slot 19)
        gui.setSlot(19, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Text.literal("⚔ Bounty Swords (T1)")
                        .formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Basic bounty swords for").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("hunting bounty mobs").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to view!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openBountySwordsT1(player))
                .build());

        // Category: Bounty Swords T2 (slot 20)
        gui.setSlot(20, new GuiElementBuilder(Items.DIAMOND_SWORD)
                .setName(Text.literal("⚔ Bounty Swords II (T2)")
                        .formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Upgraded bounty swords").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("with 3x damage bonus").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to view!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openBountySwordsT2(player))
                .build());

        // Category: HPEBM Weapons (slot 21)
        gui.setSlot(21, new GuiElementBuilder(Items.DIAMOND_SHOVEL)
                .setName(Text.literal("⚡ HPEBM Weapons")
                        .formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("High-Powered Energy").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Beam Weapons (Mk1-Ultra)").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to view!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openHPEBMRecipes(player))
                .build());

        // Category: Slayer Armor (slot 23)
        gui.setSlot(23, new GuiElementBuilder(Items.DIAMOND_CHESTPLATE)
                .setName(Text.literal("🛡 Bounty Armor")
                        .formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Protective gear crafted from").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("bounty boss materials").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to view!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openArmorRecipes(player))
                .build());

        // Category: Special Items (slot 25)
        gui.setSlot(25, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("✦ Special Items")
                        .formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Unique items like").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("The Gavel").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to view!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openSpecialRecipes(player))
                .build());

        // Close button (slot 49)
        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("✖ Close").formatted(Formatting.RED))
                .setCallback((idx, type, action) -> player.closeHandledScreen())
                .build());

        gui.open();
    }

    // ============================================================
    // BOUNTY SWORDS T1 RECIPES
    // ============================================================
    public static void openBountySwordsT1(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⚔ Bounty Swords (T1)"));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Text.literal("⚔ Bounty Swords (T1)")
                        .formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Recipe: 3 Cores + Iron Sword + 2 Sticks").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Requires: Bounty Level 3").formatted(Formatting.RED))
                .glow()
                .build());

        // Show all slayer types
        int slot = 19;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            if (slot > 25) break;
            gui.setSlot(slot, new GuiElementBuilder(Items.IRON_SWORD)
                    .setName(Text.literal(type.displayName + " Bounty Sword")
                            .formatted(type.color, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("━━━ RECIPE ━━━").formatted(Formatting.GOLD))
                    .addLoreLine(Text.literal("• 3x " + type.displayName + " Core").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("• 1x Iron Sword").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("• 2x Stick").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("━━━ EFFECT ━━━").formatted(Formatting.RED))
                    .addLoreLine(Text.literal("• 2x damage to " + type.displayName + "s").formatted(Formatting.GREEN))
                    .addLoreLine(Text.literal("• 2x kill count").formatted(Formatting.GREEN))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("⚠ Requires: " + type.displayName + " Bounty Lvl 3").formatted(Formatting.RED))
                    .build());
            slot++;
        }

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ============================================================
    // BOUNTY SWORDS T2 RECIPES
    // ============================================================
    public static void openBountySwordsT2(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⚔ Bounty Swords II (T2)"));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.YELLOW_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.DIAMOND_SWORD)
                .setName(Text.literal("⚔ Bounty Swords II (T2)")
                        .formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Recipe: 5 Cores + Diamond Sword + T1 Sword").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Requires: Bounty Level 6").formatted(Formatting.RED))
                .glow()
                .build());

        // Show all slayer types
        int slot = 19;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            if (slot > 25) break;
            gui.setSlot(slot, new GuiElementBuilder(Items.DIAMOND_SWORD)
                    .setName(Text.literal(type.displayName + " Bounty Sword II")
                            .formatted(type.color, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("━━━ RECIPE ━━━").formatted(Formatting.GOLD))
                    .addLoreLine(Text.literal("• 5x " + type.displayName + " Core").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("• 1x Diamond Sword").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("• 1x " + type.displayName + " Bounty Sword (T1)").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("━━━ EFFECT ━━━").formatted(Formatting.RED))
                    .addLoreLine(Text.literal("• 3x damage to " + type.displayName + "s").formatted(Formatting.GREEN))
                    .addLoreLine(Text.literal("• 3x kill count").formatted(Formatting.GREEN))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("⚠ Requires: " + type.displayName + " Bounty Lvl 6").formatted(Formatting.RED))
                    .glow()
                    .build());
            slot++;
        }

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ============================================================
    // ARMOR RECIPES
    // ============================================================
    public static void openArmorRecipes(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("🛡 Bounty Armor Recipes"));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.CYAN_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Zombie Berserker Helmet
        gui.setSlot(19, new GuiElementBuilder(Items.ZOMBIE_HEAD)
                .setName(Text.literal("☠ Zombie Berserker Helmet")
                        .formatted(Formatting.DARK_GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("━━━ RECIPE ━━━").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal("• 5x Zombie Core").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• 1x Zombie Head").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• 3x Rotten Flesh").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("━━━ EFFECT ━━━").formatted(Formatting.RED))
                .addLoreLine(Text.literal("❤ Health: -50%").formatted(Formatting.DARK_RED))
                .addLoreLine(Text.literal("⚔ Damage: +300%").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⚠ Requires: Zombie Bounty Lvl 12").formatted(Formatting.RED))
                .glow()
                .build());

        // Spider Leggings
        gui.setSlot(21, new GuiElementBuilder(Items.LEATHER_LEGGINGS)
                .setName(Text.literal("🕷 Venomous Crawler Leggings")
                        .formatted(Formatting.DARK_RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("━━━ RECIPE ━━━").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal("• 5x Spider Core").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• 8x String").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• 1x Spider Eye").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("━━━ EFFECT ━━━").formatted(Formatting.RED))
                .addLoreLine(Text.literal("🕸 Web Walk: Pass through webs").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("☠ Poison Immunity").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⚠ Requires: Spider Bounty Lvl 10").formatted(Formatting.RED))
                .glow()
                .build());

        // Skeleton Bow
        gui.setSlot(23, new GuiElementBuilder(Items.BOW)
                .setName(Text.literal("🏹 Bone Desperado's Longbow")
                        .formatted(Formatting.WHITE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("━━━ RECIPE ━━━").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal("• 5x Skeleton Core").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• 1x Bow").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• 3x Bone").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("━━━ EFFECT ━━━").formatted(Formatting.RED))
                .addLoreLine(Text.literal("🎯 Auto-Lock: Arrows home to targets").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("💀 Headshot: 5x Damage").formatted(Formatting.RED))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⚠ Requires: Skeleton Bounty Lvl 10").formatted(Formatting.RED))
                .glow()
                .build());

        // Slime Boots
        gui.setSlot(25, new GuiElementBuilder(Items.LEATHER_BOOTS)
                .setName(Text.literal("🟢 Gelatinous Rustler Boots")
                        .formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("━━━ RECIPE ━━━").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal("• 5x Slime Core").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• 8x Slime Ball").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• 1x Leather Boots").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("━━━ EFFECT ━━━").formatted(Formatting.RED))
                .addLoreLine(Text.literal("🦘 Super Jump: 3x jump height").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("🛡 No Fall Damage").formatted(Formatting.AQUA))
                .addLoreLine(Text.literal("💚 Death Save (1x per life)").formatted(Formatting.LIGHT_PURPLE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⚠ Requires: Slime Bounty Lvl 8").formatted(Formatting.RED))
                .glow()
                .build());

        // Warden Chestplate
        gui.setSlot(31, new GuiElementBuilder(Items.NETHERITE_CHESTPLATE)
                .setName(Text.literal("💀 Sculk Terror Chestplate")
                        .formatted(Formatting.DARK_AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("━━━ RECIPE ━━━").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal("• 8x Warden Core").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• 1x Netherite Chestplate").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• 4x Echo Shard").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("━━━ EFFECT ━━━").formatted(Formatting.RED))
                .addLoreLine(Text.literal("👁 ESP: See noisy entities (64 blocks)").formatted(Formatting.AQUA))
                .addLoreLine(Text.literal("🌙 Night Vision: Permanent").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("🛡 Darkness Immunity").formatted(Formatting.LIGHT_PURPLE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⚠ Requires: Warden Bounty Lvl 12").formatted(Formatting.RED))
                .glow()
                .build());

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ============================================================
    // HPEBM RECIPES
    // ============================================================
    public static void openHPEBMRecipes(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⚡ HPEBM Recipes"));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.LIGHT_BLUE_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Base HPEBM
        gui.setSlot(19, new GuiElementBuilder(Items.DIAMOND_SHOVEL)
                .setName(Text.literal("⚡ HPEBM Mk1 (Base)")
                        .formatted(Formatting.WHITE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("━━━ RECIPE ━━━").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal("• 1x Diamond Shovel").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• 4x Redstone Block").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• 4x Glowstone").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Damage: 15").formatted(Formatting.RED))
                .build());

        // Upgrade Recipe
        gui.setSlot(22, new GuiElementBuilder(Items.CRAFTING_TABLE)
                .setName(Text.literal("⬆ Standard Upgrade Recipe")
                        .formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("━━━ RECIPE ━━━").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal("[Star] [Core] [Star]").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("[Star] [Beam] [Star]").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("[Star] [Breath] [Star]").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Materials:").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("• 6x Nether Star").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• 1x Warden Core").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• 1x Dragon's Breath").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• 1x Current Beam Weapon").formatted(Formatting.GRAY))
                .build());

        // Tier List
        gui.setSlot(25, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("📊 Tier List")
                        .formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Mk1 → Ultra: 15 → 25 dmg").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("Ultra → Mk1: 25 → 35 dmg").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("Mk1 → Mk2: 35 → 45 dmg").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Mk2 → Mk3: 45 → 55 dmg").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal("Mk3 → Mk4: 55 → 65 dmg").formatted(Formatting.RED))
                .addLoreLine(Text.literal("Mk4 → Mk5: 65 → 75 dmg").formatted(Formatting.DARK_RED))
                .addLoreLine(Text.literal("Mk5 → Ultra OC: 75 → 100 dmg").formatted(Formatting.LIGHT_PURPLE))
                .build());

        // Ultra Overclocked Recipe
        gui.setSlot(31, new GuiElementBuilder(Items.DIAMOND_SHOVEL)
                .setName(Text.literal("✦ Ultra Overclocked Recipe")
                        .formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("━━━ RECIPE ━━━").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal("[Core] [Core] [Core]").formatted(Formatting.LIGHT_PURPLE))
                .addLoreLine(Text.literal("[Core] [Mk5] [Core]").formatted(Formatting.LIGHT_PURPLE))
                .addLoreLine(Text.literal("[Star] [Star] [Star]").formatted(Formatting.LIGHT_PURPLE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Materials:").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("• 5x Warden Core").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• 3x Nether Star").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• 1x HPEBM Mk5").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Damage: 100").formatted(Formatting.RED, Formatting.BOLD))
                .glow()
                .build());

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ============================================================
    // SPECIAL ITEMS RECIPES
    // ============================================================
    public static void openSpecialRecipes(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("✦ Special Item Recipes"));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // The Gavel
        gui.setSlot(22, new GuiElementBuilder(Items.WOODEN_AXE)
                .setName(Text.literal("⚖ The Gavel")
                        .formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("━━━ RECIPE ━━━").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal("• 2x Warden Core").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• 2x Netherite Ingot").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• 1x Wooden Axe").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("━━━ EFFECT ━━━").formatted(Formatting.RED))
                .addLoreLine(Text.literal("⚖ Order in Court!").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Massive knockback + stun").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⚠ Requires: Warden Bounty Lvl 7").formatted(Formatting.RED))
                .glow()
                .build());

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ============================================================
    // HELPER: Open methods that may not exist yet
    // ============================================================
    public static void openBountyRecipes(ServerPlayerEntity player) {
        openBountySwordsT1(player);
    }

    public static void openUpgradedRecipes(ServerPlayerEntity player) {
        openBountySwordsT2(player);
    }
}