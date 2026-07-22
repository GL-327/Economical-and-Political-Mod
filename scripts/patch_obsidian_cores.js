const fs = require("fs");
const path = require("path");
const file = path.join(__dirname, "..", "config", "political", "custom_recipes.json");
const raw = fs.readFileSync(file, "utf8");
const data = JSON.parse(raw);
let n = 0;
for (const r of data.recipes) {
  const resStr = JSON.stringify(r.result || "");
  if (!resStr.includes("obsidian_block")) continue;
  const ing = r.ingredients;
  if (!Array.isArray(ing)) continue;
  for (let i = 0; i < ing.length; i++) {
    const slot = ing[i];
    if (typeof slot === "object" && slot && slot.id === "political:custom_crafter_core_t1") {
      slot.id = "political:custom_crafter_core_t3";
      n++;
    }
  }
}
fs.writeFileSync(file, JSON.stringify(data, null, 2) + "\n");
console.log("Updated obsidian_block recipe cores:", n);
