package com.political;

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

public class ProgressionTreeGui {

    private static final int[] TREE_SLOTS = {
        37, 38, 39,  // Tiers 1-3: bottom row
        28, 30,      // Tiers 4-5: mid-bottom
        19, 21,      // Tiers 6-7: middle
        10, 12,      // Tiers 8-9: upper
        4            // Tier 10: mastery capstone
    };

    private static final int[][] PATH_CONNECTIONS = {
        {46, 47},    // T1→T2 horizontal
        {47, 48},    // T2→T3 horizontal
        {29},        // T3→T4 diagonal up-left (between 39→28 via 29)
        {29},        // reuse slot 29 for the junction
        {20},        // T5→T6 diagonal up-left (between 30→19 via 20)
        {20},        // reuse
        {11},        // T7→T8 diagonal up-left (between 21→10 via 11)
        {11},        // reuse
    };

    private static final String[][] STAT_TEMPLATES = {
        {"+%d%% melee damage", "+%d%% ranged damage", "+%d%% armor penetration", "+%d%% crit chance"},
        {"+%d%% mining speed", "+%d%% ore double-drop", "+%d%% gem find", "+%d blocks vein mine"},
        {"+%d%% crop growth", "+%d%% harvest yield", "+%d%% rare crop", "Auto-replant tier %d"},
        {"+%d%% catch speed", "+%d%% rare fish", "+%d%% treasure find", "+%d auto-fish"},
        {"+%d%% move speed", "+%d%% discovery XP", "+%d%% chest loot", "+%d waypoints"},
        {"+%d%% craft speed", "+%d%% durability", "+%d%% enchant boost", "+%d%% recipe discount"},
    };

    // ════════════════════════════════════════════════════════════
    // MAIN MENU - Heart of the Forest hub
    // ════════════════════════════════════════════════════════════

    public static void openMainMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Heart of the Forest (HOTF)").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD));

        String uuid = player.getUUID().toString();
        long skillPoints = ProgressionTreeManager.getSkillPoints(uuid);
        long lifetimePoints = ProgressionTreeManager.getLifetimePoints(uuid);
        int totalTiers = ProgressionTreeManager.getTotalTiers(uuid);

        fillBackground(gui, Items.BLACK_STAINED_GLASS_PANE);

        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
            .setName(Component.literal("§6§l✦ Heart of the Forest ✦"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Skill Points: §a" + String.format("%,d", skillPoints)))
            .addLoreLine(Component.literal("§7Lifetime Earned: §e" + String.format("%,d", lifetimePoints)))
            .addLoreLine(Component.literal("§7Total Tiers Unlocked: §b" + totalTiers + "§7/§f60"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§8Grow your power through the branches"))
            .addLoreLine(Component.literal("§8of the forest. Each branch strengthens"))
            .addLoreLine(Component.literal("§8a different aspect of your abilities."))
            .build());

        ProgressionTreeManager.Branch[] branches = ProgressionTreeManager.Branch.values();
        int[] branchSlots = {10, 11, 12, 14, 15, 16};

        for (int i = 0; i < branches.length && i < branchSlots.length; i++) {
            ProgressionTreeManager.Branch branch = branches[i];
            int slot = branchSlots[i];
            int tier = ProgressionTreeManager.getTier(uuid, branch);
            double bonus = ProgressionTreeManager.getBonus(uuid, branch);

            ItemStack icon = getBranchDisplayItem(branch, tier);

            List<Component> lore = new ArrayList<>();
            lore.add(Component.literal(""));
            lore.add(Component.literal("§7Tier: " + (tier > 0 ? "§a" + tier : "§c0") + "§7/§f10"));
            lore.add(Component.literal("§7Bonus: §b+" + (int)(bonus * 100) + "%"));
            lore.add(Component.literal(""));
            lore.add(buildProgressBar(tier, ProgressionTreeManager.MAX_TIERS));
            lore.add(Component.literal(""));

            if (tier < ProgressionTreeManager.MAX_TIERS) {
                int nextCost = ProgressionTreeManager.TIER_COSTS[tier]; // tier is 0-indexed for next
                lore.add(Component.literal("§7Next tier requires §e" + String.format("%,d", nextCost) + " §7lifetime pts"));
                lore.add(Component.literal("§7You have §" + (lifetimePoints >= nextCost ? "a" : "c") + String.format("%,d", lifetimePoints)));
            } else {
                lore.add(Component.literal("§6§l✦ FULLY MASTERED ✦"));
            }
            lore.add(Component.literal(""));
            lore.add(Component.literal("§eClick to open skill tree →"));

            final ProgressionTreeManager.Branch branchRef = branch;
            gui.setSlot(slot, new GuiElementBuilder(icon.getItem())
                .setName(Component.literal(branch.icon + " " + branch.name).withStyle(branch.color, ChatFormatting.BOLD))
                .setLore(lore)
                .setCallback((index, type, containerInput, slotGui) -> openBranchDetail(player, branchRef))
                .build());
        }

        gui.setSlot(38, new GuiElementBuilder(Items.TNT)
            .setName(Component.literal("§c§lReset Points"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Resets §call §7branch tiers to 0."))
            .addLoreLine(Component.literal("§7You will lose §c50% §7of your current"))
            .addLoreLine(Component.literal("§7skill points as a penalty."))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Current Points: §a" + String.format("%,d", skillPoints)))
            .addLoreLine(Component.literal("§7After Reset: §e" + String.format("%,d", skillPoints / 2)))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§c⚠ SHIFT-CLICK to confirm"))
            .setCallback((index, type, containerInput, slotGui) -> {
                if (type.shift) {
                    handleResetPoints(player);
                }
            })
            .build());

        gui.setSlot(40, buildQuickStatsItem(uuid));

        gui.setSlot(42, new GuiElementBuilder(Items.EMERALD)
            .setName(Component.literal("§a§lToken Shop"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Spend tokens earned from mastering"))
            .addLoreLine(Component.literal("§7branches on exclusive rewards."))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§oComing soon..."))
            .build());

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
            .setName(Component.literal("§c§lClose"))
            .setCallback((index, type, containerInput, slotGui) -> gui.close())
            .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    // BRANCH DETAIL - HOTM-style tree layout
    // ════════════════════════════════════════════════════════════

    public static void openBranchDetail(ServerPlayer player, ProgressionTreeManager.Branch branch) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal(branch.icon + " " + branch.name + " — Skill Tree").withStyle(branch.color));

        String uuid = player.getUUID().toString();
        int currentTier = ProgressionTreeManager.getTier(uuid, branch);
        long lifetime = ProgressionTreeManager.getLifetimePoints(uuid);

        fillBackground(gui, Items.GRAY_STAINED_GLASS_PANE);

        placePaths(gui, currentTier);

        int branchIndex = branch.ordinal();
        String[] templates = STAT_TEMPLATES[branchIndex];

        for (int t = 0; t < ProgressionTreeManager.MAX_TIERS; t++) {
            int tier = t + 1;
            int slot = TREE_SLOTS[t];
            int cost = ProgressionTreeManager.TIER_COSTS[t];
            int bonusPct = (int)(ProgressionTreeManager.TIER_BONUSES[t] * 100);
            boolean unlocked = tier <= currentTier;
            boolean isNext = tier == currentTier + 1;

            String tierLabel = tier == 10 ? "§6§l✦ MASTERY ✦" : "§fTier " + tier;

            GuiElementBuilder builder;
            if (unlocked) {
                builder = new GuiElementBuilder(Items.LIME_CONCRETE);
                builder.setName(Component.literal("§a✔ " + tierLabel));
            } else if (isNext) {
                builder = new GuiElementBuilder(Items.YELLOW_CONCRETE);
                builder.setName(Component.literal("§e➤ " + tierLabel));
            } else {
                builder = new GuiElementBuilder(Items.RED_CONCRETE);
                builder.setName(Component.literal("§c✖ " + tierLabel));
            }

            List<Component> lore = new ArrayList<>();
            lore.add(Component.literal(""));

            if (unlocked) {
                lore.add(Component.literal("§a§lUNLOCKED"));
                lore.add(Component.literal(""));
                lore.add(Component.literal("§7Bonuses at this tier:"));
                for (String tmpl : templates) {
                    lore.add(Component.literal("  §b" + String.format(tmpl, bonusPct)));
                }
            } else if (isNext) {
                lore.add(Component.literal("§e§lAVAILABLE TO UNLOCK"));
                lore.add(Component.literal(""));
                lore.add(Component.literal("§7Requires: §e" + String.format("%,d", cost) + " §7lifetime points"));
                lore.add(Component.literal("§7You have: §" + (lifetime >= cost ? "a" : "c") + String.format("%,d", lifetime)));
                lore.add(Component.literal(""));
                lore.add(buildProgressBar(lifetime, cost));
                lore.add(Component.literal(""));
                lore.add(Component.literal("§7Bonuses:"));
                for (String tmpl : templates) {
                    lore.add(Component.literal("  §b" + String.format(tmpl, bonusPct)));
                }
            } else {
                lore.add(Component.literal("§c§lLOCKED"));
                lore.add(Component.literal(""));
                lore.add(Component.literal("§7Requires: §cTier " + (tier - 1) + " §7unlocked first"));
                lore.add(Component.literal("§7Cost: §e" + String.format("%,d", cost) + " §7lifetime points"));
                lore.add(Component.literal(""));
                lore.add(Component.literal("§7Bonuses:"));
                for (String tmpl : templates) {
                    lore.add(Component.literal("  §8" + String.format(tmpl, bonusPct)));
                }
            }

            builder.setLore(lore);
            gui.setSlot(slot, builder.build());
        }

        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
            .setName(Component.literal("§e← Back to Heart of the Forest"))
            .setCallback((index, type, containerInput, slotGui) -> openMainMenu(player))
            .build());

        long skillPoints = ProgressionTreeManager.getSkillPoints(uuid);
        gui.setSlot(49, new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
            .setName(Component.literal("§a§lSkill Points"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Current: §a" + String.format("%,d", skillPoints)))
            .addLoreLine(Component.literal("§7Lifetime: §e" + String.format("%,d", lifetime)))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7" + branch.name + " Tier: §b" + currentTier + "§7/§f10"))
            .addLoreLine(Component.literal("§7Bonus: §b+" + (int)(ProgressionTreeManager.getBonus(uuid, branch) * 100) + "%"))
            .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    // PATH RENDERING - colored glass connections between tiers
    // ════════════════════════════════════════════════════════════

    private static void placePaths(SimpleGui gui, int currentTier) {
        int[][] paths = {
            {46},      // between T1(37) and T2(38)
            {47},      // between T2(38) and T3(39)
            {48, 29},  // T3(39) up to T4(28): right of T3 + above
            {29},      // T4(28) across to T5(30): slot 29 junction
            {20},      // T5(30) up to T6(19): slot 20
            {20},      // T6(19) across to T7(21): slot 20 junction
            {11},      // T7(21) up to T8(10): slot 11
            {11},      // T8(10) across to T9(12): slot 11 junction
            {3},       // T9(12) up to T10(4): slot 3
        };

        boolean[][] placed = new boolean[6][9];

        for (int p = 0; p < paths.length; p++) {
            int tierForPath = p + 1;
            boolean unlocked = tierForPath <= currentTier;

            for (int slot : paths[p]) {
                int row = slot / 9;
                int col = slot % 9;
                if (placed[row][col]) continue;
                placed[row][col] = true;

                gui.setSlot(slot, new GuiElementBuilder(
                    unlocked ? Items.LIME_STAINED_GLASS_PANE : Items.RED_STAINED_GLASS_PANE
                ).setName(Component.literal(unlocked ? "§a│" : "§c│")).build());
            }
        }

        int[][] extraPaths = {
            {38, 28, 30, 19, 21, 10, 12, 4}, // node slots handled by tier rendering
        };

        int[] vertLinks = {46, 47, 29, 20, 11, 3};
        for (int slot : vertLinks) {
            int row = slot / 9;
            int col = slot % 9;
            if (placed[row][col]) continue;
            placed[row][col] = true;

            int associatedTier = slotToPathTier(slot);
            boolean unlocked = associatedTier <= currentTier;

            gui.setSlot(slot, new GuiElementBuilder(
                unlocked ? Items.LIME_STAINED_GLASS_PANE : Items.RED_STAINED_GLASS_PANE
            ).setName(Component.literal(unlocked ? "§a│" : "§c│")).build());
        }
    }

    private static int slotToPathTier(int slot) {
        return switch (slot) {
            case 46 -> 1;
            case 47 -> 2;
            case 48, 29 -> 3;
            case 20 -> 5;
            case 11 -> 7;
            case 3 -> 9;
            default -> 0;
        };
    }

    // ════════════════════════════════════════════════════════════
    // RESET HANDLER
    // ════════════════════════════════════════════════════════════

    private static void handleResetPoints(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long currentPoints = ProgressionTreeManager.getSkillPoints(uuid);
        int totalTiers = ProgressionTreeManager.getTotalTiers(uuid);

        if (totalTiers == 0) {
            player.sendSystemMessage(Component.literal("§cYou have no tiers to reset!"), false);
            return;
        }

        for (ProgressionTreeManager.Branch branch : ProgressionTreeManager.Branch.values()) {
            resetBranchTier(uuid, branch);
        }

        long penalty = currentPoints / 2;
        setSkillPoints(uuid, currentPoints - penalty);
        ProgressionTreeManager.saveData();

        player.sendSystemMessage(Component.literal("")
            .append(Component.literal("§c§l⚠ RESET COMPLETE ⚠").withStyle(ChatFormatting.RED))
            .append(Component.literal("\n§7All tiers reset. Lost §c" + String.format("%,d", penalty) + " §7skill points.")), false);

        openMainMenu(player);
    }

    private static void resetBranchTier(String uuid, ProgressionTreeManager.Branch branch) {
        try {
            var field = ProgressionTreeManager.class.getDeclaredField("playerTiers");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            var map = (java.util.Map<String, java.util.Map<String, Integer>>) field.get(null);
            var tiers = map.get(uuid);
            if (tiers != null) {
                tiers.put(branch.name(), 0);
            }
        } catch (Exception ignored) {}
    }

    private static void setSkillPoints(String uuid, long amount) {
        try {
            var field = ProgressionTreeManager.class.getDeclaredField("playerSkillPoints");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            var map = (java.util.Map<String, Long>) field.get(null);
            map.put(uuid, Math.max(0, amount));
        } catch (Exception ignored) {}
    }

    // ════════════════════════════════════════════════════════════
    // SHARED UI HELPERS
    // ════════════════════════════════════════════════════════════

    /** Keeps the caller's colour as the edge, but gains the two tone frame. */
    private static void fillBackground(SimpleGui gui, net.minecraft.world.item.Item pane) {
        GuiKit.frame(gui, 54, pane);
    }

    private static Component buildProgressBar(int current, int max) {
        int filled = max > 0 ? Math.min(10, (current * 10) / max) : 0;
        StringBuilder bar = new StringBuilder("§7[");
        for (int i = 0; i < 10; i++) {
            bar.append(i < filled ? "§a■" : "§8■");
        }
        bar.append("§7] §f").append(current).append("§7/§f").append(max);
        return Component.literal(bar.toString());
    }

    private static Component buildProgressBar(long current, long required) {
        double ratio = required > 0 ? Math.min(1.0, (double) current / required) : 0;
        int filled = (int)(ratio * 10);
        StringBuilder bar = new StringBuilder("§7[");
        for (int i = 0; i < 10; i++) {
            bar.append(i < filled ? "§a■" : "§8■");
        }
        int pct = (int)(ratio * 100);
        bar.append("§7] §f").append(pct).append("%");
        return Component.literal(bar.toString());
    }

    private static GuiElementBuilder buildQuickStatsItem(String uuid) {
        return new GuiElementBuilder(Items.WRITTEN_BOOK)
            .setName(Component.literal("§e§l✦ Quick Stats ✦"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§c" + ProgressionTreeManager.Branch.COMBAT.icon + " Combat: §f+"
                + (int)(ProgressionTreeManager.getCombatDamageBonus(uuid) * 100) + "% damage"))
            .addLoreLine(Component.literal("§7" + ProgressionTreeManager.Branch.MINING.icon + " Mining: §f+"
                + (int)(ProgressionTreeManager.getMiningSpeedBonus(uuid) * 100) + "% speed"))
            .addLoreLine(Component.literal("§a" + ProgressionTreeManager.Branch.FARMING.icon + " Farming: §f+"
                + (int)(ProgressionTreeManager.getFarmingYieldBonus(uuid) * 100) + "% yield"))
            .addLoreLine(Component.literal("§b" + ProgressionTreeManager.Branch.FISHING.icon + " Fishing: §f+"
                + (int)(ProgressionTreeManager.getFishingLuckBonus(uuid) * 100) + "% luck"))
            .addLoreLine(Component.literal("§e" + ProgressionTreeManager.Branch.EXPLORATION.icon + " Exploration: §f+"
                + (int)(ProgressionTreeManager.getExplorationSpeedBonus(uuid) * 100) + "% speed"))
            .addLoreLine(Component.literal("§d" + ProgressionTreeManager.Branch.CRAFTING.icon + " Crafting: §f+"
                + (int)(ProgressionTreeManager.getCraftingEfficiencyBonus(uuid) * 100) + "% efficiency"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Total Tiers: §f" + ProgressionTreeManager.getTotalTiers(uuid) + "§7/§f60"));
    }

    private static ItemStack getBranchDisplayItem(ProgressionTreeManager.Branch branch, int tier) {
        if (tier >= 8) {
            return switch (branch) {
                case COMBAT -> new ItemStack(Items.NETHERITE_SWORD);
                case MINING -> new ItemStack(Items.NETHERITE_PICKAXE);
                case FARMING -> new ItemStack(Items.NETHERITE_HOE);
                case FISHING -> new ItemStack(Items.FISHING_ROD);
                case EXPLORATION -> new ItemStack(Items.ELYTRA);
                case CRAFTING -> new ItemStack(Items.SMITHING_TABLE);
            };
        } else if (tier >= 5) {
            return switch (branch) {
                case COMBAT -> new ItemStack(Items.DIAMOND_SWORD);
                case MINING -> new ItemStack(Items.DIAMOND_PICKAXE);
                case FARMING -> new ItemStack(Items.DIAMOND_HOE);
                case FISHING -> new ItemStack(Items.FISHING_ROD);
                case EXPLORATION -> new ItemStack(Items.DIAMOND_BOOTS);
                case CRAFTING -> new ItemStack(Items.CRAFTING_TABLE);
            };
        } else if (tier >= 1) {
            return switch (branch) {
                case COMBAT -> new ItemStack(Items.IRON_SWORD);
                case MINING -> new ItemStack(Items.IRON_PICKAXE);
                case FARMING -> new ItemStack(Items.IRON_HOE);
                case FISHING -> new ItemStack(Items.FISHING_ROD);
                case EXPLORATION -> new ItemStack(Items.IRON_BOOTS);
                case CRAFTING -> new ItemStack(Items.CRAFTING_TABLE);
            };
        } else {
            return new ItemStack(Items.BARRIER);
        }
    }
}
