package com.political.mixin;

import com.political.PerkManager;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.screen.EnchantmentScreenHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EnchantmentScreenHandler.class)
public class EnchantmentScreenHandlerMixin {

    @Final
    @Shadow
    public int[] enchantmentPower;

    // Track the original unmodified values
    @Unique
    private int[] political_originalPower = null;

    @Unique
    private float political_lastMultiplier = 1.0f;

    @Inject(method = "generateEnchantments", at = @At("TAIL"))
    private void political_modifyEnchantCost(DynamicRegistryManager registryManager, ItemStack stack, int slot, int level, CallbackInfoReturnable<List<EnchantmentLevelEntry>> cir) {
        float multiplier = PerkManager.getEnchantCostMultiplier();

        // Store original values if this is a fresh generation
        if (political_originalPower == null || political_originalPower.length != this.enchantmentPower.length) {
            political_originalPower = new int[this.enchantmentPower.length];
        }

        // Check if these are fresh values (not yet modified) by comparing to last multiplier
        // or if the multiplier changed
        if (multiplier != political_lastMultiplier || !political_hasBeenModified()) {
            // Store the original values before modification
            for (int i = 0; i < this.enchantmentPower.length; i++) {
                political_originalPower[i] = this.enchantmentPower[i];
            }
        }

        // Apply multiplier from original values, not from already-modified values
        if (multiplier != 1.0f) {
            for (int i = 0; i < this.enchantmentPower.length; i++) {
                if (political_originalPower[i] > 0) {
                    this.enchantmentPower[i] = Math.max(1, (int) Math.ceil(political_originalPower[i] * multiplier));
                }
            }
        }

        political_lastMultiplier = multiplier;
    }

    @Unique
    private boolean political_hasBeenModified() {
        if (political_originalPower == null) return false;

        // Check if current values match what we would expect after modification
        float multiplier = PerkManager.getEnchantCostMultiplier();
        for (int i = 0; i < this.enchantmentPower.length; i++) {
            int expected = (int) Math.ceil(political_originalPower[i] * multiplier);
            if (this.enchantmentPower[i] != expected && political_originalPower[i] > 0) {
                return false; // Values are fresh, not yet modified
            }
        }
        return true;
    }
}