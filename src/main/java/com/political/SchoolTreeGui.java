package com.political;


import com.political.magic.MagicSchool;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.List;

/** School picker + HotM-style per-school node tree. */
public final class SchoolTreeGui {

    private SchoolTreeGui() {}

    // 12 slots so all schools render (8 original + 4 added in the V4 expansion).
    private static final int[] SCHOOL_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23};

    public static void openSchoolList(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("Schools of Magic"));

        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.black())
                    .setName(Component.literal("")).build());
        }

        MagicSchool[] schools = MagicSchool.values();
        for (int i = 0; i < schools.length && i < SCHOOL_SLOTS.length; i++) {
            MagicSchool s = schools[i];
            int unlocked = SchoolTreeManager.unlockedCount(uuid, s);
            int total = SchoolTree.nodesPerSchool(s);
            boolean awakened = SchoolTreeManager.isAwakened(uuid, s);

            GuiElementBuilder b = new GuiElementBuilder(iconFor(s))
                    .setName(Component.literal(s.symbol + " " + s.displayName)
                            .withStyle(s.color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7" + s.description))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7Nodes: §a" + unlocked + "§7/§f" + total))
                    .addLoreLine(Component.literal("§7Affinity: " + s.legacyColor
                            + SchoolTreeManager.getAffinity(player, s)))
                    .addLoreLine(Component.literal("§7Spell damage: §c+"
                            + SchoolTreeManager.getSchoolDamageBonus(uuid, s) + "%"))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§eClick to open tree"));
            if (awakened) {
                b.addLoreLine(Component.literal("§d§lAWAKENED"));
                b.glow();
            }
            final MagicSchool sel = s;
            b.setCallback((index, clickType, action, slotGui) -> openTree(player, sel));
            gui.setSlot(SCHOOL_SLOTS[i], b.build());
        }

        gui.setSlot(31, new GuiElementBuilder(Items.AMETHYST_SHARD)
                .setName(Component.literal("Arcane Shards").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Balance: §b" + SchoolTreeManager.getShards(uuid)))
                .addLoreLine(Component.literal("§8Earned from magic kills and levels."))
                .addLoreLine(Component.literal("§8Spent unlocking school nodes."))
                .build());

        gui.open();
    }

    public static void openTree(ServerPlayer player, MagicSchool school) {
        String uuid = player.getUUID().toString();
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal(school.displayName + " Tree"));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.black())
                    .setName(Component.literal("")).build());
        }

        List<SchoolTree.SchoolNode> nodes = SchoolTree.nodesFor(school);
        // Spine down the middle columns, side nodes on the flanks.
        int[] layout = {13, 22, 31, 40, 49, 12, 21, 30, 39, 48, 14, 23, 32, 41};

        for (int i = 0; i < nodes.size() && i < layout.length; i++) {
            SchoolTree.SchoolNode n = nodes.get(i);
            boolean owned = SchoolTreeManager.isUnlocked(uuid, n.id());
            boolean parentOk = n.parentId() == null || n.parentId().isEmpty()
                    || SchoolTreeManager.isUnlocked(uuid, n.parentId());
            int shards = SchoolTreeManager.getShards(uuid);
            boolean affordable = shards >= n.shardCost();
            boolean buyable = !owned && parentOk && affordable;

            GuiElementBuilder b = new GuiElementBuilder(
                    owned ? Items.CONCRETE.lime() : buyable ? Items.GOLD_BLOCK
                            : parentOk ? Items.CONCRETE.yellow() : Items.CONCRETE.gray())
                    .setName(Component.literal((n.capstone() ? "✦ " : "") + n.name())
                            .withStyle(owned ? ChatFormatting.GREEN : school.color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7" + n.description()))
                    .addLoreLine(Component.literal(""));

            if (n.dmgPct() > 0) b.addLoreLine(Component.literal("§7Spell damage: §c+" + n.dmgPct() + "%"));
            if (n.manaBonus() > 0) b.addLoreLine(Component.literal("§7Max mana: §b+" + n.manaBonus()));
            if (n.cdPct() > 0) b.addLoreLine(Component.literal("§7Cooldowns: §a-" + n.cdPct() + "%"));

            b.addLoreLine(Component.literal(""));
            b.addLoreLine(Component.literal("§7Cost: §b" + n.shardCost() + " shards"));
            b.addLoreLine(Component.literal("§7Magic level: §e" + n.levelRequired()));

            if (owned) {
                b.addLoreLine(Component.literal("§a§lUNLOCKED"));
                b.glow();
            } else if (!parentOk) {
                b.addLoreLine(Component.literal("§cRequires the previous node"));
            } else if (!affordable) {
                b.addLoreLine(Component.literal("§cNeed " + (n.shardCost() - shards) + " more shards"));
            } else {
                b.addLoreLine(Component.literal("§eClick to unlock"));
                final String nodeId = n.id();
                b.setCallback((index, clickType, action, slotGui) -> {
                    if (SchoolTreeManager.unlockNode(player, nodeId)) {
                        openTree(player, school);
                    }
                });
            }
            gui.setSlot(layout[i], b.build());
        }

        gui.setSlot(0, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← All Schools").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, clickType, action, slotGui) -> openSchoolList(player))
                .build());

        gui.setSlot(8, new GuiElementBuilder(Items.AMETHYST_SHARD)
                .setName(Component.literal("Shards: " + SchoolTreeManager.getShards(uuid))
                        .withStyle(ChatFormatting.LIGHT_PURPLE))
                .build());

        gui.open();
    }

    private static net.minecraft.world.item.Item iconFor(MagicSchool s) {
        return switch (s.prefix) {
            case "pyr" -> Items.BLAZE_POWDER;
            case "fro" -> Items.PACKED_ICE;
            case "sto" -> Items.LIGHTNING_ROD.weathering().unaffected();
            case "voi" -> Items.ENDER_PEARL;
            case "blo" -> Items.REDSTONE;
            case "ver" -> Items.MOSS_BLOCK;
            case "rad" -> Items.GLOWSTONE_DUST;
            default -> Items.HEAVY_CORE;
        };
    }
}
