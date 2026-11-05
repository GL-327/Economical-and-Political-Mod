package com.political.core.config;

/**
 * Switches for the systems that add their own mobs and events on top of the base game. They are off by default
 * (the server plays close to vanilla Minecraft): mob scaling, the skyblock stat and skill systems, slayers, the economy,
 * politics and the custom tools, armor and weapons stay on and have no switch here. Turning a switch on restores the
 * system as it was; nothing was removed from the code.
 */
public final class FeatureSettings {

    private FeatureSettings() {
    }

    public static final ConfigKey<Boolean> WORLD_BOSS = ConfigKey.bool(
            "features.world_boss", false,
            "Timed world bosses that spawn in the overworld on their own. Admins can still spawn one by hand.");

    public static final ConfigKey<Boolean> NEMESIS = ConfigKey.bool(
            "features.nemesis", false,
            "The nemesis system: a mob that kills a player becomes a named grudge that hunts them.");

    public static final ConfigKey<Boolean> INCURSIONS = ConfigKey.bool(
            "features.incursions", false,
            "Incursions: rifts a player can open that spawn waves of custom mobs.");

    public static final ConfigKey<Boolean> GREAT_HUNT = ConfigKey.bool(
            "features.great_hunt", false,
            "The Great Hunt and the other living world events that spawn named custom mobs.");

    public static final ConfigKey<Boolean> WANDERING_SPIRITS = ConfigKey.bool(
            "features.wandering_spirits", false,
            "World modifiers such as the Wandering Spirits, which add roaming custom mobs and weather style events.");
}
