package com.political;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.political.economy.CoinManager;
import com.political.settlement.Settlement;
import com.political.settlement.SettlementManager;
import com.political.settlement.SettlementScanner;
import com.political.settlement.WarManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * V5 Pillar 3 — physical caravans.
 *
 * <p>When {@link TradeRouteManager} dispatches a route, a tagged villager (plus llama escorts)
 * walks the straight line between the two cornerstones over {@link #TRAVEL_TICKS}. The caravan
 * is stepped by direct repositioning rather than pathfinding, so a 5000-block haul costs nothing
 * in AI budget.</p>
 *
 * <h2>Loaded vs. virtual travel</h2>
 * The entity is only materialised while its current position is in a loaded chunk with a player
 * within {@link #RENDER_RADIUS}. Otherwise the caravan keeps travelling "virtually". Virtual legs
 * deliver less ({@link #VIRTUAL_FLOOR} of value at 0% escorted, full value at 100%), which both
 * rewards players who actually live along their trade roads and prevents a fully offline network
 * from paying maximum rates.
 *
 * <h2>Ambush</h2>
 * A materialised caravan can be killed by anything. If a player lands the last hit:
 * <ul>
 *   <li>at war with the caravan's polity — {@link #WAR_PLUNDER_SHARE} of the cargo in coins,
 *       plus a cut to their own settlement treasury;</li>
 *   <li>not at war — only {@link #RAID_PLUNDER_SHARE}, unless they are carrying a
 *       <b>Letter of Marque</b> (dropped by plundering) which is consumed for a better rate;</li>
 *   <li>inside either endpoint's settlement radius — nothing, the town guard recovers the cargo.</li>
 * </ul>
 * Payouts are rate limited per player: a cooldown between plunders and a rolling cap, so this
 * cannot be farmed by camping a road.
 */
public final class CaravanManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(CaravanManager.class);


    private CaravanManager() {}

    public static final String TAG_CARAVAN = "political_caravan";
    public static final String TAG_ESCORT = "political_caravan_escort";
    public static final String TAG_PREFIX = "political_caravan_";

    /** ~3 minutes of travel, i.e. roughly three economy cycles per delivery. */
    public static final int TRAVEL_TICKS = 3600;
    private static final int STEP_INTERVAL = 10;
    private static final double RENDER_RADIUS = 160.0;

    /** Fraction of cargo delivered when the whole haul was virtual. */
    private static final double VIRTUAL_FLOOR = 0.6;

    private static final double WAR_PLUNDER_SHARE = 0.50;
    private static final double WAR_TREASURY_SHARE = 0.30;
    private static final double RAID_PLUNDER_SHARE = 0.20;
    private static final double MARQUE_PLUNDER_SHARE = 0.55;

    private static final long PLUNDER_COOLDOWN_MS = 4L * 60L * 1000L;
    private static final long PLUNDER_WINDOW_MS = 30L * 60L * 1000L;
    private static final int MAX_PLUNDERS_PER_WINDOW = 3;

    private static final double MARQUE_DROP_CHANCE = 0.25;

    // ── State ───────────────────────────────────────────────────────────────

    public static class Caravan {
        public String id;
        public String routeId;
        public String fromId;
        public String toId;
        public String worldDim;
        public String good;
        public long cargoValue;

        public double startX, startY, startZ;
        public double endX, endY, endZ;

        public int progressTicks = 0;
        public int loadedTicks = 0;

        public String villagerUuid;
        public List<String> escortUuids = new ArrayList<>();
        public String lastAttackerUuid;

        public double curX, curY, curZ;

        public List<String> escorts() {
            if (escortUuids == null) escortUuids = new ArrayList<>();
            return escortUuids;
        }

        public double progressFraction() {
            return Math.min(1.0, (double) progressTicks / (double) TRAVEL_TICKS);
        }

        public TradeGood goodEnum() {
            TradeGood g = TradeGood.byName(good);
            return g == null ? TradeGood.GRAIN : g;
        }
    }

    private static final Map<String, Caravan> caravans = new LinkedHashMap<>();
    private static Map<String, Long> lastPlunderMs = new HashMap<>();
    private static Map<String, List<Long>> plunderHistory = new HashMap<>();

    private static Path dataFile;
    private static int tickCounter = 0;

    private static class SaveFile {
        List<Caravan> caravans = new ArrayList<>();
        Map<String, Long> lastPlunderMs = new HashMap<>();
        Map<String, List<Long>> plunderHistory = new HashMap<>();
    }

    // ── Persistence ─────────────────────────────────────────────────────────

    public static void initialize(Path worldRoot) {
        dataFile = worldRoot.resolve("caravans.json");
        caravans.clear();
        lastPlunderMs = new HashMap<>();
        plunderHistory = new HashMap<>();
        if (!Files.exists(dataFile)) return;
        try (Reader r = Files.newBufferedReader(dataFile)) {
            Gson g = new GsonBuilder().create();
            Type t = new TypeToken<SaveFile>() {}.getType();
            SaveFile sf = g.fromJson(r, t);
            if (sf != null) {
                if (sf.caravans != null) {
                    for (Caravan c : sf.caravans) {
                        if (c != null && c.id != null) caravans.put(c.id, c);
                    }
                }
                if (sf.lastPlunderMs != null) lastPlunderMs = new HashMap<>(sf.lastPlunderMs);
                if (sf.plunderHistory != null) plunderHistory = new HashMap<>(sf.plunderHistory);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load", e);
        }
    }

    public static void save() {
        if (dataFile == null) return;
        try {
            Files.createDirectories(dataFile.getParent());
            SaveFile sf = new SaveFile();
            sf.caravans = new ArrayList<>(caravans.values());
            sf.lastPlunderMs = new HashMap<>(lastPlunderMs);
            sf.plunderHistory = new HashMap<>(plunderHistory);
            Gson g = new GsonBuilder().setPrettyPrinting().create();
            try (Writer w = Files.newBufferedWriter(dataFile)) {
                g.toJson(sf, w);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save", e);
        }
    }

    // ── Accessors ───────────────────────────────────────────────────────────

    public static List<Caravan> activeCaravans() {
        return new ArrayList<>(caravans.values());
    }

    public static boolean hasActiveCaravan(String routeId) {
        for (Caravan c : caravans.values()) {
            if (c.routeId.equals(routeId)) return true;
        }
        return false;
    }

    public static List<Caravan> caravansForSettlement(String settlementId) {
        List<Caravan> out = new ArrayList<>();
        if (settlementId == null) return out;
        for (Caravan c : caravans.values()) {
            if (settlementId.equals(c.fromId) || settlementId.equals(c.toId)) out.add(c);
        }
        return out;
    }

    public static void cancelCaravansForRoute(String routeId) {
        List<Caravan> doomed = new ArrayList<>();
        for (Caravan c : caravans.values()) {
            if (c.routeId.equals(routeId)) doomed.add(c);
        }
        for (Caravan c : doomed) {
            despawnEntities(c);
            caravans.remove(c.id);
        }
        if (!doomed.isEmpty()) save();
    }

    // ── Dispatch ────────────────────────────────────────────────────────────

    public static void dispatch(MinecraftServer server, TradeRouteManager.TradeRoute route,
                                Settlement origin, Settlement dest, long cargoValue) {
        if (server == null || route == null || origin == null || dest == null) return;
        if (cargoValue <= 0) return;
        if (hasActiveCaravan(route.id)) return;

        Caravan c = new Caravan();
        c.id = UUID.randomUUID().toString();
        c.routeId = route.id;
        c.fromId = origin.id;
        c.toId = dest.id;
        c.worldDim = origin.worldDim;
        c.good = route.good;
        c.cargoValue = cargoValue;
        c.startX = origin.cornerstoneX + 0.5;
        c.startY = origin.cornerstoneY;
        c.startZ = origin.cornerstoneZ + 0.5;
        c.endX = dest.cornerstoneX + 0.5;
        c.endY = dest.cornerstoneY;
        c.endZ = dest.cornerstoneZ + 0.5;
        c.curX = c.startX;
        c.curY = c.startY;
        c.curZ = c.startZ;
        caravans.put(c.id, c);
        save();

        TradeRouteManager.notifyNear(server, origin, Component.literal("§6✦ §7A caravan departs for §f"
                + dest.name + " §7carrying " + route.goodEnum().colored()
                + " §8(worth ~" + cargoValue + " coins)"));
    }

    // ── Tick ────────────────────────────────────────────────────────────────

    public static void tick(MinecraftServer server) {
        if (server == null) return;
        if (caravans.isEmpty()) return;
        if (++tickCounter < STEP_INTERVAL) return;
        tickCounter = 0;

        for (Caravan c : new ArrayList<>(caravans.values())) {
            try {
                step(server, c);
            } catch (Exception e) {
                LOGGER.error("Caravan step failed", e);
                despawnEntities(c);
                caravans.remove(c.id);
            }
        }
    }

    private static void step(MinecraftServer server, Caravan c) {
        Settlement from = SettlementManager.get(c.fromId);
        Settlement to = SettlementManager.get(c.toId);
        TradeRouteManager.TradeRoute route = TradeRouteManager.get(c.routeId);
        if (from == null || to == null || route == null) {
            despawnEntities(c);
            caravans.remove(c.id);
            save();
            return;
        }

        ServerLevel world = SettlementManager.levelFor(server, from);
        if (world == null) {
            caravans.remove(c.id);
            save();
            return;
        }

        c.progressTicks += STEP_INTERVAL;
        double t = c.progressFraction();
        c.curX = c.startX + (c.endX - c.startX) * t;
        c.curZ = c.startZ + (c.endZ - c.startZ) * t;
        c.curY = c.startY + (c.endY - c.startY) * t;

        BlockPos here = new BlockPos((int) Math.floor(c.curX), (int) Math.floor(c.curY),
                (int) Math.floor(c.curZ));
        boolean chunkLoaded = world.hasChunkAt(here);
        boolean playerNear = chunkLoaded && anyPlayerNear(world, c.curX, c.curY, c.curZ);
        boolean shouldExist = chunkLoaded && playerNear;

        Villager rider = resolveVillager(world, c);

        if (rider != null) {
            if (!rider.isAlive() || rider.isRemoved()) {
                handleDestroyed(server, c, route, from, to, world, lastAttacker(world, c));
                return;
            }
            LivingEntity attacker = rider.getLastHurtByMob();
            if (attacker instanceof ServerPlayer sp) {
                c.lastAttackerUuid = sp.getUUID().toString();
            }
            if (!shouldExist) {
                despawnEntities(c);
            } else {
                c.loadedTicks += STEP_INTERVAL;
                moveEntities(world, c);
            }
        } else if (c.villagerUuid != null) {
            if (chunkLoaded && playerNear) {
                // Gone from a loaded chunk with a player right there — it was killed.
                handleDestroyed(server, c, route, from, to, world, lastAttacker(world, c));
                return;
            }
            // Unresolvable (chunk churn / player left mid-step): fall back to virtual travel,
            // sweeping any stray tagged entity we can still reach so none is orphaned.
            if (chunkLoaded) sweepStrays(world, c);
            c.villagerUuid = null;
            c.escorts().clear();
        } else if (shouldExist) {
            spawnCaravan(world, c, from, to);
            c.loadedTicks += STEP_INTERVAL;
        }

        if (t >= 1.0) {
            arrive(server, c, route, from, to, world);
        }
    }

    private static boolean anyPlayerNear(ServerLevel world, double x, double y, double z) {
        for (ServerPlayer p : world.players()) {
            if (p.distanceToSqr(x, y, z) <= RENDER_RADIUS * RENDER_RADIUS) return true;
        }
        return false;
    }

    private static Villager resolveVillager(ServerLevel world, Caravan c) {
        if (c.villagerUuid == null) return null;
        try {
            Entity e = world.getEntity(UUID.fromString(c.villagerUuid));
            return e instanceof Villager v ? v : null;
        } catch (Exception ex) {
            LOGGER.debug("CaravanManager.resolveVillager: ignored an error", ex);
            return null;
        }
    }

    private static ServerPlayer lastAttacker(ServerLevel world, Caravan c) {
        if (c.lastAttackerUuid == null) {
            // Fallback attribution: whoever is standing closest to the wreck.
            ServerPlayer best = null;
            double bestSq = 20 * 20;
            for (ServerPlayer p : world.players()) {
                double d2 = p.distanceToSqr(c.curX, c.curY, c.curZ);
                if (d2 < bestSq) { bestSq = d2; best = p; }
            }
            return best;
        }
        try {
            return world.getServer().getPlayerList().getPlayer(UUID.fromString(c.lastAttackerUuid));
        } catch (Exception e) {
            LOGGER.debug("CaravanManager.lastAttacker: ignored an error", e);
            return null;
        }
    }

    // ── Entities ────────────────────────────────────────────────────────────

    private static void spawnCaravan(ServerLevel world, Caravan c, Settlement from, Settlement to) {
        BlockPos ground = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                new BlockPos((int) Math.floor(c.curX), (int) Math.floor(c.curY), (int) Math.floor(c.curZ)));

        Villager v = new Villager(EntityTypes.VILLAGER, world);
        v.setPos(ground.getX() + 0.5, ground.getY(), ground.getZ() + 0.5);
        v.setPersistenceRequired();
        v.setNoAi(true);
        v.setCustomName(Component.literal("Caravan — " + from.name + " → " + to.name)
                .withStyle(ChatFormatting.GOLD));
        v.setCustomNameVisible(true);
        v.addTag(TAG_CARAVAN);
        v.addTag(TAG_PREFIX + c.id);
        world.addFreshEntity(v);
        c.villagerUuid = v.getUUID().toString();
        c.curY = ground.getY();

        // Escort strength scales with the two settlements' guard corps.
        int defense = from.defense + to.defense;
        int resist = Math.min(2, defense / 12);
        int boost = Math.min(4, defense / 8);
        int duration = TRAVEL_TICKS + 400;
        if (resist > 0) {
            v.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, duration, resist - 1, true, false, false));
        }
        if (boost > 0) {
            v.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, duration, boost - 1, true, false, false));
            v.setHealth(v.getMaxHealth());
        }

        int escorts = 1 + Math.min(2, TradeRouteManager.routeCapacity(from, to) / 8);
        for (int i = 0; i < escorts; i++) {
            try {
                BlockPos side = ground.offset(i + 1, 0, 0);
                Entity llama = EntityTypes.LLAMA.spawn(world, side, EntitySpawnReason.EVENT);
                if (llama == null) continue;
                llama.addTag(TAG_ESCORT);
                llama.addTag(TAG_PREFIX + c.id);
                llama.setCustomName(Component.literal("Pack Llama").withStyle(ChatFormatting.GRAY));
                if (llama instanceof Mob mob) {
                    mob.setPersistenceRequired();
                    mob.setNoAi(true);
                }
                c.escorts().add(llama.getUUID().toString());
            } catch (Exception ignored) {
                LOGGER.debug("CaravanManager.spawnCaravan: ignored an error", ignored);
            }
        }

        world.playSound(null, ground, SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 0.7f, 1.0f);
        save();
    }

    private static void moveEntities(ServerLevel world, Caravan c) {
        BlockPos ground = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                new BlockPos((int) Math.floor(c.curX), (int) Math.floor(c.curY), (int) Math.floor(c.curZ)));
        double gx = ground.getX() + 0.5;
        double gy = ground.getY();
        double gz = ground.getZ() + 0.5;
        c.curY = gy;

        float yaw = (float) (Math.toDegrees(Math.atan2(c.endZ - c.curZ, c.endX - c.curX)) - 90.0);

        Villager v = resolveVillager(world, c);
        if (v != null) {
            v.setPos(gx, gy, gz);
            v.setYRot(yaw);
            v.setYHeadRot(yaw);
        }
        int i = 1;
        for (String uuid : new ArrayList<>(c.escorts())) {
            Entity e;
            try {
                e = world.getEntity(UUID.fromString(uuid));
            } catch (Exception ex) {
                LOGGER.debug("CaravanManager.moveEntities: ignored an error", ex);
                continue;
            }
            if (e == null || !e.isAlive()) {
                c.escorts().remove(uuid);
                continue;
            }
            double ox = Math.cos(Math.toRadians(yaw)) * i * 1.4;
            double oz = Math.sin(Math.toRadians(yaw)) * i * 1.4;
            e.setPos(gx - ox, gy, gz - oz);
            e.setYRot(yaw);
            i++;
        }

        world.sendParticles(ParticleTypes.HAPPY_VILLAGER, gx, gy + 1.4, gz, 2, 0.3, 0.2, 0.3, 0.01);
    }

    private static void despawnEntities(Caravan c) {
        MinecraftServer server = PoliticalServer.server;
        if (server == null) return;
        Settlement from = SettlementManager.get(c.fromId);
        ServerLevel world = from == null ? null : SettlementManager.levelFor(server, from);
        if (world == null) return;
        if (c.villagerUuid != null) {
            try {
                Entity e = world.getEntity(UUID.fromString(c.villagerUuid));
                if (e != null) e.discard();
            } catch (Exception ignored) {
                LOGGER.debug("CaravanManager.despawnEntities: ignored an error", ignored);
            }
            c.villagerUuid = null;
        }
        for (String uuid : new ArrayList<>(c.escorts())) {
            try {
                Entity e = world.getEntity(UUID.fromString(uuid));
                if (e != null) e.discard();
            } catch (Exception ignored) {
                LOGGER.debug("CaravanManager.despawnEntities: ignored an error", ignored);
            }
        }
        c.escorts().clear();
    }

    /** Discards any entity still carrying this caravan's tag near its current position. */
    private static void sweepStrays(ServerLevel world, Caravan c) {
        String tag = TAG_PREFIX + c.id;
        AABB box = new AABB(c.curX - 48, c.curY - 40, c.curZ - 48,
                c.curX + 48, c.curY + 40, c.curZ + 48);
        try {
            List<Entity> found = world.getEntities((Entity) null, box,
                    e -> e.entityTags().contains(tag));
            for (Entity e : found) e.discard();
        } catch (Exception ignored) {
            LOGGER.debug("CaravanManager.sweepStrays: ignored an error", ignored);
        }
    }

    // ── Arrival ─────────────────────────────────────────────────────────────

    private static void arrive(MinecraftServer server, Caravan c, TradeRouteManager.TradeRoute route,
                               Settlement from, Settlement to, ServerLevel world) {
        despawnEntities(c);
        caravans.remove(c.id);

        double loadedFraction = Math.min(1.0, (double) c.loadedTicks / (double) TRAVEL_TICKS);
        double factor = VIRTUAL_FLOOR + (1.0 - VIRTUAL_FLOOR) * loadedFraction;
        long delivered = Math.round(c.cargoValue * factor);

        TradeRouteManager.onDelivery(server, route, from, to, delivered);

        BlockPos dest = to.cornerstonePos();
        if (world.hasChunkAt(dest)) {
            world.sendParticles(ParticleTypes.HAPPY_VILLAGER, dest.getX() + 0.5, dest.getY() + 1.5,
                    dest.getZ() + 0.5, 20, 0.8, 0.6, 0.8, 0.02);
            world.playSound(null, dest, SoundEvents.VILLAGER_CELEBRATE, SoundSource.NEUTRAL, 0.8f, 1.0f);
        }
        save();
    }

    // ── Destruction / plunder ───────────────────────────────────────────────

    private static void handleDestroyed(MinecraftServer server, Caravan c,
                                        TradeRouteManager.TradeRoute route, Settlement from,
                                        Settlement to, ServerLevel world, ServerPlayer killer) {
        despawnEntities(c);
        caravans.remove(c.id);
        save();

        if (killer == null) {
            TradeRouteManager.onCaravanLost(route, false);
            Component msg = Component.literal("§c✘ §7A caravan from §f" + from.name
                    + " §7was destroyed on the road. Cargo lost.");
            TradeRouteManager.notifyNear(server, from, msg);
            TradeRouteManager.notifyNear(server, to, msg);
            return;
        }

        TradeRouteManager.onCaravanLost(route, true);

        // Town guard: a caravan wrecked inside either settlement is recovered, not plundered.
        if (insideSettlement(from, c) || insideSettlement(to, c)) {
            killer.sendSystemMessage(Component.literal(
                    "§7The town guard recovers the scattered cargo. You gain nothing."));
            return;
        }

        String uuid = killer.getUUID().toString();
        if (!plunderAllowed(uuid)) {
            killer.sendSystemMessage(Component.literal(
                    "§7You have raided too recently — the cargo is ruined in the scuffle."));
            return;
        }

        Settlement killerPolity = WarManager.polityOf(killer);
        String kp = killerPolity == null ? null : killerPolity.id;
        String pFrom = TradeRouteManager.polityIdFor(from);
        String pTo = TradeRouteManager.polityIdFor(to);
        boolean atWar = kp != null
                && ((pFrom != null && !kp.equals(pFrom) && WarManager.warBetween(kp, pFrom) != null)
                || (pTo != null && !kp.equals(pTo) && WarManager.warBetween(kp, pTo) != null));

        double share;
        boolean usedMarque = false;
        if (atWar) {
            share = WAR_PLUNDER_SHARE;
        } else if (TradeItems.consumeOne(killer, TradeItems.ID_MARQUE)) {
            share = MARQUE_PLUNDER_SHARE;
            usedMarque = true;
        } else {
            share = RAID_PLUNDER_SHARE;
        }

        long loot = Math.max(0L, Math.round(c.cargoValue * share));
        if (loot > 0) {
            CoinManager.giveCoinsQuiet(killer, loot);
            recordPlunder(uuid);
        }

        long treasuryCut = 0L;
        if (atWar) {
            Settlement home = SettlementManager.getRegisteredSettlement(uuid);
            if (home != null) {
                treasuryCut = Math.round(c.cargoValue * WAR_TREASURY_SHARE);
                home.treasury += treasuryCut;
                SettlementManager.save();
            }
        }

        killer.sendSystemMessage(Component.literal("⚔ §6Caravan plundered! §7+§6" + loot + " §7coins"
                + (treasuryCut > 0 ? " §8(+" + treasuryCut + " to your settlement treasury)" : "")
                + (usedMarque ? " §8[Letter of Marque spent]" : "")));

        if (!atWar && !usedMarque) {
            killer.sendSystemMessage(Component.literal(
                    "§8Unsanctioned banditry pays badly. A Letter of Marque, or an actual war, pays more."));
        }

        // Marque drops seed the next raid without ever being craftable.
        if (world.getRandom().nextDouble() < MARQUE_DROP_CHANCE) {
            TradeItems.give(killer, TradeItems.createLetterOfMarque());
            killer.sendSystemMessage(Component.literal("§8You pry a §fLetter of Marque §8from the wreck."));
        }

        world.playSound(null, killer.blockPosition(), SoundEvents.VILLAGER_DEATH,
                SoundSource.PLAYERS, 1.0f, 0.8f);

        Component msg = Component.literal("§4⚔ §7A caravan of §f" + from.name
                + " §7was plundered by §c" + killer.getName().getString() + "§7!");
        TradeRouteManager.notifyNear(server, from, msg);
        TradeRouteManager.notifyNear(server, to, msg);
    }

    private static boolean insideSettlement(Settlement s, Caravan c) {
        if (s == null) return false;
        int radius = SettlementScanner.radiusForTier(s.tierEnum());
        double dx = s.cornerstoneX - c.curX;
        double dz = s.cornerstoneZ - c.curZ;
        return dx * dx + dz * dz <= (double) radius * radius;
    }

    // ── Plunder rate limiting ───────────────────────────────────────────────

    private static boolean plunderAllowed(String uuid) {
        long now = System.currentTimeMillis();
        Long last = lastPlunderMs.get(uuid);
        if (last != null && now - last < PLUNDER_COOLDOWN_MS) return false;
        List<Long> hist = plunderHistory.get(uuid);
        if (hist == null) return true;
        hist.removeIf(ts -> now - ts > PLUNDER_WINDOW_MS);
        return hist.size() < MAX_PLUNDERS_PER_WINDOW;
    }

    private static void recordPlunder(String uuid) {
        long now = System.currentTimeMillis();
        lastPlunderMs.put(uuid, now);
        List<Long> hist = plunderHistory.computeIfAbsent(uuid, k -> new ArrayList<>());
        hist.removeIf(ts -> now - ts > PLUNDER_WINDOW_MS);
        hist.add(now);
        save();
    }

    /** Seconds until this player may profitably plunder again (0 if ready). Used by the GUI. */
    public static long plunderCooldownSeconds(String uuid) {
        long now = System.currentTimeMillis();
        Long last = lastPlunderMs.get(uuid);
        long cd = last == null ? 0L : Math.max(0L, PLUNDER_COOLDOWN_MS - (now - last));
        List<Long> hist = plunderHistory.get(uuid);
        if (hist != null) {
            List<Long> live = new ArrayList<>(hist);
            live.removeIf(ts -> now - ts > PLUNDER_WINDOW_MS);
            if (live.size() >= MAX_PLUNDERS_PER_WINDOW) {
                long oldest = live.get(0);
                cd = Math.max(cd, PLUNDER_WINDOW_MS - (now - oldest));
            }
        }
        return cd / 1000L;
    }

    // ── Startup hygiene ─────────────────────────────────────────────────────

    /**
     * Removes caravan entities left behind by a crash whose caravan record no longer exists.
     * Scans a bounded box around every settlement cornerstone, so the cost is proportional to
     * the number of settlements, not the world size.
     */
    public static void cleanupOrphans(MinecraftServer server) {
        if (server == null) return;
        java.util.Set<String> liveTags = new java.util.HashSet<>();
        for (Caravan c : caravans.values()) liveTags.add(TAG_PREFIX + c.id);

        for (Settlement s : SettlementManager.getAllSettlements()) {
            ServerLevel world = SettlementManager.levelFor(server, s);
            if (world == null) continue;
            BlockPos pos = s.cornerstonePos();
            if (!world.hasChunkAt(pos)) continue;
            AABB box = new AABB(pos).inflate(160);
            List<Entity> found = world.getEntities((Entity) null, box,
                    e -> e.entityTags().contains(TAG_CARAVAN) || e.entityTags().contains(TAG_ESCORT));
            for (Entity e : found) {
                boolean live = false;
                for (String tag : e.entityTags()) {
                    if (liveTags.contains(tag)) { live = true; break; }
                }
                if (!live) e.discard();
            }
        }
    }
}
