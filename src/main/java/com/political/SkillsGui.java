package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public class SkillsGui {

    public static void openMainMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Skills Menu").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));

        String uuid = player.getUUID().toString();

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray())
                .setName(Component.literal("")).build());
        }

        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
            .setName(Component.literal("Skyblock-style progression").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("HOTF / Classes / HOTS / HOTG — progression").withStyle(ChatFormatting.GRAY))
            .addLoreLine(Component.literal("+ Skill points & class bonuses").withStyle(ChatFormatting.GRAY))
            .addLoreLine(Component.literal("Progression Points: " + String.format("%,d", ProgressionTreeManager.getSkillPoints(uuid))).withStyle(ChatFormatting.YELLOW))
            .addLoreLine(Component.literal("Forest tiers: " + ProgressionTreeManager.getTotalTiers(uuid) + "/60").withStyle(ChatFormatting.AQUA))
            .glow()
            .build());

        gui.setSlot(19, new GuiElementBuilder(Items.DIAMOND_PICKAXE)
            .setName(Component.literal("Heart of the Forest (HOTF)").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("Six branches — combat to crafting").withStyle(ChatFormatting.GRAY))
            .addLoreLine(Component.literal("Skill points & lifetime tiers").withStyle(ChatFormatting.GRAY))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("Command: /hotf").withStyle(ChatFormatting.DARK_GREEN))
            .addLoreLine(Component.literal("Click to open").withStyle(ChatFormatting.GREEN))
            .setCallback((index, type, containerInput, slotGui) -> ProgressionTreeGui.openMainMenu(player))
            .build());

        gui.setSlot(21, new GuiElementBuilder(Items.IRON_SWORD)
            .setName(Component.literal("Classes").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("Pick a class, earn XP, unlock buffs").withStyle(ChatFormatting.GRAY))
            .addLoreLine(Component.literal("Replaces old mastery scoreboard track").withStyle(ChatFormatting.DARK_GRAY))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("Command: /class").withStyle(ChatFormatting.DARK_GREEN))
            .addLoreLine(Component.literal("Click to open").withStyle(ChatFormatting.GREEN))
            .setCallback((index, type, containerInput, slotGui) -> ClassGui.openMainMenu(player))
            .build());

        gui.setSlot(23, new GuiElementBuilder(Items.END_CRYSTAL)
            .setName(Component.literal("Heart of the Sectors (HOTS)").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("Four coin trees — like HotM paths").withStyle(ChatFormatting.GRAY))
            .addLoreLine(Component.literal("Mining, Grove, Tidal, Garden").withStyle(ChatFormatting.GRAY))
            .addLoreLine(Component.literal("Tiers: " + countAllAscensionTiers(uuid) + "/40").withStyle(ChatFormatting.AQUA))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("Command: /hots").withStyle(ChatFormatting.DARK_AQUA))
            .addLoreLine(Component.literal("Click to open").withStyle(ChatFormatting.AQUA))
            .setCallback((index, type, containerInput, slotGui) -> AscensionHeartsGui.openTreePicker(player))
            .build());

        gui.setSlot(25, new GuiElementBuilder(Items.GOLDEN_APPLE)
            .setName(Component.literal("Heart of the Grind (HOTG)").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("Magic Find & collections").withStyle(ChatFormatting.GRAY))
            .addLoreLine(Component.literal("Mine ores, kill mobs, roll rare drops").withStyle(ChatFormatting.GRAY))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("Command: /hotg").withStyle(ChatFormatting.DARK_GRAY))
            .addLoreLine(Component.literal("Click to open").withStyle(ChatFormatting.GOLD))
            .setCallback((index, type, containerInput, slotGui) -> AscensionGrindGui.openMain(player))
            .build());

        gui.setSlot(37, new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
            .setName(Component.literal("Quick Stats").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("Combat Bonus: +" + pct(ProgressionTreeManager.getCombatDamageBonus(uuid))).withStyle(ChatFormatting.RED))
            .addLoreLine(Component.literal("Mining Bonus: +" + pct(ProgressionTreeManager.getMiningSpeedBonus(uuid))).withStyle(ChatFormatting.GRAY))
            .addLoreLine(Component.literal("Farming Bonus: +" + pct(ProgressionTreeManager.getFarmingYieldBonus(uuid))).withStyle(ChatFormatting.GREEN))
            .addLoreLine(Component.literal("Fishing Bonus: +" + pct(ProgressionTreeManager.getFishingLuckBonus(uuid))).withStyle(ChatFormatting.AQUA))
            .addLoreLine(Component.literal("Exploration Bonus: +" + pct(ProgressionTreeManager.getExplorationSpeedBonus(uuid))).withStyle(ChatFormatting.YELLOW))
            .addLoreLine(Component.literal("Crafting Bonus: +" + pct(ProgressionTreeManager.getCraftingEfficiencyBonus(uuid))).withStyle(ChatFormatting.LIGHT_PURPLE))
            .build());

        gui.setSlot(43, new GuiElementBuilder(Items.NETHER_STAR)
            .setName(Component.literal("Your class").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
            .addLoreLine(Component.literal(""))
            .addLoreLine(buildClassSummaryLine(uuid))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("Click to manage class").withStyle(ChatFormatting.YELLOW))
            .setCallback((index, type, containerInput, slotGui) -> ClassGui.openMainMenu(player))
            .build());

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
            .setName(Component.literal("Close").withStyle(ChatFormatting.RED))
            .setCallback((index, type, containerInput, slotGui) -> player.closeContainer())
            .build());

        gui.open();
    }

    private static Component buildClassSummaryLine(String uuid) {
        ClassManager.PlayerClass pc = ClassManager.getPlayerClass(uuid);
        if (pc == null) {
            return Component.literal("No class — open Classes to pick one").withStyle(ChatFormatting.GRAY);
        }
        int lv = ClassManager.getClassLevel(uuid, pc);
        return Component.literal(pc.icon + " " + pc.name + " §7Lv." + lv).withStyle(pc.color);
    }

    private static String pct(double value) {
        return (int)(value * 100) + "%";
    }

    private static int countAllAscensionTiers(String uuid) {
        int total = 0;
        for (AscensionHeartSystem.HeartTree tree : AscensionHeartSystem.HeartTree.values()) {
            total += AscensionHeartSystem.countUnlockedInTree(uuid, tree);
        }
        return total;
    }
}
