#!/usr/bin/env node
/**
 * CORRECTED Custom Recipe Generator - Only uses materials that exist in BOTH BlockArmor and BlockWeapons
 */

const fs = require('fs');
const path = require('path');

// Materials that exist in BOTH BlockArmor AND BlockWeapons (146 materials)
const commonMaterials = [
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
    'tuff', 'polished_tuff', 'nether_wart_block', 'warped_wart_block', 'chiseled_deepslate',
    'reinforced_deepslate', 'chiseled_nether_brick', 'cracked_nether_brick', 'chiseled_stone_brick',
    'cracked_stone_brick', 'end_stone_brick', 'red_sandstone', 'raw_iron_block', 'raw_gold_block',
    'prismarine_bricks', 'dark_prismarine', 'sea_lantern', 'lodestone', 'blackstone_bricks',
    'polished_blackstone', 'smooth_basalt', 'amethyst_cluster', 'obsidian_block',
    'stripped_spruce_log', 'stripped_birch_log', 'stripped_dark_oak_log', 'stripped_jungle_log',
    'stripped_acacia_log', 'stripped_mangrove_log', 'stripped_cherry_log', 'stripped_bamboo_block',
    'stripped_crimson_stem', 'stripped_warped_stem', 'spruce_planks', 'birch_planks',
    'jungle_planks', 'acacia_planks', 'dark_oak_planks', 'mangrove_planks', 'cherry_planks',
    'bamboo_planks', 'crimson_planks', 'warped_planks', 'oak_planks', 'oak_log', 'spruce_log',
    'birch_log', 'stone_bricks', 'cobblestone', 'mossy_cobblestone', 'cobbled_deepslate',
    'mud_bricks', 'mangrove_roots', 'muddy_mangrove_roots', 'netherite_block', 'chiseled_copper',
    'cut_copper', 'exposed_copper', 'weathered_copper', 'oxidised_copper', 'waxed_cut_copper',
    'polished_basalt', 'verdant_froglight', 'pearlescent_froglight', 'ochre_froglight',
    'iron_nugget', 'gold_nugget', 'copper_ingot', 'emerald', 'lapis_lazuli', 'amethyst_shard',
    'flint', 'bone_meal', 'charcoal', 'end_stone', 'snow_block'
];

const armorPieces = ['helmet', 'chestplate', 'leggings', 'boots'];
const toolTypes = ['pickaxe', 'axe', 'shovel', 'hoe'];

// Helper Functions
function getCoreForMaterial(material) {
    const t1 = ['glass', 'sand', 'hay', 'lily_pad', 'melon', 'cactus', 'tnt', 'wet_sponge', 'snow', 'dirt', 'grass_block', 'gravel', 'cobblestone', 'stone', 'oak_log', 'spruce_log', 'birch_log', 'wool_white', 'wool_black', 'carpet_white', 'carpet_black', 'concrete_white', 'concrete_black', 'terracotta', 'bed_white', 'bed_black'];
    const t2 = ['quartz', 'glowstone', 'redstone', 'netherrack', 'ice', 'prismarine', 'terracotta',
                'sandstone', 'coal', 'copper_block', 'bone_block', 'brick', 'calcite', 'honeycomb',
                'moss_block', 'mycelium', 'pumpkin', 'slime', 'sponge', 'target', 'dripstone',
                'granite', 'diorite', 'andesite', 'polished_granite', 'polished_diorite', 'polished_andesite',
                'tuff', 'polished_tuff', 'bamboo_block', 'crimson_stem', 'gilded_blackstone',
                'copper_ore', 'iron_ore', 'gold_ore', 'coal_ore', 'lapis_ore', 'redstone_ore',
                'nether_quartz_ore', 'deepslate_copper_ore', 'deepslate_iron_ore', 'deepslate_gold_ore',
                'deepslate_coal_ore', 'deepslate_lapis_ore', 'deepslate_redstone_ore',
                'wool_orange', 'wool_magenta', 'wool_light_blue', 'wool_yellow', 'wool_lime',
                'wool_pink', 'wool_gray', 'wool_light_gray', 'wool_cyan', 'wool_purple',
                'wool_blue', 'wool_brown', 'wool_green', 'wool_red',
                'concrete_orange', 'concrete_magenta', 'concrete_light_blue', 'concrete_yellow',
                'concrete_lime', 'concrete_pink', 'concrete_gray', 'concrete_light_gray',
                'concrete_cyan', 'concrete_purple', 'concrete_blue', 'concrete_brown',
                'concrete_green', 'concrete_red'];
    const t3 = ['obsidian', 'endstone', 'mossy', 'soul_sand', 'magma', 'amethyst', 'diamond_block',
                'emerald_block', 'gold_block', 'iron_block', 'lapis_block', 'ancient_debris', 'basalt',
                'blackstone', 'deepslate', 'nether_brick', 'purpur', 'sculk', 'shroomlight', 'smooth_stone',
                'soul_soil', 'warped', 'crying_obsidian', 'packed_ice', 'blue_ice', 'nether_gold_ore',
                'dark_oak_log', 'jungle_log', 'acacia_log', 'mangrove_log', 'cherry_log',
                'nether_wart_block', 'warped_wart_block', 'diamond_ore', 'emerald_ore', 'ancient_debris_ore',
                'deepslate_diamond_ore', 'deepslate_emerald_ore', 'deepslate_tuff', 'calcite',
                'stained_glass_white', 'stained_glass_black', 'stained_glass_pane_white',
                'stained_glass_pane_black', 'banner_white', 'banner_black', 'shulker_box_white',
                'shulker_box_black', 'glazed_terracotta_white', 'glazed_terracotta_black',
                'terracotta_white', 'terracotta_black', 'carpet_orange', 'carpet_magenta',
                'carpet_light_blue', 'carpet_yellow', 'carpet_lime', 'carpet_pink', 'carpet_gray',
                'carpet_light_gray', 'carpet_cyan', 'carpet_purple', 'carpet_blue', 'carpet_brown',
                'carpet_green', 'carpet_red'];
    
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
        'wet_sponge': 'minecraft:wet_sponge',
        'crimson_stem': 'minecraft:crimson_stem',
        'crying_obsidian': 'minecraft:crying_obsidian',
        'gilded_blackstone': 'minecraft:gilded_blackstone',
        'granite': 'minecraft:granite',
        'diorite': 'minecraft:diorite',
        'andesite': 'minecraft:andesite',
        'polished_granite': 'minecraft:polished_granite',
        'polished_diorite': 'minecraft:polished_diorite',
        'polished_andesite': 'minecraft:polished_andesite',
        'packed_ice': 'minecraft:packed_ice',
        'blue_ice': 'minecraft:blue_ice',
        'nether_gold_ore': 'minecraft:nether_gold_ore',
        'dark_oak_log': 'minecraft:dark_oak_log',
        'jungle_log': 'minecraft:jungle_log',
        'acacia_log': 'minecraft:acacia_log',
        'mangrove_log': 'minecraft:mangrove_log',
        'cherry_log': 'minecraft:cherry_log',
        'bamboo_block': 'minecraft:bamboo_block',
        'tuff': 'minecraft:tuff',
        'polished_tuff': 'minecraft:polished_tuff',
        'nether_wart_block': 'minecraft:nether_wart_block',
        'warped_wart_block': 'minecraft:warped_wart_block'
    };
    return mapping[material] || `minecraft:${material}`;
}

function createArmorNBT(material, piece) {
    const color = getArmorColor(material);
    const armorValue = getArmorValue(material);
    const toughnessValue = getToughnessValue(material);
    
    return `{components:{\"minecraft:attribute_modifiers\":[{amount:${armorValue},id:\"politicalserver:${material}_${piece}_armor\",operation:\"add_value\",slot:\"${getSlotForPiece(piece)}\",type:\"minecraft:armor\"},{amount:${toughnessValue},id:\"politicalserver:${material}_${piece}_toughness\",operation:\"add_value\",slot:\"${getSlotForPiece(piece)}\",type:\"minecraft:armor_toughness\"}],\"minecraft:custom_data\":{custom_item_id:\"${material}_${piece}\"},\"minecraft:custom_name\":{bold:1b,color:\"${getColorHex(material)}\",text:\"${getDisplayName(material)} ${piece.charAt(0).toUpperCase() + piece.slice(1)}\"},\"minecraft:dyed_color\":${color},\"minecraft:enchantment_glint_override\":1b,\"minecraft:lore\":[\"\",{color:\"${getColorHex(material)}\",text:\"━━━ STATS ━━━\"},{color:\"white\",text:\"🛡 Armor: \"},{color:\"${getColorHex(material)}\",text:\"+${armorValue}\"},{color:\"white\",text:\"💎 Toughness: \"},{color:\"${getColorHex(material)}\",text:\"+${toughnessValue}\"},\"\",{color:\"gold\",text:\"━━━ SET BONUS ━━━\"},{color:\"yellow\",text:\"✦ Full set: ${getSetBonus(material)}\"},\"\",{color:\"dark_gray\",text:\"§8${getFlavorText(material)}\"}]},\"minecraft:max_damage\":${getDurability(material)}},count:1,id:\"minecraft:${getArmorBase(piece)}\"}`;
}

function createWeaponNBT(material) {
    const damage = getWeaponDamage(material);
    const color = getColorHex(material);
    
    return `{components:{\"minecraft:attribute_modifiers\":[{amount:${damage},id:\"politicalserver:${material}_sword_damage\",operation:\"add_value\",slot:\"mainhand\",type:\"minecraft:attack_damage\"}],\"minecraft:custom_data\":{custom_item_id:\"${material}_sword\"},\"minecraft:custom_name\":{bold:1b,color:\"${color}\",text:\"${getDisplayName(material)} Sword\"},\"minecraft:enchantment_glint_override\":1b,\"minecraft:lore\":[\"\",{color:\"${color}\",text:\"━━━ STATS ━━━\"},{color:\"white\",text:\"⚔ Attack Damage: \"},{color:\"${color}\",text:\"+${damage}\"},\"\",{color:\"gold\",text:\"━━━ ABILITY ━━━\"},{color:\"yellow\",text:\"✦ ${getWeaponAbility(material)}\"},\"\",{color:\"dark_gray\",text:\"§8${getFlavorText(material)}\"}]},\"minecraft:max_damage\":${getWeaponDurability(material)}},count:1,id:\"minecraft:iron_sword\"}`;
}

function createCoreNBT(coreType) {
    const tier = coreType.slice(-1);
    const color = tier === '1' ? 'green' : tier === '2' ? 'aqua' : 'light_purple';
    const rarity = tier === '1' ? 'Uncommon' : tier === '2' ? 'Rare' : 'Legendary';
    
    return `{components:{\"minecraft:custom_data\":{custom_item_id:\"${coreType}\"},\"minecraft:custom_name\":{bold:1b,color:\"${color}\",text:\"⚙ Custom Crafting Core (T${tier})\"},\"minecraft:enchantment_glint_override\":1b,\"minecraft:lore\":[\"\",{bold:1b,color:\"${color}\",text:\"✦ ${rarity} Crafting Component\"},\"\",{color:\"gray\",text:\"Used to craft Tier ${tier} custom\"},{color:\"gray\",text:\"armor and weapon pieces.\"},\"\",{color:\"yellow\",text:\"Required for:\"},{color:\"${color}\",text:\"• All T${tier} Custom Armor Sets\"},\"\",{color:\"dark_gray\",text:\"§8[Rare drop from high-tier bosses]\"}]},count:1,id:\"minecraft:heavy_core\"}`;
}

function createArmorRecipe(material, piece) {
    const itemId = `${material}_${piece}`;
    const coreId = getCoreForMaterial(material);
    const materialId = getMaterialIngredient(material);
    
    return {
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

function createWeaponRecipe(material) {
    const coreId = getCoreForMaterial(material);
    const materialId = getMaterialIngredient(material);
    
    return {
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

// Generate CORRECTED recipes
function generateCorrectedRecipes() {
    const recipes = [];
    
    console.log('Generating CORRECTED Block Armor recipes...');
    // Block Armor recipes (146 materials × 4 pieces = 584 recipes)
    for (const material of commonMaterials) {
        for (const piece of armorPieces) {
            recipes.push(createArmorRecipe(material, piece));
        }
    }
    
    console.log('Generating CORRECTED Block Weapon recipes...');
    // Block Weapon recipes (146 materials × 1 sword = 146 recipes)
    for (const material of commonMaterials) {
        recipes.push(createWeaponRecipe(material));
    }
    
    console.log(`Generated ${recipes.length} CORRECTED recipes total`);
    return recipes;
}

// Generate and save
const recipes = generateCorrectedRecipes();
const output = { recipes };

const outputPath = path.join(__dirname, 'config', 'political', 'custom_recipes_corrected.json');
fs.writeFileSync(outputPath, JSON.stringify(output, null, 2));

console.log(`Generated ${recipes.length} CORRECTED recipes with NBT format`);
console.log(`Output: ${outputPath}`);
console.log('\nCORRECTED Recipe types included:');
console.log(`- Block Armor: ${commonMaterials.length * armorPieces.length} recipes`);
console.log(`- Block Weapons: ${commonMaterials.length} recipes`);
console.log(`Total: ${recipes.length} recipes`);
console.log('\nAll recipes use materials that exist in BOTH BlockArmor AND BlockWeapons!');
console.log('Ready to deploy!');
