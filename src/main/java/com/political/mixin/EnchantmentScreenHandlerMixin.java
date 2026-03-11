package com.political.mixin;

import com.political.CustomEnchantmentManager;
import com.political.CustomItemHandler;
import com.political.PerkManager;
import com.political.SlayerItems;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.entry.RegistryEntry;
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

import java.util.ArrayList;
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

    /**
     * Maximum level multiplier: enchantments can reach up to 2x their vanilla max level.
     * E.g. Sharpness V → X, Protection IV → VIII, etc.
     */
    @Unique
    private static final double ENCHANT_LEVEL_MULTIPLIER = 2.0;

    /**
     * XP level cost multiplier for the boosted enchanting table.
     * Higher enchantments cost proportionally more levels.
     */
    @Unique
    private static final double ENCHANT_COST_MULTIPLIER = 2.0;

    @Inject(method = "generateEnchantments", at = @At("TAIL"), cancellable = true)
    private void political_modifyEnchantCost(DynamicRegistryManager registryManager, ItemStack stack, int slot, int level, CallbackInfoReturnable<List<EnchantmentLevelEntry>> cir) {
        float perkMultiplier = PerkManager.getEnchantCostMultiplier();

        // Store original values if this is a fresh generation
        if (political_originalPower == null || political_originalPower.length != this.enchantmentPower.length) {
            political_originalPower = new int[this.enchantmentPower.length];
        }

        // Check if these are fresh values (not yet modified) by comparing to last multiplier
        // or if the multiplier changed
        if (perkMultiplier != political_lastMultiplier || !political_hasBeenModified()) {
            // Store the original values before modification
            for (int i = 0; i < this.enchantmentPower.length; i++) {
                political_originalPower[i] = this.enchantmentPower[i];
            }
        }

        // Apply perk multiplier from original values, then apply the higher-level cost multiplier
        for (int i = 0; i < this.enchantmentPower.length; i++) {
            if (political_originalPower[i] > 0) {
                int base = political_originalPower[i];
                if (perkMultiplier != 1.0f) {
                    base = Math.max(1, (int) Math.ceil(base * perkMultiplier));
                }
                // Scale costs up to reflect that higher-level enchantments are being given
                this.enchantmentPower[i] = (int) Math.ceil(base * ENCHANT_COST_MULTIPLIER);
            }
        }

        political_lastMultiplier = perkMultiplier;

        // ── Boost enchantment levels in the returned list ──────────────────
        List<EnchantmentLevelEntry> original = cir.getReturnValue();
        if (original == null || original.isEmpty()) return;

        List<EnchantmentLevelEntry> boosted = new ArrayList<>(original.size());
        for (EnchantmentLevelEntry entry : original) {
            RegistryEntry<Enchantment> enchantEntry = entry.enchantment();
            Enchantment enchantment = enchantEntry.value();
            int vanillaMaxLevel = enchantment.getMaxLevel();
            int currentLevel = entry.level();

            // Boost the level up to ENCHANT_LEVEL_MULTIPLIER × vanilla max
            int boostedMax = (int) Math.ceil(vanillaMaxLevel * ENCHANT_LEVEL_MULTIPLIER);
            int newLevel = Math.min(
                    (int) Math.ceil(currentLevel * ENCHANT_LEVEL_MULTIPLIER),
                    boostedMax
            );
            newLevel = Math.max(1, newLevel); // never go below 1
            boosted.add(new EnchantmentLevelEntry(enchantEntry, newLevel));
        }
        cir.setReturnValue(boosted);
    }

    @Unique
    private boolean political_hasBeenModified() {
        if (political_originalPower == null) return false;

        float perkMultiplier = PerkManager.getEnchantCostMultiplier();
        for (int i = 0; i < this.enchantmentPower.length; i++) {
            int base = political_originalPower[i];
            if (perkMultiplier != 1.0f) {
                base = (int) Math.ceil(base * perkMultiplier);
            }
            int expected = (int) Math.ceil(base * ENCHANT_COST_MULTIPLIER);
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
        // Only apply custom enchantments to bounty/custom items, not random vanilla items
        if (!SlayerItems.isAnyBountyItem(stack) && !CustomItemHandler.isProtectedItem(stack)) {
            return;
        }

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