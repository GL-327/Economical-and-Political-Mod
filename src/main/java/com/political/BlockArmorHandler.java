package com.political;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Handles block armor set bonuses - applies effects when wearing full sets.
 */
public class BlockArmorHandler {
    
    /**
     * Called every tick for each player wearing block armor.
     */
    public static void tick(ServerPlayerEntity player) {
        // Check each armor set for full set bonuses
        tickObsidianArmor(player);
        tickGlowstoneArmor(player);
        tickRedstoneArmor(player);
        tickNetherrackArmor(player);
        tickEndstoneArmor(player);
        tickIceArmor(player);
        tickPrismarineArmor(player);
        tickQuartzArmor(player);
        tickGlassArmor(player);
        tickTerracottaArmor(player);
        tickMossyArmor(player);
        tickSoulSandArmor(player);
        tickMagmaArmor(player);
        tickSandstoneArmor(player);
        tickAmethystArmor(player);
        tickCoalArmor(player);
        tickWarpedArmor(player);
        tickWetSpongeArmor(player);
    }
    
    // ============================================================
    // OBSIDIAN ARMOR - Blast resistance & Fire immunity
    // ============================================================
    private static void tickObsidianArmor(ServerPlayerEntity player) {
        if (BlockArmor.hasFullBlockArmorSet(player, "obsidian")) {
            // Fire resistance
            if (!player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE) || 
                player.getStatusEffect(StatusEffects.FIRE_RESISTANCE).getDuration() < 200) {
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.FIRE_RESISTANCE, 400, 0, true, false, false));
            }
            // Resistance (blast resistance simulation)
            if (!player.hasStatusEffect(StatusEffects.RESISTANCE) || 
                player.getStatusEffect(StatusEffects.RESISTANCE).getDuration() < 200) {
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.RESISTANCE, 400, 1, true, false, false));
            }
        }
    }
    
    // ============================================================
    // GLOWSTONE ARMOR - Light emission & Night Vision
    // ============================================================
    private static void tickGlowstoneArmor(ServerPlayerEntity player) {
        if (BlockArmor.hasFullBlockArmorSet(player, "glowstone")) {
            // Night vision
            if (!player.hasStatusEffect(StatusEffects.NIGHT_VISION) || 
                player.getStatusEffect(StatusEffects.NIGHT_VISION) == null ||
                player.getStatusEffect(StatusEffects.NIGHT_VISION).getDuration() < 200) {
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.NIGHT_VISION, 400, 0, true, false, false));
            }
            // Glowing effect on nearby enemies (simulates light emission)
            if (player.age % 40 == 0) {
                for (var entity : player.getEntityWorld().getEntitiesByClass(
                    net.minecraft.entity.LivingEntity.class,
                    player.getBoundingBox().expand(8.0),
                    e -> e != player && e.isAlive())) {
                    entity.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.GLOWING, 60, 0, true, false, false));
                }
            }
        }
    }
    
    // ============================================================
    // REDSTONE ARMOR - Haste II
    // ============================================================
    private static void tickRedstoneArmor(ServerPlayerEntity player) {
        if (BlockArmor.hasFullBlockArmorSet(player, "redstone")) {
            if (!player.hasStatusEffect(StatusEffects.HASTE) || 
                player.getStatusEffect(StatusEffects.HASTE).getDuration() < 200) {
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.HASTE, 400, 1, true, false, false));
            }
        }
    }
    
    // ============================================================
    // NETHERRACK ARMOR - Fire Resistance
    // ============================================================
    private static void tickNetherrackArmor(ServerPlayerEntity player) {
        if (BlockArmor.hasFullBlockArmorSet(player, "netherrack")) {
            if (!player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE) || 
                player.getStatusEffect(StatusEffects.FIRE_RESISTANCE).getDuration() < 200) {
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.FIRE_RESISTANCE, 400, 0, true, false, false));
            }
        }
    }
    
    // ============================================================
    // END STONE ARMOR - Slow Falling & Void resistance
    // ============================================================
    private static void tickEndstoneArmor(ServerPlayerEntity player) {
        if (BlockArmor.hasFullBlockArmorSet(player, "endstone")) {
            // Slow falling
            if (!player.hasStatusEffect(StatusEffects.SLOW_FALLING) || 
                player.getStatusEffect(StatusEffects.SLOW_FALLING) == null ||
                player.getStatusEffect(StatusEffects.SLOW_FALLING).getDuration() < 200) {
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SLOW_FALLING, 400, 0, true, false, false));
            }
            // Levitation immunity via slow falling also helps with void
        }
    }
    
    // ============================================================
    // ICE ARMOR - Frost Walker & Freeze immunity
    // ============================================================
    private static void tickIceArmor(ServerPlayerEntity player) {
        if (BlockArmor.hasFullBlockArmorSet(player, "ice")) {
            // Resistance to freezing (simulated by removing freezing effect)
            if (player.hasStatusEffect(StatusEffects.SLOWNESS) && 
                player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier() >= 4) {
                // Remove severe slowness that might be from freezing
                player.removeStatusEffect(StatusEffects.SLOWNESS);
            }
            // Speed boost on ice
            if (player.isOnGround() && player.getEntityWorld().getBlockState(
                player.getBlockPos().down()).getBlock() instanceof net.minecraft.block.IceBlock) {
                if (!player.hasStatusEffect(StatusEffects.SPEED) || 
                    player.getStatusEffect(StatusEffects.SPEED) == null ||
                    player.getStatusEffect(StatusEffects.SPEED).getDuration() < 40) {
                    player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.SPEED, 60, 1, true, false, false));
                }
            }
        }
    }
    
    // ============================================================
    // PRISMARINE ARMOR - Water Breathing & Dolphins Grace
    // ============================================================
    private static void tickPrismarineArmor(ServerPlayerEntity player) {
        if (BlockArmor.hasFullBlockArmorSet(player, "prismarine")) {
            // Water breathing
            if (!player.hasStatusEffect(StatusEffects.WATER_BREATHING) || 
                player.getStatusEffect(StatusEffects.WATER_BREATHING) == null ||
                player.getStatusEffect(StatusEffects.WATER_BREATHING).getDuration() < 200) {
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.WATER_BREATHING, 400, 0, true, false, false));
            }
            // Dolphins grace when in water
            if (player.isTouchingWaterOrRain()) {
                if (!player.hasStatusEffect(StatusEffects.DOLPHINS_GRACE) || 
                    player.getStatusEffect(StatusEffects.DOLPHINS_GRACE) == null ||
                    player.getStatusEffect(StatusEffects.DOLPHINS_GRACE).getDuration() < 200) {
                    player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.DOLPHINS_GRACE, 400, 0, true, false, false));
                }
            }
        }
    }
    
    // ============================================================
    // QUARTZ ARMOR - Attack speed boost
    // ============================================================
    private static void tickQuartzArmor(ServerPlayerEntity player) {
        if (BlockArmor.hasFullBlockArmorSet(player, "quartz")) {
            // Attack speed boost via haste (affects attack cooldown)
            if (!player.hasStatusEffect(StatusEffects.HASTE) || 
                player.getStatusEffect(StatusEffects.HASTE).getDuration() < 200) {
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.HASTE, 400, 0, true, false, false));
            }
        }
    }
    
    // ============================================================
    // GLASS ARMOR - Fragile but deflects
    // ============================================================
    private static void tickGlassArmor(ServerPlayerEntity player) {
        if (BlockArmor.hasFullBlockArmorSet(player, "glass")) {
            // Speed boost (glass is light)
            if (!player.hasStatusEffect(StatusEffects.SPEED) || 
                player.getStatusEffect(StatusEffects.SPEED) == null ||
                player.getStatusEffect(StatusEffects.SPEED).getDuration() < 200) {
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SPEED, 400, 1, true, false, false));
            }
        }
    }
    
    // ============================================================
    // TERRACOTTA ARMOR - Hardened protection
    // ============================================================
    private static void tickTerracottaArmor(ServerPlayerEntity player) {
        if (BlockArmor.hasFullBlockArmorSet(player, "terracotta")) {
            // Resistance
            if (!player.hasStatusEffect(StatusEffects.RESISTANCE) || 
                player.getStatusEffect(StatusEffects.RESISTANCE).getDuration() < 200) {
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.RESISTANCE, 400, 0, true, false, false));
            }
        }
    }
    
    // ============================================================
    // MOSSY ARMOR - Natural regeneration
    // ============================================================
    private static void tickMossyArmor(ServerPlayerEntity player) {
        if (BlockArmor.hasFullBlockArmorSet(player, "mossy")) {
            // Regeneration I when wearing full set
            if (!player.hasStatusEffect(StatusEffects.REGENERATION) || 
                player.getStatusEffect(StatusEffects.REGENERATION).getDuration() < 200) {
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.REGENERATION, 400, 0, true, false, false));
            }
        }
    }
    
    // ============================================================
    // SOUL SAND ARMOR - Soul speed
    // ============================================================
    private static void tickSoulSandArmor(ServerPlayerEntity player) {
        if (BlockArmor.hasFullBlockArmorSet(player, "soul_sand")) {
            // Speed boost on soul sand or soul soil
            if (player.getEntityWorld().getBlockState(player.getBlockPos().down()).isOf(net.minecraft.block.Blocks.SOUL_SAND) ||
                player.getEntityWorld().getBlockState(player.getBlockPos().down()).isOf(net.minecraft.block.Blocks.SOUL_SOIL)) {
                if (!player.hasStatusEffect(StatusEffects.SPEED) || 
                    player.getStatusEffect(StatusEffects.SPEED) == null ||
                    player.getStatusEffect(StatusEffects.SPEED).getDuration() < 200) {
                    player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.SPEED, 400, 1, true, false, false));
                }
            }
        }
    }
    
    // ============================================================
    // MAGMA ARMOR - Fire resistance and burning attackers
    // ============================================================
    private static void tickMagmaArmor(ServerPlayerEntity player) {
        if (BlockArmor.hasFullBlockArmorSet(player, "magma")) {
            // Fire resistance
            if (!player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE) || 
                player.getStatusEffect(StatusEffects.FIRE_RESISTANCE).getDuration() < 200) {
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.FIRE_RESISTANCE, 400, 0, true, false, false));
            }
        }
    }
    
    // ============================================================
    // SANDSTONE ARMOR - Desert wanderer (fall damage immunity handled elsewhere)
    // ============================================================
    private static void tickSandstoneArmor(ServerPlayerEntity player) {
        if (BlockArmor.hasFullBlockArmorSet(player, "sandstone")) {
            // Speed boost on sand
            if (player.getEntityWorld().getBlockState(player.getBlockPos().down()).getBlock() instanceof net.minecraft.block.SandBlock) {
                if (!player.hasStatusEffect(StatusEffects.SPEED) || 
                    player.getStatusEffect(StatusEffects.SPEED) == null ||
                    player.getStatusEffect(StatusEffects.SPEED).getDuration() < 200) {
                    player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.SPEED, 400, 0, true, false, false));
                }
            }
        }
    }
    
    // ============================================================
    // AMETHYST ARMOR - Magic damage reduction (Resistance)
    // ============================================================
    private static void tickAmethystArmor(ServerPlayerEntity player) {
        if (BlockArmor.hasFullBlockArmorSet(player, "amethyst")) {
            // Resistance (represents magic damage reduction)
            if (!player.hasStatusEffect(StatusEffects.RESISTANCE) || 
                player.getStatusEffect(StatusEffects.RESISTANCE).getDuration() < 200) {
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.RESISTANCE, 400, 0, true, false, false));
            }
        }
    }
    
    // ============================================================
    // COAL ARMOR - Night vision
    // ============================================================
    private static void tickCoalArmor(ServerPlayerEntity player) {
        if (BlockArmor.hasFullBlockArmorSet(player, "coal")) {
            // Night vision
            if (!player.hasStatusEffect(StatusEffects.NIGHT_VISION) || 
                player.getStatusEffect(StatusEffects.NIGHT_VISION) == null ||
                player.getStatusEffect(StatusEffects.NIGHT_VISION).getDuration() < 200) {
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.NIGHT_VISION, 400, 0, true, false, false));
            }
        }
    }
    
    // ============================================================
    // WARPED ARMOR - Endermen ignore player
    // ============================================================
    private static void tickWarpedArmor(ServerPlayerEntity player) {
        if (BlockArmor.hasFullBlockArmorSet(player, "warped")) {
            // Endermen ignore player with invisibility effect (simulated)
            if (!player.hasStatusEffect(StatusEffects.INVISIBILITY) || 
                player.getStatusEffect(StatusEffects.INVISIBILITY).getDuration() < 200) {
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.INVISIBILITY, 400, 0, true, false, false));
            }
        }
    }
    
    // ============================================================
    // WET SPONGE ARMOR - Fire extinguisher
    // ============================================================
    private static void tickWetSpongeArmor(ServerPlayerEntity player) {
        if (BlockArmor.hasFullBlockArmorSet(player, "wet_sponge")) {
            // Extinguish fire immediately when on fire
            if (player.isOnFire()) {
                player.extinguish();
                // Splash particles effect
                player.getEntityWorld().spawnParticles(
                    net.minecraft.particle.ParticleTypes.SPLASH,
                    player.getX(), player.getY() + 1, player.getZ(),
                    20, 0.5, 0.5, 0.5, 0.1);
            }
            // Fire resistance to prevent reignition
            if (!player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE) || 
                player.getStatusEffect(StatusEffects.FIRE_RESISTANCE).getDuration() < 60) {
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.FIRE_RESISTANCE, 100, 0, true, false, false));
            }
        }
    }
}
