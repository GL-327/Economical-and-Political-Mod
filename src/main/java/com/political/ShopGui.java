package com.political;


import net.minecraft.util.Prediction;
import com.political.bounty.SlayerItems;
import com.political.economy.CoinManager;
import com.political.economy.ShopManager;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.List;

public class ShopGui {
    private static final String ENCHANTED_BOOKS_CATEGORY = "Enchanted Books";

    // ════════════════════════════════════════════════════════════
    // MAIN MENU - Category Selection
    // ════════════════════════════════════════════════════════════

    /** Where Back leads from this screen: the menu that opened it, remembered per player. */
    private static final com.political.core.gui.ReturnTo FROM = new com.political.core.gui.ReturnTo();

    /** Opens the screen from a parent menu (Back returns there), or with {@code null} from a command (no Back). */
    public static void openMainMenuFrom(ServerPlayer player, Runnable back) {
        FROM.set(player.getUUID(), back);
        openMainMenu(player);
    }

    public static void openMainMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("🏪 Premium Item Shop"));

        // Fill background with decorative pattern
        for (int i = 0; i < 54; i++) {
            if (i % 9 == 0 || i % 9 == 8 || i < 9 || i >= 45) {
                gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray())
                        .setName(Component.literal("⬛").withStyle(ChatFormatting.DARK_GRAY))
                        .build());
            } else {
                gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.black())
                        .setName(Component.literal(""))
                        .build());
            }
        }

        // Header with balance - center position
        int coins = CoinManager.getCoins(player);
        gui.setSlot(4, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Component.literal("💰 Your Balance").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Coins: " + ShopManager.formatCoins(coins)).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("• Buy items at 10x sell price").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("• Sell items for instant coins!").withStyle(ChatFormatting.GRAY))
                .glow()
                .build());

        // Category buttons - match icons to actual categories
        Item[] categoryIcons = {
                Items.BRICKS,           // Building Blocks
                Items.DIAMOND,           // Ores & Materials  
                Items.GOLDEN_APPLE,      // Food
                Items.BONE_BLOCK,        // Mob Drops
                Items.REDSTONE_BLOCK,    // Redstone
                Items.NETHERITE_PICKAXE, // Tools & Utility
                Items.GRASS_BLOCK,       // Plants
                Items.BREWING_STAND,     // Brewing
                Items.DYE.cyan(),          // Dyes & Decoration
                Items.ENDER_CHEST,       // Miscellaneous
                Items.WHEAT,             // Farming & Natural
                Items.WOOL.white(),        // Wool & Carpets
                Items.SKELETON_SKULL     // Heads & Banners
        };

        List<String> categories = ShopManager.getCategoryNames().stream()
                .filter(c -> !c.equalsIgnoreCase("Compacted Items"))
                .filter(c -> !c.equalsIgnoreCase("Super Compacted"))
                .filter(c -> !c.equalsIgnoreCase("Enchanted Items"))
                .toList();
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};

        for (int i = 0; i < categories.size() && i < slots.length; i++) {
            String category = categories.get(i);
            Item icon = i < categoryIcons.length ? categoryIcons[i] : Items.CHEST;

            final String cat = category;
            gui.setSlot(slots[i], new GuiElementBuilder(icon)
                    .setName(Component.literal("📦 " + category).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to browse items").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("Right-click for quick buy").withStyle(ChatFormatting.DARK_GRAY))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        openCategoryPage(player, cat, 0, false);
                    })
                    .build());
        }

        // Enchanted books (buy-only, expensive)
        gui.setSlot(40, new GuiElementBuilder(Items.ENCHANTED_BOOK)
                .setName(Component.literal("📘 Enchanted Books").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Buy specific max-level books").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Sell disabled").withStyle(ChatFormatting.DARK_RED))
                .addLoreLine(Component.literal("Villagers are still cheaper").withStyle(ChatFormatting.DARK_GRAY))
                .setCallback((i, t, containerInput, slotGui) -> openEnchantedBooksPage(player, 0))
                .build());

        gui.setSlot(39, new GuiElementBuilder(Items.STAINED_GLASS_PANE.black())
                .setName(Component.literal(""))
                .build());

        // Bottom row action buttons - improved layout
        gui.setSlot(47, new GuiElementBuilder(Items.HOPPER)
                .setName(Component.literal("⚡ Quick Sell").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Sell items from inventory").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Fast & convenient!").withStyle(ChatFormatting.DARK_GRAY))
                .setCallback((index, type, containerInput, slotGui) -> {
                    openSellInventory(player);
                })
                .build());

        int sellAllValue = 0;
        for (int si = 0; si < player.getInventory().getContainerSize(); si++) {
            ItemStack stack = player.getInventory().getItem(si);
            if (!stack.isEmpty() && ShopManager.canBeSold(stack.getItem())) {
                sellAllValue += ShopManager.getSellPrice(stack.getItem()) * stack.getCount();
            }
        }
        final int estimatedValue = sellAllValue;

        gui.setSlot(49, new GuiElementBuilder(Items.CHEST)
                .setName(Component.literal("💸 Sell All").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("⚠ WARNING: Sells ALL valid items").withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal("in your inventory!").withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Estimated value: " + ShopManager.formatCoins(estimatedValue) + " coins").withStyle(ChatFormatting.GOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("⚠ Shift+Click to confirm").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> {
                    if (type.shift) {
                        sellAllItems(player);
                    } else {
                        player.sendSystemMessage(Component.literal("⚠ Shift+Click to sell all items!").withStyle(ChatFormatting.YELLOW));
                    }
                })
                .build());

        gui.setSlot(51, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("❌ Close").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Close the shop").withStyle(ChatFormatting.GRAY))
                .setCallback((index, type, containerInput, slotGui) -> {
                    gui.close();
                })
                .build());

        // Convert Credits → Coins button - better position
        int playerCredits = DataManager.getCredits(player.getUUID().toString());
        gui.setSlot(53, new GuiElementBuilder(Items.AMETHYST_SHARD)
                .setName(Component.literal("💎 Credit Exchange").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("1 Credit = 10,000 Coins").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Your Credits: " + playerCredits).withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Left-click: exchange 1 credit").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("⚠ Shift+Click: convert ALL credits").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("A chat warning is shown, then all credits are sold.").withStyle(ChatFormatting.DARK_RED))
                .addLoreLine(Component.literal(""))
                .addLoreLine(playerCredits > 0
                        ? Component.literal("Click to exchange").withStyle(ChatFormatting.AQUA)
                        : Component.literal("No credits available").withStyle(ChatFormatting.RED))
                .setCallback((index, type, containerInput, slotGui) -> {
                    String uuid = player.getUUID().toString();
                    int credits = DataManager.getCredits(uuid);
                    if (credits <= 0) {
                        player.sendSystemMessage(Component.literal("✗ You have no credits to exchange!").withStyle(ChatFormatting.RED), false);
                        return;
                    }
                    if (type.shift) {
                        player.sendSystemMessage(Component.literal("")
                                .append(Component.literal("⚠ WARNING: ").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                                .append(Component.literal("Converting all " + credits + " credits to coins at 10,000 coins each. This cannot be undone.")
                                        .withStyle(ChatFormatting.YELLOW)), false);
                        long coinsOut = (long) credits * 10_000L;
                        if (!DataManager.removeCredits(uuid, credits)) {
                            player.sendSystemMessage(Component.literal("✗ Conversion failed.").withStyle(ChatFormatting.RED), false);
                            return;
                        }
                        CoinManager.giveCoins(player, coinsOut);
                        String coinDisplay = coinsOut <= Integer.MAX_VALUE
                                ? ShopManager.formatCoins((int) coinsOut)
                                : String.format("%,d", coinsOut);
                        player.sendSystemMessage(Component.literal("✓ Exchanged " + credits + " credits → "
                                        + coinDisplay + " coins!")
                                .withStyle(ChatFormatting.GREEN), false);
                        DataManager.save(PoliticalServer.server);
                        openMainMenu(player);
                    } else {
                        DataManager.removeCredits(uuid, 1);
                        CoinManager.giveCoins(player, 10_000L);
                        player.sendSystemMessage(Component.literal("✓ Exchanged 1 Credit → 10,000 Coins!").withStyle(ChatFormatting.GREEN), false);
                        DataManager.save(PoliticalServer.server);
                        openMainMenu(player);
                    }
                })
                .build());

        com.political.core.gui.BackSlot.add(gui, FROM.get(player.getUUID()));
        gui.open();
    }

    private static void openEnchantedBooksPage(ServerPlayer player, int page) {
        if (PoliticalServer.server == null) {
            player.sendSystemMessage(Component.literal("Server not ready.").withStyle(ChatFormatting.RED), false);
            return;
        }

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("📘 Enchanted Books — Page " + (page + 1)).withStyle(ChatFormatting.AQUA));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.black()).setName(Component.literal("")).build());
        }

        java.util.List<net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment>> enchants = new java.util.ArrayList<>();
        PoliticalServer.server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).listElements().forEach(enchants::add);
        enchants.sort(java.util.Comparator.comparing(a -> a.unwrapKey().map(k -> k.identifier().toString()).orElse("")));

        // Add custom enchants to the list
        java.util.List<CustomEnchantEntry> customEnchants = getCustomEnchantsForShop();
        
        int perPage = 36;
        int totalItems = enchants.size() + customEnchants.size();
        int totalPages = Math.max(1, (totalItems + perPage - 1) / perPage);
        int p = Math.min(Math.max(0, page), totalPages - 1);
        int start = p * perPage;
        int end = Math.min(start + perPage, totalItems);

        int slot = 9;
        for (int idx = start; idx < end && slot < 45; idx++) {
            if (idx < enchants.size()) {
                // Vanilla enchant
                var entry = enchants.get(idx);
                var ench = entry.value();
                int max = Math.max(1, ench.getMaxLevel());

                ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
                var stored = book.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
                var mutable = new ItemEnchantments.Mutable(stored);
                mutable.set(entry, max);
                book.set(DataComponents.STORED_ENCHANTMENTS, mutable.toImmutable());

                int price = 2500 + (max * 1500);

                gui.setSlot(slot, new GuiElementBuilder(book)
                        .setName(Component.literal("Max book").withStyle(ChatFormatting.AQUA))
                        .addLoreLine(Component.literal("Max level: " + max).withStyle(ChatFormatting.YELLOW))
                        .addLoreLine(Component.literal("Price: " + price + " coins").withStyle(ChatFormatting.GOLD))
                        .addLoreLine(Component.literal("Click to buy").withStyle(ChatFormatting.GREEN))
                        .setCallback((s, ct, ac, __slotGui) -> {
                            if (CoinManager.getCoins(player) < price) {
                                player.sendSystemMessage(Component.literal("Not enough coins.").withStyle(ChatFormatting.RED), false);
                                return;
                            }
                            CoinManager.removeCoins(player, price);
                            if (!player.addItem(book.copy())) {
                                player.drop(book.copy(), false, Prediction.SERVER_ONLY);
                            }
                            player.sendSystemMessage(Component.literal("✓ Purchased enchanted book.").withStyle(ChatFormatting.GREEN), false);
                        })
                        .build());
            } else {
                // Custom enchant
                int customIdx = idx - enchants.size();
                CustomEnchantEntry custom = customEnchants.get(customIdx);
                
                ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
                book.set(DataComponents.CUSTOM_NAME, 
                        Component.literal("✦ " + custom.displayName).withStyle(ChatFormatting.LIGHT_PURPLE));
                CustomEnchantmentManager.addEnchantment(book, custom.key, custom.maxLevel);
                CustomEnchantmentManager.addEnchantmentLore(book);
                
                int price = custom.price;

                gui.setSlot(slot, new GuiElementBuilder(book)
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("Custom Enchant").withStyle(ChatFormatting.LIGHT_PURPLE))
                        .addLoreLine(Component.literal("Max level: " + custom.maxLevel).withStyle(ChatFormatting.YELLOW))
                        .addLoreLine(Component.literal("Price: " + price + " coins").withStyle(ChatFormatting.GOLD))
                        .addLoreLine(Component.literal("Click to buy").withStyle(ChatFormatting.GREEN))
                        .setCallback((s, ct, ac, __slotGui) -> {
                            if (CoinManager.getCoins(player) < price) {
                                player.sendSystemMessage(Component.literal("Not enough coins.").withStyle(ChatFormatting.RED), false);
                                return;
                            }
                            CoinManager.removeCoins(player, price);
                            ItemStack purchased = new ItemStack(Items.ENCHANTED_BOOK);
                            purchased.set(DataComponents.CUSTOM_NAME, 
                                    Component.literal("✦ " + custom.displayName).withStyle(ChatFormatting.LIGHT_PURPLE));
                            CustomEnchantmentManager.addEnchantment(purchased, custom.key, custom.maxLevel);
                            CustomEnchantmentManager.addEnchantmentLore(purchased);
                            if (!player.addItem(purchased)) {
                                player.drop(purchased, false, Prediction.SERVER_ONLY);
                            }
                            player.sendSystemMessage(Component.literal("✓ Purchased " + custom.displayName + ".").withStyle(ChatFormatting.GREEN), false);
                        })
                        .build());
            }
            slot++;
            if (slot % 9 == 0) slot++; // keep in inner 8-wide area
        }

        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((s, t, a, __slotGui) -> openMainMenu(player))
                .build());
        if (p > 0) {
            gui.setSlot(48, new GuiElementBuilder(Items.SPECTRAL_ARROW)
                    .setName(Component.literal("◀ Prev").withStyle(ChatFormatting.AQUA))
                    .setCallback((s, t, a, __slotGui) -> openEnchantedBooksPage(player, p - 1))
                    .build());
        }
        if (p < totalPages - 1) {
            gui.setSlot(50, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Next ▶").withStyle(ChatFormatting.AQUA))
                    .setCallback((s, t, a, __slotGui) -> openEnchantedBooksPage(player, p + 1))
                    .build());
        }
        gui.setSlot(53, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("✖ Close").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .setCallback((s, t, a, __slotGui) -> gui.close())
                .build());

        gui.open();
    }

    private static void openEnchantedBlockGearPage(ServerPlayer player, int page) {
        java.util.List<ItemStack> stacks = EnchantedShopManager.allEnchantedBlockStacks();
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("✦ Enchanted block gear — " + (page + 1)));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.black()).setName(Component.literal("")).build());
        }

        int perPage = 28;
        int totalPages = Math.max(1, (stacks.size() + perPage - 1) / perPage);
        int p = Math.min(Math.max(0, page), totalPages - 1);
        int start = p * perPage;
        int coins = CoinManager.getCoins(player);

        int[] slots = {10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43};

        int si = 0;
        for (int idx = start; idx < stacks.size() && si < slots.length; idx++) {
            ItemStack offer = stacks.get(idx);
            String cid = SlayerItems.getCustomItemId(offer);
            boolean off = cid != null && EnchantedShopManager.isDisabled(cid);
            int buy = EnchantedShopManager.getBuyPrice(offer);
            int sell = EnchantedShopManager.getSellPrice(offer);
            Component nm = offer.get(DataComponents.CUSTOM_NAME);
            int slot = slots[si++];
            if (off) {
                gui.setSlot(slot, new GuiElementBuilder(Items.BARRIER)
                        .setName(Component.literal("Disabled").withStyle(ChatFormatting.RED))
                        .addLoreLine(nm != null ? nm : Component.literal(cid != null ? cid : "?"))
                        .build());
                continue;
            }
            boolean can = coins >= buy;
            gui.setSlot(slot, new GuiElementBuilder(offer.getItem())
                    .setName(nm != null ? nm : Component.literal("Enchanted gear"))
                    .addLoreLine(Component.literal("Buy: " + buy + " coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal("Est. sell value: " + sell).withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal("Click to buy 1").withStyle(can ? ChatFormatting.YELLOW : ChatFormatting.RED))
                    .glow()
                    .setCallback((s, ct, ac, __slotGui) -> {
                        if (CoinManager.getCoins(player) < buy) {
                            player.sendSystemMessage(Component.literal("Not enough coins.").withStyle(ChatFormatting.RED), false);
                            return;
                        }
                        CoinManager.removeCoins(player, buy);
                        ItemStack g = offer.copy();
                        g.setCount(1);
                        if (!player.addItem(g)) {
                            player.drop(g, false, Prediction.SERVER_ONLY);
                        }
                        player.sendSystemMessage(Component.literal("Purchased enchanted gear.").withStyle(ChatFormatting.GREEN), false);
                        openEnchantedBlockGearPage(player, p);
                    })
                    .build());
        }

        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Shop").withStyle(ChatFormatting.YELLOW))
                .setCallback((s, t, a, __slotGui) -> openMainMenu(player))
                .build());
        if (p > 0) {
            gui.setSlot(48, new GuiElementBuilder(Items.SPECTRAL_ARROW)
                    .setName(Component.literal("◀ Prev").withStyle(ChatFormatting.AQUA))
                    .setCallback((s, t, a, __slotGui) -> openEnchantedBlockGearPage(player, p - 1))
                    .build());
        }
        if (p < totalPages - 1) {
            gui.setSlot(50, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Next ▶").withStyle(ChatFormatting.AQUA))
                    .setCallback((s, t, a, __slotGui) -> openEnchantedBlockGearPage(player, p + 1))
                    .build());
        }
        gui.setSlot(53, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Close").withStyle(ChatFormatting.RED))
                .setCallback((s, t, a, __slotGui) -> gui.close())
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    // CATEGORY PAGE - Browse Items in Category
    // ════════════════════════════════════════════════════════════

    public static void openCategoryPage(ServerPlayer player, String category, int page, boolean sellMode) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal((sellMode ? "Sell: " : "Buy: ") + category + " - Page " + (page + 1)));

        // Fill background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray())
                    .setName(Component.literal(""))
                    .build());
        }

        // Header with balance
        int coins = CoinManager.getCoins(player);
        gui.setSlot(4, new GuiElementBuilder(Items.SUNFLOWER)
                .setName(Component.literal("Coins: " + ShopManager.formatCoins(coins)).withStyle(ChatFormatting.GOLD))
                .build());

        List<Item> items = ShopManager.getItemsInCategory(category);
        int itemsPerPage = 36; // 4 rows of 9
        int totalPages = (int) Math.ceil((double) items.size() / itemsPerPage);
        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, items.size());

        // Display items
        int slot = 9; // Start at second row
        for (int i = startIndex; i < endIndex; i++) {
            Item item = items.get(i);
            int sellPrice = ShopManager.getSellPrice(item);
            int buyPrice = ShopManager.getBuyPrice(item);

            if (sellPrice <= 0) continue; // Skip items without prices

            int playerHas = ShopManager.countItems(player, item);
            
            String itemName;
            GuiElementBuilder builder;

            // Vanilla item (custom variants are reserved for /sshop)
            itemName = ShopManager.getItemName(item);
            builder = new GuiElementBuilder(item)
                    .setName(Component.literal(itemName).withStyle(ChatFormatting.WHITE));

            if (sellMode) {
                // SELL MODE
                boolean canSell = playerHas > 0;
                builder.addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("Sell Price: " + sellPrice + " coins").withStyle(ChatFormatting.GREEN))
                        .addLoreLine(Component.literal("You have: " + playerHas).withStyle(ChatFormatting.GRAY))
                        .addLoreLine(Component.literal(""));

                if (canSell) {
                    builder.addLoreLine(Component.literal("Left: Sell 1 | Shift+Left: Sell 16 | Right: Sell 64").withStyle(ChatFormatting.YELLOW));

                    final Item finalItem = item;
                    builder.setCallback((index, type, containerInput, slotGui) -> {
                        int amount;
                        if (type == eu.pb4.sgui.api.ClickType.MOUSE_LEFT_SHIFT) {
                            amount = Math.min(16, ShopManager.countItems(player, finalItem));
                        } else if (type == eu.pb4.sgui.api.ClickType.MOUSE_RIGHT) {
                            amount = Math.min(64, ShopManager.countItems(player, finalItem));
                        } else {
                            amount = 1;
                        }
                        if (amount > 0) {
                            ShopManager.sellItem(player, finalItem, amount);
                            openCategoryPage(player, category, page, true);
                        }
                    });
                } else {
                    builder.addLoreLine(Component.literal("You don't have any!").withStyle(ChatFormatting.RED));
                }
            } else {
                // BUY MODE
                boolean canAfford = coins >= buyPrice;
                builder.addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("Buy Price: " + buyPrice + " coins").withStyle(ChatFormatting.RED))
                        .addLoreLine(Component.literal("Sell Price: " + sellPrice + " coins").withStyle(ChatFormatting.GREEN))
                        .addLoreLine(Component.literal("You have: " + playerHas).withStyle(ChatFormatting.GRAY))
                        .addLoreLine(Component.literal(""));

                if (canAfford) {
                    builder.addLoreLine(Component.literal("Left: Buy 1 | Shift+Left: Buy 16 | Right: Buy 64").withStyle(ChatFormatting.YELLOW));

                    final Item finalItem = item;
                    builder.setCallback((index, type, containerInput, slotGui) -> {
                        int amount;
                        if (type == eu.pb4.sgui.api.ClickType.MOUSE_LEFT_SHIFT) {
                            amount = 16;
                        } else if (type == eu.pb4.sgui.api.ClickType.MOUSE_RIGHT) {
                            amount = 64;
                        } else {
                            amount = 1;
                        }
                        ShopManager.buyItem(player, finalItem, amount);
                        openCategoryPage(player, category, page, false);
                    });
                } else {
                    builder.addLoreLine(Component.literal("Not enough coins!").withStyle(ChatFormatting.RED));
                }
            }

            gui.setSlot(slot, builder.build());
            slot++;
            if (slot == 45) break; // Don't overflow into bottom row
        }

        // Navigation - Previous Page
        if (page > 0) {
            gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("← Previous Page").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        openCategoryPage(player, category, page - 1, sellMode);
                    })
                    .build());
        }

        // Back to main menu
        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Back to Categories").withStyle(ChatFormatting.RED))
                .setCallback((index, type, containerInput, slotGui) -> {
                    openMainMenu(player);
                })
                .build());

        // Toggle Buy/Sell mode
        gui.setSlot(48, new GuiElementBuilder(sellMode ? Items.GOLD_INGOT : Items.EMERALD)
                .setName(Component.literal(sellMode ? "Switch to BUY" : "Switch to SELL").withStyle(sellMode ? ChatFormatting.GREEN : ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Currently: " + (sellMode ? "SELLING" : "BUYING")).withStyle(ChatFormatting.GRAY))
                .setCallback((index, type, containerInput, slotGui) -> {
                    openCategoryPage(player, category, page, !sellMode);
                })
                .build());

        // Navigation - Next Page
        if (page < totalPages - 1) {
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Next Page →").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        openCategoryPage(player, category, page + 1, sellMode);
                    })
                    .build());
        }

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    // SELL INVENTORY - Shows all sellable items from player inventory
    // ════════════════════════════════════════════════════════════

    public static void openSellInventory(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Quick Sell - Your Items"));

        // Fill background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.lime())
                    .setName(Component.literal(""))
                    .build());
        }

        // Header
        int coins = CoinManager.getCoins(player);
        gui.setSlot(4, new GuiElementBuilder(Items.SUNFLOWER)
                .setName(Component.literal("Coins: " + ShopManager.formatCoins(coins)).withStyle(ChatFormatting.GOLD))
                .build());

        // Scan player inventory for sellable items
        int slot = 9;
        java.util.Map<Item, Integer> sellableItems = new java.util.LinkedHashMap<>();

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && ShopManager.canBeSold(stack.getItem())) {
                sellableItems.merge(stack.getItem(), stack.getCount(), (oldVal, newVal) -> oldVal + newVal);
            }
        }

        for (java.util.Map.Entry<Item, Integer> entry : sellableItems.entrySet()) {
            if (slot >= 45) break;

            Item item = entry.getKey();
            int count = entry.getValue();
            int sellPrice = ShopManager.getSellPrice(item);
            int totalValue = sellPrice * count;
            String itemName = ShopManager.getItemName(item);

            final Item finalItem = item;
            gui.setSlot(slot, new GuiElementBuilder(item)
                    .setCount(Math.min(count, 64))
                    .setName(Component.literal(itemName + " x" + count).withStyle(ChatFormatting.WHITE))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Each: " + sellPrice + " coins").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal("Total: " + totalValue + " coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Left-click: Sell 1").withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal("Right-click: Sell 10").withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal("Shift+Click: Sell All").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        int amount;
                        if (type.shift) {
                            amount = ShopManager.countItems(player, finalItem);
                        } else if (type.isRight) {
                            amount = Math.min(10, ShopManager.countItems(player, finalItem));
                        } else {
                            amount = 1;
                        }
                        if (amount > 0) {
                            ShopManager.sellItem(player, finalItem, amount);
                            openSellInventory(player); // Refresh
                        }
                    })
                    .build());
            slot++;
        }

        if (sellableItems.isEmpty()) {
            gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                    .setName(Component.literal("No Sellable Items").withStyle(ChatFormatting.RED))
                    .addLoreLine(Component.literal("Your inventory has no items").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("that can be sold.").withStyle(ChatFormatting.GRAY))
                    .build());
        }

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> {
                    openMainMenu(player);
                })
                .build());

        // Sell all button
        if (!sellableItems.isEmpty()) {
            int totalValue = sellableItems.entrySet().stream()
                    .mapToInt(e -> ShopManager.getSellPrice(e.getKey()) * e.getValue())
                    .sum();

            gui.setSlot(50, new GuiElementBuilder(Items.CHEST)
                    .setName(Component.literal("Sell All").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Total Value: " + totalValue + " coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Shift+Click to confirm!").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        if (type.shift) {
                            sellAllItems(player);
                            openSellInventory(player);
                        }
                    })
                    .build());
        }

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    // SELL ALL ITEMS
    // ════════════════════════════════════════════════════════════

    private static void sellAllItems(ServerPlayer player) {
        int totalCoins = 0;
        int totalItems = 0;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && ShopManager.canBeSold(stack.getItem())) {
                int sellPrice = ShopManager.getSellPrice(stack.getItem());
                int count = stack.getCount();
                totalCoins += sellPrice * count;
                totalItems += count;
                player.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }

        if (totalCoins > 0) {
            CoinManager.giveCoinsQuiet(player, totalCoins);
            player.sendSystemMessage(Component.literal("Sold " + totalItems + " items for ")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(ShopManager.formatCoins(totalCoins)).withStyle(ChatFormatting.GOLD))
                    .append(Component.literal("!").withStyle(ChatFormatting.GREEN)));
            DataManager.save(PoliticalServer.server);
        } else {
            player.sendSystemMessage(Component.literal("No sellable items in your inventory!").withStyle(ChatFormatting.RED));
        }
    }

    // ════════════════════════════════════════════════════════════
    // CUSTOM ENCHANTS FOR SHOP
    // ════════════════════════════════════════════════════════════

    private record CustomEnchantEntry(String key, String displayName, int maxLevel, int price) {}

    private static java.util.List<CustomEnchantEntry> getCustomEnchantsForShop() {
        java.util.List<CustomEnchantEntry> list = new java.util.ArrayList<>();
        // Weapon enchants
        list.add(new CustomEnchantEntry(CustomEnchantmentManager.SLAYER_SHARPNESS, "Slayer Sharpness", 5, 15000));
        list.add(new CustomEnchantEntry(CustomEnchantmentManager.BOSS_HUNTER, "Boss Hunter", 5, 20000));
        list.add(new CustomEnchantEntry(CustomEnchantmentManager.MOB_BANE, "Mob Bane", 5, 12000));
        list.add(new CustomEnchantEntry(CustomEnchantmentManager.VAMPIRIC, "Vampiric", 3, 18000));
        list.add(new CustomEnchantEntry(CustomEnchantmentManager.THUNDERBOLT, "Thunderbolt", 3, 25000));
        list.add(new CustomEnchantEntry(CustomEnchantmentManager.EXECUTE, "Execute", 3, 22000));
        list.add(new CustomEnchantEntry(CustomEnchantmentManager.FROSTBITE, "Frostbite", 3, 14000));
        // Armor enchants
        list.add(new CustomEnchantEntry(CustomEnchantmentManager.BOUNTY_PROTECTION, "Bounty Protection", 3, 10000));
        list.add(new CustomEnchantEntry(CustomEnchantmentManager.BOSS_WARD, "Boss Ward", 3, 16000));
        list.add(new CustomEnchantEntry(CustomEnchantmentManager.VITALITY, "Vitality", 3, 12000));
        list.add(new CustomEnchantEntry(CustomEnchantmentManager.THORNS_OF_VENGEANCE, "Thorns of Vengeance", 3, 15000));
        list.add(new CustomEnchantEntry(CustomEnchantmentManager.LAST_STAND, "Last Stand", 3, 20000));
        // Utility enchants
        list.add(new CustomEnchantEntry(CustomEnchantmentManager.SOULBOUND, "Soulbound", 1, 50000));
        list.add(new CustomEnchantEntry(CustomEnchantmentManager.PROSPECTOR, "Prospector", 5, 8000));
        return list;
    }
}