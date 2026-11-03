/**
 * 1) Assigns one custom_crafter_core tier per armor/tool set (T2 default; T1 weak; T3 high).
 * 2) Dragon chestplate recipes use dragon_crafter_core_* (tier from dragon tier map).
 * 3) Appends shaped recipes for the 15 extended dragon chestplates if missing.
 */
const fs = require("fs");
const path = require("path");

const recipesPath = path.join(__dirname, "..", "config", "political", "custom_recipes.json");

const SUFFIX_RE = /_helmet$|_chestplate$|_leggings$|_boots$|_sword$|_pickaxe$|_axe$|_shovel$|_hoe$|_bow$|_crossbow$/;

function setKeyFromRecipe(recipe) {
  const res = recipe.result;
  if (!res) return null;
  if (typeof res.id === "string" && res.id.startsWith("political:")) {
    return res.id.slice("political:".length).replace(SUFFIX_RE, "");
  }
  if (typeof res.nbt === "string") {
    const m = res.nbt.match(/custom_item_id:"([^"]+)"/);
    if (!m) return null;
    return m[1].replace(SUFFIX_RE, "");
  }
  return null;
}

function tierForSet(key) {
  if (!key) return 2;
  const k = key.toLowerCase();
  if (
    /(^|_)inferno|^storm_|^void_|eclipse|obsidian_wyrm|starfall|tempest|crystal_dragon|blood_dragon|venom_dragon|ancient_debris|netherite|obsidian_block|reinforced|warden|sculk|crying_obsidian|chiseled_deepslate|end_stone|purpur|prismarine_bricks|dark_prismarine|lodestone|amethyst_cluster|nether_gold|gilded_blackstone/.test(
      k
    )
  ) {
    return 3;
  }
  if (
    k.startsWith("glass") ||
    k.startsWith("cactus") ||
    k.startsWith("snow") ||
    k.startsWith("moss") ||
    k.startsWith("kelp") ||
    k.startsWith("vine") ||
    k.startsWith("paper") ||
    k.startsWith("wool") ||
    k.startsWith("string") ||
    k.startsWith("feather") ||
    k.startsWith("honey") ||
    k.includes("sponge")
  ) {
    return 1;
  }
  return 2;
}

function dragonCenterTier(resultId) {
  if (!resultId) return 2;
  const id = resultId.replace(/^political:/, "");
  if (
    /inferno_dragon|storm_dragon|void_dragon|eclipse_dragon|obsidian_wyrm|starfall_dragon|tempest_dragon|crystal_dragon|blood_dragon|venom_dragon/.test(
      id
    )
  ) {
    return 3;
  }
  return 2;
}

function shapedDragon(name, resultId, blockId, tier) {
  return {
    name,
    requiredSlayer: "",
    requiredLevel: 0,
    result: { id: resultId, count: 1 },
    ingredients: [
      { id: blockId, count: 1 },
      "",
      { id: blockId, count: 1 },
      { id: blockId, count: 1 },
      { id: `political:dragon_crafter_core_t${tier}`, count: 1 },
      { id: blockId, count: 1 },
      { id: blockId, count: 1 },
      { id: blockId, count: 1 },
      { id: blockId, count: 1 },
    ],
    lockedSlots: [true, true, true, true, true, true, true, true, true],
  };
}

const raw = fs.readFileSync(recipesPath, "utf8");
const data = JSON.parse(raw);
const recipes = Array.isArray(data) ? data : data.recipes;

const existingIds = new Set();
for (const r of recipes) {
  const id = r.result && r.result.id;
  if (id) existingIds.add(id);
}

for (const r of recipes) {
  const setKey = setKeyFromRecipe(r);
  const tier = tierForSet(setKey);
  const rid = r.result && r.result.id;
  const isDragon = typeof rid === "string" && rid.includes("dragon_chestplate");
  const dTier = isDragon ? dragonCenterTier(rid) : tier;

  if (!Array.isArray(r.ingredients)) continue;
  for (const ing of r.ingredients) {
    if (!ing || typeof ing !== "object" || !ing.id) continue;
    if (ing.id.startsWith("political:custom_crafter_core_")) {
      ing.id = isDragon
        ? `political:dragon_crafter_core_t${dTier}`
        : `political:custom_crafter_core_t${tier}`;
    }
    if (isDragon && ing.id.startsWith("political:dragon_crafter_core_")) {
      ing.id = `political:dragon_crafter_core_t${dTier}`;
    }
  }
}

const NEW_DRAGONS = [
  ["Dune Wyrm Chestplate", "political:dune_dragon_chestplate", "minecraft:sand", 2],
  ["Reef Leviathan Chestplate", "political:reef_dragon_chestplate", "minecraft:prismarine", 2],
  ["Ember Drakeling Chestplate", "political:ember_dragon_chestplate", "minecraft:magma_block", 2],
  ["Crystal Drake Chestplate", "political:crystal_dragon_chestplate", "minecraft:amethyst_block", 3],
  ["Shadow Wyrm Chestplate", "political:shadow_dragon_chestplate", "minecraft:blackstone", 2],
  ["Solar Serpent Chestplate", "political:solar_dragon_chestplate", "minecraft:glowstone", 2],
  ["Lunar Serpent Chestplate", "political:lunar_dragon_chestplate", "minecraft:lapis_block", 2],
  ["Terra Colossus Chestplate", "political:terra_dragon_chestplate", "minecraft:moss_block", 2],
  ["Tempest Lord Chestplate", "political:tempest_dragon_chestplate", "minecraft:weathered_copper", 3],
  ["Auric Drake Chestplate", "political:auric_dragon_chestplate", "minecraft:gold_block", 2],
  ["Glacier Titan Chestplate", "political:glacier_dragon_chestplate", "minecraft:packed_ice", 2],
  ["Mire Horror Chestplate", "political:mire_dragon_chestplate", "minecraft:mud", 2],
  ["Starfall Chimera Chestplate", "political:starfall_dragon_chestplate", "minecraft:end_rod", 3],
  ["Mirage Dancer Chestplate", "political:mirage_dragon_chestplate", "minecraft:terracotta", 2],
  ["Obsidian Wyrm Chestplate", "political:obsidian_wyrm_chestplate", "minecraft:crying_obsidian", 3],
];

let added = 0;
for (const [name, resultId, block, t] of NEW_DRAGONS) {
  if (existingIds.has(resultId)) continue;
  recipes.push(shapedDragon(name, resultId, block, t));
  existingIds.add(resultId);
  added++;
}

const out = Array.isArray(data) ? recipes : { ...data, recipes };
fs.writeFileSync(recipesPath, JSON.stringify(out, null, 2), "utf8");
console.log(
  JSON.stringify(
    {
      totalRecipes: recipes.length,
      appendedDragonRecipes: added,
    },
    null,
    2
  )
);
