package com.political;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages custom enchantments stored as NBT data in item CustomData components.
 * Enchantments are stored in a "custom_enchants" sub-compound with keys like
 * "slayer_sharpness: 3, boss_hunter: 2".
 */
public class CustomEnchantmentManager {

    // ── Enchantment keys ────────────────────────────────────────
    public static final String SLAYER_SHARPNESS   = "slayer_sharpness";
    public static final String BOSS_HUNTER        = "boss_hunter";
    public static final String MOB_BANE           = "mob_bane";
    public static final String VAMPIRIC           = "vampiric";
    public static final String THUNDERBOLT        = "thunderbolt";
    public static final String BOUNTY_PROTECTION  = "bounty_protection";
    public static final String BOSS_WARD          = "boss_ward";
    public static final String VITALITY           = "vitality";
    public static final String THORNS_OF_VENGEANCE = "thorns_of_vengeance";
    public static final String EXECUTE            = "execute";
    public static final String FROSTBITE          = "frostbite";
    public static final String LAST_STAND         = "last_stand";
    public static final String SOULBOUND          = "soulbound";
    public static final String PROSPECTOR         = "prospector";

    private static final String ENCHANT_TAG = "custom_enchants";

    // ── Read ─────────────────────────────────────────────────────

    /**
     * Returns the level of the given custom enchantment on the item, or 0 if absent.
     */
    public static int getLevel(ItemStack stack, String enchantment) {
        if (stack == null || stack.isEmpty()) return 0;
        NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (data == null) return 0;
        NbtCompound nbt = data.copyNbt();
        if (!nbt.contains(ENCHANT_TAG)) return 0;
        NbtCompound enchants = nbt.getCompound(ENCHANT_TAG).orElse(null);
        if (enchants == null) return 0;
        if (!enchants.contains(enchantment)) return 0;
        return enchants.getInt(enchantment).orElse(0);
    }

    public static boolean has(ItemStack stack, String enchantment) {
        return getLevel(stack, enchantment) > 0;
    }

    // ── Write ────────────────────────────────────────────────────

    /**
     * Adds a custom enchantment to the item's CustomData NBT and returns the item.
     */
    public static ItemStack addEnchantment(ItemStack stack, String enchantment, int level) {
        NbtCompound nbt;
        NbtComponent existing = stack.get(DataComponentTypes.CUSTOM_DATA);
        nbt = (existing != null) ? existing.copyNbt() : new NbtCompound();

        NbtCompound enchants = nbt.contains(ENCHANT_TAG)
                ? nbt.getCompound(ENCHANT_TAG).orElse(new NbtCompound())
                : new NbtCompound();
        enchants.putInt(enchantment, level);
        nbt.put(ENCHANT_TAG, enchants);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        return stack;
    }

    // ── Damage multipliers ───────────────────────────────────────

    /**
     * Returns the total offensive damage multiplier from all custom enchantments
     * on the weapon, given the target entity.
     */
    public static float getOffensiveMultiplier(ItemStack weapon,
                                                net.minecraft.entity.LivingEntity target) {
        float multiplier = 1.0f;

        // Slayer Sharpness: +15% per level to ALL mobs
        int sharpness = getLevel(weapon, SLAYER_SHARPNESS);
        if (sharpness > 0) {
            multiplier += 0.15f * sharpness;
        }

        // Boss Hunter: +25% per level against slayer bosses
        int bossHunter = getLevel(weapon, BOSS_HUNTER);
        if (bossHunter > 0 && SlayerManager.isSlayerBoss(target.getUuid())) {
            multiplier += 0.25f * bossHunter;
        }

        // Mob Bane: +20% per level against matching mob type
        int mobBane = getLevel(weapon, MOB_BANE);
        if (mobBane > 0) {
            SlayerManager.SlayerType weaponType = SlayerItems.getSwordSlayerType(weapon);
            if (weaponType != null && isMobOfType(target, weaponType)) {
                multiplier += 0.20f * mobBane;
            }
        }

        // Execute: +1% damage per 1% of target's missing HP per level
        int execute = getLevel(weapon, EXECUTE);
        if (execute > 0) {
            float missingHpPercent = 1.0f - (target.getHealth() / target.getMaxHealth());
            multiplier += execute * missingHpPercent;
        }

        return multiplier;
    }

    /**
     * Returns the total defensive damage multiplier from all custom enchantments
     * on armor pieces worn by the target player.
     * Returns a value < 1.0 to reduce damage.
     */
    public static float getDefensiveMultiplier(net.minecraft.server.network.ServerPlayerEntity player,
                                                net.minecraft.entity.damage.DamageSource source) {
        float multiplier = 1.0f;

        ItemStack[] armor = {
                player.getEquippedStack(net.minecraft.entity.EquipmentSlot.HEAD),
                player.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST),
                player.getEquippedStack(net.minecraft.entity.EquipmentSlot.LEGS),
                player.getEquippedStack(net.minecraft.entity.EquipmentSlot.FEET)
        };

        boolean isBossAttack = source.getAttacker() instanceof net.minecraft.entity.LivingEntity attacker
                && SlayerManager.isSlayerBoss(attacker.getUuid());

        for (ItemStack piece : armor) {
            if (piece.isEmpty()) continue;

            // Bounty Protection: -5% per level ALL damage
            int protection = getLevel(piece, BOUNTY_PROTECTION);
            if (protection > 0) {
                multiplier -= 0.05f * protection;
            }

            // Boss Ward: -10% from slayer bosses per level
            int bossWard = getLevel(piece, BOSS_WARD);
            if (bossWard > 0 && isBossAttack) {
                multiplier -= 0.10f * bossWard;
            }

            // Last Stand: -2% per level per 10% missing HP (only below 30% HP)
            int lastStand = getLevel(piece, LAST_STAND);
            if (lastStand > 0) {
                float missingHpPercent = 1.0f - (player.getHealth() / player.getMaxHealth());
                if (missingHpPercent > 0.70f) { // only activates below 30% HP
                    int tenPercentChunks = (int)((missingHpPercent - 0.70f) * 10);
                    multiplier -= 0.02f * lastStand * tenPercentChunks;
                }
            }
        }

        return Math.max(0.1f, multiplier);
    }

    /**
     * Returns the flat health-bonus hearts from Vitality enchantments.
     * Each level gives +2 max health (= 4 HP = 2 hearts).
     */
    public static double getVitalityHealthBonus(net.minecraft.server.network.ServerPlayerEntity player) {
        ItemStack chest = player.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST);
        int vitality = getLevel(chest, VITALITY);
        return vitality * 4.0; // +2 hearts (4 HP) per level
    }

    // ── Helpers ──────────────────────────────────────────────────

    private static boolean isMobOfType(net.minecraft.entity.LivingEntity entity,
                                        SlayerManager.SlayerType type) {
        if (entity instanceof net.minecraft.entity.mob.ZombieEntity
                || entity instanceof net.minecraft.entity.mob.ZombieVillagerEntity
                || entity instanceof net.minecraft.entity.mob.DrownedEntity
                || entity instanceof net.minecraft.entity.mob.HuskEntity) {
            return type == SlayerManager.SlayerType.ZOMBIE;
        }
        if (entity instanceof net.minecraft.entity.mob.SpiderEntity
                || entity instanceof net.minecraft.entity.mob.CaveSpiderEntity) {
            return type == SlayerManager.SlayerType.SPIDER;
        }
        if (entity instanceof net.minecraft.entity.mob.SkeletonEntity
                || entity instanceof net.minecraft.entity.mob.WitherSkeletonEntity
                || entity instanceof net.minecraft.entity.mob.StrayEntity) {
            return type == SlayerManager.SlayerType.SKELETON;
        }
        if (entity instanceof net.minecraft.entity.mob.SlimeEntity) {
            return type == SlayerManager.SlayerType.SLIME;
        }
        if (entity instanceof net.minecraft.entity.mob.EndermanEntity) {
            return type == SlayerManager.SlayerType.ENDERMAN;
        }
        if (entity instanceof net.minecraft.entity.mob.WardenEntity) {
            return type == SlayerManager.SlayerType.IRON_GOLEM;
        }
        // Also check custom name for bounty mobs
        if (entity.hasCustomName()) {
            String name = entity.getCustomName().getString();
            return switch (type) {
                case ZOMBIE   -> name.contains("Zombie");
                case SPIDER   -> name.contains("Spider");
                case SKELETON -> name.contains("Skeleton");
                case SLIME    -> name.contains("Slime");
                case ENDERMAN -> name.contains("Enderman");
                case IRON_GOLEM   -> name.contains("Warden");
                case PIGLIN   -> name.contains("Piglin") || name.contains("Gilded") || name.contains("Ravager");
            };
        }
        return false;
    }

    /**
     * Returns true if the item has any custom enchantment.
     */
    public static boolean hasAnyCustomEnchantment(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (data == null) return false;
        NbtCompound nbt = data.copyNbt();
        if (!nbt.contains(ENCHANT_TAG)) return false;
        NbtCompound enchants = nbt.getCompound(ENCHANT_TAG).orElse(null);
        return enchants != null && !enchants.isEmpty();
    }

    /**
     * Removes all custom enchantments from the item.
     */
    public static void removeAllCustomEnchantments(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (data == null) return;
        NbtCompound nbt = data.copyNbt();
        if (!nbt.contains(ENCHANT_TAG)) return;
        nbt.remove(ENCHANT_TAG);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

    // ── Lore helpers ─────────────────────────────────────────────

    /**
     * Alias for {@link #getLevel} for API compatibility.
     */
    public static int getEnchantmentLevel(ItemStack stack, String enchantment) {
        return getLevel(stack, enchantment);
    }

    /**
     * Appends lore lines for all active custom enchantments on the item.
     * Lines are added in a consistent order.
     */
    public static void addEnchantmentLore(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (data == null) return;
        NbtCompound nbt = data.copyNbt();
        if (!nbt.contains(ENCHANT_TAG)) return;
        NbtCompound enchants = nbt.getCompound(ENCHANT_TAG).orElse(null);
        if (enchants == null) return;

        LoreComponent existing = stack.get(DataComponentTypes.LORE);
        List<Text> lore = existing != null ? new ArrayList<>(existing.lines()) : new ArrayList<>();

        String[][] entries = {
            {SLAYER_SHARPNESS,      "✦ Slayer Sharpness"},
            {BOSS_HUNTER,           "✦ Boss Hunter"},
            {MOB_BANE,              "✦ Mob Bane"},
            {VAMPIRIC,              "✦ Vampiric"},
            {THUNDERBOLT,           "✦ Thunderbolt"},
            {BOUNTY_PROTECTION,     "✦ Bounty Protection"},
            {BOSS_WARD,             "✦ Boss Ward"},
            {VITALITY,              "✦ Vitality"},
            {THORNS_OF_VENGEANCE,   "✦ Thorns of Vengeance"},
            {EXECUTE,               "✦ Execute"},
            {FROSTBITE,             "✦ Frostbite"},
            {LAST_STAND,            "✦ Last Stand"},
            {SOULBOUND,             "✦ Soulbound"},
            {PROSPECTOR,            "✦ Prospector"},
        };
        for (String[] entry : entries) {
            int level = enchants.getInt(entry[0], 0);
            if (level > 0) {
                lore.add(Text.literal(entry[1] + " " + toRoman(level)).formatted(Formatting.AQUA));
            }
        }
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
    }

    private static String toRoman(int level) {
        return switch (level) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(level);
        };
    }
}
