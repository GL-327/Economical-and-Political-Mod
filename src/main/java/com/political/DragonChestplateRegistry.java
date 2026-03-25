package com.political;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * All dragon chestplate custom_item_id values (bounty defence, menus, abilities).
 */
public final class DragonChestplateRegistry {

    private static final List<String> IDS = new ArrayList<>();
    private static final Set<String> ID_SET;

    static {
        Collections.addAll(IDS,
                "inferno_dragon_chestplate",
                "storm_dragon_chestplate",
                "void_dragon_chestplate",
                "aurora_dragon_chestplate",
                "frost_dragon_chestplate",
                "venom_dragon_chestplate",
                "blood_dragon_chestplate",
                "eclipse_dragon_chestplate",
                // Extended set (15)
                "dune_dragon_chestplate",
                "reef_dragon_chestplate",
                "ember_dragon_chestplate",
                "crystal_dragon_chestplate",
                "shadow_dragon_chestplate",
                "solar_dragon_chestplate",
                "lunar_dragon_chestplate",
                "terra_dragon_chestplate",
                "tempest_dragon_chestplate",
                "auric_dragon_chestplate",
                "glacier_dragon_chestplate",
                "mire_dragon_chestplate",
                "starfall_dragon_chestplate",
                "mirage_dragon_chestplate",
                "obsidian_wyrm_chestplate");

        // New set (35) - generated for easier maintenance.
        for (int i = 1; i <= 95; i++) {
            IDS.add(String.format("new_dragon_chestplate_%02d", i));
        }
        // Omega set (3)
        for (int i = 1; i <= 3; i++) {
            IDS.add("omega_dragon_chestplate_" + i);
        }
        ID_SET = Set.copyOf(IDS);
    }

    private DragonChestplateRegistry() {
    }

    public static boolean isRegistered(String customItemId) {
        return customItemId != null && ID_SET.contains(customItemId);
    }

    public static boolean isDragonChestplateStack(ItemStack chest) {
        String id = DragonChestplates.getCustomItemId(chest);
        return isRegistered(id);
    }

    /** Ordered list for GUIs (stable order). */
    public static List<String> allIds() {
        return List.copyOf(IDS);
    }

    public static List<ItemStack> allStacks() {
        List<ItemStack> out = new ArrayList<>();
        out.add(DragonChestplates.createDragonChestplate1());
        out.add(DragonChestplates.createDragonChestplate2());
        out.add(DragonChestplates.createDragonChestplate3());
        out.add(DragonChestplates.createDragonChestplateAurora());
        out.add(DragonChestplates.createDragonChestplateFrost());
        out.add(DragonChestplates.createDragonChestplateVenom());
        out.add(DragonChestplates.createDragonChestplateBlood());
        out.add(DragonChestplates.createDragonChestplateEclipse());
        out.add(MoreDragonChestplates.createDune());
        out.add(MoreDragonChestplates.createReef());
        out.add(MoreDragonChestplates.createEmber());
        out.add(MoreDragonChestplates.createCrystal());
        out.add(MoreDragonChestplates.createShadow());
        out.add(MoreDragonChestplates.createSolar());
        out.add(MoreDragonChestplates.createLunar());
        out.add(MoreDragonChestplates.createTerra());
        out.add(MoreDragonChestplates.createTempest());
        out.add(MoreDragonChestplates.createAuric());
        out.add(MoreDragonChestplates.createGlacier());
        out.add(MoreDragonChestplates.createMire());
        out.add(MoreDragonChestplates.createStarfall());
        out.add(MoreDragonChestplates.createMirage());
        out.add(MoreDragonChestplates.createObsidianWyrm());

        for (int i = 1; i <= 95; i++) {
            out.add(MoreDragonChestplates.createNewDragonChestplate(i));
        }
        for (int i = 1; i <= 3; i++) {
            out.add(MoreDragonChestplates.createOmegaDragonChestplate(i));
        }
        return out;
    }
}
