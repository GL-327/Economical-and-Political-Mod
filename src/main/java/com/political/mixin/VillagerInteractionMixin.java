package com.political.mixin;

import com.political.AuctionMasterManager;
import com.political.UndergroundAuctionManager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Villager.class)
public class VillagerInteractionMixin {

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void onInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        Villager self = (Villager) (Object) this;

        // Check for Auction Master
        if (AuctionMasterManager.isAuctionMaster(self)) {
            AuctionMasterManager.handleInteraction(player, self);
            cir.setReturnValue(InteractionResult.SUCCESS);
            return;
        }

        // Check for Underground Auctioneer
        if (UndergroundAuctionManager.isAuctioneer(self)) {
            com.political.UndergroundAuctionGui.open(serverPlayer);
            cir.setReturnValue(InteractionResult.SUCCESS);
            return;
        }
    }
}