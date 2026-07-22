import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.join(__dirname, "..", "src", "main", "java");

const PAIRS = [
  ["NbtCompound", "CompoundTag"],
  ["NbtList", "ListTag"],
  ["NbtString", "StringTag"],
  ["NbtByte", "ByteTag"],
  ["NbtInt", "IntTag"],
  ["NbtLong", "LongTag"],
  ["NbtFloat", "FloatTag"],
  ["NbtDouble", "DoubleTag"],
  ["ServerWorld", "ServerLevel"],
  ["MobEntity", "Mob"],
  ["import net.minecraft.world.entity.SpawnReason;", "import net.minecraft.world.entity.MobSpawnType;"],
  ["net.minecraft.world.entity.SpawnReason", "net.minecraft.world.entity.MobSpawnType"],
  ["SpawnReason.", "MobSpawnType."],
  ["WardenEntity", "Warden"],
  ["PiglinBruteEntity", "PiglinBrute"],
  ["PiglinEntity", "Piglin"],
  ["Vec3d", "Vec3"],
  ["net.minecraft.enchantment.", "net.minecraft.world.item.enchantment."],
  ["ScreenHandler", "AbstractContainerMenu"],
  ["CraftingScreenHandler", "CraftingMenu"],
  ["EnchantmentScreenHandler", "EnchantmentMenu"],
  ["SmithingScreenHandler", "SmithingMenu"],
  ["GrindstoneScreenHandler", "GrindstoneMenu"],
  ["AnvilScreenHandler", "AnvilMenu"],
  ["BeaconScreenHandler", "BeaconMenu"],
  ["PlayerScreenHandler", "InventoryMenu"],
];

function walk(dir, out = []) {
  for (const e of fs.readdirSync(dir, { withFileTypes: true })) {
    const p = path.join(dir, e.name);
    if (e.isDirectory()) walk(p, out);
    else if (e.name.endsWith(".java") && !e.name.includes(".backup")) out.push(p);
  }
  return out;
}

let n = 0;
for (const file of walk(ROOT)) {
  let s = fs.readFileSync(file, "utf8");
  const orig = s;
  for (const [a, b] of PAIRS) s = s.split(a).join(b);
  if (s !== orig) {
    fs.writeFileSync(file, s);
    n++;
  }
}
console.error("Pass3 updated", n, "files");
