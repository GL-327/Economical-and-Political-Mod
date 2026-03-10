package com.political;

import java.util.*;

/**
 * Registry for custom mob type definitions.
 * Use {@link #register(String, CustomMobManager.CustomMobDefinition)} to add new mob types.
 * Use {@link #get(String)} to look up a type by its ID.
 */
public class CustomMobRegistry {

    private static final Map<String, CustomMobManager.CustomMobDefinition> REGISTRY = new LinkedHashMap<>();

    // ── Registration ─────────────────────────────────────────────

    /**
     * Registers a custom mob definition under the given ID.
     * The ID must be unique and is used in commands (e.g. /custommob spawn zombie_king).
     *
     * @param id  lowercase identifier (e.g. "zombie_king")
     * @param def mob definition produced by {@link CustomMobManager#builder(String)}
     */
    public static void register(String id, CustomMobManager.CustomMobDefinition def) {
        String key = id.toLowerCase(Locale.ROOT);
        if (REGISTRY.containsKey(key)) {
            PoliticalServer.LOGGER.warn("[CustomMobRegistry] Duplicate mob type '{}' – overwriting previous entry.", key);
        }
        REGISTRY.put(key, def);
        PoliticalServer.LOGGER.info("[CustomMobRegistry] Registered custom mob type '{}'.", key);
    }

    // ── Lookup ───────────────────────────────────────────────────

    public static Optional<CustomMobManager.CustomMobDefinition> get(String id) {
        return Optional.ofNullable(REGISTRY.get(id.toLowerCase(Locale.ROOT)));
    }

    public static boolean contains(String id) {
        return REGISTRY.containsKey(id.toLowerCase(Locale.ROOT));
    }

    /** Returns all registered mob IDs in insertion order. */
    public static Set<String> getAllIds() {
        return Collections.unmodifiableSet(REGISTRY.keySet());
    }

    /** Returns all registered mob definitions. */
    public static Collection<CustomMobManager.CustomMobDefinition> getAllDefinitions() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

    // ── Bootstrap ────────────────────────────────────────────────

    /**
     * Called once on server startup to register all built-in custom mob types.
     * Add new mob types here using CustomMobManager.builder(...).
     */
    public static void registerAll() {
        // Example (add real mob definitions here as the mod grows):
        // register("zombie_king", CustomMobManager.builder("Zombie King")
        //         .baseEntity(EntityType.ZOMBIE)
        //         .health(200.0)
        //         .damage(15.0)
        //         .scale(1.5f)
        //         .displayName(Text.literal("☠ Zombie King").formatted(Formatting.DARK_GREEN, Formatting.BOLD))
        //         .drops(List.of(SlayerItems.createCore(SlayerManager.SlayerType.ZOMBIE)))
        //         .requiredSlayerLevel(SlayerManager.SlayerType.ZOMBIE, 5)
        //         .build());
    }
}
