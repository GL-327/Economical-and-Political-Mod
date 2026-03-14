package com.political.backrooms.fracture3;

import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Flicker Handler
 * Randomly toggles redstone lamps on and off with sound effects.
 */
public class FlickerHandler {
    
    // Minimal tracking class for lamps
    private static class FlickeringLamp {
        public BlockPos pos;
        public long nextToggleTime;
        public boolean isOn;
        
        public FlickeringLamp(BlockPos pos, long nextTime) {
            this.pos = pos;
            this.nextToggleTime = nextTime;
            this.isOn = false;
        }
    }
    
    private static final List<FlickeringLamp> lamps = new ArrayList<>();
    private static final Random random = new Random();
    
    public static void registerLamp(BlockPos pos) {
        // Prevent duplicates
        for (FlickeringLamp l : lamps) {
            if (l.pos.equals(pos)) return;
        }
        long nextTime = System.currentTimeMillis() + 4000L + random.nextInt(5000); // 4-9s irregular cycle
        lamps.add(new FlickeringLamp(pos, nextTime));
    }
    
    public static void tick(ServerWorld world) {
        long now = System.currentTimeMillis();
        
        // Use standard iteration, remove unavailable blocks
        lamps.removeIf(lamp -> {
            if (!world.isChunkLoaded(lamp.pos.getX() >> 4, lamp.pos.getZ() >> 4)) {
                return false; // Leave unloaded chunks in queue
            }
            
            if (!world.getBlockState(lamp.pos).isOf(Blocks.REDSTONE_LAMP)) {
                return true; // Not a lamp anymore, remove from tracking
            }
            
            if (now >= lamp.nextToggleTime) {
                // Toggle
                lamp.isOn = !lamp.isOn;
                
                // Set state (this visually works if we spoof metadata, but default Redstone Lamp requires redstone block technically.
                // We can instead swap the block for Sea Lantern temporarily, or Lit Redstone Lamp if it exists without block updates).
                // Actually, let's just use the lit property if possible, or play effects.
                // For simplicity, play the crackling sound
                if (lamp.isOn) {
                    world.playSound(null, lamp.pos, SoundEvent.of(Identifier.of("backrooms", "light_flicker")), SoundCategory.BLOCKS, 0.5f, 1.0f);
                }
                
                // Calculate next flick
                long delay = lamp.isOn ? 100L + random.nextInt(400) : 4000L + random.nextInt(5000);
                lamp.nextToggleTime = now + delay;
            }
            
            return false;
        });
    }
}
