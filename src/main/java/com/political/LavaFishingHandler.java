package com.political;


import com.political.economy.CoinManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.tags.FluidTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Hypixel-style lava-line fishing: build tension in lava, then roll loot / “sea creatures”.
 */
public final class LavaFishingHandler {

    private static final Map<UUID, Integer> TICKS = new HashMap<>();

    public static void tick(MinecraftServer server) {
        for (ServerLevel world : server.getAllLevels()) {
            for (ServerPlayer player : world.players()) {
                FishingHook bob = player.fishing;
                if (bob == null || !bob.isAlive()) {
                    TICKS.remove(player.getUUID());
                    continue;
                }
                FluidState f = world.getFluidState(bob.blockPosition());
                if (!f.is(FluidTags.LAVA)) {
                    TICKS.remove(player.getUUID());
                    continue;
                }
                int t = TICKS.merge(player.getUUID(), 1, Integer::sum);
                int need = 55 - Math.min(25, AscensionHeartSystem.tidalTier(player.getUUID().toString()) * 2);
                if (t < need) {
                    continue;
                }
                TICKS.put(player.getUUID(), 0);
                rollCatch(world, player, bob);
            }
        }
    }

    private static void rollCatch(ServerLevel world, ServerPlayer player, FishingHook bob) {
        double tidal = AscensionHeartSystem.tidalPotency(player.getUUID().toString());
        float r = player.getRandom().nextFloat();
        if (r < 0.08f * (float) tidal) {
            Silverfish leech = EntityTypes.SILVERFISH.create(world, EntitySpawnReason.TRIGGERED);
            if (leech != null) {
                leech.teleportTo(bob.getX(), bob.getY(), bob.getZ());
                leech.setCustomName(Component.literal("Scoria Leech").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                leech.setCustomNameVisible(true);
                world.addFreshEntity(leech);
                player.sendSystemMessage(Component.literal("§cA lava creature surfaces!"), true);
            }
            return;
        }
        if (r < 0.45f) {
            CoinManager.giveCoinsQuiet(player, 500 + player.getRandom().nextInt(2500));
            player.sendSystemMessage(Component.literal("§6Molten treasure!"), true);
            return;
        }
        player.addItem(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.PRISMARINE_SHARD, 1 + player.getRandom().nextInt(3)));
        player.sendSystemMessage(Component.literal("§bYou hooked something crystalline…"), true);
    }
}
