package com.political;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Classes System
 * 10 unique classes with buffs and debuffs
 */
public class ClassManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassManager.class);


    // ════════════════════════════════════════════════════════════
    // CLASS DEFINITIONS
    // ════════════════════════════════════════════════════════════

    public enum PlayerClass {
        WARRIOR("Warrior", "⚔", ChatFormatting.RED,
            "Master of melee combat",
            new ClassEffect[]{
                new ClassEffect("Strength", MobEffects.STRENGTH, 0, true),
                new ClassEffect("Resistance", MobEffects.RESISTANCE, 0, true)
            },
            new ClassEffect[]{
                new ClassEffect("Clumsy", MobEffects.MINING_FATIGUE, 0, true)
            },
            1.2, 0.9, 1.0), // +20% melee damage, -10% mining speed, normal defense

        MAGE("Mage", "🔮", ChatFormatting.LIGHT_PURPLE,
            "Wielder of arcane powers",
            new ClassEffect[]{
                new ClassEffect("Arcane Wisdom", MobEffects.INSTANT_DAMAGE, 0, false), // Bonus spell damage
                new ClassEffect("Mana Regeneration", MobEffects.HASTE, 0, true)
            },
            new ClassEffect[]{
                new ClassEffect("Fragile", MobEffects.WEAKNESS, 0, true)
            },
            0.8, 1.0, 0.85), // -20% melee damage, normal mining, -15% defense

        RANGER("Ranger", "🏹", ChatFormatting.GREEN,
            "Swift and deadly archer",
            new ClassEffect[]{
                new ClassEffect("Swiftness", MobEffects.SPEED, 1, true),
                new ClassEffect("Jump Boost", MobEffects.JUMP_BOOST, 1, true)
            },
            new ClassEffect[]{
                new ClassEffect("Light Armor", MobEffects.RESISTANCE, 0, false) // Reduced armor
            },
            0.9, 1.0, 0.9), // -10% melee damage, normal mining, -10% defense

        PALADIN("Paladin", "🛡", ChatFormatting.YELLOW,
            "Holy protector of the innocent",
            new ClassEffect[]{
                new ClassEffect("Divine Shield", MobEffects.RESISTANCE, 1, true),
                new ClassEffect("Regeneration", MobEffects.REGENERATION, 0, true)
            },
            new ClassEffect[]{
                new ClassEffect("Heavy Armor", MobEffects.SLOWNESS, 0, true)
            },
            0.9, 0.8, 1.3), // -10% melee damage, -20% speed, +30% defense

        ROGUE("Rogue", "🗡", ChatFormatting.DARK_GRAY,
            "Silent assassin in the shadows",
            new ClassEffect[]{
                new ClassEffect("Invisibility", MobEffects.INVISIBILITY, 0, false),
                new ClassEffect("Night Vision", MobEffects.NIGHT_VISION, 0, true)
            },
            new ClassEffect[]{
                new ClassEffect("Low Health", MobEffects.HEALTH_BOOST, 0, false) // Reduced max health
            },
            1.15, 1.0, 0.8), // +15% melee damage, normal mining, -20% defense

        NECROMANCER("Necromancer", "💀", ChatFormatting.DARK_PURPLE,
            "Dark mage commanding the dead",
            new ClassEffect[]{
                new ClassEffect("Wither Touch", MobEffects.WITHER, 0, false), // Wither on hit
                new ClassEffect("Soul Harvest", MobEffects.ABSORPTION, 0, false) // Heal on kill
            },
            new ClassEffect[]{
                new ClassEffect("Undead Curse", MobEffects.UNLUCK, 0, true)
            },
            0.85, 1.0, 0.9), // -15% melee damage, normal mining, -10% defense

        BERSERKER("Berserker", "🪓", ChatFormatting.DARK_RED,
            "Raging warrior of destruction",
            new ClassEffect[]{
                new ClassEffect("Rage", MobEffects.STRENGTH, 2, true),
                new ClassEffect("Fury", MobEffects.SPEED, 0, true)
            },
            new ClassEffect[]{
                new ClassEffect("Reckless", MobEffects.RESISTANCE, 0, false) // Reduced resistance
            },
            1.4, 1.0, 0.7), // +40% melee damage, normal mining, -30% defense

        CLERIC("Cleric", "✝", ChatFormatting.WHITE,
            "Divine healer and supporter",
            new ClassEffect[]{
                new ClassEffect("Healing Aura", MobEffects.REGENERATION, 1, true),
                new ClassEffect("Blessing", MobEffects.LUCK, 1, true)
            },
            new ClassEffect[]{
                new ClassEffect("Pacifist", MobEffects.WEAKNESS, 1, true)
            },
            0.7, 1.0, 1.1), // -30% melee damage, normal mining, +10% defense

        DRUID("Druid", "🌿", ChatFormatting.DARK_GREEN,
            "Nature's guardian and shapeshifter",
            new ClassEffect[]{
                new ClassEffect("Nature's Blessing", MobEffects.SATURATION, 0, false),
                new ClassEffect("Forest Walk", MobEffects.SPEED, 0, true)
            },
            new ClassEffect[]{
                new ClassEffect("Wild Form", MobEffects.SLOWNESS, 0, false)
            },
            0.95, 1.0, 1.0), // -5% melee damage, normal mining, normal defense

        BARD("Bard", "🎵", ChatFormatting.GOLD,
            "Melodic performer and inspirer",
            new ClassEffect[]{
                new ClassEffect("Inspiration", MobEffects.HASTE, 1, true),
                new ClassEffect("Charisma", MobEffects.HERO_OF_THE_VILLAGE, 0, false)
            },
            new ClassEffect[]{
                new ClassEffect("Non-combatant", MobEffects.WEAKNESS, 0, true)
            },
            0.75, 1.0, 1.0); // -25% melee damage, normal mining, normal defense

        public final String name;
        public final String icon;
        public final ChatFormatting color;
        public final String description;
        public final ClassEffect[] buffs;
        public final ClassEffect[] debuffs;
        public final double meleeMultiplier;
        public final double miningMultiplier;
        public final double defenseMultiplier;

        PlayerClass(String name, String icon, ChatFormatting color, String description,
                    ClassEffect[] buffs, ClassEffect[] debuffs,
                    double meleeMultiplier, double miningMultiplier, double defenseMultiplier) {
            this.name = name;
            this.icon = icon;
            this.color = color;
            this.description = description;
            this.buffs = buffs;
            this.debuffs = debuffs;
            this.meleeMultiplier = meleeMultiplier;
            this.miningMultiplier = miningMultiplier;
            this.defenseMultiplier = defenseMultiplier;
        }
    }

    public static class ClassEffect {
        public final String name;
        public final Holder<net.minecraft.world.effect.MobEffect> effect;
        public final int amplifier;
        public final boolean permanent;

        public ClassEffect(String name, Holder<net.minecraft.world.effect.MobEffect> effect, int amplifier, boolean permanent) {
            this.name = name;
            this.effect = effect;
            this.amplifier = amplifier;
            this.permanent = permanent;
        }
        
        public Holder<net.minecraft.world.effect.MobEffect> getEffect() {
            return effect;
        }
    }

    // ════════════════════════════════════════════════════════════
    // DATA STORAGE
    // ════════════════════════════════════════════════════════════

    // Player UUID -> Selected Class
    public static final Map<String, PlayerClass> playerClasses = new ConcurrentHashMap<>();
    
    // Player UUID -> Class Experience
    public static final Map<String, Map<PlayerClass, Integer>> playerClassExperience = new ConcurrentHashMap<>();
    
    // Player UUID -> Time when class was selected (for cooldown)
    public static final Map<String, Long> classChangeCooldown = new ConcurrentHashMap<>();
    
    // Player UUID -> Whether class is hidden from scoreboard
    public static final Map<String, Boolean> classDisplayHidden = new ConcurrentHashMap<>();

    // ════════════════════════════════════════════════════════════
    // CONFIGURATION
    // ════════════════════════════════════════════════════════════

    private static final long CLASS_CHANGE_COOLDOWN = 2 * 60 * 60 * 1000; // 2 hours
    private static final long CLASS_CHANGE_COOLDOWN_INITIAL = 24 * 60 * 60 * 1000; // 24 hours for first change
    private static final int MAX_CLASS_LEVEL = 50;
    private static final int XP_PER_LEVEL = 1000;

    // ════════════════════════════════════════════════════════════
    // DATA PERSISTENCE
    // ════════════════════════════════════════════════════════════

    private static Path dataPath;
    private static final String DATA_FILE = "classes.dat";

    public static void initialize(Path savePath) {
        dataPath = savePath;
        loadData();
    }

    private static void loadData() {
        if (dataPath == null) return;
        
        File file = dataPath.resolve(DATA_FILE).toFile();
        if (!file.exists()) return;

        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
            int playerCount = dis.readInt();
            for (int i = 0; i < playerCount; i++) {
                String uuid = dis.readUTF();
                
                // Load selected class
                int classOrdinal = dis.readInt();
                if (classOrdinal >= 0 && classOrdinal < PlayerClass.values().length) {
                    playerClasses.put(uuid, PlayerClass.values()[classOrdinal]);
                }
                
                // Load class change cooldown
                long cooldown = dis.readLong();
                if (cooldown > 0) {
                    classChangeCooldown.put(uuid, cooldown);
                }
                
                // Load class experience
                int expCount = dis.readInt();
                Map<PlayerClass, Integer> exp = new HashMap<>();
                for (int j = 0; j < expCount; j++) {
                    int classOrd = dis.readInt();
                    int xp = dis.readInt();
                    if (classOrd >= 0 && classOrd < PlayerClass.values().length) {
                        exp.put(PlayerClass.values()[classOrd], xp);
                    }
                }
                playerClassExperience.put(uuid, exp);
            }
        } catch (IOException e) {
            LOGGER.error("ClassManager.loadData failed", e);
        }
    }

    public static void saveData() {
        if (dataPath == null) return;
        
        File file = dataPath.resolve(DATA_FILE).toFile();
        file.getParentFile().mkdirs();

        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file))) {
            dos.writeInt(playerClasses.size());
            
            for (String uuid : playerClasses.keySet()) {
                dos.writeUTF(uuid);
                
                PlayerClass selected = playerClasses.get(uuid);
                dos.writeInt(selected != null ? selected.ordinal() : -1);
                
                Long cooldown = classChangeCooldown.get(uuid);
                dos.writeLong(cooldown != null ? cooldown : 0);
                
                Map<PlayerClass, Integer> exp = playerClassExperience.getOrDefault(uuid, Map.of());
                dos.writeInt(exp.size());
                for (Map.Entry<PlayerClass, Integer> entry : exp.entrySet()) {
                    dos.writeInt(entry.getKey().ordinal());
                    dos.writeInt(entry.getValue());
                }
            }
        } catch (IOException e) {
            LOGGER.error("ClassManager.saveData failed", e);
        }
    }

    // ════════════════════════════════════════════════════════════
    // PLAYER MANAGEMENT
    // ════════════════════════════════════════════════════════════

    public static void registerPlayer(String uuid) {
        if (!playerClassExperience.containsKey(uuid)) {
            playerClassExperience.put(uuid, new HashMap<>());
        }
    }

    public static PlayerClass getPlayerClass(String uuid) {
        return playerClasses.get(uuid);
    }

    public static int getClassLevel(String uuid, PlayerClass playerClass) {
        Map<PlayerClass, Integer> exp = playerClassExperience.get(uuid);
        if (exp == null) return 1;
        int xp = exp.getOrDefault(playerClass, 0);
        return Math.min(1 + xp / XP_PER_LEVEL, MAX_CLASS_LEVEL);
    }

    public static int getClassExperience(String uuid, PlayerClass playerClass) {
        Map<PlayerClass, Integer> exp = playerClassExperience.get(uuid);
        return exp != null ? exp.getOrDefault(playerClass, 0) : 0;
    }

    public static long getTimeUntilClassChange(String uuid) {
        Long lastChange = classChangeCooldown.get(uuid);
        if (lastChange == null) return 0;
        long cooldown = playerClasses.containsKey(uuid) ? CLASS_CHANGE_COOLDOWN : CLASS_CHANGE_COOLDOWN_INITIAL;
        long remaining = cooldown - (System.currentTimeMillis() - lastChange);
        return Math.max(0, remaining);
    }
    
    public static boolean isClassDisplayHidden(String uuid) {
        return classDisplayHidden.getOrDefault(uuid, false);
    }
    
    public static void toggleClassDisplay(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        boolean current = classDisplayHidden.getOrDefault(uuid, false);
        classDisplayHidden.put(uuid, !current);
        if (!current) {
            player.sendSystemMessage(Component.literal("§7Class display hidden from scoreboard. Use /class toggle to show again."), false);
        } else {
            player.sendSystemMessage(Component.literal("§aClass display enabled on scoreboard."), false);
        }
    }

    // ════════════════════════════════════════════════════════════
    // CLASS SELECTION
    // ════════════════════════════════════════════════════════════

    /**
     * Select a class for a player
     */
    public static boolean selectClass(ServerPlayer player, PlayerClass newClass) {
        String uuid = player.getUUID().toString();
        
        // Check cooldown
        long cooldown = getTimeUntilClassChange(uuid);
        if (cooldown > 0 && playerClasses.containsKey(uuid)) {
            player.sendSystemMessage(Component.literal("§cCannot change class for " + (cooldown / 3600000) + " hours!"), false);
            return false;
        }
        
        // Remove old class effects
        PlayerClass oldClass = playerClasses.get(uuid);
        if (oldClass != null) {
            removeClassEffects(player, oldClass);
        }
        
        // Set new class
        playerClasses.put(uuid, newClass);
        classChangeCooldown.put(uuid, System.currentTimeMillis());
        
        // Apply new class effects
        applyClassEffects(player, newClass);
        MasteryManager.syncMasteryTrackFromClass(player, newClass);

        player.sendSystemMessage(Component.literal("§a★ Class selected: " + newClass.icon + " " + newClass.name), false);
        player.sendSystemMessage(Component.literal("§7" + newClass.description), false);
        
        saveData();
        return true;
    }

    /**
     * Apply class effects to a player
     */
    public static void applyClassEffects(ServerPlayer player, PlayerClass playerClass) {
        int level = getClassLevel(player.getUUID().toString(), playerClass);
        int bonusAmplifier = (level - 1) / 10; // +1 amplifier every 10 levels
        
        // Apply attribute modifiers based on class
        applyAttributeModifiers(player, playerClass);
        
        // Apply buffs
        for (ClassEffect effect : playerClass.buffs) {
            int amplifier = effect.amplifier + bonusAmplifier;
            if (effect.permanent) {
                player.addEffect(new MobEffectInstance(effect.getEffect(), Integer.MAX_VALUE, amplifier, true, false, true));
            }
        }
        
        // Apply debuffs
        for (ClassEffect effect : playerClass.debuffs) {
            if (effect.permanent) {
                player.addEffect(new MobEffectInstance(effect.getEffect(), Integer.MAX_VALUE, effect.amplifier, true, false, true));
            }
        }
    }
    
    private static void applyAttributeModifiers(ServerPlayer player, PlayerClass playerClass) {
        // Remove existing modifiers first
        removeAttributeModifiers(player, playerClass);
        
        // Get attribute instance
        AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance miningSpeed = player.getAttribute(Attributes.MINING_EFFICIENCY);
        AttributeInstance armor = player.getAttribute(Attributes.ARMOR);
        
        if (attackDamage != null) {
            double meleeBonus = playerClass.meleeMultiplier - 1.0;
            attackDamage.addPermanentModifier(new AttributeModifier(
                Identifier.fromNamespaceAndPath("politicalserver", "class_melee_bonus"),
                meleeBonus,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }
        
        if (miningSpeed != null) {
            double miningBonus = playerClass.miningMultiplier - 1.0;
            miningSpeed.addPermanentModifier(new AttributeModifier(
                Identifier.fromNamespaceAndPath("politicalserver", "class_mining_bonus"),
                miningBonus,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }
        
        if (armor != null) {
            double defenseBonus = playerClass.defenseMultiplier - 1.0;
            armor.addPermanentModifier(new AttributeModifier(
                Identifier.fromNamespaceAndPath("politicalserver", "class_defense_bonus"),
                defenseBonus,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }
    }
    
    private static void removeAttributeModifiers(ServerPlayer player, PlayerClass playerClass) {
        AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance miningSpeed = player.getAttribute(Attributes.MINING_EFFICIENCY);
        AttributeInstance armor = player.getAttribute(Attributes.ARMOR);
        
        if (attackDamage != null) {
            attackDamage.removeModifier(Identifier.fromNamespaceAndPath("politicalserver", "class_melee_bonus"));
        }
        if (miningSpeed != null) {
            miningSpeed.removeModifier(Identifier.fromNamespaceAndPath("politicalserver", "class_mining_bonus"));
        }
        if (armor != null) {
            armor.removeModifier(Identifier.fromNamespaceAndPath("politicalserver", "class_defense_bonus"));
        }
    }

    /**
     * Remove class effects from a player
     */
    public static void removeClassEffects(ServerPlayer player, PlayerClass playerClass) {
        // Remove buffs
        for (ClassEffect effect : playerClass.buffs) {
            player.removeEffect(effect.getEffect());
        }
        
        // Remove debuffs
        for (ClassEffect effect : playerClass.debuffs) {
            player.removeEffect(effect.getEffect());
        }
    }

    /**
     * Reapply class effects (call on player join, respawn, etc.)
     */
    public static void reapplyClassEffects(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        PlayerClass playerClass = playerClasses.get(uuid);
        if (playerClass != null) {
            applyClassEffects(player, playerClass);
            MasteryManager.syncMasteryTrackFromClass(player, playerClass);
        }
    }

    // ════════════════════════════════════════════════════════════
    // EXPERIENCE & LEVELING
    // ════════════════════════════════════════════════════════════

    /**
     * Award class experience
     */
    public static void awardClassExperience(ServerPlayer player, int amount) {
        String uuid = player.getUUID().toString();
        PlayerClass playerClass = playerClasses.get(uuid);
        
        if (playerClass == null) return;
        
        Map<PlayerClass, Integer> exp = playerClassExperience.computeIfAbsent(uuid, k -> new HashMap<>());
        int current = exp.getOrDefault(playerClass, 0);
        int newExp = current + amount;
        exp.put(playerClass, newExp);
        
        int oldLevel = 1 + current / XP_PER_LEVEL;
        int newLevel = 1 + newExp / XP_PER_LEVEL;
        
        if (newLevel > oldLevel && newLevel <= MAX_CLASS_LEVEL) {
            player.sendSystemMessage(Component.literal("§6★ " + playerClass.name + " Level Up! §f" + oldLevel + " → " + newLevel), false);
            
            // Reapply effects with new bonuses
            removeClassEffects(player, playerClass);
            applyClassEffects(player, playerClass);
        }
        
        saveData();
    }

    // ════════════════════════════════════════════════════════════
    // DAMAGE CALCULATIONS
    // ════════════════════════════════════════════════════════════

    /**
     * Get melee damage multiplier for a player
     */
    public static double getMeleeMultiplier(String uuid) {
        PlayerClass playerClass = playerClasses.get(uuid);
        if (playerClass == null) return 1.0;
        
        int level = getClassLevel(uuid, playerClass);
        double levelBonus = 0.01 * (level - 1); // +1% per level
        
        return playerClass.meleeMultiplier + levelBonus;
    }

    /**
     * Get defense multiplier for a player
     */
    public static double getDefenseMultiplier(String uuid) {
        PlayerClass playerClass = playerClasses.get(uuid);
        if (playerClass == null) return 1.0;
        
        int level = getClassLevel(uuid, playerClass);
        double levelBonus = 0.005 * (level - 1); // +0.5% per level
        
        return playerClass.defenseMultiplier + levelBonus;
    }

    /**
     * Get mining speed multiplier for a player
     */
    public static double getMiningMultiplier(String uuid) {
        PlayerClass playerClass = playerClasses.get(uuid);
        if (playerClass == null) return 1.0;
        
        return playerClass.miningMultiplier;
    }

    // ════════════════════════════════════════════════════════════
    // DISPLAY
    // ════════════════════════════════════════════════════════════

    public static void displayClassInfo(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        PlayerClass playerClass = playerClasses.get(uuid);
        
        player.sendSystemMessage(Component.literal("═══════════════════════════════════")
            .withStyle(ChatFormatting.GOLD), false);
        player.sendSystemMessage(Component.literal("         ⚔ CLASS STATUS ⚔")
            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        player.sendSystemMessage(Component.literal("═══════════════════════════════════")
            .withStyle(ChatFormatting.GOLD), false);
        
        if (playerClass == null) {
            player.sendSystemMessage(Component.literal("§7No class selected"), false);
            player.sendSystemMessage(Component.literal("§eUse /class select to choose a class!"), false);
        } else {
            int level = getClassLevel(uuid, playerClass);
            int xp = getClassExperience(uuid, playerClass);
            int xpNeeded = XP_PER_LEVEL;
            int xpProgress = xp % XP_PER_LEVEL;
            
            player.sendSystemMessage(Component.literal(playerClass.icon + " " + playerClass.name + " §7(Level " + level + ")")
                .withStyle(playerClass.color), false);
            player.sendSystemMessage(Component.literal("§7" + playerClass.description), false);
            player.sendSystemMessage(Component.literal(""), false);
            player.sendSystemMessage(Component.literal("§bXP: §f" + xpProgress + "/" + xpNeeded + " (" + xp + " total)"), false);
            player.sendSystemMessage(Component.literal(""), false);
            
            player.sendSystemMessage(Component.literal("§aBuffs:"), false);
            for (ClassEffect effect : playerClass.buffs) {
                player.sendSystemMessage(Component.literal("  §a+ " + effect.name), false);
            }
            
            player.sendSystemMessage(Component.literal("§cDebuffs:"), false);
            for (ClassEffect effect : playerClass.debuffs) {
                player.sendSystemMessage(Component.literal("  §c- " + effect.name), false);
            }
            
            player.sendSystemMessage(Component.literal(""), false);
            player.sendSystemMessage(Component.literal("§7Melee: §f" + String.format("%.0f%%", playerClass.meleeMultiplier * 100)), false);
            player.sendSystemMessage(Component.literal("§7Defense: §f" + String.format("%.0f%%", playerClass.defenseMultiplier * 100)), false);
        }
    }

    public static void displayClassList(ServerPlayer player) {
        player.sendSystemMessage(Component.literal("═══════════════════════════════════")
            .withStyle(ChatFormatting.GOLD), false);
        player.sendSystemMessage(Component.literal("         ⚔ AVAILABLE CLASSES ⚔")
            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        player.sendSystemMessage(Component.literal("═══════════════════════════════════")
            .withStyle(ChatFormatting.GOLD), false);
        
        for (PlayerClass playerClass : PlayerClass.values()) {
            player.sendSystemMessage(Component.literal(playerClass.icon + " " + playerClass.name)
                .withStyle(playerClass.color), false);
            player.sendSystemMessage(Component.literal("  §7" + playerClass.description), false);
        }
    }
}
