package com.political.mixin;

import com.political.PerkManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.registry.tag.BlockTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LootTable.class)
public class LootTableMixin {

    @Inject(method = "generateLoot(Lnet/minecraft/loot/context/LootWorldContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;",
            at = @At("RETURN"))
    private void political_multiplyLoot(LootWorldContext context, CallbackInfoReturnable<ObjectArrayList<ItemStack>> cir) {
        ObjectArrayList<ItemStack> loot = cir.getReturnValue();
        if (loot == null) return;

        // General loot multiplier
        float multiplier = PerkManager.getLootMultiplier();

        // Ore drop multiplier (stacks with general loot multiplier if both active)
        float oreMultiplier = PerkManager.getOreDropMultiplier();

        for (ItemStack stack : loot) {
            if (stack.isEmpty()) continue;

            int originalCount = stack.getCount();
            float effectiveMultiplier = multiplier;

            // Apply ore multiplier for ore-related items
            if (oreMultiplier != 1.0f && isOreRelatedItem(stack)) {
                effectiveMultiplier *= oreMultiplier;
            }

            if (effectiveMultiplier != 1.0f) {
                int newCount = Math.max(1, (int) Math.ceil(originalCount * effectiveMultiplier));
                stack.setCount(Math.min(newCount, stack.getMaxCount()));
            }
        }
    }

    private boolean isOreRelatedItem(ItemStack stack) {
        // Check if item is a raw ore material or gem
        String itemId = stack.getItem().toString().toLowerCase();
        return itemId.contains("raw_") ||
                itemId.contains("diamond") ||
                itemId.contains("emerald") ||
                itemId.contains("lapis") ||
                itemId.contains("redstone") ||
                itemId.contains("coal") ||
                itemId.contains("quartz") ||
                itemId.contains("copper");
    }
}