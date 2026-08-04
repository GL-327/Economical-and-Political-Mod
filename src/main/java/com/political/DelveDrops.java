package com.political;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * What a delve actually pays out in.
 *
 * <p>The thousand and fifty spoils were craftable from the day they shipped and dropped from
 * nothing, which made the bottom of every chain unobtainable and the whole ladder decorative. This
 * is the source: mobs drop the depth's own spoils, secrets pay key shards, and the boss pays the
 * good ones.</p>
 *
 * <p>Everything here is weighted by depth, so what falls on Depth 7 is visibly not what falls on
 * Depth 1. That is the entire reason the spoils carry a depth in their name.</p>
 */
public final class DelveDrops {

    private DelveDrops() {}

    /** Which spoil depth matches a delve depth. They are the same seven, in the same order. */
    private static DelveSpoils.Depth depthFor(DelveDepth floor) {
        int i = Math.max(0, Math.min(DelveSpoils.DEPTHS.size() - 1, floor.tier - 1));
        return DelveSpoils.DEPTHS.get(i);
    }

    /**
     * Picks a spoil grade.
     *
     * <p>Weighted toward the low end and tailing off sharply, so the fifteenth grade is something
     * people talk about rather than something they have a stack of. Magic Find pushes the roll up,
     * which is the whole reason the stat exists.</p>
     */
    private static DelveSpoils.Variant rollVariant(ServerPlayer player, double bonus) {
        double mf = 1.0;
        try {
            mf = 1.0 + RareDropManager.magicFindOf(player) / 150.0;
        } catch (Exception ignored) {
            // Stats not ready. A flat roll is fine.
        }
        double roll = player.getRandom().nextDouble();
        // Raising a value below one to a power above one pushes it toward zero, so a bigger
        // multiplier means a smaller number here, which means a higher index below.
        double shaped = Math.pow(roll, 2.4 / (mf * bonus));
        int idx = (int) Math.floor(shaped * DelveSpoils.VARIANTS.size());
        idx = DelveSpoils.VARIANTS.size() - 1 - Math.max(0,
                Math.min(DelveSpoils.VARIANTS.size() - 1, idx));
        return DelveSpoils.VARIANTS.get(idx);
    }

    private static DelveSpoils.Kind rollKind(ServerPlayer player) {
        return DelveSpoils.KINDS.get(player.getRandom().nextInt(DelveSpoils.KINDS.size()));
    }

    // ── Mobs ──────────────────────────────────────────────────────────────

    /**
     * A delve mob dying.
     *
     * <p>Not every kill drops something. A guaranteed drop on every mob turns a floor into a
     * conveyor belt, and the point of a spoil is that it is worth picking up.</p>
     */
    public static void onMobKill(ServerPlayer by, LivingEntity victim) {
        DelveRun run = DelveManager.runOf(by);
        if (run == null) return;
        if (!victim.entityTags().contains(DelvePopulator.MOB_TAG)) return;

        boolean elite = victim.entityTags().contains(DelvePopulator.ELITE_TAG);
        double chance = elite ? 1.0 : 0.35;
        if (by.getRandom().nextDouble() > chance) return;

        int count = elite ? 2 + by.getRandom().nextInt(3) : 1;
        for (int i = 0; i < count; i++) {
            give(by, run, rollKind(by), rollVariant(by, elite ? 1.6 : 1.0), false);
        }
    }

    /**
     * A secret found.
     *
     * <p>Secrets pay key shards specifically, because keys are what marks cost and secrets are the
     * thing the score rewards. Tying the two together means the player who explores is the player
     * who can afford to upgrade.</p>
     */
    public static void onSecret(ServerPlayer by, DelveRun run) {
        DelveSpoils.Kind keys = DelveSpoils.KINDS.get(3);   // Key Shard
        int count = 2 + by.getRandom().nextInt(3);
        for (int i = 0; i < count; i++) {
            give(by, run, keys, rollVariant(by, 1.8), true);
        }
    }

    /** The boss. Pays the whole party, and pays the good grades. */
    public static void onBossKill(DelveRun run) {
        for (ServerPlayer p : run.online()) {
            int count = 4 + run.floor.tier;
            for (int i = 0; i < count; i++) {
                give(p, run, rollKind(p), rollVariant(p, 2.6), false);
            }
            // A guaranteed handful of key shards, so a clear always moves the forge along.
            DelveSpoils.Kind keys = DelveSpoils.KINDS.get(3);
            for (int i = 0; i < 2 + run.floor.tier / 2; i++) {
                give(p, run, keys, rollVariant(p, 2.2), true);
            }
        }
    }

    /** Hands over one spoil and says so when it is worth saying. */
    private static void give(ServerPlayer to, DelveRun run, DelveSpoils.Kind kind,
                             DelveSpoils.Variant variant, boolean quiet) {
        DelveSpoils.Depth depth = depthFor(run.floor);
        ItemStack stack = DelveItems.spoil(kind, depth, variant);
        if (!to.getInventory().add(stack)) to.drop(stack, false);

        int grade = DelveSpoils.VARIANTS.indexOf(variant);
        if (!quiet && grade >= 10) {
            // The top third is rare enough that the whole party should hear about it.
            for (ServerPlayer p : run.online()) {
                p.sendSystemMessage(Component.literal("  " + depth.colour() + "✦ "
                        + to.getName().getString() + " found "
                        + DelveSpoils.nameOf(kind, depth, variant)));
            }
            if (to.level() instanceof ServerLevel level) {
                level.playSound(null, to.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                        SoundSource.PLAYERS, 0.8f, 1.4f);
            }
        }
    }

    // ── Keys ──────────────────────────────────────────────────────────────

    /**
     * How many key shards a player is carrying, across every depth and grade.
     *
     * <p>Counted by weight rather than by piece, so a Primal Hollow Key Shard is worth many
     * Catacomb ones. Otherwise the cheapest possible shard would be the only one anybody kept.</p>
     */
    public static long keyWeight(ServerPlayer player) {
        long total = 0;
        for (ItemStack st : player.getInventory().getNonEquipmentItems()) {
            if (st.isEmpty()) continue;
            String id = SlayerItems.getCustomItemId(st);
            if (id == null || !id.startsWith("delve_") || !id.contains("_keyshard_")) continue;
            total += weightOf(id) * st.getCount();
        }
        return total;
    }

    /** Takes key weight, cheapest first. Returns false and takes nothing when short. */
    public static boolean takeKeys(ServerPlayer player, long want) {
        if (want <= 0) return true;
        if (keyWeight(player) < want) return false;

        long left = want;
        // Cheapest first, so a player is never charged a Primal shard for a Catacomb price.
        List<ItemStack> shards = new ArrayList<>();
        for (ItemStack st : player.getInventory().getNonEquipmentItems()) {
            if (st.isEmpty()) continue;
            String id = SlayerItems.getCustomItemId(st);
            if (id != null && id.startsWith("delve_") && id.contains("_keyshard_")) shards.add(st);
        }
        shards.sort((a, b) -> Long.compare(
                weightOf(SlayerItems.getCustomItemId(a)),
                weightOf(SlayerItems.getCustomItemId(b))));

        for (ItemStack st : shards) {
            if (left <= 0) break;
            long each = weightOf(SlayerItems.getCustomItemId(st));
            if (each <= 0) continue;
            int need = (int) Math.min(st.getCount(), (left + each - 1) / each);
            st.shrink(need);
            left -= (long) need * each;
        }
        return left <= 0;
    }

    /** What one shard of a given id is worth, from its depth and grade. */
    private static long weightOf(String id) {
        if (id == null) return 0;
        for (DelveSpoils.Depth d : DelveSpoils.DEPTHS) {
            for (DelveSpoils.Variant v : DelveSpoils.VARIANTS) {
                if (id.equals("delve_" + d.key() + "_keyshard_" + v.key())) {
                    return Math.max(1, Math.round(d.tier() * v.weight()));
                }
            }
        }
        return 0;
    }
}
