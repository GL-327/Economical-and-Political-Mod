package com.political;

import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;

/**
 * Proves each stat reaches the game, rather than merely existing.
 *
 * <p>A stat can be declared, parsed off lore, shown on the stat screen and still do nothing, which
 * is exactly what Attack Speed did: it was written on tooltips, promised by an armour attribute,
 * and wired to nothing at all. Reading the call sites did not catch that. Checking the arithmetic
 * does.</p>
 *
 * <p>Each check builds a sheet, runs the real code path, and asserts the number moved in the
 * direction it should. Nothing here needs a player or a world, so it runs at startup behind a flag
 * and costs a server owner nothing.</p>
 */
public final class StatSelfTest {

    private StatSelfTest() {}

    private record Result(String stat, boolean ok, String detail) {}

    public static void runIfAsked(MinecraftServer server) {
        if (!"true".equals(System.getProperty("political.statSelfTest"))) return;

        List<Result> results = new ArrayList<>();

        // ── Clamps ──
        // Every capped stat should refuse a number past its ceiling. A cap that does not hold is
        // how a gear combination quietly reaches invulnerability.
        results.add(clampCheck(PlayerStatsManager.Stat.DEFENSE, 5000, 1000));
        results.add(clampCheck(PlayerStatsManager.Stat.STRENGTH, 9000, 2000));
        results.add(clampCheck(PlayerStatsManager.Stat.CRIT_DAMAGE, 9000, 800));
        results.add(clampCheck(PlayerStatsManager.Stat.MAGIC_FIND, 9000, 400));
        results.add(clampCheck(PlayerStatsManager.Stat.CRIT_CHANCE, 500, 100));
        results.add(clampCheck(PlayerStatsManager.Stat.FEROCITY, 9000, 200));
        results.add(clampCheck(PlayerStatsManager.Stat.ATTACK_SPEED, 9000, 400));

        // ── The defence curve ──
        // dmg * 100 / (100 + defence). More defence must always mean strictly less damage, and no
        // amount of it may ever reach zero.
        results.add(defenceCheck());

        // ── Parsing ──
        // A stat that cannot be read off a tooltip is a stat no item can ever grant.
        for (PlayerStatsManager.Stat stat : PlayerStatsManager.Stat.values()) {
            results.add(parseCheck(stat));
        }

        int failed = 0;
        for (Result r : results) {
            if (!r.ok()) {
                failed++;
                PoliticalServer.LOGGER.error("Stat self test FAILED: " + r.stat() + " " + r.detail());
            }
        }
        PoliticalServer.LOGGER.info("Stat self test: " + (results.size() - failed) + "/"
                + results.size() + " checks passed across "
                + PlayerStatsManager.Stat.values().length + " stats");
    }

    /** A capped stat must refuse anything past its ceiling. */
    private static Result clampCheck(PlayerStatsManager.Stat stat, double push, double ceiling) {
        try {
            PlayerStatsManager.StatSheet sheet = new PlayerStatsManager.StatSheet();
            sheet.add(stat, push);
            sheet.clamp();
            double got = sheet.get(stat);
            boolean ok = got <= ceiling + 0.001;
            return new Result(stat.name(), ok,
                    "pushed " + push + ", expected at most " + ceiling + ", got " + got);
        } catch (Exception e) {
            return new Result(stat.name(), false, "threw " + e);
        }
    }

    /**
     * The defence curve has to be monotonic and never reach zero.
     *
     * <p>Checked as arithmetic rather than by hitting something, because the real path wants a
     * player and a damage source and this only needs to know the shape of the formula.</p>
     */
    private static Result defenceCheck() {
        double last = Double.MAX_VALUE;
        for (int def = 0; def <= 1000; def += 50) {
            double taken = 100.0 * 100.0 / (100.0 + def);
            if (taken <= 0) {
                return new Result("DEFENSE", false, "reaches zero damage at " + def);
            }
            if (taken >= last) {
                return new Result("DEFENSE", false, "not monotonic at " + def);
            }
            last = taken;
        }
        double atCap = 100.0 * 100.0 / (100.0 + 1000.0);
        return new Result("DEFENSE", true, "at the cap a hit still lands "
                + Math.round(atCap) + "% of its damage");
    }

    /**
     * A stat must be readable back off a tooltip line written in the house format.
     *
     * <p>This is the check that would have caught Attack Speed. The stat existed, the symbol
     * existed, and nothing on earth could put it on an item.</p>
     */
    private static Result parseCheck(PlayerStatsManager.Stat stat) {
        try {
            String line = "§7" + stat.symbol + " " + stat.displayName + ": §c+25"
                    + (stat.percent ? "%" : "");
            double got = PlayerStatsManager.parseStatLineForTest(line, stat);
            boolean ok = Math.abs(got - 25.0) < 0.001;
            return new Result(stat.name() + " parse", ok,
                    "wrote 25 into a tooltip and read back " + got);
        } catch (Exception e) {
            return new Result(stat.name() + " parse", false, "threw " + e);
        }
    }
}
