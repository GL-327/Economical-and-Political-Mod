package com.political.mixin;

import com.political.backrooms.BackroomsDimensionManager;
import com.political.backrooms.fracture3.Fracture3ChunkGenerator;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.Executor;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World {

    protected ServerWorldMixin(RegistryKey<World> key, RegistryEntry<DimensionType> dimensionTypeEntry, Profiler profiler, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates) {
        super(key, dimensionTypeEntry, profiler, isClient, debugWorld, seed, maxChainedNeighborUpdates);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, DimensionOptions dimensionOptions, RegistryKey<World> worldKey, RegistryEntry<DimensionType> dimensionTypeEntry, boolean debugWorld, long seed, java.util.List<net.minecraft.server.world.ServerWorld.WorldHandler> spoutWorldHandlers, boolean shouldTickTime, net.minecraft.world.RandomSequences randomSequences, CallbackInfo ci) {
        ServerWorld world = (ServerWorld) (Object) this;
        
        // If this is the Fracture 3 dimension, swap the generator
        if (worldKey.getValue().getNamespace().equals("politicalserver") && worldKey.getValue().getPath().equals("fracture_3")) {
            try {
                // Get the biome source from the existing generator or use the void biome
                var chunkManager = world.getChunkManager();
                var currentGenerator = chunkManager.getChunkGenerator();
                
                // Construct our custom generator
                Fracture3ChunkGenerator customGenerator = new Fracture3ChunkGenerator(currentGenerator.getBiomeSource());
                
                // Swap the generator in the ChunkManager via Reflection (since it's typically final/protected)
                java.lang.reflect.Field generatorField = net.minecraft.server.world.ServerChunkManager.class.getDeclaredField("chunkGenerator");
                generatorField.setAccessible(true);
                generatorField.set(chunkManager, customGenerator);
                
                System.out.println("[PoliticalServer] Swapped generator for Fracture 3 to custom Backrooms generator.");
            } catch (Exception e) {
                System.err.println("[PoliticalServer] Failed to swap generator for Fracture 3: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
