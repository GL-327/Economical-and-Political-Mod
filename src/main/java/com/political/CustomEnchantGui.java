package com.political;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The enchanting bench.
 *
 * <p>Works on whatever the player is holding, which is the whole interface. There is no slot to put
 * an item into and no way to lose one to a menu that closed at the wrong moment, because the item
 * never leaves the player's hand.</p>
 *
 * <p>The list is filtered to what will actually go on the held item. Showing an armour enchantment
 * beside a sword and then refusing it on click is the kind of thing that makes people think a menu
 * is broken.</p>
 */
public final class CustomEnchantGui {

    private CustomEnchantGui() {}

    private static final int SIZE = 54;

    public static void open(ServerPlayer player) {
        open(player, 0, null);
    }

    /** The bench. A null filter shows everything applicable, otherwise only that slot's set. */
    public static void open(ServerPlayer player, int page, CustomEnchant.Slot filter) {
        ItemStack held = player.getMainHandItem();

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Enchanting")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        if (held.isEmpty()) {
            GuiKit.header(gui, Items.ENCHANTING_TABLE, "Enchanting", ChatFormatting.DARK_PURPLE,
                    "Hold the item you want to enchant",
                    "Then right click the table again",
                    CustomEnchant.values().length + " enchantments exist");
            gui.setSlot(22, GuiKit.info(Items.BARRIER, "Nothing in hand", ChatFormatting.RED,
                    "Close this, hold a weapon, tool, bow or",
                    "piece of armour, and use the table again.",
                    "",
                    "The item never leaves your hand, so there",
                    "is nothing here that can eat it."));
            GuiKit.footer(gui, SIZE, player, null);
            gui.open();
            return;
        }

        Map<CustomEnchant, Integer> current = CustomEnchantManager.read(held);
        List<CustomEnchant> applicable = CustomEnchantManager.applicableTo(held);
        if (filter != null) {
            applicable = applicable.stream().filter(e -> e.slot == filter).toList();
        }

        GuiKit.header(gui, Items.ENCHANTING_TABLE, "Enchanting", ChatFormatting.DARK_PURPLE,
                "Working on " + held.getHoverName().getString(),
                current.size() + " of " + CustomEnchantManager.MAX_PER_ITEM
                        + " slots used, " + applicable.size() + " fit this item",
                "You have §a" + player.experienceLevel + " levels §7and §9"
                        + EnchantTableHandler.lapisHeld(player) + " lapis");

        // The item itself, so the player can see what they are about to change.
        GuiElementBuilder preview = new GuiElementBuilder(held.getItem())
                .setName(held.getHoverName().copy())
                .addLoreLine(Component.literal(""));
        if (current.isEmpty()) {
            preview.addLoreLine(Component.literal("§8Nothing on it yet."));
        } else {
            for (Map.Entry<CustomEnchant, Integer> e : current.entrySet()) {
                preview.addLoreLine(Component.literal("  " + e.getKey().loreLine(e.getValue())));
            }
        }
        gui.setSlot(4, preview.build());

        // Slot filters, so the eighty are browsable rather than one long scroll.
        int f = 45;
        gui.setSlot(f, tab(player, page, null, filter, Items.NETHER_STAR, "Everything"));
        gui.setSlot(f + 1, tab(player, page, CustomEnchant.Slot.WEAPON, filter,
                Items.DIAMOND_SWORD, "Weapons"));
        gui.setSlot(f + 2, tab(player, page, CustomEnchant.Slot.ARMOUR, filter,
                Items.DIAMOND_CHESTPLATE, "Armour"));
        gui.setSlot(f + 3, tab(player, page, CustomEnchant.Slot.TOOL, filter,
                Items.DIAMOND_PICKAXE, "Tools"));
        gui.setSlot(f + 4, tab(player, page, CustomEnchant.Slot.BOW, filter, Items.BOW, "Ranged"));
        gui.setSlot(f + 5, tab(player, page, CustomEnchant.Slot.ANY, filter,
                Items.AMETHYST_SHARD, "Universal"));

        List<GuiElement> entries = new ArrayList<>();
        for (CustomEnchant e : applicable) {
            int have = current.getOrDefault(e, 0);
            int next = Math.min(e.maxLevel, have + 1);
            int xp = e.xpFor(next);
            int lapis = e.lapisFor(next);
            boolean maxed = have >= e.maxLevel;
            boolean afford = player.experienceLevel >= xp
                    && EnchantTableHandler.lapisHeld(player) >= lapis;

            GuiElementBuilder b = new GuiElementBuilder(e.icon())
                    .setName(Component.literal(e.display).withStyle(e.colour, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7" + e.effect))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7Goes on §f" + e.slot.label))
                    .addLoreLine(Component.literal("§7Maximum §f" + CustomEnchant.roman(e.maxLevel)));

            if (have > 0) {
                b.addLoreLine(Component.literal("§7On this item §a"
                        + CustomEnchant.roman(have)));
                b.glow();
            }
            b.addLoreLine(Component.literal(""));

            if (maxed) {
                b.addLoreLine(Component.literal("§aAlready at maximum."));
                b.addLoreLine(Component.literal("§8Right click to strip it off."));
            } else {
                b.addLoreLine(Component.literal("§7" + CustomEnchant.roman(next) + " costs §a"
                        + xp + " levels §7and §9" + lapis + " lapis"));
                if (!afford) {
                    b.addLoreLine(Component.literal(player.experienceLevel < xp
                            ? "§cYou need " + (xp - player.experienceLevel) + " more levels."
                            : "§cYou need more lapis."));
                } else {
                    b.addLoreLine(Component.literal("§eClick to apply."));
                }
                if (have > 0) b.addLoreLine(Component.literal("§8Right click to strip it off."));
            }

            final int applyLevel = next;
            entries.add(b.setCallback((slot, type, action, sg) -> {
                ItemStack live = player.getMainHandItem();
                if (live.isEmpty()) {
                    player.sendSystemMessage(Component.literal("§cYou are not holding anything."));
                    return;
                }
                if (type.isRight) {
                    if (CustomEnchantManager.levelOf(live, e) <= 0) return;
                    CustomEnchantManager.remove(live, e);
                    player.sendSystemMessage(Component.literal("§7Stripped §f" + e.display
                            + " §7off " + live.getHoverName().getString() + "."));
                    open(player, page, filter);
                    return;
                }
                if (CustomEnchantManager.levelOf(live, e) >= e.maxLevel) return;
                int lv = Math.min(e.maxLevel, CustomEnchantManager.levelOf(live, e) + 1);
                int needXp = e.xpFor(lv);
                int needLapis = e.lapisFor(lv);
                if (!EnchantTableHandler.charge(player, needXp, needLapis)) {
                    player.sendSystemMessage(Component.literal("§cThat costs " + needXp
                            + " levels and " + needLapis + " lapis."));
                    return;
                }
                String refusal = CustomEnchantManager.apply(live, e, lv);
                if (refusal != null) {
                    // Give both back for a job that did not happen.
                    EnchantTableHandler.refund(player, needXp, needLapis);
                    player.sendSystemMessage(Component.literal("§c" + refusal));
                    return;
                }
                player.sendSystemMessage(Component.literal("§d✦ §f" + e.display + " "
                        + CustomEnchant.roman(lv) + " §7applied for §a" + needXp
                        + " levels §7and §9" + needLapis + " lapis"));
                if (player.level() instanceof ServerLevel level) {
                    level.playSound(null, player.blockPosition(),
                            SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.8f, 1.0f);
                }
                open(player, page, filter);
            }).build());
        }

        if (entries.isEmpty()) {
            gui.setSlot(22, GuiKit.info(Items.BARRIER, "Nothing fits", ChatFormatting.RED,
                    "No enchantment in that group goes on",
                    held.getHoverName().getString() + ".",
                    "",
                    "Try another group along the bottom."));
        }

        GuiKit.paint(gui, entries, page, GuiKit.LIST_SLOTS.length);
        GuiKit.pager(gui, SIZE, page, entries.size(), GuiKit.LIST_SLOTS.length,
                p -> open(player, p, filter));

        gui.setSlot(SIZE - 5, GuiKit.info(Items.WRITABLE_BOOK, "How this works",
                ChatFormatting.WHITE,
                "An enchantment here is written onto the",
                "item's own tooltip, which is why they work",
                "on all seven thousand items in the mod and",
                "need nothing installed on your client.",
                "",
                "One item carries " + CustomEnchantManager.MAX_PER_ITEM + " at once.",
                "Right click any of them to strip it back off.",
                "",
                "Paid in experience levels and lapis, the",
                "same two things vanilla always asked for.",
                "",
                "§8Sneak and right click the table for the",
                "§8vanilla screen instead."));

        GuiKit.footer(gui, SIZE, player, null);
        gui.open();
    }

    private static GuiElement tab(ServerPlayer player, int page, CustomEnchant.Slot slot,
                                  CustomEnchant.Slot active, net.minecraft.world.item.Item icon,
                                  String label) {
        boolean on = slot == active;
        int count = 0;
        for (CustomEnchant e : CustomEnchant.values()) {
            if (slot == null || e.slot == slot) count++;
        }
        return new GuiElementBuilder(icon)
                .setName(Component.literal(label)
                        .withStyle(on ? ChatFormatting.GREEN : ChatFormatting.GRAY,
                                ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7" + count + " enchantments"))
                .addLoreLine(Component.literal(on ? "§aShowing these" : "§8Click to show these"))
                .setCallback((s, t, a, sg) -> open(player, 0, slot))
                .build();
    }
}
