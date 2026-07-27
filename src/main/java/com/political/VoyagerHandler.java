package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Runs Voyager.
 *
 * <p>Two questions decide what a Voyager piece is worth. First, are you standing in its reach, which
 * is a plain read of the dimension, the depth and whether you are in water. Second, how many Leagues
 * are you from the nearest settlement, which is the anti-camping half of the design: the line pays
 * out for genuinely being somewhere else rather than for standing in your own town wearing it.</p>
 */
public final class VoyagerHandler {

    private VoyagerHandler() {}

    /** Blocks per League. */
    private static final double LEAGUE = 250.0;
    /** Leagues past which the scaling stops, so this cannot run away forever. */
    private static final int MAX_LEAGUES = 12;

    private static final Map<UUID, Integer> SHOWN_LEAGUES = new HashMap<>();

    /** Where the player currently is, in the line's own terms. */
    public static Waypoint.Reach reachOf(ServerPlayer player) {
        Level level = player.level();
        String dim = level.dimension().identifier().toString();
        if (dim.contains("the_nether")) return Waypoint.Reach.CINDER;
        if (dim.contains("the_end")) return Waypoint.Reach.OUTER;
        if (player.isInWater() || player.isUnderWater()) return Waypoint.Reach.TIDES;
        if (player.blockPosition().getY() < 45) return Waypoint.Reach.UNDERDARK;
        return Waypoint.Reach.FRONTIER;
    }

    /**
     * How far the player is from the nearest settlement, in Leagues. With no settlements anywhere
     * this falls back to the world spawn so the line still works on a fresh world.
     */
    public static int leagues(ServerPlayer player) {
        Vec3 me = player.position();
        String dim = player.level().dimension().identifier().toString();
        double best = Double.MAX_VALUE;

        try {
            for (Settlement s : SettlementManager.getAllSettlements()) {
                if (s == null || !dim.equals(s.worldDim)) continue;
                double dx = me.x - s.cornerstoneX;
                double dz = me.z - s.cornerstoneZ;
                best = Math.min(best, Math.sqrt(dx * dx + dz * dz));
            }
        } catch (Exception ignored) {
            // A settlement store that is not up yet should never break the gear.
        }

        // No settlements in this dimension yet, so measure from the origin instead. That keeps the
        // line working on a brand new world where nobody has planted a Cornerstone.
        if (best == Double.MAX_VALUE) {
            best = Math.sqrt(me.x * me.x + me.z * me.z);
        }
        return Math.min(MAX_LEAGUES, (int) (best / LEAGUE));
    }

    private static EnumMap<Waypoint.Reach, Integer> reachCounts(ServerPlayer player) {
        EnumMap<Waypoint.Reach, Integer> counts = new EnumMap<>(Waypoint.Reach.class);
        for (ItemStack st : GearLine.carried(player)) {
            Waypoint w = VoyagerGear.waypointOf(st);
            if (w != null) counts.merge(w.reach, 1, Integer::sum);
        }
        return counts;
    }

    /** Ticked every tick, does its work once a second. */
    public static void tickPlayer(ServerPlayer player) {
        if (player.tickCount % 20 != 0) return;

        Waypoint.Reach here = reachOf(player);
        int leagues = leagues(player);
        boolean any = false;
        boolean awake = false;

        for (ItemStack st : GearLine.carried(player)) {
            Waypoint w = VoyagerGear.waypointOf(st);
            if (w == null) continue;
            any = true;
            if (w.reach != here) continue;
            awake = true;
            applyBoon(player, w, leagues);
        }

        if (!any) {
            SHOWN_LEAGUES.remove(player.getUUID());
            return;
        }

        // The reach bonus, lit at three pieces of the reach you are standing in.
        EnumMap<Waypoint.Reach, Integer> counts = reachCounts(player);
        if (counts.getOrDefault(here, 0) >= 3) {
            applyReach(player, here, leagues);
        }

        // Tell the player where they stand, but only when the number actually moves.
        UUID id = player.getUUID();
        Integer shown = SHOWN_LEAGUES.get(id);
        if (awake && (shown == null || shown != leagues)) {
            SHOWN_LEAGUES.put(id, leagues);
            player.sendSystemMessage(Component.literal(here.colour + here.label
                    + " §8reach §7| §f" + leagues + " §7Leagues out"), true);
            if (leagues >= MAX_LEAGUES && player.level() instanceof ServerLevel level) {
                Vfx.bloom(level, player.position().add(0, 1, 0));
            }
        }
    }

    /** Stat boons already live in the lore. This is the part the stats engine cannot express. */
    private static void applyBoon(ServerPlayer player, Waypoint w, int leagues) {
        int amp = leagues >= 8 ? 1 : 0;
        switch (w.boon) {
            case STRIDE -> ensure(player, MobEffects.SPEED, amp);
            case SHELTER -> ensure(player, MobEffects.RESISTANCE, amp);
            case ENDURE -> {
                ensure(player, MobEffects.SATURATION, 0);
            }
            case WARD -> ensure(player, MobEffects.HERO_OF_THE_VILLAGE, 0);
            case QUARRY -> ensure(player, MobEffects.LUCK, amp);
            case CLIMB -> {
                ensure(player, MobEffects.JUMP_BOOST, amp);
                ensure(player, MobEffects.SLOW_FALLING, 0);
            }
            case LEY -> ensure(player, MobEffects.SPEED, amp);
            case ROOTED -> ensure(player, MobEffects.RESISTANCE, 0);
            case REST -> {
                if (player.getDeltaMovement().horizontalDistanceSqr() < 0.002
                        && player.getHealth() < player.getMaxHealth()) {
                    player.heal(1.0f + leagues * 0.1f);
                }
            }
            case DELVE -> ensure(player, MobEffects.RESISTANCE, amp);
            case NIGHT -> ensure(player, MobEffects.NIGHT_VISION, 0);
            case SPIKE -> ensure(player, MobEffects.STRENGTH, amp);
            case PROSPECT -> ensure(player, MobEffects.HASTE, amp);
            case SENSE -> ensureGlowNearby(player, 12 + leagues);
            case CREEP -> ensure(player, MobEffects.INVISIBILITY, 0);
            case BREAK -> ensure(player, MobEffects.HASTE, 1 + amp);
            case BREATH -> {
                ensure(player, MobEffects.WATER_BREATHING, 0);
                ensure(player, MobEffects.DOLPHINS_GRACE, 0);
            }
            case SURGE -> {
                if (player.isInWater()) ensure(player, MobEffects.STRENGTH, 1 + amp);
            }
            case EMBERWARD -> ensure(player, MobEffects.FIRE_RESISTANCE, 0);
            case FEATHER -> ensure(player, MobEffects.SLOW_FALLING, 0);
            case BLINK -> {
                if (player.isSprinting()) ensure(player, MobEffects.SPEED, 2);
            }
        }
    }

    /** Three of the reach you are standing in. Bigger, and it scales with Leagues. */
    private static void applyReach(ServerPlayer player, Waypoint.Reach reach, int leagues) {
        int amp = leagues >= 6 ? 1 : 0;
        switch (reach) {
            case FRONTIER -> {
                ensure(player, MobEffects.SPEED, 1 + amp);
                ensure(player, MobEffects.SATURATION, 0);
            }
            case UNDERDARK -> {
                ensure(player, MobEffects.NIGHT_VISION, 0);
                ensure(player, MobEffects.HASTE, 1 + amp);
                ensure(player, MobEffects.RESISTANCE, amp);
            }
            case TIDES -> {
                ensure(player, MobEffects.WATER_BREATHING, 0);
                ensure(player, MobEffects.DOLPHINS_GRACE, 0);
                ensure(player, MobEffects.CONDUIT_POWER, amp);
            }
            case CINDER -> {
                ensure(player, MobEffects.FIRE_RESISTANCE, 0);
                ensure(player, MobEffects.STRENGTH, amp);
                ensure(player, MobEffects.RESISTANCE, amp);
            }
            case OUTER -> {
                ensure(player, MobEffects.SLOW_FALLING, 0);
                ensure(player, MobEffects.SPEED, 1 + amp);
                ensure(player, MobEffects.NIGHT_VISION, 0);
            }
        }
    }

    private static void ensure(ServerPlayer player, Holder<MobEffect> effect, int amp) {
        MobEffectInstance cur = player.getEffect(effect);
        if (cur == null || cur.getDuration() < 40 || cur.getAmplifier() < amp) {
            player.addEffect(new MobEffectInstance(effect, 120, Math.max(0, amp), true, false, false));
        }
    }

    private static void ensureGlowNearby(ServerPlayer player, double r) {
        if (!(player.level() instanceof ServerLevel level)) return;
        if (player.tickCount % 100 != 0) return;
        Vec3 me = player.position();
        for (Entity e : level.getEntities(player, new AABB(me, me).inflate(r))) {
            if (e instanceof LivingEntity le) {
                le.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120, 0, true, false, false));
            }
        }
    }

    /** Voyager pays a Leagues bonus on kills so going out actually earns something. */
    public static void onKill(ServerPlayer player) {
        Waypoint.Reach here = reachOf(player);
        int matched = 0;
        for (ItemStack st : GearLine.carried(player)) {
            Waypoint w = VoyagerGear.waypointOf(st);
            if (w != null && w.reach == here) matched++;
        }
        if (matched == 0) return;
        long coins = (long) matched * leagues(player) * 5L;
        if (coins > 0) CoinManager.giveCoinsQuiet(player, coins);
    }

    /** A quick read of where you stand, used by the Voyager page in the catalog. */
    public static Component status(ServerPlayer player) {
        Waypoint.Reach here = reachOf(player);
        return Component.literal(here.colour + here.label + " §7| §f" + leagues(player)
                + " §7Leagues").withStyle(ChatFormatting.GRAY);
    }

    public static void clear(UUID id) {
        SHOWN_LEAGUES.remove(id);
    }
}
