package com.political;


import com.political.bounty.SlayerData;
import com.political.bounty.SlayerItems;
import com.political.bounty.SlayerManager;
import com.political.economy.CoinManager;
import com.political.economy.UndergroundAuctionManager;
import com.political.party.PoliticalPartyManager;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.chat.numbers.BlankFormat;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;

import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
        if (tickCounter < 5) return;  // Update every 5 ticks instead of 20 for better reliability
        tickCounter = 0;
        animFrame++;
        displayTickCounter++;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            updateScoreboard(player);
            // Re-send display packet more frequently to ensure scoreboard stays visible
            if (displayTickCounter % 5 == 0) {  // Every 25 ticks instead of 400
                sendDisplayPacketInternal(player);
            }
        }
    }

    public static void onPlayerJoin(ServerPlayer player) {
        // Defer scoreboard setup to next tick to ensure player network is fully ready
        PoliticalServer.server.execute(() -> {
            setupPlayerScoreboard(player);
            updateScoreboard(player);
            sendDisplayPacket(player);
        });
    }

    public static void onPlayerDisconnect(ServerPlayer player) {
        cleanupPlayerScoreboard(player);
        playerOverflowLines.remove(player.getUUID());
    }

    // ── Setup / Cleanup ──────────────────────────────────────────

    private static void setupPlayerScoreboard(ServerPlayer player) {
        ServerScoreboard scoreboard = PoliticalServer.server.getScoreboard();
        String prefix = getPlayerPrefix(player);

        // Remove old objective if it exists
        String objName = "sb_" + prefix;
        Objective old = scoreboard.getObjective(objName);
        if (old != null) scoreboard.removeObjective(old);

        // Create new objective (use BlankNumberFormat to hide score integers)
        Objective objective = scoreboard.addObjective(
            objName,
            ObjectiveCriteria.DUMMY,
            Component.literal("§6§l CivilCraft §8Version X"),
            ObjectiveCriteria.RenderType.INTEGER,
            false,
            BlankFormat.INSTANCE
        );
        playerObjectiveNames.put(player.getUUID(), objName);

        // Create per-player teams (one per line)
        String[] teamNames = new String[MAX_LINES];
        for (int i = 0; i < MAX_LINES; i++) {
            String teamName = "sbt" + prefix + String.format("%02d", i);
            // Remove old team if exists
            PlayerTeam existing = scoreboard.getPlayerTeam(teamName);
            if (existing != null) scoreboard.removePlayerTeam(existing);
            PlayerTeam team = scoreboard.addPlayerTeam(teamName);
            team.setPlayerPrefix(Component.literal(""));
            team.setSeeFriendlyInvisibles(false);
            teamNames[i] = teamName;
            // Holder name: use color codes so they're invisible in sidebar
            // Format: §<code1>§<code2> - unique per line, renders empty
            String c1 = HOLDER_COLOR_CODES[i % HOLDER_COLOR_CODES.length];
            String c2 = HOLDER_COLOR_CODES[(i / HOLDER_COLOR_CODES.length) % HOLDER_COLOR_CODES.length];
            String holderName = "§" + c1 + "§" + c2;
            scoreboard.addPlayerToTeam(holderName, team);
        }
        playerTeamNames.put(player.getUUID(), teamNames);
        
        // Send display packet after everything is set up
        player.connection.send(new ClientboundSetDisplayObjectivePacket(DisplaySlot.SIDEBAR, objective));
    }

    private static void cleanupPlayerScoreboard(ServerPlayer player) {
        ServerScoreboard scoreboard = PoliticalServer.server.getScoreboard();

        // Remove teams
        String[] teamNames = playerTeamNames.remove(player.getUUID());
        if (teamNames != null) {
            for (String teamName : teamNames) {
                if (teamName == null) continue;
                PlayerTeam team = scoreboard.getPlayerTeam(teamName);
                if (team != null) scoreboard.removePlayerTeam(team);
            }
        }

        // Remove objective
        String objName = playerObjectiveNames.remove(player.getUUID());
        if (objName != null) {
            Objective obj = scoreboard.getObjective(objName);
            if (obj != null) scoreboard.removeObjective(obj);
        }
    }

    private static void sendDisplayPacketInternal(ServerPlayer player) {
        String objName = playerObjectiveNames.get(player.getUUID());
        if (objName == null) return;
        ServerScoreboard scoreboard = PoliticalServer.server.getScoreboard();
        Objective obj = scoreboard.getObjective(objName);
        if (obj == null) return;
        
        scoreboard.setDisplayObjective(DisplaySlot.SIDEBAR, obj);
        player.connection.send(new ClientboundSetDisplayObjectivePacket(DisplaySlot.SIDEBAR, obj));
    }
    
    public static void sendDisplayPacket(ServerPlayer player) {
        sendDisplayPacketInternal(player);
    }

    // ── Update ────────────────────────────────────────────────────

    private static String getHolderName(String prefix, int i) {
        String c1 = HOLDER_COLOR_CODES[i % HOLDER_COLOR_CODES.length];
        String c2 = HOLDER_COLOR_CODES[(i / HOLDER_COLOR_CODES.length) % HOLDER_COLOR_CODES.length];
        return "§" + c1 + "§" + c2;
    }

    private static void updateScoreboard(ServerPlayer player) {
        String objName = playerObjectiveNames.get(player.getUUID());
        String[] teamNames = playerTeamNames.get(player.getUUID());
        if (objName == null || teamNames == null) {
            setupPlayerScoreboard(player);
            objName = playerObjectiveNames.get(player.getUUID());
            teamNames = playerTeamNames.get(player.getUUID());
            if (objName == null || teamNames == null) return;
        }

        ServerScoreboard scoreboard = PoliticalServer.server.getScoreboard();
        Objective objective = scoreboard.getObjective(objName);
        if (objective == null) {
            setupPlayerScoreboard(player);
            objective = scoreboard.getObjective(objName);
            if (objective == null) return;
        }

        // Do NOT set global sidebar slot - causes glitching with multiple players
        // Per-player display packets are sent via sendDisplayPacketInternal instead

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
            PlayerTeam team = scoreboard.getPlayerTeam(teamName);
            if (team == null) continue;

            if (lineText == null) {
                // Hide line: reset its score
                scoreboard.resetSinglePlayerScore(ScoreHolder.forNameOnly(holderName), objective);
                team.setPlayerPrefix(Component.literal(""));
            } else {
                team.setPlayerPrefix(Component.literal(lineText));
                ScoreAccess access = scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly(holderName), objective);
                access.set(score - i);
            }
        }

        // Ensure the sidebar stays visible even if other systems briefly overwrite the display slot.
        sendDisplayPacketInternal(player);
    }

    // ── Line builder ─────────────────────────────────────────────

    // Sidebar content budget (excluding the header separator and footer).
    // Budget of 12 keeps total active lines at ≤15 (1 sep + 9 core + 3 gear + 2 footer),
    // ensuring the cosmetic footer never gets cut off on vanilla clients.
    private static final int SIDEBAR_LINE_BUDGET = 12;

    // Per-player overflow lines sent to the Tab List footer
    private static final Map<UUID, List<String>> playerOverflowLines = new ConcurrentHashMap<>();

    /** Returns the current overflow lines for the given player (used by TabListManager). */
    public static List<String> getOverflowLines(UUID playerUuid) {
        return playerOverflowLines.getOrDefault(playerUuid, Collections.emptyList());
    }

    private static List<String> buildLines(ServerPlayer player) {
        List<String> lines = new ArrayList<>();
        String uuid = player.getUUID().toString();

        // Animated header separator
        String sep = SEPARATOR_FRAMES[animFrame % SEPARATOR_FRAMES.length];
        lines.add(sep);

        // ── Economy ──
        int coins = CoinManager.getCoins(player);
        int credits = CreditItem.countCredits(player);
        lines.add("§e Coins: §f" + formatNumber(coins));
        lines.add("§b Credits: §f" + credits);
        lines.add("§8§m            ");

        // ── Class (compact single line) ──
        if (!ClassManager.isClassDisplayHidden(uuid)) {
            ClassManager.PlayerClass playerClass = ClassManager.getPlayerClass(uuid);
            if (playerClass != null) {
                int classLevel = ClassManager.getClassLevel(uuid, playerClass);
                lines.add("§" + playerClass.color.getChar() + " ⚔ " + playerClass.name + " §7Lv." + classLevel);
            }
        }

        // ── Government ──
        String chairUuid = DataManager.getChair();
        String viceUuid  = DataManager.getViceChair();
        String chairName = chairUuid != null ? DataManager.getPlayerName(chairUuid) : null;
        String viceName  = viceUuid  != null ? DataManager.getPlayerName(viceUuid)  : null;
        lines.add("§a ⚖ §7Chair: " + (chairName != null ? "§f" + chairName : "§8None"));
        lines.add("§7   Vice: " + (viceName  != null ? "§f" + viceName  : "§8None"));

        // ── Party (if in one) ──
        String partyId = PoliticalPartyManager.getPlayerPartyId(uuid);
        if (partyId != null) {
            PoliticalPartyManager.Party party = PoliticalPartyManager.getParty(partyId);
            if (party != null) {
                lines.add("§d ⚜ §f" + party.displayName);
            }
        }

        lines.add("§8§m             ");

        // ── Underground Auction ──
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

        // ── Slayer ──
        int totalLevel = 0;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            totalLevel += SlayerData.getSlayerLevel(uuid, type);
        }
        String levelColor = totalLevel >= 30 ? "§d" : totalLevel >= 15 ? "§b" : "§7";
        lines.add("§e ⭐ §7Slayer: " + levelColor + totalLevel);

        // (online count moved to tab list)

        // ── Gear / quest lines (may overflow to Tab List) ──
        List<String> gearLines = new ArrayList<>();
        if (DataManager.isScoreboardShowBountyGear()) {
            Map<SlayerManager.SlayerType, int[]> bountyStats = collectBountyGearStats(player);
            SlayerManager.ActiveQuest quest = SlayerManager.getActiveQuest(player);
            boolean hasCrown = SlayerItems.isCrownOfGreed(player.getItemBySlot(EquipmentSlot.HEAD));

            if (quest != null) {
                String questColor = (animFrame % 6 < 3) ? "§d" : "§5";
                gearLines.add(questColor + "§l ⚔ §r§7Quest: §c" + quest.slayerType.bossName);
                int required = quest.getKillsRequired();
                int progress = required > 0 ? (quest.killCount * 5 / Math.max(1, required)) : 5;
                StringBuilder bar = new StringBuilder("§f  ");
                for (int i = 0; i < 5; i++) bar.append(i < progress ? "§a█" : "§8█");
                gearLines.add(bar + " §e" + quest.killCount + "/" + required);
                gearLines.add("§7  T" + quest.tier + " " + quest.slayerType.displayName + (quest.bossSpawned ? " §cBoss Active!" : ""));
            } else if (hasCrown) {
                gearLines.add("§6§l 👑 §r§7Crown of Greed");
                gearLines.add("§c  ✗ All defence DISABLED");
            } else if (!bountyStats.isEmpty()) {
                String gearColor = (animFrame % 4 < 2) ? "§c" : "§4";
                gearLines.add(gearColor + "§l ⚔ §r§7Bounty Gear");

                int universalDef = 0;
                int weaponBonus = 0;
                for (int[] s : bountyStats.values()) {
                    universalDef += s[2];
                    if (s[0] > weaponBonus) weaponBonus = s[0];
                }
                if (universalDef > 0) {
                    gearLines.add("§7  DEF all: §a-" + universalDef + "%");
                }
                List<Map.Entry<SlayerManager.SlayerType, int[]>> bossEntries = bountyStats.entrySet().stream()
                        .filter(e -> e.getValue()[1] > 0)
                        .collect(Collectors.toList());
                for (int i = 0; i < bossEntries.size(); i += 2) {
                    Map.Entry<SlayerManager.SlayerType, int[]> first = bossEntries.get(i);
                    StringBuilder sb = new StringBuilder("§7  vs §f")
                            .append(first.getKey().displayName)
                            .append("§7: §a-").append(first.getValue()[1]).append("%");
                    if (i + 1 < bossEntries.size()) {
                        Map.Entry<SlayerManager.SlayerType, int[]> second = bossEntries.get(i + 1);
                        sb.append(" §7| §f").append(second.getKey().displayName)
                                .append("§7: §a-").append(second.getValue()[1]).append("%");
                    }
                    gearLines.add(sb.toString());
                }
                if (weaponBonus > 0) {
                    gearLines.add("§7  DMG: §c+" + (weaponBonus - 100) + "% to boss");
                }
                List<String> pieceBuffs = collectActivePieceBuffs(player);
                gearLines.addAll(pieceBuffs);
            } else {
                gearLines.add("§8  No bounty gear");
            }
        }

        int coreContentCount = lines.size() - 1;
        int gearBudget = Math.max(0, SIDEBAR_LINE_BUDGET - coreContentCount);
        List<String> overflow;
        if (gearLines.size() <= gearBudget) {
            lines.addAll(gearLines);
            overflow = Collections.emptyList();
        } else {
            lines.addAll(gearLines.subList(0, gearBudget));
            overflow = new ArrayList<>(gearLines.subList(gearBudget, gearLines.size()));
        }
        playerOverflowLines.put(player.getUUID(), overflow);

        // Footer
        String footSep = SEPARATOR_FRAMES[(animFrame + SEPARATOR_FRAMES.length / 2) % SEPARATOR_FRAMES.length];
        lines.add(footSep);
        lines.add("§7play.civilcraft.xyz");

        return lines;
    }


    // ── Helpers ──────────────────────────────────────────────────

    /** Returns an 8-char prefix unique per player (first 8 chars of UUID string, no dashes). */
    private static String getPlayerPrefix(ServerPlayer player) {
        return player.getUUID().toString().replace("-", "").substring(0, 8);
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
    private static Map<SlayerManager.SlayerType, int[]> collectBountyGearStats(ServerPlayer player) {
        Map<SlayerManager.SlayerType, int[]> stats = new LinkedHashMap<>();

        // Crown of Greed disables all bounty defence
        ItemStack helmetCheck = player.getItemBySlot(EquipmentSlot.HEAD);
        if (SlayerItems.isCrownOfGreed(helmetCheck)) {
            return stats; // empty — no defence
        }

        // ── Weapon in main hand ──────────────────────────────────
        ItemStack weapon = player.getMainHandItem();
        if (!weapon.isEmpty()) {
            if (SlayerItems.isSkeletonBow(weapon)) {
                int[] s = stats.computeIfAbsent(SlayerManager.SlayerType.SKELETON, k -> new int[3]);
                s[0] = 500;
                s[2] += 4; // Skeleton bow: -4% universal
            } else if (SlayerItems.isUpgradedSlayerSword(weapon)) {
                SlayerManager.SlayerType t = SlayerItems.getSwordSlayerType(weapon);
                if (t != null) {
                    int[] s = stats.computeIfAbsent(t, k -> new int[3]);
                    s[0] = 300;
                    s[1] += 20;                 // -20% specific vs matching boss
                    s[2] += 6;                  // -6% universal vs all bosses
                }
            } else if (SlayerItems.isSlayerSword(weapon)) {
                SlayerManager.SlayerType t = SlayerItems.getSwordSlayerType(weapon);
                if (t != null) {
                    int[] s = stats.computeIfAbsent(t, k -> new int[3]);
                    s[0] = 200;
                    s[1] += 15;                 // -15% specific vs matching boss
                    s[2] += 4;                  // -4% universal vs all bosses
                }
            }
        }

        // ── Armour slots ─────────────────────────────────────────
        ItemStack[] armorSlots = {
            player.getItemBySlot(EquipmentSlot.HEAD),
            player.getItemBySlot(EquipmentSlot.CHEST),
            player.getItemBySlot(EquipmentSlot.LEGS),
            player.getItemBySlot(EquipmentSlot.FEET)
        };

        // Track the highest tier seen per type for the standard set bonus
        Map<SlayerManager.SlayerType, Integer> highestTier = new LinkedHashMap<>();

        for (ItemStack armor : armorSlots) {
            if (armor.isEmpty()) continue;

            // Special pieces — register universal + specific using the stacking model
            if (SlayerItems.isZombieBerserkerHelmet(armor)) {
                int[] s = stats.computeIfAbsent(SlayerManager.SlayerType.ZOMBIE, k -> new int[3]);
                s[2] += (int)(SlayerItems.BERSERKER_HELMET_ALL_REDUCTION * 100);
                s[1] += (int)(SlayerItems.BERSERKER_HELMET_BOSS_REDUCTION * 100);
                continue;
            }
            if (SlayerItems.isSpiderLeggings(armor)) {
                int[] s = stats.computeIfAbsent(SlayerManager.SlayerType.SPIDER, k -> new int[3]);
                s[2] += (int)(SlayerItems.SPIDER_LEGS_ALL_REDUCTION * 100);
                s[1] += (int)(SlayerItems.SPIDER_LEGS_BOSS_REDUCTION * 100);
                continue;
            }
            if (SlayerItems.isSlimeBoots(armor)) {
                int[] s = stats.computeIfAbsent(SlayerManager.SlayerType.SLIME, k -> new int[3]);
                s[2] += (int)(SlayerItems.SLIME_BOOTS_ALL_REDUCTION * 100);
                s[1] += (int)(SlayerItems.SLIME_BOOTS_BOSS_REDUCTION * 100);
                continue;
            }
            if (SlayerItems.isWardenChestplate(armor)) {
                int[] s = stats.computeIfAbsent(SlayerManager.SlayerType.IRON_GOLEM, k -> new int[3]);
                s[2] += (int)(SlayerItems.IRON_GOLEM_CHEST_ALL_REDUCTION * 100);
                s[1] += (int)(SlayerItems.IRON_GOLEM_CHEST_BOSS_REDUCTION * 100);
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
                int capPct = t2Pieces == 4 ? 38 : 26;
                s[1] += capPct;
                // Universal defence: use exact allBossReduction of the set's primary tier
                int setTier = t2Pieces == 4 ? 2 : 1;
                SlayerItems.ArmorStats setStats = SlayerItems.getArmorStats(type, setTier);
                s[2] += (int)(setStats.allBossReduction * 100);
            } else {
                // Partial set: sum of the per-piece contributions, always additive
                s[1] += bossContrib;
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
    private static List<String> collectActivePieceBuffs(ServerPlayer player) {
        List<String> buffs = new ArrayList<>();

        // Crown of Greed disables all buffs
        if (SlayerItems.isCrownOfGreed(player.getItemBySlot(EquipmentSlot.HEAD))) return buffs;

        EquipmentSlot[] slots = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
        };

        for (EquipmentSlot slot : slots) {
            ItemStack stack = player.getItemBySlot(slot);
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
            // ── ZOMBIE ───────────────────────────────────────────────────────
            case "zombie_t1_helmet"       -> "Hunger Immune §7| +2 Hearts";
            case "zombie_t2_helmet"       -> "Hunger Immune §7| §6Fire Resist";
            case "zombie_t1_chestplate"   -> "+2 Hearts";
            case "zombie_t2_chestplate"   -> "+4 Hearts";
            case "zombie_t1_leggings"     -> "-10% Undead Dmg";
            case "zombie_t2_leggings"     -> "-20% Undead Dmg";
            case "zombie_t1_boots"        -> "+10% KB Resist";
            case "zombie_t2_boots"        -> "+20% KB Resist §7| §aSpeed I (<50% HP)";
            // ── SPIDER ────────────────────────────────────────────────────────
            case "spider_t1_helmet"       -> "+5% Speed";
            case "spider_t2_helmet"       -> "+10% Speed";
            case "spider_t1_chestplate"   -> "Weakness Immune";
            case "spider_t2_chestplate"   -> "Weakness Immune §7| Poison Thorns";
            case "spider_t1_leggings"     -> "Poison Immune";
            case "spider_t2_leggings"     -> "Poison Immune §7| §aSpeed I";
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
            case "slime_t2_chestplate"    -> "+4 Hearts §7| Absorption /30s";
            case "slime_t1_leggings"      -> "Jump Boost I";
            case "slime_t2_leggings"      -> "Jump Boost II";
            case "slime_t1_boots"         -> "No Fall Dmg";
            case "slime_t2_boots"         -> "No Fall Dmg §7| Jump Boost II";
            // ── ENDERMAN ──────────────────────────────────────────────────────
            case "enderman_t1_helmet"     -> "Night Vision";
            case "enderman_t2_helmet"     -> "Night Vision";
            case "enderman_t1_chestplate" -> "+5% Speed";
            case "enderman_t2_chestplate" -> "+15% Speed §7| Fall Dmg Immune";
            case "enderman_t1_leggings"   -> "+5% Speed";
            case "enderman_t2_leggings"   -> "+10% Speed";
            case "enderman_t1_boots"      -> "Safe Fall +3";
            case "enderman_t2_boots"      -> "No Fall Dmg §7| +10% Speed";
            // ── WARDEN ────────────────────────────────────────────────────────
            case "warden_t1_helmet"       -> "Darkness Immune";
            case "warden_t2_helmet"       -> "Darkness Immune §7| Vibration Sense";
            case "warden_t1_chestplate"   -> "+10% KB Resist";
            case "warden_t2_chestplate"   -> "+25% KB Resist §7| Resistance I";
            case "warden_t1_leggings"     -> "+10% KB Resist";
            case "warden_t2_leggings"     -> "+15% KB Resist §7| +4 Hearts";
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
            case "piglin_t2_boots"        -> "Fire Resist §7| +10% Speed";
            default -> null;
        };
    }
}
