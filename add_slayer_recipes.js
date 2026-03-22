const fs = require('fs');
const path = require('path');

// Load existing recipes
const recipesPath = path.join(__dirname, 'config', 'political', 'custom_recipes.json');
const recipes = JSON.parse(fs.readFileSync(recipesPath, 'utf8'));

// The 7 actual slayer types from SlayerManager.java
const slayerTypes = [
    { id: 'zombie', name: 'Zombie', icon: 'minecraft:rotten_flesh', color: '0x2E7D32' },
    { id: 'spider', name: 'Spider', icon: 'minecraft:spider_eye', color: '0x8B0000' },
    { id: 'skeleton', name: 'Skeleton', icon: 'minecraft:bone', color: '0xFFFFFF' },
    { id: 'slime', name: 'Slime', icon: 'minecraft:slime_ball', color: '0x00FF00' },
    { id: 'enderman', name: 'Enderman', icon: 'minecraft:ender_pearl', color: '0x800080' },
    { id: 'piglin', name: 'Piglin', icon: 'minecraft:gold_nugget', color: '0xFFD700' },
    { id: 'warden', name: 'Warden', icon: 'minecraft:sculk_catalyst', color: '0x4B0082' }
];

const newRecipes = [];

// Generate T1 Slayer Armor recipes
slayerTypes.forEach(type => {
    const armorPieces = ['helmet', 'chestplate', 'leggings', 'boots'];
    const armorBases = {
        helmet: 'minecraft:leather_helmet',
        chestplate: 'minecraft:leather_chestplate',
        leggings: 'minecraft:leather_leggings',
        boots: 'minecraft:leather_boots'
    };
    
    armorPieces.forEach(piece => {
        const itemId = `${type.id}_${piece}_t1`;
        const displayName = `${type.name} ${piece.charAt(0).toUpperCase() + piece.slice(1)} T1`;
        
        newRecipes.push({
            name: displayName,
            requiredSlayer: type.id.toUpperCase(),
            requiredLevel: 1,
            result: {
                nbt: `{components:{"minecraft:attribute_modifiers":[],"minecraft:custom_data":{custom_id:"${itemId}"},"minecraft:custom_name":{bold:1b,color:"${type.color}",text:"${displayName}"},"minecraft:dyed_color":${type.color},"minecraft:enchantment_glint_override":1b},"minecraft:max_damage":250},count:1,id:"${armorBases[piece]}"}`,
                count: 1
            },
            ingredients: [
                { nbt: `{count:16,id:"${type.icon}"}`, count: 16 },
                { nbt: `{count:8,id:"minecraft:iron_ingot"}`, count: 8 },
                { nbt: `{count:1,id:"political:custom_crafter_core_t1"}`, count: 1 }
            ]
        });
    });
});

// Generate T2 Slayer Armor recipes  
slayerTypes.forEach(type => {
    const armorPieces = ['helmet', 'chestplate', 'leggings', 'boots'];
    const armorBases = {
        helmet: 'minecraft:diamond_helmet',
        chestplate: 'minecraft:diamond_chestplate',
        leggings: 'minecraft:diamond_leggings',
        boots: 'minecraft:diamond_boots'
    };
    
    armorPieces.forEach(piece => {
        const itemId = `${type.id}_${piece}_t2`;
        const displayName = `${type.name} ${piece.charAt(0).toUpperCase() + piece.slice(1)} T2`;
        
        newRecipes.push({
            name: displayName,
            requiredSlayer: type.id.toUpperCase(),
            requiredLevel: 6,
            result: {
                nbt: `{components:{"minecraft:attribute_modifiers":[],"minecraft:custom_data":{custom_id:"${itemId}"},"minecraft:custom_name":{bold:1b,color:"${type.color}",text:"${displayName}"},"minecraft:dyed_color":${type.color},"minecraft:enchantment_glint_override":1b},"minecraft:max_damage":500},count:1,id:"${armorBases[piece]}"}`,
                count: 1
            },
            ingredients: [
                { nbt: `{count:32,id:"${type.icon}"}`, count: 32 },
                { nbt: `{count:16,id:"minecraft:diamond"}`, count: 16 },
                { nbt: `{count:1,id:"political:custom_crafter_core_t2"}`, count: 1 },
                { nbt: `{count:1,components:{"minecraft:custom_data":{custom_id:"${type.id}_${piece}_t1"}},id:"${armorBases[piece]}"}`, count: 1 }
            ]
        });
    });
});

// Generate T1 Slayer Sword recipes
slayerTypes.forEach(type => {
    const itemId = `${type.id}_sword`;
    const displayName = `${type.name} Slayer Sword`;
    
    newRecipes.push({
        name: displayName,
        requiredSlayer: type.id.toUpperCase(),
        requiredLevel: 1,
        result: {
            nbt: `{components:{"minecraft:attribute_modifiers":[],"minecraft:custom_data":{custom_id:"${itemId}"},"minecraft:custom_name":{bold:1b,color:"${type.color}",text:"${displayName}"},"minecraft:enchantment_glint_override":1b},"minecraft:max_damage":500},count:1,id:"minecraft:iron_sword"}`,
            count: 1
        },
        ingredients: [
            { nbt: `{count:24,id:"${type.icon}"}`, count: 24 },
            { nbt: `{count:4,id:"minecraft:iron_ingot"}`, count: 4 },
            { nbt: `{count:1,id:"political:custom_crafter_core_t1"}`, count: 1 }
        ]
    });
});

// Generate T2 Slayer Sword recipes
slayerTypes.forEach(type => {
    const itemId = `${type.id}_sword_t2`;
    const displayName = `${type.name} Slayer Sword T2`;
    
    newRecipes.push({
        name: displayName,
        requiredSlayer: type.id.toUpperCase(),
        requiredLevel: 6,
        result: {
            nbt: `{components:{"minecraft:attribute_modifiers":[],"minecraft:custom_data":{custom_id:"${itemId}"},"minecraft:custom_name":{bold:1b,color:"${type.color}",text:"${displayName}"},"minecraft:enchantment_glint_override":1b},"minecraft:max_damage":1000},count:1,id:"minecraft:diamond_sword"}`,
            count: 1
        },
        ingredients: [
            { nbt: `{count:48,id:"${type.icon}"}`, count: 48 },
            { nbt: `{count:8,id:"minecraft:diamond"}`, count: 8 },
            { nbt: `{count:1,id:"political:custom_crafter_core_t2"}`, count: 1 },
            { nbt: `{count:1,components:{"minecraft:custom_data":{custom_id:"${type.id}_sword"}},id:"minecraft:iron_sword"}`, count: 1 }
        ]
    });
});

// Generate Slayer Core recipes (boss drops, harder to craft)
slayerTypes.forEach(type => {
    const itemId = `${type.id}_core`;
    const displayName = `${type.name} Core`;
    
    newRecipes.push({
        name: displayName,
        requiredSlayer: type.id.toUpperCase(),
        requiredLevel: 3,
        result: {
            nbt: `{components:{"minecraft:custom_data":{custom_id:"${itemId}"},"minecraft:custom_name":{bold:1b,color:"gold",text:"${displayName}"},"minecraft:enchantment_glint_override":1b},count:1,id:"${type.icon}"}`,
            count: 1
        },
        ingredients: [
            { nbt: `{count:64,id:"${type.icon}"}`, count: 64 },
            { nbt: `{count:16,id:"minecraft:gold_ingot"}`, count: 16 },
            { nbt: `{count:2,id:"political:custom_crafter_core_t2"}`, count: 2 }
        ]
    });
});

// Generate Slayer Chunk recipes
slayerTypes.forEach(type => {
    const itemId = `${type.id}_chunk`;
    const displayName = `${type.name} Chunk`;
    
    newRecipes.push({
        name: displayName,
        requiredSlayer: type.id.toUpperCase(),
        requiredLevel: 1,
        result: {
            nbt: `{components:{"minecraft:custom_data":{custom_id:"${itemId}"},"minecraft:custom_name":{bold:1b,color:"aqua",text:"${displayName}"},"minecraft:enchantment_glint_override":1b},count:1,id:"${type.icon}"}`,
            count: 1
        },
        ingredients: [
            { nbt: `{count:32,id:"${type.icon}"}`, count: 32 },
            { nbt: `{count:1,id:"political:custom_crafter_core_t1"}`, count: 1 }
        ]
    });
});

// Add all new recipes
recipes.recipes = [...recipes.recipes, ...newRecipes];

// Save updated recipes
fs.writeFileSync(recipesPath, JSON.stringify(recipes, null, 2));

console.log(`✅ Added ${newRecipes.length} slayer bounty recipes!`);
console.log(`  - 28 T1 Armor pieces (7 types × 4 pieces)`);
console.log(`  - 28 T2 Armor pieces (7 types × 4 pieces)`);
console.log(`  - 7 T1 Swords`);
console.log(`  - 7 T2 Swords`);
console.log(`  - 7 Cores`);
console.log(`  - 7 Chunks`);
console.log(`\nTotal recipes in file: ${recipes.recipes.length}`);

// Save summary
const summary = {
    added: newRecipes.length,
    slayerTypes: slayerTypes.map(t => t.id),
    categories: {
        t1Armor: 28,
        t2Armor: 28,
        t1Swords: 7,
        t2Swords: 7,
        cores: 7,
        chunks: 7
    },
    totalRecipes: recipes.recipes.length
};

fs.writeFileSync('slayer_recipes_added.json', JSON.stringify(summary, null, 2));
console.log('\nSaved summary to slayer_recipes_added.json');
