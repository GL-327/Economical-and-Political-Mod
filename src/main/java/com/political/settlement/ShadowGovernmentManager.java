package com.political.settlement;
import com.political.DataManager;
import com.political.PoliticalServer;



import com.political.economy.CoinManager;
import com.political.party.PoliticalPartyManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Shadow Government — the unelected power that sits alongside (and nominally "above")
 * an elected Chair.
 *
 * <p>Membership is <b>emergent, not appointed</b>: influence is earned through wealth,
 * party standing and prior office, so shadow seats reflect who actually holds power in a
 * polity rather than who was voted in. Shadow figures get a real, permanent voice in war
 * decisions — but by design their combined weight is deliberately smaller than that of the
 * elected Chair and Vice Chair (see {@link WarManager.WarRole}). They can steer a war
 * council; they cannot win one against a united elected leadership.</p>
 */
public final class ShadowGovernmentManager {

    private ShadowGovernmentManager() {}

    /** How a shadow figure came by their influence. */
    public enum ShadowSeat {
        FINANCIER("Financier", ChatFormatting.GOLD, "Wealth beyond the treasury's reach."),
        PARTY_BOSS("Party Boss", ChatFormatting.DARK_PURPLE, "Commands the ruling party machine."),
        FORMER_CHAIR("Former Chair", ChatFormatting.GRAY, "Out of office, never out of power."),
        SPYMASTER("Spymaster", ChatFormatting.DARK_GRAY, "Knows what the Chair does not.");

        public final String displayName;
        public final ChatFormatting color;
        public final String flavor;

        ShadowSeat(String displayName, ChatFormatting color, String flavor) {
            this.displayName = displayName;
            this.color = color;
            this.flavor = flavor;
        }
    }

    public record ShadowMember(String uuid, ShadowSeat seat, long influence) {}

    /** polityId -> ordered shadow roster (highest influence first). */
    private static final Map<String, List<ShadowMember>> ROSTERS = new HashMap<>();

    /** Players who have ever held the Chair, per polity — they keep influence afterwards. */
    private static Map<String, List<String>> formerChairs = new HashMap<>();

    private static Path file;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** How many shadow seats a polity may have. Kept small so the bloc stays a minority. */
    private static final int MAX_SEATS = 4;

    // ── persistence ─────────────────────────────────────────────────────────

    public static void initialize(Path worldRoot) {
        file = worldRoot.resolve("shadow_government.json");
        if (Files.exists(file)) {
            try (Reader r = Files.newBufferedReader(file)) {
                Map<String, List<String>> loaded = GSON.fromJson(r,
                        new TypeToken<Map<String, List<String>>>() {}.getType());
                if (loaded != null) formerChairs = loaded;
            } catch (Exception e) {
                PoliticalServer.LOGGER.warn("Failed to load shadow_government.json", e);
            }
        }
    }

    public static void save() {
        if (file == null) return;
        try (Writer w = Files.newBufferedWriter(file)) {
            GSON.toJson(formerChairs, w);
        } catch (Exception e) {
            PoliticalServer.LOGGER.warn("Failed to save shadow_government.json", e);
        }
    }

    /** Record an outgoing Chair so they retain shadow influence. */
    public static void recordFormerChair(String polityId, String uuid) {
        if (polityId == null || uuid == null) return;
        List<String> list = formerChairs.computeIfAbsent(polityId, k -> new ArrayList<>());
        list.remove(uuid);
        list.add(0, uuid);
        while (list.size() > 5) list.remove(list.size() - 1);
        save();
    }

    // ── roster construction ─────────────────────────────────────────────────

    private static int rebuildCounter = 0;

    /** Recomputes every polity's shadow roster periodically (influence shifts over time). */
    public static void tick(MinecraftServer server) {
        if (server == null) return;
        if (++rebuildCounter < 600) return;  // ~30s
        rebuildCounter = 0;

        for (Settlement s : SettlementManager.getAllSettlements()) {
            if (!s.tierEnum().isAtLeast(SettlementTier.METROPOLIS)) continue;
            ROSTERS.put(s.id, buildRoster(s));
        }
    }

    /** The shadow roster for a polity, rebuilt on demand if not cached. */
    public static List<ShadowMember> rosterOf(String polityId) {
        List<ShadowMember> cached = ROSTERS.get(polityId);
        if (cached != null) return cached;
        Settlement s = SettlementManager.get(polityId);
        if (s == null) return List.of();
        List<ShadowMember> built = buildRoster(s);
        ROSTERS.put(polityId, built);
        return built;
    }

    private static List<ShadowMember> buildRoster(Settlement s) {
        // Elected officials are excluded — you cannot sit in the shadows and the throne.
        Map<String, String> leaders = s.leaderMap();
        java.util.Set<String> elected = new java.util.HashSet<>();
        if (leaders != null) elected.addAll(leaders.values());

        Map<String, ShadowMember> best = new LinkedHashMap<>();

        // 1. Financiers — the wealthiest registered citizens.
        List<String> registered = new ArrayList<>(s.registered());
        registered.sort((a, b) -> Long.compare(CoinManager.getCoinsLong(b), CoinManager.getCoinsLong(a)));
        for (String uuid : registered) {
            if (elected.contains(uuid)) continue;
            long coins = CoinManager.getCoinsLong(uuid);
            if (coins < 100_000L) break;               // pocket change buys no influence
            best.put(uuid, new ShadowMember(uuid, ShadowSeat.FINANCIER, coins / 1000L));
            if (best.size() >= 2) break;               // at most two financiers
        }

        // 2. Party Boss — the leader of the ruling party, if they are a citizen here.
        String rulingParty = PoliticalPartyManager.getRulingPartyId();
        if (rulingParty != null) {
            String bossUuid = PoliticalPartyManager.getPartyLeaderUuid(rulingParty);
            if (bossUuid != null && !elected.contains(bossUuid) && s.registered().contains(bossUuid)) {
                best.put(bossUuid, new ShadowMember(bossUuid, ShadowSeat.PARTY_BOSS, 750L));
            }
        }

        // 3. Former Chairs — out of office, still owed favours.
        for (String uuid : formerChairs.getOrDefault(s.id, List.of())) {
            if (elected.contains(uuid) || best.containsKey(uuid)) continue;
            best.put(uuid, new ShadowMember(uuid, ShadowSeat.FORMER_CHAIR, 500L));
            break;                                     // only the most recent
        }

        // 4. Spymaster — the highest-charisma citizen NPC's patron stands in for the
        //    intelligence apparatus when no player has claimed the seat.
        if (best.size() < MAX_SEATS) {
            for (String uuid : registered) {
                if (elected.contains(uuid) || best.containsKey(uuid)) continue;
                best.put(uuid, new ShadowMember(uuid, ShadowSeat.SPYMASTER, 300L));
                break;
            }
        }

        List<ShadowMember> out = new ArrayList<>(best.values());
        out.sort((a, b) -> Long.compare(b.influence(), a.influence()));
        while (out.size() > MAX_SEATS) out.remove(out.size() - 1);
        return out;
    }

    public static boolean isShadowMember(String polityId, String uuid) {
        if (uuid == null) return false;
        for (ShadowMember m : rosterOf(polityId)) {
            if (uuid.equals(m.uuid())) return true;
        }
        return false;
    }

    public static ShadowSeat seatOf(String polityId, String uuid) {
        for (ShadowMember m : rosterOf(polityId)) {
            if (uuid.equals(m.uuid())) return m.seat();
        }
        return null;
    }

    // ── display ─────────────────────────────────────────────────────────────

    public static void printRoster(ServerPlayer player, Settlement s) {
        List<ShadowMember> roster = rosterOf(s.id);
        player.sendSystemMessage(Component.literal("§8§l━━━ Shadow Government of " + s.name + " ━━━"));
        if (roster.isEmpty()) {
            player.sendSystemMessage(Component.literal("§8No shadow power has taken root here."));
            player.sendSystemMessage(Component.literal("§8Influence comes from wealth, party rank and past office."));
            return;
        }
        for (ShadowMember m : roster) {
            player.sendSystemMessage(Component.literal("  ")
                    .append(Component.literal(m.seat().displayName).withStyle(m.seat().color))
                    .append(Component.literal(": §f" + DataManager.getPlayerName(m.uuid())
                            + " §8(influence " + m.influence() + ")")));
            player.sendSystemMessage(Component.literal("    §8" + m.seat().flavor));
        }
        player.sendSystemMessage(Component.literal("§8They hold "
                + WarManager.WarRole.SHADOW.weight + " war-council votes each — real weight,"));
        player.sendSystemMessage(Component.literal("§8but the elected Chair alone casts "
                + WarManager.WarRole.CHAIR.weight + "."));
    }
}
