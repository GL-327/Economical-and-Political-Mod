package com.political.bounty;
import net.minecraft.util.Prediction;
import com.political.CustomItemLore;
import com.political.PolymerBridge;
import com.political.WeaponAttribute;


import com.political.items.CustomItemHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.CompoundTag;
import com.political.ArmourAttribute;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;

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

    /** Sets the custom_item_id field in this item's CUSTOM_DATA NBT and summarizes item lore for readability. */
    public static void setCustomItemId(ItemStack stack, String id) {
        CustomData existing = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag nbt = (existing != null) ? existing.copyTag() : new CompoundTag();
        nbt.putString(CUSTOM_ITEM_ID_KEY, id);
        // Backwards compatibility: older code paths read from custom_id
        nbt.putString(LEGACY_CUSTOM_ITEM_ID_KEY, id);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        CustomItemLore.applySummarizedLore(stack);
        // Give it a real model if art exists for it. This is a no op for any item without art,
        // which is currently almost all of them, so it can safely sit on the hot path.
        PolymerBridge.applyModel(stack, id);
    }

    /** Returns the custom_item_id of this item, or null if not set. */
    public static String getCustomItemId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        CompoundTag nbt = data.copyTag();
        String id = nbt.getStringOr(CUSTOM_ITEM_ID_KEY, "");
        if (id.isEmpty()) {
            id = nbt.getStringOr(LEGACY_CUSTOM_ITEM_ID_KEY, "");
        }
        if (id.isEmpty()) {
            id = nbt.getStringOr("gem_armor_id", "");
        }
        if (id.isEmpty()) return null;
        return canonicalizeCustomItemId(id);
    }

    /**
     * Canonicalizes legacy/variant IDs so handlers and recipes can use one stable form.
     * Current canonical form for slayer armor is: {@code <type>_t<tier>_<piece>}.
     */
    public static String canonicalizeCustomItemId(String id) {
        if (id == null || id.isBlank()) return id;
        // Normalize slayer armor variants:
        //   zombie_helmet_t1  -> zombie_t1_helmet
        //   zombie_t1_helmet  -> zombie_t1_helmet (unchanged)
        String[] pieces = {"helmet", "chestplate", "leggings", "boots"};
        for (String piece : pieces) {
            String suffixT1 = "_" + piece + "_t1";
            String suffixT2 = "_" + piece + "_t2";
            if (id.endsWith(suffixT1)) {
                String type = id.substring(0, id.length() - suffixT1.length());
                return type + "_t1_" + piece;
            }
            if (id.endsWith(suffixT2)) {
                String type = id.substring(0, id.length() - suffixT2.length());
                return type + "_t2_" + piece;
            }
        }
        return id;
    }

    /** Strips Skyblock-style enchanted suffix so {@code obsidian_sword_sbl_ench} behaves like {@code obsidian_sword} in combat/mining. */
    public static String stripSkyblockEnchantSuffix(String id) {
        if (id != null && id.endsWith("_sbl_ench")) {
            return id.substring(0, id.length() - "_sbl_ench".length());
        }
        return id;
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
        CustomData existing = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag nbt = (existing != null) ? existing.copyTag() : new CompoundTag();
        nbt.putString(BOUNTY_SWORD_TYPE_TAG, type.name());
        if (upgraded) nbt.putBoolean(BOUNTY_SWORD_UPGRADED_TAG, true);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
    }

    public static boolean isSlayerSword(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        // Prefer NBT-based identification (new swords)
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null) {
            CompoundTag nbt = data.copyTag();
            if (nbt.contains(BOUNTY_SWORD_TYPE_TAG) && !nbt.getBooleanOr(BOUNTY_SWORD_UPGRADED_TAG, false)) {
                return true;
            }
        }
        // Fallback: name-based for backward compatibility
        Component name = stack.get(DataComponents.CUSTOM_NAME);
        if (name == null) return false;
        String n = name.getString();
        return n.contains("Bounty Sword") && !n.contains("Bounty Sword II");
    }

    public static boolean isSlayerCore(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Component name = stack.get(DataComponents.CUSTOM_NAME);
        if (name == null) return false;
        return name.getString().contains("Core");
    }

    public static SlayerManager.SlayerType getCoreType(ItemStack stack) {
        if (!isSlayerCore(stack)) return null;

        Component name = stack.get(DataComponents.CUSTOM_NAME);
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
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null) {
            CompoundTag nbt = data.copyTag();
            String typeName = nbt.getString(BOUNTY_SWORD_TYPE_TAG).orElse(null);
            if (typeName != null) {
                for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
                    if (type.name().equals(typeName)) return type;
                }
            }
        }
        // Fallback: name-based for backward compat
        if (!isSlayerSword(stack) && !isUpgradedSlayerSword(stack)) return null;
        Component name = stack.get(DataComponents.CUSTOM_NAME);
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
    public static float getLevelLockedDamageMultiplier(ServerPlayer player, ItemStack weapon) {
        // Check bounty swords
        if (isSlayerSword(weapon)) {
            SlayerManager.SlayerType swordType = getSwordSlayerType(weapon);
            if (swordType == null) return 1.0f;

            int playerLevel = SlayerData.getSlayerLevel(player.getUUID().toString(), swordType);
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

            int playerLevel = SlayerData.getSlayerLevel(player.getUUID().toString(), swordType);

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

        sword.set(DataComponents.CUSTOM_NAME,
                Component.literal(getSwordDisplayName(type))
                        .withStyle(type.color, ChatFormatting.BOLD));

        // Add damage attribute (iron sword base is 7, add 8 for total 15)
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("political", "slayer_sword_" + type.name().toLowerCase() + "_damage"),
                                8,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("political", "slayer_sword_" + type.name().toLowerCase() + "_speed"),
                                -2.4,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("⚔ Bounty Weapon").withStyle(ChatFormatting.DARK_PURPLE));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Damage: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("15").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Deals ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("200% damage").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .append(Component.literal(" to:").withStyle(ChatFormatting.GRAY)));
        lore.add(Component.literal("  • " + type.displayName + "s").withStyle(type.color));
        lore.add(Component.literal("  • " + type.bossName).withStyle(type.color));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Counts as 2 kills per kill").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("Bypasses ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("Bounty Boss Resistance").withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ DEFENCE ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("vs All Bosses: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("-4% damage").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal("vs " + type.displayName + " Boss: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("-19% total (-4 + -15 extra)").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
        lore.add(Component.literal(""));
        lore.add(Component.literal("MFLUX Contractor Program").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("⚠ Requires: " + type.displayName + " Bounty Lvl " + BASIC_SWORD_LEVEL_REQ)
                .withStyle(ChatFormatting.RED));

        sword.set(DataComponents.LORE, new ItemLore(lore));

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

        core.set(DataComponents.CUSTOM_NAME,
                Component.literal(type.displayName + " Core")
                        .withStyle(type.color, ChatFormatting.BOLD));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Used for crafting " + type.displayName).withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("bounty equipment.").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX: Anomalous biological residue | Charadrius core sample]").withStyle(ChatFormatting.DARK_GRAY));

        core.set(DataComponents.LORE, new ItemLore(lore));
        core.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

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
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return false;
        return data.copyTag().contains(ARMOR_ATTRIBUTE_TOKEN_KEY);
    }

    public static ArmourAttribute getAttributeFromToken(ItemStack stack) {
        if (!isAttributeToken(stack)) return null;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        String id = data.copyTag().getString(ARMOR_ATTRIBUTE_TOKEN_KEY).orElse("");
        return ArmourAttribute.fromId(id);
    }

    public static ArmourAttribute getAppliedAttribute(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        String id = data.copyTag().getString(ARMOR_ATTRIBUTE_KEY).orElse("");
        return ArmourAttribute.fromId(id);
    }

    public static void applyAttribute(ItemStack stack, ArmourAttribute attribute) {
        CustomData existing = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag nbt = (existing != null) ? existing.copyTag() : new CompoundTag();
        nbt.putString(ARMOR_ATTRIBUTE_KEY, attribute.id);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        
        // Add enhanced lore to the armor piece
        List<Component> existingLore = new ArrayList<>();
        ItemLore currentLore = stack.get(DataComponents.LORE);
        if (currentLore != null) {
            existingLore.addAll(currentLore.lines());
        }
        
        // Add attribute lore at the beginning
        List<Component> newLore = new ArrayList<>();
        newLore.add(Component.literal(""));
        newLore.add(Component.literal("◆ " + attribute.displayName + " ATTRIBUTE").withStyle(attribute.color, ChatFormatting.BOLD));
        newLore.add(Component.literal(""));
        
        // Add thematic description based on attribute type
        switch (attribute) {
            case BURNING -> {
                newLore.add(Component.literal("✦ Eternal flame courses through this armor").withStyle(ChatFormatting.YELLOW));
                newLore.add(Component.literal("✦ Immune to fire extinguishment").withStyle(ChatFormatting.YELLOW));
                break;
            }
            case SIGHTLESS -> {
                newLore.add(Component.literal("✦ Darkness reveals all to the wearer").withStyle(ChatFormatting.YELLOW));
                newLore.add(Component.literal("✦ Visual impairments have no effect").withStyle(ChatFormatting.YELLOW));
                break;
            }
            case FRENZIED -> {
                newLore.add(Component.literal("✦ Berserk rage enhances attack speed").withStyle(ChatFormatting.YELLOW));
                newLore.add(Component.literal("✦ Occasional nausea from raw power").withStyle(ChatFormatting.YELLOW));
                break;
            }
            case GROUNDED -> {
                newLore.add(Component.literal("✦ Earth's protection against lightning").withStyle(ChatFormatting.YELLOW));
                newLore.add(Component.literal("✦ Deflect incoming projectiles").withStyle(ChatFormatting.YELLOW));
                break;
            }
            case WEBBED -> {
                newLore.add(Component.literal("✦ Move freely through cobwebs").withStyle(ChatFormatting.YELLOW));
                newLore.add(Component.literal("✦ Spiders view you as neutral").withStyle(ChatFormatting.YELLOW));
                break;
            }
            case FROST -> {
                newLore.add(Component.literal("✦ Walk on water as frozen ground").withStyle(ChatFormatting.YELLOW));
                newLore.add(Component.literal("✦ Cold environments no longer harm").withStyle(ChatFormatting.YELLOW));
                break;
            }
            case PHANTOMSTEP -> {
                newLore.add(Component.literal("✦ Walk with spectral silence").withStyle(ChatFormatting.YELLOW));
                newLore.add(Component.literal("✦ Gravity's pull weakened").withStyle(ChatFormatting.YELLOW));
                break;
            }
            case CURSED -> {
                newLore.add(Component.literal("✦ Vampiric life steal on kills").withStyle(ChatFormatting.YELLOW));
                newLore.add(Component.literal("✦ Dark power corrupts the soul").withStyle(ChatFormatting.YELLOW));
                break;
            }
            case OVERGROWN -> {
                newLore.add(Component.literal("✦ Regenerate on natural ground").withStyle(ChatFormatting.YELLOW));
                newLore.add(Component.literal("✦ Earth's vitality flows through").withStyle(ChatFormatting.YELLOW));
                break;
            }
            case VOLATILE -> {
                newLore.add(Component.literal("✦ Explosive death when near defeat").withStyle(ChatFormatting.YELLOW));
                newLore.add(Component.literal("✦ Blast immunity protects wearer").withStyle(ChatFormatting.YELLOW));
                break;
            }
        }
        
        newLore.add(Component.literal(""));
        newLore.add(Component.literal("━━━ ORIGINAL STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        
        // Add existing lore after the attribute section
        newLore.addAll(existingLore);
        
        stack.set(DataComponents.LORE, new ItemLore(newLore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    public static boolean isZombieBerserkerHelmet(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        // Check custom ID first (preferred)
        if (hasCustomItemId(stack, "zombie_berserker_helmet")) return true;
        // Fallback: name-based for old items
        Component name = stack.get(DataComponents.CUSTOM_NAME);
        if (name == null) return false;
        return name.getString().contains("Zombie Berserker Helmet");
    }

    public static boolean canUseZombieBerserkerHelmet(ServerPlayer player) {
        int level = SlayerData.getSlayerLevel(player.getUUID().toString(), SlayerManager.SlayerType.ZOMBIE);
        return level >= ZOMBIE_BERSERKER_LEVEL_REQ;
    }

    // Update lore dynamically based on player level
    public static ItemStack createZombieBerserkerHelmetForPlayer(ServerPlayer player) {
        ItemStack helmet = new ItemStack(Items.LEATHER_HELMET);
        helmet.set(DataComponents.DYED_COLOR, new DyedItemColor(0x1A6600));

        int playerLevel = SlayerData.getSlayerLevel(player.getUUID().toString(), SlayerManager.SlayerType.ZOMBIE);
        boolean meetsRequirement = playerLevel >= BERSERKER_HELMET_LEVEL_REQ;

        helmet.set(DataComponents.CUSTOM_NAME,
                Component.literal("☠ Zombie Berserker Helmet")
                        .withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal(""));
        lore.add(Component.literal("A cursed helm forged from the").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("essence of fallen bounty bosses.").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("§8MFLUX Research Division Prototype").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ EFFECTS ━━━").withStyle(ChatFormatting.RED));
        lore.add(Component.literal(""));
        lore.add(Component.literal("❤ Max Health: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("-50%").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD)));
        lore.add(Component.literal("⚔ Damage Dealt: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+300%").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ BOSS DEFENSE ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("vs Zombie Bosses: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("-6% damage").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
        lore.add(Component.literal("vs All Bosses: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("-20% damage").withStyle(ChatFormatting.GREEN)));

        // Only show requirement if not met [1]
        if (!meetsRequirement) {
            lore.add(Component.literal(""));
            lore.add(Component.literal("⚠ Requires: Zombie Bounty Lvl " + BERSERKER_HELMET_LEVEL_REQ)
                    .withStyle(ChatFormatting.RED));
            lore.add(Component.literal("⛔ LOCKED - Effects disabled").withStyle(ChatFormatting.DARK_RED));
        } else {
            lore.add(Component.literal(""));
            lore.add(Component.literal("✔ UNLOCKED").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        }

        helmet.set(DataComponents.LORE, new ItemLore(lore));
        helmet.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

        return helmet;
    }

    public static final int UPGRADED_SWORD_LEVEL_REQ = 6;

    public static ItemStack createUpgradedSlayerSword(SlayerManager.SlayerType type) {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);

        sword.set(DataComponents.CUSTOM_NAME,
                Component.literal(getUpgradedSwordDisplayName(type))
                        .withStyle(type.color, ChatFormatting.BOLD));

        // Add damage attribute (diamond sword base is 10, add 10 for total 20)
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("political", "upgraded_slayer_sword_" + type.name().toLowerCase() + "_damage"),
                                10,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("political", "upgraded_slayer_sword_" + type.name().toLowerCase() + "_speed"),
                                -2.4,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND);
        sword.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("⚔ Upgraded Bounty Weapon").withStyle(ChatFormatting.DARK_PURPLE));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Damage: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("20").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Deals ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("300% damage").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .append(Component.literal(" to:").withStyle(ChatFormatting.GRAY)));
        lore.add(Component.literal("  • " + type.displayName + "s").withStyle(type.color));
        lore.add(Component.literal("  • " + type.bossName).withStyle(type.color));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Counts as 3 kills per kill").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("Bypasses ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("Bounty Boss Resistance").withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ DEFENCE ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("vs All Bosses: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("-6% damage").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal("vs " + type.displayName + " Boss: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("-26% total (-6 + -20 extra)").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
        lore.add(Component.literal(""));
        lore.add(Component.literal("MFLUX Classified: Charadrius Mk.II").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("⚠ Requires: " + type.displayName + " Bounty Lvl " + UPGRADED_SWORD_LEVEL_REQ)
                .withStyle(ChatFormatting.RED));

        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

        // Custom enchantments are NOT auto-applied — players apply them manually.
        // The lore shows compatible enchantments as a guide only.

        // Tag for NBT-based identification
        setBountySwordNbt(sword, type, true);

        setCustomItemId(sword, type.name().toLowerCase() + "_sword_t2");
        return sword;
    }

    public static ItemStack createSlayerSwordForPlayer(ServerPlayer player, SlayerManager.SlayerType type) {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);

        sword.set(DataComponents.CUSTOM_NAME,
                Component.literal(getSwordDisplayName(type))
                        .withStyle(type.color, ChatFormatting.BOLD));

        int playerLevel = SlayerData.getSlayerLevel(player.getUUID().toString(), type);
        int requiredLevel = BASIC_SWORD_LEVEL_REQ;
        boolean meetsRequirement = playerLevel >= requiredLevel;

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("⚔ Bounty Weapon").withStyle(ChatFormatting.DARK_PURPLE));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Deals ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("200% damage").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .append(Component.literal(" to:").withStyle(ChatFormatting.GRAY)));
        lore.add(Component.literal("  • " + type.displayName + "s").withStyle(type.color));
        lore.add(Component.literal("  • " + type.bossName).withStyle(type.color));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Counts as 2 kills per kill").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("Bypasses ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("Bounty Resistance").withStyle(ChatFormatting.GOLD)));

        // Only show requirement if not met
        if (!meetsRequirement) {
            lore.add(Component.literal(""));
            lore.add(Component.literal("⚠ Requires: " + type.displayName + " Bounty Lvl " + requiredLevel)
                    .withStyle(ChatFormatting.RED));
        }

        sword.set(DataComponents.LORE, new ItemLore(lore));

        return sword;
    }


    public static double getSlayerSwordDamageMultiplier(ItemStack weapon, LivingEntity target, ServerPlayer player) {
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
        if (SlayerManager.isSlayerBoss(target.getUUID())) {
            SlayerManager.SlayerType bossType = SlayerManager.getBossSlayerType(target.getUUID());
            isMatchingBoss = (bossType == swordType);
        }

        if (isMatchingMob || isMatchingBoss) {
            return 1.4;
        }

        if (SlayerManager.isSlayerBoss(target.getUUID())) {
            return 1.2;
        }

        return 1.0;
    }

    public static double getUpgradedSlayerSwordDamageMultiplier(ItemStack weapon, LivingEntity target, ServerPlayer player) {
        if (!isUpgradedSlayerSword(weapon)) return 1.0;

        SlayerManager.SlayerType swordType = getSwordSlayerType(weapon);
        if (swordType == null) return 1.0;

        // Level requirement check
        int playerLevel = SlayerData.getSlayerLevel(player.getUUID().toString(), swordType);
        if (playerLevel < UPGRADED_SWORD_LEVEL_REQ) return 1.0;

        boolean isMatchingMob = isMatchingMobType(target, swordType);
        boolean isMatchingBoss = false;
        if (SlayerManager.isSlayerBoss(target.getUUID())) {
            SlayerManager.SlayerType bossType = SlayerManager.getBossSlayerType(target.getUUID());
            isMatchingBoss = (bossType == swordType);
        }

        if (isMatchingMob || isMatchingBoss) {
            return 1.7;
        }

        if (SlayerManager.isSlayerBoss(target.getUUID())) {
            return 1.35;
        }

        return 1.0;
    }

    public static boolean bypassesSlayerResistance(ItemStack weapon) {
        return isSlayerSword(weapon);
    }

    private static boolean isMatchingMobType(LivingEntity entity, SlayerManager.SlayerType type) {
        return switch (type) {
            case ZOMBIE -> entity instanceof net.minecraft.world.entity.monster.zombie.Zombie;
            case SPIDER -> entity instanceof net.minecraft.world.entity.monster.spider.Spider;
            case SKELETON -> entity instanceof net.minecraft.world.entity.monster.skeleton.Skeleton
                    || entity instanceof net.minecraft.world.entity.monster.skeleton.Stray
                    || entity instanceof net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
            case SLIME -> entity instanceof net.minecraft.world.entity.monster.cubemob.Slime
                    || entity instanceof net.minecraft.world.entity.monster.cubemob.MagmaCube;
            case ENDERMAN -> entity instanceof net.minecraft.world.entity.monster.Enderman;
            case IRON_GOLEM -> entity instanceof net.minecraft.world.entity.monster.warden.Warden;
            case PIGLIN -> entity instanceof net.minecraft.world.entity.monster.piglin.PiglinBrute || entity instanceof net.minecraft.world.entity.monster.piglin.Piglin;
        };
    }

    // ============================================================
    // SLAYER CORES - Rare boss drops for crafting
    // ============================================================

    public static ItemStack createCore(SlayerManager.SlayerType type) {
        ItemStack core = new ItemStack(type.icon);

        String coreName = type.displayName + " Core";

        core.set(DataComponents.CUSTOM_NAME,
                Component.literal("✦ " + coreName + " ✦")
                        .withStyle(type.color, ChatFormatting.BOLD));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal(""));
        lore.add(Component.literal("A powerful essence from the").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(type.bossName + ".").withStyle(type.color));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Used in crafting powerful").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("Bounty equipment.").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX: Anomalous biological residue | Charadrius specimen]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal(""));

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
        lore.add(Component.literal(craftHint).withStyle(ChatFormatting.DARK_PURPLE));

        core.set(DataComponents.LORE, new ItemLore(lore));

        // Make it glow
        core.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

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

        chunk.set(DataComponents.CUSTOM_NAME,
                Component.literal("✦ " + chunkName + " ✦")
                        .withStyle(type.color, ChatFormatting.BOLD));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal(""));
        lore.add(Component.literal("A fragment from the").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(type.bossName + ".").withStyle(type.color));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Used to craft Bounty Hunter's Swords").withStyle(ChatFormatting.DARK_PURPLE));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX: Anomalous biological sample | Charadrius specimen]").withStyle(ChatFormatting.DARK_GRAY));

        chunk.set(DataComponents.LORE, new ItemLore(lore));
        chunk.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

        setCustomItemId(chunk, type.name().toLowerCase() + "_chunk");
        return chunk;
    }
    // ============================================================
    // GIVE COMMANDS (for admin/testing)
    // ============================================================
// ============================================================
// CRAFTING - Check if player can craft sword
// ============================================================

    public static boolean canCraftSlayerSword(ServerPlayer player, SlayerManager.SlayerType type) {
        // Requires 2 chunks of the slayer type
        int chunkCount = countChunks(player, type);
        return chunkCount >= 2;
    }

    public static int countChunks(ServerPlayer player, SlayerManager.SlayerType type) {
        int count = 0;
        String chunkName = getChunkName(type);

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (isChunk(stack, type)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static boolean isChunk(ItemStack stack, SlayerManager.SlayerType type) {
        if (stack == null || stack.isEmpty()) return false;
        Component name = stack.get(DataComponents.CUSTOM_NAME);
        if (name == null) return false;
        String chunkName = getChunkName(type);
        return name.getString().contains(chunkName);
    }

    public static boolean isEnderSword(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Component name = stack.get(DataComponents.CUSTOM_NAME);
        if (name == null) return false;
        return name.getString().contains("Ender Sword");
    }

    public static boolean isAbyssalBlade(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Component name = stack.get(DataComponents.CUSTOM_NAME);
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
        CustomData existing = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag nbt = (existing != null) ? existing.copyTag() : new CompoundTag();
        nbt.putString(CUSTOM_TYPE_KEY, type);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
    }

    public static String getCustomItemType(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        return data.copyTag().getString(CUSTOM_TYPE_KEY).orElse("");
    }
    public static boolean isType(ItemStack stack, String type) {
        return type.equals(getCustomItemType(stack));
    }

    public static void setSlayerLore(ItemStack stack, List<String> lines) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        for (String line : lines) {
            // Convert §7 formatting to proper gray formatting
            String formattedLine = line.replace("§7", "");
            lore.add(Component.literal(formattedLine).withStyle(ChatFormatting.GRAY));
        }
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX: Anomalous boss residue | Charadrius specimen]").withStyle(ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    public static ItemStack createUndeadHeart() {
        ItemStack stack = new ItemStack(Items.NETHER_STAR);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("❤ Undead Heart").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        setSlayerLore(stack, List.of(
            "§7A pulsating heart from a powerful Undead boss.",
            "§7Increases maximum health when held."
        ));
        setCustomItemType(stack, UNDEAD_HEART);
        return stack;
    }

    public static ItemStack createSpectralQuiver() {
        ItemStack stack = new ItemStack(Items.LEATHER_HORSE_ARMOR); 
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("🏹 Spectral Quiver").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        setSlayerLore(stack, List.of(
            "§7A quiver from the Spectral Archer.",
            "§7Arrows have a chance to phase through walls."
        ));
        setCustomItemType(stack, SPECTRAL_QUIVER);
        return stack;
    }

    public static ItemStack createEchoingCore() {
        ItemStack stack = new ItemStack(Items.ECHO_SHARD);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("🌀 Echoing Core").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        setSlayerLore(stack, List.of(
            "§7The pulsating core of a Deep Dark boss.",
            "§7Emits a sonic boom when used."
        ));
        setCustomItemType(stack, ECHOING_CORE);
        return stack;
    }

    public static ItemStack createBouncySlime() {
        ItemStack stack = new ItemStack(Items.SLIME_BALL);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("🟢 Bouncy Slime").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        setSlayerLore(stack, List.of(
            "§7A concentrated blob of slime.",
            "§7Gives jump boost and fall resistance."
        ));
        setCustomItemType(stack, BOUNCY_SLIME);
        return stack;
    }

    public static ItemStack createEnderSword() {
        ItemStack stack = new ItemStack(Items.NETHERITE_SWORD);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("⚔ Ender Sword").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        setSlayerLore(stack, List.of(
            "§7A blade forged from the ender dragon's breath.",
            "§7Teleports you behind your target on hit."
        ));
        setCustomItemType(stack, ENDER_SWORD);
        return stack;
    }

    public static ItemStack createAbyssalBlade() {
        ItemStack stack = new ItemStack(Items.NETHERITE_SWORD);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("🌑 Abyssal Blade").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        setSlayerLore(stack, List.of(
            "§7A blade from the deepest abyss.",
            "§7Deals bonus damage to blinded targets."
        ));
        setCustomItemType(stack, ABYSSAL_BLADE);
        return stack;
    }

    public static boolean canUseSlayerSword(ServerPlayer player, ItemStack sword) {
        if (!isSlayerSword(sword)) return true;

        SlayerManager.SlayerType swordType = getSwordSlayerType(sword);
        if (swordType == null) return true;

        int playerLevel = SlayerData.getSlayerLevel(player.getUUID().toString(), swordType);
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

    public static void giveFullArmorSet(ServerPlayer player, SlayerManager.SlayerType type, int tier) {
        ItemStack helmet = createSlayerHelmet(type, tier);
        ItemStack chestplate = createSlayerChestplate(type, tier);
        ItemStack leggings = createSlayerLeggings(type, tier);
        ItemStack boots = createSlayerBoots(type, tier);

        giveItem(player, helmet);
        giveItem(player, chestplate);
        giveItem(player, leggings);
        giveItem(player, boots);

        String tierName = tier == 1 ? "" : " II";
        player.sendSystemMessage(Component.literal("✔ Received full " + type.displayName + " Bounty Armor" + tierName + " set!")
                .withStyle(ChatFormatting.GREEN), false);
    }

    private static void giveItem(ServerPlayer player, ItemStack stack) {
        if (!player.addItem(stack)) {
            player.drop(stack, false, Prediction.SERVER_ONLY);
        }
    }

    public static boolean isSlayerArmor(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Component name = stack.get(DataComponents.CUSTOM_NAME);
        if (name == null) return false;
        String nameStr = name.getString();
        return nameStr.contains("Slayer Helmet") ||
                nameStr.contains("Slayer Chestplate") ||
                nameStr.contains("Slayer Leggings") ||
                nameStr.contains("Slayer Boots");
    }


    public static boolean craftSlayerSword(ServerPlayer player, SlayerManager.SlayerType type) {
        // Remove 2 chunks
        int toRemove = 2;
        for (int i = 0; i < player.getInventory().getContainerSize() && toRemove > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (isChunk(stack, type)) {
                int removeFromStack = Math.min(toRemove, stack.getCount());
                stack.shrink(removeFromStack);
                toRemove -= removeFromStack;
            }
        }

        // Give sword
        ItemStack sword = createSlayerSword(type);
        if (!player.addItem(sword)) {
            player.drop(sword, false, Prediction.SERVER_ONLY);
        }

        player.sendSystemMessage(Component.literal("✔ Crafted " + type.displayName + " Bounty Hunter's Sword!")
                .withStyle(ChatFormatting.GREEN), false);

        return true;
    }

    public static void giveSlayerSword(ServerPlayer player, SlayerManager.SlayerType type) {
        ItemStack sword = createSlayerSword(type);
        if (!player.addItem(sword)) {
            player.drop(sword, false, Prediction.SERVER_ONLY);
        }
        player.sendSystemMessage(Component.literal("✔ Received " + type.displayName + " Bounty Hunter's Sword!")
                .withStyle(ChatFormatting.GREEN), false);
    }

    public static void giveCore(ServerPlayer player, SlayerManager.SlayerType type) {
        ItemStack core = createCustomCrafterCoreT1();
        if (!player.addItem(core)) {
            player.drop(core, false, Prediction.SERVER_ONLY);
        }
        core.set(DataComponents.CUSTOM_NAME,
                Component.literal("⚙ Custom Crafting Core (T1)").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("✦ Crafting Component").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Used to craft Tier 1 custom").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("armor and weapon pieces.").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Required for:").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("• All T1 Custom Armor Sets").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[Drop from any slayer boss]").withStyle(ChatFormatting.DARK_GRAY));
        core.set(DataComponents.LORE, new ItemLore(lore));
        core.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(core, "custom_crafter_core_t1");
        player.sendSystemMessage(Component.literal("✔ Received " + type.displayName + " Core!")
                .withStyle(ChatFormatting.GREEN), false);
    }

    public static ItemStack createCustomCrafterCoreT1() {
        ItemStack core = new ItemStack(Items.HEAVY_CORE);
        core.set(DataComponents.CUSTOM_NAME,
                Component.literal("⚙ Custom Crafting Core (T1)").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("✦ Crafting Component").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Used to craft Tier 1 custom").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("armor and weapon pieces.").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Required for:").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("• All T1 Custom Armor Sets").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[Drop from any slayer boss]").withStyle(ChatFormatting.DARK_GRAY));
        core.set(DataComponents.LORE, new ItemLore(lore));
        core.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(core, "custom_crafter_core_t1");
        return core;
    }

    public static ItemStack createCustomCrafterCoreT2() {
        ItemStack core = new ItemStack(Items.HEAVY_CORE);
        core.set(DataComponents.CUSTOM_NAME,
                Component.literal("⚙ Custom Crafting Core (T2)").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("✦ Crafting Component").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Used to craft Tier 2 custom").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("armor and weapon pieces.").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Required for:").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("• All T2 Custom Armor Sets").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[Drop from any slayer boss T2+]").withStyle(ChatFormatting.DARK_GRAY));
        core.set(DataComponents.LORE, new ItemLore(lore));
        core.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(core, "custom_crafter_core_t2");
        return core;
    }

    public static ItemStack createCustomCrafterCoreT3() {
        ItemStack core = new ItemStack(Items.HEAVY_CORE);
        core.set(DataComponents.CUSTOM_NAME,
                Component.literal("⚙ Custom Crafting Core (T3)").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("✦ Legendary Crafting Component").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Used to craft Tier 3 custom").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("armor and weapon pieces.").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Required for:").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("• All T3 Custom Armor Sets").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[Rare drop from high-tier bosses]").withStyle(ChatFormatting.DARK_GRAY));
        core.set(DataComponents.LORE, new ItemLore(lore));
        core.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(core, "custom_crafter_core_t3");
        return core;
    }

    public static ItemStack createDragonCrafterCoreT1() {
        ItemStack core = new ItemStack(Items.HEAVY_CORE);
        core.set(DataComponents.CUSTOM_NAME,
                Component.literal("🐉 Dragon Crafting Core (T1)").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("✦ Draconic reagent").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Used for legendary dragon-forged recipes.").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[Tier 1 dragon core]").withStyle(ChatFormatting.DARK_GRAY));
        core.set(DataComponents.LORE, new ItemLore(lore));
        core.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(core, "dragon_crafter_core_t1");
        return core;
    }

    public static ItemStack createDragonCrafterCoreT2() {
        ItemStack core = new ItemStack(Items.HEAVY_CORE);
        core.set(DataComponents.CUSTOM_NAME,
                Component.literal("🐉 Dragon Crafting Core (T2)").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("✦ Infused draconic heart").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Required for mid-tier dragon armor smithing.").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[Tier 2 dragon core]").withStyle(ChatFormatting.DARK_GRAY));
        core.set(DataComponents.LORE, new ItemLore(lore));
        core.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(core, "dragon_crafter_core_t2");
        return core;
    }

    public static ItemStack createDragonCrafterCoreT3() {
        ItemStack core = new ItemStack(Items.HEAVY_CORE);
        core.set(DataComponents.CUSTOM_NAME,
                Component.literal("🐉 Dragon Crafting Core (T3)").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("✦ Ancient wyrm nucleus").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Reserved for apex dragon recipes.").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[Tier 3 dragon core]").withStyle(ChatFormatting.DARK_GRAY));
        core.set(DataComponents.LORE, new ItemLore(lore));
        core.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(core, "dragon_crafter_core_t3");
        return core;
    }

    public static ItemStack createDragonCrafterCoreOmega() {
        ItemStack core = new ItemStack(Items.HEAVY_CORE);
        core.set(DataComponents.CUSTOM_NAME,
                Component.literal("🐉 Omega Dragon Crafting Core").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("✦ Apex crafting component").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Reserved for omega-tier dragon recipes").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[Omega Core]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("• Powers omega chestplate enhancements").withStyle(ChatFormatting.LIGHT_PURPLE));

        core.set(DataComponents.LORE, new ItemLore(lore));
        core.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(core, "dragon_crafter_core_omega");
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

    private static List<Component> buildSlayerArmorLore(SlayerManager.SlayerType type, int tier, String piece, ArmorStats stats) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
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
        lore.add(Component.literal(mfluxLabel).withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(type.color));

        // Per-piece armor/toughness values (already applied via attributes; show in lore)
        double pieceMult = getPieceBossMultiplier(piece);
        double armorValue = stats.armor * pieceMult;
        double toughValue = stats.toughness * pieceMult;
        lore.add(Component.literal("🛡 Armor: +" + (int) Math.round(armorValue)).withStyle(ChatFormatting.BLUE));
        lore.add(Component.literal("🛡 Toughness: +" + (int) Math.round(toughValue)).withStyle(ChatFormatting.BLUE));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ BOSS DEFENSE ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal(""));

        // Per-piece boss reductions
        int pieceSpecificDef = (int) Math.round(stats.bossReduction * pieceMult * 100);
        int pieceAllDef      = (int) Math.round(stats.allBossReduction * pieceMult * 100);
        if (pieceSpecificDef < 1) pieceSpecificDef = 1;
        if (pieceAllDef < 1) pieceAllDef = 1;

        // Full-set boss reductions (actual capped values)
        // T1 full set caps at 26% (20% per-piece + 6% set bonus), T2 at 38% (28% + 10%)
        int fullSetBossDef = (tier == 1) ? 26 : 38;
        int fullSetAllDef  = (int) Math.round(stats.allBossReduction * 100);

        lore.add(Component.literal("This piece: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal("-" + pieceSpecificDef + "% vs " + type.displayName + " Boss")
                        .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
        lore.add(Component.literal("This piece: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal("-" + pieceAllDef + "% vs All Bosses")
                        .withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal("Full set: ")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal("-" + fullSetBossDef + "% vs " + type.displayName + " Boss")
                        .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)));
        lore.add(Component.literal("Full set: ")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal("-" + fullSetAllDef + "% vs All Bosses")
                        .withStyle(ChatFormatting.YELLOW)));

        if (tier >= 2) {
            lore.add(Component.literal("+15% XP from " + type.displayName + " bounties").withStyle(ChatFormatting.AQUA));
        }

        // Per-piece ability
        String pieceBuff = tier == 1 ? getT1PieceBuff(type, piece) : getT2PieceBuff(type, piece);
        if (pieceBuff != null) {
            lore.add(Component.literal(""));
            lore.add(Component.literal("━━━ PIECE ABILITY ━━━").withStyle(ChatFormatting.AQUA));
            lore.add(Component.literal("✦ " + pieceBuff).withStyle(ChatFormatting.WHITE));
        }

        lore.add(Component.literal(""));
        int reqLevel = tier == 1 ? T1_ARMOR_LEVEL_REQ : T2_ARMOR_LEVEL_REQ;
        lore.add(Component.literal("⚠ Requires: " + type.displayName + " Bounty Lvl " + reqLevel)
                .withStyle(ChatFormatting.RED));

        return lore;
    }

// ============================================================
// SPECIAL ARMOR PIECES (Fixed with custom attributes)
// ============================================================

    public static ItemStack createWardenChestplate() {
        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        chestplate.set(DataComponents.DYED_COLOR, new DyedItemColor(0x004D4D));

        chestplate.set(DataComponents.CUSTOM_NAME,
                Component.literal("💀 Sculk Terror Chestplate")
                        .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD));

        // CUSTOM ARMOR ATTRIBUTES
        ItemAttributeModifiers attributes = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "warden_armor"),
                                12.0,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.CHEST)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "warden_toughness"),
                                6.0,   // buffed: 4 → 6
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.CHEST)
                .add(Attributes.MAX_HEALTH,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "warden_health"),
                                24.0,  // buffed: +10 hearts → +12 hearts
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.CHEST)
                .add(Attributes.KNOCKBACK_RESISTANCE,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "warden_knockback"),
                                1.0,  // Full knockback immunity
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.CHEST)
                .build();
        chestplate.set(DataComponents.ATTRIBUTE_MODIFIERS, attributes);

        // Durability - 10x netherite
        chestplate.set(DataComponents.MAX_DAMAGE, 4070);
        chestplate.set(DataComponents.DAMAGE, 0);

        // FIXED LORE
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX: CRD-006 | Warden-class containment armor]"));
        lore.add(Component.literal("§8[STATUS: All field tests resulted in operator fatality]"));
        lore.add(Component.literal("§8[NOTE: \"It doesn't protect you. It makes the Warden hesitate.\"]"));
        lore.add(Component.literal("§7\"Recovered from MFLUX Research Station Sigma-7.\"").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        lore.add(Component.literal("§7\"No operative has successfully worn this in combat\"").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        lore.add(Component.literal("§7\"and survived to file a report.\"").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        lore.add(Component.literal("§7\"The containment field appears to disrupt the\"").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        lore.add(Component.literal("§7\"Warden's echolocation — but at tremendous cost\"").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        lore.add(Component.literal("§7\"to the wearer's nervous system.\"").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_AQUA));
        lore.add(Component.literal("🛡 Armor: +12").withStyle(ChatFormatting.BLUE));
        lore.add(Component.literal("🛡 Toughness: +6").withStyle(ChatFormatting.BLUE));
        lore.add(Component.literal("❤ Health: +12 Hearts").withStyle(ChatFormatting.RED));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ EFFECTS ━━━").withStyle(ChatFormatting.DARK_AQUA));
        lore.add(Component.literal("👁 ESP: See noisy entities (64 blocks)").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal("🌙 Night Vision: Permanent").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("🛡 Darkness Immunity").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal("🛡 Full Knockback Resistance").withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ BOSS DEFENSE ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("vs All Bosses: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("-25% damage").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal("vs Warden Bosses: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("-39% total (-25 + -14 extra)").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
        lore.add(Component.literal(""));
        lore.add(Component.literal("⚠ Requires: Warden Bounty Lvl " + IRON_GOLEM_CHESTPLATE_LEVEL_REQ)
                .withStyle(ChatFormatting.RED));

        chestplate.set(DataComponents.LORE, new ItemLore(lore));
        chestplate.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

        setCustomItemId(chestplate, "warden_chestplate");
        return chestplate;
    }

    public static ItemStack createSpiderLeggings() {
        ItemStack leggings = new ItemStack(Items.LEATHER_LEGGINGS);

        leggings.set(DataComponents.DYED_COLOR,
                new net.minecraft.world.item.component.DyedItemColor(0x8B0000));

        leggings.set(DataComponents.CUSTOM_NAME,
                Component.literal("🕷 Venomous Crawler Leggings")
                        .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));

        ItemAttributeModifiers attributes = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "spider_legs_armor"),
                                10.0,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.LEGS)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "spider_legs_toughness"),
                                4.0,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.LEGS)
                .add(Attributes.MOVEMENT_SPEED,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "spider_speed"),
                                0.50,
                                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                        ),
                        EquipmentSlotGroup.LEGS)
                .build();
        leggings.set(DataComponents.ATTRIBUTE_MODIFIERS, attributes);

        leggings.set(DataComponents.MAX_DAMAGE, 750);
        leggings.set(DataComponents.DAMAGE, 0);

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Woven from the silk of").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("a thousand slain bounty spiders.").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("§8[MFLUX: CRD-002 spider-silk containment leggings]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_RED));
        lore.add(Component.literal("🛡 Armor: +10").withStyle(ChatFormatting.BLUE));
        lore.add(Component.literal("🛡 Toughness: +4").withStyle(ChatFormatting.BLUE));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ EFFECTS ━━━").withStyle(ChatFormatting.DARK_RED));
        lore.add(Component.literal("🕸 Web Immunity: Walk through webs").withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("☠ Poison Immunity").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("⚡ Movement Speed: +50%").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ BOSS DEFENSE ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("vs All Bosses: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("-22% damage").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal("vs Spider Bosses: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("-30% total (-22 + -8 extra)").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
        lore.add(Component.literal(""));
        lore.add(Component.literal("⚠ Requires: Spider Bounty Lvl " + SPIDER_LEGGINGS_LEVEL_REQ)
                .withStyle(ChatFormatting.RED));

        leggings.set(DataComponents.LORE, new ItemLore(lore));
        leggings.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

        setCustomItemId(leggings, "spider_leggings");
        return leggings;
    }

    public static ItemStack createSkeletonBow() {
        ItemStack bow = new ItemStack(Items.BOW);

        bow.set(DataComponents.CUSTOM_NAME,
                Component.literal("🦴 Bone Desperado Bow")
                        .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));

        bow.set(DataComponents.MAX_DAMAGE, 3840);
        bow.set(DataComponents.DAMAGE, 0);

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Carved from the bones of a").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("thousand fallen archers.").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ EFFECTS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("🏹 Standard arrow mechanics").withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("✦ +2x damage to Skeleton Bosses").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal("💀 Headshot: 500% damage (fully-charged)").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⏱ Infinite Durability").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("MFLUX Research Division").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("§8[MFLUX: CRD-003 reanimated projectile weapon]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Skeleton Core: projectiles self-correct in flight").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("⚠ Requires: Skeleton Bounty Lvl " + SKELETON_BOW_LEVEL_REQ)
                .withStyle(ChatFormatting.RED));

        bow.set(DataComponents.LORE, new ItemLore(lore));
        bow.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

        setCustomItemId(bow, "skeleton_bow");
        return bow;
    }

    public static ItemStack createSlimeBoots() {
        ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);

        boots.set(DataComponents.DYED_COLOR,
                new net.minecraft.world.item.component.DyedItemColor(0x7CFC00));

        boots.set(DataComponents.CUSTOM_NAME,
                Component.literal("🟢 Gelatinous Rustler Boots")
                        .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));

        // CUSTOM ARMOR ATTRIBUTES - buffed (Item #6)
        ItemAttributeModifiers attributes = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "slime_boots_armor"),
                                6.0,   // buffed: 4 → 6
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.FEET)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "slime_boots_toughness"),
                                3.0,   // buffed: 2 → 3
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.FEET)
                .add(Attributes.SAFE_FALL_DISTANCE,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "slime_fall"),
                                100.0,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.FEET)
                .build();
        boots.set(DataComponents.ATTRIBUTE_MODIFIERS, attributes);

        // Durability
        boots.set(DataComponents.MAX_DAMAGE, 650);
        boots.set(DataComponents.DAMAGE, 0);

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Bouncy boots made from").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("condensed slime.").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("§8[MFLUX: CRD-004 gelatinous containment boots]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Some say it saves you").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.OBFUSCATED, ChatFormatting.ITALIC));
        lore.add(Component.literal("from death").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.OBFUSCATED, ChatFormatting.ITALIC));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("🛡 Armor: +6").withStyle(ChatFormatting.BLUE));
        lore.add(Component.literal("🛡 Toughness: +3").withStyle(ChatFormatting.BLUE));
        lore.add(Component.literal("⬆ Jump Boost: III").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ EFFECTS ━━━").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("⬆ Jump Boost III").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal("🛡 No Fall Damage").withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ BOSS DEFENSE ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("vs All Bosses: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("-22% damage").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal("vs Slime Bosses: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("-30% total (-22 + -8 extra)").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
        lore.add(Component.literal(""));
        lore.add(Component.literal("⚠ Requires: Slime Bounty Lvl " + SLIME_BOOTS_LEVEL_REQ)
                .withStyle(ChatFormatting.RED));
        boots.set(DataComponents.LORE, new ItemLore(lore));
        boots.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

        setCustomItemId(boots, "slime_boots");
        return boots;
    }
// ============================================================



    // ============================================================
    // ============================================================
// T2 ARMOR - legacy type-specific methods (delegate to unified system)
// ============================================================
    /** @deprecated Use {@link #createT2Helmet(SlayerManager.SlayerType)} with ZOMBIE */
    @Deprecated
    public static ItemStack createT2ZombieHelmet()       { return createT2Helmet(SlayerManager.SlayerType.ZOMBIE); }
    /** @deprecated Use {@link #createT2Chestplate(SlayerManager.SlayerType)} with ZOMBIE */
    @Deprecated
    public static ItemStack createT2ZombieChestplate()   { return createT2Chestplate(SlayerManager.SlayerType.ZOMBIE); }
    /** @deprecated Use {@link #createT2Leggings(SlayerManager.SlayerType)} with ZOMBIE */
    @Deprecated
    public static ItemStack createT2ZombieLeggings()     { return createT2Leggings(SlayerManager.SlayerType.ZOMBIE); }
    /** @deprecated Use {@link #createT2Boots(SlayerManager.SlayerType)} with ZOMBIE */
    @Deprecated
    public static ItemStack createT2ZombieBoots()        { return createT2Boots(SlayerManager.SlayerType.ZOMBIE); }

    /** @deprecated Use {@link #createT2Helmet(SlayerManager.SlayerType)} with SPIDER */
    @Deprecated
    public static ItemStack createT2SpiderHelmet()       { return createT2Helmet(SlayerManager.SlayerType.SPIDER); }
    /** @deprecated Use {@link #createT2Chestplate(SlayerManager.SlayerType)} with SPIDER */
    @Deprecated
    public static ItemStack createT2SpiderChestplate()   { return createT2Chestplate(SlayerManager.SlayerType.SPIDER); }
    /** @deprecated Use {@link #createT2Leggings(SlayerManager.SlayerType)} with SPIDER */
    @Deprecated
    public static ItemStack createT2SpiderLeggings()     { return createT2Leggings(SlayerManager.SlayerType.SPIDER); }
    /** @deprecated Use {@link #createT2Boots(SlayerManager.SlayerType)} with SPIDER */
    @Deprecated
    public static ItemStack createT2SpiderBoots()        { return createT2Boots(SlayerManager.SlayerType.SPIDER); }

    /** @deprecated Use {@link #createT2Helmet(SlayerManager.SlayerType)} with SKELETON */
    @Deprecated
    public static ItemStack createT2SkeletonHelmet()     { return createT2Helmet(SlayerManager.SlayerType.SKELETON); }
    /** @deprecated Use {@link #createT2Chestplate(SlayerManager.SlayerType)} with SKELETON */
    @Deprecated
    public static ItemStack createT2SkeletonChestplate() { return createT2Chestplate(SlayerManager.SlayerType.SKELETON); }
    /** @deprecated Use {@link #createT2Leggings(SlayerManager.SlayerType)} with SKELETON */
    @Deprecated
    public static ItemStack createT2SkeletonLeggings()   { return createT2Leggings(SlayerManager.SlayerType.SKELETON); }
    /** @deprecated Use {@link #createT2Boots(SlayerManager.SlayerType)} with SKELETON */
    @Deprecated
    public static ItemStack createT2SkeletonBoots()      { return createT2Boots(SlayerManager.SlayerType.SKELETON); }

    /** @deprecated Use {@link #createT2Helmet(SlayerManager.SlayerType)} with SLIME */
    @Deprecated
    public static ItemStack createT2SlimeHelmet()        { return createT2Helmet(SlayerManager.SlayerType.SLIME); }
    /** @deprecated Use {@link #createT2Chestplate(SlayerManager.SlayerType)} with SLIME */
    @Deprecated
    public static ItemStack createT2SlimeChestplate()    { return createT2Chestplate(SlayerManager.SlayerType.SLIME); }
    /** @deprecated Use {@link #createT2Leggings(SlayerManager.SlayerType)} with SLIME */
    @Deprecated
    public static ItemStack createT2SlimeLeggings()      { return createT2Leggings(SlayerManager.SlayerType.SLIME); }
    /** @deprecated Use {@link #createT2Boots(SlayerManager.SlayerType)} with SLIME */
    @Deprecated
    public static ItemStack createT2SlimeBoots()         { return createT2Boots(SlayerManager.SlayerType.SLIME); }

    /** @deprecated Use {@link #createT2Helmet(SlayerManager.SlayerType)} with ENDERMAN */
    @Deprecated
    public static ItemStack createT2EndermanHelmet()     { return createT2Helmet(SlayerManager.SlayerType.ENDERMAN); }
    /** @deprecated Use {@link #createT2Chestplate(SlayerManager.SlayerType)} with ENDERMAN */
    @Deprecated
    public static ItemStack createT2EndermanChestplate() { return createT2Chestplate(SlayerManager.SlayerType.ENDERMAN); }
    /** @deprecated Use {@link #createT2Leggings(SlayerManager.SlayerType)} with ENDERMAN */
    @Deprecated
    public static ItemStack createT2EndermanLeggings()   { return createT2Leggings(SlayerManager.SlayerType.ENDERMAN); }
    /** @deprecated Use {@link #createT2Boots(SlayerManager.SlayerType)} with ENDERMAN */
    @Deprecated
    public static ItemStack createT2EndermanBoots()      { return createT2Boots(SlayerManager.SlayerType.ENDERMAN); }

    /** @deprecated Use {@link #createT2Helmet(SlayerManager.SlayerType)} with IRON_GOLEM */
    @Deprecated
    public static ItemStack createT2WardenHelmet()       { return createT2Helmet(SlayerManager.SlayerType.IRON_GOLEM); }
    /** @deprecated Use {@link #createT2Chestplate(SlayerManager.SlayerType)} with IRON_GOLEM */
    @Deprecated
    public static ItemStack createT2WardenChestplate()   { return createT2Chestplate(SlayerManager.SlayerType.IRON_GOLEM); }
    /** @deprecated Use {@link #createT2Leggings(SlayerManager.SlayerType)} with IRON_GOLEM */
    @Deprecated
    public static ItemStack createT2WardenLeggings()     { return createT2Leggings(SlayerManager.SlayerType.IRON_GOLEM); }
    /** @deprecated Use {@link #createT2Boots(SlayerManager.SlayerType)} with IRON_GOLEM */
    @Deprecated
    public static ItemStack createT2WardenBoots()        { return createT2Boots(SlayerManager.SlayerType.IRON_GOLEM); }

    // ============================================================
// MISSING DETECTION METHODS
// ============================================================

    public static boolean isUpgradedSlayerSword(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        // Prefer NBT-based identification (new swords)
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null) {
            CompoundTag nbt = data.copyTag();
            if (nbt.contains(BOUNTY_SWORD_TYPE_TAG) && nbt.getBooleanOr(BOUNTY_SWORD_UPGRADED_TAG, false)) {
                return true;
            }
        }
        // Fallback: name-based for backward compat
        Component name = stack.get(DataComponents.CUSTOM_NAME);
        if (name == null) return false;
        String n = name.getString();
        return n.contains("Slayer Sword II") || n.contains("Bounty Sword II");
    }

    public static boolean isSpiderLeggings(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        // Check custom ID first (preferred)
        if (hasCustomItemId(stack, "spider_leggings")) return true;
        // Fallback: name-based for old items
        Component name = stack.get(DataComponents.CUSTOM_NAME);
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
        Component name = stack.get(DataComponents.CUSTOM_NAME);
        if (name == null) return false;
        String n = name.getString();
        return n.contains("Bone") || n.contains("Desperado") || n.contains("Skeleton");
    }

    public static boolean isSlimeBoots(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        // Check custom ID first (preferred)
        if (hasCustomItemId(stack, "slime_boots")) return true;
        // Fallback: name-based for old items
        Component name = stack.get(DataComponents.CUSTOM_NAME);
        if (name == null) return false;
        String n = name.getString();
        return n.contains("Slime") || n.contains("Gelatinous") || n.contains("Rustler");
    }

    public static boolean isWardenChestplate(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        // Check custom ID first (preferred) - only matches legendary chestplate
        if (hasCustomItemId(stack, "warden_chestplate")) return true;
        // Fallback: name-based for old items - exclude T2 armor (has " II")
        Component name = stack.get(DataComponents.CUSTOM_NAME);
        if (name == null) return false;
        String n = name.getString();
        if (n.contains(" II")) return false; // T2 armor, not legendary
        return n.contains("Sculk") || n.contains("Warden") || n.contains("Terror");
    }

    public static boolean isCrownOfGreed(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (hasCustomItemId(stack, "crown_of_greed")) return true;
        Component name = stack.get(DataComponents.CUSTOM_NAME);
        return name != null && name.getString().contains("Crown of Greed");
    }

    public static boolean isCrownOfMidas(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (hasCustomItemId(stack, "crown_of_midas")) return true;
        Component name = stack.get(DataComponents.CUSTOM_NAME);
        return name != null && name.getString().contains("Crown of Midas");
    }

    public static boolean canUseSkeletonBow(ServerPlayer player) {
        int level = SlayerData.getSlayerLevel(player.getUUID().toString(), SlayerManager.SlayerType.SKELETON);
        return level >= SKELETON_BOW_LEVEL_REQ;
    }

    public static boolean canUseSpiderLeggings(ServerPlayer player) {
        int level = SlayerData.getSlayerLevel(player.getUUID().toString(), SlayerManager.SlayerType.SPIDER);
        return level >= SPIDER_LEGGINGS_LEVEL_REQ;
    }

    public static boolean canUseSlimeBoots(ServerPlayer player) {
        int level = SlayerData.getSlayerLevel(player.getUUID().toString(), SlayerManager.SlayerType.SLIME);
        return level >= SLIME_BOOTS_LEVEL_REQ;
    }

    public static boolean canUseWardenChestplate(ServerPlayer player) {
        int level = SlayerData.getSlayerLevel(player.getUUID().toString(), SlayerManager.SlayerType.IRON_GOLEM);
        return level >= IRON_GOLEM_CHESTPLATE_LEVEL_REQ;
    }

    public static boolean canUseEnderPhaseHelmet(ServerPlayer player) {
        int level = SlayerData.getSlayerLevel(player.getUUID().toString(), SlayerManager.SlayerType.ENDERMAN);
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
        helmet.set(DataComponents.DYED_COLOR, new DyedItemColor(0x1A6600));

        helmet.set(DataComponents.CUSTOM_NAME,
                Component.literal("☠ Zombie Berserker Helmet")
                        .withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD));

        // Set high durability
        helmet.set(DataComponents.MAX_DAMAGE, BERSERKER_HELMET_DURABILITY);
        helmet.set(DataComponents.DAMAGE, 0);

        // Add baked-in armor attributes
        ItemAttributeModifiers attributes = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "berserker_helmet_armor"),
                                10.0,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.HEAD)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "berserker_helmet_toughness"),
                                5.0,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.HEAD)
                .add(Attributes.MAX_HEALTH,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "berserker_helmet_health"),
                                8.0,  // +4 hearts
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.HEAD)
                .build();
        helmet.set(DataComponents.ATTRIBUTE_MODIFIERS, attributes);

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal(""));
        lore.add(Component.literal("A cursed helm forged from the").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("essence of fallen bounty bosses.").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GREEN));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+10").withStyle(ChatFormatting.BLUE)));
        lore.add(Component.literal("🛡 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+5").withStyle(ChatFormatting.BLUE)));
        lore.add(Component.literal("❤ Bonus Health: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+4 Hearts").withStyle(ChatFormatting.RED)));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ EFFECTS ━━━").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Damage Dealt: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+300% MORE").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ BOSS DEFENSE ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("vs All Bosses: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("-20% damage").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal("vs Zombie Bosses: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("-26% total (-20 + -6 extra)").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX: CRD-001 augmented containment helm | Berserker-class]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("⚠ Requires: Zombie Bounty Lvl " + BERSERKER_HELMET_LEVEL_REQ)
                .withStyle(ChatFormatting.RED));

        helmet.set(DataComponents.LORE, new ItemLore(lore));
        helmet.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

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
        Component name = stack.get(DataComponents.CUSTOM_NAME);
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
        Component name = stack.get(DataComponents.CUSTOM_NAME);
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
        Component name = stack.get(DataComponents.CUSTOM_NAME);
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
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(lighterColor));

        // Set custom name
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal(armorName + " " + pieceName)
                        .withStyle(type.color, ChatFormatting.BOLD));

        // Set custom max damage (durability)
        stack.set(DataComponents.MAX_DAMAGE, T1_ARMOR_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);

        // ADD ACTUAL ARMOR ATTRIBUTES including health
        EquipmentSlotGroup slot = getSlotForPiece(pieceName);
        double healthValue = getT1ArmorHealth(type, pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "t1_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        slot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "t1_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        slot);
        if (healthValue > 0) {
            attrBuilder.add(Attributes.MAX_HEALTH,
                    new AttributeModifier(
                            Identifier.fromNamespaceAndPath("politicalserver", "t1_" + pieceName.toLowerCase() + "_health"),
                            healthValue,
                            AttributeModifier.Operation.ADD_VALUE
                    ),
                    slot);
        }
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());

        // Compute per-piece boss defense from ArmorStats * pieceMult
        ArmorStats stats = getArmorStats(type, 1);
        double pieceMult = getPieceBossMultiplier(pieceName);
        int pieceBossDef  = Math.max(1, (int) Math.round(stats.bossReduction * pieceMult * 100));
        int pieceAllDef   = Math.max(1, (int) Math.round(stats.allBossReduction * pieceMult * 100));
        // T1 full set caps at 26% boss-specific (20% per-piece + 6% set bonus); all-boss is sum of all pieces
        int fullSetBoss   = 26;
        int fullSetAll    = (int) Math.round(stats.allBossReduction * 100);

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX: Field-tested containment gear | Contractor Program]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.GREEN)));
        if (healthValue > 0) {
            lore.add(Component.literal("❤ Health: ").withStyle(ChatFormatting.WHITE)
                    .append(Component.literal("+" + (int)(healthValue / 2) + " Hearts").withStyle(ChatFormatting.RED)));
        }
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ BOSS DEFENCE ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("🛡 This piece: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("-" + pieceBossDef + "% " + type.displayName).withStyle(ChatFormatting.AQUA))
                .append(Component.literal(" / -" + pieceAllDef + "% all").withStyle(ChatFormatting.GRAY)));
        lore.add(Component.literal("⚔ Full set bonus: ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal("-" + fullSetBoss + "% " + type.displayName).withStyle(ChatFormatting.GREEN))
                .append(Component.literal(" / -" + fullSetAll + "% all").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        // Per-piece unique buff line
        String pieceBuff = getT1PieceBuff(type, pieceName);
        if (pieceBuff != null) {
            lore.add(Component.literal("━━━ PIECE BUFF ━━━").withStyle(ChatFormatting.AQUA));
            lore.add(Component.literal("✦ " + pieceBuff).withStyle(ChatFormatting.AQUA));
            lore.add(Component.literal(""));
        }
        lore.add(Component.literal("⚠ Requires: " + type.displayName + " Bounty Lvl " + T1_ARMOR_LEVEL_REQ)
                .withStyle(ChatFormatting.RED));

        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
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
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(brighterColor));

        // Set custom name with II
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal(armorName + " " + pieceName + " II")
                        .withStyle(type.color, ChatFormatting.BOLD));

        // Set custom max damage (durability)
        stack.set(DataComponents.MAX_DAMAGE, T2_ARMOR_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);

        // ADD ACTUAL ARMOR ATTRIBUTES including health
        EquipmentSlotGroup slot = getSlotForPiece(pieceName);
        double t2HealthValue = getT2ArmorHealth(type, pieceName);
        ItemAttributeModifiers.Builder t2AttrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "t2_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        slot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "t2_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        slot);
        if (t2HealthValue > 0) {
            t2AttrBuilder.add(Attributes.MAX_HEALTH,
                    new AttributeModifier(
                            Identifier.fromNamespaceAndPath("politicalserver", "t2_" + pieceName.toLowerCase() + "_health"),
                            t2HealthValue,
                            AttributeModifier.Operation.ADD_VALUE
                    ),
                    slot);
        }
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, t2AttrBuilder.build());

        // Compute per-piece boss defense from ArmorStats * pieceMult
        ArmorStats stats = getArmorStats(type, 2);
        double pieceMult = getPieceBossMultiplier(pieceName);
        int pieceBossDef  = Math.max(1, (int) Math.round(stats.bossReduction * pieceMult * 100));
        int pieceAllDef   = Math.max(1, (int) Math.round(stats.allBossReduction * pieceMult * 100));
        // T2 full set caps at 38% boss-specific (28% + 10% set bonus); all-boss is sum of all pieces
        int fullSetBoss   = 38;
        int fullSetAll    = (int) Math.round(stats.allBossReduction * 100);

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX: Upgraded containment gear | Research Division]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_PURPLE));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.AQUA)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.AQUA)));
        if (t2HealthValue > 0) {
            lore.add(Component.literal("❤ Health: ").withStyle(ChatFormatting.WHITE)
                    .append(Component.literal("+" + (int)(t2HealthValue / 2) + " Hearts").withStyle(ChatFormatting.RED)));
        }
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ BOSS DEFENCE ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("🛡 This piece: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("-" + pieceBossDef + "% " + type.displayName).withStyle(ChatFormatting.AQUA))
                .append(Component.literal(" / -" + pieceAllDef + "% all").withStyle(ChatFormatting.GRAY)));
        lore.add(Component.literal("⚔ Full set bonus: ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal("-" + fullSetBoss + "% " + type.displayName).withStyle(ChatFormatting.GREEN))
                .append(Component.literal(" / -" + fullSetAll + "% all").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal("+15% XP from " + type.displayName + " bounties").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal(""));
        // Per-piece unique buff line
        String pieceBuff = getT2PieceBuff(type, pieceName);
        if (pieceBuff != null) {
            lore.add(Component.literal("━━━ PIECE BUFF ━━━").withStyle(ChatFormatting.LIGHT_PURPLE));
            lore.add(Component.literal("✦ " + pieceBuff).withStyle(ChatFormatting.LIGHT_PURPLE));
            lore.add(Component.literal(""));
        }
        lore.add(Component.literal("⚠ Requires: " + type.displayName + " Bounty Lvl " + T2_ARMOR_LEVEL_REQ)
                .withStyle(ChatFormatting.RED));

        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
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
    private static EquipmentSlotGroup getSlotForPiece(String pieceName) {
        return switch (pieceName) {
            case "Helmet" -> EquipmentSlotGroup.HEAD;
            case "Chestplate" -> EquipmentSlotGroup.CHEST;
            case "Leggings" -> EquipmentSlotGroup.LEGS;
            case "Boots" -> EquipmentSlotGroup.FEET;
            default -> EquipmentSlotGroup.ANY;
        };
    }

    public static ItemStack createPiglinCore() {
        ItemStack core = new ItemStack(Items.GOLD_NUGGET);
        core.set(DataComponents.CUSTOM_NAME,
                Component.literal("✦ Piglin Core ✦").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("A powerful essence from the").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("The Gilded Ravager.").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Used to craft the Crown of Greed.").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX: CRD-007 Piglin-class anomalous residue]").withStyle(ChatFormatting.DARK_GRAY));
        core.set(DataComponents.LORE, new ItemLore(lore));
        core.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(core, "piglin_core");
        return core;
    }

    public static ItemStack createPiglinFlesh() {
        ItemStack flesh = new ItemStack(Items.GOLD_NUGGET);
        flesh.set(DataComponents.CUSTOM_NAME,
                Component.literal("Piglin Flesh").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("The singed flesh of a fallen Piglin.").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("Still radiates heat.").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX: Biological material, handle with care]").withStyle(ChatFormatting.DARK_GRAY));
        flesh.set(DataComponents.LORE, new ItemLore(lore));
        setCustomItemId(flesh, "piglin_flesh");
        return flesh;
    }


    public static ItemStack createAttributeToken(ArmourAttribute attr) {
        ItemStack token = new ItemStack(Items.PAPER);
        token.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ " + attr.displayName + " Attribute Token ✧")
                        .withStyle(attr.color, ChatFormatting.BOLD));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ LEGENDARY ATTRIBUTE").withStyle(attr.color, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        
        // Add thematic lore based on attribute type
        switch (attr) {
            case BURNING -> {
                lore.add(Component.literal("A crystallized fragment of eternal flame").withStyle(ChatFormatting.GRAY));
                lore.add(Component.literal("forged in deepest Nether fires.").withStyle(ChatFormatting.DARK_GRAY));
                lore.add(Component.literal(""));
                lore.add(Component.literal("◆ ABILITIES:").withStyle(attr.color, ChatFormatting.BOLD));
                lore.add(Component.literal("• 3x damage while on fire").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• Immune to fire extinguishment").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• Fire damage immunity").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal(""));
                lore.add(Component.literal("Harness power of perpetual combustion").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("and become immune to extinguishment.").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal(""));
                lore.add(Component.literal("\"If you want to shine like the sun,").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
                lore.add(Component.literal("first you have to burn like it.\"").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
                break;
            }
            case SIGHTLESS -> {
                lore.add(Component.literal("An essence of pure darkness").withStyle(ChatFormatting.GRAY));
                lore.add(Component.literal("whispered by blind cave dwellers.").withStyle(ChatFormatting.DARK_GRAY));
                lore.add(Component.literal(""));
                lore.add(Component.literal("◆ ABILITIES:").withStyle(attr.color, ChatFormatting.BOLD));
                lore.add(Component.literal("• Permanent Night Vision").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• Blindness immunity").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• Reduced FOV with vignette").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal(""));
                lore.add(Component.literal("See beyond mortal sight limitations").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("while immune to visual impairments.").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal(""));
                lore.add(Component.literal("\"In darkness, true vision begins.\"").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
                break;
            }
            case FRENZIED -> {
                lore.add(Component.literal("The raw rage of ancient warriors").withStyle(ChatFormatting.GRAY));
                lore.add(Component.literal("distilled into crystalline form.").withStyle(ChatFormatting.DARK_GRAY));
                lore.add(Component.literal(""));
                lore.add(Component.literal("◆ ABILITIES:").withStyle(attr.color, ChatFormatting.BOLD));
                lore.add(Component.literal("• +15% Attack Speed").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• Berserk combat buffs").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• Cannot eat food").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal(""));
                lore.add(Component.literal("Unleash berserk fury in combat").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("with enhanced attack speed.").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal(""));
                lore.add(Component.literal("\"Rage is the weapon of the fierce.\"").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
                break;
            }
            case GROUNDED -> {
                lore.add(Component.literal("Earth's steadfast protection").withStyle(ChatFormatting.GRAY));
                lore.add(Component.literal("solidified into defensive crystal.").withStyle(ChatFormatting.DARK_GRAY));
                lore.add(Component.literal(""));
                lore.add(Component.literal("◆ ABILITIES:").withStyle(attr.color, ChatFormatting.BOLD));
                lore.add(Component.literal("• Lightning immunity").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• Projectile deflection").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• Reduced jump height").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal(""));
                lore.add(Component.literal("Stand firm against lightning strikes").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("and deflect incoming projectiles.").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal(""));
                lore.add(Component.literal("\"As steady as the mountain itself.\"").withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC));
                break;
            }
            case WEBBED -> {
                lore.add(Component.literal("Spider silk woven with magic").withStyle(ChatFormatting.GRAY));
                lore.add(Component.literal("enchanted by ancient arachnids.").withStyle(ChatFormatting.DARK_GRAY));
                lore.add(Component.literal(""));
                lore.add(Component.literal("◆ ABILITIES:").withStyle(attr.color, ChatFormatting.BOLD));
                lore.add(Component.literal("• Move through cobweb barriers").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• Gain spider neutrality").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• Slowed on grass/leaves").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal(""));
                lore.add(Component.literal("Move freely through cobweb barriers").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("and gain spider neutrality.").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal(""));
                lore.add(Component.literal("\"The web catches all but the spider.\"").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC));
                break;
            }
            case FROST -> {
                lore.add(Component.literal("Frozen essence of ice giants").withStyle(ChatFormatting.GRAY));
                lore.add(Component.literal("preserved in eternal winter.").withStyle(ChatFormatting.DARK_GRAY));
                lore.add(Component.literal(""));
                lore.add(Component.literal("◆ ABILITIES:").withStyle(attr.color, ChatFormatting.BOLD));
                lore.add(Component.literal("• Walk on water as frozen ground").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• Freeze immunity").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• 150% lava damage").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal(""));
                lore.add(Component.literal("Walk on water as frozen ground").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("and resist the cold embrace.").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal(""));
                lore.add(Component.literal("\"Winter's fury, summer's bane.\"").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
                break;
            }
            case PHANTOMSTEP -> {
                lore.add(Component.literal("Ethereal essence of phantoms").withStyle(ChatFormatting.GRAY));
                lore.add(Component.literal("captured from silent hunters.").withStyle(ChatFormatting.DARK_GRAY));
                lore.add(Component.literal(""));
                lore.add(Component.literal("◆ ABILITIES:").withStyle(attr.color, ChatFormatting.BOLD));
                lore.add(Component.literal("• No fall damage").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• Stealth step sounds").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• Phantoms always target you").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• Cannot sleep").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal(""));
                lore.add(Component.literal("Walk with spectral silence").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("and laugh at gravity's pull.").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal(""));
                lore.add(Component.literal("\"Between steps, there is only silence.\"").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
                break;
            }
            case CURSED -> {
                lore.add(Component.literal("Dark power of forbidden knowledge").withStyle(ChatFormatting.GRAY));
                lore.add(Component.literal("bound in blood magic parchment.").withStyle(ChatFormatting.DARK_GRAY));
                lore.add(Component.literal(""));
                lore.add(Component.literal("◆ ABILITIES:").withStyle(attr.color, ChatFormatting.BOLD));
                lore.add(Component.literal("• +6 Max Hearts").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• 20% life steal on damage").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• XP reduction").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• Villager hostility").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal(""));
                lore.add(Component.literal("Steal life force with each kill").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("and embrace vampiric power.").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal(""));
                lore.add(Component.literal("\"Life given, life taken away.\"").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
                break;
            }
            case OVERGROWN -> {
                lore.add(Component.literal("Nature's healing crystallized").withStyle(ChatFormatting.GRAY));
                lore.add(Component.literal("blessed by ancient forest spirits.").withStyle(ChatFormatting.DARK_GRAY));
                lore.add(Component.literal(""));
                lore.add(Component.literal("◆ ABILITIES:").withStyle(attr.color, ChatFormatting.BOLD));
                lore.add(Component.literal("• Regenerate on natural ground").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• Channel earth's vitality").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• Reduced mining speed").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• Nether/End decay").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal(""));
                lore.add(Component.literal("Regenerate on natural ground").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("and channel earth's vitality.").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal(""));
                lore.add(Component.literal("\"From earth we come, to earth we return.\"").withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC));
                break;
            }
            case VOLATILE -> {
                lore.add(Component.literal("Explosive power of chaos").withStyle(ChatFormatting.GRAY));
                lore.add(Component.literal("contained in unstable crystal.").withStyle(ChatFormatting.DARK_GRAY));
                lore.add(Component.literal(""));
                lore.add(Component.literal("◆ ABILITIES:").withStyle(attr.color, ChatFormatting.BOLD));
                lore.add(Component.literal("• Explode when near death").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• Blast immunity").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• Blowback with fire items").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal(""));
                lore.add(Component.literal("Detonate when near death").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("and become blast immune.").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal(""));
                lore.add(Component.literal("\"In destruction, there is creation.\"").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
                break;
            }
        }
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ USAGE ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("Apply to: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(attr.slot == null ? "Any Armor Piece" : attr.slot.getName().toUpperCase())
                        .withStyle(ChatFormatting.AQUA)));
        lore.add(Component.literal("XP Cost: ").withStyle(ChatFormatting.RED)
                .append(Component.literal(attr.xpCost + " levels").withStyle(ChatFormatting.BOLD, ChatFormatting.RED)));
        lore.add(Component.literal(""));
        lore.add(Component.literal("⚔ Combine with armor in anvil").withStyle(ChatFormatting.YELLOW));
        
        token.set(DataComponents.LORE, new ItemLore(lore)); // Moved this line up
        token.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        CompoundTag nbt = new CompoundTag();
        nbt.putString(ARMOR_ATTRIBUTE_TOKEN_KEY, attr.id);
        token.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        
        setCustomItemId(token, "attribute_token_" + attr.id);
        token.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        return token;
    }

    public static ItemStack createAttributeCore(ArmourAttribute attr) {
        ItemStack core = new ItemStack(Items.NETHER_STAR);
        core.set(DataComponents.CUSTOM_NAME,
                Component.literal(attr.displayName + " Core").withStyle(attr.color, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX: Attribute essence containment | Research Division]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("A crystallized essence of").withStyle(attr.color));
        lore.add(Component.literal(attr.displayName.toLowerCase() + " power.").withStyle(attr.color));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Used to craft " + attr.displayName + " Attribute Token.").withStyle(ChatFormatting.GRAY));
        core.set(DataComponents.LORE, new ItemLore(lore));
        core.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(core, "attribute_core_" + attr.id);
        return core;
    }

    public static ItemStack createVoidwalkerCrown() {
        ItemStack stack = new ItemStack(Items.NETHERITE_HELMET);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("👑 Voidwalker's Crown").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));

        // CUSTOM ARMOR ATTRIBUTES
        ItemAttributeModifiers attributes = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "voidwalker_armor"),
                                8.0,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.HEAD)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "voidwalker_toughness"),
                                4.0,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.HEAD)
                .add(Attributes.MAX_HEALTH,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "voidwalker_health"),
                                20.0,  // +10 hearts
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.HEAD)
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "voidwalker_damage"),
                                3.0,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.HEAD)
                .build();
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attributes);

        // Durability - 8x netherite
        stack.set(DataComponents.MAX_DAMAGE, 3256);
        stack.set(DataComponents.DAMAGE, 0);

        // ENHANCED LORE
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX: CRD-007 | Void-phase containment circlet]"));
        lore.add(Component.literal("§8[STATUS: Field testing terminated after 7 disappearances]"));
        lore.add(Component.literal("§8[NOTE: \"It doesn't teleport you. It makes the void notice you.\"]"));
        lore.add(Component.literal("§7\"Recovered from the ruins of Research Station Epsilon-3.\"").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        lore.add(Component.literal("§7\"Subjects report hearing whispers from the void\"").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        lore.add(Component.literal("§7\"and experiencing moments of non-existence.\"").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        lore.add(Component.literal("§7\"The circlet appears to bend spacetime around the wearer\"").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        lore.add(Component.literal("§7\"but the void demands its price.\"").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_PURPLE));
        lore.add(Component.literal("🛡 Armor: +8").withStyle(ChatFormatting.BLUE));
        lore.add(Component.literal("🛡 Toughness: +4").withStyle(ChatFormatting.BLUE));
        lore.add(Component.literal("❤ Health: +10 Hearts").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Damage: +3").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("❁ Strength: +45").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("☣ Crit Chance: +10%").withStyle(ChatFormatting.BLUE));
        lore.add(Component.literal("☠ Crit Damage: +40%").withStyle(ChatFormatting.BLUE));
        lore.add(Component.literal("✎ Intelligence: +50").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal("✦ Speed: +5").withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ EFFECTS ━━━").withStyle(ChatFormatting.DARK_PURPLE));
        lore.add(Component.literal("🌙 Void Sight: ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal("permanent Night Vision while worn").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal("⚡ Phase Dodge: ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal("20% chance to phase out of a hit,").withStyle(ChatFormatting.AQUA)));
        lore.add(Component.literal("   taking no damage and blinking 3 blocks away").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ VOID POWERS ━━━").withStyle(ChatFormatting.DARK_PURPLE));
        lore.add(Component.literal("🌌 Dark Affinity: ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal("+20% damage").withStyle(ChatFormatting.LIGHT_PURPLE)));
        lore.add(Component.literal("   at night, or against foes in Darkness/Blindness").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ LEGENDARY ABILITY ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("🌀 Void Step: ").withStyle(ChatFormatting.LIGHT_PURPLE)
                .append(Component.literal("Sprint + Sneak to blink 4 blocks").withStyle(ChatFormatting.WHITE)));
        lore.add(Component.literal("   forward through thin walls; fall damage resets").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("   Cooldown: 2.5 seconds").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§7\"The void remembers. The void waits. The void claims.\"").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));

        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(stack, "voidwalker_crown");

        return stack;
    }

    public static ItemStack createEnderPhaseHelmet() {
        ItemStack stack = new ItemStack(Items.NETHERITE_HELMET);
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("✦ Ender Phase Helmet").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));

        ItemAttributeModifiers attributes = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "ender_phase_armor"),
                                7.0,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.HEAD)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "ender_phase_toughness"),
                                3.0,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.HEAD)
                .build();
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attributes);

        stack.set(DataComponents.MAX_DAMAGE, 2035);
        stack.set(DataComponents.DAMAGE, 0);

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX: PHS-011 | Partial-displacement headgear]"));
        lore.add(Component.literal("§8[NOTE: \"The wearer is still here. Mostly.\"]"));
        lore.add(Component.literal("§7\"Salvaged from an End city that was never fully built,\"").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        lore.add(Component.literal("§7\"because its masons kept slipping out of the world.\"").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_PURPLE));
        lore.add(Component.literal("🛡 Armor: +7").withStyle(ChatFormatting.BLUE));
        lore.add(Component.literal("🛡 Toughness: +3").withStyle(ChatFormatting.BLUE));
        lore.add(Component.literal("❈ Defense: +40").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✎ Intelligence: +75").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal("✦ Speed: +10").withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("☣ Crit Chance: +5%").withStyle(ChatFormatting.BLUE));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ ENDER PHASE ━━━").withStyle(ChatFormatting.DARK_PURPLE));
        lore.add(Component.literal("Hold Sneak to enter Ender Phase:").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal(" • Invisibility while phased").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(" • Speed II while phased").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(" • Cloaked in Darkness (hides your outline)").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(" • Portal shimmer marks your drift").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("Release Sneak to snap back into the world.").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ THE PRICE ━━━").withStyle(ChatFormatting.DARK_RED));
        lore.add(Component.literal("✗ You cannot strike other players while phased").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("   — half-real fists pass straight through.").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§7\"Step sideways from the world, and nothing can hold you.\"").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));

        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(stack, "ender_phase_helmet");
        return stack;
    }

    public static ItemStack createVenomousDagger() {
        ItemStack stack = new ItemStack(Items.IRON_SWORD);
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("✦ Venomous Dagger").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD));

        stack.set(DataComponents.MAX_DAMAGE, 1560);
        stack.set(DataComponents.DAMAGE, 0);

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX: VNM-003 | Cultivated arachnid toxin, live sample]"));
        lore.add(Component.literal("§8[WARNING: Blade remains wet. It does not dry.]"));
        lore.add(Component.literal("§7\"A poor blade in a duel. A death sentence in a long fight.\"").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GREEN));
        lore.add(Component.literal("⚔ Damage: +60").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("❁ Strength: +50").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("☣ Crit Chance: +15%").withStyle(ChatFormatting.BLUE));
        lore.add(Component.literal("☠ Crit Damage: +45%").withStyle(ChatFormatting.BLUE));
        lore.add(Component.literal("⫽ Ferocity: +5").withStyle(ChatFormatting.RED));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ VENOM ━━━").withStyle(ChatFormatting.DARK_GREEN));
        lore.add(Component.literal("Every hit applies 1 Venom stack (max 10).").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("Stacks fall off if the target is not struck").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("again within 6 seconds.").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal(" • 1-3 stacks: ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal("Poison I").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(" • 4-6 stacks: ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal("Poison II").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(" • 6+ stacks: ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal("venom turns necrotic — Wither").withStyle(ChatFormatting.DARK_GRAY)));
        lore.add(Component.literal(" • 7-9 stacks: ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal("Poison III, Wither II at 9").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(" • 10 stacks: ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal("RUPTURE — 8 burst magic damage,").withStyle(ChatFormatting.DARK_RED)));
        lore.add(Component.literal("   then the venom is spent and stacks reset").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§7\"Patience is the assassin's only real weapon.\"").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));

        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(stack, "venomous_dagger");
        return stack;
    }

    public static ItemStack createCrownOfGreed() {
        ItemStack crown = new ItemStack(Items.LEATHER_HELMET);
        crown.set(DataComponents.DYED_COLOR, new DyedItemColor(0xD4AF00));
        crown.set(DataComponents.CUSTOM_NAME,
                Component.literal("👑 Crown of Greed").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX Research Division | Specimen CRD-007-G]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("❤ Max Health: 4 HP (2 hearts)").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🛡 Armour: 0 (all defence removed)").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Damage: ×(digits of your coins)").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("  (e.g. 999 coins = ×3 damage)").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ EFFECTS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("✓ Allows any items (not gold-only)").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✗ All bounty buffs/defence disabled").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("✗ All armour defence cleared").withStyle(ChatFormatting.RED));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ WARNING ━━━").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        lore.add(Component.literal("💀 ON DEATH: Lose ALL coins!").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("\"Wealth is power — and power devours.\"").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        crown.set(DataComponents.LORE, new ItemLore(lore));
        crown.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        crown.set(DataComponents.MAX_DAMAGE, 5000); // Buffed durability
        setCustomItemId(crown, "crown_of_greed");
        return crown;
    }

    public static ItemStack createCrownOfMidas() {
        ItemStack crown = new ItemStack(Items.GOLDEN_HELMET);
        crown.set(DataComponents.DYED_COLOR, new DyedItemColor(0xFFD700));
        crown.set(DataComponents.CUSTOM_NAME,
                Component.literal("👑 Crown of Midas").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX Research Division | Specimen CRD-008-M]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("❤ Max Health: 10 HP (5 hearts)").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🛡 Armour: 0 (all defence removed)").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Damage: ×(digits of your coins × 3)").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("  (e.g. 999 coins = ×9 damage)").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ EFFECTS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("✓ Only allows gold items + piglin armor").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✗ All bounty buffs/defence disabled").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("✗ All armour defence cleared").withStyle(ChatFormatting.RED));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ WARNING ━━━").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        lore.add(Component.literal("💀 ON DEATH: Lose ALL coins!").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("\"The golden touch comes at a price.\"").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        crown.set(DataComponents.LORE, new ItemLore(lore));
        crown.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        crown.set(DataComponents.MAX_DAMAGE, 5000); // Buffed durability
        setCustomItemId(crown, "crown_of_midas");
        return crown;
    }

    public static ItemStack createMidasSword() {
        ItemStack sword = new ItemStack(Items.GOLDEN_SWORD);
        sword.set(DataComponents.CUSTOM_NAME,
                Component.literal("⚔ Midas's Sword").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX Research Division | Specimen CRD-009-M]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Base Damage: 4 (Golden Sword)").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Kill Bonus: +1 coin per mob kill").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ DAMAGE SCALING ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("⚔ Damage = base × (kill_digits × 2)").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("  e.g. 999 kills = 6 digits → ×12 damage").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ CROWN SYNERGY ━━━").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal("👑 Wearing Crown of Greed/Midas:").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal("   → DOUBLES the damage multiplier!").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ EFFECTS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("✓ 5000 durability").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Live kill counter in item tooltip").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ WARNING ━━━").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        lore.add(Component.literal("💀 ON DEATH: Lose ALL coins!").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        lore.add(Component.literal("(Only from crowns and this sword)").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("\"Fortune favors the bold, but").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        lore.add(Component.literal("death claims all.\"").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 5000);
        setCustomItemId(sword, "midas_sword");
        return sword;
    }

    public static boolean isMidasSword(ItemStack stack) {
        return "midas_sword".equals(getCustomItemId(stack));
    }

    public static int getMidasSwordKills(ItemStack sword) {
        if (!isMidasSword(sword)) return 0;
        
        CustomData nbt = sword.get(DataComponents.CUSTOM_DATA);
        if (nbt != null) {
            return nbt.copyTag().getInt("midas_kills").orElse(0);
        }
        return 0;
    }

    public static void setMidasSwordKills(ItemStack sword, int kills) {
        if (!isMidasSword(sword)) return;
        
        CustomData existing = sword.get(DataComponents.CUSTOM_DATA);
        CompoundTag nbt = (existing != null) ? existing.copyTag() : new CompoundTag();
        nbt.putInt("midas_kills", kills);
        sword.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
    }

    public static boolean isEnderPhaseHelmet(ItemStack stack) {
        return hasCustomItemId(stack, "ender_phase_helmet");
    }

    // ═══════════════════════════════════════════════════════════════
    // SUPER COMPACTED ORES - Ultimate compression
    // ═══════════════════════════════════════════════════════════════

    public static ItemStack createSuperCompactedGold() {
        ItemStack compacted = new ItemStack(Items.GOLD_BLOCK);
        compacted.set(DataComponents.CUSTOM_NAME,
                Component.literal("⭐ Super Compacted Gold").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ SUPER COMPACTED BLOCK ◆").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("The ultimate gold storage block").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("Crafted from 9 compacted gold").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Value: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("576 Gold Ingots").withStyle(ChatFormatting.GOLD)));
        compacted.set(DataComponents.LORE, new ItemLore(lore));
        compacted.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(compacted, "super_compacted_gold");
        return compacted;
    }

    public static ItemStack createSuperCompactedIron() {
        ItemStack compacted = new ItemStack(Items.IRON_BLOCK);
        compacted.set(DataComponents.CUSTOM_NAME,
                Component.literal("⭐ Super Compacted Iron").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ SUPER COMPACTED BLOCK ◆").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("The ultimate iron storage block").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("Crafted from 9 compacted iron").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Value: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("576 Iron Ingots").withStyle(ChatFormatting.WHITE)));
        compacted.set(DataComponents.LORE, new ItemLore(lore));
        compacted.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(compacted, "super_compacted_iron");
        return compacted;
    }

    public static ItemStack createSuperCompactedDiamond() {
        ItemStack compacted = new ItemStack(Items.DIAMOND_BLOCK);
        compacted.set(DataComponents.CUSTOM_NAME,
                Component.literal("⭐ Super Compacted Diamond").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ SUPER COMPACTED BLOCK ◆").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("The ultimate diamond storage block").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("Crafted from 9 compacted diamond").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Value: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("576 Diamonds").withStyle(ChatFormatting.AQUA)));
        compacted.set(DataComponents.LORE, new ItemLore(lore));
        compacted.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(compacted, "super_compacted_diamond");
        return compacted;
    }

    public static ItemStack createSuperCompactedEmerald() {
        ItemStack compacted = new ItemStack(Items.EMERALD_BLOCK);
        compacted.set(DataComponents.CUSTOM_NAME,
                Component.literal("⭐ Super Compacted Emerald").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ SUPER COMPACTED BLOCK ◆").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("The ultimate emerald storage block").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("Crafted from 9 compacted emerald").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Value: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("576 Emeralds").withStyle(ChatFormatting.GREEN)));
        compacted.set(DataComponents.LORE, new ItemLore(lore));
        compacted.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(compacted, "super_compacted_emerald");
        return compacted;
    }

    public static ItemStack createSuperCompactedNetherite() {
        ItemStack compacted = new ItemStack(Items.NETHERITE_BLOCK);
        compacted.set(DataComponents.CUSTOM_NAME,
                Component.literal("⭐ Super Compacted Netherite").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ SUPER COMPACTED BLOCK ◆").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("The ultimate netherite storage block").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("Crafted from 9 compacted netherite").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Value: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("576 Netherite Ingots").withStyle(ChatFormatting.DARK_GRAY)));
        compacted.set(DataComponents.LORE, new ItemLore(lore));
        compacted.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(compacted, "super_compacted_netherite");
        return compacted;
    }

    public static ItemStack createCompactedIron() {
        ItemStack compacted = new ItemStack(Items.RAW_IRON);
        compacted.set(DataComponents.CUSTOM_NAME,
                Component.literal("⬛ Compacted Iron").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ COMPACTED ORE ◆").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Highly concentrated raw iron").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("Crafted from 64 raw iron").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Value: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("64 Raw Iron").withStyle(ChatFormatting.WHITE)));
        compacted.set(DataComponents.LORE, new ItemLore(lore));
        compacted.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(compacted, "compacted_iron");
        return compacted;
    }

    public static ItemStack createCompactedGold() {
        ItemStack compacted = new ItemStack(Items.RAW_GOLD);
        compacted.set(DataComponents.CUSTOM_NAME,
                Component.literal("⬛ Compacted Gold").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ COMPACTED ORE ◆").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Highly concentrated raw gold").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("Crafted from 64 raw gold").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Value: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("64 Raw Gold").withStyle(ChatFormatting.YELLOW)));
        compacted.set(DataComponents.LORE, new ItemLore(lore));
        compacted.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(compacted, "compacted_gold");
        return compacted;
    }

    public static ItemStack createCompactedNetherite() {
        ItemStack compacted = new ItemStack(Items.NETHERITE_INGOT);
        compacted.set(DataComponents.CUSTOM_NAME,
                Component.literal("⬛ Compacted Netherite").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ COMPACTED MATERIAL ◆").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("A dense ingot of compressed netherite").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("Crafted from 64 netherite ingots").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Value: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("64 Netherite Ingots").withStyle(ChatFormatting.DARK_GRAY)));
        compacted.set(DataComponents.LORE, new ItemLore(lore));
        compacted.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(compacted, "compacted_netherite");
        return compacted;
    }

    public static ItemStack createCompactedDiamond() {
        ItemStack compacted = new ItemStack(Items.DIAMOND);
        compacted.set(DataComponents.CUSTOM_NAME,
                Component.literal("⬛ Compacted Diamond").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ COMPACTED ORE ◆").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Highly concentrated diamond").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("Crafted from 64 diamonds").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Value: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("64 Diamonds").withStyle(ChatFormatting.AQUA)));
        compacted.set(DataComponents.LORE, new ItemLore(lore));
        compacted.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(compacted, "compacted_diamond");
        return compacted;
    }

    public static ItemStack createCompactedEmerald() {
        ItemStack compacted = new ItemStack(Items.EMERALD);
        compacted.set(DataComponents.CUSTOM_NAME,
                Component.literal("⬛ Compacted Emerald").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ COMPACTED ORE ◆").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Highly concentrated emerald").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("Crafted from 64 emeralds").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Value: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("64 Emeralds").withStyle(ChatFormatting.GREEN)));
        compacted.set(DataComponents.LORE, new ItemLore(lore));
        compacted.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(compacted, "compacted_emerald");
        return compacted;
    }

    public static ItemStack createCompactedCopper() {
        ItemStack compacted = new ItemStack(Items.RAW_COPPER);
        compacted.set(DataComponents.CUSTOM_NAME,
                Component.literal("⬛ Compacted Copper").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ COMPACTED ORE ◆").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Highly concentrated raw copper").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("Crafted from 64 raw copper").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Value: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("64 Raw Copper").withStyle(ChatFormatting.RED)));
        compacted.set(DataComponents.LORE, new ItemLore(lore));
        compacted.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(compacted, "compacted_copper");
        return compacted;
    }

    public static ItemStack createCompactedCoal() {
        ItemStack compacted = new ItemStack(Items.COAL);
        compacted.set(DataComponents.CUSTOM_NAME,
                Component.literal("⬛ Compacted Coal").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ COMPACTED MATERIAL ◆").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("A dense piece of compressed coal").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("Crafted from 64 coal").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Value: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("64 Coal").withStyle(ChatFormatting.DARK_GRAY)));
        compacted.set(DataComponents.LORE, new ItemLore(lore));
        compacted.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(compacted, "compacted_coal");
        return compacted;
    }

    public static ItemStack createCompactedLapis() {
        ItemStack compacted = new ItemStack(Items.LAPIS_LAZULI);
        compacted.set(DataComponents.CUSTOM_NAME,
                Component.literal("⬛ Compacted Lapis Lazuli").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ COMPACTED MATERIAL ◆").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("A dense gem of compressed lapis lazuli").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("Crafted from 64 lapis lazuli").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Value: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("64 Lapis Lazuli").withStyle(ChatFormatting.BLUE)));
        compacted.set(DataComponents.LORE, new ItemLore(lore));
        compacted.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(compacted, "compacted_lapis");
        return compacted;
    }

    public static ItemStack createCompactedRedstone() {
        ItemStack compacted = new ItemStack(Items.REDSTONE);
        compacted.set(DataComponents.CUSTOM_NAME,
                Component.literal("⬛ Compacted Redstone").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ COMPACTED MATERIAL ◆").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("A dense piece of compressed redstone").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("Crafted from 64 redstone").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Value: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("64 Redstone").withStyle(ChatFormatting.RED)));
        compacted.set(DataComponents.LORE, new ItemLore(lore));
        compacted.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(compacted, "compacted_redstone");
        return compacted;
    }

    public static ItemStack createCompactedQuartz() {
        ItemStack compacted = new ItemStack(Items.QUARTZ);
        compacted.set(DataComponents.CUSTOM_NAME,
                Component.literal("⬛ Compacted Quartz").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ COMPACTED MATERIAL ◆").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("A dense gem of compressed quartz").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("Crafted from 64 quartz").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Value: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("64 Quartz").withStyle(ChatFormatting.WHITE)));
        compacted.set(DataComponents.LORE, new ItemLore(lore));
        compacted.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(compacted, "compacted_quartz");
        return compacted;
    }

    public static ItemStack createPureGoldHelmet() {
        ItemStack helmet = new ItemStack(Items.GOLDEN_HELMET);
        helmet.set(DataComponents.CUSTOM_NAME,
                Component.literal("☀ Pure Gold Helmet").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX Research Division | Gold Armor Tier 1]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("❤ Max Health: 2 HP (1 heart)").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🛡 Armour: 2").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Toughness: 0").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ EFFECTS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("✓ Basic gold protection").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Golden appearance").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✗ No special abilities").withStyle(ChatFormatting.RED));
        lore.add(Component.literal(""));
        lore.add(Component.literal("\"The beginning of golden excellence\"").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        helmet.set(DataComponents.LORE, new ItemLore(lore));
        helmet.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(helmet, "pure_gold_helmet");
        return helmet;
    }

    public static ItemStack createPureGoldChestplate() {
        ItemStack chestplate = new ItemStack(Items.GOLDEN_CHESTPLATE);
        chestplate.set(DataComponents.CUSTOM_NAME,
                Component.literal("☀ Pure Gold Chestplate").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX Research Division | Gold Armor Tier 1]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("❤ Max Health: 4 HP (2 hearts)").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🛡 Armour: 6").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Toughness: 0").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ EFFECTS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("✓ Basic gold protection").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Golden appearance").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✗ No special abilities").withStyle(ChatFormatting.RED));
        lore.add(Component.literal(""));
        lore.add(Component.literal("\"The beginning of golden excellence\"").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        chestplate.set(DataComponents.LORE, new ItemLore(lore));
        chestplate.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(chestplate, "pure_gold_chestplate");
        return chestplate;
    }

    public static ItemStack createPureGoldLeggings() {
        ItemStack leggings = new ItemStack(Items.GOLDEN_LEGGINGS);
        leggings.set(DataComponents.CUSTOM_NAME,
                Component.literal("☀ Pure Gold Leggings").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX Research Division | Gold Armor Tier 1]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("❤ Max Health: 2 HP (1 heart)").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🛡 Armour: 5").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Toughness: 0").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ EFFECTS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("✓ Basic gold protection").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Golden appearance").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✗ No special abilities").withStyle(ChatFormatting.RED));
        lore.add(Component.literal(""));
        lore.add(Component.literal("\"The beginning of golden excellence\"").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        leggings.set(DataComponents.LORE, new ItemLore(lore));
        leggings.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(leggings, "pure_gold_leggings");
        return leggings;
    }

    public static ItemStack createPureGoldBoots() {
        ItemStack boots = new ItemStack(Items.GOLDEN_BOOTS);
        boots.set(DataComponents.CUSTOM_NAME,
                Component.literal("☀ Pure Gold Boots").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX Research Division | Gold Armor Tier 1]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("❤ Max Health: 2 HP (1 heart)").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🛡 Armour: 2").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Toughness: 0").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ EFFECTS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("✓ Basic gold protection").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Golden appearance").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✗ No special abilities").withStyle(ChatFormatting.RED));
        lore.add(Component.literal(""));
        lore.add(Component.literal("\"The beginning of golden excellence\"").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        boots.set(DataComponents.LORE, new ItemLore(lore));
        boots.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(boots, "pure_gold_boots");
        return boots;
    }

    public static ItemStack createPolishedGoldHelmet() {
        ItemStack helmet = new ItemStack(Items.GOLDEN_HELMET);
        helmet.set(DataComponents.CUSTOM_NAME,
                Component.literal("✨ Polished Gold Helmet").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX Research Division | Gold Armor Tier 2]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("❤ Max Health: 4 HP (2 hearts)").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🛡 Armour: 3").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Toughness: 1").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ EFFECTS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("✓ Enhanced gold protection").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Polished golden shine").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ +10% movement speed").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        lore.add(Component.literal("\"Refined to perfection\"").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        helmet.set(DataComponents.LORE, new ItemLore(lore));
        helmet.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(helmet, "polished_gold_helmet");
        return helmet;
    }

    public static ItemStack createPolishedGoldChestplate() {
        ItemStack chestplate = new ItemStack(Items.GOLDEN_CHESTPLATE);
        chestplate.set(DataComponents.CUSTOM_NAME,
                Component.literal("✨ Polished Gold Chestplate").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX Research Division | Gold Armor Tier 2]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("❤ Max Health: 6 HP (3 hearts)").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🛡 Armour: 7").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Toughness: 1").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ EFFECTS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("✓ Enhanced gold protection").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Polished golden shine").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ +10% movement speed").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        lore.add(Component.literal("\"Refined to perfection\"").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        chestplate.set(DataComponents.LORE, new ItemLore(lore));
        chestplate.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(chestplate, "polished_gold_chestplate");
        return chestplate;
    }

    public static ItemStack createPolishedGoldLeggings() {
        ItemStack leggings = new ItemStack(Items.GOLDEN_LEGGINGS);
        leggings.set(DataComponents.CUSTOM_NAME,
                Component.literal("✨ Polished Gold Leggings").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX Research Division | Gold Armor Tier 2]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("❤ Max Health: 4 HP (2 hearts)").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🛡 Armour: 6").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Toughness: 1").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ EFFECTS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("✓ Enhanced gold protection").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Polished golden shine").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ +10% movement speed").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        lore.add(Component.literal("\"Refined to perfection\"").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        leggings.set(DataComponents.LORE, new ItemLore(lore));
        leggings.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(leggings, "polished_gold_leggings");
        return leggings;
    }

    public static ItemStack createPolishedGoldBoots() {
        ItemStack boots = new ItemStack(Items.GOLDEN_BOOTS);
        boots.set(DataComponents.CUSTOM_NAME,
                Component.literal("✨ Polished Gold Boots").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX Research Division | Gold Armor Tier 2]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("❤ Max Health: 4 HP (2 hearts)").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🛡 Armour: 3").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Toughness: 1").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ EFFECTS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("✓ Enhanced gold protection").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Polished golden shine").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ +10% movement speed").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        lore.add(Component.literal("\"Refined to perfection\"").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        boots.set(DataComponents.LORE, new ItemLore(lore));
        boots.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(boots, "polished_gold_boots");
        return boots;
    }

    public static ItemStack createShinyGoldHelmet() {
        ItemStack helmet = new ItemStack(Items.GOLDEN_HELMET);
        helmet.set(DataComponents.CUSTOM_NAME,
                Component.literal("⭐ Shiny Gold Helmet").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX Research Division | Gold Armor Tier 3]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("❤ Max Health: 6 HP (3 hearts)").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🛡 Armour: 4").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Toughness: 2").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ EFFECTS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("✓ Superior gold protection").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Brilliant golden glow").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ +20% movement speed").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Night vision when worn").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        lore.add(Component.literal("\"Shines like the sun\"").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        helmet.set(DataComponents.LORE, new ItemLore(lore));
        helmet.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(helmet, "shiny_gold_helmet");
        return helmet;
    }

    public static ItemStack createShinyGoldChestplate() {
        ItemStack chestplate = new ItemStack(Items.GOLDEN_CHESTPLATE);
        chestplate.set(DataComponents.CUSTOM_NAME,
                Component.literal("⭐ Shiny Gold Chestplate").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX Research Division | Gold Armor Tier 3]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("❤ Max Health: 8 HP (4 hearts)").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🛡 Armour: 8").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Toughness: 2").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ EFFECTS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("✓ Superior gold protection").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Brilliant golden glow").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ +20% movement speed").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Night vision when worn").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        lore.add(Component.literal("\"Shines like the sun\"").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        chestplate.set(DataComponents.LORE, new ItemLore(lore));
        chestplate.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(chestplate, "shiny_gold_chestplate");
        return chestplate;
    }

    public static ItemStack createShinyGoldLeggings() {
        ItemStack leggings = new ItemStack(Items.GOLDEN_LEGGINGS);
        leggings.set(DataComponents.CUSTOM_NAME,
                Component.literal("⭐ Shiny Gold Leggings").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX Research Division | Gold Armor Tier 3]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("❤ Max Health: 6 HP (3 hearts)").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🛡 Armour: 7").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Toughness: 2").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ EFFECTS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("✓ Superior gold protection").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Brilliant golden glow").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ +20% movement speed").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Night vision when worn").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        lore.add(Component.literal("\"Shines like the sun\"").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        leggings.set(DataComponents.LORE, new ItemLore(lore));
        leggings.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(leggings, "shiny_gold_leggings");
        return leggings;
    }

    public static ItemStack createShinyGoldBoots() {
        ItemStack boots = new ItemStack(Items.GOLDEN_BOOTS);
        boots.set(DataComponents.CUSTOM_NAME,
                Component.literal("⭐ Shiny Gold Boots").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX Research Division | Gold Armor Tier 3]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("❤ Max Health: 6 HP (3 hearts)").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🛡 Armour: 4").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Toughness: 2").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ EFFECTS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("✓ Superior gold protection").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Brilliant golden glow").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ +20% movement speed").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Night vision when worn").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        lore.add(Component.literal("\"Shines like the sun\"").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        boots.set(DataComponents.LORE, new ItemLore(lore));
        boots.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(boots, "shiny_gold_boots");
        return boots;
    }

    public static ItemStack createGlisteningGoldHelmet() {
        ItemStack helmet = new ItemStack(Items.GOLDEN_HELMET);
        helmet.set(DataComponents.CUSTOM_NAME,
                Component.literal("💫 Glistening Gold Helmet").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX Research Division | Gold Armor Tier 4]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("❤ Max Health: 8 HP (4 hearts)").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🛡 Armour: 5").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Toughness: 3").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ EFFECTS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("✓ Exceptional gold protection").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Glistening golden aura").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ +30% movement speed").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Night vision when worn").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ +2 hearts regeneration bonus").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        lore.add(Component.literal("\"Glistens with divine light\"").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        helmet.set(DataComponents.LORE, new ItemLore(lore));
        helmet.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(helmet, "glistening_gold_helmet");
        return helmet;
    }

    public static ItemStack createGlisteningGoldChestplate() {
        ItemStack chestplate = new ItemStack(Items.GOLDEN_CHESTPLATE);
        chestplate.set(DataComponents.CUSTOM_NAME,
                Component.literal("💫 Glistening Gold Chestplate").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX Research Division | Gold Armor Tier 4]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("❤ Max Health: 10 HP (5 hearts)").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🛡 Armour: 9").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Toughness: 3").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ EFFECTS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("✓ Exceptional gold protection").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Glistening golden aura").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ +30% movement speed").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Night vision when worn").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ +2 hearts regeneration bonus").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        lore.add(Component.literal("\"Glistens with divine light\"").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        chestplate.set(DataComponents.LORE, new ItemLore(lore));
        chestplate.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(chestplate, "glistening_gold_chestplate");
        return chestplate;
    }

    public static ItemStack createGlisteningGoldLeggings() {
        ItemStack leggings = new ItemStack(Items.GOLDEN_LEGGINGS);
        leggings.set(DataComponents.CUSTOM_NAME,
                Component.literal("💫 Glistening Gold Leggings").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX Research Division | Gold Armor Tier 4]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("❤ Max Health: 8 HP (4 hearts)").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🛡 Armour: 8").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Toughness: 3").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ EFFECTS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("✓ Exceptional gold protection").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Glistening golden aura").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ +30% movement speed").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Night vision when worn").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ +2 hearts regeneration bonus").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        lore.add(Component.literal("\"Glistens with divine light\"").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        leggings.set(DataComponents.LORE, new ItemLore(lore));
        leggings.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(leggings, "glistening_gold_leggings");
        return leggings;
    }

    public static ItemStack createGlisteningGoldBoots() {
        ItemStack boots = new ItemStack(Items.GOLDEN_BOOTS);
        boots.set(DataComponents.CUSTOM_NAME,
                Component.literal("💫 Glistening Gold Boots").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX Research Division | Gold Armor Tier 4]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("❤ Max Health: 8 HP (4 hearts)").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🛡 Armour: 5").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Toughness: 3").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ EFFECTS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("✓ Exceptional gold protection").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Glistening golden aura").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ +30% movement speed").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Night vision when worn").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ +2 hearts regeneration bonus").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        lore.add(Component.literal("\"Glistens with divine light\"").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        boots.set(DataComponents.LORE, new ItemLore(lore));
        boots.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(boots, "glistening_gold_boots");
        return boots;
    }

    public static ItemStack createGildedHelmet() {
        ItemStack helmet = new ItemStack(Items.GOLDEN_HELMET);
        helmet.set(DataComponents.CUSTOM_NAME,
                Component.literal("👑 Gilded Helmet").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX Research Division | Gold Armor Tier 5]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("❤ Max Health: 12 HP (6 hearts)").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🛡 Armour: 6").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Toughness: 4").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ EFFECTS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("✓ Ultimate gold protection").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Royal gilded appearance").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ +40% movement speed").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Night vision when worn").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ +4 hearts regeneration bonus").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Fire resistance when worn").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        lore.add(Component.literal("\"Fit for royalty\"").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        helmet.set(DataComponents.LORE, new ItemLore(lore));
        helmet.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(helmet, "gilded_helmet");
        return helmet;
    }

    public static ItemStack createGildedChestplate() {
        ItemStack chestplate = new ItemStack(Items.GOLDEN_CHESTPLATE);
        chestplate.set(DataComponents.CUSTOM_NAME,
                Component.literal("👑 Gilded Chestplate").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX Research Division | Gold Armor Tier 5]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("❤ Max Health: 14 HP (7 hearts)").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🛡 Armour: 10").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Toughness: 4").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ EFFECTS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("✓ Ultimate gold protection").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Royal gilded appearance").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ +40% movement speed").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Night vision when worn").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ +4 hearts regeneration bonus").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Fire resistance when worn").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        lore.add(Component.literal("\"Fit for royalty\"").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        chestplate.set(DataComponents.LORE, new ItemLore(lore));
        chestplate.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(chestplate, "gilded_chestplate");
        return chestplate;
    }

    public static ItemStack createGildedLeggings() {
        ItemStack leggings = new ItemStack(Items.GOLDEN_LEGGINGS);
        leggings.set(DataComponents.CUSTOM_NAME,
                Component.literal("👑 Gilded Leggings").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX Research Division | Gold Armor Tier 5]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("❤ Max Health: 12 HP (6 hearts)").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🛡 Armour: 9").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Toughness: 4").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ EFFECTS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("✓ Ultimate gold protection").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Royal gilded appearance").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ +40% movement speed").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Night vision when worn").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ +4 hearts regeneration bonus").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Fire resistance when worn").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        lore.add(Component.literal("\"Fit for royalty\"").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        leggings.set(DataComponents.LORE, new ItemLore(lore));
        leggings.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(leggings, "gilded_leggings");
        return leggings;
    }

    public static ItemStack createGildedBoots() {
        ItemStack boots = new ItemStack(Items.GOLDEN_BOOTS);
        boots.set(DataComponents.CUSTOM_NAME,
                Component.literal("👑 Gilded Boots").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX Research Division | Gold Armor Tier 5]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("❤ Max Health: 12 HP (6 hearts)").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🛡 Armour: 6").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Toughness: 4").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ EFFECTS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("✓ Ultimate gold protection").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Royal gilded appearance").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ +40% movement speed").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Night vision when worn").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ +4 hearts regeneration bonus").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Fire resistance when worn").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        lore.add(Component.literal("\"Fit for royalty\"").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        boots.set(DataComponents.LORE, new ItemLore(lore));
        boots.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(boots, "gilded_boots");
        return boots;
    }

    public static ItemStack createEnchantedBlackstone() {
        ItemStack blackstone = new ItemStack(Items.BLACKSTONE);
        blackstone.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted Blackstone").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED MATERIAL ◆").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Blackstone infused with ancient magic").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("Used for ultimate armor crafting").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Crafted from 64 blackstone").withStyle(ChatFormatting.GRAY));
        blackstone.set(DataComponents.LORE, new ItemLore(lore));
        blackstone.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(blackstone, "enchanted_blackstone");
        return blackstone;
    }

    public static ItemStack createEnchantedGildedBlackstone() {
        ItemStack blackstone = new ItemStack(Items.BLACKSTONE);
        blackstone.set(DataComponents.CUSTOM_NAME,
                Component.literal("✦ Enchanted Gilded Blackstone").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED MATERIAL ◆").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Blackstone infused with gilded magic").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("Used for ultimate netherite armor crafting").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Crafted from 64 blackstone").withStyle(ChatFormatting.GRAY));
        blackstone.set(DataComponents.LORE, new ItemLore(lore));
        blackstone.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(blackstone, "enchanted_gilded_blackstone");
        return blackstone;
    }

    // ENCHANTED BLOCKS - For shop and crafting
    // ═══════════════════════════════════════════════════════════════

    public static ItemStack createEnchantedCobblestone() {
        ItemStack cobblestone = new ItemStack(Items.COBBLESTONE);
        cobblestone.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted Cobblestone").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED BLOCK ◆").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced cobblestone").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 cobblestone").withStyle(ChatFormatting.GRAY));
        cobblestone.set(DataComponents.LORE, new ItemLore(lore));
        cobblestone.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(cobblestone, "enchanted_cobblestone");
        return cobblestone;
    }

    public static ItemStack createEnchantedStone() {
        ItemStack stone = new ItemStack(Items.STONE);
        stone.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted Stone").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED BLOCK ◆").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced stone").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 stone").withStyle(ChatFormatting.GRAY));
        stone.set(DataComponents.LORE, new ItemLore(lore));
        stone.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(stone, "enchanted_stone");
        return stone;
    }

    public static ItemStack createEnchantedIronBlock() {
        ItemStack ironBlock = new ItemStack(Items.IRON_BLOCK);
        ironBlock.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted Iron Block").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED BLOCK ◆").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced iron block").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 iron blocks").withStyle(ChatFormatting.GRAY));
        ironBlock.set(DataComponents.LORE, new ItemLore(lore));
        ironBlock.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(ironBlock, "enchanted_iron_block");
        return ironBlock;
    }

    public static ItemStack createEnchantedGoldBlock() {
        ItemStack goldBlock = new ItemStack(Items.GOLD_BLOCK);
        goldBlock.set(DataComponents.CUSTOM_NAME,
                Component.literal("✦ Enchanted Gold Block").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED BLOCK ◆").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced gold block").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 gold blocks").withStyle(ChatFormatting.GRAY));
        goldBlock.set(DataComponents.LORE, new ItemLore(lore));
        goldBlock.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(goldBlock, "enchanted_gold_block");
        return goldBlock;
    }

    public static ItemStack createEnchantedDiamondBlock() {
        ItemStack diamondBlock = new ItemStack(Items.DIAMOND_BLOCK);
        diamondBlock.set(DataComponents.CUSTOM_NAME,
                Component.literal("✦ Enchanted Diamond Block").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED BLOCK ◆").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced diamond block").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 diamond blocks").withStyle(ChatFormatting.GRAY));
        diamondBlock.set(DataComponents.LORE, new ItemLore(lore));
        diamondBlock.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(diamondBlock, "enchanted_diamond_block");
        return diamondBlock;
    }

    public static ItemStack createEnchantedOakLog() {
        ItemStack log = new ItemStack(Items.OAK_LOG);
        log.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted Oak Log").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED BLOCK ◆").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced oak log").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 oak logs").withStyle(ChatFormatting.GRAY));
        log.set(DataComponents.LORE, new ItemLore(lore));
        log.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(log, "enchanted_oak_log");
        return log;
    }

    public static ItemStack createEnchantedRose() {
        ItemStack rose = new ItemStack(Items.POPPY);
        rose.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted Rose").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED FLOWER ◆").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced rose").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 roses").withStyle(ChatFormatting.GRAY));
        rose.set(DataComponents.LORE, new ItemLore(lore));
        rose.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(rose, "enchanted_rose");
        return rose;
    }

    public static ItemStack createEnchantedDandelion() {
        ItemStack dandelion = new ItemStack(Items.DANDELION);
        dandelion.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted Dandelion").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED FLOWER ◆").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced dandelion").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 dandelions").withStyle(ChatFormatting.GRAY));
        dandelion.set(DataComponents.LORE, new ItemLore(lore));
        dandelion.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(dandelion, "enchanted_dandelion");
        return dandelion;
    }

    public static ItemStack createEnchantedWheat() {
        ItemStack wheat = new ItemStack(Items.WHEAT);
        wheat.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted Wheat").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED CROP ◆").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced wheat").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 wheat").withStyle(ChatFormatting.GRAY));
        wheat.set(DataComponents.LORE, new ItemLore(lore));
        wheat.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(wheat, "enchanted_wheat");
        return wheat;
    }

    public static ItemStack createEnchantedCarrot() {
        ItemStack carrot = new ItemStack(Items.CARROT);
        carrot.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted Carrot").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED CROP ◆").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced carrot").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 carrots").withStyle(ChatFormatting.GRAY));
        carrot.set(DataComponents.LORE, new ItemLore(lore));
        carrot.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(carrot, "enchanted_carrot");
        return carrot;
    }

    public static ItemStack createEnchantedPotato() {
        ItemStack potato = new ItemStack(Items.POTATO);
        potato.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted Potato").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED CROP ◆").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced potato").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 potatoes").withStyle(ChatFormatting.GRAY));
        potato.set(DataComponents.LORE, new ItemLore(lore));
        potato.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(potato, "enchanted_potato");
        return potato;
    }

    public static ItemStack createEnchantedSugarCane() {
        ItemStack sugarCane = new ItemStack(Items.SUGAR_CANE);
        sugarCane.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted Sugar Cane").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED PLANT ◆").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced sugar cane").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 sugar cane").withStyle(ChatFormatting.GRAY));
        sugarCane.set(DataComponents.LORE, new ItemLore(lore));
        sugarCane.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(sugarCane, "enchanted_sugar_cane");
        return sugarCane;
    }

    // More Enchanted Blocks - Extended variety
    // ═══════════════════════════════════════════════════════════════

    public static ItemStack createEnchantedSand() {
        ItemStack sand = new ItemStack(Items.SAND);
        sand.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted Sand").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED BLOCK ◆").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced sand").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 sand").withStyle(ChatFormatting.GRAY));
        sand.set(DataComponents.LORE, new ItemLore(lore));
        sand.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(sand, "enchanted_sand");
        return sand;
    }

    public static ItemStack createEnchantedRedSand() {
        ItemStack sand = new ItemStack(Items.RED_SAND);
        sand.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted Red Sand").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED BLOCK ◆").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced red sand").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 red sand").withStyle(ChatFormatting.GRAY));
        sand.set(DataComponents.LORE, new ItemLore(lore));
        sand.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(sand, "enchanted_red_sand");
        return sand;
    }

    public static ItemStack createEnchantedGravel() {
        ItemStack gravel = new ItemStack(Items.GRAVEL);
        gravel.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted Gravel").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED BLOCK ◆").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced gravel").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 gravel").withStyle(ChatFormatting.GRAY));
        gravel.set(DataComponents.LORE, new ItemLore(lore));
        gravel.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(gravel, "enchanted_gravel");
        return gravel;
    }

    public static ItemStack createEnchantedNetherrack() {
        ItemStack netherrack = new ItemStack(Items.NETHERRACK);
        netherrack.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted Netherrack").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED BLOCK ◆").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced netherrack").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 netherrack").withStyle(ChatFormatting.GRAY));
        netherrack.set(DataComponents.LORE, new ItemLore(lore));
        netherrack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(netherrack, "enchanted_netherrack");
        return netherrack;
    }

    public static ItemStack createEnchantedEndStone() {
        ItemStack endStone = new ItemStack(Items.END_STONE);
        endStone.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted End Stone").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED BLOCK ◆").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced end stone").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 end stone").withStyle(ChatFormatting.GRAY));
        endStone.set(DataComponents.LORE, new ItemLore(lore));
        endStone.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(endStone, "enchanted_end_stone");
        return endStone;
    }

    public static ItemStack createEnchantedObsidian() {
        ItemStack obsidian = new ItemStack(Items.OBSIDIAN);
        obsidian.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted Obsidian").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED BLOCK ◆").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced obsidian").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 obsidian").withStyle(ChatFormatting.GRAY));
        obsidian.set(DataComponents.LORE, new ItemLore(lore));
        obsidian.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(obsidian, "enchanted_obsidian");
        return obsidian;
    }

    public static ItemStack createEnchantedGlowstone() {
        ItemStack glowstone = new ItemStack(Items.GLOWSTONE);
        glowstone.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted Glowstone").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED BLOCK ◆").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced glowstone").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 glowstone").withStyle(ChatFormatting.GRAY));
        glowstone.set(DataComponents.LORE, new ItemLore(lore));
        glowstone.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(glowstone, "enchanted_glowstone");
        return glowstone;
    }

    public static ItemStack createEnchantedPrismarine() {
        ItemStack prismarine = new ItemStack(Items.PRISMARINE);
        prismarine.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted Prismarine").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED BLOCK ◆").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced prismarine").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 prismarine").withStyle(ChatFormatting.GRAY));
        prismarine.set(DataComponents.LORE, new ItemLore(lore));
        prismarine.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(prismarine, "enchanted_prismarine");
        return prismarine;
    }

    public static ItemStack createEnchantedCopperBlock() {
        ItemStack copper = new ItemStack(Items.COPPER_BLOCK.weathering().unaffected());
        copper.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted Copper Block").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED BLOCK ◆").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced copper").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 copper blocks").withStyle(ChatFormatting.GRAY));
        copper.set(DataComponents.LORE, new ItemLore(lore));
        copper.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(copper, "enchanted_copper_block");
        return copper;
    }

    public static ItemStack createEnchantedEmeraldBlock() {
        ItemStack emerald = new ItemStack(Items.EMERALD_BLOCK);
        emerald.set(DataComponents.CUSTOM_NAME,
                Component.literal("✦ Enchanted Emerald Block").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED BLOCK ◆").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced emeralds").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 emerald blocks").withStyle(ChatFormatting.GRAY));
        emerald.set(DataComponents.LORE, new ItemLore(lore));
        emerald.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(emerald, "enchanted_emerald_block");
        return emerald;
    }

    public static ItemStack createEnchantedLapisBlock() {
        ItemStack lapis = new ItemStack(Items.LAPIS_BLOCK);
        lapis.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted Lapis Block").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED BLOCK ◆").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced lapis").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 lapis blocks").withStyle(ChatFormatting.GRAY));
        lapis.set(DataComponents.LORE, new ItemLore(lore));
        lapis.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(lapis, "enchanted_lapis_block");
        return lapis;
    }

    public static ItemStack createEnchantedCoalBlock() {
        ItemStack coal = new ItemStack(Items.COAL_BLOCK);
        coal.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted Coal Block").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED BLOCK ◆").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced coal").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 coal blocks").withStyle(ChatFormatting.GRAY));
        coal.set(DataComponents.LORE, new ItemLore(lore));
        coal.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(coal, "enchanted_coal_block");
        return coal;
    }

    public static ItemStack createEnchantedNetheriteBlock() {
        ItemStack netherite = new ItemStack(Items.NETHERITE_BLOCK);
        netherite.set(DataComponents.CUSTOM_NAME,
                Component.literal("✦ Enchanted Netherite Block").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED BLOCK ◆").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced netherite").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 netherite blocks").withStyle(ChatFormatting.GRAY));
        netherite.set(DataComponents.LORE, new ItemLore(lore));
        netherite.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(netherite, "enchanted_netherite_block");
        return netherite;
    }

    public static ItemStack createEnchantedQuartzBlock() {
        ItemStack quartz = new ItemStack(Items.QUARTZ_BLOCK);
        quartz.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted Quartz Block").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED BLOCK ◆").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced quartz").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 quartz blocks").withStyle(ChatFormatting.GRAY));
        quartz.set(DataComponents.LORE, new ItemLore(lore));
        quartz.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(quartz, "enchanted_quartz_block");
        return quartz;
    }

    public static ItemStack createEnchantedRedstoneBlock() {
        ItemStack redstone = new ItemStack(Items.REDSTONE_BLOCK);
        redstone.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted Redstone Block").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED BLOCK ◆").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced redstone").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 redstone blocks").withStyle(ChatFormatting.GRAY));
        redstone.set(DataComponents.LORE, new ItemLore(lore));
        redstone.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(redstone, "enchanted_redstone_block");
        return redstone;
    }

    public static ItemStack createEnchantedBasalt() {
        ItemStack basalt = new ItemStack(Items.BASALT);
        basalt.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted Basalt").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED BLOCK ◆").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced basalt").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 basalt").withStyle(ChatFormatting.GRAY));
        basalt.set(DataComponents.LORE, new ItemLore(lore));
        basalt.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(basalt, "enchanted_basalt");
        return basalt;
    }

    public static ItemStack createEnchantedDeepslate() {
        ItemStack deepslate = new ItemStack(Items.DEEPSLATE);
        deepslate.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted Deepslate").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED BLOCK ◆").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced deepslate").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 deepslate").withStyle(ChatFormatting.GRAY));
        deepslate.set(DataComponents.LORE, new ItemLore(lore));
        deepslate.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(deepslate, "enchanted_deepslate");
        return deepslate;
    }

    public static ItemStack createEnchantedCobbledDeepslate() {
        ItemStack deepslate = new ItemStack(Items.COBBLED_DEEPSLATE);
        deepslate.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted Cobbled Deepslate").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED BLOCK ◆").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced cobbled deepslate").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 cobbled deepslate").withStyle(ChatFormatting.GRAY));
        deepslate.set(DataComponents.LORE, new ItemLore(lore));
        deepslate.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(deepslate, "enchanted_cobbled_deepslate");
        return deepslate;
    }

    public static ItemStack createEnchantedTuff() {
        ItemStack tuff = new ItemStack(Items.TUFF);
        tuff.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted Tuff").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED BLOCK ◆").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced tuff").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 tuff").withStyle(ChatFormatting.GRAY));
        tuff.set(DataComponents.LORE, new ItemLore(lore));
        tuff.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(tuff, "enchanted_tuff");
        return tuff;
    }

    public static ItemStack createEnchantedCalcite() {
        ItemStack calcite = new ItemStack(Items.CALCITE);
        calcite.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted Calcite").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED BLOCK ◆").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced calcite").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 calcite").withStyle(ChatFormatting.GRAY));
        calcite.set(DataComponents.LORE, new ItemLore(lore));
        calcite.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(calcite, "enchanted_calcite");
        return calcite;
    }

    public static ItemStack createEnchantedAmethystBlock() {
        ItemStack amethyst = new ItemStack(Items.AMETHYST_BLOCK);
        amethyst.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted Amethyst Block").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED BLOCK ◆").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced amethyst").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 amethyst blocks").withStyle(ChatFormatting.GRAY));
        amethyst.set(DataComponents.LORE, new ItemLore(lore));
        amethyst.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(amethyst, "enchanted_amethyst_block");
        return amethyst;
    }

    public static ItemStack createEnchantedGlass() {
        ItemStack glass = new ItemStack(Items.GLASS);
        glass.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted Glass").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED BLOCK ◆").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced glass").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 glass").withStyle(ChatFormatting.GRAY));
        glass.set(DataComponents.LORE, new ItemLore(lore));
        glass.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(glass, "enchanted_glass");
        return glass;
    }

    public static ItemStack createEnchantedIce() {
        ItemStack ice = new ItemStack(Items.ICE);
        ice.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted Ice").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED BLOCK ◆").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced ice").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 ice").withStyle(ChatFormatting.GRAY));
        ice.set(DataComponents.LORE, new ItemLore(lore));
        ice.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(ice, "enchanted_ice");
        return ice;
    }

    public static ItemStack createEnchantedPackedIce() {
        ItemStack ice = new ItemStack(Items.PACKED_ICE);
        ice.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted Packed Ice").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED BLOCK ◆").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced packed ice").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 packed ice").withStyle(ChatFormatting.GRAY));
        ice.set(DataComponents.LORE, new ItemLore(lore));
        ice.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(ice, "enchanted_packed_ice");
        return ice;
    }

    public static ItemStack createEnchantedBlueIce() {
        ItemStack ice = new ItemStack(Items.BLUE_ICE);
        ice.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted Blue Ice").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED BLOCK ◆").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced blue ice").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 blue ice").withStyle(ChatFormatting.GRAY));
        ice.set(DataComponents.LORE, new ItemLore(lore));
        ice.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(ice, "enchanted_blue_ice");
        return ice;
    }

    public static ItemStack createEnchantedTerracotta() {
        ItemStack terracotta = new ItemStack(Items.TERRACOTTA);
        terracotta.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted Terracotta").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED BLOCK ◆").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced terracotta").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 terracotta").withStyle(ChatFormatting.GRAY));
        terracotta.set(DataComponents.LORE, new ItemLore(lore));
        terracotta.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(terracotta, "enchanted_terracotta");
        return terracotta;
    }

    public static ItemStack createEnchantedClay() {
        ItemStack clay = new ItemStack(Items.CLAY);
        clay.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted Clay").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED BLOCK ◆").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced clay").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 clay").withStyle(ChatFormatting.GRAY));
        clay.set(DataComponents.LORE, new ItemLore(lore));
        clay.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(clay, "enchanted_clay");
        return clay;
    }

    public static ItemStack createEnchantedBrick() {
        ItemStack brick = new ItemStack(Items.BRICKS);
        brick.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted Bricks").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED BLOCK ◆").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced bricks").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 bricks").withStyle(ChatFormatting.GRAY));
        brick.set(DataComponents.LORE, new ItemLore(lore));
        brick.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(brick, "enchanted_brick");
        return brick;
    }

    public static ItemStack createEnchantedNetherBrick() {
        ItemStack brick = new ItemStack(Items.NETHER_BRICKS);
        brick.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted Nether Bricks").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED BLOCK ◆").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced nether bricks").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 nether bricks").withStyle(ChatFormatting.GRAY));
        brick.set(DataComponents.LORE, new ItemLore(lore));
        brick.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(brick, "enchanted_nether_brick");
        return brick;
    }

    public static ItemStack createEnchantedPurpurBlock() {
        ItemStack purpur = new ItemStack(Items.PURPUR_BLOCK);
        purpur.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted Purpur Block").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED BLOCK ◆").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced purpur").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 purpur blocks").withStyle(ChatFormatting.GRAY));
        purpur.set(DataComponents.LORE, new ItemLore(lore));
        purpur.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(purpur, "enchanted_purpur_block");
        return purpur;
    }

    public static ItemStack createEnchantedSeaLantern() {
        ItemStack lantern = new ItemStack(Items.SEA_LANTERN);
        lantern.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted Sea Lantern").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED BLOCK ◆").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced sea lantern").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 sea lanterns").withStyle(ChatFormatting.GRAY));
        lantern.set(DataComponents.LORE, new ItemLore(lore));
        lantern.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(lantern, "enchanted_sea_lantern");
        return lantern;
    }

    public static ItemStack createEnchantedShroomlight() {
        ItemStack shroomlight = new ItemStack(Items.SHROOMLIGHT);
        shroomlight.set(DataComponents.CUSTOM_NAME,
                Component.literal("✧ Enchanted Shroomlight").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ENCHANTED BLOCK ◆").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        lore.add(Component.literal("Magically enhanced shroomlight").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("Crafted from 64 shroomlights").withStyle(ChatFormatting.GRAY));
        shroomlight.set(DataComponents.LORE, new ItemLore(lore));
        shroomlight.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(shroomlight, "enchanted_shroomlight");
        return shroomlight;
    }

    // GILDED NETHERITE ARMOR - Ultimate protection
    // ═══════════════════════════════════════════════════════════════

    public static ItemStack createGildedNetheriteHelmet() {
        ItemStack helmet = new ItemStack(Items.NETHERITE_HELMET);
        helmet.set(DataComponents.CUSTOM_NAME,
                Component.literal("⚜ Gilded Netherite Helmet").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX Research Division | Ultimate Armor Tier 6]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_PURPLE));
        lore.add(Component.literal("❤ Max Health: 16 HP (8 hearts)").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🛡 Armour: 8").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Toughness: 5").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ EFFECTS ━━━").withStyle(ChatFormatting.DARK_PURPLE));
        lore.add(Component.literal("✓ Ultimate netherite protection").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Gilded netherite aura").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ +50% movement speed").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Night vision when worn").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ +6 hearts regeneration bonus").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Fire resistance when worn").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Knockback resistance +20%").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        lore.add(Component.literal("\"Beyond legendary\"").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        helmet.set(DataComponents.LORE, new ItemLore(lore));
        helmet.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(helmet, "gilded_netherite_helmet");
        return helmet;
    }

    public static ItemStack createGildedNetheriteChestplate() {
        ItemStack chestplate = new ItemStack(Items.NETHERITE_CHESTPLATE);
        chestplate.set(DataComponents.CUSTOM_NAME,
                Component.literal("⚜ Gilded Netherite Chestplate").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX Research Division | Ultimate Armor Tier 6]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_PURPLE));
        lore.add(Component.literal("❤ Max Health: 20 HP (10 hearts)").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🛡 Armour: 12").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Toughness: 5").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ EFFECTS ━━━").withStyle(ChatFormatting.DARK_PURPLE));
        lore.add(Component.literal("✓ Ultimate netherite protection").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Gilded netherite aura").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ +50% movement speed").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Night vision when worn").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ +6 hearts regeneration bonus").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Fire resistance when worn").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Knockback resistance +20%").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        lore.add(Component.literal("\"Beyond legendary\"").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        chestplate.set(DataComponents.LORE, new ItemLore(lore));
        chestplate.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(chestplate, "gilded_netherite_chestplate");
        return chestplate;
    }

    public static ItemStack createGildedNetheriteLeggings() {
        ItemStack leggings = new ItemStack(Items.NETHERITE_LEGGINGS);
        leggings.set(DataComponents.CUSTOM_NAME,
                Component.literal("⚜ Gilded Netherite Leggings").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX Research Division | Ultimate Armor Tier 6]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_PURPLE));
        lore.add(Component.literal("❤ Max Health: 16 HP (8 hearts)").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🛡 Armour: 11").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Toughness: 5").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ EFFECTS ━━━").withStyle(ChatFormatting.DARK_PURPLE));
        lore.add(Component.literal("✓ Ultimate netherite protection").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Gilded netherite aura").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ +50% movement speed").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Night vision when worn").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ +6 hearts regeneration bonus").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Fire resistance when worn").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Knockback resistance +20%").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        lore.add(Component.literal("\"Beyond legendary\"").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        leggings.set(DataComponents.LORE, new ItemLore(lore));
        leggings.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(leggings, "gilded_netherite_leggings");
        return leggings;
    }

    public static ItemStack createGildedNetheriteBoots() {
        ItemStack boots = new ItemStack(Items.NETHERITE_BOOTS);
        boots.set(DataComponents.CUSTOM_NAME,
                Component.literal("⚜ Gilded Netherite Boots").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[MFLUX Research Division | Ultimate Armor Tier 6]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_PURPLE));
        lore.add(Component.literal("❤ Max Health: 16 HP (8 hearts)").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🛡 Armour: 8").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Toughness: 5").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ EFFECTS ━━━").withStyle(ChatFormatting.DARK_PURPLE));
        lore.add(Component.literal("✓ Ultimate netherite protection").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Gilded netherite aura").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ +50% movement speed").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Night vision when worn").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ +6 hearts regeneration bonus").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Fire resistance when worn").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("✓ Knockback resistance +20%").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        lore.add(Component.literal("\"Beyond legendary\"").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        boots.set(DataComponents.LORE, new ItemLore(lore));
        boots.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        setCustomItemId(boots, "gilded_netherite_boots");
        return boots;
    }

    // ============================================================
    // WEAPON ATTRIBUTE SYSTEM
    // ============================================================

    public static boolean isWeaponAttributeToken(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.copyTag().contains(WEAPON_ATTRIBUTE_TOKEN_KEY);
    }

    public static WeaponAttribute getWeaponAttributeFromToken(ItemStack stack) {
        if (!isWeaponAttributeToken(stack)) return null;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        String id = data.copyTag().getString(WEAPON_ATTRIBUTE_TOKEN_KEY).orElse("");
        return WeaponAttribute.fromId(id);
    }

    public static WeaponAttribute getAppliedWeaponAttribute(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        String id = data.copyTag().getString(WEAPON_ATTRIBUTE_KEY).orElse("");
        return WeaponAttribute.fromId(id);
    }

    public static void applyWeaponAttribute(ItemStack stack, WeaponAttribute attribute) {
        CustomData existing = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag nbt = (existing != null) ? existing.copyTag() : new CompoundTag();
        nbt.putString(WEAPON_ATTRIBUTE_KEY, attribute.id);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        
        // Add enhanced lore to the weapon
        List<Component> existingLore = new ArrayList<>();
        ItemLore currentLore = stack.get(DataComponents.LORE);
        if (currentLore != null) {
            existingLore.addAll(currentLore.lines());
        }
        
        // Add attribute lore at the beginning
        List<Component> newLore = new ArrayList<>();
        newLore.add(Component.literal(""));
        newLore.add(Component.literal("⚔ " + attribute.displayName + " ATTRIBUTE").withStyle(attribute.color, ChatFormatting.BOLD));
        newLore.add(Component.literal(""));
        
        // Add thematic description based on attribute type
        switch (attribute) {
            case SHARP -> {
                newLore.add(Component.literal("✦ Enhanced cutting power").withStyle(ChatFormatting.YELLOW));
                newLore.add(Component.literal("✦ Critical strikes more frequent").withStyle(ChatFormatting.YELLOW));
                break;
            }
            case SWIFT -> {
                newLore.add(Component.literal("✦ Lightning-fast attacks").withStyle(ChatFormatting.YELLOW));
                newLore.add(Component.literal("✦ No cooldown between strikes").withStyle(ChatFormatting.YELLOW));
                break;
            }
            case VAMPIRIC -> {
                newLore.add(Component.literal("✦ Life stolen from victims").withStyle(ChatFormatting.YELLOW));
                newLore.add(Component.literal("✦ Dark power flows through blade").withStyle(ChatFormatting.YELLOW));
                break;
            }
            case FROSTBITE -> {
                newLore.add(Component.literal("✦ Cold bites at enemies").withStyle(ChatFormatting.YELLOW));
                newLore.add(Component.literal("✦ Ice forms on impact").withStyle(ChatFormatting.YELLOW));
                break;
            }
            case THUNDER -> {
                newLore.add(Component.literal("✦ Lightning crackles in blade").withStyle(ChatFormatting.YELLOW));
                newLore.add(Component.literal("✦ Storm power unleashed").withStyle(ChatFormatting.YELLOW));
                break;
            }
            case POISON -> {
                newLore.add(Component.literal("✦ Venom drips from edge").withStyle(ChatFormatting.YELLOW));
                newLore.add(Component.literal("✦ Toxins infect targets").withStyle(ChatFormatting.YELLOW));
                break;
            }
            case EXPLOSIVE -> {
                newLore.add(Component.literal("✦ Blast radius on impact").withStyle(ChatFormatting.YELLOW));
                newLore.add(Component.literal("✦ Explosive force applied").withStyle(ChatFormatting.YELLOW));
                break;
            }
            case HOLY -> {
                newLore.add(Component.literal("✦ Sacred light radiates").withStyle(ChatFormatting.YELLOW));
                newLore.add(Component.literal("✦ Unholy enemies burn").withStyle(ChatFormatting.YELLOW));
                break;
            }
            case SHADOW -> {
                newLore.add(Component.literal("✦ Darkness cloaks wielder").withStyle(ChatFormatting.YELLOW));
                newLore.add(Component.literal("✦ Shadows conceal movements").withStyle(ChatFormatting.YELLOW));
                break;
            }
            case WIND -> {
                newLore.add(Component.literal("✦ Gale force in strikes").withStyle(ChatFormatting.YELLOW));
                newLore.add(Component.literal("✦ Air currents obey blade").withStyle(ChatFormatting.YELLOW));
                break;
            }
        }
        
        newLore.add(Component.literal(""));
        newLore.add(Component.literal("━━━ ORIGINAL STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        
        // Add existing lore after the attribute section
        newLore.addAll(existingLore);
        stack.set(DataComponents.LORE, new ItemLore(newLore));
    }

    public static ItemStack createWeaponAttributeToken(WeaponAttribute attr) {
        ItemStack token = new ItemStack(Items.PAPER);
        token.set(DataComponents.CUSTOM_NAME,
                Component.literal("⚔ " + attr.displayName + " Weapon Token ⚔")
                        .withStyle(attr.color, ChatFormatting.BOLD));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("⚔ LEGENDARY WEAPON ATTRIBUTE").withStyle(attr.color, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        
        // Add thematic lore based on attribute type
        switch (attr) {
            case SHARP -> {
                lore.add(Component.literal("A razor-sharp essence distilled").withStyle(ChatFormatting.GRAY));
                lore.add(Component.literal("from countless battles.").withStyle(ChatFormatting.DARK_GRAY));
                lore.add(Component.literal(""));
                lore.add(Component.literal("⚔ ABILITIES:").withStyle(attr.color, ChatFormatting.BOLD));
                lore.add(Component.literal("• +15% Attack Damage").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• Critical hit chance increased").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• Armor penetration").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal(""));
                lore.add(Component.literal("\"Cut through all opposition").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
                lore.add(Component.literal("with surgical precision.\"").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
            }
            case SWIFT -> {
                lore.add(Component.literal("The essence of lightning speed").withStyle(ChatFormatting.GRAY));
                lore.add(Component.literal("captured in crystalline form.").withStyle(ChatFormatting.DARK_GRAY));
                lore.add(Component.literal(""));
                lore.add(Component.literal("⚔ ABILITIES:").withStyle(attr.color, ChatFormatting.BOLD));
                lore.add(Component.literal("• +25% Attack Speed").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• No attack cooldown").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• Quick draw bonus").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal(""));
                lore.add(Component.literal("\"Speed is the essence of war.\"").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC));
            }
            case VAMPIRIC -> {
                lore.add(Component.literal("A dark essence pulsing with").withStyle(ChatFormatting.GRAY));
                lore.add(Component.literal("stolen life force.").withStyle(ChatFormatting.DARK_GRAY));
                lore.add(Component.literal(""));
                lore.add(Component.literal("⚔ ABILITIES:").withStyle(attr.color, ChatFormatting.BOLD));
                lore.add(Component.literal("• Lifesteal on hit (+10% max HP)").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• Heal on kill").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• Blood rage on low HP").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal(""));
                lore.add(Component.literal("\"The blood is the life!\"").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
            }
            case FROSTBITE -> {
                lore.add(Component.literal("A fragment of eternal winter").withStyle(ChatFormatting.GRAY));
                lore.add(Component.literal("from the frozen peaks.").withStyle(ChatFormatting.DARK_GRAY));
                lore.add(Component.literal(""));
                lore.add(Component.literal("⚔ ABILITIES:").withStyle(attr.color, ChatFormatting.BOLD));
                lore.add(Component.literal("• Slows enemies on hit").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• Freezes water on contact").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• Frost aura damage").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal(""));
                lore.add(Component.literal("\"Winter is coming.\"").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
            }
            case THUNDER -> {
                lore.add(Component.literal("The fury of storms captured").withStyle(ChatFormatting.GRAY));
                lore.add(Component.literal("in a single token.").withStyle(ChatFormatting.DARK_GRAY));
                lore.add(Component.literal(""));
                lore.add(Component.literal("⚔ ABILITIES:").withStyle(attr.color, ChatFormatting.BOLD));
                lore.add(Component.literal("• Lightning strikes on hit").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• Chain damage to nearby foes").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• Storm call on critical").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal(""));
                lore.add(Component.literal("\"Let the storm rage on!\"").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
            }
            case POISON -> {
                lore.add(Component.literal("A toxic essence brewed from").withStyle(ChatFormatting.GRAY));
                lore.add(Component.literal("the deadliest venoms.").withStyle(ChatFormatting.DARK_GRAY));
                lore.add(Component.literal(""));
                lore.add(Component.literal("⚔ ABILITIES:").withStyle(attr.color, ChatFormatting.BOLD));
                lore.add(Component.literal("• Poisons enemies on hit").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• Toxic cloud on kill").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• Wither on critical hits").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal(""));
                lore.add(Component.literal("\"One touch is all it takes.\"").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC));
            }
            case EXPLOSIVE -> {
                lore.add(Component.literal("Unstable explosive power").withStyle(ChatFormatting.GRAY));
                lore.add(Component.literal("condensed into a token.").withStyle(ChatFormatting.DARK_GRAY));
                lore.add(Component.literal(""));
                lore.add(Component.literal("⚔ ABILITIES:").withStyle(attr.color, ChatFormatting.BOLD));
                lore.add(Component.literal("• Area damage on hit").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• Knockback explosion").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• Chain reaction on kill").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal(""));
                lore.add(Component.literal("\"Boom goes the dynamite!\"").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
            }
            case HOLY -> {
                lore.add(Component.literal("Divine light captured from").withStyle(ChatFormatting.GRAY));
                lore.add(Component.literal("the highest heavens.").withStyle(ChatFormatting.DARK_GRAY));
                lore.add(Component.literal(""));
                lore.add(Component.literal("⚔ ABILITIES:").withStyle(attr.color, ChatFormatting.BOLD));
                lore.add(Component.literal("• Extra damage to undead").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• Smite chance on hit").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• Holy aura protection").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal(""));
                lore.add(Component.literal("\"Let the light purge darkness.\"").withStyle(ChatFormatting.WHITE, ChatFormatting.ITALIC));
            }
            case SHADOW -> {
                lore.add(Component.literal("Pure darkness extracted from").withStyle(ChatFormatting.GRAY));
                lore.add(Component.literal("the void between worlds.").withStyle(ChatFormatting.DARK_GRAY));
                lore.add(Component.literal(""));
                lore.add(Component.literal("⚔ ABILITIES:").withStyle(attr.color, ChatFormatting.BOLD));
                lore.add(Component.literal("• Invisibility on kill").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• Stealth movement bonus").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• Shadow step teleport").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal(""));
                lore.add(Component.literal("\"From shadows, I strike.\"").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            }
            case WIND -> {
                lore.add(Component.literal("The essence of gale winds").withStyle(ChatFormatting.GRAY));
                lore.add(Component.literal("from mountain peaks.").withStyle(ChatFormatting.DARK_GRAY));
                lore.add(Component.literal(""));
                lore.add(Component.literal("⚔ ABILITIES:").withStyle(attr.color, ChatFormatting.BOLD));
                lore.add(Component.literal("• Enhanced knockback").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• Flight boost ability").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal("• Wind burst on critical").withStyle(ChatFormatting.YELLOW));
                lore.add(Component.literal(""));
                lore.add(Component.literal("\"Ride the wind.\"").withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC));
            }
        }
        
        lore.add(Component.literal(""));
        lore.add(Component.literal("💰 XP Cost: " + attr.xpCost).withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("⚠ DEBUFF:").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        lore.add(Component.literal(attr.debuffSummary).withStyle(ChatFormatting.DARK_RED));
        
        token.set(DataComponents.LORE, new ItemLore(lore));
        token.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        
        // Store attribute ID in NBT
        CompoundTag nbt = new CompoundTag();
        nbt.putString(WEAPON_ATTRIBUTE_TOKEN_KEY, attr.id);
        token.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        
        setCustomItemId(token, "weapon_attribute_token_" + attr.id);
        return token;
    }

    // ═══════════════════════════════════════════════════════════════
    // CUSTOM VANILLA-LIKE WEAPONS
    // ═══════════════════════════════════════════════════════════════

    public static ItemStack createReinforcedIronSword() {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponents.CUSTOM_NAME,
                Component.literal("⚔ Reinforced Iron Sword").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("⚔ Damage: 7 (Iron +1)").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 500").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Reinforced with extra iron plating").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 500);
        setCustomItemId(sword, "reinforced_iron_sword");
        return sword;
    }

    public static ItemStack createHardenedDiamondSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponents.CUSTOM_NAME,
                Component.literal("⚔ Hardened Diamond Sword").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal("⚔ Damage: 8 (Diamond +1)").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 2000").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Compressed diamond edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 2000);
        setCustomItemId(sword, "hardened_diamond_sword");
        return sword;
    }

    public static ItemStack createVoidTouchedSword() {
        ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME,
                Component.literal("⚔ Void-Touched Blade").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_PURPLE));
        lore.add(Component.literal("⚔ Damage: 9 (Netherite +1)").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 2500").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Infused with void essence").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 2500);
        setCustomItemId(sword, "void_touched_sword");
        return sword;
    }

    public static ItemStack createSharpenedGoldSword() {
        ItemStack sword = new ItemStack(Items.GOLDEN_SWORD);
        sword.set(DataComponents.CUSTOM_NAME,
                Component.literal("⚔ Sharpened Gold Sword").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Damage: 5 (Gold +1)").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 100").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Enchantability: High").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Finely sharpened edge").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 100);
        setCustomItemId(sword, "sharpened_gold_sword");
        return sword;
    }

    public static ItemStack createHeavyStoneSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponents.CUSTOM_NAME,
                Component.literal("⚔ Heavy Stone Sword").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("⚔ Damage: 6 (Stone +1)").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 200").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("⚔ Attack Speed: Slower").withStyle(ChatFormatting.RED));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Dense stone composition").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 200);
        setCustomItemId(sword, "heavy_stone_sword");
        return sword;
    }

    public static ItemStack createSwiftWoodenSword() {
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        sword.set(DataComponents.CUSTOM_NAME,
                Component.literal("⚔ Swift Wooden Sword").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("⚔ Damage: 5 (Wood +1)").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("⚔ Durability: 100").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Lightweight ash wood").withStyle(ChatFormatting.DARK_GRAY));
        sword.set(DataComponents.LORE, new ItemLore(lore));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponents.MAX_DAMAGE, 100);
        setCustomItemId(sword, "swift_wooden_sword");
        return sword;
    }
}
