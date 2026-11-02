package com.political.magic;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.List;

public class MagicStaff {

    public enum StaffTier {
        APPRENTICE("Apprentice Staff", 1, 5, 0, 0, 0, ChatFormatting.WHITE),
        ADEPT("Adept Staff", 10, 8, 10, 5, 5, ChatFormatting.GREEN),
        MASTER("Master Staff", 25, 11, 18, 10, 10, ChatFormatting.AQUA),
        ARCHMAGE("Archmage Staff", 40, 14, 28, 15, 14, ChatFormatting.LIGHT_PURPLE),
        MYSTIC("Mystic Staff", 50, 17, 35, 18, 18, ChatFormatting.DARK_PURPLE),
        CELESTIAL("Celestial Staff", 60, 20, 42, 22, 22, ChatFormatting.GOLD);

        public final String name;
        public final int levelRequired;
        public final int damage;
        /** Percent less mana spent (stacked with skill tree). */
        public final int manaCostReduction;
        /** Extra spell damage scaling (percent). */
        public final int spellPowerPercent;
        /** Extra cooldown reduction (percent). */
        public final int cooldownReductionPercent;
        public final ChatFormatting color;

        StaffTier(String name, int lvl, int dmg, int manaReduce, int spellPow, int cdReduce, ChatFormatting color) {
            this.name = name;
            this.levelRequired = lvl;
            this.damage = dmg;
            this.manaCostReduction = manaReduce;
            this.spellPowerPercent = spellPow;
            this.cooldownReductionPercent = cdReduce;
            this.color = color;
        }

        public static StaffTier forLevel(int level) {
            StaffTier best = APPRENTICE;
            for (StaffTier t : values()) {
                if (level >= t.levelRequired) best = t;
            }
            return best;
        }
    }

    public static ItemStack createStaff(StaffTier tier) {
        return createStaff(tier, null);
    }

    public static ItemStack createStaff(StaffTier tier, String activeSpellName) {
        ItemStack stack = new ItemStack(Items.BLAZE_ROD);
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("✦ " + tier.name).withStyle(tier.color, ChatFormatting.BOLD));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("Magic Staff").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal("Tier: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(tier.name).withStyle(tier.color)));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Damage: +" + tier.damage).withStyle(ChatFormatting.RED));
        lore.add(Component.literal("Mana Cost Reduction: " + tier.manaCostReduction + "%").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal("Spell Power: +" + tier.spellPowerPercent + "%").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal("Cooldown Reduction: " + tier.cooldownReductionPercent + "%").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal(""));

        if (activeSpellName != null) {
            MagicManager.Spell spell = MagicManager.Spell.byName(activeSpellName);
            if (spell != null) {
                lore.add(Component.literal("Applied Spell: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(spell.displayName).withStyle(spell.color, ChatFormatting.BOLD)));
                lore.add(Component.literal("Right-click to cast!").withStyle(ChatFormatting.YELLOW));
            }
        } else {
            lore.add(Component.literal("No spell applied").withStyle(ChatFormatting.GRAY));
            lore.add(Component.literal("Use /magic to apply a spell").withStyle(ChatFormatting.DARK_GRAY));
        }

        lore.add(Component.literal(""));
        lore.add(Component.literal("Level " + tier.levelRequired + "+ Required").withStyle(ChatFormatting.DARK_GRAY));

        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        return stack;
    }

    public static boolean isStaff(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.is(Items.BLAZE_ROD)) return false;
        Component name = stack.get(DataComponents.CUSTOM_NAME);
        if (name == null) return false;
        String n = name.getString();
        return n.contains("Staff") && n.contains("✦");
    }

    public static StaffTier getStaffTier(ItemStack stack) {
        if (!isStaff(stack)) return null;
        String name = stack.get(DataComponents.CUSTOM_NAME).getString();
        for (StaffTier tier : StaffTier.values()) {
            if (name.contains(tier.name)) return tier;
        }
        return StaffTier.APPRENTICE;
    }

    public static int getManaCostReduction(ItemStack stack) {
        StaffTier tier = getStaffTier(stack);
        return tier != null ? tier.manaCostReduction : 0;
    }

    public static int getSpellPowerPercent(ItemStack stack) {
        StaffTier tier = getStaffTier(stack);
        return tier != null ? tier.spellPowerPercent : 0;
    }

    public static int getCooldownReductionPercent(ItemStack stack) {
        StaffTier tier = getStaffTier(stack);
        return tier != null ? tier.cooldownReductionPercent : 0;
    }

    public static List<ItemStack> getAllStaffs() {
        List<ItemStack> staves = new ArrayList<>();
        for (StaffTier tier : StaffTier.values()) {
            staves.add(createStaff(tier));
        }
        return staves;
    }
}
