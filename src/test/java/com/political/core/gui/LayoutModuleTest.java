package com.political.core.gui;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;
import org.junit.jupiter.api.Test;

/** The arithmetic of the layout module: slot grid, pages, stepper clicks, progress bars and the palette. */
class LayoutModuleTest {

    // ── Layout ──────────────────────────────────────────────────────────────────────────────

    @Test
    void sizesAndRows() {
        assertEquals(9, Layout.size(1));
        assertEquals(54, Layout.size(6));
        assertEquals(3, Layout.rows(27));
        assertThrows(IllegalArgumentException.class, () -> Layout.size(0));
        assertThrows(IllegalArgumentException.class, () -> Layout.size(7));
        assertThrows(IllegalArgumentException.class, () -> Layout.rows(10));
        assertThrows(IllegalArgumentException.class, () -> Layout.rows(63));
    }

    @Test
    void edgeIsTheOuterRing() {
        int size = 54;
        Set<Integer> edge = new HashSet<>();
        for (int slot = 0; slot < size; slot++) {
            if (Layout.isEdge(slot, size)) {
                edge.add(slot);
            }
        }
        assertEquals(9 + 9 + 2 * 4, edge.size(), "top row, bottom row and two slots in each of the four middle rows");
        assertTrue(edge.contains(0) && edge.contains(8) && edge.contains(45) && edge.contains(53));
        assertTrue(edge.contains(9) && edge.contains(17));
        assertFalse(edge.contains(10) && edge.contains(16));
    }

    @Test
    void footerSlotsSitInTheBottomRow() {
        for (int rows = 1; rows <= 6; rows++) {
            int size = Layout.size(rows);
            assertEquals(size - 9, Layout.backSlot(size));
            assertEquals(size - 1, Layout.closeSlot(size));
            assertEquals(size - 6, Layout.previousPageSlot(size));
            assertEquals(size - 5, Layout.pageInfoSlot(size));
            assertEquals(size - 4, Layout.nextPageSlot(size));
        }
    }

    @Test
    void sixRowInteriorIsTheTwentyEightSlotListArea() {
        // The list area GuiKit has always used, so screens moved to the module keep their entries where they were.
        int[] guiKitListSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };
        assertArrayEquals(guiKitListSlots, Layout.interior(54));
        assertEquals(28, Layout.perPage(54));
    }

    @Test
    void interiorOfSmallerScreens() {
        assertArrayEquals(new int[] {10, 11, 12, 13, 14, 15, 16}, Layout.interior(27));
        assertEquals(14, Layout.perPage(36));
        assertEquals(0, Layout.perPage(18));
        assertEquals(0, Layout.perPage(9));
    }

    @Test
    void interiorSlotsAreNeverEdgeOrFooter() {
        for (int rows = 1; rows <= 6; rows++) {
            int size = Layout.size(rows);
            for (int slot : Layout.interior(size)) {
                assertFalse(Layout.isEdge(slot, size), "slot " + slot + " on " + rows + " rows is on the edge");
            }
        }
    }

    @Test
    void confirmationSlotsFitTheThreeRowScreen() {
        int size = Layout.size(Layout.CONFIRM_ROWS);
        assertTrue(Layout.CONFIRM_YES_SLOT < size && Layout.CONFIRM_NO_SLOT < size);
        assertFalse(Layout.isEdge(Layout.CONFIRM_YES_SLOT, size));
        assertFalse(Layout.isEdge(Layout.CONFIRM_NO_SLOT, size));
        assertEquals(Layout.HEADER_SLOT, Layout.CONFIRM_INFO_SLOT);
        assertTrue(Layout.CONFIRM_YES_SLOT < Layout.CONFIRM_NO_SLOT, "confirm on the left, cancel on the right");
    }

    // ── Pager ───────────────────────────────────────────────────────────────────────────────

    @Test
    void pageCountIsAtLeastOne() {
        assertEquals(1, Pager.pageCount(0, 28));
        assertEquals(1, Pager.pageCount(28, 28));
        assertEquals(2, Pager.pageCount(29, 28));
        assertEquals(3, Pager.pageCount(57, 28));
        assertEquals(1, Pager.pageCount(-5, 28));
        assertThrows(IllegalArgumentException.class, () -> Pager.pageCount(10, 0));
    }

    @Test
    void pageNumbersAreClamped() {
        assertEquals(0, Pager.clamp(-3, 4));
        assertEquals(3, Pager.clamp(9, 4));
        assertEquals(2, Pager.clamp(2, 4));
        assertEquals(0, Pager.clamp(5, 1));
    }

    @Test
    void previousAndNextAvailability() {
        assertFalse(Pager.hasPrevious(0));
        assertTrue(Pager.hasPrevious(1));
        assertTrue(Pager.hasNext(0, 2));
        assertFalse(Pager.hasNext(1, 2));
        assertFalse(Pager.hasNext(0, 1));
        assertEquals(56, Pager.firstIndex(2, 28));
    }

    // ── Stepper ─────────────────────────────────────────────────────────────────────────────

    @Test
    void stepperClicks() {
        assertEquals(5, Stepper.delta(5, false, false));
        assertEquals(-5, Stepper.delta(5, true, false));
        assertEquals(50, Stepper.delta(5, false, true));
        assertEquals(-50, Stepper.delta(5, true, true));
        assertThrows(ArithmeticException.class, () -> Stepper.delta(Long.MAX_VALUE, false, true));
    }

    @Test
    void stepperResultsStayInRange() {
        assertEquals(10, Stepper.clamped(5, 50, 0, 10));
        assertEquals(0, Stepper.clamped(5, -50, 0, 10));
        assertEquals(7, Stepper.clamped(5, 2, 0, 10));
    }

    // ── Progress bar ────────────────────────────────────────────────────────────────────────

    @Test
    void progressBarCellsAndPercent() {
        assertEquals(0, ProgressBar.filledCells(0.0, 10));
        assertEquals(5, ProgressBar.filledCells(0.5, 10));
        assertEquals(10, ProgressBar.filledCells(1.0, 10));
        assertEquals(1, ProgressBar.filledCells(0.05, 10), "0.5 of a cell rounds up");
        assertEquals(0, ProgressBar.filledCells(0.049, 10));
        assertEquals(50, ProgressBar.percent(0.5));
        assertEquals(34, ProgressBar.percent(0.337));
    }

    @Test
    void progressBarCopesWithNonsense() {
        assertEquals(0, ProgressBar.filledCells(-2.0, 10));
        assertEquals(10, ProgressBar.filledCells(7.0, 10));
        assertEquals(0, ProgressBar.filledCells(Double.NaN, 10));
        assertEquals(0, ProgressBar.percent(Double.NaN));
        assertEquals(100, ProgressBar.percent(Double.POSITIVE_INFINITY));
    }

    @Test
    void progressBarTextAlwaysHasTenCells() {
        for (double f : new double[] {0.0, 0.1, 0.33, 0.5, 0.99, 1.0}) {
            assertEquals(10, ProgressBar.filled(f, 10).length() + ProgressBar.empty(f, 10).length());
        }
        assertEquals("|||||", ProgressBar.filled(0.5, 10));
        assertEquals("|||||", ProgressBar.empty(0.5, 10));
    }

    // ── Palette ─────────────────────────────────────────────────────────────────────────────

    @Test
    void paletteValuesAreUniqueAndWellFormed() {
        Set<Integer> seen = new HashSet<>();
        for (Palette p : Palette.values()) {
            assertTrue(seen.add(p.rgb()), "two palette entries share " + p.hex());
            assertTrue(p.hex().matches("#[0-9A-F]{6}"), p.hex());
            assertTrue(p.use().length() > 10, p.name() + " needs a description");
        }
    }

    @Test
    void paletteIsTheClassicChatColors() {
        // The old screens use ChatFormatting, so screens moved to the palette must look the same.
        assertEquals(TextColor.fromLegacyFormat(ChatFormatting.GOLD).getValue(), Palette.TITLE.rgb());
        assertEquals(TextColor.fromLegacyFormat(ChatFormatting.WHITE).getValue(), Palette.BODY.rgb());
        assertEquals(TextColor.fromLegacyFormat(ChatFormatting.GRAY).getValue(), Palette.MUTED.rgb());
        assertEquals(TextColor.fromLegacyFormat(ChatFormatting.DARK_GRAY).getValue(), Palette.FAINT.rgb());
        assertEquals(TextColor.fromLegacyFormat(ChatFormatting.GREEN).getValue(), Palette.GOOD.rgb());
        assertEquals(TextColor.fromLegacyFormat(ChatFormatting.YELLOW).getValue(), Palette.WARN.rgb());
        assertEquals(TextColor.fromLegacyFormat(ChatFormatting.RED).getValue(), Palette.BAD.rgb());
        assertEquals(TextColor.fromLegacyFormat(ChatFormatting.AQUA).getValue(), Palette.ACCENT.rgb());
    }

    @Test
    void paletteStyles() {
        assertEquals(Palette.GOOD.rgb(), Palette.GOOD.style().getColor().getValue());
        assertTrue(Palette.GOOD.bold().isBold());
        assertFalse(Palette.GOOD.style().isBold());
    }

    @Test
    void iconAndSoundIdsAreWellFormed() {
        Set<String> roles = new HashSet<>();
        for (Icon icon : Icon.values()) {
            assertTrue(icon.itemId().matches("minecraft:[a-z0-9_]+"), icon.itemId());
            assertTrue(roles.add(icon.name()));
            assertTrue(icon.use().length() > 5);
        }
        for (UiSound sound : UiSound.values()) {
            assertTrue(sound.soundId().matches("minecraft:[a-z0-9_.]+"), sound.soundId());
            assertTrue(sound.volume() > 0f && sound.volume() <= 1f);
            assertTrue(sound.pitch() >= 0.5f && sound.pitch() <= 2f);
        }
    }
}
