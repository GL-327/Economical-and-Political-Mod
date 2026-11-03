package com.political;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.political.settlement.Settlement;
import com.political.settlement.SettlementProgressGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

/**
 * The custom village directory.
 *
 * <p>Two village types ship with the mod that vanilla has no equivalent for, and until you stumble
 * into the right biome there is no way to know whether they are generating at all. This screen
 * answers that directly: pick a type and it runs the server's own structure search for it, which
 * either returns coordinates or tells you plainly that none was found within the search radius.</p>
 *
 * <p>The search runs through the vanilla locate command rather than the generator API on purpose.
 * Locate is the same code path a server owner would use to check by hand, so if this screen and a
 * manual check ever disagree, the bug is here rather than somewhere subtle in worldgen.</p>
 */
public final class VillageAtlasGui {
    private static final Logger LOGGER = LoggerFactory.getLogger(VillageAtlasGui.class);


    private VillageAtlasGui() {}

    private static final int SIZE = 54;

    /** The village types this mod adds, with the biome each one needs. */
    private record Village(String id, String display, String biome, ChatFormatting colour,
                           net.minecraft.world.item.Item icon, String note) {}

    private static final Village[] VILLAGES = {
            new Village("vsrevamped:village_cherry", "Cherry Village", "Cherry Grove",
                    ChatFormatting.LIGHT_PURPLE, Items.CHERRY_SAPLING,
                    "Forty five pieces. Cherry grove has no vanilla village, so this fills a gap."),
            new Village("vsrevamped:village_swamp", "Swamp Village", "Swamp",
                    ChatFormatting.DARK_GREEN, Items.LILY_PAD,
                    "Forty two pieces. Swamp has no vanilla village either.")
    };

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Custom Villages")
                .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        GuiKit.header(gui, Items.CAMPFIRE, "Custom Villages", ChatFormatting.GREEN,
                VILLAGES.length + " types added by this mod",
                "They generate on their own, like vanilla villages");

        int[] slots = {20, 24};
        for (int i = 0; i < VILLAGES.length && i < slots.length; i++) {
            Village v = VILLAGES[i];
            gui.setSlot(slots[i], GuiKit.action(v.icon(), v.display(), v.colour(),
                    () -> {
                        player.sendSystemMessage(Component.literal("§7Searching for §f"
                                + v.display() + "§7..."));
                        run(player, "locate structure " + v.id());
                        player.closeContainer();
                    },
                    "Generates in " + v.biome(),
                    v.note(),
                    "",
                    "Click to locate the nearest one"));
        }

        gui.setSlot(31, GuiKit.info(Items.WRITABLE_BOOK, "If nothing is found", ChatFormatting.WHITE,
                "Locate only searches so far from where you stand.",
                "Both types need their own biome, so stand in or",
                "near one before searching.",
                "",
                "A result means the structure is registered and",
                "placing correctly. No result usually means the",
                "biome is simply not nearby."));

        gui.setSlot(38, GuiKit.action(Items.COMPASS, "Locate a vanilla village", ChatFormatting.GRAY,
                () -> {
                    run(player, "locate structure #minecraft:village");
                    player.closeContainer();
                },
                "A control test.",
                "If this works and the custom ones do not,",
                "the problem is the mod rather than the seed."));

        gui.setSlot(42, GuiKit.tile(Items.BEACON, "Settlement tiers", ChatFormatting.LIGHT_PURPLE,
                () -> SettlementProgressGui.openList(player),
                "Your own settlements and what is blocking them"));

        GuiKit.footer(gui, SIZE, player, null);
        gui.open();
    }

    private static void run(ServerPlayer player, String command) {
        MinecraftServer server = ((ServerLevel) player.level()).getServer();
        if (server == null) return;
        server.execute(() -> {
            try {
                server.getCommands().getDispatcher()
                        .execute(command, player.createCommandSourceStack());
            } catch (Exception e) {
                LOGGER.warn("VillageAtlasGui.run: ignored an error", e);
                player.sendSystemMessage(Component.literal(
                        "§cNothing found nearby. Try standing in the right biome."));
            }
        });
    }
}
