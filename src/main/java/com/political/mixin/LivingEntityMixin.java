package com.political.mixin;

import com.political.BountyArmorHandler;
import com.political.CustomItemHandler;
import com.political.SlayerItems;
import com.political.SlayerManager;
import com.political.SpawnProtectionManager;
import com.political.T2ArmorAbilityHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.EntityTypeTags;
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

        // Spawn protection: cancel PvP damage (player-vs-player) when either party is in the zone
        if (source.getAttacker() instanceof ServerPlayerEntity attacker) {
            if (SpawnProtectionManager.isPosProtected(player.getX(), player.getY(), player.getZ())
                    || SpawnProtectionManager.isPosProtected(attacker.getX(), attacker.getY(), attacker.getZ())) {
                // Operators bypass protection
                if (!SpawnProtectionManager.isOperator(attacker)) {
                    attacker.sendMessage(Text.literal("⛔ This area is under Government Protection!").formatted(Formatting.RED), true);
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
        LivingEntity attacker = source.getAttacker() instanceof LivingEntity le ? le : null;
        if (attacker != null && attacker.getType().isIn(EntityTypeTags.UNDEAD)) {
            float multiplier = BountyArmorHandler.getUndeadDamageReduction(player);
            amount *= multiplier;
        }

        // Projectile damage reduction for Skeleton armor (T1: -10%, T2 set: -15%/piece)
        if (source.isIn(DamageTypeTags.IS_PROJECTILE)) {
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
    @Inject(method = "applyDamage", at = @At("HEAD"))
    private void political_poisonThorns(ServerWorld world, DamageSource source, float amount, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (!(self instanceof ServerPlayerEntity player)) return;
        if (amount <= 0) return;
        // Only trigger on direct melee (non-projectile)
        if (source.isIn(DamageTypeTags.IS_PROJECTILE)) return;
        if (!(source.getAttacker() instanceof LivingEntity attacker)) return;

        if (BountyArmorHandler.hasPoisonThorns(player)) {
            attacker.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.POISON, 60, 0, true, false, false)); // 3s Poison I
        }
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

        // Check for legendary Slime boots
        if (SlayerItems.isSlimeBoots(boots) && SlayerItems.canUseSlimeBoots(player)) {
            cir.setReturnValue(false);
            return;
        }

        // T2 Enderman Chestplate: fall damage immunity
        String chestId = SlayerItems.getCustomItemId(player.getEquippedStack(EquipmentSlot.CHEST));
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
                        Vec3d velocity = player.getVelocity();
                        player.setVelocity(velocity.x, Math.min(fallDistance * 0.1, 1.0), velocity.z);
                        ServerWorld world = (ServerWorld) player.getEntityWorld();
                        world.playSound(null, player.getBlockPos(),
                                SoundEvents.ENTITY_SLIME_SQUISH, SoundCategory.PLAYERS, 1.0f, 1.0f);
                        world.spawnParticles(ParticleTypes.ITEM_SLIME,
                                player.getX(), player.getY(), player.getZ(),
                                15, 0.5, 0.1, 0.5, 0.1);
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
        Text customName = boots.get(DataComponentTypes.CUSTOM_NAME);
        if (customName != null) {
            String name = customName.getString();
            if (name.contains(" II") &&
                    (name.contains("Rustler") || name.contains("Gelatinous") ||
                            name.contains("Void") || name.contains("Phantom"))) {
                if (name.contains("Rustler") || name.contains("Gelatinous")) {
                    if (fallDistance > 3.0) {
                        Vec3d velocity = player.getVelocity();
                        player.setVelocity(velocity.x, Math.min(fallDistance * 0.1, 1.0), velocity.z);
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