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
        String wanted = System.getProperty("powertest.phases", "permission,setup,tree,grants,domain,combat,death,regression");
        try {
            for (String phase : wanted.split(",")) {
                switch (phase.trim()) {
                    case "permission" -> phase("permission", this::permission);
                    case "setup" -> phase("setup", this::setup);
                    case "tree" -> phase("school and tree through the screens", this::schoolAndTree);
                    case "grants" -> phase("grants and the techniques screen", this::grants);
                    case "domain" -> phase("Domain, and vows while it runs", this::domainAndVows);
                    case "combat" -> phase("Blue, Red, Cursed Blast, Reverse, Barrier", this::combat);
                    case "death" -> phase("death", this::death);
                    case "blackflash" -> phase("Black Flash", this::blackFlash);
                    case "regression" -> phase("older commands still answer", this::regression);
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
        } catch (RuntimeException e) {
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
        int mark = mark();
        cmd("poweradmin");
        boolean refused = waitFor(mark, "Unknown or incomplete", null, 60);
        check("a non-operator gets no /poweradmin (" + since(mark) + ")", refused);

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

    /** Script item 1: choose a school and buy the whole spine with real clicks. */
    private void schoolAndTree() {
        check("the Powers hub opens from the menu", openHub());
        ctx.takeScreenshot("02-hub-fresh");
        hover("Cursed Energy", false, "02b-hub-energy-tooltip");
        hover("How to Cast", false, "02c-hub-how-to-cast-tooltip");
        int mark = mark();
        check("the school tile opens the school list", go("School", true, "School"));
        ctx.takeScreenshot("03-school-list");
        hover("Radiance", false, "03b-school-radiance-tooltip");
        hover("Chronomancy", false, "03c-school-dormant-tooltip");
        check("choosing a school asks for confirmation", go("Radiance", false, "Confirm"));
        ctx.takeScreenshot("04-confirm");
        clickSlot(11);
        boolean chose = waitFor(mark, "You chose", false, 40);
        check("the choice is confirmed (" + since(mark) + ")", chose);

        check("the school tile now opens the tree", openHub() && go("School", true, "School Tree"));
        ctx.takeScreenshot("05-tree-empty");
        hover("Next Step", false, "05b-tree-next-step-tooltip");
        hover("Radiance Path 1", true, "05c-tree-node1-tooltip");
        for (int i = 1; i <= 10; i++) {
            mark = mark();
            boolean asked = go("Radiance Path " + i, true, "Confirm");
            if (asked) {
                clickSlot(11);
            }
            boolean unlocked = asked && waitFor(mark, "Unlocked", false, 40);
            check("buy node " + i + " (" + since(mark) + ")", unlocked);
            if (i == 4) {
                ctx.takeScreenshot("06-tree-midway");
            }
        }
        ctx.takeScreenshot("07-tree-done");
        hover("Radiance Path 10", true, "07b-tree-capstone-tooltip");
        boolean tier4 = openHub() && loreOf("School", true).stream().anyMatch(l -> l.contains("Tier 4 of 4"));
        check("the hub shows tier 4", tier4);
        ctx.takeScreenshot("08-hub-tier4");
        closeScreen();
    }

    private void grants() {
        for (String technique : List.of("BLUE", "RED", "CURSED_BLAST")) {
            int mark = mark();
            cmd("poweradmin grant PowerTester " + technique);
            boolean granted = waitFor(mark, "Granted", false, 40);
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

        // item 6: the armor vow
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
        check("the Barrier absorbs a hit (" + b0 + " to " + b1 + ", " + since(mark) + ")", b1 >= b0 - 0.2f);
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
        boolean again = saw(mark, "Barrier", true) && !saw(mark, "On cooldown", true);
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
