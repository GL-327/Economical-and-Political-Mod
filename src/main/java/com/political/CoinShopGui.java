package com.political;
//
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

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

    public static void open(ServerPlayer player) {
        open(player, 0);
    }

    public static void open(ServerPlayer player, int page) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Coin Shop - Page " + (page + 1)));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.YELLOW_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        int playerCoins = CoinManager.getCoins(player);
        gui.setSlot(4, new GuiElementBuilder(Items.SUNFLOWER)
                .setName(Component.literal("Your Coins: " + playerCoins).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
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
                    .setName(Component.literal(shopItem.name + " x" + shopItem.amount).withStyle(canAfford ? ChatFormatting.GREEN : ChatFormatting.RED))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Cost: " + shopItem.coinCost + " coins").withStyle(ChatFormatting.YELLOW));

            if (canAfford) {
                final int index = i;
                builder.addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("Click to buy!").withStyle(ChatFormatting.GREEN))
                        .setCallback((idx, type, containerInput, slotGui) -> {
                            ShopItem item = SHOP_ITEMS.get(index);
                            if (CoinManager.removeCoins(player, item.coinCost)) {
                                ItemStack stack = new ItemStack(item.item, item.amount);
                                if (!player.addItem(stack)) {
                                    player.drop(stack, false);
                                }
                                player.sendSystemMessage(Component.literal("Purchased " + item.name + " x" + item.amount + "!").withStyle(ChatFormatting.GREEN));
                                open(player, page);
                            } else {
                                player.sendSystemMessage(Component.literal("Not enough coins!").withStyle(ChatFormatting.RED));
                            }
                        });
            } else {
                builder.addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("Not enough coins!").withStyle(ChatFormatting.RED));
            }

            gui.setSlot(slots[slotIndex], builder.build());
            slotIndex++;
        }

        // Previous page
        if (page > 0) {
            gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("← Previous Page").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> open(player, page - 1))
                    .build());
        }

        // Convert coins to credits button
        gui.setSlot(49, new GuiElementBuilder(Items.GOLD_NUGGET)
                .setName(Component.literal("Convert Coins → Credits").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("250 coins = 1 credit").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("Your coins: " + playerCoins).withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to convert!").withStyle(ChatFormatting.GREEN))
                .setCallback((index, type, containerInput, slotGui) -> {
                    if (CoinManager.convertToCredits(player, 1)) {
                        player.sendSystemMessage(Component.literal("Converted 250 coins to 1 credit!").withStyle(ChatFormatting.GREEN));
                        open(player, page);
                    } else {
                        player.sendSystemMessage(Component.literal("Need at least 250 coins!").withStyle(ChatFormatting.RED));
                    }
                })
                .build());

        // Next page
        if (page < totalPages - 1) {
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Next Page →").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> open(player, page + 1))
                    .build());
        }

        gui.open();
    }

}
//