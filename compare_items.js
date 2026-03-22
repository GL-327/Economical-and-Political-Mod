const fs = require('fs');
const path = require('path');

// Load existing item IDs from Java files
const existingItems = JSON.parse(fs.readFileSync('existing_item_ids.json', 'utf8'));
const existingSet = new Set(existingItems);

// Load recipe items from custom_recipes.json
const recipes = JSON.parse(fs.readFileSync(path.join('config', 'political', 'custom_recipes.json'), 'utf8'));

// Extract custom_id from each recipe's result NBT
const recipeItems = [];
recipes.recipes.forEach((recipe, index) => {
    const nbt = recipe.result.nbt;
    const match = nbt.match(/custom_id:"([^"]+)"/);
    if (match) {
        recipeItems.push({
            id: match[1],
            name: recipe.name,
            index: index
        });
    }
});

// Find items in recipes that don't exist in Java
const extraItems = recipeItems.filter(item => !existingSet.has(item.id));

// Find items that exist in Java but not in recipes
const recipeItemIds = new Set(recipeItems.map(item => item.id));
const missingItems = existingItems.filter(id => !recipeItemIds.has(id));

console.log('=== ANALYSIS RESULTS ===\n');
console.log(`Total existing items in Java: ${existingItems.length}`);
console.log(`Total items in recipes: ${recipeItems.length}`);
console.log(`Extra items in recipes (not in Java): ${extraItems.length}`);
console.log(`Missing items from recipes (in Java only): ${missingItems.length}`);

if (extraItems.length > 0) {
    console.log('\n=== EXTRA ITEMS IN RECIPES (need to be removed) ===');
    extraItems.forEach(item => {
        console.log(`- ${item.id} (${item.name})`);
    });
}

if (missingItems.length > 0) {
    console.log('\n=== MISSING ITEMS FROM RECIPES (need to be added) ===');
    missingItems.forEach(id => {
        console.log(`- ${id}`);
    });
    
    // Save missing items to file
    fs.writeFileSync('missing_items.json', JSON.stringify(missingItems, null, 2));
    console.log(`\nSaved ${missingItems.length} missing items to missing_items.json`);
}

// Save extra items to file
if (extraItems.length > 0) {
    fs.writeFileSync('extra_items.json', JSON.stringify(extraItems.map(e => e.id), null, 2));
    console.log(`Saved ${extraItems.length} extra items to extra_items.json`);
}
