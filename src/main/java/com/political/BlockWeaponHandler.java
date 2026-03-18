package com.political;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles abilities for block-based weapons (Glass, Obsidian, Quartz, etc.)
 * Each weapon has unique abilities themed after its block material.
 */
public class BlockWeaponHandler {

    private static final Map<UUID, Integer> quartzCritCounters = new HashMap<>();
    private static final Map<UUID, Long> redstoneCooldowns = new HashMap<>();
    private static final Map<UUID, Integer> glowstoneKillCounters = new HashMap<>();

    /**
     * Process block weapon damage and abilities
     * Call this from the damage event handler
     */
    public static float processDamage(ServerPlayerEntity player, LivingEntity target, float baseDamage) {
        ItemStack weapon = player.getMainHandStack();
        float finalDamage = baseDamage;

        Text customName = weapon.get(DataComponentTypes.CUSTOM_NAME);
        String weaponName = customName != null ? customName.getString() : "";

        // ===== GLASS BLADE - Fragile but Deadly =====
        if (weaponName.contains("Glass Blade")) {
            // 25% chance for critical hit (glass is sharp but fragile)
            if (player.getRandom().nextFloat() < 0.25f) {
                finalDamage *= 1.5f;
                player.sendMessage(Text.literal("§b§lSHATTER STRIKE! §r§7+50% damage"), true);
            }
            // Glass sword takes extra durability damage
            weapon.damage(2, player, net.minecraft.entity.EquipmentSlot.MAINHAND);
        }

        // ===== OBSIDIAN BLADE - Unbreakable Force =====
        if (weaponName.contains("Obsidian Blade")) {
            // Knockback resistance and heavy hits
            finalDamage *= 1.2f;
            target.takeKnockback(0.8f,
                    player.getX() - target.getX(),
                    player.getZ() - target.getZ());
        }

        // ===== QUARTZ BLADE - Crystalline Precision =====
        if (weaponName.contains("Quartz Blade")) {
            // Every 4th hit is a guaranteed crit
            int hits = quartzCritCounters.getOrDefault(player.getUuid(), 0) + 1;
            quartzCritCounters.put(player.getUuid(), hits);

            if (hits % 4 == 0) {
                finalDamage *= 2.0f;
                player.sendMessage(Text.literal("§f§lCRYSTAL CRIT! §r§72x Damage!"), true);
                // Apply glowing to target
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 100, 0, true, false, false));
            }
        }

        // ===== GLOWSTONE BLADE - Radiant Power =====
        if (weaponName.contains("Glowstone Blade")) {
            // Extra damage to undead in dark areas
            if (target.getEntityWorld().getLightLevel(target.getBlockPos()) < 8) {
                if (target.getType().isIn(net.minecraft.registry.tag.EntityTypeTags.UNDEAD)) {
                    finalDamage *= 1.3f;
                    player.sendMessage(Text.literal("§e§lRADIANT BURN! §r§7+30% vs undead"), true);
                }
            }
            // Heal player slightly (light energy)
            if (player.getHealth() < player.getMaxHealth()) {
                player.heal(0.5f);
            }
        }

        // ===== REDSTONE BLADE - Electric Charge =====
        if (weaponName.contains("Redstone Blade")) {
            String cooldownKey = player.getUuid() + "_redstone_stun";
            long now = System.currentTimeMillis();
            long lastUse = redstoneCooldowns.getOrDefault(UUID.fromString(cooldownKey.split("_")[0]), 0L);

            if (now - lastUse > 3000) { // 3 second cooldown
                redstoneCooldowns.put(player.getUuid(), now);
                // Stun effect (slowness + weakness)
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 3, true, false, false));
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 40, 0, true, false, false));
                player.sendMessage(Text.literal("§c§lELECTRIC STUN!"), true);

                // Chain to nearby enemies
                ServerWorld world = player.getEntityWorld();
                for (LivingEntity nearby : world.getEntitiesByClass(LivingEntity.class,
                        target.getBoundingBox().expand(3), e -> e != player && e != target && e.isAlive())) {
                    nearby.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20, 1, true, false, false));
                }
            }
        }

        // ===== NETHERRACK BLADE - Infernal Fire =====
        if (weaponName.contains("Netherrack Blade")) {
            // Set target on fire
            target.setFireTicks(100); // 5 seconds
            // Extra damage in Nether
            if (player.getEntityWorld().getDimension().hasCeiling()) {
                finalDamage *= 1.25f;
                player.sendMessage(Text.literal("§4§lINFERNAL STRIKE! §r§7+25% in Nether"), true);
            }
        }

        // ===== END STONE BLADE - Void Touch =====
        if (weaponName.contains("Endstone") || weaponName.contains("End Stone")) {
            // Reduced knockback (End dimension stability)
            // Small chance to teleport enemy randomly (5%)
            if (player.getRandom().nextFloat() < 0.05f) {
                Vec3d targetPos = new Vec3d(target.getX(), target.getY(), target.getZ());
                Vec3d randomOffset = new Vec3d(
                        (player.getRandom().nextDouble() - 0.5) * 6,
                        0,
                        (player.getRandom().nextDouble() - 0.5) * 6
                );
                target.requestTeleport(targetPos.x + randomOffset.x, targetPos.y, targetPos.z + randomOffset.z);
                player.sendMessage(Text.literal("§5§lVOID SHIFT!"), true);
            }
        }

        // ===== ICE BLADE - Frost Touch =====
        if (weaponName.contains("Ice Blade")) {
            // Slow enemies
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 1, true, false, false));
            // Bonus damage if target is already slowed
            if (target.hasStatusEffect(StatusEffects.SLOWNESS)) {
                finalDamage *= 1.15f;
            }
            // Create snow particles on hit
            ServerWorld world = player.getEntityWorld();
            world.spawnParticles(net.minecraft.particle.ParticleTypes.SNOWFLAKE,
                    target.getX(), target.getY() + 1, target.getZ(), 10, 0.5, 0.5, 0.5, 0.1);
        }

        // ===== PRISMARINE BLADE - Ocean's Might =====
        if (weaponName.contains("Prismarine Blade")) {
            // Bonus damage in water/rain
            if (player.isTouchingWater() || player.getEntityWorld().isRaining()) {
                finalDamage *= 1.3f;
                player.sendMessage(Text.literal("§3§lOCEAN'S MIGHT! §r§7+30%"), true);
            }
            // Apply mining fatigue (represents water pressure)
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 40, 0, true, false, false));
        }

        // ===== TERRACOTTA BLADE - Earth Smash =====
        if (weaponName.contains("Terracotta Blade")) {
            // Heavy knockback
            target.takeKnockback(1.2f,
                    player.getX() - target.getX(),
                    player.getZ() - target.getZ());
            // Bonus damage if player is on ground
            if (player.isOnGround()) {
                finalDamage *= 1.1f;
            }
        }

        return finalDamage;
    }

    /**
     * Reset counters when player switches targets or dies
     */
    public static void resetCounters(UUID playerUuid) {
        quartzCritCounters.remove(playerUuid);
        glowstoneKillCounters.remove(playerUuid);
    }

    /**
     * Check if item is a block sword
     */
    public static boolean isBlockSword(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Text customName = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (customName == null) return false;
        String name = customName.getString();
        return name.contains("Blade") || name.contains("Sword") &&
               (name.contains("Glass") || name.contains("Obsidian") || name.contains("Quartz") ||
                name.contains("Glowstone") || name.contains("Redstone") || name.contains("Netherrack") ||
                name.contains("End Stone") || name.contains("Ice") || name.contains("Prismarine") ||
                name.contains("Terracotta") || name.contains("Mossy") || name.contains("Soul Sand") ||
                name.contains("Magma") || name.contains("Sandstone") || name.contains("Amethyst") ||
                name.contains("Coal"));
    }
}
