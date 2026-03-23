const fs = require('fs');
const path = 'c:\\Users\\Gathe\\Desktop\\Economical-and-Political-Mod\\config\\political\\custom_recipes.json';

let content = fs.readFileSync(path, 'utf8');
let json = JSON.parse(content);
console.log('Keys:', Object.keys(json));
console.log('Type:', json.constructor.name);
if (json.recipes) {
    console.log('Recipe count:', json.recipes.length);
}
