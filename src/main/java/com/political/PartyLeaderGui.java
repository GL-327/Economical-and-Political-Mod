package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SignGui;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.*;
import java.util.stream.Collectors;

public final class PartyLeaderGui {

    public static void open(ServerPlayer player) {
        if (!PoliticalPartyManager.isPartyLeader(player)) {
            player.sendSystemMessage(Component.literal("You are not the leader of your party (highest support).").withStyle(ChatFormatting.RED));
            return;
        }
        String pid = PoliticalPartyManager.getPlayerPartyId(player.getUUID().toString());
        PoliticalPartyManager.Party p = pid != null ? PoliticalPartyManager.getParty(pid) : null;
        long budget = p != null ? p.budgetCoins : 0L;

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Party Leader").withStyle(ChatFormatting.GOLD));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE).setName(Component.literal("")).build());
        }

        // --- Header ---
        gui.setSlot(4, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Component.literal(p != null ? p.displayName : "Party").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Budget: " + String.format("%,d", budget)).withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal("Support (you): " + PoliticalPartyManager.getSupport(player.getUUID().toString())).withStyle(ChatFormatting.AQUA))
                .build());

        // ──── Row 2: Broadcast & Proposals ────

        gui.setSlot(10, new GuiElementBuilder(Items.OAK_SIGN)
                .setName(Component.literal("Nationwide Broadcast").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Cost: 2,500,000 from party pot").withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal("Short message to all players").withStyle(ChatFormatting.GRAY))
                .setCallback((index, type, containerInput, slotGui) -> openAdSign(player))
                .build());

        boolean ruling = PoliticalPartyManager.isLeaderOfRulingParty(player);
        if (ruling) {
            gui.setSlot(12, new GuiElementBuilder(Items.BEACON)
                    .setName(Component.literal("Propose: Infrastructure Surge").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Cost: 8,000,000 | Council vote 1h").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal("Effect: INFRASTRUCTURE_BONUS 24h").withStyle(ChatFormatting.GREEN))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        PoliticalPartyManager.proposeRulingPartyPerk(player, "INFRASTRUCTURE_BONUS", 8_000_000L, 24L * 60L * 60L * 1000L);
                        player.closeContainer();
                    })
                    .build());
            gui.setSlot(13, new GuiElementBuilder(Items.SCAFFOLDING)
                    .setName(Component.literal("Propose: Tax Relief").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Cost: 20,000,000 | Council vote 1h").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal("Effect: TAX_REDUCTION 48h").withStyle(ChatFormatting.GREEN))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        PoliticalPartyManager.proposeRulingPartyPerk(player, "TAX_REDUCTION", 20_000_000L, 48L * 60L * 60L * 1000L);
                        player.closeContainer();
                    })
                    .build());
            gui.setSlot(14, new GuiElementBuilder(Items.SUNFLOWER)
                    .setName(Component.literal("Propose: Economic Stimulus").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Cost: 75,000,000 | Council vote 1h").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal("Effect: PROSPERITY_SURGE 6h").withStyle(ChatFormatting.GREEN))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        PoliticalPartyManager.proposeRulingPartyPerk(player, "PROSPERITY_SURGE", 75_000_000L, 6L * 60L * 60L * 1000L);
                        player.closeContainer();
                    })
                    .build());
        } else {
            gui.setSlot(13, new GuiElementBuilder(Items.BOOK)
                    .setName(Component.literal("Ruling Party Programs").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("Only the ruling party leader can").withStyle(ChatFormatting.DARK_GRAY))
                    .addLoreLine(Component.literal("propose nation-wide perks.").withStyle(ChatFormatting.DARK_GRAY))
                    .build());
        }

        // ──── Row 3: Party Perks Shop ────

        gui.setSlot(18, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("\u2605 Buy Party Perks").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Green = owned | Red = available to buy").withStyle(ChatFormatting.DARK_GRAY))
                .build());

        Set<String> owned = pid != null ? PoliticalPartyManager.getPartyPerks(pid) : Set.of();
        int perkSlot = 19;
        for (PoliticalPartyManager.PartyPerkDef def : PoliticalPartyManager.PERK_DEFS) {
            if (perkSlot >= 27) break;
            boolean has = owned.contains(def.id);
            GuiElementBuilder perkItem = new GuiElementBuilder(has ? Items.LIME_DYE : Items.RED_DYE)
                    .setName(Component.literal(def.displayName).withStyle(has ? ChatFormatting.GREEN : ChatFormatting.WHITE, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(def.description).withStyle(ChatFormatting.GRAY));
            if (has) {
                perkItem.addLoreLine(Component.literal("\u2714 Owned").withStyle(ChatFormatting.GREEN));
            } else {
                perkItem.addLoreLine(Component.literal("Cost: " + String.format("%,d", def.cost) + " coins").withStyle(ChatFormatting.RED));
                perkItem.addLoreLine(Component.literal("Click to purchase").withStyle(ChatFormatting.YELLOW));
                String perkId = def.id;
                perkItem.setCallback((index, type, containerInput, slotGui) -> {
                    PoliticalPartyManager.tryBuyPartyPerk(player, perkId);
                    open(player);
                });
            }
            gui.setSlot(perkSlot, perkItem.build());
            perkSlot++;
        }

        // ──── Row 4: Diplomacy ────

        gui.setSlot(27, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Component.literal("\u2694 Declare War").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Cost: 10,000,000 from budget").withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal("Click to choose target party").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> openDeclareWarPicker(player, pid))
                .build());

        gui.setSlot(29, new GuiElementBuilder(Items.GOLDEN_APPLE)
                .setName(Component.literal("\u2764 Propose Alliance").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Free — target must accept").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Click to choose target party").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> openAlliancePicker(player, pid))
                .build());

        // ──── Row 5: Member Management ────

        gui.setSlot(36, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal("Promote/Demote Members").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Manage member ranks").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Click to open member list").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> PartyJoinGui.openMemberList(player, pid))
                .build());

        gui.setSlot(38, new GuiElementBuilder(Items.FIREWORK_ROCKET)
                .setName(Component.literal("Start Party Event").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Weekly challenge for members").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Reward: 5M to budget").withStyle(ChatFormatting.GREEN))
                .setCallback((index, type, containerInput, slotGui) -> {
                    gui.close();
                    PartyJoinGui.open(player);
                })
                .build());

        // --- Back / Close ---
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("Back to Party Hub").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> PartyJoinGui.open(player))
                .build());

        gui.setSlot(53, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Close").withStyle(ChatFormatting.RED))
                .setCallback((index, type, containerInput, slotGui) -> player.closeContainer())
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    //  DECLARE WAR PICKER
    // ════════════════════════════════════════════════════════════

    private static void openDeclareWarPicker(ServerPlayer player, String myPartyId) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("Declare War On...").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));

        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE).setName(Component.literal("")).build());
        }

        int slot = 10;
        for (PoliticalPartyManager.Party p : PoliticalPartyManager.getParties()) {
            if (slot >= 26) break;
            if (p.id.equals(myPartyId)) continue;
            if (slot == 17) slot = 19;

            boolean alreadyAtWar = PoliticalPartyManager.isAtWar(myPartyId, p.id);
            boolean isAlly = PoliticalPartyManager.isAllied(myPartyId, p.id);

            GuiElementBuilder item = new GuiElementBuilder(Items.RED_BANNER)
                    .setName(Component.literal(p.displayName).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Members: " + p.members.size()).withStyle(ChatFormatting.GRAY));

            if (alreadyAtWar) {
                item.addLoreLine(Component.literal("Already at war!").withStyle(ChatFormatting.DARK_RED));
            } else if (isAlly) {
                item.addLoreLine(Component.literal("Allied — break alliance first").withStyle(ChatFormatting.AQUA));
            } else {
                item.addLoreLine(Component.literal("Cost: 10,000,000 coins").withStyle(ChatFormatting.RED));
                item.addLoreLine(Component.literal("Click to declare war").withStyle(ChatFormatting.YELLOW));
                String targetId = p.id;
                item.setCallback((index, type, containerInput, slotGui) -> {
                    PoliticalPartyManager.declareWar(player, targetId);
                    open(player);
                });
            }
            gui.setSlot(slot, item.build());
            slot++;
        }

        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> open(player))
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    //  PROPOSE ALLIANCE PICKER
    // ════════════════════════════════════════════════════════════

    private static void openAlliancePicker(ServerPlayer player, String myPartyId) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("Propose Alliance To...").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));

        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.LIGHT_BLUE_STAINED_GLASS_PANE).setName(Component.literal("")).build());
        }

        int slot = 10;
        for (PoliticalPartyManager.Party p : PoliticalPartyManager.getParties()) {
            if (slot >= 26) break;
            if (p.id.equals(myPartyId)) continue;
            if (slot == 17) slot = 19;

            boolean isAlly = PoliticalPartyManager.isAllied(myPartyId, p.id);
            boolean atWar = PoliticalPartyManager.isAtWar(myPartyId, p.id);

            GuiElementBuilder item = new GuiElementBuilder(Items.BLUE_BANNER)
                    .setName(Component.literal(p.displayName).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Members: " + p.members.size()).withStyle(ChatFormatting.GRAY));

            if (isAlly) {
                item.addLoreLine(Component.literal("Already allied!").withStyle(ChatFormatting.GREEN));
            } else if (atWar) {
                item.addLoreLine(Component.literal("At war — end war first").withStyle(ChatFormatting.RED));
            } else {
                item.addLoreLine(Component.literal("Free — they must accept").withStyle(ChatFormatting.GRAY));
                item.addLoreLine(Component.literal("Click to propose").withStyle(ChatFormatting.YELLOW));
                String targetId = p.id;
                item.setCallback((index, type, containerInput, slotGui) -> {
                    PoliticalPartyManager.proposeAlliance(player, targetId);
                    open(player);
                });
            }
            gui.setSlot(slot, item.build());
            slot++;
        }

        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> open(player))
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    //  AD SIGN
    // ════════════════════════════════════════════════════════════

    private static void openAdSign(ServerPlayer player) {
        SignGui signGui = new SignGui(player) {
            @Override
            public void onManualClose() {
                String msg = this.getLine(0).getString().trim();
                if (msg.length() > 120) {
                    msg = msg.substring(0, 120);
                }
                if (!msg.isEmpty()) {
                    PoliticalPartyManager.broadcastPartyAd(player, msg);
                }
                PartyLeaderGui.open(player);
            }
        };
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal("^^^^^^^^^^^^^^").withStyle(ChatFormatting.YELLOW));
        signGui.setLine(2, Component.literal("Party broadcast").withStyle(ChatFormatting.AQUA));
        signGui.setLine(3, Component.literal("(max 120 chars)").withStyle(ChatFormatting.GRAY));
        signGui.open();
    }
}
