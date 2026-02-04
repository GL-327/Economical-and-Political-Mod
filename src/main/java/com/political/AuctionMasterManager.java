package com.political;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;
import net.minecraft.village.VillagerProfession;
import net.minecraft.registry.Registries;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AuctionMasterManager {

    private static final Set<UUID> auctionMasterIds = new HashSet<>();

    public static VillagerEntity spawnAuctionMaster(ServerWorld world, double x, double y, double z, float yaw) {
        VillagerEntity villager = new VillagerEntity(EntityType.VILLAGER, world);
        villager.refreshPositionAndAngles(x, y, z, yaw, 0);
        villager.setCustomName(Text.literal("Auction Master").formatted(Formatting.GOLD, Formatting.BOLD));
        villager.setCustomNameVisible(true);
        villager.setInvulnerable(true);
        villager.setAiDisabled(true);
        villager.setSilent(true);
        villager.setNoGravity(false);
        villager.setPersistent();

        villager.setHealth(villager.getMaxHealth());
        villager.clearStatusEffects();

        villager.setVillagerData(villager.getVillagerData()
                .withProfession(Registries.VILLAGER_PROFESSION.getOrThrow(VillagerProfession.LIBRARIAN))
                .withLevel(5));

        villager.addCommandTag("auction_master");
        world.spawnEntity(villager);
        auctionMasterIds.add(villager.getUuid());

        return villager;
    }

    public static boolean isAuctionMaster(VillagerEntity villager) {
        return villager.getCommandTags().contains("auction_master")
                || auctionMasterIds.contains(villager.getUuid());
    }

    public static ActionResult handleInteraction(PlayerEntity player, VillagerEntity villager) {
        if (!isAuctionMaster(villager)) {
            return ActionResult.PASS;
        }

        if (player instanceof ServerPlayerEntity serverPlayer) {
            AuctionHouseGui.open(serverPlayer);
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    public static boolean removeAuctionMaster(ServerWorld world, ServerPlayerEntity player, double radius) {
        Box searchBox = player.getBoundingBox().expand(radius);
        List<VillagerEntity> found = new ArrayList<>();

        world.collectEntitiesByType(
                TypeFilter.instanceOf(VillagerEntity.class),
                searchBox,
                AuctionMasterManager::isAuctionMaster,
                found,
                1
        );

        if (!found.isEmpty()) {
            VillagerEntity villager = found.get(0);
            auctionMasterIds.remove(villager.getUuid());
            villager.discard();
            return true;
        }
        return false;
    }

    public static int removeAllAuctionMasters(ServerWorld world) {
        List<VillagerEntity> toRemove = new ArrayList<>();

        world.getEntitiesByType(
                TypeFilter.instanceOf(VillagerEntity.class),
                AuctionMasterManager::isAuctionMaster
        ).forEach(toRemove::add);

        for (VillagerEntity villager : toRemove) {
            auctionMasterIds.remove(villager.getUuid());
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