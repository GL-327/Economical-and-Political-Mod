package com.political;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.political.bounty.SlayerItems;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Sell/buy overrides and enable flags for Skyblock-style enchanted block gear (custom id suffix {@code _sbl_ench}).
 */
public final class EnchantedShopManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnchantedShopManager.class);


    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("political").resolve("enchanted_shop.json");

    private static final Map<String, Integer> sellOverrides = new HashMap<>();
    private static final Set<String> disabled = new HashSet<>();

    static {
        load();
    }

    private EnchantedShopManager() {}

    public static void load() {
        sellOverrides.clear();
        disabled.clear();
        try {
            if (!Files.exists(FILE)) {
                return;
            }
            try (Reader r = Files.newBufferedReader(FILE)) {
                Data d = GSON.fromJson(r, Data.class);
                if (d == null) return;
                if (d.sellOverrides != null) {
                    sellOverrides.putAll(d.sellOverrides);
                }
                if (d.disabledIds != null) {
                    disabled.addAll(d.disabledIds);
                }
            }
        } catch (Exception e) {
            LOGGER.error("load failed", e);
        }
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Data d = new Data();
            d.sellOverrides = new HashMap<>(sellOverrides);
            d.disabledIds = new HashSet<>(disabled);
            try (Writer w = Files.newBufferedWriter(FILE)) {
                GSON.toJson(d, w);
            }
        } catch (Exception e) {
            LOGGER.error("save failed", e);
        }
    }

    public static boolean isDisabled(String customId) {
        return customId != null && disabled.contains(customId);
    }

    public static void setDisabled(String customId, boolean off) {
        if (customId == null) return;
        if (off) {
            disabled.add(customId);
        } else {
            disabled.remove(customId);
        }
        save();
    }

    public static void setSellOverride(String customId, int sellPrice) {
        if (customId == null) return;
        if (sellPrice <= 0) {
            sellOverrides.remove(customId);
        } else {
            sellOverrides.put(customId, sellPrice);
        }
        save();
    }

    public static int getSellOverrideOrZero(String customId) {
        return sellOverrides.getOrDefault(customId, 0);
    }

    /** Default sell value from ingredient costs + 160 lapis (Skyblock grid), then overrides. */
    public static int getSellPrice(ItemStack enchanted) {
        String id = SlayerItems.getCustomItemId(enchanted);
        if (id == null || !id.endsWith("_sbl_ench")) {
            return 0;
        }
        if (sellOverrides.containsKey(id)) {
            return Math.max(1, sellOverrides.get(id));
        }
        return 0;
    }

    public static int getBuyPrice(ItemStack enchanted) {
        int s = getSellPrice(enchanted);
        return s > 0 ? s * 10 : 0;
    }

    public static List<ItemStack> allEnchantedBlockStacks() {
        List<ItemStack> list = new java.util.ArrayList<>();
        return list;
    }

    private static final class Data {
        Map<String, Integer> sellOverrides;
        Set<String> disabledIds;
    }
}
