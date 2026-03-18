package com.political;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.VillagerProfession;
import net.minecraft.registry.Registries;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class StockBrokerManager {

    private static final Set<UUID> stockBrokerIds = new HashSet<>();

    public static VillagerEntity spawnStockBroker(ServerWorld world, double x, double y, double z, float yaw) {
        VillagerEntity villager = new VillagerEntity(EntityType.VILLAGER, world);
        villager.refreshPositionAndAngles(x, y, z, yaw, 0);
        villager.setCustomName(Text.literal("Stock Broker").formatted(Formatting.DARK_BLUE, Formatting.BOLD));
        villager.setCustomNameVisible(true);
        villager.setInvulnerable(true);
        villager.setAiDisabled(true);
        villager.setSilent(true);
        villager.setNoGravity(false);
        villager.setPersistent();

        villager.setHealth(villager.getMaxHealth());
        villager.clearStatusEffects();

        // Set villager data for stock broker
        villager.setVillagerData(villager.getVillagerData()
                .withProfession(Registries.VILLAGER_PROFESSION.getOrThrow(VillagerProfession.CLERIC))
                .withLevel(10));

        villager.addCommandTag("stock_broker");
        world.spawnEntity(villager);
        stockBrokerIds.add(villager.getUuid());

        return villager;
    }

    public static boolean isStockBroker(VillagerEntity villager) {
        return villager != null && villager.getCommandTags().contains("stock_broker");
    }

    // Temporarily disabled for testing
/*
    public static boolean removeStockBroker(ServerWorld world, ServerPlayerEntity player, double range) {
        boolean removed = false;
        
        for (UUID brokerId : new ArrayList<>(stockBrokerIds)) {
            // Find the broker entity
            VillagerEntity broker = null;
            BlockPos playerPos = player.getBlockPos();
            for (VillagerEntity villager : world.getEntitiesByClass(VillagerEntity.class, 
                    new Box(player.getBlockPos().add(-range, -range, -range), 
                          player.getBlockPos().add(range, range, range), 
                          v -> v.getUuid().equals(brokerId)))) {
                if (isStockBroker(villager)) {
                    broker = villager;
                    break;
                }
            }
            
            if (broker != null) {
                broker.discard();
                stockBrokerIds.remove(brokerId);
                removed = true;
            }
        }
        
        return removed;
    }
*/

    public static boolean removeStockBroker(ServerWorld world, ServerPlayerEntity player, double range) {
        // Simple implementation for now
        return false;
    }

    public static int removeAllStockBrokers(ServerWorld world) {
        int removed = 0;
        double range = 100.0;
        BlockPos center = new BlockPos(0, 0, 0);
        
        for (UUID brokerId : new ArrayList<>(stockBrokerIds)) {
            VillagerEntity broker = null;
            Box searchArea = new Box(
            center.getX() - range, center.getY() - range, center.getZ() - range,
            center.getX() + range, center.getY() + range, center.getZ() + range
        );
            
            for (VillagerEntity villager : world.getEntitiesByClass(VillagerEntity.class, searchArea, null)) {
                if (villager.getUuid().equals(brokerId)) {
                    if (isStockBroker(villager)) {
                        broker = villager;
                        break;
                    }
                }
            }
            
            if (broker != null) {
                broker.discard();
                removed++;
            }
        }
        
        stockBrokerIds.clear();
        return removed;
    }

    // Handle player interaction with Stock Broker
    public static ActionResult handleInteraction(ServerPlayerEntity player, VillagerEntity villager) {
        if (!isStockBroker(villager)) {
            return ActionResult.PASS;
        }

        // Open stock market GUI
        StockMarketGui.openMainMenu(player);
        return ActionResult.SUCCESS;
    }
}
