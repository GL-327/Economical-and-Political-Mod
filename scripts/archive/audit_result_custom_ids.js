const fs = require("fs");

const recipesPath = "config/political/custom_recipes.json";
const absPath = require("path").join(process.cwd(), recipesPath);
const data = JSON.parse(fs.readFileSync(absPath, "utf8"));
const recipes = Array.isArray(data) ? data : data.recipes;

let nbtHasBoth = 0;
let nbtHasItemOnly = 0;
let nbtHasCustomOnly = 0;
let nbtMissingBoth = 0;
let idPolitical = 0;
let idNonPolitical = 0;

for (const r of recipes) {
  const res = r?.result;
  if (!res || typeof res !== "object") continue;

  if (typeof res.nbt === "string") {
    const s = res.nbt;
    const hasItem = s.includes("custom_item_id");
    const hasCustom = s.includes("custom_id");

    if (hasItem && hasCustom) nbtHasBoth++;
    else if (hasItem && !hasCustom) nbtHasItemOnly++;
    else if (!hasItem && hasCustom) nbtHasCustomOnly++;
    else nbtMissingBoth++;
  } else if (typeof res.id === "string") {
    if (res.id.startsWith("political:")) idPolitical++;
    else idNonPolitical++;
  }
}

console.log({
  total: recipes.length,
  nbtHasBoth,
  nbtHasItemOnly,
  nbtHasCustomOnly,
  nbtMissingBoth,
  idPolitical,
  idNonPolitical,
});

