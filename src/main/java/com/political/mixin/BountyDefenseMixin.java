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
        boolean isAnyBoss = bossType != null || SlayerManager.isSlayerBoss(attacker.getUuid());

        if (!isAnyBoss) return amount;

        // Get actual boss type if tracked
        if (bossType == null) {
            bossType = SlayerManager.getBossSlayerType(attacker.getUuid());
        }

        float reduction = 1.0f;
        reduction *= getArmorDefenseMultiplier(player, bossType, isAnyBoss);
        reduction *= getWeaponDefenseMultiplier(player, bossType);

        return amount * reduction;
    }

    private SlayerManager.SlayerType getBossType(LivingEntity entity) {
        if (!entity.hasCustomName()) return null;
        String name = entity.getCustomName().getString();

        if (name.contains("Undying Outlaw") || name.contains("Rotting"))
            return SlayerManager.SlayerType.ZOMBIE;
        if (name.contains("Venomous Bandit") || name.contains("Silk"))
            return SlayerManager.SlayerType.SPIDER;
        if (name.contains("Bone Desperado"))
            return SlayerManager.SlayerType.SKELETON;
        if (name.contains("Gelatinous Rustler"))
            return SlayerManager.SlayerType.SLIME;
        if (name.contains("Void Phantom"))
            return SlayerManager.SlayerType.ENDERMAN;
        if (name.contains("Sculk Terror"))
            return SlayerManager.SlayerType.WARDEN;

        return null;
    }

    private float getArmorDefenseMultiplier(ServerPlayerEntity player, SlayerManager.SlayerType bossType, boolean isAnyBoss) {
        float multiplier = 1.0f;

        ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);
        ItemStack chestplate = player.getEquippedStack(EquipmentSlot.CHEST);
        ItemStack leggings = player.getEquippedStack(EquipmentSlot.LEGS);
        ItemStack boots = player.getEquippedStack(EquipmentSlot.FEET);

        // ===== ZOMBIE BERSERKER HELMET (T1) =====
        if (isZombieBerserkerHelmet(helmet) && SlayerItems.canUseZombieBerserkerHelmet(player)) {
            if (bossType == SlayerManager.SlayerType.ZOMBIE) {
                multiplier *= 0.25f; // -75% from matching boss
            } else if (isAnyBoss) {
                multiplier *= 0.85f; // -15% from all bosses
            }
        }

        // ===== VENOMOUS CRAWLER LEGGINGS (T1) =====
        if (isSpiderLeggings(leggings) && SlayerItems.canUseSpiderLeggings(player)) {
            if (bossType == SlayerManager.SlayerType.SPIDER) {
                multiplier *= 0.25f; // -75% from matching boss
            } else if (isAnyBoss) {
                multiplier *= 0.85f; // -15% from all bosses
            }
        }

        // ===== GELATINOUS RUSTLER BOOTS (T1) =====
        if (isSlimeBoots(boots) && SlayerItems.canUseSlimeBoots(player)) {
            if (bossType == SlayerManager.SlayerType.SLIME) {
                multiplier *= 0.25f; // -75% from matching boss
            } else if (isAnyBoss) {
                multiplier *= 0.85f; // -15% from all bosses
            }
        }

        // ===== SCULK TERROR CHESTPLATE (T2) =====
        if (isWardenChestplate(chestplate) && SlayerItems.canUseWardenChestplate(player)) {
            if (bossType == SlayerManager.SlayerType.WARDEN) {
                multiplier *= 0.10f; // -90% from matching boss
            } else if (isAnyBoss) {
                multiplier *= 0.70f; // -30% from all bosses
            }
        }

        return multiplier;
    }

    private float getWeaponDefenseMultiplier(ServerPlayerEntity player, SlayerManager.SlayerType bossType) {
        ItemStack mainHand = player.getMainHandStack();

        // T2 swords provide better defense
        if (SlayerItems.isUpgradedSlayerSword(mainHand)) {
            SlayerManager.SlayerType swordType = SlayerItems.getSwordSlayerType(mainHand);
            if (swordType == bossType) {
                return 0.40f; // -60% damage from matching boss
            }
            return 0.85f; // -15% from any boss
        }

        // T1 swords
        if (SlayerItems.isSlayerSword(mainHand)) {
            SlayerManager.SlayerType swordType = SlayerItems.getSwordSlayerType(mainHand);
            if (swordType == bossType) {
                return 0.50f; // -50% damage from matching boss
            }
            return 0.90f; // -10% from any boss
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
        if (name == null) return false;
        String n = name.getString();
        return n.contains("Venomous") || (n.contains("Spider") && n.contains("Leggings"));
    }

    private boolean isSlimeBoots(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return false;
        String n = name.getString();
        return n.contains("Slime") || n.contains("Gelatinous");
    }

    private boolean isWardenChestplate(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return false;
        String n = name.getString();
        return n.contains("Warden") || n.contains("Sculk");
    }
}