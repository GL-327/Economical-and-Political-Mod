package com.political;


import com.political.economy.CoinManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;

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
    
    public static boolean createCheckpoint(ServerPlayer player, String name) {
        String playerUuid = player.getUUID().toString();
        DataManager.SaveData data = DataManager.getData();
        
        if (data.checkpoints == null) data.checkpoints = new ArrayList<>();
        
        // Check max checkpoints
        long count = data.checkpoints.stream().filter(cp -> cp.ownerUuid.equals(playerUuid)).count();
        if (count >= MAX_CHECKPOINTS) {
            player.sendSystemMessage(Component.literal("✗ Maximum checkpoints reached (" + MAX_CHECKPOINTS + ")")
                    .withStyle(ChatFormatting.RED));
            return false;
        }
        
        // Check cost
        if (!CoinManager.hasCoins(player, CREATION_COST)) {
            player.sendSystemMessage(Component.literal("✗ You need " + CREATION_COST + " coins to create a checkpoint!")
                    .withStyle(ChatFormatting.RED));
            return false;
        }
        
        // Create checkpoint
        String id = UUID.randomUUID().toString().substring(0, 8);
        BlockPos pos = player.blockPosition();
        Checkpoint cp = new Checkpoint(
            id, playerUuid, name,
            player.level().dimension().identifier().toString(),
            pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
            player.getYRot(), player.getXRot()
        );
        
        data.checkpoints.add(cp);
        CoinManager.removeCoins(player, CREATION_COST);
        
        player.sendSystemMessage(Component.literal("✓ Checkpoint '").withStyle(ChatFormatting.GREEN)
                .append(Component.literal(name).withStyle(ChatFormatting.GOLD))
                .append(Component.literal("' created for " + CREATION_COST + " coins!").withStyle(ChatFormatting.GREEN)));
        
        DataManager.save(PoliticalServer.server);
        return true;
    }
    
    public static boolean deleteCheckpoint(ServerPlayer player, String checkpointId) {
        String playerUuid = player.getUUID().toString();
        DataManager.SaveData data = DataManager.getData();
        
        if (data.checkpoints == null) {
            player.sendSystemMessage(Component.literal("✗ Checkpoint not found!").withStyle(ChatFormatting.RED));
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
            player.sendSystemMessage(Component.literal("✗ Checkpoint not found!").withStyle(ChatFormatting.RED));
            return false;
        }
        
        data.checkpoints.remove(toRemove);
        CoinManager.giveCoins(player, DELETION_REFUND);
        
        player.sendSystemMessage(Component.literal("✓ Checkpoint deleted. Refunded " + DELETION_REFUND + " coins!")
                .withStyle(ChatFormatting.GREEN));
        
        DataManager.save(PoliticalServer.server);
        return true;
    }
    
    public static boolean renameCheckpoint(ServerPlayer player, String checkpointId, String newName) {
        String playerUuid = player.getUUID().toString();
        DataManager.SaveData data = DataManager.getData();
        
        if (data.checkpoints == null) {
            player.sendSystemMessage(Component.literal("✗ Checkpoint not found!").withStyle(ChatFormatting.RED));
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
            player.sendSystemMessage(Component.literal("✗ Checkpoint not found!").withStyle(ChatFormatting.RED));
            return false;
        }
        
        if (!CoinManager.hasCoins(player, RENAME_COST)) {
            player.sendSystemMessage(Component.literal("✗ You need " + RENAME_COST + " coins to rename a checkpoint!")
                    .withStyle(ChatFormatting.RED));
            return false;
        }
        
        cp.name = newName;
        CoinManager.removeCoins(player, RENAME_COST);
        
        player.sendSystemMessage(Component.literal("✓ Checkpoint renamed to '").withStyle(ChatFormatting.GREEN)
                .append(Component.literal(newName).withStyle(ChatFormatting.GOLD))
                .append(Component.literal("' for " + RENAME_COST + " coins!").withStyle(ChatFormatting.GREEN)));
        
        DataManager.save(PoliticalServer.server);
        return true;
    }
    
    public static boolean teleportToCheckpoint(ServerPlayer player, String checkpointId) {
        DataManager.SaveData data = DataManager.getData();
        
        if (data.checkpoints == null) {
            player.sendSystemMessage(Component.literal("✗ Checkpoint not found!").withStyle(ChatFormatting.RED));
            return false;
        }
        
        Checkpoint cp = null;
        for (Checkpoint c : data.checkpoints) {
            if (c.id.equals(checkpointId) && c.ownerUuid.equals(player.getUUID().toString())) {
                cp = c;
                break;
            }
        }
        
        if (cp == null) {
            player.sendSystemMessage(Component.literal("✗ Checkpoint not found!").withStyle(ChatFormatting.RED));
            return false;
        }
        
        // Check teleport cost (1000 coins)
        if (CoinManager.getCoins(player) < 1000) {
            player.sendSystemMessage(Component.literal("✗ You need 1000 coins to teleport!").withStyle(ChatFormatting.RED));
            return false;
        }
        
        // Deduct teleport cost
        CoinManager.removeCoins(player, 1000);
        
        // Find the world
        for (net.minecraft.server.level.ServerLevel world : PoliticalServer.server.getAllLevels()) {
            if (world.dimension().identifier().toString().equals(cp.worldName)) {
                PlayerTeleportHelper.teleport(player, world, cp.x, cp.y, cp.z, cp.yaw, cp.pitch);
                player.sendSystemMessage(Component.literal("✓ Teleported to '").withStyle(ChatFormatting.GREEN)
                        .append(Component.literal(cp.name).withStyle(ChatFormatting.GOLD))
                        .append(Component.literal("' for 1000 coins!").withStyle(ChatFormatting.GREEN)));
                return true;
            }
        }
        
        player.sendSystemMessage(Component.literal("✗ World not found for this checkpoint!").withStyle(ChatFormatting.RED));
        return false;
    }
    
    public static void loadFromData(DataManager.SaveData data) {
        // Checkpoints are already loaded as part of SaveData via GSON
    }
    
    public static void saveToData(DataManager.SaveData data) {
        // Checkpoints are already saved as part of SaveData via GSON
    }
}
