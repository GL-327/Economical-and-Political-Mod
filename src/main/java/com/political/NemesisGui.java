package com.political;

import com.political.core.gui.ReturnTo;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * The grudge list.
 *
 * <p>Two screens. The list is everything currently hunting you, and the dossier is one of them in
 * full: rank, traits with what each actually does, the tally of who has killed whom, and where it was
 * last seen. The dossier matters more than it looks, because walking into a Nightmare carrying
 * Undying and Thorned without knowing is the difference between a fight and a funeral.</p>
 */
public final class NemesisGui {

    private NemesisGui() {}

    private static final int SIZE = 54;

    // ── The list ──────────────────────────────────────────────────────────

    /** Where Back leads from this screen's first page: the menu that opened it, remembered per player. */
    private static final ReturnTo FROM = new ReturnTo();

    /** Opens the screen from a parent menu (Back returns there), or with {@code null} from a command (no Back). */
    public static void openFrom(ServerPlayer player, Runnable back) {
        FROM.set(player.getUUID(), back);
        open(player);
    }

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Grudges")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE, GuiKit.DANGER_EDGE);

        List<Nemesis> mine = NemesisManager.forPlayer(player);
        int owed = 0;
        long bounties = 0;
        for (Nemesis n : mine) {
            owed += n.kills;
            bounties += n.bounty();
        }

        GuiKit.header(gui, Items.WITHER_SKELETON_SKULL, "Grudges", ChatFormatting.DARK_RED,
                mine.isEmpty() ? "Nothing is hunting you yet"
                        : mine.size() + " hunting you",
                owed + " deaths owed",
                bounties > 0 ? GuiKit.num(bounties) + " coins in bounties" : "");

        if (mine.isEmpty()) {
            gui.setSlot(31, GuiKit.info(Items.FEATHER, "Nothing yet", ChatFormatting.GRAY,
                    "A mob that kills you does not stay",
                    "an ordinary mob. It takes a name,",
                    "rolls traits, and comes looking.",
                    "",
                    "Kill it and the grudge is settled.",
                    "Lose again and it climbs a rank."));
        }

        List<GuiElement> entries = new ArrayList<>();
        for (Nemesis n : mine) {
            GuiElementBuilder b = new GuiElementBuilder(iconFor(n))
                    .setName(Component.literal(n.fullName())
                            .withStyle(n.rank().colour, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7" + n.rank().colour + n.rank().label))
                    .addLoreLine(Component.literal("§7Killed you §c" + n.kills
                            + (n.kills == 1 ? " §7time" : " §7times")));
            if (n.deaths > 0) {
                b.addLoreLine(Component.literal("§7Put down §a" + n.deaths
                        + (n.deaths == 1 ? " §7time" : " §7times")));
            }
            if (n.scars > 0) {
                b.addLoreLine(Component.literal("§7Escaped you §c" + n.scars
                        + (n.scars == 1 ? " §7time" : " §7times")));
            }
            b.addLoreLine(Component.literal(""));
            StringBuilder tr = new StringBuilder();
            for (NemesisTrait t : n.traitList()) {
                tr.append(t.colour).append(t.display).append("§8, ");
            }
            String traits = tr.length() > 2 ? tr.substring(0, tr.length() - 4) : "§8none";
            b.addLoreLine(Component.literal("§8" + traits));
            b.addLoreLine(Component.literal(""));
            b.addLoreLine(Component.literal(n.active
                    ? "§c● Out there right now" : "§8● Waiting"));
            b.addLoreLine(Component.literal("§7Bounty §6" + GuiKit.num(n.bounty()) + " coins"));
            b.addLoreLine(Component.literal(""));
            b.addLoreLine(Component.literal("§eClick for the dossier"));
            if (n.rank().isTop()) b.glow();

            entries.add(b.setCallback((a, t, ci, sg) -> openDossier(player, n)).build());
        }

        GuiKit.paint(gui, entries, 0, GuiKit.LIST_SLOTS.length);
        GuiKit.footer(gui, SIZE, player, FROM.get(player.getUUID()));
        gui.open();
    }

    // ── One nemesis ───────────────────────────────────────────────────────

    private static void openDossier(ServerPlayer player, Nemesis n) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal(n.fullName())
                .withStyle(n.rank().colour, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE, GuiKit.DANGER_EDGE);

        GuiKit.header(gui, iconFor(n), n.fullName(), n.rank().colour,
                n.rank().label,
                "It was " + shortType(n) + " once");

        gui.setSlot(19, GuiKit.info(Items.IRON_SWORD, "The tally", ChatFormatting.WHITE,
                "Killed you " + n.kills + (n.kills == 1 ? " time" : " times"),
                "You put it down " + n.deaths + (n.deaths == 1 ? " time" : " times"),
                "It ran " + n.scars + (n.scars == 1 ? " time" : " times")));

        gui.setSlot(20, GuiKit.info(Items.REDSTONE, "What it is now", ChatFormatting.RED,
                String.format("%.0f%% more health than its kind", (n.healthMultiplier() - 1) * 100),
                String.format("%.0f%% more damage", (n.damageMultiplier() - 1) * 100),
                n.scars > 0 ? "Each escape made it worse" : "No scars yet"));

        gui.setSlot(21, GuiKit.info(Items.GOLD_INGOT, "Bounty", ChatFormatting.GOLD,
                GuiKit.num(n.bounty()) + " coins",
                "Paid the moment it stops moving"));

        gui.setSlot(22, GuiKit.info(Items.COMPASS, "Last seen", ChatFormatting.AQUA,
                n.lastX + ", " + n.lastY + ", " + n.lastZ,
                n.lastDim.replace("minecraft:", ""),
                n.active ? "§cIt is out there right now" : "§8Waiting for its moment"));

        // Every trait gets its own slot with the effect written out, because this is the screen a
        // player reads before deciding whether to go looking for it.
        List<NemesisTrait> traits = n.traitList();
        int[] slots = {28, 29, 30, 31, 32, 33, 34};
        for (int i = 0; i < traits.size() && i < slots.length; i++) {
            NemesisTrait t = traits.get(i);
            gui.setSlot(slots[i], new GuiElementBuilder(traitIcon(t))
                    .setName(Component.literal(t.display).withStyle(t.colour, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7" + t.effect))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§8" + t.flavor))
                    .build());
        }

        gui.setSlot(40, GuiKit.info(Items.WRITABLE_BOOK, "How this ends", ChatFormatting.LIGHT_PURPLE,
                "Kill it and the grudge is settled for good.",
                "Lose and it takes another rank and another trait.",
                n.has(NemesisTrait.CUNNING)
                        ? "§cThis one runs. Expect to do it twice."
                        : "It does not know how to run."));

        GuiKit.footer(gui, SIZE, player, () -> open(player));
        gui.open();
    }

    // ── Presentation ──────────────────────────────────────────────────────

    private static net.minecraft.world.item.Item iconFor(Nemesis n) {
        String t = n.entityType;
        if (t.contains("skeleton")) return Items.SKELETON_SKULL;
        if (t.contains("wither")) return Items.WITHER_SKELETON_SKULL;
        if (t.contains("creeper")) return Items.CREEPER_HEAD;
        if (t.contains("zombie") || t.contains("husk") || t.contains("drowned")) return Items.ZOMBIE_HEAD;
        if (t.contains("spider")) return Items.SPIDER_EYE;
        if (t.contains("enderman")) return Items.ENDER_PEARL;
        if (t.contains("blaze")) return Items.BLAZE_ROD;
        if (t.contains("piglin")) return Items.PIGLIN_HEAD;
        if (t.contains("ghast")) return Items.GHAST_TEAR;
        if (t.contains("witch")) return Items.POTION;
        if (t.contains("pillager") || t.contains("vindicator") || t.contains("evoker")) return Items.CROSSBOW;
        if (t.contains("slime") || t.contains("magma")) return Items.SLIME_BALL;
        if (t.contains("phantom")) return Items.PHANTOM_MEMBRANE;
        return Items.ZOMBIE_HEAD;
    }

    private static String shortType(Nemesis n) {
        String t = n.entityType.replace("minecraft:", "").replace('_', ' ');
        return "a " + t;
    }

    private static net.minecraft.world.item.Item traitIcon(NemesisTrait t) {
        return switch (t.kind) {
            case DEFENSIVE -> Items.SHIELD;
            case OFFENSIVE -> Items.IRON_SWORD;
            case MOBILITY -> Items.FEATHER;
            case SUPPORT -> Items.BONE;
        };
    }
}
