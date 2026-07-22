package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

/** Backrooms status panel: where you are, how to get out, and what lives there. */
public final class BackroomsGui {

    private BackroomsGui() {}

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x5, player, false);
        gui.setTitle(Component.literal("The Backrooms").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        WarGui.fill(gui, 45);

        boolean inside = BackroomsManager.isBackrooms(player.level());

        gui.setSlot(4, new GuiElementBuilder(inside ? Items.YELLOW_CONCRETE : Items.END_PORTAL_FRAME)
                .setName(Component.literal(inside ? "You are inside" : "The Backrooms")
                        .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(inside
                        ? "§7Endless damp halls. Find a humming exit portal."
                        : "§7A place behind the world. You do not go there on purpose."))
                .glow().build());

        // Entry / exit
        if (inside) {
            gui.setSlot(20, new GuiElementBuilder(Items.COMPASS)
                    .setName(Component.literal("Locate Exit").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Exit portals hum. Explore to find one."))
                    .addLoreLine(Component.literal("§7Escaping counts toward the"))
                    .addLoreLine(Component.literal("§7§lBackrooms Survivor§r§7 buff."))
                    .build());

            gui.setSlot(24, new GuiElementBuilder(Items.ENDER_PEARL)
                    .setName(Component.literal("Leave (no reward)").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Step out without escaping properly."))
                    .addLoreLine(Component.literal("§cNo escape credit."))
                    .setCallback((a, b, c, d) -> {
                        BackroomsManager.leave(player);
                        player.closeContainer();
                    })
                    .build());
        } else {
            gui.setSlot(22, new GuiElementBuilder(Items.END_CRYSTAL)
                    .setName(Component.literal("How you get in").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7There is a §e3% §7chance of no-clipping"))
                    .addLoreLine(Component.literal("§7through an §eEnd Gateway§7 instead of"))
                    .addLoreLine(Component.literal("§7teleporting normally."))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§8Operators can force entry with /backrooms enter."))
                    .build());
        }

        // Field guide
        gui.setSlot(30, new GuiElementBuilder(Items.POTION)
                .setName(Component.literal("Almond Water").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Cleanses the effects of the halls"))
                .addLoreLine(Component.literal("§7and restores you briefly."))
                .addLoreLine(Component.literal("§8Found in stash rooms, or crafted."))
                .build());

        gui.setSlot(31, new GuiElementBuilder(Items.HUSK_SPAWN_EGG)
                .setName(Component.literal("Wanderers").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7They gather in the dark rooms and"))
                .addLoreLine(Component.literal("§7drag the light down with them."))
                .addLoreLine(Component.literal("§8Scaled by the usual tier system."))
                .build());

        gui.setSlot(32, new GuiElementBuilder(Items.ECHO_SHARD)
                .setName(Component.literal("Reality Shard").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7A piece of somewhere real."))
                .addLoreLine(Component.literal("§8Used to build an Escape Beacon."))
                .build());

        gui.setSlot(40, new GuiElementBuilder(Items.CRAFTING_TABLE)
                .setName(Component.literal("Backrooms Recipes").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("§7Browse everything craftable."))
                .setCallback((a, b, c, d) -> RecipesGui.openMainMenu(player))
                .build());

        gui.setSlot(36, WarGui.back(() -> CivilizationHubGui.open(player)));
        gui.open();
    }
}
