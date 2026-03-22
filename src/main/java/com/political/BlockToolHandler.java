package com.political;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles special abilities for block-themed tools.
 * Each tool type has unique abilities based on its block material.
 * Updated: Push test
 */
public class BlockToolHandler {

    // Cooldown tracking for abilities
    private static final Map<UUID, Long> abilityCooldowns = new HashMap<>();
    private static final long COOLDOWN_MS = 3000; // 3 second cooldown

    // ============================================================
    // TOOL TYPE DETECTION
    // ============================================================

    public static boolean isBlockTool(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        String id = SlayerItems.getCustomItemId(stack);
        if (id == null) return false;
        return id.endsWith("_pickaxe") || id.endsWith("_axe") || 
               id.endsWith("_shovel") || id.endsWith("_hoe");
    }

    public static String getToolMaterial(ItemStack stack) {
        String id = SlayerItems.getCustomItemId(stack);
        if (id == null) return null;
        // Extract material name (e.g., "glass_pickaxe" -> "glass")
        int underscore = id.lastIndexOf('_');
        if (underscore > 0) {
            return id.substring(0, underscore);
        }
        return null;
    }

    public static String getToolType(ItemStack stack) {
        String id = SlayerItems.getCustomItemId(stack);
        if (id == null) return null;
        int underscore = id.lastIndexOf('_');
        if (underscore > 0) {
            return id.substring(underscore + 1);
        }
        return null;
    }

    // ============================================================
    // ABILITY TRIGGERS
    // ============================================================

    public static void onBlockBreak(ServerPlayerEntity player, World world, BlockPos pos, BlockState state, ItemStack tool) {
        if (!isBlockTool(tool)) return;
        
        String material = getToolMaterial(tool);
        String toolType = getToolType(tool);
        if (material == null || toolType == null) return;

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
        }
    }

    public static void onEntityHit(ServerPlayerEntity player, Entity target, ItemStack tool) {
        if (!isBlockTool(tool)) return;
        if (!(target instanceof LivingEntity)) return;
        
        String material = getToolMaterial(tool);
        if (material == null) return;

        LivingEntity livingTarget = (LivingEntity) target;

        switch (material) {
            case "ice" -> {
                // Ice tools slow enemies
                livingTarget.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 1));
                player.getEntityWorld().spawnParticles(ParticleTypes.SNOWFLAKE, 
                    target.getX(), target.getY() + 1, target.getZ(), 10, 0.5, 0.5, 0.5, 0.1);
            }
            case "glowstone" -> {
                // Glowstone tools deal bonus damage to undead
                if (isUndead(livingTarget)) {
                    livingTarget.damage(player.getEntityWorld(), player.getDamageSources().playerAttack(player), 2.0f);
                    player.getEntityWorld().spawnParticles(ParticleTypes.END_ROD,
                        target.getX(), target.getY() + 1, target.getZ(), 15, 0.5, 0.5, 0.5, 0.1);
                }
            }
            case "redstone" -> {
                // Redstone tools stun enemies
                livingTarget.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 2));
                player.getEntityWorld().spawnParticles(ParticleTypes.CRIT,
                    target.getX(), target.getY() + 1, target.getZ(), 10, 0.5, 0.5, 0.5, 0.1);
            }
            case "netherrack", "magma" -> {
                // Netherrack/Magma tools set enemies on fire
                livingTarget.setFireTicks(60);
                player.getEntityWorld().spawnParticles(ParticleTypes.FLAME,
                    target.getX(), target.getY() + 1, target.getZ(), 10, 0.5, 0.5, 0.5, 0.1);
            }
            case "endstone" -> {
                // End Stone tools deal bonus damage to ender mobs
                if (isEnderMob(livingTarget)) {
                    livingTarget.damage(player.getEntityWorld(), player.getDamageSources().playerAttack(player), 3.0f);
                }
            }
            case "prismarine" -> {
                // Prismarine tools deal bonus damage to ocean mobs
                if (isOceanMob(livingTarget)) {
                    livingTarget.damage(player.getEntityWorld(), player.getDamageSources().playerAttack(player), 3.0f);
                }
            }
            case "soul_sand" -> {
                // Soul Sand tools slow and wither
                livingTarget.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 1));
                livingTarget.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 40, 0));
            }
            case "coal" -> {
                // Coal tools blind enemies
                livingTarget.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 40, 0));
            }
            case "amethyst" -> {
                // Amethyst tools have sonic boom chance
                if (player.getRandom().nextFloat() < 0.2f) {
                    livingTarget.damage(player.getEntityWorld(), player.getDamageSources().sonicBoom(player), 4.0f);
                    player.playSound(SoundEvents.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 1.0f);
                }
            }
        }
    }

    // ============================================================
    // MATERIAL-SPECIFIC BREAK HANDLERS
    // ============================================================

    private static void handleGlassToolBreak(ServerPlayerEntity player, World world, BlockPos pos, BlockState state, String toolType) {
        // Glass tools: 20% chance to double drops
        if (player.getRandom().nextFloat() < 0.2f) {
            Block.dropStacks(state, world, pos, null, player, player.getMainHandStack());
            player.sendMessage(Text.literal("✦ Double drops!").formatted(Formatting.AQUA), true);
        }
        
        // Glass Shovel: 3x3 excavation when sneaking
        if ("shovel".equals(toolType) && player.isSneaking()) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    BlockPos adjPos = pos.add(dx, 0, dz);
                    BlockState adjState = world.getBlockState(adjPos);
                    if (adjState.getBlock().getHardness() >= 0 && isShovelEffective(adjState)) {
                        world.breakBlock(adjPos, true, player);
                    }
                }
            }
            player.sendMessage(Text.literal("⛏ 3x3 Excavation!").formatted(Formatting.AQUA), true);
        }
        
        // Glass Hoe: Auto-replant crops (handled in block break event)
        if ("hoe".equals(toolType)) {
            // Check if broken crop - replant handled elsewhere
            ((ServerWorld) world).spawnParticles(ParticleTypes.END_ROD,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.3, 0.3, 0.3, 0.05);
        }
    }
    
    private static boolean isShovelEffective(BlockState state) {
        Block block = state.getBlock();
        return block == Blocks.DIRT || block == Blocks.GRASS_BLOCK || block == Blocks.SAND ||
               block == Blocks.GRAVEL || block == Blocks.SOUL_SAND || block == Blocks.SOUL_SOIL ||
               block == Blocks.CLAY || block == Blocks.COARSE_DIRT || block == Blocks.ROOTED_DIRT;
    }

    private static void handleObsidianToolBreak(ServerPlayerEntity player, World world, BlockPos pos, BlockState state, String toolType) {
        // Obsidian pickaxe can slowly break bedrock
        if ("pickaxe".equals(toolType) && state.getBlock() == Blocks.BEDROCK) {
            // Very slow - takes many hits, but can actually break it
            player.sendMessage(Text.literal("⛏ Breaking bedrock...").formatted(Formatting.DARK_PURPLE), true);
            
            // Actually break the bedrock with a small chance
            if (player.getRandom().nextFloat() < 0.1f) { // 10% chance per hit
                if (world instanceof ServerWorld serverWorld) {
                    serverWorld.breakBlock(pos, true, player);
                    serverWorld.spawnParticles(ParticleTypes.EXPLOSION,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            10, 0.5, 0.5, 0.5, 0.1);
                    player.sendMessage(Text.literal("💥 Bedrock shattered!").formatted(Formatting.DARK_RED, Formatting.BOLD), true);
                    return;
                }
            }
        }
        
        // Obsidian Pickaxe: Fortune III effect - bonus drops
        if ("pickaxe".equals(toolType) && player.getRandom().nextFloat() < 0.3f) {
            if (isOre(state.getBlock())) {
                Block.dropStacks(state, world, pos, null, player, player.getMainHandStack());
                player.sendMessage(Text.literal("✦ Fortune bonus!").formatted(Formatting.DARK_PURPLE), true);
            }
        }
        
        // Obsidian Axe: 3x3 strip logs
        if ("axe".equals(toolType)) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    BlockPos adjPos = pos.add(dx, 0, dz);
                    BlockState adjState = world.getBlockState(adjPos);
                    if (adjState.getBlock() instanceof net.minecraft.block.PillarBlock && 
                        adjState.isIn(net.minecraft.registry.tag.BlockTags.LOGS)) {
                        // Strip the log
                        world.setBlockState(adjPos, adjState.getBlock() == Blocks.OAK_LOG ? Blocks.STRIPPED_OAK_LOG.getDefaultState() :
                            adjState.getBlock() == Blocks.SPRUCE_LOG ? Blocks.STRIPPED_SPRUCE_LOG.getDefaultState() :
                            adjState.getBlock() == Blocks.BIRCH_LOG ? Blocks.STRIPPED_BIRCH_LOG.getDefaultState() :
                            adjState.getBlock() == Blocks.JUNGLE_LOG ? Blocks.STRIPPED_JUNGLE_LOG.getDefaultState() :
                            adjState.getBlock() == Blocks.DARK_OAK_LOG ? Blocks.STRIPPED_DARK_OAK_LOG.getDefaultState() :
                            adjState.getBlock() == Blocks.ACACIA_LOG ? Blocks.STRIPPED_ACACIA_LOG.getDefaultState() :
                            adjState.getBlock() == Blocks.CRIMSON_STEM ? Blocks.STRIPPED_CRIMSON_STEM.getDefaultState() :
                            adjState.getBlock() == Blocks.WARPED_STEM ? Blocks.STRIPPED_WARPED_STEM.getDefaultState() :
                            adjState);
                    }
                }
            }
        }
        
        // Obsidian Shovel: 5x5 excavation when sneaking
        if ("shovel".equals(toolType) && player.isSneaking()) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    BlockPos adjPos = pos.add(dx, 0, dz);
                    BlockState adjState = world.getBlockState(adjPos);
                    if (adjState.getBlock().getHardness() >= 0 && isShovelEffective(adjState)) {
                        world.breakBlock(adjPos, true, player);
                    }
                }
            }
            player.sendMessage(Text.literal("⛏ 5x5 Excavation!").formatted(Formatting.DARK_PURPLE), true);
        }
        
        // Obsidian Hoe: Crops grow faster (handled via event)
        if ("hoe".equals(toolType)) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.LUCK, 100, 0));
        }
    }

    private static void handleQuartzToolBreak(ServerPlayerEntity player, World world, BlockPos pos, BlockState state, String toolType) {
        // Quartz tools: Silk touch behavior handled elsewhere
        if ("pickaxe".equals(toolType)) {
            ((ServerWorld) world).spawnParticles(ParticleTypes.END_ROD,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.3, 0.3, 0.3, 0.05);
        }
    }

    private static void handleGlowstoneToolBreak(ServerPlayerEntity player, World world, BlockPos pos, BlockState state, String toolType) {
        // Glowstone tools: 30% bonus glowstone dust
        if (state.getBlock() == Blocks.GLOWSTONE && player.getRandom().nextFloat() < 0.3f) {
            Block.dropStack(world, pos, new ItemStack(Items.GLOWSTONE_DUST, 2));
            player.sendMessage(Text.literal("✦ Bonus glowstone!").formatted(Formatting.YELLOW), true);
        }
    }

    private static void handleRedstoneToolBreak(ServerPlayerEntity player, World world, BlockPos pos, BlockState state, String toolType) {
        // Redstone pickaxe: Auto-smelts ores
        if ("pickaxe".equals(toolType) && isOre(state.getBlock())) {
            // Smelting handled in event
            ((ServerWorld) world).spawnParticles(ParticleTypes.CRIT,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.3, 0.3, 0.3, 0.1);
        }
    }

    private static void handleNetherrackToolBreak(ServerPlayerEntity player, World world, BlockPos pos, BlockState state, String toolType) {
        // Netherrack tools: Faster in Nether
        if (world.getRegistryKey() == World.NETHER) {
            player.sendMessage(Text.literal("⛏ Nether mining speed!").formatted(Formatting.DARK_RED), true);
        }
    }

    private static void handleEndstoneToolBreak(ServerPlayerEntity player, World world, BlockPos pos, BlockState state, String toolType) {
        // End Stone tools: Teleport drops to player
        if ("pickaxe".equals(toolType) || "shovel".equals(toolType)) {
            // Items teleport to player
            ((ServerWorld) world).spawnParticles(ParticleTypes.PORTAL,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 15, 0.5, 0.5, 0.5, 0.1);
        }
    }

    private static void handleIceToolBreak(ServerPlayerEntity player, World world, BlockPos pos, BlockState state, String toolType) {
        // Ice tools: Freeze nearby water/lava
        if ("pickaxe".equals(toolType)) {
            for (Direction dir : Direction.values()) {
                BlockPos adjacent = pos.offset(dir);
                BlockState adjState = world.getBlockState(adjacent);
                if (adjState.getBlock() == Blocks.WATER) {
                    world.setBlockState(adjacent, Blocks.ICE.getDefaultState());
                } else if (adjState.getBlock() == Blocks.LAVA) {
                    world.setBlockState(adjacent, Blocks.OBSIDIAN.getDefaultState());
                }
            }
        }
    }

    private static void handlePrismarineToolBreak(ServerPlayerEntity player, World world, BlockPos pos, BlockState state, String toolType) {
        // Prismarine tools: Underwater bonuses
        if (player.isSubmergedInWater()) {
            player.sendMessage(Text.literal("🌊 Underwater mining bonus!").formatted(Formatting.DARK_AQUA), true);
        }
    }

    private static void handleTerracottaToolBreak(ServerPlayerEntity player, World world, BlockPos pos, BlockState state, String toolType) {
        // Terracotta tools: Faster on terracotta/clay
        if (state.getBlock() == Blocks.TERRACOTTA || state.getBlock() == Blocks.CLAY) {
            player.sendMessage(Text.literal("⛏ Clay mining speed!").formatted(Formatting.GOLD), true);
        }
    }

    private static void handleMossyToolBreak(ServerPlayerEntity player, World world, BlockPos pos, BlockState state, String toolType) {
        // Mossy tools: Spread moss to nearby stone
        if ("pickaxe".equals(toolType)) {
            for (Direction dir : Direction.values()) {
                BlockPos adjacent = pos.offset(dir);
                BlockState adjState = world.getBlockState(adjacent);
                if (adjState.getBlock() == Blocks.STONE && player.getRandom().nextFloat() < 0.1f) {
                    world.setBlockState(adjacent, Blocks.MOSSY_COBBLESTONE.getDefaultState());
                }
            }
        }
    }

    private static void handleSoulSandToolBreak(ServerPlayerEntity player, World world, BlockPos pos, BlockState state, String toolType) {
        // Soul Sand tools: Normal speed on soul sand
        if (state.getBlock() == Blocks.SOUL_SAND || state.getBlock() == Blocks.SOUL_SOIL) {
            player.sendMessage(Text.literal("⛏ Soul speed!").formatted(Formatting.DARK_GRAY), true);
        }
    }

    private static void handleMagmaToolBreak(ServerPlayerEntity player, World world, BlockPos pos, BlockState state, String toolType) {
        // Magma tools: Fire resistance in Nether
        if (world.getRegistryKey() == World.NETHER) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 100, 0));
        }
    }

    private static void handleSandstoneToolBreak(ServerPlayerEntity player, World world, BlockPos pos, BlockState state, String toolType) {
        // Sandstone tools: Faster on sand
        if (state.getBlock() == Blocks.SAND || state.getBlock() == Blocks.SANDSTONE) {
            player.sendMessage(Text.literal("⛏ Desert speed!").formatted(Formatting.YELLOW), true);
        }
    }

    private static void handleAmethystToolBreak(ServerPlayerEntity player, World world, BlockPos pos, BlockState state, String toolType) {
        // Amethyst tools: 25% bonus amethyst shards
        if (state.getBlock() == Blocks.AMETHYST_CLUSTER && player.getRandom().nextFloat() < 0.25f) {
            Block.dropStack(world, pos, new ItemStack(Items.AMETHYST_SHARD, 2));
            player.sendMessage(Text.literal("✦ Bonus amethyst!").formatted(Formatting.LIGHT_PURPLE), true);
        }
    }

    private static void handleCoalToolBreak(ServerPlayerEntity player, World world, BlockPos pos, BlockState state, String toolType) {
        // Coal tools: 50% bonus coal
        if ((state.getBlock() == Blocks.COAL_ORE || state.getBlock() == Blocks.DEEPSLATE_COAL_ORE) 
            && player.getRandom().nextFloat() < 0.5f) {
            Block.dropStack(world, pos, new ItemStack(Items.COAL, 1));
            player.sendMessage(Text.literal("✦ Bonus coal!").formatted(Formatting.DARK_GRAY), true);
        }
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    private static boolean isOre(Block block) {
        return block == Blocks.IRON_ORE || block == Blocks.GOLD_ORE || 
               block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_IRON_ORE ||
               block == Blocks.DEEPSLATE_GOLD_ORE || block == Blocks.DEEPSLATE_COPPER_ORE ||
               block == Blocks.NETHER_GOLD_ORE || block == Blocks.NETHER_QUARTZ_ORE;
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
