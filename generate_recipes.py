#!/usr/bin/env python3
"""Generate complete custom_recipes.json for all 1000+ items"""

import json
import re

# Material mapping from item name to minecraft item ID
MATERIAL_MAP = {
    # Basic blocks
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
    # Wood types
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
    # Other blocks
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
}

# Core tier mapping based on material rarity
T3_MATERIALS = ['obsidian', 'netherite', 'ancient_debris', 'crying_obsidian', 'reinforced_deepslate', 'netherite_block']
T2_MATERIALS = ['diamond', 'emerald', 'amethyst', 'end_stone', 'purpur', 'sculk', 'terracotta', 'mossy_cobblestone',
                'blackstone', 'gilded_blackstone', 'prismarine_bricks', 'dark_prismarine', 'red_sandstone',
                'cut_copper', 'exposed_copper', 'weathered_copper', 'oxidised_copper', 'waxed_cut_copper',
                'chiseled_copper', 'polished_blackstone', 'crimson', 'warped', 'basalt', 'polished_basalt', 'smooth_basalt',
                'polished_deepslate', 'reinforced_deepslate', 'tuff', 'polished_tuff', 'calcite', 'dripstone',
                'chiseled_deepslate', 'chiseled_nether_brick', 'cracked_nether_brick', 'cracked_stone_brick',
                'chiseled_stone_brick', 'deepslate_bricks', 'deepslate_tiles', 'polished_deepslate',
                'ochre_froglight', 'verdant_froglight', 'pearlescent_froglight', 'shroomlight',
                'warped_stem', 'crimson_stem', 'stripped_warped_stem', 'stripped_crimson_stem',
                'stripped_bamboo_block', 'bamboo_block', 'cherry_log', 'cherry_planks', 'stripped_cherry_log',
                'mangrove_log', 'mangrove_planks', 'stripped_mangrove_log', 'mud_bricks', 'packed_mud']

def get_core_tier(material):
    """Determine core tier based on material"""
    mat_lower = material.lower()
    for t3 in T3_MATERIALS:
        if t3 in mat_lower:
            return 3
    for t2 in T2_MATERIALS:
        if t2 in mat_lower:
            return 2
    return 1

def get_material(item_base):
    """Get minecraft material ID from item base name"""
    # Try direct lookup
    if item_base in MATERIAL_MAP:
        return MATERIAL_MAP[item_base]
    # Try with _block suffix
    if item_base + '_block' in MATERIAL_MAP:
        return MATERIAL_MAP[item_base + '_block']
    # Default fallback
    return f'minecraft:{item_base}'

def snake_to_title(snake):
    """Convert snake_case to Title Case"""
    return ' '.join(word.capitalize() for word in snake.split('_'))

def create_recipe(item_id):
    """Create a recipe for an item"""
    # Skip certain items
    if any(skip in item_id for skip in ['enchanted_', 'compacted_', 'super_compacted_', 'attribute_',
                                           '_core', 'crown', 'gilded', 'midas', 'hardened', 'heavy_',
                                           'reinforced_', 'sharpened_', 'swift_', 'venomous', 'void_touched',
                                           'voidwalker', 'ender_phase', 'glistening', 'polished_gold',
                                           'pure_gold', 'shiny_gold', 'zombie_berserker', 'spider_leggings',
                                           'warden_chestplate', 'slime_boots', 'piglin_core', 'piglin_flesh',
                                           'skeleton_bow', 'weapon_attribute', 'token_', 'gilded_netherite']):
        return None
    
    # Skip if ends with underscore
    if item_id.endswith('_'):
        return None
    
    recipe = {
        'name': snake_to_title(item_id),
        'requiredSlayer': '',
        'requiredLevel': 0,
        'result': {
            'id': f'political:{item_id}',
            'count': 1
        },
        'ingredients': [],
        'lockedSlots': []
    }
    
    # Initialize 9 empty slots
    recipe['ingredients'] = ['' for _ in range(9)]
    recipe['lockedSlots'] = [True for _ in range(9)]  # All locked by default
    
    # Determine item type and generate recipe
    if item_id.endswith('_helmet'):
        generate_armor_recipe(recipe, item_id, 'helmet')
    elif item_id.endswith('_chestplate'):
        generate_armor_recipe(recipe, item_id, 'chestplate')
    elif item_id.endswith('_leggings'):
        generate_armor_recipe(recipe, item_id, 'leggings')
    elif item_id.endswith('_boots'):
        generate_armor_recipe(recipe, item_id, 'boots')
    elif item_id.endswith('_sword'):
        generate_sword_recipe(recipe, item_id)
    elif item_id.endswith('_pickaxe'):
        generate_tool_recipe(recipe, item_id, 'pickaxe')
    elif item_id.endswith('_axe'):
        generate_tool_recipe(recipe, item_id, 'axe')
    elif item_id.endswith('_shovel'):
        generate_tool_recipe(recipe, item_id, 'shovel')
    elif item_id.endswith('_hoe'):
        generate_tool_recipe(recipe, item_id, 'hoe')
    else:
        return None  # Unknown type
    
    return recipe

def generate_armor_recipe(recipe, item_id, armor_type):
    """Generate armor recipe"""
    # Extract material from item ID
    material_base = item_id.replace(f'_{armor_type}', '')
    material = get_material(material_base)
    core_tier = get_core_tier(material)
    core_id = f'political:custom_crafter_core_t{core_tier}'
    
    mat = {'id': material, 'count': 1}
    core = {'id': core_id, 'count': 1}
    
    if armor_type == 'helmet':
        recipe['ingredients'] = [mat, mat, mat, mat, '', mat, '', core, '']
    elif armor_type == 'chestplate':
        recipe['ingredients'] = [mat, '', mat, mat, mat, mat, mat, core, mat]
    elif armor_type == 'leggings':
        recipe['ingredients'] = [mat, mat, mat, mat, '', mat, mat, core, mat]
    elif armor_type == 'boots':
        recipe['ingredients'] = ['', '', '', mat, '', mat, mat, core, mat]
    
    recipe['lockedSlots'] = [i != '' for i in recipe['ingredients']]

def generate_sword_recipe(recipe, item_id):
    """Generate sword recipe"""
    material_base = item_id.replace('_sword', '')
    material = get_material(material_base)
    core_tier = get_core_tier(material)
    core_id = f'political:custom_crafter_core_t{core_tier}'
    
    mat = {'id': material, 'count': 1}
    stick = {'id': 'minecraft:stick', 'count': 1}
    core = {'id': core_id, 'count': 1}
    
    recipe['ingredients'] = ['', mat, '', '', mat, '', '', stick, core]
    recipe['lockedSlots'] = [i != '' for i in recipe['ingredients']]

def generate_tool_recipe(recipe, item_id, tool_type):
    """Generate tool recipe"""
    material_base = item_id.replace(f'_{tool_type}', '')
    material = get_material(material_base)
    core_tier = get_core_tier(material)
    core_id = f'political:custom_crafter_core_t{core_tier}'
    
    mat = {'id': material, 'count': 1}
    stick = {'id': 'minecraft:stick', 'count': 1}
    core = {'id': core_id, 'count': 1}
    
    if tool_type == 'pickaxe':
        recipe['ingredients'] = [mat, mat, mat, '', stick, '', '', stick, core]
    elif tool_type == 'axe':
        recipe['ingredients'] = [mat, mat, '', mat, stick, '', '', stick, core]
    elif tool_type == 'shovel':
        recipe['ingredients'] = ['', mat, '', '', stick, '', '', stick, core]
    elif tool_type == 'hoe':
        recipe['ingredients'] = [mat, mat, '', '', stick, '', '', stick, core]
    
    recipe['lockedSlots'] = [i != '' for i in recipe['ingredients']]

def main():
    # Read item IDs from file
    with open('all_item_ids.txt', 'r') as f:
        item_ids = [line.strip() for line in f if line.strip()]
    
    recipes = []
    skipped = []
    
    for item_id in item_ids:
        recipe = create_recipe(item_id)
        if recipe:
            recipes.append(recipe)
        else:
            skipped.append(item_id)
    
    # Write recipes to JSON file
    with open('config/political/custom_recipes.json', 'w') as f:
        json.dump(recipes, f, indent=2)
    
    print(f"Generated {len(recipes)} recipes")
    print(f"Skipped {len(skipped)} items")
    if skipped:
        print(f"Skipped items: {', '.join(skipped[:20])}{'...' if len(skipped) > 20 else ''}")

if __name__ == '__main__':
    main()
