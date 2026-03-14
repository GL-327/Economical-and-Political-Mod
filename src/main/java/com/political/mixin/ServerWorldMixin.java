package com.political.mixin;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        ServerWorld world = (ServerWorld) (Object) this;
        RegistryKey<World> worldKey = world.getRegistryKey();
        
        // If this is the Fracture 3 dimension, swap the generator
        if (worldKey.getValue().getNamespace().equals("politicalserver") && worldKey.getValue().getPath().equals("fracture_3")) {
            try {
                var chunkManager = world.getChunkManager();
                var currentGenerator = chunkManager.getChunkGenerator();
                BiomeSource bs = currentGenerator.getBiomeSource();
                
                // Use reflection to bypass compiler type-checking issues with Fracture3ChunkGenerator
                Class<?> genClass = Class.forName("com.political.backrooms.fracture3.Fracture3ChunkGenerator");
                java.lang.reflect.Constructor<?> ctor = genClass.getConstructor(BiomeSource.class);
                Object customGenerator = ctor.newInstance(bs);
                
                // Swap the generator in the ChunkManager via Reflection
                java.lang.reflect.Field generatorField = net.minecraft.server.world.ServerChunkManager.class.getDeclaredField("chunkGenerator");
                generatorField.setAccessible(true);
                generatorField.set(chunkManager, customGenerator);
                
                System.out.println("[PoliticalServer] Swapped generator for Fracture 3 to custom Backrooms generator via Reflection.");
            } catch (Exception e) {
                System.err.println("[PoliticalServer] Failed to swap generator for Fracture 3: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
