package com.political.mixin;

import com.political.CustomItemHandler;
import com.political.SlayerItems;
import com.political.backrooms.attribute.ArmorAttribute;
import com.political.backrooms.attribute.ArmorAttributeManager;
import com.political.backrooms.attribute.AttributeRuneItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
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

@Mixin(AnvilScreenHandler.class)
public class AnvilCraftingMixin {

    @Shadow @Final private Property levelCost;

    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    private void preventCustomItemAnvil(CallbackInfo ci) {
        AnvilScreenHandler handler = (AnvilScreenHandler) (Object) this;

        ItemStack first = handler.getSlot(0).getStack();
        ItemStack second = handler.getSlot(1).getStack();

        // --- Rune application: left slot = armor, right slot = AttributeRuneItem ---
        if (!first.isEmpty() && !second.isEmpty()
                && second.getItem() instanceof AttributeRuneItem) {

            // Use getAttributeFromStack to support both baked-in and NBT-stored attributes
            ArmorAttribute attr = AttributeRuneItem.getAttributeFromStack(second);
            EquippableComponent equippable = first.get(DataComponentTypes.EQUIPPABLE);

            if (attr != null && equippable != null && equippable.slot() == attr.requiredSlot) {
                if (ArmorAttributeManager.getArmorAttribute(first) != null) {
                    // Armor piece already has a rune — cannot apply a second one
                    handler.getSlot(2).setStack(ItemStack.EMPTY);
                } else {
                    // Valid combination: apply the rune to a copy of the armor
                    ItemStack result = first.copy();
                    ArmorAttributeManager.setArmorAttribute(result, attr);
                    handler.getSlot(2).setStack(result);
                    this.levelCost.set(attr.getXpCost());
                }
            } else {
                // Rune is incompatible with this armor slot
                handler.getSlot(2).setStack(ItemStack.EMPTY);
            }
            ci.cancel();
            return;
        }

        // Block any operation that involves a protected custom item as an input.
        // This prevents: renaming (which consumes the item), combining, repairing, etc.
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
