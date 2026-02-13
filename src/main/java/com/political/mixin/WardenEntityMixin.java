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
            } else if (!warden.getEntityWorld().isClient()) {
                var nearestPlayer = warden.getEntityWorld().getClosestPlayer(warden, 50);
                if (nearestPlayer != null) {
                    warden.increaseAngerAt(nearestPlayer, 100, true);
                    warden.setTarget(nearestPlayer);
                }
            }
        }
    }
}