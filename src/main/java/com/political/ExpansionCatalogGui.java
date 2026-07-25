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
import java.util.function.Predicate;

/**
 * A second home for browsing the newer content: every V4, V5 and V6 item, grouped into sections
 * and paged. Built from the live recipe book, so anything registered as a recipe shows here without
 * per-item bookkeeping. Operators can click an item to receive it.
 */
public final class ExpansionCatalogGui {

    private ExpansionCatalogGui() {}

    private record Section(String label, ChatFormatting color, net.minecraft.world.item.Item icon,
                           Predicate<String> match) {}

    private static final List<Section> SECTIONS = List.of(
            new Section("Runeforge", ChatFormatting.AQUA, Items.AMETHYST_SHARD,
                    n -> n.startsWith("vx rune ")),
            new Section("Aether Arsenal", ChatFormatting.LIGHT_PURPLE, Items.NETHERITE_SWORD,
                    n -> n.contains("aether")),
            new Section("Relics & Companions", ChatFormatting.GOLD, Items.BUNDLE,
                    n -> n.contains("relic:") || n.contains("companion:") || n.contains("reforge stone")),
            new Section("Cursed Spirits & Sorcerer", ChatFormatting.DARK_PURPLE, Items.WITHER_SKELETON_SKULL,
                    n -> n.contains("curse") || n.contains("cursed") || n.contains("sorcerer")
                            || n.contains("domain") || n.contains("binding") || n.contains("special grade")
                            || n.contains("womb") || n.contains("grade ")),
            new Section("Magic Gear", ChatFormatting.LIGHT_PURPLE, Items.ENCHANTED_BOOK,
                    n -> n.startsWith("magic:") || n.contains("grimoire") || n.contains("catalyst")
                            || n.contains("robe") || n.contains("archmage") || n.contains("awakening")
                            || n.contains("relic of")),
            new Section("Expeditions", ChatFormatting.DARK_AQUA, Items.ENDER_EYE,
                    n -> n.contains("expedition") || n.contains("sigil") || n.contains("delver")
                            || n.contains("vaultwarden") || n.contains("abyssal circlet")),
            new Section("Guilds, Trade & Living World", ChatFormatting.GOLD, Items.CHEST_MINECART,
                    n -> n.contains("guild") || n.contains("caravan") || n.contains("trade")
                            || n.contains("merchant") || n.contains("bestiary") || n.contains("season")
                            || n.contains("hunter's horn") || n.contains("trophy") || n.contains("almanac")),
            new Section("Backrooms", ChatFormatting.YELLOW, Items.YELLOW_CONCRETE,
                    n -> n.contains("backrooms") || n.contains("almond") || n.contains("reality shard")
                            || n.contains("fluorescent") || n.contains("escape beacon")),
            new Section("Flagship & Trinkets", ChatFormatting.AQUA, Items.NETHER_STAR,
                    n -> n.startsWith("flagship:") || n.startsWith("sb trinket:")
                            || n.contains("crown of") || n.contains("voidwalker") || n.contains("ender phase"))
    );

    public static void openMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("New Content Catalog").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        WarGui.fill(gui, 54);

        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("V4 to Version X Items").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Everything added since the settlements update."))
                .addLoreLine(Component.literal("§8All of it is also in /recipe and /setrecipe."))
                .glow().build());

        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
        List<SlayerRecipes.Recipe> all = safeRecipes();
        for (int i = 0; i < SECTIONS.size() && i < slots.length; i++) {
            Section sec = SECTIONS.get(i);
            int count = 0;
            for (SlayerRecipes.Recipe r : all) {
                if (r.result != null && !r.result.isEmpty() && sec.match().test(lc(r.name))) count++;
            }
            final int idx = i;
            gui.setSlot(slots[i], new GuiElementBuilder(sec.icon())
                    .setName(Component.literal(sec.label()).withStyle(sec.color(), ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7" + count + " items"))
                    .addLoreLine(Component.literal("§eClick to browse"))
                    .setCallback((a, b, c, d) -> openSection(player, idx, 0))
                    .build());
        }

        gui.setSlot(49, WarGui.back(() -> CivilizationHubGui.open(player)));
        gui.open();
    }

    private static void openSection(ServerPlayer player, int sectionIdx, int page) {
        Section sec = SECTIONS.get(sectionIdx);
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal(sec.label()).withStyle(sec.color(), ChatFormatting.BOLD));
        WarGui.fill(gui, 54);

        List<ItemStack> items = new ArrayList<>();
        for (SlayerRecipes.Recipe r : safeRecipes()) {
            if (r.result != null && !r.result.isEmpty() && sec.match().test(lc(r.name))) {
                items.add(r.result);
            }
        }

        int[] cells = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
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
            }
            gui.setSlot(cells[i], b.build());
        }

        final int fp = page;
        if (page > 0) {
            gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("← Previous").withStyle(ChatFormatting.YELLOW))
                    .setCallback((a, b, c, d) -> openSection(player, sectionIdx, fp - 1)).build());
        }
        if (page < totalPages - 1) {
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Next →").withStyle(ChatFormatting.YELLOW))
                    .setCallback((a, b, c, d) -> openSection(player, sectionIdx, fp + 1)).build());
        }
        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("← Sections §8(page " + (page + 1) + "/" + totalPages + ")")
                        .withStyle(ChatFormatting.YELLOW))
                .setCallback((a, b, c, d) -> openMenu(player)).build());
        gui.open();
    }

    private static List<SlayerRecipes.Recipe> safeRecipes() {
        try {
            return RecipeConfigManager.getRecipes();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private static String lc(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT);
    }
}
