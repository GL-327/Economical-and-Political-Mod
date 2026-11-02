package com.political.items;


import com.political.bounty.SlayerItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.Arrays;

/** Four legendary tool sets: Dragon (3x3 + autosmelt), Void (drops to inventory), Nature (vein/tree + replant), Frost (slow + ice-path). */
public class LegendaryTools {

    public static ItemStack createDragonPickaxe() {
        return createLegendaryTool(Items.NETHERITE_PICKAXE, "Dragon Pickaxe", ChatFormatting.DARK_RED,
                "Blazing Excavation", "Mines 3x3 and auto-smelts ores", 6);
    }
    public static ItemStack createDragonAxe() {
        return createLegendaryTool(Items.NETHERITE_AXE, "Dragon Axe", ChatFormatting.DARK_RED,
                "Inferno Cut", "Chops entire trees at once", 7);
    }
    public static ItemStack createDragonShovel() {
        return createLegendaryTool(Items.NETHERITE_SHOVEL, "Dragon Shovel", ChatFormatting.DARK_RED,
                "Magma Dig", "3x3 digging with autosmelt", 5);
    }
    public static ItemStack createDragonHoe() {
        return createLegendaryTool(Items.NETHERITE_HOE, "Dragon Hoe", ChatFormatting.DARK_RED,
                "Scorched Earth", "Tills a wide patch and replants", 4);
    }

    public static ItemStack createVoidPickaxe() {
        return createLegendaryTool(Items.NETHERITE_PICKAXE, "Void Pickaxe", ChatFormatting.DARK_PURPLE,
                "Null Mining", "Teleports mined blocks to inventory", 6);
    }
    public static ItemStack createVoidAxe() {
        return createLegendaryTool(Items.NETHERITE_AXE, "Void Axe", ChatFormatting.DARK_PURPLE,
                "Shadow Strike", "Silent tree felling into inventory", 7);
    }
    public static ItemStack createVoidShovel() {
        return createLegendaryTool(Items.NETHERITE_SHOVEL, "Void Shovel", ChatFormatting.DARK_PURPLE,
                "Ender Excavation", "Drops skip the ground", 5);
    }
    public static ItemStack createVoidHoe() {
        return createLegendaryTool(Items.NETHERITE_HOE, "Void Hoe", ChatFormatting.DARK_PURPLE,
                "Dark Harvest", "Crops go straight to inventory", 4);
    }

    public static ItemStack createNaturePickaxe() {
        return createLegendaryTool(Items.DIAMOND_PICKAXE, "Nature Pickaxe", ChatFormatting.GREEN,
                "Living Stone", "Veins connected ores", 4);
    }
    public static ItemStack createNatureAxe() {
        return createLegendaryTool(Items.DIAMOND_AXE, "Nature Axe", ChatFormatting.GREEN,
                "Regrowth", "Fells trees and replants saplings", 5);
    }
    public static ItemStack createNatureShovel() {
        return createLegendaryTool(Items.DIAMOND_SHOVEL, "Nature Shovel", ChatFormatting.GREEN,
                "Root Dig", "Soft dirt veins", 3);
    }
    public static ItemStack createNatureHoe() {
        return createLegendaryTool(Items.DIAMOND_HOE, "Nature Hoe", ChatFormatting.GREEN,
                "Harvest Cycle", "Replants after harvest", 3);
    }

    public static ItemStack createFrostPickaxe() {
        return createLegendaryTool(Items.DIAMOND_PICKAXE, "Frost Pickaxe", ChatFormatting.AQUA,
                "Glacial Edge", "Slows nearby foes while mining", 4);
    }
    public static ItemStack createFrostAxe() {
        return createLegendaryTool(Items.DIAMOND_AXE, "Frost Axe", ChatFormatting.AQUA,
                "Rime Cut", "Slows on hit", 5);
    }
    public static ItemStack createFrostShovel() {
        return createLegendaryTool(Items.DIAMOND_SHOVEL, "Frost Shovel", ChatFormatting.AQUA,
                "Ice Path", "Freezes water underfoot", 3);
    }
    public static ItemStack createFrostHoe() {
        return createLegendaryTool(Items.DIAMOND_HOE, "Frost Hoe", ChatFormatting.AQUA,
                "Winter Till", "Slows crop pests nearby", 3);
    }

    private static ItemStack createLegendaryTool(Item baseItem, String name, ChatFormatting color,
                                                 String ability, String description, int damage) {
        ItemStack stack = new ItemStack(baseItem);
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal(name).withStyle(color, ChatFormatting.BOLD));
        stack.set(DataComponents.LORE, new ItemLore(Arrays.asList(
                Component.literal(""),
                Component.literal("Legendary Tool").withStyle(color, ChatFormatting.BOLD),
                Component.literal(""),
                Component.literal("Ability: " + ability).withStyle(ChatFormatting.YELLOW),
                Component.literal(description).withStyle(ChatFormatting.GRAY),
                Component.literal(""),
                Component.literal("Damage: " + damage).withStyle(ChatFormatting.RED)
        )));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(stack, name.toLowerCase().replace("'", "").replace(" ", "_"));
        return stack;
    }
}
