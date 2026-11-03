package com.political;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.political.settlement.Settlement;
import com.political.settlement.SettlementEconomyGui;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

/**
 * The Almanac — the seasonal clock, what the current season is doing, and what is coming.
 *
 * <p>Everything the season system exposes is readable here; nothing needs a command.
 * Operators get an extra row of controls, gated on {@link SpawnProtectionManager#isOperator}.</p>
 */
public final class SeasonGui {
    private static final Logger LOGGER = LoggerFactory.getLogger(SeasonGui.class);


    private SeasonGui() {}

    private static final int[] SEASON_SLOTS = {20, 21, 23, 24};

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("The Almanac").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        LivingWorldGui.fill(gui, 54);

        Season now = SeasonManager.current();
        boolean op = isOperator(player);

        // ── Header: the season we are in ──
        GuiElementBuilder header = new GuiElementBuilder(now.icon)
                .setName(Component.literal(now.symbol + " " + now.displayName)
                        .withStyle(now.color, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7" + now.descriptionLine1))
                .addLoreLine(Component.literal("§7" + now.descriptionLine2))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Crop growth: " + fmtMult(now.cropGrowth)))
                .addLoreLine(Component.literal("§7Settlement food: " + fmtMult(now.foodMultiplier)))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("  " + SeasonManager.progressBar()))
                .addLoreLine(Component.literal("§7Turns in: §f" + SeasonManager.formatRemaining()))
                .addLoreLine(Component.literal("§7Year: §f" + (SeasonManager.yearsElapsed() + 1)))
                .glow();
        if (!SeasonManager.isEnabled()) {
            header.addLoreLine(Component.literal(""));
            header.addLoreLine(Component.literal("§c§lSEASONS ARE DISABLED"));
            header.addLoreLine(Component.literal("§8All multipliers read 1.00x."));
        }
        gui.setSlot(4, header.build());

        // ── The wheel of the year ──
        Season[] all = Season.values();
        for (int i = 0; i < all.length && i < SEASON_SLOTS.length; i++) {
            Season s = all[i];
            boolean isNow = s == now;
            boolean isNext = s == now.next();

            GuiElementBuilder b = new GuiElementBuilder(s.icon)
                    .setName(Component.literal(s.symbol + " " + s.displayName)
                            .withStyle(s.color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7" + s.descriptionLine1))
                    .addLoreLine(Component.literal("§7" + s.descriptionLine2))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7Crop growth: " + fmtMult(s.cropGrowth)))
                    .addLoreLine(Component.literal("§7Settlement food: " + fmtMult(s.foodMultiplier)))
                    .addLoreLine(Component.literal(""));

            if (isNow) {
                b.addLoreLine(Component.literal("§a§lCURRENT SEASON"));
                b.addLoreLine(Component.literal("§7Ends in §f" + SeasonManager.formatRemaining()));
                b.glow();
            } else if (isNext) {
                b.addLoreLine(Component.literal("§e§lNEXT"));
                b.addLoreLine(Component.literal("§7Begins in §f" + SeasonManager.formatRemaining()));
            } else {
                b.addLoreLine(Component.literal("§8" + seasonsAway(now, s) + " seasons away"));
            }

            if (op) {
                b.addLoreLine(Component.literal(""));
                b.addLoreLine(Component.literal("§8[Operator] Click to force this season."));
                final Season target = s;
                b.setCallback((a, c, d, e) -> {
                    SeasonManager.setSeason(((net.minecraft.server.level.ServerLevel) player.level()).getServer(), target, true);
                    open(player);
                });
            }
            gui.setSlot(SEASON_SLOTS[i], b.build());
        }

        // ── What seasons actually do ──
        gui.setSlot(30, new GuiElementBuilder(Items.WHEAT_SEEDS)
                .setName(Component.literal("Crops").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Seasons scale how often planted crops"))
                .addLoreLine(Component.literal("§7take a growth step."))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Right now: " + fmtMult(SeasonManager.cropGrowthMultiplier())))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§8Winter genuinely slows farming. Plant"))
                .addLoreLine(Component.literal("§8in spring, store through autumn."))
                .build());

        gui.setSlot(31, new GuiElementBuilder(Items.BREAD)
                .setName(Component.literal("Settlement Food").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Seasons scale the FOOD figure your"))
                .addLoreLine(Component.literal("§7settlement produces each cycle."))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Right now: " + fmtMult(SeasonManager.foodMultiplier())))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§8Food below population means starvation,"))
                .addLoreLine(Component.literal("§8falling happiness and stalled growth."))
                .setCallback((a, b, c, d) -> {
                    Settlement s = CivilizationHubGui.currentOrHome(player);
                    if (s != null) SettlementEconomyGui.open(player, s);
                    else player.sendSystemMessage(Component.literal("§7No settlement here."));
                })
                .build());

        gui.setSlot(32, new GuiElementBuilder(Items.ZOMBIE_HEAD)
                .setName(Component.literal("Wildlife").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7A fraction of hostile mobs take on"))
                .addLoreLine(Component.literal("§7the character of the season."))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Spring: §fbounding"))
                .addLoreLine(Component.literal("§7Summer: §funburnt"))
                .addLoreLine(Component.literal("§7Autumn: §fthick-hided"))
                .addLoreLine(Component.literal("§7Winter: §fhungry and fast"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§8Flavour only — never a difficulty wall."))
                .build());

        // ── Operator controls ──
        if (op) {
            gui.setSlot(46, new GuiElementBuilder(Items.CLOCK)
                    .setName(Component.literal("Advance Season")
                            .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Skip straight to §f" + now.next().displayName + "§7."))
                    .addLoreLine(Component.literal("§8[Operator]"))
                    .setCallback((a, b, c, d) -> {
                        SeasonManager.advance(((net.minecraft.server.level.ServerLevel) player.level()).getServer());
                        open(player);
                    })
                    .build());

            gui.setSlot(47, new GuiElementBuilder(SeasonManager.isEnabled()
                    ? Items.DYE.lime() : Items.DYE.gray())
                    .setName(Component.literal(SeasonManager.isEnabled()
                            ? "Seasons: ENABLED" : "Seasons: DISABLED")
                            .withStyle(SeasonManager.isEnabled() ? ChatFormatting.GREEN : ChatFormatting.RED,
                                    ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Click to toggle. While disabled every"))
                    .addLoreLine(Component.literal("§7seasonal multiplier reads 1.00x."))
                    .addLoreLine(Component.literal("§8[Operator]"))
                    .setCallback((a, b, c, d) -> {
                        SeasonManager.setEnabled(!SeasonManager.isEnabled());
                        open(player);
                    })
                    .build());

            gui.setSlot(48, new GuiElementBuilder(Items.REPEATER)
                    .setName(Component.literal("Season Length")
                            .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Current: §f"
                            + PoliticalServer.formatTime(SeasonManager.seasonLengthMs())))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§eLeft-click §7+30 minutes"))
                    .addLoreLine(Component.literal("§eRight-click §7-30 minutes"))
                    .addLoreLine(Component.literal("§8[Operator]"))
                    .setCallback((index, clickType, action, slotGui) -> {
                        long delta = clickType.isRight ? -30L * 60_000L : 30L * 60_000L;
                        SeasonManager.setSeasonLengthMs(SeasonManager.seasonLengthMs() + delta);
                        open(player);
                    })
                    .build());
        }

        gui.setSlot(49, LivingWorldGui.back(() -> LivingWorldGui.open(player)));
        gui.open();
    }

    private static int seasonsAway(Season from, Season to) {
        int diff = to.ordinal() - from.ordinal();
        if (diff < 0) diff += Season.values().length;
        return diff;
    }

    /** "§a+35%" / "§c-45%" / "§7±0%" with the raw multiplier appended. */
    static String fmtMult(double mult) {
        long pct = Math.round(mult * 100.0) - 100;
        String colour = pct > 0 ? "§a+" : pct < 0 ? "§c" : "§7±";
        return colour + pct + "% §8(" + String.format(java.util.Locale.ROOT, "%.2f", mult) + "x)";
    }

    static boolean isOperator(ServerPlayer player) {
        try {
            return SpawnProtectionManager.isOperator(player);
        } catch (Throwable t) {
            LOGGER.debug("SeasonGui.isOperator: ignored an error", t);
            return false;
        }
    }
}
