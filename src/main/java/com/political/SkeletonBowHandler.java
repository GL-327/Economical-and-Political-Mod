package com.political;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

public class SkeletonBowHandler {

    public static boolean isSkeletonBow(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.getItem() != Items.BOW) return false;
        Text customName = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (customName == null) return false;
        String name = customName.getString();
        return name.contains("Bone") || name.contains("Skeleton") || name.contains("Desperado");
    }

    /**
     * Handle custom bow shot - called from BowItemMixin
     * Returns true if we handled the shot
     */
    public static boolean handleBowShot(ServerPlayerEntity player, ItemStack bow, int chargeTime) {
        if (!isSkeletonBow(bow)) return false;

        ServerWorld world = player.getEntityWorld();
        float power = getPullProgress(chargeTime);

        if (power < 0.1f) return true; // Too weak, consume but don't fire

        // Check for arrows
        ItemStack arrowStack = player.getProjectileType(bow);
        boolean hasArrows = !arrowStack.isEmpty() || player.isCreative();

        if (!hasArrows) {
            return true; // No arrows, consume the action
        }

        // Create the arrow
        ItemStack arrowForEntity = arrowStack.isEmpty() ? new ItemStack(Items.ARROW) : arrowStack.copy();
        ArrowEntity arrow = new ArrowEntity(world, player, arrowForEntity, bow);

        // Set velocity - 50% faster and more accurate than normal bow (3.0 -> 4.5)
        arrow.setVelocity(player, player.getPitch(), player.getYaw(), 0.0f, power * 4.5f, 0.5f);

        // Critical if fully charged (this also increases damage in vanilla)
        if (power >= 1.0f) {
            arrow.setCritical(true);
        }

        // Set higher base damage via power enchantment effect simulation
        // ArrowEntity calculates damage based on velocity, so higher velocity = more damage
        // The 4.5f multiplier already gives ~50% more damage

        // For extra damage boost, we can use the arrow's punch/power
        // Or handle it in a damage mixin

        // Tag for homing behavior (checked in ArrowHomingMixin)
        arrow.getCommandTags().add("skeleton_bow_arrow");

        // Spawn arrow
        world.spawnEntity(arrow);

        // Sound
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ARROW_SHOOT,
                SoundCategory.PLAYERS, 1.0f, 1.0f / (world.getRandom().nextFloat() * 0.4f + 1.2f) + power * 0.5f);

        // Consume arrow if not creative
        if (!player.isCreative() && !arrowStack.isEmpty()) {
            arrowStack.decrement(1);
            if (arrowStack.isEmpty()) {
                player.getInventory().removeOne(arrowStack);
            }
        }

        // Damage bow
        EquipmentSlot slot = player.getActiveHand() == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        bow.damage(1, player, slot);

        return true;
    }

    private static float getPullProgress(int useTicks) {
        float f = (float) useTicks / 20.0f;
        f = (f * f + f * 2.0f) / 3.0f;
        if (f > 1.0f) f = 1.0f;
        return f;
    }
}