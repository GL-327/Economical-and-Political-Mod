package com.political.power.game;

import com.political.core.gui.Elements;
import com.political.core.gui.Icon;
import com.political.core.gui.Palette;
import com.political.core.gui.Screen;
import com.political.core.gui.UiSound;
import com.political.core.text.Text;
import com.political.power.PowerSettings;
import com.political.power.formula.Innate;
import com.political.power.formula.Mastery;
import com.political.power.formula.Reroll;
import com.political.power.formula.Vow;
import com.political.power.formula.VowSet;
import com.political.power.model.AbilityKind;
import com.political.power.model.AbilitySpec;
import com.political.power.model.PlayerPower;
import com.political.power.model.PowerCatalog;
import com.political.technique.Technique;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * The player's screens for the power system (STYLE.md): the hub, techniques and abilities, the school choice,
 * the school tree with its next step and cost, and binding vows. Every action goes through {@link PowerService},
 * which owns the rules; a screen only shows what is true now and reports what the service answered.
 */
public final class PowerScreens {

    private static final int ROWS = 6;

    private PowerScreens() {
    }

    // ── helpers ─────────────────────────────────────────────────────────────────────────────

    private static String uuid(ServerPlayer player) {
        return player.getUUID().toString();
    }

    /** Seconds with one decimal for a number of ticks. */
    private static String seconds(long ticks) {
        return String.format(Locale.ROOT, "%.1f", ticks / 20.0);
    }

    private static String percent(double fraction) {
        return String.valueOf(Math.round(fraction * 100.0));
    }

    /** A list entry or tile with a name, lore under a blank line, an optional glow and an optional click. */
    private static GuiElement entry(Item icon, Component name, Palette color, boolean glow, Runnable onClick, Component prompt,
                                    List<Component> lore) {
        GuiElementBuilder b = new GuiElementBuilder(icon).setName(Elements.name(name, color));
        if (glow) {
            b.glow();
        }
        if (!lore.isEmpty()) {
            b.addLoreLine(Component.empty());
            for (Component line : lore) {
                // a line that already carries a color keeps it; every other line is muted
                b.addLoreLine(line.getStyle().getColor() == null ? Elements.lore(line) : line);
            }
        }
        if (onClick != null) {
            b.addLoreLine(Component.empty());
            b.addLoreLine(Elements.lore(prompt, Palette.WARN));
            b.setCallback((a, t, c, d) -> onClick.run());
        }
        return b.build();
    }

    // ── hub ─────────────────────────────────────────────────────────────────────────────────

    public static void open(ServerPlayer player) {
        String uuid = uuid(player);
        PlayerPower p = PowerService.state(uuid);
        boolean awake = PowerService.awakened(uuid);
        int tier = PowerService.tier(uuid);

        Screen screen = Screen.chest(player, ROWS, Text.tr("political.power.gui.title")).frame(Screen.Edge.NORMAL)
                .header(Items.ENCHANTED_BOOK, Text.tr("political.power.gui.title"),
                        Text.tr(awake ? "political.power.gui.state.awake" : "political.power.gui.state.asleep"),
                        Text.tr("political.power.gui.shards", p.shards));

        // Where to go
        screen.set(20, yourTechniqueTile(player, uuid, p, awake));
        screen.set(22, awake
                ? Elements.info(Items.BEACON, Text.tr("political.power.gui.awakening"), Palette.GOOD, Text.tr("political.power.gui.awakening.done"))
                : Elements.info(Items.DRAGON_EGG, Text.tr("political.power.gui.awakening"), Palette.WARN,
                        Text.trNamed("political.power.awaken.how", PowerSettings.AWAKENING_BOSS.get()), Text.tr("political.power.gui.awakening.why")));
        screen.set(24, Elements.tile(Items.AMETHYST_SHARD, Text.tr("political.power.gui.reroll"), Palette.ACCENT,
                () -> openReroll(player), Text.tr("political.power.gui.reroll.about"),
                Text.tr("political.power.gui.reroll.charges", p.rerollCharges)));
        screen.set(28, Elements.info(Items.EXPERIENCE_BOTTLE, Text.tr("political.power.gui.mastery"), Palette.BODY, masteryLines(uuid, p, tier)));
        screen.set(30, Elements.tile(Items.BOOKSHELF, Text.tr("political.power.gui.techniques"), Palette.ACCENT,
                () -> openTechniques(player, 0), Text.tr("political.power.gui.techniques.about")));
        screen.set(32, Elements.tile(Items.WRITABLE_BOOK, Text.tr("political.power.gui.vows"), Palette.ACCENT,
                () -> openVows(player), Text.tr("political.power.gui.vows.about"),
                Text.tr("political.power.gui.vows.active", PowerService.vowsOf(uuid).active().size(), PowerSettings.VOW_MAX_ACTIVE.get())));
        screen.set(34, Elements.info(Items.PAPER, Text.tr("political.power.gui.cast"), Palette.WARN,
                Text.tr("political.power.gui.cast.how"), Text.tr("political.power.gui.cast.what")));

        // Where you stand
        screen.set(40, Elements.progress(Items.LAPIS_LAZULI, Text.tr("political.power.gui.energy"), Palette.ACCENT,
                energyFraction(uuid), Text.tr("political.power.gui.energy.amount", (int) PowerService.resource(uuid), (int) PowerService.maxResource(uuid))));

        screen.footer(null).open();
    }

    private static double energyFraction(String uuid) {
        double max = PowerService.maxResource(uuid);
        return max <= 0 ? 0.0 : Math.max(0.0, Math.min(1.0, PowerService.resource(uuid) / max));
    }

    private static GuiElement yourTechniqueTile(ServerPlayer player, String uuid, PlayerPower p, boolean awake) {
        if (p.innate == null) {
            return Elements.info(Items.BOOK, Text.tr("political.power.gui.yours"), Palette.FAINT, Text.tr("political.power.gui.yours.none"));
        }
        Component name = PowerService.techniqueName(p.innate);
        List<Component> lines = new ArrayList<>();
        lines.add(name);
        lines.add(Text.tr(awake ? "political.power.gui.state.awake" : "political.power.gui.state.asleep"));
        AbilitySpec selected = PowerService.selected(uuid);
        if (selected != null && p.innate.equals(p.equipped)) {
            lines.add(Text.tr("political.power.gui.techniques.selected", abilityName(selected)));
        }
        if (awake) {
            return Elements.tile(Items.ENCHANTED_BOOK, Text.tr("political.power.gui.yours"), Palette.TITLE, () -> openAbilities(player),
                    lines.toArray(new Component[0]));
        }
        return Elements.info(Items.ENCHANTED_BOOK, Text.tr("political.power.gui.yours"), Palette.TITLE, lines.toArray(new Component[0]));
    }

    private static Component[] masteryLines(String uuid, PlayerPower p, int tier) {
        List<Component> lines = new ArrayList<>();
        if (p.innate == null || tier == 0) {
            lines.add(Text.tr("political.power.gui.mastery.asleep"));
            return lines.toArray(new Component[0]);
        }
        int uses = p.uses.getOrDefault(p.innate, 0);
        lines.add(Text.plain(Mastery.rank(uses).display + "  (" + uses + ")"));
        int next = Mastery.usesToNextRank(uses);
        lines.add(next < 0 ? Text.tr("political.power.gui.mastery.max") : Text.tr("political.power.gui.mastery.next", next));
        if (tier >= 2) {
            lines.add(Text.tr("political.power.gui.mastery.bonus", percent(Mastery.damageBonus(tier)), percent(Mastery.cooldownReduction(tier))));
        }
        lines.add(Text.tr(Mastery.domainUnlocked(tier) ? "political.power.gui.mastery.domain.open" : "political.power.gui.mastery.domain.locked"));
        return lines.toArray(new Component[0]);
    }

    // ── reroll ──────────────────────────────────────────────────────────────────────────────

    public static void openReroll(ServerPlayer player) {
        String uuid = uuid(player);
        PlayerPower p = PowerService.state(uuid);
        int cost = PowerService.rerollShardCost(uuid);
        Screen screen = Screen.chest(player, ROWS, Text.tr("political.power.gui.reroll")).frame(Screen.Edge.DANGER)
                .header(Items.AMETHYST_SHARD, Text.tr("political.power.gui.reroll"), Text.tr("political.power.gui.reroll.warning"));

        boolean canPay = p.shards >= cost;
        screen.set(20, entry(Items.AMETHYST_SHARD, Text.tr("political.power.gui.reroll.shards"), canPay ? Palette.TITLE : Palette.FAINT, false,
                canPay ? () -> confirmReroll(player, false) : null, Text.tr("political.power.gui.reroll.click"),
                List.of(Text.tr("political.power.gui.reroll.cost", cost), Text.tr("political.power.gui.reroll.have", p.shards),
                        Text.tr("political.power.gui.reroll.random"))));
        boolean hasCharge = p.rerollCharges > 0;
        screen.set(22, entry(Items.NETHER_STAR, Text.tr("political.power.gui.reroll.charge"), hasCharge ? Palette.TITLE : Palette.FAINT, false,
                hasCharge ? () -> confirmReroll(player, true) : null, Text.tr("political.power.gui.reroll.click"),
                List.of(Text.tr("political.power.gui.reroll.charges", p.rerollCharges),
                        Text.trNamed("political.power.reroll.earn", PowerSettings.REROLL_CHARGE_BOSS.get()),
                        Text.tr("political.power.gui.reroll.random"))));
        screen.set(24, Elements.info(Items.WRITTEN_BOOK, Text.tr("political.power.gui.reroll.manual"), Palette.BODY,
                Text.tr("political.power.gui.reroll.manual.how"), Text.tr("political.power.gui.reroll.manual.what")));
        screen.footer(() -> open(player)).open();
    }

    private static void confirmReroll(ServerPlayer player, boolean withCharge) {
        Screen.confirm(player, Text.tr("political.power.gui.reroll.confirm"), Text.tr("political.power.gui.reroll.consequence"),
                () -> {
                    PowerService.RerollResult result = withCharge ? PowerService.rerollWithCharge(player) : PowerService.rerollWithShards(player);
                    if (result != PowerService.RerollResult.OK) {
                        String keep = PowerService.state(uuid(player)).innate;
                        player.sendSystemMessage(Text.trNamed("political.power.reroll.result", result.name(),
                                keep == null ? Text.plain("-") : PowerService.techniqueName(keep)), false);
                    }
                    (result == PowerService.RerollResult.OK ? UiSound.SUCCESS : UiSound.DENIED).play(player);
                    open(player);
                }, () -> openReroll(player));
    }

    // ── techniques and abilities ────────────────────────────────────────────────────────────

    public static void openTechniques(ServerPlayer player, int page) {
        String uuid = uuid(player);
        PlayerPower p = PowerService.state(uuid);
        List<GuiElement> entries = new ArrayList<>();
        for (PowerCatalog.TechniqueDef def : PowerCatalog.techniques()) {
            entries.add(techniqueEntry(player, uuid, p, def, page));
        }
        Screen.chest(player, ROWS, Text.tr("political.power.gui.techniques")).frame(Screen.Edge.NORMAL)
                .header(Items.ENCHANTED_BOOK, Text.tr("political.power.gui.techniques"), Text.tr("political.power.gui.techniques.list"))
                .list(entries, page, next -> openTechniques(player, next))
                .footer(() -> open(player))
                .open();
    }

    private static GuiElement techniqueEntry(ServerPlayer player, String uuid, PlayerPower p, PowerCatalog.TechniqueDef def, int page) {
        Component name = PowerService.techniqueName(def.id());
        boolean held = PowerService.holds(uuid, def.id());
        boolean equipped = def.id().equals(p.equipped);
        List<Component> lore = new ArrayList<>();
        if (def.legacy()) {
            Technique older = Technique.byName(def.id());
            if (older != null) {
                lore.add(Text.plain(older.flavor));
            }
        }
        for (AbilitySpec spec : PowerCatalog.abilities(def.id())) {
            lore.add(Text.plain("• ").append(abilityName(spec)));
        }
        lore.add(Component.empty());
        if (!held) {
            lore.add(Elements.lore(whereFrom(def), Palette.BAD));
            return entry(Items.BOOK, name, Palette.FAINT, false, null, null, lore);
        }
        if (def.legacy()) {
            int uses = p.uses.getOrDefault(def.id(), 0);
            lore.add(Elements.lore(Text.plain(PowerService.rank(uuid, def.id()).display + " (" + uses + ")"), Palette.BODY));
        }
        if (equipped) {
            lore.add(Elements.lore(Text.tr("political.power.gui.techniques.state.equipped"), Palette.GOOD));
            return entry(Items.ENCHANTED_BOOK, name, Palette.GOOD, true, () -> openAbilities(player), Text.tr("political.power.gui.techniques.click.abilities"), lore);
        }
        lore.add(Elements.lore(Text.tr("political.power.gui.techniques.state.known"), Palette.BODY));
        return entry(Items.ENCHANTED_BOOK, name, Palette.TITLE, false, () -> {
            if (PowerService.equip(player, def.id())) {
                player.sendSystemMessage(Text.tr("political.power.equip.done", name), false);
                UiSound.SUCCESS.play(player);
            } else {
                UiSound.DENIED.play(player);
            }
            openTechniques(player, page);
        }, Text.tr("political.power.gui.techniques.click.equip"), lore);
    }

    /** Why a technique the player does not hold is not theirs. */
    private static Component whereFrom(PowerCatalog.TechniqueDef def) {
        return Text.tr(PowerCatalog.DOMAIN.equals(def.id()) ? "political.power.gui.from.mastery" : "political.power.gui.from.roll");
    }

    private static Component abilityName(AbilitySpec spec) {
        if (spec.kind() == AbilityKind.LEGACY) {
            Technique technique = Technique.byName(spec.techniqueId());
            Technique.Ability ability = technique == null ? null : technique.abilityById(spec.id());
            return Text.plain(ability == null ? spec.id() : ability.name());
        }
        return Text.trNamed("political.power.ability", spec.id());
    }

    private static Component abilityAbout(AbilitySpec spec) {
        if (spec.kind() == AbilityKind.LEGACY) {
            Technique technique = Technique.byName(spec.techniqueId());
            Technique.Ability ability = technique == null ? null : technique.abilityById(spec.id());
            return Text.plain(ability == null ? "" : ability.description());
        }
        return Text.trNamed("political.power.ability.about", spec.id());
    }

    private static Item abilityIcon(AbilityKind kind) {
        return switch (kind) {
            case BLUE -> Items.LAPIS_LAZULI;
            case RED -> Items.REDSTONE;
            case BLAST -> Items.FIRE_CHARGE;
            case REVERSE -> Items.GLISTERING_MELON_SLICE;
            case BARRIER -> Items.SHIELD;
            case DOMAIN -> Items.END_CRYSTAL;
            case LEGACY -> Items.PAPER;
        };
    }

    public static void openAbilities(ServerPlayer player) {
        String uuid = uuid(player);
        PlayerPower p = PowerService.state(uuid);
        if (p.equipped == null) {
            openTechniques(player, 0);
            return;
        }
        AbilitySpec selected = PowerService.selected(uuid);
        List<GuiElement> entries = new ArrayList<>();
        for (AbilitySpec spec : PowerCatalog.abilities(p.equipped)) {
            entries.add(abilityEntry(player, uuid, spec, selected != null && selected.id().equals(spec.id())));
        }
        Screen.chest(player, ROWS, Text.tr("political.power.gui.abilities")).frame(Screen.Edge.NORMAL)
                .header(Items.ENCHANTED_BOOK, PowerService.techniqueName(p.equipped), Text.tr("political.power.gui.abilities.about"))
                .list(entries, 0, page -> openAbilities(player))
                .footer(() -> openTechniques(player, 0))
                .open();
    }

    private static GuiElement abilityEntry(ServerPlayer player, String uuid, AbilitySpec spec, boolean isSelected) {
        Component name = abilityName(spec);
        boolean unlocked = PowerService.unlocked(uuid, spec);
        List<Component> lore = new ArrayList<>();
        lore.add(abilityAbout(spec));
        lore.add(Component.empty());
        lore.add(Elements.lore(Text.tr("political.power.gui.abilities.cost", spec.kind() == AbilityKind.REVERSE
                ? Text.tr("political.power.gui.abilities.cost.reverse", PowerSettings.REVERSE_COST_PER_HP.get())
                : Text.plain(String.valueOf((int) Math.ceil(spec.cost())))), Palette.BODY));
        lore.add(Elements.lore(Text.tr("political.power.gui.abilities.cooldown", seconds(spec.cooldownTicks())), Palette.BODY));
        if (spec.range() > 0) {
            lore.add(Elements.lore(Text.tr("political.power.gui.abilities.range", (int) spec.range()), Palette.BODY));
        }
        long remaining = PowerService.cooldownRemaining(uuid, spec);
        if (remaining > 0 && unlocked) {
            lore.add(Elements.lore(Text.tr("political.power.gui.abilities.ready_in", seconds(remaining)), Palette.WARN));
        }
        if (!unlocked && !PowerService.awakened(uuid)) {
            lore.add(Elements.lore(Text.tr("political.power.gui.abilities.asleep"), Palette.BAD));
            return entry(Icon.OFF.item(), name, Palette.FAINT, false, null, null, lore);
        }
        if (!unlocked) {
            Technique.MasteryRank needed = Technique.MasteryRank.values()[Math.min(PowerCatalog.rankIndexOf(spec), Technique.MasteryRank.values().length - 1)];
            lore.add(Elements.lore(Text.tr("political.power.gui.abilities.locked", needed.display), Palette.BAD));
            return entry(Icon.OFF.item(), name, Palette.FAINT, false, null, null, lore);
        }
        if (isSelected) {
            lore.add(Elements.lore(Text.tr("political.power.gui.abilities.selected"), Palette.GOOD));
            return entry(abilityIcon(spec.kind()), name, Palette.GOOD, true, null, null, lore);
        }
        return entry(abilityIcon(spec.kind()), name, Palette.TITLE, false, () -> {
            (PowerService.selectAbility(uuid, spec.id()) ? UiSound.SUCCESS : UiSound.DENIED).play(player);
            openAbilities(player);
        }, Text.tr("political.power.gui.abilities.click"), lore);
    }

    // ── vows ────────────────────────────────────────────────────────────────────────────────

    public static void openVows(ServerPlayer player) {
        String uuid = uuid(player);
        VowSet set = PowerService.vowsOf(uuid);
        long wait = set.reacceptRemaining(com.political.core.Clock.ticks());
        List<GuiElement> entries = new ArrayList<>();
        for (Vow vow : Vow.values()) {
            entries.add(vowEntry(player, vow, set, wait));
        }
        Screen.chest(player, ROWS, Text.tr("political.power.gui.vows")).frame(Screen.Edge.NORMAL)
                .header(Items.WRITABLE_BOOK, Text.tr("political.power.gui.vows"), Text.tr("political.power.gui.vows.about"),
                        Text.tr("political.power.gui.vows.active", set.active().size(), PowerSettings.VOW_MAX_ACTIVE.get()))
                .list(entries, 0, page -> openVows(player))
                .footer(() -> open(player))
                .open();
    }

    private static Component vowGain(Vow vow) {
        return switch (vow) {
            case NO_ARMOR -> Text.trNamed("political.power.vow.gain", vow.name(), PowerSettings.VOW_NO_ARMOR_BONUS.get());
            case NO_OFFHAND -> Text.trNamed("political.power.vow.gain", vow.name(), percent(PowerSettings.VOW_NO_OFFHAND_COOLDOWN_REDUCTION.get()));
            case NO_SPRINT -> Text.trNamed("political.power.vow.gain", vow.name(), percent(PowerSettings.VOW_NO_SPRINT_DAMAGE_BONUS.get()));
        };
    }

    private static GuiElement vowEntry(ServerPlayer player, Vow vow, VowSet set, long waitTicks) {
        Component name = Text.trNamed("political.power.vow", vow.name());
        List<Component> lore = new ArrayList<>();
        lore.add(Text.trNamed("political.power.vow.rule", vow.name()));
        lore.add(vowGain(vow));
        lore.add(Component.empty());
        if (set.isActive(vow)) {
            lore.add(Elements.lore(Text.tr("political.power.gui.vows.state.active"), Palette.GOOD));
            return entry(Icon.ON.item(), name, Palette.GOOD, true, null, null, lore);
        }
        if (waitTicks > 0) {
            lore.add(Elements.lore(Text.tr("political.power.gui.vows.state.waiting", seconds(waitTicks)), Palette.BAD));
            return entry(Icon.OFF.item(), name, Palette.FAINT, false, null, null, lore);
        }
        if (set.active().size() >= PowerSettings.VOW_MAX_ACTIVE.get()) {
            lore.add(Elements.lore(Text.tr("political.power.gui.vows.state.full"), Palette.BAD));
            return entry(Icon.OFF.item(), name, Palette.FAINT, false, null, null, lore);
        }
        lore.add(Elements.lore(Text.tr("political.power.gui.vows.state.open"), Palette.BODY));
        return entry(Items.WRITABLE_BOOK, name, Palette.TITLE, false, () -> Screen.confirm(player,
                Text.tr("political.power.gui.vows.confirm", name), Text.tr("political.power.gui.vows.consequence", seconds(PowerSettings.VOW_REACCEPT_COOLDOWN_TICKS.get())),
                () -> {
                    VowSet.AcceptResult result = PowerService.acceptVow(player, vow);
                    player.sendSystemMessage(Text.trNamed("political.power.vow.result", result.name(), name), false);
                    (result == VowSet.AcceptResult.ACCEPTED ? UiSound.SUCCESS : UiSound.DENIED).play(player);
                    openVows(player);
                }, () -> openVows(player)), Text.tr("political.power.gui.vows.click"), lore);
    }
}
