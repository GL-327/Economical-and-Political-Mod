package com.political;

import com.mojang.brigadier.CommandDispatcher;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/** The accessory bag. Equip the relics you want live, browse the ones you have not found yet. */
public final class RelicGui {

    private RelicGui() {}

    private static final int[] CELLS = {
            10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    public static void open(ServerPlayer player) {
        open(player, 0, null);
    }

    public static void open(ServerPlayer player, int page, Relic.Family filter) {
        String uuid = player.getUUID().toString();
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Accessory Bag").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        WarGui.fill(gui, 54);

        int slots = RelicManager.slots(uuid);
        List<Relic> equipped = RelicManager.equipped(uuid);

        gui.setSlot(4, new GuiElementBuilder(Items.BUNDLE)
                .setName(Component.literal("Accessory Bag").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Equipped: §f" + equipped.size() + "§7/§f" + slots))
                .addLoreLine(Component.literal("§8Only relics in the bag do anything."))
                .addLoreLine(Component.literal("§8Choose what kind of fighter you are."))
                .glow().build());

        // Filter row so families are easy to find.
        gui.setSlot(0, filterButton(player, page, null, "All", Items.CHEST, ChatFormatting.WHITE, filter));
        int fi = 9;
        for (Relic.Family fam : Relic.Family.values()) {
            if (fi > 45) break;
            gui.setSlot(fi, filterButton(player, 0, fam, fam.label,
                    Items.PAPER, fam.colour, filter));
            fi += 9;
        }

        // The relic list.
        List<Relic> list = new ArrayList<>();
        for (Relic r : Relic.values()) {
            if (filter == null || r.family == filter) list.add(r);
        }

        int perPage = CELLS.length;
        int totalPages = Math.max(1, (list.size() + perPage - 1) / perPage);
        int p = Math.max(0, Math.min(page, totalPages - 1));
        int start = p * perPage;

        for (int i = 0; i < perPage && start + i < list.size(); i++) {
            Relic r = list.get(start + i);
            boolean owned = RelicManager.owns(uuid, r);
            boolean on = RelicManager.isEquipped(uuid, r);

            GuiElementBuilder b = new GuiElementBuilder(owned ? r.icon : Items.GRAY_DYE)
                    .setName(Component.literal(r.display)
                            .withStyle(owned ? r.colour : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§8" + r.family.label + " Relic"))
                    .addLoreLine(Component.literal("§7" + r.flavor))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7Fires: §f" + switch (r.trigger) {
                        case ON_HIT -> "when you strike";
                        case ON_KILL -> "when you kill";
                        case ON_HURT -> "when you are hurt";
                        case PASSIVE -> "always, while carried";
                    }));

            if (!owned) {
                b.addLoreLine(Component.literal("§cNot found yet."));
            } else if (on) {
                b.addLoreLine(Component.literal("§a§lEQUIPPED §7- click to remove"));
                b.glow();
            } else {
                b.addLoreLine(Component.literal("§eClick to equip"));
            }

            if (owned) {
                final Relic sel = r;
                final int fp = p;
                final Relic.Family ff = filter;
                b.setCallback((a, c, d, g) -> {
                    RelicManager.toggle(player, sel);
                    open(player, fp, ff);
                });
            }
            gui.setSlot(CELLS[i], b.build());
        }

        // Bag upgrade.
        gui.setSlot(49, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Component.literal("Expand the Bag").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Holds §f" + slots + " §7relics now."))
                .addLoreLine(Component.literal("§7Each expansion costs more than the last."))
                .setCallback((a, b, c, d) -> {
                    RelicManager.upgradeBag(player);
                    open(player, page, filter);
                })
                .build());

        final int fp = p;
        final Relic.Family ff = filter;
        if (p > 0) gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Previous").withStyle(ChatFormatting.YELLOW))
                .setCallback((a, b, c, d) -> open(player, fp - 1, ff)).build());
        if (p < totalPages - 1) gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("Next →").withStyle(ChatFormatting.YELLOW))
                .setCallback((a, b, c, d) -> open(player, fp + 1, ff)).build());

        gui.setSlot(48, WarGui.back(() -> CivilizationHubGui.open(player)));
        gui.open();
    }

    private static eu.pb4.sgui.api.elements.GuiElement filterButton(
            ServerPlayer player, int page, Relic.Family fam, String label,
            net.minecraft.world.item.Item icon, ChatFormatting colour, Relic.Family active) {
        boolean on = fam == active;
        GuiElementBuilder b = new GuiElementBuilder(icon)
                .setName(Component.literal(label).withStyle(colour, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(on ? "§a§lSHOWING" : "§eClick to filter"));
        if (on) b.glow();
        b.setCallback((a, c, d, g) -> open(player, 0, fam));
        return b.build();
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("relics").executes(ctx -> {
            open(ctx.getSource().getPlayerOrException());
            return 1;
        }));
        dispatcher.register(Commands.literal("bag").executes(ctx -> {
            open(ctx.getSource().getPlayerOrException());
            return 1;
        }));
    }
}
