package com.political;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The meter shared by the five newer gear lines.
 *
 * <p>Tidecaller, Emberforge, Thornwood, Ashen Crown and Prismatic all work the same way. Landing
 * hits fills a meter, and when it fills the piece in your hand spends it. One handler serves all
 * five because the only thing that differs is what the spend looks like, and five copies of this
 * file would have drifted apart the first time one of them was fixed.</p>
 *
 * <p>What the spend does is keyed off the motif's group rather than off the individual motif. Two
 * hundred and twenty five separate effects would be two hundred and twenty five things to balance,
 * and nobody would notice the difference between the ninth and the tenth variation on "it hits
 * harder". The group is the unit a player actually feels.</p>
 */
public final class MeterLineHandler {

    private MeterLineHandler() {}

    /** Hits needed to fill, matching the "fills at 16 hits" line every piece carries. */
    private static final int FILL = 16;

    private static final Map<UUID, Integer> CHARGE = new HashMap<>();
    private static final Map<UUID, Long> LAST_SPEND = new HashMap<>();

    public static void forget(ServerPlayer player) {
        CHARGE.remove(player.getUUID());
        LAST_SPEND.remove(player.getUUID());
    }

    public static int chargeOf(ServerPlayer player) {
        return CHARGE.getOrDefault(player.getUUID(), 0);
    }

    /**
     * Which of the five lines this stack belongs to, or null.
     *
     * <p>One lookup per hit rather than five, since a stack can only be one of them.</p>
     */
    private static Line lineOf(ItemStack stack) {
        String id = SlayerItems.getCustomItemId(stack);
        if (id == null) return null;
        if (id.startsWith("tide_")) return Line.TIDE;
        if (id.startsWith("ember_")) return Line.EMBER;
        if (id.startsWith("thorn_")) return Line.THORN;
        if (id.startsWith("ashen_")) return Line.ASHEN;
        if (id.startsWith("prism_")) return Line.PRISM;
        return null;
    }

    private enum Line {
        TIDE("Current", "§b"),
        EMBER("Heat", "§c"),
        THORN("Growth", "§a"),
        ASHEN("The Crown", "§8"),
        PRISM("Light", "§d");

        final String meter;
        final String colour;

        Line(String meter, String colour) {
            this.meter = meter;
            this.colour = colour;
        }
    }

    /** The group of whatever motif this stack carries, as a lowercase word. */
    private static String groupOf(ItemStack stack, Line line) {
        return switch (line) {
            case TIDE -> {
                Tide t = TidecallerGear.motifOf(stack);
                yield t == null ? null : t.group.name();
            }
            case EMBER -> {
                Ember e = EmberforgeGear.motifOf(stack);
                yield e == null ? null : e.group.name();
            }
            case THORN -> {
                Thorn t = ThornwoodGear.motifOf(stack);
                yield t == null ? null : t.group.name();
            }
            case ASHEN -> {
                Ashen a = AshencrownGear.motifOf(stack);
                yield a == null ? null : a.group.name();
            }
            case PRISM -> {
                Prism p = PrismaticGear.motifOf(stack);
                yield p == null ? null : p.group.name();
            }
        };
    }

    /** How many pieces of this line the player is wearing, for the set bonus. */
    private static int wornOfLine(ServerPlayer player, Line line) {
        int n = 0;
        for (ItemStack st : GearLine.carried(player)) {
            if (lineOf(st) == line) n++;
        }
        return n;
    }

    // ── The hit ───────────────────────────────────────────────────────────

    /**
     * Fills the meter, and spends it when it is full.
     *
     * <p>Returns nothing because the spend is an effect rather than a damage change. The damage the
     * pieces add is already in their stat lines, which is where a number the player can read
     * belongs.</p>
     */
    public static void onHit(ServerPlayer player, LivingEntity target, float damage) {
        ItemStack held = player.getMainHandItem();
        Line line = lineOf(held);
        if (line == null) return;

        UUID id = player.getUUID();
        long now = PoliticalServer.server == null ? 0L : PoliticalServer.server.getTickCount();

        // Three of a house fills a quarter faster, which is the set bonus every piece advertises.
        int gain = wornOfLine(player, line) >= 3 ? 5 : 4;
        int charge = CHARGE.merge(id, gain, Integer::sum);

        int need = FILL * 4;
        if (charge < need) {
            if (charge % 16 == 0) {
                player.sendSystemMessage(Component.literal(line.colour + line.meter
                        + " §7" + (charge * 100 / need) + "%"), true);
            }
            return;
        }

        // A short floor between spends, so a mob farm cannot turn this into a permanent effect.
        long last = LAST_SPEND.getOrDefault(id, -1000L);
        if (now - last < 60) return;

        CHARGE.put(id, 0);
        LAST_SPEND.put(id, now);
        spend(player, target, line, groupOf(held, line), damage);
    }

    // ── The spend ─────────────────────────────────────────────────────────

    private static void spend(ServerPlayer player, LivingEntity target, Line line,
                              String group, float damage) {
        if (!(player.level() instanceof ServerLevel level)) return;

        player.sendSystemMessage(Component.literal(line.colour + "✦ " + line.meter), true);
        level.playSound(null, player.blockPosition(), soundFor(line),
                SoundSource.PLAYERS, 0.7f, 1.2f);

        switch (line) {
            case TIDE -> tide(player, target, level, group, damage);
            case EMBER -> ember(player, target, level, group, damage);
            case THORN -> thorn(player, target, level, group, damage);
            case ASHEN -> ashen(player, target, level, group, damage);
            case PRISM -> prism(player, target, level, group, damage);
        }
    }

    private static net.minecraft.sounds.SoundEvent soundFor(Line line) {
        return switch (line) {
            case TIDE -> SoundEvents.CONDUIT_ACTIVATE;
            case EMBER -> SoundEvents.BLAZE_SHOOT;
            case THORN -> SoundEvents.AZALEA_BREAK;
            case ASHEN -> SoundEvents.WITHER_SPAWN;
            case PRISM -> SoundEvents.AMETHYST_BLOCK_CHIME;
        };
    }

    private static void tide(ServerPlayer p, LivingEntity t, ServerLevel w, String g, float dmg) {
        switch (g == null ? "" : g) {
            case "DEEP" -> {
                // Pressure. Everything near the target takes the hit again and slows.
                for (LivingEntity e : around(w, t, 5)) {
                    e.hurtServer(w, p.damageSources().magic(), dmg * 0.8f);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 2));
                }
            }
            case "SHOAL" -> {
                p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 120, 1));
                p.heal(p.getMaxHealth() * 0.15f);
            }
            case "SURGE" -> {
                for (LivingEntity e : around(w, p, 7)) {
                    e.hurtServer(w, p.damageSources().playerAttack(p), dmg * 0.6f);
                    e.setDeltaMovement(e.position().subtract(p.position()).normalize().scale(1.6));
                }
            }
            case "GLACIER" -> {
                for (LivingEntity e : around(w, t, 5)) {
                    e.setTicksFrozen(Math.min(e.getTicksFrozen() + 200, 400));
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 140, 4));
                }
            }
            default -> {
                // Myth. The whole thing at once.
                t.hurtServer(w, p.damageSources().magic(), dmg * 2.5f);
                for (LivingEntity e : around(w, t, 6)) {
                    e.hurtServer(w, p.damageSources().magic(), dmg);
                }
                p.heal(p.getMaxHealth() * 0.2f);
            }
        }
    }

    private static void ember(ServerPlayer p, LivingEntity t, ServerLevel w, String g, float dmg) {
        switch (g == null ? "" : g) {
            case "FORGE" -> {
                t.hurtServer(w, p.damageSources().playerAttack(p), dmg * 1.5f);
                p.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 200, 1));
            }
            case "MOLTEN" -> {
                for (LivingEntity e : around(w, t, 5)) {
                    e.igniteForSeconds(8);
                    e.hurtServer(w, p.damageSources().onFire(), dmg * 0.7f);
                }
            }
            case "PHOENIX" -> {
                p.heal(p.getMaxHealth() * 0.25f);
                p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 300, 0));
                p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 1));
            }
            case "EMBERKING" -> {
                for (LivingEntity e : around(w, p, 8)) {
                    e.igniteForSeconds(12);
                    e.hurtServer(w, p.damageSources().magic(), dmg * 1.2f);
                }
            }
            default -> {
                // Starforge. One blow worth the fight.
                t.igniteForSeconds(15);
                t.hurtServer(w, p.damageSources().magic(), dmg * 3.0f);
            }
        }
    }

    private static void thorn(ServerPlayer p, LivingEntity t, ServerLevel w, String g, float dmg) {
        switch (g == null ? "" : g) {
            case "GROVE" -> {
                for (LivingEntity e : around(w, t, 5)) {
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 160, 5));
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 1));
                }
            }
            case "THICKET" -> {
                for (LivingEntity e : around(w, t, 5)) {
                    e.addEffect(new MobEffectInstance(MobEffects.POISON, 160, 2));
                    e.hurtServer(w, p.damageSources().magic(), dmg * 0.5f);
                }
            }
            case "BLOOM" -> {
                p.heal(p.getMaxHealth() * 0.2f);
                p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
                p.addEffect(new MobEffectInstance(MobEffects.SPEED, 200, 1));
            }
            case "ANCIENT" -> {
                p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 160, 2));
                p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, 3));
            }
            default -> {
                // Wildwood. Everything at once, and it holds.
                for (LivingEntity e : around(w, p, 8)) {
                    e.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 3));
                    e.hurtServer(w, p.damageSources().magic(), dmg * 1.2f);
                }
                p.heal(p.getMaxHealth() * 0.25f);
            }
        }
    }

    private static void ashen(ServerPlayer p, LivingEntity t, ServerLevel w, String g, float dmg) {
        switch (g == null ? "" : g) {
            case "COURT" -> {
                t.addEffect(new MobEffectInstance(MobEffects.WITHER, 140, 2));
                CoinManager.giveCoinsQuiet(p, 250);
            }
            case "BARROW" -> {
                for (LivingEntity e : around(w, t, 5)) {
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 120, 1));
                    e.hurtServer(w, p.damageSources().magic(), dmg * 0.6f);
                }
                p.heal(p.getMaxHealth() * 0.1f);
            }
            case "WIGHT" -> {
                t.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 2));
                t.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 2));
                p.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 200, 1));
            }
            case "SOVEREIGN" -> {
                // The decree. Anything already low simply stops.
                for (LivingEntity e : around(w, p, 8)) {
                    if (e.getHealth() <= e.getMaxHealth() * 0.25f) {
                        e.hurtServer(w, p.damageSources().magic(), e.getHealth() + 1f);
                    } else {
                        e.hurtServer(w, p.damageSources().magic(), dmg * 1.4f);
                    }
                }
            }
            default -> {
                // Rite. It empties the field.
                for (LivingEntity e : around(w, p, 9)) {
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 3));
                    e.hurtServer(w, p.damageSources().magic(), dmg * 1.5f);
                }
            }
        }
    }

    private static void prism(ServerPlayer p, LivingEntity t, ServerLevel w, String g, float dmg) {
        switch (g == null ? "" : g) {
            case "SPECTRUM" -> {
                int hit = 0;
                for (LivingEntity e : around(w, p, 10)) {
                    if (hit++ >= 7) break;
                    e.hurtServer(w, p.damageSources().magic(), dmg * 0.9f);
                }
            }
            case "SHADOW" -> {
                p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 120, 0));
                p.addEffect(new MobEffectInstance(MobEffects.SPEED, 120, 2));
                t.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 120, 0));
            }
            case "MIRROR" -> {
                t.hurtServer(w, p.damageSources().magic(), dmg);
                t.hurtServer(w, p.damageSources().magic(), dmg);
            }
            case "RADIANT" -> {
                for (LivingEntity e : around(w, p, 10)) {
                    e.hurtServer(w, p.damageSources().magic(), dmg * 1.1f);
                    e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
                }
                p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 1));
            }
            default -> {
                // Unseen. One hit nothing reduces.
                t.hurtServer(w, p.damageSources().magic(), dmg * 3.5f);
            }
        }
    }

    private static java.util.List<LivingEntity> around(ServerLevel w, LivingEntity centre, double r) {
        return w.getEntitiesOfClass(LivingEntity.class,
                centre.getBoundingBox().inflate(r),
                e -> e.isAlive() && !(e instanceof ServerPlayer));
    }
}
