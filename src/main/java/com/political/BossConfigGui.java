package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SignGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class BossConfigGui {

    public static void openMainMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("⚙ Boss Configuration").formatted(Formatting.RED, Formatting.BOLD));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        gui.setSlot(11, new GuiElementBuilder(Items.BEACON)
                .setName(Text.literal("Boss Stats").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal("HP, Damage, XP per boss").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("type & tier").formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> openBossTypeMenu(player))
                .build());

        gui.setSlot(13, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Text.literal("Sword Damage %").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal("T1: " + DataManager.getT1SwordDamagePercent() + "%").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("T2: " + DataManager.getT2SwordDamagePercent() + "%").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openSwordDamageMenu(player))
                .build());

        gui.setSlot(15, new GuiElementBuilder(Items.BOOK)
                .setName(Text.literal("Abilities & Cooldowns").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal("Toggle abilities on/off").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Set ability cooldowns").formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> openAbilitiesMenu(player))
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.DIAMOND_CHESTPLATE)
                .setName(Text.literal("Boss Gear & Defense").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal("Configure boss armor").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("weapons & defense").formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> openGearDefenseMenu(player))
                .build());

        gui.setSlot(26, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> {})
                .build());

        gui.open();
    }

    // ==================== BOSS STATS ====================

    private static void openBossTypeMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("Select Boss Type").formatted(Formatting.GOLD, Formatting.BOLD));

        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        SlayerManager.SlayerType[] types = SlayerManager.SlayerType.values();
        int[] slots = {11, 12, 13, 14, 15, 20, 21, 22};

        for (int i = 0; i < types.length && i < slots.length; i++) {
            final SlayerManager.SlayerType bossType = types[i];
            gui.setSlot(slots[i], new GuiElementBuilder(Items.PAPER)
                    .setName(Text.literal(bossType.displayName).formatted(bossType.color, Formatting.BOLD))
                    .addLoreLine(Text.literal("Click to configure").formatted(Formatting.GREEN))
                    .setCallback((idx, clickType, action) -> openTierMenu(player, bossType))
                    .build());
        }

        gui.setSlot(27, new GuiElementBuilder(Items.GREEN_WOOL)
                .setName(Text.literal("Reset All Stats").formatted(Formatting.GREEN, Formatting.BOLD))
                .setCallback((idx, clickType, action) -> {
                    DataManager.resetAllBossConfigs();
                    DataManager.save(PoliticalServer.server);
                    player.sendMessage(Text.literal("✓ Reset all boss configs to defaults").formatted(Formatting.GREEN), false);
                    openBossTypeMenu(player);
                })
                .build());

        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((idx, clickType, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    private static void openTierMenu(ServerPlayerEntity player, SlayerManager.SlayerType bossType) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal(bossType.bossName + " Config").formatted(bossType.color, Formatting.BOLD));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        int[] tierSlots = {10, 11, 12, 13, 14, 19, 20, 21, 22, 23};
        Formatting[] tierColors = {Formatting.WHITE, Formatting.GREEN, Formatting.BLUE, Formatting.GOLD, Formatting.LIGHT_PURPLE};

        for (int t = 1; t <= 5; t++) {
            final int tierNum = t;
            SlayerManager.TierConfig config = SlayerManager.getTierConfig(tierNum);

            double hp = config.getActualHp(bossType) * DataManager.getBossHpMultiplier(bossType);
            double dmg = config.getActualDamage(bossType) * DataManager.getBossDamageMultiplier(bossType);
            int xp = (int)(config.xpReward * DataManager.getBossXpMultiplier(bossType));

            gui.setSlot(tierSlots[t - 1], new GuiElementBuilder(Items.PAPER)
                    .setName(Text.literal("Tier " + t).formatted(tierColors[t - 1], Formatting.BOLD))
                    .addLoreLine(Text.literal("❤ HP: " + String.format("%,.0f", hp)).formatted(Formatting.RED))
                    .addLoreLine(Text.literal("⚔ DMG: " + String.format("%,.0f", dmg)).formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("✨ XP: " + xp).formatted(Formatting.AQUA))
                    .addLoreLine(Text.literal("Click to edit").formatted(Formatting.GREEN))
                    .setCallback((idx, clickType, action) -> openEditMenu(player, bossType, tierNum))
                    .build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((idx, clickType, action) -> openBossTypeMenu(player))
                .build());

        gui.open();
    }

    private static void openEditMenu(ServerPlayerEntity player, SlayerManager.SlayerType bossType, int tierNum) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("Edit " + bossType.bossName + " T" + tierNum).formatted(bossType.color, Formatting.BOLD));

        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        SlayerManager.TierConfig config = SlayerManager.getTierConfig(tierNum);
        double hp = config.getActualHp(bossType) * DataManager.getBossHpMultiplier(bossType);
        double dmg = config.getActualDamage(bossType) * DataManager.getBossDamageMultiplier(bossType);
        int xp = (int)(config.xpReward * DataManager.getBossXpMultiplier(bossType));

        gui.setSlot(11, new GuiElementBuilder(Items.REDSTONE_BLOCK)
                .setName(Text.literal("❤ Health: " + String.format("%,.0f", hp)).formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal("Current: " + String.format("%,.0f", hp)).formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click to set new value").formatted(Formatting.GREEN))
                .setCallback((idx, clickType, action) -> openHealthSign(player, bossType, tierNum))
                .build());

        gui.setSlot(13, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Text.literal("⚔ Damage: " + String.format("%,.0f", dmg)).formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal("Current: " + String.format("%,.0f", dmg)).formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click to set new value").formatted(Formatting.GREEN))
                .setCallback((idx, clickType, action) -> openDamageSign(player, bossType, tierNum))
                .build());

        gui.setSlot(15, new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
                .setName(Text.literal("✨ XP: " + xp).formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal("Current: " + xp).formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click to set new value").formatted(Formatting.GREEN))
                .setCallback((idx, clickType, action) -> openXpSign(player, bossType, tierNum))
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("⚙ Multipliers").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal("HPx" + String.format("%.1f", DataManager.getBossHpMultiplier(bossType))).formatted(Formatting.RED))
                .addLoreLine(Text.literal("DMGx" + String.format("%.1f", DataManager.getBossDamageMultiplier(bossType))).formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("XPx" + String.format("%.1f", DataManager.getBossXpMultiplier(bossType))).formatted(Formatting.AQUA))
                .setCallback((idx, clickType, action) -> openMultiplierMenu(player, bossType, tierNum))
                .build());

        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((idx, clickType, action) -> openTierMenu(player, bossType))
                .build());

        gui.open();
    }

    private static void openMultiplierMenu(ServerPlayerEntity player, SlayerManager.SlayerType bossType, int tierNum) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("Multipliers: " + bossType.bossName).formatted(bossType.color, Formatting.BOLD));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        float hpMult = DataManager.getBossHpMultiplier(bossType);
        float dmgMult = DataManager.getBossDamageMultiplier(bossType);
        float xpMult = DataManager.getBossXpMultiplier(bossType);

        gui.setSlot(11, new GuiElementBuilder(Items.REDSTONE_BLOCK)
                .setName(Text.literal("HP Multiplier: " + String.format("%.1fx", hpMult)).formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal("Set to 1.0 for default").formatted(Formatting.GRAY))
                .setCallback((idx, clickType, action) -> openHpMultiplierSign(player, bossType))
                .build());

        gui.setSlot(13, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Text.literal("DMG Multiplier: " + String.format("%.1fx", dmgMult)).formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal("Set to 1.0 for default").formatted(Formatting.GRAY))
                .setCallback((idx, clickType, action) -> openDmgMultiplierSign(player, bossType))
                .build());

        gui.setSlot(15, new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
                .setName(Text.literal("XP Multiplier: " + String.format("%.1fx", xpMult)).formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal("Set to 1.0 for default").formatted(Formatting.GRAY))
                .setCallback((idx, clickType, action) -> openXpMultiplierSign(player, bossType))
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.GREEN_WOOL)
                .setName(Text.literal("Reset Multipliers").formatted(Formatting.GREEN, Formatting.BOLD))
                .setCallback((idx, clickType, action) -> {
                    DataManager.setBossHpMultiplier(bossType, 1.0f);
                    DataManager.setBossDamageMultiplier(bossType, 1.0f);
                    DataManager.setBossXpMultiplier(bossType, 1.0f);
                    DataManager.save(PoliticalServer.server);
                    player.sendMessage(Text.literal("✓ Reset multipliers for " + bossType.bossName).formatted(Formatting.GREEN), false);
                    openMultiplierMenu(player, bossType, tierNum);
                })
                .build());

        gui.setSlot(26, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((idx, clickType, action) -> openEditMenu(player, bossType, tierNum))
                .build());

        gui.open();
    }

    private static void openHealthSign(ServerPlayerEntity player, SlayerManager.SlayerType bossType, int tierNum) {
        SlayerManager.TierConfig config = SlayerManager.getTierConfig(tierNum);
        double currentHp = config.getActualHp(bossType) * DataManager.getBossHpMultiplier(bossType);

        SignGui sign = new SignGui(player) {
            @Override
            public void onClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9]", "");
                if (!input.isEmpty()) {
                    try {
                        int val = Integer.parseInt(input);
                        if (val > 0) {
                            float mult = (float)val / (float)config.baseHp;
                            DataManager.setBossHpMultiplier(bossType, mult);
                            DataManager.save(PoliticalServer.server);
                            player.sendMessage(Text.literal("✓ Set " + bossType.bossName + " HP to " + val).formatted(Formatting.GREEN), false);
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage(Text.literal("✗ Invalid number").formatted(Formatting.RED), false);
                    }
                }
                openEditMenu(player, bossType, tierNum);
            }
        };
        sign.setLine(0, Text.literal(String.valueOf((int)currentHp)));
        sign.setLine(1, Text.literal("Enter health:"));
        sign.open();
    }

    private static void openDamageSign(ServerPlayerEntity player, SlayerManager.SlayerType bossType, int tierNum) {
        SlayerManager.TierConfig config = SlayerManager.getTierConfig(tierNum);
        double currentDmg = config.getActualDamage(bossType) * DataManager.getBossDamageMultiplier(bossType);

        SignGui sign = new SignGui(player) {
            @Override
            public void onClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9]", "");
                if (!input.isEmpty()) {
                    try {
                        int val = Integer.parseInt(input);
                        if (val > 0) {
                            float mult = (float)val / (float)config.baseDamage;
                            DataManager.setBossDamageMultiplier(bossType, mult);
                            DataManager.save(PoliticalServer.server);
                            player.sendMessage(Text.literal("✓ Set " + bossType.bossName + " DMG to " + val).formatted(Formatting.GREEN), false);
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage(Text.literal("✗ Invalid number").formatted(Formatting.RED), false);
                    }
                }
                openEditMenu(player, bossType, tierNum);
            }
        };
        sign.setLine(0, Text.literal(String.valueOf((int)currentDmg)));
        sign.setLine(1, Text.literal("Enter damage:"));
        sign.open();
    }

    private static void openXpSign(ServerPlayerEntity player, SlayerManager.SlayerType bossType, int tierNum) {
        SlayerManager.TierConfig config = SlayerManager.getTierConfig(tierNum);
        int currentXp = (int)(config.xpReward * DataManager.getBossXpMultiplier(bossType));

        SignGui sign = new SignGui(player) {
            @Override
            public void onClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9]", "");
                if (!input.isEmpty()) {
                    try {
                        int val = Integer.parseInt(input);
                        if (val > 0) {
                            float mult = (float)val / (float)config.xpReward;
                            DataManager.setBossXpMultiplier(bossType, mult);
                            DataManager.save(PoliticalServer.server);
                            player.sendMessage(Text.literal("✓ Set " + bossType.bossName + " XP to " + val).formatted(Formatting.GREEN), false);
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage(Text.literal("✗ Invalid number").formatted(Formatting.RED), false);
                    }
                }
                openEditMenu(player, bossType, tierNum);
            }
        };
        sign.setLine(0, Text.literal(String.valueOf(currentXp)));
        sign.setLine(1, Text.literal("Enter XP:"));
        sign.open();
    }

    private static void openHpMultiplierSign(ServerPlayerEntity player, SlayerManager.SlayerType bossType) {
        float current = DataManager.getBossHpMultiplier(bossType);

        SignGui sign = new SignGui(player) {
            @Override
            public void onClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9.]", "");
                if (!input.isEmpty()) {
                    try {
                        float val = Float.parseFloat(input);
                        if (val > 0 && val <= 10) {
                            DataManager.setBossHpMultiplier(bossType, val);
                            DataManager.save(PoliticalServer.server);
                            player.sendMessage(Text.literal("✓ HP multiplier set to " + val + "x").formatted(Formatting.GREEN), false);
                        } else {
                            player.sendMessage(Text.literal("✗ Must be 0.1 - 10").formatted(Formatting.RED), false);
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage(Text.literal("✗ Invalid number").formatted(Formatting.RED), false);
                    }
                }
                openMultiplierMenu(player, bossType, 1);
            }
        };
        sign.setLine(0, Text.literal(String.valueOf(current)));
        sign.setLine(1, Text.literal("HP mult (0.1-10):"));
        sign.open();
    }

    private static void openDmgMultiplierSign(ServerPlayerEntity player, SlayerManager.SlayerType bossType) {
        float current = DataManager.getBossDamageMultiplier(bossType);

        SignGui sign = new SignGui(player) {
            @Override
            public void onClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9.]", "");
                if (!input.isEmpty()) {
                    try {
                        float val = Float.parseFloat(input);
                        if (val > 0 && val <= 10) {
                            DataManager.setBossDamageMultiplier(bossType, val);
                            DataManager.save(PoliticalServer.server);
                            player.sendMessage(Text.literal("✓ DMG multiplier set to " + val + "x").formatted(Formatting.GREEN), false);
                        } else {
                            player.sendMessage(Text.literal("✗ Must be 0.1 - 10").formatted(Formatting.RED), false);
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage(Text.literal("✗ Invalid number").formatted(Formatting.RED), false);
                    }
                }
                openMultiplierMenu(player, bossType, 1);
            }
        };
        sign.setLine(0, Text.literal(String.valueOf(current)));
        sign.setLine(1, Text.literal("DMG mult (0.1-10):"));
        sign.open();
    }

    private static void openXpMultiplierSign(ServerPlayerEntity player, SlayerManager.SlayerType bossType) {
        float current = DataManager.getBossXpMultiplier(bossType);

        SignGui sign = new SignGui(player) {
            @Override
            public void onClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9.]", "");
                if (!input.isEmpty()) {
                    try {
                        float val = Float.parseFloat(input);
                        if (val > 0 && val <= 10) {
                            DataManager.setBossXpMultiplier(bossType, val);
                            DataManager.save(PoliticalServer.server);
                            player.sendMessage(Text.literal("✓ XP multiplier set to " + val + "x").formatted(Formatting.GREEN), false);
                        } else {
                            player.sendMessage(Text.literal("✗ Must be 0.1 - 10").formatted(Formatting.RED), false);
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage(Text.literal("✗ Invalid number").formatted(Formatting.RED), false);
                    }
                }
                openMultiplierMenu(player, bossType, 1);
            }
        };
        sign.setLine(0, Text.literal(String.valueOf(current)));
        sign.setLine(1, Text.literal("XP mult (0.1-10):"));
        sign.open();
    }

    // ==================== SWORD DAMAGE ====================

    private static void openSwordDamageMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("⚔ Sword Damage %").formatted(Formatting.AQUA, Formatting.BOLD));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        gui.setSlot(11, new GuiElementBuilder(Items.STONE_SWORD)
                .setName(Text.literal("T1 Sword").formatted(Formatting.WHITE, Formatting.BOLD))
                .addLoreLine(Text.literal("Current: " + DataManager.getT1SwordDamagePercent() + "%").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("of boss HP per hit").formatted(Formatting.GRAY))
                .setCallback((idx, clickType, action) -> openT1SwordSign(player))
                .build());

        gui.setSlot(15, new GuiElementBuilder(Items.DIAMOND_SWORD)
                .setName(Text.literal("T2 Sword").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal("Current: " + DataManager.getT2SwordDamagePercent() + "%").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("of boss HP per hit").formatted(Formatting.GRAY))
                .setCallback((idx, clickType, action) -> openT2SwordSign(player))
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.GREEN_WOOL)
                .setName(Text.literal("Reset Defaults").formatted(Formatting.GREEN, Formatting.BOLD))
                .setCallback((idx, clickType, action) -> {
                    DataManager.setT1SwordDamagePercent(2.5);
                    DataManager.setT2SwordDamagePercent(5.0);
                    DataManager.save(PoliticalServer.server);
                    player.sendMessage(Text.literal("✓ Reset to 2.5% / 5%").formatted(Formatting.GREEN), false);
                    openSwordDamageMenu(player);
                })
                .build());

        gui.setSlot(26, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((idx, clickType, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    private static void openT1SwordSign(ServerPlayerEntity player) {
        double current = DataManager.getT1SwordDamagePercent();

        SignGui sign = new SignGui(player) {
            @Override
            public void onClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9.]", "");
                if (!input.isEmpty()) {
                    try {
                        double val = Double.parseDouble(input);
                        if (val >= 0 && val <= 100) {
                            DataManager.setT1SwordDamagePercent(val);
                            DataManager.save(PoliticalServer.server);
                            player.sendMessage(Text.literal("✓ T1 sword: " + val + "% of boss HP").formatted(Formatting.GREEN), false);
                        } else {
                            player.sendMessage(Text.literal("✗ Must be 0-100").formatted(Formatting.RED), false);
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage(Text.literal("✗ Invalid number").formatted(Formatting.RED), false);
                    }
                }
                openSwordDamageMenu(player);
            }
        };
        sign.setLine(0, Text.literal(String.valueOf(current)));
        sign.setLine(1, Text.literal("T1 % of boss HP:"));
        sign.open();
    }

    private static void openT2SwordSign(ServerPlayerEntity player) {
        double current = DataManager.getT2SwordDamagePercent();

        SignGui sign = new SignGui(player) {
            @Override
            public void onClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9.]", "");
                if (!input.isEmpty()) {
                    try {
                        double val = Double.parseDouble(input);
                        if (val >= 0 && val <= 100) {
                            DataManager.setT2SwordDamagePercent(val);
                            DataManager.save(PoliticalServer.server);
                            player.sendMessage(Text.literal("✓ T2 sword: " + val + "% of boss HP").formatted(Formatting.GREEN), false);
                        } else {
                            player.sendMessage(Text.literal("✗ Must be 0-100").formatted(Formatting.RED), false);
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage(Text.literal("✗ Invalid number").formatted(Formatting.RED), false);
                    }
                }
                openSwordDamageMenu(player);
            }
        };
        sign.setLine(0, Text.literal(String.valueOf(current)));
        sign.setLine(1, Text.literal("T2 % of boss HP:"));
        sign.open();
    }

    // ==================== ABILITIES ====================

    private static void openAbilitiesMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X5, player, false);
        gui.setTitle(Text.literal("⚡ Boss Abilities").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));

        for (int i = 0; i < 45; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        SlayerManager.SlayerType[] types = SlayerManager.SlayerType.values();
        int[] slots = {11, 12, 13, 14, 15, 20, 21, 22};

        for (int i = 0; i < types.length && i < slots.length; i++) {
            final SlayerManager.SlayerType bossType = types[i];
            boolean enabled = DataManager.isBossAbilitiesEnabled(bossType);
            int cooldown = DataManager.getBossAbilityCooldown(bossType);

            gui.setSlot(slots[i], new GuiElementBuilder(enabled ? Items.LIME_CONCRETE : Items.RED_CONCRETE)
                    .setName(Text.literal(bossType.displayName).formatted(bossType.color, Formatting.BOLD))
                    .addLoreLine(Text.literal("Abilities: " + (enabled ? "§aENABLED" : "§cDISABLED")).formatted(Formatting.WHITE))
                    .addLoreLine(Text.literal("Cooldown: " + cooldown + "s").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("Click to toggle").formatted(Formatting.GREEN))
                    .setCallback((idx, clickType, action) -> openAbilityDetailMenu(player, bossType))
                    .build());
        }

        gui.setSlot(40, new GuiElementBuilder(Items.GREEN_WOOL)
                .setName(Text.literal("Enable All").formatted(Formatting.GREEN, Formatting.BOLD))
                .setCallback((idx, clickType, action) -> {
                    for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
                        DataManager.setBossAbilitiesEnabled(type, true);
                    }
                    DataManager.save(PoliticalServer.server);
                    player.sendMessage(Text.literal("✓ All abilities enabled").formatted(Formatting.GREEN), false);
                    openAbilitiesMenu(player);
                })
                .build());

        gui.setSlot(41, new GuiElementBuilder(Items.RED_WOOL)
                .setName(Text.literal("Disable All").formatted(Formatting.RED, Formatting.BOLD))
                .setCallback((idx, clickType, action) -> {
                    for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
                        DataManager.setBossAbilitiesEnabled(type, false);
                    }
                    DataManager.save(PoliticalServer.server);
                    player.sendMessage(Text.literal("✓ All abilities disabled").formatted(Formatting.GREEN), false);
                    openAbilitiesMenu(player);
                })
                .build());

        gui.setSlot(44, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((idx, clickType, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    private static void openAbilityDetailMenu(ServerPlayerEntity player, SlayerManager.SlayerType bossType) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal(bossType.displayName + " Abilities").formatted(bossType.color, Formatting.BOLD));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        boolean enabled = DataManager.isBossAbilitiesEnabled(bossType);
        int cooldown = DataManager.getBossAbilityCooldown(bossType);

        gui.setSlot(11, new GuiElementBuilder(enabled ? Items.LIME_WOOL : Items.RED_WOOL)
                .setName(Text.literal("Abilities: " + (enabled ? "ENABLED" : "DISABLED")).formatted(enabled ? Formatting.GREEN : Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal("Click to toggle").formatted(Formatting.GREEN))
                .setCallback((idx, clickType, action) -> {
                    DataManager.setBossAbilitiesEnabled(bossType, !enabled);
                    DataManager.save(PoliticalServer.server);
                    player.sendMessage(Text.literal("✓ " + bossType.bossName + " abilities " + (!enabled ? "enabled" : "disabled")).formatted(Formatting.GREEN), false);
                    openAbilityDetailMenu(player, bossType);
                })
                .build());

        gui.setSlot(15, new GuiElementBuilder(Items.CLOCK)
                .setName(Text.literal("Cooldown: " + cooldown + "s").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal("Set ability cooldown").formatted(Formatting.GREEN))
                .setCallback((idx, clickType, action) -> openCooldownSign(player, bossType))
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.GREEN_WOOL)
                .setName(Text.literal("Reset to Defaults").formatted(Formatting.GREEN, Formatting.BOLD))
                .setCallback((idx, clickType, action) -> {
                    DataManager.setBossAbilitiesEnabled(bossType, true);
                    DataManager.setBossAbilityCooldown(bossType, 30);
                    DataManager.save(PoliticalServer.server);
                    player.sendMessage(Text.literal("✓ Reset abilities for " + bossType.bossName).formatted(Formatting.GREEN), false);
                    openAbilityDetailMenu(player, bossType);
                })
                .build());

        gui.setSlot(26, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((idx, clickType, action) -> openAbilitiesMenu(player))
                .build());

        gui.open();
    }

    private static void openCooldownSign(ServerPlayerEntity player, SlayerManager.SlayerType bossType) {
        int current = DataManager.getBossAbilityCooldown(bossType);

        SignGui sign = new SignGui(player) {
            @Override
            public void onClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9]", "");
                if (!input.isEmpty()) {
                    try {
                        int val = Integer.parseInt(input);
                        if (val >= 5 && val <= 300) {
                            DataManager.setBossAbilityCooldown(bossType, val);
                            DataManager.save(PoliticalServer.server);
                            player.sendMessage(Text.literal("✓ Cooldown set to " + val + "s").formatted(Formatting.GREEN), false);
                        } else {
                            player.sendMessage(Text.literal("✗ Must be 5-300 seconds").formatted(Formatting.RED), false);
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage(Text.literal("✗ Invalid number").formatted(Formatting.RED), false);
                    }
                }
                openAbilityDetailMenu(player, bossType);
            }
        };
        sign.setLine(0, Text.literal(String.valueOf(current)));
        sign.setLine(1, Text.literal("Cooldown (5-300):"));
        sign.open();
    }

    // ==================== GEAR & DEFENSE ====================

    private static void openGearDefenseMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("🛡 Boss Gear & Defense").formatted(Formatting.GREEN, Formatting.BOLD));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        int universalDef = DataManager.getBossUniversalDefense();

        gui.setSlot(11, new GuiElementBuilder(Items.DIAMOND_CHESTPLATE)
                .setName(Text.literal("Boss-Specific Defense").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal("Defense per boss type").formatted(Formatting.GRAY))
                .setCallback((idx, clickType, action) -> openSpecificDefenseMenu(player))
                .build());

        gui.setSlot(15, new GuiElementBuilder(Items.SHIELD)
                .setName(Text.literal("Universal Defense: " + universalDef + "%").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal("Applied to all bosses").formatted(Formatting.GRAY))
                .setCallback((idx, clickType, action) -> openUniversalDefenseSign(player))
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.IRON_CHESTPLATE)
                .setName(Text.literal("Boss Weapons Config").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal("Coming soon...").formatted(Formatting.GRAY))
                .build());

        gui.setSlot(26, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((idx, clickType, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    private static void openSpecificDefenseMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("⚔ Boss-Specific Defense").formatted(Formatting.GOLD, Formatting.BOLD));

        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        SlayerManager.SlayerType[] types = SlayerManager.SlayerType.values();
        int[] slots = {11, 12, 13, 14, 15, 20, 21, 22};

        for (int i = 0; i < types.length && i < slots.length; i++) {
            final SlayerManager.SlayerType bossType = types[i];
            int def = DataManager.getBossSpecificDefense(bossType);

            gui.setSlot(slots[i], new GuiElementBuilder(Items.IRON_CHESTPLATE)
                    .setName(Text.literal(bossType.displayName).formatted(bossType.color, Formatting.BOLD))
                    .addLoreLine(Text.literal("Defense: " + def + "%").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("Click to set").formatted(Formatting.GREEN))
                    .setCallback((idx, clickType, action) -> openSpecificDefenseSign(player, bossType))
                    .build());
        }

        gui.setSlot(27, new GuiElementBuilder(Items.GREEN_WOOL)
                .setName(Text.literal("Reset All Defense").formatted(Formatting.GREEN, Formatting.BOLD))
                .setCallback((idx, clickType, action) -> {
                    for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
                        DataManager.setBossSpecificDefense(type, 0);
                    }
                    DataManager.save(PoliticalServer.server);
                    player.sendMessage(Text.literal("✓ Reset all boss defense").formatted(Formatting.GREEN), false);
                    openSpecificDefenseMenu(player);
                })
                .build());

        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((idx, clickType, action) -> openGearDefenseMenu(player))
                .build());

        gui.open();
    }

    private static void openSpecificDefenseSign(ServerPlayerEntity player, SlayerManager.SlayerType bossType) {
        int current = DataManager.getBossSpecificDefense(bossType);

        SignGui sign = new SignGui(player) {
            @Override
            public void onClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9]", "");
                if (!input.isEmpty()) {
                    try {
                        int val = Integer.parseInt(input);
                        if (val >= 0 && val <= 100) {
                            DataManager.setBossSpecificDefense(bossType, val);
                            DataManager.save(PoliticalServer.server);
                            player.sendMessage(Text.literal("✓ " + bossType.bossName + " defense: " + val + "%").formatted(Formatting.GREEN), false);
                        } else {
                            player.sendMessage(Text.literal("✗ Must be 0-100").formatted(Formatting.RED), false);
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage(Text.literal("✗ Invalid number").formatted(Formatting.RED), false);
                    }
                }
                openSpecificDefenseMenu(player);
            }
        };
        sign.setLine(0, Text.literal(String.valueOf(current)));
        sign.setLine(1, Text.literal(bossType.name() + " def%:"));
        sign.open();
    }

    private static void openUniversalDefenseSign(ServerPlayerEntity player) {
        int current = DataManager.getBossUniversalDefense();

        SignGui sign = new SignGui(player) {
            @Override
            public void onClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9]", "");
                if (!input.isEmpty()) {
                    try {
                        int val = Integer.parseInt(input);
                        if (val >= 0 && val <= 100) {
                            DataManager.setBossUniversalDefense(val);
                            DataManager.save(PoliticalServer.server);
                            player.sendMessage(Text.literal("✓ Universal defense: " + val + "%").formatted(Formatting.GREEN), false);
                        } else {
                            player.sendMessage(Text.literal("✗ Must be 0-100").formatted(Formatting.RED), false);
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage(Text.literal("✗ Invalid number").formatted(Formatting.RED), false);
                    }
                }
                openGearDefenseMenu(player);
            }
        };
        sign.setLine(0, Text.literal(String.valueOf(current)));
        sign.setLine(1, Text.literal("Universal def%:"));
        sign.open();
    }
}
