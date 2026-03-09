package com.political.mixin;

import com.political.SkeletonBowHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BowItem.class)
public class BowItemMixin {

    @Inject(method = "onStoppedUsing", at = @At("HEAD"), cancellable = true)
    private void political_skeletonBowShot(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfoReturnable<Boolean> ci) {
        if (world.isClient()) return;
        if (!(user instanceof ServerPlayerEntity serverPlayer)) return;
        if (!SkeletonBowHandler.isSkeletonBow(stack)) return;

        int chargeTime = stack.getMaxUseTime(user) - remainingUseTicks;
        boolean handled = SkeletonBowHandler.handleBowShot(serverPlayer, stack, chargeTime);
        if (handled) {
            ci.setReturnValue(true);
        }
    }
}
