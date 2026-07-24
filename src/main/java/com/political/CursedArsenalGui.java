package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The Cursed Arsenal forge board. Shows every arsenal piece pulled from the recipe book, so
 * anything registered as a recipe appears here without extra bookkeeping. Operators can click a
 * piece to receive it.
 */
public final class CursedArsenalGui {

    private CursedArsenalGui() {}

    private static boolean isArsenal(String name) {
        String n = name.toLowerCase(Locale.ROOT);
        return n.contains("arsenal") || n.contains("cursed blade") || n.contains("cursed sword")
                || n.contains("scythe") || n.contains("katana") || n.contains("gauntlet")
                || n.contains("cursed plate") || n.contains("cursed charm") || n.contains("reverse cursed");
    }

    public static void open(ServerPlayer player) {
        open(player, 0);
    }

    public static void open(ServerPlayer player, int page) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("The Cursed Arsenal").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        WarGui.fill(gui, 54);

        gui.setSlot(4, new GuiElementBuilder(Items.NETHERITE_SWORD)
                .setName(Component.literal("The Cursed Arsenal").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Weapons and armor forged from cursed energy,"))
                .addLoreLine(Component.literal("§7graded from Grade 4 up to Special Grade."))
                .addLoreLine(Component.literal("§8Higher grades need lower grades to forge."))
                .glow().build());

        List<ItemStack> items = new ArrayList<>();
        List<SlayerRecipes.Recipe> recipes;
        try {
            recipes = RecipeConfigManager.getRecipes();
        } catch (Exception e) {
            recipes = new ArrayList<>();
        }
        for (SlayerRecipes.Recipe r : recipes) {
            if (r.result != null && !r.result.isEmpty() && r.name != null && isArsenal(r.name)) {
                items.add(r.result);
            }
        }

        int[] cells = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34};
        int perPage = cells.length;
        int totalPages = Math.max(1, (items.size() + perPage - 1) / perPage);
        page = Math.max(0, Math.min(page, totalPages - 1));
        int start = page * perPage;
        boolean op = SpawnProtectionManager.isOperator(player);

        for (int i = 0; i < perPage && start + i < items.size(); i++) {
            ItemStack stack = items.get(start + i);
            GuiElementBuilder b = GuiElementBuilder.from(stack.copy());
            if (op) {
                b.addLoreLine(Component.literal(""));
                b.addLoreLine(Component.literal("§eClick to receive (operator)"));
                final ItemStack give = stack.copy();
                b.setCallback((a, c, d, g) -> {
                    if (!player.getInventory().add(give.copy())) player.drop(give.copy(), false);
                });
            } else {
                b.addLoreLine(Component.literal(""));
                b.addLoreLine(Component.literal("§8Forge it: see /recipe"));
            }
            gui.setSlot(cells[i], b.build());
        }

        if (items.isEmpty()) {
            gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                    .setName(Component.literal("Nothing forged yet").withStyle(ChatFormatting.RED))
                    .addLoreLine(Component.literal("§7Arsenal recipes load on server start."))
                    .build());
        }

        final int fp = page;
        if (page > 0) gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Previous").withStyle(ChatFormatting.YELLOW))
                .setCallback((a, b, c, d) -> open(player, fp - 1)).build());
        if (page < totalPages - 1) gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("Next →").withStyle(ChatFormatting.YELLOW))
                .setCallback((a, b, c, d) -> open(player, fp + 1)).build());

        gui.setSlot(49, WarGui.back(() -> CivilizationHubGui.open(player)));
        gui.open();
    }
}
