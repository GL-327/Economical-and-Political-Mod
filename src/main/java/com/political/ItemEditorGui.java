package com.political;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The item editor.
 *
 * <p>Works on the item in the admin's hand and edits every copy of it at once. That is the whole
 * point of the thing: an admin who nerfs a sword here has nerfed the four hundred already in
 * players' enderchests, without a world scan and without asking anybody to log out.</p>
 *
 * <p>What may be applied depends on what the item is. A chestplate offers armour abilities, a sword
 * offers weapon abilities, and the type itself can be changed, so a chestplate can be turned into a
 * sword and keep its texture while ceasing to be wearable. Stats and rarity and name and colour are
 * offered on everything, since those make sense on anything.</p>
 */
public final class ItemEditorGui {

    private ItemEditorGui() {}

    private static final int SIZE = 54;

    // ── Entry ─────────────────────────────────────────────────────────────

    public static void open(ServerPlayer player) {
        ItemStack held = player.getMainHandItem();
        String id = SlayerItems.getCustomItemId(held);

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Item editor")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        if (held.isEmpty()) {
            GuiKit.header(gui, Items.ANVIL, "Item editor", ChatFormatting.GOLD,
                    "Hold the item you want to edit",
                    ItemOverrideManager.count() + " items have been edited so far",
                    "Or build a new one from nothing");
            gui.setSlot(21, GuiKit.action(Items.BARRIER, "Nothing in hand", ChatFormatting.RED,
                    () -> open(player),
                    "Hold something and open this again."));
            gui.setSlot(23, GuiKit.action(Items.NETHER_STAR, "Make a new item",
                    ChatFormatting.LIGHT_PURPLE,
                    () -> openCreate(player),
                    "Build one from nothing, name it,",
                    "colour it, give it stats and a recipe."));
            gui.setSlot(31, GuiKit.action(Items.WRITABLE_BOOK, "Everything edited so far",
                    ChatFormatting.AQUA,
                    () -> openLedger(player, 0),
                    ItemOverrideManager.count() + " ids carry an edit",
                    "Review or undo any of them."));
            GuiKit.footer(gui, SIZE, player, () -> AdminPanel.open(player));
            gui.open();
            return;
        }

        if (id == null) {
            GuiKit.header(gui, Items.ANVIL, "Item editor", ChatFormatting.GOLD,
                    "That is a plain vanilla item",
                    "It has no id to hang an edit on",
                    "Give it one first");
            gui.setSlot(22, GuiKit.action(Items.NAME_TAG, "Give it an id", ChatFormatting.YELLOW,
                    () -> GuiKit.prompt(player, "New item id", raw -> {
                        String clean = slug(raw);
                        if (clean.isEmpty()) {
                            player.sendSystemMessage(Component.literal("§cThat is not a usable id."));
                            open(player);
                            return;
                        }
                        SlayerItems.setCustomItemId(player.getMainHandItem(), clean);
                        player.sendSystemMessage(Component.literal("§aThat is now §f" + clean));
                        open(player);
                    }, () -> open(player)),
                    "An id is how every copy of this item is",
                    "recognised, which is what lets one edit",
                    "reach all of them at once.",
                    "",
                    "§8Lowercase, underscores, no spaces."));
            GuiKit.footer(gui, SIZE, player, () -> AdminPanel.open(player));
            gui.open();
            return;
        }

        ItemOverrideManager.Override o = ItemOverrideManager.get(id);
        Kind kind = kindOf(held, o);

        GuiKit.header(gui, Items.ANVIL, "Item editor", ChatFormatting.GOLD,
                "Editing " + held.getHoverName().getString(),
                "id " + id + ", treated as a " + kind.label.toLowerCase(),
                o == null ? "No edits yet" : "Revision " + o.revision + ", live everywhere");

        gui.setSlot(4, preview(held, id, o, kind));

        // ── Identity ──
        gui.setSlot(10, GuiKit.action(Items.NAME_TAG, "Name", ChatFormatting.WHITE,
                () -> GuiKit.prompt(player, "New name", raw -> {
                    ItemOverrideManager.Override ov = ItemOverrideManager.getOrCreate(id);
                    ov.name = raw.isBlank() ? null : raw;
                    ItemOverrideManager.commit(ov);
                    say(player, "Renamed every copy to " + raw);
                    open(player);
                }, () -> open(player)),
                o != null && o.name != null ? "Now: " + o.name : "Unchanged",
                "",
                "Changes the name on every copy that exists,",
                "including ones in chests nobody has opened."));

        gui.setSlot(11, GuiKit.action(Items.RED_DYE, "Colour", ChatFormatting.RED,
                () -> openColour(player, id),
                o != null && o.colour != null ? "Now: " + o.colour : "Unchanged",
                "",
                "Pick from the sixteen, or type a hex code",
                "for anything else."));

        gui.setSlot(12, GuiKit.action(Items.ITEM_FRAME, "Texture code", ChatFormatting.AQUA,
                () -> GuiKit.prompt(player, "Model id", raw -> {
                    ItemOverrideManager.Override ov = ItemOverrideManager.getOrCreate(id);
                    ov.texture = raw.isBlank() ? null : raw.trim();
                    ItemOverrideManager.commit(ov);
                    say(player, "Texture set to " + raw);
                    open(player);
                }, () -> open(player)),
                o != null && o.texture != null ? "Now: " + o.texture : "Uses the item's own id",
                "",
                "The model a resource pack draws for this.",
                "Defaults to the item id, so most items",
                "already have one without setting it here."));

        gui.setSlot(13, GuiKit.action(Items.NETHER_STAR, "Rarity", ChatFormatting.LIGHT_PURPLE,
                () -> openRarity(player, id),
                o != null && o.rarity != null
                        ? "Now: " + ItemOverrideManager.rarityColour(o.rarity) + o.rarity
                        : "Unchanged",
                "",
                "Common through Primal, plus Special."));

        // ── Type ──
        gui.setSlot(14, GuiKit.action(Items.SMITHING_TABLE, "Type", ChatFormatting.GOLD,
                () -> openKind(player, id),
                "Now: " + kind.label,
                "",
                "Turn a chestplate into a sword and it keeps",
                "its texture and stops being wearable.",
                "",
                "Type decides which abilities are on offer."));

        gui.setSlot(15, GuiKit.action(Items.ARMOR_STAND, "Base item", ChatFormatting.YELLOW,
                () -> GuiKit.prompt(player, "Item id, eg diamond_sword", raw -> {
                    String clean = slug(raw);
                    if (BuiltInRegistries.ITEM.getOptional(
                            Identifier.fromNamespaceAndPath("minecraft", clean)).isEmpty()) {
                        player.sendSystemMessage(Component.literal(
                                "§cThere is no vanilla item called " + clean + "."));
                        open(player);
                        return;
                    }
                    ItemOverrideManager.Override ov = ItemOverrideManager.getOrCreate(id);
                    ov.baseItem = clean;
                    ItemOverrideManager.commit(ov);
                    say(player, "Base item set to " + clean
                            + ". Existing copies keep their own shape.");
                    open(player);
                }, () -> open(player)),
                o != null && o.baseItem != null ? "Now: " + o.baseItem : "Unchanged",
                "",
                "§8Applies to copies made from here on.",
                "§8A stack already in the world cannot change",
                "§8which vanilla item it is."));

        gui.setSlot(16, GuiKit.action(Items.GLOWSTONE_DUST, "Glint", ChatFormatting.AQUA,
                () -> {
                    ItemOverrideManager.Override ov = ItemOverrideManager.getOrCreate(id);
                    ov.glint = ov.glint == null || !ov.glint;
                    ItemOverrideManager.commit(ov);
                    open(player);
                },
                o != null && o.glint != null ? (o.glint ? "On" : "Off") : "Unchanged",
                "The enchanted shimmer."));

        // ── Numbers and powers ──
        gui.setSlot(28, GuiKit.action(Items.DIAMOND, "Stats", ChatFormatting.GREEN,
                () -> openStats(player, id, 0),
                o == null || o.stats.isEmpty()
                        ? "None set" : o.stats.size() + " set",
                "",
                "Every stat is offered on every item, because",
                "health on a pickaxe is a decision, not a bug."));

        gui.setSlot(29, GuiKit.action(Items.BLAZE_POWDER, "Abilities", ChatFormatting.GOLD,
                () -> openAbilities(player, id, kind, 0),
                o == null || o.abilities.isEmpty()
                        ? "None set" : o.abilities.size() + " set",
                "",
                "Only the ones that fit a " + kind.label.toLowerCase() + ".",
                "Change the type to change what is offered."));

        gui.setSlot(30, GuiKit.action(Items.ENCHANTED_BOOK, "Enchantments", ChatFormatting.DARK_PURPLE,
                () -> openEnchants(player, id, 0),
                o == null || o.enchants.isEmpty()
                        ? "None set" : o.enchants.size() + " baked in",
                "",
                "Baked onto every copy, unlike the ones a",
                "player buys at the bench."));

        gui.setSlot(31, GuiKit.action(Items.WRITABLE_BOOK, "Extra lore", ChatFormatting.GRAY,
                () -> openLore(player, id),
                o == null || o.lore.isEmpty() ? "None" : o.lore.size() + " lines",
                "Flavour text under the stats."));

        // ── Recipe and ledger ──
        gui.setSlot(32, GuiKit.action(Items.CRAFTING_TABLE, "Recipe", ChatFormatting.YELLOW,
                () -> RecipeEditorGui.openMainMenu(player),
                "Open the recipe editor",
                "so this can be crafted."));

        gui.setSlot(33, GuiKit.action(Items.CHEST, "Give me one", ChatFormatting.AQUA,
                () -> {
                    ItemStack copy = player.getMainHandItem().copy();
                    copy.setCount(1);
                    ItemOverrideManager.apply(copy);
                    if (!player.getInventory().add(copy)) {
                        player.drop(copy, false);
                    }
                    say(player, "There you go.");
                    open(player);
                },
                "A fresh copy with every edit applied."));

        gui.setSlot(34, GuiKit.action(Items.BARRIER, "Undo every edit", ChatFormatting.RED,
                () -> GuiKit.confirm(player, "Undo every edit to " + id,
                        "Every copy goes back to how it was built. Nothing is deleted.",
                        () -> {
                            ItemOverrideManager.clear(id);
                            say(player, "Edits dropped. Copies revert as they are handled.");
                            open(player);
                        },
                        () -> open(player)),
                o == null ? "Nothing to undo" : "Drops all " + o.revision + " revisions"));

        gui.setSlot(SIZE - 5, GuiKit.info(Items.ENDER_EYE, "How edits reach old copies",
                ChatFormatting.WHITE,
                "There is no list of every copy of an item",
                "and no way to walk an unloaded chunk to",
                "find one, so nothing is rewritten in place.",
                "",
                "The edit is stored against the id and each",
                "copy carries a revision number. A copy that",
                "is behind rebuilds itself the moment it is",
                "next handled.",
                "",
                "In practice that means within two seconds",
                "for anything a player is carrying, and on",
                "pickup for anything in a chest."));

        GuiKit.footer(gui, SIZE, player, () -> AdminPanel.open(player));
        gui.open();
    }

    // ── Preview ───────────────────────────────────────────────────────────

    private static GuiElement preview(ItemStack held, String id,
                                      ItemOverrideManager.Override o, Kind kind) {
        GuiElementBuilder b = new GuiElementBuilder(held.getItem())
                .setName(held.getHoverName().copy())
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§8" + id))
                .addLoreLine(Component.literal("§7Treated as §f" + kind.label));
        if (o == null) {
            b.addLoreLine(Component.literal("§8No edits stored."));
        } else {
            b.addLoreLine(Component.literal("§7Revision §f" + o.revision));
            if (o.name != null) b.addLoreLine(Component.literal("§7Name §f" + o.name));
            if (o.colour != null) b.addLoreLine(Component.literal("§7Colour §f" + o.colour));
            if (o.texture != null) b.addLoreLine(Component.literal("§7Texture §f" + o.texture));
            if (o.rarity != null) b.addLoreLine(Component.literal("§7Rarity §f" + o.rarity));
            if (!o.stats.isEmpty()) {
                b.addLoreLine(Component.literal("§7Stats §f" + o.stats.size()));
            }
            if (!o.abilities.isEmpty()) {
                b.addLoreLine(Component.literal("§7Abilities §f" + o.abilities.size()));
            }
            b.glow();
        }
        return b.build();
    }

    // ── Colour ────────────────────────────────────────────────────────────

    private static void openColour(ServerPlayer player, String id) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Colour").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);
        GuiKit.header(gui, Items.RED_DYE, "Colour", ChatFormatting.RED,
                "Recolours the name on every copy",
                "The sixteen, or anything as hex",
                "Editing " + id);

        ChatFormatting[] set = {
                ChatFormatting.BLACK, ChatFormatting.DARK_BLUE, ChatFormatting.DARK_GREEN,
                ChatFormatting.DARK_AQUA, ChatFormatting.DARK_RED, ChatFormatting.DARK_PURPLE,
                ChatFormatting.GOLD, ChatFormatting.GRAY, ChatFormatting.DARK_GRAY,
                ChatFormatting.BLUE, ChatFormatting.GREEN, ChatFormatting.AQUA,
                ChatFormatting.RED, ChatFormatting.LIGHT_PURPLE, ChatFormatting.YELLOW,
                ChatFormatting.WHITE
        };
        Item[] dyes = {
                Items.INK_SAC, Items.BLUE_DYE, Items.GREEN_DYE, Items.CYAN_DYE,
                Items.RED_DYE, Items.PURPLE_DYE, Items.ORANGE_DYE, Items.LIGHT_GRAY_DYE,
                Items.GRAY_DYE, Items.LIGHT_BLUE_DYE, Items.LIME_DYE, Items.CYAN_DYE,
                Items.PINK_DYE, Items.MAGENTA_DYE, Items.YELLOW_DYE, Items.WHITE_DYE
        };

        for (int i = 0; i < set.length; i++) {
            ChatFormatting f = set[i];
            gui.setSlot(GuiKit.LIST_SLOTS[i], new GuiElementBuilder(dyes[i])
                    .setName(Component.literal(prettify(f.getName())).withStyle(f, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Code §f§" + f.getChar()))
                    .addLoreLine(Component.literal("§eClick to use this."))
                    .setCallback((s, t, a, sg) -> {
                        ItemOverrideManager.Override ov = ItemOverrideManager.getOrCreate(id);
                        ov.colour = String.valueOf(f.getChar());
                        ItemOverrideManager.commit(ov);
                        say(player, "Recoloured every copy.");
                        open(player);
                    }).build());
        }

        gui.setSlot(SIZE - 6, GuiKit.action(Items.PAINTING, "Hex colour", ChatFormatting.LIGHT_PURPLE,
                () -> GuiKit.prompt(player, "#rrggbb", raw -> {
                    String h = raw.trim();
                    if (!h.startsWith("#")) h = "#" + h;
                    if (!h.matches("#[0-9a-fA-F]{6}")) {
                        player.sendSystemMessage(Component.literal(
                                "§cThat is not a hex colour. It looks like #ffaa00."));
                        openColour(player, id);
                        return;
                    }
                    ItemOverrideManager.Override ov = ItemOverrideManager.getOrCreate(id);
                    ov.colour = h;
                    ItemOverrideManager.commit(ov);
                    say(player, "Recoloured every copy to " + h);
                    open(player);
                }, () -> open(player)),
                "Anything outside the sixteen.",
                "Written as #ffaa00."));

        gui.setSlot(SIZE - 4, GuiKit.action(Items.BARRIER, "Leave it alone", ChatFormatting.GRAY,
                () -> {
                    ItemOverrideManager.Override ov = ItemOverrideManager.getOrCreate(id);
                    ov.colour = null;
                    ItemOverrideManager.commit(ov);
                    open(player);
                },
                "Back to whatever colour it was built with."));

        GuiKit.footer(gui, SIZE, player, () -> open(player));
        gui.open();
    }

    // ── Rarity ────────────────────────────────────────────────────────────

    private static final String[] RARITIES = {
            "COMMON", "UNCOMMON", "RARE", "EPIC", "LEGENDARY", "MYTHIC", "DIVINE", "PRIMAL", "SPECIAL"
    };

    private static void openRarity(ServerPlayer player, String id) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Rarity")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);
        GuiKit.header(gui, Items.NETHER_STAR, "Rarity", ChatFormatting.LIGHT_PURPLE,
                "Sets the band on every copy",
                "Editing " + id,
                RARITIES.length + " bands");

        for (int i = 0; i < RARITIES.length; i++) {
            String r = RARITIES[i];
            gui.setSlot(GuiKit.LIST_SLOTS[i], new GuiElementBuilder(Items.NETHER_STAR)
                    .setName(Component.literal(ItemOverrideManager.rarityColour(r) + "§l" + r))
                    .addLoreLine(Component.literal("§eClick to set this."))
                    .setCallback((s, t, a, sg) -> {
                        ItemOverrideManager.Override ov = ItemOverrideManager.getOrCreate(id);
                        ov.rarity = r;
                        ItemOverrideManager.commit(ov);
                        say(player, "Every copy is now " + r + ".");
                        open(player);
                    }).build());
        }

        gui.setSlot(SIZE - 5, GuiKit.action(Items.BARRIER, "Leave it alone", ChatFormatting.GRAY,
                () -> {
                    ItemOverrideManager.Override ov = ItemOverrideManager.getOrCreate(id);
                    ov.rarity = null;
                    ItemOverrideManager.commit(ov);
                    open(player);
                },
                "Back to whatever it was built with."));

        GuiKit.footer(gui, SIZE, player, () -> open(player));
        gui.open();
    }

    // ── Type ──────────────────────────────────────────────────────────────

    /** What an item counts as, which decides which abilities it may carry. */
    public enum Kind {
        WEAPON("Weapon", Items.DIAMOND_SWORD),
        ARMOUR("Armour", Items.DIAMOND_CHESTPLATE),
        TOOL("Tool", Items.DIAMOND_PICKAXE),
        BOW("Ranged", Items.BOW),
        ACCESSORY("Accessory", Items.AMETHYST_SHARD),
        ITEM("Plain item", Items.PAPER);

        public final String label;
        public final Item icon;

        Kind(String label, Item icon) {
            this.label = label;
            this.icon = icon;
        }
    }

    /** What the item counts as, honouring an admin override before guessing from the item. */
    public static Kind kindOf(ItemStack stack, ItemOverrideManager.Override o) {
        if (o != null && o.kind != null) {
            try {
                return Kind.valueOf(o.kind.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                // A stored kind that no longer exists falls through to the guess.
            }
        }
        if (CustomEnchantManager.isBow(stack)) return Kind.BOW;
        if (CustomEnchantManager.isWeapon(stack)) return Kind.WEAPON;
        if (CustomEnchantManager.isArmour(stack)) return Kind.ARMOUR;
        if (CustomEnchantManager.isTool(stack)) return Kind.TOOL;
        return Kind.ITEM;
    }

    private static void openKind(ServerPlayer player, String id) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Type").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        ItemOverrideManager.Override o = ItemOverrideManager.get(id);
        Kind now = kindOf(player.getMainHandItem(), o);

        GuiKit.header(gui, Items.SMITHING_TABLE, "Type", ChatFormatting.GOLD,
                "Currently a " + now.label.toLowerCase(),
                "Decides which abilities are on offer",
                "Editing " + id);

        int i = 0;
        for (Kind k : Kind.values()) {
            boolean current = k == now;
            gui.setSlot(GuiKit.LIST_SLOTS[i++], new GuiElementBuilder(k.icon)
                    .setName(Component.literal(k.label).withStyle(
                            current ? ChatFormatting.GREEN : ChatFormatting.WHITE,
                            ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7" + abilitiesFor(k).size()
                            + " abilities can go on this"))
                    .addLoreLine(Component.literal(k == Kind.ARMOUR
                            ? "§7Wearable" : "§7Not wearable"))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal(current
                            ? "§aThis is what it is." : "§eClick to make it this."))
                    .setCallback((s, t, a, sg) -> {
                        ItemOverrideManager.Override ov = ItemOverrideManager.getOrCreate(id);
                        ov.kind = k.name();
                        ov.wearable = k == Kind.ARMOUR;
                        // Abilities that no longer fit go, since leaving a sword ability on a
                        // pickaxe would be a lie the tooltip keeps telling.
                        List<String> keep = abilitiesFor(k);
                        ov.abilities.removeIf(ab -> !keep.contains(ab));
                        ItemOverrideManager.commit(ov);
                        say(player, "Now a " + k.label.toLowerCase()
                                + ". It keeps its texture.");
                        open(player);
                    }).build());
        }

        gui.setSlot(SIZE - 5, GuiKit.info(Items.WRITABLE_BOOK, "What changing type does",
                ChatFormatting.WHITE,
                "It keeps the texture and the name and the",
                "stats. What changes is which abilities the",
                "editor will let you put on it, and whether",
                "armour may be worn.",
                "",
                "A chestplate made into a sword still looks",
                "like a chestplate and stops being wearable,",
                "which is exactly the point."));

        GuiKit.footer(gui, SIZE, player, () -> open(player));
        gui.open();
    }

    // ── Stats ─────────────────────────────────────────────────────────────

    private static void openStats(ServerPlayer player, String id, int page) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Stats").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        ItemOverrideManager.Override o = ItemOverrideManager.get(id);
        GuiKit.header(gui, Items.DIAMOND, "Stats", ChatFormatting.GREEN,
                "Applied to every copy",
                o == null ? "None set" : o.stats.size() + " set",
                "Editing " + id);

        List<GuiElement> entries = new ArrayList<>();
        for (PlayerStatsManager.Stat stat : PlayerStatsManager.Stat.values()) {
            double have = o == null ? 0 : o.stats.getOrDefault(stat.name(), 0.0);
            entries.add(new GuiElementBuilder(iconFor(stat))
                    .setName(Component.literal(stat.symbol + " " + stat.displayName)
                            .withStyle(stat.color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7Currently §f"
                            + (have == 0 ? "not set" : fmt(have) + (stat.percent ? "%" : ""))))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§eLeft click to set a number."))
                    .addLoreLine(Component.literal("§eRight click to clear it."))
                    .setCallback((s, t, a, sg) -> {
                        ItemOverrideManager.Override ov = ItemOverrideManager.getOrCreate(id);
                        if (t.isRight) {
                            ov.stats.remove(stat.name());
                            ItemOverrideManager.commit(ov);
                            openStats(player, id, page);
                            return;
                        }
                        GuiKit.prompt(player, stat.displayName + " value", raw -> {
                            try {
                                double v = Double.parseDouble(raw.trim());
                                ItemOverrideManager.Override live =
                                        ItemOverrideManager.getOrCreate(id);
                                live.stats.put(stat.name(), v);
                                ItemOverrideManager.commit(live);
                                say(player, stat.displayName + " set to " + fmt(v)
                                        + " on every copy.");
                            } catch (NumberFormatException ex) {
                                player.sendSystemMessage(Component.literal(
                                        "§cThat is not a number."));
                            }
                            openStats(player, id, page);
                        }, () -> open(player));
                    }).build());
        }

        GuiKit.paint(gui, entries, page, GuiKit.LIST_SLOTS.length);
        GuiKit.pager(gui, SIZE, page, entries.size(), GuiKit.LIST_SLOTS.length,
                p -> openStats(player, id, p));

        gui.setSlot(SIZE - 5, GuiKit.action(Items.BARRIER, "Clear every stat", ChatFormatting.RED,
                () -> {
                    ItemOverrideManager.Override ov = ItemOverrideManager.getOrCreate(id);
                    ov.stats.clear();
                    ItemOverrideManager.commit(ov);
                    openStats(player, id, page);
                },
                "Back to whatever it was built with."));

        GuiKit.footer(gui, SIZE, player, () -> open(player));
        gui.open();
    }

    private static Item iconFor(PlayerStatsManager.Stat s) {
        return switch (s) {
            case STRENGTH -> Items.IRON_SWORD;
            case DEFENSE -> Items.SHIELD;
            case HEALTH -> Items.GOLDEN_APPLE;
            case SPEED -> Items.SUGAR;
            case INTELLIGENCE -> Items.LAPIS_LAZULI;
            case CRIT_CHANCE -> Items.FLINT;
            case CRIT_DAMAGE -> Items.BLAZE_POWDER;
            case FEROCITY -> Items.PRISMARINE_SHARD;
            case MAGIC_FIND -> Items.NETHER_STAR;
        };
    }

    // ── Abilities ─────────────────────────────────────────────────────────

    /**
     * The abilities on offer for a type.
     *
     * <p>Type gated on purpose. A sword offers sword abilities, armour offers armour abilities, and
     * nothing offers both, because an ability that fires on a swing is meaningless welded to a pair
     * of boots.</p>
     */
    public static List<String> abilitiesFor(Kind kind) {
        List<String> out = new ArrayList<>();
        switch (kind) {
            case WEAPON -> {
                out.add("Cleaving arc on every third swing");
                out.add("Ignites what it hits");
                out.add("Drains a share of the damage as health");
                out.add("Chains lightning to nearby enemies");
                out.add("Throws the target back hard");
                out.add("Marks the target and hits marked targets harder");
                out.add("Doubles damage while under a third health");
                out.add("Freezes on hit");
                out.add("Withers on hit");
                out.add("Refunds a swing when it kills");
                out.add("Explodes on a critical hit");
                out.add("Rips a share of the target's armour off");
            }
            case ARMOUR -> {
                out.add("Absorbs the first hit of every fight");
                out.add("Returns a share of melee damage");
                out.add("Heals steadily out of combat");
                out.add("Blocks fall damage entirely");
                out.add("Lets you breathe under water");
                out.add("Sets attackers alight");
                out.add("Grants a burst of speed when hurt");
                out.add("Halves damage while standing still");
                out.add("Cleanses one harmful effect a minute");
                out.add("Drops a shockwave when you land");
                out.add("Feeds you slowly");
                out.add("Warns you when something rare is near");
            }
            case TOOL -> {
                out.add("Breaks a whole ore seam at once");
                out.add("Breaks a three by three");
                out.add("Smelts what it mines");
                out.add("Replants what it harvests");
                out.add("Fells a whole tree");
                out.add("Never wears out");
                out.add("Doubles what it drops sometimes");
                out.add("Pulls drops straight into your bag");
                out.add("Digs faster the longer you dig");
                out.add("Finds more from every ore");
            }
            case BOW -> {
                out.add("Fires three arrows in a spread");
                out.add("Arrows pass through everything");
                out.add("Arrows explode on impact");
                out.add("Arrows never drop");
                out.add("Arrows seek the nearest target");
                out.add("Costs no arrows at all");
                out.add("Damage rises with the draw");
                out.add("Arrows leave a poison cloud");
            }
            case ACCESSORY -> {
                out.add("Raises every stat a little");
                out.add("Raises coins from every source");
                out.add("Raises Magic Find");
                out.add("Shortens every cooldown");
                out.add("Grants a second chance once a day");
                out.add("Pulls drops from further away");
            }
            case ITEM -> {
                out.add("Consumed for a burst of health");
                out.add("Consumed for a burst of coins");
                out.add("Consumed for an hour of a buff");
                out.add("Right click to teleport home");
                out.add("Right click to open a menu");
            }
        }
        return out;
    }

    private static void openAbilities(ServerPlayer player, String id, Kind kind, int page) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Abilities")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        ItemOverrideManager.Override o = ItemOverrideManager.get(id);
        List<String> offer = abilitiesFor(kind);

        GuiKit.header(gui, Items.BLAZE_POWDER, "Abilities", ChatFormatting.GOLD,
                "Only what fits a " + kind.label.toLowerCase(),
                (o == null ? 0 : o.abilities.size()) + " on this item, "
                        + offer.size() + " on offer",
                "Add as many as you like");

        List<GuiElement> entries = new ArrayList<>();
        for (String ab : offer) {
            boolean on = o != null && o.abilities.contains(ab);
            GuiElementBuilder b = new GuiElementBuilder(on ? Items.BLAZE_POWDER : Items.GUNPOWDER)
                    .setName(Component.literal(ab)
                            .withStyle(on ? ChatFormatting.GOLD : ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7For §f" + kind.label.toLowerCase() + "s"))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal(on
                            ? "§aOn this item. Click to take it off."
                            : "§eClick to put it on."));
            if (on) b.glow();
            entries.add(b.setCallback((s, t, a, sg) -> {
                ItemOverrideManager.Override ov = ItemOverrideManager.getOrCreate(id);
                if (ov.abilities.contains(ab)) {
                    ov.abilities.remove(ab);
                } else {
                    ov.abilities.add(ab);
                }
                ItemOverrideManager.commit(ov);
                openAbilities(player, id, kind, page);
            }).build());
        }

        GuiKit.paint(gui, entries, page, GuiKit.LIST_SLOTS.length);
        GuiKit.pager(gui, SIZE, page, entries.size(), GuiKit.LIST_SLOTS.length,
                p -> openAbilities(player, id, kind, p));

        gui.setSlot(SIZE - 6, GuiKit.action(Items.WRITABLE_BOOK, "Write your own",
                ChatFormatting.AQUA,
                () -> GuiKit.prompt(player, "Ability text", raw -> {
                    if (!raw.isBlank()) {
                        ItemOverrideManager.Override ov = ItemOverrideManager.getOrCreate(id);
                        ov.abilities.add(raw.trim());
                        ItemOverrideManager.commit(ov);
                    }
                    openAbilities(player, id, kind, page);
                }, () -> open(player)),
                "Anything the list does not cover."));

        gui.setSlot(SIZE - 4, GuiKit.action(Items.BARRIER, "Take them all off",
                ChatFormatting.RED,
                () -> {
                    ItemOverrideManager.Override ov = ItemOverrideManager.getOrCreate(id);
                    ov.abilities.clear();
                    ItemOverrideManager.commit(ov);
                    openAbilities(player, id, kind, page);
                }));

        GuiKit.footer(gui, SIZE, player, () -> open(player));
        gui.open();
    }

    // ── Baked enchantments ────────────────────────────────────────────────

    private static void openEnchants(ServerPlayer player, String id, int page) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Baked enchantments")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        ItemOverrideManager.Override o = ItemOverrideManager.get(id);
        GuiKit.header(gui, Items.ENCHANTED_BOOK, "Baked enchantments",
                ChatFormatting.DARK_PURPLE,
                "On every copy, not bought at the bench",
                (o == null ? 0 : o.enchants.size()) + " baked in",
                CustomEnchant.values().length + " exist");

        List<GuiElement> entries = new ArrayList<>();
        for (CustomEnchant e : CustomEnchant.values()) {
            int have = o == null ? 0 : o.enchants.getOrDefault(e.key(), 0);
            GuiElementBuilder b = new GuiElementBuilder(e.icon())
                    .setName(Component.literal(e.display).withStyle(e.colour, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7" + e.effect))
                    .addLoreLine(Component.literal("§7Goes on §f" + e.slot.label))
                    .addLoreLine(Component.literal("§7Maximum §f" + CustomEnchant.roman(e.maxLevel)))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal(have > 0
                            ? "§aBaked at " + CustomEnchant.roman(have)
                            : "§8Not baked in"))
                    .addLoreLine(Component.literal("§eLeft click raises the level."))
                    .addLoreLine(Component.literal("§eRight click removes it."));
            if (have > 0) b.glow();
            entries.add(b.setCallback((s, t, a, sg) -> {
                ItemOverrideManager.Override ov = ItemOverrideManager.getOrCreate(id);
                int cur = ov.enchants.getOrDefault(e.key(), 0);
                if (t.isRight) {
                    ov.enchants.remove(e.key());
                } else {
                    ov.enchants.put(e.key(), cur >= e.maxLevel ? 1 : cur + 1);
                }
                ItemOverrideManager.commit(ov);
                openEnchants(player, id, page);
            }).build());
        }

        GuiKit.paint(gui, entries, page, GuiKit.LIST_SLOTS.length);
        GuiKit.pager(gui, SIZE, page, entries.size(), GuiKit.LIST_SLOTS.length,
                p -> openEnchants(player, id, p));
        GuiKit.footer(gui, SIZE, player, () -> open(player));
        gui.open();
    }

    // ── Extra lore ────────────────────────────────────────────────────────

    private static void openLore(ServerPlayer player, String id) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Lore").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        ItemOverrideManager.Override o = ItemOverrideManager.get(id);
        List<String> lines = o == null ? List.of() : o.lore;

        GuiKit.header(gui, Items.WRITABLE_BOOK, "Lore", ChatFormatting.GRAY,
                lines.size() + " lines",
                "Flavour text under the stats",
                "Editing " + id);

        List<GuiElement> entries = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            final int idx = i;
            entries.add(new GuiElementBuilder(Items.PAPER)
                    .setName(Component.literal("§7" + lines.get(i)))
                    .addLoreLine(Component.literal("§7Line " + (i + 1)))
                    .addLoreLine(Component.literal("§eRight click to delete this line."))
                    .setCallback((s, t, a, sg) -> {
                        if (!t.isRight) return;
                        ItemOverrideManager.Override ov = ItemOverrideManager.getOrCreate(id);
                        if (idx < ov.lore.size()) ov.lore.remove(idx);
                        ItemOverrideManager.commit(ov);
                        openLore(player, id);
                    }).build());
        }
        GuiKit.paint(gui, entries, 0, GuiKit.LIST_SLOTS.length);

        gui.setSlot(SIZE - 5, GuiKit.action(Items.WRITABLE_BOOK, "Add a line", ChatFormatting.AQUA,
                () -> GuiKit.prompt(player, "Lore line", raw -> {
                    if (!raw.isBlank()) {
                        ItemOverrideManager.Override ov = ItemOverrideManager.getOrCreate(id);
                        ov.lore.add(raw.trim());
                        ItemOverrideManager.commit(ov);
                    }
                    openLore(player, id);
                }, () -> open(player))));

        GuiKit.footer(gui, SIZE, player, () -> open(player));
        gui.open();
    }

    // ── Making one from nothing ───────────────────────────────────────────

    private static void openCreate(ServerPlayer player) {
        GuiKit.prompt(player, "New item id", raw -> {
            String id = slug(raw);
            if (id.isEmpty()) {
                player.sendSystemMessage(Component.literal("§cThat is not a usable id."));
                open(player);
                return;
            }
            if (ItemOverrideManager.has(id)) {
                player.sendSystemMessage(Component.literal(
                        "§cSomething is already called " + id + "."));
                open(player);
                return;
            }
            ItemStack made = new ItemStack(Items.PAPER);
            made.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,
                    Component.literal("§f" + pretty(id)).withStyle(s -> s.withItalic(false)));
            SlayerItems.setCustomItemId(made, id);

            ItemOverrideManager.Override o = ItemOverrideManager.getOrCreate(id);
            o.name = pretty(id);
            o.rarity = "COMMON";
            ItemOverrideManager.commit(o);

            if (!player.getInventory().add(made)) player.drop(made, false);
            say(player, "Made " + id + ". It is in your hand now, edit it from there.");
            open(player);
        }, () -> open(player));
    }

    // ── Ledger ────────────────────────────────────────────────────────────

    private static void openLedger(ServerPlayer player, int page) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Edited items")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);
        GuiKit.header(gui, Items.WRITABLE_BOOK, "Edited items", ChatFormatting.AQUA,
                ItemOverrideManager.count() + " ids carry an edit",
                "Every one is live on all copies",
                "Right click any of them to undo it");

        List<GuiElement> entries = new ArrayList<>();
        List<String> keys = new ArrayList<>(ItemOverrideManager.all().keySet());
        java.util.Collections.sort(keys);
        for (String k : keys) {
            ItemOverrideManager.Override o = ItemOverrideManager.get(k);
            if (o == null) continue;
            GuiElementBuilder b = new GuiElementBuilder(Items.PAPER)
                    .setName(Component.literal(o.name != null ? o.name : k)
                            .withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§8" + k))
                    .addLoreLine(Component.literal("§7Revision §f" + o.revision));
            if (o.rarity != null) {
                b.addLoreLine(Component.literal("§7Rarity §f" + o.rarity));
            }
            if (!o.stats.isEmpty()) {
                b.addLoreLine(Component.literal("§7Stats §f" + o.stats.size()));
            }
            if (!o.abilities.isEmpty()) {
                b.addLoreLine(Component.literal("§7Abilities §f" + o.abilities.size()));
            }
            b.addLoreLine(Component.literal(""));
            b.addLoreLine(Component.literal("§eRight click to undo every edit."));
            entries.add(b.setCallback((s, t, a, sg) -> {
                if (!t.isRight) return;
                GuiKit.confirm(player, "Undo every edit to " + k,
                        "Every copy goes back to how it was built.",
                        () -> {
                            ItemOverrideManager.clear(k);
                            openLedger(player, page);
                        },
                        () -> openLedger(player, page));
            }).build());
        }

        GuiKit.paint(gui, entries, page, GuiKit.LIST_SLOTS.length);
        GuiKit.pager(gui, SIZE, page, entries.size(), GuiKit.LIST_SLOTS.length,
                p -> openLedger(player, p));
        GuiKit.footer(gui, SIZE, player, () -> open(player));
        gui.open();
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private static void say(ServerPlayer player, String msg) {
        player.sendSystemMessage(Component.literal("§6✎ §7" + msg));
    }

    private static String slug(String raw) {
        return raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9_ ]", "").replace(' ', '_');
    }

    private static String pretty(String id) {
        String[] parts = id.split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
        }
        return sb.toString();
    }

    private static String prettify(String name) {
        return pretty(name.toLowerCase(Locale.ROOT));
    }

    private static String fmt(double v) {
        return v == Math.floor(v) ? String.valueOf((long) v) : String.valueOf(v);
    }
}
