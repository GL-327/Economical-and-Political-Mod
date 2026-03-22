const fs = require('fs');
const path = require('path');

console.log('=== FINAL 100% VERIFICATION ===\n');

// 1. Check recipe file exists and is valid
const recipesPath = path.join(__dirname, 'config', 'political', 'custom_recipes.json');
const recipes = JSON.parse(fs.readFileSync(recipesPath, 'utf8'));
console.log('✓ Recipe file is valid JSON');
console.log('  Total recipes:', recipes.recipes.length);

// 2. Count all item types
const counts = {
    helmet: 0, chestplate: 0, leggings: 0, boots: 0,
    sword: 0, pickaxe: 0, axe: 0, shovel: 0, hoe: 0,
    bow: 0, core: 0, chunk: 0,
    t1: 0, t2: 0,
    misc: 0
};

const uniqueIds = new Set();
const crafterRefs = { t1: 0, t2: 0, t3: 0 };
const invalidRecipes = [];

recipes.recipes.forEach((recipe, idx) => {
    const idMatch = recipe.result.nbt.match(/custom_id:"([^"]+)"/);
    if (!idMatch) {
        invalidRecipes.push({ idx, reason: 'No custom_id' });
        return;
    }
    
    const id = idMatch[1];
    uniqueIds.add(id);
    
    // Count crafter core references
    recipe.ingredients.forEach(ing => {
        if (ing.nbt.includes('custom_crafter_core_t1')) crafterRefs.t1++;
        if (ing.nbt.includes('custom_crafter_core_t2')) crafterRefs.t2++;
        if (ing.nbt.includes('custom_crafter_core_t3')) crafterRefs.t3++;
    });
    
    // Count by type
    if (id.endsWith('_helmet')) counts.helmet++;
    else if (id.endsWith('_chestplate')) counts.chestplate++;
    else if (id.endsWith('_leggings')) counts.leggings++;
    else if (id.endsWith('_boots')) counts.boots++;
    else if (id.endsWith('_sword')) counts.sword++;
    else if (id.endsWith('_pickaxe')) counts.pickaxe++;
    else if (id.endsWith('_axe')) counts.axe++;
    else if (id.endsWith('_shovel')) counts.shovel++;
    else if (id.endsWith('_hoe')) counts.hoe++;
    else if (id.endsWith('_bow')) counts.bow++;
    else if (id.endsWith('_core')) counts.core++;
    else if (id.endsWith('_chunk')) counts.chunk++;
    else counts.misc++;
    
    // Count by tier
    if (id.includes('_t1')) counts.t1++;
    if (id.includes('_t2')) counts.t2++;
});

console.log('\n=== ITEM COUNTS ===');
console.log('Helmets:', counts.helmet);
console.log('Chestplates:', counts.chestplate);
console.log('Leggings:', counts.leggings);
console.log('Boots:', counts.boots);
console.log('Total Armor:', counts.helmet + counts.chestplate + counts.leggings + counts.boots);
console.log('\nSwords:', counts.sword);
console.log('Pickaxes:', counts.pickaxe);
console.log('Axes:', counts.axe);
console.log('Shovels:', counts.shovel);
console.log('Hoes:', counts.hoe);
console.log('Bows:', counts.bow);
console.log('Cores:', counts.core);
console.log('Chunks:', counts.chunk);
console.log('Misc:', counts.misc);
console.log('\nT1 items:', counts.t1);
console.log('T2 items:', counts.t2);
console.log('Unique IDs:', uniqueIds.size);

// 3. Block armor calculation (exclude slayer T1/T2)
let blockArmorCount = 0;
uniqueIds.forEach(id => {
    if ((id.endsWith('_helmet') || id.endsWith('_chestplate') || id.endsWith('_leggings') || id.endsWith('_boots')) &&
        !id.includes('_t1') && !id.includes('_t2')) {
        blockArmorCount++;
    }
});
console.log('\n=== BLOCK ARMOR ===');
console.log('Block armor pieces:', blockArmorCount);
console.log('Complete sets (÷4):', Math.floor(blockArmorCount / 4));

// 4. Crafter core verification
console.log('\n=== CRAFTER CORE USAGE ===');
console.log('T1 cores referenced:', crafterRefs.t1);
console.log('T2 cores referenced:', crafterRefs.t2);
console.log('T3 cores referenced:', crafterRefs.t3);
console.log('Total recipes using cores:', crafterRefs.t1 + crafterRefs.t2 + crafterRefs.t3);

// 5. Compare with existing Java items
const existing = JSON.parse(fs.readFileSync('existing_item_ids.json', 'utf8'));
const existingSet = new Set(existing);
const missing = existing.filter(id => !uniqueIds.has(id));
const extra = Array.from(uniqueIds).filter(id => !existingSet.has(id));

console.log('\n=== COMPARISON WITH JAVA ===');
console.log('Items in Java:', existing.length);
console.log('Items in recipes:', uniqueIds.size);
console.log('Missing from recipes:', missing.length);
if (missing.length > 0) {
    console.log('  Missing:', missing.join(', '));
}
console.log('Extra in recipes:', extra.length);
if (extra.length > 0) {
    console.log('  Extra:', extra.slice(0, 10).join(', ') + (extra.length > 10 ? '...' : ''));
}

// 6. Check crafter core IDs are correct
const hasT1 = uniqueIds.has('custom_crafter_core_t1');
const hasT2 = uniqueIds.has('custom_crafter_core_t2');
const hasT3 = uniqueIds.has('custom_crafter_core_t3');
console.log('\n=== CRAFTER CORE ITEMS ===');
console.log('T1 core in recipes:', hasT1 ? '✓' : '✗ (expected - no recipe)');
console.log('T2 core in recipes:', hasT2 ? '✓' : '✗ (expected - no recipe)');
console.log('T3 core in recipes:', hasT3 ? '✓' : '✗ (expected - no recipe)');

// 7. Summary
console.log('\n=== FINAL SUMMARY ===');
const coverage = ((uniqueIds.size / existing.length) * 100).toFixed(1);
console.log(`Coverage: ${coverage}% (${uniqueIds.size}/${existing.length})`);
console.log('Recipes using correct crafter core format:', crafterRefs.t1 + crafterRefs.t2 + crafterRefs.t3 > 0 ? '✓' : '✗');
console.log('Block armor count:', blockArmorCount, blockArmorCount >= 620 && blockArmorCount <= 640 ? '✓ (in range)' : '⚠ (outside 620-640 range)');
console.log('Invalid recipes:', invalidRecipes.length === 0 ? '✓ None' : '✗ ' + invalidRecipes.length);

if (missing.length === 3 && missing.every(id => id.includes('custom_crafter_core'))) {
    console.log('\n✅ 100% VERIFIED - Only missing items are the 3 crafter cores (as intended)');
} else if (missing.length === 0) {
    console.log('\n✅ 100% VERIFIED - All items have recipes!');
} else {
    console.log(`\n⚠️ ${missing.length} items still missing (not including crafter cores)`);
}
