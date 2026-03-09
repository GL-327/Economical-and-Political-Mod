package com.political.mixin;

import com.political.CustomItemHandler;
import com.political.PerkManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin {

    @Shadow
    private int cookingTimeSpent;

    @Shadow
    protected abstract net.minecraft.util.collection.DefaultedList<ItemStack> getHeldStacks();

    @Inject(method = "tick", at = @At("HEAD"))
    private static void political_onTick(ServerWorld world, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity blockEntity, CallbackInfo ci) {
        if (world.isClient()) return;

        AbstractFurnaceBlockEntityMixin mixin = (AbstractFurnaceBlockEntityMixin)(Object)blockEntity;

        // Prevent smelting protected custom items
        if (!mixin.getHeldStacks().isEmpty()) {
            ItemStack inputStack = mixin.getHeldStacks().get(0);
            if (CustomItemHandler.isProtectedItem(inputStack)) {
                // Reset cooking progress to prevent smelting
                mixin.cookingTimeSpent = 0;
                return;
            }
        }

        // Speed boost from RESOURCE_SUBSIDY perk
        if (PerkManager.hasActivePerk("RESOURCE_SUBSIDY")) {
            if (mixin.cookingTimeSpent > 0) {
                mixin.cookingTimeSpent++;
            }
        }
    }
}