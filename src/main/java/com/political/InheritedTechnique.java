package com.political;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * V7 Pillar A — the curated set of eight signature clan cursed techniques.
 *
 * <p>Each clan owns exactly one. Any clan member who is also an enrolled sorcerer of the
 * required grade may channel it. Channelling is a paid, cooldown-gated active ability handled
 * by {@link ClanManager#channelSignature(ServerPlayer)}; this enum only carries the metadata
 * and the self-contained effect. Nothing here mints coins or heals for free.</p>
 */
public enum InheritedTechnique {

    LIMITLESS_BLUE("limitless_blue", "Limitless: Blue", "☯", Items.HEART_OF_THE_SEA,
            SorcererRank.GRADE_2, 40.0, 14_000L,
            "§7Warp space to a point and drag every",
            "§7cursed thing within reach toward it."),

    LIMITLESS_RED("limitless_red", "Limitless: Red", "☀", Items.FIRE_CHARGE,
            SorcererRank.GRADE_1, 55.0, 18_000L,
            "§7A divergent blast that hurls enemies",
            "§7back and tears through their guard."),

    TEN_SHADOWS("ten_shadows", "Ten Shadows", "☾", Items.WOLF_ARMOR,
            SorcererRank.GRADE_2, 45.0, 20_000L,
            "§7Call shadow beasts to hunt whatever",
            "§7you have marked as prey."),

    CURSED_SPEECH("cursed_speech", "Cursed Speech", "❝", Items.GOAT_HORN,
            SorcererRank.GRADE_3, 30.0, 12_000L,
            "§7Speak a cursed word. Nearby enemies",
            "§7are silenced, slowed and thrown."),

    STRAW_DOLL("straw_doll", "Straw Doll Resonance", "✚", Items.ARMOR_STAND,
            SorcererRank.GRADE_2, 45.0, 15_000L,
            "§7Bind a nail to your prey, then resonate",
            "§7it for heavy delayed damage."),

    DISASTER_FLAMES("disaster_flames", "Disaster Flames", "✹", Items.BLAZE_ROD,
            SorcererRank.GRADE_1, 50.0, 16_000L,
            "§7Loose a maximum flame arrow in a cone",
            "§7that scorches all it touches."),

    BOOGIE_WOOGIE("boogie_woogie", "Boogie Woogie", "✦", Items.FIREWORK_STAR,
            SorcererRank.GRADE_3, 25.0, 10_000L,
            "§7Clap to swap places with your prey,",
            "§7landing behind it with a strike."),

    SOUL_REND("soul_rend", "Soul Rend", "⚔", Items.NETHERITE_SWORD,
            SorcererRank.GRADE_1, 55.0, 17_000L,
            "§7A slashing arc that rends the soul",
            "§7of everything in front of you.");

    public final String id;
    public final String display;
    public final String icon;
    public final Item guiIcon;
    public final SorcererRank minRank;
    public final double energyCost;
    public final long cooldownMs;
    public final String[] lore;

    InheritedTechnique(String id, String display, String icon, Item guiIcon,
                       SorcererRank minRank, double energyCost, long cooldownMs, String... lore) {
        this.id = id;
        this.display = display;
        this.icon = icon;
        this.guiIcon = guiIcon;
        this.minRank = minRank;
        this.energyCost = energyCost;
        this.cooldownMs = cooldownMs;
        this.lore = lore;
    }

    public static InheritedTechnique byId(String id) {
        if (id == null) return null;
        for (InheritedTechnique t : values()) {
            if (t.id.equals(id)) return t;
        }
        return null;
    }

    public String coloured() {
        return "§d" + icon + " " + display;
    }

    // ════════════════════════════════════════════════════════════════════
    // EFFECT — self-contained, called only after cost + cooldown are paid
    // ════════════════════════════════════════════════════════════════════

    /** Run the technique's effect. Returns false only if there was nothing to act on. */
    public boolean execute(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel sl)) return false;
        BlockPos at = player.blockPosition();
        Vec3 look = player.getLookAngle();

        switch (this) {
            case LIMITLESS_BLUE -> {
                Vec3 focus = player.position().add(look.scale(6.0));
                List<LivingEntity> hit = enemiesAround(sl, player, player.position(), 12.0);
                for (LivingEntity e : hit) {
                    Vec3 pull = focus.subtract(e.position()).normalize().scale(1.2);
                    e.push(pull.x, 0.3, pull.z);
                    e.hurtServer(sl, player.damageSources().magic(), 6.0f);
                }
                sl.sendParticles(ParticleTypes.PORTAL, focus.x, focus.y + 0.5, focus.z,
                        60, 0.6, 0.8, 0.6, 0.6);
                sl.playSound(null, at, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 0.6f);
                return true;
            }
            case LIMITLESS_RED -> {
                List<LivingEntity> hit = enemiesAround(sl, player, player.position(), 9.0);
                for (LivingEntity e : hit) {
                    Vec3 push = e.position().subtract(player.position()).normalize().scale(1.8);
                    e.push(push.x, 0.6, push.z);
                    e.hurtServer(sl, player.damageSources().magic(), 14.0f);
                }
                sl.sendParticles(ParticleTypes.EXPLOSION, player.getX() + look.x * 3,
                        player.getY() + 1 + look.y * 3, player.getZ() + look.z * 3, 4, 0.3, 0.3, 0.3, 0.0);
                sl.playSound(null, at, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.1f, 1.1f);
                return true;
            }
            case TEN_SHADOWS -> {
                LivingEntity prey = nearestEnemy(sl, player, 24.0);
                int spawned = 0;
                for (int i = 0; i < 2; i++) {
                    Entity e = EntityType.WOLF.spawn(sl, at.above(), EntitySpawnReason.EVENT);
                    if (e instanceof Mob wolf) {
                        wolf.setPos(player.getX() + (i == 0 ? 1.5 : -1.5), player.getY(), player.getZ());
                        wolf.setCustomName(Component.literal("§8Shadow Beast"));
                        wolf.setCustomNameVisible(true);
                        wolf.addTag("political_clan_shadow");
                        wolf.addEffect(new MobEffectInstance(MobEffects.SPEED, 400, 1, true, false, false));
                        if (prey != null) wolf.setTarget(prey);
                        spawned++;
                    } else if (e != null) {
                        e.discard();
                    }
                }
                sl.sendParticles(ParticleTypes.SOUL, player.getX(), player.getY() + 0.5, player.getZ(),
                        30, 1.0, 0.5, 1.0, 0.05);
                sl.playSound(null, at, SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 1.0f, 0.6f);
                return spawned > 0;
            }
            case CURSED_SPEECH -> {
                List<LivingEntity> hit = enemiesAround(sl, player, player.position(), 11.0);
                for (LivingEntity e : hit) {
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 120, 2, true, false, true));
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1, true, false, true));
                    Vec3 push = e.position().subtract(player.position()).normalize().scale(1.0);
                    e.push(push.x, 0.4, push.z);
                    e.hurtServer(sl, player.damageSources().magic(), 8.0f);
                }
                sl.sendParticles(ParticleTypes.SONIC_BOOM, player.getX() + look.x * 2,
                        player.getY() + 1 + look.y * 2, player.getZ() + look.z * 2, 1, 0.0, 0.0, 0.0, 0.0);
                sl.playSound(null, at, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.0f, 1.3f);
                return true;
            }
            case STRAW_DOLL -> {
                LivingEntity prey = nearestEnemy(sl, player, 20.0);
                if (prey == null) return false;
                prey.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1, true, false, true));
                prey.hurtServer(sl, player.damageSources().magic(), 22.0f);
                sl.sendParticles(ParticleTypes.CRIT, prey.getX(), prey.getY() + prey.getBbHeight() * 0.6, prey.getZ(),
                        40, 0.4, 0.5, 0.4, 0.4);
                sl.playSound(null, prey.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0f, 0.7f);
                return true;
            }
            case DISASTER_FLAMES -> {
                List<LivingEntity> hit = coneInFront(sl, player, 12.0, 0.4);
                for (LivingEntity e : hit) {
                    e.setRemainingFireTicks(120);
                    e.hurtServer(sl, player.damageSources().magic(), 16.0f);
                }
                for (double d = 1.0; d <= 12.0; d += 1.0) {
                    sl.sendParticles(ParticleTypes.FLAME,
                            player.getX() + look.x * d, player.getY() + 1.0 + look.y * d, player.getZ() + look.z * d,
                            6, 0.3, 0.3, 0.3, 0.02);
                }
                sl.playSound(null, at, SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.1f, 0.8f);
                return true;
            }
            case BOOGIE_WOOGIE -> {
                LivingEntity prey = nearestEnemy(sl, player, 24.0);
                if (prey == null) return false;
                Vec3 mine = player.position();
                Vec3 theirs = prey.position();
                player.teleportTo(theirs.x, theirs.y, theirs.z);
                prey.teleportTo(mine.x, mine.y, mine.z);
                prey.hurtServer(sl, player.damageSources().magic(), 12.0f);
                sl.sendParticles(ParticleTypes.PORTAL, theirs.x, theirs.y + 1, theirs.z, 30, 0.5, 0.8, 0.5, 0.4);
                sl.sendParticles(ParticleTypes.PORTAL, mine.x, mine.y + 1, mine.z, 30, 0.5, 0.8, 0.5, 0.4);
                sl.playSound(null, at, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.4f);
                return true;
            }
            case SOUL_REND -> {
                List<LivingEntity> hit = coneInFront(sl, player, 6.0, 0.2);
                for (LivingEntity e : hit) {
                    e.hurtServer(sl, player.damageSources().magic(), 20.0f);
                }
                sl.sendParticles(ParticleTypes.SWEEP_ATTACK, player.getX() + look.x * 2,
                        player.getY() + 1 + look.y * 2, player.getZ() + look.z * 2, 8, 1.2, 0.5, 1.2, 0.0);
                sl.playSound(null, at, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.2f, 0.6f);
                return true;
            }
        }
        return false;
    }

    // ── target helpers ──

    private static List<LivingEntity> enemiesAround(ServerLevel sl, ServerPlayer player, Vec3 center, double radius) {
        AABB box = new AABB(center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius);
        List<LivingEntity> out = new ArrayList<>();
        for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, box,
                le -> le != player && le.isAlive() && (le instanceof Enemy))) {
            out.add(e);
        }
        return out;
    }

    private static LivingEntity nearestEnemy(ServerLevel sl, ServerPlayer player, double radius) {
        LivingEntity best = null;
        double bestSq = radius * radius;
        for (LivingEntity e : enemiesAround(sl, player, player.position(), radius)) {
            double d = e.distanceToSqr(player);
            if (d <= bestSq) { bestSq = d; best = e; }
        }
        return best;
    }

    /** Enemies in front of the player within range, filtered by how directly they face the look vector. */
    private static List<LivingEntity> coneInFront(ServerLevel sl, ServerPlayer player, double range, double minDot) {
        Vec3 look = player.getLookAngle().normalize();
        List<LivingEntity> out = new ArrayList<>();
        for (LivingEntity e : enemiesAround(sl, player, player.position(), range)) {
            Vec3 to = e.position().subtract(player.position()).normalize();
            if (to.dot(look) >= minDot) out.add(e);
        }
        return out;
    }
}
