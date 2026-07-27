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
 * The Aether Arsenal: ten attunements across ten gear pieces, so a hundred items that each carry
 * their attunement's own visual identity.
 *
 * <p>The pieces are not recolours of one another. Every slot has its own role and its own line of
 * behaviour, and every attunement has its own signature effect, so a Voidrend Greataxe and a
 * Verdant Greataxe fight differently and look nothing alike.</p>
 */
public final class AetherGear {

    private AetherGear() {}

    /** The ten pieces every attunement is forged into. */
    public enum Piece {
        BLADE("Blade", Items.NETHERITE_SWORD, Slot.WEAPON, "A fast edge that carries the attunement into every cut."),
        GREATAXE("Greataxe", Items.NETHERITE_AXE, Slot.WEAPON, "Slow, heavy, and it lands like a verdict."),
        LANCE("Lance", Items.TRIDENT, Slot.WEAPON, "Reach, and a signature that fires at the tip."),
        PICKAXE("Pickaxe", Items.NETHERITE_PICKAXE, Slot.TOOL, "Stone parts for it without much argument."),
        MATTOCK("Mattock", Items.NETHERITE_SHOVEL, Slot.TOOL, "Earth moves faster than it should."),
        REAPER("Reaper", Items.NETHERITE_HOE, Slot.TOOL, "Harvests wide and leaves the field kind."),
        HELM("Helm", Items.NETHERITE_HELMET, Slot.HEAD, "The attunement sits closest to the mind here."),
        CUIRASS("Cuirass", Items.NETHERITE_CHESTPLATE, Slot.CHEST, "The core of the set and the heart of its aura."),
        GREAVES("Greaves", Items.NETHERITE_LEGGINGS, Slot.LEGS, "Weight where weight is wanted."),
        TREADS("Treads", Items.NETHERITE_BOOTS, Slot.FEET, "The ground answers to these.");

        public enum Slot { WEAPON, TOOL, HEAD, CHEST, LEGS, FEET }

        public final String display;
        public final Item base;
        public final Slot slot;
        public final String blurb;

        Piece(String display, Item base, Slot slot, String blurb) {
            this.display = display;
            this.base = base;
            this.slot = slot;
            this.blurb = blurb;
        }
    }

    public static String idFor(AetherAttunement a, Piece p) {
        return "aether_" + a.prefix + "_" + p.name().toLowerCase(Locale.ROOT);
    }

    /** Reads the attunement off an Aether item, or null if it is not one. */
    public static AetherAttunement attunementOf(ItemStack stack) {
        String id = SlayerItems.getCustomItemId(stack);
        if (id == null) return null;
        id = SlayerItems.stripSkyblockEnchantSuffix(id);
        if (!id.startsWith("aether_")) return null;
        String[] bits = id.split("_");
        return bits.length >= 2 ? AetherAttunement.byPrefix(bits[1]) : null;
    }

    /** Reads the piece off an Aether item, or null. */
    public static Piece pieceOf(ItemStack stack) {
        String id = SlayerItems.getCustomItemId(stack);
        if (id == null) return null;
        id = SlayerItems.stripSkyblockEnchantSuffix(id);
        if (!id.startsWith("aether_")) return null;
        String[] bits = id.split("_");
        if (bits.length < 3) return null;
        for (Piece p : Piece.values()) {
            if (p.name().equalsIgnoreCase(bits[2])) return p;
        }
        return null;
    }

    // ── Stats, in multiples of five so the stats engine reads them cleanly ──

    private static int damageFor(Piece p) {
        return switch (p.slot) {
            case WEAPON -> switch (p) {
                case BLADE -> 120;
                case GREATAXE -> 165;
                default -> 140;
            };
            case TOOL -> 45;
            default -> 0;
        };
    }

    private static int strengthFor(Piece p) {
        return switch (p.slot) {
            case WEAPON -> 60;
            case TOOL -> 25;
            case CHEST -> 30;
            default -> 15;
        };
    }

    private static int defenseFor(Piece p) {
        return switch (p.slot) {
            case HEAD -> 55;
            case CHEST -> 85;
            case LEGS -> 70;
            case FEET -> 45;
            default -> 0;
        };
    }

    private static int healthFor(Piece p) {
        return switch (p.slot) {
            case HEAD -> 40;
            case CHEST -> 70;
            case LEGS -> 55;
            case FEET -> 35;
            default -> 0;
        };
    }

    /** Builds one piece of the arsenal. */
    public static ItemStack create(AetherAttunement a, Piece p) {
        ItemStack s = new ItemStack(p.base);
        s.set(DataComponents.CUSTOM_NAME,
                Component.literal("✦ " + a.display + " " + p.display)
                        .withStyle(a.colour, ChatFormatting.BOLD));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§8Aether Arsenal, " + a.display + " attunement"));
        lore.add(Component.literal("§7" + p.blurb));
        lore.add(Component.literal("§8\"" + a.flavor + "\""));
        lore.add(Component.literal(""));

        int dmg = damageFor(p);
        int str = strengthFor(p);
        int def = defenseFor(p);
        int hp = healthFor(p);
        if (dmg > 0) lore.add(Component.literal("§7⚔ Damage: §c+" + dmg));
        if (str > 0) lore.add(Component.literal("§7❁ Strength: §c+" + str));
        if (def > 0) lore.add(Component.literal("§7❈ Defense: §a+" + def));
        if (hp > 0) lore.add(Component.literal("§7❤ Health: §c+" + hp));
        if (p.slot == Piece.Slot.WEAPON) {
            lore.add(Component.literal("§7☠ Crit Damage: §9+35%"));
        }
        if (p.slot == Piece.Slot.HEAD || p.slot == Piece.Slot.CHEST) {
            lore.add(Component.literal("§7✎ Intelligence: §b+50"));
        }
        if (p.slot == Piece.Slot.FEET) {
            lore.add(Component.literal("§7✦ Speed: §f+5"));
        }

        lore.add(Component.literal(""));
        lore.add(Component.literal("§7Signature: " + a.colour + a.display));
        lore.add(Component.literal(switch (p.slot) {
            case WEAPON -> "§8Fires on a strike, on a cooldown.";
            case TOOL -> "§8Fires as you break blocks, on a cooldown.";
            default -> "§8Worn pieces give off the attunement's aura.";
        }));
        if (p.slot != Piece.Slot.WEAPON && p.slot != Piece.Slot.TOOL) {
            lore.add(Component.literal("§8Full set of one attunement: the signature"));
            lore.add(Component.literal("§8answers when you are badly hurt."));
        }

        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, idFor(a, p));
        return s;
    }

    /** Every piece of the arsenal, for catalogues. */
    public static List<ItemStack> allStacks() {
        List<ItemStack> out = new ArrayList<>();
        for (AetherAttunement a : AetherAttunement.values()) {
            for (Piece p : Piece.values()) {
                out.add(create(a, p));
            }
        }
        return out;
    }

    // ── Recipes ─────────────────────────────────────────────────────────────

    /** The catalyst that keys each attunement, so a recipe reads as that attunement. */
    private static Item catalystFor(AetherAttunement a) {
        return switch (a) {
            case SOLARIS -> Items.GLOWSTONE_DUST;
            case UMBRA -> Items.SCULK;
            case TEMPEST -> Items.LIGHTNING_ROD;
            case GLACIUS -> Items.BLUE_ICE;
            case VERDANT -> Items.MOSS_BLOCK;
            case ABYSSAL -> Items.PRISMARINE_SHARD;
            case EMBER -> Items.BLAZE_POWDER;
            case ASTRAL -> Items.NETHER_STAR;
            case SANGUINE -> Items.REDSTONE_BLOCK;
            case VOIDREND -> Items.ECHO_SHARD;
        };
    }

    /** The Aether line runs one tier hotter than the others, so nothing here drops below T2. */
    private static LineCatalyst.Tier tierFor(Piece p) {
        return switch (p.slot) {
            case TOOL -> LineCatalyst.Tier.T2;
            case CHEST, LEGS -> LineCatalyst.Tier.T3;
            default -> LineCatalyst.Tier.T2;
        };
    }

    public static void ensureRecipes() {
        List<SlayerRecipes.Recipe> out = new ArrayList<>();
        for (AetherAttunement a : AetherAttunement.values()) {
            Item catalyst = catalystFor(a);
            for (Piece p : Piece.values()) {
                // The base gear in the middle, the attunement catalyst around it, an Aether Mote
                // in the corners and the crafting core underneath.
                ItemStack[] grid = new ItemStack[9];
                grid[0] = LineCatalyst.AETHER_MOTE.create(2);
                grid[1] = new ItemStack(catalyst, 16);
                grid[2] = LineCatalyst.AETHER_MOTE.create(2);
                grid[3] = new ItemStack(catalyst, 16);
                grid[4] = new ItemStack(p.base);
                grid[5] = new ItemStack(catalyst, 16);
                grid[6] = new ItemStack(Items.NETHERITE_INGOT, 4);
                grid[7] = tierFor(p).core();
                grid[8] = new ItemStack(Items.NETHERITE_INGOT, 4);

                boolean[] locked = new boolean[9];
                for (int i = 0; i < 9; i++) locked[i] = true;

                out.add(new SlayerRecipes.Recipe(
                        "VX Aether " + a.display + " " + p.display,
                        create(a, p), grid, locked, null, 0));
            }
        }
        RecipeConfigManager.upsertCustomRecipesSilent(out);
    }
}
