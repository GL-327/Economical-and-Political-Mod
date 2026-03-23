const fs = require('fs');
const path = 'c:\\Users\\Gathe\\Desktop\\Economical-and-Political-Mod\\config\\political\\custom_recipes.json';

let content = fs.readFileSync(path, 'utf8');

// Find first occurrence and show context
let idx = content.indexOf('max_damage');
if (idx >= 0) {
    let start = Math.max(0, idx - 50);
    let end = Math.min(content.length, idx + 100);
    console.log('Found at:', idx);
    console.log('Context:', JSON.stringify(content.substring(start, end)));
} else {
    console.log('Not found');
}
