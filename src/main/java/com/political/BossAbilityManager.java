package com.political;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.Set;
import java.util.HashSet;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.monster.spider.CaveSpider;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.BossEvent;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.*;


public class BossAbilityManager {

    // ============================================================
    // STATE TRACKING MAPS
    // ============================================================

    private static final Map<UUID, Long> lastAbilityUse = new HashMap<>();
    private static final Map<UUID, Map<String, Long>> abilityCooldowns = new HashMap<>();
    private static final Map<UUID, Integer> bossPhases = new HashMap<>();
    private static final Map<UUID, Integer> minionCounts = new HashMap<>();
    private static final Map<UUID, Set<UUID>> bossMinions = new HashMap<>();
    private static final Map<UUID, BossAbilityState> bossAbilityData = new HashMap<>();
    private static final Map<UUID, ServerBossEvent> splitBossBars = new HashMap<>();
    // Special state tracking
    private static final Set<UUID> enragedBosses = new HashSet<>();
    private static final Map<UUID, Long> enrageEndTimes = new HashMap<>();
    private static final Map<UUID, Long> phasedBosses = new HashMap<>();
    private static final Map<UUID, Long> shieldedBosses = new HashMap<>();
    private static final Map<UUID, DeathMark> deathMarkedPlayers = new HashMap<>();
    private static final Map<UUID, VoidZone> voidZones = new HashMap<>();
    private static final Map<UUID, AbyssalRoar> abyssalRoarTargets = new HashMap<>();
    private static final Map<UUID, SculkInfestation> sculkInfestations = new HashMap<>();
    private static final List<ScheduledBlockRemoval> scheduledBlockRemovals = new ArrayList<>();

    // ============================================================
    // HELPER METHOD - Get Vec3 from entity
    // ============================================================
// Slime boss split tracking
    private static final Map<UUID, Set<UUID>> slimeSplitChildren = new HashMap<>();  // root boss -> all children
    private static final Map<UUID, UUID> slimeSplitParent = new HashMap<>();  // child -> its parent
    private static final Map<UUID, Integer> slimeSplitLevel = new HashMap<>();  // boss -> split level (0=main, 1=first split, 2=final)
    private static Vec3 getEntityPos(Entity entity) {
        return new Vec3(entity.getX(), entity.getY(), entity.getZ());
    }


    // ============================================================
    // MAIN TICK METHOD
    // ============================================================
    public static void removeBoss(UUID bossId) {
        cleanup(bossId);
        undyingRageCooldowns.remove(bossId);
        undyingRageEndTimes.remove(bossId);
        undyingRageUsed.remove(bossId);
    }
    public static void tickBossAbilities(LivingEntity boss, ServerPlayer target,
                                         SlayerManager.SlayerType type, int tier) {
        if (!(boss.level() instanceof ServerLevel world)) return;

        // Tick Piglin barter shields (remove invulnerability when expired)
        if (type == SlayerManager.SlayerType.PIGLIN) {
            tickPiglinShields(world);
        }

        if (type == SlayerManager.SlayerType.IRON_GOLEM && boss instanceof Warden warden) {
            warden.increaseAngerAt(target, 80, true);
            if (warden.getTarget() == null) {
                warden.setTarget(target);
            }
        }
        if (type == SlayerManager.SlayerType.ZOMBIE) {
            tickUndyingRage(boss, world);

            // Try to activate if health is low and not on cooldown
            float healthPercent = boss.getHealth() / boss.getMaxHealth();
            if (healthPercent < 0.3f && !isInUndyingRage(boss)) {
                activateUndyingRage(boss, world);
            }

        }
        if (boss == null || !boss.isAlive() || target == null) return;
        if (!(boss.level() instanceof ServerLevel bossWorld)) return;

        // DELETE THE SECOND IRON_GOLEM CHECK THAT USES "player" - IT'S A DUPLICATE

        UUID bossId = boss.getUUID();
        long now = System.currentTimeMillis();
        ServerBossEvent splitBar = splitBossBars.get(bossId);
        if (splitBar != null) {
            splitBar.setProgress(boss.getHealth() / boss.getMaxHealth());
        }
        updatePhase(boss, bossId, target, type, tier, world);
        tickPassiveRegen(boss, tier, world);

        long lastAbility = lastAbilityUse.getOrDefault(bossId, 0L);
        long cooldown = getAbilityCooldown(tier);

        if (now - lastAbility < cooldown) return;
        if (boss.getRandom().nextFloat() > 0.6f) return;

        boolean used = switch (type) {
            case ZOMBIE -> useZombieAbility(boss, target, tier, world);
            case SPIDER -> useSpiderAbility(boss, target, tier);
            case SKELETON -> useSkeletonAbility(boss, target, tier);
            case SLIME -> useSlimeAbility(boss, target, tier);
            case ENDERMAN -> useEndermanAbility(boss, target, tier);
            case IRON_GOLEM -> useWardenAbility(boss, target, tier);
            case PIGLIN -> usePiglinAbility(boss, target, tier, world);
        };

        if (used) {
            lastAbilityUse.put(bossId, now);
        }

    }

    private static long getAbilityCooldown(int tier) {
        return switch (tier) {
            case 1 -> 20000L;
            case 2 -> 16000L;
            case 3 -> 14000L;
            case 4 -> 12000L;
            case 5 -> 10000L;
            default -> 20000L;
        };
    }
// ============================================================
// UNDYING RAGE - Zombie Boss Invincibility
// ============================================================

    private static final long UNDYING_RAGE_DURATION_MS = 8000;  // 8 seconds of invincibility
    private static final Map<UUID, Long> undyingRageEndTimes = new HashMap<>();
    // Tracks bosses that have already used Undying Rage (once per fight only)
    private static final Set<UUID> undyingRageUsed = new HashSet<>();
    // Keep this for compatibility with removeBoss()
    private static final Map<UUID, Long> undyingRageCooldowns = new HashMap<>();

    /**
     * Activate Undying Rage for zombie boss - triggers once per fight at 30% HP
     */
    public static boolean activateUndyingRage(LivingEntity boss, ServerLevel world) {
        UUID bossId = boss.getUUID();

        // Only trigger once per fight
        if (undyingRageUsed.contains(bossId)) {
            return false;
        }

        undyingRageUsed.add(bossId);
        long now = System.currentTimeMillis();
        undyingRageEndTimes.put(bossId, now + UNDYING_RAGE_DURATION_MS);
        undyingRageCooldowns.put(bossId, now);

        // Add to enragedBosses for true invincibility via modifyIncomingDamage
        enragedBosses.add(bossId);
        enrageEndTimes.put(bossId, now + UNDYING_RAGE_DURATION_MS);

        // Visual effects
        boss.addEffect(new MobEffectInstance(
                MobEffects.GLOWING, 160, 0, true, true, true
        ));
        boss.addEffect(new MobEffectInstance(
                MobEffects.STRENGTH, 160, 1, true, true, true
        ));
        boss.addEffect(new MobEffectInstance(
                MobEffects.SPEED, 160, 1, true, true, true
        ));

        // Heal 5% HP
        boss.heal(boss.getMaxHealth() * 0.05f);

        // Notify nearby players
        String bossName = boss.hasCustomName() ? boss.getCustomName().getString() : "The Boss";
        for (ServerPlayer player : world.players()) {
            if (player.distanceToSqr(boss) < 2500) { // 50 blocks
                player.sendSystemMessage(Component.literal("§4§l☠ UNDYING RAGE! §r§c" + bossName + " §cis temporarily INVINCIBLE!")
                        .withStyle(ChatFormatting.DARK_RED), false);
                player.sendSystemMessage(Component.literal("§7(Stay back for §e8 seconds§7 or deal no damage!)")
                        .withStyle(ChatFormatting.GRAY), false);
            }
        }

        // Sound
        world.playSound(null, boss.blockPosition(),
                SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.5f, 0.5f);

        return true;
    }

    /**
     * Check if boss is currently in Undying Rage
     */
    public static boolean isInUndyingRage(LivingEntity boss) {
        Long endTime = undyingRageEndTimes.get(boss.getUUID());
        if (endTime == null) return false;
        return System.currentTimeMillis() < endTime;
    }

    /**
     * Tick Undying Rage - call every tick for active bosses
     */
    public static void tickUndyingRage(LivingEntity boss, ServerLevel world) {
        UUID bossId = boss.getUUID();
        Long endTime = undyingRageEndTimes.get(bossId);

        if (endTime == null) return;

        long now = System.currentTimeMillis();

        // Check if rage just ended
        if (now >= endTime) {
            undyingRageEndTimes.remove(bossId);
            // enragedBosses cleanup is handled by tickEnrageEffects

            // Remove strength/speed but keep boss alive
            boss.removeEffect(MobEffects.STRENGTH);
            boss.removeEffect(MobEffects.SPEED);

            // Notify nearby players that it ended
            String bossName = boss.hasCustomName() ? boss.getCustomName().getString() : "The Boss";
            for (ServerPlayer player : world.players()) {
                if (player.distanceToSqr(boss) < 2500) { // 50 blocks
                    player.sendSystemMessage(Component.literal("§a§l✓ " + bossName + "'s §a§lUndying Rage has ENDED! Attack now!")
                            .withStyle(ChatFormatting.GREEN), false);
                }
            }

            // Sound indicating vulnerability
            world.playSound(null, boss.blockPosition(),
                    SoundEvents.BEACON_DEACTIVATE, SoundSource.HOSTILE, 1.0f, 1.0f);
        }
    }

    // ============================================================
    // PHASE SYSTEM
    // ============================================================

    private static void updatePhase(LivingEntity boss, UUID bossId, ServerPlayer owner,
                                    SlayerManager.SlayerType type, int tier, ServerLevel world) {
        float healthPercent = boss.getHealth() / boss.getMaxHealth();
        int currentPhase = bossPhases.getOrDefault(bossId, 1);
        int newPhase;

        if (healthPercent > 0.75f) newPhase = 1;
        else if (healthPercent > 0.50f) newPhase = 2;
        else if (healthPercent > 0.25f) newPhase = 3;
        else newPhase = 4;

        if (newPhase != currentPhase) {
            bossPhases.put(bossId, newPhase);
            onPhaseChange(boss, owner, newPhase, type, tier, world);
        }
    }

    private static void onPhaseChange(LivingEntity boss, ServerPlayer owner, int newPhase,
                                      SlayerManager.SlayerType type, int tier, ServerLevel world) {
        String phaseMessage = switch (newPhase) {
            case 2 -> "§e⚠ " + type.bossName + " grows angry!";
            case 3 -> "§c⚠ " + type.bossName + " enters a frenzy!";
            case 4 -> "§4§l⚠ " + type.bossName + " IS ENRAGED! ⚠";
            default -> null;
        };

        if (phaseMessage != null && owner != null) {
            owner.sendSystemMessage(Component.literal(phaseMessage), false);
            world.playSound(null, boss, SoundEvents.WITHER_AMBIENT,
                    SoundSource.HOSTILE, 1.5f, 0.5f);
            world.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                    boss.getX(), boss.getY() + boss.getBbHeight(), boss.getZ(),
                    5, 0.6, 0.5, 0.6, 0.1);
        }

        if (newPhase == 4 && tier >= 3) {
            boss.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 6000, tier - 1, false, true));
            boss.addEffect(new MobEffectInstance(MobEffects.SPEED, 6000, 1, false, true));
        }
    }

    // ============================================================
    // PASSIVE ABILITIES
    // ============================================================

    private static void tickPassiveRegen(LivingEntity boss, int tier, ServerLevel world) {
        if (world.getServer().getTickCount() % 40 != 0) return;

        float regenAmount = switch (tier) {
            case 1 -> 0.5f;
            case 2 -> 1.0f;
            case 3 -> 2.0f;
            case 4 -> 4.0f;
            case 5 -> 8.0f;
            default -> 0.5f;
        };

        boss.setHealth(Math.min(boss.getMaxHealth(), boss.getHealth() + regenAmount));

        if (world.getServer().getTickCount() % 80 == 0) {
            world.sendParticles(ParticleTypes.HEART,
                    boss.getX(), boss.getY() + boss.getBbHeight() + 0.5, boss.getZ(),
                    3, 0.5, 0.3, 0.5, 0.0);
        }
    }

    public static void handleBossLifesteal(LivingEntity boss, float damageDealt, int tier) {
        if (tier < 3) return;

        float lifestealPercent = switch (tier) {
            case 3 -> 0.02f;
            case 4 -> 0.03f;
            case 5 -> 0.05f;
            default -> 0f;
        };

        float healAmount = damageDealt * lifestealPercent;
        if (healAmount > 0) {
            boss.heal(healAmount);
            if (boss.level() instanceof ServerLevel serverWorld) {
                serverWorld.sendParticles(ParticleTypes.HEART,
                        boss.getX(), boss.getY() + boss.getBbHeight(), boss.getZ(),
                        3, 0.3, 0.3, 0.3, 0.0);
            }
        }
    }

    // ============================================================
    // ZOMBIE ABILITIES
    // ============================================================

    private static boolean useZombieAbility(LivingEntity boss, ServerPlayer target, int tier, ServerLevel world) {
        return switch (tier) {
            case 1, 2 -> zombieGroan(boss, target, world);
            case 3 -> zombieSummonMinions(boss, target, world);
            case 4, 5 -> zombiePestilence(boss, target, world);
            default -> false;
        };
        // NOTE: Undying Rage is triggered automatically via tickBossAbilities when health < 30%
    }

    private static boolean zombieGroan(LivingEntity boss, ServerPlayer target, ServerLevel world) {
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));
        world.playSound(null, boss, SoundEvents.ZOMBIE_AMBIENT,
                SoundSource.HOSTILE, 2.0f, 0.5f);
        target.sendSystemMessage(Component.literal("☠ The Undying Outlaw lets out a chilling groan!")
                .withStyle(ChatFormatting.DARK_GREEN), true);
        return true;
    }

    private static boolean zombieSummonMinions(LivingEntity boss, ServerPlayer target, ServerLevel world) {
        int count = 3;
        for (int i = 0; i < count; i++) {
            Entity minionEnt = EntityType.ZOMBIE.create(world, EntitySpawnReason.MOB_SUMMONED);
            if (minionEnt instanceof Zombie minion) {
                double angle = (2 * Math.PI / count) * i;
                double x = boss.getX() + Math.cos(angle) * 3;
                double z = boss.getZ() + Math.sin(angle) * 3;
                minion.setPos(x, boss.getY(), z);
                minion.setCustomName(Component.literal("§2Undead Minion"));

                var healthAttr = minion.getAttribute(Attributes.MAX_HEALTH);
                if (healthAttr != null) {
                    healthAttr.setBaseValue(40);
                    minion.setHealth(40);
                }
                world.addFreshEntity(minion);
                world.sendParticles(ParticleTypes.SOUL, x, boss.getY() + 1, z, 4, 0.3, 0.5, 0.3, 0.02);
            }
        }
        world.playSound(null, boss, SoundEvents.ZOMBIE_VILLAGER_CONVERTED,
                SoundSource.HOSTILE, 2.0f, 0.7f);
        target.sendSystemMessage(Component.literal("☠ The Undying Outlaw summons reinforcements!")
                .withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD), true);
        return true;
    }

    private static boolean zombiePestilence(LivingEntity boss, ServerPlayer target, ServerLevel world) {
        AABB area = boss.getBoundingBox().inflate(8);
        for (ServerPlayer player : world.getEntitiesOfClass(ServerPlayer.class, area, p -> true)) {
            player.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 200, 2));
        }
        for (int i = 0; i < 36; i++) {
            double angle = (2 * Math.PI / 36) * i;
            double x = boss.getX() + Math.cos(angle) * 8;
            double z = boss.getZ() + Math.sin(angle) * 8;
            world.sendParticles(ParticleTypes.ITEM_SLIME, x, boss.getY() + 0.5, z, 5, 0.2, 0.2, 0.2, 0.01);
        }
        world.playSound(null, boss, SoundEvents.HUSK_AMBIENT,
                SoundSource.HOSTILE, 2.0f, 0.5f);
        target.sendSystemMessage(Component.literal("☣ Pestilence spreads from the Undying Outlaw!")
                .withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD), true);
        return true;
    }

    // Tier 5: Undying Rage - temporary invincibility + damage boost
    private static boolean zombieUndyingRage(LivingEntity boss, ServerPlayer target, ServerLevel world) {
        UUID bossId = boss.getUUID();

        // Mark as enraged
        enragedBosses.add(bossId);

        // Visual effects
        boss.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
        boss.addEffect(new MobEffectInstance(MobEffects.SPEED, 100, 2));

        // Schedule rage end
        enrageEndTimes.put(bossId, System.currentTimeMillis() + 5000);

        // Particles
        world.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                boss.getX(), boss.getY() + 1, boss.getZ(),
                5, 0.5, 0.5, 0.5, 0.1);

        world.playSound(null, boss, SoundEvents.RAVAGER_ROAR,
                SoundSource.HOSTILE, 2.0f, 0.6f);

        target.sendSystemMessage(Component.literal("💀 UNDYING RAGE! The boss is temporarily invincible!")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), true);

        return true;
    }

    // ============================================================
    // SPIDER ABILITIES
    // ============================================================

    private static boolean useSpiderAbility(LivingEntity boss, ServerPlayer target, int tier) {
        if (!(boss.level() instanceof ServerLevel world)) return false;

        return switch (tier) {
            case 1, 2 -> spiderWebShot(boss, target, world);
            case 3 -> spiderVenomSpit(boss, target, world);
            case 4 -> spiderAmbush(boss, target, world);
            case 5 -> spiderBroodMother(boss, target, world);
            default -> false;
        };
    }

    private static boolean spiderWebShot(LivingEntity boss, ServerPlayer target, ServerLevel world) {
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 3));

        BlockPos playerPos = target.blockPosition();
        if (world.getBlockState(playerPos).isAir()) {
            world.setBlock(playerPos, net.minecraft.world.level.block.Blocks.COBWEB.defaultBlockState(), Block.UPDATE_ALL);
            scheduleBlockRemoval(world, playerPos, 60);
        }

        world.playSound(null, boss, SoundEvents.SPIDER_AMBIENT,
                SoundSource.HOSTILE, 2.0f, 1.2f);

        target.sendSystemMessage(Component.literal("🕸 The Venomous Bandit traps you in webs!")
                .withStyle(ChatFormatting.DARK_RED), true);

        return true;
    }

    private static boolean spiderVenomSpit(LivingEntity boss, ServerPlayer target, ServerLevel world) {
        target.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 2));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
        target.hurtServer(world, boss.damageSources().magic(), 8.0f);

        Vec3 start = new Vec3(boss.getX(), boss.getY() + boss.getBbHeight() / 2, boss.getZ());
        Vec3 end = new Vec3(target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ());
        Vec3 dir = end.subtract(start).normalize();
        double dist = start.distanceTo(end);

        for (double d = 0; d < dist; d += 0.5) {
            Vec3 pos = start.add(dir.scale(d));
            world.sendParticles(ParticleTypes.ITEM_SLIME, pos.x, pos.y, pos.z,
                    2, 0.1, 0.1, 0.1, 0.01);
        }

        world.playSound(null, boss, SoundEvents.LLAMA_SPIT,
                SoundSource.HOSTILE, 2.0f, 0.5f);

        target.sendSystemMessage(Component.literal("☠ Venom courses through your veins!")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), true);

        return true;
    }

    private static boolean spiderAmbush(LivingEntity boss, ServerPlayer target, ServerLevel world) {
        Vec3 targetPos = new Vec3(target.getX(), target.getY(), target.getZ());
        Vec3 behindPlayer = targetPos.subtract(target.getViewVector(1.0f).scale(3));

        world.sendParticles(ParticleTypes.LARGE_SMOKE,
                boss.getX(), boss.getY(), boss.getZ(),
                5, 0.5, 0.5, 0.5, 0.02);

        boss.teleportTo(behindPlayer.x, behindPlayer.y, behindPlayer.z);

        world.sendParticles(ParticleTypes.LARGE_SMOKE,
                behindPlayer.x, behindPlayer.y, behindPlayer.z,
                5, 0.5, 0.5, 0.5, 0.02);

        target.hurtServer(world, boss.damageSources().mobAttack(boss), 15.0f);

        world.playSound(null, boss, SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.HOSTILE, 1.5f, 1.5f);

        target.sendSystemMessage(Component.literal("⚡ The spider ambushes you from behind!")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), true);

        return true;
    }

    private static boolean spiderBroodMother(LivingEntity boss, ServerPlayer target, ServerLevel world) {
        int count = 8;

        for (int i = 0; i < count; i++) {
            Entity spiderEnt = EntityType.CAVE_SPIDER.create(world, EntitySpawnReason.MOB_SUMMONED);
            if (spiderEnt instanceof CaveSpider spider) {
                double angle = (2 * Math.PI / count) * i;
                double radius = 2 + boss.getRandom().nextDouble() * 2;
                double x = boss.getX() + Math.cos(angle) * radius;
                double z = boss.getZ() + Math.sin(angle) * radius;

                spider.setPos(x, boss.getY(), z);
                spider.setCustomName(Component.literal("§4Venomous Spawn"));

                var healthAttr = spider.getAttribute(Attributes.MAX_HEALTH);
                if (healthAttr != null) {
                    healthAttr.setBaseValue(30);
                    spider.setHealth(30);
                }

                world.addFreshEntity(spider);
            }
        }

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (boss.getRandom().nextFloat() < 0.3f) {
                    BlockPos webPos = target.blockPosition().offset(dx, 0, dz);
                    if (world.getBlockState(webPos).isAir()) {
                        world.setBlock(webPos, net.minecraft.world.level.block.Blocks.COBWEB.defaultBlockState(), Block.UPDATE_ALL);
                        scheduleBlockRemoval(world, webPos, 100);
                    }
                }
            }
        }

        world.playSound(null, boss, SoundEvents.SPIDER_DEATH,
                SoundSource.HOSTILE, 2.0f, 0.5f);

        target.sendSystemMessage(Component.literal("🕷 THE BROOD AWAKENS! Spiders swarm from all directions!")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), true);

        return true;
    }

    // ============================================================
    // SKELETON ABILITIES
    // ============================================================

    private static boolean useSkeletonAbility(LivingEntity boss, ServerPlayer target, int tier) {
        if (!(boss.level() instanceof ServerLevel world)) return false;

        return switch (tier) {
            case 1, 2 -> skeletonBoneRattle(boss, target, world);
            case 3 -> skeletonArrowBarrage(boss, target, world);
            case 4 -> skeletonBoneShield(boss, target, world);
            case 5 -> skeletonDeathMark(boss, target, world);
            default -> false;
        };
    }

    private static boolean skeletonBoneRattle(LivingEntity boss, ServerPlayer target, ServerLevel world) {
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
        target.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 80, 0));

        world.playSound(null, boss, SoundEvents.SKELETON_AMBIENT,
                SoundSource.HOSTILE, 2.0f, 0.5f);
        world.playSound(null, boss, SoundEvents.SKELETON_AMBIENT,
                SoundSource.HOSTILE, 2.0f, 1.5f);

        target.sendSystemMessage(Component.literal("💀 The Bone Desperado rattles its bones menacingly!")
                .withStyle(ChatFormatting.WHITE), true);

        return true;
    }

    private static boolean skeletonArrowBarrage(LivingEntity boss, ServerPlayer target, ServerLevel world) {
        int arrowCount = 8;

        for (int i = 0; i < arrowCount; i++) {
            net.minecraft.world.entity.projectile.arrow.Arrow arrow = new net.minecraft.world.entity.projectile.arrow.Arrow(
                    world, boss, new ItemStack(Items.ARROW), null);

            Vec3 targetPos = new Vec3(target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ());
            Vec3 bossPos = new Vec3(boss.getX(), boss.getY() + boss.getBbHeight() / 2, boss.getZ());
            Vec3 toTarget = targetPos.subtract(bossPos);

            double spread = 0.15;
            double dx = toTarget.x + (boss.getRandom().nextDouble() - 0.5) * spread * toTarget.length();
            double dy = toTarget.y + (boss.getRandom().nextDouble() - 0.5) * spread * toTarget.length();
            double dz = toTarget.z + (boss.getRandom().nextDouble() - 0.5) * spread * toTarget.length();

            arrow.shoot(dx, dy + 0.1, dz, 2.0f, 0.0f);
            arrow.setBaseDamage(6.0);
            arrow.setCritArrow(true);

            world.addFreshEntity(arrow);
        }

        world.playSound(null, boss, SoundEvents.ARROW_SHOOT,
                SoundSource.HOSTILE, 2.0f, 0.8f);

        target.sendSystemMessage(Component.literal("🏹 Arrow barrage incoming!")
                .withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD), true);

        return true;
    }

    private static boolean skeletonBoneShield(LivingEntity boss, ServerPlayer target, ServerLevel world) {
        UUID bossId = boss.getUUID();

        shieldedBosses.put(bossId, System.currentTimeMillis() + 5000);

        boss.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 100, 3));
        boss.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));

        for (int i = 0; i < 20; i++) {
            double angle = (2 * Math.PI / 20) * i;
            double x = boss.getX() + Math.cos(angle) * 1.5;
            double z = boss.getZ() + Math.sin(angle) * 1.5;
            world.sendParticles(ParticleTypes.WHITE_ASH, x, boss.getY() + 1, z,
                    5, 0.1, 0.5, 0.1, 0.01);
        }

        world.playSound(null, boss, SoundEvents.SHIELD_BLOCK.value(),
                SoundSource.HOSTILE, 2.0f, 0.5f);
        target.sendSystemMessage(Component.literal("🛡 The Bone Desperado summons a shield of bones!")
                .withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD), true);

        return true;
    }

    private static boolean skeletonDeathMark(LivingEntity boss, ServerPlayer target, ServerLevel world) {
        UUID targetId = target.getUUID();

        deathMarkedPlayers.put(targetId, new DeathMark(boss.getUUID(), System.currentTimeMillis() + 3000, 50.0f));

        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0));

        world.playSound(null, target, SoundEvents.WITHER_SPAWN,
                SoundSource.HOSTILE, 0.5f, 2.0f);

        target.sendSystemMessage(Component.literal("☠ YOU HAVE BEEN MARKED FOR DEATH!")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), false);
        target.sendSystemMessage(Component.literal("  Move away from the boss in 3 seconds or take massive damage!")
                .withStyle(ChatFormatting.RED), false);

        return true;
    }

    // ============================================================
    // SLIME ABILITIES
    // ============================================================

    private static boolean useSlimeAbility(LivingEntity boss, ServerPlayer target, int tier) {
        if (!(boss.level() instanceof ServerLevel world)) return false;

        return switch (tier) {
            case 1, 2 -> slimeBounce(boss, target, world);
            case 3 -> slimeSplit(boss, target, world);
            case 4 -> slimeAbsorb(boss, target, world);
            case 5 -> slimeMegaBounce(boss, target, world);
            default -> false;
        };
    }

    private static boolean slimeBounce(LivingEntity boss, ServerPlayer target, ServerLevel world) {
        if (boss.distanceToSqr(target) < 25) {
            target.setDeltaMovement(target.getDeltaMovement().add(0, 1.5, 0));
            target.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(target));
            target.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 60, 0));
        }

        world.playSound(null, boss, SoundEvents.SLIME_JUMP,
                SoundSource.HOSTILE, 2.0f, 0.5f);

        target.sendSystemMessage(Component.literal("🟢 The Gelatinous Rustler bounces you into the air!")
                .withStyle(ChatFormatting.GREEN), true);

        return true;
    }

    // Tier 3: Split - creates smaller slimes
    private static boolean slimeSplit(LivingEntity boss, ServerPlayer target, ServerLevel world) {
        int count = 4;

        for (int i = 0; i < count; i++) {
            Slime slime = EntityType.SLIME.create(world, EntitySpawnReason.MOB_SUMMONED);
            if (slime != null) {
                double angle = (2 * Math.PI / count) * i;
                double x = boss.getX() + Math.cos(angle) * 2;
                double z = boss.getZ() + Math.sin(angle) * 2;

                slime.setPos(x, boss.getY(), z);
                slime.setSize(2, true); // Medium slime
                slime.setCustomName(Component.literal("§aGelatinous Fragment"));

                world.addFreshEntity(slime);

                // Launch them outward
                slime.setDeltaMovement(Math.cos(angle) * 0.5, 0.5, Math.sin(angle) * 0.5);
            }
        }

        world.playSound(null, boss, SoundEvents.SLIME_SQUISH,
                SoundSource.HOSTILE, 2.0f, 0.5f);

        target.sendSystemMessage(Component.literal("🟢 The slime splits into fragments!")
                .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), true);

        return true;
    }

    // Tier 4: Absorb - pulls player in and damages
    private static boolean slimeAbsorb(LivingEntity boss, ServerPlayer target, ServerLevel world) {
        Vec3 bossPos = new Vec3(boss.getX(), boss.getY(), boss.getZ());
        Vec3 targetPos = new Vec3(target.getX(), target.getY(), target.getZ());
        Vec3 pullDir = bossPos.subtract(targetPos).normalize().scale(1.5);
        target.setDeltaMovement(pullDir);
        target.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(target));

        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 2));
        target.hurtServer(world, boss.damageSources().magic(), 10.0f);

        // Particle suction effect
        for (int i = 0; i < 20; i++) {
            double t = i / 20.0;
            Vec3 particleTargetPos = new Vec3(target.getX(), target.getY(), target.getZ());
            Vec3 particleBossPos = new Vec3(boss.getX(), boss.getY(), boss.getZ());
            Vec3 pos = particleTargetPos.lerp(particleBossPos, t);
            world.sendParticles(ParticleTypes.ITEM_SLIME, pos.x, pos.y + 1, pos.z,
                    2, 0.1, 0.1, 0.1, 0.01);
        }

        world.playSound(null, boss, SoundEvents.SLIME_ATTACK,
                SoundSource.HOSTILE, 2.0f, 0.3f);

        target.sendSystemMessage(Component.literal("🟢 The Rustler tries to absorb you!")
                .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), true);

        return true;
    }

    // Tier 5: Mega Bounce - massive AoE knockback + damage
    private static boolean slimeMegaBounce(LivingEntity boss, ServerPlayer target, ServerLevel world) {
        AABB area = boss.getBoundingBox().inflate(10);

        // Jump animation
        boss.setDeltaMovement(0, 2, 0);

        // Delayed ground pound effect
        for (ServerPlayer player : world.getEntitiesOfClass(ServerPlayer.class, area, p -> true)) {
            double dist = player.distanceToSqr(boss);
            if (dist < 100) { // 10 blocks
                double knockback = 2.0 * (1 - dist / 100);
                Vec3 playerPos = new Vec3(player.getX(), player.getY(), player.getZ());
                Vec3 bossPos = new Vec3(boss.getX(), boss.getY(), boss.getZ());
                Vec3 away = playerPos.subtract(bossPos).normalize().scale(knockback);
                player.setDeltaMovement(away.add(0, 1, 0));
                player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(player));
                player.hurtServer(world, boss.damageSources().mobAttack(boss), 20.0f);
            }
        }

        // Ground pound particles
        for (int i = 0; i < 36; i++) {
            double angle = (2 * Math.PI / 36) * i;
            for (double r = 1; r <= 10; r += 2) {
                double x = boss.getX() + Math.cos(angle) * r;
                double z = boss.getZ() + Math.sin(angle) * r;
                world.sendParticles(ParticleTypes.ITEM_SLIME, x, boss.getY() + 0.5, z,
                        3, 0.2, 0.1, 0.2, 0.05);
            }
        }

        world.playSound(null, boss, SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.HOSTILE, 2.0f, 0.5f);

        target.sendSystemMessage(Component.literal("💥 MEGA BOUNCE! The ground shakes violently!")
                .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), true);

        return true;
    }

    // ============================================================
    // ENDERMAN ABILITIES
    // ============================================================

    private static boolean useEndermanAbility(LivingEntity boss, ServerPlayer target, int tier) {
        if (!(boss.level() instanceof ServerLevel world)) return false;

        return switch (tier) {
            case 1, 2 -> endermanBlink(boss, target, world);
            case 3 -> endermanVoidGaze(boss, target, world);
            case 4 -> endermanPhaseShift(boss, target, world);
            case 5 -> endermanVoidRift(boss, target, world);
            default -> false;
        };
    }

    // Tier 1-2: Blink - teleports to random position near player
    // Tier 1-2: Blink - teleports to random position near player
    private static boolean endermanBlink(LivingEntity boss, ServerPlayer target, ServerLevel world) {
        double angle = boss.getRandom().nextDouble() * Math.PI * 2;
        double dist = 3 + boss.getRandom().nextDouble() * 3;
        double x = target.getX() + Math.cos(angle) * dist;
        double z = target.getZ() + Math.sin(angle) * dist;

        // Particles at old location
        world.sendParticles(ParticleTypes.PORTAL,
                boss.getX(), boss.getY() + 1, boss.getZ(),
                8, 0.5, 0.6, 0.5, 0.1);

        // Teleport
        boss.teleportTo(x, target.getY(), z);

        // Particles at new location
        world.sendParticles(ParticleTypes.PORTAL,
                x, target.getY() + 1, z,
                30, 0.5, 1, 0.5, 0.1);

        world.playSound(null, boss, SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.HOSTILE, 2.0f, 1.0f);

        target.sendSystemMessage(Component.literal("⟐ The Void Phantom blinks through reality!")
                .withStyle(ChatFormatting.DARK_PURPLE), true);

        return true;
    }

    // Tier 3: Void Gaze - forces player to look at boss, damages if they do
    private static boolean endermanVoidGaze(LivingEntity boss, ServerPlayer target, ServerLevel world) {
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
        target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0));

        target.hurtServer(world, boss.damageSources().magic(), 12.0f);

        // Eye particles
        world.sendParticles(ParticleTypes.WITCH,
                boss.getX(), boss.getY() + boss.getBbHeight() - 0.5, boss.getZ(),
                5, 0.2, 0.2, 0.2, 0.05);

        world.playSound(null, boss, SoundEvents.ENDERMAN_STARE,
                SoundSource.HOSTILE, 2.0f, 0.5f);

        target.sendSystemMessage(Component.literal("👁 The Void Phantom's gaze pierces your soul!")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), true);

        return true;
    }

    // Tier 4: Phase Shift - becomes temporarily invulnerable and faster
    private static boolean endermanPhaseShift(LivingEntity boss, ServerPlayer target, ServerLevel world) {
        UUID bossId = boss.getUUID();

        phasedBosses.put(bossId, System.currentTimeMillis() + 4000);

        boss.addEffect(new MobEffectInstance(MobEffects.SPEED, 80, 3));
        boss.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 80, 0));

        world.sendParticles(ParticleTypes.REVERSE_PORTAL,
                boss.getX(), boss.getY() + 1, boss.getZ(),
                8, 0.5, 0.6, 0.5, 0.1);

        world.playSound(null, boss, SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.HOSTILE, 2.0f, 0.3f);

        target.sendSystemMessage(Component.literal("⟐ The Void Phantom phases out of reality!")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), true);
        target.sendSystemMessage(Component.literal("  It cannot be damaged for 4 seconds!")
                .withStyle(ChatFormatting.LIGHT_PURPLE), true);

        return true;
    }

    // Tier 5: Void Rift - creates damaging zone + teleports player randomly
    private static boolean endermanVoidRift(LivingEntity boss, ServerPlayer target, ServerLevel world) {
        double angle = boss.getRandom().nextDouble() * Math.PI * 2;
        double dist = 10 + boss.getRandom().nextDouble() * 10;
        double newX = target.getX() + Math.cos(angle) * dist;
        double newZ = target.getZ() + Math.sin(angle) * dist;

        BlockPos safePos = findSafePosition(world, new BlockPos((int)newX, (int)target.getY(), (int)newZ));

        target.teleportTo(safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5);

        UUID bossId = boss.getUUID();
        Vec3 bossCenter = new Vec3(boss.getX(), boss.getY(), boss.getZ());
        voidZones.put(bossId, new VoidZone(bossCenter, System.currentTimeMillis() + 8000, 6.0));

        for (int i = 0; i < 100; i++) {
            double a = boss.getRandom().nextDouble() * Math.PI * 2;
            double r = boss.getRandom().nextDouble() * 6;
            double x = boss.getX() + Math.cos(a) * r;
            double z = boss.getZ() + Math.sin(a) * r;
            world.sendParticles(ParticleTypes.REVERSE_PORTAL, x, boss.getY() + 0.5, z,
                    5, 0.1, 0.5, 0.1, 0.02);
        }

        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
        target.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 0));

        world.playSound(null, boss, SoundEvents.WARDEN_SONIC_BOOM,
                SoundSource.HOSTILE, 1.0f, 2.0f);
        world.playSound(null, boss, SoundEvents.END_PORTAL_SPAWN,
                SoundSource.HOSTILE, 1.0f, 0.5f);

        target.sendSystemMessage(Component.literal("🌀 A VOID RIFT TEARS OPEN!")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), false);
        target.sendSystemMessage(Component.literal("  You've been displaced through the void!")
                .withStyle(ChatFormatting.LIGHT_PURPLE), false);

        return true;
    }

    // Piglin boss shield state tracking
    private static final Map<UUID, Long> piglinShieldEndTimes = new HashMap<>();
    private static final Map<UUID, Integer> piglinAbilityCounter = new HashMap<>();

    private static boolean usePiglinAbility(LivingEntity boss, ServerPlayer target, int tier, ServerLevel world) {
        UUID bossId = boss.getUUID();
        Map<String, Long> cooldowns = abilityCooldowns.computeIfAbsent(bossId, k -> new HashMap<>());
        long now = System.currentTimeMillis();

        // Cycle through abilities: Gold Rush → Greed Aura → Barter Shield → Reinforcements
        int counter = piglinAbilityCounter.getOrDefault(bossId, 0);
        int ability = counter % (tier >= 3 ? 4 : 2);
        piglinAbilityCounter.put(bossId, counter + 1);

        return switch (ability) {
            case 0 -> piglinGoldRush(boss, target, tier, world, cooldowns, now);
            case 1 -> piglinGreedAura(boss, target, tier, world);
            case 2 -> piglinBarterShield(boss, tier, world);
            case 3 -> piglinReinforcements(boss, target, tier, world, cooldowns, now);
            default -> false;
        };
    }

    /** Gold Rush: throws a burst of golden items that deal damage on impact */
    private static boolean piglinGoldRush(LivingEntity boss, ServerPlayer target, int tier, ServerLevel world,
                                          Map<String, Long> cooldowns, long now) {
        if (now - cooldowns.getOrDefault("gold_rush", 0L) < 8000L) return false;
        cooldowns.put("gold_rush", now);

        int projectileCount = 3 + tier * 2;
        Vec3 bossPos = getEntityPos(boss);
        Vec3 toTarget = new Vec3(target.getX(), target.getY(), target.getZ()).subtract(bossPos).normalize();

        for (int i = 0; i < projectileCount; i++) {
            // Spread projectiles slightly
            double spread = 0.3;
            double dx = toTarget.x + (world.getRandom().nextDouble() - 0.5) * spread;
            double dy = toTarget.y + 0.1 + world.getRandom().nextDouble() * 0.2;
            double dz = toTarget.z + (world.getRandom().nextDouble() - 0.5) * spread;
            Vec3 shot = new Vec3(dx * (1.0 + tier * 0.3), dy * (1.0 + tier * 0.3), dz * (1.0 + tier * 0.3));
            SmallFireball fireball = new SmallFireball(world, boss, shot);
            fireball.setDeltaMovement(dx * 0.8, dy * 0.8, dz * 0.8);
            world.addFreshEntity(fireball);
        }

        world.playSound(null, boss, SoundEvents.PIGLIN_ANGRY,
                SoundSource.HOSTILE, 1.5f, 0.8f + world.getRandom().nextFloat() * 0.4f);

        target.sendSystemMessage(Component.literal("💰 Gold Rush! The Gilded Ravager hurls golden bolts!")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        return true;
    }

    /** Greed Aura: nearby players lose coins while inside the radius */
    private static boolean piglinGreedAura(LivingEntity boss, ServerPlayer target, int tier, ServerLevel world) {
        AABB area = boss.getBoundingBox().inflate(6 + tier);
        boolean affected = false;

        for (ServerPlayer nearby : world.getEntitiesOfClass(ServerPlayer.class, area, p -> true)) {
            int coins = CoinManager.getCoins(nearby);
            if (coins > 0) {
                int drain = Math.min(coins, 50 + tier * 50);
                CoinManager.removeCoins(nearby, drain);
                nearby.sendSystemMessage(Component.literal("💸 Greed Aura! The Gilded Ravager drained §c" + drain + "§r coins from you!")
                        .withStyle(ChatFormatting.YELLOW));
                affected = true;
            }
        }

        world.playSound(null, boss, SoundEvents.PIGLIN_AMBIENT,
                SoundSource.HOSTILE, 1.5f, 0.6f);

        // Visual particle burst
        world.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                boss.getX(), boss.getY() + 1, boss.getZ(),
                5, 0.8, 0.5, 0.8, 0.1);

        target.sendSystemMessage(Component.literal("✦ The Gilded Ravager's Greed Aura pulses!")
                .withStyle(ChatFormatting.GOLD));
        return true;
    }

    /** Barter Shield: boss becomes temporarily immune and throws items */
    private static boolean piglinBarterShield(LivingEntity boss, int tier, ServerLevel world) {
        UUID bossId = boss.getUUID();
        long now = System.currentTimeMillis();
        long existingShield = piglinShieldEndTimes.getOrDefault(bossId, 0L);
        if (now < existingShield) return false;

        // Grant brief invulnerability
        int shieldTicks = 40 + tier * 10;  // 2–3 seconds
        boss.setInvulnerable(true);
        piglinShieldEndTimes.put(bossId, now + shieldTicks * 50L);

        // Glowing gold effect
        boss.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, shieldTicks, tier));
        boss.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, shieldTicks, 4));

        world.playSound(null, boss, SoundEvents.PIGLIN_CELEBRATE,
                SoundSource.HOSTILE, 1.5f, 1.0f);
        world.sendParticles(ParticleTypes.FIREWORK,
                boss.getX(), boss.getY() + 1, boss.getZ(), 8, 0.6, 0.5, 0.6, 0.05);

        AABB area = boss.getBoundingBox().inflate(20);
        for (ServerPlayer nearby : world.getEntitiesOfClass(ServerPlayer.class, area, p -> true)) {
            nearby.sendSystemMessage(Component.literal("🛡 Barter Shield! The Gilded Ravager becomes invulnerable! (Lasts " + (shieldTicks / 20) + "s)")
                    .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        }

        return true;
    }

    /** Reinforcements: summons Piglin Brutes to fight alongside the boss */
    private static boolean piglinReinforcements(LivingEntity boss, ServerPlayer target, int tier, ServerLevel world,
                                                 Map<String, Long> cooldowns, long now) {
        if (now - cooldowns.getOrDefault("reinforcements", 0L) < 30000L) return false;
        cooldowns.put("reinforcements", now);

        int count = 1 + tier;  // 2–6 piglins depending on tier
        BlockPos bossBlock = boss.blockPosition();

        for (int i = 0; i < count; i++) {
            int offsetX = (world.getRandom().nextInt(5) - 2);
            int offsetZ = (world.getRandom().nextInt(5) - 2);
            BlockPos spawnPos = bossBlock.offset(offsetX, 0, offsetZ);

            PiglinBrute brute = EntityType.PIGLIN_BRUTE.create(world, EntitySpawnReason.EVENT);
            if (brute != null) {
                brute.teleportTo(spawnPos.getX() + 0.5,
                        spawnPos.getY(), spawnPos.getZ() + 0.5);
                brute.setTarget(target);
                world.addFreshEntity(brute);
                world.sendParticles(ParticleTypes.PORTAL,
                        brute.getX(), brute.getY() + 1, brute.getZ(),
                        4, 0.5, 0.5, 0.5, 0.1);
            }
        }

        world.playSound(null, boss, SoundEvents.PIGLIN_CONVERTED_TO_ZOMBIFIED,
                SoundSource.HOSTILE, 1.5f, 0.8f);

        target.sendSystemMessage(Component.literal("⚔ Reinforcements! " + count + " Piglin Brute(s) join the fray!")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        return true;
    }

    /** Tick method to remove Barter Shield when expired */
    public static void tickPiglinShields(ServerLevel world) {
        long now = System.currentTimeMillis();
        piglinShieldEndTimes.entrySet().removeIf(entry -> {
            if (now >= entry.getValue()) {
                // Find the entity and remove invulnerability
                net.minecraft.world.entity.Entity entity = world.getEntity(entry.getKey());
                if (entity instanceof LivingEntity living) {
                    living.setInvulnerable(false);
                }
                return true;
            }
            return false;
        });
    }


    private static boolean useWardenAbility(LivingEntity boss, ServerPlayer target, int tier) {
        if (!(boss.level() instanceof ServerLevel world)) return false;

        return switch (tier) {
            case 1 -> wardenSonicPulse(boss, target, world);
            case 2 -> wardenTremor(boss, target, world);
            case 3 -> wardenSculkInfestation(boss, target, world);
            case 4 -> wardenSonicBoom(boss, target, world);
            case 5 -> wardenAbyssalRoar(boss, target, world);
            default -> false;
        };
    }

    private static boolean wardenSonicPulse(LivingEntity boss, ServerPlayer target, ServerLevel world) {
        AABB area = boss.getBoundingBox().inflate(6);

        for (ServerPlayer player : world.getEntitiesOfClass(ServerPlayer.class, area, p -> true)) {
            double dist = player.distanceTo(boss);
            float damage = (float) (8.0 * (1 - dist / 6.0));
            if (damage > 0) {
                player.hurtServer(world, boss.damageSources().sonicBoom(boss), damage);
                player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20, 0));
            }
        }

        for (double r = 1; r <= 8; r += 1) {
            for (int i = 0; i < 16; i++) {
                double angle = (2 * Math.PI / 16) * i;
                double x = boss.getX() + Math.cos(angle) * r;
                double z = boss.getZ() + Math.sin(angle) * r;
                world.sendParticles(ParticleTypes.SONIC_BOOM, x, boss.getY() + 1, z, 1, 0, 0, 0, 0);
            }
        }

        world.playSound(null, boss, SoundEvents.WARDEN_SONIC_CHARGE,
                SoundSource.HOSTILE, 2.0f, 1.0f);

        target.sendSystemMessage(Component.literal("〰 Sonic pulse emanates from the Sculk Terror!")
                .withStyle(ChatFormatting.DARK_AQUA), true);

        return true;
    }

    private static boolean wardenTremor(LivingEntity boss, ServerPlayer target, ServerLevel world) {
        AABB area = boss.getBoundingBox().inflate(8);

        for (ServerPlayer player : world.getEntitiesOfClass(ServerPlayer.class, area, p -> true)) {
            player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1));
            player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 40, 64));
            player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 30, 0));
            player.hurtServer(world, boss.damageSources().mobAttack(boss), 4.0f);
        }

        for (int i = 0; i < 50; i++) {
            double x = boss.getX() + (boss.getRandom().nextDouble() - 0.5) * 20;
            double z = boss.getZ() + (boss.getRandom().nextDouble() - 0.5) * 20;
            world.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, boss.getY() + 0.1, z,
                    3, 0.2, 0.1, 0.2, 0.01);
        }

        world.playSound(null, boss, SoundEvents.WARDEN_ROAR,
                SoundSource.HOSTILE, 2.0f, 0.5f);
        world.playSound(null, boss, SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.HOSTILE, 2.0f, 0.5f);


        target.sendSystemMessage(Component.literal("🌋 THE GROUND TREMBLES!")
                .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD), true);

        return true;
    }

    private static boolean wardenSculkInfestation(LivingEntity boss, ServerPlayer target, ServerLevel world) {
        int count = 5;
        List<BlockPos> sculkPositions = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            double angle = (2 * Math.PI / count) * i;
            double dist = 3 + boss.getRandom().nextDouble() * 4;
            int x = (int) (target.getX() + Math.cos(angle) * dist);
            int z = (int) (target.getZ() + Math.sin(angle) * dist);
            BlockPos pos = new BlockPos(x, (int) target.getY(), z);

            while (world.getBlockState(pos).isAir() && pos.getY() > world.getMinY()) {
                pos = pos.below();
            }
            pos = pos.above();

            if (world.getBlockState(pos).isAir()) {
                world.setBlock(pos, net.minecraft.world.level.block.Blocks.SCULK_SENSOR.defaultBlockState(), Block.UPDATE_ALL);
                sculkPositions.add(pos);
                scheduleBlockRemoval(world, pos, 200);

                world.sendParticles(ParticleTypes.SCULK_SOUL, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        4, 0.3, 0.3, 0.3, 0.02);
            }
        }

        if (!sculkPositions.isEmpty()) {
            sculkInfestations.put(boss.getUUID(), new SculkInfestation(sculkPositions, System.currentTimeMillis() + 10000));
        }

        world.playSound(null, boss, SoundEvents.SCULK_BLOCK_SPREAD,
                SoundSource.HOSTILE, 2.0f, 0.5f);

        target.sendSystemMessage(Component.literal("🔵 Sculk spreads around you! Avoid making noise!")
                .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD), true);

        return true;
    }

    // Tier 4: Sonic Boom - devastating ranged attack
    private static boolean wardenSonicBoom(LivingEntity boss, ServerPlayer target, ServerLevel world) {
        // Direct line of sonic damage
        Vec3 start = new Vec3(boss.getX(), boss.getY() + boss.getBbHeight() / 2, boss.getZ());
        Vec3 targetPos = new Vec3(target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ());
        Vec3 toTarget = targetPos.subtract(start).normalize();

        // Damage everything in a line
        for (double d = 0; d < 20; d += 0.5) {
            Vec3 pos = start.add(toTarget.scale(d));

            // Particles along beam
            world.sendParticles(ParticleTypes.SONIC_BOOM, pos.x, pos.y, pos.z,
                    1, 0, 0, 0, 0);

            // Check for entities to damage
            AABB hitBox = new AABB(pos.x - 1, pos.y - 1, pos.z - 1, pos.x + 1, pos.y + 1, pos.z + 1);
            for (LivingEntity entity : world.getEntitiesOfClass(LivingEntity.class, hitBox, e -> e != boss)) {
                if (entity instanceof ServerPlayer player) {
                    player.hurtServer(world, boss.damageSources().sonicBoom(boss), 20.0f);
                    player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0));

                    // Knockback
                    player.setDeltaMovement(toTarget.scale(2).add(0, 0.5, 0));
                    player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(player));
                }
            }
        }

        world.playSound(null, boss, SoundEvents.WARDEN_SONIC_BOOM,
                SoundSource.HOSTILE, 2.0f, 1.0f);

        target.sendSystemMessage(Component.literal("💀 SONIC BOOM! Devastating force tears through you!")
                .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD), true);

        return true;
    }

    // Tier 5: Abyssal Roar - ultimate ability
    private static boolean wardenAbyssalRoar(LivingEntity boss, ServerPlayer target, ServerLevel world) {
        AABB area = boss.getBoundingBox().inflate(20);

        // Phase 1: Pull all players toward boss
        for (ServerPlayer player : world.getEntitiesOfClass(ServerPlayer.class, area, p -> true)) {
            Vec3 bossPos = new Vec3(boss.getX(), boss.getY(), boss.getZ());
            Vec3 playerPos = new Vec3(player.getX(), player.getY(), player.getZ());
            Vec3 pullDir = bossPos.subtract(playerPos).normalize().scale(2.0);
            player.setDeltaMovement(pullDir);
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(player));

            // Heavy debuffs
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200, 0));
            player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 3));
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
            player.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 100, 2));
        }

        // Phase 2: Schedule explosion damage after pull
        Vec3 bossCenter = new Vec3(boss.getX(), boss.getY(), boss.getZ());
        abyssalRoarTargets.put(boss.getUUID(), new AbyssalRoar(System.currentTimeMillis() + 1500, bossCenter));

        // Massive particle vortex
        for (int ring = 0; ring < 10; ring++) {
            double radius = 20 - (ring * 2);
            for (int i = 0; i < 24; i++) {
                double angle = (2 * Math.PI / 24) * i + (ring * 0.2);
                double x = boss.getX() + Math.cos(angle) * radius;
                double z = boss.getZ() + Math.sin(angle) * radius;
                world.sendParticles(ParticleTypes.SCULK_SOUL, x, boss.getY() + 0.5 + ring * 0.3, z,
                        2, 0.1, 0.1, 0.1, 0.01);
            }
        }

        // Spawn sculk decorations temporarily
        for (int i = 0; i < 20; i++) {
            double angle = boss.getRandom().nextDouble() * Math.PI * 2;
            double dist = boss.getRandom().nextDouble() * 15;
            int x = (int) (boss.getX() + Math.cos(angle) * dist);
            int z = (int) (boss.getZ() + Math.sin(angle) * dist);
            BlockPos pos = new BlockPos(x, (int) boss.getY(), z);

            while (world.getBlockState(pos).isAir() && pos.getY() > world.getMinY()) {
                pos = pos.below();
            }

            if (world.getBlockState(pos.above()).isAir()) {
                world.setBlock(pos.above(), net.minecraft.world.level.block.Blocks.SCULK_VEIN.defaultBlockState(), Block.UPDATE_ALL);
                scheduleBlockRemoval(world, pos.above(), 200);
            }
        }

        world.playSound(null, boss, SoundEvents.WARDEN_ROAR,
                SoundSource.HOSTILE, 3.0f, 0.3f);
        world.playSound(null, boss, SoundEvents.WARDEN_SONIC_CHARGE,
                SoundSource.HOSTILE, 2.0f, 0.5f);

        target.sendSystemMessage(Component.literal("☠☠☠ ABYSSAL ROAR ☠☠☠")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), false);
        target.sendSystemMessage(Component.literal("  THE ABYSS CALLS FOR YOUR SOUL!")
                .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD), false);

        return true;
    }

    // ============================================================
    // TICK HANDLERS - Call these from your main server tick
    // ============================================================

    private static int cleanupTickCounter = 0;

    public static void tickAllBossAbilities(MinecraftServer server) {
        long now = System.currentTimeMillis();

        tickEnrageEffects(now);
        tickPhaseShift(now);
        tickDeathMarks(server, now);
        tickVoidZones(server, now);
        tickAbyssalRoar(server, now);
        tickSculkInfestations(server, now);
        tickShieldEffects(now);
        // Only run the expensive entity-existence sweep every 1200 ticks (~60 seconds)
        cleanupTickCounter++;
        if (cleanupTickCounter >= 1200) {
            cleanupTickCounter = 0;
            cleanupDespawnedBosses(server);
        }
    }

    private static void tickEnrageEffects(long now) {
        Iterator<Map.Entry<UUID, Long>> iter = enrageEndTimes.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<UUID, Long> entry = iter.next();
            if (now >= entry.getValue()) {
                enragedBosses.remove(entry.getKey());
                iter.remove();
            }
        }
    }

    private static void tickPhaseShift(long now) {
        phasedBosses.entrySet().removeIf(entry -> now >= entry.getValue());
    }

    private static void tickShieldEffects(long now) {
        shieldedBosses.entrySet().removeIf(entry -> now >= entry.getValue());
    }

    private static void tickDeathMarks(MinecraftServer server, long now) {
        Iterator<Map.Entry<UUID, DeathMark>> iter = deathMarkedPlayers.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<UUID, DeathMark> entry = iter.next();
            DeathMark mark = entry.getValue();

            if (now >= mark.triggerTime) {
                // OPTIMIZED: Use server.getPlayerList() for direct player lookup instead of iterating worlds
                ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
                if (player != null) {
                    ServerLevel world = (ServerLevel) player.level();
                    Entity bossEntity = world.getEntity(mark.bossUuid);
                    if (bossEntity instanceof LivingEntity boss) {
                        double dist = player.distanceTo(boss);

                        if (dist < 10) {
                            player.hurtServer(world, player.damageSources().magic(), mark.damage);

                            world.sendParticles(ParticleTypes.SOUL,
                                    player.getX(), player.getY() + 1, player.getZ(),
                                    5, 0.5, 0.5, 0.5, 0.1);

                            player.sendSystemMessage(Component.literal("💀 DEATH MARK TRIGGERED!")
                                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), false);
                        } else {
                            player.sendSystemMessage(Component.literal("✓ You escaped the death mark!")
                                    .withStyle(ChatFormatting.GREEN), true);
                        }
                    }
                }
                iter.remove();
            }
        }
    }

    private static void tickVoidZones(MinecraftServer server, long now) {
        Iterator<Map.Entry<UUID, VoidZone>> iter = voidZones.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<UUID, VoidZone> entry = iter.next();
            VoidZone zone = entry.getValue();

            if (now >= zone.endTime) {
                iter.remove();
                continue;
            }

            for (ServerLevel world : server.getAllLevels()) {
                AABB zoneAABB = new AABB(
                        zone.center.x - zone.radius, zone.center.y - 2, zone.center.z - zone.radius,
                        zone.center.x + zone.radius, zone.center.y + 4, zone.center.z + zone.radius
                );

                for (ServerPlayer player : world.getEntitiesOfClass(ServerPlayer.class, zoneAABB, p -> true)) {
                    Vec3 playerPos = new Vec3(player.getX(), player.getY(), player.getZ());
                    double dist = playerPos.distanceTo(zone.center);
                    if (dist < zone.radius) {
                        player.hurtServer(world, player.damageSources().magic(), 5.0f);

                        world.sendParticles(ParticleTypes.REVERSE_PORTAL,
                                player.getX(), player.getY() + 1, player.getZ(),
                                5, 0.3, 0.5, 0.3, 0.02);
                    }
                }

                if (now % 200 < 50) {
                    for (int i = 0; i < 10; i++) {
                        double angle = Math.random() * Math.PI * 2;
                        double r = Math.random() * zone.radius;
                        double x = zone.center.x + Math.cos(angle) * r;
                        double z = zone.center.z + Math.sin(angle) * r;
                        world.sendParticles(ParticleTypes.PORTAL, x, zone.center.y + 0.5, z,
                                2, 0.1, 0.3, 0.1, 0.01);
                    }
                }
            }
        }
    }

    private static void tickAbyssalRoar(MinecraftServer server, long now) {
        Iterator<Map.Entry<UUID, AbyssalRoar>> iter = abyssalRoarTargets.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<UUID, AbyssalRoar> entry = iter.next();
            AbyssalRoar roar = entry.getValue();

            if (now >= roar.explodeTime) {
                for (ServerLevel world : server.getAllLevels()) {
                    AABB area = new AABB(
                            roar.center.x - 8, roar.center.y - 4, roar.center.z - 8,
                            roar.center.x + 8, roar.center.y + 4, roar.center.z + 8
                    );

                    for (ServerPlayer player : world.getEntitiesOfClass(ServerPlayer.class, area, p -> true)) {
                        Vec3 playerPos = new Vec3(player.getX(), player.getY(), player.getZ());
                        double dist = playerPos.distanceTo(roar.center);
                        if (dist < 8) {
                            float damage = (float) (60.0 * (1 - dist / 8.0));
                            player.hurtServer(world, player.damageSources().magic(), damage);

                            Vec3 knockback = playerPos.subtract(roar.center).normalize().scale(3);
                            player.setDeltaMovement(knockback.add(0, 1, 0));
                            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(player));
                        }
                    }

                    world.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                            roar.center.x, roar.center.y + 1, roar.center.z,
                            3, 1, 0.6, 1, 0);
                    world.sendParticles(ParticleTypes.SCULK_SOUL,
                            roar.center.x, roar.center.y + 1, roar.center.z,
                            10, 2, 1, 2, 0.1);
                }
                iter.remove();
            }
        }
    }

    private static void tickSculkInfestations(MinecraftServer server, long now) {
        Iterator<Map.Entry<UUID, SculkInfestation>> iter = sculkInfestations.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<UUID, SculkInfestation> entry = iter.next();
            SculkInfestation infestation = entry.getValue();

            if (now >= infestation.endTime) {
                iter.remove();
                continue;
            }

            // Damage players near sculk sensors when they move
            for (ServerLevel world : server.getAllLevels()) {
                for (BlockPos sensorPos : infestation.sensorPositions) {
                    AABB sensorArea = new AABB(sensorPos).inflate(4);

                    for (ServerPlayer player : world.getEntitiesOfClass(ServerPlayer.class, sensorArea, p -> true)) {
                        // Check if player is moving (making noise)
                        if (player.getDeltaMovement().lengthSqr() > 0.01) {
                            player.hurtServer(world, player.damageSources().magic(), 3.0f);

                            world.sendParticles(ParticleTypes.SCULK_CHARGE_POP,
                                    sensorPos.getX() + 0.5, sensorPos.getY() + 0.5, sensorPos.getZ() + 0.5,
                                    3, 0.2, 0.2, 0.2, 0.02);
                        }
                    }
                }
            }
        }
    }

    private static void cleanupDespawnedBosses(MinecraftServer server) {
        Set<UUID> aliveBosses = new HashSet<>();

        for (ServerLevel world : server.getAllLevels()) {
            for (UUID bossId : new HashSet<>(bossPhases.keySet())) {
                Entity entity = world.getEntity(bossId);
                if (entity != null && entity.isAlive()) {
                    aliveBosses.add(bossId);
                }
            }
        }

        // Clean up data for dead bosses
        bossPhases.keySet().retainAll(aliveBosses);
        lastAbilityUse.keySet().retainAll(aliveBosses);
        abilityCooldowns.keySet().retainAll(aliveBosses);
        minionCounts.keySet().retainAll(aliveBosses);
        bossMinions.keySet().retainAll(aliveBosses);
        enragedBosses.retainAll(aliveBosses);
        enrageEndTimes.keySet().retainAll(aliveBosses);
        phasedBosses.keySet().retainAll(aliveBosses);
        shieldedBosses.keySet().retainAll(aliveBosses);
        voidZones.keySet().retainAll(aliveBosses);
        abyssalRoarTargets.keySet().retainAll(aliveBosses);
        sculkInfestations.keySet().retainAll(aliveBosses);
        undyingRageEndTimes.keySet().retainAll(aliveBosses);
        undyingRageUsed.retainAll(aliveBosses);
    }

    // ============================================================
    // DAMAGE MODIFICATION - Call from damage mixin
    // ============================================================

    public static float modifyIncomingDamage(UUID bossId, float originalDamage) {
        // Check if boss is enraged (invincible)
        if (enragedBosses.contains(bossId)) {
            return 0f;
        }

        // Check if boss is phased (invincible)
        if (phasedBosses.containsKey(bossId)) {
            return 0f;
        }

        // Check if boss has bone shield (90% reduction)
        if (shieldedBosses.containsKey(bossId)) {
            return originalDamage * 0.1f;
        }

        return originalDamage;
    }

    // ============================================================
    // UTILITY METHODS
    // ============================================================

    private static void scheduleBlockRemoval(ServerLevel world, BlockPos pos, int ticks) {
        scheduledBlockRemovals.add(new ScheduledBlockRemoval(world, pos, System.currentTimeMillis() + (ticks * 50L)));
    }

    public static void tickScheduledBlockRemovals() {
        long now = System.currentTimeMillis();
        Iterator<ScheduledBlockRemoval> iter = scheduledBlockRemovals.iterator();
        while (iter.hasNext()) {
            ScheduledBlockRemoval removal = iter.next();
            if (now >= removal.removeTime) {
                if (removal.world != null) {
                    removal.world.setBlock(removal.pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
                iter.remove();
            }
        }
    }

    private static BlockPos findSafePosition(ServerLevel world, BlockPos startPos) {
        BlockPos pos = startPos;

        // Search up for air
        for (int y = 0; y < 10; y++) {
            BlockPos check = pos.above(y);
            if (world.getBlockState(check).isAir() && world.getBlockState(check.above()).isAir()) {
                // Find ground below
                BlockPos ground = check;
                while (world.getBlockState(ground.below()).isAir() && ground.getY() > world.getMinY()) {
                    ground = ground.below();
                }
                return ground;
            }
        }

        // Search down for air
        for (int y = 0; y < 10; y++) {
            BlockPos check = pos.below(y);
            if (world.getBlockState(check).isAir() && world.getBlockState(check.above()).isAir()) {
                return check;
            }
        }

        return startPos;
    }

    private static void cleanup(UUID bossId) {
        bossPhases.remove(bossId);
        minionCounts.remove(bossId);
        bossMinions.remove(bossId);
        abilityCooldowns.remove(bossId);
        lastAbilityUse.remove(bossId);
        enragedBosses.remove(bossId);
        enrageEndTimes.remove(bossId);
        phasedBosses.remove(bossId);
        shieldedBosses.remove(bossId);
        voidZones.remove(bossId);
        abyssalRoarTargets.remove(bossId);
        sculkInfestations.remove(bossId);
        bossAbilityData.remove(bossId);
    }

    // ============================================================
    // DATA CLASSES
    // ============================================================

    public static class BossAbilityState {
        public final SlayerManager.SlayerType type;
        public final int tier;
        public long lastPassiveProc;

        public BossAbilityState(SlayerManager.SlayerType type, int tier) {
            this.type = type;
            this.tier = tier;
            this.lastPassiveProc = 0;
        }
    }

    private static class DeathMark {
        public final UUID bossUuid;
        public final long triggerTime;
        public final float damage;

        public DeathMark(UUID bossUuid, long triggerTime, float damage) {
            this.bossUuid = bossUuid;
            this.triggerTime = triggerTime;
            this.damage = damage;
        }
    }

    private static class VoidZone {
        public final Vec3 center;
        public final long endTime;
        public final double radius;

        public VoidZone(Vec3 center, long endTime, double radius) {
            this.center = center;
            this.endTime = endTime;
            this.radius = radius;
        }
    }

    private static class AbyssalRoar {
        public final long explodeTime;
        public final Vec3 center;

        public AbyssalRoar(long explodeTime, Vec3 center) {
            this.explodeTime = explodeTime;
            this.center = center;
        }
    }

    private static class SculkInfestation {
        public final List<BlockPos> sensorPositions;
        public final long endTime;

        public SculkInfestation(List<BlockPos> sensorPositions, long endTime) {
            this.sensorPositions = sensorPositions;
            this.endTime = endTime;
        }
    }

    private static class ScheduledBlockRemoval {
        public final ServerLevel world;
        public final BlockPos pos;
        public final long removeTime;

        public ScheduledBlockRemoval(ServerLevel world, BlockPos pos, long removeTime) {
            this.world = world;
            this.pos = pos;
            this.removeTime = removeTime;
        }
    }

    // ============================================================
    // PUBLIC API - For integration with SlayerManager
    // ============================================================

    public static boolean isBossEnraged(UUID bossId) {
        return enragedBosses.contains(bossId);
    }

    public static boolean isBossPhased(UUID bossId) {
        return phasedBosses.containsKey(bossId);
    }

    public static boolean isBossShielded(UUID bossId) {
        return shieldedBosses.containsKey(bossId);
    }



    public static Set<UUID> getActiveBosses() {
        return new HashSet<>(bossPhases.keySet());
    }

    public static BossAbilityState getBossState(UUID bossId) {
        return bossAbilityData.get(bossId);

    }
    public static float handleBossThorns(LivingEntity boss, ServerPlayer attacker,
                                         float damage, int tier) {
        if (!SlayerManager.isSlayerBoss(boss.getUUID())) return damage;

        float reflectPercent = 0.10f + (tier * 0.04f);
        float reflectedDamage = damage * reflectPercent;

        ServerLevel world = (ServerLevel) boss.level();
        attacker.hurtServer(world, world.damageSources().thorns(boss), reflectedDamage);

        world.sendParticles(ParticleTypes.CRIT,
                attacker.getX(), attacker.getY() + 1, attacker.getZ(),
                3, 0.3, 0.3, 0.3, 0.1);

        return damage;
    }
    public static void registerBoss(LivingEntity boss, SlayerManager.SlayerType type, int tier) {
        UUID bossId = boss.getUUID();
        bossPhases.put(bossId, 1);
        lastAbilityUse.put(bossId, 0L);
        abilityCooldowns.put(bossId, new HashMap<>());
        minionCounts.put(bossId, 0);
        bossMinions.put(bossId, new HashSet<>());
        bossAbilityData.put(bossId, new BossAbilityState(type, tier));
    }
    // ============================================================
// SLIME BOSS SPLITTING SYSTEM
// ============================================================

    public static void onSlimeBossDeath(LivingEntity boss, ServerLevel world, ServerPlayer player, int tier) {
        UUID bossId = boss.getUUID();
        int splitLevel = slimeSplitLevel.getOrDefault(bossId, 0);

        // Get the root boss (original boss that started all splits)
        UUID rootId = getRootBoss(bossId);

        // Remove this boss from children tracking
        Set<UUID> children = slimeSplitChildren.get(rootId);
        if (children != null) {
            children.remove(bossId);
        }

        // Remove split boss bar
        ServerBossEvent splitBar = splitBossBars.remove(bossId);
        if (splitBar != null) {
            splitBar.removeAllPlayers();
        }

        // Only split if this is the MAIN boss (level 0) - NO SECOND SPLITS
        if (splitLevel == 0) {
            for (int i = 0; i < 3; i++) {
                UUID childId = spawnSplitSlime(world, boss, 1, tier, player);
                if (childId != null) {
                    slimeSplitChildren.computeIfAbsent(rootId, k -> new HashSet<>()).add(childId);
                    slimeSplitParent.put(childId, bossId);
                    slimeSplitLevel.put(childId, 1);  // Level 1 = final split, won't split again
                }
            }
        }

        // Clean up this boss
        slimeSplitLevel.remove(bossId);
        slimeSplitParent.remove(bossId);
        removeBoss(bossId);

        // Check if ALL splits are dead
        Set<UUID> remainingChildren = slimeSplitChildren.get(rootId);
        boolean allDead = (remainingChildren == null || remainingChildren.isEmpty());

        if (allDead && splitLevel > 0) {
            // All split slimes are dead - complete the quest!
            SlayerManager.completeSlayerQuest(player, SlayerManager.SlayerType.SLIME);
            cleanupSlimeBoss(rootId);
            player.sendSystemMessage(Component.literal("☠ The Slime Boss has been fully defeated!").withStyle(ChatFormatting.GREEN), false);
        } else if (splitLevel == 0) {
            // Main boss just died, spawned splits
            int remaining = remainingChildren != null ? remainingChildren.size() : 0;
            player.sendSystemMessage(Component.literal("⚔ The Slime Boss split into " + remaining + " smaller slimes!").withStyle(ChatFormatting.YELLOW), false);
        } else {
            // A split died but others remain
            int remaining = remainingChildren != null ? remainingChildren.size() : 0;
            player.sendSystemMessage(Component.literal("⚔ " + remaining + " split slimes remaining...").withStyle(ChatFormatting.YELLOW), true);
        }
    }

    private static UUID spawnSplitSlime(ServerLevel world, LivingEntity parent, int splitLevel, int tier, ServerPlayer player) {
        Slime slime = (Slime) EntityType.SLIME.create(world, EntitySpawnReason.TRIGGERED);
        if (slime == null) return null;

        // Position near parent with random offset
        double offsetX = (world.getRandom().nextDouble() - 0.5) * 4;
        double offsetZ = (world.getRandom().nextDouble() - 0.5) * 4;
        slime.setPos(parent.getX() + offsetX, parent.getY() + 0.5, parent.getZ() + offsetZ);

        // Size 2 for split slimes (smaller than main boss)
        slime.setSize(2, true);

        // Scale health - 35% of parent health
        double baseHealth = parent.getMaxHealth() * 0.35;
        var healthAttr = slime.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.setBaseValue(baseHealth);
            slime.setHealth((float) baseHealth);
        }

        // Set custom name
        String tierStars = "⚔".repeat(Math.min(tier, 5));
        slime.setCustomName(Component.literal(tierStars + " Split Slime Boss").withStyle(ChatFormatting.GREEN));
        slime.setCustomNameVisible(false);

        slime.setPersistenceRequired();
        slime.addEffect(new MobEffectInstance(MobEffects.GLOWING, -1, 0, false, false, false));
        world.addFreshEntity(slime);

        UUID childId = slime.getUUID();

        // Register as boss
        registerBoss(slime, SlayerManager.SlayerType.SLIME, tier);
        SlayerManager.markAsSlayerBoss(childId);
        SlayerManager.trackBossOwner(childId, player.getUUID().toString());

        // Create boss bar for this split
        ServerBossEvent splitBar = new ServerBossEvent(
                UUID.randomUUID(),
                Component.literal(tierStars + " Split Slime").withStyle(ChatFormatting.GREEN),
                BossEvent.BossBarColor.GREEN,
                BossEvent.BossBarOverlay.PROGRESS
        );
        splitBar.addPlayer(player);
        splitBar.setProgress(1.0f);
        splitBossBars.put(childId, splitBar);

        return childId;
    }

    private static UUID getRootBoss(UUID bossId) {
        UUID parent = slimeSplitParent.get(bossId);
        if (parent == null) {
            return bossId;  // This is the root
        }
        return getRootBoss(parent);  // Recurse up
    }

    public static boolean isSlimeSplit(UUID bossId) {
        return slimeSplitParent.containsKey(bossId) || slimeSplitChildren.containsKey(bossId);
    }

    public static void initSlimeBoss(UUID bossId) {
        slimeSplitLevel.put(bossId, 0);  // Mark as root boss (level 0)
        slimeSplitChildren.put(bossId, new HashSet<>());  // Initialize children set
    }

    private static void cleanupSlimeBoss(UUID rootId) {
        slimeSplitChildren.remove(rootId);
        slimeSplitLevel.remove(rootId);
        // Remove all parent references for this tree
        slimeSplitParent.entrySet().removeIf(e -> getRootBoss(e.getKey()).equals(rootId));
    }
}