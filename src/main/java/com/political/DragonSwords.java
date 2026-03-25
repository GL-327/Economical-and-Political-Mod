package com.political;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public final class DragonSwords {
    private DragonSwords() {}

    private static final String[] THEMES = {
            "Inferno", "Tempest", "Void", "Aurora", "Frost",
            "Venom", "Blood", "Eclipse", "Terra", "Starfall",
            "Abyss", "Solar", "Lunar", "Obsidian", "Omega",
            "Cinder", "Tide", "Prism", "Nightwatch", "Bloom",
            "Ironroot", "Stormseeker", "Emberweave", "Frostbitten", "Mirage",
            "Terraforge", "Glimmervault", "Deepwaltz", "Shadowspire", "Sunflare",
            "Crownspire", "Emberlynx", "Skyforged", "Runeclad", "Moonfall",
            "Dreadreef", "Thorncrown", "Stonepulse", "Starveil", "Arcaneharbor",
            "Auricwave", "Verdantwyrm", "Glacierkin", "Swampmender", "Vortexmantle",
            "Duskbloom", "Rimevanguard", "Nightflare", "Cragwarden", "Obsidianveil",

            // Extra sword themes so indices can reach 1..55.
            "Cindervein", "Frostwarden", "Tideweaver", "Reefgloom", "Voidbastion",
            "Auroraforge", "Verdantlore", "Obsidianrelic", "Glowstar", "Runehaven"
    };

    private static final Formatting[] COLORS = {
            Formatting.RED, Formatting.AQUA, Formatting.DARK_PURPLE, Formatting.LIGHT_PURPLE, Formatting.AQUA,
            Formatting.DARK_GREEN, Formatting.DARK_RED, Formatting.DARK_GRAY, Formatting.GREEN, Formatting.LIGHT_PURPLE,
            Formatting.BLUE, Formatting.GOLD, Formatting.BLUE, Formatting.DARK_PURPLE, Formatting.GOLD,
            Formatting.DARK_RED, Formatting.AQUA, Formatting.DARK_PURPLE, Formatting.GOLD, Formatting.YELLOW,
            Formatting.GREEN, Formatting.AQUA, Formatting.RED, Formatting.BLUE, Formatting.YELLOW,
            Formatting.LIGHT_PURPLE, Formatting.DARK_PURPLE, Formatting.DARK_GREEN, Formatting.DARK_GRAY, Formatting.GOLD
    };

    private static final String[] DEBUFF_NAMES = {
            "Weakness", "Slowness", "Mining Fatigue", "Poison", "Wither",
            "Blindness", "Hunger", "Nausea", "Levitation", "Darkness"
    };

    static String themeForSwordIndex(int index1Based) {
        int i = index1Based - 1;
        if (i >= 0 && i < THEMES.length) return THEMES[i];
        // Should never happen because we cap index1Based to 1..55
        return "DrakeVariant" + index1Based;
    }

    public static ItemStack createDragonSword(int index1Based) {
        if (index1Based < 1 || index1Based > 55) throw new IllegalArgumentException("index1Based must be 1..55");
        int i = index1Based - 1;
        int tier = i / 10;        // 0..4
        int within = i % 10;     // 0..9
        int tierScaled = Math.min(tier, 3); // cap power scaling

        ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);
        String customId = String.format("dragon_sword_%02d", index1Based);
        String theme = themeForSwordIndex(index1Based);
        String title = "⚔ " + theme + " Dragon Sword";
        String debuffName = DEBUFF_NAMES[within];

        sword.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(title).formatted(COLORS[i % COLORS.length], Formatting.BOLD));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 60000);
        sword.set(DataComponentTypes.DAMAGE, 0);

        AttributeModifiersComponent.Builder attrs = AttributeModifiersComponent.builder();
        double attackDamage = 10.0 + tierScaled * 1.6 + within * 0.12; // capped growth
        attrs.add(EntityAttributes.ATTACK_DAMAGE, new EntityAttributeModifier(
                Identifier.of("politicalserver", customId + "_atk"),
                attackDamage,
                EntityAttributeModifier.Operation.ADD_VALUE
        ), AttributeModifierSlot.MAINHAND);
        attrs.add(EntityAttributes.ATTACK_SPEED, new EntityAttributeModifier(
                Identifier.of("politicalserver", customId + "_spd"),
                0.8,
                EntityAttributeModifier.Operation.ADD_VALUE
        ), AttributeModifierSlot.MAINHAND);
        sword.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrs.build());

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[Legendary Dragon Weapon]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.GRAY));
        lore.add(Text.literal("⚔ Damage: +" + String.format("%.1f", attackDamage)).formatted(Formatting.RED));
        lore.add(Text.literal("⚡ Speed: +0.8").formatted(Formatting.AQUA));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ DRAGON ABILITY ━━━").formatted(COLORS[i % COLORS.length]));
        lore.add(Text.literal("✦ On-hit debuff: " + debuffName).formatted(Formatting.YELLOW));
        lore.add(Text.literal("✦ Tier scaling with balanced self-strain").formatted(Formatting.RED));
        lore.add(Text.literal("✦ Subtle trail particles while held").formatted(Formatting.GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));

        SlayerItems.setCustomItemId(sword, customId);
        return sword;
    }

    public static List<ItemStack> allStacks() {
        List<ItemStack> out = new ArrayList<>();
        for (int i = 1; i <= 55; i++) out.add(createDragonSword(i));
        return out;
    }
}

