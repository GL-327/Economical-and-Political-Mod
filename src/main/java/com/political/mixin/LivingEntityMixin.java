package com.political.mixin;

import com.political.CustomItemHandler;
import com.political.SlayerItems;
import com.political.SlayerManager;
import com.political.T2ArmorAbilityHandler;
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
        LivingEntity self = (LivingEntity)(Object)this;
        if (!(self instanceof ServerPlayerEntity player)) return amount;

        // Teleport dodge for Enderman T2 armor
        if (T2ArmorAbilityHandler.tryTeleportDodge(player, amount)) {
            return 0.0f;
        }

        // Projectile damage reduction for Skeleton T2 armor
        if (source.isIn(DamageTypeTags.IS_PROJECTILE)) {
            float multiplier = T2ArmorAbilityHandler.getProjectileDamageReduction(player);
            amount *= multiplier;
        }

        return amount;
    }

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

    // FIXED: fallDistance is double in 1.21.11, not float
    @Inject(method = "handleFallDamage", at = @At("HEAD"), cancellable = true)
    private void preventFallDamage(double fallDistance, float damageMultiplier, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity)(Object)this;
        if (!(entity instanceof ServerPlayerEntity player)) return;

        ItemStack boots = player.getEquippedStack(EquipmentSlot.FEET);

        // Check for Slime boots
        if (SlayerItems.isSlimeBoots(boots) && SlayerItems.canUseSlimeBoots(player)) {
            cir.setReturnValue(false);
            return;
        }

        // Check for T2 armor with fall damage immunity
        Text customName = boots.get(DataComponentTypes.CUSTOM_NAME);
        if (customName != null) {
            String name = customName.getString();

            // T2 Slime or Enderman boots
            if (name.contains(" II") &&
                    (name.contains("Rustler") || name.contains("Gelatinous") ||
                            name.contains("Void") || name.contains("Phantom"))) {

                // Bounce effect for slime boots
                if (name.contains("Rustler") || name.contains("Gelatinous")) {
                    if (fallDistance > 3.0) {
                        Vec3d velocity = player.getVelocity();
                        player.setVelocity(velocity.x, Math.min(fallDistance * 0.1, 1.0), velocity.z);

                        // FIXED: Use player.getWorld() cast to ServerWorld
                        ServerWorld world = (ServerWorld) player.getEntityWorld();
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
}