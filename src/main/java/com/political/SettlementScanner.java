package com.political;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Scans the region around a settlement's Cornerstone to derive beds, appeal, job sites and
 * wonders. Heavy, so SettlementManager only rescans one settlement per throttle window and
 * caches the result on the Settlement. Only loaded chunks are read.
 *
 * <h2>Anti-cheese model</h2>
 * Appeal is deliberately NOT a raw sum of block weights — that let a player drop a thousand
 * diamond blocks and jump straight to Utopia. Instead:
 * <ul>
 *   <li><b>Diminishing returns</b>: each block <i>type</i> contributes {@code weight * sqrt(count)},
 *       so the 500th diamond block is worth ~2% of the first.</li>
 *   <li><b>Per-type caps</b>: no single block type may exceed {@link #PER_TYPE_CAP}.</li>
 *   <li><b>Category caps</b>: appeal is bucketed into {@link Category} and each bucket is capped,
 *       so a settlement must be developed on several independent axes. Precious-block spam alone
 *       tops out well below the Utopia requirement.</li>
 *   <li><b>Footprint</b>: appeal concentrated into one or two 16×16 cells is scaled down — a
 *       compact tower of valuables is not a city.</li>
 *   <li><b>Sheltered beds</b>: only beds under a roof and inside something wall-like count, so
 *       a field of 250 beds is not a Utopia.</li>
 * </ul>
 */
public class SettlementScanner {

    public static class ScanResult {
        public int beds;          // sheltered/valid beds only
        public int rawBeds;       // every bed head seen (for player feedback)
        public int appeal;
        public int jobSites;
        public int wonders;
        /** Distinct 16x16 cells containing counted appeal — the settlement's developed footprint. */
        public int footprintCells;
        /** Per-category appeal after caps, for the settlement info GUI. */
        public Map<String, Integer> appealByCategory = new HashMap<>();
    }

    /** Appeal buckets. Each is independently capped so no single axis can carry a settlement. */
    public enum Category {
        MONUMENT(700),    // beacons, conduits — rare and expensive by nature
        PRECIOUS(350),    // diamond/emerald/gold/netherite blocks — the classic spam vector
        LIGHTING(800),
        GREENERY(800),
        FURNISHING(800),
        CRAFTED(800);

        public final int cap;

        Category(int cap) {
            this.cap = cap;
        }
    }

    /** No single block type may contribute more appeal than this, before category capping. */
    private static final int PER_TYPE_CAP = 150;

    /** Horizontal scan radius by tier (blocks). Vertical band is fixed and small. */
    public static int radiusForTier(SettlementTier tier) {
        return switch (tier) {
            case OUTPOST, HAMLET -> 48;
            case VILLAGE, TOWN -> 64;
            case CITY, METROPOLIS -> 80;
            case MEGALOPOLIS, UTOPIA -> 96;
            case ELYSIUM -> 128;
        };
    }

    private static final int Y_DOWN = 12;
    private static final int Y_UP = 18;

    public static ScanResult scan(ServerLevel level, BlockPos center, SettlementTier tier) {
        ScanResult r = new ScanResult();
        int radius = radiusForTier(tier);
        int radiusSq = radius * radius;

        // type -> raw count, so we can apply sqrt + caps after the sweep.
        Map<String, Integer> typeCounts = new HashMap<>();
        Map<String, Category> typeCategory = new HashMap<>();
        Set<String> decoVariety = new HashSet<>();
        Set<Long> appealCells = new HashSet<>();
        Set<Long> countedBedHeads = new HashSet<>();
        int shelteredBeds = 0;

        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        BlockPos.MutableBlockPos mp = new BlockPos.MutableBlockPos();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radiusSq) continue;
                int wx = cx + dx;
                int wz = cz + dz;
                // Skip unloaded chunks entirely.
                if (!level.hasChunkAt(new BlockPos(wx, cy, wz))) continue;

                for (int dy = -Y_DOWN; dy <= Y_UP; dy++) {
                    int wy = cy + dy;
                    mp.set(wx, wy, wz);
                    BlockState state = level.getBlockState(mp);
                    if (state.isAir()) continue;

                    // Beds: count HEAD parts only, and only if actually housed.
                    if (state.getBlock() instanceof BedBlock) {
                        if (state.getValue(BedBlock.PART) == BedPart.HEAD
                                && countedBedHeads.add(mp.asLong())
                                && isSheltered(level, mp)) {
                            shelteredBeds++;
                        }
                        continue;
                    }

                    // Job sites (villager workstations).
                    if (isJobSite(state)) {
                        r.jobSites++;
                    }

                    Category cat = categoryOf(state);
                    if (cat != null) {
                        String key = state.getBlock().toString();
                        typeCounts.merge(key, 1, Integer::sum);
                        typeCategory.put(key, cat);
                        decoVariety.add(key);
                        appealCells.add((((long) (wx >> 4)) << 32) ^ (wz >> 4));
                    }

                    // Wonder: a beacon block sitting on a mineral base.
                    if (state.is(Blocks.BEACON) && hasBeaconBase(level, mp)) {
                        r.wonders++;
                    }
                }
            }
        }

        // ── Appeal: diminishing returns → per-type cap → per-category cap ──
        EnumMap<Category, Double> byCategory = new EnumMap<>(Category.class);
        for (Map.Entry<String, Integer> e : typeCounts.entrySet()) {
            Category cat = typeCategory.get(e.getKey());
            if (cat == null) continue;
            double weight = weightOfType(e.getKey());
            // sqrt growth: doubling the block count is worth ~1.41x, not 2x.
            double contribution = weight * Math.sqrt(e.getValue());
            contribution = Math.min(PER_TYPE_CAP, contribution);
            byCategory.merge(cat, contribution, Double::sum);
        }

        double appeal = 0.0;
        for (Category c : Category.values()) {
            double v = Math.min(c.cap, byCategory.getOrDefault(c, 0.0));
            appeal += v;
            if (v > 0) r.appealByCategory.put(c.name(), (int) Math.round(v));
        }

        // Variety: rewards breadth of decoration (1.0 .. 1.5).
        double variety = 1.0 + Math.min(0.5, decoVariety.size() / 60.0);

        // Footprint: a settlement squeezed into one or two chunks is not a city.
        r.footprintCells = appealCells.size();
        double spread = Math.min(1.0, 0.35 + 0.13 * appealCells.size());

        r.beds = shelteredBeds;
        r.rawBeds = countedBedHeads.size();
        r.appeal = (int) Math.round(appeal * variety * spread);
        return r;
    }

    /**
     * A bed only houses someone if it is under cover and inside something room-shaped:
     * a solid roof within 5 blocks above, and walls on at least 3 of the 4 horizontal
     * directions within 7 blocks. Stops open-field bed farms from inflating population.
     */
    private static boolean isSheltered(ServerLevel level, BlockPos bed) {
        boolean roofed = false;
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int up = 1; up <= 5; up++) {
            p.set(bed.getX(), bed.getY() + up, bed.getZ());
            if (!level.hasChunkAt(p)) return false;
            if (level.getBlockState(p).isSolidRender()) {
                roofed = true;
                break;
            }
        }
        if (!roofed) return false;

        int walls = 0;
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] d : dirs) {
            for (int dist = 1; dist <= 7; dist++) {
                p.set(bed.getX() + d[0] * dist, bed.getY() + 1, bed.getZ() + d[1] * dist);
                if (!level.hasChunkAt(p)) break;
                if (level.getBlockState(p).isSolidRender()) {
                    walls++;
                    break;
                }
            }
        }
        return walls >= 3;
    }

    private static boolean isJobSite(BlockState s) {
        return s.is(Blocks.SMITHING_TABLE) || s.is(Blocks.BLAST_FURNACE) || s.is(Blocks.SMOKER)
                || s.is(Blocks.CARTOGRAPHY_TABLE) || s.is(Blocks.FLETCHING_TABLE)
                || s.is(Blocks.BREWING_STAND) || s.is(Blocks.CAULDRON) || s.is(Blocks.WATER_CAULDRON)
                || s.is(Blocks.COMPOSTER) || s.is(Blocks.BARREL) || s.is(Blocks.LOOM)
                || s.is(Blocks.STONECUTTER) || s.is(Blocks.LECTERN) || s.is(Blocks.GRINDSTONE);
    }

    /** Which appeal bucket a block belongs to, or null if it contributes nothing. */
    private static Category categoryOf(BlockState s) {
        if (s.is(Blocks.BEACON) || s.is(Blocks.CONDUIT)) return Category.MONUMENT;

        if (s.is(Blocks.NETHERITE_BLOCK) || s.is(Blocks.DIAMOND_BLOCK) || s.is(Blocks.EMERALD_BLOCK)
                || s.is(Blocks.GOLD_BLOCK)) return Category.PRECIOUS;

        if (s.is(Blocks.SEA_LANTERN) || s.is(Blocks.LANTERN) || s.is(Blocks.SOUL_LANTERN)
                || s.is(Blocks.CAMPFIRE) || s.is(Blocks.SOUL_CAMPFIRE) || s.is(Blocks.GLOWSTONE)
                || s.is(Blocks.SHROOMLIGHT) || s.is(Blocks.OCHRE_FROGLIGHT)
                || s.is(Blocks.VERDANT_FROGLIGHT) || s.is(Blocks.PEARLESCENT_FROGLIGHT)
                || s.is(Blocks.END_ROD) || s.getBlock() instanceof net.minecraft.world.level.block.CandleBlock) {
            return Category.LIGHTING;
        }

        if (s.is(Blocks.AMETHYST_CLUSTER) || s.is(Blocks.FLOWER_POT) || s.is(Blocks.HAY_BLOCK)
                || s.is(Blocks.MOSS_BLOCK) || s.is(Blocks.MOSS_CARPET) || s.is(Blocks.AZALEA)
                || s.is(Blocks.FLOWERING_AZALEA) || s.is(Blocks.BIG_DRIPLEAF)
                || s.is(Blocks.SMALL_DRIPLEAF) || s.is(Blocks.PINK_PETALS)
                || s.getBlock() instanceof net.minecraft.world.level.block.FlowerBlock
                || s.getBlock() instanceof net.minecraft.world.level.block.SaplingBlock
                || s.getBlock() instanceof net.minecraft.world.level.block.LeavesBlock) {
            return Category.GREENERY;
        }

        if (s.getBlock() instanceof net.minecraft.world.level.block.BedBlock
                || s.getBlock() instanceof net.minecraft.world.level.block.ChestBlock
                || s.getBlock() instanceof net.minecraft.world.level.block.DoorBlock
                || s.getBlock() instanceof net.minecraft.world.level.block.TrapDoorBlock
                || s.getBlock() instanceof net.minecraft.world.level.block.FenceGateBlock
                || s.is(Blocks.BOOKSHELF) || s.is(Blocks.CHISELED_BOOKSHELF)
                || s.is(Blocks.CRAFTING_TABLE) || s.is(Blocks.ANVIL)
                || s.getBlock() instanceof net.minecraft.world.level.block.BellBlock) {
            return Category.FURNISHING;
        }

        if (s.getBlock() instanceof net.minecraft.world.level.block.BannerBlock
                || s.getBlock() instanceof net.minecraft.world.level.block.GlazedTerracottaBlock
                || s.is(Blocks.CHISELED_STONE_BRICKS) || s.is(Blocks.CHISELED_DEEPSLATE)
                || s.is(Blocks.CHISELED_QUARTZ_BLOCK) || s.is(Blocks.CHISELED_SANDSTONE)
                || s.is(Blocks.DECORATED_POT) || s.is(Blocks.LODESTONE)) {
            return Category.CRAFTED;
        }
        return null;
    }

    /** Base per-block weight before sqrt scaling and capping. */
    private static double weightOfType(String blockKey) {
        String k = blockKey.toLowerCase(java.util.Locale.ROOT);
        if (k.contains("beacon")) return 40;
        if (k.contains("conduit")) return 32;
        if (k.contains("netherite_block")) return 14;
        if (k.contains("diamond_block")) return 8;
        if (k.contains("emerald_block")) return 7;
        if (k.contains("gold_block")) return 5;
        if (k.contains("sea_lantern") || k.contains("glowstone") || k.contains("shroomlight")
                || k.contains("froglight")) return 4;
        if (k.contains("amethyst")) return 4;
        if (k.contains("lantern") || k.contains("candle") || k.contains("end_rod")) return 3;
        if (k.contains("banner") || k.contains("painting") || k.contains("decorated_pot")
                || k.contains("chiseled") || k.contains("glazed")) return 3;
        if (k.contains("bookshelf") || k.contains("anvil") || k.contains("lodestone")) return 3;
        if (k.contains("bed") || k.contains("door") || k.contains("chest")) return 2;
        return 2;
    }

    /** Rough check: the layer directly below the beacon is all beacon-base mineral blocks (>=1 tier). */
    private static boolean hasBeaconBase(ServerLevel level, BlockPos beacon) {
        BlockPos below = beacon.below();
        if (!level.hasChunkAt(below)) return false;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockState s = level.getBlockState(below.offset(dx, 0, dz));
                if (!(s.is(Blocks.IRON_BLOCK) || s.is(Blocks.GOLD_BLOCK) || s.is(Blocks.DIAMOND_BLOCK)
                        || s.is(Blocks.EMERALD_BLOCK) || s.is(Blocks.NETHERITE_BLOCK))) {
                    return false;
                }
            }
        }
        return true;
    }
}
