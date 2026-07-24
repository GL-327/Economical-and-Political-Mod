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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Applies and removes {@link Reforge}s.
 *
 * <p>The reforge lives in the item's own data, so it travels with the item and survives restarts
 * with no save file of its own. Applying a reforge does three things: it records the id, it renames
 * the item with the reforge prefix, and it appends a stat block that the stats engine reads back
 * out of the lore.</p>
 *
 * <p>The block is delimited by a marker line, which is what makes reforging repeatable. Applying a
 * new reforge strips everything from the marker down and writes a fresh block, so stats can never
 * stack up from repeated rerolls.</p>
 */
public final class ReforgeManager {

    private ReforgeManager() {}

    private static final String KEY_REFORGE = "political_reforge";
    private static final String KEY_BASE_NAME = "political_reforge_base_name";
    private static final String KEY_COUNT = "political_reforge_count";

    /** Marks the start of the reforge block in an item's lore. */
    private static final String MARKER = "§8§m                    §r§8 REFORGE §8§m                    §r";

    private static final long BASE_COST = 5_000L;
    private static final long COST_STEP = 2_500L;
    private static final long MAX_COST = 100_000L;

    // ── Reading ─────────────────────────────────────────────────────────────

    private static CompoundTag tagOf(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null ? data.copyTag() : new CompoundTag();
    }

    private static void writeTag(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /** The reforge currently on this item, or null. */
    public static Reforge get(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        return Reforge.byName(tagOf(stack).getStringOr(KEY_REFORGE, ""));
    }

    /** How many times this item has been reforged, used to escalate the cost. */
    public static int rerollCount(ItemStack stack) {
        try {
            return Integer.parseInt(tagOf(stack).getStringOr(KEY_COUNT, "0"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** The coin cost of the next reroll on this item. */
    public static long costFor(ItemStack stack) {
        return Math.min(MAX_COST, BASE_COST + COST_STEP * rerollCount(stack));
    }

    /** Only items that belong to this mod can be reforged. */
    public static boolean canReforge(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        String id = SlayerItems.getCustomItemId(stack);
        return id != null && !id.isBlank();
    }

    /** Works out which reforge pool an item draws from, by looking at what it is. */
    public static Reforge.Kind gearKindOf(ItemStack stack) {
        String id = SlayerItems.getCustomItemId(stack);
        String lid = id == null ? "" : SlayerItems.stripSkyblockEnchantSuffix(id).toLowerCase(Locale.ROOT);
        if (lid.contains("sword") || lid.contains("blade") || lid.contains("katana")
                || lid.contains("scythe") || lid.contains("dagger") || lid.contains("bow")
                || lid.contains("gauntlet") || lid.contains("staff") || lid.contains("wand")) {
            return Reforge.Kind.WEAPON;
        }
        if (lid.contains("helmet") || lid.contains("chestplate") || lid.contains("leggings")
                || lid.contains("boots") || lid.contains("hood") || lid.contains("robe")
                || lid.contains("crown") || lid.contains("plate") || lid.contains("mantle")) {
            return Reforge.Kind.ARMOR;
        }
        if (lid.contains("pickaxe") || lid.contains("axe") || lid.contains("shovel")
                || lid.contains("hoe") || lid.contains("rod")) {
            return Reforge.Kind.TOOL;
        }
        return Reforge.Kind.GENERIC;
    }

    /** Every reforge that could roll onto this item. */
    public static List<Reforge> poolFor(ItemStack stack) {
        Reforge.Kind gear = gearKindOf(stack);
        List<Reforge> pool = new ArrayList<>();
        for (Reforge r : Reforge.values()) {
            if (r.fits(gear)) pool.add(r);
        }
        return pool;
    }

    // ── Applying ────────────────────────────────────────────────────────────

    /** Rolls a weighted random reforge from the item's pool. */
    public static Reforge roll(ItemStack stack, java.util.Random rng) {
        List<Reforge> pool = poolFor(stack);
        int total = 0;
        for (Reforge r : pool) total += Math.max(1, r.weight);
        if (total <= 0 || pool.isEmpty()) return null;
        int pick = rng.nextInt(total);
        int cum = 0;
        for (Reforge r : pool) {
            cum += Math.max(1, r.weight);
            if (pick < cum) return r;
        }
        return pool.get(pool.size() - 1);
    }

    /**
     * Writes a reforge onto an item, replacing any previous one. Safe to call repeatedly.
     */
    public static void apply(ItemStack stack, Reforge reforge) {
        if (stack == null || stack.isEmpty() || reforge == null) return;

        CompoundTag tag = tagOf(stack);

        // Remember the untouched name once, so prefixes never pile up.
        String baseName = tag.getStringOr(KEY_BASE_NAME, "");
        if (baseName.isEmpty()) {
            Component current = stack.get(DataComponents.CUSTOM_NAME);
            baseName = current != null ? current.getString() : "";
            if (!baseName.isEmpty()) tag.putString(KEY_BASE_NAME, baseName);
        }

        tag.putString(KEY_REFORGE, reforge.name());
        tag.putString(KEY_COUNT, String.valueOf(rerollCount(stack) + 1));
        writeTag(stack, tag);

        // Rename with the prefix, keeping the original colour of the item name.
        if (!baseName.isEmpty()) {
            stack.set(DataComponents.CUSTOM_NAME,
                    Component.literal(reforge.display + " " + baseName).withStyle(reforge.colour));
        }

        // Rebuild lore: everything before the marker, then a fresh reforge block.
        List<Component> lore = new ArrayList<>(stripBlock(stack));
        lore.add(Component.literal(MARKER));
        lore.add(Component.literal("§8Reforge: " + reforge.colour + reforge.display));
        for (String line : reforge.statLines()) {
            lore.add(Component.literal(line));
        }
        stack.set(DataComponents.LORE, new ItemLore(lore));
    }

    /** Strips the reforge block and prefix, returning the item to its base state. */
    public static void remove(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        CompoundTag tag = tagOf(stack);
        String baseName = tag.getStringOr(KEY_BASE_NAME, "");
        tag.remove(KEY_REFORGE);
        writeTag(stack, tag);

        if (!baseName.isEmpty()) {
            stack.set(DataComponents.CUSTOM_NAME, Component.literal(baseName));
        }
        stack.set(DataComponents.LORE, new ItemLore(new ArrayList<>(stripBlock(stack))));
    }

    /** The item's lore with any existing reforge block cut off. */
    private static List<Component> stripBlock(ItemStack stack) {
        ItemLore lore = stack.get(DataComponents.LORE);
        List<Component> out = new ArrayList<>();
        if (lore == null) return out;
        for (Component c : lore.lines()) {
            if (c.getString().contains("REFORGE")) break;
            out.add(c);
        }
        return out;
    }

    // ── Player facing actions ───────────────────────────────────────────────

    /** Charges the player and rerolls the held item. Returns the new reforge, or null on failure. */
    public static Reforge reroll(ServerPlayer player, ItemStack stack) {
        if (!canReforge(stack)) {
            player.sendSystemMessage(Component.literal("§cThat item cannot be reforged.")
                    .withStyle(ChatFormatting.RED));
            return null;
        }
        long cost = costFor(stack);
        if (CoinManager.getCoinsLong(player) < cost) {
            player.sendSystemMessage(Component.literal("§cYou need §6" + cost + " §ccoins to reforge this.")
                    .withStyle(ChatFormatting.RED));
            return null;
        }
        Reforge rolled = roll(stack, new java.util.Random());
        if (rolled == null) {
            player.sendSystemMessage(Component.literal("§cNothing wants to bind to that item.")
                    .withStyle(ChatFormatting.RED));
            return null;
        }
        CoinManager.removeCoins(player, cost);
        apply(stack, rolled);

        player.sendSystemMessage(Component.literal("§7Reforged into ")
                .append(Component.literal(rolled.display).withStyle(rolled.colour, ChatFormatting.BOLD))
                .append(Component.literal(" §8(-" + cost + " coins)")));
        if (player.level() instanceof ServerLevel level) {
            Vfx.bloom(level, player.position().add(0, 1, 0));
        }
        return rolled;
    }

    // ── The reforge stone, so this is reachable without an admin ────────────

    public static ItemStack createReforgeStone() {
        ItemStack s = new ItemStack(Items.AMETHYST_SHARD);
        s.set(DataComponents.CUSTOM_NAME,
                Component.literal("✦ Reforge Stone").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§7Opens the reforge anvil."));
        lore.add(Component.literal("§8Right click to open, or use /reforge."));
        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, "reforge_stone");
        return s;
    }

    public static void ensureRecipes() {
        List<SlayerRecipes.Recipe> out = new ArrayList<>();
        ItemStack[] grid = new ItemStack[9];
        for (int i = 0; i < 9; i++) grid[i] = ItemStack.EMPTY;
        grid[1] = new ItemStack(Items.AMETHYST_SHARD, 16);
        grid[3] = new ItemStack(Items.GOLD_INGOT, 32);
        grid[4] = new ItemStack(Items.ANVIL);
        grid[5] = new ItemStack(Items.GOLD_INGOT, 32);
        grid[7] = new ItemStack(Items.LAPIS_LAZULI, 32);
        boolean[] locked = new boolean[9];
        for (int i = 0; i < 9; i++) locked[i] = !grid[i].isEmpty();
        out.add(new SlayerRecipes.Recipe("VX Reforge Stone", createReforgeStone(), grid, locked, null, 0));
        RecipeConfigManager.upsertCustomRecipesSilent(out);
    }
}
