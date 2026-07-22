package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.List;

public class JudgeGui {

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x5, player, false);
        gui.setTitle(Component.literal("⚖ Judge Dashboard"));

        // Dark elegant background
        for (int i = 0; i < 45; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Component.literal("")).build());
        }

        // Decorative border with purple
        int[] purpleBorder = {0, 1, 2, 3, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44};
        for (int slot : purpleBorder) {
            gui.setSlot(slot, new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE)
                    .setName(Component.literal("")).build());
        }

        // Header with gavel
        gui.setSlot(4, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Component.literal("⚖ Judge Dashboard").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Welcome, Judge!").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("Enforce justice and order").withStyle(ChatFormatting.GRAY))
                .glow()
                .build());

        // ── Row 1: Justice Actions (with gaps) ──
        gui.setSlot(10, new GuiElementBuilder(Items.IRON_BARS)
                .setName(Component.literal("⛓ View Prisoners").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("View imprisoned players").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Click to manage").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> openPrisonerList(player))
                .build());

        // Gap at 11
        gui.setSlot(12, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal("👥 Exile Player").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Exile a player from spawn").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Use /exile <player>").withStyle(ChatFormatting.YELLOW))
                .build());

        // Gap at 13
        gui.setSlot(14, new GuiElementBuilder(Items.TRIDENT)
                .setName(Component.literal("⚡ Smite Player").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Strike a player with lightning").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Use /smite <player>").withStyle(ChatFormatting.YELLOW))
                .build());

        // ── Row 2: Government Info (with gaps) ──
        String chair = DataManager.getChair();
        String chairName = chair != null ? DataManager.getPlayerName(chair) : "None";
        gui.setSlot(19, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Component.literal("👑 Current Chair").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(chairName).withStyle(ChatFormatting.YELLOW))
                .build());

        // Gap at 20
        String viceChair = DataManager.getViceChair();
        String viceChairName = viceChair != null ? DataManager.getPlayerName(viceChair) : "None";
        gui.setSlot(21, new GuiElementBuilder(Items.IRON_HELMET)
                .setName(Component.literal("🎖 Vice Chair").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(viceChairName).withStyle(ChatFormatting.YELLOW))
                .build());

        // Gap at 22
        gui.setSlot(23, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("⚖ Impeachment Status").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(ElectionManager.isImpeachmentActive() ? "§cVote in progress!" : "§aNo active vote"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Use /impeach to start").withStyle(ChatFormatting.GRAY))
                .build());

        // ── Row 3: Perks (with gaps) ──
        List<String> activePerks = PerkManager.getActivePerks();
        gui.setSlot(28, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("✨ Active Perks").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(activePerks.size() + " perks active").withStyle(ChatFormatting.GREEN))
                .glow().build());

        // Show active perks
        int perkSlot = 29;
        for (String perkId : activePerks) {
            if (perkSlot >= 35) break;
            Perk perk = PerkManager.getPerk(perkId);
            if (perk != null) {
                gui.setSlot(perkSlot, new GuiElementBuilder(Items.ENCHANTED_BOOK)
                        .setName(Component.literal(perk.name).withStyle(ChatFormatting.GREEN))
                        .addLoreLine(Component.literal(perk.description).withStyle(ChatFormatting.GRAY))
                        .build());
                perkSlot += 2; // Gap between perks
            }
        }

        // Perks selection button
        boolean canChangePerks = PerkManager.canChangePerks(false);
        if (canChangePerks) {
            gui.setSlot(40, new GuiElementBuilder(Items.NETHER_STAR)
                    .setName(Component.literal("🌟 Select Perks").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Choose perks for this term").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("You have 2 perk points").withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to select").withStyle(ChatFormatting.GREEN))
                    .setCallback((index, type, containerInput, slotGui) -> PerksGui.open(player, false))
                    .glow().build());
        } else {
            gui.setSlot(40, new GuiElementBuilder(Items.BARRIER)
                    .setName(Component.literal("🔒 Perks Locked").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Perks already selected").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("for this term!").withStyle(ChatFormatting.GRAY))
                    .build());
        }

        // Close button
        gui.setSlot(44, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("✖ Close").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .setCallback((slot, clicktype, containerInput, slotGui) -> gui.close())
                .build());

        gui.open();
    }

    private static void openPrisonerList(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("⛓ Prisoners"));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.IRON_BARS)
                .setName(Component.literal("⛓ Imprisoned Players").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click a prisoner to release").withStyle(ChatFormatting.YELLOW))
                .glow().build());

        // Get prisoners from PrisonManager via DataManager
        java.util.Map<String, Long> prisoners = DataManager.getPrisoners();
        int slot = 10;
        
        if (prisoners.isEmpty()) {
            gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                    .setName(Component.literal("No Prisoners").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("No one is currently imprisoned").withStyle(ChatFormatting.GRAY))
                    .build());
        } else {
            for (java.util.Map.Entry<String, Long> entry : prisoners.entrySet()) {
                if (slot >= 44) break;
                if (slot == 17) slot = 19;
                if (slot == 26) slot = 28;
                if (slot == 35) slot = 37;

                String uuid = entry.getKey();
                long releaseTime = entry.getValue();
                long remainingMinutes = Math.max(0, (releaseTime - System.currentTimeMillis()) / 60000);
                String name = DataManager.getPlayerName(uuid);

                final String prisonerUuid = uuid;
                gui.setSlot(slot, new GuiElementBuilder(Items.PLAYER_HEAD)
                        .setName(Component.literal(name).withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("Time remaining: " + remainingMinutes + " min").withStyle(ChatFormatting.YELLOW))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("Click to release").withStyle(ChatFormatting.GREEN))
                        .setCallback((index, type, containerInput, slotGui) -> {
                            ServerPlayer target = PoliticalServer.server.getPlayerList()
                                    .getPlayer(java.util.UUID.fromString(prisonerUuid));
                            if (target != null) {
                                PrisonManager.release(target);
                                player.sendSystemMessage(Component.literal("✓ Released " + target.getName().getString() + "!")
                                        .withStyle(ChatFormatting.GREEN));
                                openPrisonerList(player);
                            } else {
                                player.sendSystemMessage(Component.literal("Player is offline!")
                                        .withStyle(ChatFormatting.RED));
                            }
                        })
                        .build());
                slot++;
            }
        }

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> open(player))
                .build());

        gui.open();
    }
}
