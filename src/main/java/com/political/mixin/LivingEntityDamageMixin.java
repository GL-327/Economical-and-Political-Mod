package com.political.mixin;

import com.political.items.ArmorAbilityHandler;
import com.political.BossAbilityManager;
import com.political.MasteryManager;
import com.political.bounty.SlayerManager;
import com.political.WorldBossManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDamageMixin {

    @Unique
    private float political_wbHealthBefore;

    @Unique
    private float political_healthBeforeArmorXp = -1f;

    @ModifyVariable(
            method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private float modifyDamageForBoss(float amount, ServerLevel world, DamageSource source) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (SlayerManager.isSlayerBoss(self.getUUID())) {
            return BossAbilityManager.modifyIncomingDamage(self.getUUID(), amount);
        }

        // Void Dragon Chestplate damage immunity
        if (self instanceof ServerPlayer player) {
            amount = ArmorAbilityHandler.onVoidDragonDamage(player, source, amount);
        }

        return amount;
    }

    @Inject(method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z", at = @At("HEAD"))
    private void political_captureWorldBossHealth(ServerLevel world, DamageSource source, float amount,
                                                  CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (WorldBossManager.isWorldBoss(self.getUUID())) {
            political_wbHealthBefore = self.getHealth();
        }
        political_healthBeforeArmorXp = self instanceof ServerPlayer ? self.getHealth() : -1f;
    }

    @Inject(method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z", at = @At("RETURN"))
    private void political_recordWorldBossDamage(ServerLevel world, DamageSource source, float amount,
                                                 CallbackInfoReturnable<Boolean> cir) {
        if (!Boolean.TRUE.equals(cir.getReturnValue())) {
            political_healthBeforeArmorXp = -1f;
            return;
        }
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof ServerPlayer player && political_healthBeforeArmorXp >= 0f) {
            float lost = political_healthBeforeArmorXp - self.getHealth();
            political_healthBeforeArmorXp = -1f;
            if (lost > 0.25f) {
                MasteryManager.awardArmorCombatXp(player, lost);
            }
        } else {
            political_healthBeforeArmorXp = -1f;
        }
        if (!WorldBossManager.isWorldBoss(self.getUUID())) return;
        if (!(source.getEntity() instanceof ServerPlayer player)) return;
        float dealt = political_wbHealthBefore - self.getHealth();
        if (dealt > 0) {
            WorldBossManager.recordDamage(self.getUUID(), player, dealt);
        }
    }
}