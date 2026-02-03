package com.political.mixin;

import com.political.PerkManager;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.screen.EnchantmentScreenHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EnchantmentScreenHandler.class)
public class EnchantmentScreenHandlerMixin {

    @Final
    @Shadow
    public int[] enchantmentPower;

    @Inject(method = "generateEnchantments", at = @At("TAIL"))
    private void political_modifyEnchantCost(DynamicRegistryManager registryManager, ItemStack stack, int slot, int level, CallbackInfoReturnable<List<EnchantmentLevelEntry>> cir) {
        float multiplier = PerkManager.getEnchantCostMultiplier();
        if (multiplier != 1.0f) {
            for (int i = 0; i < this.enchantmentPower.length; i++) {
                if (this.enchantmentPower[i] > 0) {
                    this.enchantmentPower[i] = (int) Math.ceil(this.enchantmentPower[i] * multiplier);
                    this.enchantmentPower[i] = Math.max(1, this.enchantmentPower[i]);
                }
            }
        }
    }
}