package com.political.backrooms.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.item.consume.UseAction;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.function.Consumer;

/**
 * Generic consumable item for Backrooms drinks, foods, and tonics.
 * Applies configurable status effects when consumed.
 */
public class BackroomsConsumableItem extends Item {

    private final UseAction useAction;
    private final Consumer<LivingEntity> effects;
    private final boolean returnsBottle;
    private final boolean returnsBowl;

    public BackroomsConsumableItem(Settings settings, UseAction useAction, Consumer<LivingEntity> effects) {
        this(settings, useAction, effects, false, false);
    }

    public BackroomsConsumableItem(Settings settings, UseAction useAction,
                                   Consumer<LivingEntity> effects, boolean returnsBottle, boolean returnsBowl) {
        super(settings);
        this.useAction = useAction;
        this.effects = effects;
        this.returnsBottle = returnsBottle;
        this.returnsBowl = returnsBowl;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient() && user instanceof PlayerEntity player) {
            effects.accept(user);
            if (!player.isCreative()) {
                stack.decrement(1);
                if (returnsBottle) {
                    ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
                    if (stack.isEmpty()) player.giveItemStack(bottle);
                    else player.getInventory().insertStack(bottle);
                } else if (returnsBowl) {
                    ItemStack bowl = new ItemStack(Items.BOWL);
                    if (stack.isEmpty()) player.giveItemStack(bowl);
                    else player.getInventory().insertStack(bowl);
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
        return useAction;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        return ItemUsage.consumeHeldItem(world, user, hand);
    }
}
