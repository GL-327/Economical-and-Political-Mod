package com.political.backrooms.attribute;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Manages reading and writing ArmorAttribute data stored in armor piece NBT.
 */
public class ArmorAttributeManager {

    public static final String ATTRIBUTE_KEY = "backrooms_attribute";

    /** Writes an ArmorAttribute into an armor piece's CUSTOM_DATA NBT. */
    public static void setArmorAttribute(ItemStack armorStack, ArmorAttribute attribute) {
        NbtComponent existing = armorStack.get(DataComponentTypes.CUSTOM_DATA);
        NbtCompound nbt = (existing != null) ? existing.copyNbt() : new NbtCompound();
        nbt.putString(ATTRIBUTE_KEY, attribute.name());
        armorStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

    /** Reads the ArmorAttribute from an armor piece, or null if none. */
    public static ArmorAttribute getArmorAttribute(ItemStack armorStack) {
        if (armorStack == null || armorStack.isEmpty()) return null;
        NbtComponent data = armorStack.get(DataComponentTypes.CUSTOM_DATA);
        if (data == null) return null;
        NbtCompound nbt = data.copyNbt();
        String name = nbt.getString(ATTRIBUTE_KEY, null);
        if (name == null || name.isEmpty()) return null;
        try {
            return ArmorAttribute.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Counts how many armor pieces a player is wearing that have the specified attribute.
     * Checks HEAD, CHEST, LEGS, FEET slots.
     */
    public static int countArmorWithAttribute(ServerPlayerEntity player, ArmorAttribute target) {
        int count = 0;
        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = player.getEquippedStack(slot);
            if (target.equals(getArmorAttribute(stack))) {
                count++;
            }
        }
        return count;
    }
}
