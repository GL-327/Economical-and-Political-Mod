package com.political;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class StockBrokerManager {

    private static final Set<UUID> stockBrokerIds = new HashSet<>();

    public static Villager spawnStockBroker(ServerLevel world, double x, double y, double z, float yaw) {
        Villager villager = new Villager(EntityType.VILLAGER, world);
        villager.setPos(x, y, z);
        villager.setYRot(yaw);
        villager.setXRot(0f);
        villager.setCustomName(Component.literal("Stock Broker").withStyle(ChatFormatting.DARK_BLUE, ChatFormatting.BOLD));
        villager.setCustomNameVisible(true);
        villager.setInvulnerable(true);
        villager.setNoAi(true);
        villager.setSilent(true);
        villager.setNoGravity(false);
        villager.setPersistenceRequired();

        villager.setHealth(villager.getMaxHealth());
        villager.removeAllEffects();

        // Set villager data for stock broker
        villager.setVillagerData(villager.getVillagerData()
                .withProfession(BuiltInRegistries.VILLAGER_PROFESSION.getOrThrow(VillagerProfession.CLERIC))
                .withLevel(10));

        villager.addTag("political_stock_broker");
        world.addFreshEntity(villager);
        stockBrokerIds.add(villager.getUUID());

        return villager;
    }

    public static boolean isStockBroker(Villager villager) {
        return villager != null && villager.entityTags().contains("political_stock_broker");
    }

    // Temporarily disabled for testing
/*
    public static boolean removeStockBroker(ServerLevel world, ServerPlayer player, double range) {
        boolean removed = false;
        
        for (UUID brokerId : new ArrayList<>(stockBrokerIds)) {
            // Find the broker entity
            Villager broker = null;
            BlockPos playerPos = player.blockPosition();
            for (Villager villager : world.getEntitiesOfClass(Villager.class, 
                    new AABB(player.blockPosition().offset(-range, -range, -range), 
                          player.blockPosition().offset(range, range, range), 
                          v -> v.getUUID().equals(brokerId)))) {
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

    public static boolean removeStockBroker(ServerLevel world, ServerPlayer player, double range) {
        // Simple implementation for now
        return false;
    }

    public static int removeAllStockBrokers(ServerLevel world) {
        int removed = 0;
        double range = 100.0;
        BlockPos center = new BlockPos(0, 0, 0);
        
        for (UUID brokerId : new ArrayList<>(stockBrokerIds)) {
            Villager broker = null;
            AABB searchArea = new AABB(
            center.getX() - range, center.getY() - range, center.getZ() - range,
            center.getX() + range, center.getY() + range, center.getZ() + range
        );
            
            for (Villager villager : world.getEntitiesOfClass(Villager.class, searchArea, null)) {
                if (villager.getUUID().equals(brokerId)) {
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
    public static InteractionResult handleInteraction(ServerPlayer player, Villager villager) {
        if (!isStockBroker(villager)) {
            return InteractionResult.PASS;
        }

        // Open stock market GUI
        StockMarketGui.openMainMenu(player);
        return InteractionResult.SUCCESS;
    }
}
