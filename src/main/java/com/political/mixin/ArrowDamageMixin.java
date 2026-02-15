package com.political.mixin;

import com.political.SkeletonBowHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PersistentProjectileEntity.class)
public class ArrowDamageMixin {

    @ModifyVariable(method = "onEntityHit", at = @At("STORE"), ordinal = 0)
    private int modifyArrowDamage(int damage) {
        PersistentProjectileEntity self = (PersistentProjectileEntity)(Object)this;

        // Check if this is a skeleton bow arrow
        if (self.getCommandTags().contains("skeleton_bow_arrow")) {
            // 50% more damage
            return (int)(damage * 1.5f);
        }

        return damage;
    }
}