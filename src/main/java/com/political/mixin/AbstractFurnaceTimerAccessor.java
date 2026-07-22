package com.political.mixin;

import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractFurnaceBlockEntity.class)
public interface AbstractFurnaceTimerAccessor {

    @Accessor("cookingTimer")
    void political_setCookingTimer(int value);

    @Accessor("cookingTimer")
    int political_getCookingTimer();
}
