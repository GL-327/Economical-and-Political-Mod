package com.political;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handles special abilities for block-themed tools.
 * Each tool type has unique abilities based on its block material.
 * Updated: Push test
 */
public class BlockToolHandler {

    private static final Map<UUID, Long> abilityCooldowns = new HashMap<>();
    private static final long COOLDOWN_MS = 3000;
    private static final Map<UUID, Integer> quartzHitCounter = new HashMap<>();
    private static boolean legacyToolHandlersEnabled() { return true; }

    // ============================================================
    // TOOL TYPE DETECTION
    // ============================================================

    public static boolean isBlockTool(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        String id = SlayerItems.stripSkyblockEnchantSuffix(SlayerItems.getCustomItemId(stack));
        if (id == null) return false;
        return id.endsWith("_pickaxe") || id.endsWith("_axe") || 
               id.endsWith("_shovel") || id.endsWith("_hoe");
    }

    public static String getToolMaterial(ItemStack stack) {
        String id = SlayerItems.stripSkyblockEnchantSuffix(SlayerItems.getCustomItemId(stack));
        if (id == null) return null;
        String[] suffixes = {"_pickaxe", "_shovel", "_hoe", "_axe"};
        for (String suf : suffixes) {
            if (id.endsWith(suf)) {
                return id.substring(0, id.length() - suf.length());
            }
        }
        return null;
    }

    public static String getToolType(ItemStack stack) {
        String id = SlayerItems.stripSkyblockEnchantSuffix(SlayerItems.getCustomItemId(stack));
        if (id == null) return null;
        if (id.endsWith("_pickaxe")) return "pickaxe";
        if (id.endsWith("_shovel")) return "shovel";
        if (id.endsWith("_hoe")) return "hoe";
        if (id.endsWith("_axe")) return "axe";
        return null;
    }

    // ============================================================
    // ABILITY TRIGGERS
    // ============================================================

    public static void onBlockBreak(ServerPlayer player, Level world, BlockPos pos, BlockState state, ItemStack tool) {
        if (!isBlockTool(tool)) return;
        
        String material = getToolMaterial(tool);
        String toolType = getToolType(tool);
        if (material == null || toolType == null) return;

        applyUniqueToolSetOnBreak(player, world, pos, state, toolType, material);
        applyPerMaterialToolIdentityOnBreak(player, world, pos, state, toolType, material);
        if ("pickaxe".equals(toolType)) {
            applyPickaxeMaterialSignature(player, world, pos, state, toolType, material);
        }
        if (!legacyToolHandlersEnabled()) return;

        // Apply material-specific abilities
        switch (material) {
            case "glass" -> handleGlassToolBreak(player, world, pos, state, toolType);
            case "obsidian" -> handleObsidianToolBreak(player, world, pos, state, toolType);
            case "quartz" -> handleQuartzToolBreak(player, world, pos, state, toolType);
            case "glowstone" -> handleGlowstoneToolBreak(player, world, pos, state, toolType);
            case "redstone" -> handleRedstoneToolBreak(player, world, pos, state, toolType);
            case "netherrack" -> handleNetherrackToolBreak(player, world, pos, state, toolType);
            case "endstone" -> handleEndstoneToolBreak(player, world, pos, state, toolType);
            case "ice" -> handleIceToolBreak(player, world, pos, state, toolType);
            case "prismarine" -> handlePrismarineToolBreak(player, world, pos, state, toolType);
            case "terracotta" -> handleTerracottaToolBreak(player, world, pos, state, toolType);
            case "mossy" -> handleMossyToolBreak(player, world, pos, state, toolType);
            case "soul_sand" -> handleSoulSandToolBreak(player, world, pos, state, toolType);
            case "magma" -> handleMagmaToolBreak(player, world, pos, state, toolType);
            case "sandstone" -> handleSandstoneToolBreak(player, world, pos, state, toolType);
            case "amethyst" -> handleAmethystToolBreak(player, world, pos, state, toolType);
            case "coal" -> handleCoalToolBreak(player, world, pos, state, toolType);
            case "emerald" -> handleGemEmeraldToolBreak(player, world, pos, state, toolType);
            case "lapis" -> handleGemLapisToolBreak(player, world, pos, state, toolType);
            case "sculk" -> handleSculkToolBreak(player, world, pos, state, toolType);

            // --- STONE FAMILY ---
            case "basalt", "smooth_basalt", "polished_basalt",
                 "granite", "polished_granite",
                 "diorite", "polished_diorite",
                 "andesite", "polished_andesite",
                 "cobblestone",
                 "stone_bricks", "chiseled_stone_brick", "cracked_stone_brick",
                 "tuff", "polished_tuff",
                 "calcite", "brick", "dripstone", "smooth_stone" -> handleStoneToolBreak(player, world, pos, state, toolType);

            // --- DEEPSLATE FAMILY ---
            case "deepslate", "cobbled_deepslate", "polished_deepslate",
                 "chiseled_deepslate", "deepslate_tile", "deepslate_brick",
                 "reinforced_deepslate" -> handleDeepslateToolBreak(player, world, pos, state, toolType);

            // --- BLACKSTONE FAMILY ---
            case "blackstone", "polished_blackstone",
                 "blackstone_bricks", "gilded_blackstone" -> handleBlackstoneToolBreak(player, world, pos, state, toolType);

            // --- NETHER FAMILY ---
            case "nether_brick", "chiseled_nether_brick", "cracked_nether_brick",
                 "crimson_stem", "nether_gold_ore", "nether_wart_block",
                 "warped_wart_block", "netherite_block",
                 "crying_obsidian", "shroomlight" -> handleNetherToolBreak(player, world, pos, state, toolType, material);

            // --- ICE FAMILY ---
            case "packed_ice", "blue_ice",
                 "snow", "snow_block" -> handleIceFamilyToolBreak(player, world, pos, state, toolType);

            // --- OCEAN FAMILY ---
            case "prismarine_bricks", "dark_prismarine",
                 "sea_lantern", "lodestone" -> handleOceanToolBreak(player, world, pos, state, toolType);

            // --- END FAMILY ---
            case "end_stone", "end_stone_brick",
                 "purpur" -> handleEndFamilyToolBreak(player, world, pos, state, toolType);

            // --- NATURE FAMILY ---
            case "bone_block", "bone_meal",
                 "hay", "hay_block",
                 "honeycomb", "honeycomb_block",
                 "melon", "pumpkin", "lily_pad", "moss_block",
                 "mycelium", "cactus", "target", "sponge",
                 "sand", "red_sandstone" -> handleNatureToolBreak(player, world, pos, state, toolType);

            // --- WOOD FAMILY ---
            case "oak_log", "birch_log", "spruce_log", "dark_oak_log",
                 "jungle_log", "acacia_log", "cherry_log", "mangrove_log",
                 "stripped_oak_log", "stripped_birch_log", "stripped_spruce_log",
                 "stripped_dark_oak_log", "stripped_jungle_log", "stripped_acacia_log",
                 "stripped_cherry_log", "stripped_mangrove_log",
                 "oak_planks", "birch_planks", "spruce_planks", "dark_oak_planks",
                 "jungle_planks", "acacia_planks", "cherry_planks", "mangrove_planks",
                 "bamboo_block", "mangrove_roots", "muddy_mangrove_roots" -> handleWoodToolBreak(player, world, pos, state, toolType);

            // --- COPPER FAMILY ---
            case "copper_ingot", "chiseled_copper", "cut_copper",
                 "waxed_cut_copper", "exposed_copper", "weathered_copper",
                 "oxidised_copper" -> handleCopperToolBreak(player, world, pos, state, toolType);

            // --- MISC FAMILY ---
            case "iron_nugget", "gold_nugget", "lapis_lazuli",
                 "amethyst_shard", "amethyst_cluster",
                 "flint", "charcoal", "mud_brick", "mud_bricks",
                 "raw_iron_block", "raw_gold_block",
                 "verdant_froglight", "pearlescent_froglight", "ochre_froglight",
                 "tnt", "soul_soil" -> handleMiscToolBreak(player, world, pos, state, toolType, material);

            // --- NEW 25 MATERIALS ---
            case "raw_prismarine", "clay", "glazed_terracotta", "concrete", "concrete_powder",
                 "bookshelf", "barrel", "smithing_table", "anvil", "grindstone",
                 "lectern", "composter", "stonecutter", "dried_kelp", "coral_block",
                 "lantern", "chain", "lightning_rod", "tinted_glass", "candle",
                 "sculk_sensor", "sculk_catalyst", "sculk_shrieker",
                 "brewing_stand", "furnace" -> handleNew25ToolBreak(player, world, pos, state, toolType, material);
        }
    }

    public static void onEntityHit(ServerPlayer player, Entity target, ItemStack tool) {
        if (!isBlockTool(tool)) return;
        if (!(target instanceof LivingEntity)) return;
        
        String material = getToolMaterial(tool);
        if (material == null) return;

        LivingEntity livingTarget = (LivingEntity) target;
        applyUniqueToolSetOnHit(player, livingTarget, tool, material);
        applyPerMaterialToolIdentityOnHit(player, livingTarget, tool, material);
        if (!legacyToolHandlersEnabled()) return;

        switch (material) {
            case "ice" -> {
                livingTarget.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));
                player.level().sendParticles(ParticleTypes.SNOWFLAKE, 
                    target.getX(), target.getY() + 1, target.getZ(), 4, 0.5, 0.5, 0.5, 0.1);
            }
            case "glass" -> {
                if (player.getRandom().nextFloat() < 0.20f) {
                    livingTarget.hurtServer(player.level(), player.damageSources().playerAttack(player), 3.0f);
                    ((ServerLevel) player.level()).sendParticles(ParticleTypes.CRIT,
                        target.getX(), target.getY() + 1, target.getZ(), 4, 0.4, 0.5, 0.4, 0.15);
                    player.sendSystemMessage(Component.literal("✦ Shatter!").withStyle(ChatFormatting.AQUA), true);
                }
            }
            case "obsidian" -> {
                livingTarget.knockback(0.8, player.getX() - target.getX(), player.getZ() - target.getZ());
                livingTarget.hurtServer(player.level(), player.damageSources().playerAttack(player), 2.0f);
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.CRIT,
                    target.getX(), target.getY() + 1, target.getZ(), 3, 0.3, 0.5, 0.3, 0.1);
            }
            case "quartz" -> {
                UUID uid = player.getUUID();
                int hits = quartzHitCounter.getOrDefault(uid, 0) + 1;
                if (hits >= 3) {
                    livingTarget.hurtServer(player.level(), player.damageSources().playerAttack(player), 6.0f);
                    ((ServerLevel) player.level()).sendParticles(ParticleTypes.ENCHANT,
                        target.getX(), target.getY() + 1.5, target.getZ(), 5, 0.4, 0.5, 0.4, 0.2);
                    player.sendSystemMessage(Component.literal("✦ Critical Strike!").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD), true);
                    hits = 0;
                }
                quartzHitCounter.put(uid, hits);
            }
            case "terracotta" -> {
                livingTarget.knockback(1.0, player.getX() - target.getX(), player.getZ() - target.getZ());
                if (player.onGround()) {
                    livingTarget.hurtServer(player.level(), player.damageSources().playerAttack(player), 2.5f);
                    player.sendSystemMessage(Component.literal("✦ Grounded Strike!").withStyle(ChatFormatting.GOLD), true);
                }
            }
            case "mossy" -> {
                livingTarget.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0, true, true, true));
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                    target.getX(), target.getY() + 1, target.getZ(), 4, 0.4, 0.5, 0.4, 0.05);
            }
            case "sandstone" -> {
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 0, true, false, false));
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.DUST_PLUME,
                    player.getX(), player.getY() + 0.5, player.getZ(), 3, 0.3, 0.2, 0.3, 0.05);
            }
            case "warped" -> {
                livingTarget.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 40, 0, true, true, true));
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.WARPED_SPORE,
                    target.getX(), target.getY() + 1, target.getZ(), 5, 0.5, 0.5, 0.5, 0.1);
            }
            case "magma" -> {
                livingTarget.setRemainingFireTicks(80);
                if (player.level().dimension() == Level.NETHER) {
                    livingTarget.hurtServer(player.level(), player.damageSources().playerAttack(player), 3.0f);
                    player.sendSystemMessage(Component.literal("✦ Nether Blaze!").withStyle(ChatFormatting.DARK_RED), true);
                }
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.FLAME,
                    target.getX(), target.getY() + 1, target.getZ(), 4, 0.5, 0.5, 0.5, 0.1);
            }
            case "cactus" -> {
                livingTarget.hurtServer(player.level(), player.damageSources().cactus(), 2.0f);
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.CRIT,
                    target.getX(), target.getY() + 1, target.getZ(), 3, 0.3, 0.4, 0.3, 0.1);
                player.sendSystemMessage(Component.literal("✦ Thorns!").withStyle(ChatFormatting.GREEN), true);
            }
            case "tnt" -> {
                if (player.getRandom().nextFloat() < 0.05f) {
                    livingTarget.hurtServer(player.level(), player.damageSources().explosion(null, player), 3.0f);
                    ((ServerLevel) player.level()).sendParticles(ParticleTypes.EXPLOSION,
                        target.getX(), target.getY() + 1, target.getZ(), 3, 0.3, 0.3, 0.3, 0.0);
                    player.playSound(SoundEvents.GENERIC_EXPLODE.value(), 0.6f, 1.2f);
                    player.sendSystemMessage(Component.literal("💥 Explosive Hit!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                }
            }
            case "diamond_block" -> {
                livingTarget.hurtServer(player.level(), player.damageSources().playerAttack(player), 3.0f);
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.ENCHANT,
                    target.getX(), target.getY() + 1, target.getZ(), 4, 0.4, 0.5, 0.4, 0.2);
            }
            case "iron_block" -> {
                livingTarget.hurtServer(player.level(), player.damageSources().playerAttack(player), 2.0f);
                livingTarget.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, true, false, true));
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.CRIT,
                    target.getX(), target.getY() + 1, target.getZ(), 3, 0.3, 0.4, 0.3, 0.1);
            }
            case "gold_block" -> {
                if (player.getRandom().nextFloat() < 0.10f) {
                    int coins = 1 + player.getRandom().nextInt(3);
                    CoinManager.giveCoinsQuiet(player, coins);
                    player.sendSystemMessage(Component.literal("✦ +" + coins + " coins!").withStyle(ChatFormatting.GOLD), true);
                }
            }
            case "copper_block" -> {
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 0, true, false, false));
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    player.getX(), player.getY() + 0.5, player.getZ(), 3, 0.3, 0.3, 0.3, 0.1);
            }
            case "glowstone" -> {
                if (isUndead(livingTarget)) {
                    livingTarget.hurtServer(player.level(), player.damageSources().playerAttack(player), 3.5f);
                    player.level().sendParticles(ParticleTypes.END_ROD,
                        target.getX(), target.getY() + 1, target.getZ(), 5, 0.5, 0.5, 0.5, 0.1);
                }
            }
            case "redstone" -> {
                livingTarget.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 2));
                player.level().sendParticles(ParticleTypes.CRIT,
                    target.getX(), target.getY() + 1, target.getZ(), 4, 0.5, 0.5, 0.5, 0.1);
            }
            case "netherrack" -> {
                livingTarget.setRemainingFireTicks(60);
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.FLAME,
                    target.getX(), target.getY() + 1, target.getZ(), 4, 0.5, 0.5, 0.5, 0.1);
            }
            case "endstone" -> {
                if (isEnderMob(livingTarget)) {
                    livingTarget.hurtServer(player.level(), player.damageSources().playerAttack(player), 5.0f);
                }
            }
            case "prismarine" -> {
                if (isOceanMob(livingTarget)) {
                    livingTarget.hurtServer(player.level(), player.damageSources().playerAttack(player), 5.0f);
                }
            }
            case "soul_sand" -> {
                livingTarget.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));
                livingTarget.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 0));
            }
            case "coal" -> {
                livingTarget.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
            }
            case "emerald" -> {
                if (player.getRandom().nextFloat() < 0.22f) {
                    CoinManager.giveCoinsQuiet(player, 1 + player.getRandom().nextInt(4));
                }
                livingTarget.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 50, 0, true, false, false));
            }
            case "lapis" -> {
                livingTarget.hurtServer(player.level(), player.damageSources().magic(), 4.0f);
                livingTarget.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 55, 0, true, false, false));
            }
            case "amethyst" -> {
                if (player.getRandom().nextFloat() < 0.28f) {
                    livingTarget.hurtServer(player.level(), player.damageSources().sonicBoom(player), 6.0f);
                    player.playSound(SoundEvents.WARDEN_SONIC_BOOM, 1.0f, 1.0f);
                }
            }

            // --- STONE FAMILY ---
            case "basalt", "smooth_basalt", "polished_basalt",
                 "granite", "polished_granite",
                 "diorite", "polished_diorite",
                 "andesite", "polished_andesite",
                 "cobblestone",
                 "stone_bricks", "chiseled_stone_brick", "cracked_stone_brick",
                 "tuff", "polished_tuff",
                 "calcite", "brick", "dripstone", "smooth_stone" -> {
                livingTarget.hurtServer(player.level(), player.damageSources().playerAttack(player), 1.5f);
                livingTarget.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 0, true, false, true));
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.CRIT,
                    target.getX(), target.getY() + 1, target.getZ(), 3, 0.3, 0.4, 0.3, 0.1);
            }

            // --- DEEPSLATE FAMILY ---
            case "deepslate", "cobbled_deepslate", "polished_deepslate",
                 "chiseled_deepslate", "deepslate_tile", "deepslate_brick",
                 "reinforced_deepslate" -> {
                float deepBonus = player.getY() < 0 ? 2.0f : 0.5f;
                livingTarget.hurtServer(player.level(), player.damageSources().playerAttack(player), deepBonus);
                livingTarget.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 30, 0, true, false, false));
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.SCULK_SOUL,
                    target.getX(), target.getY() + 1, target.getZ(), 3, 0.4, 0.5, 0.4, 0.05);
            }

            // --- BLACKSTONE FAMILY ---
            case "blackstone", "polished_blackstone",
                 "blackstone_bricks", "gilded_blackstone" -> {
                float bsBonus = player.level().dimension() == Level.NETHER ? 2.0f : 0.5f;
                livingTarget.hurtServer(player.level(), player.damageSources().playerAttack(player), bsBonus);
                livingTarget.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 25, 0, true, false, false));
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.SMOKE,
                    target.getX(), target.getY() + 1, target.getZ(), 4, 0.4, 0.5, 0.4, 0.05);
            }

            // --- NETHER FAMILY ---
            case "nether_brick", "chiseled_nether_brick", "cracked_nether_brick",
                 "crimson_stem", "nether_gold_ore", "nether_wart_block",
                 "warped_wart_block", "netherite_block",
                 "crying_obsidian", "shroomlight" -> {
                livingTarget.setRemainingFireTicks(60);
                if (player.level().dimension() == Level.NETHER) {
                    livingTarget.hurtServer(player.level(), player.damageSources().playerAttack(player), 1.5f);
                    player.sendSystemMessage(Component.literal("✦ Nether Fury!").withStyle(ChatFormatting.DARK_RED), true);
                }
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.FLAME,
                    target.getX(), target.getY() + 1, target.getZ(), 4, 0.5, 0.5, 0.5, 0.1);
            }

            // --- ICE FAMILY ---
            case "packed_ice", "blue_ice",
                 "snow", "snow_block" -> {
                livingTarget.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 1, true, false, true));
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.SNOWFLAKE,
                    target.getX(), target.getY() + 1, target.getZ(), 5, 0.5, 0.5, 0.5, 0.1);
            }

            // --- OCEAN FAMILY ---
            case "prismarine_bricks", "dark_prismarine",
                 "sea_lantern", "lodestone" -> {
                livingTarget.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 40, 0, true, false, true));
                if (player.isUnderWater()) {
                    livingTarget.hurtServer(player.level(), player.damageSources().playerAttack(player), 2.0f);
                    player.sendSystemMessage(Component.literal("✦ Tidal Strike!").withStyle(ChatFormatting.DARK_AQUA), true);
                }
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.BUBBLE,
                    target.getX(), target.getY() + 1, target.getZ(), 4, 0.4, 0.5, 0.4, 0.1);
            }

            // --- END FAMILY ---
            case "end_stone", "end_stone_brick",
                 "purpur" -> {
                if (player.getRandom().nextFloat() < 0.05f) {
                    livingTarget.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 15, 0, true, false, true));
                    ((ServerLevel) player.level()).sendParticles(ParticleTypes.PORTAL,
                        target.getX(), target.getY() + 1, target.getZ(), 5, 0.5, 0.5, 0.5, 0.2);
                    player.sendSystemMessage(Component.literal("✦ Void Launch!").withStyle(ChatFormatting.DARK_PURPLE), true);
                }
            }

            // --- NATURE FAMILY ---
            case "bone_block", "bone_meal",
                 "honeycomb", "honeycomb_block",
                 "lily_pad", "moss_block", "mycelium",
                 "target", "sponge", "sand", "red_sandstone" -> {
                livingTarget.addEffect(new MobEffectInstance(MobEffects.POISON, 25, 0, true, true, true));
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                    target.getX(), target.getY() + 1, target.getZ(), 4, 0.4, 0.5, 0.4, 0.05);
            }
            case "hay", "hay_block" -> {
                livingTarget.knockback(1.2, player.getX() - target.getX(), player.getZ() - target.getZ());
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.DUST_PLUME,
                    target.getX(), target.getY() + 1, target.getZ(), 3, 0.3, 0.4, 0.3, 0.05);
                player.sendSystemMessage(Component.literal("✦ Bale Bash!").withStyle(ChatFormatting.GOLD), true);
            }
            case "melon" -> {
                livingTarget.addEffect(new MobEffectInstance(MobEffects.POISON, 25, 0, true, true, true));
                if (player.getRandom().nextFloat() < 0.15f) {
                    Block.popResource(player.level(), target.blockPosition(), new ItemStack(Items.MELON_SLICE, 1));
                }
            }
            case "pumpkin" -> {
                livingTarget.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0, true, false, false));
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.SMOKE,
                    target.getX(), target.getY() + 1, target.getZ(), 3, 0.3, 0.4, 0.3, 0.05);
                player.sendSystemMessage(Component.literal("✦ Pumpkin Smash!").withStyle(ChatFormatting.GOLD), true);
            }

            // --- WOOD FAMILY ---
            case "oak_log", "birch_log", "spruce_log", "dark_oak_log",
                 "jungle_log", "acacia_log", "cherry_log", "mangrove_log",
                 "stripped_oak_log", "stripped_birch_log", "stripped_spruce_log",
                 "stripped_dark_oak_log", "stripped_jungle_log", "stripped_acacia_log",
                 "stripped_cherry_log", "stripped_mangrove_log",
                 "oak_planks", "birch_planks", "spruce_planks", "dark_oak_planks",
                 "jungle_planks", "acacia_planks", "cherry_planks", "mangrove_planks",
                 "bamboo_block", "mangrove_roots", "muddy_mangrove_roots" -> {
                livingTarget.addEffect(new MobEffectInstance(MobEffects.POISON, 15, 0, true, false, true));
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.CRIT,
                    target.getX(), target.getY() + 1, target.getZ(), 3, 0.3, 0.4, 0.3, 0.1);
            }

            // --- COPPER FAMILY ---
            case "copper_ingot", "chiseled_copper", "cut_copper",
                 "waxed_cut_copper", "exposed_copper", "weathered_copper",
                 "oxidised_copper" -> {
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 0, true, false, false));
                livingTarget.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0, true, false, true));
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    target.getX(), target.getY() + 1, target.getZ(), 3, 0.4, 0.5, 0.4, 0.1);
            }

            // --- MISC FAMILY ---
            case "iron_nugget" -> {
                livingTarget.hurtServer(player.level(), player.damageSources().playerAttack(player), 0.5f);
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.CRIT,
                    target.getX(), target.getY() + 1, target.getZ(), 4, 0.2, 0.3, 0.2, 0.1);
            }
            case "gold_nugget" -> {
                if (player.getRandom().nextFloat() < 0.08f) {
                    CoinManager.giveCoinsQuiet(player, 1);
                    player.sendSystemMessage(Component.literal("✦ +1 coin!").withStyle(ChatFormatting.GOLD), true);
                }
            }
            case "lapis_lazuli" -> {
                livingTarget.hurtServer(player.level(), player.damageSources().magic(), 2.0f);
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.ENCHANT,
                    target.getX(), target.getY() + 1.5, target.getZ(), 4, 0.4, 0.5, 0.4, 0.15);
            }
            case "amethyst_shard" -> {
                if (player.getRandom().nextFloat() < 0.20f) {
                    livingTarget.hurtServer(player.level(), player.damageSources().sonicBoom(player), 3.0f);
                    player.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 0.8f, 1.2f);
                }
            }
            case "amethyst_cluster" -> {
                if (player.getRandom().nextFloat() < 0.15f) {
                    livingTarget.hurtServer(player.level(), player.damageSources().sonicBoom(player), 4.5f);
                    player.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.0f, 0.8f);
                    ((ServerLevel) player.level()).sendParticles(ParticleTypes.SONIC_BOOM,
                        target.getX(), target.getY() + 1, target.getZ(), 1, 0, 0, 0, 0);
                }
            }
            case "flint" -> {
                livingTarget.hurtServer(player.level(), player.damageSources().playerAttack(player), 1.0f);
                if (player.getRandom().nextFloat() < 0.10f) {
                    livingTarget.setRemainingFireTicks(40);
                    player.sendSystemMessage(Component.literal("✦ Spark!").withStyle(ChatFormatting.RED), true);
                }
            }
            case "charcoal" -> {
                livingTarget.setRemainingFireTicks(40);
                livingTarget.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20, 0, true, false, false));
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.SMOKE,
                    target.getX(), target.getY() + 1, target.getZ(), 3, 0.3, 0.4, 0.3, 0.05);
            }
            case "mud_brick", "mud_bricks" -> {
                livingTarget.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 0, true, false, true));
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.DUST_PLUME,
                    target.getX(), target.getY() + 0.5, target.getZ(), 3, 0.3, 0.2, 0.3, 0.05);
            }
            case "raw_iron_block" -> {
                livingTarget.hurtServer(player.level(), player.damageSources().playerAttack(player), 2.0f);
                livingTarget.knockback(0.6, player.getX() - target.getX(), player.getZ() - target.getZ());
            }
            case "raw_gold_block" -> {
                livingTarget.hurtServer(player.level(), player.damageSources().playerAttack(player), 1.5f);
                if (player.getRandom().nextFloat() < 0.10f) {
                    CoinManager.giveCoinsQuiet(player, 1 + player.getRandom().nextInt(2));
                    player.sendSystemMessage(Component.literal("✦ Gold strike!").withStyle(ChatFormatting.GOLD), true);
                }
            }
            case "verdant_froglight" -> {
                livingTarget.addEffect(new MobEffectInstance(MobEffects.POISON, 30, 0, true, true, true));
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    target.getX(), target.getY() + 1, target.getZ(), 3, 0.4, 0.5, 0.4, 0.05);
            }
            case "pearlescent_froglight" -> {
                livingTarget.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 10, 0, true, false, true));
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.END_ROD,
                    target.getX(), target.getY() + 1, target.getZ(), 3, 0.4, 0.5, 0.4, 0.05);
            }
            case "ochre_froglight" -> {
                livingTarget.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, true, false, true));
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.GLOW,
                    target.getX(), target.getY() + 1, target.getZ(), 3, 0.4, 0.5, 0.4, 0.05);
            }
            case "soul_soil" -> {
                livingTarget.addEffect(new MobEffectInstance(MobEffects.WITHER, 30, 0, true, true, false));
                livingTarget.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 0, true, false, true));
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.SOUL,
                    target.getX(), target.getY() + 1, target.getZ(), 3, 0.4, 0.5, 0.4, 0.05);
            }

            // --- NEW 25 MATERIALS ---
            case "raw_prismarine", "coral_block", "dried_kelp" -> {
                livingTarget.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 40, 0, true, false, true));
                if (player.isUnderWater()) {
                    livingTarget.hurtServer(player.level(), player.damageSources().playerAttack(player), 2.0f);
                    player.sendSystemMessage(Component.literal("✦ Tidal Strike!").withStyle(ChatFormatting.DARK_AQUA), true);
                }
            }
            case "clay", "concrete_powder" -> {
                livingTarget.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1, true, false, true));
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.DUST_PLUME,
                    target.getX(), target.getY() + 1, target.getZ(), 8, 0.4, 0.4, 0.4, 0.05);
            }
            case "glazed_terracotta", "concrete" -> {
                livingTarget.knockback(0.8, player.getX() - target.getX(), player.getZ() - target.getZ());
                livingTarget.hurtServer(player.level(), player.damageSources().playerAttack(player), 1.5f);
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.CRIT,
                    target.getX(), target.getY() + 1, target.getZ(), 8, 0.3, 0.4, 0.3, 0.1);
            }
            case "bookshelf", "lectern" -> {
                if (player.getRandom().nextFloat() < 0.15f) {
                    livingTarget.hurtServer(player.level(), player.damageSources().magic(), 3.0f);
                    ((ServerLevel) player.level()).sendParticles(ParticleTypes.ENCHANT,
                        target.getX(), target.getY() + 1.5, target.getZ(), 12, 0.4, 0.6, 0.4, 0.15);
                    player.sendSystemMessage(Component.literal("✦ Arcane Pulse!").withStyle(ChatFormatting.DARK_PURPLE), true);
                }
            }
            case "barrel", "composter" -> {
                livingTarget.addEffect(new MobEffectInstance(MobEffects.POISON, 25, 0, true, true, true));
                player.heal(0.5f);
            }
            case "smithing_table", "grindstone", "stonecutter" -> {
                livingTarget.hurtServer(player.level(), player.damageSources().playerAttack(player), 2.0f);
                livingTarget.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, true, false, true));
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.CRIT,
                    target.getX(), target.getY() + 1, target.getZ(), 8, 0.3, 0.4, 0.3, 0.1);
            }
            case "anvil" -> {
                livingTarget.hurtServer(player.level(), player.damageSources().playerAttack(player), 3.0f);
                livingTarget.knockback(1.2, player.getX() - target.getX(), player.getZ() - target.getZ());
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.CRIT,
                    target.getX(), target.getY() + 1, target.getZ(), 12, 0.4, 0.5, 0.4, 0.1);
                player.sendSystemMessage(Component.literal("✦ Crushing Blow!").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD), true);
            }
            case "lantern", "candle" -> {
                livingTarget.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, true, false, true));
                livingTarget.setRemainingFireTicks(40);
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.FLAME,
                    target.getX(), target.getY() + 1, target.getZ(), 6, 0.3, 0.4, 0.3, 0.05);
            }
            case "chain" -> {
                livingTarget.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 1, true, false, true));
                livingTarget.hurtServer(player.level(), player.damageSources().playerAttack(player), 1.5f);
                player.sendSystemMessage(Component.literal("✦ Shackled!").withStyle(ChatFormatting.DARK_GRAY), true);
            }
            case "lightning_rod" -> {
                if (player.getRandom().nextFloat() < 0.1f) {
                    livingTarget.hurtServer(player.level(), player.damageSources().magic(), 5.0f);
                    ((ServerLevel) player.level()).sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        target.getX(), target.getY() + 1, target.getZ(), 16, 0.5, 0.6, 0.5, 0.15);
                    player.sendSystemMessage(Component.literal("✦ Lightning Strike!").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD), true);
                }
            }
            case "tinted_glass" -> {
                livingTarget.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 30, 0, true, false, false));
                if (player.getRandom().nextFloat() < 0.2f) {
                    livingTarget.hurtServer(player.level(), player.damageSources().playerAttack(player), 3.0f);
                    player.sendSystemMessage(Component.literal("✦ Shadow Shard!").withStyle(ChatFormatting.DARK_GRAY), true);
                }
            }
            case "sculk_sensor", "sculk_catalyst", "sculk_shrieker" -> {
                livingTarget.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, 0, true, false, false));
                if (player.getRandom().nextFloat() < 0.1f) {
                    livingTarget.hurtServer(player.level(), player.damageSources().sonicBoom(player), 4.0f);
                    ((ServerLevel) player.level()).sendParticles(ParticleTypes.SONIC_BOOM,
                        target.getX(), target.getY() + 1, target.getZ(), 1, 0, 0, 0, 0);
                }
            }
            case "brewing_stand" -> {
                float roll = player.getRandom().nextFloat();
                if (roll < 0.33f) livingTarget.addEffect(new MobEffectInstance(MobEffects.POISON, 40, 0, true, false, false));
                else if (roll < 0.66f) livingTarget.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, true, false, false));
                else livingTarget.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1, true, false, false));
            }
            case "furnace" -> {
                livingTarget.setRemainingFireTicks(80);
                livingTarget.hurtServer(player.level(), player.damageSources().playerAttack(player), 1.5f);
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.FLAME,
                    target.getX(), target.getY() + 1, target.getZ(), 10, 0.5, 0.5, 0.5, 0.1);
            }
        }
    }

    // ============================================================
    // MATERIAL-SPECIFIC BREAK HANDLERS
    // ============================================================

    private static void handleGlassToolBreak(ServerPlayer player, Level world, BlockPos pos, BlockState state, String toolType) {
        if (player.getRandom().nextFloat() < 0.28f) {
            Block.dropResources(state, world, pos, null, player, player.getMainHandItem());
            player.sendSystemMessage(Component.literal("✦ Double drops!").withStyle(ChatFormatting.AQUA), true);
        }

        if (player.getRandom().nextFloat() < 0.10f) {
            ItemStack bonus = switch (player.getRandom().nextInt(3)) {
                case 0 -> new ItemStack(Items.RAW_IRON, 1);
                case 1 -> new ItemStack(Items.RAW_GOLD, 1);
                default -> new ItemStack(Items.DIAMOND, 1);
            };
            Block.popResource(world, pos, bonus);
            player.sendSystemMessage(Component.literal("✦ Mineral find!").withStyle(ChatFormatting.AQUA), true);
        }

        if ("shovel".equals(toolType) && player.isShiftKeyDown()) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    BlockPos adjPos = pos.offset(dx, 0, dz);
                    BlockState adjState = world.getBlockState(adjPos);
                    if (!adjState.isAir() && adjState.getBlock() != Blocks.BEDROCK && isShovelEffective(adjState)) {
                        world.destroyBlock(adjPos, true, player, Block.UPDATE_ALL);
                    }
                }
            }
            player.sendSystemMessage(Component.literal("⛏ 3x3 Excavation!").withStyle(ChatFormatting.AQUA), true);
        }

        if ("hoe".equals(toolType)) {
            ((ServerLevel) world).sendParticles(ParticleTypes.END_ROD,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.3, 0.3, 0.3, 0.05);
        }
    }
    
    private static boolean isShovelEffective(BlockState state) {
        Block block = state.getBlock();
        return block == Blocks.DIRT || block == Blocks.GRASS_BLOCK || block == Blocks.SAND ||
               block == Blocks.GRAVEL || block == Blocks.SOUL_SAND || block == Blocks.SOUL_SOIL ||
               block == Blocks.CLAY || block == Blocks.COARSE_DIRT || block == Blocks.ROOTED_DIRT;
    }

    private static void handleObsidianToolBreak(ServerPlayer player, Level world, BlockPos pos, BlockState state, String toolType) {
        if ("pickaxe".equals(toolType) && state.getBlock() == Blocks.BEDROCK) {
            player.sendSystemMessage(Component.literal("⛏ Breaking bedrock...").withStyle(ChatFormatting.DARK_PURPLE), true);
            if (player.getRandom().nextFloat() < 0.1f) {
                if (world instanceof ServerLevel serverWorld) {
                    serverWorld.destroyBlock(pos, true, player, Block.UPDATE_ALL);
                    serverWorld.sendParticles(ParticleTypes.EXPLOSION,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            10, 0.5, 0.5, 0.5, 0.1);
                    player.sendSystemMessage(Component.literal("💥 Bedrock shattered!").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), true);
                    return;
                }
            }
        }

        if ("pickaxe".equals(toolType) && player.getRandom().nextFloat() < 0.38f) {
            if (isOre(state.getBlock())) {
                Block.dropResources(state, world, pos, null, player, player.getMainHandItem());
                player.sendSystemMessage(Component.literal("✦ Fortune bonus!").withStyle(ChatFormatting.DARK_PURPLE), true);
            }
        }

        autoSmeltDrop(world, pos, state, player);

        if ("axe".equals(toolType)) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    BlockPos adjPos = pos.offset(dx, 0, dz);
                    BlockState adjState = world.getBlockState(adjPos);
                    if (adjState.getBlock() instanceof RotatedPillarBlock && 
                        adjState.is(net.minecraft.tags.BlockTags.LOGS)) {
                        world.setBlock(adjPos, adjState.getBlock() == Blocks.OAK_LOG ? Blocks.STRIPPED_OAK_LOG.defaultBlockState() :
                            adjState.getBlock() == Blocks.SPRUCE_LOG ? Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState() :
                            adjState.getBlock() == Blocks.BIRCH_LOG ? Blocks.STRIPPED_BIRCH_LOG.defaultBlockState() :
                            adjState.getBlock() == Blocks.JUNGLE_LOG ? Blocks.STRIPPED_JUNGLE_LOG.defaultBlockState() :
                            adjState.getBlock() == Blocks.DARK_OAK_LOG ? Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState() :
                            adjState.getBlock() == Blocks.ACACIA_LOG ? Blocks.STRIPPED_ACACIA_LOG.defaultBlockState() :
                            adjState.getBlock() == Blocks.CRIMSON_STEM ? Blocks.STRIPPED_CRIMSON_STEM.defaultBlockState() :
                            adjState.getBlock() == Blocks.WARPED_STEM ? Blocks.STRIPPED_WARPED_STEM.defaultBlockState() :
                            adjState, Block.UPDATE_ALL);
                    }
                }
            }
        }

        if ("shovel".equals(toolType) && player.isShiftKeyDown()) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    BlockPos adjPos = pos.offset(dx, 0, dz);
                    BlockState adjState = world.getBlockState(adjPos);
                    if (!adjState.isAir() && adjState.getBlock() != Blocks.BEDROCK && isShovelEffective(adjState)) {
                        world.destroyBlock(adjPos, true, player, Block.UPDATE_ALL);
                    }
                }
            }
            player.sendSystemMessage(Component.literal("⛏ 5x5 Excavation!").withStyle(ChatFormatting.DARK_PURPLE), true);
        }

        if ("hoe".equals(toolType)) {
            player.addEffect(new MobEffectInstance(MobEffects.LUCK, 100, 0));
        }
    }

    private static void handleQuartzToolBreak(ServerPlayer player, Level world, BlockPos pos, BlockState state, String toolType) {
        if ("pickaxe".equals(toolType)) {
            ((ServerLevel) world).sendParticles(ParticleTypes.END_ROD,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.3, 0.3, 0.3, 0.05);
        }
        if (world instanceof ServerLevel sw) {
            int orbCount = 1 + player.getRandom().nextInt(2);
            for (int i = 0; i < orbCount; i++) {
                sw.addFreshEntity(new net.minecraft.world.entity.ExperienceOrb(sw,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    1 + player.getRandom().nextInt(3)));
            }
        }
    }

    private static void handleGlowstoneToolBreak(ServerPlayer player, Level world, BlockPos pos, BlockState state, String toolType) {
        if (state.getBlock() == Blocks.GLOWSTONE && player.getRandom().nextFloat() < 0.3f) {
            Block.popResource(world, pos, new ItemStack(Items.GLOWSTONE_DUST, 2));
            player.sendSystemMessage(Component.literal("✦ Bonus glowstone!").withStyle(ChatFormatting.YELLOW), true);
        }
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 200, 0, true, false, true));
    }

    private static void handleRedstoneToolBreak(ServerPlayer player, Level world, BlockPos pos, BlockState state, String toolType) {
        if ("pickaxe".equals(toolType) && isOre(state.getBlock())) {
            ((ServerLevel) world).sendParticles(ParticleTypes.CRIT,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.3, 0.3, 0.3, 0.1);
        }
        player.addEffect(new MobEffectInstance(MobEffects.HASTE, 100, 1, true, false, true));
    }

    private static void handleNetherrackToolBreak(ServerPlayer player, Level world, BlockPos pos, BlockState state, String toolType) {
        if (world.dimension() == Level.NETHER) {
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100, 0, true, false, true));
            player.sendSystemMessage(Component.literal("⛏ Nether mining speed!").withStyle(ChatFormatting.DARK_RED), true);
        }
    }

    private static void handleEndstoneToolBreak(ServerPlayer player, Level world, BlockPos pos, BlockState state, String toolType) {
        if ("pickaxe".equals(toolType) || "shovel".equals(toolType)) {
            ((ServerLevel) world).sendParticles(ParticleTypes.PORTAL,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 15, 0.5, 0.5, 0.5, 0.1);
        }
        if (player.getRandom().nextFloat() < 0.05f) {
            var look = player.getLookAngle();
            double tx = player.getX() + look.x * 5;
            double ty = player.getY();
            double tz = player.getZ() + look.z * 5;
            BlockPos dest = BlockPos.containing(tx, ty, tz);
            if (world.getBlockState(dest).isAir() && world.getBlockState(dest.above()).isAir()) {
                player.teleportTo(tx, ty, tz);
                ((ServerLevel) world).sendParticles(ParticleTypes.REVERSE_PORTAL,
                    tx, ty + 1, tz, 20, 0.5, 0.5, 0.5, 0.1);
                player.sendSystemMessage(Component.literal("✦ Void Shift!").withStyle(ChatFormatting.DARK_PURPLE), true);
            }
        }
    }

    private static void handleIceToolBreak(ServerPlayer player, Level world, BlockPos pos, BlockState state, String toolType) {
        // Ice tools: Freeze nearby water/lava
        if ("pickaxe".equals(toolType)) {
            for (Direction dir : Direction.values()) {
                BlockPos adjacent = pos.relative(dir);
                BlockState adjState = world.getBlockState(adjacent);
                if (adjState.getBlock() == Blocks.WATER) {
                    world.setBlock(adjacent, Blocks.ICE.defaultBlockState(), Block.UPDATE_ALL);
                } else if (adjState.getBlock() == Blocks.LAVA) {
                    world.setBlock(adjacent, Blocks.OBSIDIAN.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
    }

    private static void handlePrismarineToolBreak(ServerPlayer player, Level world, BlockPos pos, BlockState state, String toolType) {
        if (player.isUnderWater()) {
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 200, 0, true, false, true));
            player.sendSystemMessage(Component.literal("🌊 Underwater mining bonus!").withStyle(ChatFormatting.DARK_AQUA), true);
        }
    }

    private static void handleTerracottaToolBreak(ServerPlayer player, Level world, BlockPos pos, BlockState state, String toolType) {
        // Terracotta tools: Faster on terracotta/clay
        if (state.getBlock() == Blocks.TERRACOTTA || state.getBlock() == Blocks.CLAY) {
            player.sendSystemMessage(Component.literal("⛏ Clay mining speed!").withStyle(ChatFormatting.GOLD), true);
        }
    }

    private static void handleMossyToolBreak(ServerPlayer player, Level world, BlockPos pos, BlockState state, String toolType) {
        // Mossy tools: Spread moss to nearby stone
        if ("pickaxe".equals(toolType)) {
            for (Direction dir : Direction.values()) {
                BlockPos adjacent = pos.relative(dir);
                BlockState adjState = world.getBlockState(adjacent);
                if (adjState.getBlock() == Blocks.STONE && player.getRandom().nextFloat() < 0.1f) {
                    world.setBlock(adjacent, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
    }

    private static void handleSoulSandToolBreak(ServerPlayer player, Level world, BlockPos pos, BlockState state, String toolType) {
        if (state.getBlock() == Blocks.SOUL_SAND || state.getBlock() == Blocks.SOUL_SOIL) {
            player.sendSystemMessage(Component.literal("⛏ Soul speed!").withStyle(ChatFormatting.DARK_GRAY), true);
        }
        if (world instanceof ServerLevel sw) {
            var nearby = sw.getEntitiesOfClass(LivingEntity.class,
                new net.minecraft.world.phys.AABB(pos).inflate(3.0),
                e -> e != player && e.isAlive());
            for (LivingEntity mob : nearby) {
                mob.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 0, true, true, false));
            }
            if (!nearby.isEmpty()) {
                sw.sendParticles(ParticleTypes.SOUL,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 15, 1.5, 0.5, 1.5, 0.05);
                player.sendSystemMessage(Component.literal("✦ Soul Wither!").withStyle(ChatFormatting.DARK_GRAY), true);
            }
        }
    }

    private static void handleMagmaToolBreak(ServerPlayer player, Level world, BlockPos pos, BlockState state, String toolType) {
        // Magma tools: Fire resistance in Nether
        if (world.dimension() == Level.NETHER) {
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100, 0));
        }
    }

    private static void handleSandstoneToolBreak(ServerPlayer player, Level world, BlockPos pos, BlockState state, String toolType) {
        // Sandstone tools: Faster on sand
        if (state.getBlock() == Blocks.SAND || state.getBlock() == Blocks.SANDSTONE) {
            player.sendSystemMessage(Component.literal("⛏ Desert speed!").withStyle(ChatFormatting.YELLOW), true);
        }
    }

    private static void handleAmethystToolBreak(ServerPlayer player, Level world, BlockPos pos, BlockState state, String toolType) {
        if (state.getBlock() == Blocks.AMETHYST_CLUSTER && player.getRandom().nextFloat() < 0.25f) {
            Block.popResource(world, pos, new ItemStack(Items.AMETHYST_SHARD, 2));
            player.sendSystemMessage(Component.literal("✦ Bonus amethyst!").withStyle(ChatFormatting.LIGHT_PURPLE), true);
        }
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, false, true));
    }

    private static void handleCoalToolBreak(ServerPlayer player, Level world, BlockPos pos, BlockState state, String toolType) {
        if ((state.getBlock() == Blocks.COAL_ORE || state.getBlock() == Blocks.DEEPSLATE_COAL_ORE) 
            && player.getRandom().nextFloat() < 0.5f) {
            Block.popResource(world, pos, new ItemStack(Items.COAL, 1));
            player.sendSystemMessage(Component.literal("✦ Bonus coal!").withStyle(ChatFormatting.DARK_GRAY), true);
        }
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 200, 0, true, false, true));
    }

    private static void handleGemEmeraldToolBreak(ServerPlayer player, Level world, BlockPos pos, BlockState state, String toolType) {
        if ("pickaxe".equals(toolType) && isOre(state.getBlock()) && player.getRandom().nextFloat() < 0.35f) {
            CoinManager.giveCoinsQuiet(player, 2 + player.getRandom().nextInt(6));
            player.sendSystemMessage(Component.literal("✦ Prospector strike!").withStyle(ChatFormatting.GREEN), true);
        }
        if ("hoe".equals(toolType) && state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state)) {
            player.addEffect(new MobEffectInstance(MobEffects.LUCK, 200, 1, true, false, false));
        }
        if (player.getRandom().nextFloat() < 0.05f) {
            CoinManager.giveCoinsQuiet(player, 1);
            player.sendSystemMessage(Component.literal("✦ Coin find!").withStyle(ChatFormatting.GREEN), true);
        }
    }

    private static void handleGemLapisToolBreak(ServerPlayer player, Level world, BlockPos pos, BlockState state, String toolType) {
        if ("pickaxe".equals(toolType) && isOre(state.getBlock()) && player.getRandom().nextFloat() < 0.4f) {
            if (world instanceof ServerLevel sw) {
                sw.addFreshEntity(new net.minecraft.world.entity.ExperienceOrb(sw, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4 + player.getRandom().nextInt(5)));
            }
            player.sendSystemMessage(Component.literal("✦ Bonus vein!").withStyle(ChatFormatting.BLUE), true);
        }
        if (isOre(state.getBlock()) && world instanceof ServerLevel sw) {
            int bonusXp = 1 + player.getRandom().nextInt(2);
            sw.addFreshEntity(new net.minecraft.world.entity.ExperienceOrb(sw,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, bonusXp));
        }
    }

    private static void handleSculkToolBreak(ServerPlayer player, Level world, BlockPos pos, BlockState state, String toolType) {
        if (world instanceof ServerLevel sw) {
            var nearby = sw.getEntitiesOfClass(LivingEntity.class,
                new net.minecraft.world.phys.AABB(pos).inflate(4.0),
                e -> e != player && e.isAlive());
            for (LivingEntity mob : nearby) {
                mob.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, 0, true, false, false));
            }
            if (!nearby.isEmpty()) {
                sw.sendParticles(ParticleTypes.SCULK_CHARGE_POP,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 20, 2.0, 0.5, 2.0, 0.02);
                player.sendSystemMessage(Component.literal("✦ Sculk Pulse!").withStyle(ChatFormatting.DARK_AQUA), true);
            }
        }
    }

    // ============================================================
    // STONE FAMILY BREAK HANDLER
    // ============================================================

    private static void handleStoneToolBreak(ServerPlayer player, Level world, BlockPos pos, BlockState state, String toolType) {
        player.addEffect(new MobEffectInstance(MobEffects.HASTE, 60, 0, true, false, false));
        if (world instanceof ServerLevel sw) {
            sw.sendParticles(ParticleTypes.CRIT,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, 0.3, 0.3, 0.3, 0.05);
        }
    }

    // ============================================================
    // DEEPSLATE FAMILY BREAK HANDLER
    // ============================================================

    private static void handleDeepslateToolBreak(ServerPlayer player, Level world, BlockPos pos, BlockState state, String toolType) {
        if (pos.getY() < 0) {
            player.addEffect(new MobEffectInstance(MobEffects.HASTE, 60, 1, true, false, false));
        } else {
            player.addEffect(new MobEffectInstance(MobEffects.HASTE, 60, 0, true, false, false));
        }
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 100, 0, true, false, true));
        if (world instanceof ServerLevel sw) {
            sw.sendParticles(ParticleTypes.SCULK_SOUL,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.3, 0.3, 0.3, 0.03);
        }
    }

    // ============================================================
    // BLACKSTONE FAMILY BREAK HANDLER
    // ============================================================

    private static void handleBlackstoneToolBreak(ServerPlayer player, Level world, BlockPos pos, BlockState state, String toolType) {
        if (world.dimension() == Level.NETHER) {
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100, 0, true, false, true));
            player.sendSystemMessage(Component.literal("✦ Nether Ward!").withStyle(ChatFormatting.DARK_GRAY), true);
        }
        if (world instanceof ServerLevel sw) {
            sw.sendParticles(ParticleTypes.SMOKE,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.02);
        }
    }

    // ============================================================
    // NETHER FAMILY BREAK HANDLER
    // ============================================================

    private static void handleNetherToolBreak(ServerPlayer player, Level world, BlockPos pos, BlockState state, String toolType, String material) {
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100, 0, true, false, true));
        if ("nether_gold_ore".equals(material) && player.getRandom().nextFloat() < 0.05f) {
            CoinManager.giveCoinsQuiet(player, 1);
            player.sendSystemMessage(Component.literal("✦ Nether coin!").withStyle(ChatFormatting.GOLD), true);
        }
        if (world instanceof ServerLevel sw) {
            sw.sendParticles(ParticleTypes.FLAME,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.05);
        }
    }

    // ============================================================
    // ICE FAMILY BREAK HANDLER
    // ============================================================

    private static void handleIceFamilyToolBreak(ServerPlayer player, Level world, BlockPos pos, BlockState state, String toolType) {
        player.addEffect(new MobEffectInstance(MobEffects.SPEED, 100, 0, true, false, false));
        if (world instanceof ServerLevel sw) {
            sw.sendParticles(ParticleTypes.SNOWFLAKE,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.5, 0.5, 0.5, 0.05);
        }
    }

    // ============================================================
    // OCEAN FAMILY BREAK HANDLER
    // ============================================================

    private static void handleOceanToolBreak(ServerPlayer player, Level world, BlockPos pos, BlockState state, String toolType) {
        if (player.isUnderWater()) {
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 200, 0, true, false, true));
            player.sendSystemMessage(Component.literal("🌊 Ocean's Blessing!").withStyle(ChatFormatting.DARK_AQUA), true);
        }
        if (world instanceof ServerLevel sw) {
            sw.sendParticles(ParticleTypes.BUBBLE,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 8, 0.4, 0.4, 0.4, 0.05);
        }
    }

    // ============================================================
    // END FAMILY BREAK HANDLER
    // ============================================================

    private static void handleEndFamilyToolBreak(ServerPlayer player, Level world, BlockPos pos, BlockState state, String toolType) {
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0, true, false, true));
        if (player.getRandom().nextFloat() < 0.03f) {
            var look = player.getLookAngle();
            double tx = player.getX() + look.x * 3;
            double ty = player.getY();
            double tz = player.getZ() + look.z * 3;
            BlockPos dest = BlockPos.containing(tx, ty, tz);
            if (world.getBlockState(dest).isAir() && world.getBlockState(dest.above()).isAir()) {
                player.teleportTo(tx, ty, tz);
                if (world instanceof ServerLevel sw) {
                    sw.sendParticles(ParticleTypes.REVERSE_PORTAL,
                        tx, ty + 1, tz, 15, 0.5, 0.5, 0.5, 0.1);
                }
                player.sendSystemMessage(Component.literal("✦ Phase Shift!").withStyle(ChatFormatting.DARK_PURPLE), true);
            }
        }
        if (world instanceof ServerLevel sw) {
            sw.sendParticles(ParticleTypes.PORTAL,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 8, 0.4, 0.4, 0.4, 0.1);
        }
    }

    // ============================================================
    // NATURE FAMILY BREAK HANDLER
    // ============================================================

    private static void handleNatureToolBreak(ServerPlayer player, Level world, BlockPos pos, BlockState state, String toolType) {
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, false, true));
        if (world instanceof ServerLevel sw) {
            sw.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.05);
        }
    }

    // ============================================================
    // WOOD FAMILY BREAK HANDLER
    // ============================================================

    private static void handleWoodToolBreak(ServerPlayer player, Level world, BlockPos pos, BlockState state, String toolType) {
        player.addEffect(new MobEffectInstance(MobEffects.SPEED, 60, 0, true, false, false));
        if (world instanceof ServerLevel sw) {
            sw.sendParticles(ParticleTypes.CRIT,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, 0.3, 0.3, 0.3, 0.05);
        }
    }

    // ============================================================
    // COPPER FAMILY BREAK HANDLER
    // ============================================================

    private static void handleCopperToolBreak(ServerPlayer player, Level world, BlockPos pos, BlockState state, String toolType) {
        player.addEffect(new MobEffectInstance(MobEffects.HASTE, 60, 0, true, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 0, true, false, false));
        if (world instanceof ServerLevel sw) {
            sw.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.1);
        }
    }

    // ============================================================
    // MISC FAMILY BREAK HANDLER
    // ============================================================

    private static void handleMiscToolBreak(ServerPlayer player, Level world, BlockPos pos, BlockState state, String toolType, String material) {
        switch (material) {
            case "iron_nugget" -> {
                player.addEffect(new MobEffectInstance(MobEffects.HASTE, 40, 0, true, false, false));
            }
            case "gold_nugget" -> {
                if (player.getRandom().nextFloat() < 0.05f) {
                    CoinManager.giveCoinsQuiet(player, 1);
                    player.sendSystemMessage(Component.literal("✦ Nugget find!").withStyle(ChatFormatting.GOLD), true);
                }
            }
            case "lapis_lazuli" -> {
                if (world instanceof ServerLevel sw && isOre(state.getBlock())) {
                    int bonusXp = 1 + player.getRandom().nextInt(3);
                    sw.addFreshEntity(new net.minecraft.world.entity.ExperienceOrb(sw,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, bonusXp));
                }
            }
            case "amethyst_shard", "amethyst_cluster" -> {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, true, false, true));
                if (world instanceof ServerLevel sw) {
                    sw.sendParticles(ParticleTypes.END_ROD,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.3, 0.3, 0.3, 0.05);
                }
            }
            case "flint" -> {
                player.addEffect(new MobEffectInstance(MobEffects.HASTE, 40, 0, true, false, false));
                if (world instanceof ServerLevel sw) {
                    sw.sendParticles(ParticleTypes.CRIT,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, 0.2, 0.2, 0.2, 0.05);
                }
            }
            case "charcoal" -> {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 100, 0, true, false, true));
                if (world instanceof ServerLevel sw) {
                    sw.sendParticles(ParticleTypes.SMOKE,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.03);
                }
            }
            case "mud_brick", "mud_bricks" -> {
                player.addEffect(new MobEffectInstance(MobEffects.HASTE, 40, 0, true, false, false));
            }
            case "raw_iron_block" -> {
                player.addEffect(new MobEffectInstance(MobEffects.HASTE, 60, 0, true, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 40, 0, true, false, false));
            }
            case "raw_gold_block" -> {
                player.addEffect(new MobEffectInstance(MobEffects.LUCK, 100, 0, true, false, false));
                if (player.getRandom().nextFloat() < 0.05f) {
                    CoinManager.giveCoinsQuiet(player, 1);
                    player.sendSystemMessage(Component.literal("✦ Gold find!").withStyle(ChatFormatting.GOLD), true);
                }
            }
            case "verdant_froglight", "pearlescent_froglight", "ochre_froglight" -> {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 100, 0, true, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 0, true, false, false));
                if (world instanceof ServerLevel sw) {
                    sw.sendParticles(ParticleTypes.GLOW,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.05);
                }
            }
            case "tnt" -> {
                if (player.getRandom().nextFloat() < 0.03f) {
                    if (world instanceof ServerLevel sw) {
                        sw.sendParticles(ParticleTypes.EXPLOSION,
                            pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, 2, 0.3, 0.3, 0.3, 0.0);
                        var nearby = sw.getEntitiesOfClass(LivingEntity.class,
                            new net.minecraft.world.phys.AABB(pos).inflate(3.0),
                            e -> e != player && e.isAlive());
                        for (LivingEntity mob : nearby) {
                            mob.hurtServer(sw, player.damageSources().explosion(null, player), 4.0f);
                        }
                        player.sendSystemMessage(Component.literal("💥 Mining Blast!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                    }
                }
            }
            case "soul_soil" -> {
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 60, 0, true, false, false));
                if (world instanceof ServerLevel sw) {
                    sw.sendParticles(ParticleTypes.SOUL,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.03);
                }
            }
        }
    }

    // ============================================================
    // NEW 25 MATERIALS BREAK HANDLER
    // ============================================================

    private static void handleNew25ToolBreak(ServerPlayer player, Level world, BlockPos pos, BlockState state, String toolType, String material) {
        switch (material) {
            case "raw_prismarine", "coral_block", "dried_kelp" -> {
                if (player.isUnderWater()) {
                    player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 200, 0, true, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.HASTE, 60, 0, true, false, false));
                }
                if (world instanceof ServerLevel sw)
                    sw.sendParticles(ParticleTypes.BUBBLE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 8, 0.4, 0.4, 0.4, 0.05);
            }
            case "clay", "concrete_powder" -> {
                player.addEffect(new MobEffectInstance(MobEffects.HASTE, 60, 0, true, false, false));
                if (world instanceof ServerLevel sw)
                    sw.sendParticles(ParticleTypes.DUST_PLUME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.05);
            }
            case "glazed_terracotta", "concrete" -> {
                player.addEffect(new MobEffectInstance(MobEffects.HASTE, 60, 0, true, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 40, 0, true, false, false));
                if (world instanceof ServerLevel sw)
                    sw.sendParticles(ParticleTypes.CRIT, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, 0.3, 0.3, 0.3, 0.05);
            }
            case "bookshelf", "lectern" -> {
                player.addEffect(new MobEffectInstance(MobEffects.LUCK, 100, 0, true, false, false));
                if (world instanceof ServerLevel sw) {
                    int bonusXp = 1 + player.getRandom().nextInt(3);
                    sw.addFreshEntity(new net.minecraft.world.entity.ExperienceOrb(sw,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, bonusXp));
                    sw.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 8, 0.3, 0.3, 0.3, 0.1);
                }
            }
            case "barrel", "composter" -> {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, true, false, true));
                if (player.getRandom().nextFloat() < 0.05f) {
                    CoinManager.giveCoinsQuiet(player, 1);
                    player.sendSystemMessage(Component.literal("✦ Loot find!").withStyle(ChatFormatting.GOLD), true);
                }
                if (world instanceof ServerLevel sw)
                    sw.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.05);
            }
            case "smithing_table", "grindstone", "stonecutter" -> {
                player.addEffect(new MobEffectInstance(MobEffects.HASTE, 80, 1, true, false, false));
                if ("pickaxe".equals(toolType) && isOre(state.getBlock()) && player.getRandom().nextFloat() < 0.25f) {
                    Block.dropResources(state, world, pos, null, player, player.getMainHandItem());
                    player.sendSystemMessage(Component.literal("✦ Refined ore!").withStyle(ChatFormatting.GRAY), true);
                }
                if (world instanceof ServerLevel sw)
                    sw.sendParticles(ParticleTypes.CRIT, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, 0.3, 0.3, 0.3, 0.05);
            }
            case "anvil" -> {
                player.addEffect(new MobEffectInstance(MobEffects.HASTE, 60, 1, true, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 40, 0, true, false, false));
                if (world instanceof ServerLevel sw)
                    sw.sendParticles(ParticleTypes.CRIT, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.05);
            }
            case "lantern", "candle" -> {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 200, 0, true, false, true));
                if (world instanceof ServerLevel sw)
                    sw.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.03);
            }
            case "chain" -> {
                player.addEffect(new MobEffectInstance(MobEffects.HASTE, 60, 0, true, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 40, 0, true, false, false));
                if (world instanceof ServerLevel sw)
                    sw.sendParticles(ParticleTypes.CRIT, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, 0.3, 0.3, 0.3, 0.05);
            }
            case "lightning_rod" -> {
                player.addEffect(new MobEffectInstance(MobEffects.HASTE, 60, 0, true, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 0, true, false, false));
                if (world instanceof ServerLevel sw)
                    sw.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.1);
            }
            case "tinted_glass" -> {
                if (player.getRandom().nextFloat() < 0.25f) {
                    Block.dropResources(state, world, pos, null, player, player.getMainHandItem());
                    player.sendSystemMessage(Component.literal("✦ Double drops!").withStyle(ChatFormatting.DARK_GRAY), true);
                }
                if (world instanceof ServerLevel sw)
                    sw.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.03);
            }
            case "sculk_sensor", "sculk_catalyst", "sculk_shrieker" -> {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 100, 0, true, false, true));
                if (world instanceof ServerLevel sw) {
                    var nearby = sw.getEntitiesOfClass(LivingEntity.class,
                        new net.minecraft.world.phys.AABB(pos).inflate(4.0),
                        e -> e != player && e.isAlive());
                    for (LivingEntity mob : nearby)
                        mob.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, 0, true, false, false));
                    if (!nearby.isEmpty())
                        sw.sendParticles(ParticleTypes.SCULK_CHARGE_POP, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 15, 1.5, 0.5, 1.5, 0.02);
                }
            }
            case "brewing_stand" -> {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 0, true, false, false));
                if (world instanceof ServerLevel sw)
                    sw.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 8, 0.3, 0.3, 0.3, 0.05);
            }
            case "furnace" -> {
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100, 0, true, false, true));
                autoSmeltDrop(world, pos, state, player);
                if (world instanceof ServerLevel sw)
                    sw.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.05);
            }
        }
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    private static void autoSmeltDrop(Level world, BlockPos pos, BlockState state, ServerPlayer player) {
        Block block = state.getBlock();
        ItemStack smelted = null;
        if (block == Blocks.COBBLESTONE || block == Blocks.COBBLED_DEEPSLATE) {
            smelted = new ItemStack(Items.STONE, 1);
        } else if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE) {
            smelted = new ItemStack(Items.IRON_INGOT, 1);
        } else if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE) {
            smelted = new ItemStack(Items.GOLD_INGOT, 1);
        } else if (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE) {
            smelted = new ItemStack(Items.COPPER_INGOT, 1);
        } else if (block == Blocks.SAND) {
            smelted = new ItemStack(Items.GLASS, 1);
        } else if (block == Blocks.RAW_IRON_BLOCK) {
            smelted = new ItemStack(Items.IRON_INGOT, 3);
        } else if (block == Blocks.RAW_GOLD_BLOCK) {
            smelted = new ItemStack(Items.GOLD_INGOT, 3);
        } else if (block == Blocks.RAW_COPPER_BLOCK) {
            smelted = new ItemStack(Items.COPPER_INGOT, 3);
        }
        if (smelted != null) {
            Block.popResource(world, pos, smelted);
            if (world instanceof ServerLevel sw) {
                sw.sendParticles(ParticleTypes.FLAME,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.05);
            }
            player.sendSystemMessage(Component.literal("✦ Auto-smelted!").withStyle(ChatFormatting.DARK_PURPLE), true);
        }
    }

    private static boolean isOre(Block block) {
        if (block == Blocks.ANCIENT_DEBRIS) return true;
        var path = BuiltInRegistries.BLOCK.getKey(block).getPath();
        return path.contains("ore") || path.contains("nether_gold_ore") || path.contains("ancient_debris");
    }

    private static boolean isUndead(LivingEntity entity) {
        String name = entity.getType().toString().toLowerCase();
        return name.contains("zombie") || name.contains("skeleton") || 
               name.contains("wither") || name.contains("phantom") ||
               name.contains("drowned") || name.contains("husk") ||
               name.contains("stray");
    }

    private static boolean isEnderMob(LivingEntity entity) {
        String name = entity.getType().toString().toLowerCase();
        return name.contains("enderman") || name.contains("endermite") ||
               name.contains("shulker");
    }

    private static boolean isOceanMob(LivingEntity entity) {
        String name = entity.getType().toString().toLowerCase();
        return name.contains("guardian") || name.contains("drowned") ||
               name.contains("elder_guardian");
    }

    private static void applyUniqueToolSetOnBreak(ServerPlayer player, Level world, BlockPos pos, BlockState state, String toolType, String material) {
        if (!(world instanceof ServerLevel sw)) return;
        int sig = Math.floorMod((material + "|" + toolType + "|break").hashCode(), Integer.MAX_VALUE);
        String theme = resolveToolTheme(material.toLowerCase(java.util.Locale.ROOT));
        int mode = (sig + theme.hashCode()) & 7;
        int radius = 1 + (sig % 2);

        // Shared visual pulse, material-coded cadence.
        if (player.tickCount % (18 + (sig % 23)) == 0) {
            sw.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 4, 0.2, 0.2, 0.2, 0.02);
        }

        switch (mode) {
            case 0 -> {
                // Vein linker
                if (!"pickaxe".equals(toolType) || !isOre(state.getBlock())) return;
                java.util.ArrayDeque<BlockPos> q = new java.util.ArrayDeque<>();
                java.util.HashSet<BlockPos> seen = new java.util.HashSet<>();
                q.add(pos);
                while (!q.isEmpty() && seen.size() < (5 + sig % 5)) {
                    BlockPos cur = q.poll();
                    if (!seen.add(cur)) continue;
                    BlockState cs = world.getBlockState(cur);
                    if (!cs.is(state.getBlock())) continue;
                    if (!cur.equals(pos)) world.destroyBlock(cur, true, player, Block.UPDATE_ALL);
                    for (Direction d : Direction.values()) q.add(cur.relative(d));
                }
            }
            case 1 -> {
                // Cross-cleave mining
                if (!"pickaxe".equals(toolType) && !"shovel".equals(toolType)) return;
                for (Direction d : new Direction[] {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
                    BlockPos p = pos.relative(d);
                    BlockState s = world.getBlockState(p);
                    if (!s.isAir() && s.getDestroySpeed(world, p) >= 0 && s.getDestroySpeed(world, p) <= 10.0f) {
                        world.destroyBlock(p, true, player, Block.UPDATE_ALL);
                    }
                }
            }
            case 2 -> {
                // Magnetized drops
                for (ItemEntity it : sw.getEntitiesOfClass(ItemEntity.class, new net.minecraft.world.phys.AABB(pos).inflate(6.0))) {
                    it.teleportTo(player.getX(), player.getY() + 0.6, player.getZ());
                }
                for (ExperienceOrb orb : sw.getEntitiesOfClass(ExperienceOrb.class, new net.minecraft.world.phys.AABB(pos).inflate(6.0))) {
                    orb.teleportTo(player.getX(), player.getY() + 0.6, player.getZ());
                }
            }
            case 3 -> {
                // Resonant XP pulse
                sw.addFreshEntity(new ExperienceOrb(sw, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 2 + (sig % 5)));
                if (player.getRandom().nextFloat() < 0.30f) Block.popResource(world, pos, new ItemStack(Items.AMETHYST_SHARD));
            }
            case 4 -> {
                // Thermal conversion
                if ("pickaxe".equals(toolType)) autoSmeltDrop(world, pos, state, player);
                if ("hoe".equals(toolType) && state.getBlock() instanceof CropBlock) {
                    player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 2, 0, true, false, false));
                }
            }
            case 5 -> {
                // Shadow pulse
                for (LivingEntity e : sw.getEntitiesOfClass(LivingEntity.class, new net.minecraft.world.phys.AABB(pos).inflate(4.0),
                        ent -> ent != player && ent.isAlive() && ent instanceof net.minecraft.world.entity.monster.Monster)) {
                    e.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 35, 0, true, false, false));
                }
            }
            case 6 -> {
                // Nature mend
                player.heal(0.2f + (sig % 3) * 0.1f);
                if ("axe".equals(toolType)) {
                    for (Direction d : Direction.Plane.HORIZONTAL) {
                        BlockPos p = pos.relative(d);
                        if (world.getBlockState(p).isAir() && player.getRandom().nextFloat() < 0.25f) {
                            Block.popResource(world, p, new ItemStack(Items.OAK_SAPLING));
                        }
                    }
                }
            }
            default -> {
                // Arc shock
                if (player.getRandom().nextFloat() < 0.22f) {
                    for (LivingEntity e : sw.getEntitiesOfClass(LivingEntity.class, new net.minecraft.world.phys.AABB(pos).inflate(3.5),
                            ent -> ent != player && ent.isAlive())) {
                        e.hurtServer(sw, player.damageSources().magic(), 1.5f + radius);
                    }
                    sw.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 10, 0.5, 0.3, 0.5, 0.04);
                }
            }
        }

        // Theme fit layer for all tool sets.
        if ("fire".equals(theme) && player.getRandom().nextFloat() < 0.15f) {
            sw.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 5, 0.3, 0.2, 0.3, 0.02);
        } else if ("water".equals(theme) && player.getRandom().nextFloat() < 0.15f) {
            sw.sendParticles(ParticleTypes.BUBBLE, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 6, 0.3, 0.2, 0.3, 0.02);
        } else if ("shadow".equals(theme) && player.getRandom().nextFloat() < 0.15f) {
            sw.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 6, 0.3, 0.2, 0.3, 0.02);
        }
    }

    private static void applyUniqueToolSetOnHit(ServerPlayer player, LivingEntity target, ItemStack tool, String material) {
        int sig = Math.floorMod((material + "|hit").hashCode(), Integer.MAX_VALUE);
        String theme = resolveToolTheme(material.toLowerCase(java.util.Locale.ROOT));
        int mode = (sig + theme.hashCode()) & 7;
        float burst = 1.0f + (sig % 6) * 0.35f;
        if (player.level() instanceof ServerLevel sw) {
            sw.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1, target.getZ(), 2 + (sig % 4), 0.2, 0.25, 0.2, 0.05);
        }
        switch (mode) {
            case 0 -> target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 1, true, false, true));
            case 1 -> {
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 45, 0, true, false, true));
                target.hurtServer(player.level(), player.damageSources().playerAttack(player), burst);
            }
            case 2 -> {
                target.setRemainingFireTicks(70);
                target.hurtServer(player.level(), player.damageSources().onFire(), burst * 0.6f);
            }
            case 3 -> target.addEffect(new MobEffectInstance(MobEffects.POISON, 50, 0, true, false, true));
            case 4 -> {
                target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, 0, true, false, false));
                target.hurtServer(player.level(), player.damageSources().magic(), burst * 0.7f);
            }
            case 5 -> {
                target.knockback(0.9 + (sig % 3) * 0.25, player.getX() - target.getX(), player.getZ() - target.getZ());
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 35, 0, true, false, false));
            }
            case 6 -> {
                if (player.getRandom().nextFloat() < 0.35f) CoinManager.giveCoinsQuiet(player, 1 + (sig % 2));
                player.heal(0.3f);
            }
            default -> target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 55, 0, true, false, false));
        }
        if (!tool.isEmpty() && player.getRandom().nextFloat() < 0.03f) {
            tool.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        }
        if ("fire".equals(theme)) {
            target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), 40));
        } else if ("water".equals(theme)) {
            target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 25, 0, true, false, true));
        } else if ("earth".equals(theme) || "metal".equals(theme)) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20, 0, true, false, true));
        } else if ("nature".equals(theme)) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 20, 0, true, false, true));
        } else if ("shadow".equals(theme)) {
            target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 20, 0, true, false, false));
        } else if ("light".equals(theme)) {
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 30, 0, true, false, false));
        }
    }

    private static void applyPerMaterialToolIdentityOnBreak(ServerPlayer player, Level world, BlockPos pos, BlockState state, String toolType, String material) {
        int sig = Math.floorMod((material + "|" + toolType).hashCode(), Integer.MAX_VALUE);
        String theme = resolveToolTheme(material.toLowerCase(java.util.Locale.ROOT));
        int cadence = 25 + (sig % 57);
        if (player.tickCount % cadence == 0) {
            if (world instanceof ServerLevel sw) {
                sw.sendParticles(ParticleTypes.ENCHANT,
                        pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5,
                        3 + (sig % 5), 0.25, 0.25, 0.25, 0.03);
            }
        }

        int roll = Math.floorMod(sig, 11);
        float proc = 0.02f + (Math.floorMod(sig, 9) * 0.01f);
        if (player.getRandom().nextFloat() < proc) {
            switch (theme) {
                case "fire" -> {
                    player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 90 + (sig % 35), 0, true, false, false));
                    if (world instanceof ServerLevel sw) sw.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.03);
                }
                case "water" -> {
                    player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 100 + (sig % 40), 0, true, false, false));
                    if (world instanceof ServerLevel sw) sw.sendParticles(ParticleTypes.BUBBLE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 8, 0.4, 0.3, 0.4, 0.03);
                }
                case "earth", "metal" -> {
                    if (isOre(state.getBlock()) && (roll % 2 == 0)) Block.dropResources(state, world, pos, null, player, player.getMainHandItem());
                    player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 45 + (sig % 25), 0, true, false, false));
                }
                case "nature" -> {
                    player.heal(0.15f + (sig % 3) * 0.05f);
                    if (state.getBlock() instanceof CropBlock) player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 1, 0, true, false, false));
                }
                case "shadow" -> {
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 100 + (sig % 60), 0, true, false, true));
                    if (world instanceof ServerLevel sw) sw.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.03);
                }
                case "light" -> {
                    player.addEffect(new MobEffectInstance(MobEffects.LUCK, 90 + (sig % 50), 0, true, false, false));
                    if (world instanceof ServerLevel sw) {
                        sw.addFreshEntity(new net.minecraft.world.entity.ExperienceOrb(sw, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1 + (sig % 3)));
                    }
                }
                default -> {
                    if (roll >= 8) CoinManager.giveCoinsQuiet(player, 1 + (sig % 2));
                    if (world instanceof ServerLevel sw) {
                        sw.sendParticles(ParticleTypes.CRIT, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.3, 0.3, 0.3, 0.03);
                    }
                }
            }
        }
    }

    private static void applyPickaxeMaterialSignature(ServerPlayer player, Level world, BlockPos pos, BlockState state, String toolType, String material) {
        if (!"pickaxe".equals(toolType) || !(world instanceof ServerLevel sw)) return;
        int sig = Math.floorMod(material.hashCode(), Integer.MAX_VALUE);
        float proc = 0.08f + (sig % 6) * 0.01f;
        if (player.getRandom().nextFloat() > proc) return;

        int mode = sig % 5;
        switch (mode) {
            case 0 -> {
                // Seismic cleave: breaks a plus-shaped slice around the mined block.
                for (Direction d : new Direction[] {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
                    BlockPos p = pos.relative(d);
                    BlockState s = world.getBlockState(p);
                    if (!s.isAir() && s.getDestroySpeed(world, p) >= 0 && s.getDestroySpeed(world, p) <= 15.0f) {
                        world.destroyBlock(p, true, player, Block.UPDATE_ALL);
                    }
                }
                sw.sendParticles(ParticleTypes.CRIT, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.6, 0.4, 0.6, 0.01);
                player.sendSystemMessage(Component.literal("✦ Seismic Cleave").withStyle(ChatFormatting.GRAY), true);
            }
            case 1 -> {
                // Vein link: chain-break matching ore blocks.
                if (isOre(state.getBlock())) {
                    java.util.ArrayDeque<BlockPos> q = new java.util.ArrayDeque<>();
                    java.util.HashSet<BlockPos> seen = new java.util.HashSet<>();
                    q.add(pos);
                    while (!q.isEmpty() && seen.size() < 8) {
                        BlockPos cur = q.poll();
                        if (!seen.add(cur)) continue;
                        BlockState curState = world.getBlockState(cur);
                        if (!curState.is(state.getBlock())) continue;
                        if (!cur.equals(pos)) world.destroyBlock(cur, true, player, Block.UPDATE_ALL);
                        for (Direction d : Direction.values()) q.add(cur.relative(d));
                    }
                    sw.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 8, 0.5, 0.4, 0.5, 0.02);
                }
            }
            case 2 -> {
                // Magnet pulse: pull nearby drops and XP to the player.
                for (ItemEntity it : sw.getEntitiesOfClass(ItemEntity.class, new net.minecraft.world.phys.AABB(pos).inflate(6.0))) {
                    it.teleportTo(player.getX(), player.getY() + 0.7, player.getZ());
                }
                for (ExperienceOrb orb : sw.getEntitiesOfClass(ExperienceOrb.class, new net.minecraft.world.phys.AABB(pos).inflate(6.0))) {
                    orb.teleportTo(player.getX(), player.getY() + 0.6, player.getZ());
                }
                sw.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getEyeY(), player.getZ(), 7, 0.25, 0.2, 0.25, 0.02);
            }
            case 3 -> {
                // Resonant shard: bonus XP burst + occasional amethyst shard.
                sw.addFreshEntity(new ExperienceOrb(sw, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3 + (sig % 5)));
                if (player.getRandom().nextFloat() < 0.35f) {
                    Block.popResource(world, pos, new ItemStack(Items.AMETHYST_SHARD, 1));
                }
                sw.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5, 6, 0.3, 0.25, 0.3, 0.02);
            }
            default -> {
                // Thermal tap: extra smelt tick and tiny lava spark ring.
                autoSmeltDrop(world, pos, state, player);
                sw.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 6, 0.35, 0.2, 0.35, 0.02);
            }
        }
    }

    private static void applyPerMaterialToolIdentityOnHit(ServerPlayer player, LivingEntity target, ItemStack tool, String material) {
        int sig = Math.floorMod((material + "|hit").hashCode(), Integer.MAX_VALUE);
        String theme = resolveToolTheme(material.toLowerCase(java.util.Locale.ROOT));
        float extra = 0.6f + ((sig % 10) * 0.15f);
        int cadence = 2 + (sig % 5);
        if ((player.tickCount + target.tickCount + sig) % cadence == 0) {
            target.hurtServer(player.level(), player.damageSources().playerAttack(player), extra);
            if (player.level() instanceof ServerLevel sw) {
                sw.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1, target.getZ(), 2 + (sig % 4), 0.2, 0.3, 0.2, 0.06);
            }
        }
        switch (theme) {
            case "fire" -> target.setRemainingFireTicks(50 + (sig % 50));
            case "water" -> target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30 + (sig % 20), 0, true, false, true));
            case "earth", "metal" -> target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 28 + (sig % 20), 0, true, false, true));
            case "nature" -> target.addEffect(new MobEffectInstance(MobEffects.POISON, 26 + (sig % 20), 0, true, false, true));
            case "shadow" -> target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 24 + (sig % 18), 0, true, false, false));
            case "light" -> target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 25 + (sig % 20), 0, true, false, false));
            default -> target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 25 + (sig % 20), 0, true, false, true));
        }
        if (!tool.isEmpty() && player.getRandom().nextFloat() < (0.01f + (sig % 5) * 0.005f)) {
            tool.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        }
    }

    private static String resolveToolTheme(String m) {
        if (m.contains("glass")) return "glass";
        if (m.contains("redstone")) return "fire";
        if (m.contains("magma") || m.contains("nether") || m.contains("furnace") || m.contains("blaze")) return "fire";
        if (m.contains("prismarine") || m.contains("water") || m.contains("coral") || m.contains("kelp") || m.contains("sponge")) return "water";
        if (m.contains("stone") || m.contains("deepslate") || m.contains("obsidian") || m.contains("clay") || m.contains("concrete") || m.contains("dripstone")) return "earth";
        if (m.contains("iron") || m.contains("gold") || m.contains("copper") || m.contains("anvil") || m.contains("chain") || m.contains("smith") || m.contains("grindstone")) return "metal";
        if (m.contains("moss") || m.contains("wood") || m.contains("log") || m.contains("hay") || m.contains("honey") || m.contains("melon") || m.contains("pumpkin") || m.contains("mycelium")) return "nature";
        if (m.contains("sculk") || m.contains("soul") || m.contains("coal") || m.contains("blackstone") || m.contains("void") || m.contains("dark")) return "shadow";
        if (m.contains("glow") || m.contains("lantern") || m.contains("candle") || m.contains("quartz")) return "light";
        return "air";
    }

    // ============================================================
    // COOLDOWN MANAGEMENT
    // ============================================================

    private static boolean canUseAbility(UUID playerId) {
        Long lastUse = abilityCooldowns.get(playerId);
        if (lastUse == null) return true;
        return System.currentTimeMillis() - lastUse > COOLDOWN_MS;
    }

    private static void setCooldown(UUID playerId) {
        abilityCooldowns.put(playerId, System.currentTimeMillis());
    }
}
