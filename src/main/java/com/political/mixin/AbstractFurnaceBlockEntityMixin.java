package com.political.mixin;

import com.political.CustomItemHandler;
import com.political.PerkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin {

    @Inject(
            method = "serverTick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;)V",
            at = @At("HEAD"))
    private static void political_onTick(ServerLevel world, BlockPos pos, BlockState state,
                                        AbstractFurnaceBlockEntity blockEntity, CallbackInfo ci) {
        if (world.isClientSide()) {
            return;
        }
        AbstractFurnaceTimerAccessor acc = (AbstractFurnaceTimerAccessor) blockEntity;
        ItemStack inputStack = blockEntity.getItem(0);
        if (!inputStack.isEmpty() && CustomItemHandler.isProtectedItem(inputStack)) {
            acc.political_setCookingTimer(0);
            return;
        }
        if (PerkManager.hasActivePerk("RESOURCE_SUBSIDY")) {
            int t = acc.political_getCookingTimer();
            if (t > 0) {
                acc.political_setCookingTimer(t + 1);
            }
        }
    }
}
