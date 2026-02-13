package com.political;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public class BountyCraftingHandler {

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;

            BlockPos pos = hitResult.getBlockPos();

            if (!world.getBlockState(pos).isOf(Blocks.CRAFTING_TABLE)) return ActionResult.PASS;

            // === ARMOR RECIPES ===
            if (tryZombieBerserkerHelmetRecipe(serverPlayer)) return ActionResult.SUCCESS;
            if (trySpiderLeggingsRecipe(serverPlayer)) return ActionResult.SUCCESS;
            if (trySkeletonBowRecipe(serverPlayer)) return ActionResult.SUCCESS;
            if (trySlimeBootsRecipe(serverPlayer)) return ActionResult.SUCCESS;
            if (tryWardenChestplateRecipe(serverPlayer)) return ActionResult.SUCCESS;

            // === SWORD RECIPES (T1 & T2) ===
            if (tryBountySwordRecipe(serverPlayer)) return ActionResult.SUCCESS;
            if (tryUpgradedSwordRecipe(serverPlayer)) return ActionResult.SUCCESS;

            // === WEAPON RECIPES ===
            if (tryGavelRecipe(serverPlayer)) return ActionResult.SUCCESS;
            if (tryBaseHPEBMRecipe(serverPlayer)) return ActionResult.SUCCESS;

            return ActionResult.PASS;
        });
    }

    // ============================================================
    // ZOMBIE BERSERKER HELMET
    // Recipe: 5 Zombie Cores + 1 Zombie Head + 3 Rotten Flesh
    // ============================================================
    private static boolean tryZombieBerserkerHelmetRecipe(ServerPlayerEntity player) {
        int zombieCores = countCores(player, "Zombie Core");
        int rottenFlesh = countItem(player, Items.ROTTEN_FLESH);
        boolean hasZombieHead = hasItem(player, Items.ZOMBIE_HEAD);

        if (zombieCores < 5 || rottenFlesh < 3 || !hasZombieHead) return false;

        int level = SlayerData.getSlayerLevel(player.getUuidAsString(), SlayerManager.SlayerType.ZOMBIE);
        if (level < SlayerItems.ZOMBIE_BERSERKER_LEVEL_REQ) {
            player.sendMessage(Text.literal("✗ Requires Zombie Bounty Level " + SlayerItems.ZOMBIE_BERSERKER_LEVEL_REQ)
                    .formatted(Formatting.RED), false);
            return false;
        }

        removeCores(player, "Zombie Core", 5);
        removeItem(player, Items.ROTTEN_FLESH, 3);
        removeItem(player, Items.ZOMBIE_HEAD, 1);

        player.getInventory().insertStack(SlayerItems.createZombieBerserkerHelmet());
        player.sendMessage(Text.literal("✓ Crafted Zombie Berserker Helmet!")
                .formatted(Formatting.GOLD, Formatting.BOLD), false);

        return true;
    }

    // ============================================================
    // SPIDER LEGGINGS
    // Recipe: 5 Spider Cores + 8 String + 1 Spider Eye
    // ============================================================
    private static boolean trySpiderLeggingsRecipe(ServerPlayerEntity player) {
        int spiderCores = countCores(player, "Spider Core");
        int string = countItem(player, Items.STRING);
        boolean hasSpiderEye = hasItem(player, Items.SPIDER_EYE);

        if (spiderCores < 5 || string < 8 || !hasSpiderEye) return false;

        int level = SlayerData.getSlayerLevel(player.getUuidAsString(), SlayerManager.SlayerType.SPIDER);
        if (level < SlayerItems.SPIDER_LEGGINGS_LEVEL_REQ) {
            player.sendMessage(Text.literal("✗ Requires Spider Bounty Level " + SlayerItems.SPIDER_LEGGINGS_LEVEL_REQ)
                    .formatted(Formatting.RED), false);
            return false;
        }

        removeCores(player, "Spider Core", 5);
        removeItem(player, Items.STRING, 8);
        removeItem(player, Items.SPIDER_EYE, 1);

        player.getInventory().insertStack(SlayerItems.createSpiderLeggings());
        player.sendMessage(Text.literal("✓ Crafted Venomous Crawler Leggings!")
                .formatted(Formatting.DARK_RED, Formatting.BOLD), false);

        return true;
    }

    // ============================================================
    // SKELETON BOW
    // Recipe: 5 Skeleton Cores + 1 Bow + 3 Bones
    // ============================================================
    private static boolean trySkeletonBowRecipe(ServerPlayerEntity player) {
        int skeletonCores = countCores(player, "Skeleton Core");
        int bones = countItem(player, Items.BONE);
        boolean hasBow = hasItem(player, Items.BOW);

        if (skeletonCores < 5 || bones < 3 || !hasBow) return false;

        int level = SlayerData.getSlayerLevel(player.getUuidAsString(), SlayerManager.SlayerType.SKELETON);
        if (level < SlayerItems.SKELETON_BOW_LEVEL_REQ) {
            player.sendMessage(Text.literal("✗ Requires Skeleton Bounty Level " + SlayerItems.SKELETON_BOW_LEVEL_REQ)
                    .formatted(Formatting.RED), false);
            return false;
        }

        removeCores(player, "Skeleton Core", 5);
        removeItem(player, Items.BONE, 3);
        removeItem(player, Items.BOW, 1);

        player.getInventory().insertStack(SlayerItems.createSkeletonBow());
        player.sendMessage(Text.literal("✓ Crafted Bone Desperado's Longbow!")
                .formatted(Formatting.WHITE, Formatting.BOLD), false);

        return true;
    }

    // ============================================================
    // SLIME BOOTS
    // Recipe: 5 Slime Cores + 8 Slime Balls + 1 Leather Boots
    // ============================================================
    private static boolean trySlimeBootsRecipe(ServerPlayerEntity player) {
        int slimeCores = countCores(player, "Slime Core");
        int slimeBalls = countItem(player, Items.SLIME_BALL);
        boolean hasBoots = hasItem(player, Items.LEATHER_BOOTS);

        if (slimeCores < 5 || slimeBalls < 8 || !hasBoots) return false;

        int level = SlayerData.getSlayerLevel(player.getUuidAsString(), SlayerManager.SlayerType.SLIME);
        if (level < SlayerItems.SLIME_BOOTS_LEVEL_REQ) {
            player.sendMessage(Text.literal("✗ Requires Slime Bounty Level " + SlayerItems.SLIME_BOOTS_LEVEL_REQ)
                    .formatted(Formatting.RED), false);
            return false;
        }

        removeCores(player, "Slime Core", 5);
        removeItem(player, Items.SLIME_BALL, 8);
        removeItem(player, Items.LEATHER_BOOTS, 1);

        player.getInventory().insertStack(SlayerItems.createSlimeBoots());
        player.sendMessage(Text.literal("✓ Crafted Gelatinous Rustler Boots!")
                .formatted(Formatting.GREEN, Formatting.BOLD), false);

        return true;
    }

    // ============================================================
    // WARDEN CHESTPLATE
    // Recipe: 8 Warden Cores + 1 Netherite Chestplate + 4 Echo Shards
    // ============================================================
    private static boolean tryWardenChestplateRecipe(ServerPlayerEntity player) {
        int wardenCores = countCores(player, "Warden Core");
        int echoShards = countItem(player, Items.ECHO_SHARD);
        boolean hasChestplate = hasItem(player, Items.NETHERITE_CHESTPLATE);

        if (wardenCores < 8 || echoShards < 4 || !hasChestplate) return false;

        int level = SlayerData.getSlayerLevel(player.getUuidAsString(), SlayerManager.SlayerType.WARDEN);
        if (level < SlayerItems.WARDEN_CHESTPLATE_LEVEL_REQ) {
            player.sendMessage(Text.literal("✗ Requires Warden Bounty Level " + SlayerItems.WARDEN_CHESTPLATE_LEVEL_REQ)
                    .formatted(Formatting.RED), false);
            return false;
        }

        removeCores(player, "Warden Core", 8);
        removeItem(player, Items.ECHO_SHARD, 4);
        removeItem(player, Items.NETHERITE_CHESTPLATE, 1);

        player.getInventory().insertStack(SlayerItems.createWardenChestplate());
        player.sendMessage(Text.literal("✓ Crafted Sculk Terror Chestplate!")
                .formatted(Formatting.DARK_AQUA, Formatting.BOLD), false);

        return true;
    }

    // ============================================================
    // BOUNTY SWORDS (T1) - All Types
    // Recipe: 3 Cores + 1 Iron Sword + 2 Sticks
    // ============================================================
    private static boolean tryBountySwordRecipe(ServerPlayerEntity player) {
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            String coreName = type.displayName + " Core";
            int cores = countCores(player, coreName);
            boolean hasSword = hasItem(player, Items.IRON_SWORD);
            int sticks = countItem(player, Items.STICK);

            if (cores >= 3 && hasSword && sticks >= 2) {
                int level = SlayerData.getSlayerLevel(player.getUuidAsString(), type);
                if (level < SlayerItems.BASIC_SWORD_LEVEL_REQ) {
                    player.sendMessage(Text.literal("✗ Requires " + type.displayName + " Bounty Level " + SlayerItems.BASIC_SWORD_LEVEL_REQ)
                            .formatted(Formatting.RED), false);
                    return false;
                }

                removeCores(player, coreName, 3);
                removeItem(player, Items.IRON_SWORD, 1);
                removeItem(player, Items.STICK, 2);

                player.getInventory().insertStack(SlayerItems.createSlayerSword(type));
                player.sendMessage(Text.literal("✓ Crafted " + type.displayName + " Bounty Sword!")
                        .formatted(type.color, Formatting.BOLD), false);
                return true;
            }
        }
        return false;
    }

    // ============================================================
    // UPGRADED SLAYER SWORDS (T2) - All Types
    // Recipe: 5 Cores + 1 Diamond Sword + 1 T1 Bounty Sword
    // ============================================================
    private static boolean tryUpgradedSwordRecipe(ServerPlayerEntity player) {
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            String coreName = type.displayName + " Core";
            int cores = countCores(player, coreName);
            boolean hasDiamondSword = hasItem(player, Items.DIAMOND_SWORD);
            boolean hasT1Sword = hasSlayerSword(player, type);

            if (cores >= 5 && hasDiamondSword && hasT1Sword) {
                int level = SlayerData.getSlayerLevel(player.getUuidAsString(), type);
                if (level < SlayerItems.UPGRADED_SWORD_LEVEL_REQ) {
                    player.sendMessage(Text.literal("✗ Requires " + type.displayName + " Bounty Level " + SlayerItems.UPGRADED_SWORD_LEVEL_REQ)
                            .formatted(Formatting.RED), false);
                    return false;
                }

                removeCores(player, coreName, 5);
                removeItem(player, Items.DIAMOND_SWORD, 1);
                removeSlayerSword(player, type);

                player.getInventory().insertStack(SlayerItems.createUpgradedSlayerSword(type));
                player.sendMessage(Text.literal("✓ Crafted " + type.displayName + " Slayer Sword II!")
                        .formatted(type.color, Formatting.BOLD), false);
                return true;
            }
        }
        return false;
    }

    // ============================================================
    // THE GAVEL
    // Recipe: 2 Warden Cores + 2 Netherite Ingots + 1 Wooden Axe
    // ============================================================
    private static boolean tryGavelRecipe(ServerPlayerEntity player) {
        int wardenCores = countCores(player, "Warden Core");
        int netheriteIngots = countItem(player, Items.NETHERITE_INGOT);
        boolean hasAxe = hasItem(player, Items.WOODEN_AXE);

        if (wardenCores < 2 || netheriteIngots < 2 || !hasAxe) return false;

        int level = SlayerData.getSlayerLevel(player.getUuidAsString(), SlayerManager.SlayerType.WARDEN);
        if (level < 7) {
            player.sendMessage(Text.literal("✗ Requires Warden Bounty Level 7")
                    .formatted(Formatting.RED), false);
            return false;
        }

        removeCores(player, "Warden Core", 2);
        removeItem(player, Items.NETHERITE_INGOT, 2);
        removeItem(player, Items.WOODEN_AXE, 1);

        player.getInventory().insertStack(SlayerItems.createTheGavel());
        player.sendMessage(Text.literal("✓ Crafted The Gavel!")
                .formatted(Formatting.GOLD, Formatting.BOLD), false);

        return true;
    }

    // ============================================================
    // BASE HPEBM
    // Recipe: 1 Diamond Shovel + 4 Redstone Blocks + 4 Glowstone
    // ============================================================
    private static boolean tryBaseHPEBMRecipe(ServerPlayerEntity player) {
        boolean hasShovel = hasItem(player, Items.DIAMOND_SHOVEL);
        int redstoneBlocks = countItem(player, Items.REDSTONE_BLOCK);
        int glowstone = countItem(player, Items.GLOWSTONE);

        if (!hasShovel || redstoneBlocks < 4 || glowstone < 4) return false;

        removeItem(player, Items.DIAMOND_SHOVEL, 1);
        removeItem(player, Items.REDSTONE_BLOCK, 4);
        removeItem(player, Items.GLOWSTONE, 4);

        player.getInventory().insertStack(CustomItemHandler.createHPEBM(1));
        player.sendMessage(Text.literal("✓ Crafted HPEBM Mk1!")
                .formatted(Formatting.WHITE, Formatting.BOLD), false);

        return true;
    }

    // ============================================================
    // HELPER METHODS FOR SLAYER SWORDS
    // ============================================================
    private static boolean hasSlayerSword(ServerPlayerEntity player, SlayerManager.SlayerType type) {
        String swordName = type.displayName + " Bounty Sword";
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
            if (name != null && name.getString().contains(swordName)) {
                return true;
            }
        }
        return false;
    }

    private static void removeSlayerSword(ServerPlayerEntity player, SlayerManager.SlayerType type) {
        String swordName = type.displayName + " Bounty Sword";
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
            if (name != null && name.getString().contains(swordName)) {
                stack.decrement(1);
                return;
            }
        }
    }

    // ============================================================
    // EXISTING HELPER METHODS (keep these from your current file)
    // ============================================================
    private static int countCores(ServerPlayerEntity player, String coreName) {
        int count = 0;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
            if (name != null && name.getString().equals(coreName)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static int countItem(ServerPlayerEntity player, Item item) {
        int count = 0;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isOf(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static boolean hasItem(ServerPlayerEntity player, Item item) {
        return countItem(player, item) >= 1;
    }

    private static void removeCores(ServerPlayerEntity player, String coreName, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().size() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
            if (name != null && name.getString().equals(coreName)) {
                int toRemove = Math.min(remaining, stack.getCount());
                stack.decrement(toRemove);
                remaining -= toRemove;
            }
        }
    }

    private static void removeItem(ServerPlayerEntity player, Item item, int amount) {
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
}