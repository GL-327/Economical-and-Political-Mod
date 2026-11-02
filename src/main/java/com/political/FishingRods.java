package com.political;


import com.political.bounty.SlayerItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.List;

public final class FishingRods {

    private FishingRods() {}

    public enum RodTier {
        STARTER("starter_rod", "Driftwood Rod", 1, 0,
                ChatFormatting.WHITE, false,
                0, 0, 0, 0),
        STEEL("steel_rod", "Ironside Rod", 5, 0,
                ChatFormatting.GRAY, true,
                10, 0, 5, 0),
        CORAL("coral_rod", "Coralweave Rod", 15, 0,
                ChatFormatting.AQUA, true,
                5, 15, 10, 0),
        ABYSSAL("abyssal_rod", "Abyssal Rod", 25, 0,
                ChatFormatting.DARK_PURPLE, true,
                15, 10, 20, 1),
        LEVIATHAN("leviathan_rod", "Leviathan's Hook", 40, 0,
                ChatFormatting.GOLD, true,
                25, 20, 30, 1);

        public final String id;
        public final String displayName;
        public final int levelRequired;
        public final int customModelData;
        public final ChatFormatting color;
        public final boolean glowing;
        public final int catchSpeedBonus;
        public final int seaCreatureBonus;
        public final int treasureBonus;
        public final int lavaFishing; // 0=no, 1=yes

        RodTier(String id, String displayName, int levelRequired, int customModelData,
                ChatFormatting color, boolean glowing,
                int catchSpeedBonus, int seaCreatureBonus, int treasureBonus, int lavaFishing) {
            this.id = id;
            this.displayName = displayName;
            this.levelRequired = levelRequired;
            this.customModelData = customModelData;
            this.color = color;
            this.glowing = glowing;
            this.catchSpeedBonus = catchSpeedBonus;
            this.seaCreatureBonus = seaCreatureBonus;
            this.treasureBonus = treasureBonus;
            this.lavaFishing = lavaFishing;
        }

        public static RodTier fromId(String id) {
            for (RodTier t : values()) {
                if (t.id.equals(id)) return t;
            }
            return null;
        }

        public static RodTier bestForLevel(int level) {
            RodTier best = STARTER;
            for (RodTier t : values()) {
                if (level >= t.levelRequired) best = t;
            }
            return best;
        }
    }

    public static ItemStack createRod(RodTier tier) {
        ItemStack stack = new ItemStack(Items.FISHING_ROD, 1);

        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal(tier.displayName).withStyle(s -> s.withColor(tier.color).withItalic(false)));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("Fishing Rod").withStyle(s -> s.withColor(ChatFormatting.DARK_GRAY).withItalic(false)));
        lore.add(Component.literal(""));
        if (tier.catchSpeedBonus > 0)
            lore.add(Component.literal("+" + tier.catchSpeedBonus + "% Catch Speed").withStyle(s -> s.withColor(ChatFormatting.GREEN).withItalic(false)));
        if (tier.seaCreatureBonus > 0)
            lore.add(Component.literal("+" + tier.seaCreatureBonus + "% Sea Creature Chance").withStyle(s -> s.withColor(ChatFormatting.AQUA).withItalic(false)));
        if (tier.treasureBonus > 0)
            lore.add(Component.literal("+" + tier.treasureBonus + "% Treasure Find").withStyle(s -> s.withColor(ChatFormatting.GOLD).withItalic(false)));
        if (tier.lavaFishing > 0)
            lore.add(Component.literal("Lava Fishing Enabled").withStyle(s -> s.withColor(ChatFormatting.RED).withItalic(false)));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Requires Fishing Level " + tier.levelRequired).withStyle(s -> s.withColor(ChatFormatting.DARK_GRAY).withItalic(false)));
        stack.set(DataComponents.LORE, new ItemLore(lore));

        CompoundTag nbt = new CompoundTag();
        nbt.putString("fishing_rod_tier", tier.name());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        SlayerItems.setCustomItemId(stack, tier.id);

        if (tier.glowing) {
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        }

        return stack;
    }

    public static RodTier getRodTier(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        CompoundTag tag = data.copyTag();
        String tierStr = tag.getStringOr("fishing_rod_tier", "");
        if (tierStr.isEmpty()) return null;
        try {
            return RodTier.valueOf(tierStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static boolean isFishingRod(ItemStack stack) {
        return getRodTier(stack) != null;
    }

    public static List<ItemStack> allRods() {
        List<ItemStack> rods = new ArrayList<>();
        for (RodTier t : RodTier.values()) {
            rods.add(createRod(t));
        }
        return rods;
    }
}
