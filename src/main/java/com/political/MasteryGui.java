package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * GUI for viewing and managing Mastery progress.
 */
public class MasteryGui {

    public static void openMainMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Heart of Mastery").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        // Fill background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("Mastery & Heart Tree").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Skyblock-style: six weapon/armor tracks").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Earn XP by fighting, mining, crafting,").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("taking hits in armor, and more.").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Grandmaster at level 10 per track.").withStyle(ChatFormatting.YELLOW))
                .glow()
                .build());

        // Display each mastery family
        MasteryManager.MasteryFamily[] families = MasteryManager.MasteryFamily.values();
        int[] slots = {10, 12, 14, 30, 32, 34};
        
        for (int i = 0; i < families.length && i < slots.length; i++) {
            MasteryManager.MasteryFamily family = families[i];
            MasteryManager.MasteryData data = MasteryManager.getMasteryData(player, family);
            
            Item icon = getFamilyIcon(family);
            int level = data.level;
            double progress = data.getProgressPercent();
            
            ChatFormatting levelColor = level >= 10 ? ChatFormatting.DARK_PURPLE : 
                                   level >= 7 ? ChatFormatting.LIGHT_PURPLE :
                                   level >= 5 ? ChatFormatting.YELLOW : ChatFormatting.WHITE;
            
            gui.setSlot(slots[i], new GuiElementBuilder(icon)
                    .setName(Component.literal(family.icon + " " + family.displayName).withStyle(family.color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Level: ").withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(String.valueOf(level)).withStyle(levelColor, ChatFormatting.BOLD)))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("XP: " + data.xp + " / " + MasteryManager.getCumulativeXpForLevel(10)).withStyle(ChatFormatting.DARK_GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Progress to Next:").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(createProgressBar(progress))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to view perks").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> openFamilyDetail(player, family))
                    .build());
        }

        // Stats summary
        int totalLevels = 0;
        int maxedFamilies = 0;
        Map<MasteryManager.MasteryFamily, MasteryManager.MasteryData> allMastery = MasteryManager.getAllMastery(player);
        for (MasteryManager.MasteryData d : allMastery.values()) {
            totalLevels += d.level;
            if (d.level >= 10) maxedFamilies++;
        }
        
        gui.setSlot(49, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal("Your Mastery Stats").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Total Levels: " + totalLevels).withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("Grandmaster Tracks: " + maxedFamilies).withStyle(ChatFormatting.LIGHT_PURPLE))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Max all 6 tracks for").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("the ultimate title!").withStyle(ChatFormatting.GOLD))
                .build());

        ClassManager.PlayerClass pc = ClassManager.getPlayerClass(player.getUUID().toString());
        MasteryManager.MasteryFamily track = MasteryManager.getMasteryFamilyForClass(pc);
        String trackName = track != null ? track.icon + " " + track.displayName
                : (pc != null ? "Set after class sync" : "Pick a class with /class");

        gui.setSlot(48, new GuiElementBuilder(Items.HEART_OF_THE_SEA)
                .setName(Component.literal("Coin Heart (inner tree)").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Spend coins: Mining / Combat / Fortune rows").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Like Skyblock Heart of the Mountain perks").withStyle(ChatFormatting.DARK_GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to open").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> openHeartTree(player))
                .build());

        gui.setSlot(50, new GuiElementBuilder(Items.COMPASS)
                .setName(Component.literal("Mastery track").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Linked to your class (/class)").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Active XP track: " + trackName).withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Change class to switch mastery XP").withStyle(ChatFormatting.DARK_GRAY))
                .build());

        gui.open();
    }

    public static void openHeartTree(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Heart of Mastery — Coin Tree").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        String uuid = player.getUUID().toString();
        Set<String> unlocked = new HashSet<>(MasteryManager.getHeartUnlocks(uuid));
        int totalUnlocked = unlocked.size();

        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("Permanent Upgrades").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Your coins: " + String.format("%,d", CoinManager.getCoinsLong(player))).withStyle(ChatFormatting.GOLD))
                .addLoreLine(Component.literal("Unlocked: " + totalUnlocked + "/" + MasteryHeartTree.NODES.size()).withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Unlock left-to-right per row").withStyle(ChatFormatting.GRAY))
                .glow()
                .build());

        gui.setSlot(9, new GuiElementBuilder(Items.IRON_PICKAXE)
                .setName(Component.literal("⛏ Mining Branch").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Block-break, ore drops, mining XP").withStyle(ChatFormatting.GRAY))
                .build());
        gui.setSlot(18, new GuiElementBuilder(Items.DIAMOND_SWORD)
                .setName(Component.literal("⚔ Combat Branch").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Damage, crit, lifesteal, boss DPS").withStyle(ChatFormatting.GRAY))
                .build());
        gui.setSlot(27, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Component.literal("💰 Fortune Branch").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Coins, interest, XP, magic find").withStyle(ChatFormatting.GRAY))
                .build());

        int[] rowBase = {10, 19, 28};
        String[] branches = {"MINING", "COMBAT", "FORTUNE"};

        for (int b = 0; b < branches.length; b++) {
            String br = branches[b];
            int base = rowBase[b];
            for (MasteryHeartTree.HeartNode node : MasteryHeartTree.NODES) {
                if (!node.branch().equals(br)) continue;
                int slot = base + (node.tier() - 1);
                boolean has = unlocked.contains(node.id());
                boolean prereqMet = MasteryHeartTree.canUnlock(player, node.id(), unlocked);
                boolean canAfford = CoinManager.hasCoins(player, node.coinCost());
                boolean canBuy = !has && prereqMet && canAfford;

                Item icon;
                ChatFormatting nameCol;
                if (has) {
                    icon = Items.LIME_CONCRETE;
                    nameCol = ChatFormatting.GREEN;
                } else if (canBuy) {
                    icon = Items.GOLD_BLOCK;
                    nameCol = ChatFormatting.YELLOW;
                } else if (!prereqMet) {
                    icon = Items.RED_CONCRETE;
                    nameCol = ChatFormatting.DARK_GRAY;
                } else {
                    icon = Items.YELLOW_CONCRETE;
                    nameCol = ChatFormatting.DARK_GRAY;
                }

                GuiElementBuilder el = new GuiElementBuilder(icon)
                        .setName(Component.literal(node.title()).withStyle(nameCol, ChatFormatting.BOLD))
                        .addLoreLine(Component.literal(node.description()).withStyle(ChatFormatting.GRAY))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("Branch: " + node.branch() + " | Tier " + node.tier()).withStyle(ChatFormatting.DARK_GRAY))
                        .addLoreLine(Component.literal("Cost: " + String.format("%,d", node.coinCost()) + " coins").withStyle(ChatFormatting.GOLD));

                if (has) {
                    el.addLoreLine(Component.literal("✓ Unlocked").withStyle(ChatFormatting.GREEN));
                    el.glow();
                } else if (canBuy) {
                    el.addLoreLine(Component.literal("Click to purchase").withStyle(ChatFormatting.GREEN));
                } else if (!prereqMet) {
                    el.addLoreLine(Component.literal("Unlock the previous node first").withStyle(ChatFormatting.RED));
                } else {
                    el.addLoreLine(Component.literal("Not enough coins").withStyle(ChatFormatting.RED));
                }

                if (!has && prereqMet) {
                    String nid = node.id();
                    el.setCallback((index, type, containerInput, slotGui) -> {
                        MasteryHeartTree.tryPurchase(player, nid);
                        openHeartTree(player);
                    });
                }
                gui.setSlot(slot, el.build());
            }
        }

        java.util.List<String> activeBonuses = new java.util.ArrayList<>();
        for (MasteryHeartTree.HeartNode node : MasteryHeartTree.NODES) {
            if (unlocked.contains(node.id())) {
                activeBonuses.add("§a✓ §7" + node.title() + ": §f" + node.description());
            }
        }
        GuiElementBuilder summary = new GuiElementBuilder(Items.ENCHANTED_BOOK)
                .setName(Component.literal("Active Bonuses").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""));
        if (activeBonuses.isEmpty()) {
            summary.addLoreLine(Component.literal("No bonuses unlocked yet").withStyle(ChatFormatting.GRAY));
        } else {
            for (String line : activeBonuses) {
                summary.addLoreLine(Component.literal(line));
            }
        }
        gui.setSlot(45, summary.glow().build());

        gui.setSlot(53, new GuiElementBuilder(Items.BOOK)
                .setName(Component.literal("Progress").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Unlocked: " + totalUnlocked + " / " + MasteryHeartTree.NODES.size()).withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal(String.format("%.0f%%", totalUnlocked * 100.0 / MasteryHeartTree.NODES.size()) + " complete").withStyle(ChatFormatting.AQUA))
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("Back").withStyle(ChatFormatting.GRAY))
                .setCallback((index, type, containerInput, slotGui) -> openMainMenu(player))
                .build());

        gui.open();
    }

    private static String formatTime(long millis) {
        long hours = millis / (60 * 60 * 1000);
        long minutes = (millis % (60 * 60 * 1000)) / (60 * 1000);
        return hours + "h " + minutes + "m";
    }
    
    public static void openFamilySelection(ServerPlayer player) {
        if (!MasteryManager.canChangeFamily(player)) {
            player.sendSystemMessage(Component.literal("§cYou cannot change your mastery family yet!"), false);
            openMainMenu(player);
            return;
        }
        
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("§6🎯 Select Your Mastery Family"));

        // Fill background
        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.COMPASS)
                .setName(Component.literal("Choose Your Path").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Select one family to focus on").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("This gives bonuses when using").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("items from that family.").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("You can change once per day!").withStyle(ChatFormatting.YELLOW))
                .glow()
                .build());

        // Show all families
        MasteryManager.MasteryFamily[] families = MasteryManager.MasteryFamily.values();
        int[] slots = {10, 11, 12, 13, 14, 15};
        
        for (int i = 0; i < families.length; i++) {
            MasteryManager.MasteryFamily family = families[i];
            Item icon = getFamilyIcon(family);
            
            gui.setSlot(slots[i], new GuiElementBuilder(icon)
                    .setName(Component.literal(family.icon + " " + family.displayName).withStyle(family.color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to select").withStyle(ChatFormatting.GREEN))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        MasteryManager.setSelectedFamily(player, family);
                        openMainMenu(player);
                    })
                    .build());
        }

        // Back button
        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("§c← Back"))
                .setCallback((index, type, containerInput, slotGui) -> openMainMenu(player))
                .build());

        gui.open();
    }

    public static void openFamilyDetail(ServerPlayer player, MasteryManager.MasteryFamily family) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("§6" + family.icon + " " + family.displayName + " Mastery"));

        // Fill background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        MasteryManager.MasteryData data = MasteryManager.getMasteryData(player, family);
        int currentLevel = data.level;

        // Header with current level
        gui.setSlot(4, new GuiElementBuilder(getFamilyIcon(family))
                .setName(Component.literal(family.icon + " " + family.displayName + " Mastery").withStyle(family.color, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Current Level: " + currentLevel).withStyle(ChatFormatting.GOLD))
                .addLoreLine(Component.literal("Total XP: " + data.xp).withStyle(ChatFormatting.GRAY))
                .glow()
                .build());

        // Show perks for each level
        int[] levelSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21};
        
        for (int level = 1; level <= 10; level++) {
            int slot = levelSlots[level - 1];
            String perkDesc = MasteryManager.getPerkDescription(family, level);
            boolean unlocked = level <= currentLevel;
            
            ChatFormatting color = unlocked ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY;
            Item icon = unlocked ? Items.LIME_STAINED_GLASS_PANE : Items.RED_STAINED_GLASS_PANE;
            if (level == 10 && unlocked) icon = Items.NETHER_STAR;
            else if (level == 10) icon = Items.GRAY_STAINED_GLASS_PANE;
            
            String levelName = level == 10 ? "Grandmaster" : "Level " + level;
            
            GuiElementBuilder builder = new GuiElementBuilder(icon)
                    .setName(Component.literal(levelName).withStyle(color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""));
            
            if (perkDesc != null) {
                builder.addLoreLine(Component.literal(perkDesc).withStyle(unlocked ? ChatFormatting.WHITE : ChatFormatting.DARK_GRAY));
            } else {
                builder.addLoreLine(Component.literal("No perk at this level").withStyle(ChatFormatting.DARK_GRAY));
            }
            
            builder.addLoreLine(Component.literal(""));
            if (unlocked) {
                builder.addLoreLine(Component.literal("§a✓ UNLOCKED"));
            } else {
                int xpNeeded = MasteryManager.getXpForLevel(level);
                builder.addLoreLine(Component.literal("§c✗ Locked").withStyle(ChatFormatting.RED));
                builder.addLoreLine(Component.literal("XP needed: " + xpNeeded).withStyle(ChatFormatting.GRAY));
            }
            
            gui.setSlot(slot, builder.build());
        }

        // XP sources
        gui.setSlot(38, new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
                .setName(Component.literal("How to Gain XP").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Melee/Ranged/Magic: kills with weapons").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Gathering: mine blocks with tools").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Armor: take damage in armor").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Crafting: craft items; varies by result").withStyle(ChatFormatting.YELLOW))
                .build());

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((index, type, containerInput, slotGui) -> openMainMenu(player))
                .build());

        gui.open();
    }

    private static Item getFamilyIcon(MasteryManager.MasteryFamily family) {
        return switch (family) {
            case MELEE -> Items.DIAMOND_SWORD;
            case RANGED -> Items.BOW;
            case MAGIC -> Items.ENCHANTED_BOOK;
            case GATHERING -> Items.DIAMOND_PICKAXE;
            case CRAFTING -> Items.CRAFTING_TABLE;
            case ARMOR -> Items.DIAMOND_CHESTPLATE;
        };
    }

    private static Component createProgressBar(double percent) {
        StringBuilder bar = new StringBuilder("§8[");
        int filled = (int) (percent / 10);
        int empty = 10 - filled;
        
        for (int i = 0; i < filled; i++) bar.append("§a█");
        for (int i = 0; i < empty; i++) bar.append("§7░");
        bar.append("§8] §f").append(String.format("%.1f%%", percent));
        
        return Component.literal(bar.toString());
    }

    public static void openEnhancementsGui(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Your Enhancements").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Component.literal("")).build());
        }

        String uuid = player.getUUID().toString();

        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("Your Active Enhancements").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("All permanent upgrades you've unlocked").withStyle(ChatFormatting.GRAY))
                .glow().build());

        int col = 10;
        for (MasteryManager.MasteryFamily family : MasteryManager.MasteryFamily.values()) {
            int level = MasteryManager.getMasteryLevel(uuid, family);
            MasteryManager.MasteryData data = MasteryManager.getMasteryData(uuid, family);
            int nextXp = level < 10 ? MasteryManager.getXpForLevel(level + 1) : 0;
            gui.setSlot(col, new GuiElementBuilder(getFamilyIcon(family))
                    .setName(Component.literal(family.displayName + " Mastery Lv." + level).withStyle(family.color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(level >= 10
                            ? Component.literal("GRANDMASTER").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)
                            : Component.literal("XP: " + data.xp + "/" + nextXp).withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(MasteryManager.getPerkDescription(family, level) != null
                            ? MasteryManager.getPerkDescription(family, level) : "").withStyle(ChatFormatting.WHITE))
                    .build());
            col++;
            if (col > 15) break;
        }

        Set<String> heartUnlocks = MasteryManager.getHeartUnlocks(uuid);
        int heartSlot = 19;
        for (MasteryHeartTree.HeartNode node : MasteryHeartTree.NODES) {
            if (heartUnlocks.contains(node.id())) {
                if (heartSlot > 26) break;
                gui.setSlot(heartSlot, new GuiElementBuilder(Items.LIME_CONCRETE)
                        .setName(Component.literal("♦ " + node.title()).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                        .addLoreLine(Component.literal(node.description()).withStyle(ChatFormatting.GRAY))
                        .addLoreLine(Component.literal(node.branch() + " Tier " + node.tier()).withStyle(ChatFormatting.DARK_GRAY))
                        .build());
                heartSlot++;
            }
        }
        while (heartSlot <= 26) {
            gui.setSlot(heartSlot, new GuiElementBuilder(Items.RED_CONCRETE)
                    .setName(Component.literal("Locked").withStyle(ChatFormatting.DARK_GRAY))
                    .addLoreLine(Component.literal("Purchase from /mastery coin tree").withStyle(ChatFormatting.GRAY))
                    .build());
            heartSlot++;
        }

        int magicLevel = MagicManager.getLevel(uuid);
        gui.setSlot(28, new GuiElementBuilder(Items.BLAZE_POWDER)
                .setName(Component.literal("Magic Level " + magicLevel).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Max Mana: " + MagicManager.getMaxMana(uuid)).withStyle(ChatFormatting.AQUA))
                .build());

        ClassManager.PlayerClass pc = ClassManager.getPlayerClass(uuid);
        if (pc != null) {
            int classLevel = ClassManager.getClassLevel(uuid, pc);
            gui.setSlot(29, new GuiElementBuilder(Items.IRON_SWORD)
                    .setName(Component.literal("Class: " + pc.name + " Lv." + classLevel).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                    .build());
        }

        gui.setSlot(30, new GuiElementBuilder(Items.WITHER_SKELETON_SKULL)
                .setName(Component.literal("Total Slayer Level: " + SlayerData.getTotalSlayerLevel(uuid)).withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .build());

        gui.setSlot(31, new GuiElementBuilder(Items.OAK_SAPLING)
                .setName(Component.literal("Progression Tree").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Skill Points: " + ProgressionTreeManager.getSkillPoints(uuid)).withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal("Total Tiers: " + ProgressionTreeManager.getTotalTiers(uuid)).withStyle(ChatFormatting.GRAY))
                .build());

        gui.setSlot(32, new GuiElementBuilder(Items.AMETHYST_SHARD)
                .setName(Component.literal("HOTG Magic Find: " + AscensionGrindManager.getMagicFind(uuid)).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                .build());

        FishingManager.PlayerFishingData fishData = FishingManager.getData(uuid);
        gui.setSlot(33, new GuiElementBuilder(Items.FISHING_ROD)
                .setName(Component.literal("Fishing Level " + fishData.level).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Fish Caught: " + fishData.fishCaught).withStyle(ChatFormatting.GREEN))
                .build());

        long support = PoliticalPartyManager.getSupport(uuid);
        gui.setSlot(34, new GuiElementBuilder(Items.EMERALD)
                .setName(Component.literal("Party Support: " + support).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .build());

        int totalMastery = MasteryManager.getTotalLevel(uuid);
        int grandmasters = MasteryManager.getGrandmasterCount(uuid);
        gui.setSlot(40, new GuiElementBuilder(Items.GOLDEN_APPLE)
                .setName(Component.literal("Summary").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Total Mastery Levels: " + totalMastery + "/60").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("Grandmaster Tracks: " + grandmasters).withStyle(ChatFormatting.LIGHT_PURPLE))
                .addLoreLine(Component.literal("Heart Nodes Unlocked: " + heartUnlocks.size() + "/" + MasteryHeartTree.NODES.size()).withStyle(ChatFormatting.AQUA))
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("Close").withStyle(ChatFormatting.GRAY))
                .setCallback((i, t, c, s) -> player.closeContainer())
                .build());

        gui.open();
    }
}
