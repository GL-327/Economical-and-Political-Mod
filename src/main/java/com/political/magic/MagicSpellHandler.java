package com.political.magic;
import com.political.SchoolTreeManager;



import com.political.party.PoliticalPartyManager;
import com.political.stats.HealthScalingManager;
import com.political.technique.Technique;
import com.political.technique.TechniqueManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.*;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class MagicSpellHandler {

    private static int manaTickCounter = 0;
    private static int creatureTickCounter = 0;

    // ════════════════════════════════════════════════════════════
    // TICK HANDLER
    // ════════════════════════════════════════════════════════════

    public static void tick(MinecraftServer server) {
        manaTickCounter++;
        if (manaTickCounter >= 20) {
            manaTickCounter = 0;
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                MagicManager.regenMana(player.getUUID().toString());
            }
        }

        creatureTickCounter++;
        if (creatureTickCounter >= 200) {
            creatureTickCounter = 0;
            for (ServerLevel world : server.getAllLevels()) {
                trySpawnMagicCreatures(world);
            }
        }
    }

    // ════════════════════════════════════════════════════════════
    // STAFF USE HANDLER
    // ════════════════════════════════════════════════════════════

    public static boolean handleStaffUse(ServerPlayer player, ItemStack stack) {
        if (!MagicStaff.isStaff(stack)) return false;

        // Sneak + right-click casts the selected Magic Technique ability instead of the spell.
        if (player.isShiftKeyDown()) {
            return TechniqueManager.handleStaffSneakUse(player, stack);
        }

        String uuid = player.getUUID().toString();
        MagicManager.Spell spell = MagicManager.getActiveSpell(uuid);

        if (spell == null) {
            player.sendSystemMessage(Component.literal("✦ No spell applied to your staff! Use /magic to apply one.")
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        if (!MagicManager.hasSpell(uuid, spell)) {
            player.sendSystemMessage(Component.literal("✦ You haven't learned this spell!")
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        if (MagicManager.getLevel(uuid) < spell.levelRequired) {
            player.sendSystemMessage(Component.literal("✦ Requires Magic Level " + spell.levelRequired + "!")
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        if (MagicManager.isOnCooldown(uuid, spell)) {
            long remaining = MagicManager.getCooldownRemaining(uuid, spell);
            player.sendSystemMessage(Component.literal("✦ " + spell.displayName + " on cooldown! "
                    + (remaining / 1000) + "s remaining").withStyle(ChatFormatting.YELLOW));
            return false;
        }

        if (!MagicManager.consumeMana(player, spell.manaCost)) return false;

        castSpell(player, spell);
        MagicManager.setCooldown(player, spell);
        MagicManager.tryProgressManaSpent(player, spell.manaCost);
        MagicManager.tryProgressSpellCast(player, spell);
        PoliticalPartyManager.progressPartyCommission(player, "CAST_SPELLS", 1);
        return true;
    }

    // ════════════════════════════════════════════════════════════
    // SPELL DISPATCH
    // ════════════════════════════════════════════════════════════

    private static void castSpell(ServerPlayer player, MagicManager.Spell spell) {
        ServerLevel world = (ServerLevel) player.level();
        String uuid = player.getUUID().toString();
        Set<String> unlocks = MagicManager.getSkillTreeUnlocks(uuid);
        float damageMultiplier = 1.0f + MagicSkillTree.getSpellDamageBonus(unlocks) / 100f;
        damageMultiplier += MagicStaff.getSpellPowerPercent(player.getMainHandItem()) / 100f;
        emitSpellCastVisuals(player, world, spell);

        switch (spell) {
            case FIREBALL -> castFireball(player, world);
            case ICE_SPIKE -> castIceSpike(player, world);
            case LIGHTNING_STRIKE -> castLightning(player, world);
            case HEALING_WAVE -> castHealingWave(player, world, unlocks);
            case SHADOW_STEP -> castShadowStep(player, unlocks);
            case ARCANE_SHIELD -> castArcaneShield(player, unlocks);
            case POISON_CLOUD -> castPoisonCloud(player, world);
            case WIND_GUST -> castWindGust(player, world);
            case SOUL_DRAIN -> castSoulDrain(player, world, unlocks, damageMultiplier);
            case METEOR -> castMeteor(player, world, unlocks, damageMultiplier);
            case ARCANE_BURST -> castArcaneBurst(player, world, damageMultiplier);
            case FLAME_WAVE -> castFlameWave(player, world, damageMultiplier);
            case FROST_NOVA -> castFrostNova(player, world, damageMultiplier);
            case CHAIN_SPARK -> castChainSpark(player, world, damageMultiplier);
            case VOID_GRASP -> castVoidGrasp(player, world);
            case SOLAR_FLARE -> castSolarFlare(player, world, damageMultiplier);
            case NETHER_BOLT -> castNetherBolt(player, world, damageMultiplier);
            case SOUL_SPARK -> castSoulSpark(player, world, damageMultiplier);
            case STORM_BOLT -> castStormBolt(player, world);
            case GRAVITY_WELL -> castGravityWell(player, world);
            case LIFE_TAP -> castLifeTap(player);
            case REGEN_AURA -> castRegenAura(player, world);
            case ICE_LANCE -> castIceLance(player, world, damageMultiplier);
            case SHADOW_BOLT -> castShadowBolt(player, world, damageMultiplier);
            case HOLY_BOLT -> castHolyBolt(player, world, damageMultiplier);
            case ROCK_SPIKE -> castRockSpike(player, world, damageMultiplier);
            case TIDE_PUSH -> castTidePush(player, world);
            case WIND_SHEAR -> castWindShear(player, world);
            case ARCANE_NOVA -> castArcaneNova(player, world, unlocks, damageMultiplier);
            case BLOOD_NOVA -> castBloodNova(player, world, damageMultiplier);
            case PHANTOM_SHIFT -> castPhantomShift(player);
            case STAR_SHARD -> castStarShard(player, world, unlocks, damageMultiplier);
            case MANA_SURGE -> castManaSurge(player);
        }

        player.sendSystemMessage(Component.literal("✦ Cast " + spell.displayName + "!")
                .withStyle(spell.color));
    }

    // ════════════════════════════════════════════════════════════
    // SPELL IMPLEMENTATIONS
    // ════════════════════════════════════════════════════════════

    private static void castFireball(ServerPlayer player, ServerLevel world) {
        Vec3 look = player.getLookAngle();
        SmallFireball fireball = new SmallFireball(world, player, look);
        fireball.setPos(player.getX() + look.x, player.getEyeY(), player.getZ() + look.z);
        world.addFreshEntity(fireball);
    }

    private static void castIceSpike(ServerPlayer player, ServerLevel world) {
        LivingEntity target = getTargetEntity(player, 20);
        if (target == null) {
            player.sendSystemMessage(Component.literal("  No target in range!").withStyle(ChatFormatting.GRAY));
            return;
        }
        emitIceSpikeProjectile(player, world, target);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 4));
        target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 60, 2));
        target.hurt(world.damageSources().magic(), 4f);
    }

    private static void emitSpellCastVisuals(ServerPlayer player, ServerLevel world, MagicManager.Spell spell) {
        double x = player.getX();
        double y = player.getEyeY();
        double z = player.getZ();
        Vec3 look = player.getLookAngle();
        ParticleOptionsPack p = spellParticlePack(spell);
        world.sendParticles(p.core, x, y, z, 14, 0.35, 0.25, 0.35, 0.02);
        world.sendParticles(p.accent, x, y, z, 8, 0.5, 0.35, 0.5, 0.01);
        for (int i = 1; i <= 10; i++) {
            Vec3 point = player.getEyePosition().add(look.scale(i * 0.9));
            world.sendParticles(p.trail, point.x, point.y, point.z, 2, 0.08, 0.08, 0.08, 0.0);
        }
        world.playSound(null, player.blockPosition(), p.sound, SoundSource.PLAYERS, 0.8f, p.pitch);
    }

    private static void emitIceSpikeProjectile(ServerPlayer player, ServerLevel world, LivingEntity target) {
        Vec3 start = player.getEyePosition();
        Vec3 end = target.getEyePosition();
        Vec3 delta = end.subtract(start);
        double len = Math.max(0.5, delta.length());
        Vec3 dir = delta.scale(1.0 / len);
        for (double d = 0.0; d <= len; d += 0.45) {
            Vec3 p = start.add(dir.scale(d));
            world.sendParticles(ParticleTypes.SNOWFLAKE, p.x, p.y, p.z, 3, 0.06, 0.06, 0.06, 0.0);
            world.sendParticles(ParticleTypes.ITEM_SNOWBALL, p.x, p.y, p.z, 1, 0.03, 0.03, 0.03, 0.0);
        }
        world.sendParticles(ParticleTypes.SNOWFLAKE, end.x, end.y, end.z, 18, 0.25, 0.25, 0.25, 0.02);
        world.playSound(null, target.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.55f, 1.7f);
    }

    private record ParticleOptionsPack(net.minecraft.core.particles.ParticleOptions core,
                                       net.minecraft.core.particles.ParticleOptions accent,
                                       net.minecraft.core.particles.ParticleOptions trail,
                                       SoundEvent sound,
                                       float pitch) {}

    private static ParticleOptionsPack spellParticlePack(MagicManager.Spell spell) {
        return switch (spell) {
            case FIREBALL, FLAME_WAVE, METEOR, NETHER_BOLT -> new ParticleOptionsPack(ParticleTypes.FLAME, ParticleTypes.LAVA, ParticleTypes.SMOKE, SoundEvents.BLAZE_SHOOT, 1.05f);
            case ICE_SPIKE, ICE_LANCE, FROST_NOVA -> new ParticleOptionsPack(ParticleTypes.SNOWFLAKE, ParticleTypes.ITEM_SNOWBALL, ParticleTypes.END_ROD, SoundEvents.PLAYER_HURT_FREEZE, 1.2f);
            case LIGHTNING_STRIKE, CHAIN_SPARK, STORM_BOLT -> new ParticleOptionsPack(ParticleTypes.ELECTRIC_SPARK, ParticleTypes.END_ROD, ParticleTypes.CRIT, SoundEvents.LIGHTNING_BOLT_IMPACT, 1.3f);
            case SHADOW_STEP, PHANTOM_SHIFT, SHADOW_BOLT, VOID_GRASP -> new ParticleOptionsPack(ParticleTypes.PORTAL, ParticleTypes.SMOKE, ParticleTypes.REVERSE_PORTAL, SoundEvents.ENDERMAN_TELEPORT, 0.9f);
            case HEALING_WAVE, HOLY_BOLT, REGEN_AURA, LIFE_TAP -> new ParticleOptionsPack(ParticleTypes.HEART, ParticleTypes.HAPPY_VILLAGER, ParticleTypes.END_ROD, SoundEvents.AMETHYST_BLOCK_CHIME, 1.4f);
            default -> new ParticleOptionsPack(ParticleTypes.ENCHANT, ParticleTypes.END_ROD, ParticleTypes.WITCH, SoundEvents.ENCHANTMENT_TABLE_USE, 1.0f);
        };
    }

    private static void castLightning(ServerPlayer player, ServerLevel world) {
        BlockHitResult hit = castBlockRay(player, 30);
        if (hit == null) return;
        BlockPos pos = hit.getBlockPos();
        LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, world);
        bolt.teleportTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
        bolt.setCause(player);
        world.addFreshEntity(bolt);
    }

    private static void castHealingWave(ServerPlayer player, ServerLevel world, Set<String> unlocks) {
        float healAmount = 8f;
        if (MagicSkillTree.hasGreaterHealing(unlocks)) healAmount *= 1.5f;
        player.heal(healAmount);

        for (ServerPlayer nearby : world.getPlayers(p -> p.distanceToSqr(player) <= 25 && p != player)) {
            nearby.heal(healAmount * 0.5f);
            nearby.sendSystemMessage(Component.literal("✦ Healed by " + player.getName().getString()
                    + "'s Healing Wave!").withStyle(ChatFormatting.GREEN));
        }

        if (MagicSkillTree.hasHolyAura(unlocks)) {
            for (ServerPlayer nearby : world.getPlayers(p -> p.distanceToSqr(player) <= 25)) {
                nearby.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0));
            }
        }
    }

    private static void castShadowStep(ServerPlayer player, Set<String> unlocks) {
        int range = MagicSkillTree.hasShadowStepRange(unlocks) ? 12 : 8;
        Vec3 look = player.getLookAngle();
        Vec3 target = player.position().add(look.scale(range));
        BlockPos targetPos = BlockPos.containing(target);
        ServerLevel world = (ServerLevel) player.level();

        int safeY = targetPos.getY();
        for (int y = targetPos.getY() + 5; y >= targetPos.getY() - 5; y--) {
            BlockPos check = new BlockPos(targetPos.getX(), y, targetPos.getZ());
            if (!world.getBlockState(check).isSolid()
                    && !world.getBlockState(check.above()).isSolid()
                    && world.getBlockState(check.below()).isSolid()) {
                safeY = y;
                break;
            }
        }
        player.teleportTo(targetPos.getX() + 0.5, safeY, targetPos.getZ() + 0.5);
    }

    private static void castArcaneShield(ServerPlayer player, Set<String> unlocks) {
        int duration = MagicSkillTree.hasFortification(unlocks) ? 300 : 200;
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, duration, 1));
    }

    private static void castPoisonCloud(ServerPlayer player, ServerLevel world) {
        BlockHitResult hit = castBlockRay(player, 15);
        double x, y, z;
        if (hit != null) {
            BlockPos pos = hit.getBlockPos();
            x = pos.getX() + 0.5;
            y = pos.getY() + 1;
            z = pos.getZ() + 0.5;
        } else {
            Vec3 look = player.getLookAngle();
            Vec3 t = player.getEyePosition().add(look.scale(10));
            x = t.x; y = t.y; z = t.z;
        }
        AreaEffectCloud cloud = new AreaEffectCloud(world, x, y, z);
        cloud.setRadius(3.0f);
        cloud.setDuration(100);
        cloud.setWaitTime(0);
        cloud.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
        cloud.setOwner(player);
        world.addFreshEntity(cloud);
    }

    private static void castWindGust(ServerPlayer player, ServerLevel world) {
        List<Entity> nearby = world.getEntities(player,
                player.getBoundingBox().inflate(5.0),
                e -> e instanceof LivingEntity && e != player);
        for (Entity e : nearby) {
            Vec3 dir = e.position().subtract(player.position()).normalize().scale(2.0);
            e.setDeltaMovement(dir.x, 0.5, dir.z);
            e.hurtMarked = true;
        }
    }

    private static void castSoulDrain(ServerPlayer player, ServerLevel world,
                                       Set<String> unlocks, float dmgMult) {
        LivingEntity target = getTargetEntity(player, 15);
        if (target == null) {
            player.sendSystemMessage(Component.literal("  No target in range!").withStyle(ChatFormatting.GRAY));
            return;
        }
        target.hurt(world.damageSources().magic(), 5f * dmgMult);
        float heal = 6f;
        if (MagicSkillTree.hasLifeLeech(unlocks)) heal *= 1.5f;
        player.heal(heal);
    }

    private static void castMeteor(ServerPlayer player, ServerLevel world,
                                    Set<String> unlocks, float dmgMult) {
        BlockHitResult hit = castBlockRay(player, 40);
        double x, y, z;
        if (hit != null) {
            BlockPos pos = hit.getBlockPos();
            x = pos.getX() + 0.5;
            z = pos.getZ() + 0.5;
            y = pos.getY() + 1;
        } else {
            Vec3 look = player.getLookAngle();
            Vec3 t = player.getEyePosition().add(look.scale(20));
            x = t.x; y = t.y; z = t.z;
        }

        float meteorDmg = MagicSkillTree.hasStellarImpact(unlocks) ? 15f * 1.4f : 15f;
        meteorDmg *= dmgMult;

        world.explode(player, x, y, z, meteorDmg / 3f, Level.ExplosionInteraction.NONE);

        List<Entity> targets = world.getEntities(player,
                new AABB(x - 4, y - 2, z - 4, x + 4, y + 4, z + 4));
        for (Entity e : targets) {
            if (e instanceof LivingEntity living && e != player) {
                living.hurt(world.damageSources().magic(), meteorDmg);
            }
        }
    }

    private static void castArcaneBurst(ServerPlayer player, ServerLevel world, float dmgMult) {
        LivingEntity t = getTargetEntity(player, 12);
        if (t != null) t.hurt(world.damageSources().magic(), 7f * dmgMult);
    }

    private static void castFlameWave(ServerPlayer player, ServerLevel world, float dmgMult) {
        for (Entity e : world.getEntities(player, player.getBoundingBox().inflate(8))) {
            if (e instanceof LivingEntity living && e != player) {
                living.setRemainingFireTicks(60);
                living.hurt(world.damageSources().magic(), 9f * dmgMult);
            }
        }
    }

    private static void castFrostNova(ServerPlayer player, ServerLevel world, float dmgMult) {
        for (Entity e : world.getEntities(player, player.getBoundingBox().inflate(6))) {
            if (e instanceof LivingEntity living && e != player) {
                living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2));
                living.hurt(world.damageSources().magic(), 5f * dmgMult);
            }
        }
    }

    private static void castChainSpark(ServerPlayer player, ServerLevel world, float dmgMult) {
        LivingEntity first = getTargetEntity(player, 16);
        if (first == null) return;
        first.hurt(world.damageSources().magic(), 10f * dmgMult);
        LivingEntity second = null;
        double best = 64;
        for (Entity e : world.getEntities(player, first.getBoundingBox().inflate(8))) {
            if (e instanceof LivingEntity le && le != player && le != first) {
                double d = le.distanceToSqr(first);
                if (d < best) { best = d; second = le; }
            }
        }
        if (second != null) second.hurt(world.damageSources().magic(), 7f * dmgMult);
    }

    private static void castVoidGrasp(ServerPlayer player, ServerLevel world) {
        LivingEntity t = getTargetEntity(player, 12);
        if (t == null) return;
        Vec3 pull = player.position().subtract(t.position()).normalize().scale(0.8);
        t.setDeltaMovement(t.getDeltaMovement().add(pull.x, 0.35, pull.z));
        t.hurtMarked = true;
    }

    private static void castSolarFlare(ServerPlayer player, ServerLevel world, float dmgMult) {
        BlockHitResult hit = castBlockRay(player, 25);
        double x = player.getX(), y = player.getY(), z = player.getZ();
        if (hit != null) {
            BlockPos p = hit.getBlockPos();
            x = p.getX() + 0.5; y = p.getY() + 1; z = p.getZ() + 0.5;
        }
        world.explode(player, x, y, z, 2.2f, Level.ExplosionInteraction.NONE);
        for (Entity e : world.getEntities(player, new AABB(x - 3, y - 2, z - 3, x + 3, y + 3, z + 3))) {
            if (e instanceof LivingEntity living && e != player) {
                living.hurt(world.damageSources().magic(), 11f * dmgMult);
            }
        }
    }

    private static void castNetherBolt(ServerPlayer player, ServerLevel world, float dmgMult) {
        LivingEntity t = getTargetEntity(player, 18);
        if (t == null) return;
        t.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0));
        t.hurt(world.damageSources().magic(), 8f * dmgMult);
    }

    private static void castSoulSpark(ServerPlayer player, ServerLevel world, float dmgMult) {
        LivingEntity t = getTargetEntity(player, 14);
        if (t == null) return;
        t.hurt(world.damageSources().magic(), 6f * dmgMult);
        player.heal(3f);
    }

    private static void castStormBolt(ServerPlayer player, ServerLevel world) {
        BlockHitResult hit = castBlockRay(player, 32);
        if (hit == null) return;
        BlockPos pos = hit.getBlockPos();
        LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, world);
        bolt.teleportTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
        bolt.setCause(player);
        world.addFreshEntity(bolt);
    }

    private static void castGravityWell(ServerPlayer player, ServerLevel world) {
        for (Entity e : world.getEntities(player, player.getBoundingBox().inflate(10))) {
            if (e instanceof LivingEntity living && e != player) {
                living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 3));
                living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
            }
        }
    }

    private static void castLifeTap(ServerPlayer player) {
        if (player.getHealth() > 4f) {
            player.hurt(player.level().damageSources().magic(), 4f);
            String u = player.getUUID().toString();
            MagicManager.setMana(u, MagicManager.getMana(u) + Math.min(50f, MagicManager.getMaxMana(u) * 0.3f));
        }
    }

    private static void castRegenAura(ServerPlayer player, ServerLevel world) {
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 1));
        for (ServerPlayer nearby : world.getPlayers(p -> p.distanceToSqr(player) <= 64)) {
            nearby.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0));
        }
    }

    private static void castIceLance(ServerPlayer player, ServerLevel world, float dmgMult) {
        LivingEntity t = getTargetEntity(player, 22);
        if (t == null) return;
        t.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 3));
        t.hurt(world.damageSources().magic(), 12f * dmgMult);
    }

    private static void castShadowBolt(ServerPlayer player, ServerLevel world, float dmgMult) {
        LivingEntity t = getTargetEntity(player, 20);
        if (t == null) return;
        t.hurt(world.damageSources().magic(), 9f * dmgMult);
    }

    private static void castHolyBolt(ServerPlayer player, ServerLevel world, float dmgMult) {
        LivingEntity t = getTargetEntity(player, 18);
        if (t == null) return;
        float dmg = 10f * dmgMult;
        if (t.getType().getCategory() == MobCategory.MONSTER) dmg *= 1.25f;
        t.hurt(world.damageSources().magic(), dmg);
    }

    private static void castRockSpike(ServerPlayer player, ServerLevel world, float dmgMult) {
        BlockHitResult hit = castBlockRay(player, 22);
        if (hit == null) return;
        Vec3 c = hit.getLocation();
        for (Entity e : world.getEntities(player, new AABB(c.x - 2, c.y - 1, c.z - 2, c.x + 2, c.y + 3, c.z + 2))) {
            if (e instanceof LivingEntity living && e != player) {
                living.hurt(world.damageSources().magic(), 11f * dmgMult);
            }
        }
    }

    private static void castTidePush(ServerPlayer player, ServerLevel world) {
        castWindGust(player, world);
        for (Entity e : world.getEntities(player, player.getBoundingBox().inflate(6))) {
            if (e instanceof LivingEntity living && e != player) {
                living.addEffect(new MobEffectInstance(MobEffects.POISON, 40, 0));
            }
        }
    }

    private static void castWindShear(ServerPlayer player, ServerLevel world) {
        for (Entity e : world.getEntities(player, player.getBoundingBox().inflate(7))) {
            if (e instanceof LivingEntity living && e != player) {
                Vec3 dir = e.position().subtract(player.position()).normalize().scale(3.5);
                e.setDeltaMovement(dir.x, 0.8, dir.z);
                e.hurtMarked = true;
            }
        }
    }

    private static void castArcaneNova(ServerPlayer player, ServerLevel world, Set<String> unlocks, float dmgMult) {
        float d = 14f * dmgMult;
        for (Entity e : world.getEntities(player, player.getBoundingBox().inflate(8))) {
            if (e instanceof LivingEntity living && e != player) {
                living.hurt(world.damageSources().magic(), d);
            }
        }
    }

    private static void castBloodNova(ServerPlayer player, ServerLevel world, float dmgMult) {
        if (player.getHealth() <= 6f) return;
        player.hurt(world.damageSources().magic(), 4f);
        for (Entity e : world.getEntities(player, player.getBoundingBox().inflate(6))) {
            if (e instanceof LivingEntity living && e != player) {
                living.hurt(world.damageSources().magic(), 12f * dmgMult);
            }
        }
    }

    private static void castPhantomShift(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.SPEED, 60, 1));
        player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 40, 0));
    }

    private static void castStarShard(ServerPlayer player, ServerLevel world, Set<String> unlocks, float dmgMult) {
        castMeteor(player, world, unlocks, dmgMult * 0.65f);
    }

    private static void castManaSurge(ServerPlayer player) {
        String u = player.getUUID().toString();
        float cur = MagicManager.getMana(u);
        float max = MagicManager.getMaxMana(u);
        MagicManager.setMana(u, Math.min(max, cur + max * 0.45f));
    }

    // ════════════════════════════════════════════════════════════
    // TARGETING UTILITIES
    // ════════════════════════════════════════════════════════════

    private static LivingEntity getTargetEntity(ServerPlayer player, double range) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(range));
        AABB area = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0);

        LivingEntity closest = null;
        double bestDist = range * range;

        for (Entity e : player.level().getEntities(player, area)) {
            if (!(e instanceof LivingEntity living) || e == player) continue;
            AABB bb = e.getBoundingBox().inflate(0.3);
            Optional<Vec3> hitOpt = bb.clip(start, end);
            if (hitOpt.isPresent()) {
                double d = start.distanceToSqr(hitOpt.get());
                if (d < bestDist) {
                    bestDist = d;
                    closest = living;
                }
            }
        }
        return closest;
    }

    private static BlockHitResult castBlockRay(ServerPlayer player, double range) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(range));
        ClipContext ctx = new ClipContext(start, end,
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
        BlockHitResult result = player.level().clip(ctx);
        return result.getType() == HitResult.Type.BLOCK ? result : null;
    }

    // ════════════════════════════════════════════════════════════
    // MAGIC CREATURE SPAWNING
    // ════════════════════════════════════════════════════════════

    private static void trySpawnMagicCreatures(ServerLevel world) {
        for (ServerPlayer player : world.players()) {
            if (ThreadLocalRandom.current().nextFloat() > 0.05f) continue;

            MagicManager.MagicCreature type = rollCreatureType();
            if (type == null) continue;

            double angle = ThreadLocalRandom.current().nextDouble() * Math.PI * 2;
            double dist = 15 + ThreadLocalRandom.current().nextDouble() * 20;
            double x = player.getX() + Math.cos(angle) * dist;
            double z = player.getZ() + Math.sin(angle) * dist;
            int y = world.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) x, (int) z);

            LivingEntity creature = createMagicCreature(world, type, x, y, z);
            if (creature != null) {
                world.addFreshEntity(creature);
            }
        }
    }

    private static MagicManager.MagicCreature rollCreatureType() {
        float roll = ThreadLocalRandom.current().nextFloat();
        if (roll < 0.30f) return MagicManager.MagicCreature.ARCANE_WISP;
        if (roll < 0.48f) return MagicManager.MagicCreature.VOID_WALKER;
        if (roll < 0.62f) return MagicManager.MagicCreature.CRYSTAL_GOLEM;
        if (roll < 0.74f) return MagicManager.MagicCreature.SHADOW_MAGE;
        if (roll < 0.84f) return MagicManager.MagicCreature.STORM_PHANTOM;
        if (roll < 0.91f) return MagicManager.MagicCreature.ASHEN_HULK;
        if (roll < 0.97f) return MagicManager.MagicCreature.CELESTIAL_SERAPH;
        if (roll < 0.995f) return MagicManager.MagicCreature.RIFT_GUARDIAN;
        return MagicManager.MagicCreature.ABYSS_DEVOURER;
    }

    @SuppressWarnings("unchecked")
    private static LivingEntity createMagicCreature(ServerLevel world,
                                                     MagicManager.MagicCreature type,
                                                     double x, double y, double z) {
        Mob creature = switch (type) {
            case ARCANE_WISP   -> new Blaze(EntityType.BLAZE, world);
            case VOID_WALKER   -> new EnderMan(EntityType.ENDERMAN, world);
            case CRYSTAL_GOLEM -> {
                var golem = new net.minecraft.world.entity.animal.golem.IronGolem(EntityType.IRON_GOLEM, world);
                golem.setPlayerCreated(false);
                yield golem;
            }
            case SHADOW_MAGE   -> new Witch(EntityType.WITCH, world);
            case RIFT_GUARDIAN  -> new net.minecraft.world.entity.monster.warden.Warden(EntityType.WARDEN, world);
            case STORM_PHANTOM  -> new Phantom(EntityType.PHANTOM, world);
            case ASHEN_HULK     -> new net.minecraft.world.entity.monster.piglin.PiglinBrute(EntityType.PIGLIN_BRUTE, world);
            case CELESTIAL_SERAPH -> new Vex(EntityType.VEX, world);
            case ABYSS_DEVOURER -> new Ravager(EntityType.RAVAGER, world);
        };

        creature.teleportTo(x, y, z);
        creature.setCustomName(Component.literal("✦ " + type.displayName)
                .withStyle(type.color, ChatFormatting.BOLD));

        var healthAttr = creature.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.setBaseValue(type.health);
            creature.setHealth(type.health);
        }

        creature.addTag("magic_creature");
        creature.addTag("magic_" + type.name().toLowerCase());
        HealthScalingManager.registerWallOcclusionNameTag(creature);
        return creature;
    }

    // ════════════════════════════════════════════════════════════
    // KILL XP HANDLER
    // ════════════════════════════════════════════════════════════

    public static void onMagicCreatureKill(ServerPlayer killer, LivingEntity entity) {
        String name = entity.hasCustomName() ? entity.getCustomName().getString() : null;
        MagicManager.MagicCreature type = MagicManager.getCreatureType(name);
        if (type != null) {
            MagicManager.addXp(killer, type.xpReward);
            // Arcane Shards are the school-tree currency (kept separate from magic XP so
            // spending on nodes can never roll back your magic level).
            SchoolTreeManager.grantShardsSilent(killer.getUUID().toString(),
                    Math.max(1, type.xpReward / 20));
            String commissionName = switch (type) {
                case ARCANE_WISP -> "KILL_ARCANE_WISPS";
                case VOID_WALKER -> "KILL_VOID_WALKERS";
                case CRYSTAL_GOLEM -> "KILL_CRYSTAL_GOLEMS";
                case SHADOW_MAGE -> "KILL_SHADOW_MAGES";
                case RIFT_GUARDIAN -> "KILL_RIFT_GUARDIANS";
                case STORM_PHANTOM -> "HUNT_STORM_PHANTOMS";
                case ASHEN_HULK -> "HUNT_ASHEN_HULKS";
                case CELESTIAL_SERAPH -> "HUNT_CELESTIAL_SERAPHS";
                case ABYSS_DEVOURER -> "HUNT_ABYSS_DEVOURER";
            };
            MagicManager.progressCommission(killer, commissionName, 1);
            switch (type) {
                case ARCANE_WISP -> MagicManager.progressCommission(killer, "WISP_CULLER", 1);
                case VOID_WALKER -> MagicManager.progressCommission(killer, "VOID_HUNTER", 1);
                case CRYSTAL_GOLEM -> MagicManager.progressCommission(killer, "CRYSTAL_BREAKER", 1);
                case SHADOW_MAGE -> MagicManager.progressCommission(killer, "SHADOW_WAR", 1);
                case RIFT_GUARDIAN -> MagicManager.progressCommission(killer, "RIFT_TRIAL", 1);
                case STORM_PHANTOM -> MagicManager.progressCommission(killer, "HUNT_STORM_PHANTOMS", 1);
                case ASHEN_HULK -> MagicManager.progressCommission(killer, "HUNT_ASHEN_HULKS", 1);
                case CELESTIAL_SERAPH -> MagicManager.progressCommission(killer, "HUNT_CELESTIAL_SERAPHS", 1);
                case ABYSS_DEVOURER -> MagicManager.progressCommission(killer, "HUNT_ABYSS_DEVOURER", 1);
            }
        }
    }
}
