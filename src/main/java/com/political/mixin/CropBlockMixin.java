package com.political.mixin;

import com.political.government.PerkManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CropBlock.class)
public class CropBlockMixin {

    @Inject(method = "randomTick", at = @At("HEAD"))
    private void onRandomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo ci) {
        float multiplier = PerkManager.getCropGrowthMultiplier();

        // Extra growth chance for GREEN_THUMB
        if (multiplier > 1.0f && random.nextFloat() < (multiplier - 1.0f)) {
            CropBlock crop = (CropBlock) (Object) this;
            if (crop.getAge(state) < crop.getMaxAge()) {
                int age = crop.getAge(state);
                world.setBlock(pos, state.setValue(CropBlock.AGE, Math.min(age + 1, crop.getMaxAge())), Block.UPDATE_CLIENTS);
            }
        }
    }
}