package com.political.settlement;
import com.political.Citizen;
import com.political.CitizenManager;
import com.political.CivilizationHubGui;
import com.political.government.Treasury;


import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Settlement economy dashboard: production, workforce, growth gates and appeal breakdown. */
public final class SettlementEconomyGui {

    private SettlementEconomyGui() {}

    public static void open(ServerPlayer player, Settlement s) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal(s.name + " — Economy"));
        WarGui.fill(gui, 54);

        List<Citizen> citizens = CitizenManager.citizensOf(s.id);
        EnumMap<Citizen.CitizenJob, Integer> counts = new EnumMap<>(Citizen.CitizenJob.class);
        for (Citizen c : citizens) counts.merge(c.jobEnum(), 1, Integer::sum);

        SettlementTier tier = s.tierEnum();
        gui.setSlot(4, new GuiElementBuilder(Items.BELL)
                .setName(Component.literal(s.name).withStyle(tier.color, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Tier: " + tier.colorCode + tier.displayName))
                .addLoreLine(Component.literal("§7Population: §f" + s.population))
                .glow().build());

        // ── Production ──
        gui.setSlot(19, stat(Items.BREAD, "Food", s.food + " / " + s.population + " needed",
                s.population > 0 && s.food < s.population
                        ? "§cSTARVING — growth halted" : "§aSurplus"));
        gui.setSlot(20, stat(Items.EMERALD, "Goods", String.valueOf(s.goods),
                "§8Traded for coin each cycle"));
        gui.setSlot(21, stat(Items.IRON_SWORD, "Defense", String.valueOf(s.defense),
                "§8Deters raids on the treasury"));
        gui.setSlot(22, stat(Items.CAKE, "Happiness", s.happiness + "%",
                happinessNote(s.happiness)));
        gui.setSlot(23, stat(Items.GOLD_INGOT, "Treasury", String.valueOf(s.treasury),
                "§7Last cycle: §6+" + s.lastIncome + " §8(tax " + s.lastTaxPaid + ")"));
        gui.setSlot(24, stat(Items.CRAFTING_TABLE, "Workstations", String.valueOf(s.jobSites),
                "§8Caps how many citizens can work"));

        // ── Workforce ──
        GuiElementBuilder work = new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal("Workforce").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        if (counts.isEmpty()) {
            work.addLoreLine(Component.literal("§8No citizens yet."));
        } else {
            for (Map.Entry<Citizen.CitizenJob, Integer> e : counts.entrySet()) {
                work.addLoreLine(Component.literal("  " + e.getKey().color + e.getKey().displayName
                        + "§7: §f" + e.getValue()));
            }
        }
        work.addLoreLine(Component.literal(""));
        work.addLoreLine(Component.literal("§8Citizens self-assign to free workstations."));
        work.addLoreLine(Component.literal("§8Sheriffs are staffed before traders."));
        gui.setSlot(31, work.build());

        // ── Growth gate ──
        boolean canGrow = SettlementEconomy.canGrow(s);
        gui.setSlot(29, new GuiElementBuilder(canGrow ? Items.CONCRETE.lime() : Items.CONCRETE.red())
                .setName(Component.literal(canGrow ? "Growing" : "Growth Stalled")
                        .withStyle(canGrow ? ChatFormatting.GREEN : ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Free sheltered beds: §f"
                        + Math.max(0, s.beds - s.population)))
                .addLoreLine(Component.literal("§7Happiness ≥45: " + (s.happiness >= 45 ? "§a✔" : "§c✘")))
                .addLoreLine(Component.literal("§7Food covers population: "
                        + (s.population == 0 || s.food >= s.population ? "§a✔" : "§c✘")))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§8All three are required for settlers to arrive."))
                .build());

        // ── Appeal breakdown (anti-cheese transparency) ──
        GuiElementBuilder appeal = new GuiElementBuilder(Items.FLOWER_POT)
                .setName(Component.literal("Appeal: " + s.appeal)
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        if (s.appealByCategory == null || s.appealByCategory.isEmpty()) {
            appeal.addLoreLine(Component.literal("§8Nothing decorative built yet."));
        } else {
            for (Map.Entry<String, Integer> e : s.appealByCategory.entrySet()) {
                appeal.addLoreLine(Component.literal("  §7" + e.getKey() + ": §f" + e.getValue()));
            }
        }
        appeal.addLoreLine(Component.literal(""));
        appeal.addLoreLine(Component.literal("§7Developed area: §f" + s.footprintCells + " cells"));
        appeal.addLoreLine(Component.literal("§7Sheltered beds: §f" + s.beds + " §8of " + s.rawBeds));
        appeal.addLoreLine(Component.literal(""));
        appeal.addLoreLine(Component.literal("§8Every category is capped, and each block type"));
        appeal.addLoreLine(Component.literal("§8gives less the more you place. Variety and"));
        appeal.addLoreLine(Component.literal("§8spread beat stacking one valuable block."));
        gui.setSlot(33, appeal.build());

        gui.setSlot(45, WarGui.back(com.political.core.gui.MenuTrail.backOr(player, () -> CivilizationHubGui.open(player))));
        gui.setSlot(49, new GuiElementBuilder(Items.BOOK)
                .setName(Component.literal("Settlement Panel").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("§7Tier requirements and registration."))
                .setCallback((a, b, c, d) -> SettlementGui.open(player, s))
                .build());
        gui.open();
    }

    private static String happinessNote(int h) {
        if (h >= 80) return "§aThriving";
        if (h >= 60) return "§eContent";
        if (h >= 45) return "§6Restless";
        return "§cMiserable — no one will settle here";
    }

    private static eu.pb4.sgui.api.elements.GuiElement stat(net.minecraft.world.item.Item icon,
                                                            String label, String value, String note) {
        return new GuiElementBuilder(icon)
                .setName(Component.literal(label).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§f" + value))
                .addLoreLine(Component.literal(note))
                .build();
    }
}
