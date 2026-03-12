package com.political.mixin;

import com.political.CustomItemHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.SmithingScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevents custom items from being upgraded or modified via the smithing table.
 * The smithing table could be used to strip or alter the custom NBT/components
 * stored on these items.
 */
@Mixin(SmithingScreenHandler.class)
public class SmithingScreenHandlerMixin {

    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    private void political_preventCustomItemSmithing(CallbackInfo ci) {
        SmithingScreenHandler handler = (SmithingScreenHandler) (Object) this;

        // Smithing table slots: 0 = template, 1 = input (base item), 2 = material
        ItemStack template = handler.getSlot(0).getStack();
        ItemStack base     = handler.getSlot(1).getStack();
        ItemStack material = handler.getSlot(2).getStack();

        if (CustomItemHandler.isProtectedItem(template)
                || CustomItemHandler.isProtectedItem(base)
                || CustomItemHandler.isProtectedItem(material)) {
            // Clear the output slot so the player can't take the result
            handler.getSlot(3).setStack(ItemStack.EMPTY);
            ci.cancel();
        }
    }
}
