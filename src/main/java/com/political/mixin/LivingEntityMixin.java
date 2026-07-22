package com.political.mixin;

import com.political.BountyArmorHandler;
import com.political.CustomItemHandler;
import com.political.SlayerItems;
import com.political.SlayerManager;
import com.political.SpawnProtectionManager;
import com.political.T2ArmorAbilityHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @ModifyVariable(
            method = "actuallyHurt(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)V",
            at = @At("HEAD"),
            argsOnly = true)
    private float political_adjustDamage(float amount, ServerLevel world, DamageSource source) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (!(self instanceof ServerPlayer player)) return amount;

        // Spawn protection: cancel PvP damage (player-vs-player) when either party is in the zone
        if (source.getEntity() instanceof ServerPlayer attacker) {
            if (SpawnProtectionManager.isPosProtected(player.getX(), player.getY(), player.getZ())
                    || SpawnProtectionManager.isPosProtected(attacker.getX(), attacker.getY(), attacker.getZ())) {
                // Operators bypass protection
                if (!SpawnProtectionManager.isOperator(attacker)) {
                    attacker.sendSystemMessage(Component.literal("⛔ This area is under Government Protection!").withStyle(ChatFormatting.RED), true);
                    return 0.0f;
                }
            }
        }

        // Spawn protection: cancel ALL damage to players inside the zone (non-operators)
        if (SpawnProtectionManager.isPosProtected(player.getX(), player.getY(), player.getZ())
                && !SpawnProtectionManager.isOperator(player)) {
            return 0.0f;
        }

        // Teleport dodge for Enderman T2 armor
        if (T2ArmorAbilityHandler.tryTeleportDodge(player, amount)) {
            return 0.0f;
        }

        // Undead mob damage reduction from Zombie Leggings (T1: -10%, T2: -20%)
        LivingEntity attacker = source.getEntity() instanceof LivingEntity le ? le : null;
        if (attacker != null && attacker.getType().builtInRegistryHolder().is(EntityTypeTags.UNDEAD)) {
            float multiplier = BountyArmorHandler.getUndeadDamageReduction(player);
            amount *= multiplier;
        }

        // Projectile damage reduction for Skeleton armor (T1: -10%, T2 set: -15%/piece)
        if (source.is(DamageTypeTags.IS_PROJECTILE)) {
            float multiplier = T2ArmorAbilityHandler.getProjectileDamageReduction(player);
            multiplier *= BountyArmorHandler.getProjectileDamageResistance(player);
            amount *= multiplier;
        }

        return amount;
    }

    /**
     * Poison thorns: when a player with Spider T2 Chestplate is hit by a melee attacker,
     * the attacker gets Poison I for 3 seconds.
     */
    @Inject(
            method = "actuallyHurt(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)V",
            at = @At("HEAD"))
    private void political_poisonThorns(ServerLevel world, DamageSource source, float amount, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (!(self instanceof ServerPlayer player)) return;
        if (amount <= 0) return;
        // Only trigger on direct melee (non-projectile)
        if (source.is(DamageTypeTags.IS_PROJECTILE)) return;
        if (!(source.getEntity() instanceof LivingEntity attacker)) return;

        if (BountyArmorHandler.hasPoisonThorns(player)) {
            attacker.addEffect(new MobEffectInstance(
                    MobEffects.POISON, 60, 0, true, false, false)); // 3s Poison I
        }
    }

    @Inject(method = "checkTotemDeathProtection", at = @At("HEAD"), cancellable = true)
    private void preventTotemDuringBoss(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity)(Object)this;
        if (!(entity instanceof ServerPlayer player)) return;

        SlayerManager.ActiveQuest quest = SlayerManager.getActiveQuest(player);
        if (quest != null && quest.bossAlive) {
            player.sendSystemMessage(Component.literal("☠ Totems are disabled during boss fights!")
                    .withStyle(ChatFormatting.DARK_RED), true);
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "checkTotemDeathProtection", at = @At("HEAD"), cancellable = true)
    private void slimeBootsDeathSave(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity)(Object)this;
        if (!(entity instanceof ServerPlayer player)) return;

        if (CustomItemHandler.trySlimeBootsDeathSave(player)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "knockback", at = @At("HEAD"), cancellable = true)
    private void preventKnockback(double strength, double x, double z, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity)(Object)this;
        if (!(entity instanceof ServerPlayer player)) return;

        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (SlayerItems.isWardenChestplate(chestplate) && SlayerItems.canUseWardenChestplate(player)) {
            ci.cancel();
        }
    }

    // FIXED: fallDistance is double in 1.21.11, not float
    @Inject(method = "causeFallDamage", at = @At("HEAD"), cancellable = true)
    private void preventFallDamage(double fallDistance, float damageMultiplier, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity)(Object)this;
        if (!(entity instanceof ServerPlayer player)) return;

        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);

        // Check for legendary Slime boots
        if (SlayerItems.isSlimeBoots(boots) && SlayerItems.canUseSlimeBoots(player)) {
            cir.setReturnValue(false);
            return;
        }

        // T2 Enderman Chestplate: fall damage immunity
        String chestId = SlayerItems.getCustomItemId(player.getItemBySlot(EquipmentSlot.CHEST));
        if ("enderman_t2_chestplate".equals(chestId)) {
            cir.setReturnValue(false);
            return;
        }

        // Check per-piece fall damage immunity / reduction
        String bootsId = SlayerItems.getCustomItemId(boots);
        if (bootsId != null) {
            switch (bootsId) {
                // T1 Slime Boots: no fall damage
                case "slime_t1_boots" -> {
                    cir.setReturnValue(false);
                    return;
                }
                // T2 Slime Boots: no fall damage + bounce
                case "slime_t2_boots" -> {
                    if (fallDistance > 3.0) {
                        Vec3 velocity = player.getDeltaMovement();
                        player.setDeltaMovement(velocity.x, Math.min(fallDistance * 0.1, 1.0), velocity.z);
                        ServerLevel world = (ServerLevel) player.level();
                        world.playSound(null, player.blockPosition(),
                                SoundEvents.SLIME_SQUISH, SoundSource.PLAYERS, 1.0f, 1.0f);
                        world.sendParticles(ParticleTypes.ITEM_SLIME,
                                player.getX(), player.getY(), player.getZ(),
                                4, 0.5, 0.1, 0.5, 0.1);
                    }
                    cir.setReturnValue(false);
                    return;
                }
                // T1 Enderman Boots: safe fall +3 blocks (cancel if fall <= 6 blocks)
                case "enderman_t1_boots" -> {
                    if (fallDistance <= 6.0) {
                        cir.setReturnValue(false);
                        return;
                    }
                }
                // T2 Enderman Boots: complete no fall damage
                case "enderman_t2_boots" -> {
                    cir.setReturnValue(false);
                    return;
                }
                // T2 Spider Boots: safe fall (+3 block boost = cancel if fall <= 6)
                case "spider_t2_boots" -> {
                    if (fallDistance <= 6.0) {
                        cir.setReturnValue(false);
                        return;
                    }
                }
                default -> {}
            }
        }

        // Legacy: T2 Slime or Enderman boots identified by name (for items created before custom ID system)
        Component customName = boots.get(DataComponents.CUSTOM_NAME);
        if (customName != null) {
            String name = customName.getString();
            if (name.contains(" II") &&
                    (name.contains("Rustler") || name.contains("Gelatinous") ||
                            name.contains("Void") || name.contains("Phantom"))) {
                if (name.contains("Rustler") || name.contains("Gelatinous")) {
                    if (fallDistance > 3.0) {
                        Vec3 velocity = player.getDeltaMovement();
                        player.setDeltaMovement(velocity.x, Math.min(fallDistance * 0.1, 1.0), velocity.z);
                        ServerLevel world = (ServerLevel) player.level();
                        world.playSound(null, player.blockPosition(),
                                SoundEvents.SLIME_SQUISH, SoundSource.PLAYERS, 1.0f, 1.0f);
                        world.sendParticles(ParticleTypes.ITEM_SLIME,
                                player.getX(), player.getY(), player.getZ(),
                                4, 0.5, 0.1, 0.5, 0.1);
                    }
                }
                cir.setReturnValue(false);
            }
        }
    }
}