package com.political.mixin;

import com.political.CoinManager;
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
            // Apply HUNTERS_INSTINCT perk damage bonus against any bounty boss
            modifiedAmount *= com.political.PerkManager.getBountyBossDamageMultiplier();
            
            // SLAYER_ELITE buff: +10% bounty damage
            double slayerEliteBonus = com.political.PlayerBuffManager.getBountyDamageBonus(player.getUuidAsString());
            modifiedAmount *= (1.0f + (float) slayerEliteBonus);
        }
        
        // UNDEAD_HUNTER buff: +15% damage to undead mobs
        if (self.getType().getSpawnGroup() == net.minecraft.entity.SpawnGroup.MONSTER && 
            (self instanceof net.minecraft.entity.mob.ZombieEntity ||
             self instanceof net.minecraft.entity.mob.SkeletonEntity ||
             self instanceof net.minecraft.entity.mob.WitherSkeletonEntity ||
             self instanceof net.minecraft.entity.mob.StrayEntity ||
             self instanceof net.minecraft.entity.mob.HuskEntity ||
             self instanceof net.minecraft.entity.mob.DrownedEntity ||
             self instanceof net.minecraft.entity.mob.ZombifiedPiglinEntity ||
             self instanceof net.minecraft.entity.mob.ZoglinEntity ||
             self instanceof net.minecraft.entity.mob.PhantomEntity)) {
            double undeadBonus = com.political.PlayerBuffManager.getUndeadDamageBonus(player.getUuidAsString());
            modifiedAmount *= (1.0f + (float) undeadBonus);
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

        // Midas sword coin bonus
        ItemStack weapon = player.getMainHandStack();
        if (SlayerItems.isMidasSword(weapon)) {
            // Get current kill bonus from NBT or default to 1
            String customId = SlayerItems.getCustomItemId(weapon);
            if ("midas_sword".equals(customId)) {
                // Give 1 coin per kill
                CoinManager.giveCoinsQuiet(player, 1);
                
                // Update the sword's kill counter in NBT
                int currentKills = SlayerItems.getMidasSwordKills(weapon);
                SlayerItems.setMidasSwordKills(weapon, currentKills + 1);
                
                // Show subtle coin notification
                player.sendMessage(Text.literal("§6+1 coin §7(Midas's Sword)").formatted(Formatting.GOLD), true);
            }
        }

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