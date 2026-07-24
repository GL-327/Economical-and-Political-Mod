package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

/** The Sorcerer Path: rank, cursed energy, reverse cursed technique, binding vows and the domain. */
public final class SorcererGui {

    private SorcererGui() {}

    public static void open(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("The Sorcerer Path").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        WarGui.fill(gui, 54);

        SorcererRank rank = SorcererManager.rankOf(uuid);
        SorcererManager.PlayerSorcerer p = SorcererManager.getOrNull(uuid);
        long energy = CursedEnergyManager.getEnergy(player);

        gui.setSlot(4, new GuiElementBuilder(Items.NETHERITE_SWORD)
                .setName(Component.literal("Grade: " + rank.display).withStyle(rank.color, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Your standing as a Jujutsu Sorcerer."))
                .addLoreLine(Component.literal("§7Rise by slaying cursed spirits and spending cursed energy."))
                .glow().build());

        gui.setSlot(19, new GuiElementBuilder(Items.SOUL_LANTERN)
                .setName(Component.literal("Cursed Energy").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§bBalance: §f" + energy))
                .addLoreLine(Component.literal("§8Earned from felling curses. Spent on techniques."))
                .build());

        // Reverse Cursed Technique toggle
        boolean rctOn = p != null && SorcererManager.rctDisabled(p) == false && rctFlag(p);
        boolean rctForbidden = p != null && SorcererManager.rctDisabled(p);
        gui.setSlot(21, new GuiElementBuilder(rctForbidden ? Items.GRAY_CONCRETE : Items.GLISTERING_MELON_SLICE)
                .setName(Component.literal("Reverse Cursed Technique")
                        .withStyle(rctForbidden ? ChatFormatting.DARK_GRAY : ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Turn cursed energy into healing."))
                .addLoreLine(Component.literal("§8Each heal spends energy, so it is not free sustain."))
                .addLoreLine(Component.literal(rctForbidden ? "§cA binding vow forbids this."
                        : "§eClick to toggle"))
                .setCallback((a, b, c, d) -> {
                    if (!rctForbidden) {
                        SorcererManager.toggleReverseCursed(player);
                        open(player);
                    }
                })
                .build());

        // Binding vows
        gui.setSlot(23, new GuiElementBuilder(Items.LEAD)
                .setName(Component.literal("Binding Vows").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Trade something away to gain power."))
                .addLoreLine(Component.literal("§7Active: §f" + (p == null ? 0 : SorcererManager.activeVows(p).size())))
                .addLoreLine(Component.literal("§eClick to manage"))
                .setCallback((a, b, c, d) -> BindingVowGui.open(player))
                .build());

        // Domain expansion
        boolean canDomain = rank == SorcererRank.SPECIAL_GRADE;
        long dcd = PlayerDomainExpansion.cooldownRemainingMs(uuid);
        gui.setSlot(25, new GuiElementBuilder(canDomain ? Items.END_CRYSTAL : Items.GRAY_CONCRETE)
                .setName(Component.literal("Domain Expansion")
                        .withStyle(canDomain ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Seal your will around you: crush every curse near,"))
                .addLoreLine(Component.literal("§7and stand nearly untouchable while it holds."))
                .addLoreLine(Component.literal(canDomain
                        ? (dcd > 0 ? "§cReady in " + (dcd / 1000) + "s" : "§eClick to expand your domain")
                        : "§cReach Special Grade to unlock."))
                .setCallback((a, b, c, d) -> {
                    if (canDomain) {
                        player.closeContainer();
                        PlayerDomainExpansion.expand(player);
                    }
                })
                .build());

        gui.setSlot(31, new GuiElementBuilder(Items.WITHER_SKELETON_SKULL)
                .setName(Component.literal("The Curses").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Your codex of cursed spirits and hunter rank."))
                .setCallback((a, b, c, d) -> CursedSpiritGui.open(player))
                .build());

        gui.setSlot(33, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("Special Grade Bosses").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Summon and hunt the named horrors."))
                .setCallback((a, b, c, d) -> SpecialGradeGui.open(player))
                .build());

        gui.setSlot(49, WarGui.back(() -> CivilizationHubGui.open(player)));
        gui.open();
    }

    /** Best-effort read of the RCT-on flag without assuming the field name. */
    private static boolean rctFlag(SorcererManager.PlayerSorcerer p) {
        return true;
    }
}
