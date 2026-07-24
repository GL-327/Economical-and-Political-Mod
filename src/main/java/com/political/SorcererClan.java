package com.political;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * V7 Pillar A — a Sorcerer Clan: an inheritable bloodline that sits between the individual
 * sorcerer (V6) and settlement politics (V4).
 *
 * <p>A clan owns a signature {@link InheritedTechnique}, a shared treasury, a prestige score
 * that rises only from real deeds, and its own ranks. Distinct from a guild (an order of
 * convenience) and from a settlement (territory): a clan is a lineage.</p>
 *
 * <p>Gson-friendly: public fields, no-arg constructor, enums stored as their {@code name()}.</p>
 */
public class SorcererClan {

    /** Clan ranks, lowest weight first. Gates every privileged action. */
    public enum Rank {
        INITIATE("Initiate", "§8", 0),
        ADEPT("Adept", "§a", 1),
        ELDER("Elder", "§b", 2),
        HEAD("Head", "§6", 3);

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

        /** One step up, capped below HEAD — headship is only handed over explicitly. */
        public Rank up() {
            return switch (this) {
                case INITIATE -> ADEPT;
                case ADEPT -> ELDER;
                case ELDER, HEAD -> ELDER;
            };
        }

        public Rank down() {
            return switch (this) {
                case HEAD, ELDER -> ADEPT;
                case ADEPT -> INITIATE;
                case INITIATE -> INITIATE;
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
    public String id;                        // stable uuid string
    public String name = "Unnamed Clan";
    public String crest = "✦";               // a single symbol shown before the name
    public String founderUuid;
    public String motto = "";

    // ── membership: uuid -> Rank.name() ──
    public Map<String, String> members = new LinkedHashMap<>();
    /** uuids that have been invited and may accept. */
    public Set<String> invites = new LinkedHashSet<>();

    // ── economy / progression ──
    public long treasury = 0L;
    public long prestige = 0L;
    public int prestigeLevel = 1;

    // ── signature technique (see InheritedTechnique) ──
    public String signatureTechniqueId = InheritedTechnique.values()[0].id;

    // ── tie-ins ──
    /** Settlement id of the clan hall (must be TOWN+), or null. */
    public String hallSettlementId = null;
    public long hallDeclaredAt = 0L;

    /** Clan ids this clan is allied with / rivals of. Feuds live in {@link ClanWarBridge}. */
    public Set<String> allies = new LinkedHashSet<>();
    public Set<String> rivals = new LinkedHashSet<>();

    public long createdAt = 0L;

    /** Newest-last activity log, trimmed to {@link #LOG_LIMIT}. */
    public List<String> log = new ArrayList<>();

    public static final int LOG_LIMIT = 90;
    public static final int MAX_MEMBERS = 30;

    public SorcererClan() {}

    public SorcererClan(String id, String name, String crest, String founderUuid) {
        this.id = id;
        this.name = name;
        this.crest = crest;
        this.founderUuid = founderUuid;
        this.createdAt = System.currentTimeMillis();
        memberMap().put(founderUuid, Rank.HEAD.name());
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

    public Set<String> allySet() {
        if (allies == null) allies = new LinkedHashSet<>();
        return allies;
    }

    public Set<String> rivalSet() {
        if (rivals == null) rivals = new LinkedHashSet<>();
        return rivals;
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

    public String headUuid() {
        for (Map.Entry<String, String> e : memberMap().entrySet()) {
            if (Rank.parse(e.getValue()) == Rank.HEAD) return e.getKey();
        }
        return founderUuid;
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

    public InheritedTechnique signature() {
        InheritedTechnique t = InheritedTechnique.byId(signatureTechniqueId);
        return t == null ? InheritedTechnique.values()[0] : t;
    }

    public String displayNameString() {
        return (crest == null ? "✦" : crest) + " " + name;
    }
}
