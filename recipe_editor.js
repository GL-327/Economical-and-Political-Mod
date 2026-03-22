#!/usr/bin/env node
/**
 * Recipe Editor - Edit individual recipes without affecting others
 * Usage: node recipe_editor.js <action> <recipe_id> [options]
 */

const fs = require('fs');
const path = require('path');

const MODULAR_RECIPES_PATH = path.join(__dirname, 'config', 'political', 'custom_recipes_modular.json');
const COMBINED_RECIPES_PATH = path.join(__dirname, 'config', 'political', 'custom_recipes.json');

// Load modular recipes
let modularRecipes = {};
if (fs.existsSync(MODULAR_RECIPES_PATH)) {
    modularRecipes = JSON.parse(fs.readFileSync(MODULAR_RECIPES_PATH, 'utf8'));
}

// Helper functions
function saveModularRecipes() {
    fs.writeFileSync(MODULAR_RECIPES_PATH, JSON.stringify(modularRecipes, null, 2));
    console.log('✅ Modular recipes saved');
}

function rebuildCombinedRecipes() {
    const allRecipes = [];
    
    // Combine all categories
    Object.values(modularRecipes).forEach(category => {
        Object.values(category).forEach(recipe => {
            allRecipes.push(recipe);
        });
    });
    
    fs.writeFileSync(COMBINED_RECIPES_PATH, JSON.stringify({ recipes: allRecipes }, null, 2));
    console.log('✅ Combined recipes rebuilt');
}

function findRecipe(recipeId) {
    for (const [category, recipes] of Object.entries(modularRecipes)) {
        if (recipes[recipeId]) {
            return { category, recipe: recipes[recipeId] };
        }
    }
    return null;
}

function listRecipes(category = null) {
    if (category && modularRecipes[category]) {
        console.log(`\n📋 Recipes in category: ${category}`);
        Object.keys(modularRecipes[category]).forEach(id => {
            console.log(`  - ${id}`);
        });
    } else {
        console.log('\n📋 All Recipe Categories:');
        Object.entries(modularRecipes).forEach(([cat, recipes]) => {
            console.log(`\n${cat}: ${Object.keys(recipes).length} recipes`);
            Object.keys(recipes).forEach(id => {
                console.log(`  - ${id}`);
            });
        });
    }
}

function showRecipe(recipeId) {
    const found = findRecipe(recipeId);
    if (!found) {
        console.log(`❌ Recipe '${recipeId}' not found`);
        return;
    }
    
    console.log(`\n📄 Recipe: ${recipeId}`);
    console.log(`Category: ${found.category}`);
    console.log(`Name: ${found.recipe.name}`);
    console.log(`Required Slayer: ${found.recipe.requiredSlayer}`);
    console.log(`Required Level: ${found.recipe.requiredLevel}`);
    console.log(`Ingredients: ${found.recipe.ingredients.length}`);
    console.log(`Locked Slots: ${found.recipe.lockedSlots.length}`);
    
    if (process.argv.includes('--verbose')) {
        console.log('\nFull Recipe JSON:');
        console.log(JSON.stringify(found.recipe, null, 2));
    }
}

function editRecipe(recipeId, field, newValue) {
    const found = findRecipe(recipeId);
    if (!found) {
        console.log(`❌ Recipe '${recipeId}' not found`);
        return;
    }
    
    const recipe = found.recipe;
    
    switch (field) {
        case 'name':
            recipe.name = newValue;
            break;
        case 'requiredSlayer':
            recipe.requiredSlayer = newValue;
            break;
        case 'requiredLevel':
            recipe.requiredLevel = parseInt(newValue);
            break;
        default:
            console.log(`❌ Unknown field: ${field}`);
            console.log('Available fields: name, requiredSlayer, requiredLevel');
            return;
    }
    
    modularRecipes[found.category][recipeId] = recipe;
    saveModularRecipes();
    rebuildCombinedRecipes();
    console.log(`✅ Updated ${recipeId}.${field} = ${newValue}`);
}

function addIngredient(recipeId, ingredientNbt, count = 1) {
    const found = findRecipe(recipeId);
    if (!found) {
        console.log(`❌ Recipe '${recipeId}' not found`);
        return;
    }
    
    const recipe = found.recipe;
    recipe.ingredients.push({ nbt: ingredientNbt, count });
    recipe.lockedSlots.push(true);
    
    modularRecipes[found.category][recipeId] = recipe;
    saveModularRecipes();
    rebuildCombinedRecipes();
    console.log(`✅ Added ingredient to ${recipeId}`);
}

function removeIngredient(recipeId, index) {
    const found = findRecipe(recipeId);
    if (!found) {
        console.log(`❌ Recipe '${recipeId}' not found`);
        return;
    }
    
    const recipe = found.recipe;
    const idx = parseInt(index);
    
    if (idx < 0 || idx >= recipe.ingredients.length) {
        console.log(`❌ Invalid ingredient index: ${idx}`);
        return;
    }
    
    recipe.ingredients.splice(idx, 1);
    recipe.lockedSlots.splice(idx, 1);
    
    modularRecipes[found.category][recipeId] = recipe;
    saveModularRecipes();
    rebuildCombinedRecipes();
    console.log(`✅ Removed ingredient ${idx} from ${recipeId}`);
}

function backupRecipes() {
    const timestamp = new Date().toISOString().replace(/[:.]/g, '-');
    const backupPath = path.join(__dirname, 'config', 'political', `custom_recipes_backup_${timestamp}.json`);
    
    fs.copyFileSync(MODULAR_RECIPES_PATH, backupPath);
    console.log(`✅ Recipes backed up to: ${backupPath}`);
}

function restoreRecipes(backupFile) {
    const backupPath = path.join(__dirname, 'config', 'political', backupFile);
    
    if (!fs.existsSync(backupPath)) {
        console.log(`❌ Backup file not found: ${backupPath}`);
        return;
    }
    
    fs.copyFileSync(backupPath, MODULAR_RECIPES_PATH);
    modularRecipes = JSON.parse(fs.readFileSync(MODULAR_RECIPES_PATH, 'utf8'));
    rebuildCombinedRecipes();
    console.log(`✅ Recipes restored from: ${backupPath}`);
}

// CLI interface
function showHelp() {
    console.log(`
Recipe Editor - Edit individual recipes without affecting others

Usage: node recipe_editor.js <action> [arguments]

Actions:
  list [category]                    - List all recipes or recipes in a category
  show <recipe_id> [--verbose]       - Show recipe details
  edit <recipe_id> <field> <value>   - Edit recipe field
  add-ingredient <recipe_id> <nbt> [count] - Add ingredient to recipe
  remove-ingredient <recipe_id> <index> - Remove ingredient from recipe
  backup                            - Create backup of current recipes
  restore <backup_file>             - Restore recipes from backup
  help                              - Show this help

Fields for edit:
  name, requiredSlayer, requiredLevel

Examples:
  node recipe_editor.js list blockArmor
  node recipe_editor.js show glass_helmet --verbose
  node recipe_editor.js edit glass_helmet name "Enhanced Glass Helmet"
  node recipe_editor.js edit zombie_sword_t1 requiredLevel 15
  node recipe_editor.js add-ingredient glass_helmet "{count:1,id:\"minecraft:diamond\"}" 2
  node recipe_editor.js remove-ingredient glass_helmet 0
  node recipe_editor.js backup
  node recipe_editor.js restore custom_recipes_backup_2024-01-01T12-00-00-000Z.json

Categories:
${Object.keys(modularRecipes).map(cat => `  - ${cat}`).join('\n')}
`);
}

// Main execution
const action = process.argv[2];

switch (action) {
    case 'list':
        listRecipes(process.argv[3]);
        break;
    case 'show':
        showRecipe(process.argv[3]);
        break;
    case 'edit':
        editRecipe(process.argv[3], process.argv[4], process.argv[5]);
        break;
    case 'add-ingredient':
        addIngredient(process.argv[3], process.argv[4], process.argv[5]);
        break;
    case 'remove-ingredient':
        removeIngredient(process.argv[3], process.argv[4]);
        break;
    case 'backup':
        backupRecipes();
        break;
    case 'restore':
        restoreRecipes(process.argv[3]);
        break;
    case 'help':
    default:
        showHelp();
        break;
}
