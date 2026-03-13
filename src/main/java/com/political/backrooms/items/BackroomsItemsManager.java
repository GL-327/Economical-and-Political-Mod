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
        addItemWithLore("infused_echo_shard", "Infused Echo Shard", 
            "A crystalline shard pulsating with residual void energy. When thrown, it detonates in a brilliant blue flash that disorients nearby entities.",
            "Throwable • Stuns enemies in radius", Items.AMETHYST_SHARD);
            
        addItemWithLore("almond_water", "Almond Water", 
            "The most prized discovery of the Fracture. This amber liquid defies analysis—its molecular structure shifts constantly. Survivors report feelings of profound calm and renewed vigor after consumption.",
            "Saturation III (25s) • Restores sanity", Items.POTION);
            
        addItemWithLore("energy_drink", "Energy Drink", 
            "A metallic can bearing faded logos from forgotten corporations. The contents fizz with unnatural luminescence. One sip and the drinker feels boundless energy coursing through their veins.",
            "Speed II + Saturation II (25s)", Items.POTION);
            
        addItemWithLore("liquid_pain", "Liquid Pain", 
            "A swirling crimson fluid that seems to absorb light itself. Distilled from the suffering of those lost in the Fracture. Even sealed, its container feels warm with anguish.",
            "⚠ DANGEROUS • Inflicts suffering", Items.POTION);
            
        addItemWithLore("memory_juice", "Memory Juice", 
            "A golden elixir that tastes of nostalgia and forgotten dreams. Those who drink it report vivid recollections of lives they may never have lived.",
            "Restores lost memories • Clarity", Items.POTION);
            
        addItemWithLore("wanderers_tea", "Wanderer's Tea", 
            "A soothing herbal brew found occasionally in Fracture supply caches. The leaves are unknown to any botanical database. Warmth spreads through the body, easing the weariness of endless wandering.",
            "Regeneration • Soothing warmth", Items.POTION);
            
        addItemWithLore("null_cola", "Null Cola", 
            "A carbonated beverage that shouldn't exist. The can shows no manufacturing marks. The liquid inside is darker than black, yet tastes impossibly refreshing.",
            "Refreshes • Void-touched", Items.POTION);
            
        addItemWithLore("filtered_water", "Filtered Water", 
            "Water purified through Fracture filtration systems. Crystal clear and cold. A rare comfort in an endless maze of hostile geometry.",
            "Clean hydration • Safe to drink", Items.POTION);
            
        addItemWithLore("suspicious_liquid", "Suspicious Liquid", 
            "An unidentified fluid that shifts between colors. Its origin is unknown. Analysis equipment malfunctions in its presence. Consumption is not recommended.",
            "⚠ UNKNOWN • Unpredictable effects", Items.POTION);
            
        addItemWithLore("adrenaline_shot", "Adrenaline Shot", 
            "A medical injector containing a synthesized compound. When used, it floods the body with emergency response hormones. Vital for escaping dangerous situations.",
            "Emergency stim • Burst of energy", Items.SPYGLASS);
            
        addItemWithLore("courage_elixir", "Courage Elixir", 
            "A warm amber liquid that glows faintly in darkness. Ancient recipes speak of liquid bravery—this may be the closest thing to it. Fear fades, replaced by determination.",
            "Resistance • Fear immunity", Items.POTION);
            
        addItemWithLore("night_vision_serum", "Night Vision Serum", 
            "A luminescent blue serum developed for Fracture exploration. One drop in each eye grants the ability to see in absolute darkness for extended periods.",
            "Night Vision • Sees through void", Items.POTION);
            
        addItemWithLore("clarity_potion", "Clarity Potion", 
            "A pale silver liquid that seems to flow upward against gravity. Clears the mind of confusion and mental fog. Thoughts become sharp and focused.",
            "Clears confusion • Mental focus", Items.POTION);
            
        addItemWithLore("endurance_brew", "Endurance Brew", 
            "A thick red mixture that tastes of iron and determination. The body becomes more resilient, able to withstand punishment that would fell lesser beings.",
            "Resistance + Absorption", Items.POTION);
            
        addItemWithLore("haste_tonic", "Haste Tonic", 
            "A bright yellow effervescent liquid. Time seems to slow around the drinker while their movements accelerate to supernatural speeds.",
            "Haste II • Rapid actions", Items.POTION);
            
        addItemWithLore("featherfall_draught", "Featherfall Draught", 
            "A light blue liquid that seems to float in its container. Gravity's grip loosens on the drinker, allowing graceful descents from any height.",
            "Slow Falling • Safe descent", Items.POTION);
        
        // SECTION 2: FOOD
        addItemWithLore("raw_crawler_meat", "Raw Crawler Meat", 
            "Raw flesh harvested from crawler entities. Tough, sinewy, and carrying unknown pathogens. Cooking is strongly recommended before consumption.",
            "⚠ Raw • Low nutrition", Items.ROTTEN_FLESH);
            
        addItemWithLore("cooked_crawler_meat", "Cooked Crawler Meat", 
            "Properly prepared crawler flesh. The cooking process neutralizes most harmful compounds. Surprisingly nutritious, if one can overcome the texture.",
            "Good nutrition • Sustenance", Items.COOKED_BEEF);
            
        addItemWithLore("raw_hound_meat", "Raw Hound Meat", 
            "Cuts from hound entities. Dark red and pungent. The flesh of these aggressive creatures requires thorough cooking to neutralize trace toxins.",
            "⚠ Raw • Risk of illness", Items.ROTTEN_FLESH);
            
        addItemWithLore("cooked_hound_meat", "Cooked Hound Meat", 
            "Properly prepared hound flesh. Rich in protein and surprisingly filling. A staple food source for long-term Fracture survivors.",
            "High nutrition • Filling", Items.COOKED_PORKCHOP);
            
        addItemWithLore("void_jerky", "Void Jerky", 
            "Dried meat preserved through unknown Fracture processes. It never seems to spoil. The taste is smoky with an otherworldly undertone.",
            "Long-lasting • Never spoils", Items.BEEF);
            
        addItemWithLore("skulk_stew", "Skulk Stew", 
            "A thick stew incorporating sculk compounds found in the deepest Fracture regions. The bowl seems to absorb sound. Eating it is an eerily silent experience.",
            "Strange nourishment • Silence", Items.MUSHROOM_STEW);
            
        addItemWithLore("moldy_bread", "Moldy Bread", 
            "Ancient bread found in Fracture supply caches. The mold is actually beneficial—a strain that produces natural antibiotics. Better than it looks.",
            "Cures minor ailments • Filling", Items.BREAD);
            
        addItemWithLore("canned_beans", "Canned Beans", 
            "Preserved beans in a dented can. The label is in an unknown language. Despite appearances, the contents remain perfectly edible after countless years.",
            "Preserved • Reliable food", Items.RABBIT_STEW);
            
        addItemWithLore("protein_bar", "Protein Bar", 
            "A dense nutritional bar wrapped in faded packaging. Developed for military operations, now scattered throughout the Fracture. Dry but effective.",
            "Compact nutrition • Energy boost", Items.COOKIE);
            
        addItemWithLore("emergency_rations", "Emergency Rations", 
            "Compact survival food designed for extreme conditions. Tasteless but life-sustaining. Each packet contains a day's worth of nutrition.",
            "Survival food • Long-lasting", Items.DRIED_KELP);
            
        addItemWithLore("mystery_meat", "Mystery Meat", 
            "Meat of unknown origin. The texture and flavor don't match any known species. It's edible—probably—but questions remain unanswered.",
            "Unknown source • Edible", Items.COOKED_CHICKEN);
            
        addItemWithLore("preserved_fruit", "Preserved Fruit", 
            "Fruit preserved in a crystalline substance. Sweet and refreshing, a rare pleasure in the Fracture. Some say it brings brief glimpses of sunlight.",
            "Rare treat • Morale boost", Items.GOLDEN_APPLE);
            
        addItemWithLore("nutrient_paste", "Nutrient Paste", 
            "A viscous green paste containing all essential nutrients. Developed for long-term survival. The taste is... functional. Survival isn't always pleasant.",
            "Complete nutrition • No taste", Items.SLIME_BALL);
            
        addItemWithLore("survival_biscuit", "Survival Biscuit", 
            "A hard, dry biscuit designed for long-term storage. Nearly impossible to break by hand. Soaking in water is recommended before eating.",
            "Dense • Hard • Long-lasting", Items.BREAD);
            
        addItemWithLore("faceling_steak", "Faceling Steak", 
            "A cut of meat from uncertain origins. Those who've eaten it report vivid dreams of faces they've never seen. The taste is unsettlingly familiar.",
            "Strange visions • High quality", Items.COOKED_BEEF);
        
        // SECTION 3: MATERIALS
        addItemWithLore("crawler_hide", "Crawler Hide", 
            "Tough leather harvested from crawler entities. Resistant to tearing and water. Valuable for crafting durable equipment.",
            "Durable leather • Water-resistant", Items.LEATHER);
            
        addItemWithLore("hound_pelt", "Hound Pelt", 
            "A thick fur pelt from hound entities. Surprisingly warm despite its dark color. The fur seems to absorb light.",
            "Warm • Light-absorbing", Items.RABBIT_HIDE);
            
        addItemWithLore("aranea_silk", "Aranea Silk", 
            "Silk threads from aranea entities. Incredibly strong for their weight. Useful for crafting high-tension applications.",
            "Strong thread • Versatile", Items.STRING);
            
        addItemWithLore("smiler_essence", "Smiler Essence", 
            "A glowing powder extracted from smiler entities. It emits faint light and seems to shift in brightness. Unsettling to hold.",
            "Glowing • Unstable light", Items.GLOWSTONE_DUST);
            
        addItemWithLore("partygoer_confetti", "Partygoer Confetti", 
            "Colorful confetti from partygoer entities. It never seems to stop moving, as if caught in an eternal celebration. Slightly warm to the touch.",
            "Animated • Festive residue", Items.PAPER);
            
        addItemWithLore("skinstealer_fabric", "Skinstealer Fabric", 
            "A pale fabric woven from skinstealer residue. Uncomfortably soft. Those who touch it too long report phantom sensations of other skin.",
            "Unsettling texture • Soft", Items.WHITE_WOOL);
            
        addItemWithLore("faceling_mask", "Faceling Mask", 
            "A smooth mask worn by faceling entities. Featureless and cold. Wearing it brings a strange sense of anonymity—and loss of identity.",
            "Identity obscuring • Cold", Items.CARVED_PUMPKIN);
            
        addItemWithLore("wretch_scrap", "Wretch Scrap", 
            "Salvaged metal from wretch entities. Corroded yet somehow still structurally sound. Useful for basic crafting.",
            "Salvaged metal • Refined", Items.IRON_INGOT);
            
        addItemWithLore("backrooms_iodine", "Backrooms Iodine", 
            "Crystalline iodine found in Fracture medical caches. Useful for purification and treatment. A rare find with practical applications.",
            "Medical • Purification", Items.AMETHYST_SHARD);
            
        addItemWithLore("conduit_fragment", "Conduit Fragment", 
            "A shard from an unknown conduit system. It hums with residual energy. Perhaps once part of a larger transportation network.",
            "Energy residue • Conduit part", Items.PRISMARINE_SHARD);
            
        addItemWithLore("reality_core", "Reality Core", 
            "A pulsating core that seems to anchor reality around it. Invaluable for stabilizing dimensional rifts. Extremely rare.",
            "Stabilizes reality • Anchor", Items.END_CRYSTAL);
            
        addItemWithLore("fracture_shard", "Fracture Shard", 
            "A sharp fragment from a dimensional fracture point. The edges seem to cut through space itself. Handle with extreme caution.",
            "Dimensional cutting • Sharp", Items.OBSIDIAN);
            
        addItemWithLore("echo_dust", "Echo Dust", 
            "Fine dust that carries whispers of distant sounds. Used in certain Fracture technologies. Listening too closely is not recommended.",
            "Carries echoes • Whispering", Items.GUNPOWDER);
            
        addItemWithLore("static_crystal", "Static Crystal", 
            "A crystal that crackles with captured static energy. Useful for powering certain devices. The noise can be maddening after extended exposure.",
            "Static energy • Power source", Items.QUARTZ);
            
        addItemWithLore("time_sand", "Time Sand", 
            "Sand grains that seem to move slower than they should. Each grain allegedly contains a frozen moment. Valuable for temporal applications.",
            "Time-frozen • Temporal", Items.SAND);
            
        addItemWithLore("memory_fragment", "Memory Fragment", 
            "A crystallized fragment of someone's memory. Gazing into it reveals brief, emotionally-charged moments from an unknown life.",
            "Contains memories • Emotional", Items.GHAST_TEAR);
        
        // SECTION 4: TOOLS
        addItemWithLore("reality_anchor", "Reality Anchor", 
            "A complex device that stabilizes local reality. Prevents dimensional drift in unstable areas. Essential for long-term Fracture survival.",
            "Stabilizes position • Prevents drift", Items.COMPASS);
            
        addItemWithLore("dimensional_key", "Dimensional Key", 
            "A key that seems to fit no known lock. It hums when near dimensional boundaries. Perhaps it opens doors that don't exist yet.",
            "Opens dimensional locks • Humming", Items.TRIPWIRE_HOOK);
            
        addItemWithLore("void_compass", "Void Compass", 
            "A compass that points toward the nearest void pocket rather than spawn. The needle moves erratically in stable areas.",
            "Points to void • Navigation", Items.COMPASS);
            
        addItemWithLore("echo_locator", "Echo Locator", 
            "A device that detects recent dimensional activity. The dial spins when near Fracture boundaries. Useful for finding exits—or avoiding them.",
            "Detects rifts • Locates echoes", Items.CLOCK);
            
        addItemWithLore("fracture_detector", "Fracture Detector", 
            "An instrument for measuring dimensional instability. Higher readings indicate proximity to fracture points. Essential for safe exploration.",
            "Measures instability • Warning", Items.SPYGLASS);
            
        addItemWithLore("static_filter", "Static Filter", 
            "A device that screens out dimensional static. Clarity of perception increases near Fracture zones. The world becomes slightly more real.",
            "Filters static • Clarity", Items.TOTEM_OF_UNDYING);
            
        addItemWithLore("timepiece", "Timepiece", 
            "A clock that measures local temporal flow rather than absolute time. In the Fracture, time moves strangely. This device helps track it.",
            "Tracks local time • Temporal", Items.CLOCK);
            
        addItemWithLore("memory_bank", "Memory Bank", 
            "A storage vessel for preserving important memories. In the Fracture, forgetting is easy. This device ensures some things remain remembered.",
            "Stores memories • Preservation", Items.ENDER_PEARL);
            
        addItemWithLore("special_torch", "Special Torch", 
            "A torch that burns with unusual intensity. Its light repels certain light-sensitive entities. A comfort in the endless dark.",
            "Bright light • Repels entities", Items.TORCH);
        
        // SECTION 5: ARMOR RUNES
        addItemWithLore("rune_pyromaniac", "Rune of Pyromaniac", 
            "A burning rune etched with flame symbols. When applied to armor, it grants resistance to fire and the ability to walk through flames unburned.",
            "Fire resistance • Flame walk", Items.NETHERITE_SCRAP);
            
        addItemWithLore("rune_berserkers_rage", "Rune of Berserker's Rage", 
            "A blood-red rune that pulses with contained fury. Grants immense strength in battle but clouds judgment. Power has a price.",
            "Strength boost • Battle fury", Items.NETHERITE_SCRAP);
            
        addItemWithLore("rune_nightstalker", "Rune of Nightstalker", 
            "A dark rune that seems to absorb light. Grants enhanced vision in darkness. The shadows become allies rather than threats.",
            "Night vision • Shadow affinity", Items.NETHERITE_SCRAP);
            
        addItemWithLore("rune_heavyweight", "Rune of Heavyweight", 
            "A dense rune that feels impossibly heavy. Grants immense durability and knockback resistance. Movement becomes slower but resilience increases.",
            "Durability • Knockback resist", Items.NETHERITE_SCRAP);
            
        addItemWithLore("rune_vampiric", "Rune of Vampiric", 
            "A crimson rune that seems to pulse with stolen life. Grants life-stealing abilities on hit. What is taken from enemies restores the wielder.",
            "Life steal • Sustain", Items.NETHERITE_SCRAP);
            
        addItemWithLore("rune_echo_walker", "Rune of Echo Walker", 
            "A shimmering rune that leaves afterimages in the air. Grants silent movement and reduced detection. Walk without being heard.",
            "Silent movement • Stealth", Items.NETHERITE_SCRAP);
            
        addItemWithLore("rune_skulk_infused", "Rune of Skulk Infusion", 
            "A dark blue rune that resonates with sculk energy. Grants sensitivity to vibrations and sculk-based abilities. The deep listens.",
            "Sculk sensitivity • Deep powers", Items.NETHERITE_SCRAP);
            
        addItemWithLore("rune_featherweight", "Rune of Featherweight", 
            "A light rune that seems to float in hand. Grants enhanced agility and jump height. The ground becomes merely a suggestion.",
            "Agility • Jump boost", Items.NETHERITE_SCRAP);
            
        addItemWithLore("rune_thorned", "Rune of Thorns", 
            "A spiked rune that pricks the holder. Grants defensive thorns that damage attackers. Pain shared is pain halved.",
            "Thorns • Damage reflection", Items.NETHERITE_SCRAP);
            
        addItemWithLore("rune_glutton", "Rune of Glutton", 
            "A rune etched with consumption symbols. Grants enhanced consumption benefits. Food and potions become more effective.",
            "Enhanced consumption • Better food", Items.NETHERITE_SCRAP);
        
        PoliticalServer.LOGGER.info("Loaded " + ITEMS.size() + " backrooms items");
    }
    
    /**
     * Add an item with detailed lore
     */
    private static void addItemWithLore(String id, String name, String description, String effect, Item baseItem) {
        BackroomsItemDefinition definition = new BackroomsItemDefinition(id, name, description, baseItem);
        ITEMS.put(id, definition);
        
        // Create ItemStack with detailed lore
        ItemStack stack = new ItemStack(baseItem);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name).formatted(Formatting.YELLOW));
        
        // Build detailed lore
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        // Wrap description into multiple lines (approx 35 chars per line)
        int descLen = description.length();
        for (int i = 0; i < descLen; i += 35) {
            int end = Math.min(i + 35, descLen);
            String line = description.substring(i, end);
            lore.add(Text.literal(line).formatted(Formatting.GRAY));
        }
        lore.add(Text.literal(""));
        lore.add(Text.literal("【" + effect + "】").formatted(Formatting.AQUA));
        lore.add(Text.literal(""));
        lore.add(Text.literal("「Fracture Relic」").formatted(Formatting.DARK_PURPLE, Formatting.ITALIC));
        
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        
        ITEM_STACKS.put(id, stack);
    }
    
    /**
     * Add an item to the registry (legacy method)
     */
    private static void addItem(String id, String name, String description, Item baseItem) {
        addItemWithLore(id, name, description, "Unknown effect", baseItem);
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
