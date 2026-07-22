package com.political;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.Slot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

public class AuctionGui extends AbstractContainerMenu {

    private static final int AUCTION_SLOTS = 54;
    private static final int PLAYER_INVENTORY_ROWS = 3;
    private static final int PLAYER_INVENTORY_COLUMNS = 9;
    private static final int PLAYER_INVENTORY_SLOTS = PLAYER_INVENTORY_ROWS * PLAYER_INVENTORY_COLUMNS;

    private final Container auctionInventory;

    public AuctionGui(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(AUCTION_SLOTS));
    }

    public AuctionGui(int syncId, Inventory playerInventory, Container auctionInventory) {
        super(null, syncId);
        this.auctionInventory = auctionInventory;

        // Add auction slots (6 rows x 9 columns = 54 slots)
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(auctionInventory, row * 9 + col, 8 + col * 18, 18 + row * 18));
            }
        }

        // Add player inventory
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < PLAYER_INVENTORY_ROWS; row++) {
            for (int col = 0; col < PLAYER_INVENTORY_COLUMNS; col++) {
                this.addSlot(new Slot(playerInventory, col + row * PLAYER_INVENTORY_COLUMNS + 9, 8 + col * 18, 140 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int col = 0; col < PLAYER_INVENTORY_COLUMNS; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 198));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slotObject = this.slots.get(slot);

        if (slotObject != null && slotObject.hasItem()) {
            ItemStack itemStack2 = slotObject.getItem();
            itemStack = itemStack2.copy();

            if (slot < AUCTION_SLOTS) {
                if (!this.moveItemStackTo(itemStack2, AUCTION_SLOTS, AUCTION_SLOTS + PLAYER_INVENTORY_SLOTS, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemStack2, 0, AUCTION_SLOTS, false)) {
                return ItemStack.EMPTY;
            }

            if (itemStack2.isEmpty()) {
                slotObject.set(ItemStack.EMPTY);
            } else {
                slotObject.setChanged();
            }
        }

        return itemStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public static void open(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
                (syncId, playerInventory, playerEntity) -> new AuctionGui(syncId, playerInventory),
                Component.literal("Auction House")
        ));
    }
}
