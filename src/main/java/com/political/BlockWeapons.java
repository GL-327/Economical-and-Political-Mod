package com.political;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.*;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Block-based weapons that match BlockArmor sets.
 * Each weapon has stats and abilities themed after its block material.
 */
public class BlockWeapons {

    /** Extra flat attack damage on generic block swords; lore matches 1 + (atk mod + this). */
    private static final double GENERIC_BLOCK_SWORD_DAMAGE_BUFF = 1.5;

    // ============================================================
    // WEAPON CREATION METHODS - Each matches a BlockArmor set
    // ============================================================

    public static ItemStack createGlassSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponents.CUSTOM_NAME,
                Component.literal("⚔ Glass Blade").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        
        // Add damage attribute (diamond sword base 10, add -1 for fast attack = 9)
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(Identifier.fromNamespaceAndPath("political", "glass_sword_damage"), 2,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(Identifier.fromNamespaceAndPath("political", "glass_sword_speed"), 0.5,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal("⚔ Damage: 3").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 50").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Fast").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        lore.add(Component.literal("✨ ABILITY: Shatter Strike").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        lore.add(Component.literal("• 32% chance for 1.65x critical hit").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("• Extra durability loss on hit").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "glass");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Fragile but blindingly fast").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 50);
        return completePoliticalSword(sword, "glass_sword");
    }

    public static ItemStack createObsidianSword() {
        ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME,
                Component.literal("⚔ Obsidian Blade").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "obsidian_sword_dmg"), 6, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "obsidian_sword_spd"), -0.3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_PURPLE));
        lore.add(Component.literal("⚔ Damage: 7").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 2000").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Slow").withStyle(ChatFormatting.RED));
        lore.add(Component.literal(""));
        lore.add(Component.literal("✨ ABILITY: Crushing Blow").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        lore.add(Component.literal("• 1.32x damage multiplier").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("• Massive knockback on hit").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "obsidian");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Slow but nearly indestructible").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 2000);
        return completePoliticalSword(sword, "obsidian_sword");
    }

    public static ItemStack createQuartzSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponents.CUSTOM_NAME,
                Component.literal("⚔ Quartz Blade").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "quartz_sword_dmg"), 4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "quartz_sword_spd"), 0.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("⚔ Damage: 5").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 300").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal(""));
        lore.add(Component.literal("✨ ABILITY: Crystal Crit").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        lore.add(Component.literal("• Every 3rd hit: 2.25x critical!").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("• Crits apply Glowing to target").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "quartz");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Crystalline sharpness reflects damage").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 300);
        return completePoliticalSword(sword, "quartz_sword");
    }

    public static ItemStack createGlowstoneSword() {
        ItemStack sword = new ItemStack(Items.GOLDEN_SWORD);
        sword.set(DataComponents.CUSTOM_NAME,
                Component.literal("⚔ Glowstone Blade").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "glowstone_sword_dmg"), 6, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "glowstone_sword_spd"), 0.4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Damage: 7").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 200").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Fast").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        lore.add(Component.literal("✨ ABILITY: Radiant Strike").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        lore.add(Component.literal("• 1.45x damage vs undead in darkness").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("• Heals 1 HP per hit").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "glowstone");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Radiant edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 200);
        return completePoliticalSword(sword, "glowstone_sword");
    }

    public static ItemStack createRedstoneSword() {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponents.CUSTOM_NAME,
                Component.literal("⚔ Redstone Blade").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "redstone_sword_dmg"), 2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "redstone_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Damage: 3").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 250").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal(""));
        lore.add(Component.literal("✨ ABILITY: Electric Stun").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        lore.add(Component.literal("• Stuns enemies on hit (cooldown)").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("• Slowness II + Weakness I (3s)").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "redstone");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Powered by redstone energy").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 250);
        return completePoliticalSword(sword, "redstone_sword");
    }

    public static ItemStack createNetherrackSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME,
                Component.literal("⚔ Netherrack Blade").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "netherrack_sword_dmg"), 3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "netherrack_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_RED));
        lore.add(Component.literal("⚔ Damage: 4").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 180").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal(""));
        lore.add(Component.literal("✨ ABILITY: Infernal Edge").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        lore.add(Component.literal("• Sets targets on fire (6s)").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("• +32% damage in the Nether").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "netherrack");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Forged in the Nether's heat").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 180);
        return completePoliticalSword(sword, "netherrack_sword");
    }

    public static ItemStack createEndstoneSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponents.CUSTOM_NAME,
                Component.literal("⚔ Endstone Blade").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "endstone_sword_dmg"), 5, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "endstone_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("⚔ Damage: 6").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 350").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal(""));
        lore.add(Component.literal("✨ ABILITY: Void Shift").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        lore.add(Component.literal("• 8% chance to teleport target away").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "endstone");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Touched by the Void").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 350);
        return completePoliticalSword(sword, "endstone_sword");
    }

    public static ItemStack createIceSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponents.CUSTOM_NAME,
                Component.literal("⚔ Ice Blade").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "ice_sword_dmg"), 4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "ice_sword_spd"), 0.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal("⚔ Damage: 5").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 120").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal(""));
        lore.add(Component.literal("✨ ABILITY: Frostbite").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        lore.add(Component.literal("• Slowness II on hit (4s)").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("• +22% bonus vs already-slowed targets").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "ice");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Frozen in eternal cold").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 120);
        return completePoliticalSword(sword, "ice_sword");
    }

    public static ItemStack createPrismarineSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponents.CUSTOM_NAME,
                Component.literal("⚔ Prismarine Blade").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "prismarine_sword_dmg"), 4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "prismarine_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_AQUA));
        lore.add(Component.literal("⚔ Damage: 5").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 280").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal(""));
        lore.add(Component.literal("✨ ABILITY: Tidal Edge").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD));
        lore.add(Component.literal("• +38% damage in water/rain").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("• Mining Fatigue on hit").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "prismarine");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Gift of the Ocean Monument").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 280);
        return completePoliticalSword(sword, "prismarine_sword");
    }

    public static ItemStack createTerracottaSword() {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponents.CUSTOM_NAME,
                Component.literal("⚔ Terracotta Blade").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "terracotta_sword_dmg"), 2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "terracotta_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("⚔ Damage: 3").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 400").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal(""));
        lore.add(Component.literal("✨ ABILITY: Clay Slam").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        lore.add(Component.literal("• Heavy knockback on hit").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("• +18% damage while on ground").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "terracotta");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Hardened by fire and time").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 400);
        return completePoliticalSword(sword, "terracotta_sword");
    }

    public static ItemStack createMossySword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME,
                Component.literal("⚔ Mossy Blade").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "mossy_sword_dmg"), 3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "mossy_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GREEN));
        lore.add(Component.literal("⚔ Damage: 4").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 450").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal(""));
        lore.add(Component.literal("✨ ABILITY: Venomous Growth").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD));
        lore.add(Component.literal("• Poison on hit (3s)").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("• 1.1x damage multiplier").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "mossy");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Ancient stone overgrown with moss").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 450);
        return completePoliticalSword(sword, "mossy_sword");
    }

    public static ItemStack createSoulSandSword() {
        ItemStack sword = new ItemStack(Items.GOLDEN_SWORD);
        sword.set(DataComponents.CUSTOM_NAME,
                Component.literal("⚔ Soul Blade").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "soulsand_sword_dmg"), 7, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "soulsand_sword_spd"), 0.3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 220").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal(""));
        lore.add(Component.literal("✨ ABILITY: Soul Drain").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        lore.add(Component.literal("• Slowness II + Wither on hit").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "soulsand");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Infused with the souls of the nether").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 220);
        return completePoliticalSword(sword, "soulsand_sword");
    }

    public static ItemStack createMagmaSword() {
        ItemStack sword = new ItemStack(Items.GOLDEN_SWORD);
        sword.set(DataComponents.CUSTOM_NAME,
                Component.literal("⚔ Magma Blade").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "magma_sword_dmg"), 7, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "magma_sword_spd"), 0.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 350").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal(""));
        lore.add(Component.literal("✨ ABILITY: Inferno Strike").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        lore.add(Component.literal("• Sets targets ablaze for 8 seconds").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("• 1.12x damage multiplier").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "magma");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Burning with eternal heat").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 350);
        return completePoliticalSword(sword, "magma_sword");
    }

    public static ItemStack createSandstoneSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME,
                Component.literal("⚔ Sandstone Blade").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "sandstone_sword_dmg"), 3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "sandstone_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Damage: 4").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 280").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal(""));
        lore.add(Component.literal("✨ ABILITY: Desert Wind").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        lore.add(Component.literal("• +15% damage at high altitude").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "sandstone");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Carved from ancient desert stone").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 280);
        return completePoliticalSword(sword, "sandstone_sword");
    }

    public static ItemStack createAmethystSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponents.CUSTOM_NAME,
                Component.literal("⚔ Amethyst Blade").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "amethyst_sword_dmg"), 6, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "amethyst_sword_spd"), 0.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal("⚔ Damage: 7").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 320").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal(""));
        lore.add(Component.literal("✨ ABILITY: Echo Resonance").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        lore.add(Component.literal("• 22% chance for 1.35x crit + Weakness").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "amethyst");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Resonant crystal wards off magic").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 320);
        return completePoliticalSword(sword, "amethyst_sword");
    }

    public static ItemStack createCoalSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME,
                Component.literal("⚔ Coal Blade").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "coal_sword_dmg"), 4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "coal_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("⚔ Damage: 5").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 200").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal(""));
        lore.add(Component.literal("✨ ABILITY: Shadow Strike").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        lore.add(Component.literal("• Blindness on hit (2.5s)").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("• 1.14x damage multiplier").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "coal");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Dark as the deepest caves").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 200);
        return completePoliticalSword(sword, "coal_sword");
    }

    // ============================================================
    // 36+ NEW BLOCK SWORDS - Matching BlockArmor sets
    // ============================================================

    public static ItemStack createDiamondBlockSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Diamond Block Blade").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "diamond_block_sword_dmg"), 6, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "diamond_block_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal("⚔ Damage: 13").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 3000").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Sharpness V effect").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "diamond_block");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Ultimate luxury").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 3000);
        return completePoliticalSword(sword, "diamond_block_sword");
    }

    public static ItemStack createEmeraldBlockSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Emerald Block Blade").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "emerald_block_sword_dmg"), 8, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "emerald_block_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("⚔ Damage: 15").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 400").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Looting II effect").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "emerald_block");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8The merchant's blade").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 400);
        return completePoliticalSword(sword, "emerald_block_sword");
    }

    public static ItemStack createGoldBlockSword() {
        ItemStack sword = new ItemStack(Items.GOLDEN_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Gold Block Blade").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "gold_block_sword_dmg"), 7, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "gold_block_sword_spd"), 0.4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Damage: 11").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 150").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Fortune II on kills").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "gold_block");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Gilded glory").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 150);
        return completePoliticalSword(sword, "gold_block_sword");
    }

    public static ItemStack createIronBlockSword() {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Iron Block Blade").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "iron_block_sword_dmg"), 2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "iron_block_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 600").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Magnetic pull on hit").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "iron_block");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Industrial grade").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 600);
        return completePoliticalSword(sword, "iron_block_sword");
    }

    public static ItemStack createLapisBlockSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Lapis Block Blade").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "lapis_block_sword_dmg"), 9, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "lapis_block_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.BLUE));
        lore.add(Component.literal("⚔ Damage: 16").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 350").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ XP bonus on kill").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "lapis_block");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Knowledge edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 350);
        return completePoliticalSword(sword, "lapis_block_sword");
    }

    public static ItemStack createCopperBlockSword() {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Copper Block Blade").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "copper_block_sword_dmg"), 3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "copper_block_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("⚔ Damage: 9").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 280").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Lightning charged strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "copper_block");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Conductive edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 280);
        return completePoliticalSword(sword, "copper_block_sword");
    }

    public static ItemStack createAncientDebrisSword() {
        ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Ancient Debris Blade").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "ancient_debris_sword_dmg"), 6, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "ancient_debris_sword_spd"), -0.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("⚔ Damage: 14").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 2500").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Fire Aspect II").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "ancient_debris");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Ancient edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 2500);
        return completePoliticalSword(sword, "ancient_debris_sword");
    }

    public static ItemStack createBasaltSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Basalt Blade").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "basalt_sword_dmg"), 4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "basalt_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Damage: 9").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 500").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Lava walker strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "basalt");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Volcanic edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 500);
        return completePoliticalSword(sword, "basalt_sword");
    }

    public static ItemStack createBlackstoneSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Blackstone Blade").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "blackstone_sword_dmg"), 3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "blackstone_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 450").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Dark strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "blackstone");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Dark fortress edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 450);
        return completePoliticalSword(sword, "blackstone_sword");
    }

    public static ItemStack createBoneBlockSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Bone Block Blade").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "bone_block_sword_dmg"), 3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "bone_block_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 200").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Undead bane").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "bone_block");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Skeleton's edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 200);
        return completePoliticalSword(sword, "bone_block_sword");
    }

    public static ItemStack createBrickSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Brick Blade").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "brick_sword_dmg"), 3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "brick_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 380").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Heavy knockback").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "brick");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Builder's edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 380);
        return completePoliticalSword(sword, "brick_sword");
    }

    public static ItemStack createCactusSword() {
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Cactus Blade").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "cactus_sword_dmg"), 4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "cactus_sword_spd"), 0.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GREEN));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 180").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Thorns on hit").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "cactus");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Prickly edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 180);
        return completePoliticalSword(sword, "cactus_sword");
    }

    public static ItemStack createCalciteSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Calcite Blade").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "calcite_sword_dmg"), 6, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "calcite_sword_spd"), 0.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("⚔ Damage: 13").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 340").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Purifying strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "calcite");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Crystal edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 340);
        return completePoliticalSword(sword, "calcite_sword");
    }

    public static ItemStack createDeepslateSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Deepslate Blade").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "deepslate_sword_dmg"), 4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "deepslate_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("⚔ Damage: 9").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 550").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Mining strike bonus").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "deepslate");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Deep edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 550);
        return completePoliticalSword(sword, "deepslate_sword");
    }

    public static ItemStack createDripstoneSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Dripstone Blade").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "dripstone_sword_dmg"), 3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "dripstone_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 360").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Piercing strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "dripstone");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Sharp edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 360);
        return completePoliticalSword(sword, "dripstone_sword");
    }

    public static ItemStack createHaySword() {
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Hay Blade").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "hay_sword_dmg"), 4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "hay_sword_spd"), 0.3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 100").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Soft strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "hay");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Soft edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 100);
        return completePoliticalSword(sword, "hay_sword");
    }

    public static ItemStack createHoneycombSword() {
        ItemStack sword = new ItemStack(Items.GOLDEN_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Honeycomb Blade").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "honeycomb_sword_dmg"), 7, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "honeycomb_sword_spd"), 0.3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("⚔ Damage: 11").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 220").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Sticky strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "honeycomb");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Sweet edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 220);
        return completePoliticalSword(sword, "honeycomb_sword");
    }

    public static ItemStack createLilyPadSword() {
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Lily Pad Blade").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "lily_pad_sword_dmg"), 4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "lily_pad_sword_spd"), 0.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GREEN));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 160").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Water affinity").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "lily_pad");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Floating edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 160);
        return completePoliticalSword(sword, "lily_pad_sword");
    }

    public static ItemStack createMelonSword() {
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Melon Blade").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "melon_sword_dmg"), 4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "melon_sword_spd"), 0.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 170").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Juicy strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "melon");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Refreshing edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 170);
        return completePoliticalSword(sword, "melon_sword");
    }

    public static ItemStack createMossBlockSword() {
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Moss Block Blade").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "moss_block_sword_dmg"), 4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "moss_block_sword_spd"), 0.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GREEN));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 175").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Nature strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "moss_block");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Overgrown edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 175);
        return completePoliticalSword(sword, "moss_block_sword");
    }

    public static ItemStack createMyceliumSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Mycelium Blade").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "mycelium_sword_dmg"), 4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "mycelium_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_PURPLE));
        lore.add(Component.literal("⚔ Damage: 9").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 290").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Spore strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "mycelium");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Fungal edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 290);
        return completePoliticalSword(sword, "mycelium_sword");
    }

    public static ItemStack createNetherBrickSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Nether Brick Blade").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "nether_brick_sword_dmg"), 3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "nether_brick_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_RED));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 290").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Fire damage").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "nether_brick");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Hellish edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 290);
        return completePoliticalSword(sword, "nether_brick_sword");
    }

    public static ItemStack createPumpkinSword() {
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Pumpkin Blade").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "pumpkin_sword_dmg"), 4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "pumpkin_sword_spd"), 0.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 190").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Halloween strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "pumpkin");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Soft edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 190);
        return completePoliticalSword(sword, "pumpkin_sword");
    }

    public static ItemStack createPurpurSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Purpur Blade").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "purpur_sword_dmg"), 6, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "purpur_sword_spd"), 0.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal("⚔ Damage: 13").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 420").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ End strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "purpur");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8End city edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 420);
        return completePoliticalSword(sword, "purpur_sword");
    }

    public static ItemStack createSandSword() {
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Sand Blade").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "sand_sword_dmg"), 4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "sand_sword_spd"), 0.3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 140").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Shifting strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "sand");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Shifting edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 140);
        return completePoliticalSword(sword, "sand_sword");
    }

    public static ItemStack createSculkSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Sculk Blade").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "sculk_sword_dmg"), 6, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "sculk_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_AQUA));
        lore.add(Component.literal("⚔ Damage: 13").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 580").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Silent strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "sculk");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Dark sensor edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 580);
        return completePoliticalSword(sword, "sculk_sword");
    }

    public static ItemStack createShroomlightSword() {
        ItemStack sword = new ItemStack(Items.GOLDEN_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Shroomlight Blade").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "shroomlight_sword_dmg"), 7, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "shroomlight_sword_spd"), 0.3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal("⚔ Damage: 11").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 310").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Light strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "shroomlight");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Bioluminescent edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 310);
        return completePoliticalSword(sword, "shroomlight_sword");
    }

    public static ItemStack createSlimeSword() {
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Slime Blade").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "slime_sword_dmg"), 4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "slime_sword_spd"), 0.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 165").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Bouncy strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "slime");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Boing edge!").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 165);
        return completePoliticalSword(sword, "slime_sword");
    }

    public static ItemStack createSmoothStoneSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Smooth Stone Blade").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "smooth_stone_sword_dmg"), 3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "smooth_stone_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 440").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Smooth strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "smooth_stone");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Refined edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 440);
        return completePoliticalSword(sword, "smooth_stone_sword");
    }

    public static ItemStack createSnowSword() {
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Snow Blade").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "snow_sword_dmg"), 4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "snow_sword_spd"), 0.3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 130").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Freezing strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "snow");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Frozen edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 130);
        return completePoliticalSword(sword, "snow_sword");
    }

    public static ItemStack createSoulSoilSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Soul Soil Blade").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "soul_soil_sword_dmg"), 3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "soul_soil_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 410").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Soul fire strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "soul_soil");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Soul powered edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 410);
        return completePoliticalSword(sword, "soul_soil_sword");
    }

    public static ItemStack createSpongeSword() {
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Sponge Blade").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "sponge_sword_dmg"), 4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "sponge_sword_spd"), 0.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 185").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Absorbing strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "sponge");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Absorbent edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 185);
        return completePoliticalSword(sword, "sponge_sword");
    }

    public static ItemStack createTargetSword() {
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Target Blade").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "target_sword_dmg"), 4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "target_sword_spd"), 0.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 195").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Precise strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "target");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Bullseye edge!").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 195);
        return completePoliticalSword(sword, "target_sword");
    }

    public static ItemStack createTntSword() {
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ TNT Blade").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "tnt_sword_dmg"), 4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "tnt_sword_spd"), 0.3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 120").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Explosive strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "tnt");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§4Careful!").withStyle(ChatFormatting.DARK_RED));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 120);
        return completePoliticalSword(sword, "tnt_sword");
    }

    public static ItemStack createWarpedSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Warped Blade").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "warped_sword_dmg"), 3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "warped_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_AQUA));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 350").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Warped strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "warped");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Warped edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 350);
        return completePoliticalSword(sword, "warped_sword");
    }

    public static ItemStack createWetSpongeSword() {
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Wet Sponge Blade").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "wet_sponge_sword_dmg"), 4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "wet_sponge_sword_spd"), 0.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GREEN));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 180").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Wet strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "wet_sponge");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Soggy edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 180);
        return completePoliticalSword(sword, "wet_sponge_sword");
    }
    
    public static ItemStack createCrimsonStemSword() {
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Crimson Stem Blade").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "crimson_stem_sword_dmg"), 4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "crimson_stem_sword_spd"), 0.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_RED));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 250").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Fungal strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "crimson_stem");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Fungal edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 250);
        return completePoliticalSword(sword, "crimson_stem_sword");
    }
    
    public static ItemStack createCryingObsidianSword() {
        ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Crying Obsidian Blade").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "crying_obsidian_sword_dmg"), 6, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "crying_obsidian_sword_spd"), -0.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_PURPLE));
        lore.add(Component.literal("⚔ Damage: 14").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 1800").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Void tears").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "crying_obsidian");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Tears of the void").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 1800);
        return completePoliticalSword(sword, "crying_obsidian_sword");
    }
    
    public static ItemStack createGildedBlackstoneSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Gilded Blackstone Blade").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "gilded_blackstone_sword_dmg"), 6, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "gilded_blackstone_sword_spd"), 0.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("⚔ Damage: 13").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 650").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Golden fortune").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "gilded_blackstone");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Golden edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 650);
        return completePoliticalSword(sword, "gilded_blackstone_sword");
    }
    
    public static ItemStack createGraniteSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Granite Blade").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "granite_sword_dmg"), 3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "granite_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 420").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Hard strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "granite");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Granite edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 420);
        return completePoliticalSword(sword, "granite_sword");
    }
    
    public static ItemStack createDioriteSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Diorite Blade").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "diorite_sword_dmg"), 3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "diorite_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 400").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Crystal sharpness").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "diorite");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Crystal edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 400);
        return completePoliticalSword(sword, "diorite_sword");
    }
    
    public static ItemStack createAndesiteSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Andesite Blade").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "andesite_sword_dmg"), 3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "andesite_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 410").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Volcanic strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "andesite");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Volcanic edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 410);
        return completePoliticalSword(sword, "andesite_sword");
    }
    
    public static ItemStack createPolishedGraniteSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Polished Granite Blade").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "polished_granite_sword_dmg"), 3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "polished_granite_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 430").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Smooth strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "polished_granite");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Sleek edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 430);
        return completePoliticalSword(sword, "polished_granite_sword");
    }
    
    public static ItemStack createPolishedDioriteSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Polished Diorite Blade").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "polished_diorite_sword_dmg"), 3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "polished_diorite_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 410").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Clean cuts").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "polished_diorite");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Clean edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 410);
        return completePoliticalSword(sword, "polished_diorite_sword");
    }
    
    public static ItemStack createPolishedAndesiteSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Polished Andesite Blade").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "polished_andesite_sword_dmg"), 3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "polished_andesite_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 420").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Sleek strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "polished_andesite");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Modern edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 420);
        return completePoliticalSword(sword, "polished_andesite_sword");
    }
    
    public static ItemStack createPackedIceSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Packed Ice Blade").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "packed_ice_sword_dmg"), 6, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "packed_ice_sword_spd"), 0.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal("⚔ Damage: 13").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 520").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Frozen strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "packed_ice");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Frozen edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 520);
        return completePoliticalSword(sword, "packed_ice_sword");
    }
    
    public static ItemStack createBlueIceSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Blue Ice Blade").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "blue_ice_sword_dmg"), 6, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "blue_ice_sword_spd"), 0.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.BLUE));
        lore.add(Component.literal("⚔ Damage: 13").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 680").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Glacial power").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "blue_ice");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Glacial edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 680);
        return completePoliticalSword(sword, "blue_ice_sword");
    }
    
    public static ItemStack createNetherGoldOreSword() {
        ItemStack sword = new ItemStack(Items.GOLDEN_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Nether Gold Ore Blade").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "nether_gold_ore_sword_dmg"), 7, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "nether_gold_ore_sword_spd"), 0.3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("⚔ Damage: 11").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 280").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Golden nether strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "nether_gold_ore");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Golden nether edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 280);
        return completePoliticalSword(sword, "nether_gold_ore_sword");
    }
    
    public static ItemStack createDarkOakLogSword() {
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Dark Oak Log Blade").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "dark_oak_log_sword_dmg"), 4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "dark_oak_log_sword_spd"), 0.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_RED));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 320").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Shadow strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "dark_oak_log");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Shadow edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 320);
        return completePoliticalSword(sword, "dark_oak_log_sword");
    }
    
    public static ItemStack createJungleLogSword() {
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Jungle Log Blade").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "jungle_log_sword_dmg"), 4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "jungle_log_sword_spd"), 0.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 300").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Tropical strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "jungle_log");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Tropical edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 300);
        return completePoliticalSword(sword, "jungle_log_sword");
    }
    
    public static ItemStack createAcaciaLogSword() {
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Acacia Log Blade").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "acacia_log_sword_dmg"), 4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "acacia_log_sword_spd"), 0.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 290").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Savanna strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "acacia_log");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Savanna edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 290);
        return completePoliticalSword(sword, "acacia_log_sword");
    }
    
    public static ItemStack createMangroveLogSword() {
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Mangrove Log Blade").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "mangrove_log_sword_dmg"), 4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "mangrove_log_sword_spd"), 0.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_RED));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 340").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Swamp strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "mangrove_log");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Swamp edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 340);
        return completePoliticalSword(sword, "mangrove_log_sword");
    }
    
    public static ItemStack createCherryLogSword() {
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Cherry Log Blade").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "cherry_log_sword_dmg"), 4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "cherry_log_sword_spd"), 0.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 310").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Blossom strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "cherry_log");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Blossom edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 310);
        return completePoliticalSword(sword, "cherry_log_sword");
    }
    
    public static ItemStack createBambooBlockSword() {
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Bamboo Block Blade").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "bamboo_block_sword_dmg"), 4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "bamboo_block_sword_spd"), 0.3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 180").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Quick strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "bamboo_block");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Light edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 180);
        return completePoliticalSword(sword, "bamboo_block_sword");
    }
    
    public static ItemStack createTuffSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Tuff Blade").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "tuff_sword_dmg"), 3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "tuff_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 400").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Volcanic strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "tuff");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Volcanic edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 400);
        return completePoliticalSword(sword, "tuff_sword");
    }
    
    public static ItemStack createPolishedTuffSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Polished Tuff Blade").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "polished_tuff_sword_dmg"), 3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "polished_tuff_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 400").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Smooth cuts").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "polished_tuff");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Sleek edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 400);
        return completePoliticalSword(sword, "polished_tuff_sword");
    }
    
    public static ItemStack createNetherWartBlockSword() {
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Nether Wart Block Blade").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "nether_wart_block_sword_dmg"), 4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "nether_wart_block_sword_spd"), 0.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_RED));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 260").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Fungal strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "nether_wart_block");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Fungal edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 260);
        return completePoliticalSword(sword, "nether_wart_block_sword");
    }
    
    public static ItemStack createCobbledDeepslateSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Cobbled Deepslate Blade").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "cobbled_deepslate_sword_dmg"), 3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "cobbled_deepslate_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 460").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Deep strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "cobbled_deepslate");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Deep edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 460);
        return completePoliticalSword(sword, "cobbled_deepslate_sword");
    }
    
    public static ItemStack createPolishedDeepslateSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Polished Deepslate Blade").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "polished_deepslate_sword_dmg"), 3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "polished_deepslate_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 480").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Sleek deep cuts").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "polished_deepslate");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Polished edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 480);
        return completePoliticalSword(sword, "polished_deepslate_sword");
    }
    
    public static ItemStack createDeepslateTileSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Deepslate Tile Blade").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "deepslate_tile_sword_dmg"), 3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "deepslate_tile_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 470").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Tile strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "deepslate_tile");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Tile edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 470);
        return completePoliticalSword(sword, "deepslate_tile_sword");
    }
    
    public static ItemStack createDeepslateBrickSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Deepslate Brick Blade").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "deepslate_brick_sword_dmg"), 3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "deepslate_brick_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 490").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Brick strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "deepslate_brick");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Brick edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 490);
        return completePoliticalSword(sword, "deepslate_brick_sword");
    }
    
    public static ItemStack createMudBrickSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Mud Brick Blade").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "mud_brick_sword_dmg"), 3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "mud_brick_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_RED));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 350").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Muddy strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "mud_brick");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Muddy edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 350);
        return completePoliticalSword(sword, "mud_brick_sword");
    }
    
    public static ItemStack createReinforcedDeepslateSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Reinforced Deepslate Blade").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "reinforced_deepslate_sword_dmg"), 6, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "reinforced_deepslate_sword_spd"), 0.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("⚔ Damage: 13").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 620").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Reinforced strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "reinforced_deepslate");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Unbreakable edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 620);
        return completePoliticalSword(sword, "reinforced_deepslate_sword");
    }
    
    public static ItemStack createChiseledDeepslateSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Chiseled Deepslate Blade").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "chiseled_deepslate_sword_dmg"), 3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "chiseled_deepslate_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 475").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Chiseled strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "chiseled_deepslate");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Chiseled edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 475);
        return completePoliticalSword(sword, "chiseled_deepslate_sword");
    }
    
    public static ItemStack createChiseledNetherBrickSword() {
        ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Chiseled Nether Brick Blade").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "chiseled_nether_brick_sword_dmg"), 5, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "chiseled_nether_brick_sword_spd"), 0.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_RED));
        lore.add(Component.literal("⚔ Damage: 13").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 540").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Ancient strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "chiseled_nether_brick");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Ancient edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 540);
        return completePoliticalSword(sword, "chiseled_nether_brick_sword");
    }
    
    public static ItemStack createCrackedNetherBrickSword() {
        ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Cracked Nether Brick Blade").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "cracked_nether_brick_sword_dmg"), 5, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "cracked_nether_brick_sword_spd"), 0.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_RED));
        lore.add(Component.literal("⚔ Damage: 13").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 530").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Cracked strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "cracked_nether_brick");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Cracked edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 530);
        return completePoliticalSword(sword, "cracked_nether_brick_sword");
    }
    
    public static ItemStack createChiseledStoneBrickSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Chiseled Stone Brick Blade").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "chiseled_stone_brick_sword_dmg"), 3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "chiseled_stone_brick_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 340").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Carved strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "chiseled_stone_brick");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Carved edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 340);
        return completePoliticalSword(sword, "chiseled_stone_brick_sword");
    }
    
    public static ItemStack createCrackedStoneBrickSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Cracked Stone Brick Blade").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "cracked_stone_brick_sword_dmg"), 3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "cracked_stone_brick_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 340").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Ruin strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "cracked_stone_brick");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Ruin edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 340);
        return completePoliticalSword(sword, "cracked_stone_brick_sword");
    }
    
    public static ItemStack createEndStoneBrickSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ End Stone Brick Blade").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "end_stone_brick_sword_dmg"), 3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "end_stone_brick_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 360").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Void strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "end_stone_brick");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Void edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 360);
        return completePoliticalSword(sword, "end_stone_brick_sword");
    }
    
    public static ItemStack createRedSandstoneSword() {
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Red Sandstone Blade").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "red_sandstone_sword_dmg"), 4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "red_sandstone_sword_spd"), 0.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 280").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Desert strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "red_sandstone");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Desert edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 280);
        return completePoliticalSword(sword, "red_sandstone_sword");
    }
    
    public static ItemStack createRawIronBlockSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Raw Iron Block Blade").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "raw_iron_block_sword_dmg"), 6, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "raw_iron_block_sword_spd"), 0.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("⚔ Damage: 13").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 500").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Heavy strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "raw_iron_block");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Heavy edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 500);
        return completePoliticalSword(sword, "raw_iron_block_sword");
    }
    
    public static ItemStack createRawGoldBlockSword() {
        ItemStack sword = new ItemStack(Items.GOLDEN_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Raw Gold Block Blade").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "raw_gold_block_sword_dmg"), 7, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "raw_gold_block_sword_spd"), 0.3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("⚔ Damage: 11").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 350").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Fortune strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "raw_gold_block");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Golden edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 350);
        return completePoliticalSword(sword, "raw_gold_block_sword");
    }
    
    public static ItemStack createPrismarineBricksSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Prismarine Bricks Blade").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "prismarine_bricks_sword_dmg"), 6, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "prismarine_bricks_sword_spd"), 0.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_AQUA));
        lore.add(Component.literal("⚔ Damage: 13").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 320").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Ocean strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "prismarine_bricks");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Ocean edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 320);
        return completePoliticalSword(sword, "prismarine_bricks_sword");
    }
    
    public static ItemStack createDarkPrismarineSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Dark Prismarine Blade").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "dark_prismarine_sword_dmg"), 6, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "dark_prismarine_sword_spd"), 0.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GREEN));
        lore.add(Component.literal("⚔ Damage: 13").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 360").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Abyss strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "dark_prismarine");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Abyss edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 360);
        return completePoliticalSword(sword, "dark_prismarine_sword");
    }
    
    public static ItemStack createSeaLanternSword() {
        ItemStack sword = new ItemStack(Items.GOLDEN_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Sea Lantern Blade").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "sea_lantern_sword_dmg"), 7, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "sea_lantern_sword_spd"), 0.3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal("⚔ Damage: 11").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 220").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Glowing strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "sea_lantern");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Glow edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 220);
        return completePoliticalSword(sword, "sea_lantern_sword");
    }
    
    public static ItemStack createLodestoneSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Lodestone Blade").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "lodestone_sword_dmg"), 6, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "lodestone_sword_spd"), 0.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Damage: 13").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 450").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ True north strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "lodestone");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Magnetic edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 450);
        return completePoliticalSword(sword, "lodestone_sword");
    }
    
    public static ItemStack createBlackstoneBricksSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Blackstone Bricks Blade").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "blackstone_bricks_sword_dmg"), 3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "blackstone_bricks_sword_spd"), 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 400").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Fortress strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "blackstone_bricks");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Fortress edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 400);
        return completePoliticalSword(sword, "blackstone_bricks_sword");
    }
    
    public static ItemStack createWarpedWartBlockSword() {
        ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Warped Wart Block Blade").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD));
        
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "warped_wart_block_sword_dmg"), 5, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", "warped_wart_block_sword_spd"), 0.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GREEN));
        lore.add(Component.literal("⚔ Damage: 13").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 560").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Warped strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "warped_wart_block");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Warped edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 560);
        return completePoliticalSword(sword, "warped_wart_block_sword");
    }
    
    public static ItemStack createPolishedBlackstoneSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Polished Blackstone Blade").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Damage: 5").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 380").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Sleek strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "polished_blackstone");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Polished edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 380);
        return completePoliticalSword(sword, "polished_blackstone_sword");
    }
    
    public static ItemStack createSmoothBasaltSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Smooth Basalt Blade").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("⚔ Damage: 7").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 420").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Volcanic strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "smooth_basalt");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Volcanic edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 420);
        return completePoliticalSword(sword, "smooth_basalt_sword");
    }
    
    public static ItemStack createAmethystClusterSword() {
        ItemStack sword = new ItemStack(Items.GOLDEN_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Amethyst Cluster Blade").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal("⚔ Damage: 5").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 280").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Crystal strikes").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "amethyst_cluster");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Crystal edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 280);
        return completePoliticalSword(sword, "amethyst_cluster_sword");
    }
    
    // ============================================================
    // STRIPPED LOG WEAPONS
    // ============================================================

    public static ItemStack createStrippedSpruceLogSword() {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Stripped Spruce Blade").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_AQUA));
        lore.add(Component.literal("⚔ Damage: 6").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 250").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Fast").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "stripped_spruce_log");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Stripped spruce edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 250);
        return completePoliticalSword(sword, "stripped_spruce_log_sword");
    }

    public static ItemStack createStrippedBirchLogSword() {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Stripped Birch Blade").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("⚔ Damage: 5").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 200").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Fast").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "stripped_birch_log");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Stripped birch edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 200);
        return completePoliticalSword(sword, "stripped_birch_log_sword");
    }

    public static ItemStack createStrippedDarkOakLogSword() {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Stripped Dark Oak Blade").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("⚔ Damage: 7").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 300").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Normal").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "stripped_dark_oak_log");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Stripped dark oak edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 300);
        return completePoliticalSword(sword, "stripped_dark_oak_log_sword");
    }

    public static ItemStack createStrippedJungleLogSword() {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Stripped Jungle Blade").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("⚔ Damage: 6").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 250").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Fast").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "stripped_jungle_log");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Stripped jungle edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 250);
        return completePoliticalSword(sword, "stripped_jungle_log_sword");
    }

    public static ItemStack createStrippedAcaciaLogSword() {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Stripped Acacia Blade").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("⚔ Damage: 6").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 260").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Fast").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "stripped_acacia_log");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Stripped acacia edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 260);
        return completePoliticalSword(sword, "stripped_acacia_log_sword");
    }

    public static ItemStack createStrippedMangroveLogSword() {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Stripped Mangrove Blade").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_RED));
        lore.add(Component.literal("⚔ Damage: 7").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 280").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Normal").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "stripped_mangrove_log");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Stripped mangrove edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 280);
        return completePoliticalSword(sword, "stripped_mangrove_log_sword");
    }

    public static ItemStack createStrippedCherryLogSword() {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Stripped Cherry Blade").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal("⚔ Damage: 6").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 240").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Fast").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "stripped_cherry_log");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Stripped cherry edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 240);
        return completePoliticalSword(sword, "stripped_cherry_log_sword");
    }

    public static ItemStack createStrippedBambooBlockSword() {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Bamboo Blade").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Damage: 5").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 180").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Very Fast").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "stripped_bamboo_block");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Bamboo edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 180);
        return completePoliticalSword(sword, "stripped_bamboo_block_sword");
    }

    public static ItemStack createStrippedCrimsonStemSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Crimson Stem Blade").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 400").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Normal").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "stripped_crimson_stem");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Crimson edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 400);
        return completePoliticalSword(sword, "stripped_crimson_stem_sword");
    }

    public static ItemStack createStrippedWarpedStemSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Warped Stem Blade").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 420").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Normal").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "stripped_warped_stem");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Warped edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 420);
        return completePoliticalSword(sword, "stripped_warped_stem_sword");
    }

    // ============================================================
    // PLANK WEAPONS
    // ============================================================

    public static ItemStack createSprucePlanksSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Spruce Planks Blade").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_AQUA));
        lore.add(Component.literal("⚔ Damage: 4").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 150").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Fast").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "spruce_planks");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Spruce edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 150);
        return completePoliticalSword(sword, "spruce_planks_sword");
    }

    public static ItemStack createBirchPlanksSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Birch Planks Blade").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("⚔ Damage: 3").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 120").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Fast").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "birch_planks");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Birch edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 120);
        return completePoliticalSword(sword, "birch_planks_sword");
    }

    public static ItemStack createJunglePlanksSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Jungle Planks Blade").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("⚔ Damage: 4").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 150").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Fast").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "jungle_planks");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Jungle edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 150);
        return completePoliticalSword(sword, "jungle_planks_sword");
    }

    public static ItemStack createAcaciaPlanksSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Acacia Planks Blade").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("⚔ Damage: 4").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 160").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Fast").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "acacia_planks");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Acacia edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 160);
        return completePoliticalSword(sword, "acacia_planks_sword");
    }

    public static ItemStack createDarkOakPlanksSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Dark Oak Planks Blade").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("⚔ Damage: 5").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 180").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Normal").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "dark_oak_planks");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Dark oak edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 180);
        return completePoliticalSword(sword, "dark_oak_planks_sword");
    }

    public static ItemStack createMangrovePlanksSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Mangrove Planks Blade").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_RED));
        lore.add(Component.literal("⚔ Damage: 5").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 170").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Normal").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "mangrove_planks");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Mangrove edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 170);
        return completePoliticalSword(sword, "mangrove_planks_sword");
    }

    public static ItemStack createCherryPlanksSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Cherry Planks Blade").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal("⚔ Damage: 4").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 140").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Fast").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "cherry_planks");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Cherry edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 140);
        return completePoliticalSword(sword, "cherry_planks_sword");
    }

    public static ItemStack createBambooPlanksSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Bamboo Planks Blade").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Damage: 3").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 100").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Very Fast").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "bamboo_planks");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Bamboo edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 100);
        return completePoliticalSword(sword, "bamboo_planks_sword");
    }

    public static ItemStack createCrimsonPlanksSword() {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Crimson Planks Blade").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Damage: 6").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 250").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Fast").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "crimson_planks");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Crimson edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 250);
        return completePoliticalSword(sword, "crimson_planks_sword");
    }

    public static ItemStack createWarpedPlanksSword() {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Warped Planks Blade").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal("⚔ Damage: 6").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 260").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Fast").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "warped_planks");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Warped edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 260);
        return completePoliticalSword(sword, "warped_planks_sword");
    }

    public static ItemStack createOakPlanksSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Oak Planks Blade").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Damage: 3").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 120").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Fast").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "oak_planks");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Oak edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 120);
        return completePoliticalSword(sword, "oak_planks_sword");
    }

    // ============================================================
    // STONE/MUD/LOG WEAPONS
    // ============================================================

    public static ItemStack createOakLogSword() {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Oak Log Blade").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Damage: 5").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 200").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Fast").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "oak_log");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Oak edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 200);
        return completePoliticalSword(sword, "oak_log_sword");
    }

    public static ItemStack createSpruceLogSword() {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Spruce Log Blade").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_AQUA));
        lore.add(Component.literal("⚔ Damage: 5").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 210").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Fast").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "spruce_log");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Spruce edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 210);
        return completePoliticalSword(sword, "spruce_log_sword");
    }

    public static ItemStack createBirchLogSword() {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Birch Log Blade").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("⚔ Damage: 4").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 180").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Fast").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "birch_log");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Birch edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 180);
        return completePoliticalSword(sword, "birch_log_sword");
    }

    public static ItemStack createStoneBricksSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Stone Bricks Blade").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 500").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Normal").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "stone_bricks");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Stone edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 500);
        return completePoliticalSword(sword, "stone_bricks_sword");
    }

    public static ItemStack createCobblestoneSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Cobblestone Blade").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Damage: 4").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 200").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Normal").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "cobblestone");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Cobble edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 200);
        return completePoliticalSword(sword, "cobblestone_sword");
    }

    public static ItemStack createMossyCobblestoneSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Mossy Cobblestone Blade").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GREEN));
        lore.add(Component.literal("⚔ Damage: 4").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 220").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Normal").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "mossy_cobblestone");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Mossy edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 220);
        return completePoliticalSword(sword, "mossy_cobblestone_sword");
    }

    public static ItemStack createMudBricksSword() {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Mud Bricks Blade").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Damage: 5").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 250").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Fast").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "mud_bricks");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Mud edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 250);
        return completePoliticalSword(sword, "mud_bricks_sword");
    }

    public static ItemStack createMangroveRootsSword() {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Mangrove Roots Blade").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_RED));
        lore.add(Component.literal("⚔ Damage: 5").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 220").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Fast").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "mangrove_roots");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Root edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 220);
        return completePoliticalSword(sword, "mangrove_roots_sword");
    }

    public static ItemStack createMuddyMangroveRootsSword() {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Muddy Mangrove Roots Blade").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_RED));
        lore.add(Component.literal("⚔ Damage: 6").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 260").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Normal").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "muddy_mangrove_roots");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Muddy root edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 260);
        return completePoliticalSword(sword, "muddy_mangrove_roots_sword");
    }

    // ============================================================
    // COPPER/NETHERITE/FROGLIGHT/BASALT WEAPONS
    // ============================================================

    public static ItemStack createNetheriteBlockSword() {
        ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Netherite Block Blade").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_PURPLE));
        lore.add(Component.literal("⚔ Damage: 10").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 2000").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Normal").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "netherite_block");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Netherite edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 2000);
        return completePoliticalSword(sword, "netherite_block_sword");
    }

    public static ItemStack createChiseledCopperSword() {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Chiseled Copper Blade").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("⚔ Damage: 5").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 220").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Fast").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "chiseled_copper");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Copper edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 220);
        return completePoliticalSword(sword, "chiseled_copper_sword");
    }

    public static ItemStack createCutCopperSword() {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Cut Copper Blade").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("⚔ Damage: 5").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 200").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Fast").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "cut_copper");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Copper edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 200);
        return completePoliticalSword(sword, "cut_copper_sword");
    }

    public static ItemStack createExposedCopperSword() {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Exposed Copper Blade").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("⚔ Damage: 5").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 190").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Fast").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "exposed_copper");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Copper edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 190);
        return completePoliticalSword(sword, "exposed_copper_sword");
    }

    public static ItemStack createWeatheredCopperSword() {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Weathered Copper Blade").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("⚔ Damage: 5").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 180").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Fast").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "weathered_copper");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Copper edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 180);
        return completePoliticalSword(sword, "weathered_copper_sword");
    }

    public static ItemStack createOxidisedCopperSword() {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Oxidised Copper Blade").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal("⚔ Damage: 5").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 170").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Fast").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "oxidised_copper");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Oxidised edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 170);
        return completePoliticalSword(sword, "oxidised_copper_sword");
    }

    public static ItemStack createWaxedCutCopperSword() {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Waxed Cut Copper Blade").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("⚔ Damage: 5").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 210").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Fast").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "waxed_cut_copper");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Waxed copper edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 210);
        return completePoliticalSword(sword, "waxed_cut_copper_sword");
    }

    public static ItemStack createPolishedBasaltSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Polished Basalt Blade").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 450").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Normal").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "polished_basalt");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Basalt edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 450);
        return completePoliticalSword(sword, "polished_basalt_sword");
    }

    public static ItemStack createVerdantFroglightSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Verdant Froglight Blade").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 480").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Normal").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "verdant_froglight");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Froglight edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 480);
        return completePoliticalSword(sword, "verdant_froglight_sword");
    }

    public static ItemStack createPearlescentFroglightSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Pearlescent Froglight Blade").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 500").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Normal").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "pearlescent_froglight");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Froglight edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 500);
        return completePoliticalSword(sword, "pearlescent_froglight_sword");
    }

    public static ItemStack createOchreFroglightSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Ochre Froglight Blade").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Damage: 8").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 460").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Normal").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "ochre_froglight");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Froglight edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 460);
        return completePoliticalSword(sword, "ochre_froglight_sword");
    }

    // ============================================================
    // MATERIAL ITEM WEAPONS
    // ============================================================

    public static ItemStack createIronNuggetSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Iron Nugget Blade").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("⚔ Damage: 3").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 100").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Fast").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "iron_nugget");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Nugget edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 100);
        return completePoliticalSword(sword, "iron_nugget_sword");
    }

    public static ItemStack createGoldNuggetSword() {
        ItemStack sword = new ItemStack(Items.GOLDEN_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Gold Nugget Blade").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("⚔ Damage: 2").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 50").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Very Fast").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "gold_nugget");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Gold edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 50);
        return completePoliticalSword(sword, "gold_nugget_sword");
    }

    public static ItemStack createCopperIngotSword() {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Copper Ingot Blade").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("⚔ Damage: 4").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 150").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Fast").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "copper_ingot");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Copper edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 150);
        return completePoliticalSword(sword, "copper_ingot_sword");
    }

    public static ItemStack createEmeraldSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Emerald Blade").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("⚔ Damage: 7").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 400").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Fast").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "emerald");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Emerald edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 400);
        return completePoliticalSword(sword, "emerald_sword");
    }

    public static ItemStack createLapisLazuliSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Lapis Lazuli Blade").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.BLUE));
        lore.add(Component.literal("⚔ Damage: 6").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 300").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Fast").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "lapis_lazuli");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Lapis edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 300);
        return completePoliticalSword(sword, "lapis_lazuli_sword");
    }

    public static ItemStack createAmethystShardSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Amethyst Shard Blade").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal("⚔ Damage: 7").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 380").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Fast").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "amethyst_shard");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Amethyst edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 380);
        return completePoliticalSword(sword, "amethyst_shard_sword");
    }

    public static ItemStack createFlintSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Flint Blade").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Damage: 3").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 120").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Fast").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "flint");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Flint edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 120);
        return completePoliticalSword(sword, "flint_sword");
    }

    public static ItemStack createBoneMealSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Bone Meal Blade").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("⚔ Damage: 2").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 80").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Very Fast").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "bone_meal");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Bone edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 80);
        return completePoliticalSword(sword, "bone_meal_sword");
    }

    public static ItemStack createCharcoalSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Charcoal Blade").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("⚔ Damage: 3").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 130").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Fast").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "charcoal");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Charcoal edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 130);
        return completePoliticalSword(sword, "charcoal_sword");
    }

    public static ItemStack createSnowBlockSword() {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Snow Block Blade").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("⚔ Damage: 4").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 180").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Fast").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, "snow_block");
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8Snow edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 180);
        return completePoliticalSword(sword, "snow_block_sword");
    }

    // ============================================================
    // GENERIC BLOCK SWORD HELPER
    // ============================================================

    @SuppressWarnings("unused")
    private static ItemStack createBlockSword(String materialName, String idPrefix,
            net.minecraft.world.item.Item baseItem, ChatFormatting nameColor, int durability,
            @SuppressWarnings("unused") int displayDamage, String speedDesc, String setBonus, String flavorText, double atkDmgMod, double atkSpdMod) {
        ItemStack sword = new ItemStack(baseItem);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ " + materialName + " Blade").withStyle(nameColor, ChatFormatting.BOLD));
        double dmgMod = atkDmgMod + GENERIC_BLOCK_SWORD_DAMAGE_BUFF;
        int damageLine = (int) Math.round(1.0 + dmgMod);
        ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("political", idPrefix + "_sword_damage"), dmgMod, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("political", idPrefix + "_sword_speed"), atkSpdMod, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrs.build());
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(nameColor));
        lore.add(Component.literal("⚔ Damage: " + damageLine).withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: " + durability).withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: " + speedDesc).withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        BlockGearLore.appendSwordArmorSynergy(lore, idPrefix);
        BlockGearLore.appendSwordCombatPipelineNote(lore);
        lore.add(Component.literal("§8" + flavorText).withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, durability);
        return completePoliticalSword(sword, idPrefix + "_sword");
    }

    // ============================================================
    // 25 NEW BLOCK SWORD SETS
    // ============================================================

    // 1. Raw Prismarine - medium, ocean themed
    public static ItemStack createRawPrismarineSword() { return createBlockSword("Raw Prismarine", "raw_prismarine", Items.IRON_SWORD, ChatFormatting.DARK_AQUA, 250, 10, "Normal", "Conduit Power I", "Ocean-forged edge", 2, 0); }

    // 2. Clay - light, weak
    public static ItemStack createClaySword() { return createBlockSword("Clay", "clay", Items.STONE_SWORD, ChatFormatting.GOLD, 130, 7, "Fast", "Earthen Shield", "Soft earthen edge", 1, 0.3); }

    // 3. Glazed Terracotta - medium, decorative
    public static ItemStack createGlazedTerracottaSword() { return createBlockSword("Glazed Terracotta", "glazed_terracotta", Items.IRON_SWORD, ChatFormatting.GOLD, 280, 10, "Normal", "Pattern Shift", "Vibrant glazed edge", 2, 0); }

    // 4. Concrete - heavy, solid
    public static ItemStack createConcreteSword() { return createBlockSword("Concrete", "concrete", Items.DIAMOND_SWORD, ChatFormatting.GRAY, 500, 13, "Slow", "Reinforced Shell", "Solid concrete edge", 5, -0.3); }

    // 5. Concrete Powder - light, fast
    public static ItemStack createConcretePowderSword() { return createBlockSword("Concrete Powder", "concrete_powder", Items.STONE_SWORD, ChatFormatting.WHITE, 150, 8, "Fast", "Dust Cloud", "Powdery light edge", 2, 0.4); }

    // 6. Bookshelf - medium, enchant themed
    public static ItemStack createBookshelfSword() { return createBlockSword("Bookshelf", "bookshelf", Items.IRON_SWORD, ChatFormatting.GOLD, 300, 11, "Normal", "Knowledge Aura +30% XP", "Bound in ancient knowledge", 3, 0); }

    // 7. Barrel - medium, storage themed
    public static ItemStack createBarrelSword() { return createBlockSword("Barrel", "barrel", Items.IRON_SWORD, ChatFormatting.GOLD, 280, 10, "Normal", "Extra Inventory Row", "Barrel-bound edge", 2, 0); }

    // 8. Smithing Table - medium, upgrade themed
    public static ItemStack createSmithingTableSword() { return createBlockSword("Smithing Table", "smithing_table", Items.IRON_SWORD, ChatFormatting.DARK_GRAY, 350, 11, "Normal", "Upgrade Mastery +15% craft", "Forged at the smithing table", 3, 0); }

    // 9. Anvil - heavy, slow, powerful
    public static ItemStack createAnvilSword() { return createBlockSword("Anvil", "anvil", Items.NETHERITE_SWORD, ChatFormatting.GRAY, 800, 14, "Very Slow", "Iron Fortress + KB immune", "Heavy iron edge", 6, -0.5); }

    // 10. Grindstone - medium, sharpening
    public static ItemStack createGrindstoneSword() { return createBlockSword("Grindstone", "grindstone", Items.IRON_SWORD, ChatFormatting.DARK_GRAY, 320, 11, "Normal", "Sharpen Aura +10% melee dmg", "Honed by the grindstone", 3, 0.1); }

    // 11. Lectern - medium, knowledge
    public static ItemStack createLecternSword() { return createBlockSword("Lectern", "lectern", Items.IRON_SWORD, ChatFormatting.GOLD, 260, 9, "Normal", "Wisdom Pulse +20% XP", "Scholarly edge", 1, 0); }

    // 12. Composter - medium, nature
    public static ItemStack createComposterSword() { return createBlockSword("Composter", "composter", Items.IRON_SWORD, ChatFormatting.GREEN, 240, 9, "Normal", "Nature Regen + crop growth", "Rich organic edge", 1, 0); }

    // 13. Stonecutter - medium, precision
    public static ItemStack createStonecutterSword() { return createBlockSword("Stonecutter", "stonecutter", Items.IRON_SWORD, ChatFormatting.WHITE, 300, 11, "Fast", "Precision Strike +crit chance", "Cut with precision", 3, 0.3); }

    // 14. Dried Kelp - light, ocean
    public static ItemStack createDriedKelpSword() { return createBlockSword("Dried Kelp", "dried_kelp", Items.STONE_SWORD, ChatFormatting.DARK_GREEN, 120, 7, "Fast", "Ocean Nourish + saturation", "Dried ocean edge", 1, 0.3); }

    // 15. Coral Block - light, vibrant
    public static ItemStack createCoralBlockSword() { return createBlockSword("Coral Block", "coral_block", Items.STONE_SWORD, ChatFormatting.LIGHT_PURPLE, 150, 8, "Fast", "Reef Barrier + water speed", "Vibrant reef edge", 2, 0.4); }

    // 16. Lantern - medium, light
    public static ItemStack createLanternSword() { return createBlockSword("Lantern", "lantern", Items.IRON_SWORD, ChatFormatting.GOLD, 260, 10, "Normal", "Light Aura + Night Vision", "Illuminated edge", 2, 0); }

    // 17. Chain - heavy, chainmail
    public static ItemStack createChainSword() { return createBlockSword("Chain", "chain", Items.DIAMOND_SWORD, ChatFormatting.DARK_GRAY, 600, 13, "Normal", "Linked Defense + thorns", "Interlocking chain edge", 5, -0.1); }

    // 18. Lightning Rod - special, electrical
    public static ItemStack createLightningRodSword() { return createBlockSword("Lightning Rod", "lightning_rod", Items.IRON_SWORD, ChatFormatting.GOLD, 350, 12, "Normal", "Storm Conductor + lightning dmg", "Crackling with energy", 4, 0); }

    // 19. Tinted Glass - medium, stealth
    public static ItemStack createTintedGlassSword() { return createBlockSword("Tinted Glass", "tinted_glass", Items.IRON_SWORD, ChatFormatting.DARK_GRAY, 200, 9, "Fast", "Shadow Cloak + Invisibility", "Dark transparent edge", 1, 0.3); }

    // 20. Candle - medium, mystical
    public static ItemStack createCandleSword() { return createBlockSword("Candle", "candle", Items.IRON_SWORD, ChatFormatting.WHITE, 220, 9, "Normal", "Wax Ward + fire resistance", "Wax-sealed edge", 1, 0); }

    // 21. Sculk Sensor - special, stealth
    public static ItemStack createSculkSensorSword() { return createBlockSword("Sculk Sensor", "sculk_sensor", Items.DIAMOND_SWORD, ChatFormatting.DARK_AQUA, 380, 12, "Normal", "Vibration Sense + mob radar", "Attuned to vibrations", 4, 0); }

    // 22. Sculk Catalyst - special, death/growth
    public static ItemStack createSculkCatalystSword() { return createBlockSword("Sculk Catalyst", "sculk_catalyst", Items.DIAMOND_SWORD, ChatFormatting.DARK_AQUA, 400, 12, "Normal", "Soul Harvest + XP on kill", "Feeds on fallen souls", 4, -0.1); }

    // 23. Sculk Shrieker - special, warden
    public static ItemStack createSculkShriekerSword() { return createBlockSword("Sculk Shrieker", "sculk_shrieker", Items.DIAMOND_SWORD, ChatFormatting.DARK_BLUE, 420, 13, "Slow", "Warden's Cry + fear aura", "Echoes of the deep dark", 5, -0.3); }

    // 24. Brewing Stand - special, potion
    public static ItemStack createBrewingStandSword() { return createBlockSword("Brewing Stand", "brewing_stand", Items.IRON_SWORD, ChatFormatting.DARK_PURPLE, 340, 11, "Normal", "Potion Master + random buffs", "Bubbling with potency", 3, 0); }

    // 25. Furnace - heavy, smelting
    public static ItemStack createFurnaceSword() { return createBlockSword("Furnace", "furnace", Items.DIAMOND_SWORD, ChatFormatting.GRAY, 550, 13, "Slow", "Smelt Ward + fire immune", "Forged in the furnace", 5, -0.2); }

    /** Keeps lore attack line in sync with MAINHAND ADD_VALUE attack damage (+1 fist base). */
    private static void syncDamageLoreFromMeleeAttributes(ItemStack sword) {
        var modComp = sword.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (modComp == null) return;
        double sum = 0;
        for (var entry : modComp.modifiers()) {
            if (!entry.attribute().is(Attributes.ATTACK_DAMAGE)) continue;
            if (!entry.slot().equals(EquipmentSlotGroup.MAINHAND)) continue;
            if (entry.modifier().operation() != AttributeModifier.Operation.ADD_VALUE) continue;
            sum += entry.modifier().amount();
        }
        int shown = (int) Math.round(1.0 + sum);
        var lore = sword.get(DataComponents.LORE);
        if (lore == null) return;
        List<Component> lines = new ArrayList<>(lore.lines());
        for (int i = 0; i < lines.size(); i++) {
            String s = lines.get(i).getString();
            if (s.contains("Damage:")) {
                lines.set(i, Component.literal("⚔ Damage: " + shown).withStyle(ChatFormatting.RED));
                sword.set(DataComponents.LORE, new ItemLore(lines));
                return;
            }
        }
    }

    private static ItemStack completePoliticalSword(ItemStack sword, String customId) {
        SlayerItems.setCustomItemId(sword, customId);
        syncDamageLoreFromMeleeAttributes(sword);
        return sword;
    }
}
