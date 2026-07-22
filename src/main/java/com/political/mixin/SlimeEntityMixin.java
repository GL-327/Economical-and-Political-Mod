package com.political.mixin;

import com.political.BossAbilityManager;
import com.political.SlayerManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Slime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Slime.class)
public class SlimeEntityMixin {

    // Prevent vanilla slime splitting for slayer boss slimes
    // In 1.21.11, slime splitting happens in the remove method
    @Inject(method = "remove", at = @At("HEAD"), cancellable = true)
    private void preventVanillaSlimeSplit(Entity.RemovalReason reason, CallbackInfo ci) {
        Slime slime = (Slime) (Object) this;

        // If this is a slayer boss or split slime, prevent vanilla splitting
        if (reason == Entity.RemovalReason.KILLED) {
            if (SlayerManager.isSlayerBoss(slime.getUUID()) || BossAbilityManager.isSlimeSplit(slime.getUUID())) {
                // Call the super remove without spawning children
                // We handle custom splitting in onSlimeBossDeath
                slime.setRemoved(reason);
                ci.cancel();
            }
        }
    }
}