package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Skyblock-style trinkets (consumables & trophies) with recipes upserted into custom_recipes.json.
 */
public final class SkyblockTrinkets {

    private SkyblockTrinkets() {}

    private record Def(String id, String name, String[] lore, net.minecraft.world.item.Item base,
                       String effect) {}

    private static final List<Def> DEFS = Arrays.asList(
            def("sb_glacite_splinter", "§b§lGlacite Splinter", "luck_60", Items.LIGHT_BLUE_DYE),
            def("sb_mithril_flake", "§5§lMithril Flake", "haste_80", Items.IRON_INGOT),
            def("sb_gemstone_dust", "§d§lGemstone Dust", "speed_80", Items.PURPLE_DYE),
            def("sb_dwarven_geode", "§6§lDwarven Geode", "coins_5000", Items.CLAY_BALL),
            def("sb_crystal_hollows_map", "§5§lCrystal Hollows Map", "night_vision_200", Items.MAP),
            def("sb_jungle_fern", "§2§lJungle Fern", "regen_100", Items.FERN),
            def("sb_tangle_moss", "§a§lTangle Moss", "jump_120", Items.MOSS_BLOCK),
            def("sb_sweet_berries_plus", "§c§lSweet Berries+", "saturation_40", Items.SWEET_BERRIES),
            def("sb_mushroom_spores", "§c§lCrimson Spores", "resistance_100", Items.CRIMSON_FUNGUS),
            def("sb_deep_sea_pearl", "§3§lDeep Sea Pearl", "water_breath_300", Items.PRISMARINE_CRYSTALS),
            def("sb_treasure_hook", "§e§lTreasure Hook", "luck_120", Items.TRIPWIRE_HOOK),
            def("sb_sea_creature_bait", "§b§lSea Creature Bait", "dolphins_grace_200", Items.TROPICAL_FISH),
            def("sb_volcanic_ash", "§4§lVolcanic Ash", "fire_res_200", Items.BLAZE_POWDER),
            def("sb_ember_fragment", "§6§lEmber Fragment", "strength_100", Items.FIRE_CHARGE),
            def("sb_void_splinter", "§5§lVoid Splinter", "slow_falling_160", Items.ENDER_PEARL),
            def("sb_chronos_token", "§e§lChronos Token", "coins_15000", Items.CLOCK),
            def("sb_bits_coupon", "§a§lBits Coupon", "coins_25000", Items.PAPER),
            def("sb_experience_bottle_rich", "§a§lRich XP Bottle", "xp_orb_burst", Items.EXPERIENCE_BOTTLE),
            def("sb_mastery_nibble", "§6§lMastery Nibble", "mastery_40_all", Items.HONEY_BOTTLE),
            def("sb_combat_chunk", "§c§lCombat Chunk", "mastery_combat_80", Items.BRICK),
            def("sb_mining_chunk", "§7§lMining Chunk", "mastery_gather_80", Items.COBBLESTONE),
            def("sb_magic_mote", "§d§lMagic Mote", "mastery_magic_80", Items.GLOW_INK_SAC),
            def("sb_ranchers_cookie", "§e§lRancher's Cookie", "farming_saturation", Items.COOKIE),
            def("sb_builder_rod", "§e§lBuilder's Rod", "reach_hint", Items.STICK),
            def("sb_slayer_vial", "§4§lSlayer Vial", "weakness_aura_mobs", Items.GLASS_BOTTLE),
            def("sb_dungeon_key", "§6§lDungeon Key", "coins_8000", Items.TRIPWIRE_HOOK),
            def("sb_reaper_orb", "§8§lReaper Orb", "wither_skull_chance_flavor", Items.WITHER_SKELETON_SKULL),
            def("sb_phoenix_feather", "§c§lPhoenix Feather", "regen_200", Items.FEATHER),
            def("sb_skyblock_menu", "§b§lPortable Menu", "open_skills_hint", Items.NETHER_STAR),
            def("sb_pocket_espresso", "§6§lPocket Espresso", "speed_haste_60", Items.COCOA_BEANS)
    );

    private static Def def(String id, String name, String effect, net.minecraft.world.item.Item base) {
        return new Def(id, name, new String[]{
                "§7Skyblock-style trinket",
                "§8Right-click to use",
                "§8Recipe: /recipe or custom crafter"
        }, base, effect);
    }

    public static List<String> allIds() {
        return DEFS.stream().map(d -> d.id).toList();
    }

    public static void registerItems() {
        for (Def d : DEFS) {
            if (CustomItemManager.getItem(d.id) != null) {
                continue;
            }
            CustomItemManager.registerItem(new CustomItemManager.CustomItem(
                    d.id, d.name, d.lore, ChatFormatting.AQUA, d.base,
                    Map.of("type", "sb_trinket", "effect", d.effect), true, 0));
        }
    }

    /** Call after {@link CustomItemManager#initialize} on server start. */
    public static void ensureRecipes() {
        List<SlayerRecipes.Recipe> out = new ArrayList<>();
        int n = 0;
        for (Def d : DEFS) {
            CustomItemManager.CustomItem ci = CustomItemManager.getItem(d.id);
            if (ci == null) {
                continue;
            }
            ItemStack result = ci.createStack(1);
            ItemStack[] g = recipeGrid(n++);
            boolean[] locked = new boolean[9];
            Arrays.fill(locked, true);
            out.add(new SlayerRecipes.Recipe(
                    "SB Trinket: " + d.id,
                    result,
                    g,
                    locked,
                    null,
                    0));
        }
        if (!out.isEmpty()) {
            RecipeConfigManager.upsertCustomRecipesSilent(out);
        }
    }

    /** Varied 3x3 grids (vanilla mats) — cycles for 30 items. */
    private static ItemStack[] recipeGrid(int index) {
        ItemStack E = ItemStack.EMPTY;
        ItemStack g = new ItemStack(Items.GOLD_NUGGET);
        ItemStack p = new ItemStack(Items.PRISMARINE_SHARD);
        ItemStack d = new ItemStack(Items.DIAMOND);
        ItemStack e = new ItemStack(Items.EMERALD);
        ItemStack l = new ItemStack(Items.LAPIS_LAZULI);
        ItemStack r = new ItemStack(Items.REDSTONE);
        ItemStack a = new ItemStack(Items.AMETHYST_SHARD);
        return switch (index % 6) {
            case 0 -> new ItemStack[]{g, p, g, p, d, p, g, p, g};
            case 1 -> new ItemStack[]{E, g, E, g, e, g, E, g, E};
            case 2 -> new ItemStack[]{p, p, p, p, l, p, p, p, p};
            case 3 -> new ItemStack[]{r, g, r, g, a, g, r, g, r};
            case 4 -> new ItemStack[]{g, g, g, p, d, p, g, g, g};
            default -> new ItemStack[]{l, r, l, r, e, r, l, r, l};
        };
    }

    public static boolean handleUse(ServerPlayer player, ItemStack stack, CustomItemManager.CustomItem item) {
        String effect = (String) item.properties.getOrDefault("effect", "");
        return applyEffect(player, stack, effect, item.name);
    }

    private static boolean applyEffect(ServerPlayer player, ItemStack stack, String effect, String itemName) {
        switch (effect) {
            case "luck_60" -> player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.LUCK, 1200, 0, true, true, true));
            case "haste_80" -> player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.HASTE, 1600, 0, true, true, true));
            case "speed_80" -> player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.SPEED, 1600, 0, true, true, true));
            case "coins_5000" -> CoinManager.giveCoinsQuiet(player, 5000);
            case "night_vision_200" -> player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.NIGHT_VISION, 4000, 0, true, true, true));
            case "regen_100" -> player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.REGENERATION, 2000, 0, true, true, true));
            case "jump_120" -> player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.JUMP_BOOST, 2400, 0, true, true, true));
            case "saturation_40" -> player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.SATURATION, 800, 1, true, true, true));
            case "resistance_100" -> player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.RESISTANCE, 2000, 0, true, true, true));
            case "water_breath_300" -> player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.WATER_BREATHING, 6000, 0, true, true, true));
            case "luck_120" -> player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.LUCK, 2400, 1, true, true, true));
            case "dolphins_grace_200" -> player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.DOLPHINS_GRACE, 4000, 0, true, true, true));
            case "fire_res_200" -> player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE, 4000, 0, true, true, true));
            case "strength_100" -> player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.STRENGTH, 2000, 0, true, true, true));
            case "slow_falling_160" -> player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.SLOW_FALLING, 3200, 0, true, true, true));
            case "coins_15000" -> CoinManager.giveCoinsQuiet(player, 15000);
            case "coins_25000" -> CoinManager.giveCoinsQuiet(player, 25000);
            case "xp_orb_burst" -> player.giveExperiencePoints(120);
            case "mastery_40_all" -> {
                for (MasteryManager.MasteryFamily f : MasteryManager.MasteryFamily.values()) {
                    MasteryManager.addXp(player, f, 40);
                }
            }
            case "mastery_combat_80" -> MasteryManager.addXp(player, MasteryManager.MasteryFamily.MELEE, 80);
            case "mastery_gather_80" -> MasteryManager.addXp(player, MasteryManager.MasteryFamily.GATHERING, 80);
            case "mastery_magic_80" -> MasteryManager.addXp(player, MasteryManager.MasteryFamily.MAGIC, 80);
            case "farming_saturation" -> player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.SATURATION, 600, 2, true, true, true));
            case "reach_hint" -> player.sendSystemMessage(
                    Component.literal("Tip: use /skills for progression hubs.").withStyle(ChatFormatting.YELLOW), false);
            case "weakness_aura_mobs" -> player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.STRENGTH, 3000, 0, true, true, true));
            case "coins_8000" -> CoinManager.giveCoinsQuiet(player, 8000);
            case "wither_skull_chance_flavor" -> player.sendSystemMessage(
                    Component.literal("You feel unlucky... or lucky?").withStyle(ChatFormatting.DARK_GRAY), false);
            case "regen_200" -> player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.REGENERATION, 4000, 0, true, true, true));
            case "open_skills_hint" -> {
                SkillsGui.openMainMenu(player);
                stack.shrink(1);
                player.sendSystemMessage(Component.literal("Opened Skills Hub").withStyle(ChatFormatting.GREEN), false);
                return true;
            }
            case "speed_haste_60" -> {
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.SPEED, 1200, 0, true, true, true));
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.HASTE, 1200, 0, true, true, true));
            }
            default -> {
                return false;
            }
        }
        stack.shrink(1);
        player.sendSystemMessage(Component.literal("Used " + itemName).withStyle(ChatFormatting.GREEN), false);
        return true;
    }
}
