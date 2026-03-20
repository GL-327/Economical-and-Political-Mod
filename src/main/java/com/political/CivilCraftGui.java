package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class CivilCraftGui {

    public static void open(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⚔ CivilCraft Commands"));

        // Decorative border
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                gui.setSlot(i, new GuiElementBuilder(Items.YELLOW_STAINED_GLASS_PANE)
                        .setName(Text.literal("")).build());
            } else {
                gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                        .setName(Text.literal("")).build());
            }
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("⚔ CIVILCRAFT").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click any command to use it!").formatted(Formatting.YELLOW))
                .glow().build());

        // ════════════════════════════════════════════════════════════
        // ECONOMY CATEGORY - Row 1 (with gaps for symmetry)
        // ════════════════════════════════════════════════════════════
        gui.setSlot(10, new GuiElementBuilder(Items.EMERALD)
                .setName(Text.literal("💰 Economy").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Financial commands").formatted(Formatting.GRAY))
                .glow().build());

        // Gap at 11
        gui.setSlot(12, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Text.literal("/shop").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Buy and sell items").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to open").formatted(Formatting.YELLOW))
                .setCallback((i, t, a) -> executeCommand(player, "shop"))
                .build());

        // Gap at 13
        gui.setSlot(14, new GuiElementBuilder(Items.CHEST)
                .setName(Text.literal("/bank").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Deposits & savings").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to open").formatted(Formatting.YELLOW))
                .setCallback((i, t, a) -> executeCommand(player, "bank"))
                .build());

        // Gap at 15
        gui.setSlot(16, new GuiElementBuilder(Items.DIAMOND)
                .setName(Text.literal("/credits").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Check credits").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to use").formatted(Formatting.YELLOW))
                .setCallback((i, t, a) -> executeCommand(player, "credits"))
                .build());

        // ════════════════════════════════════════════════════════════
        // BOUNTY HUNTING CATEGORY - Row 2 (with gaps)
        // ════════════════════════════════════════════════════════════
        gui.setSlot(19, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Text.literal("⚔ Bounty Hunting").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Hunt monsters").formatted(Formatting.GRAY))
                .glow().build());

        // Gap at 20
        gui.setSlot(21, new GuiElementBuilder(Items.NETHERITE_SWORD)
                .setName(Text.literal("/bounties").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("View bounty board").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to open").formatted(Formatting.YELLOW))
                .setCallback((i, t, a) -> executeCommand(player, "bounties"))
                .build());

        // Gap at 22
        gui.setSlot(23, new GuiElementBuilder(Items.TIPPED_ARROW)
                .setName(Text.literal("/customitems").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("View custom items").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to open").formatted(Formatting.YELLOW))
                .setCallback((i, t, a) -> executeCommand(player, "customitems"))
                .build());

        // Gap at 24
        gui.setSlot(25, new GuiElementBuilder(Items.BLAZE_ROD)
                .setName(Text.literal("/slayer").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("View slayer progress").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to use").formatted(Formatting.YELLOW))
                .setCallback((i, t, a) -> executeCommand(player, "slayer"))
                .build());

        // ════════════════════════════════════════════════════════════
        // GOVERNMENT CATEGORY - Row 3 (with gaps)
        // ════════════════════════════════════════════════════════════
        gui.setSlot(28, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Text.literal("⚖ Government").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Political commands").formatted(Formatting.GRAY))
                .glow().build());

        // Gap at 29
        gui.setSlot(30, new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Text.literal("/vote").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Vote in elections").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to use").formatted(Formatting.YELLOW))
                .setCallback((i, t, a) -> executeCommand(player, "vote"))
                .build());

        // Gap at 31
        gui.setSlot(32, new GuiElementBuilder(Items.REDSTONE_TORCH)
                .setName(Text.literal("/intercom").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Broadcast message").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to use").formatted(Formatting.YELLOW))
                .setCallback((i, t, a) -> executeCommand(player, "intercom"))
                .build());

        // Gap at 33
        gui.setSlot(34, new GuiElementBuilder(Items.SPYGLASS)
                .setName(Text.literal("/laws").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("View server laws").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to use").formatted(Formatting.YELLOW))
                .setCallback((i, t, a) -> executeCommand(player, "laws"))
                .build());

        // ════════════════════════════════════════════════════════════
        // UTILITIES CATEGORY - Row 4 (with gaps)
        // ════════════════════════════════════════════════════════════
        gui.setSlot(37, new GuiElementBuilder(Items.COMPASS)
                .setName(Text.literal("⭐ Utilities").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Helpful tools").formatted(Formatting.GRAY))
                .glow().build());

        // Gap at 38
        gui.setSlot(39, new GuiElementBuilder(Items.BOOK)
                .setName(Text.literal("/modhelp").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Show text commands").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to use").formatted(Formatting.YELLOW))
                .setCallback((i, t, a) -> executeCommand(player, "modhelp"))
                .build());

        // Gap at 40
        gui.setSlot(41, new GuiElementBuilder(Items.BEACON)
                .setName(Text.literal("/pbuff").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("View active buffs").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to use").formatted(Formatting.YELLOW))
                .setCallback((i, t, a) -> executeCommand(player, "pbuff"))
                .build());

        // Gap at 42
        gui.setSlot(43, new GuiElementBuilder(Items.RECOVERY_COMPASS)
                .setName(Text.literal("/checkpoint").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Manage checkpoints").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to use").formatted(Formatting.YELLOW))
                .setCallback((i, t, a) -> executeCommand(player, "checkpoint"))
                .build());

        // ════════════════════════════════════════════════════════════
        // PLAYER COMMANDS - Row 5 (with gaps)
        // ════════════════════════════════════════════════════════════
        gui.setSlot(46, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Text.literal("👤 Player Commands").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Personal tools").formatted(Formatting.GRAY))
                .glow().build());

        // Gap at 47
        gui.setSlot(48, new GuiElementBuilder(Items.ENDER_PEARL)
                .setName(Text.literal("/home").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Teleport to home").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to use").formatted(Formatting.YELLOW))
                .setCallback((i, t, a) -> executeCommand(player, "home"))
                .build());

        // Close button
        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("✖ Close").formatted(Formatting.RED))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to close").formatted(Formatting.GRAY))
                .setCallback((i, t, a) -> gui.close())
                .build());

        gui.open();
    }

    private static void executeCommand(ServerPlayerEntity player, String command) {
        PoliticalServer.server.execute(() -> {
            try {
                PoliticalServer.server.getCommandManager().getDispatcher().execute(command, player.getCommandSource());
            } catch (Exception e) {
                player.sendMessage(Text.literal("✖ Command not available: /" + command).formatted(Formatting.RED), false);
            }
        });
    }
}
