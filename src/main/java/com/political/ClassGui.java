package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI for Classes System
 */
public class ClassGui {

    public static void openMainMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("§6§l⚔ Classes ⚔"));

        String uuid = player.getUUID().toString();

        // Fill background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                .setName(Component.literal(""))
                .build());
        }

        // Current class display
        ClassManager.PlayerClass currentClass = ClassManager.getPlayerClass(uuid);
        
        if (currentClass != null) {
            int level = ClassManager.getClassLevel(uuid, currentClass);
            int xp = ClassManager.getClassExperience(uuid, currentClass);
            
            gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal(currentClass.icon + " " + currentClass.name + " §7(Lvl " + level + ")")
                    .withStyle(currentClass.color))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7" + currentClass.description))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§bXP: §f" + (xp % 1000) + "/1000"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to view details"))
                .setCallback((index, type, containerInput, slotGui) -> openClassDetails(player, currentClass))
                .build());
        } else {
            gui.setSlot(4, new GuiElementBuilder(Items.BOOK)
                .setName(Component.literal("§7No Class Selected"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eChoose a class below!"))
                .build());
        }

        // Class selection
        ClassManager.PlayerClass[] classes = ClassManager.PlayerClass.values();
        int[] classSlots = {19, 20, 21, 22, 23, 24, 28, 29, 30, 31};
        
        for (int i = 0; i < classes.length && i < classSlots.length; i++) {
            ClassManager.PlayerClass playerClass = classes[i];
            int slot = classSlots[i];
            boolean isSelected = currentClass == playerClass;
            int classLevel = ClassManager.getClassLevel(uuid, playerClass);
            
            List<Component> lore = new ArrayList<>();
            lore.add(Component.literal(""));
            lore.add(Component.literal("§7" + playerClass.description));
            lore.add(Component.literal(""));
            lore.add(Component.literal("§aBuffs:"));
            for (ClassManager.ClassEffect effect : playerClass.buffs) {
                lore.add(Component.literal("  §a+ " + effect.name));
            }
            lore.add(Component.literal(""));
            lore.add(Component.literal("§cDebuffs:"));
            for (ClassManager.ClassEffect effect : playerClass.debuffs) {
                lore.add(Component.literal("  §c- " + effect.name));
            }
            lore.add(Component.literal(""));
            lore.add(Component.literal("§7Melee: §f" + String.format("%.0f%%", playerClass.meleeMultiplier * 100)));
            lore.add(Component.literal("§7Defense: §f" + String.format("%.0f%%", playerClass.defenseMultiplier * 100)));
            lore.add(Component.literal(""));
            
            if (isSelected) {
                lore.add(Component.literal("§a§l✓ SELECTED"));
            } else {
                long cooldown = ClassManager.getTimeUntilClassChange(uuid);
                if (cooldown > 0) {
                    lore.add(Component.literal("§cCooldown: " + (cooldown / 3600000) + "h remaining"));
                } else {
                    lore.add(Component.literal("§eClick to select"));
                }
            }
            
            GuiElementBuilder builder = new GuiElementBuilder(isSelected ? Items.ENCHANTED_BOOK : Items.BOOK)
                .setName(Component.literal(playerClass.icon + " " + playerClass.name)
                    .withStyle(playerClass.color));
            
            for (Component line : lore) {
                builder.addLoreLine(line);
            }
            
            builder.setCallback((index, type, containerInput, slotGui) -> {
                if (!isSelected) {
                    ClassManager.selectClass(player, playerClass);
                    openMainMenu(player);
                }
            });
            
            gui.setSlot(slot, builder.build());
        }

        // Stats button
        gui.setSlot(49, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
            .setName(Component.literal("§e§lClass Stats"))
            .setCallback((index, type, containerInput, slotGui) -> ClassManager.displayClassInfo(player))
            .build());

        gui.open();
    }

    public static void openClassDetails(ServerPlayer player, ClassManager.PlayerClass playerClass) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal(playerClass.icon + " " + playerClass.name).withStyle(playerClass.color));

        String uuid = player.getUUID().toString();

        // Fill background
        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                .setName(Component.literal(""))
                .build());
        }

        // Back button
        gui.setSlot(0, new GuiElementBuilder(Items.ARROW)
            .setName(Component.literal("§e← Back"))
            .setCallback((index, type, containerInput, slotGui) -> openMainMenu(player))
            .build());

        int level = ClassManager.getClassLevel(uuid, playerClass);
        int xp = ClassManager.getClassExperience(uuid, playerClass);
        int xpProgress = xp % 1000;
        
        // Class info
        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
            .setName(Component.literal(playerClass.icon + " " + playerClass.name + " §7(Lvl " + level + ")")
                .withStyle(playerClass.color))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7" + playerClass.description))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§bXP: §f" + xpProgress + "/1000"))
            .addLoreLine(Component.literal("§7Total XP: §f" + xp))
            .build());

        // Buffs
        int buffSlot = 10;
        for (ClassManager.ClassEffect effect : playerClass.buffs) {
            gui.setSlot(buffSlot++, new GuiElementBuilder(Items.LIME_DYE)
                .setName(Component.literal("§a+ " + effect.name))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Level " + (effect.amplifier + 1)))
                .build());
        }

        // Debuffs
        int debuffSlot = 16;
        for (ClassManager.ClassEffect effect : playerClass.debuffs) {
            gui.setSlot(debuffSlot++, new GuiElementBuilder(Items.RED_DYE)
                .setName(Component.literal("§c- " + effect.name))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Level " + (effect.amplifier + 1)))
                .build());
        }

        // Multipliers
        gui.setSlot(22, new GuiElementBuilder(Items.DIAMOND_SWORD)
            .setName(Component.literal("§7Melee: §f" + String.format("%.0f%%", playerClass.meleeMultiplier * 100)))
            .build());

        gui.open();
    }

    public static void openClassSelection(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("§6§lSelect Your Class"));

        String uuid = player.getUUID().toString();

        // Fill background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                .setName(Component.literal(""))
                .build());
        }

        // Info
        gui.setSlot(4, new GuiElementBuilder(Items.WRITABLE_BOOK)
            .setName(Component.literal("§eChoose Your Path"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Each class has unique buffs"))
            .addLoreLine(Component.literal("§7and debuffs. Choose wisely!"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§cClass change: 24h cooldown"))
            .build());

        // Classes
        ClassManager.PlayerClass[] classes = ClassManager.PlayerClass.values();
        int[] classSlots = {19, 20, 21, 22, 23, 24, 28, 29, 30, 31};
        
        for (int i = 0; i < classes.length && i < classSlots.length; i++) {
            ClassManager.PlayerClass playerClass = classes[i];
            int slot = classSlots[i];
            
            List<Component> lore = new ArrayList<>();
            lore.add(Component.literal(""));
            lore.add(Component.literal("§7" + playerClass.description));
            lore.add(Component.literal(""));
            lore.add(Component.literal("§aBuffs:"));
            for (ClassManager.ClassEffect effect : playerClass.buffs) {
                lore.add(Component.literal("  §a+ " + effect.name));
            }
            lore.add(Component.literal(""));
            lore.add(Component.literal("§cDebuffs:"));
            for (ClassManager.ClassEffect effect : playerClass.debuffs) {
                lore.add(Component.literal("  §c- " + effect.name));
            }
            lore.add(Component.literal(""));
            lore.add(Component.literal("§eClick to select"));
            
            GuiElementBuilder builder = new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal(playerClass.icon + " " + playerClass.name)
                    .withStyle(playerClass.color));
            
            for (Component line : lore) {
                builder.addLoreLine(line);
            }
            
            builder.setCallback((index, type, containerInput, slotGui) -> {
                if (ClassManager.selectClass(player, playerClass)) {
                    gui.close();
                }
            });
            
            gui.setSlot(slot, builder.build());
        }

        gui.open();
    }
}
