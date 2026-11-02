package com.political.items;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/**
 * Handles Legendary Tool abilities - 15 unique tool sets with special powers
 */
public class LegendaryToolHandler {

    // Tool set identification tags (based on item IDs set in LegendaryTools.java)
    private static final Set<String> DRAGON_TOOLS = Set.of(
        "dragon_pickaxe", "dragon_axe", "dragon_shovel", "dragon_hoe");
    private static final Set<String> AETHER_TOOLS = Set.of(
        "aether_pickaxe", "aether_axe", "aether_shovel", "aether_hoe");
    private static final Set<String> VOID_TOOLS = Set.of(
        "void_pickaxe", "void_axe", "void_shovel", "void_hoe");
    private static final Set<String> NATURE_TOOLS = Set.of(
        "nature_pickaxe", "nature_axe", "nature_shovel", "nature_hoe");
    private static final Set<String> FROST_TOOLS = Set.of(
        "frost_pickaxe", "frost_axe", "frost_shovel", "frost_hoe");
    private static final Set<String> THUNDER_TOOLS = Set.of(
        "thunder_pickaxe", "thunder_axe", "thunder_shovel", "thunder_hoe");
    private static final Set<String> OCEAN_TOOLS = Set.of(
        "ocean_pickaxe", "ocean_axe", "ocean_shovel", "ocean_hoe");
    private static final Set<String> LUNAR_TOOLS = Set.of(
        "lunar_pickaxe", "lunar_axe", "lunar_shovel", "lunar_hoe");
    private static final Set<String> SOLAR_TOOLS = Set.of(
        "solar_pickaxe", "solar_axe", "solar_shovel", "solar_hoe");
    private static final Set<String> TERRA_TOOLS = Set.of(
        "terra_pickaxe", "terra_axe", "terra_shovel", "terra_hoe");
    private static final Set<String> PHANTOM_TOOLS = Set.of(
        "phantom_pickaxe", "phantom_axe", "phantom_shovel", "phantom_hoe");
    private static final Set<String> BLOOD_TOOLS = Set.of(
        "blood_pickaxe", "blood_axe", "blood_shovel", "blood_hoe");
    private static final Set<String> CELESTIAL_TOOLS = Set.of(
        "celestial_pickaxe", "celestial_axe", "celestial_shovel", "celestial_hoe");
    private static final Set<String> SHADOW_TOOLS = Set.of(
        "shadow_pickaxe", "shadow_axe", "shadow_shovel", "shadow_hoe");
    private static final Set<String> CRYSTAL_TOOLS = Set.of(
        "crystal_pickaxe", "crystal_axe", "crystal_shovel", "crystal_hoe");

    // Blocks that count as ores for special abilities
    private static final Set<Block> ORE_BLOCKS = Set.of(
        Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE,
        Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE,
        Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE,
        Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE,
        Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE,
        Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE,
        Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
        Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
        Blocks.NETHER_GOLD_ORE, Blocks.NETHER_QUARTZ_ORE,
        Blocks.ANCIENT_DEBRIS
    );

    // Smelt results for auto-smelting ores
    private static final Map<Block, net.minecraft.world.item.Item> SMELT_RESULTS = Map.of(
        Blocks.IRON_ORE, Items.IRON_INGOT,
        Blocks.DEEPSLATE_IRON_ORE, Items.IRON_INGOT,
        Blocks.GOLD_ORE, Items.GOLD_INGOT,
        Blocks.DEEPSLATE_GOLD_ORE, Items.GOLD_INGOT,
        Blocks.COPPER_ORE, Items.COPPER_INGOT,
        Blocks.DEEPSLATE_COPPER_ORE, Items.COPPER_INGOT,
        Blocks.NETHER_GOLD_ORE, Items.GOLD_INGOT
    );

    // Tree logs for nature/void/axe abilities
    private static final Set<Block> TREE_LOGS = Set.of(
        Blocks.OAK_LOG, Blocks.SPRUCE_LOG, Blocks.BIRCH_LOG, Blocks.JUNGLE_LOG,
        Blocks.ACACIA_LOG, Blocks.DARK_OAK_LOG, Blocks.MANGROVE_LOG, Blocks.CHERRY_LOG,
        Blocks.PALE_OAK_LOG,
        Blocks.STRIPPED_OAK_LOG, Blocks.STRIPPED_SPRUCE_LOG, Blocks.STRIPPED_BIRCH_LOG,
        Blocks.STRIPPED_JUNGLE_LOG, Blocks.STRIPPED_ACACIA_LOG, Blocks.STRIPPED_DARK_OAK_LOG,
        Blocks.STRIPPED_MANGROVE_LOG, Blocks.STRIPPED_CHERRY_LOG, Blocks.STRIPPED_PALE_OAK_LOG
    );

    /**
     * Main handler called when a block is broken with a legendary tool
     */
    public static boolean onBlockBreak(ServerPlayer player, ServerLevel world, BlockPos pos, BlockState state, ItemStack tool) {
        String toolId = getToolId(tool);
        if (toolId == null) return false;

        boolean handled = false;

        // Dragon Tools - Fire/Explosion themed
        if (DRAGON_TOOLS.contains(toolId)) {
            handled |= handleDragonToolBreak(player, world, pos, state, tool, toolId);
        } else if (VOID_TOOLS.contains(toolId)) {
            handled |= handleVoidToolBreak(player, world, pos, state, tool, toolId);
        } else if (NATURE_TOOLS.contains(toolId)) {
            handled |= handleNatureToolBreak(player, world, pos, state, tool, toolId);
        } else if (FROST_TOOLS.contains(toolId)) {
            handled |= handleFrostToolBreak(player, world, pos, state, tool, toolId);
        }

        return handled;
    }

    /**
     * Called when attacking an entity with a legendary tool
     */
    public static boolean onEntityAttack(ServerPlayer player, LivingEntity target, ItemStack tool) {
        String toolId = getToolId(tool);
        if (toolId == null) return false;

        // ── AETHER TOOLS - Sky/Floating themed ──────────────────────
        // Aether Axe - Wind Cut: hurls the target skyward on a gust
        if (toolId.equals("aether_axe")) {
            target.addDeltaMovement(new Vec3(0.0, 1.5, 0.0));
            target.hurtMarked = true;
            if (player.level() instanceof ServerLevel sw) {
                sw.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD,
                        target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                        12, 0.4, 0.3, 0.4, 0.08);
                sw.playSound(null, target.blockPosition(), net.minecraft.sounds.SoundEvents.BREEZE_WIND_CHARGE_BURST.value(),
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.8f, 1.2f);
            }
            return true;
        }
        // Aether Pickaxe - Skyfall: bonus damage scaling with how high you stand
        else if (toolId.equals("aether_pickaxe")) {
            if (player.level() instanceof ServerLevel sw) {
                int height = player.getBlockY();
                float bonus = height > 100 ? Math.min(8.0f, (height - 100) / 25.0f + 2.0f) : 1.0f;
                target.hurtServer(sw, player.damageSources().magic(), bonus);
                sw.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,
                        target.getX(), target.getY() + target.getBbHeight(), target.getZ(),
                        8, 0.3, 0.4, 0.3, 0.05);
                sw.playSound(null, target.blockPosition(), net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME,
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.7f, 1.6f);
            }
            return true;
        }
        // Aether Shovel - Updraft: knocks the target back and grants the wielder cloud footing
        else if (toolId.equals("aether_shovel")) {
            Vec3 push = new Vec3(target.getX() - player.getX(), 0.0, target.getZ() - player.getZ());
            if (push.lengthSqr() > 1.0E-4) {
                push = push.normalize().scale(1.1);
                target.addDeltaMovement(new Vec3(push.x, 0.55, push.z));
                target.hurtMarked = true;
            }
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0, true, false, false));
            if (player.level() instanceof ServerLevel sw) {
                sw.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD,
                        target.getX(), target.getY() + 0.2, target.getZ(),
                        14, 0.5, 0.1, 0.5, 0.06);
                sw.playSound(null, target.blockPosition(), net.minecraft.sounds.SoundEvents.WIND_CHARGE_BURST.value(),
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.8f, 1.0f);
            }
            return true;
        }
        // Aether Hoe - Serene Rebuke: no damage, but roots and pacifies with a calming gale
        else if (toolId.equals("aether_hoe")) {
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 120, 2, false, true, true));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1, false, true, true));
            target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 40, 0, false, true, true));
            if (player.level() instanceof ServerLevel sw) {
                sw.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                        target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(),
                        10, 0.4, 0.5, 0.4, 0.04);
                sw.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD,
                        target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(),
                        6, 0.3, 0.3, 0.3, 0.03);
                sw.playSound(null, target.blockPosition(), net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_RESONATE,
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.7f, 1.4f);
            }
            return true;
        }
        // Frost Axe - Slows enemies
        else if (toolId.equals("frost_axe")) {
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 1));
            return true;
        }
        // Thunder Axe - Summons lightning
        else if (toolId.equals("thunder_axe")) {
            if (target.level() instanceof ServerLevel serverWorld) {
                net.minecraft.world.entity.LightningBolt lightning = new net.minecraft.world.entity.LightningBolt(net.minecraft.world.entity.EntityType.LIGHTNING_BOLT, serverWorld);
                lightning.setPos(target.getX(), target.getY(), target.getZ());
                serverWorld.addFreshEntity(lightning);
            }
            return true;
        }
        // Lunar Axe - Bonus damage to undead
        else if (toolId.equals("lunar_axe") && target instanceof net.minecraft.world.entity.monster.Monster) {
            target.hurtServer((ServerLevel) player.level(), player.damageSources().generic(), 5.0f);
            return true;
        }
        // Solar Axe - Sets targets on fire
        else if (toolId.equals("solar_axe")) {
            if (player.level().isBrightOutside()) {
                target.setRemainingFireTicks(100);
            }
            return true;
        }
        // Terra Axe - Roots enemies (Slowness + Jump Boost negative)
        else if (toolId.equals("terra_axe")) {
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2));
            target.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 60, -128)); // Negative jump
            return true;
        }
        // Phantom Axe - Ignores armor (extra damage)
        else if (toolId.equals("phantom_axe")) {
            target.hurtServer((ServerLevel) player.level(), player.damageSources().magic(), 3.0f);
            return true;
        }
        // Blood Axe - Life drain
        else if (toolId.equals("blood_axe")) {
            float damage = Math.min(4.0f, player.getMaxHealth() - player.getHealth());
            if (damage > 0) {
                player.heal(damage);
            }
            return true;
        }
        // Shadow Axe - Sneak attack bonus
        else if (toolId.equals("shadow_axe") && player.isShiftKeyDown()) {
            target.hurtServer((ServerLevel) player.level(), player.damageSources().generic(), 8.0f);
            return true;
        }

        return false;
    }

    /**
     * Called every tick to apply passive effects
     */
    public static void tickPlayer(ServerPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        String toolId = getToolId(mainHand);
        if (toolId == null) return;

        // Aether Pickaxe - Mining speed increases with height
        if (toolId.equals("aether_pickaxe")) {
            int height = player.getBlockY();
            if (height > 100 && !player.hasEffect(MobEffects.HASTE)) {
                int hasteLevel = Math.min((height - 100) / 20, 2);
                if (hasteLevel > 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.HASTE, 60, hasteLevel - 1, true, false));
                }
            }
        }
        // Void Hoe - Night vision
        else if (toolId.equals("void_hoe")) {
            if (!player.hasEffect(MobEffects.NIGHT_VISION)) {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, true, false));
            }
        }
        // Ocean Pickaxe - Water breathing
        else if (toolId.equals("ocean_pickaxe")) {
            if (!player.hasEffect(MobEffects.WATER_BREATHING)) {
                player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 400, 0, true, false));
            }
        }
        // Phantom Pickaxe - Temporary invisibility when mining
        else if (toolId.equals("phantom_pickaxe") && player.tickCount % 100 == 0) {
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0, true, false));
        }
        // Shadow Pickaxe - Blindness immunity (cleanse blindness)
        else if (toolId.equals("shadow_pickaxe") && player.hasEffect(MobEffects.BLINDNESS)) {
            player.removeEffect(MobEffects.BLINDNESS);
        }
    }

    /**
     * Get tool ID from item stack
     */
    private static String getToolId(ItemStack stack) {
        if (stack.isEmpty()) return null;
        CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.of(new CompoundTag())).copyTag();
        if (nbt.contains("custom_id")) {
            return nbt.getString("custom_id").orElse(null);
        }
        return null;
    }

    // ============================================================
    // DRAGON TOOLS - Fire/Explosion themed
    // ============================================================
    private static boolean handleDragonToolBreak(ServerPlayer player, ServerLevel world, BlockPos pos, BlockState state, ItemStack tool, String toolId) {
        Block block = state.getBlock();

        // Dragon Pickaxe - 3x3 mining + auto-smelt ores
        if (toolId.equals("dragon_pickaxe")) {
            // Break 3x3 area
            break3x3Area(player, world, pos, tool);
            // Auto-smelt ores
            if (SMELT_RESULTS.containsKey(block)) {
                world.destroyBlock(pos, false, null, 512);
                net.minecraft.world.item.Item result = SMELT_RESULTS.get(block);
                Block.popResource(world, pos, new ItemStack(result));
                world.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3, 0.3, 0.3, 0.3, 0.05);
                return true;
            }
        }
        // Dragon Axe - Chop entire trees
        else if (toolId.equals("dragon_axe") && TREE_LOGS.contains(block)) {
            chopTree(player, world, pos, tool);
            return true;
        }
        // Dragon Shovel - 3x3 digging
        else if (toolId.equals("dragon_shovel")) {
            break3x3Area(player, world, pos, tool);
            return true;
        }
        // Dragon Hoe - 5x5 tilling + auto-replant
        else if (toolId.equals("dragon_hoe")) {
            till5x5Area(player, world, pos);
            return true;
        }

        return false;
    }

    // ============================================================
    // AETHER TOOLS - Sky/Floating themed
    // ============================================================
    private static boolean handleAetherToolBreak(ServerPlayer player, ServerLevel world, BlockPos pos, BlockState state, ItemStack tool, String toolId) {
        Block block = state.getBlock();

        // Aether Pickaxe - Skyward mining: 3x3 mining, bonus haste while high up
        if (toolId.equals("aether_pickaxe")) {
            break3x3Area(player, world, pos, tool);
            if (player.getBlockY() > 100 && !player.hasEffect(MobEffects.HASTE)) {
                player.addEffect(new MobEffectInstance(MobEffects.HASTE, 60, 1, true, false));
            }
            world.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3, 0.3, 0.3, 0.3, 0.02);
            return true;
        }
        // Aether Axe - Wind Cut: fell trees and launch the chips skyward
        else if (toolId.equals("aether_axe") && TREE_LOGS.contains(block)) {
            chopTree(player, world, pos, tool);
            world.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 6, 0.4, 0.6, 0.4, 0.05);
            return true;
        }
        // Aether Shovel - Cloud Walker: 3x3 dig + slow-fall footing on the winds
        else if (toolId.equals("aether_shovel")) {
            break3x3Area(player, world, pos, tool);
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0, true, false));
            player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 100, 1, true, false));
            world.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD, player.getX(), player.getY(), player.getZ(), 8, 0.3, 0.1, 0.3, 0.02);
            return true;
        }
        // Aether Hoe - Serene Cultivation: crops mature instantly when worked
        else if (toolId.equals("aether_hoe")) {
            if (isCrop(state)) {
                growCrop(world, pos, state);
                growCrop(world, pos, state);
                growCrop(world, pos, state);
                growCrop(world, pos, state);
                world.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.3, 0.3, 0.3, 0.05);
                return true;
            }
            till3x3Area(player, world, pos, true);
            return true;
        }

        return false;
    }

    // ============================================================
    // VOID TOOLS - Dark/Mystery themed
    // ============================================================
    private static boolean handleVoidToolBreak(ServerPlayer player, ServerLevel world, BlockPos pos, BlockState state, ItemStack tool, String toolId) {
        Block block = state.getBlock();

        // Void Pickaxe - Teleports blocks to inventory
        if (toolId.equals("void_pickaxe")) {
            // Drop items directly to player inventory instead of on ground
            world.destroyBlock(pos, true, player, 512);
            // Clean up drops and give to player
            var drops = Block.getDrops(state, world, pos, null, player, tool);
            for (ItemStack drop : drops) {
                if (!player.addItem(drop)) {
                    Block.popResource(world, pos, drop);
                }
            }
            // Also teleport XP to player
            player.giveExperiencePoints(1);
            return true;
        }
        // Void Axe - Silent tree felling + invisibility
        else if (toolId.equals("void_axe") && TREE_LOGS.contains(block)) {
            chopTree(player, world, pos, tool);
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 100, 0, true, false));
            return true;
        }
        // Void Shovel - 3x3 mining
        else if (toolId.equals("void_shovel")) {
            break3x3Area(player, world, pos, tool);
            return true;
        }

        return false;
    }

    // ============================================================
    // NATURE TOOLS - Growth/Life themed
    // ============================================================
    private static boolean handleNatureToolBreak(ServerPlayer player, ServerLevel world, BlockPos pos, BlockState state, ItemStack tool, String toolId) {
        Block block = state.getBlock();

        // Nature Pickaxe - Plants saplings when mining stone
        if (toolId.equals("nature_pickaxe") && (block == Blocks.STONE || block == Blocks.DEEPSLATE)) {
            world.setBlock(pos, Blocks.OAK_SAPLING.defaultBlockState(), Block.UPDATE_ALL);
            world.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3, 0.3, 0.3, 0.3, 0.05);
            return true;
        }
        // Nature Axe - Auto-replants trees after chopping
        else if (toolId.equals("nature_axe") && TREE_LOGS.contains(block)) {
            BlockPos saplingPos = findSaplingPosition(world, pos);
            if (saplingPos != null) {
                chopTreeAndReplant(player, world, pos, tool, saplingPos);
                return true;
            }
            chopTree(player, world, pos, tool);
            return true;
        }
        // Nature Shovel - Creates grass paths + bone meal effect
        else if (toolId.equals("nature_shovel") && (block == Blocks.GRASS_BLOCK || block == Blocks.DIRT)) {
            world.setBlock(pos, Blocks.DIRT_PATH.defaultBlockState(), Block.UPDATE_ALL);
            // Apply bone meal effect to nearby crops
            applyBoneMealEffect(world, pos);
            return true;
        }
        // Nature Hoe - 3x3 tilling + instant growth
        else if (toolId.equals("nature_hoe")) {
            till3x3Area(player, world, pos, true); // true = instant growth
            return true;
        }

        return false;
    }

    // ============================================================
    // FROST TOOLS - Ice/Cold themed
    // ============================================================
    private static boolean handleFrostToolBreak(ServerPlayer player, ServerLevel world, BlockPos pos, BlockState state, ItemStack tool, String toolId) {
        Block block = state.getBlock();

        // Frost Pickaxe - Freezes water + creates ice blocks
        if (toolId.equals("frost_pickaxe")) {
            // Check adjacent blocks for water and freeze them
            for (Direction dir : Direction.values()) {
                BlockPos adjacent = pos.relative(dir);
                if (world.getBlockState(adjacent).getBlock() == Blocks.WATER) {
                    world.setBlock(adjacent, Blocks.ICE.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
        // Frost Axe - Freezes logs when chopped
        else if (toolId.equals("frost_axe") && TREE_LOGS.contains(block)) {
            // Frozen logs - break normally but spawn ice particles
            chopTree(player, world, pos, tool);
            world.sendParticles(net.minecraft.core.particles.ParticleTypes.SNOWFLAKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.5, 0.5, 0.5, 0.1);
            return true;
        }
        // Frost Shovel - Creates packed ice from snow
        else if (toolId.equals("frost_shovel") && (block == Blocks.SNOW || block == Blocks.SNOW_BLOCK)) {
            world.setBlock(pos, Blocks.PACKED_ICE.defaultBlockState(), Block.UPDATE_ALL);
            return true;
        }

        return false;
    }

    // ============================================================
    // THUNDER TOOLS - Lightning/Electric themed
    // ============================================================
    private static boolean handleThunderToolBreak(ServerPlayer player, ServerLevel world, BlockPos pos, BlockState state, ItemStack tool, String toolId) {
        Block block = state.getBlock();

        // Thunder Pickaxe - Summons lightning on ore veins
        if (toolId.equals("thunder_pickaxe") && ORE_BLOCKS.contains(block)) {
            veinMineWithLightning(player, world, pos, block, tool);
            return true;
        }
        // Thunder Axe - Chain lightning between trees
        else if (toolId.equals("thunder_axe") && TREE_LOGS.contains(block)) {
            chopTreeWithChainLightning(player, world, pos, tool);
            return true;
        }
        // Thunder Shovel - Charges blocks + repels mobs
        else if (toolId.equals("thunder_shovel")) {
            // Repel nearby mobs
            AABB box = new AABB(pos).inflate(8.0);
            for (LivingEntity mob : world.getEntitiesOfClass(LivingEntity.class, box, e -> e instanceof Monster)) {
                Vec3 mobPos = new Vec3(mob.getX(), mob.getY(), mob.getZ());
                Vec3 pushDir = mobPos.subtract(Vec3.atCenterOf(pos)).normalize().scale(2.0);
                mob.addDeltaMovement(new Vec3(pushDir.x, 0.5, pushDir.z));
            }
            world.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.5, 0.5, 0.5, 0.1);
        }

        return false;
    }

    // ============================================================
    // OCEAN TOOLS - Water/Sea themed
    // ============================================================
    private static boolean handleOceanToolBreak(ServerPlayer player, ServerLevel world, BlockPos pos, BlockState state, ItemStack tool, String toolId) {
        Block block = state.getBlock();

        // Ocean Axe - Creates water streams on chop
        if (toolId.equals("ocean_axe")) {
            // Spawn flowing water in adjacent air blocks
            for (Direction dir : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
                BlockPos waterPos = pos.relative(dir);
                if (world.getBlockState(waterPos).isAir()) {
                    world.setBlock(waterPos, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
        // Ocean Shovel - Creates quicksand traps (soul sand effect)
        else if (toolId.equals("ocean_shovel") && (block == Blocks.SAND || block == Blocks.DIRT)) {
            world.setBlock(pos, Blocks.SOUL_SAND.defaultBlockState(), Block.UPDATE_ALL);
            return true;
        }

        return false;
    }

    // ============================================================
    // LUNAR TOOLS - Moon/Night themed
    // ============================================================
    private static boolean handleLunarToolBreak(ServerPlayer player, ServerLevel world, BlockPos pos, BlockState state, ItemStack tool, String toolId) {
        // Lunar Shovel - Grants night vision when digging
        if (toolId.equals("lunar_shovel")) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, true, false));
        }
        // Lunar Hoe - Crops glow and grow at night
        else if (toolId.equals("lunar_hoe") && isCrop(state)) {
            if (!world.isBrightOutside()) {
                growCrop(world, pos, state);
                world.sendParticles(net.minecraft.core.particles.ParticleTypes.GLOW, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, 0.3, 0.3, 0.3, 0.05);
                return true;
            }
        }
        // Lunar Pickaxe - Silverfish detection (highlight nearby silverfish)
        else if (toolId.equals("lunar_pickaxe")) {
            AABB box = new AABB(pos).inflate(10.0);
            for (var silverfish : world.getEntitiesOfClass(net.minecraft.world.entity.monster.Silverfish.class, box, e -> true)) {
                silverfish.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0, true, false));
            }
        }

        return false;
    }

    // ============================================================
    // SOLAR TOOLS - Sun/Day themed
    // ============================================================
    private static boolean handleSolarToolBreak(ServerPlayer player, ServerLevel world, BlockPos pos, BlockState state, ItemStack tool, String toolId) {
        Block block = state.getBlock();

        // Solar Pickaxe - Burns stone into smooth variants
        if (toolId.equals("solar_pickaxe") && world.isBrightOutside()) {
            if (block == Blocks.STONE || block == Blocks.DEEPSLATE) {
                Block smoothVariant = block == Blocks.DEEPSLATE ? Blocks.POLISHED_DEEPSLATE : Blocks.SMOOTH_STONE;
                world.setBlock(pos, smoothVariant.defaultBlockState(), Block.UPDATE_ALL);
                world.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, 0.3, 0.3, 0.3, 0.05);
                return true;
            }
        }
        // Solar Shovel - Faster mining during day
        else if (toolId.equals("solar_shovel") && world.isBrightOutside()) {
            if (!player.hasEffect(MobEffects.HASTE)) {
                player.addEffect(new MobEffectInstance(MobEffects.HASTE, 60, 1, true, false));
            }
        }
        // Solar Hoe - Crops grow 10x faster in sunlight
        else if (toolId.equals("solar_hoe") && isCrop(state) && world.isBrightOutside()) {
            growCrop(world, pos, state);
            growCrop(world, pos, state);
            growCrop(world, pos, state);
            return true;
        }

        return false;
    }

    // ============================================================
    // TERRA TOOLS - Earth/Stone themed
    // ============================================================
    private static boolean handleTerraToolBreak(ServerPlayer player, ServerLevel world, BlockPos pos, BlockState state, ItemStack tool, String toolId) {
        // Terra Pickaxe - 5x5 mining with haste boost
        if (toolId.equals("terra_pickaxe")) {
            break5x5Area(player, world, pos, tool);
            player.addEffect(new MobEffectInstance(MobEffects.HASTE, 60, 0, true, false));
            return true;
        }
        // Terra Shovel - Flattens large areas instantly
        else if (toolId.equals("terra_shovel")) {
            flattenArea(player, world, pos, tool);
            return true;
        }
        // Terra Hoe - Generates bonemeal from tilling
        else if (toolId.equals("terra_hoe")) {
            if (player.addItem(new ItemStack(Items.BONE_MEAL))) {
                world.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 3, 0.3, 0.3, 0.3, 0.05);
            }
            till3x3Area(player, world, pos, false);
            return true;
        }

        return false;
    }

    // ============================================================
    // PHANTOM TOOLS - Ghost/Invisible themed
    // ============================================================
    private static boolean handlePhantomToolBreak(ServerPlayer player, ServerLevel world, BlockPos pos, BlockState state, ItemStack tool, String toolId) {
        // Phantom Hoe - Crops drop XP orbs
        if (toolId.equals("phantom_hoe") && isCrop(state)) {
            world.destroyBlock(pos, true, null, 512);
            // Drop extra XP
            player.giveExperiencePoints(5);
            world.addFreshEntity(new net.minecraft.world.entity.ExperienceOrb(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3));
            return true;
        }
        // Phantom Shovel - Walk through blocks briefly (no clip effect simulated by speed)
        else if (toolId.equals("phantom_shovel")) {
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 2, true, false));
        }

        return false;
    }

    // ============================================================
    // BLOOD TOOLS - Vampire/Life steal themed
    // ============================================================
    private static boolean handleBloodToolBreak(ServerPlayer player, ServerLevel world, BlockPos pos, BlockState state, ItemStack tool, String toolId) {
        Block block = state.getBlock();

        // Blood Pickaxe - Heals when mining ores
        if (toolId.equals("blood_pickaxe") && ORE_BLOCKS.contains(block)) {
            float heal = Math.min(2.0f, player.getMaxHealth() - player.getHealth());
            if (heal > 0) {
                player.heal(heal);
                world.sendParticles(net.minecraft.core.particles.ParticleTypes.HEART, player.getX(), player.getY() + 1, player.getZ(), 3, 0.5, 0.5, 0.5, 0.1);
            }
        }
        // Blood Shovel - Grave digging - finds buried loot
        else if (toolId.equals("blood_shovel")) {
            if (world.getRandom().nextFloat() < 0.1f) {
                // Chance to find buried treasure items
                ItemStack[] possibleLoot = {
                    new ItemStack(Items.BONE, world.getRandom().nextInt(4) + 1),
                    new ItemStack(Items.ROTTEN_FLESH, world.getRandom().nextInt(2) + 1),
                    new ItemStack(Items.GOLD_NUGGET, world.getRandom().nextInt(3) + 1),
                    new ItemStack(Items.EMERALD)
                };
                ItemStack loot = possibleLoot[world.getRandom().nextInt(possibleLoot.length)];
                Block.popResource(world, pos.above(), loot);
                world.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, 0.3, 0.3, 0.3, 0.05);
            }
        }
        // Blood Hoe - Uses health to instantly grow crops
        else if (toolId.equals("blood_hoe") && isCrop(state)) {
            if (player.getHealth() > 2.0f) {
                player.hurtServer(world, player.damageSources().generic(), 2.0f);
                growCrop(world, pos, state);
                growCrop(world, pos, state);
                world.sendParticles(net.minecraft.core.particles.ParticleTypes.DAMAGE_INDICATOR, player.getX(), player.getY() + 1, player.getZ(), 2, 0.5, 0.5, 0.5, 0.1);
                return true;
            }
        }

        return false;
    }

    // ============================================================
    // CELESTIAL TOOLS - Star/Space themed
    // ============================================================
    private static boolean handleCelestialToolBreak(ServerPlayer player, ServerLevel world, BlockPos pos, BlockState state, ItemStack tool, String toolId) {
        Block block = state.getBlock();

        // Celestial Pickaxe - Finds rare ores easier (highlights nearby ores briefly)
        if (toolId.equals("celestial_pickaxe")) {
            AABB box = new AABB(pos).inflate(5.0);
            for (BlockPos orePos : BlockPos.betweenClosed(pos.offset(-5, -5, -5), pos.offset(5, 5, 5))) {
                if (ORE_BLOCKS.contains(world.getBlockState(orePos).getBlock())) {
                    world.sendParticles(net.minecraft.core.particles.ParticleTypes.GLOW, orePos.getX() + 0.5, orePos.getY() + 0.5, orePos.getZ() + 0.5, 5, 0.3, 0.3, 0.3, 0.05);
                }
            }
        }
        // Celestial Shovel - Explosive digging (controlled - breaks 3x3 without drops, then drops center)
        else if (toolId.equals("celestial_shovel")) {
            break3x3NoDrops(world, pos);
            // Give drops for center block only
            world.destroyBlock(pos, true, null, 512);
            return true;
        }
        // Celestial Hoe - Crops sparkle and give bonus drops
        else if (toolId.equals("celestial_hoe") && isCrop(state)) {
            world.destroyBlock(pos, true, null, 512);
            // Bonus drops
            if (world.getRandom().nextFloat() < 0.3f) {
                Block.popResource(world, pos, new ItemStack(Items.GLOWSTONE_DUST, world.getRandom().nextInt(2) + 1));
            }
            world.sendParticles(net.minecraft.core.particles.ParticleTypes.GLOW, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, 0.3, 0.3, 0.3, 0.05);
            return true;
        }

        return false;
    }

    // ============================================================
    // SHADOW TOOLS - Darkness/Stealth themed
    // ============================================================
    private static boolean handleShadowToolBreak(ServerPlayer player, ServerLevel world, BlockPos pos, BlockState state, ItemStack tool, String toolId) {
        // Shadow Shovel - Creates darkness around dig site ( blindness to nearby mobs)
        if (toolId.equals("shadow_shovel")) {
            AABB box = new AABB(pos).inflate(6.0);
            for (LivingEntity mob : world.getEntitiesOfClass(LivingEntity.class, box, e -> e != player)) {
                mob.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            }
            world.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.5, 0.5, 0.5, 0.05);
        }
        // Shadow Hoe - Poisonous crops damage enemies (spawn lingering poison)
        else if (toolId.equals("shadow_hoe")) {
            // Plant poisonous potatoes instead
            if (world.getRandom().nextFloat() < 0.3f) {
                Block.popResource(world, pos.above(), new ItemStack(Items.POISONOUS_POTATO));
            }
        }

        return false;
    }

    // ============================================================
    // CRYSTAL TOOLS - Gem/Shiny themed
    // ============================================================
    private static boolean handleCrystalToolBreak(ServerPlayer player, ServerLevel world, BlockPos pos, BlockState state, ItemStack tool, String toolId) {
        Block block = state.getBlock();

        // Crystal Shovel - Highlights ores through walls briefly
        if (toolId.equals("crystal_shovel")) {
            AABB box = new AABB(pos).inflate(8.0);
            for (BlockPos orePos : BlockPos.betweenClosed(pos.offset(-8, -8, -8), pos.offset(8, 8, 8))) {
                if (ORE_BLOCKS.contains(world.getBlockState(orePos).getBlock())) {
                    world.sendParticles(net.minecraft.core.particles.ParticleTypes.GLOW, orePos.getX() + 0.5, orePos.getY() + 0.5, orePos.getZ() + 0.5, 3, 0.3, 0.3, 0.3, 0.05);
                }
            }
        }
        // Crystal Hoe - Crops have chance to drop diamonds
        else if (toolId.equals("crystal_hoe") && isCrop(state)) {
            world.destroyBlock(pos, true, null, 512);
            if (world.getRandom().nextFloat() < 0.02f) { // 2% chance
                Block.popResource(world, pos, new ItemStack(Items.DIAMOND));
                world.sendParticles(net.minecraft.core.particles.ParticleTypes.GLOW, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.3, 0.3, 0.3, 0.1);
            }
            return true;
        }

        return false;
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    private static void break3x3Area(ServerPlayer player, ServerLevel world, BlockPos center, ItemStack tool) {
        Direction facing = Direction.fromYRot(player.getYRot());
        int dx = Math.abs(facing.getStepX());
        int dz = Math.abs(facing.getStepZ());

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                BlockPos target;
                if (dx == 0) { // Facing Z, break in X-Y plane
                    target = center.offset(x, y, 0);
                } else { // Facing X, break in Z-Y plane
                    target = center.offset(0, y, x);
                }
                if (!target.equals(center)) {
                    BlockState targetState = world.getBlockState(target);
                    if (!targetState.isAir() && targetState.getDestroySpeed(world, target) >= 0.0f) {
                        world.destroyBlock(target, true, null, 512);
                    }
                }
            }
        }
    }

    private static void break5x5Area(ServerPlayer player, ServerLevel world, BlockPos center, ItemStack tool) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos target = center.offset(x, 0, z);
                if (!target.equals(center)) {
                    BlockState targetState = world.getBlockState(target);
                    if (!targetState.isAir() && targetState.getDestroySpeed(world, target) >= 0.0f) {
                        world.destroyBlock(target, true, null, 512);
                    }
                }
            }
        }
    }

    private static void break3x3NoDrops(ServerLevel world, BlockPos center) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos target = center.offset(x, 0, z);
                if (!target.equals(center)) {
                    BlockState targetState = world.getBlockState(target);
                    if (!targetState.isAir() && targetState.getDestroySpeed(world, target) >= 0.0f) {
                        world.destroyBlock(target, false, null, 512);
                    }
                }
            }
        }
    }

    private static void chopTree(ServerPlayer player, ServerLevel world, BlockPos start, ItemStack tool) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(start);
        visited.add(start);

        int count = 0;
        while (!queue.isEmpty() && count < 64) {
            BlockPos current = queue.poll();
            BlockState state = world.getBlockState(current);
            if (TREE_LOGS.contains(state.getBlock())) {
                world.destroyBlock(current, true, null, 512);
                count++;

                // Add neighbors
                for (Direction dir : Direction.values()) {
                    BlockPos neighbor = current.relative(dir);
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }
    }

    private static void chopTreeAndReplant(ServerPlayer player, ServerLevel world, BlockPos start, ItemStack tool, BlockPos saplingPos) {
        chopTree(player, world, start, tool);
        // Plant sapling
        world.setBlock(saplingPos, Blocks.OAK_SAPLING.defaultBlockState(), Block.UPDATE_ALL);
    }

    private static BlockPos findSaplingPosition(ServerLevel world, BlockPos logPos) {
        // Find a dirt/grass block below the tree
        for (int i = 0; i < 5; i++) {
            BlockPos check = logPos.below(i);
            Block block = world.getBlockState(check).getBlock();
            if (block == Blocks.GRASS_BLOCK || block == Blocks.DIRT) {
                return check.above();
            }
        }
        return null;
    }

    private static void till3x3Area(ServerPlayer player, ServerLevel world, BlockPos center, boolean instantGrowth) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos target = center.offset(x, 0, z);
                BlockState state = world.getBlockState(target);
                if (state.getBlock() == Blocks.GRASS_BLOCK || state.getBlock() == Blocks.DIRT) {
                    world.setBlock(target, Blocks.FARMLAND.defaultBlockState(), Block.UPDATE_ALL);
                    if (instantGrowth) {
                        applyBoneMealEffect(world, target.above());
                    }
                }
            }
        }
    }

    private static void till5x5Area(ServerPlayer player, ServerLevel world, BlockPos center) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos target = center.offset(x, 0, z);
                BlockState state = world.getBlockState(target);
                if (state.getBlock() == Blocks.GRASS_BLOCK || state.getBlock() == Blocks.DIRT) {
                    world.setBlock(target, Blocks.FARMLAND.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
    }

    private static void flattenArea(ServerPlayer player, ServerLevel world, BlockPos center, ItemStack tool) {
        int yLevel = center.getY();
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                BlockPos target = center.offset(x, 0, z);
                BlockState state = world.getBlockState(target);
                if (!state.isAir() && state.getDestroySpeed(world, target) >= 0.0f) {
                    if (target.getY() > yLevel) {
                        world.destroyBlock(target, true, null, 512);
                    } else if (target.getY() < yLevel) {
                        world.setBlock(target, Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }
    }

    private static void applyBoneMealEffect(ServerLevel world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof net.minecraft.world.level.block.CropBlock crop) {
            int currentAge = state.getValue(net.minecraft.world.level.block.CropBlock.AGE);
            int maxAge = crop.getMaxAge();
            if (currentAge < maxAge) {
                world.setBlock(pos, state.setValue(net.minecraft.world.level.block.CropBlock.AGE, Math.min(currentAge + 2, maxAge)), Block.UPDATE_ALL);
                world.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3, 0.3, 0.3, 0.3, 0.05);
            }
        }
    }

    private static void veinMineWithLightning(ServerPlayer player, ServerLevel world, BlockPos start, Block oreBlock, ItemStack tool) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(start);
        visited.add(start);

        int count = 0;
        while (!queue.isEmpty() && count < 16) {
            BlockPos current = queue.poll();
            BlockState state = world.getBlockState(current);
            if (state.getBlock() == oreBlock || ORE_BLOCKS.contains(state.getBlock())) {
                // Summon lightning on the player instead of the ore
                if (count == 0) {
                    net.minecraft.world.entity.LightningBolt lightning = new net.minecraft.world.entity.LightningBolt(net.minecraft.world.entity.EntityType.LIGHTNING_BOLT, world);
                    lightning.setPos(player.getX(), player.getY(), player.getZ());
                    world.addFreshEntity(lightning);
                }
                world.destroyBlock(current, true, null, 512);
                count++;

                // Add neighbors
                for (Direction dir : Direction.values()) {
                    BlockPos neighbor = current.relative(dir);
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }
    }

    private static void chopTreeWithChainLightning(ServerPlayer player, ServerLevel world, BlockPos start, ItemStack tool) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(start);
        visited.add(start);

        int count = 0;
        List<BlockPos> logPositions = new ArrayList<>();

        // Find all logs first
        while (!queue.isEmpty() && count < 64) {
            BlockPos current = queue.poll();
            BlockState state = world.getBlockState(current);
            if (TREE_LOGS.contains(state.getBlock())) {
                logPositions.add(current);
                count++;

                for (Direction dir : Direction.values()) {
                    BlockPos neighbor = current.relative(dir);
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }

        // Chain lightning between logs
        for (int i = 0; i < logPositions.size() - 1; i++) {
            BlockPos from = logPositions.get(i);
            BlockPos to = logPositions.get(i + 1);
            world.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                from.getX() + 0.5, from.getY() + 0.5, from.getZ() + 0.5,
                4, (to.getX() - from.getX()) * 0.1, (to.getY() - from.getY()) * 0.1, (to.getZ() - from.getZ()) * 0.1, 0.1);
        }

        // Break all logs
        for (BlockPos pos : logPositions) {
            world.destroyBlock(pos, true, null, 512);
        }
    }

    private static boolean isCrop(BlockState state) {
        return state.getBlock() instanceof net.minecraft.world.level.block.CropBlock ||
               state.getBlock() == Blocks.CARROTS ||
               state.getBlock() == Blocks.POTATOES ||
               state.getBlock() == Blocks.WHEAT ||
               state.getBlock() == Blocks.BEETROOTS ||
               state.getBlock() == Blocks.NETHER_WART;
    }

    private static void growCrop(ServerLevel world, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof net.minecraft.world.level.block.CropBlock crop) {
            int currentAge = state.getValue(net.minecraft.world.level.block.CropBlock.AGE);
            int maxAge = crop.getMaxAge();
            if (currentAge < maxAge) {
                world.setBlock(pos, state.setValue(net.minecraft.world.level.block.CropBlock.AGE, Math.min(currentAge + 1, maxAge)), Block.UPDATE_ALL);
                world.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3, 0.3, 0.3, 0.3, 0.05);
            }
        }
    }
}
