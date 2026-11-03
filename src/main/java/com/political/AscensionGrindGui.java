package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

/**
 * Heart of the Grind (HOTG) — Skyblock-style profile: Magic Find, collections, milestones.
 */
public final class AscensionGrindGui {

    private AscensionGrindGui() {}

    public static void openMain(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Heart of the Grind").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.black())
                    .setName(Component.literal(""))
                    .build());
        }
        for (int c = 0; c < 9; c++) {
            gui.setSlot(c, new GuiElementBuilder(Items.STAINED_GLASS_PANE.yellow()).setName(Component.literal("")).build());
            gui.setSlot(45 + c, new GuiElementBuilder(Items.STAINED_GLASS_PANE.orange()).setName(Component.literal("")).build());
        }
        for (int r = 1; r < 5; r++) {
            gui.setSlot(r * 9, new GuiElementBuilder(Items.STAINED_GLASS_PANE.yellow()).setName(Component.literal("")).build());
            gui.setSlot(r * 9 + 8, new GuiElementBuilder(Items.STAINED_GLASS_PANE.yellow()).setName(Component.literal("")).build());
        }

        String uuid = player.getUUID().toString();
        int mf = AscensionGrindManager.getMagicFind(uuid);
        long ores = AscensionGrindManager.getCollectionCount(uuid, "sector_ores");
        long hostiles = AscensionGrindManager.getCollectionCount(uuid, "sector_hostiles");
        int tierOres = AscensionGrindManager.getCollectionTier(uuid, "sector_ores");
        int tierHos = AscensionGrindManager.getCollectionTier(uuid, "sector_hostiles");
        long nextOres = AscensionGrindManager.getNextThresholdAfterTier(tierOres);
        long nextHos = AscensionGrindManager.getNextThresholdAfterTier(tierHos);

        gui.setSlot(4, new GuiElementBuilder(Items.GOLDEN_APPLE)
                .setName(Component.literal("Profile: Grind & Fortune").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Like Skyblock collections: mine ores").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("and slay hostiles to raise tiers.").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Higher Magic Find improves rare drops.").withStyle(ChatFormatting.DARK_GRAY))
                .glow()
                .build());

        gui.setSlot(13, new GuiElementBuilder(Items.RABBIT_FOOT)
                .setName(Component.literal("Magic Find").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Current: " + mf + " / 400").withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal("Boosts rare sector drops & RNG rolls.").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("+3 MF per collection tier up.").withStyle(ChatFormatting.DARK_AQUA))
                .build());

        gui.setSlot(20, new GuiElementBuilder(Items.DIAMOND_PICKAXE)
                .setName(Component.literal("Ore Collection").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Count: " + String.format("%,d", ores)).withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal("Tier: " + tierOres + " / 10").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(nextOres == Long.MAX_VALUE
                        ? "Max tier reached"
                        : "Next tier at: " + String.format("%,d", nextOres)).withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("Mine vanilla ores & ancient debris.").withStyle(ChatFormatting.DARK_GRAY))
                .build());

        gui.setSlot(24, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Component.literal("Hostile Collection").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Count: " + String.format("%,d", hostiles)).withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal("Tier: " + tierHos + " / 10").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(nextHos == Long.MAX_VALUE
                        ? "Max tier reached"
                        : "Next tier at: " + String.format("%,d", nextHos)).withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("Kill monsters & water mobs.").withStyle(ChatFormatting.DARK_GRAY))
                .build());

        gui.setSlot(31, new GuiElementBuilder(Items.PRISMARINE_SHARD)
                .setName(Component.literal("Rare drops").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Shards, stars, fragments roll from").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("mining & combat while MF is high.").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Tip: Hyper Catalyst doubles collection").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("progress for 60s (right-click item).").withStyle(ChatFormatting.YELLOW))
                .build());

        gui.setSlot(40, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("Ascension Titan buff").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Reach high collection tiers to unlock").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("the Ascension Titan player buff.").withStyle(ChatFormatting.GRAY))
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.BOOK)
                .setName(Component.literal("Commissions (flavor)").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Your daily grind: break ores, kill mobs,").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("open /customitems Ascension tab for crafts.").withStyle(ChatFormatting.GRAY))
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Close").withStyle(ChatFormatting.RED))
                .setCallback((a, b, c, d) -> gui.close())
                .build());

        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("Back to Skills Hub").withStyle(ChatFormatting.YELLOW))
                .setCallback((a, b, c, d) -> SkillsGui.openMainMenu(player))
                .build());

        gui.open();
    }
}
