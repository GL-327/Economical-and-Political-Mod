package com.political.core.tick;

/** How often a {@link TickService} runs inside one server tick. */
public enum TickScope {
    /** Once per due tick, with the server. */
    SERVER,
    /** Once per due tick for each loaded world, with the world. */
    WORLD,
    /** Once per due tick for each player in each world, with the player. */
    PLAYER
}
