package com.political;
import com.political.government.Treasury;



import com.political.bounty.SlayerItems;
import com.political.economy.CoinManager;
import com.political.items.CustomItemManager;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.List;
import java.util.Random;

/**
 * Large coin sinks that return progression items — integrates with {@link CoinSinkManager}.
 */
public final class RoyalTreasuryManager {
    private RoyalTreasuryManager() {}

    private static final long COST_DRAUGHT = 45_000_000L;
    private static final long COST_FRAGMENTS = 90_000_000L;
    private static final long COST_ASCENDANT = 220_000_000L;
    private static final long COST_SOVEREIGN = 550_000_000L;

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("\uD83C\uDFDB Royal Treasury").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Component.literal("")).build());
        }

        long bal = DataManager.getCoins(player.getUUID().toString());
        gui.setSlot(4, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Component.literal("Royal Treasury").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Sink excess coins for permanent progression items.").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Balance: " + String.format("%,d", bal)).withStyle(ChatFormatting.YELLOW))
                .build());

        gui.setSlot(10, offer(player, Items.HONEY_BOTTLE, "Mastery Draught",
                COST_DRAUGHT, "§7+120 Mastery XP on every track", () -> giveDraught(player)));

        gui.setSlot(11, offer(player, Items.GOLD_NUGGET, "Treasury Fragments ×5",
                COST_FRAGMENTS, "§7For §6Treasury Sigil §7craft (9 fragments)", () -> giveFragments(player, 5)));

        gui.setSlot(12, offer(player, Items.ENCHANTED_BOOK, "Ascendant Surge",
                COST_ASCENDANT, "§7Skill points in §fevery §7progression branch", () -> ascendantSurge(player)));

        gui.setSlot(13, offer(player, Items.NETHER_STAR, "Sovereign Hoard",
                COST_SOVEREIGN, "§7T3 Crafter Core + tomes + fragments", () -> sovereignHoard(player)));

        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((a, b, c, __slotGui) -> CoinSinkManager.openCoinSinkGui(player, 0))
                .build());

        gui.open();
    }

    private static GuiElement offer(ServerPlayer player, net.minecraft.world.item.Item icon, String title,
                                    long cost, String desc, Runnable onBuy) {
        long bal = DataManager.getCoins(player.getUUID().toString());
        boolean ok = bal >= cost;
        return new GuiElementBuilder(icon)
                .setName(Component.literal(title).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Cost: " + String.format("%,d", cost) + " coins").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal(desc).withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(ok ? "Click to purchase" : "Not enough coins").withStyle(ok ? ChatFormatting.GREEN : ChatFormatting.RED))
                .setCallback((i, t, containerInput, slotGui) -> {
                    if (CoinManager.removeCoins(player, cost)) {
                        onBuy.run();
                        open(player);
                    } else {
                        player.sendSystemMessage(Component.literal("§cNot enough coins."), false);
                    }
                })
                .build();
    }

    private static void giveDraught(ServerPlayer player) {
        CustomItemManager.CustomItem item = CustomItemManager.getItem("mastery_draught");
        if (item != null) {
            player.addItem(item.createStack(1));
        }
        player.sendSystemMessage(Component.literal("§aTreasury: §fMastery Draught delivered."), false);
    }

    private static void giveFragments(ServerPlayer player, int n) {
        CustomItemManager.CustomItem item = CustomItemManager.getItem("treasury_fragment");
        if (item != null) {
            player.addItem(item.createStack(n));
        }
        player.sendSystemMessage(Component.literal("§aTreasury: §f+" + n + " Treasury Fragments."), false);
    }

    private static void ascendantSurge(ServerPlayer player) {
        for (ProgressionTreeManager.Branch b : ProgressionTreeManager.Branch.values()) {
            ProgressionTreeManager.awardSkillPoints(player, b, 8);
        }
        player.sendSystemMessage(Component.literal("§aTreasury: §fAscendant Surge — skill points in all six branches."), false);
    }

    private static void sovereignHoard(ServerPlayer player) {
        player.addItem(SlayerItems.createCustomCrafterCoreT3());
        giveFragments(player, 6);
        Random r = new Random();
        List<String> tomes = List.of("skill_tome_combat", "skill_tome_mining", "skill_tome_farming");
        String pick = tomes.get(r.nextInt(tomes.size()));
        CustomItemManager.CustomItem t = CustomItemManager.getItem(pick);
        if (t != null) {
            player.addItem(t.createStack(1));
        }
        CustomItemManager.CustomItem com = CustomItemManager.getItem("sovereign_commendation");
        if (com != null) {
            player.addItem(com.createStack(1));
        }
        player.sendSystemMessage(Component.literal("§dTreasury: §fSovereign Hoard delivered!"), false);
    }
}
