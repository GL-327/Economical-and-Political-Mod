package com.political;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

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

    public static float processDamage(ServerPlayerEntity player, LivingEntity target, float baseDamage) {
        ItemStack weapon = player.getMainHandStack();
        float finalDamage = baseDamage;

        // ===== WEAPON ATTRIBUTE SYSTEM =====
        WeaponAttribute weaponAttr = SlayerItems.getAppliedWeaponAttribute(weapon);
        if (weaponAttr != null) {
            finalDamage = processWeaponAttribute(player, target, finalDamage, weaponAttr);
        }

        // 1.21.11: Use DataComponentTypes for custom name
        Text customName = weapon.get(DataComponentTypes.CUSTOM_NAME);
        String weaponName = customName != null ? customName.getString() : "";

        // ===== ZOMBIE CLEAVER - Berserker Rage =====
        if (weaponName.contains("Cleaver") || weaponName.contains("Undying")) {
            float healthPercent = player.getHealth() / player.getMaxHealth();
            float berserkerMult = 1.0f + (1.0f - healthPercent) * 1.5f;
            finalDamage *= berserkerMult;

            // Lifesteal 10%
            player.heal(finalDamage * 0.1f);

            if (berserkerMult > 1.5f) {
                player.sendMessage(Text.literal("§c§lBERSERK! §r§7" +
                        String.format("%.0f%%", berserkerMult * 100) + " damage"), true);
            }
        }

        // ===== SPIDER FANG - Venomous Strike =====
        if (weaponName.contains("Fang") || weaponName.contains("Venomous")) {
            target.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.POISON, 100, 2, true, false, false
            ));
            finalDamage *= 1.25f;
        }

        // ===== SKELETON BLADE - Bone Shatter =====
        if (weaponName.contains("Bone") && weaponName.contains("Blade")) {
            float trueDamage = baseDamage * 0.3f;
            target.damage((ServerWorld) player.getEntityWorld(), player.getDamageSources().magic(), trueDamage);
            finalDamage *= 1.1f;
        }

        // ===== SLIME SWORD - Bouncy Strikes =====
        if (weaponName.contains("Slime") || weaponName.contains("Gelatinous")) {
            int hits = hitCounters.getOrDefault(player.getUuid(), 0) + 1;
            hitCounters.put(player.getUuid(), hits);

            if (hits % 3 == 0) {
                finalDamage *= 2.0f;
                player.sendMessage(Text.literal("§a§lBOUNCE! §r§72x Damage!"), true);
            }
        }

        // ===== ENDERMAN BLADE - Void Strike =====
        if (weaponName.contains("Void") || weaponName.contains("Ender")) {
            String cooldownKey = player.getUuid() + "_void_strike";
            long now = System.currentTimeMillis();
            long lastUse = cooldowns.getOrDefault(cooldownKey, 0L);

            if (now - lastUse > 5000) { // 5 second cooldown
                cooldowns.put(cooldownKey, now);

                // 1.21.11 FIX: Use getX(), getY(), getZ() instead of getPos()
                Vec3d targetPos = new Vec3d(target.getX(), target.getY(), target.getZ());
                Vec3d lookVec = target.getRotationVector();
                Vec3d behind = targetPos.add(lookVec.multiply(-2.0));

                // Teleport player behind target
                ServerWorld world = (ServerWorld) player.getEntityWorld();
                for (LivingEntity nearby : world.getEntitiesByClass(
                        LivingEntity.class,
                        target.getBoundingBox().expand(3.0),
                        e -> e != player && e != target && e.isAlive())) {
                    // THIS LINE MUST BE INSIDE THE FOR LOOP
                    nearby.damage(world, player.getDamageSources().playerAttack(player), finalDamage * 0.5f);
                }

                finalDamage *= 2.5f; // Guaranteed crit
                player.sendMessage(Text.literal("§5§lVOID STRIKE!"), true);

                // Particle effect
                world = player.getEntityWorld();
                world.playSound(null, player.getBlockPos(),
                        net.minecraft.sound.SoundEvents.ENTITY_ENDERMAN_TELEPORT,
                        net.minecraft.sound.SoundCategory.PLAYERS, 1.0f, 1.0f);
            }
        }

        // ===== THE GAVEL - Justice Strike =====
        if (weaponName.contains("Gavel") || weaponName.contains("Justice")) {
            // Bonus damage vs bosses
            if (SlayerManager.isSlayerBoss(target.getUuid())) {
                finalDamage *= 1.75f;
                player.sendMessage(Text.literal("§e§lJUSTICE! §r§7+75% vs Boss"), true);
            }

            // AOE damage
            ServerWorld world = player.getEntityWorld();
            for (LivingEntity nearby : world.getEntitiesByClass(
                    LivingEntity.class,
                    target.getBoundingBox().expand(3.0),
                    e -> e != player && e != target && e.isAlive())) {
                Vec3d targetPos = new Vec3d(target.getX(), target.getY(), target.getZ());
                Vec3d lookVec = target.getRotationVector();
                Vec3d behind = targetPos.add(lookVec.multiply(-2.0));

                // Teleport - MUST BE IN SAME BLOCK AS 'behind' DECLARATION
                player.requestTeleport(behind.x, behind.y, behind.z);

            }
        }

        // ===== BOSS TYPE MULTIPLIER =====
        SlayerManager.SlayerType bossType = SlayerManager.getBossSlayerType(target.getUuid());
        if (bossType != null) {
            finalDamage *= getSlayerGearMultiplier(player, bossType);
        }

        return finalDamage;
    }

    /**
     * Process weapon attribute effects
     */
    private static float processWeaponAttribute(ServerPlayerEntity player, LivingEntity target, float damage, WeaponAttribute attr) {
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        float finalDamage = damage;
        
        switch (attr) {
            case SHARP -> {
                // +15% Attack Damage & Critical hits
                finalDamage *= 1.15f;
                // 25% chance for critical hit (1.5x damage)
                if (world.getRandom().nextFloat() < 0.25f) {
                    finalDamage *= 1.5f;
                    player.sendMessage(Text.literal("§c§lCRITICAL STRIKE!"), true);
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
            }
            case FROSTBITE -> {
                // Slows enemies & Freezes water
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 2));
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 60, 128)); // Prevents jumping
                // Reduced damage in hot biomes (Nether)
                if (world.getDimension().hasCeiling()) {
                    finalDamage *= 0.7f;
                }
            }
            case THUNDER -> {
                // Lightning strikes & Chain damage
                if (world.getRandom().nextFloat() < 0.15f) {
                    // Strike target with lightning effect
                    world.playSound(null, target.getBlockPos(), 
                            net.minecraft.sound.SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT,
                            net.minecraft.sound.SoundCategory.PLAYERS, 1.0f, 1.0f);
                    target.damage(world, player.getDamageSources().magic(), finalDamage * 0.3f);
                    
                    // Chain to nearby enemies
                    for (LivingEntity nearby : world.getEntitiesByClass(LivingEntity.class,
                            target.getBoundingBox().expand(4), e -> e != player && e != target && e.isAlive())) {
                        nearby.damage(world, player.getDamageSources().magic(), finalDamage * 0.2f);
                    }
                    player.sendMessage(Text.literal("§d§l⚡ THUNDER STRIKE!"), true);
                }
                // Self damage in rain
                if (world.isRaining()) {
                    player.damage(world, player.getDamageSources().magic(), 2.0f);
                }
            }
            case POISON -> {
                // Poison enemies on hit
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 100, 1));
                // Rare self poison (5% chance)
                if (world.getRandom().nextFloat() < 0.05f) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 40, 0));
                }
            }
            case EXPLOSIVE -> {
                // Area damage & Knockback
                if (world.getRandom().nextFloat() < 0.2f) {
                    world.createExplosion(target, target.getX(), target.getY(), target.getZ(), 2.0f, 
                            net.minecraft.world.World.ExplosionSourceType.NONE);
                    // Self damage chance
                    player.damage(world, player.getDamageSources().explosion(null, null), 3.0f);
                    player.sendMessage(Text.literal("§6§l💥 EXPLOSIVE HIT!"), true);
                }
            }
            case HOLY -> {
                // Extra damage to undead mobs (zombie, skeleton, phantom, etc.)
                if (target.getType().isIn(net.minecraft.registry.tag.EntityTypeTags.UNDEAD)) {
                    finalDamage *= 1.5f;
                    player.sendMessage(Text.literal("§f§l✧ SMITE!"), true);
                } else {
                    // Reduced damage to normal mobs
                    finalDamage *= 0.85f;
                }
            }
            case SHADOW -> {
                // Invisibility on kill handled elsewhere
                // Reduced damage in daylight
                long time = world.getTimeOfDay() % 24000;
                if (time >= 0 && time < 12000) { // Daytime
                    finalDamage *= 0.8f;
                }
            }
            case WIND -> {
                // Knockback & Flight boost
                target.takeKnockback(1.5f, 
                        player.getX() - target.getX(), 
                        player.getZ() - target.getZ());
                // Reduced damage on ground
                if (player.isOnGround()) {
                    finalDamage *= 0.85f;
                }
            }
        }
        
        return finalDamage;
    }

    /**
     * Calculate damage multiplier based on slayer gear vs boss type
     */
    public static float getSlayerGearMultiplier(ServerPlayerEntity player, SlayerManager.SlayerType bossType) {
        float multiplier = 1.0f;

        ItemStack weapon = player.getMainHandStack();
        Text weaponCustomName = weapon.get(DataComponentTypes.CUSTOM_NAME);
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
    public static boolean hasFullSlayerSet(ServerPlayerEntity player, SlayerManager.SlayerType type) {
        int matchingPieces = 0;


            ItemStack helmet = player.getEquippedStack(net.minecraft.entity.EquipmentSlot.HEAD);
            ItemStack chestplate = player.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST);
            ItemStack leggings = player.getEquippedStack(net.minecraft.entity.EquipmentSlot.LEGS);
            ItemStack boots = player.getEquippedStack(net.minecraft.entity.EquipmentSlot.FEET);

            List<ItemStack> armorPieces = List.of(helmet, chestplate, leggings, boots);

            for (ItemStack stack : armorPieces) {
                Text customName = stack.get(DataComponentTypes.CUSTOM_NAME);
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