package com.political.mixin;

import com.political.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @ModifyVariable(method = "applyDamage", at = @At("HEAD"), argsOnly = true)
    private float political_adjustDamage(float amount, ServerWorld world, DamageSource source) {
        // ... your existing code ...
        return amount;
    }

    // FIXED: Changed to tryUseDeathProtector (not tryUseDeathProtection)
    @Inject(method = "tryUseDeathProtector", at = @At("HEAD"), cancellable = true)
    private void preventTotemDuringBoss(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity)(Object)this;
        if (!(entity instanceof ServerPlayerEntity player)) return;

        SlayerManager.ActiveQuest quest = SlayerManager.getActiveQuest(player);
        if (quest != null && quest.bossAlive) {
            player.sendMessage(Text.literal("☠ Totems are disabled during boss fights!")
                    .formatted(Formatting.DARK_RED), true);
            cir.setReturnValue(false);
        }
    }

    // FIXED: Changed to tryUseDeathProtector (not tryUseDeathProtection)
    @Inject(method = "tryUseDeathProtector", at = @At("HEAD"), cancellable = true)
    private void slimeBootsDeathSave(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity)(Object)this;
        if (!(entity instanceof ServerPlayerEntity player)) return;

        if (CustomItemHandler.trySlimeBootsDeathSave(player)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "takeKnockback", at = @At("HEAD"), cancellable = true)
    private void preventKnockback(double strength, double x, double z, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity)(Object)this;
        if (!(entity instanceof ServerPlayerEntity player)) return;

        ItemStack chestplate = player.getEquippedStack(EquipmentSlot.CHEST);
        if (SlayerItems.isWardenChestplate(chestplate) && SlayerItems.canUseWardenChestplate(player)) {
            ci.cancel();
        }
    }

    @Inject(method = "handleFallDamage", at = @At("HEAD"), cancellable = true)
    private void preventFallDamage(double fallDistance, float damageMultiplier, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity)(Object)this;
        if (!(entity instanceof ServerPlayerEntity player)) return;

        ItemStack boots = player.getEquippedStack(EquipmentSlot.FEET);
        if (SlayerItems.isSlimeBoots(boots) && SlayerItems.canUseSlimeBoots(player)) {
            cir.setReturnValue(false);
        }
    }
    // Add to LivingEntityMixin.java

    // Teleport Dodge for Enderman T2 armor
    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float tryTeleportDodge(float amount, ServerWorld world, DamageSource source) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (!(self instanceof ServerPlayerEntity player)) return amount;

        // Try teleport dodge
        if (T2ArmorAbilityHandler.tryTeleportDodge(player, amount)) {
            return 0.0f; // Damage dodged!
        }

        return amount;
    }

    // Projectile damage reduction for Skeleton T2 armor
    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float reduceProjectileDamage(float amount, ServerWorld world, DamageSource source) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (!(self instanceof ServerPlayerEntity player)) return amount;

        // Check if projectile damage
        if (source.isIn(DamageTypeTags.IS_PROJECTILE)) {
            float multiplier = T2ArmorAbilityHandler.getProjectileDamageReduction(player);
            return amount * multiplier;
        }

        return amount;
    }

    // No fall damage for Slime T2 boots or Enderman T2 boots
    @Inject(method = "handleFallDamage", at = @At("HEAD"), cancellable = true)
    private void preventT2FallDamage(float fallDistance, float damageMultiplier, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity)(Object)this;
        if (!(entity instanceof ServerPlayerEntity player)) return;

        ItemStack boots = player.getEquippedStack(EquipmentSlot.FEET);
        Text customName = boots.get(DataComponentTypes.CUSTOM_NAME);
        if (customName == null) return;
        String name = customName.getString();

        // T2 Slime boots or T2 Enderman boots
        if (name.contains(" II") &&
                (name.contains("Rustler") || name.contains("Gelatinous") ||
                        name.contains("Void") || name.contains("Phantom"))) {

            // Bounce effect for slime boots
            if (name.contains("Rustler") || name.contains("Gelatinous")) {
                if (fallDistance > 3.0f) {
                    Vec3d velocity = player.getVelocity();
                    player.setVelocity(velocity.x, Math.min(fallDistance * 0.1, 1.0), velocity.z);
                    player.velocityModified = true;

                    ServerWorld world = player.getEntityWorld();
                    world.playSound(null, player.getBlockPos(),
                            SoundEvents.ENTITY_SLIME_SQUISH, SoundCategory.PLAYERS, 1.0f, 1.0f);
                    world.spawnParticles(ParticleTypes.ITEM_SLIME,
                            player.getX(), player.getY(), player.getZ(),
                            15, 0.5, 0.1, 0.5, 0.1);
                }
            }

            cir.setReturnValue(false);
        }
    }
}