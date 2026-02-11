package com.political;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class SlayerItems {

    // ============================================================
    // ITEM IDENTIFICATION
    // ============================================================

    private static final String SLAYER_SWORD_TAG = "SLAYER_SWORD";
    private static final String SLAYER_CORE_TAG = "SLAYER_CORE";

    public static boolean isSlayerSword(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return false;
        return name.getString().contains("Slayer Sword");
    }

    public static boolean isSlayerCore(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return false;
        return name.getString().contains("Core");
    }

    public static SlayerManager.SlayerType getSwordSlayerType(ItemStack stack) {
        if (!isSlayerSword(stack)) return null;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return null;
        String nameStr = name.getString();

        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            if (nameStr.contains(type.displayName)) {
                return type;
            }
        }
        return null;
    }

    // ============================================================
    // SLAYER SWORDS - 2x damage to matching slayer type
    // ============================================================
    public static ItemStack createSlayerSword(SlayerManager.SlayerType type) {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);

        sword.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(type.displayName + " Slayer Sword")
                        .formatted(type.color, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚔ Slayer Weapon").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Deals ").formatted(Formatting.GRAY)
                .append(Text.literal("2x damage").formatted(Formatting.RED, Formatting.BOLD))
                .append(Text.literal(" to:").formatted(Formatting.GRAY)));
        lore.add(Text.literal("  • " + type.displayName + "s").formatted(type.color));
        lore.add(Text.literal("  • " + type.bossName).formatted(type.color));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Counts as 2 kills per kill").formatted(Formatting.GREEN));
        lore.add(Text.literal("Bypasses ").formatted(Formatting.GRAY)
                .append(Text.literal("Slayer Boss Resistance").formatted(Formatting.GOLD)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires: " + type.displayName + " Slayer Lvl " + BASIC_SWORD_LEVEL_REQ)
                .formatted(Formatting.RED));

        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));

        return sword;
    }

    public static ItemStack createSlayerSwordForPlayer(ServerPlayerEntity player, SlayerManager.SlayerType type) {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);

        sword.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(type.displayName + " Slayer Sword")
                        .formatted(type.color, Formatting.BOLD));

        int playerLevel = SlayerData.getSlayerLevel(player.getUuidAsString(), type);
        int requiredLevel = BASIC_SWORD_LEVEL_REQ;
        boolean meetsRequirement = playerLevel >= requiredLevel;

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚔ Slayer Weapon").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Deals ").formatted(Formatting.GRAY)
                .append(Text.literal("2x damage").formatted(Formatting.RED, Formatting.BOLD))
                .append(Text.literal(" to:").formatted(Formatting.GRAY)));
        lore.add(Text.literal("  • " + type.displayName + "s").formatted(type.color));
        lore.add(Text.literal("  • " + type.bossName).formatted(type.color));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Counts as 2 kills per kill").formatted(Formatting.GREEN));
        lore.add(Text.literal("Bypasses ").formatted(Formatting.GRAY)
                .append(Text.literal("Slayer Boss Resistance").formatted(Formatting.GOLD)));

        // Only show requirement if not met
        if (!meetsRequirement) {
            lore.add(Text.literal(""));
            lore.add(Text.literal("⚠ Requires: " + type.displayName + " Slayer Lvl " + requiredLevel)
                    .formatted(Formatting.RED));
        }

        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));

        return sword;
    }



    public static double getSlayerSwordDamageMultiplier(ItemStack weapon, LivingEntity target, ServerPlayerEntity player) {
        if (!isSlayerSword(weapon)) return 1.0;

        // Check level requirement - if not met, no bonus damage
        if (!canUseSlayerSword(player, weapon)) {
            return 1.0; // No bonus, sword acts like normal iron sword
        }

        SlayerManager.SlayerType swordType = getSwordSlayerType(weapon);
        if (swordType == null) return 1.0;

        // Rest of existing logic...
        boolean isMatchingMob = isMatchingMobType(target, swordType);
        boolean isMatchingBoss = false;
        if (SlayerManager.isSlayerBoss(target.getUuid())) {
            SlayerManager.SlayerType bossType = SlayerManager.getBossSlayerType(target.getUuid());
            isMatchingBoss = (bossType == swordType);
        }

        if (isMatchingMob || isMatchingBoss) {
            return 2.0;
        }

        if (SlayerManager.isSlayerBoss(target.getUuid())) {
            return 1.5;
        }

        return 1.0;
    }

    public static boolean bypassesSlayerResistance(ItemStack weapon) {
        return isSlayerSword(weapon);
    }

    private static boolean isMatchingMobType(LivingEntity entity, SlayerManager.SlayerType type) {
        return switch (type) {
            case ZOMBIE -> entity instanceof net.minecraft.entity.mob.ZombieEntity;
            case SPIDER -> entity instanceof net.minecraft.entity.mob.SpiderEntity;
            case SKELETON -> entity instanceof net.minecraft.entity.mob.SkeletonEntity
                    || entity instanceof net.minecraft.entity.mob.StrayEntity
                    || entity instanceof net.minecraft.entity.mob.WitherSkeletonEntity;
            case SLIME -> entity instanceof net.minecraft.entity.mob.SlimeEntity
                    || entity instanceof net.minecraft.entity.mob.MagmaCubeEntity;
            case ENDERMAN -> entity instanceof net.minecraft.entity.mob.EndermanEntity;
            case WARDEN -> entity instanceof net.minecraft.entity.mob.WardenEntity;
        };
    }

    // ============================================================
    // SLAYER CORES - Rare boss drops for crafting
    // ============================================================

    public static ItemStack createCore(SlayerManager.SlayerType type) {
        ItemStack core = new ItemStack(type.icon);

        String coreName = type.displayName + " Core";

        core.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✦ " + coreName + " ✦")
                        .formatted(type.color, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("RARE DROP").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("A powerful essence from the").formatted(Formatting.GRAY));
        lore.add(Text.literal(type.bossName + ".").formatted(type.color));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Used in crafting powerful").formatted(Formatting.GRAY));
        lore.add(Text.literal("slayer equipment.").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));

        // Hint at what it crafts
        String craftHint = switch (type) {
            case ZOMBIE -> "Crafts: Revenant Falchion";
            case SPIDER -> "Crafts: Tarantula Blade";
            case SKELETON -> "Crafts: Bone Reaper";
            case SLIME -> "Crafts: Sludge Hammer";
            case ENDERMAN -> "Crafts: Ender Sword";
            case WARDEN -> "Crafts: Abyssal Blade";
        };
        lore.add(Text.literal(craftHint).formatted(Formatting.DARK_PURPLE));

        core.set(DataComponentTypes.LORE, new LoreComponent(lore));

        // Make it glow
        core.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        return core;
    }

    // ============================================================
    // CRAFTED SLAYER WEAPONS (Future expansion)
    // ============================================================

    public static ItemStack createEnderSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);

        sword.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⚔ Ender Sword ⚔")
                        .formatted(Formatting.DARK_PURPLE, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("LEGENDARY").formatted(Formatting.GOLD, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Forged from the essence of").formatted(Formatting.GRAY));
        lore.add(Text.literal("the Voidgloom Seraph.").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Ability: Void Strike").formatted(Formatting.GOLD));
        lore.add(Text.literal("Teleport behind your target").formatted(Formatting.GRAY));
        lore.add(Text.literal("and deal massive damage.").formatted(Formatting.GRAY));
        lore.add(Text.literal("Cooldown: 5s").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Deals ").formatted(Formatting.GRAY)
                .append(Text.literal("3x damage").formatted(Formatting.RED, Formatting.BOLD))
                .append(Text.literal(" to Endermen").formatted(Formatting.GRAY)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Recipe: 1x Ender Core + 64 Obsidian").formatted(Formatting.DARK_GRAY));

        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));

        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        return sword;
    }

    public static ItemStack createAbyssalBlade() {
        ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);

        sword.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⚔ Abyssal Blade ⚔")
                        .formatted(Formatting.DARK_AQUA, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("MYTHIC").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("The ultimate slayer weapon,").formatted(Formatting.GRAY));
        lore.add(Text.literal("born from the depths.").formatted(Formatting.DARK_AQUA));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Ability: Sonic Devastation").formatted(Formatting.GOLD));
        lore.add(Text.literal("Release a shockwave that").formatted(Formatting.GRAY));
        lore.add(Text.literal("stuns and damages all").formatted(Formatting.GRAY));
        lore.add(Text.literal("nearby enemies.").formatted(Formatting.GRAY));
        lore.add(Text.literal("Cooldown: 10s").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Deals ").formatted(Formatting.GRAY)
                .append(Text.literal("2x damage").formatted(Formatting.RED, Formatting.BOLD))
                .append(Text.literal(" to ALL slayer mobs").formatted(Formatting.GRAY)));
        lore.add(Text.literal("Bypasses ALL slayer resistance").formatted(Formatting.GOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Recipe: 1x Warden Core + 32 Echo Shards").formatted(Formatting.DARK_GRAY));

        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));

        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        return sword;
    }
    public static ItemStack createChunk(SlayerManager.SlayerType type) {
        ItemStack chunk = new ItemStack(type.icon);

        String chunkName = switch (type) {
            case ZOMBIE -> "Undead Chunk";
            case SPIDER -> "Venomous Gland";
            case SKELETON -> "Ancient Bone";
            case SLIME -> "Condensed Gel";
            case ENDERMAN -> "Void Fragment";
            case WARDEN -> "Sculk Heart";
        };

        chunk.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✦ " + chunkName + " ✦")
                        .formatted(type.color, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("BOSS DROP").formatted(Formatting.GOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("A fragment from the").formatted(Formatting.GRAY));
        lore.add(Text.literal(type.bossName + ".").formatted(type.color));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Used to craft Slayer Swords").formatted(Formatting.DARK_PURPLE));

        chunk.set(DataComponentTypes.LORE, new LoreComponent(lore));
        chunk.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        return chunk;
    }
    // ============================================================
    // GIVE COMMANDS (for admin/testing)
    // ============================================================
// ============================================================
// CRAFTING - Check if player can craft sword
// ============================================================

    public static boolean canCraftSlayerSword(ServerPlayerEntity player, SlayerManager.SlayerType type) {
        // Requires 2 chunks of the slayer type
        int chunkCount = countChunks(player, type);
        return chunkCount >= 2;
    }

    public static int countChunks(ServerPlayerEntity player, SlayerManager.SlayerType type) {
        int count = 0;
        String chunkName = getChunkName(type);

        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (isChunk(stack, type)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static boolean isChunk(ItemStack stack, SlayerManager.SlayerType type) {
        if (stack == null || stack.isEmpty()) return false;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return false;
        String chunkName = getChunkName(type);
        return name.getString().contains(chunkName);
    }

    public static String getChunkName(SlayerManager.SlayerType type) {
        return switch (type) {
            case ZOMBIE -> "Undead Chunk";
            case SPIDER -> "Venomous Gland";
            case SKELETON -> "Ancient Bone";
            case SLIME -> "Condensed Gel";
            case ENDERMAN -> "Void Fragment";
            case WARDEN -> "Sculk Heart";
        };
    }
// ============================================================
// LEVEL REQUIREMENTS FOR SLAYER SWORDS
// ============================================================

    // Sword tiers and their level requirements
    public static final int BASIC_SWORD_LEVEL_REQ = 3;      // T1 sword
    public static final int UPGRADED_SWORD_LEVEL_REQ = 6;   // T2 sword

    public static boolean canUseSlayerSword(ServerPlayerEntity player, ItemStack sword) {
        if (!isSlayerSword(sword)) return true;

        SlayerManager.SlayerType swordType = getSwordSlayerType(sword);
        if (swordType == null) return true;

        int playerLevel = SlayerData.getSlayerLevel(player.getUuidAsString(), swordType);
        int requiredLevel = getSwordLevelRequirement(sword);

        return playerLevel >= requiredLevel;
    }

    public static int getSwordLevelRequirement(ItemStack sword) {
        if (!isSlayerSword(sword)) return 0;

        Text name = sword.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return BASIC_SWORD_LEVEL_REQ;

        // Check if it's an upgraded sword
        if (name.getString().contains("Upgraded") || name.getString().contains("II")) {
            return UPGRADED_SWORD_LEVEL_REQ;
        }

        return BASIC_SWORD_LEVEL_REQ;
    }

    public static boolean craftSlayerSword(ServerPlayerEntity player, SlayerManager.SlayerType type) {
        if (!canCraftSlayerSword(player, type)) {
            player.sendMessage(Text.literal("✖ Need 2x " + getChunkName(type) + " to craft!")
                    .formatted(Formatting.RED), false);
            return false;
        }

        // Remove 2 chunks
        int toRemove = 2;
        for (int i = 0; i < player.getInventory().size() && toRemove > 0; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (isChunk(stack, type)) {
                int removeFromStack = Math.min(toRemove, stack.getCount());
                stack.decrement(removeFromStack);
                toRemove -= removeFromStack;
            }
        }

        // Give sword
        ItemStack sword = createSlayerSword(type);
        if (!player.getInventory().insertStack(sword)) {
            player.dropItem(sword, false);
        }

        player.sendMessage(Text.literal("✔ Crafted " + type.displayName + " Slayer Sword!")
                .formatted(Formatting.GREEN), false);

        return true;
    }

    public static void giveSlayerSword(ServerPlayerEntity player, SlayerManager.SlayerType type) {
        ItemStack sword = createSlayerSword(type);
        if (!player.getInventory().insertStack(sword)) {
            player.dropItem(sword, false);
        }
        player.sendMessage(Text.literal("✔ Received " + type.displayName + " Slayer Sword!")
                .formatted(Formatting.GREEN), false);
    }

    public static void giveCore(ServerPlayerEntity player, SlayerManager.SlayerType type) {
        ItemStack core = createCore(type);
        if (!player.getInventory().insertStack(core)) {
            player.dropItem(core, false);
        }
        player.sendMessage(Text.literal("✔ Received " + type.displayName + " Core!")
                .formatted(Formatting.GREEN), false);
    }
}