package com.political;


import com.political.core.Clock;
import com.political.bounty.SlayerItems;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.ClipContext;

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
    private static final Identifier MINING_SPEED_MODIFIER = Identifier.fromNamespaceAndPath("political", "daniels_mining_speed");

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
    private static final Map<Block, net.minecraft.world.item.Item> SMELT_RESULTS = new HashMap<>();
    static {
        SMELT_RESULTS.put(Blocks.IRON_ORE, Items.IRON_INGOT);
        SMELT_RESULTS.put(Blocks.DEEPSLATE_IRON_ORE, Items.IRON_INGOT);
        SMELT_RESULTS.put(Blocks.GOLD_ORE, Items.GOLD_INGOT);
        SMELT_RESULTS.put(Blocks.DEEPSLATE_GOLD_ORE, Items.GOLD_INGOT);
        SMELT_RESULTS.put(Blocks.COPPER_ORE, Items.COPPER_INGOT);
        SMELT_RESULTS.put(Blocks.DEEPSLATE_COPPER_ORE, Items.COPPER_INGOT);
        SMELT_RESULTS.put(Blocks.NETHER_GOLD_ORE, Items.GOLD_INGOT);
    }

    public static ItemStack create() {
        // Start with diamond pickaxe for insta-mine mode (base)
        ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);

        CompoundTag nbt = new CompoundTag();
        nbt.putByte(TAG_DANIELS_PICKAXE, (byte) 1);
        nbt.putByte(TAG_MODE, (byte) 0); // 0 = insta mode
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));

        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Daniel's Pickaxe").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));

        stack.set(DataComponents.LORE, new ItemLore(Arrays.asList(
                Component.literal(""),
                Component.literal("◆ LEGENDARY MINING TOOL ◆").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD),
                Component.literal(""),
                Component.literal("Right-click: Toggle Mining Mode").withStyle(ChatFormatting.YELLOW),
                Component.literal(""),
                Component.literal("Mode: INSTANT BREAK").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD),
                Component.literal("  └ Breaks any block instantly").withStyle(ChatFormatting.GRAY),
                Component.literal("  └ Haste LX for extreme speed").withStyle(ChatFormatting.GRAY),
                Component.literal("  └ Diamond Pickaxe").withStyle(ChatFormatting.GRAY),
                Component.literal(""),
                Component.literal("§7Toggle for TUNNEL MODE:").withStyle(ChatFormatting.GRAY),
                Component.literal("  └ 3x3 tunnel mining").withStyle(ChatFormatting.GRAY),
                Component.literal("  └ Vein mining for ores").withStyle(ChatFormatting.GRAY),
                Component.literal("  └ Auto-smelts ores").withStyle(ChatFormatting.GRAY),
                Component.literal("  └ Netherite Pickaxe").withStyle(ChatFormatting.GRAY),
                Component.literal(""),
                Component.literal("Unbreakable").withStyle(ChatFormatting.GREEN),
                Component.literal("§8Cannot apply Efficiency, Silk Touch, or Mending").withStyle(ChatFormatting.DARK_GRAY),
                Component.literal(""),
                Component.literal("§8[Forged in the depths of determination]").withStyle(ChatFormatting.DARK_GRAY),
                Component.literal("「Legendary」").withStyle(ChatFormatting.DARK_PURPLE)
        )));

        // Make unbreakable (very high durability)
        stack.set(DataComponents.MAX_DAMAGE, Integer.MAX_VALUE);

        // Add Fortune V and Efficiency V enchantments via NBT
        CompoundTag display = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.of(new CompoundTag())).copyTag();
        
        // Store enchantments in custom data for the pickaxe
        CompoundTag enchantNbt = new CompoundTag();
        enchantNbt.putInt("fortune", 5);
        enchantNbt.putInt("efficiency", 5);
        display.put("StoredEnchantments", enchantNbt);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(display));

        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(stack, "daniels_pickaxe");

        return stack;
    }

    public static boolean isDanielsPickaxe(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.of(new CompoundTag())).copyTag();
        return nbt.contains(TAG_DANIELS_PICKAXE);
    }

    public static boolean isAbilityActive(UUID playerUuid) {
        return abilityActive.getOrDefault(playerUuid, false);
    }

    public static boolean isAbilityActive(ServerPlayer player) {
        return isAbilityActive(player.getUUID());
    }

    public static void toggleAbility(ServerPlayer player) {
        UUID uuid = player.getUUID();

        long now = Clock.gameMillis();
        long last = lastToggleTime.getOrDefault(uuid, 0L);
        if (now - last < 5000) {
            player.sendSystemMessage(Component.literal("⏳ Please wait before switching modes again").withStyle(ChatFormatting.RED));
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
            player.sendSystemMessage(
                    Component.literal("⛏ TUNNEL MODE ACTIVATED").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
            );
            player.level().playSound(
                    null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.0f, 1.5f
            );
        } else {
            player.sendSystemMessage(
                    Component.literal("⛏ INSTANT BREAK MODE ACTIVATED").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)
            );
            player.level().playSound(
                    null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0f, 1.0f
            );
        }
    }

    /**
     * Switch the pickaxe item type based on mode.
     * Tunnel mode = Netherite pickaxe (for 3x3 mining)
     * Instant break mode = Diamond pickaxe (with haste 10)
     */
    private static void switchPickaxeType(ServerPlayer player, boolean tunnelMode) {
        ItemStack mainHand = player.getMainHandItem();
        if (!isDanielsPickaxe(mainHand)) return;

        // Get the NBT data to preserve
        CompoundTag nbt = mainHand.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.of(new CompoundTag())).copyTag();
        nbt.putByte(TAG_MODE, tunnelMode ? (byte) 1 : (byte) 0);

        // Create new pickaxe based on mode
        ItemStack newPickaxe;
        if (tunnelMode) {
            newPickaxe = new ItemStack(Items.NETHERITE_PICKAXE);
        } else {
            newPickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
        }

        // Copy all data to new pickaxe
        newPickaxe.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        newPickaxe.set(DataComponents.CUSTOM_NAME, mainHand.get(DataComponents.CUSTOM_NAME));
        newPickaxe.set(DataComponents.MAX_DAMAGE, Integer.MAX_VALUE);
        newPickaxe.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        
        // Copy lore (will be updated separately)
        newPickaxe.set(DataComponents.LORE, mainHand.get(DataComponents.LORE));

        // Copy custom item ID
        SlayerItems.setCustomItemId(newPickaxe, "daniels_pickaxe");

        // Replace in main hand using the proper vanilla method
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, newPickaxe);
    }

    private static void updateItemLore(ServerPlayer player, boolean abilityActive) {
        ItemStack mainHand = player.getMainHandItem();
        if (!isDanielsPickaxe(mainHand)) return;
        updateItemLoreOnStack(mainHand, abilityActive);
    }

    /**
     * Update lore on a specific ItemStack (used when switching types).
     */
    private static void updateItemLoreOnStack(ItemStack stack, boolean tunnelMode) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ LEGENDARY MINING TOOL ◆").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Right-click: Toggle Mining Mode").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal(""));

        if (tunnelMode) {
            lore.add(Component.literal("Mode: TUNNEL MINING").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            lore.add(Component.literal("  └ 3x3 tunnel excavation").withStyle(ChatFormatting.GRAY));
            lore.add(Component.literal("  └ Vein mining ores").withStyle(ChatFormatting.GRAY));
            lore.add(Component.literal("  └ Auto-smelts ores").withStyle(ChatFormatting.GRAY));
            lore.add(Component.literal("  └ Netherite Pickaxe").withStyle(ChatFormatting.GRAY));
        } else {
            lore.add(Component.literal("Mode: INSTANT BREAK").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
            lore.add(Component.literal("  └ Breaks any block instantly").withStyle(ChatFormatting.GRAY));
            lore.add(Component.literal("  └ Haste LX for extreme speed").withStyle(ChatFormatting.GRAY));
            lore.add(Component.literal("  └ Diamond Pickaxe").withStyle(ChatFormatting.GRAY));
        }

        lore.add(Component.literal(""));
        lore.add(Component.literal("Unbreakable").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[Forged in the depths of determination]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("「Legendary」").withStyle(ChatFormatting.DARK_PURPLE));

        stack.set(DataComponents.LORE, new ItemLore(lore));
        CustomItemLore.applySummarizedLore(stack);
    }

    /**
     * Called every tick to refresh haste effect in insta-mine mode.
     * Should be called from a player tick event.
     */
    public static void tickPlayer(ServerPlayer player) {
        if (!isAbilityActive(player)) {
            // Insta-mine mode - apply Haste 5
            long currentTime = Clock.gameMillis();
            long lastTime = lastHasteTime.getOrDefault(player.getUUID(), 0L);
            
            // Refresh every 1000ms (1 second), duration 40 ticks (2 seconds)
            if (currentTime - lastTime >= 1000) {
                if (isDanielsPickaxe(player.getMainHandItem())) {
                    player.addEffect(new MobEffectInstance(
                        MobEffects.HASTE, 40, 59, true, false, false)); // Haste 60 (level 59)
                    lastHasteTime.put(player.getUUID(), currentTime);
                }
            }
        }
    }

    /**
     * Called when a block is broken with Daniel's Pickaxe.
     * Returns true if the break should be handled specially (3x3/vein mining).
     */
    public static boolean onBlockBreak(ServerPlayer player, ServerLevel world, BlockPos pos, BlockState state) {
        ItemStack tool = player.getMainHandItem();
        if (!isDanielsPickaxe(tool)) return false;

        UUID uuid = player.getUUID();
        boolean tunnelMode = isAbilityActive(uuid);

        if (!tunnelMode) {
            // Instant break mode - no special handling needed, haste 10 handles it
            return false;
        }

        // Tunnel mode: 3x3 excavation + vein mining
        // Get the actual block face the player is looking at
        Direction blockFace = getBlockFace(player, world, pos);

        if (blockFace == Direction.UP || blockFace == Direction.DOWN) {
            blockFace = Direction.fromYRot(player.getYRot());
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
    private static Direction getBlockFace(ServerPlayer player, ServerLevel world, BlockPos pos) {
        // Raycast from player's eye position
        net.minecraft.world.phys.Vec3 eyePos = player.getEyePosition();
        net.minecraft.world.phys.Vec3 lookVec = player.getViewVector(1.0f);
        net.minecraft.world.phys.Vec3 endPos = eyePos.add(lookVec.scale(6.0)); // 6 block range
        
        BlockHitResult hitResult = world.clip(new ClipContext(
            eyePos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player
        ));
        
        if (hitResult.getType() == HitResult.Type.BLOCK && hitResult.getBlockPos().equals(pos)) {
            return hitResult.getDirection();
        }
        
        // Fallback to player's horizontal facing if raycast fails
        return Direction.fromYRot(player.getYRot());
    }

    private static void break3x3Area(ServerPlayer player, ServerLevel world, BlockPos center, Direction blockFace, ItemStack tool) {
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
                BlockPos targetPos = center.offset(
                    offsetX1 * d1 + offsetX2 * d2,
                    offsetY1 * d1 + offsetY2 * d2,
                    offsetZ1 * d1 + offsetZ2 * d2
                );

                BlockState targetState = world.getBlockState(targetPos);
                if (!targetState.isAir() && targetState.getDestroySpeed(world, targetPos) >= 0.0f) {
                    // Break the block
                    breakBlockWithAutoSmelt(player, world, targetPos, targetState, tool);
                }
            }
        }
    }

    private static void veinMineOre(ServerPlayer player, ServerLevel world, BlockPos start, Block oreBlock, ItemStack tool, Set<BlockPos> visited) {
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
                    
                    BlockPos neighbor = start.offset(dx, dy, dz);
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

    private static void breakBlockWithAutoSmelt(ServerPlayer player, ServerLevel world, BlockPos pos, BlockState state, ItemStack tool) {
        Block block = state.getBlock();

        // Always break without world drops, then route drops into inventory (fallback: ground).
        world.destroyBlock(pos, false, null, 512);

        // Auto-smelt in tunnel mode for mapped ores.
        if (isAbilityActive(player.getUUID()) && SMELT_RESULTS.containsKey(block)) {
            net.minecraft.world.item.Item smeltedResult = SMELT_RESULTS.get(block);
            if (smeltedResult != Items.AIR) {
                ItemStack drop = new ItemStack(smeltedResult);
                if (!player.addItem(drop)) {
                    Block.popResource(world, pos, drop);
                }
                world.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        3, 0.3, 0.3, 0.3, 0.05);
                return;
            }
        }

        // Normal drops routed to inventory.
        var drops = Block.getDrops(state, world, pos, null, player, tool);
        for (ItemStack drop : drops) {
            if (!player.addItem(drop)) {
                Block.popResource(world, pos, drop);
            }
        }
    }

    private static boolean isOreBlock(Block block) {
        return ORE_BLOCKS.contains(block);
    }

    /**
     * Get the mining speed multiplier for the pickaxe.
     * Returns extremely high value when ability is OFF (instant break for ALL blocks).
     * Returns normal value when ability is ON.
     */
    public static float getMiningSpeedMultiplier(ServerPlayer player, BlockState state) {
        ItemStack tool = player.getMainHandItem();
        if (!isDanielsPickaxe(tool)) return 1.0f;

        // Instant break mode: extreme speed for ALL breakable blocks
        if (!isAbilityActive(player.getUUID())) {
            // Check if it's a block that can be broken (not bedrock, etc.)
            if (state.getDestroySpeed(player.level(), player.blockPosition()) < 0.0f) {
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
    public static boolean shouldInstaBreak(ServerPlayer player, BlockState state) {
        if (!isDanielsPickaxe(player.getMainHandItem())) return false;
        if (isAbilityActive(player.getUUID())) return false; // Tunnel mode doesn't insta-break
        
        // Insta-break any breakable block
        return state.getDestroySpeed(player.level(), player.blockPosition()) >= 0.0f;
    }

    /**
     * Check if an enchantment can be applied to Daniel's Pickaxe.
     * Blocks Efficiency, Silk Touch, and Mending.
     */
    public static boolean canApplyEnchantment(net.minecraft.resources.ResourceKey<net.minecraft.world.item.enchantment.Enchantment> enchantmentKey) {
        String id = enchantmentKey.identifier().getPath();
        // Block efficiency (already has hidden V), silk touch, and mending
        return !id.equals("efficiency") && !id.equals("silk_touch") && !id.equals("mending");
    }

    public static void onPlayerDisconnect(UUID uuid) {
        abilityActive.remove(uuid);
        lastHasteTime.remove(uuid);
    }
}
