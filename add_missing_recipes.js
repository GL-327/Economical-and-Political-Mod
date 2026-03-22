const fs = require('fs');
const path = require('path');

// Load existing recipes and missing items
const recipesPath = path.join(__dirname, 'config', 'political', 'custom_recipes.json');
const recipes = JSON.parse(fs.readFileSync(recipesPath, 'utf8'));
const missingItems = JSON.parse(fs.readFileSync('missing_items.json', 'utf8'));

// Filter out crafter cores (user will set those manually)
const itemsToAdd = missingItems.filter(id => !id.includes('custom_crafter_core'));

console.log(`Adding recipes for ${itemsToAdd.length} missing items...`);

// Helper: Get color hex for materials
const materialColors = {
    amethyst: '0x9D65C7', amethyst_cluster: '0xB88BDB',
    coal: '0x2A2A2A', copper: '0xB87333', diamond: '0x00FFFF',
    emerald: '0x50C878', gold: '0xFFD700', iron: '0xB0B0B0',
    lapis: '0x22419A', netherite: '0x383838', quartz: '0xE3DAC9',
    redstone: '0xB02E26', obsidian: '0x291842', glowstone: '0xFFEB6B',
    endstone: '0xE0E0B0', netherrack: '0x6D2B2B', ice: '0xA5F2F3',
    prismarine: '0x5F9EA0', quartz: '0xE3DAC9', sandstone: '0xDBD3A4',
    red_sandstone: '0xB5651D', soul_sand: '0x4A3B2A', magma: '0xFF4500',
    terracotta: '0xC06C4F', mossy: '0x6B8E23', glass: '0xFFFFFF',
    emerald_block: '0x50C878', diamond_block: '0x00FFFF', gold_block: '0xFFD700',
    iron_block: '0xB0B0B0', lapis_block: '0x22419A', ancient_debris: '0x5C4033',
    netherite_block: '0x383838', ender: '0x1A0A2E', void: '0x2D004D',
    midas: '0xFFD700', gilded: '0xD4AF37', pure_gold: '0xFFD700',
    polished_gold: '0xFFD700', glistening_gold: '0xFFD700', shiny_gold: '0xFFD700'
};

function getColorForMaterial(material) {
    return materialColors[material] || '0xAAAAAA';
}

// Crafter core tiers for different item types
const CORE_TIERS = {
    basic: 'custom_crafter_core_t1',      // Wood, stone, basic materials
    intermediate: 'custom_crafter_core_t2', // Iron, gold, lapis, redstone
    advanced: 'custom_crafter_core_t3'    // Diamond, emerald, netherite, obsidian
};

// Determine which crafter core tier based on item
function getCrafterCoreTier(itemId) {
    const lowerId = itemId.toLowerCase();
    
    // Advanced tier - precious/rare materials
    if (lowerId.includes('diamond') || lowerId.includes('emerald') || 
        lowerId.includes('netherite') || lowerId.includes('obsidian') ||
        lowerId.includes('ancient_debris') || lowerId.includes('void') ||
        lowerId.includes('ender') || lowerId.includes('midas') ||
        lowerId.includes('crown') || lowerId.includes('super_compacted') ||
        lowerId.includes('gilded_netherite') || lowerId.includes('enchanted_netherite')) {
        return CORE_TIERS.advanced;
    }
    
    // Intermediate tier - metals and uncommon materials
    if (lowerId.includes('iron') || lowerId.includes('gold') || 
        lowerId.includes('lapis') || lowerId.includes('redstone') ||
        lowerId.includes('quartz') || lowerId.includes('amethyst') ||
        lowerId.includes('prismarine') || lowerId.includes('gilded') ||
        lowerId.includes('polished_gold') || lowerId.includes('pure_gold') ||
        lowerId.includes('glistening') || lowerId.includes('shiny') ||
        lowerId.includes('hardened') || lowerId.includes('reinforced') ||
        lowerId.includes('lodestone') || lowerId.includes('compacted') ||
        lowerId.includes('enchanted_') || lowerId.includes('blackstone') ||
        lowerId.includes('deepslate') || lowerId.includes('nether_brick') ||
        lowerId.includes('piglin') || lowerId.includes('warden') ||
        lowerId.includes('voidwalker') || lowerId.includes('ender_phase')) {
        return CORE_TIERS.intermediate;
    }
    
    // Basic tier - everything else
    return CORE_TIERS.basic;
}

// Get base material from item ID
function getBaseMaterial(itemId) {
    const parts = itemId.split('_');
    
    // Handle compound names
    if (itemId.includes('amethyst_cluster')) return 'minecraft:amethyst_cluster';
    if (itemId.includes('blackstone_bricks')) return 'minecraft:blackstone_bricks';
    if (itemId.includes('chiseled_deepslate')) return 'minecraft:chiseled_deepslate';
    if (itemId.includes('chiseled_nether_brick')) return 'minecraft:chiseled_nether_bricks';
    if (itemId.includes('chiseled_stone_brick')) return 'minecraft:chiseled_stone_bricks';
    if (itemId.includes('cracked_nether_brick')) return 'minecraft:cracked_nether_bricks';
    if (itemId.includes('cracked_stone_brick')) return 'minecraft:cracked_stone_bricks';
    if (itemId.includes('dark_prismarine')) return 'minecraft:dark_prismarine';
    if (itemId.includes('end_stone_brick')) return 'minecraft:end_stone_bricks';
    if (itemId.includes('gilded_netherite')) return 'minecraft:gilded_blackstone'; // Gilded netherite uses gilded blackstone
    if (itemId.includes('hay_block')) return 'minecraft:hay_block';
    if (itemId.includes('honeycomb_block')) return 'minecraft:honeycomb_block';
    if (itemId.includes('obsidian_block')) return 'minecraft:obsidian';
    if (itemId.includes('polished_blackstone')) return 'minecraft:polished_blackstone';
    if (itemId.includes('prismarine_bricks')) return 'minecraft:prismarine_bricks';
    if (itemId.includes('raw_gold_block')) return 'minecraft:raw_gold_block';
    if (itemId.includes('raw_iron_block')) return 'minecraft:raw_iron_block';
    if (itemId.includes('red_sandstone')) return 'minecraft:red_sandstone';
    if (itemId.includes('reinforced_deepslate')) return 'minecraft:reinforced_deepslate';
    if (itemId.includes('sea_lantern')) return 'minecraft:sea_lantern';
    if (itemId.includes('smooth_basalt')) return 'minecraft:smooth_basalt';
    
    // Single word materials
    const materialMap = {
        'amethyst': 'minecraft:amethyst_block',
        'coal': 'minecraft:coal_block',
        'copper': 'minecraft:copper_block',
        'diamond': 'minecraft:diamond_block',
        'emerald': 'minecraft:emerald_block',
        'gold': 'minecraft:gold_block',
        'iron': 'minecraft:iron_block',
        'lapis': 'minecraft:lapis_block',
        'netherite': 'minecraft:netherite_block',
        'quartz': 'minecraft:quartz_block',
        'redstone': 'minecraft:redstone_block',
        'obsidian': 'minecraft:obsidian',
        'glowstone': 'minecraft:glowstone',
        'endstone': 'minecraft:end_stone',
        'netherrack': 'minecraft:netherrack',
        'ice': 'minecraft:ice',
        'prismarine': 'minecraft:prismarine',
        'sandstone': 'minecraft:sandstone',
        'soul_sand': 'minecraft:soul_sand',
        'magma': 'minecraft:magma_block',
        'terracotta': 'minecraft:terracotta',
        'mossy': 'minecraft:mossy_cobblestone',
        'glass': 'minecraft:glass',
        'basalt': 'minecraft:basalt',
        'blackstone': 'minecraft:blackstone',
        'deepslate': 'minecraft:deepslate',
        'gilded': 'minecraft:gilded_blackstone',
        'lodestone': 'minecraft:lodestone',
        'tuff': 'minecraft:tuff',
        'calcite': 'minecraft:calcite',
        'dripstone': 'minecraft:dripstone_block',
        'ochre_froglight': 'minecraft:ochre_froglight',
        'verdant_froglight': 'minecraft:verdant_froglight',
        'pearlescent_froglight': 'minecraft:pearlescent_froglight',
        'shroomlight': 'minecraft:shroomlight'
    };
    
    for (const [mat, item] of Object.entries(materialMap)) {
        if (itemId.includes(mat)) return item;
    }
    
    // Special items
    if (itemId.includes('crown')) return 'minecraft:gold_block';
    if (itemId === 'harveys_stick') return 'minecraft:stick';
    if (itemId === 'hermes_shoes') return 'minecraft:iron_boots';
    if (itemId === 'venomous_dagger') return 'minecraft:iron_sword';
    if (itemId === 'midas_sword') return 'minecraft:gold_sword';
    if (itemId === 'hardened_diamond_sword') return 'minecraft:diamond_sword';
    if (itemId === 'reinforced_iron_sword') return 'minecraft:iron_sword';
    if (itemId === 'sharpened_gold_sword') return 'minecraft:gold_sword';
    if (itemId === 'heavy_stone_sword') return 'minecraft:stone_sword';
    if (itemId === 'swift_wooden_sword') return 'minecraft:wooden_sword';
    if (itemId === 'void_touched_sword') return 'minecraft:netherite_sword';
    if (itemId === 'warden_chestplate') return 'minecraft:netherite_chestplate';
    if (itemId === 'voidwalker_crown') return 'minecraft:netherite_helmet';
    if (itemId === 'ender_phase_helmet') return 'minecraft:diamond_helmet';
    if (itemId === 'zombie_berserker_helmet') return 'minecraft:leather_helmet';
    if (itemId === 'spider_leggings') return 'minecraft:leather_leggings';
    if (itemId === 'skeleton_bow') return 'minecraft:bow';
    
    return 'minecraft:iron_block'; // default
}

// Generate recipe for a tool
function generateToolRecipe(itemId, toolType) {
    const coreTier = getCrafterCoreTier(itemId);
    const baseMaterial = getBaseMaterial(itemId);
    const displayName = itemId.split('_').map(w => w.charAt(0).toUpperCase() + w.slice(1)).join(' ');
    
    const toolBaseItems = {
        'pickaxe': 'minecraft:iron_pickaxe',
        'axe': 'minecraft:iron_axe',
        'shovel': 'minecraft:iron_shovel',
        'hoe': 'minecraft:iron_hoe',
        'sword': 'minecraft:iron_sword'
    };
    
    return {
        name: displayName,
        requiredSlayer: '',
        requiredLevel: 0,
        result: {
            nbt: `{components:{"minecraft:attribute_modifiers":[],"minecraft:custom_data":{custom_id:"${itemId}"},"minecraft:custom_name":{bold:1b,text:"${displayName}"},"minecraft:enchantment_glint_override":1b},"minecraft:max_damage":500},count:1,id:"${toolBaseItems[toolType] || 'minecraft:iron_pickaxe'}"}`,
            count: 1
        },
        ingredients: [
            { nbt: `{count:8,id:"${baseMaterial}"}`, count: 8 },
            { nbt: `{count:1,id:"political:${coreTier}"}`, count: 1 }
        ]
    };
}

// Generate recipe for armor
function generateArmorRecipe(itemId, piece) {
    const coreTier = getCrafterCoreTier(itemId);
    const baseMaterial = getBaseMaterial(itemId);
    const displayName = itemId.split('_').map(w => w.charAt(0).toUpperCase() + w.slice(1)).join(' ');
    
    const armorBaseItems = {
        'helmet': 'minecraft:iron_helmet',
        'chestplate': 'minecraft:iron_chestplate',
        'leggings': 'minecraft:iron_leggings',
        'boots': 'minecraft:iron_boots'
    };
    
    const colorHex = getColorForMaterial(itemId.split('_')[0]);
    
    return {
        name: displayName,
        requiredSlayer: '',
        requiredLevel: 0,
        result: {
            nbt: `{components:{"minecraft:attribute_modifiers":[],"minecraft:custom_data":{custom_id:"${itemId}"},"minecraft:custom_name":{bold:1b,text:"${displayName}"},"minecraft:dyed_color":${colorHex},"minecraft:enchantment_glint_override":1b},"minecraft:max_damage":500},count:1,id:"${armorBaseItems[piece] || 'minecraft:iron_helmet'}"}`,
            count: 1
        },
        ingredients: [
            { nbt: `{count:8,id:"${baseMaterial}"}`, count: 8 },
            { nbt: `{count:1,id:"political:${coreTier}"}`, count: 1 }
        ]
    };
}

// Generate recipe for special items
function generateSpecialRecipe(itemId) {
    const coreTier = getCrafterCoreTier(itemId);
    const baseMaterial = getBaseMaterial(itemId);
    const displayName = itemId.split('_').map(w => w.charAt(0).toUpperCase() + w.slice(1)).join(' ');
    
    let baseItem = 'minecraft:diamond';
    if (itemId.includes('sword')) baseItem = 'minecraft:iron_sword';
    else if (itemId.includes('boots')) baseItem = 'minecraft:iron_boots';
    else if (itemId.includes('helmet')) baseItem = 'minecraft:iron_helmet';
    else if (itemId.includes('chestplate')) baseItem = 'minecraft:iron_chestplate';
    else if (itemId.includes('leggings')) baseItem = 'minecraft:iron_leggings';
    else if (itemId.includes('bow')) baseItem = 'minecraft:bow';
    else if (itemId.includes('stick')) baseItem = 'minecraft:stick';
    else if (itemId.includes('shoes')) baseItem = 'minecraft:iron_boots';
    
    return {
        name: displayName,
        requiredSlayer: '',
        requiredLevel: 0,
        result: {
            nbt: `{components:{"minecraft:custom_data":{custom_id:"${itemId}"},"minecraft:custom_name":{bold:1b,text:"${displayName}"},"minecraft:enchantment_glint_override":1b},"minecraft:max_damage":500},count:1,id:"${baseItem}"}`,
            count: 1
        },
        ingredients: [
            { nbt: `{count:16,id:"${baseMaterial}"}`, count: 16 },
            { nbt: `{count:2,id:"political:${coreTier}"}`, count: 2 }
        ]
    };
}

// Generate recipe for compacted materials
function generateCompactedRecipe(itemId) {
    const coreTier = CORE_TIERS.intermediate;
    const material = itemId.replace('compacted_', '');
    const baseMap = {
        'coal': 'minecraft:coal_block',
        'copper': 'minecraft:copper_block',
        'diamond': 'minecraft:diamond_block',
        'emerald': 'minecraft:emerald_block',
        'gold': 'minecraft:gold_block',
        'iron': 'minecraft:iron_block',
        'lapis': 'minecraft:lapis_block',
        'netherite': 'minecraft:netherite_block',
        'quartz': 'minecraft:quartz_block',
        'redstone': 'minecraft:redstone_block'
    };
    const baseMaterial = baseMap[material] || 'minecraft:iron_block';
    const displayName = `Compacted ${material.charAt(0).toUpperCase() + material.slice(1)}`;
    
    return {
        name: displayName,
        requiredSlayer: '',
        requiredLevel: 0,
        result: {
            nbt: `{components:{"minecraft:custom_data":{custom_id:"${itemId}"},"minecraft:custom_name":{bold:1b,text:"${displayName}"},"minecraft:enchantment_glint_override":1b},count:1,id:"minecraft:diamond"}`,
            count: 1
        },
        ingredients: [
            { nbt: `{count:64,id:"${baseMaterial}"}`, count: 64 },
            { nbt: `{count:1,id:"political:${coreTier}"}`, count: 1 }
        ]
    };
}

// Generate recipe for super compacted materials
function generateSuperCompactedRecipe(itemId) {
    const coreTier = CORE_TIERS.advanced;
    const material = itemId.replace('super_compacted_', '');
    const compactedId = `compacted_${material}`;
    const displayName = `Super Compacted ${material.charAt(0).toUpperCase() + material.slice(1)}`;
    
    return {
        name: displayName,
        requiredSlayer: '',
        requiredLevel: 0,
        result: {
            nbt: `{components:{"minecraft:custom_data":{custom_id:"${itemId}"},"minecraft:custom_name":{bold:1b,color:"dark_purple",text:"${displayName}"},"minecraft:enchantment_glint_override":1b},count:1,id:"minecraft:netherite_ingot"}`,
            count: 1
        },
        ingredients: [
            { nbt: `{count:64,components:{"minecraft:custom_data":{custom_id:"${compactedId}"}},id:"minecraft:diamond"}`, count: 64 },
            { nbt: `{count:2,id:"political:${coreTier}"}`, count: 2 }
        ]
    };
}

// Generate recipe for enchanted items
function generateEnchantedRecipe(itemId) {
    const coreTier = CORE_TIERS.intermediate;
    const enchantedBlock = itemId.replace('enchanted_', '');
    const baseMap = {
        'amethyst_block': 'minecraft:amethyst_block',
        'basalt': 'minecraft:basalt',
        'blackstone': 'minecraft:blackstone',
        'blue_ice': 'minecraft:blue_ice',
        'brick': 'minecraft:bricks',
        'calcite': 'minecraft:calcite',
        'carrot': 'minecraft:carrot',
        'clay': 'minecraft:clay',
        'coal_block': 'minecraft:coal_block',
        'cobbled_deepslate': 'minecraft:cobbled_deepslate',
        'cobblestone': 'minecraft:cobblestone',
        'copper_block': 'minecraft:copper_block',
        'dandelion': 'minecraft:dandelion',
        'deepslate': 'minecraft:deepslate',
        'diamond_block': 'minecraft:diamond_block',
        'emerald_block': 'minecraft:emerald_block',
        'end_stone': 'minecraft:end_stone',
        'gilded_blackstone': 'minecraft:gilded_blackstone',
        'glass': 'minecraft:glass',
        'glowstone': 'minecraft:glowstone',
        'gold_block': 'minecraft:gold_block',
        'gravel': 'minecraft:gravel',
        'ice': 'minecraft:ice',
        'iron_block': 'minecraft:iron_block',
        'lapis_block': 'minecraft:lapis_block',
        'nether_brick': 'minecraft:nether_bricks',
        'netherite_block': 'minecraft:netherite_block',
        'netherrack': 'minecraft:netherrack',
        'oak_log': 'minecraft:oak_log',
        'obsidian': 'minecraft:obsidian',
        'packed_ice': 'minecraft:packed_ice',
        'potato': 'minecraft:potato',
        'prismarine': 'minecraft:prismarine',
        'purpur_block': 'minecraft:purpur_block',
        'quartz_block': 'minecraft:quartz_block',
        'red_sand': 'minecraft:red_sand',
        'redstone_block': 'minecraft:redstone_block',
        'rose': 'minecraft:poppy',
        'sand': 'minecraft:sand',
        'sea_lantern': 'minecraft:sea_lantern',
        'shroomlight': 'minecraft:shroomlight',
        'stone': 'minecraft:stone',
        'sugar_cane': 'minecraft:sugar_cane',
        'terracotta': 'minecraft:terracotta',
        'tuff': 'minecraft:tuff',
        'wheat': 'minecraft:wheat'
    };
    const baseMaterial = baseMap[enchantedBlock] || 'minecraft:stone';
    const displayName = `Enchanted ${enchantedBlock.split('_').map(w => w.charAt(0).toUpperCase() + w.slice(1)).join(' ')}`;
    
    return {
        name: displayName,
        requiredSlayer: '',
        requiredLevel: 0,
        result: {
            nbt: `{components:{"minecraft:custom_data":{custom_id:"${itemId}"},"minecraft:custom_name":{bold:1b,color:"aqua",text:"${displayName}"},"minecraft:enchantment_glint_override":1b},count:1,id:"${baseMaterial}"}`,
            count: 1
        },
        ingredients: [
            { nbt: `{count:64,id:"${baseMaterial}"}`, count: 64 },
            { nbt: `{count:1,id:"minecraft:experience_bottle"}`, count: 16 },
            { nbt: `{count:1,id:"political:${coreTier}"}`, count: 1 }
        ]
    };
}

// Generate recipe for attribute tokens
function generateAttributeTokenRecipe(itemId) {
    const coreTier = CORE_TIERS.advanced;
    const displayName = 'Attribute Token';
    
    return {
        name: displayName,
        requiredSlayer: '',
        requiredLevel: 0,
        result: {
            nbt: `{components:{"minecraft:custom_data":{custom_id:"${itemId}"},"minecraft:custom_name":{bold:1b,color:"gold",text:"Attribute Token"},"minecraft:enchantment_glint_override":1b},count:1,id:"minecraft:paper"}`,
            count: 1
        },
        ingredients: [
            { nbt: `{count:32,id:"minecraft:diamond"}`, count: 32 },
            { nbt: `{count:1,id:"political:${coreTier}"}`, count: 1 }
        ]
    };
}

// Generate recipe for attribute cores
function generateAttributeCoreRecipe(itemId) {
    const coreTier = CORE_TIERS.advanced;
    const displayName = 'Attribute Core';
    
    return {
        name: displayName,
        requiredSlayer: '',
        requiredLevel: 0,
        result: {
            nbt: `{components:{"minecraft:custom_data":{custom_id:"${itemId}"},"minecraft:custom_name":{bold:1b,color:"dark_red",text:"Attribute Core"},"minecraft:enchantment_glint_override":1b},count:1,id:"minecraft:netherite_ingot"}`,
            count: 1
        },
        ingredients: [
            { nbt: `{count:16,id:"minecraft:netherite_ingot"}`, count: 16 },
            { nbt: `{count:2,id:"political:${coreTier}"}`, count: 2 }
        ]
    };
}

// Generate recipe for weapon attribute tokens
function generateWeaponTokenRecipe(itemId) {
    const coreTier = CORE_TIERS.advanced;
    const displayName = 'Weapon Attribute Token';
    
    return {
        name: displayName,
        requiredSlayer: '',
        requiredLevel: 0,
        result: {
            nbt: `{components:{"minecraft:custom_data":{custom_id:"${itemId}"},"minecraft:custom_name":{bold:1b,color:"dark_purple",text:"Weapon Attribute Token"},"minecraft:enchantment_glint_override":1b},count:1,id:"minecraft:paper"}`,
            count: 1
        },
        ingredients: [
            { nbt: `{count:32,id:"minecraft:netherite_ingot"}`, count: 32 },
            { nbt: `{count:1,id:"political:${coreTier}"}`, count: 1 }
        ]
    };
}

// Generate recipe for HPEBM weapons
function generateHPEBMRecipe(itemId) {
    const coreTier = CORE_TIERS.advanced;
    let displayName, mk;
    
    if (itemId === 'hpebm_ultra') {
        displayName = 'HPEBM Ultra';
        mk = 'Ultra';
    } else {
        mk = itemId.replace('hpebm_mk', '');
        displayName = `HPEBM Mk.${mk}`;
    }
    
    return {
        name: displayName,
        requiredSlayer: '',
        requiredLevel: 0,
        result: {
            nbt: `{components:{"minecraft:custom_data":{custom_id:"${itemId}"},"minecraft:custom_name":{bold:1b,color:"red",text:"${displayName}"},"minecraft:enchantment_glint_override":1b},count:1,id:"minecraft:iron_sword"}`,
            count: 1
        },
        ingredients: [
            { nbt: `{count:32,id:"minecraft:redstone_block"}`, count: 32 },
            { nbt: `{count:16,id:"minecraft:iron_block"}`, count: 16 },
            { nbt: `{count:2,id:"political:${coreTier}"}`, count: 2 }
        ]
    };
}

// Generate recipe for piglin items
function generatePiglinRecipe(itemId) {
    const coreTier = CORE_TIERS.intermediate;
    const displayName = itemId.split('_').map(w => w.charAt(0).toUpperCase() + w.slice(1)).join(' ');
    const baseItem = itemId.includes('core') ? 'minecraft:nether_star' : 'minecraft:porkchop';
    
    return {
        name: displayName,
        requiredSlayer: '',
        requiredLevel: 0,
        result: {
            nbt: `{components:{"minecraft:custom_data":{custom_id:"${itemId}"},"minecraft:custom_name":{bold:1b,color:"gold",text:"${displayName}"},"minecraft:enchantment_glint_override":1b},count:1,id:"${baseItem}"}`,
            count: 1
        },
        ingredients: [
            { nbt: `{count:16,id:"minecraft:gold_block"}`, count: 16 },
            { nbt: `{count:8,id:"minecraft:porkchop"}`, count: 8 },
            { nbt: `{count:1,id:"political:${coreTier}"}`, count: 1 }
        ]
    };
}

// Main categorization and recipe generation
const newRecipes = [];

itemsToAdd.forEach(itemId => {
    let recipe = null;
    
    // Categorize item and generate appropriate recipe
    if (itemId.includes('_pickaxe')) {
        recipe = generateToolRecipe(itemId, 'pickaxe');
    } else if (itemId.includes('_axe')) {
        recipe = generateToolRecipe(itemId, 'axe');
    } else if (itemId.includes('_shovel')) {
        recipe = generateToolRecipe(itemId, 'shovel');
    } else if (itemId.includes('_hoe')) {
        recipe = generateToolRecipe(itemId, 'hoe');
    } else if (itemId.includes('_helmet') || itemId.includes('_crown')) {
        recipe = generateArmorRecipe(itemId, 'helmet');
    } else if (itemId.includes('_chestplate')) {
        recipe = generateArmorRecipe(itemId, 'chestplate');
    } else if (itemId.includes('_leggings')) {
        recipe = generateArmorRecipe(itemId, 'leggings');
    } else if (itemId.includes('_boots') || itemId.includes('_shoes')) {
        recipe = generateArmorRecipe(itemId, 'boots');
    } else if (itemId.includes('_sword') || itemId.includes('_bow') || itemId.includes('_dagger')) {
        if (itemId.includes('_sword')) {
            recipe = generateToolRecipe(itemId, 'sword');
        } else {
            recipe = generateSpecialRecipe(itemId);
        }
    } else if (itemId.startsWith('compacted_')) {
        recipe = generateCompactedRecipe(itemId);
    } else if (itemId.startsWith('super_compacted_')) {
        recipe = generateSuperCompactedRecipe(itemId);
    } else if (itemId.startsWith('enchanted_')) {
        recipe = generateEnchantedRecipe(itemId);
    } else if (itemId === 'attribute_token_') {
        recipe = generateAttributeTokenRecipe(itemId);
    } else if (itemId === 'attribute_core_') {
        recipe = generateAttributeCoreRecipe(itemId);
    } else if (itemId === 'weapon_attribute_token_') {
        recipe = generateWeaponTokenRecipe(itemId);
    } else if (itemId.startsWith('hpebm_')) {
        recipe = generateHPEBMRecipe(itemId);
    } else if (itemId.startsWith('piglin_')) {
        recipe = generatePiglinRecipe(itemId);
    } else {
        // All other special items
        recipe = generateSpecialRecipe(itemId);
    }
    
    if (recipe) {
        newRecipes.push(recipe);
    }
});

// Add new recipes to existing ones
recipes.recipes = [...recipes.recipes, ...newRecipes];

// Save updated recipes
fs.writeFileSync(recipesPath, JSON.stringify(recipes, null, 2));

console.log(`\n✅ Added ${newRecipes.length} new recipes!`);
console.log(`Total recipes in file: ${recipes.recipes.length}`);
console.log(`\nAll recipes use appropriate crafter core tiers:`);
console.log(`- T1 (basic): Wood, stone, glass, ice, etc.`);
console.log(`- T2 (intermediate): Iron, gold, lapis, enchanted, etc.`);
console.log(`- T3 (advanced): Diamond, emerald, netherite, crowns, etc.`);
console.log(`\nCrafter cores themselves have NO recipes (as requested).`);

// Save summary
const summary = {
    totalRecipes: recipes.recipes.length,
    newRecipesAdded: newRecipes.length,
    categories: {
        tools: itemsToAdd.filter(i => i.includes('_pickaxe') || i.includes('_axe') || i.includes('_shovel') || i.includes('_hoe')).length,
        armor: itemsToAdd.filter(i => i.includes('_helmet') || i.includes('_chestplate') || i.includes('_leggings') || i.includes('_boots')).length,
        weapons: itemsToAdd.filter(i => i.includes('_sword') || i.includes('_bow') || i.includes('_dagger')).length,
        materials: itemsToAdd.filter(i => i.startsWith('compacted_') || i.startsWith('super_compacted_') || i.startsWith('enchanted_')).length,
        special: itemsToAdd.filter(i => !i.includes('_') || i.includes('crown') || i.includes('warden') || i.includes('voidwalker') || i.includes('zombie_') || i.includes('spider_') || i.includes('skeleton_')).length
    }
};

fs.writeFileSync('recipe_addition_summary.json', JSON.stringify(summary, null, 2));
console.log('\nSaved summary to recipe_addition_summary.json');
