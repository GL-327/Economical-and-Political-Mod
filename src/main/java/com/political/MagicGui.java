package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.*;

public class MagicGui {

    // ════════════════════════════════════════════════════════════
    // MAIN MENU
    // ════════════════════════════════════════════════════════════

    public static void openMainMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("✦ Magic System").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));

        RecipeGuiTheme.fillDoubleChestCatalog(gui);

        String uuid = player.getUUID().toString();
        MagicManager.PlayerMagicData data = MagicManager.getPlayerData(uuid);
        int maxMana = MagicManager.getMaxMana(uuid);

        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("✦ Magic Overview").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Magic Level: " + data.level + "/" + MagicManager.MAX_LEVEL).withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal("Magic XP: " + data.xp
                        + (data.level < MagicManager.MAX_LEVEL
                        ? " / " + MagicManager.cumulativeXpForLevel(data.level + 1) : " (MAX)"))
                        .withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal(""))
                .addLoreLine(createManaBar((int) data.mana, maxMana))
                .addLoreLine(Component.literal("Mana: " + (int) data.mana + "/" + maxMana).withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Spells Learned: " + data.unlockedSpells.size() + "/" + MagicManager.Spell.values().length).withStyle(ChatFormatting.YELLOW))
                .glow()
                .build());

        gui.setSlot(20, new GuiElementBuilder(Items.ENCHANTED_BOOK)
                .setName(Component.literal("Skill Tree").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Unlock spells and passives").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Nodes unlocked: " + data.skillTreeUnlocks.size()
                        + "/" + MagicSkillTree.NODES.size()).withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to open").withStyle(ChatFormatting.YELLOW))
                .setCallback((i, t, c, s) -> openSkillTree(player))
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Component.literal("Spellbook").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("View all learned spells").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to open").withStyle(ChatFormatting.YELLOW))
                .setCallback((i, t, c, s) -> openSpellbook(player))
                .build());

        gui.setSlot(24, new GuiElementBuilder(Items.BLAZE_ROD)
                .setName(Component.literal("Staff Workshop").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Apply spells to your staff").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Upgrade your staff tier").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to open").withStyle(ChatFormatting.YELLOW))
                .setCallback((i, t, c, s) -> openStaffWorkshop(player))
                .build());

        gui.setSlot(38, new GuiElementBuilder(Items.WITHER_SKELETON_SKULL)
                .setName(Component.literal("Bestiary").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Magic creature encyclopedia").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to open").withStyle(ChatFormatting.YELLOW))
                .setCallback((i, t, c, s) -> openBestiary(player))
                .build());

        MagicManager.MagicCommission activeComm = MagicManager.getActiveCommission(uuid);
        GuiElementBuilder commElem = new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Component.literal("Commissions").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Complete tasks for Magic XP").withStyle(ChatFormatting.GRAY));
        if (activeComm != null) {
            commElem.addLoreLine(Component.literal("Active: " + activeComm.displayName).withStyle(ChatFormatting.GREEN));
        }
        commElem.addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to open").withStyle(ChatFormatting.YELLOW))
                .setCallback((i, t, c, s) -> openCommissionsGui(player));
        gui.setSlot(40, commElem.build());

        MagicManager.Spell active = MagicManager.getActiveSpell(uuid);
        gui.setSlot(42, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal("Stats").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Active Spell: "
                        + (active != null ? active.displayName : "None")).withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal("Staff Tier: "
                        + MagicStaff.StaffTier.forLevel(data.level).name).withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("Mana Regen: "
                        + String.format("%.1f", 2.0f * (1 + MagicSkillTree.getManaRegenBonus(data.skillTreeUnlocks) / 100f))
                        + "/sec").withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal("Spell Damage Bonus: +"
                        + MagicSkillTree.getSpellDamageBonus(data.skillTreeUnlocks) + "%").withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal("Cooldown Reduction: -"
                        + MagicSkillTree.getCooldownReduction(data.skillTreeUnlocks) + "%").withStyle(ChatFormatting.LIGHT_PURPLE))
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    // SKILL TREE
    // ════════════════════════════════════════════════════════════

    private static final Item[] CAT_ICONS = {
            Items.LAPIS_LAZULI, Items.BLAZE_POWDER, Items.COAL, Items.GLOWSTONE_DUST, Items.AMETHYST_SHARD
    };
    private static final ChatFormatting[] CAT_COLORS = {
            ChatFormatting.BLUE, ChatFormatting.RED, ChatFormatting.DARK_GRAY,
            ChatFormatting.YELLOW, ChatFormatting.LIGHT_PURPLE
    };
    /** 28 content slots per category page — rows 2-5 of a 9x6 chest. */
    private static final int[] NODE_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    /**
     * Category picker. The old single-screen layout could only fit 7 nodes per category row,
     * so level-50 capstones were unreachable; each category now gets its own paginated page
     * where EVERY node renders and is clickable.
     */
    public static void openSkillTree(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("✦ Magic Skill Tree").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal("")).build());
        }

        String uuid = player.getUUID().toString();
        MagicManager.PlayerMagicData data = MagicManager.getPlayerData(uuid);

        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("✦ Skill Tree").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Level: " + data.level + " | XP: " + data.xp).withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal("Nodes: " + data.skillTreeUnlocks.size()
                        + "/" + MagicSkillTree.NODES.size()).withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("Spells reachable: " + MagicSkillTree.allUnlockableSpells().size()
                        + "/" + MagicManager.Spell.values().length).withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Pick a discipline below").withStyle(ChatFormatting.GRAY))
                .glow()
                .build());

        int[] catSlots = {20, 21, 22, 23, 24};

        for (int c = 0; c < MagicSkillTree.CATEGORIES.length && c < catSlots.length; c++) {
            final int catIndex = c;
            List<MagicSkillTree.SkillNode> nodes =
                    MagicSkillTree.getNodesForCategory(MagicSkillTree.CATEGORIES[c]);
            int owned = 0;
            int spells = 0;
            for (MagicSkillTree.SkillNode n : nodes) {
                if (data.skillTreeUnlocks.contains(n.id())) owned++;
                if (n.unlocksSpell() != null) spells++;
            }

            gui.setSlot(catSlots[c], new GuiElementBuilder(CAT_ICONS[c])
                    .setName(Component.literal(MagicSkillTree.CATEGORY_NAMES[c])
                            .withStyle(CAT_COLORS[c], ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Nodes: " + owned + "/" + nodes.size()).withStyle(ChatFormatting.AQUA))
                    .addLoreLine(Component.literal("Spells in this branch: " + spells).withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to view all nodes").withStyle(ChatFormatting.YELLOW))
                    .setCallback((idx, type, containerInput, slotGui) -> openSkillTreeCategory(player, catIndex, 0))
                    .build());
        }

        gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("Back").withStyle(ChatFormatting.RED))
                .setCallback((i, t, c, s) -> openMainMenu(player))
                .build());

        gui.open();
    }

    /** Renders EVERY node of one category, paginated 28 per page. */
    public static void openSkillTreeCategory(ServerPlayer player, int catIndex, int page) {
        if (catIndex < 0 || catIndex >= MagicSkillTree.CATEGORIES.length) catIndex = 0;
        final int cat = catIndex;

        List<MagicSkillTree.SkillNode> nodes =
                MagicSkillTree.getNodesForCategory(MagicSkillTree.CATEGORIES[cat]);
        int pageCount = Math.max(1, (nodes.size() + NODE_SLOTS.length - 1) / NODE_SLOTS.length);
        if (page < 0) page = 0;
        if (page >= pageCount) page = pageCount - 1;
        final int pg = page;

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("✦ " + MagicSkillTree.CATEGORY_NAMES[cat])
                .withStyle(CAT_COLORS[cat], ChatFormatting.BOLD));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal("")).build());
        }

        String uuid = player.getUUID().toString();
        MagicManager.PlayerMagicData data = MagicManager.getPlayerData(uuid);

        int owned = 0;
        for (MagicSkillTree.SkillNode n : nodes) {
            if (data.skillTreeUnlocks.contains(n.id())) owned++;
        }

        gui.setSlot(4, new GuiElementBuilder(CAT_ICONS[cat])
                .setName(Component.literal(MagicSkillTree.CATEGORY_NAMES[cat])
                        .withStyle(CAT_COLORS[cat], ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Level: " + data.level + " | XP: " + data.xp).withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal("Branch nodes: " + owned + "/" + nodes.size()).withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("Page " + (pg + 1) + "/" + pageCount).withStyle(ChatFormatting.GRAY))
                .glow()
                .build());

        int start = pg * NODE_SLOTS.length;
        for (int i = 0; i < NODE_SLOTS.length && start + i < nodes.size(); i++) {
            MagicSkillTree.SkillNode node = nodes.get(start + i);
            boolean unlocked = data.skillTreeUnlocks.contains(node.id());
            boolean canUnlock = !unlocked
                    && data.level >= node.levelRequired()
                    && data.xp >= node.xpCost()
                    && (node.parentId() == null || data.skillTreeUnlocks.contains(node.parentId()));

            Item icon;
            ChatFormatting nameColor;
            if (unlocked) {
                icon = Items.LIME_STAINED_GLASS_PANE;
                nameColor = ChatFormatting.GREEN;
            } else if (canUnlock) {
                icon = Items.YELLOW_STAINED_GLASS_PANE;
                nameColor = ChatFormatting.YELLOW;
            } else {
                icon = Items.RED_STAINED_GLASS_PANE;
                nameColor = ChatFormatting.RED;
            }

            GuiElementBuilder elem = new GuiElementBuilder(icon)
                    .setName(Component.literal(node.name()).withStyle(nameColor, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal(node.description()).withStyle(ChatFormatting.GRAY));

            if (node.unlocksSpell() != null) {
                MagicManager.Spell spell = MagicManager.Spell.byName(node.unlocksSpell());
                String spellName = spell != null ? spell.displayName : node.unlocksSpell();
                elem.addLoreLine(Component.literal("Unlocks: " + spellName).withStyle(ChatFormatting.AQUA));
            }

            elem.addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Cost: " + node.xpCost() + " XP").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal("Level Required: " + node.levelRequired()).withStyle(ChatFormatting.DARK_GRAY));

            if (unlocked) {
                elem.addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("UNLOCKED").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
                elem.glow();
            } else if (canUnlock) {
                elem.addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("Click to unlock!").withStyle(ChatFormatting.YELLOW));
            } else {
                if (data.level < node.levelRequired()) {
                    elem.addLoreLine(Component.literal("Requires level " + node.levelRequired())
                            .withStyle(ChatFormatting.RED));
                }
                if (data.xp < node.xpCost()) {
                    elem.addLoreLine(Component.literal("Requires " + node.xpCost() + " XP")
                            .withStyle(ChatFormatting.RED));
                }
                if (node.parentId() != null && !data.skillTreeUnlocks.contains(node.parentId())) {
                    MagicSkillTree.SkillNode parent = MagicSkillTree.getNode(node.parentId());
                    elem.addLoreLine(Component.literal("Requires: "
                            + (parent != null ? parent.name() : node.parentId()))
                            .withStyle(ChatFormatting.RED));
                }
            }

            final String nodeId = node.id();
            if (canUnlock) {
                elem.setCallback((idx, type, containerInput, slotGui) -> {
                    if (MagicManager.unlockSkillNode(player, nodeId)) {
                        MagicSkillTree.SkillNode n2 = MagicSkillTree.getNode(nodeId);
                        player.sendSystemMessage(Component.literal("✦ Unlocked: "
                                + (n2 != null ? n2.name() : nodeId) + "!")
                                .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
                        openSkillTreeCategory(player, cat, pg);
                    }
                });
            }

            gui.setSlot(NODE_SLOTS[i], elem.build());
        }

        if (pg > 0) {
            gui.setSlot(48, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("◀ Previous Page").withStyle(ChatFormatting.YELLOW))
                    .setCallback((i, t, c, s) -> openSkillTreeCategory(player, cat, pg - 1))
                    .build());
        }
        if (pg < pageCount - 1) {
            gui.setSlot(50, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Next Page ▶").withStyle(ChatFormatting.YELLOW))
                    .setCallback((i, t, c, s) -> openSkillTreeCategory(player, cat, pg + 1))
                    .build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Back").withStyle(ChatFormatting.RED))
                .setCallback((i, t, c, s) -> openSkillTree(player))
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    // SPELLBOOK
    // ════════════════════════════════════════════════════════════

    public static void openSpellbook(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("✦ Spellbook").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));

        RecipeGuiTheme.fillDoubleChestCatalog(gui);

        String uuid = player.getUUID().toString();
        MagicManager.Spell active = MagicManager.getActiveSpell(uuid);
        MagicManager.Spell[] spells = MagicManager.Spell.values();
        int[] slots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43,
                46, 47, 48, 52, 53
        };

        for (int i = 0; i < spells.length && i < slots.length; i++) {
            MagicManager.Spell spell = spells[i];
            boolean learned = MagicManager.hasSpell(uuid, spell);
            boolean isActive = spell == active;

            GuiElementBuilder elem = new GuiElementBuilder(learned ? spell.icon : Items.GRAY_DYE)
                    .setName(Component.literal(spell.displayName)
                            .withStyle(learned ? spell.color : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal(spell.description).withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Mana Cost: " + spell.manaCost).withStyle(ChatFormatting.AQUA))
                    .addLoreLine(Component.literal("Cooldown: "
                            + String.format("%.1f", spell.cooldownTicks / 20.0) + "s").withStyle(ChatFormatting.YELLOW));

            if (spell.damage > 0) {
                elem.addLoreLine(Component.literal("Damage: " + (int) spell.damage).withStyle(ChatFormatting.RED));
            }
            if (spell.levelRequired > 1) {
                elem.addLoreLine(Component.literal("Requires Level: " + spell.levelRequired).withStyle(ChatFormatting.DARK_GRAY));
            }

            if (learned && isActive) {
                elem.addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("ACTIVE ON STAFF").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
                elem.glow();
            } else if (learned) {
                elem.addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("LEARNED").withStyle(ChatFormatting.AQUA));
            } else {
                elem.addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("LOCKED — Unlock in Skill Tree").withStyle(ChatFormatting.RED));
            }

            gui.setSlot(slots[i], elem.build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("Back").withStyle(ChatFormatting.RED))
                .setCallback((i, t, c, s) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    // STAFF WORKSHOP
    // ════════════════════════════════════════════════════════════

    public static void openStaffWorkshop(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("✦ Staff Workshop").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        RecipeGuiTheme.fillDoubleChestCatalog(gui);

        String uuid = player.getUUID().toString();
        MagicManager.PlayerMagicData data = MagicManager.getPlayerData(uuid);
        MagicStaff.StaffTier currentTier = MagicStaff.StaffTier.forLevel(data.level);

        gui.setSlot(4, new GuiElementBuilder(Items.BLAZE_ROD)
                .setName(Component.literal("✦ " + currentTier.name).withStyle(currentTier.color, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Your current staff tier").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Damage: +" + currentTier.damage).withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal("Mana Reduction: " + currentTier.manaCostReduction + "%").withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to receive staff!").withStyle(ChatFormatting.YELLOW))
                .glow()
                .setCallback((i, t, c, s) -> {
                    MagicManager.Spell activeSpell = MagicManager.getActiveSpell(uuid);
                    ItemStack staff = MagicStaff.createStaff(currentTier,
                            activeSpell != null ? activeSpell.name() : null);
                    if (player.addItem(staff)) {
                        player.sendSystemMessage(Component.literal("✦ Received " + currentTier.name + "!")
                                .withStyle(ChatFormatting.GREEN));
                    } else {
                        player.sendSystemMessage(Component.literal("Inventory full!")
                                .withStyle(ChatFormatting.RED));
                    }
                })
                .build());

        int[] staffSpellSlots = {
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };
        int si = 0;
        for (MagicManager.Spell spell : MagicManager.Spell.values()) {
            if (!MagicManager.hasSpell(uuid, spell)) continue;
            if (si >= staffSpellSlots.length) break;
            int slotIdx = staffSpellSlots[si++];

            boolean isActive = spell == MagicManager.getActiveSpell(uuid);
            GuiElementBuilder elem = new GuiElementBuilder(spell.icon)
                    .setName(Component.literal(spell.displayName).withStyle(spell.color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Mana: " + spell.manaCost + " | CD: "
                            + String.format("%.1f", spell.cooldownTicks / 20.0) + "s").withStyle(ChatFormatting.GRAY));

            if (isActive) {
                elem.addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("CURRENTLY APPLIED").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
                elem.glow();
            } else {
                elem.addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("Click to apply to staff").withStyle(ChatFormatting.YELLOW));
            }

            final MagicManager.Spell spellRef = spell;
            elem.setCallback((i, t, c, s) -> {
                MagicManager.setActiveSpell(uuid, spellRef);
                MagicManager.saveData();
                player.sendSystemMessage(Component.literal("✦ Applied " + spellRef.displayName + " to your staff!")
                        .withStyle(ChatFormatting.GREEN));
                openStaffWorkshop(player);
            });

            gui.setSlot(slotIdx, elem.build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("Back").withStyle(ChatFormatting.RED))
                .setCallback((i, t, c, s) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    // BESTIARY
    // ════════════════════════════════════════════════════════════

    public static void openBestiary(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("✦ Bestiary").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));

        RecipeGuiTheme.fillChest4Hub(gui);

        MagicManager.MagicCreature[] creatures = MagicManager.MagicCreature.values();
        Item[] icons = {
                Items.BLAZE_POWDER, Items.ENDER_PEARL, Items.IRON_BLOCK, Items.SPIDER_EYE, Items.SCULK_SHRIEKER,
                Items.PHANTOM_MEMBRANE, Items.MAGMA_CREAM, Items.GLOWSTONE_DUST, Items.ECHO_SHARD
        };
        int[] slots = {9, 10, 11, 12, 13, 14, 15, 16, 17};

        for (int i = 0; i < creatures.length; i++) {
            MagicManager.MagicCreature cr = creatures[i];
            gui.setSlot(slots[i], new GuiElementBuilder(icons[i])
                    .setName(Component.literal("✦ " + cr.displayName).withStyle(cr.color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Rarity: " + cr.rarity).withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("Health: " + cr.health + " HP").withStyle(ChatFormatting.RED))
                    .addLoreLine(Component.literal("XP Reward: " + cr.xpReward + " Magic XP").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal(getCreatureDesc(cr)).withStyle(ChatFormatting.DARK_GRAY))
                    .build());
        }

        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("Back").withStyle(ChatFormatting.RED))
                .setCallback((i, t, c, s) -> openMainMenu(player))
                .build());

        gui.open();
    }

    private static String getCreatureDesc(MagicManager.MagicCreature c) {
        return switch (c) {
            case ARCANE_WISP   -> "Blaze variant, drops mana crystals";
            case VOID_WALKER   -> "Enderman variant, teleports frequently";
            case CRYSTAL_GOLEM -> "Iron golem variant, reflects spells";
            case SHADOW_MAGE   -> "Witch variant, casts debuffs";
            case RIFT_GUARDIAN  -> "Warden variant, legendary magic boss";
            case STORM_PHANTOM -> "Aerial arcane predator with burst dives";
            case ASHEN_HULK -> "Brutal nether juggernaut with huge swings";
            case CELESTIAL_SERAPH -> "Fast astral striker with evasive movement";
            case ABYSS_DEVOURER -> "Mythic siege beast from the deep rift";
        };
    }

    // ════════════════════════════════════════════════════════════
    // MAGIC COMMISSIONS
    // ════════════════════════════════════════════════════════════

    public static void openCommissionsGui(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("✦ Magic Commissions").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));

        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal("")).build());
        }

        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("✦ Magic Commissions").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("3 random offers rotate every 30 minutes").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Pick one to track. Complete for Magic XP.").withStyle(ChatFormatting.DARK_GRAY))
                .glow()
                .build());

        String uuid = player.getUUID().toString();
        MagicManager.MagicCommission activeCommission = MagicManager.getActiveCommission(uuid);
        int progress = MagicManager.getCommissionProgress(uuid);
        List<MagicManager.MagicCommission> offered = MagicManager.getCurrentCommissionOffers();

        Item[] icons = {Items.ENDER_PEARL, Items.AMETHYST_SHARD, Items.BLAZE_ROD};

        for (int i = 0; i < offered.size() && i < 3; i++) {
            MagicManager.MagicCommission commission = offered.get(i);
            boolean isActive = activeCommission == commission;

            GuiElementBuilder elem = new GuiElementBuilder(icons[i])
                    .setName(Component.literal(commission.displayName).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal(commission.description).withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("Reward: " + commission.xpReward + " Magic XP").withStyle(ChatFormatting.GOLD));

            if (isActive) {
                elem.addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("Progress: " + progress + "/" + commission.target).withStyle(ChatFormatting.GREEN))
                        .addLoreLine(Component.literal("Already tracking!").withStyle(ChatFormatting.YELLOW));
                elem.glow();
            } else if (activeCommission != null) {
                elem.addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("Finish current commission first!").withStyle(ChatFormatting.RED));
            } else {
                elem.addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("Click to start!").withStyle(ChatFormatting.YELLOW));
            }

            final MagicManager.MagicCommission commRef = commission;
            elem.setCallback((idx, type, containerInput, slotGui) -> {
                if (MagicManager.canStartCommission(player.getUUID().toString())) {
                    MagicManager.startCommission(player, commRef);
                    openCommissionsGui(player);
                }
            });

            gui.setSlot(11 + i, elem.build());
        }

        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("Back").withStyle(ChatFormatting.RED))
                .setCallback((i, t, c, s) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════════════════════════

    private static Component createManaBar(int current, int max) {
        int filled = max > 0 ? (int) ((float) current / max * 20) : 0;
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < 20; i++) bar.append(i < filled ? "█" : "░");
        bar.append("]");
        return Component.literal(bar.toString()).withStyle(ChatFormatting.AQUA);
    }
}
