package com.political;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.entity.attribute.EntityAttribute;

import java.util.List;
import com.political.ArmourAttribute;

public class SlayerArmorHandler {

    private static final Identifier SLAYER_ARMOR_ID = Identifier.of("political", "slayer_armor");
    private static final Identifier SLAYER_TOUGHNESS_ID = Identifier.of("political", "slayer_toughness");
    private static final Identifier SLAYER_HEALTH_ID = Identifier.of("political", "slayer_health");
    private static final Identifier SLAYER_KB_ID = Identifier.of("political", "slayer_knockback");
    private static final Identifier SLAYER_SPEED_ID = Identifier.of("political", "slayer_speed");

    /**
     * Calculate total boss-specific damage reduction from standard T1/T2 armor sets only.
     * Uses per-piece ArmorStats values weighted by slot (Helmet 20%, Chest 40%, Legs 25%, Boots 15%)
     * to exactly match what the per-piece lore displays.
     *
     * NOTE: Special legendary pieces (Berserker Helmet, Spider Leggings, Slime Boots, Warden
     * Chestplate) are intentionally excluded here — they are handled directly by BountyDefenseMixin.
     */
    public static float getBossDamageReduction(ServerPlayerEntity player, SlayerManager.SlayerType bossType) {
        float reduction = 0.0f;
        int t1MatchingPieces = 0;
        int t2MatchingPieces = 0;

        // Weights must match SlayerItems.getPieceBossMultiplier()
        float[] weights = {0.20f, 0.40f, 0.25f, 0.15f}; // HEAD, CHEST, LEGS, FEET
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

        for (int i = 0; i < slots.length; i++) {
            ItemStack stack = player.getEquippedStack(slots[i]);

            // Skip special legendary pieces — handled separately in BountyDefenseMixin
            if (SlayerItems.isZombieBerserkerHelmet(stack) || SlayerItems.isSpiderLeggings(stack)
                    || SlayerItems.isSlimeBoots(stack) || SlayerItems.isWardenChestplate(stack)
                    || SlayerItems.isEnderPhaseHelmet(stack)) {
                continue;
            }

            Text customName = stack.get(DataComponentTypes.CUSTOM_NAME);
            if (customName == null) continue;
            String name = customName.getString();

            boolean isT2 = name.contains(" II");
            if (!isMatchingSlayerArmor(name, bossType)) continue;

            int tier = isT2 ? 2 : 1;
            SlayerItems.ArmorStats stats = SlayerItems.getArmorStats(bossType, tier);
            reduction += (float) stats.bossReduction * weights[i];

            if (isT2) t2MatchingPieces++;
            else t1MatchingPieces++;
        }

        // Full set bonus — only when ALL 4 pieces are the same tier
        if (t1MatchingPieces == 4) {
            reduction += 0.06f; // T1 full set: +6%
        }
        if (t2MatchingPieces == 4) {
            reduction += 0.10f; // T2 full set: +10%
        }
        // No mixed/hybrid set bonus — mixed sets do not grant a full set bonus

        return reduction;
    }

    /**
     * Calculate the universal (all-boss) damage reduction from standard T1/T2 armor sets only.
     * Each armor piece contributes its allBossReduction weighted by slot.
     * Special legendary pieces are excluded (handled by BountyDefenseMixin).
     */
    public static float getUniversalBossReduction(ServerPlayerEntity player) {
        float reduction = 0.0f;

        float[] weights = {0.20f, 0.40f, 0.25f, 0.15f}; // HEAD, CHEST, LEGS, FEET
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

        for (int i = 0; i < slots.length; i++) {
            ItemStack stack = player.getEquippedStack(slots[i]);

            // Skip special legendary pieces — handled separately in BountyDefenseMixin
            if (SlayerItems.isZombieBerserkerHelmet(stack) || SlayerItems.isSpiderLeggings(stack)
                    || SlayerItems.isSlimeBoots(stack) || SlayerItems.isWardenChestplate(stack)
                    || SlayerItems.isEnderPhaseHelmet(stack)) {
                continue;
            }

            Text customName = stack.get(DataComponentTypes.CUSTOM_NAME);
            if (customName == null) continue;
            String name = customName.getString();

            // Match ANY slayer armor type for universal reduction
            SlayerManager.SlayerType armorType = null;
            for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
                if (isMatchingSlayerArmor(name, type)) {
                    armorType = type;
                    break;
                }
            }
            if (armorType == null) continue;

            boolean isT2 = name.contains(" II");
            int tier = isT2 ? 2 : 1;
            SlayerItems.ArmorStats stats = SlayerItems.getArmorStats(armorType, tier);
            reduction += (float) stats.allBossReduction * weights[i];
        }

        return reduction;
    }

    private static boolean isMatchingSlayerArmor(String name, SlayerManager.SlayerType type) {
        return switch (type) {
            case ZOMBIE -> name.contains("Berserker") || name.contains("Undying") || name.contains("Outlaw");
            case SPIDER -> name.contains("Venomous") || name.contains("Spider") || name.contains("Bandit");
            case SKELETON -> name.contains("Bone") || name.contains("Desperado");
            case SLIME -> name.contains("Slime") || name.contains("Gelatinous") || name.contains("Rustler");
            // "Voidwalker" should NOT be counted as standard Enderman set armor
            case ENDERMAN -> (name.contains("Void") && !name.contains("Voidwalker")) || name.contains("Phantom");
            case IRON_GOLEM -> name.contains("Sculk") || name.contains("Warden") || name.contains("Terror");
            // "Crown of Greed" is NOT piglin set armor either
            case PIGLIN -> name.contains("Gilded") || name.contains("Ravager");
        };
    }

    /**
     * Apply armor attribute modifiers based on equipped armor
     * Call this every tick
     */
    public static void applyCustomArmorAttributes(ServerPlayerEntity player) {
        double bonusArmor = 0;
        double bonusToughness = 0;
        double bonusHealth = 0;
        double bonusKnockbackResist = 0;
        double bonusSpeed = 0;

        ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);
        ItemStack chestplate = player.getEquippedStack(EquipmentSlot.CHEST);
        ItemStack leggings = player.getEquippedStack(EquipmentSlot.LEGS);
        ItemStack boots = player.getEquippedStack(EquipmentSlot.FEET);

        List<ItemStack> armorPieces = List.of(helmet, chestplate, leggings, boots);

        for (ItemStack stack : armorPieces) {
            Text customName = stack.get(DataComponentTypes.CUSTOM_NAME);
            String name = customName != null ? customName.getString() : "";
            boolean isT2 = name.contains(" II");

            // Standard T1/T2 bounty armor items have armor+toughness baked into
            // ATTRIBUTE_MODIFIERS. We still need to add the EXTRA attributes
            // (health, speed, knockback resist) using the custom item ID.
            AttributeModifiersComponent builtIn = stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
            if (builtIn != null && !builtIn.modifiers().isEmpty()) {
                String cid = SlayerItems.getCustomItemId(stack);
                if (cid != null) {
                    switch (cid) {
                        // ── ZOMBIE ──────────────────────────────────────
                        case "zombie_t1_helmet"      -> bonusHealth += 4.0;   // +2 hearts
                        // zombie_t2_helmet: no health bonus (Hunger Immunity + Fire Resist only)
                        case "zombie_t1_chestplate"  -> bonusHealth += 4.0;   // +2 hearts
                        case "zombie_t2_chestplate"  -> bonusHealth += 8.0;   // +4 hearts
                        case "zombie_t1_boots"       -> bonusKnockbackResist += 0.1;  // +10%
                        case "zombie_t2_boots"       -> bonusKnockbackResist += 0.2;  // +20%
                        // ── SPIDER ──────────────────────────────────────
                        case "spider_t1_helmet"      -> bonusSpeed += 0.05;   // +5%
                        case "spider_t2_helmet"      -> bonusSpeed += 0.10;   // +10%
                        case "spider_t1_boots"       -> bonusSpeed += 0.10;   // +10%
                        case "spider_t2_boots"       -> bonusSpeed += 0.15;   // +15%
                        // ── SKELETON ─────────────────────────────────────
                        case "skeleton_t1_leggings"  -> bonusSpeed += 0.05;   // +5%
                        case "skeleton_t1_boots"     -> bonusSpeed += 0.05;   // +5%
                        case "skeleton_t2_leggings"  -> bonusSpeed += 0.10;   // +10%
                        case "skeleton_t2_boots"     -> bonusSpeed += 0.10;   // +10%
                        // ── SLIME ────────────────────────────────────────
                        case "slime_t1_helmet"       -> bonusHealth += 4.0;   // +2 hearts
                        case "slime_t2_helmet"       -> bonusHealth += 8.0;   // +4 hearts
                        case "slime_t1_chestplate"   -> bonusHealth += 8.0;   // +4 hearts
                        case "slime_t2_chestplate"   -> bonusHealth += 8.0;   // +4 hearts
                        // ── ENDERMAN ─────────────────────────────────────
                        case "enderman_t1_chestplate" -> bonusSpeed += 0.05;  // +5%
                        case "enderman_t2_chestplate" -> bonusSpeed += 0.15;  // +15%
                        case "enderman_t1_leggings"   -> bonusSpeed += 0.05;  // +5%
                        case "enderman_t2_leggings"   -> bonusSpeed += 0.10;  // +10%
                        case "enderman_t2_boots"      -> bonusSpeed += 0.10;  // +10%
                        // ── IRON_GOLEM ───────────────────────────────────────
                        case "warden_t1_chestplate"  -> bonusKnockbackResist += 0.10; // +10%
                        case "warden_t2_chestplate"  -> bonusKnockbackResist += 0.25; // +25%
                        case "warden_t1_leggings"    -> bonusKnockbackResist += 0.10; // +10%
                        case "warden_t2_leggings"    -> {
                            bonusKnockbackResist += 0.15; // +15%
                            bonusHealth += 8.0;            // +4 hearts
                        }
                        // warden_t1_boots: Vibration Sense only — no KB resist
                        case "warden_t2_boots"       -> bonusKnockbackResist += 0.10; // +10%
                        // ── PIGLIN ───────────────────────────────────────
                        case "piglin_t1_chestplate"  -> bonusHealth += 4.0;    // +2 hearts
                        case "piglin_t2_chestplate"  -> bonusHealth += 8.0;    // +4 hearts
                        case "piglin_t1_leggings"    -> bonusSpeed  += 0.05;   // +5%
                        case "piglin_t2_leggings"    -> bonusSpeed  += 0.10;   // +10%
                        case "piglin_t2_boots"       -> bonusSpeed  += 0.10;   // +10%
                        default -> {}
                    }
                }
            }

            // Armour Attribute system: Cursed (+6 HP)
            ArmourAttribute attribute = SlayerItems.getAppliedAttribute(stack);
            if (attribute == ArmourAttribute.CURSED) {
                bonusHealth += 6.0;
            }
            
            // ── Legacy / special items WITHOUT built-in attribute modifiers ──
            // Skip if item already has ATTRIBUTE_MODIFIERS (handled by custom ID switch above)
            if (builtIn != null && !builtIn.modifiers().isEmpty()) {
                continue; // Already processed via custom ID switch
            }

            // ===== ZOMBIE ARMOR =====
            if (name.contains("Berserker") && name.contains("Helmet")) {
                bonusArmor += 10.0;      // buffed from 3 → 10
                bonusToughness += 5.0;   // buffed from 2 → 5
                bonusHealth += 8.0;      // +4 hearts bonus health
            }
            if ((name.contains("Undying") || name.contains("Outlaw")) && name.contains("Helmet")) {
                bonusArmor += isT2 ? 5.0 : 3.0;
                bonusToughness += isT2 ? 3.0 : 2.0;
                bonusHealth += isT2 ? 8.0 : 4.0;
            }
            if ((name.contains("Undying") || name.contains("Outlaw")) && name.contains("Chestplate")) {
                bonusArmor += isT2 ? 10.0 : 6.0;
                bonusToughness += isT2 ? 4.0 : 2.0;
                bonusHealth += isT2 ? 8.0 : 4.0;
            }
            if ((name.contains("Undying") || name.contains("Outlaw")) && name.contains("Leggings")) {
                bonusArmor += isT2 ? 8.0 : 5.0;
                bonusToughness += isT2 ? 3.0 : 2.0;
            }
            if ((name.contains("Undying") || name.contains("Outlaw")) && name.contains("Boots")) {
                bonusArmor += isT2 ? 5.0 : 3.0;
                bonusToughness += isT2 ? 3.0 : 2.0;
                bonusKnockbackResist += isT2 ? 0.2 : 0.1;
            }

            // ===== SPIDER ARMOR =====
            if (name.contains("Venomous") && name.contains("Leggings")) {
                bonusArmor += isT2 ? 10.0 : 10.0;
                bonusToughness += isT2 ? 4.0 : 4.0;
            }
            if ((name.contains("Venomous") || name.contains("Bandit")) && name.contains("Helmet")) {
                bonusArmor += isT2 ? 5.0 : 3.0;
                bonusToughness += isT2 ? 3.0 : 2.0;
                bonusSpeed += isT2 ? 0.1 : 0.05;
            }
            if ((name.contains("Venomous") || name.contains("Bandit")) && name.contains("Chestplate")) {
                bonusArmor += isT2 ? 10.0 : 7.0;
                bonusToughness += isT2 ? 4.0 : 2.0;
            }
            if ((name.contains("Venomous") || name.contains("Bandit")) && name.contains("Boots")) {
                bonusArmor += isT2 ? 5.0 : 3.0;
                bonusToughness += isT2 ? 3.0 : 2.0;
                bonusSpeed += isT2 ? 0.15 : 0.1;
            }

            // ===== SKELETON ARMOR =====
            if ((name.contains("Bone") || name.contains("Desperado")) && name.contains("Chestplate")) {
                bonusArmor += isT2 ? 10.0 : 8.0;
                bonusToughness += isT2 ? 4.0 : 2.0;
            }
            if ((name.contains("Bone") || name.contains("Desperado")) && name.contains("Helmet")) {
                bonusArmor += isT2 ? 5.0 : 4.0;
                bonusToughness += isT2 ? 3.0 : 2.0;
            }
            if ((name.contains("Bone") || name.contains("Desperado")) && name.contains("Leggings")) {
                bonusArmor += isT2 ? 8.0 : 6.0;
                bonusToughness += isT2 ? 3.0 : 2.0;
                bonusSpeed += isT2 ? 0.10 : 0.05;
            }
            if ((name.contains("Bone") || name.contains("Desperado")) && name.contains("Boots")) {
                bonusArmor += isT2 ? 5.0 : 3.0;
                bonusToughness += isT2 ? 3.0 : 2.0;
                bonusSpeed += isT2 ? 0.10 : 0.05;
            }

            // ===== SLIME ARMOR =====
            if ((name.contains("Slime") || name.contains("Gelatinous") || name.contains("Rustler")) && name.contains("Boots")) {
                bonusArmor += isT2 ? 6.0 : 6.0;
                bonusToughness += isT2 ? 3.0 : 3.0;
            }
            if ((name.contains("Gelatinous") || name.contains("Rustler")) && name.contains("Helmet")) {
                bonusArmor += isT2 ? 5.0 : 3.0;
                bonusToughness += isT2 ? 3.0 : 2.0;
                bonusHealth += isT2 ? 8.0 : 4.0;
            }
            if ((name.contains("Gelatinous") || name.contains("Rustler")) && name.contains("Chestplate")) {
                bonusArmor += isT2 ? 10.0 : 7.0;
                bonusToughness += isT2 ? 4.0 : 2.0;
                bonusHealth += 8.0;  // +4 hearts for both T1 and T2
            }
            if ((name.contains("Gelatinous") || name.contains("Rustler")) && name.contains("Leggings")) {
                bonusArmor += isT2 ? 8.0 : 6.0;
                bonusToughness += isT2 ? 3.0 : 2.0;
            }

            // ===== IRON_GOLEM/SCULK ARMOR =====
            if (name.contains("Sculk Terror") || name.contains("Terror Chestplate")) {
                continue; // Already has +12 armor, +6 toughness, +24 health, +1 KB from attributes
            }
            if (name.contains("Warden") || name.contains("Sculk") || name.contains("Terror")) {
                bonusArmor += isT2 ? 10.0 : 8.0;
                bonusToughness += isT2 ? 4.0 : 3.0;
                bonusKnockbackResist += isT2 ? 0.3 : 0.2;
            }

            // ===== ENDERMAN/VOID ARMOR =====
            if (name.contains("Voidwalker's Crown") || name.contains("Voidwalker Crown")) {
                continue; // Already has +8 armor, +4 toughness, +20 health, +3 damage from attributes
            }
            if (name.contains("Void") || name.contains("Phantom")) {
                bonusArmor += isT2 ? 8.0 : 7.0;
                bonusToughness += isT2 ? 3.0 : 2.5;
                bonusSpeed += isT2 ? 0.1 : 0.05;
            }
            if (name.contains("Inferno Dragon Chestplate")) {
                bonusArmor += 20.0;
                bonusToughness += 15.0;
                bonusHealth += 20.0;
                bonusKnockbackResist += 0.3;
            }
            if (name.contains("Storm Dragon Chestplate")) {
                bonusArmor += 18.0;
                bonusToughness += 12.0;
                bonusHealth += 24.0;
                bonusSpeed += 0.25;
                bonusKnockbackResist += 0.2;
            }
            if (name.contains("Void Dragon Chestplate")) {
                bonusArmor += 25.0;
                bonusToughness += 20.0;
                bonusHealth += 30.0;
                bonusSpeed += 0.15;
                bonusKnockbackResist += 0.4;
            }
        }

        // Apply modifiers
        applyModifier(player, EntityAttributes.ARMOR, SLAYER_ARMOR_ID, bonusArmor);
        applyModifier(player, EntityAttributes.ARMOR_TOUGHNESS, SLAYER_TOUGHNESS_ID, bonusToughness);
        applyModifier(player, EntityAttributes.MAX_HEALTH, SLAYER_HEALTH_ID, bonusHealth);
        applyModifier(player, EntityAttributes.KNOCKBACK_RESISTANCE, SLAYER_KB_ID, bonusKnockbackResist);
        applyModifier(player, EntityAttributes.MOVEMENT_SPEED, SLAYER_SPEED_ID, bonusSpeed);
    }

    private static void applyModifier(ServerPlayerEntity player,
                                      RegistryEntry<EntityAttribute> attribute,
                                      Identifier id, double value) {
        EntityAttributeInstance instance = player.getAttributeInstance(attribute);
        if (instance == null) return;

        instance.removeModifier(id);
        if (value > 0) {
            instance.addTemporaryModifier(new EntityAttributeModifier(
                    id, value, EntityAttributeModifier.Operation.ADD_VALUE
            ));
        }
    }
}