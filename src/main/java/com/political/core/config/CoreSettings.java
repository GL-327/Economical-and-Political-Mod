package com.political.core.config;

/** Settings of the core infrastructure (M1). Gameplay settings get their own holder in their milestone. */
public final class CoreSettings {

    private CoreSettings() {
    }

    public static final ConfigKey<Double> TICK_BUDGET_SERVER_MS = ConfigKey.decimal(
            "tick.budget.server_ms", 5.0, 0.1, 1000.0,
            "How long one server or world tick service may take in a single tick before the server logs it by name. "
                    + "Nothing is cut short; this only decides when a line is logged (at most once a minute per service).");

    public static final ConfigKey<Double> TICK_BUDGET_PLAYER_MS = ConfigKey.decimal(
            "tick.budget.player_ms", 10.0, 0.1, 1000.0,
            "The same limit for a per-player tick service, counted over all players in that tick.");

    public static final ConfigKey<Integer> PERSISTENCE_FLUSH_INTERVAL_TICKS = ConfigKey.integer(
            "persistence.flush_interval_ticks", 600, 20, 72_000,
            "How often modified data stores are written to disk, in ticks (20 ticks = 1 second). Stores are also written when the server stops.");
}
