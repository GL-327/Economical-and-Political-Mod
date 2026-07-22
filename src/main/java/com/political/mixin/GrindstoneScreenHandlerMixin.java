package com.political.mixin;

import com.political.CustomItemHandler;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.GrindstoneMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevents custom items from being disenchanted or repaired via the grindstone.
 * The grindstone can strip enchantments and custom data, which would destroy
 * the functionality of custom items.
 */
@Mixin(GrindstoneMenu.class)
public class GrindstoneScreenHandlerMixin {

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void political_preventCustomItemGrindstone(CallbackInfo ci) {
        GrindstoneMenu handler = (GrindstoneMenu) (Object) this;

        ItemStack top    = handler.getSlot(0).getItem();
        ItemStack bottom = handler.getSlot(1).getItem();

        if (CustomItemHandler.isProtectedItem(top) || CustomItemHandler.isProtectedItem(bottom)) {
            // Clear the output slot so the player can't take anything out
            handler.getSlot(2).set(ItemStack.EMPTY);
            ci.cancel();
        }
    }
}
