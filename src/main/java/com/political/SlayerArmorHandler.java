package com.political;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class SlayerArmorHandler {

    private static final Identifier SLAYER_ARMOR_ID = Identifier.of("political", "slayer_armor");
    private static final Identifier SLAYER_TOUGHNESS_ID = Identifier.of("political", "slayer_toughness");
    // Add this method to calculate boss damage reduction
    public static float getBossDamageReduction(ServerPlayerEntity player, SlayerManager.SlayerType bossType) {
        float reduction = 0.0f;

        int matchingPieces = 0;

        ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);
        ItemStack chestplate = player.getEquippedStack(EquipmentSlot.CHEST);
        ItemStack leggings = player.getEquippedStack(EquipmentSlot.LEGS);
        ItemStack boots = player.getEquippedStack(EquipmentSlot.FEET);

        List<ItemStack> armor = List.of(helmet, chestplate, leggings, boots);

        for (ItemStack stack : armor) {
            Text customName = stack.get(DataComponentTypes.CUSTOM_NAME);
            if (customName == null) continue;
            String name = customName.getString();

            if (isMatchingSlayerArmor(name, bossType)) {
                matchingPieces++;
            }
        }

        // Per piece: 10% damage reduction vs matching boss
        reduction += matchingPieces * 0.10f;

        // Full set bonus: additional 20%
        if (matchingPieces >= 4) {
            reduction += 0.20f;
        }

        // Slayer level bonus from SlayerManager [1]
        reduction += (float) SlayerManager.getLevelDamageReduction(player, bossType);

        // Cap at 60% reduction
        return Math.min(0.60f, reduction);
    }

    private static boolean isMatchingSlayerArmor(String name, SlayerManager.SlayerType type) {
        return switch (type) {
            case ZOMBIE -> name.contains("Berserker") || name.contains("Undying");
            case SPIDER -> name.contains("Venomous") || name.contains("Spider");
            case SKELETON -> name.contains("Bone") || name.contains("Desperado");
            case SLIME -> name.contains("Slime") || name.contains("Gelatinous");
            case ENDERMAN -> name.contains("Void") || name.contains("Phantom");
            case WARDEN -> name.contains("Sculk") || name.contains("Warden");
        };
    }
    public static void applyCustomArmorAttributes(ServerPlayerEntity player) {
        double bonusArmor = 0;
        double bonusToughness = 0;
        ItemStack helmet = player.getEquippedStack(net.minecraft.entity.EquipmentSlot.HEAD);
        ItemStack chestplate = player.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST);
        ItemStack leggings = player.getEquippedStack(net.minecraft.entity.EquipmentSlot.LEGS);
        ItemStack boots = player.getEquippedStack(net.minecraft.entity.EquipmentSlot.FEET);

        List<ItemStack> armorPieces = List.of(helmet, chestplate, leggings, boots);

        for (ItemStack stack : armorPieces) {
            Text customName = stack.get(DataComponentTypes.CUSTOM_NAME);
            String name = customName != null ? customName.getString() : "";

            // Zombie Berserker Helmet
            if (name.contains("Berserker") && name.contains("Helmet")) {
                bonusArmor += 3.0;
                bonusToughness += 2.0;
            }
            // Spider Venomous Leggings
            if (name.contains("Venomous") && name.contains("Leggings")) {
                bonusArmor += 6.0;
                bonusToughness += 2.0;
            }
            // Skeleton Bone Chestplate
            if (name.contains("Bone") && name.contains("Chestplate")) {
                bonusArmor += 8.0;
                bonusToughness += 2.0;
            }
            // Slime Boots
            if (name.contains("Slime") && name.contains("Boots")) {
                bonusArmor += 3.0;
                bonusToughness += 2.0;
            }
            // Warden/Sculk Chestplate
            if (name.contains("Warden") || name.contains("Sculk")) {
                bonusArmor += 8.0;
                bonusToughness += 3.0;
            }
            // Enderman/Void pieces
            if (name.contains("Void") || name.contains("Phantom")) {
                bonusArmor += 7.0;
                bonusToughness += 2.5;
            }
        }

        // Apply armor modifier
        var armorAttr = player.getAttributeInstance(EntityAttributes.ARMOR);
        if (armorAttr != null) {
            armorAttr.removeModifier(SLAYER_ARMOR_ID);

            if (bonusArmor > 0) {
                armorAttr.addTemporaryModifier(new EntityAttributeModifier(
                        SLAYER_ARMOR_ID,
                        bonusArmor,
                        EntityAttributeModifier.Operation.ADD_VALUE
                ));
            }
        }

        // Apply toughness modifier
        var toughnessAttr = player.getAttributeInstance(EntityAttributes.ARMOR_TOUGHNESS);
        if (toughnessAttr != null) {
            toughnessAttr.removeModifier(SLAYER_TOUGHNESS_ID);

            if (bonusToughness > 0) {
                toughnessAttr.addTemporaryModifier(new EntityAttributeModifier(
                        SLAYER_TOUGHNESS_ID,
                        bonusToughness,
                        EntityAttributeModifier.Operation.ADD_VALUE
                ));
            }

        }

    }
}