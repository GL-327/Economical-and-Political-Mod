const fs = require('fs');

const recipes = JSON.parse(fs.readFileSync('config/political/custom_recipes.json', 'utf8'));

let helmetCount = 0;
let chestplateCount = 0;
let leggingsCount = 0;
let bootsCount = 0;
let totalArmor = 0;
const armorMaterials = new Set();

recipes.recipes.forEach(recipe => {
    const match = recipe.result.nbt.match(/custom_id:"([^"]+)"/);
    if (match) {
        const id = match[1];
        // Exclude slayer armor (has _t1 or _t2)
        if (!id.includes('_t1') && !id.includes('_t2')) {
            if (id.endsWith('_helmet')) {
                helmetCount++;
                totalArmor++;
                armorMaterials.add(id.replace('_helmet', ''));
            } else if (id.endsWith('_chestplate')) {
                chestplateCount++;
                totalArmor++;
            } else if (id.endsWith('_leggings')) {
                leggingsCount++;
                totalArmor++;
            } else if (id.endsWith('_boots')) {
                bootsCount++;
                totalArmor++;
            }
        }
    }
});

console.log('=== BLOCK ARMOR COUNT ===');
console.log('Helmets:', helmetCount);
console.log('Chestplates:', chestplateCount);
console.log('Leggings:', leggingsCount);
console.log('Boots:', bootsCount);
console.log('Total armor pieces:', totalArmor);
console.log('Unique materials:', armorMaterials.size);
console.log('Expected complete sets:', Math.min(helmetCount, chestplateCount, leggingsCount, bootsCount));

// Check slayer armor
let slayerArmor = 0;
recipes.recipes.forEach(recipe => {
    const match = recipe.result.nbt.match(/custom_id:"([^"]+)"/);
    if (match) {
        const id = match[1];
        if ((id.endsWith('_helmet_t1') || id.endsWith('_chestplate_t1') || id.endsWith('_leggings_t1') || id.endsWith('_boots_t1') ||
             id.endsWith('_helmet_t2') || id.endsWith('_chestplate_t2') || id.endsWith('_leggings_t2') || id.endsWith('_boots_t2')) && 
            !id.includes('zombie_berserker') && !id.includes('warden_chestplate')) {
            slayerArmor++;
        }
    }
});

console.log('\n=== SLAYER ARMOR ===');
console.log('Slayer armor pieces:', slayerArmor, '(expected: 56 = 7 types × 4 pieces × 2 tiers)');

// Check crafter core references
let t1Count = 0, t2Count = 0, t3Count = 0;
recipes.recipes.forEach(recipe => {
    recipe.ingredients.forEach(ing => {
        if (ing.nbt.includes('custom_crafter_core_t1')) t1Count++;
        if (ing.nbt.includes('custom_crafter_core_t2')) t2Count++;
        if (ing.nbt.includes('custom_crafter_core_t3')) t3Count++;
    });
});

console.log('\n=== CRAFTER CORE USAGE ===');
console.log('T1 cores used:', t1Count);
console.log('T2 cores used:', t2Count);
console.log('T3 cores used:', t3Count);
console.log('Total recipes using cores:', t1Count + t2Count + t3Count);
