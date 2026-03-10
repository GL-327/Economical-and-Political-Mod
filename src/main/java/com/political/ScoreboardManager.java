package com.political;

import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket;
import net.minecraft.scoreboard.*;
import net.minecraft.scoreboard.number.BlankNumberFormat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.EquipmentSlot;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages a per-player Hypixel SkyBlock-style sidebar scoreboard.
 * Updated every 20 ticks (1 second).
 */
public class ScoreboardManager {

    // Tick counter for throttling updates
    private static int tickCounter = 0;
    private static int displayTickCounter = 0;
    // How often (in update cycles = seconds) to re-send the display packet
    private static final int DISPLAY_REFRESH_INTERVAL_SECONDS = 5;

    // Per-player data: objective name → objective
    private static final Map<UUID, String> playerObjectiveNames = new ConcurrentHashMap<>();
    // Per-player data: line index → team name
    private static final Map<UUID, String[]> playerTeamNames = new ConcurrentHashMap<>();

    // Max sidebar lines (enough for the full scoreboard layout)
    private static final int MAX_LINES = 18;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("M/d/yyyy");
    // Color codes used as invisible score holder names in the sidebar
    private static final String[] HOLDER_COLOR_CODES = {"0","1","2","3","4","5","6","7","8","9","a","b","c","d","e","f","r","k"};

    // ── Lifecycle ─────────────────────────────────────────────────

    public static void init(MinecraftServer server) {
        // Nothing to do on init; per-player setup happens on join
    }

    public static void tick(MinecraftServer server) {
        tickCounter++;
        if (tickCounter < 20) return;
        tickCounter = 0;
        displayTickCounter++;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            updateScoreboard(player);
            // Re-send display packet periodically to ensure scoreboard stays visible
            if (displayTickCounter % DISPLAY_REFRESH_INTERVAL_SECONDS == 0) {
                sendDisplayPacket(player);
            }
        }
    }

    public static void onPlayerJoin(ServerPlayerEntity player) {
        setupPlayerScoreboard(player);
        updateScoreboard(player);
        sendDisplayPacket(player);
    }

    public static void onPlayerDisconnect(ServerPlayerEntity player) {
        cleanupPlayerScoreboard(player);
    }

    // ── Setup / Cleanup ──────────────────────────────────────────

    private static void setupPlayerScoreboard(ServerPlayerEntity player) {
        ServerScoreboard scoreboard = PoliticalServer.server.getScoreboard();
        String prefix = getPlayerPrefix(player);

        // Remove old objective if it exists
        String objName = "sb_" + prefix;
        ScoreboardObjective old = scoreboard.getNullableObjective(objName);
        if (old != null) scoreboard.removeObjective(old);

        // Create new objective (use BlankNumberFormat to hide score integers)
        scoreboard.addObjective(
            objName,
            ScoreboardCriterion.DUMMY,
            Text.literal("§6§lPOLITICAL SERVER"),
            ScoreboardCriterion.RenderType.INTEGER,
            false,
            BlankNumberFormat.INSTANCE
        );
        playerObjectiveNames.put(player.getUuid(), objName);

        // Register as the sidebar display on the server scoreboard so clients see it
        ScoreboardObjective objective = scoreboard.getNullableObjective(objName);
        if (objective != null) {
            scoreboard.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, objective);
        }

        // Create per-player teams (one per line)
        String[] teamNames = new String[MAX_LINES];
        for (int i = 0; i < MAX_LINES; i++) {
            String teamName = "sbt" + prefix + String.format("%02d", i);
            // Remove old team if exists
            Team existing = scoreboard.getTeam(teamName);
            if (existing != null) scoreboard.removeTeam(existing);
            Team team = scoreboard.addTeam(teamName);
            team.setPrefix(Text.literal(""));
            // Hide score number display
            team.setShowFriendlyInvisibles(false);
            teamNames[i] = teamName;
            // Holder name: use color codes so they're invisible in sidebar
            // Format: §<code1>§<code2> - unique per line, renders empty
            String c1 = HOLDER_COLOR_CODES[i % HOLDER_COLOR_CODES.length];
            String c2 = HOLDER_COLOR_CODES[(i / HOLDER_COLOR_CODES.length) % HOLDER_COLOR_CODES.length];
            String holderName = "§" + c1 + "§" + c2 + prefix;
            scoreboard.addScoreHolderToTeam(holderName, team);
        }
        playerTeamNames.put(player.getUuid(), teamNames);
    }

    private static void cleanupPlayerScoreboard(ServerPlayerEntity player) {
        ServerScoreboard scoreboard = PoliticalServer.server.getScoreboard();

        // Remove teams
        String[] teamNames = playerTeamNames.remove(player.getUuid());
        if (teamNames != null) {
            for (String teamName : teamNames) {
                if (teamName == null) continue;
                Team team = scoreboard.getTeam(teamName);
                if (team != null) scoreboard.removeTeam(team);
            }
        }

        // Remove objective
        String objName = playerObjectiveNames.remove(player.getUuid());
        if (objName != null) {
            ScoreboardObjective obj = scoreboard.getNullableObjective(objName);
            if (obj != null) scoreboard.removeObjective(obj);
        }
    }

    private static void sendDisplayPacket(ServerPlayerEntity player) {
        String objName = playerObjectiveNames.get(player.getUuid());
        if (objName == null) return;
        ServerScoreboard scoreboard = PoliticalServer.server.getScoreboard();
        ScoreboardObjective obj = scoreboard.getNullableObjective(objName);
        if (obj == null) return;
        player.networkHandler.sendPacket(new ScoreboardDisplayS2CPacket(ScoreboardDisplaySlot.SIDEBAR, obj));
    }

    // ── Update ────────────────────────────────────────────────────

    private static String getHolderName(String prefix, int i) {
        String c1 = HOLDER_COLOR_CODES[i % HOLDER_COLOR_CODES.length];
        String c2 = HOLDER_COLOR_CODES[(i / HOLDER_COLOR_CODES.length) % HOLDER_COLOR_CODES.length];
        return "§" + c1 + "§" + c2 + prefix;
    }

    private static void updateScoreboard(ServerPlayerEntity player) {
        String objName = playerObjectiveNames.get(player.getUuid());
        String[] teamNames = playerTeamNames.get(player.getUuid());
        if (objName == null || teamNames == null) return;

        ServerScoreboard scoreboard = PoliticalServer.server.getScoreboard();
        ScoreboardObjective objective = scoreboard.getNullableObjective(objName);
        if (objective == null) return;

        List<String> lines = buildLines(player);

        // Pad or trim lines list to MAX_LINES
        while (lines.size() < MAX_LINES) lines.add(null); // null = hidden line

        // Update each line (from bottom = score 0, to top = score MAX_LINES-1)
        int score = lines.size();
        String prefix = getPlayerPrefix(player);

        for (int i = 0; i < Math.min(lines.size(), MAX_LINES); i++) {
            String lineText = lines.get(i);
            String teamName = teamNames[i];
            String holderName = getHolderName(prefix, i);

            if (teamName == null) continue;
            Team team = scoreboard.getTeam(teamName);
            if (team == null) continue;

            if (lineText == null) {
                // Hide line: reset its score
                scoreboard.removeScore(ScoreHolder.fromName(holderName), objective);
                team.setPrefix(Text.literal(""));
            } else {
                // Set the prefix (the actual displayed text) and assign a score
                team.setPrefix(Text.literal(lineText));
                ScoreAccess access = scoreboard.getOrCreateScore(ScoreHolder.fromName(holderName), objective);
                access.setScore(score - i);
            }
        }
    }

    // ── Line builder ─────────────────────────────────────────────

    private static List<String> buildLines(ServerPlayerEntity player) {
        List<String> lines = new ArrayList<>();

        // Title / Date
        String date = LocalDate.now().format(DATE_FMT);
        lines.add("§7 " + date);
        lines.add("§8────────────────");

        // Economy
        int coins = CoinManager.getCoins(player);
        int credits = CreditItem.countCredits(player);
        lines.add("§e Coins: §f" + formatNumber(coins));
        lines.add("§e Credits: §f" + credits);
        lines.add("§8 "); // blank

        // Government
        lines.add("§a Government:");
        String chairUuid = DataManager.getChair();
        String viceUuid  = DataManager.getViceChair();
        String chairName = chairUuid != null ? DataManager.getPlayerName(chairUuid) : null;
        String viceName  = viceUuid  != null ? DataManager.getPlayerName(viceUuid)  : null;
        lines.add("§f  Chair: " + (chairName != null ? "§a" + chairName : "§cNone"));
        lines.add("§f  Vice: "  + (viceName  != null ? "§b" + viceName  : "§cNone"));

        // Bounty Gear Stats (conditional)
        SlayerManager.SlayerType gearType = detectBountyGearType(player);
        if (gearType != null) {
            lines.add("§8  "); // blank
            lines.add("§c Bounty Gear Stats:");
            lines.add("§f  Type: §a" + gearType.displayName);
            double dmgMulti = SlayerManager.getLevelDamageMultiplier(player, gearType);
            double dmgReduction = SlayerManager.getLevelDamageReduction(player, gearType);
            int dmgBonusPct  = (int) Math.round((dmgMulti - 1.0) * 100);
            int defPct       = (int) Math.round(dmgReduction * 100);
            lines.add("§f  Dmg Bonus: §c+" + dmgBonusPct + "%");
            lines.add("§f  Defense: §b-" + defPct + "%");
        }

        // Bounty Quest (conditional)
        SlayerManager.ActiveQuest quest = SlayerManager.getActiveQuest(player);
        if (quest != null) {
            lines.add("§8   "); // blank
            lines.add("§d Bounty Quest:");
            lines.add("§f  Target: §4" + quest.slayerType.bossName);
            int required = quest.getKillsRequired();
            lines.add("§f  Kills: §e" + quest.killCount + "/" + required);
        }

        // Footer
        lines.add("§8────────────────");
        lines.add("§ewww.example.com");

        return lines;
    }

    // ── Helpers ──────────────────────────────────────────────────

    /** Returns an 8-char prefix unique per player (first 8 chars of UUID string, no dashes). */
    private static String getPlayerPrefix(ServerPlayerEntity player) {
        return player.getUuid().toString().replace("-", "").substring(0, 8);
    }

    private static String formatNumber(int n) {
        return NumberFormat.getInstance().format(n);
    }

    /**
     * Detects the bounty gear type the player is wearing/holding.
     * Prefers main-hand weapon type, then armor.
     */
    private static SlayerManager.SlayerType detectBountyGearType(ServerPlayerEntity player) {
        // Check main hand weapon
        ItemStack mainHand = player.getMainHandStack();
        if (!mainHand.isEmpty()) {
            SlayerManager.SlayerType swordType = SlayerItems.getSwordSlayerType(mainHand);
            if (swordType != null) return swordType;
        }

        // Check armor pieces for bounty-related names
        net.minecraft.item.ItemStack[] armorSlots = {
            player.getEquippedStack(net.minecraft.entity.EquipmentSlot.HEAD),
            player.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST),
            player.getEquippedStack(net.minecraft.entity.EquipmentSlot.LEGS),
            player.getEquippedStack(net.minecraft.entity.EquipmentSlot.FEET)
        };
        for (net.minecraft.item.ItemStack armor : armorSlots) {
            if (armor.isEmpty()) continue;
            if (SlayerItems.isZombieBerserkerHelmet(armor))  return SlayerManager.SlayerType.ZOMBIE;
            if (SlayerItems.isSpiderLeggings(armor))          return SlayerManager.SlayerType.SPIDER;
            if (SlayerItems.isSkeletonBow(armor))             return SlayerManager.SlayerType.SKELETON;
            if (SlayerItems.isSlimeBoots(armor))              return SlayerManager.SlayerType.SLIME;
            if (SlayerItems.isWardenChestplate(armor))        return SlayerManager.SlayerType.WARDEN;
        }
        return null;
    }
}
