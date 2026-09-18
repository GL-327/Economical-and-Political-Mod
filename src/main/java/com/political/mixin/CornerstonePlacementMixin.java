package com.political.mixin;

import com.political.CornerstoneItem;
import com.political.settlement.SettlementManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * When a Cornerstone (Bell + custom id) is successfully placed, found a settlement at that spot.
 * Two non-cancelling injects: HEAD records whether the in-hand stack was a Cornerstone (the
 * stack may be consumed by the time TAIL runs), TAIL acts only if placement actually succeeded.
 */
@Mixin(BlockItem.class)
public class CornerstonePlacementMixin {

    private static final ThreadLocal<Boolean> POLITICAL_WAS_CORNERSTONE =
            ThreadLocal.withInitial(() -> Boolean.FALSE);

    @Inject(method = "useOn", at = @At("HEAD"))
    private void political_cornerstone_capture(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack stack = context.getItemInHand();
        POLITICAL_WAS_CORNERSTONE.set(CornerstoneItem.isCornerstone(stack));
    }

    @Inject(method = "useOn", at = @At("TAIL"))
    private void political_cornerstone_found(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        boolean was = POLITICAL_WAS_CORNERSTONE.get();
        POLITICAL_WAS_CORNERSTONE.set(Boolean.FALSE);
        if (!was) return;

        InteractionResult result = cir.getReturnValue();
        if (result == null || !result.consumesAction()) return; // placement failed / did nothing

        if (!(context.getPlayer() instanceof ServerPlayer player)) return;
        if (!(context.getLevel() instanceof ServerLevel world)) return;

        // Approximate placed position the same way as the existing BlockItemMixin.
        BlockPos placed = context.getClickedPos().relative(context.getClickedFace());
        SettlementManager.foundSettlement(player, world, placed);
    }
}
