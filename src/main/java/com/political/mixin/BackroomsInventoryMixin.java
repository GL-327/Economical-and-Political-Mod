package com.political.mixin;

/**
 * Backrooms inventory dimension-change handler.
 *
 * NOTE: Dimension-change detection uses Fabric's ServerEntityWorldChangeEvents API,
 * not a Mixin. This class exists as a registration placeholder.
 * The actual event registration is performed in
 * {@link com.political.backrooms.inventory.BackroomsSpawnManager#register(net.minecraft.server.MinecraftServer)}.
 *
 * This file is intentionally non-functional as a mixin — it is kept here to document
 * that the inventory system hooks into dimension transitions.
 */
public class BackroomsInventoryMixin {
    // No-op: event registration is in BackroomsSpawnManager.register()
}
