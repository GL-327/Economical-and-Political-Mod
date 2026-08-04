package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * The seven floors, in the order you are meant to meet them.
 *
 * <p>Linear on purpose. Floor one is open to anybody and every floor after it wants the one before
 * it cleared, so the progression is a ladder rather than a menu. That is the part of the Skyblock
 * design worth taking: the delve is not a place you visit, it is a thing you climb.</p>
 *
 * <p>Each floor draws its rooms from sources that share a look, so a run reads as one building
 * rather than a shuffle of everything on disk. The room lists themselves live in
 * {@code delve_rooms.json} beside the structures, because seven hundred filenames are data and
 * have no business being typed into a class file.</p>
 */
public enum DelveDepth {

    CATACOMBS(1, "The Catacombs", "catacombs", ChatFormatting.GRAY,
            "Somebody was buried here properly. Most of the others were not.",
            Blocks.POLISHED_DEEPSLATE, Blocks.DEEPSLATE_BRICKS, Blocks.SOUL_LANTERN,
            EntityType.SKELETON, EntityType.ZOMBIE,
            "The Gravewright", Items.BONE, 1_600f, 30_000L),

    SUNKEN(2, "The Sunken Halls", "sunken", ChatFormatting.DARK_AQUA,
            "The water came in a long time ago and never left.",
            Blocks.PRISMARINE_BRICKS, Blocks.DARK_PRISMARINE, Blocks.SEA_LANTERN,
            EntityType.DROWNED, EntityType.GUARDIAN,
            "The Tidewarden", Items.HEART_OF_THE_SEA, 2_800f, 55_000L),

    ICE(3, "The Ice Vault", "ice", ChatFormatting.AQUA,
            "Everything in here was put away for keeping.",
            Blocks.PACKED_ICE, Blocks.BLUE_ICE, Blocks.SEA_LANTERN,
            EntityType.STRAY, EntityType.POLAR_BEAR,
            "The Hoarfrost Keeper", Items.BLUE_ICE, 4_500f, 90_000L),

    SANDS(4, "The Buried Sands", "sands", ChatFormatting.YELLOW,
            "A city that lost an argument with the desert.",
            Blocks.CUT_SANDSTONE, Blocks.SMOOTH_SANDSTONE, Blocks.SHROOMLIGHT,
            EntityType.HUSK, EntityType.SILVERFISH,
            "The Sand Vizier", Items.GOLD_BLOCK, 7_000f, 140_000L),

    TOXIC(5, "The Toxic Lair", "toxic", ChatFormatting.DARK_GREEN,
            "Something has been living down here, and it has been eating.",
            Blocks.MOSS_BLOCK, Blocks.MUDDY_MANGROVE_ROOTS, Blocks.OCHRE_FROGLIGHT,
            EntityType.CAVE_SPIDER, EntityType.WITCH,
            "The Blight Mother", Items.SLIME_BLOCK, 10_500f, 210_000L),

    CITADEL(6, "The Lone Citadel", "citadel", ChatFormatting.GOLD,
            "Held by somebody, against somebody, for a very long time.",
            Blocks.STONE_BRICKS, Blocks.CHISELED_STONE_BRICKS, Blocks.LANTERN,
            EntityType.PILLAGER, EntityType.VINDICATOR,
            "The Warden of the Keep", Items.NETHERITE_INGOT, 15_000f, 320_000L),

    END_CASTLE(7, "The End Castle", "endcastle", ChatFormatting.DARK_PURPLE,
            "It was not built here. It arrived.",
            Blocks.END_STONE_BRICKS, Blocks.PURPUR_BLOCK, Blocks.END_ROD,
            EntityType.ENDERMAN, EntityType.SHULKER,
            "The Hollow Sovereign", Items.DRAGON_EGG, 22_000f, 500_000L);

    /** Where this floor sits on the ladder, counting from one. */
    public final int tier;
    public final String display;
    /** Which pool in the manifest this floor pulls from. */
    public final String pool;
    public final ChatFormatting colour;
    public final String flavour;

    /** Corridor floor, corridor wall, and the light that goes in the ceiling. */
    public final Block corridorFloor;
    public final Block corridorWall;
    public final Block corridorLight;

    /** What wanders the rooms. Two kinds so a floor is not one silhouette repeated. */
    public final EntityType<?> commonMob;
    public final EntityType<?> eliteMob;

    public final String bossName;
    /** What the boss is rendered holding, which is also the floor's icon in the menu. */
    public final Item icon;
    public final float bossHealth;
    /** Coins for a clear at par. Score moves it a long way either side of this. */
    public final long baseReward;

    DelveDepth(int tier, String display, String pool, ChatFormatting colour, String flavour,
                 Block corridorFloor, Block corridorWall, Block corridorLight,
                 EntityType<?> commonMob, EntityType<?> eliteMob,
                 String bossName, Item icon, float bossHealth, long baseReward) {
        this.tier = tier;
        this.display = display;
        this.pool = pool;
        this.colour = colour;
        this.flavour = flavour;
        this.corridorFloor = corridorFloor;
        this.corridorWall = corridorWall;
        this.corridorLight = corridorLight;
        this.commonMob = commonMob;
        this.eliteMob = eliteMob;
        this.bossName = bossName;
        this.icon = icon;
        this.bossHealth = bossHealth;
        this.baseReward = baseReward;
    }

    /** The floor before this one, or null for the first. */
    public DelveDepth previous() {
        return tier <= 1 ? null : values()[tier - 2];
    }

    public static DelveDepth byTier(int tier) {
        for (DelveDepth f : values()) {
            if (f.tier == tier) return f;
        }
        return null;
    }

    /**
     * How many rooms this floor lays out, boss and entrance included.
     *
     * <p>Grows with depth so the later floors feel like a longer walk, and stops at thirty because
     * a six by six grid has thirty six cells and the layout wants room to breathe.</p>
     */
    public int roomCount() {
        return Math.min(30, 12 + tier * 2);
    }

    /** How many secrets are hidden on this floor. */
    public int secretCount() {
        return 3 + tier;
    }

    /** How hard the ordinary mobs hit, relative to their vanilla selves. */
    public float mobScale() {
        // Raised alongside the boss health. A player in marked delve gear takes roughly a fifth of
        // what they used to, so the old curve made the lower depths a walk by the time anybody had
        // the gear to reach the upper ones.
        return 1.0f + tier * 0.60f;
    }
}
