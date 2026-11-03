package com.political;


import com.political.bounty.BountySpawnManager;
import com.political.bounty.SlayerManager;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SignGui;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public class BountyConfigGui {

    public static void open(ServerPlayer admin) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, admin, false);
        gui.setTitle(Component.literal("⚙ Bounty Configuration ⚙"));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray())
                    .setName(Component.literal(""))
                    .build());
        }

        // Spawn settings
        gui.setSlot(10, new GuiElementBuilder(Items.SPAWNER)
                .setName(Component.literal("🎯 Spawn Settings").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Spawn Chance: " + (BountySpawnManager.getSpawnChance() * 100) + "%").withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal("Spawn Cooldown: " + (BountySpawnManager.getSpawnCooldown() / 20) + "s").withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal("Spawn Radius: " + BountySpawnManager.getSpawnRadius() + " blocks").withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to modify spawn settings").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> openSpawnSettings(admin))
                .build());

        // XP settings
        gui.setSlot(13, new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
                .setName(Component.literal("✨ XP Settings").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Damage Bonus: " + (SlayerManager.DAMAGE_BONUS_PER_LEVEL * 100) + "% per level").withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal("Damage Reduction: " + (SlayerManager.DAMAGE_REDUCTION_PER_LEVEL * 100) + "% per level").withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to modify XP settings").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> openXpSettings(admin))
                .build());

        // Difficulty settings
        gui.setSlot(16, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Component.literal("⚔ Difficulty Settings").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Boss health multiplier").withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal("Boss damage multiplier").withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to modify difficulty").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> openDifficultySettings(admin))
                .build());

        // Rewards settings
        gui.setSlot(22, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Component.literal("💰 Reward Settings").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Credit rewards per level").withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal("Coin rewards").withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to modify rewards").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> openRewardSettings(admin))
                .build());

        // Reset to defaults
        gui.setSlot(40, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("🔄 Reset to Defaults").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Reset all bounty settings").withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal("to default values").withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("⚠ This cannot be undone!").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> resetToDefaults(admin))
                .build());

        // Close button
        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("❌ Close").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                .setCallback((index, type, containerInput, slotGui) -> admin.closeContainer())
                .build());

        gui.open();
    }

    private static void openSpawnSettings(ServerPlayer admin) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, admin, false);
        gui.setTitle(Component.literal("Spawn Settings"));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray())
                    .setName(Component.literal(""))
                    .build());
        }

        gui.setSlot(11, new GuiElementBuilder(Items.SPAWNER)
                .setName(Component.literal("Spawn Chance").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("Current: " + (BountySpawnManager.getSpawnChance() * 100) + "%").withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to change").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> setSpawnChance(admin))
                .build());

        gui.setSlot(13, new GuiElementBuilder(Items.CLOCK)
                .setName(Component.literal("Spawn Cooldown").withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal("Current: " + (BountySpawnManager.getSpawnCooldown() / 20) + " seconds").withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to change").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> setSpawnCooldown(admin))
                .build());

        gui.setSlot(15, new GuiElementBuilder(Items.COMPASS)
                .setName(Component.literal("Spawn Radius").withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal("Current: " + BountySpawnManager.getSpawnRadius() + " blocks").withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to change").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> setSpawnRadius(admin))
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> open(admin))
                .build());

        gui.open();
    }

    private static void openXpSettings(ServerPlayer admin) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, admin, false);
        gui.setTitle(Component.literal("XP Settings"));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray())
                    .setName(Component.literal(""))
                    .build());
        }

        gui.setSlot(11, new GuiElementBuilder(Items.DIAMOND_SWORD)
                .setName(Component.literal("Damage Bonus").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("Current: " + (SlayerManager.DAMAGE_BONUS_PER_LEVEL * 100) + "% per level").withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to change").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> setDamageBonus(admin))
                .build());

        gui.setSlot(15, new GuiElementBuilder(Items.SHIELD)
                .setName(Component.literal("Damage Reduction").withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal("Current: " + (SlayerManager.DAMAGE_REDUCTION_PER_LEVEL * 100) + "% per level").withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to change").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> setDamageReduction(admin))
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> open(admin))
                .build());

        gui.open();
    }

    private static void setSpawnChance(ServerPlayer admin) {
        SignGui signGui = new SignGui(admin) {
            @Override
            public void onManualClose() {
                try {
                    double chance = Double.parseDouble(this.getLine(0).getString().trim()) / 100.0;
                    if (chance < 0 || chance > 1) {
                        admin.sendSystemMessage(Component.literal("Chance must be between 0 and 100").withStyle(ChatFormatting.RED), false);
                        return;
                    }
                    BountySpawnManager.setSpawnChance(chance);
                    admin.sendSystemMessage(Component.literal("✓ Spawn chance set to " + (chance * 100) + "%").withStyle(ChatFormatting.GREEN), false);
                    openSpawnSettings(admin);
                } catch (NumberFormatException e) {
                    admin.sendSystemMessage(Component.literal("Invalid number!").withStyle(ChatFormatting.RED), false);
                    openSpawnSettings(admin);
                }
            }
        };
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal("^^^^^^^^^^^^^^").withStyle(ChatFormatting.YELLOW));
        signGui.setLine(2, Component.literal("Enter % chance").withStyle(ChatFormatting.AQUA));
        signGui.setLine(3, Component.literal("Cur: " + (BountySpawnManager.getSpawnChance() * 100) + "%").withStyle(ChatFormatting.GRAY));
        signGui.open();
    }

    private static void setSpawnCooldown(ServerPlayer admin) {
        SignGui signGui = new SignGui(admin) {
            @Override
            public void onManualClose() {
                try {
                    int seconds = Integer.parseInt(this.getLine(0).getString().trim());
                    if (seconds < 1) {
                        admin.sendSystemMessage(Component.literal("Cooldown must be at least 1 second").withStyle(ChatFormatting.RED), false);
                        return;
                    }
                    BountySpawnManager.setSpawnCooldown(seconds * 20);
                    admin.sendSystemMessage(Component.literal("✓ Spawn cooldown set to " + seconds + " seconds").withStyle(ChatFormatting.GREEN), false);
                    openSpawnSettings(admin);
                } catch (NumberFormatException e) {
                    admin.sendSystemMessage(Component.literal("Invalid number!").withStyle(ChatFormatting.RED), false);
                    openSpawnSettings(admin);
                }
            }
        };
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal("^^^^^^^^^^^^^^").withStyle(ChatFormatting.YELLOW));
        signGui.setLine(2, Component.literal("Enter seconds").withStyle(ChatFormatting.AQUA));
        signGui.setLine(3, Component.literal("Cur: " + (BountySpawnManager.getSpawnCooldown() / 20) + "s").withStyle(ChatFormatting.GRAY));
        signGui.open();
    }

    private static void setSpawnRadius(ServerPlayer admin) {
        SignGui signGui = new SignGui(admin) {
            @Override
            public void onManualClose() {
                try {
                    int radius = Integer.parseInt(this.getLine(0).getString().trim());
                    if (radius < 5 || radius > 100) {
                        admin.sendSystemMessage(Component.literal("Radius must be between 5 and 100").withStyle(ChatFormatting.RED), false);
                        return;
                    }
                    BountySpawnManager.setSpawnRadius(radius);
                    admin.sendSystemMessage(Component.literal("✓ Spawn radius set to " + radius + " blocks").withStyle(ChatFormatting.GREEN), false);
                    openSpawnSettings(admin);
                } catch (NumberFormatException e) {
                    admin.sendSystemMessage(Component.literal("Invalid number!").withStyle(ChatFormatting.RED), false);
                    openSpawnSettings(admin);
                }
            }
        };
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal("^^^^^^^^^^^^^^").withStyle(ChatFormatting.YELLOW));
        signGui.setLine(2, Component.literal("Enter radius").withStyle(ChatFormatting.AQUA));
        signGui.setLine(3, Component.literal("Current: " + BountySpawnManager.getSpawnRadius()).withStyle(ChatFormatting.GRAY));
        signGui.open();
    }

    private static void setDamageBonus(ServerPlayer admin) {
        admin.sendSystemMessage(Component.literal("Damage bonus modification not yet implemented").withStyle(ChatFormatting.YELLOW), false);
    }

    private static void setDamageReduction(ServerPlayer admin) {
        admin.sendSystemMessage(Component.literal("Damage reduction modification not yet implemented").withStyle(ChatFormatting.YELLOW), false);
    }

    private static void openDifficultySettings(ServerPlayer admin) {
        admin.sendSystemMessage(Component.literal("Difficulty settings coming soon!").withStyle(ChatFormatting.YELLOW), false);
    }

    private static void openRewardSettings(ServerPlayer admin) {
        admin.sendSystemMessage(Component.literal("Reward settings coming soon!").withStyle(ChatFormatting.YELLOW), false);
    }

    private static void resetToDefaults(ServerPlayer admin) {
        BountySpawnManager.setSpawnChance(0.3);
        BountySpawnManager.setSpawnCooldown(100);
        BountySpawnManager.setSpawnRadius(24);
        
        admin.sendSystemMessage(Component.literal("✓ Spawn settings reset to defaults!").withStyle(ChatFormatting.GREEN), false);
        admin.sendSystemMessage(Component.literal("Note: XP settings are read-only").withStyle(ChatFormatting.YELLOW), false);
        open(admin);
    }
}
