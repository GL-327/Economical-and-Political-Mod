package com.political.recipe;

import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SymmetricLayoutsTest {
    @Test void allThirtyTrinketsHaveDistinctSymmetricGridsWithUnchangedCosts() {
        String[] bases = {"gpgpdpgpg", "_g_g eg_g_".replace(" ", ""), "pppplpppp", "rgrgagrgr", "gggpd pggg".replace(" ", ""), "lrlrerlrl"};
        Set<List<String>> all = new HashSet<>();
        for (String base : bases) {
            var original = base.chars().mapToObj(c -> String.valueOf((char)c)).toList();
            for (int i = 0; i < 5; i++) {
                var variant = SymmetricLayouts.variant(original, i);
                assertTrue(SymmetricLayouts.symmetric(variant));
                assertTrue(all.add(variant), "Duplicate recipe grid");
                assertEquals(counts(original), counts(variant));
            }
            assertEquals(original, SymmetricLayouts.variant(original, 0));
        }
        assertEquals(30, all.size());
    }
    private Map<String, Long> counts(List<String> grid) {
        return grid.stream().collect(Collectors.groupingBy(s -> s, Collectors.counting()));
    }
}
