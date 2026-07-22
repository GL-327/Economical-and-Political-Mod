package com.political;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

/** Mojmap: item numeric ids are not used for persistence; resolve string ids via registry keys. */
public final class ItemRegistryIds {
    private ItemRegistryIds() {}

    public static String asString(Item item) {
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        return id != null ? id.toString() : "minecraft:air";
    }

    public static Identifier asId(Item item) {
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        return id != null ? id : Identifier.parse("minecraft:air");
    }
}
