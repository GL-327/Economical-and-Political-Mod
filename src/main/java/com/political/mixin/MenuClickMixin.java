package com.political.mixin;

import com.political.core.gui.MenuTrail;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Tells {@link MenuTrail} which slot a player clicked in a menu, before the menu reacts to it, so the trail can tell a click on a Back
 * button (the player is going back) from a click on anything else that opens another menu (the player is going forward). It only
 * reads; the click is handled as usual. The low priority number makes it run before the menu library's own handler of the same packet.
 */
@Mixin(value = ServerGamePacketListenerImpl.class, priority = 500)
public abstract class MenuClickMixin {

    @Shadow
    public ServerPlayer player;

    @Inject(method = "handleContainerClick", at = @At("HEAD"))
    private void political_noteMenuClick(ServerboundContainerClickPacket packet, CallbackInfo ci) {
        if (!this.player.level().getServer().isSameThread()) {
            return;
        }
        MenuTrail.onClick(this.player, packet.slotNum());
    }
}
