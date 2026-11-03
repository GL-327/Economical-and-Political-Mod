package com.political.core.gui;

/**
 * The slot grid every chest screen follows (STYLE.md). All of it is arithmetic on slot numbers, so it can
 * be tested without a server.
 *
 * <pre>
 *  0  1  2  3  H  5  6  7  8      row 0: frame edge, header in the middle (slot 4)
 *  9 10 11 12 13 14 15 16 17      rows 1 .. n-2: frame edge on both sides, content in columns 1..7
 *  ...
 *  B  .  .  P  I  N  .  .  C      last row: Back, Previous, page Info, Next, Close
 * </pre>
 */
public final class Layout {

    public static final int COLUMNS = 9;
    public static final int MIN_ROWS = 1;
    public static final int MAX_ROWS = 6;

    /** The header (title icon) sits in the middle of the top row. */
    public static final int HEADER_SLOT = 4;

    /** Confirmation screens are three rows: what it is about, then yes on the left and no on the right. */
    public static final int CONFIRM_ROWS = 3;
    public static final int CONFIRM_INFO_SLOT = 4;
    public static final int CONFIRM_YES_SLOT = 11;
    public static final int CONFIRM_NO_SLOT = 15;

    private Layout() {
    }

    public static int size(int rows) {
        if (rows < MIN_ROWS || rows > MAX_ROWS) {
            throw new IllegalArgumentException("a chest screen has 1 to 6 rows, got " + rows);
        }
        return rows * COLUMNS;
    }

    public static int rows(int size) {
        if (size % COLUMNS != 0 || size < COLUMNS || size > MAX_ROWS * COLUMNS) {
            throw new IllegalArgumentException("not a chest size: " + size);
        }
        return size / COLUMNS;
    }

    /** True for the outer ring of the screen. */
    public static boolean isEdge(int slot, int size) {
        int rows = rows(size);
        int row = slot / COLUMNS;
        int col = slot % COLUMNS;
        return row == 0 || row == rows - 1 || col == 0 || col == COLUMNS - 1;
    }

    /** Back sits at the left of the bottom row. */
    public static int backSlot(int size) {
        rows(size);
        return size - COLUMNS;
    }

    /** Close sits at the right of the bottom row. */
    public static int closeSlot(int size) {
        rows(size);
        return size - 1;
    }

    public static int previousPageSlot(int size) {
        return backSlot(size) + 3;
    }

    public static int pageInfoSlot(int size) {
        return backSlot(size) + 4;
    }

    public static int nextPageSlot(int size) {
        return backSlot(size) + 5;
    }

    /**
     * The slots a list of entries uses, in reading order: columns 1 to 7 of every row between the top
     * row and the bottom row. A six row screen has 28 of them.
     */
    public static int[] interior(int size) {
        int rows = rows(size);
        int innerRows = Math.max(0, rows - 2);
        int[] slots = new int[innerRows * (COLUMNS - 2)];
        int i = 0;
        for (int row = 1; row <= innerRows; row++) {
            for (int col = 1; col < COLUMNS - 1; col++) {
                slots[i++] = row * COLUMNS + col;
            }
        }
        return slots;
    }

    public static int perPage(int size) {
        return interior(size).length;
    }
}
