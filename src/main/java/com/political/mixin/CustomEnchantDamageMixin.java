package com.political.mixin;

import com.political.CustomEnchantmentManager;
import com.political.SlayerManager;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
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
            method = "damage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)Z",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private float applyCustomEnchantments(float amount, ServerWorld world, DamageSource source) {
        LivingEntity self = (LivingEntity) (Object) this;
        float modified = amount;

        // ── Defensive enchantments (target wearing custom armor) ──
        if (self instanceof ServerPlayerEntity defender) {
            modified *= CustomEnchantmentManager.getDefensiveMultiplier(defender, source);
        }

        // ── Offensive enchantments (attacker holding custom weapon) ──
        if (source.getAttacker() instanceof ServerPlayerEntity attacker) {
            ItemStack weapon = attacker.getMainHandStack();
            modified *= CustomEnchantmentManager.getOffensiveMultiplier(weapon, self);
        }

        return modified;
    }

    @Inject(
            method = "damage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)Z",
            at = @At("RETURN")
    )
    private void onDamageDealt(ServerWorld world, DamageSource source, float amount,
                                CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        LivingEntity self = (LivingEntity) (Object) this;

        if (!(source.getAttacker() instanceof ServerPlayerEntity attacker)) return;
        ItemStack weapon = attacker.getMainHandStack();

        // ── Vampiric: heal 10% of damage dealt ──
        if (CustomEnchantmentManager.has(weapon, CustomEnchantmentManager.VAMPIRIC)) {
            float heal = amount * 0.10f;
            attacker.heal(heal);
        }

        // ── Thunderbolt: 5% chance to strike lightning ──
        if (CustomEnchantmentManager.has(weapon, CustomEnchantmentManager.THUNDERBOLT)) {
            if (world.getRandom().nextFloat() < 0.05f) {
                net.minecraft.entity.LightningEntity lightning =
                        net.minecraft.entity.EntityType.LIGHTNING_BOLT.create(world,
                                net.minecraft.entity.SpawnReason.TRIGGERED);
                if (lightning != null) {
                    lightning.setPosition(self.getX(), self.getY(), self.getZ());
                    world.spawnEntity(lightning);
                }
            }
        }

        // ── Thorns of Vengeance: reflect 15% of melee damage ──
        if (self instanceof ServerPlayerEntity defender) {
            // Only reflect melee attacks (where the source entity is the attacker directly)
            boolean isMeleeAttack = source.getAttacker() != null
                    && source.getSource() == source.getAttacker();
            if (isMeleeAttack) {
                ItemStack leggings = defender.getEquippedStack(EquipmentSlot.LEGS);
                if (CustomEnchantmentManager.has(leggings, CustomEnchantmentManager.THORNS_OF_VENGEANCE)) {
                    float reflected = amount * 0.15f;
                    if (source.getAttacker() instanceof LivingEntity attLiving) {
                        attLiving.damage(world,
                                world.getDamageSources().thorns(defender),
                                reflected);
                    }
                }
            }
        }

        // ── Frostbite: apply Slowness II for 3 seconds on hit ──
        if (CustomEnchantmentManager.has(weapon, CustomEnchantmentManager.FROSTBITE)) {
            self.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                    net.minecraft.entity.effect.StatusEffects.SLOWNESS, 60, 1, true, false, false));
        }
    }
}
