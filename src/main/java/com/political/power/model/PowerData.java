package com.political.power.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * The whole power file: every player's state, who holds each unique technique, and a quarantine of old
 * data that had no place in the new model. The file is {@code power.json} in the world root, saved through
 * {@code VersionedStore}.
 */
public final class PowerData {

    /** Player UUID (as text) to that player's state. */
    public final Map<String, PlayerPower> players = new HashMap<>();
    /** Unique technique id to the UUID of its one holder. */
    public final Map<String, String> uniqueHolders = new HashMap<>();
    /**
     * Old values that could not be mapped (spells, skill tree nodes, unknown technique names). Keys look like
     * {@code <uuid>.<field>}, values are the old JSON as text. Nothing here is ever deleted by the mod.
     */
    public final Map<String, String> quarantine = new TreeMap<>();

    public PowerData() {
    }

    private PowerData(Map<String, PlayerPower> players, Map<String, String> uniqueHolders, Map<String, String> quarantine) {
        this.players.putAll(players);
        this.uniqueHolders.putAll(uniqueHolders);
        this.quarantine.putAll(quarantine);
    }

    public static final Codec<PowerData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.unboundedMap(Codec.STRING, PlayerPower.CODEC).optionalFieldOf("players", Map.of()).forGetter(d -> d.players),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("unique_holders", Map.of()).forGetter(d -> d.uniqueHolders),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("quarantine", Map.of()).forGetter(d -> d.quarantine)
    ).apply(i, PowerData::new));

    public PlayerPower player(String uuid) {
        return players.computeIfAbsent(uuid, k -> new PlayerPower());
    }
}
