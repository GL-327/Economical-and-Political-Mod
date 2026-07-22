package com.political;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.*;

/**
 * Hypixel-style "Heart of the Mountain" skill tree: spend coins on prerequisite-linked perks.
 * Unlock state is stored in {@link MasteryManager} per player.
 */
public final class MasteryHeartTree {

    public record HeartNode(
            String id,
            String title,
            String branch,
            int tier,
            long coinCost,
            String parentId,
            String description
    ) {}

    public static final List<HeartNode> NODES = List.of(
            // Mining column
            new HeartNode("heart_m1", "Pickaxe Proficiency", "MINING", 1, 50_000L, null,
                    "+10% block break speed"),
            new HeartNode("heart_m2", "Powder Charged", "MINING", 2, 175_000L, "heart_m1",
                    "+18% extra drops from ores you break"),
            new HeartNode("heart_m3", "Efficient Excavator", "MINING", 3, 450_000L, "heart_m2",
                    "+35% Mastery XP from mining actions"),
            new HeartNode("heart_m4", "Vein Sense", "MINING", 4, 1_000_000L, "heart_m3",
                    "+12% Gathering mastery double-drop roll"),
            new HeartNode("heart_m5", "Deep Strata", "MINING", 5, 2_200_000L, "heart_m4",
                    "+22% damage vs Slayer bounty bosses (main hand)"),
            new HeartNode("heart_m6", "Core Breaker", "MINING", 6, 4_000_000L, "heart_m5",
                    "+40% damage vs Slayer bosses while holding a pickaxe"),

            // Combat column
            new HeartNode("heart_c1", "Warmonger", "COMBAT", 1, 50_000L, null,
                    "+8% melee & projectile damage"),
            new HeartNode("heart_c2", "Bounty Focus", "COMBAT", 2, 200_000L, "heart_c1",
                    "+15% damage vs Slayer bounty bosses"),
            new HeartNode("heart_c3", "Cleave Training", "COMBAT", 3, 500_000L, "heart_c2",
                    "+12% damage when target below 50% HP"),
            new HeartNode("heart_c4", "Glass Cannon", "COMBAT", 4, 1_100_000L, "heart_c3",
                    "+18% crit-strike damage (rolled in mastery chain)"),
            new HeartNode("heart_c5", "Blood Toll", "COMBAT", 5, 2_500_000L, "heart_c4",
                    "Heal 8% max HP on killing blows"),
            new HeartNode("heart_c6", "Apex Heart", "COMBAT", 6, 4_500_000L, "heart_c5",
                    "+25% damage vs hostile mobs"),

            // Fortune / civil column
            new HeartNode("heart_f1", "Tally Keeper", "FORTUNE", 1, 50_000L, null,
                    "+8% coins from mob kill bonus rolls"),
            new HeartNode("heart_f2", "Vault Line", "FORTUNE", 2, 220_000L, "heart_f1",
                    "+6% effective bank interest"),
            new HeartNode("heart_f3", "Scholar's Cut", "FORTUNE", 3, 550_000L, "heart_f2",
                    "+15% experience orb value"),
            new HeartNode("heart_f4", "Sector Pass", "FORTUNE", 4, 1_300_000L, "heart_f3",
                    "+18 Magic Find"),
            new HeartNode("heart_f5", "Networked", "FORTUNE", 5, 2_800_000L, "heart_f4",
                    "+25% Mastery XP from all sources"),
            new HeartNode("heart_f6", "Heart of Commerce", "FORTUNE", 6, 5_000_000L, "heart_f5",
                    "+12% shop sell prices")
    );

    private static final Map<String, HeartNode> BY_ID = new HashMap<>();

    static {
        for (HeartNode n : NODES) {
            BY_ID.put(n.id, n);
        }
    }

    public static HeartNode getNode(String id) {
        return BY_ID.get(id);
    }

    public static boolean hasUnlocked(Set<String> unlocked, String id) {
        return id != null && unlocked != null && unlocked.contains(id);
    }

    public static boolean canUnlock(ServerPlayer player, String id, Set<String> unlocked) {
        HeartNode n = BY_ID.get(id);
        if (n == null) return false;
        if (hasUnlocked(unlocked, id)) return false;
        // Coin tree: only prerequisite chain (no mastery level gate — coins + order are the grind)
        if (n.parentId == null) return true;
        return hasUnlocked(unlocked, n.parentId);
    }

    public static boolean tryPurchase(ServerPlayer player, String id) {
        HeartNode n = BY_ID.get(id);
        if (n == null) {
            player.sendSystemMessage(Component.literal("Unknown perk.").withStyle(ChatFormatting.RED), false);
            return false;
        }
        Set<String> u = MasteryManager.getHeartUnlocks(player.getUUID().toString());
        if (hasUnlocked(u, id)) {
            player.sendSystemMessage(Component.literal("Already unlocked.").withStyle(ChatFormatting.YELLOW), false);
            return false;
        }
        if (!canUnlock(player, id, u)) {
            player.sendSystemMessage(Component.literal("Unlock the previous perk in this branch first.").withStyle(ChatFormatting.RED), false);
            return false;
        }
        long cost = n.coinCost();
        if (!CoinManager.hasCoins(player, cost)) {
            player.sendSystemMessage(Component.literal("Need " + String.format("%,d", cost) + " coins.").withStyle(ChatFormatting.RED), false);
            return false;
        }
        if (!CoinManager.removeCoins(player, cost)) {
            player.sendSystemMessage(Component.literal("Payment failed.").withStyle(ChatFormatting.RED), false);
            return false;
        }
        MasteryManager.addHeartUnlock(player.getUUID().toString(), id);
        player.sendSystemMessage(Component.literal("♦ Heart unlocked: ").withStyle(ChatFormatting.GOLD)
                .append(Component.literal(n.title()).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)), false);
        player.sendSystemMessage(Component.literal(n.description()).withStyle(ChatFormatting.GRAY), false);
        return true;
    }

    // --- Bonus helpers (read unlock set from MasteryManager each call) ---

    public static double miningSpeedMultiplier(String uuid) {
        Set<String> u = MasteryManager.getHeartUnlocks(uuid);
        double m = 1.0;
        if (hasUnlocked(u, "heart_m1")) m += 0.10;
        return m;
    }

    /** 18% chance to duplicate ore drops (afterBreak), stacks independently of other buffs. */
    public static boolean rollHeartOreDupe(ServerPlayer player) {
        if (!hasUnlocked(MasteryManager.getHeartUnlocks(player.getUUID().toString()), "heart_m2")) {
            return false;
        }
        return player.getRandom().nextDouble() < 0.18;
    }

    public static double masteryXpFromMiningMultiplier(String uuid) {
        Set<String> u = MasteryManager.getHeartUnlocks(uuid);
        if (hasUnlocked(u, "heart_m3")) return 1.35;
        return 1.0;
    }

    public static double gatheringDoubleRollBonus(String uuid) {
        Set<String> u = MasteryManager.getHeartUnlocks(uuid);
        if (hasUnlocked(u, "heart_m4")) return 0.12;
        return 0.0;
    }

    public static float slayerBossDamageMultiplier(String uuid, ItemStack weapon) {
        Set<String> u = MasteryManager.getHeartUnlocks(uuid);
        float m = 1.0f;
        if (hasUnlocked(u, "heart_m5")) m *= 1.22f;
        if (hasUnlocked(u, "heart_m6") && weapon != null && !weapon.isEmpty()) {
            String id = SlayerItems.getCustomItemId(weapon);
            if (id != null && id.contains("pickaxe")) {
                m *= 1.40f;
            }
        }
        return m;
    }

    /** Same as heart_c1 melee bonus; use for player-shot projectiles. */
    public static float projectileDamageMultiplier(String uuid) {
        return hasUnlocked(MasteryManager.getHeartUnlocks(uuid), "heart_c1") ? 1.08f : 1.0f;
    }

    public static float combatDamageMultiplier(ServerPlayer player, LivingEntity target) {
        Set<String> u = MasteryManager.getHeartUnlocks(player.getUUID().toString());
        float m = 1.0f;
        if (hasUnlocked(u, "heart_c1")) m *= 1.08f;
        if (hasUnlocked(u, "heart_c2") && SlayerManager.isSlayerBoss(target.getUUID())) {
            m *= 1.15f;
        }
        if (hasUnlocked(u, "heart_c3") && target.getHealth() <= target.getMaxHealth() * 0.5f) {
            m *= 1.12f;
        }
        if (hasUnlocked(u, "heart_c4") && player.getRandom().nextFloat() < 0.12f) {
            m *= 1.18f;
        }
        if (hasUnlocked(u, "heart_c6") && target.getType().getCategory() == net.minecraft.world.entity.MobCategory.MONSTER) {
            m *= 1.25f;
        }
        return m;
    }

    public static boolean shouldHealOnKill(String uuid) {
        return hasUnlocked(MasteryManager.getHeartUnlocks(uuid), "heart_c5");
    }

    public static void onKillHeal(ServerPlayer player) {
        if (!shouldHealOnKill(player.getUUID().toString())) return;
        float heal = player.getMaxHealth() * 0.08f;
        if (heal > 0) {
            player.heal(heal);
            player.sendSystemMessage(Component.literal("♥ Heart: Blood Toll").withStyle(ChatFormatting.RED), true);
        }
    }

    public static double coinKillBonusMultiplier(String uuid) {
        Set<String> u = MasteryManager.getHeartUnlocks(uuid);
        if (hasUnlocked(u, "heart_f1")) return 1.08;
        return 1.0;
    }

    public static double bankInterestBonus(String uuid) {
        Set<String> u = MasteryManager.getHeartUnlocks(uuid);
        if (hasUnlocked(u, "heart_f2")) return 0.06;
        return 0.0;
    }

    public static double experienceOrbMultiplier(String uuid) {
        Set<String> u = MasteryManager.getHeartUnlocks(uuid);
        if (hasUnlocked(u, "heart_f3")) return 1.15;
        return 1.0;
    }

    public static int magicFindBonus(String uuid) {
        Set<String> u = MasteryManager.getHeartUnlocks(uuid);
        if (hasUnlocked(u, "heart_f4")) return 18;
        return 0;
    }

    public static double masteryXpGlobalMultiplier(String uuid) {
        Set<String> u = MasteryManager.getHeartUnlocks(uuid);
        if (hasUnlocked(u, "heart_f5")) return 1.25;
        return 1.0;
    }

    public static double shopSellBonus(String uuid) {
        Set<String> u = MasteryManager.getHeartUnlocks(uuid);
        if (hasUnlocked(u, "heart_f6")) return 0.12;
        return 0.0;
    }
}
