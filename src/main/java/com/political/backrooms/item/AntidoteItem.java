package com.political.backrooms.item;

import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/** Clears all negative status effects. */
public class AntidoteItem extends Item {

    public AntidoteItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient()) return ActionResult.PASS;
        ItemStack stack = user.getStackInHand(hand);
        user.removeStatusEffect(StatusEffects.POISON);
        user.removeStatusEffect(StatusEffects.WITHER);
        user.removeStatusEffect(StatusEffects.NAUSEA);
        user.removeStatusEffect(StatusEffects.BLINDNESS);
        user.removeStatusEffect(StatusEffects.WEAKNESS);
        user.removeStatusEffect(StatusEffects.SLOWNESS);
        user.removeStatusEffect(StatusEffects.MINING_FATIGUE);
        if (user instanceof ServerPlayerEntity sp) {
            sp.sendMessage(Text.literal("✦ Antidote administered - all negative effects cleared!").formatted(Formatting.GREEN), true);
        }
        if (!user.isCreative()) stack.decrement(1);
        return ActionResult.SUCCESS;
    }
}
