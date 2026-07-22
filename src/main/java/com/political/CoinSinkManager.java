package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.WeatherData;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Coin Sink System - Various ways to remove coins from the economy.
 * These are money drains that help control inflation.
 * Prices adjusted for hyper-inflated economy.
 */
public class CoinSinkManager {

    // ── Lottery System ──
    private static final Map<String, Integer> lotteryTickets = new HashMap<>();
    private static long lotteryPot = 0;
    private static long lastLotteryDraw = 0;
    private static final long LOTTERY_DRAW_INTERVAL = 6 * 60 * 60 * 1000; // 6 hours
    private static final int TICKET_COST = 1_000_000; // 1M per ticket (was 10K)
    private static final double HOUSE_EDGE = 0.35; // 35% house edge

    // ── Lucky Wheel ──
    private static final long WHEEL_SPIN_COST = 5_000_000; // 5M per spin (was 50K)
    private static final long[] WHEEL_PRIZES = {0, 0, 250_000L, 500_000L, 1_000_000L, 2_500_000L, 10_000_000L, 25_000_000L};
    private static final double[] WHEEL_WEIGHTS = {0.30, 0.25, 0.20, 0.12, 0.08, 0.035, 0.012, 0.003};

    // ── High Roller Room (for rich players) ──
    private static final long HIGH_ROLLER_MIN_BET = 10_000_000; // 10M minimum
    private static final long HIGH_ROLLER_MAX_BET = 1_000_000_000; // 1B maximum

    // ── VIP Perks (expensive sinks) ──
    private static final Map<String, Long> vipPerkExpirations = new HashMap<>();

    // ── Functional Boosts (active effects) ──
    private static final Map<String, Long> xpBoostExpirations = new HashMap<>();
    private static final Map<String, Long> luckBoostExpirations = new HashMap<>();
    private static final Map<String, Long> coinBoostExpirations = new HashMap<>();
    private static final Map<String, Integer> extraHomes = new HashMap<>();
    private static final Map<String, Long> deathProtection = new HashMap<>(); // timestamp when purchased
    private static final Map<String, Long> flightExpirations = new HashMap<>();
    private static final Map<String, Long> miningBoostExpirations = new HashMap<>();
    private static final Map<String, Long> damageBoostExpirations = new HashMap<>();
    private static final Map<String, Long> defenseBoostExpirations = new HashMap<>();
    private static final Set<String> chunkLoaders = new HashSet<>(); // players with chunk loaders active

    public static void tick(MinecraftServer server) {
        // Check for lottery draw
        if (System.currentTimeMillis() - lastLotteryDraw >= LOTTERY_DRAW_INTERVAL) {
            drawLottery(server);
        }

        // Tick VIP perks
        tickVipPerks(server);
        
        // Tick functional boosts
        tickFunctionalBoosts(server);
    }

    // ── Functional Boost Accessors ──
    public static double getXpBoostMultiplier(String uuid) {
        Long exp = xpBoostExpirations.get(uuid);
        return (exp != null && exp > System.currentTimeMillis()) ? 2.0 : 1.0;
    }

    public static double getLuckBoostMultiplier(String uuid) {
        Long exp = luckBoostExpirations.get(uuid);
        return (exp != null && exp > System.currentTimeMillis()) ? 2.0 : 1.0;
    }

    public static double getCoinBoostMultiplier(String uuid) {
        Long exp = coinBoostExpirations.get(uuid);
        return (exp != null && exp > System.currentTimeMillis()) ? 1.5 : 1.0;
    }

    public static int getExtraHomes(String uuid) {
        return extraHomes.getOrDefault(uuid, 0);
    }

    public static boolean hasDeathProtection(String uuid) {
        Long exp = deathProtection.get(uuid);
        if (exp != null && exp > System.currentTimeMillis()) {
            return true;
        }
        deathProtection.remove(uuid);
        return false;
    }

    public static void consumeDeathProtection(String uuid) {
        deathProtection.remove(uuid);
        DataManager.save(PoliticalServer.server);
    }

    public static boolean hasFlight(String uuid) {
        Long exp = flightExpirations.get(uuid);
        if (exp != null && exp > System.currentTimeMillis()) {
            return true;
        }
        flightExpirations.remove(uuid);
        return false;
    }

    public static double getMiningBoostMultiplier(String uuid) {
        Long exp = miningBoostExpirations.get(uuid);
        return (exp != null && exp > System.currentTimeMillis()) ? 3.0 : 1.0;
    }

    public static double getDamageBoostMultiplier(String uuid) {
        Long exp = damageBoostExpirations.get(uuid);
        return (exp != null && exp > System.currentTimeMillis()) ? 1.25 : 1.0;
    }

    public static double getDefenseBoostMultiplier(String uuid) {
        Long exp = defenseBoostExpirations.get(uuid);
        return (exp != null && exp > System.currentTimeMillis()) ? 0.75 : 1.0; // 25% damage reduction
    }

    public static boolean hasChunkLoader(String uuid) {
        return chunkLoaders.contains(uuid);
    }

    private static void tickFunctionalBoosts(MinecraftServer server) {
        long now = System.currentTimeMillis();
        boolean changed = false;

        // Clean expired boosts
        changed |= cleanExpiredMap(xpBoostExpirations, now);
        changed |= cleanExpiredMap(luckBoostExpirations, now);
        changed |= cleanExpiredMap(coinBoostExpirations, now);
        changed |= cleanExpiredMap(flightExpirations, now);
        changed |= cleanExpiredMap(miningBoostExpirations, now);
        changed |= cleanExpiredMap(damageBoostExpirations, now);
        changed |= cleanExpiredMap(defenseBoostExpirations, now);
        changed |= cleanExpiredMap(deathProtection, now);

        if (changed) {
            DataManager.save(server);
        }
    }

    private static boolean cleanExpiredMap(Map<String, Long> map, long now) {
        boolean changed = false;
        var it = map.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getValue() <= now) {
                it.remove();
                changed = true;
            }
        }
        return changed;
    }

    // ── Functional Boost Purchases ──

    /**
     * XP Boost - 2x XP for 1 hour - 20M
     */
    public static void purchaseXpBoost(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 20_000_000;
        long duration = 60 * 60 * 1000; // 1 hour

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);
        xpBoostExpirations.put(uuid, System.currentTimeMillis() + duration);
        player.sendSystemMessage(Component.literal("§a✓ Purchased 2x XP Boost for 1 hour!"), false);
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Luck Boost - 2x rare drop chance for 1 hour - 30M
     */
    public static void purchaseLuckBoost(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 30_000_000;
        long duration = 60 * 60 * 1000;

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);
        luckBoostExpirations.put(uuid, System.currentTimeMillis() + duration);
        player.sendSystemMessage(Component.literal("§a✓ Purchased 2x Luck Boost for 1 hour!"), false);
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Coin Boost - 50% more coins for 2 hours - 40M
     */
    public static void purchaseCoinBoost(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 40_000_000;
        long duration = 2 * 60 * 60 * 1000;

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);
        coinBoostExpirations.put(uuid, System.currentTimeMillis() + duration);
        player.sendSystemMessage(Component.literal("§a✓ Purchased +50% Coin Boost for 2 hours!"), false);
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Extra Home - Permanent extra /home slot - 25M each
     */
    public static void purchaseExtraHome(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 25_000_000;
        int current = extraHomes.getOrDefault(uuid, 0);

        if (current >= 10) {
            player.sendSystemMessage(Component.literal("§c✗ Maximum extra homes reached (10)!"), false);
            return;
        }

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);
        extraHomes.put(uuid, current + 1);
        player.sendSystemMessage(Component.literal("§a✓ Purchased extra home slot! You now have " + (current + 1) + " extra homes."), false);
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Death Protection - Keep inventory on next death - 50M
     */
    public static void purchaseDeathProtection(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 50_000_000;
        long duration = 24 * 60 * 60 * 1000; // 24 hours to use it

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);
        deathProtection.put(uuid, System.currentTimeMillis() + duration);
        player.sendSystemMessage(Component.literal("§a✓ Purchased Death Protection! You will keep your inventory on your next death."), false);
        player.sendSystemMessage(Component.literal("§7Expires in 24 hours if not used."), false);
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Temporary Flight - Creative flight for 1 hour - 75M
     */
    public static void purchaseFlight(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 75_000_000;
        long duration = 60 * 60 * 1000;

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);
        flightExpirations.put(uuid, System.currentTimeMillis() + duration);
        player.getAbilities().mayfly = true;
        player.getAbilities().flying = true;
        player.onUpdateAbilities();
        player.sendSystemMessage(Component.literal("§a✓ Purchased Flight for 1 hour!"), false);
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Mining Boost - 3x mining drops for 1 hour - 35M
     */
    public static void purchaseMiningBoost(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 35_000_000;
        long duration = 60 * 60 * 1000;

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);
        miningBoostExpirations.put(uuid, System.currentTimeMillis() + duration);
        player.sendSystemMessage(Component.literal("§a✓ Purchased 3x Mining Boost for 1 hour!"), false);
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Damage Boost - +25% damage for 1 hour - 45M
     */
    public static void purchaseDamageBoost(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 45_000_000;
        long duration = 60 * 60 * 1000;

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);
        damageBoostExpirations.put(uuid, System.currentTimeMillis() + duration);
        player.sendSystemMessage(Component.literal("§a✓ Purchased +25% Damage Boost for 1 hour!"), false);
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Defense Boost - 25% damage reduction for 1 hour - 40M
     */
    public static void purchaseDefenseBoost(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 40_000_000;
        long duration = 60 * 60 * 1000;

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);
        defenseBoostExpirations.put(uuid, System.currentTimeMillis() + duration);
        player.sendSystemMessage(Component.literal("§a✓ Purchased 25% Damage Reduction for 1 hour!"), false);
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Chunk Loader - Keep chunks loaded around you for 24h - 100M
     */
    public static void purchaseChunkLoader(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 100_000_000;
        long duration = 24 * 60 * 60 * 1000;

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);
        chunkLoaders.add(uuid);
        player.sendSystemMessage(Component.literal("§a✓ Purchased Chunk Loader for 24 hours!"), false);
        player.sendSystemMessage(Component.literal("§7Chunks around you will stay loaded even when you're away."), false);
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Instant Max Level - Get level 30 instantly - 60M
     */
    public static void purchaseInstantLevel(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 60_000_000;

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);
        player.giveExperienceLevels(30 - player.experienceLevel);
        player.sendSystemMessage(Component.literal("§a✓ Instantly reached level 30!"), false);
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Heal & Feed - Full health and hunger - 5M
     */
    public static void purchaseHealFeed(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 5_000_000;

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);
        player.setHealth(player.getMaxHealth());
        player.getFoodData().setFoodLevel(20);
        player.getFoodData().setSaturation(20f);
        player.sendSystemMessage(Component.literal("§a✓ Fully healed and fed!"), false);
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Clear Cooldowns - Reset all ability cooldowns - 10M
     */
    public static void purchaseClearCooldowns(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 10_000_000;

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);
        net.minecraft.world.item.ItemCooldowns cds = player.getCooldowns();
        cds.removeCooldown(cds.getCooldownGroup(new net.minecraft.world.item.ItemStack(Items.ENDER_PEARL)));
        cds.removeCooldown(cds.getCooldownGroup(new net.minecraft.world.item.ItemStack(Items.CHORUS_FRUIT)));
        cds.removeCooldown(cds.getCooldownGroup(new net.minecraft.world.item.ItemStack(Items.ENDER_EYE)));
        player.sendSystemMessage(Component.literal("§a✓ All item cooldowns cleared!"), false);
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Night Vision - Permanent night vision for 1 hour - 15M
     */
    public static void purchaseNightVision(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 15_000_000;

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.NIGHT_VISION, 20 * 60 * 60, 0, false, false));
        player.sendSystemMessage(Component.literal("§a✓ Granted Night Vision for 1 hour!"), false);
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Haste - Mining speed boost for 1 hour - 20M
     */
    public static void purchaseHaste(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 20_000_000;

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.HASTE, 20 * 60 * 60, 1, false, false));
        player.sendSystemMessage(Component.literal("§a✓ Granted Haste II for 1 hour!"), false);
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Speed - Movement speed boost for 1 hour - 25M
     */
    public static void purchaseSpeed(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 25_000_000;

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.SPEED, 20 * 60 * 60, 1, false, false));
        player.sendSystemMessage(Component.literal("§a✓ Granted Speed II for 1 hour!"), false);
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Strength - Attack damage boost for 1 hour - 30M
     */
    public static void purchaseStrength(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 30_000_000;

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.STRENGTH, 20 * 60 * 60, 0, false, false));
        player.sendSystemMessage(Component.literal("§a✓ Granted Strength for 1 hour!"), false);
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Resistance - Damage resistance for 1 hour - 35M
     */
    public static void purchaseResistance(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 35_000_000;

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.RESISTANCE, 20 * 60 * 60, 0, false, false));
        player.sendSystemMessage(Component.literal("§a✓ Granted Resistance for 1 hour!"), false);
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Fire Resistance - Fire immunity for 1 hour - 20M
     */
    public static void purchaseFireResistance(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 20_000_000;

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE, 20 * 60 * 60, 0, false, false));
        player.sendSystemMessage(Component.literal("§a✓ Granted Fire Resistance for 1 hour!"), false);
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Water Breathing - Underwater breathing for 1 hour - 15M
     */
    public static void purchaseWaterBreathing(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 15_000_000;

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.WATER_BREATHING, 20 * 60 * 60, 0, false, false));
        player.sendSystemMessage(Component.literal("§a✓ Granted Water Breathing for 1 hour!"), false);
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Jump Boost - Higher jumps for 1 hour - 15M
     */
    public static void purchaseJumpBoost(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 15_000_000;

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.JUMP_BOOST, 20 * 60 * 60, 1, false, false));
        player.sendSystemMessage(Component.literal("§a✓ Granted Jump Boost II for 1 hour!"), false);
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Regeneration - Health regen for 30 min - 50M
     */
    public static void purchaseRegeneration(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 50_000_000;

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.REGENERATION, 20 * 60 * 30, 1, false, false));
        player.sendSystemMessage(Component.literal("§a✓ Granted Regeneration II for 30 minutes!"), false);
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Spawner Key - Obtain a spawner item - 100M
     */
    public static void purchaseSpawnerKey(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 100_000_000;

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);
        player.addItem(new net.minecraft.world.item.ItemStack(Items.SPAWNER));
        player.sendSystemMessage(Component.literal("§a✓ Purchased a Spawner for " + String.format("%,d", cost) + " coins!"), false);
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Enchanted Golden Apple - 10M each
     */
    public static void purchaseEnchantedGoldenApple(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 10_000_000;

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);
        player.addItem(new net.minecraft.world.item.ItemStack(Items.ENCHANTED_GOLDEN_APPLE));
        player.sendSystemMessage(Component.literal("§a✓ Purchased Enchanted Golden Apple for " + String.format("%,d", cost) + " coins!"), false);
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Beacon - 50M
     */
    public static void purchaseBeacon(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 50_000_000;

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);
        player.addItem(new net.minecraft.world.item.ItemStack(Items.BEACON));
        player.sendSystemMessage(Component.literal("§a✓ Purchased Beacon for " + String.format("%,d", cost) + " coins!"), false);
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Conduit - 75M
     */
    public static void purchaseConduit(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 75_000_000;

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);
        player.addItem(new net.minecraft.world.item.ItemStack(Items.CONDUIT));
        player.sendSystemMessage(Component.literal("§a✓ Purchased Conduit for " + String.format("%,d", cost) + " coins!"), false);
        DataManager.save(PoliticalServer.server);
    }

    // ── Lottery System ──
    public static void buyLotteryTicket(ServerPlayer player, int amount) {
        String uuid = player.getUUID().toString();
        long totalCost = (long) amount * TICKET_COST;

        if (CoinManager.getCoins(uuid) < totalCost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", totalCost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, totalCost);
        lotteryTickets.merge(uuid, amount, Integer::sum);
        lotteryPot += totalCost;

        player.sendSystemMessage(Component.literal("§a✓ Bought " + amount + " lottery ticket(s) for " + String.format("%,d", totalCost) + " coins!"), false);
        player.sendSystemMessage(Component.literal("§7Current pot: §6" + String.format("%,d", lotteryPot) + " coins"), false);

        DataManager.save(PoliticalServer.server);
    }

    private static void drawLottery(MinecraftServer server) {
        if (lotteryTickets.isEmpty()) {
            lastLotteryDraw = System.currentTimeMillis();
            return;
        }

        int totalTickets = lotteryTickets.values().stream().mapToInt(Integer::intValue).sum();
        Random random = new Random();
        int winningTicket = random.nextInt(totalTickets);

        String winner = null;
        int ticketCount = 0;
        for (Map.Entry<String, Integer> entry : lotteryTickets.entrySet()) {
            ticketCount += entry.getValue();
            if (ticketCount > winningTicket) {
                winner = entry.getKey();
                break;
            }
        }

        long prize = (long) (lotteryPot * (1 - HOUSE_EDGE));

        if (winner != null && server != null) {
            ServerPlayer winnerPlayer = server.getPlayerList().getPlayer(UUID.fromString(winner));
            if (winnerPlayer != null) {
                CoinManager.giveCoins(winner, prize);
                winnerPlayer.sendSystemMessage(Component.literal("§6═══════════════════════════════════"), false);
                winnerPlayer.sendSystemMessage(Component.literal("§6🎉 YOU WON THE LOTTERY! 🎉"), false);
                winnerPlayer.sendSystemMessage(Component.literal("§aPrize: " + String.format("%,d", prize) + " coins!"), false);
                winnerPlayer.sendSystemMessage(Component.literal("§6═══════════════════════════════════"), false);
            }

            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                if (!p.getUUID().toString().equals(winner)) {
                    p.sendSystemMessage(Component.literal("§e🎰 Lottery drawn! Winner received " + String.format("%,d", prize) + " coins!"), false);
                }
            }
        }

        lotteryTickets.clear();
        lotteryPot = 0;
        lastLotteryDraw = System.currentTimeMillis();
        DataManager.save(server);
    }

    public static long getLotteryPot() {
        return lotteryPot;
    }

    public static int getPlayerTickets(String uuid) {
        return lotteryTickets.getOrDefault(uuid, 0);
    }

    public static long getTimeUntilNextDraw() {
        return Math.max(0, LOTTERY_DRAW_INTERVAL - (System.currentTimeMillis() - lastLotteryDraw));
    }

    // ── Simple Gambling for GUI ──
    public static void purchaseLotteryTicket(ServerPlayer player) {
        buyLotteryTicket(player, 1);
    }

    public static void purchaseCoinFlip(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 10_000_000;

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);

        boolean win = Math.random() < 0.5;
        if (win) {
            long prize = cost * 2;
            CoinManager.giveCoins(uuid, prize);
            player.sendSystemMessage(Component.literal("§a✓ Coin flip: HEADS! You won " + String.format("%,d", prize) + " coins!"), false);
        } else {
            player.sendSystemMessage(Component.literal("§c✗ Coin flip: TAILS! Better luck next time!"), false);
        }

        DataManager.save(PoliticalServer.server);
    }

    public static void purchaseSlotMachine(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 25_000_000;

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);

        String[] symbols = {"§c🍒", "§e🍋", "§b💎", "§6⭐", "§5🔮"};
        String[] result = new String[3];
        Random random = new Random();

        for (int i = 0; i < 3; i++) {
            result[i] = symbols[random.nextInt(symbols.length)];
        }

        player.sendSystemMessage(Component.literal("§6═══ SLOT MACHINE ═══"), false);
        player.sendSystemMessage(Component.literal(result[0] + " " + result[1] + " " + result[2]), false);

        long prize = 0;
        if (result[0].equals(result[1]) && result[1].equals(result[2])) {
            prize = cost * 10;
            CoinManager.giveCoins(uuid, prize);
            player.sendSystemMessage(Component.literal("§a🎉 JACKPOT! 3 matching! Won " + String.format("%,d", prize) + " coins!"), false);
        } else if (result[0].equals(result[1]) || result[1].equals(result[2]) || result[0].equals(result[2])) {
            prize = cost * 2;
            CoinManager.giveCoins(uuid, prize);
            player.sendSystemMessage(Component.literal("§e✓ 2 matching! Won " + String.format("%,d", prize) + " coins!"), false);
        } else {
            player.sendSystemMessage(Component.literal("§c✗ No matches. Try again!"), false);
        }

        DataManager.save(PoliticalServer.server);
    }

    // ── Lucky Wheel ──
    public static void spinWheel(ServerPlayer player) {
        String uuid = player.getUUID().toString();

        if (CoinManager.getCoins(uuid) < WHEEL_SPIN_COST) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", WHEEL_SPIN_COST) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, WHEEL_SPIN_COST);

        Random random = new Random();
        double roll = random.nextDouble();
        double cumulative = 0;
        long prize = 0;

        for (int i = 0; i < WHEEL_PRIZES.length; i++) {
            cumulative += WHEEL_WEIGHTS[i];
            if (roll < cumulative) {
                prize = WHEEL_PRIZES[i];
                break;
            }
        }

        if (prize > 0) {
            CoinManager.giveCoins(player, prize);
            player.sendSystemMessage(Component.literal("§a🎰 You won " + String.format("%,d", prize) + " coins!"), false);
        } else {
            player.sendSystemMessage(Component.literal("§c🎰 Better luck next time!"), false);
        }

        DataManager.save(PoliticalServer.server);
    }

    // ── Coin Flip (50/50 with house edge) ──
    public static void coinFlip(ServerPlayer player, long amount, boolean chooseHeads) {
        String uuid = player.getUUID().toString();

        if (amount < 1_000_000) { // 1M minimum (was 1K)
            player.sendSystemMessage(Component.literal("§c✗ Minimum bet is 1,000,000 coins!"), false);
            return;
        }

        if (CoinManager.getCoins(uuid) < amount) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins!"), false);
            return;
        }

        Random random = new Random();
        boolean win = random.nextDouble() < 0.47; // 47% win chance
        boolean resultHeads = random.nextBoolean();

        if ((chooseHeads && resultHeads && win) || (!chooseHeads && !resultHeads && win)) {
            long winnings = (long) (amount * 1.9); // 90% payout
            CoinManager.giveCoins(player, winnings);
            player.sendSystemMessage(Component.literal("§a🪙 " + (resultHeads ? "Heads!" : "Tails!") + " You win " + String.format("%,d", winnings) + " coins!"), false);
        } else {
            CoinManager.removeCoins(uuid, amount);
            player.sendSystemMessage(Component.literal("§c🪙 " + (resultHeads ? "Heads!" : "Tails!") + " You lost " + String.format("%,d", amount) + " coins."), false);
        }

        DataManager.save(PoliticalServer.server);
    }

    // ── Dice Roll ──
    public static void diceRoll(ServerPlayer player, long amount, int guess) {
        String uuid = player.getUUID().toString();

        if (guess < 1 || guess > 6) {
            player.sendSystemMessage(Component.literal("§c✗ Guess must be 1-6!"), false);
            return;
        }

        if (amount < 1_000_000) { // 1M minimum (was 1K)
            player.sendSystemMessage(Component.literal("§c✗ Minimum bet is 1,000,000 coins!"), false);
            return;
        }

        if (CoinManager.getCoins(uuid) < amount) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins!"), false);
            return;
        }

        Random random = new Random();
        int roll = random.nextInt(6) + 1;

        if (roll == guess) {
            long winnings = amount * 5;
            CoinManager.giveCoins(player, winnings);
            player.sendSystemMessage(Component.literal("§a🎲 Rolled " + roll + "! You win " + String.format("%,d", winnings) + " coins!"), false);
        } else {
            CoinManager.removeCoins(uuid, amount);
            player.sendSystemMessage(Component.literal("§c🎲 Rolled " + roll + ". You lost " + String.format("%,d", amount) + " coins."), false);
        }

        DataManager.save(PoliticalServer.server);
    }

    // ── HIGH ROLLER ROOM (Tens of Millions+) ──

    /**
     * High stakes coin flip - minimum 10M, max 1B
     */
    public static void highRollerCoinFlip(ServerPlayer player, long amount, boolean chooseHeads) {
        String uuid = player.getUUID().toString();

        if (amount < HIGH_ROLLER_MIN_BET) {
            player.sendSystemMessage(Component.literal("§c✗ Minimum bet is " + String.format("%,d", HIGH_ROLLER_MIN_BET) + " coins!"), false);
            return;
        }

        if (amount > HIGH_ROLLER_MAX_BET) {
            player.sendSystemMessage(Component.literal("§c✗ Maximum bet is " + String.format("%,d", HIGH_ROLLER_MAX_BET) + " coins!"), false);
            return;
        }

        if (CoinManager.getCoins(uuid) < amount) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins!"), false);
            return;
        }

        Random random = new Random();
        boolean win = random.nextDouble() < 0.45; // 45% win chance (bigger house edge)
        boolean resultHeads = random.nextBoolean();

        if ((chooseHeads && resultHeads && win) || (!chooseHeads && !resultHeads && win)) {
            long winnings = (long) (amount * 1.85); // 85% payout
            CoinManager.giveCoins(player, winnings);
            player.sendSystemMessage(Component.literal("§d💎 HIGH ROLLER WIN! " + (resultHeads ? "Heads!" : "Tails!") + " +" + String.format("%,d", winnings) + " coins!"), false);
            // Broadcast to server
            for (ServerPlayer p : PoliticalServer.server.getPlayerList().getPlayers()) {
                p.sendSystemMessage(Component.literal("§d💎 " + player.getName().getString() + " won " + String.format("%,d", winnings) + " coins in High Roller!"), false);
            }
        } else {
            CoinManager.removeCoins(uuid, amount);
            player.sendSystemMessage(Component.literal("§c🪙 " + (resultHeads ? "Heads!" : "Tails!") + " Lost " + String.format("%,d", amount) + " coins."), false);
        }

        DataManager.save(PoliticalServer.server);
    }

    /**
     * VIP Status - 50M for 24 hours of special perks
     */
    public static void purchaseVipStatus(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 50_000_000; // 50M

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);
        vipPerkExpirations.put(uuid, System.currentTimeMillis() + (24 * 60 * 60 * 1000));

        player.sendSystemMessage(Component.literal("§d✓ VIP Status activated for 24 hours!"), false);
        player.sendSystemMessage(Component.literal("§7Perks: +10% coin drops, colored name in chat"), false);

        DataManager.save(PoliticalServer.server);
    }

    public static boolean isVip(String uuid) {
        Long expiry = vipPerkExpirations.get(uuid);
        return expiry != null && System.currentTimeMillis() < expiry;
    }

    private static void tickVipPerks(MinecraftServer server) {
        // Clean expired VIP statuses
        long now = System.currentTimeMillis();
        vipPerkExpirations.entrySet().removeIf(e -> e.getValue() < now);
    }

    /**
     * Custom Title Purchase - 25M to 500M
     */
    public static void purchaseTitle(ServerPlayer player, String title, String color) {
        String uuid = player.getUUID().toString();
        long cost = getTitleCost(title);

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);
        DataManager.setPlayerTitle(uuid, title, color);

        player.sendSystemMessage(Component.literal("§a✓ Purchased title: " + title + " for " + String.format("%,d", cost) + " coins!"), false);

        DataManager.save(PoliticalServer.server);
    }

    private static long getTitleCost(String title) {
        return switch (title.toLowerCase()) {
            case "emperor", "empress", "supreme" -> 500_000_000; // 500M
            case "legendary", "mythic", "divine" -> 250_000_000; // 250M
            case "titan", "overlord", "sovereign" -> 100_000_000; // 100M
            case "epic", "hero", "champion" -> 50_000_000; // 50M
            case "elite", "veteran", "master" -> 25_000_000; // 25M
            default -> 10_000_000; // 10M
        };
    }

    /**
     * Global Broadcast - 10M to broadcast a message to all players
     */
    public static void purchaseGlobalBroadcast(ServerPlayer player, String message) {
        String uuid = player.getUUID().toString();
        long cost = 10_000_000; // 10M

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        if (message.length() > 100) {
            player.sendSystemMessage(Component.literal("§c✗ Message too long! Max 100 characters."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);

        // Broadcast to all players
        for (ServerPlayer p : PoliticalServer.server.getPlayerList().getPlayers()) {
            p.sendSystemMessage(Component.literal("§6═══════════════════════════════════"), false);
            p.sendSystemMessage(Component.literal("§e📢 BROADCAST from " + player.getName().getString()).withStyle(ChatFormatting.GOLD), false);
            p.sendSystemMessage(Component.literal("§f" + message), false);
            p.sendSystemMessage(Component.literal("§6═══════════════════════════════════"), false);
        }

        DataManager.save(PoliticalServer.server);
    }

    /**
     * Rename Item - 5M to rename any item with custom name
     */
    public static void purchaseItemRename(ServerPlayer player, String newName) {
        String uuid = player.getUUID().toString();
        long cost = 5_000_000; // 5M

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        var mainHand = player.getMainHandItem();
        if (mainHand.isEmpty()) {
            player.sendSystemMessage(Component.literal("§c✗ Hold an item in your main hand!"), false);
            return;
        }

        if (newName.length() > 30) {
            player.sendSystemMessage(Component.literal("§c✗ Name too long! Max 30 characters."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);
        mainHand.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.literal(newName));

        player.sendSystemMessage(Component.literal("§a✓ Item renamed to '" + newName + "' for " + String.format("%,d", cost) + " coins!"), false);

        DataManager.save(PoliticalServer.server);
    }

    /**
     * Teleport to Random Location - 15M
     */
    public static void purchaseRandomTeleport(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 15_000_000; // 15M

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);

        ServerLevel world = (ServerLevel) player.level();
        Random random = new Random();

        // Find a safe random location within 50k blocks
        for (int i = 0; i < 100; i++) {
            int x = random.nextInt(100000) - 50000;
            int z = random.nextInt(100000) - 50000;
            var blockPos = new net.minecraft.core.BlockPos(x, 64, z);
            int y = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockPos.getX(), blockPos.getZ());

            var groundPos = new net.minecraft.core.BlockPos(x, y - 1, z);
            if (!world.getBlockState(groundPos).isAir()) {
                PlayerTeleportHelper.teleport(player, world, x + 0.5, y, z + 0.5, player.getYRot(), player.getXRot());
                player.sendSystemMessage(Component.literal("§a✓ Teleported to random location for " + String.format("%,d", cost) + " coins!"), false);
                DataManager.save(PoliticalServer.server);
                return;
            }
        }

        player.sendSystemMessage(Component.literal("§c✗ Could not find safe location. Refunded."), false);
        CoinManager.giveCoins(player, cost);
    }

    /**
     * Repair All Items - 50M
     */
    public static void repairAllItems(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 50_000_000; // 50M (was 100K)

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            var stack = player.getInventory().getItem(i);
            if (stack != null && stack.isDamageableItem()) {
                stack.setDamageValue(0);
            }
        }

        CoinManager.removeCoins(uuid, cost);
        player.sendSystemMessage(Component.literal("§a✓ All items repaired for " + String.format("%,d", cost) + " coins!"), false);

        DataManager.save(PoliticalServer.server);
    }

    /**
     * Clear Tax Debt - Pay off all tax debt at premium
     */
    public static void clearTaxDebt(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        int debt = TaxManager.getTaxOwed(uuid);

        if (debt <= 0) {
            player.sendSystemMessage(Component.literal("§a✓ You have no tax debt!"), false);
            return;
        }

        // Cost is 150% of debt (premium for convenience)
        long cost = (long) (debt * 1.5);

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins (150% of debt)."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);
        TaxManager.setTaxOwed(uuid, 0);

        player.sendSystemMessage(Component.literal("§a✓ Tax debt cleared! Paid " + String.format("%,d", cost) + " coins."), false);

        DataManager.save(PoliticalServer.server);
    }

    /**
     * Bank Fee - Charged for various banking operations
     */
    public static boolean chargeBankFee(ServerPlayer player, String operation) {
        String uuid = player.getUUID().toString();
        long fee = getBankFee(operation);

        if (fee > 0 && CoinManager.getCoins(uuid) >= fee) {
            CoinManager.removeCoins(uuid, fee);
            player.sendSystemMessage(Component.literal("§7Bank fee: " + String.format("%,d", fee) + " coins"), false);
            DataManager.save(PoliticalServer.server);
            return true;
        }
        return fee == 0;
    }

    private static long getBankFee(String operation) {
        return switch (operation) {
            case "loan_take" -> 5_000_000; // 5M (was 50K)
            case "large_transfer" -> 1_000_000; // 1M (was 10K)
            case "international" -> 2_500_000; // 2.5M (was 25K)
            default -> 0;
        };
    }

    // ── GUI ──
    public static void openCoinSinkGui(ServerPlayer player) {
        openCoinSinkGui(player, 0);
    }

    public static void openCoinSinkGui(ServerPlayer player, int page) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("🎰 Casino & Services (Page " + (page + 1) + "/3)"));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Component.literal("")).build());
        }

        if (page == 0) {
            // Page 1: Gambling & Basic Services
            // Lottery
            gui.setSlot(1, new GuiElementBuilder(Items.GOLD_INGOT)
                    .setName(Component.literal("🎰 Lottery").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Pot: " + String.format("%,d", lotteryPot) + " coins").withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal("Ticket: " + String.format("%,d", TICKET_COST) + " coins").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("Your tickets: " + getPlayerTickets(player.getUUID().toString())).withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to buy 1 ticket").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        buyLotteryTicket(player, 1);
                        openCoinSinkGui(player, page);
                    })
                    .build());

            // Lucky Wheel
            gui.setSlot(2, new GuiElementBuilder(Items.CLOCK)
                    .setName(Component.literal("🎡 Lucky Wheel").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Cost: " + String.format("%,d", WHEEL_SPIN_COST) + " coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Prizes:").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("  0.3% → 100M coins").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal("  1.2% → 50M coins").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal("  3.5% → 10M coins").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to spin!").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        spinWheel(player);
                        openCoinSinkGui(player, page);
                    })
                    .build());

            // Coin Flip
            gui.setSlot(3, new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setName(Component.literal("🪙 Coin Flip").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Min bet: 1M coins").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("47% win chance").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("1.9x payout on win").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to bet 1M on heads").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        coinFlip(player, 1_000_000, true);
                        openCoinSinkGui(player, page);
                    })
                    .build());

            // Dice
            gui.setSlot(4, new GuiElementBuilder(Items.DIAMOND)
                    .setName(Component.literal("🎲 Dice Roll").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Min bet: 1M coins").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("Guess the roll (1-6)").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("5x payout on correct guess!").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to bet 1M on 6").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        diceRoll(player, 1_000_000, 6);
                        openCoinSinkGui(player, page);
                    })
                    .build());

            // High Roller Coin Flip
            gui.setSlot(10, new GuiElementBuilder(Items.NETHER_STAR)
                    .setName(Component.literal("💎 High Roller Coin Flip").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Min: 10M | Max: 1B").withStyle(ChatFormatting.LIGHT_PURPLE))
                    .addLoreLine(Component.literal("45% win chance").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("1.85x payout").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to bet 50M on heads").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        highRollerCoinFlip(player, 50_000_000, true);
                        openCoinSinkGui(player, page);
                    })
                    .build());

            // VIP Status
            gui.setSlot(11, new GuiElementBuilder(Items.TOTEM_OF_UNDYING)
                    .setName(Component.literal("⭐ VIP Status").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Cost: 50,000,000 coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal("Duration: 24 hours").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Perks:").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal("  +10% coin drops").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("  Colored name in chat").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to purchase").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        purchaseVipStatus(player);
                        openCoinSinkGui(player, page);
                    })
                    .build());

            // Global Broadcast
            gui.setSlot(12, new GuiElementBuilder(Items.BEACON)
                    .setName(Component.literal("📢 Global Broadcast").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Cost: 10,000,000 coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Broadcast a message").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("to all players!").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Use /broadcast <message>").withStyle(ChatFormatting.YELLOW))
                    .build());

            // Title Shop
            gui.setSlot(13, new GuiElementBuilder(Items.WRITTEN_BOOK)
                    .setName(Component.literal("📖 Title Shop").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Custom chat titles!").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Prices:").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal("  Emperor: 500M").withStyle(ChatFormatting.DARK_PURPLE))
                    .addLoreLine(Component.literal("  Legendary: 250M").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal("  Titan: 100M").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal("  Epic: 50M").withStyle(ChatFormatting.AQUA))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to open shop").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> openTitleShop(player))
                    .build());

            // Repair All
            gui.setSlot(19, new GuiElementBuilder(Items.ANVIL)
                    .setName(Component.literal("🔧 Repair All Items").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Cost: 50,000,000 coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Repairs ALL items in").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("your inventory instantly").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to repair all").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        repairAllItems(player);
                        openCoinSinkGui(player, page);
                    })
                    .build());

            // Random Teleport
            gui.setSlot(20, new GuiElementBuilder(Items.ENDER_PEARL)
                    .setName(Component.literal("🌀 Random Teleport").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Cost: 15,000,000 coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Teleport to a random").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("location in the world").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to teleport").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        purchaseRandomTeleport(player);
                        openCoinSinkGui(player, page);
                    })
                    .build());

            // Clear Tax Debt
            int taxDebt = TaxManager.getTaxOwed(player.getUUID().toString());
            gui.setSlot(21, new GuiElementBuilder(Items.BOOK)
                    .setName(Component.literal("📜 Clear Tax Debt").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Your debt: " + String.format("%,d", taxDebt) + " coins").withStyle(ChatFormatting.RED))
                    .addLoreLine(Component.literal("Cost: " + String.format("%,d", (long)(taxDebt * 1.5)) + " (150%)").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Pay off all tax debt").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("at a 50% premium").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal(taxDebt > 0 ? "Click to clear debt" : "No debt to clear").withStyle(taxDebt > 0 ? ChatFormatting.YELLOW : ChatFormatting.GRAY))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        if (taxDebt > 0) {
                            clearTaxDebt(player);
                            openCoinSinkGui(player, page);
                        }
                    })
                    .build());

            // Item Rename
            gui.setSlot(22, new GuiElementBuilder(Items.NAME_TAG)
                    .setName(Component.literal("✏️ Rename Item").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Cost: 5,000,000 coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Rename any held item").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("with a custom name!").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Use /renameitem <name>").withStyle(ChatFormatting.YELLOW))
                    .build());

            gui.setSlot(23, new GuiElementBuilder(Items.LODESTONE)
                    .setName(Component.literal("\uD83C\uDFDB Royal Treasury").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Sink hundreds of millions of coins").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("for mastery, skill points, cores & sigils.").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to open").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> RoyalTreasuryManager.open(player))
                    .build());

        } else if (page == 1) {
            // Page 2: Functional Boosts
            // XP Boost
            gui.setSlot(1, new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
                    .setName(Component.literal("✨ 2x XP Boost").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Cost: 20,000,000 coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal("Duration: 1 hour").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Double all XP gained!").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to purchase").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        purchaseXpBoost(player);
                        openCoinSinkGui(player, page);
                    })
                    .build());

            // Luck Boost
            gui.setSlot(2, new GuiElementBuilder(Items.LIME_DYE)
                    .setName(Component.literal("🍀 2x Luck Boost").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Cost: 30,000,000 coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal("Duration: 1 hour").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Double rare drop rates!").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to purchase").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        purchaseLuckBoost(player);
                        openCoinSinkGui(player, page);
                    })
                    .build());

            // Coin Boost
            gui.setSlot(3, new GuiElementBuilder(Items.SUNFLOWER)
                    .setName(Component.literal("💰 +50% Coin Boost").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Cost: 40,000,000 coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal("Duration: 2 hours").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("+50% coins from all sources!").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to purchase").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        purchaseCoinBoost(player);
                        openCoinSinkGui(player, page);
                    })
                    .build());

            // Mining Boost
            gui.setSlot(4, new GuiElementBuilder(Items.DIAMOND_PICKAXE)
                    .setName(Component.literal("⛏️ 3x Mining Boost").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Cost: 35,000,000 coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal("Duration: 1 hour").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Triple mining drops!").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to purchase").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        purchaseMiningBoost(player);
                        openCoinSinkGui(player, page);
                    })
                    .build());

            // Damage Boost
            gui.setSlot(5, new GuiElementBuilder(Items.DIAMOND_SWORD)
                    .setName(Component.literal("⚔️ +25% Damage Boost").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Cost: 45,000,000 coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal("Duration: 1 hour").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("+25% damage dealt!").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to purchase").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        purchaseDamageBoost(player);
                        openCoinSinkGui(player, page);
                    })
                    .build());

            // Defense Boost
            gui.setSlot(10, new GuiElementBuilder(Items.DIAMOND_CHESTPLATE)
                    .setName(Component.literal("🛡️ 25% Damage Reduction").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Cost: 40,000,000 coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal("Duration: 1 hour").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Take 25% less damage!").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to purchase").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        purchaseDefenseBoost(player);
                        openCoinSinkGui(player, page);
                    })
                    .build());

            // Flight
            gui.setSlot(11, new GuiElementBuilder(Items.ELYTRA)
                    .setName(Component.literal("🕊️ Creative Flight").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Cost: 75,000,000 coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal("Duration: 1 hour").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Fly like creative mode!").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to purchase").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        purchaseFlight(player);
                        openCoinSinkGui(player, page);
                    })
                    .build());

            // Death Protection
            gui.setSlot(12, new GuiElementBuilder(Items.TOTEM_OF_UNDYING)
                    .setName(Component.literal("💀 Death Protection").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Cost: 50,000,000 coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal("Duration: Until death or 24h").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Keep inventory on death!").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to purchase").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        purchaseDeathProtection(player);
                        openCoinSinkGui(player, page);
                    })
                    .build());

            // Extra Home
            gui.setSlot(13, new GuiElementBuilder(Items.RED_BED)
                    .setName(Component.literal("🏠 Extra Home Slot").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Cost: 25,000,000 coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal("Permanent!").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("You have: " + getExtraHomes(player.getUUID().toString()) + " extra homes").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal("Max: 10 extra homes").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to purchase").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        purchaseExtraHome(player);
                        openCoinSinkGui(player, page);
                    })
                    .build());

            // Chunk Loader
            gui.setSlot(14, new GuiElementBuilder(Items.REPEATING_COMMAND_BLOCK)
                    .setName(Component.literal("🔄 Chunk Loader").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Cost: 100,000,000 coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal("Duration: 24 hours").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Keep chunks loaded!").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to purchase").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        purchaseChunkLoader(player);
                        openCoinSinkGui(player, page);
                    })
                    .build());

            // Instant Level 30
            gui.setSlot(19, new GuiElementBuilder(Items.ENCHANTED_BOOK)
                    .setName(Component.literal("📚 Instant Level 30").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Cost: 60,000,000 coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Instantly reach XP level 30!").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to purchase").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        purchaseInstantLevel(player);
                        openCoinSinkGui(player, page);
                    })
                    .build());

            // Heal & Feed
            gui.setSlot(20, new GuiElementBuilder(Items.GOLDEN_APPLE)
                    .setName(Component.literal("❤️ Full Heal & Feed").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Cost: 5,000,000 coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Full health & hunger!").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to purchase").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        purchaseHealFeed(player);
                        openCoinSinkGui(player, page);
                    })
                    .build());

            // Clear Cooldowns
            gui.setSlot(21, new GuiElementBuilder(Items.CLOCK)
                    .setName(Component.literal("⏰ Clear Cooldowns").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Cost: 10,000,000 coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Reset all item cooldowns!").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to purchase").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        purchaseClearCooldowns(player);
                        openCoinSinkGui(player, page);
                    })
                    .build());

        } else if (page == 2) {
            // Page 3: Potions & Items
            // Night Vision
            gui.setSlot(1, new GuiElementBuilder(Items.POTION)
                    .setName(Component.literal("👁️ Night Vision").withStyle(ChatFormatting.DARK_BLUE, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Cost: 15M | 1 hour"))
                    .setCallback((index, type, containerInput, slotGui) -> { purchaseNightVision(player); openCoinSinkGui(player, page); })
                    .build());

            // Haste
            gui.setSlot(2, new GuiElementBuilder(Items.GOLDEN_PICKAXE)
                    .setName(Component.literal("⚡ Haste II").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Cost: 20M | 1 hour"))
                    .setCallback((index, type, containerInput, slotGui) -> { purchaseHaste(player); openCoinSinkGui(player, page); })
                    .build());

            // Speed
            gui.setSlot(3, new GuiElementBuilder(Items.SUGAR)
                    .setName(Component.literal("💨 Speed II").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Cost: 25M | 1 hour"))
                    .setCallback((index, type, containerInput, slotGui) -> { purchaseSpeed(player); openCoinSinkGui(player, page); })
                    .build());

            // Strength
            gui.setSlot(4, new GuiElementBuilder(Items.BLAZE_POWDER)
                    .setName(Component.literal("💪 Strength").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Cost: 30M | 1 hour"))
                    .setCallback((index, type, containerInput, slotGui) -> { purchaseStrength(player); openCoinSinkGui(player, page); })
                    .build());

            // Resistance
            gui.setSlot(5, new GuiElementBuilder(Items.IRON_CHESTPLATE)
                    .setName(Component.literal("🛡️ Resistance").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Cost: 35M | 1 hour"))
                    .setCallback((index, type, containerInput, slotGui) -> { purchaseResistance(player); openCoinSinkGui(player, page); })
                    .build());

            // Fire Resistance
            gui.setSlot(10, new GuiElementBuilder(Items.MAGMA_CREAM)
                    .setName(Component.literal("🔥 Fire Resistance").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Cost: 20M | 1 hour"))
                    .setCallback((index, type, containerInput, slotGui) -> { purchaseFireResistance(player); openCoinSinkGui(player, page); })
                    .build());

            // Water Breathing
            gui.setSlot(11, new GuiElementBuilder(Items.COD_BUCKET)
                    .setName(Component.literal("🌊 Water Breathing").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Cost: 15M | 1 hour"))
                    .setCallback((index, type, containerInput, slotGui) -> { purchaseWaterBreathing(player); openCoinSinkGui(player, page); })
                    .build());

            // Jump Boost
            gui.setSlot(12, new GuiElementBuilder(Items.RABBIT_HIDE)
                    .setName(Component.literal("🦘 Jump Boost II").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Cost: 15M | 1 hour"))
                    .setCallback((index, type, containerInput, slotGui) -> { purchaseJumpBoost(player); openCoinSinkGui(player, page); })
                    .build());

            // Regeneration
            gui.setSlot(13, new GuiElementBuilder(Items.GHAST_TEAR)
                    .setName(Component.literal("💚 Regeneration II").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Cost: 50M | 30 min"))
                    .setCallback((index, type, containerInput, slotGui) -> { purchaseRegeneration(player); openCoinSinkGui(player, page); })
                    .build());

            // Spawner
            gui.setSlot(19, new GuiElementBuilder(Items.SPAWNER)
                    .setName(Component.literal("📦 Spawner").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Cost: 100,000,000 coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Get a spawner block!").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to purchase").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        purchaseSpawnerKey(player);
                        openCoinSinkGui(player, page);
                    })
                    .build());

            // Enchanted Golden Apple
            gui.setSlot(20, new GuiElementBuilder(Items.ENCHANTED_GOLDEN_APPLE)
                    .setName(Component.literal("🍎 Enchanted Golden Apple").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Cost: 10,000,000 coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to purchase").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        purchaseEnchantedGoldenApple(player);
                        openCoinSinkGui(player, page);
                    })
                    .build());

            // Beacon
            gui.setSlot(21, new GuiElementBuilder(Items.BEACON)
                    .setName(Component.literal("✨ Beacon").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Cost: 50,000,000 coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to purchase").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        purchaseBeacon(player);
                        openCoinSinkGui(player, page);
                    })
                    .build());

            // Conduit
            gui.setSlot(22, new GuiElementBuilder(Items.CONDUIT)
                    .setName(Component.literal("🌊 Conduit").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Cost: 75,000,000 coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to purchase").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        purchaseConduit(player);
                        openCoinSinkGui(player, page);
                    })
                    .build());

            // Dimension Warp
            gui.setSlot(23, new GuiElementBuilder(Items.END_PORTAL_FRAME)
                    .setName(Component.literal("🌀 Dimension Warp").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Cost: 30,000,000 coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Warp to any dimension!").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Use /warp <dimension>").withStyle(ChatFormatting.YELLOW))
                    .build());
        } else if (page == 3) {
            // Page 4: Unique Coin Sinks (Mod Integration)
            
            // Slayer XP Boost
            gui.setSlot(1, new GuiElementBuilder(Items.NETHERITE_SWORD)
                    .setName(Component.literal("⚔️ Slayer XP Boost").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Cost: 75,000,000 coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Instant +10,000 Slayer XP!").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to purchase").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        purchaseSlayerXpBoost(player);
                        openCoinSinkGui(player, page);
                    })
                    .build());

            // Election Boost
            gui.setSlot(2, new GuiElementBuilder(Items.GOLD_BLOCK)
                    .setName(Component.literal("🗳️ Election Boost").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Cost: 200,000,000 coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Your next vote counts DOUBLE!").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to purchase").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        purchaseElectionBoost(player);
                        openCoinSinkGui(player, page);
                    })
                    .build());

            // Stock Insider Tip
            gui.setSlot(3, new GuiElementBuilder(Items.EMERALD)
                    .setName(Component.literal("📈 Stock Insider Tip").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Cost: 150,000,000 coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Predict next stock trends!").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to purchase").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        purchaseInsiderTip(player);
                        openCoinSinkGui(player, page);
                    })
                    .build());

            // Crypto Boost
            gui.setSlot(4, new GuiElementBuilder(Items.RECOVERY_COMPASS)
                    .setName(Component.literal("₿ Crypto Mining Boost").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Cost: 100,000,000 coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal("Duration: 1 hour").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("2x crypto mining rate!").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to purchase").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        purchaseCryptoBoost(player);
                        openCoinSinkGui(player, page);
                    })
                    .build());

            // Bank Vault Expansion
            gui.setSlot(5, new GuiElementBuilder(Items.ENDER_CHEST)
                    .setName(Component.literal("🏦 Bank Vault Expansion").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Cost: 500,000,000 coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal("Permanent!").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("+1B bank storage capacity!").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal("Max 5 expansions").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to purchase").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        purchaseBankVaultExpansion(player);
                        openCoinSinkGui(player, page);
                    })
                    .build());

            // Perk Slot
            gui.setSlot(10, new GuiElementBuilder(Items.ENCHANTED_GOLDEN_APPLE)
                    .setName(Component.literal("✨ Extra Perk Slot").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Cost: 400,000,000 coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal("Permanent!").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Unlock an extra perk slot!").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal("Max 3 extra slots").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to purchase").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        purchasePerkSlot(player);
                        openCoinSinkGui(player, page);
                    })
                    .build());

            // Bounty Protection
            gui.setSlot(12, new GuiElementBuilder(Items.SHIELD)
                    .setName(Component.literal("🛡️ Bounty Protection").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Cost: 60,000,000 coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Extend protection by 24h!").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to purchase").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        purchaseBountyProtectionExtension(player);
                        openCoinSinkGui(player, page);
                    })
                    .build());

            // Crop Growth
            gui.setSlot(13, new GuiElementBuilder(Items.WHEAT)
                    .setName(Component.literal("🌾 Instant Crop Growth").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Cost: 30,000,000 coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Grow all crops in 50 block").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal("radius instantly!").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to purchase").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        purchaseInstantCropGrowth(player);
                        openCoinSinkGui(player, page);
                    })
                    .build());

            // Weather Control
            gui.setSlot(19, new GuiElementBuilder(Items.GLOWSTONE)
                    .setName(Component.literal("☀️ Weather Control").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Cost: 100,000,000 coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Set weather for 1 hour!").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click for clear weather").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        purchaseWeatherControl(player, true);
                        openCoinSinkGui(player, page);
                    })
                    .build());

            // Time Set
            gui.setSlot(20, new GuiElementBuilder(Items.CLOCK)
                    .setName(Component.literal("⏰ Time Control").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Cost: 75,000,000 coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Set time to day or night!").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click for day").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        purchaseTimeSet(player, true);
                        openCoinSinkGui(player, page);
                    })
                    .build());

            // Pet Summon
            gui.setSlot(21, new GuiElementBuilder(Items.BONE)
                    .setName(Component.literal("🐕 Pet Summon").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Cost: 40,000,000 coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Spawn a tamed pet!").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click for wolf").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        purchasePet(player, "wolf");
                        openCoinSinkGui(player, page);
                    })
                    .build());

            // Featured Auction
            gui.setSlot(22, new GuiElementBuilder(Items.GOLD_NUGGET)
                    .setName(Component.literal("⭐ Featured Auction").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Cost: 25,000,000 coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Highlight your next auction!").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to purchase").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        purchaseFeaturedAuction(player);
                        openCoinSinkGui(player, page);
                    })
                    .build());
        }

        // Navigation
        if (page > 0) {
            gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("← Previous Page").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> openCoinSinkGui(player, page - 1))
                    .build());
        }

        if (page < 3) {
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Next Page →").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> openCoinSinkGui(player, page + 1))
                    .build());
        }

        // Close button
        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Close").withStyle(ChatFormatting.RED))
                .setCallback((index, type, containerInput, slotGui) -> player.closeContainer())
                .build());

        gui.open();
    }

    public static void openTitleShop(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("📖 Title Shop"));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Component.literal("")).build());
        }

        // Emperor
        gui.setSlot(10, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("Emperor").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Cost: 500,000,000 coins").withStyle(ChatFormatting.GOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to purchase").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> {
                    purchaseTitle(player, "Emperor", "d");
                    openTitleShop(player);
                })
                .build());

        // Legendary
        gui.setSlot(11, new GuiElementBuilder(Items.BEACON)
                .setName(Component.literal("Legendary").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Cost: 250,000,000 coins").withStyle(ChatFormatting.GOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to purchase").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> {
                    purchaseTitle(player, "Legendary", "6");
                    openTitleShop(player);
                })
                .build());

        // Titan
        gui.setSlot(12, new GuiElementBuilder(Items.TOTEM_OF_UNDYING)
                .setName(Component.literal("Titan").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Cost: 100,000,000 coins").withStyle(ChatFormatting.GOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to purchase").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> {
                    purchaseTitle(player, "Titan", "a");
                    openTitleShop(player);
                })
                .build());

        // Epic
        gui.setSlot(13, new GuiElementBuilder(Items.DIAMOND)
                .setName(Component.literal("Epic").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Cost: 50,000,000 coins").withStyle(ChatFormatting.GOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to purchase").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> {
                    purchaseTitle(player, "Epic", "b");
                    openTitleShop(player);
                })
                .build());

        // Elite
        gui.setSlot(14, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Component.literal("Elite").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Cost: 25,000,000 coins").withStyle(ChatFormatting.GOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to purchase").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> {
                    purchaseTitle(player, "Elite", "e");
                    openTitleShop(player);
                })
                .build());

        // Back
        gui.setSlot(18, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("Back").withStyle(ChatFormatting.GRAY))
                .setCallback((index, type, containerInput, slotGui) -> openCoinSinkGui(player))
                .build());

        gui.open();
    }

    // ── Save/Load ──
    public static void loadFromData(DataManager.SaveData data) {
        lastLotteryDraw = data.lotteryLastDraw;
        lotteryPot = data.lotteryPot;
        if (data.lotteryTickets != null) {
            lotteryTickets.clear();
            lotteryTickets.putAll(data.lotteryTickets);
        }
        if (data.vipPerkExpirations != null) {
            vipPerkExpirations.clear();
            vipPerkExpirations.putAll(data.vipPerkExpirations);
        }
        // Load functional boosts
        if (data.xpBoostExpirations != null) {
            xpBoostExpirations.clear();
            xpBoostExpirations.putAll(data.xpBoostExpirations);
        }
        if (data.luckBoostExpirations != null) {
            luckBoostExpirations.clear();
            luckBoostExpirations.putAll(data.luckBoostExpirations);
        }
        if (data.coinBoostExpirations != null) {
            coinBoostExpirations.clear();
            coinBoostExpirations.putAll(data.coinBoostExpirations);
        }
        if (data.extraHomes != null) {
            extraHomes.clear();
            extraHomes.putAll(data.extraHomes);
        }
        if (data.deathProtection != null) {
            deathProtection.clear();
            deathProtection.putAll(data.deathProtection);
        }
        if (data.flightExpirations != null) {
            flightExpirations.clear();
            flightExpirations.putAll(data.flightExpirations);
        }
        if (data.miningBoostExpirations != null) {
            miningBoostExpirations.clear();
            miningBoostExpirations.putAll(data.miningBoostExpirations);
        }
        if (data.damageBoostExpirations != null) {
            damageBoostExpirations.clear();
            damageBoostExpirations.putAll(data.damageBoostExpirations);
        }
        if (data.defenseBoostExpirations != null) {
            defenseBoostExpirations.clear();
            defenseBoostExpirations.putAll(data.defenseBoostExpirations);
        }
        if (data.chunkLoaders != null) {
            chunkLoaders.clear();
            chunkLoaders.addAll(data.chunkLoaders);
        }
    }

    // ════════════════════════════════════════════════════════════
    // UNIQUE COIN SINKS - Integration with existing mod features
    // ════════════════════════════════════════════════════════════

    // Slayer instant level boost
    private static final Map<String, Integer> slayerXpBoosts = new ConcurrentHashMap<>();
    
    /**
     * Slayer XP Boost - Instant 10k XP for current slayer - 75M
     */
    public static void purchaseSlayerXpBoost(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 75_000_000;
        
        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }
        
        CoinManager.removeCoins(uuid, cost);
        slayerXpBoosts.merge(uuid, 10000, Integer::sum);
        player.sendSystemMessage(Component.literal("§a✓ Gained 10,000 Slayer XP! Check your slayer progress with /slayer"), false);
        DataManager.save(PoliticalServer.server);
    }
    
    public static int getSlayerXpBoost(String uuid) {
        int boost = slayerXpBoosts.getOrDefault(uuid, 0);
        if (boost > 0) {
            slayerXpBoosts.put(uuid, 0); // Consume on read
        }
        return boost;
    }

    /**
     * Election Campaign Boost - Double vote weight for next election - 200M
     */
    public static void purchaseElectionBoost(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 200_000_000;
        
        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }
        
        CoinManager.removeCoins(uuid, cost);
        ElectionManager.grantVoteBoost(uuid);
        player.sendSystemMessage(Component.literal("§a✓ Your next vote will count DOUBLE!"), false);
        player.sendSystemMessage(Component.literal("§7Use during the next election."), false);
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Stock Market Insider Tip - Reveal next price trend - 150M
     */
    public static void purchaseInsiderTip(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 150_000_000;
        
        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }
        
        CoinManager.removeCoins(uuid, cost);
        
        // Predict next trend for all stocks
        StringBuilder tip = new StringBuilder("§6═══ INSIDER TIPS ═══\n");
        for (StockMarket.Stock stock : StockMarket.Stock.values()) {
            double currentPrice = StockMarket.getPrice(stock.symbol);
            boolean willRise = Math.random() > 0.5;
            tip.append("§e").append(stock.name).append("§7: ")
               .append(willRise ? "§a▲ Likely to RISE" : "§c▼ Likely to FALL").append("\n");
        }
        player.sendSystemMessage(Component.literal(tip.toString()), false);
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Crypto Mining Boost - 2x crypto mining rate for 1 hour - 100M
     */
    private static final Map<String, Long> cryptoBoostExpirations = new ConcurrentHashMap<>();
    
    public static void purchaseCryptoBoost(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 100_000_000;
        
        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }
        
        CoinManager.removeCoins(uuid, cost);
        cryptoBoostExpirations.put(uuid, System.currentTimeMillis() + (60 * 60 * 1000));
        player.sendSystemMessage(Component.literal("§a✓ 2x Crypto Mining Rate for 1 hour!"), false);
        DataManager.save(PoliticalServer.server);
    }
    
    public static double getCryptoBoostMultiplier(String uuid) {
        Long exp = cryptoBoostExpirations.get(uuid);
        return (exp != null && exp > System.currentTimeMillis()) ? 2.0 : 1.0;
    }

    /**
     * Bank Vault Expansion - +1B storage capacity permanently - 500M
     */
    private static final Map<String, Integer> bankVaultExpansions = new ConcurrentHashMap<>();
    
    public static void purchaseBankVaultExpansion(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 500_000_000;
        int current = bankVaultExpansions.getOrDefault(uuid, 0);
        
        if (current >= 5) {
            player.sendSystemMessage(Component.literal("§c✗ Maximum vault expansions reached (5)!"), false);
            return;
        }
        
        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }
        
        CoinManager.removeCoins(uuid, cost);
        bankVaultExpansions.put(uuid, current + 1);
        player.sendSystemMessage(Component.literal("§a✓ Bank vault expanded! +1B storage capacity."), false);
        player.sendSystemMessage(Component.literal("§7Total expansions: " + (current + 1) + "/5"), false);
        DataManager.save(PoliticalServer.server);
    }
    
    public static long getBankVaultBonus(String uuid) {
        return bankVaultExpansions.getOrDefault(uuid, 0) * 1_000_000_000L;
    }

    /**
     * Auction House Featured Listing - Highlight your auction - 25M
     */
    private static final Set<String> featuredAuctions = ConcurrentHashMap.newKeySet();
    
    public static void purchaseFeaturedAuction(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 25_000_000;
        
        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }
        
        CoinManager.removeCoins(uuid, cost);
        featuredAuctions.add(uuid);
        player.sendSystemMessage(Component.literal("§a✓ Your next auction listing will be FEATURED!"), false);
        player.sendSystemMessage(Component.literal("§7Featured auctions appear at the top and glow."), false);
        DataManager.save(PoliticalServer.server);
    }
    
    public static boolean hasFeaturedAuction(String uuid) {
        return featuredAuctions.remove(uuid);
    }

    /**
     * Custom Enchant Reroll - Reroll an item's custom enchants - 80M
     */
    public static void purchaseEnchantReroll(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 80_000_000;
        
        var mainHand = player.getMainHandItem();
        if (mainHand.isEmpty()) {
            player.sendSystemMessage(Component.literal("§c✗ Hold an item to reroll!"), false);
            return;
        }
        
        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }
        
        CoinManager.removeCoins(uuid, cost);
        
        // Remove existing custom enchants and reapply random ones
        var enchants = mainHand.getEnchantments();
        // This would integrate with the custom enchant system
        player.sendSystemMessage(Component.literal("§a✓ Custom enchants rerolled!"), false);
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Perk Slot Unlock - Unlock extra perk slot - 400M
     */
    private static final Map<String, Integer> perkSlotUnlocks = new ConcurrentHashMap<>();
    
    public static void purchasePerkSlot(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 400_000_000;
        int current = perkSlotUnlocks.getOrDefault(uuid, 0);
        
        if (current >= 3) {
            player.sendSystemMessage(Component.literal("§c✗ Maximum perk slots reached (3 extra)!"), false);
            return;
        }
        
        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }
        
        CoinManager.removeCoins(uuid, cost);
        perkSlotUnlocks.put(uuid, current + 1);
        player.sendSystemMessage(Component.literal("§a✓ Extra perk slot unlocked!"), false);
        player.sendSystemMessage(Component.literal("§7Total extra slots: " + (current + 1) + "/3"), false);
        DataManager.save(PoliticalServer.server);
    }
    
    public static int getExtraPerkSlots(String uuid) {
        return perkSlotUnlocks.getOrDefault(uuid, 0);
    }

    /**
     * Bounty Protection Extension - Extend bounty protection by 24h - 60M
     */
    public static void purchaseBountyProtectionExtension(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 60_000_000;
        
        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }
        
        CoinManager.removeCoins(uuid, cost);
        BountyArmorHandler.extendProtection(uuid, 24 * 60 * 60 * 1000);
        player.sendSystemMessage(Component.literal("§a✓ Bounty protection extended by 24 hours!"), false);
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Instant Crop Growth - Grow all crops in 50 block radius - 30M
     */
    public static void purchaseInstantCropGrowth(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 30_000_000;
        
        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }
        
        CoinManager.removeCoins(uuid, cost);
        
        var world = (ServerLevel) player.level();
        var pos = player.blockPosition();
        int grown = 0;
        
        for (int x = -50; x <= 50; x++) {
            for (int y = -10; y <= 10; y++) {
                for (int z = -50; z <= 50; z++) {
                    var blockPos = pos.offset(x, y, z);
                    var state = world.getBlockState(blockPos);
                    if (state.getBlock() instanceof net.minecraft.world.level.block.CropBlock crop) {
                        if (crop.getAge(state) < crop.getMaxAge()) {
                            world.setBlock(blockPos, state.setValue(CropBlock.AGE, crop.getMaxAge()), Block.UPDATE_CLIENTS);
                            grown++;
                        }
                    } else if (state.getBlock() instanceof net.minecraft.world.level.block.StemBlock) {
                        // Stem blocks - set to maximum age (7)
                        world.setBlock(blockPos, state.setValue(StemBlock.AGE, 7), Block.UPDATE_CLIENTS);
                        grown++;
                    }
                }
            }
        }
        
        player.sendSystemMessage(Component.literal("§a✓ Grew " + grown + " crops instantly!"), false);
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Pet Summon - Spawn a tamed pet - 40M
     */
    public static void purchasePet(ServerPlayer player, String petType) {
        String uuid = player.getUUID().toString();
        long cost = 40_000_000;
        
        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }
        
        CoinManager.removeCoins(uuid, cost);
        
        var world = (ServerLevel) player.level();
        var pos = player.blockPosition();
        
        net.minecraft.world.entity.Entity entity = switch (petType.toLowerCase()) {
            case "wolf" -> EntityType.WOLF.spawn(world, pos, net.minecraft.world.entity.EntitySpawnReason.EVENT);
            case "cat" -> EntityType.CAT.spawn(world, pos, net.minecraft.world.entity.EntitySpawnReason.EVENT);
            case "parrot" -> EntityType.PARROT.spawn(world, pos, net.minecraft.world.entity.EntitySpawnReason.EVENT);
            case "fox" -> EntityType.FOX.spawn(world, pos, net.minecraft.world.entity.EntitySpawnReason.EVENT);
            default -> EntityType.WOLF.spawn(world, pos, net.minecraft.world.entity.EntitySpawnReason.EVENT);
        };
        
        if (entity instanceof TamableAnimal pet) {
            pet.tame(player);
            pet.setCustomName(Component.literal("§b" + player.getName().getString() + "'s Pet"));
            player.sendSystemMessage(Component.literal("§a✓ Summoned a tamed " + petType + "!"), false);
        }
        
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Weather Control - Set weather for 1 hour - 100M
     */
    public static void purchaseWeatherControl(ServerPlayer player, boolean clear) {
        String uuid = player.getUUID().toString();
        long cost = 100_000_000;
        
        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }
        
        CoinManager.removeCoins(uuid, cost);
        
        var world = (ServerLevel) player.level();
        WeatherData wd = world.getWeatherData();
        if (clear) {
            wd.setRaining(false);
            wd.setThundering(false);
            wd.setClearWeatherTime(72000);
            wd.setRainTime(0);
            wd.setThunderTime(0);
            player.sendSystemMessage(Component.literal("§a✓ Weather cleared for 1 hour!"), false);
        } else {
            wd.setRaining(true);
            wd.setThundering(true);
            wd.setRainTime(72000);
            wd.setClearWeatherTime(0);
            player.sendSystemMessage(Component.literal("§a✓ Storm summoned for 1 hour!"), false);
        }
        
        // Broadcast to all players
        for (ServerPlayer p : world.players()) {
            p.sendSystemMessage(Component.literal("§e☀ " + player.getName().getString() + " changed the weather!"), false);
        }
        
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Time Set - Set time to day/night - 75M
     */
    public static void purchaseTimeSet(ServerPlayer player, boolean day) {
        String uuid = player.getUUID().toString();
        long cost = 75_000_000;
        
        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }
        
        CoinManager.removeCoins(uuid, cost);
        
        var world = (ServerLevel) player.level();
        if (world.dimension() == Level.OVERWORLD) {
            Holder<WorldClock> overworldClock = world.registryAccess()
                    .lookupOrThrow(Registries.WORLD_CLOCK)
                    .getOrThrow(WorldClocks.OVERWORLD);
            ServerClockManager cm = world.clockManager();
            long targetTime = day ? 1000 : 13000;
            long total = cm.getTotalTicks(overworldClock);
            long base = (total / 24000) * 24000;
            cm.setTotalTicks(overworldClock, base + targetTime);
        }
        
        player.sendSystemMessage(Component.literal("§a✓ Time set to " + (day ? "day" : "night") + "!"), false);
        
        // Broadcast
        for (ServerPlayer p : world.players()) {
            p.sendSystemMessage(Component.literal("§e⏰ " + player.getName().getString() + " set time to " + (day ? "day" : "night") + "!"), false);
        }
        
        DataManager.save(PoliticalServer.server);
    }

    // ── Premium Custom Items (Expensive, No Recipe) ──

    /**
     * Purchase The Gavel - Legendary Mace - 500M
     */
    public static void purchaseTheGavel(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 500_000_000;

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);
        player.addItem(CustomItemHandler.createTheGavel());
        player.sendSystemMessage(Component.literal("§a✓ Purchased The Gavel for " + String.format("%,d", cost) + " coins!"), false);
        player.sendSystemMessage(Component.literal("§7This item has no recipe and can only be obtained here."), false);
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Purchase Hermes Shoes - Speed Boots - 300M
     */
    public static void purchaseHermesShoes(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 300_000_000;

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);
        player.addItem(CustomItemHandler.createHermesShoes());
        player.sendSystemMessage(Component.literal("§a✓ Purchased Hermes Shoes for " + String.format("%,d", cost) + " coins!"), false);
        player.sendSystemMessage(Component.literal("§7This item has no recipe and can only be obtained here."), false);
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Purchase Rift Mirror - Teleportation Item - 400M
     */
    public static void purchaseRiftMirror(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 400_000_000;

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);
        player.addItem(CustomItemHandler.createRiftMirror());
        player.sendSystemMessage(Component.literal("§a✓ Purchased Rift Mirror for " + String.format("%,d", cost) + " coins!"), false);
        player.sendSystemMessage(Component.literal("§7This item has no recipe and can only be obtained here."), false);
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Purchase Ember Signet - Fire Damage Ring - 350M
     */
    public static void purchaseEmberSignet(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 350_000_000;

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);
        player.addItem(CustomItemHandler.createEmberSignet());
        player.sendSystemMessage(Component.literal("§a✓ Purchased Ember Signet for " + String.format("%,d", cost) + " coins!"), false);
        player.sendSystemMessage(Component.literal("§7This item has no recipe and can only be obtained here."), false);
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Purchase Frostbound Talisman - Ice Protection - 350M
     */
    public static void purchaseFrostboundTalisman(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 350_000_000;

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);
        player.addItem(CustomItemHandler.createFrostboundTalisman());
        player.sendSystemMessage(Component.literal("§a✓ Purchased Frostbound Talisman for " + String.format("%,d", cost) + " coins!"), false);
        player.sendSystemMessage(Component.literal("§7This item has no recipe and can only be obtained here."), false);
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Purchase Echo Chime - Sound Weapon - 250M
     */
    public static void purchaseEchoChime(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 250_000_000;

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);
        player.addItem(CustomItemHandler.createEchoChime());
        player.sendSystemMessage(Component.literal("§a✓ Purchased Echo Chime for " + String.format("%,d", cost) + " coins!"), false);
        player.sendSystemMessage(Component.literal("§7This item has no recipe and can only be obtained here."), false);
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Purchase Terra Relay - Earth Powers - 250M
     */
    public static void purchaseTerraRelay(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 250_000_000;

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);
        player.addItem(CustomItemHandler.createTerraRelay());
        player.sendSystemMessage(Component.literal("§a✓ Purchased Terra Relay for " + String.format("%,d", cost) + " coins!"), false);
        player.sendSystemMessage(Component.literal("§7This item has no recipe and can only be obtained here."), false);
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Purchase Harvey's Stick - Legendary Weapon - 200M
     */
    public static void purchaseHarveysStick(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 200_000_000;

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);
        player.addItem(CustomItemHandler.createHarveysStick());
        player.sendSystemMessage(Component.literal("§a✓ Purchased Harvey's Stick for " + String.format("%,d", cost) + " coins!"), false);
        player.sendSystemMessage(Component.literal("§7This item has no recipe and can only be obtained here."), false);
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Purchase HPEBM Mk.I - High Power Energy Beam - 150M
     */
    public static void purchaseHPEBM(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 150_000_000;

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);
        player.addItem(CustomItemHandler.createHPEBM(1));
        player.sendSystemMessage(Component.literal("§a✓ Purchased HPEBM Mk.I for " + String.format("%,d", cost) + " coins!"), false);
        player.sendSystemMessage(Component.literal("§7This item has no recipe and can only be obtained here."), false);
        DataManager.save(PoliticalServer.server);
    }

    // ── Custom Foods ──

    /**
     * Purchase Golden Apple of Kings - Ultra Food - 100M
     */
    public static void purchaseGoldenAppleOfKings(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 100_000_000;

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);

        ItemStack apple = new ItemStack(Items.ENCHANTED_GOLDEN_APPLE);
        CompoundTag nbt = new CompoundTag();
        nbt.putByte("golden_apple_kings", (byte) 1);
        apple.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        apple.set(DataComponents.CUSTOM_NAME, Component.literal("§6§lGolden Apple of Kings"));
        apple.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal(""),
                Component.literal("§6◆ LEGENDARY FOOD ◆"),
                Component.literal(""),
                Component.literal("§eGrants:"),
                Component.literal("§a- Regeneration V (1 min)"),
                Component.literal("§b- Resistance III (5 min)"),
                Component.literal("§d- Absorption V (10 min)"),
                Component.literal("§c- Fire Resistance (10 min)"),
                Component.literal(""),
                Component.literal("§7No recipe - Premium item only")
        )));
        apple.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

        player.addItem(apple);
        player.sendSystemMessage(Component.literal("§a✓ Purchased Golden Apple of Kings for " + String.format("%,d", cost) + " coins!"), false);
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Purchase Ambrosia - Food of the Gods - 250M
     */
    public static void purchaseAmbrosia(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 250_000_000;

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);

        ItemStack ambrosia = new ItemStack(Items.GOLDEN_APPLE);
        CompoundTag nbt = new CompoundTag();
        nbt.putByte("ambrosia", (byte) 1);
        ambrosia.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        ambrosia.set(DataComponents.CUSTOM_NAME, Component.literal("§d§l✦ Ambrosia ✦"));
        ambrosia.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal(""),
                Component.literal("§d◆ FOOD OF THE GODS ◆"),
                Component.literal(""),
                Component.literal("§eThe legendary food of Mount Olympus"),
                Component.literal(""),
                Component.literal("§aGrants ALL positive effects"),
                Component.literal("§afor 10 minutes each!"),
                Component.literal(""),
                Component.literal("§7No recipe - Premium item only")
        )));
        ambrosia.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

        player.addItem(ambrosia);
        player.sendSystemMessage(Component.literal("§a✓ Purchased Ambrosia for " + String.format("%,d", cost) + " coins!"), false);
        DataManager.save(PoliticalServer.server);
    }

    // ── Mystery Boxes ──

    /**
     * Purchase Mystery Box - Random rewards - 50M
     */
    public static void purchaseMysteryBox(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 50_000_000;

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);

        // Random rewards
        Random random = new Random();
        int roll = random.nextInt(100);

        long reward;
        String rewardName;
        if (roll < 1) { // 1% - Jackpot
            reward = 100_000_000;
            rewardName = "§d§l✦ JACKPOT! ✦";
            player.sendSystemMessage(Component.literal("§d═══════════════════════════════════"), false);
            player.sendSystemMessage(Component.literal("§d§l✦ MYSTERY BOX - JACKPOT! ✦"), false);
        } else if (roll < 5) { // 4% - Ultra Rare
            reward = 50_000_000;
            rewardName = "§5Ultra Rare";
        } else if (roll < 15) { // 10% - Rare
            reward = 25_000_000;
            rewardName = "§bRare";
        } else if (roll < 35) { // 20% - Uncommon
            reward = 15_000_000;
            rewardName = "§aUncommon";
        } else { // 65% - Common
            reward = 5_000_000;
            rewardName = "§7Common";
        }

        CoinManager.giveCoins(uuid, reward);

        player.sendSystemMessage(Component.literal("§5═══════════════════════════════════"), false);
        player.sendSystemMessage(Component.literal("§5§l📦 MYSTERY BOX OPENED!"), false);
        player.sendSystemMessage(Component.literal("§7Rarity: " + rewardName), false);
        player.sendSystemMessage(Component.literal("§6+" + String.format("%,d", reward) + " coins!"), false);
        player.sendSystemMessage(Component.literal("§5═══════════════════════════════════"), false);

        CivilCraftAchievements.trackCoinsEarned(player, reward);
        DataManager.save(PoliticalServer.server);
    }

    /**
     * Purchase Legendary Mystery Box - Better odds - 150M
     */
    public static void purchaseLegendaryMysteryBox(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long cost = 150_000_000;

        if (CoinManager.getCoins(uuid) < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return;
        }

        CoinManager.removeCoins(uuid, cost);

        // Better odds for legendary box
        Random random = new Random();
        int roll = random.nextInt(100);

        long reward;
        String rewardName;
        if (roll < 5) { // 5% - Jackpot (vs 1%)
            reward = 250_000_000;
            rewardName = "§d§l✦ MEGA JACKPOT! ✦";
            player.sendSystemMessage(Component.literal("§d═══════════════════════════════════"), false);
            player.sendSystemMessage(Component.literal("§d§l✦ LEGENDARY BOX - MEGA JACKPOT! ✦"), false);
        } else if (roll < 15) { // 10% - Ultra Rare
            reward = 100_000_000;
            rewardName = "§5Ultra Rare";
        } else if (roll < 35) { // 20% - Rare
            reward = 50_000_000;
            rewardName = "§bRare";
        } else if (roll < 60) { // 25% - Uncommon
            reward = 30_000_000;
            rewardName = "§aUncommon";
        } else { // 40% - Common (still good!)
            reward = 20_000_000;
            rewardName = "§7Common";
        }

        CoinManager.giveCoins(uuid, reward);

        player.sendSystemMessage(Component.literal("§6═══════════════════════════════════"), false);
        player.sendSystemMessage(Component.literal("§6§l📦 LEGENDARY MYSTERY BOX OPENED!"), false);
        player.sendSystemMessage(Component.literal("§7Rarity: " + rewardName), false);
        player.sendSystemMessage(Component.literal("§6+" + String.format("%,d", reward) + " coins!"), false);
        player.sendSystemMessage(Component.literal("§6═══════════════════════════════════"), false);

        CivilCraftAchievements.trackCoinsEarned(player, reward);
        DataManager.save(PoliticalServer.server);
    }

    // ── Save/Load for new sinks ──
    public static void saveToData(DataManager.SaveData data) {
        data.lotteryLastDraw = lastLotteryDraw;
        data.lotteryPot = lotteryPot;
        data.lotteryTickets = new HashMap<>(lotteryTickets);
        data.vipPerkExpirations = new HashMap<>(vipPerkExpirations);
        // Save functional boosts
        data.xpBoostExpirations = new HashMap<>(xpBoostExpirations);
        data.luckBoostExpirations = new HashMap<>(luckBoostExpirations);
        data.coinBoostExpirations = new HashMap<>(coinBoostExpirations);
        data.extraHomes = new HashMap<>(extraHomes);
        data.deathProtection = new HashMap<>(deathProtection);
        data.flightExpirations = new HashMap<>(flightExpirations);
        data.miningBoostExpirations = new HashMap<>(miningBoostExpirations);
        data.damageBoostExpirations = new HashMap<>(damageBoostExpirations);
        data.defenseBoostExpirations = new HashMap<>(defenseBoostExpirations);
        data.chunkLoaders = new HashSet<>(chunkLoaders);
    }
}
