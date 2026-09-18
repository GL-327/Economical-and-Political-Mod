package com.political.party;
import com.political.BuffProgressManager;
import com.political.Commission;
import com.political.CreditItem;
import com.political.DataManager;
import com.political.PoliticalServer;
import com.political.government.Treasury;



import com.political.economy.CoinManager;
import com.political.government.Perk;
import com.political.government.PerkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Items;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Political parties: budgets, support, defect cooldown, ruling-party elections, council votes on purchases, salaries,
 * ranks, party perks, wars, alliances, treasury logs, and weekly party events.
 */
public final class PoliticalPartyManager {

    public static final int CREATE_PARTY_CREDITS = 10_000;
    public static final long DEFECT_COOLDOWN_MS = 24L * 60L * 60L * 1000L;
    public static final long COUNCIL_VOTE_MS = 60L * 60L * 1000L;
    /** Coins donated -> support (1 support per this many coins). */
    public static final long COINS_PER_SUPPORT_POINT = 10_000L;
    private static final double SALARY_HOURLY_BUDGET_FRACTION = 0.008;
    private static long lastSalaryMs = 0L;
    private static final long SALARY_INTERVAL_MS = 60L * 60L * 1000L;

    // ================================================================
    //  RANK DEFINITIONS
    // ================================================================

    public static final List<String> RANK_ORDER = List.of(
            "Leader", "Vice Leader", "General", "Officer", "Member", "Recruit"
    );

    static int rankIndex(String rank) {
        int idx = RANK_ORDER.indexOf(rank);
        return idx < 0 ? RANK_ORDER.size() - 1 : idx;
    }

    // ================================================================
    //  PARTY PERK DEFINITIONS
    // ================================================================

    public static final class PartyPerkDef {
        public final String id;
        public final String displayName;
        public final String description;
        public final long cost;

        public PartyPerkDef(String id, String displayName, String description, long cost) {
            this.id = id;
            this.displayName = displayName;
            this.description = description;
            this.cost = cost;
        }
    }

    public static final List<PartyPerkDef> PERK_DEFS = List.of(
            new PartyPerkDef("coin_bonus", "Coin Tribute I", "Members earn ~1.2% more coins (party budget only)", 35_000_000L),
            new PartyPerkDef("prosperity_mark", "Coin Tribute II", "Members earn ~0.8% more coins (stacks lightly)", 55_000_000L),
            new PartyPerkDef("xp_bonus", "Scholar I", "Members earn ~1.2% more XP orbs", 40_000_000L),
            new PartyPerkDef("scholars_circle", "Scholar II", "Members earn ~0.8% more XP orbs", 60_000_000L),
            new PartyPerkDef("death_insurance", "Warden I", "~12% less coin loss on death", 70_000_000L),
            new PartyPerkDef("wardens_oath", "Warden II", "~8% less coin loss on death (stacks lightly)", 95_000_000L),
            new PartyPerkDef("fast_travel", "Waystone I", "~12% shorter /home cooldown", 50_000_000L),
            new PartyPerkDef("waystone_network", "Waystone II", "~10% shorter /home cooldown", 75_000_000L),
            new PartyPerkDef("shop_discount", "Quartermaster I", "~2% cheaper /shop buys", 65_000_000L),
            new PartyPerkDef("bulk_contract", "Quartermaster II", "~1.5% cheaper /shop buys", 90_000_000L),
            new PartyPerkDef("sell_bonus", "Broker I", "~2% better /shop sell prices", 55_000_000L),
            new PartyPerkDef("merchants_favor", "Broker II", "~1.5% better /shop sell prices", 80_000_000L),
            new PartyPerkDef("war_banner", "War Banner", "~2% more melee damage vs mobs", 100_000_000L),
            new PartyPerkDef("rangers_signal", "Ranger's Signal", "~2% more projectile damage", 100_000_000L)
    );

    public static PartyPerkDef getPerkDef(String id) {
        for (PartyPerkDef d : PERK_DEFS) {
            if (d.id.equals(id)) return d;
        }
        return null;
    }

    // ================================================================
    //  EVENT DEFINITIONS
    // ================================================================

    public static final long EVENT_DURATION_MS = 7L * 24L * 60L * 60L * 1000L;
    public static final long EVENT_REWARD_BUDGET = 5_000_000L;
    public static final List<String> EVENT_TYPES = List.of(
            "mob_kill_race", "mining_race", "donation_drive", "pvp_tournament"
    );

    public static String eventDisplayName(String eventType) {
        return switch (eventType) {
            case "mob_kill_race" -> "Mob Kill Race";
            case "mining_race" -> "Mining Race";
            case "donation_drive" -> "Donation Drive";
            case "pvp_tournament" -> "PvP Tournament";
            default -> eventType;
        };
    }

    // ================================================================
    //  TREASURY LOG ENTRY
    // ================================================================

    public static final class TreasuryLog {
        public final long timestampMs;
        public final String action;
        public final long amount;
        public final String detail;

        public TreasuryLog(long timestampMs, String action, long amount, String detail) {
            this.timestampMs = timestampMs;
            this.action = action;
            this.amount = amount;
            this.detail = detail;
        }
    }

    // ================================================================
    //  PARTY EVENT
    // ================================================================

    public static final class PartyEvent {
        public String eventType;
        public long startMs;
        public long endMs;
        public Map<String, Long> memberProgress = new LinkedHashMap<>();
        public boolean rewarded;
    }

    // ================================================================
    //  CORE DATA STRUCTURES
    // ================================================================

    public static final class PartyMember {
        public long support;
        public long joinedAtMs;

        public PartyMember() {}

        public PartyMember(long support, long joinedAtMs) {
            this.support = support;
            this.joinedAtMs = joinedAtMs;
        }
    }

    public static final class Party {
        public String id;
        public String displayName;
        public long budgetCoins;
        public String platform = "";
        public Map<String, PartyMember> members = new LinkedHashMap<>();

        public Party() {}

        public Party(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }
    }

    public static final class PerkProposal {
        public String partyId;
        public String perkId;
        public long costCoins;
        public long durationMs;
        public long deadlineMs;
        public Set<String> councilUuids = new HashSet<>();
        public Map<String, Boolean> votesYes = new HashMap<>();
    }

    private static final Map<String, Party> parties = new LinkedHashMap<>();
    private static final Map<String, String> playerParty = new HashMap<>();
    private static final Map<String, Long> lastDefectMs = new HashMap<>();
    private static final Set<String> completedPartyOnboarding = new HashSet<>();
    private static String rulingPartyId;
    private static boolean leaderElectionActive;
    private static long leaderElectionEndMs;
    private static final Map<String, String> leaderElectionVotes = new HashMap<>();
    private static final List<PerkProposal> pendingProposals = new ArrayList<>();

    // --- Ranks: partyId -> (uuid -> rank) ---
    private static final Map<String, Map<String, String>> partyRanks = new LinkedHashMap<>();

    // --- Party Perks: partyId -> set of perk IDs ---
    private static final Map<String, Set<String>> partyPerks = new LinkedHashMap<>();

    // --- Wars: partyId -> set of enemy partyIds ---
    private static final Map<String, Set<String>> activeWars = new LinkedHashMap<>();

    // --- Alliances: partyId -> set of allied partyIds ---
    private static final Map<String, Set<String>> alliances = new LinkedHashMap<>();

    // --- Treasury Logs: partyId -> recent log entries ---
    private static final Map<String, List<TreasuryLog>> treasuryLogs = new LinkedHashMap<>();

    // --- Peace proposals: partyId -> set of partyIds that proposed peace ---
    private static final Map<String, Set<String>> peacePending = new LinkedHashMap<>();

    // --- Alliance proposals: partyId -> set of partyIds that proposed alliance ---
    private static final Map<String, Set<String>> alliancePending = new LinkedHashMap<>();

    // --- Party Events: partyId -> active event ---
    private static final Map<String, PartyEvent> partyEvents = new LinkedHashMap<>();

    // ================================================================
    //  LOAD / SAVE
    // ================================================================

    public static void loadFromData(DataManager.SaveData data) {
        parties.clear();
        playerParty.clear();
        lastDefectMs.clear();
        completedPartyOnboarding.clear();
        leaderElectionVotes.clear();
        pendingProposals.clear();
        partyRanks.clear();
        partyPerks.clear();
        activeWars.clear();
        alliances.clear();
        treasuryLogs.clear();
        peacePending.clear();
        alliancePending.clear();
        partyEvents.clear();
        rulingPartyId = null;
        leaderElectionActive = false;
        leaderElectionEndMs = 0L;
        lastSalaryMs = System.currentTimeMillis();

        DataManager.PoliticalPartySave ps = data.politicalPartySave;
        if (ps == null) {
            ensureDefaultParties();
            return;
        }
        if (ps.parties != null) {
            for (DataManager.PartyDTO dto : ps.parties) {
                Party p = new Party(dto.id, dto.displayName);
                p.budgetCoins = Math.max(0L, dto.budgetCoins);
                p.platform = dto.platform != null ? dto.platform : "";
                if (dto.members != null) {
                    for (Map.Entry<String, DataManager.PartyMemberDTO> e : dto.members.entrySet()) {
                        PartyMember m = new PartyMember();
                        m.support = Math.max(0L, e.getValue().support);
                        m.joinedAtMs = e.getValue().joinedAtMs;
                        p.members.put(e.getKey(), m);
                    }
                }
                parties.put(p.id, p);
            }
        }
        if (ps.playerParty != null) {
            playerParty.putAll(ps.playerParty);
        }
        if (ps.lastDefectMs != null) {
            lastDefectMs.putAll(ps.lastDefectMs);
        }
        if (ps.completedPartyOnboarding != null) {
            completedPartyOnboarding.addAll(ps.completedPartyOnboarding);
        }
        rulingPartyId = ps.rulingPartyId;
        leaderElectionActive = ps.leaderElectionActive;
        leaderElectionEndMs = ps.leaderElectionEndMs;
        if (ps.leaderElectionVotes != null) {
            leaderElectionVotes.putAll(ps.leaderElectionVotes);
        }
        if (ps.pendingProposals != null) {
            for (DataManager.PerkProposalDTO d : ps.pendingProposals) {
                PerkProposal pr = new PerkProposal();
                pr.partyId = d.partyId;
                pr.perkId = d.perkId;
                pr.costCoins = d.costCoins;
                pr.durationMs = d.durationMs;
                pr.deadlineMs = d.deadlineMs;
                if (d.councilUuids != null) {
                    pr.councilUuids.addAll(d.councilUuids);
                }
                if (d.votesYes != null) {
                    pr.votesYes.putAll(d.votesYes);
                }
                pendingProposals.add(pr);
            }
        }

        // --- Ranks ---
        if (ps.partyRanks != null) {
            for (Map.Entry<String, Map<String, String>> e : ps.partyRanks.entrySet()) {
                partyRanks.put(e.getKey(), new LinkedHashMap<>(e.getValue()));
            }
        }

        // --- Party Perks ---
        if (ps.partyPerks != null) {
            for (Map.Entry<String, List<String>> e : ps.partyPerks.entrySet()) {
                partyPerks.put(e.getKey(), new LinkedHashSet<>(e.getValue()));
            }
        }

        // --- Wars ---
        if (ps.activeWars != null) {
            for (Map.Entry<String, List<String>> e : ps.activeWars.entrySet()) {
                activeWars.put(e.getKey(), new LinkedHashSet<>(e.getValue()));
            }
        }

        // --- Alliances ---
        if (ps.alliances != null) {
            for (Map.Entry<String, List<String>> e : ps.alliances.entrySet()) {
                alliances.put(e.getKey(), new LinkedHashSet<>(e.getValue()));
            }
        }

        // --- Treasury Logs ---
        if (ps.treasuryLogs != null) {
            for (Map.Entry<String, List<DataManager.TreasuryLogDTO>> e : ps.treasuryLogs.entrySet()) {
                List<TreasuryLog> logs = new ArrayList<>();
                for (DataManager.TreasuryLogDTO d : e.getValue()) {
                    logs.add(new TreasuryLog(d.timestampMs, d.action, d.amount, d.detail));
                }
                treasuryLogs.put(e.getKey(), logs);
            }
        }

        // --- Peace Pending ---
        if (ps.peacePending != null) {
            for (Map.Entry<String, List<String>> e : ps.peacePending.entrySet()) {
                peacePending.put(e.getKey(), new LinkedHashSet<>(e.getValue()));
            }
        }

        // --- Alliance Pending ---
        if (ps.alliancePending != null) {
            for (Map.Entry<String, List<String>> e : ps.alliancePending.entrySet()) {
                alliancePending.put(e.getKey(), new LinkedHashSet<>(e.getValue()));
            }
        }

        // --- Party Events ---
        if (ps.partyEvents != null) {
            for (Map.Entry<String, DataManager.PartyEventDTO> e : ps.partyEvents.entrySet()) {
                PartyEvent pe = new PartyEvent();
                pe.eventType = e.getValue().eventType;
                pe.startMs = e.getValue().startMs;
                pe.endMs = e.getValue().endMs;
                pe.rewarded = e.getValue().rewarded;
                if (e.getValue().memberProgress != null) {
                    pe.memberProgress.putAll(e.getValue().memberProgress);
                }
                partyEvents.put(e.getKey(), pe);
            }
        }

        ensureDefaultParties();
    }

    private static void migrateGovernmentTreasuryToParties(DataManager.SaveData data) {
        // National tax and VAT stay in the government ledger. Party kitties are donations only.
    }

    public static void saveToData(DataManager.SaveData data) {
        DataManager.PoliticalPartySave ps = new DataManager.PoliticalPartySave();
        for (Party p : parties.values()) {
            DataManager.PartyDTO dto = new DataManager.PartyDTO();
            dto.id = p.id;
            dto.displayName = p.displayName;
            dto.budgetCoins = p.budgetCoins;
            dto.platform = p.platform != null ? p.platform : "";
            dto.members = new LinkedHashMap<>();
            for (Map.Entry<String, PartyMember> e : p.members.entrySet()) {
                DataManager.PartyMemberDTO md = new DataManager.PartyMemberDTO();
                md.support = e.getValue().support;
                md.joinedAtMs = e.getValue().joinedAtMs;
                dto.members.put(e.getKey(), md);
            }
            ps.parties.add(dto);
        }
        ps.playerParty = new HashMap<>(playerParty);
        ps.lastDefectMs = new HashMap<>(lastDefectMs);
        ps.completedPartyOnboarding = new ArrayList<>(completedPartyOnboarding);
        ps.rulingPartyId = rulingPartyId;
        ps.leaderElectionActive = leaderElectionActive;
        ps.leaderElectionEndMs = leaderElectionEndMs;
        ps.leaderElectionVotes = new HashMap<>(leaderElectionVotes);
        ps.pendingProposals = new ArrayList<>();
        for (PerkProposal pr : pendingProposals) {
            DataManager.PerkProposalDTO d = new DataManager.PerkProposalDTO();
            d.partyId = pr.partyId;
            d.perkId = pr.perkId;
            d.costCoins = pr.costCoins;
            d.durationMs = pr.durationMs;
            d.deadlineMs = pr.deadlineMs;
            d.councilUuids = new ArrayList<>(pr.councilUuids);
            d.votesYes = new HashMap<>(pr.votesYes);
            ps.pendingProposals.add(d);
        }

        // --- Ranks ---
        ps.partyRanks = new HashMap<>();
        for (Map.Entry<String, Map<String, String>> e : partyRanks.entrySet()) {
            ps.partyRanks.put(e.getKey(), new HashMap<>(e.getValue()));
        }

        // --- Party Perks ---
        ps.partyPerks = new HashMap<>();
        for (Map.Entry<String, Set<String>> e : partyPerks.entrySet()) {
            ps.partyPerks.put(e.getKey(), new ArrayList<>(e.getValue()));
        }

        // --- Wars ---
        ps.activeWars = new HashMap<>();
        for (Map.Entry<String, Set<String>> e : activeWars.entrySet()) {
            ps.activeWars.put(e.getKey(), new ArrayList<>(e.getValue()));
        }

        // --- Alliances ---
        ps.alliances = new HashMap<>();
        for (Map.Entry<String, Set<String>> e : alliances.entrySet()) {
            ps.alliances.put(e.getKey(), new ArrayList<>(e.getValue()));
        }

        // --- Treasury Logs ---
        ps.treasuryLogs = new HashMap<>();
        for (Map.Entry<String, List<TreasuryLog>> e : treasuryLogs.entrySet()) {
            List<DataManager.TreasuryLogDTO> dtos = new ArrayList<>();
            for (TreasuryLog l : e.getValue()) {
                DataManager.TreasuryLogDTO d = new DataManager.TreasuryLogDTO();
                d.timestampMs = l.timestampMs;
                d.action = l.action;
                d.amount = l.amount;
                d.detail = l.detail;
                dtos.add(d);
            }
            ps.treasuryLogs.put(e.getKey(), dtos);
        }

        // --- Peace Pending ---
        ps.peacePending = new HashMap<>();
        for (Map.Entry<String, Set<String>> e : peacePending.entrySet()) {
            ps.peacePending.put(e.getKey(), new ArrayList<>(e.getValue()));
        }

        // --- Alliance Pending ---
        ps.alliancePending = new HashMap<>();
        for (Map.Entry<String, Set<String>> e : alliancePending.entrySet()) {
            ps.alliancePending.put(e.getKey(), new ArrayList<>(e.getValue()));
        }

        // --- Party Events ---
        ps.partyEvents = new HashMap<>();
        for (Map.Entry<String, PartyEvent> e : partyEvents.entrySet()) {
            DataManager.PartyEventDTO d = new DataManager.PartyEventDTO();
            d.eventType = e.getValue().eventType;
            d.startMs = e.getValue().startMs;
            d.endMs = e.getValue().endMs;
            d.rewarded = e.getValue().rewarded;
            d.memberProgress = new HashMap<>(e.getValue().memberProgress);
            ps.partyEvents.put(e.getKey(), d);
        }

        data.politicalPartySave = ps;
    }

    // ================================================================
    //  PARTY BASICS
    // ================================================================

    private static void ensureDefaultParties() {
        if (!parties.containsKey("caine")) {
            parties.put("caine", new Party("caine", "Caine"));
        }
        if (!parties.containsKey("abel")) {
            parties.put("abel", new Party("abel", "Abel"));
        }
    }

    public static Collection<Party> getParties() {
        return Collections.unmodifiableCollection(parties.values());
    }

    public static Party getParty(String id) {
        return parties.get(id);
    }

    public static String getPlayerPartyId(String uuid) {
        return playerParty.get(uuid);
    }

    public static boolean hasCompletedOnboarding(String uuid) {
        return completedPartyOnboarding.contains(uuid);
    }

    public static void markOnboardingDone(String uuid) {
        completedPartyOnboarding.add(uuid);
        DataManager.save(PoliticalServer.server);
    }

    public static boolean tryJoinParty(ServerPlayer player, String partyId) {
        Party p = parties.get(partyId);
        if (p == null) {
            player.sendSystemMessage(Component.literal("Unknown party.").withStyle(ChatFormatting.RED));
            return false;
        }
        String uuid = player.getUUID().toString();
        String cur = playerParty.get(uuid);
        long now = System.currentTimeMillis();
        if (cur != null && !cur.equals(partyId)) {
            Long last = lastDefectMs.get(uuid);
            if (last != null && now - last < DEFECT_COOLDOWN_MS) {
                long left = (DEFECT_COOLDOWN_MS - (now - last)) / 1000L / 60L;
                player.sendSystemMessage(Component.literal("Defect cooldown: " + left + " min remaining.").withStyle(ChatFormatting.RED));
                return false;
            }
            Party old = parties.get(cur);
            if (old != null) {
                old.members.remove(uuid);
            }
            removeRank(cur, uuid);
            lastDefectMs.put(uuid, now);
        }
        playerParty.put(uuid, partyId);
        p.members.putIfAbsent(uuid, new PartyMember(0, now));
        setRankIfAbsent(partyId, uuid, "Recruit");
        markOnboardingDone(uuid);
        player.sendSystemMessage(Component.literal("Joined " + p.displayName + ".").withStyle(ChatFormatting.GREEN));
        DataManager.save(PoliticalServer.server);
        return true;
    }

    public static boolean tryCreateParty(ServerPlayer player, String rawName) {
        String name = rawName == null ? "" : rawName.trim();
        if (name.length() < 2 || name.length() > 32) {
            player.sendSystemMessage(Component.literal("Name must be 2-32 characters.").withStyle(ChatFormatting.RED));
            return false;
        }
        if (!CreditItem.removeCredits(player, CREATE_PARTY_CREDITS)) {
            player.sendSystemMessage(Component.literal("You need " + CREATE_PARTY_CREDITS + " credits (no coin substitute).").withStyle(ChatFormatting.RED));
            return false;
        }
        String id = slugify(name);
        if (id.isEmpty()) {
            id = "party_" + System.currentTimeMillis();
        }
        if (parties.containsKey(id)) {
            player.sendSystemMessage(Component.literal("That id is taken or invalid; pick another name.").withStyle(ChatFormatting.RED));
            CreditItem.giveCreditsQuiet(player, CREATE_PARTY_CREDITS);
            return false;
        }
        parties.put(id, new Party(id, name));
        tryJoinParty(player, id);
        setRank(id, player.getUUID().toString(), "Leader");
        player.sendSystemMessage(Component.literal("Party created: " + name).withStyle(ChatFormatting.GOLD));
        return true;
    }

    private static String slugify(String name) {
        return name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]+", "_").replaceAll("^_+|_+$", "");
    }

    public static void donateCoins(ServerPlayer player, long amount) {
        if (amount <= 0) {
            return;
        }
        String uuid = player.getUUID().toString();
        String pid = playerParty.get(uuid);
        if (pid == null) {
            player.sendSystemMessage(Component.literal("Join a party first.").withStyle(ChatFormatting.RED));
            return;
        }
        if (!CoinManager.removeCoins(player, amount)) {
            player.sendSystemMessage(Component.literal("Not enough coins.").withStyle(ChatFormatting.RED));
            return;
        }
        Party p = parties.get(pid);
        if (p == null) {
            return;
        }
        p.budgetCoins += amount;
        PartyMember m = p.members.computeIfAbsent(uuid, u -> new PartyMember(0, System.currentTimeMillis()));
        long supportGained = Math.max(1L, amount / COINS_PER_SUPPORT_POINT);
        m.support += supportGained;
        player.sendSystemMessage(Component.literal("Donated " + amount + " coins. +" + supportGained + " support.").withStyle(ChatFormatting.GREEN));
        addTreasuryLog(pid, "Donation", amount, DataManager.getPlayerName(uuid));
        BuffProgressManager.onPartyDonation(player, amount);
        recordEventProgress(pid, uuid, "donation_drive", amount);
        progressPartyCommission(player, "DONATE_COINS", (int) Math.min(amount, Integer.MAX_VALUE));
        DataManager.save(PoliticalServer.server);
    }

    public static boolean trySetPartyPlatform(ServerPlayer player, String text) {
        String uuid = player.getUUID().toString();
        String pid = playerParty.get(uuid);
        if (pid == null) {
            player.sendSystemMessage(Component.literal("Join a party first.").withStyle(ChatFormatting.RED));
            return false;
        }
        if (!isPartyLeader(player)) {
            player.sendSystemMessage(Component.literal("Only your party's top supporter (leader) can set the platform.").withStyle(ChatFormatting.RED));
            return false;
        }
        Party p = parties.get(pid);
        if (p == null) {
            return false;
        }
        String t = text == null ? "" : text.trim();
        if (t.length() > 160) {
            t = t.substring(0, 160);
        }
        p.platform = t;
        player.sendSystemMessage(Component.literal("Party platform updated.").withStyle(ChatFormatting.GREEN));
        DataManager.save(PoliticalServer.server);
        return true;
    }

    public static void distributeTaxToParties(int amount) {
        if (amount <= 0) {
            return;
        }
        Treasury.deposit(amount, "tax");
    }

    public static void distributeBountyTaxCoins(int taxAmount) {
        distributeTaxToParties(taxAmount);
        if (PoliticalServer.server != null) {
            DataManager.save(PoliticalServer.server);
        }
    }

    // ================================================================
    //  LEADERSHIP
    // ================================================================

    public static String getPartyLeaderUuid(String partyId) {
        Party p = parties.get(partyId);
        if (p == null || p.members.isEmpty()) {
            return null;
        }
        return p.members.entrySet().stream()
                .max(Comparator
                        .<Map.Entry<String, PartyMember>>comparingLong(e -> e.getValue().support)
                        .thenComparingLong(e -> -e.getValue().joinedAtMs))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public static boolean isPartyLeader(ServerPlayer player) {
        String pid = playerParty.get(player.getUUID().toString());
        if (pid == null) {
            return false;
        }
        String leader = getPartyLeaderUuid(pid);
        return leader != null && leader.equals(player.getUUID().toString());
    }

    public static boolean isLeaderOfRulingParty(ServerPlayer player) {
        if (rulingPartyId == null) {
            return false;
        }
        String leader = getPartyLeaderUuid(rulingPartyId);
        return leader != null && leader.equals(player.getUUID().toString());
    }

    public static long getPartyBudget(String partyId) {
        Party p = parties.get(partyId);
        return p == null ? 0L : p.budgetCoins;
    }

    public static boolean trySpendPartyBudget(String partyId, long amount) {
        Party p = parties.get(partyId);
        if (p == null || p.budgetCoins < amount) {
            return false;
        }
        p.budgetCoins -= amount;
        return true;
    }

    // ================================================================
    //  ELECTIONS
    // ================================================================

    public static void syncStartPartyLeaderElection(long endTimeMs) {
        leaderElectionActive = true;
        leaderElectionEndMs = endTimeMs;
        leaderElectionVotes.clear();
        if (PoliticalServer.server != null) {
            PoliticalPartyManager.broadcast(PoliticalServer.server,
                    Component.literal("Ruling party election is open — use /vote to choose a party.").withStyle(ChatFormatting.AQUA));
        }
    }

    public static void syncEndPartyLeaderElection(MinecraftServer server) {
        if (!leaderElectionActive) {
            return;
        }
        leaderElectionActive = false;
        leaderElectionEndMs = 0L;
        Map<String, Integer> tally = new HashMap<>();
        for (String partyId : leaderElectionVotes.values()) {
            tally.merge(partyId, 1, Integer::sum);
        }
        if (tally.isEmpty()) {
            if (rulingPartyId == null && !parties.isEmpty()) {
                rulingPartyId = parties.keySet().iterator().next();
            }
        } else {
            int best = tally.values().stream().mapToInt(i -> i).max().orElse(0);
            List<String> tied = tally.entrySet().stream().filter(e -> e.getValue() == best).map(Map.Entry::getKey).toList();
            rulingPartyId = tied.get(new Random(server.overworld().getSeed() ^ System.currentTimeMillis()).nextInt(tied.size()));
        }
        leaderElectionVotes.clear();
        Party rp = parties.get(rulingPartyId);
        String name = rp != null ? rp.displayName : rulingPartyId;
        broadcast(server, Component.literal("Ruling party: " + name).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        DataManager.save(server);
    }

    public static boolean isLeaderElectionActive() {
        return leaderElectionActive && System.currentTimeMillis() < leaderElectionEndMs;
    }

    public static void castLeaderVote(ServerPlayer player, String partyId) {
        if (!isLeaderElectionActive()) {
            player.sendSystemMessage(Component.literal("No ruling-party election.").withStyle(ChatFormatting.RED));
            return;
        }
        if (parties.get(partyId) == null) {
            player.sendSystemMessage(Component.literal("Invalid party.").withStyle(ChatFormatting.RED));
            return;
        }
        leaderElectionVotes.put(player.getUUID().toString(), partyId);
        player.sendSystemMessage(Component.literal("Vote recorded.").withStyle(ChatFormatting.GREEN));
        DataManager.save(PoliticalServer.server);
    }

    public static Map<String, String> getLeaderElectionVotes() {
        return Collections.unmodifiableMap(leaderElectionVotes);
    }

    public static String getRulingPartyId() {
        return rulingPartyId;
    }

    public static List<String> getCouncilUuids(String partyId) {
        String leader = getPartyLeaderUuid(partyId);
        Party p = parties.get(partyId);
        if (p == null || leader == null) {
            return List.of();
        }
        return p.members.entrySet().stream()
                .filter(e -> !e.getKey().equals(leader))
                .sorted(Comparator.<Map.Entry<String, PartyMember>>comparingLong(e -> e.getValue().support).reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public static String getPartyViceLeaderUuid(String partyId) {
        Party p = parties.get(partyId);
        if (p == null || p.members.size() < 2) {
            return null;
        }
        return p.members.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, PartyMember>>comparingLong((Map.Entry<String, PartyMember> e) -> e.getValue().support).reversed()
                        .thenComparingLong(e -> e.getValue().joinedAtMs))
                .skip(1)
                .findFirst()
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public static ServerPlayer resolveRulingPartyDelegateOnline(MinecraftServer server) {
        if (rulingPartyId == null) {
            return null;
        }
        String lead = getPartyLeaderUuid(rulingPartyId);
        if (lead != null) {
            ServerPlayer pl = server.getPlayerList().getPlayer(java.util.UUID.fromString(lead));
            if (pl != null) {
                return pl;
            }
        }
        String vice = getPartyViceLeaderUuid(rulingPartyId);
        if (vice == null) {
            return null;
        }
        return server.getPlayerList().getPlayer(java.util.UUID.fromString(vice));
    }

    public static void addPartyBudgetCoins(String partyId, long amount) {
        if (amount <= 0) {
            return;
        }
        Party p = parties.get(partyId);
        if (p != null) {
            p.budgetCoins += amount;
        }
    }

    public static void grantActivitySupport(ServerPlayer player, long points, String reason) {
        if (points <= 0) {
            return;
        }
        String pid = playerParty.get(player.getUUID().toString());
        if (pid == null) {
            return;
        }
        Party p = parties.get(pid);
        if (p == null) {
            return;
        }
        PartyMember m = p.members.computeIfAbsent(player.getUUID().toString(), u -> new PartyMember(0, System.currentTimeMillis()));
        m.support += points;
        if (reason != null) {
            player.sendSystemMessage(Component.literal("+" + points + " party support (" + reason + ")").withStyle(ChatFormatting.AQUA), true);
        }
        DataManager.save(PoliticalServer.server);
    }

    public static void proposeRulingPartyPerk(ServerPlayer player, String perkId, long cost, long durationMs) {
        player.sendSystemMessage(Component.literal("National perks are chosen by the Chair and Vice Chair, not by party council.")
                .withStyle(ChatFormatting.RED));
    }

    public static PerkProposal getOpenProposalForCouncil(String uuid) {
        long now = System.currentTimeMillis();
        for (PerkProposal pr : pendingProposals) {
            if (now > pr.deadlineMs) {
                continue;
            }
            if (pr.councilUuids.contains(uuid)) {
                return pr;
            }
        }
        return null;
    }

    public static void councilVote(ServerPlayer player, boolean yes) {
        PerkProposal pr = getOpenProposalForCouncil(player.getUUID().toString());
        if (pr == null) {
            player.sendSystemMessage(Component.literal("No active proposal for you.").withStyle(ChatFormatting.RED));
            return;
        }
        pr.votesYes.put(player.getUUID().toString(), yes);
        player.sendSystemMessage(Component.literal(yes ? "Recorded: approve." : "Recorded: reject.").withStyle(ChatFormatting.GREEN));
        DataManager.save(PoliticalServer.server);
    }

    // ================================================================
    //  PARTY RANKS
    // ================================================================

    public static String getPartyRank(String partyId, String uuid) {
        Map<String, String> ranks = partyRanks.get(partyId);
        if (ranks == null) {
            return "Recruit";
        }
        String r = ranks.get(uuid);
        return r != null ? r : "Recruit";
    }

    private static void setRank(String partyId, String uuid, String rank) {
        partyRanks.computeIfAbsent(partyId, k -> new LinkedHashMap<>()).put(uuid, rank);
    }

    private static void setRankIfAbsent(String partyId, String uuid, String rank) {
        Map<String, String> ranks = partyRanks.computeIfAbsent(partyId, k -> new LinkedHashMap<>());
        ranks.putIfAbsent(uuid, rank);
    }

    private static void removeRank(String partyId, String uuid) {
        Map<String, String> ranks = partyRanks.get(partyId);
        if (ranks != null) {
            ranks.remove(uuid);
        }
    }

    public static boolean canPromote(String promoterRank, String targetRank) {
        int pi = rankIndex(promoterRank);
        int ti = rankIndex(targetRank);
        return pi < ti && ti > 1;
    }

    public static boolean canDemote(String promoterRank, String targetRank) {
        int pi = rankIndex(promoterRank);
        int ti = rankIndex(targetRank);
        return pi < ti && ti < RANK_ORDER.size() - 1;
    }

    public static void promotePartyMember(ServerPlayer leader, String targetUuid) {
        String pid = playerParty.get(leader.getUUID().toString());
        if (pid == null) {
            leader.sendSystemMessage(Component.literal("You are not in a party.").withStyle(ChatFormatting.RED));
            return;
        }
        String leaderRank = getPartyRank(pid, leader.getUUID().toString());
        if (rankIndex(leaderRank) > 1) {
            leader.sendSystemMessage(Component.literal("Only Leader or Vice Leader can promote.").withStyle(ChatFormatting.RED));
            return;
        }
        String targetPid = playerParty.get(targetUuid);
        if (!pid.equals(targetPid)) {
            leader.sendSystemMessage(Component.literal("That player is not in your party.").withStyle(ChatFormatting.RED));
            return;
        }
        String currentRank = getPartyRank(pid, targetUuid);
        if (!canPromote(leaderRank, currentRank)) {
            leader.sendSystemMessage(Component.literal("Cannot promote that member further.").withStyle(ChatFormatting.RED));
            return;
        }
        int idx = rankIndex(currentRank);
        String newRank = RANK_ORDER.get(idx - 1);
        setRank(pid, targetUuid, newRank);
        String name = DataManager.getPlayerName(targetUuid);
        leader.sendSystemMessage(Component.literal("Promoted " + name + " to " + newRank + ".").withStyle(ChatFormatting.GREEN));
        addTreasuryLog(pid, "Promotion", 0, name + " -> " + newRank);

        if (PoliticalServer.server != null) {
            ServerPlayer target = PoliticalServer.server.getPlayerList().getPlayer(java.util.UUID.fromString(targetUuid));
            if (target != null) {
                target.sendSystemMessage(Component.literal("You were promoted to " + newRank + " in your party!").withStyle(ChatFormatting.GREEN));
            }
        }
        DataManager.save(PoliticalServer.server);
    }

    public static void demotePartyMember(ServerPlayer leader, String targetUuid) {
        String pid = playerParty.get(leader.getUUID().toString());
        if (pid == null) {
            leader.sendSystemMessage(Component.literal("You are not in a party.").withStyle(ChatFormatting.RED));
            return;
        }
        String leaderRank = getPartyRank(pid, leader.getUUID().toString());
        if (rankIndex(leaderRank) > 1) {
            leader.sendSystemMessage(Component.literal("Only Leader or Vice Leader can demote.").withStyle(ChatFormatting.RED));
            return;
        }
        String targetPid = playerParty.get(targetUuid);
        if (!pid.equals(targetPid)) {
            leader.sendSystemMessage(Component.literal("That player is not in your party.").withStyle(ChatFormatting.RED));
            return;
        }
        String currentRank = getPartyRank(pid, targetUuid);
        if (!canDemote(leaderRank, currentRank)) {
            leader.sendSystemMessage(Component.literal("Cannot demote that member further.").withStyle(ChatFormatting.RED));
            return;
        }
        int idx = rankIndex(currentRank);
        String newRank = RANK_ORDER.get(idx + 1);
        setRank(pid, targetUuid, newRank);
        String name = DataManager.getPlayerName(targetUuid);
        leader.sendSystemMessage(Component.literal("Demoted " + name + " to " + newRank + ".").withStyle(ChatFormatting.GREEN));
        addTreasuryLog(pid, "Demotion", 0, name + " -> " + newRank);

        if (PoliticalServer.server != null) {
            ServerPlayer target = PoliticalServer.server.getPlayerList().getPlayer(java.util.UUID.fromString(targetUuid));
            if (target != null) {
                target.sendSystemMessage(Component.literal("You were demoted to " + newRank + " in your party.").withStyle(ChatFormatting.RED));
            }
        }
        DataManager.save(PoliticalServer.server);
    }

    // ================================================================
    //  PARTY PERKS
    // ================================================================

    public static boolean hasPartyPerk(String uuid, String perkId) {
        String pid = playerParty.get(uuid);
        if (pid == null) return false;
        Set<String> perks = partyPerks.get(pid);
        return perks != null && perks.contains(perkId);
    }

    public static Set<String> getPartyPerks(String partyId) {
        Set<String> perks = partyPerks.get(partyId);
        return perks != null ? Collections.unmodifiableSet(perks) : Set.of();
    }

    public static void tryBuyPartyPerk(ServerPlayer leader, String perkId) {
        leader.sendSystemMessage(Component.literal("Party perks are affiliation only. National policy is set by the Chair.")
                .withStyle(ChatFormatting.RED));
    }

    // ================================================================
    //  PARTY WARS
    // ================================================================

    private static final long WAR_COST = 10_000_000L;
    private static final long PEACE_COST = 5_000_000L;
    public static final long WAR_KILL_BONUS_COINS = 50_000L;
    public static final long WAR_KILL_BONUS_SUPPORT = 5L;

    public static boolean isAtWar(String partyId1, String partyId2) {
        Set<String> w1 = activeWars.get(partyId1);
        return w1 != null && w1.contains(partyId2);
    }

    public static Set<String> getWars(String partyId) {
        Set<String> w = activeWars.get(partyId);
        return w != null ? Collections.unmodifiableSet(w) : Set.of();
    }

    public static void declareWar(ServerPlayer leader, String targetPartyId) {
        if (!isPartyLeader(leader)) {
            leader.sendSystemMessage(Component.literal("Only the party leader can declare war.").withStyle(ChatFormatting.RED));
            return;
        }
        String pid = playerParty.get(leader.getUUID().toString());
        if (pid == null) return;
        if (pid.equals(targetPartyId)) {
            leader.sendSystemMessage(Component.literal("You can't declare war on your own party.").withStyle(ChatFormatting.RED));
            return;
        }
        Party target = parties.get(targetPartyId);
        if (target == null) {
            leader.sendSystemMessage(Component.literal("Unknown party.").withStyle(ChatFormatting.RED));
            return;
        }
        if (isAtWar(pid, targetPartyId)) {
            leader.sendSystemMessage(Component.literal("Already at war with " + target.displayName + "!").withStyle(ChatFormatting.RED));
            return;
        }
        if (isAllied(pid, targetPartyId)) {
            leader.sendSystemMessage(Component.literal("Break your alliance first!").withStyle(ChatFormatting.RED));
            return;
        }
        if (!trySpendPartyBudget(pid, WAR_COST)) {
            leader.sendSystemMessage(Component.literal("Need " + String.format("%,d", WAR_COST) + " in party budget to declare war.").withStyle(ChatFormatting.RED));
            return;
        }
        activeWars.computeIfAbsent(pid, k -> new LinkedHashSet<>()).add(targetPartyId);
        activeWars.computeIfAbsent(targetPartyId, k -> new LinkedHashSet<>()).add(pid);

        Party own = parties.get(pid);
        addTreasuryLog(pid, "War Declaration", -WAR_COST, "vs " + target.displayName);

        if (PoliticalServer.server != null) {
            broadcast(PoliticalServer.server, Component.literal(
                    "\u2694 " + (own != null ? own.displayName : pid) + " declared WAR on " + target.displayName + "!")
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        }
        DataManager.save(PoliticalServer.server);
    }

    public static void proposePeace(ServerPlayer leader, String targetPartyId) {
        if (!isPartyLeader(leader)) {
            leader.sendSystemMessage(Component.literal("Only the party leader can propose peace.").withStyle(ChatFormatting.RED));
            return;
        }
        String pid = playerParty.get(leader.getUUID().toString());
        if (pid == null) return;
        if (!isAtWar(pid, targetPartyId)) {
            leader.sendSystemMessage(Component.literal("You are not at war with that party.").withStyle(ChatFormatting.RED));
            return;
        }
        if (!trySpendPartyBudget(pid, PEACE_COST)) {
            leader.sendSystemMessage(Component.literal("Need " + String.format("%,d", PEACE_COST) + " in party budget to propose peace.").withStyle(ChatFormatting.RED));
            return;
        }
        peacePending.computeIfAbsent(targetPartyId, k -> new LinkedHashSet<>()).add(pid);
        addTreasuryLog(pid, "Peace Proposal", -PEACE_COST, "to " + targetPartyId);

        Party target = parties.get(targetPartyId);
        leader.sendSystemMessage(Component.literal("Peace proposed to " + (target != null ? target.displayName : targetPartyId) + ".").withStyle(ChatFormatting.YELLOW));
        broadcastToParty(targetPartyId, Component.literal(
                "\u2618 " + (parties.get(pid) != null ? parties.get(pid).displayName : pid) + " has proposed peace. Leader must accept.")
                .withStyle(ChatFormatting.YELLOW));
        DataManager.save(PoliticalServer.server);
    }

    public static Set<String> getPeacePendingFrom(String partyId) {
        Set<String> p = peacePending.get(partyId);
        return p != null ? Collections.unmodifiableSet(p) : Set.of();
    }

    public static void acceptPeace(ServerPlayer leader, String fromPartyId) {
        if (!isPartyLeader(leader)) {
            leader.sendSystemMessage(Component.literal("Only the party leader can accept peace.").withStyle(ChatFormatting.RED));
            return;
        }
        String pid = playerParty.get(leader.getUUID().toString());
        if (pid == null) return;
        Set<String> pending = peacePending.get(pid);
        if (pending == null || !pending.contains(fromPartyId)) {
            leader.sendSystemMessage(Component.literal("No peace proposal from that party.").withStyle(ChatFormatting.RED));
            return;
        }
        pending.remove(fromPartyId);
        Set<String> w1 = activeWars.get(pid);
        Set<String> w2 = activeWars.get(fromPartyId);
        if (w1 != null) w1.remove(fromPartyId);
        if (w2 != null) w2.remove(pid);

        Party own = parties.get(pid);
        Party from = parties.get(fromPartyId);
        addTreasuryLog(pid, "Peace Accepted", 0, "with " + (from != null ? from.displayName : fromPartyId));
        addTreasuryLog(fromPartyId, "Peace Accepted", 0, "with " + (own != null ? own.displayName : pid));

        if (PoliticalServer.server != null) {
            broadcast(PoliticalServer.server, Component.literal(
                    "\u2618 Peace between " + (own != null ? own.displayName : pid) +
                    " and " + (from != null ? from.displayName : fromPartyId) + "!")
                    .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        }
        DataManager.save(PoliticalServer.server);
    }

    public static void onWarKill(ServerPlayer killer, ServerPlayer victim) {
        String killerPid = playerParty.get(killer.getUUID().toString());
        String victimPid = playerParty.get(victim.getUUID().toString());
        if (killerPid == null || victimPid == null || killerPid.equals(victimPid)) return;
        if (!isAtWar(killerPid, victimPid)) return;

        DataManager.addCoins(killer.getUUID().toString(), WAR_KILL_BONUS_COINS);
        grantActivitySupport(killer, WAR_KILL_BONUS_SUPPORT, "war kill");
        killer.sendSystemMessage(Component.literal("War kill bonus: +" + String.format("%,d", WAR_KILL_BONUS_COINS) + " coins!").withStyle(ChatFormatting.GOLD));
        recordEventProgress(killerPid, killer.getUUID().toString(), "pvp_tournament", 1);
    }

    // ================================================================
    //  PARTY ALLIANCES
    // ================================================================

    public static boolean isAllied(String partyId1, String partyId2) {
        Set<String> a1 = alliances.get(partyId1);
        return a1 != null && a1.contains(partyId2);
    }

    public static Set<String> getAlliances(String partyId) {
        Set<String> a = alliances.get(partyId);
        return a != null ? Collections.unmodifiableSet(a) : Set.of();
    }

    public static void proposeAlliance(ServerPlayer leader, String targetPartyId) {
        if (!isPartyLeader(leader)) {
            leader.sendSystemMessage(Component.literal("Only the party leader can propose alliances.").withStyle(ChatFormatting.RED));
            return;
        }
        String pid = playerParty.get(leader.getUUID().toString());
        if (pid == null) return;
        if (pid.equals(targetPartyId)) {
            leader.sendSystemMessage(Component.literal("You can't ally with yourself.").withStyle(ChatFormatting.RED));
            return;
        }
        Party target = parties.get(targetPartyId);
        if (target == null) {
            leader.sendSystemMessage(Component.literal("Unknown party.").withStyle(ChatFormatting.RED));
            return;
        }
        if (isAllied(pid, targetPartyId)) {
            leader.sendSystemMessage(Component.literal("Already allied with " + target.displayName + "!").withStyle(ChatFormatting.RED));
            return;
        }
        if (isAtWar(pid, targetPartyId)) {
            leader.sendSystemMessage(Component.literal("End the war first!").withStyle(ChatFormatting.RED));
            return;
        }
        alliancePending.computeIfAbsent(targetPartyId, k -> new LinkedHashSet<>()).add(pid);
        leader.sendSystemMessage(Component.literal("Alliance proposed to " + target.displayName + ".").withStyle(ChatFormatting.AQUA));
        broadcastToParty(targetPartyId, Component.literal(
                "\u2764 " + (parties.get(pid) != null ? parties.get(pid).displayName : pid) + " has proposed an alliance. Leader must accept.")
                .withStyle(ChatFormatting.AQUA));
        DataManager.save(PoliticalServer.server);
    }

    public static Set<String> getAlliancePendingFrom(String partyId) {
        Set<String> a = alliancePending.get(partyId);
        return a != null ? Collections.unmodifiableSet(a) : Set.of();
    }

    public static void acceptAlliance(ServerPlayer leader, String fromPartyId) {
        if (!isPartyLeader(leader)) {
            leader.sendSystemMessage(Component.literal("Only the party leader can accept alliances.").withStyle(ChatFormatting.RED));
            return;
        }
        String pid = playerParty.get(leader.getUUID().toString());
        if (pid == null) return;
        Set<String> pending = alliancePending.get(pid);
        if (pending == null || !pending.contains(fromPartyId)) {
            leader.sendSystemMessage(Component.literal("No alliance proposal from that party.").withStyle(ChatFormatting.RED));
            return;
        }
        pending.remove(fromPartyId);
        alliances.computeIfAbsent(pid, k -> new LinkedHashSet<>()).add(fromPartyId);
        alliances.computeIfAbsent(fromPartyId, k -> new LinkedHashSet<>()).add(pid);

        Party own = parties.get(pid);
        Party from = parties.get(fromPartyId);
        addTreasuryLog(pid, "Alliance Formed", 0, "with " + (from != null ? from.displayName : fromPartyId));
        addTreasuryLog(fromPartyId, "Alliance Formed", 0, "with " + (own != null ? own.displayName : pid));

        if (PoliticalServer.server != null) {
            broadcast(PoliticalServer.server, Component.literal(
                    "\u2764 Alliance formed: " + (own != null ? own.displayName : pid) +
                    " & " + (from != null ? from.displayName : fromPartyId) + "!")
                    .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        }
        DataManager.save(PoliticalServer.server);
    }

    public static void breakAlliance(ServerPlayer leader, String targetPartyId) {
        if (!isPartyLeader(leader)) {
            leader.sendSystemMessage(Component.literal("Only the party leader can break alliances.").withStyle(ChatFormatting.RED));
            return;
        }
        String pid = playerParty.get(leader.getUUID().toString());
        if (pid == null) return;
        if (!isAllied(pid, targetPartyId)) {
            leader.sendSystemMessage(Component.literal("Not allied with that party.").withStyle(ChatFormatting.RED));
            return;
        }
        Set<String> a1 = alliances.get(pid);
        Set<String> a2 = alliances.get(targetPartyId);
        if (a1 != null) a1.remove(targetPartyId);
        if (a2 != null) a2.remove(pid);

        Party own = parties.get(pid);
        Party target = parties.get(targetPartyId);
        leader.sendSystemMessage(Component.literal("Alliance with " + (target != null ? target.displayName : targetPartyId) + " broken.").withStyle(ChatFormatting.YELLOW));
        addTreasuryLog(pid, "Alliance Broken", 0, "with " + (target != null ? target.displayName : targetPartyId));
        DataManager.save(PoliticalServer.server);
    }

    // ================================================================
    //  TREASURY LOG
    // ================================================================

    private static final int MAX_TREASURY_LOGS = 50;

    private static void addTreasuryLog(String partyId, String action, long amount, String detail) {
        List<TreasuryLog> logs = treasuryLogs.computeIfAbsent(partyId, k -> new ArrayList<>());
        logs.add(new TreasuryLog(System.currentTimeMillis(), action, amount, detail));
        while (logs.size() > MAX_TREASURY_LOGS) {
            logs.remove(0);
        }
    }

    public static List<TreasuryLog> getTreasuryLog(String partyId) {
        List<TreasuryLog> logs = treasuryLogs.get(partyId);
        return logs != null ? Collections.unmodifiableList(logs) : List.of();
    }

    // ================================================================
    //  PARTY EVENTS / QUESTS
    // ================================================================

    public static PartyEvent getPartyEvent(String partyId) {
        return partyEvents.get(partyId);
    }

    public static void startPartyEvent(String partyId, String eventType) {
        if (!EVENT_TYPES.contains(eventType)) return;
        long now = System.currentTimeMillis();
        PartyEvent existing = partyEvents.get(partyId);
        if (existing != null && now < existing.endMs) {
            return;
        }
        PartyEvent ev = new PartyEvent();
        ev.eventType = eventType;
        ev.startMs = now;
        ev.endMs = now + EVENT_DURATION_MS;
        ev.rewarded = false;
        partyEvents.put(partyId, ev);
        broadcastToParty(partyId, Component.literal(
                "\u2605 Party Event started: " + eventDisplayName(eventType) + "! Lasts 7 days.")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        addTreasuryLog(partyId, "Event Started", 0, eventDisplayName(eventType));
        DataManager.save(PoliticalServer.server);
    }

    public static void recordEventProgress(String partyId, String uuid, String requiredType, long amount) {
        PartyEvent ev = partyEvents.get(partyId);
        if (ev == null || ev.rewarded) return;
        if (System.currentTimeMillis() > ev.endMs) return;
        if (!ev.eventType.equals(requiredType)) return;
        ev.memberProgress.merge(uuid, amount, Long::sum);
    }

    /** Called from mob kill hooks. */
    public static void onMobKillForEvent(ServerPlayer player) {
        String pid = playerParty.get(player.getUUID().toString());
        if (pid == null) return;
        recordEventProgress(pid, player.getUUID().toString(), "mob_kill_race", 1);
    }

    /** Called from mining hooks. */
    public static void onBlockMinedForEvent(ServerPlayer player) {
        String pid = playerParty.get(player.getUUID().toString());
        if (pid == null) return;
        recordEventProgress(pid, player.getUUID().toString(), "mining_race", 1);
    }

    private static void tickPartyEvents() {
        long now = System.currentTimeMillis();
        for (Map.Entry<String, PartyEvent> e : partyEvents.entrySet()) {
            PartyEvent ev = e.getValue();
            if (ev.rewarded || now < ev.endMs) continue;
            ev.rewarded = true;
            long totalProgress = ev.memberProgress.values().stream().mapToLong(Long::longValue).sum();
            if (totalProgress > 0) {
                Party p = parties.get(e.getKey());
                if (p != null) {
                    p.budgetCoins += EVENT_REWARD_BUDGET;
                    addTreasuryLog(e.getKey(), "Event Reward", EVENT_REWARD_BUDGET, eventDisplayName(ev.eventType) + " completed");
                    broadcastToParty(e.getKey(), Component.literal(
                            "\u2605 Party Event '" + eventDisplayName(ev.eventType) + "' complete! +" +
                            String.format("%,d", EVENT_REWARD_BUDGET) + " coins to budget!")
                            .withStyle(ChatFormatting.GOLD));
                }
            }
        }
    }

    // ================================================================
    //  TICK
    // ================================================================

    public static void tick(MinecraftServer server) {
        long now = System.currentTimeMillis();
        boolean dirty = false;
        if (leaderElectionActive && leaderElectionEndMs > 0 && now >= leaderElectionEndMs) {
            syncEndPartyLeaderElection(server);
            dirty = true;
        }
        Iterator<PerkProposal> it = pendingProposals.iterator();
        while (it.hasNext()) {
            PerkProposal pr = it.next();
            if (now < pr.deadlineMs) {
                continue;
            }
            boolean rejected = pr.votesYes.values().stream().anyMatch(v -> !v);
            if (rejected) {
                it.remove();
                dirty = true;
                broadcast(server, Component.literal("Party perk proposal rejected by council.").withStyle(ChatFormatting.RED));
                continue;
            }
            Party p = parties.get(pr.partyId);
            if (p == null || p.budgetCoins < pr.costCoins) {
                it.remove();
                dirty = true;
                continue;
            }
            if (!pr.councilUuids.isEmpty() && pr.votesYes.isEmpty()) {
                // no votes = pass
            } else if (!pr.councilUuids.isEmpty()) {
                long yes = pr.votesYes.values().stream().filter(v -> v).count();
                if (yes < 2 && pr.councilUuids.size() >= 2) {
                    it.remove();
                    dirty = true;
                    broadcast(server, Component.literal("Party perk proposal lacked council approval.").withStyle(ChatFormatting.RED));
                    continue;
                }
            }
            p.budgetCoins -= pr.costCoins;
            PerkManager.activateTemporaryPerk(pr.perkId, pr.durationMs);
            it.remove();
            dirty = true;
            broadcast(server, Component.literal("Ruling party funded national program: " + pr.perkId).withStyle(ChatFormatting.GREEN));
        }
        if (now - lastSalaryMs >= SALARY_INTERVAL_MS) {
            lastSalaryMs = now;
            paySalaries(server);
            dirty = true;
        }
        tickPartyEvents();
        if (dirty) {
            DataManager.save(server);
        }
    }

    // ================================================================
    //  SALARIES
    // ================================================================

    private static void paySalaries(MinecraftServer server) {
        for (Party p : parties.values()) {
            if (p.members.isEmpty() || p.budgetCoins <= 0) {
                continue;
            }
            long pool = (long) (p.budgetCoins * SALARY_HOURLY_BUDGET_FRACTION);
            if (pool <= 0) {
                continue;
            }
            long totalSupport = p.members.values().stream().mapToLong(m -> m.support).sum();
            if (totalSupport <= 0) {
                continue;
            }
            long paid = 0L;
            for (Map.Entry<String, PartyMember> e : p.members.entrySet()) {
                long share = (long) (pool * ((double) e.getValue().support / (double) totalSupport));
                if (share <= 0) {
                    continue;
                }
                if (p.budgetCoins - paid < share) {
                    share = Math.max(0L, p.budgetCoins - paid);
                }
                if (share <= 0) {
                    break;
                }
                DataManager.addCoins(e.getKey(), share);
                paid += share;
                ServerPlayer pl = server.getPlayerList().getPlayer(java.util.UUID.fromString(e.getKey()));
                if (pl != null) {
                    pl.sendSystemMessage(Component.literal("Party salary: +" + share + " coins (support-based).").withStyle(ChatFormatting.GOLD), false);
                }
            }
            p.budgetCoins -= paid;
            if (paid > 0) {
                addTreasuryLog(p.id, "Salaries Paid", -paid, "hourly distribution");
            }
        }
    }

    // ================================================================
    //  ADS
    // ================================================================

    public static void broadcastPartyAd(ServerPlayer leader, String message) {
        if (!isPartyLeader(leader)) {
            leader.sendSystemMessage(Component.literal("Only party leaders can buy ads.").withStyle(ChatFormatting.RED));
            return;
        }
        String pid = playerParty.get(leader.getUUID().toString());
        Party p = parties.get(pid);
        if (p == null) {
            return;
        }
        long cost = 2_500_000L;
        if (p.budgetCoins < cost) {
            leader.sendSystemMessage(Component.literal("Need " + cost + " coins in party budget.").withStyle(ChatFormatting.RED));
            return;
        }
        p.budgetCoins -= cost;
        addTreasuryLog(pid, "Ad Broadcast", -cost, message.length() > 30 ? message.substring(0, 30) + "..." : message);
        Component msg = Component.literal("[§6" + p.displayName + "§f] §f" + message);
        for (ServerPlayer pl : PoliticalServer.server.getPlayerList().getPlayers()) {
            pl.sendSystemMessage(msg, false);
        }
        DataManager.save(PoliticalServer.server);
    }

    // ================================================================
    //  UTILITIES
    // ================================================================

    private static void broadcast(MinecraftServer server, Component text) {
        for (ServerPlayer pl : server.getPlayerList().getPlayers()) {
            pl.sendSystemMessage(text, false);
        }
    }

    private static void broadcastToParty(String partyId, Component text) {
        if (PoliticalServer.server == null) return;
        Party p = parties.get(partyId);
        if (p == null) return;
        for (String uuid : p.members.keySet()) {
            ServerPlayer pl = PoliticalServer.server.getPlayerList().getPlayer(java.util.UUID.fromString(uuid));
            if (pl != null) {
                pl.sendSystemMessage(text, false);
            }
        }
    }

    // ================================================================
    //  PARTY COMMISSIONS (mage-style missions for support)
    // ================================================================

    public enum PartyCommission {
        KILL_MOBS("Slay 50 Hostile Mobs", 50, 5, "Kill hostile mobs for your party", Items.DIAMOND_SWORD),
        MINE_BLOCKS("Mine 200 Blocks", 200, 4, "Mine blocks for your party", Items.DIAMOND_PICKAXE),
        CAST_SPELLS("Cast 10 Spells", 10, 3, "Cast magic spells", Items.BLAZE_POWDER),
        COMPLETE_BOUNTY("Complete a Bounty Quest", 1, 10, "Complete any slayer bounty quest", Items.WITHER_SKELETON_SKULL),
        CRAFT_ITEMS("Craft 10 Custom Items", 10, 5, "Craft items from /recipe", Items.CRAFTING_TABLE),
        FISH_CATCHES("Catch 20 Fish", 20, 4, "Catch fish with a fishing rod", Items.FISHING_ROD),
        EARN_XP("Earn 500 XP Orbs", 500, 3, "Gain vanilla experience", Items.EXPERIENCE_BOTTLE),
        DONATE_COINS("Donate 25,000 Coins", 25_000, 6, "Donate coins to your party", Items.GOLD_INGOT);

        public final String displayName;
        public final int target;
        public final int supportReward;
        public final String description;
        public final net.minecraft.world.item.Item icon;

        PartyCommission(String name, int target, int support, String desc, net.minecraft.world.item.Item icon) {
            this.displayName = name;
            this.target = target;
            this.supportReward = support;
            this.description = desc;
            this.icon = icon;
        }
    }

    private static final Map<String, String> activePartyCommission = new HashMap<>();
    private static final Map<String, Integer> partyCommissionProgress = new HashMap<>();

    public static PartyCommission getActivePartyCommission(String uuid) {
        String name = activePartyCommission.get(uuid);
        if (name == null) return null;
        try { return PartyCommission.valueOf(name); } catch (Exception e) { return null; }
    }

    public static int getPartyCommissionProgress(String uuid) {
        return partyCommissionProgress.getOrDefault(uuid, 0);
    }

    public static boolean canStartPartyCommission(String uuid) {
        return activePartyCommission.get(uuid) == null && playerParty.get(uuid) != null;
    }

    public static void startPartyCommission(ServerPlayer player, PartyCommission commission) {
        String uuid = player.getUUID().toString();
        if (playerParty.get(uuid) == null) {
            player.sendSystemMessage(Component.literal("Join a party first!").withStyle(ChatFormatting.RED));
            return;
        }
        activePartyCommission.put(uuid, commission.name());
        partyCommissionProgress.put(uuid, 0);
        player.sendSystemMessage(Component.literal("⚜ Party Commission: " + commission.displayName).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal("  " + commission.description).withStyle(ChatFormatting.GRAY));
    }

    public static void progressPartyCommission(ServerPlayer player, String commissionName, int amount) {
        String uuid = player.getUUID().toString();
        PartyCommission active = getActivePartyCommission(uuid);
        if (active == null) return;
        if (!active.name().equals(commissionName)) return;

        int progress = partyCommissionProgress.getOrDefault(uuid, 0) + amount;
        partyCommissionProgress.put(uuid, progress);

        if (progress >= active.target) {
            String pid = playerParty.get(uuid);
            if (pid != null) {
                Party p = parties.get(pid);
                if (p != null) {
                    PartyMember m = p.members.computeIfAbsent(uuid, u -> new PartyMember(0, System.currentTimeMillis()));
                    m.support += active.supportReward;
                    player.sendSystemMessage(Component.literal("⚜ Commission Complete! +" + active.supportReward + " Party Support")
                            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
                    DataManager.save(PoliticalServer.server);
                }
            }
            activePartyCommission.remove(uuid);
            partyCommissionProgress.remove(uuid);
        }
    }

    public static long getSupport(String uuid) {
        String pid = playerParty.get(uuid);
        if (pid == null) {
            return 0L;
        }
        Party p = parties.get(pid);
        if (p == null) {
            return 0L;
        }
        PartyMember m = p.members.get(uuid);
        return m == null ? 0L : m.support;
    }

    public static void leaveParty(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        String pid = playerParty.remove(uuid);
        if (pid == null) {
            player.sendSystemMessage(Component.literal("You are not in a party.").withStyle(ChatFormatting.RED));
            return;
        }
        Party p = parties.get(pid);
        if (p != null) {
            p.members.remove(uuid);
        }
        removeRank(pid, uuid);
        player.sendSystemMessage(Component.literal("Left your party. Use /pp join to enlist again.").withStyle(ChatFormatting.YELLOW));
        DataManager.save(PoliticalServer.server);
    }
}
