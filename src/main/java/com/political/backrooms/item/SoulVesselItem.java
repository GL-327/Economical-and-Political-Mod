package com.political.backrooms.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/** A rare item that, when used, grants the player a large amount of XP. */
public class SoulVesselItem extends Item {

    private static final int XP_AMOUNT = 500;

    public SoulVesselItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient()) return ActionResult.PASS;
        ItemStack stack = user.getStackInHand(hand);
        user.addExperience(XP_AMOUNT);
        if (user instanceof ServerPlayerEntity sp) {
            sp.sendMessage(Text.literal("✨ Soul Vessel released — +" + XP_AMOUNT + " XP!").formatted(Formatting.LIGHT_PURPLE), true);
        }
        if (!user.isCreative()) stack.decrement(1);
        return ActionResult.SUCCESS;
    }
}
