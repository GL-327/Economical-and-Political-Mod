package com.political;


import com.political.economy.CoinManager;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.HashSet;
import java.util.Set;

/**
 * Heart of the Sectors (HOTS) — four parallel “powder” trees (Skyblock-inspired sector progression).
 */
public final class AscensionHeartsGui {

    private AscensionHeartsGui() {}

    public static void openTreePicker(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Heart of the Sectors").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Component.literal("")).build());
        }
        for (int c = 0; c < 9; c++) {
            gui.setSlot(c, new GuiElementBuilder(Items.CYAN_STAINED_GLASS_PANE).setName(Component.literal("")).build());
            gui.setSlot(45 + c, new GuiElementBuilder(Items.BLUE_STAINED_GLASS_PANE).setName(Component.literal("")).build());
        }
        for (int r = 1; r < 5; r++) {
            gui.setSlot(r * 9, new GuiElementBuilder(Items.CYAN_STAINED_GLASS_PANE).setName(Component.literal("")).build());
            gui.setSlot(r * 9 + 8, new GuiElementBuilder(Items.CYAN_STAINED_GLASS_PANE).setName(Component.literal("")).build());
        }

        String uuid = player.getUUID().toString();
        int totalTiers = 0;
        for (AscensionHeartSystem.HeartTree t : AscensionHeartSystem.HeartTree.values()) {
            totalTiers += AscensionHeartSystem.countUnlockedInTree(uuid, t);
        }

        gui.setSlot(4, new GuiElementBuilder(Items.END_CRYSTAL)
                .setName(Component.literal("Sector Heart Network").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Four trees — like different HotM paths:").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Mining Depths, Grove, Tidal, Verdant.").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Spend coins for passive perks (tiers 1–10).").withStyle(ChatFormatting.DARK_GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Total tiers unlocked: " + totalTiers + " / 40").withStyle(ChatFormatting.AQUA))
                .glow()
                .build());

        int[] slots = {20, 22, 24, 30};
        AscensionHeartSystem.HeartTree[] trees = AscensionHeartSystem.HeartTree.values();
        for (int i = 0; i < trees.length && i < slots.length; i++) {
            AscensionHeartSystem.HeartTree t = trees[i];
            int unlocked = AscensionHeartSystem.countUnlockedInTree(uuid, t);
            gui.setSlot(slots[i], new GuiElementBuilder(t.icon)
                    .setName(Component.literal(t.title).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(stripColorCodes(t.subtitle)).withStyle(ChatFormatting.DARK_GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Tiers: " + unlocked + " / 10").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to open tree").withStyle(ChatFormatting.GOLD))
                    .setCallback((a, b, c, __slotGui) -> openTree(player, t))
                    .build());
        }

        gui.setSlot(38, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Component.literal("Coin costs scale to billions").withStyle(ChatFormatting.GOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Endgame sector progression —").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("pair with /hotg for Magic Find.").withStyle(ChatFormatting.GRAY))
                .build());

        gui.setSlot(40, new GuiElementBuilder(Items.RABBIT_FOOT)
                .setName(Component.literal("Magic Find link").withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Grind ores & mobs in /hotg to raise MF.").withStyle(ChatFormatting.GRAY))
                .setCallback((a, b, c, d) -> AscensionGrindGui.openMain(player))
                .build());

        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("Back to Skills Hub").withStyle(ChatFormatting.YELLOW))
                .setCallback((a, b, c, d) -> SkillsGui.openMainMenu(player))
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Close").withStyle(ChatFormatting.RED))
                .setCallback((a, b, c, __slotGui) -> player.closeContainer())
                .build());

        gui.open();
    }

    private static String stripColorCodes(String s) {
        if (s == null) return "";
        return s.replaceAll("§.", "");
    }

    public static void openTree(ServerPlayer player, AscensionHeartSystem.HeartTree tree) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal(tree.title).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Component.literal("")).build());
        }
        for (int c = 0; c < 9; c++) {
            gui.setSlot(c, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Component.literal("")).build());
            gui.setSlot(45 + c, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Component.literal("")).build());
        }

        gui.setSlot(4, new GuiElementBuilder(tree.icon)
                .setName(Component.literal(tree.title).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(stripColorCodes(tree.subtitle)).withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Unlock left-to-right; costs are in coins.").withStyle(ChatFormatting.DARK_GRAY))
                .glow()
                .build());

        Set<String> u = new HashSet<>(AscensionHeartSystem.getUnlocked(player.getUUID().toString(), tree));
        int baseSlot = 10;
        for (AscensionHeartSystem.HeartNode node : AscensionHeartSystem.nodesFor(tree)) {
            int slot = baseSlot + node.col();
            boolean has = u.contains(node.id());
            boolean can = !has && AscensionHeartSystem.canUnlock(player, node.id()) && CoinManager.hasCoins(player, node.coinCost());

            var b = new GuiElementBuilder(has ? Items.LIME_STAINED_GLASS_PANE : (can ? Items.GOLD_BLOCK : Items.GRAY_STAINED_GLASS_PANE))
                    .setName(Component.literal(node.name()).withStyle(has ? ChatFormatting.GREEN : can ? ChatFormatting.YELLOW : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(node.description()).withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal(String.format("%,d", node.coinCost()) + " coins").withStyle(ChatFormatting.GOLD));
            if (has) {
                b.addLoreLine(Component.literal("Unlocked").withStyle(ChatFormatting.GREEN));
            } else if (can) {
                b.addLoreLine(Component.literal("Click to purchase").withStyle(ChatFormatting.GREEN));
                String nid = node.id();
                b.setCallback((x, y, z, __slotGui) -> {
                    AscensionHeartSystem.tryPurchase(player, nid);
                    openTree(player, tree);
                });
            } else {
                b.addLoreLine(Component.literal("Locked").withStyle(ChatFormatting.RED));
            }
            gui.setSlot(slot, b.build());
        }

        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("Back to sector hub").withStyle(ChatFormatting.GRAY))
                .setCallback((a, b, c, __slotGui) -> openTreePicker(player))
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Close").withStyle(ChatFormatting.RED))
                .setCallback((a, b, c, __slotGui) -> player.closeContainer())
                .build());

        gui.open();
    }
}
