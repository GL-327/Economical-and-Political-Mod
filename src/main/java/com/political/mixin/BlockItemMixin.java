package com.political.mixin;

import com.political.CustomItemHandler;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void political_preventHPEBMPlacement(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = context.getStack();

        if (CustomItemHandler.isHPEBM(stack)) {
            // Cancel the placement entirely
            cir.setReturnValue(ActionResult.FAIL);

            if (context.getPlayer() instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.sendMessage(
                        Text.literal("You cannot place beam weapons!").formatted(Formatting.RED),
                        true
                );
            }
        }
    }
}