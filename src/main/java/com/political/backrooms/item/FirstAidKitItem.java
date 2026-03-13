package com.political.backrooms.item;

import net.minecraft.entity.effect.StatusEffectInstance;
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

/** Heals 10 HP (5 hearts) instantly when used. */
public class FirstAidKitItem extends Item {

    public FirstAidKitItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient()) return ActionResult.PASS;
        ItemStack stack = user.getStackInHand(hand);
        user.heal(10.0f);
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 1));
        if (user instanceof ServerPlayerEntity sp) {
            sp.sendMessage(Text.literal("✚ First Aid Applied! +5 Hearts").formatted(Formatting.GREEN), true);
        }
        if (!user.isCreative()) stack.decrement(1);
        return ActionResult.SUCCESS;
    }
}
