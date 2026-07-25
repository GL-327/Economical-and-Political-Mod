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

/** The companion roster. Summon one, see how it is growing, and dismiss it. */
public final class CompanionGui {

    private CompanionGui() {}

    private static final int[] CELLS = {
            10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    public static void open(ServerPlayer player) {
        open(player, 0, null);
    }

    public static void open(ServerPlayer player, int page, Companion.Role filter) {
        String uuid = player.getUUID().toString();
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Companions").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        WarGui.fill(gui, 54);

        Companion active = CompanionManager.activeOf(uuid);

        gui.setSlot(4, new GuiElementBuilder(Items.BONE)
                .setName(Component.literal("Companions").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Active: §f"
                        + (active == null ? "§8none" : CompanionManager.nameOf(uuid, active))))
                .addLoreLine(Component.literal("§8One walks with you at a time."))
                .glow().build());

        // Role filters down the left edge.
        gui.setSlot(0, roleButton(player, null, "All", Items.CHEST, ChatFormatting.WHITE, filter));
        int fi = 9;
        for (Companion.Role role : Companion.Role.values()) {
            if (fi > 45) break;
            gui.setSlot(fi, roleButton(player, role, role.label, Items.PAPER, role.colour, filter));
            fi += 9;
        }

        List<Companion> list = new ArrayList<>();
        for (Companion c : Companion.values()) {
            if (filter == null || c.role == filter) list.add(c);
        }

        int perPage = CELLS.length;
        int totalPages = Math.max(1, (list.size() + perPage - 1) / perPage);
        int p = Math.max(0, Math.min(page, totalPages - 1));
        int start = p * perPage;

        for (int i = 0; i < perPage && start + i < list.size(); i++) {
            Companion c = list.get(start + i);
            boolean owned = CompanionManager.owns(uuid, c);
            boolean isActive = active == c;
            int lvl = CompanionManager.levelOf(uuid, c);

            GuiElementBuilder b = new GuiElementBuilder(owned ? c.icon : Items.GRAY_DYE)
                    .setName(Component.literal(CompanionManager.nameOf(uuid, c))
                            .withStyle(owned ? c.colour : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§8" + c.rarity.colour + c.rarity.label
                            + " §8" + c.role.colour + c.role.label))
                    .addLoreLine(Component.literal("§7" + c.flavor))
                    .addLoreLine(Component.literal(""));

            if (owned) {
                b.addLoreLine(Component.literal("§7Level §f" + lvl + "§7/§f" + CompanionManager.maxLevel()));
                b.addLoreLine(Component.literal("§7Progress: §f"
                        + CompanionManager.xpInLevel(uuid, c) + "§7/§f" + CompanionManager.xpPerLevel()));
                b.addLoreLine(Component.literal("§7Health §f" + (int) c.healthAt(lvl)
                        + "  §7Damage §f" + String.format(java.util.Locale.ROOT, "%.1f", c.damageAt(lvl))));
                b.addLoreLine(Component.literal(""));
                b.addLoreLine(Component.literal(isActive
                        ? "§a§lOUT WITH YOU §7- click to dismiss"
                        : "§eClick to summon"));
                if (isActive) b.glow();

                final Companion sel = c;
                final int fp = p;
                final Companion.Role ff = filter;
                b.setCallback((a, ct, d, g) -> {
                    if (isActive) {
                        CompanionManager.dismiss(player);
                        CompanionManager.profile(uuid).active = null;
                        CompanionManager.save();
                        player.sendSystemMessage(Component.literal("§7" + sel.display + " returns to rest."));
                    } else {
                        CompanionManager.summon(player, sel);
                    }
                    open(player, fp, ff);
                });
            } else {
                b.addLoreLine(Component.literal("§cNot yet yours."));
                b.addLoreLine(Component.literal("§8Craft or find its token."));
            }
            gui.setSlot(CELLS[i], b.build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.LEAD)
                .setName(Component.literal("Dismiss").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Send your companion away for now."))
                .setCallback((a, b, c, d) -> {
                    CompanionManager.dismiss(player);
                    CompanionManager.profile(uuid).active = null;
                    CompanionManager.save();
                    open(player, page, filter);
                })
                .build());

        final int fp = p;
        final Companion.Role ff = filter;
        if (p > 0) gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Previous").withStyle(ChatFormatting.YELLOW))
                .setCallback((a, b, c, d) -> open(player, fp - 1, ff)).build());
        if (p < totalPages - 1) gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("Next →").withStyle(ChatFormatting.YELLOW))
                .setCallback((a, b, c, d) -> open(player, fp + 1, ff)).build());

        gui.setSlot(48, WarGui.back(() -> CivilizationHubGui.open(player)));
        gui.open();
    }

    private static eu.pb4.sgui.api.elements.GuiElement roleButton(
            ServerPlayer player, Companion.Role role, String label,
            net.minecraft.world.item.Item icon, ChatFormatting colour, Companion.Role active) {
        boolean on = role == active;
        GuiElementBuilder b = new GuiElementBuilder(icon)
                .setName(Component.literal(label).withStyle(colour, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(on ? "§a§lSHOWING" : "§eClick to filter"));
        if (on) b.glow();
        b.setCallback((a, c, d, g) -> open(player, 0, role));
        return b.build();
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("companion").executes(ctx -> {
            open(ctx.getSource().getPlayerOrException());
            return 1;
        }));
        dispatcher.register(Commands.literal("pets").executes(ctx -> {
            open(ctx.getSource().getPlayerOrException());
            return 1;
        }));
    }
}
