const fs = require('fs');
const recipes = JSON.parse(fs.readFileSync('config/political/custom_recipes.json'));
const ids = recipes.recipes.map(x => {
    const m = x.result.nbt.match(/custom_id:"([^"]+)"/);
    return m ? m[1] : null;
}).filter(Boolean);
const unique = new Set(ids);
console.log('Total recipes:', recipes.recipes.length);
console.log('Unique IDs:', unique.size);
console.log('Duplicates:', ids.length - unique.size);
