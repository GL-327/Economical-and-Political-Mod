package com.political.core.tick;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Where the dispatcher reports trouble. Tests record the calls; the server logs them. */
public interface TickReporter {

    /**
     * A service threw. {@code suppressed} is how many earlier failures of the same service were not
     * reported because they fell inside the throttle window.
     */
    void failed(String service, String where, Throwable error, long suppressed);

    /** A service used more than its budget in one tick; {@code suppressed} as for {@link #failed}. */
    void overBudget(String service, double tookMillis, double budgetMillis, long suppressed);

    /** Reports through SLF4J under the {@code TickServices} logger. */
    static TickReporter logging() {
        Logger logger = LoggerFactory.getLogger("TickServices");
        return new TickReporter() {
            @Override
            public void failed(String service, String where, Throwable error, long suppressed) {
                if (suppressed > 0) {
                    logger.error("Tick service {} failed{} ({} similar failures were not logged)", service, where, suppressed, error);
                } else {
                    logger.error("Tick service {} failed{}", service, where, error);
                }
            }

            @Override
            public void overBudget(String service, double tookMillis, double budgetMillis, long suppressed) {
                logger.warn("Tick service {} took {} ms, budget {} ms ({} earlier overruns not logged)",
                        service, String.format("%.2f", tookMillis), String.format("%.2f", budgetMillis), suppressed);
            }
        };
    }
}
