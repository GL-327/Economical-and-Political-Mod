package com.political;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;

/**
 * Block-themed leather armor: bonuses scale with 2 / 3 / 4 matching pieces, plus short reactive buffs when hit (full sets).
 */
public final class BlockArmorHandler {

    private BlockArmorHandler() {}
    private static final java.util.Set<java.util.UUID> glassFlightGranted = new java.util.HashSet<>();

    public static void tick(ServerPlayer player) {
        int anyPieces = BlockArmor.countAnyBlockArmorPieces(player);
        if (anyPieces >= 4 && player.tickCount % 32 == 0) {
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 70, 0, true, false, false));
        }

        tickAfterHit(player);

        // Full-set only model (4/4 matching): abilities are handled by tickUniqueFullSetAbility.

        // Per-material identity layer: every 4/4 set gets a unique hard-coded behavior.
        tickUniqueFullSetAbility(player);
        // Legacy material handlers still contain many set-specific effects; keep them active.
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
        tickEmeraldArmor(player);
        tickDiamondBlockArmor(player);
        tickGoldBlockArmor(player);
        tickCopperBlockArmor(player);
        tickAncientDebrisArmor(player);
        tickSculkArmor(player);
        tickLapisArmor(player);
        tickSlimeArmor(player);
        tickStoneVariants(player);
        tickDeepslateVariants(player);
        tickBlackstoneVariants(player);
        tickNetherVariants(player);
        tickIceVariants(player);
        tickOceanVariants(player);
        tickEndVariants(player);
        tickNatureVariants(player);
        tickWoodVariants(player);
        tickCopperVariants(player);
        tickMiscVariants(player);
        tickNew25Materials(player);
        tickGlassFlightState(player);
    }

    /** One frame after taking damage (vanilla hurtTime peaks at 10). */
    private static void tickAfterHit(ServerPlayer player) {
        if (player.hurtTime != 10) {
            return;
        }
        if (!(player.level() instanceof ServerLevel sl)) {
            return;
        }
        if (BlockArmor.hasFullBlockArmorSet(player, "obsidian")) {
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 120, 1, true, false, false));
            sl.sendParticles(ParticleTypes.ENCHANTED_HIT, player.getX(), player.getEyeY(), player.getZ(), 5, 0.4, 0.3, 0.4, 0.02);
        }
        if (BlockArmor.hasFullBlockArmorSet(player, "quartz")) {
            player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 80, 2, true, false, false));
            sl.sendParticles(ParticleTypes.CRIT, player.getX(), player.getEyeY(), player.getZ(), 5, 0.5, 0.2, 0.5, 0.05);
        }
        if (BlockArmor.hasFullBlockArmorSet(player, "amethyst")) {
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 100, 2, true, false, false));
            if (player.getRandom().nextFloat() < 0.15f) {
                LivingEntity attacker = player.getLastHurtByMob();
                if (attacker != null && attacker.isAlive() && attacker.distanceTo(player) < 5) {
                    float reflectDmg = 3.0f;
                    if (reflectDmg > 0) {
                        attacker.hurtServer((ServerLevel) player.level(), player.damageSources().thorns(player), reflectDmg);
                        sl.sendParticles(ParticleTypes.REVERSE_PORTAL, attacker.getX(), attacker.getEyeY(), attacker.getZ(), 4, 0.3, 0.2, 0.3, 0.05);
                        sl.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 0.8f, 1.4f);
                    }
                }
            }
        }
        // Glass identity: shatter retaliation with a durability tradeoff.
        if (BlockArmor.hasFullBlockArmorSet(player, "glass")) {
            LivingEntity attacker = player.getLastHurtByMob();
            if (attacker != null && attacker.isAlive() && attacker.distanceTo(player) < 5) {
                attacker.hurtServer((ServerLevel) player.level(), player.damageSources().thorns(player), 6.0f);
                attacker.knockback(1.2f, player.getX() - attacker.getX(), player.getZ() - attacker.getZ());
                sl.sendParticles(ParticleTypes.ITEM_COBWEB, attacker.getX(), attacker.getEyeY(), attacker.getZ(), 6, 0.2, 0.2, 0.2, 0.01);
                // Shatter cost: one random armor piece loses extra durability.
                EquipmentSlot[] slots = new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };
                EquipmentSlot chosen = slots[player.getRandom().nextInt(slots.length)];
                ItemStack piece = player.getItemBySlot(chosen);
                if (!piece.isEmpty()) piece.hurtAndBreak(2, player, chosen);
            }
        }
    }

    private static String getFullSetPrefix(ServerPlayer player) {
        String prefix = null;
        for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET }) {
            ItemStack piece = player.getItemBySlot(slot);
            String id = SlayerItems.stripSkyblockEnchantSuffix(SlayerItems.getCustomItemId(piece));
            if (id == null) {
                id = BlockArmor.getBlockArmorId(piece);
            }
            id = normalizeBlockArmorSetPieceId(id);
            if (id == null
                    || !(id.endsWith("_helmet") || id.endsWith("_chestplate") || id.endsWith("_leggings") || id.endsWith("_boots"))) {
                return null;
            }
            int cut = id.lastIndexOf('_');
            if (cut <= 0) return null;
            String p = id.substring(0, cut);
            if (prefix == null) prefix = p;
            else if (!prefix.equals(p)) return null;
        }
        return prefix;
    }

    /** Strip tier suffixes so T1/T2 bounty-style ids still form a full set key (e.g. glass_helmet_t2 → prefix "glass"). */
    private static String normalizeBlockArmorSetPieceId(String id) {
        if (id == null) return null;
        String s = id;
        if (s.endsWith("_t2")) s = s.substring(0, s.length() - 3);
        else if (s.endsWith("_t1")) s = s.substring(0, s.length() - 3);
        return s;
    }

    private static void tickUniqueFullSetAbility(ServerPlayer player) {
        String prefix = getFullSetPrefix(player);
        if (prefix == null) return;
        if (!(player.level() instanceof ServerLevel sl)) return;

        // Hand-assigned unique bonus per material (UniqueArmorSetBonuses registry).
        if (UniqueArmorSetBonuses.tick(player, sl, prefix)) return;

        // Fallback for prefixes without a registry entry: deterministic generated signature.
        applyGeneratedUniqueArmorSignature(player, sl, prefix);
    }

    private static void applyGeneratedUniqueArmorSignature(ServerPlayer player, ServerLevel sl, String prefix) {
        int sig = Math.floorMod(prefix.hashCode(), Integer.MAX_VALUE);
        String theme = resolveArmorTheme(prefix.toLowerCase(java.util.Locale.ROOT));
        int cadenceA = 30 + (sig % 53);
        int cadenceB = 46 + ((sig / 11) % 67);
        int mode = (sig + theme.hashCode()) & 7;
        int radius = 2 + (sig % 3);

        if ("glass".equals(prefix)) {
            ensureEffect(player, new MobEffectInstance(MobEffects.ABSORPTION, 60, 0, true, false, false));
        }

        if (player.tickCount % cadenceA == 0) {
            switch (mode) {
                case 0 -> {
                    var look = player.getLookAngle().normalize().scale(0.25 + (sig % 5) * 0.04);
                    player.push(look.x, 0.04, look.z);
                    sl.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.8, player.getZ(), 6, 0.35, 0.2, 0.35, 0.02);
                }
                case 1 -> {
                    for (LivingEntity e : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius), ent -> ent != player && ent instanceof Monster)) {
                        e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 55, 0, true, false, false));
                    }
                    sl.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getEyeY(), player.getZ(), 7, 0.45, 0.25, 0.45, 0.02);
                }
                case 2 -> {
                    player.heal(0.25f + (sig % 4) * 0.05f);
                    ensureEffect(player, new MobEffectInstance(MobEffects.ABSORPTION, 45, 0, true, false, false));
                    sl.sendParticles(ParticleTypes.HEART, player.getX(), player.getEyeY(), player.getZ(), 3, 0.2, 0.2, 0.2, 0.01);
                }
                case 3 -> {
                    ensureEffect(player, new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 90, 0, true, false, false));
                    for (LivingEntity e : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius), ent -> ent != player && ent instanceof Monster)) {
                        e.setRemainingFireTicks(70);
                    }
                    sl.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 0.3, player.getZ(), 6, 0.5, 0.2, 0.5, 0.02);
                }
                case 4 -> {
                    ensureEffect(player, new MobEffectInstance(MobEffects.WATER_BREATHING, 100, 0, true, false, false));
                    for (LivingEntity e : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius), ent -> ent != player && ent instanceof Monster)) {
                        e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 0, true, false, false));
                    }
                    sl.sendParticles(ParticleTypes.BUBBLE, player.getX(), player.getY() + 0.6, player.getZ(), 8, 0.5, 0.3, 0.5, 0.01);
                }
                case 5 -> {
                    ensureEffect(player, new MobEffectInstance(MobEffects.NIGHT_VISION, 120, 0, true, false, true));
                    for (LivingEntity e : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius + 1), ent -> ent != player && ent instanceof Monster)) {
                        e.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 45, 0, true, false, false));
                    }
                    sl.sendParticles(ParticleTypes.SMOKE, player.getX(), player.getEyeY(), player.getZ(), 8, 0.35, 0.2, 0.35, 0.03);
                }
                case 6 -> {
                    ensureEffect(player, new MobEffectInstance(MobEffects.HASTE, 75, 0, true, false, false));
                    ensureEffect(player, new MobEffectInstance(MobEffects.STRENGTH, 65, 0, true, false, false));
                    sl.sendParticles(ParticleTypes.CRIT, player.getX(), player.getEyeY(), player.getZ(), 6, 0.3, 0.2, 0.3, 0.03);
                }
                default -> {
                    ensureEffect(player, new MobEffectInstance(MobEffects.SLOW_FALLING, 75, 0, true, false, false));
                    ensureEffect(player, new MobEffectInstance(MobEffects.JUMP_BOOST, 75, 0, true, false, false));
                    sl.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getEyeY(), player.getZ(), 5, 0.25, 0.2, 0.25, 0.02);
                }
            }
        }

        if (player.tickCount % cadenceB == 0) {
            int burst = 2 + (sig % 4);
            for (LivingEntity e : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius), ent -> ent != player && ent instanceof Monster)) {
                e.hurtServer(sl, player.damageSources().magic(), burst);
                if ("fire".equals(theme)) e.setRemainingFireTicks(50);
                if ("water".equals(theme)) e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0, true, false, false));
                if ("shadow".equals(theme)) e.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 35, 0, true, false, false));
            }
            if ("light".equals(theme)) {
                sl.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getEyeY(), player.getZ(), 6, 0.3, 0.2, 0.3, 0.02);
            } else if ("earth".equals(theme) || "metal".equals(theme)) {
                sl.sendParticles(ParticleTypes.CRIT, player.getX(), player.getEyeY(), player.getZ(), 6, 0.3, 0.2, 0.3, 0.02);
            }
        }
    }
    private static String resolveArmorTheme(String p) {
        if (p.contains("glass")) return "glass";
        if (p.contains("redstone")) return "fire";
        if (p.contains("magma") || p.contains("nether") || p.contains("blaze") || p.contains("netherrack") || p.contains("furnace")) return "fire";
        if (p.contains("prismarine") || p.contains("water") || p.contains("coral") || p.contains("kelp") || p.contains("sponge") || p.contains("tidal")) return "water";
        if (p.contains("obsidian") || p.contains("stone") || p.contains("deepslate") || p.contains("concrete") || p.contains("clay") || p.contains("terracotta") || p.contains("dripstone")) return "earth";
        if (p.contains("iron") || p.contains("gold") || p.contains("copper") || p.contains("anvil") || p.contains("chain") || p.contains("smith") || p.contains("grindstone")) return "metal";
        if (p.contains("moss") || p.contains("wood") || p.contains("log") || p.contains("hay") || p.contains("honey") || p.contains("pumpkin") || p.contains("melon") || p.contains("mycelium")) return "nature";
        if (p.contains("sculk") || p.contains("soul") || p.contains("coal") || p.contains("blackstone") || p.contains("void") || p.contains("dark")) return "shadow";
        if (p.contains("glow") || p.contains("lantern") || p.contains("candle") || p.contains("quartz") || p.contains("lumen")) return "light";
        return "air";
    }


    private static void tickGlassFlightState(ServerPlayer player) {
        boolean fullGlass = BlockArmor.hasFullBlockArmorSet(player, "glass");
        var abilities = player.getAbilities();
        java.util.UUID id = player.getUUID();

        if (fullGlass) {
            if (!abilities.mayfly) {
                abilities.mayfly = true;
                player.onUpdateAbilities();
            }
            glassFlightGranted.add(id);
            return;
        }

        if (!glassFlightGranted.contains(id)) return;
        glassFlightGranted.remove(id);
        if (player.isCreative() || player.isSpectator()) return;
        abilities.mayfly = false;
        abilities.flying = false;
        player.onUpdateAbilities();
    }


    private static int pieces(ServerPlayer player, String prefix) {
        return BlockArmor.countBlockArmorPieces(player, prefix);
    }

    private static void applyScaled(ServerPlayer player, int n, MobEffectInstance weak, MobEffectInstance mid, MobEffectInstance full) {
        if (n >= 4) {
            ensureEffect(player, full);
        } else if (n == 3) {
            ensureEffect(player, mid);
        } else if (n == 2) {
            ensureEffect(player, weak);
        }
    }

    private static void ensureEffect(ServerPlayer player, MobEffectInstance inst) {
        MobEffectInstance cur = player.getEffect(inst.getEffect());
        if (cur == null || cur.getDuration() < inst.getDuration() / 4) {
            player.addEffect(new MobEffectInstance(
                    inst.getEffect(),
                    inst.getDuration(),
                    inst.getAmplifier(),
                    inst.isAmbient(),
                    true,
                    true));
        }
    }

    private static void tickObsidianArmor(ServerPlayer player) {
        int n = pieces(player, "obsidian");
        if (n < 2) return;
        applyScaled(player, n,
                new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 320, 0, true, false, false),
                new MobEffectInstance(MobEffects.RESISTANCE, 320, 0, true, false, false),
                new MobEffectInstance(MobEffects.RESISTANCE, 400, 1, true, false, false));
        if (n >= 3) {
            ensureEffect(player, new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 400, 0, true, false, false));
        }
        if (n >= 4 && player.tickCount % 60 == 0 && player.level() instanceof ServerLevel sl) {
            for (LivingEntity e : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(1.5),
                    ent -> ent != player && ent.isAlive() && ent instanceof Monster)) {
                e.hurtServer((ServerLevel) player.level(), player.damageSources().magic(), 3.0f);
                e.setRemainingFireTicks(40);
            }
            sl.sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 0.3, player.getZ(), 3, 0.5, 0.2, 0.5, 0.01);
            sl.sendParticles(ParticleTypes.LAVA, player.getX(), player.getY() + 0.2, player.getZ(), 2, 0.5, 0.1, 0.5, 0);
        }
    }

    private static void tickGlowstoneArmor(ServerPlayer player) {
        int n = pieces(player, "glowstone");
        if (n < 2) return;
        applyScaled(player, n,
                new MobEffectInstance(MobEffects.NIGHT_VISION, 280, 0, true, false, false),
                new MobEffectInstance(MobEffects.GLOWING, 60, 0, true, false, false),
                new MobEffectInstance(MobEffects.NIGHT_VISION, 420, 0, true, false, false));
        if (n >= 4 && player.tickCount % 40 == 0) {
            for (LivingEntity e : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(8),
                    ent -> ent != player && ent.isAlive() && ent instanceof Monster)) {
                e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, true, false, false));
            }
        }
    }

    private static void tickRedstoneArmor(ServerPlayer player) {
        int n = pieces(player, "redstone");
        if (n < 2) return;
        applyScaled(player, n,
                new MobEffectInstance(MobEffects.HASTE, 260, 0, true, false, false),
                new MobEffectInstance(MobEffects.HASTE, 320, 1, true, false, false),
                new MobEffectInstance(MobEffects.HASTE, 400, 2, true, false, false));
        if (n >= 3) {
            ensureEffect(player, new MobEffectInstance(MobEffects.SPEED, 300, 0, true, false, false));
        }
        if (n >= 4) {
            ensureEffect(player, new MobEffectInstance(MobEffects.HASTE, 400, 2, true, false, false));
        }
    }

    private static void tickNetherrackArmor(ServerPlayer player) {
        int n = pieces(player, "netherrack");
        if (n < 2) return;
        applyScaled(player, n,
                new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 260, 0, true, false, false),
                new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 360, 0, true, false, false),
                new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 450, 0, true, false, false));
        if (n >= 4 && player.level().dimension() == Level.NETHER && player.tickCount % 30 == 0) {
            ensureEffect(player, new MobEffectInstance(MobEffects.STRENGTH, 120, 1, true, false, false));
        }
    }

    private static void tickEndstoneArmor(ServerPlayer player) {
        int n = pieces(player, "endstone");
        if (n < 2) return;
        applyScaled(player, n,
                new MobEffectInstance(MobEffects.SLOW_FALLING, 260, 0, true, false, false),
                new MobEffectInstance(MobEffects.SLOW_FALLING, 360, 0, true, false, false),
                new MobEffectInstance(MobEffects.SLOW_FALLING, 450, 0, true, false, false));
        if (n >= 3) {
            ensureEffect(player, new MobEffectInstance(MobEffects.JUMP_BOOST, 320, 0, true, false, false));
        }
        if (n >= 4 && player.getY() < player.level().getMinY() + 32) {
            ensureEffect(player, new MobEffectInstance(MobEffects.LEVITATION, 15, 0, true, false, false));
        }
    }

    private static void tickIceArmor(ServerPlayer player) {
        int n = pieces(player, "ice");
        if (n < 2) return;
        if (n >= 2 && player.hasEffect(MobEffects.SLOWNESS) && player.getEffect(MobEffects.SLOWNESS).getAmplifier() >= 3) {
            player.removeEffect(MobEffects.SLOWNESS);
        }
        applyScaled(player, n,
                new MobEffectInstance(MobEffects.SPEED, 200, 0, true, false, false),
                new MobEffectInstance(MobEffects.RESISTANCE, 220, 0, true, false, false),
                new MobEffectInstance(MobEffects.SPEED, 360, 1, true, false, false));
        if (n >= 4 && (player.isInWaterOrRain() || player.level().getBiome(player.blockPosition()).value().coldEnoughToSnow(player.blockPosition(), player.level().getSeaLevel()))) {
            ensureEffect(player, new MobEffectInstance(MobEffects.RESISTANCE, 80, 1, true, false, false));
        }
        if (n >= 4 && player.tickCount % 40 == 0) {
            for (LivingEntity e : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(4),
                    ent -> ent != player && ent.isAlive() && ent instanceof Monster)) {
                e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0, true, false, false));
            }
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() + 0.5, player.getZ(), 4, 0.8, 0.3, 0.8, 0.01);
            }
        }
    }

    private static void tickPrismarineArmor(ServerPlayer player) {
        int n = pieces(player, "prismarine");
        if (n < 2) return;
        applyScaled(player, n,
                new MobEffectInstance(MobEffects.WATER_BREATHING, 280, 0, true, false, false),
                new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 200, 0, true, false, false),
                new MobEffectInstance(MobEffects.WATER_BREATHING, 450, 0, true, false, false));
        if (n >= 3 && player.isInWaterOrRain()) {
            ensureEffect(player, new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 360, 0, true, false, false));
        }
        if (n >= 4 && player.isInWater()) {
            ensureEffect(player, new MobEffectInstance(MobEffects.CONDUIT_POWER, 200, 0, true, false, false));
            ensureEffect(player, new MobEffectInstance(MobEffects.SPEED, 200, 0, true, false, false));
            if (player.tickCount % 25 == 0) {
                player.heal(0.25f);
            }
        }
    }

    private static void tickQuartzArmor(ServerPlayer player) {
        int n = pieces(player, "quartz");
        if (n < 2) return;
        applyScaled(player, n,
                new MobEffectInstance(MobEffects.HASTE, 260, 0, true, false, false),
                new MobEffectInstance(MobEffects.STRENGTH, 260, 0, true, false, false),
                new MobEffectInstance(MobEffects.HASTE, 400, 1, true, false, false));
        if (n >= 4) {
            ensureEffect(player, new MobEffectInstance(MobEffects.STRENGTH, 400, 1, true, false, false));
        }
    }

    private static void tickGlassArmor(ServerPlayer player) {
        int n = pieces(player, "glass");
        if (n < 2) return;
        applyScaled(player, n,
                new MobEffectInstance(MobEffects.SPEED, 220, 0, true, false, false),
                new MobEffectInstance(MobEffects.SPEED, 300, 1, true, false, false),
                new MobEffectInstance(MobEffects.SPEED, 400, 2, true, false, false));
        if (n >= 4 && player.tickCount % 60 == 0) {
            ensureEffect(player, new MobEffectInstance(MobEffects.INVISIBILITY, 25, 0, true, false, false));
        }
    }

    private static void tickTerracottaArmor(ServerPlayer player) {
        int n = pieces(player, "terracotta");
        if (n < 2) return;
        applyScaled(player, n,
                new MobEffectInstance(MobEffects.RESISTANCE, 280, 0, true, false, false),
                new MobEffectInstance(MobEffects.RESISTANCE, 340, 1, true, false, false),
                new MobEffectInstance(MobEffects.RESISTANCE, 420, 1, true, false, false));
    }

    private static void tickMossyArmor(ServerPlayer player) {
        int n = pieces(player, "mossy");
        if (n < 2) return;
        applyScaled(player, n,
                new MobEffectInstance(MobEffects.REGENERATION, 260, 0, true, false, false),
                new MobEffectInstance(MobEffects.REGENERATION, 340, 1, true, false, false),
                new MobEffectInstance(MobEffects.REGENERATION, 450, 1, true, false, false));
        if (n >= 3 && player.level().getMaxLocalRawBrightness(player.blockPosition()) < 8) {
            ensureEffect(player, new MobEffectInstance(MobEffects.STRENGTH, 160, 0, true, false, false));
        }
    }

    private static void tickSoulSandArmor(ServerPlayer player) {
        int n = pieces(player, "soul_sand");
        if (n < 2) return;
        boolean onSoul = player.level().getBlockState(player.blockPosition().below()).is(net.minecraft.world.level.block.Blocks.SOUL_SAND)
                || player.level().getBlockState(player.blockPosition().below()).is(net.minecraft.world.level.block.Blocks.SOUL_SOIL);
        if (onSoul || n >= 4) {
            applyScaled(player, n,
                    new MobEffectInstance(MobEffects.SPEED, 220, 0, true, false, false),
                    new MobEffectInstance(MobEffects.SPEED, 300, 1, true, false, false),
                    new MobEffectInstance(MobEffects.SPEED, 400, 2, true, false, false));
        }
        if (n >= 3 && player.tickCount % 40 == 0) {
            for (LivingEntity e : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(4),
                    ent -> ent != player && ent.isAlive() && ent instanceof Monster)) {
                e.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0, true, false, false));
            }
        }
        if (n >= 4 && player.tickCount % 40 == 0 && player.level() instanceof ServerLevel sl) {
            for (LivingEntity e : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(4),
                    ent -> ent != player && ent.isAlive() && ent instanceof Monster)) {
                e.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0, true, false, false));
                e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 0, true, false, false));
            }
            sl.sendParticles(ParticleTypes.SOUL, player.getX(), player.getY() + 0.5, player.getZ(), 3, 0.8, 0.3, 0.8, 0.02);
        }
    }

    private static void tickMagmaArmor(ServerPlayer player) {
        int n = pieces(player, "magma");
        if (n < 2) return;
        applyScaled(player, n,
                new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 260, 0, true, false, false),
                new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 360, 0, true, false, false),
                new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 450, 0, true, false, false));
        if (n >= 4 && player.tickCount % 40 == 0) {
            for (LivingEntity e : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(3.0),
                    ent -> ent != player && ent.isAlive() && ent.distanceTo(player) <= 3.0 && ent instanceof Monster)) {
                e.setRemainingFireTicks(80);
                e.hurtServer((ServerLevel) player.level(), player.damageSources().onFire(), 2.0f);
            }
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 0.3, player.getZ(), 4, 0.8, 0.2, 0.8, 0.02);
            }
        }
    }

    private static void tickSandstoneArmor(ServerPlayer player) {
        int n = pieces(player, "sandstone");
        if (n < 2) return;
        boolean sand = player.level().getBlockState(player.blockPosition().below()).getBlock() instanceof net.minecraft.world.level.block.SandBlock;
        if (sand || n >= 4) {
            applyScaled(player, n,
                    new MobEffectInstance(MobEffects.SPEED, 240, 0, true, false, false),
                    new MobEffectInstance(MobEffects.JUMP_BOOST, 260, 0, true, false, false),
                    new MobEffectInstance(MobEffects.SPEED, 400, 1, true, false, false));
        }
        if (n >= 3 && player.level().dimension() == Level.OVERWORLD && player.getY() > 120) {
            ensureEffect(player, new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0, true, false, false));
        }
    }

    private static void tickAmethystArmor(ServerPlayer player) {
        int n = pieces(player, "amethyst");
        if (n < 2) return;
        applyScaled(player, n,
                new MobEffectInstance(MobEffects.RESISTANCE, 280, 0, true, false, false),
                new MobEffectInstance(MobEffects.RESISTANCE, 340, 1, true, false, false),
                new MobEffectInstance(MobEffects.RESISTANCE, 420, 2, true, false, false));
        if (n >= 4 && player.tickCount % 55 == 0) {
            for (LivingEntity e : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(6),
                    ent -> ent != player && ent.isAlive() && ent instanceof Monster)) {
                e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 50, 0, true, false, false));
            }
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 3, 0.6, 0.4, 0.6, 0.01);
            }
        }
    }

    private static final java.util.Map<java.util.UUID, Long> coalSmokeCooldowns = new java.util.HashMap<>();

    private static void tickCoalArmor(ServerPlayer player) {
        int n = pieces(player, "coal");
        if (n < 2) return;
        applyScaled(player, n,
                new MobEffectInstance(MobEffects.NIGHT_VISION, 260, 0, true, false, false),
                new MobEffectInstance(MobEffects.NIGHT_VISION, 340, 0, true, false, false),
                new MobEffectInstance(MobEffects.NIGHT_VISION, 450, 0, true, false, false));
        if (n >= 4 && player.isShiftKeyDown() && player.level() instanceof ServerLevel sl) {
            long now = System.currentTimeMillis();
            long last = coalSmokeCooldowns.getOrDefault(player.getUUID(), 0L);
            if (now - last >= 30000) {
                coalSmokeCooldowns.put(player.getUUID(), now);
                for (LivingEntity e : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(5),
                        ent -> ent != player && ent.isAlive() && ent instanceof Monster)) {
                    e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0, true, false, false));
                }
                sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, player.getX(), player.getY() + 0.5, player.getZ(), 15, 1.5, 0.8, 1.5, 0.02);
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("SMOKE SCREEN!").withStyle(net.minecraft.ChatFormatting.DARK_GRAY, net.minecraft.ChatFormatting.BOLD), true);
            }
        }
    }

    private static void tickWarpedArmor(ServerPlayer player) {
        int n = pieces(player, "warped");
        if (n < 2) return;
        applyScaled(player, n,
                new MobEffectInstance(MobEffects.INVISIBILITY, 120, 0, true, false, false),
                new MobEffectInstance(MobEffects.SPEED, 280, 0, true, false, false),
                new MobEffectInstance(MobEffects.INVISIBILITY, 200, 0, true, false, false));
        if (n >= 4 && player.level().dimension() == Level.NETHER) {
            ensureEffect(player, new MobEffectInstance(MobEffects.STRENGTH, 200, 0, true, false, false));
        }
    }

    private static void tickWetSpongeArmor(ServerPlayer player) {
        int n = pieces(player, "wet_sponge");
        if (n < 2) return;
        if (player.isOnFire()) {
            player.clearFire();
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SPLASH, player.getX(), player.getEyeY(), player.getZ(), 5, 0.4, 0.2, 0.4, 0.06);
            }
        }
        applyScaled(player, n,
                new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0, true, false, false),
                new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 300, 0, true, false, false),
                new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 400, 1, true, false, false));
        if (n >= 4 && player.isInWater()) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, false, false));
        }
    }

    // ================================================================
    // NEW MATERIAL SETS
    // ================================================================

    private static void tickEmeraldArmor(ServerPlayer player) {
        int n = pieces(player, "emerald");
        if (n < 2) return;
        applyScaled(player, n,
                new MobEffectInstance(MobEffects.LUCK, 300, 0, true, false, false),
                new MobEffectInstance(MobEffects.LUCK, 400, 0, true, false, false),
                new MobEffectInstance(MobEffects.LUCK, 500, 1, true, false, false));
        if (n >= 4) {
            ensureEffect(player, new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 300, 0, true, false, false));
        }
    }

    private static void tickDiamondBlockArmor(ServerPlayer player) {
        int n = pieces(player, "diamond_block");
        if (n < 2) return;
        applyScaled(player, n,
                new MobEffectInstance(MobEffects.RESISTANCE, 300, 0, true, false, false),
                new MobEffectInstance(MobEffects.RESISTANCE, 400, 0, true, false, false),
                new MobEffectInstance(MobEffects.RESISTANCE, 500, 0, true, false, false));
        if (n >= 3) {
            ensureEffect(player, new MobEffectInstance(MobEffects.RESISTANCE, 400, 0, true, false, false));
        }
        if (n >= 4) {
            ensureEffect(player, new MobEffectInstance(MobEffects.ABSORPTION, 300, 1, true, false, false));
        }
    }

    private static void tickGoldBlockArmor(ServerPlayer player) {
        int n = pieces(player, "gold_block");
        if (n < 2) return;
        applyScaled(player, n,
                new MobEffectInstance(MobEffects.HASTE, 300, 0, true, false, false),
                new MobEffectInstance(MobEffects.LUCK, 400, 0, true, false, false),
                new MobEffectInstance(MobEffects.HASTE, 400, 1, true, false, false));
        if (n >= 3) {
            ensureEffect(player, new MobEffectInstance(MobEffects.LUCK, 400, 0, true, false, false));
        }
        if (n >= 4) {
            ensureEffect(player, new MobEffectInstance(MobEffects.LUCK, 500, 1, true, false, false));
        }
    }

    private static void tickCopperBlockArmor(ServerPlayer player) {
        int n = pieces(player, "copper_block");
        if (n < 2) return;
        applyScaled(player, n,
                new MobEffectInstance(MobEffects.SPEED, 300, 0, true, false, false),
                new MobEffectInstance(MobEffects.JUMP_BOOST, 300, 0, true, false, false),
                new MobEffectInstance(MobEffects.SPEED, 400, 1, true, false, false));
        if (n >= 3) {
            ensureEffect(player, new MobEffectInstance(MobEffects.JUMP_BOOST, 300, 0, true, false, false));
        }
        if (n >= 4 && player.level() instanceof ServerLevel sl) {
            ensureEffect(player, new MobEffectInstance(MobEffects.SPEED, 400, 1, true, false, false));
            if (sl.isThundering() && player.tickCount % 100 == 0
                    && player.level().canSeeSky(player.blockPosition())) {
                player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 100, 2, true, false, false));
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getEyeY(), player.getZ(), 5, 0.5, 0.3, 0.5, 0.1);
                sl.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.4f, 1.6f);
            }
        }
    }

    private static void tickAncientDebrisArmor(ServerPlayer player) {
        int n = pieces(player, "ancient_debris");
        if (n < 2) return;
        applyScaled(player, n,
                new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 300, 0, true, false, false),
                new MobEffectInstance(MobEffects.RESISTANCE, 400, 0, true, false, false),
                new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 500, 0, true, false, false));
        if (n >= 3) {
            ensureEffect(player, new MobEffectInstance(MobEffects.RESISTANCE, 400, 0, true, false, false));
        }
        if (n >= 4) {
            ensureEffect(player, new MobEffectInstance(MobEffects.STRENGTH, 300, 0, true, false, false));
        }
    }

    private static void tickSculkArmor(ServerPlayer player) {
        int n = pieces(player, "sculk");
        if (n < 2) return;
        applyScaled(player, n,
                new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, true, false, false),
                new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, true, false, false),
                new MobEffectInstance(MobEffects.NIGHT_VISION, 500, 0, true, false, false));
        if (n >= 3 && player.tickCount % 50 == 0) {
            for (LivingEntity e : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(4),
                    ent -> ent != player && ent.isAlive() && ent instanceof Monster)) {
                e.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 80, 0, true, false, false));
            }
        }
        if (n >= 4 && player.tickCount % 60 == 0 && player.level() instanceof ServerLevel sl) {
            LivingEntity target = player.getLastHurtMob();
            if (target != null && target.isAlive() && target.distanceTo(player) < 8) {
                target.hurtServer((ServerLevel) player.level(), player.damageSources().sonicBoom(player), 6.0f);
                sl.sendParticles(ParticleTypes.SONIC_BOOM, target.getX(), target.getEyeY(), target.getZ(), 1, 0, 0, 0, 0);
                sl.playSound(null, target.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.6f, 1.2f);
            }
        }
    }

    private static void tickLapisArmor(ServerPlayer player) {
        int n = pieces(player, "lapis");
        if (n < 2) return;
        applyScaled(player, n,
                new MobEffectInstance(MobEffects.SPEED, 300, 0, true, false, false),
                new MobEffectInstance(MobEffects.SPEED, 400, 0, true, false, false),
                new MobEffectInstance(MobEffects.LUCK, 500, 0, true, false, false));
        if (n >= 4) {
            ensureEffect(player, new MobEffectInstance(MobEffects.LUCK, 500, 1, true, false, false));
        }
    }

    private static void tickSlimeArmor(ServerPlayer player) {
        int n = pieces(player, "slime");
        if (n < 2) return;
        applyScaled(player, n,
                new MobEffectInstance(MobEffects.JUMP_BOOST, 300, 1, true, false, false),
                new MobEffectInstance(MobEffects.SLOW_FALLING, 300, 0, true, false, false),
                new MobEffectInstance(MobEffects.JUMP_BOOST, 400, 2, true, false, false));
        if (n >= 3) {
            ensureEffect(player, new MobEffectInstance(MobEffects.SLOW_FALLING, 300, 0, true, false, false));
        }
        if (n >= 4 && player.fallDistance > 3.0f && player.level() instanceof ServerLevel sl) {
            player.fallDistance = 0;
            sl.sendParticles(ParticleTypes.ITEM_SLIME, player.getX(), player.getY(), player.getZ(), 4, 0.4, 0.1, 0.4, 0.05);
            sl.playSound(null, player.blockPosition(), SoundEvents.SLIME_BLOCK_FALL, SoundSource.PLAYERS, 0.5f, 1.0f);
        }
    }

    // ================================================================
    // FAMILY-GROUPED TICK METHODS
    // ================================================================

    private static final String[] STONE_VARIANTS = {
        "basalt", "smooth_basalt", "polished_basalt",
        "granite", "polished_granite",
        "diorite", "polished_diorite",
        "andesite", "polished_andesite",
        "cobblestone", "mossy_cobblestone",
        "stone_bricks", "chiseled_stone_brick", "cracked_stone_brick",
        "tuff", "polished_tuff",
        "calcite", "brick", "dripstone", "smooth_stone"
    };

    private static void tickStoneVariants(ServerPlayer player) {
        for (String mat : STONE_VARIANTS) {
            int n = pieces(player, mat);
            if (n < 2) continue;
            ensureEffect(player, new MobEffectInstance(MobEffects.RESISTANCE, 280, 0, true, false, false));
            if (n >= 3) {
                ensureEffect(player, new MobEffectInstance(MobEffects.STRENGTH, 240, 0, true, false, false));
            }
            if (n >= 4) {
                ensureEffect(player, new MobEffectInstance(MobEffects.RESISTANCE, 400, 1, true, false, false));
                switch (mat) {
                    case "basalt", "smooth_basalt", "polished_basalt" ->
                        ensureEffect(player, new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0, true, false, false));
                    case "granite", "polished_granite" ->
                        ensureEffect(player, new MobEffectInstance(MobEffects.HASTE, 200, 0, true, false, false));
                    case "diorite", "polished_diorite" ->
                        ensureEffect(player, new MobEffectInstance(MobEffects.ABSORPTION, 200, 0, true, false, false));
                    case "andesite", "polished_andesite" ->
                        ensureEffect(player, new MobEffectInstance(MobEffects.SPEED, 200, 0, true, false, false));
                    case "cobblestone", "mossy_cobblestone" ->
                        ensureEffect(player, new MobEffectInstance(MobEffects.REGENERATION, 160, 0, true, false, false));
                    case "stone_bricks", "chiseled_stone_brick", "cracked_stone_brick" ->
                        ensureEffect(player, new MobEffectInstance(MobEffects.STRENGTH, 200, 1, true, false, false));
                    case "tuff", "polished_tuff" ->
                        ensureEffect(player, new MobEffectInstance(MobEffects.JUMP_BOOST, 200, 0, true, false, false));
                    case "calcite" ->
                        ensureEffect(player, new MobEffectInstance(MobEffects.LUCK, 200, 0, true, false, false));
                    case "brick" ->
                        ensureEffect(player, new MobEffectInstance(MobEffects.STRENGTH, 200, 0, true, false, false));
                    case "dripstone" -> {
                        if (player.tickCount % 50 == 0) {
                            for (LivingEntity e : player.level().getEntitiesOfClass(LivingEntity.class,
                                    player.getBoundingBox().inflate(3),
                                    ent -> ent != player && ent.isAlive() && ent instanceof Monster)) {
                                e.hurtServer((ServerLevel) player.level(), player.damageSources().magic(), 2.0f);
                            }
                        }
                    }
                    case "smooth_stone" ->
                        ensureEffect(player, new MobEffectInstance(MobEffects.RESISTANCE, 200, 1, true, false, false));
                    default -> {}
                }
            }
            return;
        }
    }

    private static final String[] DEEPSLATE_VARIANTS = {
        "cobbled_deepslate", "polished_deepslate", "chiseled_deepslate",
        "deepslate_tile", "deepslate_brick", "reinforced_deepslate",
        "deepslate"
    };

    private static void tickDeepslateVariants(ServerPlayer player) {
        for (String mat : DEEPSLATE_VARIANTS) {
            int n = pieces(player, mat);
            if (n < 2) continue;
            boolean underground = player.getY() < 0;
            if (n >= 2 && underground) {
                ensureEffect(player, new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, true, false, false));
            }
            if (n >= 2 && !underground) {
                ensureEffect(player, new MobEffectInstance(MobEffects.RESISTANCE, 200, 0, true, false, false));
            }
            if (n >= 3 && underground) {
                ensureEffect(player, new MobEffectInstance(MobEffects.RESISTANCE, 300, 0, true, false, false));
            }
            if (n >= 4) {
                if (underground) {
                    ensureEffect(player, new MobEffectInstance(MobEffects.RESISTANCE, 400, 1, true, false, false));
                    ensureEffect(player, new MobEffectInstance(MobEffects.STRENGTH, 300, 0, true, false, false));
                }
                if (player.tickCount % 40 == 0) {
                    for (LivingEntity e : player.level().getEntitiesOfClass(LivingEntity.class,
                            player.getBoundingBox().inflate(4),
                            ent -> ent != player && ent.isAlive() && ent instanceof Monster)) {
                        e.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0, true, false, false));
                    }
                }
                if (mat.equals("reinforced_deepslate")) {
                    ensureEffect(player, new MobEffectInstance(MobEffects.ABSORPTION, 300, 1, true, false, false));
                }
            }
            return;
        }
    }

    private static final String[] BLACKSTONE_VARIANTS = {
        "polished_blackstone", "blackstone_bricks", "gilded_blackstone", "blackstone"
    };

    private static void tickBlackstoneVariants(ServerPlayer player) {
        for (String mat : BLACKSTONE_VARIANTS) {
            int n = pieces(player, mat);
            if (n < 2) continue;
            boolean inNether = player.level().dimension() == Level.NETHER;
            if (n >= 2 && inNether) {
                ensureEffect(player, new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 300, 0, true, false, false));
            }
            if (n >= 2 && !inNether) {
                ensureEffect(player, new MobEffectInstance(MobEffects.RESISTANCE, 200, 0, true, false, false));
            }
            if (n >= 3 && inNether) {
                ensureEffect(player, new MobEffectInstance(MobEffects.RESISTANCE, 300, 0, true, false, false));
            }
            if (n >= 4 && inNether) {
                ensureEffect(player, new MobEffectInstance(MobEffects.STRENGTH, 300, 0, true, false, false));
                if (player.tickCount % 40 == 0) {
                    for (LivingEntity e : player.level().getEntitiesOfClass(LivingEntity.class,
                            player.getBoundingBox().inflate(4),
                            ent -> ent != player && ent.isAlive() && ent instanceof Monster)) {
                        e.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0, true, false, false));
                    }
                    if (player.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 0.4, player.getZ(), 3, 0.6, 0.2, 0.6, 0.01);
                    }
                }
            }
            if (mat.equals("gilded_blackstone") && n >= 4) {
                ensureEffect(player, new MobEffectInstance(MobEffects.LUCK, 300, 0, true, false, false));
            }
            return;
        }
    }

    private static final String[] NETHER_VARIANTS = {
        "nether_brick", "chiseled_nether_brick", "cracked_nether_brick",
        "crimson_stem", "nether_gold_ore", "nether_wart_block",
        "warped_wart_block", "netherite_block", "crying_obsidian", "shroomlight"
    };

    private static void tickNetherVariants(ServerPlayer player) {
        for (String mat : NETHER_VARIANTS) {
            int n = pieces(player, mat);
            if (n < 2) continue;
            boolean inNether = player.level().dimension() == Level.NETHER;
            ensureEffect(player, new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 300, 0, true, false, false));
            if (n >= 3 && inNether) {
                ensureEffect(player, new MobEffectInstance(MobEffects.STRENGTH, 260, 0, true, false, false));
            }
            if (n >= 4) {
                switch (mat) {
                    case "netherite_block" -> {
                        ensureEffect(player, new MobEffectInstance(MobEffects.RESISTANCE, 400, 1, true, false, false));
                        ensureEffect(player, new MobEffectInstance(MobEffects.STRENGTH, 400, 1, true, false, false));
                    }
                    case "crying_obsidian" -> {
                        ensureEffect(player, new MobEffectInstance(MobEffects.REGENERATION, 300, 0, true, false, false));
                        ensureEffect(player, new MobEffectInstance(MobEffects.ABSORPTION, 300, 0, true, false, false));
                        if (player.level() instanceof ServerLevel sl && player.tickCount % 60 == 0) {
                            sl.sendParticles(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, player.getX(), player.getEyeY(), player.getZ(), 3, 0.5, 0.3, 0.5, 0.01);
                        }
                    }
                    case "crimson_stem" -> {
                        ensureEffect(player, new MobEffectInstance(MobEffects.STRENGTH, 300, 0, true, false, false));
                        if (inNether) ensureEffect(player, new MobEffectInstance(MobEffects.SPEED, 200, 0, true, false, false));
                    }
                    case "nether_gold_ore" -> {
                        ensureEffect(player, new MobEffectInstance(MobEffects.HASTE, 300, 1, true, false, false));
                        ensureEffect(player, new MobEffectInstance(MobEffects.LUCK, 300, 0, true, false, false));
                    }
                    case "shroomlight" -> {
                        ensureEffect(player, new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, true, false, false));
                        if (player.tickCount % 50 == 0) {
                            for (LivingEntity e : player.level().getEntitiesOfClass(LivingEntity.class,
                                    player.getBoundingBox().inflate(5),
                                    ent -> ent != player && ent.isAlive() && ent instanceof Monster)) {
                                e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, true, false, false));
                            }
                        }
                    }
                    case "nether_wart_block" -> {
                        ensureEffect(player, new MobEffectInstance(MobEffects.REGENERATION, 200, 0, true, false, false));
                        if (inNether) ensureEffect(player, new MobEffectInstance(MobEffects.STRENGTH, 200, 0, true, false, false));
                    }
                    case "warped_wart_block" -> {
                        ensureEffect(player, new MobEffectInstance(MobEffects.SPEED, 300, 0, true, false, false));
                        if (inNether) ensureEffect(player, new MobEffectInstance(MobEffects.RESISTANCE, 200, 0, true, false, false));
                    }
                    default -> {
                        ensureEffect(player, new MobEffectInstance(MobEffects.STRENGTH, 200, 0, true, false, false));
                        ensureEffect(player, new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 400, 0, true, false, false));
                    }
                }
            }
            return;
        }
    }

    private static final String[] ICE_VARIANTS = {
        "packed_ice", "blue_ice", "snow_block", "snow"
    };

    private static void tickIceVariants(ServerPlayer player) {
        for (String mat : ICE_VARIANTS) {
            int n = pieces(player, mat);
            if (n < 2) continue;
            boolean cold = player.level().getBiome(player.blockPosition()).value()
                    .coldEnoughToSnow(player.blockPosition(), player.level().getSeaLevel());
            ensureEffect(player, new MobEffectInstance(MobEffects.SPEED, 260, 0, true, false, false));
            if (n >= 3 && cold) {
                ensureEffect(player, new MobEffectInstance(MobEffects.RESISTANCE, 260, 0, true, false, false));
            }
            if (n >= 4) {
                ensureEffect(player, new MobEffectInstance(MobEffects.SPEED, 360, 1, true, false, false));
                if (player.tickCount % 40 == 0) {
                    for (LivingEntity e : player.level().getEntitiesOfClass(LivingEntity.class,
                            player.getBoundingBox().inflate(3),
                            ent -> ent != player && ent.isAlive() && ent instanceof Monster)) {
                        e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 0, true, false, false));
                    }
                    if (player.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() + 0.5, player.getZ(), 3, 0.8, 0.3, 0.8, 0.01);
                    }
                }
                if (mat.equals("blue_ice")) {
                    ensureEffect(player, new MobEffectInstance(MobEffects.RESISTANCE, 300, 1, true, false, false));
                }
            }
            return;
        }
    }

    private static final String[] OCEAN_VARIANTS = {
        "prismarine_bricks", "dark_prismarine", "sea_lantern", "lodestone"
    };

    private static void tickOceanVariants(ServerPlayer player) {
        for (String mat : OCEAN_VARIANTS) {
            int n = pieces(player, mat);
            if (n < 2) continue;
            ensureEffect(player, new MobEffectInstance(MobEffects.WATER_BREATHING, 300, 0, true, false, false));
            if (n >= 3 && player.isInWater()) {
                ensureEffect(player, new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 300, 0, true, false, false));
            }
            if (n >= 4 && player.isInWater()) {
                ensureEffect(player, new MobEffectInstance(MobEffects.CONDUIT_POWER, 200, 0, true, false, false));
                ensureEffect(player, new MobEffectInstance(MobEffects.SPEED, 200, 0, true, false, false));
            }
            if (mat.equals("sea_lantern") && n >= 3) {
                ensureEffect(player, new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, true, false, false));
            }
            if (mat.equals("lodestone") && n >= 4) {
                ensureEffect(player, new MobEffectInstance(MobEffects.RESISTANCE, 200, 0, true, false, false));
            }
            return;
        }
    }

    private static final String[] END_VARIANTS = {
        "end_stone_brick", "end_stone", "purpur"
    };

    private static void tickEndVariants(ServerPlayer player) {
        for (String mat : END_VARIANTS) {
            int n = pieces(player, mat);
            if (n < 2) continue;
            ensureEffect(player, new MobEffectInstance(MobEffects.SLOW_FALLING, 280, 0, true, false, false));
            if (n >= 3) {
                ensureEffect(player, new MobEffectInstance(MobEffects.JUMP_BOOST, 300, 0, true, false, false));
            }
            if (n >= 4) {
                ensureEffect(player, new MobEffectInstance(MobEffects.RESISTANCE, 300, 0, true, false, false));
                float hpRatio = player.getHealth() / player.getMaxHealth();
                if (hpRatio < 0.2f && !player.hasEffect(MobEffects.LEVITATION)) {
                    player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 30, 1, true, false, false));
                    player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 60, 2, true, false, false));
                    if (player.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY(), player.getZ(), 5, 0.4, 0.4, 0.4, 0.05);
                    }
                }
                if (mat.equals("purpur")) {
                    ensureEffect(player, new MobEffectInstance(MobEffects.STRENGTH, 200, 0, true, false, false));
                }
            }
            return;
        }
    }

    private static final String[] NATURE_VARIANTS = {
        "bone_block", "bone_meal", "hay_block", "hay", "honeycomb_block", "honeycomb",
        "melon", "pumpkin", "lily_pad", "moss_block", "mycelium",
        "cactus", "target", "sponge", "sand"
    };

    private static void tickNatureVariants(ServerPlayer player) {
        for (String mat : NATURE_VARIANTS) {
            int n = pieces(player, mat);
            if (n < 2) continue;
            ensureEffect(player, new MobEffectInstance(MobEffects.REGENERATION, 260, 0, true, false, false));
            if (n >= 3) {
                ensureEffect(player, new MobEffectInstance(MobEffects.REGENERATION, 360, 0, true, false, false));
                ensureEffect(player, new MobEffectInstance(MobEffects.SATURATION, 40, 0, true, false, false));
            }
            if (n >= 4) {
                switch (mat) {
                    case "cactus" -> {
                        if (player.tickCount % 40 == 0) {
                            for (LivingEntity e : player.level().getEntitiesOfClass(LivingEntity.class,
                                    player.getBoundingBox().inflate(2),
                                    ent -> ent != player && ent.isAlive() && ent instanceof Monster)) {
                                e.hurtServer((ServerLevel) player.level(), player.damageSources().cactus(), 3.0f);
                            }
                            if (player.level() instanceof ServerLevel sl) {
                                sl.sendParticles(ParticleTypes.CRIT, player.getX(), player.getY() + 0.5, player.getZ(), 3, 0.6, 0.2, 0.6, 0.02);
                            }
                        }
                    }
                    case "pumpkin" ->
                        ensureEffect(player, new MobEffectInstance(MobEffects.INVISIBILITY, 200, 0, true, false, false));
                    case "melon" ->
                        ensureEffect(player, new MobEffectInstance(MobEffects.REGENERATION, 400, 1, true, false, false));
                    case "bone_block", "bone_meal" -> {
                        ensureEffect(player, new MobEffectInstance(MobEffects.STRENGTH, 200, 0, true, false, false));
                        ensureEffect(player, new MobEffectInstance(MobEffects.RESISTANCE, 200, 0, true, false, false));
                    }
                    case "hay", "hay_block" ->
                        ensureEffect(player, new MobEffectInstance(MobEffects.SATURATION, 60, 0, true, false, false));
                    case "honeycomb", "honeycomb_block" -> {
                        ensureEffect(player, new MobEffectInstance(MobEffects.ABSORPTION, 200, 0, true, false, false));
                        ensureEffect(player, new MobEffectInstance(MobEffects.REGENERATION, 300, 0, true, false, false));
                    }
                    case "lily_pad" -> {
                        ensureEffect(player, new MobEffectInstance(MobEffects.SPEED, 200, 0, true, false, false));
                        if (player.isInWater())
                            ensureEffect(player, new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 200, 0, true, false, false));
                    }
                    case "moss_block" ->
                        ensureEffect(player, new MobEffectInstance(MobEffects.REGENERATION, 400, 1, true, false, false));
                    case "mycelium" -> {
                        ensureEffect(player, new MobEffectInstance(MobEffects.RESISTANCE, 200, 0, true, false, false));
                        if (player.level().getMaxLocalRawBrightness(player.blockPosition()) < 8)
                            ensureEffect(player, new MobEffectInstance(MobEffects.STRENGTH, 200, 0, true, false, false));
                    }
                    case "target" -> {
                        ensureEffect(player, new MobEffectInstance(MobEffects.SPEED, 200, 1, true, false, false));
                        ensureEffect(player, new MobEffectInstance(MobEffects.JUMP_BOOST, 200, 0, true, false, false));
                    }
                    case "sponge" -> {
                        ensureEffect(player, new MobEffectInstance(MobEffects.WATER_BREATHING, 300, 0, true, false, false));
                        if (player.isInWater()) player.heal(0.25f);
                    }
                    case "sand" -> {
                        ensureEffect(player, new MobEffectInstance(MobEffects.SPEED, 200, 0, true, false, false));
                        ensureEffect(player, new MobEffectInstance(MobEffects.JUMP_BOOST, 200, 0, true, false, false));
                    }
                    default ->
                        ensureEffect(player, new MobEffectInstance(MobEffects.REGENERATION, 360, 0, true, false, false));
                }
            }
            return;
        }
    }

    private static final String[] WOOD_PREFIXES = {
        "oak_log", "spruce_log", "birch_log", "dark_oak_log", "jungle_log",
        "acacia_log", "mangrove_log", "cherry_log",
        "oak_planks", "spruce_planks", "birch_planks", "jungle_planks",
        "acacia_planks", "dark_oak_planks", "mangrove_planks", "cherry_planks",
        "bamboo_planks", "crimson_planks", "warped_planks",
        "stripped_oak_log", "stripped_spruce_log", "stripped_birch_log",
        "stripped_dark_oak_log", "stripped_jungle_log", "stripped_acacia_log",
        "stripped_mangrove_log", "stripped_cherry_log",
        "stripped_bamboo_block", "stripped_crimson_stem", "stripped_warped_stem",
        "bamboo_block", "mangrove_roots", "muddy_mangrove_roots"
    };

    private static void tickWoodVariants(ServerPlayer player) {
        for (String mat : WOOD_PREFIXES) {
            int n = pieces(player, mat);
            if (n < 2) continue;
            ensureEffect(player, new MobEffectInstance(MobEffects.REGENERATION, 260, 0, true, false, false));
            if (n >= 3) {
                ensureEffect(player, new MobEffectInstance(MobEffects.SPEED, 260, 0, true, false, false));
            }
            if (n >= 4) {
                ensureEffect(player, new MobEffectInstance(MobEffects.STRENGTH, 260, 0, true, false, false));
                boolean forestBiome = player.level().getBiome(player.blockPosition()).unwrapKey()
                        .map(k -> k.identifier().getPath().contains("forest")).orElse(false);
                if (forestBiome) {
                    ensureEffect(player, new MobEffectInstance(MobEffects.SPEED, 300, 1, true, false, false));
                    ensureEffect(player, new MobEffectInstance(MobEffects.REGENERATION, 300, 1, true, false, false));
                }
                if (mat.contains("mangrove") || mat.contains("muddy")) {
                    if (player.isInWater())
                        ensureEffect(player, new MobEffectInstance(MobEffects.WATER_BREATHING, 200, 0, true, false, false));
                }
                if (mat.contains("crimson") || mat.contains("warped")) {
                    if (player.level().dimension() == Level.NETHER)
                        ensureEffect(player, new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0, true, false, false));
                }
            }
            return;
        }
    }

    private static final String[] COPPER_VARIANTS = {
        "copper_ingot", "chiseled_copper", "cut_copper", "waxed_cut_copper",
        "exposed_copper", "weathered_copper", "oxidised_copper"
    };

    private static void tickCopperVariants(ServerPlayer player) {
        for (String mat : COPPER_VARIANTS) {
            int n = pieces(player, mat);
            if (n < 2) continue;
            ensureEffect(player, new MobEffectInstance(MobEffects.SPEED, 280, 0, true, false, false));
            if (n >= 3) {
                ensureEffect(player, new MobEffectInstance(MobEffects.HASTE, 280, 0, true, false, false));
            }
            if (n >= 4) {
                ensureEffect(player, new MobEffectInstance(MobEffects.SPEED, 360, 1, true, false, false));
                if (player.level() instanceof ServerLevel sl && sl.isThundering()
                        && player.tickCount % 100 == 0
                        && player.level().canSeeSky(player.blockPosition())) {
                    player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 60, 2, true, false, false));
                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getEyeY(), player.getZ(), 4, 0.4, 0.3, 0.4, 0.08);
                    sl.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.3f, 1.8f);
                }
                if (mat.equals("oxidised_copper")) {
                    ensureEffect(player, new MobEffectInstance(MobEffects.RESISTANCE, 200, 0, true, false, false));
                } else if (mat.equals("waxed_cut_copper")) {
                    ensureEffect(player, new MobEffectInstance(MobEffects.ABSORPTION, 200, 0, true, false, false));
                }
            }
            return;
        }
    }

    private static final String[] MISC_IRON = { "iron_block", "iron_nugget", "raw_iron_block" };
    private static final String[] MISC_GOLD = { "gold_nugget", "raw_gold_block" };
    private static final String[] MISC_EARTH = { "flint", "charcoal", "mud_bricks", "mud_brick", "soul_soil", "red_sandstone", "tnt" };
    private static final String[] MISC_FROGLIGHT = { "verdant_froglight", "pearlescent_froglight", "ochre_froglight" };

    private static void tickMiscVariants(ServerPlayer player) {
        for (String mat : MISC_IRON) {
            int n = pieces(player, mat);
            if (n < 2) continue;
            ensureEffect(player, new MobEffectInstance(MobEffects.RESISTANCE, 280, 0, true, false, false));
            if (n >= 3) ensureEffect(player, new MobEffectInstance(MobEffects.STRENGTH, 240, 0, true, false, false));
            if (n >= 4) {
                ensureEffect(player, new MobEffectInstance(MobEffects.RESISTANCE, 400, 1, true, false, false));
                if (mat.equals("raw_iron_block"))
                    ensureEffect(player, new MobEffectInstance(MobEffects.HASTE, 200, 1, true, false, false));
            }
            return;
        }

        for (String mat : MISC_GOLD) {
            int n = pieces(player, mat);
            if (n < 2) continue;
            ensureEffect(player, new MobEffectInstance(MobEffects.HASTE, 280, 0, true, false, false));
            if (n >= 3) ensureEffect(player, new MobEffectInstance(MobEffects.LUCK, 300, 0, true, false, false));
            if (n >= 4) {
                ensureEffect(player, new MobEffectInstance(MobEffects.LUCK, 400, 1, true, false, false));
                if (mat.equals("raw_gold_block"))
                    ensureEffect(player, new MobEffectInstance(MobEffects.HASTE, 200, 1, true, false, false));
            }
            return;
        }

        for (String mat : MISC_EARTH) {
            int n = pieces(player, mat);
            if (n < 2) continue;
            ensureEffect(player, new MobEffectInstance(MobEffects.RESISTANCE, 240, 0, true, false, false));
            if (n >= 3) ensureEffect(player, new MobEffectInstance(MobEffects.STRENGTH, 200, 0, true, false, false));
            if (n >= 4) {
                switch (mat) {
                    case "flint" -> {
                        ensureEffect(player, new MobEffectInstance(MobEffects.STRENGTH, 300, 1, true, false, false));
                        ensureEffect(player, new MobEffectInstance(MobEffects.HASTE, 200, 0, true, false, false));
                    }
                    case "charcoal" -> {
                        ensureEffect(player, new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, true, false, false));
                        ensureEffect(player, new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0, true, false, false));
                    }
                    case "mud_brick", "mud_bricks" -> {
                        ensureEffect(player, new MobEffectInstance(MobEffects.RESISTANCE, 350, 1, true, false, false));
                        ensureEffect(player, new MobEffectInstance(MobEffects.REGENERATION, 200, 0, true, false, false));
                    }
                    case "soul_soil" -> {
                        ensureEffect(player, new MobEffectInstance(MobEffects.SPEED, 200, 0, true, false, false));
                        if (player.tickCount % 40 == 0) {
                            for (LivingEntity e : player.level().getEntitiesOfClass(LivingEntity.class,
                                    player.getBoundingBox().inflate(3),
                                    ent -> ent != player && ent.isAlive() && ent instanceof Monster)) {
                                e.addEffect(new MobEffectInstance(MobEffects.WITHER, 50, 0, true, false, false));
                            }
                            if (player.level() instanceof ServerLevel sl) {
                                sl.sendParticles(ParticleTypes.SOUL, player.getX(), player.getY() + 0.4, player.getZ(), 3, 0.6, 0.2, 0.6, 0.02);
                            }
                        }
                    }
                    case "red_sandstone" -> {
                        ensureEffect(player, new MobEffectInstance(MobEffects.SPEED, 300, 1, true, false, false));
                        ensureEffect(player, new MobEffectInstance(MobEffects.JUMP_BOOST, 200, 0, true, false, false));
                    }
                    case "tnt" -> {
                        ensureEffect(player, new MobEffectInstance(MobEffects.SPEED, 300, 1, true, false, false));
                        if (player.tickCount % 80 == 0 && player.level() instanceof ServerLevel sl) {
                            for (LivingEntity e : player.level().getEntitiesOfClass(LivingEntity.class,
                                    player.getBoundingBox().inflate(3),
                                    ent -> ent != player && ent.isAlive() && ent instanceof Monster)) {
                                e.hurtServer(sl, player.damageSources().explosion(null), 4.0f);
                            }
                            sl.sendParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY() + 0.5, player.getZ(), 3, 0.5, 0.3, 0.5, 0);
                            sl.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.5f, 1.2f);
                        }
                    }
                    default -> ensureEffect(player, new MobEffectInstance(MobEffects.RESISTANCE, 300, 0, true, false, false));
                }
            }
            return;
        }

        for (String mat : MISC_FROGLIGHT) {
            int n = pieces(player, mat);
            if (n < 2) continue;
            ensureEffect(player, new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, true, false, false));
            if (n >= 3) {
                ensureEffect(player, new MobEffectInstance(MobEffects.SPEED, 260, 0, true, false, false));
                if (player.tickCount % 40 == 0) {
                    for (LivingEntity e : player.level().getEntitiesOfClass(LivingEntity.class,
                            player.getBoundingBox().inflate(6),
                            ent -> ent != player && ent.isAlive() && ent instanceof Monster)) {
                        e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, true, false, false));
                    }
                }
            }
            if (n >= 4) {
                switch (mat) {
                    case "verdant_froglight" -> {
                        ensureEffect(player, new MobEffectInstance(MobEffects.REGENERATION, 300, 0, true, false, false));
                        ensureEffect(player, new MobEffectInstance(MobEffects.JUMP_BOOST, 200, 1, true, false, false));
                    }
                    case "pearlescent_froglight" -> {
                        ensureEffect(player, new MobEffectInstance(MobEffects.LUCK, 300, 0, true, false, false));
                        ensureEffect(player, new MobEffectInstance(MobEffects.ABSORPTION, 200, 0, true, false, false));
                    }
                    case "ochre_froglight" -> {
                        ensureEffect(player, new MobEffectInstance(MobEffects.HASTE, 300, 1, true, false, false));
                        ensureEffect(player, new MobEffectInstance(MobEffects.STRENGTH, 200, 0, true, false, false));
                    }
                    default -> {}
                }
            }
            return;
        }
    }

    // ================================================================
    // NEW 25 MATERIAL ARMOR SETS
    // ================================================================

    private static final String[] NEW_25_MATERIALS = {
        "raw_prismarine", "clay", "glazed_terracotta", "concrete", "concrete_powder",
        "bookshelf", "barrel", "smithing_table", "anvil", "grindstone",
        "lectern", "composter", "stonecutter", "dried_kelp", "coral_block",
        "lantern", "chain", "lightning_rod", "tinted_glass", "candle",
        "sculk_sensor", "sculk_catalyst", "sculk_shrieker", "brewing_stand", "furnace"
    };

    private static void tickNew25Materials(ServerPlayer player) {
        for (String mat : NEW_25_MATERIALS) {
            int n = pieces(player, mat);
            if (n < 2) continue;

            switch (mat) {
                case "raw_prismarine", "coral_block", "dried_kelp" -> {
                    ensureEffect(player, new MobEffectInstance(MobEffects.WATER_BREATHING, 280, 0, true, false, false));
                    if (n >= 3 && player.isInWater())
                        ensureEffect(player, new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 260, 0, true, false, false));
                    if (n >= 4 && player.isInWater()) {
                        ensureEffect(player, new MobEffectInstance(MobEffects.CONDUIT_POWER, 200, 0, true, false, false));
                        ensureEffect(player, new MobEffectInstance(MobEffects.SPEED, 200, 0, true, false, false));
                    }
                }
                case "clay", "concrete_powder" -> {
                    ensureEffect(player, new MobEffectInstance(MobEffects.RESISTANCE, 260, 0, true, false, false));
                    if (n >= 3)
                        ensureEffect(player, new MobEffectInstance(MobEffects.STRENGTH, 240, 0, true, false, false));
                    if (n >= 4)
                        ensureEffect(player, new MobEffectInstance(MobEffects.RESISTANCE, 360, 1, true, false, false));
                }
                case "glazed_terracotta", "concrete" -> {
                    ensureEffect(player, new MobEffectInstance(MobEffects.RESISTANCE, 280, 0, true, false, false));
                    if (n >= 3)
                        ensureEffect(player, new MobEffectInstance(MobEffects.RESISTANCE, 340, 1, true, false, false));
                    if (n >= 4) {
                        ensureEffect(player, new MobEffectInstance(MobEffects.RESISTANCE, 420, 1, true, false, false));
                        ensureEffect(player, new MobEffectInstance(MobEffects.STRENGTH, 200, 0, true, false, false));
                    }
                }
                case "bookshelf", "lectern" -> {
                    ensureEffect(player, new MobEffectInstance(MobEffects.LUCK, 280, 0, true, false, false));
                    if (n >= 3)
                        ensureEffect(player, new MobEffectInstance(MobEffects.HASTE, 280, 0, true, false, false));
                    if (n >= 4) {
                        ensureEffect(player, new MobEffectInstance(MobEffects.LUCK, 400, 1, true, false, false));
                        ensureEffect(player, new MobEffectInstance(MobEffects.SPEED, 200, 0, true, false, false));
                    }
                }
                case "barrel", "composter" -> {
                    ensureEffect(player, new MobEffectInstance(MobEffects.REGENERATION, 260, 0, true, false, false));
                    if (n >= 3)
                        ensureEffect(player, new MobEffectInstance(MobEffects.SATURATION, 40, 0, true, false, false));
                    if (n >= 4) {
                        ensureEffect(player, new MobEffectInstance(MobEffects.REGENERATION, 360, 0, true, false, false));
                        ensureEffect(player, new MobEffectInstance(MobEffects.LUCK, 200, 0, true, false, false));
                    }
                }
                case "smithing_table", "anvil", "grindstone", "stonecutter" -> {
                    ensureEffect(player, new MobEffectInstance(MobEffects.RESISTANCE, 280, 0, true, false, false));
                    if (n >= 3) {
                        ensureEffect(player, new MobEffectInstance(MobEffects.HASTE, 280, 0, true, false, false));
                        ensureEffect(player, new MobEffectInstance(MobEffects.STRENGTH, 240, 0, true, false, false));
                    }
                    if (n >= 4) {
                        ensureEffect(player, new MobEffectInstance(MobEffects.RESISTANCE, 400, 1, true, false, false));
                        if (mat.equals("anvil"))
                            ensureEffect(player, new MobEffectInstance(MobEffects.ABSORPTION, 200, 0, true, false, false));
                    }
                }
                case "lantern", "candle" -> {
                    ensureEffect(player, new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, true, false, false));
                    if (n >= 3) {
                        ensureEffect(player, new MobEffectInstance(MobEffects.SPEED, 260, 0, true, false, false));
                        if (player.tickCount % 40 == 0) {
                            for (LivingEntity e : player.level().getEntitiesOfClass(LivingEntity.class,
                                    player.getBoundingBox().inflate(6),
                                    ent -> ent != player && ent.isAlive() && ent instanceof Monster)) {
                                e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, true, false, false));
                            }
                        }
                    }
                    if (n >= 4)
                        ensureEffect(player, new MobEffectInstance(MobEffects.REGENERATION, 200, 0, true, false, false));
                }
                case "chain" -> {
                    ensureEffect(player, new MobEffectInstance(MobEffects.RESISTANCE, 280, 0, true, false, false));
                    if (n >= 3)
                        ensureEffect(player, new MobEffectInstance(MobEffects.STRENGTH, 260, 0, true, false, false));
                    if (n >= 4) {
                        ensureEffect(player, new MobEffectInstance(MobEffects.RESISTANCE, 400, 1, true, false, false));
                        ensureEffect(player, new MobEffectInstance(MobEffects.SPEED, 200, 0, true, false, false));
                    }
                }
                case "lightning_rod" -> {
                    ensureEffect(player, new MobEffectInstance(MobEffects.SPEED, 280, 0, true, false, false));
                    if (n >= 3)
                        ensureEffect(player, new MobEffectInstance(MobEffects.HASTE, 280, 0, true, false, false));
                    if (n >= 4 && player.level() instanceof ServerLevel sl) {
                        ensureEffect(player, new MobEffectInstance(MobEffects.SPEED, 360, 1, true, false, false));
                        if (sl.isThundering() && player.tickCount % 100 == 0
                                && player.level().canSeeSky(player.blockPosition())) {
                            player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 80, 2, true, false, false));
                            sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getEyeY(), player.getZ(), 5, 0.5, 0.3, 0.5, 0.1);
                        }
                    }
                }
                case "tinted_glass" -> {
                    ensureEffect(player, new MobEffectInstance(MobEffects.SPEED, 260, 0, true, false, false));
                    if (n >= 3)
                        ensureEffect(player, new MobEffectInstance(MobEffects.SPEED, 320, 1, true, false, false));
                    if (n >= 4) {
                        ensureEffect(player, new MobEffectInstance(MobEffects.SPEED, 400, 2, true, false, false));
                        if (player.tickCount % 60 == 0)
                            ensureEffect(player, new MobEffectInstance(MobEffects.INVISIBILITY, 25, 0, true, false, false));
                    }
                }
                case "sculk_sensor", "sculk_catalyst", "sculk_shrieker" -> {
                    ensureEffect(player, new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, true, false, false));
                    if (n >= 3 && player.tickCount % 50 == 0) {
                        for (LivingEntity e : player.level().getEntitiesOfClass(LivingEntity.class,
                                player.getBoundingBox().inflate(5),
                                ent -> ent != player && ent.isAlive() && ent instanceof Monster)) {
                            e.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 80, 0, true, false, false));
                        }
                    }
                    if (n >= 4) {
                        ensureEffect(player, new MobEffectInstance(MobEffects.RESISTANCE, 300, 0, true, false, false));
                        if (mat.equals("sculk_shrieker") && player.tickCount % 60 == 0 && player.level() instanceof ServerLevel sl) {
                            LivingEntity target = player.getLastHurtMob();
                            if (target != null && target.isAlive() && target.distanceTo(player) < 8) {
                                target.hurtServer(sl, player.damageSources().sonicBoom(player), 4.0f);
                                sl.sendParticles(ParticleTypes.SONIC_BOOM, target.getX(), target.getEyeY(), target.getZ(), 1, 0, 0, 0, 0);
                            }
                        }
                    }
                }
                case "brewing_stand" -> {
                    ensureEffect(player, new MobEffectInstance(MobEffects.REGENERATION, 260, 0, true, false, false));
                    if (n >= 3) {
                        ensureEffect(player, new MobEffectInstance(MobEffects.SPEED, 260, 0, true, false, false));
                        ensureEffect(player, new MobEffectInstance(MobEffects.RESISTANCE, 200, 0, true, false, false));
                    }
                    if (n >= 4) {
                        ensureEffect(player, new MobEffectInstance(MobEffects.REGENERATION, 400, 1, true, false, false));
                        ensureEffect(player, new MobEffectInstance(MobEffects.STRENGTH, 200, 0, true, false, false));
                    }
                }
                case "furnace" -> {
                    ensureEffect(player, new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 300, 0, true, false, false));
                    if (n >= 3) {
                        ensureEffect(player, new MobEffectInstance(MobEffects.HASTE, 280, 0, true, false, false));
                    }
                    if (n >= 4) {
                        ensureEffect(player, new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 450, 0, true, false, false));
                        ensureEffect(player, new MobEffectInstance(MobEffects.STRENGTH, 200, 0, true, false, false));
                        if (player.tickCount % 40 == 0) {
                            for (LivingEntity e : player.level().getEntitiesOfClass(LivingEntity.class,
                                    player.getBoundingBox().inflate(3),
                                    ent -> ent != player && ent.isAlive() && ent instanceof Monster)) {
                                e.setRemainingFireTicks(60);
                            }
                            if (player.level() instanceof ServerLevel sl) {
                                sl.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 0.3, player.getZ(), 3, 0.6, 0.2, 0.6, 0.02);
                            }
                        }
                    }
                }
                default -> {
                    ensureEffect(player, new MobEffectInstance(MobEffects.RESISTANCE, 260, 0, true, false, false));
                    if (n >= 3) ensureEffect(player, new MobEffectInstance(MobEffects.STRENGTH, 200, 0, true, false, false));
                    if (n >= 4) ensureEffect(player, new MobEffectInstance(MobEffects.RESISTANCE, 360, 1, true, false, false));
                }
            }
            return;
        }
    }
}
