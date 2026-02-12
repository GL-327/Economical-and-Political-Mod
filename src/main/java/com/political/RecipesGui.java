package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.component.DataComponentTypes;
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
        gui.setSlot(10, new GuiElementBuilder(Items.ZOMBIE_HEAD)
                .setName(Text.literal("☠ Zombie Berserker Helmet")
                        .formatted(Formatting.DARK_GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("━━━ RECIPE ━━━").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal(" [Core] [Core] [Core]").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(" [Core] [Helmet] [Core]").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(" [    ] [    ] [    ]").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Materials:").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(" 5x Zombie Core").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(" 1x Iron Helmet").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("━━━ EFFECT ━━━").formatted(Formatting.RED))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("❤ Health: -50%").formatted(Formatting.DARK_RED))
                .addLoreLine(Text.literal("⚔ Damage: +300%").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⚠ Requires: Zombie Bounty Lvl 12").formatted(Formatting.RED))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to view crafting grid!").formatted(Formatting.AQUA))
                .setCallback((idx, type, action) -> openZombieHelmetRecipe(player))
                .build());

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player))
                .build());

        gui.open();

        // Category: Bounty Weapons (slot 19)
        gui.setSlot(19, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Text.literal("⚔ Bounty Weapons")
                        .formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Slayer swords and").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("bounty hunting gear").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to view!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openBountyRecipes(player))
                .build());

        // Category: HPEBM Weapons (slot 21)
        gui.setSlot(21, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal("⚡ HPEBM Weapons")
                        .formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("High-Powered Energy").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Beam Weapons (Mk1-Mk5)").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to view!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openHPEBMRecipes(player))
                .build());

        // Category: Special Items (slot 23)
        gui.setSlot(23, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("✦ Special Items")
                        .formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Unique items like the").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Gavel and other tools").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to view!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openSpecialRecipes(player))
                .build());

        // Category: Upgraded Weapons (slot 25)
        gui.setSlot(25, new GuiElementBuilder(Items.DIAMOND_SWORD)
                .setName(Text.literal("⬆ Upgraded Weapons")
                        .formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Advanced versions of").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("bounty and special weapons").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to view!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openUpgradedRecipes(player))
                .build());

        // Category: Slayer Armor (slot 28) - THIS WAS AFTER gui.open()!
        gui.setSlot(28, new GuiElementBuilder(Items.DIAMOND_CHESTPLATE)
                .setName(Text.literal("🛡 Slayer Armor")
                        .formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Protective gear crafted from").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("bounty boss materials").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to view!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openArmorRecipes(player))
                .build());

        // Close button (slot 49)
        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("✖ Close").formatted(Formatting.RED))
                .setCallback((idx, type, action) -> player.closeHandledScreen())
                .build());

        // NOW open the GUI after all slots are set
        gui.open();
    }


    // ============================================================
    // BOUNTY/SLAYER WEAPONS RECIPES
    // ============================================================

    public static void openZombieHelmetRecipe(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("☠ Zombie Berserker Helmet Recipe"));

        // Dark background for dramatic effect
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.ZOMBIE_HEAD)
                .setName(Text.literal("☠ Zombie Berserker Helmet")
                        .formatted(Formatting.DARK_GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("A cursed helm that trades").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("vitality for raw power.").formatted(Formatting.GRAY))
                .glow()
                .build());

        // 3x3 Crafting Grid
        // Row 1: Core, Core, Core
        ItemStack zombieCore = SlayerItems.createCore(SlayerManager.SlayerType.ZOMBIE);

        gui.setSlot(10, new GuiElementBuilder(zombieCore.getItem())
                .setName(Text.literal("✦ Zombie Core ✦").formatted(Formatting.DARK_GREEN, Formatting.BOLD))
                .build());
        gui.setSlot(11, new GuiElementBuilder(zombieCore.getItem())
                .setName(Text.literal("✦ Zombie Core ✦").formatted(Formatting.DARK_GREEN, Formatting.BOLD))
                .build());
        gui.setSlot(12, new GuiElementBuilder(zombieCore.getItem())
                .setName(Text.literal("✦ Zombie Core ✦").formatted(Formatting.DARK_GREEN, Formatting.BOLD))
                .build());

        // Row 2: Core, Iron Helmet, Core
        gui.setSlot(19, new GuiElementBuilder(zombieCore.getItem())
                .setName(Text.literal("✦ Zombie Core ✦").formatted(Formatting.DARK_GREEN, Formatting.BOLD))
                .build());
        gui.setSlot(20, new GuiElementBuilder(Items.IRON_HELMET)
                .setName(Text.literal("Iron Helmet").formatted(Formatting.WHITE))
                .build());
        gui.setSlot(21, new GuiElementBuilder(zombieCore.getItem())
                .setName(Text.literal("✦ Zombie Core ✦").formatted(Formatting.DARK_GREEN, Formatting.BOLD))
                .build());

        // Row 3: Empty
        gui.setSlot(28, new GuiElementBuilder(Items.LIGHT_GRAY_STAINED_GLASS_PANE)
                .setName(Text.literal("")).build());
        gui.setSlot(29, new GuiElementBuilder(Items.LIGHT_GRAY_STAINED_GLASS_PANE)
                .setName(Text.literal("")).build());
        gui.setSlot(30, new GuiElementBuilder(Items.LIGHT_GRAY_STAINED_GLASS_PANE)
                .setName(Text.literal("")).build());

        // Arrow
        gui.setSlot(23, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("→").formatted(Formatting.GREEN, Formatting.BOLD))
                .build());

        // Result
        gui.setSlot(25, new GuiElementBuilder(Items.ZOMBIE_HEAD)
                .setName(Text.literal("☠ Zombie Berserker Helmet")
                        .formatted(Formatting.DARK_GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("❤ Health: -50%").formatted(Formatting.DARK_RED))
                .addLoreLine(Text.literal("⚔ Damage: +300%").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⚠ Requires: Zombie Lvl 12").formatted(Formatting.RED))
                .glow()
                .build());

        // Info panel
        gui.setSlot(40, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("ℹ How to Obtain Cores").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Zombie Cores drop from:").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(" The Undying Outlaw (Boss)").formatted(Formatting.DARK_GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Drop Rates:").formatted(Formatting.AQUA))
                .addLoreLine(Text.literal(" T1: 2%  T2: 6%  T3: 11%").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(" T4: 16%  T5: 20%").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("You need 5 cores for this recipe!").formatted(Formatting.RED))
                .build());

        // Warning about the helmet's effect
        gui.setSlot(42, new GuiElementBuilder(Items.WITHER_SKELETON_SKULL)
                .setName(Text.literal("⚠ WARNING").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("This helmet is DANGEROUS!").formatted(Formatting.DARK_RED))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Your max HP will be HALVED").formatted(Formatting.RED))
                .addLoreLine(Text.literal("while wearing this helmet.").formatted(Formatting.RED))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Recommended for experienced").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("players who can avoid damage!").formatted(Formatting.GRAY))
                .build());

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Armor").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openArmorRecipes(player))
                .build());

        // Close button
        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("✖ Close").formatted(Formatting.RED))
                .setCallback((idx, type, action) -> player.closeHandledScreen())
                .build());

        gui.open();
    }

    public static void openBountyRecipes(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⚔ Bounty Weapon Recipes"));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BROWN_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        int slot = 10;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            String chunkName = SlayerItems.getChunkName(type);

            List<Text> lore = new ArrayList<>();
            lore.add(Text.literal(""));
            lore.add(Text.literal("━━━ RECIPE ━━━").formatted(Formatting.GOLD));
            lore.add(Text.literal(""));
            lore.add(Text.literal("Materials:").formatted(Formatting.YELLOW));
            lore.add(Text.literal("  2x " + chunkName).formatted(Formatting.WHITE));
            lore.add(Text.literal(""));
            lore.add(Text.literal("Obtain chunks from:").formatted(Formatting.GRAY));
            lore.add(Text.literal("  " + type.bossName + " (Boss Drop)").formatted(type.color));
            lore.add(Text.literal(""));
            lore.add(Text.literal("Drop Rates:").formatted(Formatting.AQUA));
            lore.add(Text.literal("  T1: 2%  T2: 6%  T3: 11%").formatted(Formatting.GRAY));
            lore.add(Text.literal("  T4: 16%  T5: 20%").formatted(Formatting.GRAY));
            lore.add(Text.literal(""));
            lore.add(Text.literal("⚠ Requires Level 3 to use").formatted(Formatting.RED));

            gui.setSlot(slot, new GuiElementBuilder(Items.IRON_SWORD)
                    .setName(Text.literal(type.displayName + " Slayer Sword")
                            .formatted(type.color, Formatting.BOLD))
                    .setLore(lore)
                    .build());

            slot++;
            if (slot == 17) slot = 19; // Next row
        }

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back")
                        .formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ============================================================
    // HPEBM WEAPONS RECIPES
    // ============================================================

    public static void openHPEBMRecipes(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⚡ HPEBM Weapon Recipes"));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.CYAN_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Mk1 Recipe
        gui.setSlot(10, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal("HPEBM Mk1")
                        .formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("━━━ RECIPE ━━━").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Crafting Table:").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("  [ ] [End Rod] [ ]").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("  [ ] [Iron Shovel] [ ]").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("  [ ] [Redstone Block] [ ]").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Base Energy Weapon").formatted(Formatting.GRAY))
                .build());

        // Mk2 Recipe
        gui.setSlot(12, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal("HPEBM Mk2")
                        .formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("━━━ RECIPE ━━━").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Upgrade:").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("  1x HPEBM Mk1").formatted(Formatting.AQUA))
                .addLoreLine(Text.literal("  4x Diamond").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("  2x Blaze Powder").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Enhanced power output").formatted(Formatting.GRAY))
                .build());

        // Mk3 Recipe
        gui.setSlot(14, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal("HPEBM Mk3")
                        .formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("━━━ RECIPE ━━━").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Upgrade:").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("  1x HPEBM Mk2").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("  2x Nether Star Fragment").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("  8x Gold Block").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Military-grade output").formatted(Formatting.GRAY))
                .build());

        // Mk4 Recipe
        gui.setSlot(16, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal("HPEBM Mk4")
                        .formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("━━━ RECIPE ━━━").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Upgrade:").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("  1x HPEBM Mk3").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("  1x Nether Star").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("  4x Netherite Ingot").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Experimental prototype").formatted(Formatting.GRAY))
                .build());

        // Mk5 Recipe
        gui.setSlot(22, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal("HPEBM Mk5")
                        .formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("━━━ RECIPE ━━━").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Upgrade:").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("  1x HPEBM Mk4").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal("  2x Dragon Breath").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("  1x Elytra").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("  16x End Crystal").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§d§lLEGENDARY").formatted(Formatting.LIGHT_PURPLE))
                .addLoreLine(Text.literal("Ultimate energy weapon").formatted(Formatting.GRAY))
                .glow()
                .build());

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back")
                        .formatted(Formatting.YELLOW))
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

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Gavel Recipe
        gui.setSlot(11, new GuiElementBuilder(Items.WOODEN_AXE)
                .setName(Text.literal("⚖ The Gavel")
                        .formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("━━━ RECIPE ━━━").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Crafting Table:").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("  [Oak Plank] [Oak Plank] [Oak Plank]").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("  [ ] [Stick] [ ]").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("  [ ] [Stick] [ ]").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Special ability for Chair holder").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Right-click to use powers").formatted(Formatting.GRAY))
                .build());

        // Credit Item Recipe
        gui.setSlot(13, new GuiElementBuilder(Items.EMERALD)
                .setName(Text.literal("💎 Credit")
                        .formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("━━━ HOW TO OBTAIN ━━━").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Sources:").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("  • Bounty level-up rewards").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("  • Auction House sales").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("  • Admin grants").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Primary server currency").formatted(Formatting.GRAY))
                .build());

        // Coin Item Recipe
        gui.setSlot(15, new GuiElementBuilder(Items.SUNFLOWER)
                .setName(Text.literal("🪙 Coin")
                        .formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("━━━ HOW TO OBTAIN ━━━").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Sources:").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("  • Killing scaled mobs").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("  • Bounty boss drops").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("  • Trading with players").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Used to start bounty quests").formatted(Formatting.GRAY))
                .build());

        // Slayer Cores Display
        gui.setSlot(29, new GuiElementBuilder(Items.ENDER_PEARL)
                .setName(Text.literal("✦ Bounty Cores")
                        .formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("━━━ RARE BOSS DROPS ━━━").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Each bounty boss can drop").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("its unique core item:").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("  • Undead Core (Zombie)").formatted(Formatting.DARK_GREEN))
                .addLoreLine(Text.literal("  • Venom Core (Spider)").formatted(Formatting.DARK_RED))
                .addLoreLine(Text.literal("  • Bone Core (Skeleton)").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("  • Gel Core (Slime)").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("  • Void Core (Enderman)").formatted(Formatting.DARK_PURPLE))
                .addLoreLine(Text.literal("  • Sculk Core (Warden)").formatted(Formatting.DARK_AQUA))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Drop Rate: 0.01% - 1%").formatted(Formatting.RED))
                .glow()
                .build());

        // Slayer Chunks Display
        gui.setSlot(33, new GuiElementBuilder(Items.ROTTEN_FLESH)
                .setName(Text.literal("✦ Bounty Chunks")
                        .formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("━━━ BOSS DROPS ━━━").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Used to craft Bounty Swords:").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("  • Undead Chunk (Zombie)").formatted(Formatting.DARK_GREEN))
                .addLoreLine(Text.literal("  • Venomous Gland (Spider)").formatted(Formatting.DARK_RED))
                .addLoreLine(Text.literal("  • Ancient Bone (Skeleton)").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("  • Condensed Gel (Slime)").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("  • Void Fragment (Enderman)").formatted(Formatting.DARK_PURPLE))
                .addLoreLine(Text.literal("  • Sculk Heart (Warden)").formatted(Formatting.DARK_AQUA))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Drop Rate: 2% - 20%").formatted(Formatting.YELLOW))
                .build());

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back")
                        .formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ============================================================
    // UPGRADED WEAPONS RECIPES
    // ============================================================

    public static void openUpgradedRecipes(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⬆ Upgraded Weapon Recipes"));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.YELLOW_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        int slot = 10;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            String chunkName = SlayerItems.getChunkName(type);
            String coreName = type.displayName + " Core";

            List<Text> lore = new ArrayList<>();
            lore.add(Text.literal(""));
            lore.add(Text.literal("━━━ UPGRADE RECIPE ━━━").formatted(Formatting.GOLD));
            lore.add(Text.literal(""));
            lore.add(Text.literal("Materials:").formatted(Formatting.YELLOW));
            lore.add(Text.literal("  1x " + type.displayName + " Slayer Sword").formatted(Formatting.WHITE));
            lore.add(Text.literal("  1x " + coreName).formatted(Formatting.LIGHT_PURPLE));
            lore.add(Text.literal("  4x " + chunkName).formatted(Formatting.WHITE));
            lore.add(Text.literal(""));
            lore.add(Text.literal("Bonuses:").formatted(Formatting.AQUA));
            lore.add(Text.literal("  • 3x damage to " + type.displayName + "s").formatted(Formatting.GREEN));
            lore.add(Text.literal("  • Counts as 3 kills per kill").formatted(Formatting.GREEN));
            lore.add(Text.literal("  • Special ability (varies)").formatted(Formatting.GREEN));
            lore.add(Text.literal(""));
            lore.add(Text.literal("⚠ Requires Level 6 to use").formatted(Formatting.RED));

            gui.setSlot(slot, new GuiElementBuilder(Items.DIAMOND_SWORD)
                    .setName(Text.literal(type.displayName + " Slayer Sword II")
                            .formatted(type.color, Formatting.BOLD))
                    .setLore(lore)
                    .glow()
                    .build());

            slot++;
            if (slot == 16) slot = 19; // Next row
        }

        // Ender Sword (Special Legendary)
        gui.setSlot(31, new GuiElementBuilder(Items.NETHERITE_SWORD)
                .setName(Text.literal("⚔ Ender Sword")
                        .formatted(Formatting.DARK_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("━━━ LEGENDARY RECIPE ━━━").formatted(Formatting.LIGHT_PURPLE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Materials:").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("  1x Void Core (Enderman)").formatted(Formatting.DARK_PURPLE))
                .addLoreLine(Text.literal("  64x Obsidian").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("  16x Ender Pearl").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("  8x Blaze Rod").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Ability: Void Strike").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal("  Teleport behind target").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("  and deal massive damage").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§d§lLEGENDARY WEAPON"))
                .glow()
                .build());

        // Abyssal Blade (Warden Special)
        gui.setSlot(33, new GuiElementBuilder(Items.NETHERITE_SWORD)
                .setName(Text.literal("⚔ Abyssal Blade")
                        .formatted(Formatting.DARK_AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("━━━ MYTHIC RECIPE ━━━").formatted(Formatting.DARK_AQUA))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Materials:").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("  1x Sculk Core (Warden)").formatted(Formatting.DARK_AQUA))
                .addLoreLine(Text.literal("  32x Echo Shard").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("  16x Sculk Catalyst").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("  1x Nether Star").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Ability: Sonic Devastation").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal("  Release a shockwave that").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("  stuns and damages all enemies").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§5§lMYTHIC WEAPON"))
                .glow()
                .build());

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back")
                        .formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }
    // ============================================================
// SLAYER ARMOR RECIPES
// ============================================================
    private static void setupCraftingGrid(SimpleGui gui, ItemStack[][] recipe, ItemStack result) {
        // 3x3 crafting grid positions (centered in GUI)
        // Layout:
        //   10 11 12    ->   14 (result)
        //   19 20 21
        //   28 29 30

        int[][] gridSlots = {
                {10, 11, 12},
                {19, 20, 21},
                {28, 29, 30}
        };

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                ItemStack ingredient = recipe[row][col];
                if (ingredient != null && !ingredient.isEmpty()) {
                    gui.setSlot(gridSlots[row][col], new GuiElementBuilder(ingredient.getItem())
                            .setCount(ingredient.getCount())
                            .setName(ingredient.getName())
                            .build());
                } else {
                    gui.setSlot(gridSlots[row][col], new GuiElementBuilder(Items.LIGHT_GRAY_STAINED_GLASS_PANE)
                            .setName(Text.literal(""))
                            .build());
                }
            }
        }

        // Arrow indicator
        gui.setSlot(13, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("→").formatted(Formatting.WHITE))
                .build());

        // Result slot
        gui.setSlot(14, new GuiElementBuilder(result.getItem())
                .setName(result.getName())
                .glow()
                .build());
    }

    public static void openArmorRecipes(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("🛡 Slayer Armor Recipes"));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }


        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.DIAMOND_CHESTPLATE)
                .setName(Text.literal("🛡 Slayer Armor").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Craft protective gear from").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("bounty boss drops!").formatted(Formatting.GRAY))
                .build());

        // T1 Armor info
        gui.setSlot(19, new GuiElementBuilder(Items.IRON_CHESTPLATE)
                .setName(Text.literal("T1 Slayer Armor").formatted(Formatting.WHITE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("━━━ RECIPE ━━━").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Helmet: 5x Chunks").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Chestplate: 8x Chunks").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Leggings: 7x Chunks").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Boots: 4x Chunks").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Total: 24x Chunks").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Requires Level 4").formatted(Formatting.RED))
                .build());

        // T2 Armor info
        gui.setSlot(21, new GuiElementBuilder(Items.DIAMOND_CHESTPLATE)
                .setName(Text.literal("T2 Slayer Armor").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("━━━ UPGRADE RECIPE ━━━").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Each piece requires:").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("  1x T1 Armor Piece").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("  1x Slayer Core").formatted(Formatting.LIGHT_PURPLE))
                .addLoreLine(Text.literal("  4x Chunks").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Total: 1 Core + 4 Chunks per piece").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Requires Level 8").formatted(Formatting.RED))
                .glow()
                .build());

        // Individual slayer type armor
        int[] slots = {28, 29, 30, 32, 33, 34};
        SlayerManager.SlayerType[] types = SlayerManager.SlayerType.values();

        for (int i = 0; i < types.length && i < slots.length; i++) {
            SlayerManager.SlayerType type = types[i];
            String chunkName = SlayerItems.getChunkName(type);

            gui.setSlot(slots[i], new GuiElementBuilder(type.icon)
                    .setName(Text.literal(type.displayName + " Armor Set").formatted(type.color, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("T1 Materials:").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("  24x " + chunkName).formatted(Formatting.WHITE))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("T2 Upgrade Materials:").formatted(Formatting.AQUA))
                    .addLoreLine(Text.literal("  4x " + type.displayName + " Core").formatted(Formatting.LIGHT_PURPLE))
                    .addLoreLine(Text.literal("  16x " + chunkName).formatted(Formatting.WHITE))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Set Bonus:").formatted(Formatting.GOLD))
                    .addLoreLine(Text.literal("  Damage reduction vs " + type.displayName + "s").formatted(Formatting.GREEN))
                    .build());
        }

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }
    // ============================================================
// CRAFTING TABLE STYLE RECIPE DISPLAY
// ============================================================
    private static void displayCraftingTableRecipe(SimpleGui gui, ServerPlayerEntity player,
                                                   ItemStack output, ItemStack[] recipe, String recipeName, Text description) {

        // Clear previous recipe area
        for (int i = 0; i < 54; i++) {
            if (i < 45) { // Don't clear navigation row
                gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                        .setName(Text.literal(""))
                        .build());
            }
        }

        // Title
        gui.setSlot(4, new GuiElementBuilder(Items.CRAFTING_TABLE)
                .setName(Text.literal("📖 " + recipeName).formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(description)
                .build());

        // Crafting Grid (3x3) - Slots 10,11,12 / 19,20,21 / 28,29,30
        int[] craftingSlots = {10, 11, 12, 19, 20, 21, 28, 29, 30};

        for (int i = 0; i < 9; i++) {
            if (i < recipe.length && recipe[i] != null && !recipe[i].isEmpty()) {
                gui.setSlot(craftingSlots[i], new GuiElementBuilder(recipe[i].getItem())
                        .setCount(recipe[i].getCount())
                        .setName(recipe[i].getName())
                        .build());
            } else {
                // Empty slot indicator
                gui.setSlot(craftingSlots[i], new GuiElementBuilder(Items.LIGHT_GRAY_STAINED_GLASS_PANE)
                        .setName(Text.literal("Empty").formatted(Formatting.DARK_GRAY))
                        .build());
            }
        }

        // Arrow pointing to output
        gui.setSlot(23, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("→").formatted(Formatting.WHITE))
                .build());

        // Output item
        gui.setSlot(24, new GuiElementBuilder(output.getItem())
                .setName(output.getName())
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("OUTPUT").formatted(Formatting.GREEN, Formatting.BOLD))
                .glow()
                .build());
    }
    // ============================================================
// BOUNTY ITEM RECIPES
// ============================================================

    private static void showZombieBerserkerHelmetRecipe(SimpleGui gui, ServerPlayerEntity player) {
        ItemStack[] recipe = new ItemStack[9];
        // 5 Zombie Cores + 1 Zombie Head + 3 Rotten Flesh
        recipe[0] = createCoreDisplay("Zombie Core");
        recipe[1] = new ItemStack(Items.ZOMBIE_HEAD);
        recipe[2] = createCoreDisplay("Zombie Core");
        recipe[3] = createCoreDisplay("Zombie Core");
        recipe[4] = new ItemStack(Items.ROTTEN_FLESH, 3);
        recipe[5] = createCoreDisplay("Zombie Core");
        recipe[6] = null;
        recipe[7] = createCoreDisplay("Zombie Core");
        recipe[8] = null;

        displayCraftingTableRecipe(gui, player,
                SlayerItems.createZombieBerserkerHelmet(),
                recipe,
                "Zombie Berserker Helmet",
                Text.literal("Requires: Zombie Bounty Level 6").formatted(Formatting.RED));
    }

    private static void showSpiderLeggingsRecipe(SimpleGui gui, ServerPlayerEntity player) {
        ItemStack[] recipe = new ItemStack[9];
        // 5 Spider Cores + 8 String + 1 Spider Eye
        recipe[0] = createCoreDisplay("Spider Core");
        recipe[1] = new ItemStack(Items.STRING, 4);
        recipe[2] = createCoreDisplay("Spider Core");
        recipe[3] = createCoreDisplay("Spider Core");
        recipe[4] = new ItemStack(Items.SPIDER_EYE);
        recipe[5] = createCoreDisplay("Spider Core");
        recipe[6] = new ItemStack(Items.STRING, 4);
        recipe[7] = createCoreDisplay("Spider Core");
        recipe[8] = null;

        displayCraftingTableRecipe(gui, player,
                SlayerItems.createSpiderLeggings(),
                recipe,
                "Venomous Crawler Leggings",
                Text.literal("Requires: Spider Bounty Level 12").formatted(Formatting.RED));
    }

    private static void showSkeletonBowRecipe(SimpleGui gui, ServerPlayerEntity player) {
        ItemStack[] recipe = new ItemStack[9];
        // 5 Skeleton Cores + 1 Bow + 3 Bones
        recipe[0] = createCoreDisplay("Skeleton Core");
        recipe[1] = new ItemStack(Items.BONE);
        recipe[2] = createCoreDisplay("Skeleton Core");
        recipe[3] = createCoreDisplay("Skeleton Core");
        recipe[4] = new ItemStack(Items.BOW);
        recipe[5] = createCoreDisplay("Skeleton Core");
        recipe[6] = new ItemStack(Items.BONE);
        recipe[7] = createCoreDisplay("Skeleton Core");
        recipe[8] = new ItemStack(Items.BONE);

        displayCraftingTableRecipe(gui, player,
                SlayerItems.createSkeletonBow(),
                recipe,
                "Bone Desperado's Longbow",
                Text.literal("Requires: Skeleton Bounty Level 10").formatted(Formatting.RED));
    }

    private static void showSlimeBootsRecipe(SimpleGui gui, ServerPlayerEntity player) {
        ItemStack[] recipe = new ItemStack[9];
        // 5 Slime Cores + 8 Slime Balls + 1 Leather Boots
        recipe[0] = createCoreDisplay("Slime Core");
        recipe[1] = new ItemStack(Items.SLIME_BALL, 4);
        recipe[2] = createCoreDisplay("Slime Core");
        recipe[3] = createCoreDisplay("Slime Core");
        recipe[4] = new ItemStack(Items.LEATHER_BOOTS);
        recipe[5] = createCoreDisplay("Slime Core");
        recipe[6] = new ItemStack(Items.SLIME_BALL, 4);
        recipe[7] = createCoreDisplay("Slime Core");
        recipe[8] = null;

        displayCraftingTableRecipe(gui, player,
                SlayerItems.createSlimeBoots(),
                recipe,
                "Gelatinous Rustler Boots",
                Text.literal("Requires: Slime Bounty Level 8").formatted(Formatting.RED));
    }

    private static void showWardenChestplateRecipe(SimpleGui gui, ServerPlayerEntity player) {
        ItemStack[] recipe = new ItemStack[9];
        // 8 Warden Cores + 1 Netherite Chestplate + 4 Echo Shards
        recipe[0] = createCoreDisplay("Warden Core");
        recipe[1] = createCoreDisplay("Warden Core");
        recipe[2] = createCoreDisplay("Warden Core");
        recipe[3] = createCoreDisplay("Warden Core");
        recipe[4] = new ItemStack(Items.NETHERITE_CHESTPLATE);
        recipe[5] = createCoreDisplay("Warden Core");
        recipe[6] = createCoreDisplay("Warden Core");
        recipe[7] = new ItemStack(Items.ECHO_SHARD, 4);
        recipe[8] = createCoreDisplay("Warden Core");

        displayCraftingTableRecipe(gui, player,
                SlayerItems.createWardenChestplate(),
                recipe,
                "Sculk Terror Chestplate",
                Text.literal("Requires: Warden Bounty Level 12").formatted(Formatting.RED));
    }

    // Helper to create a core display item
    private static ItemStack createCoreDisplay(String coreName) {
        Item icon = switch (coreName) {
            case "Zombie Core" -> Items.ROTTEN_FLESH;
            case "Spider Core" -> Items.SPIDER_EYE;
            case "Skeleton Core" -> Items.BONE;
            case "Slime Core" -> Items.SLIME_BALL;
            case "Warden Core" -> Items.ECHO_SHARD;
            default -> Items.NETHER_STAR;
        };

        ItemStack stack = new ItemStack(icon);
        stack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(coreName).formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return stack;
    }
}