const fs = require('fs');

const recipes = JSON.parse(fs.readFileSync('config/political/custom_recipes.json', 'utf8'));

// Find all slayer armor IDs
const slayerTypes = ['zombie', 'spider', 'skeleton', 'slime', 'enderman', 'piglin', 'warden'];
const pieces = ['helmet', 'chestplate', 'leggings', 'boots'];
const tiers = ['t1', 't2'];

const found = new Set();
const missing = [];

recipes.recipes.forEach(recipe => {
    const match = recipe.result.nbt.match(/custom_id:"([^"]+)"/);
    if (match) {
        found.add(match[1]);
    }
});

slayerTypes.forEach(type => {
    pieces.forEach(piece => {
        tiers.forEach(tier => {
            const id = `${type}_${piece}_${tier}`;
            if (!found.has(id)) {
                missing.push(id);
            }
        });
    });
});

console.log('Missing slayer armor:', missing);

// Also check slayer swords
const t1Swords = ['zombie_sword', 'spider_sword', 'skeleton_sword', 'slime_sword', 'enderman_sword', 'piglin_sword', 'warden_sword'];
const t2Swords = ['zombie_sword_t2', 'spider_sword_t2', 'skeleton_sword_t2', 'slime_sword_t2', 'enderman_sword_t2', 'piglin_sword_t2', 'warden_sword_t2'];

const missingSwords = [];
t1Swords.forEach(id => { if (!found.has(id)) missingSwords.push(id); });
t2Swords.forEach(id => { if (!found.has(id)) missingSwords.push(id); });

console.log('Missing slayer swords:', missingSwords);

// Check cores and chunks
const cores = ['zombie_core', 'spider_core', 'skeleton_core', 'slime_core', 'enderman_core', 'piglin_core', 'warden_core'];
const chunks = ['zombie_chunk', 'spider_chunk', 'skeleton_chunk', 'slime_chunk', 'enderman_chunk', 'piglin_chunk', 'warden_chunk'];

const missingCores = [];
const missingChunks = [];
cores.forEach(id => { if (!found.has(id)) missingCores.push(id); });
chunks.forEach(id => { if (!found.has(id)) missingChunks.push(id); });

console.log('Missing cores:', missingCores);
console.log('Missing chunks:', missingChunks);
