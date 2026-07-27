package com.political.mixin;

import com.political.AscensionGrindManager;
import com.political.CivilCraftAchievements;
import com.political.CrafterContractManager;
import com.political.CoinManager;
import com.political.CustomItemHandler;
import com.political.HealthScalingManager;
import com.political.MasteryHeartTree;
import com.political.MasteryManager;
import com.political.WorldBossManager;
import com.political.FishingEventHandler;
import com.political.SlayerData;
import com.political.SlayerItems;
import com.political.SlayerManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class BountyDamageMixin {

    private ServerPlayer resolveKillerPlayer(DamageSource source) {
        if (source.getEntity() instanceof ServerPlayer directPlayer) {
            return directPlayer;
        }
        if (source.getDirectEntity() instanceof Projectile projectile && projectile.getOwner() instanceof ServerPlayer owner) {
            return owner;
        }
        return null;
    }

    @ModifyVariable(
            method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private float modifyDamageAmount(float amount, ServerLevel world, DamageSource source) {
        LivingEntity self = (LivingEntity) (Object) this;
        float modifiedAmount = amount;

        // === Player taking damage from slayer boss ===
        if (self instanceof ServerPlayer player) {
            // Voidwalker's Crown — Phase Dodge. Handles the wearing check, the
            // BYPASSES_INVULNERABILITY exclusion, the roll, blink, sound and actionbar itself;
            // we only honour the boolean by negating the hit.
            if (CustomItemHandler.tryPhaseDodge(player, source, modifiedAmount)) {
                return 0.0f;
            }
            // Relics that react to being hurt.
            modifiedAmount *= com.political.RelicManager.onHurt(player, modifiedAmount);
            if (source.getEntity() instanceof LivingEntity attacker) {
                if (SlayerManager.isSlayerBoss(attacker.getUUID())) {
                    SlayerManager.SlayerType bossType = SlayerManager.getBossSlayerType(attacker.getUUID());
                    if (bossType != null) {
                        double reduction = SlayerManager.getLevelDamageReduction(player, bossType);
                        modifiedAmount *= (1.0f - (float) reduction);
                    }
                }
            }
            // Grimoire: three of the Ward circle spends Focus as armour before defence applies.
            modifiedAmount = com.political.GrimoireHandler.absorb(player, modifiedAmount);
            // Skyblock-style Defense stat: dmg * 100 / (100 + defense)
            return com.political.PlayerStatsManager.applyIncomingDefense(player, source, modifiedAmount);
        }

        // === Outgoing damage: player attacking something ===
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            return modifiedAmount;
        }

        // Apply level-based damage bonus against slayer bosses
        if (SlayerManager.isSlayerBoss(self.getUUID())) {
            SlayerManager.SlayerType bossType = SlayerManager.getBossSlayerType(self.getUUID());
            if (bossType != null) {
                double multiplier = SlayerManager.getLevelDamageMultiplier(player, bossType);
                modifiedAmount *= (float) multiplier;
            }
            // Apply HUNTERS_INSTINCT perk damage bonus against any bounty boss
            modifiedAmount *= com.political.PerkManager.getBountyBossDamageMultiplier();
            
            // SLAYER_ELITE buff: +10% bounty damage
            double slayerEliteBonus = com.political.PlayerBuffManager.getBountyDamageBonus(player.getUUID().toString());
            modifiedAmount *= (1.0f + (float) slayerEliteBonus);
        }
        
        // UNDEAD_HUNTER buff: +15% damage to undead mobs
        if (self.getType().getCategory() == net.minecraft.world.entity.MobCategory.MONSTER && 
            (self instanceof net.minecraft.world.entity.monster.zombie.Zombie ||
             self instanceof net.minecraft.world.entity.monster.skeleton.Skeleton ||
             self instanceof net.minecraft.world.entity.monster.skeleton.WitherSkeleton ||
             self instanceof net.minecraft.world.entity.monster.skeleton.Stray ||
             self instanceof net.minecraft.world.entity.monster.zombie.Husk ||
             self instanceof net.minecraft.world.entity.monster.zombie.Drowned ||
             self instanceof net.minecraft.world.entity.monster.zombie.ZombifiedPiglin ||
             self instanceof net.minecraft.world.entity.monster.Zoglin ||
             self instanceof net.minecraft.world.entity.monster.Phantom)) {
            double undeadBonus = com.political.PlayerBuffManager.getUndeadDamageBonus(player.getUUID().toString());
            modifiedAmount *= (1.0f + (float) undeadBonus);
        }

        // Check slayer sword level requirement
        ItemStack weapon = player.getMainHandItem();
        if (SlayerItems.isSlayerSword(weapon)) {
            SlayerManager.SlayerType swordType = SlayerItems.getSwordSlayerType(weapon);
            if (swordType != null) {
                int playerLevel = SlayerData.getSlayerLevel(player.getUUID().toString(), swordType);
                int requiredLevel = SlayerItems.getSwordLevelRequirement(weapon);

                if (playerLevel < requiredLevel) {
                    player.sendSystemMessage(Component.literal("⚠ Requires " + swordType.displayName + " Bounty Level " + requiredLevel + "!")
                            .withStyle(ChatFormatting.RED), true);
                    return modifiedAmount;
                }
            }
        }

        modifiedAmount *= (float) FishingEventHandler.getSeaCreatureDamageMultiplier(player, self);

        // Relics that fire when you land a strike.
        com.political.RelicManager.onHit(player, self);
        com.political.AetherGearHandler.onHit(player, self);
        com.political.RuneforgedHandler.onHit(player, self);
        modifiedAmount += com.political.BeastcraftHandler.onHit(player, self, modifiedAmount);
        com.political.TempestHandler.onHit(player, self, modifiedAmount);
        com.political.GrimoireHandler.onHit(player, self, modifiedAmount);
        modifiedAmount *= com.political.RegaliaHandler.damageMultiplier(player);

        // Wartime: citizens of polities at war strike harder at one another.
        if (self instanceof ServerPlayer victimPlayer) {
            modifiedAmount *= com.political.WarManager.wartimeDamageMultiplier(player, victimPlayer);
        }

        if (source.is(DamageTypeTags.IS_PROJECTILE)) {
            modifiedAmount *= com.political.PartyPerkEffects.projectileDamageBonusMultiplier(player.getUUID().toString());
        } else {
            modifiedAmount *= com.political.PartyPerkEffects.meleeDamageBonusMultiplier(player.getUUID().toString());
        }

        return CustomItemHandler.calculateSlayerDamage(player, self, modifiedAmount);
    }

    @Inject(method = "die", at = @At("HEAD"))
    private void onEntityDeath(DamageSource source, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;

        ServerPlayer player = resolveKillerPlayer(source);
        if (player == null) {
            return;
        }

        ItemStack weapon = player.getMainHandItem();

        // Relics that fire on any kill, mob or player.
        com.political.RelicManager.onKill(player, self);
        com.political.CompanionManager.onKill(player);
        com.political.StarboundHandler.onKill(player);
        com.political.VoyagerHandler.onKill(player);
        com.political.ContractManager.onKill(player);

        if (WorldBossManager.isWorldBoss(self.getUUID())) {
            WorldBossManager.onBossDeath(self, (ServerLevel) self.level(), player);
            MasteryManager.awardKillXp(player, self, weapon);
            MasteryHeartTree.onKillHeal(player);
            com.political.ProgressionTreeManager.awardSkillPoints(player, com.political.ProgressionTreeManager.Branch.COMBAT, 50);
            return;
        }

        SlayerManager.onMobKill(player, self);
        FishingEventHandler.onEntityDeath(self, player);

        if (!(self instanceof ServerPlayer) && (self.getType().getCategory() == net.minecraft.world.entity.MobCategory.MONSTER
                || self.getType().getCategory() == net.minecraft.world.entity.MobCategory.WATER_CREATURE)) {
            CrafterContractManager.onHostileKill(player);
        }

        if (SlayerManager.isSlayerBoss(self.getUUID())) {
            SlayerManager.onBossDeath(self, player);
        }

        HealthScalingManager.onScaledMobKill(self, player);

        MasteryManager.awardKillXp(player, self, weapon);
        MasteryHeartTree.onKillHeal(player);

        if (self instanceof ServerPlayer) {
            CivilCraftAchievements.trackPvPKill(player);
            // War score + spoils when the victim belonged to an enemy polity.
            com.political.WarManager.onWartimeKill(player, (ServerPlayer) self);
            com.political.ProgressionTreeManager.awardSkillPoints(player, com.political.ProgressionTreeManager.Branch.COMBAT, 10);
        } else if (self.getType().getCategory() == net.minecraft.world.entity.MobCategory.MONSTER
                || self.getType().getCategory() == net.minecraft.world.entity.MobCategory.WATER_CREATURE) {
            CivilCraftAchievements.trackHostileKill(player);
            AscensionGrindManager.onMobKilled(player, self);
            int tierBonus = HealthScalingManager.getMobTier(self);
            com.political.ProgressionTreeManager.awardSkillPoints(player, com.political.ProgressionTreeManager.Branch.COMBAT, 2 + tierBonus);
        }

        // Midas sword coin bonus
        if (SlayerItems.isMidasSword(weapon)) {
            // Get current kill bonus from NBT or default to 1
            String customId = SlayerItems.getCustomItemId(weapon);
            if ("midas_sword".equals(customId)) {
                // Give 1 coin per kill
                CoinManager.giveCoinsQuiet(player, 1);
                
                // Update the sword's kill counter in NBT
                int currentKills = SlayerItems.getMidasSwordKills(weapon);
                SlayerItems.setMidasSwordKills(weapon, currentKills + 1);
                
                // Show subtle coin notification
                player.sendSystemMessage(Component.literal("§6+1 coin §7(Midas's Sword)").withStyle(ChatFormatting.GOLD), true);
            }
        }

        // Dictator kill detection
        if (self instanceof ServerPlayer killedPlayer) {
            if (com.political.DictatorManager.isDictator(killedPlayer.getUUID().toString())) {
                if (source.getEntity() instanceof ServerPlayer killer) {
                    com.political.DictatorManager.handleDictatorKilled(killedPlayer, killer);
                }
            }
        }
    }
}