package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/** Technique overview: your technique, mastery rank, abilities, and ability selection. */
public final class TechniqueGui {

    private TechniqueGui() {}

    public static void open(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Magic Technique"));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Component.literal("")).build());
        }

        if (!TechniqueManager.hasTechnique(uuid)) {
            gui.setSlot(22, new GuiElementBuilder(Items.NETHER_STAR)
                    .setName(Component.literal("✦ Unawakened").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7You have not awakened a technique."))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7Reach Magic Level §e" + TechniqueManager.AWAKEN_LEVEL))
                    .addLoreLine(Component.literal("§7and use an §dAwakening Catalyst§7."))
                    .addLoreLine(Component.literal("§8/technique awaken"))
                    .build());
            gui.open();
            return;
        }

        Technique t = TechniqueManager.getTechnique(uuid);
        Technique.MasteryRank rank = TechniqueManager.getRank(uuid);
        int uses = TechniqueManager.totalUses(uuid);

        List<Component> head = new ArrayList<>();
        head.add(Component.literal("§7" + t.flavor));
        head.add(Component.literal(""));
        head.add(Component.literal("§7Mastery: " + t.color + rank.display));
        head.add(Component.literal("§7Uses: §f" + uses));
        Technique.MasteryRank next = nextRank(rank);
        if (next != null) {
            head.add(Component.literal("§7Next rank at §f" + next.usesRequired + " §7uses"));
        } else {
            head.add(Component.literal("§dMastery complete."));
        }
        if (t.exclusive) {
            head.add(Component.literal(""));
            head.add(Component.literal("§d§lEXCLUSIVE §7— you are the only holder."));
        }
        head.add(Component.literal(""));
        head.add(Component.literal("§7Energy: §b" + (int) TechniqueManager.getEnergy(uuid)
                + "§7/§b" + TechniqueManager.getMaxEnergy(uuid)));

        GuiElementBuilder title = new GuiElementBuilder(Items.ENCHANTED_BOOK)
                .setName(Component.literal("✦ " + t.displayName).withStyle(t.color, ChatFormatting.BOLD))
                .glow();
        for (Component c : head) title.addLoreLine(c);
        gui.setSlot(4, title.build());

        Technique.Ability selected = TechniqueManager.getSelectedAbility(uuid);
        int[] slots = {19, 20, 21, 22, 23, 24, 25};
        int i = 0;
        for (Technique.Ability ab : t.abilities) {
            if (i >= slots.length) break;
            boolean unlocked = TechniqueManager.isAbilityUnlocked(uuid, ab);
            boolean isSel = selected != null && selected.id().equals(ab.id());

            GuiElementBuilder b = new GuiElementBuilder(
                    unlocked ? (isSel ? Items.LIME_CONCRETE : Items.YELLOW_CONCRETE) : Items.GRAY_CONCRETE)
                    .setName(Component.literal((unlocked ? "" : "§8") + ab.name())
                            .withStyle(unlocked ? t.color : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7" + ab.description()))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7Energy: §b" + ab.energyCost()))
                    .addLoreLine(Component.literal("§7Cooldown: §f" + (ab.cooldownTicks() / 20.0) + "s"));

            if (!unlocked) {
                Technique.MasteryRank req = Technique.MasteryRank.values()[
                        Math.min(ab.rankIndex(), Technique.MasteryRank.values().length - 1)];
                b.addLoreLine(Component.literal(""));
                b.addLoreLine(Component.literal("§cLocked §7— requires " + req.display
                        + " §7(" + req.usesRequired + " uses)"));
            } else if (isSel) {
                b.addLoreLine(Component.literal(""));
                b.addLoreLine(Component.literal("§a§lSELECTED"));
                b.glow();
            } else {
                b.addLoreLine(Component.literal(""));
                b.addLoreLine(Component.literal("§eClick to select"));
                final String abilityId = ab.id();
                b.setCallback((index, clickType, action, slotGui) -> {
                    TechniqueManager.selectAbility(uuid, abilityId);
                    player.sendSystemMessage(Component.literal("§aSelected: §f" + ab.name()), true);
                    open(player);
                });
            }
            gui.setSlot(slots[i], b.build());
            i++;
        }

        gui.setSlot(49, new GuiElementBuilder(Items.BOOK)
                .setName(Component.literal("How to cast").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Hold a §dMagic Staff§7 and §esneak + right-click"))
                .addLoreLine(Component.literal("§7to cast your selected technique ability."))
                .addLoreLine(Component.literal("§8Normal right-click still casts spells."))
                .build());

        gui.open();
    }

    private static Technique.MasteryRank nextRank(Technique.MasteryRank cur) {
        Technique.MasteryRank[] all = Technique.MasteryRank.values();
        for (int i = 0; i < all.length - 1; i++) {
            if (all[i] == cur) return all[i + 1];
        }
        return null;
    }
}
