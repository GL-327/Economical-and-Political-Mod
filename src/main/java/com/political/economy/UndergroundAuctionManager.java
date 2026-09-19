package com.political.economy;
import com.political.DataManager;
import com.political.PoliticalServer;
import com.political.government.Treasury;



import com.political.bounty.SlayerItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public class UndergroundAuctionManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Timing constants
    private static final long AUCTION_INTERVAL_MS = 6L * 60L * 60L * 1000L; // 6 hours
    private static final long BID_TIMEOUT_MS = 30L * 1000L; // 30 seconds after last bid
    private static final long NO_BID_TIMEOUT_MS = 60L * 1000L; // 60 seconds with no bids
    private static final long MIN_ITEM_DURATION_MS = 20L * 1000L; // Minimum 20 seconds per item
    private static final int MAX_ITEMS_PER_AUCTION = 5;
    private static final long MIN_AUCTION_STARTING_BID = 1_000_000_000L; // 1b floor
    private static final double BROADCAST_RADIUS_SQUARED = 625.0; // 25^2 = 625

    // State variables
    private static long nextAuctionTime = 0;
    private static boolean auctionActive = false;
    private static List<AuctionItem> currentItems = new ArrayList<>();
    private static int currentItemIndex = 0;
    private static long lastBidTime = 0;
    private static long itemStartTime = 0;
    private static Set<UUID> auctioneerIds = new HashSet<>();
    private static boolean initialized = false;

    private static final Random random = new Random();

    public static class AuctionItem {
        public String name;
        public String itemType;
        public long startingBid;
        public long currentBid;
        public String highestBidderUuid;
        public String highestBidderName;
        public boolean sold;
        public transient ItemStack itemStack;

        public AuctionItem() {
        }

        public AuctionItem(String name, String itemType, long startingBid, ItemStack stack) {
            this.name = name;
            this.itemType = itemType;
            this.startingBid = startingBid;
            this.currentBid = startingBid;
            this.itemStack = stack;
            this.sold = false;
            this.highestBidderUuid = null;
            this.highestBidderName = null;
        }
    }

    static class SavedAuctionData {
        long nextAuctionTime;
        List<String> auctioneerIds = new ArrayList<>();
        Map<String, List<String>> pendingWins = new HashMap<>();
    }

    private static final Map<String, List<String>> pendingWins = new HashMap<>();

    // ═══════════════════════════════════════════════════════════════
    // PROXIMITY BROADCAST - Only within 25 blocks of auctioneer
    // ═══════════════════════════════════════════════════════════════

    private static void broadcastNearAuctioneers(MinecraftServer server, Component... messages) {
        if (server == null) return;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerLevel world = player.level();

            boolean nearAuctioneer = false;
            for (UUID auctioneerId : auctioneerIds) {
                var entity = world.getEntity(auctioneerId);
                if (entity != null && entity.distanceToSqr(player) <= 625) {
                    nearAuctioneer = true;
                    break;
                }
            }

            if (nearAuctioneer) {
                for (Component message : messages) {
                    player.sendSystemMessage(message);
                }
            }
        }
    }

    private static void giveWonItem(ServerPlayer winner, ItemStack stack, String name) {
        if (!winner.addItem(stack)) {
            winner.drop(stack, false);
        }
        winner.sendSystemMessage(Component.literal("You won " + name + "!").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
    }

    private static void queuePendingWin(String uuid, ItemStack stack) {
        if (uuid == null || uuid.isBlank() || stack == null || stack.isEmpty()) return;
        String encoded = encodeStack(stack);
        if (encoded == null) return;
        pendingWins.computeIfAbsent(uuid, k -> new ArrayList<>()).add(encoded);
        save(PoliticalServer.server);
    }

    public static void deliverPendingWins(ServerPlayer player) {
        if (player == null) return;
        List<String> encoded = pendingWins.remove(player.getUUID().toString());
        if (encoded == null || encoded.isEmpty()) return;
        for (String data : encoded) {
            ItemStack stack = decodeStack(data);
            if (stack == null || stack.isEmpty()) continue;
            if (!player.addItem(stack)) {
                player.drop(stack, false);
            }
        }
        player.sendSystemMessage(Component.literal("Delivered " + encoded.size() + " underground auction win(s).")
                .withStyle(ChatFormatting.GREEN));
        save(PoliticalServer.server);
    }

    private static String encodeStack(ItemStack stack) {
        if (PoliticalServer.server == null || stack == null || stack.isEmpty()) return null;
        try {
            var ops = net.minecraft.resources.RegistryOps.create(net.minecraft.nbt.NbtOps.INSTANCE, PoliticalServer.server.registryAccess());
            var result = ItemStack.CODEC.encodeStart(ops, stack);
            return result.result().map(net.minecraft.nbt.Tag::toString).orElse(null);
        } catch (Exception e) {
            PoliticalServer.LOGGER.error("Failed to store underground auction win", e);
            return null;
        }
    }

    private static ItemStack decodeStack(String data) {
        if (data == null || PoliticalServer.server == null) return ItemStack.EMPTY;
        try {
            net.minecraft.nbt.CompoundTag parsed = net.minecraft.nbt.TagParser.parseCompoundFully(data);
            var ops = net.minecraft.resources.RegistryOps.create(net.minecraft.nbt.NbtOps.INSTANCE, PoliticalServer.server.registryAccess());
            return ItemStack.CODEC.parse(ops, parsed).result().orElse(ItemStack.EMPTY);
        } catch (Exception e) {
            PoliticalServer.LOGGER.error("Failed to load underground auction win", e);
            return ItemStack.EMPTY;
        }
    }

    public static boolean isPlayerNearAuctioneer(ServerPlayer player) {
        if (player == null) return false;

        ServerLevel world = player.level();

        for (UUID auctioneerId : auctioneerIds) {
            Entity entity = world.getEntity(auctioneerId);
            if (entity != null && entity.distanceToSqr(player) <= BROADCAST_RADIUS_SQUARED) {
                return true;
            }
        }
        return false;
    }

    // ═══════════════════════════════════════════════════════════════
    // LOAD / SAVE
    // ═══════════════════════════════════════════════════════════════

    public static void load(MinecraftServer server) {
        Path path = server.getWorldPath(LevelResource.ROOT).resolve("underground_auction.json");
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                SavedAuctionData data = GSON.fromJson(reader, SavedAuctionData.class);
                if (data != null) {
                    nextAuctionTime = data.nextAuctionTime;
                    auctioneerIds.clear();
                    if (data.auctioneerIds != null) {
                        for (String id : data.auctioneerIds) {
                            try {
                                auctioneerIds.add(UUID.fromString(id));
                            } catch (Exception ignored) {
                            }
                        }
                    }
                    pendingWins.clear();
                    if (data.pendingWins != null) {
                        pendingWins.putAll(data.pendingWins);
                    }
                }
            } catch (Exception e) {
                PoliticalServer.LOGGER.error("Failed to load underground auction data", e);
            }
        }

        if (nextAuctionTime == 0 || nextAuctionTime < System.currentTimeMillis()) {
            nextAuctionTime = System.currentTimeMillis() + AUCTION_INTERVAL_MS;
        }

        initialized = true;
    }

    public static void save(MinecraftServer server) {
        if (server == null) return;
        Path path = server.getWorldPath(LevelResource.ROOT).resolve("underground_auction.json");
        try {
            Files.createDirectories(path.getParent());
            SavedAuctionData data = new SavedAuctionData();
            data.nextAuctionTime = nextAuctionTime;
            data.auctioneerIds = new ArrayList<>();
            for (UUID id : auctioneerIds) {
                data.auctioneerIds.add(id.toString());
            }
            data.pendingWins = new HashMap<>(pendingWins);
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(data, writer);
            }
        } catch (Exception e) {
            PoliticalServer.LOGGER.error("Failed to save underground auction data", e);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // AUCTIONEER NPC
    // ═══════════════════════════════════════════════════════════════

    public static Villager spawnAuctioneer(ServerLevel world, double x, double y, double z, float yaw) {
        Villager villager = new Villager(EntityType.VILLAGER, world);

        villager.setPos(x, y, z);
        villager.setYRot(yaw);
        villager.setXRot(0f);
        villager.setCustomName(Component.literal("Underground Auctioneer").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        villager.setCustomNameVisible(true);
        villager.setInvulnerable(true);
        villager.setNoAi(true);
        villager.setSilent(true);
        villager.setNoGravity(false);
        villager.setPersistenceRequired();
        villager.setHealth(villager.getMaxHealth());
        villager.removeAllEffects();

        villager.setVillagerData(villager.getVillagerData()
                .withProfession(BuiltInRegistries.VILLAGER_PROFESSION.getOrThrow(VillagerProfession.NITWIT))
                .withLevel(5));

        villager.addTag("political_underground_auctioneer");

        world.addFreshEntity(villager);
        auctioneerIds.add(villager.getUUID());
        save(PoliticalServer.server);

        return villager;
    }

    public static boolean isAuctioneer(Villager villager) {
        if (villager == null) return false;
        return villager.entityTags().contains("political_underground_auctioneer")
                || auctioneerIds.contains(villager.getUUID());
    }

    public static void removeAuctioneer(Villager villager) {
        if (isAuctioneer(villager)) {
            auctioneerIds.remove(villager.getUUID());
            villager.discard();
            save(PoliticalServer.server);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // TICK
    // ═══════════════════════════════════════════════════════════════

    public static void tick(MinecraftServer server) {
        if (!initialized) return;

        long now = System.currentTimeMillis();

        if (!auctionActive && now >= nextAuctionTime) {
            startAuction(server);
            return;
        }

        if (auctionActive) {
            processActiveAuction(server, now);
        }
    }

    private static void processActiveAuction(MinecraftServer server, long now) {
        if (currentItems == null || currentItems.isEmpty() || currentItemIndex >= currentItems.size()) {
            endAuction(server);
            return;
        }

        AuctionItem current = currentItems.get(currentItemIndex);
        if (current == null) {
            currentItemIndex++;
            if (currentItemIndex < currentItems.size()) {
                announceCurrentItem(server);
            } else {
                endAuction(server);
            }
            return;
        }

        long itemDuration = now - itemStartTime;

        if (itemDuration < MIN_ITEM_DURATION_MS) {
            return;
        }

        if (current.highestBidderUuid != null && !current.highestBidderUuid.isEmpty()) {
            if (now - lastBidTime >= BID_TIMEOUT_MS) {
                sellCurrentItem(server);
            }
        } else {
            if (itemDuration >= NO_BID_TIMEOUT_MS) {
                skipCurrentItem(server);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // AUCTION LIFECYCLE
    // ═══════════════════════════════════════════════════════════════

    public static void startAuction(MinecraftServer server) {
        if (server == null) return;

        currentItems = generateAuctionItems();

        if (currentItems == null || currentItems.isEmpty()) {
            nextAuctionTime = System.currentTimeMillis() + AUCTION_INTERVAL_MS;
            save(server);
            return;
        }

        auctionActive = true;
        currentItemIndex = 0;
        long now = System.currentTimeMillis();
        lastBidTime = now;
        itemStartTime = now;

        broadcastNearAuctioneers(server,
                Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.DARK_PURPLE),
                Component.literal("  🌙 UNDERGROUND AUCTION STARTING! 🌙").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD),
                Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.DARK_PURPLE),
                Component.literal("Interact with the Auctioneer to bid!").withStyle(ChatFormatting.GRAY),
                Component.literal(currentItems.size() + " rare items up for bidding!").withStyle(ChatFormatting.YELLOW),
                Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.DARK_PURPLE)
        );

        announceCurrentItem(server);
        nextAuctionTime = System.currentTimeMillis() + AUCTION_INTERVAL_MS;
        save(server);
    }

    private static void announceCurrentItem(MinecraftServer server) {
        if (server == null || currentItemIndex >= currentItems.size()) {
            endAuction(server);
            return;
        }

        AuctionItem item = currentItems.get(currentItemIndex);
        if (item == null) {
            currentItemIndex++;
            if (currentItemIndex < currentItems.size()) {
                announceCurrentItem(server);
            } else {
                endAuction(server);
            }
            return;
        }

        long now = System.currentTimeMillis();
        itemStartTime = now;
        lastBidTime = now;

        broadcastNearAuctioneers(server,
                Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━").withStyle(ChatFormatting.DARK_PURPLE),
                Component.literal("📦 NOW BIDDING: " + item.name).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD),
                Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━").withStyle(ChatFormatting.DARK_PURPLE),
                Component.literal("💰 Starting Bid: " + item.startingBid + " coins").withStyle(ChatFormatting.GOLD),
                Component.literal("📋 Item " + (currentItemIndex + 1) + " of " + currentItems.size()).withStyle(ChatFormatting.GRAY),
                Component.literal("⏱ " + (NO_BID_TIMEOUT_MS / 1000) + " seconds to bid!").withStyle(ChatFormatting.YELLOW),
                Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━").withStyle(ChatFormatting.DARK_PURPLE)
        );
    }

    private static void sellCurrentItem(MinecraftServer server) {
        if (server == null || currentItemIndex >= currentItems.size()) {
            endAuction(server);
            return;
        }

        AuctionItem item = currentItems.get(currentItemIndex);
        if (item == null) {
            currentItemIndex++;
            advanceToNextItem(server);
            return;
        }

        item.sold = true;

        if (item.highestBidderUuid != null && !item.highestBidderUuid.isEmpty()) {
            try {
                ServerPlayer winner = server.getPlayerList().getPlayer(UUID.fromString(item.highestBidderUuid));

                broadcastNearAuctioneers(server,
                        Component.literal("🔨 SOLD! " + item.name).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD),
                        Component.literal("   Winner: " + item.highestBidderName + " for " + item.currentBid + " coins").withStyle(ChatFormatting.YELLOW)
                );

                long vat = Treasury.vatOn(item.currentBid);
                if (vat > 0) {
                    Treasury.deposit(vat, "uah vat");
                }
                if (item.itemStack != null && !item.itemStack.isEmpty()) {
                    if (winner != null) {
                        giveWonItem(winner, item.itemStack.copy(), item.name);
                    } else {
                        queuePendingWin(item.highestBidderUuid, item.itemStack.copy());
                    }
                }
            } catch (Exception e) {
                PoliticalServer.LOGGER.error("Error giving item to winner", e);
            }
        }

        currentItemIndex++;
        advanceToNextItem(server);
    }

    private static void skipCurrentItem(MinecraftServer server) {
        if (server == null || currentItemIndex >= currentItems.size()) {
            endAuction(server);
            return;
        }

        AuctionItem item = currentItems.get(currentItemIndex);
        String itemName = item != null ? item.name : "Unknown Item";

        broadcastNearAuctioneers(server, Component.literal("❌ NO BIDS - " + itemName + " skipped!").withStyle(ChatFormatting.RED));

        currentItemIndex++;
        advanceToNextItem(server);
    }

    private static void advanceToNextItem(MinecraftServer server) {
        if (currentItemIndex < currentItems.size()) {
            broadcastNearAuctioneers(server, Component.literal("⏳ Next item coming up...").withStyle(ChatFormatting.GRAY));
            announceCurrentItem(server);
        } else {
            endAuction(server);
        }
    }

    private static void endAuction(MinecraftServer server) {
        if (server == null) return;

        auctionActive = false;

        int soldCount = 0;
        if (currentItems != null) {
            for (AuctionItem item : currentItems) {
                if (item != null && item.sold) {
                    soldCount++;
                }
            }
        }

        currentItems = new ArrayList<>();
        currentItemIndex = 0;

        int finalSoldCount = soldCount;
        broadcastNearAuctioneers(server,
                Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.DARK_PURPLE),
                Component.literal("  🌙 UNDERGROUND AUCTION ENDED 🌙").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD),
                Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.DARK_PURPLE),
                Component.literal("Items sold: " + finalSoldCount).withStyle(ChatFormatting.YELLOW),
                Component.literal("Next auction in: " + PoliticalServer.formatTime(getTimeUntilNextAuction())).withStyle(ChatFormatting.GRAY),
                Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.DARK_PURPLE)
        );

        save(server);
    }

    // ═══════════════════════════════════════════════════════════════
    // BIDDING
    // ═══════════════════════════════════════════════════════════════

    public static boolean placeBid(ServerPlayer player, long amount) {
        if (player == null) return false;

        if (!auctionActive) {
            player.sendSystemMessage(Component.literal("❌ No auction is currently active!").withStyle(ChatFormatting.RED));
            return false;
        }

        if (currentItems == null || currentItemIndex >= currentItems.size()) {
            player.sendSystemMessage(Component.literal("❌ No item is currently up for auction!").withStyle(ChatFormatting.RED));
            return false;
        }

        AuctionItem item = currentItems.get(currentItemIndex);
        if (item == null) {
            player.sendSystemMessage(Component.literal("❌ Error: Invalid auction item!").withStyle(ChatFormatting.RED));
            return false;
        }

        if (amount <= item.currentBid) {
            player.sendSystemMessage(Component.literal("❌ Bid must be higher than " + item.currentBid + " coins!").withStyle(ChatFormatting.RED));
            return false;
        }

        long vat = Treasury.vatOn(amount);
        long due = amount + vat;
        long playerCoins = CoinManager.getCoinsLong(player);
        if (playerCoins < due) {
            player.sendSystemMessage(Component.literal("❌ Not enough coins! You have: " + playerCoins + ", Need: " + due + " (bid + VAT)").withStyle(ChatFormatting.RED));
            return false;
        }

        // Refund previous bidder
        if (item.highestBidderUuid != null && !item.highestBidderUuid.isEmpty()) {
            try {
                long previousDue = item.currentBid + Treasury.vatOn(item.currentBid);
                DataManager.addCoins(item.highestBidderUuid, previousDue);
                ServerPlayer previousBidder = PoliticalServer.server.getPlayerList().getPlayer(UUID.fromString(item.highestBidderUuid));
                if (previousBidder != null) {
                    previousBidder.sendSystemMessage(Component.literal("⚠ You were outbid! Your " + item.currentBid + " coins have been refunded.").withStyle(ChatFormatting.YELLOW));
                }
            } catch (Exception e) {
                PoliticalServer.LOGGER.error("Error refunding previous bidder", e);
            }
        }

        if (!CoinManager.removeCoins(player, due)) {
            player.sendSystemMessage(Component.literal("❌ Failed to process bid!").withStyle(ChatFormatting.RED));
            return false;
        }

        item.currentBid = amount;
        item.highestBidderUuid = player.getUUID().toString();
        item.highestBidderName = player.getName().getString();
        lastBidTime = System.currentTimeMillis();

        // Broadcast only to players near auctioneers
        broadcastNearAuctioneers(PoliticalServer.server,
                Component.literal("💰 NEW BID: " + amount + " coins by " + player.getName().getString()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                Component.literal("   ⏱ " + (BID_TIMEOUT_MS / 1000) + "s until sold!").withStyle(ChatFormatting.RED)
        );

        player.sendSystemMessage(Component.literal("✓ Bid placed successfully!").withStyle(ChatFormatting.GREEN));
        return true;
    }

    // ═══════════════════════════════════════════════════════════════
    // ITEM GENERATION
    // ═══════════════════════════════════════════════════════════════

    private static List<AuctionItem> generateAuctionItems() {
        List<AuctionItem> allPossible = new ArrayList<>();

        try {
                        allPossible.add(createUltimatePotion());
            allPossible.add(createHarveysStick());
            allPossible.add(createTheGavel());
            allPossible.add(createHermesShoes());
            allPossible.add(createHPEBM());
            allPossible.add(createEnchantedElytra());
            allPossible.add(createSuperBow());
            allPossible.add(createFortunePick());
            allPossible.add(createWardenCore());
            allPossible.add(createDragonCrafterCoreT1());
            allPossible.add(createDragonEgg());
            allPossible.removeIf(Objects::isNull);
            Collections.shuffle(allPossible);

            int count = Math.min(MAX_ITEMS_PER_AUCTION, allPossible.size());
            return new ArrayList<>(allPossible.subList(0, count));
        } catch (Exception e) {
            PoliticalServer.LOGGER.error("Error generating auction items", e);
            return new ArrayList<>();
        }
    }

    private static long normalizeStartingBid(long bid) {
        return Math.max(MIN_AUCTION_STARTING_BID, bid);
    }

    private static CompoundTag createCustomNbt(String key, boolean value) {
        CompoundTag nbt = new CompoundTag();
        nbt.putByte(key, (byte) (value ? 1 : 0));  // Changed from putBoolean
        return nbt;
    }
    private static AuctionItem createWardenCore() {
        ItemStack stack = new ItemStack(Items.ECHO_SHARD);

        String name = "Warden's Core";
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name).withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD));

        CompoundTag nbt = new CompoundTag();
        nbt.putByte("warden_core", (byte) 1);
        stack.set(DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.of(nbt));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ ANCIENT ARTIFACT ◆").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("A pulsing core of sonic energy").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal("harvested from the depths.").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Drop Rate: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("0.1%").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .append(Component.literal(" from Wardens").withStyle(ChatFormatting.GRAY)));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Used to craft Ultra weapons").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal(""));
        lore.add(Component.literal("「Crafting Material」").withStyle(ChatFormatting.DARK_AQUA));
        stack.set(DataComponents.LORE, new ItemLore(lore));

        return new AuctionItem(name, "special", normalizeStartingBid(15000000), stack);
    }

    private static AuctionItem createDragonCrafterCoreT1() {
        ItemStack core = SlayerItems.createDragonCrafterCoreT1();
        String name = "Dragon Crafting Core (T1)";
        // Starting price: 2.5b
        return new AuctionItem(name, "core", normalizeStartingBid(2_500_000_000L), core);
    }

    private static AuctionItem createDragonEgg() {
        ItemStack egg = new ItemStack(Items.DRAGON_EGG);
        egg.set(DataComponents.CUSTOM_NAME, Component.literal("Dragon Egg").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[Mythic resource]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Used in high-tier draconic crafting.").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("「Underground Auction」").withStyle(ChatFormatting.DARK_PURPLE));
        egg.set(DataComponents.LORE, new ItemLore(lore));

        egg.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

        String name = "Dragon Egg";
        // Starting price: 15b
        return new AuctionItem(name, "special", normalizeStartingBid(15_000_000_000L), egg);
    }

    
    private static AuctionItem createUltimatePotion() {
        ItemStack stack = new ItemStack(Items.POTION);

        List<MobEffectInstance> effects = new ArrayList<>();
        effects.add(new MobEffectInstance(MobEffects.SPEED, 12000, 2));
        effects.add(new MobEffectInstance(MobEffects.STRENGTH, 12000, 2));
        effects.add(new MobEffectInstance(MobEffects.REGENERATION, 12000, 1));
        effects.add(new MobEffectInstance(MobEffects.RESISTANCE, 12000, 1));
        effects.add(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 12000, 0));
        effects.add(new MobEffectInstance(MobEffects.WATER_BREATHING, 12000, 0));
        effects.add(new MobEffectInstance(MobEffects.NIGHT_VISION, 12000, 0));
        effects.add(new MobEffectInstance(MobEffects.HASTE, 12000, 2));
        effects.add(new MobEffectInstance(MobEffects.JUMP_BOOST, 12000, 2));
        effects.add(new MobEffectInstance(MobEffects.ABSORPTION, 12000, 4));

        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(
                Optional.empty(), Optional.of(0xFF00FF), effects, Optional.empty()
        ));

        String name = "Elixir of the Gods";
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("All positive effects for 10 minutes").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("From the Underground Auction").withStyle(ChatFormatting.DARK_PURPLE));
        stack.set(DataComponents.LORE, new ItemLore(lore));

        return new AuctionItem(name, "potion", normalizeStartingBid(15000000), stack);
    }

    private static AuctionItem createHarveysStick() {
        ItemStack stack = new ItemStack(Items.STICK);

        String name = "Harvey's Stick";
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        stack.set(DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.of(createCustomNbt("harveys_stick", true)));

        addEnchantment(stack, Enchantments.KNOCKBACK, 2);

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ LEGENDARY WEAPON ◆").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Attack: Lightning Strike").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("  └ Summons lightning on hit").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("  └ Does not harm wielder").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Damage: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("Lightning Strike").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)));
        lore.add(Component.literal(""));
        lore.add(Component.literal("「Underground Auction」").withStyle(ChatFormatting.DARK_PURPLE));
        stack.set(DataComponents.LORE, new ItemLore(lore));

        return new AuctionItem(name, "special", normalizeStartingBid(15000000), stack);
    }

    private static AuctionItem createTheGavel() {
        ItemStack stack = new ItemStack(Items.MACE);

        String name = "The Gavel";
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));

        addEnchantment(stack, Enchantments.DENSITY, 10);
        addEnchantment(stack, Enchantments.BREACH, 8);
        addEnchantment(stack, Enchantments.UNBREAKING, 3);
        addEnchantment(stack, Enchantments.MENDING, 1);

        stack.set(DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.of(createCustomNbt("the_gavel", true)));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ JUDICIAL AUTHORITY ◆").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Right-click: Gavel Strike").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("  └ AOE explosion (4.5 block radius)").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("  └ Consumes 1 Wind Charge").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("  └ 3s cooldown").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Damage: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("25").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)));
        lore.add(Component.literal(""));
        lore.add(Component.literal("「Underground Auction」").withStyle(ChatFormatting.DARK_PURPLE));
        stack.set(DataComponents.LORE, new ItemLore(lore));

        return new AuctionItem(name, "special", normalizeStartingBid(15000000), stack);
    }

    private static AuctionItem createHermesShoes() {
        ItemStack stack = new ItemStack(Items.IRON_BOOTS);

        String name = "Hermes' Shoes";
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));

        addEnchantment(stack, Enchantments.FROST_WALKER, 3);
        addEnchantment(stack, Enchantments.UNBREAKING, 3);
        addEnchantment(stack, Enchantments.MENDING, 1);
        addEnchantment(stack, Enchantments.FEATHER_FALLING, 4);
        addEnchantment(stack, Enchantments.DEPTH_STRIDER, 3);

        stack.set(DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.of(createCustomNbt("hermes_shoes", true)));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ DIVINE FOOTWEAR ◆").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Blessed by the messenger god.").withStyle(ChatFormatting.DARK_AQUA));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Passive: Swift as the Wind").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("  └ Permanent Speed III while worn").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Speed Bonus: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("+60%").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
        lore.add(Component.literal(""));
        lore.add(Component.literal("「Underground Auction」").withStyle(ChatFormatting.DARK_PURPLE));
        stack.set(DataComponents.LORE, new ItemLore(lore));

        return new AuctionItem(name, "special", normalizeStartingBid(15000000), stack);
    }

    private static AuctionItem createHPEBM() {
        ItemStack stack = new ItemStack(Items.IRON_SHOVEL);

        String name = "H.P.E.B.M.";
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));

        CompoundTag nbt = new CompoundTag();
        nbt.putByte("hpebm", (byte) 1);
        stack.set(DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.of(nbt));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ PLASMA WEAPON ◆").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("High Powered Energy Based Plasma Emitter").withStyle(ChatFormatting.DARK_GREEN));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Right-click: Continuous beam attack").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("  └ Costs 1 XP level per second").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Damage: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("1.0").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .append(Component.literal(" per tick").withStyle(ChatFormatting.GRAY)));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Upgradeable with Warden's Core").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal(""));
        lore.add(Component.literal("「Underground Auction」").withStyle(ChatFormatting.DARK_PURPLE));
        stack.set(DataComponents.LORE, new ItemLore(lore));

        return new AuctionItem(name, "special", normalizeStartingBid(15000000), stack);
    }

    private static AuctionItem createEnchantedElytra() {
        ItemStack stack = new ItemStack(Items.ELYTRA);

        String name = "Wings of the Void";
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));

        addEnchantment(stack, Enchantments.UNBREAKING, 5);
        addEnchantment(stack, Enchantments.MENDING, 1);

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ LEGENDARY WINGS ◆").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Torn from the fabric of reality.").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Unbreaking: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("V").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)));
        lore.add(Component.literal("Mending: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("I").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Nearly indestructible").withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        lore.add(Component.literal("「Underground Auction」").withStyle(ChatFormatting.DARK_PURPLE));
        stack.set(DataComponents.LORE, new ItemLore(lore));

        return new AuctionItem(name, "armor", normalizeStartingBid(15000000), stack);
    }

    private static AuctionItem createSuperBow() {
        ItemStack stack = new ItemStack(Items.BOW);

        String name = "Apollo's Bow";
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        addEnchantment(stack, Enchantments.POWER, 7);
        addEnchantment(stack, Enchantments.INFINITY, 1);
        addEnchantment(stack, Enchantments.FLAME, 1);
        addEnchantment(stack, Enchantments.PUNCH, 3);
        addEnchantment(stack, Enchantments.UNBREAKING, 3);

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ DIVINE WEAPON ◆").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Blessed by the sun god.").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Power: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("VII").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)));
        lore.add(Component.literal("Punch: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("III").withStyle(ChatFormatting.AQUA)));
        lore.add(Component.literal("Flame: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("I").withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal("Infinity: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("I").withStyle(ChatFormatting.LIGHT_PURPLE)));
        lore.add(Component.literal(""));
        lore.add(Component.literal("「Underground Auction」").withStyle(ChatFormatting.DARK_PURPLE));
        stack.set(DataComponents.LORE, new ItemLore(lore));

        return new AuctionItem(name, "weapon", normalizeStartingBid(15000000), stack);
    }

    private static AuctionItem createFortunePick() {
        ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);

        int fortuneLevel = 5 + random.nextInt(3);
        String name = "Miner's Dream";
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));

        addEnchantment(stack, Enchantments.FORTUNE, fortuneLevel);
        addEnchantment(stack, Enchantments.EFFICIENCY, 6);
        addEnchantment(stack, Enchantments.UNBREAKING, 3);
        addEnchantment(stack, Enchantments.MENDING, 1);

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ LEGENDARY TOOL ◆").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("The ultimate mining tool.").withStyle(ChatFormatting.DARK_AQUA));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Fortune: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(toRoman(fortuneLevel)).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
        lore.add(Component.literal("Efficiency: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("VI").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal("Unbreaking: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("III").withStyle(ChatFormatting.AQUA)));
        lore.add(Component.literal("Mending: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("I").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        lore.add(Component.literal("「Underground Auction」").withStyle(ChatFormatting.DARK_PURPLE));
        stack.set(DataComponents.LORE, new ItemLore(lore));

        return new AuctionItem(
                name + " (Fortune " + fortuneLevel + ")",
                "tool",
                normalizeStartingBid(15000000 + (fortuneLevel * 1000000)),
                stack
        );
    }

    private static void addEnchantment(ItemStack stack, ResourceKey<Enchantment> enchantmentKey, int level) {
        if (PoliticalServer.server == null) return;

        var registryManager = PoliticalServer.server.registryAccess();
        var enchantmentRegistry = registryManager.lookupOrThrow(Registries.ENCHANTMENT);
        Holder<Enchantment> holder = enchantmentRegistry.getOrThrow(enchantmentKey);

        var stored = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        var mutable = new ItemEnchantments.Mutable(stored);
        mutable.set(holder, level);
        stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
    }

    private static String toRoman(int num) {
        return switch (num) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            case 10 -> "X";
            default -> String.valueOf(num);
        };
    }

    public static boolean isAuctionActive() {
        return auctionActive;
    }

    public static AuctionItem getCurrentItem() {
        if (!auctionActive || currentItems == null || currentItemIndex >= currentItems.size()) return null;
        return currentItems.get(currentItemIndex);
    }

    public static int getCurrentItemIndex() {
        return currentItemIndex;
    }

    public static int getTotalItems() {
        return currentItems != null ? currentItems.size() : 0;
    }

    public static long getTimeUntilNextAuction() {
        return Math.max(0, nextAuctionTime - System.currentTimeMillis());
    }

    public static int getSecondsUntilTimeout() {
        if (!auctionActive) return 0;

        long now = System.currentTimeMillis();
        AuctionItem current = getCurrentItem();
        if (current == null) return 0;

        long itemDuration = now - itemStartTime;

        if (current.highestBidderUuid != null && !current.highestBidderUuid.isEmpty()) {
            // Someone has bid - countdown from last bid time
            long timeSinceLastBid = now - lastBidTime;
            long remaining = BID_TIMEOUT_MS - timeSinceLastBid;
            return (int) Math.max(0, remaining / 1000);
        } else {
            // No bids yet - countdown from item start
            long remaining = NO_BID_TIMEOUT_MS - itemDuration;
            return (int) Math.max(0, remaining / 1000);
        }
    }
        public static void skipCooldown () {
            nextAuctionTime = System.currentTimeMillis();
            save(PoliticalServer.server);
        }

        public static void forceStartAuction () {
            if (PoliticalServer.server != null) {
                startAuction(PoliticalServer.server);
            }
        }

        public static void forceStartAuction (MinecraftServer server){
            if (auctionActive) {
                endAuction(server);
            }
            nextAuctionTime = System.currentTimeMillis();
            startAuction(server);
        }

        public static List<AuctionItem> getAllPossibleItems () {
            return generateAuctionItems();
        }

        public static void giveAuctionItem (ServerPlayer player,int itemIndex){
            List<AuctionItem> items = generateAuctionItems();
            if (itemIndex >= 0 && itemIndex < items.size()) {
                AuctionItem item = items.get(itemIndex);
                if (item.itemStack != null) {
                    player.addItem(item.itemStack.copy());
                    player.sendSystemMessage(Component.literal("✓ Received: " + item.name).withStyle(ChatFormatting.GREEN));
                }
            }
        }

        public static List<String> getAuctionItemNames () {
            List<String> names = new ArrayList<>();
            List<AuctionItem> items = generateAuctionItems();
            for (AuctionItem item : items) {
                names.add(item.name);
            }
            return names;
        }

        public static void forceEndAuction () {
            if (PoliticalServer.server != null && auctionActive) {
                endAuction(PoliticalServer.server);
            }
        }
    }

