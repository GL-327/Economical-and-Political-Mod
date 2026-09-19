package com.political.government;
import com.political.BossAbilityManager;
import com.political.DataManager;
import com.political.PoliticalServer;
import com.political.Season;
import com.political.WeatherManager;
import com.political.mixin.LivingEntityMixin;
import com.political.mixin.LootTableMixin;



import com.political.bounty.SlayerManager;
import com.political.economy.CoinManager;
import com.political.party.PoliticalPartyManager;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;

import java.util.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class PerkManager {

    private static final Map<String, Perk> PERKS = new LinkedHashMap<>();

    private static List<String> activePerks = new ArrayList<>();
    private static List<String> lastChairPerks = new ArrayList<>();
    private static List<String> chairSelectedPerks = new ArrayList<>();
    private static String viceChairPerk = null;
    private static List<String> viceChairPerks = new ArrayList<>();
    private static final Identifier VOID_TOUCHED_MODIFIER_ID = Identifier.fromNamespaceAndPath("political", "void_touched");
    // CHANGED: Split into two separate flags
    private static boolean chairPerksSetThisTerm = false;
    private static boolean viceChairPerksSetThisTerm = false;

    // Store what was active in the PREVIOUS term for proper cooldown
    private static List<String> previousTermPerks = new ArrayList<>();

    private static final Identifier HEALTH_MODIFIER_ID = Identifier.fromNamespaceAndPath("political", "double_health");
    private static final Identifier DAMAGE_MODIFIER_ID = Identifier.fromNamespaceAndPath("political", "double_damage");
    private static final Identifier ARMOR_MODIFIER_ID = Identifier.fromNamespaceAndPath("political", "increased_armor");
    private static final Identifier FALL_MODIFIER_ID = Identifier.fromNamespaceAndPath("political", "softer_landing");
    private static final Identifier SCALE_MODIFIER_ID = Identifier.fromNamespaceAndPath("political", "bigger_isnt_always_better");
    private static final Identifier SPEED_MODIFIER_ID = Identifier.fromNamespaceAndPath("political", "public_works");
    private static final Identifier ATTACK_MODIFIER_ID = Identifier.fromNamespaceAndPath("political", "national_unity_attack");
    private static final Identifier ARMOR_TOUGH_MODIFIER_ID = Identifier.fromNamespaceAndPath("political", "fortified_shields");
    private static final Identifier ABSORPTION_MODIFIER_ID = Identifier.fromNamespaceAndPath("political", "battle_hardened");
    private static final Identifier SCALE_UP_MODIFIER_ID = Identifier.fromNamespaceAndPath("political", "tall_order");
    private static final Identifier GRAVITY_MODIFIER_ID = Identifier.fromNamespaceAndPath("political", "heavy_gravity");
    private static final Identifier VETERANS_CARE_MODIFIER_ID = Identifier.fromNamespaceAndPath("political", "veterans_care_hp");
    private static final Identifier COMMONS_SPEED_MODIFIER_ID = Identifier.fromNamespaceAndPath("political", "commons_license_speed");

    private static int tickCounter = 0;
    private static int perkCycleCounter = 0;
    private static int witheringEconomyCounter = 0;
    private static final Random random = new Random();

    static {
        // Add these in the static block with other registerPerk calls:

        registerPerk("DOUBLE_HEALTH", "Double Health", "All players have double health", 3, Perk.PerkType.POSITIVE);
        registerPerk("DOUBLE_DAMAGE", "Double Damage", "All players deal double damage", 3, Perk.PerkType.POSITIVE);
        registerPerk("INCREASED_ARMOUR", "Increased Armour", "All players have +8 armor", 2, Perk.PerkType.POSITIVE);
        registerPerk("SOFTER_LANDING", "Softer Landing", "Fall damage reduced by 50%", 1, Perk.PerkType.POSITIVE);
                registerPerk("PUBLIC_WORKS", "Public Works", "All players move 20% faster", 2, Perk.PerkType.POSITIVE);
        registerPerk("GOLDEN_AGE", "Golden Age", "Slow regeneration for all players", 3, Perk.PerkType.POSITIVE);
        registerPerk("NATIONAL_UNITY", "National Unity", "+25% damage dealt, -10% damage taken", 2, Perk.PerkType.POSITIVE);
        registerPerk("XP_TAX_CUTS", "XP Tax Cuts", "25% bonus XP from all sources", 2, Perk.PerkType.POSITIVE);
        registerPerk("GREEN_THUMB", "Green Thumb", "Crops grow 50% faster", 1, Perk.PerkType.POSITIVE);
        registerPerk("RESOURCE_SUBSIDY", "Resource Subsidy", "Furnaces smelt 50% faster", 1, Perk.PerkType.POSITIVE);
        registerPerk("MONSTER_CONTROL", "Monster Control", "Hostile mob spawns reduced by 30%", 2, Perk.PerkType.POSITIVE);
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
                "Lose 500 coins every 30 minutes passively", -1, Perk.PerkType.NEGATIVE);

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

        // Bounty-based perks (V2.0)
        registerPerk("BOUNTY_HUNTER_SURGE", "Bounty Hunter's Surge",
                "Bounty XP gains increased by 25% for all players", 2, Perk.PerkType.POSITIVE);
        registerPerk("SLAYER_CONTRACTS", "Slayer Contracts",
                "Bounty bosses drop 50% more chunks and cores", 1, Perk.PerkType.POSITIVE);
        registerPerk("WEAKENED_QUARRY", "Weakened Quarry",
                "Bounty bosses have 20% less health", 2, Perk.PerkType.POSITIVE);
        registerPerk("HUNTERS_INSTINCT", "Hunter's Instinct",
                "Players deal 15% more damage to bounty bosses", 1, Perk.PerkType.POSITIVE);
        registerPerk("BOUNTY_TAX", "Bounty Tax",
                "10% of bounty coin rewards are taxed into political party budgets (split)", -1, Perk.PerkType.NEGATIVE);
        registerPerk("HARDENED_QUARRY", "Hardened Quarry",
                "Bounty bosses have 30% more health", -2, Perk.PerkType.NEGATIVE);

        // Expanded sets (paginated GUI)
        registerPerk("ACADEMY_GRANTS", "Academy Grants",
                "+10% experience from all sources", 1, Perk.PerkType.POSITIVE);
        registerPerk("MINERS_ALLIANCE", "Miners' Alliance",
                "+15% ore drop quantity (stacks with Treasure Hunter)", 2, Perk.PerkType.POSITIVE);
        registerPerk("RATIONS_PROGRAM", "Rations Program",
                "Food restores 25% more hunger", 1, Perk.PerkType.POSITIVE);
        registerPerk("VETERANS_CARE", "Veterans' Care",
                "+2 max hearts", 2, Perk.PerkType.POSITIVE);
        registerPerk("OPEN_SKIES", "Open Skies",
                "Slow falling when outdoors (renewing)", 1, Perk.PerkType.POSITIVE);
        registerPerk("COMMONS_LICENSE", "Commons License",
                "+8% movement speed", 1, Perk.PerkType.POSITIVE);
        registerPerk("STORM_DRILL", "Storm Drill",
                "Permanent Haste I while perks active", 1, Perk.PerkType.POSITIVE);
        registerPerk("BOUNTY_SUBSIDY_II", "Bounty Subsidy II",
                "Bounty bosses drop +15% more chunks/cores (stacks)", 2, Perk.PerkType.POSITIVE);

        registerPerk("AURORA_POLICY", "Aurora Policy",
                "Frequent harmless sky particles", 0, Perk.PerkType.NEUTRAL);
        registerPerk("MUSEUM_CURFEW", "Museum Curfew",
                "Passive mobs move slightly slower (flavor)", 0, Perk.PerkType.NEUTRAL);
        registerPerk("COIN_FLIP_DECREE", "Coin Flip Decree",
                "Random small luck or unluck bursts", 0, Perk.PerkType.NEUTRAL);
        registerPerk("HUSHED_REALM", "Hushed Realm",
                "Reduced hostile detection range (subtle)", 0, Perk.PerkType.NEUTRAL);
        registerPerk("TIDE_LOCK", "Tide Lock",
                "Slower day/night (cosmetic pacing)", 0, Perk.PerkType.NEUTRAL);

        registerPerk("SLAYER_TOLL", "Slayer Toll",
                "Bounty coin payouts reduced by 8%", -1, Perk.PerkType.NEGATIVE);
        registerPerk("QUARRY_FATIGUE", "Quarry Fatigue",
                "Mining Fatigue I (renewing)", -1, Perk.PerkType.NEGATIVE);
        registerPerk("PLAGUE_WARNING", "Plague Warning",
                "Occasional short poison in unsafe light", -1, Perk.PerkType.NEGATIVE);
        registerPerk("EMPTY_COFFERS", "Empty Coffers",
                "Auction house tax +10%", -1, Perk.PerkType.NEGATIVE);
        registerPerk("RIOT_SEASON", "Riot Season",
                "Hostile spawns +15%", -2, Perk.PerkType.NEGATIVE);
        registerPerk("TARIFF_SPIKE", "Tariff Spike",
                "Villager trades cost +15%", -1, Perk.PerkType.NEGATIVE);
        registerPerk("ARCANE_LEVY", "Arcane Levy",
                "Enchanting costs +25% levels", -1, Perk.PerkType.NEGATIVE);
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

    public static List<String> getViceChairPerks() {
        if (!viceChairPerks.isEmpty()) return new ArrayList<>(viceChairPerks);
        if (viceChairPerk != null) return new ArrayList<>(List.of(viceChairPerk));
        return new ArrayList<>();
    }

    public static void setViceChairPerk(String perk) {
        viceChairPerk = perk;
        viceChairPerks.clear();
        if (perk != null) viceChairPerks.add(perk);
    }

    public static void setViceChairPerks(List<String> perks) {
        viceChairPerks = perks != null ? new ArrayList<>(perks) : new ArrayList<>();
        viceChairPerk = viceChairPerks.isEmpty() ? null : viceChairPerks.get(0);
    }

    /** True if this perk was used last term by the Chair or Vice Chair (cannot pick again until the term after next). */
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
        activatePerks(chairPerks, vcPerk == null ? List.of() : List.of(vcPerk));
    }

    public static void activatePerks(List<String> chairPerks, List<String> vcPerks) {
        chairSelectedPerks = new ArrayList<>(chairPerks);
        setViceChairPerks(vcPerks);

        activePerks.clear();
        activePerks.addAll(chairPerks);
        for (String vcPerk : getViceChairPerks()) {
            if (vcPerk != null && !activePerks.contains(vcPerk)) {
                activePerks.add(vcPerk);
            }
        }

        // Lock CHAIR perks for this term
        chairPerksSetThisTerm = true;

        if (PoliticalServer.server != null) {
            for (ServerPlayer player : PoliticalServer.server.getPlayerList().getPlayers()) {
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
            setViceChairPerks(viceChairPerks);
        }

        // Lock VICE CHAIR perks for this term
        viceChairPerksSetThisTerm = true;

        if (PoliticalServer.server != null) {
            for (ServerPlayer player : PoliticalServer.server.getPlayerList().getPlayers()) {
                applyActivePerks(player);

            }
        }

        DataManager.save(PoliticalServer.server);
    }

    public static void clearAllPerks() {
        if (PoliticalServer.server != null) {
            for (ServerPlayer player : PoliticalServer.server.getPlayerList().getPlayers()) {
                removeAllPerkEffects(player);
            }
        }
        activePerks.clear();
        chairSelectedPerks.clear();  // ADD THIS
        viceChairPerk = null;
        viceChairPerks.clear();
        DataManager.save(PoliticalServer.server);
    }

    public static void removeAllPerkEffects(ServerPlayer player) {
        AttributeInstance health = player.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) health.removeModifier(HEALTH_MODIFIER_ID);
        AttributeInstance armorTough = player.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (armorTough != null) armorTough.removeModifier(ARMOR_TOUGH_MODIFIER_ID);

        AttributeInstance scaleUp = player.getAttribute(Attributes.SCALE);
        if (scaleUp != null) scaleUp.removeModifier(SCALE_UP_MODIFIER_ID);

        AttributeInstance gravity = player.getAttribute(Attributes.GRAVITY);
        if (gravity != null) gravity.removeModifier(GRAVITY_MODIFIER_ID);
        AttributeInstance damage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null) {
            damage.removeModifier(DAMAGE_MODIFIER_ID);
            damage.removeModifier(ATTACK_MODIFIER_ID);
        }
        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.removeModifier(VOID_TOUCHED_MODIFIER_ID);
            maxHealth.removeModifier(VETERANS_CARE_MODIFIER_ID);
        }
// Also remove night vision if it was from our perk
        if (!activePerks.contains("NIGHTVISION_DECREE")) {
            player.removeEffect(MobEffects.NIGHT_VISION);
        }
        AttributeInstance armor = player.getAttribute(Attributes.ARMOR);
        if (armor != null) armor.removeModifier(ARMOR_MODIFIER_ID);

        AttributeInstance fallDamage = player.getAttribute(Attributes.SAFE_FALL_DISTANCE);
        if (fallDamage != null) fallDamage.removeModifier(FALL_MODIFIER_ID);

        AttributeInstance scale = player.getAttribute(Attributes.SCALE);
        if (scale != null) scale.removeModifier(SCALE_MODIFIER_ID);

        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(SPEED_MODIFIER_ID);
            speed.removeModifier(COMMONS_SPEED_MODIFIER_ID);
        }
    }

    public static void applyActivePerks(ServerPlayer player) {
        removeAllPerkEffects(player);

        if (activePerks.contains("FORTIFIED_SHIELDS")) {
            AttributeInstance armorTough = player.getAttribute(Attributes.ARMOR_TOUGHNESS);
            if (armorTough != null) {
                armorTough.addPermanentModifier(new AttributeModifier(
                        ARMOR_TOUGH_MODIFIER_ID, 4.0, AttributeModifier.Operation.ADD_VALUE));
            }
        }
// Nightvision Decree
        if (activePerks.contains("NIGHTVISION_DECREE")) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.NIGHT_VISION, Integer.MAX_VALUE, 0, true, false, false));
        }

// Void Touched - reduce max health by 4 (2 hearts)
        if (activePerks.contains("VOID_TOUCHED")) {
            AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.removeModifier(VOID_TOUCHED_MODIFIER_ID);
                maxHealth.addPermanentModifier(new AttributeModifier(
                        VOID_TOUCHED_MODIFIER_ID, -4.0,
                        AttributeModifier.Operation.ADD_VALUE));
            }
        }
        if (activePerks.contains("BATTLE_HARDENED")) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.ABSORPTION, 300, 1, true, false, false));
        }

        if (activePerks.contains("TALL_ORDER")) {
            AttributeInstance scale = player.getAttribute(Attributes.SCALE);
            if (scale != null) {
                scale.addPermanentModifier(new AttributeModifier(
                        SCALE_UP_MODIFIER_ID, 0.3, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        }

        if (activePerks.contains("SWIFT_HARVEST")) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.HASTE, 300, 0, true, false, false));
        }

        if (activePerks.contains("HEAVY_GRAVITY")) {
            AttributeInstance gravity = player.getAttribute(Attributes.GRAVITY);
            if (gravity != null) {
                gravity.addPermanentModifier(new AttributeModifier(
                        GRAVITY_MODIFIER_ID, 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        }
        if (activePerks.contains("DOUBLE_HEALTH")) {
            AttributeInstance health = player.getAttribute(Attributes.MAX_HEALTH);
            if (health != null) {
                health.addPermanentModifier(new AttributeModifier(
                        HEALTH_MODIFIER_ID, 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                player.setHealth(player.getMaxHealth());
            }
        }

        if (activePerks.contains("DOUBLE_DAMAGE")) {
            AttributeInstance damage = player.getAttribute(Attributes.ATTACK_DAMAGE);
            if (damage != null) {
                damage.addPermanentModifier(new AttributeModifier(
                        DAMAGE_MODIFIER_ID, 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        }

        if (activePerks.contains("INCREASED_ARMOUR")) {
            AttributeInstance armor = player.getAttribute(Attributes.ARMOR);
            if (armor != null) {
                armor.addPermanentModifier(new AttributeModifier(
                        ARMOR_MODIFIER_ID, 8.0, AttributeModifier.Operation.ADD_VALUE));
            }
        }

        if (activePerks.contains("SOFTER_LANDING")) {
            AttributeInstance fallDistance = player.getAttribute(Attributes.SAFE_FALL_DISTANCE);
            if (fallDistance != null) {
                fallDistance.addPermanentModifier(new AttributeModifier(
                        FALL_MODIFIER_ID, 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        }

        if (activePerks.contains("BIGGER_ISNT_ALWAYS_BETTER")) {
            AttributeInstance scale = player.getAttribute(Attributes.SCALE);
            if (scale != null) {
                scale.addPermanentModifier(new AttributeModifier(
                        SCALE_MODIFIER_ID, -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        }

        if (activePerks.contains("PUBLIC_WORKS")) {
            AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null) {
                speed.addPermanentModifier(new AttributeModifier(
                        SPEED_MODIFIER_ID, 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        }

        if (activePerks.contains("INFRASTRUCTURE_NEGLECT")) {
            AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null) {
                speed.addPermanentModifier(new AttributeModifier(
                        SPEED_MODIFIER_ID, -0.08, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        }

        if (activePerks.contains("NATIONAL_UNITY")) {
            AttributeInstance damage = player.getAttribute(Attributes.ATTACK_DAMAGE);
            if (damage != null) {
                damage.addPermanentModifier(new AttributeModifier(
                        ATTACK_MODIFIER_ID, 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        }

        if (activePerks.contains("VETERANS_CARE")) {
            AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.addPermanentModifier(new AttributeModifier(
                        VETERANS_CARE_MODIFIER_ID, 4.0, AttributeModifier.Operation.ADD_VALUE));
            }
        }

        if (activePerks.contains("COMMONS_LICENSE")) {
            AttributeInstance sp = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (sp != null) {
                sp.addPermanentModifier(new AttributeModifier(
                        COMMONS_SPEED_MODIFIER_ID, 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        }
    }

    /**
     * Overworld day/night perks using 26.x {@link ServerClockManager} / {@link WorldClocks#OVERWORLD}.
     * Priority for hard locks: {@code BLOOD_MOON} &gt; {@code ETERNAL_FOG} (weather/time in {@link WeatherManager})
     * &gt; {@code ETERNAL_DAWN}. {@code MIRROR_WORLD} / {@code TIDE_LOCK} apply only when none of those three lock time.
     */
    public static void tickOverworldClockPerks(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        Holder<WorldClock> clockHolder = overworld.registryAccess()
                .lookupOrThrow(Registries.WORLD_CLOCK)
                .getOrThrow(WorldClocks.OVERWORLD);
        ServerClockManager cm = overworld.clockManager();
        long total = cm.getTotalTicks(clockHolder);
        long dayBase = (total / 24000) * 24000;
        long tod = total - dayBase;

        boolean blood = hasActivePerk("BLOOD_MOON");
        boolean dawn = hasActivePerk("ETERNAL_DAWN");
        boolean fog = hasActivePerk("ETERNAL_FOG");

        if (blood) {
            long target = 18000;
            if (Math.abs(tod - target) > 50) {
                cm.setTotalTicks(clockHolder, dayBase + target);
            }
        } else if (fog) {
            // Time + weather for ETERNAL_FOG handled in WeatherManager.tick
        } else if (dawn) {
            if (overworld.isRaining()) {
                overworld.resetWeatherCycle();
            }
            long target = 1000;
            if (Math.abs(tod - target) > 50) {
                cm.setTotalTicks(clockHolder, dayBase + target);
            }
        } else {
            if (hasActivePerk("MIRROR_WORLD")) {
                cm.setTotalTicks(clockHolder, total - 2);
            } else if (hasActivePerk("TIDE_LOCK") && server.getTickCount() % 2 == 0) {
                cm.setTotalTicks(clockHolder, total - 1);
            }
        }

        if (hasActivePerk("NIGHT_OWL_POLICY")) {
            long t = cm.getTotalTicks(clockHolder);
            long phase = t % 24000;
            if (phase >= 13000 && phase <= 23000 && server.getTickCount() % 20 == 0) {
                cm.setTotalTicks(clockHolder, t - 1);
            }
        }
    }

    public static void tickPerks(MinecraftServer server) {
        tickCounter++;
        com.political.government.Treasury.tickDaily(server);
        if (tickCounter < 100) return;
        tickCounter = 0;
        perkCycleCounter++;
// Nightvision Decree - refresh every 200 ticks (every other cycle)
        if (activePerks.contains("NIGHTVISION_DECREE") && perkCycleCounter % 2 == 0) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.NIGHT_VISION, 400, 0, true, false, false));
            }
        }

// Chaos Lottery (diamond) - already handled by DIAMOND_RAIN / CHAOS_LOTTERY
        if (activePerks.contains("CHAOS_LOTTERY")) {
            if (random.nextInt(120) == 0) { // ~once per 10 min
                List<ServerPlayer> players = new ArrayList<>(server.getPlayerList().getPlayers());
                if (!players.isEmpty()) {
                    ServerPlayer lucky = players.get(random.nextInt(players.size()));
                    lucky.addItem(new ItemStack(Items.DIAMOND));
                    lucky.sendSystemMessage(Component.literal("💎 The Chaos Lottery chose you! Free diamond!")
                            .withStyle(ChatFormatting.AQUA), false);
                }
            }
        }

        // ETERNAL_DAWN / BLOOD_MOON / MIRROR_WORLD / TIDE_LOCK / NIGHT_OWL_POLICY: overworld clock in tickOverworldClockPerks

// Withering Economy - lose 500 coins every 30 min (360 * 100 ticks = 36000 ticks)
        if (activePerks.contains("WITHERING_ECONOMY")) {
            witheringEconomyCounter++;
            if (witheringEconomyCounter >= 360) {
                witheringEconomyCounter = 0;
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    CoinManager.removeCoins(player, 500);
                    player.sendSystemMessage(Component.literal("💸 The Withering Economy took 500 coins from you!")
                            .withStyle(ChatFormatting.RED), false);
                }
            }
        } else {
            witheringEconomyCounter = 0;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {

            if (activePerks.contains("GOLDEN_AGE")) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.REGENERATION, 200, 0, true, false, false));
            }
            if (activePerks.contains("BATTLE_HARDENED")) {
                if (!player.hasEffect(MobEffects.ABSORPTION)) {
                    player.addEffect(new MobEffectInstance(
                            MobEffects.ABSORPTION, 300, 1, true, false, false));
                }
            }

            if (activePerks.contains("SWIFT_HARVEST")) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.HASTE, 200, 0, true, false, false));
            }

            if (activePerks.contains("CIVIL_UNREST")) {
                if (random.nextInt(100) == 0) {
                    int choice = random.nextInt(5);
                    MobEffectInstance debuff;
                    switch (choice) {
                        case 0:
                            debuff = new MobEffectInstance(MobEffects.SLOWNESS, 200, 0, true, false, false);
                            break;
                        case 1:
                            debuff = new MobEffectInstance(MobEffects.MINING_FATIGUE, 200, 0, true, false, false);
                            break;
                        case 2:
                            debuff = new MobEffectInstance(MobEffects.WEAKNESS, 200, 0, true, false, false);
                            break;
                        case 3:
                            debuff = new MobEffectInstance(MobEffects.NAUSEA, 100, 0, true, false, false);
                            break;
                        default:
                            debuff = new MobEffectInstance(MobEffects.HUNGER, 200, 0, true, false, true);
                            break;
                    }
                    player.addEffect(debuff);
                    player.sendSystemMessage(Component.literal("Civil unrest affects you...").withStyle(ChatFormatting.RED));
                }
            }

            if (activePerks.contains("CULTURAL_FESTIVAL")) {
                if (random.nextInt(20) == 0) {
                    ServerLevel world = server.getLevel(player.level().dimension());
                    if (world != null) {
                        world.sendParticles(
                                ParticleTypes.FIREWORK,
                                player.getX(), player.getY() + 2, player.getZ(),
                                4, 0.6, 0.6, 0.6, 0.1
                        );
                    }
                }
            }

            if (activePerks.contains("STORM_DRILL")) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.HASTE, 220, 0, true, false, false));
            }
            if (activePerks.contains("OPEN_SKIES")) {
                int skyLight = player.level().getBrightness(net.minecraft.world.level.LightLayer.SKY, player.blockPosition());
                if (skyLight > 7 && player.level().isBrightOutside()) {
                    player.addEffect(new MobEffectInstance(
                            MobEffects.SLOW_FALLING, 120, 0, true, false, false));
                }
            }
            if (activePerks.contains("QUARRY_FATIGUE")) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.MINING_FATIGUE, 220, 0, true, false, false));
            }
            if (activePerks.contains("PLAGUE_WARNING") && random.nextInt(100) == 0) {
                if (player.level().getMaxLocalRawBrightness(player.blockPosition()) < 8) {
                    player.addEffect(new MobEffectInstance(
                            MobEffects.POISON, 100, 0, true, false, false));
                }
            }
            if (activePerks.contains("AURORA_POLICY") && random.nextInt(25) == 0) {
                ServerLevel aw = server.getLevel(player.level().dimension());
                if (aw != null) {
                    aw.sendParticles(
                            ParticleTypes.DRIPPING_OBSIDIAN_TEAR,
                            player.getX(), player.getY() + 3, player.getZ(),
                            4, 0.8, 0.5, 0.8, 0.02);
                }
            }
            if (activePerks.contains("COIN_FLIP_DECREE") && random.nextInt(60) == 0) {
                if (random.nextBoolean()) {
                    player.addEffect(new MobEffectInstance(
                            MobEffects.LUCK, 300, 0, true, false, false));
                } else {
                    player.addEffect(new MobEffectInstance(
                            MobEffects.UNLUCK, 200, 0, true, false, false));
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
        float m = 1.0f;
        if (activePerks.contains("TREASURE_HUNTER")) {
            m *= 1.25f;
        }
        if (activePerks.contains("MINERS_ALLIANCE")) {
            m *= 1.15f;
        }
        return m;
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
        if (activePerks.contains("PROSPERITY_SURGE")) return 1.5f;
        return 1.0f;
    }

    public static float getXpMultiplier() {
        float multiplier = 1.0f;
        if (activePerks.contains("XP_TAX_CUTS")) {
            multiplier += 0.25f;
        }
        if (activePerks.contains("ACADEMY_GRANTS")) {
            multiplier += 0.10f;
        }
        if (activePerks.contains("MINOR_CORRUPTION")) {
            multiplier -= 0.10f;
        }
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
        if (activePerks.contains("MONSTER_CONTROL")) {
            multiplier -= 0.3f;
        }
        if (activePerks.contains("CRIME_WAVE")) {
            multiplier += 0.3f;
        }
        if (activePerks.contains("WILDLIFE_PROTECTION")) {
            multiplier -= 0.2f;
        }
        if (activePerks.contains("RIOT_SEASON")) {
            multiplier += 0.15f;
        }
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
        float m = 1.0f;
        if (activePerks.contains("ECONOMIC_COLLAPSE")) {
            m *= 1.5f;
        }
        if (activePerks.contains("TARIFF_SPIKE")) {
            m *= 1.15f;
        }
        return m;
    }

    public static float getEnchantCostMultiplier() {
        float m = 1.0f;
        if (activePerks.contains("ARCANE_DECAY")) {
            m *= 1.5f;
        }
        if (activePerks.contains("ARCANE_LEVY")) {
            m *= 1.25f;
        }
        return m;
    }

    public static boolean shouldPreventPhantoms() {
        return activePerks.contains("NIGHT_OWL_POLICY");
    }

    public static float getPatrolSpawnMultiplier() {
        if (activePerks.contains("REDUCED_PATROLS")) return 2.0f;
        return 1.0f;
    }

    // ── Bounty perk helpers (used by SlayerManager / BossAbilityManager) ──

    /** Returns the bounty XP multiplier from active perks. */
    public static float getBountyXpMultiplier() {
        float m = 1.0f;
        if (activePerks.contains("TRIPLE_BOUNTY_XP")) m += 2.0f;  // +200% = 3x total
        if (activePerks.contains("BOUNTY_HUNTER_SURGE")) m += 0.25f;
        // BOUNTY_DROUGHT is a legacy perk id; no longer registered but kept for old save compatibility
        if (activePerks.contains("BOUNTY_DROUGHT"))      m -= 0.50f;
        return Math.max(0.1f, m);
    }

    /** Returns the boss health multiplier from active perks (applied when boss spawns). */
    public static float getBountyBossHealthMultiplier() {
        float m = 1.0f;
        if (activePerks.contains("WEAKENED_QUARRY")) m -= 0.20f;
        if (activePerks.contains("HARDENED_QUARRY")) m += 0.30f;
        if (activePerks.contains("WEAKENED_PREY"))   m += 0.50f; // legacy compat
        return Math.max(0.1f, m);
    }

    /** Returns the boss drop quantity multiplier from active perks. */
    public static float getBountyDropMultiplier() {
        float m = 1.0f;
        if (activePerks.contains("SLAYER_CONTRACTS") || activePerks.contains("SLAYERS_FORTUNE")) {
            m *= 1.50f;
        }
        if (activePerks.contains("BOUNTY_SUBSIDY_II")) {
            m *= 1.15f;
        }
        return m;
    }

    /** Applied to bounty boss coin payouts before tax. */
    public static float getBountyCoinPayoutMultiplier() {
        if (activePerks.contains("SLAYER_TOLL")) {
            return 0.92f;
        }
        return 1.0f;
    }

    /** Returns the damage multiplier applied when a player attacks a bounty boss. */
    public static float getBountyBossDamageMultiplier() {
        float m = 1.0f;
        if (activePerks.contains("HUNTERS_INSTINCT"))     m += 0.15f;
        if (activePerks.contains("BOUNTY_HUNTERS_ZEAL"))  m += 0.25f; // legacy compat
        return m;
    }

    /**
     * Applies the BOUNTY_TAX perk by removing 10% of the given coin reward before giving it.
     * Returns the amount actually given to the player (after tax deduction).
     */
    public static int applyBountyTax(int coinReward) {
        if (!activePerks.contains("BOUNTY_TAX")) return coinReward;
        int tax = (int)(coinReward * 0.10);
        if (tax > 0) {
            PoliticalPartyManager.distributeBountyTaxCoins(tax);
        }
        return coinReward - tax;
    }

    // ── Temporary Perk System for Government Spending ──
    
    private static final Map<String, Long> temporaryPerkExpirations = new HashMap<>();

    /**
     * Activates a temporary perk that will automatically expire after the given duration (in ms).
     * Used for government spending programs.
     */
    public static void activateTemporaryPerk(String perkId, long durationMs) {
        if (!activePerks.contains(perkId)) {
            activePerks.add(perkId);
        }
        temporaryPerkExpirations.put(perkId, System.currentTimeMillis() + durationMs);
        
        // Apply to all online players
        if (PoliticalServer.server != null) {
            for (ServerPlayer player : PoliticalServer.server.getPlayerList().getPlayers()) {
                applyActivePerks(player);
            }
        }
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Checks and removes expired temporary perks. Called from server tick.
     */
    public static void tickTemporaryPerks() {
        long now = System.currentTimeMillis();
        boolean changed = false;
        
        Iterator<Map.Entry<String, Long>> it = temporaryPerkExpirations.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Long> entry = it.next();
            if (now >= entry.getValue()) {
                String perkId = entry.getKey();
                activePerks.remove(perkId);
                it.remove();
                changed = true;
            }
        }
        
        if (changed && PoliticalServer.server != null) {
            for (ServerPlayer player : PoliticalServer.server.getPlayerList().getPlayers()) {
                applyActivePerks(player);
            }
            DataManager.save(PoliticalServer.server);
        }
    }

    /**
     * Checks if a temporary perk is active and returns remaining time in ms, or -1 if not temporary.
     */
    public static long getTemporaryPerkRemainingTime(String perkId) {
        Long expiry = temporaryPerkExpirations.get(perkId);
        if (expiry == null) return -1;
        return Math.max(0, expiry - System.currentTimeMillis());
    }

    /**
     * Loads temporary perk expirations from save data.
     */
    public static void loadTemporaryPerks(Map<String, Long> expirations) {
        temporaryPerkExpirations.clear();
        if (expirations != null) {
            temporaryPerkExpirations.putAll(expirations);
        }
    }

    /**
     * Gets temporary perk expirations for saving.
     */
    public static Map<String, Long> getTemporaryPerkExpirations() {
        return new HashMap<>(temporaryPerkExpirations);
    }
}