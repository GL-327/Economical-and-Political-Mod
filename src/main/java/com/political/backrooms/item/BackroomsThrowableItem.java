package com.political.backrooms.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Consumer;

/**
 * Generic throwable/activatable item that applies an AOE effect on use.
 */
public class BackroomsThrowableItem extends Item {

    private final double radius;
    private final Consumer<LivingEntity> effect;
    private final String activationMessage;

    public BackroomsThrowableItem(Settings settings, double radius,
                                  Consumer<LivingEntity> effect, String activationMessage) {
        super(settings);
        this.radius = radius;
        this.effect = effect;
        this.activationMessage = activationMessage;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (world.isClient()) return ActionResult.PASS;
        if (!(user instanceof ServerPlayerEntity player)) return ActionResult.PASS;

        ServerWorld serverWorld = (ServerWorld) world;
        Box aoeBox = new Box(
            user.getX() - radius, user.getY() - radius, user.getZ() - radius,
            user.getX() + radius, user.getY() + radius, user.getZ() + radius
        );

        List<LivingEntity> targets = serverWorld.getEntitiesByClass(
            LivingEntity.class, aoeBox, e -> e != player);

        for (LivingEntity target : targets) {
            effect.accept(target);
        }

        if (!activationMessage.isEmpty() && !targets.isEmpty()) {
            player.sendMessage(
                Text.literal(activationMessage + " (" + targets.size() + " affected)")
                    .formatted(Formatting.YELLOW), true);
        }

        if (!player.isCreative()) {
            stack.decrement(1);
        }

        return ActionResult.SUCCESS;
    }
}
