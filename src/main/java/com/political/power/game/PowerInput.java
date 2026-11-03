package com.political.power.game;

import com.political.core.text.Text;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The one cast input (program 10.7): sneak and press the swap-hands key (F by default), handled by
 * {@code SwapHandsCastMixin}. The earlier gesture, a right click in the air with an empty hand, cannot work on a
 * vanilla client: the client sends a use-item packet only for a hand that holds something, so the server never
 * hears of it ({@code Minecraft.startUseItem} guards the call with {@code !heldItem.isEmpty()}). The
 * {@code /technique cast} command stays as a second way to cast the selected ability.
 */
public final class PowerInput {

    private static final Logger LOGGER = LoggerFactory.getLogger(PowerInput.class);

    private PowerInput() {
    }

    /**
     * @return true when the key press was a cast attempt and the swap must not happen; false when the player is not
     *         sneaking or has no technique selected, so the swap carries on as in vanilla
     */
    public static boolean onSwapHands(ServerPlayer player) {
        if (!player.isShiftKeyDown()) {
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
