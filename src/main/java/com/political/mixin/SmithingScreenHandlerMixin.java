package com.political.mixin;

import com.political.items.CustomItemHandler;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.SmithingMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevents custom items from being upgraded or modified via the smithing table.
 * The smithing table could be used to strip or alter the custom NBT/components
 * stored on these items.
 */
@Mixin(SmithingMenu.class)
public class SmithingScreenHandlerMixin {

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void political_preventCustomItemSmithing(CallbackInfo ci) {
        SmithingMenu handler = (SmithingMenu) (Object) this;

        // Smithing table slots: 0 = template, 1 = input (base item), 2 = material
        ItemStack template = handler.getSlot(0).getItem();
        ItemStack base     = handler.getSlot(1).getItem();
        ItemStack material = handler.getSlot(2).getItem();

        if (CustomItemHandler.isProtectedItem(template)
                || CustomItemHandler.isProtectedItem(base)
                || CustomItemHandler.isProtectedItem(material)) {
            // Clear the output slot so the player can't take the result
            handler.getSlot(3).set(ItemStack.EMPTY);
            ci.cancel();
        }
    }
}
