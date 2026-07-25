package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Runs Beastcraft.
 *
 * <p>Where Starbound is a standing bonus and the Runeforge is a set of independent procs, Beastcraft
 * is one meter. Landing a hit adds Instinct, the clade of the piece you are holding decides how much
 * Instinct it takes to fill, and filling it hands the fight to the beast for a moment. That gives the
 * line a rhythm the others do not have, and it means a Swarm piece and a Leviathan piece play very
 * differently even when the numbers are close.</p>
 */
public final class BeastcraftHandler {

    private BeastcraftHandler() {}

    private static final Map<UUID, Integer> INSTINCT = new HashMap<>();
    private static final Map<UUID, Long> LAST_BREAK = new HashMap<>();
    private static final Map<UUID, Integer> PREDATOR_STREAK = new HashMap<>();
    private static final long BREAK_CD_MS = 1200L;

    /** Grace period so a break cannot chain off a single swing. */
    private static boolean offCooldown(UUID id) {
        long now = System.currentTimeMillis();
        Long prev = LAST_BREAK.get(id);
        if (prev != null && now - prev < BREAK_CD_MS) return false;
        LAST_BREAK.put(id, now);
        return true;
    }

    private static Beast held(ServerPlayer p) {
        Beast b = BeastcraftGear.beastOf(p.getMainHandItem());
        if (b != null) return b;
        for (ItemStack st : GearLine.carried(p)) {
            Beast c = BeastcraftGear.beastOf(st);
            if (c != null) return c;
        }
        return null;
    }

    private static int cladeCount(ServerPlayer p, Beast.Clade clade) {
        int n = 0;
        for (ItemStack st : GearLine.carried(p)) {
            Beast b = BeastcraftGear.beastOf(st);
            if (b != null && b.clade == clade) n++;
        }
        return n;
    }

    /** Called for every hit a player lands. Returns bonus damage to fold into the swing. */
    public static float onHit(ServerPlayer player, LivingEntity target, float damage) {
        Beast beast = held(player);
        if (beast == null) return 0f;

        UUID id = player.getUUID();
        int cur = INSTINCT.getOrDefault(id, 0) + 1;

        // Predator clade ramps while a fight is running.
        float ramp = 0f;
        if (cladeCount(player, Beast.Clade.PREDATOR) >= 3) {
            int streak = Math.min(20, PREDATOR_STREAK.getOrDefault(id, 0) + 1);
            PREDATOR_STREAK.put(id, streak);
            ramp = damage * (streak * 0.015f);
        }

        int need = beast.clade.threshold;
        if (cladeCount(player, Beast.Clade.SWARM) >= 3) need = Math.max(3, need - 2);

        if (cur < need) {
            INSTINCT.put(id, cur);
            if (cur == need - 1) {
                player.sendSystemMessage(Component.literal("§8Instinct §7" + cur + "§8/§7" + need), true);
            }
            return ramp;
        }

        INSTINCT.put(id, 0);
        if (!offCooldown(id)) return ramp;

        float extra = breakInstinct(player, target, beast, damage);
        return ramp + extra;
    }

    /** Fires the beast. Returns whatever it adds to the triggering swing. */
    private static float breakInstinct(ServerPlayer player, LivingEntity target, Beast beast, float damage) {
        if (!(player.level() instanceof ServerLevel level)) return 0f;

        Vec3 at = target.position().add(0, 1, 0);
        Vec3 me = player.position().add(0, 1, 0);
        Vec3 look = player.getLookAngle();
        boolean leviathan = cladeCount(player, Beast.Clade.LEVIATHAN) >= 3;
        boolean spirit = cladeCount(player, Beast.Clade.SPIRIT) >= 3;
        boolean wyrm = cladeCount(player, Beast.Clade.WYRM) >= 3;
        float scale = leviathan ? 1.6f : 1.0f;
        float extra = 0f;

        player.sendSystemMessage(Component.literal(beast.colour + "" + ChatFormatting.BOLD
                + beast.display.toUpperCase(java.util.Locale.ROOT) + "!"), true);

        switch (beast.trait) {
            case PACK_RUSH -> {
                for (int i = 0; i < 3; i++) {
                    Vfx.lance(level, me.add(look.scale(-1)), at, Vfx.CURSED);
                }
                splash(level, player, at, 3.5, 22 * scale);
                extra = 15 * scale;
            }
            case AMBUSH -> {
                VfxPlus.executionFlash(level, at, Vfx.CURSED);
                extra = damage * 0.9f * scale;
            }
            case REND -> {
                VfxPlus.bladeSlash(level, me, look, Vfx.INFERNO);
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 120, 1));
                extra = 20 * scale;
            }
            case STALK -> {
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0));
                player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 100, 1));
                Vfx.puff(level, ParticleTypes.SMOKE, me, 40, 0.6, 0.05);
            }
            case FEED -> {
                VfxPlus.soulHarvest(level, at, me);
                float taken = 18 * scale;
                player.heal(taken * 0.5f);
                extra = taken;
            }
            case TRAMPLE -> {
                player.setDeltaMovement(look.scale(1.6).add(0, 0.25, 0));
                player.hurtMarked = true;
                VfxPlus.shockCone(level, me, look, Vfx.STORM);
                splash(level, player, me.add(look.scale(3)), 4.0, 26 * scale);
            }
            case GORE -> {
                Vfx.ring(level, ParticleTypes.CRIT, me, 2.5, 40);
                splash(level, player, me, 4.0, 20 * scale);
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 100, 1));
            }
            case PHASE -> {
                Vfx.voidRift(level, at);
                Vec3 behind = target.position().subtract(target.getLookAngle().scale(1.6));
                player.teleportTo(behind.x, behind.y, behind.z);
                extra = 25 * scale;
            }
            case HOWL -> {
                Vfx.domainCollapse(level, me, 7.0, Vfx.CURSED);
                level.playSound(null, player.blockPosition(), SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 1f, 0.6f);
                for (LivingEntity e : near(level, player, me, 7.0)) {
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 3));
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
                }
            }
            case SWARMCALL -> {
                VfxPlus.summoningCircle(level, at, Vfx.ARCANE);
                splash(level, player, at, 5.0, 14 * scale);
                for (LivingEntity e : near(level, player, at, 5.0)) {
                    e.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
                }
            }
            case MOLT -> {
                player.removeAllEffects();
                player.heal(10 * scale);
                VfxPlus.healingRain(level, me, 3.0);
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 120, 1));
            }
            case VENOM -> {
                Vfx.puff(level, ParticleTypes.SNEEZE, at, 60, 1.4, 0.02);
                for (LivingEntity e : near(level, player, at, 5.0)) {
                    e.addEffect(new MobEffectInstance(MobEffects.POISON, 160, 2));
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 0));
                }
            }
            case STING -> {
                for (LivingEntity e : near(level, player, me, 6.0)) {
                    Vfx.line(level, ParticleTypes.CRIT, me, e.position().add(0, 1, 0), 0.3);
                }
                splash(level, player, me, 6.0, 12 * scale);
            }
            case EMBER -> {
                VfxPlus.flamePillar(level, at);
                for (LivingEntity e : near(level, player, at, 4.0)) {
                    e.igniteForSeconds(wyrm ? 8 : 5);
                }
                splash(level, player, at, 4.0, 18 * scale);
            }
            case CONSTRICT -> {
                Vfx.shell(level, ParticleTypes.CRIT, at, 1.8, 50);
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 140, 5));
                target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 140, 2));
                extra = 22 * scale;
            }
            case ROOT -> {
                VfxPlus.fissure(level, at, look);
                for (LivingEntity e : near(level, player, at, 5.0)) {
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 120, 6));
                    e.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 120, 128));
                }
                splash(level, player, at, 5.0, 16 * scale);
            }
            case TIDE -> {
                Vfx.ring(level, ParticleTypes.BUBBLE, at, 4.0, 60);
                for (LivingEntity e : near(level, player, at, 6.0)) {
                    Vec3 push = e.position().subtract(me).normalize().scale(1.1);
                    e.setDeltaMovement(push.x, 0.5, push.z);
                    e.hurtMarked = true;
                }
                splash(level, player, at, 6.0, 20 * scale);
            }
            case CARAPACE -> {
                VfxPlus.shieldDome(level, me, 3.0, Vfx.FROST);
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 160, 2));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 2));
            }
            case DROWN -> {
                VfxPlus.singularity(level, at, Vfx.VOID);
                for (LivingEntity e : near(level, player, at, 7.0)) {
                    Vec3 pull = at.subtract(e.position()).normalize().scale(0.9);
                    e.setDeltaMovement(pull);
                    e.hurtMarked = true;
                }
                splash(level, player, at, 7.0, 24 * scale);
            }
            case QUAKE -> {
                VfxPlus.fissure(level, me, look);
                Vfx.ring(level, ParticleTypes.EXPLOSION, me, 5.0, 40);
                splash(level, player, me, 8.0, 26 * scale);
            }
            case PETRIFY -> {
                VfxPlus.frostNova(level, at);
                for (LivingEntity e : near(level, player, at, 6.0)) {
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 160, 7));
                    e.setTicksFrozen(300);
                }
            }
            case GALE -> {
                List<Vec3> chain = new ArrayList<>();
                for (LivingEntity e : near(level, player, at, 8.0)) chain.add(e.position().add(0, 1, 0));
                VfxPlus.chainLightning(level, me, chain);
                splash(level, player, at, 8.0, 20 * scale);
            }
            case CRUSH -> {
                VfxPlus.orbitalStrike(level, at, Vfx.INFERNO);
                extra = damage * 0.75f * scale + 30;
            }
            case SCREECH -> {
                Vfx.domainCollapse(level, at, 6.0, Vfx.ARCANE);
                for (LivingEntity e : near(level, player, at, 6.0)) {
                    e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
                    e.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 0));
                }
                splash(level, player, at, 6.0, 14 * scale);
            }
        }

        // Spirit clade mends on every break.
        if (spirit) {
            player.heal(6f);
            Vfx.bloom(level, me);
        }
        // Wyrm clade takes a cut of the moment in coin.
        if (wyrm) CoinManager.giveCoinsQuiet(player, 25);

        return extra;
    }

    private static List<LivingEntity> near(ServerLevel level, ServerPlayer player, Vec3 centre, double r) {
        List<LivingEntity> out = new ArrayList<>();
        for (Entity e : level.getEntities(player, new AABB(centre, centre).inflate(r))) {
            if (e instanceof LivingEntity le && le.isAlive() && !le.getUUID().equals(player.getUUID())) {
                out.add(le);
            }
        }
        return out;
    }

    private static void splash(ServerLevel level, ServerPlayer player, Vec3 centre, double r, float amount) {
        for (LivingEntity e : near(level, player, centre, r)) {
            e.hurtServer(level, level.damageSources().playerAttack(player), amount);
        }
    }

    /** Instinct bleeds off when you stop fighting, so it cannot be banked between sessions. */
    public static void tickPlayer(ServerPlayer player) {
        if (player.tickCount % 200 != 0) return;
        UUID id = player.getUUID();
        Integer cur = INSTINCT.get(id);
        if (cur != null && cur > 0) INSTINCT.put(id, Math.max(0, cur - 2));
        PREDATOR_STREAK.remove(id);
    }

    public static void clear(UUID id) {
        INSTINCT.remove(id);
        LAST_BREAK.remove(id);
        PREDATOR_STREAK.remove(id);
    }
}
