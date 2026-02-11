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
public class SlayerDamageMixin {

    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float modifySlayerDamage(float amount, ServerWorld world, DamageSource source) {
        LivingEntity self = (LivingEntity) (Object) this;
        float modifiedAmount = amount;

        // Try to scale mob on first damage
        HealthScalingManager.tryScaleMob(self);

        // === INCOMING DAMAGE REDUCTION (Player being attacked by slayer boss) ===
        if (self instanceof ServerPlayerEntity player) {
            if (source.getAttacker() != null && SlayerManager.isSlayerBoss(source.getAttacker().getUuid())) {
                SlayerManager.SlayerType bossType = SlayerManager.getBossSlayerType(source.getAttacker().getUuid());
                if (bossType != null) {
                    double reduction = SlayerManager.getLevelDamageReduction(player, bossType);
                    modifiedAmount *= (float) (1.0 - reduction);
                }
            }
            return modifiedAmount;
        }

        // === OUTGOING DAMAGE (Player attacking something) ===
        if (!(source.getAttacker() instanceof ServerPlayerEntity player)) {
            return modifiedAmount;
        }

        // Apply level-based damage bonus against slayer bosses
        if (SlayerManager.isSlayerBoss(self.getUuid())) {
            SlayerManager.SlayerType bossType = SlayerManager.getBossSlayerType(self.getUuid());
            if (bossType != null) {
                double levelBonus = SlayerManager.getLevelDamageMultiplier(player, bossType);
                modifiedAmount *= (float) levelBonus;
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
                    player.sendMessage(Text.literal("⚠ Requires " + swordType.displayName + " Slayer Level " + requiredLevel + "!")
                            .formatted(Formatting.RED), true);
                    return modifiedAmount; // No sword bonus, but keep level bonus
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
    }
}