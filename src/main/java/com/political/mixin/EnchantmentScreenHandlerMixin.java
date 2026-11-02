package com.political.mixin;

import com.political.CustomEnchantmentManager;
import com.political.items.CustomItemHandler;
import com.political.government.PerkManager;
import com.political.bounty.SlayerItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(EnchantmentMenu.class)
public class EnchantmentScreenHandlerMixin {

    @Shadow @Final public int[] costs;
    @Shadow @Final public int[] levelClue;
    @Shadow public Container enchantSlots;

    /** Vanilla clue/cost snapshot for this {@code slotsChanged} pass (written by vanilla just before this mixin tail). */
    @Unique
    private int[] political_vanillaLevelClue = null;
    @Unique
    private int[] political_vanillaCosts = null;

    @Unique
    private static final double ENCHANT_LEVEL_MULTIPLIER = 2.0;
    @Unique
    private static final double ENCHANT_COST_MULTIPLIER = 2.0;

    @Inject(method = "slotsChanged", at = @At("TAIL"))
    private void political_modifyEnchantPower(Container container, CallbackInfo ci) {
        float perkMultiplier = PerkManager.getEnchantCostMultiplier();

        if (political_vanillaLevelClue == null || political_vanillaLevelClue.length != this.levelClue.length) {
            political_vanillaLevelClue = new int[this.levelClue.length];
            political_vanillaCosts = new int[this.costs.length];
        }

        // Vanilla just populated levelClue/costs; copy before we overwrite so multipliers always match the real roll.
        System.arraycopy(this.levelClue, 0, political_vanillaLevelClue, 0, this.levelClue.length);
        System.arraycopy(this.costs, 0, political_vanillaCosts, 0, this.costs.length);

        for (int i = 0; i < this.levelClue.length; i++) {
            if (political_vanillaLevelClue[i] > 0) {
                int baseLevel = political_vanillaLevelClue[i];
                if (perkMultiplier != 1.0f) {
                    baseLevel = Math.max(1, (int) Math.ceil(baseLevel * perkMultiplier));
                }
                this.levelClue[i] = Math.min(60, (int) Math.ceil(baseLevel * ENCHANT_COST_MULTIPLIER * ENCHANT_LEVEL_MULTIPLIER));

                int baseCost = political_vanillaCosts[i];
                if (perkMultiplier != 1.0f) {
                    baseCost = Math.max(1, (int) Math.ceil(baseCost * perkMultiplier));
                }
                this.costs[i] = Math.min(60, (int) Math.ceil(baseCost * ENCHANT_COST_MULTIPLIER * ENCHANT_LEVEL_MULTIPLIER));
            }
        }
    }

    @Unique
    private static final int POLITICAL_MAX_TABLE_ENCHANT_LEVEL = 10;

    @Inject(method = "clickMenuButton", at = @At("RETURN"))
    private void political_onEnchantApply(Player player, int id, CallbackInfoReturnable<Boolean> cir) {
        if (!Boolean.TRUE.equals(cir.getReturnValue())) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        ItemStack stack = this.enchantSlots.getItem(0);
        if (stack.isEmpty()) return;

        int costLevel = (id >= 0 && id < this.costs.length) ? this.costs[id] : 0;
        political_enhanceEnchantLevels(stack, costLevel);
        political_applyBonusEnchantments(serverPlayer, stack);
    }

    @Unique
    private static void political_enhanceEnchantLevels(ItemStack stack, int costLevel) {
        ItemEnchantments current = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (current.isEmpty()) return;

        double boostFactor = 1.0 + Math.max(0, (costLevel - 30) / 30.0);

        ItemEnchantments.Mutable mut = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        for (Holder<Enchantment> holder : current.keySet()) {
            int originalLevel = current.getLevel(holder);
            int boostedLevel = (int) Math.ceil(originalLevel * boostFactor);
            mut.set(holder, Math.min(boostedLevel, POLITICAL_MAX_TABLE_ENCHANT_LEVEL));
        }
        stack.set(DataComponents.ENCHANTMENTS, mut.toImmutable());
    }

    @Unique
    private static final Random political_random = new Random();

    @Unique
    private void political_applyBonusEnchantments(ServerPlayer player, ItemStack stack) {
        if (!SlayerItems.isAnyBountyItem(stack) && !CustomItemHandler.isProtectedItem(stack)) {
            return;
        }

        EquipmentSlot equipSlot = null;
        var equippable = stack.get(net.minecraft.core.component.DataComponents.EQUIPPABLE);
        if (equippable != null) equipSlot = equippable.slot();

        boolean isArmor = equipSlot == EquipmentSlot.HEAD
                || equipSlot == EquipmentSlot.CHEST
                || equipSlot == EquipmentSlot.LEGS
                || equipSlot == EquipmentSlot.FEET;
        boolean isHelmet = equipSlot == EquipmentSlot.HEAD;
        boolean isSword = stack.is(Items.WOODEN_SWORD) || stack.is(Items.STONE_SWORD)
                || stack.is(Items.IRON_SWORD) || stack.is(Items.GOLDEN_SWORD)
                || stack.is(Items.DIAMOND_SWORD) || stack.is(Items.NETHERITE_SWORD);
        boolean isAxe = stack.getItem() instanceof net.minecraft.world.item.AxeItem;
        boolean isWeapon = isSword || isAxe;

        String applied = null;

        if (applied == null && isArmor && political_random.nextDouble() < 0.15) {
            if (CustomEnchantmentManager.getLevel(stack, CustomEnchantmentManager.BOUNTY_PROTECTION) == 0) {
                CustomEnchantmentManager.addEnchantment(stack, CustomEnchantmentManager.BOUNTY_PROTECTION, 1);
                CustomEnchantmentManager.addEnchantmentLore(stack);
                applied = "Bounty Protection I";
            }
        }
        if (applied == null && isWeapon && political_random.nextDouble() < 0.10) {
            if (CustomEnchantmentManager.getLevel(stack, CustomEnchantmentManager.SLAYER_SHARPNESS) == 0) {
                CustomEnchantmentManager.addEnchantment(stack, CustomEnchantmentManager.SLAYER_SHARPNESS, 1);
                CustomEnchantmentManager.addEnchantmentLore(stack);
                applied = "Slayer Sharpness I";
            }
        }
        if (applied == null && isSword && political_random.nextDouble() < 0.05) {
            if (CustomEnchantmentManager.getLevel(stack, CustomEnchantmentManager.MOB_BANE) == 0) {
                CustomEnchantmentManager.addEnchantment(stack, CustomEnchantmentManager.MOB_BANE, 1);
                CustomEnchantmentManager.addEnchantmentLore(stack);
                applied = "Mob Bane I";
            }
        }
        if (applied == null && isSword && political_random.nextDouble() < 0.03) {
            if (CustomEnchantmentManager.getLevel(stack, CustomEnchantmentManager.BOSS_HUNTER) == 0) {
                CustomEnchantmentManager.addEnchantment(stack, CustomEnchantmentManager.BOSS_HUNTER, 1);
                CustomEnchantmentManager.addEnchantmentLore(stack);
                applied = "Boss Hunter I";
            }
        }
        if (applied == null && isHelmet && political_random.nextDouble() < 0.02) {
            if (CustomEnchantmentManager.getLevel(stack, CustomEnchantmentManager.VAMPIRIC) == 0) {
                CustomEnchantmentManager.addEnchantment(stack, CustomEnchantmentManager.VAMPIRIC, 1);
                CustomEnchantmentManager.addEnchantmentLore(stack);
                applied = "Vampiric I";
            }
        }
        if (applied == null && isWeapon && political_random.nextDouble() < 0.01) {
            if (CustomEnchantmentManager.getLevel(stack, CustomEnchantmentManager.THUNDERBOLT) == 0) {
                CustomEnchantmentManager.addEnchantment(stack, CustomEnchantmentManager.THUNDERBOLT, 1);
                CustomEnchantmentManager.addEnchantmentLore(stack);
                applied = "Thunderbolt I";
            }
        }

        if (applied != null) {
            player.sendSystemMessage(Component.literal("✦ Bonus: ").withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(applied).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                    .append(Component.literal(" applied!").withStyle(ChatFormatting.GOLD)), false);
        }
    }
}
