package com.political;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Player Shops System - Pillar 4
 * 
 * Allows players to create and manage their own shops.
 * Players can list items for sale, set prices, and collect earnings.
 * Shops can be accessed via commands or physical shop blocks.
 */
public class PlayerShopManager {

    // ════════════════════════════════════════════════════════════
    // SHOP DATA
    // ════════════════════════════════════════════════════════════
    
    public static class ShopListing {
        public String listingId;
        public String sellerUuid;
        public String sellerName;
        public String itemData;
        public int amount;
        public long pricePerItem;
        public long listingTime;
        public int stock;
        public boolean infinite; // For admin shops
        public String category;
        
        public ShopListing() {}
        
        public ShopListing(String sellerUuid, String sellerName, ItemStack stack, int amount, long pricePerItem) {
            this.listingId = UUID.randomUUID().toString();
            this.sellerUuid = sellerUuid;
            this.sellerName = sellerName;
            this.amount = amount;
            this.pricePerItem = pricePerItem;
            this.listingTime = System.currentTimeMillis();
            this.stock = amount;
            this.infinite = false;
            this.category = determineCategory(stack);
            
            // Serialize item
            if (PoliticalServer.server != null) {
                try {
                    RegistryOps<net.minecraft.nbt.Tag> ops = RegistryOps.create(NbtOps.INSTANCE, PoliticalServer.server.registryAccess());
                    var result = ItemStack.CODEC.encodeStart(ops, stack);
                    result.result().ifPresent(nbt -> {
                        this.itemData = nbt.toString();
                    });
                } catch (Exception e) {
                    PoliticalServer.LOGGER.error("Failed to serialize item for player shop", e);
                }
            }
        }
        
        public ItemStack getItemStack() {
            if (itemData == null || PoliticalServer.server == null) return ItemStack.EMPTY;
            try {
                CompoundTag parsed = net.minecraft.nbt.TagParser.parseCompoundFully(itemData);
                RegistryOps<net.minecraft.nbt.Tag> ops = RegistryOps.create(NbtOps.INSTANCE, PoliticalServer.server.registryAccess());
                var result = ItemStack.CODEC.parse(ops, parsed);
                return result.result().orElse(ItemStack.EMPTY);
            } catch (Exception e) {
                PoliticalServer.LOGGER.error("Failed to deserialize item from player shop", e);
                return ItemStack.EMPTY;
            }
        }
        
        private String determineCategory(ItemStack stack) {
            var item = stack.getItem();
            
            // Weapons - swords, axes, bows
            if (stack.is(Items.WOODEN_SWORD) || stack.is(Items.STONE_SWORD) ||
                stack.is(Items.IRON_SWORD) || stack.is(Items.GOLDEN_SWORD) ||
                stack.is(Items.DIAMOND_SWORD) || stack.is(Items.NETHERITE_SWORD) ||
                stack.is(Items.BOW) || stack.is(Items.CROSSBOW)) {
                return "WEAPONS";
            }
            
            // Axes as weapons
            if (stack.is(Items.WOODEN_AXE) || stack.is(Items.STONE_AXE) ||
                stack.is(Items.IRON_AXE) || stack.is(Items.GOLDEN_AXE) ||
                stack.is(Items.DIAMOND_AXE) || stack.is(Items.NETHERITE_AXE)) {
                return "WEAPONS";
            }
            
            // Armor
            if (stack.is(Items.LEATHER_HELMET) || stack.is(Items.LEATHER_CHESTPLATE) ||
                stack.is(Items.LEATHER_LEGGINGS) || stack.is(Items.LEATHER_BOOTS) ||
                stack.is(Items.CHAINMAIL_HELMET) || stack.is(Items.CHAINMAIL_CHESTPLATE) ||
                stack.is(Items.CHAINMAIL_LEGGINGS) || stack.is(Items.CHAINMAIL_BOOTS) ||
                stack.is(Items.IRON_HELMET) || stack.is(Items.IRON_CHESTPLATE) ||
                stack.is(Items.IRON_LEGGINGS) || stack.is(Items.IRON_BOOTS) ||
                stack.is(Items.GOLDEN_HELMET) || stack.is(Items.GOLDEN_CHESTPLATE) ||
                stack.is(Items.GOLDEN_LEGGINGS) || stack.is(Items.GOLDEN_BOOTS) ||
                stack.is(Items.DIAMOND_HELMET) || stack.is(Items.DIAMOND_CHESTPLATE) ||
                stack.is(Items.DIAMOND_LEGGINGS) || stack.is(Items.DIAMOND_BOOTS) ||
                stack.is(Items.NETHERITE_HELMET) || stack.is(Items.NETHERITE_CHESTPLATE) ||
                stack.is(Items.NETHERITE_LEGGINGS) || stack.is(Items.NETHERITE_BOOTS)) {
                return "ARMOR";
            }
            
            // Tools - pickaxes, shovels, hoes
            if (stack.is(Items.WOODEN_PICKAXE) || stack.is(Items.STONE_PICKAXE) ||
                stack.is(Items.IRON_PICKAXE) || stack.is(Items.GOLDEN_PICKAXE) ||
                stack.is(Items.DIAMOND_PICKAXE) || stack.is(Items.NETHERITE_PICKAXE) ||
                stack.is(Items.WOODEN_SHOVEL) || stack.is(Items.STONE_SHOVEL) ||
                stack.is(Items.IRON_SHOVEL) || stack.is(Items.GOLDEN_SHOVEL) ||
                stack.is(Items.DIAMOND_SHOVEL) || stack.is(Items.NETHERITE_SHOVEL) ||
                stack.is(Items.WOODEN_HOE) || stack.is(Items.STONE_HOE) ||
                stack.is(Items.IRON_HOE) || stack.is(Items.GOLDEN_HOE) ||
                stack.is(Items.DIAMOND_HOE) || stack.is(Items.NETHERITE_HOE)) {
                return "TOOLS";
            }
            
            // Food
            if (stack.is(Items.APPLE) || stack.is(Items.GOLDEN_APPLE) ||
                stack.is(Items.ENCHANTED_GOLDEN_APPLE) || stack.is(Items.BREAD) ||
                stack.is(Items.COOKED_BEEF) || stack.is(Items.COOKED_PORKCHOP) ||
                stack.is(Items.COOKED_CHICKEN) || stack.is(Items.COOKED_MUTTON) ||
                stack.is(Items.COOKED_RABBIT) || stack.is(Items.COOKED_COD) ||
                stack.is(Items.COOKED_SALMON) || stack.has(net.minecraft.core.component.DataComponents.FOOD)) {
                return "FOOD";
            }
            
            // Blocks
            if (item instanceof net.minecraft.world.item.BlockItem) {
                return "BLOCKS";
            }
            
            // Potions
            if (stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) ||
                stack.is(Items.LINGERING_POTION) || stack.is(Items.TIPPED_ARROW)) {
                return "POTIONS";
            }
            
            // Enchanted items
            if (stack.isEnchanted() || stack.is(Items.ENCHANTED_BOOK)) {
                return "ENCHANTED";
            }
            
            return "MISC";
        }
    }
    
    public static class PlayerShop {
        public String ownerUuid;
        public String ownerName;
        public String shopName;
        public List<ShopListing> listings = new ArrayList<>();
        public long totalEarnings;
        public long uncollectedEarnings;
        public int totalSales;
        public long createdTime;
        public BlockPos shopLocation; // Physical shop location (if any)
        public boolean isPublic;
        
        public PlayerShop() {}
        
        public PlayerShop(String ownerUuid, String ownerName, String shopName) {
            this.ownerUuid = ownerUuid;
            this.ownerName = ownerName;
            this.shopName = shopName;
            this.totalEarnings = 0;
            this.uncollectedEarnings = 0;
            this.totalSales = 0;
            this.createdTime = System.currentTimeMillis();
            this.isPublic = true;
        }
    }
    
    // All player shops: UUID -> PlayerShop
    private static final Map<String, PlayerShop> playerShops = new ConcurrentHashMap<>();
    
    // All listings by ID for quick lookup
    private static final Map<String, ShopListing> listingById = new ConcurrentHashMap<>();
    
    // Physical shop locations: "world,x,y,z" -> ownerUuid
    private static final Map<String, String> shopLocations = new ConcurrentHashMap<>();
    
    // Max listings per shop
    private static final int MAX_LISTINGS_PER_SHOP = 28;
    
    // Max shops per player
    private static final int MAX_SHOPS_PER_PLAYER = 3;
    
    // Listing fee percentage
    private static final double LISTING_FEE_PERCENT = 2.0; // 2% of price
    
    // File path for persistence
    private static Path dataFile;

    // ════════════════════════════════════════════════════════════
    // INITIALIZATION & PERSISTENCE
    // ════════════════════════════════════════════════════════════
    
    public static void initialize(Path dataDir) {
        dataFile = dataDir.resolve("player_shops.json");
        loadData();
    }
    
    private static void loadData() {
        if (dataFile == null || !Files.exists(dataFile)) return;
        
        try (Reader reader = Files.newBufferedReader(dataFile)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Type type = new TypeToken<Map<String, PlayerShop>>(){}.getType();
            Map<String, PlayerShop> loaded = gson.fromJson(reader, type);
            
            if (loaded != null) {
                playerShops.putAll(loaded);
                
                // Rebuild listing index
                for (PlayerShop shop : playerShops.values()) {
                    for (ShopListing listing : shop.listings) {
                        listingById.put(listing.listingId, listing);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[PlayerShopManager] Failed to load data: " + e.getMessage());
        }
    }
    
    public static void saveData() {
        if (dataFile == null) return;
        
        try {
            Files.createDirectories(dataFile.getParent());
            
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (Writer writer = Files.newBufferedWriter(dataFile)) {
                gson.toJson(playerShops, writer);
            }
        } catch (Exception e) {
            System.err.println("[PlayerShopManager] Failed to save data: " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════
    // SHOP MANAGEMENT
    // ════════════════════════════════════════════════════════════
    
    public static boolean createShop(ServerPlayer player, String shopName) {
        String uuid = player.getUUID().toString();
        
        // Check if player already has max shops
        long shopCount = playerShops.values().stream()
                .filter(shop -> shop.ownerUuid.equals(uuid))
                .count();
        
        if (shopCount >= MAX_SHOPS_PER_PLAYER) {
            player.sendSystemMessage(Component.literal("You already have the maximum number of shops (" + MAX_SHOPS_PER_PLAYER + ")!")
                    .withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        // Check if shop name is taken
        boolean nameTaken = playerShops.values().stream()
                .anyMatch(shop -> shop.shopName.equalsIgnoreCase(shopName));
        
        if (nameTaken) {
            player.sendSystemMessage(Component.literal("A shop with that name already exists!")
                    .withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        // Create shop
        PlayerShop shop = new PlayerShop(uuid, player.getName().getString(), shopName);
        playerShops.put(uuid + "_" + shopName.toLowerCase().replace(" ", "_"), shop);
        saveData();
        
        player.sendSystemMessage(Component.literal("Created shop '" + shopName + "' successfully!")
                .withStyle(ChatFormatting.GREEN), false);
        return true;
    }
    
    public static boolean deleteShop(ServerPlayer player, String shopName) {
        String uuid = player.getUUID().toString();
        String key = uuid + "_" + shopName.toLowerCase().replace(" ", "_");
        
        PlayerShop shop = playerShops.get(key);
        if (shop == null) {
            player.sendSystemMessage(Component.literal("Shop '" + shopName + "' not found!")
                    .withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        if (!shop.ownerUuid.equals(uuid)) {
            player.sendSystemMessage(Component.literal("You don't own this shop!")
                    .withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        // Return items to player
        for (ShopListing listing : shop.listings) {
            ItemStack stack = listing.getItemStack();
            stack.setCount(listing.stock);
            player.addItem(stack);
        }
        
        // Give uncollected earnings
        if (shop.uncollectedEarnings > 0) {
            CoinManager.giveCoins(uuid, shop.uncollectedEarnings);
            player.sendSystemMessage(Component.literal("Collected " + shop.uncollectedEarnings + " coins from closed shop!")
                    .withStyle(ChatFormatting.GOLD), false);
        }
        
        // Remove shop
        playerShops.remove(key);
        for (ShopListing listing : shop.listings) {
            listingById.remove(listing.listingId);
        }
        saveData();
        
        player.sendSystemMessage(Component.literal("Shop '" + shopName + "' deleted successfully!")
                .withStyle(ChatFormatting.GREEN), false);
        return true;
    }
    
    public static PlayerShop getShop(String ownerUuid, String shopName) {
        return playerShops.get(ownerUuid + "_" + shopName.toLowerCase().replace(" ", "_"));
    }
    
    public static List<PlayerShop> getShopsByOwner(String ownerUuid) {
        List<PlayerShop> result = new ArrayList<>();
        for (PlayerShop shop : playerShops.values()) {
            if (shop.ownerUuid.equals(ownerUuid)) {
                result.add(shop);
            }
        }
        return result;
    }
    
    public static List<PlayerShop> getAllPublicShops() {
        List<PlayerShop> result = new ArrayList<>();
        for (PlayerShop shop : playerShops.values()) {
            if (shop.isPublic && !shop.listings.isEmpty()) {
                result.add(shop);
            }
        }
        return result;
    }

    // ════════════════════════════════════════════════════════════
    // LISTING MANAGEMENT
    // ════════════════════════════════════════════════════════════
    
    public static boolean addListing(ServerPlayer player, String shopName, ItemStack stack, int amount, long pricePerItem) {
        String uuid = player.getUUID().toString();
        PlayerShop shop = getShop(uuid, shopName);
        
        if (shop == null) {
            player.sendSystemMessage(Component.literal("Shop '" + shopName + "' not found!")
                    .withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        if (shop.listings.size() >= MAX_LISTINGS_PER_SHOP) {
            player.sendSystemMessage(Component.literal("Shop is full! Maximum " + MAX_LISTINGS_PER_SHOP + " listings.")
                    .withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        if (stack.isEmpty() || amount <= 0) {
            player.sendSystemMessage(Component.literal("Invalid item or amount!")
                    .withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        if (pricePerItem <= 0) {
            player.sendSystemMessage(Component.literal("Price must be greater than 0!")
                    .withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        // Check if player has enough items
        int available = countItemInInventory(player, stack);
        if (available < amount) {
            player.sendSystemMessage(Component.literal("You only have " + available + " of that item!")
                    .withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        // Calculate and charge listing fee
        long listingFee = (long) (amount * pricePerItem * LISTING_FEE_PERCENT / 100);
        if (listingFee > 0) {
            long balance = CoinManager.getCoinsLong(uuid);
            if (balance < listingFee) {
                player.sendSystemMessage(Component.literal("Listing fee is " + listingFee + " coins. You only have " + balance + "!")
                        .withStyle(ChatFormatting.RED), false);
                return false;
            }
            CoinManager.removeCoins(uuid, listingFee);
        }
        
        // Remove items from player inventory
        removeItemsFromInventory(player, stack, amount);
        
        // Create listing
        ShopListing listing = new ShopListing(uuid, player.getName().getString(), stack, amount, pricePerItem);
        shop.listings.add(listing);
        listingById.put(listing.listingId, listing);
        saveData();
        
        player.sendSystemMessage(Component.literal("Listed " + amount + "x " + stack.getHoverName().getString() + " for " + pricePerItem + " coins each!")
                .withStyle(ChatFormatting.GREEN), false);
        if (listingFee > 0) {
            player.sendSystemMessage(Component.literal("Listing fee: " + listingFee + " coins")
                    .withStyle(ChatFormatting.GRAY), false);
        }
        return true;
    }
    
    public static boolean removeListing(ServerPlayer player, String listingId) {
        String uuid = player.getUUID().toString();
        ShopListing listing = listingById.get(listingId);
        
        if (listing == null) {
            player.sendSystemMessage(Component.literal("Listing not found!")
                    .withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        if (!listing.sellerUuid.equals(uuid)) {
            player.sendSystemMessage(Component.literal("You don't own this listing!")
                    .withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        // Return remaining stock to player
        if (listing.stock > 0) {
            ItemStack stack = listing.getItemStack();
            stack.setCount(listing.stock);
            player.addItem(stack);
            player.sendSystemMessage(Component.literal("Returned " + listing.stock + "x " + stack.getHoverName().getString() + " to your inventory.")
                    .withStyle(ChatFormatting.YELLOW), false);
        }
        
        // Remove from shop
        for (PlayerShop shop : playerShops.values()) {
            if (shop.listings.removeIf(l -> l.listingId.equals(listingId))) {
                break;
            }
        }
        listingById.remove(listingId);
        saveData();
        
        player.sendSystemMessage(Component.literal("Listing removed successfully!")
                .withStyle(ChatFormatting.GREEN), false);
        return true;
    }
    
    public static boolean purchaseItem(ServerPlayer buyer, String listingId, int amount) {
        ShopListing listing = listingById.get(listingId);
        
        if (listing == null) {
            buyer.sendSystemMessage(Component.literal("Listing not found!")
                    .withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        if (amount <= 0 || amount > listing.stock) {
            buyer.sendSystemMessage(Component.literal("Invalid amount! Available: " + listing.stock)
                    .withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        long totalCost = amount * listing.pricePerItem;
        String buyerUuid = buyer.getUUID().toString();
        
        // Check if buyer has enough coins
        long buyerBalance = CoinManager.getCoinsLong(buyerUuid);
        if (buyerBalance < totalCost) {
            buyer.sendSystemMessage(Component.literal("You need " + totalCost + " coins but only have " + buyerBalance + "!")
                    .withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        // Check if buyer is seller (prevent self-purchase)
        if (listing.sellerUuid.equals(buyerUuid)) {
            buyer.sendSystemMessage(Component.literal("You cannot buy from your own shop!")
                    .withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        // Process transaction
        CoinManager.removeCoins(buyerUuid, totalCost);
        
        // Give items to buyer
        ItemStack stack = listing.getItemStack();
        stack.setCount(amount);
        buyer.addItem(stack);
        
        // Update listing stock
        listing.stock -= amount;
        
        // Add earnings to seller's shop
        PlayerShop sellerShop = null;
        for (PlayerShop shop : playerShops.values()) {
            if (shop.listings.contains(listing)) {
                sellerShop = shop;
                break;
            }
        }
        
        if (sellerShop != null) {
            sellerShop.uncollectedEarnings += totalCost;
            sellerShop.totalEarnings += totalCost;
            sellerShop.totalSales++;
        }
        
        // Remove listing if out of stock
        if (listing.stock <= 0 && !listing.infinite) {
            if (sellerShop != null) {
                sellerShop.listings.remove(listing);
            }
            listingById.remove(listingId);
        }
        
        saveData();
        
        buyer.sendSystemMessage(Component.literal("Purchased " + amount + "x " + stack.getHoverName().getString() + " for " + totalCost + " coins!")
                .withStyle(ChatFormatting.GREEN), false);
        
        // Notify seller if online
        ServerPlayer seller = PoliticalServer.server.getPlayerList().getPlayer(UUID.fromString(listing.sellerUuid));
        if (seller != null) {
            seller.sendSystemMessage(Component.literal(buyer.getName().getString() + " bought " + amount + "x " + stack.getHoverName().getString() + 
                    " from your shop for " + totalCost + " coins!").withStyle(ChatFormatting.GOLD), false);
        }
        
        return true;
    }

    // ════════════════════════════════════════════════════════════
    // EARNINGS
    // ════════════════════════════════════════════════════════════
    
    public static long collectEarnings(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long totalCollected = 0;
        
        for (PlayerShop shop : getShopsByOwner(uuid)) {
            if (shop.uncollectedEarnings > 0) {
                totalCollected += shop.uncollectedEarnings;
                shop.uncollectedEarnings = 0;
            }
        }
        
        if (totalCollected > 0) {
            CoinManager.giveCoins(uuid, totalCollected);
            saveData();
            player.sendSystemMessage(Component.literal("Collected " + totalCollected + " coins from your shops!")
                    .withStyle(ChatFormatting.GOLD), false);
        } else {
            player.sendSystemMessage(Component.literal("No earnings to collect!")
                    .withStyle(ChatFormatting.YELLOW), false);
        }
        
        return totalCollected;
    }
    
    public static long getUncollectedEarnings(String ownerUuid) {
        long total = 0;
        for (PlayerShop shop : getShopsByOwner(ownerUuid)) {
            total += shop.uncollectedEarnings;
        }
        return total;
    }

    // ════════════════════════════════════════════════════════════
    // SHOP SETTINGS
    // ════════════════════════════════════════════════════════════
    
    public static boolean setShopPublic(ServerPlayer player, String shopName, boolean isPublic) {
        String uuid = player.getUUID().toString();
        PlayerShop shop = getShop(uuid, shopName);
        
        if (shop == null) {
            player.sendSystemMessage(Component.literal("Shop not found!")
                    .withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        shop.isPublic = isPublic;
        saveData();
        
        player.sendSystemMessage(Component.literal("Shop '" + shopName + "' is now " + (isPublic ? "public" : "private") + "!")
                .withStyle(ChatFormatting.GREEN), false);
        return true;
    }
    
    public static boolean renameShop(ServerPlayer player, String oldName, String newName) {
        String uuid = player.getUUID().toString();
        PlayerShop shop = getShop(uuid, oldName);
        
        if (shop == null) {
            player.sendSystemMessage(Component.literal("Shop '" + oldName + "' not found!")
                    .withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        // Check if new name is taken
        boolean nameTaken = playerShops.values().stream()
                .anyMatch(s -> s.shopName.equalsIgnoreCase(newName) && !s.ownerUuid.equals(uuid));
        
        if (nameTaken) {
            player.sendSystemMessage(Component.literal("A shop with that name already exists!")
                    .withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        // Remove old key
        String oldKey = uuid + "_" + oldName.toLowerCase().replace(" ", "_");
        playerShops.remove(oldKey);
        
        // Update shop name
        shop.shopName = newName;
        
        // Add with new key
        String newKey = uuid + "_" + newName.toLowerCase().replace(" ", "_");
        playerShops.put(newKey, shop);
        saveData();
        
        player.sendSystemMessage(Component.literal("Shop renamed to '" + newName + "'!")
                .withStyle(ChatFormatting.GREEN), false);
        return true;
    }

    // ════════════════════════════════════════════════════════════
    // INVENTORY HELPERS
    // ════════════════════════════════════════════════════════════
    
    private static int countItemInInventory(ServerPlayer player, ItemStack template) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (ItemStack.isSameItemSameComponents(stack, template)) {
                count += stack.getCount();
            }
        }
        return count;
    }
    
    private static void removeItemsFromInventory(ServerPlayer player, ItemStack template, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (ItemStack.isSameItemSameComponents(stack, template)) {
                int toRemove = Math.min(remaining, stack.getCount());
                stack.shrink(toRemove);
                remaining -= toRemove;
            }
        }
    }

    // ════════════════════════════════════════════════════════════
    // STATISTICS
    // ════════════════════════════════════════════════════════════
    
    public static int getTotalShopCount() {
        return playerShops.size();
    }
    
    public static int getTotalListingCount() {
        return listingById.size();
    }
    
    public static Map<String, PlayerShop> getAllShops() {
        return new HashMap<>(playerShops);
    }
    
    public static ShopListing getListing(String listingId) {
        return listingById.get(listingId);
    }
    
    public static List<ShopListing> getListingsByCategory(String category) {
        List<ShopListing> result = new ArrayList<>();
        for (ShopListing listing : listingById.values()) {
            if (category.equals("ALL") || listing.category.equals(category)) {
                result.add(listing);
            }
        }
        return result;
    }
}
