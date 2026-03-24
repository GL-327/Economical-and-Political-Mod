package com.political;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

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
        helmet.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Emerald Helmet").formatted(Formatting.GREEN, Formatting.BOLD));
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ EMERALD ARMOR ◆").formatted(Formatting.GREEN, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Armor: ").formatted(Formatting.GRAY)
                .append(Text.literal("3").formatted(Formatting.GREEN)));
        lore.add(Text.literal("Toughness: ").formatted(Formatting.GRAY)
                .append(Text.literal("3").formatted(Formatting.GREEN)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("A helmet forged from pure").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("emeralds with ancient magic.").formatted(Formatting.DARK_GRAY));
        helmet.set(DataComponentTypes.LORE, new LoreComponent(lore));
        helmet.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        
        // Apply emerald color tint
        helmet.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0x50C878));
        
        setCustomItemId(helmet, "emerald_helmet");
        applyEmeraldArmorStats(helmet, EquipmentSlot.HEAD, 3, 3);
        return helmet;
    }

    public static ItemStack createEmeraldChestplate() {
        ItemStack chestplate = new ItemStack(Items.DIAMOND_CHESTPLATE);
        chestplate.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Emerald Chestplate").formatted(Formatting.GREEN, Formatting.BOLD));
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ EMERALD ARMOR ◆").formatted(Formatting.GREEN, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Armor: ").formatted(Formatting.GRAY)
                .append(Text.literal("8").formatted(Formatting.GREEN)));
        lore.add(Text.literal("Toughness: ").formatted(Formatting.GRAY)
                .append(Text.literal("3").formatted(Formatting.GREEN)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("A chestplate of legendary").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("craftsmanship and power.").formatted(Formatting.DARK_GRAY));
        chestplate.set(DataComponentTypes.LORE, new LoreComponent(lore));
        chestplate.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        chestplate.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0x50C878));
        
        setCustomItemId(chestplate, "emerald_chestplate");
        applyEmeraldArmorStats(chestplate, EquipmentSlot.CHEST, 8, 3);
        return chestplate;
    }

    public static ItemStack createEmeraldLeggings() {
        ItemStack leggings = new ItemStack(Items.DIAMOND_LEGGINGS);
        leggings.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Emerald Leggings").formatted(Formatting.GREEN, Formatting.BOLD));
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ EMERALD ARMOR ◆").formatted(Formatting.GREEN, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Armor: ").formatted(Formatting.GRAY)
                .append(Text.literal("6").formatted(Formatting.GREEN)));
        lore.add(Text.literal("Toughness: ").formatted(Formatting.GRAY)
                .append(Text.literal("3").formatted(Formatting.GREEN)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Leggings imbued with the").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("essence of prosperity.").formatted(Formatting.DARK_GRAY));
        leggings.set(DataComponentTypes.LORE, new LoreComponent(lore));
        leggings.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        leggings.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0x50C878));
        
        setCustomItemId(leggings, "emerald_leggings");
        applyEmeraldArmorStats(leggings, EquipmentSlot.LEGS, 6, 3);
        return leggings;
    }

    public static ItemStack createEmeraldBoots() {
        ItemStack boots = new ItemStack(Items.DIAMOND_BOOTS);
        boots.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Emerald Boots").formatted(Formatting.GREEN, Formatting.BOLD));
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ EMERALD ARMOR ◆").formatted(Formatting.GREEN, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Armor: ").formatted(Formatting.GRAY)
                .append(Text.literal("3").formatted(Formatting.GREEN)));
        lore.add(Text.literal("Toughness: ").formatted(Formatting.GRAY)
                .append(Text.literal("3").formatted(Formatting.GREEN)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Boots that grant swift").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("movement and protection.").formatted(Formatting.DARK_GRAY));
        boots.set(DataComponentTypes.LORE, new LoreComponent(lore));
        boots.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        boots.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0x50C878));
        
        setCustomItemId(boots, "emerald_boots");
        applyEmeraldArmorStats(boots, EquipmentSlot.FEET, 3, 3);
        return boots;
    }

    // Emerald Tools
    public static ItemStack createEmeraldSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Emerald Sword").formatted(Formatting.GREEN, Formatting.BOLD));
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ EMERALD WEAPON ◆").formatted(Formatting.GREEN, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Damage: ").formatted(Formatting.GRAY)
                .append(Text.literal("8").formatted(Formatting.GREEN)));
        lore.add(Text.literal("Speed: ").formatted(Formatting.GRAY)
                .append(Text.literal("1.7").formatted(Formatting.GREEN)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("A blade of pure emerald").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("that gleams with power.").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0x50C878));
        
        setCustomItemId(sword, "emerald_sword");
        applyEmeraldWeaponStats(sword, 8, -2.4);
        return sword;
    }

    public static ItemStack createEmeraldPickaxe() {
        ItemStack pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
        pickaxe.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Emerald Pickaxe").formatted(Formatting.GREEN, Formatting.BOLD));
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ EMERALD TOOL ◆").formatted(Formatting.GREEN, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Mining Speed: ").formatted(Formatting.GRAY)
                .append(Text.literal("Fast").formatted(Formatting.GREEN)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("A pickaxe that mines with").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("the efficiency of wealth.").formatted(Formatting.DARK_GRAY));
        pickaxe.set(DataComponentTypes.LORE, new LoreComponent(lore));
        pickaxe.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        pickaxe.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0x50C878));
        
        setCustomItemId(pickaxe, "emerald_pickaxe");
        return pickaxe;
    }

    public static ItemStack createEmeraldAxe() {
        ItemStack axe = new ItemStack(Items.DIAMOND_AXE);
        axe.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Emerald Axe").formatted(Formatting.GREEN, Formatting.BOLD));
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ EMERALD TOOL ◆").formatted(Formatting.GREEN, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Damage: ").formatted(Formatting.GRAY)
                .append(Text.literal("10").formatted(Formatting.GREEN)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("An axe that cleaves through").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("wood and foes alike.").formatted(Formatting.DARK_GRAY));
        axe.set(DataComponentTypes.LORE, new LoreComponent(lore));
        axe.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        axe.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0x50C878));
        
        setCustomItemId(axe, "emerald_axe");
        return axe;
    }

    public static ItemStack createEmeraldShovel() {
        ItemStack shovel = new ItemStack(Items.DIAMOND_SHOVEL);
        shovel.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Emerald Shovel").formatted(Formatting.GREEN, Formatting.BOLD));
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ EMERALD TOOL ◆").formatted(Formatting.GREEN, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("A shovel of emerald").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("for swift excavation.").formatted(Formatting.DARK_GRAY));
        shovel.set(DataComponentTypes.LORE, new LoreComponent(lore));
        shovel.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        shovel.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0x50C878));
        
        setCustomItemId(shovel, "emerald_shovel");
        return shovel;
    }

    public static ItemStack createEmeraldHoe() {
        ItemStack hoe = new ItemStack(Items.DIAMOND_HOE);
        hoe.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Emerald Hoe").formatted(Formatting.GREEN, Formatting.BOLD));
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ EMERALD TOOL ◆").formatted(Formatting.GREEN, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("A hoe blessed by the").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("harvest goddess herself.").formatted(Formatting.DARK_GRAY));
        hoe.set(DataComponentTypes.LORE, new LoreComponent(lore));
        hoe.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        hoe.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0x50C878));
        
        setCustomItemId(hoe, "emerald_hoe");
        return hoe;
    }

    // ============================================================
    // LAPIS GEAR (Iron+ tier, blue themed)
    // ============================================================

    public static ItemStack createLapisHelmet() {
        ItemStack helmet = new ItemStack(Items.IRON_HELMET);
        helmet.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Lapis Helmet").formatted(Formatting.BLUE, Formatting.BOLD));
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ LAPIS ARMOR ◆").formatted(Formatting.BLUE, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Armor: ").formatted(Formatting.GRAY)
                .append(Text.literal("2").formatted(Formatting.BLUE)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("A helmet enchanted with").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("ancient lapis magic.").formatted(Formatting.DARK_GRAY));
        helmet.set(DataComponentTypes.LORE, new LoreComponent(lore));
        helmet.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        helmet.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0x1E3A8A));
        
        setCustomItemId(helmet, "lapis_helmet");
        applyLapisArmorStats(helmet, EquipmentSlot.HEAD, 2);
        return helmet;
    }

    public static ItemStack createLapisChestplate() {
        ItemStack chestplate = new ItemStack(Items.IRON_CHESTPLATE);
        chestplate.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Lapis Chestplate").formatted(Formatting.BLUE, Formatting.BOLD));
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ LAPIS ARMOR ◆").formatted(Formatting.BLUE, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Armor: ").formatted(Formatting.GRAY)
                .append(Text.literal("6").formatted(Formatting.BLUE)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("A chestplate radiating").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("mystical energy.").formatted(Formatting.DARK_GRAY));
        chestplate.set(DataComponentTypes.LORE, new LoreComponent(lore));
        chestplate.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        chestplate.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0x1E3A8A));
        
        setCustomItemId(chestplate, "lapis_chestplate");
        applyLapisArmorStats(chestplate, EquipmentSlot.CHEST, 6);
        return chestplate;
    }

    public static ItemStack createLapisLeggings() {
        ItemStack leggings = new ItemStack(Items.IRON_LEGGINGS);
        leggings.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Lapis Leggings").formatted(Formatting.BLUE, Formatting.BOLD));
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ LAPIS ARMOR ◆").formatted(Formatting.BLUE, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Armor: ").formatted(Formatting.GRAY)
                .append(Text.literal("5").formatted(Formatting.BLUE)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Leggings woven with").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("enchanted lapis threads.").formatted(Formatting.DARK_GRAY));
        leggings.set(DataComponentTypes.LORE, new LoreComponent(lore));
        leggings.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        leggings.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0x1E3A8A));
        
        setCustomItemId(leggings, "lapis_leggings");
        applyLapisArmorStats(leggings, EquipmentSlot.LEGS, 5);
        return leggings;
    }

    public static ItemStack createLapisBoots() {
        ItemStack boots = new ItemStack(Items.IRON_BOOTS);
        boots.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Lapis Boots").formatted(Formatting.BLUE, Formatting.BOLD));
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ LAPIS ARMOR ◆").formatted(Formatting.BLUE, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Armor: ").formatted(Formatting.GRAY)
                .append(Text.literal("2").formatted(Formatting.BLUE)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Boots that shimmer with").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("arcane power.").formatted(Formatting.DARK_GRAY));
        boots.set(DataComponentTypes.LORE, new LoreComponent(lore));
        boots.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        boots.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0x1E3A8A));
        
        setCustomItemId(boots, "lapis_boots");
        applyLapisArmorStats(boots, EquipmentSlot.FEET, 2);
        return boots;
    }

    // Lapis Tools
    public static ItemStack createLapisSword() {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Lapis Sword").formatted(Formatting.BLUE, Formatting.BOLD));
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ LAPIS WEAPON ◆").formatted(Formatting.BLUE, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Damage: ").formatted(Formatting.GRAY)
                .append(Text.literal("7").formatted(Formatting.BLUE)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("A sword pulsing with").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("magical energy.").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0x1E3A8A));
        
        setCustomItemId(sword, "lapis_sword");
        applyLapisWeaponStats(sword, 7, -2.4);
        return sword;
    }

    public static ItemStack createLapisPickaxe() {
        ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
        pickaxe.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Lapis Pickaxe").formatted(Formatting.BLUE, Formatting.BOLD));
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ LAPIS TOOL ◆").formatted(Formatting.BLUE, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("A pickaxe infused with").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("enchanting power.").formatted(Formatting.DARK_GRAY));
        pickaxe.set(DataComponentTypes.LORE, new LoreComponent(lore));
        pickaxe.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        pickaxe.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0x1E3A8A));
        
        setCustomItemId(pickaxe, "lapis_pickaxe");
        return pickaxe;
    }

    public static ItemStack createLapisAxe() {
        ItemStack axe = new ItemStack(Items.IRON_AXE);
        axe.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Lapis Axe").formatted(Formatting.BLUE, Formatting.BOLD));
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ LAPIS TOOL ◆").formatted(Formatting.BLUE, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("An axe humming with").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("mystical resonance.").formatted(Formatting.DARK_GRAY));
        axe.set(DataComponentTypes.LORE, new LoreComponent(lore));
        axe.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        axe.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0x1E3A8A));
        
        setCustomItemId(axe, "lapis_axe");
        return axe;
    }

    public static ItemStack createLapisShovel() {
        ItemStack shovel = new ItemStack(Items.IRON_SHOVEL);
        shovel.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Lapis Shovel").formatted(Formatting.BLUE, Formatting.BOLD));
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ LAPIS TOOL ◆").formatted(Formatting.BLUE, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("A shovel blessed by").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("ancient magic.").formatted(Formatting.DARK_GRAY));
        shovel.set(DataComponentTypes.LORE, new LoreComponent(lore));
        shovel.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        shovel.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0x1E3A8A));
        
        setCustomItemId(shovel, "lapis_shovel");
        return shovel;
    }

    public static ItemStack createLapisHoe() {
        ItemStack hoe = new ItemStack(Items.IRON_HOE);
        hoe.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Lapis Hoe").formatted(Formatting.BLUE, Formatting.BOLD));
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ LAPIS TOOL ◆").formatted(Formatting.BLUE, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("A hoe touched by").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("enchantment.").formatted(Formatting.DARK_GRAY));
        hoe.set(DataComponentTypes.LORE, new LoreComponent(lore));
        hoe.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        hoe.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0x1E3A8A));
        
        setCustomItemId(hoe, "lapis_hoe");
        return hoe;
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    private static void setCustomItemId(ItemStack stack, String id) {
        SlayerItems.setCustomItemId(stack, id);
    }

    private static void applyEmeraldArmorStats(ItemStack stack, EquipmentSlot slot, int armor, int toughness) {
        AttributeModifiersComponent.Builder builder = AttributeModifiersComponent.builder();
        
        AttributeModifierSlot modifierSlot = getModifierSlot(slot);
        
        builder.add(EntityAttributes.ARMOR, new EntityAttributeModifier(
                Identifier.of("political", "emerald_armor_" + slot.getName()),
                armor, EntityAttributeModifier.Operation.ADD_VALUE),
                modifierSlot);
        
        builder.add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(
                Identifier.of("political", "emerald_toughness_" + slot.getName()),
                toughness, EntityAttributeModifier.Operation.ADD_VALUE),
                modifierSlot);
        
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, builder.build());
    }

    private static void applyLapisArmorStats(ItemStack stack, EquipmentSlot slot, int armor) {
        AttributeModifiersComponent.Builder builder = AttributeModifiersComponent.builder();
        
        AttributeModifierSlot modifierSlot = getModifierSlot(slot);
        
        builder.add(EntityAttributes.ARMOR, new EntityAttributeModifier(
                Identifier.of("political", "lapis_armor_" + slot.getName()),
                armor, EntityAttributeModifier.Operation.ADD_VALUE),
                modifierSlot);
        
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, builder.build());
    }

    private static void applyEmeraldWeaponStats(ItemStack stack, double damage, double speed) {
        AttributeModifiersComponent.Builder builder = AttributeModifiersComponent.builder();
        
        builder.add(EntityAttributes.ATTACK_DAMAGE, new EntityAttributeModifier(
                Identifier.of("political", "emerald_damage"),
                damage - 1, EntityAttributeModifier.Operation.ADD_VALUE),
                AttributeModifierSlot.MAINHAND);
        
        builder.add(EntityAttributes.ATTACK_SPEED, new EntityAttributeModifier(
                Identifier.of("political", "emerald_speed"),
                speed, EntityAttributeModifier.Operation.ADD_VALUE),
                AttributeModifierSlot.MAINHAND);
        
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, builder.build());
    }

    private static void applyLapisWeaponStats(ItemStack stack, double damage, double speed) {
        AttributeModifiersComponent.Builder builder = AttributeModifiersComponent.builder();
        
        builder.add(EntityAttributes.ATTACK_DAMAGE, new EntityAttributeModifier(
                Identifier.of("political", "lapis_damage"),
                damage - 1, EntityAttributeModifier.Operation.ADD_VALUE),
                AttributeModifierSlot.MAINHAND);
        
        builder.add(EntityAttributes.ATTACK_SPEED, new EntityAttributeModifier(
                Identifier.of("political", "lapis_speed"),
                speed, EntityAttributeModifier.Operation.ADD_VALUE),
                AttributeModifierSlot.MAINHAND);
        
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, builder.build());
    }
    
    private static AttributeModifierSlot getModifierSlot(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> AttributeModifierSlot.HEAD;
            case CHEST -> AttributeModifierSlot.CHEST;
            case LEGS -> AttributeModifierSlot.LEGS;
            case FEET -> AttributeModifierSlot.FEET;
            default -> AttributeModifierSlot.MAINHAND;
        };
    }
}
