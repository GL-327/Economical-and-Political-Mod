package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public final class RemoteChestAdminGui {

    public static void open(ServerPlayerEntity admin, ChestAccessHistory.Entry entry) {
        if (PoliticalServer.server == null) return;
        Identifier dim = Identifier.of(entry.worldId);
        RegistryKey<World> key = RegistryKey.of(RegistryKeys.WORLD, dim);
        ServerWorld world = PoliticalServer.server.getWorld(key);
        if (world == null) {
            admin.sendMessage(Text.literal("World not loaded: " + entry.worldId).formatted(Formatting.RED), false);
            return;
        }
        BlockPos pos = entry.pos();
        if (!world.isChunkLoaded(pos)) {
            admin.sendMessage(Text.literal("Chunk not loaded.").formatted(Formatting.RED), false);
            return;
        }
        Inventory inv = ContainerInventoryUtil.getContainerInventory(world, pos);
        if (inv == null) {
            admin.sendMessage(Text.literal("No container at that position.").formatted(Formatting.RED), false);
            return;
        }
        int rows = (inv.size() + 8) / 9;
        ScreenHandlerType<?> type = switch (Math.min(rows, 6)) {
            case 1 -> ScreenHandlerType.GENERIC_9X1;
            case 2 -> ScreenHandlerType.GENERIC_9X2;
            case 3 -> ScreenHandlerType.GENERIC_9X3;
            case 4 -> ScreenHandlerType.GENERIC_9X4;
            case 5 -> ScreenHandlerType.GENERIC_9X5;
            default -> ScreenHandlerType.GENERIC_9X6;
        };
        SimpleGui gui = new SimpleGui(type, admin, false);
        gui.setTitle(Text.literal("Remote: " + entry.blockId).formatted(Formatting.GOLD));

        MinecraftServer server = world.getServer();
        Runnable redraw = () -> drawInventoryContainer(gui, admin, inv, world, pos, server);
        redraw.run();
        gui.open();
    }

    private static void drawInventoryContainer(SimpleGui gui, ServerPlayerEntity admin, Inventory inv,
                                               ServerWorld world, BlockPos pos, MinecraftServer server) {
        int size = inv.size();
        for (int i = 0; i < size; i++) {
            final int idx = i;
            ItemStack st = inv.getStack(idx);
            GuiElementBuilder b = st.isEmpty()
                    ? new GuiElementBuilder(Items.LIGHT_GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal("Empty #" + idx).formatted(Formatting.DARK_GRAY))
                    : new GuiElementBuilder(st.copy());
            b.setCallback((si, ct, ac) -> {
                ItemStack cursor = admin.currentScreenHandler.getCursorStack().copy();
                ItemStack slot = inv.getStack(idx).copy();
                inv.setStack(idx, ItemStack.EMPTY);
                admin.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
                inv.setStack(idx, cursor.isEmpty() ? ItemStack.EMPTY : cursor.copy());
                admin.currentScreenHandler.setCursorStack(slot.isEmpty() ? ItemStack.EMPTY : slot.copy());
                ContainerInventoryUtil.markContainerDirty(world, pos);
                if (server != null) {
                    server.execute(() -> drawInventoryContainer(gui, admin, inv, world, pos, server));
                }
            });
            gui.setSlot(i, b.build());
        }
    }
}
