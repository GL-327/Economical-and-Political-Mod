package com.political.mixin;

import com.political.SpawnProtectionManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Prevents non-operator players from breaking blocks inside the spawn-protection region.
 */
@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {

    @Shadow @Final public ServerPlayerEntity player;

    @Inject(method = "tryBreakBlock", at = @At("HEAD"), cancellable = true)
    private void political_preventBreakInProtectedZone(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {

        // Block-break prevention in spawn-protection region
        if (!SpawnProtectionManager.isBlockProtected(pos.getX(), pos.getY(), pos.getZ())) return;
        if (SpawnProtectionManager.isOperator(player)) return;

        cir.setReturnValue(false);
        player.sendMessage(
                Text.literal("⛔ This area is under Government Protection!").formatted(Formatting.RED),
                true);
    }
}
