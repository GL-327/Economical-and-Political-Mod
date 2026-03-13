package com.political.backrooms.fracture3;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fracture 3 Registry
 * 
 * Registers the chunk generator codec for the Fracture 3 dimension.
 */
public class Fracture3Registry {

    private static final Logger LOGGER = LoggerFactory.getLogger("politicalserver");

    public static void register() {
        // Register the chunk generator codec
        Registry.register(Registries.CHUNK_GENERATOR, 
            Identifier.of("politicalserver", "fracture_3_generator"),
            Fracture3ChunkGenerator.CODEC);
        
        LOGGER.info("Fracture 3 dimension registered");
    }
}
