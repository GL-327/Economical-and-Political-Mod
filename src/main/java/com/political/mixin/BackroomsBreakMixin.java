package com.political.mixin;

import com.political.BackroomsManager;
import com.political.SpawnProtectionManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Non-operators cannot break blocks inside the Backrooms dimension.
 */
@Mixin(ServerPlayerGameMode.class)
public class BackroomsBreakMixin {

    @Shadow @Final public ServerPlayer player;

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void political_preventBackroomsBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!BackroomsManager.isBackrooms(player.level())) return;
        if (SpawnProtectionManager.isOperator(player)) return;
        cir.setReturnValue(false);
        player.sendSystemMessage(
                Component.literal("§eThe walls of the Backrooms will not yield.").withStyle(ChatFormatting.YELLOW),
                true);
    }
}
