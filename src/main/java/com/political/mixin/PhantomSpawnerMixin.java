package com.political.mixin;

import com.political.PerkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.spawner.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PhantomSpawner.class)
public class PhantomSpawnerMixin {

    @Inject(method = "spawn", at = @At("HEAD"), cancellable = true)
    private void political_preventPhantomSpawn(ServerWorld world, boolean spawnMonsters, CallbackInfo ci) {
        if (PerkManager.shouldPreventPhantoms()) {
            ci.cancel(); // Prevent any phantoms from spawning
        }
    }
}