package com.political;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Framework for spawning and managing custom mobs.
 *
 * <h3>Usage example:</h3>
 * <pre>{@code
 * CustomMobManager.CustomMobDefinition def = CustomMobManager.builder("Zombie King")
 *         .baseEntity(EntityType.ZOMBIE)
 *         .health(200.0)
 *         .damage(15.0)
 *         .scale(1.5f)
 *         .displayName(Text.literal("☠ Zombie King").formatted(Formatting.DARK_GREEN, Formatting.BOLD))
 *         .drops(List.of(SlayerItems.createCore(SlayerManager.SlayerType.ZOMBIE)))
 *         .requiredSlayerLevel(SlayerManager.SlayerType.ZOMBIE, 5)
 *         .build();
 *
 * CustomMobRegistry.register("zombie_king", def);
 * }</pre>
 *
 * <h3>Spawning via command:</h3>
 * <pre>{@code
 * /custommob spawn zombie_king
 * }</pre>
 */
public class CustomMobManager {

    // ── Definition ───────────────────────────────────────────────

    /**
     * Immutable description of a custom mob type.
     * Create instances using {@link Builder} via {@link #builder(String)}.
     */
    public static final class CustomMobDefinition {
        public final String internalName;
        public final EntityType<? extends MobEntity> baseEntityType;
        public final double maxHealth;
        public final double attackDamage;
        public final float scale;
        public final Text displayName;
        public final List<ItemStack> drops;
        /** Optional: slayer type required to receive drops/bounty credit. */
        public final SlayerManager.SlayerType requiredSlayerType;
        /** Optional: minimum slayer level required. */
        public final int requiredSlayerLevel;
        /** Optional: extra setup applied to the entity after spawning. */
        public final Consumer<MobEntity> onSpawn;
        /** Optional: called each server tick while the mob is alive. */
        public final BiConsumer<MobEntity, ServerWorld> onTick;

        private CustomMobDefinition(Builder b) {
            this.internalName = b.internalName;
            this.baseEntityType = b.baseEntityType;
            this.maxHealth = b.maxHealth;
            this.attackDamage = b.attackDamage;
            this.scale = b.scale;
            this.displayName = b.displayName;
            this.drops = Collections.unmodifiableList(new ArrayList<>(b.drops));
            this.requiredSlayerType = b.requiredSlayerType;
            this.requiredSlayerLevel = b.requiredSlayerLevel;
            this.onSpawn = b.onSpawn;
            this.onTick = b.onTick;
        }
    }

    // ── Builder ──────────────────────────────────────────────────

    public static Builder builder(String internalName) {
        return new Builder(internalName);
    }

    public static final class Builder {
        private final String internalName;
        @SuppressWarnings("unchecked")
        private EntityType<? extends MobEntity> baseEntityType = (EntityType<? extends MobEntity>) (Object) EntityType.ZOMBIE;
        private double maxHealth = 20.0;
        private double attackDamage = 2.0;
        private float scale = 1.0f;
        private Text displayName;
        private final List<ItemStack> drops = new ArrayList<>();
        private SlayerManager.SlayerType requiredSlayerType = null;
        private int requiredSlayerLevel = 0;
        private Consumer<MobEntity> onSpawn = null;
        private BiConsumer<MobEntity, ServerWorld> onTick = null;

        private Builder(String internalName) {
            this.internalName = internalName;
            this.displayName = Text.literal(internalName);
        }

        public Builder baseEntity(EntityType<? extends MobEntity> type) {
            this.baseEntityType = type;
            return this;
        }

        public Builder health(double hp) {
            this.maxHealth = hp;
            return this;
        }

        public Builder damage(double dmg) {
            this.attackDamage = dmg;
            return this;
        }

        public Builder scale(float s) {
            this.scale = s;
            return this;
        }

        public Builder displayName(Text name) {
            this.displayName = name;
            return this;
        }

        public Builder drops(List<ItemStack> dropList) {
            this.drops.addAll(dropList);
            return this;
        }

        public Builder drop(ItemStack drop) {
            this.drops.add(drop);
            return this;
        }

        public Builder requiredSlayerLevel(SlayerManager.SlayerType type, int level) {
            this.requiredSlayerType = type;
            this.requiredSlayerLevel = level;
            return this;
        }

        public Builder onSpawn(Consumer<MobEntity> callback) {
            this.onSpawn = callback;
            return this;
        }

        public Builder onTick(BiConsumer<MobEntity, ServerWorld> callback) {
            this.onTick = callback;
            return this;
        }

        public CustomMobDefinition build() {
            return new CustomMobDefinition(this);
        }
    }

    // ── Spawn ─────────────────────────────────────────────────────

    /**
     * Spawns a custom mob of the given type at the specified position.
     *
     * @param world     the server world to spawn in
     * @param typeId    the registered mob type ID (e.g. "zombie_king")
     * @param pos       the block position at which to spawn
     * @param summoner  the player who issued the spawn command (may be null)
     * @return the spawned entity, or empty if the type is not registered
     */
    public static Optional<MobEntity> spawnMob(ServerWorld world, String typeId,
                                               BlockPos pos, ServerPlayerEntity summoner) {
        Optional<CustomMobDefinition> opt = CustomMobRegistry.get(typeId);
        if (opt.isEmpty()) {
            if (summoner != null) {
                summoner.sendMessage(Text.literal("✗ Unknown mob type: " + typeId)
                        .formatted(net.minecraft.util.Formatting.RED), false);
            }
            return Optional.empty();
        }
        return spawnMob(world, opt.get(), pos, summoner);
    }

    public static Optional<MobEntity> spawnMob(ServerWorld world, CustomMobDefinition def,
                                               BlockPos pos, ServerPlayerEntity summoner) {
        MobEntity entity = def.baseEntityType.create(world, net.minecraft.entity.SpawnReason.COMMAND);
        if (entity == null) {
            PoliticalServer.LOGGER.warn("[CustomMobManager] Failed to create entity for type '{}'", def.internalName);
            return Optional.empty();
        }

        // Apply stats
        applyStats(entity, def);

        // Position
        entity.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
        entity.setPersistent();

        // Run custom spawn callback
        if (def.onSpawn != null) {
            def.onSpawn.accept(entity);
        }

        world.spawnEntity(entity);

        if (summoner != null) {
            summoner.sendMessage(Text.literal("✓ Spawned " + def.displayName.getString() + "!")
                    .formatted(net.minecraft.util.Formatting.GREEN), false);
        }
        return Optional.of(entity);
    }

    // ── Stat Application ─────────────────────────────────────────

    private static void applyStats(MobEntity entity, CustomMobDefinition def) {
        // Health
        if (entity.getAttributeInstance(EntityAttributes.MAX_HEALTH) != null) {
            Objects.requireNonNull(entity.getAttributeInstance(EntityAttributes.MAX_HEALTH))
                    .setBaseValue(def.maxHealth);
        }
        entity.setHealth((float) def.maxHealth);

        // Damage
        if (entity.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE) != null) {
            Objects.requireNonNull(entity.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE))
                    .setBaseValue(def.attackDamage);
        }

        // Display name
        entity.setCustomName(def.displayName);
        entity.setCustomNameVisible(true);

        // Scale is intentionally not applied here - it requires version-specific NBT
        // manipulation. Override onSpawn() in your mob definition to apply scale via
        // EntityAttributes.SCALE if available on the target entity type.
    }

    // ── Tick ─────────────────────────────────────────────────────

    /**
     * Tick callback for custom mob entities.
     * Call this from your entity mixin or a server tick listener if you need
     * per-tick behaviour for registered custom mobs.
     */
    public static void tick(MobEntity entity, ServerWorld world) {
        if (entity.getCustomName() == null) return;
        String name = entity.getCustomName().getString();
        CustomMobRegistry.getByDisplayName(name).ifPresent(def -> {
            if (def.onTick != null) def.onTick.accept(entity, world);
        });
    }

    // ── Drop Handling ─────────────────────────────────────────────

    /**
     * Returns the drop list for a custom mob that matches the given entity,
     * if the killing player meets the required slayer level.
     */
    public static List<ItemStack> getDrops(LivingEntity entity, ServerPlayerEntity killer) {
        if (entity.getCustomName() == null) return List.of();
        String name = entity.getCustomName().getString();
        Optional<CustomMobDefinition> opt = CustomMobRegistry.getByDisplayName(name);
        if (opt.isEmpty()) return List.of();
        CustomMobDefinition def = opt.get();
        if (def.requiredSlayerType != null && def.requiredSlayerLevel > 0) {
            int level = SlayerData.getSlayerLevel(killer.getUuidAsString(), def.requiredSlayerType);
            if (level < def.requiredSlayerLevel) return List.of();
        }
        List<ItemStack> result = new ArrayList<>();
        for (ItemStack drop : def.drops) {
            result.add(drop.copy());
        }
        return result;
    }
}
