package com.political.mixin;

import com.political.CustomItemHandler;
import com.political.PerkManager;
import com.political.SlayerItems;
import com.political.SlayerManager;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
}