package com.political;


import com.political.bounty.SlayerItems;
import com.political.recipe.RecipeConfigManager;
import net.minecraft.world.item.ItemStack;

/**
 * Resolves legacy {@code political:<id>} recipe results for the large SlayerItems
 * material/gear families that were never given explicit switch entries in
 * {@link RecipeConfigManager#resolveCustomItem}:
 *
 * <ul>
 *   <li>{@code enchanted_*}        &rarr; {@code SlayerItems.createEnchanted*()}   (e.g. enchanted_ice)</li>
 *   <li>{@code compacted_*}        &rarr; {@code SlayerItems.createCompacted*()}   (e.g. compacted_iron)</li>
 *   <li>{@code super_compacted_*}  &rarr; {@code SlayerItems.createSuperCompacted*()} (e.g. super_compacted_gold)</li>
 *   <li>{@code gilded_*}           &rarr; {@code SlayerItems.createGilded*()}      (e.g. gilded_helmet, gilded_netherite_chestplate)</li>
 * </ul>
 *
 * <p>Resolution is purely reflective: the custom id is converted to PascalCase and
 * prefixed with {@code create}, then invoked as a no-arg static factory on
 * fallbacks in RecipeConfigManager and stays robust as new members are added to a family.
 *
 * <p>If the reflected method does not exist (e.g. {@code gilded_blackstone_sword}, which
 * caller can continue down its normal fallback chain — the modern NBT-shape path and
 * every other explicit mapping are untouched.
 */
public final class SlayerItemFamilyResolver {

    private SlayerItemFamilyResolver() {}

    /**
     * @param customId a canonicalized custom id (already stripped of the {@code political:} prefix)
     * @return a freshly built stack for one of the supported families, or {@link ItemStack#EMPTY}
     */
    public static ItemStack resolve(String customId) {
        if (customId == null || customId.isEmpty()) return ItemStack.EMPTY;

        boolean family = customId.startsWith("enchanted_")
                || customId.startsWith("compacted_")
                || customId.startsWith("super_compacted_")
                || customId.startsWith("gilded_");
        if (!family) return ItemStack.EMPTY;

        String methodName = "create" + toPascalCase(customId);
        try {
            var m = SlayerItems.class.getMethod(methodName);
            Object out = m.invoke(null);
            if (out instanceof ItemStack stack && !stack.isEmpty()) {
                return stack;
            }
        } catch (Exception ignored) {
            // No matching factory — fall through to EMPTY so other resolvers can try.
        }
        return ItemStack.EMPTY;
    }

    private static String toPascalCase(String snake) {
        if (snake == null || snake.isEmpty()) return snake;
        String[] parts = snake.split("_");
        StringBuilder out = new StringBuilder();
        for (String p : parts) {
            if (p == null || p.isEmpty()) continue;
            out.append(Character.toUpperCase(p.charAt(0)));
            if (p.length() > 1) out.append(p.substring(1));
        }
        return out.toString();
    }
}
