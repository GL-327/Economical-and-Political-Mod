package com.political;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Crypto Broker NPC - spawns a villager that provides access to the crypto market.
 */
public class CryptoBrokerManager {

    private static final Set<UUID> cryptoBrokerIds = new HashSet<>();

    public static Villager spawnCryptoBroker(ServerLevel world, double x, double y, double z, float yaw) {
        Villager villager = new Villager(EntityTypes.VILLAGER, world);
        villager.setPos(x, y, z);
        villager.setYRot(yaw);
        villager.setXRot(0f);
        villager.setCustomName(Component.literal("🔮 Crypto Broker").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        villager.setCustomNameVisible(true);
        villager.setPermanentlyInvulnerable(true);
        villager.setNoAi(true);
        villager.setSilent(true);
        villager.setNoGravity(false);
        villager.setPersistenceRequired();

        villager.setHealth(villager.getMaxHealth());
        villager.removeAllEffects();

        // Set villager data
        villager.setVillagerData(villager.getVillagerData()
                .withProfession(BuiltInRegistries.VILLAGER_PROFESSION.getOrThrow(VillagerProfession.CLERIC))
                .withLevel(10));

        villager.addTag("political_crypto_broker");
        world.addFreshEntity(villager);
        cryptoBrokerIds.add(villager.getUUID());

        return villager;
    }

    public static boolean isCryptoBroker(Villager villager) {
        return villager != null && villager.entityTags().contains("political_crypto_broker");
    }

    public static int removeAllCryptoBrokers(ServerLevel world) {
        int removed = 0;
        double range = 100.0;
        BlockPos center = new BlockPos(0, 0, 0);
        
        for (UUID brokerId : new java.util.ArrayList<>(cryptoBrokerIds)) {
            for (Villager villager : world.getEntitiesOfClass(Villager.class, 
                    new net.minecraft.world.phys.AABB(
                        center.getX() - range, center.getY() - range, center.getZ() - range,
                        center.getX() + range, center.getY() + range, center.getZ() + range
                    ), null)) {
                if (villager.getUUID().equals(brokerId) && isCryptoBroker(villager)) {
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
    public static InteractionResult handleInteraction(ServerPlayer player, Villager villager) {
        if (!isCryptoBroker(villager)) {
            return InteractionResult.PASS;
        }

        // Open crypto market GUI
        CryptoMarketGui.openMainMenu(player);
        return InteractionResult.SUCCESS;
    }
}
