package com.political;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FishingManager {

    // ════════════════════════════════════════════════════════════
    // SEA CREATURE DEFINITIONS
    // ════════════════════════════════════════════════════════════

    public enum CreatureTier {
        COMMON("Common", ChatFormatting.WHITE, 50),
        UNCOMMON("Uncommon", ChatFormatting.GREEN, 150),
        RARE("Rare", ChatFormatting.BLUE, 400),
        EPIC("Epic", ChatFormatting.DARK_PURPLE, 1000),
        LEGENDARY("Legendary", ChatFormatting.GOLD, 2500);

        public final String label;
        public final ChatFormatting color;
        public final int xpReward;

        CreatureTier(String label, ChatFormatting color, int xpReward) {
            this.label = label;
            this.color = color;
            this.xpReward = xpReward;
        }
    }

    public enum SeaCreature {
        TIDAL_LURKER("Tidal Lurker", CreatureTier.COMMON, EntityType.DROWNED, 40, Items.COD),
        REEF_STALKER("Reef Stalker", CreatureTier.UNCOMMON, EntityType.GUARDIAN, 60, Items.PRISMARINE_SHARD),
        ABYSSAL_ANGLER("Abyssal Angler", CreatureTier.RARE, EntityType.ELDER_GUARDIAN, 150, Items.PRISMARINE_CRYSTALS),
        STORM_KRAKEN("Storm Kraken", CreatureTier.EPIC, EntityType.GUARDIAN, 500, Items.HEART_OF_THE_SEA),
        LEVIATHAN("Leviathan", CreatureTier.LEGENDARY, EntityType.WARDEN, 1000, Items.NETHER_STAR);

        public final String displayName;
        public final CreatureTier tier;
        public final EntityType<?> baseType;
        public final double health;
        public final net.minecraft.world.item.Item icon;

        SeaCreature(String displayName, CreatureTier tier, EntityType<?> baseType, double health, net.minecraft.world.item.Item icon) {
            this.displayName = displayName;
            this.tier = tier;
            this.baseType = baseType;
            this.health = health;
            this.icon = icon;
        }
    }

    // ════════════════════════════════════════════════════════════
    // PLAYER DATA
    // ════════════════════════════════════════════════════════════

    public static class PlayerFishingData {
        public int level = 1;
        public int xp = 0;
        public Set<String> unlockedNodes = new HashSet<>();
        public String equippedRod = FishingRods.RodTier.STARTER.id;
        public int fishCaught = 0;
        public int creaturesKilled = 0;
        public String rarestCatch = "None";
        public Map<String, Integer> bestiaryKills = new HashMap<>();
        public Set<String> discoveredCreatures = new HashSet<>();

        public JsonObject toJson() {
            JsonObject obj = new JsonObject();
            obj.addProperty("level", level);
            obj.addProperty("xp", xp);
            JsonArray nodes = new JsonArray();
            for (String n : unlockedNodes) nodes.add(n);
            obj.add("unlockedNodes", nodes);
            obj.addProperty("equippedRod", equippedRod);
            obj.addProperty("fishCaught", fishCaught);
            obj.addProperty("creaturesKilled", creaturesKilled);
            obj.addProperty("rarestCatch", rarestCatch);
            JsonObject bestiary = new JsonObject();
            for (var e : bestiaryKills.entrySet()) bestiary.addProperty(e.getKey(), e.getValue());
            obj.add("bestiaryKills", bestiary);
            JsonArray disc = new JsonArray();
            for (String d : discoveredCreatures) disc.add(d);
            obj.add("discoveredCreatures", disc);
            return obj;
        }

        public static PlayerFishingData fromJson(JsonObject obj) {
            PlayerFishingData d = new PlayerFishingData();
            if (obj.has("level")) d.level = obj.get("level").getAsInt();
            if (obj.has("xp")) d.xp = obj.get("xp").getAsInt();
            if (obj.has("unlockedNodes")) {
                for (JsonElement e : obj.getAsJsonArray("unlockedNodes")) d.unlockedNodes.add(e.getAsString());
            }
            if (obj.has("equippedRod")) d.equippedRod = obj.get("equippedRod").getAsString();
            if (obj.has("fishCaught")) d.fishCaught = obj.get("fishCaught").getAsInt();
            if (obj.has("creaturesKilled")) d.creaturesKilled = obj.get("creaturesKilled").getAsInt();
            if (obj.has("rarestCatch")) d.rarestCatch = obj.get("rarestCatch").getAsString();
            if (obj.has("bestiaryKills")) {
                for (var e : obj.getAsJsonObject("bestiaryKills").entrySet()) {
                    d.bestiaryKills.put(e.getKey(), e.getValue().getAsInt());
                }
            }
            if (obj.has("discoveredCreatures")) {
                for (JsonElement e : obj.getAsJsonArray("discoveredCreatures")) d.discoveredCreatures.add(e.getAsString());
            }
            return d;
        }
    }

    // ════════════════════════════════════════════════════════════
    // LEVEL XP TABLE
    // ════════════════════════════════════════════════════════════

    public static final int MAX_LEVEL = 50;
    private static final int[] XP_PER_LEVEL = new int[MAX_LEVEL + 1];

    static {
        XP_PER_LEVEL[0] = 0;
        for (int i = 1; i <= MAX_LEVEL; i++) {
            XP_PER_LEVEL[i] = 50 + (i * i * 10);
        }
    }

    public static int xpForLevel(int level) {
        if (level < 1 || level > MAX_LEVEL) return Integer.MAX_VALUE;
        return XP_PER_LEVEL[level];
    }

    public static int totalXpForLevel(int level) {
        int total = 0;
        for (int i = 1; i <= Math.min(level, MAX_LEVEL); i++) total += XP_PER_LEVEL[i];
        return total;
    }

    // ════════════════════════════════════════════════════════════
    // STORAGE
    // ════════════════════════════════════════════════════════════

    private static final Map<String, PlayerFishingData> playerData = new ConcurrentHashMap<>();
    private static Path dataPath;
    private static final String DATA_FILE = "fishing_data.json";
    private static final Set<UUID> activeSeaCreatures = ConcurrentHashMap.newKeySet();

    public static void initialize(Path savePath) {
        dataPath = savePath;
        loadData();
    }

    public static PlayerFishingData getData(String uuid) {
        return playerData.computeIfAbsent(uuid, k -> new PlayerFishingData());
    }

    // ════════════════════════════════════════════════════════════
    // XP AND LEVELING
    // ════════════════════════════════════════════════════════════

    public static void addXp(ServerPlayer player, int amount) {
        String uuid = player.getUUID().toString();
        PlayerFishingData data = getData(uuid);
        data.xp += amount;

        boolean leveled = false;
        while (data.level < MAX_LEVEL && data.xp >= xpForLevel(data.level + 1)) {
            data.xp -= xpForLevel(data.level + 1);
            data.level++;
            leveled = true;
        }

        if (leveled) {
            player.sendSystemMessage(Component.literal("").withStyle(ChatFormatting.AQUA)
                    .append(Component.literal("FISHING LEVEL UP! ").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                    .append(Component.literal("Level " + data.level).withStyle(ChatFormatting.GREEN)));

            FishingRods.RodTier newRod = null;
            for (FishingRods.RodTier t : FishingRods.RodTier.values()) {
                if (t.levelRequired == data.level) {
                    newRod = t;
                    break;
                }
            }
            if (newRod != null) {
                player.sendSystemMessage(Component.literal("  New rod unlocked: ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(newRod.displayName).withStyle(newRod.color)));
            }
        }
    }

    // ════════════════════════════════════════════════════════════
    // SKILL TREE
    // ════════════════════════════════════════════════════════════

    public static boolean unlockNode(ServerPlayer player, String nodeId) {
        String uuid = player.getUUID().toString();
        PlayerFishingData data = getData(uuid);
        FishingSkillTree.Node node = FishingSkillTree.getNode(nodeId);
        if (node == null) return false;

        if (!FishingSkillTree.canUnlock(nodeId, data.unlockedNodes)) return false;

        int availableXp = data.xp + totalXpForLevel(data.level);
        int spentXp = 0;
        for (String n : data.unlockedNodes) {
            FishingSkillTree.Node un = FishingSkillTree.getNode(n);
            if (un != null) spentXp += un.xpCost;
        }

        if (availableXp - spentXp < node.xpCost) return false;

        data.unlockedNodes.add(nodeId);
        player.sendSystemMessage(Component.literal("Unlocked: ").withStyle(ChatFormatting.GREEN)
                .append(Component.literal(node.name).withStyle(node.category.color, ChatFormatting.BOLD)));
        return true;
    }

    public static int getAvailableSkillXp(String uuid) {
        PlayerFishingData data = getData(uuid);
        int total = data.xp + totalXpForLevel(data.level);
        int spent = 0;
        for (String n : data.unlockedNodes) {
            FishingSkillTree.Node node = FishingSkillTree.getNode(n);
            if (node != null) spent += node.xpCost;
        }
        return total - spent;
    }

    public static boolean hasNode(String uuid, String nodeId) {
        return getData(uuid).unlockedNodes.contains(nodeId);
    }

    // ════════════════════════════════════════════════════════════
    // SEA CREATURE SPAWNING
    // ════════════════════════════════════════════════════════════

    public static double getSeaCreatureChance(String uuid) {
        PlayerFishingData data = getData(uuid);
        double base = 0.05 + (data.level / 5) * 0.01;

        FishingRods.RodTier rod = FishingRods.RodTier.fromId(data.equippedRod);
        if (rod != null) base += rod.seaCreatureBonus / 100.0;

        if (hasNode(uuid, "sh_bait_sense")) base += 0.03;
        if (hasNode(uuid, "sh_tide_caller")) base += 0.06;

        return Math.min(0.50, base);
    }

    public static SeaCreature rollSeaCreature(String uuid, Random rng) {
        double r = rng.nextDouble();
        boolean hasKrakenHunter = hasNode(uuid, "sh_kraken_hunter");

        double legendaryChance = hasKrakenHunter ? 0.015 : 0.005;
        double epicChance = hasKrakenHunter ? 0.05 : 0.025;
        double rareChance = 0.09;
        double uncommonChance = 0.35;

        if (r < legendaryChance) return SeaCreature.LEVIATHAN;
        if (r < legendaryChance + epicChance) return SeaCreature.STORM_KRAKEN;
        if (r < legendaryChance + epicChance + rareChance) return SeaCreature.ABYSSAL_ANGLER;
        if (r < legendaryChance + epicChance + rareChance + uncommonChance) return SeaCreature.REEF_STALKER;
        return SeaCreature.TIDAL_LURKER;
    }

    public static LivingEntity spawnSeaCreature(ServerLevel world, ServerPlayer player, SeaCreature creature, BlockPos pos) {
        LivingEntity entity = (LivingEntity) creature.baseType.create(world, EntitySpawnReason.TRIGGERED);
        if (entity == null) return null;

        entity.teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);

        Component name = Component.literal(creature.displayName)
                .withStyle(creature.tier.color, ChatFormatting.BOLD);
        entity.setCustomName(name);
        entity.setCustomNameVisible(true);

        if (entity instanceof Mob mob) {
            var maxHp = mob.getAttribute(Attributes.MAX_HEALTH);
            if (maxHp != null) {
                maxHp.setBaseValue(creature.health);
                mob.setHealth((float) creature.health);
            }
            var atk = mob.getAttribute(Attributes.ATTACK_DAMAGE);
            if (atk != null) {
                atk.setBaseValue(creature.health / 20.0);
            }
        }

        // Tag as sea creature
        entity.addTag("fishing_sea_creature");
        entity.addTag("sea_creature_" + creature.name());

        if (creature == SeaCreature.STORM_KRAKEN) {
            net.minecraft.world.level.saveddata.WeatherData wd = world.getWeatherData();
            wd.setRaining(true);
            wd.setThundering(true);
            wd.setRainTime(6000);
        }

        world.addFreshEntity(entity);
        activeSeaCreatures.add(entity.getUUID());

        player.sendSystemMessage(Component.literal("")
                .append(Component.literal("A ").withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(creature.displayName).withStyle(creature.tier.color, ChatFormatting.BOLD))
                .append(Component.literal(" surfaces!").withStyle(ChatFormatting.YELLOW)));

        return entity;
    }

    public static boolean isSeaCreature(UUID entityId) {
        return activeSeaCreatures.contains(entityId);
    }

    public static void onSeaCreatureKilled(ServerPlayer killer, LivingEntity entity) {
        activeSeaCreatures.remove(entity.getUUID());
        String uuid = killer.getUUID().toString();
        PlayerFishingData data = getData(uuid);

        SeaCreature killed = null;
        for (SeaCreature sc : SeaCreature.values()) {
            if (entity.entityTags().contains("sea_creature_" + sc.name())) {
                killed = sc;
                break;
            }
        }
        if (killed == null) return;

        data.creaturesKilled++;
        data.bestiaryKills.merge(killed.name(), 1, Integer::sum);
        data.discoveredCreatures.add(killed.name());

        int xp = killed.tier.xpReward;
        if (hasNode(uuid, "sh_sea_slayer")) xp = (int) (xp * 1.25);
        addXp(killer, xp);

        if (killed.tier.ordinal() > getMaxTierOrdinal(data.rarestCatch)) {
            data.rarestCatch = killed.displayName;
        }

        killer.sendSystemMessage(Component.literal("+" + xp + " Fishing XP ").withStyle(ChatFormatting.AQUA)
                .append(Component.literal("(" + killed.displayName + ")").withStyle(killed.tier.color)));

        dropSeaCreatureLoot(killer, killed, entity.level());
    }

    private static int getMaxTierOrdinal(String name) {
        for (SeaCreature sc : SeaCreature.values()) {
            if (sc.displayName.equals(name)) return sc.tier.ordinal();
        }
        return -1;
    }

    private static void dropSeaCreatureLoot(ServerPlayer player, SeaCreature creature, net.minecraft.world.level.Level world) {
        Random rng = new Random();
        int coins = switch (creature.tier) {
            case COMMON -> 500 + rng.nextInt(500);
            case UNCOMMON -> 2000 + rng.nextInt(3000);
            case RARE -> 10000 + rng.nextInt(10000);
            case EPIC -> 50000 + rng.nextInt(50000);
            case LEGENDARY -> 200000 + rng.nextInt(300000);
        };
        CoinManager.giveCoinsQuiet(player, coins);
        player.sendSystemMessage(Component.literal("+" + String.format("%,d", coins) + " coins").withStyle(ChatFormatting.GOLD));

        int shards = switch (creature.tier) {
            case COMMON -> 1;
            case UNCOMMON -> 2 + rng.nextInt(2);
            case RARE -> 5 + rng.nextInt(5);
            case EPIC -> 10 + rng.nextInt(10);
            case LEGENDARY -> 25 + rng.nextInt(25);
        };
        player.addItem(new ItemStack(Items.PRISMARINE_SHARD, shards));
    }

    // ════════════════════════════════════════════════════════════
    // BONUS CALCULATIONS
    // ════════════════════════════════════════════════════════════

    public static double getCatchSpeedMultiplier(String uuid) {
        PlayerFishingData data = getData(uuid);
        double mult = 1.0;

        FishingRods.RodTier rod = FishingRods.RodTier.fromId(data.equippedRod);
        if (rod != null) mult += rod.catchSpeedBonus / 100.0;

        if (hasNode(uuid, "rm_nimble_cast")) mult += 0.05;
        if (hasNode(uuid, "rm_steady_hand")) mult += 0.10;
        if (hasNode(uuid, "rm_anglers_focus")) mult += 0.15;
        if (hasNode(uuid, "rm_master_angler")) mult += 0.25;

        mult += PlayerBuffManager.getFishingSpeedBonus(uuid);

        return mult;
    }

    public static double getTreasureChance(String uuid) {
        PlayerFishingData data = getData(uuid);
        double chance = 0.0;

        FishingRods.RodTier rod = FishingRods.RodTier.fromId(data.equippedRod);
        if (rod != null) chance += rod.treasureBonus / 100.0;

        if (hasNode(uuid, "tf_lucky_hook")) chance += 0.05;
        if (hasNode(uuid, "tf_magnet_line")) chance += 0.05;
        if (hasNode(uuid, "tf_golden_reel")) chance += 0.15;

        return Math.min(0.50, chance);
    }

    public static double getSeaCreatureDamageMultiplier(String uuid) {
        double mult = 1.0;
        if (hasNode(uuid, "sh_hook_barb")) mult += 0.10;
        if (hasNode(uuid, "sh_sea_slayer")) mult += 0.25;
        if (hasNode(uuid, "sh_leviathan_bane")) mult += 0.50;
        return mult;
    }

    public static boolean hasLavaFishing(String uuid) {
        return hasNode(uuid, "ds_heat_proof") || hasNode(uuid, "ds_obsidian_hook")
                || hasNode(uuid, "ds_nether_angler");
    }

    // ════════════════════════════════════════════════════════════
    // PERSISTENCE
    // ════════════════════════════════════════════════════════════

    private static void loadData() {
        if (dataPath == null) return;
        File file = dataPath.resolve(DATA_FILE).toFile();
        if (!file.exists()) return;

        try (Reader reader = new FileReader(file)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            for (var entry : root.entrySet()) {
                playerData.put(entry.getKey(), PlayerFishingData.fromJson(entry.getValue().getAsJsonObject()));
            }
            PoliticalServer.LOGGER.info("[FishingManager] Loaded data for {} players", playerData.size());
        } catch (Exception e) {
            PoliticalServer.LOGGER.error("[FishingManager] Failed to load data", e);
        }
    }

    public static void saveData() {
        if (dataPath == null) return;
        File file = dataPath.resolve(DATA_FILE).toFile();

        JsonObject root = new JsonObject();
        for (var entry : playerData.entrySet()) {
            root.add(entry.getKey(), entry.getValue().toJson());
        }

        try (Writer writer = new FileWriter(file)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(root, writer);
        } catch (Exception e) {
            PoliticalServer.LOGGER.error("[FishingManager] Failed to save data", e);
        }
    }

    // ════════════════════════════════════════════════════════════
    // TICK (cleanup dead sea creatures)
    // ════════════════════════════════════════════════════════════

    public static void tick(MinecraftServer server) {
        if (server.getTickCount() % 100 != 0) return;
        activeSeaCreatures.removeIf(uuid -> {
            for (ServerLevel world : server.getAllLevels()) {
                net.minecraft.world.entity.Entity e = world.getEntity(uuid);
                if (e != null && e.isAlive()) return false;
            }
            return true;
        });
    }
}
