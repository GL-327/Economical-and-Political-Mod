package com.political.backrooms.fracture3;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RoomGraphNode {

    public enum RoomType {
        STANDARD,
        CORRIDOR,
        INFINITE_IMPRESSION,
        CORNER_ACCUMULATION,
        VOID_EDGE
    }

    private final UUID id;
    private final RoomType type;
    private final int x;
    private final int z;
    private final int width;
    private final int length;
    private final List<UUID> connectedNodes;
    private boolean generated;
    private long lastVisitedTime;

    public RoomGraphNode(RoomType type, int x, int z, int width, int length) {
        this.id = UUID.randomUUID();
        this.type = type;
        this.x = x;
        this.z = z;
        this.width = width;
        this.length = length;
        this.connectedNodes = new ArrayList<>();
        this.generated = false;
        this.lastVisitedTime = System.currentTimeMillis();
    }

    private RoomGraphNode(UUID id, RoomType type, int x, int z, int width, int length, boolean generated, long lastVisitedTime) {
        this.id = id;
        this.type = type;
        this.x = x;
        this.z = z;
        this.width = width;
        this.length = length;
        this.connectedNodes = new ArrayList<>();
        this.generated = generated;
        this.lastVisitedTime = lastVisitedTime;
    }

    public void addConnection(UUID nodeId) {
        if (!connectedNodes.contains(nodeId)) {
            connectedNodes.add(nodeId);
        }
    }

    public UUID getId() { return id; }
    public RoomType getType() { return type; }
    public int getX() { return x; }
    public int getZ() { return z; }
    public int getWidth() { return width; }
    public int getLength() { return length; }
    public List<UUID> getConnectedNodes() { return connectedNodes; }
    public boolean isGenerated() { return generated; }
    public void setGenerated(boolean generated) { this.generated = generated; }
    public long getLastVisitedTime() { return lastVisitedTime; }
    public void updateVisitedTime() { this.lastVisitedTime = System.currentTimeMillis(); }
    
    // Check if a point intersects this room (including walls)
    public boolean intersects(int blockX, int blockZ) {
        return blockX >= x && blockX < x + width && blockZ >= z && blockZ < z + length;
    }
    
    // Check if a room boundary intersects with this room's boundary
    public boolean intersects(int rx, int rz, int rWidth, int rLength) {
        return rx < x + width && rx + rWidth > x && rz < z + length && rz + rLength > z;
    }

    public NbtCompound writeNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("Id", id.toString());
        nbt.putString("Type", type.name());
        nbt.putInt("X", x);
        nbt.putInt("Z", z);
        nbt.putInt("Width", width);
        nbt.putInt("Length", length);
        nbt.putBoolean("Generated", generated);
        nbt.putLong("LastVisitedTime", lastVisitedTime);

        NbtList connections = new NbtList();
        for (UUID connectedId : connectedNodes) {
            NbtCompound conn = new NbtCompound();
            conn.putString("TargetId", connectedId.toString());
            connections.add(conn);
        }
        nbt.put("Connections", connections);

        return nbt;
    }

    public static RoomGraphNode readNbt(NbtCompound nbt) {
        UUID id = UUID.fromString(nbt.getString("Id").orElse(UUID.randomUUID().toString()));
        RoomType type = RoomType.valueOf(nbt.getString("Type").orElse("STANDARD"));
        int x = nbt.getInt("X").orElse(0);
        int z = nbt.getInt("Z").orElse(0);
        int width = nbt.getInt("Width").orElse(0);
        int length = nbt.getInt("Length").orElse(0);
        boolean generated = nbt.getBoolean("Generated").orElse(false);
        long lastVisitedTime = nbt.getLong("LastVisitedTime").orElse(0L);

        RoomGraphNode node = new RoomGraphNode(id, type, x, z, width, length, generated, lastVisitedTime);

        try {
            net.minecraft.nbt.NbtElement connectionsElement = nbt.get("Connections");
            if (connectionsElement instanceof NbtList) {
                NbtList connections = (NbtList) connectionsElement;
                for (int i = 0; i < connections.size(); i++) {
                    net.minecraft.nbt.NbtElement connElement = connections.get(i);
                    if (connElement instanceof NbtCompound) {
                        NbtCompound conn = (NbtCompound) connElement;
                        node.addConnection(UUID.fromString(conn.getString("TargetId").orElse("")));
                    }
                }
            }
        } catch (Exception e) {}

        return node;
    }
}
