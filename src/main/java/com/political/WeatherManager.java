package com.political;


import com.political.government.PerkManager;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.WeatherData;

public class WeatherManager {

    private static int tickCounter = 0;

    public static void tick(MinecraftServer server) {
        tickCounter++;

        if (tickCounter >= 100) {
            tickCounter = 0;

            if (PerkManager.hasActivePerk("ETERNAL_FOG")) {
                for (ServerLevel world : server.getAllLevels()) {
                    if (world.dimension() == Level.OVERWORLD) {
                        if (!world.isRaining()) {
                            WeatherData wd = world.getWeatherData();
                            wd.setRaining(true);
                            wd.setThundering(true);
                            wd.setRainTime(24000);
                            wd.setClearWeatherTime(0);
                        }
                        Holder<WorldClock> overworldClock = world.registryAccess()
                                .lookupOrThrow(Registries.WORLD_CLOCK)
                                .getOrThrow(WorldClocks.OVERWORLD);
                        ServerClockManager cm = world.clockManager();
                        long totalTicks = cm.getInstance(overworldClock).totalTicks();
                        long timeOfDay = totalTicks % 24000;
                        if (timeOfDay < 13000 || timeOfDay > 23000) {
                            long dayBase = (totalTicks / 24000) * 24000;
                            cm.setTotalTicks(overworldClock, dayBase + 18000);
                        }
                    }
                }
            }

            // NIGHT_OWL_POLICY longer nights: PerkManager.tickOverworldClockPerks
        }
    }
}
