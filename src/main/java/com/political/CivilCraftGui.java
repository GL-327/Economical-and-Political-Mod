package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public class CivilCraftGui {

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("⚔ CivilCraft Version X Commands"));

        // Decorative border
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.yellow())
                        .setName(Component.literal("")).build());
            } else {
                gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray())
                        .setName(Component.literal("")).build());
            }
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("⚔ CIVILCRAFT").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Version: X").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("Tip: /modhelp lists every text command").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Click below to run a command").withStyle(ChatFormatting.YELLOW))
                .glow().build());

        // ════════════════════════════════════════════════════════════
        // ECONOMY CATEGORY - Row 1 (with gaps for symmetry)
        // ════════════════════════════════════════════════════════════
        gui.setSlot(10, new GuiElementBuilder(Items.EMERALD)
                .setName(Component.literal("💰 Economy").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Financial commands").withStyle(ChatFormatting.GRAY))
                .glow().build());

        // Gap at 11
        gui.setSlot(12, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Component.literal("/shop").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Buy and sell items").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to open").withStyle(ChatFormatting.YELLOW))
                .setCallback((i, t, containerInput, slotGui) -> executeCommand(player, "shop"))
                .build());

        // Gap at 13
        gui.setSlot(14, new GuiElementBuilder(Items.CHEST)
                .setName(Component.literal("/bank").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Deposits & savings").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to open").withStyle(ChatFormatting.YELLOW))
                .setCallback((i, t, containerInput, slotGui) -> executeCommand(player, "bank"))
                .build());

        // Gap at 15
        gui.setSlot(16, new GuiElementBuilder(Items.DIAMOND)
                .setName(Component.literal("/credits").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Check credits").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to use").withStyle(ChatFormatting.YELLOW))
                .setCallback((i, t, containerInput, slotGui) -> executeCommand(player, "credits"))
                .build());

        // ════════════════════════════════════════════════════════════
        // BOUNTY HUNTING CATEGORY - Row 2 (with gaps)
        // ════════════════════════════════════════════════════════════
        gui.setSlot(19, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Component.literal("⚔ Bounty Hunting").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Hunt monsters").withStyle(ChatFormatting.GRAY))
                .glow().build());

        // Gap at 20
        gui.setSlot(21, new GuiElementBuilder(Items.NETHERITE_SWORD)
                .setName(Component.literal("/bounties").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("View bounty board").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to open").withStyle(ChatFormatting.YELLOW))
                .setCallback((i, t, containerInput, slotGui) -> executeCommand(player, "bounties"))
                .build());

        // Gap at 22
        gui.setSlot(23, new GuiElementBuilder(Items.TIPPED_ARROW)
                .setName(Component.literal("/customitems").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("View custom items").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to open").withStyle(ChatFormatting.YELLOW))
                .setCallback((i, t, containerInput, slotGui) -> executeCommand(player, "customitems"))
                .build());

        // Gap at 24
        gui.setSlot(25, new GuiElementBuilder(Items.BLAZE_ROD)
                .setName(Component.literal("/slayer").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("View slayer progress").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to use").withStyle(ChatFormatting.YELLOW))
                .setCallback((i, t, containerInput, slotGui) -> executeCommand(player, "slayer"))
                .build());

        // ════════════════════════════════════════════════════════════
        // GOVERNMENT CATEGORY - Row 3 (with gaps)
        // ════════════════════════════════════════════════════════════
        gui.setSlot(28, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Component.literal("⚖ Government").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Political commands").withStyle(ChatFormatting.GRAY))
                .glow().build());

        // Gap at 29
        gui.setSlot(30, new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Component.literal("/vote").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Vote in elections").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to use").withStyle(ChatFormatting.YELLOW))
                .setCallback((i, t, containerInput, slotGui) -> executeCommand(player, "vote"))
                .build());

        // Gap at 31
        gui.setSlot(32, new GuiElementBuilder(Items.REDSTONE_TORCH)
                .setName(Component.literal("/intercom").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Broadcast message").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to use").withStyle(ChatFormatting.YELLOW))
                .setCallback((i, t, containerInput, slotGui) -> executeCommand(player, "intercom"))
                .build());

        // Gap at 33
        gui.setSlot(34, new GuiElementBuilder(Items.SPYGLASS)
                .setName(Component.literal("/laws").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("View server laws").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to use").withStyle(ChatFormatting.YELLOW))
                .setCallback((i, t, containerInput, slotGui) -> executeCommand(player, "laws"))
                .build());

        // ════════════════════════════════════════════════════════════
        // UTILITIES CATEGORY - Row 4 (with gaps)
        // ════════════════════════════════════════════════════════════
        gui.setSlot(37, new GuiElementBuilder(Items.COMPASS)
                .setName(Component.literal("⭐ Utilities").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Helpful tools").withStyle(ChatFormatting.GRAY))
                .glow().build());

        // Gap at 38
        gui.setSlot(39, new GuiElementBuilder(Items.BOOK)
                .setName(Component.literal("/modhelp").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Show text commands").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to use").withStyle(ChatFormatting.YELLOW))
                .setCallback((i, t, containerInput, slotGui) -> executeCommand(player, "modhelp"))
                .build());

        // Gap at 40
        gui.setSlot(41, new GuiElementBuilder(Items.BEACON)
                .setName(Component.literal("/pbuff").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("View active buffs").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to use").withStyle(ChatFormatting.YELLOW))
                .setCallback((i, t, containerInput, slotGui) -> executeCommand(player, "pbuff"))
                .build());

        // Gap at 42
        gui.setSlot(43, new GuiElementBuilder(Items.RECOVERY_COMPASS)
                .setName(Component.literal("/checkpoint").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Manage checkpoints").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to use").withStyle(ChatFormatting.YELLOW))
                .setCallback((i, t, containerInput, slotGui) -> executeCommand(player, "checkpoint"))
                .build());

        // ════════════════════════════════════════════════════════════
        // PLAYER COMMANDS - Row 5 (with gaps)
        // ════════════════════════════════════════════════════════════
        gui.setSlot(46, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal("👤 Player Commands").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Personal tools").withStyle(ChatFormatting.GRAY))
                .glow().build());

        // Gap at 47
        gui.setSlot(48, new GuiElementBuilder(Items.ENDER_PEARL)
                .setName(Component.literal("/home").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Teleport to home").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to use").withStyle(ChatFormatting.YELLOW))
                .setCallback((i, t, containerInput, slotGui) -> executeCommand(player, "home"))
                .build());

        // Close button
        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("✖ Close").withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to close").withStyle(ChatFormatting.GRAY))
                .setCallback((i, t, containerInput, slotGui) -> gui.close())
                .build());

        gui.open();
    }

    private static void executeCommand(ServerPlayer player, String command) {
        PoliticalServer.server.execute(() -> {
            try {
                String raw = command.startsWith("/") ? command.substring(1) : command;
                PoliticalServer.server.getCommands().getDispatcher().execute(raw, player.createCommandSourceStack());
            } catch (Exception e) {
                player.sendSystemMessage(Component.literal("✖ Command not available: /" + command).withStyle(ChatFormatting.RED), false);
            }
        });
    }
}
