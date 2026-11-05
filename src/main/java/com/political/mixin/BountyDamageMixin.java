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
        // The rules and their order are in DamageStages (M5): a player taking damage, or a player dealing it.
        if (self instanceof ServerPlayer player) {
            return com.political.damage.DamageStages.incoming(player, source, amount);
        }
        if (!(source.getEntity() instanceof ServerPlayer attacker)) {
            return amount;
        }
        return com.political.damage.DamageStages.outgoing(attacker, self, source, amount);
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