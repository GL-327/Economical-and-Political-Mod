package com.political.power.formula;

/** The three binding vows (C10): each gives a bonus for as long as its condition holds. */
public enum Vow {
    /** All four armor slots empty. Raises the maximum of the resource. */
    NO_ARMOR,
    /** Nothing in the off hand. Shortens every cooldown. */
    NO_OFFHAND,
    /** Not sprinting. Raises technique damage. */
    NO_SPRINT;

    /** What a player is wearing and doing, as far as vows care. */
    public record Loadout(boolean armorEmpty, boolean offhandEmpty, boolean sprinting) {
    }

    /** Whether the vow's condition holds for this loadout. */
    public boolean satisfiedBy(Loadout loadout) {
        return switch (this) {
            case NO_ARMOR -> loadout.armorEmpty();
            case NO_OFFHAND -> loadout.offhandEmpty();
            case NO_SPRINT -> !loadout.sprinting();
        };
    }
}
