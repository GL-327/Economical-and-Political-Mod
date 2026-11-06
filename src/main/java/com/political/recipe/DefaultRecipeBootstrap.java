package com.political.recipe;

import com.political.DanielsPickaxe;
import com.political.DragonGearRecipeBootstrap;
import com.political.bounty.SlayerItems;
import com.political.bounty.SlayerManager;
import com.political.items.CustomItemHandler;
import com.political.items.DragonChestplates;
import com.political.items.LegendaryTools;
import com.political.items.GemGear;
import com.political.technique.Technique;
import com.political.technique.TechniqueItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Small default recipe list after HardcodedRecipes was removed:
 * slayer gear, T2, 8 dragon plates, 4 tool sets, cores, technique manuals, kept uniques.
 */
public final class DefaultRecipeBootstrap {

    private DefaultRecipeBootstrap() {}

    public static void ensureRecipes() {
        List<SlayerRecipes.Recipe> out = new ArrayList<>();
        out.addAll(DragonGearRecipeBootstrap.buildRecipes());
        addSlayerGear(out);
        addGemGear(out);
        addLegendaryTools(out);
        addTechniqueManuals(out);
        addCores(out);
        addKeptUniques(out);
        RecipeConfigManager.upsertCustomRecipesSilent(out);
    }

    private static void addSlayerGear(List<SlayerRecipes.Recipe> out) {
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            for (int tier = 1; tier <= 2; tier++) {
                ItemStack core = tier == 1 ? SlayerItems.createCustomCrafterCoreT1() : SlayerItems.createCustomCrafterCoreT2();
                String prefix = type.displayName + (tier == 1 ? " Slayer " : " T2 ");
                for (String piece : List.of("helmet", "chestplate", "leggings", "boots", "sword")) {
                    ItemStack[] grid = GearRecipes.grid(piece, SlayerItems.createChunk(type), core);
                    if (tier == 2) {
                        int upgrade = switch (piece) { case "helmet", "leggings" -> 1; case "boots" -> 7; default -> 4; };
                        grid[upgrade] = slayerPiece(type, 1, piece);
                    }
                    out.add(shaped(prefix + title(piece), slayerPiece(type, tier, piece), grid, type, tier == 1 ? 1 : 5));
                }
            }
        }
    }

    private static ItemStack slayerPiece(SlayerManager.SlayerType type, int tier, String piece) {
        return switch (piece) {
            case "helmet" -> SlayerItems.createSlayerHelmet(type, tier);
            case "chestplate" -> SlayerItems.createSlayerChestplate(type, tier);
            case "leggings" -> SlayerItems.createSlayerLeggings(type, tier);
            case "boots" -> SlayerItems.createSlayerBoots(type, tier);
            case "sword" -> tier == 1 ? SlayerItems.createSlayerSword(type) : SlayerItems.createUpgradedSlayerSword(type);
            default -> throw new IllegalArgumentException(piece);
        };
    }

    private static String title(String word) { return Character.toUpperCase(word.charAt(0)) + word.substring(1); }

    private static void addGemGear(List<SlayerRecipes.Recipe> out) {
        List<ItemStack> emerald = List.of(GemGear.createEmeraldHelmet(), GemGear.createEmeraldChestplate(),
                GemGear.createEmeraldLeggings(), GemGear.createEmeraldBoots(), GemGear.createEmeraldSword(),
                GemGear.createEmeraldPickaxe(), GemGear.createEmeraldAxe(), GemGear.createEmeraldShovel(), GemGear.createEmeraldHoe());
        List<ItemStack> lapis = List.of(GemGear.createLapisHelmet(), GemGear.createLapisChestplate(),
                GemGear.createLapisLeggings(), GemGear.createLapisBoots(), GemGear.createLapisSword(),
                GemGear.createLapisPickaxe(), GemGear.createLapisAxe(), GemGear.createLapisShovel(), GemGear.createLapisHoe());
        List<String> pieces = List.of("helmet", "chestplate", "leggings", "boots", "sword", "pickaxe", "axe", "shovel", "hoe");
        for (int i = 0; i < pieces.size(); i++) {
            String piece = pieces.get(i);
            out.add(shaped("Emerald " + title(piece), emerald.get(i), GearRecipes.grid(piece, new ItemStack(Items.EMERALD_BLOCK), SlayerItems.createCustomCrafterCoreT1())));
            out.add(shaped("Lapis " + title(piece), lapis.get(i), GearRecipes.grid(piece, new ItemStack(Items.LAPIS_BLOCK), SlayerItems.createCustomCrafterCoreT1())));
        }
    }

    private static void addLegendaryTools(List<SlayerRecipes.Recipe> out) {
        out.add(shaped("Dragon Pickaxe", LegendaryTools.createDragonPickaxe(), grid(
                Items.MAGMA_BLOCK, SlayerItems.createCustomCrafterCoreT3(), Items.MAGMA_BLOCK,
                Items.BLAZE_ROD, Items.NETHERITE_PICKAXE, Items.BLAZE_ROD,
                Items.OBSIDIAN, Items.LAVA_BUCKET, Items.OBSIDIAN)));
        out.add(shaped("Dragon Axe", LegendaryTools.createDragonAxe(), grid(
                Items.MAGMA_BLOCK, SlayerItems.createCustomCrafterCoreT3(), Items.MAGMA_BLOCK,
                Items.BLAZE_ROD, Items.NETHERITE_AXE, Items.BLAZE_ROD,
                Items.OBSIDIAN, Items.LAVA_BUCKET, Items.OBSIDIAN)));
        out.add(shaped("Void Pickaxe", LegendaryTools.createVoidPickaxe(), grid(
                Items.ENDER_PEARL, SlayerItems.createCustomCrafterCoreT3(), Items.ENDER_PEARL,
                Items.OBSIDIAN, Items.NETHERITE_PICKAXE, Items.OBSIDIAN,
                Items.ENDER_EYE, Items.CHORUS_FRUIT, Items.ENDER_EYE)));
        out.add(shaped("Void Axe", LegendaryTools.createVoidAxe(), grid(
                Items.ENDER_PEARL, SlayerItems.createCustomCrafterCoreT3(), Items.ENDER_PEARL,
                Items.OBSIDIAN, Items.NETHERITE_AXE, Items.OBSIDIAN,
                Items.ENDER_EYE, Items.CHORUS_FRUIT, Items.ENDER_EYE)));
        out.add(shaped("Nature Axe", LegendaryTools.createNatureAxe(), grid(
                Items.OAK_SAPLING, SlayerItems.createCustomCrafterCoreT3(), Items.OAK_SAPLING,
                Items.BONE_MEAL, Items.DIAMOND_AXE, Items.BONE_MEAL,
                Items.MOSS_BLOCK, Items.GOLDEN_APPLE, Items.MOSS_BLOCK)));
        out.add(shaped("Nature Pickaxe", LegendaryTools.createNaturePickaxe(), grid(
                Items.OAK_SAPLING, SlayerItems.createCustomCrafterCoreT3(), Items.OAK_SAPLING,
                Items.BONE_MEAL, Items.DIAMOND_PICKAXE, Items.BONE_MEAL,
                Items.MOSS_BLOCK, Items.GOLDEN_APPLE, Items.MOSS_BLOCK)));
        out.add(shaped("Frost Pickaxe", LegendaryTools.createFrostPickaxe(), grid(
                Items.PACKED_ICE, SlayerItems.createCustomCrafterCoreT3(), Items.PACKED_ICE,
                Items.BLUE_ICE, Items.DIAMOND_PICKAXE, Items.BLUE_ICE,
                Items.SNOW_BLOCK, Items.POWDER_SNOW_BUCKET, Items.SNOW_BLOCK)));
        out.add(shaped("Frost Shovel", LegendaryTools.createFrostShovel(), grid(
                Items.PACKED_ICE, SlayerItems.createCustomCrafterCoreT3(), Items.PACKED_ICE,
                Items.BLUE_ICE, Items.DIAMOND_SHOVEL, Items.BLUE_ICE,
                Items.SNOW_BLOCK, Items.POWDER_SNOW_BUCKET, Items.SNOW_BLOCK)));
        out.add(shaped("Dragon Shovel", LegendaryTools.createDragonShovel(), grid(
                Items.MAGMA_BLOCK, SlayerItems.createCustomCrafterCoreT3(), Items.MAGMA_BLOCK,
                Items.BLAZE_ROD, Items.NETHERITE_SHOVEL, Items.BLAZE_ROD,
                Items.OBSIDIAN, Items.LAVA_BUCKET, Items.OBSIDIAN)));
        out.add(shaped("Dragon Hoe", LegendaryTools.createDragonHoe(), grid(
                Items.MAGMA_BLOCK, SlayerItems.createCustomCrafterCoreT3(), Items.MAGMA_BLOCK,
                Items.BLAZE_ROD, Items.NETHERITE_HOE, Items.BLAZE_ROD,
                Items.OBSIDIAN, Items.LAVA_BUCKET, Items.OBSIDIAN)));
        out.add(shaped("Void Shovel", LegendaryTools.createVoidShovel(), grid(
                Items.ENDER_PEARL, SlayerItems.createCustomCrafterCoreT3(), Items.ENDER_PEARL,
                Items.OBSIDIAN, Items.NETHERITE_SHOVEL, Items.OBSIDIAN,
                Items.ENDER_EYE, Items.CHORUS_FRUIT, Items.ENDER_EYE)));
        out.add(shaped("Void Hoe", LegendaryTools.createVoidHoe(), grid(
                Items.ENDER_PEARL, SlayerItems.createCustomCrafterCoreT3(), Items.ENDER_PEARL,
                Items.OBSIDIAN, Items.NETHERITE_HOE, Items.OBSIDIAN,
                Items.ENDER_EYE, Items.CHORUS_FRUIT, Items.ENDER_EYE)));
        out.add(shaped("Nature Shovel", LegendaryTools.createNatureShovel(), grid(
                Items.OAK_SAPLING, SlayerItems.createCustomCrafterCoreT3(), Items.OAK_SAPLING,
                Items.BONE_MEAL, Items.DIAMOND_SHOVEL, Items.BONE_MEAL,
                Items.MOSS_BLOCK, Items.GOLDEN_APPLE, Items.MOSS_BLOCK)));
        out.add(shaped("Nature Hoe", LegendaryTools.createNatureHoe(), grid(
                Items.OAK_SAPLING, SlayerItems.createCustomCrafterCoreT3(), Items.OAK_SAPLING,
                Items.BONE_MEAL, Items.DIAMOND_HOE, Items.BONE_MEAL,
                Items.MOSS_BLOCK, Items.GOLDEN_APPLE, Items.MOSS_BLOCK)));
        out.add(shaped("Frost Axe", LegendaryTools.createFrostAxe(), grid(
                Items.PACKED_ICE, SlayerItems.createCustomCrafterCoreT3(), Items.PACKED_ICE,
                Items.BLUE_ICE, Items.DIAMOND_AXE, Items.BLUE_ICE,
                Items.SNOW_BLOCK, Items.POWDER_SNOW_BUCKET, Items.SNOW_BLOCK)));
        out.add(shaped("Frost Hoe", LegendaryTools.createFrostHoe(), grid(
                Items.PACKED_ICE, SlayerItems.createCustomCrafterCoreT3(), Items.PACKED_ICE,
                Items.BLUE_ICE, Items.DIAMOND_HOE, Items.BLUE_ICE,
                Items.SNOW_BLOCK, Items.POWDER_SNOW_BUCKET, Items.SNOW_BLOCK)));
    }

    private static void addTechniqueManuals(List<SlayerRecipes.Recipe> out) {
        for (Technique technique : Technique.all()) {
            var focus = switch (technique) {
                case EMBERHEART -> Items.BLAZE_POWDER;
                case GLACIER_SOUL -> Items.SNOWBALL;
                case STORM_VEIN -> Items.REDSTONE;
                case UMBRAL_STEP -> Items.ENDER_PEARL;
                case WILDGROWTH -> Items.OAK_SAPLING;
                case IRONMARROW -> Items.IRON_INGOT;
                case TIDEBOUND -> Items.PRISMARINE_SHARD;
                case WHISPERWIND -> Items.FEATHER;
            };
            ItemStack[] g = grid(
                    Items.BOOK, Items.LAPIS_LAZULI, Items.BOOK,
                    Items.AMETHYST_SHARD, SlayerItems.createCustomCrafterCoreT2(), Items.AMETHYST_SHARD,
                    Items.PAPER, focus, Items.PAPER);
            out.add(shaped("Technique Manual: " + technique.displayName, TechniqueItems.createManual(technique), g));
        }
    }

    private static void addCores(List<SlayerRecipes.Recipe> out) {
        out.add(shaped("Custom Core T1", DragonChestplates.createCustomCoreT1(), grid(
                Items.IRON_INGOT, Items.GOLD_INGOT, Items.IRON_INGOT,
                Items.GOLD_INGOT, Items.DIAMOND, Items.GOLD_INGOT,
                Items.IRON_INGOT, Items.GOLD_INGOT, Items.IRON_INGOT)));
        out.add(shaped("Custom Core T2", DragonChestplates.createCustomCoreT2(), grid(
                Items.DIAMOND, Items.NETHERITE_SCRAP, Items.DIAMOND,
                Items.NETHERITE_SCRAP, DragonChestplates.createCustomCoreT1(), Items.NETHERITE_SCRAP,
                Items.DIAMOND, Items.NETHERITE_SCRAP, Items.DIAMOND)));
        out.add(shaped("Crafting Core T1", SlayerItems.createCustomCrafterCoreT1(), grid(
                Items.IRON_INGOT, Items.GOLD_INGOT, Items.IRON_INGOT,
                Items.GOLD_INGOT, Items.HEAVY_CORE, Items.GOLD_INGOT,
                Items.IRON_INGOT, Items.GOLD_INGOT, Items.IRON_INGOT)));
        out.add(shaped("Crafting Core T2", SlayerItems.createCustomCrafterCoreT2(), grid(
                Items.DIAMOND, Items.NETHERITE_SCRAP, Items.DIAMOND,
                Items.NETHERITE_SCRAP, SlayerItems.createCustomCrafterCoreT1(), Items.NETHERITE_SCRAP,
                Items.DIAMOND, Items.NETHERITE_SCRAP, Items.DIAMOND)));
        out.add(shaped("Crafting Core T3", SlayerItems.createCustomCrafterCoreT3(), grid(
                Items.NETHERITE_INGOT, Items.NETHER_STAR, Items.NETHERITE_INGOT,
                Items.DIAMOND, SlayerItems.createCustomCrafterCoreT2(), Items.DIAMOND,
                Items.NETHERITE_INGOT, Items.NETHER_STAR, Items.NETHERITE_INGOT)));
        out.add(shaped("Dragon Core T1", SlayerItems.createDragonCrafterCoreT1(), grid(
                Items.BLAZE_ROD, Items.GOLD_INGOT, Items.BLAZE_ROD,
                Items.GOLD_INGOT, Items.NETHER_STAR, Items.GOLD_INGOT,
                Items.BLAZE_ROD, Items.GOLD_INGOT, Items.BLAZE_ROD)));
        out.add(shaped("Dragon Core T2", SlayerItems.createDragonCrafterCoreT2(), grid(
                Items.DIAMOND, Items.NETHERITE_SCRAP, Items.DIAMOND,
                Items.NETHERITE_SCRAP, SlayerItems.createDragonCrafterCoreT1(), Items.NETHERITE_SCRAP,
                Items.DIAMOND, Items.NETHERITE_SCRAP, Items.DIAMOND)));
        out.add(shaped("Dragon Core T3", SlayerItems.createDragonCrafterCoreT3(), grid(
                Items.NETHERITE_INGOT, Items.NETHER_STAR, Items.NETHERITE_INGOT,
                Items.DIAMOND, SlayerItems.createDragonCrafterCoreT2(), Items.DIAMOND,
                Items.NETHERITE_INGOT, Items.DRAGON_BREATH, Items.NETHERITE_INGOT)));
    }

    private static void addKeptUniques(List<SlayerRecipes.Recipe> out) {
        out.add(shaped("Daniel's Pickaxe", DanielsPickaxe.create(), grid(
                Items.NETHERITE_INGOT, SlayerItems.createCustomCrafterCoreT3(), Items.NETHERITE_INGOT,
                Items.DIAMOND, Items.NETHERITE_PICKAXE, Items.DIAMOND,
                Items.OBSIDIAN, Items.END_CRYSTAL, Items.OBSIDIAN)));
        ItemStack[] greed = GearRecipes.grid("helmet", new ItemStack(Items.GOLD_INGOT), SlayerItems.createCustomCrafterCoreT2());
        greed[1] = new ItemStack(Items.GOLDEN_HELMET);
        out.add(shaped("Crown of Greed", SlayerItems.createCrownOfGreed(), greed));
        ItemStack[] midas = GearRecipes.grid("helmet", new ItemStack(Items.GOLD_BLOCK), SlayerItems.createCustomCrafterCoreT3());
        midas[1] = SlayerItems.createCrownOfGreed();
        out.add(shaped("Crown of Midas", SlayerItems.createCrownOfMidas(), midas));
        out.add(shaped("Midas Sword", SlayerItems.createMidasSword(), grid(
                Items.GOLD_INGOT, SlayerItems.createCustomCrafterCoreT3(), Items.GOLD_INGOT,
                Items.GOLD_INGOT, Items.GOLDEN_SWORD, Items.GOLD_INGOT,
                Items.GOLD_BLOCK, Items.DIAMOND, Items.GOLD_BLOCK)));
        out.add(shaped("Harvey's Stick", CustomItemHandler.createHarveysStick(), grid(
                Items.COPPER_INGOT, Items.LIGHTNING_ROD.weathering().unaffected(), Items.COPPER_INGOT,
                Items.COPPER_INGOT, Items.STICK, Items.COPPER_INGOT,
                Items.COPPER_BLOCK.weathering().unaffected(), SlayerItems.createCustomCrafterCoreT3(), Items.COPPER_BLOCK.weathering().unaffected())));
        ItemStack[] hermes = GearRecipes.grid("boots", new ItemStack(Items.FEATHER), SlayerItems.createCustomCrafterCoreT2());
        hermes[7] = new ItemStack(Items.IRON_BOOTS);
        out.add(shaped("Hermes Shoes", CustomItemHandler.createHermesShoes(), hermes));
        out.add(shaped("Rift Mirror", CustomItemHandler.createRiftMirror(), grid(
                Items.ENDER_PEARL, Items.AMETHYST_SHARD, Items.ENDER_PEARL,
                Items.ENDER_EYE, Items.GLASS_PANE, Items.ENDER_EYE,
                Items.OBSIDIAN, SlayerItems.createCustomCrafterCoreT3(), Items.OBSIDIAN)));
        out.add(shaped("Ember Signet", CustomItemHandler.createEmberSignet(), grid(
                Items.BLAZE_POWDER, Items.MAGMA_CREAM, Items.BLAZE_POWDER,
                Items.GOLD_INGOT, Items.FIRE_CHARGE, Items.GOLD_INGOT,
                Items.BLAZE_POWDER, SlayerItems.createCustomCrafterCoreT3(), Items.BLAZE_POWDER)));
        out.add(shaped("Frostbound Talisman", CustomItemHandler.createFrostboundTalisman(), grid(
                Items.SNOWBALL, Items.PACKED_ICE, Items.SNOWBALL,
                Items.BLUE_ICE, Items.HEART_OF_THE_SEA, Items.BLUE_ICE,
                Items.SNOWBALL, SlayerItems.createCustomCrafterCoreT3(), Items.SNOWBALL)));
        out.add(shaped("Echo Chime", CustomItemHandler.createEchoChime(), grid(
                Items.ECHO_SHARD, Items.BELL, Items.ECHO_SHARD,
                Items.AMETHYST_SHARD, Items.GOAT_HORN, Items.AMETHYST_SHARD,
                Items.ECHO_SHARD, SlayerItems.createCustomCrafterCoreT3(), Items.ECHO_SHARD)));
        out.add(shaped("Terra Relay", CustomItemHandler.createTerraRelay(), grid(
                Items.MOSS_BLOCK, Items.OAK_SAPLING, Items.MOSS_BLOCK,
                Items.BONE_MEAL, Items.HEART_OF_THE_SEA, Items.BONE_MEAL,
                Items.DIRT, SlayerItems.createCustomCrafterCoreT3(), Items.DIRT)));
    }

    private static SlayerRecipes.Recipe shaped(String name, ItemStack result, ItemStack[] grid) {
        return shaped(name, result, grid, null, 0);
    }

    private static SlayerRecipes.Recipe shaped(String name, ItemStack result, ItemStack[] grid,
                                               SlayerManager.SlayerType type, int level) {
        boolean[] locked = new boolean[9];
        java.util.Arrays.fill(locked, true);
        return new SlayerRecipes.Recipe(name, result, grid, locked, type, level);
    }

    private static ItemStack[] grid(Object a, Object b, Object c, Object d, Object e, Object f, Object g, Object h, Object i) {
        return new ItemStack[]{stack(a), stack(b), stack(c), stack(d), stack(e), stack(f), stack(g), stack(h), stack(i)};
    }

    private static ItemStack stack(Object o) {
        if (o instanceof ItemStack s) return s;
        if (o instanceof net.minecraft.world.item.Item item) return new ItemStack(item);
        return ItemStack.EMPTY;
    }
}
