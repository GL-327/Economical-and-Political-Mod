/**
 * Fills ingredients + lockedSlots for recipes that only list a crafter core.
 * Mirrors RecipeIngredientHydrator + RecipeGenerator patterns; uses RecipeMaterials.java as source of truth.
 */
const fs = require("fs");
const path = require("path");

const ROOT = path.join(__dirname, "..");
const RECIPES_FILE = path.join(ROOT, "config/political/custom_recipes.json");
const MATERIALS_JAVA = path.join(ROOT, "src/main/java/com/political/RecipeMaterials.java");

function loadMaterialsMap() {
  const src = fs.readFileSync(MATERIALS_JAVA, "utf8");
  const map = {};
  const re = /materials\.put\("([^"]+)",\s*"([^"]+)"\)/g;
  let m;
  while ((m = re.exec(src))) {
    map[m[1]] = m[2];
  }
  if (!map.end_stone_brick) {
    map.end_stone_brick = "minecraft:end_stone_bricks";
  }
  return map;
}

const ARMOR_PIECE = {
  helmet: ["block", "block", "block", "block", "core", "block", "", "", ""],
  chestplate: ["block", "", "block", "block", "core", "block", "block", "block", "block"],
  leggings: ["block", "block", "block", "block", "core", "block", "block", "", "block"],
  boots: ["", "", "", "block", "core", "block", "block", "", "block"],
};

const TOOL_PIECE = {
  sword: ["", "block", "", "", "core", "", "", "stick", ""],
  pickaxe: ["block", "block", "block", "", "core", "", "", "stick", ""],
  axe: ["block", "block", "", "", "core", "", "", "stick", ""],
  shovel: ["", "", "", "", "core", "", "", "stick", ""],
  hoe: ["block", "block", "", "", "core", "", "", "stick", ""],
};

/** Slayer types that have political:{type}_chunk */
const SLAYER_CHUNK_TYPES = new Set([
  "zombie",
  "skeleton",
  "spider",
  "slime",
  "enderman",
  "piglin",
  "iron_golem",
]);

/** Mobs that use slayer chunk grid (matches RecipeIngredientHydrator slayer set + enderman, piglin, warden). */
const SLAYER_ARMOR_MOBS = new Set([
  "zombie",
  "skeleton",
  "spider",
  "slime",
  "enderman",
  "piglin",
  "iron_golem",
  "warden",
]);

const MOB_THEME_BLOCK = {
  creeper: "minecraft:tnt",
  witch: "minecraft:brewing_stand",
  blaze: "minecraft:magma_block",
  ghast: "minecraft:glowstone",
  wither_skeleton: "minecraft:wither_skeleton_skull",
  hoglin: "minecraft:crimson_stem",
  zoglin: "minecraft:warped_wart_block",
  pillager: "minecraft:loom",
  vindicator: "minecraft:iron_axe",
  evoker: "minecraft:totem_of_undying",
  ravager: "minecraft:iron_block",
  vex: "minecraft:iron_sword",
  phantom: "minecraft:phantom_membrane",
  phantom_membrane: "minecraft:phantom_membrane",
  drowned: "minecraft:prismarine",
  husk: "minecraft:sand",
  stray: "minecraft:packed_ice",
  bogged: "minecraft:moss_block",
  breeze: "minecraft:wind_charge",
  elder_guardian: "minecraft:prismarine_bricks",
  ender_dragon: "minecraft:dragon_egg",
  wither: "minecraft:nether_star",
  guardian: "minecraft:prismarine",
  sheep: "minecraft:white_wool",
  cow: "minecraft:beef",
  chicken: "minecraft:feather",
  pig: "minecraft:porkchop",
  silverfish: "minecraft:infested_stone",
  illusioner: "minecraft:blue_orchid",
  giant: "minecraft:iron_block",
  snow_golem: "minecraft:snow_block",
  iron_golem: "minecraft:iron_block",
  killer_bunny: "minecraft:rabbit_foot",
  mooshroom: "minecraft:red_mushroom_block",
};

const PREFIX_THEME = {
  ocean: "minecraft:prismarine",
  enchanted: "minecraft:enchanting_table",
  legendary: "minecraft:netherite_block",
  compacted: "minecraft:iron_block",
  reinforced: "minecraft:reinforced_deepslate",
  corrupted: "minecraft:nether_wart_block",
  blessed: "minecraft:gold_block",
  cursed: "minecraft:crying_obsidian",
  void: "minecraft:obsidian",
  shadow: "minecraft:blackstone",
  infernal: "minecraft:netherrack",
  celestial: "minecraft:end_stone",
  frost: "minecraft:blue_ice",
  storm: "minecraft:lightning_rod",
  ancient: "minecraft:ancient_debris",
  mystic: "minecraft:amethyst_block",
};

function extractCustomIdFromResult(result) {
  if (!result || typeof result !== "object") return null;
  if (result.nbt && typeof result.nbt === "string") {
    const s = result.nbt;
    let m = s.match(/custom_id:"([^"]+)"/);
    if (m) return m[1];
    m = s.match(/custom_id:\\"([^\\"]+)\\"/);
    if (m) return m[1];
    m = s.match(/custom_item_id:"([^"]+)"/);
    if (m) return m[1];
    m = s.match(/custom_item_id:\\"([^\\"]+)\\"/);
    if (m) return m[1];
  }
  return null;
}

/** Custom id from result NBT or `political:` plain id (no custom_data). */
function extractRecipeKey(result) {
  const fromNbt = extractCustomIdFromResult(result);
  if (fromNbt) return fromNbt;
  if (result && result.id && typeof result.id === "string" && result.id.startsWith("political:")) {
    return result.id.slice("political:".length);
  }
  return null;
}

const DONUT_ITEM_ALIASES = {
  brick: "minecraft:bricks",
  nether_brick: "minecraft:nether_bricks",
  rose: "minecraft:poppy",
  carrot: "minecraft:carrot",
  potato: "minecraft:potato",
  wheat: "minecraft:wheat",
  sugar_cane: "minecraft:sugar_cane",
  dandelion: "minecraft:dandelion",
};

function resolveDonutFill(base, materials) {
  if (DONUT_ITEM_ALIASES[base]) return DONUT_ITEM_ALIASES[base];
  if (materials[`${base}_block`]) return materials[`${base}_block`];
  if (materials[base]) return materials[base];
  if (base.endsWith("_brick")) {
    const id = materials[base + "s"];
    if (id) return id;
  }
  const stripped = base.replace(/_block$/, "");
  if (stripped !== base) {
    if (materials[stripped]) return materials[stripped];
    if (materials[`${stripped}_block`]) return materials[`${stripped}_block`];
  }
  return resolveBlockId(base, materials);
}

function buildDonut8(coreIng, fillMcId) {
  const fill = typeof fillMcId === "string" ? idIngredient(fillMcId) : fillMcId;
  const ingredients = [];
  const lockedSlots = [];
  for (let i = 0; i < 9; i++) {
    if (i === 4) {
      ingredients.push(coreIng);
      lockedSlots.push(false);
    } else {
      ingredients.push(fill);
      lockedSlots.push(true);
    }
  }
  return { ingredients, lockedSlots };
}

function skeletonBowGrid(tier) {
  const core = politicalCore(tier);
  return {
    ingredients: [
      "",
      idIngredient("minecraft:string"),
      "",
      idIngredient("minecraft:stick"),
      core,
      idIngredient("minecraft:string"),
      "",
      idIngredient("minecraft:stick"),
      idIngredient("minecraft:string"),
    ],
    lockedSlots: [true, true, true, true, false, true, true, true, true],
  };
}

function ingredientNonEmpty(ing) {
  if (ing === "" || ing == null) return false;
  if (typeof ing === "object") return true;
  return false;
}

function getCrafterCoreTier(ing) {
  if (!ing || typeof ing !== "object") return null;
  if (ing.id && typeof ing.id === "string") {
    const mm = ing.id.match(/custom_crafter_core_t([123])/);
    if (mm) return parseInt(mm[1], 10);
  }
  if (ing.nbt && typeof ing.nbt === "string") {
    const mm = ing.nbt.match(/custom_crafter_core_t([123])/);
    if (mm) return parseInt(mm[1], 10);
  }
  return null;
}

function isCoreOnlyRecipe(ingredients) {
  if (!Array.isArray(ingredients) || ingredients.length !== 9) return false;
  let count = 0;
  let coreTier = null;
  for (const ing of ingredients) {
    if (ingredientNonEmpty(ing)) {
      count++;
      const t = getCrafterCoreTier(ing);
      if (t != null) coreTier = t;
    }
  }
  if (count !== 1 || coreTier == null) return false;
  return true;
}

function politicalCore(tier) {
  return { id: `political:custom_crafter_core_t${tier}`, count: 1 };
}

function idIngredient(mcId, count = 1) {
  return { id: mcId, count };
}

function slayerChunkIngredient(slugForChunk) {
  return { id: `political:${slugForChunk}_chunk`, count: 1 };
}

function resolveBlockId(materialKey, materials) {
  let id = materials[materialKey];
  if (id) return id;
  if (materialKey.endsWith("_brick")) {
    id = materials[materialKey + "s"];
    if (id) return id;
  }
  for (const pref of Object.keys(PREFIX_THEME)) {
    if (materialKey.startsWith(pref + "_") || materialKey === pref) {
      return PREFIX_THEME[pref];
    }
  }
  if (MOB_THEME_BLOCK[materialKey]) return MOB_THEME_BLOCK[materialKey];
  const us = materialKey.replace(/-/g, "_");
  if (MOB_THEME_BLOCK[us]) return MOB_THEME_BLOCK[us];
  if (materials[materialKey.replace(/_ore$/, "")]) return materials[materialKey.replace(/_ore$/, "")];
  return "minecraft:cobblestone";
}

function chunkSlugForMobArmor(mob) {
  if (mob === "warden") return "iron_golem";
  if (SLAYER_CHUNK_TYPES.has(mob)) return mob;
  return null;
}

function synthesizeGrid(cid, tier, materials) {
  const core = politicalCore(tier);

  if (cid === "crystal_shove") {
    return build(
      TOOL_PIECE.shovel,
      core,
      idIngredient("minecraft:amethyst_block"),
      idIngredient("minecraft:stick")
    );
  }
  if (cid === "skeleton_bow") {
    return skeletonBowGrid(tier);
  }
  if (cid === "piglin_core") {
    return buildDonut8(core, "minecraft:gold_block");
  }
  if (cid === "crown_of_greed") {
    return build(ARMOR_PIECE.helmet, core, idIngredient("minecraft:gold_block"), null);
  }
  if (cid === "crown_of_midas") {
    return build(ARMOR_PIECE.helmet, core, idIngredient("minecraft:gilded_blackstone"), null);
  }
  if (cid === "voidwalker_crown") {
    return build(ARMOR_PIECE.helmet, core, idIngredient("minecraft:crying_obsidian"), null);
  }
  if (cid === "piglin_flesh") {
    return buildDonut8(core, "minecraft:rotten_flesh");
  }
  if (cid === "harveys_stick") {
    return buildDonut8(core, "minecraft:stick");
  }
  if (cid === "hermes_shoes") {
    return build(ARMOR_PIECE.boots, core, idIngredient("minecraft:feather"), null);
  }
  if (cid === "venomous_dagger") {
    return build(
      TOOL_PIECE.sword,
      core,
      idIngredient("minecraft:spider_eye"),
      idIngredient("minecraft:stick")
    );
  }

  if (cid.startsWith("enchanted_")) {
    const base = cid.slice("enchanted_".length);
    const fill = resolveDonutFill(base, materials);
    return buildDonut8(core, fill);
  }
  if (cid.startsWith("compacted_")) {
    const base = cid.slice("compacted_".length);
    const fill = resolveDonutFill(base, materials);
    return buildDonut8(core, fill);
  }
  if (cid.startsWith("super_compacted_")) {
    const base = cid.slice("super_compacted_".length);
    const fill = resolveDonutFill(base, materials);
    return buildDonut8(core, fill);
  }

  const slayerArmor = cid.match(
    /^(zombie|skeleton|spider|slime|enderman|piglin|iron_golem|warden)_(helmet|chestplate|leggings|boots)_t\d+$/i
  );
  if (slayerArmor) {
    const mob = slayerArmor[1].toLowerCase();
    const piece = slayerArmor[2].toLowerCase();
    const slug = chunkSlugForMobArmor(mob);
    const chunkIng = slayerChunkIngredient(slug);
    const pat = ARMOR_PIECE[piece];
    if (!pat) return null;
    return build(pat, core, chunkIng, null);
  }

  const genericArmorT = cid.match(/^([a-z0-9_]+)_(helmet|chestplate|leggings|boots)_t\d+$/i);
  if (genericArmorT && !SLAYER_ARMOR_MOBS.has(genericArmorT[1].toLowerCase())) {
    const mob = genericArmorT[1].toLowerCase();
    const piece = genericArmorT[2].toLowerCase();
    const blockId = resolveBlockId(mob, materials);
    const pat = ARMOR_PIECE[piece];
    if (!pat) return null;
    return build(pat, core, idIngredient(blockId), null);
  }

  const slayerSword = cid.match(
    /^(zombie|skeleton|spider|slime|enderman|piglin|iron_golem|warden)_sword(?:_t[12])?$/i
  );
  if (slayerSword) {
    const mob = slayerSword[1].toLowerCase();
    const slug = chunkSlugForMobArmor(mob);
    const chunkIng = slayerChunkIngredient(slug);
    const pat = ["", "slayer", "", "", "core", "", "", "stick", ""];
    return build(pat, core, chunkIng, idIngredient("minecraft:stick"));
  }

  const armorPieces = ["chestplate", "leggings", "helmet", "boots"];
  for (const piece of armorPieces) {
    const suf = "_" + piece;
    if (cid.endsWith(suf) && !/_t\d+$/i.test(cid)) {
      const mat = cid.slice(0, -suf.length);
      if (!mat) continue;
      const blockId = resolveBlockId(mat, materials);
      const pat = ARMOR_PIECE[piece];
      return build(pat, core, idIngredient(blockId), null);
    }
  }

  const tools = ["pickaxe", "shovel", "hoe", "axe", "sword"];
  for (const t of tools) {
    const suf = "_" + t;
    if (cid.endsWith(suf) && !/_t\d+$/i.test(cid)) {
      const mat = cid.slice(0, -suf.length);
      if (!mat) continue;
      const blockId = resolveBlockId(mat, materials);
      const pat = TOOL_PIECE[t];
      const stick = t === "sword" || t === "pickaxe" || t === "axe" || t === "shovel" || t === "hoe";
      return build(
        pat,
        core,
        idIngredient(blockId),
        stick ? idIngredient("minecraft:stick") : null
      );
    }
  }

  return null;
}

function build(pattern, coreIng, blockOrChunkIng, stickIng) {
  const ingredients = [];
  const lockedSlots = [];
  for (let i = 0; i < 9; i++) {
    const cell = pattern[i];
    if (cell === "core") {
      ingredients.push(coreIng);
      lockedSlots.push(false);
    } else if (cell === "block" || cell === "slayer") {
      ingredients.push(blockOrChunkIng);
      lockedSlots.push(true);
    } else if (cell === "stick") {
      ingredients.push(stickIng || idIngredient("minecraft:stick"));
      lockedSlots.push(true);
    } else {
      ingredients.push("");
      lockedSlots.push(true);
    }
  }
  return { ingredients, lockedSlots };
}

function fixGildedJson(str) {
  return str.replace(/minecraft:gilded(?!_blackstone)/g, "minecraft:gilded_blackstone");
}

function main() {
  const materials = loadMaterialsMap();
  const raw = fs.readFileSync(RECIPES_FILE, "utf8");
  const fixedRaw = fixGildedJson(raw);
  const data = JSON.parse(fixedRaw);
  const recipes = Array.isArray(data) ? data : data.recipes;
  let hydrated = 0;
  let skipped = 0;

  for (const r of recipes) {
    if (!r || !Array.isArray(r.ingredients)) continue;
    if (!isCoreOnlyRecipe(r.ingredients)) continue;
    const tier = getCrafterCoreTier(r.ingredients.find(ingredientNonEmpty));
    const cid = extractRecipeKey(r.result);
    if (!cid) {
      skipped++;
      continue;
    }
    const syn = synthesizeGrid(cid, tier, materials);
    if (!syn) {
      skipped++;
      continue;
    }
    r.ingredients = syn.ingredients;
    r.lockedSlots = syn.lockedSlots;
    hydrated++;
  }

  const out = Array.isArray(data) ? data : { ...data, recipes };
  fs.writeFileSync(RECIPES_FILE, JSON.stringify(out, null, 2) + "\n", "utf8");

  console.log("recipeCount:", recipes.length);
  console.log("coreOnlyHydrated:", hydrated);
  console.log("coreOnlySkipped:", skipped);
  console.log("materialKeys:", Object.keys(materials).length);
  console.log("wrote:", RECIPES_FILE);
}

main();
