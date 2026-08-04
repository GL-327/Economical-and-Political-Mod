package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The Nanite Slayer: a silverfish the size of a player, made of smaller ones.
 *
 * <p>Every other bounty boss is a bigger version of a thing you already know. This one is a
 * different shape of fight. It is a conglomerate, so hitting it takes pieces off: each shed throws
 * a nanite that fights on its own and leaves the core a little smaller. The fight gets faster and
 * more crowded at once, and the player has to decide between chasing the pieces and staying on the
 * core.</p>
 *
 * <p>Scale is a real attribute, so this needs no model and no client mod. A silverfish is about a
 * quarter of a block tall, so a scale near five puts it eye to eye with the player, which is the
 * whole joke.</p>
 */
public final class NaniteSlayer {

    private NaniteSlayer() {}

    public static final String CORE_TAG = "political_nanite_core";
    public static final String SHARD_TAG = "political_nanite_shard";

    /** Where a core started, so shrinking can be measured against it rather than guessed. */
    private static final Map<UUID, Double> START_SCALE = new HashMap<>();

    /** Player height over silverfish height, near enough. */
    private static final double CORE_SCALE = 5.0;
    private static final double MIN_SCALE = 1.6;

    // ── Making one ────────────────────────────────────────────────────────

    /**
     * Turns a freshly spawned silverfish into the core.
     *
     * <p>Called after the ordinary boss setup, so it only has to change what makes this one
     * different: how big it is, and the fact that it is a core rather than a shard.</p>
     */
    public static void makeCore(ServerLevel world, Mob boss, int tier) {
        boss.addTag(CORE_TAG);
        START_SCALE.put(boss.getUUID(), CORE_SCALE);
        setScale(boss, CORE_SCALE);

        boss.setCustomName(Component.literal("The Nanite Slayer")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        boss.setCustomNameVisible(true);
        boss.setPersistenceRequired();

        try {
            // A silverfish has almost no reach at its own size. Scaled up it needs the reach to
            // match, or a player can stand next to it and never be touched.
            var reach = boss.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
            if (reach != null) reach.setBaseValue(4.0);
            var kb = boss.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
            if (kb != null) kb.setBaseValue(0.8);
        } catch (Exception ignored) {
            // Attributes that will not move leave a slightly worse fight, not a broken one.
        }
    }

    private static void setScale(LivingEntity e, double scale) {
        try {
            var attr = e.getAttribute(Attributes.SCALE);
            if (attr != null) attr.setBaseValue(scale);
        } catch (Exception ignored) {
            // Older mappings without a scale attribute. The fight still works, it is just small.
        }
    }

    private static double scaleOf(LivingEntity e) {
        try {
            var attr = e.getAttribute(Attributes.SCALE);
            return attr == null ? 1.0 : attr.getBaseValue();
        } catch (Exception ignored) {
            return 1.0;
        }
    }

    // ── Coming apart ──────────────────────────────────────────────────────

    /**
     * Sheds pieces of the core.
     *
     * <p>Each shard takes a share of the core's remaining health with it rather than being free
     * extra health, so shedding is the boss trading size for numbers and not simply gaining a
     * second health bar. The core shrinks by the same share.</p>
     */
    public static void shed(ServerLevel world, LivingEntity core, int tier, int count) {
        if (!core.entityTags().contains(CORE_TAG)) return;

        double scale = scaleOf(core);
        if (scale <= MIN_SCALE) return;

        double newScale = Math.max(MIN_SCALE, scale - 0.45);
        setScale(core, newScale);

        // The health that leaves the core is what the shards are made of.
        float given = core.getHealth() * 0.06f * count;
        if (core.getHealth() - given > 1f) {
            core.setHealth(core.getHealth() - given);
        }

        for (int i = 0; i < count; i++) {
            try {
                var made = EntityType.SILVERFISH.create(world, EntitySpawnReason.MOB_SUMMONED);
                if (!(made instanceof Mob shard)) continue;

                double angle = (Math.PI * 2 / Math.max(1, count)) * i;
                shard.setPos(core.getX() + Math.cos(angle) * 2.0, core.getY() + 0.5,
                        core.getZ() + Math.sin(angle) * 2.0);
                shard.addTag(SHARD_TAG);
                shard.setPersistenceRequired();
                shard.setCustomName(Component.literal("Nanite")
                        .withStyle(ChatFormatting.LIGHT_PURPLE));

                // Visibly a piece of the thing it came off, which is the read the fight depends on.
                setScale(shard, Math.max(0.8, newScale * 0.35));

                var hp = shard.getAttribute(Attributes.MAX_HEALTH);
                if (hp != null) {
                    hp.setBaseValue(Math.max(8.0, given / Math.max(1, count)));
                    shard.setHealth(shard.getMaxHealth());
                }
                var dmg = shard.getAttribute(Attributes.ATTACK_DAMAGE);
                if (dmg != null) dmg.setBaseValue(3.0 + tier * 0.8);

                world.addFreshEntity(shard);
            } catch (Exception ignored) {
                // One fewer shard is not worth failing the shed over.
            }
        }

        world.playSound(null, core.blockPosition(), SoundEvents.SILVERFISH_HURT,
                SoundSource.HOSTILE, 1.4f, 0.6f);
        for (ServerPlayer p : world.players()) {
            if (p.distanceToSqr(core) > 40 * 40) continue;
            p.sendSystemMessage(Component.literal("§d✦ §7It sheds "
                    + count + " nanites. The core is smaller."), true);
        }
    }

    /**
     * Sheds on damage as well as on the ability timer.
     *
     * <p>Tied to health crossing a tenth rather than to a clock, so a player who does a lot of
     * damage quickly sees it come apart quickly. A boss whose signature move is on a timer is a
     * boss whose signature move happens to you rather than because of you.</p>
     */
    public static void onCoreHurt(ServerLevel world, LivingEntity core, int tier) {
        if (!core.entityTags().contains(CORE_TAG)) return;
        Double start = START_SCALE.get(core.getUUID());
        if (start == null) return;

        float frac = core.getHealth() / Math.max(1f, core.getMaxHealth());
        // Ten bands. Each one crossed sheds once, and the map remembers which have gone.
        int band = (int) Math.floor(frac * 10);
        Integer lastBand = LAST_BAND.get(core.getUUID());
        if (lastBand != null && band >= lastBand) return;
        LAST_BAND.put(core.getUUID(), band);
        shed(world, core, tier, 1 + tier / 4);
    }

    private static final Map<UUID, Integer> LAST_BAND = new HashMap<>();

    /** Clears everything a finished core left behind. */
    public static void onCoreDeath(ServerLevel world, LivingEntity core) {
        START_SCALE.remove(core.getUUID());
        LAST_BAND.remove(core.getUUID());
        // The shards do not outlive the thing they came off.
        for (Mob m : world.getEntitiesOfClass(Mob.class,
                new AABB(core.blockPosition()).inflate(48),
                e -> e.entityTags().contains(SHARD_TAG))) {
            m.discard();
        }
    }

    public static boolean isCore(LivingEntity e) {
        return e.entityTags().contains(CORE_TAG);
    }
}
