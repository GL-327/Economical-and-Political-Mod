package com.political;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModEntities {

    public static final Identifier AUCTION_MASTER_ID = Identifier.of("political", "auction_master");

    public static void register() {
        // Registration happens in PoliticalServer.initializeServer()
        // This method exists so you can call ModEntities.register() from your main mod class
    }
}
