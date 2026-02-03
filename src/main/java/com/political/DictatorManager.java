package com.political;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Set;

public class DictatorManager {

    private static boolean dictatorActive = false;
    private static String dictatorUuid = null;
    private static boolean dictatorTaxEnabled = false;
    private static int dictatorTaxAmount = 0;
    private static long lastSummonTime = 0;
    private static long lastSmiteTime = 0;
    private static String previousJudge = null;

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

    public static void setDictator(ServerPlayerEntity player) {
        // ... existing perk clearing code ...

        // Set dictator data
        dictatorActive = true;
        dictatorUuid = player.getUuidAsString();
        previousJudge = DataManager.getJudge();
        DataManager.setJudge(null);
        // ADD: Store and remove the judge
        previousJudge = DataManager.getJudge();
        DataManager.setJudge(null);

        // Pause elections and remove Vice Chair
        ElectionManager.setElectionSystemPaused(true);
        DataManager.setViceChair(null);

        // ... rest of broadcast code ...

        // Reset perk locks so dictator can set fresh perks
        PerkManager.setChairPerksSetThisTerm(false);
        PerkManager.setViceChairPerksSetThisTerm(false);

        // Clear previous term perks (no cooldowns for dictator)
        PerkManager.setPreviousTermPerks(new ArrayList<>());

        // Set dictator data
        dictatorActive = true;
        dictatorUuid = player.getUuidAsString();

        // Pause elections and remove Vice Chair
        ElectionManager.setElectionSystemPaused(true);
        DataManager.setViceChair(null);

        // Broadcast message
        for (ServerPlayerEntity p : PoliticalServer.server.getPlayerManager().getPlayerList()) {
            p.sendMessage(Text.literal("═══════════════════════════════════").formatted(Formatting.DARK_RED));
            p.sendMessage(Text.literal("    ⚠ DICTATOR DECLARED ⚠").formatted(Formatting.RED, Formatting.BOLD));
            p.sendMessage(Text.literal("═══════════════════════════════════").formatted(Formatting.DARK_RED));
            p.sendMessage(Text.literal(player.getName().getString() + " is now the DICTATOR!").formatted(Formatting.RED));
            p.sendMessage(Text.literal("Elections have been suspended.").formatted(Formatting.GRAY));
            p.sendMessage(Text.literal("All previous perks have been cleared!").formatted(Formatting.YELLOW));
            p.sendMessage(Text.literal("═══════════════════════════════════").formatted(Formatting.DARK_RED));
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
        for (ServerPlayerEntity p : PoliticalServer.server.getPlayerManager().getPlayerList()) {
            p.sendMessage(Text.literal("═══════════════════════════════════").formatted(Formatting.GREEN));
            p.sendMessage(Text.literal("    ✓ DICTATORSHIP ENDED ✓").formatted(Formatting.GREEN, Formatting.BOLD));
            p.sendMessage(Text.literal("═══════════════════════════════════").formatted(Formatting.GREEN));
            p.sendMessage(Text.literal("All leadership positions have been cleared!").formatted(Formatting.YELLOW));
            p.sendMessage(Text.literal("⚡ EMERGENCY ELECTION STARTED!").formatted(Formatting.GOLD, Formatting.BOLD));
            p.sendMessage(Text.literal("Use /vote to participate!").formatted(Formatting.GREEN));
            p.sendMessage(Text.literal("═══════════════════════════════════").formatted(Formatting.GREEN));
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
            for (ServerPlayerEntity p : PoliticalServer.server.getPlayerManager().getPlayerList()) {
                p.sendMessage(Text.literal("⚠ The Dictator has imposed a " + amount + " credit daily tax!").formatted(Formatting.RED));
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

    public static void summonPlayer(ServerPlayerEntity dictator, String targetUuid) {
        if (!canSummon()) return;

        ServerPlayerEntity target = PoliticalServer.server.getPlayerManager().getPlayer(java.util.UUID.fromString(targetUuid));
        if (target != null) {
            target.teleport(
                    dictator.getEntityWorld(),
                    dictator.getX(),
                    dictator.getY(),
                    dictator.getZ(),
                    Set.of(),
                    target.getYaw(),
                    target.getPitch(),
                    false
            );
            target.sendMessage(Text.literal("You have been summoned by the Dictator!").formatted(Formatting.RED, Formatting.BOLD));
            dictator.sendMessage(Text.literal("Summoned " + target.getName().getString() + "!").formatted(Formatting.GREEN));
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

    public static void smitePlayer(ServerPlayerEntity source, ServerPlayerEntity target) {
        if (!canSmite()) return;

        LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(target.getEntityWorld(), SpawnReason.EVENT);
        if (lightning != null) {
            lightning.setPosition(target.getX(), target.getY(), target.getZ());
            target.getEntityWorld().spawnEntity(lightning);
        }

        target.sendMessage(Text.literal("You have been SMITTEN by the Dictator!").formatted(Formatting.RED, Formatting.BOLD));
        lastSmiteTime = System.currentTimeMillis();

        for (ServerPlayerEntity p : PoliticalServer.server.getPlayerManager().getPlayerList()) {
            p.sendMessage(Text.literal("⚡ " + target.getName().getString() + " has been smitten by the Dictator!").formatted(Formatting.RED));
        }
    }

    public static void checkPlayerJoin(ServerPlayerEntity player) {
        if (!dictatorActive) return;

        player.sendMessage(Text.literal("═══════════════════════════════════").formatted(Formatting.DARK_RED));
        player.sendMessage(Text.literal("    ⚠ DICTATORSHIP ACTIVE ⚠").formatted(Formatting.RED, Formatting.BOLD));
        player.sendMessage(Text.literal("═══════════════════════════════════").formatted(Formatting.DARK_RED));
        player.sendMessage(Text.literal("Dictator: " + getDictatorName()).formatted(Formatting.RED));

        if (dictatorTaxEnabled) {
            player.sendMessage(Text.literal("Daily Tax: " + dictatorTaxAmount + " credits").formatted(Formatting.GOLD));
            int owed = TaxManager.getTaxOwed(player.getUuidAsString());
            if (owed > 0) {
                player.sendMessage(Text.literal("You owe: " + owed + " credits in taxes!").formatted(Formatting.RED));
            }
        }

        player.sendMessage(Text.literal("═══════════════════════════════════").formatted(Formatting.DARK_RED));
    }
}