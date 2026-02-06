package com.political.mixin;

import com.political.CustomItemHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(LivingEntity.class)
public class WardenDropMixin {

    private static final Random random = new Random();

    @Inject(method = "dropLoot", at = @At("HEAD"))
    private void political_wardenDrop(ServerWorld world, DamageSource source, boolean causedByPlayer, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (self instanceof WardenEntity && source.getAttacker() instanceof PlayerEntity) {
            // 0.1% chance = 1 in 1000
            if (random.nextInt(1000) == 0) {
                self.dropStack(world, CustomItemHandler.createWardenCore());
            }
        }
    }
}