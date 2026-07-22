package com.political.mixin;

import com.political.BuffProgressManager;
import com.political.MasteryHeartTree;
import com.political.PlayerBuffManager;
import com.political.ProgressionTreeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Applies COLLECTOR buff for double ore drops when mining ores.
 */
@Mixin(Block.class)
public class OreDropMixin {

    @Inject(method = "playerDestroy", at = @At("TAIL"))
    private void applyCollectorDoubleDrop(Level world, Player player, BlockPos pos, BlockState state,
            net.minecraft.world.level.block.entity.BlockEntity blockEntity, ItemStack tool, CallbackInfo ci) {
        
        if (world.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) return;
        
        // Check if this is an ore block (contains "ore" in translation key or is a known ore type)
        String blockId = state.getBlock().toString().toLowerCase();
        if (!blockId.contains("ore") && !blockId.contains("diamond") && !blockId.contains("emerald") && 
            !blockId.contains("redstone") && !blockId.contains("lapis") && !blockId.contains("coal")) return;
        
        String uuid = serverPlayer.getUUID().toString();
        double doubleDropChance = PlayerBuffManager.getDoubleOreChance(uuid);
        
        if (doubleDropChance > 0 && world.getRandom().nextDouble() < doubleDropChance) {
            duplicateOreDrops(world, serverPlayer, pos, state, blockEntity, tool, "✦ Double drop! (Collector)");
        }

        if (MasteryHeartTree.rollHeartOreDupe(serverPlayer)) {
            duplicateOreDrops(world, serverPlayer, pos, state, blockEntity, tool, "✦ Heart: Powder Charged");
        }

        double miningTreeBonus = ProgressionTreeManager.getMiningSpeedBonus(uuid);
        if (miningTreeBonus > 0 && world.getRandom().nextDouble() < miningTreeBonus * 0.5) {
            duplicateOreDrops(world, serverPlayer, pos, state, blockEntity, tool, "✦ Progression: Lucky Strike");
        }

        if (state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state)) {
            double cropBonus = PlayerBuffManager.getCropYieldBonus(uuid);
            if (cropBonus > 0 && world.getRandom().nextDouble() < cropBonus) {
                duplicateOreDrops(world, serverPlayer, pos, state, blockEntity, tool, "Extra crop yield (Farmer)");
            }
        }

        if (blockId.contains("_log") || blockId.contains("_stem") || blockId.contains("hyphae")) {
            BuffProgressManager.recordLogChopped(serverPlayer);
            double woodBonus = PlayerBuffManager.getWoodYieldBonus(uuid);
            if (woodBonus > 0 && world.getRandom().nextDouble() < woodBonus) {
                duplicateOreDrops(world, serverPlayer, pos, state, blockEntity, tool, "Extra wood (Woodcutter)");
            }
        }
    }

    private static void duplicateOreDrops(Level world, ServerPlayer serverPlayer, BlockPos pos, BlockState state,
            net.minecraft.world.level.block.entity.BlockEntity blockEntity, ItemStack tool, String message) {
        ServerLevel serverWorld = (ServerLevel) world;
        List<ItemStack> drops = net.minecraft.world.level.block.Block.getDrops(state, serverWorld, pos, blockEntity, serverPlayer, tool);
        for (ItemStack drop : drops) {
            if (!drop.isEmpty()) {
                net.minecraft.world.level.block.Block.popResource(world, pos, drop);
            }
        }
        serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal(message).withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE), true);
    }
}
