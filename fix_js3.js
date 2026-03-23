const fs = require('fs');
const path = 'c:\\Users\\Gathe\\Desktop\\Economical-and-Political-Mod\\config\\political\\custom_recipes.json';

let content = fs.readFileSync(path, 'utf8');

// Pattern with escaped quotes as they appear in the JSON file
content = content.replace(/\]},\\"minecraft:max_damage\\":\d+\},count:1,id:\\"minecraft:/g, '}]},count:1,id:\\"minecraft:');

fs.writeFileSync(path, content, 'utf8');
console.log('Done');
