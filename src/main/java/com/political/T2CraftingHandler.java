package com.political;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.List;

public class T2CraftingHandler {

    /**
     * Recipe requirements for T2 armor
     * T1 piece + 4 cores + 1 nether star + special material
     */
    public static class T2Recipe {
        public final SlayerManager.SlayerType type;
        public final String pieceName; // "Helmet", "Chestplate", etc.
        public final int coresRequired;
        public final int specialMaterialCount;
        public final ItemStack specialMaterial;
        public final int levelRequired;

        public T2Recipe(SlayerManager.SlayerType type, String pieceName,
                        int coresRequired, ItemStack specialMaterial, int specialCount, int levelReq) {
            this.type = type;
            this.pieceName = pieceName;
            this.coresRequired = coresRequired;
            this.specialMaterial = specialMaterial;
            this.specialMaterialCount = specialCount;
            this.levelRequired = levelReq;
        }
    }

    public static List<T2Recipe> getAllRecipes() {
        List<T2Recipe> recipes = new ArrayList<>();

        // Zombie T2
        recipes.add(new T2Recipe(SlayerManager.SlayerType.ZOMBIE, "Helmet",
                4, new ItemStack(Items.WITHER_SKELETON_SKULL), 1, 8));
        recipes.add(new T2Recipe(SlayerManager.SlayerType.ZOMBIE, "Chestplate",
                6, new ItemStack(Items.NETHER_STAR), 1, 8));
        recipes.add(new T2Recipe(SlayerManager.SlayerType.ZOMBIE, "Leggings",
                5, new ItemStack(Items.NETHERITE_INGOT), 2, 8));
        recipes.add(new T2Recipe(SlayerManager.SlayerType.ZOMBIE, "Boots",
                4, new ItemStack(Items.NETHERITE_INGOT), 1, 8));

        // Spider T2
        recipes.add(new T2Recipe(SlayerManager.SlayerType.SPIDER, "Helmet",
                4, new ItemStack(Items.FERMENTED_SPIDER_EYE), 8, 8));
        recipes.add(new T2Recipe(SlayerManager.SlayerType.SPIDER, "Chestplate",
                6, new ItemStack(Items.NETHER_STAR), 1, 8));
        recipes.add(new T2Recipe(SlayerManager.SlayerType.SPIDER, "Leggings",
                5, new ItemStack(Items.COBWEB), 32, 8));
        recipes.add(new T2Recipe(SlayerManager.SlayerType.SPIDER, "Boots",
                4, new ItemStack(Items.SPIDER_EYE), 16, 8));

        // Skeleton T2
        recipes.add(new T2Recipe(SlayerManager.SlayerType.SKELETON, "Helmet",
                4, new ItemStack(Items.SKELETON_SKULL), 1, 8));
        recipes.add(new T2Recipe(SlayerManager.SlayerType.SKELETON, "Chestplate",
                6, new ItemStack(Items.NETHER_STAR), 1, 8));
        recipes.add(new T2Recipe(SlayerManager.SlayerType.SKELETON, "Leggings",
                5, new ItemStack(Items.BONE_BLOCK), 16, 8));
        recipes.add(new T2Recipe(SlayerManager.SlayerType.SKELETON, "Boots",
                4, new ItemStack(Items.BONE), 32, 8));

        // Slime T2
        recipes.add(new T2Recipe(SlayerManager.SlayerType.SLIME, "Helmet",
                4, new ItemStack(Items.SLIME_BLOCK), 16, 8));
        recipes.add(new T2Recipe(SlayerManager.SlayerType.SLIME, "Chestplate",
                6, new ItemStack(Items.NETHER_STAR), 1, 8));
        recipes.add(new T2Recipe(SlayerManager.SlayerType.SLIME, "Leggings",
                5, new ItemStack(Items.SLIME_BLOCK), 32, 8));
        recipes.add(new T2Recipe(SlayerManager.SlayerType.SLIME, "Boots",
                4, new ItemStack(Items.HONEY_BLOCK), 8, 8));

        // Enderman T2
        recipes.add(new T2Recipe(SlayerManager.SlayerType.ENDERMAN, "Helmet",
                4, new ItemStack(Items.ENDER_EYE), 16, 8));
        recipes.add(new T2Recipe(SlayerManager.SlayerType.ENDERMAN, "Chestplate",
                6, new ItemStack(Items.NETHER_STAR), 1, 8));
        recipes.add(new T2Recipe(SlayerManager.SlayerType.ENDERMAN, "Leggings",
                5, new ItemStack(Items.END_CRYSTAL), 2, 8));
        recipes.add(new T2Recipe(SlayerManager.SlayerType.ENDERMAN, "Boots",
                4, new ItemStack(Items.CHORUS_FRUIT), 32, 8));

        // Warden T2 (hardest to craft)
        recipes.add(new T2Recipe(SlayerManager.SlayerType.IRON_GOLEM, "Helmet",
                6, new ItemStack(Items.ECHO_SHARD), 8, 10));
        recipes.add(new T2Recipe(SlayerManager.SlayerType.IRON_GOLEM, "Chestplate",
                8, new ItemStack(Items.NETHER_STAR), 2, 10));
        recipes.add(new T2Recipe(SlayerManager.SlayerType.IRON_GOLEM, "Leggings",
                7, new ItemStack(Items.SCULK_CATALYST), 4, 10));
        recipes.add(new T2Recipe(SlayerManager.SlayerType.IRON_GOLEM, "Boots",
                6, new ItemStack(Items.SCULK_SHRIEKER), 2, 10));

        return recipes;
    }

    /**
     * Check if player can craft a T2 piece
     */
    public static boolean canCraft(ServerPlayer player, T2Recipe recipe,
                                   ItemStack t1Piece, List<ItemStack> cores, ItemStack special) {

        // Check level
        int playerLevel = SlayerData.getSlayerLevel(player.getUUID().toString(), recipe.type);
        if (playerLevel < recipe.levelRequired) {
            player.sendSystemMessage(Component.literal("§c⚠ Requires " + recipe.type.displayName +
                    " Bounty Level " + recipe.levelRequired + "!"), false);
            return false;
        }

        // Check T1 piece
        if (!isMatchingT1Piece(t1Piece, recipe.type, recipe.pieceName)) {
            player.sendSystemMessage(Component.literal("§c⚠ Requires T1 " + recipe.type.displayName +
                    " " + recipe.pieceName + "!"), false);
            return false;
        }

        // Check cores
        int totalCores = 0;
        for (ItemStack coreStack : cores) {
            if (SlayerItems.isSlayerCore(coreStack)) {
                SlayerManager.SlayerType coreType = SlayerItems.getCoreType(coreStack);
                if (coreType == recipe.type) {
                    totalCores += coreStack.getCount();
                }
            }
        }
        if (totalCores < recipe.coresRequired) {
            player.sendSystemMessage(Component.literal("§c⚠ Requires " + recipe.coresRequired +
                    " " + recipe.type.displayName + " Cores! (Have: " + totalCores + ")"), false);
            return false;
        }

        // Check special material
        if (!special.is(recipe.specialMaterial.getItem()) ||
                special.getCount() < recipe.specialMaterialCount) {
            player.sendSystemMessage(Component.literal("§c⚠ Requires " + recipe.specialMaterialCount +
                      "x " + recipe.specialMaterial.getHoverName().getString() + "!"), false);
            return false;
        }

        return true;
    }

    /**
     * Perform the craft - consumes materials and returns T2 item
     */
    public static ItemStack craft(ServerPlayer player, T2Recipe recipe) {
        return switch (recipe.pieceName) {
            case "Helmet" -> SlayerItems.createT2Helmet(recipe.type);
            case "Chestplate" -> SlayerItems.createT2Chestplate(recipe.type);
            case "Leggings" -> SlayerItems.createT2Leggings(recipe.type);
            case "Boots" -> SlayerItems.createT2Boots(recipe.type);
            default -> ItemStack.EMPTY;
        };
    }

    private static boolean isMatchingT1Piece(ItemStack stack, SlayerManager.SlayerType type, String piece) {
        if (stack.isEmpty()) return false;
        Component name = stack.get(DataComponents.CUSTOM_NAME);
        if (name == null) return false;
        String n = name.getString();

        // Should NOT be T2
        if (n.contains(" II")) return false;

        // Check piece type
        if (!n.contains(piece)) return false;

        // Check slayer type match
        return switch (type) {
            case ZOMBIE -> n.contains("Undying") || n.contains("Outlaw");
            case SPIDER -> n.contains("Venomous") || n.contains("Bandit");
            case SKELETON -> n.contains("Bone") || n.contains("Desperado");
            case SLIME -> n.contains("Gelatinous") || n.contains("Rustler");
            case ENDERMAN -> n.contains("Void") || n.contains("Phantom");
            case IRON_GOLEM -> n.contains("Sculk") || n.contains("Terror");
            case PIGLIN -> n.contains("Gilded") || n.contains("Ravager");
            case HUSK -> n.contains("Undying") || n.contains("Outlaw");
            case CAVE_SPIDER -> n.contains("Venomous") || n.contains("Bandit");
            case STRAY -> n.contains("Bone") || n.contains("Desperado");
            case MAGMA_CUBE -> n.contains("Gelatinous") || n.contains("Rustler");
            case BLAZE -> n.contains("Void") || n.contains("Phantom");
            case HOGLIN -> n.contains("Gilded") || n.contains("Ravager");
            case WITHER_SKELETON -> n.contains("Bone") || n.contains("Desperado");
            case RAVAGER -> n.contains("Sculk") || n.contains("Terror");
            case NANITE -> n.contains("Void") || n.contains("Phantom");
        };
    }

    /**
     * Get recipe display for GUI
     */
    public static List<Component> getRecipeLore(T2Recipe recipe) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ RECIPE ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("• T1 " + recipe.type.displayName + " " + recipe.pieceName)
                .withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("• " + recipe.coresRequired + "x " + recipe.type.displayName + " Cores")
                .withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal("• " + recipe.specialMaterialCount + "x " +
                          recipe.specialMaterial.getHoverName().getString())
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal(""));
        lore.add(Component.literal("⚠ Requires Level " + recipe.levelRequired)
                .withStyle(ChatFormatting.RED));
        return lore;
    }
}