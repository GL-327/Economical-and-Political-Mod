package com.political;


import com.political.bounty.SlayerItems;
import com.political.bounty.SlayerManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WeaponAbilityHandler {

    private static final Map<UUID, Integer> hitCounters = new HashMap<>();
    private static final Map<String, Long> cooldowns = new HashMap<>();

    /**
     * Process weapon ability damage - call from damage event
     * Returns modified damage value
     */

    public static float processDamage(ServerPlayer player, LivingEntity target, float baseDamage) {
        ItemStack weapon = player.getMainHandItem();
        float finalDamage = baseDamage;

        // ===== WEAPON ATTRIBUTE SYSTEM =====
        WeaponAttribute weaponAttr = SlayerItems.getAppliedWeaponAttribute(weapon);
        if (weaponAttr != null) {
            finalDamage = processWeaponAttribute(player, target, finalDamage, weaponAttr);
        }

        // 1.21.11: Use DataComponentTypes for custom name
        Component customName = weapon.get(DataComponents.CUSTOM_NAME);
        String weaponName = customName != null ? customName.getString() : "";

        ServerLevel fxWorld = player.level() instanceof ServerLevel slw ? slw : null;

        // ===== ZOMBIE CLEAVER - Berserker Rage =====
        if (weaponName.contains("Cleaver") || weaponName.contains("Undying")) {
            float healthPercent = player.getHealth() / player.getMaxHealth();
            float berserkerMult = 1.0f + (1.0f - healthPercent) * 1.5f;
            finalDamage *= berserkerMult;

            // Lifesteal 10%
            player.heal(finalDamage * 0.1f);

            if (berserkerMult > 1.5f) {
                player.sendSystemMessage(Component.literal("§c§lBERSERK! §r§7" +
                        String.format("%.0f%%", berserkerMult * 100) + " damage"), true);
                if (fxWorld != null) {
                    fxWorld.sendParticles(net.minecraft.core.particles.ParticleTypes.ANGRY_VILLAGER,
                            player.getX(), player.getEyeY(), player.getZ(), 4, 0.4, 0.3, 0.4, 0.02);
                    fxWorld.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIMSON_SPORE,
                            target.getX(), target.getY() + 1.0, target.getZ(), 10, 0.4, 0.5, 0.4, 0.05);
                    fxWorld.playSound(null, player.blockPosition(),
                            net.minecraft.sounds.SoundEvents.ZOMBIE_AMBIENT,
                            net.minecraft.sounds.SoundSource.PLAYERS, 0.6f, 0.7f);
                }
            }
        }

        // ===== SPIDER FANG - Venomous Strike =====
        if (weaponName.contains("Fang") || weaponName.contains("Venomous")) {
            target.addEffect(new MobEffectInstance(
                    MobEffects.POISON, 100, 2, true, false, false
            ));
            finalDamage *= 1.25f;
            if (fxWorld != null) {
                fxWorld.sendParticles(net.minecraft.core.particles.ParticleTypes.ITEM_SLIME,
                        target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(), 6, 0.3, 0.3, 0.3, 0.05);
                fxWorld.playSound(null, target.blockPosition(),
                        net.minecraft.sounds.SoundEvents.SPIDER_HURT,
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.5f, 1.3f);
            }
        }

        // ===== SKELETON BLADE - Bone Shatter =====
        if (weaponName.contains("Bone") && weaponName.contains("Blade")) {
            float trueDamage = baseDamage * 0.3f;
            target.hurtServer((ServerLevel) player.level(), player.damageSources().magic(), trueDamage);
            finalDamage *= 1.1f;
            if (fxWorld != null) {
                fxWorld.sendParticles(net.minecraft.core.particles.ParticleTypes.WHITE_ASH,
                        target.getX(), target.getY() + 1.0, target.getZ(), 8, 0.35, 0.4, 0.35, 0.04);
                fxWorld.playSound(null, target.blockPosition(),
                        net.minecraft.sounds.SoundEvents.SKELETON_HURT,
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.5f, 1.4f);
            }
        }

        // ===== SLIME SWORD - Bouncy Strikes =====
        if (weaponName.contains("Slime") || weaponName.contains("Gelatinous")) {
            int hits = hitCounters.getOrDefault(player.getUUID(), 0) + 1;
            hitCounters.put(player.getUUID(), hits);

            if (hits % 3 == 0) {
                finalDamage *= 2.0f;
                player.sendSystemMessage(Component.literal("§a§lBOUNCE! §r§72x Damage!"), true);
                if (fxWorld != null) {
                    fxWorld.sendParticles(net.minecraft.core.particles.ParticleTypes.ITEM_SLIME,
                            target.getX(), target.getY() + 0.5, target.getZ(), 14, 0.5, 0.4, 0.5, 0.1);
                    fxWorld.playSound(null, target.blockPosition(),
                            net.minecraft.sounds.SoundEvents.SLIME_SQUISH,
                            net.minecraft.sounds.SoundSource.PLAYERS, 0.8f, 0.9f);
                }
            }
        }

        // ===== REINFORCED IRON SWORD - Sturdy Strike =====
        if (weaponName.contains("Reinforced Iron")) {
            // 15% chance to not consume durability
            if (player.getRandom().nextFloat() < 0.15f) {
                // Don't damage the weapon this hit (handled elsewhere)
                player.sendSystemMessage(Component.literal("§7§lSTURDY! §r§8Durability preserved"), true);
            }
            // Small damage bonus on consecutive hits
            int hits = hitCounters.getOrDefault(player.getUUID(), 0) + 1;
            hitCounters.put(player.getUUID(), hits);
            if (hits % 5 == 0) {
                finalDamage *= 1.2f;
                player.sendSystemMessage(Component.literal("§7§lREINFORCED! §r§8+20% damage"), true);
            }
        }

        // ===== HARDENED DIAMOND SWORD - Crystal Edge =====
        if (weaponName.contains("Hardened Diamond")) {
            // 25% chance for bonus armor piercing damage
            if (player.getRandom().nextFloat() < 0.25f) {
                float trueDamage = 2.0f;
                target.hurtServer((ServerLevel) player.level(), player.damageSources().magic(), trueDamage);
                player.sendSystemMessage(Component.literal("§b§lCRYSTAL EDGE! §r§7Armor pierced"), true);
                if (fxWorld != null) {
                    fxWorld.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,
                            target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(), 8, 0.3, 0.3, 0.3, 0.06);
                    fxWorld.playSound(null, target.blockPosition(),
                            net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME,
                            net.minecraft.sounds.SoundSource.PLAYERS, 0.9f, 1.6f);
                }
            }
            // Bonus damage to armored targets
            if (target instanceof Monster) {
                finalDamage *= 1.1f;
            }
        }

        // ===== VOID TOUCHED SWORD - Void Strike =====
        if (weaponName.contains("Void") || weaponName.contains("Ender") || weaponName.contains("Void-Touched")) {
            String cooldownKey = player.getUUID() + "_void_strike";
            long now = System.currentTimeMillis();
            long lastUse = cooldowns.getOrDefault(cooldownKey, 0L);

            if (now - lastUse > 5000) { // 5 second cooldown
                cooldowns.put(cooldownKey, now);

                // 1.21.11 FIX: Use getX(), getY(), getZ() instead of getPos()
                Vec3 targetPos = new Vec3(target.getX(), target.getY(), target.getZ());
                Vec3 lookVec = target.getLookAngle();
                Vec3 behind = targetPos.add(lookVec.scale(-2.0));

                // Teleport player behind target
                ServerLevel world = (ServerLevel) player.level();
                for (LivingEntity nearby : world.getEntitiesOfClass(
                        LivingEntity.class,
                        target.getBoundingBox().inflate(3.0),
                        e -> e != player && e != target && e.isAlive())) {
                    // THIS LINE MUST BE INSIDE THE FOR LOOP
                    nearby.hurtServer(world, player.damageSources().playerAttack(player), finalDamage * 0.5f);
                }

                finalDamage *= 2.5f; // Guaranteed crit
                player.sendSystemMessage(Component.literal("§5§lVOID STRIKE!"), true);

                // Void burst: portal spiral around the target + teleport audio.
                world = (ServerLevel) player.level();
                // A void lance tears across the gap between wielder and victim.
                Vfx.lance(world, player.getEyePosition(), target.position().add(0, target.getBbHeight() * 0.5, 0),
                        Vfx.VOID);
                Vfx.voidRift(world, target.position().add(0, 1, 0));
                world.playSound(null, player.blockPosition(),
                        net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT,
                        net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);
            }
        }

        // ===== THE GAVEL - Justice Strike =====
        if (weaponName.contains("Gavel") || weaponName.contains("Justice")) {
            // Bonus damage vs bosses
            if (SlayerManager.isSlayerBoss(target.getUUID())) {
                finalDamage *= 1.75f;
                player.sendSystemMessage(Component.literal("§e§lJUSTICE! §r§7+75% vs Boss"), true);
            }

            // AOE slam: splash damage + brief slow around the struck target.
            // Uses magic damage (no attacker) so the splash cannot re-trigger weapon procs recursively.
            ServerLevel world = (ServerLevel) player.level();
            int struck = 0;
            for (LivingEntity nearby : world.getEntitiesOfClass(
                    LivingEntity.class,
                    target.getBoundingBox().inflate(3.0),
                    e -> e != player && e != target && e.isAlive())) {
                nearby.hurtServer(world, player.damageSources().magic(), finalDamage * 0.4f);
                nearby.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1, true, false, false));
                struck++;
            }
            // Shockwave ring + gavel slam audio.
            for (int i = 0; i < 16; i++) {
                double angle = (Math.PI * 2 * i) / 16;
                world.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD,
                        target.getX() + Math.cos(angle) * 2.0, target.getY() + 0.2, target.getZ() + Math.sin(angle) * 2.0,
                        1, 0.05, 0.05, 0.05, 0.0);
            }
            world.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT,
                    target.getX(), target.getY() + 1.0, target.getZ(), 12, 1.2, 0.4, 1.2, 0.1);
            world.playSound(null, target.blockPosition(),
                    net.minecraft.sounds.SoundEvents.ANVIL_LAND,
                    net.minecraft.sounds.SoundSource.PLAYERS, 0.6f, 1.3f);
            if (struck > 0) {
                player.sendSystemMessage(Component.literal("§e§lORDER! §r§7Slammed " + struck + " nearby"), true);
            }
        }

        // ===== BOSS TYPE MULTIPLIER =====
        SlayerManager.SlayerType bossType = SlayerManager.getBossSlayerType(target.getUUID());
        if (bossType != null) {
            finalDamage *= getSlayerGearMultiplier(player, bossType);
        }

        // ===== FALLBACK: ANY CUSTOM WEAPON WITH AN ID GETS A UNIQUE PROC =====
        // Ensures every custom weapon has *some* linked unique behavior even if not explicitly handled above.
        String cid = SlayerItems.getCustomItemId(weapon);
        if (cid != null) {
            int sig = Math.floorMod(cid.hashCode(), 9);
            String cdKey = player.getUUID() + "_wproc_" + sig;
            long now = System.currentTimeMillis();
            long last = cooldowns.getOrDefault(cdKey, 0L);
            long cdMs = 1800L + sig * 350L; // 1.8s..4.6s
            if (now - last >= cdMs) {
                cooldowns.put(cdKey, now);
                ServerLevel world = (ServerLevel) player.level();
                switch (sig) {
                    case 0 -> { // Arcane burst
                        target.hurtServer(world, player.damageSources().magic(), 2.5f);
                        world.sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANT,
                                target.getX(), target.getY() + 1.0, target.getZ(), 8, 0.4, 0.5, 0.4, 0.12);
                    }
                    case 1 -> { // Bleed (wither)
                        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 50, 0, true, false, true));
                    }
                    case 2 -> { // Slow + bonus vs slowed
                        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1, true, false, true));
                        finalDamage *= 1.08f;
                    }
                    case 3 -> { // Lifesteal pulse
                        player.heal(Math.min(2.0f, finalDamage * 0.08f));
                        world.sendParticles(net.minecraft.core.particles.ParticleTypes.HEART,
                                player.getX(), player.getY() + 1.0, player.getZ(), 2, 0.2, 0.2, 0.2, 0.02);
                    }
                    case 4 -> { // Knockback smash
                        target.knockback(1.1f, player.getX() - target.getX(), player.getZ() - target.getZ());
                        finalDamage *= 1.06f;
                    }
                    case 5 -> { // Weakness
                        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 70, 0, true, false, true));
                    }
                    case 6 -> { // Mark: extra damage to monsters near target
                        for (LivingEntity e : world.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(2.8),
                                ent -> ent != player && ent != target && ent.isAlive() && ent instanceof Monster)) {
                            e.hurtServer(world, player.damageSources().magic(), 1.25f);
                        }
                        world.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT,
                                target.getX(), target.getY() + 1.0, target.getZ(), 6, 0.4, 0.4, 0.4, 0.06);
                    }
                    case 7 -> { // Tempo burst
                        player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 0, true, false, true));
                    }
                    default -> { // Blind
                        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 35, 0, true, false, true));
                    }
                }
            }
        }

        return finalDamage;
    }

    /**
     * Process weapon attribute effects
     */
    private static float processWeaponAttribute(ServerPlayer player, LivingEntity target, float damage, WeaponAttribute attr) {
        ServerLevel world = (ServerLevel) player.level();
        float finalDamage = damage;
        
        switch (attr) {
            case SHARP -> {
                // +15% Attack Damage & Critical hits
                finalDamage *= 1.15f;
                // 25% chance for critical hit (1.5x damage)
                if (world.getRandom().nextFloat() < 0.25f) {
                    finalDamage *= 1.5f;
                    player.sendSystemMessage(Component.literal("§c§lCRITICAL STRIKE!"), true);
                    world.sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANTED_HIT,
                            target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(), 10, 0.3, 0.3, 0.3, 0.15);
                    world.playSound(null, target.blockPosition(),
                            net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_CRIT,
                            net.minecraft.sounds.SoundSource.PLAYERS, 0.8f, 1.0f);
                }
            }
            case SWIFT -> {
                // +25% Attack Speed handled elsewhere, -20% damage
                finalDamage *= 0.8f;
            }
            case VAMPIRIC -> {
                // Lifesteal on hit (+10% max HP)
                float healAmount = player.getMaxHealth() * 0.1f;
                player.heal(healAmount);
                world.sendParticles(net.minecraft.core.particles.ParticleTypes.DAMAGE_INDICATOR,
                        target.getX(), target.getY() + target.getBbHeight() * 0.7, target.getZ(), 3, 0.25, 0.25, 0.25, 0.05);
                world.sendParticles(net.minecraft.core.particles.ParticleTypes.HEART,
                        player.getX(), player.getEyeY(), player.getZ(), 2, 0.25, 0.2, 0.25, 0.02);
            }
            case FROSTBITE -> {
                // Slows enemies & Freezes water
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2));
                target.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 60, 128)); // Prevents jumping
                world.sendParticles(net.minecraft.core.particles.ParticleTypes.SNOWFLAKE,
                        target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(), 7, 0.35, 0.35, 0.35, 0.03);
                // Reduced damage in hot biomes (Nether)
                if (world.dimensionType().hasCeiling()) {
                    finalDamage *= 0.7f;
                }
            }
            case THUNDER -> {
                // Lightning strikes & Chain damage
                if (world.getRandom().nextFloat() < 0.15f) {
                    // Strike target with lightning effect
                    world.playSound(null, target.blockPosition(),
                            net.minecraft.sounds.SoundEvents.LIGHTNING_BOLT_IMPACT,
                            net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);
                    target.hurtServer(world, player.damageSources().magic(), finalDamage * 0.3f);
                    // Full storm strike: a forked bolt from the sky and a ground shockwave.
                    Vfx.stormStrike(world, target.position());

                    // Chain to nearby enemies
                    for (LivingEntity nearby : world.getEntitiesOfClass(LivingEntity.class,
                            target.getBoundingBox().inflate(4), e -> e != player && e != target && e.isAlive())) {
                        nearby.hurtServer(world, player.damageSources().magic(), finalDamage * 0.2f);
                        world.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                                nearby.getX(), nearby.getY() + 1.0, nearby.getZ(), 5, 0.25, 0.4, 0.25, 0.05);
                    }
                    player.sendSystemMessage(Component.literal("§d§l⚡ THUNDER STRIKE!"), true);
                }
                // Self damage in rain
                if (world.isRaining()) {
                    player.hurtServer(world, player.damageSources().magic(), 2.0f);
                }
            }
            case POISON -> {
                // Poison enemies on hit
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
                // Rare self poison (5% chance)
                if (world.getRandom().nextFloat() < 0.05f) {
                    player.addEffect(new MobEffectInstance(MobEffects.POISON, 40, 0));
                }
            }
            case EXPLOSIVE -> {
                // Area damage & Knockback
                if (world.getRandom().nextFloat() < 0.2f) {
                    world.explode(target, target.getX(), target.getY(), target.getZ(), 2.0f,
                            net.minecraft.world.level.Level.ExplosionInteraction.NONE);
                    // Self damage chance
                    player.hurtServer(world, player.damageSources().explosion(null, null), 3.0f);
                    player.sendSystemMessage(Component.literal("§6§l✷ EXPLOSIVE HIT!"), true);
                }
            }
            case HOLY -> {
                // Extra damage to undead mobs (zombie, skeleton, phantom, etc.)
                if (target.getType().builtInRegistryHolder().is(net.minecraft.tags.EntityTypeTags.UNDEAD)) {
                    finalDamage *= 1.5f;
                    player.sendSystemMessage(Component.literal("§f§l✦ SMITE!"), true);
                    world.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,
                            target.getX(), target.getY() + target.getBbHeight(), target.getZ(), 10, 0.3, 0.5, 0.3, 0.08);
                } else {
                    // Reduced damage to normal mobs
                    finalDamage *= 0.85f;
                }
            }
            case SHADOW -> {
                // Invisibility on kill handled elsewhere
                // Reduced damage when sky light is high (daylight)
                int sky = world.getBrightness(net.minecraft.world.level.LightLayer.SKY, target.blockPosition());
                if (sky > 10) {
                    finalDamage *= 0.8f;
                }
            }
            case WIND -> {
                // Knockback & Flight boost
                target.knockback(1.5f, 
                        player.getX() - target.getX(), 
                        player.getZ() - target.getZ());
                // Reduced damage on ground
                if (player.onGround()) {
                    finalDamage *= 0.85f;
                }
            }
        }
        
        return finalDamage;
    }

    /**
     * Calculate damage multiplier based on slayer gear vs boss type
     */
    public static float getSlayerGearMultiplier(ServerPlayer player, SlayerManager.SlayerType bossType) {
        float multiplier = 1.0f;

        ItemStack weapon = player.getMainHandItem();
        Component weaponCustomName = weapon.get(DataComponents.CUSTOM_NAME);
        String weaponName = weaponCustomName != null ? weaponCustomName.getString() : "";

        // Matching slayer weapon = 50% more damage
        if (isMatchingSlayerWeapon(weaponName, bossType)) {
            multiplier *= 1.5f;
        }

        // Full set bonus = additional 25%
        if (hasFullSlayerSet(player, bossType)) {
            multiplier *= 1.25f;
        }

        // Slayer level bonus (from SlayerManager) [1]
        multiplier *= (float) SlayerManager.getLevelDamageMultiplier(player, bossType);

        return multiplier;
    }

    /**
     * Check if weapon matches boss type
     */
    private static boolean isMatchingSlayerWeapon(String weaponName, SlayerManager.SlayerType bossType) {
        return switch (bossType) {
            case ZOMBIE -> weaponName.contains("Cleaver") || weaponName.contains("Undying");
            case SPIDER -> weaponName.contains("Fang") || weaponName.contains("Venomous");
            case SKELETON -> weaponName.contains("Bone") && weaponName.contains("Blade");
            case SLIME -> weaponName.contains("Slime") || weaponName.contains("Gelatinous");
            case ENDERMAN -> weaponName.contains("Void") || weaponName.contains("Ender");
            case IRON_GOLEM -> weaponName.contains("Gavel") || weaponName.contains("Sculk");
            case PIGLIN -> weaponName.contains("Gilded") || weaponName.contains("Ravager");
        };
    }

    /**
     * Check if player has full slayer armor set
     */
    public static boolean hasFullSlayerSet(ServerPlayer player, SlayerManager.SlayerType type) {
        int matchingPieces = 0;


            ItemStack helmet = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD);
            ItemStack chestplate = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
            ItemStack leggings = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS);
            ItemStack boots = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET);

            List<ItemStack> armorPieces = List.of(helmet, chestplate, leggings, boots);

            for (ItemStack stack : armorPieces) {
                Component customName = stack.get(DataComponents.CUSTOM_NAME);
                String name = customName != null ? customName.getString() : "";

                if (isMatchingSlayerArmor(name, type)) {
                    matchingPieces++;
                }
            }

            return matchingPieces >= 4;
        }

    /**
     * Check if armor piece matches slayer type
     */
    private static boolean isMatchingSlayerArmor(String armorName, SlayerManager.SlayerType type) {
        return switch (type) {
            case ZOMBIE -> armorName.contains("Berserker") || armorName.contains("Undying");
            case SPIDER -> armorName.contains("Venomous") || armorName.contains("Spider");
            case SKELETON -> armorName.contains("Bone") || armorName.contains("Desperado");
            case SLIME -> armorName.contains("Slime") || armorName.contains("Gelatinous");
            case ENDERMAN -> armorName.contains("Void") || armorName.contains("Phantom");
            case IRON_GOLEM -> armorName.contains("Sculk") || armorName.contains("Warden");
            case PIGLIN -> armorName.contains("Gilded") || armorName.contains("Ravager") || armorName.contains("Crown");
        };
    }

    /**
     * Reset hit counter (call when player changes target or on death)
     */
    public static void resetHitCounter(UUID playerUuid) {
        hitCounters.remove(playerUuid);
    }

    /**
     * Check if ability is on cooldown
     */
    public static boolean isOnCooldown(UUID playerUuid, String abilityName, long cooldownMs) {
        String key = playerUuid + "_" + abilityName;
        long now = System.currentTimeMillis();
        long lastUse = cooldowns.getOrDefault(key, 0L);
        return (now - lastUse) < cooldownMs;
    }

    /**
     * Get remaining cooldown in seconds
     */
    public static float getRemainingCooldown(UUID playerUuid, String abilityName, long cooldownMs) {
        String key = playerUuid + "_" + abilityName;
        long now = System.currentTimeMillis();
        long lastUse = cooldowns.getOrDefault(key, 0L);
        long remaining = cooldownMs - (now - lastUse);
        return remaining > 0 ? remaining / 1000f : 0f;
    }
}