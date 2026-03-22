package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SignGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class BountyConfigGui {

    public static void open(ServerPlayerEntity admin) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, admin, false);
        gui.setTitle(Text.literal("⚙ Bounty Configuration ⚙"));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Spawn settings
        gui.setSlot(10, new GuiElementBuilder(Items.SPAWNER)
                .setName(Text.literal("🎯 Spawn Settings").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Spawn Chance: " + (BountySpawnManager.getSpawnChance() * 100) + "%").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("Spawn Cooldown: " + (BountySpawnManager.getSpawnCooldown() / 20) + "s").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("Spawn Radius: " + BountySpawnManager.getSpawnRadius() + " blocks").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to modify spawn settings").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> openSpawnSettings(admin))
                .build());

        // XP settings
        gui.setSlot(13, new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
                .setName(Text.literal("✨ XP Settings").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Damage Bonus: " + (SlayerManager.DAMAGE_BONUS_PER_LEVEL * 100) + "% per level").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("Damage Reduction: " + (SlayerManager.DAMAGE_REDUCTION_PER_LEVEL * 100) + "% per level").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to modify XP settings").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> openXpSettings(admin))
                .build());

        // Difficulty settings
        gui.setSlot(16, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Text.literal("⚔ Difficulty Settings").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Boss health multiplier").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("Boss damage multiplier").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to modify difficulty").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> openDifficultySettings(admin))
                .build());

        // Rewards settings
        gui.setSlot(22, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Text.literal("💰 Reward Settings").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Credit rewards per level").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("Coin rewards").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to modify rewards").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> openRewardSettings(admin))
                .build());

        // Reset to defaults
        gui.setSlot(40, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("🔄 Reset to Defaults").formatted(Formatting.DARK_RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Reset all bounty settings").formatted(Formatting.RED))
                .addLoreLine(Text.literal("to default values").formatted(Formatting.RED))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⚠ This cannot be undone!").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> resetToDefaults(admin))
                .build());

        // Close button
        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("❌ Close").formatted(Formatting.DARK_RED, Formatting.BOLD))
                .setCallback((index, type, action) -> admin.closeHandledScreen())
                .build());

        gui.open();
    }

    private static void openSpawnSettings(ServerPlayerEntity admin) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, admin, false);
        gui.setTitle(Text.literal("Spawn Settings"));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        gui.setSlot(11, new GuiElementBuilder(Items.SPAWNER)
                .setName(Text.literal("Spawn Chance").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("Current: " + (BountySpawnManager.getSpawnChance() * 100) + "%").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to change").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> setSpawnChance(admin))
                .build());

        gui.setSlot(13, new GuiElementBuilder(Items.CLOCK)
                .setName(Text.literal("Spawn Cooldown").formatted(Formatting.AQUA))
                .addLoreLine(Text.literal("Current: " + (BountySpawnManager.getSpawnCooldown() / 20) + " seconds").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to change").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> setSpawnCooldown(admin))
                .build());

        gui.setSlot(15, new GuiElementBuilder(Items.COMPASS)
                .setName(Text.literal("Spawn Radius").formatted(Formatting.RED))
                .addLoreLine(Text.literal("Current: " + BountySpawnManager.getSpawnRadius() + " blocks").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to change").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> setSpawnRadius(admin))
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> open(admin))
                .build());

        gui.open();
    }

    private static void openXpSettings(ServerPlayerEntity admin) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, admin, false);
        gui.setTitle(Text.literal("XP Settings"));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        gui.setSlot(11, new GuiElementBuilder(Items.DIAMOND_SWORD)
                .setName(Text.literal("Damage Bonus").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("Current: " + (SlayerManager.DAMAGE_BONUS_PER_LEVEL * 100) + "% per level").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to change").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> setDamageBonus(admin))
                .build());

        gui.setSlot(15, new GuiElementBuilder(Items.SHIELD)
                .setName(Text.literal("Damage Reduction").formatted(Formatting.RED))
                .addLoreLine(Text.literal("Current: " + (SlayerManager.DAMAGE_REDUCTION_PER_LEVEL * 100) + "% per level").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to change").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> setDamageReduction(admin))
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> open(admin))
                .build());

        gui.open();
    }

    private static void setSpawnChance(ServerPlayerEntity admin) {
        SignGui signGui = new SignGui(admin) {
            @Override
            public void onClose() {
                try {
                    double chance = Double.parseDouble(this.getLine(0).getString().trim()) / 100.0;
                    if (chance < 0 || chance > 1) {
                        admin.sendMessage(Text.literal("Chance must be between 0 and 100").formatted(Formatting.RED), false);
                        return;
                    }
                    BountySpawnManager.setSpawnChance(chance);
                    admin.sendMessage(Text.literal("✓ Spawn chance set to " + (chance * 100) + "%").formatted(Formatting.GREEN), false);
                    openSpawnSettings(admin);
                } catch (NumberFormatException e) {
                    admin.sendMessage(Text.literal("Invalid number!").formatted(Formatting.RED), false);
                    openSpawnSettings(admin);
                }
            }
        };
        signGui.setLine(0, Text.literal(String.valueOf(BountySpawnManager.getSpawnChance() * 100)));
        signGui.setLine(1, Text.literal("Enter % chance"));
        signGui.open();
    }

    private static void setSpawnCooldown(ServerPlayerEntity admin) {
        SignGui signGui = new SignGui(admin) {
            @Override
            public void onClose() {
                try {
                    int seconds = Integer.parseInt(this.getLine(0).getString().trim());
                    if (seconds < 1) {
                        admin.sendMessage(Text.literal("Cooldown must be at least 1 second").formatted(Formatting.RED), false);
                        return;
                    }
                    BountySpawnManager.setSpawnCooldown(seconds * 20);
                    admin.sendMessage(Text.literal("✓ Spawn cooldown set to " + seconds + " seconds").formatted(Formatting.GREEN), false);
                    openSpawnSettings(admin);
                } catch (NumberFormatException e) {
                    admin.sendMessage(Text.literal("Invalid number!").formatted(Formatting.RED), false);
                    openSpawnSettings(admin);
                }
            }
        };
        signGui.setLine(0, Text.literal(String.valueOf(BountySpawnManager.getSpawnCooldown() / 20)));
        signGui.setLine(1, Text.literal("Enter seconds"));
        signGui.open();
    }

    private static void setSpawnRadius(ServerPlayerEntity admin) {
        SignGui signGui = new SignGui(admin) {
            @Override
            public void onClose() {
                try {
                    int radius = Integer.parseInt(this.getLine(0).getString().trim());
                    if (radius < 5 || radius > 100) {
                        admin.sendMessage(Text.literal("Radius must be between 5 and 100").formatted(Formatting.RED), false);
                        return;
                    }
                    BountySpawnManager.setSpawnRadius(radius);
                    admin.sendMessage(Text.literal("✓ Spawn radius set to " + radius + " blocks").formatted(Formatting.GREEN), false);
                    openSpawnSettings(admin);
                } catch (NumberFormatException e) {
                    admin.sendMessage(Text.literal("Invalid number!").formatted(Formatting.RED), false);
                    openSpawnSettings(admin);
                }
            }
        };
        signGui.setLine(0, Text.literal(String.valueOf(BountySpawnManager.getSpawnRadius())));
        signGui.setLine(1, Text.literal("Enter radius"));
        signGui.open();
    }

    private static void setDamageBonus(ServerPlayerEntity admin) {
        admin.sendMessage(Text.literal("Damage bonus modification not yet implemented").formatted(Formatting.YELLOW), false);
    }

    private static void setDamageReduction(ServerPlayerEntity admin) {
        admin.sendMessage(Text.literal("Damage reduction modification not yet implemented").formatted(Formatting.YELLOW), false);
    }

    private static void openDifficultySettings(ServerPlayerEntity admin) {
        admin.sendMessage(Text.literal("Difficulty settings coming soon!").formatted(Formatting.YELLOW), false);
    }

    private static void openRewardSettings(ServerPlayerEntity admin) {
        admin.sendMessage(Text.literal("Reward settings coming soon!").formatted(Formatting.YELLOW), false);
    }

    private static void resetToDefaults(ServerPlayerEntity admin) {
        BountySpawnManager.setSpawnChance(0.3);
        BountySpawnManager.setSpawnCooldown(100);
        BountySpawnManager.setSpawnRadius(24);
        
        admin.sendMessage(Text.literal("✓ Spawn settings reset to defaults!").formatted(Formatting.GREEN), false);
        admin.sendMessage(Text.literal("Note: XP settings are read-only").formatted(Formatting.YELLOW), false);
        open(admin);
    }
}
