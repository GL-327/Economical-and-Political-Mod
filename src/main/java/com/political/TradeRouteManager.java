package com.political;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

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
 * V5 Pillar 3 — inter-settlement trade routes.
 *
 * <h2>How a route earns</h2>
 * A route links two settlements and carries one {@link TradeGood}. Every economy cycle
 * ({@link SettlementEconomy#CYCLE_TICKS}, 60s) the route:
 * <ol>
 *   <li>pays <b>upkeep</b> out of BOTH treasuries — always, whether or not anything moved;</li>
 *   <li>if no caravan of its own is already on the road, dispatches one through
 *       {@link CaravanManager} carrying a cargo whose value is fixed at dispatch time.</li>
 * </ol>
 * Coins only reach the treasuries when that caravan <b>arrives</b>, via
 * {@link #onDelivery}. A caravan takes ~3 minutes to walk, so a route pays roughly once
 * every three cycles while bleeding upkeep on all three — this is the core reason routes are a
 * supplement and not a printer.
 *
 * <h2>Where the money goes</h2>
 * Route income is credited to <b>settlement treasuries only</b>. No route ever pays coins into a
 * player's wallet. Treasuries are a tier-gating and war-spoils resource, not spendable currency,
 * so passive trade cannot inflate personal balances at all. The only player-facing payout in this
 * pillar is plundering an enemy caravan, which is rate-limited in {@link CaravanManager}.
 *
 * <h2>War</h2>
 * Route value flows through {@link WarManager#tradeMultiplier} for BOTH polities, exactly like
 * {@link SettlementEconomy}'s own income, and a route between two polities that are directly at
 * war is suspended outright — the embargo is total.
 */
public final class TradeRouteManager {

    private TradeRouteManager() {}

    // ── Tunables ────────────────────────────────────────────────────────────

    /** Aligned with the settlement economy so trade and production share a heartbeat. */
    public static final int CYCLE_TICKS = SettlementEconomy.CYCLE_TICKS;

    /** Hard ceiling on the coin value a single caravan may ever carry. */
    public static final long MAX_DELIVERY_VALUE = 1200L;

    /** Hard ceiling on what one settlement's whole route network may bank per cycle. */
    public static final long MAX_TREASURY_GAIN_PER_CYCLE = 2400L;

    private static final long ESTABLISH_BASE_COST = 25_000L;
    private static final long ESTABLISH_COST_PER_BLOCK = 20L;
    private static final long ESTABLISH_MAX_COST = 250_000L;

    private static final double MAX_ROUTE_DISTANCE = 6000.0;

    /** A Merchant's Seal boosts a settlement's delivered value for this long. */
    public static final long SEAL_DURATION_MS = 24L * 60L * 60L * 1000L;
    public static final double SEAL_BONUS = 0.12;

    // ── State ───────────────────────────────────────────────────────────────

    public static class TradeRoute {
        public String id;
        public String aId;
        public String bId;
        public String good = TradeGood.GRAIN.name();
        public String founderUuid;
        public long establishedAtMs;

        public boolean suspended = false;
        public String suspendReason = "";

        public int deliveries = 0;
        public long totalEarnedA = 0L;
        public long totalEarnedB = 0L;
        public long totalUpkeepPaid = 0L;
        public long lastDeliveryMs = 0L;
        public long lastCargoValue = 0L;
        public int plundered = 0;
        public int lost = 0;

        public TradeGood goodEnum() {
            TradeGood g = TradeGood.byName(good);
            return g == null ? TradeGood.GRAIN : g;
        }

        public boolean involves(String settlementId) {
            return aId.equals(settlementId) || bId.equals(settlementId);
        }

        public String other(String settlementId) {
            return aId.equals(settlementId) ? bId : aId;
        }
    }

    private static final Map<String, TradeRoute> routes = new LinkedHashMap<>();
    /** settlementId -> epoch ms at which its Merchant's Seal expires. */
    private static Map<String, Long> seals = new HashMap<>();
    /** settlementId -> coins banked from trade in the current cycle (reset each cycle). */
    private static final Map<String, Long> cycleGains = new HashMap<>();

    private static Path dataFile;
    private static int tickCounter = 0;

    private static class SaveFile {
        List<TradeRoute> routes = new ArrayList<>();
        Map<String, Long> seals = new HashMap<>();
    }

    // ── Persistence ─────────────────────────────────────────────────────────

    public static void initialize(Path worldRoot) {
        dataFile = worldRoot.resolve("trade_routes.json");
        routes.clear();
        seals = new HashMap<>();
        if (!Files.exists(dataFile)) return;
        try (Reader r = Files.newBufferedReader(dataFile)) {
            Gson g = new GsonBuilder().create();
            Type t = new TypeToken<SaveFile>() {}.getType();
            SaveFile sf = g.fromJson(r, t);
            if (sf != null) {
                if (sf.routes != null) {
                    for (TradeRoute route : sf.routes) {
                        if (route != null && route.id != null) routes.put(route.id, route);
                    }
                }
                if (sf.seals != null) seals = new HashMap<>(sf.seals);
            }
        } catch (Exception e) {
            System.err.println("[TradeRouteManager] Failed to load: " + e.getMessage());
        }
    }

    public static void save() {
        if (dataFile == null) return;
        try {
            Files.createDirectories(dataFile.getParent());
            SaveFile sf = new SaveFile();
            sf.routes = new ArrayList<>(routes.values());
            sf.seals = new HashMap<>(seals);
            Gson g = new GsonBuilder().setPrettyPrinting().create();
            try (Writer w = Files.newBufferedWriter(dataFile)) {
                g.toJson(sf, w);
            }
        } catch (Exception e) {
            System.err.println("[TradeRouteManager] Failed to save: " + e.getMessage());
        }
    }

    // ── Accessors ───────────────────────────────────────────────────────────

    public static List<TradeRoute> allRoutes() {
        return new ArrayList<>(routes.values());
    }

    public static TradeRoute get(String id) {
        return id == null ? null : routes.get(id);
    }

    public static List<TradeRoute> routesOf(String settlementId) {
        List<TradeRoute> out = new ArrayList<>();
        if (settlementId == null) return out;
        for (TradeRoute r : routes.values()) {
            if (r.involves(settlementId)) out.add(r);
        }
        return out;
    }

    // ── Tier limits ─────────────────────────────────────────────────────────

    /** Units of cargo a settlement of this tier can put on one caravan. */
    public static int capacityForTier(SettlementTier tier) {
        return switch (tier) {
            case OUTPOST, HAMLET -> 0;
            case VILLAGE -> 2;
            case TOWN -> 4;
            case CITY -> 7;
            case METROPOLIS -> 11;
            case MEGALOPOLIS -> 16;
            case UTOPIA -> 22;
            case ELYSIUM -> 30;
        };
    }

    /** How many routes a settlement of this tier may hold at once. */
    public static int maxRoutesForTier(SettlementTier tier) {
        return switch (tier) {
            case OUTPOST, HAMLET -> 0;
            case VILLAGE, TOWN -> 1;
            case CITY -> 2;
            case METROPOLIS -> 3;
            case MEGALOPOLIS -> 4;
            case UTOPIA -> 5;
            case ELYSIUM -> 6;
        };
    }

    /** Effective per-caravan capacity: the smaller of the two endpoints. */
    public static int routeCapacity(Settlement a, Settlement b) {
        if (a == null || b == null) return 0;
        return Math.min(capacityForTier(a.tierEnum()), capacityForTier(b.tierEnum()));
    }

    // ── War / polity plumbing ───────────────────────────────────────────────

    /**
     * Mirrors {@code SettlementEconomy.warPolityIdFor}: a METROPOLIS+ settlement is its own
     * polity, everyone else follows the metropolis they are nearest to.
     */
    public static String polityIdFor(Settlement s) {
        if (s == null) return null;
        if (s.tierEnum().isAtLeast(SettlementTier.METROPOLIS)) return s.id;
        return s.nearestMetropolisId != null ? s.nearestMetropolisId : s.id;
    }

    /** True if the two settlements' polities are directly at war — a total embargo. */
    public static boolean embargoed(Settlement a, Settlement b) {
        String pa = polityIdFor(a);
        String pb = polityIdFor(b);
        if (pa == null || pb == null || pa.equals(pb)) return false;
        return WarManager.warBetween(pa, pb) != null;
    }

    /** Combined wartime commerce penalty for both ends of a route. */
    public static double warMultiplier(Settlement a, Settlement b) {
        return WarManager.tradeMultiplier(polityIdFor(a)) * WarManager.tradeMultiplier(polityIdFor(b));
    }

    // ── Economics ───────────────────────────────────────────────────────────

    public static double distanceOf(TradeRoute r) {
        Settlement a = SettlementManager.get(r.aId);
        Settlement b = SettlementManager.get(r.bId);
        if (a == null || b == null) return 0;
        return Math.sqrt(a.distanceSqTo(b));
    }

    /** Coins each treasury pays every cycle to keep this route open. */
    public static long upkeepPerCycle(TradeRoute r) {
        Settlement a = SettlementManager.get(r.aId);
        Settlement b = SettlementManager.get(r.bId);
        int cap = routeCapacity(a, b);
        return 40L + 10L * cap;
    }

    /** 0..100 chance-flavoured risk rating shown in the GUI and used for escort strength. */
    public static int riskOf(TradeRoute r) {
        Settlement a = SettlementManager.get(r.aId);
        Settlement b = SettlementManager.get(r.bId);
        if (a == null || b == null) return 100;
        double risk = distanceOf(r) / 40.0;
        if (WarManager.isAtWar(polityIdFor(a)) || WarManager.isAtWar(polityIdFor(b))) risk += 35;
        if (a.happiness < 45 || b.happiness < 45) risk += 10;
        risk -= Math.min(25.0, (a.defense + b.defense) * 0.5);
        return (int) Math.max(0, Math.min(100, Math.round(risk)));
    }

    /**
     * The cargo value a caravan would carry from {@code from} to {@code to} right now, after
     * war multipliers, seals and the hard per-delivery cap. Zero if the route is unprofitable.
     */
    public static long projectedCargoValue(TradeRoute r, Settlement from, Settlement to) {
        if (from == null || to == null) return 0L;
        if (embargoed(from, to)) return 0L;

        TradeGood good = r.goodEnum();
        int margin = good.marginBetween(from, to);
        if (margin <= 0) return 0L;

        int cap = routeCapacity(from, to);
        if (cap <= 0) return 0L;

        double value = (double) margin * cap;
        // Long hauls are worth more, but with heavily diminishing returns.
        double dist = distanceOf(r);
        value *= 1.0 + Math.min(1.0, dist / 2000.0);
        value *= warMultiplier(from, to);
        if (hasSeal(from.id) || hasSeal(to.id)) value *= 1.0 + SEAL_BONUS;

        long v = Math.round(value);
        return Math.min(MAX_DELIVERY_VALUE, Math.max(0L, v));
    }

    /** Direction the goods actually flow this cycle: from the cheap end to the dear end. */
    public static Settlement originOf(TradeRoute r) {
        Settlement a = SettlementManager.get(r.aId);
        Settlement b = SettlementManager.get(r.bId);
        if (a == null || b == null) return a;
        TradeGood good = r.goodEnum();
        return good.priceAt(a) <= good.priceAt(b) ? a : b;
    }

    public static Settlement destinationOf(TradeRoute r) {
        Settlement origin = originOf(r);
        if (origin == null) return null;
        return SettlementManager.get(origin.id.equals(r.aId) ? r.bId : r.aId);
    }

    // ── Seals ───────────────────────────────────────────────────────────────

    public static boolean hasSeal(String settlementId) {
        Long until = seals.get(settlementId);
        return until != null && until > System.currentTimeMillis();
    }

    public static long sealRemainingMs(String settlementId) {
        Long until = seals.get(settlementId);
        if (until == null) return 0L;
        return Math.max(0L, until - System.currentTimeMillis());
    }

    public static void applySeal(String settlementId) {
        seals.put(settlementId, System.currentTimeMillis() + SEAL_DURATION_MS);
        save();
    }

    // ── Establishing / cancelling ───────────────────────────────────────────

    public static long establishCost(Settlement a, Settlement b) {
        if (a == null || b == null) return ESTABLISH_MAX_COST;
        double dist = Math.sqrt(a.distanceSqTo(b));
        long cost = ESTABLISH_BASE_COST + Math.round(dist * ESTABLISH_COST_PER_BLOCK);
        return Math.min(ESTABLISH_MAX_COST, Math.max(ESTABLISH_BASE_COST, cost));
    }

    /** Human-readable reason the player may NOT open this route, or null if they may. */
    public static String blockReason(ServerPlayer player, Settlement a, Settlement b, TradeGood good) {
        if (a == null || b == null || good == null) return "Pick two settlements and a good.";
        if (a.id.equals(b.id)) return "A route needs two different settlements.";
        if (!a.worldDim.equals(b.worldDim)) return "Both settlements must be in the same dimension.";
        if (capacityForTier(a.tierEnum()) <= 0 || capacityForTier(b.tierEnum()) <= 0) {
            return "Both ends must be a VILLAGE or larger.";
        }
        if (Math.sqrt(a.distanceSqTo(b)) > MAX_ROUTE_DISTANCE) {
            return "Too far apart — the road would never be walked.";
        }
        String uuid = player.getUUID().toString();
        boolean member = a.registered().contains(uuid) || b.registered().contains(uuid)
                || uuid.equals(a.ownerUuid) || uuid.equals(b.ownerUuid);
        if (!member && !SpawnProtectionManager.isOperator(player)) {
            return "You must be registered to one of the two settlements.";
        }
        if (routesOf(a.id).size() >= maxRoutesForTier(a.tierEnum())) {
            return a.name + " has no free route slots (" + maxRoutesForTier(a.tierEnum()) + ").";
        }
        if (routesOf(b.id).size() >= maxRoutesForTier(b.tierEnum())) {
            return b.name + " has no free route slots (" + maxRoutesForTier(b.tierEnum()) + ").";
        }
        for (TradeRoute r : routes.values()) {
            if (r.involves(a.id) && r.involves(b.id) && r.goodEnum() == good) {
                return "That good already moves between these two.";
            }
        }
        if (embargoed(a, b)) return "Their polities are at war — no charter will be honoured.";
        return null;
    }

    /**
     * Opens a route. Costs coins from the founder AND consumes one Caravan Charter, so route
     * creation is gated by a craft as well as by a coin sink.
     */
    public static boolean establish(ServerPlayer player, Settlement a, Settlement b, TradeGood good) {
        String reason = blockReason(player, a, b, good);
        if (reason != null) {
            player.sendSystemMessage(Component.literal("§c" + reason));
            return false;
        }
        if (!TradeItems.consumeOne(player, TradeItems.ID_CHARTER)) {
            player.sendSystemMessage(Component.literal(
                    "§cYou need a §fCaravan Charter §cto open a route. §8(craftable — see /recipe)"));
            return false;
        }
        long cost = establishCost(a, b);
        if (!CoinManager.removeCoins(player, cost)) {
            // Give the charter back — we already took it.
            TradeItems.give(player, TradeItems.createCharter());
            player.sendSystemMessage(Component.literal("§cThe charter costs §6"
                    + String.format("%,d", cost) + " §ccoins to file."));
            return false;
        }

        TradeRoute r = new TradeRoute();
        r.id = UUID.randomUUID().toString();
        r.aId = a.id;
        r.bId = b.id;
        r.good = good.name();
        r.founderUuid = player.getUUID().toString();
        r.establishedAtMs = System.currentTimeMillis();
        routes.put(r.id, r);
        save();

        player.sendSystemMessage(Component.literal("✦ Trade route opened: §f" + a.name
                + " §7⇄ §f" + b.name + " §8(" + good.colored() + "§8)").withStyle(
                net.minecraft.ChatFormatting.GOLD));
        player.sendSystemMessage(Component.literal("§7Upkeep §6" + upkeepPerCycle(r)
                + " §7coins per cycle from each treasury. Risk: §c" + riskOf(r) + "%"));
        return true;
    }

    public static boolean cancel(ServerPlayer player, TradeRoute r) {
        if (r == null) return false;
        Settlement a = SettlementManager.get(r.aId);
        Settlement b = SettlementManager.get(r.bId);
        String uuid = player.getUUID().toString();
        boolean allowed = uuid.equals(r.founderUuid) || SpawnProtectionManager.isOperator(player)
                || (a != null && uuid.equals(a.ownerUuid))
                || (b != null && uuid.equals(b.ownerUuid));
        if (!allowed) {
            player.sendSystemMessage(Component.literal(
                    "§cOnly the founder or a settlement owner may close this route."));
            return false;
        }
        routes.remove(r.id);
        CaravanManager.cancelCaravansForRoute(r.id);
        save();
        player.sendSystemMessage(Component.literal("§7Route closed. No refund — the charter is spent."));
        return true;
    }

    // ── Cycle ───────────────────────────────────────────────────────────────

    public static void tick(MinecraftServer server) {
        if (server == null) return;
        if (++tickCounter < CYCLE_TICKS) return;
        tickCounter = 0;
        runCycle(server);
    }

    private static void runCycle(MinecraftServer server) {
        if (routes.isEmpty()) return;
        cycleGains.clear();
        boolean dirty = false;

        for (TradeRoute r : new ArrayList<>(routes.values())) {
            Settlement a = SettlementManager.get(r.aId);
            Settlement b = SettlementManager.get(r.bId);
            if (a == null || b == null) {
                routes.remove(r.id);
                CaravanManager.cancelCaravansForRoute(r.id);
                dirty = true;
                continue;
            }

            // 1) Upkeep is unconditional — an idle route is a losing route.
            long upkeep = upkeepPerCycle(r);
            if (a.treasury < upkeep || b.treasury < upkeep) {
                if (!r.suspended) {
                    r.suspended = true;
                    r.suspendReason = "Treasury cannot meet upkeep";
                    dirty = true;
                }
                continue;
            }
            a.treasury -= upkeep;
            b.treasury -= upkeep;
            r.totalUpkeepPaid += upkeep * 2;
            dirty = true;

            // 2) Embargo check.
            if (embargoed(a, b)) {
                r.suspended = true;
                r.suspendReason = "Embargoed — the two polities are at war";
                CaravanManager.cancelCaravansForRoute(r.id);
                continue;
            }
            if (r.suspended) {
                r.suspended = false;
                r.suspendReason = "";
            }

            // 3) Dispatch, but never more than one caravan per route at a time.
            if (CaravanManager.hasActiveCaravan(r.id)) continue;

            Settlement origin = originOf(r);
            Settlement dest = destinationOf(r);
            if (origin == null || dest == null) continue;
            long cargo = projectedCargoValue(r, origin, dest);
            if (cargo <= 0L) continue;

            r.lastCargoValue = cargo;
            CaravanManager.dispatch(server, r, origin, dest, cargo);
        }

        if (dirty) {
            SettlementManager.save();
            save();
        }
    }

    /**
     * Called by {@link CaravanManager} when a caravan reaches its destination. Splits the value
     * between the two treasuries, subject to the per-settlement per-cycle ceiling.
     */
    public static void onDelivery(MinecraftServer server, TradeRoute r, Settlement origin,
                                  Settlement dest, long deliveredValue) {
        if (r == null || origin == null || dest == null || deliveredValue <= 0) return;

        long half = Math.max(1L, deliveredValue / 2L);
        long toOrigin = grantCapped(origin, half);
        long toDest = grantCapped(dest, deliveredValue - half);

        if (origin.id.equals(r.aId)) {
            r.totalEarnedA += toOrigin;
            r.totalEarnedB += toDest;
        } else {
            r.totalEarnedB += toOrigin;
            r.totalEarnedA += toDest;
        }
        r.deliveries++;
        r.lastDeliveryMs = System.currentTimeMillis();

        SettlementManager.save();
        save();

        notifyNear(server, origin, Component.literal("§2✦ §7Caravan arrived at §f" + dest.name
                + "§7: §6+" + (toOrigin + toDest) + " §7split between treasuries."));
        notifyNear(server, dest, Component.literal("§2✦ §7Caravan from §f" + origin.name
                + " §7unloaded §f" + r.goodEnum().colored() + "§7: §6+" + toDest + " §7to the treasury."));
    }

    /** Applies the per-settlement, per-cycle passive ceiling. Returns what was actually banked. */
    private static long grantCapped(Settlement s, long amount) {
        if (amount <= 0) return 0L;
        long already = cycleGains.getOrDefault(s.id, 0L);
        long room = MAX_TREASURY_GAIN_PER_CYCLE - already;
        if (room <= 0) return 0L;
        long granted = Math.min(room, amount);
        s.treasury += granted;
        cycleGains.put(s.id, already + granted);
        return granted;
    }

    /** Called by {@link CaravanManager} when a caravan is destroyed or plundered. */
    public static void onCaravanLost(TradeRoute r, boolean plundered) {
        if (r == null) return;
        if (plundered) r.plundered++;
        else r.lost++;
        save();
    }

    static void notifyNear(MinecraftServer server, Settlement s, Component msg) {
        if (server == null || s == null) return;
        net.minecraft.server.level.ServerLevel world = SettlementManager.levelFor(server, s);
        if (world == null) return;
        for (ServerPlayer p : world.players()) {
            if (p.distanceToSqr(s.cornerstoneX, s.cornerstoneY, s.cornerstoneZ) < 240 * 240) {
                p.sendSystemMessage(msg);
            }
        }
    }
}
