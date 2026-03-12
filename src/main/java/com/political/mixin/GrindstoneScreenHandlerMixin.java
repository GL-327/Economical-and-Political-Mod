package com.political.mixin;

import com.political.CustomItemHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GrindstoneScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevents custom items from being disenchanted or repaired via the grindstone.
 * The grindstone can strip enchantments and custom data, which would destroy
 * the functionality of custom items.
 */
@Mixin(GrindstoneScreenHandler.class)
public class GrindstoneScreenHandlerMixin {

    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    private void political_preventCustomItemGrindstone(CallbackInfo ci) {
        GrindstoneScreenHandler handler = (GrindstoneScreenHandler) (Object) this;

        ItemStack top    = handler.getSlot(0).getStack();
        ItemStack bottom = handler.getSlot(1).getStack();

        if (CustomItemHandler.isProtectedItem(top) || CustomItemHandler.isProtectedItem(bottom)) {
            // Clear the output slot so the player can't take anything out
            handler.getSlot(2).setStack(ItemStack.EMPTY);
            ci.cancel();
        }
    }
}
