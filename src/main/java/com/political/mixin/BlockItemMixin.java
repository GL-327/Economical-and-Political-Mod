package com.political.mixin;

import com.political.items.CustomItemHandler;
import com.political.SpawnProtectionManager;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void political_preventCustomItemPlacement(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack stack = context.getItemInHand();

        if (CustomItemHandler.isProtectedItem(stack)) {
            // Cancel the placement entirely
            cir.setReturnValue(InteractionResult.FAIL);

            if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(
                        Component.literal("You cannot place this custom item!").withStyle(ChatFormatting.RED),
                        true
                );
            }
            return;
        }

        // Spawn-protection: block placement by non-operators
        if (context.getPlayer() instanceof ServerPlayer serverPlayer
                && !SpawnProtectionManager.isOperator(serverPlayer)) {
            BlockPos targetPos = context.getClickedPos().relative(context.getClickedFace());
            if (SpawnProtectionManager.isBlockProtected(targetPos.getX(), targetPos.getY(), targetPos.getZ())) {
                cir.setReturnValue(InteractionResult.FAIL);
                serverPlayer.sendSystemMessage(
                        Component.literal("⛔ This area is under Government Protection!").withStyle(ChatFormatting.RED)
                );
            }
        }
    }
}
