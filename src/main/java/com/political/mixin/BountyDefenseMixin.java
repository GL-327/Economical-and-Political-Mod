package com.political.mixin;

import com.political.SlayerArmorHandler;
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

/**
 * Applies bounty-gear damage reduction when a player takes damage from a bounty boss.
 *
 * Defence model (additive stacking, capped at 95%):
 *   universalDef  — % reduction vs ALL bounty bosses (always active when worn/held)
 *   specificDef   — EXTRA % reduction vs the MATCHING boss type (stacks with universalDef)
 *   totalDef      = min(0.95, universalDef + specificDef)
 *   finalDamage   = incomingDamage × (1 - totalDef)
 *
 * Crown of Greed: disables ALL bounty gear defence.
 */
@Mixin(LivingEntity.class)
public class BountyDefenseMixin {

    private static final float MAX_TOTAL_REDUCTION = 0.95f;

    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float reduceBossDamage(float amount, ServerWorld world, DamageSource source) {
        LivingEntity self = (LivingEntity)(Object)this;

        // Only applies when a player is the one being hit
        if (!(self instanceof ServerPlayerEntity player)) return amount;
        if (source.getAttacker() == null) return amount;
        if (!(source.getAttacker() instanceof LivingEntity attacker)) return amount;

        // Ender Phase Mode: cancel all damage except from Endermen
        if (com.political.CustomItemHandler.isInEnderPhaseMode(player.getUuid())) {
            boolean attackerIsEnderman = attacker instanceof net.minecraft.entity.mob.EndermanEntity;
            if (!attackerIsEnderman) {
                return 0.0f; // Fully block damage while in Ender Phase Mode
            }
        }

        // Must be a bounty boss attacker
        SlayerManager.SlayerType bossType = SlayerManager.getBossSlayerType(attacker.getUuid());
        boolean isAnyBoss = bossType != null || SlayerManager.isSlayerBoss(attacker.getUuid());
        if (!isAnyBoss) return amount;

        // Crown of Greed: disables ALL bounty defence
        ItemStack helmet     = player.getEquippedStack(EquipmentSlot.HEAD);
        if (SlayerItems.isCrownOfGreed(helmet)) return amount;

        ItemStack chestplate = player.getEquippedStack(EquipmentSlot.CHEST);
        ItemStack leggings   = player.getEquippedStack(EquipmentSlot.LEGS);
        ItemStack boots      = player.getEquippedStack(EquipmentSlot.FEET);
        ItemStack weapon     = player.getMainHandStack();

        float universalDef = 0.0f;  // vs ALL bounty bosses
        float specificDef  = 0.0f;  // EXTRA vs the matching boss type

        // ── Special legendary pieces ─────────────────────────────────────
        // Zombie Berserker Helmet
        if (SlayerItems.isZombieBerserkerHelmet(helmet) && SlayerItems.canUseZombieBerserkerHelmet(player)) {
            universalDef += (float) SlayerItems.BERSERKER_HELMET_ALL_REDUCTION;
            if (bossType == SlayerManager.SlayerType.ZOMBIE) {
                specificDef += (float) SlayerItems.BERSERKER_HELMET_BOSS_REDUCTION;
            }
        }

        // Venomous Crawler Leggings
        if (SlayerItems.isSpiderLeggings(leggings) && SlayerItems.canUseSpiderLeggings(player)) {
            universalDef += (float) SlayerItems.SPIDER_LEGS_ALL_REDUCTION;
            if (bossType == SlayerManager.SlayerType.SPIDER) {
                specificDef += (float) SlayerItems.SPIDER_LEGS_BOSS_REDUCTION;
            }
        }

        // Gelatinous Rustler Boots
        if (SlayerItems.isSlimeBoots(boots) && SlayerItems.canUseSlimeBoots(player)) {
            universalDef += (float) SlayerItems.SLIME_BOOTS_ALL_REDUCTION;
            if (bossType == SlayerManager.SlayerType.SLIME) {
                specificDef += (float) SlayerItems.SLIME_BOOTS_BOSS_REDUCTION;
            }
        }

        // Sculk Terror Chestplate
        if (SlayerItems.isWardenChestplate(chestplate) && SlayerItems.canUseWardenChestplate(player)) {
            universalDef += (float) SlayerItems.WARDEN_CHEST_ALL_REDUCTION;
            if (bossType == SlayerManager.SlayerType.WARDEN) {
                specificDef += (float) SlayerItems.WARDEN_CHEST_BOSS_REDUCTION;
            }
        }

        // Voidwalker's Crown (Ender Phase Helmet)
        if (SlayerItems.isEnderPhaseHelmet(helmet) && SlayerItems.canUseEnderPhaseHelmet(player)) {
            universalDef += (float) SlayerItems.VOIDWALKER_ALL_REDUCTION;
            if (bossType == SlayerManager.SlayerType.ENDERMAN) {
                specificDef += (float) SlayerItems.VOIDWALKER_BOSS_REDUCTION;
            }
        }

        // ── Standard T1 / T2 armour sets ────────────────────────────────
        if (bossType != null) {
            specificDef  += SlayerArmorHandler.getBossDamageReduction(player, bossType);
        }
        universalDef += SlayerArmorHandler.getUniversalBossReduction(player);

        // ── Weapon (bounty swords / bow) ─────────────────────────────────
        if (SlayerItems.isUpgradedSlayerSword(weapon)) {
            universalDef += 0.15f; // T2 sword: -15% vs all bosses
            SlayerManager.SlayerType swordType = SlayerItems.getSwordSlayerType(weapon);
            if (swordType != null && swordType == bossType) {
                specificDef += 0.60f; // T2 sword: -60% extra vs matching boss
            }
        } else if (SlayerItems.isSlayerSword(weapon)) {
            universalDef += 0.10f; // T1 sword: -10% vs all bosses
            SlayerManager.SlayerType swordType = SlayerItems.getSwordSlayerType(weapon);
            if (swordType != null && swordType == bossType) {
                specificDef += 0.50f; // T1 sword: -50% extra vs matching boss
            }
        } else if (SlayerItems.isSkeletonBow(weapon)) {
            universalDef += 0.10f; // Skeleton bow: -10% universal
        }

        // ── Apply total (additive, capped at 85%) ────────────────────────
        float totalDef = Math.min(MAX_TOTAL_REDUCTION, universalDef + specificDef);
        if (totalDef <= 0) return amount;
        return amount * (1.0f - totalDef);
    }
}
