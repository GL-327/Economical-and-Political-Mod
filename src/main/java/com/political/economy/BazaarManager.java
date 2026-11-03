package com.political.economy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.util.Prediction;
import com.political.CollectionType;
import com.political.GuiKit;
import com.political.PoliticalServer;



import com.political.bounty.SlayerItems;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * A double sided order book for raw materials.
 *
 * <p>The collections give players an enormous pile of one material each. The bazaar is where that
 * pile turns into coins without needing another player online at that exact moment. Players post buy
 * orders and sell offers, and anyone can take the other side instantly at the best posted price.</p>
 *
 * <p>The one deliberate concession to a small server is the <b>house book</b>. If nobody has posted,
 * the bazaar still quotes a price either side of the material's base value, so a solo player is not
 * staring at an empty market. The house spread is wide enough that trading against real players is
 * always better, which is the point: it is a floor, not a competitor.</p>
 */
public final class BazaarManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(BazaarManager.class);


    private BazaarManager() {}

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path savePath;

    /** How far either side of base the house will quote. Wide on purpose. */
    private static final double HOUSE_BUY_FACTOR = 0.55;
    private static final double HOUSE_SELL_FACTOR = 1.60;
    /** The cut the bazaar takes from a filled order, in percent. */
    public static final int TAX_PERCENT = 2;

    public static final class Order {
        public String id = UUID.randomUUID().toString();
        public String ownerUuid = "";
        public String ownerName = "";
        public String product = "";
        public long unitPrice;
        public int amount;
        public int filled;
        public boolean buy;
        public long createdAt;
        /** Of the filled units, how many the owner has actually collected. */
        public int claimed;

        public int remaining() {
            return Math.max(0, amount - filled);
        }

        /** Goods bought on this order that are still waiting to be collected. */
        public int claimable() {
            return buy ? Math.max(0, filled - claimed) : 0;
        }

        /** An order is finished once it cannot fill further and has nothing left to hand over. */
        public boolean finished() {
            return remaining() <= 0 && claimable() <= 0;
        }
    }

    private static final List<Order> ORDERS = new ArrayList<>();

    // ── Persistence ───────────────────────────────────────────────────────

    public static void initialize(Path worldRoot) {
        savePath = worldRoot.resolve("political").resolve("bazaar.json");
        load();
    }

    private static void load() {
        ORDERS.clear();
        if (savePath == null || !Files.exists(savePath)) return;
        try (Reader r = Files.newBufferedReader(savePath)) {
            java.lang.reflect.Type t = new TypeToken<List<Order>>() {}.getType();
            List<Order> data = GSON.fromJson(r, t);
            if (data != null) ORDERS.addAll(data);
        } catch (Exception e) {
            LOGGER.error("Could not read the book", e);
        }
    }

    public static void save() {
        if (savePath == null) return;
        try {
            Files.createDirectories(savePath.getParent());
            try (Writer w = Files.newBufferedWriter(savePath)) {
                GSON.toJson(ORDERS, w);
            }
        } catch (Exception e) {
            LOGGER.error("Could not write the book", e);
        }
    }

    // ── Pricing ───────────────────────────────────────────────────────────

    /** What one unit is nominally worth, from how hard the material is to get. */
    public static long baseValue(CollectionType c) {
        return switch (c.band) {
            case COMMON -> 8L;
            case UNCOMMON -> 25L;
            case RARE -> 120L;
            case LEGENDARY -> 900L;
        };
    }

    /** The best price a seller can get right now, house book included. */
    public static long instantSellPrice(CollectionType c) {
        Order best = bestBuyOrder(c);
        long house = Math.max(1L, Math.round(baseValue(c) * HOUSE_BUY_FACTOR));
        return best == null ? house : Math.max(house, best.unitPrice);
    }

    /** The best price a buyer must pay right now, house book included. */
    public static long instantBuyPrice(CollectionType c) {
        Order best = bestSellOrder(c);
        long house = Math.max(2L, Math.round(baseValue(c) * HOUSE_SELL_FACTOR));
        return best == null ? house : Math.min(house, best.unitPrice);
    }

    public static Order bestBuyOrder(CollectionType c) {
        return ORDERS.stream()
                .filter(o -> o.buy && o.product.equals(c.key()) && o.remaining() > 0)
                .max(Comparator.comparingLong(o -> o.unitPrice))
                .orElse(null);
    }

    public static Order bestSellOrder(CollectionType c) {
        return ORDERS.stream()
                .filter(o -> !o.buy && o.product.equals(c.key()) && o.remaining() > 0)
                .min(Comparator.comparingLong(o -> o.unitPrice))
                .orElse(null);
    }

    public static List<Order> book(CollectionType c, boolean buySide) {
        List<Order> out = new ArrayList<>();
        for (Order o : ORDERS) {
            if (o.buy == buySide && o.product.equals(c.key()) && o.remaining() > 0) out.add(o);
        }
        out.sort(buySide
                ? Comparator.comparingLong((Order o) -> o.unitPrice).reversed()
                : Comparator.comparingLong(o -> o.unitPrice));
        return out;
    }

    public static List<Order> ordersOf(ServerPlayer player) {
        List<Order> out = new ArrayList<>();
        String uuid = player.getUUID().toString();
        for (Order o : ORDERS) {
            // A fully filled buy order still belongs on this list until its goods are collected.
            if (o.ownerUuid.equals(uuid) && !o.finished()) out.add(o);
        }
        out.sort(Comparator.comparingLong((Order o) -> o.createdAt).reversed());
        return out;
    }

    // ── Trading ───────────────────────────────────────────────────────────

    /**
     * Sells from the player's inventory at the best price available, filling real buy orders first
     * and falling back to the house. Returns how many units actually moved.
     */
    public static int sellInstantly(ServerPlayer player, CollectionType c, int wanted) {
        int have = countInInventory(player, c);
        int toSell = Math.min(wanted, have);
        if (toSell <= 0) {
            player.sendSystemMessage(Component.literal("§cYou have none of that to sell."));
            return 0;
        }

        int sold = 0;
        long gross = 0;

        // Real orders first, best price down.
        for (Order o : book(c, true)) {
            if (sold >= toSell) break;
            int take = Math.min(o.remaining(), toSell - sold);
            o.filled += take;
            sold += take;
            gross += (long) take * o.unitPrice;
            deliverToOwner(o, c, take);
        }

        // Whatever is left goes to the house.
        if (sold < toSell) {
            int rest = toSell - sold;
            long house = Math.max(1L, Math.round(baseValue(c) * HOUSE_BUY_FACTOR));
            sold += rest;
            gross += (long) rest * house;
        }

        removeFromInventory(player, c, sold);
        long tax = gross * TAX_PERCENT / 100L;
        long net = gross - tax;
        CoinManager.giveCoins(player, net);

        player.sendSystemMessage(Component.literal("§aSold §f" + sold + " §a"
                + c.display + " §7for §6" + GuiKit.num(net) + " coins §8(after "
                + TAX_PERCENT + "% fee)"));
        prune();
        save();
        return sold;
    }

    /**
     * Buys at the best price available, filling real sell offers first. Returns how many units the
     * player actually received.
     */
    public static int buyInstantly(ServerPlayer player, CollectionType c, int wanted) {
        if (wanted <= 0) return 0;

        int bought = 0;
        long cost = 0;
        long balance = CoinManager.getCoinsLong(player);

        for (Order o : book(c, false)) {
            if (bought >= wanted) break;
            int take = Math.min(o.remaining(), wanted - bought);
            long lineCost = (long) take * o.unitPrice;
            if (cost + lineCost > balance) {
                // Take only what the player can actually pay for.
                take = (int) Math.max(0, (balance - cost) / Math.max(1, o.unitPrice));
                lineCost = (long) take * o.unitPrice;
                if (take <= 0) break;
            }
            o.filled += take;
            bought += take;
            cost += lineCost;
            payOwner(o, lineCost);
        }

        if (bought < wanted) {
            long house = Math.max(2L, Math.round(baseValue(c) * HOUSE_SELL_FACTOR));
            int affordable = (int) Math.max(0, (balance - cost) / house);
            int rest = Math.min(wanted - bought, affordable);
            if (rest > 0) {
                bought += rest;
                cost += (long) rest * house;
            }
        }

        if (bought <= 0) {
            player.sendSystemMessage(Component.literal("§cYou cannot afford any of that."));
            return 0;
        }

        CoinManager.removeCoins(player, cost);
        giveToPlayer(player, c, bought);
        player.sendSystemMessage(Component.literal("§aBought §f" + bought + " §a"
                + c.display + " §7for §6" + GuiKit.num(cost) + " coins"));
        prune();
        save();
        return bought;
    }

    /** Posts an order onto the book. Buy orders escrow the coins, sell offers escrow the items. */
    public static boolean postOrder(ServerPlayer player, CollectionType c, long unitPrice,
                                    int amount, boolean buy) {
        if (unitPrice <= 0 || amount <= 0) return false;

        if (buy) {
            long total = unitPrice * amount;
            if (!CoinManager.removeCoins(player, total)) {
                player.sendSystemMessage(Component.literal("§cThat would cost "
                        + GuiKit.num(total) + " coins and you do not have it."));
                return false;
            }
        } else {
            if (countInInventory(player, c) < amount) {
                player.sendSystemMessage(Component.literal("§cYou do not have " + amount
                        + " " + c.display + " to offer."));
                return false;
            }
            removeFromInventory(player, c, amount);
        }

        Order o = new Order();
        o.ownerUuid = player.getUUID().toString();
        o.ownerName = player.getName().getString();
        o.product = c.key();
        o.unitPrice = unitPrice;
        o.amount = amount;
        o.buy = buy;
        o.createdAt = System.currentTimeMillis();
        ORDERS.add(o);

        player.sendSystemMessage(Component.literal("§aPosted a " + (buy ? "buy order" : "sell offer")
                + " §7for §f" + amount + " " + c.display + " §7at §6" + unitPrice + " §7each."));
        save();
        return true;
    }

    /** Cancels an order and returns whatever was escrowed. */
    public static boolean cancel(ServerPlayer player, String orderId) {
        for (Order o : new ArrayList<>(ORDERS)) {
            if (!o.id.equals(orderId)) continue;
            if (!o.ownerUuid.equals(player.getUUID().toString())) return false;

            CollectionType c = CollectionType.byKey(o.product);
            int left = o.remaining();
            if (o.buy) {
                CoinManager.giveCoins(player, (long) left * o.unitPrice);
                // Anything already bought on this order is still theirs.
                int owed = o.claimable();
                if (owed > 0 && c != null) giveToPlayer(player, c, owed);
            } else if (c != null) {
                giveToPlayer(player, c, left);
            }
            ORDERS.remove(o);
            player.sendSystemMessage(Component.literal("§eOrder cancelled, escrow returned."));
            save();
            return true;
        }
        return false;
    }

    private static void payOwner(Order o, long coins) {
        long tax = coins * TAX_PERCENT / 100L;
        CoinManager.giveCoins(o.ownerUuid, coins - tax);
    }

    /**
     * Hands the goods to the buy order owner if they happen to be online, and otherwise leaves them
     * accruing on the order for them to claim later. Either way the units are never lost, which is
     * the part that matters when somebody has already paid for them.
     */
    private static void deliverToOwner(Order o, CollectionType c, int count) {
        o.filled = Math.min(o.amount, o.filled);
        ServerPlayer owner = onlinePlayer(o.ownerUuid);
        if (owner == null) return;
        giveToPlayer(owner, c, count);
        o.claimed = Math.min(o.filled, o.claimed + count);
        owner.sendSystemMessage(Component.literal("§aYour buy order filled: §f" + count
                + " " + c.display));
    }

    private static ServerPlayer onlinePlayer(String uuid) {
        try {
            if (PoliticalServer.server == null) return null;
            return PoliticalServer.server.getPlayerList().getPlayer(java.util.UUID.fromString(uuid));
        } catch (Exception e) {
            LOGGER.debug("BazaarManager.onlinePlayer: ignored an error", e);
            return null;
        }
    }

    /** Collects goods sitting on a filled buy order. */
    public static int claim(ServerPlayer player, String orderId) {
        for (Order o : ORDERS) {
            if (!o.id.equals(orderId)) continue;
            if (!o.ownerUuid.equals(player.getUUID().toString())) return 0;
            int owed = o.claimable();
            if (owed <= 0) return 0;
            CollectionType c = CollectionType.byKey(o.product);
            if (c == null) return 0;
            giveToPlayer(player, c, owed);
            o.claimed = o.filled;
            player.sendSystemMessage(Component.literal("§aCollected §f" + owed + " "
                    + c.display + "§a."));
            prune();
            save();
            return owed;
        }
        return 0;
    }

    private static void prune() {
        ORDERS.removeIf(Order::finished);
    }

    // ── Inventory helpers ─────────────────────────────────────────────────

    public static int countInInventory(ServerPlayer player, CollectionType c) {
        int n = 0;
        for (ItemStack st : player.getInventory().getNonEquipmentItems()) {
            if (st.isEmpty() || !st.is(c.material)) continue;
            if (SlayerItems.getCustomItemId(st) != null) continue;
            n += st.getCount();
        }
        return n;
    }

    private static void removeFromInventory(ServerPlayer player, CollectionType c, int count) {
        int left = count;
        for (ItemStack st : player.getInventory().getNonEquipmentItems()) {
            if (left <= 0) break;
            if (st.isEmpty() || !st.is(c.material)) continue;
            if (SlayerItems.getCustomItemId(st) != null) continue;
            int take = Math.min(left, st.getCount());
            st.shrink(take);
            left -= take;
        }
    }

    private static void giveToPlayer(ServerPlayer player, CollectionType c, int count) {
        int left = count;
        while (left > 0) {
            int stack = Math.min(left, c.material.getDefaultMaxStackSize());
            ItemStack give = new ItemStack(c.material, stack);
            if (!player.getInventory().add(give)) {
                player.drop(give, false, Prediction.SERVER_ONLY);
            }
            left -= stack;
        }
    }

    /** Every product the bazaar trades, which is every collection material. */
    public static List<CollectionType> productsIn(CollectionType.Field field) {
        return CollectionType.inField(field);
    }
}
