package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class PerksGui {

    private static final List<String> selectedPerks = new ArrayList<>();
    private static int currentPoints = 0;
    private static int currentPage = 0;

    public static void open(ServerPlayerEntity player, boolean isChair) {
        if (!PerkManager.canChangePerks(isChair)) {
            player.sendMessage(Text.literal("Perks have already been selected for this term!").formatted(Formatting.RED));
            return;
        }

        // ADD THESE 5 LINES:
        if (!isChair && !PerkManager.isChairPerksSetThisTerm()) {
            player.sendMessage(Text.literal("The Chair must select their perks first!").formatted(Formatting.RED));
            return;
        }

        selectedPerks.clear();
        // ...
        currentPoints = 0;
        currentPage = 0;
        openPage(player, isChair, 0);
    }

    private static void openPage(ServerPlayerEntity player, boolean isChair, int page) {
        currentPage = page;
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);

        // Different titles for Chair vs Vice Chair
        String title;
        if (isChair) {
            if (page == 0) {
                title = "Chair Perks - Positive (1/3)";
            } else if (page == 1) {
                title = "Chair Perks - Neutral (2/3)";
            } else {
                title = "Chair Perks - Negative (3/3)";
            }
        } else {
            if (page == 0) {
                title = "Vice Chair Perks - Positive (1/3)";
            } else if (page == 1) {
                title = "Vice Chair Perks - Neutral (2/3)";
            } else {
                title = "Vice Chair Perks - Negative (3/3)";
            }
        }
        gui.setTitle(Text.literal(title));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        List<Perk> perksToShow;
        if (page == 0) {
            perksToShow = PerkManager.getPerksByType(Perk.PerkType.POSITIVE);
        } else if (page == 1) {
            perksToShow = PerkManager.getPerksByType(Perk.PerkType.NEUTRAL);
        } else {
            perksToShow = PerkManager.getPerksByType(Perk.PerkType.NEGATIVE);
        }

        // CHANGED: Chair gets 6 perks, Vice Chair gets 2
        int maxPerks = isChair ? 6 : 2;

// ADDED: Get Chair's selected perks (for Vice Chair to see as blocked)
        List<String> chairPerks = PerkManager.getChairSelectedPerks();

// ADDED: Get Vice Chair's perk (for Chair to see as blocked)
        String vcPerk = PerkManager.getViceChairPerk();

        int slot = 10;
        for (Perk perk : perksToShow) {
            if (slot == 17) slot = 19;
            if (slot == 26) slot = 28;
            if (slot == 35) slot = 37;
            if (slot >= 44) break;

            boolean isSelected = selectedPerks.contains(perk.id);
// FIXED: Remove && isChair so Vice Chair also respects cooldowns
            boolean isOnCooldown = PerkManager.isPerkOnCooldown(perk.id);
            // ADDED: Vice Chair can't pick perks the Chair already selected
            boolean isChairPerk = !isChair && chairPerks.contains(perk.id);
            boolean canSelect = selectedPerks.size() < maxPerks || isSelected;

            GuiElementBuilder builder;

            // ADDED: Show Chair's perks as blocked for Vice Chair
            if (isChairPerk) {
                builder = new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE)
                        .setName(Text.literal(perk.name).formatted(Formatting.LIGHT_PURPLE, Formatting.STRIKETHROUGH))
                        .addLoreLine(Text.literal(perk.description).formatted(Formatting.GRAY))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("SELECTED BY CHAIR").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                        .addLoreLine(Text.literal("You cannot select this perk").formatted(Formatting.DARK_PURPLE));
            } else if (isOnCooldown) {
                builder = new GuiElementBuilder(Items.BARRIER)
                        .setName(Text.literal(perk.name).formatted(Formatting.RED, Formatting.STRIKETHROUGH))
                        .addLoreLine(Text.literal(perk.description).formatted(Formatting.GRAY))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("ON COOLDOWN").formatted(Formatting.RED, Formatting.BOLD))
                        .addLoreLine(Text.literal("Was used by previous Chair").formatted(Formatting.DARK_RED));
            } else if (isSelected) {
                builder = new GuiElementBuilder(Items.LIME_CONCRETE)
                        .setName(Text.literal(perk.name).formatted(Formatting.GREEN, Formatting.BOLD))
                        .addLoreLine(Text.literal(perk.description).formatted(Formatting.GRAY))
                        .addLoreLine(Text.literal(""));

                // Only show points for Chair
                if (isChair) {
                    builder.addLoreLine(getPointsLore(perk.pointValue))
                            .addLoreLine(Text.literal(""));
                }

                builder.addLoreLine(Text.literal("✓ SELECTED - Click to remove").formatted(Formatting.YELLOW))
                        .glow()
                        .setCallback((index, type, action) -> {
                            selectedPerks.remove(perk.id);
                            currentPoints -= perk.pointValue;
                            openPage(player, isChair, currentPage);
                        });
            } else if (!canSelect) {
                builder = new GuiElementBuilder(Items.RED_CONCRETE)
                        .setName(Text.literal(perk.name).formatted(Formatting.RED))
                        .addLoreLine(Text.literal(perk.description).formatted(Formatting.GRAY))
                        .addLoreLine(Text.literal(""));

                if (isChair) {
                    builder.addLoreLine(getPointsLore(perk.pointValue))
                            .addLoreLine(Text.literal(""));
                }

                builder.addLoreLine(Text.literal("Maximum perks selected!").formatted(Formatting.RED));
            } else {
                builder = new GuiElementBuilder(Items.WHITE_CONCRETE)
                        .setName(Text.literal(perk.name).formatted(Formatting.WHITE))
                        .addLoreLine(Text.literal(perk.description).formatted(Formatting.GRAY))
                        .addLoreLine(Text.literal(""));

                if (isChair) {
                    builder.addLoreLine(getPointsLore(perk.pointValue))
                            .addLoreLine(Text.literal(""));
                }

                builder.addLoreLine(Text.literal("Click to select").formatted(Formatting.YELLOW))
                        .setCallback((index, type, action) -> {
                            selectedPerks.add(perk.id);
                            currentPoints += perk.pointValue;
                            openPage(player, isChair, currentPage);
                        });
            }

            gui.setSlot(slot, builder.build());
            slot++;
        }

        // Navigation arrows
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Previous Page").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> {
                    int newPage = (currentPage - 1 + 3) % 3;
                    openPage(player, isChair, newPage);
                })
                .build());

        gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("Next Page →").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> {
                    int newPage = (currentPage + 1) % 3;
                    openPage(player, isChair, newPage);
                })
                .build());

        // CHANGED: Different info display for Chair vs Vice Chair
        if (isChair) {
            Formatting pointColor;
            if (currentPoints > 0) {
                pointColor = Formatting.RED;
            } else if (currentPoints < 0) {
                pointColor = Formatting.BLUE;
            } else {
                pointColor = Formatting.GREEN;
            }

            gui.setSlot(48, new GuiElementBuilder(Items.BOOK)
                    .setName(Text.literal("Point Balance").formatted(Formatting.GOLD))
                    .addLoreLine(Text.literal("Current: " + currentPoints).formatted(pointColor))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Must equal 0 to confirm!").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("Selected: " + selectedPerks.size() + "/" + maxPerks).formatted(Formatting.WHITE))
                    .build());
        } else {
// Vice Chair info
            gui.setSlot(48, new GuiElementBuilder(Items.BOOK)
                    .setName(Text.literal("Vice Chair Perks").formatted(Formatting.AQUA))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Select up to 2 perks").formatted(Formatting.WHITE))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✓ No point balance required!").formatted(Formatting.GREEN))
                    // REMOVED: "No cooldown restrictions!" line
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Selected: " + selectedPerks.size() + "/2").formatted(Formatting.WHITE))
                    .build());
        }

        // CHANGED: Different confirm logic for Chair vs Vice Chair
        boolean canConfirm;
        if (isChair) {
            canConfirm = currentPoints == 0 && selectedPerks.size() <= 6 && !selectedPerks.isEmpty();
        } else {
            // Vice Chair: just needs at least 1 perk, max 2, no point balance
            canConfirm = !selectedPerks.isEmpty() && selectedPerks.size() <= 2;
        }

        if (canConfirm) {
            gui.setSlot(50, new GuiElementBuilder(Items.LIME_CONCRETE)
                    .setName(Text.literal("✓ CONFIRM PERKS").formatted(Formatting.GREEN, Formatting.BOLD))
                    .addLoreLine(Text.literal("Click to activate perks!").formatted(Formatting.YELLOW))
                    .glow()
                    .setCallback((index, type, action) -> {
                        if (isChair) {
                            // Chair activates their perks
                            PerkManager.activatePerks(new ArrayList<>(selectedPerks), PerkManager.getViceChairPerk());
                            player.sendMessage(Text.literal("Chair perks activated!").formatted(Formatting.GREEN));
                        } else {
                            // Vice Chair: activate with Chair's perks + VC's perks
                            List<String> currentChairPerks = PerkManager.getChairSelectedPerks();
                            // Store first VC perk (for backwards compatibility)
                            PerkManager.setViceChairPerk(selectedPerks.get(0));
                            // Add all VC perks to active perks
                            List<String> allPerks = new ArrayList<>(currentChairPerks);
                            for (String vcPerkId : selectedPerks) {
                                if (!allPerks.contains(vcPerkId)) {
                                    allPerks.add(vcPerkId);
                                }
                            }
                            PerkManager.activatePerksDirectly(allPerks, selectedPerks);
                            player.sendMessage(Text.literal("Vice Chair perks activated!").formatted(Formatting.GREEN));
                        }
                        player.closeHandledScreen();
                    })
                    .build());
        } else {
            List<String> lore = new ArrayList<>();
            if (isChair) {
                if (selectedPerks.isEmpty()) {
                    lore.add("§c• Select at least 1 perk");
                }
                if (selectedPerks.size() > 6) {
                    lore.add("§c• Select a maximum of 6 perks");
                }
                if (currentPoints != 0) {
                    lore.add("§c• Point balance must be 0");
                }
            } else {
                if (selectedPerks.isEmpty()) {
                    lore.add("§c• Select at least 1 perk");
                }
                if (selectedPerks.size() > 2) {
                    lore.add("§c• Select a maximum of 2 perks");
                }
            }

            GuiElementBuilder confirmBuilder = new GuiElementBuilder(Items.RED_CONCRETE)
                    .setName(Text.literal("✗ CANNOT CONFIRM").formatted(Formatting.RED));
            for (String line : lore) {
                confirmBuilder.addLoreLine(Text.literal(line));
            }
            gui.setSlot(50, confirmBuilder.build());
        }

        // Tax toggle button (Chair only)
        if (isChair) {
            boolean taxEnabled = TaxManager.isTaxEnabled();
            gui.setSlot(49, new GuiElementBuilder(taxEnabled ? Items.GOLD_BLOCK : Items.GOLD_INGOT)
                    .setName(Text.literal("Tax System").formatted(Formatting.GOLD, Formatting.BOLD))
                    .addLoreLine(Text.literal("Status: " + (taxEnabled ? "ENABLED" : "DISABLED")).formatted(taxEnabled ? Formatting.GREEN : Formatting.RED))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("5 credits daily tax").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("+50% interest per day").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Click to toggle").formatted(Formatting.YELLOW))
                    .setCallback((index, type, action) -> {
                        TaxManager.setTaxEnabled(!taxEnabled);
                        openPage(player, isChair, currentPage);
                    })
                    .build());
        }

        gui.open();
    }

    private static Text getPointsLore(int points) {
        if (points > 0) {
            return Text.literal("Points: +" + points).formatted(Formatting.GREEN);
        } else if (points < 0) {
            return Text.literal("Points: " + points).formatted(Formatting.RED);
        } else {
            return Text.literal("Points: 0").formatted(Formatting.YELLOW);
        }
    }
}