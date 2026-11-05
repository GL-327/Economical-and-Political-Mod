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
 * The Bestiary browser.
 *
 * <p>Two levels: the family index (all ten families with progress bars and the bonus each is
 * currently paying) and a per-family page listing every entity type in that family with the
 * player's kill count. The per-family page is paginated, so adding mob types later cannot
 * overflow the screen.</p>
 */
public final class BestiaryGui {

    private BestiaryGui() {}

    private static final int[] FAMILY_SLOTS = {10, 11, 12, 13, 14, 19, 20, 21, 22, 23};
    private static final int[] ENTRY_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    // ── Index ───────────────────────────────────────────────────────────────

    /** Where Back leads from this screen: the menu that opened it; the Living World when none passed one. */
    private static final com.political.core.gui.ReturnTo FROM = new com.political.core.gui.ReturnTo();

    /** Opens the screen from a parent menu (Back returns there), or with {@code null} from a command (Back goes to the Living World). */
    public static void openFrom(ServerPlayer player, Runnable back) {
        FROM.set(player.getUUID(), back);
        open(player);
    }

    public static void open(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Bestiary").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD));
        LivingWorldGui.fill(gui, 54);

        int completed = BestiaryManager.completedFamilies(uuid);
        double scholar = BestiaryManager.scholarBonus(uuid);

        gui.setSlot(4, new GuiElementBuilder(Items.WRITTEN_BOOK)
                .setName(Component.literal(player.getName().getString() + "'s Bestiary")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Qualifying kills: §f"
                        + BestiaryManager.totalKills(uuid)))
                .addLoreLine(Component.literal("§7Families at Authority: §f" + completed
                        + "§7/§f" + BestiaryEntry.MobFamily.values().length))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Scholar of the Wild: §a+"
                        + Math.round(scholar * 100) + "% §8(all families)"))
                .addLoreLine(Component.literal("§8Total bonus is capped at §7+"
                        + Math.round(BestiaryManager.MAX_TOTAL_BONUS * 100) + "%§8."))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§8Only kills you personally did most of"))
                .addLoreLine(Component.literal("§8the damage on are counted."))
                .glow().build());

        BestiaryEntry.MobFamily[] families = BestiaryEntry.MobFamily.values();
        for (int i = 0; i < families.length && i < FAMILY_SLOTS.length; i++) {
            BestiaryEntry.MobFamily family = families[i];
            gui.setSlot(FAMILY_SLOTS[i], familyIcon(player, uuid, family));
        }

        // ── Legend ──
        gui.setSlot(40, new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
                .setName(Component.literal("Milestones").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Each family rewards four milestones:"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7  " + BestiaryManager.MILESTONES[0]
                        + " kills §8→ §a+" + Math.round(BestiaryManager.MILESTONE_BONUS[0] * 100)
                        + "% §8(" + BestiaryManager.MILESTONE_TITLE[0] + ")"))
                .addLoreLine(Component.literal("§7  " + BestiaryManager.MILESTONES[1]
                        + " kills §8→ §a+" + Math.round(BestiaryManager.MILESTONE_BONUS[1] * 100)
                        + "% §8(" + BestiaryManager.MILESTONE_TITLE[1] + ")"))
                .addLoreLine(Component.literal("§7  " + BestiaryManager.MILESTONES[2]
                        + " kills §8→ §a+" + Math.round(BestiaryManager.MILESTONE_BONUS[2] * 100)
                        + "% §8(" + BestiaryManager.MILESTONE_TITLE[2] + ")"))
                .addLoreLine(Component.literal("§7  " + BestiaryManager.MILESTONES[3]
                        + " kills §8→ §a+" + Math.round(BestiaryManager.MILESTONE_BONUS[3] * 100)
                        + "% §8(" + BestiaryManager.MILESTONE_TITLE[3] + ")"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Milestones do not stack within a family —"))
                .addLoreLine(Component.literal("§7the highest one reached applies."))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§8This supplements gear. It never replaces it."))
                .build());

        gui.setSlot(49, LivingWorldGui.back(FROM.getOr(player.getUUID(), () -> LivingWorldGui.open(player))));
        gui.open();
    }

    private static eu.pb4.sgui.api.elements.GuiElement familyIcon(ServerPlayer player, String uuid,
                                                                  BestiaryEntry.MobFamily family) {
        int kills = BestiaryManager.kills(uuid, family);
        BestiaryEntry entry = BestiaryManager.entry(uuid, family);
        int milestone = entry == null ? 0 : entry.milestone;
        int next = BestiaryManager.nextThreshold(uuid, family);
        double bonus = BestiaryManager.familyBonus(uuid, family);

        GuiElementBuilder b = new GuiElementBuilder(family.icon)
                .setName(Component.literal(family.symbol + " " + family.displayName)
                        .withStyle(family.color, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7" + family.description))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Kills: §f" + kills))
                .addLoreLine(Component.literal("  " + BestiaryManager.progressBar(uuid, family)));

        if (next > 0) {
            b.addLoreLine(Component.literal("§7Next milestone at §f" + next
                    + " §8(" + (next - kills) + " to go)"));
        } else {
            b.addLoreLine(Component.literal("§6§lMASTERED"));
        }

        b.addLoreLine(Component.literal(""));
        if (milestone > 0) {
            b.addLoreLine(Component.literal("§7Rank: §e"
                    + BestiaryManager.MILESTONE_TITLE[Math.min(milestone, 4) - 1]));
            b.addLoreLine(Component.literal("§7Damage vs family: §a+"
                    + Math.round(bonus * 100) + "%"));
            b.glow();
        } else {
            b.addLoreLine(Component.literal("§8No milestone reached yet."));
        }

        if (entry != null && entry.distinctTypes() > 0) {
            b.addLoreLine(Component.literal("§7Types recorded: §f" + entry.distinctTypes()
                    + "§7/§f" + BestiaryManager.typesIn(family).size()));
        }
        b.addLoreLine(Component.literal(""));
        b.addLoreLine(Component.literal("§eClick for the full page"));
        b.setCallback((a, c, d, e) -> openFamily(player, family, 0));
        return b.build();
    }

    // ── Family page ─────────────────────────────────────────────────────────

    public static void openFamily(ServerPlayer player, BestiaryEntry.MobFamily family, int page) {
        String uuid = player.getUUID().toString();
        List<String> types = BestiaryManager.typesIn(family);

        int perPage = ENTRY_SLOTS.length;
        int pages = Math.max(1, (int) Math.ceil(types.size() / (double) perPage));
        if (page < 0) page = 0;
        if (page >= pages) page = pages - 1;
        final int shownPage = page;

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal(family.displayName).withStyle(family.color, ChatFormatting.BOLD));
        LivingWorldGui.fill(gui, 54);

        BestiaryEntry entry = BestiaryManager.entry(uuid, family);
        int kills = entry == null ? 0 : entry.kills;

        gui.setSlot(4, new GuiElementBuilder(family.icon)
                .setName(Component.literal(family.symbol + " " + family.displayName)
                        .withStyle(family.color, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7" + family.description))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Kills: §f" + kills))
                .addLoreLine(Component.literal("  " + BestiaryManager.progressBar(uuid, family)))
                .addLoreLine(Component.literal("§7Damage bonus: §a+"
                        + Math.round(BestiaryManager.familyBonus(uuid, family) * 100) + "%"))
                .glow().build());

        int start = shownPage * perPage;
        for (int i = 0; i < perPage && start + i < types.size(); i++) {
            String path = types.get(start + i);
            int typeKills = entry == null ? 0 : entry.killsOfType(path);
            boolean discovered = typeKills > 0;

            GuiElementBuilder b = new GuiElementBuilder(discovered ? family.icon : Items.DYE.gray())
                    .setName(Component.literal((discovered ? "" : "§8") + BestiaryManager.prettyType(path))
                            .withStyle(discovered ? family.color : ChatFormatting.DARK_GRAY,
                                    ChatFormatting.BOLD));

            if (discovered) {
                b.addLoreLine(Component.literal("§7Slain: §f" + typeKills));
                b.addLoreLine(Component.literal("§8Counts toward " + family.displayName + "."));
            } else {
                b.addLoreLine(Component.literal("§8Never recorded."));
                b.addLoreLine(Component.literal("§8Kill one to open this entry."));
            }
            gui.setSlot(ENTRY_SLOTS[i], b.build());
        }

        // ── Pagination ──
        if (shownPage > 0) {
            gui.setSlot(48, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("← Previous Page").withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal("§7Page " + shownPage + " of " + pages))
                    .setCallback((a, c, d, e) -> openFamily(player, family, shownPage - 1))
                    .build());
        }
        if (shownPage < pages - 1) {
            gui.setSlot(50, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Next Page →").withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal("§7Page " + (shownPage + 2) + " of " + pages))
                    .setCallback((a, c, d, e) -> openFamily(player, family, shownPage + 1))
                    .build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("← Back to Bestiary").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("§7Page " + (shownPage + 1) + " of " + pages))
                .setCallback((a, c, d, e) -> open(player))
                .build());

        gui.open();
    }
}
