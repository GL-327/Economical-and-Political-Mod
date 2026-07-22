// VillagerLightningMixin.java
package com.political.mixin;

import com.political.AuctionMasterManager;
import com.political.UndergroundAuctionManager;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public class VillagerLightningMixin {

    @Inject(method = "thunderHit", at = @At("HEAD"), cancellable = true)
    private void political_preventAuctionMasterConversion(ServerLevel world, LightningBolt lightning, CallbackInfo ci) {
        Villager self = (Villager) (Object) this;

        if (AuctionMasterManager.isAuctionMaster(self) || UndergroundAuctionManager.isAuctioneer(self)) {
            ci.cancel(); // Prevent witch conversion
        }
    }
}