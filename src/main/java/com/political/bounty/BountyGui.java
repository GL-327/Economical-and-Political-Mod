package com.political.bounty;
import com.political.Contract;
import com.political.DataManager;



import com.political.economy.CoinManager;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.List;

public class BountyGui {

    // ============================================================
    // MAIN BOUNTY BOARD MENU - Old West Style
    // ============================================================

    public static void openMainMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("⚔ Bounty Board ⚔"));

        String uuid = player.getUUID().toString();

        // Old West style - Brown/wood themed background
        // Use brown stained glass and oak planks for wooden look
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.brown())
                    .setName(Component.literal(""))
                    .build());
        }

        // Wood border effect (top and bottom rows)
        int[] borderSlots = {0, 1, 2, 3, 4, 5, 6, 7, 8, 45, 46, 47, 48, 49, 50, 51, 52, 53};
        for (int slot : borderSlots) {
            gui.setSlot(slot, new GuiElementBuilder(Items.OAK_PLANKS)
                    .setName(Component.literal(""))
                    .build());
        }

        // Side borders
        int[] sideBorders = {9, 17, 18, 26, 27, 35, 36, 44};
        for (int slot : sideBorders) {
            gui.setSlot(slot, new GuiElementBuilder(Items.SPRUCE_PLANKS)
                    .setName(Component.literal(""))
                    .build());
        }

        // Header - Wanted Poster style
        int totalLevel = SlayerData.getTotalSlayerLevel(uuid);
        int totalBosses = SlayerData.getTotalBossesKilled(uuid);

        gui.setSlot(4, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("☠ WANTED ☠")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Your Bounty Hunter Rank: " + getBountyRank(totalLevel))
                        .withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("Total Level: " + totalLevel)
                        .withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Outlaws Eliminated: " + totalBosses)
                        .withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Select a bounty below!")
                        .withStyle(ChatFormatting.GREEN))
                .build());

        // Active bounty status
        SlayerManager.ActiveQuest activeQuest = SlayerManager.getActiveQuest(player);
        if (activeQuest != null) {
            SlayerManager.TierConfig config = activeQuest.getConfig();
            int killsReq = SlayerManager.getKillsRequired(activeQuest.slayerType, activeQuest.tier);

            gui.setSlot(49, new GuiElementBuilder(Items.COMPASS)
                    .setName(Component.literal("🎯 Active Bounty")
                            .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Target: " + activeQuest.slayerType.displayName)
                            .withStyle(activeQuest.slayerType.color))
                    .addLoreLine(Component.literal("Tier: " + activeQuest.tier)
                            .withStyle(ChatFormatting.WHITE))
                    .addLoreLine(Component.literal("Progress: " + activeQuest.killCount + "/" + killsReq)
                            .withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(activeQuest.bossSpawned
                            ? Component.literal("☠ TARGET LOCATED!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
                            : Component.literal("Hunt more targets!").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to abandon bounty").withStyle(ChatFormatting.DARK_GRAY))
                    .setCallback((index, type, containerInput, slotGui) -> {
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

            List<Component> lore = new ArrayList<>();
            lore.add(Component.literal(""));
            lore.add(Component.literal("━━━━━━━━━━━━━━━").withStyle(ChatFormatting.DARK_GRAY));
            lore.add(Component.literal("WANTED: " + type.bossName).withStyle(type.color, ChatFormatting.BOLD));
            lore.add(Component.literal("━━━━━━━━━━━━━━━").withStyle(ChatFormatting.DARK_GRAY));
            lore.add(Component.literal(""));
            lore.add(Component.literal("Bounty Level: " + level + " / " + SlayerManager.MAX_LEVEL)
                    .withStyle(ChatFormatting.YELLOW));
            lore.add(Component.literal("Experience: " + formatNumber(xp))
                    .withStyle(ChatFormatting.AQUA));

            if (level < SlayerManager.MAX_LEVEL) {
                lore.add(Component.literal(progressBar).withStyle(ChatFormatting.GREEN));
            } else {
                lore.add(Component.literal("✔ LEGENDARY HUNTER").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            }

            lore.add(Component.literal(""));
            lore.add(Component.literal("Targets Eliminated: " + bossesKilled).withStyle(ChatFormatting.GRAY));
            lore.add(Component.literal(""));
            lore.add(Component.literal("Danger Level: " + getDangerStars(type)).withStyle(ChatFormatting.RED));

            // Unlock check
            boolean unlocked = SlayerManager.hasUnlockedSlayer(uuid, type);
            lore.add(Component.literal(""));
            if (!unlocked) {
                String req = SlayerManager.getUnlockRequirement(type);
                lore.add(Component.literal("🔒 LOCKED").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                lore.add(Component.literal("Requires: " + req).withStyle(ChatFormatting.DARK_RED));
            } else {
                lore.add(Component.literal("✔ AVAILABLE").withStyle(ChatFormatting.GREEN));
                lore.add(Component.literal("Click to view bounties!").withStyle(ChatFormatting.GREEN));
            }

            final SlayerManager.SlayerType finalType = type;
            final boolean finalUnlocked = unlocked;

            // Use skull or head items for wanted poster effect
            gui.setSlot(slots[i], new GuiElementBuilder(getWantedPosterItem(type))
                    .setName(Component.literal("☠ " + type.displayName + " Bounty")
                            .withStyle(type.color, ChatFormatting.BOLD))
                    .setLore(lore)
                    .setCallback((index, clicktype, containerInput, slotGui) -> {
                        if (finalUnlocked) {
                            openBountyTypeMenu(player, finalType);
                        } else {
                            player.sendSystemMessage(Component.literal("✖ " + SlayerManager.getUnlockRequirement(finalType) + " first!")
                                    .withStyle(ChatFormatting.RED), false);
                        }
                    })
                    .build());
        }
        // Coins display (reward money)
        int coins = CoinManager.getCoins(player);
        gui.setSlot(45, new GuiElementBuilder(Items.GOLD_NUGGET)
                .setName(Component.literal("💰 Your Funds: " + formatNumber(coins))
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .build());

        gui.open();
    }

    // ============================================================
    // BOUNTY TYPE MENU
    // ============================================================

    public static void openBountyTypeMenu(ServerPlayer player, SlayerManager.SlayerType type) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("☠ " + type.displayName + " Bounties ☠"));

        String uuid = player.getUUID().toString();
        int playerLevel = SlayerData.getSlayerLevel(uuid, type);
        int playerCoins = CoinManager.getCoins(player);
        boolean hasActiveQuest = SlayerManager.hasActiveQuest(player);

        // Wood-themed background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.SPRUCE_PLANKS)
                    .setName(Component.literal(""))
                    .build());
        }

        // Center area with brown glass
        int[] centerSlots = {19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
        for (int slot : centerSlots) {
            gui.setSlot(slot, new GuiElementBuilder(Items.STAINED_GLASS_PANE.brown())
                    .setName(Component.literal(""))
                    .build());
        }

        // Header - Wanted poster
        gui.setSlot(4, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("☠ WANTED: " + type.bossName + " ☠")
                        .withStyle(type.color, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Your Level: " + playerLevel)
                        .withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("\"Dead or... well, just dead.\"")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
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

            double actualHp = config.getActualHp(type) * DataManager.getBossHpMultiplier(type);
            double actualDmg = config.getActualDamage(type) * DataManager.getBossDamageMultiplier(type);
            int killsReq = SlayerManager.getKillsRequired(type, tier);

            List<Component> lore = new ArrayList<>();
            lore.add(Component.literal(""));
            lore.add(Component.literal("━━━ BOUNTY CONTRACT ━━━").withStyle(ChatFormatting.GOLD));
            lore.add(Component.literal(""));
            lore.add(Component.literal("Target Stats:").withStyle(ChatFormatting.RED));
            lore.add(Component.literal("  ❤ Health: " + formatNumber((long) actualHp))
                    .withStyle(ChatFormatting.WHITE));
            lore.add(Component.literal("  ⚔ Damage: " + formatNumber((long) actualDmg))
                    .withStyle(ChatFormatting.WHITE));
            lore.add(Component.literal("  ⚔ Danger: " + getDangerStars(type))
                    .withStyle(ChatFormatting.WHITE));

            if (config.damageResistance > 0) {
                lore.add(Component.literal("  🛡 Armor: " + (int) (config.damageResistance * 100) + "%")
                        .withStyle(ChatFormatting.GOLD));
            }

            lore.add(Component.literal(""));
            lore.add(Component.literal("Contract Terms:").withStyle(ChatFormatting.YELLOW));
            lore.add(Component.literal("  Kills Required: " + killsReq)
                    .withStyle(ChatFormatting.GRAY));
            lore.add(Component.literal("  Bounty Fee: " + formatNumber(config.coinCost) + " coins")
                    .withStyle(hasCoins ? ChatFormatting.GREEN : ChatFormatting.RED));
            lore.add(Component.literal("  Min Level: " + config.minLevel)
                    .withStyle(hasLevel ? ChatFormatting.GREEN : ChatFormatting.RED));

            lore.add(Component.literal(""));
            lore.add(Component.literal("Reward:").withStyle(ChatFormatting.AQUA));
            int actualXp = (int) (config.xpReward * DataManager.getBossXpMultiplier(type));
            lore.add(Component.literal("  +" + actualXp + " Bounty XP")
                    .withStyle(ChatFormatting.WHITE));

            lore.add(Component.literal(""));

            if (hasActiveQuest) {
                lore.add(Component.literal("✖ You have an active bounty!")
                        .withStyle(ChatFormatting.RED));
            } else if (!hasLevel) {
                lore.add(Component.literal("✖ Requires Level " + config.minLevel)
                        .withStyle(ChatFormatting.RED));
            } else if (!hasCoins) {
                lore.add(Component.literal("✖ Not enough coins!")
                        .withStyle(ChatFormatting.RED));
            } else {
                lore.add(Component.literal("✔ Click to accept contract!")
                        .withStyle(ChatFormatting.GREEN));
            }

            ChatFormatting tierColor = canStart ? ChatFormatting.GREEN : ChatFormatting.RED;
            final int finalTier = tier;
            final boolean finalCanStart = canStart;  // Capture for lambda

            lore.add(Component.literal(""));
            lore.add(Component.literal("Left-click: Start bounty").withStyle(ChatFormatting.GREEN));
            lore.add(Component.literal("Right-click: View drops").withStyle(ChatFormatting.AQUA));

            gui.setSlot(tierSlots[tier - 1], new GuiElementBuilder(Items.PAPER)
                    .setName(Component.literal("Tier " + tier + " Contract")
                            .withStyle(tierColor, ChatFormatting.BOLD))
                    .setLore(lore)
                    .setCallback((index, clicktype, containerInput, slotGui) -> {
                        if (clicktype == eu.pb4.sgui.api.ClickType.MOUSE_RIGHT) {
                            openDropTableGui(player, type, finalTier);
                        } else if (clicktype == eu.pb4.sgui.api.ClickType.MOUSE_LEFT && finalCanStart) {
                            player.closeContainer();
                            SlayerManager.startQuest(player, type, finalTier);
                        }
                    })
                    .build());
        }


        // Rewards button
        gui.setSlot(40, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Component.literal("💰 Bounty Rewards")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("View rewards for leveling up!")
                        .withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to view!")
                        .withStyle(ChatFormatting.GREEN))
                .setCallback((idx, clicktype, containerInput, slotGui) -> openRewardsMenu(player, type))
                .build());

        // Drop Rates button
        gui.setSlot(41, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("📊 Drop Rates")
                        .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("View drop rates for each tier!")
                        .withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to view!").withStyle(ChatFormatting.GREEN))
                .setCallback((idx, clicktype, containerInput, slotGui) -> openDropRatesTierSelect(player, type))
                .build());

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back to Bounty Board")
                        .withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, clicktype, containerInput, slotGui) -> openMainMenu(player))
                .build());

        gui.open();
    }
    // ============================================================
    // DROP RATES TIER SELECTOR
    // ============================================================
    public static void openDropRatesTierSelect(ServerPlayer player, SlayerManager.SlayerType type) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("📊 " + type.displayName + " Drop Rates — Select Tier"));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.SPRUCE_PLANKS).setName(Component.literal("")).build());
        }

        gui.setSlot(4, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("📊 Select a Tier").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Click a tier to view its drop rates").withStyle(ChatFormatting.GRAY))
                .glow().build());

        int maxTiers = SlayerManager.TIERS.length;
        int[] tierSlots = {10, 11, 12, 13, 14};
        ChatFormatting[] tierColors = {ChatFormatting.WHITE, ChatFormatting.GREEN, ChatFormatting.BLUE, ChatFormatting.GOLD, ChatFormatting.LIGHT_PURPLE};
        for (int t = 1; t <= maxTiers && t <= tierSlots.length; t++) {
            final int tier = t;
            SlayerManager.TierConfig cfg = SlayerManager.getTierConfig(tier);
            gui.setSlot(tierSlots[t - 1], new GuiElementBuilder(Items.CARVED_PUMPKIN)
                    .setName(Component.literal("Tier " + tier + " — " + type.bossName).withStyle(tierColors[t - 1], ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Kills req: " + cfg.killsRequired).withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("Click to view drops").withStyle(ChatFormatting.YELLOW))
                    .setCallback((idx, clicktype, containerInput, slotGui) -> openDropTableGui(player, type, tier,
                            () -> openDropRatesTierSelect(player, type)))
                    .build());
        }

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, clicktype, containerInput, slotGui) -> openBountyTypeMenu(player, type))
                .build());

        gui.open();
    }

    // ============================================================
    // DROP TABLE GUI - Shows boss loot and drop chances
    // ============================================================
    /** Opened from the contract screen (right-click on a tier): Back returns to that screen. */
    public static void openDropTableGui(ServerPlayer player, SlayerManager.SlayerType type, int tier) {
        openDropTableGui(player, type, tier, () -> openBountyTypeMenu(player, type));
    }

    /** {@code back} is the screen that opened this one (the contract screen or the drop-rates tier picker). */
    public static void openDropTableGui(ServerPlayer player, SlayerManager.SlayerType type, int tier, Runnable back) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("☠ " + type.bossName + " T" + tier + " Drops"));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray())
                    .setName(Component.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(getWantedPosterItem(type))
                .setName(Component.literal("☠ " + type.bossName + " Loot Table")
                        .withStyle(type.color, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Tier " + tier + " Drop Rates").withStyle(ChatFormatting.YELLOW))
                .glow()
                .build());

        double coreChance = SlayerManager.getBountyTableCorePercent(type, tier);
        double chunkChance = SlayerManager.getBountyTableChunkPercent(tier);
        double rareBossChance = SlayerManager.getBountyTableRareBossPercent(tier);
        double tokenChance = SlayerManager.getBountyTableAttributeTokenPercent(tier);
        double fleshChance = SlayerManager.getBountyTableFleshPercent();
        double armorChance = SlayerManager.getArmorDropChance(type, tier);

        int slot = 19;

        // Core drop
        gui.setSlot(slot++, new GuiElementBuilder(type.icon)
                .setName(Component.literal("✦ " + type.displayName + " Core")
                        .withStyle(type.color, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Base chance: " + formatChance(coreChance))
                        .withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("Perks & buffs can raise this.").withStyle(ChatFormatting.DARK_GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Used for crafting powerful").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("bounty equipment.").withStyle(ChatFormatting.GRAY))
                .glow()
                .build());

        slot++; // Skip a slot

        // Chunk drop
        gui.setSlot(slot++, new GuiElementBuilder(Items.PRISMARINE_SHARD)
                .setName(Component.literal(SlayerItems.getChunkName(type))
                        .withStyle(type.color))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Base chance: " + formatChance(chunkChance))
                        .withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("Perks & buffs can raise this.").withStyle(ChatFormatting.DARK_GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Crafting material for").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("bounty swords.").withStyle(ChatFormatting.GRAY))
                .build());

        slot++; // Skip a slot

        // Rare type-specific boss item
        gui.setSlot(slot++, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("Rare boss drop")
                        .withStyle(type.color, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Base chance: " + formatChance(rareBossChance))
                        .withStyle(ChatFormatting.LIGHT_PURPLE))
                .addLoreLine(Component.literal("(Undead Heart, Ender Sword, etc.)").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Not multiplied by bounty perks.").withStyle(ChatFormatting.DARK_GRAY))
                .glow()
                .build());

        slot++;

        if (tokenChance > 0) {
            gui.setSlot(slot++, new GuiElementBuilder(Items.ENCHANTED_BOOK)
                    .setName(Component.literal("Attribute token")
                            .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Base chance: " + formatChance(tokenChance))
                            .withStyle(ChatFormatting.AQUA))
                    .addLoreLine(Component.literal("Tier 3+ only.").withStyle(ChatFormatting.DARK_GRAY))
                    .addLoreLine(Component.literal("Bounty perks multiply this.").withStyle(ChatFormatting.DARK_GRAY))
                    .build());
            slot++;
        }

        if (type == SlayerManager.SlayerType.ZOMBIE || type == SlayerManager.SlayerType.PIGLIN) {
            gui.setSlot(slot++, new GuiElementBuilder(
                            type == SlayerManager.SlayerType.PIGLIN ? Items.GOLD_NUGGET : Items.ROTTEN_FLESH)
                    .setName(Component.literal(type == SlayerManager.SlayerType.PIGLIN ? "Piglin Flesh" : "Rotten Flesh")
                            .withStyle(ChatFormatting.DARK_GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Base chance: " + formatChance(fleshChance))
                            .withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal("On success, drops this boss type's flesh.").withStyle(ChatFormatting.DARK_GRAY))
                    .build());
            slot++;
        }

        // Enderman only: separate roll for Voidwalker's Crown (see SlayerManager.rollDrops)
        if (type == SlayerManager.SlayerType.ENDERMAN && tier >= 4 && armorChance > 0) {
            ItemStack armorPreview = getSpecialArmorPreview(type);
            if (armorPreview != null && !armorPreview.isEmpty()) {
                gui.setSlot(slot++, new GuiElementBuilder(armorPreview.getItem())
                        .setName(Component.literal("★ " + getSpecialArmorName(type))
                                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("Base chance: " + formatChance(armorChance))
                                .withStyle(ChatFormatting.GOLD))
                        .addLoreLine(Component.literal("Separate roll from other drops.").withStyle(ChatFormatting.DARK_GRAY))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("LEGENDARY DROP!").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                        .glow()
                        .build());
            }
        }

        // Coins reward (guaranteed)
        int coinReward = SlayerManager.getTierConfig(tier).coinReward;

        gui.setSlot(31, new GuiElementBuilder(Items.GOLD_NUGGET)
                .setName(Component.literal("💰 Coin Reward")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Guaranteed: " + formatNumber(coinReward) + " coins")
                        .withStyle(ChatFormatting.YELLOW))
                .build());

        // XP reward
        SlayerManager.TierConfig config = SlayerManager.getTierConfig(tier);
        gui.setSlot(33, new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
                .setName(Component.literal("✨ Bounty XP")
                        .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Guaranteed: +" + config.xpReward + " XP")
                        .withStyle(ChatFormatting.GREEN))
                .build());

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back to Tiers")
                        .withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, clicktype, containerInput, slotGui) -> back.run())
                .build());

        // Start bounty button
        gui.setSlot(49, new GuiElementBuilder(Items.CONCRETE.lime())
                .setName(Component.literal("▶ Start This Bounty")
                        .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .setCallback((idx, clicktype, containerInput, slotGui) -> {
                    player.closeContainer();
                    SlayerManager.startQuest(player, type, tier);
                })
                .build());

        gui.open();
    }

    // Helper methods for special armor
    private static ItemStack getSpecialArmorPreview(SlayerManager.SlayerType type) {
        return switch (type) {
            case ZOMBIE -> new ItemStack(Items.ZOMBIE_HEAD);
            case SPIDER -> new ItemStack(Items.LEATHER_LEGGINGS);
            case SKELETON -> new ItemStack(Items.BOW);
            case SLIME -> new ItemStack(Items.LEATHER_BOOTS);
            case ENDERMAN -> new ItemStack(Items.NETHERITE_HELMET);
            case IRON_GOLEM -> new ItemStack(Items.NETHERITE_CHESTPLATE);
            case PIGLIN -> new ItemStack(Items.GOLDEN_HELMET);
        };
    }

    private static String getSpecialArmorName(SlayerManager.SlayerType type) {
        return switch (type) {
            case ZOMBIE -> "Zombie Berserker Helmet";
            case SPIDER -> "Venomous Crawler Leggings";
            case SKELETON -> "Bone Desperado Bow";
            case SLIME -> "Gelatinous Rustler Boots";
            case ENDERMAN -> "Voidwalker's Crown";
            case IRON_GOLEM -> "Sculk Terror Chestplate";
            case PIGLIN -> "Crown of Greed";
        };
    }
    // ============================================================
    // REWARDS MENU
    // ============================================================

    public static void openRewardsMenu(ServerPlayer player, SlayerManager.SlayerType type) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("💰 " + type.displayName + " Bounty Rewards 💰"));

        String uuid = player.getUUID().toString();
        int playerLevel = SlayerData.getSlayerLevel(uuid, type);

        // Wood background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.SPRUCE_PLANKS)
                    .setName(Component.literal("")).build());
        }

        // Level rewards displayed in grid
        int[] levelSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23};

        for (int level = 1; level <= SlayerManager.MAX_LEVEL; level++) {
            boolean unlocked = playerLevel >= level;
            long xpRequired = SlayerManager.XP_REQUIREMENTS[level - 1];
            int creditReward = SlayerManager.LEVEL_CREDIT_REWARDS[level - 1];

            List<Component> lore = new ArrayList<>();
            lore.add(Component.literal(""));
            lore.add(Component.literal("XP Required: " + formatNumber(xpRequired))
                    .withStyle(ChatFormatting.AQUA));
            lore.add(Component.literal(""));
            lore.add(Component.literal("Rewards:").withStyle(ChatFormatting.YELLOW));
            lore.add(Component.literal("  +" + formatNumber(creditReward) + " coins")
                    .withStyle(ChatFormatting.GOLD));

            lore.add(Component.literal(""));
            if (unlocked) {
                lore.add(Component.literal("✔ CLAIMED")
                        .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
            } else {
                long currentXp = SlayerData.getSlayerXp(uuid, type);
                long xpNeeded = xpRequired - currentXp;
                lore.add(Component.literal("✖ LOCKED")
                        .withStyle(ChatFormatting.RED));
                lore.add(Component.literal("Need " + formatNumber(xpNeeded) + " more XP")
                        .withStyle(ChatFormatting.GRAY));
            }

            gui.setSlot(levelSlots[level - 1], new GuiElementBuilder(unlocked ? Items.GOLD_NUGGET : Items.COAL)
                    .setName(Component.literal("Level " + level + " Reward")
                            .withStyle(unlocked ? ChatFormatting.GOLD : ChatFormatting.GRAY, ChatFormatting.BOLD))
                    .setLore(lore)
                    .build());
        }

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back")
                        .withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, clicktype, containerInput, slotGui) -> openBountyTypeMenu(player, type))
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
            case IRON_GOLEM -> 5;
            case PIGLIN -> 4;
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

    private static net.minecraft.world.item.Item getWantedPosterItem(SlayerManager.SlayerType type) {
        return switch (type) {
            case ZOMBIE -> Items.ZOMBIE_HEAD;
            case SPIDER -> Items.SPIDER_EYE;
            case SKELETON -> Items.SKELETON_SKULL;
            case SLIME -> Items.SLIME_BALL;
            case ENDERMAN -> Items.ENDER_PEARL;
            case IRON_GOLEM -> Items.SCULK;
            case PIGLIN -> Items.GOLD_NUGGET;
        };
    }

    private static net.minecraft.world.item.Item getContractItem(int tier) {
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

    /** Formats a percentage value for drop tables (extra precision when very small). */
    private static String formatChance(double percent) {
        if (percent > 0 && percent < 0.01) {
            return String.format("%.4f", percent) + "%";
        }
        if (percent < 1) {
            return String.format("%.2f", percent) + "%";
        }
        return String.format("%.1f", percent) + "%";
    }
}