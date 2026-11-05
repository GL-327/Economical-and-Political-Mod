package com.political.government;
import com.political.CivilizationHubGui;
import com.political.DataManager;
import com.political.TaxGui;



import com.political.party.PartyJoinGui;
import com.political.settlement.Settlement;
import com.political.settlement.WarGui;
import com.political.settlement.WarManager;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

/** Government overview: who rules, how they were elected, and the panels each office opens. */
public final class GovernmentGui {

    private GovernmentGui() {}

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Government").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        WarGui.fill(gui, 54);

        String uuid = player.getUUID().toString();
        String chair = DataManager.getChair();
        String vice = DataManager.getViceChair();
        String judge = DataManager.getJudge();

        gui.setSlot(4, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Component.literal("The National Government")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Held by the largest metropolis."))
                .glow().build());

        gui.setSlot(10, office(Items.GOLDEN_HELMET, "Chair", chair, ChatFormatting.RED,
                "Sets tax, interest and national perks."));
        gui.setSlot(11, office(Items.IRON_HELMET, "Vice Chair", vice, ChatFormatting.BLUE,
                "Second seat, two perk points."));
        gui.setSlot(12, office(Items.PAPER, "Judge", judge, ChatFormatting.YELLOW,
                "Impeachment, exile, pardons."));

        // Panels — only clickable by the office holder
        gui.setSlot(19, panel(player, Items.GOLD_INGOT, "Chair Panel",
                uuid.equals(chair), () -> ChairGui.open(player),
                "Only the Chair may open this."));
        gui.setSlot(20, panel(player, Items.IRON_INGOT, "Vice Chair Panel",
                uuid.equals(vice), () -> ViceChairGui.open(player),
                "Only the Vice Chair may open this."));
        gui.setSlot(21, panel(player, Items.WRITABLE_BOOK, "Judge Panel",
                uuid.equals(judge), () -> JudgeGui.open(player),
                "Only the Judge may open this."));

        // Universal panels
        gui.setSlot(23, new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
                .setName(Component.literal("National Perks").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Server-wide effects chosen by leadership."))
                .setCallback((a, b, c, d) -> PerksGui.openFrom(player, uuid.equals(DataManager.getChair()), () -> open(player)))
                .build());

        gui.setSlot(24, new GuiElementBuilder(Items.EMERALD)
                .setName(Component.literal("Political Parties").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Join, donate, buy party perks."))
                .setCallback((a, b, c, d) -> PartyJoinGui.open(player))
                .build());

        gui.setSlot(25, new GuiElementBuilder(Items.GOLD_NUGGET)
                .setName(Component.literal("Taxation").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Rate, debts and collection."))
                .addLoreLine(Component.literal("§8Settlements remit a share of income here."))
                .setCallback((a, b, c, d) -> TaxGui.openFrom(player, () -> open(player)))
                .build());

        // Settlement-level politics
        Settlement polity = WarManager.polityOf(player);
        GuiElementBuilder local = new GuiElementBuilder(Items.BELL)
                .setName(Component.literal("Local Politics").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        if (polity == null) {
            local.addLoreLine(Component.literal("§8You belong to no realm."));
            local.addLoreLine(Component.literal("§7Register at a settlement first."));
        } else {
            local.addLoreLine(Component.literal("§7Realm: §f" + polity.name));
            var leaders = polity.leaderMap();
            if (leaders == null || leaders.isEmpty()) {
                local.addLoreLine(Component.literal("§8No local leaders elected yet."));
            } else {
                leaders.forEach((role, id) -> local.addLoreLine(
                        Component.literal("§7" + role + ": §f" + DataManager.getPlayerName(id))));
            }
            local.addLoreLine(Component.literal(""));
            local.addLoreLine(Component.literal("§8Citizens vote automatically at term's end."));
            local.addLoreLine(Component.literal("§8They judge you on appeal, happiness and food."));
        }
        gui.setSlot(31, local.build());

        gui.setSlot(40, new GuiElementBuilder(Items.NETHERITE_SWORD)
                .setName(Component.literal("War Room").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Wars between rival realms."))
                .setCallback((a, b, c, d) -> WarGui.openFrom(player, () -> open(player)))
                .build());

        gui.setSlot(45, WarGui.back(com.political.core.gui.MenuTrail.backOr(player, () -> CivilizationHubGui.open(player))));
        gui.open();
    }

    private static eu.pb4.sgui.api.elements.GuiElement office(net.minecraft.world.item.Item icon,
                                                              String title, String holderUuid,
                                                              ChatFormatting color, String note) {
        return new GuiElementBuilder(icon)
                .setName(Component.literal(title).withStyle(color, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§f" + (holderUuid == null ? "§8vacant"
                        : DataManager.getPlayerName(holderUuid))))
                .addLoreLine(Component.literal("§8" + note))
                .build();
    }

    private static eu.pb4.sgui.api.elements.GuiElement panel(ServerPlayer player,
                                                             net.minecraft.world.item.Item icon,
                                                             String title, boolean allowed,
                                                             Runnable action, String denyNote) {
        GuiElementBuilder b = new GuiElementBuilder(allowed ? icon : Items.CONCRETE.gray())
                .setName(Component.literal(title)
                        .withStyle(allowed ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY,
                                ChatFormatting.BOLD))
                .addLoreLine(Component.literal(allowed ? "§eClick to open" : "§c" + denyNote));
        if (allowed) b.setCallback((a, c, d, e) -> action.run());
        return b.build();
    }
}
