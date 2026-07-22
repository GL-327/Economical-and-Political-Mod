package com.political;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

/** Fifteen additional legendary dragon chestplates (see {@link MoreDragonChestplateAbilities}). */
public final class MoreDragonChestplates {

    private MoreDragonChestplates() {
    }

    private static ItemStack base(String title, ChatFormatting nameColor, int dyeRgb, String customId,
                                  int armor, int toughness, List<Component> abilityLines) {
        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        chestplate.set(DataComponents.CUSTOM_NAME, Component.literal(title).withStyle(nameColor, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[Legendary Dragon Armor]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("🛡 Armour: " + armor).withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Toughness: " + toughness).withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ DRAGON ABILITY ━━━").withStyle(nameColor));
        lore.addAll(abilityLines);
        lore.add(Component.literal(""));
        chestplate.set(DataComponents.LORE, new ItemLore(lore));
        chestplate.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        chestplate.set(DataComponents.MAX_DAMAGE, 50000);
        chestplate.set(DataComponents.DAMAGE, 0);
        chestplate.set(DataComponents.DYED_COLOR, new DyedItemColor(dyeRgb));
        ItemAttributeModifiers.Builder b = ItemAttributeModifiers.builder();
        b.add(Attributes.ARMOR, new AttributeModifier(
                Identifier.fromNamespaceAndPath("politicalserver", customId + "_armor"),
                armor,
                AttributeModifier.Operation.ADD_VALUE
        ), EquipmentSlotGroup.CHEST);
        b.add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(
                Identifier.fromNamespaceAndPath("politicalserver", customId + "_tough"),
                toughness,
                AttributeModifier.Operation.ADD_VALUE
        ), EquipmentSlotGroup.CHEST);
        chestplate.set(DataComponents.ATTRIBUTE_MODIFIERS, b.build());
        DragonChestplates.setCustomItemId(chestplate, customId);
        return chestplate;
    }

    private static List<Component> L(Component... lines) {
        return List.of(lines);
    }

    public static ItemStack createDune() {
        return base("🏜 Dune Wyrm Chestplate", ChatFormatting.GOLD, 0xC2B280, "dune_dragon_chestplate", 17, 11,
                L(Component.literal("✦ Desert flight: hunger drains while airborne").withStyle(ChatFormatting.YELLOW),
                        Component.literal("✦ Speed & haste on sand").withStyle(ChatFormatting.GREEN),
                        Component.literal("✦ Mining fatigue away from sand").withStyle(ChatFormatting.RED)));
    }

    public static ItemStack createReef() {
        return base("🐚 Reef Leviathan Chestplate", ChatFormatting.AQUA, 0x20B2AA, "reef_dragon_chestplate", 17, 11,
                L(Component.literal("✦ Water breathing & dolphin grace in water").withStyle(ChatFormatting.AQUA),
                        Component.literal("✦ Flight only above sea level — drowns faster if starving").withStyle(ChatFormatting.YELLOW),
                        Component.literal("✦ Slowness on dry land (heavy scales)").withStyle(ChatFormatting.RED)));
    }

    public static ItemStack createEmber() {
        return base("🔥 Ember Drakeling Chestplate", ChatFormatting.RED, 0xFF4500, "ember_dragon_chestplate", 18, 12,
                L(Component.literal("✦ Short flight: burns you unless sneaking").withStyle(ChatFormatting.GOLD),
                        Component.literal("✦ Fire affinity + strength when not burning").withStyle(ChatFormatting.RED),
                        Component.literal("✦ Ash trail (cosmetic)").withStyle(ChatFormatting.DARK_GRAY)));
    }

    public static ItemStack createCrystal() {
        return base("💎 Crystal Drake Chestplate", ChatFormatting.LIGHT_PURPLE, 0xE0B0FF, "crystal_dragon_chestplate", 19, 13,
                L(Component.literal("✦ No flight — prism shield").withStyle(ChatFormatting.LIGHT_PURPLE),
                        Component.literal("✦ Resistance + thorns-style chip damage to attackers").withStyle(ChatFormatting.AQUA),
                        Component.literal("✦ Shatter sparks at feet").withStyle(ChatFormatting.GRAY)));
    }

    public static ItemStack createShadow() {
        return base("🌑 Shadow Wyrm Chestplate", ChatFormatting.DARK_GRAY, 0x2F2F2F, "shadow_dragon_chestplate", 18, 12,
                L(Component.literal("✦ Night flight: weakness I while flying").withStyle(ChatFormatting.DARK_PURPLE),
                        Component.literal("✦ Speed & darkness immunity at low light").withStyle(ChatFormatting.GRAY),
                        Component.literal("✦ Glowing in direct sunlight").withStyle(ChatFormatting.YELLOW)));
    }

    public static ItemStack createSolar() {
        return base("☀ Solar Serpent Chestplate", ChatFormatting.YELLOW, 0xFFD700, "solar_dragon_chestplate", 18, 12,
                L(Component.literal("✦ Day flight: hunger cost + brief nausea when landing hard").withStyle(ChatFormatting.GOLD),
                        Component.literal("✦ Strength in daylight").withStyle(ChatFormatting.RED),
                        Component.literal("✦ Slowness I at night").withStyle(ChatFormatting.BLUE)));
    }

    public static ItemStack createLunar() {
        return base("🌙 Lunar Serpent Chestplate", ChatFormatting.BLUE, 0x4169E1, "lunar_dragon_chestplate", 18, 12,
                L(Component.literal("✦ Night-only creative flight").withStyle(ChatFormatting.AQUA),
                        Component.literal("✦ Regeneration under moonlight — weakness by day").withStyle(ChatFormatting.LIGHT_PURPLE),
                        Component.literal("✦ Star wisps behind you").withStyle(ChatFormatting.GRAY)));
    }

    public static ItemStack createTerra() {
        return base("🌿 Terra Colossus Chestplate", ChatFormatting.GREEN, 0x228B22, "terra_dragon_chestplate", 18, 12,
                L(Component.literal("✦ Hover-flight on natural blocks only (grass/dirt)").withStyle(ChatFormatting.GREEN),
                        Component.literal("✦ Mining fatigue in air when not over earth").withStyle(ChatFormatting.RED),
                        Component.literal("✦ Bonemeal pulse hurts undead nearby").withStyle(ChatFormatting.GOLD)));
    }

    public static ItemStack createTempest() {
        return base("⛈ Tempest Lord Chestplate", ChatFormatting.DARK_AQUA, 0x00BFFF, "tempest_dragon_chestplate", 19, 13,
                L(Component.literal("✦ Rain/thunder flight — raw damage from clear skies").withStyle(ChatFormatting.AQUA),
                        Component.literal("✦ Chain slow to nearby hostiles in storms").withStyle(ChatFormatting.YELLOW),
                        Component.literal("✦ Static sparks (subtle)").withStyle(ChatFormatting.GRAY)));
    }

    public static ItemStack createAuric() {
        return base("✨ Auric Drake Chestplate", ChatFormatting.GOLD, 0xFFC125, "auric_dragon_chestplate", 17, 11,
                L(Component.literal("✦ Greedy flight: loses saturation quickly").withStyle(ChatFormatting.YELLOW),
                        Component.literal("✦ Luck + looting aura (weak regen when full hunger)").withStyle(ChatFormatting.GREEN),
                        Component.literal("✦ Withering pulse when starving").withStyle(ChatFormatting.DARK_RED)));
    }

    public static ItemStack createGlacier() {
        return base("🧊 Glacier Titan Chestplate", ChatFormatting.AQUA, 0xADD8E6, "glacier_dragon_chestplate", 18, 12,
                L(Component.literal("✦ No flight — frost nova aura").withStyle(ChatFormatting.AQUA),
                        Component.literal("✦ Slowness to attackers; stride on ice").withStyle(ChatFormatting.BLUE),
                        Component.literal("✦ Melts in Nether (slowness II)").withStyle(ChatFormatting.RED)));
    }

    public static ItemStack createMire() {
        return base("🪱 Mire Horror Chestplate", ChatFormatting.DARK_GREEN, 0x355E3B, "mire_dragon_chestplate", 18, 12,
                L(Component.literal("✦ No flight: poison mastery").withStyle(ChatFormatting.DARK_GREEN),
                        Component.literal("✦ Hunger foes on hit — you take hunger in dry biomes").withStyle(ChatFormatting.RED),
                        Component.literal("✦ Swamp spore trail").withStyle(ChatFormatting.GRAY)));
    }

    public static ItemStack createStarfall() {
        return base("🌠 Starfall Chimera Chestplate", ChatFormatting.LIGHT_PURPLE, 0xDA70D6, "starfall_dragon_chestplate", 19, 13,
                L(Component.literal("✦ Elytra-like boosts + brief creative flight (cooldown)").withStyle(ChatFormatting.LIGHT_PURPLE),
                        Component.literal("✦ After flight: levitation junk debuffs").withStyle(ChatFormatting.RED),
                        Component.literal("✦ End rod meteors at feet").withStyle(ChatFormatting.DARK_PURPLE)));
    }

    public static ItemStack createMirage() {
        return base("🏜 Mirage Dancer Chestplate", ChatFormatting.YELLOW, 0xF4A460, "mirage_dragon_chestplate", 17, 11,
                L(Component.literal("✦ Desert-day flight + short nausea when dodging").withStyle(ChatFormatting.YELLOW),
                        Component.literal("✦ Invisibility flash while sprinting on sand").withStyle(ChatFormatting.GREEN),
                        Component.literal("✦ Blindness near cold biomes").withStyle(ChatFormatting.BLUE)));
    }

    public static ItemStack createObsidianWyrm() {
        return base("⬛ Obsidian Wyrm Chestplate", ChatFormatting.DARK_PURPLE, 0x1A1A2E, "obsidian_wyrm_chestplate", 20, 14,
                L(Component.literal("✦ Slow flight — resistance III but heavy hunger").withStyle(ChatFormatting.DARK_PURPLE),
                        Component.literal("✦ Blast & knockback resistance vibes").withStyle(ChatFormatting.GRAY),
                        Component.literal("✦ Obsidian dust at heels").withStyle(ChatFormatting.DARK_GRAY)));
    }

    // ============================================================
    // NEW DRAGON CHESTPLATES (35)
    // ============================================================

    private static final String[] NEW_THEME_PREFIXES = {
            "Cinder", "Tide", "Prism", "Nightwatch", "Bloom", "Ironroot", "Stormseeker", "Emberweave",
            "Frostbitten", "Mirage", "Terra", "Glimmervault", "Deepwaltz", "Shadowspire", "Sunflare",
            "Moonforge", "Aurorastream", "Glacierkin", "Swampmender", "Starwrought", "Reefwalker",
            "Volcanicheir", "Crystalcurrent", "Basiliskveil", "Sanguinelace", "Eclipsebound",
            "Zephyrguard", "Obsidianhush", "Pollenwraith", "Cometcradle", "Arcaneharbor",
            "Emberguard", "Auricwave", "Tideknight", "Frostwyrm", "Dreadreef", "Thorncrown",
            "Skyforge", "Stonepulse", "Starveil", "Thunderwake", "Ashbound", "Icelace",
            "Moonfall", "Dawnspire", "Runeclad", "Coralbrand", "Driftglass", "Venombloom",
            "Galeshard", "Nightflare", "Sunkenlord", "Blightward", "Voidpetal", "Silverscale",
            "Cragwarden",
            "Rimevanguard", "Verdantwyrm", "Astralreef", "Emberlynx", "Nocturnespire",
            "Skyscour", "Gravemoss", "Tempestine", "Vortexmantle", "Duskbloom"
            ,
            "Ashenwaltz", "Frostmantle", "Coralhymn", "Verdigrisheart", "Solarvortex",
            "Moonkiln", "Dunewarden", "Stormcrown", "Obsidianring", "Thornhollow",

            // Extra themes so new chestplates can reach the full 1..95 range.
            "Cinderchime", "Emberhollow", "Frostsear", "Glacierbastion", "Tidemorrow",
            "Reefmosaic", "Prismrelic", "Nightguardian", "Bloomflare", "Ironrootspring",
            "Stormcoda", "Thundercoil", "Venomwhorl", "Bloodsable", "Eclipsetide",
            "Voidwreath", "Obsidianrelay", "Terrafernsong", "Starshard"
    };

    private static final String[] EFFECT_DISPLAY_CYCLE = {
            "Strength", "Speed", "Resistance", "Regeneration", "Absorption", "Fire Resistance",
            "Water Breathing", "Night Vision", "Haste", "Jump Boost", "Luck", "Slow Falling"
    };

    private static final String[] PARTICLE_DISPLAY_CYCLE = {
            "Crit", "Enchant", "Cloud", "Happyness", "Bubble Pop", "Small Flame",
            "Ash", "End Rod", "Electric Spark", "Wax On", "Snowflake", "Large Smoke"
    };

    private static List<Component> abilityLinesForBaseChestplate(int index1Based) {
        // Distribute the 15 "classic" dragon chestplate abilities across the expanded set.
        int base = (index1Based - 1) % 15;
        return switch (base) {
            case 0 -> L(
                    Component.literal("✦ Desert flight: hunger drains while airborne").withStyle(ChatFormatting.YELLOW),
                    Component.literal("✦ Speed & haste on sand").withStyle(ChatFormatting.GREEN),
                    Component.literal("✦ Mining fatigue away from sand").withStyle(ChatFormatting.RED)
            );
            case 1 -> L(
                    Component.literal("✦ Water breathing & dolphin grace in water").withStyle(ChatFormatting.AQUA),
                    Component.literal("✦ Flight only above sea level — drowns faster if starving").withStyle(ChatFormatting.YELLOW),
                    Component.literal("✦ Slowness on dry land (heavy scales)").withStyle(ChatFormatting.RED)
            );
            case 2 -> L(
                    Component.literal("✦ Short flight: burns you unless sneaking").withStyle(ChatFormatting.GOLD),
                    Component.literal("✦ Fire affinity + strength when not burning").withStyle(ChatFormatting.RED),
                    Component.literal("✦ Ash trail (cosmetic)").withStyle(ChatFormatting.DARK_GRAY)
            );
            case 3 -> L(
                    Component.literal("✦ No flight — prism shield").withStyle(ChatFormatting.LIGHT_PURPLE),
                    Component.literal("✦ Resistance + thorns-style chip damage to attackers").withStyle(ChatFormatting.AQUA),
                    Component.literal("✦ Shatter sparks at feet").withStyle(ChatFormatting.GRAY)
            );
            case 4 -> L(
                    Component.literal("✦ Night flight: weakness I while flying").withStyle(ChatFormatting.DARK_PURPLE),
                    Component.literal("✦ Speed & darkness immunity at low light").withStyle(ChatFormatting.GRAY),
                    Component.literal("✦ Glowing in direct sunlight").withStyle(ChatFormatting.YELLOW)
            );
            case 5 -> L(
                    Component.literal("✦ Day flight: hunger cost + brief nausea when landing hard").withStyle(ChatFormatting.GOLD),
                    Component.literal("✦ Strength in daylight").withStyle(ChatFormatting.RED),
                    Component.literal("✦ Slowness I at night").withStyle(ChatFormatting.BLUE)
            );
            case 6 -> L(
                    Component.literal("✦ Night-only creative flight").withStyle(ChatFormatting.AQUA),
                    Component.literal("✦ Regeneration under moonlight — weakness by day").withStyle(ChatFormatting.LIGHT_PURPLE),
                    Component.literal("✦ Star wisps behind you").withStyle(ChatFormatting.GRAY)
            );
            case 7 -> L(
                    Component.literal("✦ Hover-flight on natural blocks only (grass/dirt)").withStyle(ChatFormatting.GREEN),
                    Component.literal("✦ Mining fatigue in air when not over earth").withStyle(ChatFormatting.RED),
                    Component.literal("✦ Bonemeal pulse hurts undead nearby").withStyle(ChatFormatting.GOLD)
            );
            case 8 -> L(
                    Component.literal("✦ Rain/thunder flight — raw damage from clear skies").withStyle(ChatFormatting.AQUA),
                    Component.literal("✦ Chain slow to nearby hostiles in storms").withStyle(ChatFormatting.YELLOW),
                    Component.literal("✦ Static sparks (subtle)").withStyle(ChatFormatting.GRAY)
            );
            case 9 -> L(
                    Component.literal("✦ Greedy flight: loses saturation quickly").withStyle(ChatFormatting.YELLOW),
                    Component.literal("✦ Luck + looting aura (weak regen when full hunger)").withStyle(ChatFormatting.GREEN),
                    Component.literal("✦ Withering pulse when starving").withStyle(ChatFormatting.DARK_RED)
            );
            case 10 -> L(
                    Component.literal("✦ No flight — frost nova aura").withStyle(ChatFormatting.AQUA),
                    Component.literal("✦ Slowness to attackers; stride on ice").withStyle(ChatFormatting.BLUE),
                    Component.literal("✦ Melts in Nether (slowness II)").withStyle(ChatFormatting.RED)
            );
            case 11 -> L(
                    Component.literal("✦ No flight: poison mastery").withStyle(ChatFormatting.DARK_GREEN),
                    Component.literal("✦ Hunger foes on hit — you take hunger in dry biomes").withStyle(ChatFormatting.RED),
                    Component.literal("✦ Swamp spore trail").withStyle(ChatFormatting.GRAY)
            );
            case 12 -> L(
                    Component.literal("✦ Elytra-like boosts + brief creative flight (cooldown)").withStyle(ChatFormatting.LIGHT_PURPLE),
                    Component.literal("✦ After flight: levitation junk debuffs").withStyle(ChatFormatting.RED),
                    Component.literal("✦ End rod meteors at feet").withStyle(ChatFormatting.DARK_PURPLE)
            );
            case 13 -> L(
                    Component.literal("✦ Desert-day flight + short nausea when dodging").withStyle(ChatFormatting.YELLOW),
                    Component.literal("✦ Invisibility flash while sprinting on sand").withStyle(ChatFormatting.GREEN),
                    Component.literal("✦ Blindness near cold biomes").withStyle(ChatFormatting.BLUE)
            );
            default -> L(
                    Component.literal("✦ Slow flight — resistance III but heavy hunger").withStyle(ChatFormatting.DARK_PURPLE),
                    Component.literal("✦ Blast & knockback resistance vibes").withStyle(ChatFormatting.GRAY),
                    Component.literal("✦ Obsidian dust at heels").withStyle(ChatFormatting.DARK_GRAY)
            );
        };
    }

    private static int clampRgb(int v) {
        return Math.max(0, Math.min(255, v));
    }

    private static int dyeRgbForNewChestplateIndex(int index1Based) {
        // Deterministic "HSV-ish" rainbow without extra deps.
        // Good variety for the GUI dye.
        double t = (index1Based - 1) / 95.0;
        double a = t * Math.PI * 2.0;
        int r = (int) Math.round(128 + 127 * Math.sin(a));
        int g = (int) Math.round(128 + 127 * Math.sin(a * 2.0 + 1.1));
        int b = (int) Math.round(128 + 127 * Math.sin(a * 3.0 + 2.2));
        return (clampRgb(r) << 16) | (clampRgb(g) << 8) | clampRgb(b);
    }

    static String themePrefixForNewChestplateIndex(int index1Based) {
        if (index1Based < 1) return "DrakeVariant" + index1Based;
        int zeroBased = index1Based - 1;
        if (zeroBased < NEW_THEME_PREFIXES.length) return NEW_THEME_PREFIXES[zeroBased];
        return "DrakeVariant" + index1Based;
    }

    private static ChatFormatting nameColorForNewChestplateIndex(int index1Based) {
        ChatFormatting[] colors = {
                ChatFormatting.RED, ChatFormatting.GOLD, ChatFormatting.YELLOW, ChatFormatting.GREEN, ChatFormatting.AQUA,
                ChatFormatting.BLUE, ChatFormatting.LIGHT_PURPLE, ChatFormatting.DARK_PURPLE, ChatFormatting.DARK_RED,
                ChatFormatting.DARK_GREEN
        };
        return colors[(index1Based - 1) % colors.length];
    }

    private static String customIdForNewChestplate(int index1Based) {
        return String.format("new_dragon_chestplate_%02d", index1Based);
    }

    private static int armorForNewChestplate(int index1Based) {
        return (index1Based <= 20) ? 17 : (index1Based <= 40) ? 18 : (index1Based <= 60) ? 19 : 20;
    }

    private static int toughnessForNewChestplate(int index1Based) {
        return (index1Based <= 20) ? 11 : (index1Based <= 40) ? 12 : (index1Based <= 60) ? 13 : 14;
    }

    private static int intervalTicksForNewChestplate(int index1Based) {
        return 30 + ((index1Based - 1) % 7) * 5; // 30..60
    }

    private static int durationTicksForNewChestplate(int index1Based) {
        return 160 + ((index1Based - 1) % 5) * 20; // 160..240
    }

    private static int amplifierForNewChestplate(int index1Based) {
        return Math.min((index1Based - 1) / 12, 3); // capped for balance
    }

    private static int particleCycleIndexForNewChestplate(int index1Based) {
        return (index1Based - 1) % 12;
    }

    public static ItemStack createNewDragonChestplate(int index1Based) {
        if (index1Based < 1 || index1Based > 95) throw new IllegalArgumentException("index1Based must be 1..95");

        // Avoid hard crashes if the prefix list is shorter than the index range.
        String prefix = themePrefixForNewChestplateIndex(index1Based);
        ChatFormatting nameColor = nameColorForNewChestplateIndex(index1Based);
        int dyeRgb = dyeRgbForNewChestplateIndex(index1Based);

        String customId = customIdForNewChestplate(index1Based);

        int armor = armorForNewChestplate(index1Based);
        int toughness = toughnessForNewChestplate(index1Based);

        String title = "✦ " + prefix + " Dragon Chestplate";
        List<Component> abilityLines = abilityLinesForBaseChestplate(index1Based);

        return base(
                title,
                nameColor,
                dyeRgb,
                customId,
                armor,
                toughness,
                abilityLines
        );
    }

    // ============================================================
    // OMEGA DRAGON CHESTPLATES (3)
    // ============================================================

    public static ItemStack createOmegaDragonChestplate(int index1Based) {
        if (index1Based < 1 || index1Based > 3)
            throw new IllegalArgumentException("index1Based must be 1..3");

        String customId = "omega_dragon_chestplate_" + index1Based;
        // Keep omega armor/toughness within the new nerfed range.
        int armor = 20;
        int toughness = 14;

        return switch (index1Based) {
            case 1 -> base(
                    "✶ Omega Bulwark Chestplate",
                    ChatFormatting.GOLD,
                    0xFFB300,
                    customId,
                    armor,
                    toughness,
                    L(
                            Component.literal("✦ Omega Core: unlocks the Bulwark Protocol").withStyle(ChatFormatting.GOLD),
                            Component.literal("✦ While flying: Resistance + burst Regeneration (very strong)").withStyle(ChatFormatting.YELLOW),
                            Component.literal("✦ Drawback: health is capped to 1 heart while airborne").withStyle(ChatFormatting.RED)
                    )
            );
            case 2 -> base(
                    "✶ Omega Aegisweave Chestplate",
                    ChatFormatting.LIGHT_PURPLE,
                    0xB48CFF,
                    customId,
                    armor,
                    toughness,
                    L(
                            Component.literal("✦ Omega Core: unlocks the Aegis-Weave Engine").withStyle(ChatFormatting.GOLD),
                            Component.literal("✦ While flying: Absorption surges + thermal protection (very strong)").withStyle(ChatFormatting.YELLOW),
                            Component.literal("✦ Drawback: health is capped to 1 heart while airborne").withStyle(ChatFormatting.RED)
                    )
            );
            case 3 -> base(
                    "✶ Omega Draconic Prism Chestplate",
                    ChatFormatting.AQUA,
                    0x38BDF8,
                    customId,
                    armor,
                    toughness,
                    L(
                            Component.literal("✦ Omega Core: unlocks the Prism Ascension").withStyle(ChatFormatting.GOLD),
                            Component.literal("✦ While flying: Night Vision + Haste + Strength (very strong)").withStyle(ChatFormatting.YELLOW),
                            Component.literal("✦ Drawback: health is capped to 1 heart while airborne").withStyle(ChatFormatting.RED)
                    )
            );
            default -> throw new IllegalArgumentException("Invalid omega index");
        };
    }
}
