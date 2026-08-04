package com.political;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;

/**
 * Grading a run.
 *
 * <p>Modelled on what a Skyblock score actually rewards rather than on its exact numbers: finding
 * things is worth more than killing things, dying costs a lot, and speed is a tiebreaker rather
 * than the point. A player who explores every room and finds every secret should beat one who ran
 * straight to the boss, and this is the only place that opinion is expressed.</p>
 */
public final class DelveScore {

    private DelveScore() {}

    /** Best to worst. Used for both display and for comparing a new run against a stored best. */
    private static final String[] ORDER = {"S+", "S", "A", "B", "C", "D"};

    public static boolean betterThan(String a, String b) {
        return rank(a) < rank(b);
    }

    private static int rank(String grade) {
        for (int i = 0; i < ORDER.length; i++) {
            if (ORDER[i].equals(grade)) return i;
        }
        return ORDER.length;
    }

    /** The four components, kept separate so the summary can show its working. */
    public record Breakdown(int skill, int exploration, int speed, int bonus, int total, String grade) {}

    /**
     * Scores a finished run out of three hundred.
     *
     * <p>Exploration is worth the most because it is the part a player chooses. Skill is mostly
     * about not dying. Speed exists so two identical clears are not identical, and it is capped low
     * enough that rushing never beats exploring.</p>
     */
    public static Breakdown grade(DelveRun run) {
        // Skill, out of 100. Clearing rooms earns it, dying spends it.
        int cleared = run.roomsTotal <= 0 ? 100
                : Math.round(100f * run.roomsCleared / run.roomsTotal);
        int skill = Math.max(0, cleared - run.deaths * 12);

        // Exploration, out of 100, entirely secrets.
        int exploration = run.secretsTotal <= 0 ? 100
                : Math.round(100f * run.secretsFound / run.secretsTotal);

        // Speed, out of 60. Par is three minutes a floor tier, and there is no reward for beating
        // par by a lot, only for not being slow.
        long parTicks = 20L * 60 * 3 * run.floor.tier;
        long took = run.elapsedTicks(run.endedTick);
        int speed = took <= parTicks ? 60
                : Math.max(0, (int) (60 - (took - parTicks) / (20L * 20)));

        // Bonus, out of 40, for the things that are hard rather than long.
        int bonus = 0;
        if (run.deaths == 0) bonus += 20;
        if (run.secretsTotal > 0 && run.secretsFound >= run.secretsTotal) bonus += 15;
        if (run.bossDead) bonus += 5;

        int total = skill + exploration + speed + bonus;
        String grade = total >= 290 ? "S+"
                : total >= 260 ? "S"
                : total >= 220 ? "A"
                : total >= 170 ? "B"
                : total >= 120 ? "C"
                : "D";
        return new Breakdown(skill, exploration, speed, bonus, total, grade);
    }

    /** What a grade multiplies the floor's base payout by. */
    private static double multiplier(String grade) {
        return switch (grade) {
            case "S+" -> 3.0;
            case "S" -> 2.2;
            case "A" -> 1.6;
            case "B" -> 1.2;
            case "C" -> 0.9;
            default -> 0.6;
        };
    }

    private static String colour(String grade) {
        return switch (grade) {
            case "S+" -> "§6§l";
            case "S" -> "§e§l";
            case "A" -> "§a";
            case "B" -> "§b";
            case "C" -> "§7";
            default -> "§8";
        };
    }

    /**
     * Scores the run, tells everybody, and pays them.
     *
     * <p>The payout is split by share of boss damage with a floor under it, so somebody who spent
     * the fight opening secrets is not paid nothing, and somebody who did all the damage is not
     * paid the same as somebody who stood still.</p>
     */
    public static void award(DelveRun run) {
        Breakdown b = grade(run);
        double mult = multiplier(b.grade());

        float totalDamage = 0;
        for (float v : run.bossDamage.values()) totalDamage += v;

        List<ServerPlayer> here = run.online();
        for (ServerPlayer p : here) {
            float mine = run.bossDamage.getOrDefault(p.getUUID(), 0f);
            double share = totalDamage <= 0 ? 1.0 / Math.max(1, here.size())
                    : mine / totalDamage;
            // Nobody drops below a third of an even split, whatever they did in the boss room.
            double floorShare = (1.0 / Math.max(1, here.size())) / 3.0;
            share = Math.max(share, floorShare);

            long coins = Math.round(run.floor.baseReward * mult * share);
            CoinManager.giveCoins(p, coins);
            DelveManager.recordScore(p.getUUID(), run.floor, b.grade());

            p.sendSystemMessage(Component.literal(""));
            p.sendSystemMessage(Component.literal("  " + run.floor.colour + "§l"
                    + run.floor.display.toUpperCase(java.util.Locale.ROOT) + " §r§7cleared"));
            p.sendSystemMessage(Component.literal("  " + colour(b.grade()) + b.grade()
                    + " §8(" + b.total() + "/300)"));
            p.sendSystemMessage(Component.literal("  §7Skill §f" + b.skill()
                    + "  §7Explore §f" + b.exploration()
                    + "  §7Speed §f" + b.speed()
                    + "  §7Bonus §f" + b.bonus()));
            p.sendSystemMessage(Component.literal("  §7Secrets §f" + run.secretsFound + "/"
                    + run.secretsTotal + "  §7Deaths §f" + run.deaths
                    + "  §7Time §f" + run.elapsedText(run.endedTick)));
            p.sendSystemMessage(Component.literal("  §7Paid §6" + GuiKit.num(coins) + " coins"));

            // A clear rolls the rare table, weighted by how well it went.
            double chance = switch (b.grade()) {
                case "S+" -> 0.9;
                case "S" -> 0.65;
                case "A" -> 0.45;
                case "B" -> 0.3;
                default -> 0.15;
            };
            if (p.getRandom().nextDouble() < chance) {
                ItemStack drop = RareDropManager.rollBossReward(p);
                if (drop != null && !drop.isEmpty()) {
                    if (!p.getInventory().add(drop)) p.drop(drop, false);
                    p.sendSystemMessage(Component.literal("  §6✦ "
                            + drop.getHoverName().getString()));
                }
            }
            p.sendSystemMessage(Component.literal(""));
        }
    }
}
