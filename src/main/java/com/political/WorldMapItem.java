package com.political;


import com.political.settlement.Settlement;
import com.political.settlement.SettlementManager;
import com.political.settlement.SettlementTier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a genuine vanilla filled map, so the Atlas can hand the player a real Minecraft map that
 * renders the actual terrain and blocks as they carry it, rather than only a grid of icons.
 *
 * Settlements the map covers are listed on the item so the player knows what the picture will show
 * once it fills in.
 */
public final class WorldMapItem {

    private WorldMapItem() {}

    /** Map scale byte (0..4). 2 covers a wide area while still showing recognisable terrain. */
    private static final byte SCALE = 2;

    /** Creates a real filled map centred on the player and starts it rendering the terrain here. */
    public static ItemStack createFor(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        int cx = player.blockPosition().getX();
        int cz = player.blockPosition().getZ();

        // Vanilla helper: creates the FILLED_MAP, allocates a MapId and registers the saved data.
        ItemStack map = MapItem.create(level, cx, cz, SCALE, true, false);

        // The map fills in the real terrain as the player carries it and moves, exactly like any
        // vanilla explorer map. We only dress it with a name and a legend of what it covers.
        map.set(DataComponents.CUSTOM_NAME,
                Component.literal("✦ Realm Atlas Map").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§7A real map of the world around you."));
        lore.add(Component.literal("§8Carry it and walk to fill in the terrain."));
        lore.add(Component.literal(""));
        List<Settlement> nearby = settlementsOnMap(level, cx, cz);
        if (nearby.isEmpty()) {
            lore.add(Component.literal("§8No known settlements in view."));
        } else {
            lore.add(Component.literal("§7Settlements in view:"));
            for (Settlement s : nearby) {
                SettlementTier tier = s.tierEnum();
                lore.add(Component.literal("  " + tier.colorCode + "● §f" + s.name
                        + " §8(" + s.cornerstoneX + ", " + s.cornerstoneZ + ")"));
            }
        }
        map.set(DataComponents.LORE, new ItemLore(lore));
        return map;
    }

    /** Settlements whose cornerstone falls within this map's covered square. */
    private static List<Settlement> settlementsOnMap(ServerLevel level, int centerX, int centerZ) {
        int radius = 64 * (1 << SCALE);   // half-width of the map in blocks
        String dim = level.dimension().identifier().toString();
        List<Settlement> out = new ArrayList<>();
        for (Settlement s : SettlementManager.getAllSettlements()) {
            if (!dim.equals(s.worldDim)) continue;
            if (Math.abs(s.cornerstoneX - centerX) <= radius
                    && Math.abs(s.cornerstoneZ - centerZ) <= radius) {
                out.add(s);
            }
        }
        return out;
    }

    /** Gives the player a fresh world map, or tops up if their inventory is full. */
    public static void give(ServerPlayer player) {
        ItemStack map = createFor(player);
        if (!player.getInventory().add(map)) {
            player.drop(map, false);
        }
        player.sendSystemMessage(Component.literal("§aYou receive a §6Realm Atlas Map§a. "
                + "Hold it and walk to reveal the land.").withStyle(ChatFormatting.GREEN));
    }
}
