package com.political.mixin;

import com.political.SlayerManager;
import net.minecraft.entity.mob.WardenEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WardenEntity.class)
public class WardenEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void keepBossWardenActive(CallbackInfo ci) {
        WardenEntity warden = (WardenEntity) (Object) this;

        if (SlayerManager.isSlayerBoss(warden.getUuid())) {
            // Force warden to stay emerged by keeping anger high
            if (warden.getTarget() != null) {
                warden.increaseAngerAt(warden.getTarget(), 50, true);
            } else {
                // Keep warden aggressive but don't force underground retreat
                // Only retreat if health is critically low
                if (warden.getHealth() <= warden.getMaxHealth() * 0.2) {
                    warden.increaseAngerAt(warden.getTarget(), 25, true);
                } else {
                    warden.increaseAngerAt(warden.getTarget(), 15, true);
                }
            }
        }
    }
}