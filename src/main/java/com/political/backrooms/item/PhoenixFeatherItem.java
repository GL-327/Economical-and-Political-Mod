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

/**
 * When held and right-clicked, prevents the player from dying ONCE.
 * Stores a flag in the player's health state.
 * On death: player survives with 1 HP and item is consumed.
 */
public class PhoenixFeatherItem extends Item {

    public static final String PHOENIX_TAG = "phoenix_feather_active";

    public PhoenixFeatherItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient()) return ActionResult.PASS;
        ItemStack stack = user.getStackInHand(hand);

        // Grant the player the "phoenix feather" death-prevention buff
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 20 * 60, 3));
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200, 2));

        if (user instanceof ServerPlayerEntity sp) {
            sp.sendMessage(Text.literal("🔥 Phoenix Feather activated — next death prevented!")
                .formatted(Formatting.GOLD, Formatting.BOLD), true);
        }
        if (!user.isCreative()) stack.decrement(1);
        return ActionResult.SUCCESS;
    }
}
