package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * The collectors: the only buyers for things the market cannot price.
 *
 * <p>A bazaar works by matching two people who both want the trade. That breaks down completely at
 * the top of the drop table, where a Golden Fleece turns up once in a thousand sheep and there is
 * simply nobody standing on the other side of the order book. Listing one is not a sale, it is an
 * advert nobody answers.</p>
 *
 * <p>So the mythic and primal drops are sold to a person instead. A collector pays a fixed, published
 * price out of nothing, which is a coin faucet and is meant to be, because the alternative is that
 * the rarest items in the game are worth nothing at all on a quiet server.</p>
 */
public final class CollectorManager {

    private CollectorManager() {}

    public static final String TAG = "political_collector";
    private static final Set<UUID> COLLECTORS = new HashSet<>();

    // ── Placing ───────────────────────────────────────────────────────────

    public static Villager spawn(ServerLevel world, double x, double y, double z, float yaw) {
        Villager v = new Villager(EntityType.VILLAGER, world);
        v.setPos(x, y, z);
        v.setYRot(yaw);
        v.setXRot(0f);
        v.setCustomName(Component.literal("Collector")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        v.setCustomNameVisible(true);
        v.setInvulnerable(true);
        v.setNoAi(true);
        v.setSilent(true);
        v.setPersistenceRequired();
        v.setHealth(v.getMaxHealth());
        v.removeAllEffects();

        try {
            v.setVillagerData(v.getVillagerData()
                    .withProfession(BuiltInRegistries.VILLAGER_PROFESSION
                            .getOrThrow(VillagerProfession.CARTOGRAPHER))
                    .withLevel(5));
        } catch (Exception ignored) {
            // A profession that will not resolve is cosmetic, so carry on without it.
        }

        v.addTag(TAG);
        world.addFreshEntity(v);
        COLLECTORS.add(v.getUUID());
        return v;
    }

    public static boolean isCollector(Villager v) {
        return v != null && (v.entityTags().contains(TAG) || COLLECTORS.contains(v.getUUID()));
    }

    public static void register(UUID id) {
        COLLECTORS.add(id);
    }

    /** Removes the nearest collector within a short reach. Returns true when one went. */
    public static boolean removeNearest(ServerLevel world, ServerPlayer player, double radius) {
        java.util.List<Villager> found = world.getEntitiesOfClass(Villager.class,
                player.getBoundingBox().inflate(radius), CollectorManager::isCollector);
        if (found.isEmpty()) return false;
        Villager v = found.get(0);
        COLLECTORS.remove(v.getUUID());
        v.discard();
        return true;
    }

    public static int removeAll(ServerLevel world) {
        // Same world spanning box the auction master uses, so both behave identically.
        net.minecraft.world.phys.AABB global = new net.minecraft.world.phys.AABB(
                -30000000, -2048, -30000000, 30000000, 2048, 30000000);
        java.util.List<Villager> found = world.getEntitiesOfClass(
                Villager.class, global, CollectorManager::isCollector);
        for (Villager v : found) {
            COLLECTORS.remove(v.getUUID());
            v.discard();
        }
        return found.size();
    }

    // ── The bazaar broker ─────────────────────────────────────────────────
    //
    // Same villager plumbing, different counter. Kept here rather than in its own manager because
    // the only thing that differs is the name and which screen the right click opens, and two
    // near identical files would drift apart the first time one of them was fixed.

    public static final String BAZAAR_TAG = "political_bazaar_broker";
    private static final Set<UUID> BROKERS = new HashSet<>();

    public static Villager spawnBroker(ServerLevel world, double x, double y, double z, float yaw) {
        Villager v = new Villager(EntityType.VILLAGER, world);
        v.setPos(x, y, z);
        v.setYRot(yaw);
        v.setXRot(0f);
        v.setCustomName(Component.literal("Bazaar Broker")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        v.setCustomNameVisible(true);
        v.setInvulnerable(true);
        v.setNoAi(true);
        v.setSilent(true);
        v.setPersistenceRequired();
        v.setHealth(v.getMaxHealth());
        v.removeAllEffects();
        try {
            v.setVillagerData(v.getVillagerData()
                    .withProfession(BuiltInRegistries.VILLAGER_PROFESSION
                            .getOrThrow(VillagerProfession.FARMER))
                    .withLevel(5));
        } catch (Exception ignored) {
            // Cosmetic only.
        }
        v.addTag(BAZAAR_TAG);
        world.addFreshEntity(v);
        BROKERS.add(v.getUUID());
        return v;
    }

    public static boolean isBroker(Villager v) {
        return v != null && (v.entityTags().contains(BAZAAR_TAG) || BROKERS.contains(v.getUUID()));
    }

    public static InteractionResult handleBrokerInteraction(Player player, Villager villager) {
        if (!isBroker(villager)) return InteractionResult.PASS;
        if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
        BazaarGui.open(sp);
        return InteractionResult.SUCCESS;
    }

    public static boolean removeNearestBroker(ServerLevel world, ServerPlayer player, double radius) {
        java.util.List<Villager> found = world.getEntitiesOfClass(Villager.class,
                player.getBoundingBox().inflate(radius), CollectorManager::isBroker);
        if (found.isEmpty()) return false;
        Villager v = found.get(0);
        BROKERS.remove(v.getUUID());
        v.discard();
        return true;
    }

    public static int removeAllBrokers(ServerLevel world) {
        net.minecraft.world.phys.AABB global = new net.minecraft.world.phys.AABB(
                -30000000, -2048, -30000000, 30000000, 2048, 30000000);
        java.util.List<Villager> found = world.getEntitiesOfClass(
                Villager.class, global, CollectorManager::isBroker);
        for (Villager v : found) {
            BROKERS.remove(v.getUUID());
            v.discard();
        }
        return found.size();
    }

    // ── Buying ────────────────────────────────────────────────────────────

    public static InteractionResult handleInteraction(Player player, Villager villager) {
        if (!isCollector(villager)) return InteractionResult.PASS;
        if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
        CollectorGui.open(sp);
        return InteractionResult.SUCCESS;
    }

    /**
     * What a collector pays for one of these.
     *
     * <p>Derived from the drop's own chance rather than set by hand, so a rarer thing is worth more
     * automatically and nothing has to be retuned when a new drop is added. The floor keeps a
     * merely uncommon find from paying nothing at all.</p>
     */
    public static long priceOf(RareDrop d) {
        double rarity = 1.0 / Math.max(0.0001, d.chance);
        long base = Math.round(rarity * 45);
        long tierBonus = switch (d.tier) {
            case UNCOMMON, RARE -> 0L;
            case CRAZY -> 25_000L;
            case INSANE -> 120_000L;
            case MYTHIC -> 400_000L;
            case PRIMAL -> 1_500_000L;
        };
        return Math.max(2_500L, base + tierBonus);
    }

    /** Whether the market can handle this, or whether a collector is the only buyer. */
    public static boolean collectorOnly(RareDrop d) {
        return d.tier.collectorOnly;
    }

    /** How many of this drop the player is carrying. */
    public static int held(ServerPlayer player, RareDrop d) {
        int n = 0;
        for (ItemStack st : player.getInventory().getNonEquipmentItems()) {
            if (st.isEmpty()) continue;
            if (RareDropManager.dropOf(st) == d) n += st.getCount();
        }
        return n;
    }

    /** Sells every one of a drop the player is carrying. Returns how many went. */
    public static int sellAll(ServerPlayer player, RareDrop d) {
        int have = held(player, d);
        if (have <= 0) {
            player.sendSystemMessage(Component.literal("§cYou are not carrying any of those."));
            return 0;
        }

        // Take the items first, so a failure part way through can never pay twice.
        int left = have;
        for (ItemStack st : player.getInventory().getNonEquipmentItems()) {
            if (left <= 0) break;
            if (st.isEmpty() || RareDropManager.dropOf(st) != d) continue;
            int take = Math.min(left, st.getCount());
            st.shrink(take);
            left -= take;
        }
        int sold = have - left;
        if (sold <= 0) return 0;

        long each = priceOf(d);
        long total = each * sold;
        CoinManager.giveCoins(player, total);

        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("  §d§lSOLD TO A COLLECTOR"));
        player.sendSystemMessage(Component.literal("  §f" + sold + " " + d.display));
        player.sendSystemMessage(Component.literal("  §7Paid §6" + GuiKit.num(total) + " coins"));
        player.sendSystemMessage(Component.literal(""));

        if (player.level() instanceof ServerLevel level) {
            level.playSound(null, player.blockPosition(), SoundEvents.VILLAGER_YES,
                    SoundSource.PLAYERS, 1.0f, 1.0f);
        }

        // A primal sale is rare enough that the whole server should hear about it.
        if (d.tier == RareDrop.Tier.PRIMAL && PoliticalServer.server != null) {
            for (ServerPlayer p : PoliticalServer.server.getPlayerList().getPlayers()) {
                p.sendSystemMessage(Component.literal("  §4§lPRIMAL SALE §r§7"
                        + player.getName().getString() + " sold " + sold + " "
                        + d.colour + d.display + " §7for §6" + GuiKit.num(total)));
            }
        }
        return sold;
    }
}
