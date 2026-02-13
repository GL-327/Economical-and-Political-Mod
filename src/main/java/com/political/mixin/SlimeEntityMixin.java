package com.political.mixin;

import com.political.BossAbilityManager;
import com.political.SlayerManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.SlimeEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SlimeEntity.class)
public class SlimeEntityMixin {

    // Prevent vanilla slime splitting for slayer boss slimes
    // In 1.21.11, slime splitting happens in the remove method
    @Inject(method = "remove", at = @At("HEAD"), cancellable = true)
    private void preventVanillaSlimeSplit(Entity.RemovalReason reason, CallbackInfo ci) {
        SlimeEntity slime = (SlimeEntity) (Object) this;

        // If this is a slayer boss or split slime, prevent vanilla splitting
        if (reason == Entity.RemovalReason.KILLED) {
            if (SlayerManager.isSlayerBoss(slime.getUuid()) || BossAbilityManager.isSlimeSplit(slime.getUuid())) {
                // Call the super remove without spawning children
                // We handle custom splitting in onSlimeBossDeath
                slime.setRemoved(reason);
                ci.cancel();
            }
        }
    }
}