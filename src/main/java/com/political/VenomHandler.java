package com.political;


import com.political.core.Clock;
import com.political.bounty.SlayerItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Venomous Dagger (id {@code venomous_dagger}).
 *
 * <p>Each hit adds a venom stack to the target (up to {@link #MAX_STACKS}). Stacks decay if
 * the target isn't struck again within {@link #STACK_TTL_MS}. Poison amplifier scales with
 * the stack count, and at high stacks the venom turns necrotic (Wither) and deals a burst of
 * direct magic damage — rewarding sustained pressure with a single weak dagger.</p>
 */
public final class VenomHandler {

    private VenomHandler() {}

    public static final String DAGGER_ID = "venomous_dagger";
    private static final int MAX_STACKS = 10;
    private static final long STACK_TTL_MS = 6000L;

    private record Venom(int stacks, long lastHit) {}

    private static final Map<UUID, Venom> venom = new HashMap<>();

    public static boolean isVenomousDagger(ItemStack stack) {
        return DAGGER_ID.equals(SlayerItems.getCustomItemId(stack));
    }

    /** Call from the attack path when the weapon is the venomous dagger. */
    public static void onHit(ServerPlayer player, LivingEntity target) {
        if (!(player.level() instanceof ServerLevel world)) return;
        UUID id = target.getUUID();
        long now = Clock.gameMillis();

        Venom prev = venom.get(id);
        int stacks = (prev == null || now - prev.lastHit() > STACK_TTL_MS) ? 0 : prev.stacks();
        stacks = Math.min(MAX_STACKS, stacks + 1);
        venom.put(id, new Venom(stacks, now));

        // Poison amplifier scales with stacks: 1..2 early, up to 3 at max.
        int poisonAmp = Math.min(3, (stacks - 1) / 3);
        target.addEffect(new MobEffectInstance(MobEffects.POISON, 80 + stacks * 10, poisonAmp, false, true, true));

        // Necrotic venom at heavy stacks.
        if (stacks >= 6) {
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, stacks >= 9 ? 1 : 0, false, true, true));
        }
        // Full-stack rupture: direct burst that also clears the stack.
        if (stacks >= MAX_STACKS) {
            target.hurtServer(world, player.damageSources().magic(), 8.0f);
            venom.remove(id);
            world.playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 0.8f, 0.6f);
        }

        world.sendParticles(ParticleTypes.ITEM_SLIME,
                target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                4 + stacks, 0.3, 0.3, 0.3, 0.02);
        world.sendParticles(ParticleTypes.SNEEZE,
                target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(),
                stacks, 0.2, 0.2, 0.2, 0.01);
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§2☠ Venom §a" + Math.min(stacks, MAX_STACKS) + "§2/§a" + MAX_STACKS), true);
    }
}
