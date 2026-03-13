package com.political.backrooms.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.component.DataComponentTypes;
import com.political.PoliticalServer;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Backrooms Items Manager - Manages all backrooms items without registry
 * Integrates with CustomItemsGui and recipe systems
 */
public class BackroomsItemsManager {
    
    private static final Map<String, BackroomsItemDefinition> ITEMS = new HashMap<>();
    private static final Map<String, ItemStack> ITEM_STACKS = new HashMap<>();
    
    static {
        initializeItems();
    }
    
    /**
     * Initialize all backrooms items without registry
     */
    private static void initializeItems() {
        PoliticalServer.LOGGER.info("Initializing backrooms items without registry...");
        
        // SECTION 1: CONSUMABLES
        addItem("infused_echo_shard", "Infused Echo Shard", 
            "§7Mystical shard infused with backrooms energy", Items.AMETHYST_SHARD);
        addItem("almond_water", "Almond Water", 
            "§bPure water with almond essence", Items.POTION);
        addItem("energy_drink", "Energy Drink", 
            "§eHigh-caffeine energy beverage", Items.POTION);
        addItem("liquid_pain", "Liquid Pain", 
            "§cDangerous concoction of suffering", Items.POTION);
        addItem("memory_juice", "Memory Juice", 
            "§6Restores lost memories", Items.POTION);
        addItem("wanderers_tea", "Wanderer's Tea", 
            "§aSoothing tea for travelers", Items.POTION);
        addItem("null_cola", "Null Cola", 
            "§8Cola from the void", Items.POTION);
        addItem("filtered_water", "Filtered Water", 
            "§9Clean purified water", Items.POTION);
        addItem("suspicious_liquid", "Suspicious Liquid", 
            "§cUnidentified mysterious liquid", Items.POTION);
        addItem("adrenaline_shot", "Adrenaline Shot", 
            "§eMedical adrenaline injector", Items.SPYGLASS);
        addItem("courage_elixir", "Courage Elixir", 
            "§6Elixir of bravery", Items.POTION);
        addItem("night_vision_serum", "Night Vision Serum", 
            "§bSerum for night vision", Items.POTION);
        addItem("clarity_potion", "Clarity Potion", 
            "§aClears mind and senses", Items.POTION);
        addItem("endurance_brew", "Endurance Brew", 
            "§cBrew for enhanced endurance", Items.POTION);
        addItem("haste_tonic", "Haste Tonic", 
            "§eTonic for increased speed", Items.POTION);
        addItem("featherfall_draught", "Featherfall Draught", 
            "§bDraught for gentle falling", Items.POTION);
        
        // SECTION 2: FOOD
        addItem("raw_crawler_meat", "Raw Crawler Meat", 
            "§cRaw meat from crawler creatures", Items.ROTTEN_FLESH);
        addItem("cooked_crawler_meat", "Cooked Crawler Meat", 
            "§6Cooked crawler meat", Items.COOKED_BEEF);
        addItem("raw_hound_meat", "Raw Hound Meat", 
            "§cRaw meat from hound creatures", Items.ROTTEN_FLESH);
        addItem("cooked_hound_meat", "Cooked Hound Meat", 
            "§6Cooked hound meat", Items.COOKED_PORKCHOP);
        addItem("void_jerky", "Void Jerky", 
            "§8Dried meat from the void", Items.BEEF);
        addItem("skulk_stew", "Skulk Stew", 
            "§5Mysterious stew with skulk", Items.MUSHROOM_STEW);
        addItem("moldy_bread", "Moldy Bread", 
            "§2Old bread with mold", Items.BREAD);
        addItem("canned_beans", "Canned Beans", 
            "§6Preserved beans in can", Items.RABBIT_STEW);
        addItem("protein_bar", "Protein Bar", 
            "§eHigh-protein energy bar", Items.COOKIE);
        addItem("emergency_rations", "Emergency Rations", 
            "§cCompact emergency food", Items.DRIED_KELP);
        addItem("mystery_meat", "Mystery Meat", 
            "§cUnidentified meat source", Items.COOKED_CHICKEN);
        addItem("preserved_fruit", "Preserved Fruit", 
            "§6Long-lasting preserved fruit", Items.GOLDEN_APPLE);
        addItem("nutrient_paste", "Nutrient Paste", 
            "§7Nutrient-rich paste", Items.SLIME_BALL);
        addItem("survival_biscuit", "Survival Biscuit", 
            "§8Dry survival biscuit", Items.BREAD);
        addItem("faceling_steak", "Faceling Steak", 
            "§cPremium steak cut", Items.COOKED_BEEF);
        
        // SECTION 3: MATERIALS
        addItem("crawler_hide", "Crawler Hide", 
            "§6Tough hide from crawlers", Items.LEATHER);
        addItem("hound_pelt", "Hound Pelt", 
            "§cThick pelt from hounds", Items.RABBIT_HIDE);
        addItem("aranea_silk", "Aranea Silk", 
            "§cSilk from aranea creatures", Items.STRING);
        addItem("smiler_essence", "Smiler Essence", 
            "§8Essence extracted from smilers", Items.GLOWSTONE_DUST);
        addItem("partygoer_confetti", "Partygoer Confetti", 
            "§eConfetti from partygoers", Items.PAPER);
        addItem("skinstealer_fabric", "Skinstealer Fabric", 
            "§cFabric from skinstealers", Items.WHITE_WOOL);
        addItem("faceling_mask", "Faceling Mask", 
            "§7Mask worn by facelings", Items.CARVED_PUMPKIN);
        addItem("wretch_scrap", "Wretch Scrap", 
            "§8Scrap metal from wretches", Items.IRON_INGOT);
        addItem("backrooms_iodine", "Backrooms Iodine", 
            "§6Iodine crystals from backrooms", Items.AMETHYST_SHARD);
        addItem("conduit_fragment", "Conduit Fragment", 
            "§bFragment of mysterious conduit", Items.PRISMARINE_SHARD);
        addItem("reality_core", "Reality Core", 
            "§5Core that stabilizes reality", Items.END_CRYSTAL);
        addItem("fracture_shard", "Fracture Shard", 
            "§cShard from dimensional fracture", Items.OBSIDIAN);
        addItem("echo_dust", "Echo Dust", 
            "§7Dust that carries echoes", Items.GUNPOWDER);
        addItem("static_crystal", "Static Crystal", 
            "§8Crystal with static energy", Items.QUARTZ);
        addItem("time_sand", "Time Sand", 
            "§eSand that affects time", Items.SAND);
        addItem("memory_fragment", "Memory Fragment", 
            "§6Fragment of lost memories", Items.GHAST_TEAR);
        
        // SECTION 4: TOOLS
        addItem("reality_anchor", "Reality Anchor", 
            "§5Device that anchors to reality", Items.COMPASS);
        addItem("dimensional_key", "Dimensional Key", 
            "§6Key to unlock dimensions", Items.TRIPWIRE_HOOK);
        addItem("void_compass", "Void Compass", 
            "§8Compass that points to void", Items.COMPASS);
        addItem("echo_locator", "Echo Locator", 
            "§7Device to locate echoes", Items.CLOCK);
        addItem("fracture_detector", "Fracture Detector", 
            "§cDetects dimensional fractures", Items.SPYGLASS);
        addItem("static_filter", "Static Filter", 
            "§8Filters out static noise", Items.TOTEM_OF_UNDYING);
        addItem("timepiece", "Timepiece", 
            "§eDevice that manipulates time", Items.CLOCK);
        addItem("memory_bank", "Memory Bank", 
            "§6Stores memories safely", Items.ENDER_PEARL);
        
        PoliticalServer.LOGGER.info("Loaded " + ITEMS.size() + " backrooms items");
    }
    
    /**
     * Add an item to the registry
     */
    private static void addItem(String id, String name, String description, Item baseItem) {
        BackroomsItemDefinition definition = new BackroomsItemDefinition(id, name, description, baseItem);
        ITEMS.put(id, definition);
        
        // Create ItemStack
        ItemStack stack = new ItemStack(baseItem);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name).formatted(Formatting.YELLOW));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal(description).formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§7Backrooms Item"));
        // Skip lore for now to avoid component issues
        // Simplified - store item ID in custom name for now
        // TODO: Implement proper NBT storage when API is figured out
        
        ITEM_STACKS.put(id, stack);
    }
    
    /**
     * Get an item by ID
     */
    public static ItemStack getItem(String id) {
        return ITEM_STACKS.getOrDefault(id, ItemStack.EMPTY);
    }
    
    /**
     * Get all items
     */
    public static Map<String, ItemStack> getAllItems() {
        return new HashMap<>(ITEM_STACKS);
    }
    
    /**
     * Get item definition
     */
    public static BackroomsItemDefinition getDefinition(String id) {
        return ITEMS.get(id);
    }
    
    /**
     * Check if an item is a backrooms item
     */
    public static boolean isBackroomsItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        // Simplified check - check if item name matches any backrooms item
        Text name = stack.getName();
        return name.getString().contains("§"); // All backrooms items have colored names
    }
    
    /**
     * Get backrooms item ID from stack
     */
    public static String getItemId(ItemStack stack) {
        if (!isBackroomsItem(stack)) return null;
        // Simplified - return null for now
        return null;
    }
    
    /**
     * Item definition class
     */
    public static class BackroomsItemDefinition {
        public final String id;
        public final String name;
        public final String description;
        public final Item baseItem;
        
        public BackroomsItemDefinition(String id, String name, String description, Item baseItem) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.baseItem = baseItem;
        }
    }
}
