package com.political;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.Random;

/**
 * Random ruling-party mini-games: spend party budget to win rewards or lose big.
 * Target player: party leader if online, else #2 supporter (vice).
 */
public final class PartyCrisisManager {

    private static final Random RAND = new Random();
    private static long nextEligibleMs;

    public static void onLoaded() {
        long saved = DataManager.getData().partyCrisisCooldownUntilMs;
        long now = System.currentTimeMillis();
        if (saved <= now) {
            nextEligibleMs = now + (15L + RAND.nextInt(30)) * 60_000L;
        } else {
            nextEligibleMs = saved;
        }
    }

    public static void tick(MinecraftServer server) {
        if (server.getTickCount() % 400 != 0) return;
        long now = System.currentTimeMillis();
        if (now < nextEligibleMs) return;

        String rp = PoliticalPartyManager.getRulingPartyId();
        if (rp == null) return;

        ServerPlayer actor = PoliticalPartyManager.resolveRulingPartyDelegateOnline(server);
        if (actor == null) return;

        int roll = RAND.nextInt(3);
        switch (roll) {
            case 0 -> VillageSiegeGui.open(actor, rp);
            case 1 -> BorderRaidGui.open(actor, rp);
            default -> TreasuryPanicGui.open(actor, rp);
        }

        scheduleNextCrisis();
        DataManager.getData().partyCrisisCooldownUntilMs = nextEligibleMs;
        DataManager.save(server);
    }

    private static void scheduleNextCrisis() {
        // 25–70 minutes
        long span = (25L + RAND.nextInt(46)) * 60L * 1000L;
        nextEligibleMs = System.currentTimeMillis() + span;
    }

    /** Persisted on {@link DataManager#save} so cooldown survives autosave without starting a crisis. */
    public static long getCooldownUntilMs() {
        return nextEligibleMs;
    }

    public static void broadcastParty(ServerPlayer actor, Component message) {
        String pid = PoliticalPartyManager.getPlayerPartyId(actor.getUUID().toString());
        if (pid == null) return;
        for (ServerPlayer p : PoliticalServer.server.getPlayerList().getPlayers()) {
            if (pid.equals(PoliticalPartyManager.getPlayerPartyId(p.getUUID().toString()))) {
                p.sendSystemMessage(message, false);
            }
        }
    }
}
