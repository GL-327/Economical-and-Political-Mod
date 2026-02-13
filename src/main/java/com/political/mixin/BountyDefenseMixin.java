package com.political.mixin;

import com.political.SlayerItems;
import com.political.SlayerManager;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public class BountyDefenseMixin {

    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float reduceBossDamage(float amount, ServerWorld world, DamageSource source) {
        LivingEntity self = (LivingEntity)(Object)this;

        if (!(self instanceof ServerPlayerEntity player)) return amount;

        if (source.getAttacker() == null) return amount;
        if (!(source.getAttacker() instanceof LivingEntity attacker)) return amount;

        SlayerManager.SlayerType bossType = getBossType(attacker);
        if (bossType == null) return amount;

        float reduction = 1.0f;
        reduction *= getArmorDefenseMultiplier(player, bossType);
        reduction *= getWeaponDefenseMultiplier(player, bossType);

        return amount * reduction;
    }

    private SlayerManager.SlayerType getBossType(LivingEntity entity) {
        if (!entity.hasCustomName()) return null;
        String name = entity.getCustomName().getString();

        if (name.contains("Rotting Outlaw") || name.contains("Zombie"))
            return SlayerManager.SlayerType.ZOMBIE;
        if (name.contains("Silk Bandit") || name.contains("Spider"))
            return SlayerManager.SlayerType.SPIDER;
        if (name.contains("Bone Desperado") || name.contains("Skeleton"))
            return SlayerManager.SlayerType.SKELETON;
        if (name.contains("Gelatinous Rustler") || name.contains("Slime"))
            return SlayerManager.SlayerType.SLIME;
        if (name.contains("Sculk Phantom") || name.contains("Warden"))
            return SlayerManager.SlayerType.WARDEN;

        return null;
    }

    private float getArmorDefenseMultiplier(ServerPlayerEntity player, SlayerManager.SlayerType bossType) {
        float multiplier = 1.0f;

        ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);
        if (isZombieBerserkerHelmet(helmet)) {
            multiplier *= (bossType == SlayerManager.SlayerType.ZOMBIE) ? 0.25f : 0.85f;
        }

        ItemStack leggings = player.getEquippedStack(EquipmentSlot.LEGS);
        if (isSpiderLeggings(leggings)) {
            multiplier *= (bossType == SlayerManager.SlayerType.SPIDER) ? 0.25f : 0.85f;
        }

        ItemStack boots = player.getEquippedStack(EquipmentSlot.FEET);
        if (isSlimeBoots(boots)) {
            multiplier *= (bossType == SlayerManager.SlayerType.SLIME) ? 0.25f : 0.85f;
        }

        ItemStack chestplate = player.getEquippedStack(EquipmentSlot.CHEST);
        if (isWardenChestplate(chestplate)) {
            multiplier *= (bossType == SlayerManager.SlayerType.WARDEN) ? 0.10f : 0.70f;
        }

        return multiplier;
    }

    private float getWeaponDefenseMultiplier(ServerPlayerEntity player, SlayerManager.SlayerType bossType) {
        ItemStack mainHand = player.getMainHandStack();

        if (SlayerItems.isSlayerSword(mainHand)) {
            SlayerManager.SlayerType swordType = SlayerItems.getSwordSlayerType(mainHand);
            return (swordType == bossType) ? 0.50f : 0.90f;
        }

        return 1.0f;
    }

    private boolean isZombieBerserkerHelmet(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        return name != null && name.getString().contains("Berserker");
    }

    private boolean isSpiderLeggings(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        return name != null && name.getString().contains("Venomous");
    }

    private boolean isSlimeBoots(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        return name != null && (name.getString().contains("Slime") || name.getString().contains("Gelatinous"));
    }

    private boolean isWardenChestplate(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        return name != null && (name.getString().contains("Warden") || name.getString().contains("Sculk"));
    }
}