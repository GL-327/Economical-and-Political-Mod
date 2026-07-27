package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Runs Tempest.
 *
 * <p>Charges are held on the target rather than on the player, which is what makes this line
 * cooperative. Anyone running Tempest contributes to the same set of charges, so two players on
 * different fronts trigger reactions neither one could produce alone. Charges expire after six
 * seconds so nothing can be stockpiled, and a reaction consumes both so the pair has to be rebuilt.</p>
 */
public final class TempestHandler {

    private TempestHandler() {}

    private static final long CHARGE_MS = 6000L;
    private static final Map<UUID, EnumMap<Storm.Charge, Long>> CHARGES = new HashMap<>();

    private static Storm held(ServerPlayer p) {
        Storm s = TempestGear.stormOf(p.getMainHandItem());
        if (s != null) return s;
        for (ItemStack st : GearLine.carried(p)) {
            Storm c = TempestGear.stormOf(st);
            if (c != null) return c;
        }
        return null;
    }

    private static int frontCount(ServerPlayer p, Storm.Front f) {
        int n = 0;
        for (ItemStack st : GearLine.carried(p)) {
            Storm s = TempestGear.stormOf(st);
            if (s != null && s.front == f) n++;
        }
        return n;
    }

    /** Called for every hit a player lands. */
    public static void onHit(ServerPlayer player, LivingEntity target, float damage) {
        Storm storm = held(player);
        if (storm == null) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        long life = CHARGE_MS;
        if (frontCount(player, Storm.Front.THERMAL) >= 3) life *= 2;

        apply(level, player, target, storm.charge, life);

        // Electric front spreads the charge to a second target on its own.
        if (frontCount(player, Storm.Front.ELECTRIC) >= 3) {
            for (LivingEntity e : near(level, player, target.position(), 5.0)) {
                if (e == target) continue;
                Vfx.arc(level, ParticleTypes.ELECTRIC_SPARK, target.position().add(0, 1, 0),
                        e.position().add(0, 1, 0), 0.3, 8);
                apply(level, player, e, storm.charge, life);
                break;
            }
        }
        if (frontCount(player, Storm.Front.POLAR) >= 3) {
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1, true, false, false));
        }
    }

    private static void apply(ServerLevel level, ServerPlayer player, LivingEntity target,
                              Storm.Charge charge, long life) {
        UUID id = target.getUUID();
        EnumMap<Storm.Charge, Long> held = CHARGES.computeIfAbsent(id,
                k -> new EnumMap<>(Storm.Charge.class));

        long now = System.currentTimeMillis();
        held.entrySet().removeIf(e -> e.getValue() < now);

        // A different charge already sitting on the target reacts instead of stacking.
        Storm.Charge partner = null;
        for (Storm.Charge other : held.keySet()) {
            if (other != charge) {
                partner = other;
                break;
            }
        }
        if (partner != null) {
            held.remove(partner);
            held.remove(charge);
            react(level, player, target, partner, charge);
            return;
        }

        held.put(charge, now + life);
        Vec3 at = target.position().add(0, 1, 0);
        Vfx.ring(level, particleFor(charge), at, 0.9, 12);
        player.sendSystemMessage(Component.literal("§8" + charge.colour + charge.label
                + " §8hangs on the target."), true);
    }

    private static net.minecraft.core.particles.ParticleOptions particleFor(Storm.Charge c) {
        return switch (c) {
            case SCALD -> ParticleTypes.FLAME;
            case FROST -> ParticleTypes.SNOWFLAKE;
            case SHOCK -> ParticleTypes.ELECTRIC_SPARK;
            case GALE -> ParticleTypes.CLOUD;
            case DUST -> ParticleTypes.ASH;
            case BRINE -> ParticleTypes.BUBBLE;
            case EMBER -> ParticleTypes.LAVA;
            case GLOOM -> ParticleTypes.SCULK_SOUL;
        };
    }

    /** Two different charges on one target. Twenty eight pairs, twenty eight results. */
    private static void react(ServerLevel level, ServerPlayer player, LivingEntity target,
                              Storm.Charge a, Storm.Charge b) {
        Storm.Charge lo = a.ordinal() < b.ordinal() ? a : b;
        Storm.Charge hi = a.ordinal() < b.ordinal() ? b : a;
        Vec3 at = target.position().add(0, 1, 0);
        Vec3 me = player.position().add(0, 1, 0);
        double spread = frontCount(player, Storm.Front.AEOLIAN) >= 3 ? 1.6 : 1.0;
        boolean telluric = frontCount(player, Storm.Front.TELLURIC) >= 3;

        String name;
        switch (lo) {
            case SCALD -> {
                switch (hi) {
                    case FROST -> {
                        name = "Shatter";
                        VfxPlus.frostNova(level, at);
                        hit(level, player, at, 3.0 * spread, 55);
                        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 2));
                    }
                    case SHOCK -> {
                        name = "Plasma";
                        List<Vec3> chain = new ArrayList<>();
                        for (LivingEntity e : near(level, player, at, 8.0 * spread)) {
                            chain.add(e.position().add(0, 1, 0));
                        }
                        VfxPlus.chainLightning(level, me, chain);
                        hit(level, player, at, 8.0 * spread, 40);
                    }
                    case GALE -> {
                        name = "Firestorm";
                        VfxPlus.flamePillar(level, at);
                        for (LivingEntity e : near(level, player, at, 6.0 * spread)) {
                            e.igniteForSeconds(10);
                        }
                        hit(level, player, at, 6.0 * spread, 32);
                    }
                    case DUST -> {
                        name = "Glassing";
                        Vfx.ring(level, ParticleTypes.FLAME, at, 3.0, 40);
                        hit(level, player, at, 4.0 * spread, 30);
                        for (LivingEntity e : near(level, player, at, 4.0 * spread)) {
                            e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 3));
                        }
                        if (telluric) glassFloor(level, target.blockPosition(), 3);
                    }
                    case BRINE -> {
                        name = "Steam Blast";
                        Vfx.puff(level, ParticleTypes.CLOUD, at, 80, 2.0, 0.1);
                        for (LivingEntity e : near(level, player, at, 5.0 * spread)) {
                            e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 120, 0));
                            Vec3 push = e.position().subtract(at).normalize().scale(1.2);
                            e.setDeltaMovement(push.x, 0.5, push.z);
                            e.hurtMarked = true;
                        }
                        hit(level, player, at, 5.0 * spread, 28);
                    }
                    case EMBER -> {
                        name = "Conflagration";
                        VfxPlus.flamePillar(level, at);
                        for (LivingEntity e : near(level, player, at, 5.0 * spread)) {
                            e.igniteForSeconds(20);
                        }
                        hit(level, player, at, 5.0 * spread, 24);
                    }
                    default -> {
                        name = "Witchfire";
                        VfxPlus.soulHarvest(level, at, me);
                        hit(level, player, at, 4.0 * spread, 45);
                        player.heal(10f);
                    }
                }
            }
            case FROST -> {
                switch (hi) {
                    case SHOCK -> {
                        name = "Superconduct";
                        VfxPlus.chainLightning(level, me, List.of(at));
                        hit(level, player, at, 5.0 * spread, 38);
                        for (LivingEntity e : near(level, player, at, 5.0 * spread)) {
                            e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 300, 4));
                        }
                    }
                    case GALE -> {
                        name = "Whiteout";
                        Vfx.puff(level, ParticleTypes.SNOWFLAKE, at, 100, 3.0, 0.05);
                        for (LivingEntity e : near(level, player, at, 8.0 * spread)) {
                            e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 160, 0));
                            e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 3));
                        }
                        hit(level, player, at, 8.0 * spread, 18);
                    }
                    case DUST -> {
                        name = "Rime";
                        Vfx.shell(level, ParticleTypes.SNOWFLAKE, at, 2.0, 40);
                        for (LivingEntity e : near(level, player, at, 4.0 * spread)) {
                            e.setTicksFrozen(400);
                            e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 6));
                        }
                        hit(level, player, at, 4.0 * spread, 26);
                    }
                    case BRINE -> {
                        name = "Deep Freeze";
                        VfxPlus.frostNova(level, at);
                        for (LivingEntity e : near(level, player, at, 6.0 * spread)) {
                            e.setTicksFrozen(600);
                            e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 300, 7));
                        }
                        hit(level, player, at, 6.0 * spread, 44);
                    }
                    case EMBER -> {
                        name = "Thermal Shock";
                        VfxPlus.executionFlash(level, at, Vfx.FROST);
                        hit(level, player, at, 3.0 * spread, 60);
                    }
                    default -> {
                        name = "Coldgrave";
                        Vfx.puff(level, ParticleTypes.SOUL, at, 50, 1.5, 0.05);
                        for (LivingEntity e : near(level, player, at, 5.0 * spread)) {
                            e.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 1));
                            e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 4));
                        }
                        hit(level, player, at, 5.0 * spread, 30);
                    }
                }
            }
            case SHOCK -> {
                switch (hi) {
                    case GALE -> {
                        name = "Thunderclap";
                        Vfx.stormStrike(level, at);
                        for (LivingEntity e : near(level, player, at, 8.0 * spread)) {
                            Vec3 push = e.position().subtract(at).normalize().scale(1.6);
                            e.setDeltaMovement(push.x, 0.8, push.z);
                            e.hurtMarked = true;
                            e.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 140, 0));
                        }
                        hit(level, player, at, 8.0 * spread, 34);
                    }
                    case DUST -> {
                        name = "Static Storm";
                        Vfx.ring(level, ParticleTypes.ELECTRIC_SPARK, at, 4.0, 50);
                        for (LivingEntity e : near(level, player, at, 7.0 * spread)) {
                            e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 140, 0));
                        }
                        hit(level, player, at, 7.0 * spread, 26);
                    }
                    case BRINE -> {
                        name = "Conduction";
                        List<Vec3> pts = new ArrayList<>();
                        for (LivingEntity e : near(level, player, at, 10.0 * spread)) {
                            pts.add(e.position().add(0, 1, 0));
                        }
                        VfxPlus.chainLightning(level, me, pts);
                        hit(level, player, at, 10.0 * spread, 46);
                    }
                    case EMBER -> {
                        name = "Skyfire";
                        VfxPlus.orbitalStrike(level, at, Vfx.STORM);
                        hit(level, player, at, 4.0 * spread, 62);
                    }
                    default -> {
                        name = "Nerve Burn";
                        Vfx.puff(level, ParticleTypes.SCULK_SOUL, at, 40, 1.2, 0.05);
                        for (LivingEntity e : near(level, player, at, 5.0 * spread)) {
                            e.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0));
                            e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 2));
                        }
                        hit(level, player, at, 5.0 * spread, 32);
                    }
                }
            }
            case GALE -> {
                switch (hi) {
                    case DUST -> {
                        name = "Sandblast";
                        VfxPlus.shockCone(level, me, player.getLookAngle(), Vfx.STORM);
                        for (LivingEntity e : near(level, player, at, 7.0 * spread)) {
                            e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 160, 0));
                        }
                        hit(level, player, at, 7.0 * spread, 30);
                    }
                    case BRINE -> {
                        name = "Squall Line";
                        Vfx.ring(level, ParticleTypes.BUBBLE, at, 5.0, 60);
                        for (LivingEntity e : near(level, player, at, 8.0 * spread)) {
                            Vec3 push = e.position().subtract(me).normalize().scale(1.3);
                            e.setDeltaMovement(push.x, 0.4, push.z);
                            e.hurtMarked = true;
                            e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 160, 2));
                        }
                        hit(level, player, at, 8.0 * spread, 24);
                    }
                    case EMBER -> {
                        name = "Firewhirl";
                        VfxPlus.flamePillar(level, at);
                        Vfx.helix(level, ParticleTypes.FLAME, at, at.add(0, 4, 0), 2.0, 3.0, 40, 0.0);
                        for (LivingEntity e : near(level, player, at, 6.0 * spread)) {
                            e.igniteForSeconds(12);
                            Vec3 pull = at.subtract(e.position()).normalize().scale(0.6);
                            e.setDeltaMovement(pull);
                            e.hurtMarked = true;
                        }
                        hit(level, player, at, 6.0 * spread, 36);
                    }
                    default -> {
                        name = "Wraithwind";
                        VfxPlus.singularity(level, at, Vfx.VOID);
                        for (LivingEntity e : near(level, player, at, 9.0 * spread)) {
                            Vec3 pull = at.subtract(e.position()).normalize().scale(1.1);
                            e.setDeltaMovement(pull);
                            e.hurtMarked = true;
                        }
                        hit(level, player, at, 9.0 * spread, 28);
                    }
                }
            }
            case DUST -> {
                switch (hi) {
                    case BRINE -> {
                        name = "Silt";
                        VfxPlus.fissure(level, at, player.getLookAngle());
                        for (LivingEntity e : near(level, player, at, 6.0 * spread)) {
                            e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 300, 6));
                            e.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 300, 128));
                        }
                        hit(level, player, at, 6.0 * spread, 26);
                    }
                    case EMBER -> {
                        name = "Cinderstorm";
                        VfxPlus.cometVolley(level, at, 8);
                        for (LivingEntity e : near(level, player, at, 6.0 * spread)) {
                            e.igniteForSeconds(8);
                        }
                        hit(level, player, at, 6.0 * spread, 34);
                    }
                    default -> {
                        name = "Blacklung";
                        Vfx.puff(level, ParticleTypes.ASH, at, 80, 2.5, 0.03);
                        for (LivingEntity e : near(level, player, at, 6.0 * spread)) {
                            e.addEffect(new MobEffectInstance(MobEffects.POISON, 240, 2));
                            e.addEffect(new MobEffectInstance(MobEffects.WITHER, 160, 0));
                        }
                        hit(level, player, at, 6.0 * spread, 24);
                    }
                }
            }
            case BRINE -> {
                if (hi == Storm.Charge.EMBER) {
                    name = "Scaldwater";
                    Vfx.puff(level, ParticleTypes.CLOUD, at, 70, 2.0, 0.08);
                    for (LivingEntity e : near(level, player, at, 5.0 * spread)) {
                        e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 120, 0));
                        e.igniteForSeconds(6);
                    }
                    hit(level, player, at, 5.0 * spread, 38);
                } else {
                    name = "Drownlight";
                    VfxPlus.soulHarvest(level, at, me);
                    for (LivingEntity e : near(level, player, at, 6.0 * spread)) {
                        e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 240, 4));
                        e.setAirSupply(0);
                    }
                    hit(level, player, at, 6.0 * spread, 34);
                    player.heal(8f);
                }
            }
            default -> {
                name = "Soulflame";
                VfxPlus.executionFlash(level, at, Vfx.CURSED);
                for (LivingEntity e : near(level, player, at, 5.0 * spread)) {
                    e.igniteForSeconds(12);
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 1));
                }
                hit(level, player, at, 5.0 * spread, 58);
                player.heal(12f);
            }
        }

        if (telluric) scorchFloor(level, target.blockPosition(), 3);

        player.sendSystemMessage(Component.literal(lo.colour + lo.label + " §8+ " + hi.colour
                + hi.label + " §8= " + ChatFormatting.BOLD + name.toUpperCase(java.util.Locale.ROOT)), true);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private static List<LivingEntity> near(ServerLevel level, ServerPlayer player, Vec3 centre, double r) {
        List<LivingEntity> out = new ArrayList<>();
        for (Entity e : level.getEntities(player, new AABB(centre, centre).inflate(r))) {
            if (e instanceof LivingEntity le && le.isAlive()) out.add(le);
        }
        return out;
    }

    private static void hit(ServerLevel level, ServerPlayer player, Vec3 centre, double r, float amount) {
        for (LivingEntity e : near(level, player, centre, r)) {
            e.hurtServer(level, level.damageSources().playerAttack(player), amount);
        }
    }

    private static void glassFloor(ServerLevel level, BlockPos base, int r) {
        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                BlockPos p = base.offset(x, -1, z);
                if (level.getBlockState(p).is(Blocks.SAND)) {
                    level.setBlockAndUpdate(p, Blocks.GLASS.defaultBlockState());
                }
            }
        }
    }

    private static void scorchFloor(ServerLevel level, BlockPos base, int r) {
        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                BlockPos p = base.offset(x, 0, z);
                if (level.getBlockState(p).isAir()
                        && level.getBlockState(p.below()).isSolidRender()) {
                    level.setBlockAndUpdate(p, Blocks.COBWEB.defaultBlockState());
                }
            }
        }
    }

    /** Sweeps expired charges so the map cannot grow without bound. */
    public static void tickServer(net.minecraft.server.MinecraftServer server) {
        if (server.getTickCount() % 200 != 0) return;
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<UUID, EnumMap<Storm.Charge, Long>>> it = CHARGES.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, EnumMap<Storm.Charge, Long>> e = it.next();
            e.getValue().entrySet().removeIf(c -> c.getValue() < now);
            if (e.getValue().isEmpty()) it.remove();
        }
    }

    public static void clear(UUID id) {
        CHARGES.remove(id);
    }
}
