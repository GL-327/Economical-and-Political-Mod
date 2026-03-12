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

// SlayerData is in the same package

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

    // Allow up to 30 lines; team-based prefix/suffix display bypasses the vanilla 15-entry restriction
    private static final int MAX_LINES = 30;
    // Color codes used as invisible score holder names in the sidebar
    private static final String[] HOLDER_COLOR_CODES = {"0","1","2","3","4","5","6","7","8","9","a","b","c","d","e","f","r","k"};

    // ── Lifecycle ─────────────────────────────────────────────────

    public static void init(MinecraftServer server) {
        // Nothing to do on init; per-player setup happens on join
    }

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
        if (tickCounter < 20) return;
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

        // Line 1: Animated separator
        String sep = SEPARATOR_FRAMES[animFrame % SEPARATOR_FRAMES.length];
        lines.add(sep);

        // Lines 2-3: Economy (combined credits/coins to save space)
        int coins = CoinManager.getCoins(player);
        int credits = CreditItem.countCredits(player);
        lines.add("§e Coins: §f" + formatNumber(coins));
        lines.add("§b Credits: §f" + credits);
        lines.add("§8 ");

        // Lines 5-7: Government
        String chairUuid = DataManager.getChair();
        String viceUuid  = DataManager.getViceChair();
        String chairName = chairUuid != null ? DataManager.getPlayerName(chairUuid) : null;
        String viceName  = viceUuid  != null ? DataManager.getPlayerName(viceUuid)  : null;
        lines.add("§a§l ⚖ §r§7Chair: " + (chairName != null ? "§c" + chairName : "§8None"));
        lines.add("§7  Vice: " + (viceName  != null ? "§9" + viceName  : "§8None"));
        lines.add("§8  ");

        // Line 8: Underground Auction timer
        if (DataManager.isScoreboardShowAuction()) {
            boolean auctionActive = UndergroundAuctionManager.isAuctionActive();
            if (auctionActive) {
                String auctionPulse = (animFrame % 4 < 2) ? "§6§l" : "§e§l";
                lines.add(auctionPulse + " 🏪 §r§aAuction: §l§aLIVE");
            } else {
                long msLeft = UndergroundAuctionManager.getTimeUntilNextAuction();
                lines.add("§6 🏪 §7Auction: §f" + formatDuration(msLeft));
            }
        }

        // Line 9: Bounty level
        int totalLevel = 0;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            totalLevel += SlayerData.getSlayerLevel(player.getUuidAsString(), type);
        }
        String levelColor = totalLevel >= 30 ? "§d" : totalLevel >= 15 ? "§b" : "§7";
        lines.add("§e ⭐ §7Bounty: " + levelColor + totalLevel);
        lines.add("§8   "); // spacer after Bounty line

        // Lines 10+: Compact bounty stats / quest info
        if (DataManager.isScoreboardShowBountyGear()) {
        Map<SlayerManager.SlayerType, int[]> bountyStats = collectBountyGearStats(player);
        SlayerManager.ActiveQuest quest = SlayerManager.getActiveQuest(player);

        // Crown of Greed indicator
        boolean hasCrown = SlayerItems.isCrownOfGreed(player.getEquippedStack(EquipmentSlot.HEAD));

        if (quest != null) {
            // Bounty quest takes priority over gear stats when active
            String questColor = (animFrame % 6 < 3) ? "§d" : "§5";
            lines.add(questColor + "§l ⚔ §r§7Quest: §c" + quest.slayerType.bossName);
            int required = quest.getKillsRequired();
            int progress = required > 0 ? (quest.killCount * 5 / Math.max(1, required)) : 5;
            StringBuilder bar = new StringBuilder("§f  ");
            for (int i = 0; i < 5; i++) bar.append(i < progress ? "§a█" : "§8█");
            lines.add(bar + " §e" + quest.killCount + "/" + required);
            lines.add("§7  T" + quest.tier + " " + quest.slayerType.displayName + (quest.bossSpawned ? " §cBoss Active!" : ""));
        } else if (hasCrown) {
            lines.add("§6§l 👑 §r§7Crown of Greed");
            lines.add("§c  ✗ All defence DISABLED");
        } else if (!bountyStats.isEmpty()) {
            // Show compact bounty stats
            String gearColor = (animFrame % 4 < 2) ? "§c" : "§4";
            lines.add(gearColor + "§l ⚔ §r§7Bounty Gear");

            // Aggregate stats across all gear types
            int universalDef = 0;
            int weaponBonus = 0;
            for (int[] s : bountyStats.values()) {
                universalDef += s[2];
                if (s[0] > weaponBonus) weaponBonus = s[0];
            }
            int cappedUniversal = Math.min(universalDef, 95);
            // Universal defence line
            if (universalDef > 0) {
                lines.add("§7  DEF all: §a-" + cappedUniversal + "%");
            }
            // Boss-specific defence lines — show combined (specific + universal) capped at 95
            for (Map.Entry<SlayerManager.SlayerType, int[]> entry : bountyStats.entrySet()) {
                int[] s = entry.getValue();
                int combined = Math.min(95, s[1] + s[2]);
                if (combined > 0) {
                    lines.add("§7  vs §f" + entry.getKey().displayName + "§7: §a-" + combined + "% §7(vs boss)");
                }
            }
            // Weapon bonus
            if (weaponBonus > 0) {
                lines.add("§7  DMG: §c+" + (weaponBonus - 100) + "% to boss");
            }
            // Individual piece buffs — condensed to avoid cutoff
            List<String> pieceBuffs = collectActivePieceBuffs(player);
            for (String buffLine : pieceBuffs) {
                lines.add(buffLine);
            }
        } else {
            lines.add("§8  No bounty gear");
        }
        }

        // Lines 14-15: Footer
        String footSep = SEPARATOR_FRAMES[(animFrame + SEPARATOR_FRAMES.length / 2) % SEPARATOR_FRAMES.length];
        List<String> footer = new ArrayList<>();
        footer.add(footSep);
        footer.add("§eplay.civilcraft.net");

        // Trim content to fit MAX_LINES, always preserving the footer
        int maxContent = MAX_LINES - footer.size();
        while (lines.size() > maxContent) {
            lines.remove(lines.size() - 1);
        }
        lines.addAll(footer);

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
     *   [0] = weapon damage % bonus (e.g. 200 for T1 sword = +100% more)
     *   [1] = total specific boss defence % vs that type (universalDef + specificDef combined, capped at 95)
     *   [2] = universal (all-boss) defence % from this type's pieces
     *
     * Crown of Greed: if worn, returns empty map (all defence disabled).
     */
    private static Map<SlayerManager.SlayerType, int[]> collectBountyGearStats(ServerPlayerEntity player) {
        Map<SlayerManager.SlayerType, int[]> stats = new LinkedHashMap<>();

        // Crown of Greed disables all bounty defence
        ItemStack helmetCheck = player.getEquippedStack(EquipmentSlot.HEAD);
        if (SlayerItems.isCrownOfGreed(helmetCheck)) {
            return stats; // empty — no defence
        }

        // ── Weapon in main hand ──────────────────────────────────
        ItemStack weapon = player.getMainHandStack();
        if (!weapon.isEmpty()) {
            if (SlayerItems.isSkeletonBow(weapon)) {
                int[] s = stats.computeIfAbsent(SlayerManager.SlayerType.SKELETON, k -> new int[3]);
                s[0] = 500;
                s[2] += 10; // Skeleton bow: -10% universal
            } else if (SlayerItems.isUpgradedSlayerSword(weapon)) {
                SlayerManager.SlayerType t = SlayerItems.getSwordSlayerType(weapon);
                if (t != null) {
                    int[] s = stats.computeIfAbsent(t, k -> new int[3]);
                    s[0] = 300;
                    s[1] += 60;                 // -60% specific vs matching boss (additive like BountyDefenseMixin)
                    s[2] += 15;                 // -15% universal vs all bosses
                }
            } else if (SlayerItems.isSlayerSword(weapon)) {
                SlayerManager.SlayerType t = SlayerItems.getSwordSlayerType(weapon);
                if (t != null) {
                    int[] s = stats.computeIfAbsent(t, k -> new int[3]);
                    s[0] = 200;
                    s[1] += 50;                 // -50% specific vs matching boss (additive like BountyDefenseMixin)
                    s[2] += 10;                 // -10% universal vs all bosses
                }
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

            // Special pieces — register universal + specific using the stacking model
            if (SlayerItems.isZombieBerserkerHelmet(armor)) {
                int[] s = stats.computeIfAbsent(SlayerManager.SlayerType.ZOMBIE, k -> new int[3]);
                s[2] += (int)(SlayerItems.BERSERKER_HELMET_ALL_REDUCTION * 100);
                s[1] = Math.max(s[1], (int)(SlayerItems.BERSERKER_HELMET_BOSS_REDUCTION * 100));
                continue;
            }
            if (SlayerItems.isSpiderLeggings(armor)) {
                int[] s = stats.computeIfAbsent(SlayerManager.SlayerType.SPIDER, k -> new int[3]);
                s[2] += (int)(SlayerItems.SPIDER_LEGS_ALL_REDUCTION * 100);
                s[1] = Math.max(s[1], (int)(SlayerItems.SPIDER_LEGS_BOSS_REDUCTION * 100));
                continue;
            }
            if (SlayerItems.isSlimeBoots(armor)) {
                int[] s = stats.computeIfAbsent(SlayerManager.SlayerType.SLIME, k -> new int[3]);
                s[2] += (int)(SlayerItems.SLIME_BOOTS_ALL_REDUCTION * 100);
                s[1] = Math.max(s[1], (int)(SlayerItems.SLIME_BOOTS_BOSS_REDUCTION * 100));
                continue;
            }
            if (SlayerItems.isWardenChestplate(armor)) {
                int[] s = stats.computeIfAbsent(SlayerManager.SlayerType.WARDEN, k -> new int[3]);
                s[2] += (int)(SlayerItems.WARDEN_CHEST_ALL_REDUCTION * 100);
                s[1] = Math.max(s[1], (int)(SlayerItems.WARDEN_CHEST_BOSS_REDUCTION * 100));
                continue;
            }
            // Voidwalker's Crown is a special piece with no set bonus — skip standard processing
            if (SlayerItems.isEnderPhaseHelmet(armor)) continue;

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

        // Apply set bonus for each type using the highest tier detected.
        // Use per-piece tier for accurate partial-set calculations.
        String[] pieceNames = {"Helmet", "Chestplate", "Leggings", "Boots"};
        for (Map.Entry<SlayerManager.SlayerType, Integer> entry : highestTier.entrySet()) {
            SlayerManager.SlayerType type = entry.getKey();
            int maxTier = entry.getValue();
            int[] s = stats.computeIfAbsent(type, k -> new int[3]);
            String pfx = type.name().toLowerCase();

            // Single loop: count equipped pieces and accumulate per-piece contributions
            // using each piece's ACTUAL tier (not the highest tier) for accuracy
            int t1Pieces = 0;
            int t2Pieces = 0;
            int bossContrib = 0;
            int allContrib  = 0;
            for (int pi = 0; pi < armorSlots.length; pi++) {
                ItemStack armor = armorSlots[pi];
                if (armor.isEmpty()) continue;
                String cid = SlayerItems.getCustomItemId(armor);
                if (cid == null) continue;
                int pieceTier = 0;
                if (cid.startsWith(pfx + "_t2_")) pieceTier = 2;
                else if (cid.startsWith(pfx + "_t1_")) pieceTier = 1;
                if (pieceTier > 0) {
                    if (pieceTier == 1) t1Pieces++;
                    else t2Pieces++;
                    SlayerItems.ArmorStats pieceStats = SlayerItems.getArmorStats(type, pieceTier);
                    double mult = SlayerItems.getPieceBossMultiplier(pieceNames[pi]);
                    bossContrib += (int) Math.round(pieceStats.bossReduction * mult * 100);
                    allContrib  += (int) Math.round(pieceStats.allBossReduction * mult * 100);
                }
            }

            if (t1Pieces == 4 || t2Pieces == 4) {
                // Full same-tier set bonus: only when ALL 4 pieces are the same tier (no mixed-set bonus)
                int capPct = t2Pieces == 4 ? 80 : 60;
                s[1] = Math.min(95, s[1] + capPct);
                // Universal defence: use exact allBossReduction of the set's primary tier
                int setTier = t2Pieces == 4 ? 2 : 1;
                SlayerItems.ArmorStats setStats = SlayerItems.getArmorStats(type, setTier);
                s[2] += (int)(setStats.allBossReduction * 100);
            } else {
                // Partial set: sum of the per-piece contributions only
                s[1] = Math.max(s[1], bossContrib);
                s[2] += allContrib;
            }
        }

        // Keep s[1] as boss-specific defence and s[2] as universal — do NOT combine them.
        // This matches the lore which lists them separately:
        //   "This piece: -X% vs Boss / -Y% vs All Bosses"
        // The actual in-game damage model adds specificDef + universalDef when fighting the
        // matching boss, so the player can mentally add s[1] + s[2] for the full picture.

        // Remove entries that have no meaningful stats to display
        stats.entrySet().removeIf(e -> e.getValue()[0] == 0 && e.getValue()[1] == 0 && e.getValue()[2] == 0);
        return stats;
    }

    /**
     * Scans all equipped armor slots and returns a list of active per-piece buff display strings.
     * Each entry is prefixed with "§b  ✦ " and lists the buff name/description.
     * Crown of Greed suppresses all buffs.
     */
    private static List<String> collectActivePieceBuffs(ServerPlayerEntity player) {
        List<String> buffs = new ArrayList<>();

        // Crown of Greed disables all buffs
        if (SlayerItems.isCrownOfGreed(player.getEquippedStack(EquipmentSlot.HEAD))) return buffs;

        EquipmentSlot[] slots = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
        };

        for (EquipmentSlot slot : slots) {
            ItemStack stack = player.getEquippedStack(slot);
            String id = SlayerItems.getCustomItemId(stack);
            if (id == null) {
                // Check legendary special pieces (old items without custom_item_id)
                if (SlayerItems.isSpiderLeggings(stack))    { buffs.add("§b  ✦ §fPoison Immune §7| Web Immune §7| §aSpeed II"); }
                else if (SlayerItems.isZombieBerserkerHelmet(stack)) { buffs.add("§b  ✦ §fBerserker §7| +300% Dmg"); }
                else if (SlayerItems.isSlimeBoots(stack))   { buffs.add("§b  ✦ §fNo Fall Dmg §7| Death Save"); }
                else if (SlayerItems.isWardenChestplate(stack)) { buffs.add("§b  ✦ §fKB Immune §7| NightVis §7| ESP 64blk"); }
                continue;
            }
            String buff = getPieceBuff(id);
            if (buff != null) buffs.add("§b  ✦ §f" + buff);
        }
        return buffs;
    }

    /** Returns the short buff description for a given custom item ID, or null if none. */
    private static String getPieceBuff(String id) {
        return switch (id) {
            // ── LEGENDARY PIECES ─────────────────────────────────────────────
            case "spider_leggings"         -> "Poison Immune §7| Web Immune §7| §aSpeed II";
            case "zombie_berserker_helmet" -> "Berserker §7| +300% Dmg";
            case "slime_boots"             -> "No Fall Dmg §7| Death Save";
            case "warden_chestplate"       -> "KB Immune §7| NightVis §7| ESP 64blk";
            case "voidwalker_crown",
                 "ender_phase_helmet"      -> "Phase Warp §7| Phase Mode §7| §d+4 Hearts";
            // ── ZOMBIE ───────────────────────────────────────────────────────
            case "zombie_t1_helmet"       -> "Hunger Immune §7| +2 Hearts";
            case "zombie_t2_helmet"       -> "Hunger Immune §7| §6Fire Resist";
            case "zombie_t1_chestplate"   -> "+2 Hearts";
            case "zombie_t2_chestplate"   -> "+4 Hearts §7| §aRegen I";
            case "zombie_t1_leggings"     -> "-10% Undead Dmg";
            case "zombie_t2_leggings"     -> "-20% Undead Dmg";
            case "zombie_t1_boots"        -> "+10% KB Resist";
            case "zombie_t2_boots"        -> "+20% KB Resist §7| §aSpeed I (<50% HP)";
            // ── SPIDER ────────────────────────────────────────────────────────
            case "spider_t1_helmet"       -> "+5% Speed";
            case "spider_t2_helmet"       -> "+10% Speed §7| §bNight Vision";
            case "spider_t1_chestplate"   -> "Weakness Immune";
            case "spider_t2_chestplate"   -> "Weakness Immune §7| Poison Thorns";
            case "spider_t1_leggings"     -> "Poison Immune";
            case "spider_t2_leggings"     -> "Poison Immune §7| §aSpeed II";
            case "spider_t1_boots"        -> "+10% Speed";
            case "spider_t2_boots"        -> "+15% Speed §7| Safe Fall +3";
            // ── SKELETON ──────────────────────────────────────────────────────
            case "skeleton_t1_helmet"     -> "+10% Proj Dmg";
            case "skeleton_t2_helmet"     -> "+20% Proj Dmg §7| Glow Arrows";
            case "skeleton_t1_chestplate" -> "+10% Proj Resist";
            case "skeleton_t2_chestplate" -> "+25% Proj Resist";
            case "skeleton_t1_leggings"   -> "+5% Speed";
            case "skeleton_t2_leggings"   -> "+10% Speed";
            case "skeleton_t1_boots"      -> "+5% Speed";
            case "skeleton_t2_boots"      -> "+10% Speed";
            // ── SLIME ─────────────────────────────────────────────────────────
            case "slime_t1_helmet"        -> "+2 Hearts";
            case "slime_t2_helmet"        -> "+4 Hearts §7| §aSaturation";
            case "slime_t1_chestplate"    -> "+4 Hearts";
            case "slime_t2_chestplate"    -> "+6 Hearts §7| Absorption /30s";
            case "slime_t1_leggings"      -> "Jump Boost I";
            case "slime_t2_leggings"      -> "Jump Boost II";
            case "slime_t1_boots"         -> "No Fall Dmg";
            case "slime_t2_boots"         -> "No Fall Dmg §7| Jump Boost II";
            // ── ENDERMAN ──────────────────────────────────────────────────────
            case "enderman_t1_helmet"     -> "Night Vision";
            case "enderman_t2_helmet"     -> "Night Vision";
            case "enderman_t1_chestplate" -> "+5% Speed";
            case "enderman_t2_chestplate" -> "+10% Speed §7| 20% Teleport Dodge";
            case "enderman_t1_leggings"   -> "+5% Speed";
            case "enderman_t2_leggings"   -> "+10% Speed";
            case "enderman_t1_boots"      -> "Safe Fall +3";
            case "enderman_t2_boots"      -> "No Fall Dmg §7| +20% Speed";
            // ── WARDEN ────────────────────────────────────────────────────────
            case "warden_t1_helmet"       -> "Darkness Immune";
            case "warden_t2_helmet"       -> "Darkness Immune §7| Vibration Sense";
            case "warden_t1_chestplate"   -> "+10% KB Resist";
            case "warden_t2_chestplate"   -> "+25% KB Resist §7| Sonic Pulse";
            case "warden_t1_leggings"     -> "+10% KB Resist";
            case "warden_t2_leggings"     -> "+25% KB Resist §7| +6 Hearts";
            case "warden_t1_boots"        -> "Vibration Sense (8 blocks, 3s glow)";
            case "warden_t2_boots"        -> "+10% KB Resist";
            // ── PIGLIN ────────────────────────────────────────────────────────
            case "piglin_t1_helmet"       -> "Fire Resist";
            case "piglin_t2_helmet"       -> "Fire Resist";
            case "piglin_t1_chestplate"   -> "+2 Hearts";
            case "piglin_t2_chestplate"   -> "+4 Hearts";
            case "piglin_t1_leggings"     -> "+5% Speed";
            case "piglin_t2_leggings"     -> "+10% Speed";
            case "piglin_t1_boots"        -> "Fire Resist";
            case "piglin_t2_boots"        -> "Fire Resist §7| +15% Speed";
            default -> null;
        };
    }
}
