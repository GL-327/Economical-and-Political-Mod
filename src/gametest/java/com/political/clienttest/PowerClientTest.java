package com.political.clienttest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

/**
 * The owner's power test (program section 15, items 1 to 12) played by a real Minecraft client against the running
 * dev server of runServerVerify on port 25566. It presses the real keys, clicks the real container screens, reads the
 * action bar and chat the client received, and takes screenshots. Development only; see docs/OWNER_POWER_TEST.md.
 *
 * <p>System properties: powertest.commands is a file that the dev server's console reads (the test appends
 * "op PowerTester" to it); powertest.port is the server port (default 25566); powertest.phases is a comma separated
 * list of phases to play (default: all except blackflash, which needs the server started with chance 1.0).</p>
 */
public class PowerClientTest implements FabricClientGameTest {

    private record Msg(boolean overlay, String text) {
    }

    private final List<Msg> messages = new CopyOnWriteArrayList<>();
    private final List<String> failures = new ArrayList<>();
    private int checks;
    private ClientGameTestContext ctx;

    @Override
    public void runTest(ClientGameTestContext context) {
        this.ctx = context;
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> messages.add(new Msg(overlay, message.getString())));
        String port = System.getProperty("powertest.port", "25566");
        context.runOnClient(client -> ConnectScreen.startConnecting(new TitleScreen(), client,
                ServerAddress.parseString("localhost:" + port), new ServerData("dev", "localhost:" + port, ServerData.Type.OTHER), false, null));
        context.waitFor(client -> client.player != null && client.level != null, 2400);
        context.waitTicks(10);
        if (context.computeOnClient(client -> client.gui.screen() instanceof net.minecraft.client.gui.screens.DeathScreen)) {
            context.clickScreenButton("deathScreen.respawn"); // a state saved while dead
            context.waitTicks(60);
        }
        // the dev world has mobs of its own (the nemesis system hunts a player who died there): stay safe at once
        cmd("gamemode creative");
        cmd("tp @s ~ 200 ~ 0 0");
        context.waitTicks(60);
        context.takeScreenshot("00-joined");
        String wanted = System.getProperty("powertest.phases", "permission,setup,technique,grants,domain,combat,death,regression,catalogs");
        try {
            for (String phase : wanted.split(",")) {
                switch (phase.trim()) {
                    case "permission" -> phase("permission", this::permission);
                    case "setup" -> phase("setup", this::setup);
                    case "technique" -> phase("innate technique, awakening and reroll through the screens", this::technique);
                    case "grants" -> phase("grants and the techniques screen", this::grants);
                    case "domain" -> phase("Domain, and vows while it runs", this::domainAndVows);
                    case "combat" -> phase("Blue, Red, Cursed Blast, Reverse, Barrier", this::combat);
                    case "death" -> phase("death", this::death);
                    case "blackflash" -> phase("Black Flash", this::blackFlash);
                    case "regression" -> phase("older commands still answer", this::regression);
                    case "menus" -> phase("every menu: in and back out", () -> menus(PLAYER_ROOTS));
                    case "catalogs" -> phase("catalog layout, paging and Back", this::catalogs);
                    case "armourtip" -> phase("an armour piece's tooltip", this::armourTip);
                    case "adminmenus" -> phase("every admin menu: in and back out", () -> menus(List.of("admin")));
                    default -> throw new IllegalArgumentException("unknown phase " + phase);
                }
            }
        } finally {
            context.runOnClient(client -> client.disconnectFromWorld(Component.literal("test finished")));
            context.waitTicks(20);
            context.setScreen(TitleScreen::new); // the framework wants the run to end on the title screen
            context.waitTicks(5);
        }
        System.out.println("CLIENTTEST " + checks + " checks, " + failures.size() + " failed");
        for (String failure : failures) {
            System.out.println("CLIENTTEST FAILED: " + failure);
        }
        if (!failures.isEmpty()) {
            throw new AssertionError(failures.size() + " of " + checks + " checks failed: " + failures);
        }
    }

    // ── plumbing ────────────────────────────────────────────────────────────────────────────

    private void phase(String name, Runnable body) {
        System.out.println("CLIENTTEST phase: " + name);
        try {
            body.run();
        } catch (RuntimeException | AssertionError e) {
            failures.add("phase '" + name + "' threw " + e);
            e.printStackTrace();
        }
    }

    private void check(String what, boolean ok) {
        checks++;
        System.out.println("CLIENTTEST " + (ok ? "ok    " : "FAILED") + " " + what);
        if (!ok) {
            failures.add(what);
        }
    }

    private int mark() {
        return messages.size();
    }

    private boolean saw(int mark, String needle, Boolean overlay) {
        for (int i = mark; i < messages.size(); i++) {
            Msg m = messages.get(i);
            if ((overlay == null || m.overlay == overlay) && m.text.toLowerCase(Locale.ROOT).contains(needle.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    /** Waits up to {@code ticks} for a message that contains the text. */
    private boolean waitFor(int mark, String needle, Boolean overlay, int ticks) {
        for (int i = 0; i < ticks; i++) {
            if (saw(mark, needle, overlay)) {
                return true;
            }
            ctx.waitTick();
        }
        return saw(mark, needle, overlay);
    }

    /** The messages since the mark, for the line of a check; call it after the waiting, not before. */
    private String since(int mark) {
        StringBuilder sb = new StringBuilder();
        for (int i = mark; i < messages.size(); i++) {
            if (messages.get(i).overlay && messages.get(i).text.contains("Mana") && messages.get(i).text.contains("Defense")) {
                continue; // the permanent status bar line
            }
            sb.append(messages.get(i).overlay ? "[bar] " : "[chat] ").append(messages.get(i).text).append(" | ");
        }
        return sb.toString();
    }

    private void cmd(String command) {
        ctx.runOnClient(client -> client.player.connection.sendCommand(command));
        ctx.waitTicks(8);
    }

    private void consoleCommand(String command) {
        String file = System.getProperty("powertest.commands");
        if (file == null) {
            throw new IllegalStateException("set -Dpowertest.commands to the file the dev server console reads");
        }
        try {
            Files.writeString(Path.of(file), command + System.lineSeparator(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    // ── container screens ───────────────────────────────────────────────────────────────────

    private String screenTitle() {
        return ctx.computeOnClient(client -> client.gui.screen() == null ? "" : client.gui.screen().getTitle().getString());
    }

    private boolean inContainer() {
        return ctx.computeOnClient(client -> client.gui.screen() instanceof AbstractContainerScreen<?>);
    }

    private boolean waitForTitle(String title, int ticks) {
        for (int i = 0; i < ticks; i++) {
            if (screenTitle().equals(title)) {
                ctx.waitTicks(4);
                return true;
            }
            ctx.waitTick();
        }
        return screenTitle().equals(title);
    }

    private static String plain(ItemStack stack) {
        return stack.isEmpty() ? "" : stack.getHoverName().getString().replace("← ", "").replace("✕ ", "").trim();
    }

    /** The first slot whose item name equals (or, with {@code exact} false, contains) the text; -1 if none. */
    private int slotNamed(String name, boolean exact) {
        return ctx.computeOnClient(client -> {
            if (!(client.gui.screen() instanceof AbstractContainerScreen<?> screen)) {
                return -1;
            }
            AbstractContainerMenu menu = screen.getMenu();
            for (int i = 0; i < menu.slots.size(); i++) {
                Slot slot = menu.slots.get(i);
                if (slot.container == client.player.getInventory()) {
                    continue;
                }
                String n = plain(slot.getItem());
                if (exact ? n.equalsIgnoreCase(name) : n.toLowerCase(Locale.ROOT).contains(name.toLowerCase(Locale.ROOT))) {
                    return i;
                }
            }
            return -1;
        });
    }

    /** Prints the title and every named slot of the open screen, for a failed lookup. */
    private void dump(String why) {
        String text = ctx.computeOnClient(client -> {
            if (!(client.gui.screen() instanceof AbstractContainerScreen<?> screen)) {
                return "no container screen";
            }
            StringBuilder sb = new StringBuilder(screen.getTitle().getString()).append(": ");
            AbstractContainerMenu menu = screen.getMenu();
            for (int i = 0; i < menu.slots.size(); i++) {
                Slot slot = menu.slots.get(i);
                if (slot.container != client.player.getInventory() && !slot.getItem().isEmpty() && !plain(slot.getItem()).isEmpty()) {
                    sb.append(i).append('=').append(plain(slot.getItem())).append("; ");
                }
            }
            return sb.toString();
        });
        System.out.println("CLIENTTEST screen (" + why + ") " + text);
    }

    private void clickSlot(int slot) {
        ctx.runOnClient(client -> {
            AbstractContainerMenu menu = ((AbstractContainerScreen<?>) client.gui.screen()).getMenu();
            client.gameMode.handleContainerInput(menu.containerId, slot, 0, ContainerInput.PICKUP, client.player);
        });
        ctx.waitTicks(10);
    }

    /** Clicks the named slot and waits for the screen with the expected title to open. */
    private boolean go(String name, boolean exact, String title) {
        int slot = slotNamed(name, exact);
        if (slot < 0) {
            dump("no slot named " + name);
            return false;
        }
        clickSlot(slot);
        boolean opened = waitForTitle(title, 120);
        if (!opened) {
            dump("expected " + title + ", have " + screenTitle());
        }
        return opened;
    }

    /** The lore lines of the first slot with that name, or an empty list. */
    private List<String> loreOf(String name, boolean exact) {
        int slot = slotNamed(name, exact);
        if (slot < 0) {
            return List.of();
        }
        return ctx.computeOnClient(client -> {
            AbstractContainerMenu menu = ((AbstractContainerScreen<?>) client.gui.screen()).getMenu();
            ItemLore lore = menu.slots.get(slot).getItem().get(DataComponents.LORE);
            List<String> out = new ArrayList<>();
            if (lore != null) {
                for (Component line : lore.lines()) {
                    out.add(line.getString());
                }
            }
            return out;
        });
    }

    /** Moves the cursor over a slot so the screenshot shows its tooltip. */
    private void hover(String name, boolean exact, String screenshot) {
        int slot = slotNamed(name, exact);
        if (slot < 0) {
            return;
        }
        double[] at = ctx.computeOnClient(client -> {
            AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) client.gui.screen();
            AbstractContainerMenu menu = screen.getMenu();
            Slot s = menu.slots.get(slot);
            int rows = (menu.slots.size() - 36) / 9;
            int imageHeight = 114 + rows * 18;
            double scale = client.getWindow().getGuiScale();
            double left = (client.getWindow().getGuiScaledWidth() - 176) / 2.0;
            double top = (client.getWindow().getGuiScaledHeight() - imageHeight) / 2.0;
            return new double[]{(left + s.x + 8) * scale, (top + s.y + 8) * scale};
        });
        ctx.getInput().setCursorPos(at[0], at[1]);
        ctx.waitTicks(4);
        ctx.takeScreenshot(screenshot);
    }

    /** Opens a bounty armour piece from the recipe browser, prints its lore lines with the colour of each part, screenshots the hover. */
    private void armourTip() {
        cmd("gamemode creative");
        cmd("recipe");
        check("the recipe browser opens", waitForContainer2(120));
        boolean sets = go("Bounty Sets", false, "⚔ Bounty Sets (T1 & T2)");
        check("the bounty sets list opens", sets);
        int slot = slotNamed("Chestplate", false);
        check("a chestplate is listed", slot >= 0);
        if (slot < 0) {
            return;
        }
        List<String> lines = ctx.computeOnClient(client -> {
            AbstractContainerMenu menu = ((AbstractContainerScreen<?>) client.gui.screen()).getMenu();
            ItemLore lore = menu.slots.get(slot).getItem().get(DataComponents.LORE);
            List<String> out = new ArrayList<>();
            if (lore != null) {
                for (Component line : lore.lines()) {
                    StringBuilder sb = new StringBuilder(line.getString()).append("   <");
                    line.visit((style, text) -> {
                        sb.append(style.getColor() == null ? "-" : style.getColor().serialize()).append(' ');
                        return java.util.Optional.empty();
                    }, net.minecraft.network.chat.Style.EMPTY);
                    out.add(sb.append('>').toString());
                }
            }
            return out;
        });
        for (String line : lines) {
            System.out.println("CLIENTTEST TIP " + line);
        }
        int[] oldSize = ctx.computeOnClient(client -> new int[]{client.getWindow().getWidth(), client.getWindow().getHeight()});
        ctx.getInput().resizeWindow(1280, 900);
        ctx.waitTicks(10);
        hover("Chestplate", false, "40-armour-tooltip");
        ctx.getInput().resizeWindow(oldSize[0], oldSize[1]);
        check("the stat lines are in the STATS block, not repeated lower down", lines.stream().filter(l -> l.contains("Defense:")).count() == 1);
    }

    private void closeScreen() {
        ctx.runOnClient(client -> client.player.closeContainer());
        ctx.waitTicks(6);
    }

    /** Opens the Powers hub through the existing menus, the way a player does: /menu, Skills & Magic, Techniques. */
    private boolean openHub() {
        if (inContainer()) {
            closeScreen();
        }
        cmd("menu");
        if (!waitForTitle("Player Hub", 160)) {
            System.out.println("CLIENTTEST /menu did not open the Player Hub; screen is '" + screenTitle() + "', last messages: " + since(Math.max(0, messages.size() - 6)));
            return false;
        }
        return go("Skills & Magic", true, "Skills & Magic") && go("Techniques", true, "Powers");
    }

    /** The energy (current, maximum) shown on the hub. */
    private int[] energy() {
        int[] out = {-1, -1};
        if (!openHub()) {
            return out;
        }
        for (String line : loreOf("Cursed Energy", false)) {
            Matcher m = Pattern.compile("(\\d+) / (\\d+)").matcher(line);
            if (m.find()) {
                out[0] = Integer.parseInt(m.group(1));
                out[1] = Integer.parseInt(m.group(2));
            }
        }
        closeScreen();
        return out;
    }

    // ── in-world helpers ────────────────────────────────────────────────────────────────────

    private void cast() {
        ctx.getInput().holdKey(options -> options.keyShift);
        ctx.waitTicks(3);
        ctx.getInput().pressKey(options -> options.keySwapOffhand);
        ctx.waitTicks(3);
        ctx.getInput().releaseKey(options -> options.keyShift);
        ctx.waitTicks(4);
    }

    private double zombieZ() {
        return ctx.computeOnClient(client -> {
            for (Entity e : client.level.entitiesForRendering()) {
                if (e instanceof Zombie) {
                    return e.getZ();
                }
            }
            return Double.NaN;
        });
    }

    private float zombieHealth() {
        return ctx.computeOnClient(client -> {
            for (Entity e : client.level.entitiesForRendering()) {
                if (e instanceof Zombie z) {
                    return z.getHealth();
                }
            }
            return Float.NaN;
        });
    }

    private float myHealth() {
        return ctx.computeOnClient(client -> client.player.getHealth());
    }

    private void zombieAt(int blocksAhead) {
        cmd("kill @e[type=zombie]");
        cmd("summon zombie ~ ~ ~" + blocksAhead);
        cmd("effect give @e[type=zombie] slowness infinite 255 true");
        ctx.waitTicks(10);
    }

    /** Equips a technique through the techniques screen; a technique that is already equipped is left as it is. */
    private void equip(String technique) {
        int mark = mark();
        boolean opened = openHub() && go("Techniques", true, "Techniques");
        check("the techniques screen opens for " + technique, opened);
        boolean already = loreOf(technique, false).stream().anyMatch(l -> l.equals("Equipped"));
        if (already) {
            check(technique + " is already equipped", true);
        } else {
            int slot = slotNamed(technique, false);
            if (slot >= 0) {
                clickSlot(slot);
            }
            boolean said = waitFor(mark, "Equipped", false, 40);
            check("equip " + technique + " (" + since(mark) + ")", slot >= 0 && said);
        }
        closeScreen();
    }

    // ── phases ──────────────────────────────────────────────────────────────────────────────

    /** Script item 9: the admin command is not there for a player who is not an operator, and is for an operator. */
    private void permission() {
        consoleCommand("deop PowerTester");
        ctx.waitTicks(80);
        boolean hidden = ctx.computeOnClient(client -> client.player.connection.getCommands().getRoot().getChild("poweradmin") == null);
        int mark = mark();
        cmd("poweradmin");
        ctx.waitTicks(60);
        boolean refused = hidden || saw(mark, "Unknown or incomplete", false) || saw(mark, "permission", false);
        check("a non-operator cannot use /poweradmin or open its screen", refused && !inContainer());

        consoleCommand("op PowerTester");
        ctx.waitTicks(80);
        mark = mark();
        cmd("poweradmin info PowerTester");
        boolean answered = waitFor(mark, "PowerTester:", false, 60);
        check("an operator can use /poweradmin (" + since(mark) + ")", answered);
    }

    private void setup() {
        cmd("gamemode creative");
        cmd("time set midnight");
        cmd("gamerule spawn_mobs false");
        cmd("tp @s ~ 200 ~ 0 0");
        // the chunks around the new position load a little later; fill until it works
        boolean built = false;
        for (int attempt = 0; attempt < 30 && !built; attempt++) {
            ctx.waitTicks(20);
            int mark = mark();
            cmd("fill ~-5 199 ~-5 ~5 199 ~16 stone");
            // "No blocks were filled" means the platform is already there, which is as good
            built = waitFor(mark, "Successfully filled", false, 10) || saw(mark, "No blocks were filled", false);
        }
        check("the platform could be built", built);
        cmd("tp @s ~ 200.5 ~ 0 0");
        ctx.waitTicks(20);
        cmd("gamemode survival");
        cmd("kill @e[type=zombie]");
        ctx.waitTicks(20);
        check("the player stands on the platform", ctx.computeOnClient(client -> client.player.onGround() && client.player.getY() > 199.9));
        ctx.takeScreenshot("01-platform");
    }

    /** Script item 1: the technique rolled at spawn, asleep; a refused cast; a reroll; the awakening; mastery. */
    private void technique() {
        check("the Powers hub opens from the menu", openHub());
        ctx.takeScreenshot("02-hub-fresh");
        List<String> yours = loreOf("Your Technique", true);
        check("the hub names a technique and says it is asleep (" + yours + ")", yours.size() >= 2 && yours.contains("Asleep"));
        check("the Awakening tile says how to wake it (" + loreOf("Awakening", true) + ")",
                loreOf("Awakening", true).stream().anyMatch(l -> l.contains("Ender Dragon")));
        hover("Your Technique", true, "02b-hub-your-technique-tooltip");
        hover("Awakening", true, "02c-hub-awakening-tooltip");
        hover("Mastery", true, "02d-hub-mastery-tooltip");
        hover("Cursed Energy", false, "02e-hub-energy-tooltip");
        closeScreen();

        // a sleeping technique cannot be cast
        int mark = mark();
        cast();
        boolean refused = saw(mark, "asleep", true);
        check("a cast while the technique sleeps is refused (" + since(mark) + ")", refused);
        ctx.takeScreenshot("03-asleep-cast");

        // reroll with shards through the screens
        String before = ctx.computeOnClient(client -> client.player.getName().getString());
        check("the reroll screen opens", openHub() && go("Reroll", true, "Reroll"));
        ctx.takeScreenshot("04-reroll");
        hover("Reroll With Shards", true, "04b-reroll-shards-tooltip");
        hover("Use a Reroll Charge", true, "04c-reroll-charge-tooltip");
        hover("Technique Manual", true, "04d-reroll-manual-tooltip");
        mark = mark();
        check("a reroll asks for confirmation", go("Reroll With Shards", true, "Confirm"));
        ctx.takeScreenshot("05-reroll-confirm");
        clickSlot(11);
        boolean rolled = waitFor(mark, "Your technique is now", false, 40);
        check("the reroll gives a technique (" + since(mark) + ")", rolled);
        mark = mark();
        cmd("poweradmin info " + before);
        waitFor(mark, before + ":", false, 40);
        check("the reroll cost shards (" + since(mark) + ")", saw(mark, "190 shards", false) || saw(mark, "shards", false));

        // awakening and mastery by an admin (the dragon itself cannot be fought in a test)
        mark = mark();
        cmd("poweradmin awaken " + before);
        check("awaken (" + since(mark) + ")", waitFor(mark, "awakened", false, 40));
        ctx.waitTicks(20);
        ctx.takeScreenshot("06-awakened-title");
        check("the hub says awakened", openHub() && loreOf("Your Technique", true).contains("Awakened"));
        ctx.takeScreenshot("07-hub-awake");
        closeScreen();
        cmd("poweradmin mastery " + before + " 100");
        check("the hub shows Master rank", openHub() && loreOf("Mastery", true).stream().anyMatch(l -> l.contains("Master")));
        hover("Mastery", true, "07b-hub-mastery-master-tooltip");
        closeScreen();
    }

    private void grants() {
        for (String technique : List.of("BLUE", "RED", "CURSED_BLAST", "REVERSE", "BARRIER")) {
            int mark = mark();
            cmd("poweradmin grant PowerTester " + technique);
            boolean granted = waitFor(mark, "Granted", false, 40) || saw(mark, "already has", false);
            check("grant " + technique + " (" + since(mark) + ")", granted);
        }
        check("the techniques screen opens", openHub() && go("Techniques", true, "Techniques"));
        ctx.takeScreenshot("09-techniques");
        hover("Blue", true, "09b-techniques-blue-tooltip");
        hover("Domain Expansion", false, "09c-techniques-domain-tooltip");
        check("Reverse, Barrier and Domain are listed", slotNamed("Reverse Technique", false) >= 0 && slotNamed("Barrier", false) >= 0
                && slotNamed("Domain Expansion", false) >= 0);
        closeScreen();
    }

    /** Script items 6 and 11. The Domain runs for 600 ticks; the vow screens are used while it does. */
    private void domainAndVows() {
        zombieAt(5);
        equip("Domain Expansion");
        int[] before = energy();
        int mark = mark();
        cast();
        boolean announced = saw(mark, "Domain Expansion", true) && saw(mark, "-150", true);
        check("Domain Expansion is announced with its cost (" + since(mark) + ")", announced);
        ctx.waitTicks(6);
        ctx.takeScreenshot("10-domain-started");
        long castAt = ctx.computeOnClient(client -> client.level.getGameTime());
        int[] after = energy();
        check("the Domain cost about 150 energy (" + before[0] + " to " + after[0] + ")", before[0] - after[0] >= 150 - 12 && before[0] - after[0] <= 150 + 4);

        // item 6: the armor vow. The dev world is reused between runs, so a piece of armor left by an earlier crawl would
        // keep the vow from being accepted: start from an empty inventory.
        cmd("clear @s");
        cmd("item replace entity @s armor.head with iron_helmet");
        mark = mark();
        check("the vow screen opens", openHub() && go("Binding Vows", true, "Binding Vows"));
        ctx.takeScreenshot("11-vows");
        hover("No armor", true, "11b-vows-tooltip");
        boolean asked = go("No armor", true, "Confirm");
        check("taking a vow asks for confirmation", asked);
        if (asked) {
            clickSlot(11);
        }
        boolean refused = waitFor(mark, "do not meet the vow", false, 40);
        check("No armor while armored is refused (" + since(mark) + ")", refused);
        cmd("item replace entity @s armor.head with air");
        mark = mark();
        check("the vow screen opens again", openHub() && go("Binding Vows", true, "Binding Vows"));
        asked = go("No armor", true, "Confirm");
        if (asked) {
            clickSlot(11);
        }
        boolean took = waitFor(mark, "You took the vow", false, 40);
        check("No armor without armor is accepted (" + since(mark) + ")", took);
        ctx.takeScreenshot("12-vow-taken");
        int[] withVow = energy();
        check("the vow raised the maximum (" + after[1] + " to " + withVow[1] + ")", withVow[1] == after[1] + 20);
        mark = mark();
        cmd("item replace entity @s armor.head with iron_helmet");
        boolean broke = waitFor(mark, "vow was broken", false, 80);
        check("putting armor on breaks the vow (" + since(mark) + ")", broke);
        ctx.takeScreenshot("13-vow-broken");
        cmd("item replace entity @s armor.head with air");

        // wait for the Domain to end; the end title shows for about 60 ticks
        long now = ctx.computeOnClient(client -> client.level.getGameTime());
        long remaining = 600 - (now - castAt);
        if (remaining > 10) {
            ctx.waitTicks((int) remaining + 12);
        }
        ctx.takeScreenshot("15-domain-ended");
    }

    /** Script items 2 to 5, with the Barrier. */
    private void combat() {
        ctx.waitTicks(300); // regeneration after the Domain
        zombieAt(9);
        equip("Blue");
        int mark = mark();
        double z0 = zombieZ();
        cast();
        boolean announced = saw(mark, "Blue", true) && saw(mark, "-15", true);
        check("Blue is announced with its cost (" + since(mark) + ")", announced);
        ctx.waitTicks(10);
        ctx.takeScreenshot("16-blue");
        double z1 = zombieZ();
        check("Blue pulls the zombie toward the player (z " + z0 + " to " + z1 + ")", z1 < z0 - 0.5);
        mark = mark();
        ctx.waitTicks(4);
        cast();
        boolean cooling = saw(mark, "On cooldown", true);
        check("Blue again is on cooldown (" + since(mark) + ")", cooling);
        ctx.takeScreenshot("17-on-cooldown");
        cmd("kill @e[type=zombie]"); // a pulled zombie stands next to the player and would hit back

        // item 4: Red through a wall
        ctx.waitTicks(100);
        zombieAt(5);
        cmd("setblock ~ ~1 ~2 stone");
        equip("Red");
        mark = mark();
        cast();
        boolean blocked = saw(mark, "Something is in the way", true);
        check("Red through a wall is refused (" + since(mark) + ")", blocked);
        ctx.takeScreenshot("18-wall");
        cmd("setblock ~ ~1 ~2 air");
        ctx.waitTicks(10);
        mark = mark();
        double r0 = zombieZ();
        cast();
        ctx.waitTicks(8);
        double r1 = zombieZ();
        check("Red without the wall is announced (" + since(mark) + ")", saw(mark, "Red", true) && saw(mark, "-20", true));
        check("Red pushes the zombie away (z " + r0 + " to " + r1 + ")", r1 > r0 + 0.5);
        cmd("kill @e[type=zombie]");

        // Cursed Blast
        ctx.waitTicks(100);
        zombieAt(6);
        equip("Cursed Blast");
        mark = mark();
        float h0 = zombieHealth();
        cast();
        ctx.waitTicks(6);
        float h1 = zombieHealth();
        check("Cursed Blast hurts the zombie (" + h0 + " to " + h1 + ", " + since(mark) + ")", h1 < h0);
        ctx.takeScreenshot("19-blast");

        // item 5: Reverse on self at low health
        ctx.waitTicks(100);
        cmd("kill @e[type=zombie]");
        // Earlier combat can leave the reused test player below 12 HP. Start the controlled hit at full health.
        cmd("effect give @s instant_health 1 10 true");
        ctx.waitTicks(20);
        check("Reverse damage setup starts alive at full health", myHealth() >= 20.0f);
        cmd("damage @s 12 minecraft:generic");
        cmd("tp @s ~ ~ ~ 0 -90");
        ctx.waitTicks(40);
        equip("Reverse");
        float low = myHealth();
        mark = mark();
        cast();
        ctx.waitTicks(8);
        float healed = myHealth();
        check("Reverse heals by at most 8 (" + low + " to " + healed + ", " + since(mark) + ")", healed > low && healed - low <= 8.5f);
        check("Reverse is announced with its cost", saw(mark, "Reverse", true));
        ctx.takeScreenshot("20-reverse");

        // Barrier
        ctx.waitTicks(220);
        cmd("tp @s ~ ~ ~ 0 0");
        equip("Barrier");
        mark = mark();
        cast();
        float b0 = myHealth();
        cmd("damage @s 4 minecraft:generic");
        ctx.waitTicks(6);
        float b1 = myHealth();
        check("the Barrier absorbs a hit (" + b0 + " to " + b1 + ", " + since(mark) + ")", b0 > 0 && b1 >= b0 - 0.2f);
        ctx.waitTicks(50);
        cmd("damage @s 2 minecraft:generic");
        ctx.waitTicks(6);
        check("after its duration the hit lands (" + b1 + " to " + myHealth() + ")", myHealth() < b1 - 1.0f);
    }

    /** Script item 7: death halves the energy and clears cooldowns. */
    private void death() {
        cmd("tp @s ~ ~ ~ 0 0");
        equip("Barrier");
        ctx.waitTicks(320); // the Barrier's own cooldown (300 ticks) passes
        int mark = mark();
        cast();
        boolean cast = saw(mark, "Barrier", true) && !saw(mark, "On cooldown", true);
        check("Barrier cast before death (" + since(mark) + ")", cast);
        int[] beforeDeath = energy();
        cmd("kill @s");
        ctx.waitForScreen(net.minecraft.client.gui.screens.DeathScreen.class);
        ctx.takeScreenshot("21-death");
        ctx.clickScreenButton("deathScreen.respawn");
        ctx.waitFor(client -> client.player != null && client.player.isAlive() && client.gui.screen() == null, 600);
        ctx.waitTicks(80);
        int[] afterDeath = energy();
        int expected = (int) Math.floor(beforeDeath[0] / 2.0);
        // the energy was read about 100 ticks after the respawn, so up to 4 per second of regeneration is on top of the half
        check("death halves the energy (" + beforeDeath[0] + " to " + afterDeath[0] + ", half is " + expected + ")",
                afterDeath[0] >= expected - 2 && afterDeath[0] <= expected + 35);
        mark = mark();
        cast();
        boolean again = waitFor(mark, "Barrier", true, 60) && !saw(mark, "On cooldown", true);
        check("the Barrier cooldown is gone after death (" + since(mark) + ")", again);
        ctx.takeScreenshot("22-after-respawn");
    }

    /**
     * Script item 13, as far as opening goes: the older systems (stats, relics, bounties, bazaar, settlements,
     * expeditions and more) still answer their commands after the power change. It opens each one and looks for an error
     * message; it does not play their normal cases (an election, a settlement registration, an expedition run).
     */
    private void regression() {
        cmd("gamemode creative");
        for (String command : List.of("stats", "statbar", "coins", "bounty", "bazaar", "civ", "relics", "titles", "settlement", "shop",
                "expedition", "hunt", "party", "guild", "trade", "auction", "war", "vote")) {
            int mark = mark();
            cmd(command);
            for (int i = 0; i < 60 && !inContainer() && messages.size() == mark; i++) {
                ctx.waitTick();
            }
            ctx.waitTicks(10);
            boolean broken = saw(mark, "unexpected error", null) || saw(mark, "Unknown or incomplete", null);
            check("/" + command + " answers without an error (" + (inContainer() ? "screen '" + screenTitle() + "'" : since(mark)) + ")", !broken);
            if (inContainer()) {
                ctx.takeScreenshot("30-" + command);
                closeScreen();
            }
        }
        // the coin ledger (M3): an admin grant is recorded, /ledger shows it, and ledger.json is written when the server stops
        int mark = mark();
        cmd("coins add @s 7");
        ctx.waitTicks(10);
        mark = mark();
        cmd("ledger");
        ctx.waitTicks(20);
        check("/ledger shows the coin totals (" + since(mark) + ")", saw(mark, "Coin ledger", null));
    }


    // ── the menu crawler ────────────────────────────────────────────────────────────────────

    private static final int CRAWL_MAX_DEPTH = 5;
    private static final int CRAWL_MAX_CLICKS = Integer.getInteger("powertest.crawl.maxClicks", 1500);
    private static final int CRAWL_MAX_SLOTS_PER_SCREEN = 24;

    /** How many new child screens one screen may be descended into: wide near the top, narrow below (a list of 60 collections). */
    private static int descendLimit(int depth) {
        return depth == 0 ? 14 : depth == 1 ? 6 : 3;
    }

    private void finding(String text) {
        crawlFindings.add(text);
        System.out.println("CLIENTTEST MENU PROBLEM " + text);
    }

    private final java.util.Set<String> crawlVisited = new java.util.HashSet<>();
    private final List<String> crawlFindings = new ArrayList<>();
    private final List<String> crawlEdges = new ArrayList<>();
    private int crawlClicks;
    private String crawlRoot = "menu";

    /**
     * Opens every menu reachable from the main menus by clicking every item, and at each new screen presses its back
     * button. Back must lead to the screen the player came from; a screen with no way back, or a back button that goes
     * somewhere else, is reported. Clicks that do things (buy, toggle, start) are clicked too: this is a throwaway
     * development world. Development only.
     */
    private static final List<String> PLAYER_ROOTS = List.of("menu", "civ", "stats", "bounty", "bazaar", "shop", "expedition", "hunt",
            "party", "guild", "trade", "auction", "war", "titles", "relics", "settlement", "technique", "credits", "view", "cc",
            "recipe", "setrecipe", "customitems");

    private void menus(List<String> roots) {
        cmd("gamemode creative");
        String only = System.getProperty("powertest.roots"); // a comma separated list of command roots, to crawl fewer
        int requested = (int) roots.stream().filter(root -> only == null || List.of(only.split(",")).contains(root)).count();
        int attempted = 0;
        for (String root : roots) {
            if (only != null && !List.of(only.split(",")).contains(root)) {
                continue;
            }
            if (crawlClicks >= CRAWL_MAX_CLICKS) {
                break;
            }
            crawlRoot = root;
            attempted++;
            cmd(root);
            if (!waitForContainer2(120)) {
                System.out.println("CLIENTTEST MENU root /" + root + " opens no screen");
                finding("requested root /" + root + " did not open a screen");
                closeAnyScreen();
                continue;
            }
            String title = screenTitle();
            System.out.println("CLIENTTEST MENU root /" + root + " -> '" + title + "'");
            crawl(new ArrayList<>(), title, 0);
            closeAnyScreen();
        }
        System.out.println("CLIENTTEST MENU visited " + crawlVisited.size() + " screens with " + crawlClicks + " clicks");
        check("all requested crawl roots were attempted", requested > 0 && attempted == requested);
        if (crawlClicks >= CRAWL_MAX_CLICKS) finding("click budget reached; this crawl is incomplete");
        check("every menu reached has a way back (" + crawlFindings.size() + " problems)", crawlFindings.isEmpty());
    }

    private boolean waitForContainer2(int ticks) {
        for (int i = 0; i < ticks; i++) {
            if (inContainer()) {
                ctx.waitTicks(4);
                return true;
            }
            ctx.waitTick();
        }
        return inContainer();
    }

    /** Closes whatever screen is open (a menu, a sign editor, the chat). */
    private void closeAnyScreen() {
        ctx.runOnClient(client -> {
            if (client.gui.screen() != null) {
                if (client.gui.screen() instanceof AbstractContainerScreen<?>) client.player.closeContainer();
                else client.gui.screen().onClose();
            }
        });
        ctx.waitTicks(3);
        // Closing a sign can reopen its catalog on the server. Close that container as well before the next command.
        ctx.runOnClient(client -> {
            if (client.gui.screen() instanceof AbstractContainerScreen<?>) client.player.closeContainer();
        });
        ctx.waitTicks(3);
    }

    /** Waits until the screen changes from {@code title} (a different title, or no container), at most {@code ticks}. */
    private String waitForScreenChange(String title, int ticks) {
        for (int i = 0; i < ticks; i++) {
            if (!inContainer()) {
                return "<closed>";
            }
            String now = screenTitle();
            if (!now.equals(title)) {
                ctx.waitTicks(4);
                return screenTitle();
            }
            ctx.waitTick();
        }
        return inContainer() ? screenTitle() : "<closed>";
    }

    private record Item(int slot, String name) {
    }

    /** The page counter is part of a catalog's identity, even though paging keeps its window title. */
    private String pageLabel() {
        for (Item item : items()) if (item.name().startsWith("Page ")) return item.name();
        return "";
    }

    private String bodyState() {
        int footer = ctx.computeOnClient(client -> client.gui.screen() instanceof AbstractContainerScreen<?> screen
                ? screen.getMenu().slots.size() - 45 : 0);
        return items().stream().filter(item -> item.slot() >= 9 && item.slot() < footer).toList().toString();
    }

    private String pageState() { return pageLabel() + "|" + bodyState(); }

    private String menuKey(String title) {
        String page = pageLabel();
        if (!page.isEmpty()) return title + "|" + page;
        int footer = ctx.computeOnClient(client -> client.gui.screen() instanceof AbstractContainerScreen<?> screen
                ? screen.getMenu().slots.size() - 45 : Integer.MAX_VALUE);
        boolean paged = items().stream().anyMatch(item -> item.slot() >= footer && isPager(item.name()));
        return title + "|" + (paged ? bodyState() : "");
    }

    /** A new chest title can arrive before its item payload. Wait before taking the candidate snapshot. */
    private boolean waitForMenuContents() {
        String previous = "";
        int stable = 0;
        for (int tick = 0; tick < 120; tick++) {
            List<Item> content = items();
            String now = content.toString();
            stable = now.equals(previous) ? stable + 1 : 0;
            previous = now;
            boolean ready = ctx.computeOnClient(client -> {
                if (!(client.gui.screen() instanceof AbstractContainerScreen<?> screen)) return false;
                var menu = screen.getMenu();
                int size = menu.slots.size() - 36;
                return size > 4 && !menu.slots.get(4).getItem().isEmpty()
                        && (!menu.slots.get(size - 1).getItem().isEmpty()
                        || content.stream().anyMatch(item -> item.slot() >= size - 9)
                        || screen.getTitle().getString().toLowerCase(Locale.ROOT).contains("confirm"));
            });
            if (ready && !content.isEmpty() && stable >= 4) return true;
            ctx.waitTick();
        }
        return false;
    }

    private boolean turnPage(int slot) {
        String before = pageState();
        clickSlot(slot);
        for (int i = 0; i < 120; i++) {
            if (inContainer() && !bodyState().equals("[]") && !pageState().equals(before)) {
                waitForMenuContents();
                return !pageState().equals(before);
            }
            ctx.waitTick();
        }
        return false;
    }

    /** Read-only presentation checks; never saves, deletes or toggles a recipe. */
    private void catalogs() {
        String[] commands = {"recipe", "setrecipe", "customitems"};
        String[] titles = {"Recipe Browser", "Recipe Editor", "Custom Items"};
        for (int family = 0; family < commands.length; family++) {
            closeAnyScreen();
            cmd(commands[family]);
            check("/" + commands[family] + " opens its main page", waitForTitle(titles[family], 120));
            waitForMenuContents();
            check("command root has no Back", slotNamed("Back", true) < 0);
            check("main page has shared Close and Search", slotNamed("Close", true) == 53 && slotNamed("Search Items", true) == 47);
            check("main header counts categories", loreOf(titles[family], true).stream().anyMatch(line -> line.contains("categories")));
            ctx.getInput().setCursorPos(0, 0);
            ctx.takeScreenshot("catalog-" + commands[family] + "-root");
            int pages = 1;
            while (slotNamed("Next", true) >= 0 && pages < 10) {
                check("main category page advances", turnPage(50));
                pages++;
            }
            check("categories have multiple main pages", pages > 1);
            String last = pageLabel();
            clickSlot(10);
            waitForScreenChange(titles[family], 120);
            waitForMenuContents();
            check("category Back restores its main page", backTo(titles[family]) && pageLabel().equals(last));
            while (slotNamed("Previous", true) >= 0) {
                if (!turnPage(48)) throw new IllegalStateException("previous main page did not open");
            }
            // The owner's original grouped rows: material, spacer, sword, helmet, chest, legs, boots.
            clickSlot(10);
            waitForScreenChange(titles[family], 120);
            waitForMenuContents();
            String gemTitle = screenTitle();
            check("gem armor retains material, spacer, sword and full armor row", equipmentRow(9, true));
            check("lapis retains its separate full armor row", equipmentRow(18, true));
            ctx.getInput().setCursorPos(0, 0);
            ctx.takeScreenshot("catalog-" + commands[family] + "-grouped-armor");
            clickSlot(13);
            if (family < 2) {
                waitForScreenChange(gemTitle, 120);
                check("armor detail Back returns to grouped armor", backTo(gemTitle) && equipmentRow(9, true));
            } else check("grant leaves grouped armor open", screenTitle().equals(gemTitle));
            check("grouped armor returns to main", backTo(titles[family]));
            clickSlot(14);
            waitForScreenChange(titles[family], 120);
            waitForMenuContents();
            check("bounty T1 retains icon, spacer, sword and armor", equipmentRow(9, true));
            check("bounty T2 retains its own grouped row", equipmentRow(18, true));
            ctx.getInput().setCursorPos(0, 0);
            ctx.waitTicks(4);
            ctx.takeScreenshot("catalog-" + commands[family] + "-grouped-bounty");
            int nextSets = slotNamed("Next", false);
            if (nextSets >= 0 && family < 2) {
                String bountyTitle = screenTitle();
                check("bounty grouped pages advance", turnPage(nextSets));
                String setPage = pageState();
                clickSlot(13);
                waitForScreenChange(bountyTitle, 120);
                check("detail Back preserves the grouped set page", backTo(bountyTitle) && pageState().equals(setPage));
            }
            check("bounty sets return to main", backTo(titles[family]));
            clickSlot(30);
            waitForScreenChange(titles[family], 120);
            waitForMenuContents();
            check("tools retain icon, spacer and four tools", equipmentRow(9, false));
            ctx.getInput().setCursorPos(0, 0);
            ctx.waitTicks(4);
            ctx.takeScreenshot("catalog-" + commands[family] + "-grouped-tools");
            check("tool sets return to main", backTo(titles[family]));
            clickSlot(47);
            ctx.waitForScreen(net.minecraft.client.gui.screens.inventory.SignEditScreen.class);
            ctx.getInput().typeChars("a");
            ctx.clickScreenButton("gui.done");
            check("search opens results", waitForTitle("Search Results", 120));
            waitForMenuContents();
            check("search reaches matches beyond twenty", slotNamed("Next", true) == 50 && turnPage(50));
            String resultPage = pageLabel();
            if (family < 2) {
                clickSlot(11);
                waitForScreenChange("Search Results", 120);
                check("search detail Back restores its page", backTo("Search Results") && pageLabel().equals(resultPage));
            }
            check("search returns to main", backTo(titles[family]));
            clickSlot(53);
            check("Close dismisses the main menu", !inContainer());
        }
        for (String source : List.of("menu", "civ")) {
            closeAnyScreen();
            cmd(source);
            waitForContainer2(120);
            waitForMenuContents();
            String parent = screenTitle();
            int recipe = slotNamed(source.equals("menu") ? "Quick shortcuts" : "Recipes", true);
            check("source offers recipe browser", recipe >= 0);
            if (recipe >= 0) {
                clickSlot(recipe);
                check("menu opens browser with Back", waitForTitle("Recipe Browser", 120) && slotNamed("Back", true) == 45);
                check("browser returns to actual " + source + " source", backTo(parent));
            }
        }
        for (int family = 0; family < commands.length; family++) {
            cmd("menu");
            waitForContainer2(120);
            cmd(commands[family]);
            check("direct /" + commands[family] + " clears the previous parent", waitForTitle(titles[family], 120) && slotNamed("Back", true) < 0);
        }
        closeAnyScreen();
    }

    private boolean backTo(String title) {
        waitForMenuContents();
        Item back = items().stream().filter(item -> isBack(item.name())).findFirst().orElse(null);
        if (back == null) return false;
        clickSlot(back.slot());
        boolean opened = waitForTitle(title, 120);
        if (opened) waitForMenuContents();
        return opened;
    }

    private boolean equipmentRow(int base, boolean armor) {
        return ctx.computeOnClient(client -> {
            if (!(client.gui.screen() instanceof AbstractContainerScreen<?> screen)) return false;
            var slots = screen.getMenu().slots;
            if (slots.get(base).getItem().isEmpty() || !plain(slots.get(base + 1).getItem()).isBlank()) return false;
            String[] expected = armor ? new String[]{"_sword", "_helmet", "_chestplate", "_leggings", "_boots"}
                    : new String[]{"_pickaxe", "_axe", "_shovel", "_hoe"};
            for (int i = 0; i < expected.length; i++) {
                var item = slots.get(base + 2 + i).getItem();
                String id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item.getItem()).getPath();
                if (!id.endsWith(expected[i])) return false;
            }
            return true;
        });
    }

    private List<Item> items() {
        return ctx.computeOnClient(client -> {
            List<Item> out = new ArrayList<>();
            if (!(client.gui.screen() instanceof AbstractContainerScreen<?> screen)) {
                return out;
            }
            AbstractContainerMenu menu = screen.getMenu();
            for (int i = 0; i < menu.slots.size(); i++) {
                Slot slot = menu.slots.get(i);
                if (slot.container == client.player.getInventory()) {
                    continue;
                }
                String name = plain(slot.getItem());
                if (!name.isBlank()) {
                    out.add(new Item(i, name));
                }
            }
            return out;
        });
    }

    private static boolean isBack(String name) {
        String n = name.toLowerCase(Locale.ROOT);
        return n.contains("back") || n.startsWith("return") || n.contains("\u25c0") || n.contains("\u2190") || n.contains("previous menu") || n.contains("main menu");
    }

    private static boolean isPager(String name) {
        String n = name.toLowerCase(Locale.ROOT);
        return n.contains("next") || n.contains("previous") || n.startsWith("page");
    }

    /** Admin buttons that would stop the crawl itself (jail, ban, vanish, stop the server...): not clicked. */
    private static boolean isDangerous(String name) {
        String n = name.toLowerCase(Locale.ROOT);
        for (String word : List.of("imprison", "jail", "ban", "kick", "mute", "freeze", "vanish", "wipe", "delete", "reset", "shut", "restart",
                "stop", "kill", "purge", "deop", "whitelist", "gamemode", "teleport", "tp ", "invsee", "clear", "confirm", "save recipe", "✔")) {
            if (n.contains(word)) {
                return true;
            }
        }
        return false;
    }

    /** Titles like "Auctions - Weapons" and "Auctions - Armor" are tabs of one screen. */
    private static boolean sameFamily(String a, String b) {
        int i = a.indexOf(" - ");
        int j = b.indexOf(" - ");
        return i > 0 && j > 0 && a.substring(0, i).equals(b.substring(0, j));
    }

    private static boolean isClose(String name) {
        String n = name.toLowerCase(Locale.ROOT);
        return n.contains("close") || n.contains("exit") || n.equals("cancel") || n.contains("\u2715");
    }

    /** Opens the root again and clicks along the recorded path; false if a step did not lead to a screen. */
    private boolean replay(List<Integer> path, String expectTitle) {
        closeAnyScreen();
        cmd(crawlRoot);
        if (!waitForContainer2(120)) {
            return false;
        }
        if (!waitForMenuContents()) return false;
        for (int slot : path) {
            if (!inContainer()) {
                return false;
            }
            for (int i = 0; i < 120 && items().stream().noneMatch(item -> item.slot() == slot); i++) ctx.waitTick();
            String before = screenTitle();
            String beforeKey = menuKey(before);
            ctx.runOnClient(client -> {
                AbstractContainerMenu menu = ((AbstractContainerScreen<?>) client.gui.screen()).getMenu();
                client.gameMode.handleContainerInput(menu.containerId, slot, 0, ContainerInput.PICKUP, client.player);
            });
            ctx.waitTicks(6);
            if (inContainer() && menuKey(screenTitle()).equals(beforeKey)) waitForScreenChange(before, 40);
            if (!waitForMenuContents()) return false;
        }
        return inContainer() && screenTitle().equals(expectTitle);
    }

    private void crawl(List<Integer> path, String title, int depth) {
        if (!waitForMenuContents()) {
            finding("menu contents did not arrive for '" + title + "'");
            return;
        }
        String visitKey = menuKey(title);
        if (!crawlVisited.add(visitKey)) {
            return;
        }
        ctx.takeScreenshot("menu-" + String.format("%03d", crawlVisited.size()) + "-" + title.replaceAll("[^A-Za-z0-9]+", "_"));
        List<Item> candidates = items();
        int tried = 0;
        int descended = 0;
        for (Item item : candidates) {
            if (crawlClicks >= CRAWL_MAX_CLICKS || tried >= CRAWL_MAX_SLOTS_PER_SCREEN) {
                break;
            }
            if (isClose(item.name()) || (depth > 0 && isBack(item.name())) || isPager(item.name()) || isDangerous(item.name())) {
                continue;
            }
            if (title.equals("Confirm") && item.name().toLowerCase(Locale.ROOT).contains("cancel")) {
                continue; // a dialog's Cancel is its way back, tried when the dialog was entered
            }
            tried++;
            if (!inContainer() || !menuKey(screenTitle()).equals(visitKey)) {
                if (!replay(path, title)) {
                    finding("could not get back to '" + title + "' to try '" + item.name() + "'");
                    return;
                }
            }
            crawlClicks++;
            ctx.runOnClient(client -> {
                AbstractContainerMenu menu = ((AbstractContainerScreen<?>) client.gui.screen()).getMenu();
                client.gameMode.handleContainerInput(menu.containerId, item.slot(), 0, ContainerInput.PICKUP, client.player);
            });
            ctx.waitTicks(6);
            String after = waitForScreenChange(title, 14);
            if (after.equals(title)) {
                continue; // an action or a refresh, not a new screen
            }
            if (after.equals("<closed>")) {
                closeAnyScreen();
                continue;
            }
            if (sameFamily(title, after)) {
                // tabs of one screen ("Auctions - Weapons", "Auctions - Armor"): siblings, not parent and child
                System.out.println("CLIENTTEST MENU tab '" + title + "' --[" + item.name() + "]--> '" + after + "'");
                continue;
            }
            crawlEdges.add("'" + title + "' --[" + item.name() + "]--> '" + after + "'");
            System.out.println("CLIENTTEST MENU edge '" + title + "' --[" + item.name() + "]--> '" + after + "'");
            String afterKey = menuKey(after);
            // the way back
            Item back = null;
            // Open-screen and content packets are distinct; a title alone does not mean its controls have arrived.
            for (int wait = 0; wait < 120 && back == null; wait++) {
                for (Item candidate : items()) {
                    if (isBack(candidate.name()) || (after.equals("Confirm") && candidate.name().toLowerCase(Locale.ROOT).contains("cancel"))) {
                        back = candidate;
                        break;
                    }
                }
                if (back == null) ctx.waitTick();
            }
            if (back == null) {
                dump("missing Back after waiting for content");
                finding("'" + after + "' (opened from '" + title + "' via '" + item.name() + "') has no back button");
            } else {
                Item backItem = back;
                ctx.runOnClient(client -> {
                    AbstractContainerMenu menu = ((AbstractContainerScreen<?>) client.gui.screen()).getMenu();
                    client.gameMode.handleContainerInput(menu.containerId, backItem.slot(), 0, ContainerInput.PICKUP, client.player);
                });
                ctx.waitTicks(6);
                String where = waitForScreenChange(after, 40);
                if (inContainer()) {
                    waitForMenuContents();
                    where = screenTitle();
                }
                if (!menuKey(where).equals(visitKey)) {
                    finding("Back on '" + after + "' (opened from '" + title + "' via '" + item.name() + "') goes to '" + where
                            + "' instead of '" + title + "'");
                }
            }
            if (depth + 1 < CRAWL_MAX_DEPTH && !crawlVisited.contains(afterKey) && descended < descendLimit(depth)) {
                descended++;
                List<Integer> deeper = new ArrayList<>(path);
                deeper.add(item.slot());
                if (replay(deeper, after)) {
                    crawl(deeper, after, depth + 1);
                } else {
                    finding("could not re-enter '" + after + "' from '" + title + "' via '" + item.name() + "' to look inside it");
                }
            }
        }
        // Main-page and original set-row pagers use different footer slots. Both are sibling pages.
        if (crawlClicks < CRAWL_MAX_CLICKS && replay(path, title) && menuKey(title).equals(visitKey)) {
            int footer = ctx.computeOnClient(client -> ((AbstractContainerScreen<?>) client.gui.screen()).getMenu().slots.size() - 45);
            Item next = items().stream().filter(item -> item.slot() >= footer
                    && item.name().replaceAll("§[0-9a-fk-orA-FK-OR]", "").toLowerCase(Locale.ROOT).startsWith("next")).findFirst().orElse(null);
            if (next != null && turnPage(next.slot())) {
                List<Integer> nextPage = new ArrayList<>(path);
                nextPage.add(next.slot());
                crawl(nextPage, title, depth);
            }
        }
    }

    /** Script item 12: the server runs with power.black_flash.chance 1.0; a punch right after a cast is a Black Flash. */
    private void blackFlash() {
        cmd("gamemode survival");
        cmd("tp @s ~ ~ ~ 0 0");
        cmd("kill @e[type=zombie]");
        equip("Barrier");
        // the first click of a window that does not hold the mouse only grabs it
        ctx.getInput().pressMouse(0);
        ctx.waitTicks(330);
        zombieAt(2);
        int mark = mark();
        cast();
        check("a cast before the punch (" + since(mark) + ")", saw(mark, "Barrier", true));
        float h0 = zombieHealth();
        String aimed = ctx.computeOnClient(client -> String.valueOf(client.hitResult == null ? "none" : client.hitResult.getType()));
        System.out.println("CLIENTTEST crosshair is on: " + aimed);
        mark = mark();
        // the client's own attack path (what a left click does), because the test window does not hold the mouse
        ctx.runOnClient(client -> {
            if (client.hitResult instanceof net.minecraft.world.phys.EntityHitResult hit) {
                client.gameMode.attack(client.player, hit.getEntity());
            }
        });
        ctx.waitTicks(6);
        boolean flash = waitFor(mark, "BLACK FLASH", true, 20);
        check("a punch right after a cast is a Black Flash (" + since(mark) + ")", flash);
        ctx.takeScreenshot("23-black-flash");
        float h1 = zombieHealth();
        check("the Black Flash hurt the zombie (" + h0 + " to " + h1 + ")", h1 < h0);
    }
}
