package com.political;


import com.political.economy.CoinManager;
import com.political.items.CustomItemManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.*;

/**
 * Long-form grind: collections, magic find, RNG extras on mining and combat (Skyblock-inspired).
 */
public final class AscensionGrindManager {

    private static final int MF_CAP = 400;
    private static final long[] TIER_THRESHOLDS = {
            250, 1_200, 6_000, 25_000, 125_000, 600_000, 2_500_000, 10_000_000, 40_000_000, 150_000_000
    };

    private static final Map<String, DataManager.AscensionPlayerSave> players = new HashMap<>();
    private static final Random rand = new Random();

    public static void loadFromData(DataManager.SaveData data) {
        players.clear();
        if (data.ascensionPlayers != null) {
            for (Map.Entry<String, DataManager.AscensionPlayerSave> e : data.ascensionPlayers.entrySet()) {
                players.put(e.getKey(), e.getValue());
            }
        }
    }

    public static void saveToData(DataManager.SaveData data) {
        data.ascensionPlayers = new HashMap<>(players);
    }

    private static DataManager.AscensionPlayerSave data(String uuid) {
        return players.computeIfAbsent(uuid, u -> {
            DataManager.AscensionPlayerSave s = new DataManager.AscensionPlayerSave();
            s.magicFind = 10;
            return s;
        });
    }

    public static int getMagicFind(String uuid) {
        int base = Math.max(0, data(uuid).magicFind);
        if (PlayerBuffManager.hasBuff(uuid, PlayerBuffManager.PlayerBuff.ASCENSION_TITAN)) {
            base += 10;
        }
        base += MasteryHeartTree.magicFindBonus(uuid);
        return Math.min(MF_CAP, base);
    }

    public static void onBlockBroken(ServerPlayer player, BlockState state) {
        if (!isOreLike(state)) {
            return;
        }
        String uuid = player.getUUID().toString();
        addCollection(uuid, "sector_ores", 1);
        tryRollExtras(player, uuid, true);
    }

    public static void onMobKilled(ServerPlayer killer, LivingEntity victim) {
        if (victim.getType().getCategory() != net.minecraft.world.entity.MobCategory.MONSTER) {
            return;
        }
        String uuid = killer.getUUID().toString();
        addCollection(uuid, "sector_hostiles", 1);
        tryRollExtras(killer, uuid, false);
    }

    private static boolean isOreLike(BlockState state) {
        var id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        String p = id != null ? id.getPath() : "";
        return p.contains("ore") || p.contains("ancient_debris") || p.contains("nether_gold");
    }

    private static void addCollection(String uuid, String key, long amount) {
        DataManager.AscensionPlayerSave d = data(uuid);
        long mult = System.currentTimeMillis() < d.doubleDropUntilMs ? 2L : 1L;
        long add = amount * mult;
        d.collectionCounts.merge(key, add, Long::sum);
        bumpTiers(uuid, d, key);
    }

    private static void bumpTiers(String uuid, DataManager.AscensionPlayerSave d, String key) {
        long c = d.collectionCounts.getOrDefault(key, 0L);
        int tier = d.collectionTiers.getOrDefault(key, 0);
        ServerPlayer pl = PoliticalServer.server != null
                ? PoliticalServer.server.getPlayerList().getPlayer(java.util.UUID.fromString(uuid))
                : null;
        while (tier < TIER_THRESHOLDS.length && c >= TIER_THRESHOLDS[tier]) {
            tier++;
            d.magicFind = Math.min(MF_CAP, d.magicFind + 3);
            d.collectionTiers.put(key, tier);
            if (pl != null) {
                pl.sendSystemMessage(Component.literal("Ascension — " + key + " tier " + tier + "! +3 Magic Find")
                        .withStyle(ChatFormatting.GOLD), false);
                BuffProgressManager.tryGrantAscensionTitan(pl);
            }
        }
        DataManager.save(PoliticalServer.server);
    }

    private static void tryRollExtras(ServerPlayer player, String uuid, boolean mining) {
        int mf = getMagicFind(uuid);
        double heartBonus = mining ? MasteryHeartTree.gatheringDoubleRollBonus(uuid) : 0.0;
        double dropChance = 0.085 + (mf / 2000.0) + (heartBonus * 0.35);
        dropChance = Math.min(0.45, Math.max(0.03, dropChance));
        if (rand.nextDouble() >= dropChance) {
            return;
        }
        int rarityRoll = rand.nextInt(1200) + (mf / 3);
        CustomItemManager.CustomItem item;
        if (rarityRoll >= 1050) {
            item = CustomItemManager.getItem("asc_rngesus_essence");
        } else if (rarityRoll >= 820) {
            item = CustomItemManager.getItem(mining ? "asc_vein_shard" : "asc_blood_fragment");
        } else if (rarityRoll >= 620) {
            item = CustomItemManager.getItem("asc_unstable_star");
        } else {
            item = CustomItemManager.getItem("asc_common_shard");
        }
        if (item != null) {
            player.addItem(item.createStack(1));
            player.sendSystemMessage(Component.literal("✦ Rare drop! ").withStyle(ChatFormatting.LIGHT_PURPLE)
                    .append(Component.literal(item.name).withStyle(ChatFormatting.AQUA)), false);
        }
    }

    public static boolean handleConsume(ServerPlayer player, String customId, ItemStack stack) {
        String uuid = player.getUUID().toString();
        return switch (customId) {
            case "asc_hyper_catalyst" -> {
                data(uuid).doubleDropUntilMs = System.currentTimeMillis() + 60_000L;
                stack.shrink(1);
                player.sendSystemMessage(Component.literal("§aSector Surge §7— double collection progress for 60s.").withStyle(ChatFormatting.GREEN));
                DataManager.save(PoliticalServer.server);
                yield true;
            }
            case "asc_mf_charm" -> {
                DataManager.AscensionPlayerSave d = data(uuid);
                d.magicFind = Math.min(MF_CAP, d.magicFind + 5);
                stack.shrink(1);
                player.sendSystemMessage(Component.literal("+5 Magic Find (permanent, cap " + MF_CAP + ")").withStyle(ChatFormatting.AQUA));
                DataManager.save(PoliticalServer.server);
                yield true;
            }
            case "asc_void_lottery" -> {
                stack.shrink(1);
                int coins = 50_000 + rand.nextInt(450_000);
                CoinManager.giveCoinsQuiet(player, coins);
                if (rand.nextDouble() < 0.08 && CustomItemManager.getItem("asc_rngesus_essence") != null) {
                    player.addItem(CustomItemManager.getItem("asc_rngesus_essence").createStack(1));
                }
                player.sendSystemMessage(Component.literal("Void lottery: +" + coins + " coins").withStyle(ChatFormatting.LIGHT_PURPLE));
                DataManager.save(PoliticalServer.server);
                yield true;
            }
            case "asc_sector_compass" -> {
                stack.shrink(1);
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.LUCK, 600, 2, true, false, true));
                CoinManager.giveCoinsQuiet(player, 25_000);
                player.sendSystemMessage(Component.literal("Expedition cache: luck + coins.").withStyle(ChatFormatting.GOLD));
                DataManager.save(PoliticalServer.server);
                yield true;
            }
            default -> false;
        };
    }

    public static int getMaxCollectionTier(String uuid) {
        DataManager.AscensionPlayerSave d = players.get(uuid);
        if (d == null || d.collectionTiers == null || d.collectionTiers.isEmpty()) {
            return 0;
        }
        int m = 0;
        for (int t : d.collectionTiers.values()) {
            m = Math.max(m, t);
        }
        return m;
    }

    public static void sendStatus(ServerPlayer player) {
        String u = player.getUUID().toString();
        DataManager.AscensionPlayerSave d = data(u);
        player.sendSystemMessage(Component.literal("══ §6Sector Ascension §f══").withStyle(ChatFormatting.GOLD), false);
        player.sendSystemMessage(Component.literal("Magic Find: §b" + getMagicFind(u)).withStyle(ChatFormatting.WHITE), false);
        for (String k : List.of("sector_ores", "sector_hostiles")) {
            long c = d.collectionCounts.getOrDefault(k, 0L);
            int t = d.collectionTiers.getOrDefault(k, 0);
            player.sendSystemMessage(Component.literal(k + ": §f" + c + " §7(tier " + t + ")").withStyle(ChatFormatting.GRAY), false);
        }
    }

    /** Next milestone count when you already have {@code currentTier} (0..9). */
    public static long getNextThresholdAfterTier(int currentTier) {
        if (currentTier < 0 || currentTier >= TIER_THRESHOLDS.length) {
            return Long.MAX_VALUE;
        }
        return TIER_THRESHOLDS[currentTier];
    }

    public static long getCollectionCount(String uuid, String key) {
        return data(uuid).collectionCounts.getOrDefault(key, 0L);
    }

    public static int getCollectionTier(String uuid, String key) {
        return data(uuid).collectionTiers.getOrDefault(key, 0);
    }
}
