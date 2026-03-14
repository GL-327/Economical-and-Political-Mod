package com.political.backrooms.fracture3;

import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TerminalLayout
 * Manages the procedural node graph of rooms within the Fracture 3 dimension.
 */
public class TerminalLayout {

    private static final ConcurrentHashMap<UUID, RoomGraphNode> nodeRegistry = new ConcurrentHashMap<>();
    private static final List<RoomGraphNode> spatialIndex = new ArrayList<>();
    
    // Ensures thread safety for graph generation
    private static final Object GRAPH_LOCK = new Object();

    public static void clearGraph() {
        synchronized(GRAPH_LOCK) {
            nodeRegistry.clear();
            spatialIndex.clear();
        }
    }

    public static RoomGraphNode addNode(RoomGraphNode node) {
        synchronized(GRAPH_LOCK) {
            nodeRegistry.put(node.getId(), node);
            spatialIndex.add(node);
            return node;
        }
    }

    public static RoomGraphNode getNode(UUID id) {
        return nodeRegistry.get(id);
    }

    public static List<RoomGraphNode> getAllNodes() {
        return new ArrayList<>(spatialIndex);
    }
    
    public static Optional<RoomGraphNode> findNodeAt(int x, int z) {
        synchronized(GRAPH_LOCK) {
            for (RoomGraphNode node : spatialIndex) {
                if (node.intersects(x, z)) {
                    return Optional.of(node);
                }
            }
            return Optional.empty();
        }
    }
    
    public static boolean hasIntersection(int x, int z, int width, int length) {
        synchronized(GRAPH_LOCK) {
            for (RoomGraphNode node : spatialIndex) {
                if (node.intersects(x, z, width, length)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Finds rooms close to the given coordinate to constrain the generator logic.
     */
    public static List<RoomGraphNode> getNodesInRegion(int minX, int minZ, int maxX, int maxZ) {
        List<RoomGraphNode> result = new ArrayList<>();
        synchronized(GRAPH_LOCK) {
            for (RoomGraphNode node : spatialIndex) {
                if (node.intersects(minX, minZ, maxX - minX, maxZ - minZ)) {
                    result.add(node);
                }
            }
        }
        return result;
    }
}
