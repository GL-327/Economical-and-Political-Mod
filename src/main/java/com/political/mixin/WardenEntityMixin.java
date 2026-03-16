package com.political.mixin;

import com.political.SlayerManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.WardenEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WardenEntity.class)
public abstract class WardenEntityMixin {

    @Shadow public abstract void increaseAngerAt(LivingEntity entity, int amount, boolean listening);
    @Shadow public abstract LivingEntity getTarget();
    @Shadow public abstract int getAnger();
    @Shadow public abstract void setPersistent();

    @Inject(method = "tick", at = @At("HEAD"))
    private void keepBossWardenActive(CallbackInfo ci) {
        WardenEntity warden = (WardenEntity) (Object) this;

        if (SlayerManager.isSlayerBoss(warden.getUuid())) {
            // Ensure persistence
            warden.setPersistent();
            
            LivingEntity target = warden.getTarget();
            int currentAnger = warden.getAnger();
            
            // Keep anger high to prevent digging behavior
            // Warden digs underground when anger is low and no target
            if (target != null) {
                // Aggressive mode - max anger toward target
                if (currentAnger < 100) {
                    increaseAngerAt(target, 100, true);
                }
            } else {
                // No target - maintain moderate anger to stay active
                // Try to find nearby players via anger simulation
                warden.increaseAngerAt(null, 50, false);
            }
            
            // Prevent warden from going underground by teleporting back up
            if (warden.getY() < 50) {
                warden.teleport(warden.getX(), 60, warden.getZ(), false);
            }
        }
    }
}