package com.political;

import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the custom tab list (player list) header and footer shown to all players.
 * Updated periodically so the player count, auction timer, and active perks stay fresh.
 */
public class TabListManager {

    private static int tickCounter = 0;
    private static final int UPDATE_INTERVAL = 40; // every 2 seconds

    public static void tick(MinecraftServer server) {
        tickCounter++;
        if (tickCounter < UPDATE_INTERVAL) return;
        tickCounter = 0;
        updateAll(server);
    }

    public static void onPlayerJoin(ServerPlayerEntity player) {
        sendTabList(player, PoliticalServer.server);
    }

    private static void updateAll(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            sendTabList(player, server);
        }
    }

    private static void sendTabList(ServerPlayerEntity player, MinecraftServer server) {
        int playerCount = server.getPlayerManager().getCurrentPlayerCount();
        int maxPlayers  = server.getPlayerManager().getMaxPlayerCount();

        // Government info
        String chairUuid = DataManager.getChair();
        String viceUuid  = DataManager.getViceChair();
        String chairName = chairUuid != null ? DataManager.getPlayerName(chairUuid) : "None";
        String viceName  = viceUuid  != null ? DataManager.getPlayerName(viceUuid)  : "None";

        // Auction line
        String auctionLine;
        if (UndergroundAuctionManager.isAuctionActive()) {
            auctionLine = "§6🏪 Underground Auction: §a§lLIVE";
        } else {
            long ms = UndergroundAuctionManager.getTimeUntilNextAuction();
            auctionLine = "§6🏪 Underground Auction: §f" + formatDuration(ms);
        }

        // Build header
        Text header = Text.literal(
            "§6§l✦ CivilCraft ✦§r\n" +
            "§8────────────────────────\n" +
            "§7Players: §e" + playerCount + "§7/§e" + maxPlayers
        );

        // Build perks section
        String perksSection = buildPerksSection();

        // Build overflow section from scoreboard (bounty gear lines that didn't fit)
        List<String> overflow = ScoreboardManager.getOverflowLines(player.getUuid());
        String overflowSection = buildOverflowSection(overflow);

        // Build buffs section for this player
        String buffsSection = buildBuffsSection(player);

        // Build footer
        Text footer = Text.literal(
            "§8────────────────────────\n" +
            "§a⚖ §7Chair: §c" + chairName + "  §9Vice: §b" + viceName + "\n" +
            auctionLine + "\n" +
            "§8────────────────────────\n" +
            perksSection +
            buffsSection +
            overflowSection +
            "§8────────────────────────\n" +
            "§7play.§bcivil§7craft.net"
        );

        player.networkHandler.sendPacket(new PlayerListHeaderS2CPacket(header, footer));
    }

    /**
     * Builds a section for scoreboard overflow lines (bounty gear stats that didn't fit
     * on the sidebar). Returns an empty string when there are no overflow lines.
     */
    private static String buildOverflowSection(List<String> overflow) {
        if (overflow == null || overflow.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("§c§l⚔ Bounty Gear §r\n");
        for (String line : overflow) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    /**
     * Builds a formatted string listing all active perks, colour-coded by type.
     * Positive perks → green, Negative perks → red, Neutral perks → yellow.
     * Returns an empty string (without its own newline) when no perks are active,
     * so the footer stays compact.
     */
    private static String buildPerksSection() {
        List<String> activePerks = PerkManager.getActivePerks();
        if (activePerks.isEmpty()) {
            return "§8No active perks this term\n";
        }

        List<String> positiveLines = new ArrayList<>();
        List<String> negativeLines = new ArrayList<>();
        List<String> neutralLines  = new ArrayList<>();

        for (String perkId : activePerks) {
            Perk perk = PerkManager.getPerk(perkId);
            if (perk == null) continue;
            switch (perk.type) {
                case POSITIVE -> positiveLines.add("§a+ " + perk.name);
                case NEGATIVE -> negativeLines.add("§c- " + perk.name);
                default       -> neutralLines .add("§e~ " + perk.name);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("§d§l⚡ Active Perks§r\n");

        // Show positive perks
        if (!positiveLines.isEmpty()) {
            // Pair them two-per-line to keep the footer compact
            for (int i = 0; i < positiveLines.size(); i += 2) {
                sb.append("  ").append(positiveLines.get(i));
                if (i + 1 < positiveLines.size()) {
                    sb.append("  ").append(positiveLines.get(i + 1));
                }
                sb.append("\n");
            }
        }

        // Show neutral perks
        if (!neutralLines.isEmpty()) {
            for (int i = 0; i < neutralLines.size(); i += 2) {
                sb.append("  ").append(neutralLines.get(i));
                if (i + 1 < neutralLines.size()) {
                    sb.append("  ").append(neutralLines.get(i + 1));
                }
                sb.append("\n");
            }
        }

        // Show negative perks
        if (!negativeLines.isEmpty()) {
            for (int i = 0; i < negativeLines.size(); i += 2) {
                sb.append("  ").append(negativeLines.get(i));
                if (i + 1 < negativeLines.size()) {
                    sb.append("  ").append(negativeLines.get(i + 1));
                }
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * Builds a formatted string listing the player's active buffs.
     * Returns an empty string when no buffs are active.
     */
    private static String buildBuffsSection(ServerPlayerEntity player) {
        java.util.Set<PlayerBuffManager.PlayerBuff> buffs = PlayerBuffManager.getBuffObjects(player.getUuidAsString());
        if (buffs.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("§d§l✦ Your Buffs§r\n");

        for (PlayerBuffManager.PlayerBuff buff : buffs) {
            sb.append("  ").append(buff.colorCode).append("◆ ").append(buff.displayName).append("\n");
        }

        return sb.toString();
    }

    private static String formatDuration(long ms) {
        if (ms <= 0) return "Now";
        long totalSecs = ms / 1000;
        long hours   = totalSecs / 3600;
        long minutes = (totalSecs % 3600) / 60;
        if (hours > 0) return hours + "h " + minutes + "m";
        long seconds = totalSecs % 60;
        if (minutes > 0) return minutes + "m " + seconds + "s";
        return seconds + "s";
    }
}
