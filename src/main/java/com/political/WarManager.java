package com.political;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wars between sovereign polities.
 *
 * <p>A world can hold several independent governments at once — every METROPOLIS+ settlement
 * elects its own Chair, and lesser settlements follow whichever is nearest. When two of those
 * governments fall out, they can go to war.</p>
 *
 * <h2>Who decides a war</h2>
 * War is never one person's choice. A motion goes to the <b>War Council</b>, where every
 * seat carries a weight ({@link WarRole}):
 * <pre>
 *   Chair            5   elected — the decisive voice
 *   Vice Chair       4   elected
 *   Prime Seat       3   High Council (Utopia+)
 *   Minister         2   High Council
 *   Shadow Govt      2   unelected, moderate say
 *   Constituent      1   elected leader of a follower city
 * </pre>
 * The elected Chair and Vice together cast 9 — more than a full shadow bench (4 seats × 2 = 8)
 * plus, in practice, enough to carry or block any motion they agree on. Unelected power can
 * tip a divided council; it cannot overrule a united elected leadership. The Chair also holds
 * a <b>veto</b>: a motion the Chair votes against cannot pass, whatever the arithmetic.
 */
public final class WarManager {

    private WarManager() {}

    // ── Council seats ───────────────────────────────────────────────────────

    public enum WarRole {
        CHAIR("Chair", 5, true),
        VICE_CHAIR("Vice Chair", 4, true),
        PRIME_SEAT("Prime Seat", 3, true),
        MINISTER("Minister", 2, true),
        SHADOW("Shadow Government", 2, false),
        CONSTITUENT("Constituent", 1, true);

        public final String displayName;
        public final int weight;
        /** Elected/appointed office vs. unelected influence. */
        public final boolean official;

        WarRole(String displayName, int weight, boolean official) {
            this.displayName = displayName;
            this.weight = weight;
            this.official = official;
        }
    }

    // ── State ───────────────────────────────────────────────────────────────

    public static class WarMotion {
        public String polityId;
        public String targetPolityId;
        public String proposerUuid;
        public long opensAtMs;
        public long closesAtMs;
        /** voter uuid -> true(yes)/false(no) */
        public Map<String, Boolean> votes = new HashMap<>();
        public boolean resolved = false;
    }

    public static class War {
        public String aId;
        public String bId;
        public long startedAtMs;
        public int scoreA;              // war score from kills/raids
        public int scoreB;
        public long plunderedA;         // coins taken BY a FROM b
        public long plunderedB;
        public String peaceProposedBy;  // polity id that has tabled peace
        public long peaceProposedAtMs;

        public boolean involves(String polityId) {
            return aId.equals(polityId) || bId.equals(polityId);
        }

        public String other(String polityId) {
            return aId.equals(polityId) ? bId : aId;
        }
    }

    private static class SaveFile {
        List<War> wars = new ArrayList<>();
        List<WarMotion> motions = new ArrayList<>();
    }

    private static List<War> wars = new ArrayList<>();
    private static List<WarMotion> motions = new ArrayList<>();

    private static Path file;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** How long a war motion stays open for votes. */
    private static final long MOTION_WINDOW_MS = 10 * 60 * 1000L;   // 10 minutes
    /** Minimum time between a war starting and peace being tabled. */
    private static final long MIN_WAR_MS = 20 * 60 * 1000L;         // 20 minutes

    // ── Persistence ─────────────────────────────────────────────────────────

    public static void initialize(Path worldRoot) {
        file = worldRoot.resolve("wars.json");
        if (Files.exists(file)) {
            try (Reader r = Files.newBufferedReader(file)) {
                SaveFile sf = GSON.fromJson(r, new TypeToken<SaveFile>() {}.getType());
                if (sf != null) {
                    wars = sf.wars != null ? sf.wars : new ArrayList<>();
                    motions = sf.motions != null ? sf.motions : new ArrayList<>();
                }
            } catch (Exception e) {
                PoliticalServer.LOGGER.warn("Failed to load wars.json", e);
            }
        }
    }

    public static void save() {
        if (file == null) return;
        try (Writer w = Files.newBufferedWriter(file)) {
            SaveFile sf = new SaveFile();
            sf.wars = wars;
            sf.motions = motions;
            GSON.toJson(sf, w);
        } catch (Exception e) {
            PoliticalServer.LOGGER.warn("Failed to save wars.json", e);
        }
    }

    // ── Council composition ─────────────────────────────────────────────────

    /** Every seat entitled to vote on this polity's war motions, with its weight. */
    public static Map<String, WarRole> councilOf(String polityId) {
        Map<String, WarRole> council = new HashMap<>();
        Settlement s = SettlementManager.get(polityId);
        if (s == null) return council;

        Map<String, String> leaders = s.leaderMap();
        if (leaders != null) {
            String chair = leaders.get("chair");
            String vice = leaders.get("vice");
            if (chair != null) council.put(chair, WarRole.CHAIR);
            if (vice != null) council.put(vice, WarRole.VICE_CHAIR);
        }

        // High Council sits above the Chair but does not outrank them on war.
        String prime = HighCouncilManager.getPrimeSeatUuid();
        if (prime != null && !council.containsKey(prime)) {
            council.put(prime, WarRole.PRIME_SEAT);
        }
        for (Map.Entry<HighCouncilManager.Ministry, String> e : HighCouncilManager.getMinisters().entrySet()) {
            if (!council.containsKey(e.getValue())) {
                council.put(e.getValue(), WarRole.MINISTER);
            }
        }

        // Unelected power: moderate, permanent, and outnumbered by design.
        for (ShadowGovernmentManager.ShadowMember m : ShadowGovernmentManager.rosterOf(polityId)) {
            if (!council.containsKey(m.uuid())) {
                council.put(m.uuid(), WarRole.SHADOW);
            }
        }

        // Constituents of follower cities get a small voice.
        for (Settlement follower : SettlementManager.getAllSettlements()) {
            if (follower.id.equals(polityId)) continue;
            if (!polityId.equals(follower.nearestMetropolisId)) continue;
            String c = follower.leaderMap() == null ? null : follower.leaderMap().get("constituent");
            if (c != null && !council.containsKey(c)) {
                council.put(c, WarRole.CONSTITUENT);
            }
        }
        return council;
    }

    public static WarRole roleOf(String polityId, String uuid) {
        return councilOf(polityId).get(uuid);
    }

    // ── Motions ─────────────────────────────────────────────────────────────

    /** The polity a player speaks for: the metropolis they are registered under. */
    public static Settlement polityOf(ServerPlayer player) {
        Settlement home = SettlementManager.getRegisteredSettlement(player.getUUID().toString());
        if (home == null) return null;
        if (home.tierEnum().isAtLeast(SettlementTier.METROPOLIS)) return home;
        return home.nearestMetropolisId == null ? null
                : SettlementManager.get(home.nearestMetropolisId);
    }

    public static boolean proposeWar(ServerPlayer player, Settlement target) {
        Settlement polity = polityOf(player);
        if (polity == null) {
            player.sendSystemMessage(Component.literal("§cYou do not belong to a sovereign polity."));
            return false;
        }
        if (target == null || target.id.equals(polity.id)) {
            player.sendSystemMessage(Component.literal("§cChoose another polity."));
            return false;
        }
        if (!target.tierEnum().isAtLeast(SettlementTier.METROPOLIS)) {
            player.sendSystemMessage(Component.literal("§cOnly METROPOLIS+ governments can be warred upon."));
            return false;
        }
        if (warBetween(polity.id, target.id) != null) {
            player.sendSystemMessage(Component.literal("§cYou are already at war with " + target.name + "."));
            return false;
        }
        WarRole role = roleOf(polity.id, player.getUUID().toString());
        if (role == null) {
            player.sendSystemMessage(Component.literal("§cOnly the War Council may table a motion."));
            return false;
        }
        for (WarMotion m : motions) {
            if (!m.resolved && m.polityId.equals(polity.id)) {
                player.sendSystemMessage(Component.literal("§cA war motion is already before the council."));
                return false;
            }
        }

        WarMotion motion = new WarMotion();
        motion.polityId = polity.id;
        motion.targetPolityId = target.id;
        motion.proposerUuid = player.getUUID().toString();
        motion.opensAtMs = System.currentTimeMillis();
        motion.closesAtMs = motion.opensAtMs + MOTION_WINDOW_MS;
        motion.votes.put(player.getUUID().toString(), true);   // proposing is a yes
        motions.add(motion);
        save();

        MinecraftServer server = ((ServerLevel) player.level()).getServer();
        broadcast(server, Component.literal("⚔ §c§lWAR MOTION §r§7— " + polity.name
                + " debates war on §f" + target.name));
        broadcast(server, Component.literal("§7Tabled by " + role.displayName + " §f"
                + DataManager.getPlayerName(motion.proposerUuid)
                + "§7. Council has 10 minutes. §8/war vote yes|no"));
        return true;
    }

    public static boolean castVote(ServerPlayer player, boolean yes) {
        Settlement polity = polityOf(player);
        if (polity == null) return false;
        WarMotion motion = openMotionFor(polity.id);
        if (motion == null) {
            player.sendSystemMessage(Component.literal("§cNo war motion is before your council."));
            return false;
        }
        WarRole role = roleOf(polity.id, player.getUUID().toString());
        if (role == null) {
            player.sendSystemMessage(Component.literal("§cYou hold no seat on the War Council."));
            return false;
        }
        motion.votes.put(player.getUUID().toString(), yes);
        save();
        player.sendSystemMessage(Component.literal("§7Vote recorded as " + role.displayName
                + " §8(weight " + role.weight + ")§7: " + (yes ? "§aFOR" : "§cAGAINST")));
        return true;
    }

    /** The open (unresolved) war motion for a polity, or null. Used by WarGui. */
    public static WarMotion openMotionFor(String polityId) {
        for (WarMotion m : motions) {
            if (!m.resolved && m.polityId.equals(polityId)) return m;
        }
        return null;
    }

    /** Weighted tally. Returns {yes, no, chairVetoed}. */
    public static int[] tally(WarMotion motion) {
        Map<String, WarRole> council = councilOf(motion.polityId);
        int yes = 0, no = 0, veto = 0;
        for (Map.Entry<String, Boolean> v : motion.votes.entrySet()) {
            WarRole role = council.get(v.getKey());
            if (role == null) continue;                 // lost their seat since voting
            if (v.getValue()) yes += role.weight;
            else {
                no += role.weight;
                if (role == WarRole.CHAIR) veto = 1;    // the Chair can always block
            }
        }
        return new int[]{yes, no, veto};
    }

    // ── Tick: resolve motions, decay peace offers ───────────────────────────

    private static int tickCounter = 0;

    public static void tick(MinecraftServer server) {
        if (server == null) return;
        if (++tickCounter < 100) return;   // ~5s
        tickCounter = 0;

        long now = System.currentTimeMillis();
        boolean dirty = false;

        for (WarMotion m : new ArrayList<>(motions)) {
            if (m.resolved || now < m.closesAtMs) continue;
            resolveMotion(server, m);
            dirty = true;
        }
        motions.removeIf(m -> m.resolved && now - m.closesAtMs > 60_000L);

        if (dirty) save();
    }

    private static void resolveMotion(MinecraftServer server, WarMotion motion) {
        motion.resolved = true;
        Settlement polity = SettlementManager.get(motion.polityId);
        Settlement target = SettlementManager.get(motion.targetPolityId);
        if (polity == null || target == null) return;

        int[] t = tally(motion);
        int yes = t[0], no = t[1];
        boolean vetoed = t[2] == 1;

        if (vetoed) {
            broadcast(server, Component.literal("§7The Chair of §f" + polity.name
                    + " §7vetoed the motion for war on §f" + target.name + "§7."));
            return;
        }
        if (yes <= no) {
            broadcast(server, Component.literal("§7The council of §f" + polity.name
                    + " §7rejected war on §f" + target.name + "§7. §8(" + yes + " for, " + no + " against)"));
            return;
        }

        declareWar(server, polity, target, yes, no);
    }

    private static void declareWar(MinecraftServer server, Settlement a, Settlement b, int yes, int no) {
        War war = new War();
        war.aId = a.id;
        war.bId = b.id;
        war.startedAtMs = System.currentTimeMillis();
        wars.add(war);
        save();

        broadcast(server, Component.literal("⚔ §4§lWAR DECLARED"));
        broadcast(server, Component.literal("§f" + a.name + " §7has declared war on §f" + b.name
                + "§7. §8(council " + yes + " for, " + no + " against)"));
        broadcast(server, Component.literal("§7Citizens of both realms now strike harder at one"
                + " another, and trade between them has ended."));
    }

    // ── Queries used by combat / economy ────────────────────────────────────

    public static War warBetween(String polityA, String polityB) {
        for (War w : wars) {
            if ((w.aId.equals(polityA) && w.bId.equals(polityB))
                    || (w.aId.equals(polityB) && w.bId.equals(polityA))) {
                return w;
            }
        }
        return null;
    }

    public static boolean isAtWar(String polityId) {
        for (War w : wars) {
            if (w.involves(polityId)) return true;
        }
        return false;
    }

    /** True if these two players belong to polities currently at war. */
    public static boolean areEnemies(ServerPlayer a, ServerPlayer b) {
        if (a == null || b == null || a == b) return false;
        Settlement pa = polityOf(a);
        Settlement pb = polityOf(b);
        if (pa == null || pb == null || pa.id.equals(pb.id)) return false;
        return warBetween(pa.id, pb.id) != null;
    }

    /** Damage multiplier applied when citizens of warring polities fight each other. */
    public static float wartimeDamageMultiplier(ServerPlayer attacker, ServerPlayer victim) {
        return areEnemies(attacker, victim) ? 1.25f : 1.0f;
    }

    /** Trade embargo: a polity at war earns less from commerce. */
    public static double tradeMultiplier(String polityId) {
        return isAtWar(polityId) ? 0.75 : 1.0;
    }

    /** Records a wartime kill toward the war score. */
    public static void onWartimeKill(ServerPlayer killer, ServerPlayer victim) {
        Settlement pk = polityOf(killer);
        Settlement pv = polityOf(victim);
        if (pk == null || pv == null) return;
        War war = warBetween(pk.id, pv.id);
        if (war == null) return;

        if (war.aId.equals(pk.id)) war.scoreA += 1;
        else war.scoreB += 1;

        // Spoils: the victor's polity takes a cut of the loser's treasury.
        Settlement loser = SettlementManager.get(pv.id);
        Settlement winner = SettlementManager.get(pk.id);
        if (loser != null && winner != null && loser.treasury > 0) {
            long spoils = Math.min(loser.treasury, Math.max(50L, loser.treasury / 200));
            loser.treasury -= spoils;
            winner.treasury += spoils;
            if (war.aId.equals(pk.id)) war.plunderedA += spoils; else war.plunderedB += spoils;
            killer.sendSystemMessage(Component.literal("⚔ §7War spoils: §6+" + spoils
                    + " §7to " + winner.name), true);
        }
        SettlementManager.save();
        save();
    }

    // ── Peace ───────────────────────────────────────────────────────────────

    public static boolean proposePeace(ServerPlayer player) {
        Settlement polity = polityOf(player);
        if (polity == null) return false;
        WarRole role = roleOf(polity.id, player.getUUID().toString());
        if (role == null || (role != WarRole.CHAIR && role != WarRole.VICE_CHAIR
                && role != WarRole.PRIME_SEAT)) {
            player.sendSystemMessage(Component.literal(
                    "§cOnly the Chair, Vice Chair or Prime Seat may sue for peace."));
            return false;
        }
        War war = null;
        for (War w : wars) {
            if (w.involves(polity.id)) { war = w; break; }
        }
        if (war == null) {
            player.sendSystemMessage(Component.literal("§cYour polity is not at war."));
            return false;
        }
        if (System.currentTimeMillis() - war.startedAtMs < MIN_WAR_MS) {
            player.sendSystemMessage(Component.literal("§cIt is too early to sue for peace."));
            return false;
        }

        MinecraftServer server = ((ServerLevel) player.level()).getServer();
        if (war.peaceProposedBy != null && !war.peaceProposedBy.equals(polity.id)) {
            // The other side already offered — this accepts it.
            endWar(server, war, "negotiated peace");
            return true;
        }
        war.peaceProposedBy = polity.id;
        war.peaceProposedAtMs = System.currentTimeMillis();
        save();

        Settlement other = SettlementManager.get(war.other(polity.id));
        broadcast(server, Component.literal("🕊 §f" + polity.name + " §7sues for peace with §f"
                + (other != null ? other.name : "?") + "§7. Their leadership must accept. §8/war peace"));
        return true;
    }

    private static void endWar(MinecraftServer server, War war, String reason) {
        wars.remove(war);
        save();
        Settlement a = SettlementManager.get(war.aId);
        Settlement b = SettlementManager.get(war.bId);
        broadcast(server, Component.literal("🕊 §a§lPEACE §r§7— " + (a != null ? a.name : "?")
                + " and " + (b != null ? b.name : "?") + " have ended their war §8(" + reason + ")"));
        broadcast(server, Component.literal("§7Final war score: §f" + war.scoreA + " §7– §f" + war.scoreB));
    }

    // ── Reporting ───────────────────────────────────────────────────────────

    public static List<War> activeWars() {
        return new ArrayList<>(wars);
    }

    public static void printStatus(ServerPlayer player) {
        Settlement polity = polityOf(player);
        player.sendSystemMessage(Component.literal("§4§l━━━ War Status ━━━"));
        if (polity == null) {
            player.sendSystemMessage(Component.literal("§7You are not a citizen of any polity."));
        } else {
            WarRole role = roleOf(polity.id, player.getUUID().toString());
            player.sendSystemMessage(Component.literal("§7Your polity: §f" + polity.name));
            player.sendSystemMessage(Component.literal("§7Your council seat: "
                    + (role == null ? "§8none" : "§f" + role.displayName + " §8(weight "
                    + role.weight + (role.official ? ", elected/appointed" : ", unelected") + ")")));

            WarMotion motion = openMotionFor(polity.id);
            if (motion != null) {
                Settlement target = SettlementManager.get(motion.targetPolityId);
                int[] t = tally(motion);
                player.sendSystemMessage(Component.literal("§e⚔ Motion: war on §f"
                        + (target != null ? target.name : "?")));
                player.sendSystemMessage(Component.literal("§7  For §a" + t[0] + " §7| Against §c" + t[1]
                        + (t[2] == 1 ? " §4(CHAIR VETO)" : "")));
                long left = Math.max(0, motion.closesAtMs - System.currentTimeMillis()) / 1000;
                player.sendSystemMessage(Component.literal("§7  Closes in §f" + left + "s §8— /war vote yes|no"));
            }
        }

        if (wars.isEmpty()) {
            player.sendSystemMessage(Component.literal("§7No wars are being fought."));
            return;
        }
        player.sendSystemMessage(Component.literal("§7Active wars:"));
        for (War w : wars) {
            Settlement a = SettlementManager.get(w.aId);
            Settlement b = SettlementManager.get(w.bId);
            player.sendSystemMessage(Component.literal("  §c" + (a != null ? a.name : "?")
                    + " §7vs §c" + (b != null ? b.name : "?")
                    + " §8— score " + w.scoreA + "–" + w.scoreB));
        }
    }

    /** Full council breakdown, so players can see exactly who outweighs whom. */
    public static void printCouncil(ServerPlayer player, Settlement polity) {
        Map<String, WarRole> council = councilOf(polity.id);
        player.sendSystemMessage(Component.literal("§6§l━━━ War Council of " + polity.name + " ━━━"));
        if (council.isEmpty()) {
            player.sendSystemMessage(Component.literal("§7No council has formed."));
            return;
        }
        int officialWeight = 0, shadowWeight = 0;
        List<Map.Entry<String, WarRole>> entries = new ArrayList<>(council.entrySet());
        entries.sort((x, y) -> Integer.compare(y.getValue().weight, x.getValue().weight));
        for (Map.Entry<String, WarRole> e : entries) {
            WarRole r = e.getValue();
            if (r.official) officialWeight += r.weight; else shadowWeight += r.weight;
            player.sendSystemMessage(Component.literal("  §f" + DataManager.getPlayerName(e.getKey())
                    + " §7— " + (r.official ? "§b" : "§8") + r.displayName
                    + " §7(weight §f" + r.weight + "§7)"));
        }
        player.sendSystemMessage(Component.literal("§7Elected/appointed weight: §b" + officialWeight
                + " §7| Unelected weight: §8" + shadowWeight));
        player.sendSystemMessage(Component.literal(
                "§8The Chair alone holds a veto: any motion they oppose fails."));
    }

    private static void broadcast(MinecraftServer server, Component msg) {
        if (server == null) return;
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            p.sendSystemMessage(msg);
        }
    }
}
