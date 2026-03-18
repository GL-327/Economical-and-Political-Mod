package com.political.mixin;

import com.political.PlayerBuffManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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

    @Inject(method = "afterBreak", at = @At("TAIL"))
    private void applyCollectorDoubleDrop(World world, PlayerEntity player, BlockPos pos, BlockState state, 
            net.minecraft.block.entity.BlockEntity blockEntity, ItemStack tool, CallbackInfo ci) {
        
        if (world.isClient() || !(player instanceof ServerPlayerEntity serverPlayer)) return;
        
        // Check if this is an ore block (contains "ore" in translation key or is a known ore type)
        String blockId = state.getBlock().toString().toLowerCase();
        if (!blockId.contains("ore") && !blockId.contains("diamond") && !blockId.contains("emerald") && 
            !blockId.contains("redstone") && !blockId.contains("lapis") && !blockId.contains("coal")) return;
        
        String uuid = serverPlayer.getUuidAsString();
        double doubleDropChance = PlayerBuffManager.getDoubleOreChance(uuid);
        
        if (doubleDropChance > 0 && world.random.nextDouble() < doubleDropChance) {
            // Get the drops again and spawn them
            ServerWorld serverWorld = (ServerWorld) world;
            List<ItemStack> drops = net.minecraft.block.Block.getDroppedStacks(state, serverWorld, pos, blockEntity, serverPlayer, tool);
            for (ItemStack drop : drops) {
                if (!drop.isEmpty()) {
                    net.minecraft.block.Block.dropStack(world, pos, drop);
                }
            }
            serverPlayer.sendMessage(
                net.minecraft.text.Text.literal("✦ Double drop! (Collector)").formatted(net.minecraft.util.Formatting.LIGHT_PURPLE), 
                true
            );
        }
    }
}
