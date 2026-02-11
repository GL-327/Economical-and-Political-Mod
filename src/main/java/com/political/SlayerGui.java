package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class SlayerGui {

    // ============================================================
    // MAIN SLAYER MENU
    // ============================================================

    public static void openMainMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⚔ Slayer Bulletin Board ⚔"));

        String uuid = player.getUuidAsString();

        // Fill background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Header - Total Slayer Level
        int totalLevel = SlayerData.getTotalSlayerLevel(uuid);
        int totalBosses = SlayerData.getTotalBossesKilled(uuid);

        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("⚔ Your Slayer Stats ⚔")
                        .formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Total Slayer Level: " + totalLevel)
                        .formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Bosses Killed: " + totalBosses)
                        .formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Select a slayer type below!")
                        .formatted(Formatting.GREEN))
                .glow()
                .build());

        // Active quest status
        SlayerManager.ActiveQuest activeQuest = SlayerManager.getActiveQuest(player);
        if (activeQuest != null) {
            SlayerManager.TierConfig config = activeQuest.getConfig();
            gui.setSlot(49, new GuiElementBuilder(Items.COMPASS)
                    .setName(Text.literal("⚡ Active Quest")
                            .formatted(Formatting.YELLOW, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Type: " + activeQuest.slayerType.displayName)
                            .formatted(activeQuest.slayerType.color))
                    .addLoreLine(Text.literal("Tier: " + activeQuest.tier)
                            .formatted(Formatting.WHITE))
                    .addLoreLine(Text.literal("Progress: " + activeQuest.killCount + "/" + config.killsRequired)
                            .formatted(Formatting.GREEN))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(activeQuest.bossSpawned
                            ? Text.literal("☠ BOSS SPAWNED!").formatted(Formatting.RED, Formatting.BOLD)
                            : Text.literal("Kill more mobs!").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Click to cancel quest").formatted(Formatting.DARK_GRAY))
                    .setCallback((index, type, action) -> {
                        SlayerManager.cancelQuest(player);
                        openMainMenu(player);
                    })
                    .glow()
                    .build());
        }

        // Slayer type icons - arranged in 2 rows
        int[] slots = {19, 21, 23, 25, 29, 31, 33};
        SlayerManager.SlayerType[] types = SlayerManager.SlayerType.values();

        for (int i = 0; i < types.length && i < slots.length; i++) {
            SlayerManager.SlayerType type = types[i];
            int level = SlayerData.getSlayerLevel(uuid, type);
            long xp = SlayerData.getSlayerXp(uuid, type);
            int bossesKilled = SlayerData.getBossesKilled(uuid, type);
            double progress = SlayerData.getProgressToNextLevel(uuid, type);

            // Progress bar
            String progressBar = createProgressBar(progress, 10);

            List<Text> lore = new ArrayList<>();
            lore.add(Text.literal(""));
            lore.add(Text.literal("Level: " + level + " / " + SlayerManager.MAX_LEVEL)
                    .formatted(Formatting.YELLOW));
            lore.add(Text.literal("XP: " + formatNumber(xp))
                    .formatted(Formatting.AQUA));
// After building the existing lore (level, XP, bosses killed, difficulty stars)
// ADD THIS SECTION:

            lore.add(Text.literal(""));

// Check if unlocked
            boolean unlocked = SlayerManager.hasUnlockedSlayer(uuid, type);
            if (!unlocked) {
                String req = SlayerManager.getUnlockRequirement(type);
                lore.add(Text.literal("🔒 LOCKED").formatted(Formatting.RED, Formatting.BOLD));
                lore.add(Text.literal("Requires: " + req).formatted(Formatting.DARK_RED));
            } else {
                lore.add(Text.literal("✔ UNLOCKED").formatted(Formatting.GREEN));
            }

            lore.add(Text.literal(""));
            if (unlocked) {
                lore.add(Text.literal("Click to view quests!").formatted(Formatting.GREEN));
            } else {
                lore.add(Text.literal("Complete previous slayer first!").formatted(Formatting.GRAY));
            }

            final SlayerManager.SlayerType finalType = type;
            final boolean finalUnlocked = unlocked;
            gui.setSlot(slots[i], new GuiElementBuilder(type.icon)
                    .setName(Text.literal(type.displayName + " Slayer")
                            .formatted(type.color, Formatting.BOLD))
                    .setLore(lore)
                    .setCallback((index, clickType, action) -> {
                        if (finalUnlocked) {
                            openSlayerTypeMenu(player, finalType);
                        } else {
                            player.sendMessage(Text.literal("✖ " + SlayerManager.getUnlockRequirement(finalType) + " first!")
                                    .formatted(Formatting.RED), false);
                        }
                    })
                    .build());        }

        // Coins display
        int coins = CoinManager.getCoins(player);
        gui.setSlot(45, new GuiElementBuilder(Items.SUNFLOWER)
                .setName(Text.literal("Your Coins: " + formatNumber(coins))
                        .formatted(Formatting.YELLOW, Formatting.BOLD))
                .build());

        gui.open();
    }

    // ============================================================
    // SLAYER TYPE MENU - Shows tiers for a specific slayer
    // ============================================================

    public static void openSlayerTypeMenu(ServerPlayerEntity player, SlayerManager.SlayerType type) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⚔ " + type.displayName + " Slayer ⚔"));

        String uuid = player.getUuidAsString();
        int playerLevel = SlayerData.getSlayerLevel(uuid, type);
        int playerCoins = CoinManager.getCoins(player);
        boolean hasActiveQuest = SlayerManager.hasActiveQuest(player);

        // Fill background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Header
        long xp = SlayerData.getSlayerXp(uuid, type);
        gui.setSlot(4, new GuiElementBuilder(type.icon)
                .setName(Text.literal(type.displayName + " Slayer")
                        .formatted(type.color, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Level: " + playerLevel)
                        .formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("XP: " + formatNumber(xp))
                        .formatted(Formatting.AQUA))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Boss: " + type.bossName)
                        .formatted(type.color))
                .glow()
                .build());

        // Tier buttons
        int[] tierSlots = {20, 21, 22, 23, 24};

        for (int tier = 1; tier <= 5; tier++) {
            SlayerManager.TierConfig config = SlayerManager.getTierConfig(tier);
            if (config == null) continue;

            boolean hasLevel = playerLevel >= config.minLevel;
            boolean hasCoins = playerCoins >= config.coinCost;
            boolean canStart = hasLevel && hasCoins && !hasActiveQuest;

            double actualHp = config.getActualHp(type);
            double actualDmg = config.getActualDamage(type);

            List<Text> lore = new ArrayList<>();
            lore.add(Text.literal(""));
            lore.add(Text.literal("Boss Stats:").formatted(Formatting.RED));
            lore.add(Text.literal("  ❤ HP: " + formatNumber((long) actualHp))
                    .formatted(Formatting.WHITE));
            lore.add(Text.literal("  ⚔ Damage: " + formatNumber((long) actualDmg))
                    .formatted(Formatting.WHITE));

            if (config.damageResistance > 0) {
                lore.add(Text.literal("  🛡 Resistance: " + (int)(config.damageResistance * 100) + "%")
                        .formatted(Formatting.GOLD));
            }

            lore.add(Text.literal(""));
            lore.add(Text.literal("Requirements:").formatted(Formatting.YELLOW));
            lore.add(Text.literal("  Kills: " + config.killsRequired + " " + type.displayName + "s")
                    .formatted(Formatting.GRAY));
            lore.add(Text.literal("  Cost: " + formatNumber(config.coinCost) + " coins")
                    .formatted(hasCoins ? Formatting.GREEN : Formatting.RED));
            lore.add(Text.literal("  Level: " + config.minLevel)
                    .formatted(hasLevel ? Formatting.GREEN : Formatting.RED));

            lore.add(Text.literal(""));
            lore.add(Text.literal("Rewards:").formatted(Formatting.AQUA));
            lore.add(Text.literal("  +" + config.xpReward + " Slayer XP")
                    .formatted(Formatting.WHITE));

            if (config.miniBossCount > 0) {
                lore.add(Text.literal("  Mini-bosses: " + config.miniBossCount)
                        .formatted(Formatting.GRAY));
            }

            lore.add(Text.literal(""));

            if (hasActiveQuest) {
                lore.add(Text.literal("✖ Already have active quest!")
                        .formatted(Formatting.RED));
            } else if (!hasLevel) {
                lore.add(Text.literal("✖ Requires Level " + config.minLevel)
                        .formatted(Formatting.RED));
            } else if (!hasCoins) {
                lore.add(Text.literal("✖ Not enough coins!")
                        .formatted(Formatting.RED));
            } else {
                lore.add(Text.literal("✔ Click to start!")
                        .formatted(Formatting.GREEN));
            }

            Formatting tierColor = canStart ? Formatting.GREEN : Formatting.RED;
            final int finalTier = tier;

            gui.setSlot(tierSlots[tier - 1], new GuiElementBuilder(getTierItem(tier))
                    .setName(Text.literal("Tier " + toRoman(tier))
                            .formatted(tierColor, Formatting.BOLD))
                    .setLore(lore)
                    .setCallback((index, clickType, action) -> {
                        if (canStart) {
                            if (SlayerManager.startQuest(player, type, finalTier)) {
                                player.closeHandledScreen();
                            }
                        }
                    })
                    .build());
        }

        // Rewards button
        gui.setSlot(40, new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
                .setName(Text.literal("Level Rewards")
                        .formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("View rewards for leveling up!")
                        .formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to view!")
                        .formatted(Formatting.GREEN))
                .setCallback((index, clickType, action) -> {
                    openRewardsMenu(player, type);
                })
                .build());

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back")
                        .formatted(Formatting.YELLOW))
                .setCallback((index, clickType, action) -> {
                    openMainMenu(player);
                })
                .build());

        gui.open();
    }

    // ============================================================
    // REWARDS MENU - Shows level-up rewards
    // ============================================================

    public static void openRewardsMenu(ServerPlayerEntity player, SlayerManager.SlayerType type) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⚔ " + type.displayName + " Rewards ⚔"));

        String uuid = player.getUuidAsString();
        int playerLevel = SlayerData.getSlayerLevel(uuid, type);

        // Fill background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Level 1-12 rewards displayed in grid
        int[] levelSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23};

        for (int level = 1; level <= SlayerManager.MAX_LEVEL; level++) {
            boolean unlocked = playerLevel >= level;
            long xpRequired = SlayerManager.XP_REQUIREMENTS[level - 1];
            int coinReward = SlayerManager.LEVEL_CREDIT_REWARDS[level - 1];

            List<Text> lore = new ArrayList<>();
            lore.add(Text.literal(""));
            lore.add(Text.literal("XP Required: " + formatNumber(xpRequired))
                    .formatted(Formatting.AQUA));
            lore.add(Text.literal(""));
            lore.add(Text.literal("Rewards:").formatted(Formatting.YELLOW));
            lore.add(Text.literal("  +" + formatNumber(coinReward) + " credits")
                    .formatted(Formatting.GOLD));

            // Future item rewards placeholder
            String itemReward = getItemRewardForLevel(type, level);
            if (itemReward != null) {
                lore.add(Text.literal("  + " + itemReward)
                        .formatted(Formatting.LIGHT_PURPLE));
            }

            lore.add(Text.literal(""));
            if (unlocked) {
                lore.add(Text.literal("✔ UNLOCKED")
                        .formatted(Formatting.GREEN, Formatting.BOLD));
            } else {
                long currentXp = SlayerData.getSlayerXp(uuid, type);
                long xpNeeded = xpRequired - currentXp;
                lore.add(Text.literal("✖ LOCKED")
                        .formatted(Formatting.RED));
                lore.add(Text.literal("Need " + formatNumber(xpNeeded) + " more XP")
                        .formatted(Formatting.GRAY));
            }

            gui.setSlot(levelSlots[level - 1], new GuiElementBuilder(unlocked ? Items.LIME_DYE : Items.GRAY_DYE)
                    .setName(Text.literal("Level " + level)
                            .formatted(unlocked ? Formatting.GREEN : Formatting.GRAY, Formatting.BOLD))
                    .setLore(lore)
                    .build());
        }

        // Slayer sword reward display
        int chunkCount = SlayerItems.countChunks(player, type);
        boolean canCraft = chunkCount >= 2;
        String chunkName = SlayerItems.getChunkName(type);

        gui.setSlot(31, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Text.literal(type.displayName + " Slayer Sword")
                        .formatted(type.color, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⚔ Slayer Weapon").formatted(Formatting.DARK_PURPLE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Deals 2x damage to " + type.displayName + "s").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Counts as 2 kills per kill").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Bypasses slayer boss resistance").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Recipe:").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("  2x " + chunkName).formatted(canCraft ? Formatting.GREEN : Formatting.RED))
                .addLoreLine(Text.literal("  You have: " + chunkCount).formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(canCraft
                        ? Text.literal("Click to craft!").formatted(Formatting.GREEN)
                        : Text.literal("Get chunks from boss drops").formatted(Formatting.DARK_GRAY))
                .setCallback((index, clickType, action) -> {
                    if (canCraft) {
                        SlayerItems.craftSlayerSword(player, type);
                        openRewardsMenu(player, type); // Refresh
                    }
                })
                .build());

        // Core drop info
        gui.setSlot(40, new GuiElementBuilder(type.icon)
                .setName(Text.literal(type.displayName + " Core")
                        .formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("RARE BOSS DROP")
                        .formatted(Formatting.GOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Drop Chances:").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("  T1: " + getCoreDropChance(type, 1) + "%")
                        .formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("  T2: " + getCoreDropChance(type, 2) + "%")
                        .formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("  T3: " + getCoreDropChance(type, 3) + "%")
                        .formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("  T4: " + getCoreDropChance(type, 4) + "%")
                        .formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("  T5: " + getCoreDropChance(type, 5) + "%")
                        .formatted(Formatting.GOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Used in crafting legendary gear!")
                        .formatted(Formatting.DARK_PURPLE))
                .glow()
                .build());

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back")
                        .formatted(Formatting.YELLOW))
                .setCallback((index, clickType, action) -> {
                    openSlayerTypeMenu(player, type);
                })
                .build());

        gui.open();
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    private static String createProgressBar(double progress, int length) {
        int filled = (int) (progress * length);
        StringBuilder bar = new StringBuilder("§a");
        for (int i = 0; i < length; i++) {
            if (i < filled) {
                bar.append("█");
            } else if (i == filled) {
                bar.append("§7█");
            } else {
                bar.append("░");
            }
        }
        return bar.toString();
    }

    private static String getDifficultyStars(SlayerManager.SlayerType type) {
        int stars = switch (type) {
            case ZOMBIE -> 1;
            case SPIDER -> 2;
            case SKELETON -> 2;
            case SLIME -> 3;
            case ENDERMAN -> 4;
            case WARDEN -> 5;
        };
        return "★".repeat(stars) + "☆".repeat(5 - stars);
    }

    private static net.minecraft.item.Item getTierItem(int tier) {
        return switch (tier) {
            case 1 -> Items.WOODEN_SWORD;
            case 2 -> Items.STONE_SWORD;
            case 3 -> Items.IRON_SWORD;
            case 4 -> Items.DIAMOND_SWORD;
            case 5 -> Items.NETHERITE_SWORD;
            default -> Items.WOODEN_SWORD;
        };
    }

    private static String toRoman(int num) {
        return switch (num) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(num);
        };
    }

    private static String formatNumber(long num) {
        if (num >= 1_000_000) {
            return String.format("%.1fM", num / 1_000_000.0);
        } else if (num >= 1_000) {
            return String.format("%.1fK", num / 1_000.0);
        }
        return String.valueOf(num);
    }

    private static String getItemRewardForLevel(SlayerManager.SlayerType type, int level) {
        // Future item rewards - return null for now, coins only [1]
        return switch (level) {
            case 3 -> type.displayName + " Slayer Sword Recipe";
            case 6 -> type.displayName + " Talisman";
            case 9 -> type.displayName + " Armor Piece";
            case 12 -> "LEGENDARY " + type.displayName + " Item";
            default -> null;
        };
    }

    private static String getCoreDropChance(SlayerManager.SlayerType type, int tier) {
        double baseChance = switch (type) {
            case ZOMBIE, SPIDER, SKELETON -> 0.5 + (tier * 0.5);
            case SLIME -> 0.4 + (tier * 0.4);
            case ENDERMAN -> 0.01 + (tier * 0.01); // Very rare [1]
            case WARDEN -> 0.5 + (tier * 0.1);     // 0.5% to 1% [1]
        };
        return String.format("%.2f", baseChance);
    }
}