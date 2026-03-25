package com.political;

import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/**
 * Resolves the playable inventory for a block (handles double chests).
 */
public final class ContainerInventoryUtil {

    private ContainerInventoryUtil() {
    }

    /**
     * Returns the inventory players interact with when opening this block (e.g. 54 slots for double chest).
     */
    public static Inventory getContainerInventory(ServerWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof ChestBlock chestBlock) {
            Inventory chestInv = ChestBlock.getInventory(chestBlock, state, world, pos, false);
            if (chestInv != null) {
                return chestInv;
            }
        }
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof Inventory inv) {
            return inv;
        }
        return null;
    }

    /**
     * Marks the block entity and chunk dirty after remote edits.
     */
    public static void markContainerDirty(ServerWorld world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be != null) {
            be.markDirty();
        }
    }
}
