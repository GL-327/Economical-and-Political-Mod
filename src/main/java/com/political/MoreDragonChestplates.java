package com.political;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/** Fifteen additional legendary dragon chestplates (see {@link MoreDragonChestplateAbilities}). */
public final class MoreDragonChestplates {

    private MoreDragonChestplates() {
    }

    private static ItemStack base(String title, Formatting nameColor, int dyeRgb, String customId,
                                  int armor, int toughness, List<Text> abilityLines) {
        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        chestplate.set(DataComponentTypes.CUSTOM_NAME, Text.literal(title).formatted(nameColor, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[Legendary Dragon Armor]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.GRAY));
        lore.add(Text.literal("🛡 Armour: " + armor).formatted(Formatting.GRAY));
        lore.add(Text.literal("⚔ Toughness: " + toughness).formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ DRAGON ABILITY ━━━").formatted(nameColor));
        lore.addAll(abilityLines);
        lore.add(Text.literal(""));
        chestplate.set(DataComponentTypes.LORE, new LoreComponent(lore));
        chestplate.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        chestplate.set(DataComponentTypes.MAX_DAMAGE, 50000);
        chestplate.set(DataComponentTypes.DAMAGE, 0);
        chestplate.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(dyeRgb));
        AttributeModifiersComponent.Builder b = AttributeModifiersComponent.builder();
        b.add(EntityAttributes.ARMOR, new EntityAttributeModifier(
                Identifier.of("politicalserver", customId + "_armor"),
                armor,
                EntityAttributeModifier.Operation.ADD_VALUE
        ), AttributeModifierSlot.CHEST);
        b.add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(
                Identifier.of("politicalserver", customId + "_tough"),
                toughness,
                EntityAttributeModifier.Operation.ADD_VALUE
        ), AttributeModifierSlot.CHEST);
        chestplate.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, b.build());
        DragonChestplates.setCustomItemId(chestplate, customId);
        return chestplate;
    }

    private static List<Text> L(Text... lines) {
        return List.of(lines);
    }

    public static ItemStack createDune() {
        return base("🏜 Dune Wyrm Chestplate", Formatting.GOLD, 0xC2B280, "dune_dragon_chestplate", 19, 13,
                L(Text.literal("✦ Desert flight: hunger drains while airborne").formatted(Formatting.YELLOW),
                        Text.literal("✦ Speed & haste on sand").formatted(Formatting.GREEN),
                        Text.literal("✦ Mining fatigue away from sand").formatted(Formatting.RED)));
    }

    public static ItemStack createReef() {
        return base("🐚 Reef Leviathan Chestplate", Formatting.AQUA, 0x20B2AA, "reef_dragon_chestplate", 19, 13,
                L(Text.literal("✦ Water breathing & dolphin grace in water").formatted(Formatting.AQUA),
                        Text.literal("✦ Flight only above sea level — drowns faster if starving").formatted(Formatting.YELLOW),
                        Text.literal("✦ Slowness on dry land (heavy scales)").formatted(Formatting.RED)));
    }

    public static ItemStack createEmber() {
        return base("🔥 Ember Drakeling Chestplate", Formatting.RED, 0xFF4500, "ember_dragon_chestplate", 20, 14,
                L(Text.literal("✦ Short flight: burns you unless sneaking").formatted(Formatting.GOLD),
                        Text.literal("✦ Fire affinity + strength when not burning").formatted(Formatting.RED),
                        Text.literal("✦ Ash trail (cosmetic)").formatted(Formatting.DARK_GRAY)));
    }

    public static ItemStack createCrystal() {
        return base("💎 Crystal Drake Chestplate", Formatting.LIGHT_PURPLE, 0xE0B0FF, "crystal_dragon_chestplate", 21, 15,
                L(Text.literal("✦ No flight — prism shield").formatted(Formatting.LIGHT_PURPLE),
                        Text.literal("✦ Resistance + thorns-style chip damage to attackers").formatted(Formatting.AQUA),
                        Text.literal("✦ Shatter sparks at feet").formatted(Formatting.GRAY)));
    }

    public static ItemStack createShadow() {
        return base("🌑 Shadow Wyrm Chestplate", Formatting.DARK_GRAY, 0x2F2F2F, "shadow_dragon_chestplate", 20, 14,
                L(Text.literal("✦ Night flight: weakness I while flying").formatted(Formatting.DARK_PURPLE),
                        Text.literal("✦ Speed & darkness immunity at low light").formatted(Formatting.GRAY),
                        Text.literal("✦ Glowing in direct sunlight").formatted(Formatting.YELLOW)));
    }

    public static ItemStack createSolar() {
        return base("☀ Solar Serpent Chestplate", Formatting.YELLOW, 0xFFD700, "solar_dragon_chestplate", 20, 14,
                L(Text.literal("✦ Day flight: hunger cost + brief nausea when landing hard").formatted(Formatting.GOLD),
                        Text.literal("✦ Strength in daylight").formatted(Formatting.RED),
                        Text.literal("✦ Slowness I at night").formatted(Formatting.BLUE)));
    }

    public static ItemStack createLunar() {
        return base("🌙 Lunar Serpent Chestplate", Formatting.BLUE, 0x4169E1, "lunar_dragon_chestplate", 20, 14,
                L(Text.literal("✦ Night-only creative flight").formatted(Formatting.AQUA),
                        Text.literal("✦ Regeneration under moonlight — weakness by day").formatted(Formatting.LIGHT_PURPLE),
                        Text.literal("✦ Star wisps behind you").formatted(Formatting.GRAY)));
    }

    public static ItemStack createTerra() {
        return base("🌿 Terra Colossus Chestplate", Formatting.GREEN, 0x228B22, "terra_dragon_chestplate", 20, 14,
                L(Text.literal("✦ Hover-flight on natural blocks only (grass/dirt)").formatted(Formatting.GREEN),
                        Text.literal("✦ Mining fatigue in air when not over earth").formatted(Formatting.RED),
                        Text.literal("✦ Bonemeal pulse hurts undead nearby").formatted(Formatting.GOLD)));
    }

    public static ItemStack createTempest() {
        return base("⛈ Tempest Lord Chestplate", Formatting.DARK_AQUA, 0x00BFFF, "tempest_dragon_chestplate", 21, 15,
                L(Text.literal("✦ Rain/thunder flight — raw damage from clear skies").formatted(Formatting.AQUA),
                        Text.literal("✦ Chain slow to nearby hostiles in storms").formatted(Formatting.YELLOW),
                        Text.literal("✦ Static sparks (subtle)").formatted(Formatting.GRAY)));
    }

    public static ItemStack createAuric() {
        return base("✨ Auric Drake Chestplate", Formatting.GOLD, 0xFFC125, "auric_dragon_chestplate", 19, 13,
                L(Text.literal("✦ Greedy flight: loses saturation quickly").formatted(Formatting.YELLOW),
                        Text.literal("✦ Luck + looting aura (weak regen when full hunger)").formatted(Formatting.GREEN),
                        Text.literal("✦ Withering pulse when starving").formatted(Formatting.DARK_RED)));
    }

    public static ItemStack createGlacier() {
        return base("🧊 Glacier Titan Chestplate", Formatting.AQUA, 0xADD8E6, "glacier_dragon_chestplate", 20, 14,
                L(Text.literal("✦ No flight — frost nova aura").formatted(Formatting.AQUA),
                        Text.literal("✦ Slowness to attackers; stride on ice").formatted(Formatting.BLUE),
                        Text.literal("✦ Melts in Nether (slowness II)").formatted(Formatting.RED)));
    }

    public static ItemStack createMire() {
        return base("🪱 Mire Horror Chestplate", Formatting.DARK_GREEN, 0x355E3B, "mire_dragon_chestplate", 20, 14,
                L(Text.literal("✦ No flight: poison mastery").formatted(Formatting.DARK_GREEN),
                        Text.literal("✦ Hunger foes on hit — you take hunger in dry biomes").formatted(Formatting.RED),
                        Text.literal("✦ Swamp spore trail").formatted(Formatting.GRAY)));
    }

    public static ItemStack createStarfall() {
        return base("🌠 Starfall Chimera Chestplate", Formatting.LIGHT_PURPLE, 0xDA70D6, "starfall_dragon_chestplate", 21, 15,
                L(Text.literal("✦ Elytra-like boosts + brief creative flight (cooldown)").formatted(Formatting.LIGHT_PURPLE),
                        Text.literal("✦ After flight: levitation junk debuffs").formatted(Formatting.RED),
                        Text.literal("✦ End rod meteors at feet").formatted(Formatting.DARK_PURPLE)));
    }

    public static ItemStack createMirage() {
        return base("🏜 Mirage Dancer Chestplate", Formatting.YELLOW, 0xF4A460, "mirage_dragon_chestplate", 19, 13,
                L(Text.literal("✦ Desert-day flight + short nausea when dodging").formatted(Formatting.YELLOW),
                        Text.literal("✦ Invisibility flash while sprinting on sand").formatted(Formatting.GREEN),
                        Text.literal("✦ Blindness near cold biomes").formatted(Formatting.BLUE)));
    }

    public static ItemStack createObsidianWyrm() {
        return base("⬛ Obsidian Wyrm Chestplate", Formatting.DARK_PURPLE, 0x1A1A2E, "obsidian_wyrm_chestplate", 22, 16,
                L(Text.literal("✦ Slow flight — resistance III but heavy hunger").formatted(Formatting.DARK_PURPLE),
                        Text.literal("✦ Blast & knockback resistance vibes").formatted(Formatting.GRAY),
                        Text.literal("✦ Obsidian dust at heels").formatted(Formatting.DARK_GRAY)));
    }
}
