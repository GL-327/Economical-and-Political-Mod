package com.political;

import com.political.backrooms.entities.HoundEntity;
import com.political.backrooms.entities.SmilerEntity;
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

    public static final RegistryKey<EntityType<?>> SMILER_KEY = RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of("political", "smiler"));
    public static final RegistryKey<EntityType<?>> HOUND_KEY = RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of("political", "hound"));

    public static final EntityType<SmilerEntity> SMILER = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of("political", "smiler"),
            EntityType.Builder.create(SmilerEntity::new, SpawnGroup.MONSTER)
                    .dimensions(0.6f, 1.95f)
                    .build(SMILER_KEY)
    );

    public static final EntityType<HoundEntity> HOUND = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of("political", "hound"),
            EntityType.Builder.create(HoundEntity::new, SpawnGroup.MONSTER)
                    .dimensions(0.6f, 0.85f)
                    .build(HOUND_KEY)
    );

    public static void register() {
        FabricDefaultAttributeRegistry.register(SMILER, SmilerEntity.createSmilerAttributes());
        FabricDefaultAttributeRegistry.register(HOUND, HoundEntity.createHoundAttributes());
    }
}
