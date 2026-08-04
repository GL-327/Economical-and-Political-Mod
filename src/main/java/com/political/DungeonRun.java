package com.political;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * One live run through one floor.
 *
 * <p>Everything that ends when the run ends lives here, so cleaning up is dropping this object
 * rather than remembering nine separate maps. The only thing that outlives it is the record of
 * which floors a player has cleared, which belongs to the player and not to the attempt.</p>
 */
public final class DungeonRun {

    public final UUID id = UUID.randomUUID();
    public final DungeonFloor floor;
    public final int slot;
    public final long seed;

    /** Everybody who walked in, whether or not they are still standing. */
    public final List<UUID> party = new ArrayList<>();
    /** Where each of them was before, so leaving puts them back rather than at spawn. */
    public final Map<UUID, ReturnPoint> returns = new HashMap<>();

    public DungeonBuilder.Layout layout;
    public BlockPos entrance;

    public long startedTick;
    public long endedTick;
    public boolean finished;
    public boolean bossSpawned;
    public boolean bossDead;

    /** Score inputs, all counted rather than estimated. */
    public int deaths;
    public int mobsKilled;
    public int roomsCleared;
    public int roomsTotal;
    public int secretsFound;
    public int secretsTotal;

    /** Damage each player did to the boss, for the payout split. */
    public final Map<UUID, Float> bossDamage = new HashMap<>();
    /** Secrets already taken, so one cannot be counted twice. */
    public final Set<Long> takenSecrets = new HashSet<>();
    /** Rooms already reported cleared. */
    public final Set<Long> clearedRooms = new HashSet<>();

    public DungeonRun(DungeonFloor floor, int slot, long seed) {
        this.floor = floor;
        this.slot = slot;
        this.seed = seed;
    }

    /** Where a player came from, so they can be put back exactly. */
    public record ReturnPoint(String dimension, double x, double y, double z, float yaw, float pitch) {
        public static ReturnPoint of(ServerPlayer p) {
            return new ReturnPoint(p.level().dimension().identifier().toString(),
                    p.getX(), p.getY(), p.getZ(), p.getYRot(), p.getXRot());
        }
    }

    public long elapsedTicks(long now) {
        return Math.max(0, (finished ? endedTick : now) - startedTick);
    }

    /** Elapsed time as minutes and seconds, which is how a player reads a run. */
    public String elapsedText(long now) {
        long secs = elapsedTicks(now) / 20;
        return String.format("%d:%02d", secs / 60, secs % 60);
    }

    public boolean contains(ServerPlayer p) {
        return party.contains(p.getUUID());
    }

    /** Every member still online, which is who gets told things. */
    public List<ServerPlayer> online() {
        List<ServerPlayer> out = new ArrayList<>();
        if (PoliticalServer.server == null) return out;
        for (UUID u : party) {
            ServerPlayer p = PoliticalServer.server.getPlayerList().getPlayer(u);
            if (p != null) out.add(p);
        }
        return out;
    }

    /** Whether every room that counts has been cleared, which is what unseals the boss. */
    public boolean roomsDone() {
        return roomsTotal > 0 && roomsCleared >= roomsTotal;
    }
}
