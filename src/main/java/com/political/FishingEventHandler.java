package com.political;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

/**
 * Hooks into vanilla fishing to integrate with FishingManager.
 * Runs on server tick, checks for bobber state changes, awards XP and spawns sea creatures.
 */
public final class FishingEventHandler {

    private static final Map<UUID, Integer> BOBBER_TICKS = new HashMap<>();
    private static final Map<UUID, Integer> LAST_CATCH_TICK = new HashMap<>();
    private static final Map<UUID, Integer> CATCH_COUNTER = new HashMap<>();
    private static final Random RNG = new Random();

    /** Set from mixin HEAD when sea creature / treasure cancels vanilla fish; TAIL skips fish progress if true. */
    private static final ThreadLocal<Boolean> VANILLA_CATCH_CUSTOM = ThreadLocal.withInitial(() -> false);

    public static void tick(MinecraftServer server) {
        for (ServerLevel world : server.getAllLevels()) {
            for (ServerPlayer player : world.players()) {
                tickPlayer(player, world, server.getTickCount());
            }
        }
    }

    private static void tickPlayer(ServerPlayer player, ServerLevel world, int currentTick) {
        UUID pid = player.getUUID();
        String uuid = pid.toString();
        FishingHook bob = player.fishing;

        if (bob == null || !bob.isAlive()) {
            BOBBER_TICKS.remove(pid);
            return;
        }

        FluidState fluid = world.getFluidState(bob.blockPosition());
        boolean inLava = fluid.is(FluidTags.LAVA);
        boolean inWater = fluid.is(FluidTags.WATER);

        if (!inWater && !inLava) {
            BOBBER_TICKS.remove(pid);
            return;
        }

        if (inLava && !FishingManager.hasLavaFishing(uuid)) {
            return;
        }

        // Vanilla fishing rod in open water: sea creatures / treasure / fish use {@link FishingHookMixin} on catchingFish.
        if (inWater && !FishingRods.isFishingRod(player.getMainHandItem())) {
            BOBBER_TICKS.remove(pid);
            return;
        }
        // Vanilla rod in lava is handled by {@link LavaFishingHandler} (no accelerated tick here).
        if (inLava && !FishingRods.isFishingRod(player.getMainHandItem())) {
            BOBBER_TICKS.remove(pid);
            return;
        }

        int ticks = BOBBER_TICKS.merge(pid, 1, Integer::sum);

        double catchSpeed = FishingManager.getCatchSpeedMultiplier(uuid);
        int catchInterval = Math.max(20, (int) (80 / catchSpeed));

        if (ticks < catchInterval) return;

        BOBBER_TICKS.put(pid, 0);

        int lastCatch = LAST_CATCH_TICK.getOrDefault(pid, 0);
        if (currentTick - lastCatch < 15) return;
        LAST_CATCH_TICK.put(pid, currentTick);

        FishingManager.PlayerFishingData data = FishingManager.getData(uuid);
        BlockPos bobPos = bob.blockPosition();

        double seaCreatureChance = FishingManager.getSeaCreatureChance(uuid);
        if (RNG.nextDouble() < seaCreatureChance) {
            FishingManager.SeaCreature creature = FishingManager.rollSeaCreature(uuid, RNG);
            FishingManager.spawnSeaCreature(world, player, creature, bobPos);
            bob.discard();
            return;
        }

        double treasureChance = FishingManager.getTreasureChance(uuid);
        if (RNG.nextDouble() < treasureChance) {
            rollTreasure(player, uuid, data, inLava);
            bob.discard();
            return;
        }

        int baseXp = 5 + RNG.nextInt(16); // 5-20
        if (FishingManager.hasNode(uuid, "rm_precision_reel")) baseXp = (int) (baseXp * 1.2);
        if (FishingManager.hasNode(uuid, "ds_abyss_diver")) baseXp = (int) (baseXp * 1.15);
        if (inLava) baseXp = (int) (baseXp * 1.5);

        data.fishCaught++;
        BuffProgressManager.recordFishCaught(player);
        FishingManager.addXp(player, baseXp);
        PoliticalPartyManager.progressPartyCommission(player, "FISH_CATCHES", 1);
        MagicManager.tryProgressFish(player);

        boolean doubleLoot = FishingManager.hasNode(uuid, "tf_double_haul") && RNG.nextDouble() < 0.05;

        if (inLava) {
            giveLavaLoot(player, doubleLoot);
        } else {
            giveWaterLoot(player, doubleLoot);
        }

        int count = CATCH_COUNTER.merge(pid, 1, Integer::sum);
        if (FishingManager.hasNode(uuid, "tf_fathom_fortune") && count % 50 == 0) {
            giveRareTreasure(player, uuid);
        }
    }

    private static void giveWaterLoot(ServerPlayer player, boolean doubleLoot) {
        int count = doubleLoot ? 2 : 1;
        float roll = RNG.nextFloat();
        if (roll < 0.50f) {
            player.addItem(new ItemStack(Items.COD, count));
        } else if (roll < 0.75f) {
            player.addItem(new ItemStack(Items.SALMON, count));
        } else if (roll < 0.90f) {
            player.addItem(new ItemStack(Items.TROPICAL_FISH, count));
        } else {
            player.addItem(new ItemStack(Items.PUFFERFISH, count));
        }
    }

    private static void giveLavaLoot(ServerPlayer player, boolean doubleLoot) {
        int count = doubleLoot ? 2 : 1;
        float roll = RNG.nextFloat();
        if (roll < 0.40f) {
            player.addItem(new ItemStack(Items.MAGMA_CREAM, count));
        } else if (roll < 0.65f) {
            player.addItem(new ItemStack(Items.BLAZE_POWDER, count));
        } else if (roll < 0.85f) {
            player.addItem(new ItemStack(Items.PRISMARINE_SHARD, count));
        } else {
            player.addItem(new ItemStack(Items.GHAST_TEAR, count));
        }
    }

    private static void rollTreasure(ServerPlayer player, String uuid, FishingManager.PlayerFishingData data, boolean inLava) {
        int coins;
        if (FishingManager.hasNode(uuid, "tf_golden_reel")) {
            coins = 1000 + RNG.nextInt(9000);
        } else {
            coins = 500 + RNG.nextInt(4500);
        }

        if (FishingManager.hasNode(uuid, "tf_magnet_line")) {
            CoinManager.giveCoinsQuiet(player, coins);
            player.sendSystemMessage(Component.literal("Treasure! +" + String.format("%,d", coins) + " coins")
                    .withStyle(ChatFormatting.GOLD));
        }

        if (FishingManager.hasNode(uuid, "tf_sunken_vault") && RNG.nextDouble() < 0.10) {
            player.addItem(new ItemStack(Items.DIAMOND, 1 + RNG.nextInt(3)));
            player.sendSystemMessage(Component.literal("Sunken Vault find!").withStyle(ChatFormatting.LIGHT_PURPLE));
        } else {
            player.addItem(new ItemStack(inLava ? Items.GOLD_INGOT : Items.IRON_INGOT, 1 + RNG.nextInt(3)));
        }

        data.fishCaught++;
        BuffProgressManager.recordFishCaught(player);
        FishingManager.addXp(player, 25 + RNG.nextInt(76));
        if (data.rarestCatch.equals("None")) data.rarestCatch = "Treasure Haul";
    }

    private static void giveRareTreasure(ServerPlayer player, String uuid) {
        player.addItem(new ItemStack(Items.HEART_OF_THE_SEA, 1));
        CoinManager.giveCoinsQuiet(player, 25000);
        player.sendSystemMessage(Component.literal("Fathom Fortune! Heart of the Sea + 25,000 coins!")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        FishingManager.addXp(player, 200);
    }

    // Called from damage mixin/handler for bonus damage to sea creatures
    public static double getSeaCreatureDamageMultiplier(ServerPlayer attacker, LivingEntity target) {
        if (!target.entityTags().contains("fishing_sea_creature")) return 1.0;
        return FishingManager.getSeaCreatureDamageMultiplier(attacker.getUUID().toString());
    }

    // Called when any entity with tag dies
    public static void onEntityDeath(LivingEntity entity, ServerPlayer killer) {
        if (entity.entityTags().contains("fishing_sea_creature") && killer != null) {
            FishingManager.onSeaCreatureKilled(killer, entity);
        }
    }

    /**
     * Mixin entry: vanilla rod only. Rolls sea creature / treasure; cancels vanilla fish when those fire.
     */
    public static void onVanillaCatchingFish(FishingHook hook, BlockPos pos, CallbackInfo ci) {
        VANILLA_CATCH_CUSTOM.set(false);
        if (!(hook.level() instanceof ServerLevel world)) return;
        if (!(hook.getOwner() instanceof ServerPlayer player)) return;
        if (FishingRods.isFishingRod(player.getMainHandItem())) return;

        String uuid = player.getUUID().toString();
        FluidState fluid = world.getFluidState(pos);
        boolean inLava = fluid.is(FluidTags.LAVA);
        boolean inWater = fluid.is(FluidTags.WATER);
        if (!inWater && !inLava) return;
        if (inLava && !FishingManager.hasLavaFishing(uuid)) return;

        double seaCreatureChance = FishingManager.getSeaCreatureChance(uuid);
        if (RNG.nextDouble() < seaCreatureChance) {
            FishingManager.SeaCreature creature = FishingManager.rollSeaCreature(uuid, RNG);
            FishingManager.spawnSeaCreature(world, player, creature, pos);
            FishingManager.addXp(player, 5 + RNG.nextInt(16));
            VANILLA_CATCH_CUSTOM.set(true);
            hook.discard();
            ci.cancel();
            VANILLA_CATCH_CUSTOM.remove();
            return;
        }

        double treasureChance = FishingManager.getTreasureChance(uuid);
        if (treasureChance > 0 && RNG.nextDouble() < treasureChance) {
            FishingManager.PlayerFishingData data = FishingManager.getData(uuid);
            rollTreasure(player, uuid, data, inLava);
            VANILLA_CATCH_CUSTOM.set(true);
            hook.discard();
            ci.cancel();
            VANILLA_CATCH_CUSTOM.remove();
        }
    }

    /** Mixin TAIL: grant fishing skill progress for a normal vanilla fish catch. */
    public static void afterVanillaFishCaught(FishingHook hook) {
        try {
            if (VANILLA_CATCH_CUSTOM.get()) return;
            if (!(hook.getOwner() instanceof ServerPlayer player)) return;
            if (FishingRods.isFishingRod(player.getMainHandItem())) return;
            BuffProgressManager.recordFishCaught(player);
            FishingManager.addXp(player, 5 + RNG.nextInt(12));
        } finally {
            VANILLA_CATCH_CUSTOM.remove();
        }
    }
}
