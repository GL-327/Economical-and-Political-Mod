package com.political.backrooms.attribute;

import net.minecraft.entity.EquipmentSlot;

/**
 * Enum of armor attributes that can be applied via AttributeRuneItem.
 * Each set piece counts; more pieces = stronger effect.
 * Each attribute has a required armor slot — runes can only be applied to the matching slot.
 * XP cost tiers: Common=5, Uncommon=10, Rare=20, Epic=35, Legendary=50.
 */
public enum ArmorAttribute {
    /** [Legendary] +0.25 dmg/piece while burning, -10% fire dmg per piece; can't stop burning naturally */
    PYROMANIAC(EquipmentSlot.CHEST, 50),
    /** [Epic] +15% dmg when below 30% HP per piece; -10% defense above 70% HP */
    BERSERKERS_RAGE(EquipmentSlot.CHEST, 35),
    /** [Rare] +20% speed in darkness (light 0-7) per piece; -15% speed in bright light */
    NIGHTSTALKER(EquipmentSlot.LEGS, 20),
    /** [Rare] +25% knockback resist per piece; -20% movement speed */
    HEAVYWEIGHT(EquipmentSlot.LEGS, 20),
    /** [Epic] 5% lifesteal per piece; 2x fire damage taken */
    VAMPIRIC(EquipmentSlot.CHEST, 35),
    /** [Uncommon] Silent (no sound for Partygoers), +10% speed; -20% defense vs Skinstealers */
    ECHO_WALKER(EquipmentSlot.FEET, 10),
    /** [Legendary] +15% dmg in Backrooms dimensions; -20% dmg in Overworld */
    SKULK_INFUSED(EquipmentSlot.HEAD, 50),
    /** [Rare] +30% jump height; with 4 pieces: no fall damage; -15% knockback resist */
    FEATHERWEIGHT(EquipmentSlot.FEET, 20),
    /** [Uncommon] Attackers take 20% dmg back per piece */
    THORNED(EquipmentSlot.CHEST, 10),
    /** [Rare] +50% food restore per piece; hunger 25% faster */
    GLUTTON(EquipmentSlot.HEAD, 20);

    /** The armor slot this rune must be applied to. */
    public final EquipmentSlot requiredSlot;

    /** XP levels required to apply this rune via the anvil. */
    public final int xpCost;

    ArmorAttribute(EquipmentSlot requiredSlot, int xpCost) {
        this.requiredSlot = requiredSlot;
        this.xpCost = xpCost;
    }

    /** Returns the XP level cost to apply this rune via an anvil. */
    public int getXpCost() {
        return xpCost;
    }
}
