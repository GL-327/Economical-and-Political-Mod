package com.political;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * V6 variety — an additive library of extra Expedition <b>combat-room</b> archetypes.
 *
 * <p>V5's {@link ExpeditionGenerator} only ever dressed a combat room one of three ways. This
 * class layers a deterministic archetype on top of that base dressing so no two combat rooms feel
 * identical: traps, shrines, gauntlets, merchant stalls, mini-boss rings, riddle alcoves and
 * themed halls. It never touches entry / puzzle / vault / boss rooms — those keep their V5
 * mechanics untouched — and it is fully deterministic from {@code (seed, floor, gx, gz)} so a
 * re-stamp of the same floor reproduces the same room.</p>
 *
 * <h2>How to wire it (one line, no edit by me)</h2>
 * In {@link ExpeditionGenerator#stampFloor}, inside the existing
 * {@code for (ExpeditionDungeon.Room r : d.roomList())} loop, AFTER the {@code switch (r.type)}
 * block, add:
 * <pre>{@code   ExpeditionRoomVariants.decorate(level, d, r);}</pre>
 * {@link #decorate} early-returns for every non-combat room, so the call is safe for all rooms.
 *
 * <h2>Why it cannot be farmed</h2>
 * Everything here is purely block dressing plus, for two archetypes, one small chest of common
 * vanilla items. Expedition instances already cost a launch, are time-limited, wiped on exit and
 * cooldown-gated by {@link ExpeditionRunManager}; the chest loot is modest and floor-capped, so it
 * cannot outrun the existing vault/boss economy. Shrine "buffs" are plain block markers with lore
 * on a sign-free lectern — they grant nothing on their own; the run manager's own mob placement is
 * unchanged.
 */
public final class ExpeditionRoomVariants {

    private ExpeditionRoomVariants() {}

    // Archetype identifiers (also the loot-flavour keys).
    public static final String TRAP_SPIKES     = "TRAP_SPIKES";
    public static final String TRAP_INFERNO    = "TRAP_INFERNO";
    public static final String SHRINE_VITALITY = "SHRINE_VITALITY";
    public static final String SHRINE_MIGHT    = "SHRINE_MIGHT";
    public static final String GAUNTLET_PILLARS= "GAUNTLET_PILLARS";
    public static final String GAUNTLET_MAZE   = "GAUNTLET_MAZE";
    public static final String MERCHANT_STALL  = "MERCHANT_STALL";
    public static final String MINIBOSS_RING   = "MINIBOSS_RING";
    public static final String RIDDLE_ALCOVE   = "RIDDLE_ALCOVE";
    public static final String TREASURE_NOOK   = "TREASURE_NOOK";
    public static final String FUNGAL_GROTTO   = "FUNGAL_GROTTO";
    public static final String FROZEN_HALL     = "FROZEN_HALL";
    public static final String SUNKEN_FORGE    = "SUNKEN_FORGE";
    public static final String LOST_LIBRARY    = "LOST_LIBRARY";

    /** Every archetype, in stamping order — 14 total. */
    public static final String[] ARCHETYPES = {
            TRAP_SPIKES, TRAP_INFERNO, SHRINE_VITALITY, SHRINE_MIGHT,
            GAUNTLET_PILLARS, GAUNTLET_MAZE, MERCHANT_STALL, MINIBOSS_RING,
            RIDDLE_ALCOVE, TREASURE_NOOK, FUNGAL_GROTTO, FROZEN_HALL,
            SUNKEN_FORGE, LOST_LIBRARY
    };

    /**
     * Deterministically choose an archetype for a floor position. Deeper floors weight the
     * dangerous / rewarding archetypes a little more heavily.
     */
    public static String pick(long seed, int floor, int gx, int gz) {
        long h = ExpeditionGenerator.hash(seed, gx * 131 + floor, gz * 17 + floor, 6006);
        int idx = (int) Math.floorMod(h, (long) ARCHETYPES.length);
        // Deep-floor bias: 1-in-3 chance to reroll toward a mini-boss / merchant / treasure room.
        if (floor >= 3) {
            long h2 = ExpeditionGenerator.hash(seed, gx + 7, gz + 9, 6007 + floor);
            if (Math.floorMod(h2, 3L) == 0L) {
                String[] rich = {MINIBOSS_RING, MERCHANT_STALL, TREASURE_NOOK, SHRINE_MIGHT};
                idx = (int) Math.floorMod(h2, (long) rich.length);
                return rich[idx];
            }
        }
        return ARCHETYPES[idx];
    }

    /** Convenience overload matching the requested {@code pick(seed, floor)} signature. */
    public static String pick(long seed, int floor) {
        return pick(seed, floor, floor & 1, (floor >> 1) & 1);
    }

    // ────────────────────────────────────────────────────────────
    // Entry point
    // ────────────────────────────────────────────────────────────

    /**
     * Layer an archetype onto a combat room. No-op for every other room type, so this can be
     * called unconditionally for every room in {@link ExpeditionGenerator#stampFloor}.
     */
    public static void decorate(ServerLevel level, ExpeditionDungeon d, ExpeditionDungeon.Room r) {
        if (level == null || d == null || r == null) return;
        if (!ExpeditionGenerator.T_COMBAT.equals(r.type)) return;

        String archetype = pick(d.seed, d.floor, r.gx, r.gz);
        BlockPos c = ExpeditionGenerator.roomCenter(d, r);
        Random rng = new Random(ExpeditionGenerator.hash(d.seed, r.gx * 3 + d.floor, r.gz * 5 + d.floor, 6100));

        switch (archetype) {
            case TRAP_SPIKES      -> stampTrapSpikes(level, c);
            case TRAP_INFERNO     -> stampTrapInferno(level, c);
            case SHRINE_VITALITY  -> stampShrine(level, c, Blocks.RED_CANDLE.defaultBlockState(), Blocks.RED_CARPET.defaultBlockState());
            case SHRINE_MIGHT     -> stampShrine(level, c, Blocks.SOUL_CAMPFIRE.defaultBlockState(), Blocks.CRIMSON_NYLIUM.defaultBlockState());
            case GAUNTLET_PILLARS -> stampGauntletPillars(level, c);
            case GAUNTLET_MAZE    -> stampGauntletMaze(level, c);
            case MERCHANT_STALL   -> stampMerchantStall(level, d, c, rng);
            case MINIBOSS_RING    -> stampMiniBossRing(level, c);
            case RIDDLE_ALCOVE    -> stampRiddleAlcove(level, c);
            case TREASURE_NOOK    -> stampTreasureNook(level, d, c, rng);
            case FUNGAL_GROTTO    -> stampThemed(level, c, Blocks.MOSS_BLOCK.defaultBlockState(),
                                        Blocks.GLOW_LICHEN.defaultBlockState(), Blocks.SHROOMLIGHT.defaultBlockState());
            case FROZEN_HALL      -> stampThemed(level, c, Blocks.PACKED_ICE.defaultBlockState(),
                                        Blocks.BLUE_ICE.defaultBlockState(), Blocks.SEA_LANTERN.defaultBlockState());
            case SUNKEN_FORGE     -> stampThemed(level, c, Blocks.BASALT.defaultBlockState(),
                                        Blocks.MAGMA_BLOCK.defaultBlockState(), Blocks.LANTERN.defaultBlockState());
            case LOST_LIBRARY     -> stampThemed(level, c, Blocks.BOOKSHELF.defaultBlockState(),
                                        Blocks.CHISELED_BOOKSHELF.defaultBlockState(), Blocks.CANDLE.defaultBlockState());
            default -> { /* nothing */ }
        }
    }

    // ────────────────────────────────────────────────────────────
    // Archetype stamps
    // ────────────────────────────────────────────────────────────

    private static void stampTrapSpikes(ServerLevel level, BlockPos c) {
        // A cross of pointed dripstone spikes rising from a sunken grate.
        BlockState spike = Blocks.POINTED_DRIPSTONE.defaultBlockState();
        for (int i = -5; i <= 5; i++) {
            if (i == 0) continue;
            set(level, c.getX() + i, ExpeditionGenerator.STAND_Y, c.getZ(), spike);
            set(level, c.getX(), ExpeditionGenerator.STAND_Y, c.getZ() + i, spike);
        }
        set(level, c.getX(), ExpeditionGenerator.FLOOR_Y, c.getZ(), Blocks.IRON_BARS.defaultBlockState());
    }

    private static void stampTrapInferno(ServerLevel level, BlockPos c) {
        // Ring of magma with a single safe stepping path — pure hazard flavour.
        BlockState magma = Blocks.MAGMA_BLOCK.defaultBlockState();
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                if (Math.max(Math.abs(dx), Math.abs(dz)) != 4) continue;
                if (dx == 0) continue; // leave a gap so nobody is trapped
                set(level, c.getX() + dx, ExpeditionGenerator.FLOOR_Y, c.getZ() + dz, magma);
            }
        }
        set(level, c.getX(), ExpeditionGenerator.CEIL_Y - 1, c.getZ(), Blocks.FIRE.defaultBlockState());
    }

    private static void stampShrine(ServerLevel level, BlockPos c, BlockState top, BlockState carpet) {
        // A dais with a lectern (readable) and a candle/campfire — cosmetic focal point.
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                set(level, c.getX() + dx, ExpeditionGenerator.FLOOR_Y, c.getZ() + dz, Blocks.SMOOTH_QUARTZ.defaultBlockState());
                set(level, c.getX() + dx, ExpeditionGenerator.STAND_Y, c.getZ() + dz, carpet);
            }
        }
        set(level, c.getX(), ExpeditionGenerator.STAND_Y, c.getZ(), Blocks.LECTERN.defaultBlockState());
        set(level, c.getX(), ExpeditionGenerator.STAND_Y + 1, c.getZ(), top);
    }

    private static void stampGauntletPillars(ServerLevel level, BlockPos c) {
        // Dense staggered pillars — a tight fight with lots of line-of-sight breaks.
        BlockState p = Blocks.POLISHED_DEEPSLATE.defaultBlockState();
        int[][] spots = {{-6, -2}, {-2, -6}, {2, 6}, {6, 2}, {-4, 4}, {4, -4}, {0, -6}, {0, 6}};
        for (int[] s : spots) {
            for (int y = ExpeditionGenerator.STAND_Y; y < ExpeditionGenerator.CEIL_Y; y++) {
                set(level, c.getX() + s[0], y, c.getZ() + s[1], p);
            }
        }
    }

    private static void stampGauntletMaze(ServerLevel level, BlockPos c) {
        // Low chest-high walls forming a short serpentine.
        BlockState w = Blocks.COBBLED_DEEPSLATE_WALL.defaultBlockState();
        for (int dx = -6; dx <= 6; dx++) {
            set(level, c.getX() + dx, ExpeditionGenerator.STAND_Y, c.getZ() - 3, w);
            if (dx != 6) set(level, c.getX() + dx, ExpeditionGenerator.STAND_Y, c.getZ() + 3, w);
        }
        for (int dz = -3; dz <= 3; dz++) {
            set(level, c.getX() - 2, ExpeditionGenerator.STAND_Y, c.getZ() + dz, w);
        }
    }

    private static void stampMerchantStall(ServerLevel level, ExpeditionDungeon d, BlockPos c, Random rng) {
        // A little market stall with a barrel of modest supplies.
        set(level, c.getX() - 1, ExpeditionGenerator.STAND_Y, c.getZ(), Blocks.OAK_FENCE.defaultBlockState());
        set(level, c.getX() + 1, ExpeditionGenerator.STAND_Y, c.getZ(), Blocks.OAK_FENCE.defaultBlockState());
        set(level, c.getX() - 1, ExpeditionGenerator.STAND_Y + 1, c.getZ(), Blocks.RED_WOOL.defaultBlockState());
        set(level, c.getX() + 1, ExpeditionGenerator.STAND_Y + 1, c.getZ(), Blocks.RED_WOOL.defaultBlockState());
        set(level, c.getX(), ExpeditionGenerator.STAND_Y + 1, c.getZ() - 1,
                Blocks.RED_WOOL.defaultBlockState());
        placeStockedContainer(level, new BlockPos(c.getX(), ExpeditionGenerator.STAND_Y, c.getZ() - 1),
                Blocks.BARREL.defaultBlockState(), flavourLoot(d.seed, d.floor, MERCHANT_STALL));
    }

    private static void stampMiniBossRing(ServerLevel level, BlockPos c) {
        // A raised obsidian arena ring — the run manager's mob placement fills it.
        BlockState ring = Blocks.CRYING_OBSIDIAN.defaultBlockState();
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                if (Math.max(Math.abs(dx), Math.abs(dz)) == 5) {
                    set(level, c.getX() + dx, ExpeditionGenerator.STAND_Y, c.getZ() + dz, ring);
                }
            }
        }
        set(level, c.getX(), ExpeditionGenerator.CEIL_Y - 1, c.getZ(), Blocks.GLOWSTONE.defaultBlockState());
    }

    private static void stampRiddleAlcove(ServerLevel level, BlockPos c) {
        // A quiet alcove with a lectern and framed sculk — flavour lore spot, no mechanic.
        set(level, c.getX(), ExpeditionGenerator.STAND_Y, c.getZ() + 5, Blocks.LECTERN.defaultBlockState());
        set(level, c.getX() - 1, ExpeditionGenerator.STAND_Y, c.getZ() + 5, Blocks.SCULK.defaultBlockState());
        set(level, c.getX() + 1, ExpeditionGenerator.STAND_Y, c.getZ() + 5, Blocks.SCULK.defaultBlockState());
        set(level, c.getX(), ExpeditionGenerator.STAND_Y + 1, c.getZ() + 5, Blocks.SCULK_CATALYST.defaultBlockState());
    }

    private static void stampTreasureNook(ServerLevel level, ExpeditionDungeon d, BlockPos c, Random rng) {
        // Gilded corner with a small chest of common spoils.
        set(level, c.getX() + 5, ExpeditionGenerator.FLOOR_Y, c.getZ() + 5, Blocks.GILDED_BLACKSTONE.defaultBlockState());
        set(level, c.getX() + 5, ExpeditionGenerator.STAND_Y + 1, c.getZ() + 5, Blocks.END_ROD.defaultBlockState());
        placeStockedContainer(level, new BlockPos(c.getX() + 5, ExpeditionGenerator.STAND_Y, c.getZ() + 5),
                Blocks.CHEST.defaultBlockState(), flavourLoot(d.seed, d.floor, TREASURE_NOOK));
    }

    private static void stampThemed(ServerLevel level, BlockPos c, BlockState floor, BlockState accent, BlockState light) {
        // A cosmetic theme wash: patterned floor + corner accents + a hanging light.
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                if (((dx + dz) & 1) == 0) {
                    set(level, c.getX() + dx, ExpeditionGenerator.FLOOR_Y, c.getZ() + dz, floor);
                }
            }
        }
        int[][] corners = {{-5, -5}, {-5, 5}, {5, -5}, {5, 5}};
        for (int[] p : corners) {
            set(level, c.getX() + p[0], ExpeditionGenerator.STAND_Y, c.getZ() + p[1], accent);
        }
        set(level, c.getX(), ExpeditionGenerator.CEIL_Y - 1, c.getZ(), light);
    }

    // ────────────────────────────────────────────────────────────
    // Loot flavour
    // ────────────────────────────────────────────────────────────

    /**
     * Modest, floor-scaled common spoils for the two archetypes that carry a container. Exposed so
     * {@link ExpeditionRunManager} could also draw on it if desired; kept small on purpose so it
     * never competes with vault / boss rewards.
     */
    public static List<ItemStack> flavourLoot(long seed, int floor, String archetype) {
        Random rng = new Random(ExpeditionGenerator.hash(seed, floor, archetype == null ? 0 : archetype.hashCode(), 6200));
        List<ItemStack> out = new ArrayList<>();
        int f = Math.max(1, floor);
        if (MERCHANT_STALL.equals(archetype)) {
            out.add(new ItemStack(Items.GOLDEN_CARROT, 2 + rng.nextInt(3)));
            out.add(new ItemStack(Items.ARROW, 8 + rng.nextInt(8)));
            if (rng.nextFloat() < 0.5f) out.add(new ItemStack(Items.EXPERIENCE_BOTTLE, 1 + rng.nextInt(f)));
        } else if (TREASURE_NOOK.equals(archetype)) {
            out.add(new ItemStack(Items.GOLD_INGOT, 1 + rng.nextInt(1 + f)));
            if (rng.nextFloat() < 0.35f) out.add(new ItemStack(Items.DIAMOND, 1));
            if (rng.nextFloat() < 0.20f) out.add(new ItemStack(Items.EMERALD, 1 + rng.nextInt(2)));
        }
        return out;
    }

    // ────────────────────────────────────────────────────────────
    // Helpers
    // ────────────────────────────────────────────────────────────

    private static void placeStockedContainer(ServerLevel level, BlockPos pos, BlockState container, List<ItemStack> loot) {
        set(level, pos.getX(), pos.getY(), pos.getZ(), container);
        if (loot == null || loot.isEmpty()) return;
        if (level.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
            int slot = 0;
            for (ItemStack st : loot) {
                if (st == null || st.isEmpty()) continue;
                if (slot >= chest.getContainerSize()) break;
                chest.setItem(slot++, st);
            }
        }
        // Barrels are also containers; ChestBlockEntity covers the CHEST case, barrel loot is
        // populated via the generic container path below when it is not a chest.
        else if (level.getBlockEntity(pos) instanceof net.minecraft.world.Container gc) {
            int slot = 0;
            for (ItemStack st : loot) {
                if (st == null || st.isEmpty()) continue;
                if (slot >= gc.getContainerSize()) break;
                gc.setItem(slot++, st);
            }
        }
    }

    private static void set(ServerLevel level, int x, int y, int z, BlockState state) {
        level.setBlock(new BlockPos(x, y, z), state, Block.UPDATE_CLIENTS);
    }
}
