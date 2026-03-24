const fs = require("fs");

const recipesPath = "config/political/custom_recipes.json";

const absPath = require("path").join(process.cwd(), recipesPath);
const raw = fs.readFileSync(absPath, "utf8");
const data = JSON.parse(raw);
const recipes = Array.isArray(data) ? data : data.recipes;

let changed = 0;
let coreReplaced = 0;

for (const r of recipes) {
  if (!r || !Array.isArray(r.ingredients)) continue;
  for (let i = 0; i < r.ingredients.length; i++) {
    const ing = r.ingredients[i];
    if (!ing || typeof ing !== "object") continue;
    const nbt = ing.nbt;
    if (typeof nbt !== "string") continue;
    if (!nbt.includes("minecraft:heavy_core")) continue;

    const tierMatch = nbt.match(/custom_crafter_core_t([123])/);
    if (!tierMatch) continue;
    const tier = tierMatch[1];

    const politicalId = `political:custom_crafter_core_t${tier}`;
    if (ing.id === politicalId) continue; // Already normalized

    // Replace the ingredient with an id-based stack so deserialization uses the correct custom core
    // (custom name/lore + custom_data) rather than a raw heavy_core SNBT blob.
    r.ingredients[i] = { id: politicalId, count: typeof ing.count === "number" ? ing.count : 1 };
    changed++;
    coreReplaced++;
  }
}

if (Array.isArray(data)) {
  fs.writeFileSync(absPath, JSON.stringify(recipes, null, 2) + "\n", "utf8");
} else {
  fs.writeFileSync(absPath, JSON.stringify(data, null, 2) + "\n", "utf8");
}

console.log("recipes:", recipes.length);
console.log("ingredientReplacements:", changed);
console.log("coreReplaced:", coreReplaced);
console.log("wrote:", absPath);

