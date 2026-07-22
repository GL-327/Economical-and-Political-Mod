package com.political.mixin;

import com.political.BestiaryManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Wires the Bestiary into the damage pipeline.
 *
 * <p>Two independent jobs:</p>
 * <ol>
 *   <li><b>Apply the bonus.</b> A {@code @ModifyVariable} at HEAD scales player-sourced
 *       outgoing damage by {@link BestiaryManager#damageBonus}. This chains cleanly with the
 *       existing {@code LivingEntityDamageMixin} modifier — mixin applies them in sequence,
 *       so the boss/armour logic there is untouched.</li>
 *   <li><b>Feed the ledger.</b> An {@code @Inject} at RETURN records how much health the hit
 *       actually removed, which is what {@link BestiaryManager} uses to decide whether a kill
 *       was really the player's work. Recording the health delta rather than the requested
 *       damage means armour, resistance and absorption are all accounted for, and a player
 *       cannot inflate their contribution by spamming zero-effect hits.</li>
 * </ol>
 */
@Mixin(LivingEntity.class)
public abstract class BestiaryDamageMixin {

    @Unique
    private float livingworld_healthBefore = -1f;

    @ModifyVariable(
            method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private float livingworld_applyBestiaryBonus(float amount, ServerLevel world, DamageSource source) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof ServerPlayer) return amount;
        if (amount <= 0f) return amount;
        if (!(source.getEntity() instanceof ServerPlayer attacker)) return amount;

        try {
            double mult = BestiaryManager.damageBonus(attacker, self);
            if (mult <= 1.0) return amount;
            return (float) (amount * mult);
        } catch (Throwable t) {
            return amount;
        }
    }

    @Inject(method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At("HEAD"))
    private void livingworld_captureHealth(ServerLevel world, DamageSource source, float amount,
                                           CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        livingworld_healthBefore = (self instanceof ServerPlayer) ? -1f : self.getHealth();
    }

    @Inject(method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At("RETURN"))
    private void livingworld_recordContribution(ServerLevel world, DamageSource source, float amount,
                                                CallbackInfoReturnable<Boolean> cir) {
        float before = livingworld_healthBefore;
        livingworld_healthBefore = -1f;

        if (before < 0f) return;
        if (!Boolean.TRUE.equals(cir.getReturnValue())) return;
        if (!(source.getEntity() instanceof ServerPlayer attacker)) return;

        LivingEntity self = (LivingEntity) (Object) this;
        float dealt = before - self.getHealth();
        if (dealt <= 0f) return;

        try {
            BestiaryManager.recordDamage(attacker, self, dealt);
        } catch (Throwable ignored) {
            // Never let bookkeeping break a hit.
        }
    }
}
