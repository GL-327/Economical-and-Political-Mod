package com.political.power;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.political.core.text.Text;
import com.political.power.formula.Vow;
import com.political.power.model.AbilitySpec;
import com.political.power.model.FailureReason;
import com.political.power.model.PowerCatalog;
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

    @Test
    void everyNewTechniqueAndAbilityHasAName() {
        for (PowerCatalog.TechniqueDef def : PowerCatalog.techniques()) {
            if (def.legacy()) {
                continue;
            }
            assertTrue(Text.keys().contains("political.power.technique." + def.id().toLowerCase(java.util.Locale.ROOT)),
                    "missing technique name for " + def.id());
            for (AbilitySpec spec : PowerCatalog.abilities(def.id())) {
                assertTrue(Text.keys().contains("political.power.ability." + spec.id()), "missing ability name for " + spec.id());
            }
        }
    }

    @Test
    void everyVowHasAName() {
        for (Vow vow : Vow.values()) {
            assertTrue(Text.keys().contains("political.power.vow." + vow.name().toLowerCase(java.util.Locale.ROOT)), "missing vow name for " + vow);
        }
    }
}
