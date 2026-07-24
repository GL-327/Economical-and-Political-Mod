package com.political;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * V7 Pillar B — the live behaviour of the {@link CursedArsenal}.
 *
 * <p>Everything here is additive: it registers its own Fabric {@code AFTER_DAMAGE} listener and a
 * per-player tick, so no existing combat class or mixin is touched. All bonus damage is dealt as
 * entity-less {@code magic} damage, which the other combat listeners ignore, and a local re-entry
 * guard stops this class from re-triggering itself.</p>
 *
 * <h2>Signatures</h2>
 * <ul>
 *   <li><b>Sword</b> — cleave: a fraction of the hit spills onto up to four nearby foes.</li>
 *   <li><b>Scythe</b> — reap: bonus true damage plus a small, capped life drain (curse targets only).</li>
 *   <li><b>Katana</b> — a Black Flash window: land a hit 0.35s–0.9s after your last katana strike for a burst.</li>
 *   <li><b>Gauntlet</b> — a combo meter (built by rhythmic hits, decays after 4s) that raises your damage.</li>
 *   <li><b>Armor set</b> — converts a slice of the damage you take into Cursed Energy and lashes a little back.</li>
 *   <li><b>Charms</b> — a bounded off-hand buff (Strength or Absorption).</li>
 * </ul>
 *
 * <h2>Anti-farm</h2>
 * <ul>
 *   <li>Scythe life drain is hard-capped per hit and only triggers against hostiles, so you cannot
 *       heal off passive animals.</li>
 *   <li>The armor's Cursed Energy conversion only fires on damage from a real attacking entity
 *       (never fall, lava, fire or drowning), is capped per hit, and has a short cooldown.</li>
 * </ul>
 */
public final class CursedArsenalHandler {

    private CursedArsenalHandler() {}

    // ── Tunables ────────────────────────────────────────────────
    private static final long KATANA_BF_MIN_MS = 350L;
    private static final long KATANA_BF_MAX_MS = 900L;
    private static final float[] KATANA_BF_FRAC = {0.40f, 0.50f, 0.60f, 0.70f, 0.90f};

    private static final float[] SWORD_CLEAVE_FRAC = {0.15f, 0.20f, 0.25f, 0.30f, 0.35f};
    private static final int SWORD_CLEAVE_TARGETS = 4;

    private static final float[] SCYTHE_REAP_FRAC = {0.20f, 0.25f, 0.30f, 0.35f, 0.40f};
    private static final float SCYTHE_HEAL_CAP = 2.0f;

    private static final int GAUNTLET_COMBO_MAX = 10;
    private static final long GAUNTLET_MIN_INTERVAL_MS = 250L;
    private static final long GAUNTLET_DECAY_MS = 4000L;
    private static final float[] GAUNTLET_STEP = {0.03f, 0.04f, 0.05f, 0.06f, 0.08f};

    private static final float[] ARMOR_CONV_FRAC = {0.06f, 0.07f, 0.08f, 0.09f, 0.10f};
    private static final long ARMOR_CONV_CAP = 12L;
    private static final long ARMOR_CONV_COOLDOWN_MS = 1200L;
    private static final float[] ARMOR_BACKLASH_FRAC = {0.10f, 0.13f, 0.16f, 0.20f, 0.25f};

    // ── State ───────────────────────────────────────────────────
    private static final Map<UUID, Long> katanaLastHit = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> gauntletCombo = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> gauntletLastHit = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> armorLastConvert = new ConcurrentHashMap<>();

    private static boolean busy = false;
    private static boolean registered = false;

    // ── Registration ────────────────────────────────────────────
    public static void register() {
        if (registered) return;
        registered = true;
        ServerLivingEntityEvents.AFTER_DAMAGE.register(CursedArsenalHandler::onAfterDamage);
    }

    private static void onAfterDamage(LivingEntity target, DamageSource source,
                                      float baseDamageTaken, float damageTaken, boolean blocked) {
        if (busy || blocked || damageTaken <= 0.0f) return;

        // Armor path: the wearer (target) took a hit.
        if (target instanceof ServerPlayer worn && target.level() instanceof ServerLevel sl) {
            try {
                onWearerHit(worn, sl, source, damageTaken);
            } catch (Throwable t) {
                PoliticalServer.LOGGER.warn("[Arsenal] armor hook failed", t);
            }
        }

        // Weapon path: an arsenal wielder landed a melee hit.
        Entity atk = source.getEntity();
        if (!(atk instanceof ServerPlayer p) || atk == target) return;
        if (source.getDirectEntity() != atk) return; // projectiles / indirect are not melee
        if (!(p.level() instanceof ServerLevel sl)) return;

        CursedArsenal.Weapon w = CursedArsenal.weaponOf(p.getMainHandItem());
        if (w == null) return;
        CursedGrade g = CursedArsenal.gradeOf(p.getMainHandItem());
        if (g == null) return;
        int gi = CursedArsenal.gradeIndex(g);

        busy = true;
        try {
            switch (w) {
                case SWORD -> cleave(p, sl, target, damageTaken, gi);
                case SCYTHE -> reap(p, sl, target, damageTaken, gi);
                case KATANA -> blackFlash(p, sl, target, damageTaken, gi);
                case GAUNTLET -> combo(p, sl, target, damageTaken, gi);
            }
        } catch (Throwable t) {
            PoliticalServer.LOGGER.warn("[Arsenal] weapon hook failed", t);
        } finally {
            busy = false;
        }
    }

    // ── Weapon signatures ───────────────────────────────────────
    private static void cleave(ServerPlayer p, ServerLevel sl, LivingEntity target, float dmg, int gi) {
        float each = dmg * SWORD_CLEAVE_FRAC[gi];
        if (each < 0.05f) return;
        int hit = 0;
        for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class,
                target.getBoundingBox().inflate(2.5), le -> le != p && le != target && le.isAlive())) {
            if (hit >= SWORD_CLEAVE_TARGETS) break;
            e.invulnerableTime = 0;
            e.hurtServer(sl, p.damageSources().magic(), each);
            hit++;
        }
        if (hit > 0) {
            sl.sendParticles(ParticleTypes.SWEEP_ATTACK,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                    2, 0.4, 0.2, 0.4, 0.0);
        }
    }

    private static void reap(ServerPlayer p, ServerLevel sl, LivingEntity target, float dmg, int gi) {
        float bleed = dmg * SCYTHE_REAP_FRAC[gi];
        if (bleed > 0.05f && target.isAlive()) {
            target.invulnerableTime = 0;
            target.hurtServer(sl, p.damageSources().magic(), bleed);
        }
        // Life drain: only from hostiles, and hard-capped so it can never become sustain farm.
        if (target instanceof Enemy) {
            float heal = Math.min(SCYTHE_HEAL_CAP, bleed * 0.3f);
            if (heal > 0.05f && p.getHealth() < p.getMaxHealth()) {
                p.heal(heal);
                sl.sendParticles(ParticleTypes.DAMAGE_INDICATOR,
                        p.getX(), p.getY() + 1.0, p.getZ(), 3, 0.2, 0.3, 0.2, 0.01);
            }
        }
        sl.sendParticles(ParticleTypes.CRIMSON_SPORE,
                target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                6, 0.3, 0.3, 0.3, 0.02);
    }

    private static void blackFlash(ServerPlayer p, ServerLevel sl, LivingEntity target, float dmg, int gi) {
        long now = System.currentTimeMillis();
        long dt = now - katanaLastHit.getOrDefault(p.getUUID(), 0L);
        katanaLastHit.put(p.getUUID(), now);
        if (dt < KATANA_BF_MIN_MS || dt > KATANA_BF_MAX_MS) return;

        float bonus = dmg * KATANA_BF_FRAC[gi];
        if (bonus > 0.05f && target.isAlive()) {
            target.invulnerableTime = 0;
            target.hurtServer(sl, p.damageSources().magic(), bonus);
        }
        sl.sendParticles(ParticleTypes.SONIC_BOOM,
                target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(),
                1, 0.0, 0.0, 0.0, 0.0);
        sl.sendParticles(ParticleTypes.ENCHANTED_HIT,
                target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(),
                16, 0.35, 0.35, 0.35, 0.2);
        sl.playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT,
                SoundSource.PLAYERS, 1.1f, 0.55f);
        p.sendSystemMessage(Component.literal("§c§l⚡ BLACK FLASH!").withStyle(ChatFormatting.RED), true);
    }

    private static void combo(ServerPlayer p, ServerLevel sl, LivingEntity target, float dmg, int gi) {
        long now = System.currentTimeMillis();
        long dt = now - gauntletLastHit.getOrDefault(p.getUUID(), 0L);
        gauntletLastHit.put(p.getUUID(), now);

        int count = gauntletCombo.getOrDefault(p.getUUID(), 0);
        if (dt < GAUNTLET_MIN_INTERVAL_MS) {
            count = 0; // spam / autoclick breaks the rhythm
        } else if (dt <= GAUNTLET_DECAY_MS) {
            count = Math.min(GAUNTLET_COMBO_MAX, count + 1);
        } else {
            count = 1;
        }
        gauntletCombo.put(p.getUUID(), count);

        float bonus = dmg * GAUNTLET_STEP[gi] * count;
        if (bonus > 0.05f && target.isAlive()) {
            target.invulnerableTime = 0;
            target.hurtServer(sl, p.damageSources().magic(), bonus);
        }
        if (count > 1) {
            p.sendSystemMessage(Component.literal("§6Combo §fx" + count), true);
            sl.sendParticles(ParticleTypes.CRIT,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                    count, 0.3, 0.3, 0.3, 0.1);
        }
    }

    // ── Armor set ───────────────────────────────────────────────
    private static void onWearerHit(ServerPlayer p, ServerLevel sl, DamageSource source, float dmg) {
        Entity attacker = source.getEntity();
        if (attacker == null) return; // no environmental farming (fall / lava / fire / drown)

        int setGrade = fullSetGradeIndex(p);
        if (setGrade < 0) return;

        // Convert a slice of the incoming hit into Cursed Energy, capped and rate-limited.
        long now = System.currentTimeMillis();
        long last = armorLastConvert.getOrDefault(p.getUUID(), 0L);
        if (now - last >= ARMOR_CONV_COOLDOWN_MS) {
            long gained = Math.min(ARMOR_CONV_CAP, Math.round(dmg * ARMOR_CONV_FRAC[setGrade]));
            if (gained > 0) {
                CursedEnergyManager.earn(p, gained);
                armorLastConvert.put(p.getUUID(), now);
                sl.sendParticles(ParticleTypes.SCULK_SOUL, p.getX(), p.getY() + 1.2, p.getZ(),
                        4, 0.3, 0.4, 0.3, 0.01);
            }
        }

        // Backlash: a little of the hit lashes back at a living melee attacker.
        if (attacker instanceof LivingEntity le && le.isAlive() && le != p) {
            float back = dmg * ARMOR_BACKLASH_FRAC[setGrade];
            if (back > 0.05f) {
                busy = true;
                try {
                    le.invulnerableTime = 0;
                    le.hurtServer(sl, p.damageSources().magic(), back);
                } finally {
                    busy = false;
                }
            }
        }
    }

    /** Lowest grade index across the four worn arsenal pieces, or -1 if the set is incomplete. */
    private static int fullSetGradeIndex(ServerPlayer p) {
        int min = Integer.MAX_VALUE;
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (EquipmentSlot slot : slots) {
            ItemStack s = p.getItemBySlot(slot);
            if (CursedArsenal.pieceOf(s) == null) return -1;
            CursedGrade g = CursedArsenal.gradeOf(s);
            if (g == null) return -1;
            min = Math.min(min, CursedArsenal.gradeIndex(g));
        }
        return min == Integer.MAX_VALUE ? -1 : min;
    }

    // ── Per-player tick (combo decay + charm off-hand buffs) ─────
    public static void tickPlayer(ServerPlayer player) {
        long now = System.currentTimeMillis();
        Integer combo = gauntletCombo.get(player.getUUID());
        if (combo != null && combo > 0
                && now - gauntletLastHit.getOrDefault(player.getUUID(), 0L) > GAUNTLET_DECAY_MS) {
            gauntletCombo.put(player.getUUID(), 0);
        }

        if (player.tickCount % 20 != 0) return;

        ItemStack off = player.getItemBySlot(EquipmentSlot.OFFHAND);
        CursedArsenal.Charm charm = CursedArsenal.charmOf(off);
        if (charm == null) return;
        CursedGrade g = CursedArsenal.gradeOf(off);
        if (g == null) return;
        int gi = CursedArsenal.gradeIndex(g);

        switch (charm) {
            case MALEVOLENT -> {
                int amp = new int[]{0, 0, 1, 1, 2}[gi];
                player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 40, amp, true, false, true));
                if (gi >= 3) {
                    player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 0, true, false, true));
                }
            }
            case WARDING -> {
                int amp = new int[]{0, 0, 1, 1, 2}[gi];
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, amp, true, false, true));
                if (gi >= 3) {
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, true, false, true));
                }
            }
        }
    }

    /** Clears transient combat state for a leaving player. */
    public static void onDisconnect(UUID uuid) {
        katanaLastHit.remove(uuid);
        gauntletCombo.remove(uuid);
        gauntletLastHit.remove(uuid);
        armorLastConvert.remove(uuid);
    }
}
