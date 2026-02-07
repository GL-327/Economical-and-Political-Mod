package com.political.mixin;

import com.political.PerkManager;
import net.minecraft.entity.ExperienceOrbEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ExperienceOrbEntity.class)
public class ExperienceOrbMixin {

    @ModifyArg(method = "onPlayerCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;addExperience(I)V"))
    private int political_modifyExperience(int original) {
        float multiplier = PerkManager.getMobXpMultiplier();
        return (int) Math.ceil(original * multiplier);
    }
}