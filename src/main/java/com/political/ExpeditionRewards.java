package com.political;


import net.minecraft.util.Prediction;
import com.political.bounty.SlayerItems;
import com.political.economy.CoinManager;
import com.political.recipe.RecipeConfigManager;
import com.political.recipe.SlayerRecipes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * V5 Pillar 1 — every reward an Expedition can pay out.
 *
 * <p>Design intent: coins are the <i>least</i> interesting reward and their value per unit
 * of difficulty falls as tiers rise, so Expeditions can never replace the settlement
 * economy as an income engine. The real payout is gear, Arcane Shards (magic tie-in),
 * settlement treasury (settlement tie-in) and Expedition Keys.
 *
 * <p>Acquisition paths:
 * <ul>
 *   <li><b>Expedition Sigil</b> — CRAFTABLE (recipe registered by {@link #bootstrapRecipes()}).</li>
 *   <li><b>Expedition Key</b> — NOT craftable. Drops on run completion, chance scales with tier
 *       (T1 never, T5 often). Also a rare vault-chest drop from T3+.</li>
 *   <li><b>Delver's Blade / Vaultwarden Plate / Abyssal Circlet</b> — NOT craftable. Rolled on
 *       run completion only; roll chance and stat tier both scale with expedition tier.</li>
 * </ul>
 */
public final class ExpeditionRewards {

    private ExpeditionRewards() {}

    // ════════════════════════════════════════════════════════════
    // COIN / SHARD TABLES  (index 0 unused; tiers are 1..5)
    // ════════════════════════════════════════════════════════════

    /**
     * Completion coins. Compare with the existing sinks: settlement registration 1k–1M,
     * party creation 10k, war declaration 10M. Top-tier Expeditions pay 60k for a ~45 minute
     * run behind a 90 minute cooldown and a MEGALOPOLIS gate — meaningful, never dominant.
     * Note the deliberate diminishing return: T5 is ~12x the difficulty of T1 for 20x the
     * coins across ~4x the run length, so coins-per-minute is roughly flat and then falls.
     */
    private static final long[] COINS = {0L, 3_000L, 8_000L, 18_000L, 35_000L, 60_000L};

    /** Entry cost, deducted at launch. A real sink that also stops re-roll spam. */
    private static final long[] ENTRY_COST = {0L, 500L, 2_000L, 5_000L, 12_000L, 25_000L};

    /** Arcane Shards (SchoolTreeManager currency) — the magic-system tie-in. */
    private static final int[] SHARDS = {0, 1, 2, 4, 7, 12};

    /** Fraction of the coin reward additionally credited to the launching settlement. */
    public static final double SETTLEMENT_TREASURY_SHARE = 0.25;

    /** Chance of a gear roll on completion. */
    private static final double[] GEAR_CHANCE = {0.0, 0.20, 0.30, 0.45, 0.60, 0.80};

    /** Chance of an Expedition Key on completion. */
    private static final double[] KEY_CHANCE = {0.0, 0.00, 0.05, 0.12, 0.25, 0.40};

    public static long coinReward(int tier) {
        return COINS[clamp(tier)];
    }

    public static long entryCost(int tier) {
        return ENTRY_COST[clamp(tier)];
    }

    public static int shardReward(int tier) {
        return SHARDS[clamp(tier)];
    }

    public static double gearChance(int tier) {
        return GEAR_CHANCE[clamp(tier)];
    }

    public static double keyChance(int tier) {
        return KEY_CHANCE[clamp(tier)];
    }

    private static int clamp(int tier) {
        return Math.max(1, Math.min(5, tier));
    }

    // ════════════════════════════════════════════════════════════
    // GEAR  (every stat value is a multiple of 5)
    // ════════════════════════════════════════════════════════════

    // index = tier (1..5)
    private static final int[] BLADE_DAMAGE = {0, 40, 60, 85, 115, 150};
    private static final int[] BLADE_STRENGTH = {0, 25, 40, 60, 85, 115};
    private static final int[] BLADE_CRIT_DAMAGE = {0, 20, 30, 45, 60, 80};
    private static final int[] BLADE_FEROCITY = {0, 0, 0, 5, 5, 10};

    private static final int[] PLATE_DEFENSE = {0, 45, 70, 100, 140, 190};
    private static final int[] PLATE_HEALTH = {0, 50, 80, 120, 170, 230};

    private static final int[] CIRCLET_INT = {0, 75, 125, 190, 265, 350};
    private static final int[] CIRCLET_DEFENSE = {0, 20, 30, 45, 60, 80};
    private static final int[] CIRCLET_SPEED = {0, 5, 5, 10, 10, 15};

    private static final String[] TIER_ROMAN = {"", "I", "II", "III", "IV", "V"};

    public static ItemStack createDelversBlade(int tier) {
        int t = clamp(tier);
        ItemStack s = new ItemStack(Items.NETHERITE_SWORD);
        s.set(DataComponents.CUSTOM_NAME, Component.literal("✦ Delver's Blade " + TIER_ROMAN[t])
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§8Expedition Spoils"));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§7⚔ Damage: §c+" + BLADE_DAMAGE[t]));
        lore.add(Component.literal("§7❁ Strength: §c+" + BLADE_STRENGTH[t]));
        lore.add(Component.literal("§7☠ Crit Damage: §9+" + BLADE_CRIT_DAMAGE[t] + "%"));
        if (BLADE_FEROCITY[t] > 0) {
            lore.add(Component.literal("§7⫽ Ferocity: §c+" + BLADE_FEROCITY[t]));
        }
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Cut from the dark of a collapsed floor."));
        lore.add(Component.literal("§8Only found. Never forged."));
        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, "expedition_blade_t" + t);
        return s;
    }

    public static ItemStack createVaultwardenPlate(int tier) {
        int t = clamp(tier);
        ItemStack s = new ItemStack(Items.NETHERITE_CHESTPLATE);
        s.set(DataComponents.CUSTOM_NAME, Component.literal("✦ Vaultwarden Plate " + TIER_ROMAN[t])
                .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§8Expedition Spoils"));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§7❈ Defense: §a+" + PLATE_DEFENSE[t]));
        lore.add(Component.literal("§7❤ Health: §c+" + PLATE_HEALTH[t]));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Stripped from something that guarded"));
        lore.add(Component.literal("§8a vault for longer than it was alive."));
        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, "expedition_plate_t" + t);
        return s;
    }

    public static ItemStack createAbyssalCirclet(int tier) {
        int t = clamp(tier);
        ItemStack s = new ItemStack(Items.GOLDEN_HELMET);
        s.set(DataComponents.CUSTOM_NAME, Component.literal("✦ Abyssal Circlet " + TIER_ROMAN[t])
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§8Expedition Spoils"));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§7✎ Intelligence: §b+" + CIRCLET_INT[t]));
        lore.add(Component.literal("§7❈ Defense: §a+" + CIRCLET_DEFENSE[t]));
        lore.add(Component.literal("§7✦ Speed: §f+" + CIRCLET_SPEED[t]));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8The deeper floors hum. This listens back."));
        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, "expedition_circlet_t" + t);
        return s;
    }

    /**
     * Expedition Key — NOT craftable. Consumed at launch to bypass the personal cooldown
     * exactly once. Keys are the only way to run back-to-back, and they are strictly
     * drop-gated, which is what keeps the per-hour reward ceiling bounded.
     */
    public static ItemStack createExpeditionKey(int amount) {
        ItemStack s = new ItemStack(Items.TRIPWIRE_HOOK, Math.max(1, amount));
        s.set(DataComponents.CUSTOM_NAME, Component.literal("✦ Expedition Key")
                .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§8Consumable"));
        lore.add(Component.literal("§7Consumed on launch to §eignore your"));
        lore.add(Component.literal("§eexpedition cooldown §7once."));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Dropped on completion. Cannot be crafted."));
        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, "expedition_key");
        return s;
    }

    /**
     * Expedition Sigil — CRAFTABLE. Consumed at launch for one extra completion loot roll.
     * A pure gamble sink: it costs materials whether or not the extra roll pays out.
     */
    public static ItemStack createExpeditionSigil(int amount) {
        ItemStack s = new ItemStack(Items.HEART_OF_THE_SEA, Math.max(1, amount));
        s.set(DataComponents.CUSTOM_NAME, Component.literal("✦ Expedition Sigil")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§8Consumable"));
        lore.add(Component.literal("§7Consumed on launch for §b+1 loot roll"));
        lore.add(Component.literal("§7on completion. Lost if the run fails."));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Craftable."));
        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, "expedition_sigil");
        return s;
    }

    // ════════════════════════════════════════════════════════════
    // RECIPES
    // ════════════════════════════════════════════════════════════

    /**
     * Registers every craftable Expedition item into /recipe and /setrecipe.
     * Recipe names are prefixed "V5 " so nothing existing is ever overwritten.
     */
    public static void bootstrapRecipes() {
        List<SlayerRecipes.Recipe> out = new ArrayList<>();

        // Expedition Sigil:
        //   P O P
        //   O D O
        //   P O P
        ItemStack[] grid = new ItemStack[9];
        for (int i = 0; i < 9; i++) grid[i] = ItemStack.EMPTY;
        ItemStack pearl = new ItemStack(Items.ENDER_PEARL);
        ItemStack obsidian = new ItemStack(Items.OBSIDIAN);
        grid[0] = pearl.copy();
        grid[1] = obsidian.copy();
        grid[2] = pearl.copy();
        grid[3] = obsidian.copy();
        grid[4] = new ItemStack(Items.DIAMOND_BLOCK);
        grid[5] = obsidian.copy();
        grid[6] = pearl.copy();
        grid[7] = obsidian.copy();
        grid[8] = pearl.copy();

        boolean[] locked = new boolean[9];
        for (int i = 0; i < 9; i++) locked[i] = true;

        out.add(new SlayerRecipes.Recipe("V5 Expedition Sigil",
                createExpeditionSigil(1), grid, locked, null, 0));

        RecipeConfigManager.upsertCustomRecipesSilent(out);
    }

    // ════════════════════════════════════════════════════════════
    // PAYOUT
    // ════════════════════════════════════════════════════════════

    /** Fill a vault chest once its guards are dead. Never called before the room is cleared. */
    public static void fillVaultChest(ServerLevel level, BlockPos pos, int tier, Random rng) {
        if (!(level.getBlockEntity(pos) instanceof ChestBlockEntity chest)) return;
        int t = clamp(tier);

        int slot = 0;
        chest.setItem(slot++, new ItemStack(Items.DIAMOND, 2 + rng.nextInt(3 * t)));
        chest.setItem(slot++, new ItemStack(Items.GOLD_INGOT, 4 + rng.nextInt(6 * t)));
        chest.setItem(slot++, new ItemStack(Items.LAPIS_LAZULI, 6 + rng.nextInt(8 * t)));

        if (t >= 2 && rng.nextFloat() < 0.45f) {
            chest.setItem(slot++, new ItemStack(Items.NETHERITE_SCRAP, 1 + rng.nextInt(t)));
        }
        if (t >= 3 && rng.nextFloat() < 0.30f) {
            chest.setItem(slot++, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 1));
        }
        // Rare bonus key from deep vaults only.
        if (t >= 3 && rng.nextFloat() < 0.10f * (t - 2)) {
            chest.setItem(slot++, createExpeditionKey(1));
        }
        chest.setChanged();
    }

    /** Roll one piece of gear for a completed run, or null if the roll missed. */
    public static ItemStack rollGear(int tier, Random rng) {
        if (rng.nextDouble() >= gearChance(tier)) return null;
        int pick = rng.nextInt(3);
        return switch (pick) {
            case 0 -> createDelversBlade(tier);
            case 1 -> createVaultwardenPlate(tier);
            default -> createAbyssalCirclet(tier);
        };
    }

    /**
     * Grant the full completion payout to one member. Idempotent per run is enforced by the
     * caller ({@code dungeon.lootRolled}), never here.
     */
    public static void grantCompletion(ServerPlayer player, int tier, boolean sigilBonus, Random rng) {
        int t = clamp(tier);

        long coins = coinReward(t);
        CoinManager.giveCoinsQuiet(player, coins);
        player.sendSystemMessage(Component.literal("§6+" + String.format("%,d", coins) + " coins")
                .withStyle(ChatFormatting.GOLD));

        int shards = shardReward(t);
        if (shards > 0) {
            SchoolTreeManager.addShards(player, shards);
        }

        int rolls = 1 + (sigilBonus ? 1 : 0);
        boolean gotAnything = false;
        for (int i = 0; i < rolls; i++) {
            ItemStack gear = rollGear(t, rng);
            if (gear != null) {
                giveOrDrop(player, gear);
                gotAnything = true;
                player.sendSystemMessage(Component.literal("§d✦ Spoils: ")
                        .append(gear.getHoverName()));
            }
        }
        if (!gotAnything) {
            player.sendSystemMessage(Component.literal("§8No gear surfaced from this descent.")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }

        if (rng.nextDouble() < keyChance(t)) {
            giveOrDrop(player, createExpeditionKey(1));
            player.sendSystemMessage(Component.literal("§e✦ An Expedition Key rattles loose.")
                    .withStyle(ChatFormatting.YELLOW));
        }
    }

    private static void giveOrDrop(ServerPlayer player, ItemStack stack) {
        if (!player.addItem(stack)) {
            player.drop(stack, false, Prediction.SERVER_ONLY);
        }
    }

    /** Public wrapper used by {@link ExpeditionRunManager} to hand back a consumed item. */
    public static void giveOrDropPublic(ServerPlayer player, ItemStack stack) {
        giveOrDrop(player, stack);
    }

    // ════════════════════════════════════════════════════════════
    // INVENTORY HELPERS (Keys / Sigils)
    // ════════════════════════════════════════════════════════════

    public static int countById(ServerPlayer player, String customId) {
        int total = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack st = player.getInventory().getItem(i);
            if (st == null || st.isEmpty()) continue;
            String id = SlayerItems.getCustomItemId(st);
            if (id == null) continue;
            if (customId.equals(SlayerItems.stripSkyblockEnchantSuffix(id))) {
                total += st.getCount();
            }
        }
        return total;
    }

    /** Remove exactly one item with the given custom id. Returns false if none was found. */
    public static boolean consumeOne(ServerPlayer player, String customId) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack st = player.getInventory().getItem(i);
            if (st == null || st.isEmpty()) continue;
            String id = SlayerItems.getCustomItemId(st);
            if (id == null) continue;
            if (customId.equals(SlayerItems.stripSkyblockEnchantSuffix(id))) {
                st.shrink(1);
                if (st.isEmpty()) {
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                }
                return true;
            }
        }
        return false;
    }

    public static int countKeys(ServerPlayer player) {
        return countById(player, "expedition_key");
    }

    public static int countSigils(ServerPlayer player) {
        return countById(player, "expedition_sigil");
    }
}
