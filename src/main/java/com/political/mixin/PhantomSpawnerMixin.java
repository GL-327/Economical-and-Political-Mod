package com.political.mixin;

import com.political.government.PerkManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PhantomSpawner.class)
public class PhantomSpawnerMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void political_preventPhantomSpawn(ServerLevel world, boolean spawnMonsters, CallbackInfo ci) {
        if (PerkManager.shouldPreventPhantoms()) {
            ci.cancel();
        }
    }
}