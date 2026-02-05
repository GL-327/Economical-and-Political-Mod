// VillagerLightningMixin.java
package com.political.mixin;

import com.political.AuctionMasterManager;
import com.political.UndergroundAuctionManager;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerEntity.class)
public class VillagerLightningMixin {

    @Inject(method = "onStruckByLightning", at = @At("HEAD"), cancellable = true)
    private void political_preventAuctionMasterConversion(ServerWorld world, LightningEntity lightning, CallbackInfo ci) {
        VillagerEntity self = (VillagerEntity) (Object) this;

        if (AuctionMasterManager.isAuctionMaster(self) || UndergroundAuctionManager.isAuctioneer(self)) {
            ci.cancel(); // Prevent witch conversion
        }
    }
}