const fs = require('fs');
const path = require('path');

// Read Java files and extract setCustomItemId calls
const javaFiles = [
    'BlockArmor.java',
    'BlockWeapons.java', 
    'BlockTools.java',
    'SlayerItems.java',
    'GemGear.java',
    'LegendaryTools.java',
    'CustomItemHandler.java'
];

const existingItems = new Set();

javaFiles.forEach(file => {
    const filePath = path.join(__dirname, 'src', 'main', 'java', 'com', 'political', file);
    if (!fs.existsSync(filePath)) {
        console.log(`File not found: ${filePath}`);
        return;
    }
    
    const content = fs.readFileSync(filePath, 'utf8');
    
    // Match setCustomItemId calls with their IDs
    const regex = /setCustomItemId\([^,]+,\s*"([^"]+)"/g;
    let match;
    
    while ((match = regex.exec(content)) !== null) {
        existingItems.add(match[1]);
    }
});

// Also check SlayerItems for armor piece IDs
const slayerItemsPath = path.join(__dirname, 'src', 'main', 'java', 'com', 'political', 'SlayerItems.java');
if (fs.existsSync(slayerItemsPath)) {
    const content = fs.readFileSync(slayerItemsPath, 'utf8');
    
    // Match armor piece IDs like type.name().toLowerCase() + "_helmet_t1"
    const armorRegex = /setCustomItemId\([^,]+,\s*type\.name\(\)\.toLowerCase\(\)\s*\+\s*"_([a-z_]+)"/g;
    let armorMatch;
    
    while ((armorMatch = armorRegex.exec(content)) !== null) {
        // This gives us the suffix like "_helmet_t1"
        const suffix = armorMatch[1];
        console.log(`Found slayer armor suffix: ${suffix}`);
    }
}

console.log('\n=== ALL EXISTING ITEM IDs ===');
const sortedItems = Array.from(existingItems).sort();
sortedItems.forEach(id => console.log(id));
console.log(`\nTotal: ${sortedItems.length} items`);

// Save to file
fs.writeFileSync('existing_item_ids.json', JSON.stringify(sortedItems, null, 2));
console.log('\nSaved to existing_item_ids.json');
