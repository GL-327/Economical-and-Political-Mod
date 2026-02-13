package com.political;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.*;import net.minecraft.util.math.Box;

public class ArmorAbilityHandler {
    private static final Map<UUID, Long> entityNoiseTimestamps = new HashMap<>();
    private static final long NOISE_FADE_TIME_MS = 5000; // 5 seconds after last noise
    private static final Set<UUID> espGlowingMobs = new HashSet<>();

    // Call this every tick from PoliticalServer.java
    public static void tick(ServerPlayerEntity player) {
        tickVenomousLeggings(player);
        tickSlimeBoots(player);
        tickWardenChestplate(player);
        tickBerserkerHelmet(player);
    }

    // Track death save cooldowns
    private static final Map<UUID, Long> deathSaveCooldowns = new HashMap<>();

    public static boolean trySlimeBootsDeathSave(ServerPlayerEntity player) {
        ItemStack boots = player.getEquippedStack(EquipmentSlot.FEET);
        if (!isSlimeBoots(boots)) return false;

        UUID uuid = player.getUuid();
        long now = System.currentTimeMillis();
        long lastSave = deathSaveCooldowns.getOrDefault(uuid, 0L);

        // 5 minute cooldown
        if (now - lastSave < 300000) return false;

        deathSaveCooldowns.put(uuid, now);

        // Save the player
        player.setHealth(1.0f);

        // Apply shrink effect (slowness + jump boost to simulate smaller hitbox feel)
// Example - in ArmorAbilityHandler.java, change all effect applications:
        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SLOWNESS,
                100,
                1,
                true,   // ambient - true hides from HUD
                false,  // showParticles
                false   // showIcon - this hides from side of screen
        ));

        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.JUMP_BOOST,
                100,
                3,
                true,   // ambient - true hides from HUD
                false,  // showParticles
                false   // showIcon - this hides from side of screen
        ));
        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.RESISTANCE,
                60,
                2,
                true,   // ambient - true hides from HUD
                false,  // showParticles
                false   // showIcon - this hides from side of screen
        ));

        // Visual/audio feedback
        ServerWorld world = player.getEntityWorld();
        world.playSound(null, player.getBlockPos(),
                net.minecraft.sound.SoundEvents.ENTITY_SLIME_SQUISH,
                net.minecraft.sound.SoundCategory.PLAYERS, 1.0f, 1.5f);

        player.sendMessage(Text.literal("§a§lSLIME SAVE! §r§7You bounced back from death!"), false);
        player.sendMessage(Text.literal("§7Cooldown: §e5 minutes"), false);

        return true;
    }

    // ============================================================
    // VENOMOUS LEGGINGS (Spider Slayer)
    // ============================================================
    private static void tickVenomousLeggings(ServerPlayerEntity player) {
        ItemStack leggings = player.getEquippedStack(EquipmentSlot.LEGS);
        if (!isVenomousLeggings(leggings)) return;

        // Passive 1: Poison immunity - remove poison if player has it
        if (player.hasStatusEffect(StatusEffects.POISON)) {
            player.removeStatusEffect(StatusEffects.POISON);
        }

        // Passive 2: Nearby enemies get poisoned (every 40 ticks = 2 seconds)
        if (player.age % 40 == 0) {
            ServerWorld world = player.getEntityWorld();
            for (LivingEntity entity : world.getEntitiesByClass(
                    LivingEntity.class,
                    player.getBoundingBox().expand(4.0),
                    e -> e != player && e instanceof HostileEntity && e.isAlive())) {
                // Hidden poison effect (ambient=true, showParticles=false, showIcon=false)
                entity.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.POISON, 60, 1, true, false, false
                ));
            }
        }

        // Passive 3: Speed boost when poisoning enemies
        if (player.age % 100 == 0) { // Check every 5 seconds
            ServerWorld world = player.getEntityWorld();
            long poisonedNearby = world.getEntitiesByClass(
                    LivingEntity.class,
                    player.getBoundingBox().expand(6.0),
                    e -> e != player && e.hasStatusEffect(StatusEffects.POISON)
            ).size();

            if (poisonedNearby > 0) {
                // Speed boost based on poisoned enemies (hidden)
                int amplifier = (int) Math.min(2, poisonedNearby - 1);
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.SPEED, 60, amplifier, true, false, false
                ));
            }
        }
    }

    private static boolean isVenomousLeggings(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Text customName = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (customName == null) return false;
        String name = customName.getString();
        return name.contains("Venomous") || (name.contains("Spider") && name.contains("Leggings"));
    }

    // ============================================================
    // SLIME BOOTS (Slime Slayer)
    // ============================================================
    private static void tickSlimeBoots(ServerPlayerEntity player) {
        ItemStack boots = player.getEquippedStack(EquipmentSlot.FEET);
        if (!isSlimeBoots(boots)) return;

        // Passive 1: No fall damage (handled via damage event, but also give slow falling)
        if (player.fallDistance > 3.0f && !player.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SLOW_FALLING, 40, 0, true, false, false
            ));
        }

        // Passive 2: Jump boost
        if (!player.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.JUMP_BOOST, 40, 1, true, false, false
            ));
        }
    }

    private static boolean isSlimeBoots(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Text customName = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (customName == null) return false;
        String name = customName.getString();
        return name.contains("Slime") && name.contains("Boots");
    }
    public static void detectEntityNoise(ServerWorld world) {
        if (world.getPlayers().isEmpty()) return;

        for (ServerPlayerEntity player : world.getPlayers()) {
            ItemStack chestplate = player.getEquippedStack(EquipmentSlot.CHEST);
            if (!isWardenChestplate(chestplate)) continue;

            if (player.getBoundingBox() == null) continue;

            Box searchBox = player.getBoundingBox().expand(64.0);

            for (LivingEntity entity : world.getEntitiesByClass(
                    LivingEntity.class,
                    searchBox,
                    e -> e != player && e.isAlive())) {

                boolean madeNoise = false;

                // 1. Entity is sprinting
                if (entity.isSprinting()) madeNoise = true;

                // 2. Entity is attacking
                if (entity.handSwinging) madeNoise = true;

                // 3. Entity is moving (very low threshold to catch walking)
                double velocitySq = entity.getVelocity().horizontalLengthSquared();
                if (velocitySq > 0.0001) madeNoise = true;

                // 4. Entity took damage
                if (entity.hurtTime > 0) madeNoise = true;

                // 5. Hostile mobs chasing a target always make noise
                if (entity instanceof HostileEntity hostile) {
                    if (hostile.getTarget() != null) {
                        madeNoise = true;
                    }
                }

                // 6. Any mob that's not sneaking and is on ground with velocity
                if (entity.isOnGround() && !entity.isSneaking() && velocitySq > 0.00001) {
                    madeNoise = true;
                }

                if (madeNoise) {
                    entityNoiseTimestamps.put(entity.getUuid(), System.currentTimeMillis());
                }
            }
        }
    }


    // ============================================================
    // WARDEN CHESTPLATE (Warden Slayer)
    // ============================================================

    private static void tickWardenChestplate(ServerPlayerEntity player) {
        ItemStack chestplate = player.getEquippedStack(EquipmentSlot.CHEST);
        if (!isWardenChestplate(chestplate)) {
            return;
        }

        // Passive 1: Darkness immunity
        if (player.hasStatusEffect(StatusEffects.DARKNESS)) {
            player.removeStatusEffect(StatusEffects.DARKNESS);
        }

        // Passive 2: Night vision
        StatusEffectInstance currentNightVision = player.getStatusEffect(StatusEffects.NIGHT_VISION);
        if (currentNightVision == null || currentNightVision.getDuration() < 220) {
// Example - in ArmorAbilityHandler.java, change all effect applications:
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.NIGHT_VISION,
                    400,
                    0,
                    true,   // ambient - true hides from HUD
                    false,  // showParticles
                    false   // showIcon - this hides from side of screen
            ));
        }

        // Passive 3: ESP - check every 10 ticks
        if (player.age % 10 == 0) {
            ServerWorld world = player.getEntityWorld();
            long now = System.currentTimeMillis();

            // Clean up old entries (older than 5 seconds)
            entityNoiseTimestamps.entrySet().removeIf(entry ->
                    now - entry.getValue() > NOISE_FADE_TIME_MS
            );

            // Make sure player has valid bounding box
            if (player.getBoundingBox() == null) return;

            // Apply glowing to entities that made noise recently (64 block range)
            for (LivingEntity entity : world.getEntitiesByClass(
                    LivingEntity.class,
                    player.getBoundingBox().expand(64.0),
                    e -> e != player && e.isAlive())) {

                UUID entityId = entity.getUuid();
                Long lastNoise = entityNoiseTimestamps.get(entityId);

                if (lastNoise != null && (now - lastNoise) <= NOISE_FADE_TIME_MS) {
                    entity.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.GLOWING, 30, 0, true, false, false
                    ));
                }
            }
        }
    }

    public static void onEntityMakeNoise(LivingEntity entity, ServerWorld world) {
        // Check if any nearby player has Warden Chestplate
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (player.squaredDistanceTo(entity) > 576) continue; // 24 blocks squared

            ItemStack chestplate = player.getEquippedStack(EquipmentSlot.CHEST);
            if (isWardenChestplate(chestplate)) {
                // Register noise for this entity
                entityNoiseTimestamps.put(entity.getUuid(), System.currentTimeMillis());
            }
        }
    }

    private static boolean isWardenChestplate(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Text customName = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (customName == null) return false;
        String name = customName.getString();
        return name.contains("Warden") || name.contains("Sculk");
    }

    // ============================================================
    // BERSERKER HELMET (Zombie Slayer)
    // ============================================================
    private static void tickBerserkerHelmet(ServerPlayerEntity player) {
        ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);
        if (!isBerserkerHelmet(helmet)) return;

        // Passive: Strength boost when low HP
        float healthPercent = player.getHealth() / player.getMaxHealth();

        if (healthPercent < 0.5f) {
            // More strength at lower HP
            int amplifier = healthPercent < 0.25f ? 1 : 0;

            if (!player.hasStatusEffect(StatusEffects.STRENGTH) ||
                    player.getStatusEffect(StatusEffects.STRENGTH).getAmplifier() < amplifier) {
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.STRENGTH, 40, amplifier, true, false, false
                ));
            }
        }

        // Passive 2: Resistance when very low
        if (healthPercent < 0.25f) {
            if (!player.hasStatusEffect(StatusEffects.RESISTANCE)) {
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.RESISTANCE, 40, 0, true, false, false
                ));
            }
        }
    }

    private static boolean isBerserkerHelmet(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Text customName = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (customName == null) return false;
        String name = customName.getString();
        return name.contains("Berserker") || (name.contains("Zombie") && name.contains("Helmet"));
    }
}