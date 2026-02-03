package com.political;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.village.VillagerProfession;
import net.minecraft.registry.Registries;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AuctionMasterManager {

    // Track auction master villager UUIDs
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

        // Additional protection - prevent despawning and make truly immortal
        villager.setHealth(villager.getMaxHealth());
        villager.clearStatusEffects(); // Prevent poison/wither killing

        villager.setVillagerData(villager.getVillagerData()
                .withProfession(Registries.VILLAGER_PROFESSION.getOrThrow(VillagerProfession.LIBRARIAN))
                .withLevel(5));

        villager.addCommandTag("auction_master");

        world.spawnEntity(villager);
        auctionMasterIds.add(villager.getUuid());

        return villager;
    }

    public static boolean isAuctionMaster(VillagerEntity villager) {
        return villager.getCommandTags().contains("auction_master") ||
                auctionMasterIds.contains(villager.getUuid());
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

    public static void registerExistingMaster(UUID uuid) {
        auctionMasterIds.add(uuid);
    }

    public static void clearTrackedMasters() {
        auctionMasterIds.clear();
    }
}