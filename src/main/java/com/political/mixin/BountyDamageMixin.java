package com.political.mixin;

import com.political.CustomItemHandler;
import com.political.HealthScalingManager;
import com.political.SlayerData;
import com.political.SlayerItems;
import com.political.SlayerManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class BountyDamageMixin {

    @ModifyVariable(
            method = "damage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)Z",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private float modifyDamageAmount(float amount, ServerWorld world, DamageSource source) {
        LivingEntity self = (LivingEntity) (Object) this;
        float modifiedAmount = amount;

        // Try to scale mob on first damage
        HealthScalingManager.tryScaleMob(self);

        // === Player taking damage from slayer boss ===
        if (self instanceof ServerPlayerEntity player) {
            if (source.getAttacker() instanceof LivingEntity attacker) {
                if (SlayerManager.isSlayerBoss(attacker.getUuid())) {
                    SlayerManager.SlayerType bossType = SlayerManager.getBossSlayerType(attacker.getUuid());
                    if (bossType != null) {
                        double reduction = SlayerManager.getLevelDamageReduction(player, bossType);
                        return modifiedAmount * (1.0f - (float) reduction);
                    }
                }
            }
            return modifiedAmount;
        }

        // === Outgoing damage: player attacking something ===
        if (!(source.getAttacker() instanceof ServerPlayerEntity player)) {
            return modifiedAmount;
        }

        // Apply level-based damage bonus against slayer bosses
        if (SlayerManager.isSlayerBoss(self.getUuid())) {
            SlayerManager.SlayerType bossType = SlayerManager.getBossSlayerType(self.getUuid());
            if (bossType != null) {
                double multiplier = SlayerManager.getLevelDamageMultiplier(player, bossType);
                modifiedAmount *= (float) multiplier;
            }
        }

        // Check slayer sword level requirement
        ItemStack weapon = player.getMainHandStack();
        if (SlayerItems.isSlayerSword(weapon)) {
            SlayerManager.SlayerType swordType = SlayerItems.getSwordSlayerType(weapon);
            if (swordType != null) {
                int playerLevel = SlayerData.getSlayerLevel(player.getUuidAsString(), swordType);
                int requiredLevel = SlayerItems.getSwordLevelRequirement(weapon);

                if (playerLevel < requiredLevel) {
                    player.sendMessage(Text.literal("⚠ Requires " + swordType.displayName + " Bounty Level " + requiredLevel + "!")
                            .formatted(Formatting.RED), true);
                    return modifiedAmount;
                }
            }
        }

        return CustomItemHandler.calculateSlayerDamage(player, self, modifiedAmount);
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onEntityDeath(DamageSource source, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (!(source.getAttacker() instanceof ServerPlayerEntity player)) {
            return;
        }

        SlayerManager.onMobKill(player, self);

        if (SlayerManager.isSlayerBoss(self.getUuid())) {
            SlayerManager.onBossDeath(self, player);
        }

        HealthScalingManager.onScaledMobKill(self, player);

        // Dictator kill detection
        if (self instanceof ServerPlayerEntity killedPlayer) {
            if (com.political.DictatorManager.isDictator(killedPlayer.getUuidAsString())) {
                if (source.getAttacker() instanceof ServerPlayerEntity killer) {
                    com.political.DictatorManager.handleDictatorKilled(killedPlayer, killer);
                }
            }
        }
    }
}