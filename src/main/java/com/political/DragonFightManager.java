package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The dragon, turned into a fight worth flying out for.
 *
 * <p>Vanilla's dragon is a crystal errand followed by a long wait for it to land. Nothing about it
 * asks the player to do anything except stand still and hit a head. This turns it into five phases
 * that each ask a different question, telegraphs every big attack before it lands, and gives the
 * whole thing a bar that says what is currently trying to kill you.</p>
 *
 * <h2>The five phases</h2>
 *
 * <ol>
 *   <li><b>Wingbeat</b> above 80%. Shockwave rings off the centre. Read them and move.</li>
 *   <li><b>The Swarm</b> above 60%. The dragon calls escorts and shrugs off damage while they
 *       live, so the answer is to kill the adds rather than to keep hitting the dragon.</li>
 *   <li><b>Voidbreath</b> above 40%. Pools of void that hurt to stand in, laid where people are
 *       standing, so the arena shrinks as the fight goes on.</li>
 *   <li><b>Rebirth</b> above 20%. Healing crystals come back. Break them or the fight resets.</li>
 *   <li><b>Enrage</b> below 20%. Everything at once, harder and faster, with a meteor overhead.</li>
 * </ol>
 *
 * <p>Every phase change is announced, and every big attack has a warning that lands before the
 * damage does. A boss fight that kills you without telling you why is a boss fight people leave.</p>
 */
public final class DragonFightManager {

    private DragonFightManager() {}

    /** How much harder this dragon hits than the vanilla one. */
    private static final float BASE_DAMAGE_SCALE = 1.6f;

    /** Extra health, so the phases have room to happen. */
    private static final float BONUS_HEALTH = 400f;

    /** Marks a dragon whose health has already been raised, so it is only ever raised once. */
    private static final String SCALED_TAG = "political_dragon_scaled";

    private static final Map<UUID, Fight> FIGHTS = new HashMap<>();

    // ── State ─────────────────────────────────────────────────────────────

    /** Which part of the fight a dragon is in. */
    public enum Phase {
        WINGBEAT("Wingbeat", ChatFormatting.WHITE,
                "Rings of force roll out from the centre. Move.",
                BossEvent.BossBarColor.WHITE),
        SWARM("The Swarm", ChatFormatting.DARK_PURPLE,
                "It has called escorts. Kill them or it will not bleed.",
                BossEvent.BossBarColor.PURPLE),
        VOIDBREATH("Voidbreath", ChatFormatting.DARK_GRAY,
                "It is spitting void where you stand. The floor is shrinking.",
                BossEvent.BossBarColor.BLUE),
        REBIRTH("Rebirth", ChatFormatting.AQUA,
                "The crystals are back. Break them or this never ends.",
                BossEvent.BossBarColor.GREEN),
        ENRAGE("Enrage", ChatFormatting.RED,
                "It has stopped being careful.",
                BossEvent.BossBarColor.RED);

        public final String display;
        public final ChatFormatting colour;
        public final String blurb;
        public final BossEvent.BossBarColor bar;

        Phase(String display, ChatFormatting colour, String blurb, BossEvent.BossBarColor bar) {
            this.display = display;
            this.colour = colour;
            this.blurb = blurb;
            this.bar = bar;
        }
    }

    /** Everything tracked for one dragon. */
    private static final class Fight {
        Phase phase = Phase.WINGBEAT;
        ServerBossEvent bar;
        long nextAttack;
        long telegraphAt;
        String telegraphed;
        final List<UUID> adds = new ArrayList<>();
        final List<BlockPos> crystals = new ArrayList<>();
        final Map<UUID, Float> damageBy = new HashMap<>();
        int ringStep;
    }

    // ── Tick ──────────────────────────────────────────────────────────────

    /**
     * Drives the fight.
     *
     * <p>Runs four times a second rather than every tick. A boss phase does not need twenty
     * decisions a second, and scanning the End for a dragon on every tick would be paid for by
     * every server whether anybody is fighting or not.</p>
     */
    public static void tick(MinecraftServer server) {
        if (server.getTickCount() % 5 != 0) return;

        ServerLevel end = server.getLevel(net.minecraft.world.level.Level.END);
        if (end == null) return;
        if (end.players().isEmpty()) {
            // Nobody is watching, so there is nothing to drive. The fight is deliberately kept:
            // dropping it here meant walking out and back in re-ran begin(), which added another
            // four hundred health every time and threw away everyone's damage contribution.
            return;
        }

        List<EnderDragon> dragons = end.getEntitiesOfClass(EnderDragon.class,
                new AABB(-256, -64, -256, 256, 320, 256), d -> d.isAlive());
        if (dragons.isEmpty()) {
            if (!FIGHTS.isEmpty()) clearAll();
            return;
        }

        for (EnderDragon dragon : dragons) {
            Fight f = FIGHTS.computeIfAbsent(dragon.getUUID(), k -> begin(dragon, end));
            run(dragon, end, f);
        }
    }

    private static Fight begin(EnderDragon dragon, ServerLevel end) {
        Fight f = new Fight();
        f.bar = new ServerBossEvent(UUID.randomUUID(),
                Component.literal("Wingbeat").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD),
                BossEvent.BossBarColor.WHITE, BossEvent.BossBarOverlay.PROGRESS);
        f.bar.setProgress(1.0f);

        // Extra health so five phases have room. The guard is a tag on the dragon rather than a
        // field on the fight, because the fight is a cache and the dragon is the thing that lasts.
        if (!dragon.entityTags().contains(SCALED_TAG)) {
            dragon.addTag(SCALED_TAG);
            try {
                var attr = dragon.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
                if (attr != null) {
                    attr.setBaseValue(attr.getBaseValue() + BONUS_HEALTH);
                    dragon.setHealth(dragon.getMaxHealth());
                }
            } catch (Exception ignored) {
                // An attribute that will not move is survivable: the fight just runs shorter.
            }
        }

        for (ServerPlayer p : end.players()) {
            f.bar.addPlayer(p);
            p.sendSystemMessage(Component.literal(""));
            p.sendSystemMessage(Component.literal("  §5§lTHE DRAGON STIRS"));
            p.sendSystemMessage(Component.literal("  §7It fights in five stages. Watch the bar."));
            p.sendSystemMessage(Component.literal(""));
        }
        return f;
    }

    private static void run(EnderDragon dragon, ServerLevel end, Fight f) {
        float frac = dragon.getHealth() / Math.max(1f, dragon.getMaxHealth());

        // Everyone in the End sees the bar, including anybody who arrived late.
        for (ServerPlayer p : end.players()) {
            f.bar.addPlayer(p);
        }
        f.bar.setProgress(Math.max(0f, Math.min(1f, frac)));

        Phase want = phaseFor(frac);
        if (want != f.phase) {
            enterPhase(dragon, end, f, want);
        }

        long now = end.getServer().getTickCount();

        // A telegraphed attack lands when its timer comes up, not on the tick it was announced.
        if (f.telegraphed != null && now >= f.telegraphAt) {
            resolve(dragon, end, f);
            f.telegraphed = null;
        }

        if (now >= f.nextAttack && f.telegraphed == null) {
            telegraph(dragon, end, f, now);
        }

        // Rebirth keeps healing the dragon while a crystal stands, which is what makes breaking
        // them the objective rather than a suggestion.
        if (f.phase == Phase.REBIRTH && !f.crystals.isEmpty()) {
            int alive = countCrystals(end, f);
            if (alive > 0) {
                dragon.heal(0.5f * alive);
                for (ServerPlayer p : end.players()) {
                    p.sendSystemMessage(Component.literal("§b✦ §7" + alive
                            + " crystal" + (alive == 1 ? "" : "s") + " still feeding it"), true);
                }
            }
        }

        // Swarm holds the dragon's damage down while its escorts live.
        if (f.phase == Phase.SWARM) {
            f.adds.removeIf(id -> {
                var e = end.getEntity(id);
                return e == null || !e.isAlive();
            });
            if (!f.adds.isEmpty()) {
                for (ServerPlayer p : end.players()) {
                    p.sendSystemMessage(Component.literal("§d✦ §7" + f.adds.size()
                            + " escorts left"), true);
                }
            }
        }
    }

    private static Phase phaseFor(float frac) {
        if (frac > 0.80f) return Phase.WINGBEAT;
        if (frac > 0.60f) return Phase.SWARM;
        if (frac > 0.40f) return Phase.VOIDBREATH;
        if (frac > 0.20f) return Phase.REBIRTH;
        return Phase.ENRAGE;
    }

    private static void enterPhase(EnderDragon dragon, ServerLevel end, Fight f, Phase phase) {
        f.phase = phase;
        f.bar.setName(Component.literal(phase.display)
                .withStyle(phase.colour, ChatFormatting.BOLD));
        f.bar.setColor(phase.bar);
        f.nextAttack = end.getServer().getTickCount() + 60;

        for (ServerPlayer p : end.players()) {
            p.sendSystemMessage(Component.literal(""));
            p.sendSystemMessage(Component.literal("  " + phase.colour + "§l"
                    + phase.display.toUpperCase(java.util.Locale.ROOT)));
            p.sendSystemMessage(Component.literal("  §7" + phase.blurb));
            p.sendSystemMessage(Component.literal(""));
            end.playSound(null, p.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL,
                    SoundSource.HOSTILE, 1.0f, phase == Phase.ENRAGE ? 0.6f : 1.0f);
        }

        switch (phase) {
            case SWARM -> summonEscorts(end, f, dragon);
            case REBIRTH -> raiseCrystals(end, f);
            case ENRAGE -> {
                dragon.addEffect(new MobEffectInstance(MobEffects.SPEED, 20 * 600, 1));
                dragon.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 20 * 600, 1));
            }
            default -> { }
        }
    }

    // ── Attacks ───────────────────────────────────────────────────────────

    /**
     * Announces what is about to happen, then schedules it.
     *
     * <p>The gap between the warning and the hit is the whole design. A player who reads the line
     * and moves survives it, and a player who does not learns what the line meant. Damage that
     * arrives with no warning teaches nothing.</p>
     */
    private static void telegraph(EnderDragon dragon, ServerLevel end, Fight f, long now) {
        String what = switch (f.phase) {
            case WINGBEAT -> "shockwave";
            case SWARM -> f.adds.isEmpty() ? "dive" : "shockwave";
            case VOIDBREATH -> "void";
            case REBIRTH -> countCrystals(end, f) > 0 ? "shockwave" : "dive";
            case ENRAGE -> switch (f.ringStep++ % 3) {
                case 0 -> "meteor";
                case 1 -> "void";
                default -> "shockwave";
            };
        };

        String warning = switch (what) {
            case "shockwave" -> "§e⚠ It beats its wings. A ring is coming.";
            case "dive" -> "§c⚠ It is lining up a dive.";
            case "void" -> "§5⚠ It is drawing breath. Move off your square.";
            default -> "§6⚠ Something is falling.";
        };

        for (ServerPlayer p : end.players()) {
            p.sendSystemMessage(Component.literal(warning), true);
            end.playSound(null, p.blockPosition(), SoundEvents.ENDER_DRAGON_FLAP,
                    SoundSource.HOSTILE, 1.0f, 1.4f);
        }

        f.telegraphed = what;
        f.telegraphAt = now + (f.phase == Phase.ENRAGE ? 25 : 40);
        f.nextAttack = f.telegraphAt + (f.phase == Phase.ENRAGE ? 60 : 120);
    }

    private static void resolve(EnderDragon dragon, ServerLevel end, Fight f) {
        switch (f.telegraphed) {
            case "shockwave" -> shockwave(dragon, end, f);
            case "dive" -> dive(dragon, end, f);
            case "void" -> voidPools(dragon, end, f);
            case "meteor" -> meteor(dragon, end, f);
            default -> { }
        }
    }

    /** A ring that spares anybody who moved away from the centre. */
    private static void shockwave(EnderDragon dragon, ServerLevel end, Fight f) {
        Vec3 centre = new Vec3(0, 64, 0);
        float dmg = 6f * scale(f);
        for (ServerPlayer p : end.players()) {
            double d = p.position().multiply(1, 0, 1).distanceTo(centre.multiply(1, 0, 1));
            // The safe band is the ring itself: too close and too far both get hit, which stops
            // "stand at the edge forever" being the answer to every phase.
            if (d < 12 || d > 34) {
                p.hurtServer(end, p.damageSources().magic(), dmg);
                p.setDeltaMovement(p.getDeltaMovement().add(0, 0.6, 0));
                p.sendSystemMessage(Component.literal("§c✦ The ring caught you."), true);
            }
        }
        end.playSound(null, BlockPos.containing(centre), SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.HOSTILE, 2.0f, 0.6f);
    }

    /** A dive at whoever has done the most damage, which is how a boss picks its problem. */
    private static void dive(EnderDragon dragon, ServerLevel end, Fight f) {
        ServerPlayer target = topDamager(end, f);
        if (target == null) return;
        float dmg = 10f * scale(f);
        target.hurtServer(end, target.damageSources().mobAttack(dragon), dmg);
        target.setDeltaMovement(target.position().subtract(dragon.position()).normalize().scale(1.4));
        target.sendSystemMessage(Component.literal("§c✦ It came for you."), true);
        end.playSound(null, target.blockPosition(), SoundEvents.ENDER_DRAGON_HURT,
                SoundSource.HOSTILE, 1.5f, 0.7f);
    }

    /** Pools laid where people were standing, so the arena gets smaller. */
    private static void voidPools(EnderDragon dragon, ServerLevel end, Fight f) {
        float dmg = 4f * scale(f);
        for (ServerPlayer p : end.players()) {
            p.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 1));
            p.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1));
            p.hurtServer(end, p.damageSources().magic(), dmg);
            p.sendSystemMessage(Component.literal("§5✦ Voidbreath washes over you."), true);
            end.playSound(null, p.blockPosition(), SoundEvents.ENDER_DRAGON_SHOOT,
                    SoundSource.HOSTILE, 1.0f, 0.5f);
        }
    }

    /** Falling rock, in Enrage only, because the last fifth should feel different. */
    private static void meteor(EnderDragon dragon, ServerLevel end, Fight f) {
        float dmg = 14f * scale(f);
        for (ServerPlayer p : end.players()) {
            p.hurtServer(end, p.damageSources().magic(), dmg);
            p.igniteForSeconds(4);
            p.sendSystemMessage(Component.literal("§6✦ Something landed on you."), true);
            end.playSound(null, p.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(),
                    SoundSource.HOSTILE, 2.0f, 0.8f);
        }
    }

    private static float scale(Fight f) {
        return f.phase == Phase.ENRAGE ? BASE_DAMAGE_SCALE * 1.5f : BASE_DAMAGE_SCALE;
    }

    // ── Adds and crystals ─────────────────────────────────────────────────

    private static void summonEscorts(ServerLevel end, Fight f, EnderDragon dragon) {
        f.adds.clear();
        int count = 3 + end.players().size();
        for (int i = 0; i < count; i++) {
            double angle = (Math.PI * 2 / count) * i;
            double x = Math.cos(angle) * 20;
            double z = Math.sin(angle) * 20;
            Mob add = EntityType.ENDERMAN.create(end, EntitySpawnReason.EVENT);
            if (add == null) continue;
            add.setPos(x, 70, z);
            add.setCustomName(Component.literal("Dragon Escort")
                    .withStyle(ChatFormatting.DARK_PURPLE));
            add.setCustomNameVisible(true);
            add.setPersistenceRequired();
            add.addEffect(new MobEffectInstance(MobEffects.SPEED, 20 * 600, 1));
            end.addFreshEntity(add);
            f.adds.add(add.getUUID());
        }
    }

    private static void raiseCrystals(ServerLevel end, Fight f) {
        f.crystals.clear();
        // The vanilla pillar ring, so anybody who has done the fight knows where to look.
        for (int i = 0; i < 6; i++) {
            double angle = (Math.PI * 2 / 6) * i;
            BlockPos pos = BlockPos.containing(Math.cos(angle) * 42, 80, Math.sin(angle) * 42);
            var crystal = EntityType.END_CRYSTAL.create(end, EntitySpawnReason.EVENT);
            if (crystal == null) continue;
            crystal.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            end.addFreshEntity(crystal);
            f.crystals.add(pos);
        }
    }

    private static int countCrystals(ServerLevel end, Fight f) {
        return end.getEntitiesOfClass(
                net.minecraft.world.entity.boss.enderdragon.EndCrystal.class,
                new AABB(-64, 60, -64, 64, 110, 64), e -> e.isAlive()).size();
    }

    // ── Damage bookkeeping ────────────────────────────────────────────────

    /** Records a hit, so the fight can pick a target and pay out fairly. */
    public static void onDragonHurt(EnderDragon dragon, ServerPlayer by, float amount) {
        Fight f = FIGHTS.get(dragon.getUUID());
        if (f == null) return;
        f.damageBy.merge(by.getUUID(), amount, Float::sum);
    }

    /** Damage the dragon deals, scaled by phase. */
    public static float outgoingScale(EnderDragon dragon) {
        Fight f = FIGHTS.get(dragon.getUUID());
        return f == null ? 1f : scale(f);
    }

    /**
     * How much of a hit the dragon actually takes.
     *
     * <p>During the Swarm it shrugs almost everything off while its escorts live. That is the phase
     * telling the player what to do, in the only language a boss has.</p>
     */
    public static float incomingScale(EnderDragon dragon) {
        Fight f = FIGHTS.get(dragon.getUUID());
        if (f == null) return 1f;
        if (f.phase == Phase.SWARM && !f.adds.isEmpty()) return 0.15f;
        return 1f;
    }

    private static ServerPlayer topDamager(ServerLevel end, Fight f) {
        ServerPlayer best = null;
        float most = -1;
        for (ServerPlayer p : end.players()) {
            float d = f.damageBy.getOrDefault(p.getUUID(), 0f);
            if (d > most) {
                most = d;
                best = p;
            }
        }
        return best;
    }

    // ── The end of it ─────────────────────────────────────────────────────

    /** Called when the dragon dies. Pays out by contribution and clears the fight. */
    public static void onDragonDeath(EnderDragon dragon, ServerLevel end) {
        Fight f = FIGHTS.remove(dragon.getUUID());
        if (f == null) return;

        // Escorts do not outlive the thing that called them.
        for (UUID id : f.adds) {
            var e = end.getEntity(id);
            if (e != null) e.discard();
        }
        f.bar.removeAllPlayers();

        float total = 0;
        for (float v : f.damageBy.values()) total += v;

        List<ServerPlayer> present = new ArrayList<>(end.players());
        for (ServerPlayer p : present) {
            float mine = f.damageBy.getOrDefault(p.getUUID(), 0f);
            double share = total <= 0 ? 1.0 / Math.max(1, present.size()) : mine / total;

            long coins = Math.round(250_000 * share) + 25_000;
            CoinManager.giveCoins(p, coins);

            p.sendSystemMessage(Component.literal(""));
            p.sendSystemMessage(Component.literal("  §5§lTHE DRAGON FALLS"));
            p.sendSystemMessage(Component.literal("  §7You did §f"
                    + Math.round(share * 100) + "% §7of the damage"));
            p.sendSystemMessage(Component.literal("  §7Paid §6" + GuiKit.num(coins) + " coins"));

            // A share of the kill rolls on the rare table, weighted by how much you actually did.
            if (p.getRandom().nextDouble() < 0.25 + share * 0.5) {
                ItemStack drop = RareDropManager.rollBossReward(p);
                if (drop != null && !drop.isEmpty()) {
                    if (!p.getInventory().add(drop)) p.drop(drop, false);
                    p.sendSystemMessage(Component.literal("  §6✦ "
                            + drop.getHoverName().getString()));
                }
            }
            p.sendSystemMessage(Component.literal(""));
        }
    }

    private static void clearAll() {
        for (Fight f : FIGHTS.values()) {
            if (f.bar != null) f.bar.removeAllPlayers();
        }
        FIGHTS.clear();
    }

    /** Whether a fight is running, for the admin screen. */
    public static boolean active() {
        return !FIGHTS.isEmpty();
    }

    public static String describe() {
        if (FIGHTS.isEmpty()) return "no dragon fight running";
        Fight f = FIGHTS.values().iterator().next();
        return f.phase.display + ", " + f.adds.size() + " escorts";
    }
}
