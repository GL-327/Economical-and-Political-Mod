package com.political.mixin;

import com.political.CustomEnchantmentManager;
import com.political.bounty.SlayerManager;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Applies custom enchantment effects during damage calculation.
 * Offensive enchantments are read from the attacker's weapon CustomData.
 * Defensive enchantments are read from the defender's armor CustomData.
 */
@Mixin(LivingEntity.class)
public abstract class CustomEnchantDamageMixin {

    @ModifyVariable(
            method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private float applyCustomEnchantments(float amount, ServerLevel world, DamageSource source) {
        LivingEntity self = (LivingEntity) (Object) this;
        float modified = amount;

        // ── Defensive enchantments (target wearing custom armor) ──
        if (self instanceof ServerPlayer defender) {
            modified *= CustomEnchantmentManager.getDefensiveMultiplier(defender, source);
        }

        // ── Offensive enchantments (attacker holding custom weapon) ──
        if (source.getEntity() instanceof ServerPlayer attacker) {
            ItemStack weapon = attacker.getMainHandItem();
            modified *= CustomEnchantmentManager.getOffensiveMultiplier(weapon, self);
        }

        return modified;
    }

    @Inject(
            method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At("RETURN")
    )
    private void onDamageDealt(ServerLevel world, DamageSource source, float amount,
                                CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        LivingEntity self = (LivingEntity) (Object) this;

        if (!(source.getEntity() instanceof ServerPlayer attacker)) return;
        ItemStack weapon = attacker.getMainHandItem();

        // ── Vampiric: heal 10% of damage dealt ──
        if (CustomEnchantmentManager.has(weapon, CustomEnchantmentManager.VAMPIRIC)) {
            float heal = amount * 0.10f;
            attacker.heal(heal);
        }

        // ── Thunderbolt: 5% chance to strike lightning ──
        if (CustomEnchantmentManager.has(weapon, CustomEnchantmentManager.THUNDERBOLT)) {
            if (world.getRandom().nextFloat() < 0.05f) {
                net.minecraft.world.entity.LightningBolt lightning =
                        net.minecraft.world.entity.EntityTypes.LIGHTNING_BOLT.create(world,
                                net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
                if (lightning != null) {
                    lightning.setPos(self.getX(), self.getY(), self.getZ());
                    world.addFreshEntity(lightning);
                }
            }
        }

        // ── Thorns of Vengeance: reflect 15% of melee damage ──
        if (self instanceof ServerPlayer defender) {
            // Only reflect melee attacks (where the source entity is the attacker directly)
            boolean isMeleeAttack = source.getEntity() != null
                    && source.getDirectEntity() == source.getEntity();
            if (isMeleeAttack) {
                ItemStack leggings = defender.getItemBySlot(EquipmentSlot.LEGS);
                if (CustomEnchantmentManager.has(leggings, CustomEnchantmentManager.THORNS_OF_VENGEANCE)) {
                    float reflected = amount * 0.15f;
                    if (source.getEntity() instanceof LivingEntity attLiving) {
                        attLiving.hurtServer(world,
                                world.damageSources().thorns(defender),
                                reflected);
                    }
                }
            }
        }

        // ── Frostbite: apply Slowness II for 3 seconds on hit ──
        if (CustomEnchantmentManager.has(weapon, CustomEnchantmentManager.FROSTBITE)) {
            self.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.SLOWNESS, 60, 1, true, false, false));
        }
    }
}
