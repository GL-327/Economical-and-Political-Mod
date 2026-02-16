package com.political;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class T2ArmorAbilityHandler {

    private static final Map<UUID, Long> sonicPulseCooldown = new HashMap<>();
    private static final Map<UUID, Long> teleportDodgeCooldown = new HashMap<>();

    private static final long SONIC_PULSE_COOLDOWN_MS = 10000;
    private static final long TELEPORT_DODGE_COOLDOWN_MS = 5000;

    private static final Random random = new Random();

    public static void tick(ServerPlayerEntity player) {
        tickEndermanArmor(player);
        tickSlimeArmor(player);
        tickWardenArmor(player);
        tickSkeletonArmor(player);
    }

    // ============================================================
    // ENDERMAN T2 ARMOR
    // ============================================================
    private static void tickEndermanArmor(ServerPlayerEntity player) {
        int t2Pieces = countT2ArmorPieces(player, "Void", "Phantom");
        if (t2Pieces == 0) return;

        ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);
        if (isT2EndermanHelmet(helmet)) {
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.NIGHT_VISION, 400, 0, true, false, false
            ));
        }

        if (t2Pieces >= 2) {
            int speedLevel = t2Pieces >= 4 ? 1 : 0;
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SPEED, 40, speedLevel, true, false, false
            ));
        }

        if (t2Pieces >= 4 && player.fallDistance > 3.0f) {
            ServerWorld world = player.getEntityWorld();
            if (world.getTime() % 5 == 0) {
                world.spawnParticles(ParticleTypes.REVERSE_PORTAL,
                        player.getX(), player.getY(), player.getZ(),
                        3, 0.3, 0.5, 0.3, 0.01);
            }
        }
    }

    public static boolean tryTeleportDodge(ServerPlayerEntity player, float damage) {
        ItemStack chestplate = player.getEquippedStack(EquipmentSlot.CHEST);
        if (!isT2EndermanChestplate(chestplate)) return false;

        UUID uuid = player.getUuid();
        long now = System.currentTimeMillis();

        Long lastDodge = teleportDodgeCooldown.get(uuid);
        if (lastDodge != null && (now - lastDodge) < TELEPORT_DODGE_COOLDOWN_MS) {
            return false;
        }

        if (random.nextFloat() > 0.20f) return false;

        teleportDodgeCooldown.put(uuid, now);

        ServerWorld world = player.getEntityWorld();
        double posX = player.getX();
        double posY = player.getY();
        double posZ = player.getZ();

        for (int attempts = 0; attempts < 10; attempts++) {
            double offsetX = (random.nextDouble() - 0.5) * 10;
            double offsetZ = (random.nextDouble() - 0.5) * 10;
            double newX = posX + offsetX;
            double newZ = posZ + offsetZ;

            if (world.isAir(player.getBlockPos().add((int)offsetX, 0, (int)offsetZ)) &&
                    world.isAir(player.getBlockPos().add((int)offsetX, 1, (int)offsetZ))) {

                world.spawnParticles(ParticleTypes.REVERSE_PORTAL,
                        posX, posY + 1, posZ, 20, 0.3, 0.5, 0.3, 0.1);

                player.teleport(world, newX, posY, newZ,
                        java.util.Set.of(), player.getYaw(), player.getPitch(), false);

                world.spawnParticles(ParticleTypes.REVERSE_PORTAL,
                        newX, posY + 1, newZ, 20, 0.3, 0.5, 0.3, 0.1);

                world.playSound(null, player.getBlockPos(),
                        SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.0f);

                player.sendMessage(Text.literal("§5§l☍ TELEPORT DODGE!"), true);

                return true;
            }
        }

        return false;
    }

    // ============================================================
    // SLIME T2 ARMOR
    // ============================================================
    private static void tickSlimeArmor(ServerPlayerEntity player) {
        int t2Pieces = countT2ArmorPieces(player, "Gelatinous", "Rustler");
        if (t2Pieces == 0) return;

        ItemStack leggings = player.getEquippedStack(EquipmentSlot.LEGS);
        ItemStack boots = player.getEquippedStack(EquipmentSlot.FEET);

        int jumpLevel = 0;
        if (isT2SlimeLeggings(leggings)) jumpLevel += 1;
        if (isT2SlimeBoots(boots)) jumpLevel += 2;

        if (jumpLevel > 0) {
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.JUMP_BOOST, 40, jumpLevel - 1, true, false, false
            ));
        }

        if (player.isOnGround() && player.fallDistance > 2.0f && t2Pieces >= 2) {
            ServerWorld world = player.getEntityWorld();
            world.spawnParticles(ParticleTypes.ITEM_SLIME,
                    player.getX(), player.getY(), player.getZ(),
                    10, 0.5, 0.1, 0.5, 0.1);
        }
    }

    // ============================================================
    // WARDEN T2 ARMOR
    // ============================================================
    private static void tickWardenArmor(ServerPlayerEntity player) {
        int t2Pieces = countT2ArmorPieces(player, "Sculk", "Terror");
        if (t2Pieces == 0) return;

        ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);
        if (isT2WardenHelmet(helmet)) {
            if (player.hasStatusEffect(StatusEffects.DARKNESS)) {
                player.removeStatusEffect(StatusEffects.DARKNESS);
            }
        }

        if (t2Pieces >= 4) {
            ServerWorld world = player.getEntityWorld();
            if (world.getTime() % 20 == 0) {
                Box searchBox = player.getBoundingBox().expand(15.0);
                for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, searchBox,
                        e -> e instanceof HostileEntity && !e.isGlowing())) {
                    entity.setGlowing(true);
                }
            }
        }
    }

    public static boolean trySonicPulse(ServerPlayerEntity player) {
        ItemStack chestplate = player.getEquippedStack(EquipmentSlot.CHEST);
        if (!isT2WardenChestplate(chestplate)) return false;
        if (!player.isSneaking()) return false;

        UUID uuid = player.getUuid();
        long now = System.currentTimeMillis();

        Long lastPulse = sonicPulseCooldown.get(uuid);
        if (lastPulse != null && (now - lastPulse) < SONIC_PULSE_COOLDOWN_MS) {
            long remaining = (SONIC_PULSE_COOLDOWN_MS - (now - lastPulse)) / 1000;
            player.sendMessage(Text.literal("§c⏳ Sonic Pulse cooldown: " + remaining + "s"), true);
            return false;
        }

        sonicPulseCooldown.put(uuid, now);

        ServerWorld world = player.getEntityWorld();

        Box pulseBox = player.getBoundingBox().expand(8.0);
        int hitCount = 0;

        for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, pulseBox,
                e -> e instanceof HostileEntity && e != player)) {

            entity.damage(world, player.getDamageSources().sonicBoom(player), 20.0f);

            // Knockback
            double entityX = entity.getX();
            double entityZ = entity.getZ();
            double playerX = player.getX();
            double playerZ = player.getZ();

            double dx = entityX - playerX;
            double dz = entityZ - playerZ;
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > 0) {
                dx /= dist;
                dz /= dist;
            }

            entity.setVelocity(dx * 2.0, 0.8, dz * 2.0);

            entity.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SLOWNESS, 60, 2, true, true, true
            ));

            hitCount++;
        }

        world.playSound(null, player.getBlockPos(),
                SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 1.5f, 1.0f);

        for (int i = 0; i < 36; i++) {
            double angle = Math.toRadians(i * 10);
            for (int r = 2; r <= 8; r += 2) {
                double x = player.getX() + Math.cos(angle) * r;
                double z = player.getZ() + Math.sin(angle) * r;
                world.spawnParticles(ParticleTypes.SONIC_BOOM, x, player.getY() + 1, z, 1, 0, 0, 0, 0);
            }
        }

        player.sendMessage(Text.literal("§3§l📡 SONIC PULSE! §7Hit " + hitCount + " enemies"), true);

        return true;
    }

    // ============================================================
    // SKELETON T2 ARMOR
    // ============================================================
    private static void tickSkeletonArmor(ServerPlayerEntity player) {
        int t2Pieces = countT2ArmorPieces(player, "Bone", "Desperado");
        if (t2Pieces == 0) return;

        ItemStack boots = player.getEquippedStack(EquipmentSlot.FEET);
        if (isT2SkeletonBoots(boots)) {
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SPEED, 40, 0, true, false, false
            ));
        }
    }

    public static float getProjectileDamageReduction(ServerPlayerEntity player) {
        int t2Pieces = countT2ArmorPieces(player, "Bone", "Desperado");
        if (t2Pieces == 0) return 1.0f;

        float reduction = t2Pieces * 0.15f;
        return 1.0f - Math.min(0.60f, reduction);
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    private static int countT2ArmorPieces(ServerPlayerEntity player, String... keywords) {
        int count = 0;
        ItemStack[] armor = {
                player.getEquippedStack(EquipmentSlot.HEAD),
                player.getEquippedStack(EquipmentSlot.CHEST),
                player.getEquippedStack(EquipmentSlot.LEGS),
                player.getEquippedStack(EquipmentSlot.FEET)
        };

        for (ItemStack stack : armor) {
            if (stack.isEmpty()) continue;
            Text customName = stack.get(DataComponentTypes.CUSTOM_NAME);
            if (customName == null) continue;
            String name = customName.getString();

            if (!name.contains(" II")) continue;

            for (String keyword : keywords) {
                if (name.contains(keyword)) {
                    count++;
                    break;
                }
            }
        }

        return count;
    }

    private static boolean isT2EndermanHelmet(ItemStack stack) {
        return isT2Piece(stack, "Void", "Phantom", "Helmet");
    }

    private static boolean isT2EndermanChestplate(ItemStack stack) {
        return isT2Piece(stack, "Void", "Phantom", "Chestplate");
    }

    private static boolean isT2SlimeLeggings(ItemStack stack) {
        return isT2Piece(stack, "Gelatinous", "Rustler", "Leggings");
    }

    private static boolean isT2SlimeBoots(ItemStack stack) {
        return isT2Piece(stack, "Gelatinous", "Rustler", "Boots");
    }

    private static boolean isT2WardenHelmet(ItemStack stack) {
        return isT2Piece(stack, "Sculk", "Terror", "Helmet");
    }

    private static boolean isT2WardenChestplate(ItemStack stack) {
        return isT2Piece(stack, "Sculk", "Terror", "Chestplate");
    }

    private static boolean isT2SkeletonBoots(ItemStack stack) {
        return isT2Piece(stack, "Bone", "Desperado", "Boots");
    }

    private static boolean isT2Piece(ItemStack stack, String keyword1, String keyword2, String pieceType) {
        if (stack.isEmpty()) return false;
        Text customName = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (customName == null) return false;
        String name = customName.getString();
        return name.contains(" II") &&
                (name.contains(keyword1) || name.contains(keyword2)) &&
                name.contains(pieceType);
    }
}