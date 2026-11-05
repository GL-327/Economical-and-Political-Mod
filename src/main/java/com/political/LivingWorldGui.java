package com.political;


import com.political.recipe.RecipesGui;
import com.political.settlement.WarGui;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

/**
 * Landing screen for the Living World pillar: seasons, bestiary and hunts in one place.
 *
 * <p>This is the screen the Civilization Hub button should point at. It also carries the
 * shared GUI helpers ({@link #fill}, {@link #back}) that the three sub-screens use, so the
 * pillar does not depend on {@code WarGui}'s package-private helpers.</p>
 */
public final class LivingWorldGui {

    private LivingWorldGui() {}

    public static void open(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("The Living World")
                .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        fill(gui, 54);

        Season season = SeasonManager.current();
        HuntManager.ActiveHunt hunt = HuntManager.active();

        gui.setSlot(4, new GuiElementBuilder(Items.CLOCK)
                .setName(Component.literal("The Living World")
                        .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7The world turns whether or not"))
                .addLoreLine(Component.literal("§7anyone is watching it."))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Season: " + season.label()))
                .addLoreLine(Component.literal("§7Hunt: " + (hunt == null
                        ? "§8none active" : "§c" + describeHunt(hunt))))
                .addLoreLine(Component.literal("§7Your kills: §f" + BestiaryManager.totalKills(uuid)))
                .glow().build());

        // ── Seasons ──
        gui.setSlot(20, new GuiElementBuilder(season.icon)
                .setName(Component.literal("Seasons").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7A four-season year that changes"))
                .addLoreLine(Component.literal("§7crop growth and settlement food."))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Currently: " + season.label()))
                .addLoreLine(Component.literal("§7Turns in: §f" + SeasonManager.formatRemaining()))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to open the Almanac"))
                .setCallback((a, b, c, d) -> SeasonGui.openFrom(player, null))
                .build());

        // ── Bestiary ──
        int completed = BestiaryManager.completedFamilies(uuid);
        gui.setSlot(22, new GuiElementBuilder(Items.WRITTEN_BOOK)
                .setName(Component.literal("Bestiary").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Every mob you have genuinely fought,"))
                .addLoreLine(Component.literal("§7counted and filed by family."))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Qualifying kills: §f" + BestiaryManager.totalKills(uuid)))
                .addLoreLine(Component.literal("§7Families mastered: §f" + completed
                        + "§7/§f" + BestiaryEntry.MobFamily.values().length))
                .addLoreLine(Component.literal("§7Scholar bonus: §a+"
                        + Math.round(BestiaryManager.scholarBonus(uuid) * 100) + "%"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to open the Bestiary"))
                .setCallback((a, b, c, d) -> BestiaryGui.openFrom(player, null))
                .build());

        // ── Hunts ──
        GuiElementBuilder huntButton = new GuiElementBuilder(Items.TARGET)
                .setName(Component.literal("The Great Hunt")
                        .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7A named, buffed quarry walks the"))
                .addLoreLine(Component.literal("§7world on a timer. First to fell it"))
                .addLoreLine(Component.literal("§7takes the purse and a trophy."))
                .addLoreLine(Component.literal(""));
        if (hunt != null) {
            huntButton.addLoreLine(Component.literal("§c§lHUNT ACTIVE"));
            huntButton.addLoreLine(Component.literal("§7" + describeHunt(hunt)));
            huntButton.addLoreLine(Component.literal("§7Near: §f" + HuntManager.locationHint()));
            huntButton.glow();
        } else {
            huntButton.addLoreLine(Component.literal("§7Next hunt in: §f"
                    + PoliticalServer.formatTime(HuntManager.msUntilNextHunt())));
        }
        huntButton.addLoreLine(Component.literal("§7Purse: §6"
                + HuntManager.fmt(HuntManager.currentReward()) + " coins"));
        huntButton.addLoreLine(Component.literal(""));
        huntButton.addLoreLine(Component.literal("§eClick to open the Hunt Board"));
        huntButton.setCallback((a, b, c, d) -> HuntGui.openFrom(player, null));
        gui.setSlot(24, huntButton.build());

        // ── How to carry these around ──
        gui.setSlot(40, new GuiElementBuilder(Items.BOOK)
                .setName(Component.literal("Almanac, Codex and Horn")
                        .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Three craftable keepsakes that open"))
                .addLoreLine(Component.literal("§7these screens with a right-click."))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7• §fAlmanac of Seasons §8— book + wheat + sapling"))
                .addLoreLine(Component.literal("§7• §fBestiary Codex §8— book + bone + flesh"))
                .addLoreLine(Component.literal("§7• §fHunter's Horn §8— goat horn + gold"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to browse every recipe"))
                .setCallback((a, b, c, d) -> RecipesGui.openMainMenu(player))
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("← Civilization Hub").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("§7Back to the master menu."))
                .setCallback((a, b, c, d) -> com.political.core.gui.MenuTrail.backOr(player, () -> CivilizationHubGui.open(player)).run())
                .build());

        gui.open();
    }

    private static String describeHunt(HuntManager.ActiveHunt hunt) {
        HuntManager.Archetype arch = hunt.archetype();
        if (arch == null) return "an unnamed quarry";
        return arch.title() + ", " + arch.epithet();
    }

    // ── Shared helpers for the pillar's screens ─────────────────────────────

    /** Fills every slot with a blank pane so unused slots are not click-through. */
    static void fill(SimpleGui gui, int size) {
        GuiKit.frame(gui, size);
    }

    /** Standard back arrow. */
    static GuiElement back(Runnable r) {
        return new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((a, b, c, d) -> r.run())
                .build();
    }
}
