package com.political.mixin;

import com.political.AuctionMasterManager;
import com.political.UndergroundAuctionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerWorld.class)
public class EntityLoadMixin {

    @Inject(method = "addEntity", at = @At("TAIL"))
    private void onEntityLoad(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof VillagerEntity villager) {
            if (AuctionMasterManager.isAuctionMaster(villager) || UndergroundAuctionManager.isAuctioneer(villager)) {
                villager.setInvulnerable(true);
            }
        }
    }
}