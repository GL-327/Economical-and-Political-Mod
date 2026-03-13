package com.political.backrooms.item;

import net.minecraft.entity.mob.MobEntity;
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

/** Resets aggro of all mobs within 20 blocks when used. */
public class MemoryWiperItem extends Item {

    public MemoryWiperItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient()) return ActionResult.PASS;
        if (!(world instanceof ServerWorld serverWorld)) return ActionResult.PASS;

        ItemStack stack = user.getStackInHand(hand);
        Box box = new Box(user.getX() - 20, user.getY() - 10, user.getZ() - 20,
            user.getX() + 20, user.getY() + 10, user.getZ() + 20);

        List<MobEntity> mobs = serverWorld.getEntitiesByClass(MobEntity.class, box, e -> true);
        for (MobEntity mob : mobs) {
            mob.setTarget(null);
        }

        if (user instanceof ServerPlayerEntity sp) {
            sp.sendMessage(Text.literal("🧠 Memory Wiper — " + mobs.size() + " mobs forgot about you!")
                .formatted(Formatting.AQUA), true);
        }
        if (!user.isCreative()) stack.decrement(1);
        return ActionResult.SUCCESS;
    }
}
