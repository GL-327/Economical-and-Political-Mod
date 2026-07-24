package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * V6 — the Cursed Spirits codex.
 *
 * <p>One screen that explains the grades, shows the archetypes you have exorcised, your Cursed
 * Energy balance and Curse Hunter rank, the drop tables, and lets you redeem Shards/Orbs and
 * spend Cursed Energy on reagents. Everything the pillar offers is reachable here — commands
 * are pure convenience.</p>
 */
public final class CursedSpiritGui {

    private CursedSpiritGui() {}

    /** Cursed Energy sinks (a use for the currency; never mints coins). */
    public static final int RESIDUE_COST = 50;
    public static final int CORE_COST    = 300;

    public static void open(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Cursed Spirits — Codex")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));

        ItemStack filler = new ItemStack(Items.BLACK_STAINED_GLASS_PANE);
        filler.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.literal(" "));
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(filler.getItem()).setName(Component.literal(" ")).build());
        }

        // ── Header: energy + rank ──
        CursedEnergyManager.HunterRank rank = CursedEnergyManager.rank(uuid);
        long energy = CursedEnergyManager.getEnergy(uuid);
        int slain = CursedEnergyManager.lifetimeSlain(uuid);
        int toNext = CursedEnergyManager.killsToNextRank(uuid);

        GuiElementBuilder head = new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal("Curse Hunter: " + player.getName().getString())
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Cursed Energy: §d" + energy))
                .addLoreLine(Component.literal("§7Rank: §5" + rank.label))
                .addLoreLine(Component.literal("§7Passive vs spirits: §a+" + rank.bonusPercent + "%"))
                .addLoreLine(Component.literal("§7Lifetime exorcised: §f" + slain))
                .addLoreLine(Component.literal(toNext > 0
                        ? "§8Next rank in §7" + toNext + "§8 kills"
                        : "§8Highest rank reached"))
                .glow();
        gui.setSlot(4, head.build());

        // ── Grades (slots 9..13) ──
        int[] gradeSlots = {9, 10, 11, 12, 13};
        CursedGrade[] grades = CursedGrade.values();
        for (int i = 0; i < grades.length && i < gradeSlots.length; i++) {
            CursedGrade g = grades[i];
            int myKills = CursedEnergyManager.gradeKills(uuid, g);
            gui.setSlot(gradeSlots[i], new GuiElementBuilder(gradeIcon(g))
                    .setName(Component.literal(g.label).withStyle(g.color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Health x§f" + trim(g.healthMultiplier)
                            + " §8· §7Damage x§f" + trim(g.damageMultiplier)))
                    .addLoreLine(Component.literal("§7Reward: §d" + g.cursedEnergyReward + " Cursed Energy"))
                    .addLoreLine(Component.literal("§7Rarity weight: §f" + g.spawnWeight))
                    .addLoreLine(Component.literal("§7Min region danger: §f" + g.minRegionDanger))
                    .addLoreLine(Component.literal("§8You have exorcised §7" + myKills + "§8."))
                    .build());
        }

        // ── Archetypes (slots 18..30) ──
        int slot = 18;
        for (CursedSpirit arch : CursedSpirit.ARCHETYPES) {
            if (slot > 30) break;
            boolean seen = CursedEnergyManager.hasEncountered(uuid, arch.id());
            GuiElementBuilder el;
            if (seen) {
                el = new GuiElementBuilder(gradeIcon(arch.grade()))
                        .setName(Component.literal(arch.name()).withStyle(arch.grade().color, ChatFormatting.BOLD))
                        .addLoreLine(Component.literal("§8" + arch.grade().label))
                        .addLoreLine(Component.literal("§7" + arch.flavour()))
                        .addLoreLine(Component.literal("§8Curse type: §7" + prettyAbility(arch.ability())))
                        .glow();
            } else {
                el = new GuiElementBuilder(Items.GRAY_DYE)
                        .setName(Component.literal("??? Unknown Curse").withStyle(ChatFormatting.DARK_GRAY))
                        .addLoreLine(Component.literal("§8A " + arch.grade().label + " you have not met."))
                        .addLoreLine(Component.literal("§8Exorcise one to record it here."));
            }
            gui.setSlot(slot++, el.build());
        }

        // ── Drop table info (slot 40) ──
        gui.setSlot(40, new GuiElementBuilder(Items.CHEST)
                .setName(Component.literal("Drop Tables").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7All spirits drop §dCursed Energy Shards§7."))
                .addLoreLine(Component.literal("§7Grade 4/3 → §fCursed Residue"))
                .addLoreLine(Component.literal("§7Grade 2/1 → §fCursed Core §7+ §fOrbs"))
                .addLoreLine(Component.literal("§7Special Grade → §cSpecial Grade Relic"))
                .addLoreLine(Component.literal("§8Reagents craft cursed gear (/recipe)."))
                .build());

        // ── Redeem Shards/Orbs (slot 47) ──
        gui.setSlot(47, new GuiElementBuilder(Items.AMETHYST_SHARD)
                .setName(Component.literal("Redeem Shards & Orbs").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Convert every Cursed Energy Shard (§d" + CursedItems.SHARD_ENERGY
                        + "§7) and Orb (§d" + CursedItems.ORB_ENERGY + "§7)"))
                .addLoreLine(Component.literal("§7in your inventory into balance."))
                .addLoreLine(Component.literal("§eClick to redeem all"))
                .setCallback((idx, click, action, g) -> redeemAll(player))
                .build());

        // ── Spend: buy Residue (slot 49) ──
        gui.setSlot(49, new GuiElementBuilder(Items.GUNPOWDER)
                .setName(Component.literal("Channel → Cursed Residue").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Spend §d" + RESIDUE_COST + " Cursed Energy§7 for 1 Residue."))
                .addLoreLine(Component.literal(CursedEnergyManager.canAfford(player, RESIDUE_COST)
                        ? "§eClick to channel" : "§cNot enough Cursed Energy"))
                .setCallback((idx, click, action, g) -> buy(player, RESIDUE_COST, CursedItems.createResidue(), "Cursed Residue"))
                .build());

        // ── Spend: buy Core (slot 51) ──
        gui.setSlot(51, new GuiElementBuilder(Items.ECHO_SHARD)
                .setName(Component.literal("Channel → Cursed Core").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Spend §d" + CORE_COST + " Cursed Energy§7 for 1 Core."))
                .addLoreLine(Component.literal(CursedEnergyManager.canAfford(player, CORE_COST)
                        ? "§eClick to channel" : "§cNot enough Cursed Energy"))
                .setCallback((idx, click, action, g) -> buy(player, CORE_COST, CursedItems.createCore(), "Cursed Core"))
                .build());

        // ── Close (slot 53) ──
        gui.setSlot(53, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Close").withStyle(ChatFormatting.RED))
                .setCallback((idx, click, action, g) -> player.closeContainer())
                .build());

        gui.open();
    }

    // ── Actions ─────────────────────────────────────────────────────────────────
    private static void redeemAll(ServerPlayer player) {
        long gained = 0;
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack st = inv.getItem(i);
            int value = CursedItems.redeemValue(st);
            if (value > 0) {
                gained += (long) value * st.getCount();
                inv.setItem(i, ItemStack.EMPTY);
            }
        }
        if (gained > 0) {
            CursedEnergyManager.earn(player, gained);
            player.sendSystemMessage(Component.literal("§dRedeemed §5+" + gained + " Cursed Energy§d."));
        } else {
            player.sendSystemMessage(Component.literal("§8No Shards or Orbs to redeem."));
        }
        open(player);
    }

    private static void buy(ServerPlayer player, int cost, ItemStack reward, String name) {
        if (!CursedEnergyManager.spend(player, cost)) {
            player.sendSystemMessage(Component.literal("§cNot enough Cursed Energy."));
            open(player);
            return;
        }
        if (!player.getInventory().add(reward)) player.drop(reward, false);
        player.sendSystemMessage(Component.literal("§7Channelled §d" + cost + " Cursed Energy §7into 1 " + name + "."));
        open(player);
    }

    // ── Cosmetics ───────────────────────────────────────────────────────────────
    private static net.minecraft.world.item.Item gradeIcon(CursedGrade g) {
        return switch (g) {
            case GRADE_4 -> Items.ZOMBIE_HEAD;
            case GRADE_3 -> Items.SKELETON_SKULL;
            case GRADE_2 -> Items.WITHER_SKELETON_SKULL;
            case GRADE_1 -> Items.CREEPER_HEAD;
            case SPECIAL_GRADE -> Items.DRAGON_HEAD;
        };
    }

    private static String prettyAbility(CursedSpirit.Ability a) {
        return switch (a) {
            case ROT_AURA -> "Rot aura (hunger, weakness)";
            case WEB_SNARE -> "Snare (slowness)";
            case VENOM -> "Venom (poison)";
            case CHILL -> "Chill (weakness, fatigue)";
            case CHOKE -> "Choke (slowness, nausea)";
            case DECAY -> "Decay (wither)";
            case DREAD -> "Dread (darkness pulses)";
            case PLAGUE -> "Plague (hunger, poison)";
            case WITHER_FLAME -> "Witherflame (heavy wither)";
            case GAZE -> "Gaze (darkness, levitation)";
            case QUAKE -> "Quake (knockback, fatigue)";
            case DOMAIN -> "Domain (blink, strength)";
            case CATACLYSM -> "Cataclysm (all of the above)";
        };
    }

    private static String trim(double d) {
        if (d == Math.floor(d)) return String.valueOf((long) d);
        return String.valueOf(d);
    }
}
