const fs = require('fs');

// Load the recipe data
const recipes = JSON.parse(fs.readFileSync('config/political/custom_recipes.json'));

console.log(`Generating ${recipes.recipes.length} hardcoded Java recipes...`);

let output = `package com.political;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.ArrayList;
import java.util.List;

/**
 * ALL HARDCODED RECIPES - Generated from JSON
 * These are the default recipes that always work.
 * Custom recipes via /setrecipe can override these.
 */
public class HardcodedRecipes {
    
    public static void registerAllRecipes() {
        SlayerRecipes.RECIPES.clear();
        
        // Register all default recipes
`;

// Generate each recipe as Java code
recipes.recipes.forEach((recipe, index) => {
    const name = recipe.name.replace(/"/g, '\\"');
    const slayerType = recipe.requiredSlayer || 'null';
    const level = recipe.requiredLevel || 0;
    
    // Parse result NBT to extract item ID and custom ID
    let resultItem = 'Items.AIR';
    let resultCustomId = '';
    
    if (recipe.result && recipe.result.nbt) {
        const nbt = recipe.result.nbt;
        const idMatch = nbt.match(/id:"minecraft:([^"]+)"/);
        const customIdMatch = nbt.match(/custom_id:"([^"]+)"/);
        
        if (idMatch) {
            const itemId = idMatch[1];
            const mcItem = minecraftItemToJava(itemId);
            if (mcItem && mcItem !== 'Items.AIR') {
                resultItem = mcItem;
            }
        }
        
        if (customIdMatch) {
            resultCustomId = customIdMatch[1];
        }
    }
    
    // Generate ingredients array
    let ingredients = 'new ItemStack[] { ';
    for (let i = 0; i < 9; i++) {
        if (recipe.ingredients && recipe.ingredients[i]) {
            const ing = recipe.ingredients[i];
            let ingItem = 'Items.AIR';
            
            if (ing.nbt) {
                const idMatch = ing.nbt.match(/id:"minecraft:([^"]+)"/);
                if (idMatch) {
                    const itemId = idMatch[1];
                    const mcItem = minecraftItemToJava(itemId);
                    if (mcItem && mcItem !== 'Items.AIR') {
                        ingItem = mcItem;
                    }
                }
            }
            
            ingredients += `new ItemStack(${ingItem})`;
        } else {
            ingredients += 'ItemStack.EMPTY';
        }
        
        if (i < 8) ingredients += ', ';
    }
    ingredients += ' }';
    
    // Generate locked slots
    let lockedSlots = 'new boolean[] { ';
    for (let i = 0; i < 9; i++) {
        lockedSlots += recipe.lockedSlots && recipe.lockedSlots[i] ? 'true' : 'false';
        if (i < 8) lockedSlots += ', ';
    }
    lockedSlots += ' }';
    
    // Generate the recipe registration code
    output += `        
        // Recipe ${index + 1}: ${name}
        {
            ItemStack result = new ItemStack(${resultItem});
            ${resultCustomId ? `SlayerItems.setCustomItemId(result, "${resultCustomId}");` : ''}
            ${recipe.result && recipe.result.nbt && recipe.result.nbt.includes('custom_name') ? generateSetNameCode(recipe.result.nbt) : ''}
            
            ItemStack[] ingredients = ${ingredients};
            boolean[] lockedSlots = ${lockedSlots};
            
            SlayerRecipes.Recipe recipe = new SlayerRecipes.Recipe(
                "${name}",
                result,
                ingredients,
                lockedSlots,
                ${slayerType !== 'null' ? `SlayerManager.SlayerType.${slayerType}` : 'null'},
                ${level}
            );
            SlayerRecipes.RECIPES.add(recipe);
        }
`;
});

output += `
        
        System.out.println("[HardcodedRecipes] Registered " + SlayerRecipes.RECIPES.size() + " default recipes");
    }
    
    private static String minecraftItemToJava(String itemId) {
        switch (itemId.toLowerCase()) {
            case "air": return "Items.AIR";
            case "stone": return "Items.STONE";
            case "glass_block": return "Items.GLASS";
            case "glass": return "Items.GLASS";
            case "leather_helmet": return "Items.LEATHER_HELMET";
            case "leather_chestplate": return "Items.LEATHER_CHESTPLATE";
            case "leather_leggings": return "Items.LEATHER_LEGGINGS";
            case "leather_boots": return "Items.LEATHER_BOOTS";
            case "iron_helmet": return "Items.IRON_HELMET";
            case "iron_chestplate": return "Items.IRON_CHESTPLATE";
            case "iron_leggings": return "Items.IRON_LEGGINGS";
            case "iron_boots": return "Items.IRON_BOOTS";
            case "iron_sword": return "Items.IRON_SWORD";
            case "iron_pickaxe": return "Items.IRON_PICKAXE";
            case "iron_axe": return "Items.IRON_AXE";
            case "iron_shovel": return "Items.IRON_SHOVEL";
            case "iron_hoe": return "Items.IRON_HOE";
            case "diamond_helmet": return "Items.DIAMOND_HELMET";
            case "diamond_chestplate": return "Items.DIAMOND_CHESTPLATE";
            case "diamond_leggings": return "Items.DIAMOND_LEGGINGS";
            case "diamond_boots": return "Items.DIAMOND_BOOTS";
            case "diamond_sword": return "Items.DIAMOND_SWORD";
            case "diamond_pickaxe": return "Items.DIAMOND_PICKAXE";
            case "diamond_axe": return "Items.DIAMOND_AXE";
            case "diamond_shovel": return "Items.DIAMOND_SHOVEL";
            case "diamond_hoe": return "Items.DIAMOND_HOE";
            case "gold_helmet": return "Items.GOLDEN_HELMET";
            case "gold_chestplate": return "Items.GOLDEN_CHESTPLATE";
            case "gold_leggings": return "Items.GOLDEN_LEGGINGS";
            case "gold_boots": return "Items.GOLDEN_BOOTS";
            case "golden_helmet": return "Items.GOLDEN_HELMET";
            case "golden_chestplate": return "Items.GOLDEN_CHESTPLATE";
            case "golden_leggings": return "Items.GOLDEN_LEGGINGS";
            case "golden_boots": return "Items.GOLDEN_BOOTS";
            case "golden_sword": return "Items.GOLDEN_SWORD";
            case "golden_pickaxe": return "Items.GOLDEN_PICKAXE";
            case "golden_axe": return "Items.GOLDEN_AXE";
            case "golden_shovel": return "Items.GOLDEN_SHOVEL";
            case "golden_hoe": return "Items.GOLDEN_HOE";
            case "netherite_helmet": return "Items.NETHERITE_HELMET";
            case "netherite_chestplate": return "Items.NETHERITE_CHESTPLATE";
            case "netherite_leggings": return "Items.NETHERITE_LEGGINGS";
            case "netherite_boots": return "Items.NETHERITE_BOOTS";
            case "netherite_sword": return "Items.NETHERITE_SWORD";
            case "netherite_pickaxe": return "Items.NETHERITE_PICKAXE";
            case "netherite_axe": return "Items.NETHERITE_AXE";
            case "netherite_shovel": return "Items.NETHERITE_SHOVEL";
            case "netherite_hoe": return "Items.NETHERITE_HOE";
            case "chainmail_helmet": return "Items.CHAINMAIL_HELMET";
            case "chainmail_chestplate": return "Items.CHAINMAIL_CHESTPLATE";
            case "chainmail_leggings": return "Items.CHAINMAIL_LEGGINGS";
            case "chainmail_boots": return "Items.CHAINMAIL_BOOTS";
            case "bow": return "Items.BOW";
            case "crossbow": return "Items.CROSSBOW";
            case "shield": return "Items.SHIELD";
            case "heavy_core": return "Items.HEAVY_CORE";
            case "emerald": return "Items.EMERALD";
            case "diamond": return "Items.DIAMOND";
            case "iron_ingot": return "Items.IRON_INGOT";
            case "gold_ingot": return "Items.GOLD_INGOT";
            case "lapis_lazuli": return "Items.LAPIS_LAZULI";
            case "redstone": return "Items.REDSTONE";
            case "coal": return "Items.COAL";
            case "copper_ingot": return "Items.COPPER_INGOT";
            case "netherite_ingot": return "Items.NETHERITE_INGOT";
            case "stick": return "Items.STICK";
            case "rotten_flesh": return "Items.ROTTEN_FLESH";
            case "spider_eye": return "Items.SPIDER_EYE";
            case "bone": return "Items.BONE";
            case "slime_ball": return "Items.SLIME_BALL";
            case "ender_pearl": return "Items.ENDER_PEARL";
            case "gold_nugget": return "Items.GOLD_NUGGET";
            case "sculk_catalyst": return "Items.SCULK_CATALYST";
            default: return "Items.AIR";
        }
    }
}`;

fs.writeFileSync('src/main/java/com/political/HardcodedRecipes.java', output);
console.log('✅ Generated HardcodedRecipes.java with ' + recipes.recipes.length + ' recipes');

function minecraftItemToJava(itemId) {
    const mapping = {
        'air': 'Items.AIR',
        'stone': 'Items.STONE',
        'glass_block': 'Items.GLASS',
        'glass': 'Items.GLASS',
        'leather_helmet': 'Items.LEATHER_HELMET',
        'leather_chestplate': 'Items.LEATHER_CHESTPLATE',
        'leather_leggings': 'Items.LEATHER_LEGGINGS',
        'leather_boots': 'Items.LEATHER_BOOTS',
        'iron_helmet': 'Items.IRON_HELMET',
        'iron_chestplate': 'Items.IRON_CHESTPLATE',
        'iron_leggings': 'Items.IRON_LEGGINGS',
        'iron_boots': 'Items.IRON_BOOTS',
        'iron_sword': 'Items.IRON_SWORD',
        'iron_pickaxe': 'Items.IRON_PICKAXE',
        'iron_axe': 'Items.IRON_AXE',
        'iron_shovel': 'Items.IRON_SHOVEL',
        'iron_hoe': 'Items.IRON_HOE',
        'diamond_helmet': 'Items.DIAMOND_HELMET',
        'diamond_chestplate': 'Items.DIAMOND_CHESTPLATE',
        'diamond_leggings': 'Items.DIAMOND_LEGGINGS',
        'diamond_boots': 'Items.DIAMOND_BOOTS',
        'diamond_sword': 'Items.DIAMOND_SWORD',
        'diamond_pickaxe': 'Items.DIAMOND_PICKAXE',
        'diamond_axe': 'Items.DIAMOND_AXE',
        'diamond_shovel': 'Items.DIAMOND_SHOVEL',
        'diamond_hoe': 'Items.DIAMOND_HOE',
        'gold_helmet': 'Items.GOLDEN_HELMET',
        'gold_chestplate': 'Items.GOLDEN_CHESTPLATE',
        'gold_leggings': 'Items.GOLDEN_LEGGINGS',
        'gold_boots': 'Items.GOLD_BOOTS',
        'golden_helmet': 'Items.GOLDEN_HELMET',
        'golden_chestplate': 'Items.GOLDEN_CHESTPLATE',
        'golden_leggings': 'Items.GOLDEN_LEGGINGS',
        'golden_boots': 'Items.GOLDEN_BOOTS',
        'golden_sword': 'Items.GOLDEN_SWORD',
        'golden_pickaxe': 'Items.GOLDEN_PICKAXE',
        'golden_axe': 'Items.GOLDEN_AXE',
        'golden_shovel': 'Items.GOLDEN_SHOVEL',
        'golden_hoe': 'Items.GOLDEN_HOE',
        'netherite_helmet': 'Items.NETHERITE_HELMET',
        'netherite_chestplate': 'Items.NETHERITE_CHESTPLATE',
        'netherite_leggings': 'Items.NETHERITE_LEGGINGS',
        'netherite_boots': 'Items.NETHERITE_BOOTS',
        'netherite_sword': 'Items.NETHERITE_SWORD',
        'netherite_pickaxe': 'Items.NETHERITE_PICKAXE',
        'netherite_axe': 'Items.NETHERITE_AXE',
        'netherite_shovel': 'Items.NETHERITE_SHOVEL',
        'netherite_hoe': 'Items.NETHERITE_HOE',
        'chainmail_helmet': 'Items.CHAINMAIL_HELMET',
        'chainmail_chestplate': 'Items.CHAINMAIL_CHESTPLATE',
        'chainmail_leggings': 'Items.CHAINMAIL_LEGGINGS',
        'chainmail_boots': 'Items.CHAINMAIL_BOOTS',
        'bow': 'Items.BOW',
        'crossbow': 'Items.CROSSBOW',
        'shield': 'Items.SHIELD',
        'heavy_core': 'Items.HEAVY_CORE',
        'emerald': 'Items.EMERALD',
        'diamond': 'Items.DIAMOND',
        'iron_ingot': 'Items.IRON_INGOT',
        'gold_ingot': 'Items.GOLD_INGOT',
        'lapis_lazuli': 'Items.LAPIS_LAZULI',
        'redstone': 'Items.REDSTONE',
        'coal': 'Items.COAL',
        'copper_ingot': 'Items.COPPER_INGOT',
        'netherite_ingot': 'Items.NETHERITE_INGOT',
        'stick': 'Items.STICK',
        'rotten_flesh': 'Items.ROTTEN_FLESH',
        'spider_eye': 'Items.SPIDER_EYE',
        'bone': 'Items.BONE',
        'slime_ball': 'Items.SLIME_BALL',
        'ender_pearl': 'Items.ENDER_PEARL',
        'gold_nugget': 'Items.GOLD_NUGGET',
        'sculk_catalyst': 'Items.SCULK_CATALYST'
    };
    return mapping[itemId] || 'Items.AIR';
}

function generateSetNameCode(nbt) {
    const nameMatch = nbt.match(/text:"([^"]+)"/);
    if (nameMatch) {
        const name = nameMatch[1].replace(/"/g, '\\"');
        return `result.set(DataComponentTypes.CUSTOM_NAME, Text.literal("${name}").formatted(Formatting.WHITE));`;
    }
    return '';
}
