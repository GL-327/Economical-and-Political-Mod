package com.political.backrooms;

import com.political.PoliticalServer;
import com.political.backrooms.item.AlmondWaterItem;
import com.political.backrooms.item.DeterrentTorchItem;
import com.political.backrooms.item.EnergyDrinkItem;
import com.political.backrooms.item.InfusedEchoShardItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class BackroomsItems {

    public static final Item INFUSED_ECHO_SHARD = new InfusedEchoShardItem(
        new Item.Settings().maxCount(16));

    public static final Item ALMOND_WATER = new AlmondWaterItem(
        new Item.Settings().maxCount(16));

    public static final Item ENERGY_DRINK = new EnergyDrinkItem(
        new Item.Settings().maxCount(16));

    public static final Item DETERRENT_TORCH = new DeterrentTorchItem(
        new Item.Settings().maxCount(64));

    public static void register() {
        Registry.register(Registries.ITEM,
            Identifier.of(PoliticalServer.MOD_ID, "infused_echo_shard"), INFUSED_ECHO_SHARD);
        Registry.register(Registries.ITEM,
            Identifier.of(PoliticalServer.MOD_ID, "almond_water"), ALMOND_WATER);
        Registry.register(Registries.ITEM,
            Identifier.of(PoliticalServer.MOD_ID, "energy_drink"), ENERGY_DRINK);
        Registry.register(Registries.ITEM,
            Identifier.of(PoliticalServer.MOD_ID, "deterrent_torch"), DETERRENT_TORCH);
    }
}
