package com.political;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Admin edits to items, and the machinery that pushes them onto copies already in the world.
 *
 * <p>This is the part of the item editor that is actually hard. Renaming an item in a menu is easy;
 * making the four hundred already sitting in players' enderchests match is not, because there is no
 * list of them and no way to walk an unloaded chunk to find one.</p>
 *
 * <p>So the edit is stored against the item's id rather than written into every copy, and each stack
 * carries a revision number. When a stack is next handled, if its revision is behind the stored one,
 * it is rebuilt on the spot. An item in a chest in an unloaded chunk updates the moment somebody
 * picks it up, which is the only point at which it could ever matter.</p>
 *
 * <p>Edits are keyed by custom item id, so one change reaches every copy of that item everywhere,
 * which is exactly what was asked for. Nothing here creates or destroys a stack, so there is no path
 * through this class that duplicates anything.</p>
 */
public final class ItemOverrideManager {

    private ItemOverrideManager() {}

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FabricLoader.getInstance().isDevelopmentEnvironment()
            ? FabricLoader.getInstance().getGameDir().getParent().resolve("config").resolve("political")
            : FabricLoader.getInstance().getGameDir().resolve("config").resolve("political");
    private static final Path FILE = CONFIG_DIR.resolve("item_overrides.json");

    /** The NBT key holding which revision of the override a stack was last built against. */
    private static final String REV_KEY = "political_item_rev";

    private static final Map<String, Override> OVERRIDES = new ConcurrentHashMap<>();
    private static boolean dirty = false;

    // ── The edit itself ───────────────────────────────────────────────────

    /**
     * One item's worth of admin edits.
     *
     * <p>Every field is nullable and null means "leave whatever the item already had". That matters:
     * an admin who only wanted to change the name should not silently wipe the stats, and an override
     * that stored a full copy of the item would do exactly that the first time somebody used it.</p>
     */
    public static final class Override {
        public String id;
        public int revision = 1;

        public String name;            // Display name, null to leave alone.
        public String colour;          // A §-code letter or a hex string like "#ffaa00".
        public String texture;         // Custom model id, for a resource pack.
        public String rarity;          // COMMON through PRIMAL, free text so packs can add their own.
        public String baseItem;        // Registry path of the item to render as, eg "diamond_sword".
        public String kind;            // WEAPON, ARMOUR, TOOL, BOW or ITEM. Drives what may be applied.
        public Boolean wearable;       // Explicit override for whether armour may be worn.
        public Boolean glint;

        public final Map<String, Double> stats = new LinkedHashMap<>();
        public final List<String> abilities = new ArrayList<>();
        public final List<String> lore = new ArrayList<>();
        public final Map<String, Integer> enchants = new LinkedHashMap<>();

        public Override(String id) {
            this.id = id;
        }

        public boolean isEmpty() {
            return name == null && colour == null && texture == null && rarity == null
                    && baseItem == null && kind == null && wearable == null && glint == null
                    && stats.isEmpty() && abilities.isEmpty() && lore.isEmpty() && enchants.isEmpty();
        }

        /** Bumps the revision, which is what makes existing copies rebuild themselves. */
        public void touch() {
            revision++;
        }
    }

    // ── Lookup and edit ───────────────────────────────────────────────────

    public static Override get(String id) {
        return id == null ? null : OVERRIDES.get(id);
    }

    /** The edit for this id, creating an empty one if there is none yet. */
    public static Override getOrCreate(String id) {
        return OVERRIDES.computeIfAbsent(id, Override::new);
    }

    public static boolean has(String id) {
        return id != null && OVERRIDES.containsKey(id);
    }

    public static Map<String, Override> all() {
        return OVERRIDES;
    }

    public static int count() {
        return OVERRIDES.size();
    }

    /**
     * Records that an edit was made and schedules a save.
     *
     * <p>Called by every setter in the editor rather than left to the caller to remember, because a
     * change that does not bump the revision is a change that never reaches anybody.</p>
     */
    public static void commit(Override o) {
        if (o == null) return;
        snapshot(o);
        if (o.isEmpty()) {
            OVERRIDES.remove(o.id);
        } else {
            o.touch();
            OVERRIDES.put(o.id, o);
        }
        dirty = true;
        save();
    }

    /** Drops every edit for an id, so existing copies fall back to how they were built. */
    public static void clear(String id) {
        if (OVERRIDES.remove(id) != null) {
            dirty = true;
            save();
        }
    }


    // ── History ───────────────────────────────────────────────────────────

    /**
     * Past versions of each edit, so a mistake is one click from being undone.
     *
     * <p>Bounded, because an admin who spends an afternoon tuning a sword should not slowly turn
     * the server memory into a changelog. Twenty deep is more than anybody backs out through.</p>
     */
    private static final int HISTORY_DEPTH = 20;
    private static final Map<String, List<Override>> HISTORY = new ConcurrentHashMap<>();

    private static Override copyOf(Override o) {
        Override c = new Override(o.id);
        c.revision = o.revision;
        c.name = o.name;
        c.colour = o.colour;
        c.texture = o.texture;
        c.rarity = o.rarity;
        c.baseItem = o.baseItem;
        c.kind = o.kind;
        c.wearable = o.wearable;
        c.glint = o.glint;
        c.stats.putAll(o.stats);
        c.abilities.addAll(o.abilities);
        c.lore.addAll(o.lore);
        c.enchants.putAll(o.enchants);
        return c;
    }

    /** Takes a snapshot before an edit lands. Called by commit, so nothing has to remember to. */
    private static void snapshot(Override o) {
        List<Override> past = HISTORY.computeIfAbsent(o.id, k -> new ArrayList<>());
        past.add(copyOf(o));
        while (past.size() > HISTORY_DEPTH) past.remove(0);
    }

    public static int historyDepth(String id) {
        List<Override> past = HISTORY.get(id);
        return past == null ? 0 : past.size();
    }

    /**
     * Steps one edit back.
     *
     * <p>The revision still counts forward, never back. Rolling the number backwards would leave
     * every copy in the world stamped ahead of the stored edit and therefore convinced it was
     * already up to date, which would make an undo look like it did nothing at all.</p>
     */
    public static boolean undo(String id) {
        List<Override> past = HISTORY.get(id);
        if (past == null || past.isEmpty()) return false;
        Override prior = past.remove(past.size() - 1);
        Override live = OVERRIDES.get(id);
        prior.revision = (live == null ? prior.revision : live.revision) + 1;
        OVERRIDES.put(id, prior);
        dirty = true;
        save();
        return true;
    }

    // ── Copying between items ─────────────────────────────────────────────

    /**
     * Copies one item whole configuration onto another.
     *
     * <p>The id and the base item are deliberately left out. Copying the id would overwrite the
     * target with the source, and copying the base would turn a sword into a chestplate the moment
     * somebody wanted to share a stat line.</p>
     */
    public static boolean copyConfig(String fromId, String toId) {
        Override src = OVERRIDES.get(fromId);
        if (src == null || fromId.equals(toId)) return false;
        Override dst = getOrCreate(toId);
        dst.name = src.name;
        dst.colour = src.colour;
        dst.texture = src.texture;
        dst.rarity = src.rarity;
        dst.kind = src.kind;
        dst.wearable = src.wearable;
        dst.glint = src.glint;
        dst.stats.clear();
        dst.stats.putAll(src.stats);
        dst.abilities.clear();
        dst.abilities.addAll(src.abilities);
        dst.lore.clear();
        dst.lore.addAll(src.lore);
        dst.enchants.clear();
        dst.enchants.putAll(src.enchants);
        commit(dst);
        return true;
    }

    /** Makes a new item that duplicates an existing one under a new id. */
    public static boolean duplicate(String fromId, String newId) {
        if (OVERRIDES.containsKey(newId)) return false;
        Override src = OVERRIDES.get(fromId);
        if (src == null) return false;
        Override dst = getOrCreate(newId);
        dst.baseItem = src.baseItem != null ? src.baseItem : "paper";
        commit(dst);
        return copyConfig(fromId, newId);
    }

    // ── Bulk ──────────────────────────────────────────────────────────────

    /**
     * Applies an edit to every item whose id matches a pattern.
     *
     * <p>This is the one that matters at this scale. There are ten thousand items, and retuning a
     * whole gear line by hand is not a task anybody finishes. It is the reason people edit code
     * instead of using an in game editor.</p>
     *
     * <p>Matching is a plain substring rather than a regular expression, because the person using it
     * is standing in a Minecraft world typing into a sign.</p>
     */
    public static int bulkEdit(String pattern, java.util.function.Consumer<Override> edit) {
        if (pattern == null || pattern.isBlank()) return 0;
        int n = 0;
        beginBatch();
        try {
            for (String id : matching(pattern)) {
                Override o = getOrCreate(id);
                edit.accept(o);
                commit(o);
                n++;
            }
        } finally {
            // In a finally block so a throw part way through still leaves the file written and
            // the flag cleared, rather than silently disabling every save from then on.
            endBatch();
        }
        return n;
    }

    /** Every known id matching a pattern, whether it carries an edit yet or not. */
    public static List<String> matching(String pattern) {
        String p = pattern == null ? "" : pattern.trim().toLowerCase(java.util.Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (String id : knownIds()) {
            if (p.isEmpty() || id.contains(p)) out.add(id);
        }
        return out;
    }

    /**
     * Every custom item id the server knows about.
     *
     * <p>Harvested from the recipe catalogue rather than kept as a second list, so it is correct by
     * construction and covers all ten thousand items without anybody maintaining a registry. Cached
     * against the recipe count, because parsing ten thousand result stacks per menu open would be
     * felt immediately.</p>
     */
    public static List<String> knownIds() {
        int now = SlayerRecipes.RECIPES.size();
        if (knownCache != null && knownCacheSize == now) return knownCache;

        java.util.Set<String> ids = new java.util.TreeSet<>(OVERRIDES.keySet());
        for (SlayerRecipes.Recipe r : SlayerRecipes.RECIPES) {
            String id = SlayerItems.getCustomItemId(r.result);
            if (id != null && !id.isBlank()) ids.add(id);
        }
        knownCache = new ArrayList<>(ids);
        knownCacheSize = now;
        return knownCache;
    }

    private static List<String> knownCache;
    private static int knownCacheSize = -1;

    /** Drops the cache, for when recipes were reloaded underneath us. */
    public static void invalidateKnown() {
        knownCache = null;
        knownCacheSize = -1;
    }

    // ── Sharing ───────────────────────────────────────────────────────────

    /**
     * An item configuration as a single line of text.
     *
     * <p>Base64 of the JSON, so it survives a chat box, a Discord message and a sign without
     * anything mangling it. This is how one server owner hands a tuned item to another.</p>
     */
    public static String export(String id) {
        Override o = OVERRIDES.get(id);
        if (o == null) return null;
        String json = GSON.toJson(toJson(o));
        return java.util.Base64.getEncoder()
                .encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    /** Reads an exported configuration onto an id. Returns a reason on failure, null on success. */
    public static String importInto(String id, String code) {
        if (code == null || code.isBlank()) return "There was nothing to read.";
        try {
            String json = new String(java.util.Base64.getDecoder().decode(code.trim()),
                    StandardCharsets.UTF_8);
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            Override incoming = fromJson(id, obj);
            // A revision from another server means nothing here, so it is not carried over.
            Override live = OVERRIDES.get(id);
            incoming.revision = live == null ? 1 : live.revision;
            OVERRIDES.put(id, incoming);
            commit(incoming);
            return null;
        } catch (IllegalArgumentException e) {
            return "That is not a valid item code.";
        } catch (Exception e) {
            return "That code could not be read: " + e.getMessage();
        }
    }

    // ── Pushing edits onto stacks ─────────────────────────────────────────

    /**
     * Brings one stack up to date, if it is behind.
     *
     * <p>Cheap on the common path. A stack with no custom id, or no override for its id, or a
     * revision that already matches, costs one NBT read and returns.</p>
     */
    public static boolean apply(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        String id = SlayerItems.getCustomItemId(stack);
        if (id == null) return false;
        Override o = OVERRIDES.get(id);

        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        int stamped = data == null ? 0 : data.copyTag().getIntOr(REV_KEY, 0);

        if (o == null) {
            // Nothing stored. Only clear the stamp so a later re-add rebuilds properly.
            if (stamped != 0) stamp(stack, 0);
            return false;
        }
        if (stamped == o.revision) return false;

        rebuild(stack, o);
        stamp(stack, o.revision);
        return true;
    }

    private static void stamp(ItemStack stack, int rev) {
        CustomData existing = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag nbt = existing != null ? existing.copyTag() : new CompoundTag();
        nbt.putInt(REV_KEY, rev);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
    }

    /**
     * Writes the stored edits onto a stack.
     *
     * <p>Additive by design. Anything the override does not mention is left exactly as the item was
     * built, so an admin can change a name today and a stat next week without the first change
     * reverting.</p>
     */
    private static void rebuild(ItemStack stack, Override o) {
        if (o.name != null) {
            String prefix = colourCode(o.colour);
            stack.set(DataComponents.CUSTOM_NAME,
                    Component.literal(prefix + o.name).withStyle(s -> s.withItalic(false)));
        } else if (o.colour != null) {
            // A recolour with no rename keeps whatever the item was already called.
            String current = stack.getHoverName().getString().replaceAll("§.", "");
            stack.set(DataComponents.CUSTOM_NAME,
                    Component.literal(colourCode(o.colour) + current)
                            .withStyle(s -> s.withItalic(false)));
        }

        if (o.glint != null) {
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, o.glint);
        }

        if (o.texture != null) {
            PolymerBridge.applyModel(stack, o.texture);
        }

        // Stats, abilities, rarity and extra lore all live in the tooltip, so they are written as
        // one block that replaces any previous block from this manager.
        List<Component> lines = new ArrayList<>();
        ItemLore existing = stack.get(DataComponents.LORE);
        if (existing != null) {
            for (Component line : existing.lines()) {
                String plain = line.getString();
                if (plain.contains(MARK)) break;
                // An enchantment this override bakes in is dropped from the kept lines, otherwise
                // an item that was also enchanted at a table shows the same name twice.
                if (!o.enchants.isEmpty() && bakedHere(o, plain)) continue;
                lines.add(line);
            }
        }

        boolean anyBlock = !o.stats.isEmpty() || !o.abilities.isEmpty()
                || !o.lore.isEmpty() || o.rarity != null || !o.enchants.isEmpty();
        if (anyBlock) {
            lines.add(Component.literal(MARK));
            for (Map.Entry<String, Double> st : o.stats.entrySet()) {
                PlayerStatsManager.Stat stat = statByName(st.getKey());
                if (stat == null) continue;
                double v = st.getValue();
                String sign = v >= 0 ? "+" : "";
                lines.add(Component.literal("§7" + stat.symbol + " " + stat.displayName
                        + ": §c" + sign + trim(v) + (stat.percent ? "%" : "")));
            }
            if (!o.abilities.isEmpty()) {
                lines.add(Component.literal(""));
                for (String ab : o.abilities) {
                    lines.add(Component.literal("§6Ability: §e" + ab));
                }
            }
            for (Map.Entry<String, Integer> en : o.enchants.entrySet()) {
                CustomEnchant ce = CustomEnchant.byKey(en.getKey());
                if (ce != null) lines.add(Component.literal(ce.loreLine(en.getValue())));
            }
            if (!o.lore.isEmpty()) {
                lines.add(Component.literal(""));
                for (String l : o.lore) lines.add(Component.literal("§7" + l));
            }
            if (o.rarity != null) {
                lines.add(Component.literal(""));
                lines.add(Component.literal(rarityColour(o.rarity) + "§l"
                        + o.rarity.toUpperCase(java.util.Locale.ROOT)));
            }
        }
        stack.set(DataComponents.LORE, new ItemLore(lines));
    }

    /** Whether a tooltip line names an enchantment this override is about to write itself. */
    private static boolean bakedHere(Override o, String line) {
        String plain = line.replaceAll("§.", "").trim();
        int lastSpace = plain.lastIndexOf(' ');
        if (lastSpace <= 0) return false;
        CustomEnchant e = CustomEnchant.byDisplay(plain.substring(0, lastSpace).trim());
        return e != null && o.enchants.containsKey(e.key());
    }

    /**
     * The line that marks where this manager's block starts.
     *
     * <p>Rendered as an empty string in game, since it is only a section code and a zero width
     * separator. It exists so a rebuild can replace its own previous output without eating lore the
     * item was built with.</p>
     */
    private static final String MARK = "§r§8§k§r";

    private static String trim(double v) {
        return v == Math.floor(v) ? String.valueOf((long) v) : String.valueOf(v);
    }

    private static PlayerStatsManager.Stat statByName(String name) {
        for (PlayerStatsManager.Stat s : PlayerStatsManager.Stat.values()) {
            if (s.name().equalsIgnoreCase(name) || s.displayName.equalsIgnoreCase(name)) return s;
        }
        return null;
    }

    /** Turns a stored colour into a prefix, accepting either a code letter or a hex string. */
    public static String colourCode(String colour) {
        if (colour == null || colour.isEmpty()) return "";
        if (colour.startsWith("#")) return "§x" + hexToCodes(colour);
        String c = colour.replace("§", "").trim();
        if (c.length() == 1) return "§" + c;
        // A named colour, matched against the vanilla set.
        try {
            net.minecraft.ChatFormatting f = net.minecraft.ChatFormatting.getByName(c);
            if (f != null) return "§" + f.getChar();
        } catch (Exception ignored) {
            // Falls through to no colour, which is better than a broken prefix.
        }
        return "";
    }

    /** §x§r§r§g§g§b§b, which is how a hex colour is spelled in a legacy formatted string. */
    private static String hexToCodes(String hex) {
        String h = hex.substring(1);
        if (h.length() != 6) return "";
        StringBuilder sb = new StringBuilder();
        for (char ch : h.toCharArray()) sb.append('§').append(ch);
        return sb.toString();
    }

    public static String rarityColour(String rarity) {
        return switch (rarity.toUpperCase(java.util.Locale.ROOT)) {
            case "COMMON" -> "§f";
            case "UNCOMMON" -> "§a";
            case "RARE" -> "§9";
            case "EPIC" -> "§5";
            case "LEGENDARY" -> "§6";
            case "MYTHIC" -> "§d";
            case "DIVINE" -> "§b";
            case "PRIMAL" -> "§4";
            case "SPECIAL" -> "§c";
            default -> "§7";
        };
    }

    /**
     * Builds a fresh copy of an item from its stored edits alone.
     *
     * <p>This is what makes an admin created item a real item rather than the one copy that happened
     * to be in somebody's hand at the time. It only works for items whose override records a base,
     * which is every item the editor minted.</p>
     */
    public static ItemStack build(String id) {
        Override o = OVERRIDES.get(id);
        if (o == null) return ItemStack.EMPTY;

        net.minecraft.world.item.Item base = net.minecraft.world.item.Items.PAPER;
        if (o.baseItem != null) {
            var found = net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(
                    net.minecraft.resources.Identifier.fromNamespaceAndPath("minecraft", o.baseItem));
            if (found.isPresent()) base = found.get();
        }

        ItemStack stack = new ItemStack(base);
        SlayerItems.setCustomItemId(stack, id);
        rebuild(stack, o);
        stamp(stack, o.revision);
        return stack;
    }

    /** Every id that can be rebuilt from its override, sorted. */
    public static List<String> buildableIds() {
        List<String> out = new ArrayList<>();
        for (Map.Entry<String, Override> e : OVERRIDES.entrySet()) {
            if (e.getValue().baseItem != null) out.add(e.getKey());
        }
        java.util.Collections.sort(out);
        return out;
    }

    // ── The sweep ─────────────────────────────────────────────────────────

    /**
     * Walks online players' inventories and brings anything stale up to date.
     *
     * <p>Runs on a slow tick because it is a full inventory scan per player, and because an item that
     * updates within a couple of seconds of an admin pressing the button is indistinguishable from
     * one that updates instantly.</p>
     */
    public static void tick() {
        if (OVERRIDES.isEmpty()) return;
        if (PoliticalServer.server == null) return;
        if (PoliticalServer.server.getTickCount() % 40 != 0) return;

        for (ServerPlayer player : PoliticalServer.server.getPlayerList().getPlayers()) {
            boolean changed = false;
            for (ItemStack st : player.getInventory().getNonEquipmentItems()) {
                if (apply(st)) changed = true;
            }
            for (net.minecraft.world.entity.EquipmentSlot slot
                    : net.minecraft.world.entity.EquipmentSlot.values()) {
                try {
                    if (apply(player.getItemBySlot(slot))) changed = true;
                } catch (Exception ignored) {
                    // A slot the entity does not have. Nothing to update.
                }
            }
            if (changed) player.containerMenu.broadcastChanges();
        }
    }

    // ── Persistence ───────────────────────────────────────────────────────

    public static void load() {
        OVERRIDES.clear();
        if (!Files.exists(FILE)) return;
        try (Reader r = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(r);
            if (!root.isJsonObject()) return;
            JsonObject obj = root.getAsJsonObject();
            for (Map.Entry<String, JsonElement> e : obj.entrySet()) {
                if (!e.getValue().isJsonObject()) continue;
                OVERRIDES.put(e.getKey(), fromJson(e.getKey(), e.getValue().getAsJsonObject()));
            }
            PoliticalServer.LOGGER.info("Item overrides loaded: " + OVERRIDES.size());
        } catch (Exception ex) {
            PoliticalServer.LOGGER.error("Could not read item_overrides.json: " + ex.getMessage());
        }
    }

    /**
     * Set while a batch is running, so a bulk change writes the file once rather than once per item.
     *
     * <p>Without this, editing a 630 piece gear line serialised and wrote the whole override file
     * 630 times. The edit was correct and the server stopped responding while it happened.</p>
     */
    private static boolean batching = false;

    /** Starts a batch. Every save is deferred until the matching endBatch. */
    public static void beginBatch() {
        batching = true;
    }

    /** Ends a batch and writes once, whatever happened in between. */
    public static void endBatch() {
        batching = false;
        save();
    }

    public static void save() {
        if (batching) return;
        if (!dirty) return;
        try {
            Files.createDirectories(CONFIG_DIR);
            JsonObject root = new JsonObject();
            // Sorted, so the file does not churn between saves that changed nothing.
            List<String> keys = new ArrayList<>(OVERRIDES.keySet());
            java.util.Collections.sort(keys);
            for (String k : keys) root.add(k, toJson(OVERRIDES.get(k)));
            try (Writer w = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8)) {
                GSON.toJson(root, w);
            }
            dirty = false;
        } catch (IOException ex) {
            PoliticalServer.LOGGER.error("Could not write item_overrides.json: " + ex.getMessage());
        }
    }

    private static JsonObject toJson(Override o) {
        JsonObject j = new JsonObject();
        j.addProperty("revision", o.revision);
        if (o.name != null) j.addProperty("name", o.name);
        if (o.colour != null) j.addProperty("colour", o.colour);
        if (o.texture != null) j.addProperty("texture", o.texture);
        if (o.rarity != null) j.addProperty("rarity", o.rarity);
        if (o.baseItem != null) j.addProperty("baseItem", o.baseItem);
        if (o.kind != null) j.addProperty("kind", o.kind);
        if (o.wearable != null) j.addProperty("wearable", o.wearable);
        if (o.glint != null) j.addProperty("glint", o.glint);
        if (!o.stats.isEmpty()) {
            JsonObject st = new JsonObject();
            o.stats.forEach(st::addProperty);
            j.add("stats", st);
        }
        if (!o.enchants.isEmpty()) {
            JsonObject en = new JsonObject();
            o.enchants.forEach(en::addProperty);
            j.add("enchants", en);
        }
        if (!o.abilities.isEmpty()) {
            JsonArray a = new JsonArray();
            o.abilities.forEach(a::add);
            j.add("abilities", a);
        }
        if (!o.lore.isEmpty()) {
            JsonArray a = new JsonArray();
            o.lore.forEach(a::add);
            j.add("lore", a);
        }
        return j;
    }

    private static Override fromJson(String id, JsonObject j) {
        Override o = new Override(id);
        if (j.has("revision")) o.revision = j.get("revision").getAsInt();
        if (j.has("name")) o.name = j.get("name").getAsString();
        if (j.has("colour")) o.colour = j.get("colour").getAsString();
        if (j.has("texture")) o.texture = j.get("texture").getAsString();
        if (j.has("rarity")) o.rarity = j.get("rarity").getAsString();
        if (j.has("baseItem")) o.baseItem = j.get("baseItem").getAsString();
        if (j.has("kind")) o.kind = j.get("kind").getAsString();
        if (j.has("wearable")) o.wearable = j.get("wearable").getAsBoolean();
        if (j.has("glint")) o.glint = j.get("glint").getAsBoolean();
        if (j.has("stats")) {
            for (Map.Entry<String, JsonElement> e : j.getAsJsonObject("stats").entrySet()) {
                o.stats.put(e.getKey(), e.getValue().getAsDouble());
            }
        }
        if (j.has("enchants")) {
            for (Map.Entry<String, JsonElement> e : j.getAsJsonObject("enchants").entrySet()) {
                o.enchants.put(e.getKey(), e.getValue().getAsInt());
            }
        }
        if (j.has("abilities")) {
            for (JsonElement e : j.getAsJsonArray("abilities")) o.abilities.add(e.getAsString());
        }
        if (j.has("lore")) {
            for (JsonElement e : j.getAsJsonArray("lore")) o.lore.add(e.getAsString());
        }
        return o;
    }
}
