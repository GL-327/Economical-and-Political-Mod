package com.political;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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
        recipes.add(new T2Recipe(SlayerManager.SlayerType.WARDEN, "Helmet",
                6, new ItemStack(Items.ECHO_SHARD), 8, 10));
        recipes.add(new T2Recipe(SlayerManager.SlayerType.WARDEN, "Chestplate",
                8, new ItemStack(Items.NETHER_STAR), 2, 10));
        recipes.add(new T2Recipe(SlayerManager.SlayerType.WARDEN, "Leggings",
                7, new ItemStack(Items.SCULK_CATALYST), 4, 10));
        recipes.add(new T2Recipe(SlayerManager.SlayerType.WARDEN, "Boots",
                6, new ItemStack(Items.SCULK_SHRIEKER), 2, 10));

        return recipes;
    }

    /**
     * Check if player can craft a T2 piece
     */
    public static boolean canCraft(ServerPlayerEntity player, T2Recipe recipe,
                                   ItemStack t1Piece, List<ItemStack> cores, ItemStack special) {

        // Check level
        int playerLevel = SlayerData.getSlayerLevel(player.getUuidAsString(), recipe.type);
        if (playerLevel < recipe.levelRequired) {
            player.sendMessage(Text.literal("§c⚠ Requires " + recipe.type.displayName +
                    " Bounty Level " + recipe.levelRequired + "!"), false);
            return false;
        }

        // Check T1 piece
        if (!isMatchingT1Piece(t1Piece, recipe.type, recipe.pieceName)) {
            player.sendMessage(Text.literal("§c⚠ Requires T1 " + recipe.type.displayName +
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
            player.sendMessage(Text.literal("§c⚠ Requires " + recipe.coresRequired +
                    " " + recipe.type.displayName + " Cores! (Have: " + totalCores + ")"), false);
            return false;
        }

        // Check special material
        if (!special.isOf(recipe.specialMaterial.getItem()) ||
                special.getCount() < recipe.specialMaterialCount) {
            player.sendMessage(Text.literal("§c⚠ Requires " + recipe.specialMaterialCount +
                    "x " + recipe.specialMaterial.getName().getString() + "!"), false);
            return false;
        }

        return true;
    }

    /**
     * Perform the craft - consumes materials and returns T2 item
     */
    public static ItemStack craft(ServerPlayerEntity player, T2Recipe recipe) {
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
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
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
            case WARDEN -> n.contains("Sculk") || n.contains("Terror");
        };
    }

    /**
     * Get recipe display for GUI
     */
    public static List<Text> getRecipeLore(T2Recipe recipe) {
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ RECIPE ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("• T1 " + recipe.type.displayName + " " + recipe.pieceName)
                .formatted(Formatting.YELLOW));
        lore.add(Text.literal("• " + recipe.coresRequired + "x " + recipe.type.displayName + " Cores")
                .formatted(Formatting.AQUA));
        lore.add(Text.literal("• " + recipe.specialMaterialCount + "x " +
                        recipe.specialMaterial.getName().getString())
                .formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires Level " + recipe.levelRequired)
                .formatted(Formatting.RED));
        return lore;
    }
}