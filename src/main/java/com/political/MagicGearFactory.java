package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

/**
 * Magic V4 gear: per-school robe sets, catalysts, grimoires, technique relics and the
 * Awakening Catalyst. Everything is a dressed vanilla stack carrying a custom_item_id,
 * so it flows through the normal lore/stat pipeline.
 *
 * Robes grant "school affinity", read back by {@link SchoolTreeManager#getAffinity}.
 */
public final class MagicGearFactory {

    private MagicGearFactory() {}

    /** Affinity granted per matching worn robe piece. */
    private static final int AFFINITY_PER_PIECE = 5;
    /** Extra affinity for wearing the full 4/4 school set. */
    private static final int FULL_SET_AFFINITY = 20;

    /**
     * Canonical grimoire shard tiers. {@link MagicRecipeBootstrap} registers a recipe for every
     * (school × tier) pair, so this array is the single source of truth for both the catalogue
     * and the recipe book.
     */
    public static final int[] GRIMOIRE_TIERS = {100, 250, 500};

    /** The four armour slots a robe/archmage set occupies, in display order. */
    public static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    // ── ids ────────────────────────────────────────────────────────────────

    public static String robeId(MagicSchool school, EquipmentSlot slot) {
        return "robe_" + school.prefix + "_" + pieceName(slot);
    }

    private static String pieceName(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> "hood";
            case CHEST -> "robe";
            case LEGS -> "leggings";
            default -> "boots";
        };
    }

    private static Item pieceItem(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> Items.LEATHER_HELMET;
            case CHEST -> Items.LEATHER_CHESTPLATE;
            case LEGS -> Items.LEATHER_LEGGINGS;
            default -> Items.LEATHER_BOOTS;
        };
    }

    // ── factories ──────────────────────────────────────────────────────────

    public static ItemStack createRobePiece(MagicSchool school, EquipmentSlot slot) {
        ItemStack s = new ItemStack(pieceItem(slot));
        String piece = switch (slot) {
            case HEAD -> "Hood";
            case CHEST -> "Robe";
            case LEGS -> "Leggings";
            default -> "Slippers";
        };
        s.set(DataComponents.CUSTOM_NAME,
                Component.literal(school.symbol + " " + school.displayName + " " + piece)
                        .withStyle(school.color, ChatFormatting.BOLD));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§8" + school.displayName + " Regalia"));
        lore.add(Component.literal("§7" + school.description));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§7❈ Defense: §a+" + defenseFor(slot)));
        lore.add(Component.literal("§7✎ Intelligence: §b+" + intelligenceFor(slot)));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§7Affinity: " + school.legacyColor + "+" + AFFINITY_PER_PIECE
                + " " + school.displayName));
        lore.add(Component.literal("§8Full set: §7+" + FULL_SET_AFFINITY + " affinity"));
        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.DYED_COLOR, new net.minecraft.world.item.component.DyedItemColor(school.color.getColor() == null ? 0xFFFFFF : school.color.getColor()));
        SlayerItems.setCustomItemId(s, robeId(school, slot));
        return s;
    }

    private static int defenseFor(EquipmentSlot slot) {
        return switch (slot) {
            case CHEST -> 30;
            case LEGS -> 25;
            case HEAD -> 15;
            default -> 10;
        };
    }

    private static int intelligenceFor(EquipmentSlot slot) {
        return switch (slot) {
            case CHEST -> 50;
            case LEGS -> 35;
            case HEAD -> 40;
            default -> 25;
        };
    }

    public static ItemStack createCatalyst(MagicSchool school) {
        ItemStack s = new ItemStack(Items.AMETHYST_SHARD);
        s.set(DataComponents.CUSTOM_NAME,
                Component.literal(school.symbol + " " + school.displayName + " Catalyst")
                        .withStyle(school.color, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§8Off-hand Catalyst"));
        lore.add(Component.literal("§7Focuses raw aether into a single school."));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§7✎ Intelligence: §b+75"));
        lore.add(Component.literal("§7Spell Power: " + school.legacyColor + "+15%"));
        lore.add(Component.literal("§7Cooldowns: §a-10%"));
        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, "catalyst_" + school.prefix);
        return s;
    }

    public static ItemStack createGrimoire(MagicSchool school, int shards) {
        ItemStack s = new ItemStack(Items.ENCHANTED_BOOK);
        s.set(DataComponents.CUSTOM_NAME,
                Component.literal("✦ Grimoire of " + school.displayName)
                        .withStyle(school.color, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§8Consumable"));
        lore.add(Component.literal("§7Right-click to absorb §b" + shards + " Arcane Shards§7."));
        lore.add(Component.literal("§8Shards unlock " + school.displayName + " tree nodes."));
        s.set(DataComponents.LORE, new ItemLore(lore));
        SlayerItems.setCustomItemId(s, "grimoire_" + school.prefix + "_" + shards);
        return s;
    }

    public static ItemStack createAwakeningCatalyst() {
        ItemStack s = new ItemStack(Items.NETHER_STAR);
        s.set(DataComponents.CUSTOM_NAME,
                Component.literal("✦ Awakening Catalyst").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§8Rite of Awakening"));
        lore.add(Component.literal("§7Consumed to awaken your innate §dMagic Technique§7."));
        lore.add(Component.literal("§7Requires Magic Level §e" + TechniqueManager.AWAKEN_LEVEL + "§7."));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Some techniques are held by only one soul"));
        lore.add(Component.literal("§8on the entire server. Most never find them."));
        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, "awakening_catalyst");
        return s;
    }

    public static ItemStack createTechniqueRelic(Technique t) {
        ItemStack s = new ItemStack(Items.ECHO_SHARD);
        s.set(DataComponents.CUSTOM_NAME,
                Component.literal("✦ Relic of " + t.displayName).withStyle(t.color, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§8Technique Relic"));
        lore.add(Component.literal("§7" + t.flavor));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§7Carrying this while " + t.displayName + " is your"));
        lore.add(Component.literal("§7awakened Technique deepens the bond:"));
        lore.add(Component.literal("§7Ability energy cost: §a-20%"));
        lore.add(Component.literal("§7❁ Strength: §c+15"));
        lore.add(Component.literal("§8Inert if you have awakened anything else."));
        if (t.exclusive) {
            lore.add(Component.literal(""));
            lore.add(Component.literal("§d§lEXCLUSIVE TECHNIQUE"));
            lore.add(Component.literal("§8Cannot be crafted — it appears only in the"));
            lore.add(Component.literal("§8hands of the one soul that holds the Technique."));
        }
        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, "relic_" + t.name().toLowerCase(java.util.Locale.ROOT));
        return s;
    }

    public static ItemStack createArchmagePiece(EquipmentSlot slot) {
        ItemStack s = new ItemStack(pieceItem(slot));
        String piece = switch (slot) {
            case HEAD -> "Crown";
            case CHEST -> "Mantle";
            case LEGS -> "Leggings";
            default -> "Striders";
        };
        s.set(DataComponents.CUSTOM_NAME,
                Component.literal("✦ Archmage " + piece).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§8Endgame Regalia"));
        lore.add(Component.literal("§7Woven from every school at once."));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§7❈ Defense: §a+" + (defenseFor(slot) + 25)));
        lore.add(Component.literal("§7✎ Intelligence: §b+" + (intelligenceFor(slot) + 50)));
        lore.add(Component.literal("§7❤ Health: §c+20"));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§7Grants §d+10 affinity §7to §oevery§r §7school."));
        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, "archmage_" + pieceName(slot));
        return s;
    }

    // ── affinity lookup (called by SchoolTreeManager) ──────────────────────

    /** Total school affinity from worn robes / archmage set for this player. */
    public static int getEquippedRobeAffinity(ServerPlayer player, MagicSchool school) {
        if (player == null || school == null) return 0;
        int affinity = 0;
        int matching = 0;
        int archmage = 0;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            String id = SlayerItems.getCustomItemId(player.getItemBySlot(slot));
            if (id == null) continue;
            id = SlayerItems.stripSkyblockEnchantSuffix(id);
            if (id.equals(robeId(school, slot))) {
                affinity += AFFINITY_PER_PIECE;
                matching++;
            } else if (id.startsWith("archmage_")) {
                archmage++;
            }
        }
        if (matching >= 4) affinity += FULL_SET_AFFINITY;
        if (archmage > 0) affinity += archmage * 10;

        // Off-hand catalyst of the same school.
        String off = SlayerItems.getCustomItemId(player.getOffhandItem());
        if (off != null && SlayerItems.stripSkyblockEnchantSuffix(off).equals("catalyst_" + school.prefix)) {
            affinity += 15;
        }
        return affinity;
    }

    /** All magic gear stacks, for catalogue GUIs. */
    public static List<ItemStack> allStacks() {
        List<ItemStack> out = new ArrayList<>();
        for (MagicSchool school : MagicSchool.values()) {
            for (EquipmentSlot slot : ARMOR_SLOTS) {
                out.add(createRobePiece(school, slot));
            }
            out.add(createCatalyst(school));
            for (int tier : GRIMOIRE_TIERS) {
                out.add(createGrimoire(school, tier));
            }
        }
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            out.add(createArchmagePiece(slot));
        }
        out.add(createAwakeningCatalyst());
        for (Technique t : Technique.values()) out.add(createTechniqueRelic(t));
        return out;
    }
}
