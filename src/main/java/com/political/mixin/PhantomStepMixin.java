package com.political.mixin;

import com.political.bounty.SlayerItems;
import com.political.ArmourAttribute;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PhantomStepMixin {
    // TODO 26.x: re-block sleeping (Player sleep API uses Either return); armour lore still says no sleep.

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        Player player = (Player) (Object) this;

        if (!hasPhantomStepAttribute(player)) {
            return;
        }

        Level world = player.level();
        if (world instanceof ServerLevel) {
            if (!player.hasEffect(MobEffects.INVISIBILITY)) {
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0, false, false, true));
            }
        }

        if (player.fallDistance > 0) {
            player.fallDistance = 0;
        }
    }

    private static boolean hasPhantomStepAttribute(Player player) {
        for (EquipmentSlot slot : new EquipmentSlot[] {
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
        }) {
            if (SlayerItems.getAppliedAttribute(player.getItemBySlot(slot)) == ArmourAttribute.PHANTOMSTEP) {
                return true;
            }
        }
        return false;
    }
}
