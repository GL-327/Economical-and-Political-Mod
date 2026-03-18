package com.political.mixin;

import com.political.PerkManager;
import net.minecraft.block.CropBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CropBlock.class)
public class CropBlockMixin {

    @Inject(method = "randomTick", at = @At("HEAD"))
    private void onRandomTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        float multiplier = PerkManager.getCropGrowthMultiplier();

        // Extra growth chance for GREEN_THUMB
        if (multiplier > 1.0f && random.nextFloat() < (multiplier - 1.0f)) {
            CropBlock crop = (CropBlock) (Object) this;
            if (!crop.isMature(state)) {
                int age = crop.getAge(state);
                world.setBlockState(pos, crop.withAge(Math.min(age + 1, crop.getMaxAge())), 2);
            }
        }
    }
}