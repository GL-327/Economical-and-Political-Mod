package com.political.backrooms.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.item.consume.UseAction;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class EnergyDrinkItem extends Item {

    public EnergyDrinkItem(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient() && user instanceof PlayerEntity player) {
            // Speed II + Saturation II for 25 seconds (500 ticks)
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 500, 1));
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 500, 1));

            if (!player.isCreative()) {
                stack.decrement(1);
                if (stack.isEmpty()) {
                    player.giveItemStack(new ItemStack(Items.GLASS_BOTTLE));
                } else {
                    player.getInventory().insertStack(new ItemStack(Items.GLASS_BOTTLE));
                }
            }
        }
        return stack;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 32;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        return ItemUsage.consumeHeldItem(world, user, hand);
    }
}
