package com.political.power.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Everything the power system saves about one player. Runtime state that must not survive a restart
 * (cooldowns, the cast history, an active Domain) lives elsewhere, in {@code PowerRuntime}.
 *
 * <p>Plain mutable fields with a codec; the codec is the file format, schema version 2. Version 2 added the innate
 * technique (every player is given one at random), whether it is awakened, and the reroll counters. The school and
 * tree fields of version 1 are kept as they were (nothing is dropped) but the game no longer reads them.</p>
 */
public final class PlayerPower {

    /** The school the player chose in schema 1, or null. Kept, unused: techniques are rolled now. */
    public String school;
    /** Arcane shards: what a reroll of the innate technique costs. */
    public int shards;
    public int shardsEarnedTotal;
    /** Ids of unlocked school tree nodes from schema 1. Kept, unused. */
    public final Set<String> unlockedNodes = new HashSet<>();
    /** Techniques granted by an admin on top of the innate one. */
    public final Set<String> learned = new HashSet<>();
    /** The technique this player was given at random (or rerolled into), or null before the first draw. */
    public String innate;
    /** Whether the innate technique has been awakened (the configured boss was defeated, or an admin did it). */
    public boolean awakened;
    /** How many times the innate technique has been rerolled; the price of the next shard reroll grows with it. */
    public int rerolls;
    /** Free rerolls the player holds. */
    public int rerollCharges;
    public String equipped;
    public String selectedAbility;
    /** Casts per technique, which ranks the older techniques up. */
    public final Map<String, Integer> uses = new HashMap<>();
    /** Current amount of the cursed resource; negative until first seen, then set to the maximum. */
    public double resource = -1.0;
    public int level = 1;
    public int xp;
    /** Active binding vows by name. */
    public final Set<String> vows = new HashSet<>();
    /** Ticks left of the wait after breaking a vow, as of the last save. */
    public long vowWaitTicks;

    public PlayerPower() {
    }

    PlayerPower(Optional<String> school, int shards, int shardsEarnedTotal, List<String> unlockedNodes, List<String> learned,
                Optional<String> equipped, Optional<String> selectedAbility, Map<String, Integer> uses, double resource,
                int level, int xp, List<String> vows, long vowWaitTicks, InnateData innateData) {
        this.school = school.orElse(null);
        this.shards = shards;
        this.shardsEarnedTotal = shardsEarnedTotal;
        this.unlockedNodes.addAll(unlockedNodes);
        this.learned.addAll(learned);
        this.equipped = equipped.orElse(null);
        this.selectedAbility = selectedAbility.orElse(null);
        this.uses.putAll(uses);
        this.resource = resource;
        this.level = level;
        this.xp = xp;
        this.vows.addAll(vows);
        this.vowWaitTicks = vowWaitTicks;
        this.innate = innateData.technique().orElse(null);
        this.awakened = innateData.awakened();
        this.rerolls = innateData.rerolls();
        this.rerollCharges = innateData.charges();
    }

    /** The innate technique fields as one saved object, so the codec stays within the limit of 16 fields. */
    record InnateData(Optional<String> technique, boolean awakened, int rerolls, int charges) {
        static final InnateData EMPTY = new InnateData(Optional.empty(), false, 0, 0);
        static final Codec<InnateData> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.optionalFieldOf("technique").forGetter(InnateData::technique),
                Codec.BOOL.optionalFieldOf("awakened", false).forGetter(InnateData::awakened),
                Codec.INT.optionalFieldOf("rerolls", 0).forGetter(InnateData::rerolls),
                Codec.INT.optionalFieldOf("reroll_charges", 0).forGetter(InnateData::charges)
        ).apply(i, InnateData::new));
    }

    private InnateData innateData() {
        return new InnateData(Optional.ofNullable(innate), awakened, rerolls, rerollCharges);
    }

    private static List<String> sorted(Set<String> set) {
        List<String> list = new ArrayList<>(set);
        java.util.Collections.sort(list);
        return list;
    }

    public static final Codec<PlayerPower> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.optionalFieldOf("school").forGetter(p -> Optional.ofNullable(p.school)),
            Codec.INT.optionalFieldOf("shards", 0).forGetter(p -> p.shards),
            Codec.INT.optionalFieldOf("shards_earned_total", 0).forGetter(p -> p.shardsEarnedTotal),
            Codec.STRING.listOf().optionalFieldOf("unlocked_nodes", List.of()).forGetter(p -> sorted(p.unlockedNodes)),
            Codec.STRING.listOf().optionalFieldOf("learned", List.of()).forGetter(p -> sorted(p.learned)),
            Codec.STRING.optionalFieldOf("equipped").forGetter(p -> Optional.ofNullable(p.equipped)),
            Codec.STRING.optionalFieldOf("selected_ability").forGetter(p -> Optional.ofNullable(p.selectedAbility)),
            Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("uses", Map.of()).forGetter(p -> p.uses),
            Codec.DOUBLE.optionalFieldOf("resource", -1.0).forGetter(p -> p.resource),
            Codec.INT.optionalFieldOf("level", 1).forGetter(p -> p.level),
            Codec.INT.optionalFieldOf("xp", 0).forGetter(p -> p.xp),
            Codec.STRING.listOf().optionalFieldOf("vows", List.of()).forGetter(p -> sorted(p.vows)),
            Codec.LONG.optionalFieldOf("vow_wait_ticks", 0L).forGetter(p -> p.vowWaitTicks),
            InnateData.CODEC.optionalFieldOf("innate", InnateData.EMPTY).forGetter(PlayerPower::innateData)
    ).apply(i, PlayerPower::new));

    /** Value equality over every saved field, for tests and for noticing real changes. */
    public boolean sameAs(PlayerPower other) {
        return java.util.Objects.equals(school, other.school) && shards == other.shards
                && shardsEarnedTotal == other.shardsEarnedTotal && unlockedNodes.equals(other.unlockedNodes)
                && learned.equals(other.learned) && java.util.Objects.equals(equipped, other.equipped)
                && java.util.Objects.equals(selectedAbility, other.selectedAbility) && uses.equals(other.uses)
                && Double.compare(resource, other.resource) == 0 && level == other.level && xp == other.xp
                && vows.equals(other.vows) && vowWaitTicks == other.vowWaitTicks
                && java.util.Objects.equals(innate, other.innate) && awakened == other.awakened
                && rerolls == other.rerolls && rerollCharges == other.rerollCharges;
    }
}
