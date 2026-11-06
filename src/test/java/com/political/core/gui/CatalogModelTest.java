package com.political.core.gui;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CatalogModelTest {
    @Test void categoryCardsHavePairedNameplatesAndNeverCoverNavigation() {
        Set<Integer> slots = new HashSet<>();
        for (int i = 0; i < CatalogModel.CARDS_PER_PAGE; i++) {
            CatalogModel.Card card = CatalogModel.card(i, 17);
            assertEquals(card.iconSlot() + 9, card.labelSlot());
            for (int slot : new int[]{card.iconSlot(), card.labelSlot()}) {
                assertTrue(slots.add(slot), "overlapping card");
                assertFalse(Layout.isEdge(slot, 54));
                assertTrue(slot > 8 && slot < 45);
            }
        }
        assertEquals(16, slots.size());
    }

    @Test void categoryBadgeIsAValidStackEvenForAnEmptyOrLargeCategory() {
        assertEquals(1, CatalogModel.card(0, 0).badgeCount());
        assertEquals(23, CatalogModel.card(0, 23).badgeCount());
        assertEquals(64, CatalogModel.card(0, 1000).badgeCount());
        assertThrows(IllegalArgumentException.class, () -> CatalogModel.card(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> CatalogModel.card(8, 0));
        assertThrows(IllegalArgumentException.class, () -> CatalogModel.card(0, -1));
    }

    @Test void itemGridHasTwentyUniqueInteriorSlotsAndUsesEveryContentRow() {
        Set<Integer> slots = new HashSet<>();
        Set<Integer> rows = new HashSet<>();
        for (int slot : CatalogModel.itemSlots()) {
            assertTrue(slots.add(slot));
            assertFalse(Layout.isEdge(slot, 54));
            rows.add(slot / 9);
        }
        assertEquals(20, slots.size());
        assertEquals(Set.of(1, 2, 3, 4), rows);
    }

    @Test void everyEntryIsReachableExactlyOnceIncludingTheFinalPartialPage() {
        for (int perPage : new int[]{CatalogModel.CARDS_PER_PAGE, CatalogModel.ITEMS_PER_PAGE}) {
            for (int count : new int[]{0, 1, 8, 20, 21, 29, 241}) {
                Set<Integer> visited = new HashSet<>();
                int pages = Pager.pageCount(count, perPage);
                for (int page = 0; page < pages; page++) {
                    int start = Pager.firstIndex(page, perPage);
                    for (int i = start; i < Math.min(count, start + perPage); i++) assertTrue(visited.add(i));
                }
                assertEquals(count, visited.size());
                assertEquals(0, Pager.clamp(-20, pages));
                assertEquals(pages - 1, Pager.clamp(999, pages));
            }
        }
    }

    @Test void searchIgnoresCaseFormattingWhitespaceAndWordOrder() {
        assertTrue(CatalogModel.matches("§6Dragon Chestplate", "  CHEST   dragon "));
        assertTrue(CatalogModel.matches("Emerald Helmet", "mer helm"));
        assertFalse(CatalogModel.matches("Emerald Helmet", "emerald boots"));
        assertFalse(CatalogModel.matches("Emerald Helmet", "netherite"));
        assertTrue(CatalogModel.matches("Emerald Helmet", " "));
    }

    @Test void searchDoesNotDependOnTheServersLocale() {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertTrue(CatalogModel.matches("PIGLIN ITEMS", "piglin"));
        } finally {
            Locale.setDefault(previous);
        }
    }
}
