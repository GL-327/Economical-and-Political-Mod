package com.political;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

final class MoreDragonChestplateAbilities {

    private static final Map<UUID, Integer> starfallStoredTicks = new HashMap<>();
    private static final Map<UUID, Integer> starfallGroundAccumulator = new HashMap<>();

    private MoreDragonChestplateAbilities() {
    }

    static void tickAll(ServerPlayerEntity player) {
        ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);
        String id = DragonChestplates.getCustomItemId(chest);
        if (id == null) return;
        switch (id) {
            case "dune_dragon_chestplate" -> tickDune(player);
            case "reef_dragon_chestplate" -> tickReef(player);
            case "ember_dragon_chestplate" -> tickEmber(player);
            case "crystal_dragon_chestplate" -> tickCrystal(player);
            case "shadow_dragon_chestplate" -> tickShadow(player);
            case "solar_dragon_chestplate" -> tickSolar(player);
            case "lunar_dragon_chestplate" -> tickLunar(player);
            case "terra_dragon_chestplate" -> tickTerra(player);
            case "tempest_dragon_chestplate" -> tickTempest(player);
            case "auric_dragon_chestplate" -> tickAuric(player);
            case "glacier_dragon_chestplate" -> tickGlacier(player);
            case "mire_dragon_chestplate" -> tickMire(player);
            case "starfall_dragon_chestplate" -> tickStarfall(player);
            case "mirage_dragon_chestplate" -> tickMirage(player);
            case "obsidian_wyrm_chestplate" -> tickObsidianWyrm(player);
            default -> {
            }
        }
    }

    public static void onHit(ServerPlayerEntity player, LivingEntity target) {
        ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);
        String id = DragonChestplates.getCustomItemId(chest);
        if (id == null) return;
        if ("mire_dragon_chestplate".equals(id)) {
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 100, 0, false, true, true));
        }
        if ("reef_dragon_chestplate".equals(id) && target instanceof LivingEntity) {
            if (target.isTouchingWaterOrRain()) {
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 0, false, true, true));
            }
        }
    }

    private static boolean dryBiome(ServerWorld world, BlockPos pos) {
        return world.getBiome(pos).isIn(BiomeTags.IS_BADLANDS)
                || world.getBiome(pos).matchesKey(net.minecraft.world.biome.BiomeKeys.DESERT)
                || world.getBiome(pos).matchesKey(net.minecraft.world.biome.BiomeKeys.SAVANNA)
                || world.getBiome(pos).matchesKey(net.minecraft.world.biome.BiomeKeys.SAVANNA_PLATEAU)
                || world.getBiome(pos).matchesKey(net.minecraft.world.biome.BiomeKeys.WINDSWEPT_SAVANNA);
    }

    private static boolean sandBelow(ServerWorld world, BlockPos pos) {
        return world.getBlockState(pos.down()).isIn(BlockTags.SAND);
    }

    private static boolean mirageColdBiome(ServerWorld world, BlockPos pos) {
        return world.getBiome(pos).matchesKey(net.minecraft.world.biome.BiomeKeys.FROZEN_OCEAN)
                || world.getBiome(pos).matchesKey(net.minecraft.world.biome.BiomeKeys.FROZEN_RIVER)
                || world.getBiome(pos).matchesKey(net.minecraft.world.biome.BiomeKeys.ICE_SPIKES)
                || world.getBiome(pos).matchesKey(net.minecraft.world.biome.BiomeKeys.SNOWY_PLAINS)
                || world.getBiome(pos).matchesKey(net.minecraft.world.biome.BiomeKeys.SNOWY_TAIGA)
                || world.getBiome(pos).matchesKey(net.minecraft.world.biome.BiomeKeys.SNOWY_SLOPES);
    }

    private static boolean naturalEarth(ServerWorld world, BlockPos pos) {
        BlockState b = world.getBlockState(pos.down());
        return b.isOf(Blocks.GRASS_BLOCK) || b.isOf(Blocks.DIRT) || b.isOf(Blocks.PODZOL)
                || b.isOf(Blocks.ROOTED_DIRT) || b.isOf(Blocks.COARSE_DIRT) || b.isOf(Blocks.MOSS_BLOCK)
                || b.isOf(Blocks.MUD) || b.isOf(Blocks.FARMLAND);
    }

    private static void trailFeet(ServerPlayerEntity player, ServerWorld world, net.minecraft.particle.ParticleEffect particle,
                                  int stride) {
        if (player.age % stride != 0) return;
        double x = player.getX();
        double y = player.getY() + 0.12;
        double z = player.getZ();
        var look = player.getRotationVec(1.0f);
        double bx = -look.z * 0.35;
        double bz = look.x * 0.35;
        world.spawnParticles(particle, x + bx, y, z + bz, 1, 0, 0, 0, 0);
    }

    private static void tickDune(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        boolean zone = dryBiome(world, player.getBlockPos()) || sandBelow(world, player.getBlockPos());
        ArmorAbilityHandler.applyConditionalCreativeFlight(player, zone);
        if (zone) {
            if (!player.hasStatusEffect(StatusEffects.HASTE)) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 120, 0, true, false, false));
            }
            if (!player.hasStatusEffect(StatusEffects.SPEED)) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 120, 0, true, false, false));
            }
            if (player.getAbilities().flying && player.age % 25 == 0) {
                player.addExhaustion(0.8f);
            }
        } else {
            if (player.age % 45 == 0) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 80, 0, true, false, false));
            }
        }
        trailFeet(player, world, ParticleTypes.CRIT, 7);
    }

    private static void tickReef(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        int sea = world.getSeaLevel();
        boolean allowFly = player.getBlockY() >= sea + 3 && !player.isSubmergedInWater();
        ArmorAbilityHandler.applyConditionalCreativeFlight(player, allowFly);
        if (player.isTouchingWaterOrRain()) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 210, 0, true, false, false));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.DOLPHINS_GRACE, 60, 0, true, false, false));
        }
        if (player.getAbilities().flying && player.getHungerManager().getFoodLevel() < 8 && player.age % 20 == 0) {
            player.damage(world, player.getDamageSources().drown(), 1.0f);
        }
        if (!player.isTouchingWaterOrRain() && player.isOnGround()) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 0, true, false, false));
        }
        trailFeet(player, world, ParticleTypes.BUBBLE_POP, 8);
    }

    private static void tickEmber(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        ArmorAbilityHandler.applyConditionalCreativeFlight(player, true);
        if (player.getAbilities().flying && !player.isSneaking() && player.age % 35 == 0) {
            player.setFireTicks(40);
        }
        if (!player.isOnFire()) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 100, 0, true, false, false));
        }
        trailFeet(player, world, ParticleTypes.SMALL_FLAME, 6);
    }

    private static void tickCrystal(ServerPlayerEntity player) {
        ArmorAbilityHandler.applyConditionalCreativeFlight(player, false);
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 100, 1, true, false, false));
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        if (player.age % 45 == 0) {
            for (HostileEntity h : world.getEntitiesByClass(HostileEntity.class, player.getBoundingBox().expand(3.5),
                    e -> e != null && e.isAlive())) {
                h.damage(world, player.getDamageSources().cactus(), 1.0f);
            }
        }
        trailFeet(player, world, ParticleTypes.ENCHANT, 9);
    }

    private static void tickShadow(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        boolean night = !world.isDay();
        ArmorAbilityHandler.applyConditionalCreativeFlight(player, night);
        if (night) {
            if (player.getAbilities().flying) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 40, 0, true, false, false));
            }
            if (world.getLightLevel(net.minecraft.world.LightType.BLOCK, player.getBlockPos()) < 6) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 80, 0, true, false, false));
            }
        }
        if (world.isDay() && world.isSkyVisible(player.getBlockPos()) && player.getBlockY() > world.getSeaLevel() + 4) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 40, 0, true, false, true));
        }
        trailFeet(player, world, ParticleTypes.ASH, 8);
    }

    private static void tickSolar(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        boolean day = world.isDay();
        ArmorAbilityHandler.applyConditionalCreativeFlight(player, day);
        if (day) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 100, 0, true, false, false));
            if (player.getAbilities().flying && player.age % 18 == 0) {
                player.addExhaustion(0.35f);
            }
            if (player.fallDistance > 6 && player.isOnGround() && player.age % 5 == 0) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 60, 0, true, false, false));
            }
        } else {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 0, true, false, false));
        }
        if (day) {
            trailFeet(player, world, ParticleTypes.END_ROD, 7);
        }
    }

    private static void tickLunar(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        boolean night = !world.isDay();
        ArmorAbilityHandler.applyConditionalCreativeFlight(player, night);
        if (night) {
            if (world.isSkyVisible(player.getBlockPos())) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 0, true, false, false));
            }
        } else {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 0, true, false, false));
        }
        trailFeet(player, world, ParticleTypes.ENCHANT, 10);
    }

    private static void tickTerra(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        boolean overEarth = naturalEarth(world, player.getBlockPos());
        ArmorAbilityHandler.applyConditionalCreativeFlight(player, overEarth);
        if (!overEarth && !player.isOnGround()) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 60, 1, true, false, false));
        }
        if (player.age % 55 == 0 && overEarth) {
            for (HostileEntity h : world.getEntitiesByClass(HostileEntity.class, player.getBoundingBox().expand(4),
                    LivingEntity::isAlive)) {
                if (h.getType().isIn(EntityTypeTags.UNDEAD)) {
                    h.damage(world, world.getDamageSources().magic(), 1.5f);
                }
            }
        }
        trailFeet(player, world, ParticleTypes.HAPPY_VILLAGER, 10);
    }

    private static void tickTempest(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        boolean storm = world.isThundering() || world.isRaining();
        ArmorAbilityHandler.applyConditionalCreativeFlight(player, storm);
        if (!storm && player.age % 45 == 0 && player.getAbilities().flying) {
            player.damage(world, player.getDamageSources().magic(), 2.0f);
        }
        if (storm && player.age % 35 == 0) {
            for (Entity e : world.getOtherEntities(player, player.getBoundingBox().expand(5))) {
                if (e instanceof HostileEntity h) {
                    h.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 1, true, false, true));
                }
            }
        }
        if (storm && player.age % 8 == 0) {
            trailFeet(player, world, ParticleTypes.ELECTRIC_SPARK, 8);
        }
    }

    private static void tickAuric(ServerPlayerEntity player) {
        ArmorAbilityHandler.applyConditionalCreativeFlight(player, true);
        if (!player.hasStatusEffect(StatusEffects.LUCK)) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.LUCK, 200, 0, true, false, false));
        }
        if (player.getHungerManager().getFoodLevel() > 16 && player.age % 40 == 0) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 60, 0, true, false, false));
        }
        if (player.getAbilities().flying && player.age % 12 == 0) {
            player.addExhaustion(0.5f);
        }
        if (player.getHungerManager().getFoodLevel() < 6) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 40, 0, false, true, true));
        }
        trailFeet(player, worldOf(player), ParticleTypes.WAX_ON, 9);
    }

    private static ServerWorld worldOf(ServerPlayerEntity player) {
        return (ServerWorld) player.getEntityWorld();
    }

    private static void tickGlacier(ServerPlayerEntity player) {
        ArmorAbilityHandler.applyConditionalCreativeFlight(player, false);
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        if (world.getRegistryKey() == World.NETHER) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 120, 1, true, false, false));
        }
        if (player.age % 40 == 0) {
            for (HostileEntity h : world.getEntitiesByClass(HostileEntity.class, player.getBoundingBox().expand(3.5),
                    e -> e.isAlive())) {
                h.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 80, 1, true, false, true));
            }
        }
        trailFeet(player, world, ParticleTypes.SNOWFLAKE, 8);
    }

    private static void tickMire(ServerPlayerEntity player) {
        ArmorAbilityHandler.applyConditionalCreativeFlight(player, false);
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        if (world.getBiome(player.getBlockPos()).matchesKey(net.minecraft.world.biome.BiomeKeys.DESERT)
                || world.getBiome(player.getBlockPos()).matchesKey(net.minecraft.world.biome.BiomeKeys.BADLANDS)) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 60, 0, true, false, false));
        }
        if (player.age % 4 == 0) {
            trailFeet(player, world, ParticleTypes.SPORE_BLOSSOM_AIR, 10);
        }
    }

    private static void tickStarfall(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        UUID u = player.getUuid();
        int stored = starfallStoredTicks.getOrDefault(u, 0);
        boolean onGround = player.isOnGround();
        boolean flying = player.getAbilities().flying;

        if (onGround && !flying) {
            int acc = starfallGroundAccumulator.getOrDefault(u, 0) + 1;
            if (acc >= 4) {
                acc = 0;
                stored = Math.min(180, stored + 2);
            }
            starfallGroundAccumulator.put(u, acc);
        } else {
            starfallGroundAccumulator.put(u, 0);
        }
        if (flying && stored > 0) {
            stored = Math.max(0, stored - 1);
        }
        ArmorAbilityHandler.applyConditionalCreativeFlight(player, stored > 0);
        if (stored == 0 && flying) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 15, 0, true, false, false));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 1, true, false, false));
        }
        starfallStoredTicks.put(u, stored);
        trailFeet(player, world, ParticleTypes.END_ROD, 6);
    }

    private static void tickMirage(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        boolean allow = dryBiome(world, player.getBlockPos()) && world.isDay();
        ArmorAbilityHandler.applyConditionalCreativeFlight(player, allow);
        if (allow && player.isSprinting() && player.age % 35 == 0) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 25, 0, true, false, false));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 40, 0, true, false, false));
        }
        if (mirageColdBiome(world, player.getBlockPos())) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 40, 0, true, false, false));
        }
        trailFeet(player, world, ParticleTypes.CLOUD, 8);
    }

    private static void tickObsidianWyrm(ServerPlayerEntity player) {
        ArmorAbilityHandler.applyConditionalCreativeFlight(player, true);
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 100, 2, true, false, false));
        if (player.getAbilities().flying) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 0, true, false, false));
            if (player.age % 22 == 0) {
                player.addExhaustion(0.25f);
            }
        }
        trailFeet(player, worldOf(player), ParticleTypes.LARGE_SMOKE, 9);
    }
}
