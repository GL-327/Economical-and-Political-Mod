package com.political.mixin;

import net.minecraft.block.EndGatewayBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EndGatewayBlock.class)
public class EndGatewayMixin {

    @Inject(method = "onEntityCollision", at = @At("HEAD"), cancellable = true)
    private void political_interceptGateway(BlockState state, World world, BlockPos pos,
            Entity entity, EntityCollisionHandler handler, boolean bool, CallbackInfo ci) {
        if (!(entity instanceof ServerPlayerEntity player)) return;
        if (!(world instanceof ServerWorld serverWorld)) return;

        // Warden Core check - if the user wants to keep some gateway mechanic later, they can, 
        // but for now we remove the Backrooms redirection.
    }
}
