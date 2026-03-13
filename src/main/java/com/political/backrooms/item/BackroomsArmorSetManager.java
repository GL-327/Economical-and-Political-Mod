package com.political.backrooms.item;

import com.political.backrooms.BackroomsItems;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * Manages Backrooms armor set bonuses.
 * Called every 20 ticks from the server tick event.
 * Detects how many pieces of each set the player wears and applies effects.
 */
public class BackroomsArmorSetManager {

    /** Identifiers for each armor set. Each has 4 pieces: head, chest, legs, boots. */
    public enum ArmorSet {
        CRAWLER(new Item[0], new Item[0], new Item[0], new Item[0]),
        HOUND(new Item[0], new Item[0], new Item[0], new Item[0]),
        FACELING(new Item[0], new Item[0], new Item[0], new Item[0]),
        VOID_SET(new Item[0], new Item[0], new Item[0], new Item[0]),
        SKULK(new Item[0], new Item[0], new Item[0], new Item[0]),
        INDUSTRIAL(new Item[0], new Item[0], new Item[0], new Item[0]),
        POOLROOMS(new Item[0], new Item[0], new Item[0], new Item[0]),
        WRETCH(new Item[0], new Item[0], new Item[0], new Item[0]),
        NULL_SET(new Item[0], new Item[0], new Item[0], new Item[0]),
        TEMPORAL(new Item[0], new Item[0], new Item[0], new Item[0]),
        ASCENDANT(new Item[0], new Item[0], new Item[0], new Item[0]),
        REALITY_WEAVER(new Item[0], new Item[0], new Item[0], new Item[0]);

        // Placeholder arrays; resolved lazily at first tick since items register after this enum init
        private Item[] headItems;
        private Item[] chestItems;
        private Item[] legItems;
        private Item[] bootItems;

        ArmorSet(Item[] h, Item[] c, Item[] l, Item[] b) {
            headItems = h; chestItems = c; legItems = l; bootItems = b;
        }
    }

    private static boolean initialized = false;

    /** Lazy initialization after all items are registered. */
    public static void ensureInitialized() {
        if (initialized) return;
        initialized = true;

        ArmorSet.CRAWLER.headItems   = new Item[]{ BackroomsItems.CRAWLER_HELMET };
        ArmorSet.CRAWLER.chestItems  = new Item[]{ BackroomsItems.CRAWLER_CHESTPLATE };
        ArmorSet.CRAWLER.legItems    = new Item[]{ BackroomsItems.CRAWLER_LEGGINGS };
        ArmorSet.CRAWLER.bootItems   = new Item[]{ BackroomsItems.CRAWLER_BOOTS };

        ArmorSet.HOUND.headItems   = new Item[]{ BackroomsItems.HOUND_HELMET };
        ArmorSet.HOUND.chestItems  = new Item[]{ BackroomsItems.HOUND_CHESTPLATE };
        ArmorSet.HOUND.legItems    = new Item[]{ BackroomsItems.HOUND_LEGGINGS };
        ArmorSet.HOUND.bootItems   = new Item[]{ BackroomsItems.HOUND_BOOTS };

        ArmorSet.FACELING.headItems   = new Item[]{ BackroomsItems.FACELING_HELMET };
        ArmorSet.FACELING.chestItems  = new Item[]{ BackroomsItems.FACELING_CHESTPLATE };
        ArmorSet.FACELING.legItems    = new Item[]{ BackroomsItems.FACELING_LEGGINGS };
        ArmorSet.FACELING.bootItems   = new Item[]{ BackroomsItems.FACELING_BOOTS };

        ArmorSet.VOID_SET.headItems   = new Item[]{ BackroomsItems.VOID_HELMET };
        ArmorSet.VOID_SET.chestItems  = new Item[]{ BackroomsItems.VOID_CHESTPLATE };
        ArmorSet.VOID_SET.legItems    = new Item[]{ BackroomsItems.VOID_LEGGINGS };
        ArmorSet.VOID_SET.bootItems   = new Item[]{ BackroomsItems.VOID_BOOTS };

        ArmorSet.SKULK.headItems   = new Item[]{ BackroomsItems.SKULK_HELMET };
        ArmorSet.SKULK.chestItems  = new Item[]{ BackroomsItems.SKULK_CHESTPLATE };
        ArmorSet.SKULK.legItems    = new Item[]{ BackroomsItems.SKULK_LEGGINGS };
        ArmorSet.SKULK.bootItems   = new Item[]{ BackroomsItems.SKULK_BOOTS };

        ArmorSet.INDUSTRIAL.headItems   = new Item[]{ BackroomsItems.INDUSTRIAL_HELMET };
        ArmorSet.INDUSTRIAL.chestItems  = new Item[]{ BackroomsItems.INDUSTRIAL_CHESTPLATE };
        ArmorSet.INDUSTRIAL.legItems    = new Item[]{ BackroomsItems.INDUSTRIAL_LEGGINGS };
        ArmorSet.INDUSTRIAL.bootItems   = new Item[]{ BackroomsItems.INDUSTRIAL_BOOTS };

        ArmorSet.POOLROOMS.headItems   = new Item[]{ BackroomsItems.POOLROOMS_HELMET };
        ArmorSet.POOLROOMS.chestItems  = new Item[]{ BackroomsItems.POOLROOMS_CHESTPLATE };
        ArmorSet.POOLROOMS.legItems    = new Item[]{ BackroomsItems.POOLROOMS_LEGGINGS };
        ArmorSet.POOLROOMS.bootItems   = new Item[]{ BackroomsItems.POOLROOMS_BOOTS };

        ArmorSet.WRETCH.headItems   = new Item[]{ BackroomsItems.WRETCH_HELMET };
        ArmorSet.WRETCH.chestItems  = new Item[]{ BackroomsItems.WRETCH_CHESTPLATE };
        ArmorSet.WRETCH.legItems    = new Item[]{ BackroomsItems.WRETCH_LEGGINGS };
        ArmorSet.WRETCH.bootItems   = new Item[]{ BackroomsItems.WRETCH_BOOTS };

        ArmorSet.NULL_SET.headItems   = new Item[]{ BackroomsItems.NULL_HELMET };
        ArmorSet.NULL_SET.chestItems  = new Item[]{ BackroomsItems.NULL_CHESTPLATE };
        ArmorSet.NULL_SET.legItems    = new Item[]{ BackroomsItems.NULL_LEGGINGS };
        ArmorSet.NULL_SET.bootItems   = new Item[]{ BackroomsItems.NULL_BOOTS };

        ArmorSet.TEMPORAL.headItems   = new Item[]{ BackroomsItems.TEMPORAL_HELMET };
        ArmorSet.TEMPORAL.chestItems  = new Item[]{ BackroomsItems.TEMPORAL_CHESTPLATE };
        ArmorSet.TEMPORAL.legItems    = new Item[]{ BackroomsItems.TEMPORAL_LEGGINGS };
        ArmorSet.TEMPORAL.bootItems   = new Item[]{ BackroomsItems.TEMPORAL_BOOTS };

        ArmorSet.ASCENDANT.headItems   = new Item[]{ BackroomsItems.ASCENDANT_HELMET };
        ArmorSet.ASCENDANT.chestItems  = new Item[]{ BackroomsItems.ASCENDANT_CHESTPLATE };
        ArmorSet.ASCENDANT.legItems    = new Item[]{ BackroomsItems.ASCENDANT_LEGGINGS };
        ArmorSet.ASCENDANT.bootItems   = new Item[]{ BackroomsItems.ASCENDANT_BOOTS };

        ArmorSet.REALITY_WEAVER.headItems   = new Item[]{ BackroomsItems.REALITY_WEAVER_HELMET };
        ArmorSet.REALITY_WEAVER.chestItems  = new Item[]{ BackroomsItems.REALITY_WEAVER_CHESTPLATE };
        ArmorSet.REALITY_WEAVER.legItems    = new Item[]{ BackroomsItems.REALITY_WEAVER_LEGGINGS };
        ArmorSet.REALITY_WEAVER.bootItems   = new Item[]{ BackroomsItems.REALITY_WEAVER_BOOTS };
    }

    /** Count how many pieces of the given set the player is wearing. */
    public static int countSetPieces(ServerPlayerEntity player, ArmorSet set) {
        int count = 0;
        Item head  = player.getEquippedStack(EquipmentSlot.HEAD).getItem();
        Item chest = player.getEquippedStack(EquipmentSlot.CHEST).getItem();
        Item legs  = player.getEquippedStack(EquipmentSlot.LEGS).getItem();
        Item feet  = player.getEquippedStack(EquipmentSlot.FEET).getItem();

        for (Item i : set.headItems)   if (head == i) { count++; break; }
        for (Item i : set.chestItems)  if (chest == i) { count++; break; }
        for (Item i : set.legItems)    if (legs == i) { count++; break; }
        for (Item i : set.bootItems)   if (feet == i) { count++; break; }
        return count;
    }

    /** Apply all set bonuses to a player. Called every 20 server ticks. */
    public static void tickPlayer(ServerPlayerEntity player, ServerWorld world) {
        ensureInitialized();
        for (ArmorSet set : ArmorSet.values()) {
            int pieces = countSetPieces(player, set);
            if (pieces >= 2) {
                applySetBonus(player, world, set, pieces);
            }
        }
    }

    private static void applySetBonus(ServerPlayerEntity player, ServerWorld world,
                                      ArmorSet set, int pieces) {
        switch (set) {
            // Crawler Set: 2pc = Resistance I, 4pc = Resistance I + Haste I
            case CRAWLER -> {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 40, 0, true, false));
                if (pieces >= 4)
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 40, 0, true, false));
            }
            // Hound Set: 2pc = Speed I, 4pc = Speed II
            case HOUND -> {
                int amp = pieces >= 4 ? 1 : 0;
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, amp, true, false));
            }
            // Faceling Set: 2pc = Strength I, 4pc = Strength II
            case FACELING -> {
                int amp = pieces >= 4 ? 1 : 0;
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 40, amp, true, false));
            }
            // Void Set: 2pc = Night Vision, 4pc = Night Vision + Absorption II
            case VOID_SET -> {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 240, 0, true, false));
                if (pieces >= 4)
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 40, 1, true, false));
            }
            // Skulk Set: 2pc = Haste I, 4pc = Haste II + Strength I
            case SKULK -> {
                int amp = pieces >= 4 ? 1 : 0;
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 40, amp, true, false));
                if (pieces >= 4)
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 40, 0, true, false));
            }
            // Industrial Set: 2pc = Resistance I, 4pc = Fire Resistance + Resistance I
            case INDUSTRIAL -> {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 40, 0, true, false));
                if (pieces >= 4)
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 40, 0, true, false));
            }
            // Poolrooms Set: 2pc = Water Breathing + Slow Falling, 4pc = + Dolphin's Grace
            case POOLROOMS -> {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 40, 0, true, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 40, 0, true, false));
                if (pieces >= 4)
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.DOLPHINS_GRACE, 40, 0, true, false));
            }
            // Wretch Set: 2pc = Weakness (debuff for balance), 4pc = Regeneration I
            case WRETCH -> {
                if (pieces >= 4)
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 40, 0, true, false));
            }
            // Null Set: 2pc = Resistance I, 4pc = Resistance II + Absorption II
            case NULL_SET -> {
                int amp = pieces >= 4 ? 1 : 0;
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 40, amp, true, false));
                if (pieces >= 4)
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 40, 1, true, false));
            }
            // Temporal Set: 2pc = Haste I + Speed I, 4pc = Haste II + Speed II
            case TEMPORAL -> {
                int amp = pieces >= 4 ? 1 : 0;
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 40, amp, true, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, amp, true, false));
            }
            // Ascendant Set: 2pc = Strength I + Resistance I, 4pc = Strength II + Resistance II + Regeneration
            case ASCENDANT -> {
                int amp = pieces >= 4 ? 1 : 0;
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 40, amp, true, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 40, amp, true, false));
                if (pieces >= 4)
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 40, 0, true, false));
            }
            // Reality Weaver Set: 2pc = Absorption + Resistance, 4pc = All of the above + Strength II
            case REALITY_WEAVER -> {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 40, 0, true, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 40, 0, true, false));
                if (pieces >= 4) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 40, 1, true, false));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 40, 0, true, false));
                }
            }
        }
    }

    /** Public tick entry point. */
    public static void onServerTick(net.minecraft.server.MinecraftServer server) {
        for (ServerWorld world : server.getWorlds()) {
            for (ServerPlayerEntity player : world.getPlayers()) {
                tickPlayer(player, world);
            }
        }
    }
}
