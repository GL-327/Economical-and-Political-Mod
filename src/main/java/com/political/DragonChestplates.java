package com.political;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.nbt.NbtCompound;
import com.political.ArmourAttribute;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DragonChestplates {
    // ============================================================
    // CUSTOM ITEM ID SYSTEM
    // ============================================================
    
    public static String getCustomItemId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        
        var customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData != null) {
            NbtCompound nbt = customData.copyNbt();
            return nbt.getString("custom_item_id").orElse(null);
        }
        return null;
    }
    
    public static boolean hasCustomItemId(ItemStack stack, String id) {
        return id.equals(getCustomItemId(stack));
    }
    
    public static void setCustomItemId(ItemStack stack, String id) {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("custom_item_id", id);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }
    
    // ============================================================
    // CUSTOM CORES
    // ============================================================
    
    public static ItemStack createCustomCoreT1() {
        ItemStack core = new ItemStack(Items.NETHER_STAR);
        core.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⚡ Custom Crafting Core T1").formatted(Formatting.YELLOW, Formatting.BOLD));
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[Crafting Component - Tier 1]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ USAGE ━━━").formatted(Formatting.GRAY));
        lore.add(Text.literal("• Used in advanced recipes").formatted(Formatting.YELLOW));
        lore.add(Text.literal("• Required for T2+ items").formatted(Formatting.YELLOW));
        lore.add(Text.literal("• Place in crafting grid").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8A concentrated essence of crafting").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        
        core.set(DataComponentTypes.LORE, new LoreComponent(lore));
        core.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(core, "custom_core_t1");
        return core;
    }
    
    public static ItemStack createCustomCoreT2() {
        ItemStack core = new ItemStack(Items.NETHER_STAR);
        core.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⚡ Custom Crafting Core T2").formatted(Formatting.AQUA, Formatting.BOLD));
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[Crafting Component - Tier 2]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ USAGE ━━━").formatted(Formatting.GRAY));
        lore.add(Text.literal("• Used in expert recipes").formatted(Formatting.AQUA));
        lore.add(Text.literal("• Required for T3+ items").formatted(Formatting.AQUA));
        lore.add(Text.literal("• Place in crafting grid").formatted(Formatting.AQUA));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8An advanced essence of creation").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        
        core.set(DataComponentTypes.LORE, new LoreComponent(lore));
        core.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(core, "custom_core_t2");
        return core;
    }
    
    public static ItemStack createCustomCoreT3() {
        ItemStack core = new ItemStack(Items.NETHER_STAR);
        core.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⚡ Custom Crafting Core T3").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[Crafting Component - Tier 3]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ USAGE ━━━").formatted(Formatting.GRAY));
        lore.add(Text.literal("• Used in master recipes").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("• Required for T4+ items").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("• Place in crafting grid").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8A master essence of fabrication").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        
        core.set(DataComponentTypes.LORE, new LoreComponent(lore));
        core.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(core, "custom_core_t3");
        return core;
    }
    
    // ============================================================
    // DRAGON CHESTPLATES - Special Items (No Recipe)
    // ============================================================
    
    public static ItemStack createDragonChestplate1() {
        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        chestplate.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🐉 Inferno Dragon Chestplate").formatted(Formatting.DARK_RED, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();

        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[Legendary Dragon Armor - Tier 1]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("❤ Max Health: 20 HP (10 hearts)").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armour: 12").formatted(Formatting.GRAY));
        lore.add(Text.literal("⚔ Toughness: 8").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ DRAGON ABILITY ━━━").formatted(Formatting.DARK_RED));
        lore.add(Text.literal("🔥 Fire Aura: Particle trail").formatted(Formatting.GOLD));
        lore.add(Text.literal("🔥 Immune to fire & lava").formatted(Formatting.GOLD));
        lore.add(Text.literal("🔥 Double damage when on fire").formatted(Formatting.GOLD));
        lore.add(Text.literal("🔥 Explosion at 2.5 hearts").formatted(Formatting.GOLD));
        lore.add(Text.literal("🔥 2x stats in Nether").formatted(Formatting.GOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Born from the depths of the Nether").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        chestplate.set(DataComponentTypes.LORE, new LoreComponent(lore));
        chestplate.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        chestplate.set(DataComponentTypes.MAX_DAMAGE, 50000);
        chestplate.set(DataComponentTypes.DAMAGE, 0);
        chestplate.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0x8B0000)); // Dark red
        
        // Apply armor stats
        AttributeModifiersComponent.Builder builder = AttributeModifiersComponent.builder();
        builder.add(EntityAttributes.ARMOR, new EntityAttributeModifier(
                Identifier.of("politicalserver", "inferno_dragon_armor"), 12.0,
                EntityAttributeModifier.Operation.ADD_VALUE
        ), AttributeModifierSlot.CHEST);
        builder.add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(
                Identifier.of("politicalserver", "inferno_dragon_toughness"), 8.0,
                EntityAttributeModifier.Operation.ADD_VALUE
        ), AttributeModifierSlot.CHEST);
        chestplate.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, builder.build());
        
        setCustomItemId(chestplate, "inferno_dragon_chestplate");
        return chestplate;
    }

    public static ItemStack createDragonChestplate2() {
        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        chestplate.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⚡ Storm Dragon Chestplate").formatted(Formatting.AQUA, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();

        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[Legendary Dragon Armor - Tier 2]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.AQUA));
        lore.add(Text.literal("❤ Max Health: 24 HP (12 hearts)").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armour: 14").formatted(Formatting.GRAY));
        lore.add(Text.literal("⚔ Toughness: 10").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ DRAGON ABILITY ━━━").formatted(Formatting.AQUA));
        lore.add(Text.literal("⚡ Storm Aura: Particle trail").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚡ Creative flight (1 heart while flying)").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚡ Immune to lightning").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚡ 1% chance to strike attackers").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚡ 2x health during thunderstorms").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Commands the fury of the storm").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        chestplate.set(DataComponentTypes.LORE, new LoreComponent(lore));
        chestplate.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        chestplate.set(DataComponentTypes.MAX_DAMAGE, 50000);
        chestplate.set(DataComponentTypes.DAMAGE, 0);
        chestplate.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0x00CED1)); // Dark cyan
        
        // Apply armor stats
        AttributeModifiersComponent.Builder builder2 = AttributeModifiersComponent.builder();
        builder2.add(EntityAttributes.ARMOR, new EntityAttributeModifier(
                Identifier.of("politicalserver", "storm_dragon_armor"), 14.0,
                EntityAttributeModifier.Operation.ADD_VALUE
        ), AttributeModifierSlot.CHEST);
        builder2.add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(
                Identifier.of("politicalserver", "storm_dragon_toughness"), 10.0,
                EntityAttributeModifier.Operation.ADD_VALUE
        ), AttributeModifierSlot.CHEST);
        chestplate.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, builder2.build());
        
        setCustomItemId(chestplate, "storm_dragon_chestplate");
        return chestplate;
    }

    public static ItemStack createDragonChestplate3() {
        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        chestplate.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🌑 Void Dragon Chestplate").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();

        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[Legendary Dragon Armor - Tier 3]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("❤ Max Health: 30 HP (15 hearts)").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armour: 16").formatted(Formatting.GRAY));
        lore.add(Text.literal("⚔ Toughness: 12").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ DRAGON ABILITY ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("🌀 Void Aura: Particle trail").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("🌀 Immune to magic & arrows").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("🌀 Invisibility on crouch (15s/10s cooldown)").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("🌀 Creative flight in The End").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Exists beyond the boundaries of reality").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        chestplate.set(DataComponentTypes.LORE, new LoreComponent(lore));
        chestplate.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        chestplate.set(DataComponentTypes.MAX_DAMAGE, 50000);
        chestplate.set(DataComponentTypes.DAMAGE, 0);
        chestplate.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0x9400D3)); // Dark violet
        
        // Apply armor stats
        AttributeModifiersComponent.Builder builder3 = AttributeModifiersComponent.builder();
        builder3.add(EntityAttributes.ARMOR, new EntityAttributeModifier(
                Identifier.of("politicalserver", "void_dragon_armor"), 16.0,
                EntityAttributeModifier.Operation.ADD_VALUE
        ), AttributeModifierSlot.CHEST);
        builder3.add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(
                Identifier.of("politicalserver", "void_dragon_toughness"), 12.0,
                EntityAttributeModifier.Operation.ADD_VALUE
        ), AttributeModifierSlot.CHEST);
        chestplate.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, builder3.build());
        
        setCustomItemId(chestplate, "void_dragon_chestplate");
        return chestplate;
    }

    public static ItemStack createDragonChestplateAurora() {
        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        chestplate.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🌈 Aurora Dragon Chestplate").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[Legendary Dragon Armor - Aurora]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("❤ Max Health: scales with ability").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armour: 18").formatted(Formatting.GRAY));
        lore.add(Text.literal("⚔ Toughness: 12").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ DRAGON ABILITY ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("✦ Flight + prismatic trail").formatted(Formatting.AQUA));
        lore.add(Text.literal("✦ While flying: 1 heart max (heavy strain)").formatted(Formatting.RED));
        lore.add(Text.literal("✦ Speed & regen when raining").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Born in polar lights").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        chestplate.set(DataComponentTypes.LORE, new LoreComponent(lore));
        chestplate.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        chestplate.set(DataComponentTypes.MAX_DAMAGE, 50000);
        chestplate.set(DataComponentTypes.DAMAGE, 0);
        chestplate.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0xFF66FF));
        AttributeModifiersComponent.Builder b = AttributeModifiersComponent.builder();
        b.add(EntityAttributes.ARMOR, new EntityAttributeModifier(
                Identifier.of("politicalserver", "aurora_dragon_armor"), 18.0,
                EntityAttributeModifier.Operation.ADD_VALUE
        ), AttributeModifierSlot.CHEST);
        b.add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(
                Identifier.of("politicalserver", "aurora_dragon_toughness"), 12.0,
                EntityAttributeModifier.Operation.ADD_VALUE
        ), AttributeModifierSlot.CHEST);
        chestplate.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, b.build());
        setCustomItemId(chestplate, "aurora_dragon_chestplate");
        return chestplate;
    }

    public static ItemStack createDragonChestplateFrost() {
        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        chestplate.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("❄ Frost Dragon Chestplate").formatted(Formatting.AQUA, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[Legendary Dragon Armor - Frost]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.AQUA));
        lore.add(Text.literal("🛡 Armour: 18").formatted(Formatting.GRAY));
        lore.add(Text.literal("⚔ Toughness: 13").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ DRAGON ABILITY ━━━").formatted(Formatting.AQUA));
        lore.add(Text.literal("❄ Snowflake trail + frost slow aura").formatted(Formatting.BLUE));
        lore.add(Text.literal("❄ Slowness in the Nether (cold imbalance)").formatted(Formatting.RED));
        lore.add(Text.literal("❄ Speed on snow or ice").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Heart of the blizzard").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        chestplate.set(DataComponentTypes.LORE, new LoreComponent(lore));
        chestplate.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        chestplate.set(DataComponentTypes.MAX_DAMAGE, 50000);
        chestplate.set(DataComponentTypes.DAMAGE, 0);
        chestplate.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0xB0E0E6));
        AttributeModifiersComponent.Builder b = AttributeModifiersComponent.builder();
        b.add(EntityAttributes.ARMOR, new EntityAttributeModifier(
                Identifier.of("politicalserver", "frost_dragon_armor"), 18.0,
                EntityAttributeModifier.Operation.ADD_VALUE
        ), AttributeModifierSlot.CHEST);
        b.add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(
                Identifier.of("politicalserver", "frost_dragon_toughness"), 13.0,
                EntityAttributeModifier.Operation.ADD_VALUE
        ), AttributeModifierSlot.CHEST);
        chestplate.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, b.build());
        setCustomItemId(chestplate, "frost_dragon_chestplate");
        return chestplate;
    }

    public static ItemStack createDragonChestplateVenom() {
        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        chestplate.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("☠ Venom Dragon Chestplate").formatted(Formatting.DARK_GREEN, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[Legendary Dragon Armor - Venom]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.GREEN));
        lore.add(Text.literal("🛡 Armour: 20").formatted(Formatting.GRAY));
        lore.add(Text.literal("⚔ Toughness: 14").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ DRAGON ABILITY ━━━").formatted(Formatting.DARK_GREEN));
        lore.add(Text.literal("☠ Toxic spores + poison cloud on hits").formatted(Formatting.GREEN));
        lore.add(Text.literal("☠ Hunger when poisoned (self-strain)").formatted(Formatting.RED));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Breath of the basilisk").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        chestplate.set(DataComponentTypes.LORE, new LoreComponent(lore));
        chestplate.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        chestplate.set(DataComponentTypes.MAX_DAMAGE, 50000);
        chestplate.set(DataComponentTypes.DAMAGE, 0);
        chestplate.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0x228B22));
        AttributeModifiersComponent.Builder b = AttributeModifiersComponent.builder();
        b.add(EntityAttributes.ARMOR, new EntityAttributeModifier(
                Identifier.of("politicalserver", "venom_dragon_armor"), 20.0,
                EntityAttributeModifier.Operation.ADD_VALUE
        ), AttributeModifierSlot.CHEST);
        b.add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(
                Identifier.of("politicalserver", "venom_dragon_toughness"), 14.0,
                EntityAttributeModifier.Operation.ADD_VALUE
        ), AttributeModifierSlot.CHEST);
        chestplate.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, b.build());
        setCustomItemId(chestplate, "venom_dragon_chestplate");
        return chestplate;
    }

    public static ItemStack createDragonChestplateBlood() {
        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        chestplate.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🩸 Blood Dragon Chestplate").formatted(Formatting.DARK_RED, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[Legendary Dragon Armor - Blood]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armour: 20").formatted(Formatting.GRAY));
        lore.add(Text.literal("⚔ Toughness: 14").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ DRAGON ABILITY ━━━").formatted(Formatting.DARK_RED));
        lore.add(Text.literal("🩸 Lifesteal on melee hits").formatted(Formatting.RED));
        lore.add(Text.literal("🩸 Wither when badly wounded").formatted(Formatting.DARK_RED));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8The heart still beats").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        chestplate.set(DataComponentTypes.LORE, new LoreComponent(lore));
        chestplate.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        chestplate.set(DataComponentTypes.MAX_DAMAGE, 50000);
        chestplate.set(DataComponentTypes.DAMAGE, 0);
        chestplate.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0x8B0000));
        AttributeModifiersComponent.Builder b = AttributeModifiersComponent.builder();
        b.add(EntityAttributes.ARMOR, new EntityAttributeModifier(
                Identifier.of("politicalserver", "blood_dragon_armor"), 20.0,
                EntityAttributeModifier.Operation.ADD_VALUE
        ), AttributeModifierSlot.CHEST);
        b.add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(
                Identifier.of("politicalserver", "blood_dragon_toughness"), 14.0,
                EntityAttributeModifier.Operation.ADD_VALUE
        ), AttributeModifierSlot.CHEST);
        chestplate.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, b.build());
        setCustomItemId(chestplate, "blood_dragon_chestplate");
        return chestplate;
    }

    public static ItemStack createDragonChestplateEclipse() {
        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        chestplate.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🌑 Eclipse Dragon Chestplate").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[Legendary Dragon Armor - Eclipse]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("🛡 Armour: 22").formatted(Formatting.GRAY));
        lore.add(Text.literal("⚔ Toughness: 16").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ DRAGON ABILITY ━━━").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("🌑 Night: flight + shadow trail").formatted(Formatting.GRAY));
        lore.add(Text.literal("🌑 While flying at night: wither strain").formatted(Formatting.RED));
        lore.add(Text.literal("🌑 Daylight: blindness in open sky").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8When the sun dies").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        chestplate.set(DataComponentTypes.LORE, new LoreComponent(lore));
        chestplate.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        chestplate.set(DataComponentTypes.MAX_DAMAGE, 50000);
        chestplate.set(DataComponentTypes.DAMAGE, 0);
        chestplate.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0x1C1C1C));
        AttributeModifiersComponent.Builder b = AttributeModifiersComponent.builder();
        b.add(EntityAttributes.ARMOR, new EntityAttributeModifier(
                Identifier.of("politicalserver", "eclipse_dragon_armor"), 22.0,
                EntityAttributeModifier.Operation.ADD_VALUE
        ), AttributeModifierSlot.CHEST);
        b.add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(
                Identifier.of("politicalserver", "eclipse_dragon_toughness"), 16.0,
                EntityAttributeModifier.Operation.ADD_VALUE
        ), AttributeModifierSlot.CHEST);
        chestplate.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, b.build());
        setCustomItemId(chestplate, "eclipse_dragon_chestplate");
        return chestplate;
    }
}
