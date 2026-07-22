package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Random;

/**
 * "Wanderers" — the hostile inhabitants of the Backrooms.
 *
 * Spawned programmatically (never via natural spawning, which the dimension_type
 * disables) as tagged husks/silverfish with a darkness aura and scaling tiers.
 */
public final class BackroomsMobs {

    private BackroomsMobs() {}

    public static final String WANDERER_TAG = "backrooms_wanderer";

    /** Spawn a small pack of Wanderers around a dark-cell center. */
    public static void spawnPack(ServerLevel level, BlockPos center, Random rng) {
        int count = 2 + rng.nextInt(3); // 2..4
        for (int i = 0; i < count; i++) {
            int ox = center.getX() + rng.nextInt(9) - 4;
            int oz = center.getZ() + rng.nextInt(9) - 4;
            BlockPos pos = new BlockPos(ox, center.getY(), oz);
            boolean silverfish = rng.nextFloat() < 0.35f;
            spawnWanderer(level, pos, silverfish, rng);
        }
    }

    public static void spawnWanderer(ServerLevel level, BlockPos pos, boolean silverfish, Random rng) {
        EntityType<?> type = silverfish ? EntityType.SILVERFISH : EntityType.HUSK;
        Entity e = type.spawn(level, pos, EntitySpawnReason.EVENT);
        if (!(e instanceof Mob mob)) return;

        mob.addTag(WANDERER_TAG);
        mob.setPersistenceRequired();
        mob.setNoAi(false);

        // Scaling tiers: 0..3 weighted toward weaker.
        int roll = rng.nextInt(100);
        int tier = roll < 55 ? 0 : roll < 80 ? 1 : roll < 95 ? 2 : 3;

        double healthMult = 1.0 + tier * 0.6;
        double damageMult = 1.0 + tier * 0.45;

        var health = mob.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            double nh = health.getBaseValue() * healthMult;
            health.setBaseValue(nh);
            mob.setHealth((float) nh);
        }
        var attack = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) {
            attack.setBaseValue(attack.getBaseValue() * damageMult);
        }

        // Feed the HealthScalingManager tier so kill bonuses / progression pick it up.
        HealthScalingManager.setMobTier(mob, tier);

        String stars = "✦".repeat(Math.max(1, tier + 1));
        ChatFormatting color = switch (tier) {
            case 0 -> ChatFormatting.GRAY;
            case 1 -> ChatFormatting.YELLOW;
            case 2 -> ChatFormatting.GOLD;
            default -> ChatFormatting.RED;
        };
        mob.setCustomName(Component.literal(stars + " Wanderer").withStyle(color));
        mob.setCustomNameVisible(false);
    }

    public static boolean isWanderer(Entity e) {
        return e.entityTags().contains(WANDERER_TAG);
    }

    // ── Drops ─────────────────────────────────────────────────────────────────

    /**
     * Registers the Wanderer death-drop handler. Call once at mod init.
     *
     * <p>Wanderers previously dropped nothing but vanilla husk/silverfish loot, which meant
     * Fluorescent Fragment (a found-only crafting material) was obtainable exclusively from
     * stash-cell chests. Killing Wanderers is now the repeatable source.
     */
    public static void registerDrops() {
        net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity == null) return;
            if (!isWanderer(entity)) return;
            if (!(entity.level() instanceof ServerLevel level)) return;
            dropWandererLoot(level, entity);
        });
    }

    /**
     * Tier-scaled Backrooms loot.
     *
     * <p>Fluorescent Fragment is the bread-and-butter drop (always at least one, more from
     * higher tiers). Almond Water is uncommon, Reality Shard is rare and only from tier 2+.
     */
    public static void dropWandererLoot(ServerLevel level, LivingEntity mob) {
        int tier = Math.max(0, Math.min(3, HealthScalingManager.getMobTier(mob)));
        var rng = level.getRandom();

        // Fluorescent Fragment: 1..2 base, +1 guaranteed per tier.
        int fragments = 1 + rng.nextInt(2) + tier;
        dropStack(level, mob, BackroomsItems.createFluorescentFragment(fragments));

        // Almond Water: 12% at tier 0, scaling to roughly 39% at tier 3.
        if (rng.nextFloat() < 0.12f + tier * 0.09f) {
            dropStack(level, mob, BackroomsItems.createAlmondWater(1));
        }

        // Reality Shard: tier 2+ only, 6% / 14%.
        if (tier >= 2 && rng.nextFloat() < (tier == 3 ? 0.14f : 0.06f)) {
            dropStack(level, mob, BackroomsItems.createRealityShard(1));
        }
    }

    private static void dropStack(ServerLevel level, LivingEntity mob, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        net.minecraft.world.level.block.Block.popResource(level, mob.blockPosition(), stack);
    }

    /**
     * Darkness aura: if any Wanderer is close to the player, refresh a short Darkness
     * effect. Called each tick from BackroomsManager for players in the dimension.
     */
    public static void applyDarknessAura(ServerLevel level, ServerPlayer player) {
        List<Mob> nearby = level.getEntitiesOfClass(
                Mob.class,
                player.getBoundingBox().inflate(10.0),
                BackroomsMobs::isWanderer);
        if (nearby.isEmpty()) return;

        boolean close = false;
        for (Mob m : nearby) {
            if (m.distanceToSqr(player) <= 7.0 * 7.0) { close = true; break; }
        }
        if (close) {
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 80, 0, true, false, false));
        }
    }
}
