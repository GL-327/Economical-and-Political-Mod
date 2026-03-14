package com.political.backrooms.fracture3;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.UUID;

public class TerminalLayoutGenerator {

    private static final int WEIGHT_STANDARD = 45;
    private static final int WEIGHT_CORRIDOR = 30;
    private static final int WEIGHT_INFINITE = 15;
    private static final int WEIGHT_CORNER = 7;
    private static final int WEIGHT_VOID_EDGE = 3;

    private static class PendingExit {
        public UUID sourceNodeId;
        public int dir; // 0=N, 1=S, 2=W, 3=E
        public int x, z;
        public PendingExit(UUID source, int dir, int x, int z) {
            this.sourceNodeId = source;
            this.dir = dir;
            this.x = x;
            this.z = z;
        }
    }

    private static final Queue<PendingExit> pendingExits = new LinkedList<>();
    private static boolean originGenerated = false;
    private static long currentSeed = 0;

    public static void ensureGraphExists(long worldSeed, int targetX, int targetZ, int radius) {
        if (!originGenerated) {
            currentSeed = worldSeed;
            // Step 1: Place entry room (Standard 12x12 at 0, 64, 0)
            RoomGraphNode entry = new RoomGraphNode(RoomGraphNode.RoomType.STANDARD, 0, 0, 12, 12);
            TerminalLayout.addNode(entry);
            originGenerated = true;
            
            enqueueExits(entry);
        }

        // Process queue until we have generated enough nodes covering the target area
        // To prevent infinite loops in one go, limit max steps per call.
        int steps = 0;
        int maxSteps = 1000;

        while (!pendingExits.isEmpty() && steps < maxSteps) {
            // Optimization: if all pending exits are far outside the target radius + 100 blocks, we can break or skip
            // For now, we expand outward breath-first
            PendingExit exit = pendingExits.poll();
            steps++;

            expandNode(exit);
        }
    }

    private static void enqueueExits(RoomGraphNode node) {
        Random rand = new Random(currentSeed ^ (long)(node.getX() * 341873128712L + node.getZ() * 132897987541L));
        // Step 2: Roll number of exits 1-4
        int numExits = 1 + rand.nextInt(4);
        if (rand.nextInt(100) < 50) numExits = Math.max(2, numExits); // weighted to 2-3

        for (int i = 0; i < numExits; i++) {
            // Step 3: Random direction
            int dir = rand.nextInt(4);
            int startX = node.getX() + node.getWidth() / 2;
            int startZ = node.getZ() + node.getLength() / 2;
            
            // Adjust start point to edge of room
            if (dir == 0) startZ = node.getZ() - 2; // North
            if (dir == 1) startZ = node.getZ() + node.getLength(); // South
            if (dir == 2) startX = node.getX() - 2; // West
            if (dir == 3) startX = node.getX() + node.getWidth(); // East

            pendingExits.add(new PendingExit(node.getId(), dir, startX, startZ));
        }
    }

    private static void expandNode(PendingExit exit) {
        RoomGraphNode source = TerminalLayout.getNode(exit.sourceNodeId);
        if (source == null) return;
        
        Random rand = new Random(currentSeed ^ (long)(exit.x * 341873128712L + exit.z * 132897987541L));
        
        // 8% chance to loop back to an existing nearby room
        if (rand.nextInt(100) < 8) {
            // Find a nearby node
            for (RoomGraphNode node : TerminalLayout.getAllNodes()) {
                if (node.getId().equals(source.getId())) continue;
                int dx = node.getX() - exit.x;
                int dz = node.getZ() - exit.z;
                if (dx * dx + dz * dz < 2000) { // arbitrary connection distance
                    source.addConnection(node.getId());
                    node.addConnection(source.getId());
                    return;
                }
            }
        }

        // Step 4: Place Long Corridor in direction
        int corridorLength = 16 + (rand.nextInt(7) * 8); // 16 to 64
        int cWidth = (exit.dir >= 2) ? corridorLength : 4;
        int cLength = (exit.dir < 2) ? corridorLength : 4;

        int cx = exit.x;
        int cz = exit.z;
        
        if (exit.dir == 0) cz -= corridorLength; // North
        if (exit.dir == 2) cx -= corridorLength; // West

        // Check intersection
        if (TerminalLayout.hasIntersection(cx, cz, cWidth, cLength)) {
            return; // Abort if blocked
        }

        RoomGraphNode corridor = new RoomGraphNode(RoomGraphNode.RoomType.CORRIDOR, cx, cz, cWidth, cLength);
        TerminalLayout.addNode(corridor);
        source.addConnection(corridor.getId());
        corridor.addConnection(source.getId());

        // Step 5: End of corridor, place next room
        int nextX = cx + (exit.dir == 3 ? cWidth : (exit.dir == 2 ? -1 : 0));
        int nextZ = cz + (exit.dir == 1 ? cLength : (exit.dir == 0 ? -1 : 0));
        
        if (exit.dir < 2) nextX = cx + 2; else nextZ = cz + 2; // center alignment

        RoomGraphNode.RoomType nextType = getNextRoomType(rand);
        
        int rWidth = 12; int rLength = 12;
        if (nextType == RoomGraphNode.RoomType.STANDARD) {
            int[][] sizes = {{8,8}, {8,12}, {12,12}, {12,16}, {16,16}};
            int[] s = sizes[rand.nextInt(sizes.length)];
            rWidth = s[0]; rLength = s[1];
        } else if (nextType == RoomGraphNode.RoomType.INFINITE_IMPRESSION) {
            rWidth = rand.nextBoolean() ? 24 : 32;
            rLength = rWidth;
        } else if (nextType == RoomGraphNode.RoomType.CORNER_ACCUMULATION) {
            rWidth = 10 + rand.nextInt(5);
            rLength = 10 + rand.nextInt(5);
        } else if (nextType == RoomGraphNode.RoomType.VOID_EDGE) {
            rWidth = 16; rLength = 16;
        } else {
            // fallback (Corridor shouldn't be picked here as normal room)
            rWidth = 12; rLength = 12;
        }

        int rx = nextX - rWidth / 2;
        int rz = nextZ - rLength / 2;

        if (exit.dir == 0) rz = cz - rLength;
        if (exit.dir == 1) rz = cz + cLength;
        if (exit.dir == 2) rx = cx - rWidth;
        if (exit.dir == 3) rx = cx + cWidth;

        if (TerminalLayout.hasIntersection(rx, rz, rWidth, rLength)) {
            return; // blocked
        }

        RoomGraphNode nextRoom = new RoomGraphNode(nextType, rx, rz, rWidth, rLength);
        TerminalLayout.addNode(nextRoom);
        corridor.addConnection(nextRoom.getId());
        nextRoom.addConnection(corridor.getId());

        // Step 7: Recurse
        enqueueExits(nextRoom);
    }
    
    private static RoomGraphNode.RoomType getNextRoomType(Random rand) {
        int roll = rand.nextInt(100);
        if (roll < WEIGHT_STANDARD) return RoomGraphNode.RoomType.STANDARD;
        if (roll < WEIGHT_STANDARD + WEIGHT_INFINITE) return RoomGraphNode.RoomType.INFINITE_IMPRESSION;
        if (roll < WEIGHT_STANDARD + WEIGHT_INFINITE + WEIGHT_CORNER) return RoomGraphNode.RoomType.CORNER_ACCUMULATION;
        if (roll < WEIGHT_STANDARD + WEIGHT_INFINITE + WEIGHT_CORNER + WEIGHT_VOID_EDGE) return RoomGraphNode.RoomType.VOID_EDGE;
        return RoomGraphNode.RoomType.STANDARD; // fallback
    }
}
