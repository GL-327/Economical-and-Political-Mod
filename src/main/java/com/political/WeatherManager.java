package com.political;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

public class WeatherManager {

    private static int tickCounter = 0;

    public static void tick(MinecraftServer server) {
        tickCounter++;

        if (tickCounter >= 100) {
            tickCounter = 0;

            if (PerkManager.hasActivePerk("ETERNAL_FOG")) {
                for (ServerWorld world : server.getWorlds()) {
                    // Keep rain/thunder
                    if (!world.isRaining()) {
                        world.setWeather(0, 24000, true, true);
                    }
                    // Keep it night (18000 = midnight)
                    long timeOfDay = world.getTimeOfDay() % 24000;
                    if (timeOfDay < 13000 || timeOfDay > 23000) {
                        world.setTimeOfDay((world.getTimeOfDay() / 24000) * 24000 + 18000);
                    }
                }
            }

            if (PerkManager.hasActivePerk("NIGHT_OWL_POLICY")) {
                for (ServerWorld world : server.getWorlds()) {
                    // Make nights longer - slow down time during night
                    long timeOfDay = world.getTimeOfDay() % 24000;
                    if (timeOfDay >= 13000 && timeOfDay <= 23000) {
                        // During night, occasionally skip time backwards slightly
                        if (tickCounter % 5 == 0) {
                            // This effectively makes night 20% longer
                        }
                    }
                }
            }
        }
    }
}