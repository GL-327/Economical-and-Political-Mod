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

    /** Each armor piece contributes this fraction of the stated bonus. (4 pieces = full bonus) */
    private static final double PER_PIECE_SCALE = 0.25;

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
                    ? 0.20 * nightstalkerPieces * PER_PIECE_SCALE  // +20%/piece in darkness
                    : -0.15 * nightstalkerPieces * PER_PIECE_SCALE; // -15%/piece in bright light
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
                double modifier = -0.20 * heavyweightPieces * PER_PIECE_SCALE;
                speedInst.addTemporaryModifier(new EntityAttributeModifier(
                    HEAVYWEIGHT_SPEED_ID, modifier, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
        }

        // VAMPIRIC: fire damage amplification is handled in BackroomsAttributeMixin.applyDamage()
        // TODO: Detect fire damage type and double it for VAMPIRIC wearers

        // FEATHERWEIGHT: no fall damage with 4 pieces
        // TODO: Implement via mixin to PlayerEntity's fall damage logic

        // --- GLUTTON: extra food effects ---
        int gluttonPieces = ArmorAttributeManager.countArmorWithAttribute(player, ArmorAttribute.GLUTTON);
        if (gluttonPieces > 0) {
            // Apply Saturation boost once per second
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 25, gluttonPieces - 1, false, false));
        }
    }
}
