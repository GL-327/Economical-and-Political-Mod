package com.political.magic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.political.Commission;



import com.political.stats.PlayerStatsManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Unused on purpose. Magic gameplay is left as a skeleton and is not registered.
 */
public class MagicManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(MagicManager.class);


    // ════════════════════════════════════════════════════════════
    // SPELLS
    // ════════════════════════════════════════════════════════════

    public enum Spell {
        FIREBALL("Fireball", "Launches a fire charge projectile", 20, 8f, 40, Items.FIRE_CHARGE, ChatFormatting.RED, 1),
        ICE_SPIKE("Ice Spike", "Freezes target for 3 seconds", 25, 4f, 50, Items.BLUE_ICE, ChatFormatting.AQUA, 1),
        LIGHTNING_STRIKE("Lightning Strike", "Calls lightning on target", 40, 12f, 60, Items.LIGHTNING_ROD.weathering().unaffected(), ChatFormatting.YELLOW, 1),
        HEALING_WAVE("Healing Wave", "Heals 4 hearts in small AOE", 30, 0f, 45, Items.GOLDEN_APPLE, ChatFormatting.GREEN, 1),
        SHADOW_STEP("Shadow Step", "Teleport 8 blocks forward", 15, 0f, 30, Items.ENDER_PEARL, ChatFormatting.DARK_GRAY, 1),
        ARCANE_SHIELD("Arcane Shield", "Absorption II for 10 seconds", 35, 0f, 50, Items.SHIELD, ChatFormatting.BLUE, 1),
        POISON_CLOUD("Poison Cloud", "AOE poison in 3-block radius", 25, 0f, 45, Items.SPIDER_EYE, ChatFormatting.DARK_GREEN, 1),
        WIND_GUST("Wind Gust", "Push entities in 5-block radius", 20, 0f, 35, Items.FEATHER, ChatFormatting.WHITE, 1),
        SOUL_DRAIN("Soul Drain", "5 damage, heal caster 3 hearts", 30, 5f, 50, Items.WITHER_ROSE, ChatFormatting.DARK_PURPLE, 1),
        METEOR("Meteor", "Massive fireball from sky, big AOE", 50, 15f, 80, Items.MAGMA_BLOCK, ChatFormatting.GOLD, 40),
        ARCANE_BURST("Arcane Burst", "Short-range magic burst", 22, 7f, 32, Items.AMETHYST_SHARD, ChatFormatting.LIGHT_PURPLE, 2),
        FLAME_WAVE("Flame Wave", "Cone of fire damage ahead", 28, 9f, 42, Items.BLAZE_POWDER, ChatFormatting.RED, 3),
        FROST_NOVA("Frost Nova", "Slows and damages nearby foes", 26, 5f, 40, Items.PACKED_ICE, ChatFormatting.AQUA, 4),
        CHAIN_SPARK("Chain Spark", "Lightning jumps to a second nearby target", 32, 10f, 48, Items.COPPER_INGOT, ChatFormatting.YELLOW, 5),
        VOID_GRASP("Void Grasp", "Pulls the nearest enemy toward you", 24, 3f, 38, Items.OBSIDIAN, ChatFormatting.DARK_PURPLE, 6),
        SOLAR_FLARE("Solar Flare", "Small explosive burst at target", 30, 11f, 45, Items.SUNFLOWER, ChatFormatting.GOLD, 8),
        NETHER_BOLT("Nether Bolt", "Dark fire damage + brief wither", 27, 8f, 41, Items.NETHER_BRICK, ChatFormatting.DARK_RED, 7),
        SOUL_SPARK("Soul Spark", "Drains life from target, minor self-heal", 29, 6f, 44, Items.SOUL_LANTERN, ChatFormatting.DARK_AQUA, 9),
        STORM_BOLT("Storm Bolt", "Calls a bolt on your crosshair", 36, 13f, 52, Items.PRISMARINE_SHARD, ChatFormatting.BLUE, 12),
        GRAVITY_WELL("Gravity Well", "Slows entities in a wide radius", 33, 4f, 50, Items.IRON_BLOCK, ChatFormatting.GRAY, 11),
        LIFE_TAP("Life Tap", "Spend health to restore mana", 10, 0f, 60, Items.GLASS_BOTTLE, ChatFormatting.RED, 10),
        REGEN_AURA("Regeneration Aura", "Regen for you and nearby allies", 38, 0f, 55, Items.GLOW_BERRIES, ChatFormatting.GREEN, 14),
        ICE_LANCE("Ice Lance", "High single-target frost damage", 31, 12f, 46, Items.SNOWBALL, ChatFormatting.AQUA, 13),
        SHADOW_BOLT("Shadow Bolt", "Dark magic bolt, ignores some armor", 34, 9f, 48, Items.COAL, ChatFormatting.DARK_GRAY, 15),
        HOLY_BOLT("Holy Bolt", "Smite undead for bonus damage", 28, 10f, 43, Items.GOLD_NUGGET, ChatFormatting.YELLOW, 16),
        ROCK_SPIKE("Rock Spike", "Earth spike at target location", 35, 11f, 51, Items.STONE, ChatFormatting.DARK_GREEN, 18),
        TIDE_PUSH("Tide Push", "Water knockback wave", 25, 4f, 39, Items.WATER_BUCKET, ChatFormatting.BLUE, 6),
        WIND_SHEAR("Wind Shear", "Stronger knockback than Wind Gust", 23, 3f, 37, Items.PHANTOM_MEMBRANE, ChatFormatting.WHITE, 5),
        ARCANE_NOVA("Arcane Nova", "Large magic explosion", 45, 14f, 70, Items.NETHER_STAR, ChatFormatting.LIGHT_PURPLE, 22),
        BLOOD_NOVA("Blood Nova", "AOE damage, costs some of your health", 40, 12f, 58, Items.REDSTONE, ChatFormatting.DARK_RED, 20),
        PHANTOM_SHIFT("Phantom Shift", "Short invuln frames + speed boost", 20, 0f, 45, Items.SPECTRAL_ARROW, ChatFormatting.GOLD, 17),
        STAR_SHARD("Star Shard", "Falling star on target (small meteor)", 42, 13f, 65, Items.END_CRYSTAL, ChatFormatting.LIGHT_PURPLE, 25),
        MANA_SURGE("Mana Surge", "Instant mana restore, long cooldown", 5, 0f, 120, Items.LAPIS_LAZULI, ChatFormatting.BLUE, 30);

        public final String displayName;
        public final String description;
        public final int manaCost;
        public final float damage;
        public final int cooldownTicks;
        public final Item icon;
        public final ChatFormatting color;
        public final int levelRequired;

        Spell(String name, String desc, int mana, float dmg, int cd, Item icon, ChatFormatting color, int lvl) {
            this.displayName = name;
            this.description = desc;
            this.manaCost = mana;
            this.damage = dmg;
            this.cooldownTicks = cd;
            this.icon = icon;
            this.color = color;
            this.levelRequired = lvl;
        }

        public static Spell byName(String name) {
            if (name == null) return null;
            try { return valueOf(name); } catch (IllegalArgumentException e) { return null; }
        }
    }

    // ════════════════════════════════════════════════════════════
    // MAGIC CREATURES
    // ════════════════════════════════════════════════════════════

    public enum MagicCreature {
        ARCANE_WISP("Arcane Wisp", 30, 100, "COMMON", ChatFormatting.LIGHT_PURPLE),
        VOID_WALKER("Void Walker", 80, 250, "UNCOMMON", ChatFormatting.DARK_PURPLE),
        CRYSTAL_GOLEM("Crystal Golem", 200, 400, "RARE", ChatFormatting.AQUA),
        SHADOW_MAGE("Shadow Mage", 100, 350, "RARE", ChatFormatting.DARK_RED),
        RIFT_GUARDIAN("Rift Guardian", 600, 500, "LEGENDARY", ChatFormatting.GOLD),
        STORM_PHANTOM("Storm Phantom", 140, 380, "RARE", ChatFormatting.BLUE),
        ASHEN_HULK("Ashen Hulk", 280, 520, "EPIC", ChatFormatting.DARK_RED),
        CELESTIAL_SERAPH("Celestial Seraph", 360, 620, "EPIC", ChatFormatting.YELLOW),
        ABYSS_DEVOURER("Abyss Devourer", 800, 900, "MYTHIC", ChatFormatting.DARK_PURPLE);

        public final String displayName;
        public final int health;
        public final int xpReward;
        public final String rarity;
        public final ChatFormatting color;

        MagicCreature(String name, int hp, int xp, String rarity, ChatFormatting color) {
            this.displayName = name;
            this.health = hp;
            this.xpReward = xp;
            this.rarity = rarity;
            this.color = color;
        }
    }

    // ════════════════════════════════════════════════════════════
    // PLAYER DATA
    // ════════════════════════════════════════════════════════════

    public static class PlayerMagicData {
        public int level = 1;
        public int xp = 0;
        public float mana = 100f;
        public Set<String> unlockedSpells = new HashSet<>();
        public String activeSpell = null;
        public Set<String> skillTreeUnlocks = new HashSet<>();
        public String activeCommission = null;
        public int commissionProgress = 0;
        public long lastCommissionTime = 0;

        public PlayerMagicData() {}
    }

    // ════════════════════════════════════════════════════════════
    // STORAGE
    // ════════════════════════════════════════════════════════════

    private static final Map<String, PlayerMagicData> playerData = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, Long>> spellCooldowns = new ConcurrentHashMap<>();
    private static Path dataFile;
    public static final int MAX_LEVEL = 50;

    // ════════════════════════════════════════════════════════════
    // XP CURVE
    // ════════════════════════════════════════════════════════════

    public static int xpForLevel(int level) {
        if (level <= 1) return 0;
        return (int) (100 * Math.pow(level, 1.5));
    }

    public static int cumulativeXpForLevel(int level) {
        int total = 0;
        for (int i = 2; i <= level; i++) total += xpForLevel(i);
        return total;
    }

    public static double getProgressPercent(String uuid) {
        PlayerMagicData data = getPlayerData(uuid);
        if (data.level >= MAX_LEVEL) return 100.0;
        int currentLevelCum = cumulativeXpForLevel(data.level);
        int nextLevelCum = cumulativeXpForLevel(data.level + 1);
        int needed = nextLevelCum - currentLevelCum;
        if (needed <= 0) return 100.0;
        int progress = data.xp - currentLevelCum;
        return Math.min(100.0, (progress * 100.0) / needed);
    }

    // ════════════════════════════════════════════════════════════
    // INITIALIZATION & PERSISTENCE
    // ════════════════════════════════════════════════════════════

    public static void initialize(Path dataDir) {
        dataFile = dataDir.resolve("magic_data.json");
        loadData();
    }

    private static void loadData() {
        if (dataFile == null || !Files.exists(dataFile)) return;
        try {
            String content = Files.readString(dataFile);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Type type = new TypeToken<Map<String, PlayerMagicData>>() {}.getType();
            Map<String, PlayerMagicData> loaded = gson.fromJson(content, type);
            if (loaded != null) {
                playerData.clear();
                playerData.putAll(loaded);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load data", e);
        }
    }

    public static void saveData() {
        if (dataFile == null) return;
        try {
            Files.createDirectories(dataFile.getParent());
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (Writer writer = Files.newBufferedWriter(dataFile)) {
                gson.toJson(playerData, writer);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save data", e);
        }
    }

    // ════════════════════════════════════════════════════════════
    // DATA ACCESS
    // ════════════════════════════════════════════════════════════

    public static PlayerMagicData getPlayerData(String uuid) {
        return playerData.computeIfAbsent(uuid, k -> new PlayerMagicData());
    }

    public static void registerPlayer(String uuid) {
        playerData.computeIfAbsent(uuid, k -> new PlayerMagicData());
    }

    public static int getLevel(String uuid) { return getPlayerData(uuid).level; }
    public static int getXp(String uuid) { return getPlayerData(uuid).xp; }

    // ════════════════════════════════════════════════════════════
    // XP & LEVELING
    // ════════════════════════════════════════════════════════════

    public static void addXp(ServerPlayer player, int amount) {
        String uuid = player.getUUID().toString();
        PlayerMagicData data = getPlayerData(uuid);
        int bonusPct = MagicSkillTree.getXpBonus(data.skillTreeUnlocks);
        amount = amount + (amount * bonusPct / 100);
        data.xp += amount;

        boolean leveledUp = false;
        while (data.level < MAX_LEVEL) {
            int needed = cumulativeXpForLevel(data.level + 1);
            if (data.xp >= needed) {
                data.level++;
                leveledUp = true;
            } else {
                break;
            }
        }

        if (leveledUp) {
            player.sendSystemMessage(Component.literal("✦ Magic Level Up! ")
                    .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)
                    .append(Component.literal("You are now level " + data.level + "!")
                            .withStyle(ChatFormatting.WHITE)));
        }
        player.sendSystemMessage(Component.literal("  +" + amount + " Magic XP")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    // ════════════════════════════════════════════════════════════
    // MANA
    // ════════════════════════════════════════════════════════════

    public static int getMaxMana(String uuid) {
        PlayerMagicData data = getPlayerData(uuid);
        int base = 100 + (data.level * 5);
        int bonus = MagicSkillTree.getManaBonus(data.skillTreeUnlocks);
        // Intelligence stat above its 100 base adds max mana 1:1 (gear lore "✎ Intelligence: +X").
        return base + bonus + PlayerStatsManager.getBonusMana(uuid);
    }

    public static float getMana(String uuid) {
        return getPlayerData(uuid).mana;
    }

    public static void setMana(String uuid, float mana) {
        getPlayerData(uuid).mana = Math.max(0, Math.min(mana, getMaxMana(uuid)));
    }

    public static boolean consumeMana(ServerPlayer player, int amount) {
        String uuid = player.getUUID().toString();
        PlayerMagicData data = getPlayerData(uuid);
        int manaCostReduction = MagicSkillTree.getManaCostReduction(data.skillTreeUnlocks);
        int adjusted = Math.max(1, amount - (amount * manaCostReduction / 100));
        int staffReduction = MagicStaff.getManaCostReduction(player.getMainHandItem());
        adjusted = Math.max(1, adjusted - (adjusted * staffReduction / 100));
        if (data.mana >= adjusted) {
            data.mana -= adjusted;
            return true;
        }
        player.sendSystemMessage(Component.literal("✦ Not enough mana! ("
                + (int) data.mana + "/" + adjusted + ")").withStyle(ChatFormatting.RED));
        return false;
    }

    public static void regenMana(String uuid) {
        PlayerMagicData data = getPlayerData(uuid);
        float rate = 2.0f;
        float bonus = MagicSkillTree.getManaRegenBonus(data.skillTreeUnlocks);
        rate += rate * bonus / 100f;
        data.mana = Math.min(data.mana + rate, getMaxMana(uuid));
    }

    // ════════════════════════════════════════════════════════════
    // SPELLS
    // ════════════════════════════════════════════════════════════

    /**
     * Sets {@link PlayerMagicData#unlockedSpells} from skill-tree nodes only.
     * Spells are not granted by magic level alone.
     */
    private static void reapplySpellUnlocksFromSkillTree(PlayerMagicData data) {
        Set<String> spells = new HashSet<>();
        for (String nodeId : data.skillTreeUnlocks) {
            MagicSkillTree.SkillNode node = MagicSkillTree.getNode(nodeId);
            if (node != null && node.unlocksSpell() != null) {
                spells.add(node.unlocksSpell());
            }
        }
        data.unlockedSpells = spells;
        if (data.activeSpell != null && !data.unlockedSpells.contains(data.activeSpell)) {
            data.activeSpell = null;
        }
    }

    /** Call on join so saved data matches skill-tree unlocks (fixes legacy level-based spell grants). */
    public static void syncSpellUnlocksFromSkillTree(ServerPlayer player) {
        PlayerMagicData data = getPlayerData(player.getUUID().toString());
        reapplySpellUnlocksFromSkillTree(data);
        saveData();
    }

    public static boolean hasSpell(String uuid, Spell spell) {
        return getPlayerData(uuid).unlockedSpells.contains(spell.name());
    }

    public static Set<String> getUnlockedSpells(String uuid) {
        return Collections.unmodifiableSet(getPlayerData(uuid).unlockedSpells);
    }

    public static void setActiveSpell(String uuid, Spell spell) {
        getPlayerData(uuid).activeSpell = spell != null ? spell.name() : null;
    }

    public static Spell getActiveSpell(String uuid) {
        return Spell.byName(getPlayerData(uuid).activeSpell);
    }

    // ════════════════════════════════════════════════════════════
    // COOLDOWNS
    // ════════════════════════════════════════════════════════════

    public static boolean isOnCooldown(String uuid, Spell spell) {
        Map<String, Long> cd = spellCooldowns.get(uuid);
        if (cd == null) return false;
        Long endTime = cd.get(spell.name());
        return endTime != null && System.currentTimeMillis() < endTime;
    }

    public static long getCooldownRemaining(String uuid, Spell spell) {
        Map<String, Long> cd = spellCooldowns.get(uuid);
        if (cd == null) return 0;
        Long endTime = cd.get(spell.name());
        if (endTime == null) return 0;
        return Math.max(0, endTime - System.currentTimeMillis());
    }

    public static void setCooldown(ServerPlayer player, Spell spell) {
        String uuid = player.getUUID().toString();
        int cooldownMs = spell.cooldownTicks * 50;
        PlayerMagicData data = getPlayerData(uuid);
        int cdReduction = MagicSkillTree.getCooldownReduction(data.skillTreeUnlocks);
        cooldownMs = cooldownMs - (cooldownMs * cdReduction / 100);
        int staffCd = MagicStaff.getCooldownReductionPercent(player.getMainHandItem());
        cooldownMs = cooldownMs - (cooldownMs * staffCd / 100);
        spellCooldowns.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
                .put(spell.name(), System.currentTimeMillis() + Math.max(50, cooldownMs));
    }

    // ════════════════════════════════════════════════════════════
    // SKILL TREE
    // ════════════════════════════════════════════════════════════

    public static Set<String> getSkillTreeUnlocks(String uuid) {
        return getPlayerData(uuid).skillTreeUnlocks;
    }

    public static boolean unlockSkillNode(ServerPlayer player, String nodeId) {
        String uuid = player.getUUID().toString();
        PlayerMagicData data = getPlayerData(uuid);
        MagicSkillTree.SkillNode node = MagicSkillTree.getNode(nodeId);
        if (node == null) return false;
        if (data.skillTreeUnlocks.contains(nodeId)) return false;
        if (data.level < node.levelRequired()) return false;
        if (data.xp < node.xpCost()) return false;
        if (node.parentId() != null && !data.skillTreeUnlocks.contains(node.parentId())) return false;

        data.xp -= node.xpCost();
        data.skillTreeUnlocks.add(nodeId);
        reapplySpellUnlocksFromSkillTree(data);

        saveData();
        return true;
    }

    // ════════════════════════════════════════════════════════════
    // MAGIC COMMISSIONS
    // ════════════════════════════════════════════════════════════

    public enum MagicCommission {
        KILL_ARCANE_WISPS("Kill Arcane Wisps", 5, 200, "Kill 5 Arcane Wisps"),
        KILL_VOID_WALKERS("Kill Void Walkers", 3, 400, "Kill 3 Void Walkers"),
        KILL_CRYSTAL_GOLEMS("Kill Crystal Golems", 2, 600, "Kill 2 Crystal Golems"),
        KILL_SHADOW_MAGES("Kill Shadow Mages", 2, 500, "Kill 2 Shadow Mages"),
        KILL_RIFT_GUARDIANS("Kill Rift Guardian", 1, 800, "Kill 1 Rift Guardian"),
        CAST_SPELLS("Cast 10 Spells", 10, 150, "Cast any 10 spells"),
        CAST_SPELLS_25("Cast 25 Spells", 25, 280, "Cast any 25 spells"),
        CAST_SPELLS_50("Cast 50 Spells", 50, 450, "Cast any 50 spells"),
        CAST_METEOR("Cast Meteor", 1, 300, "Cast the Meteor spell"),
        CAST_METEOR_3("Cast Meteor x3", 3, 420, "Cast Meteor 3 times"),
        USE_MANA("Spend 200 Mana", 200, 250, "Spend 200 total mana on spells"),
        USE_MANA_500("Spend 500 Mana", 500, 380, "Spend 500 total mana on spells"),
        USE_MANA_1000("Spend 1000 Mana", 1000, 520, "Spend 1000 total mana on spells"),
        KILL_HOSTILES_15("Slay 15 Hostiles", 15, 180, "Kill 15 hostile mobs"),
        KILL_HOSTILES_40("Slay 40 Hostiles", 40, 320, "Kill 40 hostile mobs"),
        KILL_HOSTILES_100("Slay 100 Hostiles", 100, 550, "Kill 100 hostile mobs"),
        MINE_BLOCKS_60("Mine 60 Blocks", 60, 200, "Break 60 blocks"),
        MINE_BLOCKS_180("Mine 180 Blocks", 180, 340, "Break 180 blocks"),
        MINE_BLOCKS_400("Mine 400 Blocks", 400, 480, "Break 400 blocks"),
        FISH_CATCHES_8("Catch 8 Fish", 8, 190, "Catch 8 fish"),
        FISH_CATCHES_20("Catch 20 Fish", 20, 310, "Catch 20 fish"),
        CAST_FIREBALL_8("Cast Fireball 8x", 8, 220, "Cast Fireball 8 times"),
        CAST_ICEBOLT_6("Cast Ice Spike 6x", 6, 210, "Cast Ice Spike 6 times"),
        ARCANE_SPRINT("Arcane Sprint", 20, 260, "Cast any 20 spells (commission rotation)"),
        VOID_HUNTER("Void Hunter", 4, 360, "Kill 4 Void Walkers"),
        CRYSTAL_BREAKER("Crystal Breaker", 3, 400, "Kill 3 Crystal Golems"),
        WISP_CULLER("Wisp Culler", 12, 240, "Kill 12 Arcane Wisps"),
        SHADOW_WAR("Shadow War", 4, 380, "Kill 4 Shadow Mages"),
        RIFT_TRIAL("Rift Trial", 2, 900, "Kill 2 Rift Guardians"),
        MANA_ECONOMY("Mana Economy", 750, 440, "Spend 750 mana on spells"),
        HUNT_STORM_PHANTOMS("Hunt Storm Phantoms", 4, 480, "Kill 4 Storm Phantoms"),
        HUNT_ASHEN_HULKS("Hunt Ashen Hulks", 3, 560, "Kill 3 Ashen Hulks"),
        HUNT_CELESTIAL_SERAPHS("Hunt Celestial Seraphs", 2, 650, "Kill 2 Celestial Seraphs"),
        HUNT_ABYSS_DEVOURER("Hunt Abyss Devourer", 1, 1100, "Kill 1 Abyss Devourer");

        public final String displayName;
        public final int target;
        public final int xpReward;
        public final String description;

        MagicCommission(String name, int target, int xp, String desc) {
            this.displayName = name; this.target = target; this.xpReward = xp; this.description = desc;
        }
    }

    /** New set of 3 offered commissions every 30 minutes (deterministic per time window). */
    public static final long COMMISSION_ROTATION_MS = 30L * 60L * 1000L;

    public static List<MagicCommission> getCurrentCommissionOffers() {
        MagicCommission[] all = MagicCommission.values();
        long epoch = System.currentTimeMillis() / COMMISSION_ROTATION_MS;
        Random rng = new Random(epoch * 0x9E3779B97F4A7C15L ^ 0xC0FFEEBADL);
        List<MagicCommission> list = new ArrayList<>(Arrays.asList(all));
        Collections.shuffle(list, rng);
        int n = Math.min(3, list.size());
        return new ArrayList<>(list.subList(0, n));
    }

    public static void tryProgressHostileKill(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        MagicCommission a = getActiveCommission(uuid);
        if (a == null) return;
        if (a.name().startsWith("KILL_HOSTILES") || a.name().startsWith("KILL_ARCANE") || a.name().startsWith("KILL_VOID")
                || a.name().startsWith("KILL_CRYSTAL") || a.name().startsWith("KILL_SHADOW") || a.name().startsWith("KILL_RIFT")
                || a.name().startsWith("HUNT_STORM") || a.name().startsWith("HUNT_ASHEN")
                || a.name().startsWith("HUNT_CELESTIAL") || a.name().startsWith("HUNT_ABYSS")) {
            progressCommission(player, a.name(), 1);
        }
    }

    public static void tryProgressMineBlock(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        MagicCommission a = getActiveCommission(uuid);
        if (a != null && a.name().startsWith("MINE_BLOCKS")) {
            progressCommission(player, a.name(), 1);
        }
    }

    public static void tryProgressFish(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        MagicCommission a = getActiveCommission(uuid);
        if (a != null && a.name().startsWith("FISH_CATCHES")) {
            progressCommission(player, a.name(), 1);
        }
    }

    public static void tryProgressManaSpent(ServerPlayer player, int manaAmount) {
        String uuid = player.getUUID().toString();
        MagicCommission a = getActiveCommission(uuid);
        if (a != null && (a.name().startsWith("USE_MANA") || a.name().equals("MANA_ECONOMY"))) {
            progressCommission(player, a.name(), manaAmount);
        }
    }

    public static void tryProgressSpellCast(ServerPlayer player, Spell spell) {
        String uuid = player.getUUID().toString();
        MagicCommission a = getActiveCommission(uuid);
        if (a == null || spell == null) return;
        if (a.name().startsWith("CAST_SPELLS") || a.name().equals("ARCANE_SPRINT")) {
            progressCommission(player, a.name(), 1);
        }
        if (a.name().startsWith("CAST_METEOR") && spell == Spell.METEOR) {
            progressCommission(player, a.name(), 1);
        }
        if (a.name().startsWith("CAST_FIREBALL") && spell == Spell.FIREBALL) {
            progressCommission(player, a.name(), 1);
        }
        if (a.name().startsWith("CAST_ICEBOLT") && spell == Spell.ICE_SPIKE) {
            progressCommission(player, a.name(), 1);
        }
    }

    public static MagicCommission getActiveCommission(String uuid) {
        PlayerMagicData data = getPlayerData(uuid);
        if (data.activeCommission == null) return null;
        try { return MagicCommission.valueOf(data.activeCommission); } catch (Exception e) { LOGGER.debug("MagicManager.getActiveCommission: ignored an error", e); return null; }
    }

    public static int getCommissionProgress(String uuid) {
        return getPlayerData(uuid).commissionProgress;
    }

    public static void startCommission(ServerPlayer player, MagicCommission commission) {
        String uuid = player.getUUID().toString();
        PlayerMagicData data = getPlayerData(uuid);
        data.activeCommission = commission.name();
        data.commissionProgress = 0;
        player.sendSystemMessage(Component.literal("✦ Magic Commission: " + commission.displayName).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal("  " + commission.description).withStyle(ChatFormatting.GRAY));
    }

    public static void progressCommission(ServerPlayer player, String commissionType, int amount) {
        String uuid = player.getUUID().toString();
        PlayerMagicData data = getPlayerData(uuid);
        MagicCommission active = getActiveCommission(uuid);
        if (active == null) return;
        if (!active.name().equals(commissionType) && !commissionType.equals("ANY")) return;

        data.commissionProgress += amount;
        if (data.commissionProgress >= active.target) {
            addXp(player, active.xpReward);
            player.sendSystemMessage(Component.literal("✦ Commission Complete! +" + active.xpReward + " Magic XP").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            data.activeCommission = null;
            data.commissionProgress = 0;
            data.lastCommissionTime = System.currentTimeMillis();
        }
    }

    public static boolean canStartCommission(String uuid) {
        PlayerMagicData data = getPlayerData(uuid);
        return data.activeCommission == null;
    }

    // ════════════════════════════════════════════════════════════
    // MAGIC CREATURE IDENTIFICATION
    // ════════════════════════════════════════════════════════════

    public static MagicCreature getCreatureType(String customName) {
        if (customName == null) return null;
        for (MagicCreature mc : MagicCreature.values()) {
            if (customName.contains(mc.displayName)) return mc;
        }
        return null;
    }
}
