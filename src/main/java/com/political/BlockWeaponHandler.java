package com.political;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Abilities for block-themed swords ({@code *_sword} custom IDs). Damage scales in {@link CustomItemHandler#calculateSlayerDamage}.
 */
public class BlockWeaponHandler {

    private static final Map<UUID, Integer> quartzCritCounters = new HashMap<>();
    private static final Map<UUID, Long> redstoneStunCooldowns = new HashMap<>();
    private static final Map<UUID, Integer> targetBullseyeCounters = new HashMap<>();
    private static final Map<UUID, Integer> strippedLogHitCounters = new HashMap<>();
    private static final Map<UUID, Integer> bambooHitCounters = new HashMap<>();
    private static final Map<UUID, Long> amethystClusterEchoCooldowns = new HashMap<>();

    public static boolean isPoliticalBlockSword(ItemStack weapon) {
        if (weapon == null || weapon.isEmpty()) return false;
        if (SlayerItems.isSlayerSword(weapon) || SlayerItems.isMidasSword(weapon)) return false;
        String id = SlayerItems.stripSkyblockEnchantSuffix(SlayerItems.getCustomItemId(weapon));
        return id != null && id.endsWith("_sword");
    }

    /**
     * Apply block-sword damage multipliers and on-hit effects (called from damage pipeline once per hit).
     */
    public static float processDamage(ServerPlayer player, LivingEntity target, float baseDamage) {
        ItemStack weapon = player.getMainHandItem();
        if (!isPoliticalBlockSword(weapon)) {
            return baseDamage;
        }

        String id = SlayerItems.stripSkyblockEnchantSuffix(SlayerItems.getCustomItemId(weapon));
        String material = id.substring(0, id.length() - "_sword".length());
        float d = baseDamage * 1.30f; // Global uplift for block weapons.

        switch (material) {
            // ── EXISTING CASES ──────────────────────────────────────────────
            case "glass" -> {
                if (player.getRandom().nextFloat() < 0.32f) {
                    d *= 1.65f;
                    player.sendSystemMessage(Component.literal("SHATTER STRIKE!").withStyle(net.minecraft.ChatFormatting.AQUA, net.minecraft.ChatFormatting.BOLD), true);
                }
                weapon.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
            }
            case "obsidian" -> {
                d *= 1.32f;
                target.knockback(1.0f, player.getX() - target.getX(), player.getZ() - target.getZ());
            }
            case "quartz" -> {
                int hits = quartzCritCounters.merge(player.getUUID(), 1, Integer::sum);
                if (hits % 3 == 0) {
                    d *= 2.25f;
                    target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120, 0, true, false, false));
                    player.sendSystemMessage(Component.literal("CRYSTAL CRIT!").withStyle(net.minecraft.ChatFormatting.WHITE, net.minecraft.ChatFormatting.BOLD), true);
                }
            }
            case "glowstone" -> {
                if (target.level().getLightEngine().getRawBrightness(target.blockPosition(), 0) < 10
                        && target.getType().builtInRegistryHolder().is(net.minecraft.tags.EntityTypeTags.UNDEAD)) {
                    d *= 1.45f;
                }
                if (player.getHealth() < player.getMaxHealth()) {
                    player.heal(1.0f);
                }
            }
            case "redstone" -> tryRedstoneStun(player, target);
            case "netherrack" -> {
                target.setRemainingFireTicks(120);
                if (player.level().dimensionType().hasCeiling()) {
                    d *= 1.32f;
                }
            }
            case "endstone" -> {
                if (player.getRandom().nextFloat() < 0.08f) {
                    Vec3 tp = new Vec3(target.getX() + (player.getRandom().nextDouble() - 0.5) * 8,
                            target.getY(), target.getZ() + (player.getRandom().nextDouble() - 0.5) * 8);
                    target.teleportTo(tp.x, tp.y, tp.z);
                    player.sendSystemMessage(Component.literal("VOID SHIFT!").withStyle(net.minecraft.ChatFormatting.DARK_PURPLE, net.minecraft.ChatFormatting.BOLD), true);
                }
            }
            case "ice" -> {
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1, true, false, false));
                d *= target.hasEffect(MobEffects.SLOWNESS) ? 1.22f : 1.08f;
                if (player.level() instanceof ServerLevel sw) {
                    sw.sendParticles(net.minecraft.core.particles.ParticleTypes.SNOWFLAKE,
                            target.getX(), target.getY() + 1, target.getZ(), 4, 0.5, 0.5, 0.5, 0.1);
                }
            }
            case "prismarine" -> {
                if (player.isInWater() || player.level().isRaining()) {
                    d *= 1.38f;
                }
                target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 50, 0, true, false, false));
            }
            case "terracotta" -> {
                target.knockback(1.35f, player.getX() - target.getX(), player.getZ() - target.getZ());
                if (player.onGround()) {
                    d *= 1.18f;
                }
            }
            case "mossy" -> {
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0, true, false, false));
                d *= 1.1f;
            }
            case "soulsand", "soul_sand" -> {
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 1, true, false, false));
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 50, 0, true, false, false));
            }
            case "magma" -> {
                target.setRemainingFireTicks(160);
                d *= 1.12f;
            }
            case "sandstone" -> {
                if (player.level().dimension() == net.minecraft.world.level.Level.OVERWORLD && player.getY() > 100) {
                    d *= 1.15f;
                }
            }
            case "amethyst" -> {
                if (player.getRandom().nextFloat() < 0.22f) {
                    d *= 1.35f;
                    target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, true, false, false));
                }
            }
            case "coal" -> {
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0, true, false, false));
                d *= 1.14f;
            }
            case "diamond_block" -> d *= 1.2f;
            case "emerald_block" -> {
                d *= 1.12f;
                if (player.getRandom().nextFloat() < 0.15f) {
                    CoinManager.giveCoinsQuiet(player, 2);
                }
            }
            case "gold_block" -> d *= 1.1f;
            case "iron_block", "copper_block" -> d *= 1.15f;
            case "lapis_block" -> {
                d *= 1.1f;
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, true, false, false));
            }
            case "ancient_debris" -> d *= 1.25f;
            case "sculk" -> {
                target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0, true, false, false));
                d *= 1.18f;
            }
            case "slime" -> {
                target.knockback(0.9f, player.getX() - target.getX(), player.getZ() - target.getZ());
                d *= 1.12f;
            }
            case "cactus" -> {
                target.hurtServer(player.level(), player.damageSources().cactus(), 2.0f);
                d *= 1.1f;
            }
            case "tnt" -> {
                d *= 1.28f;
                if (player.getRandom().nextFloat() < 0.1f && player.level() instanceof ServerLevel sw) {
                    sw.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION,
                            target.getX(), target.getY() + 0.5, target.getZ(), 3, 0.4, 0.4, 0.4, 0.02);
                    target.hurtServer(sw, player.damageSources().playerAttack(player), 4.0f);
                }
            }
            case "emerald" -> {
                d *= 1.2f;
                if (player.getRandom().nextFloat() < 0.2f) {
                    CoinManager.giveCoinsQuiet(player, 1 + player.getRandom().nextInt(4));
                }
            }
            case "lapis" -> {
                d *= 1.18f;
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 0, true, false, false));
                if (player.getRandom().nextFloat() < 0.18f) {
                    target.hurtServer(player.level(), player.damageSources().magic(), 4.0f);
                }
            }

            // ── STONE VARIANTS ──────────────────────────────────────────────
            case "basalt", "smooth_basalt", "polished_basalt" -> {
                d *= 1.18f;
                if (target.level().getBlockState(target.blockPosition().below()).is(net.minecraft.tags.BlockTags.BASE_STONE_OVERWORLD)) {
                    target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0, true, false, false));
                }
            }
            case "blackstone", "polished_blackstone", "blackstone_bricks", "gilded_blackstone" -> {
                d *= 1.22f;
                if (player.level().dimensionType().hasCeiling()) {
                    target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0, true, false, false));
                }
            }
            case "granite", "polished_granite" -> {
                d *= 1.15f;
                target.knockback(0.6f, player.getX() - target.getX(), player.getZ() - target.getZ());
            }
            case "diorite", "polished_diorite" -> {
                d *= 1.15f;
                target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 40, 0, true, false, false));
            }
            case "andesite", "polished_andesite" -> {
                d *= 1.15f;
            }
            case "cobblestone", "mossy_cobblestone" -> {
                d *= 1.12f;
                if (player.getRandom().nextFloat() < 0.35f) {
                    target.knockback(0.5f + player.getRandom().nextFloat() * 0.5f,
                            player.getX() - target.getX(), player.getZ() - target.getZ());
                }
            }
            case "stone_bricks", "chiseled_stone_brick", "cracked_stone_brick" -> {
                d *= 1.18f;
                if (target.getArmorValue() > 8) {
                    d *= 1.1f;
                }
            }
            case "deepslate", "cobbled_deepslate", "polished_deepslate", "chiseled_deepslate",
                 "deepslate_tile", "deepslate_brick" -> {
                d *= 1.25f;
                if (target.getY() < 0) {
                    target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, 0, true, false, false));
                }
            }
            case "tuff", "polished_tuff" -> {
                d *= 1.15f;
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20, 0, true, false, false));
                if (player.level() instanceof ServerLevel sw) {
                    sw.sendParticles(net.minecraft.core.particles.ParticleTypes.ASH,
                            target.getX(), target.getY() + 1, target.getZ(), 5, 0.4, 0.5, 0.4, 0.05);
                }
            }
            case "dripstone" -> {
                d *= 1.2f;
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 30, 0, true, false, false));
            }
            case "calcite" -> {
                d *= 1.18f;
                target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 50, 0, true, false, false));
            }
            case "brick" -> {
                d *= 1.15f;
                if (player.onGround()) {
                    target.knockback(0.7f, player.getX() - target.getX(), player.getZ() - target.getZ());
                }
            }
            case "reinforced_deepslate" -> {
                d *= 1.4f;
                weapon.hurtAndBreak(3, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
                player.sendSystemMessage(Component.literal("SEISMIC IMPACT!").withStyle(net.minecraft.ChatFormatting.DARK_GRAY, net.minecraft.ChatFormatting.BOLD), true);
            }

            // ── NETHER VARIANTS ─────────────────────────────────────────────
            case "nether_brick", "chiseled_nether_brick", "cracked_nether_brick" -> {
                d *= 1.2f;
                if (player.level().dimensionType().hasCeiling()) {
                    d *= 1.15f;
                }
            }
            case "crimson_stem" -> {
                d *= 1.18f;
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 40, 0, true, false, false));
                if (player.level().getBiome(player.blockPosition()).is(net.minecraft.world.level.biome.Biomes.CRIMSON_FOREST)) {
                    d *= 1.1f;
                }
            }
            case "soul_soil" -> {
                d *= 1.15f;
                target.knockback(0.6f, target.getX() - player.getX(), target.getZ() - player.getZ());
            }
            case "nether_gold_ore" -> {
                d *= 1.15f;
                if (player.getRandom().nextFloat() < 0.15f) {
                    CoinManager.giveCoinsQuiet(player, 1 + player.getRandom().nextInt(2));
                    player.sendSystemMessage(Component.literal("GOLD RUSH!").withStyle(net.minecraft.ChatFormatting.GOLD), true);
                }
            }
            case "nether_wart_block", "warped_wart_block" -> {
                d *= 1.15f;
                float roll = player.getRandom().nextFloat();
                if (roll < 0.33f) {
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, true, false, false));
                } else if (roll < 0.66f) {
                    player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 0, true, false, false));
                } else {
                    player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 40, 0, true, false, false));
                }
            }
            case "shroomlight" -> {
                d *= 1.12f;
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, true, false, false));
                player.heal(0.5f);
            }
            case "netherite_block" -> {
                d *= 1.45f;
                weapon.hurtAndBreak(5, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
                player.sendSystemMessage(Component.literal("NETHERITE CRUSH!").withStyle(net.minecraft.ChatFormatting.DARK_RED, net.minecraft.ChatFormatting.BOLD), true);
            }
            case "crying_obsidian" -> {
                d *= 1.3f;
                target.hurtServer(player.level(), player.damageSources().magic(), 3.0f);
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20, 0, true, false, false));
                if (player.level() instanceof ServerLevel sw) {
                    sw.sendParticles(net.minecraft.core.particles.ParticleTypes.REVERSE_PORTAL,
                            target.getX(), target.getY() + 1, target.getZ(), 4, 0.3, 0.5, 0.3, 0.05);
                }
            }

            // ── ICE VARIANTS ────────────────────────────────────────────────
            case "packed_ice" -> {
                d *= 1.2f;
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1, true, false, false));
            }
            case "blue_ice" -> {
                d *= 1.3f;
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2, true, false, false));
                if (player.level() instanceof ServerLevel sw) {
                    sw.sendParticles(net.minecraft.core.particles.ParticleTypes.SNOWFLAKE,
                            target.getX(), target.getY() + 1, target.getZ(), 5, 0.4, 0.5, 0.4, 0.08);
                }
            }
            case "snow", "snow_block" -> {
                d *= 1.1f;
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0, true, false, false));
                if (player.level() instanceof ServerLevel sw) {
                    sw.sendParticles(net.minecraft.core.particles.ParticleTypes.SNOWFLAKE,
                            target.getX(), target.getY() + 1, target.getZ(), 3, 0.3, 0.5, 0.3, 0.06);
                }
            }

            // ── OCEAN VARIANTS ──────────────────────────────────────────────
            case "prismarine_bricks", "dark_prismarine" -> {
                d *= 1.22f;
                target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 50, 0, true, false, false));
                if (player.isInWater()) {
                    d *= 1.15f;
                }
            }
            case "sea_lantern" -> {
                d *= 1.15f;
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, true, false, false));
                if (player.isInWater()) {
                    d *= 1.12f;
                }
            }
            case "lodestone" -> {
                d *= 1.2f;
                target.knockback(0.5f, target.getX() - player.getX(), target.getZ() - player.getZ());
                player.sendSystemMessage(Component.literal("MAGNETIC PULL!").withStyle(net.minecraft.ChatFormatting.GRAY), true);
            }

            // ── END VARIANTS ────────────────────────────────────────────────
            case "end_stone", "end_stone_brick" -> {
                d *= 1.2f;
                if (player.getRandom().nextFloat() < 0.05f) {
                    target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 20, 0, true, false, false));
                    player.sendSystemMessage(Component.literal("VOID LIFT!").withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE), true);
                }
            }
            case "purpur" -> {
                d *= 1.22f;
                if (player.getRandom().nextFloat() < 0.08f) {
                    double dist = 3.0 + player.getRandom().nextDouble() * 2.0;
                    double angle = player.getRandom().nextDouble() * Math.PI * 2;
                    target.teleportTo(target.getX() + Math.cos(angle) * dist, target.getY(),
                            target.getZ() + Math.sin(angle) * dist);
                    player.sendSystemMessage(Component.literal("CHORUS WARP!").withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE, net.minecraft.ChatFormatting.BOLD), true);
                }
            }

            // ── PLANT / NATURE ──────────────────────────────────────────────
            case "bone_block" -> {
                d *= 1.18f;
                if (target.getType().builtInRegistryHolder().is(net.minecraft.tags.EntityTypeTags.UNDEAD)) {
                    d *= 1.2f;
                }
            }
            case "bone_meal" -> {
                d *= 1.08f;
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20, 0, true, false, false));
                if (player.level() instanceof ServerLevel sw) {
                    sw.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                            player.getX(), player.getY() + 1, player.getZ(), 3, 0.3, 0.5, 0.3, 0.05);
                }
            }
            case "hay" -> {
                d *= 1.1f;
                target.knockback(1.2f, player.getX() - target.getX(), player.getZ() - target.getZ());
            }
            case "honeycomb" -> {
                d *= 1.12f;
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 1, true, false, false));
            }
            case "melon" -> {
                d *= 1.1f;
                player.heal(1.0f);
            }
            case "pumpkin" -> {
                d *= 1.12f;
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0, true, false, false));
            }
            case "lily_pad" -> {
                d *= 1.08f;
                if (player.isInWater()) {
                    d *= 1.3f;
                }
            }
            case "moss_block" -> {
                d *= 1.1f;
                MobEffectInstance existing = target.getEffect(MobEffects.POISON);
                int dur = (existing != null) ? existing.getDuration() + 40 : 40;
                target.addEffect(new MobEffectInstance(MobEffects.POISON, dur, 0, true, false, false));
            }
            case "mycelium" -> {
                d *= 1.15f;
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 50, 0, true, false, false));
                if (player.level() instanceof ServerLevel sw) {
                    sw.sendParticles(net.minecraft.core.particles.ParticleTypes.MYCELIUM,
                            target.getX(), target.getY() + 0.5, target.getZ(), 5, 0.5, 0.5, 0.5, 0.08);
                }
            }
            case "sand", "red_sandstone" -> {
                d *= 1.12f;
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 25, 0, true, false, false));
                if (player.level().getBiome(player.blockPosition()).is(net.minecraft.tags.BiomeTags.IS_BADLANDS)
                        || player.level().getBiome(player.blockPosition()).is(net.minecraft.world.level.biome.Biomes.DESERT)) {
                    d *= 1.15f;
                }
            }
            case "target" -> {
                d *= 1.15f;
                int count = targetBullseyeCounters.merge(player.getUUID(), 1, Integer::sum);
                if (count % 5 == 0) {
                    d *= 2.0f;
                    player.sendSystemMessage(Component.literal("BULLSEYE!").withStyle(net.minecraft.ChatFormatting.RED, net.minecraft.ChatFormatting.BOLD), true);
                }
            }
            case "sponge" -> {
                d *= 1.1f;
                if (target.getRemainingFireTicks() > 0) {
                    target.setRemainingFireTicks(0);
                    player.sendSystemMessage(Component.literal("EXTINGUISH!").withStyle(net.minecraft.ChatFormatting.YELLOW), true);
                }
            }
            case "wet_sponge" -> {
                d *= 1.12f;
                if (target.getRemainingFireTicks() > 0) {
                    target.setRemainingFireTicks(0);
                }
                if (target.getType() == net.minecraft.world.entity.EntityType.BLAZE) {
                    d *= 1.3f;
                }
            }

            // ── WOOD VARIANTS ───────────────────────────────────────────────
            case "oak_log", "birch_log", "spruce_log", "dark_oak_log", "jungle_log",
                 "acacia_log", "mangrove_log", "cherry_log" -> {
                d *= 1.08f;
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 20, 0, true, false, false));
            }
            case "stripped_oak_log", "stripped_birch_log", "stripped_spruce_log",
                 "stripped_dark_oak_log", "stripped_jungle_log", "stripped_acacia_log",
                 "stripped_mangrove_log", "stripped_cherry_log",
                 "stripped_crimson_stem", "stripped_warped_stem" -> {
                d *= 1.1f;
                int strHits = strippedLogHitCounters.merge(player.getUUID(), 1, Integer::sum);
                if (strHits % 3 == 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.SPEED, 30, 0, true, false, false));
                }
            }
            case "oak_planks", "birch_planks", "spruce_planks", "dark_oak_planks",
                 "jungle_planks", "acacia_planks", "mangrove_planks", "cherry_planks",
                 "crimson_planks", "warped_planks", "bamboo_planks" -> {
                d *= 1.06f;
                if (player.getRandom().nextFloat() < 0.2f) {
                    target.addEffect(new MobEffectInstance(MobEffects.POISON, 15, 0, true, false, false));
                }
            }
            case "bamboo_block" -> {
                d *= 1.08f;
                int bHits = bambooHitCounters.merge(player.getUUID(), 1, Integer::sum);
                if (bHits % 2 == 0) {
                    d *= 1.15f;
                }
            }
            case "mangrove_roots", "muddy_mangrove_roots" -> {
                d *= 1.1f;
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 2, true, false, false));
                player.sendSystemMessage(Component.literal("ROOT SNARE!").withStyle(net.minecraft.ChatFormatting.DARK_GREEN), true);
            }

            // ── COPPER VARIANTS ─────────────────────────────────────────────
            case "copper_ingot" -> {
                d *= 1.12f;
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 0, true, false, false));
                if (player.level() instanceof ServerLevel sw) {
                    for (LivingEntity nearby : sw.getEntitiesOfClass(LivingEntity.class,
                            target.getBoundingBox().inflate(3.0), e -> e != player && e != target && e.isAlive() && e.isInWaterOrRain())) {
                        nearby.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20, 0, true, false, false));
                    }
                }
            }
            case "chiseled_copper", "cut_copper", "waxed_cut_copper" -> {
                d *= 1.15f;
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 30, 0, true, false, false));
            }
            case "exposed_copper", "weathered_copper", "oxidised_copper" -> {
                d *= 1.12f;
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 30, 0, true, false, false));
            }

            // ── MISC ────────────────────────────────────────────────────────
            case "iron_nugget" -> {
                d *= 1.05f;
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 20, 0, true, false, false));
            }
            case "gold_nugget" -> {
                d *= 1.05f;
                if (player.getRandom().nextFloat() < 0.05f) {
                    CoinManager.giveCoinsQuiet(player, 1);
                    player.sendSystemMessage(Component.literal("LUCKY COIN!").withStyle(net.minecraft.ChatFormatting.GOLD), true);
                }
            }
            case "lapis_lazuli" -> {
                d *= 1.18f;
                if (player.getRandom().nextFloat() < 0.15f) {
                    target.hurtServer(player.level(), player.damageSources().magic(), 3.0f);
                    if (player.level() instanceof ServerLevel sw) {
                        sw.sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANT,
                                target.getX(), target.getY() + 1, target.getZ(), 4, 0.4, 0.5, 0.4, 0.1);
                    }
                }
            }
            case "amethyst_shard" -> {
                d *= 1.2f;
                if (player.getRandom().nextFloat() < 0.25f) {
                    target.hurtServer(player.level(), player.damageSources().magic(), 2.0f);
                }
            }
            case "amethyst_cluster" -> {
                d *= 1.22f;
                UUID pUuid = player.getUUID();
                long now = System.currentTimeMillis();
                if (now - amethystClusterEchoCooldowns.getOrDefault(pUuid, 0L) >= 1200L) {
                    amethystClusterEchoCooldowns.put(pUuid, now);
                    if (player.level() instanceof ServerLevel sw) {
                        sw.getServer().execute(() -> {
                            if (target.isAlive()) {
                                target.hurtServer(sw, player.damageSources().magic(), 1.5f);
                            }
                        });
                    }
                }
            }
            case "flint" -> {
                d *= 1.1f;
                if (player.getRandom().nextFloat() < 0.15f) {
                    target.setRemainingFireTicks(40);
                }
            }
            case "charcoal" -> {
                d *= 1.12f;
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0, true, false, false));
                if (player.getRandom().nextFloat() < 0.2f) {
                    target.setRemainingFireTicks(30);
                }
            }
            case "mud_brick", "mud_bricks" -> {
                d *= 1.12f;
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1, true, false, false));
            }
            case "raw_iron_block" -> {
                d *= 1.2f;
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1, true, false, false));
                player.sendSystemMessage(Component.literal("MAGNETIC DRAG!").withStyle(net.minecraft.ChatFormatting.GRAY), true);
            }
            case "raw_gold_block" -> {
                d *= 1.18f;
                if (target.getType() == net.minecraft.world.entity.EntityType.PIGLIN
                        || target.getType() == net.minecraft.world.entity.EntityType.PIGLIN_BRUTE
                        || target.getType() == net.minecraft.world.entity.EntityType.ZOMBIFIED_PIGLIN) {
                    d *= 1.25f;
                }
            }
            case "verdant_froglight" -> {
                d *= 1.1f;
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 20, 0, true, false, false));
                if (player.level() instanceof ServerLevel sw) {
                    sw.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                            target.getX(), target.getY() + 1, target.getZ(), 3, 0.3, 0.5, 0.3, 0.05);
                }
            }
            case "pearlescent_froglight" -> {
                d *= 1.1f;
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, true, false, false));
                if (player.level() instanceof ServerLevel sw) {
                    sw.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,
                            target.getX(), target.getY() + 1, target.getZ(), 3, 0.3, 0.5, 0.3, 0.05);
                }
            }
            case "ochre_froglight" -> {
                d *= 1.1f;
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 30, 0, true, false, false));
                if (player.level() instanceof ServerLevel sw) {
                    sw.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME,
                            target.getX(), target.getY() + 1, target.getZ(), 3, 0.3, 0.5, 0.3, 0.02);
                }
            }

            // ── NEW 25 MATERIALS ─────────────────────────────────────────
            case "raw_prismarine" -> {
                d *= 1.2f;
                if (player.isInWater() || player.level().isRaining()) d *= 1.25f;
                target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 60, 0, true, false, false));
            }
            case "clay" -> {
                d *= 1.12f;
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 1, true, false, false));
                if (player.onGround()) d *= 1.1f;
            }
            case "glazed_terracotta" -> {
                d *= 1.18f;
                target.knockback(1.2f, player.getX() - target.getX(), player.getZ() - target.getZ());
                if (player.getRandom().nextFloat() < 0.15f) {
                    target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 25, 0, true, false, false));
                }
            }
            case "concrete" -> {
                d *= 1.2f;
                target.knockback(0.8f, player.getX() - target.getX(), player.getZ() - target.getZ());
            }
            case "concrete_powder" -> {
                d *= 1.12f;
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0, true, false, false));
                if (player.level() instanceof ServerLevel sw) {
                    sw.sendParticles(net.minecraft.core.particles.ParticleTypes.DUST_PLUME,
                            target.getX(), target.getY() + 1, target.getZ(), 4, 0.5, 0.5, 0.5, 0.08);
                }
            }
            case "bookshelf" -> {
                d *= 1.15f;
                if (player.getRandom().nextFloat() < 0.2f) {
                    target.hurtServer(player.level(), player.damageSources().magic(), 3.0f);
                    player.sendSystemMessage(Component.literal("ARCANE STRIKE!").withStyle(net.minecraft.ChatFormatting.DARK_PURPLE, net.minecraft.ChatFormatting.BOLD), true);
                }
            }
            case "barrel" -> {
                d *= 1.14f;
                if (player.getRandom().nextFloat() < 0.12f) {
                    CoinManager.giveCoinsQuiet(player, 1 + player.getRandom().nextInt(3));
                    player.sendSystemMessage(Component.literal("LOOT FIND!").withStyle(net.minecraft.ChatFormatting.GOLD), true);
                }
            }
            case "smithing_table" -> {
                d *= 1.22f;
                if (target.getArmorValue() > 6) d *= 1.15f;
                player.sendSystemMessage(Component.literal("FORGED STRIKE!").withStyle(net.minecraft.ChatFormatting.GRAY), true);
            }
            case "anvil" -> {
                d *= 1.3f;
                target.knockback(1.5f, player.getX() - target.getX(), player.getZ() - target.getZ());
                weapon.hurtAndBreak(2, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
            }
            case "grindstone" -> {
                d *= 1.18f;
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, true, false, false));
                target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 40, 0, true, false, false));
            }
            case "lectern" -> {
                d *= 1.15f;
                if (player.getRandom().nextFloat() < 0.2f) {
                    target.hurtServer(player.level(), player.damageSources().magic(), 2.5f);
                }
                player.addEffect(new MobEffectInstance(MobEffects.LUCK, 60, 0, true, false, false));
            }
            case "composter" -> {
                d *= 1.1f;
                player.heal(1.0f);
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 40, 0, true, false, false));
            }
            case "stonecutter" -> {
                d *= 1.25f;
                if (player.getRandom().nextFloat() < 0.2f) {
                    target.hurtServer(player.level(), player.damageSources().playerAttack(player), 3.0f);
                    player.sendSystemMessage(Component.literal("PRECISION CUT!").withStyle(net.minecraft.ChatFormatting.GRAY, net.minecraft.ChatFormatting.BOLD), true);
                }
            }
            case "dried_kelp" -> {
                d *= 1.1f;
                if (player.isInWater()) d *= 1.3f;
                player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 80, 0, true, false, false));
            }
            case "coral_block" -> {
                d *= 1.18f;
                if (player.isInWater()) d *= 1.2f;
                target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 50, 0, true, false, false));
                if (player.level() instanceof ServerLevel sw) {
                    sw.sendParticles(net.minecraft.core.particles.ParticleTypes.BUBBLE,
                            target.getX(), target.getY() + 1, target.getZ(), 4, 0.5, 0.5, 0.5, 0.1);
                }
            }
            case "lantern" -> {
                d *= 1.15f;
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0, true, false, false));
                if (target.level().getLightEngine().getRawBrightness(target.blockPosition(), 0) < 8) {
                    d *= 1.2f;
                }
            }
            case "chain" -> {
                d *= 1.2f;
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1, true, false, false));
                player.sendSystemMessage(Component.literal("SHACKLE!").withStyle(net.minecraft.ChatFormatting.DARK_GRAY), true);
            }
            case "lightning_rod" -> {
                d *= 1.22f;
                if (player.getRandom().nextFloat() < 0.12f) {
                    target.hurtServer(player.level(), player.damageSources().magic(), 5.0f);
                    if (player.level() instanceof ServerLevel sw) {
                        sw.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                                target.getX(), target.getY() + 1, target.getZ(), 5, 0.4, 0.5, 0.4, 0.15);
                    }
                    player.sendSystemMessage(Component.literal("LIGHTNING BOLT!").withStyle(net.minecraft.ChatFormatting.YELLOW, net.minecraft.ChatFormatting.BOLD), true);
                }
            }
            case "tinted_glass" -> {
                d *= 1.18f;
                target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, 0, true, false, false));
                if (player.getRandom().nextFloat() < 0.25f) {
                    d *= 1.3f;
                    player.sendSystemMessage(Component.literal("SHADOW STRIKE!").withStyle(net.minecraft.ChatFormatting.DARK_GRAY, net.minecraft.ChatFormatting.BOLD), true);
                }
            }
            case "candle" -> {
                d *= 1.12f;
                target.setRemainingFireTicks(60);
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, true, false, false));
            }
            case "sculk_sensor" -> {
                d *= 1.2f;
                target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 50, 0, true, false, false));
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 0, true, false, false));
            }
            case "sculk_catalyst" -> {
                d *= 1.22f;
                target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0, true, false, false));
                if (player.level() instanceof ServerLevel sw) {
                    sw.sendParticles(net.minecraft.core.particles.ParticleTypes.SCULK_CHARGE_POP,
                            target.getX(), target.getY() + 1, target.getZ(), 4, 0.5, 0.5, 0.5, 0.05);
                }
            }
            case "sculk_shrieker" -> {
                d *= 1.25f;
                target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 80, 0, true, false, false));
                if (player.getRandom().nextFloat() < 0.1f && player.level() instanceof ServerLevel sw) {
                    sw.sendParticles(net.minecraft.core.particles.ParticleTypes.SONIC_BOOM,
                            target.getX(), target.getEyeY(), target.getZ(), 1, 0, 0, 0, 0);
                    target.hurtServer(sw, player.damageSources().sonicBoom(player), 4.0f);
                    player.sendSystemMessage(Component.literal("WARDEN'S FURY!").withStyle(net.minecraft.ChatFormatting.DARK_AQUA, net.minecraft.ChatFormatting.BOLD), true);
                }
            }
            case "brewing_stand" -> {
                d *= 1.18f;
                float roll = player.getRandom().nextFloat();
                if (roll < 0.33f) {
                    target.addEffect(new MobEffectInstance(MobEffects.POISON, 50, 0, true, false, false));
                } else if (roll < 0.66f) {
                    target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 50, 0, true, false, false));
                } else {
                    target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 1, true, false, false));
                }
            }
            case "furnace" -> {
                d *= 1.2f;
                target.setRemainingFireTicks(100);
                if (player.level() instanceof ServerLevel sw) {
                    sw.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME,
                            target.getX(), target.getY() + 1, target.getZ(), 4, 0.4, 0.5, 0.4, 0.08);
                }
            }

            default -> {
                d *= 1.22f;
                if (player.getRandom().nextFloat() < 0.18f) {
                    target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 50, 0, true, false, false));
                }
            }
        }

        spawnHitVisual(player, target, material);

        return d * 1.28f;
    }

    /** Material-themed particle puff on every block-sword hit so all 150+ swords have visible feedback. */
    private static void spawnHitVisual(ServerPlayer player, LivingEntity target, String material) {
        if (!(player.level() instanceof ServerLevel sw)) return;
        net.minecraft.core.particles.ParticleOptions particle = hitParticleFor(material);
        sw.sendParticles(particle,
                target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(),
                5, 0.35, 0.3, 0.35, 0.05);
    }

    private static net.minecraft.core.particles.ParticleOptions hitParticleFor(String m) {
        if (m.contains("magma") || m.contains("nether") || m.contains("furnace") || m.contains("blaze")
                || m.contains("lava") || m.contains("fire") || m.contains("crimson"))
            return net.minecraft.core.particles.ParticleTypes.FLAME;
        if (m.contains("soul")) return net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME;
        if (m.contains("ice") || m.contains("snow")) return net.minecraft.core.particles.ParticleTypes.SNOWFLAKE;
        if (m.contains("prismarine") || m.contains("sponge") || m.contains("kelp") || m.contains("sea"))
            return net.minecraft.core.particles.ParticleTypes.BUBBLE_POP;
        if (m.contains("copper") || m.contains("redstone") || m.contains("lightning"))
            return net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK;
        if (m.contains("end") || m.contains("purpur") || m.contains("obsidian") || m.contains("void"))
            return net.minecraft.core.particles.ParticleTypes.PORTAL;
        if (m.contains("sculk") || m.contains("blackstone") || m.contains("coal") || m.contains("charcoal")
                || m.contains("dark")) return net.minecraft.core.particles.ParticleTypes.SMOKE;
        if (m.contains("glow") || m.contains("shroomlight") || m.contains("lantern") || m.contains("froglight"))
            return net.minecraft.core.particles.ParticleTypes.GLOW;
        if (m.contains("amethyst") || m.contains("diamond") || m.contains("quartz") || m.contains("glass")
                || m.contains("calcite")) return net.minecraft.core.particles.ParticleTypes.END_ROD;
        if (m.contains("gold") || m.contains("gilded") || m.contains("honey") || m.contains("wax"))
            return net.minecraft.core.particles.ParticleTypes.WAX_ON;
        if (m.contains("emerald") || m.contains("moss") || m.contains("cactus") || m.contains("melon")
                || m.contains("bamboo") || m.contains("leaf") || m.contains("leaves") || m.contains("fern")
                || m.contains("lily") || m.contains("verdant")) return net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER;
        if (m.contains("cherry")) return net.minecraft.core.particles.ParticleTypes.CHERRY_LEAVES;
        if (m.contains("warped") || m.contains("mycelium")) return net.minecraft.core.particles.ParticleTypes.WARPED_SPORE;
        if (m.contains("slime")) return net.minecraft.core.particles.ParticleTypes.ITEM_SLIME;
        if (m.contains("lapis") || m.contains("enchant") || m.contains("book"))
            return net.minecraft.core.particles.ParticleTypes.ENCHANT;
        if (m.contains("log") || m.contains("planks") || m.contains("wood") || m.contains("stem")
                || m.contains("hay") || m.contains("stripped")) return net.minecraft.core.particles.ParticleTypes.COMPOSTER;
        if (m.contains("sand") || m.contains("tuff") || m.contains("ash"))
            return net.minecraft.core.particles.ParticleTypes.POOF;
        return net.minecraft.core.particles.ParticleTypes.CRIT;
    }

    private static void tryRedstoneStun(ServerPlayer player, LivingEntity target) {
        UUID u = player.getUUID();
        long now = System.currentTimeMillis();
        if (now - redstoneStunCooldowns.getOrDefault(u, 0L) < 3000L) {
            return;
        }
        redstoneStunCooldowns.put(u, now);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 3, true, false, false));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 45, 0, true, false, false));
        player.sendSystemMessage(Component.literal("ELECTRIC STUN!").withStyle(net.minecraft.ChatFormatting.RED, net.minecraft.ChatFormatting.BOLD), true);
        if (player.level() instanceof ServerLevel world) {
            for (LivingEntity nearby : world.getEntitiesOfClass(LivingEntity.class,
                    target.getBoundingBox().inflate(3.5), e -> e != player && e != target && e.isAlive())) {
                nearby.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 25, 1, true, false, false));
            }
        }
    }

    public static void resetCounters(UUID playerUuid) {
        quartzCritCounters.remove(playerUuid);
        targetBullseyeCounters.remove(playerUuid);
        strippedLogHitCounters.remove(playerUuid);
        bambooHitCounters.remove(playerUuid);
        amethystClusterEchoCooldowns.remove(playerUuid);
    }

    /** @deprecated Prefer {@link #isPoliticalBlockSword(ItemStack)} */
    @Deprecated
    public static boolean isBlockSword(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Component customName = stack.get(DataComponents.CUSTOM_NAME);
        if (customName != null) {
            String name = customName.getString();
            if (name.contains("Blade") || name.contains("Sword")) {
                return name.contains("Glass") || name.contains("Obsidian") || name.contains("Quartz")
                        || name.contains("Glowstone") || name.contains("Redstone") || name.contains("Netherrack")
                        || name.contains("End Stone") || name.contains("Ice") || name.contains("Prismarine")
                        || name.contains("Terracotta") || name.contains("Mossy") || name.contains("Soul Sand")
                        || name.contains("Magma") || name.contains("Sandstone") || name.contains("Amethyst")
                        || name.contains("Coal");
            }
        }
        return isPoliticalBlockSword(stack);
    }
}
