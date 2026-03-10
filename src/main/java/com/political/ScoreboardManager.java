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
        lines.add("§b Credits: §f" + credits);
        lines.add("§8 ");

        // Government
        String chairUuid = DataManager.getChair();
        String viceUuid  = DataManager.getViceChair();
        String chairName = chairUuid != null ? DataManager.getPlayerName(chairUuid) : null;
        String viceName  = viceUuid  != null ? DataManager.getPlayerName(viceUuid)  : null;
        lines.add("§a§l ⚖ §aGovernment");
        lines.add("§7  Chair: " + (chairName != null ? "§c" + chairName : "§8None"));
        lines.add("§7  Vice:  " + (viceName  != null ? "§9" + viceName  : "§8None"));
        lines.add("§8  ");

        // Underground auction timer
        boolean auctionActive = UndergroundAuctionManager.isAuctionActive();
        if (auctionActive) {
            String auctionPulse = (animFrame % 4 < 2) ? "§6§l" : "§e§l";
            lines.add(auctionPulse + " 🏪 " + "§r§a§lAuction: LIVE");
        } else {
            long msLeft = UndergroundAuctionManager.getTimeUntilNextAuction();
            lines.add("§6 🏪 §7Auction: §f" + formatDuration(msLeft));
        }

        // Bounty Gear Stats – display each active type separately
        // stats[type] = int[3]: [weaponDmgPct, specificDefPct, universalDefPct]
        Map<SlayerManager.SlayerType, int[]> bountyStats = collectBountyGearStats(player);
        SlayerManager.ActiveQuest quest = SlayerManager.getActiveQuest(player);
        boolean hasQuest = quest != null;

        if (!bountyStats.isEmpty()) {
            lines.add("§8   ");
            String gearColor = (animFrame % 4 < 2) ? "§c" : "§4";
            lines.add(gearColor + "§l ⚔ §r§7Bounty Gear");

            int totalUniversalDef = 0;
            for (Map.Entry<SlayerManager.SlayerType, int[]> entry : bountyStats.entrySet()) {
                SlayerManager.SlayerType type = entry.getKey();
                int[] s = entry.getValue();
                totalUniversalDef += s[2];
                if (s[0] > 0) {
                    lines.add("§7  " + type.displayName + " DMG: §c+" + s[0] + "%");
                }
                if (s[1] > 0) {
                    lines.add("§7  " + type.displayName + " DEF: §a-" + s[1] + "%");
                }
            }
            if (totalUniversalDef > 0) {
                lines.add("§7  Bounty Defence: §a-" + totalUniversalDef + "%");
            }
        }

        // Bounty Quest (conditional)
        if (hasQuest) {
            lines.add("§8    ");
            String questColor = (animFrame % 6 < 3) ? "§d" : "§5";
            lines.add(questColor + "§l ⚔ §r§dBounty Quest");
            lines.add("§7  Target: §c" + quest.slayerType.bossName);
            int required = quest.getKillsRequired();
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

    /** Formats a duration in milliseconds to a human-readable string like "2h 30m" or "45s". */
    private static String formatDuration(long ms) {
        if (ms <= 0) return "Now";
        long totalSecs = ms / 1000;
        long hours = totalSecs / 3600;
        long minutes = (totalSecs % 3600) / 60;
        long seconds = totalSecs % 60;
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        } else if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        } else {
            return seconds + "s";
        }
    }

    /**
     * Scans the player's main hand and all armour slots for bounty gear.
     * Returns a LinkedHashMap keyed by SlayerType, each entry being int[3]:
     *   [0] = weapon damage % bonus (e.g. 200 for T1 sword)
     *   [1] = boss-specific defence % (e.g. 50 for T1 set)
     *   [2] = universal (all-boss) defence % from this type's pieces
     */
    private static Map<SlayerManager.SlayerType, int[]> collectBountyGearStats(ServerPlayerEntity player) {
        Map<SlayerManager.SlayerType, int[]> stats = new LinkedHashMap<>();

        // ── Weapon in main hand ──────────────────────────────────
        ItemStack weapon = player.getMainHandStack();
        if (!weapon.isEmpty()) {
            if (SlayerItems.isSkeletonBow(weapon)) {
                stats.computeIfAbsent(SlayerManager.SlayerType.SKELETON, k -> new int[3])[0] = 500;
            } else if (SlayerItems.isUpgradedSlayerSword(weapon)) {
                SlayerManager.SlayerType t = SlayerItems.getSwordSlayerType(weapon);
                if (t != null) stats.computeIfAbsent(t, k -> new int[3])[0] = 300;
            } else if (SlayerItems.isSlayerSword(weapon)) {
                SlayerManager.SlayerType t = SlayerItems.getSwordSlayerType(weapon);
                if (t != null) stats.computeIfAbsent(t, k -> new int[3])[0] = 200;
            }
        }

        // ── Armour slots ─────────────────────────────────────────
        ItemStack[] armorSlots = {
            player.getEquippedStack(EquipmentSlot.HEAD),
            player.getEquippedStack(EquipmentSlot.CHEST),
            player.getEquippedStack(EquipmentSlot.LEGS),
            player.getEquippedStack(EquipmentSlot.FEET)
        };

        // Track the highest tier seen per type for the standard set bonus
        Map<SlayerManager.SlayerType, Integer> highestTier = new LinkedHashMap<>();

        for (ItemStack armor : armorSlots) {
            if (armor.isEmpty()) continue;

            // Special pieces
            if (SlayerItems.isZombieBerserkerHelmet(armor)) {
                int[] s = stats.computeIfAbsent(SlayerManager.SlayerType.ZOMBIE, k -> new int[3]);
                s[1] = Math.max(s[1], (int)(SlayerItems.BERSERKER_HELMET_BOSS_REDUCTION * 100));
                s[2] += (int)(SlayerItems.BERSERKER_HELMET_ALL_REDUCTION * 100);
                continue;
            }
            if (SlayerItems.isSpiderLeggings(armor)) {
                int[] s = stats.computeIfAbsent(SlayerManager.SlayerType.SPIDER, k -> new int[3]);
                s[1] = Math.max(s[1], (int)(SlayerItems.SPIDER_LEGS_BOSS_REDUCTION * 100));
                s[2] += (int)(SlayerItems.SPIDER_LEGS_ALL_REDUCTION * 100);
                continue;
            }
            if (SlayerItems.isSlimeBoots(armor)) {
                int[] s = stats.computeIfAbsent(SlayerManager.SlayerType.SLIME, k -> new int[3]);
                s[1] = Math.max(s[1], (int)(SlayerItems.SLIME_BOOTS_BOSS_REDUCTION * 100));
                s[2] += (int)(SlayerItems.SLIME_BOOTS_ALL_REDUCTION * 100);
                continue;
            }
            if (SlayerItems.isWardenChestplate(armor)) {
                int[] s = stats.computeIfAbsent(SlayerManager.SlayerType.WARDEN, k -> new int[3]);
                s[1] = Math.max(s[1], (int)(SlayerItems.WARDEN_CHEST_BOSS_REDUCTION * 100));
                s[2] += (int)(SlayerItems.WARDEN_CHEST_ALL_REDUCTION * 100);
                continue;
            }

            // Standard T1/T2 set pieces (identified via custom_item_id prefix)
            String customId = SlayerItems.getCustomItemId(armor);
            if (customId == null) continue;
            for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
                String pfx = type.name().toLowerCase();
                int tier = 0;
                if (customId.startsWith(pfx + "_t2_")) tier = 2;
                else if (customId.startsWith(pfx + "_t1_")) tier = 1;
                if (tier > 0) {
                    int prev = highestTier.getOrDefault(type, 0);
                    if (tier > prev) highestTier.put(type, tier);
                    // Ensure the type is in the stats map
                    stats.computeIfAbsent(type, k -> new int[3]);
                    break;
                }
            }
        }

        // Apply set bonus for each type using the highest tier detected
        for (Map.Entry<SlayerManager.SlayerType, Integer> entry : highestTier.entrySet()) {
            SlayerManager.SlayerType type = entry.getKey();
            int tier = entry.getValue();
            SlayerItems.ArmorStats armorStats = SlayerItems.getArmorStats(type, tier);
            int[] s = stats.computeIfAbsent(type, k -> new int[3]);
            s[1] = Math.max(s[1], (int)(armorStats.bossReduction * 100));
            s[2] += (int)(armorStats.allBossReduction * 100);
        }

        // Remove entries that have no meaningful stats to display
        stats.entrySet().removeIf(e -> e.getValue()[0] == 0 && e.getValue()[1] == 0 && e.getValue()[2] == 0);
        return stats;
    }
}
