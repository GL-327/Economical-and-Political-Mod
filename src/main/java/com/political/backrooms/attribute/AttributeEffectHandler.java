package com.political.backrooms.attribute;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * Handles tick-based armor attribute effects for all online players.
 * Call {@link #onServerTick(MinecraftServer)} from the server tick event.
 */
public class AttributeEffectHandler {

    private static final Identifier NIGHTSTALKER_SPEED_ID =
        Identifier.of("politicalserver", "nightstalker_speed");
    private static final Identifier HEAVYWEIGHT_SPEED_ID =
        Identifier.of("politicalserver", "heavyweight_speed");

    public static void onServerTick(MinecraftServer server) {
        long tick = server.getTicks();
        // Process every 20 ticks (once per second) to reduce overhead
        if (tick % 20 != 0) return;

        for (ServerWorld world : server.getWorlds()) {
            List<ServerPlayerEntity> players = world.getPlayers();
            for (ServerPlayerEntity player : players) {
                processPlayer(player, world);
            }
        }
    }

    private static void processPlayer(ServerPlayerEntity player, ServerWorld world) {
        // --- NIGHTSTALKER ---
        int nightstalkerPieces = ArmorAttributeManager.countArmorWithAttribute(player, ArmorAttribute.NIGHTSTALKER);
        if (nightstalkerPieces > 0) {
            int lightLevel = world.getLightLevel(player.getBlockPos());
            EntityAttributeInstance speedInst = player.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
            if (speedInst != null) {
                speedInst.removeModifier(NIGHTSTALKER_SPEED_ID);
                double modifier = lightLevel <= 7
                    ? 0.20 * nightstalkerPieces * 0.3  // +20% per piece in dark (scaled)
                    : -0.15 * nightstalkerPieces * 0.3; // -15% per piece in bright light
                speedInst.addTemporaryModifier(new EntityAttributeModifier(
                    NIGHTSTALKER_SPEED_ID, modifier, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
        }

        // --- HEAVYWEIGHT ---
        int heavyweightPieces = ArmorAttributeManager.countArmorWithAttribute(player, ArmorAttribute.HEAVYWEIGHT);
        if (heavyweightPieces > 0) {
            EntityAttributeInstance speedInst = player.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
            if (speedInst != null) {
                speedInst.removeModifier(HEAVYWEIGHT_SPEED_ID);
                double modifier = -0.20 * heavyweightPieces * 0.3;
                speedInst.addTemporaryModifier(new EntityAttributeModifier(
                    HEAVYWEIGHT_SPEED_ID, modifier, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
        }

        // --- VAMPIRIC: fire damage double (2x fire dmg handled in mixin) ---
        // Status effects applied elsewhere

        // --- FEATHERWEIGHT: no fall damage with 4 pieces ---
        // TODO: override fall damage handling via mixin

        // --- GLUTTON: extra food effects ---
        int gluttonPieces = ArmorAttributeManager.countArmorWithAttribute(player, ArmorAttribute.GLUTTON);
        if (gluttonPieces > 0) {
            // Apply Saturation boost once per second
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 25, gluttonPieces - 1, false, false));
        }
    }
}
