package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class BountyGui {

    // ============================================================
    // MAIN BOUNTY BOARD MENU - Old West Style
    // ============================================================

    public static void openMainMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⚔ Bounty Board ⚔"));

        String uuid = player.getUuidAsString();

        // Old West style - Brown/wood themed background
        // Use brown stained glass and oak planks for wooden look
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BROWN_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Wood border effect (top and bottom rows)
        int[] borderSlots = {0, 1, 2, 3, 4, 5, 6, 7, 8, 45, 46, 47, 48, 49, 50, 51, 52, 53};
        for (int slot : borderSlots) {
            gui.setSlot(slot, new GuiElementBuilder(Items.OAK_PLANKS)
                    .setName(Text.literal(""))
                    .build());
        }

        // Side borders
        int[] sideBorders = {9, 17, 18, 26, 27, 35, 36, 44};
        for (int slot : sideBorders) {
            gui.setSlot(slot, new GuiElementBuilder(Items.SPRUCE_PLANKS)
                    .setName(Text.literal(""))
                    .build());
        }

        // Header - Wanted Poster style
        int totalLevel = SlayerData.getTotalSlayerLevel(uuid);
        int totalBosses = SlayerData.getTotalBossesKilled(uuid);

        gui.setSlot(4, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("☠ WANTED ☠")
                        .formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Your Bounty Hunter Rank: " + getBountyRank(totalLevel))
                        .formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Total Level: " + totalLevel)
                        .formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Outlaws Eliminated: " + totalBosses)
                        .formatted(Formatting.RED))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Select a bounty below!")
                        .formatted(Formatting.GREEN))
                .build());

        // Active bounty status
        SlayerManager.ActiveQuest activeQuest = SlayerManager.getActiveQuest(player);
        if (activeQuest != null) {
            SlayerManager.TierConfig config = activeQuest.getConfig();
            int killsReq = SlayerManager.getKillsRequired(activeQuest.slayerType, activeQuest.tier);

            gui.setSlot(49, new GuiElementBuilder(Items.COMPASS)
                    .setName(Text.literal("🎯 Active Bounty")
                            .formatted(Formatting.YELLOW, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Target: " + activeQuest.slayerType.displayName)
                            .formatted(activeQuest.slayerType.color))
                    .addLoreLine(Text.literal("Tier: " + activeQuest.tier)
                            .formatted(Formatting.WHITE))
                    .addLoreLine(Text.literal("Progress: " + activeQuest.killCount + "/" + killsReq)
                            .formatted(Formatting.GREEN))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(activeQuest.bossSpawned
                            ? Text.literal("☠ TARGET LOCATED!").formatted(Formatting.RED, Formatting.BOLD)
                            : Text.literal("Hunt more targets!").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Click to abandon bounty").formatted(Formatting.DARK_GRAY))
                    .setCallback((index, type, action) -> {
                        SlayerManager.cancelQuest(player);
                        openMainMenu(player);
                    })
                    .glow()
                    .build());
        }

        // Bounty type icons - arranged like wanted posters
        int[] slots = {19, 21, 23, 25, 28, 30, 32, 34};
        SlayerManager.SlayerType[] types = SlayerManager.SlayerType.values();

        for (int i = 0; i < types.length && i < slots.length; i++) {
            SlayerManager.SlayerType type = types[i];
            int level = SlayerData.getSlayerLevel(uuid, type);
            long xp = SlayerData.getSlayerXp(uuid, type);
            int bossesKilled = SlayerData.getBossesKilled(uuid, type);
            double progress = SlayerData.getProgressToNextLevel(uuid, type);

            String progressBar = createProgressBar(progress, 10);

            List<Text> lore = new ArrayList<>();
            lore.add(Text.literal(""));
            lore.add(Text.literal("━━━━━━━━━━━━━━━").formatted(Formatting.DARK_GRAY));
            lore.add(Text.literal("WANTED: " + type.bossName).formatted(type.color, Formatting.BOLD));
            lore.add(Text.literal("━━━━━━━━━━━━━━━").formatted(Formatting.DARK_GRAY));
            lore.add(Text.literal(""));
            lore.add(Text.literal("Bounty Level: " + level + " / " + SlayerManager.MAX_LEVEL)
                    .formatted(Formatting.YELLOW));
            lore.add(Text.literal("Experience: " + formatNumber(xp))
                    .formatted(Formatting.AQUA));

            if (level < SlayerManager.MAX_LEVEL) {
                lore.add(Text.literal(progressBar).formatted(Formatting.GREEN));
            } else {
                lore.add(Text.literal("✔ LEGENDARY HUNTER").formatted(Formatting.GOLD, Formatting.BOLD));
            }

            lore.add(Text.literal(""));
            lore.add(Text.literal("Targets Eliminated: " + bossesKilled).formatted(Formatting.GRAY));
            lore.add(Text.literal(""));
            lore.add(Text.literal("Danger Level: " + getDangerStars(type)).formatted(Formatting.RED));

            // Unlock check
            boolean unlocked = SlayerManager.hasUnlockedSlayer(uuid, type);
            lore.add(Text.literal(""));
            if (!unlocked) {
                String req = SlayerManager.getUnlockRequirement(type);
                lore.add(Text.literal("🔒 LOCKED").formatted(Formatting.RED, Formatting.BOLD));
                lore.add(Text.literal("Requires: " + req).formatted(Formatting.DARK_RED));
            } else {
                lore.add(Text.literal("✔ AVAILABLE").formatted(Formatting.GREEN));
                lore.add(Text.literal("Click to view bounties!").formatted(Formatting.GREEN));
            }

            final SlayerManager.SlayerType finalType = type;
            final boolean finalUnlocked = unlocked;

            // Use skull or head items for wanted poster effect
            gui.setSlot(slots[i], new GuiElementBuilder(getWantedPosterItem(type))
                    .setName(Text.literal("☠ " + type.displayName + " Bounty")
                            .formatted(type.color, Formatting.BOLD))
                    .setLore(lore)
                    .setCallback((index, clickType, action) -> {
                        if (finalUnlocked) {
                            openBountyTypeMenu(player, finalType);
                        } else {
                            player.sendMessage(Text.literal("✖ " + SlayerManager.getUnlockRequirement(finalType) + " first!")
                                    .formatted(Formatting.RED), false);
                        }
                    })
                    .build());
        }
// In BountyGui.java - add this button in the appropriate slot

// T2 Crafting button
        int t2CraftSlot = 53; // Bottom right corner
        gui.setSlot(t2CraftSlot, new GuiElementBuilder(Items.SMITHING_TABLE)
                .setName(Text.literal("⚒ T2 Armor Crafting").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Upgrade your T1 bounty armor").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("to powerful T2 variants!").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Requirements:").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("• T1 armor piece").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("• Bounty cores").formatted(Formatting.AQUA))
                .addLoreLine(Text.literal("• Special materials").formatted(Formatting.LIGHT_PURPLE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to open").formatted(Formatting.GREEN))
                .setCallback((idx, clickType, action) -> {
                    T2CraftingGui.open(player);
                })
                .build());
        // Coins display (reward money)
        int coins = CoinManager.getCoins(player);
        gui.setSlot(45, new GuiElementBuilder(Items.GOLD_NUGGET)
                .setName(Text.literal("💰 Your Funds: " + formatNumber(coins))
                        .formatted(Formatting.GOLD, Formatting.BOLD))
                .build());

        gui.open();
    }

    // ============================================================
    // BOUNTY TYPE MENU
    // ============================================================

    public static void openBountyTypeMenu(ServerPlayerEntity player, SlayerManager.SlayerType type) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("☠ " + type.displayName + " Bounties ☠"));

        String uuid = player.getUuidAsString();
        int playerLevel = SlayerData.getSlayerLevel(uuid, type);
        int playerCoins = CoinManager.getCoins(player);
        boolean hasActiveQuest = SlayerManager.hasActiveQuest(player);

        // Wood-themed background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.SPRUCE_PLANKS)
                    .setName(Text.literal(""))
                    .build());
        }

        // Center area with brown glass
        int[] centerSlots = {19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
        for (int slot : centerSlots) {
            gui.setSlot(slot, new GuiElementBuilder(Items.BROWN_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Header - Wanted poster
        gui.setSlot(4, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("☠ WANTED: " + type.bossName + " ☠")
                        .formatted(type.color, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Your Level: " + playerLevel)
                        .formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("\"Dead or... well, just dead.\"")
                        .formatted(Formatting.GRAY, Formatting.ITALIC))
                .glow()
                .build());

        // Tier buttons (bounty contracts)
        int[] tierSlots = {20, 21, 22, 23, 24};

        for (int tier = 1; tier <= 5; tier++) {
            SlayerManager.TierConfig config = SlayerManager.getTierConfig(tier);
            if (config == null) continue;

            boolean hasLevel = playerLevel >= config.minLevel;
            boolean hasCoins = playerCoins >= config.coinCost;
            boolean canStart = hasLevel && hasCoins && !hasActiveQuest;

            double actualHp = config.getActualHp(type);
            int killsReq = SlayerManager.getKillsRequired(type, tier);

            List<Text> lore = new ArrayList<>();
            lore.add(Text.literal(""));
            lore.add(Text.literal("━━━ BOUNTY CONTRACT ━━━").formatted(Formatting.GOLD));
            lore.add(Text.literal(""));
            lore.add(Text.literal("Target Stats:").formatted(Formatting.RED));
            lore.add(Text.literal("  ❤ Health: " + formatNumber((long) actualHp))
                    .formatted(Formatting.WHITE));
            lore.add(Text.literal("  ⚔ Danger: " + getDangerStars(type))
                    .formatted(Formatting.WHITE));

            if (config.damageResistance > 0) {
                lore.add(Text.literal("  🛡 Armor: " + (int) (config.damageResistance * 100) + "%")
                        .formatted(Formatting.GOLD));
            }

            lore.add(Text.literal(""));
            lore.add(Text.literal("Contract Terms:").formatted(Formatting.YELLOW));
            lore.add(Text.literal("  Kills Required: " + killsReq)
                    .formatted(Formatting.GRAY));
            lore.add(Text.literal("  Bounty Fee: " + formatNumber(config.coinCost) + " coins")
                    .formatted(hasCoins ? Formatting.GREEN : Formatting.RED));
            lore.add(Text.literal("  Min Level: " + config.minLevel)
                    .formatted(hasLevel ? Formatting.GREEN : Formatting.RED));

            lore.add(Text.literal(""));
            lore.add(Text.literal("Reward:").formatted(Formatting.AQUA));
            lore.add(Text.literal("  +" + config.xpReward + " Bounty XP")
                    .formatted(Formatting.WHITE));

            lore.add(Text.literal(""));

            if (hasActiveQuest) {
                lore.add(Text.literal("✖ You have an active bounty!")
                        .formatted(Formatting.RED));
            } else if (!hasLevel) {
                lore.add(Text.literal("✖ Requires Level " + config.minLevel)
                        .formatted(Formatting.RED));
            } else if (!hasCoins) {
                lore.add(Text.literal("✖ Not enough coins!")
                        .formatted(Formatting.RED));
            } else {
                lore.add(Text.literal("✔ Click to accept contract!")
                        .formatted(Formatting.GREEN));
            }

            Formatting tierColor = canStart ? Formatting.GREEN : Formatting.RED;
            final int finalTier = tier;
            final boolean finalCanStart = canStart;  // Capture for lambda

            lore.add(Text.literal(""));
            lore.add(Text.literal("Left-click: Start bounty").formatted(Formatting.GREEN));
            lore.add(Text.literal("Right-click: View drops").formatted(Formatting.AQUA));

            gui.setSlot(tierSlots[tier - 1], new GuiElementBuilder(Items.PAPER)
                    .setName(Text.literal("Tier " + tier + " Contract")
                            .formatted(tierColor, Formatting.BOLD))
                    .setLore(lore)
                    .setCallback((index, clickType, action) -> {
                        // Right-click shows drop table [1]
                        if (clickType.isRight) {
                            openDropTableGui(player, type, finalTier);
                        }
                        // Left-click starts the bounty
                        else if (finalCanStart) {
                            player.closeHandledScreen();
                            SlayerManager.startQuest(player, type, finalTier);
                        }
                    })
                    .build());
        }


        // Rewards button
        gui.setSlot(40, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Text.literal("💰 Bounty Rewards")
                        .formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("View rewards for leveling up!")
                        .formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to view!")
                        .formatted(Formatting.GREEN))
                .setCallback((idx, clickType, action) -> openRewardsMenu(player, type))
                .build());

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Bounty Board")
                        .formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openMainMenu(player))
                .build());

        gui.open();
    }
    // ============================================================
// DROP TABLE GUI - Shows boss loot and drop chances
// ============================================================
    public static void openDropTableGui(ServerPlayerEntity player, SlayerManager.SlayerType type, int tier) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("☠ " + type.bossName + " T" + tier + " Drops"));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.DRAGON_HEAD)
                .setName(Text.literal("☠ " + type.bossName + " Loot Table")
                        .formatted(type.color, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Tier " + tier + " Drop Rates").formatted(Formatting.YELLOW))
                .glow()
                .build());

        // Drop rates scale with tier
        double coreChance = switch (tier) {
            case 1 -> 2.0;
            case 2 -> 6.0;
            case 3 -> 11.0;
            case 4 -> 16.0;
            case 5 -> 20.0;
            default -> 0.0;
        };

        double chunkChance = switch (tier) {
            case 1 -> 5.0;
            case 2 -> 10.0;
            case 3 -> 15.0;
            case 4 -> 20.0;
            case 5 -> 25.0;
            default -> 0.0;
        };

        double swordChance = switch (tier) {
            case 1 -> 0.5;
            case 2 -> 1.0;
            case 3 -> 2.0;
            case 4 -> 4.0;
            case 5 -> 6.0;
            default -> 0.0;
        };

        // Core drop
        int slot = 19;
        gui.setSlot(slot, new GuiElementBuilder(type.icon)
                .setName(Text.literal("✦ " + type.displayName + " Core")
                        .formatted(type.color, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Drop Chance: " + coreChance + "%")
                        .formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Used for crafting powerful").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("bounty equipment.").formatted(Formatting.GRAY))
                .glow()
                .build());

        // Chunk drop
        gui.setSlot(21, new GuiElementBuilder(Items.PRISMARINE_SHARD)
                .setName(Text.literal(SlayerItems.getChunkName(type))
                        .formatted(type.color))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Drop Chance: " + chunkChance + "%")
                        .formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Crafting material for").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("slayer swords.").formatted(Formatting.GRAY))
                .build());

        // Rare sword drop (T3+)
        if (tier >= 3) {
            gui.setSlot(23, new GuiElementBuilder(Items.IRON_SWORD)
                    .setName(Text.literal(type.displayName + " Bounty Sword")
                            .formatted(type.color, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Drop Chance: " + swordChance + "%")
                            .formatted(Formatting.LIGHT_PURPLE))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("RARE DROP!").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                    .glow()
                    .build());
        }

        // Coins reward (guaranteed)
        int coinReward = switch (tier) {
            case 1 -> 50;
            case 2 -> 150;
            case 3 -> 400;
            case 4 -> 1000;
            case 5 -> 3000;
            default -> 0;
        };

        gui.setSlot(25, new GuiElementBuilder(Items.GOLD_NUGGET)
                .setName(Text.literal("💰 Coin Reward")
                        .formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Guaranteed: " + coinReward + " coins")
                        .formatted(Formatting.YELLOW))
                .build());

        // XP reward
        SlayerManager.TierConfig config = SlayerManager.getTierConfig(tier);
        gui.setSlot(31, new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
                .setName(Text.literal("✨ Bounty XP")
                        .formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Guaranteed: +" + config.xpReward + " XP")
                        .formatted(Formatting.GREEN))
                .build());

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Tiers")
                        .formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openBountyTypeMenu(player, type))
                .build());

        // Start bounty button
        gui.setSlot(49, new GuiElementBuilder(Items.LIME_CONCRETE)
                .setName(Text.literal("▶ Start This Bounty")
                        .formatted(Formatting.GREEN, Formatting.BOLD))
                .setCallback((idx, clickType, action) -> {
                    player.closeHandledScreen();
                    SlayerManager.startQuest(player, type, tier);
                })
                .build());

        gui.open();
    }
    // ============================================================
    // REWARDS MENU
    // ============================================================

    public static void openRewardsMenu(ServerPlayerEntity player, SlayerManager.SlayerType type) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("💰 " + type.displayName + " Bounty Rewards 💰"));

        String uuid = player.getUuidAsString();
        int playerLevel = SlayerData.getSlayerLevel(uuid, type);

        // Wood background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.SPRUCE_PLANKS)
                    .setName(Text.literal("")).build());
        }

        // Level rewards displayed in grid
        int[] levelSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23};

        for (int level = 1; level <= SlayerManager.MAX_LEVEL; level++) {
            boolean unlocked = playerLevel >= level;
            long xpRequired = SlayerManager.XP_REQUIREMENTS[level - 1];
            int creditReward = SlayerManager.LEVEL_CREDIT_REWARDS[level - 1];

            List<Text> lore = new ArrayList<>();
            lore.add(Text.literal(""));
            lore.add(Text.literal("XP Required: " + formatNumber(xpRequired))
                    .formatted(Formatting.AQUA));
            lore.add(Text.literal(""));
            lore.add(Text.literal("Rewards:").formatted(Formatting.YELLOW));
            lore.add(Text.literal("  +" + formatNumber(creditReward) + " credits")
                    .formatted(Formatting.AQUA));

            lore.add(Text.literal(""));
            if (unlocked) {
                lore.add(Text.literal("✔ CLAIMED")
                        .formatted(Formatting.GREEN, Formatting.BOLD));
            } else {
                long currentXp = SlayerData.getSlayerXp(uuid, type);
                long xpNeeded = xpRequired - currentXp;
                lore.add(Text.literal("✖ LOCKED")
                        .formatted(Formatting.RED));
                lore.add(Text.literal("Need " + formatNumber(xpNeeded) + " more XP")
                        .formatted(Formatting.GRAY));
            }

            gui.setSlot(levelSlots[level - 1], new GuiElementBuilder(unlocked ? Items.GOLD_NUGGET : Items.COAL)
                    .setName(Text.literal("Level " + level + " Reward")
                            .formatted(unlocked ? Formatting.GOLD : Formatting.GRAY, Formatting.BOLD))
                    .setLore(lore)
                    .build());
        }

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back")
                        .formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openBountyTypeMenu(player, type))
                .build());

        gui.open();
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    private static String createProgressBar(double progress, int length) {
        int filled = (int) (progress * length);
        StringBuilder bar = new StringBuilder("§6[");
        for (int i = 0; i < length; i++) {
            if (i < filled) {
                bar.append("§a█");
            } else {
                bar.append("§8░");
            }
        }
        bar.append("§6]");
        return bar.toString();
    }

    private static String getDangerStars(SlayerManager.SlayerType type) {
        int stars = switch (type) {
            case ZOMBIE -> 1;
            case SPIDER -> 2;
            case SKELETON -> 2;
            case SLIME -> 3;
            case ENDERMAN -> 4;
            case WARDEN -> 5;
        };
        return "§c" + "★".repeat(stars) + "§7" + "☆".repeat(5 - stars);
    }

    private static String getBountyRank(int totalLevel) {
        if (totalLevel >= 60) return "§6§lLegendary Hunter";
        if (totalLevel >= 45) return "§cMaster Bounty Hunter";
        if (totalLevel >= 30) return "§eExpert Hunter";
        if (totalLevel >= 15) return "§aJourneyman Hunter";
        if (totalLevel >= 5) return "§7Apprentice Hunter";
        return "§8Rookie";
    }

    private static net.minecraft.item.Item getWantedPosterItem(SlayerManager.SlayerType type) {
        return switch (type) {
            case ZOMBIE -> Items.ZOMBIE_HEAD;
            case SPIDER -> Items.SPIDER_EYE;
            case SKELETON -> Items.SKELETON_SKULL;
            case SLIME -> Items.SLIME_BALL;
            case ENDERMAN -> Items.ENDER_PEARL;
            case WARDEN -> Items.SCULK;
        };
    }

    private static net.minecraft.item.Item getContractItem(int tier) {
        return switch (tier) {
            case 1 -> Items.PAPER;
            case 2 -> Items.MAP;
            case 3 -> Items.FILLED_MAP;
            case 4 -> Items.WRITABLE_BOOK;
            case 5 -> Items.WRITTEN_BOOK;
            default -> Items.PAPER;
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
}