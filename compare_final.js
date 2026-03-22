const fs = require('fs');

const existing = JSON.parse(fs.readFileSync('existing_item_ids.json', 'utf8'));
const recipes = JSON.parse(fs.readFileSync('config/political/custom_recipes.json', 'utf8'));

const recipeIds = recipes.recipes.map(r => {
    const match = r.result.nbt.match(/custom_id:"([^"]+)"/);
    return match ? match[1] : null;
}).filter(Boolean);

const recipeSet = new Set(recipeIds);
const missing = existing.filter(id => !recipeSet.has(id));
const extra = recipeIds.filter(id => !existing.includes(id));

console.log('=== FINAL COMPARISON ===');
console.log('Items in Java code:', existing.length);
console.log('Items in recipes:', recipeIds.length);
console.log('Unique recipe IDs:', recipeSet.size);
console.log('Missing from recipes:', missing.length);
console.log('Extra in recipes:', extra.length);

if (missing.length > 0) {
    console.log('\n=== MISSING ITEMS ===');
    missing.forEach(id => console.log('- ' + id));
    fs.writeFileSync('still_missing.json', JSON.stringify(missing, null, 2));
}

if (extra.length > 0) {
    console.log('\n=== EXTRA ITEMS (not in Java) ===');
    extra.forEach(id => console.log('- ' + id));
}

const coverage = ((recipeSet.size / existing.length) * 100).toFixed(1);
console.log(`\nCoverage: ${coverage}%`);
console.log(missing.length === 0 ? '\n✅ ALL ITEMS HAVE RECIPES!' : `\n⚠️ ${missing.length} items still need recipes`);
