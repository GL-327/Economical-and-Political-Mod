package com.political.mixin;

import com.political.CustomArrows;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Tags arrow entities when shot from custom arrow stacks (Mojmap 26.x Arrow ctor).
 */
@Mixin(Arrow.class)
public class CustomArrowEntityMixin {

    @Inject(
            method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("TAIL"))
    private void political_onArrowCreated(Level world, LivingEntity owner, ItemStack stack, ItemStack shotFrom, CallbackInfo ci) {
        Arrow self = (Arrow) (Object) this;
        if (!CustomArrows.isCustomArrow(stack)) {
            return;
        }
        CustomArrows.ArrowType type = CustomArrows.getArrowType(stack);
        if (type != null) {
            self.addTag("custom_arrow");
            self.addTag("arrow_type_" + type.name().toLowerCase());
        }
    }
}
