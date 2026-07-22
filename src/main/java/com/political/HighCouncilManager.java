package com.political;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The High Council — the tier of government unlocked by UTOPIA+ settlements.
 *
 * A Prime Seat is held by the leader of the highest-tier settlement, who appoints four
 * ministers. The council enacts {@link Edict}s: paid, timed, server-wide laws. Other
 * systems query {@link #isEdictActive} to apply their effects.
 */
public final class HighCouncilManager {

    private HighCouncilManager() {}

    // ── Ministries ──────────────────────────────────────────────────────────

    public enum Ministry {
        TREASURY("Ministry of Treasury", ChatFormatting.GOLD),
        DEFENSE("Ministry of Defense", ChatFormatting.RED),
        ARCANA("Ministry of Arcana", ChatFormatting.LIGHT_PURPLE),
        COMMERCE("Ministry of Commerce", ChatFormatting.GREEN);

        public final String displayName;
        public final ChatFormatting color;

        Ministry(String displayName, ChatFormatting color) {
            this.displayName = displayName;
            this.color = color;
        }
    }

    // ── Edicts ──────────────────────────────────────────────────────────────

    public enum Edict {
        PROSPERITY("Prosperity Edict", 500_000L, 6, ChatFormatting.GOLD,
                "Shops pay +15% when selling."),
        ARCANE_SANCTION("Arcane Sanction", 750_000L, 6, ChatFormatting.LIGHT_PURPLE,
                "Mana regenerates 50% faster server-wide."),
        CONSCRIPTION("Conscription", 400_000L, 4, ChatFormatting.RED,
                "Settlement citizens take up arms against hostiles."),
        GOLDEN_AGE("Golden Age", 2_000_000L, 8, ChatFormatting.YELLOW,
                "Settlement appeal grows twice as fast."),
        RADIANT_MANDATE("Radiant Mandate", 10_000_000L, 12, ChatFormatting.AQUA,
                "ELYSIUM only — regeneration and luck for every citizen of the realm.");

        public final String displayName;
        public final long cost;
        public final int durationHours;
        public final ChatFormatting color;
        public final String description;

        Edict(String displayName, long cost, int durationHours, ChatFormatting color, String description) {
            this.displayName = displayName;
            this.cost = cost;
            this.durationHours = durationHours;
            this.color = color;
            this.description = description;
        }

        public static Edict byName(String s) {
            if (s == null) return null;
            for (Edict e : values()) {
                if (e.name().equalsIgnoreCase(s) || e.displayName.equalsIgnoreCase(s)) return e;
            }
            return null;
        }
    }

    // ── Persisted state ─────────────────────────────────────────────────────

    public static class CouncilState {
        public String primeSeatUuid = null;
        public String primeSettlementId = null;
        /** Ministry name -> player uuid (or citizen uuid). */
        public Map<String, String> ministers = new HashMap<>();
        /** Edict name -> epoch ms when it expires. */
        public Map<String, Long> activeEdicts = new HashMap<>();
        public List<String> log = new ArrayList<>();
    }

    private static CouncilState state = new CouncilState();
    private static Path file;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void initialize(Path worldRoot) {
        file = worldRoot.resolve("high_council.json");
        if (Files.exists(file)) {
            try (Reader r = Files.newBufferedReader(file)) {
                CouncilState loaded = GSON.fromJson(r, new TypeToken<CouncilState>() {}.getType());
                if (loaded != null) {
                    state = loaded;
                    if (state.ministers == null) state.ministers = new HashMap<>();
                    if (state.activeEdicts == null) state.activeEdicts = new HashMap<>();
                    if (state.log == null) state.log = new ArrayList<>();
                }
            } catch (Exception e) {
                PoliticalServer.LOGGER.warn("Failed to load high_council.json", e);
            }
        }
    }

    public static void save() {
        if (file == null) return;
        try (Writer w = Files.newBufferedWriter(file)) {
            GSON.toJson(state, w);
        } catch (IOException e) {
            PoliticalServer.LOGGER.warn("Failed to save high_council.json", e);
        }
    }

    // ── Tick: expire edicts, keep the Prime Seat in sync ─────────────────────

    private static int tickCounter = 0;

    public static void tick(MinecraftServer server) {
        if (server == null) return;
        if (++tickCounter < 200) return; // ~10s
        tickCounter = 0;

        long now = System.currentTimeMillis();
        boolean changed = false;

        var it = state.activeEdicts.entrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            if (entry.getValue() <= now) {
                it.remove();
                changed = true;
                Edict e = Edict.byName(entry.getKey());
                broadcast(server, Component.literal("§8[High Council] §7The "
                        + (e != null ? e.displayName : entry.getKey()) + " has expired."));
            }
        }

        if (syncPrimeSeat(server)) changed = true;
        if (changed) save();
    }

    /** The Prime Seat belongs to the leader of the highest-tier UTOPIA+ settlement. */
    private static boolean syncPrimeSeat(MinecraftServer server) {
        Settlement best = null;
        for (Settlement s : SettlementManager.getAllSettlements()) {
            if (s.tier == null) continue;
            SettlementTier sTier = s.tierEnum();
            SettlementTier tier = sTier;
            if (tier.ordinal() < SettlementTier.UTOPIA.ordinal()) continue;
            if (best == null || tier.ordinal() > best.tierEnum().ordinal()
                    || (tier == best.tierEnum() && s.population > best.population)) {
                best = s;
            }
        }
        if (best == null) {
            if (state.primeSeatUuid != null) {
                state.primeSeatUuid = null;
                state.primeSettlementId = null;
                state.ministers.clear();
                return true;
            }
            return false;
        }
        String leader = best.ownerUuid;
        if (leader != null && !leader.equals(state.primeSeatUuid)) {
            state.primeSeatUuid = leader;
            state.primeSettlementId = best.id;
            log("Prime Seat passes to " + DataManager.getPlayerName(leader) + " of " + best.name);
            broadcast(server, Component.literal("§b§lHIGH COUNCIL §r§7— the Prime Seat now sits in §f"
                    + best.name + "§7."));
            return true;
        }
        return false;
    }

    // ── Queries used by other systems ────────────────────────────────────────

    public static boolean isEdictActive(Edict edict) {
        if (edict == null) return false;
        Long until = state.activeEdicts.get(edict.name());
        return until != null && until > System.currentTimeMillis();
    }

    public static long edictRemainingMs(Edict edict) {
        Long until = state.activeEdicts.get(edict.name());
        return until == null ? 0L : Math.max(0L, until - System.currentTimeMillis());
    }

    public static String getPrimeSeatUuid() {
        return state.primeSeatUuid;
    }

    public static String getPrimeSettlementId() {
        return state.primeSettlementId;
    }

    public static boolean isPrimeSeat(ServerPlayer player) {
        return player != null && player.getUUID().toString().equals(state.primeSeatUuid);
    }

    public static Map<Ministry, String> getMinisters() {
        Map<Ministry, String> out = new EnumMap<>(Ministry.class);
        for (Ministry m : Ministry.values()) {
            String uuid = state.ministers.get(m.name());
            if (uuid != null) out.put(m, uuid);
        }
        return out;
    }

    public static boolean isMinister(String uuid) {
        return uuid != null && state.ministers.containsValue(uuid);
    }

    public static List<String> getLog() {
        return new ArrayList<>(state.log);
    }

    // ── Council actions ──────────────────────────────────────────────────────

    public static boolean appointMinister(ServerPlayer actor, Ministry ministry, String targetUuid) {
        if (!isPrimeSeat(actor)) {
            actor.sendSystemMessage(Component.literal("§cOnly the Prime Seat may appoint ministers."));
            return false;
        }
        state.ministers.put(ministry.name(), targetUuid);
        log(DataManager.getPlayerName(targetUuid) + " appointed to " + ministry.displayName);
        actor.sendSystemMessage(Component.literal("§aAppointed §f"
                + DataManager.getPlayerName(targetUuid) + " §ato the " + ministry.displayName));
        save();
        return true;
    }

    /** Enact an edict, paid from the Prime settlement's treasury. */
    public static boolean enactEdict(ServerPlayer actor, Edict edict) {
        if (edict == null) return false;
        if (!isPrimeSeat(actor) && !isMinister(actor.getUUID().toString())) {
            actor.sendSystemMessage(Component.literal("§cOnly the High Council may enact edicts."));
            return false;
        }
        Settlement seat = state.primeSettlementId == null ? null
                : SettlementManager.get(state.primeSettlementId);
        if (seat == null) {
            actor.sendSystemMessage(Component.literal("§cThe Council has no seat."));
            return false;
        }
        if (edict == Edict.RADIANT_MANDATE && seat.tierEnum() != SettlementTier.ELYSIUM) {
            actor.sendSystemMessage(Component.literal("§cThe Radiant Mandate requires an ELYSIUM seat."));
            return false;
        }
        if (isEdictActive(edict)) {
            actor.sendSystemMessage(Component.literal("§cThat edict is already in force."));
            return false;
        }
        if (seat.treasury < edict.cost) {
            actor.sendSystemMessage(Component.literal("§cThe seat treasury needs §6"
                    + edict.cost + " §ccoins (has " + seat.treasury + ")."));
            return false;
        }
        seat.treasury -= edict.cost;
        state.activeEdicts.put(edict.name(),
                System.currentTimeMillis() + edict.durationHours * 3_600_000L);
        log(DataManager.getPlayerName(actor.getUUID().toString()) + " enacted " + edict.displayName);
        save();
        SettlementManager.save();

        MinecraftServer server = ((net.minecraft.server.level.ServerLevel) actor.level()).getServer();
        broadcast(server, Component.literal("§b§lHIGH COUNCIL")
                .append(Component.literal(" §7— ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(edict.displayName).withStyle(edict.color, ChatFormatting.BOLD))
                .append(Component.literal(" §7is now in force for " + edict.durationHours + "h.")));
        broadcast(server, Component.literal("  §7" + edict.description));
        return true;
    }

    public static boolean repealEdict(ServerPlayer actor, Edict edict) {
        if (!isPrimeSeat(actor)) {
            actor.sendSystemMessage(Component.literal("§cOnly the Prime Seat may repeal an edict."));
            return false;
        }
        if (state.activeEdicts.remove(edict.name()) == null) {
            actor.sendSystemMessage(Component.literal("§cThat edict is not active."));
            return false;
        }
        log(DataManager.getPlayerName(actor.getUUID().toString()) + " repealed " + edict.displayName);
        actor.sendSystemMessage(Component.literal("§eRepealed " + edict.displayName));
        save();
        return true;
    }

    /** Chat summary for /council. */
    public static void printStatus(ServerPlayer player) {
        player.sendSystemMessage(Component.literal("§b§l━━━ The High Council ━━━"));
        if (state.primeSeatUuid == null) {
            player.sendSystemMessage(Component.literal("§7No council sits. A §dUTOPIA§7 settlement is required."));
            return;
        }
        Settlement seat = state.primeSettlementId == null ? null
                : SettlementManager.get(state.primeSettlementId);
        player.sendSystemMessage(Component.literal("§7Prime Seat: §f"
                + DataManager.getPlayerName(state.primeSeatUuid)
                + (seat != null ? " §8of §7" + seat.name : "")));
        for (Ministry m : Ministry.values()) {
            String uuid = state.ministers.get(m.name());
            player.sendSystemMessage(Component.literal("  ")
                    .append(Component.literal(m.displayName).withStyle(m.color))
                    .append(Component.literal(": §f" + (uuid == null ? "§8vacant"
                            : DataManager.getPlayerName(uuid)))));
        }
        player.sendSystemMessage(Component.literal("§7Active edicts:"));
        boolean any = false;
        for (Edict e : Edict.values()) {
            if (!isEdictActive(e)) continue;
            any = true;
            long mins = edictRemainingMs(e) / 60_000L;
            player.sendSystemMessage(Component.literal("  ")
                    .append(Component.literal(e.displayName).withStyle(e.color))
                    .append(Component.literal(" §8— " + mins + "m left")));
        }
        if (!any) player.sendSystemMessage(Component.literal("  §8none"));
    }

    private static void log(String entry) {
        state.log.add(0, entry);
        while (state.log.size() > 50) state.log.remove(state.log.size() - 1);
    }

    private static void broadcast(MinecraftServer server, Component msg) {
        if (server == null) return;
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            p.sendSystemMessage(msg);
        }
    }

    public static String edictNames() {
        StringBuilder sb = new StringBuilder();
        for (Edict e : Edict.values()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(e.name().toLowerCase(Locale.ROOT));
        }
        return sb.toString();
    }
}
