package com.political.mixin;

import net.minecraft.world.level.block.EndGatewayBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import com.political.BackroomsManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EndGatewayBlock.class)
public class EndGatewayMixin {

    @Inject(method = "entityInside", at = @At("HEAD"), cancellable = true)
    private void political_interceptGateway(BlockState state, Level world, BlockPos pos,
            Entity entity, InsideBlockEffectApplier applier, boolean bl, CallbackInfo ci) {
        if (!(entity instanceof ServerPlayer player)) return;
        if (!(world instanceof ServerLevel serverWorld)) return;

        // Never re-trigger the noclip from inside the Backrooms.
        if (BackroomsManager.isBackrooms(serverWorld)) return;

        // ~3% chance to "noclip" into the Backrooms instead of the normal gateway teleport.
        if (serverWorld.getRandom().nextFloat() < 0.03f) {
            ci.cancel();
            BackroomsManager.enterBackrooms(player);
        }
    }
}
