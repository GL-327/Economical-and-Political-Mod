package com.political.technique;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.Set;

/** Learn / equip / rank UI for the eight learned techniques. */
public final class TechniqueGui {

    private TechniqueGui() {}

    public static void open(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Techniques"));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal("")).build());
        }

        Technique equipped = TechniqueManager.getTechnique(uuid);
        Set<Technique> learned = TechniqueManager.learned(uuid);

        gui.setSlot(4, new GuiElementBuilder(Items.ENCHANTED_BOOK)
                .setName(Component.literal(equipped == null ? "No technique equipped" : equipped.displayName)
                        .withStyle(equipped == null ? ChatFormatting.GRAY : equipped.color, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(equipped == null
                        ? "Right-click a Technique Manual to learn."
                        : equipped.flavor).withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(equipped == null ? "" : "Rank: " + TechniqueManager.getRank(uuid).display)
                        .withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal("Energy: " + (int) TechniqueManager.getEnergy(uuid)
                        + "/" + TechniqueManager.getMaxEnergy(uuid)).withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal("/technique cast or sneak-use to fire.").withStyle(ChatFormatting.DARK_GRAY))
                .build());

        int slot = 19;
        for (Technique technique : Technique.all()) {
            boolean known = learned.contains(technique);
            boolean isEq = equipped == technique;
            GuiElementBuilder tile = new GuiElementBuilder(
                    !known ? Items.BOOK : (isEq ? Items.LIME_CONCRETE : Items.YELLOW_CONCRETE))
                    .setName(Component.literal(technique.displayName).withStyle(technique.color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(technique.flavor).withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""));
            if (!known) {
                tile.addLoreLine(Component.literal("Not learned. Craft or find a manual.").withStyle(ChatFormatting.RED));
            } else {
                int uses = TechniqueManager.usesOf(uuid, technique);
                Technique.MasteryRank rank = Technique.MasteryRank.forUses(uses);
                tile.addLoreLine(Component.literal("Rank: " + rank.display + " (" + uses + " uses)")
                        .withStyle(ChatFormatting.WHITE));
                tile.addLoreLine(Component.literal(isEq ? "Equipped" : "Click to equip").withStyle(
                        isEq ? ChatFormatting.GREEN : ChatFormatting.YELLOW));
                if (isEq) tile.glow();
                tile.setCallback((a, b, c, d) -> {
                    TechniqueManager.equip(player, technique.displayName);
                    open(player);
                });
            }
            gui.setSlot(slot++, tile.build());
            if (slot == 26) slot = 28;
        }

        if (equipped != null) {
            int abSlot = 37;
            Technique.Ability selected = TechniqueManager.getSelectedAbility(uuid);
            for (Technique.Ability ability : equipped.abilities) {
                boolean unlocked = TechniqueManager.isAbilityUnlocked(uuid, ability);
                boolean isSel = selected != null && selected.id().equals(ability.id());
                GuiElementBuilder b = new GuiElementBuilder(
                        !unlocked ? Items.GRAY_CONCRETE : (isSel ? Items.LIME_DYE : Items.PAPER))
                        .setName(Component.literal(ability.name()).withStyle(unlocked ? equipped.color : ChatFormatting.DARK_GRAY))
                        .addLoreLine(Component.literal(ability.description()).withStyle(ChatFormatting.GRAY))
                        .addLoreLine(Component.literal("Rank " + (ability.rankIndex() + 1)
                                + "  Energy " + ability.energyCost()).withStyle(ChatFormatting.DARK_GRAY));
                if (unlocked) {
                    final String abilityId = ability.id();
                    b.setCallback((a, c, d, e) -> {
                        TechniqueManager.selectAbility(uuid, abilityId);
                        open(player);
                    });
                }
                gui.setSlot(abSlot++, b.build());
            }
        }

        gui.open();
    }
}
