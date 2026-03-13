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

/** Grants Invisibility for 30 seconds when activated. */
public class CloakingDeviceItem extends Item {

    public CloakingDeviceItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient()) return ActionResult.PASS;
        ItemStack stack = user.getStackInHand(hand);
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 600, 0));
        if (user instanceof ServerPlayerEntity sp) {
            sp.sendMessage(Text.literal("👁 Cloaking Device activated - 30s invisibility").formatted(Formatting.GRAY), true);
        }
        if (!user.isCreative()) stack.decrement(1);
        return ActionResult.SUCCESS;
    }
}
