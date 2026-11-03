package com.political.power.game;

import com.political.power.PowerSettings;
import com.political.power.cast.CastFacts;
import com.political.power.model.AbilityKind;
import com.political.power.model.AbilitySpec;
import com.political.power.model.TargetInfo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/**
 * The facts of one cast attempt, read from a real player. The target is looked up only when the pipeline asks for
 * it, and the entity it found is kept so the effect acts on exactly what was validated.
 */
final class GameFacts implements CastFacts {

    private final ServerPlayer player;
    private final String uuid;
    private final PowerRuntime runtime;
    private final long now;
    private LivingEntity resolvedEntity;

    GameFacts(ServerPlayer player, PowerRuntime runtime, long now) {
        this.player = player;
        this.uuid = player.getUUID().toString();
        this.runtime = runtime;
        this.now = now;
    }

    /** The living entity the validated target resolved to, or null when the ability needs none or targets the caster. */
    LivingEntity resolvedEntity() {
        return resolvedEntity;
    }

    @Override
    public boolean casterValid() {
        return player.isAlive() && !player.isSpectator() && !player.hasDisconnected();
    }

    @Override
    public boolean systemEnabled() {
        return PowerSettings.ENABLED.get();
    }

    @Override
    public AbilitySpec selected() {
        return PowerService.selected(uuid);
    }

    @Override
    public boolean unlocked(AbilitySpec ability) {
        return PowerService.unlocked(uuid, ability);
    }

    @Override
    public boolean suppressedByForeignDomain() {
        return DomainManager.suppresses(player, now);
    }

    @Override
    public long ticksSinceLastCast() {
        return runtime.ticksSinceLastCast(now);
    }

    @Override
    public int castsWithin(int windowTicks) {
        return runtime.castsWithin(now, windowTicks);
    }

    @Override
    public long cooldownRemainingTicks(AbilitySpec ability) {
        if (ability.kind() == AbilityKind.DOMAIN) {
            return DomainManager.cooldownRemaining(player.getUUID(), now);
        }
        return runtime.cooldownRemaining(ability.id(), now);
    }

    @Override
    public TargetInfo target(AbilitySpec ability) {
        TargetResolver.Resolved resolved = TargetResolver.resolve(player, ability);
        resolvedEntity = resolved.entity();
        return resolved.info();
    }

    @Override
    public boolean domainOverlap() {
        return DomainManager.wouldOverlap(player, now);
    }

    @Override
    public double resource() {
        return PowerService.resource(uuid);
    }

    @Override
    public double cooldownReduction() {
        return PowerService.cooldownReduction(uuid);
    }
}
