package com.political;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

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
    private static final Map<Block, net.minecraft.item.Item> SMELT_RESULTS = Map.of(
        Blocks.IRON_ORE, Items.IRON_INGOT,
        Blocks.DEEPSLATE_IRON_ORE, Items.IRON_INGOT,
        Blocks.GOLD_ORE, Items.GOLD_INGOT,
        Blocks.DEEPSLATE_GOLD_ORE, Items.GOLD_INGOT,
        Blocks.COPPER_ORE, Items.COPPER_INGOT,
        Blocks.DEEPSLATE_COPPER_ORE, Items.COPPER_INGOT,
        Blocks.NETHER_GOLD_ORE, Items.GOLD_INGOT,
        Blocks.ANCIENT_DEBRIS, Items.NETHERITE_SCRAP
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
    public static boolean onBlockBreak(ServerPlayerEntity player, ServerWorld world, BlockPos pos, BlockState state, ItemStack tool) {
        String toolId = getToolId(tool);
        if (toolId == null) return false;

        boolean handled = false;

        // Dragon Tools - Fire/Explosion themed
        if (DRAGON_TOOLS.contains(toolId)) {
            handled |= handleDragonToolBreak(player, world, pos, state, tool, toolId);
        }
        // Void Tools - Dark/Mystery themed
        else if (VOID_TOOLS.contains(toolId)) {
            handled |= handleVoidToolBreak(player, world, pos, state, tool, toolId);
        }
        // Nature Tools - Growth/Life themed
        else if (NATURE_TOOLS.contains(toolId)) {
            handled |= handleNatureToolBreak(player, world, pos, state, tool, toolId);
        }
        // Frost Tools - Ice/Cold themed
        else if (FROST_TOOLS.contains(toolId)) {
            handled |= handleFrostToolBreak(player, world, pos, state, tool, toolId);
        }
        // Thunder Tools - Lightning/Electric themed
        else if (THUNDER_TOOLS.contains(toolId)) {
            handled |= handleThunderToolBreak(player, world, pos, state, tool, toolId);
        }
        // Ocean Tools - Water/Sea themed
        else if (OCEAN_TOOLS.contains(toolId)) {
            handled |= handleOceanToolBreak(player, world, pos, state, tool, toolId);
        }
        // Lunar Tools - Moon/Night themed
        else if (LUNAR_TOOLS.contains(toolId)) {
            handled |= handleLunarToolBreak(player, world, pos, state, tool, toolId);
        }
        // Solar Tools - Sun/Day themed
        else if (SOLAR_TOOLS.contains(toolId)) {
            handled |= handleSolarToolBreak(player, world, pos, state, tool, toolId);
        }
        // Terra Tools - Earth/Stone themed
        else if (TERRA_TOOLS.contains(toolId)) {
            handled |= handleTerraToolBreak(player, world, pos, state, tool, toolId);
        }
        // Phantom Tools - Ghost/Invisible themed
        else if (PHANTOM_TOOLS.contains(toolId)) {
            handled |= handlePhantomToolBreak(player, world, pos, state, tool, toolId);
        }
        // Blood Tools - Vampire/Life steal themed
        else if (BLOOD_TOOLS.contains(toolId)) {
            handled |= handleBloodToolBreak(player, world, pos, state, tool, toolId);
        }
        // Celestial Tools - Star/Space themed
        else if (CELESTIAL_TOOLS.contains(toolId)) {
            handled |= handleCelestialToolBreak(player, world, pos, state, tool, toolId);
        }
        // Shadow Tools - Darkness/Stealth themed
        else if (SHADOW_TOOLS.contains(toolId)) {
            handled |= handleShadowToolBreak(player, world, pos, state, tool, toolId);
        }
        // Crystal Tools - Gem/Shiny themed
        else if (CRYSTAL_TOOLS.contains(toolId)) {
            handled |= handleCrystalToolBreak(player, world, pos, state, tool, toolId);
        }

        return handled;
    }

    /**
     * Called when attacking an entity with a legendary tool
     */
    public static boolean onEntityAttack(ServerPlayerEntity player, LivingEntity target, ItemStack tool) {
        String toolId = getToolId(tool);
        if (toolId == null) return false;

        // Aether Axe - Throws mobs into the air
        if (toolId.equals("aether_axe")) {
            target.addVelocity(0, 1.5, 0);
            target.velocityDirty = true;
            return true;
        }
        // Frost Axe - Slows enemies
        else if (toolId.equals("frost_axe")) {
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 1));
            return true;
        }
        // Thunder Axe - Summons lightning
        else if (toolId.equals("thunder_axe")) {
            if (target.getEntityWorld() instanceof ServerWorld serverWorld) {
                net.minecraft.entity.LightningEntity lightning = new net.minecraft.entity.LightningEntity(net.minecraft.entity.EntityType.LIGHTNING_BOLT, serverWorld);
                lightning.setPosition(target.getX(), target.getY(), target.getZ());
                serverWorld.spawnEntity(lightning);
            }
            return true;
        }
        // Lunar Axe - Bonus damage to undead
        else if (toolId.equals("lunar_axe") && target instanceof net.minecraft.entity.mob.HostileEntity) {
            target.damage((ServerWorld) player.getEntityWorld(), player.getDamageSources().generic(), 5.0f);
            return true;
        }
        // Solar Axe - Sets targets on fire
        else if (toolId.equals("solar_axe")) {
            if (player.getEntityWorld().isDay()) {
                target.setOnFireFor(5);
            }
            return true;
        }
        // Terra Axe - Roots enemies (Slowness + Jump Boost negative)
        else if (toolId.equals("terra_axe")) {
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 2));
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 60, -128)); // Negative jump
            return true;
        }
        // Phantom Axe - Ignores armor (extra damage)
        else if (toolId.equals("phantom_axe")) {
            target.damage((ServerWorld) player.getEntityWorld(), player.getDamageSources().magic(), 3.0f);
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
        else if (toolId.equals("shadow_axe") && player.isSneaking()) {
            target.damage((ServerWorld) player.getEntityWorld(), player.getDamageSources().generic(), 8.0f);
            return true;
        }

        return false;
    }

    /**
     * Called every tick to apply passive effects
     */
    public static void tickPlayer(ServerPlayerEntity player) {
        ItemStack mainHand = player.getMainHandStack();
        String toolId = getToolId(mainHand);
        if (toolId == null) return;

        // Aether Pickaxe - Mining speed increases with height
        if (toolId.equals("aether_pickaxe")) {
            int height = player.getBlockY();
            if (height > 100 && !player.hasStatusEffect(StatusEffects.HASTE)) {
                int hasteLevel = Math.min((height - 100) / 20, 2);
                if (hasteLevel > 0) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 60, hasteLevel - 1, true, false));
                }
            }
        }
        // Void Hoe - Night vision
        else if (toolId.equals("void_hoe")) {
            if (!player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 400, 0, true, false));
            }
        }
        // Ocean Pickaxe - Water breathing
        else if (toolId.equals("ocean_pickaxe")) {
            if (!player.hasStatusEffect(StatusEffects.WATER_BREATHING)) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 400, 0, true, false));
            }
        }
        // Phantom Pickaxe - Temporary invisibility when mining
        else if (toolId.equals("phantom_pickaxe") && player.age % 100 == 0) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 60, 0, true, false));
        }
        // Shadow Pickaxe - Blindness immunity (cleanse blindness)
        else if (toolId.equals("shadow_pickaxe") && player.hasStatusEffect(StatusEffects.BLINDNESS)) {
            player.removeStatusEffect(StatusEffects.BLINDNESS);
        }
    }

    /**
     * Get tool ID from item stack
     */
    private static String getToolId(ItemStack stack) {
        if (stack.isEmpty()) return null;
        NbtCompound nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound())).copyNbt();
        if (nbt.contains("custom_id")) {
            return nbt.getString("custom_id").orElse(null);
        }
        return null;
    }

    // ============================================================
    // DRAGON TOOLS - Fire/Explosion themed
    // ============================================================
    private static boolean handleDragonToolBreak(ServerPlayerEntity player, ServerWorld world, BlockPos pos, BlockState state, ItemStack tool, String toolId) {
        Block block = state.getBlock();

        // Dragon Pickaxe - 3x3 mining + auto-smelt ores
        if (toolId.equals("dragon_pickaxe")) {
            // Break 3x3 area
            break3x3Area(player, world, pos, tool);
            // Auto-smelt ores
            if (SMELT_RESULTS.containsKey(block)) {
                world.breakBlock(pos, false);
                net.minecraft.item.Item result = SMELT_RESULTS.get(block);
                Block.dropStack(world, pos, new ItemStack(result));
                world.spawnParticles(net.minecraft.particle.ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.3, 0.3, 0.3, 0.05);
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
    // VOID TOOLS - Dark/Mystery themed
    // ============================================================
    private static boolean handleVoidToolBreak(ServerPlayerEntity player, ServerWorld world, BlockPos pos, BlockState state, ItemStack tool, String toolId) {
        Block block = state.getBlock();

        // Void Pickaxe - Teleports blocks to inventory
        if (toolId.equals("void_pickaxe")) {
            // Drop items directly to player inventory instead of on ground
            world.breakBlock(pos, true, player);
            // Clean up drops and give to player
            var drops = Block.getDroppedStacks(state, world, pos, null, player, tool);
            for (ItemStack drop : drops) {
                if (!player.getInventory().insertStack(drop)) {
                    Block.dropStack(world, pos, drop);
                }
            }
            // Also teleport XP to player
            player.addExperience(1);
            return true;
        }
        // Void Axe - Silent tree felling + invisibility
        else if (toolId.equals("void_axe") && TREE_LOGS.contains(block)) {
            chopTree(player, world, pos, tool);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 100, 0, true, false));
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
    private static boolean handleNatureToolBreak(ServerPlayerEntity player, ServerWorld world, BlockPos pos, BlockState state, ItemStack tool, String toolId) {
        Block block = state.getBlock();

        // Nature Pickaxe - Plants saplings when mining stone
        if (toolId.equals("nature_pickaxe") && (block == Blocks.STONE || block == Blocks.DEEPSLATE)) {
            world.setBlockState(pos, Blocks.OAK_SAPLING.getDefaultState());
            world.spawnParticles(net.minecraft.particle.ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.3, 0.3, 0.3, 0.05);
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
            world.setBlockState(pos, Blocks.DIRT_PATH.getDefaultState());
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
    private static boolean handleFrostToolBreak(ServerPlayerEntity player, ServerWorld world, BlockPos pos, BlockState state, ItemStack tool, String toolId) {
        Block block = state.getBlock();

        // Frost Pickaxe - Freezes water + creates ice blocks
        if (toolId.equals("frost_pickaxe")) {
            // Check adjacent blocks for water and freeze them
            for (Direction dir : Direction.values()) {
                BlockPos adjacent = pos.offset(dir);
                if (world.getBlockState(adjacent).getBlock() == Blocks.WATER) {
                    world.setBlockState(adjacent, Blocks.ICE.getDefaultState());
                }
            }
        }
        // Frost Axe - Freezes logs when chopped
        else if (toolId.equals("frost_axe") && TREE_LOGS.contains(block)) {
            // Frozen logs - break normally but spawn ice particles
            chopTree(player, world, pos, tool);
            world.spawnParticles(net.minecraft.particle.ParticleTypes.SNOWFLAKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 20, 1, 1, 1, 0.1);
            return true;
        }
        // Frost Shovel - Creates packed ice from snow
        else if (toolId.equals("frost_shovel") && (block == Blocks.SNOW || block == Blocks.SNOW_BLOCK)) {
            world.setBlockState(pos, Blocks.PACKED_ICE.getDefaultState());
            return true;
        }

        return false;
    }

    // ============================================================
    // THUNDER TOOLS - Lightning/Electric themed
    // ============================================================
    private static boolean handleThunderToolBreak(ServerPlayerEntity player, ServerWorld world, BlockPos pos, BlockState state, ItemStack tool, String toolId) {
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
            Box box = new Box(pos).expand(8.0);
            for (LivingEntity mob : world.getEntitiesByClass(LivingEntity.class, box, e -> e instanceof HostileEntity)) {
                Vec3d mobPos = new Vec3d(mob.getX(), mob.getY(), mob.getZ());
                Vec3d pushDir = mobPos.subtract(Vec3d.ofCenter(pos)).normalize().multiply(2.0);
                mob.addVelocity(pushDir.x, 0.5, pushDir.z);
                mob.velocityDirty = true;
            }
            world.spawnParticles(net.minecraft.particle.ParticleTypes.ELECTRIC_SPARK, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 30, 1, 1, 1, 0.1);
        }

        return false;
    }

    // ============================================================
    // OCEAN TOOLS - Water/Sea themed
    // ============================================================
    private static boolean handleOceanToolBreak(ServerPlayerEntity player, ServerWorld world, BlockPos pos, BlockState state, ItemStack tool, String toolId) {
        Block block = state.getBlock();

        // Ocean Axe - Creates water streams on chop
        if (toolId.equals("ocean_axe")) {
            // Spawn flowing water in adjacent air blocks
            for (Direction dir : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
                BlockPos waterPos = pos.offset(dir);
                if (world.getBlockState(waterPos).isAir()) {
                    world.setBlockState(waterPos, Blocks.WATER.getDefaultState());
                }
            }
        }
        // Ocean Shovel - Creates quicksand traps (soul sand effect)
        else if (toolId.equals("ocean_shovel") && (block == Blocks.SAND || block == Blocks.DIRT)) {
            world.setBlockState(pos, Blocks.SOUL_SAND.getDefaultState());
            return true;
        }

        return false;
    }

    // ============================================================
    // LUNAR TOOLS - Moon/Night themed
    // ============================================================
    private static boolean handleLunarToolBreak(ServerPlayerEntity player, ServerWorld world, BlockPos pos, BlockState state, ItemStack tool, String toolId) {
        // Lunar Shovel - Grants night vision when digging
        if (toolId.equals("lunar_shovel")) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 400, 0, true, false));
        }
        // Lunar Hoe - Crops glow and grow at night
        else if (toolId.equals("lunar_hoe") && isCrop(state)) {
            if (!world.isDay()) {
                growCrop(world, pos, state);
                world.spawnParticles(net.minecraft.particle.ParticleTypes.GLOW, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.3, 0.3, 0.3, 0.05);
                return true;
            }
        }
        // Lunar Pickaxe - Silverfish detection (highlight nearby silverfish)
        else if (toolId.equals("lunar_pickaxe")) {
            Box box = new Box(pos).expand(10.0);
            for (var silverfish : world.getEntitiesByClass(net.minecraft.entity.mob.SilverfishEntity.class, box, e -> true)) {
                silverfish.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 100, 0, true, false));
            }
        }

        return false;
    }

    // ============================================================
    // SOLAR TOOLS - Sun/Day themed
    // ============================================================
    private static boolean handleSolarToolBreak(ServerPlayerEntity player, ServerWorld world, BlockPos pos, BlockState state, ItemStack tool, String toolId) {
        Block block = state.getBlock();

        // Solar Pickaxe - Burns stone into smooth variants
        if (toolId.equals("solar_pickaxe") && world.isDay()) {
            if (block == Blocks.STONE || block == Blocks.DEEPSLATE) {
                Block smoothVariant = block == Blocks.DEEPSLATE ? Blocks.POLISHED_DEEPSLATE : Blocks.SMOOTH_STONE;
                world.setBlockState(pos, smoothVariant.getDefaultState());
                world.spawnParticles(net.minecraft.particle.ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.3, 0.3, 0.3, 0.05);
                return true;
            }
        }
        // Solar Shovel - Faster mining during day
        else if (toolId.equals("solar_shovel") && world.isDay()) {
            if (!player.hasStatusEffect(StatusEffects.HASTE)) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 60, 1, true, false));
            }
        }
        // Solar Hoe - Crops grow 10x faster in sunlight
        else if (toolId.equals("solar_hoe") && isCrop(state) && world.isDay()) {
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
    private static boolean handleTerraToolBreak(ServerPlayerEntity player, ServerWorld world, BlockPos pos, BlockState state, ItemStack tool, String toolId) {
        // Terra Pickaxe - 5x5 mining with haste boost
        if (toolId.equals("terra_pickaxe")) {
            break5x5Area(player, world, pos, tool);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 60, 0, true, false));
            return true;
        }
        // Terra Shovel - Flattens large areas instantly
        else if (toolId.equals("terra_shovel")) {
            flattenArea(player, world, pos, tool);
            return true;
        }
        // Terra Hoe - Generates bonemeal from tilling
        else if (toolId.equals("terra_hoe")) {
            if (player.getInventory().insertStack(new ItemStack(Items.BONE_MEAL))) {
                world.spawnParticles(net.minecraft.particle.ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 5, 0.3, 0.3, 0.3, 0.05);
            }
            till3x3Area(player, world, pos, false);
            return true;
        }

        return false;
    }

    // ============================================================
    // PHANTOM TOOLS - Ghost/Invisible themed
    // ============================================================
    private static boolean handlePhantomToolBreak(ServerPlayerEntity player, ServerWorld world, BlockPos pos, BlockState state, ItemStack tool, String toolId) {
        // Phantom Hoe - Crops drop XP orbs
        if (toolId.equals("phantom_hoe") && isCrop(state)) {
            world.breakBlock(pos, true);
            // Drop extra XP
            player.addExperience(5);
            world.spawnEntity(new net.minecraft.entity.ExperienceOrbEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3));
            return true;
        }
        // Phantom Shovel - Walk through blocks briefly (no clip effect simulated by speed)
        else if (toolId.equals("phantom_shovel")) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, 2, true, false));
        }

        return false;
    }

    // ============================================================
    // BLOOD TOOLS - Vampire/Life steal themed
    // ============================================================
    private static boolean handleBloodToolBreak(ServerPlayerEntity player, ServerWorld world, BlockPos pos, BlockState state, ItemStack tool, String toolId) {
        Block block = state.getBlock();

        // Blood Pickaxe - Heals when mining ores
        if (toolId.equals("blood_pickaxe") && ORE_BLOCKS.contains(block)) {
            float heal = Math.min(2.0f, player.getMaxHealth() - player.getHealth());
            if (heal > 0) {
                player.heal(heal);
                world.spawnParticles(net.minecraft.particle.ParticleTypes.HEART, player.getX(), player.getY() + 1, player.getZ(), 3, 0.5, 0.5, 0.5, 0.1);
            }
        }
        // Blood Shovel - Grave digging - finds buried loot
        else if (toolId.equals("blood_shovel")) {
            if (world.random.nextFloat() < 0.1f) {
                // Chance to find buried treasure items
                ItemStack[] possibleLoot = {
                    new ItemStack(Items.BONE, world.random.nextInt(4) + 1),
                    new ItemStack(Items.ROTTEN_FLESH, world.random.nextInt(2) + 1),
                    new ItemStack(Items.GOLD_NUGGET, world.random.nextInt(3) + 1),
                    new ItemStack(Items.EMERALD)
                };
                ItemStack loot = possibleLoot[world.random.nextInt(possibleLoot.length)];
                Block.dropStack(world, pos.up(), loot);
                world.spawnParticles(net.minecraft.particle.ParticleTypes.SOUL, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.3, 0.3, 0.3, 0.05);
            }
        }
        // Blood Hoe - Uses health to instantly grow crops
        else if (toolId.equals("blood_hoe") && isCrop(state)) {
            if (player.getHealth() > 2.0f) {
                player.damage(world, player.getDamageSources().generic(), 2.0f);
                growCrop(world, pos, state);
                growCrop(world, pos, state);
                world.spawnParticles(net.minecraft.particle.ParticleTypes.DAMAGE_INDICATOR, player.getX(), player.getY() + 1, player.getZ(), 2, 0.5, 0.5, 0.5, 0.1);
                return true;
            }
        }

        return false;
    }

    // ============================================================
    // CELESTIAL TOOLS - Star/Space themed
    // ============================================================
    private static boolean handleCelestialToolBreak(ServerPlayerEntity player, ServerWorld world, BlockPos pos, BlockState state, ItemStack tool, String toolId) {
        Block block = state.getBlock();

        // Celestial Pickaxe - Finds rare ores easier (highlights nearby ores briefly)
        if (toolId.equals("celestial_pickaxe")) {
            Box box = new Box(pos).expand(5.0);
            for (BlockPos orePos : BlockPos.iterate(pos.add(-5, -5, -5), pos.add(5, 5, 5))) {
                if (ORE_BLOCKS.contains(world.getBlockState(orePos).getBlock())) {
                    world.spawnParticles(net.minecraft.particle.ParticleTypes.GLOW, orePos.getX() + 0.5, orePos.getY() + 0.5, orePos.getZ() + 0.5, 5, 0.3, 0.3, 0.3, 0.05);
                }
            }
        }
        // Celestial Shovel - Explosive digging (controlled - breaks 3x3 without drops, then drops center)
        else if (toolId.equals("celestial_shovel")) {
            break3x3NoDrops(world, pos);
            // Give drops for center block only
            world.breakBlock(pos, true);
            return true;
        }
        // Celestial Hoe - Crops sparkle and give bonus drops
        else if (toolId.equals("celestial_hoe") && isCrop(state)) {
            world.breakBlock(pos, true);
            // Bonus drops
            if (world.random.nextFloat() < 0.3f) {
                Block.dropStack(world, pos, new ItemStack(Items.GLOWSTONE_DUST, world.random.nextInt(2) + 1));
            }
            world.spawnParticles(net.minecraft.particle.ParticleTypes.GLOW, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.3, 0.3, 0.3, 0.05);
            return true;
        }

        return false;
    }

    // ============================================================
    // SHADOW TOOLS - Darkness/Stealth themed
    // ============================================================
    private static boolean handleShadowToolBreak(ServerPlayerEntity player, ServerWorld world, BlockPos pos, BlockState state, ItemStack tool, String toolId) {
        // Shadow Shovel - Creates darkness around dig site ( blindness to nearby mobs)
        if (toolId.equals("shadow_shovel")) {
            Box box = new Box(pos).expand(6.0);
            for (LivingEntity mob : world.getEntitiesByClass(LivingEntity.class, box, e -> e != player)) {
                mob.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 60, 0));
            }
            world.spawnParticles(net.minecraft.particle.ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 20, 1, 1, 1, 0.05);
        }
        // Shadow Hoe - Poisonous crops damage enemies (spawn lingering poison)
        else if (toolId.equals("shadow_hoe")) {
            // Plant poisonous potatoes instead
            if (world.random.nextFloat() < 0.3f) {
                Block.dropStack(world, pos.up(), new ItemStack(Items.POISONOUS_POTATO));
            }
        }

        return false;
    }

    // ============================================================
    // CRYSTAL TOOLS - Gem/Shiny themed
    // ============================================================
    private static boolean handleCrystalToolBreak(ServerPlayerEntity player, ServerWorld world, BlockPos pos, BlockState state, ItemStack tool, String toolId) {
        Block block = state.getBlock();

        // Crystal Shovel - Highlights ores through walls briefly
        if (toolId.equals("crystal_shovel")) {
            Box box = new Box(pos).expand(8.0);
            for (BlockPos orePos : BlockPos.iterate(pos.add(-8, -8, -8), pos.add(8, 8, 8))) {
                if (ORE_BLOCKS.contains(world.getBlockState(orePos).getBlock())) {
                    world.spawnParticles(net.minecraft.particle.ParticleTypes.GLOW, orePos.getX() + 0.5, orePos.getY() + 0.5, orePos.getZ() + 0.5, 3, 0.3, 0.3, 0.3, 0.05);
                }
            }
        }
        // Crystal Hoe - Crops have chance to drop diamonds
        else if (toolId.equals("crystal_hoe") && isCrop(state)) {
            world.breakBlock(pos, true);
            if (world.random.nextFloat() < 0.02f) { // 2% chance
                Block.dropStack(world, pos, new ItemStack(Items.DIAMOND));
                world.spawnParticles(net.minecraft.particle.ParticleTypes.GLOW, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 20, 0.3, 0.3, 0.3, 0.1);
            }
            return true;
        }

        return false;
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    private static void break3x3Area(ServerPlayerEntity player, ServerWorld world, BlockPos center, ItemStack tool) {
        Direction facing = player.getHorizontalFacing();
        int dx = Math.abs(facing.getOffsetX());
        int dz = Math.abs(facing.getOffsetZ());

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                BlockPos target;
                if (dx == 0) { // Facing Z, break in X-Y plane
                    target = center.add(x, y, 0);
                } else { // Facing X, break in Z-Y plane
                    target = center.add(0, y, x);
                }
                if (!target.equals(center)) {
                    BlockState targetState = world.getBlockState(target);
                    if (!targetState.isAir() && targetState.getBlock().getHardness() >= 0) {
                        world.breakBlock(target, true);
                    }
                }
            }
        }
    }

    private static void break5x5Area(ServerPlayerEntity player, ServerWorld world, BlockPos center, ItemStack tool) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos target = center.add(x, 0, z);
                if (!target.equals(center)) {
                    BlockState targetState = world.getBlockState(target);
                    if (!targetState.isAir() && targetState.getBlock().getHardness() >= 0) {
                        world.breakBlock(target, true);
                    }
                }
            }
        }
    }

    private static void break3x3NoDrops(ServerWorld world, BlockPos center) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos target = center.add(x, 0, z);
                if (!target.equals(center)) {
                    BlockState targetState = world.getBlockState(target);
                    if (!targetState.isAir() && targetState.getBlock().getHardness() >= 0) {
                        world.breakBlock(target, false);
                    }
                }
            }
        }
    }

    private static void chopTree(ServerPlayerEntity player, ServerWorld world, BlockPos start, ItemStack tool) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(start);
        visited.add(start);

        int count = 0;
        while (!queue.isEmpty() && count < 64) {
            BlockPos current = queue.poll();
            BlockState state = world.getBlockState(current);
            if (TREE_LOGS.contains(state.getBlock())) {
                world.breakBlock(current, true);
                count++;

                // Add neighbors
                for (Direction dir : Direction.values()) {
                    BlockPos neighbor = current.offset(dir);
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }
    }

    private static void chopTreeAndReplant(ServerPlayerEntity player, ServerWorld world, BlockPos start, ItemStack tool, BlockPos saplingPos) {
        chopTree(player, world, start, tool);
        // Plant sapling
        world.setBlockState(saplingPos, Blocks.OAK_SAPLING.getDefaultState());
    }

    private static BlockPos findSaplingPosition(ServerWorld world, BlockPos logPos) {
        // Find a dirt/grass block below the tree
        for (int i = 0; i < 5; i++) {
            BlockPos check = logPos.down(i);
            Block block = world.getBlockState(check).getBlock();
            if (block == Blocks.GRASS_BLOCK || block == Blocks.DIRT) {
                return check.up();
            }
        }
        return null;
    }

    private static void till3x3Area(ServerPlayerEntity player, ServerWorld world, BlockPos center, boolean instantGrowth) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos target = center.add(x, 0, z);
                BlockState state = world.getBlockState(target);
                if (state.getBlock() == Blocks.GRASS_BLOCK || state.getBlock() == Blocks.DIRT) {
                    world.setBlockState(target, Blocks.FARMLAND.getDefaultState());
                    if (instantGrowth) {
                        applyBoneMealEffect(world, target.up());
                    }
                }
            }
        }
    }

    private static void till5x5Area(ServerPlayerEntity player, ServerWorld world, BlockPos center) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos target = center.add(x, 0, z);
                BlockState state = world.getBlockState(target);
                if (state.getBlock() == Blocks.GRASS_BLOCK || state.getBlock() == Blocks.DIRT) {
                    world.setBlockState(target, Blocks.FARMLAND.getDefaultState());
                }
            }
        }
    }

    private static void flattenArea(ServerPlayerEntity player, ServerWorld world, BlockPos center, ItemStack tool) {
        int yLevel = center.getY();
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                BlockPos target = center.add(x, 0, z);
                BlockState state = world.getBlockState(target);
                if (!state.isAir() && state.getBlock().getHardness() >= 0) {
                    if (target.getY() > yLevel) {
                        world.breakBlock(target, true);
                    } else if (target.getY() < yLevel) {
                        world.setBlockState(target, Blocks.DIRT.getDefaultState());
                    }
                }
            }
        }
    }

    private static void applyBoneMealEffect(ServerWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof net.minecraft.block.CropBlock crop) {
            int currentAge = state.get(net.minecraft.block.CropBlock.AGE);
            int maxAge = crop.getMaxAge();
            if (currentAge < maxAge) {
                world.setBlockState(pos, state.with(net.minecraft.block.CropBlock.AGE, Math.min(currentAge + 2, maxAge)));
                world.spawnParticles(net.minecraft.particle.ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.3, 0.3, 0.3, 0.05);
            }
        }
    }

    private static void veinMineWithLightning(ServerPlayerEntity player, ServerWorld world, BlockPos start, Block oreBlock, ItemStack tool) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(start);
        visited.add(start);

        int count = 0;
        while (!queue.isEmpty() && count < 16) {
            BlockPos current = queue.poll();
            BlockState state = world.getBlockState(current);
            if (state.getBlock() == oreBlock || ORE_BLOCKS.contains(state.getBlock())) {
                // Summon lightning
                if (count == 0) {
                    net.minecraft.entity.LightningEntity lightning = new net.minecraft.entity.LightningEntity(net.minecraft.entity.EntityType.LIGHTNING_BOLT, world);
                    lightning.setPosition(current.getX(), current.getY(), current.getZ());
                    world.spawnEntity(lightning);
                }
                world.breakBlock(current, true);
                count++;

                // Add neighbors
                for (Direction dir : Direction.values()) {
                    BlockPos neighbor = current.offset(dir);
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }
    }

    private static void chopTreeWithChainLightning(ServerPlayerEntity player, ServerWorld world, BlockPos start, ItemStack tool) {
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
                    BlockPos neighbor = current.offset(dir);
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
            world.spawnParticles(net.minecraft.particle.ParticleTypes.ELECTRIC_SPARK,
                from.getX() + 0.5, from.getY() + 0.5, from.getZ() + 0.5,
                10, (to.getX() - from.getX()) * 0.1, (to.getY() - from.getY()) * 0.1, (to.getZ() - from.getZ()) * 0.1, 0.1);
        }

        // Break all logs
        for (BlockPos pos : logPositions) {
            world.breakBlock(pos, true);
        }
    }

    private static boolean isCrop(BlockState state) {
        return state.getBlock() instanceof net.minecraft.block.CropBlock ||
               state.getBlock() == Blocks.CARROTS ||
               state.getBlock() == Blocks.POTATOES ||
               state.getBlock() == Blocks.WHEAT ||
               state.getBlock() == Blocks.BEETROOTS ||
               state.getBlock() == Blocks.NETHER_WART;
    }

    private static void growCrop(ServerWorld world, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof net.minecraft.block.CropBlock crop) {
            int currentAge = state.get(net.minecraft.block.CropBlock.AGE);
            int maxAge = crop.getMaxAge();
            if (currentAge < maxAge) {
                world.setBlockState(pos, state.with(net.minecraft.block.CropBlock.AGE, Math.min(currentAge + 1, maxAge)));
                world.spawnParticles(net.minecraft.particle.ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.3, 0.3, 0.3, 0.05);
            }
        }
    }
}
