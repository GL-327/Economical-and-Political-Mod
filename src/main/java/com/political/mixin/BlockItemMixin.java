package com.political.mixin;

import com.political.CustomItemHandler;
import com.political.SpawnProtectionManager;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void political_preventCustomItemPlacement(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = context.getStack();

        if (CustomItemHandler.isProtectedItem(stack)) {
            // Cancel the placement entirely
            cir.setReturnValue(ActionResult.FAIL);

            if (context.getPlayer() instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.sendMessage(
                        Text.literal("You cannot place this custom item!").formatted(Formatting.RED),
                        true
                );
            }
            return;
        }

        // Spawn-protection: block placement by non-operators
        if (context.getPlayer() instanceof ServerPlayerEntity serverPlayer
                && !SpawnProtectionManager.isOperator(serverPlayer)) {
            BlockPos targetPos = context.getBlockPos().offset(context.getSide());
            if (SpawnProtectionManager.isBlockProtected(targetPos.getX(), targetPos.getY(), targetPos.getZ())) {
                cir.setReturnValue(ActionResult.FAIL);
                serverPlayer.sendMessage(
                        Text.literal("⛔ This area is under Government Protection!").formatted(Formatting.RED),
                        true
                );
            }
        }
    }
}
