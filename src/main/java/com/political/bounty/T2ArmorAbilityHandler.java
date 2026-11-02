package com.political.bounty;


import com.political.items.CustomItemHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

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

    public static void tick(ServerPlayer player) {
        tickEndermanArmor(player);
        tickSlimeArmor(player);
        tickWardenArmor(player);
        tickSkeletonArmor(player);
    }

    // ============================================================
    // ENDERMAN T2 ARMOR
    // ============================================================
    private static void tickEndermanArmor(ServerPlayer player) {
        int t2Pieces = countT2ArmorPieces(player, "Void", "Phantom");
        if (t2Pieces == 0) return;

        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (isT2EndermanHelmet(helmet)) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.NIGHT_VISION, 400, 0, true, false, false
            ));
        }

        if (t2Pieces >= 2) {
            int speedLevel = t2Pieces >= 4 ? 1 : 0;
            player.addEffect(new MobEffectInstance(
                    MobEffects.SPEED, 40, speedLevel, true, false, false
            ));
        }

        if (t2Pieces >= 4 && player.fallDistance > 3.0f) {
            ServerLevel world = player.level();
            if (world.getServer().getTickCount() % 12 == 0) {
                world.sendParticles(ParticleTypes.REVERSE_PORTAL,
                        player.getX(), player.getY(), player.getZ(),
                        2, 0.3, 0.5, 0.3, 0.01);
            }
        }
    }

    public static boolean tryTeleportDodge(ServerPlayer player, float damage) {
        return false; // T2 armor does not have this ability
    }

    // ============================================================
    // SLIME T2 ARMOR
    // ============================================================
    private static void tickSlimeArmor(ServerPlayer player) {
        int t2Pieces = countT2ArmorPieces(player, "Gelatinous", "Rustler");
        if (t2Pieces == 0) return;

        ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);

        int jumpLevel = 0;
        if (isT2SlimeLeggings(leggings)) jumpLevel += 1;
        if (isT2SlimeBoots(boots)) jumpLevel += 2;

        if (jumpLevel > 0) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.JUMP_BOOST, 40, jumpLevel - 1, true, false, false
            ));
        }

        if (player.onGround() && player.fallDistance > 2.0f && t2Pieces >= 2) {
            ServerLevel world = player.level();
            world.sendParticles(ParticleTypes.ITEM_SLIME,
                    player.getX(), player.getY(), player.getZ(),
                    4, 0.5, 0.1, 0.5, 0.1);
        }
    }

    // ============================================================
    // WARDEN T2 ARMOR
    // ============================================================
    private static void tickWardenArmor(ServerPlayer player) {
        int t2Pieces = countT2ArmorPieces(player, "Sculk", "Terror");
        if (t2Pieces == 0) return;

        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (isT2WardenHelmet(helmet)) {
            if (player.hasEffect(MobEffects.DARKNESS) && !CustomItemHandler.isInEnderPhaseMode(player.getUUID())) {
                player.removeEffect(MobEffects.DARKNESS);
            }
        }

        if (t2Pieces >= 4) {
            ServerLevel world = (ServerLevel) player.level();
            if (world.getServer().getTickCount() % 20 == 0) {
                AABB searchBox = player.getBoundingBox().inflate(15.0);
                for (LivingEntity entity : world.getEntitiesOfClass(LivingEntity.class, searchBox,
                        e -> e instanceof Monster && !e.hasEffect(MobEffects.GLOWING))) {
                    entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, false, false, false));
                }
            }
        }
    }

    public static boolean trySonicPulse(ServerPlayer player) {
        return false; // T2 armor does not have this ability
    }

    // ============================================================
    // SKELETON T2 ARMOR
    // ============================================================
    private static void tickSkeletonArmor(ServerPlayer player) {
        int t2Pieces = countT2ArmorPieces(player, "Bone", "Desperado");
        if (t2Pieces == 0) return;

        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (isT2SkeletonBoots(boots)) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.SPEED, 40, 0, true, false, false
            ));
        }
    }

    public static float getProjectileDamageReduction(ServerPlayer player) {
        int t2Pieces = countT2ArmorPieces(player, "Bone", "Desperado");
        if (t2Pieces == 0) return 1.0f;

        float reduction = t2Pieces * 0.15f;
        return 1.0f - Math.min(0.60f, reduction);
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    private static int countT2ArmorPieces(ServerPlayer player, String... keywords) {
        int count = 0;
        ItemStack[] armor = {
                player.getItemBySlot(EquipmentSlot.HEAD),
                player.getItemBySlot(EquipmentSlot.CHEST),
                player.getItemBySlot(EquipmentSlot.LEGS),
                player.getItemBySlot(EquipmentSlot.FEET)
        };

        for (ItemStack stack : armor) {
            if (stack.isEmpty()) continue;
            Component customName = stack.get(DataComponents.CUSTOM_NAME);
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
        Component customName = stack.get(DataComponents.CUSTOM_NAME);
        if (customName == null) return false;
        String name = customName.getString();
        return name.contains(" II") &&
                (name.contains(keyword1) || name.contains(keyword2)) &&
                name.contains(pieceType);
    }
}