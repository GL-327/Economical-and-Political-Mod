package com.political.mixin;

import com.political.SlayerItems;
import com.political.SlayerManager;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public class BossDamageReductionMixin {

    @ModifyVariable(method = "applyDamage", at = @At("HEAD"), argsOnly = true)
    private float reduceBossDamageWithBountyArmor(float amount, ServerWorld world, DamageSource source) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (!(self instanceof ServerPlayerEntity player)) return amount;

        // Check if damage is from a boss
        if (source.getAttacker() == null) return amount;
        String attackerName = source.getAttacker().getName().getString();
        boolean isFromBoss = false;

        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            if (attackerName.contains(type.bossName)) {
                isFromBoss = true;
                break;
            }
        }

        if (!isFromBoss) return amount;

        // Calculate damage reduction based on bounty armor worn
        float reduction = 1.0f;

        // Zombie Berserker Helmet - 15% boss damage reduction
        ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);
        if (SlayerItems.isZombieBerserkerHelmet(helmet) && SlayerItems.canUseZombieBerserkerHelmet(player)) {
            reduction -= 0.15f;
        }

        // Spider Leggings - 20% boss damage reduction
        ItemStack leggings = player.getEquippedStack(EquipmentSlot.LEGS);
        if (SlayerItems.isSpiderLeggings(leggings) && SlayerItems.canUseSpiderLeggings(player)) {
            reduction -= 0.20f;
        }

        // Slime Boots - 10% boss damage reduction
        ItemStack boots = player.getEquippedStack(EquipmentSlot.FEET);
        if (SlayerItems.isSlimeBoots(boots) && SlayerItems.canUseSlimeBoots(player)) {
            reduction -= 0.10f;
        }

        // Warden Chestplate - 30% boss damage reduction (BEST)
        ItemStack chestplate = player.getEquippedStack(EquipmentSlot.CHEST);
        if (SlayerItems.isWardenChestplate(chestplate) && SlayerItems.canUseWardenChestplate(player)) {
            reduction -= 0.30f;
        }

        // Cap reduction at 60%
        reduction = Math.max(0.4f, reduction);

        return amount * reduction;
    }
}