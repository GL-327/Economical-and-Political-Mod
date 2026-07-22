package com.political;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;

/** Random garden-heart style bonuses when harvesting mature crops. */
public final class GardenHeartEvents {

    private GardenHeartEvents() {}

    public static void onMatureCropBroken(ServerPlayer player, ServerLevel world, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof CropBlock)) {
            return;
        }
        double g = AscensionHeartSystem.gardenPotency(player.getUUID().toString());
        if (g <= 1.02) {
            return;
        }
        float chance = 0.06f * (float) (g - 1.0);
        if (player.getRandom().nextFloat() >= chance) {
            return;
        }
        var drops = net.minecraft.world.level.block.Block.getDrops(state, world, pos, null, player, player.getMainHandItem());
        for (ItemStack drop : drops) {
            if (!drop.isEmpty()) {
                ItemStack bonus = drop.copy();
                bonus.setCount(Math.max(1, bonus.getCount()));
                net.minecraft.world.level.block.Block.popResource(world, pos, bonus);
                break;
            }
        }
        player.sendSystemMessage(Component.literal("§aHarvest Surge!").withStyle(ChatFormatting.GREEN), true);
    }
}
