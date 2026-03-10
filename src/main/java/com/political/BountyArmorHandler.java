package com.political;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Handles tick-based effects for bounty armor pieces that require runtime application.
 * Called every server tick from PoliticalServer.
 */
public class BountyArmorHandler {

    /** Minimum horizontal speed when walking through cobwebs with Spider Leggings. */
    private static final double SPIDER_LEGGINGS_MIN_WALK_SPEED = 0.25;
    /** Maximum downward velocity cap when passing through cobwebs with Spider Leggings. */
    private static final double SPIDER_LEGGINGS_MAX_FALL_SPEED = -0.05;

    public static void tick(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            tickSpiderLeggingsEffects(player);
        }
    }

    /**
     * Apply Spider Leggings runtime effects:
     * - Poison immunity: remove poison effect
     * - Web immunity: cancel cobweb slowdown by restoring velocity
     * - Speed boost: apply Speed II while worn
     */
    private static void tickSpiderLeggingsEffects(ServerPlayerEntity player) {
        ItemStack leggings = player.getEquippedStack(EquipmentSlot.LEGS);
        if (!SlayerItems.isSpiderLeggings(leggings)) return;
        if (!SlayerItems.canUseSpiderLeggings(player)) return;

        // Poison immunity: continuously remove poison
        if (player.hasStatusEffect(StatusEffects.POISON)) {
            player.removeStatusEffect(StatusEffects.POISON);
        }

        // Speed boost: keep Speed II active while wearing spider leggings
        if (!player.hasStatusEffect(StatusEffects.SPEED) ||
                player.getStatusEffect(StatusEffects.SPEED).getAmplifier() < 1) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, 1, false, false, false));
        }

        // Web immunity: if standing in a cobweb, restore velocity to walk through freely
        ServerWorld world = player.getEntityWorld();
        BlockPos pos = player.getBlockPos();
        if (world.getBlockState(pos).isOf(Blocks.COBWEB)) {
            Vec3d vel = player.getVelocity();
            // Restore velocity to at least walking speed so cobweb doesn't trap the player
            double minSpeed = SPIDER_LEGGINGS_MIN_WALK_SPEED;
            double vx = Math.abs(vel.x) < minSpeed && vel.x != 0 ? Math.signum(vel.x) * minSpeed : vel.x;
            double vz = Math.abs(vel.z) < minSpeed && vel.z != 0 ? Math.signum(vel.z) * minSpeed : vel.z;
            double vy = vel.y < SPIDER_LEGGINGS_MAX_FALL_SPEED ? SPIDER_LEGGINGS_MAX_FALL_SPEED : vel.y;
            player.setVelocity(vx, vy, vz);
            player.velocityDirty = true;
        }
    }
}
