package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Fallout-4-style settlement economy.
 *
 * <p>Citizens assigned to jobs produce output every {@link #CYCLE_TICKS}. Production feeds a
 * closed loop that gates growth, so a settlement can only rise through the tiers by being
 * genuinely built and staffed:</p>
 *
 * <pre>
 *   beds + workstations  ->  settlers arrive  ->  assign jobs
 *        ^                                              |
 *        |                                              v
 *   higher tier   <-  treasury + appeal   <-   food, goods, income
 * </pre>
 *
 * <p><b>Food</b> must cover the population or happiness falls and growth halts.
 * <b>Jobs</b> are limited by scanned workstations, so you cannot employ more citizens than
 * you have built for. <b>Defense</b> from sheriffs/deputies protects against raids.
 * <b>Happiness</b> is the master multiplier on income and the gate on new arrivals.</p>
 */
public final class SettlementEconomy {

    private SettlementEconomy() {}

    /** One production cycle per minute of server time. */
    public static final int CYCLE_TICKS = 1200;

    private static int tickCounter = 0;

    // ── Per-job production profile ──────────────────────────────────────────

    /** What one worker of a given job yields per cycle, before modifiers. */
    public record Yield(double food, double goods, double coins, double defense, double happiness) {}

    private static final Map<Citizen.CitizenJob, Yield> YIELDS = new EnumMap<>(Citizen.CitizenJob.class);

    static {
        //                                            food goods coins defense happy
        YIELDS.put(Citizen.CitizenJob.CITIZEN,   new Yield(0.5, 0.0,   0,    0.0, 0.0));
        YIELDS.put(Citizen.CitizenJob.COURIER,   new Yield(0.0, 0.5,  12,    0.0, 0.0));
        YIELDS.put(Citizen.CitizenJob.MERCHANT,  new Yield(0.0, 1.0,  30,    0.0, 0.2));
        YIELDS.put(Citizen.CitizenJob.PRIEST,    new Yield(0.0, 0.0,   4,    0.0, 1.2));
        YIELDS.put(Citizen.CitizenJob.SCHOLAR,   new Yield(0.0, 1.5,   8,    0.0, 0.4));
        YIELDS.put(Citizen.CitizenJob.BLACKSMITH,new Yield(0.0, 2.0,  18,    0.5, 0.0));
        YIELDS.put(Citizen.CitizenJob.INNKEEPER, new Yield(1.5, 0.0,  10,    0.0, 1.0));
        YIELDS.put(Citizen.CitizenJob.TAX_COLLECTOR, new Yield(0.0, 0.0, 40, 0.0, -0.6));
        YIELDS.put(Citizen.CitizenJob.DEPUTY,    new Yield(0.0, 0.0,   0,    2.0, 0.1));
        YIELDS.put(Citizen.CitizenJob.SHERIFF,   new Yield(0.0, 0.0,   0,    4.0, 0.3));
    }

    /** Farmers are CITIZENs working a composter/field; they are the food backbone. */
    private static final double FOOD_PER_UNEMPLOYED = 0.5;

    // ── Main tick ───────────────────────────────────────────────────────────

    public static void tick(MinecraftServer server) {
        if (server == null) return;
        if (++tickCounter < CYCLE_TICKS) return;
        tickCounter = 0;

        boolean dirty = false;
        for (Settlement s : new ArrayList<>(SettlementManager.getAllSettlements())) {
            if (runCycle(server, s)) dirty = true;
        }
        if (dirty) SettlementManager.save();
    }

    /**
     * The polity whose wars affect this settlement: itself if it is a METROPOLIS+, otherwise
     * the metropolis it follows. Follower settlements share their capital's embargo.
     */
    private static String warPolityIdFor(Settlement s) {
        if (s.tierEnum().isAtLeast(SettlementTier.METROPOLIS)) return s.id;
        return s.nearestMetropolisId != null ? s.nearestMetropolisId : s.id;
    }

    /** Runs one production cycle. Returns true if the settlement changed. */
    private static boolean runCycle(MinecraftServer server, Settlement s) {
        List<Citizen> citizens = CitizenManager.citizensOf(s.id);
        if (citizens.isEmpty()) {
            s.food = 0;
            s.goods = 0;
            s.defense = 0;
            s.happiness = 50;
            return false;
        }

        double food = 0, goods = 0, coins = 0, defense = 0, happy = 0;
        int employed = 0;

        for (Citizen c : citizens) {
            Citizen.CitizenJob job = c.jobEnum();
            Yield y = YIELDS.getOrDefault(job, YIELDS.get(Citizen.CitizenJob.CITIZEN));
            if (job != Citizen.CitizenJob.CITIZEN) employed++;

            food += y.food();
            goods += y.goods();
            coins += y.coins();
            defense += y.defense();
            happy += y.happiness();

            // Unemployed citizens forage: a little food, but no income.
            if (job == Citizen.CitizenJob.CITIZEN) food += FOOD_PER_UNEMPLOYED;
        }

        int pop = citizens.size();

        // ── Happiness: food security, beauty per head, employment, safety ──
        double foodRatio = pop == 0 ? 1.0 : food / pop;
        double appealPerHead = pop == 0 ? 0 : (double) s.appeal / pop;
        double employmentRatio = pop == 0 ? 0 : (double) employed / pop;
        double defenseRatio = pop == 0 ? 1.0 : Math.min(1.0, defense / Math.max(1.0, pop * 0.25));

        double happiness = 40
                + 25 * Math.min(1.0, foodRatio)              // fed
                + 15 * Math.min(1.0, appealPerHead / 12.0)   // beautiful
                + 12 * employmentRatio                       // working
                + 8 * defenseRatio                           // safe
                + happy;                                     // job morale modifiers

        if (foodRatio < 1.0) {
            // Starvation bites hard.
            happiness -= (1.0 - foodRatio) * 45;
        }
        happiness = Math.max(0, Math.min(100, happiness));

        // ── Income: scaled by happiness, boosted by the Prosperity edict ──
        double happinessMult = 0.4 + (happiness / 100.0) * 1.1;   // 0.4x .. 1.5x
        double income = (coins + goods * 6.0) * happinessMult;
        if (HighCouncilManager.isEdictActive(HighCouncilManager.Edict.PROSPERITY)) {
            income *= 1.15;
        }
        // Larger settlements support more trade.
        income *= 1.0 + s.tierEnum().rank() * 0.08;
        // Wartime embargo: commerce suffers while the polity is at war.
        income *= WarManager.tradeMultiplier(warPolityIdFor(s));

        long earned = Math.round(income);

        // ── Fiscal link to national politics ──
        // Settlements remit a share of their earnings to the national government, which flows
        // into the party budgets the Chair and parties actually spend. Tax rate is the national
        // rate set by the Chair, softened by the Tax Reduction edict.
        long remitted = 0L;
        if (earned > 0 && TaxManager.isTaxEnabled() && s.tierEnum().isAtLeast(SettlementTier.TOWN)) {
            double rate = Math.max(0.0, Math.min(0.5, TaxManager.getDailyTaxPercentage() / 100.0));
            remitted = Math.round(earned * rate);
            if (remitted > 0) {
                earned -= remitted;
                PoliticalPartyManager.distributeTaxToParties(
                        (int) Math.min(Integer.MAX_VALUE, remitted));
            }
        }
        s.lastTaxPaid = remitted;
        s.treasury += earned;
        // Seasons modulate the harvest — winter granaries really are thinner.
        s.food = (int) Math.round(SeasonManager.applyFood(food));
        s.goods = (int) Math.round(goods);
        s.defense = (int) Math.round(defense);
        s.happiness = (int) Math.round(happiness);
        s.lastIncome = earned;

        // ── Auto-assign idle citizens to unstaffed workstations ──
        assignJobs(s, citizens);

        // ── Raids: low defense + real wealth invites trouble ──
        maybeRaid(server, s, defense, pop);

        return true;
    }

    /**
     * Employment is bounded by built workstations: you cannot staff more jobs than you have
     * job sites, which is what stops "population" being free. Leadership roles (sheriff,
     * deputy) are capped by population instead.
     */
    private static void assignJobs(Settlement s, List<Citizen> citizens) {
        int jobSlots = s.jobSites;
        int assigned = 0;
        List<Citizen> idle = new ArrayList<>();

        for (Citizen c : citizens) {
            if (c.jobEnum() != Citizen.CitizenJob.CITIZEN) {
                assigned++;
            } else {
                idle.add(c);
            }
        }
        if (idle.isEmpty()) return;

        int pop = citizens.size();
        boolean hasSheriff = citizens.stream().anyMatch(c -> c.jobEnum() == Citizen.CitizenJob.SHERIFF);
        int deputies = (int) citizens.stream().filter(c -> c.jobEnum() == Citizen.CitizenJob.DEPUTY).count();
        int maxDeputies = pop / 12;

        for (Citizen c : idle) {
            // Law enforcement first — a settlement needs order before commerce.
            if (!hasSheriff && pop >= 5) {
                c.setJob(Citizen.CitizenJob.SHERIFF);
                hasSheriff = true;
                continue;
            }
            if (deputies < maxDeputies) {
                c.setJob(Citizen.CitizenJob.DEPUTY);
                deputies++;
                continue;
            }
            if (assigned >= jobSlots) break;   // no free workstation — stays a forager

            // Pick the trade this settlement most lacks.
            c.setJob(pickNeededJob(citizens));
            assigned++;
        }
        CitizenManager.save();
    }

    private static Citizen.CitizenJob pickNeededJob(List<Citizen> citizens) {
        Citizen.CitizenJob[] trades = {
                Citizen.CitizenJob.INNKEEPER,   // food first
                Citizen.CitizenJob.MERCHANT,
                Citizen.CitizenJob.BLACKSMITH,
                Citizen.CitizenJob.SCHOLAR,
                Citizen.CitizenJob.COURIER,
                Citizen.CitizenJob.PRIEST,
                Citizen.CitizenJob.TAX_COLLECTOR,
        };
        EnumMap<Citizen.CitizenJob, Integer> counts = new EnumMap<>(Citizen.CitizenJob.class);
        for (Citizen c : citizens) counts.merge(c.jobEnum(), 1, Integer::sum);

        Citizen.CitizenJob best = trades[0];
        int bestCount = Integer.MAX_VALUE;
        for (Citizen.CitizenJob t : trades) {
            int n = counts.getOrDefault(t, 0);
            if (n < bestCount) {
                bestCount = n;
                best = t;
            }
        }
        return best;
    }

    /** Wealthy, poorly-defended settlements get raided: treasury is stolen, happiness drops. */
    private static void maybeRaid(MinecraftServer server, Settlement s, double defense, int pop) {
        if (pop < 8 || s.treasury < 25_000L) return;
        double needed = pop * 0.25;
        if (defense >= needed) return;

        // Chance scales with how underdefended the settlement is.
        double shortfall = 1.0 - (defense / Math.max(1.0, needed));
        if (Math.random() > shortfall * 0.25) return;

        long stolen = Math.min(s.treasury, Math.round(s.treasury * 0.12));
        s.treasury -= stolen;
        s.happiness = Math.max(0, s.happiness - 15);

        broadcastNear(server, s, Component.literal("⚔ " + s.name + " was raided! ")
                .withStyle(ChatFormatting.RED)
                .append(Component.literal("§7" + stolen + " coins stolen. Appoint more guards.")));
    }

    // ── Growth gate: called by SettlementManager before spawning a settler ──

    /**
     * Whether a new settler may arrive. Growth requires housing, food surplus and morale —
     * this is the main reason a settlement cannot be inflated by dropping blocks.
     */
    /** Below this a settlement feeds itself from outside and does not need a food surplus. */
    public static final int SUBSISTENCE_POP = 6;

    public static boolean canGrow(Settlement s) {
        int pop = s.population;
        if (pop >= s.beds) return false;             // no free sheltered bed
        if (s.happiness < 45) return false;          // miserable settlements do not attract

        // The food gate used to apply from the first citizen, which deadlocked every new
        // settlement: one citizen with no workstation produces no food, so food was always below
        // population and nothing else could ever move in. A hamlet is small enough to live off
        // what its people forage, so the gate only starts once it is genuinely a town's problem.
        if (pop >= SUBSISTENCE_POP && s.food < pop) return false;
        return true;
    }

    /** Why growth is stalled, in the words a player needs, or null when it is not. */
    public static String growthBlocker(Settlement s) {
        if (s.population >= s.beds) {
            return "No free bed. Build and shelter more beds.";
        }
        if (s.happiness < 45) {
            return "Unhappy at " + s.happiness + ". Needs 45.";
        }
        if (s.population >= SUBSISTENCE_POP && s.food < s.population) {
            return "Food " + s.food + " feeds fewer than " + s.population
                    + ". Staff farms or composters.";
        }
        return null;
    }

    /** Growth chance per roll, improved by happiness and the Golden Age edict. */
    public static double growthChance(Settlement s) {
        double base = 0.25 + (s.happiness - 45) / 100.0 * 0.5;
        if (HighCouncilManager.isEdictActive(HighCouncilManager.Edict.GOLDEN_AGE)) base *= 2.0;
        return Math.max(0.05, Math.min(0.9, base));
    }

    // ── Reporting ───────────────────────────────────────────────────────────

    public static void printReport(ServerPlayer player, Settlement s) {
        List<Citizen> citizens = CitizenManager.citizensOf(s.id);
        EnumMap<Citizen.CitizenJob, Integer> counts = new EnumMap<>(Citizen.CitizenJob.class);
        for (Citizen c : citizens) counts.merge(c.jobEnum(), 1, Integer::sum);

        player.sendSystemMessage(Component.literal("§6§l━━━ " + s.name + " — Economy ━━━"));
        player.sendSystemMessage(Component.literal("§7Happiness: " + happinessColor(s.happiness)
                + s.happiness + "%"));
        player.sendSystemMessage(Component.literal("§7Food: §a" + s.food + "§7/§f" + s.population
                + (s.food < s.population ? " §c(STARVING)" : " §8(surplus)")));
        player.sendSystemMessage(Component.literal("§7Goods: §b" + s.goods
                + "  §7Defense: §c" + s.defense));
        player.sendSystemMessage(Component.literal("§7Last cycle income: §6+" + s.lastIncome
                + " §7→ treasury §6" + s.treasury));
        player.sendSystemMessage(Component.literal("§7Workstations: §f" + s.jobSites
                + " §8(caps employment)"));
        player.sendSystemMessage(Component.literal("§7Workforce:"));
        for (Map.Entry<Citizen.CitizenJob, Integer> e : counts.entrySet()) {
            player.sendSystemMessage(Component.literal("  " + e.getKey().color + e.getKey().displayName
                    + "§7: §f" + e.getValue()));
        }
        if (!canGrow(s)) {
            player.sendSystemMessage(Component.literal("§8Growth stalled: "
                    + (s.population >= s.beds ? "no free sheltered beds"
                    : s.happiness < 45 ? "happiness too low"
                    : "not enough food")));
        }
    }

    private static String happinessColor(int h) {
        if (h >= 80) return "§a";
        if (h >= 60) return "§e";
        if (h >= 40) return "§6";
        return "§c";
    }

    private static void broadcastNear(MinecraftServer server, Settlement s, Component msg) {
        ServerLevel world = SettlementManager.levelFor(server, s);
        if (world == null) return;
        for (ServerPlayer p : world.players()) {
            if (p.distanceToSqr(s.cornerstoneX, s.cornerstoneY, s.cornerstoneZ) < 300 * 300) {
                p.sendSystemMessage(msg);
            }
        }
    }
}
