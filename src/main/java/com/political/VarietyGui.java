package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * The state of the living world in one screen: the current season and its festival, any world-wide
 * modifier in effect, and the settlement events unfolding right now.
 */
public final class VarietyGui {

    private VarietyGui() {}

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x5, player, false);
        gui.setTitle(Component.literal("The Living World").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        WarGui.fill(gui, 45);

        gui.setSlot(4, new GuiElementBuilder(Items.CLOCK)
                .setName(Component.literal("The World Turns").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Seasons, festivals, and events in motion."))
                .glow().build());

        // Season + festival
        GuiElementBuilder season = new GuiElementBuilder(Items.WHEAT)
                .setName(Component.literal("Season").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Now: §f" + String.valueOf(SeasonManager.current())));
        if (SeasonFestivals.isActive()) {
            season.addLoreLine(Component.literal("§6Festival: §f" + String.valueOf(SeasonFestivals.current())));
        } else {
            season.addLoreLine(Component.literal("§8No festival right now."));
        }
        gui.setSlot(11, season.build());

        // World modifier
        GuiElementBuilder mod = new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("World Modifier").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        if (WorldModifiers.isActive()) {
            mod.addLoreLine(Component.literal("§dActive: §f" + String.valueOf(WorldModifiers.current())));
            mod.addLoreLine(Component.literal("§8A passing turn of fortune. It will end on its own."));
            mod.glow();
        } else {
            mod.addLoreLine(Component.literal("§8The world is quiet for now."));
        }
        gui.setSlot(13, mod.build());

        // Settlement events
        List<SettlementEvents.ActiveEvent> events = SettlementEvents.allActive();
        GuiElementBuilder ev = new GuiElementBuilder(Items.BELL)
                .setName(Component.literal("Settlement Events").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Happening now: §f" + events.size()));
        int shown = 0;
        for (SettlementEvents.ActiveEvent e : events) {
            if (shown++ >= 6) break;
            ev.addLoreLine(Component.literal("§8• §7" + String.valueOf(e)));
        }
        ev.addLoreLine(Component.literal(""));
        ev.addLoreLine(Component.literal("§eClick for the event board"));
        gui.setSlot(15, ev.setCallback((a, b, c, d) -> SettlementEventGui.open(player)).build());

        gui.setSlot(31, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("About the Living World").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Seasons change the harvest and the settlement's food."))
                .addLoreLine(Component.literal("§7Festivals lift a season with their own flavour."))
                .addLoreLine(Component.literal("§7World modifiers pass through and tweak the odds."))
                .addLoreLine(Component.literal("§7Settlement events strike one place at a time."))
                .addLoreLine(Component.literal("§8Everything here is temporary and cannot be farmed."))
                .build());

        gui.setSlot(40, WarGui.back(() -> CivilizationHubGui.open(player)));
        gui.open();
    }
}
