package com.political.mixin;

import com.political.ChestAccessHistory;
import com.political.CivilCraftAchievements;
import com.political.GardenHeartEvents;
import com.political.SpawnProtectionManager;
import com.political.DanielsPickaxe;
import com.political.LegendaryToolHandler;
import com.political.MasteryManager;
import com.political.ProgressionTreeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.Container;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.ChatFormatting;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
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
@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {

    @Shadow @Final public ServerPlayer player;

    private static final ThreadLocal<BlockState> POLITICAL_PRE_BREAK_STATE = new ThreadLocal<>();

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void political_capturePreBreakState(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        POLITICAL_PRE_BREAK_STATE.set(player.level().getBlockState(pos));
    }

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void political_preventBreakInProtectedZone(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {

        // Block-break prevention in spawn-protection region
        if (!SpawnProtectionManager.isBlockProtected(pos.getX(), pos.getY(), pos.getZ())) return;
        if (SpawnProtectionManager.isOperator(player)) return;

        cir.setReturnValue(false);
        player.sendSystemMessage(
                Component.literal("⛔ This area is under Government Protection!").withStyle(ChatFormatting.RED),
                true);
    }

    @Inject(method = "destroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"), cancellable = true)
    private void political_handleSpecialToolBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        ServerLevel world = (ServerLevel) player.level();
        BlockState state = world.getBlockState(pos);
        
        // Handle Daniel's Pickaxe special effects
        if (DanielsPickaxe.onBlockBreak(player, world, pos, state)) {
            cir.setReturnValue(true);
            return;
        }
        
        // Handle Legendary Tool effects
        if (LegendaryToolHandler.onBlockBreak(player, world, pos, state, player.getMainHandItem())) {
            cir.setReturnValue(true);
            return;
        }
    }

    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void political_awardMasteryXp(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockState preBreak = POLITICAL_PRE_BREAK_STATE.get();
        POLITICAL_PRE_BREAK_STATE.remove();
        if (!Boolean.TRUE.equals(cir.getReturnValue())) return;

        ServerLevel world = (ServerLevel) player.level();
        Block block = preBreak != null ? preBreak.getBlock() : world.getBlockState(pos).getBlock();
        BlockState stateForCrop = preBreak != null ? preBreak : world.getBlockState(pos);
        if (block instanceof CropBlock crop && crop.getAge(stateForCrop) >= crop.getMaxAge()) {
            CivilCraftAchievements.trackCropHarvest(player);
            GardenHeartEvents.onMatureCropBroken(player, world, pos, stateForCrop);
        } else {
            CivilCraftAchievements.trackBlockMined(player);
        }
        MasteryManager.awardMiningXp(player, player.getMainHandItem());
        ProgressionTreeManager.awardSkillPoints(player, ProgressionTreeManager.Branch.MINING, 1);
    }

    @Inject(method = "useItemOn", at = @At("RETURN"))
    private void political_recordOpenedContainer(ServerPlayer serverPlayer, Level world, ItemStack stack,
                                                 InteractionHand hand, BlockHitResult hitResult,
                                                 CallbackInfoReturnable<InteractionResult> cir) {
        if (!(world instanceof ServerLevel sw)) return;
        InteractionResult res = cir.getReturnValue();
        if (!res.consumesAction()) return;
        BlockPos pos = hitResult.getBlockPos();
        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof Container)) return;
        if (serverPlayer.isSpectator()) return;
        String blockId = BuiltInRegistries.BLOCK.getKey(world.getBlockState(pos).getBlock()).toString();
        String worldId = sw.dimension().identifier().toString();
        ChestAccessHistory.record(serverPlayer, worldId, pos, blockId);
    }
}
