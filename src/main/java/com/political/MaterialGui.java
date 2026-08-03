package com.political;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * The refinery: every material and the chain each one runs.
 *
 * <p>Two screens. The list is all twenty four materials with what they are made of, and opening one
 * lays out its five steps side by side with the ratio between them and how many raw lumps each is
 * worth. That second screen is the important one, because the interesting fact about a super
 * compacted block is not what it does, it is that it represents six and a half thousand of the thing
 * at the bottom.</p>
 */
public final class MaterialGui {

    private MaterialGui() {}

    private static final int SIZE = 54;

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("The Refinery")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        GuiKit.header(gui, Items.BLAST_FURNACE, "The Refinery", ChatFormatting.AQUA,
                RefinedMaterial.values().length + " materials, five steps each",
                RefinedMaterial.totalItems() + " items, every one with a recipe",
                "Nine of a step makes one of the next");

        List<GuiElement> entries = new ArrayList<>();
        for (RefinedMaterial m : RefinedMaterial.values()) {
            int held = countHeld(player, m);
            entries.add(new GuiElementBuilder(m.baseFor(RefinedMaterial.Step.REFINED))
                    .setName(Component.literal(m.display).withStyle(m.colour, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§8" + m.flavor))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7Smelted from §f" + m.seedCost + " "
                            + new ItemStack(m.seed).getHoverName().getString()))
                    .addLoreLine(Component.literal("§7with §f"
                            + new ItemStack(m.reagent).getHoverName().getString()))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7You hold §f" + held
                            + " §7raw equivalent"))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§eClick to see the chain"))
                    .setCallback((a, t, ci, sg) -> openChain(player, m))
                    .build());
        }

        GuiKit.paint(gui, entries, 0, GuiKit.LIST_SLOTS.length);
        GuiKit.footer(gui, SIZE, player, null);
        gui.open();
    }

    private static void openChain(ServerPlayer player, RefinedMaterial m) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal(m.display).withStyle(m.colour, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        GuiKit.header(gui, m.baseFor(RefinedMaterial.Step.SUPER), m.display, m.colour,
                m.flavor,
                "Five steps, nine to one each");

        int[] slots = {20, 21, 22, 23, 24};
        RefinedMaterial.Step[] steps = RefinedMaterial.Step.values();
        for (int i = 0; i < steps.length; i++) {
            RefinedMaterial.Step st = steps[i];
            int held = countExact(player, m, st);

            GuiElementBuilder b = new GuiElementBuilder(m.baseFor(st))
                    .setName(Component.literal(m.displayFor(st))
                            .withStyle(st.colour, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7Worth §f" + GuiKit.num(st.rawWorth)
                            + " §7raw"))
                    .addLoreLine(Component.literal("§7Value §6"
                            + GuiKit.num(m.valueFor(st)) + " coins"))
                    .addLoreLine(Component.literal("§7You hold §f" + held));

            RefinedMaterial.Step below = st.below();
            b.addLoreLine(Component.literal(""));
            if (below == null) {
                b.addLoreLine(Component.literal("§8Made from vanilla goods and a T1 core."));
            } else {
                b.addLoreLine(Component.literal("§8Nine " + m.displayFor(below) + " make one."));
            }
            b.addLoreLine(Component.literal("§8Look it up in /recipe to craft it."));
            if (st.ordinal() >= RefinedMaterial.Step.ENCHANTED.ordinal()) b.glow();

            gui.setSlot(slots[i], b.build());
        }

        gui.setSlot(31, GuiKit.info(Items.PAPER, "How the chain works", ChatFormatting.WHITE,
                "Nine of any step makes one of the next.",
                "",
                "That means a super compacted block is",
                GuiKit.num(RefinedMaterial.Step.SUPER.rawWorth) + " raw " + m.display.toLowerCase() + ",",
                "which is a target rather than a purchase.",
                "",
                "Every step is a real recipe. Nothing here",
                "is hidden behind a command."));

        GuiKit.footer(gui, SIZE, player, () -> open(player));
        gui.open();
    }

    /** Raw equivalent of everything of this material a player is carrying. */
    private static int countHeld(ServerPlayer player, RefinedMaterial m) {
        int total = 0;
        for (ItemStack st : player.getInventory().getNonEquipmentItems()) {
            if (st.isEmpty()) continue;
            if (RefinedMaterialItems.materialOf(st) != m) continue;
            RefinedMaterial.Step step = RefinedMaterialItems.stepOf(st);
            if (step != null) total += st.getCount() * step.rawWorth;
        }
        return total;
    }

    private static int countExact(ServerPlayer player, RefinedMaterial m, RefinedMaterial.Step want) {
        int total = 0;
        for (ItemStack st : player.getInventory().getNonEquipmentItems()) {
            if (st.isEmpty()) continue;
            if (RefinedMaterialItems.materialOf(st) != m) continue;
            if (RefinedMaterialItems.stepOf(st) != want) continue;
            total += st.getCount();
        }
        return total;
    }
}
