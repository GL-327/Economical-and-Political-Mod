package com.political;


import com.political.bounty.SlayerItems;
import com.political.bounty.SlayerManager;
import com.political.items.CustomItemHandler;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;

import java.util.List;

public class BeamCraftingHandler {

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClientSide()) return InteractionResult.PASS;
            if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;

            BlockPos pos = hitResult.getBlockPos();
            if (!world.getBlockState(pos).is(Blocks.SMITHING_TABLE)) return InteractionResult.PASS;

            ItemStack heldItem = player.getItemInHand(hand);
            if (!CustomItemHandler.isAnyBeamWeapon(heldItem)) return InteractionResult.PASS;

            int tier = CustomItemHandler.getBeamTier(heldItem);
            if (tryBeamUpgrade(serverPlayer, tier)) {
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });
    }

    private static boolean tryBeamUpgrade(ServerPlayer player, int currentTier) {
        return switch (currentTier) {
            // Mk1 (tier 2) -> Mk2 (tier 3): 4 Iron Blocks + 2 Redstone Blocks
            case 2 -> tryStandardUpgrade(player, currentTier,
                    Items.IRON_BLOCK, 4, Items.REDSTONE_BLOCK, 2);
            // Mk2 (tier 3) -> Mk3 (tier 4): 4 Diamond Blocks + 4 Redstone Blocks
            case 3 -> tryStandardUpgrade(player, currentTier,
                    Items.DIAMOND_BLOCK, 4, Items.REDSTONE_BLOCK, 4);
            // Mk3 (tier 4) -> Mk4 (tier 5): 2 Netherite Ingots + 4 Glowstone
            case 4 -> tryStandardUpgrade(player, currentTier,
                    Items.NETHERITE_INGOT, 2, Items.GLOWSTONE, 4);
            // Mk4 (tier 5) -> Mk5 (tier 6): 4 Netherite Ingots + 8 Glowstone
            case 5 -> tryStandardUpgrade(player, currentTier,
                    Items.NETHERITE_INGOT, 4, Items.GLOWSTONE, 8);
            // Mk5 (tier 6) -> Ultra Overclocked (tier 7): 2 Warden Cores + 2 Nether Stars
            case 6 -> tryWardenCoreUpgrade(player);
            default -> false;
        };
    }

    private static boolean tryStandardUpgrade(ServerPlayer player, int currentTier,
                                               net.minecraft.world.item.Item mat1, int count1,
                                               net.minecraft.world.item.Item mat2, int count2) {
        if (countItem(player, mat1) < count1 || countItem(player, mat2) < count2) {
            player.sendSystemMessage(Component.literal("✗ Requires: " + count1 + "x "
                    + Component.translatable(mat1.getDescriptionId()).getString()
                    + " and " + count2 + "x " + Component.translatable(mat2.getDescriptionId()).getString())
                    .withStyle(ChatFormatting.RED), false);
            return false;
        }

        player.getMainHandItem().shrink(1);
        removeItem(player, mat1, count1);
        removeItem(player, mat2, count2);

        ItemStack upgraded = getUpgradedBeam(currentTier);
        if (upgraded != null) {
            if (!player.addItem(upgraded)) {
                player.drop(upgraded, false);
            }
            player.sendSystemMessage(Component.literal("✓ Upgraded to " + getBeamName(currentTier + 1) + "!")
                    .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), false);
        }
        return true;
    }

    private static boolean tryWardenCoreUpgrade(ServerPlayer player) {
        int cores = countWardenCores(player);
        int stars = countItem(player, Items.NETHER_STAR);

        if (cores < 2 || stars < 2) {
            player.sendSystemMessage(Component.literal("✗ Requires: 2x Warden's Core and 2x Nether Star")
                    .withStyle(ChatFormatting.RED), false);
            return false;
        }

        player.getMainHandItem().shrink(1);
        removeWardenCores(player, 2);
        removeItem(player, Items.NETHER_STAR, 2);

        ItemStack upgraded = CustomItemHandler.createUltraOverclockedBeam();
        if (!player.addItem(upgraded)) {
            player.drop(upgraded, false);
        }
        player.sendSystemMessage(Component.literal("✓ Upgraded to Ultra Overclocked HPEBM!")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), false);
        return true;
    }

    private static ItemStack getUpgradedBeam(int currentTier) {
        return switch (currentTier) {
            case 2 -> CustomItemHandler.createUltraBeamMk(2);
            case 3 -> CustomItemHandler.createUltraBeamMk(3);
            case 4 -> CustomItemHandler.createUltraBeamMk(4);
            case 5 -> CustomItemHandler.createUltraBeamMk(5);
            default -> null;
        };
    }

    private static String getBeamName(int tier) {
        return switch (tier) {
            case 3 -> "HPEBM Mk2";
            case 4 -> "HPEBM Mk3";
            case 5 -> "HPEBM Mk4";
            case 6 -> "HPEBM Mk5";
            case 7 -> "Ultra Overclocked HPEBM";
            default -> "HPEBM Mk" + (tier - 1);
        };
    }

    private static int countItem(ServerPlayer player, net.minecraft.world.item.Item item) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) count += stack.getCount();
        }
        return count;
    }

    private static int countWardenCores(ServerPlayer player) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (CustomItemHandler.isWardenCore(player.getInventory().getItem(i))) count++;
        }
        return count;
    }

    private static void removeItem(ServerPlayer player, net.minecraft.world.item.Item item, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                int toRemove = Math.min(remaining, stack.getCount());
                stack.shrink(toRemove);
                remaining -= toRemove;
            }
        }
    }

    private static void removeWardenCores(ServerPlayer player, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (CustomItemHandler.isWardenCore(stack)) {
                stack.shrink(1);
                remaining--;
            }
        }
    }

    public static ItemStack checkBeamRecipe(List<ItemStack> grid) {
        if (grid.size() != 9) return null;

        ItemStack center = grid.get(4);

        // Center must be exactly 1 beam weapon
        if (!CustomItemHandler.isAnyBeamWeapon(center) || center.getCount() != 1) return null;

        int currentTier = CustomItemHandler.getBeamTier(center);

        // ═══════════════════════════════════════════════════════════════
        // TIER 7 RECIPE: Ultra Overclocked (from Mk5)
        // [Warden Core] [Warden Core] [Warden Core]
        // [Warden Core] [   Mk5     ] [Warden Core]
        // [Nether Star] [Nether Star] [Nether Star]
        // ═══════════════════════════════════════════════════════════════
        if (currentTier == 6) { // Mk5 -> Ultra Overclocked
            boolean validUltraOverclocked =
                    CustomItemHandler.isWardenCore(grid.get(0)) && grid.get(0).getCount() == 1 &&
                            CustomItemHandler.isWardenCore(grid.get(1)) && grid.get(1).getCount() == 1 &&
                            CustomItemHandler.isWardenCore(grid.get(2)) && grid.get(2).getCount() == 1 &&
                            CustomItemHandler.isWardenCore(grid.get(3)) && grid.get(3).getCount() == 1 &&
                            CustomItemHandler.isWardenCore(grid.get(5)) && grid.get(5).getCount() == 1 &&
                            grid.get(6).is(Items.NETHER_STAR) && grid.get(6).getCount() == 1 &&
                            grid.get(7).is(Items.NETHER_STAR) && grid.get(7).getCount() == 1 &&
                            grid.get(8).is(Items.NETHER_STAR) && grid.get(8).getCount() == 1;

            if (validUltraOverclocked) {
                return CustomItemHandler.createUltraOverclockedBeam();
            }
        }

        // ═══════════════════════════════════════════════════════════════
        // STANDARD UPGRADE RECIPE (Tiers 0-5)
        // [Redstone Block] [Warden Core]   [Redstone Block]
        // [Glowstone]      [   Beam    ]   [Glowstone]
        // [Redstone Block] [Dragon Brth]   [Redstone Block]
        // ═══════════════════════════════════════════════════════════════
        ItemStack topMid = grid.get(1);
        ItemStack botMid = grid.get(7);

        // Top middle must be exactly 1 Warden's Core
        if (!CustomItemHandler.isWardenCore(topMid) || topMid.getCount() != 1) return null;

        // Bottom middle must be exactly 1 Dragon's Breath
        if (!botMid.is(Items.DRAGON_BREATH) || botMid.getCount() != 1) return null;

        // Corner slots must each have exactly 1 Redstone Block
        int[] cornerSlots = {0, 2, 6, 8};
        for (int slot : cornerSlots) {
            ItemStack stack = grid.get(slot);
            if (!stack.is(Items.REDSTONE_BLOCK) || stack.getCount() != 1) {
                return null;
            }
        }

        // Side middle slots must each have exactly 1 Glowstone
        int[] sideSlots = {3, 5};
        for (int slot : sideSlots) {
            ItemStack stack = grid.get(slot);
            if (!stack.is(Items.GLOWSTONE) || stack.getCount() != 1) {
                return null;
            }
        }

        return switch (currentTier) {
            case 0 -> CustomItemHandler.createUltraBeam();      // HPEBM -> Ultra
            case 1 -> CustomItemHandler.createUltraBeamMk(1);   // Ultra -> Mk1
            case 2 -> CustomItemHandler.createUltraBeamMk(2);   // Mk1 -> Mk2
            case 3 -> CustomItemHandler.createUltraBeamMk(3);   // Mk2 -> Mk3
            case 4 -> CustomItemHandler.createUltraBeamMk(4);   // Mk3 -> Mk4
            case 5 -> CustomItemHandler.createUltraBeamMk(5);   // Mk4 -> Mk5
            default -> null;
        };
    }

    public static int getRequiredStars(int currentTier) {
        if (currentTier == 6) return 3; // Ultra Overclocked uses 3 Nether Stars
        return 0; // Standard upgrade (tiers 0-5) does not use Nether Stars
    }

    public static int getRequiredBreath(int currentTier) {
        if (currentTier == 6) return 0; // Ultra Overclocked does not use Dragon's Breath
        return 1; // Standard upgrade uses 1 Dragon's Breath
    }

    public static int getRequiredWardenCores(int currentTier) {
        if (currentTier == 6) return 5; // Ultra Overclocked uses 5 cores
        return 1; // Standard upgrade uses 1
    }

    public static boolean tryZombieBerserkerHelmetCraft(ServerPlayer player, ItemStack[] craftingGrid) {
        // Recipe: 5 Zombie Cores + 1 Iron Helmet
        // [Core] [Core] [Core]
        // [Core] [Helmet] [Core]
        // [    ] [    ] [    ]

        int coreCount = 0;
        boolean hasHelmet = false;

        // Check positions
        int[] corePositions = {0, 1, 2, 3, 5}; // Top row + sides of middle
        int helmetPosition = 4; // Center
        int[] emptyPositions = {6, 7, 8}; // Bottom row

        for (int i = 0; i < 9; i++) {
            ItemStack stack = craftingGrid[i];

            if (contains(corePositions, i)) {
                if (SlayerItems.isSlayerCore(stack) &&
                        SlayerItems.getCoreType(stack) == SlayerManager.SlayerType.ZOMBIE) {
                    coreCount++;
                } else if (!stack.isEmpty()) {
                    return false; // Wrong item in core slot
                }
            } else if (i == helmetPosition) {
                if (stack.is(Items.IRON_HELMET)) {
                    hasHelmet = true;
                } else if (!stack.isEmpty()) {
                    return false; // Wrong item in helmet slot
                }
            } else if (contains(emptyPositions, i)) {
                if (!stack.isEmpty()) {
                    return false; // Should be empty
                }
            }
        }

        return coreCount == 5 && hasHelmet;
    }

    private static boolean contains(int[] arr, int value) {
        for (int i : arr) {
            if (i == value) return true;
        }
        return false;
    }

    public static ItemStack getZombieBerserkerHelmetResult() {
        return SlayerItems.createZombieBerserkerHelmet();
    }
}
