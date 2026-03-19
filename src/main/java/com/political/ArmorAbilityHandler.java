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
    /** How often (in server ticks) to sweep expired noise timestamps. */
    private static final int NOISE_CLEANUP_INTERVAL = 200; // ~10 seconds
    private static int armorAbilityTickCounter = 0;

    // Call this every tick from PoliticalServer.java
    public static void tick(ServerPlayerEntity player) {
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
    public static void tickVenomousLeggings(ServerPlayerEntity player) {
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
                entity.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.POISON, 80, 1, false, true, true
                ));
            }
        }

        // Passive 3: Speed boost when poisoning enemies
        if (!player.hasStatusEffect(StatusEffects.SPEED)) {
            ServerWorld world = player.getEntityWorld();
            long poisonedNearby = world.getEntitiesByClass(
                    LivingEntity.class,
                    player.getBoundingBox().expand(6.0),
                    e -> e != player && e.hasStatusEffect(StatusEffects.POISON)
            ).size();

            if (poisonedNearby > 0) {
                int amplifier = (int) Math.min(2, poisonedNearby - 1);
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.SPEED, 60, amplifier, false, true, true
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

        // Passive 1: Darkness immunity — suppressed during Ender Phase Mode (which intentionally applies Darkness)
        if (player.hasStatusEffect(StatusEffects.DARKNESS) && !CustomItemHandler.isInEnderPhaseMode(player.getUuid())) {
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

        // Check level requirement
        if (!SlayerItems.canUseZombieBerserkerHelmet(player)) return;

        // NOTE: Damage buff (+300%) is handled via BerserkerDamageMixin (melee only).
        // No passive effects applied here — the helmet's power comes solely from the damage mixin.
    }

    private static boolean isBerserkerHelmet(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Text customName = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (customName == null) return false;
        String name = customName.getString();
        return name.contains("Berserker") && name.contains("Helmet");
    }

    // ============================================================
    // FULL SET DETECTION HELPER
    // ============================================================
    private static boolean hasFullSet(ServerPlayerEntity player, String setPrefix) {
        ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);
        ItemStack chestplate = player.getEquippedStack(EquipmentSlot.CHEST);
        ItemStack leggings = player.getEquippedStack(EquipmentSlot.LEGS);
        ItemStack boots = player.getEquippedStack(EquipmentSlot.FEET);

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
    private static void tickCrimsonStemSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "crimson_stem")) return;
        if (player.age % 40 == 0) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.REGENERATION, 60, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // CRYING OBSIDIAN SET - Portal Teleport
    // ============================================================
    private static void tickCryingObsidianSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "crying_obsidian")) return;
        if (player.age % 60 == 0) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.RESISTANCE, 40, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // GILDED BLACKSTONE SET - Gold Fortune
    // ============================================================
    private static void tickGildedBlackstoneSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "gilded_blackstone")) return;
        if (player.age % 60 == 0) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.LUCK, 40, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // GRANITE SET - Stone Strength
    // ============================================================
    private static void tickGraniteSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "granite")) return;
        if (!player.hasStatusEffect(StatusEffects.RESISTANCE)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.RESISTANCE, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // DIORITE SET - Shining Protection
    // ============================================================
    private static void tickDioriteSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "diorite")) return;
        if (!player.hasStatusEffect(StatusEffects.GLOWING)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.GLOWING, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // ANDESITE SET - Mountain Resilience
    // ============================================================
    private static void tickAndesiteSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "andesite")) return;
        if (!player.hasStatusEffect(StatusEffects.SLOWNESS)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SLOWNESS, 100, 0, true, false, false
            ));
        }
        if (!player.hasStatusEffect(StatusEffects.RESISTANCE)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.RESISTANCE, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // POLISHED GRANITE SET - Polished Power
    // ============================================================
    private static void tickPolishedGraniteSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "polished_granite")) return;
        if (!player.hasStatusEffect(StatusEffects.STRENGTH)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.STRENGTH, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // POLISHED DIORITE SET - Elegant Speed
    // ============================================================
    private static void tickPolishedDioriteSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "polished_diorite")) return;
        if (!player.hasStatusEffect(StatusEffects.SPEED)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SPEED, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // POLISHED ANDESITE SET - Smooth Defense
    // ============================================================
    private static void tickPolishedAndesiteSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "polished_andesite")) return;
        if (!player.hasStatusEffect(StatusEffects.RESISTANCE)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.RESISTANCE, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // PACKED ICE SET - Frozen Walk
    // ============================================================
    private static void tickPackedIceSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "packed_ice")) return;
        if (!player.hasStatusEffect(StatusEffects.SPEED)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SPEED, 100, 1, true, false, false
            ));
        }
    }

    // ============================================================
    // BLUE ICE SET - Deep Freeze
    // ============================================================
    private static void tickBlueIceSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "blue_ice")) return;
        if (!player.hasStatusEffect(StatusEffects.SPEED)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SPEED, 100, 2, true, false, false
            ));
        }
        if (!player.hasStatusEffect(StatusEffects.DOLPHINS_GRACE)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.DOLPHINS_GRACE, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // NETHER GOLD ORE SET - Gold Rush
    // ============================================================
    private static void tickNetherGoldOreSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "nether_gold_ore")) return;
        if (!player.hasStatusEffect(StatusEffects.LUCK)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.LUCK, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // DARK OAK LOG SET - Forest Stealth
    // ============================================================
    private static void tickDarkOakLogSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "dark_oak_log")) return;
        if (!player.hasStatusEffect(StatusEffects.INVISIBILITY)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.INVISIBILITY, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // JUNGLE LOG SET - Jungle Agility
    // ============================================================
    private static void tickJungleLogSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "jungle_log")) return;
        if (!player.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.JUMP_BOOST, 100, 1, true, false, false
            ));
        }
    }

    // ============================================================
    // ACACIA LOG SET - Savanna Speed
    // ============================================================
    private static void tickAcaciaLogSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "acacia_log")) return;
        if (!player.hasStatusEffect(StatusEffects.SPEED)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SPEED, 100, 1, true, false, false
            ));
        }
    }

    // ============================================================
    // MANGROVE LOG SET - Swamp Survival
    // ============================================================
    private static void tickMangroveLogSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "mangrove_log")) return;
        if (!player.hasStatusEffect(StatusEffects.WATER_BREATHING)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.WATER_BREATHING, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // CHERRY LOG SET - Cherry Blossom
    // ============================================================
    private static void tickCherryLogSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "cherry_log")) return;
        if (!player.hasStatusEffect(StatusEffects.LUCK)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.LUCK, 100, 1, true, false, false
            ));
        }
    }

    // ============================================================
    // BAMBOO BLOCK SET - Bouncy Step
    // ============================================================
    private static void tickBambooBlockSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "bamboo_block")) return;
        if (!player.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.JUMP_BOOST, 100, 2, true, false, false
            ));
        }
    }

    // ============================================================
    // TUFF SET - Stone Guardian
    // ============================================================
    private static void tickTuffSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "tuff")) return;
        if (!player.hasStatusEffect(StatusEffects.RESISTANCE)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.RESISTANCE, 100, 1, true, false, false
            ));
        }
    }

    // ============================================================
    // POLISHED TUFF SET - Refined Protection
    // ============================================================
    private static void tickPolishedTuffSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "polished_tuff")) return;
        if (!player.hasStatusEffect(StatusEffects.RESISTANCE)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.RESISTANCE, 100, 0, true, false, false
            ));
        }
        if (!player.hasStatusEffect(StatusEffects.RESISTANCE)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.RESISTANCE, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // NETHER WART BLOCK SET - Wart Growth
    // ============================================================
    private static void tickNetherWartBlockSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "nether_wart_block")) return;
        if (player.age % 40 == 0) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.REGENERATION, 60, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // WARPED WART BLOCK SET - Warped Speed
    // ============================================================
    private static void tickWarpedWartBlockSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "warped_wart_block")) return;
        if (!player.hasStatusEffect(StatusEffects.SPEED)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SPEED, 100, 1, true, false, false
            ));
        }
        if (!player.hasStatusEffect(StatusEffects.RESISTANCE)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.RESISTANCE, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // CHISELED DEEPSLATE SET - Deepslate Defense
    // ============================================================
    private static void tickChiseledDeepslateSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "chiseled_deepslate")) return;
        if (!player.hasStatusEffect(StatusEffects.RESISTANCE)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.RESISTANCE, 100, 1, true, false, false
            ));
        }
    }

    // ============================================================
    // REINFORCED DEEPSLATE SET - Ultimate Defense
    // ============================================================
    private static void tickReinforcedDeepslateSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "reinforced_deepslate")) return;
        if (!player.hasStatusEffect(StatusEffects.RESISTANCE)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.RESISTANCE, 100, 2, true, false, false
            ));
        }
    }

    // ============================================================
    // CHISELED NETHER BRICK SET - Ancient Strength
    // ============================================================
    private static void tickChiseledNetherBrickSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "chiseled_nether_brick")) return;
        if (!player.hasStatusEffect(StatusEffects.STRENGTH)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.STRENGTH, 100, 1, true, false, false
            ));
        }
    }

    // ============================================================
    // CRACKED NETHER BRICK SET - Broken Power
    // ============================================================
    private static void tickCrackedNetherBrickSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "cracked_nether_brick")) return;
        if (!player.hasStatusEffect(StatusEffects.STRENGTH)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.STRENGTH, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // CHISELED STONE BRICK SET - Carved Stone
    // ============================================================
    private static void tickChiseledStoneBrickSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "chiseled_stone_brick")) return;
        if (!player.hasStatusEffect(StatusEffects.RESISTANCE)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.RESISTANCE, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // CRACKED STONE BRICK SET - Weathered Might
    // ============================================================
    private static void tickCrackedStoneBrickSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "cracked_stone_brick")) return;
        if (!player.hasStatusEffect(StatusEffects.STRENGTH)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.STRENGTH, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // END STONE BRICK SET - End Protection
    // ============================================================
    private static void tickEndStoneBrickSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "end_stone_brick")) return;
        if (!player.hasStatusEffect(StatusEffects.RESISTANCE)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.RESISTANCE, 100, 1, true, false, false
            ));
        }
    }

    // ============================================================
    // RED SANDSTONE SET - Desert Strength
    // ============================================================
    private static void tickRedSandstoneSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "red_sandstone")) return;
        if (!player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.FIRE_RESISTANCE, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // RAW IRON BLOCK SET - Iron Fortitude
    // ============================================================
    private static void tickRawIronBlockSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "raw_iron_block")) return;
        if (!player.hasStatusEffect(StatusEffects.RESISTANCE)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.RESISTANCE, 100, 1, true, false, false
            ));
        }
    }

    // ============================================================
    // RAW GOLD BLOCK SET - Golden Fortune
    // ============================================================
    private static void tickRawGoldBlockSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "raw_gold_block")) return;
        if (!player.hasStatusEffect(StatusEffects.LUCK)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.LUCK, 100, 2, true, false, false
            ));
        }
    }

    // ============================================================
    // PRISMARINE BRICKS SET - Ocean Might
    // ============================================================
    private static void tickPrismarineBricksSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "prismarine_bricks")) return;
        if (!player.hasStatusEffect(StatusEffects.WATER_BREATHING)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.WATER_BREATHING, 100, 0, true, false, false
            ));
        }
        if (!player.hasStatusEffect(StatusEffects.DOLPHINS_GRACE)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.DOLPHINS_GRACE, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // DARK PRISMARINE SET - Deep Ocean
    // ============================================================
    private static void tickDarkPrismarineSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "dark_prismarine")) return;
        if (!player.hasStatusEffect(StatusEffects.WATER_BREATHING)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.WATER_BREATHING, 100, 0, true, false, false
            ));
        }
        if (!player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.NIGHT_VISION, 200, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // SEA LANTERN SET - Light in Darkness
    // ============================================================
    private static void tickSeaLanternSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "sea_lantern")) return;
        if (!player.hasStatusEffect(StatusEffects.GLOWING)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.GLOWING, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // LODESTONE SET - Magnetic Field
    // ============================================================
    private static void tickLodestoneSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "lodestone")) return;
        if (!player.hasStatusEffect(StatusEffects.RESISTANCE)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.RESISTANCE, 100, 1, true, false, false
            ));
        }
    }

    // ============================================================
    // BLACKSTONE BRICKS SET - Nether Brick Power
    // ============================================================
    private static void tickBlackstoneBricksSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "blackstone_bricks")) return;
        if (!player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.FIRE_RESISTANCE, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // POLISHED BLACKSTONE SET - Obsidian Shield
    // ============================================================
    private static void tickPolishedBlackstoneSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "polished_blackstone")) return;
        if (!player.hasStatusEffect(StatusEffects.RESISTANCE)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.RESISTANCE, 100, 1, true, false, false
            ));
        }
    }

    // ============================================================
    // SMOOTH BASALT SET - Volcanic Defense
    // ============================================================
    private static void tickSmoothBasaltSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "smooth_basalt")) return;
        if (!player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.FIRE_RESISTANCE, 100, 0, true, false, false
            ));
        }
        if (!player.hasStatusEffect(StatusEffects.RESISTANCE)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.RESISTANCE, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // AMETHYST CLUSTER SET - Crystal Harmony
    // ============================================================
    private static void tickAmethystClusterSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "amethyst_cluster")) return;
        if (!player.hasStatusEffect(StatusEffects.REGENERATION)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.REGENERATION, 100, 0, true, false, false
            ));
        }
    }

    // ============================================================
    // OBSIDIAN BLOCK SET - Bedrock Strength
    // ============================================================
    private static void tickObsidianBlockSet(ServerPlayerEntity player) {
        if (!hasFullSet(player, "obsidian_block")) return;
        if (!player.hasStatusEffect(StatusEffects.RESISTANCE)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.RESISTANCE, 100, 3, true, false, false
            ));
        }
    }
}