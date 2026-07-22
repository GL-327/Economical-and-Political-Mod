package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Verdant / Garden Heart Overalls (leggings, id {@code garden_heart_overalls_t1..t10}).
 *
 * <p>The overalls' lore promises "+X% effective crop drops"; this handler makes that real.
 * When a mature crop is harvested while wearing the overalls, there is a tier-scaled chance
 * to duplicate the crop's drops (higher tiers can roll a second bonus). Wired into the
 * block-break / harvest path alongside {@link GardenHeartEvents}.</p>
 */
public final class GardenHeartOverallsHandler {

    private GardenHeartOverallsHandler() {}

    private static final String PREFIX = "garden_heart_overalls_t";

    /** Returns the equipped overalls tier (1..10), or 0 if not worn. */
    public static int wornTier(ServerPlayer player) {
        ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
        String id = SlayerItems.getCustomItemId(legs);
        if (id == null || !id.startsWith(PREFIX)) return 0;
        try {
            int t = Integer.parseInt(id.substring(PREFIX.length()));
            return Math.max(0, Math.min(10, t));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Called when a mature crop is broken. Grants tier-scaled bonus drops.
     * Safe to call for any block; no-ops unless it's a fully grown crop and the overalls are worn.
     */
    public static void onMatureCropBroken(ServerPlayer player, ServerLevel world, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof CropBlock crop)) return;
        if (crop.getAge(state) < crop.getMaxAge()) return;

        int tier = wornTier(player);
        if (tier <= 0) return;

        // Bonus-drop chance: 5% per tier (t1 = 5% .. t10 = 50%).
        float chance = 0.05f * tier;
        int bonusRolls = 1 + (tier >= 8 ? 1 : 0); // t8+ can roll twice

        int granted = 0;
        for (int roll = 0; roll < bonusRolls; roll++) {
            if (player.getRandom().nextFloat() >= chance) continue;
            var drops = Block.getDrops(state, world, pos, null, player, player.getMainHandItem());
            for (ItemStack drop : drops) {
                if (!drop.isEmpty()) {
                    ItemStack bonus = drop.copy();
                    bonus.setCount(Math.max(1, bonus.getCount()));
                    Block.popResource(world, pos, bonus);
                    granted++;
                    break;
                }
            }
        }

        if (granted > 0) {
            world.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, 0.3, 0.3, 0.3, 0.05);
            player.sendSystemMessage(Component.literal("§aVerdant Bounty!").withStyle(ChatFormatting.GREEN), true);
        }
    }
}
