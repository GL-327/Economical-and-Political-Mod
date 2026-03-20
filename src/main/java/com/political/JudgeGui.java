package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class JudgeGui {

    public static void open(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X5, player, false);
        gui.setTitle(Text.literal("⚖ Judge Dashboard"));

        // Dark elegant background
        for (int i = 0; i < 45; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Decorative border with purple
        int[] purpleBorder = {0, 1, 2, 3, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44};
        for (int slot : purpleBorder) {
            gui.setSlot(slot, new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header with gavel
        gui.setSlot(4, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Text.literal("⚖ Judge Dashboard").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Welcome, Judge!").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("Enforce justice and order").formatted(Formatting.GRAY))
                .glow()
                .build());

        // ── Row 1: Justice Actions (with gaps) ──
        gui.setSlot(10, new GuiElementBuilder(Items.IRON_BARS)
                .setName(Text.literal("⛓ View Prisoners").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("View imprisoned players").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click to manage").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> openPrisonerList(player))
                .build());

        // Gap at 11
        gui.setSlot(12, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Text.literal("👥 Exile Player").formatted(Formatting.DARK_RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Exile a player from spawn").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Use /exile <player>").formatted(Formatting.YELLOW))
                .build());

        // Gap at 13
        gui.setSlot(14, new GuiElementBuilder(Items.TRIDENT)
                .setName(Text.literal("⚡ Smite Player").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Strike a player with lightning").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Use /smite <player>").formatted(Formatting.YELLOW))
                .build());

        // ── Row 2: Government Info (with gaps) ──
        String chair = DataManager.getChair();
        String chairName = chair != null ? DataManager.getPlayerName(chair) : "None";
        gui.setSlot(19, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Text.literal("👑 Current Chair").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal(chairName).formatted(Formatting.YELLOW))
                .build());

        // Gap at 20
        String viceChair = DataManager.getViceChair();
        String viceChairName = viceChair != null ? DataManager.getPlayerName(viceChair) : "None";
        gui.setSlot(21, new GuiElementBuilder(Items.IRON_HELMET)
                .setName(Text.literal("🎖 Vice Chair").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal(viceChairName).formatted(Formatting.YELLOW))
                .build());

        // Gap at 22
        gui.setSlot(23, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("⚖ Impeachment Status").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal(ElectionManager.isImpeachmentActive() ? "§cVote in progress!" : "§aNo active vote"))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Use /impeach to start").formatted(Formatting.GRAY))
                .build());

        // ── Row 3: Perks (with gaps) ──
        List<String> activePerks = PerkManager.getActivePerks();
        gui.setSlot(28, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("✨ Active Perks").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal(activePerks.size() + " perks active").formatted(Formatting.GREEN))
                .glow().build());

        // Show active perks
        int perkSlot = 29;
        for (String perkId : activePerks) {
            if (perkSlot >= 35) break;
            Perk perk = PerkManager.getPerk(perkId);
            if (perk != null) {
                gui.setSlot(perkSlot, new GuiElementBuilder(Items.ENCHANTED_BOOK)
                        .setName(Text.literal(perk.name).formatted(Formatting.GREEN))
                        .addLoreLine(Text.literal(perk.description).formatted(Formatting.GRAY))
                        .build());
                perkSlot += 2; // Gap between perks
            }
        }

        // Perks selection button
        boolean canChangePerks = PerkManager.canChangePerks(false);
        if (canChangePerks) {
            gui.setSlot(40, new GuiElementBuilder(Items.NETHER_STAR)
                    .setName(Text.literal("🌟 Select Perks").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Choose perks for this term").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("You have 2 perk points").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Click to select").formatted(Formatting.GREEN))
                    .setCallback((index, type, action) -> PerksGui.open(player, false))
                    .glow().build());
        } else {
            gui.setSlot(40, new GuiElementBuilder(Items.BARRIER)
                    .setName(Text.literal("🔒 Perks Locked").formatted(Formatting.RED, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Perks already selected").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("for this term!").formatted(Formatting.GRAY))
                    .build());
        }

        // Close button
        gui.setSlot(44, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("✖ Close").formatted(Formatting.RED, Formatting.BOLD))
                .setCallback((slot, clickType, action) -> gui.close())
                .build());

        gui.open();
    }

    private static void openPrisonerList(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⛓ Prisoners"));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.IRON_BARS)
                .setName(Text.literal("⛓ Imprisoned Players").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click a prisoner to release").formatted(Formatting.YELLOW))
                .glow().build());

        // Get prisoners from PrisonManager via DataManager
        java.util.Map<String, Long> prisoners = DataManager.getPrisoners();
        int slot = 10;
        
        if (prisoners.isEmpty()) {
            gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                    .setName(Text.literal("No Prisoners").formatted(Formatting.GREEN))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("No one is currently imprisoned").formatted(Formatting.GRAY))
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
                        .setName(Text.literal(name).formatted(Formatting.RED, Formatting.BOLD))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("Time remaining: " + remainingMinutes + " min").formatted(Formatting.YELLOW))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("Click to release").formatted(Formatting.GREEN))
                        .setCallback((index, type, action) -> {
                            ServerPlayerEntity target = PoliticalServer.server.getPlayerManager()
                                    .getPlayer(java.util.UUID.fromString(prisonerUuid));
                            if (target != null) {
                                PrisonManager.release(target);
                                player.sendMessage(Text.literal("✓ Released " + target.getName().getString() + "!")
                                        .formatted(Formatting.GREEN));
                                openPrisonerList(player);
                            } else {
                                player.sendMessage(Text.literal("Player is offline!")
                                        .formatted(Formatting.RED));
                            }
                        })
                        .build());
                slot++;
            }
        }

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> open(player))
                .build());

        gui.open();
    }
}
