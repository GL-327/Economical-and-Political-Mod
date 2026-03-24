const fs = require("fs");
const path = require("path");

const root = path.resolve(__dirname, "..");
const recipesPath = path.join(root, "config", "political", "custom_recipes.json");
const auditPath = path.join(root, "scripts", "audit_customitems_vs_recipes.js");

function runAuditAndGetMissing() {
  const { spawnSync } = require("child_process");
  const out = spawnSync(process.execPath, [auditPath], { encoding: "utf8" });
  if (out.status !== 0) {
    throw new Error("Audit script failed:\n" + out.stderr);
  }
  const lines = out.stdout.split(/\r?\n/);
  const missing = [];
  let inMissing = false;
  for (const line of lines) {
    if (line.trim() === "missing_ids:") {
      inMissing = true;
      continue;
    }
    if (!inMissing) continue;
    const m = line.match(/^\s*-\s+(.+)\s*$/);
    if (m) missing.push(m[1].trim());
  }
  return missing;
}

function coreTierForId(id) {
  const v = id.toLowerCase();
  if (
    v.includes("netherite") ||
    v.includes("obsidian") ||
    v.includes("dragon") ||
    v.includes("void") ||
    v.includes("midas")
  ) return 3;
  if (
    v.includes("diamond") ||
    v.includes("emerald") ||
    v.includes("end") ||
    v.includes("gilded") ||
    v.includes("enchanted") ||
    v.includes("compacted") ||
    v.includes("copper") ||
    v.includes("amethyst")
  ) return 2;
  return 1;
}

function materialForBase(base) {
  const map = {
    endstone: "minecraft:end_stone",
    end_stone: "minecraft:end_stone",
    ice: "minecraft:packed_ice",
    lapis: "minecraft:lapis_block",
    lapis_lazuli: "minecraft:lapis_lazuli",
    emerald: "minecraft:emerald_block",
    diamond: "minecraft:diamond_block",
    gold: "minecraft:gold_block",
    iron: "minecraft:iron_block",
    copper: "minecraft:copper_block",
    coal: "minecraft:coal_block",
    redstone: "minecraft:redstone_block",
    quartz: "minecraft:quartz_block",
    bone_meal: "minecraft:bone_meal",
    gold_nugget: "minecraft:gold_nugget",
    iron_nugget: "minecraft:iron_nugget",
    copper_ingot: "minecraft:copper_ingot",
    amethyst_shard: "minecraft:amethyst_shard",
    flint: "minecraft:flint",
    charcoal: "minecraft:charcoal",
    compacted_iron: "minecraft:iron_block",
    compacted_gold: "minecraft:gold_block",
    compacted_diamond: "minecraft:diamond_block",
    compacted_emerald: "minecraft:emerald_block",
    compacted_netherite: "minecraft:netherite_block",
    compacted_copper: "minecraft:copper_block",
    compacted_coal: "minecraft:coal_block",
    compacted_lapis: "minecraft:lapis_block",
    compacted_redstone: "minecraft:redstone_block",
    compacted_quartz: "minecraft:quartz_block",
  };
  if (map[base]) return map[base];
  if (base.endsWith("_planks") || base.endsWith("_log") || base.endsWith("_stem") || base.endsWith("_roots")) {
    return "minecraft:" + base;
  }
  return "minecraft:" + base;
}

function coreIngredient(tier) {
  return { id: `political:custom_crafter_core_t${tier}`, count: 1 };
}

function ingredient(id) {
  return { id, count: 1 };
}

function armorPattern(mat, core) {
  return [mat, mat, mat, mat, core, mat, "", "", ""];
}
function chestPattern(mat, core) {
  return [mat, "", mat, mat, core, mat, mat, mat, mat];
}
function leggingsPattern(mat, core) {
  return [mat, mat, mat, mat, core, mat, mat, "", mat];
}
function bootsPattern(mat, core) {
  return ["", "", "", mat, core, mat, mat, "", mat];
}
function swordPattern(mat, core) {
  return ["", mat, "", "", core, "", "", ingredient("minecraft:stick"), ""];
}
function pickaxePattern(mat, core) {
  return [mat, mat, mat, "", core, "", "", ingredient("minecraft:stick"), ""];
}
function axePattern(mat, core) {
  return [mat, mat, "", mat, core, "", "", ingredient("minecraft:stick"), ""];
}
function shovelPattern(mat, core) {
  return ["", mat, "", "", core, "", "", ingredient("minecraft:stick"), ""];
}
function hoePattern(mat, core) {
  return [mat, mat, "", "", core, "", "", ingredient("minecraft:stick"), ""];
}

function recipeForId(id) {
  const tier = coreTierForId(id);
  const core = coreIngredient(tier);
  const baseResult = { id: `political:${id}`, count: 1 };
  const r = {
    name: id.replaceAll("_", " ").replace(/\b\w/g, (c) => c.toUpperCase()),
    requiredSlayer: "",
    requiredLevel: 0,
    result: baseResult,
    ingredients: ["", "", "", "", "", "", "", "", ""],
    lockedSlots: [true, true, true, true, true, true, true, true, true],
  };

  if (id.endsWith("_helmet")) {
    const base = id.slice(0, -"_helmet".length);
    const mat = ingredient(materialForBase(base));
    r.ingredients = armorPattern(mat, core);
    return r;
  }
  if (id.endsWith("_chestplate")) {
    const base = id.slice(0, -"_chestplate".length);
    const mat = ingredient(materialForBase(base));
    r.ingredients = chestPattern(mat, core);
    return r;
  }
  if (id.endsWith("_leggings")) {
    const base = id.slice(0, -"_leggings".length);
    const mat = ingredient(materialForBase(base));
    r.ingredients = leggingsPattern(mat, core);
    return r;
  }
  if (id.endsWith("_boots")) {
    const base = id.slice(0, -"_boots".length);
    const mat = ingredient(materialForBase(base));
    r.ingredients = bootsPattern(mat, core);
    return r;
  }
  if (id.endsWith("_sword")) {
    const base = id.slice(0, -"_sword".length);
    const mat = ingredient(materialForBase(base));
    r.ingredients = swordPattern(mat, core);
    return r;
  }
  if (id.endsWith("_pickaxe")) {
    const base = id.slice(0, -"_pickaxe".length);
    const mat = ingredient(materialForBase(base));
    r.ingredients = pickaxePattern(mat, core);
    return r;
  }
  if (id.endsWith("_axe")) {
    const base = id.slice(0, -"_axe".length);
    const mat = ingredient(materialForBase(base));
    r.ingredients = axePattern(mat, core);
    return r;
  }
  if (id.endsWith("_shovel")) {
    const base = id.slice(0, -"_shovel".length);
    const mat = ingredient(materialForBase(base));
    r.ingredients = shovelPattern(mat, core);
    return r;
  }
  if (id.endsWith("_hoe")) {
    const base = id.slice(0, -"_hoe".length);
    const mat = ingredient(materialForBase(base));
    r.ingredients = hoePattern(mat, core);
    return r;
  }

  // Special core progression
  if (id === "custom_crafter_core_t2") {
    const mat = ingredient("minecraft:iron_block");
    r.ingredients = [mat, mat, mat, mat, ingredient("political:custom_crafter_core_t1"), mat, mat, mat, mat];
    return r;
  }
  if (id === "custom_crafter_core_t3") {
    const mat = ingredient("minecraft:diamond_block");
    r.ingredients = [mat, mat, mat, mat, ingredient("political:custom_crafter_core_t2"), mat, mat, mat, mat];
    return r;
  }

  // Fallback: center core only
  r.ingredients[4] = core;
  return r;
}

const data = JSON.parse(fs.readFileSync(recipesPath, "utf8"));
const recipes = Array.isArray(data) ? data : (data.recipes || []);

const existingResultIds = new Set();
for (const recipe of recipes) {
  const id = recipe?.result?.id;
  if (typeof id === "string" && id.startsWith("political:")) {
    existingResultIds.add(id.slice("political:".length));
  }
  const nbt = recipe?.result?.nbt || "";
  const nbtId =
    (nbt.match(/custom_item_id:"([^"]+)"/) || [])[1] ||
    (nbt.match(/"custom_item_id":"([^"]+)"/) || [])[1] ||
    (nbt.match(/custom_id:"([^"]+)"/) || [])[1] ||
    (nbt.match(/"custom_id":"([^"]+)"/) || [])[1];
  if (nbtId) existingResultIds.add(nbtId);
}

const missingIds = runAuditAndGetMissing().filter((id) => !existingResultIds.has(id));
for (const id of missingIds) {
  recipes.push(recipeForId(id));
}

if (Array.isArray(data)) {
  fs.writeFileSync(recipesPath, JSON.stringify(recipes, null, 4) + "\n", "utf8");
} else {
  data.recipes = recipes;
  fs.writeFileSync(recipesPath, JSON.stringify(data, null, 4) + "\n", "utf8");
}

console.log(`Added ${missingIds.length} missing recipes.`);
