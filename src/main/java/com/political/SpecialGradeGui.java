package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

/** Summon and hunt the named Special Grade bosses. */
public final class SpecialGradeGui {

    private SpecialGradeGui() {}

    private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};

    public static void open(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Special Grade").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        WarGui.fill(gui, 54);

        long cd = SpecialGradeBossManager.cooldownRemaining(uuid);
        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("Special Grade Curses").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Named horrors, each a fight of its own."))
                .addLoreLine(Component.literal(cd > 0 ? "§cNext summon in " + (cd / 1000) + "s"
                        : "§aReady to summon"))
                .glow().build());

        SpecialGradeBoss[] bosses = SpecialGradeBoss.values();
        for (int i = 0; i < bosses.length && i < SLOTS.length; i++) {
            SpecialGradeBoss boss = bosses[i];
            String reason = SpecialGradeBossManager.blockReason(player, boss);
            boolean canSummon = reason == null || reason.isEmpty();
            int kills = SpecialGradeBossManager.killsOf(uuid, boss);
            boolean trophy = SpecialGradeBossManager.hasTrophy(uuid, boss);

            GuiElementBuilder b = new GuiElementBuilder(canSummon ? Items.WITHER_SKELETON_SKULL : Items.GRAY_CONCRETE)
                    .setName(Component.literal(boss.icon + " " + boss.title).withStyle(boss.color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Slain: §f" + kills + (trophy ? "  §6✦ trophy earned" : "")))
                    .addLoreLine(Component.literal(""));
            if (canSummon) {
                b.addLoreLine(Component.literal("§eClick to begin the ritual"));
                b.setCallback((a, c, d, g) -> {
                    player.closeContainer();
                    SpecialGradeBossManager.summon(player, boss);
                });
            } else {
                b.addLoreLine(Component.literal("§c" + reason));
            }
            gui.setSlot(SLOTS[i], b.build());
        }

        gui.setSlot(49, WarGui.back(() -> CivilizationHubGui.open(player)));
        gui.open();
    }
}
