package com.political.recipe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Pure inventory allocation: aggregate repeated recipe cells before planning any removal. */
public final class IngredientPlan {
    public record Stack(String key, int count) {
        public Stack {
            if (key == null || key.isBlank() || count <= 0) throw new IllegalArgumentException("Invalid ingredient");
        }
    }
    public record Take(int slot, int count) {}
    private IngredientPlan() {}

    /** Null inventory slots are empty. Failure never returns a partial consumption plan. */
    public static Optional<List<Take>> allocate(List<Stack> ingredients, List<Stack> inventory) {
        Map<String, Integer> remaining = new LinkedHashMap<>();
        for (Stack ingredient : ingredients) remaining.merge(ingredient.key(), ingredient.count(), Math::addExact);
        List<Take> result = new ArrayList<>();
        for (int slot = 0; slot < inventory.size(); slot++) {
            Stack stack = inventory.get(slot);
            if (stack == null) continue;
            int needed = remaining.getOrDefault(stack.key(), 0);
            int take = Math.min(needed, stack.count());
            if (take > 0) {
                result.add(new Take(slot, take));
                remaining.put(stack.key(), needed - take);
            }
        }
        return remaining.values().stream().anyMatch(count -> count > 0) ? Optional.empty() : Optional.of(List.copyOf(result));
    }
}
