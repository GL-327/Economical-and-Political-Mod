package com.political.mixin;

import com.political.PerkManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootWorldContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LootTable.class)
public class LootTableMixin {

    @Inject(method = "generateLoot(Lnet/minecraft/loot/context/LootWorldContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;",
            at = @At("RETURN"))
    private void political_multiplyLoot(LootWorldContext context, CallbackInfoReturnable<ObjectArrayList<ItemStack>> cir) {
        float multiplier = PerkManager.getLootMultiplier();
        if (multiplier <= 1.0f) return;

        ObjectArrayList<ItemStack> loot = cir.getReturnValue();
        if (loot == null) return;

        for (ItemStack stack : loot) {
            if (!stack.isEmpty()) {
                int newCount = (int) Math.ceil(stack.getCount() * multiplier);
                stack.setCount(Math.min(newCount, stack.getMaxCount()));
            }
        }
    }
}