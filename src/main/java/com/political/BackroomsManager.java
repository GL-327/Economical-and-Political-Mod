package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Drives the Backrooms dimension: lazily stamps 16x16 room cells around players,
 * spawns Wanderer packs, hosts the rare exit portals, and manages entry/escape.
 *
 * Generation is idempotent and permanent — cells are never removed. A cell is only
 * stamped once per session (tracked in {@link #generatedCells}) and, to survive
 * restarts without a save file, cells already present in the persisted world are
 * detected and skipped.
 */
public final class BackroomsManager {

    private BackroomsManager() {}

    public static final ResourceKey<Level> BACKROOMS = ResourceKey.create(
            Registries.DIMENSION, Identifier.fromNamespaceAndPath("politicalserver", "backrooms"));

    private static final int GEN_RADIUS = 2;   // cells around each player (5x5)
    private static final int ENTRY_X = 8;      // anchor room center (cell 0,0)
    private static final int ENTRY_Z = 8;

    /** Cells stamped this session (packed cellX/cellZ). */
    private static final Set<Long> generatedCells = new HashSet<>();
    /** Discovered exit-portal trigger positions (packed cell -> pos). */
    private static final Map<Long, BlockPos> exitPortals = new HashMap<>();

    private static long packCell(int cx, int cz) {
        return (((long) cx) & 0xFFFFFFFFL) | (((long) cz) << 32);
    }

    public static boolean isBackrooms(Level level) {
        return level != null && level.dimension() == BACKROOMS;
    }

    public static ServerLevel getBackrooms(MinecraftServer server) {
        return server == null ? null : server.getLevel(BACKROOMS);
    }

    // ── Tick ────────────────────────────────────────────────────────────────

    public static void tick(MinecraftServer server) {
        ServerLevel level = getBackrooms(server);
        if (level == null) return;

        var players = level.players();
        if (players.isEmpty()) return;

        boolean genPass = server.getTickCount() % 20 == 0; // stamp geometry ~1/sec
        long seed = level.getSeed();

        for (ServerPlayer player : players) {
            if (genPass) {
                ensureAround(level, player.blockPosition(), seed);
            }
            // Void safety: never let a player fall out of the world.
            if (player.getY() < BackroomsGenerator.FLOOR_Y - 5) {
                teleportToAnchor(level, player);
            }
            BackroomsMobs.applyDarknessAura(level, player);
            checkExitProximity(level, player);
        }
    }

    private static void ensureAround(ServerLevel level, BlockPos pos, long seed) {
        int pcx = Math.floorDiv(pos.getX(), BackroomsGenerator.CELL);
        int pcz = Math.floorDiv(pos.getZ(), BackroomsGenerator.CELL);
        for (int dx = -GEN_RADIUS; dx <= GEN_RADIUS; dx++) {
            for (int dz = -GEN_RADIUS; dz <= GEN_RADIUS; dz++) {
                generateCell(level, pcx + dx, pcz + dz, seed, -1);
            }
        }
    }

    /**
     * Generate a single cell if not already present.
     *
     * @param forceVariant a specific BackroomsGenerator variant, or -1 to pick deterministically.
     */
    private static void generateCell(ServerLevel level, int cx, int cz, long seed, int forceVariant) {
        long key = packCell(cx, cz);
        if (generatedCells.contains(key)) return;

        // Restart-safety: if the floor is already solid this cell was stamped in a
        // previous session and persisted — just re-register it.
        BlockPos floorProbe = new BlockPos(cx * BackroomsGenerator.CELL + 8,
                BackroomsGenerator.FLOOR_Y, cz * BackroomsGenerator.CELL + 8);
        boolean alreadyStamped = !level.getBlockState(floorProbe).isAir();

        int variant = forceVariant >= 0 ? forceVariant
                : BackroomsGenerator.pickVariant(seed, cx, cz);

        if (!alreadyStamped) {
            BlockPos exitPos = BackroomsGenerator.stampCell(level, cx, cz, variant, seed);
            if (exitPos != null) {
                exitPortals.put(key, exitPos);
            }
            if (variant == BackroomsGenerator.VARIANT_DARK) {
                BlockPos center = new BlockPos(cx * BackroomsGenerator.CELL + 8,
                        BackroomsGenerator.STAND_Y, cz * BackroomsGenerator.CELL + 8);
                BackroomsMobs.spawnPack(level, center, new Random(
                        BackroomsGenerator.hash(seed, cx, cz, 33)));
            }
        } else if (variant == BackroomsGenerator.VARIANT_EXIT) {
            // Re-register the portal trigger from a persisted exit cell.
            exitPortals.put(key, new BlockPos(cx * BackroomsGenerator.CELL + 8,
                    BackroomsGenerator.STAND_Y + 1, cz * BackroomsGenerator.CELL + 8));
        }
        generatedCells.add(key);
    }

    private static void checkExitProximity(ServerLevel level, ServerPlayer player) {
        if (exitPortals.isEmpty()) return;
        int pcx = Math.floorDiv((int) Math.floor(player.getX()), BackroomsGenerator.CELL);
        int pcz = Math.floorDiv((int) Math.floor(player.getZ()), BackroomsGenerator.CELL);
        // Only check the exit in the player's current cell (portals sit at cell center).
        BlockPos portal = exitPortals.get(packCell(pcx, pcz));
        if (portal == null) return;
        if (player.distanceToSqr(portal.getX() + 0.5, portal.getY(), portal.getZ() + 0.5) <= 2.2 * 2.2) {
            escape(player);
        }
    }

    // ── Entry / escape ────────────────────────────────────────────────────────

    /** Teleport a player into the Backrooms, generating the safe anchor room first. */
    public static void enterBackrooms(ServerPlayer player) {
        MinecraftServer server = ((ServerLevel) player.level()).getServer();
        ServerLevel level = getBackrooms(server);
        if (level == null) {
            player.sendSystemMessage(Component.literal("§cThe Backrooms could not be reached.").withStyle(ChatFormatting.RED));
            return;
        }
        // Force a safe classic hall at the anchor and generate its surroundings.
        long seed = level.getSeed();
        generateCell(level, 0, 0, seed, BackroomsGenerator.VARIANT_HALL);
        ensureAround(level, new BlockPos(ENTRY_X, BackroomsGenerator.STAND_Y, ENTRY_Z), seed);

        teleportToAnchor(level, player);

        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0, true, false, false));
        level.playSound(null, player.blockPosition(), SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.5f, 0.7f);
        player.sendSystemMessage(Component.literal("§e§lYou have no-clipped into the Backrooms."));
        player.sendSystemMessage(Component.literal("§7Find a §bhumming exit portal§7 to escape. §oGood luck.§r"));
    }

    private static void teleportToAnchor(ServerLevel level, ServerPlayer player) {
        PlayerTeleportHelper.teleport(player, level,
                ENTRY_X + 0.5, BackroomsGenerator.STAND_Y, ENTRY_Z + 0.5,
                player.getYRot(), player.getXRot());
    }

    /** Successful escape via an exit portal / Escape Beacon: rewards + overworld return. */
    public static void escape(ServerPlayer player) {
        MinecraftServer server = ((ServerLevel) player.level()).getServer();
        if (server == null) return;

        // Fire the previously-dead progression hooks.
        BuffProgressManager.onBackroomsEscape(player);
        CivilCraftAchievements.trackBackroomsEscape(player);

        returnToOverworld(player, server);

        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1, false, true, true));
        if (player.level() instanceof ServerLevel ow) {
            ow.playSound(null, player.blockPosition(), SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.6f, 1.2f);
        }
        player.sendSystemMessage(Component.literal("§a§l✦ You tore free from the Backrooms!"));
    }

    /** Leave the Backrooms with no reward (admin/self exit command). */
    public static void leave(ServerPlayer player) {
        MinecraftServer server = ((ServerLevel) player.level()).getServer();
        if (server == null) return;
        returnToOverworld(player, server);
        player.sendSystemMessage(Component.literal("§7You step back into the real world."));
    }

    private static void returnToOverworld(ServerPlayer player, MinecraftServer server) {
        // Prefer the server's configured spawn (the mod owns its own spawn point);
        // fall back to a heightmap-safe position at the world origin.
        if (SpawnManager.hasSpawn()) {
            SpawnManager.teleportToSpawn(player);
            return;
        }
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) return;
        BlockPos origin = overworld.getHeightmapPos(
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                BlockPos.ZERO);
        PlayerTeleportHelper.teleport(player, overworld,
                origin.getX() + 0.5, origin.getY(), origin.getZ() + 0.5, 0.0f, 0.0f);
    }

    // ── Admin / info helpers ────────────────────────────────────────────────

    /** Admin regen: forget the cells around the player so they re-stamp next tick. */
    public static int regenAround(ServerPlayer player) {
        if (!isBackrooms(player.level())) return 0;
        int pcx = Math.floorDiv((int) Math.floor(player.getX()), BackroomsGenerator.CELL);
        int pcz = Math.floorDiv((int) Math.floor(player.getZ()), BackroomsGenerator.CELL);
        int cleared = 0;
        for (int dx = -GEN_RADIUS; dx <= GEN_RADIUS; dx++) {
            for (int dz = -GEN_RADIUS; dz <= GEN_RADIUS; dz++) {
                long key = packCell(pcx + dx, pcz + dz);
                if (generatedCells.remove(key)) cleared++;
                exitPortals.remove(key);
                // Wipe the cell so restart-detection re-stamps it fresh.
                if (player.level() instanceof ServerLevel level) {
                    wipeCell(level, pcx + dx, pcz + dz);
                }
            }
        }
        return cleared;
    }

    private static void wipeCell(ServerLevel level, int cx, int cz) {
        int minX = cx * BackroomsGenerator.CELL;
        int minZ = cz * BackroomsGenerator.CELL;
        for (int dx = 0; dx < BackroomsGenerator.CELL; dx++) {
            for (int dz = 0; dz < BackroomsGenerator.CELL; dz++) {
                for (int y = BackroomsGenerator.FLOOR_Y; y <= BackroomsGenerator.CEIL_Y; y++) {
                    level.setBlock(new BlockPos(minX + dx, y, minZ + dz),
                            net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),
                            net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
                }
            }
        }
    }

    /** Nearest known exit portal to a player, or null. */
    public static BlockPos nearestExit(ServerPlayer player) {
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos p : exitPortals.values()) {
            double d = player.distanceToSqr(p.getX() + 0.5, p.getY(), p.getZ() + 0.5);
            if (d < bestDist) { bestDist = d; best = p; }
        }
        return best;
    }

    public static int getGeneratedCellCount() {
        return generatedCells.size();
    }

    public static int getKnownExitCount() {
        return exitPortals.size();
    }
}
