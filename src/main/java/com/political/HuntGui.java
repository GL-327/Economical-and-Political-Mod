package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Map;

/**
 * The Hunt Board — the live contract, the roster of possible quarry, and the kill history.
 */
public final class HuntGui {

    private HuntGui() {}

    private static final int[] ROSTER_SLOTS = {28, 29, 30, 31, 32, 33, 34, 37};
    private static final int[] HISTORY_SLOTS = {19, 20, 21, 22, 23};

    public static void open(ServerPlayer player) {
        MinecraftServer server = ((ServerLevel) player.level()).getServer();
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("The Great Hunt").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        LivingWorldGui.fill(gui, 54);

        HuntManager.ActiveHunt active = HuntManager.active();
        boolean op = SeasonGui.isOperator(player);

        // ── The live contract ──
        if (active != null) {
            HuntManager.Archetype arch = active.archetype();
            double health = HuntManager.currentHealth(server);

            GuiElementBuilder b = new GuiElementBuilder(arch != null ? arch.icon() : Items.IRON_SWORD)
                    .setName(Component.literal("✦ " + (arch == null ? "Great Hunt" : arch.title())
                                    + (arch == null ? "" : ", " + arch.epithet()))
                            .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§c§lHUNT ACTIVE"))
                    .addLoreLine(Component.literal(""));
            if (arch != null) {
                b.addLoreLine(Component.literal("§7" + arch.loreLine()));
                b.addLoreLine(Component.literal(""));
                b.addLoreLine(Component.literal("§7Family: " + arch.family().label()));
            }
            b.addLoreLine(Component.literal("§7Last seen: §f" + HuntManager.locationHint()));
            b.addLoreLine(Component.literal(""));
            if (health >= 0) {
                b.addLoreLine(Component.literal("§7Health: §c" + Math.round(health)
                        + "§7/§f" + Math.round(active.maxHealth)));
                b.addLoreLine(Component.literal("  " + healthBar(health, active.maxHealth)));
            } else {
                b.addLoreLine(Component.literal("§8Out of loaded range — health unknown."));
            }
            b.addLoreLine(Component.literal(""));
            b.addLoreLine(Component.literal("§7Purse: §6" + HuntManager.fmt(HuntManager.currentReward())
                    + " coins"));
            if (arch != null) {
                b.addLoreLine(Component.literal("§7Trophy: §d"
                        + LivingWorldItems.trophyDisplayName(arch.trophyId())));
            }
            b.addLoreLine(Component.literal("§7Leaves in: §f"
                    + PoliticalServer.formatTime(active.remainingMs())));
            b.addLoreLine(Component.literal(""));
            b.addLoreLine(Component.literal("§8You must deal 20% of its health to claim"));
            b.addLoreLine(Component.literal("§8the full purse and the trophy."));
            b.glow();
            gui.setSlot(13, b.build());
        } else {
            gui.setSlot(13, new GuiElementBuilder(Items.CLOCK)
                    .setName(Component.literal("No Active Hunt")
                            .withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7The world is quiet. For now."))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7Next hunt in: §f"
                            + PoliticalServer.formatTime(HuntManager.msUntilNextHunt())))
                    .addLoreLine(Component.literal("§7Interval: §f"
                            + PoliticalServer.formatTime(HuntManager.cooldownMs())))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§8Hunts need at least one player online."))
                    .addLoreLine(Component.literal("§8Time offline is not banked."))
                    .build());
        }

        // ── Board header ──
        gui.setSlot(4, new GuiElementBuilder(Items.TARGET)
                .setName(Component.literal("Hunt Board").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Hunts felled: §f" + HuntManager.huntsCompleted()))
                .addLoreLine(Component.literal("§7Current purse: §6"
                        + HuntManager.fmt(HuntManager.currentReward()) + " coins"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7One hunt at a time, world-wide."))
                .addLoreLine(Component.literal("§7The purse goes to whoever fells it;"))
                .addLoreLine(Component.literal("§7up to 4 helpers split 15% each."))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(HuntManager.isEnabled()
                        ? "§aHunts are enabled." : "§cHunts are disabled."))
                .glow().build());

        // ── History ──
        List<HuntManager.HuntRecord> history = HuntManager.history();
        for (int i = 0; i < HISTORY_SLOTS.length; i++) {
            if (i < history.size()) {
                HuntManager.HuntRecord r = history.get(i);
                HuntManager.Archetype arch = HuntManager.archetypeById(r.archetypeId);
                gui.setSlot(HISTORY_SLOTS[i], new GuiElementBuilder(Items.BONE)
                        .setName(Component.literal((arch == null ? "Unknown Quarry" : arch.title()))
                                .withStyle(ChatFormatting.WHITE))
                        .addLoreLine(Component.literal("§7Felled by: §e"
                                + DataManager.getPlayerName(r.killerUuid)))
                        .addLoreLine(Component.literal("§7Purse: §6" + HuntManager.fmt(r.reward)))
                        .addLoreLine(Component.literal("§7Assists: §f" + r.assists))
                        .addLoreLine(Component.literal("§8"
                                + PoliticalServer.formatTime(
                                System.currentTimeMillis() - r.killedAtMs) + " ago"))
                        .build());
            } else {
                gui.setSlot(HISTORY_SLOTS[i], new GuiElementBuilder(Items.LIGHT_GRAY_STAINED_GLASS_PANE)
                        .setName(Component.literal("§8— no record —")).build());
            }
        }

        // ── Roster of possible quarry ──
        Map<String, Integer> byArch = HuntManager.killsByArchetype();
        List<HuntManager.Archetype> roster = HuntManager.archetypes();
        for (int i = 0; i < roster.size() && i < ROSTER_SLOTS.length; i++) {
            HuntManager.Archetype arch = roster.get(i);
            boolean isLive = active != null && arch.id().equals(active.archetypeId);
            int felled = byArch.getOrDefault(arch.id(), 0);

            GuiElementBuilder b = new GuiElementBuilder(arch.icon())
                    .setName(Component.literal(arch.title() + ", " + arch.epithet())
                            .withStyle(isLive ? ChatFormatting.RED : ChatFormatting.GRAY,
                                    ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7" + arch.loreLine()))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7Family: " + arch.family().label()))
                    .addLoreLine(Component.literal("§7Health: §c×"
                            + String.format(java.util.Locale.ROOT, "%.1f", arch.healthMult())))
                    .addLoreLine(Component.literal("§7Damage: §c×"
                            + String.format(java.util.Locale.ROOT, "%.1f", arch.damageMult())))
                    .addLoreLine(Component.literal("§7Trophy: §d"
                            + LivingWorldItems.trophyDisplayName(arch.trophyId())))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7Times felled: §f" + felled));
            if (isLive) {
                b.addLoreLine(Component.literal(""));
                b.addLoreLine(Component.literal("§c§lWALKING NOW"));
                b.glow();
            }
            gui.setSlot(ROSTER_SLOTS[i], b.build());
        }

        // ── Trophies panel ──
        gui.setSlot(43, new GuiElementBuilder(Items.NETHERITE_HELMET)
                .setName(Component.literal("Grand Trophy Crest")
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Crafted from one of each of the four"))
                .addLoreLine(Component.literal("§7hunt trophies — four separate victories."))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7❁ Strength: §c+30"))
                .addLoreLine(Component.literal("§7❈ Defense: §a+40"))
                .addLoreLine(Component.literal("§7❤ Health: §c+50"))
                .addLoreLine(Component.literal("§7☠ Crit Damage: §c+25"))
                .addLoreLine(Component.literal("§7✎ Intelligence: §b+50"))
                .addLoreLine(Component.literal("§7✦ Speed: §f+5"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to view the recipe"))
                .setCallback((a, b, c, d) -> {
                    player.sendSystemMessage(Component.literal(
                            "§6Grand Trophy Crest §7— one of each trophy in the corners,"));
                    player.sendSystemMessage(Component.literal(
                            "§7gold blocks between, a netherite helmet in the centre."));
                    player.sendSystemMessage(Component.literal(
                            "§8Browse it in the Recipes menu for the exact grid."));
                    RecipesGui.openMainMenu(player);
                })
                .build());

        // ── Operator controls ──
        if (op) {
            gui.setSlot(46, new GuiElementBuilder(Items.BLAZE_ROD)
                    .setName(Component.literal("Force Hunt Now")
                            .withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Despawns any live hunt and starts"))
                    .addLoreLine(Component.literal("§7a fresh one immediately."))
                    .addLoreLine(Component.literal("§8[Operator]"))
                    .setCallback((a, b, c, d) -> {
                        HuntManager.forceSpawn(server);
                        open(player);
                    })
                    .build());

            gui.setSlot(47, new GuiElementBuilder(HuntManager.isEnabled()
                    ? Items.LIME_DYE : Items.GRAY_DYE)
                    .setName(Component.literal(HuntManager.isEnabled()
                            ? "Hunts: ENABLED" : "Hunts: DISABLED")
                            .withStyle(HuntManager.isEnabled() ? ChatFormatting.GREEN : ChatFormatting.RED,
                                    ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Click to toggle."))
                    .addLoreLine(Component.literal("§8[Operator]"))
                    .setCallback((a, b, c, d) -> {
                        HuntManager.setEnabled(!HuntManager.isEnabled());
                        open(player);
                    })
                    .build());

            gui.setSlot(48, new GuiElementBuilder(Items.REPEATER)
                    .setName(Component.literal("Hunt Interval")
                            .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Current: §f"
                            + PoliticalServer.formatTime(HuntManager.cooldownMs())))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§eLeft-click §7+30 minutes"))
                    .addLoreLine(Component.literal("§eRight-click §7-30 minutes"))
                    .addLoreLine(Component.literal("§8[Operator]"))
                    .setCallback((index, clickType, action, slotGui) -> {
                        long delta = clickType.isRight ? -30L * 60_000L : 30L * 60_000L;
                        HuntManager.setCooldownMs(HuntManager.cooldownMs() + delta);
                        open(player);
                    })
                    .build());
        }

        gui.setSlot(49, LivingWorldGui.back(() -> LivingWorldGui.open(player)));
        gui.open();
    }

    private static String healthBar(double health, double max) {
        if (max <= 0) return "§8||||||||||||||||||||";
        int filled = (int) Math.round(Math.max(0, Math.min(1.0, health / max)) * 20);
        StringBuilder sb = new StringBuilder("§c");
        for (int i = 0; i < 20; i++) {
            if (i == filled) sb.append("§8");
            sb.append('|');
        }
        return sb.toString();
    }
}
