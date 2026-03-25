/**
 * 1) Normalizes all tool recipes ( *_pickaxe | *_axe | *_shovel | *_hoe ) to vanilla-like shapes
 *    with political custom crafter core ALWAYS in slot index 4 (center).
 * 2) Normalizes core + stick to { id, count } form.
 * 3) Sorts recipes: by max core tier (T1 < T2 < T3 < no core), then by name.
 */
const fs = require("fs");
const path = require("path");

const file = path.join(__dirname, "..", "config", "political", "custom_recipes.json");
const data = JSON.parse(fs.readFileSync(file, "utf8"));
const recipes = data.recipes;

const PREFIX_MATERIAL_FALLBACK = {
  dragon: "minecraft:cobblestone",
  ocean: "minecraft:prismarine",
  aether: "minecraft:feather",
  void: "minecraft:obsidian",
  nature: "minecraft:oak_sapling",
  frost: "minecraft:packed_ice",
  thunder: "minecraft:lightning_rod",
  lunar: "minecraft:end_stone",
  solar: "minecraft:glowstone",
  terra: "minecraft:grass_block",
  phantom: "minecraft:phantom_membrane",
  blood: "minecraft:redstone_block",
  celestial: "minecraft:amethyst_shard",
  shadow: "minecraft:blackstone",
  crystal: "minecraft:prismarine_crystals",
  obsidian_block: "minecraft:obsidian_block",
  emerald: "minecraft:emerald",
  lapis: "minecraft:lapis_lazuli",
  copper: "minecraft:copper_ingot",
  iron: "minecraft:iron_ingot",
  gold: "minecraft:gold_ingot",
  diamond: "minecraft:diamond",
  netherite: "minecraft:netherite_ingot",
};

function extractCustomItemId(result) {
  if (!result) return null;
  if (result.id && String(result.id).startsWith("political:")) {
    return String(result.id).slice("political:".length);
  }
  if (result.nbt && typeof result.nbt === "string") {
    const s = result.nbt;
    let m = s.match(/custom_item_id:"([^"]+)"/);
    if (m) return m[1];
    m = s.match(/custom_id:"([^"]+)"/);
    if (m) return m[1];
  }
  return null;
}

function getIngredientId(ing) {
  if (!ing || ing === "") return null;
  if (typeof ing === "string") return null;
  if (ing.id) return ing.id;
  if (ing.nbt && typeof ing.nbt === "string") {
    const s = ing.nbt;
    let m = s.match(/id:"([^"]+)"/);
    if (m) return m[1];
    m = s.match(/id:\\"([^"]+)\\"/);
    if (m) return m[1];
  }
  return null;
}

function extractCoreTier(ingredients) {
  for (const ing of ingredients) {
    const id = getIngredientId(ing);
    if (!id) continue;
    const m = id.match(/custom_crafter_core_t([123])/);
    if (m) return parseInt(m[1], 10);
  }
  return 3;
}

function extractMaterialId(ingredients) {
  for (const ing of ingredients) {
    const id = getIngredientId(ing);
    if (!id) continue;
    if (id.includes("custom_crafter_core")) continue;
    if (id === "minecraft:stick") continue;
    if (id === "minecraft:wheat_seeds") continue;
    if (id === "minecraft:blaze_powder") continue;
    return id;
  }
  return null;
}

function toolKindFromId(customId) {
  if (customId.endsWith("_pickaxe")) return "pickaxe";
  if (customId.endsWith("_shovel")) return "shovel";
  if (customId.endsWith("_hoe")) return "hoe";
  if (customId.endsWith("_axe")) return "axe";
  return null;
}

function prefixFromToolId(customId) {
  return customId
    .replace(/_pickaxe$/, "")
    .replace(/_shovel$/, "")
    .replace(/_hoe$/, "")
    .replace(/_axe$/, "");
}

function coreStack(tier) {
  return { id: `political:custom_crafter_core_t${tier}`, count: 1 };
}

function stick() {
  return { id: "minecraft:stick", count: 1 };
}

function buildIngredients(kind, materialId, tier) {
  const c = coreStack(tier);
  const s = stick();
  const M = { id: materialId, count: 1 };
  const E = "";
  if (kind === "pickaxe") {
    return [M, M, M, E, c, E, E, s, E];
  }
  if (kind === "axe") {
    return [M, M, E, M, c, E, E, s, E];
  }
  if (kind === "shovel") {
    return [E, M, E, E, c, E, E, s, E];
  }
  if (kind === "hoe") {
    return [M, M, E, E, c, E, E, s, E];
  }
  return null;
}

const allTrue = [true, true, true, true, true, true, true, true, true];

let toolsFixed = 0;
for (const r of recipes) {
  const cid = extractCustomItemId(r.result);
  if (!cid) continue;
  const kind = toolKindFromId(cid);
  if (!kind) continue;

  const tier = extractCoreTier(r.ingredients || []);
  let mat = extractMaterialId(r.ingredients || []);
  if (!mat) {
    const prefix = prefixFromToolId(cid);
    mat = PREFIX_MATERIAL_FALLBACK[prefix] || "minecraft:cobblestone";
  }

  const next = buildIngredients(kind, mat, tier);
  if (!next) continue;
  r.ingredients = next;
  r.lockedSlots = allTrue.slice();
  toolsFixed++;
}

function maxCoreTierInRecipe(r) {
  const ing = r.ingredients;
  if (!Array.isArray(ing)) return 0;
  let max = 0;
  for (const slot of ing) {
    const id = getIngredientId(slot);
    if (!id) continue;
    const m = id.match(/custom_crafter_core_t([123])/);
    if (m) max = Math.max(max, parseInt(m[1], 10));
  }
  return max;
}

recipes.sort((a, b) => {
  const ta = maxCoreTierInRecipe(a);
  const tb = maxCoreTierInRecipe(b);
  if (ta !== tb) return ta - tb;
  const na = (a && a.name) ? String(a.name) : "";
  const nb = (b && b.name) ? String(b.name) : "";
  return na.localeCompare(nb);
});

data.recipes = recipes;
fs.writeFileSync(file, JSON.stringify(data, null, 2) + "\n", "utf8");
console.log("Tool recipes normalized:", toolsFixed);
console.log("Recipes sorted by max core tier (1→3), then name. Total:", recipes.length);
