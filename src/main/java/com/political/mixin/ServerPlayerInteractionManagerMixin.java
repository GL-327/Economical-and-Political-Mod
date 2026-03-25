package com.political.mixin;

import com.political.ChestAccessHistory;
import com.political.SpawnProtectionManager;
import com.political.DanielsPickaxe;
import com.political.LegendaryToolHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Prevents non-operator players from breaking blocks inside the spawn-protection region.
 * Handles special tool block breaking effects (Daniel's Pickaxe, Legendary Tools).
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

    @Inject(method = "tryBreakBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"), cancellable = true)
    private void political_handleSpecialToolBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        ServerWorld world = player.getEntityWorld();
        BlockState state = world.getBlockState(pos);
        
        // Handle Daniel's Pickaxe special effects
        if (DanielsPickaxe.onBlockBreak(player, world, pos, state)) {
            cir.setReturnValue(true);
            return;
        }
        
        // Handle Legendary Tool effects
        if (LegendaryToolHandler.onBlockBreak(player, world, pos, state, player.getMainHandStack())) {
            cir.setReturnValue(true);
            return;
        }
    }

    @Inject(method = "interactBlock", at = @At("RETURN"))
    private void political_recordOpenedContainer(ServerPlayerEntity serverPlayer, World world, ItemStack stack,
                                                 Hand hand, BlockHitResult hitResult,
                                                 CallbackInfoReturnable<ActionResult> cir) {
        if (!(world instanceof ServerWorld sw)) return;
        ActionResult res = cir.getReturnValue();
        if (!res.isAccepted()) return;
        BlockPos pos = hitResult.getBlockPos();
        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof Inventory)) return;
        if (serverPlayer.isSpectator()) return;
        String blockId = Registries.BLOCK.getId(world.getBlockState(pos).getBlock()).toString();
        String worldId = sw.getRegistryKey().getValue().toString();
        ChestAccessHistory.record(serverPlayer, worldId, pos, blockId);
    }
}
