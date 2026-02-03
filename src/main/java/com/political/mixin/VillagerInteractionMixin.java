package com.political.mixin;

import com.political.AuctionMasterManager;
import com.political.UndergroundAuctionManager;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerEntity.class)
public class VillagerInteractionMixin {

    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    private void onInteract(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;

        VillagerEntity self = (VillagerEntity) (Object) this;

        // Check for Auction Master
        if (AuctionMasterManager.isAuctionMaster(self)) {
            AuctionMasterManager.handleInteraction(player, self);
            cir.setReturnValue(ActionResult.SUCCESS);
            return;
        }

        // Check for Underground Auctioneer
        if (UndergroundAuctionManager.isAuctioneer(self)) {
            com.political.UndergroundAuctionGui.open(serverPlayer);
            cir.setReturnValue(ActionResult.SUCCESS);
            return;
        }
    }
}