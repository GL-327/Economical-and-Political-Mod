package com.political;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

/**
 * Runs the Runeforge in play. The piece decides when a rune fires, the rune decides what happens,
 * and every firing draws its attunement's signature so the effect is visible as well as felt.
 *
 * <p>Every path is cooldown gated per player and per source, so no amount of clicking turns a rune
 * into a strobe.</p>
 */
public final class RuneforgedHandler {

    private RuneforgedHandler() {}

    private static final long WEAPON_CD_MS = 2_500L;
    private static final long TOOL_CD_MS = 5_000L;
    private static final long ARMOUR_CD_MS = 8_000L;
    private static final long CHARM_CD_MS = 30_000L;

    private static final Map<String, Long> COOLDOWNS = new HashMap<>();

    private static boolean ready(String key, long ms) {
        long now = System.currentTimeMillis();
        Long last = COOLDOWNS.get(key);
        if (last != null && now - last < ms) return false;
        COOLDOWNS.put(key, now);
        return true;
    }

    // ── Entry points ────────────────────────────────────────────────────────

    /** A player landed a hit. Fires a Runeforged weapon. */
    public static void onHit(ServerPlayer player, LivingEntity target) {
        ItemStack held = player.getMainHandItem();
        Rune r = RuneforgedGear.runeOf(held);
        RuneforgedGear.Piece p = RuneforgedGear.pieceOf(held);
        if (r == null || p == null || p.kind != RuneforgedGear.Piece.Kind.WEAPON) return;
        if (!(player.level() instanceof ServerLevel level)) return;
        if (!ready(player.getUUID() + ":runehit", WEAPON_CD_MS)) return;

        fire(player, level, target, target.position().add(0, target.getBbHeight() * 0.5, 0), r);
    }

    /** A player broke a block. Fires a Runeforged tool. */
    public static void onBreak(ServerPlayer player, Vec3 at) {
        ItemStack held = player.getMainHandItem();
        Rune r = RuneforgedGear.runeOf(held);
        RuneforgedGear.Piece p = RuneforgedGear.pieceOf(held);
        if (r == null || p == null || p.kind != RuneforgedGear.Piece.Kind.TOOL) return;
        if (!(player.level() instanceof ServerLevel level)) return;
        if (!ready(player.getUUID() + ":runebreak", TOOL_CD_MS)) return;

        fire(player, level, null, at, r);
    }

    /** Ticked per player for worn pieces and the talisman. */
    public static void tickPlayer(ServerPlayer player) {
        if (player.tickCount % 20 != 0) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        // Worn armour fires on a slow rhythm.
        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack worn = player.getItemBySlot(slot);
            Rune r = RuneforgedGear.runeOf(worn);
            RuneforgedGear.Piece p = RuneforgedGear.pieceOf(worn);
            if (r == null || p == null) continue;
            if (!ready(player.getUUID() + ":runewear:" + slot.name(), ARMOUR_CD_MS)) continue;
            // Worn pieces only ever help the wearer, never lash out on their own.
            selfEffect(player, level, r);
        }

        // A talisman in hand answers when things go badly.
        ItemStack off = player.getOffhandItem();
        Rune charmRune = RuneforgedGear.runeOf(off);
        RuneforgedGear.Piece charmPiece = RuneforgedGear.pieceOf(off);
        if (charmRune != null && charmPiece != null
                && charmPiece.kind == RuneforgedGear.Piece.Kind.CHARM
                && player.getHealth() <= player.getMaxHealth() * 0.4f
                && ready(player.getUUID() + ":runecharm", CHARM_CD_MS)) {
            charmRune.attunement.signature(level, player.position());
            selfEffect(player, level, charmRune);
        }
    }

    // ── The effects ─────────────────────────────────────────────────────────

    /** Fires a rune at a target and a position. Target may be null for tool breaks. */
    private static void fire(ServerPlayer player, ServerLevel level, LivingEntity target, Vec3 at, Rune r) {
        r.attunement.signature(level, at);

        switch (r.effect) {
            case IGNITE -> {
                if (target != null) target.setRemainingFireTicks(r.magnitude);
            }
            case CHILL -> {
                if (target != null) target.addEffect(new MobEffectInstance(
                        MobEffects.SLOWNESS, r.magnitude, 1, true, false, true));
            }
            case SHOCK -> {
                // Arcs to a couple of neighbours as well as the target.
                if (target != null) {
                    target.hurtServer(level, player.damageSources().magic(), r.magnitude / 20.0f);
                    int hit = 0;
                    for (LivingEntity near : level.getEntitiesOfClass(LivingEntity.class,
                            target.getBoundingBox().inflate(4.0),
                            e -> e != player && e != target && e.isAlive() && e instanceof Monster)) {
                        near.hurtServer(level, player.damageSources().magic(), r.magnitude / 30.0f);
                        if (++hit >= 2) break;
                    }
                }
            }
            case POISON -> {
                if (target != null) target.addEffect(new MobEffectInstance(
                        MobEffects.POISON, r.magnitude, 1, true, false, true));
            }
            case WITHER -> {
                if (target != null) target.addEffect(new MobEffectInstance(
                        MobEffects.WITHER, r.magnitude, 0, true, false, true));
            }
            case LEECH -> player.heal(r.magnitude / 10.0f);
            case REND -> {
                if (target != null) target.addEffect(new MobEffectInstance(
                        MobEffects.WEAKNESS, r.magnitude, 1, true, false, true));
            }
            case STAGGER -> {
                if (target != null) {
                    target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, r.magnitude, 4, true, false, true));
                    target.setDeltaMovement(Vec3.ZERO);
                }
            }
            case BLIND -> {
                if (target != null) target.addEffect(new MobEffectInstance(
                        MobEffects.BLINDNESS, r.magnitude, 0, true, false, true));
            }
            case LEVITATE -> {
                if (target != null) target.addEffect(new MobEffectInstance(
                        MobEffects.LEVITATION, r.magnitude, 0, true, false, true));
            }
            case PULL -> {
                for (LivingEntity near : level.getEntitiesOfClass(LivingEntity.class,
                        player.getBoundingBox().inflate(7.0),
                        e -> e != player && e.isAlive() && e instanceof Monster)) {
                    Vec3 toward = at.subtract(near.position()).normalize().scale(0.6);
                    near.push(toward.x, 0.15, toward.z);
                }
            }
            case PUSH -> {
                for (LivingEntity near : level.getEntitiesOfClass(LivingEntity.class,
                        player.getBoundingBox().inflate(6.0),
                        e -> e != player && e.isAlive() && e instanceof Monster)) {
                    Vec3 away = near.position().subtract(at).normalize().scale(0.8);
                    near.push(away.x, 0.25, away.z);
                }
            }
            case ENERGY -> CursedEnergyManager.earn(player, r.magnitude);
            case COIN -> CoinManager.giveCoinsQuiet(player, r.magnitude);
            case XP -> player.giveExperiencePoints(r.magnitude);
            case MARK -> {
                if (target != null) target.addEffect(new MobEffectInstance(
                        MobEffects.GLOWING, r.magnitude, 0, true, false, true));
            }
            default -> selfEffect(player, level, r);
        }

        // Self buff runes always also help the wielder.
        switch (r.effect) {
            case HASTE_SELF, SHIELD_SELF, HEAL_SELF, SPEED_SELF -> selfEffect(player, level, r);
            default -> { }
        }
    }

    /** The wielder facing half of a rune, used by worn pieces and self buff runes. */
    private static void selfEffect(ServerPlayer player, ServerLevel level, Rune r) {
        switch (r.effect) {
            case HASTE_SELF -> player.addEffect(new MobEffectInstance(
                    MobEffects.HASTE, r.magnitude, 1, true, false, false));
            case SHIELD_SELF -> player.addEffect(new MobEffectInstance(
                    MobEffects.RESISTANCE, r.magnitude, 1, true, false, false));
            case HEAL_SELF -> player.heal(r.magnitude / 10.0f);
            case SPEED_SELF -> player.addEffect(new MobEffectInstance(
                    MobEffects.SPEED, r.magnitude, 1, true, false, false));
            case LEECH -> player.heal(r.magnitude / 20.0f);
            case ENERGY -> CursedEnergyManager.earn(player, Math.max(1, r.magnitude / 2));
            // Worn offensive runes give a quiet defensive echo instead of attacking.
            default -> player.addEffect(new MobEffectInstance(
                    MobEffects.RESISTANCE, 40, 0, true, false, false));
        }
    }

    public static void clear(java.util.UUID uuid) {
        COOLDOWNS.keySet().removeIf(k -> k.startsWith(uuid.toString()));
    }
}
