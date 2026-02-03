package com.political.mixin;

import com.political.PerkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.spawner.PatrolSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PatrolSpawner.class)
public class PatrolSpawnerMixin {

    @Shadow
    private int cooldown;

    @Inject(method = "spawn", at = @At("HEAD"))
    private void political_modifyPatrolCooldown(ServerWorld world, boolean spawnMonsters, CallbackInfo ci) {
        if (PerkManager.hasActivePerk("REDUCED_PATROLS")) {
            // Reduce cooldown to spawn patrols more frequently
            if (this.cooldown > 100) {
                this.cooldown = (int)(this.cooldown * 0.5f);
            }
        }
    }
}