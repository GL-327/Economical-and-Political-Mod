package com.political.power;

import com.political.core.config.Config;
import com.political.core.config.ConfigCatalog;
import com.political.core.config.ConfigStore;
import com.political.power.formula.UniqueHolders;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class UniqueHoldersTest {
    @TempDir Path directory;
    @BeforeEach @AfterEach void defaults() { Config.reset(); }

    private void uniqueBlue() throws Exception {
        Path config = directory.resolve("unique.json");
        Files.writeString(config, "{\"power\":{\"unique\":{\"blue\":true}}}");
        Config.useForTests(ConfigStore.load(config, ConfigCatalog.keys()));
    }

    @Test void aUniqueTechniqueHasOnlyOneHolderUntilItsOwnerReleasesIt() throws Exception {
        uniqueBlue();
        Map<String, String> holders = new HashMap<>();
        assertEquals(UniqueHolders.Claim.CLAIMED, UniqueHolders.claim(holders, "BLUE", "first"));
        assertEquals(UniqueHolders.Claim.ALREADY_YOURS, UniqueHolders.claim(holders, "BLUE", "first"));
        assertEquals(UniqueHolders.Claim.TAKEN, UniqueHolders.claim(holders, "BLUE", "second"));
        assertFalse(UniqueHolders.release(holders, "BLUE", "second"));
        assertEquals("first", holders.get("BLUE"));
        assertTrue(UniqueHolders.release(holders, "BLUE", "first"));
        assertEquals(UniqueHolders.Claim.CLAIMED, UniqueHolders.claim(holders, "BLUE", "second"));
    }

    @Test void aDisabledUniqueRuleDoesNotReserveTheTechnique() {
        Map<String, String> holders = new HashMap<>();
        assertEquals(UniqueHolders.Claim.NOT_UNIQUE, UniqueHolders.claim(holders, "BLUE", "first"));
        assertEquals(UniqueHolders.Claim.NOT_UNIQUE, UniqueHolders.claim(holders, "BLUE", "second"));
        assertTrue(holders.isEmpty());
    }

    @Test void otherTechniqueRulesAreIndependentOfTheEnabledUniqueRule() throws Exception {
        uniqueBlue();
        Map<String, String> holders = new HashMap<>();
        UniqueHolders.claim(holders, "BLUE", "first");
        assertEquals(UniqueHolders.Claim.NOT_UNIQUE, UniqueHolders.claim(holders, "RED", "second"));
        assertEquals(Map.of("BLUE", "first"), holders);
    }
}
