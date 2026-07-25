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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Runs Clockwork.
 *
 * <p>Pressure winds on its own while a piece is carried and is spent by breaking blocks. That makes
 * this the one line you can play entirely without fighting anything, and it is why the duties here
 * are mining conveniences rather than attacks. The Works you are wearing decides the ceiling, and
 * three of a Works lights that Works on top.</p>
 */
public final class ClockworkHandler {

    private ClockworkHandler() {}

    private static final Map<UUID, Integer> PRESSURE = new HashMap<>();
    private static final Map<UUID, Integer> BREAKS = new HashMap<>();
    private static final Map<UUID, BlockPos> ANCHOR = new HashMap<>();

    private static Mechanism held(ServerPlayer p) {
        Mechanism m = ClockworkGear.mechanismOf(p.getMainHandItem());
        if (m != null) return m;
        for (ItemStack st : GearLine.carried(p)) {
            Mechanism c = ClockworkGear.mechanismOf(st);
            if (c != null) return c;
        }
        return null;
    }

    private static int worksCount(ServerPlayer p, Mechanism.Works w) {
        int n = 0;
        for (ItemStack st : GearLine.carried(p)) {
            Mechanism m = ClockworkGear.mechanismOf(st);
            if (m != null && m.works == w) n++;
        }
        return n;
    }

    /** Ticked every tick, does work once a second. */
    public static void tickPlayer(ServerPlayer player) {
        if (player.tickCount % 20 != 0) return;
        Mechanism m = held(player);
        UUID id = player.getUUID();
        if (m == null) {
            PRESSURE.remove(id);
            ANCHOR.remove(id);
            return;
        }

        int cap = m.works.capacity;
        int gain = 2;
        if (m.duty == Mechanism.Duty.OVERPRESSURE) gain = 4;
        if (m.duty == Mechanism.Duty.FUEL && player.blockPosition().getY() < 40) gain *= 2;
        if (m.duty == Mechanism.Duty.MOMENTUM) gain += 1;
        if (worksCount(player, Mechanism.Works.ENGINE) >= 3) gain += 2;

        int cur = Math.min(cap, PRESSURE.getOrDefault(id, 0) + gain);
        PRESSURE.put(id, cur);

        // Standing conveniences that do not wait for a discharge.
        if (m.duty == Mechanism.Duty.HASTE && cur >= cap / 2) {
            player.addEffect(new MobEffectInstance(MobEffects.HASTE, 60, 1, true, false, false));
        }
        if (m.duty == Mechanism.Duty.SURVEY || worksCount(player, Mechanism.Works.OPTIC) >= 3) {
            surveyOre(player, 10);
        }
        if (m.duty == Mechanism.Duty.TEMPER && cur >= cap) {
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 60, 0, true, false, false));
        }
        if (m.duty == Mechanism.Duty.MAGNET || worksCount(player, Mechanism.Works.AUTOMATON) >= 3) {
            gatherDrops(player);
        }

        if (cur >= cap && player.tickCount % 100 == 0) {
            player.sendSystemMessage(Component.literal("§b⚙ Pressure §f" + cur + "§8/§f" + cap
                    + " §7ready"), true);
        }
    }

    /** Called after a player breaks a block. */
    public static void onBreak(ServerPlayer player, ServerLevel level, BlockPos pos, BlockState state) {
        Mechanism m = held(player);
        if (m == null) return;
        UUID id = player.getUUID();

        ANCHOR.putIfAbsent(id, player.blockPosition());

        int cap = m.works.capacity;
        int add = 3;
        if (m.duty == Mechanism.Duty.TIMING) {
            int n = BREAKS.merge(id, 1, Integer::sum);
            if (n % 4 == 0) add *= 2;
        }
        if (worksCount(player, Mechanism.Works.FOUNDRY) >= 3) add += 2;
        int cur = Math.min(cap, PRESSURE.getOrDefault(id, 0) + add);

        // Foundry conveniences apply on every break, not only on a discharge.
        if (m.duty == Mechanism.Duty.SMELT) smeltDropsAt(level, pos);
        if (m.duty == Mechanism.Duty.LIGHT && level.getBlockState(pos).isAir()
                && level.getMaxLocalRawBrightness(pos) < 6) {
            level.setBlockAndUpdate(pos, Blocks.LIGHT.defaultBlockState());
        }

        if (cur < cap) {
            PRESSURE.put(id, cur);
            return;
        }

        // Discharge.
        int keep = switch (m.duty) {
            case REGULATE -> cap / 2;
            case MOMENTUM -> cap / 4;
            default -> 0;
        };
        PRESSURE.put(id, keep);
        discharge(player, level, pos, m);
    }

    private static void discharge(ServerPlayer player, ServerLevel level, BlockPos pos, Mechanism m) {
        Vec3 at = Vec3.atCenterOf(pos);
        Vec3 me = player.position().add(0, 1, 0);
        boolean circuit = worksCount(player, Mechanism.Works.CIRCUIT) >= 3;
        double reach = circuit ? 3.0 : 2.0;

        player.sendSystemMessage(Component.literal(m.colour + "" + ChatFormatting.BOLD
                + m.display.toUpperCase(Locale.ROOT) + " §r§7discharges."), true);
        level.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.PLAYERS, 0.8f, 1.4f);

        switch (m.duty) {
            case SHATTER -> {
                VfxPlus.fissure(level, at, player.getLookAngle());
                breakArea(level, player, pos, (int) reach, 1);
            }
            case CHAIN -> {
                Vfx.ring(level, ParticleTypes.ELECTRIC_SPARK, at, 2.0, 30);
                breakMatching(level, player, pos, 4);
            }
            case BEAM -> {
                Vfx.lance(level, me, at.add(player.getLookAngle().scale(8)), Vfx.ARCANE);
                breakLine(level, player, pos, player.getLookAngle(), 8);
            }
            case COMPRESS -> {
                Vfx.puff(level, ParticleTypes.CRIT, at, 30, 0.8, 0.05);
                compressDrops(level, pos);
            }
            case SMELT -> {
                VfxPlus.flamePillar(level, at);
                smeltDropsAt(level, pos);
                breakArea(level, player, pos, 1, 1);
            }
            case VENT -> {
                VfxPlus.shockCone(level, me, player.getLookAngle(), Vfx.INFERNO);
                for (Entity e : level.getEntities(player, new AABB(me, me).inflate(5))) {
                    if (e instanceof LivingEntity le) {
                        le.igniteForSeconds(4);
                        le.hurtServer(level, level.damageSources().playerAttack(player), 18f);
                    }
                }
            }
            case OVERPRESSURE -> {
                VfxPlus.orbitalStrike(level, at, Vfx.STORM);
                breakArea(level, player, pos, 2, 2);
                player.hurtServer(level, level.damageSources().generic(), 2f);
            }
            case LIFT -> {
                Vfx.ring(level, ParticleTypes.CLOUD, me, 2.0, 30);
                player.setDeltaMovement(0, 1.4, 0);
                player.hurtMarked = true;
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0));
            }
            case RECALL -> {
                BlockPos anchor = ANCHOR.get(player.getUUID());
                if (anchor != null) {
                    VfxPlus.singularity(level, me, Vfx.VOID);
                    player.teleportTo(anchor.getX() + 0.5, anchor.getY() + 1, anchor.getZ() + 0.5);
                    ANCHOR.remove(player.getUUID());
                }
            }
            case TEMPER -> {
                VfxPlus.shieldDome(level, me, 3.0, Vfx.FROST);
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 300, 2));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, 3));
            }
            case MAGNET, SORT -> {
                Vfx.ring(level, ParticleTypes.ENCHANT, me, 6.0, 40);
                gatherDrops(player, 16);
            }
            case HASTE -> {
                Vfx.bloom(level, me);
                player.addEffect(new MobEffectInstance(MobEffects.HASTE, 400, 2));
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 400, 1));
            }
            case LIGHT -> {
                Vfx.shell(level, ParticleTypes.END_ROD, me, 4.0, 60);
                for (Entity e : level.getEntities(player, new AABB(me, me).inflate(8))) {
                    if (e instanceof LivingEntity le) {
                        le.addEffect(new MobEffectInstance(MobEffects.GLOWING, 300, 0));
                        le.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                    }
                }
            }
            case SURVEY -> {
                Vfx.shell(level, ParticleTypes.HAPPY_VILLAGER, me, 10.0, 80);
                surveyOre(player, 16);
            }
            case FUEL -> {
                VfxPlus.flamePillar(level, at);
                player.addEffect(new MobEffectInstance(MobEffects.HASTE, 600, 1));
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0));
            }
            case REGULATE, MOMENTUM, TIMING -> {
                Vfx.ring(level, ParticleTypes.ELECTRIC_SPARK, at, 2.5, 30);
                breakArea(level, player, pos, 1, 1);
                player.addEffect(new MobEffectInstance(MobEffects.HASTE, 200, 1));
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private static void breakArea(ServerLevel level, ServerPlayer player, BlockPos centre, int r, int depth) {
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -depth; z <= depth; z++) {
                    BlockPos p = centre.offset(x, y, z);
                    if (p.equals(centre)) continue;
                    BlockState s = level.getBlockState(p);
                    if (s.isAir() || s.getDestroySpeed(level, p) < 0) continue;
                    level.destroyBlock(p, true, player);
                }
            }
        }
    }

    private static void breakMatching(ServerLevel level, ServerPlayer player, BlockPos centre, int r) {
        BlockState want = level.getBlockState(centre);
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos p = centre.offset(x, y, z);
                    if (!level.getBlockState(p).is(want.getBlock())) continue;
                    level.destroyBlock(p, true, player);
                }
            }
        }
    }

    private static void breakLine(ServerLevel level, ServerPlayer player, BlockPos start, Vec3 dir, int len) {
        Vec3 cur = Vec3.atCenterOf(start);
        for (int i = 0; i < len; i++) {
            cur = cur.add(dir);
            BlockPos p = BlockPos.containing(cur);
            BlockState s = level.getBlockState(p);
            if (s.isAir() || s.getDestroySpeed(level, p) < 0) continue;
            level.destroyBlock(p, true, player);
        }
    }

    /** Turns raw ore drops on the floor into their smelted result. */
    private static void smeltDropsAt(ServerLevel level, BlockPos pos) {
        Vec3 c = Vec3.atCenterOf(pos);
        for (Entity e : level.getEntities((Entity) null, new AABB(c, c).inflate(2.5), en -> en instanceof ItemEntity)) {
            ItemEntity ie = (ItemEntity) e;
            ItemStack st = ie.getItem();
            ItemStack out = smelted(st);
            if (out != null) {
                out.setCount(st.getCount());
                ie.setItem(out);
            }
        }
    }

    private static ItemStack smelted(ItemStack in) {
        if (in.is(Items.RAW_IRON)) return new ItemStack(Items.IRON_INGOT);
        if (in.is(Items.RAW_GOLD)) return new ItemStack(Items.GOLD_INGOT);
        if (in.is(Items.RAW_COPPER)) return new ItemStack(Items.COPPER_INGOT);
        if (in.is(Items.COBBLESTONE)) return new ItemStack(Items.STONE);
        if (in.is(Items.SAND)) return new ItemStack(Items.GLASS);
        if (in.is(Items.CLAY_BALL)) return new ItemStack(Items.BRICK);
        if (in.is(Items.NETHERRACK)) return new ItemStack(Items.NETHER_BRICK);
        return null;
    }

    /** Turns loose nuggets and ingots into their block form. */
    private static void compressDrops(ServerLevel level, BlockPos pos) {
        Vec3 c = Vec3.atCenterOf(pos);
        for (Entity e : level.getEntities((Entity) null, new AABB(c, c).inflate(3.0), en -> en instanceof ItemEntity)) {
            ItemEntity ie = (ItemEntity) e;
            ItemStack st = ie.getItem();
            if (st.getCount() < 9) continue;
            ItemStack block = compressed(st);
            if (block == null) continue;
            int blocks = st.getCount() / 9;
            int rest = st.getCount() % 9;
            block.setCount(blocks);
            if (rest > 0) {
                ItemStack left = st.copy();
                left.setCount(rest);
                level.addFreshEntity(new ItemEntity(level, ie.getX(), ie.getY(), ie.getZ(), left));
            }
            ie.setItem(block);
        }
    }

    private static ItemStack compressed(ItemStack in) {
        if (in.is(Items.IRON_INGOT)) return new ItemStack(Items.IRON_BLOCK);
        if (in.is(Items.GOLD_INGOT)) return new ItemStack(Items.GOLD_BLOCK);
        if (in.is(Items.COPPER_INGOT)) return new ItemStack(Items.COPPER_BLOCK);
        if (in.is(Items.DIAMOND)) return new ItemStack(Items.DIAMOND_BLOCK);
        if (in.is(Items.EMERALD)) return new ItemStack(Items.EMERALD_BLOCK);
        if (in.is(Items.COAL)) return new ItemStack(Items.COAL_BLOCK);
        if (in.is(Items.REDSTONE)) return new ItemStack(Items.REDSTONE_BLOCK);
        if (in.is(Items.LAPIS_LAZULI)) return new ItemStack(Items.LAPIS_BLOCK);
        return null;
    }

    private static void gatherDrops(ServerPlayer player) {
        gatherDrops(player, 8);
    }

    private static void gatherDrops(ServerPlayer player, double r) {
        if (!(player.level() instanceof ServerLevel level)) return;
        Vec3 me = player.position();
        for (Entity e : level.getEntities((Entity) null, new AABB(me, me).inflate(r), en -> en instanceof ItemEntity)) {
            ItemEntity ie = (ItemEntity) e;
            if (ie.hasPickUpDelay()) continue;
            Vec3 pull = me.add(0, 0.5, 0).subtract(ie.position()).normalize().scale(0.45);
            ie.setDeltaMovement(pull);
        }
    }

    /** Marks ore inside the stone so it glows through the wall for a few seconds. */
    private static void surveyOre(ServerPlayer player, double r) {
        if (!(player.level() instanceof ServerLevel level)) return;
        if (player.tickCount % 60 != 0) return;
        BlockPos base = player.blockPosition();
        int ri = (int) r;
        for (int x = -ri; x <= ri; x += 2) {
            for (int y = -ri; y <= ri; y += 2) {
                for (int z = -ri; z <= ri; z += 2) {
                    BlockPos p = base.offset(x, y, z);
                    BlockState s = level.getBlockState(p);
                    if (!isOre(s)) continue;
                    Vec3 c = Vec3.atCenterOf(p);
                    level.sendParticles(ParticleTypes.HAPPY_VILLAGER, c.x, c.y, c.z, 2, 0.2, 0.2, 0.2, 0.0);
                }
            }
        }
    }

    private static boolean isOre(BlockState s) {
        return s.is(Blocks.DIAMOND_ORE) || s.is(Blocks.DEEPSLATE_DIAMOND_ORE)
                || s.is(Blocks.GOLD_ORE) || s.is(Blocks.DEEPSLATE_GOLD_ORE)
                || s.is(Blocks.IRON_ORE) || s.is(Blocks.DEEPSLATE_IRON_ORE)
                || s.is(Blocks.EMERALD_ORE) || s.is(Blocks.DEEPSLATE_EMERALD_ORE)
                || s.is(Blocks.LAPIS_ORE) || s.is(Blocks.DEEPSLATE_LAPIS_ORE)
                || s.is(Blocks.REDSTONE_ORE) || s.is(Blocks.DEEPSLATE_REDSTONE_ORE)
                || s.is(Blocks.ANCIENT_DEBRIS) || s.is(Blocks.NETHER_QUARTZ_ORE)
                || s.is(Blocks.NETHER_GOLD_ORE) || s.is(Blocks.COPPER_ORE)
                || s.is(Blocks.DEEPSLATE_COPPER_ORE);
    }

    public static void clear(UUID id) {
        PRESSURE.remove(id);
        BREAKS.remove(id);
        ANCHOR.remove(id);
    }
}
