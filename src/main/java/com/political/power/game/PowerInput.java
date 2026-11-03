package com.political.power.game;

import com.political.core.text.Text;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The one cast input (program 10.7): sneak and right click in the air with an empty main hand. It is called from
 * the first branch of the item use handler in {@code CustomItemHandler}; no other handler claims that combination.
 */
public final class PowerInput {

    private static final Logger LOGGER = LoggerFactory.getLogger(PowerInput.class);

    private PowerInput() {
    }

    /**
     * @return true when the click was a cast attempt and must not go on to other handlers; false when the player is
     *         not sneaking, holds something, or has no technique selected, so the click carries on as before
     */
    public static boolean onUse(ServerPlayer player) {
        if (!player.isShiftKeyDown() || !player.getMainHandItem().isEmpty()) {
            return false;
        }
        if (PowerService.selected(player.getUUID().toString()) == null) {
            return false;
        }
        try {
            PowerService.cast(player);
        } catch (Exception e) {
            LOGGER.error("Power cast crashed for player {}", player.getUUID(), e);
            player.sendSystemMessage(Text.tr("political.error.generic"), true);
        }
        return true;
    }
}
