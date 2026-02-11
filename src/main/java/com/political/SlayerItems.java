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

    private static final String SLAYER_SWORD_TAG = "BOUNTY_SWORD";
    private static final String SLAYER_CORE_TAG = "BOUNTY_CORE";

    public static boolean isSlayerSword(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return false;
        return name.getString().contains("Bounty Sword");
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
                Text.literal(type.displayName + " Bounty Sword")
                        .formatted(type.color, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚔ Bounty Weapon").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Deals ").formatted(Formatting.GRAY)
                .append(Text.literal("2x damage").formatted(Formatting.RED, Formatting.BOLD))
                .append(Text.literal(" to:").formatted(Formatting.GRAY)));
        lore.add(Text.literal("  • " + type.displayName + "s").formatted(type.color));
        lore.add(Text.literal("  • " + type.bossName).formatted(type.color));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Counts as 2 kills per kill").formatted(Formatting.GREEN));
        lore.add(Text.literal("Bypasses ").formatted(Formatting.GRAY)
                .append(Text.literal("Bounty Boss Resistance").formatted(Formatting.GOLD)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires: " + type.displayName + " Bounty Lvl " + BASIC_SWORD_LEVEL_REQ)
                .formatted(Formatting.RED));

        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));

        return sword;
    }
// Add this method to SlayerItems.java:

    public static final int UPGRADED_SWORD_LEVEL_REQ = 6;

    public static ItemStack createUpgradedSlayerSword(SlayerManager.SlayerType type) {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);

        sword.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(type.displayName + " Slayer Sword II")
                        .formatted(type.color, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚔ Upgraded Slayer Weapon").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Deals ").formatted(Formatting.GRAY)
                .append(Text.literal("3x damage").formatted(Formatting.RED, Formatting.BOLD))
                .append(Text.literal(" to:").formatted(Formatting.GRAY)));
        lore.add(Text.literal("  • " + type.displayName + "s").formatted(type.color));
        lore.add(Text.literal("  • " + type.bossName).formatted(type.color));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Counts as 3 kills per kill").formatted(Formatting.GREEN));
        lore.add(Text.literal("Bypasses ").formatted(Formatting.GRAY)
                .append(Text.literal("Slayer Boss Resistance").formatted(Formatting.GOLD)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires: " + type.displayName + " Slayer Lvl " + UPGRADED_SWORD_LEVEL_REQ)
                .formatted(Formatting.RED));

        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        return sword;
    }

    public static boolean isUpgradedSlayerSword(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return false;
        return name.getString().contains("Slayer Sword II");
    }
    public static ItemStack createSlayerSwordForPlayer(ServerPlayerEntity player, SlayerManager.SlayerType type) {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);

        sword.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(type.displayName + " Bounty Hunter's Sword")
                        .formatted(type.color, Formatting.BOLD));

        int playerLevel = SlayerData.getSlayerLevel(player.getUuidAsString(), type);
        int requiredLevel = BASIC_SWORD_LEVEL_REQ;
        boolean meetsRequirement = playerLevel >= requiredLevel;

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚔ Bounty Weapon").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Deals ").formatted(Formatting.GRAY)
                .append(Text.literal("2x damage").formatted(Formatting.RED, Formatting.BOLD))
                .append(Text.literal(" to:").formatted(Formatting.GRAY)));
        lore.add(Text.literal("  • " + type.displayName + "s").formatted(type.color));
        lore.add(Text.literal("  • " + type.bossName).formatted(type.color));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Counts as 2 kills per kill").formatted(Formatting.GREEN));
        lore.add(Text.literal("Bypasses ").formatted(Formatting.GRAY)
                .append(Text.literal("Bounty Resistance").formatted(Formatting.GOLD)));

        // Only show requirement if not met
        if (!meetsRequirement) {
            lore.add(Text.literal(""));
            lore.add(Text.literal("⚠ Requires: " + type.displayName + " Bounty Lvl " + requiredLevel)
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
        lore.add(Text.literal("Bounty equipment.").formatted(Formatting.GRAY));
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
        ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);

        sword.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⚔ Ender Sword")
                        .formatted(Formatting.DARK_PURPLE, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§d§lLEGENDARY WEAPON"));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Forged from the essence of").formatted(Formatting.GRAY));
        lore.add(Text.literal("the Void Phantom itself.").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Ability: Void Strike").formatted(Formatting.GOLD, Formatting.BOLD));
        lore.add(Text.literal("  Right-click to teleport behind").formatted(Formatting.GRAY));
        lore.add(Text.literal("  your target and deal 2x damage").formatted(Formatting.GRAY));
        lore.add(Text.literal("  Cooldown: 10s").formatted(Formatting.RED));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Deals 4x damage to Endermen").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Crafted with: Void Core").formatted(Formatting.DARK_GRAY));

        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        return sword;
    }

    public static ItemStack createAbyssalBlade() {
        ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);

        sword.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⚔ Abyssal Blade")
                        .formatted(Formatting.DARK_AQUA, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§5§lMYTHIC WEAPON"));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Born from the depths of the").formatted(Formatting.GRAY));
        lore.add(Text.literal("Sculk Devourer's domain.").formatted(Formatting.DARK_AQUA));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Ability: Sonic Devastation").formatted(Formatting.GOLD, Formatting.BOLD));
        lore.add(Text.literal("  Right-click to release a").formatted(Formatting.GRAY));
        lore.add(Text.literal("  shockwave stunning all enemies").formatted(Formatting.GRAY));
        lore.add(Text.literal("  within 8 blocks for 3s").formatted(Formatting.GRAY));
        lore.add(Text.literal("  Cooldown: 30s").formatted(Formatting.RED));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Deals 5x damage to Wardens").formatted(Formatting.DARK_AQUA));
        lore.add(Text.literal("Immune to Darkness effect").formatted(Formatting.BLUE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Crafted with: Sculk Core").formatted(Formatting.DARK_GRAY));

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
        lore.add(Text.literal("Used to craft Bounty Hunter's Swords").formatted(Formatting.DARK_PURPLE));

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
    public static boolean isEnderSword(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return false;
        return name.getString().contains("Ender Sword");
    }

    public static boolean isAbyssalBlade(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return false;
        return name.getString().contains("Abyssal Blade");
    }

    public static boolean isLegendaryWeapon(ItemStack stack) {
        return isEnderSword(stack) || isAbyssalBlade(stack);
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

    public static boolean canUseSlayerSword(ServerPlayerEntity player, ItemStack sword) {
        if (!isSlayerSword(sword)) return true;

        SlayerManager.SlayerType swordType = getSwordSlayerType(sword);
        if (swordType == null) return true;

        int playerLevel = SlayerData.getSlayerLevel(player.getUuidAsString(), swordType);
        int requiredLevel = getSwordLevelRequirement(sword);

        return playerLevel >= requiredLevel;
    }
// ============================================================
// SLAYER ARMOR - T1 (Crafted with Chunks)
// ============================================================

    public static final int T1_ARMOR_LEVEL_REQ = 4;
    public static final int T2_ARMOR_LEVEL_REQ = 8;

    public static ItemStack createSlayerHelmet(SlayerManager.SlayerType type, int tier) {
        ItemStack helmet = new ItemStack(tier == 1 ? Items.IRON_HELMET : Items.DIAMOND_HELMET);
        String tierName = tier == 1 ? "" : " II";

        helmet.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(type.displayName + " Slayer Helmet" + tierName)
                        .formatted(type.color, Formatting.BOLD));

        List<Text> lore = buildArmorLore(type, tier, "Helmet");
        helmet.set(DataComponentTypes.LORE, new LoreComponent(lore));

        if (tier >= 2) {
            helmet.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        }

        return helmet;
    }

    public static ItemStack createSlayerChestplate(SlayerManager.SlayerType type, int tier) {
        ItemStack chestplate = new ItemStack(tier == 1 ? Items.IRON_CHESTPLATE : Items.DIAMOND_CHESTPLATE);
        String tierName = tier == 1 ? "" : " II";

        chestplate.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(type.displayName + " Slayer Chestplate" + tierName)
                        .formatted(type.color, Formatting.BOLD));

        List<Text> lore = buildArmorLore(type, tier, "Chestplate");
        chestplate.set(DataComponentTypes.LORE, new LoreComponent(lore));

        if (tier >= 2) {
            chestplate.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        }

        return chestplate;
    }

    public static ItemStack createSlayerLeggings(SlayerManager.SlayerType type, int tier) {
        ItemStack leggings = new ItemStack(tier == 1 ? Items.IRON_LEGGINGS : Items.DIAMOND_LEGGINGS);
        String tierName = tier == 1 ? "" : " II";

        leggings.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(type.displayName + " Slayer Leggings" + tierName)
                        .formatted(type.color, Formatting.BOLD));

        List<Text> lore = buildArmorLore(type, tier, "Leggings");
        leggings.set(DataComponentTypes.LORE, new LoreComponent(lore));

        if (tier >= 2) {
            leggings.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        }

        return leggings;
    }

    public static ItemStack createSlayerBoots(SlayerManager.SlayerType type, int tier) {
        ItemStack boots = new ItemStack(tier == 1 ? Items.IRON_BOOTS : Items.DIAMOND_BOOTS);
        String tierName = tier == 1 ? "" : " II";

        boots.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(type.displayName + " Slayer Boots" + tierName)
                        .formatted(type.color, Formatting.BOLD));

        List<Text> lore = buildArmorLore(type, tier, "Boots");
        boots.set(DataComponentTypes.LORE, new LoreComponent(lore));

        if (tier >= 2) {
            boots.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        }

        return boots;
    }

    private static List<Text> buildArmorLore(SlayerManager.SlayerType type, int tier, String piece) {
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));

        if (tier == 1) {
            lore.add(Text.literal("⚔ Slayer Armor").formatted(Formatting.GRAY));
        } else {
            lore.add(Text.literal("⚔ Upgraded Slayer Armor").formatted(Formatting.DARK_PURPLE));
        }

        lore.add(Text.literal(""));
        lore.add(Text.literal("Set Bonus (" + type.displayName + "):").formatted(Formatting.GOLD));

        int damageReduction = tier == 1 ? 10 : 25;
        int bonusHealth = tier == 1 ? 2 : 5;

        lore.add(Text.literal("  -" + damageReduction + "% damage from " + type.displayName + "s").formatted(Formatting.GREEN));
        lore.add(Text.literal("  +" + bonusHealth + " hearts vs " + type.displayName + " bosses").formatted(Formatting.GREEN));

        if (tier >= 2) {
            lore.add(Text.literal("  +15% XP from " + type.displayName + " bounties").formatted(Formatting.AQUA));
        }

        lore.add(Text.literal(""));

        int reqLevel = tier == 1 ? T1_ARMOR_LEVEL_REQ : T2_ARMOR_LEVEL_REQ;
        lore.add(Text.literal("⚠ Requires: " + type.displayName + " Slayer Lvl " + reqLevel)
                .formatted(Formatting.RED));

        return lore;
    }

// ============================================================
// ARMOR SET HELPERS
// ============================================================

    public static void giveFullArmorSet(ServerPlayerEntity player, SlayerManager.SlayerType type, int tier) {
        ItemStack helmet = createSlayerHelmet(type, tier);
        ItemStack chestplate = createSlayerChestplate(type, tier);
        ItemStack leggings = createSlayerLeggings(type, tier);
        ItemStack boots = createSlayerBoots(type, tier);

        giveItem(player, helmet);
        giveItem(player, chestplate);
        giveItem(player, leggings);
        giveItem(player, boots);

        String tierName = tier == 1 ? "" : " II";
        player.sendMessage(Text.literal("✔ Received full " + type.displayName + " Slayer Armor" + tierName + " set!")
                .formatted(Formatting.GREEN), false);
    }

    private static void giveItem(ServerPlayerEntity player, ItemStack stack) {
        if (!player.getInventory().insertStack(stack)) {
            player.dropItem(stack, false);
        }
    }

    public static boolean isSlayerArmor(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return false;
        String nameStr = name.getString();
        return nameStr.contains("Slayer Helmet") ||
                nameStr.contains("Slayer Chestplate") ||
                nameStr.contains("Slayer Leggings") ||
                nameStr.contains("Slayer Boots");
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

        player.sendMessage(Text.literal("✔ Crafted " + type.displayName + " Bounty Hunter's Sword!")
                .formatted(Formatting.GREEN), false);

        return true;
    }

    public static void giveSlayerSword(ServerPlayerEntity player, SlayerManager.SlayerType type) {
        ItemStack sword = createSlayerSword(type);
        if (!player.getInventory().insertStack(sword)) {
            player.dropItem(sword, false);
        }
        player.sendMessage(Text.literal("✔ Received " + type.displayName + " Bounty Hunter's Sword!")
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