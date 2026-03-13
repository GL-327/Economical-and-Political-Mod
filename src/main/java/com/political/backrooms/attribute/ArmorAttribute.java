package com.political.backrooms.attribute;

import net.minecraft.entity.EquipmentSlot;

/**
 * Enum of armor attributes that can be applied via AttributeRuneItem.
 * Each set piece counts; more pieces = stronger effect.
 * Each attribute has a required armor slot — runes can only be applied to the matching slot.
 */
public enum ArmorAttribute {
    /** +0.25 dmg/piece while burning, -10% fire dmg per piece; can't stop burning naturally */
    PYROMANIAC(EquipmentSlot.CHEST),
    /** +15% dmg when below 30% HP per piece; -10% defense above 70% HP */
    BERSERKERS_RAGE(EquipmentSlot.CHEST),
    /** +20% speed in darkness (light 0-7) per piece; -15% speed in bright light */
    NIGHTSTALKER(EquipmentSlot.LEGS),
    /** +25% knockback resist per piece; -20% movement speed */
    HEAVYWEIGHT(EquipmentSlot.LEGS),
    /** 5% lifesteal per piece; 2x fire damage taken */
    VAMPIRIC(EquipmentSlot.CHEST),
    /** Silent (no sound for Partygoers), +10% speed; -20% defense vs Skinstealers */
    ECHO_WALKER(EquipmentSlot.FEET),
    /** +15% dmg in Backrooms dimensions; -20% dmg in Overworld */
    SKULK_INFUSED(EquipmentSlot.HEAD),
    /** +30% jump height; with 4 pieces: no fall damage; -15% knockback resist */
    FEATHERWEIGHT(EquipmentSlot.FEET),
    /** Attackers take 20% dmg back per piece */
    THORNED(EquipmentSlot.CHEST),
    /** +50% food restore per piece; hunger 25% faster */
    GLUTTON(EquipmentSlot.HEAD);

    /** The armor slot this rune must be applied to. */
    public final EquipmentSlot requiredSlot;

    ArmorAttribute(EquipmentSlot requiredSlot) {
        this.requiredSlot = requiredSlot;
    }
}
