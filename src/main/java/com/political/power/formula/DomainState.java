package com.political.power.formula;

import com.political.power.PowerSettings;

/**
 * The life of one Domain, on server ticks: it lasts the configured duration, and its cooldown starts when
 * it ends, whether it ran out or was ended early (death, logout, a change of dimension). Until the
 * cooldown is over the caster cannot cast another.
 */
public final class DomainState {

    private final long startTick;
    private long endTick;
    private long cooldownEndTick;

    private DomainState(long startTick, long endTick, long cooldownEndTick) {
        this.startTick = startTick;
        this.endTick = endTick;
        this.cooldownEndTick = cooldownEndTick;
    }

    /** A Domain cast at {@code nowTick}. */
    public static DomainState cast(long nowTick) {
        long end = nowTick + PowerSettings.DOMAIN_DURATION_TICKS.get();
        return new DomainState(nowTick, end, end + PowerSettings.DOMAIN_COOLDOWN_TICKS.get());
    }

    public long startTick() {
        return startTick;
    }

    public long endTick() {
        return endTick;
    }

    public boolean isActive(long nowTick) {
        return nowTick >= startTick && nowTick < endTick;
    }

    /** Ends the Domain now and starts its cooldown from this moment. Does nothing if it already ended. */
    public void endEarly(long nowTick) {
        if (isActive(nowTick)) {
            endTick = nowTick;
            cooldownEndTick = nowTick + PowerSettings.DOMAIN_COOLDOWN_TICKS.get();
        }
    }

    /** Ticks until the caster may cast a Domain again, counting the time the Domain itself still has to run. */
    public long cooldownRemaining(long nowTick) {
        return Math.max(0L, cooldownEndTick - nowTick);
    }
}
