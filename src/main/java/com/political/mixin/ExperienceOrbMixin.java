package com.political.mixin;

import com.political.GemArmor;
import com.political.MasteryHeartTree;
import com.political.PartyManager;
import com.political.PartyPerkEffects;
import com.political.PerkManager;
import com.political.PlayerBuffManager;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ExperienceOrb.class)
public class ExperienceOrbMixin {

    @Redirect(method = "playerTouch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;giveExperiencePoints(I)V"))
    private void political_redirectExperience(Player player, int original) {
        float multiplier = PerkManager.getMobXpMultiplier();
        
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            String uuid = serverPlayer.getUUID().toString();
            
            // VETERAN buff: +5% XP from all sources
            double xpBonus = PlayerBuffManager.getXpBonus(uuid);
            multiplier *= (1.0f + xpBonus);
            
            // MENTOR buff: +5% XP for party members
            double partyBonus = PartyManager.getPartyXpBonus(uuid);
            multiplier *= (1.0f + partyBonus);
            
            // Lapis armor: +10% XP per piece worn
            double lapisBonus = GemArmor.getLapisXpBonus(player);
            multiplier *= (1.0f + lapisBonus);

            multiplier *= (float) MasteryHeartTree.experienceOrbMultiplier(uuid);
            multiplier *= (float) PartyPerkEffects.experienceOrbMultiplier(uuid);
        }
        
        player.giveExperiencePoints((int) Math.ceil(original * multiplier));
    }
}