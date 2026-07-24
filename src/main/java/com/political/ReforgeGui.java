package com.political;

import com.mojang.brigadier.CommandDispatcher;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/** The reforge anvil. Hold an item, open this, and reroll what it carries. */
public final class ReforgeGui {

    private ReforgeGui() {}

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Reforge Anvil").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        WarGui.fill(gui, 54);

        ItemStack held = player.getMainHandItem();
        boolean ok = ReforgeManager.canReforge(held);
        Reforge current = ReforgeManager.get(held);

        gui.setSlot(4, new GuiElementBuilder(Items.ANVIL)
                .setName(Component.literal("Reforge Anvil").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7A reforge renames your item and adds"))
                .addLoreLine(Component.literal("§7its own stats on top of what it already has."))
                .addLoreLine(Component.literal("§8Rerolling replaces the old one, it never stacks."))
                .glow().build());

        // The item under the hammer.
        if (ok) {
            gui.setSlot(22, GuiElementBuilder.from(held.copy())
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7Currently: " + (current == null
                            ? "§8no reforge" : current.colour + current.display)))
                    .addLoreLine(Component.literal("§7Reforged §f" + ReforgeManager.rerollCount(held) + " §7times"))
                    .build());
        } else {
            gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                    .setName(Component.literal("Hold an item").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Hold one of this mod's items in your main hand."))
                    .build());
        }

        // Reroll.
        long cost = ok ? ReforgeManager.costFor(held) : 0;
        boolean afford = ok && CoinManager.getCoinsLong(player) >= cost;
        gui.setSlot(29, new GuiElementBuilder(ok && afford ? Items.GOLD_INGOT : Items.GRAY_CONCRETE)
                .setName(Component.literal("Reroll Reforge")
                        .withStyle(ok && afford ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY,
                                ChatFormatting.BOLD))
                .addLoreLine(Component.literal(ok
                        ? "§7Cost: §6" + cost + " coins"
                        : "§cNothing to reforge."))
                .addLoreLine(Component.literal(ok && !afford ? "§cYou cannot afford this." : ""))
                .addLoreLine(Component.literal("§8The cost rises each time you reroll this item."))
                .setCallback((a, b, c, d) -> {
                    if (ok && afford) {
                        ReforgeManager.reroll(player, player.getMainHandItem());
                        open(player);
                    }
                })
                .build());

        // Remove.
        gui.setSlot(33, new GuiElementBuilder(current != null ? Items.SHEARS : Items.GRAY_CONCRETE)
                .setName(Component.literal("Remove Reforge")
                        .withStyle(current != null ? ChatFormatting.YELLOW : ChatFormatting.DARK_GRAY,
                                ChatFormatting.BOLD))
                .addLoreLine(Component.literal(current != null
                        ? "§7Strip the reforge and go back to base."
                        : "§8This item has no reforge."))
                .setCallback((a, b, c, d) -> {
                    if (current != null) {
                        ReforgeManager.remove(player.getMainHandItem());
                        player.sendSystemMessage(Component.literal("§7Reforge stripped."));
                        open(player);
                    }
                })
                .build());

        // Browse the pool so players can see what they are chasing.
        gui.setSlot(31, new GuiElementBuilder(Items.BOOK)
                .setName(Component.literal("Possible Reforges").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7See every reforge this item can roll."))
                .setCallback((a, b, c, d) -> openPool(player, 0))
                .build());

        gui.setSlot(49, WarGui.back(() -> CivilizationHubGui.open(player)));
        gui.open();
    }

    private static final int[] CELLS = {
            10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    private static void openPool(ServerPlayer player, int page) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Reforge Pool").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        WarGui.fill(gui, 54);

        ItemStack held = player.getMainHandItem();
        List<Reforge> pool = ReforgeManager.canReforge(held)
                ? ReforgeManager.poolFor(held)
                : List.of(Reforge.values());

        int perPage = CELLS.length;
        int totalPages = Math.max(1, (pool.size() + perPage - 1) / perPage);
        int p = Math.max(0, Math.min(page, totalPages - 1));
        int start = p * perPage;

        for (int i = 0; i < perPage && start + i < pool.size(); i++) {
            Reforge r = pool.get(start + i);
            GuiElementBuilder b = new GuiElementBuilder(Items.PAPER)
                    .setName(Component.literal(r.display).withStyle(r.colour, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§8" + r.kind.name().toLowerCase()));
            for (String line : r.statLines()) b.addLoreLine(Component.literal(line));
            gui.setSlot(CELLS[i], b.build());
        }

        final int fp = p;
        if (p > 0) gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Previous").withStyle(ChatFormatting.YELLOW))
                .setCallback((a, b, c, d) -> openPool(player, fp - 1)).build());
        if (p < totalPages - 1) gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("Next →").withStyle(ChatFormatting.YELLOW))
                .setCallback((a, b, c, d) -> openPool(player, fp + 1)).build());

        gui.setSlot(49, new GuiElementBuilder(Items.ANVIL)
                .setName(Component.literal("← Back to the anvil §8(page " + (p + 1) + "/" + totalPages + ")")
                        .withStyle(ChatFormatting.YELLOW))
                .setCallback((a, b, c, d) -> open(player)).build());
        gui.open();
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("reforge").executes(ctx -> {
            open(ctx.getSource().getPlayerOrException());
            return 1;
        }));
    }
}
