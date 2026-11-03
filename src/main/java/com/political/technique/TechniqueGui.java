package com.political.technique;

import com.political.power.game.PowerScreens;
import net.minecraft.server.level.ServerPlayer;

/**
 * The old entry point of the technique screen. Every menu that links to techniques still calls this, and it now
 * opens the power hub ({@link PowerScreens}), which shows techniques, abilities, the school and its tree, and vows.
 */
public final class TechniqueGui {

    private TechniqueGui() {}

    public static void open(ServerPlayer player) {
        PowerScreens.open(player);
    }
}
