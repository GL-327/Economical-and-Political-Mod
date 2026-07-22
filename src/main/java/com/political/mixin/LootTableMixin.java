package com.political.mixin;

import com.political.CustomEnchantmentManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LootTable.class)
public class LootTableMixin {

    @Inject(method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;",
            at = @At("RETURN"))
    private void political_multiplyLoot(LootContext context, CallbackInfoReturnable<ObjectArrayList<ItemStack>> cir) {
        ObjectArrayList<ItemStack> loot = cir.getReturnValue();
        if (loot == null) return;

        // Temporarily disable loot multipliers to prevent block duplication
        // TODO: Implement proper block vs mob drop detection
        float multiplier = 1.0f;
        float oreMultiplier = 1.0f;

        for (ItemStack stack : loot) {
            if (stack.isEmpty()) continue;

            // Strip any custom enchantments from looted items to prevent
            // players from obtaining pre-enchanted custom-enchant items
            if (CustomEnchantmentManager.hasAnyCustomEnchantment(stack)) {
                CustomEnchantmentManager.removeAllCustomEnchantments(stack);
            }

            int originalCount = stack.getCount();
            float effectiveMultiplier = multiplier;

            // Apply ore multiplier for ore-related items (but not from blocks)
            if (oreMultiplier != 1.0f && isOreRelatedItem(stack)) {
                effectiveMultiplier *= oreMultiplier;
            }

            if (effectiveMultiplier != 1.0f) {
                int newCount = Math.max(1, (int) Math.ceil(originalCount * effectiveMultiplier));
                stack.setCount(Math.min(newCount, stack.getMaxStackSize()));
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