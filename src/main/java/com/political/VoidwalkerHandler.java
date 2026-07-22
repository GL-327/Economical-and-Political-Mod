package com.political;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Voidwalker's Crown (head, id {@code voidwalker_crown}).
 *
 * <ul>
 *   <li><b>Void Step</b> — sprint + sneak together to blink several blocks in your look
 *       direction (phases through thin walls), on a short cooldown.</li>
 *   <li><b>Phase-Dodge</b> — a chance to phase out of an incoming hit entirely, blinking a
 *       short distance and taking no damage.</li>
 *   <li><b>Dark Affinity</b> — bonus outgoing damage at night or against foes wrapped in
 *       Darkness / Blindness.</li>
 * </ul>
 */
public final class VoidwalkerHandler {

    private VoidwalkerHandler() {}

    private static final String CROWN_ID = "voidwalker_crown";
    private static final Map<UUID, Long> voidStepCooldown = new HashMap<>();
    private static final long VOID_STEP_COOLDOWN_MS = 2500L;
    private static final float PHASE_DODGE_CHANCE = 0.20f;
    private static final float DARK_DAMAGE_MULT = 1.20f;

    public static boolean isWearing(ServerPlayer player) {
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        return CROWN_ID.equals(SlayerItems.getCustomItemId(head));
    }

    // ════════════════════════════════════════════════════════════
    // PASSIVE TICK  (call once per player tick)
    // ════════════════════════════════════════════════════════════
    public static void tickPlayer(ServerPlayer player) {
        if (!isWearing(player)) return;
        ServerLevel world = (ServerLevel) player.level();

        // See in the dark like a creature of the void.
        if (!player.hasEffect(MobEffects.NIGHT_VISION)
                || player.getEffect(MobEffects.NIGHT_VISION).getDuration() < 40) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, true, false, false));
        }

        // Ambient void shimmer.
        if (player.tickCount % 8 == 0) {
            world.sendParticles(ParticleTypes.PORTAL,
                    player.getX(), player.getY() + player.getBbHeight() * 0.6, player.getZ(),
                    2, 0.3, 0.4, 0.3, 0.02);
        }

        // Void Step: sprint + sneak to blink.
        if (player.isSprinting() && player.isShiftKeyDown()) {
            long now = System.currentTimeMillis();
            long ready = voidStepCooldown.getOrDefault(player.getUUID(), 0L);
            if (now >= ready) {
                voidStepCooldown.put(player.getUUID(), now + VOID_STEP_COOLDOWN_MS);
                blink(player, world, 4.0);
            }
        }
    }

    // ════════════════════════════════════════════════════════════
    // PHASE-DODGE  (call from incoming-damage path; true = dodged, deal 0)
    // ════════════════════════════════════════════════════════════
    public static boolean tryPhaseDodge(ServerPlayer player, DamageSource source, float amount) {
        if (amount <= 0) return false;
        if (!isWearing(player)) return false;
        // Cannot phase out of the void itself / true-damage sources.
        if (source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) return false;
        if (player.getRandom().nextFloat() >= PHASE_DODGE_CHANCE) return false;

        ServerLevel world = (ServerLevel) player.level();
        blink(player, world, 3.0);
        world.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.7f, 1.3f);
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§5✦ Phased!"), true);
        return true;
    }

    // ════════════════════════════════════════════════════════════
    // DARK AFFINITY  (call from calculateSlayerDamage)
    // ════════════════════════════════════════════════════════════
    public static float applyDarkDamageBonus(ServerPlayer attacker, LivingEntity target, float damage) {
        if (!isWearing(attacker)) return damage;
        ServerLevel world = (ServerLevel) attacker.level();
        boolean night = !world.isBrightOutside();
        boolean shrouded = target.hasEffect(MobEffects.DARKNESS) || target.hasEffect(MobEffects.BLINDNESS);
        if (night || shrouded) {
            return damage * DARK_DAMAGE_MULT;
        }
        return damage;
    }

    // ════════════════════════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════════════════════════
    private static void blink(ServerPlayer player, ServerLevel world, double maxDistance) {
        Vec3 look = player.getViewVector(1.0f);
        Vec3 flat = new Vec3(look.x, 0, look.z).normalize();
        double startX = player.getX(), startY = player.getY(), startZ = player.getZ();

        double best = 0;
        for (double d = maxDistance; d >= 1.0; d -= 1.0) {
            double tx = startX + flat.x * d;
            double tz = startZ + flat.z * d;
            var feet = net.minecraft.core.BlockPos.containing(tx, startY, tz);
            var head = feet.above();
            if (world.getBlockState(feet).getCollisionShape(world, feet).isEmpty()
                    && world.getBlockState(head).getCollisionShape(world, head).isEmpty()) {
                best = d;
                break;
            }
        }
        if (best <= 0) return;

        double nx = startX + flat.x * best;
        double nz = startZ + flat.z * best;
        world.sendParticles(ParticleTypes.PORTAL, startX, startY + 1.0, startZ, 20, 0.3, 0.6, 0.3, 0.4);
        PlayerTeleportHelper.teleport(player, world, nx, startY, nz, player.getYRot(), player.getXRot());
        world.sendParticles(ParticleTypes.PORTAL, nx, startY + 1.0, nz, 20, 0.3, 0.6, 0.3, 0.4);
        world.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.6f, 1.1f);
        player.fallDistance = 0;
    }
}
