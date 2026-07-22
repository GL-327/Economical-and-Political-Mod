package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Full per-material lore aligned with {@link BlockArmorHandler}, {@link BlockWeaponHandler},
 * and {@link BlockToolHandler} (no “see handler” shortcuts).
 */
public final class BlockGearLore {

    private BlockGearLore() {}

    private static final Set<String> STONE_FAMILY = Set.of(
            "basalt", "smooth_basalt", "polished_basalt",
            "granite", "polished_granite",
            "diorite", "polished_diorite",
            "andesite", "polished_andesite",
            "cobblestone", "mossy_cobblestone",
            "stone_bricks", "chiseled_stone_brick", "cracked_stone_brick",
            "tuff", "polished_tuff",
            "calcite", "brick", "dripstone", "smooth_stone"
    );
    private static final Set<String> DEEPSLATE_FAMILY = Set.of(
            "cobbled_deepslate", "polished_deepslate", "chiseled_deepslate",
            "deepslate_tile", "deepslate_brick", "reinforced_deepslate", "deepslate"
    );
    private static final Set<String> BLACKSTONE_FAMILY = Set.of(
            "polished_blackstone", "blackstone_bricks", "gilded_blackstone", "blackstone"
    );
    private static final Set<String> NETHER_FAMILY = Set.of(
            "nether_brick", "chiseled_nether_brick", "cracked_nether_brick",
            "crimson_stem", "nether_gold_ore", "nether_wart_block",
            "warped_wart_block", "netherite_block", "crying_obsidian", "shroomlight"
    );
    private static final Set<String> ICE_FAMILY = Set.of("packed_ice", "blue_ice", "snow_block", "snow");
    private static final Set<String> OCEAN_FAMILY = Set.of("prismarine_bricks", "dark_prismarine", "sea_lantern", "lodestone");
    private static final Set<String> END_FAMILY = Set.of("end_stone_brick", "end_stone", "purpur");
    /** red_sandstone armor uses {@link BlockArmorHandler#tickMiscVariants} (earth), not nature tick. */
    private static final Set<String> NATURE_FAMILY = Set.of(
            "bone_block", "bone_meal", "hay_block", "hay", "honeycomb_block", "honeycomb",
            "melon", "pumpkin", "lily_pad", "moss_block", "mycelium",
            "cactus", "target", "sponge", "sand"
    );
    private static final Set<String> WOOD_FAMILY = Set.of(
            "oak_log", "spruce_log", "birch_log", "dark_oak_log", "jungle_log",
            "acacia_log", "mangrove_log", "cherry_log",
            "oak_planks", "spruce_planks", "birch_planks", "jungle_planks",
            "acacia_planks", "dark_oak_planks", "mangrove_planks", "cherry_planks",
            "bamboo_planks", "crimson_planks", "warped_planks",
            "stripped_oak_log", "stripped_spruce_log", "stripped_birch_log",
            "stripped_dark_oak_log", "stripped_jungle_log", "stripped_acacia_log",
            "stripped_mangrove_log", "stripped_cherry_log",
            "stripped_bamboo_block", "stripped_crimson_stem", "stripped_warped_stem",
            "bamboo_block", "mangrove_roots", "muddy_mangrove_roots"
    );
    private static final Set<String> COPPER_FAMILY = Set.of(
            "copper_ingot", "chiseled_copper", "cut_copper", "waxed_cut_copper",
            "exposed_copper", "weathered_copper", "oxidised_copper"
    );
    private static final Set<String> NEW_25 = Set.of(
            "raw_prismarine", "clay", "glazed_terracotta", "concrete", "concrete_powder",
            "bookshelf", "barrel", "smithing_table", "anvil", "grindstone",
            "lectern", "composter", "stonecutter", "dried_kelp", "coral_block",
            "lantern", "chain", "lightning_rod", "tinted_glass", "candle",
            "sculk_sensor", "sculk_catalyst", "sculk_shrieker", "brewing_stand", "furnace"
    );
    /** Wood ids that {@link BlockToolHandler#onBlockBreak} actually routes to {@code handleWoodToolBreak}. */
    private static final Set<String> TOOL_WOOD_HANDLED = Set.of(
            "oak_log", "birch_log", "spruce_log", "dark_oak_log", "jungle_log", "acacia_log", "cherry_log", "mangrove_log",
            "stripped_oak_log", "stripped_birch_log", "stripped_spruce_log", "stripped_dark_oak_log", "stripped_jungle_log",
            "stripped_acacia_log", "stripped_cherry_log", "stripped_mangrove_log",
            "oak_planks", "birch_planks", "spruce_planks", "dark_oak_planks", "jungle_planks", "acacia_planks",
            "cherry_planks", "mangrove_planks", "bamboo_block", "mangrove_roots", "muddy_mangrove_roots"
    );
    private static final Set<String> MISC_TOOL_MATERIALS = Set.of(
            "iron_nugget", "gold_nugget", "lapis_lazuli", "amethyst_shard", "amethyst_cluster",
            "flint", "charcoal", "mud_brick", "mud_bricks", "raw_iron_block", "raw_gold_block",
            "verdant_froglight", "pearlescent_froglight", "ochre_froglight", "tnt", "soul_soil"
    );

    public static void appendArmorSetBonus(List<Component> lore, ChatFormatting headerColor, String prefix) {
        lore.add(Component.literal("✦ SET BONUS:").withStyle(headerColor));
        lore.add(Component.literal("  4+ block pieces (any mix): Resistance I (renewing)").withStyle(ChatFormatting.DARK_GRAY));
        String bonus = getShortSetBonus(prefix);
        if (!bonus.isEmpty()) {
            lore.add(Component.literal("  4/4 matching: " + bonus).withStyle(ChatFormatting.GREEN));
        }
    }

    public static void appendSwordArmorSynergy(List<Component> lore, String armorPrefix) {
        lore.add(Component.literal(""));
        lore.add(Component.literal("✦ ARMOR SYNERGY:").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal("  4+ pieces (any mix): Resistance I").withStyle(ChatFormatting.DARK_GRAY));
        String bonus = getShortSetBonus(armorPrefix);
        if (!bonus.isEmpty()) {
            lore.add(Component.literal("  4/4 matching: " + bonus).withStyle(ChatFormatting.GREEN));
        }
    }

    public static void appendSwordCombatPipelineNote(List<Component> lore) {
    }

    public static void appendToolHandlerSummary(List<Component> lore, String materialKey, String toolKind) {
        lore.add(Component.literal(""));
        String summary = getShortToolSummary(materialKey, toolKind);
        if (!summary.isEmpty()) {
            lore.add(Component.literal("⚒ " + summary).withStyle(ChatFormatting.BLUE));
        }
    }

    private static void line(List<Component> lore, String text, ChatFormatting color) {
        lore.add(Component.literal(text).withStyle(color));
    }

    private static String getShortSetBonus(String p) {
        // Hand-assigned unique bonus names take priority; generated signature is the fallback.
        String name = UniqueArmorSetBonuses.loreName(p);
        if (name != null) {
            return name + " — " + UniqueArmorSetBonuses.loreDescription(p);
        }
        return generatedArmorBonus(p);
    }

    private static String getShortToolSummary(String m, String toolKind) {
        return generatedToolBonus(m, toolKind != null ? toolKind : "tool");
    }

    private static String generatedArmorBonus(String p) {
        int sig = Math.floorMod(p.hashCode(), Integer.MAX_VALUE);
        String theme = loreTheme(p);
        int mode = (sig + theme.hashCode()) & 7;
        int radius = 2 + (sig % 3);
        int cadenceA = 30 + (sig % 53);
        int cadenceB = 46 + ((sig / 11) % 67);
        String core = switch (mode) {
            case 0 -> "Momentum Burst";
            case 1 -> "Suppression Aura";
            case 2 -> "Bastion Pulse";
            case 3 -> "Cinder Ward";
            case 4 -> "Tide Snare";
            case 5 -> "Umbral Veil";
            case 6 -> "War Rhythm";
            default -> "Sky Step";
        };
        String glass = p.toLowerCase(Locale.ROOT).contains("glass") ? " · flight (glass)" : "";
        return core + " · " + theme + " · r" + radius + " · ~" + cadenceA + "/" + cadenceB + "t" + glass;
    }

    private static String generatedToolBonus(String m, String toolKind) {
        int sig = Math.floorMod((m + "|" + toolKind + "|break").hashCode(), Integer.MAX_VALUE);
        String theme = loreTheme(m);
        int mode = (sig + theme.hashCode()) & 7;
        String setCore = switch (mode) {
            case 0 -> "Vein Link";
            case 1 -> "Cross Cleave";
            case 2 -> "Magnet Pulse";
            case 3 -> "Resonance";
            case 4 -> "Thermal Tap";
            case 5 -> "Dusk Pulse";
            case 6 -> "Verdant Mend";
            default -> "Arc Shock";
        };
        int pickMode = Math.floorMod(m.hashCode(), 5);
        String pickCore = switch (pickMode) {
            case 0 -> "Seismic";
            case 1 -> "Vein-Link";
            case 2 -> "Magnet";
            case 3 -> "Resonant";
            default -> "Thermal";
        };
        String pickLine = "pickaxe".equalsIgnoreCase(toolKind) ? " · pick: " + pickCore : "";
        return setCore + " (" + theme + ")" + pickLine;
    }

    /** Mirrors {@link BlockArmorHandler} resolveArmorTheme for lore text. */
    private static String loreTheme(String p) {
        String x = p.toLowerCase(Locale.ROOT);
        if (x.contains("glass")) return "glass";
        if (x.contains("redstone")) return "fire";
        if (x.contains("magma") || x.contains("nether") || x.contains("netherrack") || x.contains("blaze") || x.contains("furnace")) return "fire";
        if (x.contains("prismarine") || x.contains("water") || x.contains("coral") || x.contains("kelp") || x.contains("sponge") || x.contains("tidal")) return "water";
        if (x.contains("obsidian") || x.contains("stone") || x.contains("deepslate") || x.contains("concrete") || x.contains("clay") || x.contains("terracotta") || x.contains("dripstone")) return "earth";
        if (x.contains("iron") || x.contains("gold") || x.contains("copper") || x.contains("anvil") || x.contains("chain") || x.contains("smith") || x.contains("grindstone")) return "metal";
        if (x.contains("moss") || x.contains("wood") || x.contains("log") || x.contains("hay") || x.contains("honey") || x.contains("melon") || x.contains("pumpkin") || x.contains("mycelium")) return "nature";
        if (x.contains("sculk") || x.contains("soul") || x.contains("coal") || x.contains("blackstone") || x.contains("void") || x.contains("dark")) return "shadow";
        if (x.contains("glow") || x.contains("lantern") || x.contains("candle") || x.contains("quartz") || x.contains("lumen")) return "light";
        return "air";
    }

    private static void appendArmorDetailOnly(List<Component> lore, String p) {
        switch (p) {
            case "glass" -> line(lore, "• Glass set: 2pc Speed I → 3pc Speed II → 4pc Speed III; 4pc briefly flickers Invisibility.", ChatFormatting.GREEN);
            case "obsidian" -> {
                line(lore, "• Obsidian: 2pc Fire Res → 3pc Res I + Fire Res → 4pc Res II; 4pc burns nearby monsters for magic+fire.", ChatFormatting.GREEN);
                line(lore, "• On hurt frame: Absorption II + particles; quartz/amethyst full sets also proc on hurt separately.", ChatFormatting.DARK_GREEN);
            }
            case "quartz" -> line(lore, "• Quartz: 2pc Haste I → 3pc Strength I → 4pc Haste II + Strength II; on hurt: Strength III spike.", ChatFormatting.GREEN);
            case "glowstone" -> line(lore, "• Glowstone: 2pc Night Vision → 3pc Glowing (self) → 4pc Night Vision; 4pc tags nearby monsters with Glowing.", ChatFormatting.GREEN);
            case "redstone" -> line(lore, "• Redstone: 2pc Haste I → 3pc Haste II + Speed I → 4pc keeps high Haste tier.", ChatFormatting.GREEN);
            case "netherrack" -> line(lore, "• Netherrack: Fire Res tiers; 4pc in Nether gains periodic Strength II.", ChatFormatting.GREEN);
            case "endstone" -> line(lore, "• End stone: Slow Falling → 3pc Jump I; 4pc near void height can get short Levitation rescue.", ChatFormatting.GREEN);
            case "ice" -> line(lore, "• Ice: Speed + Res; clears extreme Slowness on you; 4pc frost aura Slow I on monsters + snowflakes.", ChatFormatting.GREEN);
            case "prismarine" -> line(lore, "• Prismarine: Water Breathing / Dolphin's Grace; in water at 4pc: Conduit Power + Speed + small heals.", ChatFormatting.GREEN);
            case "terracotta" -> line(lore, "• Terracotta: Resistance I → II → II (extended at 4pc).", ChatFormatting.GREEN);
            case "mossy" -> line(lore, "• Mossy: Regen I → II; 3pc+ in low light: Strength I.", ChatFormatting.GREEN);
            case "soul_sand" -> line(lore, "• Soul sand: Speed on soul soil/sand or at 4pc; 3pc Wither aura; 4pc Wither + Slowness aura + soul particles.", ChatFormatting.GREEN);
            case "magma" -> line(lore, "• Magma: Fire Res tiers; 4pc ignites and damages nearby monsters.", ChatFormatting.GREEN);
            case "sandstone" -> line(lore, "• Sandstone: Speed on sand or 4pc + Jump; 3pc+ high up: Slow Falling bursts.", ChatFormatting.GREEN);
            case "amethyst" -> line(lore, "• Amethyst: Res I → II → III; 4pc Weakness cloud on monsters; on hurt: chance to thorns-reflect + echo particles.", ChatFormatting.GREEN);
            case "coal" -> line(lore, "• Coal: Night Vision tiers; 4pc sneak: Smoke Screen blinds monsters (30s cooldown) + campfire smoke.", ChatFormatting.GREEN);
            case "wet_sponge" -> line(lore, "• Wet sponge: clears fire when burning; Fire Res tiers; 4pc in water: short Regen pulses.", ChatFormatting.GREEN);
            case "emerald", "emerald_block" -> line(lore, "• Emerald: Luck tiers → Luck II; 4pc Hero of the Village.", ChatFormatting.GREEN);
            case "diamond_block" -> line(lore, "• Diamond block: Resistance stacking; 4pc Absorption II upkeep.", ChatFormatting.GREEN);
            case "gold_block" -> line(lore, "• Gold block: Haste + Luck → Luck II at 4pc.", ChatFormatting.GREEN);
            case "copper_block" -> line(lore, "• Copper block: Speed + Jump; thunderstorms + sky visible: periodic Strength III + sparks.", ChatFormatting.GREEN);
            case "ancient_debris" -> line(lore, "• Ancient debris: Fire Res + Res mix; 4pc adds Strength.", ChatFormatting.GREEN);
            case "sculk" -> line(lore, "• Sculk: Night Vision; 3pc Darkness pulse on monsters; 4pc Res + shrieker-style sonic vs recent target.", ChatFormatting.GREEN);
            case "lapis", "lapis_block", "lapis_lazuli" -> line(lore, "• Lapis (prefix lapis*): Speed → Luck; 4pc Luck II.", ChatFormatting.GREEN);
            case "slime" -> line(lore, "• Slime: Jump + Slow Fall; 4pc cancels heavy fall with slime particles/sound.", ChatFormatting.GREEN);
            case "iron_block" -> appendMiscIronArmor(lore, p);
            default -> {
                if (STONE_FAMILY.contains(p)) appendStoneArmorFull(lore, p);
                else if (DEEPSLATE_FAMILY.contains(p)) appendDeepslateArmorFull(lore, p);
                else if (BLACKSTONE_FAMILY.contains(p)) appendBlackstoneArmorFull(lore, p);
                else if (NETHER_FAMILY.contains(p)) appendNetherArmorFull(lore, p);
                else if (ICE_FAMILY.contains(p)) appendIceArmorFull(lore, p);
                else if (OCEAN_FAMILY.contains(p)) appendOceanArmorFull(lore, p);
                else if (END_FAMILY.contains(p)) appendEndArmorFull(lore, p);
                else if (NATURE_FAMILY.contains(p)) appendNatureArmorFull(lore, p);
                else if (WOOD_FAMILY.contains(p)) appendWoodArmorFull(lore, p);
                else if (COPPER_FAMILY.contains(p)) appendCopperArmorFull(lore, p);
                else if (p.equals("iron_nugget") || p.equals("raw_iron_block")) appendMiscIronArmor(lore, p);
                else if (p.equals("gold_nugget") || p.equals("raw_gold_block")) appendMiscGoldArmor(lore, p);
                else if (p.equals("flint")) appendMiscEarthArmor(lore, "flint");
                else if (p.equals("charcoal")) appendMiscEarthArmor(lore, "charcoal");
                else if (p.equals("mud_brick") || p.equals("mud_bricks")) appendMiscEarthArmor(lore, "mud");
                else if (p.equals("soul_soil")) appendMiscEarthArmor(lore, "soul_soil");
                else if (p.equals("red_sandstone")) appendMiscEarthArmor(lore, "red_sandstone");
                else if (p.equals("tnt")) appendMiscEarthArmor(lore, "tnt");
                else if (p.equals("verdant_froglight")) appendFroglightArmor(lore, "verdant");
                else if (p.equals("pearlescent_froglight")) appendFroglightArmor(lore, "pearlescent");
                else if (p.equals("ochre_froglight")) appendFroglightArmor(lore, "ochre");
                else if (NEW_25.contains(p)) appendNew25ArmorFull(lore, p);
                else line(lore, "• No themed tick for id \"" + p + "\" — universal lines above only.", ChatFormatting.YELLOW);
            }
        }
    }

    private static void appendStoneArmorFull(List<Component> lore, String p) {
        line(lore, "• Stone family base: 2pc Resistance I → 3pc Strength I → 4pc Resistance II.", ChatFormatting.GREEN);
        String extra = switch (p) {
            case "basalt", "smooth_basalt", "polished_basalt" -> "4pc also: Fire Resistance.";
            case "granite", "polished_granite" -> "4pc also: Haste.";
            case "diorite", "polished_diorite" -> "4pc also: Absorption.";
            case "andesite", "polished_andesite" -> "4pc also: Speed.";
            case "cobblestone", "mossy_cobblestone" -> "4pc also: Regeneration.";
            case "stone_bricks", "chiseled_stone_brick", "cracked_stone_brick" -> "4pc also: Strength II.";
            case "tuff", "polished_tuff" -> "4pc also: Jump Boost.";
            case "calcite" -> "4pc also: Luck.";
            case "brick" -> "4pc also: Strength.";
            case "dripstone" -> "4pc: every 50 ticks magic damage to monsters in 3 blocks.";
            case "smooth_stone" -> "4pc also: Resistance II (extra instance).";
            default -> "";
        };
        line(lore, "• " + pretty(p) + ": " + extra, ChatFormatting.DARK_GREEN);
    }

    private static void appendDeepslateArmorFull(List<Component> lore, String p) {
        line(lore, "• Deepslate (" + pretty(p) + "): Y<0 → Night Vision + deeper Res; surface → lighter Res.", ChatFormatting.GREEN);
        line(lore, "• 4pc: Darkness on nearby monsters every 40 ticks; underground adds Res II + Strength.", ChatFormatting.GREEN);
        if ("reinforced_deepslate".equals(p)) {
            line(lore, "• Reinforced only: 4pc Absorption II upkeep.", ChatFormatting.DARK_GREEN);
        }
    }

    private static void appendBlackstoneArmorFull(List<Component> lore, String p) {
        line(lore, "• Blackstone (" + pretty(p) + "): in Nether 2pc Fire Res → 3pc Res → 4pc Strength + Wither pulse + soul flames.", ChatFormatting.GREEN);
        line(lore, "• Out of Nether: milder Resistance from 2pc.", ChatFormatting.DARK_GREEN);
        if ("gilded_blackstone".equals(p)) {
            line(lore, "• Gilded 4pc: extra Luck.", ChatFormatting.DARK_GREEN);
        }
    }

    private static void appendNetherArmorFull(List<Component> lore, String p) {
        line(lore, "• Nether brick-line (" + pretty(p) + "): always Fire Res; 3pc+ in Nether adds Strength.", ChatFormatting.GREEN);
        String four = switch (p) {
            case "netherite_block" -> "4pc: Resistance II + Strength II.";
            case "crying_obsidian" -> "4pc: Regen + Absorption + dripping tear particles.";
            case "crimson_stem" -> "4pc: Strength; in Nether also Speed.";
            case "nether_gold_ore" -> "4pc: Haste II + Luck.";
            case "shroomlight" -> "4pc: Night Vision + periodic Glowing on monsters.";
            case "nether_wart_block" -> "4pc: Regen; in Nether also Strength.";
            case "warped_wart_block" -> "4pc: Speed; in Nether also Resistance.";
            case "nether_brick", "chiseled_nether_brick", "cracked_nether_brick" ->
                    "4pc default: Strength + longer Fire Res.";
            default -> "4pc: Strength + Fire Res extension.";
        };
        line(lore, "• " + four, ChatFormatting.DARK_GREEN);
    }

    private static void appendIceArmorFull(List<Component> lore, String p) {
        line(lore, "• Ice/snow (" + pretty(p) + "): baseline Speed I; 3pc in cold biomes adds Resistance.", ChatFormatting.GREEN);
        line(lore, "• 4pc: Speed II + periodic Slow I on monsters + snowflakes.", ChatFormatting.GREEN);
        if ("blue_ice".equals(p)) {
            line(lore, "• Blue ice 4pc: extra Resistance II.", ChatFormatting.DARK_GREEN);
        }
    }

    private static void appendOceanArmorFull(List<Component> lore, String p) {
        line(lore, "• Ocean monument (" + pretty(p) + "): Water Breathing; 3pc+ in water Dolphin's Grace.", ChatFormatting.GREEN);
        line(lore, "• 4pc in water: Conduit Power + Speed.", ChatFormatting.GREEN);
        if ("sea_lantern".equals(p)) {
            line(lore, "• Sea lantern 3pc+: Night Vision.", ChatFormatting.DARK_GREEN);
        }
        if ("lodestone".equals(p)) {
            line(lore, "• Lodestone 4pc: Resistance I.", ChatFormatting.DARK_GREEN);
        }
    }

    private static void appendEndArmorFull(List<Component> lore, String p) {
        line(lore, "• End stone family (" + pretty(p) + "): Slow Falling → 3pc Jump → 4pc Resistance.", ChatFormatting.GREEN);
        line(lore, "• 4pc low HP: short Levitation + Resistance III + portal particles.", ChatFormatting.GREEN);
        if ("purpur".equals(p)) {
            line(lore, "• Purpur 4pc: extra Strength.", ChatFormatting.DARK_GREEN);
        }
    }

    private static void appendNatureArmorFull(List<Component> lore, String p) {
        line(lore, "• Nature (" + pretty(p) + "): 2pc Regen → 3pc Regen + Saturation ticks.", ChatFormatting.GREEN);
        String four = switch (p) {
            case "cactus" -> "4pc: thorn AOE damage to monsters every 40 ticks.";
            case "pumpkin" -> "4pc: Invisibility upkeep.";
            case "melon" -> "4pc: Regeneration II.";
            case "bone_block", "bone_meal" -> "4pc: Strength + Resistance.";
            case "hay", "hay_block" -> "4pc: longer Saturation.";
            case "honeycomb", "honeycomb_block" -> "4pc: Absorption + Regen.";
            case "lily_pad" -> "4pc: Speed; in water Dolphin's Grace.";
            case "moss_block" -> "4pc: Regeneration II.";
            case "mycelium" -> "4pc: Resistance; in darkness Strength.";
            case "target" -> "4pc: Speed II + Jump Boost.";
            case "sponge" -> "4pc: Water Breathing; heal in water.";
            case "sand" -> "4pc: Speed + Jump Boost.";
            default -> "4pc: stronger Regen.";
        };
        line(lore, "• " + four, ChatFormatting.DARK_GREEN);
    }

    private static void appendWoodArmorFull(List<Component> lore, String p) {
        line(lore, "• Wood (" + pretty(p) + "): Regen → Speed → Strength at 2/3/4pc.", ChatFormatting.GREEN);
        line(lore, "• Forest biome 4pc: Speed II + Regen II.", ChatFormatting.DARK_GREEN);
        if (p.contains("mangrove") || p.contains("muddy")) {
            line(lore, "• Mangrove/mud: 4pc Water Breathing while submerged.", ChatFormatting.DARK_GREEN);
        }
        if (p.contains("crimson") || p.contains("warped")) {
            line(lore, "• Crimson/warped stem/planks: 4pc Fire Res in Nether.", ChatFormatting.DARK_GREEN);
        }
    }

    private static void appendCopperArmorFull(List<Component> lore, String p) {
        line(lore, "• Copper (" + pretty(p) + "): Speed → Haste → Speed II; thunder + sky: periodic Strength III + sparks.", ChatFormatting.GREEN);
        if ("oxidised_copper".equals(p)) {
            line(lore, "• Oxidised 4pc: Resistance.", ChatFormatting.DARK_GREEN);
        }
        if ("waxed_cut_copper".equals(p)) {
            line(lore, "• Waxed cut 4pc: Absorption.", ChatFormatting.DARK_GREEN);
        }
    }

    private static void appendMiscIronArmor(List<Component> lore, String p) {
        line(lore, "• Iron misc (" + pretty(p) + "): 2pc Res → 3pc Strength → 4pc Res II.", ChatFormatting.GREEN);
        if ("raw_iron_block".equals(p)) {
            line(lore, "• Raw iron block 4pc: Haste II.", ChatFormatting.DARK_GREEN);
        }
    }

    private static void appendMiscGoldArmor(List<Component> lore, String p) {
        line(lore, "• Gold misc (" + pretty(p) + "): 2pc Haste → 3pc Luck → 4pc Luck II.", ChatFormatting.GREEN);
        if ("raw_gold_block".equals(p)) {
            line(lore, "• Raw gold block 4pc: extra Haste II.", ChatFormatting.DARK_GREEN);
        }
    }

    private static void appendMiscEarthArmor(List<Component> lore, String kind) {
        line(lore, "• Earth misc (" + kind + "): 2pc Res → 3pc Strength → 4pc themed bonus.", ChatFormatting.GREEN);
        switch (kind) {
            case "flint" -> line(lore, "• Flint 4pc: Strength II + Haste.", ChatFormatting.DARK_GREEN);
            case "charcoal" -> line(lore, "• Charcoal 4pc: Night Vision + Fire Res.", ChatFormatting.DARK_GREEN);
            case "mud" -> line(lore, "• Mud brick 4pc: Resistance II + Regen.", ChatFormatting.DARK_GREEN);
            case "soul_soil" -> line(lore, "• Soul soil 4pc: Speed + Wither aura + soul particles.", ChatFormatting.DARK_GREEN);
            case "red_sandstone" -> line(lore, "• Red sandstone 4pc: Speed II + Jump Boost.", ChatFormatting.DARK_GREEN);
            case "tnt" -> line(lore, "• TNT 4pc: Speed II + periodic explosion damage to monsters.", ChatFormatting.DARK_GREEN);
        }
    }

    private static void appendFroglightArmor(List<Component> lore, String which) {
        line(lore, "• Froglight (" + which + "): 2pc Night Vision → 3pc Speed + glowing monsters → 4pc branch bonus.", ChatFormatting.GREEN);
        switch (which) {
            case "verdant" -> line(lore, "• Verdant 4pc: Regen + Jump Boost II.", ChatFormatting.DARK_GREEN);
            case "pearlescent" -> line(lore, "• Pearlescent 4pc: Luck + Absorption.", ChatFormatting.DARK_GREEN);
            case "ochre" -> line(lore, "• Ochre 4pc: Haste II + Strength.", ChatFormatting.DARK_GREEN);
        }
    }

    private static void appendNew25ArmorFull(List<Component> lore, String p) {
        switch (p) {
            case "raw_prismarine", "coral_block", "dried_kelp" -> {
                line(lore, "• " + pretty(p) + ": Water Breathing; 3pc in water Dolphin's Grace; 4pc in water Conduit + Speed.", ChatFormatting.GREEN);
            }
            case "clay", "concrete_powder" -> {
                line(lore, "• " + pretty(p) + ": 2pc Res → 3pc Strength → 4pc Res II.", ChatFormatting.GREEN);
            }
            case "glazed_terracotta", "concrete" -> {
                line(lore, "• " + pretty(p) + ": Res I → II → II + Strength at 4pc.", ChatFormatting.GREEN);
            }
            case "bookshelf", "lectern" -> {
                line(lore, "• " + pretty(p) + ": Luck → 3pc Haste → 4pc Luck II + Speed.", ChatFormatting.GREEN);
            }
            case "barrel", "composter" -> {
                line(lore, "• " + pretty(p) + ": Regen → 3pc Saturation → 4pc Regen + Luck.", ChatFormatting.GREEN);
            }
            case "smithing_table", "anvil", "grindstone", "stonecutter" -> {
                line(lore, "• " + pretty(p) + ": 2pc Res → 3pc Haste + Strength → 4pc Res II.", ChatFormatting.GREEN);
                if ("anvil".equals(p)) {
                    line(lore, "• Anvil 4pc: also Absorption.", ChatFormatting.DARK_GREEN);
                }
            }
            case "lantern", "candle" -> {
                line(lore, "• " + pretty(p) + ": Night Vision → 3pc Speed + glowing monsters → 4pc Regen.", ChatFormatting.GREEN);
            }
            case "chain" -> {
                line(lore, "• Chain: Res → 3pc Strength → 4pc Res II + Speed.", ChatFormatting.GREEN);
            }
            case "lightning_rod" -> {
                line(lore, "• Lightning rod: Speed → 3pc Haste → 4pc Speed II; thunder + sky: Strength III bursts.", ChatFormatting.GREEN);
            }
            case "tinted_glass" -> {
                line(lore, "• Tinted glass: Speed I → II → III; 4pc periodic Invisibility flicker.", ChatFormatting.GREEN);
            }
            case "sculk_sensor", "sculk_catalyst", "sculk_shrieker" -> {
                line(lore, "• " + pretty(p) + ": Night Vision; 3pc Darkness on monsters; 4pc Resistance.", ChatFormatting.GREEN);
                if ("sculk_shrieker".equals(p)) {
                    line(lore, "• Shrieker 4pc: sonic boom vs last hurt mob in range.", ChatFormatting.DARK_GREEN);
                }
            }
            case "brewing_stand" -> {
                line(lore, "• Brewing stand: Regen → 3pc Speed + Res → 4pc Regen II + Strength.", ChatFormatting.GREEN);
            }
            case "furnace" -> {
                line(lore, "• Furnace: Fire Res → 3pc Haste → 4pc Fire Res + Strength + ignite monsters in radius.", ChatFormatting.GREEN);
            }
            default -> line(lore, "• " + pretty(p) + ": fallback Res → Strength → Res II.", ChatFormatting.YELLOW);
        }
    }

    private static String pretty(String id) {
        return id.replace("_", " ");
    }

    private static void appendToolBreakAndMeleeDetail(List<Component> lore, String m, String k) {
        switch (m) {
            case "glass" -> {
                line(lore, "• Break: ~28% double drops; ~10% bonus raw iron/gold/diamond pop; shift+shovel 3×3 shovel-suitable ring; hoe spawns end rods.", ChatFormatting.GREEN);
                line(lore, "• Melee: 20% extra +3 player damage + Shatter; crit particles.", ChatFormatting.GREEN);
            }
            case "obsidian" -> {
                line(lore, "• Break: pickaxe on bedrock 10% break + explosion; ore fortune-style; auto-smelt drops; shift+shovel 5×5 dig; axe strips logs.", ChatFormatting.GREEN);
                line(lore, "• Melee: knockback 0.8 + +2 damage + crit particles.", ChatFormatting.GREEN);
            }
            case "quartz" -> {
                line(lore, "• Break: pickaxe end-rod particles; 1–3 XP orbs per break.", ChatFormatting.GREEN);
                line(lore, "• Melee: every 3rd hit +6 damage + enchant particles + Critical Strike.", ChatFormatting.GREEN);
            }
            case "terracotta" -> {
                line(lore, "• Break: message when breaking terracotta/clay.", ChatFormatting.GREEN);
                line(lore, "• Melee: knockback 1.0; on ground +2.5 damage + Grounded Strike.", ChatFormatting.GREEN);
            }
            case "mossy" -> {
                line(lore, "• Break: pickaxe 10% converts adjacent stone → mossy cobble.", ChatFormatting.GREEN);
                line(lore, "• Melee: Poison I 60t + spore blossom air.", ChatFormatting.GREEN);
            }
            case "sandstone" -> {
                line(lore, "• Break: message when breaking sand/sandstone.", ChatFormatting.GREEN);
                line(lore, "• Melee: self Speed I 40t + dust plume.", ChatFormatting.GREEN);
            }
            case "magma" -> {
                line(lore, "• Break: Fire Resistance 100t in Nether.", ChatFormatting.GREEN);
                line(lore, "• Melee: 80 fire ticks; in Nether +3 damage + Nether Blaze + flames.", ChatFormatting.GREEN);
            }
            case "cactus" -> {
                line(lore, "• Break: same nature regen line as other nature tools (Regen 60t + happy villager).", ChatFormatting.GREEN);
                line(lore, "• Melee: +2 cactus damage + Thorns.", ChatFormatting.GREEN);
            }
            case "tnt" -> {
                line(lore, "• Break: 3% mining blast — explosion particles, 4 damage to mobs in 3 blocks.", ChatFormatting.GREEN);
                line(lore, "• Melee: 5% explosion hit (3 damage) + explode particles.", ChatFormatting.GREEN);
            }
            case "diamond_block" -> {
                line(lore, "• Break: none (only global periodic Haste).", ChatFormatting.DARK_GREEN);
                line(lore, "• Melee: +3 damage + enchant particles.", ChatFormatting.GREEN);
            }
            case "iron_block" -> {
                line(lore, "• Break: none.", ChatFormatting.DARK_GREEN);
                line(lore, "• Melee: +2 damage + Weakness I 60t + crit particles.", ChatFormatting.GREEN);
            }
            case "gold_block" -> {
                line(lore, "• Break: none.", ChatFormatting.DARK_GREEN);
                line(lore, "• Melee: 10% 1–3 coins.", ChatFormatting.GREEN);
            }
            case "copper_block" -> {
                line(lore, "• Break: none.", ChatFormatting.DARK_GREEN);
                line(lore, "• Melee: self Speed I 40t + electric sparks on you.", ChatFormatting.GREEN);
            }
            case "glowstone" -> {
                line(lore, "• Break: 30% +2 glowstone dust on glowstone block; Night Vision 200t.", ChatFormatting.GREEN);
                line(lore, "• Melee: vs undead +3.5 + end rod particles.", ChatFormatting.GREEN);
            }
            case "redstone" -> {
                line(lore, "• Break: pickaxe on ore spawns crit particles; Haste II 100t.", ChatFormatting.GREEN);
                line(lore, "• Melee: Slowness III 40t + crit particles.", ChatFormatting.GREEN);
            }
            case "netherrack" -> {
                line(lore, "• Break: in Nether Fire Res 100t + chat.", ChatFormatting.GREEN);
                line(lore, "• Melee: 60 fire ticks + flame particles.", ChatFormatting.GREEN);
            }
            case "endstone" -> {
                line(lore, "• Break: pickaxe/shovel portal particles; 5% short-range forward teleport if air (Void Shift).", ChatFormatting.GREEN);
                line(lore, "• Melee: vs ender-tagged mobs +5 damage.", ChatFormatting.GREEN);
            }
            case "prismarine" -> {
                line(lore, "• Break: underwater Water Breathing 200t + message.", ChatFormatting.GREEN);
                line(lore, "• Melee: vs ocean-tagged mobs +5 damage.", ChatFormatting.GREEN);
            }
            case "soul_sand" -> {
                line(lore, "• Break: on soul sand/soil chat; Wither I 40t in 3 blocks + soul particles if mobs hit.", ChatFormatting.GREEN);
                line(lore, "• Melee: Slowness II 60t + Wither I 40t.", ChatFormatting.GREEN);
            }
            case "coal" -> {
                line(lore, "• Break: 50% bonus coal on coal ores; Night Vision 200t.", ChatFormatting.GREEN);
                line(lore, "• Melee: Blindness 40t.", ChatFormatting.GREEN);
            }
            case "emerald" -> {
                line(lore, "• Break: pickaxe on ore 35% coins; mature crop hoe → Luck II; 5% coin find.", ChatFormatting.GREEN);
                line(lore, "• Melee: 22% coins; always Weakness I 50t.", ChatFormatting.GREEN);
            }
            case "lapis" -> {
                line(lore, "• Break: pickaxe on ore 40% XP burst; all ores drip bonus XP orbs.", ChatFormatting.GREEN);
                line(lore, "• Melee: +4 magic damage + Slowness I 55t.", ChatFormatting.GREEN);
            }
            case "amethyst" -> {
                line(lore, "• Break: cluster 25% +2 shards; Regen I 60t.", ChatFormatting.GREEN);
                line(lore, "• Melee: 28% sonic boom (6 damage) + warden sound.", ChatFormatting.GREEN);
            }
            case "ice" -> {
                line(lore, "• Break: pickaxe turns adjacent water→ice, lava→obsidian.", ChatFormatting.GREEN);
                line(lore, "• Melee: Slowness II 60t + snowflakes.", ChatFormatting.GREEN);
            }
            case "sculk" -> {
                line(lore, "• Break: mobs within 4 blocks get Darkness 40t; sculk charge pop + Sculk Pulse if any.", ChatFormatting.GREEN);
                line(lore, "• Melee: none in tool handler.", ChatFormatting.DARK_GREEN);
            }
            case "warped" -> {
                line(lore, "• Break: none routed for id warped (material key from “Warped” tools).", ChatFormatting.YELLOW);
                line(lore, "• Melee: Nausea 40t + warped spore particles.", ChatFormatting.GREEN);
            }
            default -> appendToolBreakMeleeTail(lore, m, k);
        }
    }

    private static void appendNatureToolSharedBreak(List<Component> lore) {
        line(lore, "• Break: Regen I 60t + happy villager particles (all nature bucket materials).", ChatFormatting.GREEN);
    }

    private static void appendNatureToolMelee(List<Component> lore, String m) {
        switch (m) {
            case "hay", "hay_block" -> line(lore, "• Melee: knockback 1.2 + dust plume + Bale Bash.", ChatFormatting.GREEN);
            case "melon" -> line(lore, "• Melee: Poison 25t; 15% drops melon slice at target feet.", ChatFormatting.GREEN);
            case "pumpkin" -> line(lore, "• Melee: Blindness 30t + smoke + Pumpkin Smash.", ChatFormatting.GREEN);
            default -> line(lore, "• Melee: Poison 25t + spore blossom air (bone/honeycomb/lily/moss/mycelium/target/sponge/sand/red_sandstone).", ChatFormatting.GREEN);
        }
    }

    private static void appendNew25ToolBreakMelee(List<Component> lore, String m, String k) {
        switch (m) {
            case "raw_prismarine", "coral_block", "dried_kelp" -> {
                line(lore, "• Break: underwater Water Breathing 200t + Haste 60t; bubble particles.", ChatFormatting.GREEN);
                line(lore, "• Melee: Mining Fatigue 40t; underwater +2 damage + Tidal Strike + bubbles.", ChatFormatting.GREEN);
            }
            case "clay", "concrete_powder" -> {
                line(lore, "• Break: Haste 60t + dust plume.", ChatFormatting.GREEN);
                line(lore, "• Melee: Slowness II 40t + heavy dust plume.", ChatFormatting.GREEN);
            }
            case "glazed_terracotta", "concrete" -> {
                line(lore, "• Break: Haste 60t + Resistance 40t + crit particles.", ChatFormatting.GREEN);
                line(lore, "• Melee: knockback 0.8 + +1.5 damage + crit burst.", ChatFormatting.GREEN);
            }
            case "bookshelf", "lectern" -> {
                line(lore, "• Break: Luck 100t; XP orbs + enchant particles.", ChatFormatting.GREEN);
                line(lore, "• Melee: 15% +3 magic + enchant particles + Arcane Pulse.", ChatFormatting.GREEN);
            }
            case "barrel", "composter" -> {
                line(lore, "• Break: Regen 40t; 5% coin; happy villager particles.", ChatFormatting.GREEN);
                line(lore, "• Melee: Poison 25t; you heal 0.5 hearts.", ChatFormatting.GREEN);
            }
            case "smithing_table", "grindstone", "stonecutter" -> {
                line(lore, "• Break: Haste II 80t; pickaxe on ore 25% double drop + Refined ore; crit particles.", ChatFormatting.GREEN);
                line(lore, "• Melee: +2 damage + Weakness 40t + crit particles.", ChatFormatting.GREEN);
            }
            case "anvil" -> {
                line(lore, "• Break: Haste II 60t + Resistance 40t + crit particles.", ChatFormatting.GREEN);
                line(lore, "• Melee: +3 damage + knockback 1.2 + Crushing Blow + crit burst.", ChatFormatting.GREEN);
            }
            case "lantern", "candle" -> {
                line(lore, "• Break: Night Vision 200t + flame particles.", ChatFormatting.GREEN);
                line(lore, "• Melee: Glowing 60t + 40 fire ticks + flame particles.", ChatFormatting.GREEN);
            }
            case "chain" -> {
                line(lore, "• Break: Haste 60t + Resistance 40t + crit particles.", ChatFormatting.GREEN);
                line(lore, "• Melee: Slowness II 50t + +1.5 damage + Shackled.", ChatFormatting.GREEN);
            }
            case "lightning_rod" -> {
                line(lore, "• Break: Haste 60t + Speed 40t + electric sparks.", ChatFormatting.GREEN);
                line(lore, "• Melee: 10% +5 magic + heavy sparks + Lightning Strike.", ChatFormatting.GREEN);
            }
            case "tinted_glass" -> {
                line(lore, "• Break: 25% double drops + smoke particles.", ChatFormatting.GREEN);
                line(lore, "• Melee: Darkness 30t; 20% +3 damage + Shadow Shard.", ChatFormatting.GREEN);
            }
            case "sculk_sensor", "sculk_catalyst", "sculk_shrieker" -> {
                line(lore, "• Break: Night Vision 100t; mobs in 4 blocks Darkness 40t + sculk charge pop if any.", ChatFormatting.GREEN);
                line(lore, "• Melee: Darkness 40t; 10% sonic boom 4 damage + sonic boom particle.", ChatFormatting.GREEN);
            }
            case "brewing_stand" -> {
                line(lore, "• Break: Regen 60t + Speed 40t + happy villager.", ChatFormatting.GREEN);
                line(lore, "• Melee: random Poison / Weakness / Slowness II (equal thirds).", ChatFormatting.GREEN);
            }
            case "furnace" -> {
                line(lore, "• Break: Fire Res 100t + auto-smelt eligible blocks + flame particles.", ChatFormatting.GREEN);
                line(lore, "• Melee: 80 fire ticks + +1.5 damage + flame burst.", ChatFormatting.GREEN);
            }
            default -> line(lore, "• Break/melee: unlisted new-25 id \"" + m + "\".", ChatFormatting.YELLOW);
        }
    }

    private static void appendMiscToolBreakMelee(List<Component> lore, String m) {
        switch (m) {
            case "iron_nugget" -> {
                line(lore, "• Break: Haste 40t.", ChatFormatting.GREEN);
                line(lore, "• Melee: +0.5 damage + crit particles.", ChatFormatting.GREEN);
            }
            case "gold_nugget" -> {
                line(lore, "• Break: 5% +1 coin + Nugget find.", ChatFormatting.GREEN);
                line(lore, "• Melee: 8% +1 coin.", ChatFormatting.GREEN);
            }
            case "lapis_lazuli" -> {
                line(lore, "• Break: mining lapis on ore spawns 1–3 XP.", ChatFormatting.GREEN);
                line(lore, "• Melee: +2 magic + enchant particles.", ChatFormatting.GREEN);
            }
            case "amethyst_shard" -> {
                line(lore, "• Break: Regen 40t + end rod particles.", ChatFormatting.GREEN);
                line(lore, "• Melee: 20% sonic boom 3 + amethyst chime.", ChatFormatting.GREEN);
            }
            case "amethyst_cluster" -> {
                line(lore, "• Break: Regen 40t + end rod particles.", ChatFormatting.GREEN);
                line(lore, "• Melee: 15% sonic boom 4.5 + chime + sonic boom particle.", ChatFormatting.GREEN);
            }
            case "flint" -> {
                line(lore, "• Break: Haste 40t + crit particles.", ChatFormatting.GREEN);
                line(lore, "• Melee: +1 damage; 10% set target on fire 40t + Spark.", ChatFormatting.GREEN);
            }
            case "charcoal" -> {
                line(lore, "• Break: Night Vision 100t + smoke.", ChatFormatting.GREEN);
                line(lore, "• Melee: 40 fire ticks + Blindness 20t + smoke.", ChatFormatting.GREEN);
            }
            case "mud_brick", "mud_bricks" -> {
                line(lore, "• Break: Haste 40t.", ChatFormatting.GREEN);
                line(lore, "• Melee: Slowness 30t + dust plume.", ChatFormatting.GREEN);
            }
            case "raw_iron_block" -> {
                line(lore, "• Break: Haste 60t + Resistance 40t.", ChatFormatting.GREEN);
                line(lore, "• Melee: +2 damage + knockback 0.6.", ChatFormatting.GREEN);
            }
            case "raw_gold_block" -> {
                line(lore, "• Break: Luck 100t; 5% coin + Gold find.", ChatFormatting.GREEN);
                line(lore, "• Melee: +1.5 damage; 10% 1–2 coins + Gold strike.", ChatFormatting.GREEN);
            }
            case "verdant_froglight" -> {
                line(lore, "• Break: Night Vision 100t + Speed 40t + glow particles.", ChatFormatting.GREEN);
                line(lore, "• Melee: Poison 30t + happy villager particles.", ChatFormatting.GREEN);
            }
            case "pearlescent_froglight" -> {
                line(lore, "• Break: Night Vision 100t + Speed 40t + glow particles.", ChatFormatting.GREEN);
                line(lore, "• Melee: Levitation 10t + end rod particles.", ChatFormatting.GREEN);
            }
            case "ochre_froglight" -> {
                line(lore, "• Break: Night Vision 100t + Speed 40t + glow particles.", ChatFormatting.GREEN);
                line(lore, "• Melee: Glowing 40t + glow particles.", ChatFormatting.GREEN);
            }
            case "soul_soil" -> {
                line(lore, "• Break: Speed 60t + soul particles.", ChatFormatting.GREEN);
                line(lore, "• Melee: Wither 30t + Slowness 30t + soul particles.", ChatFormatting.GREEN);
            }
            default -> line(lore, "• Misc id \"" + m + "\" not mapped.", ChatFormatting.YELLOW);
        }
    }

    private static void appendToolBreakMeleeTail(List<Component> lore, String m, String k) {
        if (NEW_25.contains(m)) {
            appendNew25ToolBreakMelee(lore, m, k);
            return;
        }
        if (MISC_TOOL_MATERIALS.contains(m) && !"tnt".equals(m)) {
            appendMiscToolBreakMelee(lore, m);
            return;
        }
        if (TOOL_WOOD_HANDLED.contains(m)) {
            line(lore, "• Break: Speed 60t + crit particles.", ChatFormatting.GREEN);
            line(lore, "• Melee: Poison 15t + crit particles.", ChatFormatting.GREEN);
            return;
        }
        if (WOOD_FAMILY.contains(m)) {
            line(lore, "• Break/melee: id \"" + m + "\" is not in BlockToolHandler’s wood switch — only global Haste applies.", ChatFormatting.YELLOW);
            return;
        }
        if (STONE_FAMILY.contains(m)) {
            line(lore, "• Break: Haste 60t + crit particles.", ChatFormatting.GREEN);
            line(lore, "• Melee: +1.5 damage + Slowness 30t + crit particles.", ChatFormatting.GREEN);
            return;
        }
        if (DEEPSLATE_FAMILY.contains(m)) {
            line(lore, "• Break: Haste II if broken block Y<0 else Haste I; Night Vision 100t; sculk soul particles.", ChatFormatting.GREEN);
            line(lore, "• Melee: +2 damage if your Y<0 else +0.5 + Darkness 30t + sculk soul.", ChatFormatting.GREEN);
            return;
        }
        if (BLACKSTONE_FAMILY.contains(m)) {
            line(lore, "• Break: in Nether Fire Res 100t + Nether Ward; always smoke particles.", ChatFormatting.GREEN);
            line(lore, "• Melee: +0.5 (overworld) or +2 (Nether) + Blindness 25t + smoke.", ChatFormatting.GREEN);
            return;
        }
        if (NETHER_FAMILY.contains(m)) {
            line(lore, "• Break: Fire Res 100t + flames; nether_gold_ore only: 5% +1 coin + Nether coin.", ChatFormatting.GREEN);
            line(lore, "• Melee: 60 fire ticks; in Nether +1.5 + Nether Fury + flames.", ChatFormatting.GREEN);
            return;
        }
        if (ICE_FAMILY.contains(m)) {
            line(lore, "• Break: Speed I 100t + snowflakes.", ChatFormatting.GREEN);
            line(lore, "• Melee: Slowness II 50t + snowflakes.", ChatFormatting.GREEN);
            return;
        }
        if (OCEAN_FAMILY.contains(m)) {
            line(lore, "• Break: underwater Water Breathing 200t + Ocean's Blessing; bubbles.", ChatFormatting.GREEN);
            line(lore, "• Melee: Mining Fatigue 40t; underwater +2 + Tidal Strike + bubbles.", ChatFormatting.GREEN);
            return;
        }
        if (END_FAMILY.contains(m)) {
            line(lore, "• Break: Slow Falling 100t; 3% short teleport + Phase Shift; portal particles.", ChatFormatting.GREEN);
            line(lore, "• Melee: 5% Levitation 15t + portal + Void Launch.", ChatFormatting.GREEN);
            return;
        }
        if (NATURE_FAMILY.contains(m) || "red_sandstone".equals(m)) {
            appendNatureToolSharedBreak(lore);
            appendNatureToolMelee(lore, m);
            return;
        }
        if (COPPER_FAMILY.contains(m)) {
            line(lore, "• Break: Haste 60t + Speed 40t + electric sparks.", ChatFormatting.GREEN);
            line(lore, "• Melee: self Speed 40t + target Slowness 40t + sparks on target.", ChatFormatting.GREEN);
            return;
        }
        if ("warped_stem".equals(m)) {
            line(lore, "• Break/melee: id warped_stem is not listed in BlockToolHandler — only global Haste.", ChatFormatting.YELLOW);
            return;
        }
        line(lore, "• Break/melee: no matching case for \"" + m + "\" — verify custom tool id vs BlockToolHandler.", ChatFormatting.YELLOW);
    }
}
