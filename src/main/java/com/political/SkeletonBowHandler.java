package com.political;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.entity.EquipmentSlot;

public class SkeletonBowHandler {

    // FIX: Use DataComponentTypes instead of hasCustomName()
    public static boolean isSkeletonBow(ItemStack stack) {
        if (stack.getItem() != Items.BOW) return false;

        // 1.21.11: Check for custom name using DataComponentTypes
        Text customName = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (customName == null) return false;

        String name = customName.getString();
        return name.contains("Bone") || name.contains("Skeleton") || name.contains("Desperado");
    }

    public static void onBowRelease(ServerPlayerEntity player, ItemStack bow, int chargeTime) {
        if (!isSkeletonBow(bow)) return;

        ServerWorld world = player.getEntityWorld();

        float power = getPullProgress(chargeTime);
        if (power < 0.1f) return;

        // Check for arrows
        ItemStack arrowStack = player.getProjectileType(bow);
        if (arrowStack.isEmpty() && !player.isCreative()) {
            return;
        }

        // Create arrow - 1.21.11 constructor
        ArrowEntity arrow = new ArrowEntity(world, player, new ItemStack(Items.ARROW), bow);
        arrow.setVelocity(player, player.getPitch(), player.getYaw(), 0.0f, power * 3.0f, 1.0f);

        // FIX: 1.21.11 uses getBaseDamage() and setBaseDamage()
        arrow.setVelocity(player, player.getPitch(), player.getYaw(), 0.0f, power * 4.5f, 1.0f); // 4.5 instead of 3.0 = +50% damage

        // Critical if fully charged
        if (power >= 1.0f) {
            arrow.setCritical(true);
        }

        // Spawn the arrow
        world.spawnEntity(arrow);

        // Play sound
        world.playSound(null, player.getBlockPos(),
                SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0f, 1.0f);

        // Consume arrow if not creative
        if (!player.isCreative()) {
            arrowStack.decrement(1);
            if (arrowStack.isEmpty()) {
                player.getInventory().removeOne(arrowStack);
            }
        }

        // Damage the bow - 1.21.11 syntax
        bow.damage(1, player, player.getActiveHand() == Hand.MAIN_HAND
                ? EquipmentSlot.MAINHAND
                : EquipmentSlot.OFFHAND);
    }

    private static float getPullProgress(int useTicks) {
        float f = (float) useTicks / 20.0f;
        f = (f * f + f * 2.0f) / 3.0f;
        if (f > 1.0f) f = 1.0f;
        return f;
    }
}