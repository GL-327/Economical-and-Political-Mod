package com.political;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;

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

    private static final ChatFormatting[] COLORS = {
            ChatFormatting.RED, ChatFormatting.AQUA, ChatFormatting.DARK_PURPLE, ChatFormatting.LIGHT_PURPLE, ChatFormatting.AQUA,
            ChatFormatting.DARK_GREEN, ChatFormatting.DARK_RED, ChatFormatting.DARK_GRAY, ChatFormatting.GREEN, ChatFormatting.LIGHT_PURPLE,
            ChatFormatting.BLUE, ChatFormatting.GOLD, ChatFormatting.BLUE, ChatFormatting.DARK_PURPLE, ChatFormatting.GOLD,
            ChatFormatting.DARK_RED, ChatFormatting.AQUA, ChatFormatting.DARK_PURPLE, ChatFormatting.GOLD, ChatFormatting.YELLOW,
            ChatFormatting.GREEN, ChatFormatting.AQUA, ChatFormatting.RED, ChatFormatting.BLUE, ChatFormatting.YELLOW,
            ChatFormatting.LIGHT_PURPLE, ChatFormatting.DARK_PURPLE, ChatFormatting.DARK_GREEN, ChatFormatting.DARK_GRAY, ChatFormatting.GOLD
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

        sword.set(DataComponents.CUSTOM_NAME,
                Component.literal(title).withStyle(COLORS[i % COLORS.length], ChatFormatting.BOLD));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 60000);
        sword.set(DataComponents.DAMAGE, 0);

        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder();
        double attackDamage = 12.0 + tierScaled * 1.6 + within * 0.12; // capped growth; lore matches 1 + ADD_VALUE
        attrs.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                Identifier.fromNamespaceAndPath("politicalserver", customId + "_atk"),
                attackDamage,
                AttributeModifier.Operation.ADD_VALUE
        ), EquipmentSlotGroup.MAINHAND);
        attrs.add(Attributes.ATTACK_SPEED, new AttributeModifier(
                Identifier.fromNamespaceAndPath("politicalserver", customId + "_spd"),
                0.8,
                AttributeModifier.Operation.ADD_VALUE
        ), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[Legendary Dragon Weapon]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Damage: " + String.format("%.1f", 1.0 + attackDamage)).withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚡ Speed: +0.8").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ DRAGON ABILITY ━━━").withStyle(COLORS[i % COLORS.length]));
        lore.add(Component.literal("✦ On-hit debuff: " + debuffName).withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("✦ Tier scaling with balanced self-strain").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("✦ Subtle trail particles while held").withStyle(ChatFormatting.GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));

        SlayerItems.setCustomItemId(sword, customId);
        return sword;
    }

    public static List<ItemStack> allStacks() {
        List<ItemStack> out = new ArrayList<>();
        for (int i = 1; i <= 55; i++) out.add(createDragonSword(i));
        return out;
    }
}

