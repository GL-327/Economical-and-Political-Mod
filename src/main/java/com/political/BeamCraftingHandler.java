package com.political;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class BeamCraftingHandler {

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;

            BlockPos pos = hitResult.getBlockPos();
            if (!world.getBlockState(pos).isOf(Blocks.SMITHING_TABLE)) return ActionResult.PASS;

            ItemStack heldItem = player.getStackInHand(hand);
            if (!CustomItemHandler.isAnyBeamWeapon(heldItem)) return ActionResult.PASS;

            int tier = CustomItemHandler.getBeamTier(heldItem);
            if (tryBeamUpgrade(serverPlayer, tier)) {
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });
    }

    private static boolean tryBeamUpgrade(ServerPlayerEntity player, int currentTier) {
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

    private static boolean tryStandardUpgrade(ServerPlayerEntity player, int currentTier,
                                               net.minecraft.item.Item mat1, int count1,
                                               net.minecraft.item.Item mat2, int count2) {
        if (countItem(player, mat1) < count1 || countItem(player, mat2) < count2) {
            player.sendMessage(Text.literal("✗ Requires: " + count1 + "x " + mat1.getName().getString()
                    + " and " + count2 + "x " + mat2.getName().getString()).formatted(Formatting.RED), false);
            return false;
        }

        player.getMainHandStack().decrement(1);
        removeItem(player, mat1, count1);
        removeItem(player, mat2, count2);

        ItemStack upgraded = getUpgradedBeam(currentTier);
        if (upgraded != null) {
            if (!player.getInventory().insertStack(upgraded)) {
                player.dropItem(upgraded, false);
            }
            player.sendMessage(Text.literal("✓ Upgraded to " + getBeamName(currentTier + 1) + "!")
                    .formatted(Formatting.AQUA, Formatting.BOLD), false);
        }
        return true;
    }

    private static boolean tryWardenCoreUpgrade(ServerPlayerEntity player) {
        int cores = countWardenCores(player);
        int stars = countItem(player, Items.NETHER_STAR);

        if (cores < 2 || stars < 2) {
            player.sendMessage(Text.literal("✗ Requires: 2x Warden's Core and 2x Nether Star")
                    .formatted(Formatting.RED), false);
            return false;
        }

        player.getMainHandStack().decrement(1);
        removeWardenCores(player, 2);
        removeItem(player, Items.NETHER_STAR, 2);

        ItemStack upgraded = CustomItemHandler.createUltraOverclockedBeam();
        if (!player.getInventory().insertStack(upgraded)) {
            player.dropItem(upgraded, false);
        }
        player.sendMessage(Text.literal("✓ Upgraded to Ultra Overclocked HPEBM!")
                .formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD), false);
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

    private static int countItem(ServerPlayerEntity player, net.minecraft.item.Item item) {
        int count = 0;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isOf(item)) count += stack.getCount();
        }
        return count;
    }

    private static int countWardenCores(ServerPlayerEntity player) {
        int count = 0;
        for (int i = 0; i < player.getInventory().size(); i++) {
            if (CustomItemHandler.isWardenCore(player.getInventory().getStack(i))) count++;
        }
        return count;
    }

    private static void removeItem(ServerPlayerEntity player, net.minecraft.item.Item item, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().size() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isOf(item)) {
                int toRemove = Math.min(remaining, stack.getCount());
                stack.decrement(toRemove);
                remaining -= toRemove;
            }
        }
    }

    private static void removeWardenCores(ServerPlayerEntity player, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().size() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (CustomItemHandler.isWardenCore(stack)) {
                stack.decrement(1);
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
                            grid.get(6).isOf(Items.NETHER_STAR) && grid.get(6).getCount() == 1 &&
                            grid.get(7).isOf(Items.NETHER_STAR) && grid.get(7).getCount() == 1 &&
                            grid.get(8).isOf(Items.NETHER_STAR) && grid.get(8).getCount() == 1;

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
        if (!botMid.isOf(Items.DRAGON_BREATH) || botMid.getCount() != 1) return null;

        // Corner slots must each have exactly 1 Redstone Block
        int[] cornerSlots = {0, 2, 6, 8};
        for (int slot : cornerSlots) {
            ItemStack stack = grid.get(slot);
            if (!stack.isOf(Items.REDSTONE_BLOCK) || stack.getCount() != 1) {
                return null;
            }
        }

        // Side middle slots must each have exactly 1 Glowstone
        int[] sideSlots = {3, 5};
        for (int slot : sideSlots) {
            ItemStack stack = grid.get(slot);
            if (!stack.isOf(Items.GLOWSTONE) || stack.getCount() != 1) {
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

    public static boolean tryZombieBerserkerHelmetCraft(ServerPlayerEntity player, ItemStack[] craftingGrid) {
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
                if (stack.isOf(Items.IRON_HELMET)) {
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
