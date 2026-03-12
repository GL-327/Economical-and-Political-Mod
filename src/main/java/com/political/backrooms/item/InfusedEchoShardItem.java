package com.political.backrooms.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class InfusedEchoShardItem extends Item {

    public InfusedEchoShardItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, net.minecraft.entity.player.PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (world.isClient()) return ActionResult.PASS;
        if (!(user instanceof ServerPlayerEntity player)) return ActionResult.PASS;

        ServerWorld serverWorld = (ServerWorld) world;
        Vec3d pos = player.getEyePos();

        // Stun all entities within 8 blocks except the player
        Box stunBox = new Box(pos.x - 8, pos.y - 8, pos.z - 8, pos.x + 8, pos.y + 8, pos.z + 8);
        List<LivingEntity> targets = serverWorld.getEntitiesByClass(LivingEntity.class, stunBox, e -> e != player);

        for (LivingEntity target : targets) {
            // Slowness 10 + Weakness 10 for 3 seconds (60 ticks)
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 9));
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 60, 9));
        }

        serverWorld.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 1.0f, 2.0f);

        serverWorld.spawnParticles(net.minecraft.particle.ParticleTypes.SONIC_BOOM,
            pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);

        if (!targets.isEmpty()) {
            player.sendMessage(
                Text.literal("⚡ Stunned " + targets.size() + " entities!").formatted(Formatting.AQUA),
                true);
        }

        if (!player.isCreative()) {
            stack.decrement(1);
        }

        return ActionResult.SUCCESS;
    }
}
