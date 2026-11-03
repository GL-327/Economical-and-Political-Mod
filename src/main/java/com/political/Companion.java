package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * Companions. One walks with you at a time, so picking a companion is picking a playstyle rather
 * than collecting another pile of buffs.
 *
 * <p>Each species is a dressed vanilla mob with a role, a growth curve and one signature ability
 * that fires on a cooldown. A companion is meant to feel like help, never like a second player.</p>
 */
public enum Companion {

    // ── Damage ────────────────────────────────────────────────────────────
    EMBER_PUP("Ember Pup", EntityTypes.WOLF, Items.FIRE_CHARGE, ChatFormatting.GOLD,
            Role.DAMAGE, Rarity.COMMON, "Sets what it bites alight.", 12.0, 3.0),
    DIRE_WOLF("Dire Wolf", EntityTypes.WOLF, Items.BONE, ChatFormatting.GRAY,
            Role.DAMAGE, Rarity.COMMON, "Runs down anything that flees.", 16.0, 4.0),
    SHADOW_CAT("Shadow Cat", EntityTypes.CAT, Items.DYE.black(), ChatFormatting.DARK_GRAY,
            Role.DAMAGE, Rarity.UNCOMMON, "Strikes from where you were not looking.", 10.0, 5.0),
    STING_WASP("Sting Wasp", EntityTypes.BEE, Items.HONEYCOMB, ChatFormatting.YELLOW,
            Role.DAMAGE, Rarity.COMMON, "Leaves venom in every wound.", 8.0, 3.0),
    BASALT_HOUND("Basalt Hound", EntityTypes.WOLF, Items.BASALT, ChatFormatting.DARK_RED,
            Role.DAMAGE, Rarity.RARE, "Born in the deep heat and still burning.", 20.0, 6.0),
    VOID_STALKER("Void Stalker", EntityTypes.CAT, Items.ENDER_EYE, ChatFormatting.DARK_PURPLE,
            Role.DAMAGE, Rarity.EPIC, "Blinks to your target before you do.", 18.0, 7.0),

    // ── Tank ──────────────────────────────────────────────────────────────
    IRON_GUARDIAN("Iron Guardian", EntityTypes.IRON_GOLEM, Items.IRON_BLOCK, ChatFormatting.GRAY,
            Role.TANK, Rarity.RARE, "Stands in front of you without being asked.", 40.0, 4.0),
    STONE_WARD("Stone Ward", EntityTypes.IRON_GOLEM, Items.STONE, ChatFormatting.WHITE,
            Role.TANK, Rarity.UNCOMMON, "Slow, patient and very hard to move.", 32.0, 3.0),
    SHELL_BROTHER("Shell Brother", EntityTypes.TURTLE, Items.TURTLE_HELMET, ChatFormatting.GREEN,
            Role.TANK, Rarity.COMMON, "Takes the hit so you do not.", 24.0, 2.0),
    BASTION_GOLEM("Bastion Golem", EntityTypes.IRON_GOLEM, Items.NETHERITE_INGOT, ChatFormatting.DARK_GRAY,
            Role.TANK, Rarity.EPIC, "A wall that follows you around.", 55.0, 6.0),

    // ── Support ───────────────────────────────────────────────────────────
    MENDING_ALLAY("Mending Allay", EntityTypes.ALLAY, Items.GLISTERING_MELON_SLICE, ChatFormatting.AQUA,
            Role.SUPPORT, Rarity.RARE, "Closes your wounds while you work.", 10.0, 1.0),
    LANTERN_WISP("Lantern Wisp", EntityTypes.ALLAY, Items.LANTERN, ChatFormatting.YELLOW,
            Role.SUPPORT, Rarity.UNCOMMON, "Keeps the dark off you.", 8.0, 1.0),
    ARCANE_FAMILIAR("Arcane Familiar", EntityTypes.ALLAY, Items.AMETHYST_SHARD, ChatFormatting.LIGHT_PURPLE,
            Role.SUPPORT, Rarity.EPIC, "Feeds your mana back to you.", 12.0, 1.0),
    CHOIR_PARROT("Choir Parrot", EntityTypes.PARROT, Items.FEATHER, ChatFormatting.GREEN,
            Role.SUPPORT, Rarity.COMMON, "Its song puts strength in your arms.", 8.0, 1.0),

    // ── Forager ───────────────────────────────────────────────────────────
    TRUFFLE_PIG("Truffle Pig", EntityTypes.PIG, Items.CARROT, ChatFormatting.RED,
            Role.FORAGER, Rarity.COMMON, "Noses out things worth keeping.", 12.0, 1.0),
    PACK_MULE("Pack Mule", EntityTypes.MULE, Items.CHEST, ChatFormatting.GOLD,
            Role.FORAGER, Rarity.UNCOMMON, "Carries the takings home.", 20.0, 2.0),
    PROSPECT_FOX("Prospect Fox", EntityTypes.FOX, Items.RAW_GOLD, ChatFormatting.GOLD,
            Role.FORAGER, Rarity.RARE, "Digs up coin from nothing at all.", 14.0, 2.0),
    GLOW_SQUIRE("Glow Squire", EntityTypes.GLOW_SQUID, Items.GLOW_INK_SAC, ChatFormatting.AQUA,
            Role.FORAGER, Rarity.UNCOMMON, "Finds what the dark was hiding.", 12.0, 1.0),

    // ── Scout ─────────────────────────────────────────────────────────────
    SCOUT_HAWK("Scout Hawk", EntityTypes.PARROT, Items.SPYGLASS, ChatFormatting.WHITE,
            Role.SCOUT, Rarity.COMMON, "Marks what is coming before it arrives.", 8.0, 2.0),
    FROST_HARE("Frost Hare", EntityTypes.RABBIT, Items.SNOWBALL, ChatFormatting.AQUA,
            Role.SCOUT, Rarity.UNCOMMON, "Quick enough to drag you along with it.", 8.0, 1.0),
    CURSE_MOTH("Curse Moth", EntityTypes.BAT, Items.PHANTOM_MEMBRANE, ChatFormatting.DARK_PURPLE,
            Role.SCOUT, Rarity.RARE, "Drawn to cursed things, and warns you of them.", 10.0, 2.0),
    STAR_OWL("Star Owl", EntityTypes.PARROT, Items.NETHER_STAR, ChatFormatting.LIGHT_PURPLE,
            Role.SCOUT, Rarity.LEGENDARY, "Sees a little further than it should.", 16.0, 3.0);

    public enum Role {
        DAMAGE("Damage", ChatFormatting.RED),
        TANK("Tank", ChatFormatting.GRAY),
        SUPPORT("Support", ChatFormatting.GREEN),
        FORAGER("Forager", ChatFormatting.GOLD),
        SCOUT("Scout", ChatFormatting.AQUA);

        public final String label;
        public final ChatFormatting colour;

        Role(String label, ChatFormatting colour) {
            this.label = label;
            this.colour = colour;
        }
    }

    public enum Rarity {
        COMMON("Common", ChatFormatting.WHITE, 1.0),
        UNCOMMON("Uncommon", ChatFormatting.GREEN, 1.15),
        RARE("Rare", ChatFormatting.BLUE, 1.3),
        EPIC("Epic", ChatFormatting.DARK_PURPLE, 1.5),
        LEGENDARY("Legendary", ChatFormatting.GOLD, 1.75);

        public final String label;
        public final ChatFormatting colour;
        /** Multiplier on the companion's base health and damage. */
        public final double scale;

        Rarity(String label, ChatFormatting colour, double scale) {
            this.label = label;
            this.colour = colour;
            this.scale = scale;
        }
    }

    public final String display;
    public final EntityType<?> base;
    public final Item icon;
    public final ChatFormatting colour;
    public final Role role;
    public final Rarity rarity;
    public final String flavor;
    public final double baseHealth;
    public final double baseDamage;

    Companion(String display, EntityType<?> base, Item icon, ChatFormatting colour,
              Role role, Rarity rarity, String flavor, double baseHealth, double baseDamage) {
        this.display = display;
        this.base = base;
        this.icon = icon;
        this.colour = colour;
        this.role = role;
        this.rarity = rarity;
        this.flavor = flavor;
        this.baseHealth = baseHealth;
        this.baseDamage = baseDamage;
    }

    public String id() {
        return "companion_" + name().toLowerCase(java.util.Locale.ROOT);
    }

    /** Health at a given level. Levels are capped, so this cannot run away. */
    public double healthAt(int level) {
        return (baseHealth + level * 1.5) * rarity.scale;
    }

    /** Damage at a given level, deliberately modest. */
    public double damageAt(int level) {
        return (baseDamage + level * 0.25) * rarity.scale;
    }

    public static Companion byId(String id) {
        if (id == null) return null;
        for (Companion c : values()) {
            if (c.id().equalsIgnoreCase(id) || c.name().equalsIgnoreCase(id)) return c;
        }
        return null;
    }
}
