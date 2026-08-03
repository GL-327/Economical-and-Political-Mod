package com.political;

import net.minecraft.ChatFormatting;

import java.util.Locale;

/**
 * The custom enchantments: eighty five of them, none registered.
 *
 * <p>Vanilla enchantments live in a registry, which on a server side mod means a datapack and a
 * second file for a server owner to lose. These do not. An enchantment here is a line of lore plus
 * a handler that reads it, exactly like the stat system, so the whole set ships inside the one jar
 * and works on a vanilla client with nothing installed.</p>
 *
 * <p>They are also readable. A player sees <b>Bloodthirst V</b> on the tooltip, the same way a
 * vanilla enchantment reads, and the mod parses that back off the item when it matters. That is the
 * whole trick, and it is why these can be applied to any of the seven thousand existing items
 * without touching a single one of them.</p>
 */
public enum CustomEnchant {

    // ── Weapon: damage ────────────────────────────────────────────────────
    BLOODTHIRST("Bloodthirst", Slot.WEAPON, 5, ChatFormatting.DARK_RED,
            "Heals you for a share of the damage you deal."),
    EXECUTIONER("Executioner", Slot.WEAPON, 5, ChatFormatting.DARK_RED,
            "Hits far harder against anything already below half."),
    FIRST_STRIKE("First Strike", Slot.WEAPON, 4, ChatFormatting.GOLD,
            "Your opening hit on a target lands much heavier."),
    RELENTLESS("Relentless", Slot.WEAPON, 5, ChatFormatting.RED,
            "Every consecutive hit on the same target adds damage."),
    GIANT_SLAYER("Giant Slayer", Slot.WEAPON, 5, ChatFormatting.YELLOW,
            "Scales with how much more health the target has than you."),
    CLEAVE("Cleave", Slot.WEAPON, 4, ChatFormatting.RED,
            "A share of the hit carries to everything beside the target."),
    THUNDERSTRUCK("Thunderstruck", Slot.WEAPON, 3, ChatFormatting.YELLOW,
            "Occasionally calls lightning onto whatever you hit."),
    VENOMOUS_EDGE("Venomous Edge", Slot.WEAPON, 4, ChatFormatting.DARK_GREEN,
            "Poisons on hit, and the poison stacks."),
    FROSTBRAND("Frostbrand", Slot.WEAPON, 4, ChatFormatting.AQUA,
            "Slows what you hit and freezes it at higher levels."),
    SOULREND("Soulrend", Slot.WEAPON, 5, ChatFormatting.DARK_PURPLE,
            "Withers the target and stops it healing."),
    OVERWHELM("Overwhelm", Slot.WEAPON, 4, ChatFormatting.GRAY,
            "Ignores a share of the target's armour."),
    DUELIST("Duelist", Slot.WEAPON, 5, ChatFormatting.WHITE,
            "Far stronger when only one enemy is near you."),
    SWARMBANE("Swarmbane", Slot.WEAPON, 5, ChatFormatting.GREEN,
            "Far stronger when several enemies are near you."),
    MOMENTUM("Momentum", Slot.WEAPON, 4, ChatFormatting.AQUA,
            "Damage rises the faster you are moving."),
    LAST_WORD("Last Word", Slot.WEAPON, 3, ChatFormatting.DARK_RED,
            "Enormous damage while you are near death."),
    PIERCING_WILL("Piercing Will", Slot.WEAPON, 4, ChatFormatting.LIGHT_PURPLE,
            "A share of the hit ignores every reduction entirely."),
    TROPHY_HUNTER("Trophy Hunter", Slot.WEAPON, 5, ChatFormatting.GOLD,
            "Raises Magic Find on anything this weapon kills."),
    HEADHUNTER("Headhunter", Slot.WEAPON, 3, ChatFormatting.DARK_PURPLE,
            "A kill occasionally leaves the victim's head behind."),
    CHAIN_KILL("Chain Kill", Slot.WEAPON, 4, ChatFormatting.RED,
            "A kill briefly raises your damage, stacking."),
    TITHE("Tithe", Slot.WEAPON, 5, ChatFormatting.GOLD,
            "Kills pay coins directly."),

    // ── Weapon: critical and precision ────────────────────────────────────
    KEEN_EDGE("Keen Edge", Slot.WEAPON, 5, ChatFormatting.BLUE,
            "Raises crit chance."),
    DEVASTATE("Devastate", Slot.WEAPON, 5, ChatFormatting.BLUE,
            "Raises crit damage sharply."),
    PERFECT_FORM("Perfect Form", Slot.WEAPON, 3, ChatFormatting.AQUA,
            "Every fifth hit is guaranteed to crit."),
    EXPOSE("Expose", Slot.WEAPON, 4, ChatFormatting.YELLOW,
            "A crit leaves the target taking more from everything."),
    BUTCHER("Butcher", Slot.WEAPON, 4, ChatFormatting.DARK_RED,
            "Crits against animals and the undead land far harder."),

    // ── Armour: survival ──────────────────────────────────────────────────
    BULWARK("Bulwark", Slot.ARMOUR, 5, ChatFormatting.GREEN,
            "Flat reduction on everything that hits you."),
    SECOND_SKIN("Second Skin", Slot.ARMOUR, 4, ChatFormatting.WHITE,
            "Raises defence the more of this set you wear."),
    THORNMAIL("Thornmail", Slot.ARMOUR, 5, ChatFormatting.DARK_GREEN,
            "Returns part of every melee hit to the attacker."),
    STOICISM("Stoicism", Slot.ARMOUR, 4, ChatFormatting.GRAY,
            "The lower your health, the less you take."),
    SECOND_WIND("Second Wind", Slot.ARMOUR, 3, ChatFormatting.LIGHT_PURPLE,
            "Once a fight, a killing blow leaves you at a sliver instead."),
    IRONCLAD("Ironclad", Slot.ARMOUR, 5, ChatFormatting.GRAY,
            "Heavy reduction against projectiles."),
    FIREWALK("Firewalk", Slot.ARMOUR, 3, ChatFormatting.GOLD,
            "Fire and lava stop mattering."),
    DEEP_BREATH("Deep Breath", Slot.ARMOUR, 3, ChatFormatting.AQUA,
            "Water stops mattering and you swim quicker."),
    SUREFOOTED("Surefooted", Slot.ARMOUR, 3, ChatFormatting.WHITE,
            "Falling stops hurting and the ground stops slowing you."),
    WARDING("Warding", Slot.ARMOUR, 4, ChatFormatting.LIGHT_PURPLE,
            "Reduces magic and wither damage."),
    REGROWTH("Regrowth", Slot.ARMOUR, 5, ChatFormatting.RED,
            "You mend steadily out of combat."),
    LIFELINE("Lifeline", Slot.ARMOUR, 4, ChatFormatting.RED,
            "Dropping below a third heals you at once, on a long cooldown."),
    VITALITY("Vitality", Slot.ARMOUR, 5, ChatFormatting.RED,
            "Raises maximum health."),
    RESILIENCE("Resilience", Slot.ARMOUR, 4, ChatFormatting.GREEN,
            "Harmful effects wear off far quicker."),
    UNSHAKEABLE("Unshakeable", Slot.ARMOUR, 3, ChatFormatting.DARK_GRAY,
            "You cannot be knocked back."),
    COUNTERPOISE("Counterpoise", Slot.ARMOUR, 4, ChatFormatting.AQUA,
            "Being hit briefly raises your own damage."),

    // ── Armour: utility and finding ───────────────────────────────────────
    PROSPECTOR("Prospector", Slot.ARMOUR, 5, ChatFormatting.AQUA,
            "Raises Magic Find."),
    TREASURE_SENSE("Treasure Sense", Slot.ARMOUR, 3, ChatFormatting.GOLD,
            "Valuable ore inside stone glows as you pass."),
    SCAVENGER("Scavenger", Slot.ARMOUR, 5, ChatFormatting.GOLD,
            "Kills pay a little coin on top of everything else."),
    QUICK_HANDS("Quick Hands", Slot.ARMOUR, 4, ChatFormatting.YELLOW,
            "Drops pull toward you from further away."),
    NIGHT_SIGHT("Night Sight", Slot.ARMOUR, 1, ChatFormatting.DARK_BLUE,
            "The dark stops hiding things."),
    FLEETFOOT("Fleetfoot", Slot.ARMOUR, 5, ChatFormatting.WHITE,
            "Raises movement speed."),
    SCHOLAR("Scholar", Slot.ARMOUR, 5, ChatFormatting.AQUA,
            "Raises intelligence."),
    HAGGLER("Haggler", Slot.ARMOUR, 4, ChatFormatting.GOLD,
            "The bazaar and collectors both pay you more."),
    PATRON("Patron", Slot.ARMOUR, 3, ChatFormatting.LIGHT_PURPLE,
            "Commissions grant extra standing."),

    // ── Tools: gathering ──────────────────────────────────────────────────
    VEINSEEKER("Veinseeker", Slot.TOOL, 4, ChatFormatting.AQUA,
            "Breaking an ore breaks the whole seam."),
    EFFICIENT_BITE("Efficient Bite", Slot.TOOL, 5, ChatFormatting.YELLOW,
            "Breaks blocks considerably faster."),
    SMELTER("Smelter", Slot.TOOL, 1, ChatFormatting.GOLD,
            "Ore comes out already smelted."),
    COMPACTOR("Compactor", Slot.TOOL, 1, ChatFormatting.LIGHT_PURPLE,
            "Loose drops compact themselves into blocks."),
    HARVESTER("Harvester", Slot.TOOL, 4, ChatFormatting.GREEN,
            "Breaking a crop harvests everything around it."),
    LUMBERJACK("Lumberjack", Slot.TOOL, 4, ChatFormatting.DARK_GREEN,
            "Felling one log fells the tree."),
    EXCAVATOR("Excavator", Slot.TOOL, 3, ChatFormatting.GRAY,
            "Digs a wide face in one swing."),
    PROSPECTORS_EYE("Prospector's Eye", Slot.TOOL, 3, ChatFormatting.AQUA,
            "Rare ore pays additional drops."),
    REPLANTER("Replanter", Slot.TOOL, 1, ChatFormatting.GREEN,
            "Harvested crops replant themselves."),
    SIFTER("Sifter", Slot.TOOL, 4, ChatFormatting.GOLD,
            "Gravel, sand and dirt yield far more than they should."),
    TUNNELLER("Tunneller", Slot.TOOL, 3, ChatFormatting.WHITE,
            "Lights the tunnel behind you as you dig."),
    SALVAGER("Salvager", Slot.TOOL, 4, ChatFormatting.LIGHT_PURPLE,
            "Broken blocks occasionally yield refined materials."),
    COLLECTORS_TOUCH("Collector's Touch", Slot.TOOL, 5, ChatFormatting.AQUA,
            "Everything you break counts double toward collections."),
    UNBREAKING_WILL("Unbreaking Will", Slot.TOOL, 5, ChatFormatting.GRAY,
            "The tool takes far less wear."),

    // ── Ranged ────────────────────────────────────────────────────────────
    HEARTSEEKER("Heartseeker", Slot.BOW, 5, ChatFormatting.RED,
            "Arrows curve toward what you were aiming at."),
    VOLLEY("Volley", Slot.BOW, 3, ChatFormatting.YELLOW,
            "Fires additional arrows in a spread."),
    PENETRATOR("Penetrator", Slot.BOW, 4, ChatFormatting.WHITE,
            "Arrows pass through and keep going."),
    SNIPER("Sniper", Slot.BOW, 5, ChatFormatting.AQUA,
            "Damage rises sharply with distance."),
    QUICKDRAW("Quickdraw", Slot.BOW, 4, ChatFormatting.GREEN,
            "Draws far quicker."),
    EXPLOSIVE_TIP("Explosive Tip", Slot.BOW, 3, ChatFormatting.GOLD,
            "Arrows burst on landing."),
    ENDLESS_QUIVER("Endless Quiver", Slot.BOW, 1, ChatFormatting.LIGHT_PURPLE,
            "Occasionally does not consume the arrow."),

    // ── Universal, applies to anything ────────────────────────────────────
    SOULBOUND("Soulbound", Slot.ANY, 1, ChatFormatting.LIGHT_PURPLE,
            "Stays with you through death."),
    EXPERIENCED("Experienced", Slot.ANY, 5, ChatFormatting.GREEN,
            "Everything grants additional experience."),
    FORTUNATE("Fortunate", Slot.ANY, 5, ChatFormatting.GOLD,
            "Raises Magic Find while carried."),
    WEIGHTLESS("Weightless", Slot.ANY, 1, ChatFormatting.WHITE,
            "This piece never counts against you."),
    RESONANT("Resonant", Slot.ANY, 4, ChatFormatting.AQUA,
            "Every other enchantment on this item is slightly stronger."),
    INHERITANCE("Inheritance", Slot.ANY, 3, ChatFormatting.YELLOW,
            "Passes a share of its enchantments on when reforged."),
    TEMPERED("Tempered", Slot.ANY, 5, ChatFormatting.GRAY,
            "Raises every base stat on the item a little."),
    STARLIT("Starlit", Slot.ANY, 3, ChatFormatting.LIGHT_PURPLE,
            "Glows, and is worth more to a collector."),
    OMEN("Omen", Slot.ANY, 4, ChatFormatting.DARK_PURPLE,
            "Rare drops announce one tier louder than they are.");

    /** What an enchantment will go on. */
    public enum Slot {
        WEAPON("Weapon", ChatFormatting.RED),
        ARMOUR("Armour", ChatFormatting.BLUE),
        TOOL("Tool", ChatFormatting.YELLOW),
        BOW("Ranged", ChatFormatting.GREEN),
        ANY("Anything", ChatFormatting.LIGHT_PURPLE);

        public final String label;
        public final ChatFormatting colour;

        Slot(String label, ChatFormatting colour) {
            this.label = label;
            this.colour = colour;
        }
    }

    public final String display;
    public final Slot slot;
    public final int maxLevel;
    public final ChatFormatting colour;
    public final String effect;

    CustomEnchant(String display, Slot slot, int maxLevel, ChatFormatting colour, String effect) {
        this.display = display;
        this.slot = slot;
        this.maxLevel = maxLevel;
        this.colour = colour;
        this.effect = effect;
    }

    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** Roman numerals, because that is how an enchantment reads. */
    public static String roman(int n) {
        return switch (n) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            case 10 -> "X";
            default -> String.valueOf(n);
        };
    }

    /** The exact lore line this enchantment writes, which is also what the parser reads back. */
    public String loreLine(int level) {
        return "§9" + display + " " + roman(Math.max(1, Math.min(maxLevel, level)));
    }

    /** What applying one costs, which rises steeply with level. */
    public long costFor(int level) {
        int lv = Math.max(1, Math.min(maxLevel, level));
        long base = switch (slot) {
            case ANY -> 40_000L;
            case WEAPON, ARMOUR -> 25_000L;
            case TOOL, BOW -> 18_000L;
        };
        return base * lv * lv;
    }

    /**
     * The icon the menu draws this with.
     *
     * <p>Derived from the slot rather than set per enchantment, because eighty five hand picked
     * icons would be eighty five things to get wrong and the colour on the name already carries
     * which one it is.</p>
     */
    public net.minecraft.world.item.Item icon() {
        return switch (slot) {
            case WEAPON -> net.minecraft.world.item.Items.IRON_SWORD;
            case ARMOUR -> net.minecraft.world.item.Items.IRON_CHESTPLATE;
            case TOOL -> net.minecraft.world.item.Items.IRON_PICKAXE;
            case BOW -> net.minecraft.world.item.Items.ARROW;
            case ANY -> net.minecraft.world.item.Items.ENCHANTED_BOOK;
        };
    }

    public static CustomEnchant byKey(String key) {
        if (key == null) return null;
        for (CustomEnchant e : values()) {
            if (e.key().equalsIgnoreCase(key)) return e;
        }
        return null;
    }

    public static CustomEnchant byDisplay(String display) {
        if (display == null) return null;
        for (CustomEnchant e : values()) {
            if (e.display.equalsIgnoreCase(display)) return e;
        }
        return null;
    }
}
