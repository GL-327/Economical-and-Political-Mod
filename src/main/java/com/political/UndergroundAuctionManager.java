package com.political;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.WorldSavePath;
import net.minecraft.village.VillagerProfession;
import net.minecraft.registry.Registries;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.properties.Property;


public class UndergroundAuctionManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Timing constants
    private static final long AUCTION_INTERVAL_MS = 6L * 60L * 60L * 1000L; // 6 hours
    private static final long BID_TIMEOUT_MS = 30L * 1000L; // 30 seconds after last bid
    private static final long NO_BID_TIMEOUT_MS = 60L * 1000L; // 60 seconds with no bids
    private static final long MIN_ITEM_DURATION_MS = 20L * 1000L; // Minimum 20 seconds per item
    private static final int MAX_ITEMS_PER_AUCTION = 5;
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
        public int startingBid;
        public int currentBid;
        public String highestBidderUuid;
        public String highestBidderName;
        public boolean sold;
        public transient ItemStack itemStack;

        public AuctionItem() {
        }

        public AuctionItem(String name, String itemType, int startingBid, ItemStack stack) {
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
    }

    // ═══════════════════════════════════════════════════════════════
    // PROXIMITY BROADCAST - Only within 25 blocks of auctioneer
    // ═══════════════════════════════════════════════════════════════

    private static void broadcastNearAuctioneers(MinecraftServer server, Text... messages) {
        if (server == null) return;

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerWorld world = player.getEntityWorld();

            boolean nearAuctioneer = false;
            for (UUID auctioneerId : auctioneerIds) {
                var entity = world.getEntity(auctioneerId);
                if (entity != null && entity.squaredDistanceTo(player) <= 625) {
                    nearAuctioneer = true;
                    break;
                }
            }

            if (nearAuctioneer) {
                for (Text message : messages) {
                    player.sendMessage(message);
                }
            }
        }
    }

    public static boolean isPlayerNearAuctioneer(ServerPlayerEntity player) {
        if (player == null) return false;

        ServerWorld world = player.getEntityWorld();

        for (UUID auctioneerId : auctioneerIds) {
            Entity entity = world.getEntity(auctioneerId);
            if (entity != null && entity.squaredDistanceTo(player) <= BROADCAST_RADIUS_SQUARED) {
                return true;
            }
        }
        return false;
    }

    // ═══════════════════════════════════════════════════════════════
    // LOAD / SAVE
    // ═══════════════════════════════════════════════════════════════

    public static void load(MinecraftServer server) {
        Path path = server.getSavePath(WorldSavePath.ROOT).resolve("underground_auction.json");
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
        Path path = server.getSavePath(WorldSavePath.ROOT).resolve("underground_auction.json");
        try {
            Files.createDirectories(path.getParent());
            SavedAuctionData data = new SavedAuctionData();
            data.nextAuctionTime = nextAuctionTime;
            data.auctioneerIds = new ArrayList<>();
            for (UUID id : auctioneerIds) {
                data.auctioneerIds.add(id.toString());
            }
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

    public static VillagerEntity spawnAuctioneer(ServerWorld world, double x, double y, double z, float yaw) {
        VillagerEntity villager = new VillagerEntity(EntityType.VILLAGER, world);

        villager.refreshPositionAndAngles(x, y, z, yaw, 0);
        villager.setCustomName(Text.literal("Underground Auctioneer").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        villager.setCustomNameVisible(true);
        villager.setInvulnerable(true);
        villager.setAiDisabled(true);
        villager.setSilent(true);
        villager.setNoGravity(false);
        villager.setPersistent();
        villager.setHealth(villager.getMaxHealth());
        villager.clearStatusEffects();

        villager.setVillagerData(villager.getVillagerData()
                .withProfession(Registries.VILLAGER_PROFESSION.getOrThrow(VillagerProfession.NITWIT))
                .withLevel(5));

        villager.addCommandTag("underground_auctioneer");

        world.spawnEntity(villager);
        auctioneerIds.add(villager.getUuid());
        save(PoliticalServer.server);

        return villager;
    }

    public static boolean isAuctioneer(VillagerEntity villager) {
        if (villager == null) return false;
        return villager.getCommandTags().contains("underground_auctioneer")
                || auctioneerIds.contains(villager.getUuid());
    }

    public static void removeAuctioneer(VillagerEntity villager) {
        if (isAuctioneer(villager)) {
            auctioneerIds.remove(villager.getUuid());
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
                Text.literal("═══════════════════════════════════").formatted(Formatting.DARK_PURPLE),
                Text.literal("  🌙 UNDERGROUND AUCTION STARTING! 🌙").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD),
                Text.literal("═══════════════════════════════════").formatted(Formatting.DARK_PURPLE),
                Text.literal("Interact with the Auctioneer to bid!").formatted(Formatting.GRAY),
                Text.literal(currentItems.size() + " rare items up for bidding!").formatted(Formatting.YELLOW),
                Text.literal("═══════════════════════════════════").formatted(Formatting.DARK_PURPLE)
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
                Text.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━").formatted(Formatting.DARK_PURPLE),
                Text.literal("📦 NOW BIDDING: " + item.name).formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD),
                Text.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━").formatted(Formatting.DARK_PURPLE),
                Text.literal("💰 Starting Bid: " + item.startingBid + " credits").formatted(Formatting.GOLD),
                Text.literal("📋 Item " + (currentItemIndex + 1) + " of " + currentItems.size()).formatted(Formatting.GRAY),
                Text.literal("⏱ " + (NO_BID_TIMEOUT_MS / 1000) + " seconds to bid!").formatted(Formatting.YELLOW),
                Text.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━").formatted(Formatting.DARK_PURPLE)
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
                ServerPlayerEntity winner = server.getPlayerManager().getPlayer(UUID.fromString(item.highestBidderUuid));

                broadcastNearAuctioneers(server,
                        Text.literal("🔨 SOLD! " + item.name).formatted(Formatting.GREEN, Formatting.BOLD),
                        Text.literal("   Winner: " + item.highestBidderName + " for " + item.currentBid + " credits").formatted(Formatting.YELLOW)
                );

                if (winner != null && item.itemStack != null && !item.itemStack.isEmpty()) {
                    ItemStack itemToGive = item.itemStack.copy();
                    if (!winner.getInventory().insertStack(itemToGive)) {
                        winner.dropItem(itemToGive, false);
                    }
                    winner.sendMessage(Text.literal("✓ You won " + item.name + "!").formatted(Formatting.GREEN, Formatting.BOLD));
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

        broadcastNearAuctioneers(server, Text.literal("❌ NO BIDS - " + itemName + " skipped!").formatted(Formatting.RED));

        currentItemIndex++;
        advanceToNextItem(server);
    }

    private static void advanceToNextItem(MinecraftServer server) {
        if (currentItemIndex < currentItems.size()) {
            broadcastNearAuctioneers(server, Text.literal("⏳ Next item coming up...").formatted(Formatting.GRAY));
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
                Text.literal("═══════════════════════════════════").formatted(Formatting.DARK_PURPLE),
                Text.literal("  🌙 UNDERGROUND AUCTION ENDED 🌙").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD),
                Text.literal("═══════════════════════════════════").formatted(Formatting.DARK_PURPLE),
                Text.literal("Items sold: " + finalSoldCount).formatted(Formatting.YELLOW),
                Text.literal("Next auction in: " + PoliticalServer.formatTime(getTimeUntilNextAuction())).formatted(Formatting.GRAY),
                Text.literal("═══════════════════════════════════").formatted(Formatting.DARK_PURPLE)
        );

        save(server);
    }

    // ═══════════════════════════════════════════════════════════════
    // BIDDING
    // ═══════════════════════════════════════════════════════════════

    public static boolean placeBid(ServerPlayerEntity player, int amount) {
        if (player == null) return false;

        if (!auctionActive) {
            player.sendMessage(Text.literal("❌ No auction is currently active!").formatted(Formatting.RED));
            return false;
        }

        if (currentItems == null || currentItemIndex >= currentItems.size()) {
            player.sendMessage(Text.literal("❌ No item is currently up for auction!").formatted(Formatting.RED));
            return false;
        }

        AuctionItem item = currentItems.get(currentItemIndex);
        if (item == null) {
            player.sendMessage(Text.literal("❌ Error: Invalid auction item!").formatted(Formatting.RED));
            return false;
        }

        if (amount <= item.currentBid) {
            player.sendMessage(Text.literal("❌ Bid must be higher than " + item.currentBid + " credits!").formatted(Formatting.RED));
            return false;
        }

        if (!CreditItem.hasCredits(player, amount)) {
            int playerCredits = CreditItem.countCredits(player);
            player.sendMessage(Text.literal("❌ Not enough credits! You have: " + playerCredits + ", Need: " + amount).formatted(Formatting.RED));
            return false;
        }

        // Refund previous bidder
        if (item.highestBidderUuid != null && !item.highestBidderUuid.isEmpty()) {
            try {
                CreditItem.giveCredits(item.highestBidderUuid, item.currentBid);
                ServerPlayerEntity previousBidder = PoliticalServer.server.getPlayerManager().getPlayer(UUID.fromString(item.highestBidderUuid));
                if (previousBidder != null) {
                    previousBidder.sendMessage(Text.literal("⚠ You were outbid! Your " + item.currentBid + " credits have been refunded.").formatted(Formatting.YELLOW));
                }
            } catch (Exception e) {
                PoliticalServer.LOGGER.error("Error refunding previous bidder", e);
            }
        }

        if (!CreditItem.removeCredits(player, amount)) {
            player.sendMessage(Text.literal("❌ Failed to process bid!").formatted(Formatting.RED));
            return false;
        }

        item.currentBid = amount;
        item.highestBidderUuid = player.getUuidAsString();
        item.highestBidderName = player.getName().getString();
        lastBidTime = System.currentTimeMillis();

        // Broadcast only to players near auctioneers
        broadcastNearAuctioneers(PoliticalServer.server,
                Text.literal("💰 NEW BID: " + amount + " credits by " + player.getName().getString()).formatted(Formatting.GOLD, Formatting.BOLD),
                Text.literal("   ⏱ " + (BID_TIMEOUT_MS / 1000) + "s until sold!").formatted(Formatting.RED)
        );

        player.sendMessage(Text.literal("✓ Bid placed successfully!").formatted(Formatting.GREEN));
        return true;
    }

    // ═══════════════════════════════════════════════════════════════
    // ITEM GENERATION
    // ═══════════════════════════════════════════════════════════════

    private static List<AuctionItem> generateAuctionItems() {
        List<AuctionItem> allPossible = new ArrayList<>();

        try {
            allPossible.add(createNetheriteArmor());
            allPossible.add(createNetheriteSword());
            allPossible.add(createUltimatePotion());
            allPossible.add(createHarveysStick());
            allPossible.add(createTheGavel());
            allPossible.add(createHermesShoes());
            allPossible.add(createHPEBM());
            allPossible.add(createEnchantedElytra());
            allPossible.add(createSuperBow());
            allPossible.add(createFortunePick());
            allPossible.add(createWardenCore());
            allPossible.removeIf(Objects::isNull);
            Collections.shuffle(allPossible);

            int count = Math.min(MAX_ITEMS_PER_AUCTION, allPossible.size());
            return new ArrayList<>(allPossible.subList(0, count));
        } catch (Exception e) {
            PoliticalServer.LOGGER.error("Error generating auction items", e);
            return new ArrayList<>();
        }
    }

    private static NbtCompound createCustomNbt(String key, boolean value) {
        NbtCompound nbt = new NbtCompound();
        nbt.putByte(key, (byte) (value ? 1 : 0));  // Changed from putBoolean
        return nbt;
    }
    private static AuctionItem createWardenCore() {
        ItemStack stack = new ItemStack(Items.ECHO_SHARD);

        String name = "Warden's Core";
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name).formatted(Formatting.DARK_AQUA, Formatting.BOLD));

        NbtCompound nbt = new NbtCompound();
        nbt.putByte("warden_core", (byte) 1);
        stack.set(DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.of(nbt));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal("A pulsing core of sonic energy").formatted(Formatting.DARK_AQUA));
        lore.add(Text.literal("0.1% drop from Wardens").formatted(Formatting.GRAY));
        lore.add(Text.literal("Used to craft Ultra Beam weapons").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("From the Underground Auction").formatted(Formatting.DARK_PURPLE));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));

        return new AuctionItem(name, "special", 10000, stack);
    }
    private static AuctionItem createNetheriteArmor() {
        ItemStack stack;
        String name;

        int piece = random.nextInt(4);
        switch (piece) {
            case 0 -> {
                stack = new ItemStack(Items.NETHERITE_HELMET);
                name = "Enchanted Netherite Helmet";
            }
            case 1 -> {
                stack = new ItemStack(Items.NETHERITE_CHESTPLATE);
                name = "Enchanted Netherite Chestplate";
            }
            case 2 -> {
                stack = new ItemStack(Items.NETHERITE_LEGGINGS);
                name = "Enchanted Netherite Leggings";
            }
            default -> {
                stack = new ItemStack(Items.NETHERITE_BOOTS);
                name = "Enchanted Netherite Boots";
            }
        }

        int protLevel = 5 + random.nextInt(6);
        addEnchantment(stack, Enchantments.PROTECTION, protLevel);
        addEnchantment(stack, Enchantments.UNBREAKING, 3);
        addEnchantment(stack, Enchantments.MENDING, 1);

        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name).formatted(Formatting.LIGHT_PURPLE));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal("Protection " + toRoman(protLevel)).formatted(Formatting.GRAY));
        lore.add(Text.literal("From the Underground Auction").formatted(Formatting.DARK_PURPLE));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));

        return new AuctionItem(name + " (Prot " + protLevel + ")", "armor", 500 + (protLevel * 50), stack);
    }

    private static AuctionItem createNetheriteSword() {
        ItemStack stack = new ItemStack(Items.NETHERITE_SWORD);

        int sharpLevel = 6 + random.nextInt(3);
        addEnchantment(stack, Enchantments.SHARPNESS, sharpLevel);
        addEnchantment(stack, Enchantments.UNBREAKING, 3);
        addEnchantment(stack, Enchantments.MENDING, 1);
        addEnchantment(stack, Enchantments.LOOTING, 3);
        addEnchantment(stack, Enchantments.FIRE_ASPECT, 2);

        String name = "Blade of the Underworld";
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name).formatted(Formatting.RED, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal("Sharpness " + toRoman(sharpLevel)).formatted(Formatting.GRAY));
        lore.add(Text.literal("Forged in darkness").formatted(Formatting.DARK_RED));
        lore.add(Text.literal("From the Underground Auction").formatted(Formatting.DARK_PURPLE));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));

        return new AuctionItem(name + " (Sharp " + sharpLevel + ")", "weapon", 600 + (sharpLevel * 75), stack);
    }

    private static AuctionItem createUltimatePotion() {
        ItemStack stack = new ItemStack(Items.POTION);

        List<StatusEffectInstance> effects = new ArrayList<>();
        effects.add(new StatusEffectInstance(StatusEffects.SPEED, 12000, 2));
        effects.add(new StatusEffectInstance(StatusEffects.STRENGTH, 12000, 2));
        effects.add(new StatusEffectInstance(StatusEffects.REGENERATION, 12000, 1));
        effects.add(new StatusEffectInstance(StatusEffects.RESISTANCE, 12000, 1));
        effects.add(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 12000, 0));
        effects.add(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 12000, 0));
        effects.add(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 12000, 0));
        effects.add(new StatusEffectInstance(StatusEffects.HASTE, 12000, 2));
        effects.add(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 12000, 2));
        effects.add(new StatusEffectInstance(StatusEffects.ABSORPTION, 12000, 4));

        stack.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(
                Optional.empty(), Optional.of(0xFF00FF), effects, Optional.empty()
        ));

        String name = "Elixir of the Gods";
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name).formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal("All positive effects for 10 minutes").formatted(Formatting.GRAY));
        lore.add(Text.literal("From the Underground Auction").formatted(Formatting.DARK_PURPLE));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));

        return new AuctionItem(name, "potion", 650, stack);
    }

    private static AuctionItem createHarveysStick() {
        ItemStack stack = new ItemStack(Items.STICK);

        String name = "Harvey's Stick";
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name).formatted(Formatting.YELLOW, Formatting.BOLD));

        stack.set(DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.of(createCustomNbt("harveys_stick", true)));

        addEnchantment(stack, Enchantments.KNOCKBACK, 2);

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal("Strike your enemies with lightning!").formatted(Formatting.YELLOW));
        lore.add(Text.literal("Does not affect the wielder").formatted(Formatting.GRAY));
        lore.add(Text.literal("From the Underground Auction").formatted(Formatting.DARK_PURPLE));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));

        return new AuctionItem(name, "special", 1750, stack);
    }

    private static AuctionItem createTheGavel() {
        ItemStack stack = new ItemStack(Items.MACE);

        String name = "THE GAVEL";
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name).formatted(Formatting.DARK_RED, Formatting.BOLD));

        addEnchantment(stack, Enchantments.DENSITY, 10);
        addEnchantment(stack, Enchantments.BREACH, 8);
        addEnchantment(stack, Enchantments.UNBREAKING, 3);
        addEnchantment(stack, Enchantments.MENDING, 1);

        stack.set(DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.of(createCustomNbt("the_gavel", true)));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal("ORDER IN THE COURT!").formatted(Formatting.RED, Formatting.BOLD));
        lore.add(Text.literal("Right-click with Wind Charge to launch").formatted(Formatting.GRAY));
        lore.add(Text.literal("From the Underground Auction").formatted(Formatting.DARK_PURPLE));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));

        return new AuctionItem(name, "special", 5000, stack);
    }

    private static AuctionItem createHermesShoes() {
        ItemStack stack = new ItemStack(Items.IRON_BOOTS);

        String name = "Hermes' Shoes";
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name).formatted(Formatting.AQUA, Formatting.BOLD));

        addEnchantment(stack, Enchantments.FROST_WALKER, 3);
        addEnchantment(stack, Enchantments.UNBREAKING, 3);
        addEnchantment(stack, Enchantments.MENDING, 1);
        addEnchantment(stack, Enchantments.FEATHER_FALLING, 4);
        addEnchantment(stack, Enchantments.DEPTH_STRIDER, 3);

        stack.set(DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.of(createCustomNbt("hermes_shoes", true)));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal("Speed of the messenger god").formatted(Formatting.AQUA));
        lore.add(Text.literal("Grants permanent Speed III when worn").formatted(Formatting.GRAY));
        lore.add(Text.literal("From the Underground Auction").formatted(Formatting.DARK_PURPLE));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));

        return new AuctionItem(name, "special", 3000, stack);
    }

    private static AuctionItem createHPEBM() {
        ItemStack stack = new ItemStack(Items.IRON_SHOVEL);

        String name = "H.P.E.B.M.";
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name).formatted(Formatting.GREEN, Formatting.BOLD));

        // Use putByte to match hasCustomTag
        NbtCompound nbt = new NbtCompound();
        nbt.putByte("hpebm", (byte) 1);
        stack.set(DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.of(nbt));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal("High Powered Energy Beam Manipulator").formatted(Formatting.GREEN));
        lore.add(Text.literal("Hold right click to fire a continuous beam").formatted(Formatting.GRAY));
        lore.add(Text.literal("Costs 1 XP level per second").formatted(Formatting.RED));
        lore.add(Text.literal("Upgradeable with Warden's Core").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("From the Underground Auction").formatted(Formatting.DARK_PURPLE));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));

        return new AuctionItem(name, "special", 2500, stack);
    }

    private static AuctionItem createEnchantedElytra() {
        ItemStack stack = new ItemStack(Items.ELYTRA);

        String name = "Wings of the Void";
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name).formatted(Formatting.DARK_PURPLE, Formatting.BOLD));

        addEnchantment(stack, Enchantments.UNBREAKING, 5);
        addEnchantment(stack, Enchantments.MENDING, 1);

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal("Unbreaking V").formatted(Formatting.GRAY));
        lore.add(Text.literal("Nearly indestructible").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("From the Underground Auction").formatted(Formatting.DARK_PURPLE));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));

        return new AuctionItem(name, "armor", 4000, stack);
    }

    private static AuctionItem createSuperBow() {
        ItemStack stack = new ItemStack(Items.BOW);

        String name = "Apollo's Bow";
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name).formatted(Formatting.GOLD, Formatting.BOLD));

        addEnchantment(stack, Enchantments.POWER, 7);
        addEnchantment(stack, Enchantments.INFINITY, 1);
        addEnchantment(stack, Enchantments.FLAME, 1);
        addEnchantment(stack, Enchantments.PUNCH, 3);
        addEnchantment(stack, Enchantments.UNBREAKING, 3);

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal("Power VII").formatted(Formatting.GRAY));
        lore.add(Text.literal("Blessed by the sun god").formatted(Formatting.GOLD));
        lore.add(Text.literal("From the Underground Auction").formatted(Formatting.DARK_PURPLE));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));

        return new AuctionItem(name, "weapon", 1500, stack);
    }

    private static AuctionItem createFortunePick() {
        ItemStack stack = new ItemStack(Items.NETHERITE_PICKAXE);

        int fortuneLevel = 5 + random.nextInt(3);
        String name = "Miner's Dream";
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name).formatted(Formatting.AQUA, Formatting.BOLD));

        addEnchantment(stack, Enchantments.FORTUNE, fortuneLevel);
        addEnchantment(stack, Enchantments.EFFICIENCY, 6);
        addEnchantment(stack, Enchantments.UNBREAKING, 3);
        addEnchantment(stack, Enchantments.MENDING, 1);

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal("Fortune " + toRoman(fortuneLevel)).formatted(Formatting.GRAY));
        lore.add(Text.literal("Efficiency VI").formatted(Formatting.GRAY));
        lore.add(Text.literal("The ultimate mining tool").formatted(Formatting.AQUA));
        lore.add(Text.literal("From the Underground Auction").formatted(Formatting.DARK_PURPLE));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));

        return new AuctionItem(name + " (Fortune " + fortuneLevel + ")", "tool", 800 + (fortuneLevel * 100), stack);
    }

    private static void addEnchantment(ItemStack stack, RegistryKey<Enchantment> enchantmentKey, int level) {
        if (PoliticalServer.server == null) return;

        var registryManager = PoliticalServer.server.getRegistryManager();
        var enchantmentRegistry = registryManager.getOrThrow(RegistryKeys.ENCHANTMENT);

        Enchantment enchantment = enchantmentRegistry.get(enchantmentKey);
        if (enchantment == null) return;

        var entryOptional = enchantmentRegistry.getEntry(enchantmentRegistry.getRawId(enchantment));
        if (entryOptional.isEmpty()) return;

        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(
                stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT)
        );
        builder.add(entryOptional.get(), level);
        stack.set(DataComponentTypes.ENCHANTMENTS, builder.build());
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

        public static void giveAuctionItem (ServerPlayerEntity player,int itemIndex){
            List<AuctionItem> items = generateAuctionItems();
            if (itemIndex >= 0 && itemIndex < items.size()) {
                AuctionItem item = items.get(itemIndex);
                if (item.itemStack != null) {
                    player.getInventory().insertStack(item.itemStack.copy());
                    player.sendMessage(Text.literal("✓ Received: " + item.name).formatted(Formatting.GREEN));
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

