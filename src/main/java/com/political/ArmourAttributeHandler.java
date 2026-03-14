package com.political;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Blocks;
import net.minecraft.world.World;
import net.minecraft.registry.RegistryKey;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArmourAttributeHandler {
    private static final Map<UUID, Integer> tickCounters = new HashMap<>();

    public static void tick(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        int ticks = tickCounters.getOrDefault(uuid, 0) + 1;
        tickCounters.put(uuid, ticks);

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot != EquipmentSlot.HEAD && slot != EquipmentSlot.CHEST && 
                slot != EquipmentSlot.LEGS && slot != EquipmentSlot.FEET) continue;
            ItemStack stack = player.getEquippedStack(slot);
            ArmourAttribute attr = SlayerItems.getAppliedAttribute(stack);
            if (attr == null) continue;

            // Handle attribute effects
            handleAttributeEffect(player, attr, ticks);
        }
    }

    private static void handleAttributeEffect(ServerPlayerEntity player, ArmourAttribute attr, int ticks) {
        switch (attr) {
            case BURNING -> {
                if (player.isOnFire()) {
                    // Fire damage handled by mixin or implicitly
                    // Force fire duration to stay high
                    player.setFireTicks(200);
                }
            }
            case SIGHTLESS -> {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 220, 0, false, false, true));
                if (player.hasStatusEffect(StatusEffects.BLINDNESS)) {
                    player.removeStatusEffect(StatusEffects.BLINDNESS);
                }
            }
            case FRENZIED -> {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 40, 0, false, false, true)); // Mimics attack speed partially
                if (ticks % 100 == 0) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 120, 0, false, false, true));
                }
            }
            case GROUNDED -> {
                // Lightning immunity handled by mixin
                // Jump reduction handled by mixin
            }
            case WEBBED -> {
                // Slowed on grass/leaves
                BlockPos pos = player.getBlockPos().down();
                if (player.getEntityWorld().getBlockState(pos).isOf(Blocks.GRASS_BLOCK) || 
                    player.getEntityWorld().getBlockState(pos).isOf(Blocks.OAK_LEAVES)) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 10, 0, false, false, true));
                }
            }
            case FROST -> {
                // Check if in Nether-like dimension (hot dimension)
                if (player.getEntityWorld().getDimension().hasCeiling()) { // Nether-like
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 1, false, false, true));
                }
                // Frost Walker logic (partial)
                if (player.isOnGround()) {
                    BlockPos p = player.getBlockPos().down();
                    if (player.getEntityWorld().getBlockState(p).isOf(Blocks.WATER)) {
                        player.getEntityWorld().setBlockState(p, Blocks.FROSTED_ICE.getDefaultState());
                    }
                }
            }
            case PHANTOMSTEP -> {
                // Stealth handled by mixin (noise)
            }
            case CURSED -> {
                // Max HP is handled via Attribute modifiers on application (ideally)
            }
            case OVERGROWN -> {
                if (ticks % 40 == 0 && !player.isSprinting() && isNaturalBlock(player.getEntityWorld(), player.getBlockPos().down())) {
                    player.heal(1.0f);
                }
                if (player.getEntityWorld().getRegistryKey() == World.NETHER || player.getEntityWorld().getRegistryKey() == World.END) {
                    // Decay logic?
                }
            }
            case VOLATILE -> {
                if (player.getHealth() <= 4.0f && ticks % 100 == 0) {
                    player.getEntityWorld().createExplosion(player, player.getX(), player.getY(), player.getZ(), 2.0f, World.ExplosionSourceType.NONE);
                }
            }
        }
    }

    private static boolean isNaturalBlock(World world, BlockPos pos) {
        return world.getBlockState(pos).isOf(Blocks.GRASS_BLOCK) || 
               world.getBlockState(pos).isOf(Blocks.DIRT) || 
               world.getBlockState(pos).isOf(Blocks.MOSS_BLOCK);
    }
}
