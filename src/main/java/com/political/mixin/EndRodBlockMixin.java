// EndRodBlockMixin.java
package com.political.mixin;

import com.political.CustomItemHandler;
import net.minecraft.block.EndRodBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndRodBlock.class)
public class EndRodBlockMixin {

    @Inject(method = "getPlacementState", at = @At("HEAD"), cancellable = true)
    private void political_preventHPEBMPlacement(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> cir) {
        if (CustomItemHandler.isHPEBM(ctx.getStack())) {
            cir.setReturnValue(null); // Prevent placement
        }
    }
}