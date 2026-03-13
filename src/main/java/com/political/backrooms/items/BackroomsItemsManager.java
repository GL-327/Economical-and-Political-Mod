package com.political.backrooms.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import com.political.PoliticalServer;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Fracture 3 Items Manager - Manages all Fracture 3 dimension items
 * Integrates with CustomItemsGui and recipe systems
 */
public class BackroomsItemsManager {
    
    private static final Map<String, Fracture3ItemDefinition> ITEMS = new HashMap<>();
    private static final Map<String, ItemStack> ITEM_STACKS = new HashMap<>();
    
    static {
        initializeItems();
    }
    
    private static void initializeItems() {
        PoliticalServer.LOGGER.info("Initializing Fracture 3 items...");
        
        // ════════════════════════════════════════════════════════════
        // RAW MATERIALS (6)
        // ════════════════════════════════════════════════════════════
        
        addMaterial("wallpaper_fibre", "Wallpaper Fibre", Items.PAPER,
            "Thin strips of faded wallpaper peeled from Fracture walls.",
            "Used in Wallpaper Wraps armor crafting.");
            
        addMaterial("static_residue", "Static Residue", Items.GUNPOWDER,
            "Crackling dust collected from areas of high dimensional instability.",
            "Used in Static Shell armor crafting.");
            
        addMaterial("void_shard", "Void Shard", Items.END_CRYSTAL,
            "A fragment of crystallized nothingness from the deepest void pockets.",
            "Used in Void Plate armor crafting.");
            
        addMaterial("fluorescent_dust", "Fluorescent Dust", Items.GLOWSTONE_DUST,
            "Luminescent powder scraped from Fracture lighting fixtures.",
            "Used in Fluorescent Staff crafting.");
            
        addMaterial("hound_pelt", "Hound Pelt", Items.RABBIT_HIDE,
            "Thick dark fur harvested from defeated Hound entities.",
            "Used in Hound Leather armor crafting.");
            
        addMaterial("presence_residue", "Presence Residue", Items.PRISMARINE_SHARD,
            "An eerie substance left behind when a Presence fades.",
            "Used in advanced Fracture crafting.");
        
        // ════════════════════════════════════════════════════════════
        // ARMOR SETS - WALLPAPER WRAPS (Leather-based, Cyan)
        // ════════════════════════════════════════════════════════════
        
        addArmorPiece("wallpaper_helmet", "Wallpaper Wrap Hood", Items.LEATHER_HELMET,
            "A hood woven from layered wallpaper strips.", "WALLPAPER_WRAPS",
            2, 1, 0x00FFFF, "Blends with walls • Slight invisibility in corners");
            
        addArmorPiece("wallpaper_chestplate", "Wallpaper Wrap Cloak", Items.LEATHER_CHESTPLATE,
            "A cloak of faded patterns and peeling layers.", "WALLPAPER_WRAPS",
            3, 1, 0x00FFFF, "Blends with walls • Slight invisibility in corners");
            
        addArmorPiece("wallpaper_leggings", "Wallpaper Wrap Leggings", Items.LEATHER_LEGGINGS,
            "Leggings wrapped in yellowed wallpaper strips.", "WALLPAPER_WRAPS",
            2, 1, 0x00FFFF, "Blends with walls • Slight invisibility in corners");
            
        addArmorPiece("wallpaper_boots", "Wallpaper Wrap Boots", Items.LEATHER_BOOTS,
            "Boots covered in faded floral patterns.", "WALLPAPER_WRAPS",
            1, 1, 0x00FFFF, "Blends with walls • Silent footsteps");
        
        // ════════════════════════════════════════════════════════════
        // ARMOR SETS - STATIC SHELL (Chainmail-based, Yellow)
        // ════════════════════════════════════════════════════════════
        
        addArmorPiece("static_helmet", "Static Shell Helmet", Items.CHAINMAIL_HELMET,
            "A crackling helm of compressed static energy.", "STATIC_SHELL",
            2, 2, 0xFFFF00, "Absorbs lightning • Redirects electrical damage");
            
        addArmorPiece("static_chestplate", "Static Shell Chestplate", Items.CHAINMAIL_CHESTPLATE,
            "A vest that hums with contained static charge.", "STATIC_SHELL",
            4, 2, 0xFFFF00, "Absorbs lightning • Redirects electrical damage");
            
        addArmorPiece("static_leggings", "Static Shell Leggings", Items.CHAINMAIL_LEGGINGS,
            "Leg armor crackling with residual energy.", "STATIC_SHELL",
            3, 2, 0xFFFF00, "Absorbs lightning • Speed boost in storms");
            
        addArmorPiece("static_boots", "Static Shell Boots", Items.CHAINMAIL_BOOTS,
            "Boots that spark with each step.", "STATIC_SHELL",
            1, 2, 0xFFFF00, "Absorbs lightning • Walk on charged blocks");
        
        // ════════════════════════════════════════════════════════════
        // ARMOR SETS - VOID PLATE (Iron-based, Purple)
        // ════════════════════════════════════════════════════════════
        
        addArmorPiece("void_helmet", "Void Plate Helmet", Items.IRON_HELMET,
            "A helm forged from crystallized void matter.", "VOID_PLATE",
            2, 3, 0x800080, "Void resistance • Breath in void areas");
            
        addArmorPiece("void_chestplate", "Void Plate Chestplate", Items.IRON_CHESTPLATE,
            "Heavy armor that absorbs light itself.", "VOID_PLATE",
            5, 3, 0x800080, "Void resistance • 20% damage reduction from void");
            
        addArmorPiece("void_leggings", "Void Plate Leggings", Items.IRON_LEGGINGS,
            "Leg plates that seem to flicker in and out of existence.", "VOID_PLATE",
            4, 3, 0x800080, "Void resistance • Phase through thin walls");
            
        addArmorPiece("void_boots", "Void Plate Boots", Items.IRON_BOOTS,
            "Boots that anchor you against dimensional drift.", "VOID_PLATE",
            2, 3, 0x800080, "Void resistance • No fall damage in void");
        
        // ════════════════════════════════════════════════════════════
        // ARMOR SETS - HOUND LEATHER (Leather-based, Dark Red)
        // ════════════════════════════════════════════════════════════
        
        addArmorPiece("hound_helmet", "Hound Leather Hood", Items.LEATHER_HELMET,
            "A hood crafted from the dark fur of Hound entities.", "HOUND_LEATHER",
            2, 2, 0x8B0000, "Hound affinity • Intimidation aura");
            
        addArmorPiece("hound_chestplate", "Hound Leather Chestplate", Items.LEATHER_CHESTPLATE,
            "A vest of thick fur that absorbs impact.", "HOUND_LEATHER",
            4, 2, 0x8B0000, "Hound affinity • Reduced entity detection range");
            
        addArmorPiece("hound_leggings", "Hound Leather Leggings", Items.LEATHER_LEGGINGS,
            "Leg armor of dark, coarse fur.", "HOUND_LEATHER",
            3, 2, 0x8B0000, "Hound affinity • Scent tracking");
            
        addArmorPiece("hound_boots", "Hound Leather Boots", Items.LEATHER_BOOTS,
            "Boots that muffle all sound.", "HOUND_LEATHER",
            1, 2, 0x8B0000, "Hound affinity • Silent movement");
        
        // ════════════════════════════════════════════════════════════
        // WEAPONS (5)
        // ════════════════════════════════════════════════════════════
        
        addWeapon("corridor_blade", "Corridor Blade", Items.IRON_SWORD,
            "A blade that extends infinitely in the wielder's perception.",
            "Reach +2 blocks • Bonus damage in corridors");
            
        addWeapon("static_lance", "Static Lance", Items.TRIDENT,
            "A crackling spear of compressed lightning.",
            "Chain lightning on hit • 30% chance to paralyze");
            
        addWeapon("mirror_blade", "Mirror Blade", Items.DIAMOND_SWORD,
            "A reflective sword that shows glimpses of other places.",
            "Reflects 25% projectile damage • Teleport on critical");
            
        addWeapon("hound_fang_dagger", "Hound Fang Dagger", Items.IRON_SWORD,
            "A jagged dagger carved from a Hound's fang.",
            "Bleed effect • +50% damage to Hound entities");
            
        addWeapon("fluorescent_staff", "Fluorescent Staff", Items.BLAZE_ROD,
            "A staff that channels the Fracture's eerie lighting.",
            "Light aura • Blindness on hit • Illuminates hidden entities");
        
        // ════════════════════════════════════════════════════════════
        // TOOLS (3)
        // ════════════════════════════════════════════════════════════
        
        addTool("corridor_lantern", "Corridor Lantern", Items.LANTERN,
            "A lantern that reveals paths through impossible geometry.",
            "Reveals hidden doors • Lights 20 block radius");
            
        addTool("static_compass", "Static Compass", Items.COMPASS,
            "A compass that points toward dimensional instabilities.",
            "Points to nearest Fracture exit • Detects entities through walls");
            
        addTool("void_drill", "Void Drill", Items.DIAMOND_PICKAXE,
            "A mining tool that phases through solid matter.",
            "Mines 3x3 area • Breaks unbreakable blocks in Fracture");
        
        // ════════════════════════════════════════════════════════════
        // MISC ITEMS (3)
        // ════════════════════════════════════════════════════════════
        
        addMisc("almond_water_flask", "Almond Water Flask", Items.POTION,
            "A flask of the Fracture's most precious resource.",
            "Restores 8 hearts • Clears all negative effects • Sanity restore");
            
        addMisc("room_number_plaque", "Room Number Plaque", Items.NAME_TAG,
            "A brass plaque bearing a room number. One of thousands.",
            "Marks location • Teleport back to marked room");
            
        addMisc("the_log", "The Log", Items.WRITTEN_BOOK,
            "A journal found in the Fracture. The entries are... disturbing.",
            "Contains survivor notes • Hints at exit locations");
        
        PoliticalServer.LOGGER.info("Loaded " + ITEMS.size() + " Fracture 3 items");
    }
    
    // Helper methods for creating items
    private static void addMaterial(String id, String name, Item baseItem, String desc, String use) {
        ItemStack stack = new ItemStack(baseItem);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name).formatted(Formatting.AQUA));
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal(desc).formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ RAW MATERIAL").formatted(Formatting.DARK_AQUA));
        lore.add(Text.literal(use).formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("「Fracture 3」").formatted(Formatting.DARK_PURPLE, Formatting.ITALIC));
        
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        ITEMS.put(id, new Fracture3ItemDefinition(id, name, "material", baseItem));
        ITEM_STACKS.put(id, stack);
    }
    
    private static void addArmorPiece(String id, String name, Item baseItem, String desc, String set, 
                                       int armor, int toughness, int color, String effect) {
        ItemStack stack = new ItemStack(baseItem);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name).formatted(Formatting.LIGHT_PURPLE));
        stack.set(DataComponentTypes.DYED_COLOR, new net.minecraft.component.type.DyedColorComponent(color));
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal(desc).formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ " + set.replace("_", " ")).formatted(Formatting.GOLD));
        lore.add(Text.literal("Armor: +" + armor + "  Toughness: +" + toughness).formatted(Formatting.AQUA));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✦ " + effect).formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("「Fracture 3」").formatted(Formatting.DARK_PURPLE, Formatting.ITALIC));
        
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        ITEMS.put(id, new Fracture3ItemDefinition(id, name, "armor", baseItem));
        ITEM_STACKS.put(id, stack);
    }
    
    private static void addWeapon(String id, String name, Item baseItem, String desc, String effect) {
        ItemStack stack = new ItemStack(baseItem);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name).formatted(Formatting.RED, Formatting.BOLD));
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal(desc).formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚔ FRACTURE WEAPON").formatted(Formatting.DARK_RED));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✦ " + effect).formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("「Fracture 3」").formatted(Formatting.DARK_PURPLE, Formatting.ITALIC));
        
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        ITEMS.put(id, new Fracture3ItemDefinition(id, name, "weapon", baseItem));
        ITEM_STACKS.put(id, stack);
    }
    
    private static void addTool(String id, String name, Item baseItem, String desc, String effect) {
        ItemStack stack = new ItemStack(baseItem);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name).formatted(Formatting.GREEN));
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal(desc).formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⛏ FRACTURE TOOL").formatted(Formatting.DARK_GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✦ " + effect).formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("「Fracture 3」").formatted(Formatting.DARK_PURPLE, Formatting.ITALIC));
        
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        ITEMS.put(id, new Fracture3ItemDefinition(id, name, "tool", baseItem));
        ITEM_STACKS.put(id, stack);
    }
    
    private static void addMisc(String id, String name, Item baseItem, String desc, String effect) {
        ItemStack stack = new ItemStack(baseItem);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name).formatted(Formatting.GOLD));
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal(desc).formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("★ FRACTURE ARTIFACT").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✦ " + effect).formatted(Formatting.AQUA));
        lore.add(Text.literal(""));
        lore.add(Text.literal("「Fracture 3」").formatted(Formatting.DARK_PURPLE, Formatting.ITALIC));
        
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        ITEMS.put(id, new Fracture3ItemDefinition(id, name, "misc", baseItem));
        ITEM_STACKS.put(id, stack);
    }
    
    public static ItemStack getItem(String id) {
        return ITEM_STACKS.getOrDefault(id, ItemStack.EMPTY);
    }
    
    public static Map<String, ItemStack> getAllItems() {
        return new HashMap<>(ITEM_STACKS);
    }
    
    public static Fracture3ItemDefinition getDefinition(String id) {
        return ITEMS.get(id);
    }
    
    public static boolean isFracture3Item(ItemStack stack) {
        if (stack.isEmpty()) return false;
        var lore = stack.get(DataComponentTypes.LORE);
        if (lore == null) return false;
        for (Text line : lore.lines()) {
            if (line.getString().contains("Fracture 3")) return true;
        }
        return false;
    }
    
    public static class Fracture3ItemDefinition {
        public final String id;
        public final String name;
        public final String type;
        public final Item baseItem;
        
        public Fracture3ItemDefinition(String id, String name, String type, Item baseItem) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.baseItem = baseItem;
        }
    }
}
