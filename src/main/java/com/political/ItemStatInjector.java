package com.political;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Deterministic per-id stat + flavor generator for the whole custom-item catalog.
 *
 * <p>Given a custom_item_id, its base item and a derived tier, this produces Skyblock-style
 * stat lines (⚔ Damage / ❈ Defense / ❤ Health / ❁ Strength / ☣ Crit Chance / ☠ Crit Damage)
 * in multiples of 5, plus one RPG flavor line. Values are seeded off the id's stable hash so
 * the same item always rolls the same sheet. The symbols match exactly what
 * {@link PlayerStatsManager} (and {@code CustomItemHandler.getListedLoreDamage}) parse, so
 * every custom item that flows through {@link CustomItemLore#applySummarizedLore} feeds the
 * V3 stats engine.</p>
 */
public final class ItemStatInjector {

    private ItemStatInjector() {}

    private enum Category { WEAPON, ARMOR, TOOL, MISC }

    /** Marker text placed on the stats header so re-processing is idempotent. */
    private static final String HEADER = "§8§m                    §r";
    private static final int MAX_BASE_LINES_WHEN_INJECTING = 9;

    /**
     * Appends generated stat lines (+ flavor) to {@code baseLines} for the given stack.
     * Returns the base list unchanged if the stack has no custom id, or already carries stats.
     */
    public static List<Component> appendStats(ItemStack stack, List<Component> baseLines) {
        if (stack == null || stack.isEmpty()) return baseLines;
        String id = SlayerItems.getCustomItemId(stack);
        if (id == null || id.isEmpty()) return baseLines;
        if (alreadyHasStats(baseLines)) return baseLines;

        Category cat = classify(stack, id);
        int tier = deriveTier(id);
        long seed = stableSeed(id);

        List<Component> stats = buildStatLines(cat, tier, seed);
        if (stats.isEmpty()) return baseLines;

        List<Component> out = new ArrayList<>();
        // Keep the descriptive lore compact so the stat block stays readable.
        int kept = 0;
        for (Component c : baseLines) {
            if (kept >= MAX_BASE_LINES_WHEN_INJECTING) break;
            out.add(c);
            kept++;
        }
        out.add(Component.literal(HEADER));
        out.addAll(stats);
        out.add(Component.literal(flavor(id, cat, seed)));
        return out;
    }

    // ════════════════════════════════════════════════════════════
    // STAT DERIVATION
    // ════════════════════════════════════════════════════════════
    private static List<Component> buildStatLines(Category cat, int tier, long seed) {
        List<Component> lines = new ArrayList<>();
        switch (cat) {
            case WEAPON -> {
                int damage = mult5(15 + tier * 10, 6, seed, 1);        // base weapon damage
                int strength = mult5(10 + tier * 5, 5, seed, 2);
                int critChance = clamp(mult5(5 + tier * 2, 4, seed, 3), 5, 40);
                int critDamage = mult5(20 + tier * 10, 6, seed, 4);
                lines.add(Component.literal("§7⚔ Damage: §c+" + damage));
                lines.add(Component.literal("§7❁ Strength: §c+" + strength));
                lines.add(Component.literal("§7☣ Crit Chance: §9+" + critChance + "%"));
                lines.add(Component.literal("§7☠ Crit Damage: §9+" + critDamage + "%"));
            }
            case ARMOR -> {
                int defense = mult5(10 + tier * 10, 6, seed, 1);
                int health = mult5(10 + tier * 5, 5, seed, 2);
                int strength = mult5(tier * 3, 4, seed, 3);            // may roll 0
                lines.add(Component.literal("§7❈ Defense: §a+" + defense));
                lines.add(Component.literal("§7❤ Health: §c+" + health));
                if (strength > 0) {
                    lines.add(Component.literal("§7❁ Strength: §c+" + strength));
                }
            }
            case TOOL -> {
                int strength = mult5(5 + tier * 3, 4, seed, 1);
                int health = mult5(5 + tier * 2, 3, seed, 2);
                lines.add(Component.literal("§7❁ Strength: §c+" + strength));
                lines.add(Component.literal("§7❤ Health: §c+" + health));
            }
            case MISC -> {
                int strength = mult5(5 + tier * 2, 4, seed, 1);
                int health = mult5(5 + tier * 2, 3, seed, 2);
                lines.add(Component.literal("§7❁ Strength: §c+" + strength));
                lines.add(Component.literal("§7❤ Health: §c+" + health));
            }
        }
        return lines;
    }

    /** Returns a multiple of 5: 5 * (floor(base/5) + (seed-derived offset in 0..spread-1)). */
    private static int mult5(int base, int spread, long seed, int salt) {
        int units = Math.max(0, base / 5);
        int off = (int) (mix(seed, salt) % Math.max(1, spread));
        return 5 * (units + off);
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    // ════════════════════════════════════════════════════════════
    // CLASSIFICATION
    // ════════════════════════════════════════════════════════════
    private static Category classify(ItemStack stack, String id) {
        Equippable eq = stack.get(DataComponents.EQUIPPABLE);
        if (eq != null) {
            EquipmentSlot s = eq.slot();
            if (s == EquipmentSlot.HEAD || s == EquipmentSlot.CHEST
                    || s == EquipmentSlot.LEGS || s == EquipmentSlot.FEET) {
                return Category.ARMOR;
            }
        }
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().toLowerCase(Locale.ROOT);
        String lid = id.toLowerCase(Locale.ROOT);
        if (path.contains("sword") || lid.contains("sword") || lid.contains("blade")
                || lid.contains("dagger") || lid.contains("scythe") || lid.contains("katana")
                || lid.contains("bow") || lid.contains("wand") || lid.contains("staff")
                || lid.contains("gavel") || lid.contains("spear")) {
            return Category.WEAPON;
        }
        if (path.contains("pickaxe") || path.contains("_axe") || path.endsWith("axe")
                || path.contains("shovel") || path.contains("hoe")
                || path.contains("shears") || path.contains("fishing_rod")) {
            return Category.TOOL;
        }
        return Category.MISC;
    }

    // ════════════════════════════════════════════════════════════
    // TIER
    // ════════════════════════════════════════════════════════════
    private static int deriveTier(String id) {
        String lid = id.toLowerCase(Locale.ROOT);
        // explicit "_t<digit>" marker
        int ti = lid.indexOf("_t");
        while (ti >= 0) {
            int j = ti + 2;
            if (j < lid.length() && Character.isDigit(lid.charAt(j))) {
                int val = 0;
                while (j < lid.length() && Character.isDigit(lid.charAt(j))) {
                    val = val * 10 + (lid.charAt(j) - '0');
                    j++;
                }
                if (val >= 1) return clamp(val, 1, 10);
            }
            ti = lid.indexOf("_t", ti + 2);
        }
        if (lid.contains("omega")) return 10;
        if (lid.contains("legendary") || lid.contains("celestial") || lid.contains("void")
                || lid.contains("ascend")) return 8;
        if (lid.contains("dragon") || lid.contains("wyrm") || lid.contains("mythic")) return 6;
        if (lid.contains("enchanted") || lid.contains("gilded") || lid.contains("super")) return 5;
        if (lid.contains("compacted")) return 4;
        // trailing number (e.g. new_dragon_chestplate_42)
        int n = trailingNumber(lid);
        if (n > 0) return clamp(2 + n / 20, 2, 8);
        // otherwise a stable low tier
        return 1 + (int) (stableSeed(id) % 3); // 1..3
    }

    private static int trailingNumber(String s) {
        int i = s.length();
        while (i > 0 && Character.isDigit(s.charAt(i - 1))) i--;
        if (i == s.length()) return 0;
        try {
            return Integer.parseInt(s.substring(i));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ════════════════════════════════════════════════════════════
    // FLAVOR
    // ════════════════════════════════════════════════════════════
    private static final String[] GENERIC_FLAVOR = {
            "§8\"Forged for those who dare to wield it.\"",
            "§8\"Its power hums beneath the surface.\"",
            "§8\"Few hands are worthy of its edge.\"",
            "§8\"A relic of a forgotten age.\"",
            "§8\"Whispers of old battles cling to it.\"",
            "§8\"Not the strongest, but the boldest choose it.\"",
            "§8\"Shaped by fire, tempered by will.\"",
            "§8\"It remembers every foe it has felled.\""
    };

    private static String flavor(String id, Category cat, long seed) {
        String lid = id.toLowerCase(Locale.ROOT);
        if (lid.contains("dragon") || lid.contains("wyrm"))
            return "§8\"Scaled in the breath of ancient dragons.\"";
        if (lid.contains("void") || lid.contains("ender") || lid.contains("phase"))
            return "§8\"It steps between worlds as easily as shadow.\"";
        if (lid.contains("venom") || lid.contains("poison") || lid.contains("toxic"))
            return "§8\"Every touch leaves a lingering rot.\"";
        if (lid.contains("frost") || lid.contains("glacier") || lid.contains("ice"))
            return "§8\"Cold enough to still a beating heart.\"";
        if (lid.contains("blood") || lid.contains("vampir"))
            return "§8\"It thirsts, and it is never sated.\"";
        if (lid.contains("solar") || lid.contains("sun") || lid.contains("radiant"))
            return "§8\"Kindled with the light of a dying star.\"";
        if (lid.contains("lunar") || lid.contains("moon") || lid.contains("night"))
            return "§8\"It waxes strong beneath the pale moon.\"";
        if (lid.contains("garden") || lid.contains("verdant") || lid.contains("nature"))
            return "§8\"Life springs eager wherever it treads.\"";
        if (lid.contains("storm") || lid.contains("thunder") || lid.contains("tempest"))
            return "§8\"The sky answers when it is raised.\"";
        int idx = (int) (mix(seed, 99) % GENERIC_FLAVOR.length);
        return GENERIC_FLAVOR[idx];
    }

    // ════════════════════════════════════════════════════════════
    // IDEMPOTENCY + HASHING
    // ════════════════════════════════════════════════════════════
    private static boolean alreadyHasStats(List<Component> lines) {
        for (Component c : lines) {
            String s = c.getString();
            if (s == null) continue;
            if (s.contains(HEADER)) return true;
            String lower = s.replaceAll("§.", "").toLowerCase(Locale.ROOT);
            if (lower.contains("strength:") || lower.contains("defense:")
                    || lower.contains("⚔ damage:") || lower.contains("crit chance:")
                    || lower.contains("crit damage:")) {
                return true;
            }
        }
        return false;
    }

    private static long stableSeed(String id) {
        long h = 1125899906842597L; // prime
        for (int i = 0; i < id.length(); i++) {
            h = 31 * h + id.charAt(i);
        }
        return h & 0x7fffffffffffffffL;
    }

    private static long mix(long seed, int salt) {
        long x = seed ^ (0x9E3779B97F4A7C15L * (salt + 1));
        x ^= (x >>> 30);
        x *= 0xBF58476D1CE4E5B9L;
        x ^= (x >>> 27);
        x *= 0x94D049BB133111EBL;
        x ^= (x >>> 31);
        return x & 0x7fffffffffffffffL;
    }
}
