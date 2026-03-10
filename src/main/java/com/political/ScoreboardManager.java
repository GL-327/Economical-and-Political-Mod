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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages a per-player Hypixel SkyBlock-style sidebar scoreboard.
 * Updated every 5 ticks for animation effects.
 */
public class ScoreboardManager {

    // Tick counter for throttling updates
    private static int tickCounter = 0;
    private static int displayTickCounter = 0;
    // Animation frame counter (increments each update cycle)
    private static int animFrame = 0;
    // How often (in update cycles) to re-send the display packet
    private static final int DISPLAY_REFRESH_INTERVAL = 20;

    // Per-player data: objective name → objective
    private static final Map<UUID, String> playerObjectiveNames = new ConcurrentHashMap<>();
    // Per-player data: line index → team name
    private static final Map<UUID, String[]> playerTeamNames = new ConcurrentHashMap<>();

    // Max sidebar lines (enough for the full scoreboard layout)
    private static final int MAX_LINES = 18;
    // Color codes used as invisible score holder names in the sidebar
    private static final String[] HOLDER_COLOR_CODES = {"0","1","2","3","4","5","6","7","8","9","a","b","c","d","e","f","r","k"};

    // ── Lifecycle ─────────────────────────────────────────────────

    public static void init(MinecraftServer server) {
        // Nothing to do on init; per-player setup happens on join
    }

    // Color codes for the animated title cycle
    private static final String[] TITLE_COLORS = {"§6", "§e", "§a", "§b", "§d", "§c", "§e", "§6"};
    // Separator animation frames
    private static final String[] SEPARATOR_FRAMES = {
        "§8────────────────",
        "§7─§8───────────────",
        "§8─§7─§8──────────────",
        "§8──§7─§8─────────────",
        "§8───§7─§8────────────",
        "§8────§7─§8───────────",
        "§8─────§7─§8──────────",
        "§8──────§7─§8─────────",
        "§8───────§7─§8────────",
        "§8────────§7─§8───────",
        "§8─────────§7─§8──────",
        "§8──────────§7─§8─────",
        "§8───────────§7─§8────",
        "§8────────────§7─§8───",
        "§8─────────────§7─§8──",
        "§8──────────────§7─§8─",
    };

    public static void tick(MinecraftServer server) {
        tickCounter++;
        if (tickCounter < 5) return;
        tickCounter = 0;
        animFrame++;
        displayTickCounter++;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            updateScoreboard(player);
            // Re-send display packet periodically to ensure scoreboard stays visible
            if (displayTickCounter % DISPLAY_REFRESH_INTERVAL == 0) {
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
            Text.literal("§6§lCivilCraft"),
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
            String holderName = "§" + c1 + "§" + c2;
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
        return "§" + c1 + "§" + c2;
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

        // Animated title color
        String titleColor = TITLE_COLORS[animFrame % TITLE_COLORS.length];
        lines.add(titleColor + "§l✦ CivilCraft ✦");

        // Animated separator
        String sep = SEPARATOR_FRAMES[animFrame % SEPARATOR_FRAMES.length];
        lines.add(sep);

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
        lines.add("§f  Chair: " + (chairName != null ? "§c§l" + chairName : "§cNone"));
        lines.add("§f  Vice: "  + (viceName  != null ? "§9§l" + viceName  : "§cNone"));

        // Bounty Gear Stats (conditional)
        SlayerManager.SlayerType gearType = detectBountyGearType(player);
        if (gearType != null) {
            lines.add("§8  "); // blank
            // Pulse effect: cycle between two shades for gear stats header
            String gearColor = (animFrame % 4 < 2) ? "§c" : "§4";
            lines.add(gearColor + " Bounty Gear Stats:");
            lines.add("§f  Type: " + gearType.color + gearType.displayName);

            // Show actual armor set bonus stats from the armor being worn
            int[] armorStats = detectArmorSetStats(player, gearType);
            int bossReductPct = armorStats[0];
            int allBossReductPct = armorStats[1];
            lines.add("§f  vs " + gearType.displayName + " Boss: §a-" + bossReductPct + "%");
            lines.add("§f  vs All Bosses: §a-" + allBossReductPct + "%");
        }

        // Bounty Quest (conditional)
        SlayerManager.ActiveQuest quest = SlayerManager.getActiveQuest(player);
        if (quest != null) {
            lines.add("§8   "); // blank
            // Pulse the quest header
            String questColor = (animFrame % 6 < 3) ? "§d" : "§5";
            lines.add(questColor + " Bounty Quest:");
            lines.add("§f  Target: §4" + quest.slayerType.bossName);
            int required = quest.getKillsRequired();
            // Animated kill counter bar
            int progress = required > 0 ? (quest.killCount * 5 / required) : 5;
            StringBuilder bar = new StringBuilder("§a");
            for (int i = 0; i < 5; i++) {
                bar.append(i < progress ? "█" : "§8█");
            }
            lines.add("§f  " + bar + " §e" + quest.killCount + "/" + required);
        }

        // Footer separator with animation offset
        String footSep = SEPARATOR_FRAMES[(animFrame + SEPARATOR_FRAMES.length / 2) % SEPARATOR_FRAMES.length];
        lines.add(footSep);
        lines.add("§eplay.civilcraft.net");

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
     * Checks main-hand weapon first, then all armor slots for any T1 or T2 bounty armor.
     */
    private static SlayerManager.SlayerType detectBountyGearType(ServerPlayerEntity player) {
        // Check main hand weapon
        ItemStack mainHand = player.getMainHandStack();
        if (!mainHand.isEmpty()) {
            SlayerManager.SlayerType swordType = SlayerItems.getSwordSlayerType(mainHand);
            if (swordType != null) return swordType;
        }

        // Check armor slots for any bounty armor (T1 or T2 for any slayer type)
        net.minecraft.item.ItemStack[] armorSlots = {
            player.getEquippedStack(net.minecraft.entity.EquipmentSlot.HEAD),
            player.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST),
            player.getEquippedStack(net.minecraft.entity.EquipmentSlot.LEGS),
            player.getEquippedStack(net.minecraft.entity.EquipmentSlot.FEET)
        };
        for (net.minecraft.item.ItemStack armor : armorSlots) {
            if (armor.isEmpty()) continue;
            // Check custom_item_id for T1/T2 bounty armor
            String customId = SlayerItems.getCustomItemId(armor);
            if (customId != null) {
                for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
                    String prefix = type.name().toLowerCase();
                    if (customId.startsWith(prefix + "_t1_") || customId.startsWith(prefix + "_t2_")) {
                        return type;
                    }
                }
            }
            // Also check special pieces
            if (SlayerItems.isZombieBerserkerHelmet(armor))  return SlayerManager.SlayerType.ZOMBIE;
            if (SlayerItems.isSpiderLeggings(armor))          return SlayerManager.SlayerType.SPIDER;
            if (SlayerItems.isSkeletonBow(armor))             return SlayerManager.SlayerType.SKELETON;
            if (SlayerItems.isSlimeBoots(armor))              return SlayerManager.SlayerType.SLIME;
            if (SlayerItems.isWardenChestplate(armor))        return SlayerManager.SlayerType.WARDEN;
        }
        return null;
    }

    /**
     * Detects the armor set stats (boss reduction %) from the bounty armor the player is wearing.
     * Returns int[]{bossReductionPct, allBossReductionPct}.
     */
    private static int[] detectArmorSetStats(ServerPlayerEntity player, SlayerManager.SlayerType type) {
        int bossReductPct = 0;
        int allBossReductPct = 0;
        int tier = 1;

        net.minecraft.item.ItemStack[] armorSlots = {
            player.getEquippedStack(net.minecraft.entity.EquipmentSlot.HEAD),
            player.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST),
            player.getEquippedStack(net.minecraft.entity.EquipmentSlot.LEGS),
            player.getEquippedStack(net.minecraft.entity.EquipmentSlot.FEET)
        };
        for (net.minecraft.item.ItemStack armor : armorSlots) {
            if (armor.isEmpty()) continue;
            String customId = SlayerItems.getCustomItemId(armor);
            if (customId != null) {
                String prefix = type.name().toLowerCase();
                if (customId.startsWith(prefix + "_t2_")) {
                    tier = 2;
                    break;
                } else if (customId.startsWith(prefix + "_t1_")) {
                    tier = 1;
                }
            }
        }

        SlayerItems.ArmorStats stats = SlayerItems.getArmorStats(type, tier);
        bossReductPct = (int) (stats.bossReduction * 100);
        allBossReductPct = (int) (stats.allBossReduction * 100);
        return new int[]{bossReductPct, allBossReductPct};
    }
}
