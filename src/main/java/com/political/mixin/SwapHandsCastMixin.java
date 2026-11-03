package com.political.mixin;

import com.political.power.game.PowerInput;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The cast input of the power system: sneak and press the swap-hands key (F by default). A vanilla client sends no
 * packet for a right click in the air with an empty hand, so the only keys that reach the server whatever the
 * player holds are the ones that send a player action. Swap-hands sends {@code SWAP_ITEM_WITH_OFFHAND} every time,
 * which this mixin turns into a cast when the player sneaks and has a technique equipped; otherwise the swap
 * happens as usual. Nothing runs on the network thread: the packet is handled again on the server thread, where
 * this injection acts.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class SwapHandsCastMixin {

    @Shadow
    public ServerPlayer player;

    @Inject(method = "handlePlayerAction", at = @At("HEAD"), cancellable = true)
    private void political_castOnSwapHands(ServerboundPlayerActionPacket packet, CallbackInfo ci) {
        if (packet.getAction() != ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND) {
            return;
        }
        if (!this.player.level().getServer().isSameThread()) {
            return;
        }
        if (PowerInput.onSwapHands(this.player)) {
            ci.cancel();
        }
    }
}
