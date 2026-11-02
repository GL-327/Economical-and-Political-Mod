package com.political.mixin;

import com.political.government.PerkManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PatrolSpawner.class)
public class PatrolSpawnerMixin {

    @Shadow
    private int nextTick;

    @Inject(method = "tick", at = @At("HEAD"))
    private void political_modifyPatrolCooldown(ServerLevel world, boolean spawnMonsters, CallbackInfo ci) {
        if (PerkManager.hasActivePerk("REDUCED_PATROLS")) {
            if (this.nextTick > 100) {
                this.nextTick = (int)(this.nextTick * 0.5f);
            }
        }
    }
}