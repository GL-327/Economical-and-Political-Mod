package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * CivilCraft Help GUI - A clickable GUI showing all player commands.
 * Accessible via /civilcraft or /cc
 */
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
        // ECONOMY CATEGORY (Row 1)
        // ════════════════════════════════════════════════════════════
        gui.setSlot(10, new GuiElementBuilder(Items.EMERALD)
                .setName(Text.literal("💰 Economy").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Financial commands").formatted(Formatting.GRAY))
                .glow().build());

        gui.setSlot(11, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Text.literal("/shop").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Buy and sell items").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Convert credits → coins").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to open").formatted(Formatting.YELLOW))
                .setCallback((i, t, a) -> executeCommand(player, "shop"))
                .build());

        gui.setSlot(12, new GuiElementBuilder(Items.CHEST)
                .setName(Text.literal("/bank").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Deposits & savings").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Loans available").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to open").formatted(Formatting.YELLOW))
                .setCallback((i, t, a) -> executeCommand(player, "bank"))
                .build());

        gui.setSlot(13, new GuiElementBuilder(Items.DIAMOND)
                .setName(Text.literal("/credits").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Check your credit balance").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to use").formatted(Formatting.YELLOW))
                .setCallback((i, t, a) -> executeCommand(player, "credits"))
                .build());

        gui.setSlot(14, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Text.literal("/balance").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Check your coin balance").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to use").formatted(Formatting.YELLOW))
                .setCallback((i, t, a) -> executeCommand(player, "balance"))
                .build());

        gui.setSlot(15, new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
                .setName(Text.literal("/pay").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Pay another player").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to use").formatted(Formatting.YELLOW))
                .setCallback((i, t, a) -> executeCommand(player, "pay"))
                .build());

        gui.setSlot(16, new GuiElementBuilder(Items.HOPPER)
                .setName(Text.literal("/trade").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Trade with other players").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to use").formatted(Formatting.YELLOW))
                .setCallback((i, t, a) -> executeCommand(player, "trade"))
                .build());

        // ════════════════════════════════════════════════════════════
        // BOUNTY HUNTING CATEGORY (Row 2)
        // ════════════════════════════════════════════════════════════
        gui.setSlot(19, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Text.literal("⚔ Bounty Hunting").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Hunt monsters for rewards").formatted(Formatting.GRAY))
                .glow().build());

        gui.setSlot(20, new GuiElementBuilder(Items.NETHERITE_SWORD)
                .setName(Text.literal("/bounties").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("View bounty board").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Start hunting quests").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to open").formatted(Formatting.YELLOW))
                .setCallback((i, t, a) -> executeCommand(player, "bounties"))
                .build());

        gui.setSlot(21, new GuiElementBuilder(Items.TIPPED_ARROW)
                .setName(Text.literal("/customitems").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("View all custom items").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("See crafting recipes").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to open").formatted(Formatting.YELLOW))
                .setCallback((i, t, a) -> executeCommand(player, "customitems"))
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.BLAZE_ROD)
                .setName(Text.literal("/slayer").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("View slayer progress").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to use").formatted(Formatting.YELLOW))
                .setCallback((i, t, a) -> executeCommand(player, "slayer"))
                .build());

        gui.setSlot(23, new GuiElementBuilder(Items.ENCHANTED_BOOK)
                .setName(Text.literal("/skills").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("View your skills").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to use").formatted(Formatting.YELLOW))
                .setCallback((i, t, a) -> executeCommand(player, "skills"))
                .build());

        // ════════════════════════════════════════════════════════════
        // GOVERNMENT CATEGORY (Row 3)
        // ════════════════════════════════════════════════════════════
        gui.setSlot(28, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Text.literal("⚖ Government").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Political commands").formatted(Formatting.GRAY))
                .glow().build());

        gui.setSlot(29, new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Text.literal("/vote").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Vote in elections").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to use").formatted(Formatting.YELLOW))
                .setCallback((i, t, a) -> executeCommand(player, "vote"))
                .build());

        gui.setSlot(30, new GuiElementBuilder(Items.REDSTONE_TORCH)
                .setName(Text.literal("/intercom").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Broadcast server-wide").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to use").formatted(Formatting.YELLOW))
                .setCallback((i, t, a) -> executeCommand(player, "intercom"))
                .build());

        gui.setSlot(31, new GuiElementBuilder(Items.SPYGLASS)
                .setName(Text.literal("/laws").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("View server laws").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to use").formatted(Formatting.YELLOW))
                .setCallback((i, t, a) -> executeCommand(player, "laws"))
                .build());

        gui.setSlot(32, new GuiElementBuilder(Items.STRUCTURE_VOID)
                .setName(Text.literal("/court").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Legal system").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to use").formatted(Formatting.YELLOW))
                .setCallback((i, t, a) -> executeCommand(player, "court"))
                .build());

        // ════════════════════════════════════════════════════════════
        // UTILITIES CATEGORY (Row 4)
        // ════════════════════════════════════════════════════════════
        gui.setSlot(37, new GuiElementBuilder(Items.COMPASS)
                .setName(Text.literal("⭐ Utilities").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Helpful tools").formatted(Formatting.GRAY))
                .glow().build());

        gui.setSlot(38, new GuiElementBuilder(Items.BOOK)
                .setName(Text.literal("/modhelp").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Show text commands").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to use").formatted(Formatting.YELLOW))
                .setCallback((i, t, a) -> executeCommand(player, "modhelp"))
                .build());

        gui.setSlot(39, new GuiElementBuilder(Items.BEACON)
                .setName(Text.literal("/pbuff").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("View active buffs").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to use").formatted(Formatting.YELLOW))
                .setCallback((i, t, a) -> executeCommand(player, "pbuff"))
                .build());

        gui.setSlot(40, new GuiElementBuilder(Items.RECOVERY_COMPASS)
                .setName(Text.literal("/checkpoint").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Manage checkpoints").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to use").formatted(Formatting.YELLOW))
                .setCallback((i, t, a) -> executeCommand(player, "checkpoint"))
                .build());

        gui.setSlot(41, new GuiElementBuilder(Items.ENDER_PEARL)
                .setName(Text.literal("/home").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Teleport to home").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to use").formatted(Formatting.YELLOW))
                .setCallback((i, t, a) -> executeCommand(player, "home"))
                .build());

        gui.setSlot(42, new GuiElementBuilder(Items.CLOCK)
                .setName(Text.literal("/stats").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("View your statistics").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to use").formatted(Formatting.YELLOW))
                .setCallback((i, t, a) -> executeCommand(player, "stats"))
                .build());

        // ════════════════════════════════════════════════════════════
        // LEADERSHIP COMMANDS (Row 5 - only shown if applicable)
        // ════════════════════════════════════════════════════════════
        String uuid = player.getUuidAsString();
        boolean isChair = uuid.equals(DataManager.getChair());
        boolean isVice = uuid.equals(DataManager.getViceChair());
        boolean isDictator = DictatorManager.isDictator(uuid);

        if (isChair || isVice || isDictator) {
            gui.setSlot(46, new GuiElementBuilder(Items.GOLDEN_APPLE)
                    .setName(Text.literal("👑 Leadership").formatted(Formatting.GOLD, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Your special commands").formatted(Formatting.GRAY))
                    .glow().build());

            int slot = 47;
            if (isChair) {
                gui.setSlot(slot++, new GuiElementBuilder(Items.GOLD_BLOCK)
                        .setName(Text.literal("/chair").formatted(Formatting.AQUA))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("Chair government panel").formatted(Formatting.GRAY))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("▶ Click to open").formatted(Formatting.YELLOW))
                        .setCallback((i, t, a) -> executeCommand(player, "chair"))
                        .build());
            }
            if (isVice) {
                gui.setSlot(slot++, new GuiElementBuilder(Items.IRON_BLOCK)
                        .setName(Text.literal("/vicechair").formatted(Formatting.AQUA))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("Vice Chair panel").formatted(Formatting.GRAY))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("▶ Click to open").formatted(Formatting.YELLOW))
                        .setCallback((i, t, a) -> executeCommand(player, "vicechair"))
                        .build());
            }
            if (isDictator) {
                gui.setSlot(slot++, new GuiElementBuilder(Items.NETHERITE_BLOCK)
                        .setName(Text.literal("/dictator").formatted(Formatting.RED))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("Dictator control panel").formatted(Formatting.GRAY))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("▶ Click to open").formatted(Formatting.YELLOW))
                        .setCallback((i, t, a) -> executeCommand(player, "dictator"))
                        .build());
            }
        }

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
