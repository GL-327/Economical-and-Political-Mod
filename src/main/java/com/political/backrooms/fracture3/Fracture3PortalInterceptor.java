package com.political.backrooms.fracture3;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.random.Random;

/**
 * Fracture 3 Portal Interceptor
 * 
 * Intercepts portal travel to potentially send players to Fracture 3.
 * 
 * Entry chance:
 * - Normal: 1 in 1000 portal uses
 * - Holding Warden Core: 1 in 2 portal uses
 */
public class Fracture3PortalInterceptor {

    /**
     * Check if portal should be intercepted and send player to Fracture 3
     * @param player The player using the portal
     * @return true if portal was intercepted (cancel normal portal behavior)
     */
    public static boolean checkPortalInterception(ServerPlayerEntity player) {
        Random random = player.getEntityWorld().getRandom();
        int roll = random.nextInt(1000);
        
        // Check if player is holding Warden Core
        boolean holdingWardenCore = isHoldingWardenCore(player);
        
        if (holdingWardenCore) {
            // 1 in 2 chance (500 out of 1000)
            if (roll < 500) {
                interceptPortal(player);
                return true;
            }
        } else {
            // 1 in 1000 chance
            if (roll == 0) {
                interceptPortal(player);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if player is holding Warden Core in main or off hand
     */
    private static boolean isHoldingWardenCore(ServerPlayerEntity player) {
        ItemStack mainHand = player.getMainHandStack();
        ItemStack offHand = player.getOffHandStack();
        
        return isWardenCore(mainHand) || isWardenCore(offHand);
    }
    
    /**
     * Check if an item is a Warden Core
     */
    public static boolean isWardenCore(ItemStack stack) {
        if (stack.isEmpty()) return false;
        
        // Check for warden_core NBT tag via CUSTOM_DATA component
        NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData != null) {
            NbtCompound nbt = customData.copyNbt();
            if (nbt.contains("warden_core")) {
                return nbt.getByte("warden_core").orElse((byte) 0) != 0;
            }
        }
        
        // Check by item identifier (echo_shard is the base item)
        return stack.getItem().toString().contains("echo_shard");
    }
    
    /**
     * Intercept the portal and send player to Fracture 3
     */
    private static void interceptPortal(ServerPlayerEntity player) {
        // Play sound: backrooms:electrical_hum at player position, volume 0.8, pitch 0.9
        player.getEntityWorld().playSound(null, player.getBlockPos(), net.minecraft.sound.SoundEvent.of(net.minecraft.util.Identifier.of("backrooms", "electrical_hum")), net.minecraft.sound.SoundCategory.AMBIENT, 0.8f, 0.9f);
        
        // Apply Nausea II for 5 seconds on arrival
        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.NAUSEA, 100, 1, false, false, true));
        
        // Send action bar message: "" (empty - no message. silence is the point.)
        player.sendMessage(net.minecraft.text.Text.literal(""), true);
        
        // Screen fade to white over 1.5 seconds (TitleTimes packet: 10 in, 20 stay, 10 out)
        player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket(10, 20, 10)); 

        // Teleport to Fracture 3
        Fracture3DimensionManager.teleportToFracture3(player);
    }
}
