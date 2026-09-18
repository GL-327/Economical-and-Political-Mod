package com.political.government;
import com.political.DataManager;
import com.political.PlayerTeleportHelper;
import com.political.PoliticalServer;
import com.political.TaxManager;


import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DictatorManager {

    private static boolean dictatorActive = false;
    private static String dictatorUuid = null;
    private static boolean dictatorTaxEnabled = false;
    private static int dictatorTaxAmount = 0;
    private static long lastSummonTime = 0;
    private static long lastSmiteTime = 0;
    private static String previousJudge = null;

    private static final Map<UUID, Boolean> pendingDictatorOffer = new HashMap<>();
    private static final Map<UUID, Boolean> pendingLeadershipChoice = new HashMap<>();

    private static final long SUMMON_COOLDOWN_MS = 60L * 1000L;
    private static final long SMITE_COOLDOWN_MS = 15L * 1000L;

    public static void loadFromData(DataManager.SaveData data) {
        dictatorActive = data.dictatorActive;
        dictatorUuid = data.dictator;
        dictatorTaxEnabled = data.dictatorTaxEnabled;
        dictatorTaxAmount = data.dictatorTaxAmount;
        previousJudge = data.previousJudge;  // ADD THIS LINE
    }

    public static void saveToData(DataManager.SaveData data) {
        data.dictatorActive = dictatorActive;
        data.dictator = dictatorUuid;
        data.dictatorTaxEnabled = dictatorTaxEnabled;
        data.dictatorTaxAmount = dictatorTaxAmount;
        data.previousJudge = previousJudge;  // ADD THIS
    }

    public static boolean isDictatorActive() {
        return dictatorActive;
    }

    public static boolean isDictator(String uuid) {
        return dictatorActive && uuid != null && uuid.equals(dictatorUuid);
    }

    public static String getDictatorUuid() {
        return dictatorUuid;
    }

    public static String getDictatorName() {
        if (dictatorUuid == null) return null;
        return DataManager.getPlayerName(dictatorUuid);
    }

    public static void setDictator(ServerPlayer player) {
        // Store and remove the judge FIRST
        previousJudge = DataManager.getJudge();
        DataManager.setJudge(null);

        // Set dictator data
        dictatorActive = true;
        dictatorUuid = player.getUUID().toString();

        // Pause elections and remove Vice Chair
        ElectionManager.setElectionSystemPaused(true);
        DataManager.setViceChair(null);

        // Reset perk locks so dictator can set fresh perks
        PerkManager.setChairPerksSetThisTerm(false);
        PerkManager.setViceChairPerksSetThisTerm(false);
        PerkManager.setPreviousTermPerks(new ArrayList<>());

        // Broadcast message
        for (ServerPlayer p : PoliticalServer.server.getPlayerList().getPlayers()) {
            p.sendSystemMessage(Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.DARK_RED));
            p.sendSystemMessage(Component.literal("    ⚠ DICTATOR DECLARED ⚠").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
            p.sendSystemMessage(Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.DARK_RED));
            p.sendSystemMessage(Component.literal(player.getName().getString() + " is now the DICTATOR!").withStyle(ChatFormatting.RED));
            p.sendSystemMessage(Component.literal("Elections have been suspended.").withStyle(ChatFormatting.GRAY));
            p.sendSystemMessage(Component.literal("All previous perks have been cleared!").withStyle(ChatFormatting.YELLOW));
            p.sendSystemMessage(Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.DARK_RED));
        }

        DataManager.save(PoliticalServer.server);
    }
    // Add this method anywhere in the class
    public static boolean hasJudgePermissions(String uuid) {
        // Dictator has all judge powers
        if (isDictator(uuid)) {
            return true;
        }
        // Otherwise check if they're the actual judge
        return uuid != null && uuid.equals(DataManager.getJudge());
    }
    public static void removeDictator() {
        if (!dictatorActive) return;

        dictatorActive = false;
        dictatorUuid = null;
        dictatorTaxEnabled = false;
        dictatorTaxAmount = 0;

        // Resume elections
        ElectionManager.setElectionSystemPaused(false);

        // Restore the previous judge
        if (previousJudge != null) {
            DataManager.setJudge(previousJudge);
            previousJudge = null;
        }

        // Wipe ALL leadership and perks
        DataManager.setChair(null);
        DataManager.setViceChair(null);
        PerkManager.clearAllPerks();
        // ... rest of perk clearing ...

        // ADD THIS: Reset impeachment when dictator is removed
        ElectionManager.resetImpeachment();

        // Start emergency election
        ElectionManager.startEmergencyElectionSilent(PoliticalServer.server);

        // ... broadcast messages ...
        for (ServerPlayer p : PoliticalServer.server.getPlayerList().getPlayers()) {
            p.sendSystemMessage(Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.GREEN));
            p.sendSystemMessage(Component.literal("    ✓ DICTATORSHIP ENDED ✓").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
            p.sendSystemMessage(Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.GREEN));
            p.sendSystemMessage(Component.literal("All leadership positions have been cleared!").withStyle(ChatFormatting.YELLOW));
            p.sendSystemMessage(Component.literal("⚡ EMERGENCY ELECTION STARTED!").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            p.sendSystemMessage(Component.literal("Use /vote to participate!").withStyle(ChatFormatting.GREEN));
            p.sendSystemMessage(Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.GREEN));
        }

        DataManager.save(PoliticalServer.server);
    }

    public static boolean isDictatorTaxEnabled() {
        return dictatorActive && dictatorTaxEnabled;
    }

    public static void setDictatorTax(boolean enabled, int amount) {
        dictatorTaxEnabled = enabled;
        dictatorTaxAmount = amount;
        DataManager.save(PoliticalServer.server);

        if (enabled) {
            for (ServerPlayer p : PoliticalServer.server.getPlayerList().getPlayers()) {
                p.sendSystemMessage(Component.literal("⚠ The Dictator has imposed a " + amount + " credit daily tax!").withStyle(ChatFormatting.RED));
            }
        }
    }

    public static int getDictatorTaxAmount() {
        return dictatorTaxAmount;
    }

    public static boolean canSummon() {
        return System.currentTimeMillis() - lastSummonTime >= SUMMON_COOLDOWN_MS;
    }

    public static long getSummonCooldownRemaining() {
        long remaining = SUMMON_COOLDOWN_MS - (System.currentTimeMillis() - lastSummonTime);
        return Math.max(0, remaining / 1000);
    }

    public static void summonPlayer(ServerPlayer dictator, String targetUuid) {
        if (!canSummon()) return;

        ServerPlayer target = PoliticalServer.server.getPlayerList().getPlayer(java.util.UUID.fromString(targetUuid));
        if (target != null) {
            PlayerTeleportHelper.teleport(target, (net.minecraft.server.level.ServerLevel) dictator.level(),
                    dictator.getX(),
                    dictator.getY(),
                    dictator.getZ(),
                    target.getYRot(),
                    target.getXRot());
            target.sendSystemMessage(Component.literal("You have been summoned by the Dictator!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
            dictator.sendSystemMessage(Component.literal("Summoned " + target.getName().getString() + "!").withStyle(ChatFormatting.GREEN));
            lastSummonTime = System.currentTimeMillis();
        }
    }

    public static boolean canSmite() {
        return System.currentTimeMillis() - lastSmiteTime >= SMITE_COOLDOWN_MS;
    }

    public static long getSmiteCooldownRemaining() {
        long remaining = SMITE_COOLDOWN_MS - (System.currentTimeMillis() - lastSmiteTime);
        return Math.max(0, remaining / 1000);
    }

    public static void smitePlayer(ServerPlayer source, ServerPlayer target) {
        if (!canSmite()) return;

        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(target.level(), EntitySpawnReason.EVENT);
        if (lightning != null) {
            lightning.setPos(target.getX(), target.getY(), target.getZ());
            target.level().addFreshEntity(lightning);
        }

        target.sendSystemMessage(Component.literal("You have been SMITTEN by the Dictator!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        lastSmiteTime = System.currentTimeMillis();

        for (ServerPlayer p : PoliticalServer.server.getPlayerList().getPlayers()) {
            p.sendSystemMessage(Component.literal("⚡ " + target.getName().getString() + " has been smitten by the Dictator!").withStyle(ChatFormatting.RED));
        }
    }

    public static void offerDictatorTitle(ServerPlayer player) {
        UUID uuid = player.getUUID();
        pendingDictatorOffer.put(uuid, true);

        MutableComponent msg = Component.literal("You received overwhelming support! Would you like to become Dictator?").withStyle(ChatFormatting.GOLD);
        MutableComponent yes = Component.literal(" [YES]").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD);
        yes = yes.withStyle(s -> s.withClickEvent(new ClickEvent.RunCommand("/dictator accept")));
        MutableComponent no = Component.literal(" [NO]").withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
        no = no.withStyle(s -> s.withClickEvent(new ClickEvent.RunCommand("/dictator decline")));

        player.sendSystemMessage(msg.append(yes).append(no));
    }

    public static boolean hasPendingDictatorOffer(UUID uuid) {
        return pendingDictatorOffer.containsKey(uuid);
    }

    public static void acceptDictatorOffer(ServerPlayer player) {
        UUID uuid = player.getUUID();
        if (!pendingDictatorOffer.containsKey(uuid)) {
            player.sendSystemMessage(Component.literal("You don't have a pending dictator offer!").withStyle(ChatFormatting.RED));
            return;
        }
        pendingDictatorOffer.remove(uuid);
        DictatorManager.setDictator(player);
    }

    public static void declineDictatorOffer(ServerPlayer player) {
        UUID uuid = player.getUUID();
        if (!pendingDictatorOffer.containsKey(uuid)) {
            player.sendSystemMessage(Component.literal("You don't have a pending dictator offer!").withStyle(ChatFormatting.RED));
            return;
        }
        pendingDictatorOffer.remove(uuid);
        player.sendSystemMessage(Component.literal("You declined the Dictator title.").withStyle(ChatFormatting.GREEN));
    }

    public static void handleDictatorKilled(ServerPlayer dictator, ServerPlayer killer) {
        removeDictator();

        UUID killerUuid = killer.getUUID();
        pendingLeadershipChoice.put(killerUuid, true);

        MutableComponent msg = Component.literal("You slew the Dictator! Choose your path:").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
        killer.sendSystemMessage(msg);

        MutableComponent force = Component.literal("[Become Leader by Force]").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD);
        force = force.withStyle(s -> s.withClickEvent(new ClickEvent.RunCommand("/takeleadership force")));
        killer.sendSystemMessage(force);

        MutableComponent election = Component.literal("[Run an Election]").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD);
        election = election.withStyle(s -> s.withClickEvent(new ClickEvent.RunCommand("/takeleadership election")));
        killer.sendSystemMessage(election);
    }

    public static boolean hasPendingLeadershipChoice(UUID uuid) {
        return pendingLeadershipChoice.containsKey(uuid);
    }

    public static void takeLeadershipByForce(ServerPlayer player) {
        UUID uuid = player.getUUID();
        if (!pendingLeadershipChoice.containsKey(uuid)) {
            player.sendSystemMessage(Component.literal("You don't have a pending leadership choice!").withStyle(ChatFormatting.RED));
            return;
        }
        pendingLeadershipChoice.remove(uuid);
        DataManager.setChair(player.getUUID().toString());
        DataManager.setViceChair(null);
        PerkManager.clearAllPerks();
        PerkManager.onNewTermStart();
        player.sendSystemMessage(Component.literal("You have seized power! You are now the Chair.").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        for (ServerPlayer p : PoliticalServer.server.getPlayerList().getPlayers()) {
            if (!p.getUUID().toString().equals(player.getUUID().toString())) {
                p.sendSystemMessage(Component.literal("⚠ " + player.getName().getString() + " has seized power as the new Chair!").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
            }
        }
        DataManager.save(PoliticalServer.server);
    }

    public static void takeLeadershipByElection(ServerPlayer player) {
        UUID uuid = player.getUUID();
        if (!pendingLeadershipChoice.containsKey(uuid)) {
            player.sendSystemMessage(Component.literal("You don't have a pending leadership choice!").withStyle(ChatFormatting.RED));
            return;
        }
        pendingLeadershipChoice.remove(uuid);
        ElectionManager.startEmergencyElection(PoliticalServer.server);
    }

    public static void checkPlayerJoin(ServerPlayer player) {
        if (!dictatorActive) return;

        player.sendSystemMessage(Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.DARK_RED));
        player.sendSystemMessage(Component.literal("    ⚠ DICTATORSHIP ACTIVE ⚠").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.DARK_RED));
        player.sendSystemMessage(Component.literal("Dictator: " + getDictatorName()).withStyle(ChatFormatting.RED));

        if (dictatorTaxEnabled) {
            player.sendSystemMessage(Component.literal("Daily Tax: " + dictatorTaxAmount + " credits").withStyle(ChatFormatting.GOLD));
            int owed = TaxManager.getTaxOwed(player.getUUID().toString());
            if (owed > 0) {
                player.sendSystemMessage(Component.literal("You owe: " + owed + " credits in taxes!").withStyle(ChatFormatting.RED));
            }
        }

        player.sendSystemMessage(Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.DARK_RED));
    }
}