package com.political.core.gui;

/**
 * A fraction as a row of cells plus a percentage. The percentage is always shown, because a bar read only
 * by its color or length is not accessible (STYLE.md).
 */
public final class ProgressBar {

    public static final int DEFAULT_CELLS = 10;
    private static final char CELL = '|';

    private ProgressBar() {
    }

    /** Fraction clamped to 0 .. 1; anything that is not a number counts as 0. */
    public static double clamp(double fraction) {
        if (Double.isNaN(fraction)) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, fraction));
    }

    /** How many of {@code cells} are filled: the fraction rounded to the nearest cell. */
    public static int filledCells(double fraction, int cells) {
        return (int) Math.round(clamp(fraction) * cells);
    }

    public static int percent(double fraction) {
        return (int) Math.round(clamp(fraction) * 100.0);
    }

    /** The filled part, as text. */
    public static String filled(double fraction, int cells) {
        return String.valueOf(CELL).repeat(filledCells(fraction, cells));
    }

    /** The empty part, as text. */
    public static String empty(double fraction, int cells) {
        return String.valueOf(CELL).repeat(cells - filledCells(fraction, cells));
    }
}
