package com.political.items;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** The eight original dragon chestplates. */
public final class DragonChestplateRegistry {

    private static final List<String> IDS = List.of(
            "inferno_dragon_chestplate",
            "storm_dragon_chestplate",
            "void_dragon_chestplate",
            "aurora_dragon_chestplate",
            "frost_dragon_chestplate",
            "venom_dragon_chestplate",
            "blood_dragon_chestplate",
            "eclipse_dragon_chestplate"
    );
    private static final Set<String> ID_SET = Set.copyOf(IDS);

    private DragonChestplateRegistry() {}

    public static boolean isRegistered(String customItemId) {
        return customItemId != null && ID_SET.contains(customItemId);
    }

    public static boolean isDragonChestplateStack(ItemStack chest) {
        return isRegistered(DragonChestplates.getCustomItemId(chest));
    }

    public static List<String> allIds() {
        return IDS;
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
        return out;
    }
}
