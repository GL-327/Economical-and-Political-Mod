package com.political;
//
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class CoinShopGui {

    public static class ShopItem {
        public final Item item;
        public final String name;
        public final int amount;
        public final int coinCost;

        public ShopItem(Item item, String name, int amount, int coinCost) {
            this.item = item;
            this.name = name;
            this.amount = amount;
            this.coinCost = coinCost;
        }
    }

    public static final List<ShopItem> SHOP_ITEMS = new ArrayList<>();

    static {
        // Easy to obtain items - priced in coins
        SHOP_ITEMS.add(new ShopItem(Items.COBBLESTONE, "Cobblestone", 64, 5));
        SHOP_ITEMS.add(new ShopItem(Items.OAK_LOG, "Oak Log", 32, 10));
        SHOP_ITEMS.add(new ShopItem(Items.SPRUCE_LOG, "Spruce Log", 32, 10));
        SHOP_ITEMS.add(new ShopItem(Items.BIRCH_LOG, "Birch Log", 32, 10));
        SHOP_ITEMS.add(new ShopItem(Items.IRON_INGOT, "Iron Ingot", 16, 25));
        SHOP_ITEMS.add(new ShopItem(Items.COAL, "Coal", 32, 15));
        SHOP_ITEMS.add(new ShopItem(Items.WHEAT, "Wheat", 64, 8));
        SHOP_ITEMS.add(new ShopItem(Items.BREAD, "Bread", 32, 12));
        SHOP_ITEMS.add(new ShopItem(Items.COOKED_BEEF, "Steak", 32, 20));
        SHOP_ITEMS.add(new ShopItem(Items.ARROW, "Arrows", 64, 15));
        SHOP_ITEMS.add(new ShopItem(Items.TORCH, "Torches", 64, 10));
        SHOP_ITEMS.add(new ShopItem(Items.GLASS, "Glass", 64, 12));
        SHOP_ITEMS.add(new ShopItem(Items.REDSTONE, "Redstone", 32, 20));
        SHOP_ITEMS.add(new ShopItem(Items.LAPIS_LAZULI, "Lapis Lazuli", 16, 15));
        SHOP_ITEMS.add(new ShopItem(Items.BONE, "Bones", 32, 10));
        SHOP_ITEMS.add(new ShopItem(Items.STRING, "String", 32, 12));
        SHOP_ITEMS.add(new ShopItem(Items.LEATHER, "Leather", 16, 18));
        SHOP_ITEMS.add(new ShopItem(Items.PAPER, "Paper", 32, 8));
        SHOP_ITEMS.add(new ShopItem(Items.BOOK, "Books", 16, 15));
        SHOP_ITEMS.add(new ShopItem(Items.SAND, "Sand", 64, 5));
        SHOP_ITEMS.add(new ShopItem(Items.GRAVEL, "Gravel", 64, 5));
        SHOP_ITEMS.add(new ShopItem(Items.CLAY_BALL, "Clay", 32, 10));
        SHOP_ITEMS.add(new ShopItem(Items.SLIME_BALL, "Slime Ball", 8, 30));
        SHOP_ITEMS.add(new ShopItem(Items.GUNPOWDER, "Gunpowder", 16, 25));
        SHOP_ITEMS.add(new ShopItem(Items.FLINT, "Flint", 32, 8));
        SHOP_ITEMS.add(new ShopItem(Items.GOLD_INGOT, "Gold Ingot", 8, 40));
        SHOP_ITEMS.add(new ShopItem(Items.COPPER_INGOT, "Copper Ingot", 32, 15));
    }

    public static void open(ServerPlayerEntity player) {
        open(player, 0);
    }

    public static void open(ServerPlayerEntity player, int page) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("Coin Shop - Page " + (page + 1)));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.YELLOW_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        int playerCoins = CoinManager.getCoins(player);
        gui.setSlot(4, new GuiElementBuilder(Items.SUNFLOWER)
                .setName(Text.literal("Your Coins: " + playerCoins).formatted(Formatting.YELLOW, Formatting.BOLD))
                .glow()
                .build());

        int itemsPerPage = 28;
        int totalPages = (int) Math.ceil((double) SHOP_ITEMS.size() / itemsPerPage);
        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, SHOP_ITEMS.size());

        int[] slots = {10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43};

        int slotIndex = 0;
        for (int i = startIndex; i < endIndex && slotIndex < slots.length; i++) {
            ShopItem shopItem = SHOP_ITEMS.get(i);
            boolean canAfford = playerCoins >= shopItem.coinCost;

            GuiElementBuilder builder = new GuiElementBuilder(shopItem.item)
                    .setCount(Math.min(shopItem.amount, 64))
                    .setName(Text.literal(shopItem.name + " x" + shopItem.amount).formatted(canAfford ? Formatting.GREEN : Formatting.RED))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Cost: " + shopItem.coinCost + " coins").formatted(Formatting.YELLOW));

            if (canAfford) {
                final int index = i;
                builder.addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("Click to buy!").formatted(Formatting.GREEN))
                        .setCallback((idx, type, action) -> {
                            ShopItem item = SHOP_ITEMS.get(index);
                            if (CoinManager.removeCoins(player, item.coinCost)) {
                                ItemStack stack = new ItemStack(item.item, item.amount);
                                if (!player.getInventory().insertStack(stack)) {
                                    player.dropItem(stack, false);
                                }
                                player.sendMessage(Text.literal("Purchased " + item.name + " x" + item.amount + "!").formatted(Formatting.GREEN));
                                open(player, page);
                            } else {
                                player.sendMessage(Text.literal("Not enough coins!").formatted(Formatting.RED));
                            }
                        });
            } else {
                builder.addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("Not enough coins!").formatted(Formatting.RED));
            }

            gui.setSlot(slots[slotIndex], builder.build());
            slotIndex++;
        }

        // Previous page
        if (page > 0) {
            gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("← Previous Page").formatted(Formatting.YELLOW))
                    .setCallback((index, type, action) -> open(player, page - 1))
                    .build());
        }

        // Convert coins to credits button
        gui.setSlot(49, new GuiElementBuilder(Items.GOLD_NUGGET)
                .setName(Text.literal("Convert Coins → Credits").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("250 coins = 1 credit").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Your coins: " + playerCoins).formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to convert!").formatted(Formatting.GREEN))
                .setCallback((index, type, action) -> {
                    if (CoinManager.convertToCredits(player, 1)) {
                        player.sendMessage(Text.literal("Converted 250 coins to 1 credit!").formatted(Formatting.GREEN));
                        open(player, page);
                    } else {
                        player.sendMessage(Text.literal("Need at least 250 coins!").formatted(Formatting.RED));
                    }
                })
                .build());

        // Next page
        if (page < totalPages - 1) {
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("Next Page →").formatted(Formatting.YELLOW))
                    .setCallback((index, type, action) -> open(player, page + 1))
                    .build());
        }

        gui.open();
    }
}
//