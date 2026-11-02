package com.political;


import com.political.magic.MagicManager;
import com.political.settlement.WarGui;
import com.political.stats.PlayerStatsManager;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

/** Skyblock-style stat sheet, with an explanation of where each number comes from. */
public final class PlayerStatsGui {

    private PlayerStatsGui() {}

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x5, player, false);
        gui.setTitle(Component.literal("Your Stats").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        WarGui.fill(gui, 45);

        PlayerStatsManager.StatSheet sheet = PlayerStatsManager.getStats(player);
        String uuid = player.getUUID().toString();

        gui.setSlot(4, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal(player.getName().getString())
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Stats come from the lore on your gear."))
                .glow().build());

        gui.setSlot(10, statSlot(Items.RED_DYE, PlayerStatsManager.Stat.HEALTH,
                Math.round(player.getMaxHealth()) + "",
                "§8Base 20 + gear + bonuses"));
        gui.setSlot(11, statSlot(Items.IRON_CHESTPLATE, PlayerStatsManager.Stat.DEFENSE,
                fmt(sheet.get(PlayerStatsManager.Stat.DEFENSE)),
                "§8Damage taken × 100/(100+DEF)"));
        gui.setSlot(12, statSlot(Items.IRON_SWORD, PlayerStatsManager.Stat.STRENGTH,
                fmt(sheet.get(PlayerStatsManager.Stat.STRENGTH)),
                "§8Multiplies your outgoing damage"));
        gui.setSlot(13, statSlot(Items.DIAMOND, PlayerStatsManager.Stat.CRIT_CHANCE,
                fmt(sheet.get(PlayerStatsManager.Stat.CRIT_CHANCE)) + "%",
                "§8Chance to land a critical hit"));
        gui.setSlot(14, statSlot(Items.NETHERITE_SWORD, PlayerStatsManager.Stat.CRIT_DAMAGE,
                fmt(sheet.get(PlayerStatsManager.Stat.CRIT_DAMAGE)) + "%",
                "§8Extra damage when you crit"));
        gui.setSlot(15, statSlot(Items.SUGAR, PlayerStatsManager.Stat.SPEED,
                fmt(sheet.get(PlayerStatsManager.Stat.SPEED)),
                "§8100 is normal walking speed"));
        gui.setSlot(16, statSlot(Items.ENCHANTED_BOOK, PlayerStatsManager.Stat.INTELLIGENCE,
                fmt(sheet.get(PlayerStatsManager.Stat.INTELLIGENCE)),
                "§8Above 100 adds max mana"));
        gui.setSlot(19, statSlot(Items.BLAZE_POWDER, PlayerStatsManager.Stat.FEROCITY,
                fmt(sheet.get(PlayerStatsManager.Stat.FEROCITY)),
                "§8Each 100 = one guaranteed extra strike"));

        gui.setSlot(22, new GuiElementBuilder(Items.LAPIS_LAZULI)
                .setName(Component.literal("Mana").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§b" + (int) MagicManager.getMana(uuid)
                        + "§7/§b" + MagicManager.getMaxMana(uuid)))
                .addLoreLine(Component.literal("§8Raised by Intelligence and school nodes"))
                .build());

        gui.setSlot(31, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("How stats work").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Every custom item's lore carries stat lines"))
                .addLoreLine(Component.literal("§7like §f❁ Strength: +50§7. Wearing or holding"))
                .addLoreLine(Component.literal("§7the item adds those numbers here."))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§8Armour contributes every stat."))
                .addLoreLine(Component.literal("§8Held items contribute offensive stats only."))
                .build());

        gui.setSlot(40, new GuiElementBuilder(Items.COMPARATOR)
                .setName(Component.literal("Toggle Action Bar").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Show or hide the HP / Defense / Mana bar."))
                .setCallback((a, b, c, d) -> {
                    PlayerStatsManager.toggleActionBar(player);
                    open(player);
                })
                .build());

        gui.setSlot(36, WarGui.back(() -> MenuGui.openMainMenu(player)));
        gui.open();
    }

    private static eu.pb4.sgui.api.elements.GuiElement statSlot(net.minecraft.world.item.Item icon,
                                                                PlayerStatsManager.Stat stat,
                                                                String value, String note) {
        return new GuiElementBuilder(icon)
                .setName(Component.literal(stat.symbol + " " + stat.displayName)
                        .withStyle(stat.color, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§f" + value))
                .addLoreLine(Component.literal(note))
                .build();
    }

    private static String fmt(double v) {
        return v == Math.floor(v) ? String.valueOf((long) v)
                : String.format(java.util.Locale.ROOT, "%.1f", v);
    }
}
