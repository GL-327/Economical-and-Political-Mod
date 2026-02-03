package com.political.mixin;

import com.political.AuctionMasterManager;
import com.political.UndergroundAuctionManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class AuctionNPCDamageMixin {

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void political_preventAuctionNPCDamage(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;

        // Only check if it's a VillagerEntity
        if (self instanceof VillagerEntity villager) {
            // Prevent ALL damage to Auction Master and Underground Auctioneer
            if (AuctionMasterManager.isAuctionMaster(villager) || UndergroundAuctionManager.isAuctioneer(villager)) {
                cir.setReturnValue(false);
            }
        }
    }
}