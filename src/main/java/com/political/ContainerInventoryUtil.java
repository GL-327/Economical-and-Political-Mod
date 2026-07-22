package com.political;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.Container;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;

/**
 * Resolves the playable inventory for a block (handles double chests).
 */
public final class ContainerInventoryUtil {

    private ContainerInventoryUtil() {
    }

    /**
     * Returns the inventory players interact with when opening this block (e.g. 54 slots for double chest).
     */
    public static Container getContainerInventory(ServerLevel world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof ChestBlock chestBlock) {
            Container chestInv = ChestBlock.getContainer(chestBlock, state, world, pos, false);
            if (chestInv != null) {
                return chestInv;
            }
        }
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof Container inv) {
            return inv;
        }
        return null;
    }

    /**
     * Marks the block entity and chunk dirty after remote edits.
     */
    public static void markContainerDirty(ServerLevel world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be != null) {
            be.setChanged();
        }
    }
}
