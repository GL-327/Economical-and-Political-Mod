package com.political;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.*;
import net.minecraft.server.world.ServerWorld;import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class PerkManager {

    private static final Map<String, Perk> PERKS = new LinkedHashMap<>();

    private static List<String> activePerks = new ArrayList<>();
    private static List<String> lastChairPerks = new ArrayList<>();
    private static List<String> chairSelectedPerks = new ArrayList<>();
    private static String viceChairPerk = null;
    private static final Identifier VOID_TOUCHED_MODIFIER_ID = Identifier.of("political", "void_touched");
    // CHANGED: Split into two separate flags
    private static boolean chairPerksSetThisTerm = false;
    private static boolean viceChairPerksSetThisTerm = false;

    // Store what was active in the PREVIOUS term for proper cooldown
    private static List<String> previousTermPerks = new ArrayList<>();

    private static final Identifier HEALTH_MODIFIER_ID = Identifier.of("political", "double_health");
    private static final Identifier DAMAGE_MODIFIER_ID = Identifier.of("political", "double_damage");
    private static final Identifier ARMOR_MODIFIER_ID = Identifier.of("political", "increased_armor");
    private static final Identifier FALL_MODIFIER_ID = Identifier.of("political", "softer_landing");
    private static final Identifier SCALE_MODIFIER_ID = Identifier.of("political", "bigger_isnt_always_better");
    private static final Identifier SPEED_MODIFIER_ID = Identifier.of("political", "public_works");
    private static final Identifier ATTACK_MODIFIER_ID = Identifier.of("political", "national_unity_attack");
    private static final Identifier ARMOR_TOUGH_MODIFIER_ID = Identifier.of("political", "fortified_shields");
    private static final Identifier ABSORPTION_MODIFIER_ID = Identifier.of("political", "battle_hardened");
    private static final Identifier SCALE_UP_MODIFIER_ID = Identifier.of("political", "tall_order");
    private static final Identifier GRAVITY_MODIFIER_ID = Identifier.of("political", "heavy_gravity");

    private static int tickCounter = 0;
    private static final Random random = new Random();

    static {
        // Add these in the static block with other registerPerk calls:

        registerPerk("DOUBLE_HEALTH", "Double Health", "All players have double health", 3, Perk.PerkType.POSITIVE);
        registerPerk("DOUBLE_DAMAGE", "Double Damage", "All players deal double damage", 3, Perk.PerkType.POSITIVE);
        registerPerk("INCREASED_ARMOUR", "Increased Armour", "All players have +8 armor", 2, Perk.PerkType.POSITIVE);
        registerPerk("SOFTER_LANDING", "Softer Landing", "Fall damage reduced by 50%", 1, Perk.PerkType.POSITIVE);
        registerPerk("LOOT_GALORE", "Loot Galore", "Mob drops are doubled", 2, Perk.PerkType.POSITIVE);
        registerPerk("PUBLIC_WORKS", "Public Works", "All players move 20% faster", 2, Perk.PerkType.POSITIVE);
        registerPerk("GOLDEN_AGE", "Golden Age", "Slow regeneration for all players", 3, Perk.PerkType.POSITIVE);
        registerPerk("NATIONAL_UNITY", "National Unity", "+25% damage dealt, -10% damage taken", 2, Perk.PerkType.POSITIVE);
        registerPerk("XP_TAX_CUTS", "XP Tax Cuts", "25% bonus XP from all sources", 2, Perk.PerkType.POSITIVE);
        registerPerk("GREEN_THUMB", "Green Thumb", "Crops grow 50% faster", 1, Perk.PerkType.POSITIVE);
        registerPerk("RESOURCE_SUBSIDY", "Resource Subsidy", "Furnaces smelt 50% faster", 1, Perk.PerkType.POSITIVE);
        registerPerk("MONSTER_CONTROL", "Monster Control", "Hostile mob spawns reduced by 30%", 2, Perk.PerkType.POSITIVE);
        registerPerk("PROSPERITY_SURGE", "Prosperity Surge", "Rare drops chance increased", 2, Perk.PerkType.POSITIVE);
        registerPerk("FORTIFIED_SHIELDS", "Fortified Shields",
                "All players gain +4 armor toughness", 2, Perk.PerkType.POSITIVE);

        registerPerk("SWIFT_HARVEST", "Swift Harvest",
                "Mining speed increased by 20%", 1, Perk.PerkType.POSITIVE);

        registerPerk("IRON_STOMACH", "Iron Stomach",
                "Food restores 50% more hunger", 1, Perk.PerkType.POSITIVE);

        registerPerk("LUCKY_FISHERMAN", "Lucky Fisherman",
                "Fishing treasure chance doubled", 1, Perk.PerkType.POSITIVE);

        registerPerk("BATTLE_HARDENED", "Battle Hardened",
                "+4 max absorption hearts permanently", 2, Perk.PerkType.POSITIVE);

        registerPerk("MERCHANTS_FAVOUR", "Merchant's Favour",
                "Villager trades cost 25% less", 2, Perk.PerkType.POSITIVE);
        registerPerk("PHOENIX_BLESSING", "Phoenix Blessing",
                "Respawn with full health + 5 sec fire resistance", 2, Perk.PerkType.POSITIVE);

        registerPerk("TREASURE_HUNTER", "Treasure Hunter",
                "Ores drop 25% more raw resources", 2, Perk.PerkType.POSITIVE);

        registerPerk("DIPLOMATIC_IMMUNITY", "Diplomatic Immunity",
                "Players take 50% less damage from other players", 3, Perk.PerkType.POSITIVE);

        registerPerk("NIGHTVISION_DECREE", "Nightvision Decree",
                "All players have permanent night vision", 1, Perk.PerkType.POSITIVE);

        registerPerk("ETERNAL_FOG", "Eternal Fog", "Permanent rain and night time", 0, Perk.PerkType.NEUTRAL);
        registerPerk("BIGGER_ISNT_ALWAYS_BETTER", "Bigger Isn't Always Better", "All players are *average size* (50% smaller)", 0, Perk.PerkType.NEUTRAL);
        registerPerk("NIGHT_OWL_POLICY", "Night Owl Policy", "No phantoms spawn, nights are longer", 0, Perk.PerkType.NEUTRAL);
        registerPerk("WILDLIFE_PROTECTION", "Wildlife Protection", "Passive mobs spawn more, hostiles less", 0, Perk.PerkType.NEUTRAL);
        registerPerk("BALANCED_BUDGET", "Balanced Budget", "No special effects (cosmetic fireworks)", 0, Perk.PerkType.NEUTRAL);
        registerPerk("CULTURAL_FESTIVAL", "Cultural Festival", "Occasional particle effects for all", 0, Perk.PerkType.NEUTRAL);
        registerPerk("TALL_ORDER", "Tall Order",
                "All players are 30% larger", 0, Perk.PerkType.NEUTRAL);

        registerPerk("ETERNAL_DAWN", "Eternal Dawn",
                "Time is locked to sunrise (clear weather)", 0, Perk.PerkType.NEUTRAL);

        registerPerk("CHAOS_LOTTERY", "Chaos Lottery",
                "Random player gets a diamond every 10 minutes", 0, Perk.PerkType.NEUTRAL);

        registerPerk("SILENT_WORLD", "Silent World",
                "No ambient mob sounds (peaceful atmosphere)", 0, Perk.PerkType.NEUTRAL);
        registerPerk("MIRROR_WORLD", "Mirror World",
                "Day/night cycle runs backwards", 0, Perk.PerkType.NEUTRAL);

        registerPerk("TRADERS_GAMBIT", "Trader's Gambit",
                "Villagers randomly offer 1 trade at 90% off, 1 at 200% markup", 0, Perk.PerkType.NEUTRAL);

        registerPerk("BLOOD_MOON", "Blood Moon",
                "Permanent night, but hostile mobs drop double XP", 0, Perk.PerkType.NEUTRAL);

        registerPerk("GIANTS_PLAYGROUND", "Giant's Playground",
                "All mobs are 50% larger (more health but slower)", 0, Perk.PerkType.NEUTRAL);

        registerPerk("CIVIL_UNREST", "Civil Unrest", "Random debuffs applied to players", -2, Perk.PerkType.NEGATIVE);
        registerPerk("CRIME_WAVE", "Crime Wave", "Hostile mob spawns increased by 30%", -2, Perk.PerkType.NEGATIVE);
        registerPerk("INFRASTRUCTURE_NEGLECT", "Infrastructure Neglect", "Movement speed reduced by 8%", -1, Perk.PerkType.NEGATIVE);
        registerPerk("ENVIRONMENTAL_MISMANAGEMENT", "Environmental Mismanagement", "Crops grow 30% slower", -1, Perk.PerkType.NEGATIVE);
        registerPerk("ECONOMIC_COLLAPSE", "Economic Collapse", "Villager trades cost 50% more", -2, Perk.PerkType.NEGATIVE);
        registerPerk("ARCANE_DECAY", "Arcane Decay", "Enchanting costs 50% more levels", -1, Perk.PerkType.NEGATIVE);
        registerPerk("REDUCED_PATROLS", "Reduced Patrols", "Pillager patrols spawn more frequently", -1, Perk.PerkType.NEGATIVE);
        registerPerk("MONSTER_UPRISING", "Monster Uprising", "Hostile mobs have +25% health", -2, Perk.PerkType.NEGATIVE);
        registerPerk("MINOR_CORRUPTION", "Minor Corruption", "10% XP loss from all sources", -1, Perk.PerkType.NEGATIVE);
        registerPerk("GLASS_CANNON", "Glass Cannon",
                "Deal +30% damage but take +20% damage", -2, Perk.PerkType.NEGATIVE);

        registerPerk("HEAVY_GRAVITY", "Heavy Gravity",
                "Jump height reduced, fall damage +25%", -1, Perk.PerkType.NEGATIVE);

        registerPerk("FAMINE", "Famine",
                "Hunger depletes 30% faster", -1, Perk.PerkType.NEGATIVE);

        registerPerk("CURSED_WATERS", "Cursed Waters",
                "Swimming speed reduced by 40%", -1, Perk.PerkType.NEGATIVE);

        registerPerk("CREEPER_SURGE", "Creeper Surge",
                "Creeper explosion radius +50%", -2, Perk.PerkType.NEGATIVE);

        registerPerk("BRITTLE_TOOLS", "Brittle Tools",
                "Tool durability drains 25% faster", -1, Perk.PerkType.NEGATIVE);
        registerPerk("WITHERING_ECONOMY", "Withering Economy",
                "Lose 1 credit every 30 minutes passively", -1, Perk.PerkType.NEGATIVE);

        registerPerk("PARANOIA", "Paranoia",
                "Endermen spawn 3x more frequently", -1, Perk.PerkType.NEGATIVE);

        registerPerk("SCORCHED_EARTH", "Scorched Earth",
                "Fire spreads 50% faster, fire damage +25%", -2, Perk.PerkType.NEGATIVE);

        registerPerk("VOID_TOUCHED", "Void Touched",
                "Max health reduced by 2 hearts for all players", -2, Perk.PerkType.NEGATIVE);

        // Auction Tax Perks
        registerPerk("AUCTION_TAX_FREE", "Free Trade Zone",
                "No auction house tax for all transactions",
                2, Perk.PerkType.POSITIVE);

        registerPerk("AUCTION_TAX_REDUCTION", "Trade Agreement",
                "Auction house tax reduced by 50%",
                1, Perk.PerkType.POSITIVE);

        registerPerk("AUCTION_TAX_INCREASE", "Trade Tariffs",
                "Auction house tax increased by 50% - generates government revenue",
                -1, Perk.PerkType.NEGATIVE);
    }


    private static void registerPerk(String id, String name, String description, int pointValue, Perk.PerkType type) {
        PERKS.put(id, new Perk(id, name, description, pointValue, type));
    }

    public static Perk getPerk(String id) {
        return PERKS.get(id);
    }

    public static Collection<Perk> getAllPerks() {
        return PERKS.values();
    }

    public static List<Perk> getPerksByType(Perk.PerkType type) {
        List<Perk> result = new ArrayList<>();
        for (Perk perk : PERKS.values()) {
            if (perk.type == type) {
                result.add(perk);
            }
        }
        return result;
    }

    public static List<String> getActivePerks() {
        return new ArrayList<>(activePerks);
    }

    public static void setActivePerks(List<String> perks) {
        activePerks = perks != null ? new ArrayList<>(perks) : new ArrayList<>();
    }

    public static List<String> getLastChairPerks() {
        return new ArrayList<>(lastChairPerks);
    }

    public static void setLastChairPerks(List<String> perks) {
        lastChairPerks = perks != null ? new ArrayList<>(perks) : new ArrayList<>();
    }

    public static List<String> getChairSelectedPerks() {
        return new ArrayList<>(chairSelectedPerks);
    }

    public static void setChairSelectedPerks(List<String> perks) {
        chairSelectedPerks = perks != null ? new ArrayList<>(perks) : new ArrayList<>();
    }

    public static String getViceChairPerk() {
        return viceChairPerk;
    }

    public static void setViceChairPerk(String perk) {
        viceChairPerk = perk;
    }

    // Uses previousTermPerks for accurate cooldown
    public static boolean isPerkOnCooldown(String perkId) {
        return previousTermPerks.contains(perkId);
    }

    // CHANGED: Now takes isChair parameter to check the right flag
    public static boolean canChangePerks(boolean isChair) {
        return isChair ? !chairPerksSetThisTerm : !viceChairPerksSetThisTerm;
    }

    // Called when a new term starts to allow perk selection
    public static void onNewTermStart() {
        // FIXED: Don't overwrite previousTermPerks here - it's already set in endElection()
        // previousTermPerks should already contain the perks from the previous term
        chairPerksSetThisTerm = false;
        viceChairPerksSetThisTerm = false;
    }

    // Getters and setters for persistence
    public static boolean isChairPerksSetThisTerm() {
        return chairPerksSetThisTerm;
    }

    public static void setChairPerksSetThisTerm(boolean set) {
        chairPerksSetThisTerm = set;
    }

    public static boolean isViceChairPerksSetThisTerm() {
        return viceChairPerksSetThisTerm;
    }

    public static void setViceChairPerksSetThisTerm(boolean set) {
        viceChairPerksSetThisTerm = set;
    }

    public static List<String> getPreviousTermPerks() {
        return new ArrayList<>(previousTermPerks);
    }

    public static void setPreviousTermPerks(List<String> perks) {
        previousTermPerks = perks != null ? new ArrayList<>(perks) : new ArrayList<>();
    }

    // Chair activating perks
    public static void activatePerks(List<String> chairPerks, String vcPerk) {
        chairSelectedPerks = new ArrayList<>(chairPerks);
        viceChairPerk = vcPerk;

        activePerks.clear();
        activePerks.addAll(chairPerks);
        if (vcPerk != null && !activePerks.contains(vcPerk)) {
            activePerks.add(vcPerk);
        }

        // Lock CHAIR perks for this term
        chairPerksSetThisTerm = true;

        if (PoliticalServer.server != null) {
            for (ServerPlayerEntity player : PoliticalServer.server.getPlayerManager().getPlayerList()) {
                applyActivePerks(player);

            }
        }

        DataManager.save(PoliticalServer.server);
    }

    // Vice Chair activating perks
    public static void activatePerksDirectly(List<String> allPerks, List<String> viceChairPerks) {
        lastChairPerks = new ArrayList<>(activePerks);

        activePerks.clear();
        activePerks.addAll(allPerks);

        if (viceChairPerks != null && !viceChairPerks.isEmpty()) {
            viceChairPerk = viceChairPerks.get(0);
        }

        // Lock VICE CHAIR perks for this term
        viceChairPerksSetThisTerm = true;

        if (PoliticalServer.server != null) {
            for (ServerPlayerEntity player : PoliticalServer.server.getPlayerManager().getPlayerList()) {
                applyActivePerks(player);

            }
        }

        DataManager.save(PoliticalServer.server);
    }

    public static void clearAllPerks() {
        if (PoliticalServer.server != null) {
            for (ServerPlayerEntity player : PoliticalServer.server.getPlayerManager().getPlayerList()) {
                removeAllPerkEffects(player);
            }
        }
        activePerks.clear();
        chairSelectedPerks.clear();  // ADD THIS
        viceChairPerk = null;        // ADD THIS
        DataManager.save(PoliticalServer.server);
    }

    public static void removeAllPerkEffects(ServerPlayerEntity player) {
        EntityAttributeInstance health = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (health != null) health.removeModifier(HEALTH_MODIFIER_ID);
        EntityAttributeInstance armorTough = player.getAttributeInstance(EntityAttributes.ARMOR_TOUGHNESS);
        if (armorTough != null) armorTough.removeModifier(ARMOR_TOUGH_MODIFIER_ID);

        EntityAttributeInstance scaleUp = player.getAttributeInstance(EntityAttributes.SCALE);
        if (scaleUp != null) scaleUp.removeModifier(SCALE_UP_MODIFIER_ID);

        EntityAttributeInstance gravity = player.getAttributeInstance(EntityAttributes.GRAVITY);
        if (gravity != null) gravity.removeModifier(GRAVITY_MODIFIER_ID);
        EntityAttributeInstance damage = player.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE);
        if (damage != null) {
            damage.removeModifier(DAMAGE_MODIFIER_ID);
            damage.removeModifier(ATTACK_MODIFIER_ID);
        }
        EntityAttributeInstance maxHealth = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.removeModifier(VOID_TOUCHED_MODIFIER_ID);
        }
// Also remove night vision if it was from our perk
        if (!activePerks.contains("NIGHTVISION_DECREE")) {
            player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
        EntityAttributeInstance armor = player.getAttributeInstance(EntityAttributes.ARMOR);
        if (armor != null) armor.removeModifier(ARMOR_MODIFIER_ID);

        EntityAttributeInstance fallDamage = player.getAttributeInstance(EntityAttributes.SAFE_FALL_DISTANCE);
        if (fallDamage != null) fallDamage.removeModifier(FALL_MODIFIER_ID);

        EntityAttributeInstance scale = player.getAttributeInstance(EntityAttributes.SCALE);
        if (scale != null) scale.removeModifier(SCALE_MODIFIER_ID);

        EntityAttributeInstance speed = player.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
        if (speed != null) speed.removeModifier(SPEED_MODIFIER_ID);
    }

    public static void applyActivePerks(ServerPlayerEntity player) {
        removeAllPerkEffects(player);

        if (activePerks.contains("FORTIFIED_SHIELDS")) {
            EntityAttributeInstance armorTough = player.getAttributeInstance(EntityAttributes.ARMOR_TOUGHNESS);
            if (armorTough != null) {
                armorTough.addPersistentModifier(new EntityAttributeModifier(
                        ARMOR_TOUGH_MODIFIER_ID, 4.0, EntityAttributeModifier.Operation.ADD_VALUE));
            }
        }
// Nightvision Decree
        if (activePerks.contains("NIGHTVISION_DECREE")) {
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.NIGHT_VISION, Integer.MAX_VALUE, 0, true, false, false));
        }

// Void Touched - reduce max health by 4 (2 hearts)
        if (activePerks.contains("VOID_TOUCHED")) {
            EntityAttributeInstance maxHealth = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.removeModifier(VOID_TOUCHED_MODIFIER_ID);
                maxHealth.addPersistentModifier(new EntityAttributeModifier(
                        VOID_TOUCHED_MODIFIER_ID, -4.0,
                        EntityAttributeModifier.Operation.ADD_VALUE));
            }
        }
        if (activePerks.contains("BATTLE_HARDENED")) {
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.ABSORPTION, 300, 1, true, false, false));
        }

        if (activePerks.contains("TALL_ORDER")) {
            EntityAttributeInstance scale = player.getAttributeInstance(EntityAttributes.SCALE);
            if (scale != null) {
                scale.addPersistentModifier(new EntityAttributeModifier(
                        SCALE_UP_MODIFIER_ID, 0.3, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        }

        if (activePerks.contains("SWIFT_HARVEST")) {
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.HASTE, 300, 0, true, false, false));
        }

        if (activePerks.contains("HEAVY_GRAVITY")) {
            EntityAttributeInstance gravity = player.getAttributeInstance(EntityAttributes.GRAVITY);
            if (gravity != null) {
                gravity.addPersistentModifier(new EntityAttributeModifier(
                        GRAVITY_MODIFIER_ID, 0.25, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        }
        if (activePerks.contains("DOUBLE_HEALTH")) {
            EntityAttributeInstance health = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
            if (health != null) {
                health.addPersistentModifier(new EntityAttributeModifier(
                        HEALTH_MODIFIER_ID, 1.0, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                player.setHealth(player.getMaxHealth());
            }
        }

        if (activePerks.contains("DOUBLE_DAMAGE")) {
            EntityAttributeInstance damage = player.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE);
            if (damage != null) {
                damage.addPersistentModifier(new EntityAttributeModifier(
                        DAMAGE_MODIFIER_ID, 1.0, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        }

        if (activePerks.contains("INCREASED_ARMOUR")) {
            EntityAttributeInstance armor = player.getAttributeInstance(EntityAttributes.ARMOR);
            if (armor != null) {
                armor.addPersistentModifier(new EntityAttributeModifier(
                        ARMOR_MODIFIER_ID, 8.0, EntityAttributeModifier.Operation.ADD_VALUE));
            }
        }

        if (activePerks.contains("SOFTER_LANDING")) {
            EntityAttributeInstance fallDistance = player.getAttributeInstance(EntityAttributes.SAFE_FALL_DISTANCE);
            if (fallDistance != null) {
                fallDistance.addPersistentModifier(new EntityAttributeModifier(
                        FALL_MODIFIER_ID, 1.0, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        }

        if (activePerks.contains("BIGGER_ISNT_ALWAYS_BETTER")) {
            EntityAttributeInstance scale = player.getAttributeInstance(EntityAttributes.SCALE);
            if (scale != null) {
                scale.addPersistentModifier(new EntityAttributeModifier(
                        SCALE_MODIFIER_ID, -0.5, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        }

        if (activePerks.contains("PUBLIC_WORKS")) {
            EntityAttributeInstance speed = player.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
            if (speed != null) {
                speed.addPersistentModifier(new EntityAttributeModifier(
                        SPEED_MODIFIER_ID, 0.2, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        }

        if (activePerks.contains("INFRASTRUCTURE_NEGLECT")) {
            EntityAttributeInstance speed = player.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
            if (speed != null) {
                speed.addPersistentModifier(new EntityAttributeModifier(
                        SPEED_MODIFIER_ID, -0.08, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        }

        if (activePerks.contains("NATIONAL_UNITY")) {
            EntityAttributeInstance damage = player.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE);
            if (damage != null) {
                damage.addPersistentModifier(new EntityAttributeModifier(
                        ATTACK_MODIFIER_ID, 0.25, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        }
    }

    public static void tickPerks(MinecraftServer server) {
        tickCounter++;
        if (tickCounter < 100) return;
        tickCounter = 0;
// Nightvision Decree - refresh every 200 ticks
        if (activePerks.contains("NIGHTVISION_DECREE") && tickCounter % 200 == 0) {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.NIGHT_VISION, 400, 0, true, false, false));
            }
        }

// Chaos Lottery (diamond) - already handled by DIAMOND_RAIN / CHAOS_LOTTERY
        if (activePerks.contains("CHAOS_LOTTERY") && tickCounter % 100 == 0) {
            if (random.nextInt(120) == 0) { // ~once per 10 min
                List<ServerPlayerEntity> players = new ArrayList<>(server.getPlayerManager().getPlayerList());
                if (!players.isEmpty()) {
                    ServerPlayerEntity lucky = players.get(random.nextInt(players.size()));
                    lucky.giveItemStack(new ItemStack(Items.DIAMOND));
                    lucky.sendMessage(Text.literal("💎 The Chaos Lottery chose you! Free diamond!")
                            .formatted(Formatting.AQUA), false);
                }
            }
        }

// Blood Moon - permanent night
        if (activePerks.contains("BLOOD_MOON") && tickCounter % 100 == 0) {
            for (ServerWorld world : server.getWorlds()) {
                if (world.getTimeOfDay() % 24000 < 13000) {
                    world.setTimeOfDay(18000);
                }
            }
        }

// Mirror World - reverse time (move backwards)
        if (activePerks.contains("MIRROR_WORLD") && tickCounter % 20 == 0) {
            for (ServerWorld world : server.getWorlds()) {
                long time = world.getTimeOfDay();
                world.setTimeOfDay(time - 40); // Go back 2x speed
            }
        }

// Eternal Dawn - lock to sunrise
        if (activePerks.contains("ETERNAL_DAWN") && tickCounter % 100 == 0) {
            for (ServerWorld world : server.getWorlds()) {
                world.setTimeOfDay(23000); // Sunrise
                if (world.isRaining()) {
                    world.setWeather(6000, 0, false, false);
                }
            }
        }

// Withering Economy - lose 1 credit every 30 min (36000 ticks)
        if (activePerks.contains("WITHERING_ECONOMY") && tickCounter % 36000 == 0) {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                // Integrate with your CoinManager/credit system
                // CoinManager.removeCredits(player.getUuidAsString(), 1);
                player.sendMessage(Text.literal("💸 The Withering Economy took 1 credit...")
                        .formatted(Formatting.RED), false);
            }
        }
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {

            if (activePerks.contains("GOLDEN_AGE")) {
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.REGENERATION, 200, 0, true, false, false));
            }
            if (activePerks.contains("BATTLE_HARDENED")) {
                if (!player.hasStatusEffect(StatusEffects.ABSORPTION)) {
                    player.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.ABSORPTION, 300, 1, true, false, false));
                }
            }

            if (activePerks.contains("SWIFT_HARVEST")) {
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.HASTE, 200, 0, true, false, false));
            }

            if (activePerks.contains("ETERNAL_DAWN")) {
                ServerWorld world = server.getWorld(player.getEntityWorld().getRegistryKey());
                if (world != null) {
                    world.setTimeOfDay(2000); // sunrise
                }
            }

            if (activePerks.contains("CHAOS_LOTTERY")) {
                // Once per 10 min = 12000 ticks. tickPerks runs every 100 ticks, so 1 in 120
                if (random.nextInt(120) == 0) {
                    List<ServerPlayerEntity> players = new ArrayList<>(server.getPlayerManager().getPlayerList());
                    if (!players.isEmpty()) {
                        ServerPlayerEntity lucky = players.get(random.nextInt(players.size()));
                        lucky.giveItemStack(new ItemStack(Items.DIAMOND));
                        lucky.sendMessage(Text.literal("✦ The Chaos Lottery smiles upon you! (+1 Diamond)").formatted(Formatting.AQUA));
                    }
                }
            }
            if (activePerks.contains("CIVIL_UNREST")) {
                if (random.nextInt(100) == 0) {
                    int choice = random.nextInt(5);
                    StatusEffectInstance debuff;
                    switch (choice) {
                        case 0:
                            debuff = new StatusEffectInstance(StatusEffects.SLOWNESS, 200, 0, true, false, false);
                            break;
                        case 1:
                            debuff = new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 200, 0, true, false, false);
                            break;
                        case 2:
                            debuff = new StatusEffectInstance(StatusEffects.WEAKNESS, 200, 0, true, false, false);
                            break;
                        case 3:
                            debuff = new StatusEffectInstance(StatusEffects.NAUSEA, 100, 0, true, false, false);
                            break;
                        default:
                            debuff = new StatusEffectInstance(StatusEffects.HUNGER, 200, 0, true, false, true);
                            break;
                    }
                    player.addStatusEffect(debuff);
                    player.sendMessage(Text.literal("Civil unrest affects you...").formatted(Formatting.RED));
                }
            }

            if (activePerks.contains("CULTURAL_FESTIVAL")) {
                if (random.nextInt(20) == 0) {
                    ServerWorld world = server.getWorld(player.getEntityWorld().getRegistryKey());
                    if (world != null) {
                        world.spawnParticles(
                                ParticleTypes.FIREWORK,
                                player.getX(), player.getY() + 2, player.getZ(),
                                10, 1.0, 1.0, 1.0, 0.1
                        );
                    }
                }
            }

            if (activePerks.contains("BALANCED_BUDGET")) {
                if (random.nextInt(100) == 0) {
                    ServerWorld world = server.getWorld(player.getEntityWorld().getRegistryKey());
                    if (world != null) {
                        world.spawnParticles(
                                ParticleTypes.HAPPY_VILLAGER,
                                player.getX(), player.getY() + 1, player.getZ(),
                                5, 0.5, 0.5, 0.5, 0.0
                        );
                    }
                }
            }
        }
    }

    // ============================================================
    // HELPER METHODS FOR MIXINS
    // ============================================================
// For Diplomatic Immunity - use in LivingEntityMixin for PvP damage reduction
    public static float getPvpDamageMultiplier() {
        if (activePerks.contains("DIPLOMATIC_IMMUNITY")) return 0.5f;
        return 1.0f;
    }

    // For Treasure Hunter - use in LootTableMixin for ore drops
    public static float getOreDropMultiplier() {
        if (activePerks.contains("TREASURE_HUNTER")) return 1.25f;
        return 1.0f;
    }

    // For Scorched Earth - use in a fire damage mixin
    public static float getFireDamageMultiplier() {
        if (activePerks.contains("SCORCHED_EARTH")) return 1.25f;
        return 1.0f;
    }

    // For Blood Moon - double XP from hostile mobs
    public static float getMobXpMultiplier() {
        float multiplier = getXpMultiplier();
        if (activePerks.contains("BLOOD_MOON")) multiplier += 1.0f;
        return multiplier;
    }

    // For Paranoia - enderman spawn rate
    public static float getEndermanSpawnMultiplier() {
        if (activePerks.contains("PARANOIA")) return 3.0f;
        return 1.0f;
    }
    public static boolean hasActivePerk(String perkId) {
        return activePerks.contains(perkId);
    }

    public static float getLootMultiplier() {
        if (activePerks.contains("LOOT_GALORE")) return 2.0f;
        if (activePerks.contains("PROSPERITY_SURGE")) return 1.5f;
        return 1.0f;
    }

    public static float getXpMultiplier() {
        float multiplier = 1.0f;
        if (activePerks.contains("XP_TAX_CUTS")) multiplier += 0.25f;
        if (activePerks.contains("MINOR_CORRUPTION")) multiplier -= 0.10f;
        return multiplier;
    }

    public static float getCropGrowthMultiplier() {
        if (activePerks.contains("GREEN_THUMB")) return 1.5f;
        if (activePerks.contains("ENVIRONMENTAL_MISMANAGEMENT")) return 0.7f;
        return 1.0f;
    }

    public static float getFurnaceSpeedMultiplier() {
        if (activePerks.contains("RESOURCE_SUBSIDY")) return 1.5f;
        return 1.0f;
    }

    public static float getHostileSpawnMultiplier() {
        float multiplier = 1.0f;
        if (activePerks.contains("MONSTER_CONTROL")) multiplier -= 0.3f;
        if (activePerks.contains("CRIME_WAVE")) multiplier += 0.3f;
        if (activePerks.contains("WILDLIFE_PROTECTION")) multiplier -= 0.2f;
        return Math.max(0.1f, multiplier);
    }

    public static float getPassiveSpawnMultiplier() {
        if (activePerks.contains("WILDLIFE_PROTECTION")) return 1.5f;
        return 1.0f;
    }

    public static float getMobHealthMultiplier() {
        if (activePerks.contains("MONSTER_UPRISING")) return 1.25f;
        return 1.0f;
    }

    public static float getDamageReductionMultiplier() {
        if (activePerks.contains("NATIONAL_UNITY")) return 0.9f;
        return 1.0f;
    }

    public static float getTradeMultiplier() {
        if (activePerks.contains("ECONOMIC_COLLAPSE")) return 1.5f;
        return 1.0f;
    }

    public static float getEnchantCostMultiplier() {
        if (activePerks.contains("ARCANE_DECAY")) return 1.5f;
        return 1.0f;
    }

    public static boolean shouldPreventPhantoms() {
        return activePerks.contains("NIGHT_OWL_POLICY");
    }

    public static float getPatrolSpawnMultiplier() {
        if (activePerks.contains("REDUCED_PATROLS")) return 2.0f;
        return 1.0f;

    }
}