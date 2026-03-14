package com.political.mixin;

import com.political.ArmourAttribute;
import com.political.CustomItemHandler;
import com.political.SlayerItems;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.Property;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.entity.EquipmentSlot;

@Mixin(AnvilScreenHandler.class)
public class AnvilCraftingMixin {

    @Shadow @Final private Property levelCost;

    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    private void handleAttributeApplication(CallbackInfo ci) {
        AnvilScreenHandler handler = (AnvilScreenHandler) (Object) this;
        ItemStack first = handler.getSlot(0).getStack();
        ItemStack second = handler.getSlot(1).getStack();

        if (first.isEmpty() || second.isEmpty()) return;

        // Check if second slot is an Attribute Token
        ArmourAttribute attr = SlayerItems.getAttributeFromToken(second);
        if (attr != null) {
            // Validate first slot is armor
            EquipmentSlot slot = null;
            if (first.getItem() instanceof ArmorItem armor) {
                slot = armor.getSlotType();
            } else if (first.get(DataComponentTypes.EQUIPPABLE) != null) {
                slot = first.get(DataComponentTypes.EQUIPPABLE).slot();
            }

            if (slot == null || !attr.isValidSlot(slot)) {
                handler.getSlot(2).setStack(ItemStack.EMPTY);
                ci.cancel();
                return;
            }

            // Check if piece already has an attribute
            if (SlayerItems.getAppliedAttribute(first) != null) {
                handler.getSlot(2).setStack(ItemStack.EMPTY);
                ci.cancel();
                return;
            }

            // SUCCESS: Apply attribute
            ItemStack result = first.copy();
            SlayerItems.applyAttribute(result, attr);
            
            // Set cost
            this.levelCost.set(attr.xpCost);
            
            handler.getSlot(2).setStack(result);
            ci.cancel(); // Skip vanilla processing
            return;
        }

        // Block any operation that involves a protected custom item as an input.
        if (CustomItemHandler.isProtectedItem(first) || CustomItemHandler.isProtectedItem(second)) {
            handler.getSlot(2).setStack(ItemStack.EMPTY);
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
        Text name = stack.getName();
        if (name == null) return false;
        String nameStr = name.getString();
        return nameStr.contains("HPEBM") || nameStr.contains("High-Powered");
    }

    private boolean isGavel(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Text name = stack.getName();
        if (name == null) return false;
        return name.getString().contains("Gavel");
    }

    private boolean isNormalSword(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (isCustomItem(stack)) return false;
        return stack.isOf(Items.IRON_SWORD) ||
                stack.isOf(Items.DIAMOND_SWORD) ||
                stack.isOf(Items.NETHERITE_SWORD) ||
                stack.isOf(Items.STONE_SWORD) ||
                stack.isOf(Items.WOODEN_SWORD) ||
                stack.isOf(Items.GOLDEN_SWORD);
    }

    private boolean isNormalShovel(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (isCustomItem(stack)) return false;
        return stack.isOf(Items.IRON_SHOVEL) ||
                stack.isOf(Items.DIAMOND_SHOVEL) ||
                stack.isOf(Items.NETHERITE_SHOVEL) ||
                stack.isOf(Items.STONE_SHOVEL) ||
                stack.isOf(Items.WOODEN_SHOVEL) ||
                stack.isOf(Items.GOLDEN_SHOVEL);
    }

    private boolean isSameCustomType(ItemStack first, ItemStack second) {
        if (SlayerItems.isSlayerSword(first) && SlayerItems.isSlayerSword(second)) {
            return SlayerItems.getSwordSlayerType(first) == SlayerItems.getSwordSlayerType(second);
        }
        return false;
    }
}
