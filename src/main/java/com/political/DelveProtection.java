package com.political;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

/**
 * What you may and may not touch inside a delve.
 *
 * <p>Almost nothing is breakable, which is the rule Skyblock runs on and the reason its delves
 * hold their shape. A delve you can mine through is a delve where the intended route is a
 * suggestion, every secret is findable with a pickaxe, and the boss room is reachable in a straight
 * line from the entrance.</p>
 *
 * <p>The exceptions are the handful of things the design actually needs: the blocks a secret hides
 * behind, the things you open, and the crops and pots that are scenery. Everything else refuses,
 * and says so once rather than every swing.</p>
 */
public final class DelveProtection {

    private DelveProtection() {}

    /**
     * The short list of blocks a player may break inside a delve.
     *
     * <p>Deliberately short. Every entry here is something a room might legitimately ask you to
     * clear, and the list is checked rather than a block tag so nothing gets added to it by a
     * datapack the server owner installed for something else.</p>
     */
    private static final Set<Block> BREAKABLE = Set.of(
            // Secrets sit behind these.
            Blocks.SCULK_SHRIEKER, Blocks.SCULK_SENSOR,
            Blocks.COBWEB,
            // Scenery, and the things people expect to be able to smash.
            Blocks.FLOWER_POT, Blocks.DECORATED_POT,
            Blocks.TORCH, Blocks.WALL_TORCH, Blocks.SOUL_TORCH, Blocks.SOUL_WALL_TORCH,
            Blocks.VINE, Blocks.CAVE_VINES, Blocks.CAVE_VINES_PLANT,
            Blocks.BROWN_MUSHROOM, Blocks.RED_MUSHROOM,
            Blocks.TALL_GRASS, Blocks.SHORT_GRASS, Blocks.DEAD_BUSH,
            // Barrels and chests are opened rather than broken, but breaking one is harmless.
            Blocks.BARREL
    );

    /** Blocks that may be used but never broken, so the delve still works as a building. */
    private static final Set<Block> USABLE = Set.of(
            Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.BARREL, Blocks.ENDER_CHEST,
            Blocks.LEVER, Blocks.STONE_BUTTON, Blocks.OAK_BUTTON, Blocks.SPRUCE_BUTTON,
            Blocks.BIRCH_BUTTON, Blocks.JUNGLE_BUTTON, Blocks.ACACIA_BUTTON,
            Blocks.DARK_OAK_BUTTON, Blocks.CRIMSON_BUTTON, Blocks.WARPED_BUTTON,
            Blocks.POLISHED_BLACKSTONE_BUTTON
    );

    public static void register() {
        // Breaking.
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, entity) -> {
            if (world.isClientSide()) return true;
            if (!(player instanceof ServerPlayer sp)) return true;
            if (!DelveManager.inDelve(sp)) return true;

            // An operator in creative is presumably fixing something.
            if (sp.isCreative()) return true;

            if (BREAKABLE.contains(state.getBlock())) {
                DelveRun run = DelveManager.runOf(sp);
                if (run != null) DelvePopulator.claimSecret(run, pos, sp);
                return true;
            }
            warn(sp);
            return false;
        });

        // Using. A secret can also be a thing you click rather than a thing you break.
        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (world.isClientSide()) return InteractionResult.PASS;
            if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
            if (!DelveManager.inDelve(sp)) return InteractionResult.PASS;

            BlockPos pos = hit.getBlockPos();
            BlockState state = world.getBlockState(pos);
            DelveRun run = DelveManager.runOf(sp);
            if (run != null && DelvePopulator.claimSecret(run, pos, sp)) {
                return InteractionResult.SUCCESS;
            }
            if (USABLE.contains(state.getBlock())) return InteractionResult.PASS;

            // Placing blocks would let a party stair up and over the whole floor.
            if (!player.getItemInHand(hand).isEmpty()
                    && player.getItemInHand(hand).getItem() instanceof net.minecraft.world.item.BlockItem
                    && !sp.isCreative()) {
                warn(sp);
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });
    }

    /**
     * Says no, but only about once a second.
     *
     * <p>Holding down a mouse button fires the break event many times a second, and a message per
     * attempt fills the chat so fast the player cannot read the one that mattered.</p>
     */
    private static void warn(ServerPlayer sp) {
        long now = PoliticalServer.server == null ? 0 : PoliticalServer.server.getTickCount();
        Long last = LAST_WARNED.get(sp.getUUID());
        if (last != null && now - last < 20) return;
        LAST_WARNED.put(sp.getUUID(), now);
        sp.sendSystemMessage(Component.literal("§8The delve does not give."), true);
    }

    private static final java.util.Map<java.util.UUID, Long> LAST_WARNED =
            new java.util.concurrent.ConcurrentHashMap<>();

    public static void forget(ServerPlayer p) {
        LAST_WARNED.remove(p.getUUID());
    }
}
