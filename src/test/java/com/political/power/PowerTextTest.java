package com.political.power;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.political.core.text.Text;
import com.political.power.model.FailureReason;
import org.junit.jupiter.api.Test;

/** Every refusal reason has words for the player. */
class PowerTextTest {

    @Test
    void everyFailureReasonHasAMessage() {
        for (FailureReason reason : FailureReason.values()) {
            assertTrue(Text.keys().contains(reason.messageKey()), "missing text for " + reason.messageKey());
        }
        assertTrue(Text.keys().contains("political.power.fail.execution_failed"));
    }
}
