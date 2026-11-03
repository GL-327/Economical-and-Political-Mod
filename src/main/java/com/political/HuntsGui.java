package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI for Hunts System
 */
public class HuntsGui {

    public static void openMainMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("§6§l★ Hunts ★"));

        String uuid = player.getUUID().toString();

        // Fill background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray())
                .setName(Component.literal(""))
                .build());
        }

        // Current tier info
        HuntsManager.HuntTier tier = HuntsManager.getPlayerTier(uuid);
        int totalHunts = HuntsManager.getTotalHuntsCompleted(uuid);
        
        gui.setSlot(4, new GuiElementBuilder(Items.GOLD_INGOT)
            .setName(Component.literal("§6§lYour Hunt Progress"))
            .addLoreLine(Component.literal("§7Tier: " + tier.stars + " " + tier.name))
            .addLoreLine(Component.literal("§7Hunts Completed: §f" + totalHunts))
            .build());

        // Active hunt display
        HuntsManager.Hunt activeHunt = HuntsManager.getActiveHunt(uuid);
        if (activeHunt != null) {
            int progress = HuntsManager.getProgress(uuid, activeHunt.id);
            
            gui.setSlot(13, new GuiElementBuilder(activeHunt.targetItem.getItem())
                .setName(Component.literal("§e§lActive Hunt: " + activeHunt.name))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Progress: §f" + progress + "/" + activeHunt.tier.itemAmount))
                .addLoreLine(Component.literal("§7Reward: §6" + String.format("%,d", activeHunt.tier.coinReward) + " coins"))
                .addLoreLine(Component.literal("§7Skill Points: §b+" + activeHunt.bonusSkillPoints))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§cClick to abandon"))
                .setCallback((index, type, containerInput, slotGui) -> {
                    HuntsManager.abandonHunt(player);
                    openMainMenu(player);
                })
                .build());

            // Claim button
            gui.setSlot(22, new GuiElementBuilder(Items.HOPPER)
                .setName(Component.literal("§a§lClaim Hunt Items"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Click to check inventory"))
                .addLoreLine(Component.literal("§7and complete hunt"))
                .setCallback((index, type, containerInput, slotGui) -> {
                    HuntsManager.checkInventoryForHunt(player);
                    openMainMenu(player);
                })
                .build());
        } else {
            gui.setSlot(13, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("§7No Active Hunt"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eSelect a hunt below to start!"))
                .build());
        }

        // Tier buttons (slots 28-32)
        HuntsManager.HuntTier[] tiers = HuntsManager.HuntTier.values();
        int[] tierSlots = {28, 30, 32, 34, 36};
        
        for (int t = 0; t < tiers.length; t++) {
            HuntsManager.HuntTier huntTier = tiers[t];
            int slot = tierSlots[t];
            
            boolean unlocked = huntTier.ordinal() <= tier.ordinal();
            var tierIcon = unlocked ? switch (huntTier) {
                case NOVICE -> Items.CONCRETE.lime();
                case APPRENTICE -> Items.CONCRETE.yellow();
                case JOURNEYMAN -> Items.CONCRETE.lightBlue();
                case EXPERT -> Items.CONCRETE.magenta();
                case MASTER -> Items.CONCRETE.orange();
            } : Items.BARRIER;
            
            gui.setSlot(slot, new GuiElementBuilder(tierIcon)
                .setName(Component.literal(huntTier.stars + " " + huntTier.name + " Hunts").withStyle(huntTier.color, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(unlocked ? "§eTier marker: colored block = difficulty band" : "§7Complete more hunts to unlock"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(unlocked ? "§aClick to view hunts" : "§cLocked"))
                .setCallback((index, type, containerInput, slotGui) -> {
                    if (unlocked) {
                        openTierHunts(player, huntTier);
                    }
                })
                .build());
        }

        // Stats
        gui.setSlot(49, new GuiElementBuilder(Items.BOOK)
            .setName(Component.literal("§e§lHunt Statistics"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Total Completed: §f" + totalHunts))
            .addLoreLine(Component.literal("§7Current Tier: §f" + tier.name))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Next tier at: §f" + getNextTierRequirement(tier.ordinal()) + " hunts"))
            .build());

        gui.open();
    }

    private static int getNextTierRequirement(int currentTier) {
        return switch (currentTier) {
            case 0 -> 5;
            case 1 -> 15;
            case 2 -> 30;
            case 3 -> 50;
            default -> -1;
        };
    }

    public static void openTierHunts(ServerPlayer player, HuntsManager.HuntTier tier) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal(tier.stars + " " + tier.name + " Hunts").withStyle(tier.color));

        String uuid = player.getUUID().toString();

        // Fill background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray())
                .setName(Component.literal(""))
                .build());
        }

        // Back button
        gui.setSlot(0, new GuiElementBuilder(Items.ARROW)
            .setName(Component.literal("§e← Back"))
            .setCallback((index, type, containerInput, slotGui) -> openMainMenu(player))
            .build());

        var tierHeaderIcon = switch (tier) {
            case NOVICE -> Items.CONCRETE.lime();
            case APPRENTICE -> Items.CONCRETE.yellow();
            case JOURNEYMAN -> Items.CONCRETE.lightBlue();
            case EXPERT -> Items.CONCRETE.magenta();
            case MASTER -> Items.CONCRETE.orange();
        };
        gui.setSlot(4, new GuiElementBuilder(tierHeaderIcon)
            .setName(Component.literal(tier.stars + " " + tier.name + " Hunts").withStyle(tier.color, ChatFormatting.BOLD))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Items Required: §f" + tier.itemAmount))
            .addLoreLine(Component.literal("§7Coin Reward: §6" + String.format("%,d", tier.coinReward)))
            .build());

        // List hunts
        List<HuntsManager.Hunt> hunts = HuntsManager.getHuntsForTier(tier);
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33};
        
        for (int i = 0; i < hunts.size() && i < slots.length; i++) {
            HuntsManager.Hunt hunt = hunts.get(i);
            int slot = slots[i];
            
            boolean completed = HuntsManager.isHuntCompleted(uuid, hunt.id);
            boolean isActive = HuntsManager.getActiveHunt(uuid) != null && 
                              HuntsManager.getActiveHunt(uuid).id.equals(hunt.id);
            
            ItemStack huntIcon = hunt.targetItem.copy();
            huntIcon.setCount(1);
            GuiElementBuilder builder = new GuiElementBuilder(huntIcon);
            if (completed || isActive) {
                builder.glow();
            }

            List<Component> lore = new ArrayList<>();
            lore.add(Component.literal(""));
            lore.add(Component.literal((completed ? "§a" : isActive ? "§e" : "§7") + "Status: "
                    + (completed ? "Done" : isActive ? "In progress" : "Available")));
            lore.add(Component.literal(""));
            lore.add(Component.literal("§7Collect: §f" + hunt.tier.itemAmount + "x " + 
                hunt.targetItem.getHoverName().getString()));
            lore.add(Component.literal("§7Reward: §6" + String.format("%,d", hunt.tier.coinReward) + " coins"));
            lore.add(Component.literal("§7Skill Points: §b+" + hunt.bonusSkillPoints));
            lore.add(Component.literal(""));
            
            if (completed) {
                lore.add(Component.literal("§a✓ Completed"));
            } else if (isActive) {
                int progress = HuntsManager.getProgress(uuid, hunt.id);
                lore.add(Component.literal("§eIn Progress: " + progress + "/" + hunt.tier.itemAmount));
            } else {
                lore.add(Component.literal("§eClick to start"));
            }

            for (String line : hunt.lore) {
                lore.add(Component.literal("§8" + line));
            }

            builder.setName(Component.literal((completed ? "§a✓ " : isActive ? "§e► " : "§7○ ") + hunt.name)
                .withStyle(completed ? ChatFormatting.GREEN : isActive ? ChatFormatting.YELLOW : ChatFormatting.GRAY));
            
            for (Component line : lore) {
                builder.addLoreLine(line);
            }

            if (!completed && !isActive) {
                builder.setCallback((index, type, containerInput, slotGui) -> {
                    HuntsManager.startHunt(player, hunt.id);
                    openTierHunts(player, tier);
                });
            }

            gui.setSlot(slot, builder.build());
        }

        gui.open();
    }
}
