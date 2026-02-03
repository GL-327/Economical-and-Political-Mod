package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SignGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;import eu.pb4.sgui.api.ClickType;import eu.pb4.sgui.api.ClickType;

public class AuctionHouseGui {

    private static final int ITEMS_PER_PAGE = 21;
    private static final long COINS_PER_CREDIT = 10000L;
    private static final long MAX_PRICE = 1_000_000_000L;

    // Pending sell data
    private static final Map<UUID, ItemStack> pendingSellItems = new HashMap<>();
    private static final Map<UUID, Long> pendingSellPrice = new HashMap<>();
    private static final Map<UUID, Boolean> pendingSellInCoins = new HashMap<>();

    // Unsellable items
    private static final Set<Item> UNSELLABLE_ITEMS = new HashSet<>();

    static {
        UNSELLABLE_ITEMS.add(Items.COMMAND_BLOCK);
        UNSELLABLE_ITEMS.add(Items.CHAIN_COMMAND_BLOCK);
        UNSELLABLE_ITEMS.add(Items.REPEATING_COMMAND_BLOCK);
        UNSELLABLE_ITEMS.add(Items.COMMAND_BLOCK_MINECART);
        UNSELLABLE_ITEMS.add(Items.BARRIER);
        UNSELLABLE_ITEMS.add(Items.STRUCTURE_BLOCK);
        UNSELLABLE_ITEMS.add(Items.STRUCTURE_VOID);
        UNSELLABLE_ITEMS.add(Items.JIGSAW);
        UNSELLABLE_ITEMS.add(Items.DEBUG_STICK);
        UNSELLABLE_ITEMS.add(Items.LIGHT);
        UNSELLABLE_ITEMS.add(Items.BEDROCK);
        UNSELLABLE_ITEMS.add(Items.END_PORTAL_FRAME);
        UNSELLABLE_ITEMS.add(Items.SPAWNER);
        UNSELLABLE_ITEMS.add(Items.KNOWLEDGE_BOOK);
        UNSELLABLE_ITEMS.add(Items.WRITTEN_BOOK);
    }

    // Categories
    public enum Category {
        ALL("All Items", Items.CHEST, Formatting.WHITE),
        WEAPONS("Weapons", Items.DIAMOND_SWORD, Formatting.RED),
        ARMOR("Armor", Items.DIAMOND_CHESTPLATE, Formatting.BLUE),
        TOOLS("Tools", Items.DIAMOND_PICKAXE, Formatting.YELLOW),
        FOOD("Food", Items.GOLDEN_APPLE, Formatting.GOLD),
        BLOCKS("Blocks", Items.STONE, Formatting.GRAY),
        MATERIALS("Materials", Items.DIAMOND, Formatting.AQUA),
        POTIONS("Potions", Items.POTION, Formatting.LIGHT_PURPLE),
        ENCHANTED("Enchanted", Items.ENCHANTED_BOOK, Formatting.DARK_PURPLE),
        MISC("Misc", Items.ENDER_PEARL, Formatting.GREEN);

        public final String displayName;
        public final Item icon;
        public final Formatting color;

        Category(String displayName, Item icon, Formatting color) {
            this.displayName = displayName;
            this.icon = icon;
            this.color = color;
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // TAX CALCULATION - Can be modified by government perk
    // ════════════════════════════════════════════════════════════════════

    public static float getTaxMultiplier() {
        if (PerkManager.hasActivePerk("AUCTION_TAX_FREE")) {
            return 0.0f;
        }
        if (PerkManager.hasActivePerk("AUCTION_TAX_REDUCTION")) {
            return 0.5f;
        }
        if (PerkManager.hasActivePerk("AUCTION_TAX_INCREASE")) {
            return 1.5f;
        }
        return 1.0f;
    }

    public static long calculateListingTax(long price) {
        float multiplier = getTaxMultiplier();
        if (multiplier == 0) return 0;

        long baseTax;

        if (price <= 10000) {
            baseTax = (long) Math.ceil(price * 0.001);
        } else if (price <= 100000) {
            long lowTax = (long) Math.ceil(10000 * 0.001);
            baseTax = lowTax + (long) Math.ceil((price - 10000) * 0.003);
        } else if (price <= 1000000) {
            long lowTax = (long) Math.ceil(10000 * 0.001);
            long midTax = (long) Math.ceil(90000 * 0.003);
            baseTax = lowTax + midTax + (long) Math.ceil((price - 100000) * 0.005);
        } else {
            long lowTax = (long) Math.ceil(10000 * 0.001);
            long midTax = (long) Math.ceil(90000 * 0.003);
            long highTax = (long) Math.ceil(900000 * 0.005);
            baseTax = lowTax + midTax + highTax + (long) Math.ceil((price - 1000000) * 0.01);
        }

        return (long) Math.ceil(baseTax * multiplier);
    }

    public static long calculatePurchaseTax(long price) {
        return calculateListingTax(price) / 2;
    }

    // ════════════════════════════════════════════════════════════════════
    // MAIN MENU - Buy or Sell
    // ════════════════════════════════════════════════════════════════════

    public static void open(ServerPlayerEntity player) {
        openMainMenu(player);
    }

    public static void openMainMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("Auction House"));

        // Fill background
        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Top row decoration
        for (int i = 0; i < 9; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Bottom row decoration
        for (int i = 18; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Balance display (top center)
        int credits = CreditItem.countCredits(player);
        int coins = CoinManager.getCoins(player);
        gui.setSlot(4, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Text.literal("Your Balance").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Credits: " + formatNumber(credits)).formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("Coins: " + formatNumber(coins)).formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("10,000 coins = 1 credit").formatted(Formatting.GRAY))
                .glow()
                .build());

        // Browse Auctions (left)
        gui.setSlot(11, new GuiElementBuilder(Items.EMERALD)
                .setName(Text.literal("Browse Auctions").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("View items for sale").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Buy with coins or credits").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to browse!").formatted(Formatting.YELLOW))
                .glow()
                .setCallback((index, type, action) -> openBuyMenu(player, 0, Category.ALL))
                .build());

        // Create Auction (center)
        gui.setSlot(13, new GuiElementBuilder(Items.CHEST)
                .setName(Text.literal("Create Auction").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("List your items for sale").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Set your own price!").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to sell!").formatted(Formatting.YELLOW))
                .glow()
                .setCallback((index, type, action) -> openSellMenu(player))
                .build());

        // My Listings (right)
        gui.setSlot(15, new GuiElementBuilder(Items.BOOK)
                .setName(Text.literal("My Listings").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("View your active listings").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Cancel listings here").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to view!").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> openMyListings(player, 0))
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════════════
    // BUY MENU - Browse Auctions with Categories on Side
    // ════════════════════════════════════════════════════════════════════

    public static void openBuyMenu(ServerPlayerEntity player, int page, Category category) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("Auctions - " + category.displayName));

        // Fill background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Categories on left side (column 0)
        int categorySlot = 0;
        for (Category cat : Category.values()) {
            if (categorySlot >= 54) break;

            boolean isSelected = cat == category;
            final Category clickedCat = cat;

            gui.setSlot(categorySlot, new GuiElementBuilder(cat.icon)
                    .setName(Text.literal(cat.displayName).formatted(cat.color, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(isSelected
                            ? Text.literal("✓ Currently viewing").formatted(Formatting.GREEN)
                            : Text.literal("Click to view").formatted(Formatting.YELLOW))
                    .glow(isSelected)
                    .setCallback((index, type, action) -> openBuyMenu(player, 0, clickedCat))
                    .build());

            categorySlot += 9; // Move down one row
        }

        // Get filtered listings
        List<AuctionHouseManager.AuctionListing> allListings = AuctionHouseManager.getListings();
        List<AuctionHouseManager.AuctionListing> filteredListings = new ArrayList<>();

        for (AuctionHouseManager.AuctionListing listing : allListings) {
            ItemStack stack = listing.getItemStack();
            if (stack.isEmpty()) continue;

            if (category == Category.ALL || getItemCategory(stack) == category) {
                filteredListings.add(listing);
            }
        }

        int totalPages = Math.max(1, (int) Math.ceil((double) filteredListings.size() / ITEMS_PER_PAGE));
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, filteredListings.size());

        // Item display slots (columns 1-7, rows 0-2)
        int[] itemSlots = {
                1, 2, 3, 4, 5, 6, 7,
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25
        };

        int slotIndex = 0;
        int playerCredits = CreditItem.countCredits(player);
        int playerCoins = CoinManager.getCoins(player);

        for (int i = startIndex; i < endIndex && slotIndex < itemSlots.length; i++) {
            AuctionHouseManager.AuctionListing listing = filteredListings.get(i);
            ItemStack displayStack = listing.getItemStack();

            if (displayStack.isEmpty()) continue;

            String listingId = listing.id;
            long price = listing.price;
            boolean priceInCoins = listing.priceInCoins;
            boolean isOwnListing = player.getUuidAsString().equals(listing.sellerUuid);

            long creditEquivalent = priceInCoins ? Math.max(1, price / COINS_PER_CREDIT) : price;
            long coinEquivalent = priceInCoins ? price : (price * COINS_PER_CREDIT);
            boolean canAfford = playerCredits >= creditEquivalent || playerCoins >= coinEquivalent;

            GuiElementBuilder builder = new GuiElementBuilder(displayStack.getItem())
                    .setCount(Math.min(displayStack.getCount(), 64))
                    .setName(displayStack.getName().copy().formatted(Formatting.WHITE, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("━━━━━━━━━━━━━━━━").formatted(Formatting.DARK_GRAY));

            if (priceInCoins) {
                builder.addLoreLine(Text.literal("Price: " + formatNumber(price) + " coins").formatted(Formatting.YELLOW));
                builder.addLoreLine(Text.literal("  (~" + formatNumber(creditEquivalent) + " credits)").formatted(Formatting.DARK_GRAY));
            } else {
                builder.addLoreLine(Text.literal("Price: " + formatNumber(price) + " credits").formatted(Formatting.GREEN));
                builder.addLoreLine(Text.literal("  (~" + formatNumber(coinEquivalent) + " coins)").formatted(Formatting.DARK_GRAY));
            }

            builder.addLoreLine(Text.literal("Seller: " + listing.sellerName).formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("Qty: x" + displayStack.getCount()).formatted(Formatting.AQUA))
                    .addLoreLine(Text.literal("━━━━━━━━━━━━━━━━").formatted(Formatting.DARK_GRAY))
                    .addLoreLine(Text.literal(""));

            if (isOwnListing) {
                builder.addLoreLine(Text.literal("This is your listing").formatted(Formatting.YELLOW));
            } else if (canAfford) {
                builder.addLoreLine(Text.literal("Click to buy!").formatted(Formatting.GREEN));
            } else {
                builder.addLoreLine(Text.literal("Cannot afford").formatted(Formatting.RED));
            }

            final String fListingId = listingId;
            final int fPage = page;
            final Category fCategory = category;
            final boolean fCanAfford = canAfford;
            final boolean fIsOwn = isOwnListing;

            builder.setCallback((index, type, action) -> {
                if (!fIsOwn && fCanAfford) {
                    openPurchaseConfirmation(player, fListingId, fPage, fCategory);
                }
            });

            gui.setSlot(itemSlots[slotIndex], builder.build());
            slotIndex++;
        }

        // Empty state
        if (filteredListings.isEmpty()) {
            gui.setSlot(13, new GuiElementBuilder(Items.BARRIER)
                    .setName(Text.literal("No Listings").formatted(Formatting.RED, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("No items in this category").formatted(Formatting.GRAY))
                    .build());
        }

        // Bottom navigation bar

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("Back to Menu").formatted(Formatting.RED))
                .setCallback((index, type, action) -> openMainMenu(player))
                .build());

        // Previous page
        if (page > 0) {
            final int prevPage = page - 1;
            final Category fCat = category;
            gui.setSlot(48, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("◀ Previous Page").formatted(Formatting.YELLOW))
                    .setCallback((index, type, action) -> openBuyMenu(player, prevPage, fCat))
                    .build());
        }

        // Page info
        gui.setSlot(49, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("Page " + (page + 1) + "/" + totalPages).formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Total: " + filteredListings.size() + " listings").formatted(Formatting.GRAY))
                .build());

        // Next page
        if (page < totalPages - 1) {
            final int nextPage = page + 1;
            final Category fCat = category;
            gui.setSlot(50, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("Next Page ▶").formatted(Formatting.YELLOW))
                    .setCallback((index, type, action) -> openBuyMenu(player, nextPage, fCat))
                    .build());
        }

        // Refresh
        final int fPage = page;
        final Category fCat = category;
        gui.setSlot(53, new GuiElementBuilder(Items.SUNFLOWER)
                .setName(Text.literal("Refresh").formatted(Formatting.AQUA))
                .setCallback((index, type, action) -> openBuyMenu(player, fPage, fCat))
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════════════
    // PURCHASE CONFIRMATION
    // ════════════════════════════════════════════════════════════════════

    private static void openPurchaseConfirmation(ServerPlayerEntity player, String listingId, int returnPage, Category returnCategory) {
        AuctionHouseManager.AuctionListing listing = AuctionHouseManager.getListing(listingId);
        if (listing == null) {
            player.sendMessage(Text.literal("Listing no longer exists!").formatted(Formatting.RED));
            openBuyMenu(player, returnPage, returnCategory);
            return;
        }

        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("Confirm Purchase"));

        // Fill background
        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        ItemStack displayStack = listing.getItemStack();
        long price = listing.price;
        boolean priceInCoins = listing.priceInCoins;
        long tax = calculatePurchaseTax(price);
        long totalPrice = price + tax;

        // Item display
        gui.setSlot(4, new GuiElementBuilder(displayStack.getItem())
                .setCount(Math.min(displayStack.getCount(), 64))
                .setName(displayStack.getName().copy().formatted(Formatting.WHITE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Price: " + formatNumber(price) + (priceInCoins ? " coins" : " credits")).formatted(Formatting.GOLD))
                .addLoreLine(Text.literal("Tax: " + formatNumber(tax) + (priceInCoins ? " coins" : " credits")).formatted(Formatting.RED))
                .addLoreLine(Text.literal("Total: " + formatNumber(totalPrice) + (priceInCoins ? " coins" : " credits")).formatted(Formatting.YELLOW))
                .glow()
                .build());

        int playerCredits = CreditItem.countCredits(player);
        int playerCoins = CoinManager.getCoins(player);

        long creditPrice = priceInCoins ? Math.max(1, (totalPrice + COINS_PER_CREDIT - 1) / COINS_PER_CREDIT) : totalPrice;
        long coinPrice = priceInCoins ? totalPrice : (totalPrice * COINS_PER_CREDIT);

        boolean canAffordCredits = playerCredits >= creditPrice;
        boolean canAffordCoins = playerCoins >= coinPrice;

        final int fReturnPage = returnPage;
        final Category fReturnCategory = returnCategory;
        final String fListingId = listingId;

        // Pay with Credits
        gui.setSlot(11, new GuiElementBuilder(canAffordCredits ? Items.EMERALD : Items.BARRIER)
                .setName(Text.literal("Pay with Credits").formatted(canAffordCredits ? Formatting.GREEN : Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Cost: " + formatNumber(creditPrice) + " credits").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("You have: " + formatNumber(playerCredits)).formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(canAffordCredits ? Text.literal("Click to buy!").formatted(Formatting.YELLOW) : Text.literal("Not enough credits!").formatted(Formatting.RED))
                .glow(canAffordCredits)
                .setCallback((index, type, action) -> {
                    if (canAffordCredits) {
                        purchaseItem(player, fListingId, false);
                        openBuyMenu(player, fReturnPage, fReturnCategory);
                    }
                })
                .build());

        // Pay with Coins
        gui.setSlot(15, new GuiElementBuilder(canAffordCoins ? Items.GOLD_INGOT : Items.BARRIER)
                .setName(Text.literal("Pay with Coins").formatted(canAffordCoins ? Formatting.YELLOW : Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Cost: " + formatNumber(coinPrice) + " coins").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("You have: " + formatNumber(playerCoins)).formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(canAffordCoins ? Text.literal("Click to buy!").formatted(Formatting.YELLOW) : Text.literal("Not enough coins!").formatted(Formatting.RED))
                .glow(canAffordCoins)
                .setCallback((index, type, action) -> {
                    if (canAffordCoins) {
                        purchaseItem(player, fListingId, true);
                        openBuyMenu(player, fReturnPage, fReturnCategory);
                    }
                })
                .build());

        // Cancel
        gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("Cancel").formatted(Formatting.RED))
                .setCallback((index, type, action) -> openBuyMenu(player, fReturnPage, fReturnCategory))
                .build());

        gui.open();
    }

    private static void purchaseItem(ServerPlayerEntity buyer, String listingId, boolean payWithCoins) {
        AuctionHouseManager.AuctionListing listing = AuctionHouseManager.getListing(listingId);
        if (listing == null) {
            buyer.sendMessage(Text.literal("Listing no longer exists!").formatted(Formatting.RED));
            return;
        }

        if (buyer.getUuidAsString().equals(listing.sellerUuid)) {
            buyer.sendMessage(Text.literal("Cannot buy your own listing!").formatted(Formatting.RED));
            return;
        }

        long price = listing.price;
        boolean priceInCoins = listing.priceInCoins;
        long tax = calculatePurchaseTax(price);
        long totalPrice = price + tax;

        long creditPrice = priceInCoins ? Math.max(1, (totalPrice + COINS_PER_CREDIT - 1) / COINS_PER_CREDIT) : totalPrice;
        long coinPrice = priceInCoins ? totalPrice : (totalPrice * COINS_PER_CREDIT);

        if (payWithCoins) {
            if (!CoinManager.hasCoins(buyer, (int) Math.min(coinPrice, Integer.MAX_VALUE))) {
                buyer.sendMessage(Text.literal("Not enough coins!").formatted(Formatting.RED));
                return;
            }
            CoinManager.removeCoins(buyer, (int) Math.min(coinPrice, Integer.MAX_VALUE));
            if (priceInCoins) {
                CoinManager.giveCoins(listing.sellerUuid, (int) Math.min(price, Integer.MAX_VALUE));
            } else {
                CreditItem.giveCredits(listing.sellerUuid, (int) Math.min(price, Integer.MAX_VALUE));
            }
        } else {
            if (!CreditItem.hasCredits(buyer, (int) Math.min(creditPrice, Integer.MAX_VALUE))) {
                buyer.sendMessage(Text.literal("Not enough credits!").formatted(Formatting.RED));
                return;
            }
            CreditItem.removeCredits(buyer, (int) Math.min(creditPrice, Integer.MAX_VALUE));
            if (priceInCoins) {
                CoinManager.giveCoins(listing.sellerUuid, (int) Math.min(price, Integer.MAX_VALUE));
            } else {
                CreditItem.giveCredits(listing.sellerUuid, (int) Math.min(price, Integer.MAX_VALUE));
            }
        }

        ItemStack item = listing.getItemStack();
        AuctionHouseManager.removeListing(listingId);

        if (!buyer.getInventory().insertStack(item.copy())) {
            buyer.dropItem(item.copy(), false);
        }

        String currencyUsed = payWithCoins ? "coins" : "credits";
        long amountPaid = payWithCoins ? coinPrice : creditPrice;
        buyer.sendMessage(Text.literal("✓ Purchased for " + formatNumber(amountPaid) + " " + currencyUsed + "!").formatted(Formatting.GREEN));

        ServerPlayerEntity seller = PoliticalServer.server.getPlayerManager().getPlayer(java.util.UUID.fromString(listing.sellerUuid));
        if (seller != null) {
            String sellerCurrency = priceInCoins ? "coins" : "credits";
            seller.sendMessage(Text.literal("✓ Sold " + item.getName().getString() + " for " + formatNumber(price) + " " + sellerCurrency + "!").formatted(Formatting.GREEN));
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // SELL MENU - Create Auction
    // ════════════════════════════════════════════════════════════════════

    public static void openSellMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false) {
            @Override
            public boolean onAnyClick(int index, ClickType type, SlotActionType action) {
                // Player inventory starts at slot 54 in a 9x6 chest
                if (index >= 54 && index < 54 + 36) {
                    int invSlot = index - 54;
                    // Convert to proper inventory index (hotbar is 0-8, main inv is 9-35)
                    int realSlot;
                    if (invSlot < 27) {
                        realSlot = invSlot + 9; // Main inventory
                    } else {
                        realSlot = invSlot - 27; // Hotbar
                    }

                    if (realSlot >= 0 && realSlot < player.getInventory().size()) {
                        ItemStack clicked = player.getInventory().getStack(realSlot);
                        if (!clicked.isEmpty() && !UNSELLABLE_ITEMS.contains(clicked.getItem())) {
                            // Return existing pending item first
                            ItemStack existing = pendingSellItems.get(player.getUuid());
                            if (existing != null && !existing.isEmpty()) {
                                if (!player.getInventory().insertStack(existing.copy())) {
                                    player.dropItem(existing.copy(), false);
                                }
                            }

                            // Take the clicked item
                            pendingSellItems.put(player.getUuid(), clicked.copy());
                            pendingSellPrice.remove(player.getUuid());
                            pendingSellInCoins.remove(player.getUuid());
                            player.getInventory().setStack(realSlot, ItemStack.EMPTY);
                            player.sendMessage(Text.literal("✓ Item selected! Now set a price.").formatted(Formatting.GREEN));

                            // Refresh the GUI
                            openSellMenu(player);
                            return false;
                        } else if (!clicked.isEmpty()) {
                            player.sendMessage(Text.literal("✗ This item cannot be sold!").formatted(Formatting.RED));
                            return false;
                        }
                    }
                }
                return super.onAnyClick(index, type, action);
            }
        };


        // ... rest of method stays the same

        gui.setTitle(Text.literal("Create Auction"));



        // Fill entire GUI with background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Top decorative row
        for (int i = 0; i < 9; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // ═══════════════════════════════════════════════════════
        // ROW 1 (slots 0-8): Header with item slot in center
        // ═══════════════════════════════════════════════════════

        ItemStack pendingItem = pendingSellItems.get(player.getUuid());
        if (pendingItem != null && !pendingItem.isEmpty()) {
            gui.setSlot(4, new GuiElementBuilder(pendingItem.getItem())
                    .setCount(Math.min(pendingItem.getCount(), 64))
                    .setName(Text.literal("✦ Item to Sell ✦").formatted(Formatting.GREEN, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(pendingItem.getName().copy().formatted(Formatting.WHITE))
                    .addLoreLine(Text.literal("Quantity: x" + pendingItem.getCount()).formatted(Formatting.AQUA))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Click to remove").formatted(Formatting.RED))
                    .glow()
                    .setCallback((index, type, action) -> {
                        returnPendingItem(player);
                        openSellMenu(player);
                    })
                    .build());
        } else {
            gui.setSlot(4, new GuiElementBuilder(Items.LIGHT_GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal("Item Slot").formatted(Formatting.YELLOW, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Click an item in your").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("inventory to place it here").formatted(Formatting.GRAY))
                    .build());
        }

        // ═══════════════════════════════════════════════════════
        // ROW 3 (slots 18-26): Price controls
        // ═══════════════════════════════════════════════════════

        Long currentPrice = pendingSellPrice.get(player.getUuid());
        Boolean inCoins = pendingSellInCoins.get(player.getUuid());
        String priceDisplay = currentPrice != null ? formatNumber(currentPrice) : "Not set";
        String currencyDisplay = inCoins != null ? (inCoins ? "coins" : "credits") : "";

        // Set Price Button (left side)
        gui.setSlot(20, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Text.literal("Set Price").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(currentPrice != null
                        ? Text.literal("Current: " + priceDisplay + " " + currencyDisplay).formatted(Formatting.YELLOW)
                        : Text.literal("No price set").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Left-click: Price in Coins").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Right-click: Price in Credits").formatted(Formatting.GREEN))
                .glow()
                .setCallback((index, type, action) -> {
                    ItemStack pItem = pendingSellItems.get(player.getUuid());
                    if (pItem == null || pItem.isEmpty()) {
                        player.sendMessage(Text.literal("✗ Select an item first!").formatted(Formatting.RED));
                        return;
                    }
                    pendingSellInCoins.put(player.getUuid(), type.isLeft);
                    openPriceSign(player);
                })
                .build());

        // Listing Preview (center)
        if (currentPrice != null && currentPrice > 0 && inCoins != null) {
            long tax = calculateListingTax(currentPrice);
            String curr = inCoins ? "coins" : "credits";

            gui.setSlot(22, new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
                    .setName(Text.literal("Listing Preview").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Price: " + formatNumber(currentPrice) + " " + curr).formatted(Formatting.GOLD))
                    .addLoreLine(Text.literal("Tax: " + formatNumber(tax) + " " + curr).formatted(Formatting.RED))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("You receive: " + formatNumber(currentPrice) + " " + curr).formatted(Formatting.GREEN))
                    .addLoreLine(Text.literal("(Tax paid upfront)").formatted(Formatting.GRAY))
                    .glow()
                    .build());
        } else {
            gui.setSlot(22, new GuiElementBuilder(Items.GLASS_BOTTLE)
                    .setName(Text.literal("Listing Preview").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Set a price to see preview").formatted(Formatting.DARK_GRAY))
                    .build());
        }

        // Tax Info (right side)
        float taxMult = getTaxMultiplier();
        String taxStatus;
        Formatting taxColor;
        if (taxMult == 0) {
            taxStatus = "TAX FREE!";
            taxColor = Formatting.GREEN;
        } else if (taxMult < 1) {
            taxStatus = "Reduced (" + (int) (taxMult * 100) + "%)";
            taxColor = Formatting.YELLOW;
        } else if (taxMult > 1) {
            taxStatus = "Increased (" + (int) (taxMult * 100) + "%)";
            taxColor = Formatting.RED;
        } else {
            taxStatus = "Normal (100%)";
            taxColor = Formatting.WHITE;
        }

        gui.setSlot(24, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("Tax Info").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Status: " + taxStatus).formatted(taxColor))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Tax Brackets:").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("  0-10K: 0.1%").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("  10K-100K: 0.3%").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("  100K-1M: 0.5%").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal("  1M+: 1.0%").formatted(Formatting.RED))
                .build());

        // ═══════════════════════════════════════════════════════
        // ROW 4 (slots 27-35): Instructions
        // ═══════════════════════════════════════════════════════

        gui.setSlot(31, new GuiElementBuilder(Items.OAK_SIGN)
                .setName(Text.literal("How to Sell").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("1. Click item in inventory below").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("2. Click gold ingot to set price").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("3. Click confirm to list!").formatted(Formatting.WHITE))
                .build());

        // ═══════════════════════════════════════════════════════
        // ROW 5 (slots 36-44): Action buttons
        // ═══════════════════════════════════════════════════════

        // Confirm Button (left)
        boolean canConfirm = pendingItem != null && !pendingItem.isEmpty() && currentPrice != null && currentPrice > 0;
        gui.setSlot(38, new GuiElementBuilder(canConfirm ? Items.LIME_CONCRETE : Items.LIGHT_GRAY_CONCRETE)
                .setName(Text.literal("Confirm Listing").formatted(canConfirm ? Formatting.GREEN : Formatting.GRAY, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(canConfirm
                        ? Text.literal("Click to list your item!").formatted(Formatting.YELLOW)
                        : Text.literal("Select item and set price first").formatted(Formatting.RED))
                .glow(canConfirm)
                .setCallback((index, type, action) -> {
                    ItemStack pItem = pendingSellItems.get(player.getUuid());
                    Long pPrice = pendingSellPrice.get(player.getUuid());
                    if (pItem != null && !pItem.isEmpty() && pPrice != null && pPrice > 0) {
                        confirmListing(player);
                    }
                })
                .build());

        // Cancel Button (center)
        gui.setSlot(40, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("Cancel").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Return to main menu").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Item will be returned").formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> {
                    returnPendingItem(player);
                    openMainMenu(player);
                })
                .build());

        // Clear Item Button (right)
        if (pendingItem != null && !pendingItem.isEmpty()) {
            gui.setSlot(42, new GuiElementBuilder(Items.HONEY_BOTTLE)
                    .setName(Text.literal("Remove Item").formatted(Formatting.GOLD, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Return item to inventory").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("and select a different one").formatted(Formatting.GRAY))
                    .setCallback((index, type, action) -> {
                        returnPendingItem(player);
                        openSellMenu(player);
                    })
                    .build());
        }

        // ═══════════════════════════════════════════════════════
        // ROW 6 (slots 45-53): Bottom border
        // ═══════════════════════════════════════════════════════
        for (int i = 45; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Balance display in bottom corner
        int credits = CreditItem.countCredits(player);
        int coins = CoinManager.getCoins(player);
        gui.setSlot(53, new GuiElementBuilder(Items.GOLD_NUGGET)
                .setName(Text.literal("Your Balance").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Credits: " + formatNumber(credits)).formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("Coins: " + formatNumber(coins)).formatted(Formatting.YELLOW))
                .build());

        gui.open();
    }

    private static void confirmListing(ServerPlayerEntity player) {
        ItemStack item = pendingSellItems.get(player.getUuid());
        Long price = pendingSellPrice.get(player.getUuid());
        Boolean inCoins = pendingSellInCoins.get(player.getUuid());

        if (item == null || item.isEmpty() || price == null || price <= 0 || inCoins == null) {
            player.sendMessage(Text.literal("✗ Invalid listing data!").formatted(Formatting.RED));
            return;
        }

        long listingTax = calculateListingTax(price);

        if (listingTax > 0) {
            if (inCoins) {
                if (!CoinManager.hasCoins(player, (int) Math.min(listingTax, Integer.MAX_VALUE))) {
                    player.sendMessage(Text.literal("✗ Not enough coins for tax (" + formatNumber(listingTax) + ")!").formatted(Formatting.RED));
                    return;
                }
                CoinManager.removeCoins(player, (int) Math.min(listingTax, Integer.MAX_VALUE));
            } else {
                if (!CreditItem.hasCredits(player, (int) Math.min(listingTax, Integer.MAX_VALUE))) {
                    player.sendMessage(Text.literal("✗ Not enough credits for tax (" + formatNumber(listingTax) + ")!").formatted(Formatting.RED));
                    return;
                }
                CreditItem.removeCredits(player, (int) Math.min(listingTax, Integer.MAX_VALUE));
            }
        }

        AuctionHouseManager.AuctionListing listing = new AuctionHouseManager.AuctionListing(
                player.getUuidAsString(),
                player.getName().getString(),
                item,
                price,
                inCoins
        );
        AuctionHouseManager.addListing(listing);

        pendingSellItems.remove(player.getUuid());
        pendingSellPrice.remove(player.getUuid());
        pendingSellInCoins.remove(player.getUuid());

        String curr = inCoins ? "coins" : "credits";
        player.sendMessage(Text.literal("✓ Listed for " + formatNumber(price) + " " + curr + "!").formatted(Formatting.GREEN));
        if (listingTax > 0) {
            player.sendMessage(Text.literal("  (Tax: " + formatNumber(listingTax) + " " + curr + ")").formatted(Formatting.GRAY));
        }

        openMainMenu(player);
    }

    // ════════════════════════════════════════════════════════════════════
    // MY LISTINGS
    // ════════════════════════════════════════════════════════════════════

    public static void openMyListings(ServerPlayerEntity player, int page) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("My Listings"));

        // Fill background
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Get player's listings
        List<AuctionHouseManager.AuctionListing> myListings = new ArrayList<>();
        for (AuctionHouseManager.AuctionListing listing : AuctionHouseManager.getListings()) {
            if (listing.sellerUuid.equals(player.getUuidAsString())) {
                myListings.add(listing);
            }
        }

        int totalPages = Math.max(1, (int) Math.ceil((double) myListings.size() / 21));
        int startIndex = page * 21;
        int endIndex = Math.min(startIndex + 21, myListings.size());

        int[] slots = {
                0, 1, 2, 3, 4, 5, 6, 7, 8,
                9, 10, 11, 12, 13, 14, 15, 16, 17,
                18, 19, 20
        };

        int slotIndex = 0;
        for (int i = startIndex; i < endIndex && slotIndex < slots.length; i++) {
            AuctionHouseManager.AuctionListing listing = myListings.get(i);
            ItemStack displayStack = listing.getItemStack();

            if (displayStack.isEmpty()) continue;

            final String listingId = listing.id;
            final int fPage = page;

            gui.setSlot(slots[slotIndex], new GuiElementBuilder(displayStack.getItem())
                    .setCount(Math.min(displayStack.getCount(), 64))
                    .setName(displayStack.getName().copy().formatted(Formatting.WHITE))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Price: " + formatNumber(listing.price) + (listing.priceInCoins ? " coins" : " credits")).formatted(Formatting.GOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Click to CANCEL").formatted(Formatting.RED))
                    .setCallback((index, type, action) -> {
                        cancelListing(player, listingId);
                        openMyListings(player, fPage);
                    })
                    .build());

            slotIndex++;
        }

        // Empty state
        if (myListings.isEmpty()) {
            gui.setSlot(13, new GuiElementBuilder(Items.BARRIER)
                    .setName(Text.literal("No Listings").formatted(Formatting.RED, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("You have no active listings").formatted(Formatting.GRAY))
                    .build());
        }

        // Navigation
        gui.setSlot(27, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("Back").formatted(Formatting.RED))
                .setCallback((index, type, action) -> openMainMenu(player))
                .build());

        if (page > 0) {
            final int prevPage = page - 1;
            gui.setSlot(30, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("◀ Previous").formatted(Formatting.YELLOW))
                    .setCallback((index, type, action) -> openMyListings(player, prevPage))
                    .build());
        }

        gui.setSlot(31, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("Page " + (page + 1) + "/" + totalPages).formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Total: " + myListings.size() + " listings").formatted(Formatting.GRAY))
                .build());

        if (page < totalPages - 1) {
            final int nextPage = page + 1;
            gui.setSlot(32, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("Next ▶").formatted(Formatting.YELLOW))
                    .setCallback((index, type, action) -> openMyListings(player, nextPage))
                    .build());
        }

        gui.open();
    }

    private static void cancelListing(ServerPlayerEntity player, String listingId) {
        AuctionHouseManager.AuctionListing listing = AuctionHouseManager.getListing(listingId);
        if (listing == null) {
            player.sendMessage(Text.literal("✗ Listing not found!").formatted(Formatting.RED));
            return;
        }

        if (!player.getUuidAsString().equals(listing.sellerUuid)) {
            player.sendMessage(Text.literal("✗ Not your listing!").formatted(Formatting.RED));
            return;
        }

        ItemStack item = listing.getItemStack();
        AuctionHouseManager.removeListing(listingId);

        if (!item.isEmpty()) {
            if (!player.getInventory().insertStack(item.copy())) {
                player.dropItem(item.copy(), false);
            }
        }

        player.sendMessage(Text.literal("✓ Listing cancelled! Item returned.").formatted(Formatting.YELLOW));
    }

    // ════════════════════════════════════════════════════════════════════
    // PRICE SIGN
    // ════════════════════════════════════════════════════════════════════

    private static void openPriceSign(ServerPlayerEntity player) {
        ItemStack pendingItem = pendingSellItems.get(player.getUuid());
        Boolean inCoins = pendingSellInCoins.get(player.getUuid());

        if (pendingItem == null || pendingItem.isEmpty()) {
            player.sendMessage(Text.literal("✗ No item selected!").formatted(Formatting.RED));
            openSellMenu(player);
            return;
        }

        if (inCoins == null) {
            inCoins = false;
        }

        final String currencyName = inCoins ? "coins" : "credits";

        SignGui signGui = new SignGui(player) {
            @Override
            public void onClose() {
                String priceText = this.getLine(0).getString().trim();
                priceText = priceText.replaceAll("[^0-9]", "");

                if (priceText.isEmpty()) {
                    player.sendMessage(Text.literal("✗ No price entered!").formatted(Formatting.RED));
                    openSellMenu(player);
                    return;
                }

                try {
                    long price = Long.parseLong(priceText);

                    if (price <= 0) {
                        player.sendMessage(Text.literal("✗ Price must be greater than 0!").formatted(Formatting.RED));
                        openSellMenu(player);
                        return;
                    }

                    if (price > MAX_PRICE) {
                        player.sendMessage(Text.literal("✗ Max price is 1 billion!").formatted(Formatting.RED));
                        openSellMenu(player);
                        return;
                    }

                    pendingSellPrice.put(player.getUuid(), price);
                    player.sendMessage(Text.literal("✓ Price set to " + formatNumber(price) + " " + currencyName + "!").formatted(Formatting.GREEN));
                    openSellMenu(player);

                } catch (NumberFormatException e) {
                    player.sendMessage(Text.literal("✗ Invalid number!").formatted(Formatting.RED));
                    openSellMenu(player);
                }
            }
        };

        signGui.setLine(0, Text.literal(""));
        signGui.setLine(1, Text.literal("^^^^^^^^"));
        signGui.setLine(2, Text.literal("Enter price"));
        signGui.setLine(3, Text.literal("in " + currencyName));

        signGui.open();
    }

    // ════════════════════════════════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════════════════════════════════

    private static void returnPendingItem(ServerPlayerEntity player) {
        ItemStack pending = pendingSellItems.remove(player.getUuid());
        pendingSellPrice.remove(player.getUuid());
        pendingSellInCoins.remove(player.getUuid());
        if (pending != null && !pending.isEmpty()) {
            if (!player.getInventory().insertStack(pending.copy())) {
                player.dropItem(pending.copy(), false);
            }
        }
    }

    public static void onPlayerDisconnect(ServerPlayerEntity player) {
        returnPendingItem(player);
    }

    private static String formatNumber(long number) {
        if (number >= 1_000_000_000) {
            return String.format("%.2fB", number / 1_000_000_000.0);
        } else if (number >= 1_000_000) {
            return String.format("%.2fM", number / 1_000_000.0);
        } else if (number >= 1_000) {
            return String.format("%.1fK", number / 1_000.0);
        }
        return String.valueOf(number);
    }

    private static Category getItemCategory(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return Category.MISC;

        Item item = stack.getItem();

        // Check for enchantments first
        if (stack.hasEnchantments()) {
            return Category.ENCHANTED;
        }

        // Weapons
        if (item == Items.DIAMOND_SWORD || item == Items.NETHERITE_SWORD ||
                item == Items.IRON_SWORD || item == Items.GOLDEN_SWORD ||
                item == Items.STONE_SWORD || item == Items.WOODEN_SWORD ||
                item == Items.BOW || item == Items.CROSSBOW || item == Items.TRIDENT ||
                item == Items.MACE) {
            return Category.WEAPONS;
        }

        // Armor
        if (item == Items.DIAMOND_HELMET || item == Items.DIAMOND_CHESTPLATE ||
                item == Items.DIAMOND_LEGGINGS || item == Items.DIAMOND_BOOTS ||
                item == Items.NETHERITE_HELMET || item == Items.NETHERITE_CHESTPLATE ||
                item == Items.NETHERITE_LEGGINGS || item == Items.NETHERITE_BOOTS ||
                item == Items.IRON_HELMET || item == Items.IRON_CHESTPLATE ||
                item == Items.IRON_LEGGINGS || item == Items.IRON_BOOTS ||
                item == Items.GOLDEN_HELMET || item == Items.GOLDEN_CHESTPLATE ||
                item == Items.GOLDEN_LEGGINGS || item == Items.GOLDEN_BOOTS ||
                item == Items.CHAINMAIL_HELMET || item == Items.CHAINMAIL_CHESTPLATE ||
                item == Items.CHAINMAIL_LEGGINGS || item == Items.CHAINMAIL_BOOTS ||
                item == Items.LEATHER_HELMET || item == Items.LEATHER_CHESTPLATE ||
                item == Items.LEATHER_LEGGINGS || item == Items.LEATHER_BOOTS ||
                item == Items.TURTLE_HELMET || item == Items.ELYTRA || item == Items.SHIELD) {
            return Category.ARMOR;
        }

        // Tools
        if (item == Items.DIAMOND_PICKAXE || item == Items.DIAMOND_AXE ||
                item == Items.DIAMOND_SHOVEL || item == Items.DIAMOND_HOE ||
                item == Items.NETHERITE_PICKAXE || item == Items.NETHERITE_AXE ||
                item == Items.NETHERITE_SHOVEL || item == Items.NETHERITE_HOE ||
                item == Items.IRON_PICKAXE || item == Items.IRON_AXE ||
                item == Items.IRON_SHOVEL || item == Items.IRON_HOE ||
                item == Items.GOLDEN_PICKAXE || item == Items.GOLDEN_AXE ||
                item == Items.GOLDEN_SHOVEL || item == Items.GOLDEN_HOE ||
                item == Items.STONE_PICKAXE || item == Items.STONE_AXE ||
                item == Items.STONE_SHOVEL || item == Items.STONE_HOE ||
                item == Items.WOODEN_PICKAXE || item == Items.WOODEN_AXE ||
                item == Items.WOODEN_SHOVEL || item == Items.WOODEN_HOE ||
                item == Items.FISHING_ROD || item == Items.SHEARS || item == Items.FLINT_AND_STEEL) {
            return Category.TOOLS;
        }

        // Food
        if (stack.contains(DataComponentTypes.FOOD)) {
            return Category.FOOD;
        }

        // Potions
        if (item == Items.POTION || item == Items.SPLASH_POTION ||
                item == Items.LINGERING_POTION || item == Items.GLASS_BOTTLE) {
            return Category.POTIONS;
        }

        // Materials
        if (item == Items.DIAMOND || item == Items.EMERALD || item == Items.GOLD_INGOT ||
                item == Items.IRON_INGOT || item == Items.COPPER_INGOT || item == Items.NETHERITE_INGOT ||
                item == Items.COAL || item == Items.REDSTONE || item == Items.LAPIS_LAZULI ||
                item == Items.QUARTZ || item == Items.AMETHYST_SHARD || item == Items.NETHERITE_SCRAP ||
                item == Items.RAW_IRON || item == Items.RAW_GOLD || item == Items.RAW_COPPER ||
                item == Items.LEATHER || item == Items.STRING || item == Items.FEATHER ||
                item == Items.GUNPOWDER || item == Items.BLAZE_ROD || item == Items.ENDER_PEARL ||
                item == Items.SLIME_BALL || item == Items.BONE || item == Items.GLOWSTONE_DUST) {
            return Category.MATERIALS;
        }

        // Blocks
        if (item instanceof BlockItem) {
            return Category.BLOCKS;
        }

        return Category.MISC;
    }
}