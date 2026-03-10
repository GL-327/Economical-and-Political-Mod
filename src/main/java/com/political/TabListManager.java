package com.political;

import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * Manages the custom tab list (player list) header and footer shown to all players.
 * Updated periodically so the player count and auction timer stay fresh.
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

        Text header = Text.literal(
            "§6§l✦ CivilCraft ✦\n" +
            "§8────────────────────────\n" +
            "§7Players: §e" + playerCount + "§7/§e" + maxPlayers
        );

        Text footer = Text.literal(
            "§8────────────────────────\n" +
            "§a⚖ Chair: §c" + chairName + "  §9Vice: " + viceName + "\n" +
            auctionLine + "\n" +
            "§8────────────────────────\n" +
            "§7play.§bcivil§7craft.net"
        );

        player.networkHandler.sendPacket(new PlayerListHeaderS2CPacket(header, footer));
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
