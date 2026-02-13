package com.political;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import java.util.Set;
import java.util.HashSet;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
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
    private static final Map<UUID, ServerBossBar> splitBossBars = new HashMap<>();
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
    // HELPER METHOD - Get Vec3d from entity
    // ============================================================
// Slime boss split tracking
    private static final Map<UUID, Set<UUID>> slimeSplitChildren = new HashMap<>();  // root boss -> all children
    private static final Map<UUID, UUID> slimeSplitParent = new HashMap<>();  // child -> its parent
    private static final Map<UUID, Integer> slimeSplitLevel = new HashMap<>();  // boss -> split level (0=main, 1=first split, 2=final)
    private static Vec3d getEntityPos(Entity entity) {
        return new Vec3d(entity.getX(), entity.getY(), entity.getZ());
    }
    private static final Map<UUID, Long> undyingRageCooldowns = new HashMap<>();
    private static final long UNDYING_RAGE_COOLDOWN_MS = 30000; // 30 seconds between rages
    // ============================================================
    // MAIN TICK METHOD
    // ============================================================
    public static void removeBoss(UUID bossId) {
        cleanup(bossId);
        undyingRageCooldowns.remove(bossId); // Add this line
    }
    public static void tickZombieBoss(MobEntity boss, ServerWorld world) {
        UUID bossId = boss.getUuid();

        // Check if boss is low HP for Undying Rage trigger
        float healthPercent = boss.getHealth() / boss.getMaxHealth();

        if (healthPercent < 0.5f) {
            long now = System.currentTimeMillis();
            long lastRage = undyingRageCooldowns.getOrDefault(bossId, 0L);

            // CHECK COOLDOWN - only trigger if 30 seconds have passed
            if (now - lastRage >= UNDYING_RAGE_COOLDOWN_MS) {
                undyingRageCooldowns.put(bossId, now);

                // Apply Undying Rage effects ONCE
                boss.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.STRENGTH, 200, 1, true, false, false // 10 seconds
                ));
                boss.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.SPEED, 200, 1, true, false, false
                ));
                boss.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.RESISTANCE, 100, 0, true, false, false // 5 seconds
                ));

                // Heal 10% HP
                boss.heal(boss.getMaxHealth() * 0.1f);

                // Announce rage
                for (ServerPlayerEntity player : world.getPlayers()) {
                    if (player.squaredDistanceTo(boss) < 2500) { // 50 block range
                        player.sendMessage(Text.literal("§4§l☠ UNDYING RAGE! §r§cThe Outlaw grows stronger!"), false);
                    }
                }

                // Sound effect
                world.playSound(null, boss.getBlockPos(),
                        net.minecraft.sound.SoundEvents.ENTITY_ZOMBIE_VILLAGER_CONVERTED,
                        net.minecraft.sound.SoundCategory.HOSTILE, 2.0f, 0.5f);
            }
            // If on cooldown, do nothing - prevents cycling
        }
    }
    public static void tickBossAbilities(LivingEntity boss, ServerPlayerEntity target,
                                         SlayerManager.SlayerType type, int tier) {
        if (type == SlayerManager.SlayerType.WARDEN && boss instanceof net.minecraft.entity.mob.WardenEntity warden) {
            warden.increaseAngerAt(target, 80, true);
            if (warden.getTarget() == null) {
                warden.setTarget(target);
            }
        }

        if (boss == null || !boss.isAlive() || target == null) return;
        if (!(boss.getEntityWorld() instanceof ServerWorld world)) return;

        // DELETE THE SECOND WARDEN CHECK THAT USES "player" - IT'S A DUPLICATE

        UUID bossId = boss.getUuid();
        long now = System.currentTimeMillis();
        ServerBossBar splitBar = splitBossBars.get(bossId);
        if (splitBar != null) {
            splitBar.setPercent(boss.getHealth() / boss.getMaxHealth());
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
            case WARDEN -> useWardenAbility(boss, target, tier);
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
    // PHASE SYSTEM
    // ============================================================

    private static void updatePhase(LivingEntity boss, UUID bossId, ServerPlayerEntity owner,
                                    SlayerManager.SlayerType type, int tier, ServerWorld world) {
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

    private static void onPhaseChange(LivingEntity boss, ServerPlayerEntity owner, int newPhase,
                                      SlayerManager.SlayerType type, int tier, ServerWorld world) {
        String phaseMessage = switch (newPhase) {
            case 2 -> "§e⚠ " + type.bossName + " grows angry!";
            case 3 -> "§c⚠ " + type.bossName + " enters a frenzy!";
            case 4 -> "§4§l⚠ " + type.bossName + " IS ENRAGED! ⚠";
            default -> null;
        };

        if (phaseMessage != null && owner != null) {
            owner.sendMessage(Text.literal(phaseMessage), false);
            world.playSoundFromEntity(null, boss, SoundEvents.ENTITY_WITHER_AMBIENT,
                    SoundCategory.HOSTILE, 1.5f, 0.5f);
            world.spawnParticles(ParticleTypes.ANGRY_VILLAGER,
                    boss.getX(), boss.getY() + boss.getHeight(), boss.getZ(),
                    20, 1.0, 0.5, 1.0, 0.1);
        }

        if (newPhase == 4 && tier >= 3) {
            boss.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 6000, tier - 1, false, true));
            boss.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 6000, 1, false, true));
        }
    }

    // ============================================================
    // PASSIVE ABILITIES
    // ============================================================

    private static void tickPassiveRegen(LivingEntity boss, int tier, ServerWorld world) {
        if (world.getServer().getTicks() % 40 != 0) return;

        float regenAmount = switch (tier) {
            case 1 -> 2.0f;
            case 2 -> 5.0f;
            case 3 -> 15.0f;
            case 4 -> 40.0f;
            case 5 -> 100.0f;
            default -> 1.0f;
        };

        boss.setHealth(Math.min(boss.getMaxHealth(), boss.getHealth() + regenAmount));

        if (world.getServer().getTicks() % 80 == 0) {
            world.spawnParticles(ParticleTypes.HEART,
                    boss.getX(), boss.getY() + boss.getHeight() + 0.5, boss.getZ(),
                    3, 0.5, 0.3, 0.5, 0.0);
        }
    }

    public static void handleBossLifesteal(LivingEntity boss, float damageDealt, int tier) {
        if (tier < 3) return;

        float lifestealPercent = switch (tier) {
            case 3 -> 0.05f;
            case 4 -> 0.10f;
            case 5 -> 0.15f;
            default -> 0f;
        };

        float healAmount = damageDealt * lifestealPercent;
        if (healAmount > 0) {
            boss.heal(healAmount);
            if (boss.getEntityWorld() instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.HEART,
                        boss.getX(), boss.getY() + boss.getHeight(), boss.getZ(),
                        3, 0.3, 0.3, 0.3, 0.0);
            }
        }
    }

    // ============================================================
    // ZOMBIE ABILITIES
    // ============================================================

    private static boolean useZombieAbility(LivingEntity boss, ServerPlayerEntity target, int tier, ServerWorld world) {
        return switch (tier) {
            case 1, 2 -> zombieGroan(boss, target, world);
            case 3 -> zombieSummonMinions(boss, target, world);
            case 4 -> zombiePestilence(boss, target, world);
            case 5 -> zombieUndyingRage(boss, target, world);
            default -> false;
        };
    }

    private static boolean zombieGroan(LivingEntity boss, ServerPlayerEntity target, ServerWorld world) {
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 1));
        world.playSoundFromEntity(null, boss, SoundEvents.ENTITY_ZOMBIE_AMBIENT,
                SoundCategory.HOSTILE, 2.0f, 0.5f);
        target.sendMessage(Text.literal("☠ The Undying Outlaw lets out a chilling groan!")
                .formatted(Formatting.DARK_GREEN), true);
        return true;
    }

    private static boolean zombieSummonMinions(LivingEntity boss, ServerPlayerEntity target, ServerWorld world) {
        int count = 3;
        for (int i = 0; i < count; i++) {
            ZombieEntity minion = EntityType.ZOMBIE.create(world, SpawnReason.MOB_SUMMONED);
            if (minion != null) {
                double angle = (2 * Math.PI / count) * i;
                double x = boss.getX() + Math.cos(angle) * 3;
                double z = boss.getZ() + Math.sin(angle) * 3;
                minion.setPosition(x, boss.getY(), z);
                minion.setCustomName(Text.literal("§2Undead Minion"));

                var healthAttr = minion.getAttributeInstance(EntityAttributes.MAX_HEALTH);
                if (healthAttr != null) {
                    healthAttr.setBaseValue(40);
                    minion.setHealth(40);
                }
                world.spawnEntity(minion);
                world.spawnParticles(ParticleTypes.SOUL, x, boss.getY() + 1, z, 10, 0.3, 0.5, 0.3, 0.02);
            }
        }
        world.playSoundFromEntity(null, boss, SoundEvents.ENTITY_ZOMBIE_VILLAGER_CONVERTED,
                SoundCategory.HOSTILE, 2.0f, 0.7f);
        target.sendMessage(Text.literal("☠ The Undying Outlaw summons reinforcements!")
                .formatted(Formatting.DARK_GREEN, Formatting.BOLD), true);
        return true;
    }

    private static boolean zombiePestilence(LivingEntity boss, ServerPlayerEntity target, ServerWorld world) {
        Box area = boss.getBoundingBox().expand(8);
        for (ServerPlayerEntity player : world.getEntitiesByClass(ServerPlayerEntity.class, area, p -> true)) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 100, 1));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 200, 2));
        }
        for (int i = 0; i < 36; i++) {
            double angle = (2 * Math.PI / 36) * i;
            double x = boss.getX() + Math.cos(angle) * 8;
            double z = boss.getZ() + Math.sin(angle) * 8;
            world.spawnParticles(ParticleTypes.ITEM_SLIME, x, boss.getY() + 0.5, z, 5, 0.2, 0.2, 0.2, 0.01);
        }
        world.playSoundFromEntity(null, boss, SoundEvents.ENTITY_HUSK_AMBIENT,
                SoundCategory.HOSTILE, 2.0f, 0.5f);
        target.sendMessage(Text.literal("☣ Pestilence spreads from the Undying Outlaw!")
                .formatted(Formatting.DARK_GREEN, Formatting.BOLD), true);
        return true;
    }

    // Tier 5: Undying Rage - temporary invincibility + damage boost
    private static boolean zombieUndyingRage(LivingEntity boss, ServerPlayerEntity target, ServerWorld world) {
        UUID bossId = boss.getUuid();

        // Mark as enraged
        enragedBosses.add(bossId);

        // Visual effects
        boss.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 100, 0));
        boss.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 100, 2));

        // Schedule rage end
        enrageEndTimes.put(bossId, System.currentTimeMillis() + 5000);

        // Particles
        world.spawnParticles(ParticleTypes.ANGRY_VILLAGER,
                boss.getX(), boss.getY() + 1, boss.getZ(),
                20, 0.5, 0.5, 0.5, 0.1);

        world.playSoundFromEntity(null, boss, SoundEvents.ENTITY_RAVAGER_ROAR,
                SoundCategory.HOSTILE, 2.0f, 0.6f);

        target.sendMessage(Text.literal("💀 UNDYING RAGE! The boss is temporarily invincible!")
                .formatted(Formatting.DARK_RED, Formatting.BOLD), true);

        return true;
    }

    // ============================================================
    // SPIDER ABILITIES
    // ============================================================

    private static boolean useSpiderAbility(LivingEntity boss, ServerPlayerEntity target, int tier) {
        if (!(boss.getEntityWorld() instanceof ServerWorld world)) return false;

        return switch (tier) {
            case 1, 2 -> spiderWebShot(boss, target, world);
            case 3 -> spiderVenomSpit(boss, target, world);
            case 4 -> spiderAmbush(boss, target, world);
            case 5 -> spiderBroodMother(boss, target, world);
            default -> false;
        };
    }

    private static boolean spiderWebShot(LivingEntity boss, ServerPlayerEntity target, ServerWorld world) {
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 80, 3));

        BlockPos playerPos = target.getBlockPos();
        if (world.getBlockState(playerPos).isAir()) {
            world.setBlockState(playerPos, net.minecraft.block.Blocks.COBWEB.getDefaultState());
            scheduleBlockRemoval(world, playerPos, 60);
        }

        world.playSoundFromEntity(null, boss, SoundEvents.ENTITY_SPIDER_AMBIENT,
                SoundCategory.HOSTILE, 2.0f, 1.2f);

        target.sendMessage(Text.literal("🕸 The Venomous Bandit traps you in webs!")
                .formatted(Formatting.DARK_RED), true);

        return true;
    }

    private static boolean spiderVenomSpit(LivingEntity boss, ServerPlayerEntity target, ServerWorld world) {
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 200, 2));
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 1));
        target.damage(world, boss.getDamageSources().magic(), 8.0f);

        Vec3d start = new Vec3d(boss.getX(), boss.getY() + boss.getHeight() / 2, boss.getZ());
        Vec3d end = new Vec3d(target.getX(), target.getY() + target.getHeight() / 2, target.getZ());
        Vec3d dir = end.subtract(start).normalize();
        double dist = start.distanceTo(end);

        for (double d = 0; d < dist; d += 0.5) {
            Vec3d pos = start.add(dir.multiply(d));
            world.spawnParticles(ParticleTypes.ITEM_SLIME, pos.x, pos.y, pos.z,
                    2, 0.1, 0.1, 0.1, 0.01);
        }

        world.playSoundFromEntity(null, boss, SoundEvents.ENTITY_LLAMA_SPIT,
                SoundCategory.HOSTILE, 2.0f, 0.5f);

        target.sendMessage(Text.literal("☠ Venom courses through your veins!")
                .formatted(Formatting.DARK_RED, Formatting.BOLD), true);

        return true;
    }

    private static boolean spiderAmbush(LivingEntity boss, ServerPlayerEntity target, ServerWorld world) {
        Vec3d targetPos = new Vec3d(target.getX(), target.getY(), target.getZ());
        Vec3d behindPlayer = targetPos.subtract(target.getRotationVec(1.0f).multiply(3));

        world.spawnParticles(ParticleTypes.LARGE_SMOKE,
                boss.getX(), boss.getY(), boss.getZ(),
                20, 0.5, 0.5, 0.5, 0.02);

        boss.requestTeleport(behindPlayer.x, behindPlayer.y, behindPlayer.z);

        world.spawnParticles(ParticleTypes.LARGE_SMOKE,
                behindPlayer.x, behindPlayer.y, behindPlayer.z,
                20, 0.5, 0.5, 0.5, 0.02);

        target.damage(world, boss.getDamageSources().mobAttack(boss), 15.0f);

        world.playSoundFromEntity(null, boss, SoundEvents.ENTITY_ENDERMAN_TELEPORT,
                SoundCategory.HOSTILE, 1.5f, 1.5f);

        target.sendMessage(Text.literal("⚡ The spider ambushes you from behind!")
                .formatted(Formatting.DARK_RED, Formatting.BOLD), true);

        return true;
    }

    private static boolean spiderBroodMother(LivingEntity boss, ServerPlayerEntity target, ServerWorld world) {
        int count = 8;

        for (int i = 0; i < count; i++) {
            CaveSpiderEntity spider = EntityType.CAVE_SPIDER.create(world, SpawnReason.MOB_SUMMONED);
            if (spider != null) {
                double angle = (2 * Math.PI / count) * i;
                double radius = 2 + boss.getRandom().nextDouble() * 2;
                double x = boss.getX() + Math.cos(angle) * radius;
                double z = boss.getZ() + Math.sin(angle) * radius;

                spider.setPosition(x, boss.getY(), z);
                spider.setCustomName(Text.literal("§4Venomous Spawn"));

                var healthAttr = spider.getAttributeInstance(EntityAttributes.MAX_HEALTH);
                if (healthAttr != null) {
                    healthAttr.setBaseValue(30);
                    spider.setHealth(30);
                }

                world.spawnEntity(spider);
            }
        }

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (boss.getRandom().nextFloat() < 0.3f) {
                    BlockPos webPos = target.getBlockPos().add(dx, 0, dz);
                    if (world.getBlockState(webPos).isAir()) {
                        world.setBlockState(webPos, net.minecraft.block.Blocks.COBWEB.getDefaultState());
                        scheduleBlockRemoval(world, webPos, 100);
                    }
                }
            }
        }

        world.playSoundFromEntity(null, boss, SoundEvents.ENTITY_SPIDER_DEATH,
                SoundCategory.HOSTILE, 2.0f, 0.5f);

        target.sendMessage(Text.literal("🕷 THE BROOD AWAKENS! Spiders swarm from all directions!")
                .formatted(Formatting.DARK_RED, Formatting.BOLD), true);

        return true;
    }

    // ============================================================
    // SKELETON ABILITIES
    // ============================================================

    private static boolean useSkeletonAbility(LivingEntity boss, ServerPlayerEntity target, int tier) {
        if (!(boss.getEntityWorld() instanceof ServerWorld world)) return false;

        return switch (tier) {
            case 1, 2 -> skeletonBoneRattle(boss, target, world);
            case 3 -> skeletonArrowBarrage(boss, target, world);
            case 4 -> skeletonBoneShield(boss, target, world);
            case 5 -> skeletonDeathMark(boss, target, world);
            default -> false;
        };
    }

    private static boolean skeletonBoneRattle(LivingEntity boss, ServerPlayerEntity target, ServerWorld world) {
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 60, 0));
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 80, 0));

        world.playSoundFromEntity(null, boss, SoundEvents.ENTITY_SKELETON_AMBIENT,
                SoundCategory.HOSTILE, 2.0f, 0.5f);
        world.playSoundFromEntity(null, boss, SoundEvents.ENTITY_SKELETON_AMBIENT,
                SoundCategory.HOSTILE, 2.0f, 1.5f);

        target.sendMessage(Text.literal("💀 The Bone Desperado rattles its bones menacingly!")
                .formatted(Formatting.WHITE), true);

        return true;
    }

    private static boolean skeletonArrowBarrage(LivingEntity boss, ServerPlayerEntity target, ServerWorld world) {
        int arrowCount = 8;

        for (int i = 0; i < arrowCount; i++) {
            net.minecraft.entity.projectile.ArrowEntity arrow = new net.minecraft.entity.projectile.ArrowEntity(
                    world, boss, new ItemStack(Items.ARROW), null);

            Vec3d targetPos = new Vec3d(target.getX(), target.getY() + target.getHeight() / 2, target.getZ());
            Vec3d bossPos = new Vec3d(boss.getX(), boss.getY() + boss.getHeight() / 2, boss.getZ());
            Vec3d toTarget = targetPos.subtract(bossPos);

            double spread = 0.15;
            double dx = toTarget.x + (boss.getRandom().nextDouble() - 0.5) * spread * toTarget.length();
            double dy = toTarget.y + (boss.getRandom().nextDouble() - 0.5) * spread * toTarget.length();
            double dz = toTarget.z + (boss.getRandom().nextDouble() - 0.5) * spread * toTarget.length();

            arrow.setVelocity(dx, dy + 0.1, dz, 2.0f, 0);
            arrow.setDamage(6.0);
            arrow.setCritical(true);

            world.spawnEntity(arrow);
        }

        world.playSoundFromEntity(null, boss, SoundEvents.ENTITY_ARROW_SHOOT,
                SoundCategory.HOSTILE, 2.0f, 0.8f);

        target.sendMessage(Text.literal("🏹 Arrow barrage incoming!")
                .formatted(Formatting.WHITE, Formatting.BOLD), true);

        return true;
    }

    private static boolean skeletonBoneShield(LivingEntity boss, ServerPlayerEntity target, ServerWorld world) {
        UUID bossId = boss.getUuid();

        shieldedBosses.put(bossId, System.currentTimeMillis() + 5000);

        boss.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 100, 3));
        boss.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 100, 0));

        for (int i = 0; i < 20; i++) {
            double angle = (2 * Math.PI / 20) * i;
            double x = boss.getX() + Math.cos(angle) * 1.5;
            double z = boss.getZ() + Math.sin(angle) * 1.5;
            world.spawnParticles(ParticleTypes.WHITE_ASH, x, boss.getY() + 1, z,
                    5, 0.1, 0.5, 0.1, 0.01);
        }

        world.playSoundFromEntity(null, boss, SoundEvents.ITEM_SHIELD_BLOCK.value(),
                SoundCategory.HOSTILE, 2.0f, 0.5f);
        target.sendMessage(Text.literal("🛡 The Bone Desperado summons a shield of bones!")
                .formatted(Formatting.WHITE, Formatting.BOLD), true);

        return true;
    }

    private static boolean skeletonDeathMark(LivingEntity boss, ServerPlayerEntity target, ServerWorld world) {
        UUID targetId = target.getUuid();

        deathMarkedPlayers.put(targetId, new DeathMark(boss.getUuid(), System.currentTimeMillis() + 3000, 50.0f));

        target.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 60, 0));

        world.playSoundFromEntity(null, target, SoundEvents.ENTITY_WITHER_SPAWN,
                SoundCategory.HOSTILE, 0.5f, 2.0f);

        target.sendMessage(Text.literal("☠ YOU HAVE BEEN MARKED FOR DEATH!")
                .formatted(Formatting.DARK_RED, Formatting.BOLD), false);
        target.sendMessage(Text.literal("  Move away from the boss in 3 seconds or take massive damage!")
                .formatted(Formatting.RED), false);

        return true;
    }

    // ============================================================
    // SLIME ABILITIES
    // ============================================================

    private static boolean useSlimeAbility(LivingEntity boss, ServerPlayerEntity target, int tier) {
        if (!(boss.getEntityWorld() instanceof ServerWorld world)) return false;

        return switch (tier) {
            case 1, 2 -> slimeBounce(boss, target, world);
            case 3 -> slimeSplit(boss, target, world);
            case 4 -> slimeAbsorb(boss, target, world);
            case 5 -> slimeMegaBounce(boss, target, world);
            default -> false;
        };
    }

    private static boolean slimeBounce(LivingEntity boss, ServerPlayerEntity target, ServerWorld world) {
        if (boss.squaredDistanceTo(target) < 25) {
            target.setVelocity(target.getVelocity().add(0, 1.5, 0));
            target.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket(target));
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 60, 0));
        }

        world.playSoundFromEntity(null, boss, SoundEvents.ENTITY_SLIME_JUMP,
                SoundCategory.HOSTILE, 2.0f, 0.5f);

        target.sendMessage(Text.literal("🟢 The Gelatinous Rustler bounces you into the air!")
                .formatted(Formatting.GREEN), true);

        return true;
    }

    // Tier 3: Split - creates smaller slimes
    private static boolean slimeSplit(LivingEntity boss, ServerPlayerEntity target, ServerWorld world) {
        int count = 4;

        for (int i = 0; i < count; i++) {
            SlimeEntity slime = EntityType.SLIME.create(world, SpawnReason.MOB_SUMMONED);
            if (slime != null) {
                double angle = (2 * Math.PI / count) * i;
                double x = boss.getX() + Math.cos(angle) * 2;
                double z = boss.getZ() + Math.sin(angle) * 2;

                slime.setPosition(x, boss.getY(), z);
                slime.setSize(2, true); // Medium slime
                slime.setCustomName(Text.literal("§aGelatinous Fragment"));

                world.spawnEntity(slime);

                // Launch them outward
                slime.setVelocity(Math.cos(angle) * 0.5, 0.5, Math.sin(angle) * 0.5);
            }
        }

        world.playSoundFromEntity(null, boss, SoundEvents.ENTITY_SLIME_SQUISH,
                SoundCategory.HOSTILE, 2.0f, 0.5f);

        target.sendMessage(Text.literal("🟢 The slime splits into fragments!")
                .formatted(Formatting.GREEN, Formatting.BOLD), true);

        return true;
    }

    // Tier 4: Absorb - pulls player in and damages
    private static boolean slimeAbsorb(LivingEntity boss, ServerPlayerEntity target, ServerWorld world) {
        Vec3d bossPos = new Vec3d(boss.getX(), boss.getY(), boss.getZ());
        Vec3d targetPos = new Vec3d(target.getX(), target.getY(), target.getZ());
        Vec3d pullDir = bossPos.subtract(targetPos).normalize().multiply(1.5);
        target.setVelocity(pullDir);
        target.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket(target));

        target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 2));
        target.damage(world, boss.getDamageSources().magic(), 10.0f);

        // Particle suction effect
        for (int i = 0; i < 20; i++) {
            double t = i / 20.0;
            Vec3d particleTargetPos = new Vec3d(target.getX(), target.getY(), target.getZ());
            Vec3d particleBossPos = new Vec3d(boss.getX(), boss.getY(), boss.getZ());
            Vec3d pos = particleTargetPos.lerp(particleBossPos, t);
            world.spawnParticles(ParticleTypes.ITEM_SLIME, pos.x, pos.y + 1, pos.z,
                    2, 0.1, 0.1, 0.1, 0.01);
        }

        world.playSoundFromEntity(null, boss, SoundEvents.ENTITY_SLIME_ATTACK,
                SoundCategory.HOSTILE, 2.0f, 0.3f);

        target.sendMessage(Text.literal("🟢 The Rustler tries to absorb you!")
                .formatted(Formatting.GREEN, Formatting.BOLD), true);

        return true;
    }

    // Tier 5: Mega Bounce - massive AoE knockback + damage
    private static boolean slimeMegaBounce(LivingEntity boss, ServerPlayerEntity target, ServerWorld world) {
        Box area = boss.getBoundingBox().expand(10);

        // Jump animation
        boss.setVelocity(0, 2, 0);

        // Delayed ground pound effect
        for (ServerPlayerEntity player : world.getEntitiesByClass(ServerPlayerEntity.class, area, p -> true)) {
            double dist = player.squaredDistanceTo(boss);
            if (dist < 100) { // 10 blocks
                double knockback = 2.0 * (1 - dist / 100);
                Vec3d playerPos = new Vec3d(player.getX(), player.getY(), player.getZ());
                Vec3d bossPos = new Vec3d(boss.getX(), boss.getY(), boss.getZ());
                Vec3d away = playerPos.subtract(bossPos).normalize().multiply(knockback);
                player.setVelocity(away.add(0, 1, 0));
                target.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket(target));
                player.damage(world, boss.getDamageSources().mobAttack(boss), 20.0f);
            }
        }

        // Ground pound particles
        for (int i = 0; i < 36; i++) {
            double angle = (2 * Math.PI / 36) * i;
            for (double r = 1; r <= 10; r += 2) {
                double x = boss.getX() + Math.cos(angle) * r;
                double z = boss.getZ() + Math.sin(angle) * r;
                world.spawnParticles(ParticleTypes.ITEM_SLIME, x, boss.getY() + 0.5, z,
                        3, 0.2, 0.1, 0.2, 0.05);
            }
        }

        world.playSoundFromEntity(null, boss, SoundEvents.ENTITY_GENERIC_EXPLODE.value(),
                SoundCategory.HOSTILE, 2.0f, 0.5f);

        target.sendMessage(Text.literal("💥 MEGA BOUNCE! The ground shakes violently!")
                .formatted(Formatting.GREEN, Formatting.BOLD), true);

        return true;
    }

    // ============================================================
    // ENDERMAN ABILITIES
    // ============================================================

    private static boolean useEndermanAbility(LivingEntity boss, ServerPlayerEntity target, int tier) {
        if (!(boss.getEntityWorld() instanceof ServerWorld world)) return false;

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
    private static boolean endermanBlink(LivingEntity boss, ServerPlayerEntity target, ServerWorld world) {
        double angle = boss.getRandom().nextDouble() * Math.PI * 2;
        double dist = 3 + boss.getRandom().nextDouble() * 3;
        double x = target.getX() + Math.cos(angle) * dist;
        double z = target.getZ() + Math.sin(angle) * dist;

        // Particles at old location
        world.spawnParticles(ParticleTypes.PORTAL,
                boss.getX(), boss.getY() + 1, boss.getZ(),
                30, 0.5, 1, 0.5, 0.1);

        // Teleport
        boss.requestTeleport(x, target.getY(), z);

        // Particles at new location
        world.spawnParticles(ParticleTypes.PORTAL,
                x, target.getY() + 1, z,
                30, 0.5, 1, 0.5, 0.1);

        world.playSoundFromEntity(null, boss, SoundEvents.ENTITY_ENDERMAN_TELEPORT,
                SoundCategory.HOSTILE, 2.0f, 1.0f);

        target.sendMessage(Text.literal("⟐ The Void Phantom blinks through reality!")
                .formatted(Formatting.DARK_PURPLE), true);

        return true;
    }

    // Tier 3: Void Gaze - forces player to look at boss, damages if they do
    private static boolean endermanVoidGaze(LivingEntity boss, ServerPlayerEntity target, ServerWorld world) {
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 40, 0));
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 100, 0));

        target.damage(world, boss.getDamageSources().magic(), 12.0f);

        // Eye particles
        world.spawnParticles(ParticleTypes.WITCH,
                boss.getX(), boss.getY() + boss.getHeight() - 0.5, boss.getZ(),
                15, 0.2, 0.2, 0.2, 0.05);

        world.playSoundFromEntity(null, boss, SoundEvents.ENTITY_ENDERMAN_STARE,
                SoundCategory.HOSTILE, 2.0f, 0.5f);

        target.sendMessage(Text.literal("👁 The Void Phantom's gaze pierces your soul!")
                .formatted(Formatting.DARK_PURPLE, Formatting.BOLD), true);

        return true;
    }

    // Tier 4: Phase Shift - becomes temporarily invulnerable and faster
    private static boolean endermanPhaseShift(LivingEntity boss, ServerPlayerEntity target, ServerWorld world) {
        UUID bossId = boss.getUuid();

        phasedBosses.put(bossId, System.currentTimeMillis() + 4000);

        boss.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 80, 3));
        boss.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 80, 0));

        world.spawnParticles(ParticleTypes.REVERSE_PORTAL,
                boss.getX(), boss.getY() + 1, boss.getZ(),
                50, 0.5, 1, 0.5, 0.1);

        world.playSoundFromEntity(null, boss, SoundEvents.ENTITY_ENDERMAN_TELEPORT,
                SoundCategory.HOSTILE, 2.0f, 0.3f);

        target.sendMessage(Text.literal("⟐ The Void Phantom phases out of reality!")
                .formatted(Formatting.DARK_PURPLE, Formatting.BOLD), true);
        target.sendMessage(Text.literal("  It cannot be damaged for 4 seconds!")
                .formatted(Formatting.LIGHT_PURPLE), true);

        return true;
    }

    // Tier 5: Void Rift - creates damaging zone + teleports player randomly
    private static boolean endermanVoidRift(LivingEntity boss, ServerPlayerEntity target, ServerWorld world) {
        double angle = boss.getRandom().nextDouble() * Math.PI * 2;
        double dist = 10 + boss.getRandom().nextDouble() * 10;
        double newX = target.getX() + Math.cos(angle) * dist;
        double newZ = target.getZ() + Math.sin(angle) * dist;

        BlockPos safePos = findSafePosition(world, new BlockPos((int)newX, (int)target.getY(), (int)newZ));

        target.requestTeleport(safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5);

        UUID bossId = boss.getUuid();
        Vec3d bossCenter = new Vec3d(boss.getX(), boss.getY(), boss.getZ());
        voidZones.put(bossId, new VoidZone(bossCenter, System.currentTimeMillis() + 8000, 6.0));

        for (int i = 0; i < 100; i++) {
            double a = boss.getRandom().nextDouble() * Math.PI * 2;
            double r = boss.getRandom().nextDouble() * 6;
            double x = boss.getX() + Math.cos(a) * r;
            double z = boss.getZ() + Math.sin(a) * r;
            world.spawnParticles(ParticleTypes.REVERSE_PORTAL, x, boss.getY() + 0.5, z,
                    5, 0.1, 0.5, 0.1, 0.02);
        }

        target.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 60, 0));
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 0));

        world.playSoundFromEntity(null, boss, SoundEvents.ENTITY_WARDEN_SONIC_BOOM,
                SoundCategory.HOSTILE, 1.0f, 2.0f);
        world.playSoundFromEntity(null, boss, SoundEvents.BLOCK_END_PORTAL_SPAWN,
                SoundCategory.HOSTILE, 1.0f, 0.5f);

        target.sendMessage(Text.literal("🌀 A VOID RIFT TEARS OPEN!")
                .formatted(Formatting.DARK_PURPLE, Formatting.BOLD), false);
        target.sendMessage(Text.literal("  You've been displaced through the void!")
                .formatted(Formatting.LIGHT_PURPLE), false);

        return true;
    }

    // ============================================================
    // WARDEN ABILITIES
    // ============================================================

    private static boolean useWardenAbility(LivingEntity boss, ServerPlayerEntity target, int tier) {
        if (!(boss.getEntityWorld() instanceof ServerWorld world)) return false;

        return switch (tier) {
            case 1 -> wardenSonicPulse(boss, target, world);
            case 2 -> wardenTremor(boss, target, world);
            case 3 -> wardenSculkInfestation(boss, target, world);
            case 4 -> wardenSonicBoom(boss, target, world);
            case 5 -> wardenAbyssalRoar(boss, target, world);
            default -> false;
        };
    }

    private static boolean wardenSonicPulse(LivingEntity boss, ServerPlayerEntity target, ServerWorld world) {
        Box area = boss.getBoundingBox().expand(8);

        for (ServerPlayerEntity player : world.getEntitiesByClass(ServerPlayerEntity.class, area, p -> true)) {
            double dist = player.distanceTo(boss);
            float damage = (float) (15.0 * (1 - dist / 8.0));
            if (damage > 0) {
                player.damage(world, boss.getDamageSources().sonicBoom(boss), damage);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 1));
            }
        }

        for (double r = 1; r <= 8; r += 1) {
            for (int i = 0; i < 16; i++) {
                double angle = (2 * Math.PI / 16) * i;
                double x = boss.getX() + Math.cos(angle) * r;
                double z = boss.getZ() + Math.sin(angle) * r;
                world.spawnParticles(ParticleTypes.SONIC_BOOM, x, boss.getY() + 1, z, 1, 0, 0, 0, 0);
            }
        }

        world.playSoundFromEntity(null, boss, SoundEvents.ENTITY_WARDEN_SONIC_CHARGE,
                SoundCategory.HOSTILE, 2.0f, 1.0f);

        target.sendMessage(Text.literal("〰 Sonic pulse emanates from the Sculk Terror!")
                .formatted(Formatting.DARK_AQUA), true);

        return true;
    }

    private static boolean wardenTremor(LivingEntity boss, ServerPlayerEntity target, ServerWorld world) {
        Box area = boss.getBoundingBox().expand(12);

        for (ServerPlayerEntity player : world.getEntitiesByClass(ServerPlayerEntity.class, area, p -> true)) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 80, 3));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 80, 128));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 60, 0));
            player.damage(world, boss.getDamageSources().mobAttack(boss), 8.0f);
        }

        for (int i = 0; i < 50; i++) {
            double x = boss.getX() + (boss.getRandom().nextDouble() - 0.5) * 20;
            double z = boss.getZ() + (boss.getRandom().nextDouble() - 0.5) * 20;
            world.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, boss.getY() + 0.1, z,
                    3, 0.2, 0.1, 0.2, 0.01);
        }

        world.playSoundFromEntity(null, boss, SoundEvents.ENTITY_WARDEN_ROAR,
                SoundCategory.HOSTILE, 2.0f, 0.5f);
        world.playSoundFromEntity(null, boss, SoundEvents.ENTITY_GENERIC_EXPLODE.value(),
                SoundCategory.HOSTILE, 2.0f, 0.5f);


        target.sendMessage(Text.literal("🌋 THE GROUND TREMBLES!")
                .formatted(Formatting.DARK_AQUA, Formatting.BOLD), true);

        return true;
    }

    private static boolean wardenSculkInfestation(LivingEntity boss, ServerPlayerEntity target, ServerWorld world) {
        int count = 5;
        List<BlockPos> sculkPositions = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            double angle = (2 * Math.PI / count) * i;
            double dist = 3 + boss.getRandom().nextDouble() * 4;
            int x = (int) (target.getX() + Math.cos(angle) * dist);
            int z = (int) (target.getZ() + Math.sin(angle) * dist);
            BlockPos pos = new BlockPos(x, (int) target.getY(), z);

            while (world.getBlockState(pos).isAir() && pos.getY() > world.getBottomY()) {
                pos = pos.down();
            }
            pos = pos.up();

            if (world.getBlockState(pos).isAir()) {
                world.setBlockState(pos, net.minecraft.block.Blocks.SCULK_SENSOR.getDefaultState());
                sculkPositions.add(pos);
                scheduleBlockRemoval(world, pos, 200);

                world.spawnParticles(ParticleTypes.SCULK_SOUL, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        10, 0.3, 0.3, 0.3, 0.02);
            }
        }

        if (!sculkPositions.isEmpty()) {
            sculkInfestations.put(boss.getUuid(), new SculkInfestation(sculkPositions, System.currentTimeMillis() + 10000));
        }

        world.playSoundFromEntity(null, boss, SoundEvents.BLOCK_SCULK_SPREAD,
                SoundCategory.HOSTILE, 2.0f, 0.5f);

        target.sendMessage(Text.literal("🔵 Sculk spreads around you! Avoid making noise!")
                .formatted(Formatting.DARK_AQUA, Formatting.BOLD), true);

        return true;
    }

    // Tier 4: Sonic Boom - devastating ranged attack
    private static boolean wardenSonicBoom(LivingEntity boss, ServerPlayerEntity target, ServerWorld world) {
        // Direct line of sonic damage
        Vec3d start = new Vec3d(boss.getX(), boss.getY() + boss.getHeight() / 2, boss.getZ());
        Vec3d targetPos = new Vec3d(target.getX(), target.getY() + target.getHeight() / 2, target.getZ());
        Vec3d toTarget = targetPos.subtract(start).normalize();

        // Damage everything in a line
        for (double d = 0; d < 20; d += 0.5) {
            Vec3d pos = start.add(toTarget.multiply(d));

            // Particles along beam
            world.spawnParticles(ParticleTypes.SONIC_BOOM, pos.x, pos.y, pos.z,
                    1, 0, 0, 0, 0);

            // Check for entities to damage
            Box hitBox = new Box(pos.x - 1, pos.y - 1, pos.z - 1, pos.x + 1, pos.y + 1, pos.z + 1);
            for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, hitBox, e -> e != boss)) {
                if (entity instanceof ServerPlayerEntity player) {
                    player.damage(world, boss.getDamageSources().sonicBoom(boss), 40.0f);
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 100, 0));

                    // Knockback
                    player.setVelocity(toTarget.multiply(2).add(0, 0.5, 0));
                    target.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket(target));
                }
            }
        }

        world.playSoundFromEntity(null, boss, SoundEvents.ENTITY_WARDEN_SONIC_BOOM,
                SoundCategory.HOSTILE, 2.0f, 1.0f);

        target.sendMessage(Text.literal("💀 SONIC BOOM! Devastating force tears through you!")
                .formatted(Formatting.DARK_AQUA, Formatting.BOLD), true);

        return true;
    }

    // Tier 5: Abyssal Roar - ultimate ability
    private static boolean wardenAbyssalRoar(LivingEntity boss, ServerPlayerEntity target, ServerWorld world) {
        Box area = boss.getBoundingBox().expand(20);

        // Phase 1: Pull all players toward boss
        for (ServerPlayerEntity player : world.getEntitiesByClass(ServerPlayerEntity.class, area, p -> true)) {
            Vec3d bossPos = new Vec3d(boss.getX(), boss.getY(), boss.getZ());
            Vec3d playerPos = new Vec3d(player.getX(), player.getY(), player.getZ());
            Vec3d pullDir = bossPos.subtract(playerPos).normalize().multiply(2.0);
            player.setVelocity(pullDir);
            target.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket(target));

            // Heavy debuffs
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 200, 0));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 3));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 1));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 100, 2));
        }

        // Phase 2: Schedule explosion damage after pull
        Vec3d bossCenter = new Vec3d(boss.getX(), boss.getY(), boss.getZ());
        abyssalRoarTargets.put(boss.getUuid(), new AbyssalRoar(System.currentTimeMillis() + 1500, bossCenter));

        // Massive particle vortex
        for (int ring = 0; ring < 10; ring++) {
            double radius = 20 - (ring * 2);
            for (int i = 0; i < 24; i++) {
                double angle = (2 * Math.PI / 24) * i + (ring * 0.2);
                double x = boss.getX() + Math.cos(angle) * radius;
                double z = boss.getZ() + Math.sin(angle) * radius;
                world.spawnParticles(ParticleTypes.SCULK_SOUL, x, boss.getY() + 0.5 + ring * 0.3, z,
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

            while (world.getBlockState(pos).isAir() && pos.getY() > world.getBottomY()) {
                pos = pos.down();
            }

            if (world.getBlockState(pos.up()).isAir()) {
                world.setBlockState(pos.up(), net.minecraft.block.Blocks.SCULK_VEIN.getDefaultState());
                scheduleBlockRemoval(world, pos.up(), 200);
            }
        }

        world.playSoundFromEntity(null, boss, SoundEvents.ENTITY_WARDEN_ROAR,
                SoundCategory.HOSTILE, 3.0f, 0.3f);
        world.playSoundFromEntity(null, boss, SoundEvents.ENTITY_WARDEN_SONIC_CHARGE,
                SoundCategory.HOSTILE, 2.0f, 0.5f);

        target.sendMessage(Text.literal("☠☠☠ ABYSSAL ROAR ☠☠☠")
                .formatted(Formatting.DARK_RED, Formatting.BOLD), false);
        target.sendMessage(Text.literal("  THE ABYSS CALLS FOR YOUR SOUL!")
                .formatted(Formatting.DARK_AQUA, Formatting.BOLD), false);

        return true;
    }

    // ============================================================
    // TICK HANDLERS - Call these from your main server tick
    // ============================================================

    public static void tickAllBossAbilities(MinecraftServer server) {
        long now = System.currentTimeMillis();

        tickEnrageEffects(now);
        tickPhaseShift(now);
        tickDeathMarks(server, now);
        tickVoidZones(server, now);
        tickAbyssalRoar(server, now);
        tickSculkInfestations(server, now);
        tickShieldEffects(now);
        cleanupDespawnedBosses(server);
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
                for (ServerWorld world : server.getWorlds()) {
                    ServerPlayerEntity player = (ServerPlayerEntity) world.getEntity(entry.getKey());
                    if (player != null) {
                        Entity bossEntity = world.getEntity(mark.bossUuid);
                        if (bossEntity instanceof LivingEntity boss) {
                            double dist = player.distanceTo(boss);

                            if (dist < 10) {
                                player.damage(world, player.getDamageSources().magic(), mark.damage);

                                world.spawnParticles(ParticleTypes.SOUL,
                                        player.getX(), player.getY() + 1, player.getZ(),
                                        30, 0.5, 0.5, 0.5, 0.1);

                                player.sendMessage(Text.literal("💀 DEATH MARK TRIGGERED!")
                                        .formatted(Formatting.DARK_RED, Formatting.BOLD), false);
                            } else {
                                player.sendMessage(Text.literal("✓ You escaped the death mark!")
                                        .formatted(Formatting.GREEN), true);
                            }
                        }
                        break;
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

            for (ServerWorld world : server.getWorlds()) {
                Box zoneBox = new Box(
                        zone.center.x - zone.radius, zone.center.y - 2, zone.center.z - zone.radius,
                        zone.center.x + zone.radius, zone.center.y + 4, zone.center.z + zone.radius
                );

                for (ServerPlayerEntity player : world.getEntitiesByClass(ServerPlayerEntity.class, zoneBox, p -> true)) {
                    Vec3d playerPos = new Vec3d(player.getX(), player.getY(), player.getZ());
                    double dist = playerPos.distanceTo(zone.center);
                    if (dist < zone.radius) {
                        player.damage(world, player.getDamageSources().magic(), 5.0f);

                        world.spawnParticles(ParticleTypes.REVERSE_PORTAL,
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
                        world.spawnParticles(ParticleTypes.PORTAL, x, zone.center.y + 0.5, z,
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
                for (ServerWorld world : server.getWorlds()) {
                    Box area = new Box(
                            roar.center.x - 8, roar.center.y - 4, roar.center.z - 8,
                            roar.center.x + 8, roar.center.y + 4, roar.center.z + 8
                    );

                    for (ServerPlayerEntity player : world.getEntitiesByClass(ServerPlayerEntity.class, area, p -> true)) {
                        Vec3d playerPos = new Vec3d(player.getX(), player.getY(), player.getZ());
                        double dist = playerPos.distanceTo(roar.center);
                        if (dist < 8) {
                            float damage = (float) (60.0 * (1 - dist / 8.0));
                            player.damage(world, player.getDamageSources().magic(), damage);

                            Vec3d knockback = playerPos.subtract(roar.center).normalize().multiply(3);
                            player.setVelocity(knockback.add(0, 1, 0));
                            player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket(player));
                        }
                    }

                    world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER,
                            roar.center.x, roar.center.y + 1, roar.center.z,
                            5, 2, 1, 2, 0);
                    world.spawnParticles(ParticleTypes.SCULK_SOUL,
                            roar.center.x, roar.center.y + 1, roar.center.z,
                            100, 4, 2, 4, 0.1);
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
            for (ServerWorld world : server.getWorlds()) {
                for (BlockPos sensorPos : infestation.sensorPositions) {
                    Box sensorArea = new Box(sensorPos).expand(4);

                    for (ServerPlayerEntity player : world.getEntitiesByClass(ServerPlayerEntity.class, sensorArea, p -> true)) {
                        // Check if player is moving (making noise)
                        if (player.getVelocity().lengthSquared() > 0.01) {
                            player.damage(world, player.getDamageSources().magic(), 3.0f);

                            world.spawnParticles(ParticleTypes.SCULK_CHARGE_POP,
                                    sensorPos.getX() + 0.5, sensorPos.getY() + 0.5, sensorPos.getZ() + 0.5,
                                    5, 0.2, 0.2, 0.2, 0.02);
                        }
                    }
                }
            }
        }
    }

    private static void cleanupDespawnedBosses(MinecraftServer server) {
        Set<UUID> aliveBosses = new HashSet<>();

        for (ServerWorld world : server.getWorlds()) {
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

    private static void scheduleBlockRemoval(ServerWorld world, BlockPos pos, int ticks) {
        scheduledBlockRemovals.add(new ScheduledBlockRemoval(world, pos, System.currentTimeMillis() + (ticks * 50L)));
    }

    public static void tickScheduledBlockRemovals() {
        long now = System.currentTimeMillis();
        Iterator<ScheduledBlockRemoval> iter = scheduledBlockRemovals.iterator();
        while (iter.hasNext()) {
            ScheduledBlockRemoval removal = iter.next();
            if (now >= removal.removeTime) {
                if (removal.world != null) {
                    removal.world.setBlockState(removal.pos, net.minecraft.block.Blocks.AIR.getDefaultState());
                }
                iter.remove();
            }
        }
    }

    private static BlockPos findSafePosition(ServerWorld world, BlockPos startPos) {
        BlockPos pos = startPos;

        // Search up for air
        for (int y = 0; y < 10; y++) {
            BlockPos check = pos.up(y);
            if (world.getBlockState(check).isAir() && world.getBlockState(check.up()).isAir()) {
                // Find ground below
                BlockPos ground = check;
                while (world.getBlockState(ground.down()).isAir() && ground.getY() > world.getBottomY()) {
                    ground = ground.down();
                }
                return ground;
            }
        }

        // Search down for air
        for (int y = 0; y < 10; y++) {
            BlockPos check = pos.down(y);
            if (world.getBlockState(check).isAir() && world.getBlockState(check.up()).isAir()) {
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
        public final Vec3d center;
        public final long endTime;
        public final double radius;

        public VoidZone(Vec3d center, long endTime, double radius) {
            this.center = center;
            this.endTime = endTime;
            this.radius = radius;
        }
    }

    private static class AbyssalRoar {
        public final long explodeTime;
        public final Vec3d center;

        public AbyssalRoar(long explodeTime, Vec3d center) {
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
        public final ServerWorld world;
        public final BlockPos pos;
        public final long removeTime;

        public ScheduledBlockRemoval(ServerWorld world, BlockPos pos, long removeTime) {
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
    public static float handleBossThorns(LivingEntity boss, ServerPlayerEntity attacker,
                                         float damage, int tier) {
        if (!SlayerManager.isSlayerBoss(boss.getUuid())) return damage;

        float reflectPercent = 0.10f + (tier * 0.04f);
        float reflectedDamage = damage * reflectPercent;

        ServerWorld world = (ServerWorld) boss.getEntityWorld();
        attacker.damage(world, world.getDamageSources().thorns(boss), reflectedDamage);

        world.spawnParticles(ParticleTypes.CRIT,
                attacker.getX(), attacker.getY() + 1, attacker.getZ(),
                5, 0.3, 0.3, 0.3, 0.1);

        return damage;
    }
    public static void registerBoss(LivingEntity boss, SlayerManager.SlayerType type, int tier) {
        UUID bossId = boss.getUuid();
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

    public static void onSlimeBossDeath(LivingEntity boss, ServerWorld world, ServerPlayerEntity player, int tier) {
        UUID bossId = boss.getUuid();
        int splitLevel = slimeSplitLevel.getOrDefault(bossId, 0);

        // Get the root boss (original boss that started all splits)
        UUID rootId = getRootBoss(bossId);

        // Remove this boss from children tracking
        Set<UUID> children = slimeSplitChildren.get(rootId);
        if (children != null) {
            children.remove(bossId);
        }

        // Remove split boss bar
        ServerBossBar splitBar = splitBossBars.remove(bossId);
        if (splitBar != null) {
            splitBar.clearPlayers();
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
            player.sendMessage(Text.literal("☠ The Slime Boss has been fully defeated!").formatted(Formatting.GREEN), false);
        } else if (splitLevel == 0) {
            // Main boss just died, spawned splits
            int remaining = remainingChildren != null ? remainingChildren.size() : 0;
            player.sendMessage(Text.literal("⚔ The Slime Boss split into " + remaining + " smaller slimes!").formatted(Formatting.YELLOW), false);
        } else {
            // A split died but others remain
            int remaining = remainingChildren != null ? remainingChildren.size() : 0;
            player.sendMessage(Text.literal("⚔ " + remaining + " split slimes remaining...").formatted(Formatting.YELLOW), true);
        }
    }

    private static UUID spawnSplitSlime(ServerWorld world, LivingEntity parent, int splitLevel, int tier, ServerPlayerEntity player) {
        SlimeEntity slime = (SlimeEntity) EntityType.SLIME.create(world, SpawnReason.TRIGGERED);
        if (slime == null) return null;

        // Position near parent with random offset
        double offsetX = (world.random.nextDouble() - 0.5) * 4;
        double offsetZ = (world.random.nextDouble() - 0.5) * 4;
        slime.setPosition(parent.getX() + offsetX, parent.getY() + 0.5, parent.getZ() + offsetZ);

        // Size 2 for split slimes (smaller than main boss)
        slime.setSize(2, true);

        // Scale health - 35% of parent health
        double baseHealth = parent.getMaxHealth() * 0.35;
        var healthAttr = slime.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.setBaseValue(baseHealth);
            slime.setHealth((float) baseHealth);
        }

        // Set custom name
        String tierStars = "⚔".repeat(Math.min(tier, 5));
        slime.setCustomName(Text.literal(tierStars + " Split Slime Boss").formatted(Formatting.GREEN));
        slime.setCustomNameVisible(false);

        slime.setPersistent();
        slime.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, -1, 0, false, false, false));
        world.spawnEntity(slime);

        UUID childId = slime.getUuid();

        // Register as boss
        registerBoss(slime, SlayerManager.SlayerType.SLIME, tier);
        SlayerManager.markAsSlayerBoss(childId);
        SlayerManager.trackBossOwner(childId, player.getUuidAsString());

        // Create boss bar for this split
        ServerBossBar splitBar = new ServerBossBar(
                Text.literal(tierStars + " Split Slime").formatted(Formatting.GREEN),
                BossBar.Color.GREEN,
                BossBar.Style.PROGRESS
        );
        splitBar.addPlayer(player);
        splitBar.setPercent(1.0f);
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