package com.political.mixin;

import com.political.SlayerItems;
import com.political.SlayerManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(LivingEntity.class)
public class WardenDropMixin {

    private static final Random random = new Random();

    @Inject(
            method = "dropAllDeathLoot(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;)V",
            at = @At("HEAD"))
    private void political_wardenDrop(ServerLevel world, DamageSource source, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (self instanceof Warden && source.getEntity() instanceof Player) {
            // ~1 in 200k (rarer bonus core)
            if (random.nextInt(200_000) == 0) {
                self.spawnAtLocation(world, SlayerItems.createCore(SlayerManager.SlayerType.IRON_GOLEM));
            }
        }
    }
}