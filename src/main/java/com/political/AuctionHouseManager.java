package com.political;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AuctionHouseManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static List<AuctionListing> listings = new ArrayList<>();

    public static class AuctionListing {
        public String sellerUuid;
        public String sellerName;
        public String itemData;
        public long price;
        public boolean priceInCoins;
        public long listingTime;
        public String id;

        public AuctionListing() {}

        // Legacy constructor (price in credits)
        public AuctionListing(String sellerUuid, String sellerName, ItemStack stack, int price) {
            this(sellerUuid, sellerName, stack, (long) price, false);
        }

        // New constructor with currency choice
        public AuctionListing(String sellerUuid, String sellerName, ItemStack stack, long price, boolean priceInCoins) {
            this.sellerUuid = sellerUuid;
            this.sellerName = sellerName;
            this.price = price;
            this.priceInCoins = priceInCoins;
            this.listingTime = System.currentTimeMillis();
            this.id = UUID.randomUUID().toString();

            if (PoliticalServer.server != null) {
                try {
                    RegistryOps<net.minecraft.nbt.NbtElement> ops = PoliticalServer.server.getRegistryManager().getOps(NbtOps.INSTANCE);
                    var result = ItemStack.CODEC.encodeStart(ops, stack);
                    result.result().ifPresent(nbt -> {
                        this.itemData = nbt.toString();
                    });
                } catch (Exception e) {
                    PoliticalServer.LOGGER.error("Failed to serialize item for auction", e);
                }
            }
        }

        public ItemStack getItemStack() {
            if (itemData == null || PoliticalServer.server == null) return ItemStack.EMPTY;
            try {
                NbtCompound parsed = StringNbtReader.readCompound(itemData);
                RegistryOps<net.minecraft.nbt.NbtElement> ops = PoliticalServer.server.getRegistryManager().getOps(NbtOps.INSTANCE);
                var result = ItemStack.CODEC.parse(ops, parsed);
                return result.result().orElse(ItemStack.EMPTY);
            } catch (Exception e) {
                PoliticalServer.LOGGER.error("Failed to deserialize item from auction", e);
                return ItemStack.EMPTY;
            }
        }
    }

    public static void load(MinecraftServer server) {
        Path path = getDataPath(server);
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                Type listType = new TypeToken<ArrayList<AuctionListing>>(){}.getType();
                listings = GSON.fromJson(reader, listType);
                if (listings == null) listings = new ArrayList<>();
                PoliticalServer.LOGGER.info("Loaded " + listings.size() + " auction listings");
            } catch (Exception e) {
                PoliticalServer.LOGGER.error("Failed to load auction house data", e);
                listings = new ArrayList<>();
            }
        } else {
            listings = new ArrayList<>();
        }
    }

    public static void save(MinecraftServer server) {
        if (server == null) return;
        Path path = getDataPath(server);
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(listings, writer);
            }
        } catch (Exception e) {
            PoliticalServer.LOGGER.error("Failed to save auction house data", e);
        }
    }

    private static Path getDataPath(MinecraftServer server) {
        return server.getSavePath(WorldSavePath.ROOT).resolve("auction_house_data.json");
    }

    public static List<AuctionListing> getListings() {
        return new ArrayList<>(listings);
    }

    public static void addListing(AuctionListing listing) {
        listings.add(listing);
        save(PoliticalServer.server);
    }

    public static boolean removeListing(String listingId) {
        boolean removed = listings.removeIf(l -> l.id.equals(listingId));
        if (removed) {
            save(PoliticalServer.server);
        }
        return removed;
    }

    public static AuctionListing getListing(String listingId) {
        for (AuctionListing listing : listings) {
            if (listing.id.equals(listingId)) {
                return listing;
            }
        }
        return null;
    }

    public static List<AuctionListing> getListingsBySeller(String sellerUuid) {
        List<AuctionListing> result = new ArrayList<>();
        for (AuctionListing listing : listings) {
            if (listing.sellerUuid.equals(sellerUuid)) {
                result.add(listing);
            }
        }
        return result;
    }

    public static int getListingCount() {
        return listings.size();
    }

    public static int getListingCountBySeller(String sellerUuid) {
        int count = 0;
        for (AuctionListing listing : listings) {
            if (listing.sellerUuid.equals(sellerUuid)) {
                count++;
            }
        }
        return count;
    }

    // Tax calculation - delegates to AuctionHouseGui
    public static long calculateListingTax(long price) {
        return AuctionHouseGui.calculateListingTax(price);
    }

    public static long calculatePurchaseTax(long price) {
        return AuctionHouseGui.calculatePurchaseTax(price);
    }

    // Clean up expired listings (call periodically if you want auto-expiry)
    public static void cleanupExpiredListings(long maxAgeMs) {
        long now = System.currentTimeMillis();
        boolean removed = listings.removeIf(listing -> (now - listing.listingTime) > maxAgeMs);
        if (removed) {
            save(PoliticalServer.server);
            PoliticalServer.LOGGER.info("Cleaned up expired auction listings");
        }
    }
}