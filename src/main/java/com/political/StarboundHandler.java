package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Runs Starbound gear. Constellations are always on, so this is a steady tick rather than a set of
 * procs. Stat boons are already in the item lore and picked up by the stats engine, so the work
 * here is the effect boons and the house bonuses.
 */
public final class StarboundHandler {

    private StarboundHandler() {}

    /** How many pieces of one house are needed to light its bonus. */
    private static final int HOUSE_THRESHOLD = 3;

    private static final Map<String, Constellation.House> LIT = new HashMap<>();

    /** Counts constellations across the armour slots, both hands included. */
    private static EnumMap<Constellation.House, Integer> houseCounts(ServerPlayer player) {
        EnumMap<Constellation.House, Integer> counts = new EnumMap<>(Constellation.House.class);
        ItemStack[] carried = {
                player.getItemBySlot(EquipmentSlot.HEAD),
                player.getItemBySlot(EquipmentSlot.CHEST),
                player.getItemBySlot(EquipmentSlot.LEGS),
                player.getItemBySlot(EquipmentSlot.FEET),
                player.getMainHandItem(),
                player.getOffhandItem()
        };
        for (ItemStack st : carried) {
            Constellation c = StarboundGear.constellationOf(st);
            if (c != null) counts.merge(c.house, 1, Integer::sum);
        }
        return counts;
    }

    /** Ticked once per second per player. */
    public static void tickPlayer(ServerPlayer player) {
        if (player.tickCount % 20 != 0) return;

        // Effect boons from every carried constellation.
        boolean any = false;
        ItemStack[] carried = {
                player.getItemBySlot(EquipmentSlot.HEAD),
                player.getItemBySlot(EquipmentSlot.CHEST),
                player.getItemBySlot(EquipmentSlot.LEGS),
                player.getItemBySlot(EquipmentSlot.FEET),
                player.getMainHandItem(),
                player.getOffhandItem()
        };
        for (ItemStack st : carried) {
            Constellation c = StarboundGear.constellationOf(st);
            if (c == null) continue;
            any = true;
            applyBoon(player, c);
        }
        if (!any) {
            LIT.remove(player.getUUID().toString());
            return;
        }

        // House bonus, lit at three pieces of one house.
        EnumMap<Constellation.House, Integer> counts = houseCounts(player);
        Constellation.House best = null;
        int bestCount = 0;
        for (Map.Entry<Constellation.House, Integer> e : counts.entrySet()) {
            if (e.getValue() > bestCount) {
                bestCount = e.getValue();
                best = e.getKey();
            }
        }

        String uuid = player.getUUID().toString();
        if (best != null && bestCount >= HOUSE_THRESHOLD) {
            applyHouse(player, best);
            if (LIT.get(uuid) != best) {
                LIT.put(uuid, best);
                player.sendSystemMessage(Component.literal("✦ House of the " + best.label + " is lit.")
                        .withStyle(best.colour, ChatFormatting.BOLD), true);
                if (player.level() instanceof ServerLevel level) {
                    Vfx.bloom(level, player.position().add(0, 1, 0));
                }
            }
        } else {
            LIT.remove(uuid);
        }
    }

    private static void applyBoon(ServerPlayer player, Constellation c) {
        switch (c.boon) {
            case SPEED -> ensure(player, MobEffects.SPEED, 0);
            case HASTE -> ensure(player, MobEffects.HASTE, 0);
            case REGEN -> {
                if (player.getHealth() < player.getMaxHealth()) player.heal(0.4f);
            }
            case NIGHT_EYE -> ensure(player, MobEffects.NIGHT_VISION, 0);
            case WATER_GRACE -> {
                ensure(player, MobEffects.WATER_BREATHING, 0);
                if (player.isInWater()) ensure(player, MobEffects.DOLPHINS_GRACE, 0);
            }
            case FIRE_WARD -> ensure(player, MobEffects.FIRE_RESISTANCE, 0);
            // Coin and experience boons are handled where those are awarded.
            default -> { }
        }
    }

    private static void applyHouse(ServerPlayer player, Constellation.House house) {
        switch (house) {
            case FORGE -> {
                ensure(player, MobEffects.HASTE, 1);
                ensure(player, MobEffects.FIRE_RESISTANCE, 0);
            }
            case HUNT -> {
                ensure(player, MobEffects.SPEED, 0);
                ensure(player, MobEffects.STRENGTH, 0);
            }
            case DEEP -> {
                ensure(player, MobEffects.WATER_BREATHING, 0);
                ensure(player, MobEffects.NIGHT_VISION, 0);
                ensure(player, MobEffects.RESISTANCE, 0);
            }
            case CROWN -> {
                ensure(player, MobEffects.HERO_OF_THE_VILLAGE, 0);
                ensure(player, MobEffects.RESISTANCE, 0);
            }
            case VEIL -> {
                ensure(player, MobEffects.SPEED, 1);
                ensure(player, MobEffects.NIGHT_VISION, 0);
            }
            case BLOOM -> {
                ensure(player, MobEffects.REGENERATION, 0);
                ensure(player, MobEffects.SATURATION, 0);
            }
        }
    }

    private static void ensure(ServerPlayer player, net.minecraft.core.Holder<MobEffect> effect, int amp) {
        MobEffectInstance cur = player.getEffect(effect);
        if (cur == null || cur.getDuration() < 40 || cur.getAmplifier() < amp) {
            player.addEffect(new MobEffectInstance(effect, 120, amp, true, false, false));
        }
    }

    /** Extra coin from Coffer and Harvest, called where kills pay out. */
    public static long coinBonus(ServerPlayer player) {
        long bonus = 0;
        ItemStack[] carried = {
                player.getItemBySlot(EquipmentSlot.HEAD),
                player.getItemBySlot(EquipmentSlot.CHEST),
                player.getItemBySlot(EquipmentSlot.LEGS),
                player.getItemBySlot(EquipmentSlot.FEET),
                player.getMainHandItem(),
                player.getOffhandItem()
        };
        for (ItemStack st : carried) {
            Constellation c = StarboundGear.constellationOf(st);
            if (c != null && c.boon == Constellation.Boon.COIN_FIND) bonus += 8;
        }
        return bonus;
    }

    /** Called on a kill so the coin and experience boons pay out. */
    public static void onKill(ServerPlayer player) {
        long coins = coinBonus(player);
        if (coins > 0) CoinManager.giveCoinsQuiet(player, coins);

        int xp = 0;
        ItemStack[] carried = {
                player.getItemBySlot(EquipmentSlot.HEAD),
                player.getItemBySlot(EquipmentSlot.CHEST),
                player.getItemBySlot(EquipmentSlot.LEGS),
                player.getItemBySlot(EquipmentSlot.FEET),
                player.getMainHandItem(),
                player.getOffhandItem()
        };
        for (ItemStack st : carried) {
            Constellation c = StarboundGear.constellationOf(st);
            if (c != null && c.boon == Constellation.Boon.XP_GAIN) xp += 3;
        }
        if (xp > 0) player.giveExperiencePoints(xp);
    }

    public static void clear(java.util.UUID uuid) {
        LIT.remove(uuid.toString());
    }
}
