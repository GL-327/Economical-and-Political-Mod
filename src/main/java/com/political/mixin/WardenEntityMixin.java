package com.political.mixin;

import com.political.bounty.SlayerManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Warden.class)
public abstract class WardenEntityMixin {

    // Inject every tick to maintain boss state
    @Inject(method = "tick", at = @At("TAIL"))
    private void keepBossWardenActive(CallbackInfo ci) {
        Warden warden = (Warden) (Object) this;

        if (SlayerManager.isSlayerBoss(warden.getUUID())) {
            // Get current target
            LivingEntity target = warden.getTarget();
            
            // Always maintain maximum anger to prevent ANY digging behavior
            if (target != null && target.isAlive()) {
                warden.increaseAngerAt(target, 150, true);
            } else {
                // No target - find the boss owner and set as target
                String ownerUuid = SlayerManager.getBossOwner(warden.getUUID());
                if (ownerUuid != null && warden.level() instanceof ServerLevel world) {
                    ServerPlayer owner = world.getServer().getPlayerList().getPlayer(java.util.UUID.fromString(ownerUuid));
                    if (owner != null && owner.isAlive()) {
                        warden.setTarget(owner);
                        warden.increaseAngerAt(owner, 150, true);
                    }
                }
            }
            
            // Force persistence to prevent despawn
            warden.setPersistenceRequired();
        }
    }
    
    // Prevent the warden from ever clearing anger for a suspect (Yarn: removeSuspect)
    @Inject(method = "clearAnger", at = @At("HEAD"), cancellable = true)
    private void preventTargetLoss(Entity suspect, CallbackInfo ci) {
        Warden warden = (Warden) (Object) this;
        
        if (SlayerManager.isSlayerBoss(warden.getUUID())) {
            ci.cancel();
        }
    }
}