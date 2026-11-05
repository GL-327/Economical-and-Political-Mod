package com.political.power.game;

import com.political.PoliticalServer;
import com.political.core.gui.Elements;
import com.political.core.gui.Icon;
import com.political.core.gui.Palette;
import com.political.core.gui.Screen;
import com.political.core.text.Text;
import com.political.power.model.PlayerPower;
import com.political.power.model.PowerCatalog;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The administrator's screens for the power system: who has what, grant and revoke a technique, awaken, reroll, and
 * reset a player. Every screen and every click checks {@link PowerPermissions#isAdmin} again, because a screen can
 * stay open after the permission is taken away. Every change is logged with the administrator's name.
 */
public final class PowerAdminScreens {

    private static final Logger LOGGER = LoggerFactory.getLogger(PowerAdminScreens.class);
    private static final int ROWS = 6;

    private PowerAdminScreens() {
    }

    private static boolean allowed(ServerPlayer admin) {
        if (PowerPermissions.isAdmin(admin)) {
            return true;
        }
        admin.sendSystemMessage(Text.tr("political.power.admin.denied"), false);
        admin.closeContainer();
        return false;
    }

    private static ServerPlayer online(String uuid) {
        return PoliticalServer.server == null ? null : PoliticalServer.server.getPlayerList().getPlayer(UUID.fromString(uuid));
    }

    private static GuiElement entry(Item icon, Component name, Palette color, boolean glow, Runnable onClick, Component prompt, List<Component> lore) {
        GuiElementBuilder b = new GuiElementBuilder(icon).setName(Elements.name(name, color));
        if (glow) {
            b.glow();
        }
        if (!lore.isEmpty()) {
            b.addLoreLine(Component.empty());
            for (Component line : lore) {
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

    /** The innate technique of a player, or a dash. */
    private static Component innateName(String uuid) {
        String id = PowerService.state(uuid).innate;
        return id == null ? Text.plain("-") : PowerService.techniqueName(id);
    }

    // ── players ─────────────────────────────────────────────────────────────────────────────

    public static void open(ServerPlayer admin, int page) {
        if (!allowed(admin) || PoliticalServer.server == null) {
            return;
        }
        List<GuiElement> entries = new ArrayList<>();
        for (ServerPlayer target : PoliticalServer.server.getPlayerList().getPlayers()) {
            String uuid = target.getUUID().toString();
            PlayerPower p = PowerService.state(uuid);
            List<Component> lore = new ArrayList<>();
            lore.add(Text.tr("political.power.admin.innate", innateName(uuid)));
            lore.add(Text.tr(PowerService.awakened(uuid) ? "political.power.gui.state.awake" : "political.power.gui.state.asleep"));
            lore.add(Text.tr("political.power.gui.shards", p.shards));
            lore.add(p.equipped == null ? Text.tr("political.power.gui.techniques.none")
                    : Text.tr("political.power.gui.techniques.equipped", PowerService.techniqueName(p.equipped)));
            entries.add(entry(Items.PLAYER_HEAD, Text.plain(target.getName().getString()), Palette.ACCENT, false,
                    () -> openPlayer(admin, uuid), Text.tr("political.power.admin.click.player"), lore));
        }
        entries.add(0, Elements.info(Items.WRITTEN_BOOK, Text.tr("political.power.admin.owners"), Palette.BODY, ownerLines()));
        Screen.chest(admin, ROWS, Text.tr("political.power.admin.title")).frame(Screen.Edge.DANGER)
                .header(Items.ENCHANTED_BOOK, Text.tr("political.power.admin.title"), Text.tr("political.power.admin.about"))
                .list(entries, page, next -> open(admin, next))
                .footer(null)
                .open();
    }

    private static Component[] ownerLines() {
        Map<String, String> holders = PowerService.uniqueHolders();
        if (holders.isEmpty()) {
            return new Component[]{Text.tr("political.power.admin.owners.none")};
        }
        List<Component> lines = new ArrayList<>();
        for (Map.Entry<String, String> e : holders.entrySet()) {
            ServerPlayer holder = online(e.getValue());
            lines.add(Text.tr("political.power.admin.owners.line", PowerService.techniqueName(e.getKey()),
                    Text.plain(holder == null ? e.getValue().substring(0, 8) : holder.getName().getString())));
        }
        return lines.toArray(new Component[0]);
    }

    // ── one player ──────────────────────────────────────────────────────────────────────────

    public static void openPlayer(ServerPlayer admin, String uuid) {
        if (!allowed(admin)) {
            return;
        }
        ServerPlayer target = online(uuid);
        if (target == null) {
            admin.sendSystemMessage(Text.tr("political.power.admin.offline"), false);
            open(admin, 0);
            return;
        }
        showPlayer(admin, target);
    }

    /** The detail screen of one player who is in the game. */
    static void showPlayer(ServerPlayer admin, ServerPlayer target) {
        String uuid = target.getUUID().toString();
        PlayerPower p = PowerService.state(uuid);
        List<Component> held = new ArrayList<>();
        for (PowerCatalog.TechniqueDef def : PowerService.heldTechniques(uuid)) {
            held.add(Text.plain("• ").append(PowerService.techniqueName(def.id()))
                    .append(p.learned.contains(def.id()) ? Text.plain("") : Text.tr("political.power.admin.from_tree")));
        }
        if (held.isEmpty()) {
            held.add(Text.tr("political.power.gui.techniques.none"));
        }
        List<Component> info = new ArrayList<>();
        info.add(Text.tr("political.power.admin.innate", innateName(uuid)));
        info.add(Text.tr(PowerService.awakened(uuid) ? "political.power.gui.state.awake" : "political.power.gui.state.asleep"));
        info.add(Text.tr("political.power.gui.tier", PowerService.tier(uuid), com.political.power.formula.Tiers.MAX_TIER));
        info.add(Text.tr("political.power.admin.rerolls", p.rerolls, p.rerollCharges));
        info.add(Text.tr("political.power.gui.shards", p.shards));
        info.add(Text.tr("political.power.gui.energy.amount", (int) PowerService.resource(uuid), (int) PowerService.maxResource(uuid)));
        info.addAll(held);

        Screen screen = Screen.chest(admin, ROWS, Text.tr("political.power.admin.player")).frame(Screen.Edge.DANGER)
                .header(Items.PLAYER_HEAD, Text.plain(target.getName().getString()), Text.tr("political.power.admin.player.about"))
                .footer(() -> open(admin, 0));
        screen.set(20, Elements.info(Items.PAPER, Text.tr("political.power.admin.state"), Palette.BODY, info.toArray(new Component[0])));
        screen.set(22, Elements.tile(Items.ENCHANTED_BOOK, Text.tr("political.power.admin.grant"), Palette.GOOD,
                () -> openGrant(admin, uuid, 0), Text.tr("political.power.admin.grant.about")));
        screen.set(24, Elements.tile(Items.BARRIER, Text.tr("political.power.admin.revoke"), Palette.BAD,
                () -> openRevoke(admin, uuid), Text.tr("political.power.admin.revoke.about")));
        screen.set(29, Elements.tile(Items.BEACON, Text.tr("political.power.admin.awaken"), Palette.WARN,
                () -> awaken(admin, uuid), Text.tr("political.power.admin.awaken.about")));
        screen.set(38, Elements.tile(Items.EXPERIENCE_BOTTLE, Text.tr("political.power.admin.mastery"), Palette.WARN,
                () -> master(admin, uuid), Text.tr("political.power.admin.mastery.about")));
        screen.set(31, Elements.tile(Items.AMETHYST_SHARD, Text.tr("political.power.admin.reroll"), Palette.WARN,
                () -> confirmReroll(admin, uuid), Text.tr("political.power.admin.reroll.about")));
        screen.set(33, Elements.tile(Items.TNT, Text.tr("political.power.admin.reset"), Palette.BAD,
                () -> confirmReset(admin, uuid), Text.tr("political.power.admin.reset.about")));
        screen.open();
    }

    static void openGrant(ServerPlayer admin, String uuid, int page) {
        if (!allowed(admin)) {
            return;
        }
        List<GuiElement> entries = new ArrayList<>();
        for (PowerCatalog.TechniqueDef def : PowerCatalog.techniques()) {
            boolean has = PowerService.state(uuid).learned.contains(def.id());
            entries.add(entry(has ? Icon.ON.item() : Items.ENCHANTED_BOOK, PowerService.techniqueName(def.id()),
                    has ? Palette.GOOD : Palette.TITLE, has,
                    has ? null : () -> grant(admin, uuid, def.id()), Text.tr("political.power.admin.click.grant"),
                    List.of(Text.tr(has ? "political.power.admin.has" : "political.power.admin.has_not"))));
        }
        Screen.chest(admin, ROWS, Text.tr("political.power.admin.grant")).frame(Screen.Edge.DANGER)
                .header(Items.ENCHANTED_BOOK, Text.tr("political.power.admin.grant"), Text.tr("political.power.admin.grant.about"))
                .list(entries, page, next -> openGrant(admin, uuid, next))
                .footer(() -> openPlayer(admin, uuid))
                .open();
    }

    private static void grant(ServerPlayer admin, String uuid, String techniqueId) {
        if (!allowed(admin)) {
            return;
        }
        ServerPlayer target = online(uuid);
        if (target == null) {
            openPlayer(admin, uuid);
            return;
        }
        PowerService.LearnResult result = PowerService.learn(target, techniqueId);
        admin.sendSystemMessage(Text.trNamed("political.power.learn.result", result.name(), PowerService.techniqueName(techniqueId)), false);
        LOGGER.info("{} granted {} to {}: {}", admin.getName().getString(), techniqueId, target.getName().getString(), result);
        openGrant(admin, uuid, 0);
    }

    static void openRevoke(ServerPlayer admin, String uuid) {
        if (!allowed(admin)) {
            return;
        }
        PlayerPower p = PowerService.state(uuid);
        List<GuiElement> entries = new ArrayList<>();
        for (PowerCatalog.TechniqueDef def : PowerCatalog.techniques()) {
            if (!p.learned.contains(def.id())) {
                continue;
            }
            Component name = PowerService.techniqueName(def.id());
            entries.add(entry(Items.BARRIER, name, Palette.BAD, false,
                    () -> Screen.confirm(admin, Text.tr("political.power.admin.revoke.confirm", name),
                            Text.tr("political.power.admin.revoke.consequence"),
                            () -> revoke(admin, uuid, def.id()), () -> openRevoke(admin, uuid)),
                    Text.tr("political.power.admin.click.revoke"), List.of()));
        }
        Screen.chest(admin, ROWS, Text.tr("political.power.admin.revoke")).frame(Screen.Edge.DANGER)
                .header(Items.BARRIER, Text.tr("political.power.admin.revoke"), Text.tr("political.power.admin.revoke.about"))
                .list(entries, 0, next -> openRevoke(admin, uuid))
                .footer(() -> openPlayer(admin, uuid))
                .open();
    }

    private static void revoke(ServerPlayer admin, String uuid, String techniqueId) {
        if (!allowed(admin)) {
            return;
        }
        boolean had = PowerService.revoke(uuid, techniqueId);
        Component name = PowerService.techniqueName(techniqueId);
        admin.sendSystemMessage(Text.tr(had ? "political.power.admin.revoked" : "political.power.admin.revoke.nothing", name), false);
        if (had && PowerService.holds(uuid, techniqueId)) {
            admin.sendSystemMessage(Text.tr("political.power.admin.revoke.still", name), false);
        }
        LOGGER.info("{} revoked {} from {}", admin.getName().getString(), techniqueId, uuid);
        openPlayer(admin, uuid);
    }

    private static void awaken(ServerPlayer admin, String uuid) {
        if (!allowed(admin)) {
            return;
        }
        ServerPlayer target = online(uuid);
        if (target != null) {
            boolean changed = PowerService.awaken(target);
            admin.sendSystemMessage(Text.tr(changed ? "political.power.admin.awaken.done" : "political.power.admin.awaken.already"), false);
            LOGGER.info("{} awakened {}: {}", admin.getName().getString(), target.getName().getString(), changed);
        }
        openPlayer(admin, uuid);
    }

    private static void master(ServerPlayer admin, String uuid) {
        if (!allowed(admin)) {
            return;
        }
        PowerService.setUses(uuid, 100);
        admin.sendSystemMessage(Text.tr("political.power.admin.mastery.done", 100), false);
        LOGGER.info("{} set the casts of {} to 100", admin.getName().getString(), uuid);
        openPlayer(admin, uuid);
    }

    private static void confirmReroll(ServerPlayer admin, String uuid) {
        Screen.confirm(admin, Text.tr("political.power.admin.reroll.confirm"), Text.tr("political.power.admin.reroll.consequence"),
                () -> {
                    if (!allowed(admin)) {
                        return;
                    }
                    ServerPlayer target = online(uuid);
                    if (target != null) {
                        PowerService.RerollResult result = PowerService.rerollFree(target);
                        admin.sendSystemMessage(Text.trNamed("political.power.reroll.result", result.name(), PowerService.techniqueName(PowerService.state(uuid).innate)), false);
                        LOGGER.info("{} rerolled {} for free", admin.getName().getString(), target.getName().getString());
                    }
                    openPlayer(admin, uuid);
                }, () -> openPlayer(admin, uuid));
    }

    private static void confirmReset(ServerPlayer admin, String uuid) {
        Screen.confirm(admin, Text.tr("political.power.admin.reset.confirm"), Text.tr("political.power.admin.reset.consequence"),
                () -> {
                    if (!allowed(admin)) {
                        return;
                    }
                    PowerService.reset(uuid);
                    admin.sendSystemMessage(Text.tr("political.power.admin.reset.done"), false);
                    LOGGER.info("{} reset the power state of {}", admin.getName().getString(), uuid);
                    openPlayer(admin, uuid);
                }, () -> openPlayer(admin, uuid));
    }
}
