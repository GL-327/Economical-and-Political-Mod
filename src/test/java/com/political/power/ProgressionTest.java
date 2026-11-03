package com.political.power;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.political.SchoolTree;
import com.political.core.config.Config;
import com.political.core.config.ConfigCatalog;
import com.political.core.config.ConfigStore;
import com.political.magic.MagicSchool;
import com.political.power.formula.Progression;
import com.political.power.formula.SchoolPassives;
import com.political.power.formula.UniqueHolders;
import com.political.power.model.AbilityKind;
import com.political.power.model.AbilitySpec;
import com.political.power.model.PowerCatalog;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Levels, school passives, unique techniques and the technique catalog. */
class ProgressionTest {

    @TempDir
    Path dir;

    @AfterEach
    void restore() {
        Config.reset();
    }

    // ── levels ──────────────────────────────────────────────────────────────────────────────

    @Test
    void levelFollowsShardsEarnedAndStopsAtFifty() {
        assertEquals(1, Progression.levelFor(0));
        assertEquals(1, Progression.levelFor(2));
        assertEquals(2, Progression.levelFor(3));
        assertEquals(14, Progression.levelFor(40));
        assertEquals(50, Progression.levelFor(147));
        assertEquals(50, Progression.levelFor(10_000));
        assertEquals(1, Progression.levelFor(-9));
        assertEquals(147, Progression.shardsForLevel(50));
        assertEquals(0, Progression.shardsForLevel(1));
    }

    @Test
    void theWholeSpineIsReachableWithTheShardsItCostsPlusTheLevelWall() {
        // Node 10 of every school needs level 50 and 30 shards; the nine nodes before it cost 108 shards in all.
        MagicSchool school = MagicSchool.GRAVITAS;
        int spineCost = 0;
        int levelNeeded = 1;
        for (int n = 1; n <= 10; n++) {
            SchoolTree.SchoolNode node = SchoolTree.getNode(school.prefix + n);
            spineCost += node.shardCost();
            levelNeeded = Math.max(levelNeeded, node.levelRequired());
        }
        assertEquals(138, spineCost);
        assertEquals(50, levelNeeded);
        assertTrue(Progression.shardsForLevel(levelNeeded) >= spineCost, "the level wall is the binding constraint, not the shard cost");
    }

    // ── school passives ─────────────────────────────────────────────────────────────────────

    @Test
    void highestSpineNodeIgnoresSideNodes() {
        Set<String> unlocked = Set.of("gra1", "gra2", "gra3", "gra5", "grad", "pyr9");
        assertEquals(5, SchoolPassives.highestSpineNode(MagicSchool.GRAVITAS, unlocked));
        assertEquals(9, SchoolPassives.highestSpineNode(MagicSchool.PYROMANCY, unlocked));
        assertEquals(0, SchoolPassives.highestSpineNode(MagicSchool.RADIANCE, unlocked));
    }

    @Test
    void passivesAddUpFromTheNodeDefinitions() {
        Set<String> all = new HashSet<>();
        for (SchoolTree.SchoolNode node : SchoolTree.nodesFor(MagicSchool.GRAVITAS)) {
            all.add(node.id());
        }
        assertEquals(0.67, SchoolPassives.damageBonus(MagicSchool.GRAVITAS, all), 1e-9, "the most a school can give");
        assertEquals(125, SchoolPassives.resourceBonus(MagicSchool.GRAVITAS, all));
        assertEquals(0.33, SchoolPassives.cooldownReduction(MagicSchool.GRAVITAS, all), 1e-9, "a whole tree gives 33 percent, under the 40 percent cap");
        assertEquals(0.0, SchoolPassives.damageBonus(MagicSchool.GRAVITAS, Set.of()));
        assertEquals(0.0, SchoolPassives.damageBonus(MagicSchool.GRAVITAS, Set.of("pyr1", "pyr4")), "other schools' nodes do not count");
    }

    // ── unique techniques ───────────────────────────────────────────────────────────────────

    private void uniqueOn(String... paths) throws IOException {
        StringBuilder json = new StringBuilder("{\"power\": {\"unique\": {");
        for (int i = 0; i < paths.length; i++) {
            json.append(i == 0 ? "" : ",").append('"').append(paths[i]).append("\": true");
        }
        json.append("}}}");
        Path file = dir.resolve("config.json");
        Files.writeString(file, json.toString());
        Config.useForTests(ConfigStore.load(file, ConfigCatalog.keys()));
    }

    @Test
    void nothingIsUniqueByDefault() {
        Map<String, String> holders = new HashMap<>();
        assertEquals(UniqueHolders.Claim.NOT_UNIQUE, UniqueHolders.claim(holders, "BLUE", "u1"));
        assertEquals(UniqueHolders.Claim.NOT_UNIQUE, UniqueHolders.claim(holders, "EMBERHEART", "u1"));
        assertTrue(holders.isEmpty());
    }

    @Test
    void aUniqueTechniqueHasOneHolderAtATime() throws IOException {
        uniqueOn("blue");
        Map<String, String> holders = new HashMap<>();
        assertEquals(UniqueHolders.Claim.CLAIMED, UniqueHolders.claim(holders, "BLUE", "u1"));
        assertEquals(UniqueHolders.Claim.ALREADY_YOURS, UniqueHolders.claim(holders, "BLUE", "u1"));
        assertEquals(UniqueHolders.Claim.TAKEN, UniqueHolders.claim(holders, "BLUE", "u2"));
        assertEquals(UniqueHolders.Claim.NOT_UNIQUE, UniqueHolders.claim(holders, "RED", "u2"), "only the switched-on ones");
        assertFalse(UniqueHolders.release(holders, "BLUE", "u2"), "someone else cannot free it");
        assertTrue(UniqueHolders.release(holders, "BLUE", "u1"));
        assertEquals(UniqueHolders.Claim.CLAIMED, UniqueHolders.claim(holders, "BLUE", "u2"));
    }

    // ── the catalog ─────────────────────────────────────────────────────────────────────────

    @Test
    void everyTechniqueHasAbilitiesAndEveryAbilityIdIsUnique() {
        Set<String> abilityIds = new HashSet<>();
        for (PowerCatalog.TechniqueDef def : PowerCatalog.techniques()) {
            List<AbilitySpec> abilities = PowerCatalog.abilities(def.id());
            assertFalse(abilities.isEmpty(), def.id() + " has no abilities");
            for (AbilitySpec spec : abilities) {
                assertTrue(abilityIds.add(spec.id()), "duplicate ability id " + spec.id());
                assertEquals(def.id(), spec.techniqueId());
            }
        }
        assertEquals(32 + 6, abilityIds.size(), "32 older abilities and the six cursed ones");
    }

    @Test
    void selectableSchoolsAreTheOnesThatGrantTechniques() {
        assertEquals(Set.of("PYROMANCY", "FROSTWEAVING", "STORMCALLING", "VOIDWALKING", "VERDANCY", "GRAVITAS", "RADIANCE"),
                PowerCatalog.selectableSchools());
        assertEquals(List.of("IRONMARROW", "BLUE", "RED"),
                PowerCatalog.techniquesOf("GRAVITAS").stream().map(PowerCatalog.TechniqueDef::id).toList());
    }

    @Test
    void theTreeGrantsTechniquesAtTheirSpineNodes() {
        assertEquals(List.of(), PowerCatalog.grantedBy("GRAVITAS", 0));
        assertEquals(List.of("IRONMARROW"), PowerCatalog.grantedBy("GRAVITAS", 3).stream().map(PowerCatalog.TechniqueDef::id).toList());
        assertEquals(List.of("IRONMARROW", "BLUE"), PowerCatalog.grantedBy("GRAVITAS", 4).stream().map(PowerCatalog.TechniqueDef::id).toList());
        assertEquals(List.of("IRONMARROW", "BLUE", "RED"), PowerCatalog.grantedBy("GRAVITAS", 10).stream().map(PowerCatalog.TechniqueDef::id).toList());
        assertEquals(List.of(), PowerCatalog.grantedBy("CHRONOMANCY", 10), "dormant school");
    }

    @Test
    void cursedAbilitiesReadTheirNumbersFromTheSettings() {
        AbilitySpec blue = PowerCatalog.ability("blue").orElseThrow();
        assertEquals(AbilityKind.BLUE, blue.kind());
        assertEquals(15.0, blue.cost());
        assertEquals(80, blue.cooldownTicks());
        assertEquals(10.0, blue.range());
        assertTrue(blue.needsTarget() && blue.needsLineOfSight() && !blue.allowSelf());

        AbilitySpec reverse = PowerCatalog.ability("reverse").orElseThrow();
        assertTrue(reverse.allowSelf() && reverse.playersOnly() && !reverse.needsLineOfSight());
        assertEquals(6.0, reverse.range());

        AbilitySpec domain = PowerCatalog.ability("domain").orElseThrow();
        assertEquals(150.0, domain.cost());
        assertFalse(domain.needsTarget());
    }

    @Test
    void olderAbilitiesKeepTheirCostsCooldownsAndRankGates() {
        AbilitySpec lash = PowerCatalog.ability("ember_lash").orElseThrow();
        assertEquals(AbilityKind.LEGACY, lash.kind());
        assertEquals(16.0, lash.cost());
        assertEquals(30, lash.cooldownTicks());
        assertEquals(20.0, lash.range());
        assertTrue(lash.needsTarget());
        assertEquals(0, PowerCatalog.rankIndexOf(lash));

        AbilitySpec hearthguard = PowerCatalog.ability("hearthguard").orElseThrow();
        assertFalse(hearthguard.needsTarget(), "a self buff needs no target");
        assertEquals(3, PowerCatalog.rankIndexOf(hearthguard));
        assertEquals(0, PowerCatalog.rankIndexOf(PowerCatalog.ability("blue").orElseThrow()));
    }
}
