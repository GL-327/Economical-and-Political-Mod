package com.political;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

/**
 * Builds every floor once and reports what happened.
 *
 * <p>A dungeon that loads is not a dungeon that works. Room templates can be present and still fail
 * to place, a floor can be measured wrong and leave a room with no walkable surface, and a corridor
 * can be carved between two rooms that never actually meet. None of that shows up on a boot log,
 * and all of it shows up the moment somebody walks in.</p>
 *
 * <p>So this lays out all seven floors in a scratch area, counts what came out, and throws it away.
 * Off unless {@code -Dpolitical.dungeonSelfTest=true} is set, because a server owner should not pay
 * seven floor builds at every startup for a check that only matters when the code changed.</p>
 */
public final class DungeonSelfTest {

    private DungeonSelfTest() {}

    /** Far from any real instance slot, so a test build can never land on a live run. */
    private static final int TEST_ORIGIN_X = -2_000_000;

    public static void runIfAsked(MinecraftServer server) {
        if (!"true".equals(System.getProperty("political.dungeonSelfTest"))) return;

        ServerLevel level = DungeonManager.realm(server);
        if (level == null) {
            PoliticalServer.LOGGER.error("Dungeon self test: the realm is missing");
            return;
        }

        int floorsOk = 0;
        int totalRooms = 0;
        int totalFailed = 0;
        int noFloor = 0;

        for (DungeonFloor floor : DungeonFloor.values()) {
            int want = floor.roomCount();
            BlockPos at = new BlockPos(TEST_ORIGIN_X + floor.tier * 4096, 0, 0);
            long started = System.nanoTime();

            DungeonBuilder.Layout layout;
            try {
                layout = DungeonBuilder.build(server, level, floor, at, 12345L + floor.tier);
            } catch (Exception e) {
                PoliticalServer.LOGGER.error("Dungeon self test: " + floor.name()
                        + " threw " + e.getClass().getSimpleName() + " " + e.getMessage());
                totalFailed++;
                continue;
            }

            int placed = layout.rooms.size();
            int flat = 0;
            for (DungeonBuilder.Placed p : layout.rooms) {
                // Only a genuine fallback counts. A room whose bottom layer is its floor measures
                // its own base and is perfectly correct, and counting those was hiding the signal.
                if (!p.floorMeasured) flat++;
            }
            noFloor += flat;
            totalRooms += placed;
            if (placed >= Math.max(4, want / 2)) floorsOk++;

            long ms = (System.nanoTime() - started) / 1_000_000L;
            PoliticalServer.LOGGER.info(String.format(
                    "Dungeon self test: %-16s %2d/%2d rooms, %d unmeasurable, boss=%s, %d ms",
                    floor.display, placed, want, flat,
                    layout.bossRoom != null ? "yes" : "NO", ms));
        }

        PoliticalServer.LOGGER.info("Dungeon self test: " + floorsOk + "/"
                + DungeonFloor.values().length + " floors built, " + totalRooms
                + " rooms placed, " + totalFailed + " floors threw, "
                + noFloor + " rooms with no surface anywhere");
    }
}
