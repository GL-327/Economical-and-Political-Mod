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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Runs Regalia.
 *
 * <p>Standing is the whole line. It is built out of three things you cannot fake in an afternoon:
 * the tier of the settlement you are registered to, the seat you hold in its council, and whether
 * you are currently standing inside your own borders. A citizen with no seat sitting in an Outpost
 * gets almost nothing. A Chair standing in an Elysium gets the strongest numbers in the mod, and
 * loses most of them the moment they walk out of town or lose an election.</p>
 */
public final class RegaliaHandler {

    private RegaliaHandler() {}

    /** Standing runs 0 to 100. */
    public static final int MAX_STANDING = 100;

    private static final Map<UUID, Integer> SHOWN = new HashMap<>();

    /** The settlement this player is registered to, or null. */
    private static Settlement homeOf(ServerPlayer player) {
        try {
            return SettlementManager.getRegisteredSettlement(player.getUUID().toString());
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean insideHome(ServerPlayer player, Settlement home) {
        if (home == null) return false;
        try {
            String dim = player.level().dimension().identifier().toString();
            Settlement here = SettlementManager.getContaining(dim, player.blockPosition());
            return here != null && here.id != null && here.id.equals(home.id);
        } catch (Exception e) {
            return false;
        }
    }

    private static SettlementTier tierOf(Settlement home) {
        if (home == null) return SettlementTier.OUTPOST;
        try {
            return SettlementTier.valueOf(home.tier);
        } catch (Exception e) {
            return SettlementTier.OUTPOST;
        }
    }

    /** The seat this player holds in their own polity's council, or null for a plain citizen. */
    private static WarManager.WarRole seatOf(ServerPlayer player, Settlement home) {
        if (home == null) return null;
        try {
            return WarManager.roleOf(home.id, player.getUUID().toString());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Tier gives the floor, the seat gives the multiplier and being at home doubles the lot. A
     * citizen abroad is deliberately close to worthless in this line.
     */
    public static int standing(ServerPlayer player) {
        Settlement home = homeOf(player);
        if (home == null) return 0;

        SettlementTier tier = tierOf(home);
        int fromTier = switch (tier) {
            case OUTPOST -> 5;
            case HAMLET -> 10;
            case VILLAGE -> 15;
            case TOWN -> 20;
            case CITY -> 25;
            case METROPOLIS -> 30;
            case MEGALOPOLIS -> 35;
            case UTOPIA -> 40;
            case ELYSIUM -> 50;
        };

        // The Bench estate reads your settlement as one step larger than it is.
        if (estateCount(player, Office.Estate.BENCH) >= 3) fromTier += 5;

        WarManager.WarRole seat = seatOf(player, home);
        int fromSeat = seat == null ? 0 : switch (seat) {
            case CHAIR -> 30;
            case VICE_CHAIR -> 25;
            case PRIME_SEAT -> 20;
            case MINISTER -> 15;
            case SHADOW -> 10;
            case CONSTITUENT -> 10;
        };
        // Crown doubles what a seat is worth, and doubles the sting of not holding one.
        if (estateCount(player, Office.Estate.CROWN) >= 3) fromSeat *= 2;

        // Commons hands the citizens the floor a seat would normally buy.
        if (seat == null && estateCount(player, Office.Estate.COMMONS) >= 3) fromSeat = 15;

        int total = fromTier + fromSeat;
        if (insideHome(player, home)) total *= 2;

        return Math.min(MAX_STANDING, total);
    }

    private static int estateCount(ServerPlayer player, Office.Estate estate) {
        int n = 0;
        for (ItemStack st : GearLine.carried(player)) {
            Office o = RegaliaGear.officeOf(st);
            if (o != null && o.estate == estate) n++;
        }
        return n;
    }

    private static EnumMap<Office.Estate, Integer> estateCounts(ServerPlayer player) {
        EnumMap<Office.Estate, Integer> counts = new EnumMap<>(Office.Estate.class);
        for (ItemStack st : GearLine.carried(player)) {
            Office o = RegaliaGear.officeOf(st);
            if (o != null) counts.merge(o.estate, 1, Integer::sum);
        }
        return counts;
    }

    private static boolean atWar(Settlement home) {
        if (home == null) return false;
        try {
            return WarManager.isAtWar(home.id);
        } catch (Exception e) {
            return false;
        }
    }

    /** Ticked every tick, does its work once a second. */
    public static void tickPlayer(ServerPlayer player) {
        if (player.tickCount % 20 != 0) return;

        boolean any = false;
        for (ItemStack st : GearLine.carried(player)) {
            if (RegaliaGear.officeOf(st) != null) {
                any = true;
                break;
            }
        }
        if (!any) {
            SHOWN.remove(player.getUUID());
            return;
        }

        Settlement home = homeOf(player);
        int standing = standing(player);
        boolean war = atWar(home);
        boolean home_ = insideHome(player, home);

        for (ItemStack st : GearLine.carried(player)) {
            Office o = RegaliaGear.officeOf(st);
            if (o != null) applyMandate(player, o, standing, war, home_);
        }

        EnumMap<Office.Estate, Integer> counts = estateCounts(player);
        for (Map.Entry<Office.Estate, Integer> e : counts.entrySet()) {
            if (e.getValue() >= 3) applyEstate(player, e.getKey(), standing, war, home_);
        }

        // Report Standing when it actually moves.
        UUID id = player.getUUID();
        Integer shown = SHOWN.get(id);
        if (shown == null || shown != standing) {
            SHOWN.put(id, standing);
            String where = home == null ? "§8unregistered" : (home_ ? "§ahome" : "§7abroad");
            player.sendSystemMessage(Component.literal("§6⚖ Standing §f" + standing + "§8/§f"
                    + MAX_STANDING + " §8| " + where + (war ? " §c| at war" : "")), true);
            if (standing >= MAX_STANDING && player.level() instanceof ServerLevel level) {
                Vfx.bloom(level, player.position().add(0, 1, 0));
            }
        }
    }

    private static void applyMandate(ServerPlayer player, Office o, int standing,
                                     boolean war, boolean home) {
        int amp = standing >= 80 ? 2 : standing >= 45 ? 1 : 0;
        if (standing < 15) return;

        switch (o.mandate) {
            case COMMAND -> ensure(player, MobEffects.STRENGTH, amp);
            case AUTHORITY -> ensure(player, MobEffects.HERO_OF_THE_VILLAGE, amp);
            case VERDICT -> {
                if (home) ensure(player, MobEffects.STRENGTH, amp);
            }
            case AEGIS -> ensure(player, MobEffects.RESISTANCE, Math.min(2, amp));
            case TENURE -> ensure(player, MobEffects.LUCK, amp);
            case FILIBUSTER -> {
                if (home) {
                    ensure(player, MobEffects.RESISTANCE, 1);
                    player.setTicksFrozen(0);
                    player.removeEffect(MobEffects.SLOWNESS);
                }
            }
            case TITHE -> {
                // Paid out once a minute so it reads as income rather than as a trickle.
                if (home && player.tickCount % 1200 == 0) {
                    CoinManager.giveCoinsQuiet(player, standing * 3L);
                }
            }
            case PROSPERITY -> {
                if (home && player.tickCount % 1200 == 0) {
                    player.giveExperiencePoints(standing / 2);
                }
            }
            case MUSTER -> shareOut(player, MobEffects.STRENGTH, amp);
            case WARFOOT -> {
                if (war) ensure(player, MobEffects.STRENGTH, Math.min(2, amp + 1));
            }
            case CONCORD -> {
                if (home && player.getHealth() < player.getMaxHealth()) {
                    player.heal(0.5f + standing * 0.02f);
                }
            }
            case HEARTH -> shareOut(player, MobEffects.REGENERATION, Math.min(1, amp));
        }
    }

    private static void applyEstate(ServerPlayer player, Office.Estate estate, int standing,
                                    boolean war, boolean home) {
        int amp = standing >= 70 ? 1 : 0;
        switch (estate) {
            case CROWN -> {
                ensure(player, MobEffects.STRENGTH, amp);
                ensure(player, MobEffects.HERO_OF_THE_VILLAGE, 0);
            }
            case BENCH -> {
                ensure(player, MobEffects.RESISTANCE, amp);
                ensure(player, MobEffects.LUCK, 1);
            }
            case COFFER -> {
                if (home && player.tickCount % 1200 == 0) {
                    CoinManager.giveCoinsQuiet(player, standing * 5L);
                }
                ensure(player, MobEffects.LUCK, amp);
            }
            case GUARD -> {
                if (war) {
                    ensure(player, MobEffects.STRENGTH, 1 + amp);
                    ensure(player, MobEffects.SPEED, amp);
                } else {
                    ensure(player, MobEffects.RESISTANCE, 1);
                }
            }
            case COMMONS -> {
                ensure(player, MobEffects.REGENERATION, amp);
                ensure(player, MobEffects.SATURATION, 0);
            }
        }
    }

    private static void ensure(ServerPlayer player, Holder<MobEffect> effect, int amp) {
        MobEffectInstance cur = player.getEffect(effect);
        if (cur == null || cur.getDuration() < 40 || cur.getAmplifier() < amp) {
            player.addEffect(new MobEffectInstance(effect, 120, Math.max(0, amp), true, false, false));
        }
    }

    /** Hands the same effect to everyone standing with you, which is what a Muster is for. */
    private static void shareOut(ServerPlayer player, Holder<MobEffect> effect, int amp) {
        ensure(player, effect, amp);
        if (!(player.level() instanceof ServerLevel level)) return;
        Vec3 me = player.position();
        for (Entity e : level.getEntities(player, new AABB(me, me).inflate(10))) {
            if (e instanceof ServerPlayer other) {
                other.addEffect(new MobEffectInstance(effect, 120, Math.max(0, amp), true, false, false));
            }
        }
    }

    /** Regalia hits harder in a war your polity is actually in. */
    public static float damageMultiplier(ServerPlayer player) {
        int matched = 0;
        for (ItemStack st : GearLine.carried(player)) {
            if (RegaliaGear.officeOf(st) != null) matched++;
        }
        if (matched == 0) return 1.0f;
        Settlement home = homeOf(player);
        float base = 1.0f + (standing(player) / 100f) * 0.25f;
        if (atWar(home) && estateCount(player, Office.Estate.GUARD) >= 3) base += 0.25f;
        return base;
    }

    /** A one line read of where the player stands, for the Regalia page in the catalog. */
    public static Component status(ServerPlayer player) {
        Settlement home = homeOf(player);
        if (home == null) {
            return Component.literal("§8Not registered to a settlement.");
        }
        return Component.literal("§6" + home.name + " §8| " + tierOf(home).colorCode
                + tierOf(home).displayName + " §8| §fStanding " + standing(player))
                .withStyle(ChatFormatting.GRAY);
    }

    public static void clear(UUID id) {
        SHOWN.remove(id);
    }
}
