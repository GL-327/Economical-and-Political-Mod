package com.political;


import com.political.bounty.SlayerManager;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SignGui;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public class BossConfigGui {

    public static void openMainMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("⚙ Boss Configuration").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.black())
                    .setName(Component.literal("")).build());
        }

        gui.setSlot(11, new GuiElementBuilder(Items.BEACON)
                .setName(Component.literal("Boss Stats").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("HP, Damage, XP per boss").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("type & tier").withStyle(ChatFormatting.GRAY))
                .setCallback((idx, type, containerInput, slotGui) -> openBossTypeMenu(player))
                .build());

        gui.setSlot(13, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Component.literal("Sword Damage %").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("T1: " + DataManager.getT1SwordDamagePercent() + "%").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("T2: " + DataManager.getT2SwordDamagePercent() + "%").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openSwordDamageMenu(player))
                .build());

        gui.setSlot(15, new GuiElementBuilder(Items.BOOK)
                .setName(Component.literal("Abilities & Cooldowns").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Toggle abilities on/off").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Set ability cooldowns").withStyle(ChatFormatting.GRAY))
                .setCallback((idx, type, containerInput, slotGui) -> openAbilitiesMenu(player))
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.DIAMOND_CHESTPLATE)
                .setName(Component.literal("Boss Gear & Defense").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Configure boss armor").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("weapons & defense").withStyle(ChatFormatting.GRAY))
                .setCallback((idx, type, containerInput, slotGui) -> openGearDefenseMenu(player))
                .build());

        gui.setSlot(26, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((idx, type, containerInput, slotGui) -> {})
                .build());

        gui.open();
    }

    // ==================== BOSS STATS ====================

    private static void openBossTypeMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("Select Boss Type").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.black())
                    .setName(Component.literal("")).build());
        }

        SlayerManager.SlayerType[] types = SlayerManager.SlayerType.values();
        int[] slots = {11, 12, 13, 14, 15, 20, 21, 22};

        for (int i = 0; i < types.length && i < slots.length; i++) {
            final SlayerManager.SlayerType bossType = types[i];
            gui.setSlot(slots[i], new GuiElementBuilder(Items.PAPER)
                    .setName(Component.literal(bossType.displayName).withStyle(bossType.color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Click to configure").withStyle(ChatFormatting.GREEN))
                    .setCallback((idx, clicktype, containerInput, slotGui) -> openTierMenu(player, bossType))
                    .build());
        }

        gui.setSlot(27, new GuiElementBuilder(Items.WOOL.green())
                .setName(Component.literal("Reset All Stats").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .setCallback((idx, clicktype, containerInput, slotGui) -> {
                    DataManager.resetAllBossConfigs();
                    DataManager.save(PoliticalServer.server);
                    player.sendSystemMessage(Component.literal("✓ Reset all boss configs to defaults").withStyle(ChatFormatting.GREEN), false);
                    openBossTypeMenu(player);
                })
                .build());

        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((idx, clicktype, containerInput, slotGui) -> openMainMenu(player))
                .build());

        gui.open();
    }

    private static void openTierMenu(ServerPlayer player, SlayerManager.SlayerType bossType) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal(bossType.bossName + " Config").withStyle(bossType.color, ChatFormatting.BOLD));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.black())
                    .setName(Component.literal("")).build());
        }

        int[] tierSlots = {10, 11, 12, 13, 14, 19, 20, 21, 22, 23};
        ChatFormatting[] tierColors = {ChatFormatting.WHITE, ChatFormatting.GREEN, ChatFormatting.BLUE, ChatFormatting.GOLD, ChatFormatting.LIGHT_PURPLE};

        for (int t = 1; t <= 5; t++) {
            final int tierNum = t;
            SlayerManager.TierConfig config = SlayerManager.getTierConfig(tierNum);

            double hp = config.getActualHp(bossType) * DataManager.getBossHpMultiplier(bossType);
            double dmg = config.getActualDamage(bossType) * DataManager.getBossDamageMultiplier(bossType);
            int xp = (int)(config.xpReward * DataManager.getBossXpMultiplier(bossType));

            gui.setSlot(tierSlots[t - 1], new GuiElementBuilder(Items.PAPER)
                    .setName(Component.literal("Tier " + t).withStyle(tierColors[t - 1], ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("❤ HP: " + String.format("%,.0f", hp)).withStyle(ChatFormatting.RED))
                    .addLoreLine(Component.literal("⚔ DMG: " + String.format("%,.0f", dmg)).withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal("✨ XP: " + xp).withStyle(ChatFormatting.AQUA))
                    .addLoreLine(Component.literal("Click to edit").withStyle(ChatFormatting.GREEN))
                    .setCallback((idx, clicktype, containerInput, slotGui) -> openEditMenu(player, bossType, tierNum))
                    .build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((idx, clicktype, containerInput, slotGui) -> openBossTypeMenu(player))
                .build());

        gui.open();
    }

    private static void openEditMenu(ServerPlayer player, SlayerManager.SlayerType bossType, int tierNum) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("Edit " + bossType.bossName + " T" + tierNum).withStyle(bossType.color, ChatFormatting.BOLD));

        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray())
                    .setName(Component.literal("")).build());
        }

        SlayerManager.TierConfig config = SlayerManager.getTierConfig(tierNum);
        double hp = config.getActualHp(bossType) * DataManager.getBossHpMultiplier(bossType);
        double dmg = config.getActualDamage(bossType) * DataManager.getBossDamageMultiplier(bossType);
        int xp = (int)(config.xpReward * DataManager.getBossXpMultiplier(bossType));

        gui.setSlot(11, new GuiElementBuilder(Items.REDSTONE_BLOCK)
                .setName(Component.literal("❤ Health: " + String.format("%,.0f", hp)).withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Current: " + String.format("%,.0f", hp)).withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Click to set new value").withStyle(ChatFormatting.GREEN))
                .setCallback((idx, clicktype, containerInput, slotGui) -> openHealthSign(player, bossType, tierNum))
                .build());

        gui.setSlot(13, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Component.literal("⚔ Damage: " + String.format("%,.0f", dmg)).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Current: " + String.format("%,.0f", dmg)).withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Click to set new value").withStyle(ChatFormatting.GREEN))
                .setCallback((idx, clicktype, containerInput, slotGui) -> openDamageSign(player, bossType, tierNum))
                .build());

        gui.setSlot(15, new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
                .setName(Component.literal("✨ XP: " + xp).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Current: " + xp).withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Click to set new value").withStyle(ChatFormatting.GREEN))
                .setCallback((idx, clicktype, containerInput, slotGui) -> openXpSign(player, bossType, tierNum))
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("⚙ Multipliers").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("HPx" + String.format("%.1f", DataManager.getBossHpMultiplier(bossType))).withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal("DMGx" + String.format("%.1f", DataManager.getBossDamageMultiplier(bossType))).withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("XPx" + String.format("%.1f", DataManager.getBossXpMultiplier(bossType))).withStyle(ChatFormatting.AQUA))
                .setCallback((idx, clicktype, containerInput, slotGui) -> openMultiplierMenu(player, bossType, tierNum))
                .build());

        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((idx, clicktype, containerInput, slotGui) -> openTierMenu(player, bossType))
                .build());

        gui.open();
    }

    private static void openMultiplierMenu(ServerPlayer player, SlayerManager.SlayerType bossType, int tierNum) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("Multipliers: " + bossType.bossName).withStyle(bossType.color, ChatFormatting.BOLD));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.black())
                    .setName(Component.literal("")).build());
        }

        float hpMult = DataManager.getBossHpMultiplier(bossType);
        float dmgMult = DataManager.getBossDamageMultiplier(bossType);
        float xpMult = DataManager.getBossXpMultiplier(bossType);

        gui.setSlot(11, new GuiElementBuilder(Items.REDSTONE_BLOCK)
                .setName(Component.literal("HP Multiplier: " + String.format("%.1fx", hpMult)).withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Set to 1.0 for default").withStyle(ChatFormatting.GRAY))
                .setCallback((idx, clicktype, containerInput, slotGui) -> openHpMultiplierSign(player, bossType))
                .build());

        gui.setSlot(13, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Component.literal("DMG Multiplier: " + String.format("%.1fx", dmgMult)).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Set to 1.0 for default").withStyle(ChatFormatting.GRAY))
                .setCallback((idx, clicktype, containerInput, slotGui) -> openDmgMultiplierSign(player, bossType))
                .build());

        gui.setSlot(15, new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
                .setName(Component.literal("XP Multiplier: " + String.format("%.1fx", xpMult)).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Set to 1.0 for default").withStyle(ChatFormatting.GRAY))
                .setCallback((idx, clicktype, containerInput, slotGui) -> openXpMultiplierSign(player, bossType))
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.WOOL.green())
                .setName(Component.literal("Reset Multipliers").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .setCallback((idx, clicktype, containerInput, slotGui) -> {
                    DataManager.setBossHpMultiplier(bossType, 1.0f);
                    DataManager.setBossDamageMultiplier(bossType, 1.0f);
                    DataManager.setBossXpMultiplier(bossType, 1.0f);
                    DataManager.save(PoliticalServer.server);
                    player.sendSystemMessage(Component.literal("✓ Reset multipliers for " + bossType.bossName).withStyle(ChatFormatting.GREEN), false);
                    openMultiplierMenu(player, bossType, tierNum);
                })
                .build());

        gui.setSlot(26, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((idx, clicktype, containerInput, slotGui) -> openEditMenu(player, bossType, tierNum))
                .build());

        gui.open();
    }

    private static void openHealthSign(ServerPlayer player, SlayerManager.SlayerType bossType, int tierNum) {
        SlayerManager.TierConfig config = SlayerManager.getTierConfig(tierNum);
        double currentHp = config.getActualHp(bossType) * DataManager.getBossHpMultiplier(bossType);

        SignGui sign = new SignGui(player) {
            @Override
            public void onManualClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9]", "");
                if (!input.isEmpty()) {
                    try {
                        int val = Integer.parseInt(input);
                        if (val > 0) {
                            float mult = (float)val / (float)config.baseHp;
                            DataManager.setBossHpMultiplier(bossType, mult);
                            DataManager.save(PoliticalServer.server);
                            player.sendSystemMessage(Component.literal("✓ Set " + bossType.bossName + " HP to " + val).withStyle(ChatFormatting.GREEN), false);
                        }
                    } catch (NumberFormatException e) {
                        player.sendSystemMessage(Component.literal("✗ Invalid number").withStyle(ChatFormatting.RED), false);
                    }
                }
                openEditMenu(player, bossType, tierNum);
            }
        };
        sign.setLine(0, Component.literal(""));
        sign.setLine(1, Component.literal("^^^^^^^^^^^^^^").withStyle(ChatFormatting.YELLOW));
        sign.setLine(2, Component.literal("Enter health").withStyle(ChatFormatting.AQUA));
        sign.setLine(3, Component.literal("Current: " + (int)currentHp).withStyle(ChatFormatting.GRAY));
        sign.open();
    }

    private static void openDamageSign(ServerPlayer player, SlayerManager.SlayerType bossType, int tierNum) {
        SlayerManager.TierConfig config = SlayerManager.getTierConfig(tierNum);
        double currentDmg = config.getActualDamage(bossType) * DataManager.getBossDamageMultiplier(bossType);

        SignGui sign = new SignGui(player) {
            @Override
            public void onManualClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9]", "");
                if (!input.isEmpty()) {
                    try {
                        int val = Integer.parseInt(input);
                        if (val > 0) {
                            float mult = (float)val / (float)config.baseDamage;
                            DataManager.setBossDamageMultiplier(bossType, mult);
                            DataManager.save(PoliticalServer.server);
                            player.sendSystemMessage(Component.literal("✓ Set " + bossType.bossName + " DMG to " + val).withStyle(ChatFormatting.GREEN), false);
                        }
                    } catch (NumberFormatException e) {
                        player.sendSystemMessage(Component.literal("✗ Invalid number").withStyle(ChatFormatting.RED), false);
                    }
                }
                openEditMenu(player, bossType, tierNum);
            }
        };
        sign.setLine(0, Component.literal(""));
        sign.setLine(1, Component.literal("^^^^^^^^^^^^^^").withStyle(ChatFormatting.YELLOW));
        sign.setLine(2, Component.literal("Enter damage").withStyle(ChatFormatting.AQUA));
        sign.setLine(3, Component.literal("Current: " + (int)currentDmg).withStyle(ChatFormatting.GRAY));
        sign.open();
    }

    private static void openXpSign(ServerPlayer player, SlayerManager.SlayerType bossType, int tierNum) {
        SlayerManager.TierConfig config = SlayerManager.getTierConfig(tierNum);
        int currentXp = (int)(config.xpReward * DataManager.getBossXpMultiplier(bossType));

        SignGui sign = new SignGui(player) {
            @Override
            public void onManualClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9]", "");
                if (!input.isEmpty()) {
                    try {
                        int val = Integer.parseInt(input);
                        if (val > 0) {
                            float mult = (float)val / (float)config.xpReward;
                            DataManager.setBossXpMultiplier(bossType, mult);
                            DataManager.save(PoliticalServer.server);
                            player.sendSystemMessage(Component.literal("✓ Set " + bossType.bossName + " XP to " + val).withStyle(ChatFormatting.GREEN), false);
                        }
                    } catch (NumberFormatException e) {
                        player.sendSystemMessage(Component.literal("✗ Invalid number").withStyle(ChatFormatting.RED), false);
                    }
                }
                openEditMenu(player, bossType, tierNum);
            }
        };
        sign.setLine(0, Component.literal(""));
        sign.setLine(1, Component.literal("^^^^^^^^^^^^^^").withStyle(ChatFormatting.YELLOW));
        sign.setLine(2, Component.literal("Enter XP").withStyle(ChatFormatting.AQUA));
        sign.setLine(3, Component.literal("Current: " + currentXp).withStyle(ChatFormatting.GRAY));
        sign.open();
    }

    private static void openHpMultiplierSign(ServerPlayer player, SlayerManager.SlayerType bossType) {
        float current = DataManager.getBossHpMultiplier(bossType);

        SignGui sign = new SignGui(player) {
            @Override
            public void onManualClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9.]", "");
                if (!input.isEmpty()) {
                    try {
                        float val = Float.parseFloat(input);
                        if (val > 0 && val <= 10) {
                            DataManager.setBossHpMultiplier(bossType, val);
                            DataManager.save(PoliticalServer.server);
                            player.sendSystemMessage(Component.literal("✓ HP multiplier set to " + val + "x").withStyle(ChatFormatting.GREEN), false);
                        } else {
                            player.sendSystemMessage(Component.literal("✗ Must be 0.1 - 10").withStyle(ChatFormatting.RED), false);
                        }
                    } catch (NumberFormatException e) {
                        player.sendSystemMessage(Component.literal("✗ Invalid number").withStyle(ChatFormatting.RED), false);
                    }
                }
                openMultiplierMenu(player, bossType, 1);
            }
        };
        sign.setLine(0, Component.literal(""));
        sign.setLine(1, Component.literal("^^^^^^^^^^^^^^").withStyle(ChatFormatting.YELLOW));
        sign.setLine(2, Component.literal("HP mult (0.1-10)").withStyle(ChatFormatting.AQUA));
        sign.setLine(3, Component.literal("Current: " + current).withStyle(ChatFormatting.GRAY));
        sign.open();
    }

    private static void openDmgMultiplierSign(ServerPlayer player, SlayerManager.SlayerType bossType) {
        float current = DataManager.getBossDamageMultiplier(bossType);

        SignGui sign = new SignGui(player) {
            @Override
            public void onManualClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9.]", "");
                if (!input.isEmpty()) {
                    try {
                        float val = Float.parseFloat(input);
                        if (val > 0 && val <= 10) {
                            DataManager.setBossDamageMultiplier(bossType, val);
                            DataManager.save(PoliticalServer.server);
                            player.sendSystemMessage(Component.literal("✓ DMG multiplier set to " + val + "x").withStyle(ChatFormatting.GREEN), false);
                        } else {
                            player.sendSystemMessage(Component.literal("✗ Must be 0.1 - 10").withStyle(ChatFormatting.RED), false);
                        }
                    } catch (NumberFormatException e) {
                        player.sendSystemMessage(Component.literal("✗ Invalid number").withStyle(ChatFormatting.RED), false);
                    }
                }
                openMultiplierMenu(player, bossType, 1);
            }
        };
        sign.setLine(0, Component.literal(""));
        sign.setLine(1, Component.literal("^^^^^^^^^^^^^^").withStyle(ChatFormatting.YELLOW));
        sign.setLine(2, Component.literal("DMG mult (0.1-10)").withStyle(ChatFormatting.AQUA));
        sign.setLine(3, Component.literal("Current: " + current).withStyle(ChatFormatting.GRAY));
        sign.open();
    }

    private static void openXpMultiplierSign(ServerPlayer player, SlayerManager.SlayerType bossType) {
        float current = DataManager.getBossXpMultiplier(bossType);

        SignGui sign = new SignGui(player) {
            @Override
            public void onManualClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9.]", "");
                if (!input.isEmpty()) {
                    try {
                        float val = Float.parseFloat(input);
                        if (val > 0 && val <= 10) {
                            DataManager.setBossXpMultiplier(bossType, val);
                            DataManager.save(PoliticalServer.server);
                            player.sendSystemMessage(Component.literal("✓ XP multiplier set to " + val + "x").withStyle(ChatFormatting.GREEN), false);
                        } else {
                            player.sendSystemMessage(Component.literal("✗ Must be 0.1 - 10").withStyle(ChatFormatting.RED), false);
                        }
                    } catch (NumberFormatException e) {
                        player.sendSystemMessage(Component.literal("✗ Invalid number").withStyle(ChatFormatting.RED), false);
                    }
                }
                openMultiplierMenu(player, bossType, 1);
            }
        };
        sign.setLine(0, Component.literal(""));
        sign.setLine(1, Component.literal("^^^^^^^^^^^^^^").withStyle(ChatFormatting.YELLOW));
        sign.setLine(2, Component.literal("XP mult (0.1-10)").withStyle(ChatFormatting.AQUA));
        sign.setLine(3, Component.literal("Current: " + current).withStyle(ChatFormatting.GRAY));
        sign.open();
    }

    // ==================== SWORD DAMAGE ====================

    private static void openSwordDamageMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("⚔ Sword Damage %").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.black())
                    .setName(Component.literal("")).build());
        }

        gui.setSlot(11, new GuiElementBuilder(Items.STONE_SWORD)
                .setName(Component.literal("T1 Sword").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Current: " + DataManager.getT1SwordDamagePercent() + "%").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("of boss HP per hit").withStyle(ChatFormatting.GRAY))
                .setCallback((idx, clicktype, containerInput, slotGui) -> openT1SwordSign(player))
                .build());

        gui.setSlot(15, new GuiElementBuilder(Items.DIAMOND_SWORD)
                .setName(Component.literal("T2 Sword").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Current: " + DataManager.getT2SwordDamagePercent() + "%").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("of boss HP per hit").withStyle(ChatFormatting.GRAY))
                .setCallback((idx, clicktype, containerInput, slotGui) -> openT2SwordSign(player))
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.WOOL.green())
                .setName(Component.literal("Reset Defaults").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .setCallback((idx, clicktype, containerInput, slotGui) -> {
                    DataManager.setT1SwordDamagePercent(2.5);
                    DataManager.setT2SwordDamagePercent(5.0);
                    DataManager.save(PoliticalServer.server);
                    player.sendSystemMessage(Component.literal("✓ Reset to 2.5% / 5%").withStyle(ChatFormatting.GREEN), false);
                    openSwordDamageMenu(player);
                })
                .build());

        gui.setSlot(26, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((idx, clicktype, containerInput, slotGui) -> openMainMenu(player))
                .build());

        gui.open();
    }

    private static void openT1SwordSign(ServerPlayer player) {
        double current = DataManager.getT1SwordDamagePercent();

        SignGui sign = new SignGui(player) {
            @Override
            public void onManualClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9.]", "");
                if (!input.isEmpty()) {
                    try {
                        double val = Double.parseDouble(input);
                        if (val >= 0 && val <= 100) {
                            DataManager.setT1SwordDamagePercent(val);
                            DataManager.save(PoliticalServer.server);
                            player.sendSystemMessage(Component.literal("✓ T1 sword: " + val + "% of boss HP").withStyle(ChatFormatting.GREEN), false);
                        } else {
                            player.sendSystemMessage(Component.literal("✗ Must be 0-100").withStyle(ChatFormatting.RED), false);
                        }
                    } catch (NumberFormatException e) {
                        player.sendSystemMessage(Component.literal("✗ Invalid number").withStyle(ChatFormatting.RED), false);
                    }
                }
                openSwordDamageMenu(player);
            }
        };
        sign.setLine(0, Component.literal(""));
        sign.setLine(1, Component.literal("^^^^^^^^^^^^^^").withStyle(ChatFormatting.YELLOW));
        sign.setLine(2, Component.literal("T1 % of boss HP").withStyle(ChatFormatting.AQUA));
        sign.setLine(3, Component.literal("Current: " + current).withStyle(ChatFormatting.GRAY));
        sign.open();
    }

    private static void openT2SwordSign(ServerPlayer player) {
        double current = DataManager.getT2SwordDamagePercent();

        SignGui sign = new SignGui(player) {
            @Override
            public void onManualClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9.]", "");
                if (!input.isEmpty()) {
                    try {
                        double val = Double.parseDouble(input);
                        if (val >= 0 && val <= 100) {
                            DataManager.setT2SwordDamagePercent(val);
                            DataManager.save(PoliticalServer.server);
                            player.sendSystemMessage(Component.literal("✓ T2 sword: " + val + "% of boss HP").withStyle(ChatFormatting.GREEN), false);
                        } else {
                            player.sendSystemMessage(Component.literal("✗ Must be 0-100").withStyle(ChatFormatting.RED), false);
                        }
                    } catch (NumberFormatException e) {
                        player.sendSystemMessage(Component.literal("✗ Invalid number").withStyle(ChatFormatting.RED), false);
                    }
                }
                openSwordDamageMenu(player);
            }
        };
        sign.setLine(0, Component.literal(""));
        sign.setLine(1, Component.literal("^^^^^^^^^^^^^^").withStyle(ChatFormatting.YELLOW));
        sign.setLine(2, Component.literal("T2 % of boss HP").withStyle(ChatFormatting.AQUA));
        sign.setLine(3, Component.literal("Current: " + current).withStyle(ChatFormatting.GRAY));
        sign.open();
    }

    // ==================== ABILITIES ====================

    private static void openAbilitiesMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x5, player, false);
        gui.setTitle(Component.literal("⚡ Boss Abilities").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));

        for (int i = 0; i < 45; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.black())
                    .setName(Component.literal("")).build());
        }

        SlayerManager.SlayerType[] types = SlayerManager.SlayerType.values();
        int[] slots = {11, 12, 13, 14, 15, 20, 21, 22};

        for (int i = 0; i < types.length && i < slots.length; i++) {
            final SlayerManager.SlayerType bossType = types[i];
            boolean enabled = DataManager.isBossAbilitiesEnabled(bossType);
            int cooldown = DataManager.getBossAbilityCooldown(bossType);

            gui.setSlot(slots[i], new GuiElementBuilder(enabled ? Items.CONCRETE.lime() : Items.CONCRETE.red())
                    .setName(Component.literal(bossType.displayName).withStyle(bossType.color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Abilities: " + (enabled ? "§aENABLED" : "§cDISABLED")).withStyle(ChatFormatting.WHITE))
                    .addLoreLine(Component.literal("Cooldown: " + cooldown + "s").withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal("Click to toggle").withStyle(ChatFormatting.GREEN))
                    .setCallback((idx, clicktype, containerInput, slotGui) -> openAbilityDetailMenu(player, bossType))
                    .build());
        }

        gui.setSlot(40, new GuiElementBuilder(Items.WOOL.green())
                .setName(Component.literal("Enable All").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .setCallback((idx, clicktype, containerInput, slotGui) -> {
                    for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
                        DataManager.setBossAbilitiesEnabled(type, true);
                    }
                    DataManager.save(PoliticalServer.server);
                    player.sendSystemMessage(Component.literal("✓ All abilities enabled").withStyle(ChatFormatting.GREEN), false);
                    openAbilitiesMenu(player);
                })
                .build());

        gui.setSlot(41, new GuiElementBuilder(Items.WOOL.red())
                .setName(Component.literal("Disable All").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .setCallback((idx, clicktype, containerInput, slotGui) -> {
                    for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
                        DataManager.setBossAbilitiesEnabled(type, false);
                    }
                    DataManager.save(PoliticalServer.server);
                    player.sendSystemMessage(Component.literal("✓ All abilities disabled").withStyle(ChatFormatting.GREEN), false);
                    openAbilitiesMenu(player);
                })
                .build());

        gui.setSlot(44, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((idx, clicktype, containerInput, slotGui) -> openMainMenu(player))
                .build());

        gui.open();
    }

    private static void openAbilityDetailMenu(ServerPlayer player, SlayerManager.SlayerType bossType) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal(bossType.displayName + " Abilities").withStyle(bossType.color, ChatFormatting.BOLD));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.black())
                    .setName(Component.literal("")).build());
        }

        boolean enabled = DataManager.isBossAbilitiesEnabled(bossType);
        int cooldown = DataManager.getBossAbilityCooldown(bossType);

        gui.setSlot(11, new GuiElementBuilder(enabled ? Items.WOOL.lime() : Items.WOOL.red())
                .setName(Component.literal("Abilities: " + (enabled ? "ENABLED" : "DISABLED")).withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Click to toggle").withStyle(ChatFormatting.GREEN))
                .setCallback((idx, clicktype, containerInput, slotGui) -> {
                    DataManager.setBossAbilitiesEnabled(bossType, !enabled);
                    DataManager.save(PoliticalServer.server);
                    player.sendSystemMessage(Component.literal("✓ " + bossType.bossName + " abilities " + (!enabled ? "enabled" : "disabled")).withStyle(ChatFormatting.GREEN), false);
                    openAbilityDetailMenu(player, bossType);
                })
                .build());

        gui.setSlot(15, new GuiElementBuilder(Items.CLOCK)
                .setName(Component.literal("Cooldown: " + cooldown + "s").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Set ability cooldown").withStyle(ChatFormatting.GREEN))
                .setCallback((idx, clicktype, containerInput, slotGui) -> openCooldownSign(player, bossType))
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.WOOL.green())
                .setName(Component.literal("Reset to Defaults").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .setCallback((idx, clicktype, containerInput, slotGui) -> {
                    DataManager.setBossAbilitiesEnabled(bossType, true);
                    DataManager.setBossAbilityCooldown(bossType, 30);
                    DataManager.save(PoliticalServer.server);
                    player.sendSystemMessage(Component.literal("✓ Reset abilities for " + bossType.bossName).withStyle(ChatFormatting.GREEN), false);
                    openAbilityDetailMenu(player, bossType);
                })
                .build());

        gui.setSlot(26, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((idx, clicktype, containerInput, slotGui) -> openAbilitiesMenu(player))
                .build());

        gui.open();
    }

    private static void openCooldownSign(ServerPlayer player, SlayerManager.SlayerType bossType) {
        int current = DataManager.getBossAbilityCooldown(bossType);

        SignGui sign = new SignGui(player) {
            @Override
            public void onManualClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9]", "");
                if (!input.isEmpty()) {
                    try {
                        int val = Integer.parseInt(input);
                        if (val >= 5 && val <= 300) {
                            DataManager.setBossAbilityCooldown(bossType, val);
                            DataManager.save(PoliticalServer.server);
                            player.sendSystemMessage(Component.literal("✓ Cooldown set to " + val + "s").withStyle(ChatFormatting.GREEN), false);
                        } else {
                            player.sendSystemMessage(Component.literal("✗ Must be 5-300 seconds").withStyle(ChatFormatting.RED), false);
                        }
                    } catch (NumberFormatException e) {
                        player.sendSystemMessage(Component.literal("✗ Invalid number").withStyle(ChatFormatting.RED), false);
                    }
                }
                openAbilityDetailMenu(player, bossType);
            }
        };
        sign.setLine(0, Component.literal(""));
        sign.setLine(1, Component.literal("^^^^^^^^^^^^^^").withStyle(ChatFormatting.YELLOW));
        sign.setLine(2, Component.literal("Cooldown (5-300)").withStyle(ChatFormatting.AQUA));
        sign.setLine(3, Component.literal("Current: " + current).withStyle(ChatFormatting.GRAY));
        sign.open();
    }

    // ==================== GEAR & DEFENSE ====================

    private static void openGearDefenseMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("🛡 Boss Gear & Defense").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.black())
                    .setName(Component.literal("")).build());
        }

        int universalDef = DataManager.getBossUniversalDefense();

        gui.setSlot(11, new GuiElementBuilder(Items.DIAMOND_CHESTPLATE)
                .setName(Component.literal("Boss-Specific Defense").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Defense per boss type").withStyle(ChatFormatting.GRAY))
                .setCallback((idx, clicktype, containerInput, slotGui) -> openSpecificDefenseMenu(player))
                .build());

        gui.setSlot(15, new GuiElementBuilder(Items.SHIELD)
                .setName(Component.literal("Universal Defense: " + universalDef + "%").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Applied to all bosses").withStyle(ChatFormatting.GRAY))
                .setCallback((idx, clicktype, containerInput, slotGui) -> openUniversalDefenseSign(player))
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.IRON_CHESTPLATE)
                .setName(Component.literal("Boss Weapons Config").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Coming soon...").withStyle(ChatFormatting.GRAY))
                .build());

        gui.setSlot(26, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((idx, clicktype, containerInput, slotGui) -> openMainMenu(player))
                .build());

        gui.open();
    }

    private static void openSpecificDefenseMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("⚔ Boss-Specific Defense").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.black())
                    .setName(Component.literal("")).build());
        }

        SlayerManager.SlayerType[] types = SlayerManager.SlayerType.values();
        int[] slots = {11, 12, 13, 14, 15, 20, 21, 22};

        for (int i = 0; i < types.length && i < slots.length; i++) {
            final SlayerManager.SlayerType bossType = types[i];
            int def = DataManager.getBossSpecificDefense(bossType);

            gui.setSlot(slots[i], new GuiElementBuilder(Items.IRON_CHESTPLATE)
                    .setName(Component.literal(bossType.displayName).withStyle(bossType.color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Defense: " + def + "%").withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal("Click to set").withStyle(ChatFormatting.GREEN))
                    .setCallback((idx, clicktype, containerInput, slotGui) -> openSpecificDefenseSign(player, bossType))
                    .build());
        }

        gui.setSlot(27, new GuiElementBuilder(Items.WOOL.green())
                .setName(Component.literal("Reset All Defense").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .setCallback((idx, clicktype, containerInput, slotGui) -> {
                    for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
                        DataManager.setBossSpecificDefense(type, 0);
                    }
                    DataManager.save(PoliticalServer.server);
                    player.sendSystemMessage(Component.literal("✓ Reset all boss defense").withStyle(ChatFormatting.GREEN), false);
                    openSpecificDefenseMenu(player);
                })
                .build());

        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((idx, clicktype, containerInput, slotGui) -> openGearDefenseMenu(player))
                .build());

        gui.open();
    }

    private static void openSpecificDefenseSign(ServerPlayer player, SlayerManager.SlayerType bossType) {
        int current = DataManager.getBossSpecificDefense(bossType);

        SignGui sign = new SignGui(player) {
            @Override
            public void onManualClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9]", "");
                if (!input.isEmpty()) {
                    try {
                        int val = Integer.parseInt(input);
                        if (val >= 0 && val <= 100) {
                            DataManager.setBossSpecificDefense(bossType, val);
                            DataManager.save(PoliticalServer.server);
                            player.sendSystemMessage(Component.literal("✓ " + bossType.bossName + " defense: " + val + "%").withStyle(ChatFormatting.GREEN), false);
                        } else {
                            player.sendSystemMessage(Component.literal("✗ Must be 0-100").withStyle(ChatFormatting.RED), false);
                        }
                    } catch (NumberFormatException e) {
                        player.sendSystemMessage(Component.literal("✗ Invalid number").withStyle(ChatFormatting.RED), false);
                    }
                }
                openSpecificDefenseMenu(player);
            }
        };
        sign.setLine(0, Component.literal(""));
        sign.setLine(1, Component.literal("^^^^^^^^^^^^^^").withStyle(ChatFormatting.YELLOW));
        sign.setLine(2, Component.literal(bossType.name() + " def%").withStyle(ChatFormatting.AQUA));
        sign.setLine(3, Component.literal("Current: " + current).withStyle(ChatFormatting.GRAY));
        sign.open();
    }

    private static void openUniversalDefenseSign(ServerPlayer player) {
        int current = DataManager.getBossUniversalDefense();

        SignGui sign = new SignGui(player) {
            @Override
            public void onManualClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9]", "");
                if (!input.isEmpty()) {
                    try {
                        int val = Integer.parseInt(input);
                        if (val >= 0 && val <= 100) {
                            DataManager.setBossUniversalDefense(val);
                            DataManager.save(PoliticalServer.server);
                            player.sendSystemMessage(Component.literal("✓ Universal defense: " + val + "%").withStyle(ChatFormatting.GREEN), false);
                        } else {
                            player.sendSystemMessage(Component.literal("✗ Must be 0-100").withStyle(ChatFormatting.RED), false);
                        }
                    } catch (NumberFormatException e) {
                        player.sendSystemMessage(Component.literal("✗ Invalid number").withStyle(ChatFormatting.RED), false);
                    }
                }
                openGearDefenseMenu(player);
            }
        };
        sign.setLine(0, Component.literal(""));
        sign.setLine(1, Component.literal("^^^^^^^^^^^^^^").withStyle(ChatFormatting.YELLOW));
        sign.setLine(2, Component.literal("Universal def%").withStyle(ChatFormatting.AQUA));
        sign.setLine(3, Component.literal("Current: " + current).withStyle(ChatFormatting.GRAY));
        sign.open();
    }
}
