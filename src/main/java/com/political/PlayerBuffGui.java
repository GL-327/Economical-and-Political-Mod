package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * GUI for viewing all player buffs — two pages of earnable buffs + admin buffs on page 2 tail.
 */
public class PlayerBuffGui {

    private static List<PlayerBuffManager.PlayerBuff> listNonAdminBuffs() {
        List<PlayerBuffManager.PlayerBuff> out = new ArrayList<>();
        for (PlayerBuffManager.PlayerBuff b : PlayerBuffManager.PlayerBuff.values()) {
            if (!b.adminOnly) {
                out.add(b);
            }
        }
        return out;
    }

    private static List<PlayerBuffManager.PlayerBuff> adminBuffs() {
        List<PlayerBuffManager.PlayerBuff> out = new ArrayList<>();
        for (PlayerBuffManager.PlayerBuff b : PlayerBuffManager.PlayerBuff.values()) {
            if (b.adminOnly) {
                out.add(b);
            }
        }
        return out;
    }

    public static void openMainMenu(ServerPlayer player) {
        openPage(player, 0);
    }

    public static void openPage(ServerPlayer player, int page) {
        List<PlayerBuffManager.PlayerBuff> earnable = listNonAdminBuffs();
        int n = earnable.size();
        int perPage = 24;
        int pageCount = Math.max(1, (n + perPage - 1) / perPage);

        List<PlayerBuffManager.PlayerBuff> pageBuffs = new ArrayList<>();
        int start = page * perPage;
        for (int i = 0; i < perPage && start + i < n; i++) {
            pageBuffs.add(earnable.get(start + i));
        }
        // Admin buffs only on last page slot area if we're on last earnable page
        boolean lastEarnablePage = page >= pageCount - 1;
        if (lastEarnablePage) {
            int room = perPage - pageBuffs.size();
            List<PlayerBuffManager.PlayerBuff> admins = adminBuffs();
            for (int i = 0; i < room && i < admins.size(); i++) {
                pageBuffs.add(admins.get(i));
            }
        }

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("✦ Player Buffs — Page " + (page + 1) + "/" + pageCount));

        GuiElementBuilder bg = new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray())
                .setName(Component.literal(""));
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, bg.build());
        }

        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("✦ Player Buffs Guide").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Page " + (page + 1) + " of " + pageCount).withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("Buffs are permanent once earned!").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Your active buffs: " + PlayerBuffManager.getBuffObjects(player.getUUID().toString()).size()).withStyle(ChatFormatting.AQUA))
                .glow().build());

        int slot = 10;
        for (PlayerBuffManager.PlayerBuff buff : pageBuffs) {
            if (slot > 43) break;
            if (slot == 18 || slot == 27 || slot == 36) slot++;

            boolean hasBuff = PlayerBuffManager.hasBuff(player.getUUID().toString(), buff);

            GuiElementBuilder builder = new GuiElementBuilder(buff.icon)
                    .setName(Component.literal(buff.colorCode + "◆ " + buff.displayName)
                            .withStyle(hasBuff ? ChatFormatting.BOLD : ChatFormatting.RESET))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Effect: ").withStyle(ChatFormatting.AQUA)
                            .append(Component.literal(buff.effect).withStyle(ChatFormatting.WHITE)))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("How to obtain:").withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal("  " + buff.obtainMethod).withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("  Progress: " + BuffProgressManager.getProgressString(player.getUUID().toString(), buff)).withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""));

            if (hasBuff) {
                builder.addLoreLine(Component.literal("● UNLOCKED").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                        .addLoreLine(Component.literal("  Permanent buff active!").withStyle(ChatFormatting.YELLOW))
                        .glow();
            } else {
                builder.addLoreLine(Component.literal("○ Locked").withStyle(ChatFormatting.DARK_GRAY));
            }

            gui.setSlot(slot++, builder.build());
        }

        if (page > 0) {
            gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("← Previous page").withStyle(ChatFormatting.YELLOW))
                    .setCallback((idx, type, containerInput, slotGui) -> openPage(player, page - 1))
                    .build());
        }

        if (page < pageCount - 1) {
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Next page →").withStyle(ChatFormatting.YELLOW))
                    .setCallback((idx, type, containerInput, slotGui) -> openPage(player, page + 1))
                    .build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal("Your Active Buffs").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .setCallback((idx, type, containerInput, slotGui) -> openYourBuffsMenu(player))
                .build());

        gui.open();
    }

    public static void openYourBuffsMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("✦ Your Buffs"));

        GuiElementBuilder bg = new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray())
                .setName(Component.literal(""));
        for (int i = 0; i < 27; i++) gui.setSlot(i, bg.build());

        Set<PlayerBuffManager.PlayerBuff> buffs = PlayerBuffManager.getBuffObjects(player.getUUID().toString());

        if (buffs.isEmpty()) {
            gui.setSlot(13, new GuiElementBuilder(Items.BARRIER)
                    .setName(Component.literal("No Buffs Yet").withStyle(ChatFormatting.RED))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Complete achievements to earn buffs!").withStyle(ChatFormatting.GRAY))
                    .build());
        } else {
            int slot = 10;
            for (PlayerBuffManager.PlayerBuff buff : buffs) {
                if (slot > 16) break;

                GuiElementBuilder builder = new GuiElementBuilder(buff.icon)
                        .setName(Component.literal(buff.colorCode + "◆ " + buff.displayName))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("Effect: ").withStyle(ChatFormatting.AQUA)
                                .append(Component.literal(buff.effect).withStyle(ChatFormatting.WHITE)))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("● ACTIVE").withStyle(ChatFormatting.GREEN))
                        .addLoreLine(Component.literal("  Permanent").withStyle(ChatFormatting.YELLOW))
                        .glow();

                gui.setSlot(slot++, builder.build());
            }
        }

        gui.setSlot(18, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openMainMenu(player))
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.BOOK)
                .setName(Component.literal("Buff Info").withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Buffs are permanent once earned!").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Complete achievements to unlock them.").withStyle(ChatFormatting.GRAY))
                .build());

        gui.open();
    }
}
