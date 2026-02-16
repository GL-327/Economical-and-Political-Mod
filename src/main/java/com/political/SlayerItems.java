package com.political;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import java.util.HashSet;
import java.util.Set;import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.Identifier;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;


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

    public static SlayerManager.SlayerType getCoreType(ItemStack stack) {
        if (!isSlayerCore(stack)) return null;

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
// LEVEL-LOCKED WEAPON DAMAGE MULTIPLIER
// ============================================================
    public static float getLevelLockedDamageMultiplier(ServerPlayerEntity player, ItemStack weapon) {
        // Check bounty swords
        if (isSlayerSword(weapon)) {
            SlayerManager.SlayerType swordType = getSwordSlayerType(weapon);
            if (swordType == null) return 1.0f;

            int playerLevel = SlayerData.getSlayerLevel(player.getUuidAsString(), swordType);
            int requiredLevel = BASIC_SWORD_LEVEL_REQ;

            if (playerLevel < requiredLevel) {
                CustomItemHandler.sendLevelWarning(player, swordType.displayName + " Bounty Sword", requiredLevel, swordType.displayName);
                return 0.0f; // No damage!
            }
        }

        // Check upgraded swords
        if (isUpgradedSlayerSword(weapon)) {
            SlayerManager.SlayerType swordType = getSwordSlayerType(weapon);
            if (swordType == null) return 1.0f;

            int playerLevel = SlayerData.getSlayerLevel(player.getUuidAsString(), swordType);

            if (playerLevel < UPGRADED_SWORD_LEVEL_REQ) {
                CustomItemHandler.sendLevelWarning(player, swordType.displayName + " Slayer Sword II", UPGRADED_SWORD_LEVEL_REQ, swordType.displayName);
                return 0.0f;
            }
        }

        return 1.0f; // Normal damage
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
// ============================================================
// ZOMBIE BERSERKER HELMET - Level 12 Zombie Requirement
// ============================================================
    public static final int BERSERKER_HELMET_LEVEL_REQ = 12;


    // ============================================================
// SPIDER LEGGINGS - T12 Spider Requirement
// ============================================================
    public static final int SPIDER_LEGGINGS_LEVEL_REQ = 12;


    public static final int ZOMBIE_BERSERKER_LEVEL_REQ = 12;
    // ============================================================
// SKELETON BOW - T10 Skeleton Requirement
// ============================================================
    public static final int SKELETON_BOW_LEVEL_REQ = 10;



    public static ItemStack createSlayerCore(SlayerManager.SlayerType type) {
        ItemStack core = new ItemStack(type.icon);

        core.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(type.displayName + " Core")
                        .formatted(type.color, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("Bounty Core").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Used for crafting " + type.displayName).formatted(Formatting.GRAY));
        lore.add(Text.literal("bounty equipment.").formatted(Formatting.GRAY));

        core.set(DataComponentTypes.LORE, new LoreComponent(lore));
        core.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        return core;
    }

    // ============================================================
// SLIME BOOTS - T8 Slime Requirement
// ============================================================
    public static final int SLIME_BOOTS_LEVEL_REQ = 8;



    // ============================================================
// WARDEN CHESTPLATE - T12 Warden Requirement (Best Armor)
// ============================================================
    public static final int WARDEN_CHESTPLATE_LEVEL_REQ = 12;


    // ============================================================
// HELPER: Check if any bounty item
// ============================================================
    public static boolean isAnyBountyItem(ItemStack stack) {
        return isSlayerSword(stack) ||
                isUpgradedSlayerSword(stack) ||
                isSlayerCore(stack) ||
                isZombieBerserkerHelmet(stack) ||
                isSpiderLeggings(stack) ||
                isSkeletonBow(stack) ||
                isSlimeBoots(stack) ||
                isWardenChestplate(stack);
    }

    public static boolean isZombieBerserkerHelmet(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return false;
        return name.getString().contains("Zombie Berserker Helmet");
    }

    public static boolean canUseZombieBerserkerHelmet(ServerPlayerEntity player) {
        int level = SlayerData.getSlayerLevel(player.getUuidAsString(), SlayerManager.SlayerType.ZOMBIE);
        return level >= ZOMBIE_BERSERKER_LEVEL_REQ;
    }

    // Update lore dynamically based on player level
    public static ItemStack createZombieBerserkerHelmetForPlayer(ServerPlayerEntity player) {
        ItemStack helmet = new ItemStack(Items.ZOMBIE_HEAD);

        int playerLevel = SlayerData.getSlayerLevel(player.getUuidAsString(), SlayerManager.SlayerType.ZOMBIE);
        boolean meetsRequirement = playerLevel >= BERSERKER_HELMET_LEVEL_REQ;

        helmet.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("☠ Zombie Berserker Helmet")
                        .formatted(Formatting.DARK_GREEN, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("LEGENDARY ARMOR").formatted(Formatting.GOLD, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("A cursed helm forged from the").formatted(Formatting.GRAY));
        lore.add(Text.literal("essence of fallen bounty bosses.").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal(""));
        lore.add(Text.literal("❤ Max Health: ").formatted(Formatting.WHITE)
                .append(Text.literal("-50%").formatted(Formatting.DARK_RED, Formatting.BOLD)));
        lore.add(Text.literal("⚔ Damage Dealt: ").formatted(Formatting.WHITE)
                .append(Text.literal("+300%").formatted(Formatting.GREEN, Formatting.BOLD)));

        // Only show requirement if not met [1]
        if (!meetsRequirement) {
            lore.add(Text.literal(""));
            lore.add(Text.literal("⚠ Requires: Zombie Bounty Lvl " + BERSERKER_HELMET_LEVEL_REQ)
                    .formatted(Formatting.RED));
            lore.add(Text.literal("⛔ LOCKED - Effects disabled").formatted(Formatting.DARK_RED));
        } else {
            lore.add(Text.literal(""));
            lore.add(Text.literal("✔ UNLOCKED").formatted(Formatting.GREEN, Formatting.BOLD));
        }

        helmet.set(DataComponentTypes.LORE, new LoreComponent(lore));
        helmet.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        return helmet;
    }

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


    // ============================================================
    // ARMOR STAT CONSTANTS
    // ============================================================
    // T1 Armor - Moderate stats
    private static final double T1_HELMET_ARMOR = 4.0;        // +1 over leather
    private static final double T1_HELMET_TOUGHNESS = 1.0;
    private static final double T1_HELMET_KNOCKBACK_RESIST = 0.05;

    private static final double T1_LEGGINGS_ARMOR = 7.0;      // +1 over leather
    private static final double T1_LEGGINGS_TOUGHNESS = 1.5;
    private static final double T1_LEGGINGS_KNOCKBACK_RESIST = 0.05;

    private static final double T1_BOOTS_ARMOR = 3.0;         // +2 over leather
    private static final double T1_BOOTS_TOUGHNESS = 1.0;
    private static final double T1_BOOTS_KNOCKBACK_RESIST = 0.05;

    // T2 Armor - High stats (Warden)
    private static final double T2_CHESTPLATE_ARMOR = 10.0;   // +2 over netherite
    private static final double T2_CHESTPLATE_TOUGHNESS = 4.0; // +1 over netherite
    private static final double T2_CHESTPLATE_KNOCKBACK_RESIST = 0.15;
    private static final double T2_CHESTPLATE_HEALTH_BOOST = 4.0; // +2 hearts


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

    public static ItemStack createZombieSword() {
        return createSlayerSword(SlayerManager.SlayerType.ZOMBIE);
    }

    public static ItemStack createSpiderSword() {
        return createSlayerSword(SlayerManager.SlayerType.SPIDER);
    }

    public static ItemStack createSkeletonSword() {
        return createSlayerSword(SlayerManager.SlayerType.SKELETON);
    }

    public static ItemStack createSlimeSword() {
        return createSlayerSword(SlayerManager.SlayerType.SLIME);
    }

    public static ItemStack createEndermanSword() {
        return createSlayerSword(SlayerManager.SlayerType.ENDERMAN);
    }

    public static ItemStack createTheGavel() {
        return createSlayerSword(SlayerManager.SlayerType.WARDEN);
    }

    public static ItemStack createZombieHelmet() {
        return createSlayerSword(SlayerManager.SlayerType.WARDEN);
    }

    // ============================================================
// ARMOR STATS CONFIGURATION
// ============================================================
    public static class ArmorStats {
        public final int armor;
        public final int toughness;
        public final double bossReduction;      // vs own boss type
        public final double allBossReduction;   // vs all bosses

        public ArmorStats(int armor, int toughness, double bossReduction, double allBossReduction) {
            this.armor = armor;
            this.toughness = toughness;
            this.bossReduction = bossReduction;
            this.allBossReduction = allBossReduction;
        }
    }

    public static ArmorStats getArmorStats(SlayerManager.SlayerType type, int tier) {
        return switch (type) {
            case ZOMBIE -> tier == 1 ? new ArmorStats(6, 1, 0.50, 0.10) : new ArmorStats(10, 3, 0.70, 0.20);
            case SPIDER -> tier == 1 ? new ArmorStats(6, 1, 0.50, 0.10) : new ArmorStats(10, 3, 0.70, 0.20);
            case SKELETON -> tier == 1 ? new ArmorStats(8, 2, 0.50, 0.10) : new ArmorStats(12, 4, 0.70, 0.25);
            case SLIME -> tier == 1 ? new ArmorStats(10, 3, 0.60, 0.15) : new ArmorStats(14, 5, 0.80, 0.30);
            case ENDERMAN -> tier == 1 ? new ArmorStats(10, 3, 0.60, 0.15) : new ArmorStats(14, 5, 0.80, 0.30);
            case WARDEN -> tier == 1 ? new ArmorStats(12, 4, 0.70, 0.20) : new ArmorStats(16, 6, 0.90, 0.35);
        };
    }

    // ============================================================
// SLAYER ARMOR - T1 & T2 WITH CUSTOM ATTRIBUTES
// ============================================================
    public static ItemStack createSlayerHelmet(SlayerManager.SlayerType type, int tier) {
        ArmorStats stats = getArmorStats(type, tier);
        ItemStack helmet = new ItemStack(tier == 1 ? Items.IRON_HELMET : Items.DIAMOND_HELMET);
        String tierName = tier == 1 ? "" : " II";

        helmet.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(type.displayName + " Slayer Helmet" + tierName)
                        .formatted(type.color, Formatting.BOLD));

        // CUSTOM ARMOR ATTRIBUTES
        AttributeModifiersComponent attributes = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "slayer_helmet_armor"),
                                stats.armor * 0.2,  // Helmet = 20% of total armor
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.HEAD)
                .add(EntityAttributes.ARMOR_TOUGHNESS,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "slayer_helmet_toughness"),
                                stats.toughness * 0.2,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.HEAD)
                .build();
        helmet.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attributes);

        List<Text> lore = buildSlayerArmorLore(type, tier, "Helmet", stats);
        helmet.set(DataComponentTypes.LORE, new LoreComponent(lore));

        if (tier >= 2) {
            helmet.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        }

        return helmet;
    }

    public static ItemStack createSlayerChestplate(SlayerManager.SlayerType type, int tier) {
        ArmorStats stats = getArmorStats(type, tier);
        ItemStack chestplate = new ItemStack(tier == 1 ? Items.IRON_CHESTPLATE : Items.DIAMOND_CHESTPLATE);
        String tierName = tier == 1 ? "" : " II";

        chestplate.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(type.displayName + " Slayer Chestplate" + tierName)
                        .formatted(type.color, Formatting.BOLD));

        // CUSTOM ARMOR ATTRIBUTES - Chestplate = 40% of total
        AttributeModifiersComponent attributes = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "slayer_chest_armor"),
                                stats.armor * 0.4,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.CHEST)
                .add(EntityAttributes.ARMOR_TOUGHNESS,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "slayer_chest_toughness"),
                                stats.toughness * 0.4,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.CHEST)
                .build();
        chestplate.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attributes);

        List<Text> lore = buildSlayerArmorLore(type, tier, "Chestplate", stats);
        chestplate.set(DataComponentTypes.LORE, new LoreComponent(lore));

        if (tier >= 2) {
            chestplate.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        }

        return chestplate;
    }

    public static ItemStack createSlayerLeggings(SlayerManager.SlayerType type, int tier) {
        ArmorStats stats = getArmorStats(type, tier);
        ItemStack leggings = new ItemStack(tier == 1 ? Items.IRON_LEGGINGS : Items.DIAMOND_LEGGINGS);
        String tierName = tier == 1 ? "" : " II";

        leggings.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(type.displayName + " Slayer Leggings" + tierName)
                        .formatted(type.color, Formatting.BOLD));

        // CUSTOM ARMOR ATTRIBUTES - Leggings = 30% of total
        AttributeModifiersComponent attributes = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "slayer_legs_armor"),
                                stats.armor * 0.3,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.LEGS)
                .add(EntityAttributes.ARMOR_TOUGHNESS,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "slayer_legs_toughness"),
                                stats.toughness * 0.3,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.LEGS)
                .build();
        leggings.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attributes);

        List<Text> lore = buildSlayerArmorLore(type, tier, "Leggings", stats);
        leggings.set(DataComponentTypes.LORE, new LoreComponent(lore));

        if (tier >= 2) {
            leggings.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        }

        return leggings;
    }

    public static ItemStack createSlayerBoots(SlayerManager.SlayerType type, int tier) {
        ArmorStats stats = getArmorStats(type, tier);
        ItemStack boots = new ItemStack(tier == 1 ? Items.IRON_BOOTS : Items.DIAMOND_BOOTS);
        String tierName = tier == 1 ? "" : " II";

        boots.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(type.displayName + " Slayer Boots" + tierName)
                        .formatted(type.color, Formatting.BOLD));

        // CUSTOM ARMOR ATTRIBUTES - Boots = 10% of total
        AttributeModifiersComponent attributes = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "slayer_boots_armor"),
                                stats.armor * 0.1,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.FEET)
                .add(EntityAttributes.ARMOR_TOUGHNESS,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "slayer_boots_toughness"),
                                stats.toughness * 0.1,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.FEET)
                .build();
        boots.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attributes);

        List<Text> lore = buildSlayerArmorLore(type, tier, "Boots", stats);
        boots.set(DataComponentTypes.LORE, new LoreComponent(lore));

        if (tier >= 2) {
            boots.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        }

        return boots;
    }

    private static List<Text> buildSlayerArmorLore(SlayerManager.SlayerType type, int tier, String piece, ArmorStats stats) {
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));

        if (tier == 1) {
            lore.add(Text.literal("RARE ARMOR").formatted(Formatting.BLUE, Formatting.BOLD));
        } else {
            lore.add(Text.literal("LEGENDARY ARMOR").formatted(Formatting.GOLD, Formatting.BOLD));
        }

        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(type.color));
        lore.add(Text.literal("🛡 Armor: +" + stats.armor).formatted(Formatting.BLUE));
        lore.add(Text.literal("🛡 Toughness: +" + stats.toughness).formatted(Formatting.BLUE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal(""));

        int bossReductionPercent = (int) (stats.bossReduction * 100);
        int allReductionPercent = (int) (stats.allBossReduction * 100);

        lore.add(Text.literal("vs " + type.displayName + " Bosses: ")
                .formatted(Formatting.GRAY)
                .append(Text.literal("-" + bossReductionPercent + "% damage")
                        .formatted(Formatting.GREEN, Formatting.BOLD)));
        lore.add(Text.literal("vs All Bosses: ")
                .formatted(Formatting.GRAY)
                .append(Text.literal("-" + allReductionPercent + "% damage")
                        .formatted(Formatting.GREEN)));

        if (tier >= 2) {
            lore.add(Text.literal("+15% XP from " + type.displayName + " bounties").formatted(Formatting.AQUA));
        }

        lore.add(Text.literal(""));
        int reqLevel = tier == 1 ? T1_ARMOR_LEVEL_REQ : T2_ARMOR_LEVEL_REQ;
        lore.add(Text.literal("⚠ Requires: " + type.displayName + " Bounty Lvl " + reqLevel)
                .formatted(Formatting.RED));

        return lore;
    }

// ============================================================
// SPECIAL ARMOR PIECES (Fixed with custom attributes)
// ============================================================

    public static ItemStack createWardenChestplate() {
        ItemStack chestplate = new ItemStack(Items.NETHERITE_CHESTPLATE);

        chestplate.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("💀 Sculk Terror Chestplate")
                        .formatted(Formatting.DARK_AQUA, Formatting.BOLD));

        // CUSTOM ARMOR ATTRIBUTES
        AttributeModifiersComponent attributes = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "warden_armor"),
                                12.0,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.CHEST)
                .add(EntityAttributes.ARMOR_TOUGHNESS,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "warden_toughness"),
                                4.0,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.CHEST)
                .add(EntityAttributes.MAX_HEALTH,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "warden_health"),
                                20.0,  // +10 hearts
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.CHEST)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "warden_knockback"),
                                1.0,  // Full knockback immunity
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.CHEST)
                .build();
        chestplate.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attributes);

        // Durability - 10x netherite
        chestplate.set(DataComponentTypes.MAX_DAMAGE, 4070);
        chestplate.set(DataComponentTypes.DAMAGE, 0);

        // FIXED LORE
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("MYTHIC ARMOR").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Forged in the deep dark from").formatted(Formatting.GRAY));
        lore.add(Text.literal("the heart of The Sculk Terror.").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_AQUA));
        lore.add(Text.literal("🛡 Armor: +12").formatted(Formatting.BLUE));
        lore.add(Text.literal("🛡 Toughness: +4").formatted(Formatting.BLUE));
        lore.add(Text.literal("❤ Health: +10 Hearts").formatted(Formatting.RED));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.DARK_AQUA));
        lore.add(Text.literal("👁 ESP: See noisy entities (64 blocks)").formatted(Formatting.AQUA));
        lore.add(Text.literal("🌙 Night Vision: Permanent").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🛡 Darkness Immunity").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("🛡 Full Knockback Resistance").formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ BOSS DEFENSE ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("vs Warden Bosses: ").formatted(Formatting.GRAY)
                .append(Text.literal("-90% damage").formatted(Formatting.GREEN, Formatting.BOLD)));
        lore.add(Text.literal("vs All Bosses: ").formatted(Formatting.GRAY)
                .append(Text.literal("-35% damage").formatted(Formatting.GREEN)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires: Warden Bounty Lvl " + WARDEN_CHESTPLATE_LEVEL_REQ)
                .formatted(Formatting.RED));

        chestplate.set(DataComponentTypes.LORE, new LoreComponent(lore));
        chestplate.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        return chestplate;
    }

    public static ItemStack createSpiderLeggings() {
        ItemStack leggings = new ItemStack(Items.LEATHER_LEGGINGS);

        leggings.set(DataComponentTypes.DYED_COLOR,
                new net.minecraft.component.type.DyedColorComponent(0x8B0000));

        leggings.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🕷 Venomous Crawler Leggings")
                        .formatted(Formatting.DARK_RED, Formatting.BOLD));

        // CUSTOM ARMOR ATTRIBUTES
        AttributeModifiersComponent attributes = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "spider_legs_armor"),
                                8.0,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.LEGS)
                .add(EntityAttributes.ARMOR_TOUGHNESS,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "spider_legs_toughness"),
                                3.0,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.LEGS)
                .add(EntityAttributes.MOVEMENT_SPEED,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "spider_speed"),
                                0.05,  // +50% speed
                                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                        ),
                        AttributeModifierSlot.LEGS)
                .build();
        leggings.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attributes);

        // Durability
        leggings.set(DataComponentTypes.MAX_DAMAGE, 750);
        leggings.set(DataComponentTypes.DAMAGE, 0);

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("LEGENDARY ARMOR").formatted(Formatting.GOLD, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Woven from the silk of").formatted(Formatting.GRAY));
        lore.add(Text.literal("a thousand slain bounty spiders.").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_RED));
        lore.add(Text.literal("🛡 Armor: +8").formatted(Formatting.BLUE));
        lore.add(Text.literal("🛡 Toughness: +3").formatted(Formatting.BLUE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.DARK_RED));
        lore.add(Text.literal("🕸 Web Immunity: Walk through webs").formatted(Formatting.WHITE));
        lore.add(Text.literal("☠ Poison Immunity").formatted(Formatting.GREEN));
        lore.add(Text.literal("⚡ Movement Speed: +50%").formatted(Formatting.AQUA));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ BOSS DEFENSE ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("vs Spider Bosses: ").formatted(Formatting.GRAY)
                .append(Text.literal("-75% damage").formatted(Formatting.GREEN, Formatting.BOLD)));
        lore.add(Text.literal("vs All Bosses: ").formatted(Formatting.GRAY)
                .append(Text.literal("-15% damage").formatted(Formatting.GREEN)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires: Spider Bounty Lvl " + SPIDER_LEGGINGS_LEVEL_REQ)
                .formatted(Formatting.RED));

        leggings.set(DataComponentTypes.LORE, new LoreComponent(lore));
        leggings.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        return leggings;
    }

    public static ItemStack createSlimeBoots() {
        ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);

        boots.set(DataComponentTypes.DYED_COLOR,
                new net.minecraft.component.type.DyedColorComponent(0x7CFC00));

        boots.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🟢 Gelatinous Rustler Boots")
                        .formatted(Formatting.GREEN, Formatting.BOLD));

        // CUSTOM ARMOR ATTRIBUTES
        AttributeModifiersComponent attributes = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "slime_boots_armor"),
                                4.0,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.FEET)
                .add(EntityAttributes.ARMOR_TOUGHNESS,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "slime_boots_toughness"),
                                2.0,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.FEET)
                .add(EntityAttributes.SAFE_FALL_DISTANCE,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "slime_fall"),
                                100.0,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.FEET)
                .build();
        boots.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attributes);

        // Durability
        boots.set(DataComponentTypes.MAX_DAMAGE, 650);
        boots.set(DataComponentTypes.DAMAGE, 0);

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("LEGENDARY ARMOR").formatted(Formatting.GOLD, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Bouncy boots made from").formatted(Formatting.GRAY));
        lore.add(Text.literal("condensed slime.").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Some say it saves you").formatted(Formatting.DARK_AQUA, Formatting.OBFUSCATED, Formatting.ITALIC));
        lore.add(Text.literal("from death").formatted(Formatting.DARK_AQUA, Formatting.OBFUSCATED, Formatting.ITALIC));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.GREEN));
        lore.add(Text.literal("🛡 Armor: +4").formatted(Formatting.BLUE));
        lore.add(Text.literal("🛡 Toughness: +2").formatted(Formatting.BLUE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.GREEN));
        lore.add(Text.literal("⬆ Jump Boost II").formatted(Formatting.AQUA));
        lore.add(Text.literal("🛡 No Fall Damage").formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ BOSS DEFENSE ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("vs Slime Bosses: ").formatted(Formatting.GRAY)
                .append(Text.literal("-75% damage").formatted(Formatting.GREEN, Formatting.BOLD)));
        lore.add(Text.literal("vs All Bosses: ").formatted(Formatting.GRAY)
                .append(Text.literal("-15% damage").formatted(Formatting.GREEN)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires: Slime Bounty Lvl " + SLIME_BOOTS_LEVEL_REQ)
                .formatted(Formatting.RED));
        boots.set(DataComponentTypes.LORE, new LoreComponent(lore));
        boots.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        return boots;
    }

// ============================================================
// T2 ARMOR SYSTEM - Tiered armor per bounty type
// Higher stats than T1, better boss defense
// ============================================================



    // ============================================================
// T2 ZOMBIE ARMOR SET
// ============================================================
    public static ItemStack createT2ZombieHelmet() {
        ItemStack helmet = new ItemStack(Items.NETHERITE_HELMET);
        helmet.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Undying Outlaw Helmet II")
                        .formatted(Formatting.DARK_GREEN, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("T2 BOUNTY ARMOR").formatted(Formatting.DARK_GREEN, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+5").formatted(Formatting.GREEN)));
        lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+3").formatted(Formatting.GREEN)));
        lore.add(Text.literal("❤ Health Boost: ").formatted(Formatting.WHITE)
                .append(Text.literal("+4").formatted(Formatting.GREEN)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ VS BOSSES ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("🛡 Zombie Boss: ").formatted(Formatting.WHITE)
                .append(Text.literal("-20%").formatted(Formatting.AQUA)));
        lore.add(Text.literal("🛡 Other Bosses: ").formatted(Formatting.WHITE)
                .append(Text.literal("-5%").formatted(Formatting.GRAY)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires: Zombie Bounty Lvl " + T2_ARMOR_LEVEL_REQ)
                .formatted(Formatting.RED));

        helmet.set(DataComponentTypes.LORE, new LoreComponent(lore));
        helmet.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return helmet;
    }

    public static ItemStack createT2ZombieChestplate() {
        ItemStack chestplate = new ItemStack(Items.NETHERITE_CHESTPLATE);
        chestplate.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Undying Outlaw Chestplate II")
                        .formatted(Formatting.DARK_GREEN, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("T2 BOUNTY ARMOR").formatted(Formatting.DARK_GREEN, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+10").formatted(Formatting.GREEN)));
        lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+4").formatted(Formatting.GREEN)));
        lore.add(Text.literal("❤ Health Boost: ").formatted(Formatting.WHITE)
                .append(Text.literal("+6").formatted(Formatting.GREEN)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ VS BOSSES ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("🛡 Zombie Boss: ").formatted(Formatting.WHITE)
                .append(Text.literal("-25%").formatted(Formatting.AQUA)));
        lore.add(Text.literal("🛡 Other Bosses: ").formatted(Formatting.WHITE)
                .append(Text.literal("-8%").formatted(Formatting.GRAY)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires: Zombie Bounty Lvl " + T2_ARMOR_LEVEL_REQ)
                .formatted(Formatting.RED));

        chestplate.set(DataComponentTypes.LORE, new LoreComponent(lore));
        chestplate.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return chestplate;
    }

    public static ItemStack createT2ZombieLeggings() {
        ItemStack leggings = new ItemStack(Items.NETHERITE_LEGGINGS);
        leggings.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Undying Outlaw Leggings II")
                        .formatted(Formatting.DARK_GREEN, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("T2 BOUNTY ARMOR").formatted(Formatting.DARK_GREEN, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+8").formatted(Formatting.GREEN)));
        lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+3").formatted(Formatting.GREEN)));
        lore.add(Text.literal("❤ Health Boost: ").formatted(Formatting.WHITE)
                .append(Text.literal("+4").formatted(Formatting.GREEN)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ VS BOSSES ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("🛡 Zombie Boss: ").formatted(Formatting.WHITE)
                .append(Text.literal("-20%").formatted(Formatting.AQUA)));
        lore.add(Text.literal("🛡 Other Bosses: ").formatted(Formatting.WHITE)
                .append(Text.literal("-5%").formatted(Formatting.GRAY)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires: Zombie Bounty Lvl " + T2_ARMOR_LEVEL_REQ)
                .formatted(Formatting.RED));

        leggings.set(DataComponentTypes.LORE, new LoreComponent(lore));
        leggings.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return leggings;
    }

    public static ItemStack createT2ZombieBoots() {
        ItemStack boots = new ItemStack(Items.NETHERITE_BOOTS);
        boots.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Undying Outlaw Boots II")
                        .formatted(Formatting.DARK_GREEN, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("T2 BOUNTY ARMOR").formatted(Formatting.DARK_GREEN, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+5").formatted(Formatting.GREEN)));
        lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+3").formatted(Formatting.GREEN)));
        lore.add(Text.literal("🔰 Knockback Resist: ").formatted(Formatting.WHITE)
                .append(Text.literal("+20%").formatted(Formatting.GREEN)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ VS BOSSES ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("🛡 Zombie Boss: ").formatted(Formatting.WHITE)
                .append(Text.literal("-15%").formatted(Formatting.AQUA)));
        lore.add(Text.literal("🛡 Other Bosses: ").formatted(Formatting.WHITE)
                .append(Text.literal("-5%").formatted(Formatting.GRAY)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires: Zombie Bounty Lvl " + T2_ARMOR_LEVEL_REQ)
                .formatted(Formatting.RED));

        boots.set(DataComponentTypes.LORE, new LoreComponent(lore));
        boots.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return boots;
    }

    // Detection methods for T2 armor
    public static boolean isT2Armor(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return false;
        String n = name.getString();
        return n.contains(" II") && (n.contains("Outlaw") || n.contains("Bandit") ||
                n.contains("Desperado") || n.contains("Rustler") || n.contains("Phantom") || n.contains("Terror"));
    }

    public static SlayerManager.SlayerType getT2ArmorType(ItemStack stack) {
        if (!isT2Armor(stack)) return null;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return null;
        String n = name.getString();

        if (n.contains("Undying") || n.contains("Outlaw")) return SlayerManager.SlayerType.ZOMBIE;
        if (n.contains("Venomous") || n.contains("Bandit")) return SlayerManager.SlayerType.SPIDER;
        if (n.contains("Bone") || n.contains("Desperado")) return SlayerManager.SlayerType.SKELETON;
        if (n.contains("Gelatinous") || n.contains("Rustler")) return SlayerManager.SlayerType.SLIME;
        if (n.contains("Void") || n.contains("Phantom")) return SlayerManager.SlayerType.ENDERMAN;
        if (n.contains("Sculk") || n.contains("Terror")) return SlayerManager.SlayerType.WARDEN;

        return null;
    }

    public static boolean canUseT2Armor(ServerPlayerEntity player, SlayerManager.SlayerType type) {
        int level = SlayerData.getSlayerLevel(player.getUuidAsString(), type);
        return level >= T2_ARMOR_LEVEL_REQ;
    }

    // ============================================================
// Generic T2 Armor Creation for any type
// ============================================================
    public static ItemStack createT2Helmet(SlayerManager.SlayerType type) {
        return switch (type) {
            case ZOMBIE -> createT2ZombieHelmet();
            case SPIDER -> createT2SpiderHelmet();
            case SKELETON -> createT2SkeletonHelmet();
            case SLIME -> createT2SlimeHelmet();
            case ENDERMAN -> createT2EndermanHelmet();
            case WARDEN -> createT2WardenHelmet();
        };
    }

    public static ItemStack createT2Chestplate(SlayerManager.SlayerType type) {
        return switch (type) {
            case ZOMBIE -> createT2ZombieChestplate();
            case SPIDER -> createT2SpiderChestplate();
            case SKELETON -> createT2SkeletonChestplate();
            case SLIME -> createT2SlimeChestplate();
            case ENDERMAN -> createT2EndermanChestplate();
            case WARDEN -> createT2WardenChestplate();
        };
    }

    public static ItemStack createT2Leggings(SlayerManager.SlayerType type) {
        return switch (type) {
            case ZOMBIE -> createT2ZombieLeggings();
            case SPIDER -> createT2SpiderLeggings();
            case SKELETON -> createT2SkeletonLeggings();
            case SLIME -> createT2SlimeLeggings();
            case ENDERMAN -> createT2EndermanLeggings();
            case WARDEN -> createT2WardenLeggings();
        };
    }

    public static ItemStack createT2Boots(SlayerManager.SlayerType type) {
        return switch (type) {
            case ZOMBIE -> createT2ZombieBoots();
            case SPIDER -> createT2SpiderBoots();
            case SKELETON -> createT2SkeletonBoots();
            case SLIME -> createT2SlimeBoots();
            case ENDERMAN -> createT2EndermanBoots();
            case WARDEN -> createT2WardenBoots();
        };
    }

    // ============================================================
// T2 SPIDER ARMOR SET
// ============================================================
    public static ItemStack createT2SpiderHelmet() {
        ItemStack helmet = new ItemStack(Items.NETHERITE_HELMET);
        helmet.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Venomous Bandit Helmet II")
                        .formatted(Formatting.DARK_RED, Formatting.BOLD));

        List<Text> lore = createT2ArmorLore("T2 BOUNTY ARMOR", Formatting.DARK_RED,
                "+5", "+3", null, "+10%",
                "Spider Boss", "-20%", "-5%",
                "Spider", T2_ARMOR_LEVEL_REQ);

        helmet.set(DataComponentTypes.LORE, new LoreComponent(lore));
        helmet.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return helmet;
    }

    public static ItemStack createT2SpiderChestplate() {
        ItemStack chestplate = new ItemStack(Items.NETHERITE_CHESTPLATE);
        chestplate.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Venomous Bandit Chestplate II")
                        .formatted(Formatting.DARK_RED, Formatting.BOLD));

        List<Text> lore = createT2ArmorLore("T2 BOUNTY ARMOR", Formatting.DARK_RED,
                "+10", "+4", "+6", null,
                "Spider Boss", "-25%", "-8%",
                "Spider", T2_ARMOR_LEVEL_REQ);

        chestplate.set(DataComponentTypes.LORE, new LoreComponent(lore));
        chestplate.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return chestplate;
    }

    public static ItemStack createT2SpiderLeggings() {
        ItemStack leggings = new ItemStack(Items.NETHERITE_LEGGINGS);
        leggings.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Venomous Bandit Leggings II")
                        .formatted(Formatting.DARK_RED, Formatting.BOLD));

        List<Text> lore = createT2ArmorLore("T2 BOUNTY ARMOR", Formatting.DARK_RED,
                "+8", "+3", "+4", null,
                "Spider Boss", "-20%", "-5%",
                "Spider", T2_ARMOR_LEVEL_REQ);

        leggings.set(DataComponentTypes.LORE, new LoreComponent(lore));
        leggings.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return leggings;
    }

    public static ItemStack createT2SpiderBoots() {
        ItemStack boots = new ItemStack(Items.NETHERITE_BOOTS);
        boots.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Venomous Bandit Boots II")
                        .formatted(Formatting.DARK_RED, Formatting.BOLD));

        List<Text> lore = createT2ArmorLore("T2 BOUNTY ARMOR", Formatting.DARK_RED,
                "+5", "+3", null, "+15%",
                "Spider Boss", "-15%", "-5%",
                "Spider", T2_ARMOR_LEVEL_REQ);

        boots.set(DataComponentTypes.LORE, new LoreComponent(lore));
        boots.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return boots;
    }

    // Helper method to create consistent T2 armor lore
    private static List<Text> createT2ArmorLore(String title, Formatting titleColor,
                                                String armor, String toughness, String health, String speed,
                                                String matchBoss, String matchReduction, String otherReduction,
                                                String bountyType, int levelReq) {

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal(title).formatted(titleColor, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.RED));

        if (armor != null) {
            lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                    .append(Text.literal(armor).formatted(Formatting.GREEN)));
        }
        if (toughness != null) {
            lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                    .append(Text.literal(toughness).formatted(Formatting.GREEN)));
        }
        if (health != null) {
            lore.add(Text.literal("❤ Health Boost: ").formatted(Formatting.WHITE)
                    .append(Text.literal(health).formatted(Formatting.GREEN)));
        }
        if (speed != null) {
            lore.add(Text.literal("⚡ Speed: ").formatted(Formatting.WHITE)
                    .append(Text.literal(speed).formatted(Formatting.GREEN)));
        }

        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ VS BOSSES ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("🛡 " + matchBoss + ": ").formatted(Formatting.WHITE)
                .append(Text.literal(matchReduction).formatted(Formatting.AQUA)));
        lore.add(Text.literal("🛡 Other Bosses: ").formatted(Formatting.WHITE)
                .append(Text.literal(otherReduction).formatted(Formatting.GRAY)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires: " + bountyType + " Bounty Lvl " + levelReq)
                .formatted(Formatting.RED));

        return lore;
    }
    // ============================================================
// T2 SKELETON ARMOR SET - Bone Desperado II
// ============================================================
    public static ItemStack createT2SkeletonHelmet() {
        ItemStack helmet = new ItemStack(Items.NETHERITE_HELMET);
        helmet.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Bone Desperado Helmet II")
                        .formatted(Formatting.WHITE, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("T2 BOUNTY ARMOR").formatted(Formatting.WHITE, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+5").formatted(Formatting.GREEN)));
        lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+3").formatted(Formatting.GREEN)));
        lore.add(Text.literal("🏹 Projectile Resist: ").formatted(Formatting.WHITE)
                .append(Text.literal("+15%").formatted(Formatting.GREEN)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ VS BOSSES ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("🛡 Skeleton Boss: ").formatted(Formatting.WHITE)
                .append(Text.literal("-20%").formatted(Formatting.AQUA)));
        lore.add(Text.literal("🛡 Other Bosses: ").formatted(Formatting.WHITE)
                .append(Text.literal("-5%").formatted(Formatting.GRAY)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires: Skeleton Bounty Lvl " + T2_ARMOR_LEVEL_REQ)
                .formatted(Formatting.RED));

        helmet.set(DataComponentTypes.LORE, new LoreComponent(lore));
        helmet.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return helmet;
    }

    public static ItemStack createT2SkeletonChestplate() {
        ItemStack chestplate = new ItemStack(Items.NETHERITE_CHESTPLATE);
        chestplate.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Bone Desperado Chestplate II")
                        .formatted(Formatting.WHITE, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("T2 BOUNTY ARMOR").formatted(Formatting.WHITE, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+10").formatted(Formatting.GREEN)));
        lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+4").formatted(Formatting.GREEN)));
        lore.add(Text.literal("🏹 Projectile Resist: ").formatted(Formatting.WHITE)
                .append(Text.literal("+25%").formatted(Formatting.GREEN)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ VS BOSSES ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("🛡 Skeleton Boss: ").formatted(Formatting.WHITE)
                .append(Text.literal("-25%").formatted(Formatting.AQUA)));
        lore.add(Text.literal("🛡 Other Bosses: ").formatted(Formatting.WHITE)
                .append(Text.literal("-8%").formatted(Formatting.GRAY)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires: Skeleton Bounty Lvl " + T2_ARMOR_LEVEL_REQ)
                .formatted(Formatting.RED));

        chestplate.set(DataComponentTypes.LORE, new LoreComponent(lore));
        chestplate.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return chestplate;
    }

    public static ItemStack createT2SkeletonLeggings() {
        ItemStack leggings = new ItemStack(Items.NETHERITE_LEGGINGS);
        leggings.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Bone Desperado Leggings II")
                        .formatted(Formatting.WHITE, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("T2 BOUNTY ARMOR").formatted(Formatting.WHITE, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+8").formatted(Formatting.GREEN)));
        lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+3").formatted(Formatting.GREEN)));
        lore.add(Text.literal("🏹 Projectile Resist: ").formatted(Formatting.WHITE)
                .append(Text.literal("+20%").formatted(Formatting.GREEN)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ VS BOSSES ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("🛡 Skeleton Boss: ").formatted(Formatting.WHITE)
                .append(Text.literal("-20%").formatted(Formatting.AQUA)));
        lore.add(Text.literal("🛡 Other Bosses: ").formatted(Formatting.WHITE)
                .append(Text.literal("-5%").formatted(Formatting.GRAY)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires: Skeleton Bounty Lvl " + T2_ARMOR_LEVEL_REQ)
                .formatted(Formatting.RED));

        leggings.set(DataComponentTypes.LORE, new LoreComponent(lore));
        leggings.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return leggings;
    }

    public static ItemStack createT2SkeletonBoots() {
        ItemStack boots = new ItemStack(Items.NETHERITE_BOOTS);
        boots.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Bone Desperado Boots II")
                        .formatted(Formatting.WHITE, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("T2 BOUNTY ARMOR").formatted(Formatting.WHITE, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+5").formatted(Formatting.GREEN)));
        lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+3").formatted(Formatting.GREEN)));
        lore.add(Text.literal("⚡ Speed: ").formatted(Formatting.WHITE)
                .append(Text.literal("+10%").formatted(Formatting.GREEN)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ VS BOSSES ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("🛡 Skeleton Boss: ").formatted(Formatting.WHITE)
                .append(Text.literal("-15%").formatted(Formatting.AQUA)));
        lore.add(Text.literal("🛡 Other Bosses: ").formatted(Formatting.WHITE)
                .append(Text.literal("-5%").formatted(Formatting.GRAY)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires: Skeleton Bounty Lvl " + T2_ARMOR_LEVEL_REQ)
                .formatted(Formatting.RED));

        boots.set(DataComponentTypes.LORE, new LoreComponent(lore));
        boots.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return boots;
    }
    // ============================================================
// T2 SLIME ARMOR SET - Gelatinous Rustler II
// ============================================================
    public static ItemStack createT2SlimeHelmet() {
        ItemStack helmet = new ItemStack(Items.NETHERITE_HELMET);
        helmet.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Gelatinous Rustler Helmet II")
                        .formatted(Formatting.GREEN, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("T2 BOUNTY ARMOR").formatted(Formatting.GREEN, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+5").formatted(Formatting.GREEN)));
        lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+3").formatted(Formatting.GREEN)));
        lore.add(Text.literal("❤ Health Boost: ").formatted(Formatting.WHITE)
                .append(Text.literal("+6").formatted(Formatting.GREEN)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ VS BOSSES ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("🛡 Slime Boss: ").formatted(Formatting.WHITE)
                .append(Text.literal("-20%").formatted(Formatting.AQUA)));
        lore.add(Text.literal("🛡 Other Bosses: ").formatted(Formatting.WHITE)
                .append(Text.literal("-5%").formatted(Formatting.GRAY)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires: Slime Bounty Lvl " + T2_ARMOR_LEVEL_REQ)
                .formatted(Formatting.RED));

        helmet.set(DataComponentTypes.LORE, new LoreComponent(lore));
        helmet.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return helmet;
    }

    public static ItemStack createT2SlimeChestplate() {
        ItemStack chestplate = new ItemStack(Items.NETHERITE_CHESTPLATE);
        chestplate.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Gelatinous Rustler Chestplate II")
                        .formatted(Formatting.GREEN, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("T2 BOUNTY ARMOR").formatted(Formatting.GREEN, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+10").formatted(Formatting.GREEN)));
        lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+4").formatted(Formatting.GREEN)));
        lore.add(Text.literal("❤ Health Boost: ").formatted(Formatting.WHITE)
                .append(Text.literal("+10").formatted(Formatting.GREEN)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ VS BOSSES ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("🛡 Slime Boss: ").formatted(Formatting.WHITE)
                .append(Text.literal("-25%").formatted(Formatting.AQUA)));
        lore.add(Text.literal("🛡 Other Bosses: ").formatted(Formatting.WHITE)
                .append(Text.literal("-8%").formatted(Formatting.GRAY)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires: Slime Bounty Lvl " + T2_ARMOR_LEVEL_REQ)
                .formatted(Formatting.RED));

        chestplate.set(DataComponentTypes.LORE, new LoreComponent(lore));
        chestplate.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return chestplate;
    }

    public static ItemStack createT2SlimeLeggings() {
        ItemStack leggings = new ItemStack(Items.NETHERITE_LEGGINGS);
        leggings.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Gelatinous Rustler Leggings II")
                        .formatted(Formatting.GREEN, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("T2 BOUNTY ARMOR").formatted(Formatting.GREEN, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+8").formatted(Formatting.GREEN)));
        lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+3").formatted(Formatting.GREEN)));
        lore.add(Text.literal("❤ Health Boost: ").formatted(Formatting.WHITE)
                .append(Text.literal("+6").formatted(Formatting.GREEN)));
        lore.add(Text.literal("⬆ Jump Boost: ").formatted(Formatting.WHITE)
                .append(Text.literal("+1").formatted(Formatting.GREEN)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ VS BOSSES ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("🛡 Slime Boss: ").formatted(Formatting.WHITE)
                .append(Text.literal("-20%").formatted(Formatting.AQUA)));
        lore.add(Text.literal("🛡 Other Bosses: ").formatted(Formatting.WHITE)
                .append(Text.literal("-5%").formatted(Formatting.GRAY)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires: Slime Bounty Lvl " + T2_ARMOR_LEVEL_REQ)
                .formatted(Formatting.RED));

        leggings.set(DataComponentTypes.LORE, new LoreComponent(lore));
        leggings.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return leggings;
    }

    public static ItemStack createT2SlimeBoots() {
        ItemStack boots = new ItemStack(Items.NETHERITE_BOOTS);
        boots.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Gelatinous Rustler Boots II")
                        .formatted(Formatting.GREEN, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("T2 BOUNTY ARMOR").formatted(Formatting.GREEN, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+5").formatted(Formatting.GREEN)));
        lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+3").formatted(Formatting.GREEN)));
        lore.add(Text.literal("🪶 No Fall Damage").formatted(Formatting.GREEN));
        lore.add(Text.literal("⬆ Jump Boost: ").formatted(Formatting.WHITE)
                .append(Text.literal("+2").formatted(Formatting.GREEN)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ VS BOSSES ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("🛡 Slime Boss: ").formatted(Formatting.WHITE)
                .append(Text.literal("-15%").formatted(Formatting.AQUA)));
        lore.add(Text.literal("🛡 Other Bosses: ").formatted(Formatting.WHITE)
                .append(Text.literal("-5%").formatted(Formatting.GRAY)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires: Slime Bounty Lvl " + T2_ARMOR_LEVEL_REQ)
                .formatted(Formatting.RED));

        boots.set(DataComponentTypes.LORE, new LoreComponent(lore));
        boots.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return boots;
    }
    // ============================================================
// T2 ENDERMAN ARMOR SET - Void Phantom II
// ============================================================
    public static ItemStack createT2EndermanHelmet() {
        ItemStack helmet = new ItemStack(Items.NETHERITE_HELMET);
        helmet.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Void Phantom Helmet II")
                        .formatted(Formatting.DARK_PURPLE, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("T2 BOUNTY ARMOR").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+5").formatted(Formatting.GREEN)));
        lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+3").formatted(Formatting.GREEN)));
        lore.add(Text.literal("⚡ Speed: ").formatted(Formatting.WHITE)
                .append(Text.literal("+15%").formatted(Formatting.GREEN)));
        lore.add(Text.literal("👁 Night Vision").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ VS BOSSES ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("🛡 Enderman Boss: ").formatted(Formatting.WHITE)
                .append(Text.literal("-20%").formatted(Formatting.AQUA)));
        lore.add(Text.literal("🛡 Other Bosses: ").formatted(Formatting.WHITE)
                .append(Text.literal("-5%").formatted(Formatting.GRAY)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires: Enderman Bounty Lvl " + T2_ARMOR_LEVEL_REQ)
                .formatted(Formatting.RED));

        helmet.set(DataComponentTypes.LORE, new LoreComponent(lore));
        helmet.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return helmet;
    }

    public static ItemStack createT2EndermanChestplate() {
        ItemStack chestplate = new ItemStack(Items.NETHERITE_CHESTPLATE);
        chestplate.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Void Phantom Chestplate II")
                        .formatted(Formatting.DARK_PURPLE, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("T2 BOUNTY ARMOR").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+10").formatted(Formatting.GREEN)));
        lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+4").formatted(Formatting.GREEN)));
        lore.add(Text.literal("⚡ Speed: ").formatted(Formatting.WHITE)
                .append(Text.literal("+10%").formatted(Formatting.GREEN)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ ABILITIES ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("☍ Teleport Dodge: ").formatted(Formatting.WHITE)
                .append(Text.literal("20% chance").formatted(Formatting.LIGHT_PURPLE)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ VS BOSSES ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("🛡 Enderman Boss: ").formatted(Formatting.WHITE)
                .append(Text.literal("-25%").formatted(Formatting.AQUA)));
        lore.add(Text.literal("🛡 Other Bosses: ").formatted(Formatting.WHITE)
                .append(Text.literal("-8%").formatted(Formatting.GRAY)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires: Enderman Bounty Lvl " + T2_ARMOR_LEVEL_REQ)
                .formatted(Formatting.RED));

        chestplate.set(DataComponentTypes.LORE, new LoreComponent(lore));
        chestplate.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return chestplate;
    }

    public static ItemStack createT2EndermanLeggings() {
        ItemStack leggings = new ItemStack(Items.NETHERITE_LEGGINGS);
        leggings.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Void Phantom Leggings II")
                        .formatted(Formatting.DARK_PURPLE, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("T2 BOUNTY ARMOR").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+8").formatted(Formatting.GREEN)));
        lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+3").formatted(Formatting.GREEN)));
        lore.add(Text.literal("⚡ Speed: ").formatted(Formatting.WHITE)
                .append(Text.literal("+10%").formatted(Formatting.GREEN)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ VS BOSSES ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("🛡 Enderman Boss: ").formatted(Formatting.WHITE)
                .append(Text.literal("-20%").formatted(Formatting.AQUA)));
        lore.add(Text.literal("🛡 Other Bosses: ").formatted(Formatting.WHITE)
                .append(Text.literal("-5%").formatted(Formatting.GRAY)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires: Enderman Bounty Lvl " + T2_ARMOR_LEVEL_REQ)
                .formatted(Formatting.RED));

        leggings.set(DataComponentTypes.LORE, new LoreComponent(lore));
        leggings.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return leggings;
    }

    public static ItemStack createT2EndermanBoots() {
        ItemStack boots = new ItemStack(Items.NETHERITE_BOOTS);
        boots.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Void Phantom Boots II")
                        .formatted(Formatting.DARK_PURPLE, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("T2 BOUNTY ARMOR").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+5").formatted(Formatting.GREEN)));
        lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+3").formatted(Formatting.GREEN)));
        lore.add(Text.literal("⚡ Speed: ").formatted(Formatting.WHITE)
                .append(Text.literal("+20%").formatted(Formatting.GREEN)));
        lore.add(Text.literal("🪶 No Fall Damage").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ VS BOSSES ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("🛡 Enderman Boss: ").formatted(Formatting.WHITE)
                .append(Text.literal("-15%").formatted(Formatting.AQUA)));
        lore.add(Text.literal("🛡 Other Bosses: ").formatted(Formatting.WHITE)
                .append(Text.literal("-5%").formatted(Formatting.GRAY)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires: Enderman Bounty Lvl " + T2_ARMOR_LEVEL_REQ)
                .formatted(Formatting.RED));

        boots.set(DataComponentTypes.LORE, new LoreComponent(lore));
        boots.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return boots;
    }
    // ============================================================
// T2 WARDEN ARMOR SET - Sculk Terror II (Best T2 armor)
// ============================================================
    public static ItemStack createT2WardenHelmet() {
        ItemStack helmet = new ItemStack(Items.NETHERITE_HELMET);
        helmet.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Sculk Terror Helmet II")
                        .formatted(Formatting.DARK_AQUA, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("T2 BOUNTY ARMOR").formatted(Formatting.DARK_AQUA, Formatting.BOLD));
        lore.add(Text.literal("LEGENDARY").formatted(Formatting.GOLD, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+6").formatted(Formatting.GREEN, Formatting.BOLD)));
        lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+4").formatted(Formatting.GREEN, Formatting.BOLD)));
        lore.add(Text.literal("🔰 Knockback Resist: ").formatted(Formatting.WHITE)
                .append(Text.literal("+25%").formatted(Formatting.GREEN, Formatting.BOLD)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ ABILITIES ━━━").formatted(Formatting.DARK_AQUA));
        lore.add(Text.literal("👁 Darkness Immunity").formatted(Formatting.AQUA));
        lore.add(Text.literal("📡 Vibration Sense").formatted(Formatting.AQUA));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ VS BOSSES ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("🛡 Warden Boss: ").formatted(Formatting.WHITE)
                .append(Text.literal("-30%").formatted(Formatting.AQUA, Formatting.BOLD)));
        lore.add(Text.literal("🛡 Other Bosses: ").formatted(Formatting.WHITE)
                .append(Text.literal("-10%").formatted(Formatting.AQUA)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires: Warden Bounty Lvl " + T2_ARMOR_LEVEL_REQ)
                .formatted(Formatting.RED));

        helmet.set(DataComponentTypes.LORE, new LoreComponent(lore));
        helmet.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return helmet;
    }

    public static ItemStack createT2WardenChestplate() {
        ItemStack chestplate = new ItemStack(Items.NETHERITE_CHESTPLATE);
        chestplate.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Sculk Terror Chestplate II")
                        .formatted(Formatting.DARK_AQUA, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("T2 BOUNTY ARMOR").formatted(Formatting.DARK_AQUA, Formatting.BOLD));
        lore.add(Text.literal("LEGENDARY").formatted(Formatting.GOLD, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+12").formatted(Formatting.GREEN, Formatting.BOLD)));
        lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+5").formatted(Formatting.GREEN, Formatting.BOLD)));
        lore.add(Text.literal("🔰 Knockback Resist: ").formatted(Formatting.WHITE)
                .append(Text.literal("+40%").formatted(Formatting.GREEN, Formatting.BOLD)));
        lore.add(Text.literal("❤ Health Boost: ").formatted(Formatting.WHITE)
                .append(Text.literal("+8").formatted(Formatting.GREEN, Formatting.BOLD)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ ABILITIES ━━━").formatted(Formatting.DARK_AQUA));
        lore.add(Text.literal("📡 Sonic Pulse: ").formatted(Formatting.WHITE)
                .append(Text.literal("Shift + Attack").formatted(Formatting.YELLOW)));
        lore.add(Text.literal("   AOE damage to nearby enemies").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ VS BOSSES ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("🛡 Warden Boss: ").formatted(Formatting.WHITE)
                .append(Text.literal("-40%").formatted(Formatting.AQUA, Formatting.BOLD)));
        lore.add(Text.literal("🛡 Other Bosses: ").formatted(Formatting.WHITE)
                .append(Text.literal("-15%").formatted(Formatting.AQUA)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires: Warden Bounty Lvl " + T2_ARMOR_LEVEL_REQ)
                .formatted(Formatting.RED));

        chestplate.set(DataComponentTypes.LORE, new LoreComponent(lore));
        chestplate.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return chestplate;
    }

    public static ItemStack createT2WardenLeggings() {
        ItemStack leggings = new ItemStack(Items.NETHERITE_LEGGINGS);
        leggings.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Sculk Terror Leggings II")
                        .formatted(Formatting.DARK_AQUA, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("T2 BOUNTY ARMOR").formatted(Formatting.DARK_AQUA, Formatting.BOLD));
        lore.add(Text.literal("LEGENDARY").formatted(Formatting.GOLD, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+10").formatted(Formatting.GREEN, Formatting.BOLD)));
        lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+4").formatted(Formatting.GREEN, Formatting.BOLD)));
        lore.add(Text.literal("🔰 Knockback Resist: ").formatted(Formatting.WHITE)
                .append(Text.literal("+25%").formatted(Formatting.GREEN, Formatting.BOLD)));
        lore.add(Text.literal("❤ Health Boost: ").formatted(Formatting.WHITE)
                .append(Text.literal("+6").formatted(Formatting.GREEN, Formatting.BOLD)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ VS BOSSES ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("🛡 Warden Boss: ").formatted(Formatting.WHITE)
                .append(Text.literal("-30%").formatted(Formatting.AQUA, Formatting.BOLD)));
        lore.add(Text.literal("🛡 Other Bosses: ").formatted(Formatting.WHITE)
                .append(Text.literal("-10%").formatted(Formatting.AQUA)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires: Warden Bounty Lvl " + T2_ARMOR_LEVEL_REQ)
                .formatted(Formatting.RED));

        leggings.set(DataComponentTypes.LORE, new LoreComponent(lore));
        leggings.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return leggings;
    }

    public static ItemStack createT2WardenBoots() {
        ItemStack boots = new ItemStack(Items.NETHERITE_BOOTS);
        boots.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Sculk Terror Boots II")
                        .formatted(Formatting.DARK_AQUA, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("T2 BOUNTY ARMOR").formatted(Formatting.DARK_AQUA, Formatting.BOLD));
        lore.add(Text.literal("LEGENDARY").formatted(Formatting.GOLD, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+6").formatted(Formatting.GREEN, Formatting.BOLD)));
        lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+4").formatted(Formatting.GREEN, Formatting.BOLD)));
        lore.add(Text.literal("🔰 Knockback Resist: ").formatted(Formatting.WHITE)
                .append(Text.literal("+10%").formatted(Formatting.GREEN, Formatting.BOLD)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ ABILITIES ━━━").formatted(Formatting.DARK_AQUA));
        lore.add(Text.literal("🔇 Silent Steps").formatted(Formatting.AQUA));
        lore.add(Text.literal("   Sculk sensors ignore you").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ VS BOSSES ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("🛡 Warden Boss: ").formatted(Formatting.WHITE)
                .append(Text.literal("-20%").formatted(Formatting.AQUA, Formatting.BOLD)));
        lore.add(Text.literal("🛡 Other Bosses: ").formatted(Formatting.WHITE)
                .append(Text.literal("-8%").formatted(Formatting.AQUA)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires: Warden Bounty Lvl " + T2_ARMOR_LEVEL_REQ)
                .formatted(Formatting.RED));

        boots.set(DataComponentTypes.LORE, new LoreComponent(lore));
        boots.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return boots;
    }
    // ============================================================
// MISSING DETECTION METHODS
// ============================================================

    public static boolean isUpgradedSlayerSword(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return false;
        String n = name.getString();
        return n.contains("Slayer Sword II") || n.contains("Bounty Sword II");
    }

    public static boolean isSpiderLeggings(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return false;
        String n = name.getString();
        return n.contains("Venomous") || n.contains("Crawler") ||
                (n.contains("Spider") && n.contains("Leggings"));
    }

    public static boolean isSkeletonBow(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.getItem() != Items.BOW) return false;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return false;
        String n = name.getString();
        return n.contains("Bone") || n.contains("Desperado") || n.contains("Skeleton");
    }

    public static boolean isSlimeBoots(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return false;
        String n = name.getString();
        return n.contains("Slime") || n.contains("Gelatinous") || n.contains("Rustler");
    }

    public static boolean isWardenChestplate(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return false;
        String n = name.getString();
        return n.contains("Sculk") || n.contains("Warden") || n.contains("Terror");
    }

    public static boolean canUseSkeletonBow(ServerPlayerEntity player) {
        int level = SlayerData.getSlayerLevel(player.getUuidAsString(), SlayerManager.SlayerType.SKELETON);
        return level >= SKELETON_BOW_LEVEL_REQ;
    }

    public static boolean canUseSpiderLeggings(ServerPlayerEntity player) {
        int level = SlayerData.getSlayerLevel(player.getUuidAsString(), SlayerManager.SlayerType.SPIDER);
        return level >= SPIDER_LEGGINGS_LEVEL_REQ;
    }

    public static boolean canUseSlimeBoots(ServerPlayerEntity player) {
        int level = SlayerData.getSlayerLevel(player.getUuidAsString(), SlayerManager.SlayerType.SLIME);
        return level >= SLIME_BOOTS_LEVEL_REQ;
    }

    public static boolean canUseWardenChestplate(ServerPlayerEntity player) {
        int level = SlayerData.getSlayerLevel(player.getUuidAsString(), SlayerManager.SlayerType.WARDEN);
        return level >= WARDEN_CHESTPLATE_LEVEL_REQ;
    }

    // Level requirements


    public static int getSwordLevelRequirement(ItemStack sword) {
        if (isUpgradedSlayerSword(sword)) {
            return UPGRADED_SWORD_LEVEL_REQ;
        }
        if (isSlayerSword(sword)) {
            return BASIC_SWORD_LEVEL_REQ;
        }
        return 0;
    }

// ============================================================
// ITEM CREATION METHODS
// ============================================================

    public static ItemStack createZombieBerserkerHelmet() {
        ItemStack helmet = new ItemStack(Items.ZOMBIE_HEAD);

        helmet.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("☠ Zombie Berserker Helmet")
                        .formatted(Formatting.DARK_GREEN, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("LEGENDARY ARMOR").formatted(Formatting.GOLD, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("-50% Max Health").formatted(Formatting.RED));
        lore.add(Text.literal("+300% Damage").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires: Zombie Bounty Lvl " + BERSERKER_HELMET_LEVEL_REQ)
                .formatted(Formatting.RED));

        helmet.set(DataComponentTypes.LORE, new LoreComponent(lore));
        helmet.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        return helmet;
    }

    public static ItemStack createSkeletonBow() {
        ItemStack bow = new ItemStack(Items.BOW);

        bow.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("💀 Bone Desperado Bow")
                        .formatted(Formatting.WHITE, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("BOUNTY WEAPON").formatted(Formatting.WHITE, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("+50% Arrow Speed").formatted(Formatting.GREEN));
        lore.add(Text.literal("+50% Accuracy").formatted(Formatting.GREEN));
        lore.add(Text.literal("+50% Damage").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires: Skeleton Bounty Lvl " + SKELETON_BOW_LEVEL_REQ)
                .formatted(Formatting.RED));

        bow.set(DataComponentTypes.LORE, new LoreComponent(lore));
        bow.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        return bow;
    }

    public static ItemStack createVenomousCrawlerLeggings() {
        ItemStack leggings = new ItemStack(Items.LEATHER_LEGGINGS);


        leggings.set(DataComponentTypes.DYED_COLOR,
                new net.minecraft.component.type.DyedColorComponent(0x2E8B57));
        leggings.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🕷 Venomous Crawler Leggings")
                        .formatted(Formatting.DARK_RED, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("BOUNTY ARMOR").formatted(Formatting.DARK_RED, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Wall Climbing").formatted(Formatting.GREEN));
        lore.add(Text.literal("Poison Aura").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires: Spider Bounty Lvl " + SPIDER_LEGGINGS_LEVEL_REQ)
                .formatted(Formatting.RED));

        leggings.set(DataComponentTypes.LORE, new LoreComponent(lore));
        leggings.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        return leggings;
    }


    // T1 armor creation (needed for T2 crafting references)
    public static ItemStack createT1Armor(SlayerManager.SlayerType type, String piece) {
        return switch (piece) {
            case "Helmet" -> createT1Helmet(type);
            case "Chestplate" -> createT1Chestplate(type);
            case "Leggings" -> createT1Leggings(type);
            case "Boots" -> createT1Boots(type);
            default -> ItemStack.EMPTY;
        };
    }

    public static ItemStack createT2Armor(SlayerManager.SlayerType type, String piece) {
        return switch (piece) {
            case "Helmet" -> createT2Helmet(type);
            case "Chestplate" -> createT2Chestplate(type);
            case "Leggings" -> createT2Leggings(type);
            case "Boots" -> createT2Boots(type);
            default -> ItemStack.EMPTY;
        };
    }

    // Placeholder T1 creation methods (add your actual T1 armor creation here)
    public static ItemStack createT1Helmet(SlayerManager.SlayerType type) {
        ItemStack helmet = new ItemStack(Items.IRON_HELMET);
        helmet.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(getT1ArmorName(type, "Helmet")).formatted(type.color, Formatting.BOLD));
        return helmet;
    }

    public static ItemStack createT1Chestplate(SlayerManager.SlayerType type) {
        ItemStack chestplate = new ItemStack(Items.IRON_CHESTPLATE);
        chestplate.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(getT1ArmorName(type, "Chestplate")).formatted(type.color, Formatting.BOLD));
        return chestplate;
    }

    public static ItemStack createT1Leggings(SlayerManager.SlayerType type) {
        ItemStack leggings = new ItemStack(Items.IRON_LEGGINGS);
        leggings.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(getT1ArmorName(type, "Leggings")).formatted(type.color, Formatting.BOLD));
        return leggings;
    }

    public static ItemStack createT1Boots(SlayerManager.SlayerType type) {
        ItemStack boots = new ItemStack(Items.IRON_BOOTS);
        boots.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(getT1ArmorName(type, "Boots")).formatted(type.color, Formatting.BOLD));
        return boots;
    }

    private static String getT1ArmorName(SlayerManager.SlayerType type, String piece) {
        return switch (type) {
            case ZOMBIE -> "Undying Outlaw " + piece;
            case SPIDER -> "Venomous Bandit " + piece;
            case SKELETON -> "Bone Desperado " + piece;
            case SLIME -> "Gelatinous Rustler " + piece;
            case ENDERMAN -> "Void Phantom " + piece;
            case WARDEN -> "Sculk Terror " + piece;
        };
    }
}