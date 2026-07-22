package com.political.mixin;

import com.political.FishingEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vanilla fishing rod: roll sea creature / treasure at the same moment vanilla would reel in a fish,
 * so catches feel like normal fishing instead of the accelerated tick path.
 */
@Mixin(FishingHook.class)
public class FishingHookMixin {

    @Inject(method = "catchingFish", at = @At("HEAD"), cancellable = true)
    private void political$onVanillaCatch(BlockPos pos, CallbackInfo ci) {
        FishingEventHandler.onVanillaCatchingFish((FishingHook) (Object) this, pos, ci);
    }

    @Inject(method = "catchingFish", at = @At("TAIL"))
    private void political$afterVanillaCatch(BlockPos pos, CallbackInfo ci) {
        FishingEventHandler.afterVanillaFishCaught((FishingHook) (Object) this);
    }
}
