package com.political.mixin;

import com.political.ArmourAttribute;
import com.political.CustomItemHandler;
import com.political.SlayerItems;
import com.political.WeaponAttribute;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.entity.EquipmentSlot;

@Mixin(AnvilMenu.class)
public class AnvilCraftingMixin {

    @Shadow @Final private DataSlot cost;

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void handleAttributeApplication(CallbackInfo ci) {
        AnvilMenu handler = (AnvilMenu) (Object) this;
        ItemStack first = handler.getSlot(0).getItem();
        ItemStack second = handler.getSlot(1).getItem();

        if (first.isEmpty() || second.isEmpty()) return;

        // Check if second slot is an Armour Attribute Token
        ArmourAttribute attr = SlayerItems.getAttributeFromToken(second);
        if (attr != null) {
            // Validate first slot is armor
            EquipmentSlot slot = null;
            if (first.get(DataComponents.EQUIPPABLE) != null) {
                slot = first.get(DataComponents.EQUIPPABLE).slot();
            }

            if (slot == null || !attr.isValidSlot(slot)) {
                handler.getSlot(2).set(ItemStack.EMPTY);
                ci.cancel();
                return;
            }

            // Check if piece already has an attribute
            if (SlayerItems.getAppliedAttribute(first) != null) {
                handler.getSlot(2).set(ItemStack.EMPTY);
                ci.cancel();
                return;
            }

            // SUCCESS: Apply attribute
            ItemStack result = first.copy();
            SlayerItems.applyAttribute(result, attr);
            
            // Set cost
            this.cost.set(attr.xpCost);

            handler.getSlot(2).set(result);
            ci.cancel(); // Skip vanilla processing
            return;
        }
        
        // Check if second slot is a Weapon Attribute Token
        WeaponAttribute weaponAttr = SlayerItems.getWeaponAttributeFromToken(second);
        if (weaponAttr != null) {
            // Validate first slot is a weapon (sword or axe)
            if (!isWeapon(first)) {
                handler.getSlot(2).set(ItemStack.EMPTY);
                ci.cancel();
                return;
            }

            // Check if weapon already has an attribute
            if (SlayerItems.getAppliedWeaponAttribute(first) != null) {
                handler.getSlot(2).set(ItemStack.EMPTY);
                ci.cancel();
                return;
            }

            // SUCCESS: Apply weapon attribute
            ItemStack result = first.copy();
            SlayerItems.applyWeaponAttribute(result, weaponAttr);
            
            // Set cost
            this.cost.set(weaponAttr.xpCost);

            handler.getSlot(2).set(result);
            ci.cancel(); // Skip vanilla processing
            return;
        }

        // Block any operation that involves a protected custom item as an input.
        if (CustomItemHandler.isProtectedItem(first) || CustomItemHandler.isProtectedItem(second)) {
            handler.getSlot(2).set(ItemStack.EMPTY);
            ci.cancel();
        }
    }

    private boolean isCustomItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return SlayerItems.isSlayerSword(stack) ||
                SlayerItems.isSlayerCore(stack) ||
                isHPEBM(stack) ||
                isGavel(stack);
    }

    private boolean isHPEBM(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Component name = stack.getHoverName();
        if (name == null) return false;
        String nameStr = name.getString();
        return nameStr.contains("HPEBM") || nameStr.contains("High-Powered");
    }

    private boolean isGavel(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Component name = stack.getHoverName();
        if (name == null) return false;
        return name.getString().contains("Gavel");
    }

    private boolean isNormalSword(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (isCustomItem(stack)) return false;
        return stack.is(Items.IRON_SWORD) ||
                stack.is(Items.DIAMOND_SWORD) ||
                stack.is(Items.NETHERITE_SWORD) ||
                stack.is(Items.STONE_SWORD) ||
                stack.is(Items.WOODEN_SWORD) ||
                stack.is(Items.GOLDEN_SWORD);
    }

    private boolean isNormalShovel(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (isCustomItem(stack)) return false;
        return stack.is(Items.IRON_SHOVEL) ||
                stack.is(Items.DIAMOND_SHOVEL) ||
                stack.is(Items.NETHERITE_SHOVEL) ||
                stack.is(Items.STONE_SHOVEL) ||
                stack.is(Items.WOODEN_SHOVEL) ||
                stack.is(Items.GOLDEN_SHOVEL);
    }

    private boolean isSameCustomType(ItemStack first, ItemStack second) {
        if (SlayerItems.isSlayerSword(first) && SlayerItems.isSlayerSword(second)) {
            return SlayerItems.getSwordSlayerType(first) == SlayerItems.getSwordSlayerType(second);
        }
        return false;
    }
    
    /**
     * Check if item is a weapon (sword or axe)
     */
    private boolean isWeapon(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return stack.is(Items.WOODEN_SWORD) ||
                stack.is(Items.STONE_SWORD) ||
                stack.is(Items.IRON_SWORD) ||
                stack.is(Items.GOLDEN_SWORD) ||
                stack.is(Items.DIAMOND_SWORD) ||
                stack.is(Items.NETHERITE_SWORD) ||
                stack.is(Items.WOODEN_AXE) ||
                stack.is(Items.STONE_AXE) ||
                stack.is(Items.IRON_AXE) ||
                stack.is(Items.GOLDEN_AXE) ||
                stack.is(Items.DIAMOND_AXE) ||
                stack.is(Items.NETHERITE_AXE);
    }
}
