package com.political.mixin;

import com.political.SlayerManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WardenEntity.class)
public abstract class WardenEntityMixin {

    // Inject every tick to maintain boss state
    @Inject(method = "tick", at = @At("TAIL"))
    private void keepBossWardenActive(CallbackInfo ci) {
        WardenEntity warden = (WardenEntity) (Object) this;

        if (SlayerManager.isSlayerBoss(warden.getUuid())) {
            // Get current target
            LivingEntity target = warden.getTarget();
            
            // Always maintain maximum anger to prevent ANY digging behavior
            if (target != null && target.isAlive()) {
                // Keep anger at max (150 is the cap for Warden)
                if (warden.getAnger() < 150) {
                    warden.increaseAngerAt(target, 150, true);
                }
            } else {
                // No target - find the boss owner and set as target
                String ownerUuid = SlayerManager.getBossOwner(warden.getUuid());
                if (ownerUuid != null && warden.getEntityWorld() instanceof ServerWorld world) {
                    ServerPlayerEntity owner = world.getServer().getPlayerManager().getPlayer(java.util.UUID.fromString(ownerUuid));
                    if (owner != null && owner.isAlive()) {
                        warden.setTarget(owner);
                        warden.increaseAngerAt(owner, 150, true);
                    }
                }
            }
            
            // Force persistence to prevent despawn
            warden.setPersistent();
        }
    }
    
    // Prevent the warden from ever removing suspects from its anger list
    @Inject(method = "removeSuspect", at = @At("HEAD"), cancellable = true)
    private void preventTargetLoss(Entity suspect, CallbackInfo ci) {
        WardenEntity warden = (WardenEntity) (Object) this;
        
        if (SlayerManager.isSlayerBoss(warden.getUuid())) {
            ci.cancel();
        }
    }
}