const fs = require('fs');

// Material mapping
const MATERIAL_MAP = {
    'glass': 'minecraft:glass',
    'obsidian': 'minecraft:obsidian',
    'quartz': 'minecraft:quartz_block',
    'glowstone': 'minecraft:glowstone',
    'redstone': 'minecraft:redstone_block',
    'redstone_block': 'minecraft:redstone_block',
    'netherrack': 'minecraft:netherrack',
    'endstone': 'minecraft:end_stone',
    'end_stone': 'minecraft:end_stone',
    'ice': 'minecraft:packed_ice',
    'packed_ice': 'minecraft:packed_ice',
    'prismarine': 'minecraft:prismarine',
    'terracotta': 'minecraft:terracotta',
    'mossy': 'minecraft:mossy_cobblestone',
    'mossy_cobblestone': 'minecraft:mossy_cobblestone',
    'soul_sand': 'minecraft:soul_sand',
    'soul_soil': 'minecraft:soul_soil',
    'magma': 'minecraft:magma_block',
    'sponge': 'minecraft:sponge',
    'wet_sponge': 'minecraft:wet_sponge',
    'snow': 'minecraft:snow_block',
    'snow_block': 'minecraft:snow_block',
    'sculk': 'minecraft:sculk',
    'lapis': 'minecraft:lapis_block',
    'lapis_lazuli': 'minecraft:lapis_lazuli',
    'lapis_block': 'minecraft:lapis_block',
    'emerald': 'minecraft:emerald_block',
    'emerald_block': 'minecraft:emerald_block',
    'coal': 'minecraft:coal_block',
    'coal_block': 'minecraft:coal_block',
    'diamond': 'minecraft:diamond_block',
    'diamond_block': 'minecraft:diamond_block',
    'gold': 'minecraft:gold_block',
    'gold_block': 'minecraft:gold_block',
    'iron': 'minecraft:iron_block',
    'iron_block': 'minecraft:iron_block',
    'netherite': 'minecraft:netherite_block',
    'netherite_block': 'minecraft:netherite_block',
    'copper': 'minecraft:copper_block',
    'copper_block': 'minecraft:copper_block',
    'raw_iron': 'minecraft:raw_iron_block',
    'raw_iron_block': 'minecraft:raw_iron_block',
    'raw_gold': 'minecraft:raw_gold_block',
    'raw_gold_block': 'minecraft:raw_gold_block',
    'ancient_debris': 'minecraft:ancient_debris',
    'amethyst': 'minecraft:amethyst_block',
    'amethyst_block': 'minecraft:amethyst_block',
    'amethyst_cluster': 'minecraft:amethyst_cluster',
    'amethyst_shard': 'minecraft:amethyst_shard',
    'stone': 'minecraft:stone',
    'cobblestone': 'minecraft:cobblestone',
    'stone_bricks': 'minecraft:stone_bricks',
    'smooth_stone': 'minecraft:smooth_stone',
    'granite': 'minecraft:granite',
    'diorite': 'minecraft:diorite',
    'andesite': 'minecraft:andesite',
    'polished_granite': 'minecraft:polished_granite',
    'polished_diorite': 'minecraft:polished_diorite',
    'polished_andesite': 'minecraft:polished_andesite',
    'deepslate': 'minecraft:deepslate',
    'cobbled_deepslate': 'minecraft:cobbled_deepslate',
    'polished_deepslate': 'minecraft:polished_deepslate',
    'reinforced_deepslate': 'minecraft:reinforced_deepslate',
    'tuff': 'minecraft:tuff',
    'polished_tuff': 'minecraft:polished_tuff',
    'calcite': 'minecraft:calcite',
    'dripstone': 'minecraft:dripstone_block',
    'sandstone': 'minecraft:sandstone',
    'red_sandstone': 'minecraft:red_sandstone',
    'sand': 'minecraft:sand',
    'red_sand': 'minecraft:red_sand',
    'cactus': 'minecraft:cactus',
    'brick': 'minecraft:bricks',
    'bricks': 'minecraft:bricks',
    'mud_bricks': 'minecraft:mud_bricks',
    'nether_brick': 'minecraft:nether_bricks',
    'nether_bricks': 'minecraft:nether_bricks',
    'cracked_nether_brick': 'minecraft:cracked_nether_bricks',
    'cracked_stone_brick': 'minecraft:cracked_stone_bricks',
    'chiseled_stone_brick': 'minecraft:chiseled_stone_bricks',
    'chiseled_nether_brick': 'minecraft:chiseled_nether_bricks',
    'basalt': 'minecraft:basalt',
    'polished_basalt': 'minecraft:polished_basalt',
    'smooth_basalt': 'minecraft:smooth_basalt',
    'blackstone': 'minecraft:blackstone',
    'blackstone_bricks': 'minecraft:polished_blackstone_bricks',
    'polished_blackstone': 'minecraft:polished_blackstone',
    'gilded_blackstone': 'minecraft:gilded_blackstone',
    'crying_obsidian': 'minecraft:crying_obsidian',
    'obsidian_block': 'minecraft:obsidian',
    'end_stone_brick': 'minecraft:end_stone_bricks',
    'purpur': 'minecraft:purpur_block',
    'purpur_block': 'minecraft:purpur_block',
    'bone_block': 'minecraft:bone_block',
    'bone_meal': 'minecraft:bone_meal',
    'dried_kelp': 'minecraft:dried_kelp_block',
    'dried_kelp_block': 'minecraft:dried_kelp_block',
    'hay': 'minecraft:hay_block',
    'hay_block': 'minecraft:hay_block',
    'honeycomb': 'minecraft:honeycomb',
    'honeycomb_block': 'minecraft:honeycomb_block',
    'slime': 'minecraft:slime_block',
    'slime_block': 'minecraft:slime_block',
    'pumpkin': 'minecraft:pumpkin',
    'melon': 'minecraft:melon',
    'lily_pad': 'minecraft:lily_pad',
    'oak_log': 'minecraft:oak_log',
    'oak_planks': 'minecraft:oak_planks',
    'spruce_log': 'minecraft:spruce_log',
    'spruce_planks': 'minecraft:spruce_planks',
    'birch_log': 'minecraft:birch_log',
    'birch_planks': 'minecraft:birch_planks',
    'jungle_log': 'minecraft:jungle_log',
    'jungle_planks': 'minecraft:jungle_planks',
    'acacia_log': 'minecraft:acacia_log',
    'acacia_planks': 'minecraft:acacia_planks',
    'dark_oak_log': 'minecraft:dark_oak_log',
    'dark_oak_planks': 'minecraft:dark_oak_planks',
    'mangrove_log': 'minecraft:mangrove_log',
    'mangrove_planks': 'minecraft:mangrove_planks',
    'cherry_log': 'minecraft:cherry_log',
    'cherry_planks': 'minecraft:cherry_planks',
    'bamboo_block': 'minecraft:bamboo_block',
    'bamboo_planks': 'minecraft:bamboo_planks',
    'crimson_stem': 'minecraft:crimson_stem',
    'crimson_planks': 'minecraft:crimson_planks',
    'warped_stem': 'minecraft:warped_stem',
    'warped_planks': 'minecraft:warped_planks',
    'stripped_oak_log': 'minecraft:stripped_oak_log',
    'stripped_spruce_log': 'minecraft:stripped_spruce_log',
    'stripped_birch_log': 'minecraft:stripped_birch_log',
    'stripped_jungle_log': 'minecraft:stripped_jungle_log',
    'stripped_acacia_log': 'minecraft:stripped_acacia_log',
    'stripped_dark_oak_log': 'minecraft:stripped_dark_oak_log',
    'stripped_mangrove_log': 'minecraft:stripped_mangrove_log',
    'stripped_cherry_log': 'minecraft:stripped_cherry_log',
    'stripped_bamboo_block': 'minecraft:stripped_bamboo_block',
    'stripped_crimson_stem': 'minecraft:stripped_crimson_stem',
    'stripped_warped_stem': 'minecraft:stripped_warped_stem',
    'moss_block': 'minecraft:moss_block',
    'mycelium': 'minecraft:mycelium',
    'podzol': 'minecraft:podzol',
    'dirt': 'minecraft:dirt',
    'coarse_dirt': 'minecraft:coarse_dirt',
    'rooted_dirt': 'minecraft:rooted_dirt',
    'mud': 'minecraft:mud',
    'clay': 'minecraft:clay',
    'gravel': 'minecraft:gravel',
    'mangrove_roots': 'minecraft:mangrove_roots',
    'muddy_mangrove_roots': 'minecraft:muddy_mangrove_roots',
    'sea_lantern': 'minecraft:sea_lantern',
    'shroomlight': 'minecraft:shroomlight',
    'ochre_froglight': 'minecraft:ochre_froglight',
    'verdant_froglight': 'minecraft:verdant_froglight',
    'pearlescent_froglight': 'minecraft:pearlescent_froglight',
    'blue_ice': 'minecraft:blue_ice',
    'dark_prismarine': 'minecraft:dark_prismarine',
    'prismarine_bricks': 'minecraft:prismarine_bricks',
    'lodestone': 'minecraft:lodestone',
    'target': 'minecraft:target',
    'tnt': 'minecraft:tnt',
    'gold_nugget': 'minecraft:gold_nugget',
    'iron_nugget': 'minecraft:iron_nugget',
    'nether_gold_ore': 'minecraft:nether_gold_ore',
    'cut_copper': 'minecraft:cut_copper',
    'exposed_copper': 'minecraft:exposed_copper',
    'weathered_copper': 'minecraft:weathered_copper',
    'oxidised_copper': 'minecraft:oxidized_copper',
    'waxed_cut_copper': 'minecraft:waxed_cut_copper',
    'chiseled_copper': 'minecraft:chiseled_copper',
    'warped_wart_block': 'minecraft:warped_wart_block',
    'nether_wart_block': 'minecraft:nether_wart_block',
};

const T3_MATERIALS = ['obsidian', 'netherite', 'ancient_debris', 'crying_obsidian', 'reinforced_deepslate', 'netherite_block'];
const T2_MATERIALS = ['diamond', 'emerald', 'amethyst', 'end_stone', 'purpur', 'sculk', 'terracotta', 'mossy_cobblestone',
    'blackstone', 'gilded_blackstone', 'prismarine_bricks', 'dark_prismarine', 'red_sandstone',
    'cut_copper', 'exposed_copper', 'weathered_copper', 'oxidised_copper', 'waxed_cut_copper',
    'chiseled_copper', 'polished_blackstone', 'crimson', 'warped', 'basalt', 'polished_basalt', 'smooth_basalt',
    'polished_deepslate', 'reinforced_deepslate', 'tuff', 'polished_tuff', 'calcite', 'dripstone',
    'chiseled_deepslate', 'chiseled_nether_brick', 'cracked_nether_brick', 'cracked_stone_brick',
    'chiseled_stone_brick', 'deepslate_bricks', 'deepslate_tiles', 'polished_deepslate',
    'ochre_froglight', 'verdant_froglight', 'pearlescent_froglight', 'shroomlight',
    'warped_stem', 'crimson_stem', 'stripped_warped_stem', 'stripped_crimson_stem',
    'stripped_bamboo_block', 'bamboo_block', 'cherry_log', 'cherry_planks', 'stripped_cherry_log',
    'mangrove_log', 'mangrove_planks', 'stripped_mangrove_log', 'mud_bricks'];

function getCoreTier(material) {
    const matLower = material.toLowerCase();
    for (const t3 of T3_MATERIALS) {
        if (matLower.includes(t3)) return 3;
    }
    for (const t2 of T2_MATERIALS) {
        if (matLower.includes(t2)) return 2;
    }
    return 1;
}

function getMaterial(itemBase) {
    if (MATERIAL_MAP[itemBase]) return MATERIAL_MAP[itemBase];
    if (MATERIAL_MAP[itemBase + '_block']) return MATERIAL_MAP[itemBase + '_block'];
    return 'minecraft:' + itemBase.replace(/_block$/, '').replace(/_planks$/, '');
}

function snakeToTitle(snake) {
    return snake.split('_').map(w => w.charAt(0).toUpperCase() + w.slice(1)).join(' ');
}

function createRecipe(itemId) {
    // Skip non-craftable items
    const skipPatterns = ['enchanted_', 'compacted_', 'super_compacted_', 'attribute_', '_core', 'crown', 'gilded_netherite',
        'midas', 'hardened', 'heavy_', 'reinforced_', 'sharpened_', 'swift_', 'venomous', 'void_touched',
        'voidwalker', 'ender_phase', 'glistening', 'polished_gold', 'pure_gold', 'shiny_gold', 'zombie_berserker',
        'spider_leggings', 'warden_chestplate', 'slime_boots', 'piglin_core', 'piglin_flesh', 'skeleton_bow', 'weapon_attribute'];
    
    for (const pattern of skipPatterns) {
        if (itemId.includes(pattern)) return null;
    }
    if (itemId.endsWith('_')) return null;

    const recipe = {
        name: snakeToTitle(itemId),
        requiredSlayer: '',
        requiredLevel: 0,
        result: { id: `political:${itemId}`, count: 1 },
        ingredients: [],
        lockedSlots: []
    };

    // Initialize slots
    for (let i = 0; i < 9; i++) {
        recipe.ingredients.push('');
        recipe.lockedSlots.push(true);
    }

    // Determine type and generate recipe
    if (itemId.endsWith('_helmet')) {
        generateArmorRecipe(recipe, itemId, 'helmet');
    } else if (itemId.endsWith('_chestplate')) {
        generateArmorRecipe(recipe, itemId, 'chestplate');
    } else if (itemId.endsWith('_leggings')) {
        generateArmorRecipe(recipe, itemId, 'leggings');
    } else if (itemId.endsWith('_boots')) {
        generateArmorRecipe(recipe, itemId, 'boots');
    } else if (itemId.endsWith('_sword')) {
        generateSwordRecipe(recipe, itemId);
    } else if (itemId.endsWith('_pickaxe')) {
        generateToolRecipe(recipe, itemId, 'pickaxe');
    } else if (itemId.endsWith('_axe')) {
        generateToolRecipe(recipe, itemId, 'axe');
    } else if (itemId.endsWith('_shovel')) {
        generateToolRecipe(recipe, itemId, 'shovel');
    } else if (itemId.endsWith('_hoe')) {
        generateToolRecipe(recipe, itemId, 'hoe');
    } else {
        return null;
    }

    return recipe;
}

function generateArmorRecipe(recipe, itemId, type) {
    const materialBase = itemId.replace(`_${type}`, '');
    const material = getMaterial(materialBase);
    const coreTier = getCoreTier(material);
    const coreId = `political:custom_crafter_core_t${coreTier}`;
    
    const mat = { id: material, count: 1 };
    const core = { id: coreId, count: 1 };
    
    const patterns = {
        helmet: [mat, mat, mat, mat, '', mat, '', core, ''],
        chestplate: [mat, '', mat, mat, mat, mat, mat, core, mat],
        leggings: [mat, mat, mat, mat, '', mat, mat, core, mat],
        boots: ['', '', '', mat, '', mat, mat, core, mat]
    };
    
    recipe.ingredients = patterns[type];
    recipe.lockedSlots = recipe.ingredients.map(i => i !== '');
}

function generateSwordRecipe(recipe, itemId) {
    const materialBase = itemId.replace('_sword', '');
    const material = getMaterial(materialBase);
    const coreTier = getCoreTier(material);
    const coreId = `political:custom_crafter_core_t${coreTier}`;
    
    const mat = { id: material, count: 1 };
    const stick = { id: 'minecraft:stick', count: 1 };
    const core = { id: coreId, count: 1 };
    
    recipe.ingredients = ['', mat, '', '', mat, '', '', stick, core];
    recipe.lockedSlots = recipe.ingredients.map(i => i !== '');
}

function generateToolRecipe(recipe, itemId, type) {
    const materialBase = itemId.replace(`_${type}`, '');
    const material = getMaterial(materialBase);
    const coreTier = getCoreTier(material);
    const coreId = `political:custom_crafter_core_t${coreTier}`;
    
    const mat = { id: material, count: 1 };
    const stick = { id: 'minecraft:stick', count: 1 };
    const core = { id: coreId, count: 1 };
    
    const patterns = {
        pickaxe: [mat, mat, mat, '', stick, '', '', stick, core],
        axe: [mat, mat, '', mat, stick, '', '', stick, core],
        shovel: ['', mat, '', '', stick, '', '', stick, core],
        hoe: [mat, mat, '', '', stick, '', '', stick, core]
    };
    
    recipe.ingredients = patterns[type];
    recipe.lockedSlots = recipe.ingredients.map(i => i !== '');
}

// Main execution
const itemIds = fs.readFileSync('all_item_ids.txt', 'utf8').split('\n').filter(l => l.trim());

const recipes = [];
const skipped = [];

for (const itemId of itemIds) {
    const recipe = createRecipe(itemId);
    if (recipe) {
        recipes.push(recipe);
    } else {
        skipped.push(itemId);
    }
}

fs.writeFileSync('config/political/custom_recipes.json', JSON.stringify(recipes, null, 2));

console.log(`Generated ${recipes.length} recipes`);
console.log(`Skipped ${skipped.length} items`);
if (skipped.length > 0) {
    console.log(`First 20 skipped: ${skipped.slice(0, 20).join(', ')}`);
}
