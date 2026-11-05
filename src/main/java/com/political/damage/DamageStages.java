package com.political.damage;

import com.political.FishingEventHandler;
import com.political.NemesisManager;
import com.political.PlayerBuffManager;
import com.political.RelicManager;
import com.political.WarrantArmour;
import com.political.bounty.SlayerData;
import com.political.bounty.SlayerItems;
import com.political.bounty.SlayerManager;
import com.political.core.damage.DamagePipeline;
import com.political.core.damage.DamagePipeline.Result;
import com.political.core.damage.DamagePipeline.Step;
import com.political.core.text.Text;
import com.political.government.PerkManager;
import com.political.items.CustomItemHandler;
import com.political.items.DragonPlateHandler;
import com.political.party.PartyPerkEffects;
import com.political.power.game.PowerDamage;
import com.political.settlement.WarManager;
import com.political.stats.PlayerStatsManager;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.ItemStack;

/**
 * The rules that change a hit, in the order they apply (M5). {@code BountyDamageMixin} used to hold all of them in one
 * method; it now calls {@link #incoming} when the entity that is hurt is a player and {@link #outgoing} when a player
 * hurts something, and each builds the list of named stages that {@link DamagePipeline} runs. The order and the
 * arithmetic are the same as before the split; nothing here changes a number.
 *
 * <p>Incoming (a player is hurt): the power system's absorb, the phase dodge, Dragon plate retaliation, relic
 * reductions, the slayer level reduction, nemesis traits, the warrant armour, then the Defense stat last.</p>
 *
 * <p>Outgoing (a player hurts something): slayer boss bonuses, the undead hunter buff, the slayer sword level check
 * (which ends the calculation when the player is too low a level), sea creature bonuses, relic strikes, nemesis
 * defences, wartime, party perks, then the weapon's own damage and the power system's Black Flash and Domain last.</p>
 */
public final class DamageStages {

    private DamageStages() {
    }

    /** The damage a player takes. The caller has already let a Domain strike through untouched. */
    public static float incoming(ServerPlayer player, DamageSource source, float amount) {
        List<Step> steps = List.of(
                Step.of("power-absorb", a -> PowerDamage.absorbs(player, source) ? Result.done(0.0f) : Result.next(a)),
                Step.of("phase-dodge", a -> CustomItemHandler.tryPhaseDodge(player, source, a) ? Result.done(0.0f) : Result.next(a)),
                Step.of("dragon-plate", a -> {
                    if (source.getEntity() instanceof LivingEntity attacker) {
                        DragonPlateHandler.onWearerHit(player, attacker, a);
                    }
                    return Result.next(a);
                }),
                Step.of("relics", a -> {
                    a *= RelicManager.onHurt(player, a);
                    return Result.next(a);
                }),
                Step.of("slayer-level-reduction", a -> {
                    if (source.getEntity() instanceof LivingEntity attacker && SlayerManager.isSlayerBoss(attacker.getUUID())) {
                        SlayerManager.SlayerType bossType = SlayerManager.getBossSlayerType(attacker.getUUID());
                        if (bossType != null) {
                            double reduction = SlayerManager.getLevelDamageReduction(player, bossType);
                            a *= (1.0f - (float) reduction);
                        }
                    }
                    return Result.next(a);
                }),
                // A nemesis hitting its victim brings all of its traits with it.
                Step.of("nemesis-traits", a -> {
                    if (source.getEntity() instanceof LivingEntity nem && NemesisManager.isNemesis(nem)) {
                        a *= NemesisManager.onNemesisHit(nem, player, a);
                    }
                    return Result.next(a);
                }),
                // Grimoire: three of the Ward circle spends Focus as armour before defence applies.
                Step.of("warrant-armour", a -> Result.next(WarrantArmour.onHurt(player, a))),
                // Skyblock-style Defense stat: dmg * 100 / (100 + defense)
                Step.of("defense", a -> Result.next(PlayerStatsManager.applyIncomingDefense(player, source, a))));
        return DamagePipeline.amount(amount, steps);
    }

    /** The damage a player deals to {@code self} (anything that is not the player's own hurt). */
    public static float outgoing(ServerPlayer player, LivingEntity self, DamageSource source, float amount) {
        List<Step> steps = List.of(
                Step.of("slayer-boss-bonus", a -> {
                    if (!SlayerManager.isSlayerBoss(self.getUUID())) {
                        return Result.next(a);
                    }
                    SlayerManager.SlayerType bossType = SlayerManager.getBossSlayerType(self.getUUID());
                    if (bossType != null) {
                        double multiplier = SlayerManager.getLevelDamageMultiplier(player, bossType);
                        a *= (float) multiplier;
                    }
                    // HUNTERS_INSTINCT perk damage bonus against any bounty boss
                    a *= PerkManager.getBountyBossDamageMultiplier();
                    // SLAYER_ELITE buff: +10% bounty damage
                    double slayerEliteBonus = PlayerBuffManager.getBountyDamageBonus(player.getUUID().toString());
                    a *= (1.0f + (float) slayerEliteBonus);
                    return Result.next(a);
                }),
                // UNDEAD_HUNTER buff: +15% damage to undead mobs
                Step.of("undead-hunter", a -> {
                    if (self.getType().getCategory() == MobCategory.MONSTER && isUndead(self)) {
                        double undeadBonus = PlayerBuffManager.getUndeadDamageBonus(player.getUUID().toString());
                        a *= (1.0f + (float) undeadBonus);
                    }
                    return Result.next(a);
                }),
                // A slayer sword needs its slayer level; below it the hit goes through as it is and the rest is skipped.
                Step.of("slayer-sword-requirement", a -> {
                    ItemStack weapon = player.getMainHandItem();
                    if (SlayerItems.isSlayerSword(weapon)) {
                        SlayerManager.SlayerType swordType = SlayerItems.getSwordSlayerType(weapon);
                        if (swordType != null) {
                            int playerLevel = SlayerData.getSlayerLevel(player.getUUID().toString(), swordType);
                            int requiredLevel = SlayerItems.getSwordLevelRequirement(weapon);
                            if (playerLevel < requiredLevel) {
                                player.sendSystemMessage(Text.tr("political.damage.slayer_sword_level", Text.plain(swordType.displayName), requiredLevel)
                                        .withStyle(ChatFormatting.RED), true);
                                return Result.done(a);
                            }
                        }
                    }
                    return Result.next(a);
                }),
                Step.of("sea-creature", a -> {
                    a *= (float) FishingEventHandler.getSeaCreatureDamageMultiplier(player, self);
                    return Result.next(a);
                }),
                // Relics that fire when you land a strike (no change to the number).
                Step.of("relic-strike", a -> {
                    RelicManager.onHit(player, self);
                    return Result.next(a);
                }),
                // A nemesis reacts to being struck, which is where its defensive traits live.
                Step.of("nemesis-defence", a -> {
                    if (NemesisManager.isNemesis(self)) {
                        a *= NemesisManager.onNemesisHurt(player, self, a);
                    }
                    return Result.next(a);
                }),
                // Wartime: citizens of polities at war strike harder at one another.
                Step.of("wartime", a -> {
                    if (self instanceof ServerPlayer victimPlayer) {
                        a *= WarManager.wartimeDamageMultiplier(player, victimPlayer);
                    }
                    return Result.next(a);
                }),
                Step.of("party-perk", a -> {
                    if (source.is(DamageTypeTags.IS_PROJECTILE)) {
                        a *= PartyPerkEffects.projectileDamageBonusMultiplier(player.getUUID().toString());
                    } else {
                        a *= PartyPerkEffects.meleeDamageBonusMultiplier(player.getUUID().toString());
                    }
                    return Result.next(a);
                }),
                // Last stage: the weapon's own damage, then Black Flash and the Domain (power system).
                Step.of("weapon-and-power", a -> Result.next(
                        PowerDamage.outgoing(player, self, source, CustomItemHandler.calculateSlayerDamage(player, self, a)))));
        return DamagePipeline.amount(amount, steps);
    }

    private static boolean isUndead(LivingEntity self) {
        return self instanceof net.minecraft.world.entity.monster.zombie.Zombie
                || self instanceof net.minecraft.world.entity.monster.skeleton.Skeleton
                || self instanceof net.minecraft.world.entity.monster.skeleton.WitherSkeleton
                || self instanceof net.minecraft.world.entity.monster.skeleton.Stray
                || self instanceof net.minecraft.world.entity.monster.zombie.Husk
                || self instanceof net.minecraft.world.entity.monster.zombie.Drowned
                || self instanceof net.minecraft.world.entity.monster.zombie.ZombifiedPiglin
                || self instanceof net.minecraft.world.entity.monster.Zoglin
                || self instanceof net.minecraft.world.entity.monster.Phantom;
    }
}
