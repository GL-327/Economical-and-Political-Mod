package com.political.recipe;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class IngredientPlanTest {
    private static IngredientPlan.Stack s(String key, int count) { return new IngredientPlan.Stack(key, count); }
    @Test void repeatedCellsCannotReuseTheSameInventoryItem() {
        var recipe = List.of(s("iron", 1), s("iron", 1), s("iron", 1));
        assertTrue(IngredientPlan.allocate(recipe, List.of(s("iron", 2))).isEmpty());
        assertEquals(List.of(new IngredientPlan.Take(0, 3)), IngredientPlan.allocate(recipe, List.of(s("iron", 3))).orElseThrow());
    }
    @Test void customCoresCannotBeReplacedByVanillaOrAnotherTier() {
        var recipe = List.of(s("custom:core_t2", 1));
        assertTrue(IngredientPlan.allocate(recipe, List.of(s("minecraft:heavy_core", 1), s("custom:core_t1", 1))).isEmpty());
    }
    @Test void quantitiesCanSpanStacksAndEmptySlotsWithoutTouchingOtherItems() {
        var recipe = List.of(s("lapis", 160), s("core", 1));
        var inventory = Arrays.asList(s("lapis", 64), null, s("lapis", 64), s("lapis", 40), s("core", 1), s("other", 64));
        assertEquals(List.of(new IngredientPlan.Take(0, 64), new IngredientPlan.Take(2, 64),
                new IngredientPlan.Take(3, 32), new IngredientPlan.Take(4, 1)), IngredientPlan.allocate(recipe, inventory).orElseThrow());
        assertEquals(40, inventory.get(3).count());
    }
}
