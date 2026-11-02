package com.political.items;


import com.political.bounty.SlayerItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Emerald and Lapis Lazuli armor and tools.
 * Emerald: Between diamond and netherite tier
 * Lapis: Between iron and diamond tier
 */
public class GemGear {

    // ============================================================
    // EMERALD GEAR (Diamond+ tier, green themed)
    // ============================================================

    public static ItemStack createEmeraldHelmet() {
        ItemStack helmet = new ItemStack(Items.DIAMOND_HELMET);
        helmet.set(DataComponents.CUSTOM_NAME,
                Component.literal("Emerald Helmet").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ EMERALD ARMOR ◆").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Armor: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("5").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal("Toughness: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("4").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        lore.add(Component.literal("✦ Insight I: +10% XP gain").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal("✦ Water Breathing").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal(""));
        lore.add(Component.literal("A helmet forged from pure").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("emeralds with ancient magic.").withStyle(ChatFormatting.DARK_GRAY));
        helmet.set(DataComponents.LORE, new ItemLore(lore));
        helmet.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        
        // Apply emerald color tint
        helmet.set(DataComponents.DYED_COLOR, new DyedItemColor(0x50C878));
        
        setCustomItemId(helmet, "emerald_helmet");
        applyEmeraldArmorStats(helmet, EquipmentSlot.HEAD, 5, 4);
        return helmet;
    }

    public static ItemStack createEmeraldChestplate() {
        ItemStack chestplate = new ItemStack(Items.DIAMOND_CHESTPLATE);
        chestplate.set(DataComponents.CUSTOM_NAME,
                Component.literal("Emerald Chestplate").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ EMERALD ARMOR ◆").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Armor: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("12").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal("Toughness: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("4").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        lore.add(Component.literal("✦ Regeneration I: Slow health regen").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal("✦ Damage Shield I: 5% damage reflection").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal(""));
        lore.add(Component.literal("A chestplate of legendary").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("craftsmanship and power.").withStyle(ChatFormatting.DARK_GRAY));
        chestplate.set(DataComponents.LORE, new ItemLore(lore));
        chestplate.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        chestplate.set(DataComponents.DYED_COLOR, new DyedItemColor(0x50C878));
        
        setCustomItemId(chestplate, "emerald_chestplate");
        applyEmeraldArmorStats(chestplate, EquipmentSlot.CHEST, 12, 4);
        return chestplate;
    }

    public static ItemStack createEmeraldLeggings() {
        ItemStack leggings = new ItemStack(Items.DIAMOND_LEGGINGS);
        leggings.set(DataComponents.CUSTOM_NAME,
                Component.literal("Emerald Leggings").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ EMERALD ARMOR ◆").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Armor: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("8").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal("Toughness: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("4").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        lore.add(Component.literal("✦ Agility I: +5% movement speed").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal("✦ Jump Boost I").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Leggings imbued with the").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("essence of prosperity.").withStyle(ChatFormatting.DARK_GRAY));
        leggings.set(DataComponents.LORE, new ItemLore(lore));
        leggings.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        leggings.set(DataComponents.DYED_COLOR, new DyedItemColor(0x50C878));
        
        setCustomItemId(leggings, "emerald_leggings");
        applyEmeraldArmorStats(leggings, EquipmentSlot.LEGS, 8, 4);
        return leggings;
    }

    public static ItemStack createEmeraldBoots() {
        ItemStack boots = new ItemStack(Items.DIAMOND_BOOTS);
        boots.set(DataComponents.CUSTOM_NAME,
                Component.literal("Emerald Boots").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ EMERALD ARMOR ◆").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Armor: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("4").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal("Toughness: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("4").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        lore.add(Component.literal("✦ Swiftness I: +10% movement speed").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal("✦ Feather Falling I").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Boots that grant swift").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("movement and protection.").withStyle(ChatFormatting.DARK_GRAY));
        boots.set(DataComponents.LORE, new ItemLore(lore));
        boots.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        boots.set(DataComponents.DYED_COLOR, new DyedItemColor(0x50C878));
        
        setCustomItemId(boots, "emerald_boots");
        applyEmeraldArmorStats(boots, EquipmentSlot.FEET, 4, 4);
        return boots;
    }

    // Emerald Tools
    public static ItemStack createEmeraldSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponents.CUSTOM_NAME,
                Component.literal("Emerald Sword").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ EMERALD WEAPON ◆").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Damage: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("12").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal("Speed: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("1.8").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        lore.add(Component.literal("✦ Vampiric I: +5% lifesteal on hit").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal("✦ Critical Strike I: +8% crit chance").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal(""));
        lore.add(Component.literal("A blade of pure emerald").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("that gleams with power.").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.DYED_COLOR, new DyedItemColor(0x50C878));
        
        setCustomItemId(sword, "emerald_sword");
        applyEmeraldWeaponStats(sword, 12, -2.1);
        return sword;
    }

    public static ItemStack createEmeraldPickaxe() {
        ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
        pickaxe.set(DataComponents.CUSTOM_NAME,
                Component.literal("Emerald Pickaxe").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ EMERALD TOOL ◆").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Mining Speed: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("Fast").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        lore.add(Component.literal("A pickaxe that mines with").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("the efficiency of wealth.").withStyle(ChatFormatting.DARK_GRAY));
        pickaxe.set(DataComponents.LORE, new ItemLore(lore));
        pickaxe.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        pickaxe.set(DataComponents.DYED_COLOR, new DyedItemColor(0x50C878));
        
        setCustomItemId(pickaxe, "emerald_pickaxe");
        return pickaxe;
    }

    public static ItemStack createEmeraldAxe() {
        ItemStack axe = new ItemStack(Items.IRON_AXE);
        axe.set(DataComponents.CUSTOM_NAME,
                Component.literal("Emerald Axe").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ EMERALD TOOL ◆").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Damage: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("15").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal("✦ Cleave I: +10% damage vs multiple targets").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal(""));
        lore.add(Component.literal("An axe that cleaves through").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("wood and foes alike.").withStyle(ChatFormatting.DARK_GRAY));
        axe.set(DataComponents.LORE, new ItemLore(lore));
        axe.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        axe.set(DataComponents.DYED_COLOR, new DyedItemColor(0x50C878));
        
        setCustomItemId(axe, "emerald_axe");
        applyEmeraldWeaponStats(axe, 15, -2.6);
        return axe;
    }

    public static ItemStack createEmeraldShovel() {
        ItemStack shovel = new ItemStack(Items.IRON_SHOVEL);
        shovel.set(DataComponents.CUSTOM_NAME,
                Component.literal("Emerald Shovel").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ EMERALD TOOL ◆").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("A shovel of emerald").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("for swift excavation.").withStyle(ChatFormatting.DARK_GRAY));
        shovel.set(DataComponents.LORE, new ItemLore(lore));
        shovel.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        shovel.set(DataComponents.DYED_COLOR, new DyedItemColor(0x50C878));
        
        setCustomItemId(shovel, "emerald_shovel");
        return shovel;
    }

    public static ItemStack createEmeraldHoe() {
        ItemStack hoe = new ItemStack(Items.IRON_HOE);
        hoe.set(DataComponents.CUSTOM_NAME,
                Component.literal("Emerald Hoe").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ EMERALD TOOL ◆").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("A hoe blessed by the").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("harvest goddess herself.").withStyle(ChatFormatting.DARK_GRAY));
        hoe.set(DataComponents.LORE, new ItemLore(lore));
        hoe.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        hoe.set(DataComponents.DYED_COLOR, new DyedItemColor(0x50C878));
        
        setCustomItemId(hoe, "emerald_hoe");
        return hoe;
    }

    // ============================================================
    // LAPIS GEAR (Iron+ tier, blue themed)
    // ============================================================

    public static ItemStack createLapisHelmet() {
        ItemStack helmet = new ItemStack(Items.IRON_HELMET);
        helmet.set(DataComponents.CUSTOM_NAME,
                Component.literal("Lapis Helmet").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ LAPIS ARMOR ◆").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Armor: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("4").withStyle(ChatFormatting.BLUE)));
        lore.add(Component.literal("Toughness: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("2").withStyle(ChatFormatting.BLUE)));
        lore.add(Component.literal(""));
        lore.add(Component.literal("✦ Mana Flow I: +10% mana regen").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal("✦ Night Vision").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal(""));
        lore.add(Component.literal("A helmet enchanted with").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("ancient lapis magic.").withStyle(ChatFormatting.DARK_GRAY));
        helmet.set(DataComponents.LORE, new ItemLore(lore));
        helmet.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        helmet.set(DataComponents.DYED_COLOR, new DyedItemColor(0x1E3A8A));
        
        setCustomItemId(helmet, "lapis_helmet");
        applyLapisArmorStats(helmet, EquipmentSlot.HEAD, 4);
        return helmet;
    }

    public static ItemStack createLapisChestplate() {
        ItemStack chestplate = new ItemStack(Items.IRON_CHESTPLATE);
        chestplate.set(DataComponents.CUSTOM_NAME,
                Component.literal("Lapis Chestplate").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ LAPIS ARMOR ◆").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Armor: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("10").withStyle(ChatFormatting.BLUE)));
        lore.add(Component.literal("Toughness: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("3").withStyle(ChatFormatting.BLUE)));
        lore.add(Component.literal(""));
        lore.add(Component.literal("✦ Enchanted: +25% XP from enchanting").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal("✦ Magic Shield I: 8% magic damage reduction").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal(""));
        lore.add(Component.literal("A chestplate radiating").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("mystical energy.").withStyle(ChatFormatting.DARK_GRAY));
        chestplate.set(DataComponents.LORE, new ItemLore(lore));
        chestplate.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        chestplate.set(DataComponents.DYED_COLOR, new DyedItemColor(0x1E3A8A));
        
        setCustomItemId(chestplate, "lapis_chestplate");
        applyLapisArmorStats(chestplate, EquipmentSlot.CHEST, 10);
        return chestplate;
    }

    public static ItemStack createLapisLeggings() {
        ItemStack leggings = new ItemStack(Items.IRON_LEGGINGS);
        leggings.set(DataComponents.CUSTOM_NAME,
                Component.literal("Lapis Leggings").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ LAPIS ARMOR ◆").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Armor: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("6").withStyle(ChatFormatting.BLUE)));
        lore.add(Component.literal("Toughness: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("2").withStyle(ChatFormatting.BLUE)));
        lore.add(Component.literal(""));
        lore.add(Component.literal("✦ Arcane Speed I: +5% movement").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal("✦ Mana Pool I: +50 max mana").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Leggings woven with").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("enchanted lapis threads.").withStyle(ChatFormatting.DARK_GRAY));
        leggings.set(DataComponents.LORE, new ItemLore(lore));
        leggings.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        leggings.set(DataComponents.DYED_COLOR, new DyedItemColor(0x1E3A8A));
        
        setCustomItemId(leggings, "lapis_leggings");
        applyLapisArmorStats(leggings, EquipmentSlot.LEGS, 6);
        return leggings;
    }

    public static ItemStack createLapisBoots() {
        ItemStack boots = new ItemStack(Items.IRON_BOOTS);
        boots.set(DataComponents.CUSTOM_NAME,
                Component.literal("Lapis Boots").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ LAPIS ARMOR ◆").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Armor: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("3").withStyle(ChatFormatting.BLUE)));
        lore.add(Component.literal("Toughness: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("2").withStyle(ChatFormatting.BLUE)));
        lore.add(Component.literal(""));
        lore.add(Component.literal("✦ Arcane Stride I: +8% movement speed").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal("✦ Soul Speed I").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Boots that shimmer with").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("arcane power.").withStyle(ChatFormatting.DARK_GRAY));
        boots.set(DataComponents.LORE, new ItemLore(lore));
        boots.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        boots.set(DataComponents.DYED_COLOR, new DyedItemColor(0x1E3A8A));
        
        setCustomItemId(boots, "lapis_boots");
        applyLapisArmorStats(boots, EquipmentSlot.FEET, 3);
        return boots;
    }

    // Lapis Tools
    public static ItemStack createLapisSword() {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponents.CUSTOM_NAME,
                Component.literal("Lapis Sword").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ LAPIS WEAPON ◆").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Damage: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("10").withStyle(ChatFormatting.BLUE)));
        lore.add(Component.literal("Speed: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("1.8").withStyle(ChatFormatting.BLUE)));
        lore.add(Component.literal(""));
        lore.add(Component.literal("✦ Arcane Boost I: +15% spell damage").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal("✦ Mana Siphon I: 3% mana on hit").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal(""));
        lore.add(Component.literal("A sword pulsing with").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("magical energy.").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.DYED_COLOR, new DyedItemColor(0x1E3A8A));
        
        setCustomItemId(sword, "lapis_sword");
        applyLapisWeaponStats(sword, 10, -2.1);
        return sword;
    }

    public static ItemStack createLapisPickaxe() {
        ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
        pickaxe.set(DataComponents.CUSTOM_NAME,
                Component.literal("Lapis Pickaxe").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ LAPIS TOOL ◆").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("A pickaxe infused with").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("enchanting power.").withStyle(ChatFormatting.DARK_GRAY));
        pickaxe.set(DataComponents.LORE, new ItemLore(lore));
        pickaxe.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        pickaxe.set(DataComponents.DYED_COLOR, new DyedItemColor(0x1E3A8A));
        
        setCustomItemId(pickaxe, "lapis_pickaxe");
        return pickaxe;
    }

    public static ItemStack createLapisAxe() {
        ItemStack axe = new ItemStack(Items.IRON_AXE);
        axe.set(DataComponents.CUSTOM_NAME,
                Component.literal("Lapis Axe").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ LAPIS TOOL ◆").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("An axe humming with").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("mystical resonance.").withStyle(ChatFormatting.DARK_GRAY));
        axe.set(DataComponents.LORE, new ItemLore(lore));
        axe.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        axe.set(DataComponents.DYED_COLOR, new DyedItemColor(0x1E3A8A));
        
        setCustomItemId(axe, "lapis_axe");
        return axe;
    }

    public static ItemStack createLapisShovel() {
        ItemStack shovel = new ItemStack(Items.IRON_SHOVEL);
        shovel.set(DataComponents.CUSTOM_NAME,
                Component.literal("Lapis Shovel").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ LAPIS TOOL ◆").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("A shovel blessed by").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("ancient magic.").withStyle(ChatFormatting.DARK_GRAY));
        shovel.set(DataComponents.LORE, new ItemLore(lore));
        shovel.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        shovel.set(DataComponents.DYED_COLOR, new DyedItemColor(0x1E3A8A));
        
        setCustomItemId(shovel, "lapis_shovel");
        return shovel;
    }

    public static ItemStack createLapisHoe() {
        ItemStack hoe = new ItemStack(Items.IRON_HOE);
        hoe.set(DataComponents.CUSTOM_NAME,
                Component.literal("Lapis Hoe").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ LAPIS TOOL ◆").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("A hoe touched by").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("enchantment.").withStyle(ChatFormatting.DARK_GRAY));
        hoe.set(DataComponents.LORE, new ItemLore(lore));
        hoe.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        hoe.set(DataComponents.DYED_COLOR, new DyedItemColor(0x1E3A8A));
        
        setCustomItemId(hoe, "lapis_hoe");
        return hoe;
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    private static void setCustomItemId(ItemStack stack, String id) {
        SlayerItems.setCustomItemId(stack, id);
    }

    public static boolean isLapisArmor(ItemStack stack) {
        String id = SlayerItems.getCustomItemId(stack);
        return id != null && id.startsWith("lapis_") && isArmorId(id);
    }

    public static boolean isEmeraldArmor(ItemStack stack) {
        String id = SlayerItems.getCustomItemId(stack);
        return id != null && id.startsWith("emerald_") && isArmorId(id);
    }

    private static boolean isArmorId(String id) {
        return id.endsWith("_helmet") || id.endsWith("_chestplate")
                || id.endsWith("_leggings") || id.endsWith("_boots");
    }

    public static int countLapisPieces(net.minecraft.world.entity.player.Player player) {
        int n = 0;
        if (isLapisArmor(player.getItemBySlot(EquipmentSlot.HEAD))) n++;
        if (isLapisArmor(player.getItemBySlot(EquipmentSlot.CHEST))) n++;
        if (isLapisArmor(player.getItemBySlot(EquipmentSlot.LEGS))) n++;
        if (isLapisArmor(player.getItemBySlot(EquipmentSlot.FEET))) n++;
        return n;
    }

    public static int countEmeraldPieces(net.minecraft.world.entity.player.Player player) {
        int n = 0;
        if (isEmeraldArmor(player.getItemBySlot(EquipmentSlot.HEAD))) n++;
        if (isEmeraldArmor(player.getItemBySlot(EquipmentSlot.CHEST))) n++;
        if (isEmeraldArmor(player.getItemBySlot(EquipmentSlot.LEGS))) n++;
        if (isEmeraldArmor(player.getItemBySlot(EquipmentSlot.FEET))) n++;
        return n;
    }

    public static double getLapisXpBonus(net.minecraft.world.entity.player.Player player) {
        return countLapisPieces(player) * 0.12;
    }

    public static double getEmeraldCoinBonus(net.minecraft.world.entity.player.Player player) {
        return countEmeraldPieces(player) * 0.07;
    }

    private static void applyEmeraldArmorStats(ItemStack stack, EquipmentSlot slot, int armor, int toughness) {
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        
        EquipmentSlotGroup modifierSlot = getModifierSlot(slot);
        
        builder.add(Attributes.ARMOR, new AttributeModifier(
                Identifier.fromNamespaceAndPath("political", "emerald_armor_" + slot.getName()),
                armor, AttributeModifier.Operation.ADD_VALUE),
                modifierSlot);
        
        builder.add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(
                Identifier.fromNamespaceAndPath("political", "emerald_toughness_" + slot.getName()),
                toughness, AttributeModifier.Operation.ADD_VALUE),
                modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }

    private static void applyLapisArmorStats(ItemStack stack, EquipmentSlot slot, int armor) {
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        
        EquipmentSlotGroup modifierSlot = getModifierSlot(slot);
        
        builder.add(Attributes.ARMOR, new AttributeModifier(
                Identifier.fromNamespaceAndPath("political", "lapis_armor_" + slot.getName()),
                armor, AttributeModifier.Operation.ADD_VALUE),
                modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }

    private static void applyEmeraldWeaponStats(ItemStack stack, double damage, double speed) {
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        
        builder.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                Identifier.fromNamespaceAndPath("political", "emerald_damage"),
                damage - 1, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND);
        
        builder.add(Attributes.ATTACK_SPEED, new AttributeModifier(
                Identifier.fromNamespaceAndPath("political", "emerald_speed"),
                speed, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }

    private static void applyLapisWeaponStats(ItemStack stack, double damage, double speed) {
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        
        builder.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                Identifier.fromNamespaceAndPath("political", "lapis_damage"),
                damage - 1, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND);
        
        builder.add(Attributes.ATTACK_SPEED, new AttributeModifier(
                Identifier.fromNamespaceAndPath("political", "lapis_speed"),
                speed, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }
    
    private static EquipmentSlotGroup getModifierSlot(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> EquipmentSlotGroup.HEAD;
            case CHEST -> EquipmentSlotGroup.CHEST;
            case LEGS -> EquipmentSlotGroup.LEGS;
            case FEET -> EquipmentSlotGroup.FEET;
            default -> EquipmentSlotGroup.MAINHAND;
        };
    }
}
