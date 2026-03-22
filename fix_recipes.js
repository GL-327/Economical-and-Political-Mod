#!/usr/bin/env node
/**
 * Fix Custom Recipes - Generates correct recipes with verified item IDs
 * 
 * Verified IDs from code:
 * - Block armor: glass_helmet, obsidian_helmet, quartz_helmet, etc.
 * - Crafter cores: custom_crafter_core_t1, custom_crafter_core_t2, custom_crafter_core_t3
 */

const fs = require('fs');
const path = require('path');

// All verified block armor materials
const blockArmorMaterials = [
    'glass', 'obsidian', 'quartz', 'glowstone', 'redstone', 'netherrack', 'endstone', 'ice',
    'prismarine', 'terracotta', 'mossy', 'soul_sand', 'magma', 'sandstone', 'amethyst', 'coal',
    'diamond_block', 'emerald_block', 'gold_block', 'iron_block', 'lapis_block', 'copper_block',
    'ancient_debris', 'basalt', 'blackstone', 'bone_block', 'brick', 'cactus', 'calcite',
    'deepslate', 'dripstone', 'hay', 'honeycomb', 'lily_pad', 'melon', 'moss_block', 'mycelium',
    'nether_brick', 'pumpkin', 'purpur', 'sand', 'sculk', 'shroomlight', 'slime', 'smooth_stone',
    'snow', 'soul_soil', 'sponge', 'target', 'tnt', 'warped', 'wet_sponge'
];

const armorPieces = ['helmet', 'chestplate', 'leggings', 'boots'];

// Tier mapping for crafter cores
function getCoreForMaterial(material) {
    const t1 = ['glass', 'sand', 'hay', 'lily_pad', 'melon', 'cactus', 'tnt', 'wet_sponge'];
    const t2 = ['quartz', 'glowstone', 'redstone', 'netherrack', 'ice', 'prismarine', 'terracotta',
                'sandstone', 'coal', 'copper_block', 'bone_block', 'brick', 'calcite', 'honeycomb',
                'moss_block', 'mycelium', 'pumpkin', 'slime', 'snow', 'sponge', 'target'];
    const t3 = ['obsidian', 'endstone', 'mossy', 'soul_sand', 'magma', 'amethyst', 'diamond_block',
                'emerald_block', 'gold_block', 'iron_block', 'lapis_block', 'ancient_debris', 'basalt',
                'blackstone', 'deepslate', 'dripstone', 'nether_brick', 'purpur', 'sculk', 'shroomlight',
                'smooth_stone', 'soul_soil', 'warped'];
    
    if (t1.includes(material)) return 'custom_crafter_core_t1';
    if (t2.includes(material)) return 'custom_crafter_core_t2';
    if (t3.includes(material)) return 'custom_crafter_core_t3';
    return 'custom_crafter_core_t1'; // default
}

function getMaterialIngredient(material) {
    const mapping = {
        'glass': 'minecraft:glass_block',
        'obsidian': 'minecraft:obsidian',
        'quartz': 'minecraft:quartz_block',
        'glowstone': 'minecraft:glowstone',
        'redstone': 'minecraft:redstone_block',
        'netherrack': 'minecraft:netherrack',
        'endstone': 'minecraft:end_stone',
        'ice': 'minecraft:packed_ice',
        'prismarine': 'minecraft:prismarine',
        'terracotta': 'minecraft:terracotta',
        'mossy': 'minecraft:mossy_cobblestone',
        'soul_sand': 'minecraft:soul_sand',
        'magma': 'minecraft:magma_block',
        'sandstone': 'minecraft:chiseled_sandstone',
        'amethyst': 'minecraft:amethyst_block',
        'coal': 'minecraft:coal_block',
        'diamond_block': 'minecraft:diamond_block',
        'emerald_block': 'minecraft:emerald_block',
        'gold_block': 'minecraft:gold_block',
        'iron_block': 'minecraft:iron_block',
        'lapis_block': 'minecraft:lapis_block',
        'copper_block': 'minecraft:copper_block',
        'ancient_debris': 'minecraft:ancient_debris',
        'basalt': 'minecraft:basalt',
        'blackstone': 'minecraft:blackstone',
        'bone_block': 'minecraft:bone_block',
        'brick': 'minecraft:bricks',
        'cactus': 'minecraft:cactus',
        'calcite': 'minecraft:calcite',
        'deepslate': 'minecraft:deepslate',
        'dripstone': 'minecraft:dripstone_block',
        'hay': 'minecraft:hay_block',
        'honeycomb': 'minecraft:honeycomb_block',
        'lily_pad': 'minecraft:lily_pad',
        'melon': 'minecraft:melon',
        'moss_block': 'minecraft:moss_block',
        'mycelium': 'minecraft:mycelium',
        'nether_brick': 'minecraft:nether_bricks',
        'pumpkin': 'minecraft:pumpkin',
        'purpur': 'minecraft:purpur_block',
        'sand': 'minecraft:sand',
        'sculk': 'minecraft:sculk',
        'shroomlight': 'minecraft:shroomlight',
        'slime': 'minecraft:slime_block',
        'smooth_stone': 'minecraft:smooth_stone',
        'snow': 'minecraft:snow_block',
        'soul_soil': 'minecraft:soul_soil',
        'sponge': 'minecraft:sponge',
        'target': 'minecraft:target',
        'tnt': 'minecraft:tnt',
        'warped': 'minecraft:warped_planks',
        'wet_sponge': 'minecraft:wet_sponge'
    };
    return mapping[material] || `minecraft:${material}`;
}

function createArmorRecipe(material, piece) {
    const itemId = `political:${material}_${piece}`;
    const coreId = `political:${getCoreForMaterial(material)}`;
    const materialId = getMaterialIngredient(material);
    
    // Armor recipes use 8 material + 1 core in specific pattern
    return {
        result: { id: itemId, count: 1 },
        ingredients: [
            { id: materialId, count: 1 },
            { id: materialId, count: 1 },
            { id: materialId, count: 1 },
            { id: materialId, count: 1 },
            { id: coreId, count: 1 },
            { id: materialId, count: 1 },
            { id: materialId, count: 1 },
            { id: materialId, count: 1 },
            { id: materialId, count: 1 }
        ],
        lockedSlots: [true, true, true, true, true, true, true, true, true]
    };
}

function generateAllRecipes() {
    const recipes = [];
    
    // Generate armor recipes
    for (const material of blockArmorMaterials) {
        for (const piece of armorPieces) {
            recipes.push(createArmorRecipe(material, piece));
        }
    }
    
    return recipes;
}

// Generate and save
const recipes = generateAllRecipes();
const output = { recipes };

const outputPath = path.join(__dirname, 'config', 'political', 'custom_recipes_fixed.json');
fs.writeFileSync(outputPath, JSON.stringify(output, null, 2));

console.log(`Generated ${recipes.length} recipes with verified item IDs`);
console.log(`Output: ${outputPath}`);
console.log('\nSample recipes:');
console.log('- glass_helmet: uses custom_crafter_core_t1');
console.log('- obsidian_helmet: uses custom_crafter_core_t3');
console.log('- quartz_helmet: uses custom_crafter_core_t2');
