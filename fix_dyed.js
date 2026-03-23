const fs = require('fs');
const path = 'c:\\Users\\Gathe\\Desktop\\Economical-and-Political-Mod\\config\\political\\custom_recipes.json';

let content = fs.readFileSync(path, 'utf8');

// Fix dyed_color: {rgb:123,show_in_tooltip:1b} -> just number
content = content.replace(/"minecraft:dyed_color":\{rgb:(\d+),show_in_tooltip:1b\}/g, '"minecraft:dyed_color":$1');

fs.writeFileSync(path, content, 'utf8');
console.log('Done');
