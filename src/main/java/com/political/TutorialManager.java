package com.political;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The tutorial: what to tell somebody in the first two minutes.
 *
 * <p>This mod has ninety commands and a hundred screens. A player who logs in with no idea which of
 * them matters will conclude that none of them do, so the tutorial does not try to describe the
 * mod. It walks one path, the original one: there is a Chair, they are elected, they pick perks
 * everybody lives with, and here is how you get involved.</p>
 *
 * <p>Delivered a step at a time on a timer rather than as a wall of text, with each step clickable
 * so a player can act on it while it is still on screen. A tutorial you have to remember is a
 * tutorial nobody follows.</p>
 */
public final class TutorialManager {

    private TutorialManager() {}

    /** The gamerule that decides whether a first join gets the tutorial. */
    public static final String RULE_NAME = "doTutorialOnStart";

    /**
     * The rule itself.
     *
     * <p>Game rules became a registry in this version rather than a static table, so a custom one
     * is a real registry entry now instead of a call to a register helper that no longer exists.
     * Held as a field because the registry lookup is by object, not by name.</p>
     */
    public static GameRule<Boolean> RULE;

    /** Ticks between steps. Long enough to read, short enough not to feel stuck. */
    private static final int STEP_TICKS = 140;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path savePath;

    /** Who has already seen it, so a first join stays a first join. */
    private static final Set<UUID> SEEN = new HashSet<>();
    /** Who is partway through, and where. */
    private static final Map<UUID, Integer> RUNNING = new HashMap<>();
    private static final Map<UUID, Long> NEXT_AT = new HashMap<>();

    // ── The script ────────────────────────────────────────────────────────

    /**
     * One step: what it says, and what it offers to do about it.
     *
     * <p>Every step that mentions a screen carries the command to open it, so the line itself is
     * the button. Telling somebody to type something is how a tutorial gets ignored.</p>
     */
    private record Step(String title, String body, String command, String buttonText) {}

    private static final List<Step> STEPS = List.of(
            new Step("Welcome",
                    "This server runs on an elected government. Somebody holds the Chair, and what "
                            + "they choose affects everybody, including you.",
                    "politics", "Open the politics screen"),

            new Step("The Chair",
                    "One player holds the seat at a time. They are not a moderator and they cannot "
                            + "ban anybody. What they can do is pick the perks.",
                    "chair", "See who holds it"),

            new Step("Perks",
                    "Perks are server wide bonuses. Everybody gets them, whoever they voted for, "
                            + "so an election is an argument about what the whole server should be good at.",
                    "perks", "See what is in force"),

            new Step("Elections",
                    "Anybody can stand. Anybody can vote. Win, and the perks are yours to choose "
                            + "until the next one.",
                    "election", "See the election"),

            new Step("Making a living",
                    "Coins come from the bazaar, from bounties, and from the delves. You will want "
                            + "all three eventually. Start with whichever looks fun.",
                    "bazaar", "Open the bazaar"),

            new Step("Bounties",
                    "Sixteen bosses, from an outlaw zombie up to the Nanite Slayer. Each one drops "
                            + "gear you cannot get anywhere else.",
                    "bounty", "Open the bounty board"),

            new Step("Delves",
                    "Seven depths of dungeon, cleared for score. Find the secrets, they are worth "
                            + "more than the fighting.",
                    "delve", "Open the delves"),

            new Step("That is the lot",
                    "Everything else is reachable from the politics screen. If you only remember "
                            + "one command, remember that one.",
                    "politics", "Open it again")
    );

    // ── Lifecycle ─────────────────────────────────────────────────────────

    /**
     * Registers the gamerule.
     *
     * <p>Must run during mod init, before any world loads, because the game rule registry is frozen
     * once a level exists. Registering late throws and takes the server with it.</p>
     */
    public static void registerGameRule() {
        try {
            GameRule<Boolean> rule = new GameRule<>(
                    GameRuleCategory.PLAYER,
                    GameRuleType.BOOL,
                    com.mojang.brigadier.arguments.BoolArgumentType.bool(),
                    (visitor, r) -> visitor.visitBoolean(r),
                    com.mojang.serialization.Codec.BOOL,
                    b -> b ? 1 : 0,
                    Boolean.TRUE,
                    net.minecraft.world.flag.FeatureFlagSet.of());

            RULE = net.minecraft.core.Registry.register(
                    net.minecraft.core.registries.BuiltInRegistries.GAME_RULE,
                    net.minecraft.resources.Identifier.fromNamespaceAndPath(
                            "politicalserver", "do_tutorial_on_start"),
                    rule);
            PoliticalServer.LOGGER.info("Game rule registered: " + RULE_NAME);
        } catch (Throwable e) {
            // A rule that will not register is survivable. The command form still works and the
            // walk through simply never fires on its own, which is a smaller problem than a server
            // that will not start.
            PoliticalServer.LOGGER.error("Could not register " + RULE_NAME + ": " + e);
        }
    }

    public static void initialize(Path worldRoot) {
        savePath = worldRoot.resolve("political").resolve("tutorial.json");
        load();
    }

    /** Whether the gamerule is on right now. */
    public static boolean enabled(MinecraftServer server) {
        try {
            if (RULE == null) return false;
            var level = server.overworld();
            if (level == null) return false;
            Boolean v = level.getGameRules().get(RULE);
            return v != null && v;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean hasSeen(ServerPlayer player) {
        return SEEN.contains(player.getUUID());
    }

    // ── Running ───────────────────────────────────────────────────────────

    /** Called when somebody joins. Starts the tutorial only if it is on and they are new. */
    public static void onJoin(ServerPlayer player) {
        if (PoliticalServer.server == null) return;
        if (!enabled(PoliticalServer.server)) return;
        if (hasSeen(player)) return;
        start(player, false);
    }

    /**
     * Starts the tutorial for somebody.
     *
     * <p>The forced form ignores whether they have seen it, because an admin running this on a
     * player has already decided that question.</p>
     */
    public static void start(ServerPlayer player, boolean forced) {
        if (!forced && hasSeen(player)) return;
        RUNNING.put(player.getUUID(), 0);
        NEXT_AT.put(player.getUUID(), now() + 40L);
        SEEN.add(player.getUUID());
        save();

        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("  §6§lCIVILCRAFT"));
        player.sendSystemMessage(Component.literal("  §7A short walk through, "
                + STEPS.size() + " steps."));
        player.sendSystemMessage(Component.literal("  §8Type /tutorial stop to end it early."));
        player.sendSystemMessage(Component.literal(""));
    }

    public static void stop(ServerPlayer player) {
        RUNNING.remove(player.getUUID());
        NEXT_AT.remove(player.getUUID());
    }

    public static boolean isRunning(ServerPlayer player) {
        return RUNNING.containsKey(player.getUUID());
    }

    private static long now() {
        return PoliticalServer.server == null ? 0 : PoliticalServer.server.getTickCount();
    }

    /** Advances anybody mid tutorial. */
    public static void tick(MinecraftServer server) {
        if (RUNNING.isEmpty()) return;
        if (server.getTickCount() % 10 != 0) return;

        for (UUID id : new ArrayList<>(RUNNING.keySet())) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player == null) {
                // Left mid walkthrough. Dropped rather than kept, so it does not resume days later
                // in the middle of something else.
                RUNNING.remove(id);
                NEXT_AT.remove(id);
                continue;
            }
            long due = NEXT_AT.getOrDefault(id, 0L);
            if (server.getTickCount() < due) continue;

            int step = RUNNING.getOrDefault(id, 0);
            if (step >= STEPS.size()) {
                finish(player);
                continue;
            }
            show(player, STEPS.get(step), step);
            RUNNING.put(id, step + 1);
            NEXT_AT.put(id, (long) server.getTickCount() + STEP_TICKS);
        }
    }

    /**
     * Prints one step.
     *
     * <p>The button is a real click event running the command, so the player never has to type
     * anything or remember where they were.</p>
     */
    private static void show(ServerPlayer player, Step step, int index) {
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("  §6§l" + step.title()
                + " §r§8(" + (index + 1) + "/" + STEPS.size() + ")"));

        for (String line : wrap(step.body(), 58)) {
            player.sendSystemMessage(Component.literal("  §7" + line));
        }

        if (step.command() != null && commandExists(step.command())) {
            player.sendSystemMessage(Component.literal("  ")
                    .append(Component.literal("[ " + step.buttonText() + " ]")
                            .withStyle(Style.EMPTY
                                    .withColor(net.minecraft.ChatFormatting.AQUA)
                                    .withBold(true)
                                    .withClickEvent(new ClickEvent.RunCommand("/" + step.command()))
                                    .withHoverEvent(new HoverEvent.ShowText(
                                            Component.literal("/" + step.command()))))));
        }
        player.sendSystemMessage(Component.literal(""));

        if (player.level() instanceof net.minecraft.server.level.ServerLevel level) {
            level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_CHIME.value(),
                    SoundSource.PLAYERS, 0.5f, 1.4f);
        }
    }

    private static void finish(ServerPlayer player) {
        stop(player);
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("  §a§lTHAT IS THE WALK THROUGH"));
        player.sendSystemMessage(Component.literal("  §7Run §f/tutorial §7any time to see it again."));
        player.sendSystemMessage(Component.literal(""));
        CoinManager.giveCoins(player, 50_000L);
        player.sendSystemMessage(Component.literal("  §6✦ §7Fifty thousand coins to start you off."));
    }

    /** Whether a command is registered, so a step never offers a button that does nothing. */
    private static boolean commandExists(String command) {
        try {
            if (PoliticalServer.server == null) return false;
            return PoliticalServer.server.getCommands().getDispatcher()
                    .getRoot().getChild(command.split(" ")[0]) != null;
        } catch (Exception e) {
            return false;
        }
    }

    /** Wraps prose to a width, on word boundaries, so chat does not cut a word in half. */
    private static List<String> wrap(String text, int width) {
        List<String> out = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            if (line.length() + word.length() + 1 > width && line.length() > 0) {
                out.add(line.toString());
                line.setLength(0);
            }
            if (line.length() > 0) line.append(' ');
            line.append(word);
        }
        if (line.length() > 0) out.add(line.toString());
        return out;
    }

    public static int stepCount() {
        return STEPS.size();
    }

    public static int seenCount() {
        return SEEN.size();
    }

    // ── Persistence ───────────────────────────────────────────────────────

    public static void save() {
        if (savePath == null) return;
        try {
            Files.createDirectories(savePath.getParent());
            JsonArray arr = new JsonArray();
            List<String> ids = new ArrayList<>();
            for (UUID u : SEEN) ids.add(u.toString());
            java.util.Collections.sort(ids);
            for (String id : ids) arr.add(id);
            try (Writer w = Files.newBufferedWriter(savePath, StandardCharsets.UTF_8)) {
                GSON.toJson(arr, w);
            }
        } catch (Exception e) {
            PoliticalServer.LOGGER.error("Could not save tutorial state: " + e.getMessage());
        }
    }

    private static void load() {
        SEEN.clear();
        if (savePath == null || !Files.exists(savePath)) return;
        try (Reader r = Files.newBufferedReader(savePath, StandardCharsets.UTF_8)) {
            JsonElement root = com.google.gson.JsonParser.parseReader(r);
            if (!root.isJsonArray()) return;
            for (JsonElement e : root.getAsJsonArray()) {
                try {
                    SEEN.add(UUID.fromString(e.getAsString()));
                } catch (Exception ignored) {
                    // A hand edited file with something that is not a uuid in it.
                }
            }
        } catch (Exception e) {
            PoliticalServer.LOGGER.error("Could not read tutorial state: " + e.getMessage());
        }
    }

    /** Forgets that somebody has seen it, so the next join shows it again. */
    public static void reset(UUID id) {
        SEEN.remove(id);
        save();
    }
}
