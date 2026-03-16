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
import java.util.Map;

public class CustomItemsGui {

    // ════════════════════════════════════════════════════════════
    // MAIN MENU
    // ════════════════════════════════════════════════════════════

    public static void open(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("✨ Custom Collection"));

        // Decorative background with symmetric pattern
        for (int i = 0; i < 54; i++) {
            if (i % 9 == 0 || i % 9 == 8 || i < 9 || i >= 45) {
                gui.setSlot(i, new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE)
                        .setName(Text.literal("⬛").formatted(Formatting.DARK_PURPLE))
                        .build());
            } else {
                gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                        .setName(Text.literal("")).build());
            }
        }

        // Center header
        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("🌟 Custom Collection").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🎮 Browse exclusive items").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("💎 Premium gear & tools").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("⚔️ Unique weapons & armor").formatted(Formatting.GRAY))
                .glow()
                .build());

        // Top row - Weapon categories (symmetric layout)
        gui.setSlot(10, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Text.literal("⚔️ Bounty Swords (T1)").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🗡️ Tier 1 bounty weapons").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("🎯 7 slayer types").formatted(Formatting.DARK_GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to explore").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openSwordsMenu(player))
                .build());

        gui.setSlot(11, new GuiElementBuilder(Items.DIAMOND_SWORD)
                .setName(Text.literal("⚔️ Bounty Swords (T2)").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🗡️ Tier 2 bounty weapons").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("🎯 Upgraded versions").formatted(Formatting.DARK_GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to explore").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openSwordsMenu(player))
                .build());

        gui.setSlot(12, new GuiElementBuilder(Items.BLAZE_ROD)
                .setName(Text.literal("⚡ HPEBM Weapons").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⚡ Energy beam weapons").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("🔋 All marks available").formatted(Formatting.DARK_GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to explore").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openHPEBMMenu(player))
                .build());

        gui.setSlot(13, new GuiElementBuilder(Items.NETHERITE_SWORD)
                .setName(Text.literal("🗡️ Special Weapons").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🎁 Unique special weapons").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("🪄 The Gavel, Harvey's Stick").formatted(Formatting.DARK_GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to explore").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openSpecialWeaponsMenu(player))
                .build());

        gui.setSlot(14, new GuiElementBuilder(Items.WITHER_SKELETON_SKULL)
                .setName(Text.literal("💀 Boss Drops").formatted(Formatting.DARK_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🎁 Unique boss rewards").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("❤️ Undead Heart, 📻 Echoing Core").formatted(Formatting.DARK_GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to explore").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openBossDropsMenu(player))
                .glow()
                .build());

        gui.setSlot(15, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Text.literal("🧱 Materials").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🔨 Chunks & Cores").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("📦 All slayer types").formatted(Formatting.DARK_GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to explore").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMaterialsMenu(player))
                .build());

        gui.setSlot(16, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Text.literal("👑 Piglin Collection").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("💰 Premium piglin items").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("👑 Crowns, Cores, Armor").formatted(Formatting.DARK_GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to explore").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openPiglinMenu(player))
                .glow()
                .build());

        // Second row - Armor categories (symmetric layout)
        gui.setSlot(19, new GuiElementBuilder(Items.IRON_CHESTPLATE)
                .setName(Text.literal("🛡️ Bounty Armor (T1)").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🛡️ Tier 1 bounty armor").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("🎯 7 armor sets").formatted(Formatting.DARK_GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to explore").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openArmorMenu(player, 1))
                .build());

        gui.setSlot(20, new GuiElementBuilder(Items.DIAMOND_CHESTPLATE)
                .setName(Text.literal("🛡️ Bounty Armor (T2)").formatted(Formatting.DARK_AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🛡️ Tier 2 bounty armor").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("📈 Enhanced protection").formatted(Formatting.DARK_GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to explore").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openArmorMenu(player, 2))
                .build());

        gui.setSlot(21, new GuiElementBuilder(Items.GOLDEN_CHESTPLATE)
                .setName(Text.literal("👑 Gold Armor Evolution").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("📈 5 tiers of gold armor").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("⬆️ Pure → Polished → Gilded").formatted(Formatting.DARK_GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to explore").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openGoldArmorMenu(player))
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.NETHERITE_HELMET)
                .setName(Text.literal("👑 Legendary Gear").formatted(Formatting.DARK_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🌟 Ultra-rare items").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("👑 Voidwalker's Crown").formatted(Formatting.DARK_GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to explore").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openLegendaryMenu(player))
                .glow()
                .build());

        gui.setSlot(23, new GuiElementBuilder(Items.ENCHANTED_BOOK)
                .setName(Text.literal("✨ Weapon Attributes").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🔥 Weapon ability tokens").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("⚡ 10 unique attributes").formatted(Formatting.DARK_GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to explore").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openAttributeTokensMenu(player))
                .glow()
                .build());

        gui.setSlot(24, new GuiElementBuilder(Items.GOLD_NUGGET)
                .setName(Text.literal("💰 Compacted Materials").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🧱 Dense material blocks").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("⭐ Super compacted ores").formatted(Formatting.DARK_GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to explore").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCompactedMenu(player))
                .build());

        // Bottom navigation
        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("❌ Close").formatted(Formatting.DARK_RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Exit collection").formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> player.closeHandledScreen())
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    // HELPER METHODS - Placeholder implementations
    // ════════════════════════════════════════════════════════════

    private static void openHPEBMMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⚡ HPEBM Weapons Collection"));

        // Decorative background
        for (int i = 0; i < 54; i++) {
            if (i % 9 == 0 || i % 9 == 8 || i < 9 || i >= 45) {
                gui.setSlot(i, new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE)
                        .setName(Text.literal("⬛").formatted(Formatting.LIGHT_PURPLE))
                        .build());
            } else {
                gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                        .setName(Text.literal("")).build());
            }
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.BLAZE_ROD)
                .setName(Text.literal("⚡ HPEBM Weapons").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⚡ Energy beam weapons").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("🔋 All marks available").formatted(Formatting.DARK_GRAY))
                .glow()
                .build());

        // HPEBM Weapons
        ItemStack[] hpebmWeapons = {
            CustomItemHandler.createHPEBM(1),
            CustomItemHandler.createHPEBM(2),
            CustomItemHandler.createHPEBM(3),
            CustomItemHandler.createHPEBM(4),
            CustomItemHandler.createHPEBM(5),
            CustomItemHandler.createUltraOverclockedBeam()
        };

        int[] hpebmSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};

        for (int i = 0; i < Math.min(hpebmWeapons.length, hpebmSlots.length); i++) {
            ItemStack weapon = hpebmWeapons[i];
            gui.setSlot(hpebmSlots[i], new GuiElementBuilder(weapon.getItem())
                    .setName(weapon.get(DataComponentTypes.CUSTOM_NAME))
                    .setCount(1)
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("⚡ High-Powered Energy Beam").formatted(Formatting.LIGHT_PURPLE))
                    .addLoreLine(Text.literal("🔋 Mark " + (i + 1) + " Weapon").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✨ Click to give").formatted(Formatting.YELLOW))
                    .setCallback((idx, type, action) -> {
                        player.getInventory().insertStack(weapon.copy());
                        player.sendMessage(Text.literal("✨ Given " + weapon.get(DataComponentTypes.CUSTOM_NAME).getString() + "!").formatted(Formatting.GREEN), false);
                    })
                    .build());
        }

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("Back").formatted(Formatting.RED))
                .setCallback((index, type, action) -> open(player))
                .build());

        gui.open();
    }

    private static void openCompactedMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⬛ Compacted Materials Collection"));

        // Decorative background
        for (int i = 0; i < 54; i++) {
            if (i % 9 == 0 || i % 9 == 8 || i < 9 || i >= 45) {
                gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                        .setName(Text.literal("⬛").formatted(Formatting.GRAY))
                        .build());
            } else {
                gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                        .setName(Text.literal("")).build());
            }
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.IRON_BLOCK)
                .setName(Text.literal("⬛ Compacted Materials").formatted(Formatting.GRAY, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🏭 Compressed ore materials").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("💎 Space-efficient storage").formatted(Formatting.DARK_GRAY))
                .glow()
                .build());

        // Compacted Materials
        ItemStack[] compactedMaterials = {
            SlayerItems.createCompactedIron(),
            SlayerItems.createCompactedGold(),
            SlayerItems.createCompactedDiamond(),
            SlayerItems.createCompactedEmerald(),
            SlayerItems.createCompactedNetherite(),
            SlayerItems.createCompactedCopper(),
            SlayerItems.createCompactedCoal(),
            SlayerItems.createCompactedLapis(),
            SlayerItems.createCompactedRedstone(),
            SlayerItems.createCompactedQuartz()
        };

        int[] compactedSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};

        for (int i = 0; i < Math.min(compactedMaterials.length, compactedSlots.length); i++) {
            ItemStack material = compactedMaterials[i];
            gui.setSlot(compactedSlots[i], new GuiElementBuilder(material.getItem())
                    .setName(material.get(DataComponentTypes.CUSTOM_NAME))
                    .setCount(1)
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("🏭 " + material.getItem().toString().replace("_", " ").toUpperCase()).formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("✨ Click to give").formatted(Formatting.YELLOW))
                    .setCallback((idx, type, action) -> {
                        player.getInventory().insertStack(material.copy());
                        player.sendMessage(Text.literal("✨ Given " + material.get(DataComponentTypes.CUSTOM_NAME).getString() + "!").formatted(Formatting.GREEN), false);
                    })
                    .build());
        }

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("Back").formatted(Formatting.RED))
                .setCallback((index, type, action) -> open(player))
                .build());

        gui.open();
    }

    private static void openSwordsMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⚔️ Bounty Swords Collection"));

        // Decorative background
        for (int i = 0; i < 54; i++) {
            if (i % 9 == 0 || i % 9 == 8 || i < 9 || i >= 45) {
                gui.setSlot(i, new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE)
                        .setName(Text.literal("⬛").formatted(Formatting.DARK_RED))
                        .build());
            } else {
                gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                        .setName(Text.literal("")).build());
            }
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.DIAMOND_SWORD)
                .setName(Text.literal("⚔️ Bounty Swords").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🗡️ All bounty tier weapons").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("🎯 T1 & T2 variants").formatted(Formatting.DARK_GRAY))
                .glow()
                .build());

        // T1 Swords Row
        int[] t1Slots = {10, 11, 12, 13, 14, 15, 16, 19};
        SlayerManager.SlayerType[] types = SlayerManager.SlayerType.values();
        
        for (int i = 0; i < Math.min(types.length, t1Slots.length); i++) {
            SlayerManager.SlayerType slayerType = types[i];
            ItemStack sword = SlayerItems.createSlayerSword(slayerType);
            
            gui.setSlot(t1Slots[i], new GuiElementBuilder(sword.getItem())
                    .setName(sword.get(DataComponentTypes.CUSTOM_NAME))
                    .setCount(1)
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("🗡️ " + slayerType.toString() + " Slayer").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("⭐ Tier 1 Weapon").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✨ Click to view details").formatted(Formatting.GREEN))
                    .setCallback((idx, type, action) -> {
                        player.sendMessage(Text.literal("Viewing " + slayerType.toString() + " T1 Sword details").formatted(Formatting.AQUA), false);
                    })
                    .glow()
                    .build());
        }

        // T2 Swords Row
        int[] t2Slots = {20, 21, 22, 23, 24, 25, 28};
        for (int i = 0; i < Math.min(types.length, t2Slots.length); i++) {
            SlayerManager.SlayerType slayerType = types[i];
            ItemStack sword = SlayerItems.createUpgradedSlayerSword(slayerType);
            
            gui.setSlot(t2Slots[i], new GuiElementBuilder(sword.getItem())
                    .setName(sword.get(DataComponentTypes.CUSTOM_NAME))
                    .setCount(1)
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("🗡️ " + slayerType.toString() + " Slayer").formatted(Formatting.AQUA))
                    .addLoreLine(Text.literal("⭐ Tier 2 Weapon").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✨ Enhanced version").formatted(Formatting.GREEN))
                    .setCallback((idx, type, action) -> {
                        player.sendMessage(Text.literal("Viewing " + slayerType.toString() + " T2 Sword details").formatted(Formatting.AQUA), false);
                    })
                    .glow()
                    .build());
        }

        // Special Weapons
        gui.setSlot(31, new GuiElementBuilder(SlayerItems.createTheGavel())
                .setName(SlayerItems.createTheGavel().get(DataComponentTypes.CUSTOM_NAME))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⚖️ The Gavel").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal("🎁 Special judge weapon").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Ultimate authority").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> {
                    player.sendMessage(Text.literal("Viewing The Gavel details").formatted(Formatting.GOLD), false);
                })
                .glow()
                .build());

        gui.setSlot(33, new GuiElementBuilder(CustomItemHandler.createHarveysStick())
                .setName(CustomItemHandler.createHarveysStick().get(DataComponentTypes.CUSTOM_NAME))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🪄 Harvey's Stick").formatted(Formatting.LIGHT_PURPLE))
                .addLoreLine(Text.literal("🎁 Mysterious staff weapon").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Hidden powers").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> {
                    player.sendMessage(Text.literal("Viewing Harvey's Stick details").formatted(Formatting.LIGHT_PURPLE), false);
                })
                .glow()
                .build());

        // Midas's Sword
        gui.setSlot(34, new GuiElementBuilder(SlayerItems.createMidasSword())
                .setName(SlayerItems.createMidasSword().get(DataComponentTypes.CUSTOM_NAME))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⚔️ Midas's Sword").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal("🎁 Legendary golden weapon").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Touch of Midas").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> {
                    player.sendMessage(Text.literal("Viewing Midas's Sword details").formatted(Formatting.YELLOW), false);
                })
                .glow()
                .build());

        // Navigation
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Collection").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Return to main menu").formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> open(player))
                .build());

        gui.open();
    }

    private static void openSpecialWeaponsMenu(ServerPlayerEntity player) {
        // TODO: Implement special weapons menu
        player.sendMessage(Text.literal("Special weapons menu coming soon!").formatted(Formatting.YELLOW), false);
    }

    private static void openPiglinMenu(ServerPlayerEntity player) {
        // TODO: Implement piglin menu
        player.sendMessage(Text.literal("Piglin menu coming soon!").formatted(Formatting.YELLOW), false);
    }

    private static void openArmorMenu(ServerPlayerEntity player, int tier) {
        // TODO: Implement armor menu
        player.sendMessage(Text.literal("T" + tier + " armor menu coming soon!").formatted(Formatting.YELLOW), false);
    }

    private static void openGoldArmorMenu(ServerPlayerEntity player) {
        // TODO: Implement gold armor menu
        player.sendMessage(Text.literal("Gold armor menu coming soon!").formatted(Formatting.YELLOW), false);
    }

    private static void openLegendaryMenu(ServerPlayerEntity player) {
        // TODO: Implement legendary menu
        player.sendMessage(Text.literal("Legendary menu coming soon!").formatted(Formatting.YELLOW), false);
    }

    private static void openAttributeTokensMenu(ServerPlayerEntity player) {
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
        WeaponAttribute[] attributes = WeaponAttribute.values();
        
        for (int i = 0; i < 10 && i < slots.length; i++) {
            WeaponAttribute attr = attributes[i];
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
                .setCallback((idx, type, action) -> open(player))
                .build());

        gui.open();
    }

    private static void openBossDropsMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("💀 Boss Drops"));

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
        gui.setSlot(4, new GuiElementBuilder(Items.WITHER_SKELETON_SKULL)
                .setName(Text.literal("💀 Boss Drops").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🏆 Rare boss materials").formatted(Formatting.GRAY))
                .glow()
                .build());

        // Boss drop items
        int[] slots = {10, 11, 12, 19, 20, 21, 28, 29, 30};
        ItemStack[] drops = {
            SlayerItems.createCore(SlayerManager.SlayerType.WARDEN),
            SlayerItems.createCore(SlayerManager.SlayerType.ENDERMAN),
            SlayerItems.createCore(SlayerManager.SlayerType.PIGLIN),
            SlayerItems.createCore(SlayerManager.SlayerType.SPIDER),
            SlayerItems.createCore(SlayerManager.SlayerType.SKELETON),
            SlayerItems.createCore(SlayerManager.SlayerType.SLIME),
            SlayerItems.createCore(SlayerManager.SlayerType.ZOMBIE),
            SlayerItems.createUndeadHeart()
        };

        for (int i = 0; i < drops.length && i < slots.length; i++) {
            ItemStack drop = drops[i];
            List<Text> lore = new ArrayList<>();
            lore.add(Text.literal(""));
            lore.add(Text.literal(drop.get(DataComponentTypes.CUSTOM_NAME).getString()));
            lore.add(Text.literal(""));
            lore.add(Text.literal("🏆 Boss drop material").formatted(Formatting.GOLD));
            
            gui.setSlot(slots[i], new GuiElementBuilder(drop)
                    .setName(drop.get(DataComponentTypes.CUSTOM_NAME))
                    .setLore(lore)
                    .glow()
                    .build());
        }

        // Navigation
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> open(player))
                .build());

        gui.open();
    }

    private static void openMaterialsMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("🧱 Slayer Materials"));

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
        gui.setSlot(4, new GuiElementBuilder(Items.IRON_INGOT)
                .setName(Text.literal("🧱 Slayer Materials").formatted(Formatting.GRAY, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("📦 All slayer type materials").formatted(Formatting.GOLD))
                .glow()
                .build());

        // Material items
        int[] slots = {10, 11, 12, 19, 20, 21, 28, 29, 30};
        SlayerManager.SlayerType[] types = SlayerManager.SlayerType.values();
        
        for (int i = 0; i < Math.min(types.length, slots.length); i++) {
            SlayerManager.SlayerType type = types[i];
            ItemStack material = SlayerItems.createCore(type);
            
            List<Text> lore = new ArrayList<>();
            lore.add(Text.literal(""));
            lore.add(Text.literal(type.toString() + " Core").formatted(Formatting.YELLOW));
            lore.add(Text.literal(""));
            lore.add(Text.literal("📦 Essential crafting material").formatted(Formatting.GRAY));
            
            gui.setSlot(slots[i], new GuiElementBuilder(material)
                    .setName(Text.literal(type.toString() + " Core").formatted(Formatting.YELLOW, Formatting.BOLD))
                    .setLore(lore)
                    .glow()
                    .build());
        }

        // Navigation
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> open(player))
                .build());

        gui.open();
    }
}
