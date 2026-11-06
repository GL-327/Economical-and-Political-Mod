package com.political.recipe;

import com.mojang.authlib.GameProfile;
import com.political.bounty.SlayerItems;
import com.political.bounty.SlayerManager;
import com.political.items.GemGear;
import java.util.*;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.LoggerFactory;

/** Opt-in development checks using the real server registries and crafting matcher. */
public final class RecipeSmoke {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RecipeSmoke.class);
    private int checks;
    private final List<String> failures = new ArrayList<>();
    private RecipeSmoke() {}

    private void check(String name, boolean condition) {
        checks++;
        if (!condition) failures.add(name);
    }

    public static void runIfRequested(MinecraftServer server) {
        if (!Boolean.getBoolean("politicalserver.recipeSmoke")) return;
        RecipeSmoke smoke = new RecipeSmoke();
        try { smoke.run(server); }
        catch (Exception e) { smoke.failures.add("exception: " + e); LOGGER.error("Recipe smoke threw", e); }
        if (smoke.failures.isEmpty()) LOGGER.info("Recipe smoke PASSED: {} checks", smoke.checks);
        else LOGGER.error("Recipe smoke FAILED: {} of {} checks: {}", smoke.failures.size(), smoke.checks, smoke.failures);
    }

    private void run(MinecraftServer server) {
        FakePlayer player = FakePlayer.get(server.overworld(), new GameProfile(
                UUID.fromString("00000000-0000-0000-0000-00000000c010"), "[RecipeSmoke]"));
        try {
            check("all saved entries resolve", RecipeConfigManager.getLastFailedRecipeNames().isEmpty());
            for (var recipe : RecipeConfigManager.getRecipes()) {
                if (!RecipeConfigManager.isRecipeEnabled(recipe.name) || !BountyCraftingHandler.meetsRecipeGate(player, recipe)) continue;
                var grid = Arrays.stream(recipe.ingredients).map(ItemStack::copy).toList();
                var matched = BountyCraftingHandler.getMatchedCustomRecipe(grid, player);
                check("unambiguous table recipe: " + recipe.name, matched != null
                        && RecipeInventory.key(matched.result).equals(RecipeInventory.key(recipe.result))
                        && matched.result.getCount() == recipe.result.getCount());
            }

            var chest = recipeOutput(GemGear.createEmeraldChestplate());
            check("gem chest exists", chest != null);
            if (chest == null) return;
            player.getInventory().clearContent();
            player.getInventory().setItem(0, new ItemStack(Items.EMERALD_BLOCK));
            player.getInventory().setItem(1, SlayerItems.createCustomCrafterCoreT1());
            check("one material cannot satisfy eight repeated cells", !RecipeInventory.craft(player, chest));
            check("failed craft consumes nothing", player.getInventory().getItem(0).getCount() == 1 && !player.getInventory().getItem(1).isEmpty());
            player.getInventory().setItem(0, new ItemStack(Items.EMERALD_BLOCK, 8));
            player.getInventory().setItem(1, new ItemStack(Items.HEAVY_CORE));
            check("vanilla core is not a crafting core", !RecipeInventory.canCraft(player, chest));
            player.getInventory().setItem(1, SlayerItems.createCustomCrafterCoreT2());
            check("wrong tier cannot substitute", !RecipeInventory.canCraft(player, chest));
            player.getInventory().setItem(1, SlayerItems.createCustomCrafterCoreT1());
            check("correct materials and core craft", RecipeInventory.craft(player, chest));
            check("all eight blocks and core consumed", count(player, new ItemStack(Items.EMERALD_BLOCK)) == 0
                    && count(player, SlayerItems.createCustomCrafterCoreT1()) == 0);
            check("actual armor delivered", count(player, chest.result) == 1);

            var grid = GearRecipes.grid("chestplate", new ItemStack(Items.EMERALD_BLOCK), SlayerItems.createCustomCrafterCoreT1());
            check("normal chest grid matches", BountyCraftingHandler.getMatchedCustomRecipe(Arrays.asList(grid), player) == chest);
            grid[1] = SlayerItems.createCustomCrafterCoreT2();
            check("table also rejects wrong core tier", BountyCraftingHandler.getMatchedCustomRecipe(Arrays.asList(grid), player) == null);

            player.getInventory().clearContent();
            player.getInventory().setItem(0, new ItemStack(Items.DIRT, 2));
            ItemStack[] inputs = new ItemStack[9];
            Arrays.fill(inputs, ItemStack.EMPTY);
            inputs[4] = new ItemStack(Items.DIRT, 2);
            var batch = new SlayerRecipes.Recipe("Recipe smoke batch", new ItemStack(Items.STONE, 4), inputs, null, 0);
            check("batch craft succeeds", RecipeInventory.craft(player, batch));
            check("authoritative output count delivered", count(player, new ItemStack(Items.STONE)) == 4);
            player.getInventory().setItem(0, new ItemStack(Items.DIRT, 2));
            var gated = new SlayerRecipes.Recipe("Recipe smoke gate", new ItemStack(Items.STONE), inputs, SlayerManager.SlayerType.ZOMBIE, 999);
            check("inventory crafting enforces slayer gate", !RecipeInventory.craft(player, gated));
            check("gate failure consumes nothing", count(player, new ItemStack(Items.DIRT)) == 2);
            check("all slayer and gem set outputs have recipes", countSetRecipes() == 88);
        } finally { player.getInventory().clearContent(); }
    }

    private static SlayerRecipes.Recipe recipeOutput(ItemStack output) { return SlayerRecipes.getRecipeForOutput(output); }

    private static int count(FakePlayer player, ItemStack expected) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && RecipeInventory.key(stack).equals(RecipeInventory.key(expected))) count += stack.getCount();
        }
        return count;
    }

    private static int countSetRecipes() {
        int found = 0;
        for (var type : SlayerManager.SlayerType.values()) for (int tier = 1; tier <= 2; tier++) {
            List<ItemStack> set = List.of(SlayerItems.createSlayerHelmet(type, tier), SlayerItems.createSlayerChestplate(type, tier),
                    SlayerItems.createSlayerLeggings(type, tier), SlayerItems.createSlayerBoots(type, tier),
                    tier == 1 ? SlayerItems.createSlayerSword(type) : SlayerItems.createUpgradedSlayerSword(type));
            for (ItemStack item : set) if (recipeOutput(item) != null) found++;
        }
        for (var recipe : RecipeConfigManager.getRecipes()) {
            String id = SlayerItems.getCustomItemId(recipe.result);
            if (id != null && (id.startsWith("emerald_") || id.startsWith("lapis_"))) found++;
        }
        return found;
    }
}
