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



    public static boolean isSkeletonBow(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return false;
        return name.getString().contains("Bone Desperado's Longbow");
    }

    public static boolean canUseSkeletonBow(ServerPlayerEntity player) {
        int level = SlayerData.getSlayerLevel(player.getUuidAsString(), SlayerManager.SlayerType.SKELETON);
        return level >= SKELETON_BOW_LEVEL_REQ;
    }
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



    public static boolean isSlimeBoots(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return false;
        return name.getString().contains("Gelatinous Rustler Boots");
    }

    public static boolean canUseSlimeBoots(ServerPlayerEntity player) {
        int level = SlayerData.getSlayerLevel(player.getUuidAsString(), SlayerManager.SlayerType.SLIME);
        return level >= SLIME_BOOTS_LEVEL_REQ;
    }

    // ============================================================
// WARDEN CHESTPLATE - T12 Warden Requirement (Best Armor)
// ============================================================
    public static final int WARDEN_CHESTPLATE_LEVEL_REQ = 12;



    public static boolean isWardenChestplate(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return false;
        return name.getString().contains("Sculk Terror Chestplate");
    }

    public static boolean canUseWardenChestplate(ServerPlayerEntity player) {
        int level = SlayerData.getSlayerLevel(player.getUuidAsString(), SlayerManager.SlayerType.WARDEN);
        return level >= WARDEN_CHESTPLATE_LEVEL_REQ;
    }

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

        int bossReductionPercent = (int)(stats.bossReduction * 100);
        int allReductionPercent = (int)(stats.allBossReduction * 100);

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
        lore.add(Text.literal("condensed slime essence.").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.GREEN));
        lore.add(Text.literal("🛡 Armor: +4").formatted(Formatting.BLUE));
        lore.add(Text.literal("🛡 Toughness: +2").formatted(Formatting.BLUE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.GREEN));
        lore.add(Text.literal("⬆ Jump Boost II").formatted(Formatting.AQUA));
        lore.add(Text.literal("🛡 No Fall Damage").formatted(Formatting.WHITE));
        lore.add(Text.literal("💚 Death Save: 30s at half size").formatted(Formatting.YELLOW));
        lore.add(Text.literal("   (53min cooldown)").formatted(Formatting.DARK_GRAY));
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
// ZOMBIE BERSERKER HELMET - Fixed with custom attributes
// ============================================================
    public static ItemStack createZombieBerserkerHelmet() {
        ItemStack helmet = new ItemStack(Items.ZOMBIE_HEAD);

        helmet.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("☠ Zombie Berserker Helmet")
                        .formatted(Formatting.DARK_GREEN, Formatting.BOLD));

        // CUSTOM ATTRIBUTES - Half health, 4x damage
        AttributeModifiersComponent attributes = AttributeModifiersComponent.builder()
                .add(EntityAttributes.MAX_HEALTH,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "berserker_health"),
                                -0.5,  // -50% health
                                EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                        ),
                        AttributeModifierSlot.HEAD)
                .add(EntityAttributes.ATTACK_DAMAGE,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "berserker_damage"),
                                3.0,  // +300% damage (4x total)
                                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                        ),
                        AttributeModifierSlot.HEAD)
                .add(EntityAttributes.ARMOR,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "berserker_armor"),
                                2.0,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.HEAD)
                .build();
        helmet.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attributes);

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("LEGENDARY ARMOR").formatted(Formatting.GOLD, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("A cursed helm forged from the").formatted(Formatting.GRAY));
        lore.add(Text.literal("essence of fallen bounty bosses.").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GREEN));
        lore.add(Text.literal("🛡 Armor: +2").formatted(Formatting.BLUE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("❤ Max Health: ").formatted(Formatting.WHITE)
                .append(Text.literal("-50%").formatted(Formatting.DARK_RED, Formatting.BOLD)));
        lore.add(Text.literal("⚔ Damage Dealt: ").formatted(Formatting.WHITE)
                .append(Text.literal("+300%").formatted(Formatting.GREEN, Formatting.BOLD)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ BOSS DEFENSE ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("vs Zombie Bosses: ").formatted(Formatting.GRAY)
                .append(Text.literal("-75% damage").formatted(Formatting.GREEN, Formatting.BOLD)));
        lore.add(Text.literal("vs All Bosses: ").formatted(Formatting.GRAY)
                .append(Text.literal("-15% damage").formatted(Formatting.GREEN)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires: Zombie Bounty Lvl " + BERSERKER_HELMET_LEVEL_REQ)
                .formatted(Formatting.RED));

        helmet.set(DataComponentTypes.LORE, new LoreComponent(lore));
        helmet.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        return helmet;
    }

    // ============================================================
// SKELETON BOW - Fixed with custom attributes
// ============================================================
    public static ItemStack createSkeletonBow() {
        ItemStack bow = new ItemStack(Items.BOW);

        bow.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🏹 Bone Desperado's Longbow")
                        .formatted(Formatting.WHITE, Formatting.BOLD));

        // Durability - 10x default
        bow.set(DataComponentTypes.MAX_DAMAGE, 3840);
        bow.set(DataComponentTypes.DAMAGE, 0);

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("LEGENDARY WEAPON").formatted(Formatting.GOLD, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Carved from the spine of").formatted(Formatting.GRAY));
        lore.add(Text.literal("The Bone Desperado himself.").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.WHITE));
        lore.add(Text.literal("🎯 Auto-Lock: Arrows home to targets").formatted(Formatting.YELLOW));
        lore.add(Text.literal("💀 Headshot: 5x Damage").formatted(Formatting.RED));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ BOSS DEFENSE ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("Passive: -50% damage from Skeleton bosses").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires: Skeleton Bounty Lvl " + SKELETON_BOW_LEVEL_REQ)
                .formatted(Formatting.RED));

        bow.set(DataComponentTypes.LORE, new LoreComponent(lore));
        bow.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        return bow;
    }
    // ============================================================
// SLAYER ARMOR LEVEL REQUIREMENTS
// ============================================================


// ============================================================
// ARMOR PIECE ENUM
// ============================================================

    public enum ArmorPiece {
        HELMET("Helmet", Items.LEATHER_HELMET),
        CHESTPLATE("Chestplate", Items.LEATHER_CHESTPLATE),
        LEGGINGS("Leggings", Items.LEATHER_LEGGINGS),
        BOOTS("Boots", Items.LEATHER_BOOTS);

        public final String displayName;
        public final Item baseItem;

        ArmorPiece(String displayName, Item baseItem) {
            this.displayName = displayName;
            this.baseItem = baseItem;
        }
    }
    private static int getSlayerColor(SlayerManager.SlayerType type) {
        return switch (type) {
            case ZOMBIE -> 0x2E7D32;     // Dark green
            case SPIDER -> 0x8B0000;     // Dark red
            case SKELETON -> 0xE0E0E0;   // Light gray/bone white
            case SLIME -> 0x7CB342;      // Lime green
            case ENDERMAN -> 0x6A0DAD;   // Purple
            case WARDEN -> 0x006064;     // Dark cyan/teal
        };
    }
    // ============================================================
// T1 ARMOR CREATION - "Hunter" series
// ============================================================

    public static ItemStack createT1Armor(SlayerManager.SlayerType slayerType, ArmorPiece piece) {
        ItemStack armor = new ItemStack(piece.baseItem);

        // Base stats per piece type
        int baseDurability = switch (piece) {
            case HELMET -> 165;
            case CHESTPLATE -> 240;
            case LEGGINGS -> 225;
            case BOOTS -> 195;
        };

        // Multiply durability by slayer difficulty (harder slayers = better gear)
        int durability = (int) (baseDurability * (1 + slayerType.difficultyMultiplier * 0.1));

        // Set custom name
        armor.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(slayerType.displayName + " Hunter " + piece.displayName)
                        .formatted(slayerType.color, Formatting.BOLD));

        // Set durability
        armor.set(DataComponentTypes.MAX_DAMAGE, durability);
        armor.set(DataComponentTypes.DAMAGE, 0);

        // Set dyed color
        armor.set(DataComponentTypes.DYED_COLOR,
                new net.minecraft.component.type.DyedColorComponent(getSlayerColor(slayerType)));

        // Build lore
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("BOUNTY ARMOR [T1]").formatted(Formatting.BLUE, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("❤ Durability: ").formatted(Formatting.WHITE)
                .append(Text.literal(String.valueOf(durability)).formatted(Formatting.GREEN)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS (2+ pieces) ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal(""));

        // Type-specific bonuses
        String bonus = switch (slayerType) {
            case ZOMBIE -> "10% Undead Damage Resistance";
            case SPIDER -> "Poison Immunity";
            case SKELETON -> "15% Arrow Damage Resistance";
            case SLIME -> "25% Fall Damage Reduction";
            case WARDEN -> "Darkness Immunity";
            case ENDERMAN -> "10% Magic Resistance";
        };
        lore.add(Text.literal(" • " + bonus).formatted(slayerType.color));

        lore.add(Text.literal(""));
        lore.add(Text.literal("Effective vs: ").formatted(Formatting.GRAY)
                .append(Text.literal(slayerType.displayName + "s").formatted(slayerType.color)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires: " + slayerType.displayName + " Bounty Lvl " + T1_ARMOR_LEVEL_REQ)
                .formatted(Formatting.RED));

        armor.set(DataComponentTypes.LORE, new LoreComponent(lore));

        return armor;
    }
    // ============================================================
// T2 ARMOR CREATION - "Slayer II" series
// ============================================================

    public static ItemStack createT2Armor(SlayerManager.SlayerType slayerType, ArmorPiece piece) {
        ItemStack armor = new ItemStack(piece.baseItem);

        // T2 has better base stats
        int baseDurability = switch (piece) {
            case HELMET -> 330;
            case CHESTPLATE -> 480;
            case LEGGINGS -> 450;
            case BOOTS -> 390;
        };

        int durability = (int) (baseDurability * (1 + slayerType.difficultyMultiplier * 0.1));

        // Set custom name with II suffix
        armor.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(slayerType.displayName + " Slayer " + piece.displayName + " II")
                        .formatted(slayerType.color, Formatting.BOLD));

        // Set durability
        armor.set(DataComponentTypes.MAX_DAMAGE, durability);
        armor.set(DataComponentTypes.DAMAGE, 0);

        // Set dyed color
        armor.set(DataComponentTypes.DYED_COLOR,
                new net.minecraft.component.type.DyedColorComponent(getSlayerColor(slayerType)));

        // Add enchant glint
        armor.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        // Build lore
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("BOUNTY ARMOR [T2]").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("❤ Durability: ").formatted(Formatting.WHITE)
                .append(Text.literal(String.valueOf(durability)).formatted(Formatting.GREEN)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS (2+ pieces) ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal(""));

        // Enhanced type-specific bonuses
        String bonus = switch (slayerType) {
            case ZOMBIE -> "25% Undead Damage Resistance";
            case SPIDER -> "Poison Immunity + Speed I";
            case SKELETON -> "30% Arrow Resistance + Accuracy";
            case SLIME -> "Fall Immunity + Jump Boost I";
            case WARDEN -> "Darkness Immunity + Strength I";
            case ENDERMAN -> "25% Magic Resistance";
        };
        lore.add(Text.literal(" • " + bonus).formatted(slayerType.color));

        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ FULL SET (4 pieces) ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal(" • 2x " + slayerType.displayName + " Kill XP").formatted(Formatting.GREEN));
        lore.add(Text.literal(" • +25% damage to " + slayerType.displayName + "s").formatted(Formatting.RED));

        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires: " + slayerType.displayName + " Bounty Lvl " + T2_ARMOR_LEVEL_REQ)
                .formatted(Formatting.RED));

        armor.set(DataComponentTypes.LORE, new LoreComponent(lore));

        return armor;
    }
    // ============================================================
// ARMOR IDENTIFICATION METHODS
// ============================================================

    public static boolean isT1SlayerArmor(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return false;
        return name.getString().contains("Hunter");
    }

    public static boolean isT2SlayerArmor(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return false;
        String nameStr = name.getString();
        // Make sure it's "Slayer ... II" not the special items
        return nameStr.contains("Slayer") && nameStr.contains(" II") &&
                (nameStr.contains("Helmet") || nameStr.contains("Chestplate") ||
                        nameStr.contains("Leggings") || nameStr.contains("Boots"));
    }

    public static boolean isSlayerArmorSet(ItemStack stack) {
        return isT1SlayerArmor(stack) || isT2SlayerArmor(stack);
    }

    public static SlayerManager.SlayerType getArmorSlayerType(ItemStack stack) {
        if (!isSlayerArmorSet(stack)) return null;
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

    public static ArmorPiece getArmorPiece(ItemStack stack) {
        if (!isSlayerArmorSet(stack)) return null;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return null;
        String nameStr = name.getString();

        for (ArmorPiece piece : ArmorPiece.values()) {
            if (nameStr.contains(piece.displayName)) {
                return piece;
            }
        }
        return null;
    }

    // Check if player can use this armor
    public static boolean canUseSlayerArmorPiece(ServerPlayerEntity player, ItemStack armor) {
        if (!isSlayerArmorSet(armor)) return true;

        SlayerManager.SlayerType type = getArmorSlayerType(armor);
        if (type == null) return true;

        int playerLevel = SlayerData.getSlayerLevel(player.getUuidAsString(), type);
        int requiredLevel = isT2SlayerArmor(armor) ? T2_ARMOR_LEVEL_REQ : T1_ARMOR_LEVEL_REQ;

        return playerLevel >= requiredLevel;
    }
    // ============================================================
// BASIC SWORD LEVEL REQUIREMENT
// ============================================================


    // ============================================================
// VENOMOUS CRAWLER LEGGINGS - FIXED DETECTION
// ============================================================


    public static boolean isSpiderLeggings(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return false;
        String nameStr = name.getString();
        // Check multiple possible name patterns
        return nameStr.contains("Venomous Crawler Leggings") ||
                nameStr.contains("Venomous") && nameStr.contains("Leggings") ||
                nameStr.contains("Spider") && nameStr.contains("Leggings");
    }

    public static boolean canUseSpiderLeggings(ServerPlayerEntity player) {
        int level = SlayerData.getSlayerLevel(player.getUuidAsString(), SlayerManager.SlayerType.SPIDER);
        return level >= SPIDER_LEGGINGS_LEVEL_REQ;
    }

    // ============================================================
// UPGRADED SLAYER SWORD DETECTION
// ============================================================
    public static boolean isUpgradedSlayerSword(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return false;
        String nameStr = name.getString();
        return nameStr.contains("Sword II") || nameStr.contains("Bounty Sword II");
    }

    public static int getSwordLevelRequirement(ItemStack stack) {
        if (isUpgradedSlayerSword(stack)) {
            return UPGRADED_SWORD_LEVEL_REQ;
        }
        if (isSlayerSword(stack)) {
            return BASIC_SWORD_LEVEL_REQ;
        }
        return 0;
    }

    // ============================================================
// T2 ARMOR DETECTION HELPERS
// ============================================================
    public static boolean isT2Armor(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return false;
        String nameStr = name.getString();
        return nameStr.contains("Sculk") || nameStr.contains("Warden") ||
                nameStr.contains("II") || nameStr.contains("Mk2");
    }

    public static boolean isT2Sword(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return false;
        String nameStr = name.getString();
        return nameStr.contains("Sword II");
    }
}