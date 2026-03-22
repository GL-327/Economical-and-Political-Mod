const fs = require('fs');
const path = require('path');

// Load the actual existing item IDs from Java files
const existingItems = JSON.parse(fs.readFileSync('existing_item_ids.json', 'utf8'));
const existingSet = new Set(existingItems);

// Read current recipes
const recipesPath = path.join(__dirname, 'config', 'political', 'custom_recipes.json');
const currentRecipes = JSON.parse(fs.readFileSync(recipesPath, 'utf8'));

// Filter out recipes that don't exist in Java code
const validRecipes = [];
const removedRecipes = [];

currentRecipes.recipes.forEach(recipe => {
    const nbt = recipe.result.nbt;
    const match = nbt.match(/custom_id:"([^"]+)"/);
    if (match) {
        const itemId = match[1];
        if (existingSet.has(itemId)) {
            validRecipes.push(recipe);
        } else {
            removedRecipes.push({ id: itemId, name: recipe.name });
        }
    }
});

// Find which items we're missing
const validItemIds = new Set(validRecipes.map(r => {
    const match = r.result.nbt.match(/custom_id:"([^"]+)"/);
    return match ? match[1] : null;
}).filter(Boolean));

const missingItems = existingItems.filter(id => !validItemIds.has(id));

console.log(`Total recipes in file: ${currentRecipes.recipes.length}`);
console.log(`Valid recipes (exist in Java): ${validRecipes.length}`);
console.log(`Removed recipes (don't exist): ${removedRecipes.length}`);
console.log(`Missing items (need recipes): ${missingItems.length}`);

// Save the filtered recipes
fs.writeFileSync(recipesPath, JSON.stringify({ recipes: validRecipes }, null, 2));

console.log(`\nUpdated ${recipesPath}`);
console.log(`Recipes now contain ONLY items that exist in the mod!`);

// Save report
const report = {
    summary: {
        totalInFile: currentRecipes.recipes.length,
        valid: validRecipes.length,
        removed: removedRecipes.length,
        missing: missingItems.length
    },
    removed: removedRecipes,
    missing: missingItems
};

fs.writeFileSync('recipe_fix_report.json', JSON.stringify(report, null, 2));
console.log('Saved report to recipe_fix_report.json');
