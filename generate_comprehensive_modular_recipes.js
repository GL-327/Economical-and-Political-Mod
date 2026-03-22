#!/usr/bin/env node
/**
 * COMPREHENSIVE Custom Recipe Generator - ALL items from /customitems
 * Modular system where individual recipes can be edited without affecting others
 */

const fs = require('fs');
const path = require('path');

// ALL CUSTOM ITEMS FROM /customitems CATEGORIES

// Slayer Types for bounty items
const slayerTypes = [
    'zombie', 'spider', 'skeleton', 'creeper', 'enderman', 'witch', 'blaze', 'ghast',
    'wither_skeleton', 'piglin', 'hoglin', 'zoglin', 'pillager', 'vindicator', 'evoker',
    'ravager', 'vex', 'phantom', 'drowned', 'husk', 'stray', 'bogged', 'breeze',
    'warden', 'elder_guardian', 'ender_dragon', 'wither'
];

// Block Armor Materials (from CustomItemsGui - limited selection shown)
const blockArmorMaterials = [
    'glass', 'obsidian', 'quartz', 'glowstone', 'redstone', 'netherrack', 'endstone', 'ice',
    'prismarine', 'terracotta', 'mossy', 'soul_sand', 'magma', 'sandstone', 'amethyst', 'coal',
    'diamond_block', 'emerald_block', 'gold_block', 'iron_block', 'lapis_block', 'copper_block',
    'ancient_debris', 'basalt', 'blackstone', 'bone_block', 'brick', 'cactus', 'calcite',
    'deepslate', 'dripstone', 'hay', 'honeycomb', 'lily_pad', 'melon', 'moss_block', 'mycelium',
    'nether_brick', 'pumpkin', 'purpur', 'sand', 'sculk', 'shroomlight', 'slime', 'smooth_stone',
    'snow', 'soul_soil', 'sponge', 'target', 'tnt', 'warped', 'wet_sponge', 'crimson_stem',
    'crying_obsidian', 'gilded_blackstone', 'granite', 'diorite', 'andesite', 'polished_granite',
    'polished_diorite', 'polished_andesite', 'packed_ice', 'blue_ice', 'nether_gold_ore',
    'dark_oak_log', 'jungle_log', 'acacia_log', 'mangrove_log', 'cherry_log', 'bamboo_block',
    'tuff', 'polished_tuff', 'nether_wart_block', 'warped_wart_block',
    'stripped_spruce_log', 'stripped_birch_log', 'stripped_dark_oak_log', 'stripped_jungle_log',
    'stripped_acacia_log', 'stripped_mangrove_log', 'stripped_cherry_log', 'stripped_bamboo_block',
    'stripped_crimson_stem', 'stripped_warped_stem',
    'spruce_planks', 'birch_planks', 'jungle_planks', 'acacia_planks', 'dark_oak_planks',
    'mangrove_planks', 'cherry_planks', 'bamboo_planks', 'crimson_planks', 'warped_planks', 'oak_planks',
    'oak_log', 'spruce_log', 'birch_log', 'stone_bricks', 'cobblestone', 'mossy_cobblestone',
    'cobbled_deepslate', 'mud_bricks', 'mangrove_roots', 'muddy_mangrove_roots', 'netherite_block',
    'chiseled_copper', 'cut_copper', 'exposed_copper', 'weathered_copper', 'oxidised_copper',
    'waxed_cut_copper', 'polished_basalt', 'verdant_froglight', 'pearlescent_froglight', 'ochre_froglight',
    'iron_nugget', 'gold_nugget', 'copper_ingot', 'emerald', 'lapis_lazuli', 'amethyst_shard',
    'flint', 'bone_meal', 'charcoal', 'end_stone', 'snow_block'
];

const armorPieces = ['helmet', 'chestplate', 'leggings', 'boots'];

// Legendary Tool Types (actual methods in LegendaryTools class)
const legendaryToolTypes = [
    'dragon', 'aether', 'void', 'nature', 'frost', 'thunder', 'ocean', 'lunar',
    'solar', 'terra', 'phantom', 'blood', 'celestial', 'shadow', 'crystal'
];

const toolTypes = ['pickaxe', 'axe', 'shovel', 'hoe'];

// Helper Functions
function getCoreForMaterial(material) {
    const t1 = ['glass', 'sand', 'hay', 'lily_pad', 'melon', 'cactus', 'tnt', 'wet_sponge', 'snow', 'dirt', 'grass_block', 'gravel', 'cobblestone', 'stone', 'oak_log', 'spruce_log', 'birch_log'];
    const t2 = ['quartz', 'glowstone', 'redstone', 'netherrack', 'ice', 'prismarine', 'terracotta',
                'sandstone', 'coal', 'copper_block', 'bone_block', 'brick', 'calcite', 'honeycomb',
                'moss_block', 'mycelium', 'pumpkin', 'slime', 'sponge', 'target', 'dripstone',
                'granite', 'diorite', 'andesite', 'polished_granite', 'polished_diorite', 'polished_andesite',
                'tuff', 'polished_tuff', 'bamboo_block', 'crimson_stem', 'gilded_blackstone'];
    const t3 = ['obsidian', 'endstone', 'mossy', 'soul_sand', 'magma', 'amethyst', 'diamond_block',
                'emerald_block', 'gold_block', 'iron_block', 'lapis_block', 'ancient_debris', 'basalt',
                'blackstone', 'deepslate', 'nether_brick', 'purpur', 'sculk', 'shroomlight', 'smooth_stone',
                'soul_soil', 'warped', 'crying_obsidian', 'packed_ice', 'blue_ice', 'nether_gold_ore',
                'dark_oak_log', 'jungle_log', 'acacia_log', 'mangrove_log', 'cherry_log',
                'nether_wart_block', 'warped_wart_block', 'diamond_ore', 'emerald_ore', 'ancient_debris_ore',
                'deepslate_diamond_ore', 'deepslate_emerald_ore'];
    
    if (t1.includes(material)) return 'custom_crafter_core_t1';
    if (t2.includes(material)) return 'custom_crafter_core_t2';
    if (t3.includes(material)) return 'custom_crafter_core_t3';
    return 'custom_crafter_core_t1';
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

function createArmorNBT(material, piece) {
    const color = getArmorColor(material);
    const armorValue = getArmorValue(material);
    const toughnessValue = getToughnessValue(material);
    const customId = `${material}_${piece}`;
    
    return `{components:{\"minecraft:attribute_modifiers\":[{amount:${armorValue},id:\"politicalserver:${customId}_armor\",operation:\"add_value\",slot:\"${getSlotForPiece(piece)}\",type:\"minecraft:armor\"},{amount:${toughnessValue},id:\"politicalserver:${customId}_toughness\",operation:\"add_value\",slot:\"${getSlotForPiece(piece)}\",type:\"minecraft:armor_toughness\"}],\"minecraft:custom_data\":{custom_id:\"${customId}\"},\"minecraft:custom_name\":{bold:1b,color:\"${getColorHex(material)}\",text:\"${getDisplayName(material)} ${piece.charAt(0).toUpperCase() + piece.slice(1)}\"},\"minecraft:dyed_color\":${color},\"minecraft:enchantment_glint_override\":1b,\"minecraft:lore\":[\"\",{color:\"${getColorHex(material)}\",text:\"━━━ STATS ━━━\"},{color:\"white\",text:\"🛡 Armor: \"},{color:\"${getColorHex(material)}\",text:\"+${armorValue}\"},{color:\"white\",text:\"💎 Toughness: \"},{color:\"${getColorHex(material)}\",text:\"+${toughnessValue}\"},\"\",{color:\"gold\",text:\"━━━ SET BONUS ━━━\"},{color:\"yellow\",text:\"✦ Full set: ${getSetBonus(material)}\"},\"\",{color:\"dark_gray\",text:\"§8${getFlavorText(material)}\"}]},\"minecraft:max_damage\":${getDurability(material)}},count:1,id:\"minecraft:${getArmorBase(piece)}\"}`;
}

function createWeaponNBT(material) {
    const damage = getWeaponDamage(material);
    const color = getColorHex(material);
    const customId = material === 'soul_sand' ? 'soulsand_sword' : `${material}_sword`;
    
    return `{components:{\"minecraft:attribute_modifiers\":[{amount:${damage},id:\"politicalserver:${material}_sword_damage\",operation:\"add_value\",slot:\"mainhand\",type:\"minecraft:attack_damage\"}],\"minecraft:custom_data\":{custom_id:\"${customId}\"},\"minecraft:custom_name\":{bold:1b,color:\"${color}\",text:\"${getDisplayName(material)} Sword\"},\"minecraft:enchantment_glint_override\":1b,\"minecraft:lore\":[\"\",{color:\"${color}\",text:\"━━━ STATS ━━━\"},{color:\"white\",text:\"⚔ Attack Damage: \"},{color:\"${color}\",text:\"+${damage}\"},\"\",{color:\"gold\",text:\"━━━ ABILITY ━━━\"},{color:\"yellow\",text:\"✦ ${getWeaponAbility(material)}\"},\"\",{color:\"dark_gray\",text:\"§8${getFlavorText(material)}\"}]},\"minecraft:max_damage\":${getWeaponDurability(material)}},count:1,id:\"minecraft:iron_sword\"}`;
}

function createCoreNBT(coreType) {
    const tier = coreType.slice(-1);
    const color = tier === '1' ? 'green' : tier === '2' ? 'aqua' : 'light_purple';
    const rarity = tier === '1' ? 'Uncommon' : tier === '2' ? 'Rare' : 'Legendary';
    
    return `{components:{\"minecraft:custom_data\":{custom_id:\"${coreType}\"},\"minecraft:custom_name\":{bold:1b,color:\"${color}\",text:\"⚙ Custom Crafting Core (T${tier})\"},\"minecraft:enchantment_glint_override\":1b,\"minecraft:lore\":[\"\",{bold:1b,color:\"${color}\",text:\"✦ ${rarity} Crafting Component\"},\"\",{color:\"gray\",text:\"Used to craft Tier ${tier} custom\"},{color:\"gray\",text:\"armor and weapon pieces.\"},\"\",{color:\"yellow\",text:\"Required for:\"},{color:\"${color}\",text:\"• All T${tier} Custom Armor Sets\"},\"\",{color:\"dark_gray\",text:\"§8[Rare drop from high-tier bosses]\"}]},count:1,id:\"minecraft:heavy_core\"}`;
}

function createGemArmorNBT(gemType, piece) {
    const color = gemType === 'emerald' ? 'green' : 'blue';
    const armorValue = gemType === 'emerald' ? 7 : 6;
    const toughnessValue = gemType === 'emerald' ? 3 : 2;
    const customId = `${gemType}_${piece}`;
    
    return `{components:{\"minecraft:attribute_modifiers\":[{amount:${armorValue},id:\"politicalserver:${customId}_armor\",operation:\"add_value\",slot:\"${getSlotForPiece(piece)}\",type:\"minecraft:armor\"},{amount:${toughnessValue},id:\"politicalserver:${customId}_toughness\",operation:\"add_value\",slot:\"${getSlotForPiece(piece)}\",type:\"minecraft:armor_toughness\"}],\"minecraft:custom_data\":{custom_id:\"${customId}\"},\"minecraft:custom_name\":{bold:1b,color:\"${color}\",text:\"${gemType.charAt(0).toUpperCase() + gemType.slice(1)} ${piece.charAt(0).toUpperCase() + piece.slice(1)}\"},\"minecraft:enchantment_glint_override\":1b,\"minecraft:lore\":[\"\",{color:\"${color}\",text:\"━━━ STATS ━━━\"},{color:\"white\",text:\"🛡 Armor: \"},{color:\"${color}\",text:\"+${armorValue}\"},{color:\"white\",text:\"💎 Toughness: \"},{color:\"${color}\",text:\"+${toughnessValue}\"},\"\",{color:\"gold\",text:\"━━━ SET BONUS ━━━\"},{color:\"yellow\",text:\"✦ ${gemType === 'emerald' ? 'Luck & Experience boost' : 'Wisdom & Intelligence'}\"},\"\",{color:\"dark_gray\",text:\"§8Forged from pure ${gemType} gems\"}]},\"minecraft:max_damage\":${gemType === 'emerald' ? 1200 : 1000}},count:1,id:\"minecraft:diamond_${piece}\"}`;
}

function createGemWeaponNBT(gemType) {
    const color = gemType === 'emerald' ? 'green' : 'blue';
    const damage = gemType === 'emerald' ? 8 : 7;
    const customId = `${gemType}_sword`;
    
    return `{components:{\"minecraft:attribute_modifiers\":[{amount:${damage},id:\"politicalserver:${customId}_damage\",operation:\"add_value\",slot:\"mainhand\",type:\"minecraft:attack_damage\"}],\"minecraft:custom_data\":{custom_id:\"${customId}\"},\"minecraft:custom_name\":{bold:1b,color:\"${color}\",text:\"${gemType.charAt(0).toUpperCase() + gemType.slice(1)} Sword\"},\"minecraft:enchantment_glint_override\":1b,\"minecraft:lore\":[\"\",{color:\"${color}\",text:\"━━━ STATS ━━━\"},{color:\"white\",text:\"⚔ Attack Damage: \"},{color:\"${color}\",text:\"+${damage}\"},\"\",{color:\"gold\",text:\"━━━ ABILITY ━━━\"},{color:\"yellow\",text:\"✦ ${gemType === 'emerald' ? 'Critical strike chance' : 'Mana regeneration'}\"},\"\",{color:\"dark_gray\",text:\"§8Forged from pure ${gemType} gems\"}]},\"minecraft:max_damage\":${gemType === 'emerald' ? 1500 : 1300}},count:1,id:\"minecraft:diamond_sword\"}`;
}

function createSlayerArmorNBT(slayerType, tier, piece) {
    const color = tier === 1 ? 'green' : 'aqua';
    const armorValue = tier === 1 ? 6 : 8;
    const toughnessValue = tier === 1 ? 2 : 3;
    const customId = `${slayerType}_${piece}_t${tier}`;
    
    return `{components:{\"minecraft:attribute_modifiers\":[{amount:${armorValue},id:\"politicalserver:${customId}_armor\",operation:\"add_value\",slot:\"${getSlotForPiece(piece)}\",type:\"minecraft:armor\"},{amount:${toughnessValue},id:\"politicalserver:${customId}_toughness\",operation:\"add_value\",slot:\"${getSlotForPiece(piece)}\",type:\"minecraft:armor_toughness\"}],\"minecraft:custom_data\":{custom_id:\"${customId}\"},\"minecraft:custom_name\":{bold:1b,color:\"${color}\",text:\"${getDisplayName(slayerType)} ${tier === 1 ? 'Hunter' : 'Slayer'} ${piece.charAt(0).toUpperCase() + piece.slice(1)}\"},\"minecraft:enchantment_glint_override\":1b,\"minecraft:lore\":[\"\",{color:\"${color}\",text:\"━━━ STATS ━━━\"},{color:\"white\",text:\"🛡 Armor: \"},{color:\"${color}\",text:\"+${armorValue}\"},{color:\"white\",text:\"💎 Toughness: \"},{color:\"${color}\",text:\"+${toughnessValue}\"},\"\",{color:\"gold\",text:\"━━━ SET BONUS ━━━\"},{color:\"yellow\",text:\"✦ ${getSlayerSetBonus(slayerType)}\"},\"\",{color:\"dark_gray\",text:\"§8Tier ${tier} ${getDisplayName(slayerType)} gear\"}]},\"minecraft:max_damage\":${tier === 1 ? 800 : 1200}},count:1,id:\"minecraft:${tier === 1 ? 'iron' : 'diamond'}_${piece}\"}`;
}

function createSlayerWeaponNBT(slayerType, tier) {
    const color = tier === 1 ? 'green' : 'aqua';
    const damage = tier === 1 ? 7 : 9;
    const customId = tier === 1 ? `${slayerType}_sword` : `${slayerType}_sword_t2`;
    
    return `{components:{\"minecraft:attribute_modifiers\":[{amount:${damage},id:\"politicalserver:${customId}_damage\",operation:\"add_value\",slot:\"mainhand\",type:\"minecraft:attack_damage\"}],\"minecraft:custom_data\":{custom_id:\"${customId}\"},\"minecraft:custom_name\":{bold:1b,color:\"${color}\",text:\"${getDisplayName(slayerType)} ${tier === 1 ? 'Hunter' : 'Slayer'} Sword\"},\"minecraft:enchantment_glint_override\":1b,\"minecraft:lore\":[\"\",{color:\"${color}\",text:\"━━━ STATS ━━━\"},{color:\"white\",text:\"⚔ Attack Damage: \"},{color:\"${color}\",text:\"+${damage}\"},\"\",{color:\"gold\",text:\"━━━ ABILITY ━━━\"},{color:\"yellow\",text:\"✦ ${getSlayerAbility(slayerType)}\"},\"\",{color:\"dark_gray\",text:\"§8Tier ${tier} ${getDisplayName(slayerType)} weapon\"}]},\"minecraft:max_damage\":${tier === 1 ? 1000 : 1500}},count:1,id:\"minecraft:${tier === 1 ? 'iron' : 'diamond'}_sword\"}`;
}

function createRecipe(itemId, recipe) {
    return {
        [itemId]: recipe
    };
}

// Recipe generation functions for each category
function generateBlockArmorRecipes() {
    const recipes = {};
    
    for (const material of blockArmorMaterials) {
        for (const piece of armorPieces) {
            const itemId = `${material}_${piece}`;
            const coreId = getCoreForMaterial(material);
            const materialId = getMaterialIngredient(material);
            
            recipes[itemId] = {
                name: `${getDisplayName(material)} ${piece.charAt(0).toUpperCase() + piece.slice(1)}`,
                requiredSlayer: "",
                requiredLevel: 0,
                result: {
                    nbt: createArmorNBT(material, piece),
                    count: 1
                },
                ingredients: [
                    { nbt: `{count:1,id:"${materialId}"}`, count: 1 },
                    { nbt: `{count:1,id:"${materialId}"}`, count: 1 },
                    { nbt: `{count:1,id:"${materialId}"}`, count: 1 },
                    { nbt: `{count:1,id:"${materialId}"}`, count: 1 },
                    { nbt: createCoreNBT(coreId), count: 1 },
                    { nbt: `{count:1,id:"${materialId}"}`, count: 1 },
                    { nbt: `{count:1,id:"${materialId}"}`, count: 1 },
                    { nbt: `{count:1,id:"${materialId}"}`, count: 1 },
                    { nbt: `{count:1,id:"${materialId}"}`, count: 1 }
                ],
                lockedSlots: [true, true, true, true, true, true, true, true, true]
            };
        }
    }
    
    return recipes;
}

function generateBlockWeaponRecipes() {
    const recipes = {};
    
    for (const material of blockArmorMaterials) {
        const itemId = `${material}_sword`;
        const coreId = getCoreForMaterial(material);
        const materialId = getMaterialIngredient(material);
        
        recipes[itemId] = {
            name: `${getDisplayName(material)} Sword`,
            requiredSlayer: "",
            requiredLevel: 0,
            result: {
                nbt: createWeaponNBT(material),
                count: 1
            },
            ingredients: [
                { nbt: `{count:1,id:"${materialId}"}`, count: 1 },
                { nbt: `{count:1,id:"${materialId}"}`, count: 1 },
                { nbt: createCoreNBT(coreId), count: 1 },
                { nbt: `{count:1,id:"minecraft:stick"}`, count: 1 },
                { nbt: `{count:1,id:"minecraft:stick"}`, count: 1 }
            ],
            lockedSlots: [true, true, true, true, true]
        };
    }
    
    return recipes;
}

function generateGemArmorRecipes() {
    const recipes = {};
    const gemTypes = ['emerald', 'lapis'];
    
    for (const gemType of gemTypes) {
        for (const piece of armorPieces) {
            const itemId = `${gemType}_${piece}`;
            
            recipes[itemId] = {
                name: `${gemType.charAt(0).toUpperCase() + gemType.slice(1)} ${piece.charAt(0).toUpperCase() + piece.slice(1)}`,
                requiredSlayer: "",
                requiredLevel: 0,
                result: {
                    nbt: createGemArmorNBT(gemType, piece),
                    count: 1
                },
                ingredients: [
                    { nbt: `{count:1,id:"minecraft:${gemType}_block"}`, count: 1 },
                    { nbt: `{count:1,id:"minecraft:${gemType}_block"}`, count: 1 },
                    { nbt: `{count:1,id:"minecraft:${gemType}_block"}`, count: 1 },
                    { nbt: `{count:1,id:"minecraft:${gemType}_block"}`, count: 1 },
                    { nbt: createCoreNBT('custom_crafter_core_t2'), count: 1 },
                    { nbt: `{count:1,id:"minecraft:${gemType}_block"}`, count: 1 },
                    { nbt: `{count:1,id:"minecraft:${gemType}_block"}`, count: 1 },
                    { nbt: `{count:1,id:"minecraft:${gemType}_block"}`, count: 1 },
                    { nbt: `{count:1,id:"minecraft:${gemType}_block"}`, count: 1 }
                ],
                lockedSlots: [true, true, true, true, true, true, true, true, true]
            };
        }
    }
    
    return recipes;
}

function generateSlayerArmorRecipes() {
    const recipes = {};
    
    for (const tier of [1, 2]) {
        for (const slayerType of slayerTypes) {
            for (const piece of armorPieces) {
                const itemId = `${slayerType}_t${tier}_${piece}`;
                const coreId = tier === 1 ? 'custom_crafter_core_t1' : 'custom_crafter_core_t2';
                const coreItem = slayerType + '_core';
                
                recipes[itemId] = {
                    name: `${getDisplayName(slayerType)} ${tier === 1 ? 'Hunter' : 'Slayer'} ${piece.charAt(0).toUpperCase() + piece.slice(1)}`,
                    requiredSlayer: slayerType,
                    requiredLevel: tier * 10,
                    result: {
                        nbt: createSlayerArmorNBT(slayerType, tier, piece),
                        count: 1
                    },
                    ingredients: [
                        { nbt: `{count:1,id:"minecraft:${tier === 1 ? 'iron' : 'diamond'}_block"}`, count: 1 },
                        { nbt: `{count:1,id:"minecraft:${tier === 1 ? 'iron' : 'diamond'}_block"}`, count: 1 },
                        { nbt: `{count:1,id:"minecraft:${tier === 1 ? 'iron' : 'diamond'}_block"}`, count: 1 },
                        { nbt: `{count:1,id:"minecraft:${tier === 1 ? 'iron' : 'diamond'}_block"}`, count: 1 },
                        { nbt: `{components:{"minecraft:custom_data":{custom_id:"${coreItem}"},"minecraft:custom_name":{bold:1b,color:"${tier === 1 ? 'green' : 'aqua'}",text:"⚙ ${getDisplayName(slayerType)} Core"},"minecraft:enchantment_glint_override":1b},count:1,id:"minecraft:heavy_core"}`, count: 1 },
                        { nbt: `{count:1,id:"minecraft:${tier === 1 ? 'iron' : 'diamond'}_block"}`, count: 1 },
                        { nbt: `{count:1,id:"minecraft:${tier === 1 ? 'iron' : 'diamond'}_block"}`, count: 1 },
                        { nbt: `{count:1,id:"minecraft:${tier === 1 ? 'iron' : 'diamond'}_block"}`, count: 1 },
                        { nbt: `{count:1,id:"minecraft:${tier === 1 ? 'iron' : 'diamond'}_block"}`, count: 1 }
                    ],
                    lockedSlots: [true, true, true, true, true, true, true, true, true]
                };
            }
        }
    }
    
    return recipes;
}

function generateSlayerWeaponRecipes() {
    const recipes = {};
    
    for (const tier of [1, 2]) {
        for (const slayerType of slayerTypes) {
            const itemId = tier === 1 ? `${slayerType}_sword` : `${slayerType}_sword_t${tier}`;
            const coreItem = slayerType + '_core';
            
            recipes[itemId] = {
                name: `${getDisplayName(slayerType)} ${tier === 1 ? 'Hunter' : 'Slayer'} Sword`,
                requiredSlayer: slayerType,
                requiredLevel: tier * 10,
                result: {
                    nbt: createSlayerWeaponNBT(slayerType, tier),
                    count: 1
                },
                ingredients: [
                    { nbt: `{count:1,id:"minecraft:${tier === 1 ? 'iron' : 'diamond'}_block"}`, count: 1 },
                    { nbt: `{count:1,id:"minecraft:${tier === 1 ? 'iron' : 'diamond'}_block"}`, count: 1 },
                    { nbt: `{components:{"minecraft:custom_data":{custom_id:"${coreItem}"},"minecraft:custom_name":{bold:1b,color:"${tier === 1 ? 'green' : 'aqua'}",text:"⚙ ${getDisplayName(slayerType)} Core"},"minecraft:enchantment_glint_override":1b},count:1,id:"minecraft:heavy_core"}`, count: 1 },
                    { nbt: `{count:1,id:"minecraft:stick"}`, count: 1 },
                    { nbt: `{count:1,id:"minecraft:stick"}`, count: 1 }
                ],
                lockedSlots: [true, true, true, true, true]
            };
        }
    }
    
    return recipes;
}

// Helper functions for item properties
function getArmorColor(material) {
    const colors = {
        'glass': 15328988, 'obsidian': 1706542, 'quartz': 16119285, 'glowstone': 16766208,
        'redstone': 11141120, 'netherrack': 9145243, 'endstone': 14083551, 'ice': 11842740,
        'prismarine': 11250603, 'terracotta': 8626083, 'mossy': 5320730, 'soul_sand': 6717234,
        'magma': 16711680, 'sandstone': 13408613, 'amethyst': 12582656, 'coal': 2894893,
        'diamond_block': 4473924, 'emerald_block': 2651799, 'gold_block': 16766208, 'iron_block': 10987431,
        'lapis_block': 2651799, 'copper_block': 11665355, 'ancient_debris': 6717234, 'basalt': 2846468
    };
    return colors[material] || 16777215;
}

function getArmorValue(material) {
    const values = {
        'glass': 2, 'obsidian': 8, 'quartz': 3, 'glowstone': 4, 'redstone': 3, 'netherrack': 4,
        'endstone': 6, 'ice': 3, 'prismarine': 5, 'terracotta': 3, 'mossy': 4, 'soul_sand': 3,
        'magma': 7, 'sandstone': 2, 'amethyst': 5, 'coal': 2, 'diamond_block': 8, 'emerald_block': 7,
        'gold_block': 5, 'iron_block': 6, 'lapis_block': 4, 'copper_block': 5, 'ancient_debris': 9,
        'basalt': 5, 'blackstone': 6
    };
    return values[material] || 3;
}

function getToughnessValue(material) {
    const values = {
        'glass': 1, 'obsidian': 8, 'quartz': 2, 'glowstone': 2, 'redstone': 2, 'netherrack': 2,
        'endstone': 4, 'ice': 1, 'prismarine': 3, 'terracotta': 1, 'mossy': 2, 'soul_sand': 1,
        'magma': 6, 'sandstone': 1, 'amethyst': 3, 'coal': 1, 'diamond_block': 8, 'emerald_block': 6,
        'gold_block': 3, 'iron_block': 5, 'lapis_block': 2, 'copper_block': 3, 'ancient_debris': 8,
        'basalt': 3, 'blackstone': 4
    };
    return values[material] || 2;
}

function getDurability(material) {
    const values = {
        'glass': 50, 'obsidian': 2000, 'quartz': 300, 'glowstone': 200, 'redstone': 250, 'netherrack': 180,
        'endstone': 350, 'ice': 120, 'prismarine': 400, 'terracotta': 150, 'mossy': 200, 'soul_sand': 100,
        'magma': 800, 'sandstone': 80, 'amethyst': 500, 'coal': 60, 'diamond_block': 2000, 'emerald_block': 1800,
        'gold_block': 400, 'iron_block': 800, 'lapis_block': 300, 'copper_block': 350, 'ancient_debris': 2500,
        'basalt': 400, 'blackstone': 600
    };
    return values[material] || 200;
}

function getWeaponDamage(material) {
    const values = {
        'glass': 3, 'obsidian': 9, 'quartz': 5, 'glowstone': 6, 'redstone': 5, 'netherrack': 6,
        'endstone': 7, 'ice': 4, 'prismarine': 6, 'terracotta': 4, 'mossy': 5, 'soul_sand': 4,
        'magma': 8, 'sandstone': 3, 'amethyst': 6, 'coal': 3, 'diamond_block': 9, 'emerald_block': 8,
        'gold_block': 6, 'iron_block': 7, 'lapis_block': 5, 'copper_block': 6, 'ancient_debris': 10,
        'basalt': 6, 'blackstone': 7
    };
    return values[material] || 4;
}

function getWeaponDurability(material) {
    const values = {
        'glass': 100, 'obsidian': 2000, 'quartz': 400, 'glowstone': 300, 'redstone': 350, 'netherrack': 250,
        'endstone': 500, 'ice': 150, 'prismarine': 600, 'terracotta': 200, 'mossy': 300, 'soul_sand': 120,
        'magma': 1000, 'sandstone': 80, 'amethyst': 700, 'coal': 60, 'diamond_block': 2000, 'emerald_block': 1800,
        'gold_block': 400, 'iron_block': 800, 'lapis_block': 300, 'copper_block': 350, 'ancient_debris': 2500,
        'basalt': 400, 'blackstone': 600
    };
    return values[material] || 250;
}

function getDisplayName(material) {
    return material.split('_').map(word => word.charAt(0).toUpperCase() + word.slice(1)).join(' ');
}

function getColorHex(material) {
    const colors = {
        'glass': 'white', 'obsidian': 'dark_purple', 'quartz': 'white', 'glowstone': 'yellow',
        'redstone': 'red', 'netherrack': 'red', 'endstone': 'gray', 'ice': 'aqua',
        'prismarine': 'aqua', 'terracotta': 'dark_red', 'mossy': 'green', 'soul_sand': 'brown',
        'magma': 'dark_red', 'sandstone': 'yellow', 'amethyst': 'purple', 'coal': 'black',
        'diamond_block': 'aqua', 'emerald_block': 'green', 'gold_block': 'gold', 'iron_block': 'gray',
        'lapis_block': 'blue', 'copper_block': 'orange', 'ancient_debris': 'dark_gray',
        'basalt': 'dark_gray', 'blackstone': 'dark_gray'
    };
    return colors[material] || 'white';
}

function getSetBonus(material) {
    const bonuses = {
        'glass': 'Transparency & Speed boost', 'obsidian': 'Blast resistance & Fire immunity',
        'quartz': 'Experience boost', 'glowstone': 'Night vision', 'redstone': 'Haste effect',
        'netherrack': 'Fire resistance', 'endstone': 'Levitation resistance', 'ice': 'Frost walker & Speed',
        'prismarine': 'Water breathing', 'terracotta': 'Earth affinity', 'mossy': 'Nature regeneration',
        'soul_sand': 'Soul speed', 'magma': 'Fire damage', 'sandstone': 'Desert adaptation',
        'amethyst': 'Teleportation resistance', 'coal': 'Smoke screen', 'diamond_block': 'Ultimate protection',
        'emerald_block': 'Luck boost', 'gold_block': 'Golden touch', 'iron_block': 'Magnetic attraction',
        'lapis_block': 'Wisdom enhancement', 'copper_block': 'Lightning resistance',
        'ancient_debris': 'Nether immunity', 'basalt': 'Earthquake resistance',
        'blackstone': 'Shadow meld'
    };
    return bonuses[material] || 'Special abilities';
}

function getFlavorText(material) {
    const texts = {
        'glass': 'Fragile but beautiful', 'obsidian': 'Forged in volcanic fury',
        'quartz': 'Crystalline perfection', 'glowstone': 'Light in the darkness',
        'redstone': 'Powered by technology', 'netherrack': 'Born of fire',
        'endstone': 'Void-touched stone', 'ice': 'Frozen in time',
        'prismarine': 'Ocean\'s gift', 'terracotta': 'Earth-fired clay',
        'mossy': 'Ancient stone', 'soul_sand': 'Whispers of the dead',
        'magma': 'Liquid fire', 'sandstone': 'Desert winds',
        'amethyst': 'Crystal resonance', 'coal': 'Fossilized energy'
    };
    return texts[material] || 'Crafted with care';
}

function getWeaponAbility(material) {
    const abilities = {
        'glass': 'Critical strikes on weak enemies', 'obsidian': 'Armor penetration',
        'quartz': 'Experience orbs on kill', 'glowstone': 'Blind enemies',
        'redstone': 'Chain lightning', 'netherrack': 'Fire damage',
        'endstone': 'Teleport strike', 'ice': 'Slow enemies',
        'prismarine': 'Water damage boost', 'terracotta': 'Earthquake slam',
        'mossy': 'Poison damage', 'soul_sand': 'Life drain',
        'magma': 'Explosion on hit', 'sandstone': 'Sandstorm',
        'amethyst': 'Mind control', 'coal': 'Smoke bomb'
    };
    return abilities[material] || 'Enhanced damage';
}

function getSlotForPiece(piece) {
    const slots = {
        'helmet': 'head',
        'chestplate': 'chest',
        'leggings': 'legs',
        'boots': 'feet'
    };
    return slots[piece];
}

function getArmorBase(piece) {
    const bases = {
        'helmet': 'leather_helmet',
        'chestplate': 'leather_chestplate',
        'leggings': 'leather_leggings',
        'boots': 'leather_boots'
    };
    return bases[piece];
}

function getSlayerSetBonus(slayerType) {
    const bonuses = {
        'zombie': 'Undead resistance & Life steal',
        'spider': 'Wall climbing & Poison immunity',
        'skeleton': 'Precision accuracy & Arrow resistance',
        'creeper': 'Explosion immunity & Blast damage',
        'enderman': 'Teleportation & Void resistance',
        'witch': 'Potion effects & Magic resistance',
        'blaze': 'Fire immunity & Burn damage',
        'ghast': 'Flight & Fireball resistance',
        'wither_skeleton': 'Wither effect & Undead control',
        'piglin': 'Gold finding & Bartering bonus',
        'hoglin': 'Fear immunity & Charge attack',
        'zoglin': 'Berserker rage & Damage boost',
        'pillager': 'Raid leadership & Crossbow mastery',
        'vindicator': 'Charge attack & Axe mastery',
        'evoker': 'Summoning & Magic attacks',
        'ravager': 'Destruction & Stampede resistance',
        'vex': 'Flight & Phase through walls',
        'phantom': 'Night vision & Glide ability',
        'drowned': 'Underwater breathing & Trident mastery',
        'husk': 'Desert adaptation & Hunger effect',
        'stray': 'Slowness immunity & Ice arrows',
        'bogged': 'Poison resistance & Toxic attacks',
        'breeze': 'Wind control & Jump boost',
        'warden': 'Dark vision & Sonic attacks',
        'elder_guardian': 'Ocean mastery & Laser beam',
        'ender_dragon': 'Dragon flight & Devastating breath',
        'wither': 'Wither effect & Explosion resistance'
    };
    return bonuses[slayerType] || 'Special slayer abilities';
}

function getSlayerAbility(slayerType) {
    const abilities = {
        'zombie': 'Infection spread on hit',
        'spider': 'Web trapping & Poison',
        'skeleton': 'Precise shots & Bone arrows',
        'creeper': 'Explosive charge attack',
        'enderman': 'Teleport strike & Confusion',
        'witch': 'Potion effects & Curses',
        'blaze': 'Fireballs & Burn damage',
        'ghast': 'Explosive fireballs',
        'wither_skeleton': 'Wither effect & Life drain',
        'piglin': 'Gold finding & Bartering',
        'hoglin': 'Charge attack & Fear aura',
        'zoglin': 'Berserker rage & Area damage',
        'pillager': 'Raid leadership & Crossbow volley',
        'vindicator': 'Charge attack & Axe spin',
        'evoker': 'Summon vexes & Magic bolts',
        'ravager': 'Stampede & Destruction',
        'vex': 'Phase through walls & Rapid strikes',
        'phantom': 'Dive bomb & Glide attack',
        'drowned': 'Trident throw & Underwater strike',
        'husk': 'Hunger effect & Desert heat',
        'stray': 'Slowing arrows & Ice damage',
        'bogged': 'Poison cloud & Toxic spores',
        'breeze': 'Wind jump & Tornado attack',
        'warden': 'Sonic boom & Dark vision',
        'elder_guardian': 'Laser beam & Ocean power',
        'ender_dragon': 'Dragon breath & Devastating roar',
        'wither': 'Wither skulls & Explosion aura'
    };
    return abilities[slayerType] || 'Special slayer attacks';
}

// Generate ALL modular recipes
function generateAllModularRecipes() {
    console.log('Generating COMPREHENSIVE modular recipes...');
    
    const allRecipes = {};
    
    // Block Armor
    console.log('Generating Block Armor recipes...');
    Object.assign(allRecipes, generateBlockArmorRecipes());
    
    // Block Weapons
    console.log('Generating Block Weapon recipes...');
    Object.assign(allRecipes, generateBlockWeaponRecipes());
    
    // Gem Armor & Weapons
    console.log('Generating Gem Armor recipes...');
    Object.assign(allRecipes, generateGemArmorRecipes());
    
    // Slayer Armor T1 & T2
    console.log('Generating Slayer Armor T1 & T2 recipes...');
    Object.assign(allRecipes, generateSlayerArmorRecipes());
    
    // Slayer Weapons T1 & T2
    console.log('Generating Slayer Weapon T1 & T2 recipes...');
    Object.assign(allRecipes, generateSlayerWeaponRecipes());
    
    // TODO: Add more categories (Special Armor, HPEBM, Legendary Tools, etc.)
    
    console.log(`Generated ${Object.keys(allRecipes).length} modular recipes total`);
    return allRecipes;
}

// Generate and save modular recipes
const recipes = generateAllModularRecipes();

// Create modular recipe files structure
const recipeStructure = {
    blockArmor: generateBlockArmorRecipes(),
    blockWeapons: generateBlockWeaponRecipes(),
    gemArmor: generateGemArmorRecipes(),
    slayerArmorT1: Object.fromEntries(Object.entries(generateSlayerArmorRecipes()).filter(([key]) => key.includes('_t1_'))),
    slayerArmorT2: Object.fromEntries(Object.entries(generateSlayerArmorRecipes()).filter(([key]) => key.includes('_t2_'))),
    slayerWeaponsT1: Object.fromEntries(Object.entries(generateSlayerWeaponRecipes()).filter(([key]) => !key.includes('_t2'))),
    slayerWeaponsT2: Object.fromEntries(Object.entries(generateSlayerWeaponRecipes()).filter(([key]) => key.includes('_t2')))
};

// Save modular structure
const modularOutputPath = path.join(__dirname, 'config', 'political', 'custom_recipes_modular.json');
fs.writeFileSync(modularOutputPath, JSON.stringify(recipeStructure, null, 2));

// Save combined recipes
const combinedOutputPath = path.join(__dirname, 'config', 'political', 'custom_recipes.json');
fs.writeFileSync(combinedOutputPath, JSON.stringify({ recipes: Object.values(recipes) }, null, 2));

console.log(`Generated ${Object.keys(recipes).length} comprehensive modular recipes`);
console.log(`Modular structure: ${modularOutputPath}`);
console.log(`Combined file: ${combinedOutputPath}`);
console.log('\nRecipe categories:');
Object.keys(recipeStructure).forEach(category => {
    console.log(`- ${category}: ${Object.keys(recipeStructure[category]).length} recipes`);
});
console.log(`\nTotal: ${Object.keys(recipes).length} recipes`);
console.log('\n✅ Modular system ready - Edit individual recipes without affecting others!');
console.log('🚀 Ready to deploy!');
