package com.political;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class AuctionGui extends ScreenHandler {

    private static final int AUCTION_SLOTS = 54;
    private static final int PLAYER_INVENTORY_ROWS = 3;
    private static final int PLAYER_INVENTORY_COLUMNS = 9;
    private static final int PLAYER_INVENTORY_SLOTS = PLAYER_INVENTORY_ROWS * PLAYER_INVENTORY_COLUMNS;

    private final Inventory auctionInventory;

    public AuctionGui(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(AUCTION_SLOTS));
    }

    public AuctionGui(int syncId, PlayerInventory playerInventory, Inventory auctionInventory) {
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

    private void addPlayerInventory(PlayerInventory playerInventory) {
        for (int row = 0; row < PLAYER_INVENTORY_ROWS; row++) {
            for (int col = 0; col < PLAYER_INVENTORY_COLUMNS; col++) {
                this.addSlot(new Slot(playerInventory, col + row * PLAYER_INVENTORY_COLUMNS + 9, 8 + col * 18, 140 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(PlayerInventory playerInventory) {
        for (int col = 0; col < PLAYER_INVENTORY_COLUMNS; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 198));
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slotObject = this.slots.get(slot);

        if (slotObject != null && slotObject.hasStack()) {
            ItemStack itemStack2 = slotObject.getStack();
            itemStack = itemStack2.copy();

            if (slot < AUCTION_SLOTS) {
                if (!this.insertItem(itemStack2, AUCTION_SLOTS, AUCTION_SLOTS + PLAYER_INVENTORY_SLOTS, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(itemStack2, 0, AUCTION_SLOTS, false)) {
                return ItemStack.EMPTY;
            }

            if (itemStack2.isEmpty()) {
                slotObject.setStack(ItemStack.EMPTY);
            } else {
                slotObject.markDirty();
            }
        }

        return itemStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.auctionInventory.canPlayerUse(player);
    }

    public static void open(ServerPlayerEntity player) {
        player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                (syncId, playerInventory, playerEntity) -> new AuctionScreenHandler(syncId, playerInventory),
                Text.literal("Auction House")
        ));
    }
}
