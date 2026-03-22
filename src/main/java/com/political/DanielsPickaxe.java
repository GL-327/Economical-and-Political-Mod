package com.political;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;

import java.util.*;

/**
 * Daniel's Pickaxe - A legendary mining tool with toggleable 3x3 tunnel + vein mining.
 * When ability is OFF: Insta-breaks all blocks (extreme mining speed with Haste 10)
 * When ability is ON: 3x3 tunnel mining + vein mining for ores + auto-smelt
 */
public class DanielsPickaxe {

    private static final String TAG_DANIELS_PICKAXE = "daniels_pickaxe";
    private static final String TAG_ABILITY_ACTIVE = "daniels_ability_active";
    private static final String TAG_MODE = "daniels_mode"; // 0 = insta, 1 = tunnel
    private static final Identifier MINING_SPEED_MODIFIER = Identifier.of("political", "daniels_mining_speed");

    // Track ability state per player
    private static final Map<UUID, Boolean> abilityActive = new HashMap<>();
    private static final Map<UUID, Long> lastHasteTime = new HashMap<>(); // Track last haste refresh
    private static final Map<UUID, Long> lastToggleTime = new HashMap<>(); // Track last mode toggle

    // Blocks that count as ores for vein mining
    private static final Set<Block> ORE_BLOCKS = new HashSet<>(Arrays.asList(
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
    ));

    // Blocks that auto-smelt into ingots/gems (maps block to item)
    private static final Map<Block, net.minecraft.item.Item> SMELT_RESULTS = new HashMap<>();
    static {
        SMELT_RESULTS.put(Blocks.IRON_ORE, Items.IRON_INGOT);
        SMELT_RESULTS.put(Blocks.DEEPSLATE_IRON_ORE, Items.IRON_INGOT);
        SMELT_RESULTS.put(Blocks.GOLD_ORE, Items.GOLD_INGOT);
        SMELT_RESULTS.put(Blocks.DEEPSLATE_GOLD_ORE, Items.GOLD_INGOT);
        SMELT_RESULTS.put(Blocks.COPPER_ORE, Items.COPPER_INGOT);
        SMELT_RESULTS.put(Blocks.DEEPSLATE_COPPER_ORE, Items.COPPER_INGOT);
        SMELT_RESULTS.put(Blocks.NETHER_GOLD_ORE, Items.GOLD_INGOT);
        SMELT_RESULTS.put(Blocks.ANCIENT_DEBRIS, Items.NETHERITE_SCRAP);
    }

    public static ItemStack create() {
        // Start with diamond pickaxe for insta-mine mode (base)
        ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);

        NbtCompound nbt = new NbtCompound();
        nbt.putByte(TAG_DANIELS_PICKAXE, (byte) 1);
        nbt.putByte(TAG_MODE, (byte) 0); // 0 = insta mode
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

        stack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Daniel's Pickaxe").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));

        stack.set(DataComponentTypes.LORE, new LoreComponent(Arrays.asList(
                Text.literal(""),
                Text.literal("◆ LEGENDARY MINING TOOL ◆").formatted(Formatting.DARK_PURPLE, Formatting.BOLD),
                Text.literal(""),
                Text.literal("Right-click: Toggle Mining Mode").formatted(Formatting.YELLOW),
                Text.literal(""),
                Text.literal("Mode: INSTANT BREAK").formatted(Formatting.AQUA, Formatting.BOLD),
                Text.literal("  └ Breaks any block instantly").formatted(Formatting.GRAY),
                Text.literal("  └ Haste LX for extreme speed").formatted(Formatting.GRAY),
                Text.literal("  └ Diamond Pickaxe").formatted(Formatting.GRAY),
                Text.literal(""),
                Text.literal("§7Toggle for TUNNEL MODE:").formatted(Formatting.GRAY),
                Text.literal("  └ 3x3 tunnel mining").formatted(Formatting.GRAY),
                Text.literal("  └ Vein mining for ores").formatted(Formatting.GRAY),
                Text.literal("  └ Auto-smelts ores").formatted(Formatting.GRAY),
                Text.literal("  └ Netherite Pickaxe").formatted(Formatting.GRAY),
                Text.literal(""),
                Text.literal("Unbreakable").formatted(Formatting.GREEN),
                Text.literal("§8Cannot apply Efficiency, Silk Touch, or Mending").formatted(Formatting.DARK_GRAY),
                Text.literal(""),
                Text.literal("§8[Forged in the depths of determination]").formatted(Formatting.DARK_GRAY),
                Text.literal("「Legendary」").formatted(Formatting.DARK_PURPLE)
        )));

        // Make unbreakable (very high durability)
        stack.set(DataComponentTypes.MAX_DAMAGE, Integer.MAX_VALUE);

        // Add Fortune V and Efficiency V enchantments via NBT
        NbtCompound display = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound())).copyNbt();
        
        // Store enchantments in custom data for the pickaxe
        NbtCompound enchantNbt = new NbtCompound();
        enchantNbt.putInt("fortune", 5);
        enchantNbt.putInt("efficiency", 5);
        display.put("StoredEnchantments", enchantNbt);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(display));

        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(stack, "daniels_pickaxe");

        return stack;
    }

    public static boolean isDanielsPickaxe(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        NbtCompound nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound())).copyNbt();
        return nbt.contains(TAG_DANIELS_PICKAXE);
    }

    public static boolean isAbilityActive(UUID playerUuid) {
        return abilityActive.getOrDefault(playerUuid, false);
    }

    public static boolean isAbilityActive(ServerPlayerEntity player) {
        return isAbilityActive(player.getUuid());
    }

    public static void toggleAbility(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();

        long now = System.currentTimeMillis();
        long last = lastToggleTime.getOrDefault(uuid, 0L);
        if (now - last < 5000) {
            player.sendMessage(Text.literal("⏳ Please wait before switching modes again").formatted(Formatting.RED), true);
            return;
        }
        lastToggleTime.put(uuid, now);

        boolean currentState = abilityActive.getOrDefault(uuid, false);
        boolean newState = !currentState;
        abilityActive.put(uuid, newState);

        // Switch the pickaxe type based on mode
        switchPickaxeType(player, newState);

        // Update the item's lore to reflect the state
        updateItemLore(player, newState);

        // Play sound and show message
        if (newState) {
            player.sendMessage(
                    Text.literal("⛏ TUNNEL MODE ACTIVATED").formatted(Formatting.GOLD, Formatting.BOLD),
                    true
            );
            player.getEntityWorld().playSound(
                    null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1.0f, 1.5f
            );
        } else {
            player.sendMessage(
                    Text.literal("⛏ INSTANT BREAK MODE ACTIVATED").formatted(Formatting.AQUA, Formatting.BOLD),
                    true
            );
            player.getEntityWorld().playSound(
                    null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.PLAYERS, 1.0f, 1.0f
            );
        }
    }

    /**
     * Switch the pickaxe item type based on mode.
     * Tunnel mode = Netherite pickaxe (for 3x3 mining)
     * Instant break mode = Diamond pickaxe (with haste 10)
     */
    private static void switchPickaxeType(ServerPlayerEntity player, boolean tunnelMode) {
        ItemStack mainHand = player.getMainHandStack();
        if (!isDanielsPickaxe(mainHand)) return;

        // Get the NBT data to preserve
        NbtCompound nbt = mainHand.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound())).copyNbt();
        nbt.putByte(TAG_MODE, tunnelMode ? (byte) 1 : (byte) 0);

        // Create new pickaxe based on mode
        ItemStack newPickaxe;
        if (tunnelMode) {
            newPickaxe = new ItemStack(Items.NETHERITE_PICKAXE);
        } else {
            newPickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
        }

        // Copy all data to new pickaxe
        newPickaxe.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        newPickaxe.set(DataComponentTypes.CUSTOM_NAME, mainHand.get(DataComponentTypes.CUSTOM_NAME));
        newPickaxe.set(DataComponentTypes.MAX_DAMAGE, Integer.MAX_VALUE);
        newPickaxe.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        
        // Copy lore (will be updated separately)
        newPickaxe.set(DataComponentTypes.LORE, mainHand.get(DataComponentTypes.LORE));

        // Copy custom item ID
        SlayerItems.setCustomItemId(newPickaxe, "daniels_pickaxe");

        // Replace in main hand using the proper vanilla method
        player.setStackInHand(net.minecraft.util.Hand.MAIN_HAND, newPickaxe);
    }

    private static void updateItemLore(ServerPlayerEntity player, boolean abilityActive) {
        ItemStack mainHand = player.getMainHandStack();
        if (!isDanielsPickaxe(mainHand)) return;
        updateItemLoreOnStack(mainHand, abilityActive);
    }

    /**
     * Update lore on a specific ItemStack (used when switching types).
     */
    private static void updateItemLoreOnStack(ItemStack stack, boolean tunnelMode) {
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ LEGENDARY MINING TOOL ◆").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Right-click: Toggle Mining Mode").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));

        if (tunnelMode) {
            lore.add(Text.literal("Mode: TUNNEL MINING").formatted(Formatting.GOLD, Formatting.BOLD));
            lore.add(Text.literal("  └ 3x3 tunnel excavation").formatted(Formatting.GRAY));
            lore.add(Text.literal("  └ Vein mining ores").formatted(Formatting.GRAY));
            lore.add(Text.literal("  └ Auto-smelts ores").formatted(Formatting.GRAY));
            lore.add(Text.literal("  └ Netherite Pickaxe").formatted(Formatting.GRAY));
        } else {
            lore.add(Text.literal("Mode: INSTANT BREAK").formatted(Formatting.AQUA, Formatting.BOLD));
            lore.add(Text.literal("  └ Breaks any block instantly").formatted(Formatting.GRAY));
            lore.add(Text.literal("  └ Haste LX for extreme speed").formatted(Formatting.GRAY));
            lore.add(Text.literal("  └ Diamond Pickaxe").formatted(Formatting.GRAY));
        }

        lore.add(Text.literal(""));
        lore.add(Text.literal("Unbreakable").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[Forged in the depths of determination]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("「Legendary」").formatted(Formatting.DARK_PURPLE));

        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
    }

    /**
     * Called every tick to refresh haste effect in insta-mine mode.
     * Should be called from a player tick event.
     */
    public static void tickPlayer(ServerPlayerEntity player) {
        if (!isAbilityActive(player)) {
            // Insta-mine mode - apply Haste 5
            long currentTime = System.currentTimeMillis();
            long lastTime = lastHasteTime.getOrDefault(player.getUuid(), 0L);
            
            // Refresh every 1000ms (1 second), duration 40 ticks (2 seconds)
            if (currentTime - lastTime >= 1000) {
                if (isDanielsPickaxe(player.getMainHandStack())) {
                    player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.HASTE, 40, 59, true, false, false)); // Haste 60 (level 59)
                    lastHasteTime.put(player.getUuid(), currentTime);
                }
            }
        }
    }

    /**
     * Called when a block is broken with Daniel's Pickaxe.
     * Returns true if the break should be handled specially (3x3/vein mining).
     */
    public static boolean onBlockBreak(ServerPlayerEntity player, ServerWorld world, BlockPos pos, BlockState state) {
        ItemStack tool = player.getMainHandStack();
        if (!isDanielsPickaxe(tool)) return false;

        UUID uuid = player.getUuid();
        boolean tunnelMode = isAbilityActive(uuid);

        if (!tunnelMode) {
            // Instant break mode - no special handling needed, haste 10 handles it
            return false;
        }

        // Tunnel mode: 3x3 excavation + vein mining
        // Get the actual block face the player is looking at
        Direction blockFace = getBlockFace(player, world, pos);

        if (blockFace == Direction.UP || blockFace == Direction.DOWN) {
            blockFace = player.getHorizontalFacing();
        }

        // Break 3x3 area centered on the mined block
        break3x3Area(player, world, pos, blockFace, tool);

        // If it's an ore, also vein mine
        if (isOreBlock(state.getBlock())) {
            Set<BlockPos> visited = new HashSet<>();
            veinMineOre(player, world, pos, state.getBlock(), tool, visited);
        }

        return true;
    }
    
    /**
     * Raycast to find which block face the player is looking at.
     */
    private static Direction getBlockFace(ServerPlayerEntity player, ServerWorld world, BlockPos pos) {
        // Raycast from player's eye position
        net.minecraft.util.math.Vec3d eyePos = player.getEyePos();
        net.minecraft.util.math.Vec3d lookVec = player.getRotationVec(1.0f);
        net.minecraft.util.math.Vec3d endPos = eyePos.add(lookVec.multiply(6.0)); // 6 block range
        
        BlockHitResult hitResult = world.raycast(new RaycastContext(
            eyePos, endPos, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player
        ));
        
        if (hitResult.getType() == HitResult.Type.BLOCK && hitResult.getBlockPos().equals(pos)) {
            return hitResult.getSide();
        }
        
        // Fallback to player's horizontal facing if raycast fails
        return player.getHorizontalFacing();
    }

    private static void break3x3Area(ServerPlayerEntity player, ServerWorld world, BlockPos center, Direction blockFace, ItemStack tool) {
        // Break 3x3 flat area (tunnel cross-section) perpendicular to the block face being mined
        // This creates a 3 wide x 3 high tunnel that the player can walk through
        
        // Find perpendicular axes for the 3x3 plane
        int offsetX1, offsetY1, offsetZ1;  // First perpendicular direction (width)
        int offsetX2, offsetY2, offsetZ2;  // Second perpendicular direction (height)
        
        switch (blockFace) {
            case DOWN, UP -> {
                // Mining floor or ceiling: break in XZ plane (flat)
                offsetX1 = 1; offsetY1 = 0; offsetZ1 = 0;  // X axis (width)
                offsetX2 = 0; offsetY2 = 0; offsetZ2 = 1;  // Z axis (depth)
            }
            case NORTH, SOUTH -> {
                // Mining north/south wall: break in XY plane (flat)
                offsetX1 = 1; offsetY1 = 0; offsetZ1 = 0;  // X axis (width)
                offsetX2 = 0; offsetY2 = 1; offsetZ2 = 0;  // Y axis (height)
            }
            case WEST, EAST -> {
                // Mining west/east wall: break in YZ plane (flat)
                offsetX1 = 0; offsetY1 = 0; offsetZ1 = 1;  // Z axis (width)
                offsetX2 = 0; offsetY2 = 1; offsetZ2 = 0;  // Y axis (height)
            }
            default -> {
                // Fallback
                offsetX1 = 1; offsetY1 = 0; offsetZ1 = 0;
                offsetX2 = 0; offsetY2 = 1; offsetZ2 = 0;
            }
        }
        
        // Break 3x3 flat area (only d1 and d2, no d3 for above/below)
        for (int d1 = -1; d1 <= 1; d1++) {
            for (int d2 = -1; d2 <= 1; d2++) {
                BlockPos targetPos = center.add(
                    offsetX1 * d1 + offsetX2 * d2,
                    offsetY1 * d1 + offsetY2 * d2,
                    offsetZ1 * d1 + offsetZ2 * d2
                );

                BlockState targetState = world.getBlockState(targetPos);
                if (!targetState.isAir() && targetState.getBlock().getHardness() >= 0) {
                    // Break the block
                    breakBlockWithAutoSmelt(player, world, targetPos, targetState, tool);
                }
            }
        }
    }

    private static void veinMineOre(ServerPlayerEntity player, ServerWorld world, BlockPos start, Block oreBlock, ItemStack tool, Set<BlockPos> visited) {
        if (visited.size() >= 64) return; // Limit vein size
        if (visited.contains(start)) return;
        
        BlockState startState = world.getBlockState(start);
        if (!isOreBlock(startState.getBlock())) return;
        
        visited.add(start);

        // Check all 6 adjacent blocks and 20 diagonal blocks (full 3x3x3 minus corners)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue; // Skip center
                    
                    BlockPos neighbor = start.add(dx, dy, dz);
                    if (visited.contains(neighbor)) continue;

                    BlockState neighborState = world.getBlockState(neighbor);
                    Block neighborBlock = neighborState.getBlock();
                    
                    // Break if it's the same ore type or any other ore
                    if (isOreBlock(neighborBlock)) {
                        breakBlockWithAutoSmelt(player, world, neighbor, neighborState, tool);
                        veinMineOre(player, world, neighbor, neighborBlock, tool, visited);
                    }
                }
            }
        }
    }

    private static void breakBlockWithAutoSmelt(ServerPlayerEntity player, ServerWorld world, BlockPos pos, BlockState state, ItemStack tool) {
        Block block = state.getBlock();

        // Check for auto-smelt
        if (isAbilityActive(player.getUuid()) && SMELT_RESULTS.containsKey(block)) {
            // Drop the smelted result instead
            net.minecraft.item.Item smeltedResult = SMELT_RESULTS.get(block);
            if (smeltedResult != Items.AIR) {
                // Break the block and drop smelted item
                world.breakBlock(pos, false);
                net.minecraft.item.ItemStack drop = new net.minecraft.item.ItemStack(smeltedResult);
                Block.dropStack(world, pos, drop);

                // Particles for auto-smelt effect
                world.spawnParticles(net.minecraft.particle.ParticleTypes.FLAME,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        5, 0.3, 0.3, 0.3, 0.05);
                return;
            }
        }

        // Normal break
        world.breakBlock(pos, true);
    }

    private static boolean isOreBlock(Block block) {
        return ORE_BLOCKS.contains(block);
    }

    /**
     * Get the mining speed multiplier for the pickaxe.
     * Returns extremely high value when ability is OFF (instant break for ALL blocks).
     * Returns normal value when ability is ON.
     */
    public static float getMiningSpeedMultiplier(ServerPlayerEntity player, BlockState state) {
        ItemStack tool = player.getMainHandStack();
        if (!isDanielsPickaxe(tool)) return 1.0f;

        // Instant break mode: extreme speed for ALL breakable blocks
        if (!isAbilityActive(player.getUuid())) {
            // Check if it's a block that can be broken (not bedrock, etc.)
            if (state.getBlock().getHardness() < 0) {
                return 1.0f; // Unbreakable blocks can't be mined
            }
            return 10000.0f; // Extremely high speed = instant break for everything breakable
        }

        // Tunnel mode: normal speed (the 3x3 handles the rest)
        return 1.0f;
    }

    /**
     * Check if the pickaxe should insta-break the given block.
     */
    public static boolean shouldInstaBreak(ServerPlayerEntity player, BlockState state) {
        if (!isDanielsPickaxe(player.getMainHandStack())) return false;
        if (isAbilityActive(player.getUuid())) return false; // Tunnel mode doesn't insta-break
        
        // Insta-break any breakable block
        return state.getBlock().getHardness() >= 0;
    }

    /**
     * Check if an enchantment can be applied to Daniel's Pickaxe.
     * Blocks Efficiency, Silk Touch, and Mending.
     */
    public static boolean canApplyEnchantment(net.minecraft.registry.RegistryKey<net.minecraft.enchantment.Enchantment> enchantmentKey) {
        String id = enchantmentKey.getValue().getPath();
        // Block efficiency (already has hidden V), silk touch, and mending
        return !id.equals("efficiency") && !id.equals("silk_touch") && !id.equals("mending");
    }

    public static void onPlayerDisconnect(UUID uuid) {
        abilityActive.remove(uuid);
        lastHasteTime.remove(uuid);
    }
}
