package com.political.backrooms.items;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import com.political.backrooms.items.BackroomsItemsManager;

/**
 * Handles special functionality for backrooms items
 */
public class BackroomsItemHandler {

    /**
     * Handle item usage for backrooms items
     */
    public static boolean handleItemUse(PlayerEntity player, ItemStack stack, Hand hand) {
        String itemId = BackroomsItemsManager.getItemId(stack);
        if (itemId == null) return false;

        World world = player.getWorld();

        switch (itemId) {
            case "infused_echo_shard":
                return handleInfusedEchoShard(player, stack, world);
            case "almond_water":
                return handleAlmondWater(player, stack, world);
            case "energy_drink":
                return handleEnergyDrink(player, stack, world);
            case "special_torch":
                return handleSpecialTorch(player, stack, world);
            default:
                return false;
        }
    }

    /**
     * Handle infused echo shard - creates blue explosion and stuns enemies
     */
    private static boolean handleInfusedEchoShard(PlayerEntity player, ItemStack stack, World world) {
        if (world.isClient()) return true;

        // Create blue explosion effect
        world.createExplosion(player, player.getX(), player.getY(), player.getZ(), 2.0F, 
            net.minecraft.world.explosion.Explosion.DestructionType.NONE);

        // Play high-pitched sound
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENTITY_WITCH_CELEBRATE, SoundCategory.PLAYERS, 1.0F, 2.0F);

        // Stun nearby enemies
        stunNearbyEnemies(world, player.getX(), player.getY(), player.getZ(), 8.0);

        // Consume item if not in creative
        if (!player.getAbilities().creativeMode) {
            stack.decrement(1);
        }

        return true;
    }

    /**
     * Handle almond water - gives Saturation III for 25 seconds
     */
    private static boolean handleAlmondWater(PlayerEntity player, ItemStack stack, World world) {
        if (world.isClient()) return true;

        // Apply Saturation III for 25 seconds (500 ticks)
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 500, 2, false, false, false));

        // Play drinking sound
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENTITY_GENERIC_DRINK, SoundCategory.PLAYERS, 1.0F, 1.0F);

        // Consume item if not in creative
        if (!player.getAbilities().creativeMode) {
            stack.decrement(1);
        }

        return true;
    }

    /**
     * Handle energy drink - gives Speed II and Saturation II for 25 seconds
     */
    private static boolean handleEnergyDrink(PlayerEntity player, ItemStack stack, World world) {
        if (world.isClient()) return true;

        // Apply Speed II for 25 seconds (500 ticks)
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 500, 1, false, false, false));

        // Apply Saturation II for 25 seconds (500 ticks)
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 500, 1, false, false, false));

        // Play drinking sound
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENTITY_GENERIC_DRINK, SoundCategory.PLAYERS, 1.0F, 1.0F);

        // Consume item if not in creative
        if (!player.getAbilities().creativeMode) {
            stack.decrement(1);
        }

        return true;
    }

    /**
     * Handle special torch - bright light that deters light-sensitive enemies
     */
    private static boolean handleSpecialTorch(PlayerEntity player, ItemStack stack, World world) {
        if (world.isClient()) return true;

        // Create bright light effect
        for (int i = 0; i < 20; i++) {
            double offsetX = (world.random.nextDouble() - 0.5) * 2.0;
            double offsetY = world.random.nextDouble() * 2.0;
            double offsetZ = (world.random.nextDouble() - 0.5) * 2.0;

            // Add bright particles
            world.addParticle(net.minecraft.particle.ParticleTypes.END_ROD,
                player.getX() + offsetX, player.getY() + offsetY, player.getZ() + offsetZ,
                0, 0, 0);
        }

        // Deter light-sensitive enemies (smilers, etc.)
        deterLightSensitiveEnemies(world, player.getX(), player.getY(), player.getZ(), 12.0);

        // Play placement sound
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.PLAYERS, 1.0F, 1.0F);

        return true; // Don't consume torch - it's reusable
    }

    /**
     * Stun nearby enemies with slowness and weakness
     */
    private static void stunNearbyEnemies(World world, double x, double y, double z, double radius) {
        world.getEntitiesByClass(net.minecraft.entity.LivingEntity.class,
            net.minecraft.util.math.Box.of(net.minecraft.util.math.Vec3d.of(x, y, z), radius, radius, radius),
            entity -> entity != null && !(entity instanceof PlayerEntity))
            .forEach(entity -> {
                // Apply stun effects
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 4, false, false, false));
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 2, false, false, false));

                // Knockback
                net.minecraft.util.math.Vec3d knockback = entity.getPos().subtract(net.minecraft.util.math.Vec3d.of(x, y, z)).normalize().multiply(2.0);
                entity.addVelocity(knockback.x, knockback.y + 0.5, knockback.z);
            });
    }

    /**
     * Deter light-sensitive enemies by making them flee
     */
    private static void deterLightSensitiveEnemies(World world, double x, double y, double z, double radius) {
        world.getEntitiesByClass(net.minecraft.entity.LivingEntity.class,
            net.minecraft.util.math.Box.of(net.minecraft.util.math.Vec3d.of(x, y, z), radius, radius, radius),
            entity -> entity != null && !(entity instanceof PlayerEntity))
            .forEach(entity -> {
                // Make enemies flee from the light
                net.minecraft.util.math.Vec3d fleeDirection = entity.getPos().subtract(net.minecraft.util.math.Vec3d.of(x, y, z)).normalize().multiply(3.0);
                entity.addVelocity(fleeDirection.x, 0.5, fleeDirection.z);

                // Apply temporary fear effect
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 60, 0, false, false, false));
            });
    }
}
