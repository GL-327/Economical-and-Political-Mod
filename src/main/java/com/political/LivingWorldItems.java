package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

/**
 * Every dressed vanilla stack the Living World pillar adds.
 *
 * <h2>Two classes of item</h2>
 * <ul>
 *   <li><b>Trophies</b> — dropped only by a Great Hunt kill. There is no recipe that creates
 *       one, so the only way into the pool is winning a hunt. They are held tokens, so under
 *       {@code PlayerStatsManager} they contribute offensive stats only.</li>
 *   <li><b>Tools</b> — the Codex, Almanac and Horn. These are craftable from ordinary
 *       materials because they carry <i>no stats at all</i>; they exist purely so the three
 *       GUIs are reachable without typing a command.</li>
 * </ul>
 *
 * <p>The one crafted item that does carry stats — the <b>Grand Trophy Crest</b> — consumes
 * one of each of the four trophies, i.e. four separate won hunts. That keeps it gated behind
 * real world events rather than behind a material cost anyone can grind.</p>
 *
 * <p>All stat values are multiples of 5 (or single digits under 10) as required by
 * {@code PlayerStatsManager}'s lore parser.</p>
 */
public final class LivingWorldItems {

    private LivingWorldItems() {}

    // ── Custom item ids ─────────────────────────────────────────────────────

    public static final String ID_TROPHY_FANG = "lw_trophy_gnawing_fang";
    public static final String ID_TROPHY_CROWN = "lw_trophy_hollow_crown";
    public static final String ID_TROPHY_ASHEN = "lw_trophy_ashen_heart";
    public static final String ID_TROPHY_DEPTHS = "lw_trophy_silent_depths";
    public static final String ID_GRAND_TROPHY = "lw_grand_trophy_crest";

    public static final String ID_CODEX = "lw_bestiary_codex";
    public static final String ID_ALMANAC = "lw_seasons_almanac";
    public static final String ID_HORN = "lw_hunters_horn";

    /** Every trophy id, in the order the Grand Trophy recipe expects them. */
    public static final String[] TROPHY_IDS = {
            ID_TROPHY_FANG, ID_TROPHY_CROWN, ID_TROPHY_ASHEN, ID_TROPHY_DEPTHS
    };

    // ── Trophies ────────────────────────────────────────────────────────────

    public static ItemStack createGnawingFang(int count) {
        ItemStack s = new ItemStack(Items.BONE, count);
        s.set(DataComponents.CUSTOM_NAME, Component.literal("✦ Trophy of the Gnawing Fang")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§8Great Hunt Trophy"));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§7❁ Strength: §c+25"));
        lore.add(Component.literal("§7⫽ Ferocity: §c+5"));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§7Torn from something that did not"));
        lore.add(Component.literal("§7intend to stop chewing."));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Awarded for felling a Great Hunt."));
        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, ID_TROPHY_FANG);
        return s;
    }

    public static ItemStack createHollowCrown(int count) {
        ItemStack s = new ItemStack(Items.GOLD_INGOT, count);
        s.set(DataComponents.CUSTOM_NAME, Component.literal("✦ Trophy of the Hollow Crown")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§8Great Hunt Trophy"));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§7❁ Strength: §c+15"));
        lore.add(Component.literal("§7☠ Crit Damage: §c+15"));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§7Melted down from a circlet that"));
        lore.add(Component.literal("§7no longer had a head to sit on."));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Awarded for felling a Great Hunt."));
        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, ID_TROPHY_CROWN);
        return s;
    }

    public static ItemStack createAshenHeart(int count) {
        ItemStack s = new ItemStack(Items.MAGMA_CREAM, count);
        s.set(DataComponents.CUSTOM_NAME, Component.literal("✦ Trophy of the Ashen Heart")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§8Great Hunt Trophy"));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§7☠ Crit Damage: §c+20"));
        lore.add(Component.literal("§7☣ Crit Chance: §c+5%"));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§7Still warm. It has been still"));
        lore.add(Component.literal("§7warm for a very long time."));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Awarded for felling a Great Hunt."));
        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, ID_TROPHY_ASHEN);
        return s;
    }

    public static ItemStack createSilentDepths(int count) {
        ItemStack s = new ItemStack(Items.HEART_OF_THE_SEA, count);
        s.set(DataComponents.CUSTOM_NAME, Component.literal("✦ Trophy of the Silent Depths")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§8Great Hunt Trophy"));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§7✎ Intelligence: §b+50"));
        lore.add(Component.literal("§7⫽ Ferocity: §c+5"));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§7Pulled from water that had not"));
        lore.add(Component.literal("§7been disturbed in centuries."));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Awarded for felling a Great Hunt."));
        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, ID_TROPHY_DEPTHS);
        return s;
    }

    /** The crafted payoff: one of each trophy, i.e. four separate won hunts. */
    public static ItemStack createGrandTrophyCrest() {
        ItemStack s = new ItemStack(Items.NETHERITE_HELMET);
        s.set(DataComponents.CUSTOM_NAME, Component.literal("✦ Grand Trophy Crest")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§8Master of the Great Hunt"));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§7❁ Strength: §c+30"));
        lore.add(Component.literal("§7❈ Defense: §a+40"));
        lore.add(Component.literal("§7❤ Health: §c+50"));
        lore.add(Component.literal("§7☠ Crit Damage: §c+25"));
        lore.add(Component.literal("§7✎ Intelligence: §b+50"));
        lore.add(Component.literal("§7✦ Speed: §f+5"));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§7Four hunts, four trophies, one crest."));
        lore.add(Component.literal("§7Everyone who sees it knows the count."));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§5§lMYTHIC HELMET"));
        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, ID_GRAND_TROPHY);
        return s;
    }

    // ── GUI keys (no stats — these only open screens) ───────────────────────

    public static ItemStack createBestiaryCodex() {
        ItemStack s = new ItemStack(Items.BOOK);
        s.set(DataComponents.CUSTOM_NAME, Component.literal("✦ Bestiary Codex")
                .withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§7Everything you have killed, counted"));
        lore.add(Component.literal("§7and filed by family."));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§eRight-click §7to open the Bestiary."));
        lore.add(Component.literal("§8Also available via §7/bestiary§8."));
        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, ID_CODEX);
        return s;
    }

    public static ItemStack createSeasonsAlmanac() {
        ItemStack s = new ItemStack(Items.WRITABLE_BOOK);
        s.set(DataComponents.CUSTOM_NAME, Component.literal("✦ Almanac of Seasons")
                .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§7The turning of the year, and what"));
        lore.add(Component.literal("§7each turn does to your fields."));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§eRight-click §7to open the Almanac."));
        lore.add(Component.literal("§8Also available via §7/season§8."));
        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, ID_ALMANAC);
        return s;
    }

    public static ItemStack createHuntersHorn() {
        ItemStack s = new ItemStack(Items.GOAT_HORN);
        s.set(DataComponents.CUSTOM_NAME, Component.literal("✦ Hunter's Horn")
                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§7Sounds when a Great Hunt walks."));
        lore.add(Component.literal("§7Tells you roughly where."));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§eRight-click §7to open the Hunt board."));
        lore.add(Component.literal("§8Also available via §7/hunt§8."));
        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, ID_HORN);
        return s;
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    /** Build a trophy stack by id. Returns {@link ItemStack#EMPTY} for an unknown id. */
    public static ItemStack createTrophy(String id, int count) {
        if (id == null) return ItemStack.EMPTY;
        return switch (id) {
            case ID_TROPHY_FANG -> createGnawingFang(count);
            case ID_TROPHY_CROWN -> createHollowCrown(count);
            case ID_TROPHY_ASHEN -> createAshenHeart(count);
            case ID_TROPHY_DEPTHS -> createSilentDepths(count);
            default -> ItemStack.EMPTY;
        };
    }

    public static String trophyDisplayName(String id) {
        if (id == null) return "Trophy";
        return switch (id) {
            case ID_TROPHY_FANG -> "Trophy of the Gnawing Fang";
            case ID_TROPHY_CROWN -> "Trophy of the Hollow Crown";
            case ID_TROPHY_ASHEN -> "Trophy of the Ashen Heart";
            case ID_TROPHY_DEPTHS -> "Trophy of the Silent Depths";
            default -> "Trophy";
        };
    }

    /** Put a stack in the player's inventory, dropping it at their feet if there is no room. */
    public static void give(ServerPlayer player, ItemStack stack) {
        if (player == null || stack == null || stack.isEmpty()) return;
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}
