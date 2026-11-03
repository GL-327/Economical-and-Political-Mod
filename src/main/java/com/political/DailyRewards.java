package com.political;


import com.political.economy.CoinManager;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Daily Rewards System
 * Players can claim rewards every day with increasing bonuses for consecutive days
 */
public class DailyRewards {

    private static final Map<String, Long> lastClaimTime = new ConcurrentHashMap<>();
    private static final Map<String, Integer> consecutiveDays = new ConcurrentHashMap<>();

    // Rewards for each consecutive day (coins)
    private static final long[] DAILY_REWARDS = {
            250,       // Day 1
            500,       // Day 2
            1_000,     // Day 3
            2_500,     // Day 4
            5_000,     // Day 5
            10_000,    // Day 6
            25_000,    // Day 7 (weekly bonus)
    };

    private static final long CLAIM_COOLDOWN_MS = 24 * 60 * 60 * 1000; // 24 hours

    public static void loadFromNbt(net.minecraft.nbt.CompoundTag tag) {
        if (tag.contains("dailyRewards")) {
            net.minecraft.nbt.CompoundTag rewardsTag = tag.getCompoundOrEmpty("dailyRewards");
            for (String uuid : rewardsTag.keySet()) {
                net.minecraft.nbt.CompoundTag playerTag = rewardsTag.getCompoundOrEmpty(uuid);
                lastClaimTime.put(uuid, playerTag.getLong("lastClaim").orElse(0L));
                consecutiveDays.put(uuid, playerTag.getInt("consecutive").orElse(0));
            }
        }
    }

    public static void saveToNbt(net.minecraft.nbt.CompoundTag tag) {
        net.minecraft.nbt.CompoundTag rewardsTag = new net.minecraft.nbt.CompoundTag();
        for (String uuid : lastClaimTime.keySet()) {
            net.minecraft.nbt.CompoundTag playerTag = new net.minecraft.nbt.CompoundTag();
            playerTag.putLong("lastClaim", lastClaimTime.get(uuid));
            playerTag.putInt("consecutive", consecutiveDays.getOrDefault(uuid, 0));
            rewardsTag.put(uuid, playerTag);
        }
        tag.put("dailyRewards", rewardsTag);
    }

    public static boolean canClaim(String uuid) {
        long lastClaim = lastClaimTime.getOrDefault(uuid, 0L);
        return System.currentTimeMillis() - lastClaim >= CLAIM_COOLDOWN_MS;
    }

    public static long getTimeUntilNextClaim(String uuid) {
        long lastClaim = lastClaimTime.getOrDefault(uuid, 0L);
        long elapsed = System.currentTimeMillis() - lastClaim;
        return Math.max(0, CLAIM_COOLDOWN_MS - elapsed);
    }

    public static int getConsecutiveDays(String uuid) {
        return consecutiveDays.getOrDefault(uuid, 0);
    }

    public static void claimReward(ServerPlayer player) {
        String uuid = player.getUUID().toString();

        if (!canClaim(uuid)) {
            long timeLeft = getTimeUntilNextClaim(uuid);
            player.sendSystemMessage(Component.literal("§c✗ You've already claimed your daily reward!"), false);
            player.sendSystemMessage(Component.literal("§7Come back in " + formatTime(timeLeft)), false);
            return;
        }

        // Check if streak should reset (missed more than 48 hours)
        long lastClaim = lastClaimTime.getOrDefault(uuid, 0L);
        if (lastClaim > 0 && System.currentTimeMillis() - lastClaim > CLAIM_COOLDOWN_MS * 2) {
            consecutiveDays.put(uuid, 0);
        }

        int day = consecutiveDays.getOrDefault(uuid, 0);
        day = (day % 7) + 1; // Cycle through days 1-7
        consecutiveDays.put(uuid, day);

        long reward = DAILY_REWARDS[day - 1];
        CoinManager.giveCoins(uuid, reward);
        lastClaimTime.put(uuid, System.currentTimeMillis());

        player.sendSystemMessage(Component.literal("§6═══════════════════════════════════"), false);
        player.sendSystemMessage(Component.literal("§6§l🎁 DAILY REWARD CLAIMED!"), false);
        player.sendSystemMessage(Component.literal("§eDay " + day + " - §6" + String.format("%,d", reward) + " coins!"), false);
        if (day == 7) {
            player.sendSystemMessage(Component.literal("§d✨ Weekly bonus! Streak continues!"), false);
        }
        player.sendSystemMessage(Component.literal("§7Consecutive days: §a" + consecutiveDays.get(uuid)), false);
        player.sendSystemMessage(Component.literal("§6═══════════════════════════════════"), false);

        // Track achievement
        CivilCraftAchievements.trackCoinsEarned(player, reward);

        DataManager.save(PoliticalServer.server);
    }

    public static void openGui(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("§6§l🎁 Daily Rewards"));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.black()).setName(Component.literal("")).build());
        }

        String uuid = player.getUUID().toString();
        int currentDay = consecutiveDays.getOrDefault(uuid, 0) % 7 + 1;
        boolean canClaimNow = canClaim(uuid);

        // Current day reward
        gui.setSlot(11, new GuiElementBuilder(canClaimNow ? Items.CHEST : Items.BARRIER)
                .setName(canClaimNow ? Component.literal("§a§lClaim Today's Reward") : Component.literal("§c§lAlready Claimed"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eDay " + currentDay))
                .addLoreLine(Component.literal("§6Reward: " + String.format("%,d", DAILY_REWARDS[currentDay - 1]) + " coins"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(canClaimNow ? "§aClick to claim!" : "§7Come back tomorrow!"))
                .setCallback((index, type, containerInput, slotGui) -> {
                    if (canClaimNow) {
                        claimReward(player);
                        openGui(player);
                    }
                })
                .build());

        // Streak info
        gui.setSlot(13, new GuiElementBuilder(Items.CLOCK)
                .setName(Component.literal("§e§lYour Streak"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Consecutive days: §a" + consecutiveDays.getOrDefault(uuid, 0)))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Next reward in:"))
                .addLoreLine(Component.literal("§f" + formatTime(getTimeUntilNextClaim(uuid))))
                .build());

        // Weekly bonus preview
        gui.setSlot(15, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Component.literal("§d§lWeekly Bonus (Day 7)"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6" + String.format("%,d", DAILY_REWARDS[6]) + " coins!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Keep your streak going!"))
                .build());

        // Close
        gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("§cClose"))
                .setCallback((index, type, containerInput, slotGui) -> player.closeContainer())
                .build());

        gui.open();
    }

    private static String formatTime(long ms) {
        long hours = ms / (60 * 60 * 1000);
        long minutes = (ms % (60 * 60 * 1000)) / (60 * 1000);
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }
        return minutes + " minutes";
    }
}
