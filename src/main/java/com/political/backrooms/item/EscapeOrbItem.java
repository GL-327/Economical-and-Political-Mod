package com.political.backrooms.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/** Teleports the player forward 20 blocks in the direction they're looking. */
public class EscapeOrbItem extends Item {

    public EscapeOrbItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient()) return ActionResult.PASS;
        ItemStack stack = user.getStackInHand(hand);

        Vec3d look = user.getRotationVec(1.0f);
        double tx = user.getX() + look.x * 20;
        double ty = user.getY() + look.y * 10;
        double tz = user.getZ() + look.z * 20;

        // Clamp vertical
        ty = Math.max(world.getBottomY(), Math.min(world.getTopYInclusive(), ty));

        user.teleport(tx, ty, tz, true);

        if (user instanceof ServerPlayerEntity sp) {
            sp.sendMessage(Text.literal("💨 Escaped!").formatted(Formatting.YELLOW), true);
        }
        if (!user.isCreative()) stack.decrement(1);
        return ActionResult.SUCCESS;
    }
}
