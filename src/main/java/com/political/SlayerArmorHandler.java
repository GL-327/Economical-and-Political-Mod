package com.political;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class SlayerArmorHandler {

    private static final Identifier SLAYER_ARMOR_ID = Identifier.of("political", "slayer_armor");
    private static final Identifier SLAYER_TOUGHNESS_ID = Identifier.of("political", "slayer_toughness");
    private static final Identifier SLAYER_HEALTH_ID = Identifier.of("political", "slayer_health");
    private static final Identifier SLAYER_KB_ID = Identifier.of("political", "slayer_knockback");
    private static final Identifier SLAYER_SPEED_ID = Identifier.of("political", "slayer_speed");

    /**
     * Calculate total boss damage reduction from armor
     */
    public static float getBossDamageReduction(ServerPlayerEntity player, SlayerManager.SlayerType bossType) {
        float reduction = 0.0f;
        int t1MatchingPieces = 0;
        int t2MatchingPieces = 0;

        ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);
        ItemStack chestplate = player.getEquippedStack(EquipmentSlot.CHEST);
        ItemStack leggings = player.getEquippedStack(EquipmentSlot.LEGS);
        ItemStack boots = player.getEquippedStack(EquipmentSlot.FEET);

        List<ItemStack> armor = List.of(helmet, chestplate, leggings, boots);

        for (ItemStack stack : armor) {
            Text customName = stack.get(DataComponentTypes.CUSTOM_NAME);
            if (customName == null) continue;
            String name = customName.getString();

            // Check T2 first (contains " II")
            if (name.contains(" II") && isMatchingSlayerArmor(name, bossType)) {
                t2MatchingPieces++;
            } else if (isMatchingSlayerArmor(name, bossType)) {
                t1MatchingPieces++;
            }
        }

        // T1: 10% per piece, T2: 20% per piece
        reduction += t1MatchingPieces * 0.10f;
        reduction += t2MatchingPieces * 0.20f;

        // Full set bonus
        if (t1MatchingPieces >= 4) {
            reduction += 0.15f; // T1 full set: +15%
        }
        if (t2MatchingPieces >= 4) {
            reduction += 0.25f; // T2 full set: +25%
        }

        // Mixed set bonus (T1 + T2)
        if (t1MatchingPieces + t2MatchingPieces >= 4 && t2MatchingPieces >= 1) {
            reduction += 0.10f; // Hybrid bonus
        }

        // Cap at 80% for T2, 60% for T1 only
        float cap = t2MatchingPieces > 0 ? 0.80f : 0.60f;
        return Math.min(cap, reduction);
    }

    private static boolean isMatchingSlayerArmor(String name, SlayerManager.SlayerType type) {
        return switch (type) {
            case ZOMBIE -> name.contains("Berserker") || name.contains("Undying") || name.contains("Outlaw");
            case SPIDER -> name.contains("Venomous") || name.contains("Spider") || name.contains("Bandit");
            case SKELETON -> name.contains("Bone") || name.contains("Desperado");
            case SLIME -> name.contains("Slime") || name.contains("Gelatinous") || name.contains("Rustler");
            case ENDERMAN -> name.contains("Void") || name.contains("Phantom");
            case WARDEN -> name.contains("Sculk") || name.contains("Warden") || name.contains("Terror");
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

            // ===== ZOMBIE ARMOR =====
            if (name.contains("Berserker") && name.contains("Helmet")) {
                bonusArmor += 3.0;
                bonusToughness += 2.0;
            }
            if ((name.contains("Undying") || name.contains("Outlaw")) && name.contains("Helmet")) {
                bonusArmor += isT2 ? 5.0 : 3.0;
                bonusToughness += isT2 ? 3.0 : 2.0;
                bonusHealth += isT2 ? 4.0 : 2.0;
            }
            if ((name.contains("Undying") || name.contains("Outlaw")) && name.contains("Chestplate")) {
                bonusArmor += isT2 ? 10.0 : 6.0;
                bonusToughness += isT2 ? 4.0 : 2.0;
                bonusHealth += isT2 ? 6.0 : 4.0;
            }
            if ((name.contains("Undying") || name.contains("Outlaw")) && name.contains("Leggings")) {
                bonusArmor += isT2 ? 8.0 : 5.0;
                bonusToughness += isT2 ? 3.0 : 2.0;
                bonusHealth += isT2 ? 4.0 : 2.0;
            }
            if ((name.contains("Undying") || name.contains("Outlaw")) && name.contains("Boots")) {
                bonusArmor += isT2 ? 5.0 : 3.0;
                bonusToughness += isT2 ? 3.0 : 2.0;
                bonusKnockbackResist += isT2 ? 0.2 : 0.1;
            }

            // ===== SPIDER ARMOR =====
            if (name.contains("Venomous") && name.contains("Leggings")) {
                bonusArmor += isT2 ? 8.0 : 6.0;
                bonusToughness += isT2 ? 3.0 : 2.0;
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
            }
            if ((name.contains("Bone") || name.contains("Desperado")) && name.contains("Boots")) {
                bonusArmor += isT2 ? 5.0 : 3.0;
                bonusToughness += isT2 ? 3.0 : 2.0;
            }

            // ===== SLIME ARMOR =====
            if ((name.contains("Slime") || name.contains("Gelatinous") || name.contains("Rustler")) && name.contains("Boots")) {
                bonusArmor += isT2 ? 5.0 : 3.0;
                bonusToughness += isT2 ? 3.0 : 2.0;
            }
            if ((name.contains("Gelatinous") || name.contains("Rustler")) && name.contains("Helmet")) {
                bonusArmor += isT2 ? 5.0 : 3.0;
                bonusToughness += isT2 ? 3.0 : 2.0;
                bonusHealth += isT2 ? 4.0 : 2.0;
            }
            if ((name.contains("Gelatinous") || name.contains("Rustler")) && name.contains("Chestplate")) {
                bonusArmor += isT2 ? 10.0 : 7.0;
                bonusToughness += isT2 ? 4.0 : 2.0;
                bonusHealth += isT2 ? 6.0 : 4.0;
            }
            if ((name.contains("Gelatinous") || name.contains("Rustler")) && name.contains("Leggings")) {
                bonusArmor += isT2 ? 8.0 : 6.0;
                bonusToughness += isT2 ? 3.0 : 2.0;
            }

            // ===== WARDEN/SCULK ARMOR =====
            if (name.contains("Warden") || name.contains("Sculk") || name.contains("Terror")) {
                bonusArmor += isT2 ? 10.0 : 8.0;
                bonusToughness += isT2 ? 4.0 : 3.0;
                bonusKnockbackResist += isT2 ? 0.3 : 0.2;
            }

            // ===== ENDERMAN/VOID ARMOR =====
            if (name.contains("Void") || name.contains("Phantom")) {
                bonusArmor += isT2 ? 8.0 : 7.0;
                bonusToughness += isT2 ? 3.0 : 2.5;
                bonusSpeed += isT2 ? 0.1 : 0.05;
            }
            // ===== T2 SKELETON ARMOR (Bone Desperado II) =====
            if ((name.contains("Bone") || name.contains("Desperado")) && name.contains(" II")) {
                if (name.contains("Helmet")) {
                    bonusArmor += 5.0;
                    bonusToughness += 3.0;
                } else if (name.contains("Chestplate")) {
                    bonusArmor += 10.0;
                    bonusToughness += 4.0;
                } else if (name.contains("Leggings")) {
                    bonusArmor += 8.0;
                    bonusToughness += 3.0;
                } else if (name.contains("Boots")) {
                    bonusArmor += 5.0;
                    bonusToughness += 3.0;
                    bonusSpeed += 0.1;
                }
            }

// ===== T2 SLIME ARMOR (Gelatinous Rustler II) =====
            if ((name.contains("Gelatinous") || name.contains("Rustler")) && name.contains(" II")) {
                if (name.contains("Helmet")) {
                    bonusArmor += 5.0;
                    bonusToughness += 3.0;
                    bonusHealth += 6.0;
                } else if (name.contains("Chestplate")) {
                    bonusArmor += 10.0;
                    bonusToughness += 4.0;
                    bonusHealth += 10.0;
                } else if (name.contains("Leggings")) {
                    bonusArmor += 8.0;
                    bonusToughness += 3.0;
                    bonusHealth += 6.0;
                } else if (name.contains("Boots")) {
                    bonusArmor += 5.0;
                    bonusToughness += 3.0;
                    // Fall damage immunity handled in mixin
                }
            }

// ===== T2 ENDERMAN ARMOR (Void Phantom II) =====
            if ((name.contains("Void") || name.contains("Phantom")) && name.contains(" II")) {
                if (name.contains("Helmet")) {
                    bonusArmor += 5.0;
                    bonusToughness += 3.0;
                    bonusSpeed += 0.15;
                } else if (name.contains("Chestplate")) {
                    bonusArmor += 10.0;
                    bonusToughness += 4.0;
                    bonusSpeed += 0.1;
                } else if (name.contains("Leggings")) {
                    bonusArmor += 8.0;
                    bonusToughness += 3.0;
                    bonusSpeed += 0.1;
                } else if (name.contains("Boots")) {
                    bonusArmor += 5.0;
                    bonusToughness += 3.0;
                    bonusSpeed += 0.2;
                }
            }

// ===== T2 WARDEN ARMOR (Sculk Terror II) - BEST SET =====
            if ((name.contains("Sculk") || name.contains("Terror")) && name.contains(" II")) {
                if (name.contains("Helmet")) {
                    bonusArmor += 6.0;
                    bonusToughness += 4.0;
                    bonusKnockbackResist += 0.25;
                } else if (name.contains("Chestplate")) {
                    bonusArmor += 12.0;
                    bonusToughness += 5.0;
                    bonusKnockbackResist += 0.4;
                    bonusHealth += 8.0;
                } else if (name.contains("Leggings")) {
                    bonusArmor += 10.0;
                    bonusToughness += 4.0;
                    bonusKnockbackResist += 0.25;
                    bonusHealth += 6.0;
                } else if (name.contains("Boots")) {
                    bonusArmor += 6.0;
                    bonusToughness += 4.0;
                    bonusKnockbackResist += 0.1;
                }
            }
        }

        // Apply armor modifier
        applyModifier(player, EntityAttributes.ARMOR, SLAYER_ARMOR_ID, bonusArmor);
        applyModifier(player, EntityAttributes.ARMOR_TOUGHNESS, SLAYER_TOUGHNESS_ID, bonusToughness);
        applyModifier(player, EntityAttributes.MAX_HEALTH, SLAYER_HEALTH_ID, bonusHealth);
        applyModifier(player, EntityAttributes.KNOCKBACK_RESISTANCE, SLAYER_KB_ID, bonusKnockbackResist);
        applyModifier(player, EntityAttributes.MOVEMENT_SPEED, SLAYER_SPEED_ID, bonusSpeed);
    }

    private static void applyModifier(ServerPlayerEntity player,
                                      net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attribute,
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