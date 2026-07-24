package com.political;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

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
 * V7 Pillar A — owner of every {@link SorcererClan}, of clans.json, and of the clan prestige
 * economy. A clan sits between the individual sorcerer (V6) and settlement politics (V4).
 *
 * <p><b>Anti-abuse.</b> Prestige is only ever produced by discrete deeds that already cost the
 * player something: slaying cursed spirits, felling special grade bosses, and winning feuds.
 * Each source has a hard per-player daily cap, spirit prestige additionally carries a minimum
 * interval so a mob grinder or AFK farm converts to essentially zero prestige, and no clan
 * mechanic mints coins beyond a tiny, daily-capped kill dividend. Treasury and tribute flows are
 * pure transfers.</p>
 */
public final class ClanManager {

    private ClanManager() {}

    // ── tunables (balanced against guild 250k, party 10k, war 10M) ──
    public static final long FOUND_COST = 500_000L;
    public static final long HALL_DECLARE_COST = 150_000L;   // from the clan treasury
    public static final int MAX_PRESTIGE_LEVEL = 10;

    /** Prestige awards. */
    public static final long SPIRIT_PRESTIGE = 2L;
    public static final long BOSS_PRESTIGE = 150L;

    /** Daily per-player prestige ceilings. */
    public static final long DAILY_SPIRIT_PRESTIGE_CAP = 200L;
    public static final long DAILY_BOSS_PRESTIGE_CAP = 450L;
    /** Minimum gap between two spirit-prestige awards. Kills a grinder's throughput dead. */
    public static final long SPIRIT_MIN_INTERVAL_MS = 2_000L;

    /** Coins the Kinship Dividend perk may drip into the treasury per player per day. */
    public static final long DAILY_DIVIDEND_CAP = 1_000L;

    /** Daily withdrawal ceiling for Elders. The Head is uncapped. */
    public static final long ELDER_DAILY_WITHDRAW_CAP = 200_000L;

    /** Minimum clan members registered to a settlement to claim it as a hall. */
    public static final int HALL_MIN_RESIDENT_MEMBERS = 3;

    // ── state ──
    private static final Map<String, SorcererClan> clans = new LinkedHashMap<>();
    private static final Map<String, DailyCounters> daily = new HashMap<>();
    private static final Map<String, Long> channelCooldown = new HashMap<>();
    private static Path dataFile;
    private static boolean dirty = false;
    private static int tickCounter = 0;
    private static boolean reentrant = false;

    private static final int SAVE_CYCLE_TICKS = 1200; // once a minute

    public static class DailyCounters {
        public long day = 0L;
        public long spiritPrestige = 0L;
        public long bossPrestige = 0L;
        public long dividend = 0L;
        public long withdrawn = 0L;
        public long lastSpiritPrestigeAt = 0L;

        public void rollIfNeeded(long today) {
            if (day != today) {
                day = today;
                spiritPrestige = 0L;
                bossPrestige = 0L;
                dividend = 0L;
                withdrawn = 0L;
            }
        }
    }

    private static class SaveFile {
        Map<String, SorcererClan> clans = new LinkedHashMap<>();
        Map<String, DailyCounters> daily = new HashMap<>();
    }

    // ════════════════════════════════════════════════════════════════════
    // LIFECYCLE
    // ════════════════════════════════════════════════════════════════════

    public static void initialize(Path worldRoot) {
        dataFile = worldRoot.resolve("clans.json");
        load();
    }

    private static void load() {
        clans.clear();
        daily.clear();
        if (dataFile == null || !Files.exists(dataFile)) return;
        try (Reader r = Files.newBufferedReader(dataFile)) {
            Gson g = new GsonBuilder().create();
            SaveFile sf = g.fromJson(r, SaveFile.class);
            if (sf != null) {
                if (sf.clans != null) {
                    for (Map.Entry<String, SorcererClan> e : sf.clans.entrySet()) {
                        SorcererClan c = e.getValue();
                        if (c != null && c.id != null) clans.put(c.id, c);
                    }
                }
                if (sf.daily != null) daily.putAll(sf.daily);
            }
        } catch (Exception e) {
            System.err.println("[ClanManager] Failed to load clans.json: " + e.getMessage());
        }
    }

    public static void save() {
        if (dataFile == null) return;
        try {
            Files.createDirectories(dataFile.getParent());
            SaveFile sf = new SaveFile();
            sf.clans.putAll(clans);
            sf.daily.putAll(daily);
            Gson g = new GsonBuilder().setPrettyPrinting().create();
            try (Writer w = Files.newBufferedWriter(dataFile)) {
                g.toJson(sf, w);
            }
            dirty = false;
        } catch (Exception e) {
            System.err.println("[ClanManager] Failed to save clans.json: " + e.getMessage());
        }
    }

    /** Alias used by {@link ClanWarBridge}. */
    public static void saveAll() {
        save();
    }

    public static void tick(MinecraftServer server) {
        if (server == null) return;
        if (++tickCounter < SAVE_CYCLE_TICKS) return;
        tickCounter = 0;
        if (dirty) save();
    }

    /** Per-player upkeep: the Ancestral Hall ward while a member stands in the clan hall. */
    public static void tickPlayer(ServerPlayer player) {
        if (player == null || player.tickCount % 40 != 0) return;
        String uuid = player.getUUID().toString();
        SorcererClan c = getClanOf(uuid);
        if (c == null || c.hallSettlementId == null) return;
        if (!ClanPerks.hasHallWard(uuid)) return;
        Settlement hall = SettlementManager.get(c.hallSettlementId);
        if (hall == null) return;
        if (!(player.level() instanceof ServerLevel sl)) return;
        Settlement here = SettlementManager.getContaining(
                sl.dimension().identifier().toString(), player.blockPosition());
        if (here == null || !here.id.equals(hall.id)) return;
        player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 60, 0, true, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, false, false));
    }

    // ════════════════════════════════════════════════════════════════════
    // EVENTS (register once at mod init, alongside SorcererManager.registerEvents)
    // ════════════════════════════════════════════════════════════════════

    public static void registerEvents() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            // Player-vs-player: score any running feud.
            if (entity instanceof ServerPlayer victim) {
                if (source.getEntity() instanceof ServerPlayer killer) {
                    ClanWarBridge.onPlayerKill(killer, victim);
                }
                return;
            }
            Entity killerEnt = source.getEntity();
            if (!(killerEnt instanceof ServerPlayer killer)) return;
            if (entity.entityTags().contains(SpecialGradeBossManager.BOSS_TAG)) {
                onSpecialGradeSlain(killer);
            } else if (CursedSpiritManager.isSpirit(entity)) {
                onSpiritSlain(killer);
            }
        });

        // CURSED_BANE spirit damage + feud PvP damage amplification.
        ServerLivingEntityEvents.AFTER_DAMAGE.register(ClanManager::onAfterDamage);
    }

    private static void onAfterDamage(LivingEntity target, DamageSource source,
                                      float baseDamage, float damageTaken, boolean blocked) {
        if (reentrant || blocked || damageTaken <= 0.0f) return;
        if (!(source.getEntity() instanceof ServerPlayer attacker) || attacker == target) return;
        if (source.getDirectEntity() != attacker) return; // melee only
        if (!(attacker.level() instanceof ServerLevel sl)) return;
        String uuid = attacker.getUUID().toString();

        float bonus = 0f;
        // Spirit bane.
        if (CursedSpiritManager.isSpirit(target)) {
            bonus += damageTaken * (float) ClanPerks.spiritDamageBonus(uuid);
        }
        // Feud amplification between opted-in clans.
        if (target instanceof ServerPlayer victim) {
            SorcererClan kc = getClanOf(uuid);
            SorcererClan vc = getClanOf(victim.getUUID().toString());
            if (kc != null && vc != null && !kc.id.equals(vc.id)
                    && ClanWarBridge.areFeuding(kc.id, vc.id)) {
                bonus += damageTaken * ClanWarBridge.FEUD_DAMAGE_BONUS;
            }
        }
        if (bonus <= 0.01f || !target.isAlive()) return;
        reentrant = true;
        try {
            target.invulnerableTime = 0;
            target.hurtServer(sl, attacker.damageSources().magic(), bonus);
        } finally {
            reentrant = false;
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // PRESTIGE
    // ════════════════════════════════════════════════════════════════════

    public static long totalPrestigeForLevel(int level) {
        if (level <= 1) return 0L;
        int n = Math.min(MAX_PRESTIGE_LEVEL, level) - 1;
        return 1000L * n * n + 2000L * n;
    }

    public static long prestigeToNextLevel(SorcererClan c) {
        if (c == null || c.prestigeLevel >= MAX_PRESTIGE_LEVEL) return 0L;
        return Math.max(0L, totalPrestigeForLevel(c.prestigeLevel + 1) - c.prestige);
    }

    public static double levelProgress(SorcererClan c) {
        if (c == null || c.prestigeLevel >= MAX_PRESTIGE_LEVEL) return 1.0;
        long base = totalPrestigeForLevel(c.prestigeLevel);
        long next = totalPrestigeForLevel(c.prestigeLevel + 1);
        if (next <= base) return 1.0;
        return Math.max(0.0, Math.min(1.0, (double) (c.prestige - base) / (double) (next - base)));
    }

    /** Adds prestige and resolves level-ups. Used by the capped entry points and by feuds. */
    public static void addPrestige(SorcererClan c, long amount, String reason) {
        if (c == null || amount <= 0) return;
        c.prestige += amount;
        int gained = 0;
        while (c.prestigeLevel < MAX_PRESTIGE_LEVEL
                && c.prestige >= totalPrestigeForLevel(c.prestigeLevel + 1)) {
            c.prestigeLevel++;
            gained++;
        }
        if (gained > 0) {
            logTo(c, "Rose to prestige level " + c.prestigeLevel + ".");
            broadcastClan(c, "§6✦ §f" + c.name + " §6rose to prestige level §f"
                    + c.prestigeLevel + "§6!");
            ClanPerks.Perk unlocked = perkAtLevel(c.prestigeLevel);
            if (unlocked != null) {
                broadcastClan(c, "§b✦ Clan perk unlocked: §f" + unlocked.display);
            }
        }
        dirty = true;
    }

    private static ClanPerks.Perk perkAtLevel(int level) {
        for (ClanPerks.Perk p : ClanPerks.Perk.values()) {
            if (p.requiredLevel == level) return p;
        }
        return null;
    }

    private static void onSpiritSlain(ServerPlayer killer) {
        String uuid = killer.getUUID().toString();
        SorcererClan c = getClanOf(uuid);
        if (c == null) return;
        DailyCounters dc = counters(uuid);
        long now = System.currentTimeMillis();
        if (now - dc.lastSpiritPrestigeAt < SPIRIT_MIN_INTERVAL_MS) return;
        if (dc.spiritPrestige >= DAILY_SPIRIT_PRESTIGE_CAP) return;
        dc.lastSpiritPrestigeAt = now;

        long award = Math.min(SPIRIT_PRESTIGE, DAILY_SPIRIT_PRESTIGE_CAP - dc.spiritPrestige);
        dc.spiritPrestige += award;
        addPrestige(c, award, "spirit slain");

        long eBonus = ClanPerks.spiritKillEnergyBonus(uuid);
        if (eBonus > 0) CursedEnergyManager.earn(killer, eBonus);

        long div = ClanPerks.treasuryDividendPerKill(uuid);
        if (div > 0 && dc.dividend < DAILY_DIVIDEND_CAP) {
            long give = Math.min(div, DAILY_DIVIDEND_CAP - dc.dividend);
            dc.dividend += give;
            c.treasury += give;
        }
        dirty = true;
    }

    private static void onSpecialGradeSlain(ServerPlayer killer) {
        String uuid = killer.getUUID().toString();
        SorcererClan c = getClanOf(uuid);
        if (c == null) return;
        DailyCounters dc = counters(uuid);
        if (dc.bossPrestige >= DAILY_BOSS_PRESTIGE_CAP) return;
        long award = Math.min(BOSS_PRESTIGE, DAILY_BOSS_PRESTIGE_CAP - dc.bossPrestige);
        dc.bossPrestige += award;
        addPrestige(c, award, "special grade felled");
        broadcastClan(c, "§d✦ §f" + killer.getName().getString()
                + " §7felled a Special Grade — §f+" + award + " §7clan prestige.");
        dirty = true;
        save();
    }

    private static long todayIndex() {
        return System.currentTimeMillis() / 86_400_000L;
    }

    private static DailyCounters counters(String uuid) {
        DailyCounters dc = daily.computeIfAbsent(uuid, k -> new DailyCounters());
        dc.rollIfNeeded(todayIndex());
        return dc;
    }

    public static long dailySpiritPrestige(String uuid) { return counters(uuid).spiritPrestige; }
    public static long dailyBossPrestige(String uuid) { return counters(uuid).bossPrestige; }

    // ════════════════════════════════════════════════════════════════════
    // ACCESSORS
    // ════════════════════════════════════════════════════════════════════

    public static Collection<SorcererClan> allClans() {
        return new ArrayList<>(clans.values());
    }

    public static SorcererClan get(String clanId) {
        return clanId == null ? null : clans.get(clanId);
    }

    public static SorcererClan getClanOf(String uuid) {
        if (uuid == null) return null;
        for (SorcererClan c : clans.values()) {
            if (c.isMember(uuid)) return c;
        }
        return null;
    }

    public static SorcererClan getClanOf(ServerPlayer player) {
        return player == null ? null : getClanOf(player.getUUID().toString());
    }

    public static SorcererClan byName(String name) {
        if (name == null) return null;
        for (SorcererClan c : clans.values()) {
            if (c.name != null && c.name.equalsIgnoreCase(name)) return c;
        }
        return null;
    }

    public static SorcererClan clanWithHall(String settlementId) {
        if (settlementId == null) return null;
        for (SorcererClan c : clans.values()) {
            if (settlementId.equals(c.hallSettlementId)) return c;
        }
        return null;
    }

    public static List<SorcererClan> pendingInvitesFor(String uuid) {
        List<SorcererClan> out = new ArrayList<>();
        if (uuid == null) return out;
        for (SorcererClan c : clans.values()) {
            if (c.inviteSet().contains(uuid) && !c.isMember(uuid)) out.add(c);
        }
        return out;
    }

    public static List<SorcererClan> leaderboard() {
        List<SorcererClan> out = new ArrayList<>(clans.values());
        out.sort((a, b) -> {
            if (a.prestigeLevel != b.prestigeLevel) return Integer.compare(b.prestigeLevel, a.prestigeLevel);
            return Long.compare(b.prestige, a.prestige);
        });
        return out;
    }

    // ════════════════════════════════════════════════════════════════════
    // LOG / BROADCAST
    // ════════════════════════════════════════════════════════════════════

    private static final SimpleDateFormat LOG_FMT = new SimpleDateFormat("dd/MM HH:mm", Locale.ROOT);

    public static void logTo(SorcererClan c, String text) {
        if (c == null || text == null) return;
        List<String> log = c.logList();
        log.add("§8[" + LOG_FMT.format(new Date()) + "] §7" + text);
        while (log.size() > SorcererClan.LOG_LIMIT) log.remove(0);
        dirty = true;
    }

    public static void broadcastClan(SorcererClan c, String message) {
        if (c == null || PoliticalServer.server == null) return;
        for (String uuid : c.memberMap().keySet()) {
            ServerPlayer p = playerFor(uuid);
            if (p != null) p.sendSystemMessage(Component.literal(message));
        }
    }

    private static ServerPlayer playerFor(String uuid) {
        if (PoliticalServer.server == null || uuid == null) return null;
        try {
            return PoliticalServer.server.getPlayerList().getPlayer(UUID.fromString(uuid));
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

    /**
     * Founds a clan. Costs {@link #FOUND_COST} coins and consumes one Clan Seal from the founder.
     */
    public static boolean foundClan(ServerPlayer player, String rawName) {
        if (player == null) return false;
        String uuid = player.getUUID().toString();
        if (getClanOf(uuid) != null) {
            msg(player, "§cYou already belong to a clan. Leave it first.");
            return false;
        }
        String name = sanitizeName(rawName);
        if (name.length() < 3) {
            msg(player, "§cClan names must be 3 to 24 letters, digits, spaces, ' or -.");
            return false;
        }
        if (byName(name) != null) {
            msg(player, "§cA clan named §f" + name + " §calready exists.");
            return false;
        }
        if (!ClanItems.hasSeal(player)) {
            msg(player, "§cYou need a §6Clan Seal §cto found a clan. §7Craft one first.");
            return false;
        }
        if (CoinManager.getCoinsLong(player) < FOUND_COST) {
            msg(player, "§cFounding a clan costs §e" + fmt(FOUND_COST) + " coins§c.");
            return false;
        }
        if (!CoinManager.removeCoins(player, FOUND_COST)) {
            msg(player, "§cPayment failed.");
            return false;
        }
        if (!ClanItems.consumeSeal(player)) {
            CoinManager.giveCoinsQuiet(player, FOUND_COST);
            msg(player, "§cCould not consume the Clan Seal — nothing was charged.");
            return false;
        }

        SorcererClan c = new SorcererClan(UUID.randomUUID().toString(), name, "✦", uuid);
        c.motto = "The bloodline endures.";
        clans.put(c.id, c);
        logTo(c, "Clan founded by " + player.getName().getString() + ".");
        dirty = true;
        save();

        msg(player, "§6✦ Clan founded: §e" + name);
        if (PoliticalServer.server != null) {
            for (ServerPlayer p : PoliticalServer.server.getPlayerList().getPlayers()) {
                p.sendSystemMessage(Component.literal("§6⚑ A new sorcerer clan has risen: §e" + name)
                        .withStyle(ChatFormatting.GOLD));
            }
        }
        return true;
    }

    public static boolean invite(ServerPlayer actor, ServerPlayer target) {
        if (actor == null || target == null) return false;
        SorcererClan c = getClanOf(actor);
        if (c == null) { msg(actor, "§cYou are not in a clan."); return false; }
        SorcererClan.Rank r = c.rankOf(actor.getUUID().toString());
        if (r == null || !r.isAtLeast(SorcererClan.Rank.ELDER)) {
            msg(actor, "§cOnly Elders and the Head may invite.");
            return false;
        }
        String tUuid = target.getUUID().toString();
        if (c.isMember(tUuid)) { msg(actor, "§cThey are already in your clan."); return false; }
        if (getClanOf(tUuid) != null) { msg(actor, "§cThey already belong to another clan."); return false; }
        if (c.memberCount() >= SorcererClan.MAX_MEMBERS) {
            msg(actor, "§cYour clan is full (" + SorcererClan.MAX_MEMBERS + ").");
            return false;
        }
        if (!c.inviteSet().add(tUuid)) {
            msg(actor, "§eThey already have a pending invite.");
            return false;
        }
        logTo(c, actor.getName().getString() + " invited " + target.getName().getString() + ".");
        dirty = true;
        save();
        msg(actor, "§a✓ Invited §f" + target.getName().getString() + "§a.");
        msg(target, "§6⚑ §e" + c.name + " §6has invited you into their clan. §7Open the Clans menu to accept.");
        return true;
    }

    public static boolean acceptInvite(ServerPlayer player, String clanId) {
        if (player == null) return false;
        String uuid = player.getUUID().toString();
        if (getClanOf(uuid) != null) { msg(player, "§cLeave your current clan first."); return false; }
        SorcererClan c = get(clanId);
        if (c == null || !c.inviteSet().contains(uuid)) {
            msg(player, "§cThat invitation is no longer valid.");
            return false;
        }
        if (c.memberCount() >= SorcererClan.MAX_MEMBERS) {
            msg(player, "§cThat clan is full.");
            return false;
        }
        c.inviteSet().remove(uuid);
        c.setRank(uuid, SorcererClan.Rank.INITIATE);
        logTo(c, player.getName().getString() + " joined as an Initiate.");
        dirty = true;
        save();
        msg(player, "§a✓ You joined §e" + c.name + "§a.");
        broadcastClan(c, "§a+ §f" + player.getName().getString() + " §7joined the clan.");
        return true;
    }

    public static boolean declineInvite(ServerPlayer player, String clanId) {
        SorcererClan c = get(clanId);
        if (player == null || c == null) return false;
        if (c.inviteSet().remove(player.getUUID().toString())) {
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
        SorcererClan c = getClanOf(uuid);
        if (c == null) { msg(player, "§cYou are not in a clan."); return false; }
        SorcererClan.Rank r = c.rankOf(uuid);
        if (r == SorcererClan.Rank.HEAD && c.memberCount() > 1) {
            msg(player, "§cPass the headship to another member before you leave.");
            return false;
        }
        c.memberMap().remove(uuid);
        if (c.memberCount() == 0) {
            clans.remove(c.id);
            ClanWarBridge.purgeClan(c.id);
            msg(player, "§7Your clan dissolved — you were the last member.");
        } else {
            logTo(c, player.getName().getString() + " left the clan.");
            broadcastClan(c, "§c- §f" + player.getName().getString() + " §7left the clan.");
            msg(player, "§7You left §e" + c.name + "§7.");
        }
        dirty = true;
        save();
        return true;
    }

    public static boolean kick(ServerPlayer actor, String targetUuid) {
        if (actor == null || targetUuid == null) return false;
        SorcererClan c = getClanOf(actor);
        if (c == null) return false;
        String aUuid = actor.getUUID().toString();
        SorcererClan.Rank ar = c.rankOf(aUuid);
        SorcererClan.Rank tr = c.rankOf(targetUuid);
        if (ar == null || tr == null) return false;
        if (aUuid.equals(targetUuid)) { msg(actor, "§cUse Leave instead."); return false; }
        if (!ar.isAtLeast(SorcererClan.Rank.ELDER)) { msg(actor, "§cOnly Elders and the Head may remove members."); return false; }
        if (tr.weight >= ar.weight) { msg(actor, "§cYou cannot remove someone of equal or higher rank."); return false; }
        c.memberMap().remove(targetUuid);
        String name = DataManager.getPlayerName(targetUuid);
        logTo(c, actor.getName().getString() + " removed " + name + ".");
        dirty = true;
        save();
        msg(actor, "§a✓ Removed §f" + name + "§a.");
        ServerPlayer target = playerFor(targetUuid);
        if (target != null) msg(target, "§cYou were removed from §e" + c.name + "§c.");
        return true;
    }

    public static boolean promote(ServerPlayer actor, String targetUuid) {
        SorcererClan c = getClanOf(actor);
        if (c == null || targetUuid == null) return false;
        String aUuid = actor.getUUID().toString();
        SorcererClan.Rank ar = c.rankOf(aUuid);
        SorcererClan.Rank tr = c.rankOf(targetUuid);
        if (ar == null || tr == null) return false;
        if (!ar.isAtLeast(SorcererClan.Rank.ELDER)) { msg(actor, "§cOnly Elders and the Head may promote."); return false; }
        SorcererClan.Rank next = tr.up();
        if (next == tr) { msg(actor, "§cThey are already at the highest promotable rank."); return false; }
        if (ar != SorcererClan.Rank.HEAD && next.weight >= ar.weight) {
            msg(actor, "§cOnly the Head may create Elders.");
            return false;
        }
        c.setRank(targetUuid, next);
        String name = DataManager.getPlayerName(targetUuid);
        logTo(c, name + " promoted to " + next.displayName + ".");
        dirty = true;
        save();
        msg(actor, "§a✓ §f" + name + " §ais now " + next.colored() + "§a.");
        return true;
    }

    public static boolean demote(ServerPlayer actor, String targetUuid) {
        SorcererClan c = getClanOf(actor);
        if (c == null || targetUuid == null) return false;
        String aUuid = actor.getUUID().toString();
        SorcererClan.Rank ar = c.rankOf(aUuid);
        SorcererClan.Rank tr = c.rankOf(targetUuid);
        if (ar == null || tr == null) return false;
        if (aUuid.equals(targetUuid)) { msg(actor, "§cYou cannot demote yourself."); return false; }
        if (!ar.isAtLeast(SorcererClan.Rank.ELDER)) { msg(actor, "§cOnly Elders and the Head may demote."); return false; }
        if (tr.weight >= ar.weight) { msg(actor, "§cYou cannot demote someone of equal or higher rank."); return false; }
        SorcererClan.Rank next = tr.down();
        if (next == tr) { msg(actor, "§cThey are already an Initiate."); return false; }
        c.setRank(targetUuid, next);
        String name = DataManager.getPlayerName(targetUuid);
        logTo(c, name + " demoted to " + next.displayName + ".");
        dirty = true;
        save();
        msg(actor, "§e" + name + " §7is now " + next.colored() + "§7.");
        return true;
    }

    /** Hands the Head rank to another member. The old Head becomes an Elder. */
    public static boolean transferHead(ServerPlayer actor, String targetUuid) {
        SorcererClan c = getClanOf(actor);
        if (c == null || targetUuid == null) return false;
        String aUuid = actor.getUUID().toString();
        if (c.rankOf(aUuid) != SorcererClan.Rank.HEAD) { msg(actor, "§cOnly the Head may do that."); return false; }
        if (!c.isMember(targetUuid)) return false;
        if (aUuid.equals(targetUuid)) return false;
        c.setRank(targetUuid, SorcererClan.Rank.HEAD);
        c.setRank(aUuid, SorcererClan.Rank.ELDER);
        String name = DataManager.getPlayerName(targetUuid);
        logTo(c, "The headship passed to " + name + ".");
        dirty = true;
        save();
        broadcastClan(c, "§6✦ §f" + name + " §6is now Clan Head.");
        return true;
    }

    // ════════════════════════════════════════════════════════════════════
    // TREASURY
    // ════════════════════════════════════════════════════════════════════

    public static boolean deposit(ServerPlayer player, long amount) {
        if (player == null || amount <= 0) return false;
        SorcererClan c = getClanOf(player);
        if (c == null) { msg(player, "§cYou are not in a clan."); return false; }
        if (CoinManager.getCoinsLong(player) < amount) {
            msg(player, "§cYou only have §e" + fmt(CoinManager.getCoinsLong(player)) + " coins§c.");
            return false;
        }
        if (!CoinManager.removeCoins(player, amount)) { msg(player, "§cPayment failed."); return false; }
        c.treasury += amount;
        logTo(c, player.getName().getString() + " deposited " + fmt(amount) + " coins.");
        dirty = true;
        save();
        msg(player, "§a✓ Deposited §e" + fmt(amount) + " coins §ato the clan treasury.");
        return true;
    }

    public static long withdrawalHeadroom(SorcererClan c, String uuid) {
        if (c == null || uuid == null) return 0L;
        SorcererClan.Rank r = c.rankOf(uuid);
        if (r == null || !r.isAtLeast(SorcererClan.Rank.ELDER)) return 0L;
        if (r == SorcererClan.Rank.HEAD) return c.treasury;
        DailyCounters dc = counters(uuid);
        return Math.max(0L, Math.min(c.treasury, ELDER_DAILY_WITHDRAW_CAP - dc.withdrawn));
    }

    public static boolean withdraw(ServerPlayer player, long amount) {
        if (player == null || amount <= 0) return false;
        SorcererClan c = getClanOf(player);
        if (c == null) return false;
        String uuid = player.getUUID().toString();
        SorcererClan.Rank r = c.rankOf(uuid);
        if (r == null || !r.isAtLeast(SorcererClan.Rank.ELDER)) {
            msg(player, "§cOnly Elders and the Head may withdraw.");
            return false;
        }
        if (amount > c.treasury) { msg(player, "§cThe treasury holds only §e" + fmt(c.treasury) + " coins§c."); return false; }
        long headroom = withdrawalHeadroom(c, uuid);
        if (amount > headroom) {
            msg(player, "§cDaily withdrawal limit reached. §7Remaining today: §e" + fmt(headroom) + " coins§7.");
            return false;
        }
        c.treasury -= amount;
        if (r != SorcererClan.Rank.HEAD) counters(uuid).withdrawn += amount;
        CoinManager.giveCoinsQuiet(player, amount);
        logTo(c, player.getName().getString() + " withdrew " + fmt(amount) + " coins.");
        dirty = true;
        save();
        msg(player, "§a✓ Withdrew §e" + fmt(amount) + " coins§a.");
        return true;
    }

    public static long dailyWithdrawn(String uuid) { return counters(uuid).withdrawn; }

    // ════════════════════════════════════════════════════════════════════
    // SIGNATURE TECHNIQUE
    // ════════════════════════════════════════════════════════════════════

    public static boolean setSignature(ServerPlayer actor, InheritedTechnique tech) {
        SorcererClan c = getClanOf(actor);
        if (c == null || tech == null) return false;
        if (c.rankOf(actor.getUUID().toString()) != SorcererClan.Rank.HEAD) {
            msg(actor, "§cOnly the Clan Head may set the signature technique.");
            return false;
        }
        c.signatureTechniqueId = tech.id;
        logTo(c, "Signature technique set to " + tech.display + ".");
        dirty = true;
        save();
        broadcastClan(c, "§d✦ Clan signature technique is now §f" + tech.display + "§d.");
        return true;
    }

    public static long channelCooldownRemaining(String uuid) {
        Long until = channelCooldown.get(uuid);
        return until == null ? 0L : Math.max(0L, until - System.currentTimeMillis());
    }

    /** Channel the clan's signature technique. Returns false with a message if it cannot fire. */
    public static boolean channelSignature(ServerPlayer player) {
        if (player == null) return false;
        String uuid = player.getUUID().toString();
        SorcererClan c = getClanOf(uuid);
        if (c == null) { msg(player, "§cYou are not in a clan."); return false; }
        if (!SorcererManager.isEnrolled(uuid)) {
            msg(player, "§cYou must be an enrolled sorcerer to channel a technique.");
            return false;
        }
        InheritedTechnique t = c.signature();
        SorcererRank rank = SorcererManager.rankOf(uuid);
        if (!rank.isAtLeast(t.minRank)) {
            msg(player, "§c" + t.display + " requires " + t.minRank.display + " sorcerer or above.");
            return false;
        }
        long remaining = channelCooldownRemaining(uuid);
        if (remaining > 0) {
            msg(player, "§c" + t.display + " is recharging — §f" + (remaining / 1000L + 1) + "s§c.");
            return false;
        }
        double discount = ClanPerks.techniqueDiscount(uuid);
        double cost = t.energyCost * discount;
        if (!SorcererManager.spendCe(player, cost)) {
            msg(player, "§cNot enough cursed energy (need §f" + (long) Math.ceil(cost) + "§c).");
            return false;
        }
        boolean ok = t.execute(player);
        if (!ok) {
            // Refund the paid energy and the mastery it counted; do not start the cooldown.
            SorcererManager.PlayerSorcerer ps = SorcererManager.getOrNull(uuid);
            if (ps != null) {
                ps.cursedEnergy = Math.min(SorcererManager.maxCe(ps), ps.cursedEnergy + cost);
                ps.ceSpent = Math.max(0L, ps.ceSpent - (long) Math.ceil(cost));
            }
            msg(player, "§7No cursed target within range.");
            return false;
        }
        long cd = (long) (t.cooldownMs * discount);
        channelCooldown.put(uuid, System.currentTimeMillis() + cd);
        return true;
    }

    // ════════════════════════════════════════════════════════════════════
    // HALL
    // ════════════════════════════════════════════════════════════════════

    public static boolean declareHall(ServerPlayer player, Settlement s) {
        if (player == null || s == null) return false;
        SorcererClan c = getClanOf(player);
        if (c == null) return false;
        if (c.rankOf(player.getUUID().toString()) != SorcererClan.Rank.HEAD) {
            msg(player, "§cOnly the Clan Head may declare a hall.");
            return false;
        }
        if (!s.tierEnum().isAtLeast(SettlementTier.TOWN)) {
            msg(player, "§cA clan hall must be declared at a §fTown §cor larger settlement.");
            return false;
        }
        SorcererClan other = clanWithHall(s.id);
        if (other != null && !other.id.equals(c.id)) {
            msg(player, "§c" + s.name + " is already the hall of §f" + other.name + "§c.");
            return false;
        }
        if (s.id.equals(c.hallSettlementId)) { msg(player, "§eThat is already your hall."); return false; }
        int resident = 0;
        for (String uuid : c.memberMap().keySet()) {
            if (s.registered().contains(uuid)) resident++;
        }
        if (resident < HALL_MIN_RESIDENT_MEMBERS) {
            msg(player, "§cYou need §f" + HALL_MIN_RESIDENT_MEMBERS + " §cclan members registered to §f"
                    + s.name + "§c (currently §f" + resident + "§c).");
            return false;
        }
        if (c.treasury < HALL_DECLARE_COST) {
            msg(player, "§cDeclaring a hall costs §e" + fmt(HALL_DECLARE_COST) + " coins §cfrom the treasury.");
            return false;
        }
        c.treasury -= HALL_DECLARE_COST;
        c.hallSettlementId = s.id;
        c.hallDeclaredAt = System.currentTimeMillis();
        logTo(c, "Hall declared at " + s.name + ".");
        dirty = true;
        save();
        broadcastClan(c, "§6⚑ Clan hall declared at §f" + s.name + "§6.");
        return true;
    }

    public static boolean abandonHall(ServerPlayer player) {
        SorcererClan c = getClanOf(player);
        if (c == null || player == null) return false;
        if (c.rankOf(player.getUUID().toString()) != SorcererClan.Rank.HEAD) {
            msg(player, "§cOnly the Clan Head may abandon the hall.");
            return false;
        }
        if (c.hallSettlementId == null) return false;
        Settlement s = SettlementManager.get(c.hallSettlementId);
        c.hallSettlementId = null;
        logTo(c, "Hall abandoned" + (s == null ? "." : " at " + s.name + "."));
        dirty = true;
        save();
        msg(player, "§7Clan hall abandoned.");
        return true;
    }

    // ════════════════════════════════════════════════════════════════════
    // SETTINGS / ADMIN
    // ════════════════════════════════════════════════════════════════════

    public static boolean setMotto(ServerPlayer player, String motto) {
        SorcererClan c = getClanOf(player);
        if (c == null || player == null) return false;
        SorcererClan.Rank r = c.rankOf(player.getUUID().toString());
        if (r == null || !r.isAtLeast(SorcererClan.Rank.ELDER)) {
            msg(player, "§cOnly Elders and the Head may set the motto.");
            return false;
        }
        String m = motto == null ? "" : motto.trim();
        if (m.length() > 48) m = m.substring(0, 48);
        c.motto = m;
        logTo(c, "Motto changed.");
        dirty = true;
        save();
        msg(player, "§a✓ Motto set.");
        return true;
    }

    public static boolean setCrest(ServerPlayer player, String crest) {
        SorcererClan c = getClanOf(player);
        if (c == null || player == null) return false;
        if (c.rankOf(player.getUUID().toString()) != SorcererClan.Rank.HEAD) {
            msg(player, "§cOnly the Clan Head may set the crest.");
            return false;
        }
        if (crest == null || crest.isBlank()) return false;
        c.crest = crest.trim().substring(0, 1);
        logTo(c, "Crest changed.");
        dirty = true;
        save();
        msg(player, "§a✓ Crest set to §f" + c.crest);
        return true;
    }

    public static boolean disband(ServerPlayer actor, SorcererClan c) {
        if (c == null || actor == null) return false;
        boolean isHead = c.rankOf(actor.getUUID().toString()) == SorcererClan.Rank.HEAD;
        if (!isHead && !SpawnProtectionManager.isOperator(actor)) {
            msg(actor, "§cOnly the Clan Head or an operator may disband a clan.");
            return false;
        }
        broadcastClan(c, "§c⚑ §f" + c.name + " §chas been disbanded. Its treasury is forfeit.");
        clans.remove(c.id);
        ClanWarBridge.purgeClan(c.id);
        dirty = true;
        save();
        return true;
    }

    // ── util ──

    public static String fmt(long v) {
        return String.format(Locale.ROOT, "%,d", v);
    }

    private static void msg(ServerPlayer p, String s) {
        if (p != null) p.sendSystemMessage(Component.literal(s));
    }
}
