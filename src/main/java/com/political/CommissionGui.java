package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

/**
 * The requisition board.
 *
 * <p>Built to read like a board rather than a menu. Postings are pinned paper across the middle, the
 * frame is oak and the empty slots are bare boards with nothing on them yet, so the screen shows how
 * much room a player has earned as well as what is currently on offer.</p>
 */
public final class CommissionGui {

    private CommissionGui() {}

    private static final int SIZE = 54;
    /** Where postings hang. Spaced out so each one reads as a separate sheet of paper. */
    private static final int[] POST_SLOTS = {20, 22, 24, 30, 32};

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("The Requisition Board")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        // An oak frame rather than the usual glass, so the board feels like a physical object.
        for (int i = 0; i < SIZE; i++) {
            int row = i / 9;
            int col = i % 9;
            boolean edge = row == 0 || row == 5 || col == 0 || col == 8;
            gui.setSlot(i, new GuiElementBuilder(edge ? Items.OAK_LOG : Items.OAK_PLANKS)
                    .setName(Component.literal(" ")).build());
        }

        int slots = CommissionManager.slotCount(player);
        int standing = CommissionManager.standing(player);
        int done = CommissionManager.completed(player);
        int toNext = CommissionManager.toNextSlot(player);

        gui.setSlot(4, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("The Requisition Board")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§8Weathered, overwritten, and never empty."))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Standing §f" + standing))
                .addLoreLine(Component.literal("§7Completed §f" + done))
                .addLoreLine(Component.literal("§7Postings held §f" + slots
                        + "§8 of " + CommissionManager.MAX_SLOTS))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(toNext > 0
                        ? "§7Another slot at §f" + toNext + " §7more standing"
                        : "§aEvery slot earned."))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§8" + Commission.total()
                        + " postings exist. You will not see them all."))
                .glow().build());

        for (int i = 0; i < POST_SLOTS.length; i++) {
            if (i < slots) {
                paintPosting(gui, player, i, POST_SLOTS[i]);
            } else {
                gui.setSlot(POST_SLOTS[i], new GuiElementBuilder(Items.BARRIER)
                        .setName(Component.literal("Bare board")
                                .withStyle(ChatFormatting.DARK_GRAY))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("§8Nothing pinned here yet."))
                        .addLoreLine(Component.literal("§7Earn standing and the board"))
                        .addLoreLine(Component.literal("§7will trust you with more."))
                        .build());
            }
        }

        gui.setSlot(48, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Component.literal("The Quartermaster's Warrant")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Paid out by Great Works and"))
                .addLoreLine(Component.literal("§7Impossibilities. Four pieces."))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Charge §f" + WarrantArmour.chargeOf(player)
                        + "§8 of " + WarrantArmour.MAX_CHARGE))
                .addLoreLine(Component.literal(WarrantArmour.fullSet(player)
                        ? "§6Levy is available."
                        : "§8Levy needs all four worn."))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to read the warrant"))
                .setCallback((a, t, c, d) -> openWarrant(player))
                .build());

        gui.setSlot(50, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("✕ Close").withStyle(ChatFormatting.RED))
                .setCallback((a, t, c, d) -> player.closeContainer())
                .build());

        gui.open();
    }

    private static void paintPosting(SimpleGui gui, ServerPlayer player, int slot, int guiSlot) {
        Commission c = CommissionManager.commissionAt(player, slot);
        if (c == null) return;

        int have = CommissionManager.held(player, c);
        boolean ready = have >= c.count;
        long mins = CommissionManager.rerollMinutesLeft(player, slot);

        GuiElementBuilder b = new GuiElementBuilder(ready ? Items.WRITTEN_BOOK : Items.PAPER)
                .setName(Component.literal(c.framing.label)
                        .withStyle(c.framing.colour, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§8" + c.framing.flavor))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(c.band.colour + "" + ChatFormatting.BOLD
                        + c.band.label))
                .addLoreLine(Component.literal("§f" + c.title()))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7You have §f" + have + "§8 of §f" + c.count))
                .addLoreLine(Component.literal(GuiKit.bar(
                        c.count == 0 ? 0 : have / (double) c.count,
                        ready ? ChatFormatting.GREEN : ChatFormatting.RED,
                        ChatFormatting.DARK_GRAY)))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Pays §6" + GuiKit.num(c.coinReward()) + " coins"))
                .addLoreLine(Component.literal("§7Grants §a" + c.xpReward() + " experience"))
                .addLoreLine(Component.literal("§7Standing §f+" + c.standingReward()));

        Crate key = c.keyReward();
        if (key != null) {
            b.addLoreLine(Component.literal("§7Plus a " + key.colour + key.display + " §7key"));
        }
        if (c.awardsWarrantPiece()) {
            b.addLoreLine(Component.literal("§6A piece of the Quartermaster's Warrant"));
        }

        b.addLoreLine(Component.literal(""));
        b.addLoreLine(Component.literal("§8The goods are taken when you hand in."));
        b.addLoreLine(Component.literal(""));
        if (ready) {
            b.addLoreLine(Component.literal("§aLeft click to hand it in"));
            b.glow();
        } else {
            b.addLoreLine(Component.literal("§cYou are short by " + (c.count - have)));
        }
        b.addLoreLine(Component.literal(mins > 0
                ? "§8Right click to reroll in " + mins + " min"
                : "§eRight click to reroll"));

        gui.setSlot(guiSlot, b.setCallback((a, type, ci, sg) -> {
            if (type.isRight) {
                CommissionManager.reroll(player, slot);
            } else if (ready) {
                CommissionManager.complete(player, slot);
            } else if (WarrantArmour.fullSet(player)) {
                // The capstone lives on the posting itself, so it is discovered where it is used.
                CommissionManager.requisition(player, slot);
            } else {
                player.sendSystemMessage(Component.literal("§cYou are short by "
                        + (c.count - have) + " " + c.good.display() + "."));
            }
            open(player);
        }).build());
    }

    // ── The warrant ───────────────────────────────────────────────────────

    private static void openWarrant(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("The Quartermaster's Warrant")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        int worn = WarrantArmour.wornBy(player).size();
        GuiKit.header(gui, Items.GOLDEN_HELMET, "The Quartermaster's Warrant", ChatFormatting.GOLD,
                worn + " of 4 worn",
                "Charge " + WarrantArmour.chargeOf(player) + " of " + WarrantArmour.MAX_CHARGE);

        int[] slots = {20, 21, 23, 24};
        WarrantArmour.Piece[] pieces = WarrantArmour.Piece.values();
        for (int i = 0; i < pieces.length && i < slots.length; i++) {
            WarrantArmour.Piece p = pieces[i];
            boolean wearing = WarrantArmour.pieceOf(player.getItemBySlot(p.slot)) == p;
            gui.setSlot(slots[i], new GuiElementBuilder(p.base)
                    .setName(Component.literal(p.display).withStyle(
                            wearing ? ChatFormatting.GOLD : ChatFormatting.DARK_GRAY,
                            ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§6" + p.partName))
                    .addLoreLine(Component.literal("§7" + p.partEffect))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7❈ Defense §a+" + p.defense))
                    .addLoreLine(Component.literal("§7❤ Health §c+" + p.health))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal(wearing ? "§aWorn" : "§8Not worn"))
                    .build());
        }

        boolean full = WarrantArmour.fullSet(player);
        gui.setSlot(31, new GuiElementBuilder(full ? Items.NETHER_STAR : Items.STRUCTURE_VOID)
                .setName(Component.literal("Levy").withStyle(
                        full ? ChatFormatting.GOLD : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Spend §f" + WarrantArmour.REQUISITION_COST
                        + " §7charge on a posting and"))
                .addLoreLine(Component.literal("§7whatever it still needs is made up."))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Charge builds from every hand in,"))
                .addLoreLine(Component.literal("§7and from damage the plate absorbs."))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(full
                        ? "§aAll four worn. Click a posting to use it."
                        : "§cNeeds all four pieces worn."))
                .build());

        GuiKit.footer(gui, SIZE, player, () -> open(player));
        gui.open();
    }
}
