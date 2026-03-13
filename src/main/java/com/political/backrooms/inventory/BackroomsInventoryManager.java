package com.political.backrooms.inventory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.political.PoliticalServer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Stores the player's overworld inventory and XP when entering the Backrooms,
 * and restores them when leaving.
 * Data is persisted to world/data/backrooms_inventory_<uuid>.json.
 */
public class BackroomsInventoryManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<UUID, SavedInventory> storedInventories = new HashMap<>();

    public static boolean hasStoredInventory(UUID playerUUID) {
        return storedInventories.containsKey(playerUUID);
    }

    /**
     * Strips the player's inventory, stores it to disk, and clears their XP.
     * Called when a player enters a Backrooms dimension.
     */
    public static void storeAndClearInventory(ServerPlayerEntity player, MinecraftServer server) {
        UUID uuid = player.getUuid();
        if (storedInventories.containsKey(uuid)) return;

        RegistryOps<NbtElement> ops = server.getRegistryManager().getOps(NbtOps.INSTANCE);
        SavedInventory save = new SavedInventory();
        save.xpLevel = player.experienceLevel;
        save.xpProgress = player.experienceProgress;

        List<SlotData> slots = new ArrayList<>();
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty()) {
                final int slotIdx = i;
                ItemStack.CODEC.encodeStart(ops, stack)
                    .result()
                    .ifPresent(nbt -> slots.add(new SlotData(slotIdx, nbt.toString())));
            }
        }
        save.slots = slots;

        storedInventories.put(uuid, save);
        saveToFile(server, uuid, save);

        player.getInventory().clear();
        player.setExperienceLevel(0);
        player.setExperiencePoints(0);

        player.sendMessage(net.minecraft.text.Text.literal(
            "⚠ Your inventory has been stored. Survive the Backrooms to reclaim it.")
            .formatted(net.minecraft.util.Formatting.YELLOW), false);
    }

    /**
     * Restores the stored inventory and XP to the player.
     * Called when a player exits a Backrooms dimension.
     */
    public static void restoreInventory(ServerPlayerEntity player, MinecraftServer server) {
        UUID uuid = player.getUuid();
        SavedInventory save = storedInventories.get(uuid);
        if (save == null) {
            save = loadFromFile(server, uuid);
        }
        if (save == null) return;

        RegistryOps<NbtElement> ops = server.getRegistryManager().getOps(NbtOps.INSTANCE);
        player.setExperienceLevel(save.xpLevel);
        player.setExperiencePoints(0);
        player.experienceProgress = save.xpProgress; // restore fractional XP bar progress

        for (SlotData slotData : save.slots) {
            try {
                NbtCompound parsed = StringNbtReader.readCompound(slotData.nbt);
                ItemStack stack = ItemStack.CODEC.parse(ops, parsed).result().orElse(ItemStack.EMPTY);
                if (!stack.isEmpty()) {
                    player.getInventory().setStack(slotData.index, stack);
                }
            } catch (Exception e) {
                PoliticalServer.LOGGER.warn("Failed to restore inventory slot {}: {}", slotData.index, e.getMessage());
            }
        }

        storedInventories.remove(uuid);
        deleteFile(server, uuid);

        player.sendMessage(net.minecraft.text.Text.literal("✓ Your inventory has been restored.")
            .formatted(net.minecraft.util.Formatting.GREEN), false);
    }

    private static Path getFilePath(MinecraftServer server, UUID uuid) {
        return server.getSavePath(WorldSavePath.ROOT).resolve("data")
            .resolve("backrooms_inventory_" + uuid + ".json");
    }

    private static void saveToFile(MinecraftServer server, UUID uuid, SavedInventory save) {
        Path path = getFilePath(server, uuid);
        try {
            Files.createDirectories(path.getParent());
            try (Writer w = Files.newBufferedWriter(path)) {
                GSON.toJson(save, w);
            }
        } catch (IOException e) {
            PoliticalServer.LOGGER.error("Failed to save backrooms inventory for {}: {}", uuid, e.getMessage());
        }
    }

    private static SavedInventory loadFromFile(MinecraftServer server, UUID uuid) {
        Path path = getFilePath(server, uuid);
        if (!Files.exists(path)) return null;
        try (Reader r = Files.newBufferedReader(path)) {
            return GSON.fromJson(r, SavedInventory.class);
        } catch (IOException e) {
            PoliticalServer.LOGGER.error("Failed to load backrooms inventory for {}: {}", uuid, e.getMessage());
            return null;
        }
    }

    private static void deleteFile(MinecraftServer server, UUID uuid) {
        try {
            Files.deleteIfExists(getFilePath(server, uuid));
        } catch (IOException e) {
            PoliticalServer.LOGGER.warn("Failed to delete backrooms inventory file for {}", uuid);
        }
    }

    private static class SavedInventory {
        int xpLevel;
        float xpProgress;
        List<SlotData> slots = new ArrayList<>();
    }

    private static class SlotData {
        int index;
        String nbt;
        SlotData(int index, String nbt) {
            this.index = index;
            this.nbt = nbt;
        }
    }
}
