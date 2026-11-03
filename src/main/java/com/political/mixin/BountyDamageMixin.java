package com.political.mixin;
import com.political.CollectionManager;
import com.political.CompanionManager;
import com.political.ContractManager;
import com.political.NemesisManager;
import com.political.PlayerBuffManager;
import com.political.ProgressionTreeManager;
import com.political.RareDropManager;
import com.political.RelicManager;
import com.political.WarrantArmour;
import com.political.items.DragonPlateHandler;



import com.political.government.DictatorManager;
import com.political.government.PerkManager;
import com.political.party.PartyPerkEffects;
import com.political.settlement.WarManager;
import com.political.stats.PlayerStatsManager;
import com.political.technique.TechniqueManager;
import com.political.AscensionGrindManager;
import com.political.CivilCraftAchievements;
import com.political.CrafterContractManager;
import com.political.economy.CoinManager;
import com.political.items.CustomItemHandler;
import com.political.stats.HealthScalingManager;
import com.political.MasteryHeartTree;
import com.political.MasteryManager;
import com.political.WorldBossManager;
import com.political.FishingEventHandler;
import com.political.bounty.SlayerData;
import com.political.bounty.SlayerItems;
import com.political.bounty.SlayerManager;
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
        // A Domain strike deals exactly the damage it computed: no Defense, no multipliers a second time.
        if (com.political.power.game.PowerDamage.inDomainStrike()) {
            return amount;
        }
        float modifiedAmount = amount;

        // === Player taking damage from slayer boss ===
        if (self instanceof ServerPlayer player) {
            if (com.political.power.game.PowerDamage.absorbs(player, source)) {
                return 0.0f;
            }
            if (CustomItemHandler.tryPhaseDodge(player, source, modifiedAmount)) {
                return 0.0f;
            }
            if (source.getEntity() instanceof LivingEntity attacker) {
                com.political.items.DragonPlateHandler.onWearerHit(player, attacker, modifiedAmount);
            }
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
            // A nemesis hitting its victim brings all of its traits with it.
            if (source.getEntity() instanceof LivingEntity nem
                    && com.political.NemesisManager.isNemesis(nem)) {
                modifiedAmount *= com.political.NemesisManager.onNemesisHit(nem, player, modifiedAmount);
            }
            // Grimoire: three of the Ward circle spends Focus as armour before defence applies.
            modifiedAmount = com.political.WarrantArmour.onHurt(player, modifiedAmount);
            // Skyblock-style Defense stat: dmg * 100 / (100 + defense)
            return com.political.stats.PlayerStatsManager.applyIncomingDefense(player, source, modifiedAmount);
        }

        // Damage order: vanilla amount → weapon uniques/slayer bonus (below) → PlayerStats (strength/crit/ferocity)
        // Incoming players already applied defense: dmg * 100 / (100 + def).
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
            modifiedAmount *= com.political.government.PerkManager.getBountyBossDamageMultiplier();
            
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

        // A nemesis reacts to being struck, which is where its defensive traits live.
        if (com.political.NemesisManager.isNemesis(self)) {
            modifiedAmount *= com.political.NemesisManager.onNemesisHurt(player, self, modifiedAmount);
        }

        // Wartime: citizens of polities at war strike harder at one another.
        if (self instanceof ServerPlayer victimPlayer) {
            modifiedAmount *= com.political.settlement.WarManager.wartimeDamageMultiplier(player, victimPlayer);
        }

        if (source.is(DamageTypeTags.IS_PROJECTILE)) {
            modifiedAmount *= com.political.party.PartyPerkEffects.projectileDamageBonusMultiplier(player.getUUID().toString());
        } else {
            modifiedAmount *= com.political.party.PartyPerkEffects.meleeDamageBonusMultiplier(player.getUUID().toString());
        }

        // Last stage: Black Flash and the Domain (power system).
        return com.political.power.game.PowerDamage.outgoing(player, self, source,
                CustomItemHandler.calculateSlayerDamage(player, self, modifiedAmount));
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
        com.political.CollectionManager.onKill(player, self);
        com.political.RareDropManager.onKill(player, self);
        com.political.NemesisManager.onNemesisDeathEffects(self);
        com.political.NemesisManager.onNemesisKilled(player, self);
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
            com.political.settlement.WarManager.onWartimeKill(player, (ServerPlayer) self);
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
            if (com.political.government.DictatorManager.isDictator(killedPlayer.getUUID().toString())) {
                if (source.getEntity() instanceof ServerPlayer killer) {
                    com.political.government.DictatorManager.handleDictatorKilled(killedPlayer, killer);
                }
            }
        }
    }
}