package com.political;

import com.political.core.gui.ReturnTo;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * The custom model console.
 *
 * <p>Wiring the models up was the easy half. The hard half is that seven thousand items is far too
 * many to track by memory, so this screen exists to answer three questions: is Polymer actually
 * loaded, how many items have art, and which ones still need it.</p>
 */
public final class ItemModelGui {

    private ItemModelGui() {}

    private static final int SIZE = 54;

    /** Where Back leads from this screen's first page: the menu that opened it, remembered per player. */
    private static final ReturnTo FROM = new ReturnTo();

    /** Opens the screen from a parent menu (Back returns there), or with {@code null} from a command (no Back). */
    public static void openFrom(ServerPlayer player, Runnable back) {
        FROM.set(player.getUUID(), back);
        open(player);
    }

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Custom Item Models")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        int have = PolymerBridge.knownModels();
        int seen = PolymerBridge.seenItems();
        boolean polymer = PolymerBridge.polymerLoaded();

        GuiKit.header(gui, Items.ITEM_FRAME, "Custom Item Models", ChatFormatting.LIGHT_PURPLE,
                have + " of " + seen + " items have art",
                polymer ? "Polymer is loaded" : "Polymer is not loaded");

        gui.setSlot(19, GuiKit.info(polymer ? Items.DYE.lime() : Items.DYE.gray(),
                "Polymer", polymer ? ChatFormatting.GREEN : ChatFormatting.GRAY,
                polymer ? "Loaded and serving the pack."
                        : "Not loaded. Models will not reach clients.",
                "",
                "Polymer builds and hands out the resource",
                "pack that defines these models, so players",
                "get them without installing anything."));

        gui.setSlot(20, GuiKit.info(Items.PAINTING, "Coverage", ChatFormatting.AQUA,
                have + " with art",
                Math.max(0, seen - have) + " still on their base item look",
                GuiKit.bar(seen == 0 ? 0 : have / (double) seen,
                        ChatFormatting.GREEN, ChatFormatting.DARK_GRAY)));

        gui.setSlot(21, GuiKit.toggle("Apply models", PolymerBridge.enabled,
                () -> {
                    PolymerBridge.enabled = !PolymerBridge.enabled;
                    open(player);
                },
                "Only ever touches items that have art,",
                "so leaving this on is safe."));

        gui.setSlot(22, GuiKit.action(Items.SPYGLASS, "Rescan for art", ChatFormatting.AQUA,
                () -> {
                    PolymerBridge.reload();
                    player.sendSystemMessage(Component.literal("§aRescanned. "
                            + PolymerBridge.knownModels() + " models found."));
                    open(player);
                },
                "Rereads config/political/item_models",
                "so art can be added without a restart"));

        gui.setSlot(23, GuiKit.action(Items.WRITABLE_BOOK, "Write the worklist", ChatFormatting.GOLD,
                () -> {
                    int n = PolymerBridge.writeManifest();
                    player.sendSystemMessage(Component.literal("§aWrote " + n
                            + " entries to §fconfig/political/item_model_manifest.txt"));
                    open(player);
                },
                "Every item, whether it has art,",
                "and where that art goes"));

        // What still needs doing, so the screen is useful rather than just informative.
        List<String> missing = PolymerBridge.missing(GuiKit.LIST_SLOTS.length);
        List<GuiElement> entries = new ArrayList<>();
        for (String id : missing) {
            entries.add(new GuiElementBuilder(Items.BARRIER)
                    .setName(Component.literal(id).withStyle(ChatFormatting.RED))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7No model yet."))
                    .addLoreLine(Component.literal("§8Drop a json at"))
                    .addLoreLine(Component.literal("§8config/political/item_models/" + id + ".json"))
                    .build());
        }
        for (int i = 0; i < entries.size() && i < 14; i++) {
            gui.setSlot(GuiKit.LIST_SLOTS[i + 14], entries.get(i));
        }

        if (missing.isEmpty() && seen > 0) {
            gui.setSlot(40, GuiKit.info(Items.NETHER_STAR, "Every item has art",
                    ChatFormatting.LIGHT_PURPLE, "Nothing left on its base look."));
        }

        gui.setSlot(48, GuiKit.info(Items.PAPER, "How this works", ChatFormatting.WHITE,
                "Items are dressed vanilla stacks, not registered",
                "items, which is why they run server side and also",
                "why they have always looked like their base item.",
                "",
                "The item_model component points a stack at a model",
                "without registering anything. Polymer ships the pack",
                "that defines it.",
                "",
                "An item with no art is left alone entirely, because",
                "pointing at a missing model renders as the missing",
                "texture rather than falling back."));

        GuiKit.footer(gui, SIZE, player, FROM.get(player.getUUID()));
        gui.open();
    }
}
