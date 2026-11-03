package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.*;

public class FishingGui {

    // ════════════════════════════════════════════════════════════
    // MAIN MENU
    // ════════════════════════════════════════════════════════════

    public static void openMainMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Fishing").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        RecipeGuiTheme.fillDoubleChestCatalog(gui);

        String uuid = player.getUUID().toString();
        FishingManager.PlayerFishingData data = FishingManager.getData(uuid);

        int xpNeeded = data.level < FishingManager.MAX_LEVEL ? FishingManager.xpForLevel(data.level + 1) : 0;
        int barLen = 20;
        int filled = xpNeeded > 0 ? (int) ((double) data.xp / xpNeeded * barLen) : barLen;
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < barLen; i++) bar.append(i < filled ? "█" : "░");

        gui.setSlot(4, new GuiElementBuilder(Items.FISHING_ROD)
                .setName(Component.literal("Fishing Level " + data.level).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("XP: " + String.format("%,d", data.xp) + " / " + String.format("%,d", xpNeeded)).withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal(bar.toString()).withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Fish Caught: " + String.format("%,d", data.fishCaught)).withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Creatures Killed: " + String.format("%,d", data.creaturesKilled)).withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal("Rarest Catch: " + data.rarestCatch).withStyle(ChatFormatting.GOLD))
                .glow().build());

        // Skill Tree button
        gui.setSlot(20, new GuiElementBuilder(Items.ENCHANTED_BOOK)
                .setName(Component.literal("Skill Tree").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Unlock fishing abilities").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Nodes unlocked: " + data.unlockedNodes.size() + "/" + FishingSkillTree.getAllNodes().size()).withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("Skill XP available: " + String.format("%,d", FishingManager.getAvailableSkillXp(uuid))).withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to open").withStyle(ChatFormatting.AQUA))
                .setCallback((idx, type, containerInput, slotGui) -> openSkillTree(player))
                .build());

        // Equipment button
        FishingRods.RodTier currentRod = FishingRods.RodTier.fromId(data.equippedRod);
        String rodName = currentRod != null ? currentRod.displayName : "None";
        gui.setSlot(22, new GuiElementBuilder(Items.FISHING_ROD)
                .setName(Component.literal("Equipment").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Equipped Rod: " + rodName).withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to manage rods").withStyle(ChatFormatting.AQUA))
                .setCallback((idx, type, containerInput, slotGui) -> openRodSelector(player))
                .build());

        // Bestiary button
        gui.setSlot(24, new GuiElementBuilder(Items.AXOLOTL_BUCKET)
                .setName(Component.literal("Sea Bestiary").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Discovered: " + data.discoveredCreatures.size() + "/" + FishingManager.SeaCreature.values().length).withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Total kills: " + String.format("%,d", data.creaturesKilled)).withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to view").withStyle(ChatFormatting.AQUA))
                .setCallback((idx, type, containerInput, slotGui) -> openBestiary(player))
                .build());

        // Stats overview
        gui.setSlot(30, new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
                .setName(Component.literal("Stats Overview").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Catch Speed: +" + (int) ((FishingManager.getCatchSpeedMultiplier(uuid) - 1.0) * 100) + "%").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("Sea Creature Chance: " + String.format("%.1f%%", FishingManager.getSeaCreatureChance(uuid) * 100)).withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal("Treasure Chance: " + String.format("%.1f%%", FishingManager.getTreasureChance(uuid) * 100)).withStyle(ChatFormatting.GOLD))
                .addLoreLine(Component.literal("Sea Creature DMG: +" + (int) ((FishingManager.getSeaCreatureDamageMultiplier(uuid) - 1.0) * 100) + "%").withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal("Lava Fishing: " + (FishingManager.hasLavaFishing(uuid) ? "Yes" : "No")).withStyle(ChatFormatting.DARK_RED))
                .build());

        // Leaderboard placeholder
        gui.setSlot(32, new GuiElementBuilder(Items.GOLDEN_APPLE)
                .setName(Component.literal("Leaderboard").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Top fishers on the server").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to view").withStyle(ChatFormatting.AQUA))
                .setCallback((idx, type, containerInput, slotGui) -> openLeaderboard(player))
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Close").withStyle(ChatFormatting.RED))
                .setCallback((idx, type, containerInput, slotGui) -> gui.close())
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    // SKILL TREE GUI
    // ════════════════════════════════════════════════════════════

    public static void openSkillTree(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Fishing Skill Tree").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.black()).setName(Component.literal("")).build());
        }

        String uuid = player.getUUID().toString();
        FishingManager.PlayerFishingData data = FishingManager.getData(uuid);
        int availableXp = FishingManager.getAvailableSkillXp(uuid);

        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("Fishing Skill Tree").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Available Skill XP: " + String.format("%,d", availableXp)).withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("Nodes unlocked: " + data.unlockedNodes.size() + "/" + FishingSkillTree.getAllNodes().size()).withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Green = Unlocked").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("Yellow = Available").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("Red = Locked").withStyle(ChatFormatting.RED))
                .glow().build());

        // Category headers in row 0
        int[] catCols = {1, 2, 4, 6, 7};
        FishingSkillTree.Category[] cats = FishingSkillTree.Category.values();
        for (int c = 0; c < cats.length && c < catCols.length; c++) {
            FishingSkillTree.Category cat = cats[c];
            gui.setSlot(catCols[c], new GuiElementBuilder(cat.icon)
                    .setName(Component.literal(cat.displayName).withStyle(cat.color, ChatFormatting.BOLD))
                    .build());
        }

        for (FishingSkillTree.Node node : FishingSkillTree.getAllNodes()) {
            int slot = getSlotForNode(node);
            if (slot < 0 || slot >= 54) continue;

            boolean unlocked = data.unlockedNodes.contains(node.id);
            boolean available = !unlocked && FishingSkillTree.canUnlock(node.id, data.unlockedNodes);
            boolean affordable = available && availableXp >= node.xpCost;

            net.minecraft.world.item.Item paneColor;
            ChatFormatting nameColor;
            if (unlocked) {
                paneColor = Items.STAINED_GLASS_PANE.lime();
                nameColor = ChatFormatting.GREEN;
            } else if (affordable) {
                paneColor = Items.STAINED_GLASS_PANE.yellow();
                nameColor = ChatFormatting.YELLOW;
            } else if (available) {
                paneColor = Items.STAINED_GLASS_PANE.orange();
                nameColor = ChatFormatting.GOLD;
            } else {
                paneColor = Items.STAINED_GLASS_PANE.red();
                nameColor = ChatFormatting.RED;
            }

            GuiElementBuilder el = new GuiElementBuilder(unlocked ? node.icon : paneColor)
                    .setName(Component.literal(node.name).withStyle(nameColor, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal(node.description).withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("Category: " + node.category.displayName).withStyle(node.category.color))
                    .addLoreLine(Component.literal("Cost: " + String.format("%,d", node.xpCost) + " XP").withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal(""));

            if (unlocked) {
                el.addLoreLine(Component.literal("UNLOCKED").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
                if (node.icon != Items.AIR) el.glow();
            } else if (affordable) {
                el.addLoreLine(Component.literal("Click to unlock!").withStyle(ChatFormatting.YELLOW));
            } else if (available) {
                el.addLoreLine(Component.literal("Not enough XP").withStyle(ChatFormatting.RED));
            } else {
                String prereqName = "???";
                if (node.prerequisiteId != null) {
                    FishingSkillTree.Node prereq = FishingSkillTree.getNode(node.prerequisiteId);
                    if (prereq != null) prereqName = prereq.name;
                }
                el.addLoreLine(Component.literal("Requires: " + prereqName).withStyle(ChatFormatting.RED));
            }

            if (affordable) {
                el.setCallback((idx, type, containerInput, slotGui) -> {
                    if (FishingManager.unlockNode(player, node.id)) {
                        openSkillTree(player);
                    }
                });
            }

            gui.setSlot(slot, el.build());
        }

        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openMainMenu(player))
                .build());

        gui.open();
    }

    private static int getSlotForNode(FishingSkillTree.Node node) {
        int[] colMapping = {1, 2, 4, 6, 7};
        int col = node.gridCol < colMapping.length ? colMapping[node.gridCol] : node.gridCol + 1;
        int row = node.gridRow + 1; // offset by 1 for header
        if (row > 4) row = 4;
        return row * 9 + col;
    }

    // ════════════════════════════════════════════════════════════
    // ROD SELECTOR
    // ════════════════════════════════════════════════════════════

    public static void openRodSelector(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("Fishing Rods").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));

        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray()).setName(Component.literal("")).build());
        }

        String uuid = player.getUUID().toString();
        FishingManager.PlayerFishingData data = FishingManager.getData(uuid);
        FishingRods.RodTier equipped = FishingRods.RodTier.fromId(data.equippedRod);

        int slot = 10;
        for (FishingRods.RodTier tier : FishingRods.RodTier.values()) {
            boolean unlocked = data.level >= tier.levelRequired;
            boolean isEquipped = equipped == tier;

            GuiElementBuilder el;
            if (unlocked) {
                el = new GuiElementBuilder(Items.FISHING_ROD)
                        .setName(Component.literal(tier.displayName).withStyle(tier.color, ChatFormatting.BOLD));
            } else {
                el = new GuiElementBuilder(Items.BARRIER)
                        .setName(Component.literal(tier.displayName + " (Locked)").withStyle(ChatFormatting.RED));
            }

            el.addLoreLine(Component.literal(""));
            if (tier.catchSpeedBonus > 0)
                el.addLoreLine(Component.literal("+" + tier.catchSpeedBonus + "% Catch Speed").withStyle(ChatFormatting.GREEN));
            if (tier.seaCreatureBonus > 0)
                el.addLoreLine(Component.literal("+" + tier.seaCreatureBonus + "% Sea Creature Chance").withStyle(ChatFormatting.AQUA));
            if (tier.treasureBonus > 0)
                el.addLoreLine(Component.literal("+" + tier.treasureBonus + "% Treasure Find").withStyle(ChatFormatting.GOLD));
            if (tier.lavaFishing > 0)
                el.addLoreLine(Component.literal("Lava Fishing Enabled").withStyle(ChatFormatting.RED));
            el.addLoreLine(Component.literal(""));
            el.addLoreLine(Component.literal("Unlock: Level " + tier.levelRequired).withStyle(ChatFormatting.DARK_GRAY));

            if (isEquipped) {
                el.addLoreLine(Component.literal("EQUIPPED").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
                el.glow();
            } else if (unlocked) {
                el.addLoreLine(Component.literal("Click to equip").withStyle(ChatFormatting.YELLOW));
                FishingRods.RodTier finalTier = tier;
                el.setCallback((idx, type, containerInput, slotGui) -> {
                    data.equippedRod = finalTier.id;
                    player.sendSystemMessage(Component.literal("Equipped: ").withStyle(ChatFormatting.GREEN)
                            .append(Component.literal(finalTier.displayName).withStyle(finalTier.color)));
                    openRodSelector(player);
                });
            }

            gui.setSlot(slot, el.build());
            slot++;
        }

        // Give rod button
        gui.setSlot(22, new GuiElementBuilder(Items.CHEST)
                .setName(Component.literal("Get Rod Item").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Get the physical rod item").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("for your equipped rod tier").withStyle(ChatFormatting.GRAY))
                .setCallback((idx, type, containerInput, slotGui) -> {
                    FishingRods.RodTier rod = FishingRods.RodTier.fromId(data.equippedRod);
                    if (rod != null) {
                        player.addItem(FishingRods.createRod(rod));
                        player.sendSystemMessage(Component.literal("Received: ").withStyle(ChatFormatting.GREEN)
                                .append(Component.literal(rod.displayName).withStyle(rod.color)));
                    }
                })
                .build());

        gui.setSlot(27, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    // BESTIARY
    // ════════════════════════════════════════════════════════════

    public static void openBestiary(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Sea Bestiary").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.black()).setName(Component.literal("")).build());
        }

        String uuid = player.getUUID().toString();
        FishingManager.PlayerFishingData data = FishingManager.getData(uuid);

        gui.setSlot(4, new GuiElementBuilder(Items.AXOLOTL_BUCKET)
                .setName(Component.literal("Sea Bestiary").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Discovered: " + data.discoveredCreatures.size() + "/" + FishingManager.SeaCreature.values().length).withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal("Total kills: " + String.format("%,d", data.creaturesKilled)).withStyle(ChatFormatting.RED))
                .glow().build());

        int slot = 19;
        for (FishingManager.SeaCreature creature : FishingManager.SeaCreature.values()) {
            boolean discovered = data.discoveredCreatures.contains(creature.name());
            int kills = data.bestiaryKills.getOrDefault(creature.name(), 0);

            GuiElementBuilder el;
            if (discovered) {
                el = new GuiElementBuilder(creature.icon)
                        .setName(Component.literal(creature.displayName).withStyle(creature.tier.color, ChatFormatting.BOLD));
            } else {
                el = new GuiElementBuilder(Items.DYE.gray())
                        .setName(Component.literal("???").withStyle(ChatFormatting.DARK_GRAY));
            }

            el.addLoreLine(Component.literal(""));
            el.addLoreLine(Component.literal("Tier: " + creature.tier.label).withStyle(creature.tier.color));
            el.addLoreLine(Component.literal("Health: " + (int) creature.health + "hp").withStyle(ChatFormatting.RED));

            if (discovered) {
                el.addLoreLine(Component.literal("Kills: " + String.format("%,d", kills)).withStyle(ChatFormatting.YELLOW));
                el.addLoreLine(Component.literal("XP per kill: " + creature.tier.xpReward).withStyle(ChatFormatting.GREEN));
            } else {
                el.addLoreLine(Component.literal("Kill to discover!").withStyle(ChatFormatting.DARK_GRAY));
            }

            gui.setSlot(slot, el.build());
            slot++;
            if (slot % 9 == 8) slot += 2; // skip borders
        }

        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    // LEADERBOARD
    // ════════════════════════════════════════════════════════════

    public static void openLeaderboard(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("Fishing Leaderboard").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.black()).setName(Component.literal("")).build());
        }

        gui.setSlot(4, new GuiElementBuilder(Items.GOLDEN_APPLE)
                .setName(Component.literal("Fishing Leaderboard").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .glow().build());

        // Gather data from all server players
        if (PoliticalServer.server != null) {
            List<Map.Entry<String, FishingManager.PlayerFishingData>> sorted = new ArrayList<>();
            for (ServerPlayer sp : PoliticalServer.server.getPlayerList().getPlayers()) {
                String uuid = sp.getUUID().toString();
                sorted.add(Map.entry(uuid, FishingManager.getData(uuid)));
            }
            sorted.sort((a, b) -> Integer.compare(b.getValue().level, a.getValue().level));

            int slot = 10;
            int rank = 1;
            for (var entry : sorted) {
                if (rank > 7) break;
                String name = DataManager.getPlayerName(entry.getKey());
                if (name == null) name = entry.getKey().substring(0, 8);

                ChatFormatting rankColor = switch (rank) {
                    case 1 -> ChatFormatting.GOLD;
                    case 2 -> ChatFormatting.GRAY;
                    case 3 -> ChatFormatting.RED;
                    default -> ChatFormatting.WHITE;
                };

                gui.setSlot(slot, new GuiElementBuilder(Items.PLAYER_HEAD)
                        .setName(Component.literal("#" + rank + " " + name).withStyle(rankColor, ChatFormatting.BOLD))
                        .addLoreLine(Component.literal("Level: " + entry.getValue().level).withStyle(ChatFormatting.AQUA))
                        .addLoreLine(Component.literal("Fish: " + String.format("%,d", entry.getValue().fishCaught)).withStyle(ChatFormatting.GREEN))
                        .addLoreLine(Component.literal("Creatures: " + String.format("%,d", entry.getValue().creaturesKilled)).withStyle(ChatFormatting.RED))
                        .build());

                slot++;
                rank++;
            }
        }

        gui.setSlot(27, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    // CHAT STATS
    // ════════════════════════════════════════════════════════════

    public static void sendStats(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        FishingManager.PlayerFishingData data = FishingManager.getData(uuid);

        player.sendSystemMessage(Component.literal("═══ Fishing Stats ═══").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal("Level: " + data.level + " / " + FishingManager.MAX_LEVEL).withStyle(ChatFormatting.GREEN));

        int xpNeeded = data.level < FishingManager.MAX_LEVEL ? FishingManager.xpForLevel(data.level + 1) : 0;
        player.sendSystemMessage(Component.literal("XP: " + String.format("%,d", data.xp) + " / " + String.format("%,d", xpNeeded)).withStyle(ChatFormatting.YELLOW));

        FishingRods.RodTier rod = FishingRods.RodTier.fromId(data.equippedRod);
        player.sendSystemMessage(Component.literal("Rod: " + (rod != null ? rod.displayName : "None")).withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal("Fish Caught: " + String.format("%,d", data.fishCaught)).withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(Component.literal("Creatures Killed: " + String.format("%,d", data.creaturesKilled)).withStyle(ChatFormatting.RED));
        player.sendSystemMessage(Component.literal("Rarest Catch: " + data.rarestCatch).withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.literal("Nodes Unlocked: " + data.unlockedNodes.size() + "/" + FishingSkillTree.getAllNodes().size()).withStyle(ChatFormatting.GREEN));
    }

    public static void sendRodInfo(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        FishingManager.PlayerFishingData data = FishingManager.getData(uuid);
        FishingRods.RodTier rod = FishingRods.RodTier.fromId(data.equippedRod);

        if (rod == null) {
            player.sendSystemMessage(Component.literal("No rod equipped!").withStyle(ChatFormatting.RED));
            return;
        }

        player.sendSystemMessage(Component.literal("═══ " + rod.displayName + " ═══").withStyle(rod.color, ChatFormatting.BOLD));
        if (rod.catchSpeedBonus > 0)
            player.sendSystemMessage(Component.literal("  +" + rod.catchSpeedBonus + "% Catch Speed").withStyle(ChatFormatting.GREEN));
        if (rod.seaCreatureBonus > 0)
            player.sendSystemMessage(Component.literal("  +" + rod.seaCreatureBonus + "% Sea Creature Chance").withStyle(ChatFormatting.AQUA));
        if (rod.treasureBonus > 0)
            player.sendSystemMessage(Component.literal("  +" + rod.treasureBonus + "% Treasure Find").withStyle(ChatFormatting.GOLD));
        if (rod.lavaFishing > 0)
            player.sendSystemMessage(Component.literal("  Lava Fishing Enabled").withStyle(ChatFormatting.RED));
    }
}
