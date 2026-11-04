package com.political.power.game;

import com.political.core.text.Text;
import com.political.power.PowerSettings;
import com.political.power.formula.DomainRules;
import com.political.power.formula.DomainState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Domains that exist right now (C9). A Domain is a sphere around its caster; the caster cannot move it. It
 * ends when its time runs out, or early when the caster dies, logs out or changes dimension. Domains are not
 * saved: a restart ends them.
 */
final class DomainManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DomainManager.class);

    private record Domain(UUID caster, ResourceKey<Level> dimension, double x, double y, double z, DomainState state) {
    }

    /** Domains that are running. */
    private static final Map<UUID, Domain> ACTIVE = new HashMap<>();
    /** Domains that ended but whose cooldown still runs. */
    private static final Map<UUID, DomainState> COOLDOWNS = new HashMap<>();

    private DomainManager() {
    }

    static void start(ServerPlayer caster, long now) {
        DomainState state = DomainState.cast(now);
        ACTIVE.put(caster.getUUID(), new Domain(caster.getUUID(), caster.level().dimension(),
                caster.getX(), caster.getY(), caster.getZ(), state));
        COOLDOWNS.put(caster.getUUID(), state);
        ServerLevel world = caster.level();
        int fx = PowerVfx.variant(world);
        world.playSound(null, caster.blockPosition(), SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 0.8f, PowerVfx.pick(fx, 0.6f, 0.7f, 0.85f));
        PowerVfx.ring(world, ParticleTypes.SOUL_FIRE_FLAME, caster.getX(), caster.getY(), caster.getZ(),
                PowerSettings.DOMAIN_RADIUS.get(), PowerVfx.pick(fx, 32, 48, 64));
        // a title for the caster and for everyone inside the sphere
        Component title = Text.tr("political.power.domain.started");
        Component subtitle = Text.tr("political.power.domain.started.sub", Text.plain(caster.getName().getString()));
        for (ServerPlayer viewer : world.players()) {
            if (viewer == caster || contains(ACTIVE.get(caster.getUUID()), viewer)) {
                PowerVfx.title(viewer, title, subtitle);
            }
        }
    }

    /** Takes back a Domain that was started in a cast that then failed. */
    static void cancel(UUID caster) {
        ACTIVE.remove(caster);
        COOLDOWNS.remove(caster);
    }

    static boolean isActive(UUID caster, long now) {
        Domain d = ACTIVE.get(caster);
        return d != null && d.state().isActive(now);
    }

    static long cooldownRemaining(UUID caster, long now) {
        DomainState s = COOLDOWNS.get(caster);
        return s == null ? 0L : s.cooldownRemaining(now);
    }

    /** Ends a caster's Domain now and starts its cooldown from this moment. */
    static void endEarly(UUID caster, long now) {
        Domain d = ACTIVE.remove(caster);
        if (d != null) {
            d.state().endEarly(now);
        }
    }

    /** Whether the entity stands inside the Domain of the given caster. */
    static boolean inDomainOf(UUID caster, Entity entity, long now) {
        Domain d = ACTIVE.get(caster);
        return d != null && d.state().isActive(now) && contains(d, entity);
    }

    private static boolean contains(Domain d, Entity entity) {
        return entity.level().dimension().equals(d.dimension())
                && DomainRules.contains(d.x(), d.y(), d.z(), entity.getX(), entity.getY(), entity.getZ());
    }

    /** The player stands inside a Domain that is not their own. */
    static boolean suppresses(ServerPlayer player, long now) {
        for (Domain d : ACTIVE.values()) {
            if (!d.caster().equals(player.getUUID()) && d.state().isActive(now) && contains(d, player)) {
                return true;
            }
        }
        return false;
    }

    /** A new Domain cast by this player would be too close to a running one in the same dimension. */
    static boolean wouldOverlap(ServerPlayer caster, long now) {
        for (Domain d : ACTIVE.values()) {
            if (d.state().isActive(now) && DomainRules.tooClose(caster.level().dimension().equals(d.dimension()),
                    caster.getX(), caster.getY(), caster.getZ(), d.x(), d.y(), d.z())) {
                return true;
            }
        }
        return false;
    }

    /** The resource of a caster does not come back while their own Domain runs. */
    static boolean pausesRegen(UUID caster, long now) {
        return isActive(caster, now);
    }

    /** Domains that currently exist, for the info screen. */
    static int activeCount(long now) {
        int n = 0;
        for (Domain d : ACTIVE.values()) {
            if (d.state().isActive(now)) {
                n++;
            }
        }
        return n;
    }

    /** Ends Domains whose caster died, left, changed dimension or ran out of time; draws the edge. */
    static void tick(MinecraftServer server, long now) {
        for (Domain d : new ArrayList<>(ACTIVE.values())) {
            ServerPlayer caster = server.getPlayerList().getPlayer(d.caster());
            boolean gone = caster == null || !caster.isAlive() || !caster.level().dimension().equals(d.dimension());
            if (gone) {
                end(d, now, caster, true);
            } else if (!d.state().isActive(now)) {
                end(d, now, caster, false);
            } else {
                PowerVfx.ring(caster.level(), ParticleTypes.SOUL_FIRE_FLAME, d.x(), d.y(), d.z(), PowerSettings.DOMAIN_RADIUS.get(), 24);
            }
        }
        COOLDOWNS.entrySet().removeIf(e -> !ACTIVE.containsKey(e.getKey()) && e.getValue().cooldownRemaining(now) <= 0);
    }

    private static void end(Domain d, long now, ServerPlayer caster, boolean early) {
        ACTIVE.remove(d.caster());
        if (early) {
            d.state().endEarly(now);
        }
        if (caster != null) {
            PowerVfx.title(caster, Text.tr("political.power.domain.ended.title"), Text.tr("political.power.domain.ended"));
            caster.level().playSound(null, caster.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.8f, 0.8f);
        }
        LOGGER.debug("Domain of {} ended{}", d.caster(), early ? " early" : "");
    }

    /** For tests and shutdown: forget every Domain. */
    static void clear() {
        ACTIVE.clear();
        COOLDOWNS.clear();
    }
}
