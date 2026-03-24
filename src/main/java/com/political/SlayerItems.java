package com.political;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.nbt.NbtCompound;
import com.political.ArmourAttribute;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class SlayerItems {

    // ============================================================
    // CUSTOM ITEM ID SYSTEM (for texture packs)
    // ============================================================

    public static final String CUSTOM_ITEM_ID_KEY = "custom_item_id";
    private static final String LEGACY_CUSTOM_ITEM_ID_KEY = "custom_id";

    /** Sets the custom_item_id field in this item's CUSTOM_DATA NBT. */
    public static void setCustomItemId(ItemStack stack, String id) {
        NbtComponent existing = stack.get(DataComponentTypes.CUSTOM_DATA);
        NbtCompound nbt = (existing != null) ? existing.copyNbt() : new NbtCompound();
        nbt.putString(CUSTOM_ITEM_ID_KEY, id);
        // Backwards compatibility: older code paths read from custom_id
        nbt.putString(LEGACY_CUSTOM_ITEM_ID_KEY, id);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

    /** Returns the custom_item_id of this item, or null if not set. */
    public static String getCustomItemId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (data == null) return null;
        NbtCompound nbt = data.copyNbt();
        String id = nbt.getString(CUSTOM_ITEM_ID_KEY, null);
        if (id == null || id.isEmpty()) {
            id = nbt.getString(LEGACY_CUSTOM_ITEM_ID_KEY, null);
        }
        return (id != null && !id.isEmpty()) ? id : null;
    }

    /** Returns true if this item has the given custom_item_id. */
    public static boolean hasCustomItemId(ItemStack stack, String id) {
        return id != null && id.equals(getCustomItemId(stack));
    }

    // ============================================================
    // ITEM IDENTIFICATION
    // ============================================================

    private static final String SLAYER_SWORD_TAG = "BOUNTY_SWORD";
    private static final String SLAYER_CORE_TAG = "BOUNTY_CORE";
    public static final String BOUNTY_SWORD_TYPE_TAG = "bounty_sword_type";
    public static final String BOUNTY_SWORD_UPGRADED_TAG = "bounty_sword_upgraded";
    public static final String ARMOR_ATTRIBUTE_TOKEN_KEY = "armor_attribute_token";
    public static final String ARMOR_ATTRIBUTE_KEY = "ArmourAttribute";
    public static final String WEAPON_ATTRIBUTE_TOKEN_KEY = "weapon_attribute_token";
    public static final String WEAPON_ATTRIBUTE_KEY = "WeaponAttribute";

    /** Returns the thematic display name for a T1 bounty sword of the given type. */
    public static String getSwordDisplayName(SlayerManager.SlayerType type) {
        return switch (type) {
            case ZOMBIE   -> "Outlaw's Cleaver";
            case SPIDER   -> "Bandit's Fang";
            case SKELETON -> "Desperado's Sword";
            case SLIME    -> "Rustler's Lasso";
            case ENDERMAN -> "Phantom's Edge";
            case IRON_GOLEM   -> "Terror's Grasp";
            case PIGLIN   -> "Gilded Ravager's Blade";
        };
    }

    /** Returns the thematic display name for a T2 (upgraded) bounty sword of the given type. */
    public static String getUpgradedSwordDisplayName(SlayerManager.SlayerType type) {
        return getSwordDisplayName(type) + " II";
    }

    /** Writes bounty sword identification NBT into the item's CustomData. */
    private static void setBountySwordNbt(ItemStack stack, SlayerManager.SlayerType type, boolean upgraded) {
        NbtComponent existing = stack.get(DataComponentTypes.CUSTOM_DATA);
        NbtCompound nbt = (existing != null) ? existing.copyNbt() : new NbtCompound();
        nbt.putString(BOUNTY_SWORD_TYPE_TAG, type.name());
        if (upgraded) nbt.putBoolean(BOUNTY_SWORD_UPGRADED_TAG, true);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

    public static boolean isSlayerSword(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        // Prefer NBT-based identification (new swords)
        NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (data != null) {
            NbtCompound nbt = data.copyNbt();
            if (nbt.contains(BOUNTY_SWORD_TYPE_TAG) && !nbt.getBoolean(BOUNTY_SWORD_UPGRADED_TAG, false)) {
                return true;
            }
        }
        // Fallback: name-based for backward compatibility
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return false;
        String n = name.getString();
        return n.contains("Bounty Sword") && !n.contains("Bounty Sword II");
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
        if (stack == null || stack.isEmpty()) return null;
        // Prefer NBT-based identification (new swords)
        NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (data != null) {
            NbtCompound nbt = data.copyNbt();
            String typeName = nbt.getString(BOUNTY_SWORD_TYPE_TAG, null);
            if (typeName != null) {
                for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
                    if (type.name().equals(typeName)) return type;
                }
            }
        }
        // Fallback: name-based for backward compat
        if (!isSlayerSword(stack) && !isUpgradedSlayerSword(stack)) return null;
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
                CustomItemHandler.sendLevelWarning(player, getSwordDisplayName(swordType), requiredLevel, swordType.displayName);
                return 0.0f; // No damage!
            }
        }

        // Check upgraded swords
        if (isUpgradedSlayerSword(weapon)) {
            SlayerManager.SlayerType swordType = getSwordSlayerType(weapon);
            if (swordType == null) return 1.0f;

            int playerLevel = SlayerData.getSlayerLevel(player.getUuidAsString(), swordType);

            if (playerLevel < UPGRADED_SWORD_LEVEL_REQ) {
                CustomItemHandler.sendLevelWarning(player, getUpgradedSwordDisplayName(swordType), UPGRADED_SWORD_LEVEL_REQ, swordType.displayName);
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
                Text.literal(getSwordDisplayName(type))
                        .formatted(type.color, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚔ Bounty Weapon").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Deals ").formatted(Formatting.GRAY)
                .append(Text.literal("200% damage").formatted(Formatting.RED, Formatting.BOLD))
                .append(Text.literal(" to:").formatted(Formatting.GRAY)));
        lore.add(Text.literal("  • " + type.displayName + "s").formatted(type.color));
        lore.add(Text.literal("  • " + type.bossName).formatted(type.color));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Counts as 2 kills per kill").formatted(Formatting.GREEN));
        lore.add(Text.literal("Bypasses ").formatted(Formatting.GRAY)
                .append(Text.literal("Bounty Boss Resistance").formatted(Formatting.GOLD)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ DEFENCE ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("vs All Bosses: ").formatted(Formatting.GRAY)
                .append(Text.literal("-4% damage").formatted(Formatting.GREEN)));
        lore.add(Text.literal("vs " + type.displayName + " Boss: ").formatted(Formatting.GRAY)
                .append(Text.literal("-19% total (-4 + -15 extra)").formatted(Formatting.GREEN, Formatting.BOLD)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("MFLUX Contractor Program").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("⚠ Requires: " + type.displayName + " Bounty Lvl " + BASIC_SWORD_LEVEL_REQ)
                .formatted(Formatting.RED));

        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));

        // Tag for NBT-based identification
        setBountySwordNbt(sword, type, false);

        setCustomItemId(sword, type.name().toLowerCase() + "_sword");
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
        lore.add(Text.literal(""));
        lore.add(Text.literal("Used for crafting " + type.displayName).formatted(Formatting.GRAY));
        lore.add(Text.literal("bounty equipment.").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[MFLUX: Anomalous biological residue | Charadrius core sample]").formatted(Formatting.DARK_GRAY));

        core.set(DataComponentTypes.LORE, new LoreComponent(lore));
        core.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        return core;
    }

    // ============================================================
// SLIME BOOTS - T8 Slime Requirement
// ============================================================
    public static final int SLIME_BOOTS_LEVEL_REQ = 8;



    // ============================================================
// IRON_GOLEM CHESTPLATE - T12 Warden Requirement (Best Armor)
// ============================================================
    public static final int IRON_GOLEM_CHESTPLATE_LEVEL_REQ = 12;





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
                isWardenChestplate(stack) ||
                isAttributeToken(stack) ||
                isCrownOfGreed(stack);
    }

    public static boolean isAttributeToken(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (data == null) return false;
        return data.copyNbt().contains(ARMOR_ATTRIBUTE_TOKEN_KEY);
    }

    public static ArmourAttribute getAttributeFromToken(ItemStack stack) {
        if (!isAttributeToken(stack)) return null;
        NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);
        String id = data.copyNbt().getString(ARMOR_ATTRIBUTE_TOKEN_KEY).orElse("");
        return ArmourAttribute.fromId(id);
    }

    public static ArmourAttribute getAppliedAttribute(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (data == null) return null;
        String id = data.copyNbt().getString(ARMOR_ATTRIBUTE_KEY).orElse("");
        return ArmourAttribute.fromId(id);
    }

    public static void applyAttribute(ItemStack stack, ArmourAttribute attribute) {
        NbtComponent existing = stack.get(DataComponentTypes.CUSTOM_DATA);
        NbtCompound nbt = (existing != null) ? existing.copyNbt() : new NbtCompound();
        nbt.putString(ARMOR_ATTRIBUTE_KEY, attribute.id);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        
        // Add enhanced lore to the armor piece
        List<Text> existingLore = new ArrayList<>();
        LoreComponent currentLore = stack.get(DataComponentTypes.LORE);
        if (currentLore != null) {
            existingLore.addAll(currentLore.lines());
        }
        
        // Add attribute lore at the beginning
        List<Text> newLore = new ArrayList<>();
        newLore.add(Text.literal(""));
        newLore.add(Text.literal("◆ " + attribute.displayName + " ATTRIBUTE").formatted(attribute.color, Formatting.BOLD));
        newLore.add(Text.literal(""));
        
        // Add thematic description based on attribute type
        switch (attribute) {
            case BURNING -> {
                newLore.add(Text.literal("✦ Eternal flame courses through this armor").formatted(Formatting.YELLOW));
                newLore.add(Text.literal("✦ Immune to fire extinguishment").formatted(Formatting.YELLOW));
                break;
            }
            case SIGHTLESS -> {
                newLore.add(Text.literal("✦ Darkness reveals all to the wearer").formatted(Formatting.YELLOW));
                newLore.add(Text.literal("✦ Visual impairments have no effect").formatted(Formatting.YELLOW));
                break;
            }
            case FRENZIED -> {
                newLore.add(Text.literal("✦ Berserk rage enhances attack speed").formatted(Formatting.YELLOW));
                newLore.add(Text.literal("✦ Occasional nausea from raw power").formatted(Formatting.YELLOW));
                break;
            }
            case GROUNDED -> {
                newLore.add(Text.literal("✦ Earth's protection against lightning").formatted(Formatting.YELLOW));
                newLore.add(Text.literal("✦ Deflect incoming projectiles").formatted(Formatting.YELLOW));
                break;
            }
            case WEBBED -> {
                newLore.add(Text.literal("✦ Move freely through cobwebs").formatted(Formatting.YELLOW));
                newLore.add(Text.literal("✦ Spiders view you as neutral").formatted(Formatting.YELLOW));
                break;
            }
            case FROST -> {
                newLore.add(Text.literal("✦ Walk on water as frozen ground").formatted(Formatting.YELLOW));
                newLore.add(Text.literal("✦ Cold environments no longer harm").formatted(Formatting.YELLOW));
                break;
            }
            case PHANTOMSTEP -> {
                newLore.add(Text.literal("✦ Walk with spectral silence").formatted(Formatting.YELLOW));
                newLore.add(Text.literal("✦ Gravity's pull weakened").formatted(Formatting.YELLOW));
                break;
            }
            case CURSED -> {
                newLore.add(Text.literal("✦ Vampiric life steal on kills").formatted(Formatting.YELLOW));
                newLore.add(Text.literal("✦ Dark power corrupts the soul").formatted(Formatting.YELLOW));
                break;
            }
            case OVERGROWN -> {
                newLore.add(Text.literal("✦ Regenerate on natural ground").formatted(Formatting.YELLOW));
                newLore.add(Text.literal("✦ Earth's vitality flows through").formatted(Formatting.YELLOW));
                break;
            }
            case VOLATILE -> {
                newLore.add(Text.literal("✦ Explosive death when near defeat").formatted(Formatting.YELLOW));
                newLore.add(Text.literal("✦ Blast immunity protects wearer").formatted(Formatting.YELLOW));
                break;
            }
        }
        
        newLore.add(Text.literal(""));
        newLore.add(Text.literal("━━━ ORIGINAL STATS ━━━").formatted(Formatting.DARK_GRAY));
        
        // Add existing lore after the attribute section
        newLore.addAll(existingLore);
        
        stack.set(DataComponentTypes.LORE, new LoreComponent(newLore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    public static boolean isZombieBerserkerHelmet(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        // Check custom ID first (preferred)
        if (hasCustomItemId(stack, "zombie_berserker_helmet")) return true;
        // Fallback: name-based for old items
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
        ItemStack helmet = new ItemStack(Items.LEATHER_HELMET);
        helmet.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0x1A6600));

        int playerLevel = SlayerData.getSlayerLevel(player.getUuidAsString(), SlayerManager.SlayerType.ZOMBIE);
        boolean meetsRequirement = playerLevel >= BERSERKER_HELMET_LEVEL_REQ;

        helmet.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("☠ Zombie Berserker Helmet")
                        .formatted(Formatting.DARK_GREEN, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal(""));
        lore.add(Text.literal("A cursed helm forged from the").formatted(Formatting.GRAY));
        lore.add(Text.literal("essence of fallen bounty bosses.").formatted(Formatting.GRAY));
        lore.add(Text.literal("§8MFLUX Research Division Prototype").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal(""));
        lore.add(Text.literal("❤ Max Health: ").formatted(Formatting.WHITE)
                .append(Text.literal("-50%").formatted(Formatting.DARK_RED, Formatting.BOLD)));
        lore.add(Text.literal("⚔ Damage Dealt: ").formatted(Formatting.WHITE)
                .append(Text.literal("+300%").formatted(Formatting.GREEN, Formatting.BOLD)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ BOSS DEFENSE ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("vs Zombie Bosses: ").formatted(Formatting.GRAY)
                .append(Text.literal("-6% damage").formatted(Formatting.GREEN, Formatting.BOLD)));
        lore.add(Text.literal("vs All Bosses: ").formatted(Formatting.GRAY)
                .append(Text.literal("-20% damage").formatted(Formatting.GREEN)));

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
                Text.literal(getUpgradedSwordDisplayName(type))
                        .formatted(type.color, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚔ Upgraded Bounty Weapon").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Deals ").formatted(Formatting.GRAY)
                .append(Text.literal("300% damage").formatted(Formatting.RED, Formatting.BOLD))
                .append(Text.literal(" to:").formatted(Formatting.GRAY)));
        lore.add(Text.literal("  • " + type.displayName + "s").formatted(type.color));
        lore.add(Text.literal("  • " + type.bossName).formatted(type.color));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Counts as 3 kills per kill").formatted(Formatting.GREEN));
        lore.add(Text.literal("Bypasses ").formatted(Formatting.GRAY)
                .append(Text.literal("Bounty Boss Resistance").formatted(Formatting.GOLD)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ DEFENCE ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("vs All Bosses: ").formatted(Formatting.GRAY)
                .append(Text.literal("-6% damage").formatted(Formatting.GREEN)));
        lore.add(Text.literal("vs " + type.displayName + " Boss: ").formatted(Formatting.GRAY)
                .append(Text.literal("-26% total (-6 + -20 extra)").formatted(Formatting.GREEN, Formatting.BOLD)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("MFLUX Classified: Charadrius Mk.II").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("⚠ Requires: " + type.displayName + " Bounty Lvl " + UPGRADED_SWORD_LEVEL_REQ)
                .formatted(Formatting.RED));

        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        // Custom enchantments are NOT auto-applied — players apply them manually.
        // The lore shows compatible enchantments as a guide only.

        // Tag for NBT-based identification
        setBountySwordNbt(sword, type, true);

        setCustomItemId(sword, type.name().toLowerCase() + "_sword_t2");
        return sword;
    }

    public static ItemStack createSlayerSwordForPlayer(ServerPlayerEntity player, SlayerManager.SlayerType type) {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);

        sword.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(getSwordDisplayName(type))
                        .formatted(type.color, Formatting.BOLD));

        int playerLevel = SlayerData.getSlayerLevel(player.getUuidAsString(), type);
        int requiredLevel = BASIC_SWORD_LEVEL_REQ;
        boolean meetsRequirement = playerLevel >= requiredLevel;

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚔ Bounty Weapon").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Deals ").formatted(Formatting.GRAY)
                .append(Text.literal("200% damage").formatted(Formatting.RED, Formatting.BOLD))
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

    public static double getUpgradedSlayerSwordDamageMultiplier(ItemStack weapon, LivingEntity target, ServerPlayerEntity player) {
        if (!isUpgradedSlayerSword(weapon)) return 1.0;

        SlayerManager.SlayerType swordType = getSwordSlayerType(weapon);
        if (swordType == null) return 1.0;

        // Level requirement check
        int playerLevel = SlayerData.getSlayerLevel(player.getUuidAsString(), swordType);
        if (playerLevel < UPGRADED_SWORD_LEVEL_REQ) return 1.0;

        boolean isMatchingMob = isMatchingMobType(target, swordType);
        boolean isMatchingBoss = false;
        if (SlayerManager.isSlayerBoss(target.getUuid())) {
            SlayerManager.SlayerType bossType = SlayerManager.getBossSlayerType(target.getUuid());
            isMatchingBoss = (bossType == swordType);
        }

        if (isMatchingMob || isMatchingBoss) {
            return 3.0; // T2 sword: 3x damage as stated in lore
        }

        if (SlayerManager.isSlayerBoss(target.getUuid())) {
            return 2.0; // Better bonus vs non-matching bosses
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
            case IRON_GOLEM -> entity instanceof net.minecraft.entity.mob.WardenEntity;
            case PIGLIN -> entity instanceof net.minecraft.entity.mob.PiglinBruteEntity || entity instanceof net.minecraft.entity.mob.PiglinEntity;
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
        lore.add(Text.literal(""));
        lore.add(Text.literal("A powerful essence from the").formatted(Formatting.GRAY));
        lore.add(Text.literal(type.bossName + ".").formatted(type.color));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Used in crafting powerful").formatted(Formatting.GRAY));
        lore.add(Text.literal("Bounty equipment.").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[MFLUX: Anomalous biological residue | Charadrius specimen]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal(""));

        // Hint at what it crafts
        String craftHint = switch (type) {
            case ZOMBIE -> "Crafts: " + getSwordDisplayName(SlayerManager.SlayerType.ZOMBIE);
            case SPIDER -> "Crafts: " + getSwordDisplayName(SlayerManager.SlayerType.SPIDER);
            case SKELETON -> "Crafts: " + getSwordDisplayName(SlayerManager.SlayerType.SKELETON);
            case SLIME -> "Crafts: " + getSwordDisplayName(SlayerManager.SlayerType.SLIME);
            case ENDERMAN -> "Crafts: " + getSwordDisplayName(SlayerManager.SlayerType.ENDERMAN);
            case IRON_GOLEM -> "Crafts: " + getSwordDisplayName(SlayerManager.SlayerType.IRON_GOLEM);
            case PIGLIN -> "Crafts: " + getSwordDisplayName(SlayerManager.SlayerType.PIGLIN);
        };
        lore.add(Text.literal(craftHint).formatted(Formatting.DARK_PURPLE));

        core.set(DataComponentTypes.LORE, new LoreComponent(lore));

        // Make it glow
        core.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        setCustomItemId(core, type.name().toLowerCase() + "_core");
        return core;
    }
    // ============================================================



    public static ItemStack createChunk(SlayerManager.SlayerType type) {
        ItemStack chunk = new ItemStack(type.icon);

        String chunkName = switch (type) {
            case ZOMBIE -> "Undead Chunk";
            case SPIDER -> "Venomous Gland";
            case SKELETON -> "Ancient Bone";
            case SLIME -> "Condensed Gel";
            case ENDERMAN -> "Void Fragment";
            case IRON_GOLEM -> "Sculk Heart";
            case PIGLIN -> "Gilded Shard";
        };

        chunk.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✦ " + chunkName + " ✦")
                        .formatted(type.color, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal(""));
        lore.add(Text.literal("A fragment from the").formatted(Formatting.GRAY));
        lore.add(Text.literal(type.bossName + ".").formatted(type.color));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Used to craft Bounty Hunter's Swords").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[MFLUX: Anomalous biological sample | Charadrius specimen]").formatted(Formatting.DARK_GRAY));

        chunk.set(DataComponentTypes.LORE, new LoreComponent(lore));
        chunk.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        setCustomItemId(chunk, type.name().toLowerCase() + "_chunk");
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
            case IRON_GOLEM -> "Sculk Heart";
            case PIGLIN -> "Gilded Shard";
        };
    }

// ============================================================
// LEVEL REQUIREMENTS FOR SLAYER SWORDS
// ============================================================

    // Sword tiers and their level requirements
    public static final int BASIC_SWORD_LEVEL_REQ = 3;      // T1 sword

    public static final String UNDEAD_HEART = "undead_heart";
    public static final String SPECTRAL_QUIVER = "spectral_quiver";
    public static final String ECHOING_CORE = "echoing_core";
    public static final String ENDER_SWORD = "ender_sword";
    public static final String ABYSSAL_BLADE = "abyssal_blade";
    public static final String BOUNCY_SLIME = "bouncy_slime";
    public static final String VENOMOUS_DAGGER = "venomous_dagger";

    public static final String CUSTOM_TYPE_KEY = "custom_type";

    public static void setCustomItemType(ItemStack stack, String type) {
        NbtComponent existing = stack.get(DataComponentTypes.CUSTOM_DATA);
        NbtCompound nbt = (existing != null) ? existing.copyNbt() : new NbtCompound();
        nbt.putString(CUSTOM_TYPE_KEY, type);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

    public static String getCustomItemType(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (data == null) return null;
        return data.copyNbt().getString(CUSTOM_TYPE_KEY).orElse("");
    }
    public static boolean isType(ItemStack stack, String type) {
        return type.equals(getCustomItemType(stack));
    }

    public static void setSlayerLore(ItemStack stack, List<String> lines) {
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        for (String line : lines) {
            // Convert §7 formatting to proper gray formatting
            String formattedLine = line.replace("§7", "");
            lore.add(Text.literal(formattedLine).formatted(Formatting.GRAY));
        }
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[MFLUX: Anomalous boss residue | Charadrius specimen]").formatted(Formatting.DARK_GRAY));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    public static ItemStack createUndeadHeart() {
        ItemStack stack = new ItemStack(Items.NETHER_STAR);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("❤ Undead Heart").formatted(Formatting.RED, Formatting.BOLD));
        setSlayerLore(stack, List.of(
            "§7A pulsating heart from a powerful Undead boss.",
            "§7Increases maximum health when held."
        ));
        setCustomItemType(stack, UNDEAD_HEART);
        return stack;
    }

    public static ItemStack createSpectralQuiver() {
        ItemStack stack = new ItemStack(Items.LEATHER_HORSE_ARMOR); 
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("🏹 Spectral Quiver").formatted(Formatting.AQUA, Formatting.BOLD));
        setSlayerLore(stack, List.of(
            "§7A quiver from the Spectral Archer.",
            "§7Arrows have a chance to phase through walls."
        ));
        setCustomItemType(stack, SPECTRAL_QUIVER);
        return stack;
    }

    public static ItemStack createEchoingCore() {
        ItemStack stack = new ItemStack(Items.ECHO_SHARD);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("🌀 Echoing Core").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        setSlayerLore(stack, List.of(
            "§7The pulsating core of a Deep Dark boss.",
            "§7Emits a sonic boom when used."
        ));
        setCustomItemType(stack, ECHOING_CORE);
        return stack;
    }

    public static ItemStack createBouncySlime() {
        ItemStack stack = new ItemStack(Items.SLIME_BALL);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("🟢 Bouncy Slime").formatted(Formatting.GREEN, Formatting.BOLD));
        setSlayerLore(stack, List.of(
            "§7A concentrated blob of slime.",
            "§7Gives jump boost and fall resistance."
        ));
        setCustomItemType(stack, BOUNCY_SLIME);
        return stack;
    }

    public static ItemStack createEnderSword() {
        ItemStack stack = new ItemStack(Items.NETHERITE_SWORD);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("⚔ Ender Sword").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        setSlayerLore(stack, List.of(
            "§7A blade forged from the ender dragon's breath.",
            "§7Teleports you behind your target on hit."
        ));
        setCustomItemType(stack, ENDER_SWORD);
        return stack;
    }

    public static ItemStack createAbyssalBlade() {
        ItemStack stack = new ItemStack(Items.NETHERITE_SWORD);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("🌑 Abyssal Blade").formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        setSlayerLore(stack, List.of(
            "§7A blade from the deepest abyss.",
            "§7Deals bonus damage to blinded targets."
        ));
        setCustomItemType(stack, ABYSSAL_BLADE);
        return stack;
    }

    public static boolean canUseSlayerSword(ServerPlayerEntity player, ItemStack sword) {
        if (!isSlayerSword(sword)) return true;

        SlayerManager.SlayerType swordType = getSwordSlayerType(sword);
        if (swordType == null) return true;

        int playerLevel = SlayerData.getSlayerLevel(player.getUuidAsString(), swordType);
        int requiredLevel = getSwordLevelRequirement(sword);

        return playerLevel >= requiredLevel;
    }
// SLAYER ARMOR - T1 (Crafted with Chunks)
// ============================================================

    public static final int T1_ARMOR_LEVEL_REQ = 4;
    public static final int T2_ARMOR_LEVEL_REQ = 5;
    public static final int ENDER_PHASE_HELMET_LEVEL_REQ = 7;

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
        player.sendMessage(Text.literal("✔ Received full " + type.displayName + " Bounty Armor" + tierName + " set!")
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
        ItemStack core = createCustomCrafterCoreT1();
        if (!player.getInventory().insertStack(core)) {
            player.dropItem(core, false);
        }
        core.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⚙ Custom Crafting Core (T1)").formatted(Formatting.GREEN, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("✦ Crafting Component").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Used to craft Tier 1 custom").formatted(Formatting.GRAY));
        lore.add(Text.literal("armor and weapon pieces.").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Required for:").formatted(Formatting.YELLOW));
        lore.add(Text.literal("• All T1 Custom Armor Sets").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[Drop from any slayer boss]").formatted(Formatting.DARK_GRAY));
        core.set(DataComponentTypes.LORE, new LoreComponent(lore));
        core.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(core, "custom_crafter_core_t1");
        player.sendMessage(Text.literal("✔ Received " + type.displayName + " Core!")
                .formatted(Formatting.GREEN), false);
    }

    public static ItemStack createCustomCrafterCoreT1() {
        ItemStack core = new ItemStack(Items.HEAVY_CORE);
        core.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⚙ Custom Crafting Core (T1)").formatted(Formatting.GREEN, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("✦ Crafting Component").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Used to craft Tier 1 custom").formatted(Formatting.GRAY));
        lore.add(Text.literal("armor and weapon pieces.").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Required for:").formatted(Formatting.YELLOW));
        lore.add(Text.literal("• All T1 Custom Armor Sets").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[Drop from any slayer boss]").formatted(Formatting.DARK_GRAY));
        core.set(DataComponentTypes.LORE, new LoreComponent(lore));
        core.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(core, "custom_crafter_core_t1");
        return core;
    }

    public static ItemStack createCustomCrafterCoreT2() {
        ItemStack core = new ItemStack(Items.HEAVY_CORE);
        core.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⚙ Custom Crafting Core (T2)").formatted(Formatting.AQUA, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("✦ Crafting Component").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Used to craft Tier 2 custom").formatted(Formatting.GRAY));
        lore.add(Text.literal("armor and weapon pieces.").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Required for:").formatted(Formatting.YELLOW));
        lore.add(Text.literal("• All T2 Custom Armor Sets").formatted(Formatting.AQUA));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[Drop from any slayer boss T2+]").formatted(Formatting.DARK_GRAY));
        core.set(DataComponentTypes.LORE, new LoreComponent(lore));
        core.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(core, "custom_crafter_core_t2");
        return core;
    }

    public static ItemStack createCustomCrafterCoreT3() {
        ItemStack core = new ItemStack(Items.HEAVY_CORE);
        core.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⚙ Custom Crafting Core (T3)").formatted(Formatting.GOLD, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("✦ Legendary Crafting Component").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Used to craft Tier 3 custom").formatted(Formatting.GRAY));
        lore.add(Text.literal("armor and weapon pieces.").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Required for:").formatted(Formatting.YELLOW));
        lore.add(Text.literal("• All T3 Custom Armor Sets").formatted(Formatting.GOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[Rare drop from high-tier bosses]").formatted(Formatting.DARK_GRAY));
        core.set(DataComponentTypes.LORE, new LoreComponent(lore));
        core.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(core, "custom_crafter_core_t3");
        return core;
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

    public static ItemStack createZombieSword() {
        return createSlayerSword(SlayerManager.SlayerType.ZOMBIE);
    }

    public static ItemStack createSpiderSword() {
        return createSlayerSword(SlayerManager.SlayerType.SPIDER);
    }

    public static ItemStack createTheGavel() {
        return CustomItemHandler.createTheGavel();
    }

    public static ItemStack createZombieHelmet() {
        return createZombieBerserkerHelmet();
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

    // ── Defence constants for special (non-set) armour pieces ──
    // Reduced to ~40% of original values so full BiS stacking stays below 85%.
    public static final double BERSERKER_HELMET_BOSS_REDUCTION = 0.06;  // 6% vs zombie boss
    public static final double BERSERKER_HELMET_ALL_REDUCTION  = 0.20;  // 20% vs all bosses
    public static final double IRON_GOLEM_CHEST_BOSS_REDUCTION     = 0.14;  // 14% vs warden boss
    public static final double IRON_GOLEM_CHEST_ALL_REDUCTION      = 0.25;  // 25% vs all bosses
    public static final double SPIDER_LEGS_BOSS_REDUCTION      = 0.08;  // 8% vs spider boss
    public static final double SPIDER_LEGS_ALL_REDUCTION       = 0.22;  // 22% vs all bosses
    public static final double SLIME_BOOTS_BOSS_REDUCTION      = 0.08;  // 8% vs slime boss
    public static final double SLIME_BOOTS_ALL_REDUCTION       = 0.22;  // 22% vs all bosses
    public static final double VOIDWALKER_ALL_REDUCTION        = 0.04;  // 4% vs all bosses
    public static final double VOIDWALKER_BOSS_REDUCTION       = 0.12;  // 12% extra vs Enderman boss

    public static ArmorStats getArmorStats(SlayerManager.SlayerType type, int tier) {
        return switch (type) {
            case ZOMBIE -> tier == 1 ? new ArmorStats(6, 1, 0.20, 0.04) : new ArmorStats(10, 3, 0.28, 0.08);
            case SPIDER -> tier == 1 ? new ArmorStats(6, 1, 0.20, 0.04) : new ArmorStats(10, 3, 0.28, 0.08);
            case SKELETON -> tier == 1 ? new ArmorStats(8, 2, 0.20, 0.04) : new ArmorStats(12, 4, 0.28, 0.10);
            case SLIME -> tier == 1 ? new ArmorStats(10, 3, 0.24, 0.06) : new ArmorStats(14, 5, 0.32, 0.12);
            case ENDERMAN -> tier == 1 ? new ArmorStats(10, 3, 0.24, 0.06) : new ArmorStats(14, 5, 0.32, 0.12);
            case IRON_GOLEM -> tier == 1 ? new ArmorStats(12, 4, 0.28, 0.08) : new ArmorStats(16, 6, 0.36, 0.14);
            case PIGLIN -> tier == 1 ? new ArmorStats(10, 3, 0.24, 0.06) : new ArmorStats(14, 5, 0.32, 0.12);
        };
    }

    // ============================================================
// SLAYER ARMOR - T1 & T2 WITH CUSTOM ATTRIBUTES
// ============================================================
    public static ItemStack createSlayerHelmet(SlayerManager.SlayerType type, int tier) {
        return tier == 1 ? createT1Helmet(type) : createT2Helmet(type);
    }

    public static ItemStack createSlayerChestplate(SlayerManager.SlayerType type, int tier) {
        return tier == 1 ? createT1Chestplate(type) : createT2Chestplate(type);
    }

    public static ItemStack createSlayerLeggings(SlayerManager.SlayerType type, int tier) {
        return tier == 1 ? createT1Leggings(type) : createT2Leggings(type);
    }

    public static ItemStack createSlayerBoots(SlayerManager.SlayerType type, int tier) {
        return tier == 1 ? createT1Boots(type) : createT2Boots(type);
    }

    // Per-piece multipliers for distributing boss stats across armor slots
    private static final double PIECE_MULT_HELMET     = 0.20;
    private static final double PIECE_MULT_CHESTPLATE = 0.40;
    private static final double PIECE_MULT_LEGGINGS   = 0.25;
    private static final double PIECE_MULT_BOOTS      = 0.15;

    /** Returns the per-piece boss-reduction multiplier for the named piece. */
    public static double getPieceBossMultiplier(String piece) {
        return switch (piece) {
            case "Helmet"     -> PIECE_MULT_HELMET;
            case "Chestplate" -> PIECE_MULT_CHESTPLATE;
            case "Leggings"   -> PIECE_MULT_LEGGINGS;
            case "Boots"      -> PIECE_MULT_BOOTS;
            default           -> 0.25;
        };
    }

    private static List<Text> buildSlayerArmorLore(SlayerManager.SlayerType type, int tier, String piece, ArmorStats stats) {
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        String mfluxCrd = switch (type) {
            case ZOMBIE   -> "CRD-001";
            case SPIDER   -> "CRD-002";
            case SKELETON -> "CRD-003";
            case SLIME    -> "CRD-004";
            case ENDERMAN -> "CRD-005";
            case IRON_GOLEM   -> "CRD-006";
            case PIGLIN   -> "CRD-007";
        };
        String mfluxLabel = tier == 1
                ? "§8[MFLUX: Field-tested containment gear | " + mfluxCrd + "]"
                : "§8[MFLUX: Upgraded containment gear | " + mfluxCrd + "-T2]";
        lore.add(Text.literal(mfluxLabel).formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(type.color));

        // Per-piece armor/toughness values (already applied via attributes; show in lore)
        double pieceMult = getPieceBossMultiplier(piece);
        double armorValue = stats.armor * pieceMult;
        double toughValue = stats.toughness * pieceMult;
        lore.add(Text.literal("🛡 Armor: +" + (int) Math.round(armorValue)).formatted(Formatting.BLUE));
        lore.add(Text.literal("🛡 Toughness: +" + (int) Math.round(toughValue)).formatted(Formatting.BLUE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ BOSS DEFENSE ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal(""));

        // Per-piece boss reductions
        int pieceSpecificDef = (int) Math.round(stats.bossReduction * pieceMult * 100);
        int pieceAllDef      = (int) Math.round(stats.allBossReduction * pieceMult * 100);
        if (pieceSpecificDef < 1) pieceSpecificDef = 1;
        if (pieceAllDef < 1) pieceAllDef = 1;

        // Full-set boss reductions (actual capped values)
        // T1 full set caps at 26% (20% per-piece + 6% set bonus), T2 at 38% (28% + 10%)
        int fullSetBossDef = (tier == 1) ? 26 : 38;
        int fullSetAllDef  = (int) Math.round(stats.allBossReduction * 100);

        lore.add(Text.literal("This piece: ")
                .formatted(Formatting.GRAY)
                .append(Text.literal("-" + pieceSpecificDef + "% vs " + type.displayName + " Boss")
                        .formatted(Formatting.GREEN, Formatting.BOLD)));
        lore.add(Text.literal("This piece: ")
                .formatted(Formatting.GRAY)
                .append(Text.literal("-" + pieceAllDef + "% vs All Bosses")
                        .formatted(Formatting.GREEN)));
        lore.add(Text.literal("Full set: ")
                .formatted(Formatting.GOLD)
                .append(Text.literal("-" + fullSetBossDef + "% vs " + type.displayName + " Boss")
                        .formatted(Formatting.YELLOW, Formatting.BOLD)));
        lore.add(Text.literal("Full set: ")
                .formatted(Formatting.GOLD)
                .append(Text.literal("-" + fullSetAllDef + "% vs All Bosses")
                        .formatted(Formatting.YELLOW)));

        if (tier >= 2) {
            lore.add(Text.literal("+15% XP from " + type.displayName + " bounties").formatted(Formatting.AQUA));
        }

        // Per-piece ability
        String pieceBuff = tier == 1 ? getT1PieceBuff(type, piece) : getT2PieceBuff(type, piece);
        if (pieceBuff != null) {
            lore.add(Text.literal(""));
            lore.add(Text.literal("━━━ PIECE ABILITY ━━━").formatted(Formatting.AQUA));
            lore.add(Text.literal("✦ " + pieceBuff).formatted(Formatting.WHITE));
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
        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        chestplate.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0x004D4D));

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
                                6.0,   // buffed: 4 → 6
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.CHEST)
                .add(EntityAttributes.MAX_HEALTH,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "warden_health"),
                                24.0,  // buffed: +10 hearts → +12 hearts
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
        lore.add(Text.literal("§8[MFLUX: CRD-006 | Warden-class containment armor]"));
        lore.add(Text.literal("§8[STATUS: All field tests resulted in operator fatality]"));
        lore.add(Text.literal("§8[NOTE: \"It doesn't protect you. It makes the Warden hesitate.\"]"));
        lore.add(Text.literal("§7\"Recovered from MFLUX Research Station Sigma-7.\"").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        lore.add(Text.literal("§7\"No operative has successfully worn this in combat\"").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        lore.add(Text.literal("§7\"and survived to file a report.\"").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        lore.add(Text.literal("§7\"The containment field appears to disrupt the\"").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        lore.add(Text.literal("§7\"Warden's echolocation — but at tremendous cost\"").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        lore.add(Text.literal("§7\"to the wearer's nervous system.\"").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_AQUA));
        lore.add(Text.literal("🛡 Armor: +12").formatted(Formatting.BLUE));
        lore.add(Text.literal("🛡 Toughness: +6").formatted(Formatting.BLUE));
        lore.add(Text.literal("❤ Health: +12 Hearts").formatted(Formatting.RED));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.DARK_AQUA));
        lore.add(Text.literal("👁 ESP: See noisy entities (64 blocks)").formatted(Formatting.AQUA));
        lore.add(Text.literal("🌙 Night Vision: Permanent").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🛡 Darkness Immunity").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("🛡 Full Knockback Resistance").formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ BOSS DEFENSE ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("vs All Bosses: ").formatted(Formatting.GRAY)
                .append(Text.literal("-25% damage").formatted(Formatting.GREEN)));
        lore.add(Text.literal("vs Warden Bosses: ").formatted(Formatting.GRAY)
                .append(Text.literal("-39% total (-25 + -14 extra)").formatted(Formatting.GREEN, Formatting.BOLD)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires: Warden Bounty Lvl " + IRON_GOLEM_CHESTPLATE_LEVEL_REQ)
                .formatted(Formatting.RED));

        chestplate.set(DataComponentTypes.LORE, new LoreComponent(lore));
        chestplate.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        setCustomItemId(chestplate, "warden_chestplate");
        return chestplate;
    }

    public static ItemStack createSpiderLeggings() {
        ItemStack leggings = new ItemStack(Items.LEATHER_LEGGINGS);

        leggings.set(DataComponentTypes.DYED_COLOR,
                new net.minecraft.component.type.DyedColorComponent(0x8B0000));

        leggings.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🕷 Venomous Crawler Leggings")
                        .formatted(Formatting.DARK_RED, Formatting.BOLD));

        AttributeModifiersComponent attributes = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "spider_legs_armor"),
                                10.0,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.LEGS)
                .add(EntityAttributes.ARMOR_TOUGHNESS,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "spider_legs_toughness"),
                                4.0,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.LEGS)
                .add(EntityAttributes.MOVEMENT_SPEED,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "spider_speed"),
                                0.50,
                                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                        ),
                        AttributeModifierSlot.LEGS)
                .build();
        leggings.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attributes);

        leggings.set(DataComponentTypes.MAX_DAMAGE, 750);
        leggings.set(DataComponentTypes.DAMAGE, 0);

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Woven from the silk of").formatted(Formatting.GRAY));
        lore.add(Text.literal("a thousand slain bounty spiders.").formatted(Formatting.GRAY));
        lore.add(Text.literal("§8[MFLUX: CRD-002 spider-silk containment leggings]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_RED));
        lore.add(Text.literal("🛡 Armor: +10").formatted(Formatting.BLUE));
        lore.add(Text.literal("🛡 Toughness: +4").formatted(Formatting.BLUE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.DARK_RED));
        lore.add(Text.literal("🕸 Web Immunity: Walk through webs").formatted(Formatting.WHITE));
        lore.add(Text.literal("☠ Poison Immunity").formatted(Formatting.GREEN));
        lore.add(Text.literal("⚡ Movement Speed: +50%").formatted(Formatting.AQUA));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ BOSS DEFENSE ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("vs All Bosses: ").formatted(Formatting.GRAY)
                .append(Text.literal("-22% damage").formatted(Formatting.GREEN)));
        lore.add(Text.literal("vs Spider Bosses: ").formatted(Formatting.GRAY)
                .append(Text.literal("-30% total (-22 + -8 extra)").formatted(Formatting.GREEN, Formatting.BOLD)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires: Spider Bounty Lvl " + SPIDER_LEGGINGS_LEVEL_REQ)
                .formatted(Formatting.RED));

        leggings.set(DataComponentTypes.LORE, new LoreComponent(lore));
        leggings.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        setCustomItemId(leggings, "spider_leggings");
        return leggings;
    }

    public static ItemStack createSkeletonBow() {
        ItemStack bow = new ItemStack(Items.BOW);

        bow.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🦴 Bone Desperado Bow")
                        .formatted(Formatting.YELLOW, Formatting.BOLD));

        bow.set(DataComponentTypes.MAX_DAMAGE, 3840);
        bow.set(DataComponentTypes.DAMAGE, 0);

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Carved from the bones of a").formatted(Formatting.GRAY));
        lore.add(Text.literal("thousand fallen archers.").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🏹 Standard arrow mechanics").formatted(Formatting.WHITE));
        lore.add(Text.literal("✦ +2x damage to Skeleton Bosses").formatted(Formatting.AQUA));
        lore.add(Text.literal("💀 Headshot: 500% damage (fully-charged)").formatted(Formatting.RED));
        lore.add(Text.literal("⏱ Infinite Durability").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("MFLUX Research Division").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("§8[MFLUX: CRD-003 reanimated projectile weapon]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Skeleton Core: projectiles self-correct in flight").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("⚠ Requires: Skeleton Bounty Lvl " + SKELETON_BOW_LEVEL_REQ)
                .formatted(Formatting.RED));

        bow.set(DataComponentTypes.LORE, new LoreComponent(lore));
        bow.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        setCustomItemId(bow, "skeleton_bow");
        return bow;
    }

    public static ItemStack createSlimeBoots() {
        ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);

        boots.set(DataComponentTypes.DYED_COLOR,
                new net.minecraft.component.type.DyedColorComponent(0x7CFC00));

        boots.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🟢 Gelatinous Rustler Boots")
                        .formatted(Formatting.GREEN, Formatting.BOLD));

        // CUSTOM ARMOR ATTRIBUTES - buffed (Item #6)
        AttributeModifiersComponent attributes = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "slime_boots_armor"),
                                6.0,   // buffed: 4 → 6
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.FEET)
                .add(EntityAttributes.ARMOR_TOUGHNESS,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "slime_boots_toughness"),
                                3.0,   // buffed: 2 → 3
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
        lore.add(Text.literal(""));
        lore.add(Text.literal("Bouncy boots made from").formatted(Formatting.GRAY));
        lore.add(Text.literal("condensed slime.").formatted(Formatting.GRAY));
        lore.add(Text.literal("§8[MFLUX: CRD-004 gelatinous containment boots]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Some say it saves you").formatted(Formatting.DARK_AQUA, Formatting.OBFUSCATED, Formatting.ITALIC));
        lore.add(Text.literal("from death").formatted(Formatting.DARK_AQUA, Formatting.OBFUSCATED, Formatting.ITALIC));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.GREEN));
        lore.add(Text.literal("🛡 Armor: +6").formatted(Formatting.BLUE));
        lore.add(Text.literal("🛡 Toughness: +3").formatted(Formatting.BLUE));
        lore.add(Text.literal("⬆ Jump Boost: III").formatted(Formatting.AQUA));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.GREEN));
        lore.add(Text.literal("⬆ Jump Boost III").formatted(Formatting.AQUA));
        lore.add(Text.literal("🛡 No Fall Damage").formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ BOSS DEFENSE ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("vs All Bosses: ").formatted(Formatting.GRAY)
                .append(Text.literal("-22% damage").formatted(Formatting.GREEN)));
        lore.add(Text.literal("vs Slime Bosses: ").formatted(Formatting.GRAY)
                .append(Text.literal("-30% total (-22 + -8 extra)").formatted(Formatting.GREEN, Formatting.BOLD)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires: Slime Bounty Lvl " + SLIME_BOOTS_LEVEL_REQ)
                .formatted(Formatting.RED));
        boots.set(DataComponentTypes.LORE, new LoreComponent(lore));
        boots.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        setCustomItemId(boots, "slime_boots");
        return boots;
    }
// ============================================================



    // ============================================================
    // ============================================================
// T2 ARMOR - legacy type-specific methods (delegate to unified system)
// ============================================================
    /** @deprecated Use {@link #createT2Helmet(SlayerManager.SlayerType)} with ZOMBIE */
    public static ItemStack createT2ZombieHelmet()       { return createT2Helmet(SlayerManager.SlayerType.ZOMBIE); }
    /** @deprecated Use {@link #createT2Chestplate(SlayerManager.SlayerType)} with ZOMBIE */
    public static ItemStack createT2ZombieChestplate()   { return createT2Chestplate(SlayerManager.SlayerType.ZOMBIE); }
    /** @deprecated Use {@link #createT2Leggings(SlayerManager.SlayerType)} with ZOMBIE */
    public static ItemStack createT2ZombieLeggings()     { return createT2Leggings(SlayerManager.SlayerType.ZOMBIE); }
    /** @deprecated Use {@link #createT2Boots(SlayerManager.SlayerType)} with ZOMBIE */
    public static ItemStack createT2ZombieBoots()        { return createT2Boots(SlayerManager.SlayerType.ZOMBIE); }

    /** @deprecated Use {@link #createT2Helmet(SlayerManager.SlayerType)} with SPIDER */
    public static ItemStack createT2SpiderHelmet()       { return createT2Helmet(SlayerManager.SlayerType.SPIDER); }
    /** @deprecated Use {@link #createT2Chestplate(SlayerManager.SlayerType)} with SPIDER */
    public static ItemStack createT2SpiderChestplate()   { return createT2Chestplate(SlayerManager.SlayerType.SPIDER); }
    /** @deprecated Use {@link #createT2Leggings(SlayerManager.SlayerType)} with SPIDER */
    public static ItemStack createT2SpiderLeggings()     { return createT2Leggings(SlayerManager.SlayerType.SPIDER); }
    /** @deprecated Use {@link #createT2Boots(SlayerManager.SlayerType)} with SPIDER */
    public static ItemStack createT2SpiderBoots()        { return createT2Boots(SlayerManager.SlayerType.SPIDER); }

    /** @deprecated Use {@link #createT2Helmet(SlayerManager.SlayerType)} with SKELETON */
    public static ItemStack createT2SkeletonHelmet()     { return createT2Helmet(SlayerManager.SlayerType.SKELETON); }
    /** @deprecated Use {@link #createT2Chestplate(SlayerManager.SlayerType)} with SKELETON */
    public static ItemStack createT2SkeletonChestplate() { return createT2Chestplate(SlayerManager.SlayerType.SKELETON); }
    /** @deprecated Use {@link #createT2Leggings(SlayerManager.SlayerType)} with SKELETON */
    public static ItemStack createT2SkeletonLeggings()   { return createT2Leggings(SlayerManager.SlayerType.SKELETON); }
    /** @deprecated Use {@link #createT2Boots(SlayerManager.SlayerType)} with SKELETON */
    public static ItemStack createT2SkeletonBoots()      { return createT2Boots(SlayerManager.SlayerType.SKELETON); }

    /** @deprecated Use {@link #createT2Helmet(SlayerManager.SlayerType)} with SLIME */
    public static ItemStack createT2SlimeHelmet()        { return createT2Helmet(SlayerManager.SlayerType.SLIME); }
    /** @deprecated Use {@link #createT2Chestplate(SlayerManager.SlayerType)} with SLIME */
    public static ItemStack createT2SlimeChestplate()    { return createT2Chestplate(SlayerManager.SlayerType.SLIME); }
    /** @deprecated Use {@link #createT2Leggings(SlayerManager.SlayerType)} with SLIME */
    public static ItemStack createT2SlimeLeggings()      { return createT2Leggings(SlayerManager.SlayerType.SLIME); }
    /** @deprecated Use {@link #createT2Boots(SlayerManager.SlayerType)} with SLIME */
    public static ItemStack createT2SlimeBoots()         { return createT2Boots(SlayerManager.SlayerType.SLIME); }

    /** @deprecated Use {@link #createT2Helmet(SlayerManager.SlayerType)} with ENDERMAN */
    public static ItemStack createT2EndermanHelmet()     { return createT2Helmet(SlayerManager.SlayerType.ENDERMAN); }
    /** @deprecated Use {@link #createT2Chestplate(SlayerManager.SlayerType)} with ENDERMAN */
    public static ItemStack createT2EndermanChestplate() { return createT2Chestplate(SlayerManager.SlayerType.ENDERMAN); }
    /** @deprecated Use {@link #createT2Leggings(SlayerManager.SlayerType)} with ENDERMAN */
    public static ItemStack createT2EndermanLeggings()   { return createT2Leggings(SlayerManager.SlayerType.ENDERMAN); }
    /** @deprecated Use {@link #createT2Boots(SlayerManager.SlayerType)} with ENDERMAN */
    public static ItemStack createT2EndermanBoots()      { return createT2Boots(SlayerManager.SlayerType.ENDERMAN); }

    /** @deprecated Use {@link #createT2Helmet(SlayerManager.SlayerType)} with IRON_GOLEM */
    public static ItemStack createT2WardenHelmet()       { return createT2Helmet(SlayerManager.SlayerType.IRON_GOLEM); }
    /** @deprecated Use {@link #createT2Chestplate(SlayerManager.SlayerType)} with IRON_GOLEM */
    public static ItemStack createT2WardenChestplate()   { return createT2Chestplate(SlayerManager.SlayerType.IRON_GOLEM); }
    /** @deprecated Use {@link #createT2Leggings(SlayerManager.SlayerType)} with IRON_GOLEM */
    public static ItemStack createT2WardenLeggings()     { return createT2Leggings(SlayerManager.SlayerType.IRON_GOLEM); }
    /** @deprecated Use {@link #createT2Boots(SlayerManager.SlayerType)} with IRON_GOLEM */
    public static ItemStack createT2WardenBoots()        { return createT2Boots(SlayerManager.SlayerType.IRON_GOLEM); }

    // ============================================================
// MISSING DETECTION METHODS
// ============================================================

    public static boolean isUpgradedSlayerSword(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        // Prefer NBT-based identification (new swords)
        NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (data != null) {
            NbtCompound nbt = data.copyNbt();
            if (nbt.contains(BOUNTY_SWORD_TYPE_TAG) && nbt.getBoolean(BOUNTY_SWORD_UPGRADED_TAG, false)) {
                return true;
            }
        }
        // Fallback: name-based for backward compat
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return false;
        String n = name.getString();
        return n.contains("Slayer Sword II") || n.contains("Bounty Sword II");
    }

    public static boolean isSpiderLeggings(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        // Check custom ID first (preferred)
        if (hasCustomItemId(stack, "spider_leggings")) return true;
        // Fallback: name-based for old items
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return false;
        String n = name.getString();
        return n.contains("Venomous") || n.contains("Crawler") ||
                (n.contains("Spider") && n.contains("Leggings"));
    }

    public static boolean isSkeletonBow(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.getItem() != Items.BOW) return false;
        // Check custom ID first (preferred)
        if (hasCustomItemId(stack, "skeleton_bow")) return true;
        // Fallback: name-based for old items
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return false;
        String n = name.getString();
        return n.contains("Bone") || n.contains("Desperado") || n.contains("Skeleton");
    }

    public static boolean isSlimeBoots(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        // Check custom ID first (preferred)
        if (hasCustomItemId(stack, "slime_boots")) return true;
        // Fallback: name-based for old items
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return false;
        String n = name.getString();
        return n.contains("Slime") || n.contains("Gelatinous") || n.contains("Rustler");
    }

    public static boolean isWardenChestplate(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        // Check custom ID first (preferred) - only matches legendary chestplate
        if (hasCustomItemId(stack, "warden_chestplate")) return true;
        // Fallback: name-based for old items - exclude T2 armor (has " II")
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return false;
        String n = name.getString();
        if (n.contains(" II")) return false; // T2 armor, not legendary
        return n.contains("Sculk") || n.contains("Warden") || n.contains("Terror");
    }

    public static boolean isCrownOfGreed(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (hasCustomItemId(stack, "crown_of_greed")) return true;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        return name != null && name.getString().contains("Crown of Greed");
    }

    public static boolean isCrownOfMidas(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (hasCustomItemId(stack, "crown_of_midas")) return true;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        return name != null && name.getString().contains("Crown of Midas");
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
        int level = SlayerData.getSlayerLevel(player.getUuidAsString(), SlayerManager.SlayerType.IRON_GOLEM);
        return level >= IRON_GOLEM_CHESTPLATE_LEVEL_REQ;
    }

    public static boolean canUseEnderPhaseHelmet(ServerPlayerEntity player) {
        int level = SlayerData.getSlayerLevel(player.getUuidAsString(), SlayerManager.SlayerType.ENDERMAN);
        return level >= ENDER_PHASE_HELMET_LEVEL_REQ;
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
// ITEM CREATION METHODS
// ============================================================

    public static ItemStack createZombieBerserkerHelmet() {
        ItemStack helmet = new ItemStack(Items.LEATHER_HELMET);
        helmet.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0x1A6600));

        helmet.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("☠ Zombie Berserker Helmet")
                        .formatted(Formatting.DARK_GREEN, Formatting.BOLD));

        // Set high durability
        helmet.set(DataComponentTypes.MAX_DAMAGE, BERSERKER_HELMET_DURABILITY);
        helmet.set(DataComponentTypes.DAMAGE, 0);

        // Add baked-in armor attributes
        AttributeModifiersComponent attributes = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "berserker_helmet_armor"),
                                10.0,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.HEAD)
                .add(EntityAttributes.ARMOR_TOUGHNESS,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "berserker_helmet_toughness"),
                                5.0,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.HEAD)
                .add(EntityAttributes.MAX_HEALTH,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "berserker_helmet_health"),
                                8.0,  // +4 hearts
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.HEAD)
                .build();
        helmet.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attributes);

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal(""));
        lore.add(Text.literal("A cursed helm forged from the").formatted(Formatting.GRAY));
        lore.add(Text.literal("essence of fallen bounty bosses.").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GREEN));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+10").formatted(Formatting.BLUE)));
        lore.add(Text.literal("🛡 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+5").formatted(Formatting.BLUE)));
        lore.add(Text.literal("❤ Bonus Health: ").formatted(Formatting.WHITE)
                .append(Text.literal("+4 Hearts").formatted(Formatting.RED)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Damage Dealt: ").formatted(Formatting.WHITE)
                .append(Text.literal("+300% MORE").formatted(Formatting.GREEN, Formatting.BOLD)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ BOSS DEFENSE ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("vs All Bosses: ").formatted(Formatting.GRAY)
                .append(Text.literal("-20% damage").formatted(Formatting.GREEN)));
        lore.add(Text.literal("vs Zombie Bosses: ").formatted(Formatting.GRAY)
                .append(Text.literal("-26% total (-20 + -6 extra)").formatted(Formatting.GREEN, Formatting.BOLD)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[MFLUX: CRD-001 augmented containment helm | Berserker-class]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires: Zombie Bounty Lvl " + BERSERKER_HELMET_LEVEL_REQ)
                .formatted(Formatting.RED));

        helmet.set(DataComponentTypes.LORE, new LoreComponent(lore));
        helmet.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        setCustomItemId(helmet, "zombie_berserker_helmet");
        return helmet;
    }

    // Add this enum inside SlayerItems class
    public enum ArmorPiece {
        HELMET("Helmet", Items.NETHERITE_HELMET),
        CHESTPLATE("Chestplate", Items.NETHERITE_CHESTPLATE),
        LEGGINGS("Leggings", Items.NETHERITE_LEGGINGS),
        BOOTS("Boots", Items.NETHERITE_BOOTS);

        public final String displayName;
        public final Item baseItem;

        ArmorPiece(String displayName, Item baseItem) {
            this.displayName = displayName;
            this.baseItem = baseItem;
        }
    }

    // Add these missing methods to SlayerItems.java
    public static boolean isT1SlayerArmor(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return false;
        String n = name.getString();
        // T1 armor does NOT contain " II"
        if (n.contains(" II")) return false;
        return n.contains("Outlaw") || n.contains("Bandit") || n.contains("Desperado") ||
                n.contains("Rustler") || n.contains("Phantom") || n.contains("Terror") ||
                n.contains("Hunter");
    }

    public static SlayerManager.SlayerType getArmorSlayerType(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return null;
        String n = name.getString();

        if (n.contains("Undying") || n.contains("Outlaw") || n.contains("Zombie"))
            return SlayerManager.SlayerType.ZOMBIE;
        if (n.contains("Venomous") || n.contains("Bandit") || n.contains("Spider"))
            return SlayerManager.SlayerType.SPIDER;
        if (n.contains("Bone") || n.contains("Desperado") || n.contains("Skeleton"))
            return SlayerManager.SlayerType.SKELETON;
        if (n.contains("Gelatinous") || n.contains("Rustler") || n.contains("Slime"))
            return SlayerManager.SlayerType.SLIME;
        if (n.contains("Void") || n.contains("Phantom") || n.contains("Enderman"))
            return SlayerManager.SlayerType.ENDERMAN;
        if (n.contains("Sculk") || n.contains("Terror") || n.contains("Warden"))
            return SlayerManager.SlayerType.IRON_GOLEM;

        return null;
    }

    public static ArmorPiece getArmorPiece(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return null;
        String n = name.getString();

        if (n.contains("Helmet")) return ArmorPiece.HELMET;
        if (n.contains("Chestplate")) return ArmorPiece.CHESTPLATE;
        if (n.contains("Leggings")) return ArmorPiece.LEGGINGS;
        if (n.contains("Boots")) return ArmorPiece.BOOTS;

        return null;
    }


    /** @deprecated Use {@link #createSpiderLeggings()} instead. */
    @Deprecated
    public static ItemStack createVenomousCrawlerLeggings() {
        return createSpiderLeggings();
    }


    private static String getT1ArmorName(SlayerManager.SlayerType type, String piece) {
        return switch (type) {
            case ZOMBIE -> "Undying Outlaw " + piece;
            case SPIDER -> "Venomous Bandit " + piece;
            case SKELETON -> "Bone Desperado " + piece;
            case SLIME -> "Gelatinous Rustler " + piece;
            case ENDERMAN -> "Void Phantom " + piece;
            case IRON_GOLEM -> "Sculk Terror " + piece;
            case PIGLIN -> "Gilded Ravager " + piece;
        };
    }
    // ============================================================
// ARMOR COLOR MAPPING
// ============================================================
    private static int getArmorColor(SlayerManager.SlayerType type) {
        return switch (type) {
            case ZOMBIE -> 0x3D5C1F;      // Dark rotting green
            case SPIDER -> 0x8B0000;       // Dark red
            case SKELETON -> 0xD3D3C4;     // Bone white/gray
            case SLIME -> 0x7CFC00;        // Lime green
            case ENDERMAN -> 0x1A0033;     // Dark purple
            case IRON_GOLEM -> 0x0D4D4D;       // Dark teal/cyan
            case PIGLIN -> 0xFFD700;       // Gold
        };
    }

    // ============================================================
// T1 ARMOR - Dyed Leather with Basic Stats
// ============================================================
    public static final int T1_ARMOR_DURABILITY = 2000; // Custom durability - much higher
    public static final int T2_ARMOR_DURABILITY = 3500; // Higher durability
    public static final int BERSERKER_HELMET_DURABILITY = 5000; // Very high durability

    public static ItemStack createT1Helmet(SlayerManager.SlayerType type) {
        ItemStack helmet = new ItemStack(Items.LEATHER_HELMET);
        applyT1ArmorStats(helmet, type, "Helmet", 3, 1);
        setCustomItemId(helmet, type.name().toLowerCase() + "_helmet_t1");
        return helmet;
    }

    public static ItemStack createT1Chestplate(SlayerManager.SlayerType type) {
        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyT1ArmorStats(chestplate, type, "Chestplate", 5, 2);
        setCustomItemId(chestplate, type.name().toLowerCase() + "_chestplate_t1");
        return chestplate;
    }

    public static ItemStack createT1Leggings(SlayerManager.SlayerType type) {
        ItemStack leggings = new ItemStack(Items.LEATHER_LEGGINGS);
        applyT1ArmorStats(leggings, type, "Leggings", 4, 1);
        setCustomItemId(leggings, type.name().toLowerCase() + "_leggings_t1");
        return leggings;
    }

    public static ItemStack createT1Boots(SlayerManager.SlayerType type) {
        ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);
        applyT1ArmorStats(boots, type, "Boots", 2, 1);
        setCustomItemId(boots, type.name().toLowerCase() + "_boots_t1");
        return boots;
    }



    private static String getT1ArmorName(SlayerManager.SlayerType type) {
        return switch (type) {
            case ZOMBIE -> "Undying Outlaw";
            case SPIDER -> "Venomous Bandit";
            case SKELETON -> "Bone Desperado";
            case SLIME -> "Gelatinous Rustler";
            case ENDERMAN -> "Void Phantom";
            case IRON_GOLEM -> "Sculk Hunter";
            case PIGLIN -> "Gilded Ravager";
        };
    }

    // ============================================================
// T2 ARMOR - Dyed Leather with Better Stats (NO ABILITIES)
// ============================================================

    public static ItemStack createT2Helmet(SlayerManager.SlayerType type) {
        ItemStack helmet = new ItemStack(Items.LEATHER_HELMET);
        applyT2ArmorStats(helmet, type, "Helmet", 5, 2, 0);
        setCustomItemId(helmet, type.name().toLowerCase() + "_helmet_t2");
        return helmet;
    }

    public static ItemStack createT2Chestplate(SlayerManager.SlayerType type) {
        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyT2ArmorStats(chestplate, type, "Chestplate", 8, 3, 0);
        setCustomItemId(chestplate, type.name().toLowerCase() + "_chestplate_t2");
        return chestplate;
    }

    public static ItemStack createT2Leggings(SlayerManager.SlayerType type) {
        ItemStack leggings = new ItemStack(Items.LEATHER_LEGGINGS);
        applyT2ArmorStats(leggings, type, "Leggings", 6, 2, 0);
        setCustomItemId(leggings, type.name().toLowerCase() + "_leggings_t2");
        return leggings;
    }

    public static ItemStack createT2Boots(SlayerManager.SlayerType type) {
        ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);
        applyT2ArmorStats(boots, type, "Boots", 4, 2, 0);
        setCustomItemId(boots, type.name().toLowerCase() + "_boots_t2");
        return boots;
    }


    private static String getT2ArmorName(SlayerManager.SlayerType type) {
        return switch (type) {
            case ZOMBIE -> "Undying Outlaw";
            case SPIDER -> "Venomous Bandit";
            case SKELETON -> "Bone Desperado";
            case SLIME -> "Gelatinous Rustler";
            case ENDERMAN -> "Void Phantom";
            case IRON_GOLEM -> "Sculk Hunter";
            case PIGLIN -> "Gilded Ravager";
        };
    }

    private static int brightenColor(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        // Brighten by 30%
        r = Math.min(255, (int)(r * 1.3));
        g = Math.min(255, (int)(g * 1.3));
        b = Math.min(255, (int)(b * 1.3));

        return (r << 16) | (g << 8) | b;
    }

    // ============================================================
// GENERIC ARMOR CREATION METHODS
// ============================================================
    public static ItemStack createT1Armor(SlayerManager.SlayerType type, String piece) {
        return switch (piece) {
            case "Helmet" -> createT1Helmet(type);
            case "Chestplate" -> createT1Chestplate(type);
            case "Leggings" -> createT1Leggings(type);
            case "Boots" -> createT1Boots(type);
            default -> ItemStack.EMPTY;
        };
    }

    public static ItemStack createT1Armor(SlayerManager.SlayerType type, ArmorPiece piece) {
        return createT1Armor(type, piece.displayName);
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

    public static ItemStack createT2Armor(SlayerManager.SlayerType type, ArmorPiece piece) {
        return createT2Armor(type, piece.displayName);
    }





    //MARKER




    //MARKER
    /** Returns the MAX_HEALTH bonus in HP (2 per heart) for a T1 armor piece, or 0 if none. */
    private static double getT1ArmorHealth(SlayerManager.SlayerType type, String piece) {
        return switch (type) {
            case ZOMBIE -> switch (piece) {
                case "Helmet"     -> 4.0;  // +2 Hearts
                case "Chestplate" -> 4.0;  // +2 Hearts
                default           -> 0.0;
            };
            case SLIME -> switch (piece) {
                case "Helmet"     -> 4.0;  // +2 Hearts
                case "Chestplate" -> 8.0;  // +4 Hearts
                default           -> 0.0;
            };
            case PIGLIN -> switch (piece) {
                case "Chestplate" -> 4.0;  // +2 Hearts
                default           -> 0.0;
            };
            default -> 0.0;
        };
    }

    /** Returns the MAX_HEALTH bonus in HP (2 per heart) for a T2 armor piece, or 0 if none. */
    private static double getT2ArmorHealth(SlayerManager.SlayerType type, String piece) {
        return switch (type) {
            case ZOMBIE -> switch (piece) {
                case "Chestplate" -> 8.0;  // +4 Hearts
                default           -> 0.0;
            };
            case SLIME -> switch (piece) {
                case "Helmet"     -> 8.0;   // +4 Hearts
                case "Chestplate" -> 8.0;   // +4 Hearts
                default           -> 0.0;
            };
            case IRON_GOLEM -> switch (piece) {
                case "Leggings"   -> 8.0;   // +4 Hearts
                default           -> 0.0;
            };
            case PIGLIN -> switch (piece) {
                case "Chestplate" -> 8.0;  // +4 Hearts
                default           -> 0.0;
            };
            default -> 0.0;
        };
    }

    //MARKER
    private static void applyT1ArmorStats(ItemStack stack, SlayerManager.SlayerType type,
                                          String pieceName, int armorValue, int toughnessValue) {

        String armorName = getT1ArmorName(type);

        // Dye the leather
        int color = getArmorColor(type);
        int lighterColor = brightenColor(brightenColor(color)); // Double brighten for T1 = lighter than T2
        stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(lighterColor));

        // Set custom name
        stack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(armorName + " " + pieceName)
                        .formatted(type.color, Formatting.BOLD));

        // Set custom max damage (durability)
        stack.set(DataComponentTypes.MAX_DAMAGE, T1_ARMOR_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);

        // ADD ACTUAL ARMOR ATTRIBUTES including health
        AttributeModifierSlot slot = getSlotForPiece(pieceName);
        double healthValue = getT1ArmorHealth(type, pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "t1_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        slot)
                .add(EntityAttributes.ARMOR_TOUGHNESS,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "t1_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        slot);
        if (healthValue > 0) {
            attrBuilder.add(EntityAttributes.MAX_HEALTH,
                    new EntityAttributeModifier(
                            Identifier.of("politicalserver", "t1_" + pieceName.toLowerCase() + "_health"),
                            healthValue,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    ),
                    slot);
        }
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());

        // Compute per-piece boss defense from ArmorStats * pieceMult
        ArmorStats stats = getArmorStats(type, 1);
        double pieceMult = getPieceBossMultiplier(pieceName);
        int pieceBossDef  = Math.max(1, (int) Math.round(stats.bossReduction * pieceMult * 100));
        int pieceAllDef   = Math.max(1, (int) Math.round(stats.allBossReduction * pieceMult * 100));
        // T1 full set caps at 26% boss-specific (20% per-piece + 6% set bonus); all-boss is sum of all pieces
        int fullSetBoss   = 26;
        int fullSetAll    = (int) Math.round(stats.allBossReduction * 100);

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[MFLUX: Field-tested containment gear | Contractor Program]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+" + armorValue).formatted(Formatting.GREEN)));
        lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+" + toughnessValue).formatted(Formatting.GREEN)));
        if (healthValue > 0) {
            lore.add(Text.literal("❤ Health: ").formatted(Formatting.WHITE)
                    .append(Text.literal("+" + (int)(healthValue / 2) + " Hearts").formatted(Formatting.RED)));
        }
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ BOSS DEFENCE ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("🛡 This piece: ").formatted(Formatting.WHITE)
                .append(Text.literal("-" + pieceBossDef + "% " + type.displayName).formatted(Formatting.AQUA))
                .append(Text.literal(" / -" + pieceAllDef + "% all").formatted(Formatting.GRAY)));
        lore.add(Text.literal("⚔ Full set bonus: ").formatted(Formatting.DARK_GRAY)
                .append(Text.literal("-" + fullSetBoss + "% " + type.displayName).formatted(Formatting.GREEN))
                .append(Text.literal(" / -" + fullSetAll + "% all").formatted(Formatting.GREEN)));
        lore.add(Text.literal(""));
        // Per-piece unique buff line
        String pieceBuff = getT1PieceBuff(type, pieceName);
        if (pieceBuff != null) {
            lore.add(Text.literal("━━━ PIECE BUFF ━━━").formatted(Formatting.AQUA));
            lore.add(Text.literal("✦ " + pieceBuff).formatted(Formatting.AQUA));
            lore.add(Text.literal(""));
        }
        lore.add(Text.literal("⚠ Requires: " + type.displayName + " Bounty Lvl " + T1_ARMOR_LEVEL_REQ)
                .formatted(Formatting.RED));

        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    /** Returns the per-piece passive buff description for a T1 armor piece. */
    private static String getT1PieceBuff(SlayerManager.SlayerType type, String piece) {
        return switch (type) {
            case ZOMBIE -> switch (piece) {
                case "Helmet"     -> "Hunger Immunity + +2 Hearts Health";
                case "Chestplate" -> "+2 Hearts Health";
                case "Leggings"   -> "-10% Damage from Undead Mobs";
                case "Boots"      -> "+10% Knockback Resistance";
                default           -> null;
            };
            case SPIDER -> switch (piece) {
                case "Helmet"     -> "+5% Movement Speed";
                case "Chestplate" -> "Weakness Immunity";
                case "Leggings"   -> "Poison Immunity";
                case "Boots"      -> "+10% Movement Speed";
                default           -> null;
            };
            case SKELETON -> switch (piece) {
                case "Helmet"     -> "+10% Projectile Damage";
                case "Chestplate" -> "+10% Projectile Resistance";
                case "Leggings"   -> "+5% Movement Speed";
                case "Boots"      -> "+5% Movement Speed";
                default           -> null;
            };
            case SLIME -> switch (piece) {
                case "Helmet"     -> "+2 Hearts Health";
                case "Chestplate" -> "+4 Hearts Health";
                case "Leggings"   -> "Jump Boost I";
                case "Boots"      -> "No Fall Damage";
                default           -> null;
            };
            case ENDERMAN -> switch (piece) {
                case "Helmet"     -> "Night Vision";
                case "Chestplate" -> "+5% Movement Speed";
                case "Leggings"   -> "+5% Movement Speed";
                case "Boots"      -> "Safe Fall +3 Blocks";
                default           -> null;
            };
            case IRON_GOLEM -> switch (piece) {
                case "Helmet"     -> "Darkness Immunity";
                case "Chestplate" -> "+10% Knockback Resistance";
                case "Leggings"   -> "+10% Knockback Resistance";
                case "Boots"      -> "Vibration Sense: Hostile Mobs Glow within 8 blocks (refreshes every second)";
                default           -> null;
            };
            case PIGLIN -> switch (piece) {
                case "Helmet"     -> "Fire Resistance";
                case "Chestplate" -> "+2 Hearts Health";
                case "Leggings"   -> "+5% Movement Speed";
                case "Boots"      -> "Fire Resistance";
                default           -> null;
            };
        };
    }

    private static void applyT2ArmorStats(ItemStack stack, SlayerManager.SlayerType type,
                                          String pieceName, int armorValue, int toughnessValue, int healthValue) {

        String armorName = getT2ArmorName(type);

        // Dye the leather (brighter)
        int color = getArmorColor(type);
        int brighterColor = brightenColor(color);
        stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(brighterColor));

        // Set custom name with II
        stack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(armorName + " " + pieceName + " II")
                        .formatted(type.color, Formatting.BOLD));

        // Set custom max damage (durability)
        stack.set(DataComponentTypes.MAX_DAMAGE, T2_ARMOR_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);

        // ADD ACTUAL ARMOR ATTRIBUTES including health
        AttributeModifierSlot slot = getSlotForPiece(pieceName);
        double t2HealthValue = getT2ArmorHealth(type, pieceName);
        AttributeModifiersComponent.Builder t2AttrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "t2_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        slot)
                .add(EntityAttributes.ARMOR_TOUGHNESS,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "t2_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        slot);
        if (t2HealthValue > 0) {
            t2AttrBuilder.add(EntityAttributes.MAX_HEALTH,
                    new EntityAttributeModifier(
                            Identifier.of("politicalserver", "t2_" + pieceName.toLowerCase() + "_health"),
                            t2HealthValue,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    ),
                    slot);
        }
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, t2AttrBuilder.build());

        // Compute per-piece boss defense from ArmorStats * pieceMult
        ArmorStats stats = getArmorStats(type, 2);
        double pieceMult = getPieceBossMultiplier(pieceName);
        int pieceBossDef  = Math.max(1, (int) Math.round(stats.bossReduction * pieceMult * 100));
        int pieceAllDef   = Math.max(1, (int) Math.round(stats.allBossReduction * pieceMult * 100));
        // T2 full set caps at 38% boss-specific (28% + 10% set bonus); all-boss is sum of all pieces
        int fullSetBoss   = 38;
        int fullSetAll    = (int) Math.round(stats.allBossReduction * 100);

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[MFLUX: Upgraded containment gear | Research Division]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+" + armorValue).formatted(Formatting.AQUA)));
        lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+" + toughnessValue).formatted(Formatting.AQUA)));
        if (t2HealthValue > 0) {
            lore.add(Text.literal("❤ Health: ").formatted(Formatting.WHITE)
                    .append(Text.literal("+" + (int)(t2HealthValue / 2) + " Hearts").formatted(Formatting.RED)));
        }
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ BOSS DEFENCE ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("🛡 This piece: ").formatted(Formatting.WHITE)
                .append(Text.literal("-" + pieceBossDef + "% " + type.displayName).formatted(Formatting.AQUA))
                .append(Text.literal(" / -" + pieceAllDef + "% all").formatted(Formatting.GRAY)));
        lore.add(Text.literal("⚔ Full set bonus: ").formatted(Formatting.DARK_GRAY)
                .append(Text.literal("-" + fullSetBoss + "% " + type.displayName).formatted(Formatting.GREEN))
                .append(Text.literal(" / -" + fullSetAll + "% all").formatted(Formatting.GREEN)));
        lore.add(Text.literal("+15% XP from " + type.displayName + " bounties").formatted(Formatting.AQUA));
        lore.add(Text.literal(""));
        // Per-piece unique buff line
        String pieceBuff = getT2PieceBuff(type, pieceName);
        if (pieceBuff != null) {
            lore.add(Text.literal("━━━ PIECE BUFF ━━━").formatted(Formatting.LIGHT_PURPLE));
            lore.add(Text.literal("✦ " + pieceBuff).formatted(Formatting.LIGHT_PURPLE));
            lore.add(Text.literal(""));
        }
        lore.add(Text.literal("⚠ Requires: " + type.displayName + " Bounty Lvl " + T2_ARMOR_LEVEL_REQ)
                .formatted(Formatting.RED));

        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    /** Returns the per-piece passive buff description for a T2 armor piece. */
    private static String getT2PieceBuff(SlayerManager.SlayerType type, String piece) {
        return switch (type) {
            case ZOMBIE -> switch (piece) {
                case "Helmet"     -> "Hunger Immunity + Fire Resistance";
                case "Chestplate" -> "+4 Hearts Health";
                case "Leggings"   -> "-20% Damage from Undead Mobs";
                case "Boots"      -> "+20% Knockback Resistance + Speed I below 50% HP";
                default           -> null;
            };
            case SPIDER -> switch (piece) {
                case "Helmet"     -> "+10% Movement Speed";
                case "Chestplate" -> "Weakness Immunity + Poison Thorns (attackers get Poison I)";
                case "Leggings"   -> "Poison Immunity + Speed I";
                case "Boots"      -> "+15% Movement Speed + Safe Fall +3 Blocks";
                default           -> null;
            };
            case SKELETON -> switch (piece) {
                case "Helmet"     -> "+20% Projectile Damage + Arrows Apply Glowing";
                case "Chestplate" -> "+25% Projectile Resistance";
                case "Leggings"   -> "+10% Movement Speed";
                case "Boots"      -> "+10% Movement Speed";
                default           -> null;
            };
            case SLIME -> switch (piece) {
                case "Helmet"     -> "+4 Hearts Health + Saturation";
                case "Chestplate" -> "+4 Hearts Health + Absorption I every 30s";
                case "Leggings"   -> "Jump Boost II";
                case "Boots"      -> "No Fall Damage + Jump Boost II";
                default           -> null;
            };
            case ENDERMAN -> switch (piece) {
                case "Helmet"     -> "Night Vision (Permanent)";
                case "Chestplate" -> "+15% Movement Speed + Fall Damage Immunity";
                case "Leggings"   -> "+10% Movement Speed";
                case "Boots"      -> "No Fall Damage + +10% Movement Speed";
                default           -> null;
            };
            case IRON_GOLEM -> switch (piece) {
                case "Helmet"     -> "Darkness Immunity + Vibration Sense (12 blocks)";
                case "Chestplate" -> "+25% Knockback Resistance + Resistance I";
                case "Leggings"   -> "+15% Knockback Resistance + +4 Hearts Health";
                case "Boots"      -> "+10% Knockback Resistance";
                default           -> null;
            };
            case PIGLIN -> switch (piece) {
                case "Helmet"     -> "Fire Resistance";
                case "Chestplate" -> "+4 Hearts Health";
                case "Leggings"   -> "+10% Movement Speed";
                case "Boots"      -> "Fire Resistance + +10% Movement Speed";
                default           -> null;
            };
        };
    }

    // Helper method to get the correct slot
    private static AttributeModifierSlot getSlotForPiece(String pieceName) {
        return switch (pieceName) {
            case "Helmet" -> AttributeModifierSlot.HEAD;
            case "Chestplate" -> AttributeModifierSlot.CHEST;
            case "Leggings" -> AttributeModifierSlot.LEGS;
            case "Boots" -> AttributeModifierSlot.FEET;
            default -> AttributeModifierSlot.ANY;
        };
    }

    public static ItemStack createPiglinCore() {
        ItemStack core = new ItemStack(Items.GOLD_NUGGET);
        core.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✦ Piglin Core ✦").formatted(Formatting.GOLD, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("A powerful essence from the").formatted(Formatting.GRAY));
        lore.add(Text.literal("The Gilded Ravager.").formatted(Formatting.GOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Used to craft the Crown of Greed.").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[MFLUX: CRD-007 Piglin-class anomalous residue]").formatted(Formatting.DARK_GRAY));
        core.set(DataComponentTypes.LORE, new LoreComponent(lore));
        core.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(core, "piglin_core");
        return core;
    }

    public static ItemStack createPiglinFlesh() {
        ItemStack flesh = new ItemStack(Items.GOLD_NUGGET);
        flesh.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Piglin Flesh").formatted(Formatting.YELLOW, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("The singed flesh of a fallen Piglin.").formatted(Formatting.GRAY));
        lore.add(Text.literal("Still radiates heat.").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[MFLUX: Biological material, handle with care]").formatted(Formatting.DARK_GRAY));
        flesh.set(DataComponentTypes.LORE, new LoreComponent(lore));
        setCustomItemId(flesh, "piglin_flesh");
        return flesh;
    }


    public static ItemStack createAttributeToken(ArmourAttribute attr) {
        ItemStack token = new ItemStack(Items.PAPER);
        token.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ " + attr.displayName + " Attribute Token ✧")
                        .formatted(attr.color, Formatting.BOLD));
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ LEGENDARY ATTRIBUTE").formatted(attr.color, Formatting.BOLD));
        lore.add(Text.literal(""));
        
        // Add thematic lore based on attribute type
        switch (attr) {
            case BURNING -> {
                lore.add(Text.literal("A crystallized fragment of eternal flame").formatted(Formatting.GRAY));
                lore.add(Text.literal("forged in deepest Nether fires.").formatted(Formatting.DARK_GRAY));
                lore.add(Text.literal(""));
                lore.add(Text.literal("◆ ABILITIES:").formatted(attr.color, Formatting.BOLD));
                lore.add(Text.literal("• 3x damage while on fire").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• Immune to fire extinguishment").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• Fire damage immunity").formatted(Formatting.YELLOW));
                lore.add(Text.literal(""));
                lore.add(Text.literal("Harness power of perpetual combustion").formatted(Formatting.YELLOW));
                lore.add(Text.literal("and become immune to extinguishment.").formatted(Formatting.YELLOW));
                lore.add(Text.literal(""));
                lore.add(Text.literal("\"If you want to shine like the sun,").formatted(Formatting.GOLD, Formatting.ITALIC));
                lore.add(Text.literal("first you have to burn like it.\"").formatted(Formatting.GOLD, Formatting.ITALIC));
                break;
            }
            case SIGHTLESS -> {
                lore.add(Text.literal("An essence of pure darkness").formatted(Formatting.GRAY));
                lore.add(Text.literal("whispered by blind cave dwellers.").formatted(Formatting.DARK_GRAY));
                lore.add(Text.literal(""));
                lore.add(Text.literal("◆ ABILITIES:").formatted(attr.color, Formatting.BOLD));
                lore.add(Text.literal("• Permanent Night Vision").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• Blindness immunity").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• Reduced FOV with vignette").formatted(Formatting.YELLOW));
                lore.add(Text.literal(""));
                lore.add(Text.literal("See beyond mortal sight limitations").formatted(Formatting.YELLOW));
                lore.add(Text.literal("while immune to visual impairments.").formatted(Formatting.YELLOW));
                lore.add(Text.literal(""));
                lore.add(Text.literal("\"In darkness, true vision begins.\"").formatted(Formatting.DARK_PURPLE, Formatting.ITALIC));
                break;
            }
            case FRENZIED -> {
                lore.add(Text.literal("The raw rage of ancient warriors").formatted(Formatting.GRAY));
                lore.add(Text.literal("distilled into crystalline form.").formatted(Formatting.DARK_GRAY));
                lore.add(Text.literal(""));
                lore.add(Text.literal("◆ ABILITIES:").formatted(attr.color, Formatting.BOLD));
                lore.add(Text.literal("• +15% Attack Speed").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• Berserk combat buffs").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• Cannot eat food").formatted(Formatting.YELLOW));
                lore.add(Text.literal(""));
                lore.add(Text.literal("Unleash berserk fury in combat").formatted(Formatting.YELLOW));
                lore.add(Text.literal("with enhanced attack speed.").formatted(Formatting.YELLOW));
                lore.add(Text.literal(""));
                lore.add(Text.literal("\"Rage is the weapon of the fierce.\"").formatted(Formatting.RED, Formatting.ITALIC));
                break;
            }
            case GROUNDED -> {
                lore.add(Text.literal("Earth's steadfast protection").formatted(Formatting.GRAY));
                lore.add(Text.literal("solidified into defensive crystal.").formatted(Formatting.DARK_GRAY));
                lore.add(Text.literal(""));
                lore.add(Text.literal("◆ ABILITIES:").formatted(attr.color, Formatting.BOLD));
                lore.add(Text.literal("• Lightning immunity").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• Projectile deflection").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• Reduced jump height").formatted(Formatting.YELLOW));
                lore.add(Text.literal(""));
                lore.add(Text.literal("Stand firm against lightning strikes").formatted(Formatting.YELLOW));
                lore.add(Text.literal("and deflect incoming projectiles.").formatted(Formatting.YELLOW));
                lore.add(Text.literal(""));
                lore.add(Text.literal("\"As steady as the mountain itself.\"").formatted(Formatting.GREEN, Formatting.ITALIC));
                break;
            }
            case WEBBED -> {
                lore.add(Text.literal("Spider silk woven with magic").formatted(Formatting.GRAY));
                lore.add(Text.literal("enchanted by ancient arachnids.").formatted(Formatting.DARK_GRAY));
                lore.add(Text.literal(""));
                lore.add(Text.literal("◆ ABILITIES:").formatted(attr.color, Formatting.BOLD));
                lore.add(Text.literal("• Move through cobweb barriers").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• Gain spider neutrality").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• Slowed on grass/leaves").formatted(Formatting.YELLOW));
                lore.add(Text.literal(""));
                lore.add(Text.literal("Move freely through cobweb barriers").formatted(Formatting.YELLOW));
                lore.add(Text.literal("and gain spider neutrality.").formatted(Formatting.YELLOW));
                lore.add(Text.literal(""));
                lore.add(Text.literal("\"The web catches all but the spider.\"").formatted(Formatting.DARK_GREEN, Formatting.ITALIC));
                break;
            }
            case FROST -> {
                lore.add(Text.literal("Frozen essence of ice giants").formatted(Formatting.GRAY));
                lore.add(Text.literal("preserved in eternal winter.").formatted(Formatting.DARK_GRAY));
                lore.add(Text.literal(""));
                lore.add(Text.literal("◆ ABILITIES:").formatted(attr.color, Formatting.BOLD));
                lore.add(Text.literal("• Walk on water as frozen ground").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• Freeze immunity").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• 150% lava damage").formatted(Formatting.YELLOW));
                lore.add(Text.literal(""));
                lore.add(Text.literal("Walk on water as frozen ground").formatted(Formatting.YELLOW));
                lore.add(Text.literal("and resist the cold embrace.").formatted(Formatting.YELLOW));
                lore.add(Text.literal(""));
                lore.add(Text.literal("\"Winter's fury, summer's bane.\"").formatted(Formatting.AQUA, Formatting.ITALIC));
                break;
            }
            case PHANTOMSTEP -> {
                lore.add(Text.literal("Ethereal essence of phantoms").formatted(Formatting.GRAY));
                lore.add(Text.literal("captured from silent hunters.").formatted(Formatting.DARK_GRAY));
                lore.add(Text.literal(""));
                lore.add(Text.literal("◆ ABILITIES:").formatted(attr.color, Formatting.BOLD));
                lore.add(Text.literal("• No fall damage").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• Stealth step sounds").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• Phantoms always target you").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• Cannot sleep").formatted(Formatting.YELLOW));
                lore.add(Text.literal(""));
                lore.add(Text.literal("Walk with spectral silence").formatted(Formatting.YELLOW));
                lore.add(Text.literal("and laugh at gravity's pull.").formatted(Formatting.YELLOW));
                lore.add(Text.literal(""));
                lore.add(Text.literal("\"Between steps, there is only silence.\"").formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC));
                break;
            }
            case CURSED -> {
                lore.add(Text.literal("Dark power of forbidden knowledge").formatted(Formatting.GRAY));
                lore.add(Text.literal("bound in blood magic parchment.").formatted(Formatting.DARK_GRAY));
                lore.add(Text.literal(""));
                lore.add(Text.literal("◆ ABILITIES:").formatted(attr.color, Formatting.BOLD));
                lore.add(Text.literal("• +6 Max Hearts").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• 20% life steal on damage").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• XP reduction").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• Villager hostility").formatted(Formatting.YELLOW));
                lore.add(Text.literal(""));
                lore.add(Text.literal("Steal life force with each kill").formatted(Formatting.YELLOW));
                lore.add(Text.literal("and embrace vampiric power.").formatted(Formatting.YELLOW));
                lore.add(Text.literal(""));
                lore.add(Text.literal("\"Life given, life taken away.\"").formatted(Formatting.DARK_RED, Formatting.ITALIC));
                break;
            }
            case OVERGROWN -> {
                lore.add(Text.literal("Nature's healing crystallized").formatted(Formatting.GRAY));
                lore.add(Text.literal("blessed by ancient forest spirits.").formatted(Formatting.DARK_GRAY));
                lore.add(Text.literal(""));
                lore.add(Text.literal("◆ ABILITIES:").formatted(attr.color, Formatting.BOLD));
                lore.add(Text.literal("• Regenerate on natural ground").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• Channel earth's vitality").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• Reduced mining speed").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• Nether/End decay").formatted(Formatting.YELLOW));
                lore.add(Text.literal(""));
                lore.add(Text.literal("Regenerate on natural ground").formatted(Formatting.YELLOW));
                lore.add(Text.literal("and channel earth's vitality.").formatted(Formatting.YELLOW));
                lore.add(Text.literal(""));
                lore.add(Text.literal("\"From earth we come, to earth we return.\"").formatted(Formatting.GREEN, Formatting.ITALIC));
                break;
            }
            case VOLATILE -> {
                lore.add(Text.literal("Explosive power of chaos").formatted(Formatting.GRAY));
                lore.add(Text.literal("contained in unstable crystal.").formatted(Formatting.DARK_GRAY));
                lore.add(Text.literal(""));
                lore.add(Text.literal("◆ ABILITIES:").formatted(attr.color, Formatting.BOLD));
                lore.add(Text.literal("• Explode when near death").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• Blast immunity").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• Blowback with fire items").formatted(Formatting.YELLOW));
                lore.add(Text.literal(""));
                lore.add(Text.literal("Detonate when near death").formatted(Formatting.YELLOW));
                lore.add(Text.literal("and become blast immune.").formatted(Formatting.YELLOW));
                lore.add(Text.literal(""));
                lore.add(Text.literal("\"In destruction, there is creation.\"").formatted(Formatting.RED, Formatting.ITALIC));
                break;
            }
        }
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ USAGE ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("Apply to: ").formatted(Formatting.GRAY)
                .append(Text.literal(attr.slot == null ? "Any Armor Piece" : attr.slot.getName().toUpperCase())
                        .formatted(Formatting.AQUA)));
        lore.add(Text.literal("XP Cost: ").formatted(Formatting.RED)
                .append(Text.literal(attr.xpCost + " levels").formatted(Formatting.BOLD, Formatting.RED)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚔ Combine with armor in anvil").formatted(Formatting.YELLOW));
        
        token.set(DataComponentTypes.LORE, new LoreComponent(lore)); // Moved this line up
        token.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        NbtCompound nbt = new NbtCompound();
        nbt.putString(ARMOR_ATTRIBUTE_TOKEN_KEY, attr.id);
        token.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        
        setCustomItemId(token, "attribute_token_" + attr.id);
        token.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return token;
    }

    public static ItemStack createAttributeCore(ArmourAttribute attr) {
        ItemStack core = new ItemStack(Items.NETHER_STAR);
        core.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(attr.displayName + " Core").formatted(attr.color, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[MFLUX: Attribute essence containment | Research Division]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("A crystallized essence of").formatted(attr.color));
        lore.add(Text.literal(attr.displayName.toLowerCase() + " power.").formatted(attr.color));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Used to craft " + attr.displayName + " Attribute Token.").formatted(Formatting.GRAY));
        core.set(DataComponentTypes.LORE, new LoreComponent(lore));
        core.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(core, "attribute_core_" + attr.id);
        return core;
    }

    public static ItemStack createVoidwalkerCrown() {
        ItemStack stack = new ItemStack(Items.NETHERITE_HELMET);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("👑 Voidwalker's Crown").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        setCustomItemId(stack, "voidwalker_crown");

        // CUSTOM ARMOR ATTRIBUTES
        AttributeModifiersComponent attributes = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "voidwalker_armor"),
                                8.0,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.HEAD)
                .add(EntityAttributes.ARMOR_TOUGHNESS,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "voidwalker_toughness"),
                                4.0,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.HEAD)
                .add(EntityAttributes.MAX_HEALTH,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "voidwalker_health"),
                                20.0,  // +10 hearts
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.HEAD)
                .add(EntityAttributes.ATTACK_DAMAGE,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "voidwalker_damage"),
                                3.0,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.HEAD)
                .build();
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attributes);

        // Durability - 8x netherite
        stack.set(DataComponentTypes.MAX_DAMAGE, 3256);
        stack.set(DataComponentTypes.DAMAGE, 0);

        // ENHANCED LORE
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[MFLUX: CRD-007 | Void-phase containment circlet]"));
        lore.add(Text.literal("§8[STATUS: Field testing terminated after 7 disappearances]"));
        lore.add(Text.literal("§8[NOTE: \"It doesn't teleport you. It makes the void notice you.\"]"));
        lore.add(Text.literal("§7\"Recovered from the ruins of Research Station Epsilon-3.\"").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        lore.add(Text.literal("§7\"Subjects report hearing whispers from the void\"").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        lore.add(Text.literal("§7\"and experiencing moments of non-existence.\"").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        lore.add(Text.literal("§7\"The circlet appears to bend spacetime around the wearer\"").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        lore.add(Text.literal("§7\"but the void demands its price.\"").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("🛡 Armor: +8").formatted(Formatting.BLUE));
        lore.add(Text.literal("🛡 Toughness: +4").formatted(Formatting.BLUE));
        lore.add(Text.literal("❤ Health: +10 Hearts").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Damage: +3").formatted(Formatting.RED));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("🌀 Void Step: Random teleport when hit (10% chance)").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("👁 Void Sight: See through walls in darkness").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("🌙 Night Vision: Permanent").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚡ Phase Dodge: 15% chance to dodge attacks").formatted(Formatting.AQUA));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ VOID POWERS ━━━").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("🌌 In darkness: ").formatted(Formatting.DARK_GRAY)
                .append(Text.literal("+50% damage").formatted(Formatting.LIGHT_PURPLE)));
        lore.add(Text.literal("🌌 In void dimension: ").formatted(Formatting.DARK_GRAY)
                .append(Text.literal("+100% damage").formatted(Formatting.LIGHT_PURPLE)));
        lore.add(Text.literal("🌌 Endermen: ").formatted(Formatting.DARK_GRAY)
                .append(Text.literal("Peaceful").formatted(Formatting.GREEN)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ LEGENDARY ABILITY ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("🌀 Void Walker: ").formatted(Formatting.LIGHT_PURPLE)
                .append(Text.literal("Right-click to teleport 20 blocks").formatted(Formatting.WHITE)));
        lore.add(Text.literal("   Cooldown: 30 seconds").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§7\"The void remembers. The void waits. The void claims.\"").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        
        return stack;
    }

    public static ItemStack createEnderPhaseHelmet() {
        ItemStack stack = new ItemStack(Items.NETHERITE_HELMET);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Ender Phase Helmet").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        // ... (rest of the code remains the same)
        setCustomItemId(stack, "ender_phase_helmet");
        return stack;
    }

    public static ItemStack createVenomousDagger() {
        ItemStack stack = new ItemStack(Items.IRON_SWORD);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Venomous Dagger").formatted(Formatting.DARK_GREEN, Formatting.BOLD));
        setCustomItemId(stack, "venomous_dagger");
        return stack;
    }

    public static ItemStack createCrownOfGreed() {
        ItemStack crown = new ItemStack(Items.LEATHER_HELMET);
        crown.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0xD4AF00));
        crown.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("👑 Crown of Greed").formatted(Formatting.GOLD, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[MFLUX Research Division | Specimen CRD-007-G]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("❤ Max Health: 4 HP (2 hearts)").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armour: 0 (all defence removed)").formatted(Formatting.GRAY));
        lore.add(Text.literal("⚔ Damage: ×(digits of your coins)").formatted(Formatting.YELLOW));
        lore.add(Text.literal("  (e.g. 999 coins = ×3 damage)").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("✓ Allows any items (not gold-only)").formatted(Formatting.GREEN));
        lore.add(Text.literal("✗ All bounty buffs/defence disabled").formatted(Formatting.RED));
        lore.add(Text.literal("✗ All armour defence cleared").formatted(Formatting.RED));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ WARNING ━━━").formatted(Formatting.DARK_RED, Formatting.BOLD));
        lore.add(Text.literal("💀 ON DEATH: Lose ALL coins!").formatted(Formatting.DARK_RED, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("\"Wealth is power — and power devours.\"").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        crown.set(DataComponentTypes.LORE, new LoreComponent(lore));
        crown.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        crown.set(DataComponentTypes.MAX_DAMAGE, 5000); // Buffed durability
        setCustomItemId(crown, "crown_of_greed");
        return crown;
    }

    public static ItemStack createCrownOfMidas() {
        ItemStack crown = new ItemStack(Items.GOLDEN_HELMET);
        crown.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0xFFD700));
        crown.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("👑 Crown of Midas").formatted(Formatting.YELLOW, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[MFLUX Research Division | Specimen CRD-008-M]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("❤ Max Health: 10 HP (5 hearts)").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armour: 0 (all defence removed)").formatted(Formatting.GRAY));
        lore.add(Text.literal("⚔ Damage: ×(digits of your coins × 3)").formatted(Formatting.YELLOW));
        lore.add(Text.literal("  (e.g. 999 coins = ×9 damage)").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("✓ Only allows gold items + piglin armor").formatted(Formatting.GREEN));
        lore.add(Text.literal("✗ All bounty buffs/defence disabled").formatted(Formatting.RED));
        lore.add(Text.literal("✗ All armour defence cleared").formatted(Formatting.RED));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ WARNING ━━━").formatted(Formatting.DARK_RED, Formatting.BOLD));
        lore.add(Text.literal("💀 ON DEATH: Lose ALL coins!").formatted(Formatting.DARK_RED, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("\"The golden touch comes at a price.\"").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        crown.set(DataComponentTypes.LORE, new LoreComponent(lore));
        crown.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        crown.set(DataComponentTypes.MAX_DAMAGE, 5000); // Buffed durability
        setCustomItemId(crown, "crown_of_midas");
        return crown;
    }

    public static ItemStack createMidasSword() {
        ItemStack sword = new ItemStack(Items.GOLDEN_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⚔ Midas's Sword").formatted(Formatting.YELLOW, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[MFLUX Research Division | Specimen CRD-009-M]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Base Damage: 4 (Golden Sword)").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Kill Bonus: +1 coin per mob kill").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ DAMAGE SCALING ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("⚔ Damage = base × (kill_digits × 2)").formatted(Formatting.YELLOW));
        lore.add(Text.literal("  e.g. 999 kills = 6 digits → ×12 damage").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ CROWN SYNERGY ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("👑 Wearing Crown of Greed/Midas:").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("   → DOUBLES the damage multiplier!").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("✓ 5000 durability").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Live kill counter in item tooltip").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ WARNING ━━━").formatted(Formatting.DARK_RED, Formatting.BOLD));
        lore.add(Text.literal("💀 ON DEATH: Lose ALL coins!").formatted(Formatting.DARK_RED, Formatting.BOLD));
        lore.add(Text.literal("(Only from crowns and this sword)").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("\"Fortune favors the bold, but").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        lore.add(Text.literal("death claims all.\"").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 5000);
        setCustomItemId(sword, "midas_sword");
        return sword;
    }

    public static boolean isMidasSword(ItemStack stack) {
        return "midas_sword".equals(getCustomItemId(stack));
    }

    public static int getMidasSwordKills(ItemStack sword) {
        if (!isMidasSword(sword)) return 0;
        
        NbtComponent nbt = sword.get(DataComponentTypes.CUSTOM_DATA);
        if (nbt != null) {
            return nbt.copyNbt().getInt("midas_kills").orElse(0);
        }
        return 0;
    }

    public static void setMidasSwordKills(ItemStack sword, int kills) {
        if (!isMidasSword(sword)) return;
        
        NbtComponent existing = sword.get(DataComponentTypes.CUSTOM_DATA);
        NbtCompound nbt = (existing != null) ? existing.copyNbt() : new NbtCompound();
        nbt.putInt("midas_kills", kills);
        sword.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

    public static boolean isEnderPhaseHelmet(ItemStack stack) {
        return hasCustomItemId(stack, "ender_phase_helmet");
    }

    // ═══════════════════════════════════════════════════════════════
    // SUPER COMPACTED ORES - Ultimate compression
    // ═══════════════════════════════════════════════════════════════

    public static ItemStack createSuperCompactedGold() {
        ItemStack compacted = new ItemStack(Items.GOLD_BLOCK);
        compacted.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⭐ Super Compacted Gold").formatted(Formatting.GOLD, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ SUPER COMPACTED BLOCK ◆").formatted(Formatting.GOLD, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("The ultimate gold storage block").formatted(Formatting.GRAY));
        lore.add(Text.literal("Crafted from 9 compacted gold").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Value: ").formatted(Formatting.GRAY)
                .append(Text.literal("576 Gold Ingots").formatted(Formatting.GOLD)));
        compacted.set(DataComponentTypes.LORE, new LoreComponent(lore));
        compacted.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(compacted, "super_compacted_gold");
        return compacted;
    }

    public static ItemStack createSuperCompactedIron() {
        ItemStack compacted = new ItemStack(Items.IRON_BLOCK);
        compacted.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⭐ Super Compacted Iron").formatted(Formatting.GRAY, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ SUPER COMPACTED BLOCK ◆").formatted(Formatting.GRAY, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("The ultimate iron storage block").formatted(Formatting.GRAY));
        lore.add(Text.literal("Crafted from 9 compacted iron").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Value: ").formatted(Formatting.GRAY)
                .append(Text.literal("576 Iron Ingots").formatted(Formatting.WHITE)));
        compacted.set(DataComponentTypes.LORE, new LoreComponent(lore));
        compacted.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(compacted, "super_compacted_iron");
        return compacted;
    }

    public static ItemStack createSuperCompactedDiamond() {
        ItemStack compacted = new ItemStack(Items.DIAMOND_BLOCK);
        compacted.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⭐ Super Compacted Diamond").formatted(Formatting.AQUA, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ SUPER COMPACTED BLOCK ◆").formatted(Formatting.AQUA, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("The ultimate diamond storage block").formatted(Formatting.GRAY));
        lore.add(Text.literal("Crafted from 9 compacted diamond").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Value: ").formatted(Formatting.GRAY)
                .append(Text.literal("576 Diamonds").formatted(Formatting.AQUA)));
        compacted.set(DataComponentTypes.LORE, new LoreComponent(lore));
        compacted.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(compacted, "super_compacted_diamond");
        return compacted;
    }

    public static ItemStack createSuperCompactedEmerald() {
        ItemStack compacted = new ItemStack(Items.EMERALD_BLOCK);
        compacted.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⭐ Super Compacted Emerald").formatted(Formatting.GREEN, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ SUPER COMPACTED BLOCK ◆").formatted(Formatting.GREEN, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("The ultimate emerald storage block").formatted(Formatting.GRAY));
        lore.add(Text.literal("Crafted from 9 compacted emerald").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Value: ").formatted(Formatting.GRAY)
                .append(Text.literal("576 Emeralds").formatted(Formatting.GREEN)));
        compacted.set(DataComponentTypes.LORE, new LoreComponent(lore));
        compacted.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(compacted, "super_compacted_emerald");
        return compacted;
    }

    public static ItemStack createSuperCompactedNetherite() {
        ItemStack compacted = new ItemStack(Items.NETHERITE_BLOCK);
        compacted.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⭐ Super Compacted Netherite").formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ SUPER COMPACTED BLOCK ◆").formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("The ultimate netherite storage block").formatted(Formatting.GRAY));
        lore.add(Text.literal("Crafted from 9 compacted netherite").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Value: ").formatted(Formatting.GRAY)
                .append(Text.literal("576 Netherite Ingots").formatted(Formatting.DARK_GRAY)));
        compacted.set(DataComponentTypes.LORE, new LoreComponent(lore));
        compacted.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(compacted, "super_compacted_netherite");
        return compacted;
    }

    public static ItemStack createCompactedIron() {
        ItemStack compacted = new ItemStack(Items.RAW_IRON);
        compacted.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⬛ Compacted Iron").formatted(Formatting.GRAY, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ COMPACTED ORE ◆").formatted(Formatting.GRAY, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Highly concentrated raw iron").formatted(Formatting.GRAY));
        lore.add(Text.literal("Crafted from 64 raw iron").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Value: ").formatted(Formatting.GRAY)
                .append(Text.literal("64 Raw Iron").formatted(Formatting.WHITE)));
        compacted.set(DataComponentTypes.LORE, new LoreComponent(lore));
        compacted.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(compacted, "compacted_iron");
        return compacted;
    }

    public static ItemStack createCompactedGold() {
        ItemStack compacted = new ItemStack(Items.RAW_GOLD);
        compacted.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⬛ Compacted Gold").formatted(Formatting.YELLOW, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ COMPACTED ORE ◆").formatted(Formatting.YELLOW, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Highly concentrated raw gold").formatted(Formatting.GRAY));
        lore.add(Text.literal("Crafted from 64 raw gold").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Value: ").formatted(Formatting.GRAY)
                .append(Text.literal("64 Raw Gold").formatted(Formatting.YELLOW)));
        compacted.set(DataComponentTypes.LORE, new LoreComponent(lore));
        compacted.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(compacted, "compacted_gold");
        return compacted;
    }

    public static ItemStack createCompactedNetherite() {
        ItemStack compacted = new ItemStack(Items.NETHERITE_INGOT);
        compacted.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⬛ Compacted Netherite").formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ COMPACTED MATERIAL ◆").formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("A dense ingot of compressed netherite").formatted(Formatting.GRAY));
        lore.add(Text.literal("Crafted from 64 netherite ingots").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Value: ").formatted(Formatting.GRAY)
                .append(Text.literal("64 Netherite Ingots").formatted(Formatting.DARK_GRAY)));
        compacted.set(DataComponentTypes.LORE, new LoreComponent(lore));
        compacted.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(compacted, "compacted_netherite");
        return compacted;
    }

    public static ItemStack createCompactedDiamond() {
        ItemStack compacted = new ItemStack(Items.DIAMOND);
        compacted.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⬛ Compacted Diamond").formatted(Formatting.AQUA, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ COMPACTED ORE ◆").formatted(Formatting.AQUA, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Highly concentrated diamond").formatted(Formatting.GRAY));
        lore.add(Text.literal("Crafted from 64 diamonds").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Value: ").formatted(Formatting.GRAY)
                .append(Text.literal("64 Diamonds").formatted(Formatting.AQUA)));
        compacted.set(DataComponentTypes.LORE, new LoreComponent(lore));
        compacted.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(compacted, "compacted_diamond");
        return compacted;
    }

    public static ItemStack createCompactedEmerald() {
        ItemStack compacted = new ItemStack(Items.EMERALD);
        compacted.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⬛ Compacted Emerald").formatted(Formatting.GREEN, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ COMPACTED ORE ◆").formatted(Formatting.GREEN, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Highly concentrated emerald").formatted(Formatting.GRAY));
        lore.add(Text.literal("Crafted from 64 emeralds").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Value: ").formatted(Formatting.GRAY)
                .append(Text.literal("64 Emeralds").formatted(Formatting.GREEN)));
        compacted.set(DataComponentTypes.LORE, new LoreComponent(lore));
        compacted.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(compacted, "compacted_emerald");
        return compacted;
    }

    public static ItemStack createCompactedCopper() {
        ItemStack compacted = new ItemStack(Items.RAW_COPPER);
        compacted.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⬛ Compacted Copper").formatted(Formatting.RED, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ COMPACTED ORE ◆").formatted(Formatting.RED, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Highly concentrated raw copper").formatted(Formatting.GRAY));
        lore.add(Text.literal("Crafted from 64 raw copper").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Value: ").formatted(Formatting.GRAY)
                .append(Text.literal("64 Raw Copper").formatted(Formatting.RED)));
        compacted.set(DataComponentTypes.LORE, new LoreComponent(lore));
        compacted.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(compacted, "compacted_copper");
        return compacted;
    }

    public static ItemStack createCompactedCoal() {
        ItemStack compacted = new ItemStack(Items.COAL);
        compacted.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⬛ Compacted Coal").formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ COMPACTED MATERIAL ◆").formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("A dense piece of compressed coal").formatted(Formatting.GRAY));
        lore.add(Text.literal("Crafted from 64 coal").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Value: ").formatted(Formatting.GRAY)
                .append(Text.literal("64 Coal").formatted(Formatting.DARK_GRAY)));
        compacted.set(DataComponentTypes.LORE, new LoreComponent(lore));
        compacted.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(compacted, "compacted_coal");
        return compacted;
    }

    public static ItemStack createCompactedLapis() {
        ItemStack compacted = new ItemStack(Items.LAPIS_LAZULI);
        compacted.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⬛ Compacted Lapis Lazuli").formatted(Formatting.BLUE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ COMPACTED MATERIAL ◆").formatted(Formatting.BLUE, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("A dense gem of compressed lapis lazuli").formatted(Formatting.GRAY));
        lore.add(Text.literal("Crafted from 64 lapis lazuli").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Value: ").formatted(Formatting.GRAY)
                .append(Text.literal("64 Lapis Lazuli").formatted(Formatting.BLUE)));
        compacted.set(DataComponentTypes.LORE, new LoreComponent(lore));
        compacted.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(compacted, "compacted_lapis");
        return compacted;
    }

    public static ItemStack createCompactedRedstone() {
        ItemStack compacted = new ItemStack(Items.REDSTONE);
        compacted.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⬛ Compacted Redstone").formatted(Formatting.RED, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ COMPACTED MATERIAL ◆").formatted(Formatting.RED, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("A dense piece of compressed redstone").formatted(Formatting.GRAY));
        lore.add(Text.literal("Crafted from 64 redstone").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Value: ").formatted(Formatting.GRAY)
                .append(Text.literal("64 Redstone").formatted(Formatting.RED)));
        compacted.set(DataComponentTypes.LORE, new LoreComponent(lore));
        compacted.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(compacted, "compacted_redstone");
        return compacted;
    }

    public static ItemStack createCompactedQuartz() {
        ItemStack compacted = new ItemStack(Items.QUARTZ);
        compacted.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⬛ Compacted Quartz").formatted(Formatting.WHITE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ COMPACTED MATERIAL ◆").formatted(Formatting.WHITE, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("A dense gem of compressed quartz").formatted(Formatting.GRAY));
        lore.add(Text.literal("Crafted from 64 quartz").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Value: ").formatted(Formatting.GRAY)
                .append(Text.literal("64 Quartz").formatted(Formatting.WHITE)));
        compacted.set(DataComponentTypes.LORE, new LoreComponent(lore));
        compacted.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(compacted, "compacted_quartz");
        return compacted;
    }

    public static ItemStack createPureGoldHelmet() {
        ItemStack helmet = new ItemStack(Items.GOLDEN_HELMET);
        helmet.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("☀ Pure Gold Helmet").formatted(Formatting.YELLOW, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[MFLUX Research Division | Gold Armor Tier 1]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("❤ Max Health: 2 HP (1 heart)").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armour: 2").formatted(Formatting.GRAY));
        lore.add(Text.literal("⚔ Toughness: 0").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("✓ Basic gold protection").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Golden appearance").formatted(Formatting.GREEN));
        lore.add(Text.literal("✗ No special abilities").formatted(Formatting.RED));
        lore.add(Text.literal(""));
        lore.add(Text.literal("\"The beginning of golden excellence\"").formatted(Formatting.GRAY, Formatting.ITALIC));
        helmet.set(DataComponentTypes.LORE, new LoreComponent(lore));
        helmet.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(helmet, "pure_gold_helmet");
        return helmet;
    }

    public static ItemStack createPureGoldChestplate() {
        ItemStack chestplate = new ItemStack(Items.GOLDEN_CHESTPLATE);
        chestplate.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("☀ Pure Gold Chestplate").formatted(Formatting.YELLOW, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[MFLUX Research Division | Gold Armor Tier 1]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("❤ Max Health: 4 HP (2 hearts)").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armour: 6").formatted(Formatting.GRAY));
        lore.add(Text.literal("⚔ Toughness: 0").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("✓ Basic gold protection").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Golden appearance").formatted(Formatting.GREEN));
        lore.add(Text.literal("✗ No special abilities").formatted(Formatting.RED));
        lore.add(Text.literal(""));
        lore.add(Text.literal("\"The beginning of golden excellence\"").formatted(Formatting.GRAY, Formatting.ITALIC));
        chestplate.set(DataComponentTypes.LORE, new LoreComponent(lore));
        chestplate.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(chestplate, "pure_gold_chestplate");
        return chestplate;
    }

    public static ItemStack createPureGoldLeggings() {
        ItemStack leggings = new ItemStack(Items.GOLDEN_LEGGINGS);
        leggings.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("☀ Pure Gold Leggings").formatted(Formatting.YELLOW, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[MFLUX Research Division | Gold Armor Tier 1]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("❤ Max Health: 2 HP (1 heart)").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armour: 5").formatted(Formatting.GRAY));
        lore.add(Text.literal("⚔ Toughness: 0").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("✓ Basic gold protection").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Golden appearance").formatted(Formatting.GREEN));
        lore.add(Text.literal("✗ No special abilities").formatted(Formatting.RED));
        lore.add(Text.literal(""));
        lore.add(Text.literal("\"The beginning of golden excellence\"").formatted(Formatting.GRAY, Formatting.ITALIC));
        leggings.set(DataComponentTypes.LORE, new LoreComponent(lore));
        leggings.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(leggings, "pure_gold_leggings");
        return leggings;
    }

    public static ItemStack createPureGoldBoots() {
        ItemStack boots = new ItemStack(Items.GOLDEN_BOOTS);
        boots.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("☀ Pure Gold Boots").formatted(Formatting.YELLOW, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[MFLUX Research Division | Gold Armor Tier 1]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("❤ Max Health: 2 HP (1 heart)").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armour: 2").formatted(Formatting.GRAY));
        lore.add(Text.literal("⚔ Toughness: 0").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("✓ Basic gold protection").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Golden appearance").formatted(Formatting.GREEN));
        lore.add(Text.literal("✗ No special abilities").formatted(Formatting.RED));
        lore.add(Text.literal(""));
        lore.add(Text.literal("\"The beginning of golden excellence\"").formatted(Formatting.GRAY, Formatting.ITALIC));
        boots.set(DataComponentTypes.LORE, new LoreComponent(lore));
        boots.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(boots, "pure_gold_boots");
        return boots;
    }

    public static ItemStack createPolishedGoldHelmet() {
        ItemStack helmet = new ItemStack(Items.GOLDEN_HELMET);
        helmet.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✨ Polished Gold Helmet").formatted(Formatting.YELLOW, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[MFLUX Research Division | Gold Armor Tier 2]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("❤ Max Health: 4 HP (2 hearts)").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armour: 3").formatted(Formatting.GRAY));
        lore.add(Text.literal("⚔ Toughness: 1").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("✓ Enhanced gold protection").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Polished golden shine").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ +10% movement speed").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("\"Refined to perfection\"").formatted(Formatting.GRAY, Formatting.ITALIC));
        helmet.set(DataComponentTypes.LORE, new LoreComponent(lore));
        helmet.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(helmet, "polished_gold_helmet");
        return helmet;
    }

    public static ItemStack createPolishedGoldChestplate() {
        ItemStack chestplate = new ItemStack(Items.GOLDEN_CHESTPLATE);
        chestplate.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✨ Polished Gold Chestplate").formatted(Formatting.YELLOW, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[MFLUX Research Division | Gold Armor Tier 2]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("❤ Max Health: 6 HP (3 hearts)").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armour: 7").formatted(Formatting.GRAY));
        lore.add(Text.literal("⚔ Toughness: 1").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("✓ Enhanced gold protection").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Polished golden shine").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ +10% movement speed").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("\"Refined to perfection\"").formatted(Formatting.GRAY, Formatting.ITALIC));
        chestplate.set(DataComponentTypes.LORE, new LoreComponent(lore));
        chestplate.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(chestplate, "polished_gold_chestplate");
        return chestplate;
    }

    public static ItemStack createPolishedGoldLeggings() {
        ItemStack leggings = new ItemStack(Items.GOLDEN_LEGGINGS);
        leggings.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✨ Polished Gold Leggings").formatted(Formatting.YELLOW, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[MFLUX Research Division | Gold Armor Tier 2]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("❤ Max Health: 4 HP (2 hearts)").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armour: 6").formatted(Formatting.GRAY));
        lore.add(Text.literal("⚔ Toughness: 1").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("✓ Enhanced gold protection").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Polished golden shine").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ +10% movement speed").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("\"Refined to perfection\"").formatted(Formatting.GRAY, Formatting.ITALIC));
        leggings.set(DataComponentTypes.LORE, new LoreComponent(lore));
        leggings.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(leggings, "polished_gold_leggings");
        return leggings;
    }

    public static ItemStack createPolishedGoldBoots() {
        ItemStack boots = new ItemStack(Items.GOLDEN_BOOTS);
        boots.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✨ Polished Gold Boots").formatted(Formatting.YELLOW, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[MFLUX Research Division | Gold Armor Tier 2]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("❤ Max Health: 4 HP (2 hearts)").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armour: 3").formatted(Formatting.GRAY));
        lore.add(Text.literal("⚔ Toughness: 1").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("✓ Enhanced gold protection").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Polished golden shine").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ +10% movement speed").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("\"Refined to perfection\"").formatted(Formatting.GRAY, Formatting.ITALIC));
        boots.set(DataComponentTypes.LORE, new LoreComponent(lore));
        boots.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(boots, "polished_gold_boots");
        return boots;
    }

    public static ItemStack createShinyGoldHelmet() {
        ItemStack helmet = new ItemStack(Items.GOLDEN_HELMET);
        helmet.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⭐ Shiny Gold Helmet").formatted(Formatting.YELLOW, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[MFLUX Research Division | Gold Armor Tier 3]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("❤ Max Health: 6 HP (3 hearts)").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armour: 4").formatted(Formatting.GRAY));
        lore.add(Text.literal("⚔ Toughness: 2").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("✓ Superior gold protection").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Brilliant golden glow").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ +20% movement speed").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Night vision when worn").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("\"Shines like the sun\"").formatted(Formatting.GRAY, Formatting.ITALIC));
        helmet.set(DataComponentTypes.LORE, new LoreComponent(lore));
        helmet.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(helmet, "shiny_gold_helmet");
        return helmet;
    }

    public static ItemStack createShinyGoldChestplate() {
        ItemStack chestplate = new ItemStack(Items.GOLDEN_CHESTPLATE);
        chestplate.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⭐ Shiny Gold Chestplate").formatted(Formatting.YELLOW, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[MFLUX Research Division | Gold Armor Tier 3]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("❤ Max Health: 8 HP (4 hearts)").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armour: 8").formatted(Formatting.GRAY));
        lore.add(Text.literal("⚔ Toughness: 2").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("✓ Superior gold protection").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Brilliant golden glow").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ +20% movement speed").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Night vision when worn").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("\"Shines like the sun\"").formatted(Formatting.GRAY, Formatting.ITALIC));
        chestplate.set(DataComponentTypes.LORE, new LoreComponent(lore));
        chestplate.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(chestplate, "shiny_gold_chestplate");
        return chestplate;
    }

    public static ItemStack createShinyGoldLeggings() {
        ItemStack leggings = new ItemStack(Items.GOLDEN_LEGGINGS);
        leggings.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⭐ Shiny Gold Leggings").formatted(Formatting.YELLOW, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[MFLUX Research Division | Gold Armor Tier 3]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("❤ Max Health: 6 HP (3 hearts)").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armour: 7").formatted(Formatting.GRAY));
        lore.add(Text.literal("⚔ Toughness: 2").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("✓ Superior gold protection").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Brilliant golden glow").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ +20% movement speed").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Night vision when worn").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("\"Shines like the sun\"").formatted(Formatting.GRAY, Formatting.ITALIC));
        leggings.set(DataComponentTypes.LORE, new LoreComponent(lore));
        leggings.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(leggings, "shiny_gold_leggings");
        return leggings;
    }

    public static ItemStack createShinyGoldBoots() {
        ItemStack boots = new ItemStack(Items.GOLDEN_BOOTS);
        boots.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⭐ Shiny Gold Boots").formatted(Formatting.YELLOW, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[MFLUX Research Division | Gold Armor Tier 3]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("❤ Max Health: 6 HP (3 hearts)").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armour: 4").formatted(Formatting.GRAY));
        lore.add(Text.literal("⚔ Toughness: 2").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("✓ Superior gold protection").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Brilliant golden glow").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ +20% movement speed").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Night vision when worn").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("\"Shines like the sun\"").formatted(Formatting.GRAY, Formatting.ITALIC));
        boots.set(DataComponentTypes.LORE, new LoreComponent(lore));
        boots.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(boots, "shiny_gold_boots");
        return boots;
    }

    public static ItemStack createGlisteningGoldHelmet() {
        ItemStack helmet = new ItemStack(Items.GOLDEN_HELMET);
        helmet.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("💫 Glistening Gold Helmet").formatted(Formatting.YELLOW, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[MFLUX Research Division | Gold Armor Tier 4]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("❤ Max Health: 8 HP (4 hearts)").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armour: 5").formatted(Formatting.GRAY));
        lore.add(Text.literal("⚔ Toughness: 3").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("✓ Exceptional gold protection").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Glistening golden aura").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ +30% movement speed").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Night vision when worn").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ +2 hearts regeneration bonus").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("\"Glistens with divine light\"").formatted(Formatting.GRAY, Formatting.ITALIC));
        helmet.set(DataComponentTypes.LORE, new LoreComponent(lore));
        helmet.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(helmet, "glistening_gold_helmet");
        return helmet;
    }

    public static ItemStack createGlisteningGoldChestplate() {
        ItemStack chestplate = new ItemStack(Items.GOLDEN_CHESTPLATE);
        chestplate.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("💫 Glistening Gold Chestplate").formatted(Formatting.YELLOW, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[MFLUX Research Division | Gold Armor Tier 4]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("❤ Max Health: 10 HP (5 hearts)").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armour: 9").formatted(Formatting.GRAY));
        lore.add(Text.literal("⚔ Toughness: 3").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("✓ Exceptional gold protection").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Glistening golden aura").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ +30% movement speed").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Night vision when worn").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ +2 hearts regeneration bonus").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("\"Glistens with divine light\"").formatted(Formatting.GRAY, Formatting.ITALIC));
        chestplate.set(DataComponentTypes.LORE, new LoreComponent(lore));
        chestplate.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(chestplate, "glistening_gold_chestplate");
        return chestplate;
    }

    public static ItemStack createGlisteningGoldLeggings() {
        ItemStack leggings = new ItemStack(Items.GOLDEN_LEGGINGS);
        leggings.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("💫 Glistening Gold Leggings").formatted(Formatting.YELLOW, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[MFLUX Research Division | Gold Armor Tier 4]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("❤ Max Health: 8 HP (4 hearts)").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armour: 8").formatted(Formatting.GRAY));
        lore.add(Text.literal("⚔ Toughness: 3").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("✓ Exceptional gold protection").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Glistening golden aura").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ +30% movement speed").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Night vision when worn").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ +2 hearts regeneration bonus").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("\"Glistens with divine light\"").formatted(Formatting.GRAY, Formatting.ITALIC));
        leggings.set(DataComponentTypes.LORE, new LoreComponent(lore));
        leggings.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(leggings, "glistening_gold_leggings");
        return leggings;
    }

    public static ItemStack createGlisteningGoldBoots() {
        ItemStack boots = new ItemStack(Items.GOLDEN_BOOTS);
        boots.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("💫 Glistening Gold Boots").formatted(Formatting.YELLOW, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[MFLUX Research Division | Gold Armor Tier 4]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("❤ Max Health: 8 HP (4 hearts)").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armour: 5").formatted(Formatting.GRAY));
        lore.add(Text.literal("⚔ Toughness: 3").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("✓ Exceptional gold protection").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Glistening golden aura").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ +30% movement speed").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Night vision when worn").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ +2 hearts regeneration bonus").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("\"Glistens with divine light\"").formatted(Formatting.GRAY, Formatting.ITALIC));
        boots.set(DataComponentTypes.LORE, new LoreComponent(lore));
        boots.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(boots, "glistening_gold_boots");
        return boots;
    }

    public static ItemStack createGildedHelmet() {
        ItemStack helmet = new ItemStack(Items.GOLDEN_HELMET);
        helmet.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("👑 Gilded Helmet").formatted(Formatting.GOLD, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[MFLUX Research Division | Gold Armor Tier 5]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("❤ Max Health: 12 HP (6 hearts)").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armour: 6").formatted(Formatting.GRAY));
        lore.add(Text.literal("⚔ Toughness: 4").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("✓ Ultimate gold protection").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Royal gilded appearance").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ +40% movement speed").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Night vision when worn").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ +4 hearts regeneration bonus").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Fire resistance when worn").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("\"Fit for royalty\"").formatted(Formatting.GRAY, Formatting.ITALIC));
        helmet.set(DataComponentTypes.LORE, new LoreComponent(lore));
        helmet.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(helmet, "gilded_helmet");
        return helmet;
    }

    public static ItemStack createGildedChestplate() {
        ItemStack chestplate = new ItemStack(Items.GOLDEN_CHESTPLATE);
        chestplate.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("👑 Gilded Chestplate").formatted(Formatting.GOLD, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[MFLUX Research Division | Gold Armor Tier 5]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("❤ Max Health: 14 HP (7 hearts)").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armour: 10").formatted(Formatting.GRAY));
        lore.add(Text.literal("⚔ Toughness: 4").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("✓ Ultimate gold protection").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Royal gilded appearance").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ +40% movement speed").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Night vision when worn").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ +4 hearts regeneration bonus").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Fire resistance when worn").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("\"Fit for royalty\"").formatted(Formatting.GRAY, Formatting.ITALIC));
        chestplate.set(DataComponentTypes.LORE, new LoreComponent(lore));
        chestplate.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(chestplate, "gilded_chestplate");
        return chestplate;
    }

    public static ItemStack createGildedLeggings() {
        ItemStack leggings = new ItemStack(Items.GOLDEN_LEGGINGS);
        leggings.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("👑 Gilded Leggings").formatted(Formatting.GOLD, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[MFLUX Research Division | Gold Armor Tier 5]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("❤ Max Health: 12 HP (6 hearts)").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armour: 9").formatted(Formatting.GRAY));
        lore.add(Text.literal("⚔ Toughness: 4").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("✓ Ultimate gold protection").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Royal gilded appearance").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ +40% movement speed").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Night vision when worn").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ +4 hearts regeneration bonus").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Fire resistance when worn").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("\"Fit for royalty\"").formatted(Formatting.GRAY, Formatting.ITALIC));
        leggings.set(DataComponentTypes.LORE, new LoreComponent(lore));
        leggings.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(leggings, "gilded_leggings");
        return leggings;
    }

    public static ItemStack createGildedBoots() {
        ItemStack boots = new ItemStack(Items.GOLDEN_BOOTS);
        boots.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("👑 Gilded Boots").formatted(Formatting.GOLD, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[MFLUX Research Division | Gold Armor Tier 5]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("❤ Max Health: 12 HP (6 hearts)").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armour: 6").formatted(Formatting.GRAY));
        lore.add(Text.literal("⚔ Toughness: 4").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("✓ Ultimate gold protection").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Royal gilded appearance").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ +40% movement speed").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Night vision when worn").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ +4 hearts regeneration bonus").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Fire resistance when worn").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("\"Fit for royalty\"").formatted(Formatting.GRAY, Formatting.ITALIC));
        boots.set(DataComponentTypes.LORE, new LoreComponent(lore));
        boots.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(boots, "gilded_boots");
        return boots;
    }

    public static ItemStack createEnchantedBlackstone() {
        ItemStack blackstone = new ItemStack(Items.BLACKSTONE);
        blackstone.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted Blackstone").formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED MATERIAL ◆").formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Blackstone infused with ancient magic").formatted(Formatting.GRAY));
        lore.add(Text.literal("Used for ultimate armor crafting").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Crafted from 64 blackstone").formatted(Formatting.GRAY));
        blackstone.set(DataComponentTypes.LORE, new LoreComponent(lore));
        blackstone.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(blackstone, "enchanted_blackstone");
        return blackstone;
    }

    public static ItemStack createEnchantedGildedBlackstone() {
        ItemStack blackstone = new ItemStack(Items.BLACKSTONE);
        blackstone.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✦ Enchanted Gilded Blackstone").formatted(Formatting.GOLD, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED MATERIAL ◆").formatted(Formatting.GOLD, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Blackstone infused with gilded magic").formatted(Formatting.GRAY));
        lore.add(Text.literal("Used for ultimate netherite armor crafting").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Crafted from 64 blackstone").formatted(Formatting.GRAY));
        blackstone.set(DataComponentTypes.LORE, new LoreComponent(lore));
        blackstone.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(blackstone, "enchanted_gilded_blackstone");
        return blackstone;
    }

    // ENCHANTED BLOCKS - For shop and crafting
    // ═══════════════════════════════════════════════════════════════

    public static ItemStack createEnchantedCobblestone() {
        ItemStack cobblestone = new ItemStack(Items.COBBLESTONE);
        cobblestone.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted Cobblestone").formatted(Formatting.GRAY, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED BLOCK ◆").formatted(Formatting.GRAY, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced cobblestone").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 cobblestone").formatted(Formatting.GRAY));
        cobblestone.set(DataComponentTypes.LORE, new LoreComponent(lore));
        cobblestone.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(cobblestone, "enchanted_cobblestone");
        return cobblestone;
    }

    public static ItemStack createEnchantedStone() {
        ItemStack stone = new ItemStack(Items.STONE);
        stone.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted Stone").formatted(Formatting.WHITE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED BLOCK ◆").formatted(Formatting.WHITE, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced stone").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 stone").formatted(Formatting.GRAY));
        stone.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stone.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(stone, "enchanted_stone");
        return stone;
    }

    public static ItemStack createEnchantedIronBlock() {
        ItemStack ironBlock = new ItemStack(Items.IRON_BLOCK);
        ironBlock.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted Iron Block").formatted(Formatting.GRAY, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED BLOCK ◆").formatted(Formatting.GRAY, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced iron block").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 iron blocks").formatted(Formatting.GRAY));
        ironBlock.set(DataComponentTypes.LORE, new LoreComponent(lore));
        ironBlock.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(ironBlock, "enchanted_iron_block");
        return ironBlock;
    }

    public static ItemStack createEnchantedGoldBlock() {
        ItemStack goldBlock = new ItemStack(Items.GOLD_BLOCK);
        goldBlock.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✦ Enchanted Gold Block").formatted(Formatting.GOLD, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED BLOCK ◆").formatted(Formatting.GOLD, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced gold block").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 gold blocks").formatted(Formatting.GRAY));
        goldBlock.set(DataComponentTypes.LORE, new LoreComponent(lore));
        goldBlock.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(goldBlock, "enchanted_gold_block");
        return goldBlock;
    }

    public static ItemStack createEnchantedDiamondBlock() {
        ItemStack diamondBlock = new ItemStack(Items.DIAMOND_BLOCK);
        diamondBlock.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✦ Enchanted Diamond Block").formatted(Formatting.AQUA, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED BLOCK ◆").formatted(Formatting.AQUA, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced diamond block").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 diamond blocks").formatted(Formatting.GRAY));
        diamondBlock.set(DataComponentTypes.LORE, new LoreComponent(lore));
        diamondBlock.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(diamondBlock, "enchanted_diamond_block");
        return diamondBlock;
    }

    public static ItemStack createEnchantedOakLog() {
        ItemStack log = new ItemStack(Items.OAK_LOG);
        log.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted Oak Log").formatted(Formatting.DARK_GREEN, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED BLOCK ◆").formatted(Formatting.DARK_GREEN, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced oak log").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 oak logs").formatted(Formatting.GRAY));
        log.set(DataComponentTypes.LORE, new LoreComponent(lore));
        log.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(log, "enchanted_oak_log");
        return log;
    }

    public static ItemStack createEnchantedRose() {
        ItemStack rose = new ItemStack(Items.POPPY);
        rose.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted Rose").formatted(Formatting.RED, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED FLOWER ◆").formatted(Formatting.RED, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced rose").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 roses").formatted(Formatting.GRAY));
        rose.set(DataComponentTypes.LORE, new LoreComponent(lore));
        rose.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(rose, "enchanted_rose");
        return rose;
    }

    public static ItemStack createEnchantedDandelion() {
        ItemStack dandelion = new ItemStack(Items.DANDELION);
        dandelion.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted Dandelion").formatted(Formatting.YELLOW, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED FLOWER ◆").formatted(Formatting.YELLOW, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced dandelion").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 dandelions").formatted(Formatting.GRAY));
        dandelion.set(DataComponentTypes.LORE, new LoreComponent(lore));
        dandelion.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(dandelion, "enchanted_dandelion");
        return dandelion;
    }

    public static ItemStack createEnchantedWheat() {
        ItemStack wheat = new ItemStack(Items.WHEAT);
        wheat.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted Wheat").formatted(Formatting.GOLD, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED CROP ◆").formatted(Formatting.GOLD, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced wheat").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 wheat").formatted(Formatting.GRAY));
        wheat.set(DataComponentTypes.LORE, new LoreComponent(lore));
        wheat.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(wheat, "enchanted_wheat");
        return wheat;
    }

    public static ItemStack createEnchantedCarrot() {
        ItemStack carrot = new ItemStack(Items.CARROT);
        carrot.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted Carrot").formatted(Formatting.GOLD, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED CROP ◆").formatted(Formatting.GOLD, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced carrot").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 carrots").formatted(Formatting.GRAY));
        carrot.set(DataComponentTypes.LORE, new LoreComponent(lore));
        carrot.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(carrot, "enchanted_carrot");
        return carrot;
    }

    public static ItemStack createEnchantedPotato() {
        ItemStack potato = new ItemStack(Items.POTATO);
        potato.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted Potato").formatted(Formatting.YELLOW, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED CROP ◆").formatted(Formatting.YELLOW, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced potato").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 potatoes").formatted(Formatting.GRAY));
        potato.set(DataComponentTypes.LORE, new LoreComponent(lore));
        potato.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(potato, "enchanted_potato");
        return potato;
    }

    public static ItemStack createEnchantedSugarCane() {
        ItemStack sugarCane = new ItemStack(Items.SUGAR_CANE);
        sugarCane.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted Sugar Cane").formatted(Formatting.GREEN, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED PLANT ◆").formatted(Formatting.GREEN, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced sugar cane").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 sugar cane").formatted(Formatting.GRAY));
        sugarCane.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sugarCane.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(sugarCane, "enchanted_sugar_cane");
        return sugarCane;
    }

    // More Enchanted Blocks - Extended variety
    // ═══════════════════════════════════════════════════════════════

    public static ItemStack createEnchantedSand() {
        ItemStack sand = new ItemStack(Items.SAND);
        sand.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted Sand").formatted(Formatting.YELLOW, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED BLOCK ◆").formatted(Formatting.YELLOW, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced sand").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 sand").formatted(Formatting.GRAY));
        sand.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sand.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(sand, "enchanted_sand");
        return sand;
    }

    public static ItemStack createEnchantedRedSand() {
        ItemStack sand = new ItemStack(Items.RED_SAND);
        sand.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted Red Sand").formatted(Formatting.GOLD, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED BLOCK ◆").formatted(Formatting.GOLD, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced red sand").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 red sand").formatted(Formatting.GRAY));
        sand.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sand.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(sand, "enchanted_red_sand");
        return sand;
    }

    public static ItemStack createEnchantedGravel() {
        ItemStack gravel = new ItemStack(Items.GRAVEL);
        gravel.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted Gravel").formatted(Formatting.GRAY, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED BLOCK ◆").formatted(Formatting.GRAY, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced gravel").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 gravel").formatted(Formatting.GRAY));
        gravel.set(DataComponentTypes.LORE, new LoreComponent(lore));
        gravel.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(gravel, "enchanted_gravel");
        return gravel;
    }

    public static ItemStack createEnchantedNetherrack() {
        ItemStack netherrack = new ItemStack(Items.NETHERRACK);
        netherrack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted Netherrack").formatted(Formatting.DARK_RED, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED BLOCK ◆").formatted(Formatting.DARK_RED, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced netherrack").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 netherrack").formatted(Formatting.GRAY));
        netherrack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        netherrack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(netherrack, "enchanted_netherrack");
        return netherrack;
    }

    public static ItemStack createEnchantedEndStone() {
        ItemStack endStone = new ItemStack(Items.END_STONE);
        endStone.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted End Stone").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED BLOCK ◆").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced end stone").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 end stone").formatted(Formatting.GRAY));
        endStone.set(DataComponentTypes.LORE, new LoreComponent(lore));
        endStone.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(endStone, "enchanted_end_stone");
        return endStone;
    }

    public static ItemStack createEnchantedObsidian() {
        ItemStack obsidian = new ItemStack(Items.OBSIDIAN);
        obsidian.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted Obsidian").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED BLOCK ◆").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced obsidian").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 obsidian").formatted(Formatting.GRAY));
        obsidian.set(DataComponentTypes.LORE, new LoreComponent(lore));
        obsidian.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(obsidian, "enchanted_obsidian");
        return obsidian;
    }

    public static ItemStack createEnchantedGlowstone() {
        ItemStack glowstone = new ItemStack(Items.GLOWSTONE);
        glowstone.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted Glowstone").formatted(Formatting.YELLOW, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED BLOCK ◆").formatted(Formatting.YELLOW, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced glowstone").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 glowstone").formatted(Formatting.GRAY));
        glowstone.set(DataComponentTypes.LORE, new LoreComponent(lore));
        glowstone.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(glowstone, "enchanted_glowstone");
        return glowstone;
    }

    public static ItemStack createEnchantedPrismarine() {
        ItemStack prismarine = new ItemStack(Items.PRISMARINE);
        prismarine.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted Prismarine").formatted(Formatting.AQUA, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED BLOCK ◆").formatted(Formatting.AQUA, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced prismarine").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 prismarine").formatted(Formatting.GRAY));
        prismarine.set(DataComponentTypes.LORE, new LoreComponent(lore));
        prismarine.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(prismarine, "enchanted_prismarine");
        return prismarine;
    }

    public static ItemStack createEnchantedCopperBlock() {
        ItemStack copper = new ItemStack(Items.COPPER_BLOCK);
        copper.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted Copper Block").formatted(Formatting.GOLD, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED BLOCK ◆").formatted(Formatting.GOLD, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced copper").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 copper blocks").formatted(Formatting.GRAY));
        copper.set(DataComponentTypes.LORE, new LoreComponent(lore));
        copper.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(copper, "enchanted_copper_block");
        return copper;
    }

    public static ItemStack createEnchantedEmeraldBlock() {
        ItemStack emerald = new ItemStack(Items.EMERALD_BLOCK);
        emerald.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✦ Enchanted Emerald Block").formatted(Formatting.GREEN, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED BLOCK ◆").formatted(Formatting.GREEN, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced emeralds").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 emerald blocks").formatted(Formatting.GRAY));
        emerald.set(DataComponentTypes.LORE, new LoreComponent(lore));
        emerald.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(emerald, "enchanted_emerald_block");
        return emerald;
    }

    public static ItemStack createEnchantedLapisBlock() {
        ItemStack lapis = new ItemStack(Items.LAPIS_BLOCK);
        lapis.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted Lapis Block").formatted(Formatting.BLUE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED BLOCK ◆").formatted(Formatting.BLUE, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced lapis").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 lapis blocks").formatted(Formatting.GRAY));
        lapis.set(DataComponentTypes.LORE, new LoreComponent(lore));
        lapis.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(lapis, "enchanted_lapis_block");
        return lapis;
    }

    public static ItemStack createEnchantedCoalBlock() {
        ItemStack coal = new ItemStack(Items.COAL_BLOCK);
        coal.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted Coal Block").formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED BLOCK ◆").formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced coal").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 coal blocks").formatted(Formatting.GRAY));
        coal.set(DataComponentTypes.LORE, new LoreComponent(lore));
        coal.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(coal, "enchanted_coal_block");
        return coal;
    }

    public static ItemStack createEnchantedNetheriteBlock() {
        ItemStack netherite = new ItemStack(Items.NETHERITE_BLOCK);
        netherite.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✦ Enchanted Netherite Block").formatted(Formatting.DARK_RED, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED BLOCK ◆").formatted(Formatting.DARK_RED, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced netherite").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 netherite blocks").formatted(Formatting.GRAY));
        netherite.set(DataComponentTypes.LORE, new LoreComponent(lore));
        netherite.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(netherite, "enchanted_netherite_block");
        return netherite;
    }

    public static ItemStack createEnchantedQuartzBlock() {
        ItemStack quartz = new ItemStack(Items.QUARTZ_BLOCK);
        quartz.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted Quartz Block").formatted(Formatting.WHITE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED BLOCK ◆").formatted(Formatting.WHITE, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced quartz").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 quartz blocks").formatted(Formatting.GRAY));
        quartz.set(DataComponentTypes.LORE, new LoreComponent(lore));
        quartz.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(quartz, "enchanted_quartz_block");
        return quartz;
    }

    public static ItemStack createEnchantedRedstoneBlock() {
        ItemStack redstone = new ItemStack(Items.REDSTONE_BLOCK);
        redstone.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted Redstone Block").formatted(Formatting.DARK_RED, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED BLOCK ◆").formatted(Formatting.DARK_RED, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced redstone").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 redstone blocks").formatted(Formatting.GRAY));
        redstone.set(DataComponentTypes.LORE, new LoreComponent(lore));
        redstone.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(redstone, "enchanted_redstone_block");
        return redstone;
    }

    public static ItemStack createEnchantedBasalt() {
        ItemStack basalt = new ItemStack(Items.BASALT);
        basalt.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted Basalt").formatted(Formatting.GRAY, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED BLOCK ◆").formatted(Formatting.GRAY, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced basalt").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 basalt").formatted(Formatting.GRAY));
        basalt.set(DataComponentTypes.LORE, new LoreComponent(lore));
        basalt.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(basalt, "enchanted_basalt");
        return basalt;
    }

    public static ItemStack createEnchantedDeepslate() {
        ItemStack deepslate = new ItemStack(Items.DEEPSLATE);
        deepslate.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted Deepslate").formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED BLOCK ◆").formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced deepslate").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 deepslate").formatted(Formatting.GRAY));
        deepslate.set(DataComponentTypes.LORE, new LoreComponent(lore));
        deepslate.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(deepslate, "enchanted_deepslate");
        return deepslate;
    }

    public static ItemStack createEnchantedCobbledDeepslate() {
        ItemStack deepslate = new ItemStack(Items.COBBLED_DEEPSLATE);
        deepslate.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted Cobbled Deepslate").formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED BLOCK ◆").formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced cobbled deepslate").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 cobbled deepslate").formatted(Formatting.GRAY));
        deepslate.set(DataComponentTypes.LORE, new LoreComponent(lore));
        deepslate.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(deepslate, "enchanted_cobbled_deepslate");
        return deepslate;
    }

    public static ItemStack createEnchantedTuff() {
        ItemStack tuff = new ItemStack(Items.TUFF);
        tuff.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted Tuff").formatted(Formatting.GRAY, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED BLOCK ◆").formatted(Formatting.GRAY, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced tuff").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 tuff").formatted(Formatting.GRAY));
        tuff.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tuff.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(tuff, "enchanted_tuff");
        return tuff;
    }

    public static ItemStack createEnchantedCalcite() {
        ItemStack calcite = new ItemStack(Items.CALCITE);
        calcite.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted Calcite").formatted(Formatting.WHITE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED BLOCK ◆").formatted(Formatting.WHITE, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced calcite").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 calcite").formatted(Formatting.GRAY));
        calcite.set(DataComponentTypes.LORE, new LoreComponent(lore));
        calcite.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(calcite, "enchanted_calcite");
        return calcite;
    }

    public static ItemStack createEnchantedAmethystBlock() {
        ItemStack amethyst = new ItemStack(Items.AMETHYST_BLOCK);
        amethyst.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted Amethyst Block").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED BLOCK ◆").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced amethyst").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 amethyst blocks").formatted(Formatting.GRAY));
        amethyst.set(DataComponentTypes.LORE, new LoreComponent(lore));
        amethyst.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(amethyst, "enchanted_amethyst_block");
        return amethyst;
    }

    public static ItemStack createEnchantedGlass() {
        ItemStack glass = new ItemStack(Items.GLASS);
        glass.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted Glass").formatted(Formatting.WHITE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED BLOCK ◆").formatted(Formatting.WHITE, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced glass").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 glass").formatted(Formatting.GRAY));
        glass.set(DataComponentTypes.LORE, new LoreComponent(lore));
        glass.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(glass, "enchanted_glass");
        return glass;
    }

    public static ItemStack createEnchantedIce() {
        ItemStack ice = new ItemStack(Items.ICE);
        ice.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted Ice").formatted(Formatting.AQUA, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED BLOCK ◆").formatted(Formatting.AQUA, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced ice").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 ice").formatted(Formatting.GRAY));
        ice.set(DataComponentTypes.LORE, new LoreComponent(lore));
        ice.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(ice, "enchanted_ice");
        return ice;
    }

    public static ItemStack createEnchantedPackedIce() {
        ItemStack ice = new ItemStack(Items.PACKED_ICE);
        ice.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted Packed Ice").formatted(Formatting.AQUA, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED BLOCK ◆").formatted(Formatting.AQUA, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced packed ice").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 packed ice").formatted(Formatting.GRAY));
        ice.set(DataComponentTypes.LORE, new LoreComponent(lore));
        ice.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(ice, "enchanted_packed_ice");
        return ice;
    }

    public static ItemStack createEnchantedBlueIce() {
        ItemStack ice = new ItemStack(Items.BLUE_ICE);
        ice.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted Blue Ice").formatted(Formatting.BLUE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED BLOCK ◆").formatted(Formatting.BLUE, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced blue ice").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 blue ice").formatted(Formatting.GRAY));
        ice.set(DataComponentTypes.LORE, new LoreComponent(lore));
        ice.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(ice, "enchanted_blue_ice");
        return ice;
    }

    public static ItemStack createEnchantedTerracotta() {
        ItemStack terracotta = new ItemStack(Items.TERRACOTTA);
        terracotta.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted Terracotta").formatted(Formatting.RED, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED BLOCK ◆").formatted(Formatting.RED, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced terracotta").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 terracotta").formatted(Formatting.GRAY));
        terracotta.set(DataComponentTypes.LORE, new LoreComponent(lore));
        terracotta.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(terracotta, "enchanted_terracotta");
        return terracotta;
    }

    public static ItemStack createEnchantedClay() {
        ItemStack clay = new ItemStack(Items.CLAY);
        clay.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted Clay").formatted(Formatting.GRAY, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED BLOCK ◆").formatted(Formatting.GRAY, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced clay").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 clay").formatted(Formatting.GRAY));
        clay.set(DataComponentTypes.LORE, new LoreComponent(lore));
        clay.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(clay, "enchanted_clay");
        return clay;
    }

    public static ItemStack createEnchantedBrick() {
        ItemStack brick = new ItemStack(Items.BRICKS);
        brick.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted Bricks").formatted(Formatting.RED, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED BLOCK ◆").formatted(Formatting.RED, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced bricks").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 bricks").formatted(Formatting.GRAY));
        brick.set(DataComponentTypes.LORE, new LoreComponent(lore));
        brick.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(brick, "enchanted_brick");
        return brick;
    }

    public static ItemStack createEnchantedNetherBrick() {
        ItemStack brick = new ItemStack(Items.NETHER_BRICKS);
        brick.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted Nether Bricks").formatted(Formatting.DARK_RED, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED BLOCK ◆").formatted(Formatting.DARK_RED, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced nether bricks").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 nether bricks").formatted(Formatting.GRAY));
        brick.set(DataComponentTypes.LORE, new LoreComponent(lore));
        brick.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(brick, "enchanted_nether_brick");
        return brick;
    }

    public static ItemStack createEnchantedPurpurBlock() {
        ItemStack purpur = new ItemStack(Items.PURPUR_BLOCK);
        purpur.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted Purpur Block").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED BLOCK ◆").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced purpur").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 purpur blocks").formatted(Formatting.GRAY));
        purpur.set(DataComponentTypes.LORE, new LoreComponent(lore));
        purpur.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(purpur, "enchanted_purpur_block");
        return purpur;
    }

    public static ItemStack createEnchantedSeaLantern() {
        ItemStack lantern = new ItemStack(Items.SEA_LANTERN);
        lantern.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted Sea Lantern").formatted(Formatting.AQUA, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED BLOCK ◆").formatted(Formatting.AQUA, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced sea lantern").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 sea lanterns").formatted(Formatting.GRAY));
        lantern.set(DataComponentTypes.LORE, new LoreComponent(lore));
        lantern.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(lantern, "enchanted_sea_lantern");
        return lantern;
    }

    public static ItemStack createEnchantedShroomlight() {
        ItemStack shroomlight = new ItemStack(Items.SHROOMLIGHT);
        shroomlight.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ Enchanted Shroomlight").formatted(Formatting.YELLOW, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ ENCHANTED BLOCK ◆").formatted(Formatting.YELLOW, Formatting.BOLD));
        lore.add(Text.literal("Magically enhanced shroomlight").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("Crafted from 64 shroomlights").formatted(Formatting.GRAY));
        shroomlight.set(DataComponentTypes.LORE, new LoreComponent(lore));
        shroomlight.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(shroomlight, "enchanted_shroomlight");
        return shroomlight;
    }

    // GILDED NETHERITE ARMOR - Ultimate protection
    // ═══════════════════════════════════════════════════════════════

    public static ItemStack createGildedNetheriteHelmet() {
        ItemStack helmet = new ItemStack(Items.NETHERITE_HELMET);
        helmet.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⚜ Gilded Netherite Helmet").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[MFLUX Research Division | Ultimate Armor Tier 6]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("❤ Max Health: 16 HP (8 hearts)").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armour: 8").formatted(Formatting.GRAY));
        lore.add(Text.literal("⚔ Toughness: 5").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("✓ Ultimate netherite protection").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Gilded netherite aura").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ +50% movement speed").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Night vision when worn").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ +6 hearts regeneration bonus").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Fire resistance when worn").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Knockback resistance +20%").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("\"Beyond legendary\"").formatted(Formatting.GRAY, Formatting.ITALIC));
        helmet.set(DataComponentTypes.LORE, new LoreComponent(lore));
        helmet.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(helmet, "gilded_netherite_helmet");
        return helmet;
    }

    public static ItemStack createGildedNetheriteChestplate() {
        ItemStack chestplate = new ItemStack(Items.NETHERITE_CHESTPLATE);
        chestplate.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⚜ Gilded Netherite Chestplate").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[MFLUX Research Division | Ultimate Armor Tier 6]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("❤ Max Health: 20 HP (10 hearts)").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armour: 12").formatted(Formatting.GRAY));
        lore.add(Text.literal("⚔ Toughness: 5").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("✓ Ultimate netherite protection").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Gilded netherite aura").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ +50% movement speed").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Night vision when worn").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ +6 hearts regeneration bonus").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Fire resistance when worn").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Knockback resistance +20%").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("\"Beyond legendary\"").formatted(Formatting.GRAY, Formatting.ITALIC));
        chestplate.set(DataComponentTypes.LORE, new LoreComponent(lore));
        chestplate.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(chestplate, "gilded_netherite_chestplate");
        return chestplate;
    }

    public static ItemStack createGildedNetheriteLeggings() {
        ItemStack leggings = new ItemStack(Items.NETHERITE_LEGGINGS);
        leggings.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⚜ Gilded Netherite Leggings").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[MFLUX Research Division | Ultimate Armor Tier 6]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("❤ Max Health: 16 HP (8 hearts)").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armour: 11").formatted(Formatting.GRAY));
        lore.add(Text.literal("⚔ Toughness: 5").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("✓ Ultimate netherite protection").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Gilded netherite aura").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ +50% movement speed").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Night vision when worn").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ +6 hearts regeneration bonus").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Fire resistance when worn").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Knockback resistance +20%").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("\"Beyond legendary\"").formatted(Formatting.GRAY, Formatting.ITALIC));
        leggings.set(DataComponentTypes.LORE, new LoreComponent(lore));
        leggings.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(leggings, "gilded_netherite_leggings");
        return leggings;
    }

    public static ItemStack createGildedNetheriteBoots() {
        ItemStack boots = new ItemStack(Items.NETHERITE_BOOTS);
        boots.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⚜ Gilded Netherite Boots").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[MFLUX Research Division | Ultimate Armor Tier 6]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("❤ Max Health: 16 HP (8 hearts)").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armour: 8").formatted(Formatting.GRAY));
        lore.add(Text.literal("⚔ Toughness: 5").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("✓ Ultimate netherite protection").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Gilded netherite aura").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ +50% movement speed").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Night vision when worn").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ +6 hearts regeneration bonus").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Fire resistance when worn").formatted(Formatting.GREEN));
        lore.add(Text.literal("✓ Knockback resistance +20%").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("\"Beyond legendary\"").formatted(Formatting.GRAY, Formatting.ITALIC));
        boots.set(DataComponentTypes.LORE, new LoreComponent(lore));
        boots.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(boots, "gilded_netherite_boots");
        return boots;
    }

    // ============================================================
    // WEAPON ATTRIBUTE SYSTEM
    // ============================================================

    public static boolean isWeaponAttributeToken(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);
        return data != null && data.copyNbt().contains(WEAPON_ATTRIBUTE_TOKEN_KEY);
    }

    public static WeaponAttribute getWeaponAttributeFromToken(ItemStack stack) {
        if (!isWeaponAttributeToken(stack)) return null;
        NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);
        String id = data.copyNbt().getString(WEAPON_ATTRIBUTE_TOKEN_KEY).orElse("");
        return WeaponAttribute.fromId(id);
    }

    public static WeaponAttribute getAppliedWeaponAttribute(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (data == null) return null;
        String id = data.copyNbt().getString(WEAPON_ATTRIBUTE_KEY).orElse("");
        return WeaponAttribute.fromId(id);
    }

    public static void applyWeaponAttribute(ItemStack stack, WeaponAttribute attribute) {
        NbtComponent existing = stack.get(DataComponentTypes.CUSTOM_DATA);
        NbtCompound nbt = (existing != null) ? existing.copyNbt() : new NbtCompound();
        nbt.putString(WEAPON_ATTRIBUTE_KEY, attribute.id);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        
        // Add enhanced lore to the weapon
        List<Text> existingLore = new ArrayList<>();
        LoreComponent currentLore = stack.get(DataComponentTypes.LORE);
        if (currentLore != null) {
            existingLore.addAll(currentLore.lines());
        }
        
        // Add attribute lore at the beginning
        List<Text> newLore = new ArrayList<>();
        newLore.add(Text.literal(""));
        newLore.add(Text.literal("⚔ " + attribute.displayName + " ATTRIBUTE").formatted(attribute.color, Formatting.BOLD));
        newLore.add(Text.literal(""));
        
        // Add thematic description based on attribute type
        switch (attribute) {
            case SHARP -> {
                newLore.add(Text.literal("✦ Enhanced cutting power").formatted(Formatting.YELLOW));
                newLore.add(Text.literal("✦ Critical strikes more frequent").formatted(Formatting.YELLOW));
                break;
            }
            case SWIFT -> {
                newLore.add(Text.literal("✦ Lightning-fast attacks").formatted(Formatting.YELLOW));
                newLore.add(Text.literal("✦ No cooldown between strikes").formatted(Formatting.YELLOW));
                break;
            }
            case VAMPIRIC -> {
                newLore.add(Text.literal("✦ Life stolen from victims").formatted(Formatting.YELLOW));
                newLore.add(Text.literal("✦ Dark power flows through blade").formatted(Formatting.YELLOW));
                break;
            }
            case FROSTBITE -> {
                newLore.add(Text.literal("✦ Cold bites at enemies").formatted(Formatting.YELLOW));
                newLore.add(Text.literal("✦ Ice forms on impact").formatted(Formatting.YELLOW));
                break;
            }
            case THUNDER -> {
                newLore.add(Text.literal("✦ Lightning crackles in blade").formatted(Formatting.YELLOW));
                newLore.add(Text.literal("✦ Storm power unleashed").formatted(Formatting.YELLOW));
                break;
            }
            case POISON -> {
                newLore.add(Text.literal("✦ Venom drips from edge").formatted(Formatting.YELLOW));
                newLore.add(Text.literal("✦ Toxins infect targets").formatted(Formatting.YELLOW));
                break;
            }
            case EXPLOSIVE -> {
                newLore.add(Text.literal("✦ Blast radius on impact").formatted(Formatting.YELLOW));
                newLore.add(Text.literal("✦ Explosive force applied").formatted(Formatting.YELLOW));
                break;
            }
            case HOLY -> {
                newLore.add(Text.literal("✦ Sacred light radiates").formatted(Formatting.YELLOW));
                newLore.add(Text.literal("✦ Unholy enemies burn").formatted(Formatting.YELLOW));
                break;
            }
            case SHADOW -> {
                newLore.add(Text.literal("✦ Darkness cloaks wielder").formatted(Formatting.YELLOW));
                newLore.add(Text.literal("✦ Shadows conceal movements").formatted(Formatting.YELLOW));
                break;
            }
            case WIND -> {
                newLore.add(Text.literal("✦ Gale force in strikes").formatted(Formatting.YELLOW));
                newLore.add(Text.literal("✦ Air currents obey blade").formatted(Formatting.YELLOW));
                break;
            }
        }
        
        newLore.add(Text.literal(""));
        newLore.add(Text.literal("━━━ ORIGINAL STATS ━━━").formatted(Formatting.DARK_GRAY));
        
        // Add existing lore after the attribute section
        newLore.addAll(existingLore);
        stack.set(DataComponentTypes.LORE, new LoreComponent(newLore));
    }

    public static ItemStack createWeaponAttributeToken(WeaponAttribute attr) {
        ItemStack token = new ItemStack(Items.PAPER);
        token.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⚔ " + attr.displayName + " Weapon Token ⚔")
                        .formatted(attr.color, Formatting.BOLD));
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚔ LEGENDARY WEAPON ATTRIBUTE").formatted(attr.color, Formatting.BOLD));
        lore.add(Text.literal(""));
        
        // Add thematic lore based on attribute type
        switch (attr) {
            case SHARP -> {
                lore.add(Text.literal("A razor-sharp essence distilled").formatted(Formatting.GRAY));
                lore.add(Text.literal("from countless battles.").formatted(Formatting.DARK_GRAY));
                lore.add(Text.literal(""));
                lore.add(Text.literal("⚔ ABILITIES:").formatted(attr.color, Formatting.BOLD));
                lore.add(Text.literal("• +15% Attack Damage").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• Critical hit chance increased").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• Armor penetration").formatted(Formatting.YELLOW));
                lore.add(Text.literal(""));
                lore.add(Text.literal("\"Cut through all opposition").formatted(Formatting.RED, Formatting.ITALIC));
                lore.add(Text.literal("with surgical precision.\"").formatted(Formatting.RED, Formatting.ITALIC));
            }
            case SWIFT -> {
                lore.add(Text.literal("The essence of lightning speed").formatted(Formatting.GRAY));
                lore.add(Text.literal("captured in crystalline form.").formatted(Formatting.DARK_GRAY));
                lore.add(Text.literal(""));
                lore.add(Text.literal("⚔ ABILITIES:").formatted(attr.color, Formatting.BOLD));
                lore.add(Text.literal("• +25% Attack Speed").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• No attack cooldown").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• Quick draw bonus").formatted(Formatting.YELLOW));
                lore.add(Text.literal(""));
                lore.add(Text.literal("\"Speed is the essence of war.\"").formatted(Formatting.YELLOW, Formatting.ITALIC));
            }
            case VAMPIRIC -> {
                lore.add(Text.literal("A dark essence pulsing with").formatted(Formatting.GRAY));
                lore.add(Text.literal("stolen life force.").formatted(Formatting.DARK_GRAY));
                lore.add(Text.literal(""));
                lore.add(Text.literal("⚔ ABILITIES:").formatted(attr.color, Formatting.BOLD));
                lore.add(Text.literal("• Lifesteal on hit (+10% max HP)").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• Heal on kill").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• Blood rage on low HP").formatted(Formatting.YELLOW));
                lore.add(Text.literal(""));
                lore.add(Text.literal("\"The blood is the life!\"").formatted(Formatting.DARK_RED, Formatting.ITALIC));
            }
            case FROSTBITE -> {
                lore.add(Text.literal("A fragment of eternal winter").formatted(Formatting.GRAY));
                lore.add(Text.literal("from the frozen peaks.").formatted(Formatting.DARK_GRAY));
                lore.add(Text.literal(""));
                lore.add(Text.literal("⚔ ABILITIES:").formatted(attr.color, Formatting.BOLD));
                lore.add(Text.literal("• Slows enemies on hit").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• Freezes water on contact").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• Frost aura damage").formatted(Formatting.YELLOW));
                lore.add(Text.literal(""));
                lore.add(Text.literal("\"Winter is coming.\"").formatted(Formatting.AQUA, Formatting.ITALIC));
            }
            case THUNDER -> {
                lore.add(Text.literal("The fury of storms captured").formatted(Formatting.GRAY));
                lore.add(Text.literal("in a single token.").formatted(Formatting.DARK_GRAY));
                lore.add(Text.literal(""));
                lore.add(Text.literal("⚔ ABILITIES:").formatted(attr.color, Formatting.BOLD));
                lore.add(Text.literal("• Lightning strikes on hit").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• Chain damage to nearby foes").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• Storm call on critical").formatted(Formatting.YELLOW));
                lore.add(Text.literal(""));
                lore.add(Text.literal("\"Let the storm rage on!\"").formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC));
            }
            case POISON -> {
                lore.add(Text.literal("A toxic essence brewed from").formatted(Formatting.GRAY));
                lore.add(Text.literal("the deadliest venoms.").formatted(Formatting.DARK_GRAY));
                lore.add(Text.literal(""));
                lore.add(Text.literal("⚔ ABILITIES:").formatted(attr.color, Formatting.BOLD));
                lore.add(Text.literal("• Poisons enemies on hit").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• Toxic cloud on kill").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• Wither on critical hits").formatted(Formatting.YELLOW));
                lore.add(Text.literal(""));
                lore.add(Text.literal("\"One touch is all it takes.\"").formatted(Formatting.DARK_GREEN, Formatting.ITALIC));
            }
            case EXPLOSIVE -> {
                lore.add(Text.literal("Unstable explosive power").formatted(Formatting.GRAY));
                lore.add(Text.literal("condensed into a token.").formatted(Formatting.DARK_GRAY));
                lore.add(Text.literal(""));
                lore.add(Text.literal("⚔ ABILITIES:").formatted(attr.color, Formatting.BOLD));
                lore.add(Text.literal("• Area damage on hit").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• Knockback explosion").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• Chain reaction on kill").formatted(Formatting.YELLOW));
                lore.add(Text.literal(""));
                lore.add(Text.literal("\"Boom goes the dynamite!\"").formatted(Formatting.GOLD, Formatting.ITALIC));
            }
            case HOLY -> {
                lore.add(Text.literal("Divine light captured from").formatted(Formatting.GRAY));
                lore.add(Text.literal("the highest heavens.").formatted(Formatting.DARK_GRAY));
                lore.add(Text.literal(""));
                lore.add(Text.literal("⚔ ABILITIES:").formatted(attr.color, Formatting.BOLD));
                lore.add(Text.literal("• Extra damage to undead").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• Smite chance on hit").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• Holy aura protection").formatted(Formatting.YELLOW));
                lore.add(Text.literal(""));
                lore.add(Text.literal("\"Let the light purge darkness.\"").formatted(Formatting.WHITE, Formatting.ITALIC));
            }
            case SHADOW -> {
                lore.add(Text.literal("Pure darkness extracted from").formatted(Formatting.GRAY));
                lore.add(Text.literal("the void between worlds.").formatted(Formatting.DARK_GRAY));
                lore.add(Text.literal(""));
                lore.add(Text.literal("⚔ ABILITIES:").formatted(attr.color, Formatting.BOLD));
                lore.add(Text.literal("• Invisibility on kill").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• Stealth movement bonus").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• Shadow step teleport").formatted(Formatting.YELLOW));
                lore.add(Text.literal(""));
                lore.add(Text.literal("\"From shadows, I strike.\"").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
            }
            case WIND -> {
                lore.add(Text.literal("The essence of gale winds").formatted(Formatting.GRAY));
                lore.add(Text.literal("from mountain peaks.").formatted(Formatting.DARK_GRAY));
                lore.add(Text.literal(""));
                lore.add(Text.literal("⚔ ABILITIES:").formatted(attr.color, Formatting.BOLD));
                lore.add(Text.literal("• Enhanced knockback").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• Flight boost ability").formatted(Formatting.YELLOW));
                lore.add(Text.literal("• Wind burst on critical").formatted(Formatting.YELLOW));
                lore.add(Text.literal(""));
                lore.add(Text.literal("\"Ride the wind.\"").formatted(Formatting.GREEN, Formatting.ITALIC));
            }
        }
        
        lore.add(Text.literal(""));
        lore.add(Text.literal("💰 XP Cost: " + attr.xpCost).formatted(Formatting.GOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ DEBUFF:").formatted(Formatting.RED, Formatting.BOLD));
        lore.add(Text.literal(attr.debuffSummary).formatted(Formatting.DARK_RED));
        
        token.set(DataComponentTypes.LORE, new LoreComponent(lore));
        token.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        
        // Store attribute ID in NBT
        NbtCompound nbt = new NbtCompound();
        nbt.putString(WEAPON_ATTRIBUTE_TOKEN_KEY, attr.id);
        token.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        
        setCustomItemId(token, "weapon_attribute_token_" + attr.id);
        return token;
    }

    // ═══════════════════════════════════════════════════════════════
    // CUSTOM VANILLA-LIKE WEAPONS
    // ═══════════════════════════════════════════════════════════════

    public static ItemStack createReinforcedIronSword() {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⚔ Reinforced Iron Sword").formatted(Formatting.GRAY, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.GRAY));
        lore.add(Text.literal("⚔ Damage: 7 (Iron +1)").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 500").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Reinforced with extra iron plating").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 500);
        setCustomItemId(sword, "reinforced_iron_sword");
        return sword;
    }

    public static ItemStack createHardenedDiamondSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⚔ Hardened Diamond Sword").formatted(Formatting.AQUA, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.AQUA));
        lore.add(Text.literal("⚔ Damage: 8 (Diamond +1)").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 2000").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Compressed diamond edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 2000);
        setCustomItemId(sword, "hardened_diamond_sword");
        return sword;
    }

    public static ItemStack createVoidTouchedSword() {
        ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⚔ Void-Touched Blade").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("⚔ Damage: 9 (Netherite +1)").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 2500").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Infused with void essence").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 2500);
        setCustomItemId(sword, "void_touched_sword");
        return sword;
    }

    public static ItemStack createSharpenedGoldSword() {
        ItemStack sword = new ItemStack(Items.GOLDEN_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⚔ Sharpened Gold Sword").formatted(Formatting.YELLOW, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Damage: 5 (Gold +1)").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 100").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Enchantability: High").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Finely sharpened edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 100);
        setCustomItemId(sword, "sharpened_gold_sword");
        return sword;
    }

    public static ItemStack createHeavyStoneSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⚔ Heavy Stone Sword").formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("⚔ Damage: 6 (Stone +1)").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 200").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Attack Speed: Slower").formatted(Formatting.RED));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Dense stone composition").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 200);
        setCustomItemId(sword, "heavy_stone_sword");
        return sword;
    }

    public static ItemStack createSwiftWoodenSword() {
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⚔ Swift Wooden Sword").formatted(Formatting.GOLD, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("⚔ Damage: 5 (Wood +1)").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 100").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Lightweight ash wood").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 100);
        setCustomItemId(sword, "swift_wooden_sword");
        return sword;
    }
}