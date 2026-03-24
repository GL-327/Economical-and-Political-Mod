const fs = require("fs");
const path = require("path");

const root = path.resolve(__dirname, "..");
const srcDir = path.join(root, "src", "main", "java", "com", "political");
const guiPath = path.join(srcDir, "CustomItemsGui.java");
const recipePath = path.join(root, "config", "political", "custom_recipes.json");

const guiText = fs.readFileSync(guiPath, "utf8");
const addCallRe = /items\.add\((\w+)\.(\w+)\(\)\);/g;

const calls = [];
let m;
while ((m = addCallRe.exec(guiText)) !== null) {
  calls.push({ className: m[1], methodName: m[2] });
}

const classToFile = {
  BlockArmor: "BlockArmor.java",
  BlockWeapons: "BlockWeapons.java",
  GemGear: "GemGear.java",
  SlayerItems: "SlayerItems.java",
  CustomItemHandler: "CustomItemHandler.java",
  LegendaryTools: "LegendaryTools.java",
  DanielsPickaxe: "DanielsPickaxe.java",
  DragonChestplates: "DragonChestplates.java",
};

function getMethodBody(source, methodName) {
  const signatureIndex = source.indexOf(methodName + "(");
  if (signatureIndex < 0) return "";
  const openBrace = source.indexOf("{", signatureIndex);
  if (openBrace < 0) return "";
  let depth = 0;
  for (let i = openBrace; i < source.length; i++) {
    const ch = source[i];
    if (ch === "{") depth++;
    if (ch === "}") {
      depth--;
      if (depth === 0) return source.slice(openBrace, i + 1);
    }
  }
  return "";
}

const customItemIdsFromCustomItems = new Set();
for (const call of calls) {
  const javaFile = classToFile[call.className];
  if (!javaFile) continue;
  const filePath = path.join(srcDir, javaFile);
  if (!fs.existsSync(filePath)) continue;

  const fileText = fs.readFileSync(filePath, "utf8");
  const body = getMethodBody(fileText, call.methodName);
  if (!body) continue;

  // Preferred: parse setCustomItemId(stack, "id")
  const setIdRe = /setCustomItemId\([^,]+,\s*"([^"]+)"\)/g;
  let idMatch;
  let foundAny = false;
  while ((idMatch = setIdRe.exec(body)) !== null) {
    customItemIdsFromCustomItems.add(idMatch[1]);
    foundAny = true;
  }

  // Fallback: parse literal custom_item_id/custom_id inside SNBT strings
  if (!foundAny) {
    const nbtIdRe = /custom_(?:item_)?id:\\"([^"]+)\\"/g;
    let nbtMatch;
    while ((nbtMatch = nbtIdRe.exec(body)) !== null) {
      customItemIdsFromCustomItems.add(nbtMatch[1]);
    }
  }
}

const recipesData = JSON.parse(fs.readFileSync(recipePath, "utf8"));
const recipes = Array.isArray(recipesData) ? recipesData : (recipesData.recipes || []);
const recipeResultIds = new Set();

for (const recipe of recipes) {
  const resultId = recipe?.result?.id;
  if (typeof resultId === "string" && resultId.startsWith("political:")) {
    recipeResultIds.add(resultId.slice("political:".length));
  }

  const nbt = recipe?.result?.nbt || "";
  let id =
    (nbt.match(/custom_item_id:"([^"]+)"/) || [])[1] ||
    (nbt.match(/"custom_item_id":"([^"]+)"/) || [])[1] ||
    (nbt.match(/custom_id:"([^"]+)"/) || [])[1] ||
    (nbt.match(/"custom_id":"([^"]+)"/) || [])[1];
  if (id) recipeResultIds.add(id);
}

const missingInRecipes = [...customItemIdsFromCustomItems].filter((id) => !recipeResultIds.has(id)).sort();

console.log("customitems_calls:", calls.length);
console.log("customitems_ids_detected:", customItemIdsFromCustomItems.size);
console.log("recipe_result_ids_detected:", recipeResultIds.size);
console.log("missing_count:", missingInRecipes.length);
if (missingInRecipes.length > 0) {
  console.log("missing_ids:");
  for (const id of missingInRecipes) {
    console.log("-", id);
  }
}
