package com.political;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * V5 Pillar 2 — owner of every {@link Guild}, of guilds.json, and of the guild XP economy.
 *
 * <p><b>Anti-AFK design.</b> Guild XP is only ever produced by discrete player actions that
 * already cost the player something: mob kills, custom crafts and expedition completions.
 * Each source has a hard per-player <i>daily</i> cap, and mob kills additionally carry a
 * minimum interval so that a mob grinder / AFK farm converts to essentially zero XP. Coin
 * deposits into the treasury award <b>no</b> XP at all, which removes the obvious
 * deposit/withdraw wash loop. No guild mechanic mints coins: treasury flows are strictly
 * transfers between a player, the guild and (via patronage) a settlement.</p>
 */
public final class GuildManager {

    private GuildManager() {}

    // ── tunables (balanced against: settlement registration 1k–1M, party 10k, war 10M) ──
    public static final long CREATION_COST = 250_000L;
    public static final long HALL_DECLARE_COST = 100_000L;   // paid from the guild treasury
    public static final long RENAME_COST = 50_000L;          // paid from the guild treasury
    public static final int MAX_LEVEL = 20;

    /** Daily guild-XP ceilings per player, per source. */
    public static final int DAILY_KILL_XP_CAP = 120;
    public static final int DAILY_CRAFT_XP_CAP = 60;
    public static final int DAILY_EXPEDITION_XP_CAP = 900;

    /** Minimum gap between two kill-XP awards. Kills a grinder's throughput dead. */
    public static final long KILL_XP_MIN_INTERVAL_MS = 2_000L;

    /** Daily withdrawal ceiling, by rank. MASTER is uncapped. */
    public static final long OFFICER_DAILY_WITHDRAW_CAP = 250_000L;

    public static final long WAYSTONE_COOLDOWN_MS = 600_000L; // 10 minutes

    // ── state ──

    private static final Map<String, Guild> guilds = new LinkedHashMap<>();
    private static final Map<String, DailyCounters> daily = new HashMap<>();
    private static final Map<String, Long> waystoneCooldowns = new HashMap<>();
    private static Path dataFile;
    private static int tickCounter = 0;
    private static boolean dirty = false;

    private static final int HALL_CYCLE_TICKS = 1200; // one pass per minute, like the economy

    /** Per-player, per-UTC-day activity counters. Gson-friendly. */
    public static class DailyCounters {
        public long day = 0L;
        public int killXp = 0;
        public int craftXp = 0;
        public int expeditionXp = 0;
        public long withdrawn = 0L;
        public long lastKillXpAt = 0L;

        public void rollIfNeeded(long today) {
            if (day != today) {
                day = today;
                killXp = 0;
                craftXp = 0;
                expeditionXp = 0;
                withdrawn = 0L;
            }
        }
    }

    /** Root document written to guilds.json. */
    private static class SaveFile {
        Map<String, Guild> guilds = new LinkedHashMap<>();
        Map<String, DailyCounters> daily = new HashMap<>();
    }

    // ════════════════════════════════════════════════════════════════════
    // LIFECYCLE
    // ════════════════════════════════════════════════════════════════════

    public static void initialize(Path worldRoot) {
        dataFile = worldRoot.resolve("guilds.json");
        load();
    }

    private static void load() {
        guilds.clear();
        daily.clear();
        if (dataFile == null || !Files.exists(dataFile)) return;
        try (Reader r = Files.newBufferedReader(dataFile)) {
            Gson g = new GsonBuilder().create();
            SaveFile sf = g.fromJson(r, SaveFile.class);
            if (sf != null) {
                if (sf.guilds != null) {
                    for (Map.Entry<String, Guild> e : sf.guilds.entrySet()) {
                        Guild guild = e.getValue();
                        if (guild != null && guild.id != null) guilds.put(guild.id, guild);
                    }
                }
                if (sf.daily != null) daily.putAll(sf.daily);
            }
        } catch (Exception e) {
            System.err.println("[GuildManager] Failed to load guilds.json: " + e.getMessage());
        }
    }

    public static void save() {
        if (dataFile == null) return;
        try {
            Files.createDirectories(dataFile.getParent());
            SaveFile sf = new SaveFile();
            sf.guilds.putAll(guilds);
            sf.daily.putAll(daily);
            Gson g = new GsonBuilder().setPrettyPrinting().create();
            try (Writer w = Files.newBufferedWriter(dataFile)) {
                g.toJson(sf, w);
            }
            dirty = false;
        } catch (Exception e) {
            System.err.println("[GuildManager] Failed to save guilds.json: " + e.getMessage());
        }
    }

    /**
     * Applies the hall happiness bonus once a minute and flushes pending writes.
     * The bonus is applied additively on top of whatever SettlementEconomy computed and is
     * clamped to 100, so it can never compound across cycles.
     */
    public static void tick(MinecraftServer server) {
        if (server == null) return;
        if (++tickCounter < HALL_CYCLE_TICKS) return;
        tickCounter = 0;

        for (Guild g : new ArrayList<>(guilds.values())) {
            if (g.hallSettlementId == null) continue;
            Settlement s = SettlementManager.get(g.hallSettlementId);
            if (s == null) {
                g.hallSettlementId = null;
                logTo(g, "Hall settlement no longer exists — hall cleared.");
                dirty = true;
                continue;
            }
            // A hall must remain TOWN+; if it decays, the charter lapses.
            if (!s.tierEnum().isAtLeast(SettlementTier.TOWN)) {
                g.hallSettlementId = null;
                logTo(g, "Hall lapsed: " + s.name + " fell below Town.");
                broadcastGuild(server, g, "§e⚑ Your guild hall at §f" + s.name + " §ehas lapsed.");
                dirty = true;
                continue;
            }
            int bonus = GuildPerks.hallHappinessBonus(s.id);
            if (bonus > 0) {
                s.happiness = Math.min(100, s.happiness + bonus);
            }
        }

        if (dirty) save();
    }

    // ════════════════════════════════════════════════════════════════════
    // ACCESSORS
    // ════════════════════════════════════════════════════════════════════

    public static Collection<Guild> allGuilds() {
        return new ArrayList<>(guilds.values());
    }

    public static Guild get(String guildId) {
        return guildId == null ? null : guilds.get(guildId);
    }

    public static Guild getGuildOf(String uuid) {
        if (uuid == null) return null;
        for (Guild g : guilds.values()) {
            if (g.isMember(uuid)) return g;
        }
        return null;
    }

    public static Guild getGuildOf(ServerPlayer player) {
        return player == null ? null : getGuildOf(player.getUUID().toString());
    }

    public static Guild guildWithHall(String settlementId) {
        if (settlementId == null) return null;
        for (Guild g : guilds.values()) {
            if (settlementId.equals(g.hallSettlementId)) return g;
        }
        return null;
    }

    /** Guilds that have invited this player and that they may still join. */
    public static List<Guild> pendingInvitesFor(String uuid) {
        List<Guild> out = new ArrayList<>();
        if (uuid == null) return out;
        for (Guild g : guilds.values()) {
            if (g.inviteSet().contains(uuid) && !g.isMember(uuid)) out.add(g);
        }
        return out;
    }

    public static Guild byName(String name) {
        if (name == null) return null;
        for (Guild g : guilds.values()) {
            if (g.name != null && g.name.equalsIgnoreCase(name)) return g;
        }
        return null;
    }

    public static Guild byTag(String tag) {
        if (tag == null) return null;
        for (Guild g : guilds.values()) {
            if (g.tag != null && g.tag.equalsIgnoreCase(tag)) return g;
        }
        return null;
    }

    /** Guilds sorted by level then XP, highest first. */
    public static List<Guild> leaderboard() {
        List<Guild> out = new ArrayList<>(guilds.values());
        out.sort((a, b) -> {
            if (a.level != b.level) return Integer.compare(b.level, a.level);
            return Long.compare(b.xp, a.xp);
        });
        return out;
    }

    // ════════════════════════════════════════════════════════════════════
    // LEVELS & XP
    // ════════════════════════════════════════════════════════════════════

    /** Total cumulative XP needed to BE the given level. Level 1 = 0. */
    public static long totalXpForLevel(int level) {
        if (level <= 1) return 0L;
        int n = Math.min(MAX_LEVEL, level) - 1;
        return 2500L * n * n + 5000L * n;
    }

    /** XP still needed to reach the next level, or 0 at max level. */
    public static long xpToNextLevel(Guild g) {
        if (g == null || g.level >= MAX_LEVEL) return 0L;
        return Math.max(0L, totalXpForLevel(g.level + 1) - g.xp);
    }

    /** Progress into the current level, 0.0 .. 1.0. */
    public static double levelProgress(Guild g) {
        if (g == null || g.level >= MAX_LEVEL) return 1.0;
        long base = totalXpForLevel(g.level);
        long next = totalXpForLevel(g.level + 1);
        if (next <= base) return 1.0;
        return Math.max(0.0, Math.min(1.0, (double) (g.xp - base) / (double) (next - base)));
    }

    /** Adds XP and resolves any level-ups. Returns levels gained. */
    private static int addXp(MinecraftServer server, Guild g, long amount, String reason) {
        if (g == null || amount <= 0) return 0;
        long scaled = Math.round(amount * GuildPerks.guildXpMultiplier(g));
        if (scaled <= 0) return 0;
        g.xp += scaled;
        int gained = 0;
        while (g.level < MAX_LEVEL && g.xp >= totalXpForLevel(g.level + 1)) {
            g.level++;
            gained++;
        }
        if (gained > 0) {
            logTo(g, "Reached guild level " + g.level + ".");
            if (server != null) {
                broadcastGuild(server, g, "§6✦ §e" + g.name + " §6reached guild level §f" + g.level + "§6!");
            }
        }
        dirty = true;
        return gained;
    }

    private static long todayIndex() {
        return System.currentTimeMillis() / 86_400_000L;
    }

    private static DailyCounters counters(String uuid) {
        DailyCounters dc = daily.computeIfAbsent(uuid, k -> new DailyCounters());
        dc.rollIfNeeded(todayIndex());
        return dc;
    }

    // ── XP entry points (call these from event hooks) ──

    /**
     * Award guild XP for a mob kill. Rate-limited AND daily-capped: a mob farm produces at
     * most {@link #DAILY_KILL_XP_CAP} XP a day no matter how many mobs die.
     */
    public static void onMobKill(ServerPlayer player, boolean isBoss) {
        if (player == null) return;
        String uuid = player.getUUID().toString();
        Guild g = getGuildOf(uuid);
        if (g == null) return;

        DailyCounters dc = counters(uuid);
        long now = System.currentTimeMillis();
        if (now - dc.lastKillXpAt < KILL_XP_MIN_INTERVAL_MS) return;
        if (dc.killXp >= DAILY_KILL_XP_CAP) return;

        int award = isBoss ? 10 : 1;
        award = (int) Math.min(award, DAILY_KILL_XP_CAP - dc.killXp);
        if (award <= 0) return;

        dc.killXp += award;
        dc.lastKillXpAt = now;
        award = (int) Math.round(award * GuildPerks.hallResidentXpMultiplier(g, uuid));
        addXp(serverOf(player), g, award, "kills");
    }

    /** Award guild XP for crafting a custom item. Daily-capped. */
    public static void onCustomCraft(ServerPlayer player) {
        if (player == null) return;
        String uuid = player.getUUID().toString();
        Guild g = getGuildOf(uuid);
        if (g == null) return;

        DailyCounters dc = counters(uuid);
        if (dc.craftXp >= DAILY_CRAFT_XP_CAP) return;
        int award = (int) Math.min(3, DAILY_CRAFT_XP_CAP - dc.craftXp);
        if (award <= 0) return;
        dc.craftXp += award;
        award = (int) Math.round(award * GuildPerks.hallResidentXpMultiplier(g, uuid));
        addXp(serverOf(player), g, award, "crafting");
    }

    /** Award guild XP when a member finishes an expedition. Daily-capped at 3 runs of value. */
    public static void onExpeditionComplete(ServerPlayer player) {
        if (player == null) return;
        String uuid = player.getUUID().toString();
        Guild g = getGuildOf(uuid);
        if (g == null) return;

        DailyCounters dc = counters(uuid);
        if (dc.expeditionXp >= DAILY_EXPEDITION_XP_CAP) return;
        int award = (int) Math.min(300, DAILY_EXPEDITION_XP_CAP - dc.expeditionXp);
        if (award <= 0) return;
        dc.expeditionXp += award;

        double mult = GuildPerks.expeditionGuildXpMultiplier(g)
                * GuildPerks.hallResidentXpMultiplier(g, uuid);
        addXp(serverOf(player), g, Math.round(award * mult), "expedition");
        logTo(g, DataManager.getPlayerName(uuid) + " completed an expedition.");
    }

    /**
     * Award guild XP when the guild's hall settlement is promoted a tier. One-off per
     * promotion, and driven by the settlement system itself, so it cannot be spammed.
     */
    public static void onHallTierUp(MinecraftServer server, Settlement s, SettlementTier newTier) {
        if (s == null) return;
        Guild g = guildWithHall(s.id);
        if (g == null) return;
        long award = 500L * Math.max(1, newTier.rank());
        addXp(server, g, award, "hall growth");
        logTo(g, "Hall " + s.name + " grew to " + newTier.displayName + " (+" + award + " XP).");
    }

    private static MinecraftServer serverOf(ServerPlayer player) {
        if (player == null) return null;
        try {
            return ((ServerLevel) player.level()).getServer();
        } catch (Exception e) {
            return PoliticalServer.server;
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // LOG
    // ════════════════════════════════════════════════════════════════════

    private static final SimpleDateFormat LOG_FMT = new SimpleDateFormat("dd/MM HH:mm", Locale.ROOT);

    public static void logTo(Guild g, String text) {
        if (g == null || text == null) return;
        List<String> log = g.logList();
        log.add("§8[" + LOG_FMT.format(new Date()) + "] §7" + text);
        while (log.size() > Guild.LOG_LIMIT) log.remove(0);
        dirty = true;
    }

    public static void broadcastGuild(MinecraftServer server, Guild g, String message) {
        if (server == null || g == null) return;
        for (String uuid : g.memberMap().keySet()) {
            ServerPlayer p = playerFor(server, uuid);
            if (p != null) p.sendSystemMessage(Component.literal(message));
        }
    }

    private static ServerPlayer playerFor(MinecraftServer server, String uuid) {
        if (server == null || uuid == null) return null;
        try {
            return server.getPlayerList().getPlayer(UUID.fromString(uuid));
        } catch (Exception e) {
            return null;
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // CREATION / MEMBERSHIP
    // ════════════════════════════════════════════════════════════════════

    public static String sanitizeName(String raw) {
        if (raw == null) return "";
        String s = raw.trim().replaceAll("[^A-Za-z0-9 '\\-]", "");
        s = s.replaceAll("\\s+", " ");
        if (s.length() > 24) s = s.substring(0, 24);
        return s.trim();
    }

    public static String sanitizeTag(String raw) {
        if (raw == null) return "";
        String s = raw.trim().replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        if (s.length() > 5) s = s.substring(0, 5);
        return s;
    }

    /**
     * Founds a guild. Costs {@link #CREATION_COST} coins and consumes one Guild Charter
     * from the founder's inventory.
     */
    public static boolean createGuild(ServerPlayer player, String rawName, String rawTag) {
        if (player == null) return false;
        String uuid = player.getUUID().toString();

        if (getGuildOf(uuid) != null) {
            msg(player, "§cYou are already in a guild. Leave it first.");
            return false;
        }
        String name = sanitizeName(rawName);
        String tag = sanitizeTag(rawTag);
        if (name.length() < 3) {
            msg(player, "§cGuild names must be 3–24 letters, digits, spaces, ' or -.");
            return false;
        }
        if (tag.length() < 2) {
            msg(player, "§cGuild tags must be 2–5 letters or digits.");
            return false;
        }
        if (byName(name) != null) {
            msg(player, "§cA guild named §f" + name + " §calready exists.");
            return false;
        }
        if (byTag(tag) != null) {
            msg(player, "§cThe tag §f[" + tag + "] §cis taken.");
            return false;
        }
        if (!GuildItems.hasCharter(player)) {
            msg(player, "§cYou need a §6Guild Charter §cto found a guild. §7Craft one first.");
            return false;
        }
        if (CoinManager.getCoinsLong(player) < CREATION_COST) {
            msg(player, "§cFounding a guild costs §e" + fmt(CREATION_COST) + " coins§c.");
            return false;
        }
        if (!CoinManager.removeCoins(player, CREATION_COST)) {
            msg(player, "§cPayment failed.");
            return false;
        }
        if (!GuildItems.consumeCharter(player)) {
            // Refund: never take coins without taking the charter.
            CoinManager.giveCoinsQuiet(player, CREATION_COST);
            msg(player, "§cCould not consume the Guild Charter — nothing was charged.");
            return false;
        }

        Guild g = new Guild(UUID.randomUUID().toString(), name, tag, uuid);
        g.motto = "For the Order.";
        guilds.put(g.id, g);
        logTo(g, "Guild founded by " + player.getName().getString() + ".");
        dirty = true;
        save();

        msg(player, "§6✦ Guild founded: §e" + name + " §8[" + tag + "]");
        MinecraftServer server = serverOf(player);
        if (server != null) {
            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                p.sendSystemMessage(Component.literal("§6⚑ A new guild has been chartered: §e" + name
                        + " §8[" + tag + "]").withStyle(ChatFormatting.GOLD));
            }
        }
        return true;
    }

    public static boolean invite(ServerPlayer actor, ServerPlayer target) {
        if (actor == null || target == null) return false;
        Guild g = getGuildOf(actor);
        if (g == null) { msg(actor, "§cYou are not in a guild."); return false; }
        Guild.Rank r = g.rankOf(actor.getUUID().toString());
        if (r == null || !r.isAtLeast(Guild.Rank.OFFICER)) {
            msg(actor, "§cOnly Officers and the Master may invite.");
            return false;
        }
        String tUuid = target.getUUID().toString();
        if (g.isMember(tUuid)) { msg(actor, "§cThey are already in your guild."); return false; }
        if (getGuildOf(tUuid) != null) { msg(actor, "§cThey already belong to another guild."); return false; }
        if (g.memberCount() >= Guild.MAX_MEMBERS) {
            msg(actor, "§cYour guild is full (" + Guild.MAX_MEMBERS + ").");
            return false;
        }
        if (!g.inviteSet().add(tUuid)) {
            msg(actor, "§eThey already have a pending invite.");
            return false;
        }
        logTo(g, actor.getName().getString() + " invited " + target.getName().getString() + ".");
        dirty = true;
        save();
        msg(actor, "§a✓ Invited §f" + target.getName().getString() + "§a.");
        msg(target, "§6⚑ §e" + g.name + " §6has invited you. §7Open the Guild menu to accept.");
        return true;
    }

    public static boolean acceptInvite(ServerPlayer player, String guildId) {
        if (player == null) return false;
        String uuid = player.getUUID().toString();
        if (getGuildOf(uuid) != null) { msg(player, "§cLeave your current guild first."); return false; }
        Guild g = get(guildId);
        if (g == null || !g.inviteSet().contains(uuid)) {
            msg(player, "§cThat invitation is no longer valid.");
            return false;
        }
        if (g.memberCount() >= Guild.MAX_MEMBERS) {
            msg(player, "§cThat guild is full.");
            return false;
        }
        g.inviteSet().remove(uuid);
        g.setRank(uuid, Guild.Rank.INITIATE);
        logTo(g, player.getName().getString() + " joined as an Initiate.");
        dirty = true;
        save();
        msg(player, "§a✓ You joined §e" + g.name + "§a.");
        broadcastGuild(serverOf(player), g, "§a+ §f" + player.getName().getString() + " §7joined the guild.");
        return true;
    }

    public static boolean declineInvite(ServerPlayer player, String guildId) {
        Guild g = get(guildId);
        if (player == null || g == null) return false;
        if (g.inviteSet().remove(player.getUUID().toString())) {
            dirty = true;
            save();
            msg(player, "§7Invitation declined.");
            return true;
        }
        return false;
    }

    public static boolean leave(ServerPlayer player) {
        if (player == null) return false;
        String uuid = player.getUUID().toString();
        Guild g = getGuildOf(uuid);
        if (g == null) { msg(player, "§cYou are not in a guild."); return false; }
        Guild.Rank r = g.rankOf(uuid);
        if (r == Guild.Rank.MASTER && g.memberCount() > 1) {
            msg(player, "§cTransfer mastery to another member before you leave.");
            return false;
        }
        g.memberMap().remove(uuid);
        if (g.memberCount() == 0) {
            // Last member out: the guild dissolves and its treasury is forfeit.
            guilds.remove(g.id);
            msg(player, "§7Your guild dissolved — you were the last member.");
        } else {
            logTo(g, player.getName().getString() + " left the guild.");
            broadcastGuild(serverOf(player), g, "§c- §f" + player.getName().getString() + " §7left the guild.");
            msg(player, "§7You left §e" + g.name + "§7.");
        }
        dirty = true;
        save();
        return true;
    }

    public static boolean kick(ServerPlayer actor, String targetUuid) {
        if (actor == null || targetUuid == null) return false;
        Guild g = getGuildOf(actor);
        if (g == null) return false;
        String aUuid = actor.getUUID().toString();
        Guild.Rank ar = g.rankOf(aUuid);
        Guild.Rank tr = g.rankOf(targetUuid);
        if (ar == null || tr == null) return false;
        if (aUuid.equals(targetUuid)) { msg(actor, "§cUse Leave instead."); return false; }
        if (!ar.isAtLeast(Guild.Rank.OFFICER)) { msg(actor, "§cOnly Officers and the Master may kick."); return false; }
        if (tr.weight >= ar.weight) { msg(actor, "§cYou cannot kick someone of equal or higher rank."); return false; }

        g.memberMap().remove(targetUuid);
        String name = DataManager.getPlayerName(targetUuid);
        logTo(g, actor.getName().getString() + " removed " + name + ".");
        dirty = true;
        save();
        msg(actor, "§a✓ Removed §f" + name + "§a.");
        ServerPlayer target = playerFor(serverOf(actor), targetUuid);
        if (target != null) msg(target, "§cYou were removed from §e" + g.name + "§c.");
        return true;
    }

    public static boolean promote(ServerPlayer actor, String targetUuid) {
        Guild g = getGuildOf(actor);
        if (g == null || targetUuid == null) return false;
        String aUuid = actor.getUUID().toString();
        Guild.Rank ar = g.rankOf(aUuid);
        Guild.Rank tr = g.rankOf(targetUuid);
        if (ar == null || tr == null) return false;
        if (!ar.isAtLeast(Guild.Rank.OFFICER)) { msg(actor, "§cOnly Officers and the Master may promote."); return false; }
        Guild.Rank next = tr.up();
        if (next == tr) { msg(actor, "§cThey are already at the highest promotable rank."); return false; }
        // You may never promote someone to your own rank or above (Master aside).
        if (ar != Guild.Rank.MASTER && next.weight >= ar.weight) {
            msg(actor, "§cOnly the Master may create Officers.");
            return false;
        }
        g.setRank(targetUuid, next);
        String name = DataManager.getPlayerName(targetUuid);
        logTo(g, name + " promoted to " + next.displayName + ".");
        dirty = true;
        save();
        msg(actor, "§a✓ §f" + name + " §ais now " + next.colored() + "§a.");
        return true;
    }

    public static boolean demote(ServerPlayer actor, String targetUuid) {
        Guild g = getGuildOf(actor);
        if (g == null || targetUuid == null) return false;
        String aUuid = actor.getUUID().toString();
        Guild.Rank ar = g.rankOf(aUuid);
        Guild.Rank tr = g.rankOf(targetUuid);
        if (ar == null || tr == null) return false;
        if (aUuid.equals(targetUuid)) { msg(actor, "§cYou cannot demote yourself."); return false; }
        if (!ar.isAtLeast(Guild.Rank.OFFICER)) { msg(actor, "§cOnly Officers and the Master may demote."); return false; }
        if (tr.weight >= ar.weight) { msg(actor, "§cYou cannot demote someone of equal or higher rank."); return false; }
        Guild.Rank next = tr.down();
        if (next == tr) { msg(actor, "§cThey are already an Initiate."); return false; }
        g.setRank(targetUuid, next);
        String name = DataManager.getPlayerName(targetUuid);
        logTo(g, name + " demoted to " + next.displayName + ".");
        dirty = true;
        save();
        msg(actor, "§e" + name + " §7is now " + next.colored() + "§7.");
        return true;
    }

    /** Hands the Master rank to another member. The old Master becomes an Officer. */
    public static boolean transferMastery(ServerPlayer actor, String targetUuid) {
        Guild g = getGuildOf(actor);
        if (g == null || targetUuid == null) return false;
        String aUuid = actor.getUUID().toString();
        if (g.rankOf(aUuid) != Guild.Rank.MASTER) { msg(actor, "§cOnly the Master may do that."); return false; }
        if (!g.isMember(targetUuid)) return false;
        if (aUuid.equals(targetUuid)) return false;
        g.setRank(targetUuid, Guild.Rank.MASTER);
        g.setRank(aUuid, Guild.Rank.OFFICER);
        String name = DataManager.getPlayerName(targetUuid);
        logTo(g, "Mastery transferred to " + name + ".");
        dirty = true;
        save();
        broadcastGuild(serverOf(actor), g, "§6✦ §f" + name + " §6is now Guild Master.");
        return true;
    }

    // ════════════════════════════════════════════════════════════════════
    // TREASURY
    // ════════════════════════════════════════════════════════════════════

    public static boolean deposit(ServerPlayer player, long amount) {
        if (player == null || amount <= 0) return false;
        Guild g = getGuildOf(player);
        if (g == null) { msg(player, "§cYou are not in a guild."); return false; }
        if (CoinManager.getCoinsLong(player) < amount) {
            msg(player, "§cYou only have §e" + fmt(CoinManager.getCoinsLong(player)) + " coins§c.");
            return false;
        }
        if (!CoinManager.removeCoins(player, amount)) { msg(player, "§cPayment failed."); return false; }
        g.treasury += amount;
        logTo(g, player.getName().getString() + " deposited " + fmt(amount) + " coins.");
        dirty = true;
        save();
        msg(player, "§a✓ Deposited §e" + fmt(amount) + " coins §ato the guild treasury.");
        return true;
    }

    /** Remaining coins this player may withdraw today. */
    public static long withdrawalHeadroom(Guild g, String uuid) {
        if (g == null || uuid == null) return 0L;
        Guild.Rank r = g.rankOf(uuid);
        if (r == null || !r.isAtLeast(Guild.Rank.OFFICER)) return 0L;
        if (r == Guild.Rank.MASTER) return g.treasury;
        DailyCounters dc = counters(uuid);
        return Math.max(0L, Math.min(g.treasury, OFFICER_DAILY_WITHDRAW_CAP - dc.withdrawn));
    }

    public static boolean withdraw(ServerPlayer player, long amount) {
        if (player == null || amount <= 0) return false;
        Guild g = getGuildOf(player);
        if (g == null) return false;
        String uuid = player.getUUID().toString();
        Guild.Rank r = g.rankOf(uuid);
        if (r == null || !r.isAtLeast(Guild.Rank.OFFICER)) {
            msg(player, "§cOnly Officers and the Master may withdraw.");
            return false;
        }
        if (amount > g.treasury) { msg(player, "§cThe treasury holds only §e" + fmt(g.treasury) + " coins§c."); return false; }
        long headroom = withdrawalHeadroom(g, uuid);
        if (amount > headroom) {
            msg(player, "§cDaily withdrawal limit reached. §7Remaining today: §e" + fmt(headroom) + " coins§7.");
            return false;
        }
        g.treasury -= amount;
        if (r != Guild.Rank.MASTER) counters(uuid).withdrawn += amount;
        CoinManager.giveCoinsQuiet(player, amount);
        logTo(g, player.getName().getString() + " withdrew " + fmt(amount) + " coins.");
        dirty = true;
        save();
        msg(player, "§a✓ Withdrew §e" + fmt(amount) + " coins§a.");
        broadcastGuild(serverOf(player), g, "§7⚑ §f" + player.getName().getString()
                + " §7withdrew §e" + fmt(amount) + " §7from the treasury.");
        return true;
    }

    /**
     * Moves coins from the guild treasury into the hall settlement's treasury. A pure
     * transfer: no coins are created, so this cannot inflate the economy.
     */
    public static boolean patronage(ServerPlayer player, long amount) {
        if (player == null || amount <= 0) return false;
        Guild g = getGuildOf(player);
        if (g == null) return false;
        Guild.Rank r = g.rankOf(player.getUUID().toString());
        if (r == null || !r.isAtLeast(Guild.Rank.OFFICER)) {
            msg(player, "§cOnly Officers and the Master may fund the hall.");
            return false;
        }
        if (g.hallSettlementId == null) { msg(player, "§cYour guild has no hall."); return false; }
        Settlement s = SettlementManager.get(g.hallSettlementId);
        if (s == null) { msg(player, "§cYour hall settlement no longer exists."); return false; }
        if (amount > g.treasury) { msg(player, "§cThe treasury cannot cover that."); return false; }

        g.treasury -= amount;
        s.treasury += amount;
        logTo(g, player.getName().getString() + " sent " + fmt(amount) + " coins to " + s.name + ".");
        dirty = true;
        save();
        SettlementManager.save();
        msg(player, "§a✓ Sent §e" + fmt(amount) + " coins §ato §f" + s.name + "§a.");
        return true;
    }

    /**
     * Refund hook for {@link GuildPerks.Perk#PATRON_SEAL}: returns half of a settlement
     * registration fee from the guild treasury. Returns the amount actually refunded.
     */
    public static long refundRegistrationFee(ServerPlayer player, long feePaid) {
        if (player == null || feePaid <= 0) return 0L;
        String uuid = player.getUUID().toString();
        if (!GuildPerks.hasPatronSeal(uuid)) return 0L;
        Guild g = getGuildOf(uuid);
        if (g == null) return 0L;
        long refund = feePaid / 2L;
        if (refund <= 0 || g.treasury < refund) return 0L;
        g.treasury -= refund;
        CoinManager.giveCoinsQuiet(player, refund);
        logTo(g, "Patron's Seal refunded " + fmt(refund) + " coins to " + player.getName().getString() + ".");
        dirty = true;
        save();
        msg(player, "§a✦ Patron's Seal refunded §e" + fmt(refund) + " coins§a.");
        return refund;
    }

    // ════════════════════════════════════════════════════════════════════
    // PERKS
    // ════════════════════════════════════════════════════════════════════

    public static boolean purchasePerk(ServerPlayer player, GuildPerks.Perk perk) {
        if (player == null || perk == null) return false;
        Guild g = getGuildOf(player);
        if (g == null) return false;
        if (g.rankOf(player.getUUID().toString()) != Guild.Rank.MASTER) {
            msg(player, "§cOnly the Guild Master may purchase perks.");
            return false;
        }
        if (g.hasPerk(perk.id)) { msg(player, "§eAlready unlocked."); return false; }
        if (g.level < perk.requiredLevel) {
            msg(player, "§cRequires guild level §f" + perk.requiredLevel + "§c (you are §f" + g.level + "§c).");
            return false;
        }
        if (perk.requires != null && !g.hasPerk(perk.requires)) {
            GuildPerks.Perk req = GuildPerks.Perk.byId(perk.requires);
            msg(player, "§cRequires §f" + (req == null ? perk.requires : req.display) + " §cfirst.");
            return false;
        }
        if (g.treasury < perk.cost) {
            msg(player, "§cThe treasury holds §e" + fmt(g.treasury) + "§c, needs §e" + fmt(perk.cost) + "§c.");
            return false;
        }
        g.treasury -= perk.cost;
        g.perkSet().add(perk.id);
        logTo(g, "Unlocked perk: " + perk.display + " (-" + fmt(perk.cost) + " coins).");
        dirty = true;
        save();
        broadcastGuild(serverOf(player), g, "§b✦ Guild perk unlocked: §f" + perk.display);
        return true;
    }

    // ════════════════════════════════════════════════════════════════════
    // HALL
    // ════════════════════════════════════════════════════════════════════

    /** Minimum guild members that must be registered to a settlement to claim it as a hall. */
    public static final int HALL_MIN_RESIDENT_MEMBERS = 3;

    public static boolean declareHall(ServerPlayer player, Settlement s) {
        if (player == null || s == null) return false;
        Guild g = getGuildOf(player);
        if (g == null) return false;
        if (g.rankOf(player.getUUID().toString()) != Guild.Rank.MASTER) {
            msg(player, "§cOnly the Guild Master may declare a hall.");
            return false;
        }
        if (!s.tierEnum().isAtLeast(SettlementTier.TOWN)) {
            msg(player, "§cA hall must be declared at a §fTown §cor larger settlement.");
            return false;
        }
        Guild other = guildWithHall(s.id);
        if (other != null && !other.id.equals(g.id)) {
            msg(player, "§c" + s.name + " is already the hall of §f" + other.name + "§c.");
            return false;
        }
        if (s.id.equals(g.hallSettlementId)) { msg(player, "§eThat is already your hall."); return false; }

        int resident = 0;
        for (String uuid : g.memberMap().keySet()) {
            if (s.registered().contains(uuid)) resident++;
        }
        if (resident < HALL_MIN_RESIDENT_MEMBERS) {
            msg(player, "§cYou need §f" + HALL_MIN_RESIDENT_MEMBERS + " §cguild members registered to §f"
                    + s.name + "§c (currently §f" + resident + "§c).");
            return false;
        }
        if (g.treasury < HALL_DECLARE_COST) {
            msg(player, "§cDeclaring a hall costs §e" + fmt(HALL_DECLARE_COST) + " coins §cfrom the treasury.");
            return false;
        }
        g.treasury -= HALL_DECLARE_COST;
        g.hallSettlementId = s.id;
        g.hallDeclaredAt = System.currentTimeMillis();
        logTo(g, "Hall declared at " + s.name + ".");
        dirty = true;
        save();
        broadcastGuild(serverOf(player), g, "§6⚑ Guild hall declared at §f" + s.name + "§6.");
        return true;
    }

    public static boolean abandonHall(ServerPlayer player) {
        Guild g = getGuildOf(player);
        if (g == null || player == null) return false;
        if (g.rankOf(player.getUUID().toString()) != Guild.Rank.MASTER) {
            msg(player, "§cOnly the Guild Master may abandon the hall.");
            return false;
        }
        if (g.hallSettlementId == null) return false;
        Settlement s = SettlementManager.get(g.hallSettlementId);
        g.hallSettlementId = null;
        logTo(g, "Hall abandoned" + (s == null ? "." : " at " + s.name + "."));
        dirty = true;
        save();
        msg(player, "§7Guild hall abandoned.");
        return true;
    }

    // ── waystone ──

    public static long waystoneCooldownRemaining(String uuid) {
        Long last = waystoneCooldowns.get(uuid);
        if (last == null) return 0L;
        long left = WAYSTONE_COOLDOWN_MS - (System.currentTimeMillis() - last);
        return Math.max(0L, left);
    }

    public static boolean useWaystone(ServerPlayer player) {
        if (player == null) return false;
        String uuid = player.getUUID().toString();
        Guild g = getGuildOf(uuid);
        if (g == null) return false;
        if (!GuildPerks.hasWaystone(uuid)) {
            msg(player, "§cYour guild has not unlocked the Guild Waystone.");
            return false;
        }
        if (g.hallSettlementId == null) { msg(player, "§cYour guild has no hall to travel to."); return false; }
        Settlement s = SettlementManager.get(g.hallSettlementId);
        if (s == null) { msg(player, "§cYour hall settlement no longer exists."); return false; }
        long left = waystoneCooldownRemaining(uuid);
        if (left > 0) {
            msg(player, "§cWaystone recharging — §f" + (left / 1000L) + "s §cremaining.");
            return false;
        }
        MinecraftServer server = serverOf(player);
        ServerLevel level = SettlementManager.levelFor(server, s);
        if (level == null) { msg(player, "§cThat dimension is not loaded."); return false; }

        waystoneCooldowns.put(uuid, System.currentTimeMillis());
        PlayerTeleportHelper.teleport(player, level,
                s.cornerstoneX + 0.5, s.cornerstoneY + 1.0, s.cornerstoneZ + 0.5,
                player.getYRot(), player.getXRot());
        level.playSound(null, s.cornerstonePos(), net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME,
                net.minecraft.sounds.SoundSource.PLAYERS, 0.8f, 1.2f);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,
                s.cornerstoneX + 0.5, s.cornerstoneY + 1.5, s.cornerstoneZ + 0.5,
                40, 0.5, 0.8, 0.5, 0.02);
        msg(player, "§b✦ You step through to §f" + s.name + "§b.");
        return true;
    }

    // ── admin ──

    public static boolean disband(ServerPlayer actor, Guild g) {
        if (g == null || actor == null) return false;
        boolean isMaster = g.rankOf(actor.getUUID().toString()) == Guild.Rank.MASTER;
        if (!isMaster && !SpawnProtectionManager.isOperator(actor)) {
            msg(actor, "§cOnly the Guild Master or an operator may disband a guild.");
            return false;
        }
        MinecraftServer server = serverOf(actor);
        broadcastGuild(server, g, "§c⚑ §f" + g.name + " §chas been disbanded. Its treasury is forfeit.");
        guilds.remove(g.id);
        dirty = true;
        save();
        return true;
    }

    public static boolean rename(ServerPlayer player, String rawName, String rawTag) {
        Guild g = getGuildOf(player);
        if (g == null || player == null) return false;
        if (g.rankOf(player.getUUID().toString()) != Guild.Rank.MASTER) {
            msg(player, "§cOnly the Guild Master may rename the guild.");
            return false;
        }
        String name = sanitizeName(rawName);
        String tag = sanitizeTag(rawTag);
        if (name.length() < 3 || tag.length() < 2) { msg(player, "§cInvalid name or tag."); return false; }
        Guild byName = byName(name);
        Guild byTag = byTag(tag);
        if (byName != null && !byName.id.equals(g.id)) { msg(player, "§cThat name is taken."); return false; }
        if (byTag != null && !byTag.id.equals(g.id)) { msg(player, "§cThat tag is taken."); return false; }
        if (g.treasury < RENAME_COST) {
            msg(player, "§cRenaming costs §e" + fmt(RENAME_COST) + " coins §cfrom the treasury.");
            return false;
        }
        g.treasury -= RENAME_COST;
        String old = g.name;
        g.name = name;
        g.tag = tag;
        logTo(g, "Renamed from " + old + " to " + name + " [" + tag + "].");
        dirty = true;
        save();
        msg(player, "§a✓ Guild renamed to §e" + name + " §8[" + tag + "]");
        return true;
    }

    public static boolean setMotto(ServerPlayer player, String motto) {
        Guild g = getGuildOf(player);
        if (g == null || player == null) return false;
        Guild.Rank r = g.rankOf(player.getUUID().toString());
        if (r == null || !r.isAtLeast(Guild.Rank.OFFICER)) {
            msg(player, "§cOnly Officers and the Master may set the motto.");
            return false;
        }
        String m = motto == null ? "" : motto.trim();
        if (m.length() > 48) m = m.substring(0, 48);
        g.motto = m;
        logTo(g, "Motto changed.");
        dirty = true;
        save();
        msg(player, "§a✓ Motto set.");
        return true;
    }

    // ── daily counter read-outs for the GUI ──

    public static int dailyKillXp(String uuid) { return counters(uuid).killXp; }
    public static int dailyCraftXp(String uuid) { return counters(uuid).craftXp; }
    public static int dailyExpeditionXp(String uuid) { return counters(uuid).expeditionXp; }

    // ── util ──

    public static String fmt(long v) {
        return String.format(Locale.ROOT, "%,d", v);
    }

    private static void msg(ServerPlayer p, String s) {
        if (p != null) p.sendSystemMessage(Component.literal(s));
    }
}
