package com.political.items;


import com.political.bounty.SlayerItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.CompoundTag;
import com.political.ArmourAttribute;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DragonChestplates {
    // ============================================================
    // CUSTOM ITEM ID SYSTEM
    // ============================================================
    
    public static String getCustomItemId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        
        var customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag nbt = customData.copyTag();
            return nbt.getString("custom_item_id").orElse(null);
        }
        return null;
    }
    
    public static boolean hasCustomItemId(ItemStack stack, String id) {
        return id.equals(getCustomItemId(stack));
    }
    
    public static void setCustomItemId(ItemStack stack, String id) {
        SlayerItems.setCustomItemId(stack, id);
    }
    
    // ============================================================
    // CUSTOM CORES
    // ============================================================
    
    public static ItemStack createCustomCoreT1() {
        ItemStack core = new ItemStack(Items.NETHER_STAR);
        core.set(DataComponents.CUSTOM_NAME,
                Component.literal("⚡ Custom Crafting Core T1").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[Crafting Component - Tier 1]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ USAGE ━━━").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("• Used in advanced recipes").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("• Required for T2+ items").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("• Place in crafting grid").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8A concentrated essence of crafting").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        
        core.set(DataComponents.LORE, new ItemLore(lore));
        core.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(core, "custom_core_t1");
        return core;
    }
    
    public static ItemStack createCustomCoreT2() {
        ItemStack core = new ItemStack(Items.NETHER_STAR);
        core.set(DataComponents.CUSTOM_NAME,
                Component.literal("⚡ Custom Crafting Core T2").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[Crafting Component - Tier 2]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ USAGE ━━━").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("• Used in expert recipes").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal("• Required for T3+ items").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal("• Place in crafting grid").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8An advanced essence of creation").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        
        core.set(DataComponents.LORE, new ItemLore(lore));
        core.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(core, "custom_core_t2");
        return core;
    }
    
    public static ItemStack createCustomCoreT3() {
        ItemStack core = new ItemStack(Items.NETHER_STAR);
        core.set(DataComponents.CUSTOM_NAME,
                Component.literal("⚡ Custom Crafting Core T3").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[Crafting Component - Tier 3]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ USAGE ━━━").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("• Used in master recipes").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal("• Required for T4+ items").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal("• Place in crafting grid").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8A master essence of fabrication").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        
        core.set(DataComponents.LORE, new ItemLore(lore));
        core.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(core, "custom_core_t3");
        return core;
    }
    
    // ============================================================
    // DRAGON CHESTPLATES - Special Items (No Recipe)
    // ============================================================
    
    public static ItemStack createDragonChestplate1() {
        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        chestplate.set(DataComponents.CUSTOM_NAME,
                Component.literal("🐉 Inferno Dragon Chestplate").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();

        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[Legendary Dragon Armor - Tier 1]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("❤ Max Health: 12 HP (6 hearts)").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🛡 Armour: 8").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Toughness: 5").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ DRAGON ABILITY ━━━").withStyle(ChatFormatting.DARK_RED));
        lore.add(Component.literal("🔥 Fire Aura: Particle trail").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("🔥 Immune to fire & lava").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("🔥 Double damage when on fire").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("🔥 Explosion at 2.5 hearts").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("🔥 2x stats in Nether").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Born from the depths of the Nether").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        chestplate.set(DataComponents.LORE, new ItemLore(lore));
        chestplate.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        chestplate.set(DataComponents.MAX_DAMAGE, 50000);
        chestplate.set(DataComponents.DAMAGE, 0);
        chestplate.set(DataComponents.DYED_COLOR, new DyedItemColor(0x8B0000)); // Dark red
        
        // Apply armor stats
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        builder.add(Attributes.ARMOR, new AttributeModifier(
                Identifier.fromNamespaceAndPath("politicalserver", "inferno_dragon_armor"), 8.0,
                AttributeModifier.Operation.ADD_VALUE
        ), EquipmentSlotGroup.CHEST);
        builder.add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(
                Identifier.fromNamespaceAndPath("politicalserver", "inferno_dragon_toughness"), 5.0,
                AttributeModifier.Operation.ADD_VALUE
        ), EquipmentSlotGroup.CHEST);
        chestplate.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
        
        setCustomItemId(chestplate, "inferno_dragon_chestplate");
        return chestplate;
    }

    public static ItemStack createDragonChestplate2() {
        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        chestplate.set(DataComponents.CUSTOM_NAME,
                Component.literal("⚡ Storm Dragon Chestplate").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();

        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[Legendary Dragon Armor - Tier 2]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal("❤ Max Health: 14 HP (7 hearts)").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🛡 Armour: 8").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Toughness: 5").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ DRAGON ABILITY ━━━").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal("⚡ Storm Aura: Particle trail").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚡ Creative flight (1 heart while flying)").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚡ Immune to lightning").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚡ 1% chance to strike attackers").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚡ 2x health during thunderstorms").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Commands the fury of the storm").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        chestplate.set(DataComponents.LORE, new ItemLore(lore));
        chestplate.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        chestplate.set(DataComponents.MAX_DAMAGE, 50000);
        chestplate.set(DataComponents.DAMAGE, 0);
        chestplate.set(DataComponents.DYED_COLOR, new DyedItemColor(0x00CED1)); // Dark cyan
        
        // Apply armor stats
        ItemAttributeModifiers.Builder builder2 = ItemAttributeModifiers.builder();
        builder2.add(Attributes.ARMOR, new AttributeModifier(
                Identifier.fromNamespaceAndPath("politicalserver", "storm_dragon_armor"), 8.0,
                AttributeModifier.Operation.ADD_VALUE
        ), EquipmentSlotGroup.CHEST);
        builder2.add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(
                Identifier.fromNamespaceAndPath("politicalserver", "storm_dragon_toughness"), 5.0,
                AttributeModifier.Operation.ADD_VALUE
        ), EquipmentSlotGroup.CHEST);
        chestplate.set(DataComponents.ATTRIBUTE_MODIFIERS, builder2.build());
        
        setCustomItemId(chestplate, "storm_dragon_chestplate");
        return chestplate;
    }

    public static ItemStack createDragonChestplate3() {
        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        chestplate.set(DataComponents.CUSTOM_NAME,
                Component.literal("🌑 Void Dragon Chestplate").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();

        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[Legendary Dragon Armor - Tier 3]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal("❤ Max Health: 16 HP (8 hearts)").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🛡 Armour: 8").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Toughness: 5").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ DRAGON ABILITY ━━━").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal("🌀 Void Aura: Particle trail").withStyle(ChatFormatting.DARK_PURPLE));
        lore.add(Component.literal("🌀 Immune to magic & arrows").withStyle(ChatFormatting.DARK_PURPLE));
        lore.add(Component.literal("🌀 Invisibility on crouch (15s/10s cooldown)").withStyle(ChatFormatting.DARK_PURPLE));
        lore.add(Component.literal("🌀 Creative flight in The End").withStyle(ChatFormatting.DARK_PURPLE));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Exists beyond the boundaries of reality").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        chestplate.set(DataComponents.LORE, new ItemLore(lore));
        chestplate.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        chestplate.set(DataComponents.MAX_DAMAGE, 50000);
        chestplate.set(DataComponents.DAMAGE, 0);
        chestplate.set(DataComponents.DYED_COLOR, new DyedItemColor(0x9400D3)); // Dark violet
        
        // Apply armor stats
        ItemAttributeModifiers.Builder builder3 = ItemAttributeModifiers.builder();
        builder3.add(Attributes.ARMOR, new AttributeModifier(
                Identifier.fromNamespaceAndPath("politicalserver", "void_dragon_armor"), 8.0,
                AttributeModifier.Operation.ADD_VALUE
        ), EquipmentSlotGroup.CHEST);
        builder3.add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(
                Identifier.fromNamespaceAndPath("politicalserver", "void_dragon_toughness"), 5.0,
                AttributeModifier.Operation.ADD_VALUE
        ), EquipmentSlotGroup.CHEST);
        chestplate.set(DataComponents.ATTRIBUTE_MODIFIERS, builder3.build());
        
        setCustomItemId(chestplate, "void_dragon_chestplate");
        return chestplate;
    }

    public static ItemStack createDragonChestplateAurora() {
        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        chestplate.set(DataComponents.CUSTOM_NAME,
                Component.literal("🌈 Aurora Dragon Chestplate").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[Legendary Dragon Armor - Aurora]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal("❤ Max Health: scales with ability").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🛡 Armour: 16").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Toughness: 10").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ DRAGON ABILITY ━━━").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal("✦ Flight + prismatic trail").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal("✦ While flying: 1 heart max (heavy strain)").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("✦ Speed & regen when raining").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Born in polar lights").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        chestplate.set(DataComponents.LORE, new ItemLore(lore));
        chestplate.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        chestplate.set(DataComponents.MAX_DAMAGE, 50000);
        chestplate.set(DataComponents.DAMAGE, 0);
        chestplate.set(DataComponents.DYED_COLOR, new DyedItemColor(0xFF66FF));
        ItemAttributeModifiers.Builder b = ItemAttributeModifiers.builder();
        b.add(Attributes.ARMOR, new AttributeModifier(
                Identifier.fromNamespaceAndPath("politicalserver", "aurora_dragon_armor"), 16.0,
                AttributeModifier.Operation.ADD_VALUE
        ), EquipmentSlotGroup.CHEST);
        b.add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(
                Identifier.fromNamespaceAndPath("politicalserver", "aurora_dragon_toughness"), 10.0,
                AttributeModifier.Operation.ADD_VALUE
        ), EquipmentSlotGroup.CHEST);
        chestplate.set(DataComponents.ATTRIBUTE_MODIFIERS, b.build());
        setCustomItemId(chestplate, "aurora_dragon_chestplate");
        return chestplate;
    }

    public static ItemStack createDragonChestplateFrost() {
        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        chestplate.set(DataComponents.CUSTOM_NAME,
                Component.literal("❄ Frost Dragon Chestplate").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[Legendary Dragon Armor - Frost]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal("🛡 Armour: 16").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Toughness: 11").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ DRAGON ABILITY ━━━").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal("❄ Snowflake trail + frost slow aura").withStyle(ChatFormatting.BLUE));
        lore.add(Component.literal("❄ Slowness in the Nether (cold imbalance)").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("❄ Speed on snow or ice").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Heart of the blizzard").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        chestplate.set(DataComponents.LORE, new ItemLore(lore));
        chestplate.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        chestplate.set(DataComponents.MAX_DAMAGE, 50000);
        chestplate.set(DataComponents.DAMAGE, 0);
        chestplate.set(DataComponents.DYED_COLOR, new DyedItemColor(0xB0E0E6));
        ItemAttributeModifiers.Builder b = ItemAttributeModifiers.builder();
        b.add(Attributes.ARMOR, new AttributeModifier(
                Identifier.fromNamespaceAndPath("politicalserver", "frost_dragon_armor"), 16.0,
                AttributeModifier.Operation.ADD_VALUE
        ), EquipmentSlotGroup.CHEST);
        b.add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(
                Identifier.fromNamespaceAndPath("politicalserver", "frost_dragon_toughness"), 11.0,
                AttributeModifier.Operation.ADD_VALUE
        ), EquipmentSlotGroup.CHEST);
        chestplate.set(DataComponents.ATTRIBUTE_MODIFIERS, b.build());
        setCustomItemId(chestplate, "frost_dragon_chestplate");
        return chestplate;
    }

    public static ItemStack createDragonChestplateVenom() {
        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        chestplate.set(DataComponents.CUSTOM_NAME,
                Component.literal("☠ Venom Dragon Chestplate").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[Legendary Dragon Armor - Venom]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("🛡 Armour: 8").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Toughness: 5").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ DRAGON ABILITY ━━━").withStyle(ChatFormatting.DARK_GREEN));
        lore.add(Component.literal("☠ Toxic spores + poison cloud on hits").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("☠ Hunger when poisoned (self-strain)").withStyle(ChatFormatting.RED));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Breath of the basilisk").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        chestplate.set(DataComponents.LORE, new ItemLore(lore));
        chestplate.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        chestplate.set(DataComponents.MAX_DAMAGE, 50000);
        chestplate.set(DataComponents.DAMAGE, 0);
        chestplate.set(DataComponents.DYED_COLOR, new DyedItemColor(0x228B22));
        ItemAttributeModifiers.Builder b = ItemAttributeModifiers.builder();
        b.add(Attributes.ARMOR, new AttributeModifier(
                Identifier.fromNamespaceAndPath("politicalserver", "venom_dragon_armor"), 8.0,
                AttributeModifier.Operation.ADD_VALUE
        ), EquipmentSlotGroup.CHEST);
        b.add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(
                Identifier.fromNamespaceAndPath("politicalserver", "venom_dragon_toughness"), 5.0,
                AttributeModifier.Operation.ADD_VALUE
        ), EquipmentSlotGroup.CHEST);
        chestplate.set(DataComponents.ATTRIBUTE_MODIFIERS, b.build());
        setCustomItemId(chestplate, "venom_dragon_chestplate");
        return chestplate;
    }

    public static ItemStack createDragonChestplateBlood() {
        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        chestplate.set(DataComponents.CUSTOM_NAME,
                Component.literal("🩸 Blood Dragon Chestplate").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[Legendary Dragon Armor - Blood]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🛡 Armour: 8").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Toughness: 5").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ DRAGON ABILITY ━━━").withStyle(ChatFormatting.DARK_RED));
        lore.add(Component.literal("🩸 Lifesteal on melee hits").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🩸 Wither when badly wounded").withStyle(ChatFormatting.DARK_RED));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8The heart still beats").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        chestplate.set(DataComponents.LORE, new ItemLore(lore));
        chestplate.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        chestplate.set(DataComponents.MAX_DAMAGE, 50000);
        chestplate.set(DataComponents.DAMAGE, 0);
        chestplate.set(DataComponents.DYED_COLOR, new DyedItemColor(0x8B0000));
        ItemAttributeModifiers.Builder b = ItemAttributeModifiers.builder();
        b.add(Attributes.ARMOR, new AttributeModifier(
                Identifier.fromNamespaceAndPath("politicalserver", "blood_dragon_armor"), 8.0,
                AttributeModifier.Operation.ADD_VALUE
        ), EquipmentSlotGroup.CHEST);
        b.add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(
                Identifier.fromNamespaceAndPath("politicalserver", "blood_dragon_toughness"), 5.0,
                AttributeModifier.Operation.ADD_VALUE
        ), EquipmentSlotGroup.CHEST);
        chestplate.set(DataComponents.ATTRIBUTE_MODIFIERS, b.build());
        setCustomItemId(chestplate, "blood_dragon_chestplate");
        return chestplate;
    }

    public static ItemStack createDragonChestplateEclipse() {
        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        chestplate.set(DataComponents.CUSTOM_NAME,
                Component.literal("🌑 Eclipse Dragon Chestplate").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[Legendary Dragon Armor - Eclipse]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal("🛡 Armour: 20").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Toughness: 14").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ DRAGON ABILITY ━━━").withStyle(ChatFormatting.DARK_PURPLE));
        lore.add(Component.literal("🌑 Night: flight + shadow trail").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("🌑 While flying at night: wither strain").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🌑 Daylight: blindness in open sky").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8When the sun dies").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        chestplate.set(DataComponents.LORE, new ItemLore(lore));
        chestplate.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        chestplate.set(DataComponents.MAX_DAMAGE, 50000);
        chestplate.set(DataComponents.DAMAGE, 0);
        chestplate.set(DataComponents.DYED_COLOR, new DyedItemColor(0x1C1C1C));
        ItemAttributeModifiers.Builder b = ItemAttributeModifiers.builder();
        b.add(Attributes.ARMOR, new AttributeModifier(
                Identifier.fromNamespaceAndPath("politicalserver", "eclipse_dragon_armor"), 20.0,
                AttributeModifier.Operation.ADD_VALUE
        ), EquipmentSlotGroup.CHEST);
        b.add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(
                Identifier.fromNamespaceAndPath("politicalserver", "eclipse_dragon_toughness"), 14.0,
                AttributeModifier.Operation.ADD_VALUE
        ), EquipmentSlotGroup.CHEST);
        chestplate.set(DataComponents.ATTRIBUTE_MODIFIERS, b.build());
        setCustomItemId(chestplate, "eclipse_dragon_chestplate");
        return chestplate;
    }
}
