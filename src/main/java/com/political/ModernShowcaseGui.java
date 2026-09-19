package com.political;


import com.political.economy.CoinManager;
import com.political.technique.TechniqueGui;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

/**
 * V7 Pillar C — a "what is new / featured" board that also showcases {@link ModernGuiTheme}, so
 * there is at least one screen built entirely on the modern toolkit. It summarises the latest
 * systems and gives each a button that opens the real screen. Reachable from the Civilization
 * Hub and from {@code /featured} or {@code /whatsnew}.
 *
 * <p>Purely a launcher and a summary. It reads no state that would change and writes nothing.
 */
public final class ModernShowcaseGui {

    private ModernShowcaseGui() {}

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("✦ Featured").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        // Modern toolkit dresses the whole screen.
        ModernGuiTheme.border(gui, 54);
        ModernGuiTheme.header(gui, "What Is New", "The latest systems, one click away");

        // ── featured systems ──
        gui.setSlot(20, ModernGuiTheme.button(Items.GOLD_INGOT,
                "Shop", ChatFormatting.GOLD,
                "Buy and sell with VAT going to the treasury.",
                "Chair sets the rate.",
                () -> ShopGui.openMainMenu(player)));

        gui.setSlot(21, ModernGuiTheme.button(Items.ENCHANTED_BOOK,
                "Techniques", ChatFormatting.LIGHT_PURPLE,
                "Learn combat techniques from manuals.",
                "Equip one and rank it up by using it.",
                () -> TechniqueGui.open(player)));

        gui.setSlot(22, ModernGuiTheme.button(Items.NETHER_STAR,
                "World Bosses", ChatFormatting.RED,
                "Schedule and fight world bosses.",
                "The hardest fights left in the world.",
                () -> WorldBossGui.openMainMenu(player)));
        gui.setSlot(23, ModernGuiTheme.button(Items.ENDER_EYE,
                "Expeditions", ChatFormatting.LIGHT_PURPLE,
                "Instanced procedural dungeons.",
                "Five tiers, bosses and spoils.",
                () -> ExpeditionGui.open(player)));

        gui.setSlot(24, ModernGuiTheme.button(Items.SHIELD,
                "Guilds & Orders", ChatFormatting.BLUE,
                "Cross-settlement brotherhoods.",
                "Ranks, treasury, perks and halls.",
                () -> GuildGui.open(player)));

        // ── stat pills across the middle, demonstrating the modern pill element ──
        long coins = CoinManager.getCoinsLong(player);
        gui.setSlot(29, ModernGuiTheme.pill(Items.SUNFLOWER,
                "Your Coins", formatCoins(coins), ChatFormatting.GOLD));
        gui.setSlot(31, ModernGuiTheme.pill(Items.EMERALD,
                "Treasury", formatCoins(com.political.government.Treasury.balance()), ChatFormatting.GREEN));
        gui.setSlot(33, ModernGuiTheme.pill(Items.NETHER_STAR,
                "Featured Systems", "5", ChatFormatting.AQUA));

        // ── a short summary card ──
        gui.setSlot(40, new GuiElementBuilder(Items.WRITTEN_BOOK)
                .setName(Component.literal("Highlights").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7National elections fill Chair and Vice."))
                .addLoreLine(Component.literal("§7Shop and auction VAT funds the treasury."))
                .addLoreLine(Component.literal("§7Learn techniques from crafted manuals."))
                .addLoreLine(Component.literal("§7Guilds bind settlements into orders."))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§8Everything here opens by clicking above."))
                .glow().build());

        // Footer note plus a back button into the hub.
        ModernGuiTheme.footer(gui, 54, "Open the hub for every system");
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back to Hub").withStyle(ChatFormatting.YELLOW))
                .setCallback((a, b, c, d) -> CivilizationHubGui.open(player))
                .build());

        gui.open();
    }

    private static String formatCoins(long coins) {
        return String.format(java.util.Locale.ROOT, "%,d", coins);
    }
}
