package com.political;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SkeletonBowHandler {

    /** Base cooldown between shots in ticks (0.6 seconds at 20 TPS). Reducible by enchantments in the future. */
    public static final int BASE_SHOT_COOLDOWN_TICKS = 12;

    private static final Map<UUID, Long> shotCooldowns = new ConcurrentHashMap<>();
    /**
     * Tracks players whose current swing has already been counted, to prevent multi-fire per swing.
     * The cooldown in handleInstantShot also acts as a safety net against double-fire from
     * overlapping AttackEntityCallback / AttackBlockCallback and tickSwingDetection triggers.
     */
    private static final Set<UUID> currentlySwinging = ConcurrentHashMap.newKeySet();

    public static boolean isSkeletonBow(ItemStack stack) {
        return SlayerItems.isSkeletonBow(stack);
    }

    /**
     * Instant left-click shot — fires immediately at full power with a cooldown.
     * Called from AttackEntityCallback, AttackBlockCallback, and swing-in-air detection.
     */
    public static void handleInstantShot(ServerPlayerEntity player) {
        ItemStack bow = player.getMainHandStack();
        if (!isSkeletonBow(bow)) return;

        // Check cooldown
        long currentTick = player.getEntityWorld().getTime();
        UUID playerId = player.getUuid();
        long lastShot = shotCooldowns.getOrDefault(playerId, 0L);
        if (currentTick - lastShot < BASE_SHOT_COOLDOWN_TICKS) return;

        // Check for arrows
        ItemStack arrowStack = player.getProjectileType(bow);
        boolean hasArrows = !arrowStack.isEmpty() || player.isCreative();
        if (!hasArrows) return;

        // Create and launch the arrow at full power instantly
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        ItemStack arrowForEntity = arrowStack.isEmpty() ? new ItemStack(Items.ARROW) : arrowStack.copy();
        ArrowEntity arrow = new ArrowEntity(world, player, arrowForEntity, bow);

        // Full-power shot — 50% faster than a normal fully-drawn bow (3.0 -> 4.5)
        arrow.setVelocity(player, player.getPitch(), player.getYaw(), 0.0f, 4.5f, 0.5f);
        // Keep critical flag for visual crit particles, but tag this arrow so the
        // headshot mixin knows it is an instant shot (not a fully-drawn headshot).
        arrow.setCritical(true);

        // Tag for homing behavior (checked in ArrowHomingMixin)
        arrow.getCommandTags().add("skeleton_bow_arrow");
        // Instant shots are never headshots — headshots require a fully-drawn right-click hold.
        arrow.getCommandTags().add("instant_shot");
        // Arrows from the Skeleton Bow cannot be picked up by players.
        arrow.pickupType = PersistentProjectileEntity.PickupPermission.DISALLOWED;

        world.spawnEntity(arrow);

        // Sound
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ARROW_SHOOT,
                SoundCategory.PLAYERS, 1.0f, 1.0f / (world.getRandom().nextFloat() * 0.4f + 1.2f) + 0.5f);

        // Consume arrow if not creative
        if (!player.isCreative() && !arrowStack.isEmpty()) {
            arrowStack.decrement(1);
            if (arrowStack.isEmpty()) {
                player.getInventory().removeOne(arrowStack);
            }
        }

        // Damage bow
        bow.damage(1, player, EquipmentSlot.MAINHAND);

        // Record shot time
        shotCooldowns.put(playerId, currentTick);
    }

    /**
     * Called every tick to detect left-clicks in the air (no block or entity hit).
     * Uses handSwinging to detect new swing events.
     */
    public static void tickSwingDetection(ServerPlayerEntity player) {
        if (!isSkeletonBow(player.getMainHandStack())) {
            currentlySwinging.remove(player.getUuid());
            return;
        }

        UUID playerId = player.getUuid();
        if (player.handSwinging) {
            if (!currentlySwinging.contains(playerId)) {
                // New swing started — fire shot
                currentlySwinging.add(playerId);
                handleInstantShot(player);
            }
        } else {
            // Swing ended — allow next swing to be detected
            currentlySwinging.remove(playerId);
        }
    }
}