package com.political.mixin;

import com.political.PerkManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


/*
DISABLED
*/

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin {


    @Shadow
    private int cookingTimeSpent;

    @Inject(method = "tick", at = @At("TAIL"))
    private static void political_onTick(ServerWorld world, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity blockEntity, CallbackInfo ci) {
        if (!world.isClient() && PerkManager.hasActivePerk("RESOURCE_SUBSIDY")) {
            // Access the cookTime through casting
            AbstractFurnaceBlockEntityMixin mixin = (AbstractFurnaceBlockEntityMixin)(Object)blockEntity;
            if (mixin.cookingTimeSpent > 0) {
                mixin.cookingTimeSpent++; // Extra tick = 50% faster
            }
        }
    }
}