package com.political;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Puzzle rooms that are actually puzzles.
 *
 * <p>The role existed and did nothing: a puzzle room was a normal room with a different word on it.
 * This gives it a mechanism, and the mechanism is built rather than found, because the rooms come
 * from three unrelated packs and none of them has a lever where this would need one.</p>
 *
 * <p>The puzzle is an order: five levers, and a row of coloured wool above them showing which order
 * to pull. Get it right and the room opens. Get it wrong and it resets and bites. It is readable
 * from the doorway, solvable without a wiki, and the same every time in structure while being
 * different every time in answer, which is the only combination that survives being run a hundred
 * times.</p>
 */
public final class DelvePuzzles {

    private DelvePuzzles() {}

    /** How many levers a puzzle uses. Five is enough to be a puzzle and few enough to hold. */
    private static final int LEVERS = 5;

    /** Wool in the order the clue is shown, matched to the lever under each colour. */
    private static final Block[] COLOURS = {
            Blocks.RED_WOOL, Blocks.YELLOW_WOOL, Blocks.LIME_WOOL,
            Blocks.LIGHT_BLUE_WOOL, Blocks.MAGENTA_WOOL
    };

    /** One puzzle, mid solve. */
    private static final class Puzzle {
        /** Lever positions, in the order the clue says to pull them. */
        final List<BlockPos> order = new ArrayList<>();
        /** Where each lever is, so a click can be matched to an index. */
        final Map<Long, Integer> byPos = new HashMap<>();
        int progress;
        boolean solved;
        BlockPos rewardAt;
        /** The room this puzzle is in, so solving it can mark that room cleared. */
        DelveBuilder.Placed room;
    }

    /** Run to the puzzles inside it. */
    private static final Map<UUID, List<Puzzle>> PUZZLES = new HashMap<>();

    // ── Building ──────────────────────────────────────────────────────────

    /**
     * Builds the mechanism inside a puzzle room.
     *
     * <p>Placed against a wall of its own rather than against the room's, so it works whatever the
     * room is made of and cannot be mistaken for part of somebody's build. Five levers in a row,
     * the clue above them, and the reward chest behind.</p>
     */
    public static void build(ServerLevel level, DelveRun run, DelveBuilder.Placed room, Random rng) {
        Puzzle puzzle = new Puzzle();

        // Anchored to the middle of the room rather than to a fixed offset from its corner. The
        // first version assumed every room reached at least ten blocks in from its own edge, which
        // is true of most of them and false of exactly the small ones a puzzle would be buried in.
        int span = (LEVERS - 1) * 2;
        int baseX = room.origin.getX() + Math.max(1, (room.size.getX() - span) / 2);
        int y = room.floorY + 1;
        int z = room.origin.getZ() + Math.max(2, room.size.getZ() / 2);

        // Backing, so the levers have something to sit on whatever the room's floor is doing.
        for (int i = 0; i < LEVERS; i++) {
            for (int h = 0; h < 3; h++) {
                set(level, baseX + i * 2, y + h, z, Blocks.POLISHED_DEEPSLATE);
            }
        }

        // The answer, then the clue that shows it.
        List<Integer> answer = new ArrayList<>();
        for (int i = 0; i < LEVERS; i++) answer.add(i);
        Collections.shuffle(answer, rng);

        for (int slot = 0; slot < LEVERS; slot++) {
            int lever = answer.get(slot);
            BlockPos at = new BlockPos(baseX + lever * 2, y + 1, z + 1);
            placeLever(level, at);
            puzzle.byPos.put(at.asLong(), slot);
            puzzle.order.add(at);

            // The clue: the colour of the lever that belongs in this position, laid left to right.
            set(level, baseX + slot * 2, y + 3, z, COLOURS[lever]);
            // And the same colour under its own lever, so the two can be matched up.
            set(level, baseX + lever * 2, y, z, COLOURS[lever]);
        }

        puzzle.room = room;
        puzzle.rewardAt = new BlockPos(baseX + span / 2, y, z - 2);
        set(level, puzzle.rewardAt, Blocks.AIR);

        PUZZLES.computeIfAbsent(run.id, k -> new ArrayList<>()).add(puzzle);
    }

    private static void placeLever(ServerLevel level, BlockPos at) {
        BlockState lever = Blocks.LEVER.defaultBlockState()
                .setValue(BlockStateProperties.ATTACH_FACE, AttachFace.WALL)
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH);
        level.setBlock(at, lever, Block.UPDATE_CLIENTS);
    }

    private static void set(ServerLevel level, int x, int y, int z, Block block) {
        level.setBlock(new BlockPos(x, y, z), block.defaultBlockState(), Block.UPDATE_CLIENTS);
    }

    private static void set(ServerLevel level, BlockPos at, Block block) {
        level.setBlock(at, block.defaultBlockState(), Block.UPDATE_CLIENTS);
    }

    // ── Solving ───────────────────────────────────────────────────────────

    /**
     * A lever was pulled.
     *
     * <p>Returns true when the click belonged to a puzzle, so the caller knows to stop rather than
     * letting the ordinary use handling run as well.</p>
     */
    public static boolean onLever(DelveRun run, BlockPos at, ServerPlayer by) {
        List<Puzzle> list = PUZZLES.get(run.id);
        if (list == null) return false;

        for (Puzzle p : list) {
            if (p.solved) continue;
            Integer slot = p.byPos.get(at.asLong());
            if (slot == null) continue;

            if (slot == p.progress) {
                p.progress++;
                ping(by, SoundEvents.NOTE_BLOCK_BELL.value(), 1.0f + p.progress * 0.15f);
                if (p.progress >= LEVERS) {
                    solve(run, p, by);
                } else {
                    by.sendSystemMessage(Component.literal("§a✔ §7" + p.progress
                            + " of " + LEVERS), true);
                }
            } else {
                // Wrong one. Reset and take a bite, so guessing costs something.
                p.progress = 0;
                ping(by, SoundEvents.NOTE_BLOCK_BASS.value(), 0.6f);
                by.sendSystemMessage(Component.literal("§c✖ Wrong. It resets."), true);
                if (by.level() instanceof ServerLevel level) {
                    by.hurtServer(level, by.damageSources().magic(), 4.0f + run.floor.tier);
                }
            }
            return true;
        }
        return false;
    }

    private static void solve(DelveRun run, Puzzle p, ServerPlayer by) {
        p.solved = true;
        if (!(by.level() instanceof ServerLevel level)) return;

        // The reward, and the room counts as cleared for having been solved rather than emptied.
        level.setBlock(p.rewardAt, Blocks.CHEST.defaultBlockState(), 3);
        if (level.getBlockEntity(p.rewardAt)
                instanceof net.minecraft.world.level.block.entity.ChestBlockEntity chest) {
            for (int i = 0; i < 4; i++) {
                chest.setItem(i * 3, DelveItems.spoil(
                        DelveSpoils.KINDS.get(by.getRandom().nextInt(DelveSpoils.KINDS.size())),
                        DelveSpoils.DEPTHS.get(Math.min(DelveSpoils.DEPTHS.size() - 1,
                                run.floor.tier - 1)),
                        DelveSpoils.VARIANTS.get(8 + by.getRandom().nextInt(5))));
            }
        }

        // Solving is what clears a puzzle room. Emptying it does not, so without this the floor
        // could never finish and the boss would never unseal.
        if (p.room != null && !p.room.cleared) {
            p.room.cleared = true;
            run.roomsCleared++;
        }

        for (ServerPlayer m : run.online()) {
            m.sendSystemMessage(Component.literal("§b✦ §7" + by.getName().getString()
                    + " solved a puzzle. §f" + run.roomsCleared + "/" + run.roomsTotal));
        }
        level.playSound(null, p.rewardAt, SoundEvents.PLAYER_LEVELUP,
                SoundSource.PLAYERS, 1.0f, 1.2f);
        DelveDrops.onSecret(by, run);
    }

    private static void ping(ServerPlayer p, net.minecraft.sounds.SoundEvent sound, float pitch) {
        if (p.level() instanceof ServerLevel level) {
            level.playSound(null, p.blockPosition(), sound, SoundSource.PLAYERS, 0.8f, pitch);
        }
    }

    /** Whether every puzzle on this floor is done, which the boss seal waits on. */
    public static boolean allSolved(DelveRun run) {
        List<Puzzle> list = PUZZLES.get(run.id);
        if (list == null || list.isEmpty()) return true;
        for (Puzzle p : list) {
            if (!p.solved) return false;
        }
        return true;
    }

    public static int remaining(DelveRun run) {
        List<Puzzle> list = PUZZLES.get(run.id);
        if (list == null) return 0;
        int n = 0;
        for (Puzzle p : list) {
            if (!p.solved) n++;
        }
        return n;
    }

    public static void forget(DelveRun run) {
        PUZZLES.remove(run.id);
    }
}
