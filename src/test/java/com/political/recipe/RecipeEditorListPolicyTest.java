package com.political.recipe;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class RecipeEditorListPolicyTest {
    @Test void searchKeepsItsOriginalRecipeLookupAndEditOnlyClicks() {
        assertTrue(RecipeEditorGui.usesIndexedRecipeLookup(null));
    }

    @Test void oldToolAndBountySetListsKeepTheirEditOnlyClicks() {
        assertTrue(RecipeEditorGui.usesIndexedRecipeLookup("CUSTOM_TOOLS"));
        assertTrue(RecipeEditorGui.usesIndexedRecipeLookup("BOUNTY_SETS"));
    }

    @Test void ordinaryCategoriesKeepTheirExistingRightClickToggle() {
        assertFalse(RecipeEditorGui.usesIndexedRecipeLookup("GEM_BLOCK_GEAR"));
        assertFalse(RecipeEditorGui.usesIndexedRecipeLookup("SPECIAL_ITEMS"));
        assertFalse(RecipeEditorGui.usesIndexedRecipeLookup("MOB_DROPS"));
    }
}
