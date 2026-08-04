package com.political;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

/**
 * Delveforging: turning ordinary gear into gear that belongs down there, and marking it up.
 *
 * <p>Two separate steps, and keeping them separate is the point. <b>Forging</b> is a one time thing
 * that admits an item to the delves at all. <b>Marks</b> are the long grind afterwards, each one
 * worth a little more of the item, and they are the reason to keep running a depth you have already
 * beaten.</p>
 *
 * <p>Both are written into the item rather than tracked in a side table, so a forged sword is still
 * forged after it has been traded, stored in a chest for a month, or carried through a server
 * restart. The name carries the marks visibly, which is the whole appeal: you can see what somebody
 * has done to their gear across the room.</p>
 *
 * <h2>The scale</h2>
 *
 * <p>Five marks, then five sigils. A mark is affordable and a sigil is not, so the first five are a
 * week and the last five are a project. Stats rise with both, and again with the depth you are
 * standing in, so the same chestplate is meaningfully stronger on Depth 7 than on Depth 1.</p>
 */
public final class DelveForge {

    private DelveForge() {}

    /** NBT keys. Short, because they sit on every forged item in the world. */
    private static final String FORGED = "delve_forged";
    private static final String MARKS = "delve_marks";
    private static final String SIGILS = "delve_sigils";
    private static final String BASE_NAME = "delve_basename";

    public static final int MAX_MARKS = 5;
    public static final int MAX_SIGILS = 5;

    /** The mark glyph, and the sigil that replaces it once the scale runs out. */
    public static final String MARK_GLYPH = "✦";
    public static final String SIGIL_GLYPH = "❖";

    // ── Reading ───────────────────────────────────────────────────────────

    public static boolean isForged(ItemStack stack) {
        CompoundTag t = tag(stack);
        return t != null && t.getIntOr(FORGED, 0) == 1;
    }

    public static int marks(ItemStack stack) {
        CompoundTag t = tag(stack);
        return t == null ? 0 : Math.max(0, Math.min(MAX_MARKS, t.getIntOr(MARKS, 0)));
    }

    public static int sigils(ItemStack stack) {
        CompoundTag t = tag(stack);
        return t == null ? 0 : Math.max(0, Math.min(MAX_SIGILS, t.getIntOr(SIGILS, 0)));
    }

    /** Marks and sigils together, which is what the stat maths actually cares about. */
    public static int totalUpgrades(ItemStack stack) {
        return marks(stack) + sigils(stack);
    }

    private static CompoundTag tag(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        CustomData d = stack.get(DataComponents.CUSTOM_DATA);
        return d == null ? null : d.copyTag();
    }

    private static void write(ItemStack stack, CompoundTag t) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
    }

    // ── Eligibility ───────────────────────────────────────────────────────

    /**
     * Whether this item can be forged at all.
     *
     * <p>Anything the mod recognises as gear qualifies, which deliberately includes the seven
     * thousand items that existed before the delves did. Locking forging to delve loot would have
     * made every set anybody already owns worthless the day this shipped.</p>
     */
    public static boolean canForge(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (isForged(stack)) return false;
        return CustomEnchantManager.isWeapon(stack)
                || CustomEnchantManager.isArmour(stack)
                || CustomEnchantManager.isTool(stack)
                || CustomEnchantManager.isBow(stack);
    }

    /** What forging one costs in coins, scaled by how much the item already carries. */
    public static long forgeCost(ItemStack stack) {
        long base = 250_000L;
        // Something already covered in stats is worth more, so it costs more to admit.
        int lines = 0;
        ItemLore lore = stack.get(DataComponents.LORE);
        if (lore != null) lines = lore.lines().size();
        return base + lines * 15_000L;
    }

    /** What the next mark costs. Steep, because the last one should be a decision. */
    public static long markCost(ItemStack stack) {
        int m = marks(stack);
        if (m < MAX_MARKS) return 400_000L * (m + 1L) * (m + 1L);
        int s = sigils(stack);
        return 5_000_000L * (s + 1L) * (s + 1L);
    }

    /** How many delve keys the next upgrade wants alongside the coins. */
    public static int markKeys(ItemStack stack) {
        int m = marks(stack);
        return m < MAX_MARKS ? (m + 1) : (sigils(stack) + 1) * 4;
    }

    // ── Writing ───────────────────────────────────────────────────────────

    /**
     * Forges an item.
     *
     * <p>The original name is stashed before anything is written over it, so the marks can be
     * rebuilt from scratch every time rather than appended to whatever the name happened to be.
     * Appending is how you end up with an item called Blade ✦ ✦✦ ✦✦✦.</p>
     */
    public static void forge(ItemStack stack) {
        CompoundTag t = tag(stack);
        if (t == null) t = new CompoundTag();
        t.putInt(FORGED, 1);
        if (!t.contains(BASE_NAME)) {
            t.putString(BASE_NAME, stack.getHoverName().getString());
        }
        write(stack, t);
        rename(stack);
    }

    /** Adds one mark, rolling over into sigils once the marks are full. Returns false when maxed. */
    public static boolean addMark(ItemStack stack) {
        if (!isForged(stack)) return false;
        CompoundTag t = tag(stack);
        if (t == null) return false;
        int m = marks(stack);
        if (m < MAX_MARKS) {
            t.putInt(MARKS, m + 1);
        } else {
            int s = sigils(stack);
            if (s >= MAX_SIGILS) return false;
            t.putInt(SIGILS, s + 1);
        }
        write(stack, t);
        rename(stack);
        return true;
    }

    /**
     * Rewrites the name to carry the marks.
     *
     * <p>Rebuilt from the stashed original every time, never appended to the current name, so an
     * item that has been marked five times reads as one name and five glyphs rather than as a
     * history of every upgrade it has had.</p>
     */
    public static void rename(ItemStack stack) {
        CompoundTag t = tag(stack);
        if (t == null) return;
        String base = t.getStringOr(BASE_NAME, "");
        if (base.isEmpty()) {
            base = stack.getHoverName().getString();
            t.putString(BASE_NAME, base);
            write(stack, t);
        }

        int m = marks(stack);
        int s = sigils(stack);
        StringBuilder suffix = new StringBuilder();
        if (m > 0 || s > 0) {
            suffix.append(' ');
            // Sigils replace marks rather than sitting beside them, so a fully upgraded item reads
            // as five sigils and not as ten glyphs of two kinds.
            if (s > 0) {
                suffix.append("§d");
                suffix.append(SIGIL_GLYPH.repeat(s));
                if (m > 0 && s < MAX_SIGILS) {
                    suffix.append("§6").append(MARK_GLYPH.repeat(MAX_MARKS - 0));
                }
            } else {
                suffix.append("§6").append(MARK_GLYPH.repeat(m));
            }
        }

        String prefix = isForged(stack) ? "§5" : "";
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal(prefix + base + suffix)
                        .withStyle(st -> st.withItalic(false)));
    }

    // ── Stats ─────────────────────────────────────────────────────────────

    /**
     * How much this item's stats are multiplied by.
     *
     * <p>Three things stack: being forged at all, how far it has been marked up, and how deep the
     * wearer currently is. The depth part is what makes delve gear feel like delve gear without
     * making it useless on the surface, which was the brief.</p>
     */
    public static double statMultiplier(ItemStack stack, int depthTier) {
        if (!isForged(stack)) return 1.0;
        double mult = 1.15;                        // forged at all
        mult += marks(stack) * 0.10;               // each mark
        mult += sigils(stack) * 0.25;              // each sigil, worth far more
        if (depthTier > 0) {
            // Inside a delve, and deeper is better. Outside, this term is simply absent.
            mult += depthTier * 0.04;
        }
        return mult;
    }

    /** The multiplier for a player right now, taking their location into account. */
    public static double statMultiplier(ItemStack stack, ServerPlayer player) {
        DelveRun run = DelveManager.runOf(player);
        return statMultiplier(stack, run == null ? 0 : run.floor.tier);
    }

    /**
     * The extra a delve exclusive piece gets for being used where it belongs.
     *
     * <p>These were made down there and they know it. Outside they are merely good; inside they are
     * what you brought them for.</p>
     */
    public static double homeBonus(ItemStack stack, ServerPlayer player) {
        String id = SlayerItems.getCustomItemId(stack);
        if (id == null || !id.startsWith("delve_")) return 1.0;
        DelveRun run = DelveManager.runOf(player);
        if (run == null) return 1.0;
        // Marks make the home bonus better too, which is what ties the two systems together.
        return 1.20 + totalUpgrades(stack) * 0.03;
    }

    /** A one line summary for a tooltip or a menu. */
    public static String describe(ItemStack stack) {
        if (!isForged(stack)) return "§8Not delveforged";
        int m = marks(stack);
        int s = sigils(stack);
        if (s > 0) return "§dForged, " + s + " sigil" + (s == 1 ? "" : "s");
        if (m > 0) return "§6Forged, " + m + " mark" + (m == 1 ? "" : "s");
        return "§5Delveforged";
    }
}
