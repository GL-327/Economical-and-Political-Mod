package com.political.mixin;

import com.political.BackroomsManager;
import com.political.SpawnProtectionManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Non-operators cannot place blocks inside the Backrooms dimension (also stops beds).
 */
@Mixin(BlockItem.class)
public class BackroomsPlaceMixin {

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void political_preventBackroomsPlace(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (!(context.getPlayer() instanceof ServerPlayer player)) return;
        if (!BackroomsManager.isBackrooms(player.level())) return;
        if (SpawnProtectionManager.isOperator(player)) return;
        cir.setReturnValue(InteractionResult.FAIL);
        player.sendSystemMessage(
                Component.literal("§eYou cannot reshape the Backrooms.").withStyle(ChatFormatting.YELLOW),
                true);
    }
}
