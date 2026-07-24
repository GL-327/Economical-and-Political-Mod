package com.political;

import net.minecraft.ChatFormatting;

/**
 * V6.6 — Binding Vows. Opt-in tradeoffs: give up something real to gain something real.
 *
 * <p>Each vow is a toggle with a genuine mechanical downside <i>and</i> upside, so no vow is a
 * free buff. The number of vows a player may hold at once is limited by their
 * {@link SorcererRank#vowSlots}. All effects are enforced by {@link SorcererManager}'s own
 * damage/tick hooks — none of them require editing combat code.</p>
 */
public enum BindingVow {

    HOLLOW_WICKER("hollow_wicker", "Hollow Wicker Basket", ChatFormatting.DARK_GREEN,
            new String[]{"§a+25% melee damage"},
            new String[]{"§cYour projectiles deal no damage"},
            /*melee*/0.25, /*bfChanceMult*/1.0, /*bfBonusMult*/1.0, /*regenMult*/1.0, /*maxCeMult*/1.0,
            /*healthMult*/0.0, /*speedMult*/0.0, /*comboRateMult*/1.0, /*bfHeartCost*/0,
            /*cancelProjectiles*/true, /*disableRct*/false, /*disableDomain*/false),

    OVERTIME("overtime", "Overtime", ChatFormatting.RED,
            new String[]{"§a+25% melee damage", "§a+30% cursed energy regen"},
            new String[]{"§c-20% maximum health"},
            0.25, 1.0, 1.0, 1.30, 1.0,
            -0.20, 0.0, 1.0, 0,
            false, false, false),

    VOW_OF_SILENCE("vow_of_silence", "Vow of Silence", ChatFormatting.GRAY,
            new String[]{"§aBlack Flash chance doubled"},
            new String[]{"§cCursed energy regen halved"},
            0.0, 2.0, 1.0, 0.50, 1.0,
            0.0, 0.0, 1.0, 0,
            false, false, false),

    IRON_BODY("iron_body", "Iron Body", ChatFormatting.AQUA,
            new String[]{"§a+15% melee damage", "§aBlack Flash deals +50% more"},
            new String[]{"§c-15% movement speed"},
            0.15, 1.0, 1.50, 1.0, 1.0,
            0.0, -0.15, 1.0, 0,
            false, false, false),

    BLOOD_PACT("blood_pact", "Blood Pact", ChatFormatting.DARK_RED,
            new String[]{"§aBlack Flash deals +80% more"},
            new String[]{"§cEach Black Flash costs 2 hearts"},
            0.0, 1.0, 1.80, 1.0, 1.0,
            0.0, 0.0, 1.0, 2,
            false, false, false),

    PRAYER_SONG("prayer_song", "Prayer Song", ChatFormatting.GOLD,
            new String[]{"§a+20% melee damage"},
            new String[]{"§cCannot use Reverse Cursed Technique"},
            0.20, 1.0, 1.0, 1.0, 1.0,
            0.0, 0.0, 1.0, 0,
            false, true, false),

    DOMAIN_REFUSAL("domain_refusal", "Domain Refusal", ChatFormatting.LIGHT_PURPLE,
            new String[]{"§a+40% maximum cursed energy", "§a+20% cursed energy regen"},
            new String[]{"§cCannot expand your Domain"},
            0.0, 1.0, 1.0, 1.20, 1.40,
            0.0, 0.0, 1.0, 0,
            false, false, true),

    TEN_SHADOWS("ten_shadows", "Ten Shadows Discipline", ChatFormatting.DARK_PURPLE,
            new String[]{"§a+50% maximum cursed energy", "§aCombo meter builds twice as fast"},
            new String[]{"§c-10% maximum health", "§c-10% movement speed"},
            0.0, 1.0, 1.0, 1.0, 1.50,
            -0.10, -0.10, 2.0, 0,
            false, false, false);

    public final String id;
    public final String display;
    public final ChatFormatting color;
    public final String[] upsides;
    public final String[] downsides;
    public final double meleeBonus;      // additive fraction
    public final double bfChanceMult;    // multiplicative
    public final double bfBonusMult;     // multiplicative on the Black Flash bonus damage
    public final double regenMult;       // multiplicative on CE regen
    public final double maxCeMult;       // multiplicative on max CE
    public final double healthMult;      // additive fraction on max health (attribute)
    public final double speedMult;       // additive fraction on movement speed (attribute)
    public final double comboRateMult;   // multiplicative on combo gain
    public final int bfHeartCost;        // hearts spent per Black Flash
    public final boolean cancelsProjectiles;
    public final boolean disablesRct;
    public final boolean disablesDomain;

    BindingVow(String id, String display, ChatFormatting color, String[] upsides, String[] downsides,
               double meleeBonus, double bfChanceMult, double bfBonusMult, double regenMult, double maxCeMult,
               double healthMult, double speedMult, double comboRateMult, int bfHeartCost,
               boolean cancelsProjectiles, boolean disablesRct, boolean disablesDomain) {
        this.id = id;
        this.display = display;
        this.color = color;
        this.upsides = upsides;
        this.downsides = downsides;
        this.meleeBonus = meleeBonus;
        this.bfChanceMult = bfChanceMult;
        this.bfBonusMult = bfBonusMult;
        this.regenMult = regenMult;
        this.maxCeMult = maxCeMult;
        this.healthMult = healthMult;
        this.speedMult = speedMult;
        this.comboRateMult = comboRateMult;
        this.bfHeartCost = bfHeartCost;
        this.cancelsProjectiles = cancelsProjectiles;
        this.disablesRct = disablesRct;
        this.disablesDomain = disablesDomain;
    }

    public static BindingVow byId(String id) {
        if (id == null) return null;
        for (BindingVow v : values()) if (v.id.equals(id)) return v;
        return null;
    }
}
