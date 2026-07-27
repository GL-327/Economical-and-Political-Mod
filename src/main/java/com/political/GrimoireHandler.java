package com.political;

import net.minecraft.ChatFormatting;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Runs the Grimoire.
 *
 * <p>Nothing here happens on its own. Sneak and use a sigil and it casts, spending Focus. Focus caps
 * and refills from Intelligence, which is the point of the line, and the Circle you are wearing bends
 * the whole pool: Ward turns Focus into armour, Auspex refills it much faster, Hollow feeds you on
 * every cast.</p>
 */
public final class GrimoireHandler {

    private GrimoireHandler() {}

    private static final Map<UUID, Integer> FOCUS = new HashMap<>();
    private static final Map<UUID, Long> LAST_CAST = new HashMap<>();
    private static final Map<UUID, Sigil.Casting> LAST_SPELL = new HashMap<>();
    private static final Map<UUID, Vec3> TETHER = new HashMap<>();
    private static final Map<UUID, Float> STORED = new HashMap<>();
    private static final long CAST_CD_MS = 900L;

    public static int maxFocus(ServerPlayer p) {
        double intel = PlayerStatsManager.getStats(p).get(PlayerStatsManager.Stat.INTELLIGENCE);
        int base = 100 + (int) (intel / 2);
        if (circleCount(p, Sigil.Circle.AUSPEX) >= 3) base += 100;
        return base;
    }

    public static int focus(ServerPlayer p) {
        return Math.min(maxFocus(p), FOCUS.getOrDefault(p.getUUID(), 0));
    }

    private static int circleCount(ServerPlayer p, Sigil.Circle c) {
        int n = 0;
        for (ItemStack st : GearLine.carried(p)) {
            Sigil s = GrimoireGear.sigilOf(st);
            if (s != null && s.circle == c) n++;
        }
        return n;
    }

    private static boolean carriesAny(ServerPlayer p) {
        for (ItemStack st : GearLine.carried(p)) {
            if (GrimoireGear.sigilOf(st) != null) return true;
        }
        return false;
    }

    /** Ticked every tick, refills once a second. */
    public static void tickPlayer(ServerPlayer player) {
        if (player.tickCount % 20 != 0) return;
        UUID id = player.getUUID();
        if (!carriesAny(player)) {
            FOCUS.remove(id);
            return;
        }
        int max = maxFocus(player);
        int regen = 4 + (int) (PlayerStatsManager.getStats(player)
                .get(PlayerStatsManager.Stat.INTELLIGENCE) / 40);
        if (circleCount(player, Sigil.Circle.AUSPEX) >= 3) regen *= 2;
        FOCUS.put(id, Math.min(max, FOCUS.getOrDefault(id, 0) + regen));
    }

    /** Ward turns Focus into a damage sponge. Returns the damage left after Focus eats its share. */
    public static float absorb(ServerPlayer player, float damage) {
        if (circleCount(player, Sigil.Circle.WARD) < 3) return damage;
        UUID id = player.getUUID();
        int have = FOCUS.getOrDefault(id, 0);
        if (have <= 0) return damage;
        int eat = (int) Math.min(have, damage * 2);
        FOCUS.put(id, have - eat);
        return Math.max(0f, damage - eat / 2f);
    }

    /** Hollow siphons Focus back out of the damage you deal. */
    public static void onHit(ServerPlayer player, LivingEntity target, float damage) {
        if (!carriesAny(player)) return;
        boolean siphon = false;
        for (ItemStack st : GearLine.carried(player)) {
            Sigil s = GrimoireGear.sigilOf(st);
            if (s != null && s.casting == Sigil.Casting.SIPHON) siphon = true;
        }
        if (!siphon && circleCount(player, Sigil.Circle.HOLLOW) < 3) return;
        UUID id = player.getUUID();
        FOCUS.put(id, Math.min(maxFocus(player), FOCUS.getOrDefault(id, 0) + (int) (damage / 4)));
    }

    /** Called on use. Returns true when a cast happened and the use should be swallowed. */
    public static boolean tryCast(ServerPlayer player, ItemStack held) {
        if (!player.isShiftKeyDown()) return false;
        Sigil sigil = GrimoireGear.sigilOf(held);
        if (sigil == null) return false;
        if (!(player.level() instanceof ServerLevel level)) return false;

        UUID id = player.getUUID();
        long now = System.currentTimeMillis();
        Long prev = LAST_CAST.get(id);
        if (prev != null && now - prev < CAST_CD_MS) return true;

        int cost = sigil.cost;
        if (circleCount(player, Sigil.Circle.KINDLING) >= 3) cost = Math.max(5, (int) (cost * 0.7));

        int have = FOCUS.getOrDefault(id, 0);
        if (have < cost) {
            player.sendSystemMessage(Component.literal("§9✎ Focus §7" + have + "§8/§7"
                    + maxFocus(player) + " §8(needs " + cost + ")"), true);
            level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_BASS.value(),
                    SoundSource.PLAYERS, 0.6f, 0.6f);
            return true;
        }

        FOCUS.put(id, have - cost);
        LAST_CAST.put(id, now);
        cast(player, level, sigil, sigil.casting);
        LAST_SPELL.put(id, sigil.casting);

        // Hollow takes a bite out of the world on every cast.
        if (circleCount(player, Sigil.Circle.HOLLOW) >= 3) {
            player.heal(4f);
        }
        return true;
    }

    private static void cast(ServerPlayer player, ServerLevel level, Sigil sigil, Sigil.Casting what) {
        Vec3 me = player.position().add(0, 1, 0);
        Vec3 look = player.getLookAngle();
        Vec3 aim = me.add(look.scale(12));
        double reach = circleCount(player, Sigil.Circle.AUSPEX) >= 3 ? 1.5 : 1.0;
        boolean shareOut = circleCount(player, Sigil.Circle.FLUX) >= 3;
        boolean kindled = circleCount(player, Sigil.Circle.KINDLING) >= 3;
        float power = kindled ? 1.5f : 1.0f;

        player.sendSystemMessage(Component.literal(sigil.colour + "" + ChatFormatting.BOLD
                + sigil.display.toUpperCase(Locale.ROOT)), true);

        switch (what) {
            case FIREBOLT -> {
                Vfx.lance(level, me, aim, Vfx.INFERNO);
                LivingEntity hit = firstIn(level, player, me, look, 12 * reach);
                if (hit != null) {
                    hit.igniteForSeconds(6);
                    hit.hurtServer(level, level.damageSources().playerAttack(player), 30 * power);
                }
            }
            case PILLAR -> {
                Vec3 at = groundAim(level, player, 16 * reach);
                VfxPlus.flamePillar(level, at);
                for (LivingEntity e : near(level, player, at, 3.5)) {
                    e.igniteForSeconds(8);
                    e.hurtServer(level, level.damageSources().playerAttack(player), 34 * power);
                }
            }
            case TRAIL -> {
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 200, 2));
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0));
                Vfx.ring(level, ParticleTypes.FLAME, me, 1.5, 20);
            }
            case CONE -> {
                VfxPlus.shockCone(level, me, look, Vfx.INFERNO);
                for (LivingEntity e : cone(level, player, me, look, 8 * reach, 0.5)) {
                    e.igniteForSeconds(6);
                    e.hurtServer(level, level.damageSources().playerAttack(player), 26 * power);
                }
            }
            case BRAND -> {
                LivingEntity hit = firstIn(level, player, me, look, 16 * reach);
                if (hit != null) {
                    VfxPlus.executionFlash(level, hit.position().add(0, 1, 0), Vfx.INFERNO);
                    hit.addEffect(new MobEffectInstance(MobEffects.GLOWING, 300, 0));
                    hit.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 300, 1));
                    hit.addEffect(new MobEffectInstance(MobEffects.UNLUCK, 300, 4));
                }
            }
            case MEND -> {
                ItemStack tool = player.getMainHandItem();
                if (tool.isDamaged()) tool.setDamageValue(Math.max(0, tool.getDamageValue() - 200));
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 300, 1));
                Vfx.bloom(level, me);
            }
            case RAIN -> {
                Vec3 at = groundAim(level, player, 16 * reach);
                VfxPlus.cometVolley(level, at, 8);
                for (LivingEntity e : near(level, player, at, 6.0)) {
                    e.igniteForSeconds(4);
                    e.hurtServer(level, level.damageSources().playerAttack(player), 20 * power);
                }
            }
            case WARD -> {
                VfxPlus.shieldDome(level, me, 4.0, Vfx.INFERNO);
                for (LivingEntity e : near(level, player, me, 4.5)) {
                    e.igniteForSeconds(4);
                    Vec3 push = e.position().subtract(me).normalize().scale(0.8);
                    e.setDeltaMovement(push.x, 0.4, push.z);
                    e.hurtMarked = true;
                }
            }
            case METEOR -> {
                Vec3 at = groundAim(level, player, 20 * reach);
                Vfx.meteor(level, at);
                for (LivingEntity e : near(level, player, at, 7.0)) {
                    e.igniteForSeconds(10);
                    e.hurtServer(level, level.damageSources().playerAttack(player), 70 * power);
                }
            }

            case BARRIER -> {
                VfxPlus.shieldDome(level, me, 4.0, Vfx.FROST);
                grant(player, shareOut, level, MobEffects.RESISTANCE, 300, 2);
                grant(player, shareOut, level, MobEffects.ABSORPTION, 400, 3);
            }
            case REFLECT -> {
                Vfx.shell(level, ParticleTypes.CRIT, me, 2.0, 40);
                grant(player, shareOut, level, MobEffects.RESISTANCE, 240, 1);
                for (LivingEntity e : near(level, player, me, 5.0)) {
                    e.hurtServer(level, level.damageSources().thorns(player), 18 * power);
                }
            }
            case FREEZE -> {
                VfxPlus.frostNova(level, me);
                for (LivingEntity e : near(level, player, me, 7.0 * reach)) {
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 160, 7));
                    e.setTicksFrozen(300);
                }
            }
            case SANCTUARY -> {
                VfxPlus.summoningCircle(level, me, Vfx.ARCANE);
                grant(player, shareOut, level, MobEffects.RESISTANCE, 400, 2);
                grant(player, shareOut, level, MobEffects.REGENERATION, 400, 1);
            }
            case SILENCE -> {
                Vfx.domainCollapse(level, me, 6.0, Vfx.VOID);
                for (LivingEntity e : near(level, player, me, 6.0 * reach)) {
                    e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0));
                    e.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0));
                    e.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 200, 3));
                }
            }
            case PLATE -> {
                VfxPlus.shieldDome(level, me, 2.5, Vfx.STORM);
                grant(player, shareOut, level, MobEffects.RESISTANCE, 600, 3);
                grant(player, shareOut, level, MobEffects.SLOWNESS, 600, 0);
            }
            case PUSH -> {
                Vfx.ring(level, ParticleTypes.CLOUD, me, 5.0, 60);
                for (LivingEntity e : near(level, player, me, 8.0 * reach)) {
                    Vec3 push = e.position().subtract(me).normalize().scale(1.4);
                    e.setDeltaMovement(push.x, 0.6, push.z);
                    e.hurtMarked = true;
                    e.hurtServer(level, level.damageSources().playerAttack(player), 12 * power);
                }
            }
            case REVEAL -> {
                Vfx.shell(level, ParticleTypes.END_ROD, me, 12.0, 90);
                for (LivingEntity e : near(level, player, me, 24.0)) {
                    e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 600, 0));
                }
            }
            case SECOND_WIND -> {
                VfxPlus.healingRain(level, me, 4.0);
                player.setHealth(player.getMaxHealth());
                grant(player, shareOut, level, MobEffects.ABSORPTION, 600, 4);
                grant(player, shareOut, level, MobEffects.REGENERATION, 300, 2);
                FOCUS.put(player.getUUID(), 0);
            }

            case DRAIN -> {
                for (LivingEntity e : near(level, player, me, 6.0 * reach)) {
                    VfxPlus.soulHarvest(level, e.position().add(0, 1, 0), me);
                    e.hurtServer(level, level.damageSources().magic(), 18 * power);
                    player.heal(3f);
                }
            }
            case HARVEST -> {
                Vfx.domainCollapse(level, me, 9.0, Vfx.CURSED);
                for (LivingEntity e : near(level, player, me, 9.0 * reach)) {
                    e.hurtServer(level, level.damageSources().magic(), 26 * power);
                    player.heal(2f);
                }
            }
            case WITHER -> {
                LivingEntity hit = firstIn(level, player, me, look, 14 * reach);
                if (hit != null) {
                    Vfx.puff(level, ParticleTypes.SOUL, hit.position().add(0, 1, 0), 40, 0.8, 0.05);
                    hit.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 2));
                    hit.addEffect(new MobEffectInstance(MobEffects.INSTANT_DAMAGE, 1, 1));
                }
            }
            case PHYLACTERY -> {
                Vfx.shell(level, ParticleTypes.SOUL_FIRE_FLAME, me, 2.0, 40);
                STORED.put(player.getUUID(), 0f);
                grant(player, shareOut, level, MobEffects.RESISTANCE, 400, 1);
                player.sendSystemMessage(Component.literal("§5The knot is tied. What hurts you is kept."), false);
            }
            case SWARM -> {
                Vec3 at = groundAim(level, player, 14 * reach);
                VfxPlus.summoningCircle(level, at, Vfx.CURSED);
                for (LivingEntity e : near(level, player, at, 5.0)) {
                    e.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 2));
                    e.hurtServer(level, level.damageSources().magic(), 16 * power);
                }
            }
            case SLOW -> {
                Vfx.ring(level, ParticleTypes.SOUL, me, 7.0, 60);
                for (LivingEntity e : near(level, player, me, 10.0 * reach)) {
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 300, 3));
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 300, 1));
                }
            }
            case SIPHON -> {
                Vfx.bloom(level, me);
                grant(player, shareOut, level, MobEffects.STRENGTH, 400, 1);
                player.sendSystemMessage(Component.literal("§5The cup is open. Damage becomes Focus."), false);
            }
            case SUMMON -> {
                Vec3 at = groundAim(level, player, 12 * reach);
                VfxPlus.summoningCircle(level, at, Vfx.VOID);
                for (LivingEntity e : near(level, player, at, 5.0)) {
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 6));
                    e.hurtServer(level, level.damageSources().magic(), 22 * power);
                }
            }
            case EXECUTE -> {
                LivingEntity hit = firstIn(level, player, me, look, 14 * reach);
                if (hit != null) {
                    VfxPlus.executionFlash(level, hit.position().add(0, 1, 0), Vfx.CURSED);
                    float missing = hit.getMaxHealth() - hit.getHealth();
                    hit.hurtServer(level, level.damageSources().magic(), (25 + missing * 0.6f) * power);
                }
            }

            case BLINK -> {
                Vfx.voidRift(level, me);
                Vec3 to = me.add(look.scale(10 * reach));
                player.teleportTo(to.x, to.y, to.z);
                Vfx.voidRift(level, to);
            }
            case FORESIGHT -> {
                Vfx.shell(level, ParticleTypes.ENCHANT, me, 2.0, 40);
                grant(player, shareOut, level, MobEffects.STRENGTH, 200, 2);
                grant(player, shareOut, level, MobEffects.SPEED, 200, 1);
            }
            case TETHER -> {
                Vec3 anchor = TETHER.get(player.getUUID());
                if (anchor == null) {
                    TETHER.put(player.getUUID(), me);
                    Vfx.ring(level, ParticleTypes.ENCHANT, me, 2.0, 30);
                    player.sendSystemMessage(Component.literal("§bThread laid. Cast again to return."), false);
                } else {
                    VfxPlus.singularity(level, me, Vfx.ARCANE);
                    player.teleportTo(anchor.x, anchor.y, anchor.z);
                    TETHER.remove(player.getUUID());
                }
            }
            case PATHFIND -> {
                grant(player, shareOut, level, MobEffects.SPEED, 600, 2);
                grant(player, shareOut, level, MobEffects.NIGHT_VISION, 600, 0);
                Vfx.line(level, ParticleTypes.END_ROD, me, me.add(look.scale(20)), 0.5);
            }
            case ECHO -> {
                Sigil.Casting last = LAST_SPELL.get(player.getUUID());
                if (last != null && last != Sigil.Casting.ECHO) {
                    cast(player, level, sigil, last);
                } else {
                    Vfx.bloom(level, me);
                }
            }
            case SEEK -> {
                Vfx.shell(level, ParticleTypes.HAPPY_VILLAGER, me, 10.0, 70);
                BlockPos found = nearestOre(level, player.blockPosition(), 14);
                if (found != null) {
                    Vfx.line(level, ParticleTypes.HAPPY_VILLAGER, me, Vec3.atCenterOf(found), 0.4);
                    player.sendSystemMessage(Component.literal("§eSomething is worth digging toward."), true);
                }
            }
            case NIGHT -> grant(player, shareOut, level, MobEffects.NIGHT_VISION, 1200, 0);
            case RIFT -> {
                Vec3 at = groundAim(level, player, 14 * reach);
                VfxPlus.singularity(level, at, Vfx.VOID);
                for (LivingEntity e : near(level, player, at, 9.0)) {
                    Vec3 pull = at.subtract(e.position()).normalize().scale(1.1);
                    e.setDeltaMovement(pull);
                    e.hurtMarked = true;
                    e.hurtServer(level, level.damageSources().magic(), 20 * power);
                }
            }

            case GROW -> {
                Vfx.ring(level, ParticleTypes.HAPPY_VILLAGER, me, 5.0, 50);
                growAround(level, player.blockPosition(), 6);
            }
            case TRANSMUTE -> {
                Vfx.puff(level, ParticleTypes.ENCHANT, me, 40, 1.5, 0.05);
                transmuteAround(level, player.blockPosition(), 4);
            }
            case HEAL -> {
                VfxPlus.healingRain(level, me, 5.0);
                player.heal(20f);
                if (shareOut) {
                    for (Entity e : level.getEntities(player, new AABB(me, me).inflate(8))) {
                        if (e instanceof ServerPlayer other) other.heal(20f);
                    }
                }
            }
            case CLEANSE -> {
                player.removeAllEffects();
                player.clearFire();
                Vfx.bloom(level, me);
                grant(player, shareOut, level, MobEffects.SPEED, 300, 1);
            }
            case CORRODE -> {
                for (LivingEntity e : near(level, player, me, 7.0 * reach)) {
                    Vfx.puff(level, ParticleTypes.SMOKE, e.position().add(0, 1, 0), 20, 0.5, 0.03);
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 300, 2));
                    e.hurtServer(level, level.damageSources().magic(), 14 * power);
                }
            }
            case BLESS -> {
                Vfx.bloom(level, me);
                grant(player, true, level, MobEffects.STRENGTH, 600, 1);
                grant(player, true, level, MobEffects.REGENERATION, 400, 1);
                grant(player, true, level, MobEffects.HASTE, 600, 1);
            }
            case MIRE -> {
                Vec3 at = groundAim(level, player, 12 * reach);
                VfxPlus.fissure(level, at, look);
                for (LivingEntity e : near(level, player, at, 6.0)) {
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 300, 5));
                    e.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 300, 128));
                }
            }
            case DOMAIN -> {
                Vfx.domainCollapse(level, me, 12.0, Vfx.VOID);
                for (LivingEntity e : near(level, player, me, 12.0 * reach)) {
                    e.hurtServer(level, level.damageSources().magic(), 55 * power);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 3));
                }
            }
        }

        level.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundSource.PLAYERS, 0.7f, 1.2f);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private static void grant(ServerPlayer player, boolean shareOut, ServerLevel level,
                              net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect,
                              int ticks, int amp) {
        player.addEffect(new MobEffectInstance(effect, ticks, amp));
        if (!shareOut) return;
        Vec3 me = player.position();
        for (Entity e : level.getEntities(player, new AABB(me, me).inflate(8))) {
            if (e instanceof ServerPlayer other) other.addEffect(new MobEffectInstance(effect, ticks, amp));
        }
    }

    private static List<LivingEntity> near(ServerLevel level, ServerPlayer player, Vec3 centre, double r) {
        List<LivingEntity> out = new ArrayList<>();
        for (Entity e : level.getEntities(player, new AABB(centre, centre).inflate(r))) {
            if (e instanceof LivingEntity le && le.isAlive()) out.add(le);
        }
        return out;
    }

    private static List<LivingEntity> cone(ServerLevel level, ServerPlayer player, Vec3 origin,
                                           Vec3 look, double range, double minDot) {
        List<LivingEntity> out = new ArrayList<>();
        for (LivingEntity e : near(level, player, origin, range)) {
            Vec3 to = e.position().add(0, 1, 0).subtract(origin).normalize();
            if (to.dot(look.normalize()) >= minDot) out.add(e);
        }
        return out;
    }

    private static LivingEntity firstIn(ServerLevel level, ServerPlayer player, Vec3 origin,
                                        Vec3 look, double range) {
        LivingEntity best = null;
        double bestD = Double.MAX_VALUE;
        for (LivingEntity e : near(level, player, origin, range)) {
            Vec3 to = e.position().add(0, 1, 0).subtract(origin);
            if (to.normalize().dot(look.normalize()) < 0.85) continue;
            double d = to.lengthSqr();
            if (d < bestD) {
                bestD = d;
                best = e;
            }
        }
        return best;
    }

    private static Vec3 groundAim(ServerLevel level, ServerPlayer player, double range) {
        Vec3 me = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 cur = me;
        for (int i = 0; i < range; i++) {
            cur = cur.add(look);
            if (!level.getBlockState(BlockPos.containing(cur)).isAir()) break;
        }
        return cur;
    }

    private static BlockPos nearestOre(ServerLevel level, BlockPos base, int r) {
        BlockPos best = null;
        double bestD = Double.MAX_VALUE;
        for (int x = -r; x <= r; x += 1) {
            for (int y = -r; y <= r; y += 1) {
                for (int z = -r; z <= r; z += 1) {
                    BlockPos p = base.offset(x, y, z);
                    BlockState s = level.getBlockState(p);
                    if (!s.is(Blocks.DIAMOND_ORE) && !s.is(Blocks.DEEPSLATE_DIAMOND_ORE)
                            && !s.is(Blocks.ANCIENT_DEBRIS) && !s.is(Blocks.EMERALD_ORE)
                            && !s.is(Blocks.DEEPSLATE_EMERALD_ORE) && !s.is(Blocks.GOLD_ORE)
                            && !s.is(Blocks.DEEPSLATE_GOLD_ORE)) continue;
                    double d = p.distSqr(base);
                    if (d < bestD) {
                        bestD = d;
                        best = p;
                    }
                }
            }
        }
        return best;
    }

    private static void growAround(ServerLevel level, BlockPos base, int r) {
        for (int x = -r; x <= r; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos p = base.offset(x, y, z);
                    BlockState s = level.getBlockState(p);
                    if (s.getBlock() instanceof net.minecraft.world.level.block.BonemealableBlock b
                            && b.isValidBonemealTarget(level, p, s)) {
                        b.performBonemeal(level, level.getRandom(), p, s);
                    }
                }
            }
        }
    }

    private static void transmuteAround(ServerLevel level, BlockPos base, int r) {
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos p = base.offset(x, y, z);
                    BlockState s = level.getBlockState(p);
                    if (s.is(Blocks.STONE) || s.is(Blocks.COBBLESTONE)) {
                        level.setBlockAndUpdate(p, Blocks.DEEPSLATE.defaultBlockState());
                    } else if (s.is(Blocks.DEEPSLATE)) {
                        level.setBlockAndUpdate(p, Blocks.DEEPSLATE_IRON_ORE.defaultBlockState());
                    } else if (s.is(Blocks.GRAVEL)) {
                        level.setBlockAndUpdate(p, Blocks.COARSE_DIRT.defaultBlockState());
                    }
                }
            }
        }
    }

    /** The Lichknot returns what it stored when the wearer is next hurt badly. */
    public static float onHurt(ServerPlayer player, float damage) {
        UUID id = player.getUUID();
        Float stored = STORED.get(id);
        if (stored == null) return damage;
        STORED.put(id, stored + damage * 0.5f);
        return damage * 0.5f;
    }

    public static float takeStored(ServerPlayer player) {
        Float f = STORED.remove(player.getUUID());
        return f == null ? 0f : f;
    }

    public static void clear(UUID id) {
        FOCUS.remove(id);
        LAST_CAST.remove(id);
        LAST_SPELL.remove(id);
        TETHER.remove(id);
        STORED.remove(id);
    }
}
