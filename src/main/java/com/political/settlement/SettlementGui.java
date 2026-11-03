package com.political.settlement;
import com.political.AtlasMapGui;
import com.political.Citizen;
import com.political.DataManager;
import com.political.government.Treasury;


import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

/** Settlement info panel: tier progress, stats, leaders, registration. */
public final class SettlementGui {

    private SettlementGui() {}

    public static void open(ServerPlayer player, Settlement s) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x5, player, false);
        SettlementTier tier = s.tierEnum();
        gui.setTitle(Component.literal(s.name + " — " + tier.displayName));

        for (int i = 0; i < 45; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.black())
                    .setName(Component.literal("")).build());
        }

        // Banner / identity
        gui.setSlot(4, new GuiElementBuilder(Items.BELL)
                .setName(Component.literal(s.name).withStyle(tier.color, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Tier: " + tier.colorCode + tier.displayName))
                .addLoreLine(Component.literal("§7Founder: §f" + DataManager.getPlayerName(s.ownerUuid)))
                .addLoreLine(Component.literal("§7At §f" + s.cornerstoneX + ", " + s.cornerstoneY
                        + ", " + s.cornerstoneZ))
                .addLoreLine(Component.literal("§8" + s.worldDim))
                .glow()
                .build());

        gui.setSlot(19, stat(Items.BED.red(), "Beds", s.beds, tier.next() == null ? -1 : tier.next().minBeds));
        gui.setSlot(20, stat(Items.PLAYER_HEAD, "Population", s.population,
                tier.next() == null ? -1 : tier.next().minPop));
        gui.setSlot(21, stat(Items.FLOWER_POT, "Appeal", s.appeal,
                tier.next() == null ? -1 : tier.next().minAppeal));
        gui.setSlot(22, stat(Items.GOLD_INGOT, "Treasury", (int) Math.min(Integer.MAX_VALUE, s.treasury),
                tier.next() == null ? -1 : (int) Math.min(Integer.MAX_VALUE, tier.next().minTreasury)));
        gui.setSlot(23, stat(Items.CRAFTING_TABLE, "Job sites", s.jobSites, -1));
        gui.setSlot(24, stat(Items.BEACON, "Wonders", s.wonders,
                tier.next() == null ? -1 : tier.next().minWonders));

        // ── Economy row ──
        gui.setSlot(28, new GuiElementBuilder(Items.CAKE)
                .setName(Component.literal("Happiness").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§f" + s.happiness + "%"))
                .addLoreLine(Component.literal("§8Gates growth and multiplies income"))
                .build());
        gui.setSlot(37, new GuiElementBuilder(Items.BREAD)
                .setName(Component.literal("Food").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§f" + s.food + " §7/ §f" + s.population + " needed"))
                .addLoreLine(Component.literal(s.population > 0 && s.food < s.population
                        ? "§cStarving — growth halted" : "§aFed"))
                .build());
        gui.setSlot(38, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Component.literal("Defense").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§f" + s.defense))
                .addLoreLine(Component.literal("§8Sheriffs and deputies deter raids"))
                .build());
        gui.setSlot(39, new GuiElementBuilder(Items.EMERALD)
                .setName(Component.literal("Income").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Last cycle: §6+" + s.lastIncome))
                .addLoreLine(Component.literal("§7Tax remitted: §c" + s.lastTaxPaid))
                .addLoreLine(Component.literal("§8Workers produce goods and trade"))
                .build());

        // Next tier requirements
        SettlementTier next = tier.next();
        if (next != null) {
            gui.setSlot(31, new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
                    .setName(Component.literal("Next: " + next.displayName)
                            .withStyle(next.color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Requires:"))
                    .addLoreLine(req("Beds", s.beds, next.minBeds))
                    .addLoreLine(req("Population", s.population, next.minPop))
                    .addLoreLine(req("Appeal", s.appeal, next.minAppeal))
                    .addLoreLine(req("Treasury", s.treasury, next.minTreasury))
                    .addLoreLine(req("Wonders", s.wonders, next.minWonders))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§8Settlements re-scan periodically."))
                    .build());
        } else {
            gui.setSlot(31, new GuiElementBuilder(Items.NETHER_STAR)
                    .setName(Component.literal("Maximum Tier").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7This settlement has reached the summit."))
                    .glow().build());
        }

        // Leaders
        GuiElementBuilder leaders = new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Component.literal("Leadership").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        if (s.leaders == null || s.leaders.isEmpty()) {
            leaders.addLoreLine(Component.literal("§8No elected leaders."));
            if (!tier.isAtLeast(SettlementTier.CITY)) {
                leaders.addLoreLine(Component.literal("§7Reach §eCITY §7to elect a Constituent."));
            }
        } else {
            s.leaders.forEach((role, uuid) -> leaders.addLoreLine(
                    Component.literal("§7" + role + ": §f" + DataManager.getPlayerName(uuid))));
        }
        if (s.nearestMetropolisId != null) {
            Settlement metro = SettlementManager.get(s.nearestMetropolisId);
            if (metro != null) {
                leaders.addLoreLine(Component.literal(""));
                leaders.addLoreLine(Component.literal("§7Follows: §6" + metro.name));
            }
        }
        gui.setSlot(29, leaders.build());

        // Registration
        String uuid = player.getUUID().toString();
        boolean registered = s.registeredPlayers != null && s.registeredPlayers.contains(uuid);
        long fee = SettlementManager.registrationFee(tier);
        gui.setSlot(33, new GuiElementBuilder(registered ? Items.CONCRETE.lime() : Items.PAPER)
                .setName(Component.literal(registered ? "Registered Citizen" : "Register Here")
                        .withStyle(registered ? ChatFormatting.GREEN : ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(registered
                        ? "§7You are a citizen of this settlement."
                        : "§7Register to vote and stand for office."))
                .addLoreLine(Component.literal(registered ? "§8Click to leave" : "§7Fee: §6" + fee + " coins"))
                .setCallback((index, clickType, action, slotGui) -> {
                    if (registered) {
                        SettlementManager.leaveSettlement(player);
                    } else {
                        SettlementManager.registerPlayer(player, s);
                    }
                    player.closeContainer();
                })
                .build());

        gui.setSlot(40, new GuiElementBuilder(Items.FILLED_MAP)
                .setName(Component.literal("Open Atlas").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7View every known settlement."))
                .setCallback((index, clickType, action, slotGui) -> AtlasMapGui.open(player))
                .build());

        gui.open();
    }

    private static eu.pb4.sgui.api.elements.GuiElement stat(net.minecraft.world.item.Item icon,
                                                     String label, long value, long target) {
        GuiElementBuilder b = new GuiElementBuilder(icon)
                .setName(Component.literal(label).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§f" + value));
        if (target > 0) {
            b.addLoreLine(Component.literal("§7Next tier: §f" + target));
        }
        return b.build();
    }

    private static Component req(String label, long have, long need) {
        boolean ok = have >= need;
        return Component.literal("  " + (ok ? "§a✔ " : "§c✘ ") + "§7" + label + ": §f"
                + have + "§7/§f" + need);
    }
}
