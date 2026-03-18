package com.political.mixin;

import com.political.GemArmor;
import com.political.PartyManager;
import com.political.PerkManager;
import com.political.PlayerBuffManager;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ExperienceOrbEntity.class)
public class ExperienceOrbMixin {

    @Redirect(method = "onPlayerCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;addExperience(I)V"))
    private void political_redirectExperience(PlayerEntity player, int original) {
        float multiplier = PerkManager.getMobXpMultiplier();
        
        if (player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
            String uuid = serverPlayer.getUuidAsString();
            
            // VETERAN buff: +5% XP from all sources
            double xpBonus = PlayerBuffManager.getXpBonus(uuid);
            multiplier *= (1.0f + xpBonus);
            
            // MENTOR buff: +5% XP for party members
            double partyBonus = PartyManager.getPartyXpBonus(uuid);
            multiplier *= (1.0f + partyBonus);
            
            // Lapis armor: +10% XP per piece worn
            double lapisBonus = GemArmor.getLapisXpBonus(player);
            multiplier *= (1.0f + lapisBonus);
        }
        
        player.addExperience((int) Math.ceil(original * multiplier));
    }
}