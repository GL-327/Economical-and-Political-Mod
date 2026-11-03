package com.political.power;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.political.core.text.Text;
import com.political.power.formula.Vow;
import com.political.power.formula.VowSet;
import com.political.power.game.PowerService;
import com.political.power.model.AbilityKind;
import com.political.power.model.AbilitySpec;
import com.political.power.model.FailureReason;
import com.political.power.model.PowerCatalog;
import java.util.Locale;
import org.junit.jupiter.api.Test;

/**
 * Every refusal reason and every answer a screen can show has words for the player. Keys built from an id are
 * invisible to {@code TextPolicyTest}, so this test lists each family by its ids.
 */
class PowerTextTest {

    private static void has(String key) {
        assertTrue(Text.keys().contains(key), "missing text: " + key);
    }

    private static String lower(Enum<?> e) {
        return e.name().toLowerCase(Locale.ROOT);
    }

    @Test
    void everyFailureReasonHasAMessage() {
        for (FailureReason reason : FailureReason.values()) {
            has(reason.messageKey());
        }
        has("political.power.fail.execution_failed");
    }

    @Test
    void everyNewTechniqueAndAbilityHasANameAndADescription() {
        for (PowerCatalog.TechniqueDef def : PowerCatalog.techniques()) {
            if (def.legacy()) {
                continue;
            }
            has("political.power.technique." + def.id().toLowerCase(Locale.ROOT));
            for (AbilitySpec spec : PowerCatalog.abilities(def.id())) {
                assertTrue(spec.kind() != AbilityKind.LEGACY, spec.id());
                has("political.power.ability." + spec.id());
                has("political.power.ability.about." + spec.id());
            }
        }
    }

    @Test
    void everyVowHasANameARuleAndABonusLine() {
        for (Vow vow : Vow.values()) {
            has("political.power.vow." + lower(vow));
            has("political.power.vow.rule." + lower(vow));
            has("political.power.vow.gain." + lower(vow));
        }
    }

    @Test
    void everyAnswerOfTheServiceHasAMessage() {
        for (VowSet.AcceptResult r : VowSet.AcceptResult.values()) {
            has("political.power.vow.result." + lower(r));
        }
        for (PowerService.NodeResult r : PowerService.NodeResult.values()) {
            has("political.power.node.result." + lower(r));
        }
        for (PowerService.SchoolResult r : PowerService.SchoolResult.values()) {
            has("political.power.school.result." + lower(r));
        }
        for (PowerService.LearnResult r : PowerService.LearnResult.values()) {
            has("political.power.learn.result." + lower(r));
        }
    }

    @Test
    void resultMessagesTakeTheNameTheyAreGiven() {
        // the screens always pass one argument, so every template must have a place for it
        for (String key : Text.keys()) {
            if (key.startsWith("political.power.node.result.") || key.startsWith("political.power.school.result.")
                    || key.startsWith("political.power.vow.result.") || key.startsWith("political.power.learn.result.")) {
                assertEquals(1, Text.english(key).split("%s", -1).length - 1, key + " needs exactly one %s");
            }
        }
    }
}
