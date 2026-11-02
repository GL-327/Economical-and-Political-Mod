package com.political;


import com.political.bounty.SlayerItems;
import com.political.items.CustomItemHandler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ender Phase Helmet (head, id {@code ender_phase_helmet}).
 *
 * <p>While the helmet is worn and the player sneaks, they enter <b>Ender Phase Mode</b>:
 * they turn invisible, gain phasing speed, and cloak themselves in Darkness. In this state
 * {@link CustomItemHandler#isInEnderPhaseMode} reports true, which the Warden Chestplate
 * reads to stop stripping the (intentional) Darkness, and which blocks phased players from
 * attacking other players.</p>
 */
public final class EnderPhaseHandler {

    private EnderPhaseHandler() {}

    private static final String HELMET_ID = "ender_phase_helmet";
    private static final Set<UUID> phased = ConcurrentHashMap.newKeySet();

    public static boolean isActive(UUID uuid) {
        return phased.contains(uuid);
    }

    private static boolean isWearing(ServerPlayer player) {
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        return HELMET_ID.equals(SlayerItems.getCustomItemId(head));
    }

    /** Call once per player tick. */
    public static void tickPlayer(ServerPlayer player) {
        UUID uuid = player.getUUID();
        if (!isWearing(player)) {
            phased.remove(uuid);
            return;
        }
        ServerLevel world = (ServerLevel) player.level();

        boolean wantsPhase = player.isShiftKeyDown();
        if (wantsPhase) {
            boolean justEntered = phased.add(uuid);

            // Phasing kit: unseen, quick, wrapped in the End's dark.
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 30, 0, true, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 30, 1, true, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 30, 0, true, false, false));

            if (player.tickCount % 4 == 0) {
                world.sendParticles(ParticleTypes.PORTAL,
                        player.getX(), player.getY() + player.getBbHeight() * 0.5, player.getZ(),
                        6, 0.3, 0.6, 0.3, 0.05);
                world.sendParticles(ParticleTypes.REVERSE_PORTAL,
                        player.getX(), player.getY() + player.getBbHeight() * 0.5, player.getZ(),
                        3, 0.2, 0.4, 0.2, 0.02);
            }
            if (justEntered) {
                world.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.6f, 0.8f);
            }
        } else if (phased.remove(uuid)) {
            world.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.6f, 1.4f);
        }
    }

    public static void onDisconnect(UUID uuid) {
        phased.remove(uuid);
    }
}
