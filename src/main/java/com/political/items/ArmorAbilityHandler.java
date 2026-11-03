package com.political.items;
import com.political.PoliticalServer;



import com.political.bounty.SlayerItems;
import net.minecraft.core.component.DataComponents;
import com.political.items.DragonChestplates;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

import java.util.*;

public class ArmorAbilityHandler {
    private static final Map<UUID, Long> entityNoiseTimestamps = new HashMap<>();
    private static final long NOISE_FADE_TIME_MS = 5000; // 5 seconds after last noise
    private static final Set<UUID> espGlowingMobs = new HashSet<>();
    /** How often (in server ticks) to sweep expired noise timestamps. */
    private static final int NOISE_CLEANUP_INTERVAL = 200; // ~10 seconds
    private static int armorAbilityTickCounter = 0;
    
    // Track previous chestplate for health reset on unequip
    private static final Map<UUID, String> previousChestplate = new HashMap<>();
    // Track players with fall damage immunity after flight
    private static final Set<UUID> fallImmunityPlayers = new HashSet<>();
    // Track previous max health for auto-heal
    private static final Map<UUID, Float> previousMaxHealth = new HashMap<>();
    private static final Map<UUID, Boolean> previousFlying = new HashMap<>();

    // Call this every tick from PoliticalServer.java
    public static void tick(ServerPlayer player) {
        // Check for chestplate unequip - reset health if dragon chestplate removed
        checkChestplateUnequip(player);
        
        // Handle fall damage immunity after flight
        tickFallImmunity(player);
        
        tickVenomousLeggings(player);
        tickSlimeBoots(player);
        tickWardenChestplate(player);
        tickBerserkerHelmet(player);
        // New block armor set abilities
        tickCrimsonStemSet(player);
        tickCryingObsidianSet(player);
        tickGildedBlackstoneSet(player);
        tickGraniteSet(player);
        tickDioriteSet(player);
        tickAndesiteSet(player);
        tickPolishedGraniteSet(player);
        tickPolishedDioriteSet(player);
        tickPolishedAndesiteSet(player);
        tickPackedIceSet(player);
        tickBlueIceSet(player);
        tickNetherGoldOreSet(player);
        tickDarkOakLogSet(player);
        tickJungleLogSet(player);
        tickAcaciaLogSet(player);
        tickMangroveLogSet(player);
        tickCherryLogSet(player);
        tickBambooBlockSet(player);
        tickTuffSet(player);
        tickPolishedTuffSet(player);
        tickNetherWartBlockSet(player);
        tickWarpedWartBlockSet(player);
        tickChiseledDeepslateSet(player);
        tickReinforcedDeepslateSet(player);
        tickChiseledNetherBrickSet(player);
        tickCrackedNetherBrickSet(player);
        tickChiseledStoneBrickSet(player);
        tickCrackedStoneBrickSet(player);
        tickEndStoneBrickSet(player);
        tickRedSandstoneSet(player);
        tickRawIronBlockSet(player);
        tickRawGoldBlockSet(player);
        tickPrismarineBricksSet(player);
        tickDarkPrismarineSet(player);
        tickSeaLanternSet(player);
        tickLodestoneSet(player);
        tickBlackstoneBricksSet(player);
        tickPolishedBlackstoneSet(player);
        tickSmoothBasaltSet(player);
        tickAmethystClusterSet(player);
        tickObsidianBlockSet(player);
        com.political.items.DragonPlateHandler.tick(player);
    }

    /**
     * Shared creative-flight handling for dragon chestplates (fall immunity when toggling off mid-air).
     */
    public static void applyConditionalCreativeFlight(ServerPlayer player, boolean shouldAllowFlight) {
        UUID uuid = player.getUUID();
        var abilities = player.getAbilities();
        if (!shouldAllowFlight) {
            boolean wasAirborneFly = abilities.flying && !player.onGround();
            abilities.mayfly = false;
            abilities.flying = false;
            if (wasAirborneFly) {
                grantFallImmunity(uuid);
            }
            previousFlying.put(uuid, false);

            // Restore max health when flight is disabled so the "flight debuff"
            // only applies while the player is actually flying.
            ServerLevel world = (ServerLevel) player.level();
            float targetHealth = world.isThundering() ? 48f : 24f;
            if (player.getMaxHealth() != targetHealth) {
                player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(targetHealth);
                if (player.getHealth() < targetHealth) {
                    player.setHealth(targetHealth);
                } else if (player.getHealth() > targetHealth) {
                    player.setHealth(targetHealth);
                }
            }
            previousMaxHealth.put(uuid, targetHealth);

            player.onUpdateAbilities();
            return;
        }
        abilities.mayfly = true;
        boolean wasFlying = previousFlying.getOrDefault(uuid, false);
        boolean isFlying = abilities.flying;
        previousFlying.put(uuid, isFlying);
        if (wasFlying && !isFlying && !player.onGround()) {
            grantFallImmunity(uuid);
        }
        if (isFlying) {
            fallImmunityPlayers.remove(uuid);
            player.fallDistance = 0;
        }
        player.onUpdateAbilities();
    }

    /**
     * Called every server tick for world-level housekeeping.
     * Periodically purges expired noise timestamps so they don't accumulate
     * when no player is wearing the Warden Chestplate.
     */
    public static void tickWorld() {
        armorAbilityTickCounter++;
        if (armorAbilityTickCounter < NOISE_CLEANUP_INTERVAL) return;
        armorAbilityTickCounter = 0;
        long now = System.currentTimeMillis();
        entityNoiseTimestamps.entrySet().removeIf(e -> now - e.getValue() > NOISE_FADE_TIME_MS);
    }

    // Track death save cooldowns
    private static final Map<UUID, Long> deathSaveCooldowns = new HashMap<>();

    public static boolean trySlimeBootsDeathSave(ServerPlayer player) {
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (!isSlimeBoots(boots)) return false;

        UUID uuid = player.getUUID();
        long now = System.currentTimeMillis();
        long lastSave = deathSaveCooldowns.getOrDefault(uuid, 0L);

        // 5 minute cooldown
        if (now - lastSave < 300000) return false;

        deathSaveCooldowns.put(uuid, now);

        // Save the player
        player.setHealth(1.0f);

        // Apply shrink effect (slowness + jump boost to simulate smaller hitbox feel)
// Example - in ArmorAbilityHandler.java, change all effect applications:
        player.addEffect(new MobEffectInstance(
                MobEffects.SLOWNESS,
                100,
                1,
                true,   // ambient - true hides from HUD
                false,  // showParticles
                false   // showIcon - this hides from side of screen
        ));

        player.addEffect(new MobEffectInstance(
                MobEffects.JUMP_BOOST,
                100,
                3,
                true,   // ambient - true hides from HUD
                false,  // showParticles
                false   // showIcon - this hides from side of screen
        ));
        player.addEffect(new MobEffectInstance(
                MobEffects.RESISTANCE,
                60,
                2,
                true,   // ambient - true hides from HUD
                false,  // showParticles
                false   // showIcon - this hides from side of screen
        ));

        // Visual/audio feedback
        ServerLevel world = player.level();
        world.playSound(null, player.blockPosition(),
                net.minecraft.sounds.SoundEvents.SLIME_SQUISH,
                net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.5f);

        player.sendSystemMessage(Component.literal("§a§lSLIME SAVE! §r§7You bounced back from death!"), false);
        player.sendSystemMessage(Component.literal("§7Cooldown: §e5 minutes"), false);

        return true;
    }

    // ============================================================
    // VENOMOUS LEGGINGS (Spider Slayer)
    // ============================================================
    public static void tickVenomousLeggings(ServerPlayer player) {
        ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
        if (!isVenomousLeggings(leggings)) return;

        // Passive 1: Poison immunity - remove poison if player has it
        if (player.hasEffect(MobEffects.POISON)) {
            player.removeEffect(MobEffects.POISON);
        }

        // Passive 2: Nearby enemies get poisoned (every 40 ticks = 2 seconds)
        if (player.tickCount % 40 == 0) {
            ServerLevel world = player.level();
            for (LivingEntity entity : world.getEntitiesOfClass(
                    LivingEntity.class,
                    player.getBoundingBox().inflate(4.0),
                    e -> e != player && e instanceof Monster && e.isAlive())) {
                entity.addEffect(new MobEffectInstance(
                        MobEffects.POISON, 80, 1, false, true, true
                ));
            }
        }

        // Passive 3: Speed boost when poisoning enemies
        if (!player.hasEffect(MobEffects.SPEED)) {
            ServerLevel world = player.level();
            long poisonedNearby = world.getEntitiesOfClass(
                    LivingEntity.class,
                    player.getBoundingBox().inflate(6.0),
                    e -> e != player && e.hasEffect(MobEffects.POISON)
            ).size();

            if (poisonedNearby > 0) {
                int amplifier = (int) Math.min(2, poisonedNearby - 1);
                player.addEffect(new MobEffectInstance(
                        MobEffects.SPEED, 60, amplifier, false, true, true
                ));
            }
        }
    }

    private static boolean isVenomousLeggings(ItemStack stack) {
        return SlayerItems.isSpiderLeggings(stack);
    }

    // ============================================================
    // SLIME BOOTS (Slime Slayer)
    // ============================================================
    private static void tickSlimeBoots(ServerPlayer player) {
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (!isSlimeBoots(boots)) return;

        // Passive 1: No fall damage (handled via damage event, but also give slow falling)
        if (player.fallDistance > 3.0f && !player.hasEffect(MobEffects.SLOW_FALLING)) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.SLOW_FALLING, 40, 0, true, false, false
            ));
        }

        // Passive 2: Jump boost
        if (!player.hasEffect(MobEffects.JUMP_BOOST)) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.JUMP_BOOST, 40, 1, true, false, false
            ));
        }
    }

    private static boolean isSlimeBoots(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Component customName = stack.get(DataComponents.CUSTOM_NAME);
        if (customName == null) return false;
        String name = customName.getString();
        return name.contains("Slime") && name.contains("Boots");
    }

    public static void detectEntityNoise(ServerLevel world) {
        if (world.players().isEmpty()) return;

        for (ServerPlayer player : world.players()) {
            ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
            if (!isWardenChestplate(chestplate)) continue;

            if (player.getBoundingBox() == null) continue;

            AABB searchBox = player.getBoundingBox().inflate(64.0);

            for (LivingEntity entity : world.getEntitiesOfClass(
                    LivingEntity.class,
                    searchBox,
                    e -> e != player && e.isAlive())) {

                boolean madeNoise = false;

                // 1. Entity is sprinting
                if (entity.isSprinting()) madeNoise = true;

                // 2. Entity is attacking
                if (entity.isSwinging()) madeNoise = true;

                // 3. Entity is moving (very low threshold to catch walking)
                double velocitySq = entity.getDeltaMovement().horizontalDistanceSqr();
                if (velocitySq > 0.0001) madeNoise = true;

                // 4. Entity took damage
                if (entity.hurtTime > 0) madeNoise = true;

                // 5. Hostile mobs chasing a target always make noise
                if (entity instanceof Monster hostile) {
                    if (hostile.getTarget() != null) {
                        madeNoise = true;
                    }
                }

                // 6. Any mob that's not sneaking and is on ground with velocity
                if (entity.onGround() && !entity.isShiftKeyDown() && velocitySq > 0.00001) {
                    madeNoise = true;
                }

                if (madeNoise) {
                    entityNoiseTimestamps.put(entity.getUUID(), System.currentTimeMillis());
                }
            }
        }
    }


    // ============================================================
    // WARDEN CHESTPLATE (Warden Slayer)
    // ============================================================


    private static void tickWardenChestplate(ServerPlayer player) {
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!isWardenChestplate(chestplate)) {
            return;
        }

        // Passive 1: Darkness immunity — suppressed during Ender Phase Mode (which intentionally applies Darkness)
        if (player.hasEffect(MobEffects.DARKNESS) && !CustomItemHandler.isInEnderPhaseMode(player.getUUID())) {
            player.removeEffect(MobEffects.DARKNESS);
        }

        // Passive 2: Night vision
        MobEffectInstance currentNightVision = player.getEffect(MobEffects.NIGHT_VISION);
        if (currentNightVision == null || currentNightVision.getDuration() < 220) {
// Example - in ArmorAbilityHandler.java, change all effect applications:
            player.addEffect(new MobEffectInstance(
                    MobEffects.NIGHT_VISION,
                    400,
                    0,
                    true,   // ambient - true hides from HUD
                    false,  // showParticles
                    false   // showIcon - this hides from side of screen
            ));
        }

        // Passive 3: ESP - check every 10 ticks
        if (player.tickCount % 10 == 0) {
            ServerLevel world = player.level();
            long now = System.currentTimeMillis();

            // Clean up old entries (older than 5 seconds)
            entityNoiseTimestamps.entrySet().removeIf(entry ->
                    now - entry.getValue() > NOISE_FADE_TIME_MS
            );

            // Make sure player has valid bounding box
            if (player.getBoundingBox() == null) return;

            // Apply glowing to entities that made noise recently (64 block range)
            for (LivingEntity entity : world.getEntitiesOfClass(
                    LivingEntity.class,
                    player.getBoundingBox().inflate(64.0),
                    e -> e != player && e.isAlive())) {

                UUID entityId = entity.getUUID();
                Long lastNoise = entityNoiseTimestamps.get(entityId);

                if (lastNoise != null && (now - lastNoise) <= NOISE_FADE_TIME_MS) {
                    entity.addEffect(new MobEffectInstance(
                            MobEffects.GLOWING, 30, 0, true, false, false
                    ));
                }
            }

        }
    }

    public static void onEntityMakeNoise(LivingEntity entity, ServerLevel world) {
        // Check if any nearby player has Warden Chestplate
            for (ServerPlayer player : world.players()) {
            if (player.distanceToSqr(entity) > 576) continue; // 24 blocks squared

            ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
            if (isWardenChestplate(chestplate)) {
                // Register noise for this entity
                entityNoiseTimestamps.put(entity.getUUID(), System.currentTimeMillis());
            }
        }
    }

    private static boolean isWardenChestplate(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Component customName = stack.get(DataComponents.CUSTOM_NAME);
        if (customName == null) return false;
        String name = customName.getString();
        return name.contains("Warden") || name.contains("Sculk");
    }

    // ============================================================
    // BERSERKER HELMET (Zombie Slayer)
    // ============================================================
    private static void tickBerserkerHelmet(ServerPlayer player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!isBerserkerHelmet(helmet)) return;

        // Check level requirement
        if (!SlayerItems.canUseZombieBerserkerHelmet(player)) return;

        // NOTE: Damage buff (+300%) is handled via BerserkerDamageMixin (melee only).
        // No passive effects applied here — the helmet's power comes solely from the damage mixin.
    }

    private static boolean isBerserkerHelmet(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Component customName = stack.get(DataComponents.CUSTOM_NAME);
        if (customName == null) return false;
        String name = customName.getString();
        return name.contains("Berserker") && name.contains("Helmet");
    }

    // ============================================================
    // FULL SET DETECTION HELPER
    // ============================================================
    private static boolean hasFullSet(ServerPlayer player, String setPrefix) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);

        String helmetId = SlayerItems.getCustomItemId(helmet);
        String chestplateId = SlayerItems.getCustomItemId(chestplate);
        String leggingsId = SlayerItems.getCustomItemId(leggings);
        String bootsId = SlayerItems.getCustomItemId(boots);

        return helmetId != null && helmetId.startsWith(setPrefix) &&
               chestplateId != null && chestplateId.startsWith(setPrefix) &&
               leggingsId != null && leggingsId.startsWith(setPrefix) &&
               bootsId != null && bootsId.startsWith(setPrefix);
    }

    // ============================================================
    // CRIMSON STEM SET - Nether Regeneration
    // ============================================================
    private static void tickCrimsonStemSet(ServerPlayer player) {
        if (!hasFullSet(player, "crimson_stem")) return;
        if (player.tickCount % 40 == 0) {
            player.addEffect(new MobEffectInstance(
                MobEffects.REGENERATION, 60, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // CRYING OBSIDIAN SET - Portal Teleport
    // ============================================================
    private static void tickCryingObsidianSet(ServerPlayer player) {
        if (!hasFullSet(player, "crying_obsidian")) return;
        if (player.tickCount % 60 == 0) {
            player.addEffect(new MobEffectInstance(
                MobEffects.RESISTANCE, 40, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // GILDED BLACKSTONE SET - Gold Fortune
    // ============================================================
    private static void tickGildedBlackstoneSet(ServerPlayer player) {
        if (!hasFullSet(player, "gilded_blackstone")) return;
        if (player.tickCount % 60 == 0) {
            player.addEffect(new MobEffectInstance(
                MobEffects.LUCK, 40, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // GRANITE SET - Stone Strength
    // ============================================================
    private static void tickGraniteSet(ServerPlayer player) {
        if (!hasFullSet(player, "granite")) return;
        if (!player.hasEffect(MobEffects.RESISTANCE)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.RESISTANCE, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // DIORITE SET - Shining Protection
    // ============================================================
    private static void tickDioriteSet(ServerPlayer player) {
        if (!hasFullSet(player, "diorite")) return;
        if (!player.hasEffect(MobEffects.GLOWING)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.GLOWING, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // ANDESITE SET - Mountain Resilience
    // ============================================================
    private static void tickAndesiteSet(ServerPlayer player) {
        if (!hasFullSet(player, "andesite")) return;
        if (!player.hasEffect(MobEffects.SLOWNESS)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.SLOWNESS, 100, 0, true, false, false
            ));
        }
        if (!player.hasEffect(MobEffects.RESISTANCE)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.RESISTANCE, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // POLISHED GRANITE SET - Polished Power
    // ============================================================
    private static void tickPolishedGraniteSet(ServerPlayer player) {
        if (!hasFullSet(player, "polished_granite")) return;
        if (!player.hasEffect(MobEffects.STRENGTH)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.STRENGTH, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // POLISHED DIORITE SET - Elegant Speed
    // ============================================================
    private static void tickPolishedDioriteSet(ServerPlayer player) {
        if (!hasFullSet(player, "polished_diorite")) return;
        if (!player.hasEffect(MobEffects.SPEED)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.SPEED, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // POLISHED ANDESITE SET - Smooth Defense
    // ============================================================
    private static void tickPolishedAndesiteSet(ServerPlayer player) {
        if (!hasFullSet(player, "polished_andesite")) return;
        if (!player.hasEffect(MobEffects.RESISTANCE)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.RESISTANCE, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // PACKED ICE SET - Frozen Walk
    // ============================================================
    private static void tickPackedIceSet(ServerPlayer player) {
        if (!hasFullSet(player, "packed_ice")) return;
        if (!player.hasEffect(MobEffects.SPEED)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.SPEED, 100, 1, true, false, false
            ));
        }
    }

    // ============================================================
    // BLUE ICE SET - Deep Freeze
    // ============================================================
    private static void tickBlueIceSet(ServerPlayer player) {
        if (!hasFullSet(player, "blue_ice")) return;
        if (!player.hasEffect(MobEffects.SPEED)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.SPEED, 100, 2, true, false, false
            ));
        }
        if (!player.hasEffect(MobEffects.DOLPHINS_GRACE)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.DOLPHINS_GRACE, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // NETHER GOLD ORE SET - Gold Rush
    // ============================================================
    private static void tickNetherGoldOreSet(ServerPlayer player) {
        if (!hasFullSet(player, "nether_gold_ore")) return;
        if (!player.hasEffect(MobEffects.LUCK)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.LUCK, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // DARK OAK LOG SET - Forest Stealth
    // ============================================================
    private static void tickDarkOakLogSet(ServerPlayer player) {
        if (!hasFullSet(player, "dark_oak_log")) return;
        if (!player.hasEffect(MobEffects.INVISIBILITY)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.INVISIBILITY, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // JUNGLE LOG SET - Jungle Agility
    // ============================================================
    private static void tickJungleLogSet(ServerPlayer player) {
        if (!hasFullSet(player, "jungle_log")) return;
        if (!player.hasEffect(MobEffects.JUMP_BOOST)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.JUMP_BOOST, 100, 1, true, false, false
            ));
        }
    }

    // ============================================================
    // ACACIA LOG SET - Savanna Speed
    // ============================================================
    private static void tickAcaciaLogSet(ServerPlayer player) {
        if (!hasFullSet(player, "acacia_log")) return;
        if (!player.hasEffect(MobEffects.SPEED)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.SPEED, 100, 1, true, false, false
            ));
        }
    }

    // ============================================================
    // MANGROVE LOG SET - Swamp Survival
    // ============================================================
    private static void tickMangroveLogSet(ServerPlayer player) {
        if (!hasFullSet(player, "mangrove_log")) return;
        if (!player.hasEffect(MobEffects.WATER_BREATHING)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.WATER_BREATHING, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // CHERRY LOG SET - Cherry Blossom
    // ============================================================
    private static void tickCherryLogSet(ServerPlayer player) {
        if (!hasFullSet(player, "cherry_log")) return;
        if (!player.hasEffect(MobEffects.LUCK)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.LUCK, 100, 1, true, false, false
            ));
        }
    }

    // ============================================================
    // BAMBOO BLOCK SET - Bouncy Step
    // ============================================================
    private static void tickBambooBlockSet(ServerPlayer player) {
        if (!hasFullSet(player, "bamboo_block")) return;
        if (!player.hasEffect(MobEffects.JUMP_BOOST)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.JUMP_BOOST, 100, 2, true, false, false
            ));
        }
    }

    // ============================================================
    // TUFF SET - Stone Guardian
    // ============================================================
    private static void tickTuffSet(ServerPlayer player) {
        if (!hasFullSet(player, "tuff")) return;
        if (!player.hasEffect(MobEffects.RESISTANCE)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.RESISTANCE, 100, 1, true, false, false
            ));
        }
    }

    // ============================================================
    // POLISHED TUFF SET - Refined Protection
    // ============================================================
    private static void tickPolishedTuffSet(ServerPlayer player) {
        if (!hasFullSet(player, "polished_tuff")) return;
        if (!player.hasEffect(MobEffects.RESISTANCE)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.RESISTANCE, 100, 0, true, false, false
            ));
        }
        if (!player.hasEffect(MobEffects.RESISTANCE)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.RESISTANCE, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // NETHER WART BLOCK SET - Wart Growth
    // ============================================================
    private static void tickNetherWartBlockSet(ServerPlayer player) {
        if (!hasFullSet(player, "nether_wart_block")) return;
        if (player.tickCount % 40 == 0) {
            player.addEffect(new MobEffectInstance(
                MobEffects.REGENERATION, 60, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // WARPED WART BLOCK SET - Warped Speed
    // ============================================================
    private static void tickWarpedWartBlockSet(ServerPlayer player) {
        if (!hasFullSet(player, "warped_wart_block")) return;
        if (!player.hasEffect(MobEffects.SPEED)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.SPEED, 100, 1, true, false, false
            ));
        }
        if (!player.hasEffect(MobEffects.RESISTANCE)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.RESISTANCE, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // CHISELED DEEPSLATE SET - Deepslate Defense
    // ============================================================
    private static void tickChiseledDeepslateSet(ServerPlayer player) {
        if (!hasFullSet(player, "chiseled_deepslate")) return;
        if (!player.hasEffect(MobEffects.RESISTANCE)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.RESISTANCE, 100, 1, true, false, false
            ));
        }
    }

    // ============================================================
    // REINFORCED DEEPSLATE SET - Ultimate Defense
    // ============================================================
    private static void tickReinforcedDeepslateSet(ServerPlayer player) {
        if (!hasFullSet(player, "reinforced_deepslate")) return;
        if (!player.hasEffect(MobEffects.RESISTANCE)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.RESISTANCE, 100, 2, true, false, false
            ));
        }
    }

    // ============================================================
    // CHISELED NETHER BRICK SET - Ancient Strength
    // ============================================================
    private static void tickChiseledNetherBrickSet(ServerPlayer player) {
        if (!hasFullSet(player, "chiseled_nether_brick")) return;
        if (!player.hasEffect(MobEffects.STRENGTH)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.STRENGTH, 100, 1, true, false, false
            ));
        }
    }

    // ============================================================
    // CRACKED NETHER BRICK SET - Broken Power
    // ============================================================
    private static void tickCrackedNetherBrickSet(ServerPlayer player) {
        if (!hasFullSet(player, "cracked_nether_brick")) return;
        if (!player.hasEffect(MobEffects.STRENGTH)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.STRENGTH, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // CHISELED STONE BRICK SET - Carved Stone
    // ============================================================
    private static void tickChiseledStoneBrickSet(ServerPlayer player) {
        if (!hasFullSet(player, "chiseled_stone_brick")) return;
        if (!player.hasEffect(MobEffects.RESISTANCE)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.RESISTANCE, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // CRACKED STONE BRICK SET - Weathered Might
    // ============================================================
    private static void tickCrackedStoneBrickSet(ServerPlayer player) {
        if (!hasFullSet(player, "cracked_stone_brick")) return;
        if (!player.hasEffect(MobEffects.STRENGTH)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.STRENGTH, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // END STONE BRICK SET - End Protection
    // ============================================================
    private static void tickEndStoneBrickSet(ServerPlayer player) {
        if (!hasFullSet(player, "end_stone_brick")) return;
        if (!player.hasEffect(MobEffects.RESISTANCE)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.RESISTANCE, 100, 1, true, false, false
            ));
        }
    }

    // ============================================================
    // RED SANDSTONE SET - Desert Strength
    // ============================================================
    private static void tickRedSandstoneSet(ServerPlayer player) {
        if (!hasFullSet(player, "red_sandstone")) return;
        if (!player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.FIRE_RESISTANCE, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // RAW IRON BLOCK SET - Iron Fortitude
    // ============================================================
    private static void tickRawIronBlockSet(ServerPlayer player) {
        if (!hasFullSet(player, "raw_iron_block")) return;
        if (!player.hasEffect(MobEffects.RESISTANCE)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.RESISTANCE, 100, 1, true, false, false
            ));
        }
    }

    // ============================================================
    // RAW GOLD BLOCK SET - Golden Fortune
    // ============================================================
    private static void tickRawGoldBlockSet(ServerPlayer player) {
        if (!hasFullSet(player, "raw_gold_block")) return;
        if (!player.hasEffect(MobEffects.LUCK)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.LUCK, 100, 2, true, false, false
            ));
        }
    }

    // ============================================================
    // PRISMARINE BRICKS SET - Ocean Might
    // ============================================================
    private static void tickPrismarineBricksSet(ServerPlayer player) {
        if (!hasFullSet(player, "prismarine_bricks")) return;
        if (!player.hasEffect(MobEffects.WATER_BREATHING)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.WATER_BREATHING, 100, 0, true, false, false
            ));
        }
        if (!player.hasEffect(MobEffects.DOLPHINS_GRACE)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.DOLPHINS_GRACE, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // DARK PRISMARINE SET - Deep Ocean
    // ============================================================
    private static void tickDarkPrismarineSet(ServerPlayer player) {
        if (!hasFullSet(player, "dark_prismarine")) return;
        if (!player.hasEffect(MobEffects.WATER_BREATHING)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.WATER_BREATHING, 100, 0, true, false, false
            ));
        }
        if (!player.hasEffect(MobEffects.NIGHT_VISION)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.NIGHT_VISION, 200, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // SEA LANTERN SET - Light in Darkness
    // ============================================================
    private static void tickSeaLanternSet(ServerPlayer player) {
        if (!hasFullSet(player, "sea_lantern")) return;
        if (!player.hasEffect(MobEffects.GLOWING)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.GLOWING, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // LODESTONE SET - Magnetic Field
    // ============================================================
    private static void tickLodestoneSet(ServerPlayer player) {
        if (!hasFullSet(player, "lodestone")) return;
        if (!player.hasEffect(MobEffects.RESISTANCE)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.RESISTANCE, 100, 1, true, false, false
            ));
        }
    }

    // ============================================================
    // BLACKSTONE BRICKS SET - Nether Brick Power
    // ============================================================
    private static void tickBlackstoneBricksSet(ServerPlayer player) {
        if (!hasFullSet(player, "blackstone_bricks")) return;
        if (!player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.FIRE_RESISTANCE, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // POLISHED BLACKSTONE SET - Obsidian Shield
    // ============================================================
    private static void tickPolishedBlackstoneSet(ServerPlayer player) {
        if (!hasFullSet(player, "polished_blackstone")) return;
        if (!player.hasEffect(MobEffects.RESISTANCE)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.RESISTANCE, 100, 1, true, false, false
            ));
        }
    }

    // ============================================================
    // SMOOTH BASALT SET - Volcanic Defense
    // ============================================================
    private static void tickSmoothBasaltSet(ServerPlayer player) {
        if (!hasFullSet(player, "smooth_basalt")) return;
        if (!player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.FIRE_RESISTANCE, 100, 0, true, false, false
            ));
        }
        if (!player.hasEffect(MobEffects.RESISTANCE)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.RESISTANCE, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // AMETHYST CLUSTER SET - Crystal Harmony
    // ============================================================
    private static void tickAmethystClusterSet(ServerPlayer player) {
        if (!hasFullSet(player, "amethyst_cluster")) return;
        if (!player.hasEffect(MobEffects.REGENERATION)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.REGENERATION, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // OBSIDIAN BLOCK SET - Bedrock Strength
    // ============================================================
    private static void tickObsidianBlockSet(ServerPlayer player) {
        if (!hasFullSet(player, "obsidian_block")) return;
        if (!player.hasEffect(MobEffects.RESISTANCE)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.RESISTANCE, 100, 2, true, false, false
            ));
        }
    }

    // ============================================================
    // DRAGON CHESTPLATE ABILITIES - Special Items
    // ============================================================

    // Inferno Dragon Chestplate - Fire Aura
    public static void tickInfernoDragonChestplate(ServerPlayer player) {
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestplate.isEmpty() || !DragonChestplates.hasCustomItemId(chestplate, "inferno_dragon_chestplate")) return;

        ServerLevel world = (ServerLevel) player.level();

        // Fire resistance (immunity to fire)
        if (!player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.FIRE_RESISTANCE, 200, 0, true, false, false
            ));
        }

        // Explosion at 2.5 hearts (5 HP)
        if (player.getHealth() <= 5f) {
            // Create explosion at player position
            world.explode(player, player.getX(), player.getY(), player.getZ(), 2.0f, Level.ExplosionInteraction.NONE);
            player.igniteForSeconds(8);
        }

        // Double damage when on fire
        if (player.isOnFire() && !player.hasEffect(MobEffects.STRENGTH)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.STRENGTH, 100, 0, true, false, false
            ));
        }

        // Double stats in Nether
        if (world.dimension() == Level.NETHER) {
            if (!player.hasEffect(MobEffects.RESISTANCE)) {
                player.addEffect(new MobEffectInstance(
                    MobEffects.RESISTANCE, 100, 1, true, false, false
                ));
            }
            if (!player.hasEffect(MobEffects.SPEED)) {
                player.addEffect(new MobEffectInstance(
                    MobEffects.SPEED, 100, 1, true, false, false
                ));
            }
        }

        if (player.tickCount % 8 == 0) {
            double x = player.getX() - player.getLookAngle().x * 0.4;
            double y = player.getY() + 0.2;
            double z = player.getZ() - player.getLookAngle().z * 0.4;
            world.sendParticles(ParticleTypes.SMALL_FLAME, x, y, z, 1, 0, 0, 0, 0);
            world.sendParticles(ParticleTypes.SMOKE, x, y, z, 1, 0, 0.02, 0, 0);
        }

        // Heal to max health when health increases
        UUID uuid = player.getUUID();
        float prevMax = previousMaxHealth.getOrDefault(uuid, 20f);
        float targetMax = 20f;
        if (world.dimension() == Level.NETHER) {
            targetMax = 40f; // 2x health in Nether
        }
        if (player.getMaxHealth() != targetMax) {
            player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(targetMax);
            if (targetMax > prevMax && player.getHealth() < targetMax) {
                player.setHealth(targetMax);
            }
        }
        previousMaxHealth.put(uuid, targetMax);
    }

    // Storm Dragon Chestplate - Thunder Aura + Speed
    public static void tickStormDragonChestplate(ServerPlayer player) {
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestplate.isEmpty() || !DragonChestplates.hasCustomItemId(chestplate, "storm_dragon_chestplate")) return;

        ServerLevel world = (ServerLevel) player.level();
        UUID uuid = player.getUUID();

        // Speed boost
        if (!player.hasEffect(MobEffects.SPEED)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.SPEED, 100, 2, true, false, false
            ));
        }

        // Lightning immunity (Resistance effect)
        if (!player.hasEffect(MobEffects.RESISTANCE)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.RESISTANCE, 100, 0, true, false, false
            ));
        }

        var abilities = player.getAbilities();
        abilities.mayfly = true;

        boolean wasFlying = previousFlying.getOrDefault(uuid, false);
        boolean isFlying = abilities.flying;
        previousFlying.put(uuid, isFlying);

        if (wasFlying && !isFlying && !player.onGround()) {
            grantFallImmunity(uuid);
        }
        if (isFlying) {
            fallImmunityPlayers.remove(uuid);
        }

        float prevMax = previousMaxHealth.getOrDefault(uuid, 24f);
        float targetHealth = world.isThundering() ? 48f : 24f;
        if (isFlying) {
            targetHealth = 2f;
            player.fallDistance = 0;
        }

        // Update max health and heal if needed
        if (player.getMaxHealth() != targetHealth) {
            player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(targetHealth);
            // Heal when health increases (e.g., stopping flight) or when health is below new max
            if (player.getHealth() < targetHealth) {
                player.setHealth(targetHealth);
            }
        }
        previousMaxHealth.put(uuid, targetHealth);

        player.onUpdateAbilities();
    }

    // Void Dragon Chestplate - Void Aura + Life Steal
    public static void tickVoidDragonChestplate(ServerPlayer player) {
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestplate.isEmpty() || !DragonChestplates.hasCustomItemId(chestplate, "void_dragon_chestplate")) return;

        ServerLevel world = (ServerLevel) player.level();
        UUID uuid = player.getUUID();

        // Resistance + Strength
        if (!player.hasEffect(MobEffects.RESISTANCE)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.RESISTANCE, 100, 2, true, false, false
            ));
        }
        if (!player.hasEffect(MobEffects.STRENGTH)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.STRENGTH, 100, 1, true, false, false
            ));
        }

        // Invisibility on crouch (15 seconds)
        if (player.isShiftKeyDown() && !player.hasEffect(MobEffects.INVISIBILITY)) {
            player.addEffect(new MobEffectInstance(
                MobEffects.INVISIBILITY, 300, 0, true, false, false
            ));
        }

        // Creative flight in The End (no health penalty)
        if (world.dimension() == Level.END) {
            var abilities = player.getAbilities();
            abilities.mayfly = true;

            boolean wasFlying = previousFlying.getOrDefault(uuid, false);
            boolean isFlying = abilities.flying;
            previousFlying.put(uuid, isFlying);

            if (wasFlying && !isFlying && !player.onGround()) {
                grantFallImmunity(uuid);
            }
            if (isFlying) {
                player.fallDistance = 0;
                fallImmunityPlayers.remove(uuid);
            }

            player.onUpdateAbilities();
        }

        if (player.tickCount % 7 == 0) {
            double x = player.getX() - player.getLookAngle().x * 0.35;
            double y = player.getY() + 0.18;
            double z = player.getZ() - player.getLookAngle().z * 0.35;
            world.sendParticles(ParticleTypes.REVERSE_PORTAL, x, y, z, 1, 0, 0, 0, 0);
        }

        // Heal to max health when health increases
        float prevMax = previousMaxHealth.getOrDefault(uuid, 30f);
        float targetMax = 30f;
        if (world.dimension() == Level.END) {
            targetMax = 60f; // 2x health in The End
        }
        // Flight drawback: keep the same 1-heart (2 HP) cap while airborne.
        if (world.dimension() == Level.END && player.getAbilities().flying) {
            targetMax = 2f;
        }
        if (player.getMaxHealth() != targetMax) {
            player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(targetMax);
            if (targetMax > prevMax && player.getHealth() < targetMax) {
                player.setHealth(targetMax);
            }
        }
        previousMaxHealth.put(uuid, targetMax);
    }

    public static void tickAuroraDragonChestplate(ServerPlayer player) {
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestplate.isEmpty() || !DragonChestplates.hasCustomItemId(chestplate, "aurora_dragon_chestplate")) return;

        ServerLevel world = (ServerLevel) player.level();
        UUID uuid = player.getUUID();
        var abilities = player.getAbilities();
        abilities.mayfly = true;

        boolean wasFlying = previousFlying.getOrDefault(uuid, false);
        boolean isFlying = abilities.flying;
        previousFlying.put(uuid, isFlying);

        if (wasFlying && !isFlying && !player.onGround()) {
            grantFallImmunity(uuid);
        }
        if (isFlying) {
            fallImmunityPlayers.remove(uuid);
        }

        float targetHealth = world.isRaining() ? 36f : 28f;
        if (isFlying) {
            targetHealth = 2f;
            player.fallDistance = 0;
        }
        if (player.getMaxHealth() != targetHealth) {
            player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(targetHealth);
            if (player.getHealth() > targetHealth) {
                player.setHealth(targetHealth);
            } else if (player.getHealth() < targetHealth && !isFlying) {
                player.setHealth(Math.min(targetHealth, player.getHealth() + 0.05f));
            }
        }
        previousMaxHealth.put(uuid, targetHealth);

        if (world.isRaining()) {
            if (!player.hasEffect(MobEffects.SPEED)) {
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 120, 1, true, false, false));
            }
            if (!player.hasEffect(MobEffects.REGENERATION)) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 0, true, false, false));
            }
        }

        player.onUpdateAbilities();

        if (player.tickCount % 8 == 0) {
            double x = player.getX() - player.getLookAngle().x * 0.3;
            double y = player.getY() + 0.2;
            double z = player.getZ() - player.getLookAngle().z * 0.3;
            world.sendParticles(ParticleTypes.END_ROD, x, y, z, 1, 0, 0, 0, 0);
        }
    }

    public static void tickFrostDragonChestplate(ServerPlayer player) {
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestplate.isEmpty() || !DragonChestplates.hasCustomItemId(chestplate, "frost_dragon_chestplate")) return;

        ServerLevel world = (ServerLevel) player.level();
        BlockState below = world.getBlockState(player.blockPosition().below());
        boolean onCold = below.is(Blocks.SNOW_BLOCK) || below.is(Blocks.ICE) || below.is(Blocks.PACKED_ICE)
                || below.is(Blocks.BLUE_ICE) || below.is(Blocks.SNOW);

        if (world.dimension() == Level.NETHER) {
            player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 120, 1, true, false, false));
        } else if (onCold) {
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 120, 1, true, false, false));
        }

        if (player.tickCount % 10 == 0) {
            double x = player.getX() - player.getLookAngle().x * 0.25;
            double y = player.getY() + 0.15;
            double z = player.getZ() - player.getLookAngle().z * 0.25;
            world.sendParticles(ParticleTypes.SNOWFLAKE, x, y, z, 1, 0, 0, 0, 0);
        }
        if (player.tickCount % 50 == 0) {
            for (LivingEntity e : world.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(3),
                    ent -> ent != player && ent instanceof Monster)) {
                e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0, true, false, false));
            }
        }
    }

    public static void tickVenomDragonChestplate(ServerPlayer player) {
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestplate.isEmpty() || !DragonChestplates.hasCustomItemId(chestplate, "venom_dragon_chestplate")) return;

        ServerLevel world = (ServerLevel) player.level();
        if (player.hasEffect(MobEffects.POISON)) {
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 80, 0, true, false, false));
        }

        if (player.tickCount % 90 == 0) {
            for (Entity e : world.getEntitiesOfClass(Entity.class, player.getBoundingBox().inflate(3.5), ent -> ent != player)) {
                if (e instanceof Monster hostile) {
                    hostile.addEffect(new MobEffectInstance(MobEffects.POISON, 50, 0, true, false, true));
                }
            }
        }

        if (player.tickCount % 12 == 0) {
            double x = player.getX() - player.getLookAngle().x * 0.3;
            double y = player.getY() + 0.12;
            double z = player.getZ() - player.getLookAngle().z * 0.3;
            world.sendParticles(ParticleTypes.ITEM_SLIME, x, y, z, 1, 0, 0, 0, 0);
        }
    }

    public static void tickBloodDragonChestplate(ServerPlayer player) {
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestplate.isEmpty() || !DragonChestplates.hasCustomItemId(chestplate, "blood_dragon_chestplate")) return;

        ServerLevel world = (ServerLevel) player.level();
        float hp = player.getHealth();
        float max = player.getMaxHealth();
        if (hp < max * 0.35f && player.tickCount % 55 == 0) {
            player.addEffect(new MobEffectInstance(MobEffects.WITHER, 45, 0, false, false, true));
        }
        if (!player.hasEffect(MobEffects.STRENGTH)) {
            player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 120, 0, true, false, false));
        }

        if (player.tickCount % 10 == 0) {
            double x = player.getX() - player.getLookAngle().x * 0.35;
            double y = player.getY() + 0.25;
            double z = player.getZ() - player.getLookAngle().z * 0.35;
            world.sendParticles(ParticleTypes.DAMAGE_INDICATOR, x, y, z, 1, 0, 0, 0, 0);
        }
    }

    public static void tickEclipseDragonChestplate(ServerPlayer player) {
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestplate.isEmpty() || !DragonChestplates.hasCustomItemId(chestplate, "eclipse_dragon_chestplate")) return;

        ServerLevel world = (ServerLevel) player.level();
        boolean night = world.dimensionType().hasSkyLight()
                ? world.getBrightness(LightLayer.SKY, player.blockPosition()) < 4
                : true;

        if (night) {
            applyConditionalCreativeFlight(player, true);
            MobEffectInstance nv = player.getEffect(MobEffects.NIGHT_VISION);
            if (nv == null || nv.getDuration() < 220) {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0, false, false, false));
            }
            if (player.getAbilities().flying && player.tickCount % 35 == 0) {
                player.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0, false, false, true));
            }
        } else {
            applyConditionalCreativeFlight(player, false);
            if (player.getY() > (double) world.getSeaLevel() + 2 && world.canSeeSky(player.blockPosition())) {
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0, true, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 50, 0, true, false, true));
            }
        }

        if (player.tickCount % 9 == 0) {
            double x = player.getX() - player.getLookAngle().x * 0.3;
            double y = player.getY() + 0.15;
            double z = player.getZ() - player.getLookAngle().z * 0.3;
            world.sendParticles(ParticleTypes.SMOKE, x, y, z, 1, 0, 0, 0, 0);
            if (night) {
                world.sendParticles(ParticleTypes.REVERSE_PORTAL, x, y, z, 1, 0, 0, 0, 0);
            }
        }
    }

    public static void onVenomDragonHit(ServerPlayer player, LivingEntity target) {
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestplate.isEmpty() || !DragonChestplates.hasCustomItemId(chestplate, "venom_dragon_chestplate")) return;
        target.addEffect(new MobEffectInstance(MobEffects.POISON, 70, 1, true, false, true));
    }

    public static void onBloodDragonHit(ServerPlayer player, LivingEntity target) {
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestplate.isEmpty() || !DragonChestplates.hasCustomItemId(chestplate, "blood_dragon_chestplate")) return;
        player.heal(1.0f);
    }

    // Void Dragon - blink is handled by DragonPlateHandler; this method is a leftover hook.
    public static float onVoidDragonDamage(ServerPlayer player, DamageSource source, float amount) {
        return amount;
    }

    // Storm Dragon - Lightning strike on hit
    public static void onStormDragonHit(ServerPlayer player, LivingEntity target) {
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestplate.isEmpty() || !DragonChestplates.hasCustomItemId(chestplate, "storm_dragon_chestplate")) return;

        // 1% chance to strike target with lightning
        if (Math.random() < 0.01) {
            ServerLevel world = (ServerLevel) player.level();
            net.minecraft.world.entity.LightningBolt lightning = net.minecraft.world.entity.EntityTypes.LIGHTNING_BOLT.create(world, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
            if (lightning != null) {
                lightning.setPos(target.getX(), target.getY(), target.getZ());
                world.addFreshEntity(lightning);
            }
        }
    }

    private static void checkChestplateUnequip(ServerPlayer player) {
        UUID uuid = player.getUUID();
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        String currentId = DragonChestplates.getCustomItemId(chestplate);

        String previousId = previousChestplate.get(uuid);

        // Check if player had a dragon chestplate and unequipped it
        if (previousId != null && previousId.contains("dragon_chestplate") && 
            (currentId == null || !currentId.contains("dragon_chestplate"))) {
            var abilities = player.getAbilities();
            abilities.mayfly = false;
            abilities.flying = false;
            player.onUpdateAbilities();

            // Remove from fall immunity
            fallImmunityPlayers.remove(uuid);
            previousFlying.remove(uuid);
        }

        // Update tracking
        previousChestplate.put(uuid, currentId);
    }

    private static void tickFallImmunity(ServerPlayer player) {
        UUID uuid = player.getUUID();

        // If player has fall immunity and is on ground, remove it
        if (fallImmunityPlayers.contains(uuid) && player.onGround()) {
            fallImmunityPlayers.remove(uuid);
        }
    }

    // Check if player has fall damage immunity (called from damage mixin)
    public static boolean hasFallImmunity(UUID uuid) {
        return fallImmunityPlayers.contains(uuid);
    }

    // Grant fall damage immunity (called when flight ends)
    public static void grantFallImmunity(UUID uuid) {
        fallImmunityPlayers.add(uuid);
    }

    // Auto-heal player when max health decreases
    private static void autoHealIfNeeded(ServerPlayer player, float newMaxHealth) {
        UUID uuid = player.getUUID();
        Float prevMax = previousMaxHealth.get(uuid);
        
        if (prevMax != null && prevMax > newMaxHealth) {
            // Max health decreased - heal to full
            player.setHealth(newMaxHealth);
        }
        
        previousMaxHealth.put(uuid, newMaxHealth);
    }
}