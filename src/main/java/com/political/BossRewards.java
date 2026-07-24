package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

/**
 * V6.4–V6.5 — the loot side of the Special Grade system.
 *
 * <p>Holds three families of dressed vanilla items:
 * <ul>
 *   <li>The <b>Cursed Womb</b> — the craftable ritual item that summons a chosen boss. One Womb
 *       plus the boss's coin cost begins a fight (consumed in {@link SpecialGradeBossManager}).</li>
 *   <li>A unique <b>trophy</b> per boss — proof of the kill, small stat lines.</li>
 *   <li>A unique <b>signature gear</b> piece per boss — boss-only drops with strong stat lines
 *       (every value a multiple of 5, read by {@link PlayerStatsManager} straight from lore).</li>
 * </ul>
 *
 * <p>Only the Cursed Womb is craftable. Trophies and gear are drop-only — documented in the
 * manifest — so they cannot be farmed off the crafting bench.
 */
public final class BossRewards {

    private BossRewards() {}

    public static final String CURSED_WOMB_ID = "cursed_womb";
    private static final String RECIPE_PREFIX = "V6 ";

    // ════════════════════════════════════════════════════════════
    // CURSED WOMB (craftable ritual item)
    // ════════════════════════════════════════════════════════════

    public static ItemStack createCursedWomb(int count) {
        ItemStack s = new ItemStack(Items.HEART_OF_THE_SEA, count);
        s.set(DataComponents.CUSTOM_NAME, Component.literal("✦ Cursed Womb")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§8Special Grade Ritual"));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§7A knot of concentrated malevolence."));
        lore.add(Component.literal("§7Offer it through the Special Grade menu"));
        lore.add(Component.literal("§7to drag a named curse into the world."));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§7Consumed on summon, alongside the"));
        lore.add(Component.literal("§7chosen boss's coin cost."));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§c§lDO NOT open it lightly."));
        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, CURSED_WOMB_ID);
        return s;
    }

    public static boolean isCursedWomb(ItemStack stack) {
        return stack != null && !stack.isEmpty()
                && CURSED_WOMB_ID.equals(SlayerItems.getCustomItemId(stack));
    }

    /** Consume exactly one Cursed Womb from the player's inventory. Returns false if none found. */
    public static boolean consumeOneWomb(net.minecraft.server.level.ServerPlayer player) {
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack st = inv.getItem(i);
            if (isCursedWomb(st)) {
                st.shrink(1);
                return true;
            }
        }
        return false;
    }

    public static int countWombs(net.minecraft.server.level.ServerPlayer player) {
        int n = 0;
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (isCursedWomb(inv.getItem(i))) n += inv.getItem(i).getCount();
        }
        return n;
    }

    // ════════════════════════════════════════════════════════════
    // TROPHIES (drop-only)
    // ════════════════════════════════════════════════════════════

    public static ItemStack createTrophy(SpecialGradeBoss boss, int count) {
        ItemStack s = new ItemStack(trophyBase(boss), count);
        s.set(DataComponents.CUSTOM_NAME, Component.literal("✦ Trophy: " + boss.title)
                .withStyle(boss.color, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§8Special Grade Trophy"));
        lore.add(Component.literal(""));
        for (String stat : trophyStats(boss)) lore.add(Component.literal(stat));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§7Proof that a " + boss.mechanic.label.toLowerCase()));
        lore.add(Component.literal("§7curse was put down for good."));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Dropped by " + boss.title + "."));
        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, boss.trophyId());
        return s;
    }

    private static net.minecraft.world.item.Item trophyBase(SpecialGradeBoss boss) {
        return switch (boss) {
            case RADIAS -> Items.SLIME_BALL;
            case KAGE -> Items.ENDER_EYE;
            case BROOD_SOVEREIGN -> Items.SPIDER_EYE;
            case MALEVOLENCE -> Items.BLAZE_POWDER;
            case MIRRORED_FIST -> Items.GOLD_INGOT;
            case SUKORO -> Items.WITHER_SKELETON_SKULL;
        };
    }

    /** Small trophy stat lines. Values are multiples of 5. */
    private static String[] trophyStats(SpecialGradeBoss boss) {
        return switch (boss) {
            case RADIAS -> new String[]{"§7❤ Health: §c+40", "§7❈ Defense: §a+15"};
            case KAGE -> new String[]{"§7✦ Speed: §f+10", "§7☣ Crit Chance: §9+5%"};
            case BROOD_SOVEREIGN -> new String[]{"§7✎ Intelligence: §b+40", "§7❤ Health: §c+20"};
            case MALEVOLENCE -> new String[]{"§7❁ Strength: §c+20", "§7☠ Crit Damage: §9+15%"};
            case MIRRORED_FIST -> new String[]{"§7⫽ Ferocity: §c+10", "§7❈ Defense: §a+20"};
            case SUKORO -> new String[]{"§7❁ Strength: §c+25", "§7☠ Crit Damage: §9+20%"};
        };
    }

    // ════════════════════════════════════════════════════════════
    // SIGNATURE GEAR (drop-only, strong)
    // ════════════════════════════════════════════════════════════

    public static ItemStack createGear(SpecialGradeBoss boss, int count) {
        ItemStack s = new ItemStack(gearBase(boss), count);
        s.set(DataComponents.CUSTOM_NAME, Component.literal("✦ " + gearName(boss))
                .withStyle(boss.color, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§8Special Grade Relic"));
        lore.add(Component.literal(""));
        for (String stat : gearStats(boss)) lore.add(Component.literal(stat));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§7Torn from the curse of " + boss.title + "."));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Only drops from a slain Special Grade."));
        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, boss.gearId());
        return s;
    }

    public static String gearName(SpecialGradeBoss boss) {
        return switch (boss) {
            case RADIAS -> "Evermending Aegis";
            case KAGE -> "Hollow Blink Edge";
            case BROOD_SOVEREIGN -> "Broodcaller's Crown";
            case MALEVOLENCE -> "Shrine-Warden Greaves";
            case MIRRORED_FIST -> "Reversal Treads";
            case SUKORO -> "Twice-Born Blade";
        };
    }

    private static net.minecraft.world.item.Item gearBase(SpecialGradeBoss boss) {
        return switch (boss) {
            case RADIAS -> Items.NETHERITE_CHESTPLATE;
            case KAGE -> Items.NETHERITE_SWORD;
            case BROOD_SOVEREIGN -> Items.NETHERITE_HELMET;
            case MALEVOLENCE -> Items.NETHERITE_LEGGINGS;
            case MIRRORED_FIST -> Items.NETHERITE_BOOTS;
            case SUKORO -> Items.NETHERITE_AXE;
        };
    }

    /** Strong signature stat lines. Every value a multiple of 5. */
    private static String[] gearStats(SpecialGradeBoss boss) {
        return switch (boss) {
            case RADIAS -> new String[]{
                    "§7❈ Defense: §a+120", "§7❤ Health: §c+150", "§7❁ Strength: §c+25"};
            case KAGE -> new String[]{
                    "§7⚔ Damage: §c+90", "§7✦ Speed: §f+15", "§7☣ Crit Chance: §9+15%"};
            case BROOD_SOVEREIGN -> new String[]{
                    "§7✎ Intelligence: §b+150", "§7❤ Health: §c+75", "§7⫽ Ferocity: §c+10"};
            case MALEVOLENCE -> new String[]{
                    "§7❈ Defense: §a+90", "§7❤ Health: §c+100", "§7☠ Crit Damage: §9+40%"};
            case MIRRORED_FIST -> new String[]{
                    "§7⫽ Ferocity: §c+20", "§7❈ Defense: §a+60", "§7✦ Speed: §f+5"};
            case SUKORO -> new String[]{
                    "§7⚔ Damage: §c+120", "§7❁ Strength: §c+50", "§7☠ Crit Damage: §9+60%"};
        };
    }

    // ════════════════════════════════════════════════════════════
    // RECIPE (Cursed Womb only)
    // ════════════════════════════════════════════════════════════

    /** Idempotent — safe to call once per boot. Lands in /recipe AND /setrecipe. */
    public static void ensureRecipes() {
        List<SlayerRecipes.Recipe> out = new ArrayList<>();

        // Expensive, gated ritual craft (comparable to a mid Expedition entry sink):
        //   [CO] [SK] [CO]
        //   [ES] [NS] [ES]
        //   [CO] [SK] [CO]
        // CO = Crying Obsidian, SK = Wither Skeleton Skull, ES = Echo Shard, NS = Nether Star.
        ItemStack[] grid = new ItemStack[]{
                new ItemStack(Items.CRYING_OBSIDIAN),      new ItemStack(Items.WITHER_SKELETON_SKULL), new ItemStack(Items.CRYING_OBSIDIAN),
                new ItemStack(Items.ECHO_SHARD),           new ItemStack(Items.NETHER_STAR),           new ItemStack(Items.ECHO_SHARD),
                new ItemStack(Items.CRYING_OBSIDIAN),      new ItemStack(Items.WITHER_SKELETON_SKULL), new ItemStack(Items.CRYING_OBSIDIAN)
        };
        boolean[] locked = new boolean[9];
        for (int i = 0; i < 9; i++) locked[i] = true;

        out.add(new SlayerRecipes.Recipe(RECIPE_PREFIX + "Cursed Womb",
                createCursedWomb(1), grid, locked, null, 0));

        try {
            RecipeConfigManager.upsertCustomRecipesSilent(out);
            System.out.println("[BossRewards] Registered Cursed Womb recipe.");
        } catch (Throwable t) {
            System.err.println("[BossRewards] Failed to register recipe: " + t.getMessage());
        }
    }
}
