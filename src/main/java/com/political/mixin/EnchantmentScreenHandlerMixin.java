package com.political.mixin;

import com.political.CustomEnchantmentManager;
import com.political.PerkManager;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Random;

@Mixin(EnchantmentScreenHandler.class)
public class EnchantmentScreenHandlerMixin {

    @Final
    @Shadow
    public int[] enchantmentPower;

    @Final
    @Shadow
    private Inventory inventory;

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

    // ── Enchanting table bonus custom enchantments ───────────────

    @Inject(method = "onButtonClick", at = @At("RETURN"))
    private void political_onEnchantApply(PlayerEntity player, int id, CallbackInfoReturnable<Boolean> cir) {
        if (!Boolean.TRUE.equals(cir.getReturnValue())) return;
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;

        ItemStack stack = this.inventory.getStack(0);
        if (stack.isEmpty()) return;

        political_applyBonusEnchantments(serverPlayer, stack);
    }

    @Unique
    private static final Random political_random = new Random();

    @Unique
    private void political_applyBonusEnchantments(ServerPlayerEntity player, ItemStack stack) {
        net.minecraft.entity.EquipmentSlot equipSlot = null;
        net.minecraft.component.type.EquippableComponent equippable =
                stack.get(net.minecraft.component.DataComponentTypes.EQUIPPABLE);
        if (equippable != null) equipSlot = equippable.slot();

        boolean isArmor  = equipSlot == net.minecraft.entity.EquipmentSlot.HEAD
                        || equipSlot == net.minecraft.entity.EquipmentSlot.CHEST
                        || equipSlot == net.minecraft.entity.EquipmentSlot.LEGS
                        || equipSlot == net.minecraft.entity.EquipmentSlot.FEET;
        boolean isHelmet = equipSlot == net.minecraft.entity.EquipmentSlot.HEAD;
        boolean isSword  = stack.isOf(Items.WOODEN_SWORD) || stack.isOf(Items.STONE_SWORD)
                        || stack.isOf(Items.IRON_SWORD)   || stack.isOf(Items.GOLDEN_SWORD)
                        || stack.isOf(Items.DIAMOND_SWORD) || stack.isOf(Items.NETHERITE_SWORD);
        boolean isAxe    = stack.getItem() instanceof net.minecraft.item.AxeItem;
        boolean isWeapon = isSword || isAxe;

        String applied = null;

        // 15% chance for Bounty Protection I on any armor
        if (applied == null && isArmor && political_random.nextDouble() < 0.15) {
            if (CustomEnchantmentManager.getLevel(stack, CustomEnchantmentManager.BOUNTY_PROTECTION) == 0) {
                CustomEnchantmentManager.addEnchantment(stack, CustomEnchantmentManager.BOUNTY_PROTECTION, 1);
                CustomEnchantmentManager.addEnchantmentLore(stack);
                applied = "Bounty Protection I";
            }
        }
        // 10% chance for Slayer Sharpness I on any sword/axe
        if (applied == null && isWeapon && political_random.nextDouble() < 0.10) {
            if (CustomEnchantmentManager.getLevel(stack, CustomEnchantmentManager.SLAYER_SHARPNESS) == 0) {
                CustomEnchantmentManager.addEnchantment(stack, CustomEnchantmentManager.SLAYER_SHARPNESS, 1);
                CustomEnchantmentManager.addEnchantmentLore(stack);
                applied = "Slayer Sharpness I";
            }
        }
        // 5% chance for Mob Bane I on any sword
        if (applied == null && isSword && political_random.nextDouble() < 0.05) {
            if (CustomEnchantmentManager.getLevel(stack, CustomEnchantmentManager.MOB_BANE) == 0) {
                CustomEnchantmentManager.addEnchantment(stack, CustomEnchantmentManager.MOB_BANE, 1);
                CustomEnchantmentManager.addEnchantmentLore(stack);
                applied = "Mob Bane I";
            }
        }
        // 3% chance for Boss Hunter I on any sword
        if (applied == null && isSword && political_random.nextDouble() < 0.03) {
            if (CustomEnchantmentManager.getLevel(stack, CustomEnchantmentManager.BOSS_HUNTER) == 0) {
                CustomEnchantmentManager.addEnchantment(stack, CustomEnchantmentManager.BOSS_HUNTER, 1);
                CustomEnchantmentManager.addEnchantmentLore(stack);
                applied = "Boss Hunter I";
            }
        }
        // 2% chance for Vampiric I on any helmet
        if (applied == null && isHelmet && political_random.nextDouble() < 0.02) {
            if (CustomEnchantmentManager.getLevel(stack, CustomEnchantmentManager.VAMPIRIC) == 0) {
                CustomEnchantmentManager.addEnchantment(stack, CustomEnchantmentManager.VAMPIRIC, 1);
                CustomEnchantmentManager.addEnchantmentLore(stack);
                applied = "Vampiric I";
            }
        }
        // 1% chance for Thunderbolt I on any weapon
        if (applied == null && isWeapon && political_random.nextDouble() < 0.01) {
            if (CustomEnchantmentManager.getLevel(stack, CustomEnchantmentManager.THUNDERBOLT) == 0) {
                CustomEnchantmentManager.addEnchantment(stack, CustomEnchantmentManager.THUNDERBOLT, 1);
                CustomEnchantmentManager.addEnchantmentLore(stack);
                applied = "Thunderbolt I";
            }
        }

        if (applied != null) {
            player.sendMessage(Text.literal("✦ Bonus: ").formatted(Formatting.GOLD)
                    .append(Text.literal(applied).formatted(Formatting.AQUA, Formatting.BOLD))
                    .append(Text.literal(" applied!").formatted(Formatting.GOLD)), false);
        }
    }
}