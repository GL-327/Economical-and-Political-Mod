package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.Level;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;

public final class RemoteChestAdminGui {

    public static void open(ServerPlayer admin, ChestAccessHistory.Entry entry) {
        open(admin, null, entry);
    }

    public static void open(ServerPlayer admin, ServerPlayer watchedPlayer, ChestAccessHistory.Entry entry) {
        if (PoliticalServer.server == null) return;
        Identifier dim = Identifier.parse(entry.worldId);
        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, dim);
        ServerLevel world = PoliticalServer.server.getLevel(key);
        if (world == null) {
            admin.sendSystemMessage(Component.literal("World not loaded: " + entry.worldId).withStyle(ChatFormatting.RED));
            return;
        }
        BlockPos pos = entry.pos();
        if (!world.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
            admin.sendSystemMessage(Component.literal("Chunk not loaded.").withStyle(ChatFormatting.RED));
            return;
        }
        Container inv = ContainerInventoryUtil.getContainerInventory(world, pos);
        if (inv == null) {
            admin.sendSystemMessage(Component.literal("No container at that position.").withStyle(ChatFormatting.RED));
            return;
        }
        int rows = (inv.getContainerSize() + 8) / 9;
        MenuType<?> type = switch (Math.min(rows, 6)) {
            case 1 -> MenuType.GENERIC_9x1;
            case 2 -> MenuType.GENERIC_9x2;
            case 3 -> MenuType.GENERIC_9x3;
            case 4 -> MenuType.GENERIC_9x4;
            case 5 -> MenuType.GENERIC_9x5;
            default -> MenuType.GENERIC_9x6;
        };
        SimpleGui gui = new SimpleGui(type, admin, false);
        gui.setTitle(Component.literal("Remote: " + entry.blockId).withStyle(ChatFormatting.GOLD));

        MinecraftServer server = world.getServer();
        Runnable redraw = () -> drawInventoryContainer(gui, admin, watchedPlayer, entry, inv, world, pos, server);
        redraw.run();
        gui.open();
    }

    private static void drawInventoryContainer(SimpleGui gui, ServerPlayer admin, ServerPlayer watchedPlayer,
                                               ChestAccessHistory.Entry entry, Container inv,
                                               ServerLevel world, BlockPos pos, MinecraftServer server) {
        int size = inv.getContainerSize();
        for (int i = size; i < gui.getSize(); i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Component.literal("")).build());
        }
        for (int i = 0; i < size; i++) {
            final int idx = i;
            ItemStack st = inv.getItem(idx);
            GuiElementBuilder b = st.isEmpty()
                    ? new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal("Empty #" + idx).withStyle(ChatFormatting.DARK_GRAY))
                    : new GuiElementBuilder(st.copy());
            b.setCallback((si, ct, ac, __slotGui) -> {
                ItemStack cursor = admin.containerMenu.getCarried().copy();
                ItemStack slot = inv.getItem(idx).copy();
                inv.setItem(idx, ItemStack.EMPTY);
                admin.containerMenu.setCarried(ItemStack.EMPTY);
                inv.setItem(idx, cursor.isEmpty() ? ItemStack.EMPTY : cursor.copy());
                admin.containerMenu.setCarried(slot.isEmpty() ? ItemStack.EMPTY : slot.copy());
                ContainerInventoryUtil.markContainerDirty(world, pos);
                admin.inventoryMenu.broadcastChanges();
                if (server != null) {
                    server.execute(() -> drawInventoryContainer(gui, admin, watchedPlayer, entry, inv, world, pos, server));
                }
            });
            gui.setSlot(i, b.build());
        }

        int backSlot = Math.max(0, gui.getSize() - 5);
        gui.setSlot(backSlot, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((a, b, c, __slotGui) -> {
                    if (watchedPlayer != null) {
                        PrevChestsAdminGui.open(admin, watchedPlayer);
                    } else {
                        admin.closeContainer();
                    }
                })
                .build());
    }
}
