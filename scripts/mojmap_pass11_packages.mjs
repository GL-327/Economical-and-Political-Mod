import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.join(__dirname, "..", "src", "main", "java");

const REPLACEMENTS = [
  // Villager: 26.1 Mojmap stays under npc (pass10 animal revert)
  ["import net.minecraft.world.entity.animal.Villager;", "import net.minecraft.world.entity.npc.Villager;"],
  ["net.minecraft.world.entity.animal.Villager.class", "net.minecraft.world.entity.npc.Villager.class"],
  ["new net.minecraft.world.entity.animal.Villager(", "new net.minecraft.world.entity.npc.Villager("],

  ["import net.minecraft.util.hit.", "import net.minecraft.world.phys."],
  ["net.minecraft.util.hit.", "net.minecraft.world.phys."],

  ["import net.minecraft.util.collection.DefaultedList;", "import net.minecraft.core.NonNullList;"],
  ["net.minecraft.util.collection.DefaultedList", "net.minecraft.core.NonNullList"],

  ["import net.minecraft.world.entity.boss.dragon.EnderDragonEntity;", "import net.minecraft.world.entity.boss.enderdragon.EnderDragon;"],
  ["EnderDragonEntity", "EnderDragon"],

  ["import net.minecraft.entity.ExperienceOrbEntity;", "import net.minecraft.world.entity.ExperienceOrb;"],
  ["ExperienceOrbEntity", "ExperienceOrb"],

  ["import net.minecraft.util.math.random.Random;", "import net.minecraft.util.RandomSource;"],
  ["net.minecraft.util.math.random.Random", "net.minecraft.util.RandomSource"],

  ["import net.minecraft.entity.EntityCollisionHandler;", "import net.minecraft.world.entity.EntityCollisionHandler;"],
  ["import net.minecraft.entity.", "import net.minecraft.world.entity."],
  ["net.minecraft.entity.ExperienceOrbEntity", "net.minecraft.world.entity.ExperienceOrb"],

  ["import net.minecraft.text.", "import net.minecraft.network.chat."],
  ["net.minecraft.text.", "net.minecraft.network.chat."],

  ["import net.minecraft.loot.context.", "import net.minecraft.world.level.storage.loot.parameters."],
  ["import net.minecraft.loot.", "import net.minecraft.world.level.storage.loot."],
  ["net.minecraft.loot.context.", "net.minecraft.world.level.storage.loot.parameters."],
  ["net.minecraft.loot.", "net.minecraft.world.level.storage.loot."],

  ["import net.minecraft.fluid.FluidState;", "import net.minecraft.world.level.material.FluidState;"],
  ["import net.minecraft.fluid.", "import net.minecraft.world.level.material."],
  ["net.minecraft.fluid.", "net.minecraft.world.level.material."],

  ["import net.minecraft.entity.effect.", "import net.minecraft.world.effect."],
  ["net.minecraft.entity.effect.", "net.minecraft.world.effect."],

  ["import net.minecraft.world.entity.boss.WitherEntity;", "import net.minecraft.world.entity.boss.wither.Wither;"],
  ["WitherEntity", "Wither"],
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
  if (s !== orig) {
    fs.writeFileSync(file, s);
    n++;
  }
}
console.error("pass11 packages updated", n, "files");
