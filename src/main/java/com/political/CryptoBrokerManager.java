package com.political;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.VillagerProfession;
import net.minecraft.registry.Registries;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Crypto Broker NPC - spawns a villager that provides access to the crypto market.
 */
public class CryptoBrokerManager {

    private static final Set<UUID> cryptoBrokerIds = new HashSet<>();

    public static VillagerEntity spawnCryptoBroker(ServerWorld world, double x, double y, double z, float yaw) {
        VillagerEntity villager = new VillagerEntity(EntityType.VILLAGER, world);
        villager.refreshPositionAndAngles(x, y, z, yaw, 0);
        villager.setCustomName(Text.literal("🔮 Crypto Broker").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        villager.setCustomNameVisible(true);
        villager.setInvulnerable(true);
        villager.setAiDisabled(true);
        villager.setSilent(true);
        villager.setNoGravity(false);
        villager.setPersistent();

        villager.setHealth(villager.getMaxHealth());
        villager.clearStatusEffects();

        // Set villager data
        villager.setVillagerData(villager.getVillagerData()
                .withProfession(Registries.VILLAGER_PROFESSION.getOrThrow(VillagerProfession.CLERIC))
                .withLevel(10));

        villager.addCommandTag("crypto_broker");
        world.spawnEntity(villager);
        cryptoBrokerIds.add(villager.getUuid());

        return villager;
    }

    public static boolean isCryptoBroker(VillagerEntity villager) {
        return villager != null && villager.getCommandTags().contains("crypto_broker");
    }

    public static int removeAllCryptoBrokers(ServerWorld world) {
        int removed = 0;
        double range = 100.0;
        BlockPos center = new BlockPos(0, 0, 0);
        
        for (UUID brokerId : new java.util.ArrayList<>(cryptoBrokerIds)) {
            for (VillagerEntity villager : world.getEntitiesByClass(VillagerEntity.class, 
                    new net.minecraft.util.math.Box(
                        center.getX() - range, center.getY() - range, center.getZ() - range,
                        center.getX() + range, center.getY() + range, center.getZ() + range
                    ), null)) {
                if (villager.getUuid().equals(brokerId) && isCryptoBroker(villager)) {
                    villager.discard();
                    removed++;
                    break;
                }
            }
        }
        
        cryptoBrokerIds.clear();
        return removed;
    }

    // Handle player interaction with Crypto Broker
    public static ActionResult handleInteraction(ServerPlayerEntity player, VillagerEntity villager) {
        if (!isCryptoBroker(villager)) {
            return ActionResult.PASS;
        }

        // Open crypto market GUI
        CryptoMarketGui.openMainMenu(player);
        return ActionResult.SUCCESS;
    }
}
