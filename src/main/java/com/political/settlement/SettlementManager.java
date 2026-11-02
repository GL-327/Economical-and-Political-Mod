package com.political.settlement;
import com.political.CitizenManager;
import com.political.HighCouncilManager;
import com.political.SpawnProtectionManager;



import com.political.economy.CoinManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Central owner of the settlement registry + settlements.json persistence, and the single
 * lifecycle entry point for the whole subsystem: initialize() also boots CitizenManager,
 * SettlementElectionManager and HighCouncilManager; save() cascades to them; tick() drives
 * rescans, tier changes, wander-in spawning, city perks, then the sub-managers' ticks.
 */
public class SettlementManager {

    private static final Map<String, Settlement> settlements = new LinkedHashMap<>();
    private static Path dataFile;

    private static int tickCounter = 0;
    private static int rescanCursor = 0;
    private static final int RESCAN_INTERVAL_TICKS = 200; // ~10s: one settlement per window
    private static final int PERK_INTERVAL_TICKS = 40;

    // ── lifecycle ──

    public static void initialize(Path worldRoot) {
        dataFile = worldRoot.resolve("settlements.json");
        load();
        CitizenManager.initialize(worldRoot);
        SettlementElectionManager.initialize(worldRoot);
        HighCouncilManager.initialize(worldRoot);
    }

    private static void load() {
        settlements.clear();
        if (dataFile == null || !Files.exists(dataFile)) return;
        try (Reader r = Files.newBufferedReader(dataFile)) {
            Gson g = new GsonBuilder().create();
            Type t = new TypeToken<List<Settlement>>(){}.getType();
            List<Settlement> loaded = g.fromJson(r, t);
            if (loaded != null) {
                for (Settlement s : loaded) {
                    if (s != null && s.id != null) settlements.put(s.id, s);
                }
            }
        } catch (Exception e) {
            System.err.println("[SettlementManager] Failed to load: " + e.getMessage());
        }
    }

    public static void save() {
        if (dataFile != null) {
            try {
                Files.createDirectories(dataFile.getParent());
                Gson g = new GsonBuilder().setPrettyPrinting().create();
                try (Writer w = Files.newBufferedWriter(dataFile)) {
                    g.toJson(new ArrayList<>(settlements.values()), w);
                }
            } catch (Exception e) {
                System.err.println("[SettlementManager] Failed to save: " + e.getMessage());
            }
        }
        CitizenManager.save();
        SettlementElectionManager.save();
        HighCouncilManager.save();
    }

    // ── accessors ──

    public static Collection<Settlement> getAllSettlements() {
        return new ArrayList<>(settlements.values());
    }

    public static Settlement get(String id) {
        return id == null ? null : settlements.get(id);
    }

    public static ServerLevel levelFor(MinecraftServer server, Settlement s) {
        if (server == null || s == null) return null;
        try {
            ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, Identifier.parse(s.worldDim));
            return server.getLevel(key);
        } catch (Exception e) {
            return null;
        }
    }

    /** Nearest settlement whose cornerstone is within its own tier radius of the position. */
    public static Settlement getContaining(String dim, BlockPos pos) {
        Settlement best = null;
        double bestSq = Double.MAX_VALUE;
        for (Settlement s : settlements.values()) {
            if (!s.worldDim.equals(dim)) continue;
            double dx = s.cornerstoneX - pos.getX();
            double dy = s.cornerstoneY - pos.getY();
            double dz = s.cornerstoneZ - pos.getZ();
            double d2 = dx * dx + dy * dy + dz * dz;
            int radius = SettlementScanner.radiusForTier(s.tierEnum());
            if (d2 <= (double) radius * radius && d2 < bestSq) {
                bestSq = d2;
                best = s;
            }
        }
        return best;
    }

    public static Settlement getRegisteredSettlement(String playerUuid) {
        for (Settlement s : settlements.values()) {
            if (s.registered().contains(playerUuid)) return s;
        }
        return null;
    }

    /** Nearest METROPOLIS+ settlement to the given one (excluding itself), or null. */
    public static Settlement findNearestMetropolis(Settlement from) {
        Settlement best = null;
        double bestSq = Double.MAX_VALUE;
        for (Settlement s : settlements.values()) {
            if (s == from || !s.worldDim.equals(from.worldDim)) continue;
            if (!s.tierEnum().isAtLeast(SettlementTier.METROPOLIS)) continue;
            double d2 = from.distanceSqTo(s);
            if (d2 < bestSq) { bestSq = d2; best = s; }
        }
        return best;
    }

    /** The single largest METROPOLIS+ settlement server-wide (drives the global Chair). */
    public static Settlement findLargestMetropolis() {
        Settlement best = null;
        long bestScore = Long.MIN_VALUE;
        for (Settlement s : settlements.values()) {
            if (!s.tierEnum().isAtLeast(SettlementTier.METROPOLIS)) continue;
            long score = sizeScore(s);
            if (score > bestScore) { bestScore = score; best = s; }
        }
        return best;
    }

    public static long sizeScore(Settlement s) {
        return (long) s.population * 100L + (long) s.beds * 10L + s.appeal + s.tierEnum().rank() * 1000L;
    }

    // ── founding / membership ──

    public static void foundSettlement(ServerPlayer player, ServerLevel world, BlockPos pos) {
        String dim = world.dimension().identifier().toString();
        // Guard: don't found overlapping settlements.
        Settlement existing = getContaining(dim, pos);
        if (existing != null) {
            player.sendSystemMessage(Component.literal("⚠ Too close to an existing settlement: "
                    + existing.name).withStyle(ChatFormatting.RED));
            return;
        }

        String id = UUID.randomUUID().toString();
        String name = player.getName().getString() + "'s Settlement";
        Settlement s = new Settlement(id, name, player.getUUID().toString(), dim,
                pos.getX(), pos.getY(), pos.getZ());
        settlements.put(id, s);

        // Immediate first scan so the founder sees real numbers.
        try {
            SettlementScanner.ScanResult r = SettlementScanner.scan(world, pos, SettlementTier.OUTPOST);
            applyScan(s, r);
            recomputeTier(s);
        } catch (Exception ignored) {}

        save();

        player.sendSystemMessage(Component.literal("✦ Settlement founded: ")
                .withStyle(ChatFormatting.GOLD).append(Component.literal(s.name)
                        .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)));
        player.sendSystemMessage(Component.literal("Use /settlement register to join it and /settlement rename to name it.")
                .withStyle(ChatFormatting.GRAY));
    }

    public static long registrationFee(SettlementTier tier) {
        return switch (tier) {
            case OUTPOST, HAMLET, VILLAGE -> 0L;
            case TOWN -> 1_000L;
            case CITY -> 10_000L;
            case METROPOLIS -> 50_000L;
            case MEGALOPOLIS -> 250_000L;
            case UTOPIA, ELYSIUM -> 1_000_000L;
        };
    }

    public static void registerPlayer(ServerPlayer player, Settlement s) {
        String uuid = player.getUUID().toString();
        Settlement prev = getRegisteredSettlement(uuid);
        if (prev != null && prev != s) {
            prev.registered().remove(uuid);
        }
        if (s.registered().contains(uuid)) {
            player.sendSystemMessage(Component.literal("You are already registered to " + s.name).withStyle(ChatFormatting.YELLOW));
            return;
        }
        long fee = registrationFee(s.tierEnum());
        if (fee > 0) {
            if (!CoinManager.removeCoins(player, fee)) {
                player.sendSystemMessage(Component.literal("Registration costs " + String.format("%,d", fee)
                        + " coins — you can't afford it.").withStyle(ChatFormatting.RED));
                return;
            }
            s.treasury += fee;
        }
        s.registered().add(uuid);
        save();
        player.sendSystemMessage(Component.literal("✓ Registered as a citizen of " + s.name)
                .withStyle(ChatFormatting.GREEN));
    }

    public static void leaveSettlement(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        Settlement s = getRegisteredSettlement(uuid);
        if (s == null) {
            player.sendSystemMessage(Component.literal("You are not registered to any settlement.").withStyle(ChatFormatting.RED));
            return;
        }
        s.registered().remove(uuid);
        save();
        player.sendSystemMessage(Component.literal("You left " + s.name + ".").withStyle(ChatFormatting.YELLOW));
    }

    public static void renameSettlement(ServerPlayer player, Settlement s, String newName) {
        if (!player.getUUID().toString().equals(s.ownerUuid) && !SpawnProtectionManager.isOperator(player)) {
            player.sendSystemMessage(Component.literal("Only the founder can rename this settlement.").withStyle(ChatFormatting.RED));
            return;
        }
        s.name = newName;
        save();
        player.sendSystemMessage(Component.literal("Settlement renamed to " + newName).withStyle(ChatFormatting.GREEN));
    }

    // ── tick ──

    public static void tick(MinecraftServer server) {
        tickCounter++;

        if (!settlements.isEmpty() && tickCounter % RESCAN_INTERVAL_TICKS == 0) {
            List<Settlement> list = new ArrayList<>(settlements.values());
            if (rescanCursor >= list.size()) rescanCursor = 0;
            Settlement s = list.get(rescanCursor);
            rescanCursor++;
            rescan(server, s);
            // Scanning stays round robin because it walks blocks and is the expensive half.
            // Growth is cheap, so every settlement gets its roll every window rather than
            // waiting its turn behind all the others.
            for (Settlement each : list) {
                wanderInRoll(server, each);
            }
        }

        if (tickCounter % PERK_INTERVAL_TICKS == 0) {
            tickCityPerks(server);
        }

        CitizenManager.tick(server);
        SettlementEconomy.tick(server);
        SettlementElectionManager.tick(server);
        HighCouncilManager.tick(server);
        ShadowGovernmentManager.tick(server);
        WarManager.tick(server);
    }

    private static void rescan(MinecraftServer server, Settlement s) {
        ServerLevel world = levelFor(server, s);
        if (world == null) return;
        BlockPos pos = s.cornerstonePos();
        if (!world.hasChunkAt(pos)) return; // area unloaded; keep cached numbers

        try {
            SettlementScanner.ScanResult r = SettlementScanner.scan(world, pos, s.tierEnum());
            applyScan(s, r);
            s.population = CitizenManager.countCitizens(s.id);
            SettlementTier old = s.tierEnum();
            recomputeTier(s);
            SettlementTier now = s.tierEnum();
            if (now != old) {
                broadcastTierChange(server, s, old, now);
            }
            Settlement metro = findNearestMetropolis(s);
            s.nearestMetropolisId = metro != null ? metro.id : null;
            s.lastScanAt = System.currentTimeMillis();
            save();
        } catch (Exception e) {
            System.err.println("[SettlementManager] Scan failed for " + s.name + ": " + e.getMessage());
        }
    }

    /** Force an immediate rescan + tier recompute (used by /settlement scan). */
    public static void rescanNow(ServerLevel world, Settlement s) {
        BlockPos pos = new BlockPos(s.cornerstoneX, s.cornerstoneY, s.cornerstoneZ);
        SettlementScanner.ScanResult r = SettlementScanner.scan(world, pos, s.tierEnum());
        applyScan(s, r);
        s.population = CitizenManager.countCitizens(s.id);
        recomputeTier(s);
        save();
    }

    private static void applyScan(Settlement s, SettlementScanner.ScanResult r) {
        s.beds = r.beds;
        // The scan measures what has been built. The modifier carries what has been done to the
        // place, which is the half a block count can never see.
        s.appeal = Math.max(0, r.appeal + s.appealModifier);
        s.jobSites = r.jobSites;
        s.wonders = r.wonders;
        s.rawBeds = r.rawBeds;
        s.footprintCells = r.footprintCells;
        s.appealByCategory = r.appealByCategory;
    }

    private static void recomputeTier(Settlement s) {
        SettlementTier candidate = SettlementTier.classify(s.beds, s.population, s.appeal, s.treasury, s.wonders);

        // CITY+ requires at least one registered player; otherwise cap at TOWN.
        if (candidate.isAtLeast(SettlementTier.CITY) && s.registered().isEmpty()) {
            candidate = SettlementTier.TOWN;
        }

        // ── Anti-cheese gates ───────────────────────────────────────────────
        // A high tier must be a functioning settlement, not a pile of valuable blocks.
        // Each gate below is something block-spam cannot satisfy.

        // CITY+ needs real infrastructure: workstations to employ people.
        if (candidate.isAtLeast(SettlementTier.CITY) && s.jobSites < 8) {
            candidate = SettlementTier.TOWN;
        }
        // METROPOLIS+ needs a fed, reasonably content population and a developed footprint.
        if (candidate.isAtLeast(SettlementTier.METROPOLIS)) {
            boolean fed = s.population == 0 || s.food >= s.population;
            if (!fed || s.happiness < 50 || s.footprintCells < 8 || s.jobSites < 16) {
                candidate = SettlementTier.CITY;
            }
        }
        // UTOPIA+ demands genuine prosperity and breadth of development.
        if (candidate.isAtLeast(SettlementTier.UTOPIA)) {
            boolean broadAppeal = s.appealByCategory != null && s.appealByCategory.size() >= 4;
            if (s.happiness < 70 || !broadAppeal || s.footprintCells < 20 || s.jobSites < 32) {
                candidate = SettlementTier.MEGALOPOLIS;
            }
        }
        s.setTier(candidate);
    }

    private static void broadcastTierChange(MinecraftServer server, Settlement s, SettlementTier old, SettlementTier now) {
        boolean promoted = now.rank() > old.rank();
        Component msg = Component.literal((promoted ? "▲ " : "▼ ") + s.name + " is now a ")
                .withStyle(promoted ? ChatFormatting.GREEN : ChatFormatting.GRAY)
                .append(Component.literal(now.displayName).withStyle(now.color, ChatFormatting.BOLD))
                .append(Component.literal("!").withStyle(promoted ? ChatFormatting.GREEN : ChatFormatting.GRAY));
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            p.sendSystemMessage(msg);
        }
    }

    private static final java.util.Random RNG = new java.util.Random();

    private static void wanderInRoll(MinecraftServer server, Settlement s) {
        // Growth rolls for every settlement now rather than one per window, which makes this the
        // hot path it was not before. Spawning walks the heightmap and adds an entity, and both of
        // those will drag an unloaded chunk into memory, so a server with fifty settlements would
        // be force loading fifty areas every ten seconds for villagers nobody is there to see.
        ServerLevel check = levelFor(server, s);
        if (check == null || !check.hasChunkAt(s.cornerstonePos())) return;

        // Growth is gated by the economy: free sheltered beds, food surplus and morale.
        // This is what stops a settlement being inflated by dropping decorative blocks.
        if (!SettlementEconomy.canGrow(s)) return;

        int freeBeds = Math.max(0, s.beds - s.population);
        // Probability scales with appeal × free beds, then by settlement happiness.
        // The old curve multiplied two fractions that are both small early on, so a decent
        // starter settlement sat near three percent a roll and took the better part of an hour
        // to reach the five people a village needs. Appeal still matters, it just no longer has
        // to carry the whole equation on its own.
        double appealPart = Math.min(1.0, s.appeal / 220.0);
        double bedPart = Math.min(1.0, freeBeds / 4.0);
        double chance = Math.min(0.9, 0.10 + 0.55 * appealPart * bedPart);
        chance *= SettlementEconomy.growthChance(s);
        if (RNG.nextDouble() < chance) {
            ServerLevel world = levelFor(server, s);
            if (world != null) {
                CitizenManager.spawnWanderInCitizen(world, s);
            }
        }
    }

    /** Small in-radius comfort buff for players standing inside CITY+ settlements. */
    private static void tickCityPerks(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            String dim = ((net.minecraft.server.level.ServerLevel) player.level()).dimension().identifier().toString();
            Settlement s = getContaining(dim, player.blockPosition());
            if (s == null) continue;
            SettlementTier tier = s.tierEnum();
            if (!tier.isAtLeast(SettlementTier.CITY)) continue;

            int amp = tier.isAtLeast(SettlementTier.METROPOLIS) ? 1 : 0;
            // Gentle regen + saturation-free comfort; icons hidden to avoid HUD clutter.
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, amp, true, false, false));
            if (tier.isAtLeast(SettlementTier.MEGALOPOLIS)) {
                player.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 60, 0, true, false, false));
            }
            if (HighCouncilManager.isEdictActive(HighCouncilManager.Edict.GOLDEN_AGE)
                    && tier.isAtLeast(SettlementTier.UTOPIA)) {
                player.addEffect(new MobEffectInstance(MobEffects.LUCK, 60, 0, true, false, false));
            }
        }

        // ELYSIUM server-wide aura: weak regen/luck while any Elysium stands.
        boolean elysiumStands = false;
        for (Settlement s : settlements.values()) {
            if (s.tierEnum() == SettlementTier.ELYSIUM) { elysiumStands = true; break; }
        }
        if (elysiumStands) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.LUCK, 60, 0, true, false, false));
            }
        }
    }
}
