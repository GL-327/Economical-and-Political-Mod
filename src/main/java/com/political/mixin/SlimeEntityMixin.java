package com.political.mixin;

import com.political.BossAbilityManager;
import com.political.bounty.SlayerManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.cubemob.AbstractCubeMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractCubeMob.class)
public class SlimeEntityMixin {

    // Prevent vanilla slime splitting for slayer boss slimes
    // Splitting happens in remove(). In 26.3 that method lives on AbstractCubeMob, the shared parent of
    // Slime and MagmaCube, so the hook targets the parent and covers both.
    @Inject(method = "remove", at = @At("HEAD"), cancellable = true)
    private void preventVanillaSlimeSplit(Entity.RemovalReason reason, CallbackInfo ci) {
        AbstractCubeMob slime = (AbstractCubeMob) (Object) this;

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