package com.political;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.*;

/**
 * Manages player checkpoints - personal teleport locations.
 * Creation: 100k coins
 * Deletion: 50k refund
 * Name change: 15k coins
 */
public class CheckpointManager {
    
    private static final int CREATION_COST = 100000;
    private static final int DELETION_REFUND = 50000;
    private static final int RENAME_COST = 15000;
    private static final int MAX_CHECKPOINTS = 5;
    
    public static class Checkpoint {
        public String id;
        public String ownerUuid;
        public String name;
        public String worldName;
        public double x, y, z;
        public float yaw, pitch;
        public long createdAt;
        
        public Checkpoint() {} // For GSON
        
        public Checkpoint(String id, String ownerUuid, String name, String worldName, 
                         double x, double y, double z, float yaw, float pitch) {
            this.id = id;
            this.ownerUuid = ownerUuid;
            this.name = name;
            this.worldName = worldName;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
            this.createdAt = System.currentTimeMillis();
        }
    }
    
    public static int getCreationCost() { return CREATION_COST; }
    public static int getDeletionRefund() { return DELETION_REFUND; }
    public static int getRenameCost() { return RENAME_COST; }
    public static int getMaxCheckpoints() { return MAX_CHECKPOINTS; }
    
    public static List<Checkpoint> getPlayerCheckpoints(String playerUuid) {
        DataManager.SaveData data = DataManager.getData();
        List<Checkpoint> result = new ArrayList<>();
        if (data.checkpoints == null) return result;
        for (Checkpoint cp : data.checkpoints) {
            if (cp.ownerUuid.equals(playerUuid)) result.add(cp);
        }
        return result;
    }
    
    public static Checkpoint getCheckpoint(String checkpointId) {
        DataManager.SaveData data = DataManager.getData();
        if (data.checkpoints == null) return null;
        for (Checkpoint cp : data.checkpoints) {
            if (cp.id.equals(checkpointId)) return cp;
        }
        return null;
    }
    
    public static boolean createCheckpoint(ServerPlayerEntity player, String name) {
        String playerUuid = player.getUuidAsString();
        DataManager.SaveData data = DataManager.getData();
        
        if (data.checkpoints == null) data.checkpoints = new ArrayList<>();
        
        // Check max checkpoints
        long count = data.checkpoints.stream().filter(cp -> cp.ownerUuid.equals(playerUuid)).count();
        if (count >= MAX_CHECKPOINTS) {
            player.sendMessage(Text.literal("✗ Maximum checkpoints reached (" + MAX_CHECKPOINTS + ")")
                    .formatted(Formatting.RED));
            return false;
        }
        
        // Check cost
        if (!CoinManager.hasCoins(player, CREATION_COST)) {
            player.sendMessage(Text.literal("✗ You need " + CREATION_COST + " coins to create a checkpoint!")
                    .formatted(Formatting.RED));
            return false;
        }
        
        // Create checkpoint
        String id = UUID.randomUUID().toString().substring(0, 8);
        BlockPos pos = player.getBlockPos();
        Checkpoint cp = new Checkpoint(
            id, playerUuid, name,
            player.getEntityWorld().getRegistryKey().getValue().toString(),
            pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
            player.getYaw(), player.getPitch()
        );
        
        data.checkpoints.add(cp);
        CoinManager.removeCoins(player, CREATION_COST);
        
        player.sendMessage(Text.literal("✓ Checkpoint '").formatted(Formatting.GREEN)
                .append(Text.literal(name).formatted(Formatting.GOLD))
                .append(Text.literal("' created for " + CREATION_COST + " coins!").formatted(Formatting.GREEN)));
        
        DataManager.save(PoliticalServer.server);
        return true;
    }
    
    public static boolean deleteCheckpoint(ServerPlayerEntity player, String checkpointId) {
        String playerUuid = player.getUuidAsString();
        DataManager.SaveData data = DataManager.getData();
        
        if (data.checkpoints == null) {
            player.sendMessage(Text.literal("✗ Checkpoint not found!").formatted(Formatting.RED));
            return false;
        }
        
        Checkpoint toRemove = null;
        for (Checkpoint cp : data.checkpoints) {
            if (cp.id.equals(checkpointId) && cp.ownerUuid.equals(playerUuid)) {
                toRemove = cp;
                break;
            }
        }
        
        if (toRemove == null) {
            player.sendMessage(Text.literal("✗ Checkpoint not found!").formatted(Formatting.RED));
            return false;
        }
        
        data.checkpoints.remove(toRemove);
        CoinManager.giveCoins(player, DELETION_REFUND);
        
        player.sendMessage(Text.literal("✓ Checkpoint deleted. Refunded " + DELETION_REFUND + " coins!")
                .formatted(Formatting.GREEN));
        
        DataManager.save(PoliticalServer.server);
        return true;
    }
    
    public static boolean renameCheckpoint(ServerPlayerEntity player, String checkpointId, String newName) {
        String playerUuid = player.getUuidAsString();
        DataManager.SaveData data = DataManager.getData();
        
        if (data.checkpoints == null) {
            player.sendMessage(Text.literal("✗ Checkpoint not found!").formatted(Formatting.RED));
            return false;
        }
        
        Checkpoint cp = null;
        for (Checkpoint c : data.checkpoints) {
            if (c.id.equals(checkpointId) && c.ownerUuid.equals(playerUuid)) {
                cp = c;
                break;
            }
        }
        
        if (cp == null) {
            player.sendMessage(Text.literal("✗ Checkpoint not found!").formatted(Formatting.RED));
            return false;
        }
        
        if (!CoinManager.hasCoins(player, RENAME_COST)) {
            player.sendMessage(Text.literal("✗ You need " + RENAME_COST + " coins to rename a checkpoint!")
                    .formatted(Formatting.RED));
            return false;
        }
        
        cp.name = newName;
        CoinManager.removeCoins(player, RENAME_COST);
        
        player.sendMessage(Text.literal("✓ Checkpoint renamed to '").formatted(Formatting.GREEN)
                .append(Text.literal(newName).formatted(Formatting.GOLD))
                .append(Text.literal("' for " + RENAME_COST + " coins!").formatted(Formatting.GREEN)));
        
        DataManager.save(PoliticalServer.server);
        return true;
    }
    
    public static boolean teleportToCheckpoint(ServerPlayerEntity player, String checkpointId) {
        DataManager.SaveData data = DataManager.getData();
        
        if (data.checkpoints == null) {
            player.sendMessage(Text.literal("✗ Checkpoint not found!").formatted(Formatting.RED));
            return false;
        }
        
        Checkpoint cp = null;
        for (Checkpoint c : data.checkpoints) {
            if (c.id.equals(checkpointId) && c.ownerUuid.equals(player.getUuidAsString())) {
                cp = c;
                break;
            }
        }
        
        if (cp == null) {
            player.sendMessage(Text.literal("✗ Checkpoint not found!").formatted(Formatting.RED));
            return false;
        }
        
        // Check teleport cost (1000 coins)
        if (CoinManager.getCoins(player) < 1000) {
            player.sendMessage(Text.literal("✗ You need 1000 coins to teleport!").formatted(Formatting.RED));
            return false;
        }
        
        // Deduct teleport cost
        CoinManager.removeCoins(player, 1000);
        
        // Find the world
        for (net.minecraft.server.world.ServerWorld world : PoliticalServer.server.getWorlds()) {
            if (world.getRegistryKey().getValue().toString().equals(cp.worldName)) {
                player.teleport(world, cp.x, cp.y, cp.z, Set.of(), cp.yaw, cp.pitch, false);
                player.sendMessage(Text.literal("✓ Teleported to '").formatted(Formatting.GREEN)
                        .append(Text.literal(cp.name).formatted(Formatting.GOLD))
                        .append(Text.literal("' for 1000 coins!").formatted(Formatting.GREEN)));
                return true;
            }
        }
        
        player.sendMessage(Text.literal("✗ World not found for this checkpoint!").formatted(Formatting.RED));
        return false;
    }
    
    public static void loadFromData(DataManager.SaveData data) {
        // Checkpoints are already loaded as part of SaveData via GSON
    }
    
    public static void saveToData(DataManager.SaveData data) {
        // Checkpoints are already saved as part of SaveData via GSON
    }
}
