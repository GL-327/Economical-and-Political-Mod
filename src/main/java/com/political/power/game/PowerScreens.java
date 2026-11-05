package com.political.power.game;

import com.political.SchoolTree;
import com.political.core.gui.Elements;
import com.political.core.gui.Icon;
import com.political.core.gui.Palette;
import com.political.core.gui.Screen;
import com.political.core.gui.UiSound;
import com.political.core.text.Text;
import com.political.magic.MagicSchool;
import com.political.power.PowerSettings;
import com.political.power.formula.Progression;
import com.political.power.formula.SchoolPassives;
import com.political.power.formula.Tiers;
import com.political.power.formula.TreeAdvice;
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

    private static Component schoolName(MagicSchool school) {
        return Text.plain(school.symbol + " " + school.displayName).withStyle(school.color);
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
        MagicSchool school = PowerService.school(uuid);
        int level = PowerService.level(uuid);
        int tier = PowerService.tier(uuid);

        Screen screen = Screen.chest(player, ROWS, Text.tr("political.power.gui.title")).frame(Screen.Edge.NORMAL)
                .header(Items.ENCHANTED_BOOK, Text.tr("political.power.gui.title"),
                        Text.tr("political.power.gui.level", level), Text.tr("political.power.gui.shards", p.shards));

        // Where to go
        screen.set(19, school == null
                ? Elements.tile(Items.ENCHANTING_TABLE, Text.tr("political.power.gui.school"), Palette.ACCENT,
                        () -> openSchools(player), Text.tr("political.power.gui.school.none"))
                : Elements.tile(Items.ENCHANTING_TABLE, Text.tr("political.power.gui.school"), Palette.ACCENT,
                        () -> openTree(player),
                        schoolName(school), Text.tr("political.power.gui.tier", tier, Tiers.MAX_TIER)));
        screen.set(21, Elements.tile(Items.ENCHANTED_BOOK, Text.tr("political.power.gui.techniques"), Palette.ACCENT,
                () -> openTechniques(player, 0), techniqueSummary(uuid, p)));
        screen.set(23, Elements.tile(Items.BOOKSHELF, Text.tr("political.power.gui.tree"), Palette.ACCENT,
                () -> {
                    if (school == null) {
                        openSchools(player);
                    } else {
                        openTree(player);
                    }
                }, Text.tr("political.power.gui.tree.about")));
        screen.set(25, Elements.tile(Items.WRITABLE_BOOK, Text.tr("political.power.gui.vows"), Palette.ACCENT,
                () -> openVows(player), Text.tr("political.power.gui.vows.about"),
                Text.tr("political.power.gui.vows.active", PowerService.vowsOf(uuid).active().size(), PowerSettings.VOW_MAX_ACTIVE.get())));

        // Where you stand
        screen.set(28, levelTile(p, level));
        screen.set(31, Elements.progress(Items.LAPIS_LAZULI, Text.tr("political.power.gui.energy"), Palette.ACCENT,
                energyFraction(uuid), Text.tr("political.power.gui.energy.amount", (int) PowerService.resource(uuid), (int) PowerService.maxResource(uuid))));
        screen.set(34, Elements.info(Items.PAPER, Text.tr("political.power.gui.cast"), Palette.WARN,
                Text.tr("political.power.gui.cast.how"), Text.tr("political.power.gui.cast.what")));

        screen.footer(null).open();
    }

    private static double energyFraction(String uuid) {
        double max = PowerService.maxResource(uuid);
        return max <= 0 ? 0.0 : Math.max(0.0, Math.min(1.0, PowerService.resource(uuid) / max));
    }

    private static Component[] techniqueSummary(String uuid, PlayerPower p) {
        if (p.equipped == null) {
            return new Component[]{Text.tr("political.power.gui.techniques.none")};
        }
        AbilitySpec selected = PowerService.selected(uuid);
        List<Component> lines = new ArrayList<>();
        lines.add(Text.tr("political.power.gui.techniques.equipped", PowerService.techniqueName(p.equipped)));
        if (selected != null) {
            lines.add(Text.tr("political.power.gui.techniques.selected", abilityName(selected)));
        }
        return lines.toArray(new Component[0]);
    }

    private static GuiElement levelTile(PlayerPower p, int level) {
        List<Component> lines = new ArrayList<>();
        lines.add(Text.tr("political.power.gui.level.earned", p.shardsEarnedTotal));
        lines.add(Text.tr("political.power.gui.shards", p.shards));
        if (level < Progression.MAX_LEVEL) {
            lines.add(Text.tr("political.power.gui.level.next", Progression.shardsForLevel(level + 1)));
        } else {
            lines.add(Text.tr("political.power.gui.level.max"));
        }
        return Elements.info(Items.EXPERIENCE_BOTTLE, Text.tr("political.power.gui.level", level), Palette.BODY, lines.toArray(new Component[0]));
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
                .header(Items.ENCHANTED_BOOK, Text.tr("political.power.gui.techniques"), Text.tr("political.power.gui.techniques.about"))
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

    /** Where a technique the player does not hold comes from. */
    private static Component whereFrom(PowerCatalog.TechniqueDef def) {
        if (def.schoolId() == null) {
            return Text.tr("political.power.gui.from.capstone");
        }
        MagicSchool school = MagicSchool.byName(def.schoolId());
        Component from = Text.tr("political.power.gui.from.school", school == null ? Text.plain(def.schoolId()) : schoolName(school), def.unlockNode());
        return def.legacy() ? Text.tr("political.power.gui.from.school_or_manual", from) : from;
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

    // ── school ──────────────────────────────────────────────────────────────────────────────

    public static void openSchools(ServerPlayer player) {
        String uuid = uuid(player);
        MagicSchool chosen = PowerService.school(uuid);
        if (chosen != null) {
            openTree(player);
            return;
        }
        List<GuiElement> entries = new ArrayList<>();
        for (MagicSchool school : MagicSchool.values()) {
            entries.add(schoolEntry(player, school));
        }
        Screen.chest(player, ROWS, Text.tr("political.power.gui.school")).frame(Screen.Edge.INFO)
                .header(Items.ENCHANTING_TABLE, Text.tr("political.power.gui.school"), Text.tr("political.power.gui.school.choose"))
                .list(entries, 0, page -> openSchools(player))
                .footer(() -> open(player))
                .open();
    }

    private static GuiElement schoolEntry(ServerPlayer player, MagicSchool school) {
        List<Component> lore = new ArrayList<>();
        lore.add(Text.plain(school.description));
        lore.add(Component.empty());
        boolean selectable = PowerCatalog.selectableSchools().contains(school.name());
        List<PowerCatalog.TechniqueDef> grants = PowerCatalog.techniquesOf(school.name());
        for (PowerCatalog.TechniqueDef def : grants) {
            lore.add(Elements.lore(Text.tr("political.power.gui.school.grants", PowerService.techniqueName(def.id()), def.unlockNode()), Palette.BODY));
        }
        lore.add(Elements.lore(Text.tr("political.power.gui.school.grants", Text.tr("political.power.technique.domain"), Tiers.CAPSTONE_NODE), Palette.BODY));
        if (!selectable) {
            lore.add(Component.empty());
            lore.add(Elements.lore(Text.tr("political.power.gui.school.dormant"), Palette.BAD));
            return entry(Icon.OFF.item(), schoolName(school), Palette.FAINT, false, null, null, lore);
        }
        return entry(Items.ENCHANTING_TABLE, schoolName(school), Palette.TITLE, false, () -> Screen.confirm(player,
                Text.tr("political.power.gui.school.confirm", schoolName(school)), Text.tr("political.power.gui.school.consequence"),
                () -> {
                    PowerService.SchoolResult result = PowerService.chooseSchool(player, school);
                    player.sendSystemMessage(Text.trNamed("political.power.school.result", result.name(), schoolName(school)), false);
                    (result == PowerService.SchoolResult.OK ? UiSound.SUCCESS : UiSound.DENIED).play(player);
                    open(player);
                }, () -> openSchools(player)), Text.tr("political.power.gui.school.click"), lore);
    }

    // ── tree ────────────────────────────────────────────────────────────────────────────────

    public static void openTree(ServerPlayer player) {
        String uuid = uuid(player);
        MagicSchool school = PowerService.school(uuid);
        if (school == null) {
            openSchools(player);
            return;
        }
        PlayerPower p = PowerService.state(uuid);
        int level = PowerService.level(uuid);
        int tier = PowerService.tier(uuid);
        TreeAdvice.NextStep next = TreeAdvice.nextSpine(school, p.unlockedNodes, level, p.shards);

        List<GuiElement> entries = new ArrayList<>();
        for (SchoolTree.SchoolNode node : SchoolTree.nodesFor(school)) {
            entries.add(nodeEntry(player, node, p, level));
        }
        Component nextLine = next == null
                ? Text.tr("political.power.gui.tree.complete")
                : Text.tr("political.power.gui.tree.next", Text.plain(next.node().name()), next.node().shardCost());
        Screen screen = Screen.chest(player, ROWS, Text.tr("political.power.gui.tree")).frame(Screen.Edge.INFO)
                .header(Items.BOOKSHELF, schoolName(school), Text.tr("political.power.gui.tier", tier, Tiers.MAX_TIER), nextLine)
                .list(entries, 0, page -> openTree(player))
                .footer(() -> open(player));
        screen.set(29, Elements.info(Items.EXPERIENCE_BOTTLE, Text.tr("political.power.gui.tree.you"), Palette.BODY,
                Text.tr("political.power.gui.level", level), Text.tr("political.power.gui.shards", p.shards)));
        screen.set(31, nextStepTile(next, p.shards, level));
        screen.set(33, Elements.info(Items.NETHER_STAR, Text.tr("political.power.gui.tree.bonus"), Palette.BODY,
                Text.tr("political.power.gui.tree.bonus.damage", percent(SchoolPassives.damageBonus(school, p.unlockedNodes))),
                Text.tr("political.power.gui.tree.bonus.energy", SchoolPassives.resourceBonus(school, p.unlockedNodes)),
                Text.tr("political.power.gui.tree.bonus.cooldown", percent(SchoolPassives.cooldownReduction(school, p.unlockedNodes)))));
        screen.open();
    }

    private static GuiElement nextStepTile(TreeAdvice.NextStep next, int shards, int level) {
        if (next == null) {
            return Elements.info(Items.NETHER_STAR, Text.tr("political.power.gui.tree.next.title"), Palette.GOOD, Text.tr("political.power.gui.tree.complete"));
        }
        List<Component> lines = new ArrayList<>();
        lines.add(Text.plain(next.node().name()));
        lines.add(Text.tr("political.power.gui.tree.cost", next.node().shardCost(), next.node().levelRequired()));
        lines.add(Text.tr("political.power.gui.tier.after", next.tierAfter(), Tiers.MAX_TIER));
        for (String id : next.unlocks()) {
            lines.add(Text.tr("political.power.gui.tree.grants", PowerService.techniqueName(id)));
        }
        lines.add(statusLine(next.status(), next.shardsMissing(), next.levelsMissing(), next.node(), level));
        return Elements.info(Items.COMPASS, Text.tr("political.power.gui.tree.next.title"), Palette.WARN, lines.toArray(new Component[0]));
    }

    private static Component statusLine(TreeAdvice.Status status, int shardsMissing, int levelsMissing, SchoolTree.SchoolNode node, int level) {
        return switch (status) {
            case UNLOCKED -> Text.tr("political.power.gui.tree.status.unlocked");
            case READY -> Text.tr("political.power.gui.tree.status.ready");
            case NEEDS_SHARDS -> Text.tr("political.power.gui.tree.status.needs_shards", shardsMissing);
            case NEEDS_LEVEL -> Text.tr("political.power.gui.tree.status.needs_level", node.levelRequired(), level);
            case PARENT_LOCKED -> Text.tr("political.power.gui.tree.status.parent_locked");
        };
    }

    private static GuiElement nodeEntry(ServerPlayer player, SchoolTree.SchoolNode node, PlayerPower p, int level) {
        TreeAdvice.Status status = TreeAdvice.status(node, p.unlockedNodes, level, p.shards);
        int spine = TreeAdvice.spineNumber(node);
        List<Component> lore = new ArrayList<>();
        lore.add(Text.plain(node.description()));
        lore.add(Component.empty());
        lore.add(Elements.lore(Text.tr("political.power.gui.tree.cost", node.shardCost(), node.levelRequired()), Palette.BODY));
        if (spine > 0) {
            lore.add(Elements.lore(Text.tr("political.power.gui.tree.spine", spine, Tiers.tier(spine)), Palette.BODY));
            for (String id : TreeAdvice.unlocksAt(node.school(), spine)) {
                lore.add(Elements.lore(Text.tr("political.power.gui.tree.grants", PowerService.techniqueName(id)), Palette.GOOD));
            }
        } else {
            lore.add(Elements.lore(Text.tr("political.power.gui.tree.side"), Palette.BODY));
        }
        lore.add(Elements.lore(statusLine(status, Math.max(0, node.shardCost() - p.shards), 0, node, level),
                status == TreeAdvice.Status.UNLOCKED || status == TreeAdvice.Status.READY ? Palette.GOOD : Palette.BAD));
        Component name = Text.plain(node.name());
        return switch (status) {
            case UNLOCKED -> entry(node.capstone() ? Items.NETHER_STAR : Icon.ON.item(), name, Palette.GOOD, true, null, null, lore);
            case READY -> entry(node.capstone() ? Items.NETHER_STAR : Items.AMETHYST_SHARD, name, Palette.TITLE, false,
                    () -> Screen.confirm(player, Text.tr("political.power.gui.tree.confirm", name),
                            Text.tr("political.power.gui.tree.consequence", node.shardCost()),
                            () -> {
                                PowerService.NodeResult result = PowerService.unlockNode(player, node.id());
                                player.sendSystemMessage(Text.trNamed("political.power.node.result", result.name(), name), false);
                                (result == PowerService.NodeResult.OK ? UiSound.SUCCESS : UiSound.DENIED).play(player);
                                openTree(player);
                            }, () -> openTree(player)),
                    Text.tr("political.power.gui.tree.click"), lore);
            default -> entry(Icon.OFF.item(), name, Palette.FAINT, false, null, null, lore);
        };
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
