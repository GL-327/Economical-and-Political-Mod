package com.political;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biomes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

final class MoreDragonChestplateAbilities {

    private static final Map<UUID, Integer> starfallStoredTicks = new HashMap<>();
    private static final Map<UUID, Integer> starfallGroundAccumulator = new HashMap<>();

    private static final net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect>[] NEW_EFFECT_CYCLE = new net.minecraft.core.Holder[]{
            MobEffects.STRENGTH,
            MobEffects.SPEED,
            MobEffects.RESISTANCE,
            MobEffects.REGENERATION,
            MobEffects.ABSORPTION,
            MobEffects.FIRE_RESISTANCE,
            MobEffects.WATER_BREATHING,
            MobEffects.NIGHT_VISION,
            MobEffects.HASTE,
            MobEffects.JUMP_BOOST,
            MobEffects.LUCK,
            MobEffects.SLOW_FALLING
    };

    private static final net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect>[] NEW_DRAWBACK_CYCLE = new net.minecraft.core.Holder[]{
            MobEffects.HUNGER,
            MobEffects.SLOWNESS,
            MobEffects.WEAKNESS,
            MobEffects.WITHER,
            MobEffects.POISON,
            MobEffects.BLINDNESS,
            MobEffects.WEAKNESS,
            MobEffects.NAUSEA,
            MobEffects.MINING_FATIGUE,
            MobEffects.SLOW_FALLING, // intentional fatigue-style strain
            MobEffects.HUNGER,
            MobEffects.BLINDNESS
    };

    private static final net.minecraft.core.particles.ParticleOptions[] NEW_PARTICLE_CYCLE = new net.minecraft.core.particles.ParticleOptions[]{
            ParticleTypes.CRIT,
            ParticleTypes.ENCHANT,
            ParticleTypes.CLOUD,
            ParticleTypes.HAPPY_VILLAGER,
            ParticleTypes.BUBBLE_POP,
            ParticleTypes.SMALL_FLAME,
            ParticleTypes.ASH,
            ParticleTypes.END_ROD,
            ParticleTypes.ELECTRIC_SPARK,
            ParticleTypes.WAX_ON,
            ParticleTypes.SNOWFLAKE,
            ParticleTypes.LARGE_SMOKE
    };

    private MoreDragonChestplateAbilities() {
    }

    static void tickAll(ServerPlayer player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        String id = DragonChestplates.getCustomItemId(chest);
        if (id == null) return;

        if (id.startsWith("new_dragon_chestplate_")) {
            String suffix = id.substring("new_dragon_chestplate_".length());
            int index1Based = Integer.parseInt(suffix); // supports leading zeroes
            tickNewDragonChestplate(player, index1Based);
            return;
        }

        if (id.startsWith("omega_dragon_chestplate_")) {
            String suffix = id.substring("omega_dragon_chestplate_".length());
            int index1Based = Integer.parseInt(suffix);
            tickOmegaDragonChestplate(player, index1Based);
            return;
        }

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

    private static void tickNewDragonChestplate(ServerPlayer player, int index1Based) {
        if (index1Based < 1 || index1Based > 95) return;
        // Each of the 95 numbered plates has a hand-assigned distinct ability kit
        // (see DragonAbilityRegistry) instead of the old (index-1)%15 cycle.
        DragonAbilityRegistry.tickNumbered(player, index1Based);
    }

    private static boolean inWaterOrRain(LivingEntity entity) {
        if (entity.isInWater()) return true;
        if (!(entity.level() instanceof ServerLevel sl)) return false;
        return sl.isRaining() && sl.canSeeSky(entity.blockPosition());
    }

    private static boolean playerHasOmegaCore(ServerPlayer player) {
        // Omega core is a separate item: `dragon_crafter_core_omega`
        // Scan is only done when omega chestplates are equipped.
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack == null || stack.isEmpty()) continue;
            String cid = SlayerItems.getCustomItemId(stack);
            if ("dragon_crafter_core_omega".equals(cid)) return true;
        }
        return false;
    }

    private static void tickOmegaDragonChestplate(ServerPlayer player, int index1Based) {
        boolean omegaEnabled = playerHasOmegaCore(player);

        ArmorAbilityHandler.applyConditionalCreativeFlight(player, true);
        ServerLevel world = (ServerLevel) player.level();
        if (!player.getAbilities().flying) {
            // Focus omega power on actual flight; health cap is already handled by applyConditionalCreativeFlight.
            return;
        }

        switch (index1Based) {
            case 1 -> {
                // Omega Bulwark: Resistance + burst regeneration (no extra debuffs; health cap is the drawback).
                int interval = omegaEnabled ? 25 : 40;
                int duration = omegaEnabled ? 260 : 180;
                int resAmp = omegaEnabled ? 4 : 1;
                int regenAmp = omegaEnabled ? 1 : 0;

                if (player.tickCount % interval == 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, duration, resAmp, true, false, false));
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, regenAmp, true, false, false));
                }
                // Light offensive assist while empowered.
                if (omegaEnabled && player.tickCount % (interval + 10) == 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 120, 1, true, false, false));
                }

                trailFeet(player, world, ParticleTypes.ENCHANT, 9);
            }
            case 2 -> {
                // Omega Aegisweave: Absorption surges + fire-grade protection (few drawbacks; health cap is the drawback).
                int interval = omegaEnabled ? 20 : 40;
                int duration = omegaEnabled ? 260 : 180;
                int absAmp = omegaEnabled ? 8 : 2;

                if (player.tickCount % interval == 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, duration, absAmp, true, false, false));
                }

                if (player.tickCount % (interval + 35) == 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 220, 0, true, false, false));
                    player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 220, 0, true, false, false));
                }

                trailFeet(player, world, ParticleTypes.CRIT, 8);
            }
            case 3 -> {
                // Omega Draconic Prism: Vision + haste + combat buffs (no extra debuffs; health cap is the drawback).
                int interval = omegaEnabled ? 25 : 45;
                int duration = omegaEnabled ? 260 : 180;

                if (player.tickCount % interval == 0) {
                    int visionAmp = omegaEnabled ? 3 : 1;
                    int hasteAmp = omegaEnabled ? 2 : 0;
                    int strengthAmp = omegaEnabled ? 2 : 0;

                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, duration, visionAmp, true, false, false));
                    player.addEffect(new MobEffectInstance(MobEffects.HASTE, duration, hasteAmp, true, false, false));
                    player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, duration, strengthAmp, true, false, false));
                    player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 220, 0, true, false, false));
                }

                trailFeet(player, world, ParticleTypes.CLOUD, 8);
            }
            default -> {
            }
        }
    }

    public static void onHit(ServerPlayer player, LivingEntity target) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        String id = DragonChestplates.getCustomItemId(chest);
        if (id == null) return;
        if (id.startsWith("new_dragon_chestplate_")) {
            int index1Based = Integer.parseInt(id.substring("new_dragon_chestplate_".length()));
            DragonAbilityRegistry.onHitNumbered(player, target, index1Based);
            return;
        }
        if ("mire_dragon_chestplate".equals(id)) {
            target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 100, 0, false, true, true));
        }
        if ("reef_dragon_chestplate".equals(id) && target instanceof LivingEntity) {
            if (inWaterOrRain(target)) {
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 0, false, true, true));
            }
        }
    }

    private static boolean dryBiome(ServerLevel world, BlockPos pos) {
        return world.getBiome(pos).is(BiomeTags.IS_BADLANDS)
                || world.getBiome(pos).is(Biomes.DESERT)
                || world.getBiome(pos).is(Biomes.SAVANNA)
                || world.getBiome(pos).is(Biomes.SAVANNA_PLATEAU)
                || world.getBiome(pos).is(Biomes.WINDSWEPT_SAVANNA);
    }

    private static boolean sandBelow(ServerLevel world, BlockPos pos) {
        return world.getBlockState(pos.below()).is(BlockTags.SAND);
    }

    private static boolean mirageColdBiome(ServerLevel world, BlockPos pos) {
        return world.getBiome(pos).is(Biomes.FROZEN_OCEAN)
                || world.getBiome(pos).is(Biomes.FROZEN_RIVER)
                || world.getBiome(pos).is(Biomes.ICE_SPIKES)
                || world.getBiome(pos).is(Biomes.SNOWY_PLAINS)
                || world.getBiome(pos).is(Biomes.SNOWY_TAIGA)
                || world.getBiome(pos).is(Biomes.SNOWY_SLOPES);
    }

    private static boolean naturalEarth(ServerLevel world, BlockPos pos) {
        BlockState b = world.getBlockState(pos.below());
        return b.is(Blocks.GRASS_BLOCK) || b.is(Blocks.DIRT) || b.is(Blocks.PODZOL)
                || b.is(Blocks.ROOTED_DIRT) || b.is(Blocks.COARSE_DIRT) || b.is(Blocks.MOSS_BLOCK)
                || b.is(Blocks.MUD) || b.is(Blocks.FARMLAND);
    }

    private static void trailFeet(ServerPlayer player, ServerLevel world, net.minecraft.core.particles.ParticleOptions particle,
                                  int stride) {
        if (player.tickCount % stride != 0) return;
        double x = player.getX();
        double y = player.getY() + 0.12;
        double z = player.getZ();
        var look = player.getViewVector(1.0f);
        double bx = -look.z * 0.35;
        double bz = look.x * 0.35;
        world.sendParticles(particle, x + bx, y, z + bz, 1, 0, 0, 0, 0);
    }

    private static void tickDune(ServerPlayer player) {
        ServerLevel world = (ServerLevel) player.level();
        boolean zone = dryBiome(world, player.blockPosition()) || sandBelow(world, player.blockPosition());
        ArmorAbilityHandler.applyConditionalCreativeFlight(player, zone);
        if (zone) {
            if (!player.hasEffect(MobEffects.HASTE)) {
                player.addEffect(new MobEffectInstance(MobEffects.HASTE, 120, 0, true, false, false));
            }
            if (!player.hasEffect(MobEffects.SPEED)) {
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 120, 0, true, false, false));
            }
            if (player.getAbilities().flying && player.tickCount % 25 == 0) {
                player.getFoodData().addExhaustion(0.8f);
            }
        } else {
            if (player.tickCount % 45 == 0) {
                player.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 80, 0, true, false, false));
            }
        }
        trailFeet(player, world, ParticleTypes.CRIT, 7);
    }

    private static void tickReef(ServerPlayer player) {
        ServerLevel world = (ServerLevel) player.level();
        int sea = world.getSeaLevel();
        boolean allowFly = player.getBlockY() >= sea + 3 && !player.isUnderWater();
        ArmorAbilityHandler.applyConditionalCreativeFlight(player, allowFly);
        if (inWaterOrRain(player)) {
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 210, 0, true, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 60, 0, true, false, false));
        }
        if (player.getAbilities().flying && player.getFoodData().getFoodLevel() < 8 && player.tickCount % 20 == 0) {
            player.hurtServer(world, player.damageSources().drown(), 1.0f);
        }
        if (!inWaterOrRain(player) && player.onGround()) {
            player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0, true, false, false));
        }
        trailFeet(player, world, ParticleTypes.BUBBLE_POP, 8);
    }

    private static void tickEmber(ServerPlayer player) {
        ServerLevel world = (ServerLevel) player.level();
        ArmorAbilityHandler.applyConditionalCreativeFlight(player, true);
        if (player.getAbilities().flying && !player.isShiftKeyDown() && player.tickCount % 35 == 0) {
            player.setRemainingFireTicks(40);
        }
        if (!player.isOnFire()) {
            player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 100, 0, true, false, false));
        }
        trailFeet(player, world, ParticleTypes.SMALL_FLAME, 6);
    }

    private static void tickCrystal(ServerPlayer player) {
        ArmorAbilityHandler.applyConditionalCreativeFlight(player, false);
        player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 100, 1, true, false, false));
        ServerLevel world = (ServerLevel) player.level();
        if (player.tickCount % 45 == 0) {
            for (Monster h : world.getEntitiesOfClass(Monster.class, player.getBoundingBox().inflate(3.5),
                    e -> e != null && e.isAlive())) {
                h.hurtServer(world, player.damageSources().cactus(), 1.0f);
            }
        }
        trailFeet(player, world, ParticleTypes.ENCHANT, 9);
    }

    private static void tickShadow(ServerPlayer player) {
        ServerLevel world = (ServerLevel) player.level();
        boolean night = !world.isBrightOutside();
        ArmorAbilityHandler.applyConditionalCreativeFlight(player, night);
        if (night) {
            if (player.getAbilities().flying) {
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, true, false, false));
            }
            if (world.getBrightness(LightLayer.BLOCK, player.blockPosition()) < 6) {
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 80, 0, true, false, false));
            }
        }
        if (world.isBrightOutside() && world.canSeeSky(player.blockPosition()) && player.getBlockY() > world.getSeaLevel() + 4) {
            player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, true, false, true));
        }
        trailFeet(player, world, ParticleTypes.ASH, 8);
    }

    private static void tickSolar(ServerPlayer player) {
        ServerLevel world = (ServerLevel) player.level();
        boolean day = world.isBrightOutside();
        ArmorAbilityHandler.applyConditionalCreativeFlight(player, day);
        if (day) {
            player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 100, 0, true, false, false));
            if (player.getAbilities().flying && player.tickCount % 18 == 0) {
                player.getFoodData().addExhaustion(0.35f);
            }
            if (player.fallDistance > 6 && player.onGround() && player.tickCount % 5 == 0) {
                player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 60, 0, true, false, false));
            }
        } else {
            player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 0, true, false, false));
        }
        if (day) {
            trailFeet(player, world, ParticleTypes.END_ROD, 7);
        }
    }

    private static void tickLunar(ServerPlayer player) {
        ServerLevel world = (ServerLevel) player.level();
        boolean night = !world.isBrightOutside();
        ArmorAbilityHandler.applyConditionalCreativeFlight(player, night);
        if (night) {
            if (world.canSeeSky(player.blockPosition())) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0, true, false, false));
            }
        } else {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0, true, false, false));
        }
        trailFeet(player, world, ParticleTypes.ENCHANT, 10);
    }

    private static void tickTerra(ServerPlayer player) {
        ServerLevel world = (ServerLevel) player.level();
        boolean overEarth = naturalEarth(world, player.blockPosition());
        ArmorAbilityHandler.applyConditionalCreativeFlight(player, overEarth);
        if (!overEarth && !player.onGround()) {
            player.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 60, 1, true, false, false));
        }
        if (player.tickCount % 55 == 0 && overEarth) {
            for (Monster h : world.getEntitiesOfClass(Monster.class, player.getBoundingBox().inflate(4),
                    LivingEntity::isAlive)) {
                if (h.getType().builtInRegistryHolder().is(EntityTypeTags.UNDEAD)) {
                    h.hurtServer(world, world.damageSources().magic(), 1.5f);
                }
            }
        }
        trailFeet(player, world, ParticleTypes.HAPPY_VILLAGER, 10);
    }

    private static void tickTempest(ServerPlayer player) {
        ServerLevel world = (ServerLevel) player.level();
        boolean storm = world.isThundering() || world.isRaining();
        ArmorAbilityHandler.applyConditionalCreativeFlight(player, storm);
        if (!storm && player.tickCount % 45 == 0 && player.getAbilities().flying) {
            player.hurtServer(world, player.damageSources().magic(), 2.0f);
        }
        if (storm && player.tickCount % 35 == 0) {
            for (Entity e : world.getEntities(player, player.getBoundingBox().inflate(5), e -> true)) {
                if (e instanceof Monster h) {
                    h.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1, true, false, true));
                }
            }
        }
        if (storm && player.tickCount % 8 == 0) {
            trailFeet(player, world, ParticleTypes.ELECTRIC_SPARK, 8);
        }
    }

    private static void tickAuric(ServerPlayer player) {
        ArmorAbilityHandler.applyConditionalCreativeFlight(player, true);
        if (!player.hasEffect(MobEffects.LUCK)) {
            player.addEffect(new MobEffectInstance(MobEffects.LUCK, 200, 0, true, false, false));
        }
        if (player.getFoodData().getFoodLevel() > 16 && player.tickCount % 40 == 0) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, false, false));
        }
        if (player.getAbilities().flying && player.tickCount % 12 == 0) {
            player.getFoodData().addExhaustion(0.5f);
        }
        if (player.getFoodData().getFoodLevel() < 6) {
            player.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 0, false, true, true));
        }
        trailFeet(player, worldOf(player), ParticleTypes.WAX_ON, 9);
    }

    private static ServerLevel worldOf(ServerPlayer player) {
        return (ServerLevel) player.level();
    }

    private static void tickGlacier(ServerPlayer player) {
        ArmorAbilityHandler.applyConditionalCreativeFlight(player, false);
        ServerLevel world = (ServerLevel) player.level();
        if (world.dimension() == Level.NETHER) {
            player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 120, 1, true, false, false));
        }
        if (player.tickCount % 40 == 0) {
            for (Monster h : world.getEntitiesOfClass(Monster.class, player.getBoundingBox().inflate(3.5),
                    e -> e.isAlive())) {
                h.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1, true, false, true));
            }
        }
        trailFeet(player, world, ParticleTypes.SNOWFLAKE, 8);
    }

    private static void tickMire(ServerPlayer player) {
        ArmorAbilityHandler.applyConditionalCreativeFlight(player, false);
        ServerLevel world = (ServerLevel) player.level();
        if (world.getBiome(player.blockPosition()).is(Biomes.DESERT)
                || world.getBiome(player.blockPosition()).is(Biomes.BADLANDS)) {
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 60, 0, true, false, false));
        }
        if (player.tickCount % 4 == 0) {
            trailFeet(player, world, ParticleTypes.SPORE_BLOSSOM_AIR, 10);
        }
    }

    private static void tickStarfall(ServerPlayer player) {
        ServerLevel world = (ServerLevel) player.level();
        UUID u = player.getUUID();
        int stored = starfallStoredTicks.getOrDefault(u, 0);
        boolean onGround = player.onGround();
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
            player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 15, 0, true, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1, true, false, false));
        }
        starfallStoredTicks.put(u, stored);
        trailFeet(player, world, ParticleTypes.END_ROD, 6);
    }

    private static void tickMirage(ServerPlayer player) {
        ServerLevel world = (ServerLevel) player.level();
        boolean allow = dryBiome(world, player.blockPosition()) && world.isBrightOutside();
        ArmorAbilityHandler.applyConditionalCreativeFlight(player, allow);
        if (allow && player.isSprinting() && player.tickCount % 35 == 0) {
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 25, 0, true, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 40, 0, true, false, false));
        }
        if (mirageColdBiome(world, player.blockPosition())) {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0, true, false, false));
        }
        trailFeet(player, world, ParticleTypes.CLOUD, 8);
    }

    private static void tickObsidianWyrm(ServerPlayer player) {
        ArmorAbilityHandler.applyConditionalCreativeFlight(player, true);
        player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 100, 2, true, false, false));
        if (player.getAbilities().flying) {
            player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0, true, false, false));
            if (player.tickCount % 22 == 0) {
                player.getFoodData().addExhaustion(0.25f);
            }
        }
        trailFeet(player, worldOf(player), ParticleTypes.LARGE_SMOKE, 9);
    }
}
