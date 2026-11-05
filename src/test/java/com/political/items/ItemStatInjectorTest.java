package com.political.items;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

/** The generated stat lines of an armour piece go inside its own STATS block, in its style, and no original line is lost. */
class ItemStatInjectorTest {

    private static List<Component> t1Lore() {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("[MFLUX: Field-tested containment gear | Contractor Program]"));
        lore.add(Component.literal("━━━ STATS ━━━"));
        lore.add(Component.literal("🛡 Armor: +5"));
        lore.add(Component.literal("💎 Toughness: +2"));
        lore.add(Component.literal("❤ Health: +2 Hearts"));
        lore.add(Component.literal("━━━ BOSS DEFENCE ━━━"));
        lore.add(Component.literal("🛡 This piece: -8% Zombie / -2% all"));
        lore.add(Component.literal("⚔ Full set bonus: -26% Zombie / -4% all"));
        lore.add(Component.literal("━━━ PIECE BUFF ━━━"));
        lore.add(Component.literal("✦ +2 Hearts Health"));
        lore.add(Component.literal("⚠ Requires: Zombie Bounty Lvl 5"));
        return lore;
    }

    private static List<Component> generated() {
        return List.of(
                ItemStatInjector.statLine("❈ Defense", ChatFormatting.GREEN, "+25"),
                ItemStatInjector.statLine("❤ Health", ChatFormatting.RED, "+15"),
                ItemStatInjector.statLine("❁ Strength", ChatFormatting.RED, "+15"));
    }

    private static List<String> texts(List<Component> lines) {
        return lines.stream().map(Component::getString).toList();
    }

    @Test
    void theGeneratedLinesSitInsideTheStatsBlockBeforeTheHeartsLine() {
        List<Component> merged = ItemStatInjector.mergeIntoStatsBlock(t1Lore(), generated());
        assertEquals(List.of(
                "[MFLUX: Field-tested containment gear | Contractor Program]",
                "━━━ STATS ━━━",
                "🛡 Armor: +5",
                "💎 Toughness: +2",
                "❈ Defense: +25",
                "❤ Health: +15",
                "❁ Strength: +15",
                "❤ Health: +2 Hearts",
                "━━━ BOSS DEFENCE ━━━",
                "🛡 This piece: -8% Zombie / -2% all",
                "⚔ Full set bonus: -26% Zombie / -4% all",
                "━━━ PIECE BUFF ━━━",
                "✦ +2 Hearts Health",
                "⚠ Requires: Zombie Bounty Lvl 5"), texts(merged));
    }

    @Test
    void everyOriginalLineIsKeptEvenWhenTheLoreIsLongerThanTheOldLineLimit() {
        List<Component> base = t1Lore();
        List<Component> merged = ItemStatInjector.mergeIntoStatsBlock(base, generated());
        assertEquals(base.size() + 3, merged.size());
        assertTrue(texts(merged).containsAll(texts(base)));
        // the lines the old nine-line cut dropped: the real piece buff and the level requirement
        assertTrue(texts(merged).contains("✦ +2 Hearts Health"));
        assertTrue(texts(merged).contains("⚠ Requires: Zombie Bounty Lvl 5"));
    }

    @Test
    void aBlockWithoutAHealthLineTakesTheLinesAtItsEnd() {
        List<Component> base = new ArrayList<>();
        base.add(Component.literal("━━━ STATS ━━━"));
        base.add(Component.literal("🛡 Armor: +5"));
        base.add(Component.literal("━━━ BOSS DEFENCE ━━━"));
        List<String> merged = texts(ItemStatInjector.mergeIntoStatsBlock(base, generated()));
        assertEquals(List.of("━━━ STATS ━━━", "🛡 Armor: +5", "❈ Defense: +25", "❤ Health: +15", "❁ Strength: +15", "━━━ BOSS DEFENCE ━━━"), merged);
    }

    @Test
    void loreWithoutAStatsBlockIsLeftToTheOldPath() {
        List<Component> base = List.of(Component.literal("Some armour"), Component.literal("Another line"));
        assertNull(ItemStatInjector.mergeIntoStatsBlock(base, generated()));
    }

    @Test
    void theStatsEngineStillFindsTheLines() {
        // PlayerStatsManager reads "<name>: +<number>" after the colour codes are stripped: the label and the number must stay in one line
        for (Component line : generated()) {
            String text = line.getString();
            int colon = text.indexOf(": +");
            assertTrue(colon > 0 && text.substring(colon + 3).chars().allMatch(Character::isDigit), text);
        }
    }
}
