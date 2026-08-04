package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.MapItemColor;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The Atlas as an object you can hold.
 *
 * <p>This is the whole point of the rewrite. The Atlas is not a screen any more: it is a locked
 * vanilla map with a picture painted into it, which the player carries, hangs in an item frame,
 * copies on a cartography table and hands to somebody else. Everything the old grid of glass panes
 * tried to say — who owns what, who is at war, where the capital is — is drawn on the paper.</p>
 *
 * <p>Locking is what makes that possible. An unlocked map is rewritten by vanilla every tick a
 * player carries it, which would scrub the drawing off within seconds of it being handed over. A
 * locked one keeps whatever is written into it, and still pushes updates to whoever is holding it,
 * so redrawing an atlas in place works and the holder sees it change in their hand.</p>
 */
public final class AtlasMap {

    private AtlasMap() {}

    private static final String TAG_ATLAS = "political_atlas";
    private static final String TAG_ZOOM = "political_atlas_zoom";

    // ── Issuing ───────────────────────────────────────────────────────────

    /**
     * Draws a fresh atlas centred on the player and puts it in their hands.
     *
     * <p>The drawing may finish a moment later than the item does, because reading two thousand
     * blocks of biome is done off the server thread. The player gets a blank sheet that fills in,
     * which is a better failure mode than the server stuttering while they wait.</p>
     */
    public static void give(ServerPlayer player, AtlasCartographer.Zoom zoom) {
        ServerLevel level = (ServerLevel) player.level();
        ItemStack map = issue(level, player.blockPosition().getX(), player.blockPosition().getZ(),
                zoom, player);
        if (!player.getInventory().add(map)) {
            player.drop(map, false);
        }
        player.sendSystemMessage(Component.literal("§6✦ §f" + zoom.display
                + "§7 drawn. §8Hold it to read it — the picture is on the paper."));
    }

    /** Builds the item and starts the drawing. */
    public static ItemStack issue(ServerLevel level, int centreX, int centreZ,
                                  AtlasCartographer.Zoom zoom, ServerPlayer viewer) {
        MapItemSavedData data = MapItemSavedData
                .createFresh(centreX, centreZ, zoom.scale, false, false, level.dimension())
                .locked();

        MapId id = level.getFreeMapId();
        level.setMapData(id, data);

        ItemStack stack = new ItemStack(Items.FILLED_MAP);
        stack.set(DataComponents.MAP_ID, id);
        stack.set(DataComponents.MAP_COLOR, new MapItemColor(0xC8A24A));

        CompoundTag tag = new CompoundTag();
        tag.putByte(TAG_ATLAS, (byte) 1);
        tag.putString(TAG_ZOOM, zoom.name());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        repaint(level, data, zoom, viewer);
        dress(stack, level, data, zoom);
        return stack;
    }

    // ── Redrawing ─────────────────────────────────────────────────────────

    /**
     * Redraws every atlas the player is carrying, in place.
     *
     * <p>In place matters: a player who has framed their atlas on a wall, or handed a copy to an
     * ally, should not have to throw it away and be issued a new one every time a settlement grows.
     * Returns how many sheets were brought up to date.</p>
     */
    public static int refreshHeld(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        int done = 0;
        for (ItemStack stack : player.getInventory()) {
            if (!isAtlas(stack)) continue;
            MapId id = stack.get(DataComponents.MAP_ID);
            if (id == null) continue;
            MapItemSavedData data = level.getMapData(id);
            if (data == null) continue;

            AtlasCartographer.Zoom zoom = zoomOf(stack);
            repaint(level, data, zoom, player);
            dress(stack, level, data, zoom);
            done++;
        }
        return done;
    }

    /** Whether a stack is one of ours rather than an ordinary explorer's map. */
    public static boolean isAtlas(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.is(Items.FILLED_MAP)) return false;
        CustomData custom = stack.get(DataComponents.CUSTOM_DATA);
        if (custom == null) return false;
        return custom.copyTag().contains(TAG_ATLAS);
    }

    private static AtlasCartographer.Zoom zoomOf(ItemStack stack) {
        CustomData custom = stack.get(DataComponents.CUSTOM_DATA);
        if (custom == null) return AtlasCartographer.Zoom.REGION;
        String name = custom.copyTag().getStringOr(TAG_ZOOM, "");
        try {
            return AtlasCartographer.Zoom.valueOf(name);
        } catch (IllegalArgumentException e) {
            return AtlasCartographer.Zoom.REGION;
        }
    }

    /**
     * Pushes a finished picture into a map's pixels.
     *
     * <p>Written one pixel at a time on purpose. Assigning the array wholesale would leave the map
     * marked clean, and nobody holding it would ever be sent the change.</p>
     */
    private static void repaint(ServerLevel level, MapItemSavedData data,
                                AtlasCartographer.Zoom zoom, ServerPlayer viewer) {
        AtlasCartographer.draw(level, data.centerX, data.centerZ, zoom, viewer, pixels -> {
            for (int y = 0; y < AtlasCartographer.SIZE; y++) {
                for (int x = 0; x < AtlasCartographer.SIZE; x++) {
                    data.setColor(x, y, pixels[y * AtlasCartographer.SIZE + x]);
                }
            }
        });
    }

    // ── The item itself ───────────────────────────────────────────────────

    /** Name, legend and the list of what the sheet covers. */
    private static void dress(ItemStack stack, ServerLevel level, MapItemSavedData data,
                              AtlasCartographer.Zoom zoom) {
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("✦ " + zoom.display)
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        int reach = zoom.span() / 2;
        String dim = level.dimension().identifier().toString();

        List<Settlement> covered = new ArrayList<>();
        for (Settlement s : SettlementManager.getAllSettlements()) {
            if (!dim.equals(s.worldDim)) continue;
            if (Math.abs(s.cornerstoneX - data.centerX) <= reach
                    && Math.abs(s.cornerstoneZ - data.centerZ) <= reach) {
                covered.add(s);
            }
        }
        covered.sort((a, b) -> Integer.compare(b.tierEnum().rank(), a.tierEnum().rank()));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§8" + zoom.blurb));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§7Covers §f" + zoom.span() + "§7 blocks across"));
        lore.add(Component.literal("§7Centred on §f" + data.centerX + ", " + data.centerZ));
        lore.add(Component.literal(""));

        if (covered.isEmpty()) {
            lore.add(Component.literal("§8No settlement on this sheet."));
            lore.add(Component.literal("§8Place a Cornerstone to found one."));
        } else {
            lore.add(Component.literal("§7On this sheet:"));
            int shown = 0;
            for (Settlement s : covered) {
                if (shown++ >= 8) {
                    lore.add(Component.literal("§8  and " + (covered.size() - 8) + " more"));
                    break;
                }
                SettlementTier tier = s.tierEnum();
                lore.add(Component.literal("  " + tier.colorCode + "■ §f" + s.name
                        + " §8" + tier.displayName.toLowerCase(Locale.ROOT)));
            }
        }

        lore.add(Component.literal(""));
        lore.add(Component.literal("§7Reading it"));
        lore.add(Component.literal("§8  hatched ground — a settlement's claim"));
        lore.add(Component.literal("§8  gold points — the seat of government"));
        lore.add(Component.literal("§8  brown dashes — a road to the nearest city"));
        lore.add(Component.literal("§8  red line — an open war"));
        lore.add(Component.literal("§8  red cross — you"));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Sealed, so it keeps its drawing. Use §7/atlas redraw"));
        lore.add(Component.literal("§8to bring every sheet you carry up to date."));

        stack.set(DataComponents.LORE, new ItemLore(lore));
    }
}
