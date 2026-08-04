package com.political;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The things that make a depth feel like somewhere rather than a room list.
 *
 * <p>Two additions, both about feedback. The <b>warded gate</b> is a real wall across the boss room
 * that a party can walk up to and be stopped by, instead of a line of chat saying the boss is
 * sealed. The <b>hazards</b> give each depth its own pressure, so the Ice Vault is cold and the
 * Toxic Lair is poisonous and neither is simply a different colour of corridor.</p>
 *
 * <p>Both are deliberately visible. A mechanic the player cannot see is a mechanic they will assume
 * is broken.</p>
 */
public final class DelveMechanics {

    private DelveMechanics() {}

    /** Where each run's gate was built, so it can be opened exactly once. */
    private static final Map<UUID, BlockPos> GATES = new HashMap<>();
    private static final Map<UUID, Boolean> OPENED = new HashMap<>();

    // ── The warded gate ───────────────────────────────────────────────────

    /**
     * Walls the boss room off.
     *
     * <p>Built across the corridor mouth rather than around the whole room, because a shell around
     * a forty eight block room is thousands of blocks and a wall across its doorway is about forty.
     * The wall is placed at the room's own floor height so it always meets the ground.</p>
     */
    public static void raiseGate(ServerLevel level, DelveRun run) {
        if (run.layout == null || run.layout.bossRoom == null) return;
        DelveBuilder.Placed boss = run.layout.bossRoom;

        BlockPos centre = boss.centre();
        GATES.put(run.id, centre);
        OPENED.put(run.id, false);

        Block bar = Blocks.CRYING_OBSIDIAN;
        // A ring one block outside the room's footprint, at floor level, four high. Anything
        // already solid is left alone so the wall follows the building rather than cutting it.
        int x0 = boss.origin.getX() - 1;
        int z0 = boss.origin.getZ() - 1;
        int x1 = boss.origin.getX() + boss.size.getX();
        int z1 = boss.origin.getZ() + boss.size.getZ();

        for (int y = boss.floorY + 1; y <= boss.floorY + 4; y++) {
            for (int x = x0; x <= x1; x++) {
                fillIfAir(level, x, y, z0, bar);
                fillIfAir(level, x, y, z1, bar);
            }
            for (int z = z0; z <= z1; z++) {
                fillIfAir(level, x0, y, z, bar);
                fillIfAir(level, x1, y, z, bar);
            }
        }

        for (ServerPlayer p : run.online()) {
            p.sendSystemMessage(Component.literal("§8The way to "
                    + run.floor.bossName + " is warded shut."));
        }
    }

    private static void fillIfAir(ServerLevel level, int x, int y, int z, Block block) {
        BlockPos at = new BlockPos(x, y, z);
        if (level.getBlockState(at).isAir()) {
            level.setBlock(at, block.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    /**
     * Drops the gate once the floor is cleared.
     *
     * <p>Only removes what it put there, so a room whose own walls happened to be crying obsidian
     * does not lose them. Anything else would be a builder's nightmare to debug.</p>
     */
    public static void openGate(ServerLevel level, DelveRun run) {
        if (Boolean.TRUE.equals(OPENED.get(run.id))) return;
        if (run.layout == null || run.layout.bossRoom == null) return;
        OPENED.put(run.id, true);

        DelveBuilder.Placed boss = run.layout.bossRoom;
        int x0 = boss.origin.getX() - 1;
        int z0 = boss.origin.getZ() - 1;
        int x1 = boss.origin.getX() + boss.size.getX();
        int z1 = boss.origin.getZ() + boss.size.getZ();

        for (int y = boss.floorY + 1; y <= boss.floorY + 4; y++) {
            for (int x = x0; x <= x1; x++) {
                clearIfWard(level, x, y, z0);
                clearIfWard(level, x, y, z1);
            }
            for (int z = z0; z <= z1; z++) {
                clearIfWard(level, x0, y, z);
                clearIfWard(level, x1, y, z);
            }
        }

        for (ServerPlayer p : run.online()) {
            p.sendSystemMessage(Component.literal(""));
            p.sendSystemMessage(Component.literal("  §4§lTHE WARD BREAKS"));
            p.sendSystemMessage(Component.literal("  §7" + run.floor.bossName + " is waiting."));
            p.sendSystemMessage(Component.literal(""));
            level.playSound(null, p.blockPosition(), SoundEvents.END_PORTAL_SPAWN,
                    SoundSource.HOSTILE, 0.7f, 1.2f);
        }
    }

    private static void clearIfWard(ServerLevel level, int x, int y, int z) {
        BlockPos at = new BlockPos(x, y, z);
        if (level.getBlockState(at).is(Blocks.CRYING_OBSIDIAN)) {
            level.setBlock(at, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public static void forget(DelveRun run) {
        GATES.remove(run.id);
        OPENED.remove(run.id);
    }

    // ── Depth hazards ─────────────────────────────────────────────────────

    /**
     * The pressure each depth puts on you for being there.
     *
     * <p>Mild on purpose. This is atmosphere and a small tax on standing still, not a second health
     * bar to manage. Anything strong enough to kill an idle player would turn every delve into a
     * race against a timer, which is not what the scoring rewards.</p>
     */
    public static void tickHazards(MinecraftServer server, DelveRun run) {
        if (server.getTickCount() % 60 != 0) return;

        for (ServerPlayer p : run.online()) {
            if (!DelveManager.inDelve(p)) continue;
            switch (run.floor) {
                case CATACOMBS -> p.addEffect(new MobEffectInstance(
                        MobEffects.MINING_FATIGUE, 80, 0, true, false));
                case SUNKEN -> {
                    p.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 0, true, false));
                    // The halls are flooded in spirit if not in fact, so breath goes down.
                    if (p.getAirSupply() > 40) p.setAirSupply(p.getAirSupply() - 20);
                }
                case ICE -> {
                    p.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 0, true, false));
                    p.setTicksFrozen(Math.min(p.getTicksFrozen() + 40, 140));
                }
                case SANDS -> p.addEffect(new MobEffectInstance(
                        MobEffects.HUNGER, 100, 0, true, false));
                case TOXIC -> p.addEffect(new MobEffectInstance(
                        MobEffects.POISON, 60, 0, true, false));
                case CITADEL -> p.addEffect(new MobEffectInstance(
                        MobEffects.WEAKNESS, 100, 0, true, false));
                case END_CASTLE -> {
                    p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0, true, false));
                    p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0, true, false));
                }
            }
        }
    }

    /**
     * A one line description of what a depth does to you, for the menu.
     *
     * <p>Told up front rather than discovered. A player who packed the wrong gear because nobody
     * mentioned the Ice Vault freezes them has not been given an interesting problem.</p>
     */
    public static String hazardOf(DelveDepth floor) {
        return switch (floor) {
            case CATACOMBS -> "The dust slows your hands.";
            case SUNKEN -> "The air is thin and your breath goes.";
            case ICE -> "Cold enough to slow you and freeze you.";
            case SANDS -> "The heat takes it out of you.";
            case TOXIC -> "The air itself is poisonous.";
            case CITADEL -> "Something here saps the strength from a swing.";
            case END_CASTLE -> "Dark, and it takes the strength with it.";
        };
    }
}
