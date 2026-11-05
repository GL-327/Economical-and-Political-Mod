package com.political;


import com.political.economy.AuctionHouseGui;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.ChatFormatting;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AuctionMasterManager {

    private static final Set<UUID> auctionMasterIds = new HashSet<>();

    public static Villager spawnAuctionMaster(ServerLevel world, double x, double y, double z, float yaw) {
        Villager villager = new Villager(EntityTypes.VILLAGER, world);
        villager.setPos(x, y, z);
        villager.setYRot(yaw);
        villager.setXRot(0f);
        villager.setCustomName(Component.literal("Auction Master").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        villager.setCustomNameVisible(true);
        villager.setPermanentlyInvulnerable(true);
        villager.setNoAi(true);
        villager.setSilent(true);
        villager.setNoGravity(false);
        villager.setPersistenceRequired();

        villager.setHealth(villager.getMaxHealth());
        villager.removeAllEffects();

        villager.setVillagerData(villager.getVillagerData()
                .withProfession(BuiltInRegistries.VILLAGER_PROFESSION.getOrThrow(VillagerProfession.LIBRARIAN))
                .withLevel(5));

        villager.addTag("political_auction_master");
        world.addFreshEntity(villager);
        auctionMasterIds.add(villager.getUUID());

        return villager;
    }

    public static boolean isAuctionMaster(Villager villager) {
        return villager.entityTags().contains("political_auction_master")
                || auctionMasterIds.contains(villager.getUUID());
    }

    public static InteractionResult handleInteraction(Player player, Villager villager) {
        if (!isAuctionMaster(villager)) {
            return InteractionResult.PASS;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            AuctionHouseGui.openFrom(serverPlayer, null);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    public static boolean removeAuctionMaster(ServerLevel world, ServerPlayer player, double radius) {
        AABB searchBox = player.getBoundingBox().inflate(radius);
        List<Villager> found = world.getEntitiesOfClass(Villager.class, searchBox, AuctionMasterManager::isAuctionMaster);

        if (!found.isEmpty()) {
            Villager villager = found.get(0);
            auctionMasterIds.remove(villager.getUUID());
            villager.discard();
            return true;
        }
        return false;
    }

    public static int removeAllAuctionMasters(ServerLevel world) {
        AABB global = new AABB(-30000000, -2048, -30000000, 30000000, 2048, 30000000);
        List<Villager> toRemove = world.getEntitiesOfClass(Villager.class, global, AuctionMasterManager::isAuctionMaster);

        for (Villager villager : toRemove) {
            auctionMasterIds.remove(villager.getUUID());
            villager.discard();
        }

        return toRemove.size();
    }
    public static void registerExistingMaster(UUID uuid) {
        auctionMasterIds.add(uuid);
    }

    public static void clearTrackedMasters() {
        auctionMasterIds.clear();
    }
}