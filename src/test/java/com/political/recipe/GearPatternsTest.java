package com.political.recipe;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class GearPatternsTest {
    @Test void armorHasVanillaMaterialFootprintsAndOneCore() {
        String[][] expected = {{"helmet", "MMM", "M M", "   "}, {"chestplate", "M M", "MMM", "MMM"},
                {"leggings", "MMM", "M M", "M M"}, {"boots", "   ", "M M", "M M"}};
        for (String[] example : expected) {
            String[] pattern = GearPatterns.armor(example[0]);
            assertEquals(1, Arrays.stream(pattern).filter("core"::equals).count());
            for (int i = 0; i < 9; i++) {
                assertEquals(example[1 + i / 3].charAt(i % 3) == 'M', pattern[i].equals("block"));
                assertEquals(pattern[i], pattern[i / 3 * 3 + 2 - i % 3], "armor must be symmetric");
            }
        }
    }
    @Test void axeAndHoeHaveDifferentCorrectMaterialCounts() {
        assertEquals(3, Arrays.stream(GearPatterns.tool("axe")).filter("block"::equals).count());
        assertEquals(2, Arrays.stream(GearPatterns.tool("hoe")).filter("block"::equals).count());
        assertFalse(Arrays.equals(GearPatterns.tool("axe"), GearPatterns.tool("hoe")));
    }
    @Test void shovelHasAHeadAndSwordHasTwoMaterials() {
        assertEquals("block", GearPatterns.tool("shovel")[1]);
        assertEquals(2, Arrays.stream(GearPatterns.tool("sword")).filter("block"::equals).count());
        for (String tool : new String[]{"sword", "pickaxe", "axe", "shovel", "hoe"})
            assertEquals(1, Arrays.stream(GearPatterns.tool(tool)).filter("core"::equals).count());
    }
}
