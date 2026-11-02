package com.political;
import com.political.mixin.ArrowHomingMixin;



import com.political.bounty.SlayerItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;

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
    public static void handleInstantShot(ServerPlayer player) {
        ItemStack bow = player.getMainHandItem();
        if (!isSkeletonBow(bow)) return;

        // Check cooldown
        long currentTick = player.level().getServer().getTickCount();
        UUID playerId = player.getUUID();
        long lastShot = shotCooldowns.getOrDefault(playerId, 0L);
        if (currentTick - lastShot < BASE_SHOT_COOLDOWN_TICKS) return;

        // Check for arrows
        ItemStack arrowStack = player.getProjectile(bow);
        boolean hasArrows = !arrowStack.isEmpty() || player.isCreative();
        if (!hasArrows) return;

        // Create and launch the arrow at full power instantly
        ServerLevel world = (ServerLevel) player.level();
        ItemStack arrowForEntity = arrowStack.isEmpty() ? new ItemStack(Items.ARROW) : arrowStack.copy();
        Arrow arrow = new Arrow(world, player, arrowForEntity, bow);

        // Full-power shot — 50% faster than a normal fully-drawn bow (3.0 -> 4.5)
        Vec3 dir = player.calculateViewVector(player.getXRot(), player.getYRot());
        arrow.setDeltaMovement(dir.scale(4.5));
        // Keep critical flag for visual crit particles, but tag this arrow so the
        // headshot mixin knows it is an instant shot (not a fully-drawn headshot).
        arrow.setCritArrow(true);

        // Tag for homing behavior (checked in ArrowHomingMixin)
        arrow.addTag("skeleton_bow_arrow");
        // Instant shots are never headshots — headshots require a fully-drawn right-click hold.
        arrow.addTag("instant_shot");
        // Arrows from the Skeleton Bow cannot be picked up by players.
        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;

        world.addFreshEntity(arrow);

        // Sound
        world.playSound(null, player.blockPosition(), SoundEvents.ARROW_SHOOT,
                SoundSource.PLAYERS, 1.0f, 1.0f / (world.getRandom().nextFloat() * 0.4f + 1.2f) + 0.5f);

        // Consume arrow if not creative
        if (!player.isCreative() && !arrowStack.isEmpty()) {
            arrowStack.shrink(1);
        }

        // Damage bow
        bow.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);

        // Record shot time
        shotCooldowns.put(playerId, currentTick);
    }

    /**
     * Called every tick to detect left-clicks in the air (no block or entity hit).
     * Uses {@link net.minecraft.world.entity.LivingEntity#swinging} to detect new swing events.
     */
    public static void tickSwingDetection(ServerPlayer player) {
        if (!isSkeletonBow(player.getMainHandItem())) {
            currentlySwinging.remove(player.getUUID());
            return;
        }

        UUID playerId = player.getUUID();
        if (player.swinging) {
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