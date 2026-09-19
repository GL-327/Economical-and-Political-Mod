package com.political.economy;
import com.political.BuffProgressManager;
import com.political.CreditItem;
import com.political.PoliticalServer;
import com.political.government.Treasury;



import com.political.government.PerkManager;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SignGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import eu.pb4.sgui.api.ClickType;

public class AuctionHouseGui {

    private static final int ITEMS_PER_PAGE = 21;
    private static final long COINS_PER_CREDIT = 1000L;
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
        // SPAWNER removed - now purchasable in shop with silk touch
        UNSELLABLE_ITEMS.add(Items.KNOWLEDGE_BOOK);
        UNSELLABLE_ITEMS.add(Items.WRITTEN_BOOK);
    }

    // Categories
    public enum Category {
        ALL("All Items", Items.CHEST, ChatFormatting.WHITE),
        WEAPONS("Weapons", Items.DIAMOND_SWORD, ChatFormatting.RED),
        ARMOR("Armor", Items.DIAMOND_CHESTPLATE, ChatFormatting.BLUE),
        TOOLS("Tools", Items.DIAMOND_PICKAXE, ChatFormatting.YELLOW),
        FOOD("Food", Items.GOLDEN_APPLE, ChatFormatting.GOLD),
        BLOCKS("Blocks", Items.STONE, ChatFormatting.GRAY),
        MATERIALS("Materials", Items.DIAMOND, ChatFormatting.AQUA),
        POTIONS("Potions", Items.POTION, ChatFormatting.LIGHT_PURPLE),
        ENCHANTED("Enchanted", Items.ENCHANTED_BOOK, ChatFormatting.DARK_PURPLE),
        MISC("Misc", Items.ENDER_PEARL, ChatFormatting.GREEN);

        public final String displayName;
        public final Item icon;
        public final ChatFormatting color;

        Category(String displayName, Item icon, ChatFormatting color) {
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
        float m = 1.0f;
        if (PerkManager.hasActivePerk("AUCTION_TAX_REDUCTION")) {
            m = 0.5f;
        }
        if (PerkManager.hasActivePerk("AUCTION_TAX_INCREASE")) {
            m = 1.5f;
        }
        if (PerkManager.hasActivePerk("EMPTY_COFFERS")) {
            m *= 1.1f;
        }
        return m;
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
        return com.political.government.Treasury.vatOn(price);
    }

    // ════════════════════════════════════════════════════════════════════
    // MAIN MENU - Buy or Sell
    // ════════════════════════════════════════════════════════════════════

    public static void open(ServerPlayer player) {
        openMainMenu(player);
    }

    public static void openMainMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("Auction House"));

        // Fill background
        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        // Top row decoration
        for (int i = 0; i < 9; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        // Bottom row decoration
        for (int i = 18; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        // Balance display (top center)
        int credits = CreditItem.countCredits(player);
        int coins = CoinManager.getCoins(player);
        gui.setSlot(4, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Component.literal("Your Balance").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Credits: " + formatNumber(credits)).withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("Coins: " + formatNumber(coins)).withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("10,000 coins = 1 credit").withStyle(ChatFormatting.GRAY))
                .glow()
                .build());

        // Browse Auctions (left)
        gui.setSlot(11, new GuiElementBuilder(Items.EMERALD)
                .setName(Component.literal("Browse Auctions").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("View items for sale").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Buy with coins or credits").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to browse!").withStyle(ChatFormatting.YELLOW))
                .glow()
                .setCallback((index, type, containerInput, slotGui) -> openBuyMenu(player, 0, Category.ALL))
                .build());

        // Create Auction (center)
        gui.setSlot(13, new GuiElementBuilder(Items.CHEST)
                .setName(Component.literal("Create Auction").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("List your items for sale").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Set your own price!").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to sell!").withStyle(ChatFormatting.YELLOW))
                .glow()
                .setCallback((index, type, containerInput, slotGui) -> openSellMenu(player))
                .build());

        // My Listings (right)
        gui.setSlot(15, new GuiElementBuilder(Items.BOOK)
                .setName(Component.literal("My Listings").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("View your active listings").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Cancel listings here").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to view!").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> openMyListings(player, 0))
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════════════
    // BUY MENU - Browse Auctions with Categories on Side
    // ════════════════════════════════════════════════════════════════════

    public static void openBuyMenu(ServerPlayer player, int page, Category category) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Auctions - " + category.displayName));

        // Fill background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        // Categories on left side (column 0)
        int categorySlot = 0;
        for (Category cat : Category.values()) {
            if (categorySlot >= 54) break;

            boolean isSelected = cat == category;
            final Category clickedCat = cat;

            gui.setSlot(categorySlot, new GuiElementBuilder(cat.icon)
                    .setName(Component.literal(cat.displayName).withStyle(cat.color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(isSelected
                            ? Component.literal("✓ Currently viewing").withStyle(ChatFormatting.GREEN)
                            : Component.literal("Click to view").withStyle(ChatFormatting.YELLOW))
                    .glow(isSelected)
                    .setCallback((index, type, containerInput, slotGui) -> openBuyMenu(player, 0, clickedCat))
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
            boolean isOwnListing = player.getUUID().toString().equals(listing.sellerUuid);

            long creditEquivalent = priceInCoins ? Math.max(1, price / COINS_PER_CREDIT) : price;
            long coinEquivalent = priceInCoins ? price : (price * COINS_PER_CREDIT);
            boolean canAfford = playerCredits >= creditEquivalent || playerCoins >= coinEquivalent;

            GuiElementBuilder builder = new GuiElementBuilder(displayStack.getItem())
                    .setCount(Math.min(displayStack.getCount(), 64))
                    .setName(displayStack.getHoverName().copy().withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("━━━━━━━━━━━━━━━━").withStyle(ChatFormatting.DARK_GRAY));

            if (priceInCoins) {
                builder.addLoreLine(Component.literal("Price: " + formatNumber(price) + " coins").withStyle(ChatFormatting.YELLOW));
                builder.addLoreLine(Component.literal("  (~" + formatNumber(creditEquivalent) + " credits)").withStyle(ChatFormatting.DARK_GRAY));
            } else {
                builder.addLoreLine(Component.literal("Price: " + formatNumber(price) + " credits").withStyle(ChatFormatting.GREEN));
                builder.addLoreLine(Component.literal("  (~" + formatNumber(coinEquivalent) + " coins)").withStyle(ChatFormatting.DARK_GRAY));
            }

            builder.addLoreLine(Component.literal("Seller: " + listing.sellerName).withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("Qty: x" + displayStack.getCount()).withStyle(ChatFormatting.AQUA))
                    .addLoreLine(Component.literal("━━━━━━━━━━━━━━━━").withStyle(ChatFormatting.DARK_GRAY))
                    .addLoreLine(Component.literal(""));

            if (isOwnListing) {
                builder.addLoreLine(Component.literal("This is your listing").withStyle(ChatFormatting.YELLOW));
            } else if (canAfford) {
                builder.addLoreLine(Component.literal("Click to buy!").withStyle(ChatFormatting.GREEN));
            } else {
                builder.addLoreLine(Component.literal("Cannot afford").withStyle(ChatFormatting.RED));
            }

            final String fListingId = listingId;
            final int fPage = page;
            final Category fCategory = category;
            final boolean fCanAfford = canAfford;
            final boolean fIsOwn = isOwnListing;

            builder.setCallback((index, type, containerInput, slotGui) -> {
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
                    .setName(Component.literal("No Listings").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("No items in this category").withStyle(ChatFormatting.GRAY))
                    .build());
        }

        // Bottom navigation bar

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("Back to Menu").withStyle(ChatFormatting.RED))
                .setCallback((index, type, containerInput, slotGui) -> openMainMenu(player))
                .build());

        // Previous page
        if (page > 0) {
            final int prevPage = page - 1;
            final Category fCat = category;
            gui.setSlot(48, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("◀ Previous Page").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> openBuyMenu(player, prevPage, fCat))
                    .build());
        }

        // Page info
        gui.setSlot(49, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("Page " + (page + 1) + "/" + totalPages).withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Total: " + filteredListings.size() + " listings").withStyle(ChatFormatting.GRAY))
                .build());

        // Next page
        if (page < totalPages - 1) {
            final int nextPage = page + 1;
            final Category fCat = category;
            gui.setSlot(50, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Next Page ▶").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> openBuyMenu(player, nextPage, fCat))
                    .build());
        }

        // Refresh
        final int fPage = page;
        final Category fCat = category;
        gui.setSlot(53, new GuiElementBuilder(Items.SUNFLOWER)
                .setName(Component.literal("Refresh").withStyle(ChatFormatting.AQUA))
                .setCallback((index, type, containerInput, slotGui) -> openBuyMenu(player, fPage, fCat))
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════════════
    // PURCHASE CONFIRMATION
    // ════════════════════════════════════════════════════════════════════

    private static void openPurchaseConfirmation(ServerPlayer player, String listingId, int returnPage, Category returnCategory) {
        AuctionHouseManager.AuctionListing listing = AuctionHouseManager.getListing(listingId);
        if (listing == null) {
            player.sendSystemMessage(Component.literal("Listing no longer exists!").withStyle(ChatFormatting.RED));
            openBuyMenu(player, returnPage, returnCategory);
            return;
        }

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("Confirm Purchase"));

        // Fill background
        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
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
                .setName(displayStack.getHoverName().copy().withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Price: " + formatNumber(price) + (priceInCoins ? " coins" : " credits")).withStyle(ChatFormatting.GOLD))
                .addLoreLine(Component.literal("Tax: " + formatNumber(tax) + (priceInCoins ? " coins" : " credits")).withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal("Total: " + formatNumber(totalPrice) + (priceInCoins ? " coins" : " credits")).withStyle(ChatFormatting.YELLOW))
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
                .setName(Component.literal("Pay with Credits").withStyle(canAffordCredits ? ChatFormatting.GREEN : ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Cost: " + formatNumber(creditPrice) + " credits").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("You have: " + formatNumber(playerCredits)).withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(canAffordCredits ? Component.literal("Click to buy!").withStyle(ChatFormatting.YELLOW) : Component.literal("Not enough credits!").withStyle(ChatFormatting.RED))
                .glow(canAffordCredits)
                .setCallback((index, type, containerInput, slotGui) -> {
                    if (canAffordCredits) {
                        purchaseItem(player, fListingId, false);
                        openBuyMenu(player, fReturnPage, fReturnCategory);
                    }
                })
                .build());

        // Pay with Coins
        gui.setSlot(15, new GuiElementBuilder(canAffordCoins ? Items.GOLD_INGOT : Items.BARRIER)
                .setName(Component.literal("Pay with Coins").withStyle(canAffordCoins ? ChatFormatting.YELLOW : ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Cost: " + formatNumber(coinPrice) + " coins").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("You have: " + formatNumber(playerCoins)).withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(canAffordCoins ? Component.literal("Click to buy!").withStyle(ChatFormatting.YELLOW) : Component.literal("Not enough coins!").withStyle(ChatFormatting.RED))
                .glow(canAffordCoins)
                .setCallback((index, type, containerInput, slotGui) -> {
                    if (canAffordCoins) {
                        purchaseItem(player, fListingId, true);
                        openBuyMenu(player, fReturnPage, fReturnCategory);
                    }
                })
                .build());

        // Cancel
        gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Cancel").withStyle(ChatFormatting.RED))
                .setCallback((index, type, containerInput, slotGui) -> openBuyMenu(player, fReturnPage, fReturnCategory))
                .build());

        gui.open();
    }

    private static void purchaseItem(ServerPlayer buyer, String listingId, boolean payWithCoins) {
        AuctionHouseManager.AuctionListing listing = AuctionHouseManager.getListing(listingId);
        if (listing == null) {
            buyer.sendSystemMessage(Component.literal("Listing no longer exists!").withStyle(ChatFormatting.RED));
            return;
        }

        if (buyer.getUUID().toString().equals(listing.sellerUuid)) {
            buyer.sendSystemMessage(Component.literal("Cannot buy your own listing!").withStyle(ChatFormatting.RED));
            return;
        }

        long price = listing.price;
        boolean priceInCoins = listing.priceInCoins;
        long tax = calculatePurchaseTax(price);
        long totalPrice = price + tax;

        long creditPrice = priceInCoins ? Math.max(1, (totalPrice + COINS_PER_CREDIT - 1) / COINS_PER_CREDIT) : totalPrice;
        long coinPrice = priceInCoins ? totalPrice : (totalPrice * COINS_PER_CREDIT);

        ItemStack item = listing.getItemStack();
        if (item.isEmpty()) {
            PoliticalServer.LOGGER.error("Auction purchase failed: could not deserialize item for listing " + listingId);
            buyer.sendSystemMessage(Component.literal("Purchase failed: item could not be loaded.").withStyle(ChatFormatting.RED));
            return;
        }

        if (payWithCoins) {
            if (!CoinManager.hasCoins(buyer, coinPrice)) {
                buyer.sendSystemMessage(Component.literal("Not enough coins!").withStyle(ChatFormatting.RED));
                return;
            }
            CoinManager.removeCoins(buyer, coinPrice);
            if (priceInCoins) {
                CoinManager.giveCoins(listing.sellerUuid, price);
            } else {
                CreditItem.giveCredits(listing.sellerUuid, (int) Math.min(price, Integer.MAX_VALUE));
            }
        } else {
            if (!CreditItem.hasCredits(buyer, (int) Math.min(creditPrice, Integer.MAX_VALUE))) {
                buyer.sendSystemMessage(Component.literal("Not enough credits!").withStyle(ChatFormatting.RED));
                return;
            }
            CreditItem.removeCredits(buyer, (int) Math.min(creditPrice, Integer.MAX_VALUE));
            if (priceInCoins) {
                CoinManager.giveCoins(listing.sellerUuid, price);
            } else {
                CreditItem.giveCredits(listing.sellerUuid, (int) Math.min(price, Integer.MAX_VALUE));
            }
        }
        if (tax > 0) {
            com.political.government.Treasury.deposit(tax, "auction vat");
        }

        AuctionHouseManager.removeListing(listingId);

        // insertStack modifies the stack in place; if the stack is not fully inserted,
        // drop whatever remains so the buyer never loses the item.
        ItemStack toGive = item.copy();
        if (!buyer.addItem(toGive)) {
            // toGive may have a reduced count after a partial insert — drop what's left
            if (!toGive.isEmpty()) {
                buyer.drop(toGive, false);
            }
        }

        String currencyUsed = payWithCoins ? "coins" : "credits";
        long amountPaid = payWithCoins ? coinPrice : creditPrice;
        buyer.sendSystemMessage(Component.literal("✓ Purchased for " + formatNumber(amountPaid) + " " + currencyUsed + "!").withStyle(ChatFormatting.GREEN));

        BuffProgressManager.recordAuctionWon(buyer);

        long listingEnd = listing.listingTime + AuctionHouseManager.LISTING_MAX_DURATION_MS;
        long remainingMs = listingEnd - System.currentTimeMillis();
        if (remainingMs >= 0 && remainingMs <= 10_000L) {
            BuffProgressManager.recordSnipedAuction(buyer);
        }

        ServerPlayer seller = PoliticalServer.server.getPlayerList().getPlayer(java.util.UUID.fromString(listing.sellerUuid));
        if (seller != null) {
            String sellerCurrency = priceInCoins ? "coins" : "credits";
            seller.sendSystemMessage(Component.literal("✓ Sold " + item.getHoverName().getString() + " for " + formatNumber(price) + " " + sellerCurrency + "!").withStyle(ChatFormatting.GREEN));
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // SELL MENU - Create Auction
    // ════════════════════════════════════════════════════════════════════

    public static void openSellMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false) {
            @Override
            public boolean onAnyClick(int index, ClickType type, net.minecraft.world.inventory.ContainerInput action) {
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

                    if (realSlot >= 0 && realSlot < player.getInventory().getContainerSize()) {
                        ItemStack clicked = player.getInventory().getItem(realSlot);
                        if (!clicked.isEmpty() && !UNSELLABLE_ITEMS.contains(clicked.getItem())) {
                            // Return existing pending item first
                            ItemStack existing = pendingSellItems.get(player.getUUID());
                            if (existing != null && !existing.isEmpty()) {
                                if (!player.addItem(existing.copy())) {
                                    player.drop(existing.copy(), false);
                                }
                            }

                            // Take the clicked item
                            pendingSellItems.put(player.getUUID(), clicked.copy());
                            pendingSellPrice.remove(player.getUUID());
                            pendingSellInCoins.remove(player.getUUID());
                            player.getInventory().setItem(realSlot, ItemStack.EMPTY);
                            player.sendSystemMessage(Component.literal("✓ Item selected! Now set a price.").withStyle(ChatFormatting.GREEN));

                            // Refresh the GUI
                            openSellMenu(player);
                            return false;
                        } else if (!clicked.isEmpty()) {
                            player.sendSystemMessage(Component.literal("✗ This item cannot be sold!").withStyle(ChatFormatting.RED));
                            return false;
                        }
                    }
                }
                return super.onAnyClick(index, type, action);
            }
        };


        // ... rest of method stays the same

        gui.setTitle(Component.literal("Create Auction"));



        // Fill entire GUI with background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        // Top decorative row
        for (int i = 0; i < 9; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        // ═══════════════════════════════════════════════════════
        // ROW 1 (slots 0-8): Header with item slot in center
        // ═══════════════════════════════════════════════════════

        ItemStack pendingItem = pendingSellItems.get(player.getUUID());
        if (pendingItem != null && !pendingItem.isEmpty()) {
            gui.setSlot(4, new GuiElementBuilder(pendingItem.getItem())
                    .setCount(Math.min(pendingItem.getCount(), 64))
                    .setName(Component.literal("✦ Item to Sell ✦").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(pendingItem.getHoverName().copy().withStyle(ChatFormatting.WHITE))
                    .addLoreLine(Component.literal("Quantity: x" + pendingItem.getCount()).withStyle(ChatFormatting.AQUA))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to remove").withStyle(ChatFormatting.RED))
                    .glow()
                    .setCallback((index, type, containerInput, slotGui) -> {
                        returnPendingItem(player);
                        openSellMenu(player);
                    })
                    .build());
        } else {
            gui.setSlot(4, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal("Item Slot").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click an item in your").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("inventory to place it here").withStyle(ChatFormatting.GRAY))
                    .build());
        }

        // ═══════════════════════════════════════════════════════
        // ROW 3 (slots 18-26): Price controls
        // ═══════════════════════════════════════════════════════

        Long currentPrice = pendingSellPrice.get(player.getUUID());
        Boolean inCoins = pendingSellInCoins.get(player.getUUID());
        String priceDisplay = currentPrice != null ? formatNumber(currentPrice) : "Not set";
        String currencyDisplay = inCoins != null ? (inCoins ? "coins" : "credits") : "";

        // Set Price Button (left side)
        gui.setSlot(20, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Component.literal("Set Price").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(currentPrice != null
                        ? Component.literal("Current: " + priceDisplay + " " + currencyDisplay).withStyle(ChatFormatting.YELLOW)
                        : Component.literal("No price set").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Left-click: Price in Coins").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("Right-click: Price in Credits").withStyle(ChatFormatting.GREEN))
                .glow()
                .setCallback((index, type, containerInput, slotGui) -> {
                    ItemStack pItem = pendingSellItems.get(player.getUUID());
                    if (pItem == null || pItem.isEmpty()) {
                        player.sendSystemMessage(Component.literal("✗ Select an item first!").withStyle(ChatFormatting.RED));
                        return;
                    }
                    pendingSellInCoins.put(player.getUUID(), type.isLeft);
                    openPriceSign(player);
                })
                .build());

        // Listing Preview (center)
        if (currentPrice != null && currentPrice > 0 && inCoins != null) {
            long tax = calculateListingTax(currentPrice);
            String curr = inCoins ? "coins" : "credits";

            gui.setSlot(22, new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
                    .setName(Component.literal("Listing Preview").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Price: " + formatNumber(currentPrice) + " " + curr).withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal("Tax: " + formatNumber(tax) + " " + curr).withStyle(ChatFormatting.RED))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("You receive: " + formatNumber(currentPrice) + " " + curr).withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal("(Tax paid upfront)").withStyle(ChatFormatting.GRAY))
                    .glow()
                    .build());
        } else {
            gui.setSlot(22, new GuiElementBuilder(Items.GLASS_BOTTLE)
                    .setName(Component.literal("Listing Preview").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Set a price to see preview").withStyle(ChatFormatting.DARK_GRAY))
                    .build());
        }

        // Tax Info (right side)
        float taxMult = getTaxMultiplier();
        String taxStatus;
        ChatFormatting taxColor;
        if (taxMult == 0) {
            taxStatus = "TAX FREE!";
            taxColor = ChatFormatting.GREEN;
        } else if (taxMult < 1) {
            taxStatus = "Reduced (" + (int) (taxMult * 100) + "%)";
            taxColor = ChatFormatting.YELLOW;
        } else if (taxMult > 1) {
            taxStatus = "Increased (" + (int) (taxMult * 100) + "%)";
            taxColor = ChatFormatting.RED;
        } else {
            taxStatus = "Normal (100%)";
            taxColor = ChatFormatting.WHITE;
        }

        gui.setSlot(24, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("Tax Info").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Status: " + taxStatus).withStyle(taxColor))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Tax Brackets:").withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal("  0-10K: 0.1%").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("  10K-100K: 0.3%").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("  100K-1M: 0.5%").withStyle(ChatFormatting.GOLD))
                .addLoreLine(Component.literal("  1M+: 1.0%").withStyle(ChatFormatting.RED))
                .build());

        // ═══════════════════════════════════════════════════════
        // ROW 4 (slots 27-35): Instructions
        // ═══════════════════════════════════════════════════════

        gui.setSlot(31, new GuiElementBuilder(Items.OAK_SIGN)
                .setName(Component.literal("How to Sell").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("1. Click item in inventory below").withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal("2. Click gold ingot to set price").withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal("3. Click confirm to list!").withStyle(ChatFormatting.WHITE))
                .build());

        // ═══════════════════════════════════════════════════════
        // ROW 5 (slots 36-44): Action buttons
        // ═══════════════════════════════════════════════════════

        // Confirm Button (left)
        boolean canConfirm = pendingItem != null && !pendingItem.isEmpty() && currentPrice != null && currentPrice > 0;
        gui.setSlot(38, new GuiElementBuilder(canConfirm ? Items.LIME_CONCRETE : Items.LIGHT_GRAY_CONCRETE)
                .setName(Component.literal("Confirm Listing").withStyle(canConfirm ? ChatFormatting.GREEN : ChatFormatting.GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(canConfirm
                        ? Component.literal("Click to list your item!").withStyle(ChatFormatting.YELLOW)
                        : Component.literal("Select item and set price first").withStyle(ChatFormatting.RED))
                .glow(canConfirm)
                .setCallback((index, type, containerInput, slotGui) -> {
                    ItemStack pItem = pendingSellItems.get(player.getUUID());
                    Long pPrice = pendingSellPrice.get(player.getUUID());
                    if (pItem != null && !pItem.isEmpty() && pPrice != null && pPrice > 0) {
                        confirmListing(player);
                    }
                })
                .build());

        // Cancel Button (center)
        gui.setSlot(40, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Cancel").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Return to main menu").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Item will be returned").withStyle(ChatFormatting.GRAY))
                .setCallback((index, type, containerInput, slotGui) -> {
                    returnPendingItem(player);
                    openMainMenu(player);
                })
                .build());

        // Clear Item Button (right)
        if (pendingItem != null && !pendingItem.isEmpty()) {
            gui.setSlot(42, new GuiElementBuilder(Items.HONEY_BOTTLE)
                    .setName(Component.literal("Remove Item").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Return item to inventory").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("and select a different one").withStyle(ChatFormatting.GRAY))
                    .setCallback((index, type, containerInput, slotGui) -> {
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
                    .setName(Component.literal(""))
                    .build());
        }

        // Balance display in bottom corner
        int credits = CreditItem.countCredits(player);
        int coins = CoinManager.getCoins(player);
        gui.setSlot(53, new GuiElementBuilder(Items.GOLD_NUGGET)
                .setName(Component.literal("Your Balance").withStyle(ChatFormatting.GOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Credits: " + formatNumber(credits)).withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("Coins: " + formatNumber(coins)).withStyle(ChatFormatting.YELLOW))
                .build());

        gui.open();
    }

    private static void confirmListing(ServerPlayer player) {
        ItemStack item = pendingSellItems.get(player.getUUID());
        Long price = pendingSellPrice.get(player.getUUID());
        Boolean inCoins = pendingSellInCoins.get(player.getUUID());

        if (item == null || item.isEmpty() || price == null || price <= 0 || inCoins == null) {
            player.sendSystemMessage(Component.literal("✗ Invalid listing data!").withStyle(ChatFormatting.RED));
            return;
        }

        long listingTax = calculateListingTax(price);

        if (listingTax > 0) {
            if (inCoins) {
                if (!CoinManager.hasCoins(player, listingTax)) {
                    player.sendSystemMessage(Component.literal("✗ Not enough coins for tax (" + formatNumber(listingTax) + ")!").withStyle(ChatFormatting.RED));
                    return;
                }
                CoinManager.removeCoins(player, listingTax);
                com.political.government.Treasury.deposit(listingTax, "auction listing fee");
            } else {
                if (!CreditItem.hasCredits(player, (int) Math.min(listingTax, Integer.MAX_VALUE))) {
                    player.sendSystemMessage(Component.literal("✗ Not enough credits for tax (" + formatNumber(listingTax) + ")!").withStyle(ChatFormatting.RED));
                    return;
                }
                CreditItem.removeCredits(player, (int) Math.min(listingTax, Integer.MAX_VALUE));
                com.political.government.Treasury.deposit(listingTax * COINS_PER_CREDIT, "auction listing fee");
            }
        }

        AuctionHouseManager.AuctionListing listing = new AuctionHouseManager.AuctionListing(
                player.getUUID().toString(),
                player.getName().getString(),
                item,
                price,
                inCoins
        );
        AuctionHouseManager.addListing(listing);

        pendingSellItems.remove(player.getUUID());
        pendingSellPrice.remove(player.getUUID());
        pendingSellInCoins.remove(player.getUUID());

        String curr = inCoins ? "coins" : "credits";
        player.sendSystemMessage(Component.literal("✓ Listed for " + formatNumber(price) + " " + curr + "!").withStyle(ChatFormatting.GREEN));
        if (listingTax > 0) {
            player.sendSystemMessage(Component.literal("  (Tax: " + formatNumber(listingTax) + " " + curr + ")").withStyle(ChatFormatting.GRAY));
        }

        openMainMenu(player);
    }

    // ════════════════════════════════════════════════════════════════════
    // MY LISTINGS
    // ════════════════════════════════════════════════════════════════════

    public static void openMyListings(ServerPlayer player, int page) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("My Listings"));

        // Fill background
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        // Get player's listings
        List<AuctionHouseManager.AuctionListing> myListings = new ArrayList<>();
        for (AuctionHouseManager.AuctionListing listing : AuctionHouseManager.getListings()) {
            if (listing.sellerUuid.equals(player.getUUID().toString())) {
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
                    .setName(displayStack.getHoverName().copy().withStyle(ChatFormatting.WHITE))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Price: " + formatNumber(listing.price) + (listing.priceInCoins ? " coins" : " credits")).withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to CANCEL").withStyle(ChatFormatting.RED))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        cancelListing(player, listingId);
                        openMyListings(player, fPage);
                    })
                    .build());

            slotIndex++;
        }

        // Empty state
        if (myListings.isEmpty()) {
            gui.setSlot(13, new GuiElementBuilder(Items.BARRIER)
                    .setName(Component.literal("No Listings").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("You have no active listings").withStyle(ChatFormatting.GRAY))
                    .build());
        }

        // Navigation
        gui.setSlot(27, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.RED))
                .setCallback((index, type, containerInput, slotGui) -> openMainMenu(player))
                .build());

        if (page > 0) {
            final int prevPage = page - 1;
            gui.setSlot(30, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("◀ Previous").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> openMyListings(player, prevPage))
                    .build());
        }

        gui.setSlot(31, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("Page " + (page + 1) + "/" + totalPages).withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Total: " + myListings.size() + " listings").withStyle(ChatFormatting.GRAY))
                .build());

        if (page < totalPages - 1) {
            final int nextPage = page + 1;
            gui.setSlot(32, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Next ▶").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> openMyListings(player, nextPage))
                    .build());
        }

        gui.open();
    }

    private static void cancelListing(ServerPlayer player, String listingId) {
        AuctionHouseManager.AuctionListing listing = AuctionHouseManager.getListing(listingId);
        if (listing == null) {
            player.sendSystemMessage(Component.literal("✗ Listing not found!").withStyle(ChatFormatting.RED));
            return;
        }

        if (!player.getUUID().toString().equals(listing.sellerUuid)) {
            player.sendSystemMessage(Component.literal("✗ Not your listing!").withStyle(ChatFormatting.RED));
            return;
        }

        ItemStack item = listing.getItemStack();
        AuctionHouseManager.removeListing(listingId);

        if (!item.isEmpty()) {
            if (!player.addItem(item.copy())) {
                player.drop(item.copy(), false);
            }
        }

        player.sendSystemMessage(Component.literal("✓ Listing cancelled! Item returned.").withStyle(ChatFormatting.YELLOW));
    }

    // ════════════════════════════════════════════════════════════════════
    // PRICE SIGN
    // ════════════════════════════════════════════════════════════════════

    private static void openPriceSign(ServerPlayer player) {
        ItemStack pendingItem = pendingSellItems.get(player.getUUID());
        Boolean inCoins = pendingSellInCoins.get(player.getUUID());

        if (pendingItem == null || pendingItem.isEmpty()) {
            player.sendSystemMessage(Component.literal("✗ No item selected!").withStyle(ChatFormatting.RED));
            openSellMenu(player);
            return;
        }

        if (inCoins == null) {
            inCoins = false;
        }

        final String currencyName = inCoins ? "coins" : "credits";

        SignGui signGui = new SignGui(player) {
            @Override
            public void onManualClose() {
                String priceText = this.getLine(0).getString().trim();
                priceText = priceText.replaceAll("[^0-9]", "");

                if (priceText.isEmpty()) {
                    player.sendSystemMessage(Component.literal("✗ No price entered!").withStyle(ChatFormatting.RED));
                    openSellMenu(player);
                    return;
                }

                try {
                    long price = Long.parseLong(priceText);

                    if (price <= 0) {
                        player.sendSystemMessage(Component.literal("✗ Price must be greater than 0!").withStyle(ChatFormatting.RED));
                        openSellMenu(player);
                        return;
                    }

                    if (price > MAX_PRICE) {
                        player.sendSystemMessage(Component.literal("✗ Max price is 1 billion!").withStyle(ChatFormatting.RED));
                        openSellMenu(player);
                        return;
                    }

                    pendingSellPrice.put(player.getUUID(), price);
                    player.sendSystemMessage(Component.literal("✓ Price set to " + formatNumber(price) + " " + currencyName + "!").withStyle(ChatFormatting.GREEN));
                    openSellMenu(player);

                } catch (NumberFormatException e) {
                    player.sendSystemMessage(Component.literal("✗ Invalid number!").withStyle(ChatFormatting.RED));
                    openSellMenu(player);
                }
            }
        };

        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal("^^^^^^^^"));
        signGui.setLine(2, Component.literal("Enter price"));
        signGui.setLine(3, Component.literal("in " + currencyName));

        signGui.open();
    }

    // ════════════════════════════════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════════════════════════════════

    private static void returnPendingItem(ServerPlayer player) {
        ItemStack pending = pendingSellItems.remove(player.getUUID());
        pendingSellPrice.remove(player.getUUID());
        pendingSellInCoins.remove(player.getUUID());
        if (pending != null && !pending.isEmpty()) {
            if (!player.addItem(pending.copy())) {
                player.drop(pending.copy(), false);
            }
        }
    }

    public static void onPlayerDisconnect(ServerPlayer player) {
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
        if (stack.isEnchanted()) {
            return Category.ENCHANTED;
        }

        // Weapons
        if (item == Items.DIAMOND_SWORD ||
                item == Items.IRON_SWORD || item == Items.GOLDEN_SWORD ||
                item == Items.STONE_SWORD || item == Items.WOODEN_SWORD ||
                item == Items.BOW || item == Items.CROSSBOW || item == Items.TRIDENT ||
                item == Items.MACE) {
            return Category.WEAPONS;
        }

        // Armor
        if (item == Items.DIAMOND_HELMET || item == Items.DIAMOND_CHESTPLATE ||
                item == Items.DIAMOND_LEGGINGS || item == Items.DIAMOND_BOOTS ||
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
        if (stack.has(DataComponents.FOOD)) {
            return Category.FOOD;
        }

        // Potions
        if (item == Items.POTION || item == Items.SPLASH_POTION ||
                item == Items.LINGERING_POTION || item == Items.GLASS_BOTTLE) {
            return Category.POTIONS;
        }

        // Materials
        if (item == Items.DIAMOND || item == Items.EMERALD || item == Items.GOLD_INGOT ||
                item == Items.IRON_INGOT || item == Items.COPPER_INGOT ||
                item == Items.COAL || item == Items.REDSTONE || item == Items.LAPIS_LAZULI ||
                item == Items.QUARTZ || item == Items.AMETHYST_SHARD ||
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