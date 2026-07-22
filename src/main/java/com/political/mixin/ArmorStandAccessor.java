package com.political.mixin;

import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Exposes ArmorStand#setMarker (private in 26.1) for damage-hologram stands. */
@Mixin(ArmorStand.class)
public interface ArmorStandAccessor {
    @Invoker("setMarker")
    void political_setMarker(boolean marker);
}
