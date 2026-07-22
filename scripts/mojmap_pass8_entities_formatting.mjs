import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.join(__dirname, "..", "src", "main", "java");

const REPLACEMENTS = [
  // Yarn-style *Entity -> Mojmap (common)
  ["net.minecraft.world.entity.animal.VillagerEntity", "net.minecraft.world.entity.npc.Villager"],
  ["VillagerEntity", "Villager"],
  ["net.minecraft.world.entity.animal.IronGolemEntity", "net.minecraft.world.entity.animal.IronGolem"],
  ["IronGolemEntity", "IronGolem"],
  ["net.minecraft.world.entity.animal.TameableEntity", "net.minecraft.world.entity.TamableAnimal"],
  ["TameableEntity", "TamableAnimal"],
  ["net.minecraft.world.entity.monster.SilverfishEntity", "net.minecraft.world.entity.monster.Silverfish"],
  ["SilverfishEntity", "Silverfish"],
  ["net.minecraft.world.entity.projectile.FishingBobberEntity", "net.minecraft.world.entity.projectile.FishingHook"],
  ["FishingBobberEntity", "FishingHook"],
  ["net.minecraft.world.entity.projectile.ArrowEntity", "net.minecraft.world.entity.projectile.Arrow"],
  ["ArrowEntity", "Arrow"],
  ["net.minecraft.world.entity.projectile.WindChargeEntity", "net.minecraft.world.entity.projectile.WindCharge"],
  ["WindChargeEntity", "WindCharge"],
  ["net.minecraft.world.entity.monster.EndermanEntity", "net.minecraft.world.entity.monster.Enderman"],
  ["EndermanEntity", "Enderman"],
  ["net.minecraft.world.entity.monster.ZombieVillagerEntity", "net.minecraft.world.entity.monster.ZombieVillager"],
  ["ZombieVillagerEntity", "ZombieVillager"],
  ["net.minecraft.world.entity.projectile.PersistentProjectileEntity", "net.minecraft.world.entity.projectile.AbstractArrow"],
  ["PersistentProjectileEntity", "AbstractArrow"],

  ["getEntitiesByClass(", "getEntitiesOfClass("],

  // Formatting (Yarn) -> ChatFormatting; avoid double ChatChatFormatting
];

function walk(dir, out = []) {
  for (const e of fs.readdirSync(dir, { withFileTypes: true })) {
    const p = path.join(dir, e.name);
    if (e.isDirectory()) walk(p, out);
    else if (e.name.endsWith(".java")) out.push(p);
  }
  return out;
}

let n = 0;
for (const file of walk(ROOT)) {
  let s = fs.readFileSync(file, "utf8");
  const orig = s;
  for (const [a, b] of REPLACEMENTS) s = s.split(a).join(b);
  s = s.replace(/(?<!Chat)Formatting\b/g, "ChatFormatting");
  if (s !== orig) {
    fs.writeFileSync(file, s);
    n++;
  }
}
console.error("pass8 updated", n, "files");
