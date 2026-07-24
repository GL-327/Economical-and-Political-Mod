package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * V7 Pillar B — the Cursed Arsenal: a full graded gear line themed on cursed energy.
 *
 * <p>Five grades (Grade 4 up to Special Grade) across four families:</p>
 * <ul>
 *   <li><b>Weapons</b> — sword (cleave), scythe (reap bleed), katana (Black Flash window),
 *       gauntlet (combo). Each has a distinct on-hit signature enforced in
 *       {@link CursedArsenalHandler}.</li>
 *   <li><b>Armor</b> — helmet, chestplate, leggings, boots. Worn as a full set the pieces
 *       convert a little of the damage you take into Cursed Energy and lash a little back.</li>
 *   <li><b>Charms</b> — a Malevolent (offensive) and a Warding (defensive) charm, held in the
 *       off-hand for a bounded passive buff driven by {@link CursedArsenalHandler}.</li>
 * </ul>
 *
 * <p>Every stack carries a stable custom item id (set LAST) so the handler and the codex can key
 * off it, and an explicit stat block (all values multiples of 5) that feeds
 * {@link PlayerStatsManager}. Numbers are tuned to sit at or a touch under the existing endgame
 * cursed tools (see {@link CursedTool}); nothing here outclasses that line.</p>
 */
public final class CursedArsenal {

    private CursedArsenal() {}

    public static final String ID_PREFIX = "cursed_arsenal_";

    // Grade order used by every stat table: index 0 = Grade 4 ... index 4 = Special Grade.
    public static final CursedGrade[] GRADES = {
            CursedGrade.GRADE_4, CursedGrade.GRADE_3, CursedGrade.GRADE_2,
            CursedGrade.GRADE_1, CursedGrade.SPECIAL_GRADE
    };

    // ── Grade helpers ─────────────────────────────────────────────────────────
    public static int gradeIndex(CursedGrade g) {
        return g.ordinal(); // CursedGrade is already ordered GRADE_4..SPECIAL_GRADE
    }

    public static String gradeKey(CursedGrade g) {
        return switch (g) {
            case GRADE_4 -> "g4";
            case GRADE_3 -> "g3";
            case GRADE_2 -> "g2";
            case GRADE_1 -> "g1";
            case SPECIAL_GRADE -> "sg";
        };
    }

    public static CursedGrade gradeFromKey(String key) {
        if (key == null) return null;
        return switch (key) {
            case "g4" -> CursedGrade.GRADE_4;
            case "g3" -> CursedGrade.GRADE_3;
            case "g2" -> CursedGrade.GRADE_2;
            case "g1" -> CursedGrade.GRADE_1;
            case "sg" -> CursedGrade.SPECIAL_GRADE;
            default -> null;
        };
    }

    /** A bold, coloured rarity footer that matches the look of {@link ItemStatInjector}. */
    public static String rarityFooter(CursedGrade g, String kind) {
        String colour;
        String rarity;
        switch (g) {
            case GRADE_4 -> { colour = "§a"; rarity = "UNCOMMON"; }
            case GRADE_3 -> { colour = "§9"; rarity = "RARE"; }
            case GRADE_2 -> { colour = "§5"; rarity = "EPIC"; }
            case GRADE_1 -> { colour = "§6"; rarity = "LEGENDARY"; }
            default -> { colour = "§d"; rarity = "MYTHIC"; }
        }
        return colour + "§l" + rarity + " " + kind;
    }

    // A faint divider that also stops ItemStatInjector from double-adding stats.
    private static final String DIVIDER = "§8§m                    §r";

    // ════════════════════════════════════════════════════════════════════════
    // WEAPONS
    // ════════════════════════════════════════════════════════════════════════
    public enum Weapon {
        SWORD("sword", "Cursed Blade", Items.NETHERITE_SWORD, "Sword",
                "§7On hit: cleave nearby foes for bonus damage.",
                new int[]{40, 70, 100, 135, 170},   // damage
                new int[]{20, 30, 45, 60, 75},       // strength
                new int[]{20, 30, 40, 50, 60},       // crit damage %
                Extra.FEROCITY, new int[]{0, 5, 5, 10, 10}),
        SCYTHE("scythe", "Reaper Scythe", Items.NETHERITE_HOE, "Scythe",
                "§7On hit: reap a bleed and drain a little life.",
                new int[]{35, 60, 90, 120, 155},
                new int[]{15, 25, 35, 50, 65},
                new int[]{30, 45, 60, 75, 90},
                Extra.CRIT_CHANCE, new int[]{5, 5, 10, 10, 15}),
        KATANA("katana", "Split-Soul Katana", Items.NETHERITE_SWORD, "Katana",
                "§7On hit: time your swings for a Black Flash.",
                new int[]{35, 65, 95, 130, 165},
                new int[]{15, 25, 40, 55, 70},
                new int[]{20, 25, 35, 45, 55},
                Extra.CRIT_CHANCE, new int[]{10, 15, 20, 25, 30}),
        GAUNTLET("gauntlet", "Cursed Gauntlet", Items.MACE, "Gauntlet",
                "§7On hit: build a combo for rising damage.",
                new int[]{30, 55, 80, 110, 145},
                new int[]{25, 40, 55, 70, 85},
                new int[]{15, 20, 30, 40, 50},
                Extra.FEROCITY, new int[]{5, 10, 15, 20, 25});

        public final String key;
        public final String display;
        public final Item base;
        public final String kind;
        public final String signature;
        public final int[] damage;
        public final int[] strength;
        public final int[] critDamage;
        public final Extra extraStat;
        public final int[] extra;

        Weapon(String key, String display, Item base, String kind, String signature,
               int[] damage, int[] strength, int[] critDamage, Extra extraStat, int[] extra) {
            this.key = key;
            this.display = display;
            this.base = base;
            this.kind = kind;
            this.signature = signature;
            this.damage = damage;
            this.strength = strength;
            this.critDamage = critDamage;
            this.extraStat = extraStat;
            this.extra = extra;
        }
    }

    public enum Extra {
        FEROCITY("⫽ Ferocity", "§c"),
        CRIT_CHANCE("☣ Crit Chance", "§9");

        final String label;
        final String colour;
        final boolean percent;

        Extra(String label, String colour) {
            this.label = label;
            this.colour = colour;
            this.percent = label.contains("Crit");
        }
    }

    public static ItemStack buildWeapon(Weapon w, CursedGrade g) {
        int i = gradeIndex(g);
        ItemStack s = new ItemStack(w.base);
        s.set(DataComponents.CUSTOM_NAME, Component.literal("☠ " + w.display + " " + gradeRoman(g))
                .withStyle(g.color, ChatFormatting.BOLD));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§8Cursed Arsenal — " + g.label));
        lore.add(Component.literal("§7⚔ Damage: §c+" + w.damage[i]));
        lore.add(Component.literal("§7❁ Strength: §c+" + w.strength[i]));
        lore.add(Component.literal("§7☠ Crit Damage: §9+" + w.critDamage[i] + "%"));
        if (w.extra[i] > 0) {
            lore.add(Component.literal("§7" + w.extraStat.label + ": " + w.extraStat.colour + "+"
                    + w.extra[i] + (w.extraStat.percent ? "%" : "")));
        }
        lore.add(Component.literal(w.signature));
        lore.add(Component.literal(DIVIDER));
        lore.add(Component.literal(rarityFooter(g, "WEAPON")));
        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

        SlayerItems.setCustomItemId(s, weaponId(w, g)); // LAST
        return s;
    }

    public static String weaponId(Weapon w, CursedGrade g) {
        return ID_PREFIX + w.key + "_" + gradeKey(g);
    }

    /** Returns the weapon this stack is, or null. */
    public static Weapon weaponOf(ItemStack stack) {
        String id = idOf(stack);
        if (id == null) return null;
        for (Weapon w : Weapon.values()) {
            if (id.startsWith(ID_PREFIX + w.key + "_")) return w;
        }
        return null;
    }

    // ════════════════════════════════════════════════════════════════════════
    // ARMOR
    // ════════════════════════════════════════════════════════════════════════
    public enum Piece {
        HELMET("helmet", "Hood", "Helmet",
                new int[]{15, 25, 35, 45, 55}, new int[]{10, 15, 25, 30, 40},
                Items.IRON_HELMET, Items.DIAMOND_HELMET, Items.NETHERITE_HELMET),
        CHEST("chest", "Shroud", "Chestplate",
                new int[]{25, 35, 50, 65, 80}, new int[]{15, 25, 35, 45, 55},
                Items.IRON_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.NETHERITE_CHESTPLATE),
        LEGS("legs", "Wraps", "Leggings",
                new int[]{20, 30, 45, 55, 70}, new int[]{10, 20, 30, 40, 50},
                Items.IRON_LEGGINGS, Items.DIAMOND_LEGGINGS, Items.NETHERITE_LEGGINGS),
        BOOTS("boots", "Treads", "Boots",
                new int[]{15, 20, 30, 40, 50}, new int[]{10, 15, 20, 25, 35},
                Items.IRON_BOOTS, Items.DIAMOND_BOOTS, Items.NETHERITE_BOOTS);

        public final String key;
        public final String noun;
        public final String kind;
        public final int[] defense;
        public final int[] health;
        final Item iron;
        final Item diamond;
        final Item netherite;

        Piece(String key, String noun, String kind, int[] defense, int[] health,
              Item iron, Item diamond, Item netherite) {
            this.key = key;
            this.noun = noun;
            this.kind = kind;
            this.defense = defense;
            this.health = health;
            this.iron = iron;
            this.diamond = diamond;
            this.netherite = netherite;
        }

        Item baseFor(CursedGrade g) {
            return switch (g) {
                case GRADE_4 -> iron;
                case GRADE_3, GRADE_2 -> diamond;
                default -> netherite;
            };
        }
    }

    public static ItemStack buildArmor(Piece p, CursedGrade g) {
        int i = gradeIndex(g);
        ItemStack s = new ItemStack(p.baseFor(g));
        s.set(DataComponents.CUSTOM_NAME, Component.literal("☠ Cursed " + p.noun + " " + gradeRoman(g))
                .withStyle(g.color, ChatFormatting.BOLD));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§8Cursed Arsenal — " + g.label));
        lore.add(Component.literal("§7❈ Defense: §a+" + p.defense[i]));
        lore.add(Component.literal("§7❤ Health: §c+" + p.health[i]));
        lore.add(Component.literal("§8Set: cursed energy backlash."));
        lore.add(Component.literal(DIVIDER));
        lore.add(Component.literal(rarityFooter(g, "ARMOR")));
        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

        SlayerItems.setCustomItemId(s, armorId(p, g)); // LAST
        return s;
    }

    public static String armorId(Piece p, CursedGrade g) {
        return ID_PREFIX + p.key + "_" + gradeKey(g);
    }

    /** The armor piece this stack is, or null. */
    public static Piece pieceOf(ItemStack stack) {
        String id = idOf(stack);
        if (id == null) return null;
        for (Piece p : Piece.values()) {
            if (id.startsWith(ID_PREFIX + p.key + "_")) return p;
        }
        return null;
    }

    // ════════════════════════════════════════════════════════════════════════
    // CHARMS (off-hand passives, applied by CursedArsenalHandler)
    // ════════════════════════════════════════════════════════════════════════
    public enum Charm {
        MALEVOLENT("malevolent", "Malevolent Cursed Charm", Items.BLAZE_POWDER, ChatFormatting.RED,
                "§7Off-hand: your strikes hit harder.",
                new String[]{
                        "§7Strength I while held",
                        "§7Strength I while held",
                        "§7Strength II while held",
                        "§7Strength II, brief Speed",
                        "§7Strength III, brief Speed"
                }),
        WARDING("warding", "Warding Cursed Charm", Items.HEART_OF_THE_SEA, ChatFormatting.AQUA,
                "§7Off-hand: cursed energy shields you.",
                new String[]{
                        "§7Absorption I while held",
                        "§7Absorption I while held",
                        "§7Absorption II while held",
                        "§7Absorption II, slow Regen",
                        "§7Absorption III, slow Regen"
                });

        public final String key;
        public final String display;
        public final Item base;
        public final ChatFormatting colour;
        public final String tagline;
        public final String[] gradeText;

        Charm(String key, String display, Item base, ChatFormatting colour, String tagline, String[] gradeText) {
            this.key = key;
            this.display = display;
            this.base = base;
            this.colour = colour;
            this.tagline = tagline;
            this.gradeText = gradeText;
        }
    }

    public static ItemStack buildCharm(Charm c, CursedGrade g) {
        int i = gradeIndex(g);
        ItemStack s = new ItemStack(c.base);
        s.set(DataComponents.CUSTOM_NAME, Component.literal("✦ " + c.display + " " + gradeRoman(g))
                .withStyle(c.colour, ChatFormatting.BOLD));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§8Cursed Arsenal — " + g.label));
        lore.add(Component.literal(c.tagline));
        lore.add(Component.literal(c.gradeText[i]));
        lore.add(Component.literal("§8Hold in your off-hand to use it."));
        lore.add(Component.literal(DIVIDER));
        lore.add(Component.literal(rarityFooter(g, "CHARM")));
        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

        SlayerItems.setCustomItemId(s, charmId(c, g)); // LAST
        return s;
    }

    public static String charmId(Charm c, CursedGrade g) {
        return ID_PREFIX + "charm_" + c.key + "_" + gradeKey(g);
    }

    /** The charm this stack is, or null. */
    public static Charm charmOf(ItemStack stack) {
        String id = idOf(stack);
        if (id == null) return null;
        for (Charm c : Charm.values()) {
            if (id.startsWith(ID_PREFIX + "charm_" + c.key + "_")) return c;
        }
        return null;
    }

    // ════════════════════════════════════════════════════════════════════════
    // Shared parse helpers
    // ════════════════════════════════════════════════════════════════════════
    public static String idOf(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        String id = SlayerItems.getCustomItemId(stack);
        if (id == null) return null;
        id = SlayerItems.stripSkyblockEnchantSuffix(id).toLowerCase(Locale.ROOT);
        return id.startsWith(ID_PREFIX) ? id : null;
    }

    /** Reads the grade off any arsenal stack (weapon, armor or charm), or null. */
    public static CursedGrade gradeOf(ItemStack stack) {
        String id = idOf(stack);
        if (id == null) return null;
        int us = id.lastIndexOf('_');
        if (us < 0) return null;
        return gradeFromKey(id.substring(us + 1));
    }

    private static String gradeRoman(CursedGrade g) {
        return switch (g) {
            case GRADE_4 -> "IV";
            case GRADE_3 -> "III";
            case GRADE_2 -> "II";
            case GRADE_1 -> "I";
            case SPECIAL_GRADE -> "S";
        };
    }
}
