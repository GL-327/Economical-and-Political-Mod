package com.political;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * V5 Pillar 2 — a Guild (or Order): a cross-settlement association of players.
 *
 * <p>Distinct from a political party (national) and from a settlement (territorial). A guild
 * has its own ranks, its own treasury, its own level/XP progression and its own perk tree.
 * Gson-friendly: public fields, no-arg constructor, enums stored as their {@code name()}.</p>
 */
public class Guild {

    /** Guild ranks, lowest weight first. Gates every privileged action. */
    public enum Rank {
        INITIATE("Initiate", "§8", 0),
        MEMBER("Member", "§7", 1),
        VETERAN("Veteran", "§a", 2),
        OFFICER("Officer", "§b", 3),
        MASTER("Master", "§6", 4);

        public final String displayName;
        public final String colorCode;
        public final int weight;

        Rank(String displayName, String colorCode, int weight) {
            this.displayName = displayName;
            this.colorCode = colorCode;
            this.weight = weight;
        }

        public boolean isAtLeast(Rank other) { return this.weight >= other.weight; }

        public String colored() { return colorCode + displayName; }

        /** One step up, capped below MASTER (mastery transfers explicitly, never by promotion). */
        public Rank up() {
            return switch (this) {
                case INITIATE -> MEMBER;
                case MEMBER -> VETERAN;
                case VETERAN -> OFFICER;
                case OFFICER, MASTER -> OFFICER;
            };
        }

        public Rank down() {
            return switch (this) {
                case MASTER, OFFICER -> VETERAN;
                case VETERAN -> MEMBER;
                case MEMBER, INITIATE -> INITIATE;
            };
        }

        public static Rank parse(String s) {
            if (s == null) return INITIATE;
            try {
                return Rank.valueOf(s);
            } catch (Exception e) {
                return INITIATE;
            }
        }
    }

    // ── identity ──
    public String id;                       // stable uuid string
    public String name = "Unnamed Guild";
    public String tag = "GLD";              // 2-5 chars, shown in rosters
    public String founderUuid;
    public String motto = "";

    // ── membership: uuid -> Rank.name() ──
    public Map<String, String> members = new LinkedHashMap<>();
    /** uuids that have been invited and may accept. */
    public Set<String> invites = new LinkedHashSet<>();

    // ── economy / progression ──
    public long treasury = 0L;
    public int level = 1;
    public long xp = 0L;
    /** Purchased perk ids (see {@link GuildPerks.Perk#id}). */
    public Set<String> perks = new LinkedHashSet<>();

    // ── tie-in ──
    /** Settlement id of the declared guild hall (must be TOWN+), or null. */
    public String hallSettlementId = null;
    public long hallDeclaredAt = 0L;

    public long createdAt = 0L;

    /** Newest-last activity log. Trimmed to {@link #LOG_LIMIT}. */
    public List<String> log = new ArrayList<>();

    public static final int LOG_LIMIT = 90;
    public static final int MAX_MEMBERS = 40;

    public Guild() {}

    public Guild(String id, String name, String tag, String founderUuid) {
        this.id = id;
        this.name = name;
        this.tag = tag;
        this.founderUuid = founderUuid;
        this.createdAt = System.currentTimeMillis();
        memberMap().put(founderUuid, Rank.MASTER.name());
    }

    // ── defensive accessors (Gson can leave collections null) ──

    public Map<String, String> memberMap() {
        if (members == null) members = new LinkedHashMap<>();
        return members;
    }

    public Set<String> inviteSet() {
        if (invites == null) invites = new LinkedHashSet<>();
        return invites;
    }

    public Set<String> perkSet() {
        if (perks == null) perks = new LinkedHashSet<>();
        return perks;
    }

    public List<String> logList() {
        if (log == null) log = new ArrayList<>();
        return log;
    }

    // ── membership helpers ──

    public boolean isMember(String uuid) {
        return uuid != null && memberMap().containsKey(uuid);
    }

    public Rank rankOf(String uuid) {
        if (uuid == null) return null;
        String r = memberMap().get(uuid);
        return r == null ? null : Rank.parse(r);
    }

    public void setRank(String uuid, Rank rank) {
        if (uuid == null || rank == null) return;
        memberMap().put(uuid, rank.name());
    }

    public int memberCount() { return memberMap().size(); }

    public String masterUuid() {
        for (Map.Entry<String, String> e : memberMap().entrySet()) {
            if (Rank.parse(e.getValue()) == Rank.MASTER) return e.getKey();
        }
        return founderUuid;
    }

    /** Members at or above the given rank, in insertion order. */
    public List<String> membersAtLeast(Rank rank) {
        List<String> out = new ArrayList<>();
        for (Map.Entry<String, String> e : memberMap().entrySet()) {
            if (Rank.parse(e.getValue()).isAtLeast(rank)) out.add(e.getKey());
        }
        return out;
    }

    /** Members sorted by rank descending, then by name. */
    public List<String> rosterSorted() {
        List<String> out = new ArrayList<>(memberMap().keySet());
        out.sort((a, b) -> {
            int ra = rankOf(a) == null ? 0 : rankOf(a).weight;
            int rb = rankOf(b) == null ? 0 : rankOf(b).weight;
            if (ra != rb) return Integer.compare(rb, ra);
            return DataManager.getPlayerName(a).compareToIgnoreCase(DataManager.getPlayerName(b));
        });
        return out;
    }

    public boolean hasPerk(String perkId) {
        return perkId != null && perkSet().contains(perkId);
    }

    public String coloredTag() {
        return "§8[§b" + (tag == null ? "?" : tag) + "§8]";
    }
}
