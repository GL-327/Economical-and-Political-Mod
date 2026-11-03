package com.political.core.gui;

/** Page arithmetic for lists that are longer than a screen. Pages count from 0. */
public final class Pager {

    private Pager() {
    }

    /** At least one page, even for an empty list, so the counter never reads "page 1 of 0". */
    public static int pageCount(int total, int perPage) {
        if (perPage < 1) {
            throw new IllegalArgumentException("perPage must be at least 1, got " + perPage);
        }
        return Math.max(1, (int) Math.ceil(Math.max(0, total) / (double) perPage));
    }

    /** Keeps a page number inside 0 .. pages-1, for example after the list shrank. */
    public static int clamp(int page, int pages) {
        return Math.max(0, Math.min(page, pages - 1));
    }

    /** Index in the list of the first entry on a page. */
    public static int firstIndex(int page, int perPage) {
        return page * perPage;
    }

    public static boolean hasPrevious(int page) {
        return page > 0;
    }

    public static boolean hasNext(int page, int pages) {
        return page < pages - 1;
    }
}
