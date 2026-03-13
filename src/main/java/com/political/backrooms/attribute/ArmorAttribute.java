package com.political.backrooms.attribute;

/**
 * Enum of armor attributes that can be applied via AttributeRuneItem.
 * Each set piece counts; more pieces = stronger effect.
 */
public enum ArmorAttribute {
    /** +0.25 dmg/piece while burning, -10% fire dmg per piece; can't stop burning naturally */
    PYROMANIAC,
    /** +15% dmg when below 30% HP per piece; -10% defense above 70% HP */
    BERSERKERS_RAGE,
    /** +20% speed in darkness (light 0-7) per piece; -15% speed in bright light */
    NIGHTSTALKER,
    /** +25% knockback resist per piece; -20% movement speed */
    HEAVYWEIGHT,
    /** 5% lifesteal per piece; 2x fire damage taken */
    VAMPIRIC,
    /** Silent (no sound for Partygoers), +10% speed; -20% defense vs Skinstealers */
    ECHO_WALKER,
    /** +15% dmg in Backrooms dimensions; -20% dmg in Overworld */
    SKULK_INFUSED,
    /** +30% jump height (with 4 pieces: no fall damage); -15% knockback resist */
    FEATHERWEIGHT,
    /** Attackers take 20% dmg back per piece; 5% self-dmg when attacking */
    THORNED,
    /** +50% food restore per piece; hunger 25% faster */
    GLUTTON
}
