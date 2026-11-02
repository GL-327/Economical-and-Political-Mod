package com.political.items;
import com.political.ClassManager;
import com.political.HuntsManager;
import com.political.ItemRegistryIds;
import com.political.MasteryManager;
import com.political.ProgressionTreeManager;
import com.political.SkyblockTrinkets;
import com.political.government.Treasury;



import com.political.bounty.SlayerItems;
import com.political.economy.CoinManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Custom Items System
 * Create, manage, and distribute custom items with special properties
 */
public class CustomItemManager {

    // ════════════════════════════════════════════════════════════
    // CUSTOM ITEM DEFINITION
    // ════════════════════════════════════════════════════════════

    public static class CustomItem {
        public final String id;
        public final String name;
        public final String[] lore;
        public final ChatFormatting nameColor;
        public final Item baseItem;
        public final Map<String, Object> properties;
        public final boolean glowing;
        public final int customModelData;

        public CustomItem(String id, String name, String[] lore, ChatFormatting nameColor, 
                         Item baseItem, Map<String, Object> properties, boolean glowing, int customModelData) {
            this.id = id;
            this.name = name;
            this.lore = lore;
            this.nameColor = nameColor;
            this.baseItem = baseItem;
            this.properties = properties != null ? properties : new HashMap<>();
            this.glowing = glowing;
            this.customModelData = customModelData;
        }

        public ItemStack createStack(int count) {
            ItemStack stack = new ItemStack(baseItem, count);
            
            // Set custom name
            stack.set(DataComponents.CUSTOM_NAME, 
                Component.literal(name).withStyle(style -> style.withColor(nameColor).withItalic(false)));
            
            // Set lore
            if (lore != null && lore.length > 0) {
                List<Component> loreLines = new ArrayList<>();
                for (String line : lore) {
                    loreLines.add(Component.literal(line).withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(false)));
                }
                stack.set(DataComponents.LORE, new ItemLore(loreLines));
            }
            
            SlayerItems.setCustomItemId(stack, id);

            return stack;
        }
    }

    // ════════════════════════════════════════════════════════════
    // STORAGE
    // ════════════════════════════════════════════════════════════

    private static final Map<String, CustomItem> customItems = new ConcurrentHashMap<>();
    private static Path dataPath;
    private static final String DATA_FILE = "custom_items.dat";

    // ════════════════════════════════════════════════════════════
    // INITIALIZATION
    // ════════════════════════════════════════════════════════════

    public static void initialize(Path savePath) {
        dataPath = savePath;
        loadDefaultItems();
        loadData();
        SkyblockTrinkets.registerItems();
    }

    private static void loadDefaultItems() {
        // World Boss Trophies
        registerItem(new CustomItem("trophy_hollow_knight", "§6Hollow Knight Trophy", 
            new String[]{"§7Defeated: The Hollow Knight", "§7Tier: Lesser World Boss", "§eA token of victory"}, 
            ChatFormatting.GOLD, Items.NETHER_STAR, Map.of("type", "trophy", "boss", "hollow_knight"), true, 0));
        
        registerItem(new CustomItem("trophy_mosswarden", "§6Mosswarden Trophy",
            new String[]{"§7Defeated: Mosswarden", "§7Tier: Lesser World Boss", "§eA token of victory"},
            ChatFormatting.GOLD, Items.NETHER_STAR, Map.of("type", "trophy", "boss", "mosswarden"), true, 0));
        
        registerItem(new CustomItem("trophy_scorchfiend", "§6Scorchfiend Trophy",
            new String[]{"§7Defeated: Scorchfiend", "§7Tier: Lesser World Boss", "§eA token of victory"},
            ChatFormatting.GOLD, Items.NETHER_STAR, Map.of("type", "trophy", "boss", "scorchfiend"), true, 0));

        registerItem(new CustomItem("trophy_skulltide_captain", "§bSkulltide Captain Trophy",
            new String[]{"§7Defeated: Skulltide Captain", "§7Tier: Lesser World Boss", "§eA token of victory"},
            ChatFormatting.AQUA, Items.TRIDENT, Map.of("type", "trophy", "boss", "skulltide_captain"), true, 0));
        registerItem(new CustomItem("trophy_nethermaw", "§cNethermaw Trophy",
            new String[]{"§7Defeated: Nethermaw", "§7Tier: Greater World Boss", "§eA token of victory"},
            ChatFormatting.RED, Items.HOGLIN_SPAWN_EGG, Map.of("type", "trophy", "boss", "nethermaw"), true, 0));
        registerItem(new CustomItem("trophy_wailing_phantom", "§fWailing Phantom Trophy",
            new String[]{"§7Defeated: The Wailing Phantom", "§7Tier: Greater World Boss", "§eA token of victory"},
            ChatFormatting.GRAY, Items.PHANTOM_MEMBRANE, Map.of("type", "trophy", "boss", "wailing_phantom"), true, 0));
        registerItem(new CustomItem("trophy_crystal_golem", "§bCrystal Golem Trophy",
            new String[]{"§7Defeated: Crystal Golem", "§7Tier: Greater World Boss", "§eA token of victory"},
            ChatFormatting.AQUA, Items.PRISMARINE_SHARD, Map.of("type", "trophy", "boss", "crystal_golem"), true, 0));
        registerItem(new CustomItem("trophy_blight_mother", "§2Blight Mother Trophy",
            new String[]{"§7Defeated: Blight Mother", "§7Tier: Greater World Boss", "§eA token of victory"},
            ChatFormatting.DARK_GREEN, Items.SPIDER_EYE, Map.of("type", "trophy", "boss", "blight_mother"), true, 0));
        registerItem(new CustomItem("trophy_voidreaver", "§5Voidreaver Trophy",
            new String[]{"§7Defeated: Voidreaver", "§7Tier: Apex World Boss", "§eA token of victory"},
            ChatFormatting.DARK_PURPLE, Items.ENDER_EYE, Map.of("type", "trophy", "boss", "voidreaver"), true, 0));
        registerItem(new CustomItem("trophy_infernal_hydra", "§6Infernal Hydra Trophy",
            new String[]{"§7Defeated: Infernal Hydra", "§7Tier: Apex World Boss", "§eA token of victory"},
            ChatFormatting.GOLD, Items.FIRE_CHARGE, Map.of("type", "trophy", "boss", "infernal_hydra"), true, 0));
        registerItem(new CustomItem("trophy_ancient_one", "§3Ancient One Trophy",
            new String[]{"§7Defeated: The Ancient One", "§7Tier: Apex World Boss", "§eA token of victory"},
            ChatFormatting.DARK_AQUA, Items.HEART_OF_THE_SEA, Map.of("type", "trophy", "boss", "ancient_one"), true, 0));
        registerItem(new CustomItem("trophy_king_of_ashes", "§4King of Ashes Trophy",
            new String[]{"§7Defeated: King of Ashes", "§7Tier: Apex World Boss", "§eA token of victory"},
            ChatFormatting.DARK_RED, Items.WITHER_SKELETON_SKULL, Map.of("type", "trophy", "boss", "king_of_ashes"), true, 0));

        registerItem(new CustomItem("treasury_fragment", "§eTreasury Fragment",
            new String[]{"§7Minted by the Royal Treasury", "§7Trade for sigils or craft §6Treasury Sigil", "§8(Dropped by world bosses)"},
            ChatFormatting.YELLOW, Items.GOLD_NUGGET, Map.of("type", "treasury_fragment"), false, 0));
        registerItem(new CustomItem("treasury_sigil", "§6§lTreasury Sigil",
            new String[]{"§7Proof of vast wealth given back to the realm", "§aRight-click: §7Resistance §8(3 min)", "§8Craft: 9 Treasury Fragments in custom crafter"},
            ChatFormatting.GOLD, Items.NETHER_STAR, Map.of("type", "treasury_sigil"), true, 0));
        registerItem(new CustomItem("sovereign_commendation", "§d§lSovereign Commendation",
            new String[]{"§7Awarded to top world boss contributors", "§7Display trophy — flex your prowess"},
            ChatFormatting.LIGHT_PURPLE, Items.ENCHANTED_GOLDEN_APPLE, Map.of("type", "trophy", "boss", "sovereign"), true, 0));
        registerItem(new CustomItem("mastery_draught", "§6Mastery Draught",
            new String[]{"§7Distilled combat experience", "§eRight-click: §7+120 Mastery XP §8(all tracks)", "§8Buy: /treasury or casino treasury"},
            ChatFormatting.GOLD, Items.HONEY_BOTTLE, Map.of("type", "mastery_draught"), true, 0));

        // Sector Ascension (grind / collections / MF)
        registerItem(new CustomItem("asc_common_shard", "§f§lSector Shard",
            new String[]{"§7Common ascension material", "§8Mine ores & kill mobs — Magic Find helps", "§8Craft: /recipe §7Ascension"},
            ChatFormatting.WHITE, Items.PRISMARINE_SHARD, Map.of("type", "ascension"), false, 0));
        registerItem(new CustomItem("asc_vein_shard", "§b§lVein Shard",
            new String[]{"§7Rare mining drop", "§8Combine in Ascension recipes"},
            ChatFormatting.AQUA, Items.PRISMARINE_CRYSTALS, Map.of("type", "ascension"), true, 0));
        registerItem(new CustomItem("asc_blood_fragment", "§c§lBlood Fragment",
            new String[]{"§7Rare combat drop", "§8Combine in Ascension recipes"},
            ChatFormatting.RED, Items.ECHO_SHARD, Map.of("type", "ascension"), true, 0));
        registerItem(new CustomItem("asc_unstable_star", "§d§lUnstable Star",
            new String[]{"§7Volatile sector energy", "§8High-tier crafting core"},
            ChatFormatting.LIGHT_PURPLE, Items.NETHER_STAR, Map.of("type", "ascension"), true, 0));
        registerItem(new CustomItem("asc_rngesus_essence", "§5§lRNGesus Essence",
            new String[]{"§7Ultra-rare trophy material", "§8Bragging rights & apex crafts"},
            ChatFormatting.DARK_PURPLE, Items.DRAGON_BREATH, Map.of("type", "ascension"), true, 0));
        registerItem(new CustomItem("asc_hyper_catalyst", "§a§lHyper Catalyst",
            new String[]{"§eRight-click: §760s double §7collection progress"},
            ChatFormatting.GREEN, Items.BLAZE_POWDER, Map.of("type", "ascension"), true, 0));
        registerItem(new CustomItem("asc_mf_charm", "§b§lSliver of Fortune",
            new String[]{"§eRight-click: §7+5 permanent Magic Find §8(cap 400)"},
            ChatFormatting.AQUA, Items.RABBIT_FOOT, Map.of("type", "ascension"), true, 0));
        registerItem(new CustomItem("asc_void_lottery", "§5§lVoid Lottery Ticket",
            new String[]{"§eRight-click: §7coins + tiny essence chance"},
            ChatFormatting.DARK_PURPLE, Items.PAPER, Map.of("type", "ascension"), false, 0));
        registerItem(new CustomItem("asc_sector_compass", "§6§lSector Expedition Compass",
            new String[]{"§eRight-click: §7Luck III (30s) + 25k coins"},
            ChatFormatting.GOLD, Items.RECOVERY_COMPASS, Map.of("type", "ascension"), true, 0));
        registerItem(new CustomItem("asc_master_core", "§6§lAscension Master Core",
            new String[]{"§7Endgame craft component", "§8Grants passive grind bonuses when held §8(WIP display)"},
            ChatFormatting.GOLD, Items.HEART_OF_THE_SEA, Map.of("type", "ascension"), true, 0));
        
        // Skill Tomes
        registerItem(new CustomItem("skill_tome_combat", "§c§lSkill Tome: Combat",
            new String[]{"§7Grants 500 Combat skill points", "§cRight-click to use"},
            ChatFormatting.RED, Items.ENCHANTED_BOOK, Map.of("type", "skill_tome", "branch", "COMBAT", "points", 500), true, 0));
        
        registerItem(new CustomItem("skill_tome_mining", "§7§lSkill Tome: Mining",
            new String[]{"§7Grants 500 Mining skill points", "§cRight-click to use"},
            ChatFormatting.GRAY, Items.ENCHANTED_BOOK, Map.of("type", "skill_tome", "branch", "MINING", "points", 500), true, 0));
        
        registerItem(new CustomItem("skill_tome_farming", "§a§lSkill Tome: Farming",
            new String[]{"§7Grants 500 Farming skill points", "§cRight-click to use"},
            ChatFormatting.GREEN, Items.ENCHANTED_BOOK, Map.of("type", "skill_tome", "branch", "FARMING", "points", 500), true, 0));
        
        // Class Change Tokens
        registerItem(new CustomItem("class_change_token", "§e§lClass Change Token",
            new String[]{"§7Allows instant class change", "§ewithout cooldown", "§eRight-click to use"},
            ChatFormatting.YELLOW, Items.NAME_TAG, Map.of("type", "class_change"), true, 0));
        
        // Hunt Tokens
        registerItem(new CustomItem("hunt_skip_token", "§b§lHunt Skip Token",
            new String[]{"§7Skips current hunt", "§bRight-click to use"},
            ChatFormatting.AQUA, Items.PAPER, Map.of("type", "hunt_skip"), true, 0));
        
        // Coin Items
        registerItem(new CustomItem("coin_pouch_small", "§6§lSmall Coin Pouch",
            new String[]{"§7Contains 10,000 coins", "§6Right-click to open"},
            ChatFormatting.GOLD, Items.SUNFLOWER, Map.of("type", "coin_pouch", "amount", 10000), true, 0));
        
        registerItem(new CustomItem("coin_pouch_medium", "§6§lMedium Coin Pouch",
            new String[]{"§7Contains 50,000 coins", "§6Right-click to open"},
            ChatFormatting.GOLD, Items.SUNFLOWER, Map.of("type", "coin_pouch", "amount", 50000), true, 0));
        
        registerItem(new CustomItem("coin_pouch_large", "§6§lLarge Coin Pouch",
            new String[]{"§7Contains 250,000 coins", "§6Right-click to open"},
            ChatFormatting.GOLD, Items.SUNFLOWER, Map.of("type", "coin_pouch", "amount", 250000), true, 0));
        
        // Additional skill tomes
        registerItem(new CustomItem("skill_tome_agility", "§e§lSkill Tome: Agility",
            new String[]{"§7Grants 500 Agility skill points", "§cRight-click to use"},
            ChatFormatting.YELLOW, Items.ENCHANTED_BOOK, Map.of("type", "skill_tome", "branch", "AGILITY", "points", 500), true, 0));
        
        registerItem(new CustomItem("skill_tome_defense", "§9§lSkill Tome: Defense",
            new String[]{"§7Grants 500 Defense skill points", "§cRight-click to use"},
            ChatFormatting.BLUE, Items.ENCHANTED_BOOK, Map.of("type", "skill_tome", "branch", "DEFENSE", "points", 500), true, 0));
        
    }

    // ════════════════════════════════════════════════════════════
    // ITEM MANAGEMENT
    // ════════════════════════════════════════════════════════════

    public static void registerItem(CustomItem item) {
        customItems.put(item.id.toLowerCase(), item);
        saveData();
    }

    public static CustomItem getItem(String id) {
        return customItems.get(id.toLowerCase());
    }

    public static Collection<CustomItem> getAllItems() {
        return customItems.values();
    }

    public static boolean deleteItem(String id) {
        if (customItems.remove(id.toLowerCase()) != null) {
            saveData();
            return true;
        }
        return false;
    }

    public static void giveItem(ServerPlayer player, String id, int count) {
        CustomItem item = getItem(id);
        if (item != null) {
            ItemStack stack = item.createStack(count);
            player.addItem(stack);
            player.sendSystemMessage(Component.literal("§aReceived " + count + "x " + item.name), false);
        } else {
            player.sendSystemMessage(Component.literal("§cCustom item not found: " + id), false);
        }
    }

    // ════════════════════════════════════════════════════════════
    // ITEM USE HANDLING
    // ════════════════════════════════════════════════════════════

    public static boolean handleItemUse(ServerPlayer player, ItemStack stack) {
        var customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return false;
        
        CompoundTag nbt = customData.copyTag();
        String itemId = nbt.getString("custom_item_id").orElse("");
        if (itemId.isEmpty()) return false;
        
        CustomItem item = getItem(itemId);
        if (item == null) return false;
        
        String type = (String) item.properties.getOrDefault("type", "");
        
        switch (type) {
            case "skill_tome" -> {
                String branchName = (String) item.properties.get("branch");
                int points = (Integer) item.properties.getOrDefault("points", 0);
                try {
                    ProgressionTreeManager.Branch branch = ProgressionTreeManager.Branch.valueOf(branchName);
                    ProgressionTreeManager.awardSkillPoints(player, branch, points);
                    stack.shrink(1);
                    return true;
                } catch (IllegalArgumentException ignored) {}
            }
            case "class_change" -> {
                ClassManager.classChangeCooldown.remove(player.getUUID().toString());
                player.sendSystemMessage(Component.literal("§aClass change cooldown reset! You can now change classes."), false);
                stack.shrink(1);
                return true;
            }
            case "hunt_skip" -> {
                HuntsManager.abandonHunt(player);
                player.sendSystemMessage(Component.literal("§bHunt skipped!"), false);
                stack.shrink(1);
                return true;
            }
            case "coin_pouch" -> {
                long amount = ((Number) item.properties.getOrDefault("amount", 0)).longValue();
                CoinManager.giveCoins(player, amount);
                player.sendSystemMessage(Component.literal("§6Received " + String.format("%,d", amount) + " coins!"), false);
                stack.shrink(1);
                return true;
            }
            case "trophy", "treasury_fragment" -> {
                return false;
            }
            case "treasury_sigil" -> {
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.RESISTANCE, 3600, 0, false, true, true));
                player.sendSystemMessage(Component.literal("§6Treasury Sigil §7— §aResistance I (3 min)"), false);
                stack.shrink(1);
                return true;
            }
            case "mastery_draught" -> {
                for (MasteryManager.MasteryFamily f : MasteryManager.MasteryFamily.values()) {
                    MasteryManager.addXp(player, f, 120);
                }
                player.sendSystemMessage(Component.literal("§6+120 §7Mastery XP on every track!"), false);
                stack.shrink(1);
                return true;
            }
            case "sb_trinket" -> SkyblockTrinkets.handleUse(player, stack, item);
        }

        return false;
    }

    // ════════════════════════════════════════════════════════════
    // DATA PERSISTENCE
    // ════════════════════════════════════════════════════════════

    private static void loadData() {
        if (dataPath == null) return;
        
        File file = dataPath.resolve(DATA_FILE).toFile();
        if (!file.exists()) return;

        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
            int count = dis.readInt();
            for (int i = 0; i < count; i++) {
                String id = dis.readUTF();
                String name = dis.readUTF();
                int loreCount = dis.readInt();
                String[] lore = new String[loreCount];
                for (int j = 0; j < loreCount; j++) {
                    lore[j] = dis.readUTF();
                }
                ChatFormatting color = ChatFormatting.values()[dis.readInt()];
                String itemId = dis.readUTF();
                Item baseItem = BuiltInRegistries.ITEM.getValue(Identifier.tryParse(itemId));
                boolean glowing = dis.readBoolean();
                int customModelData = dis.readInt();
                
                int propCount = dis.readInt();
                Map<String, Object> props = new HashMap<>();
                for (int j = 0; j < propCount; j++) {
                    String key = dis.readUTF();
                    String value = dis.readUTF();
                    // Try to parse as number
                    try {
                        props.put(key, Integer.parseInt(value));
                    } catch (NumberFormatException e) {
                        props.put(key, value);
                    }
                }
                
                if (baseItem != Items.AIR) {
                    customItems.put(id.toLowerCase(), new CustomItem(id, name, lore, color, baseItem, props, glowing, customModelData));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveData() {
        if (dataPath == null) return;
        
        File file = dataPath.resolve(DATA_FILE).toFile();
        file.getParentFile().mkdirs();

        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file))) {
            dos.writeInt(customItems.size());
            
            for (CustomItem item : customItems.values()) {
                dos.writeUTF(item.id);
                dos.writeUTF(item.name);
                dos.writeInt(item.lore != null ? item.lore.length : 0);
                if (item.lore != null) {
                    for (String line : item.lore) {
                        dos.writeUTF(line);
                    }
                }
                dos.writeInt(item.nameColor.ordinal());
                dos.writeUTF(ItemRegistryIds.asString(item.baseItem));
                dos.writeBoolean(item.glowing);
                dos.writeInt(item.customModelData);
                
                dos.writeInt(item.properties.size());
                for (Map.Entry<String, Object> entry : item.properties.entrySet()) {
                    dos.writeUTF(entry.getKey());
                    dos.writeUTF(String.valueOf(entry.getValue()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ════════════════════════════════════════════════════════════
    // LISTING
    // ════════════════════════════════════════════════════════════

    public static void listItems(ServerPlayer player, int page) {
        List<CustomItem> items = new ArrayList<>(customItems.values());
        int itemsPerPage = 10;
        int totalPages = (int) Math.ceil(items.size() / (double) itemsPerPage);
        page = Math.max(1, Math.min(page, totalPages));
        
        player.sendSystemMessage(Component.literal("═══════════════════════════════════")
            .withStyle(ChatFormatting.GOLD), false);
        player.sendSystemMessage(Component.literal("       CUSTOM ITEMS (Page " + page + "/" + totalPages + ")")
            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        player.sendSystemMessage(Component.literal("═══════════════════════════════════")
            .withStyle(ChatFormatting.GOLD), false);
        
        int start = (page - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, items.size());
        
        for (int i = start; i < end; i++) {
            CustomItem item = items.get(i);
            player.sendSystemMessage(Component.literal("§7" + item.id + " §f- " + item.name), false);
        }
        
        player.sendSystemMessage(Component.literal("§7Use /customitems <page> to see more"), false);
    }
}
