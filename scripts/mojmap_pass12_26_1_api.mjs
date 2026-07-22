/**
 * Minecraft 26.1 named JAR API moves (verified against merged jar).
 */
import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.join(__dirname, "..", "src", "main", "java");

const REPLACEMENTS = [
  // ResourceLocation renamed to Identifier (26.1)
  ["import net.minecraft.resources.ResourceLocation;", "import net.minecraft.resources.Identifier;"],
  ["net.minecraft.resources.ResourceLocation", "net.minecraft.resources.Identifier"],

  // Villager types moved under npc.villager
  ["import net.minecraft.world.entity.npc.VillagerProfession;", "import net.minecraft.world.entity.npc.villager.VillagerProfession;"],
  ["import net.minecraft.world.entity.npc.Villager;", "import net.minecraft.world.entity.npc.villager.Villager;"],
  ["net.minecraft.world.entity.npc.Villager.class", "net.minecraft.world.entity.npc.villager.Villager.class"],
  ["new net.minecraft.world.entity.npc.Villager(", "new net.minecraft.world.entity.npc.villager.Villager("],

  // Brigadier-style helpers: CommandManager -> Commands
  ["import net.minecraft.commands.CommandManager;", "import net.minecraft.commands.Commands;"],
  ["CommandManager.", "Commands."],

  ["getCommandManager()", "getCommands()"],

  // Projectiles repackaged
  ["import net.minecraft.world.entity.projectile.AbstractArrow;", "import net.minecraft.world.entity.projectile.arrow.AbstractArrow;"],
  ["net.minecraft.world.entity.projectile.AbstractArrow", "net.minecraft.world.entity.projectile.arrow.AbstractArrow"],
  ["import net.minecraft.world.entity.projectile.Arrow;", "import net.minecraft.world.entity.projectile.arrow.Arrow;"],
  ["net.minecraft.world.entity.projectile.Arrow", "net.minecraft.world.entity.projectile.arrow.Arrow"],
  ["import net.minecraft.world.entity.projectile.WindCharge;", "import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.WindCharge;"],
  ["net.minecraft.world.entity.projectile.WindCharge", "net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.WindCharge"],

  // Attributes
  ["import net.minecraft.world.entity.ai.attributes.EntityAttributeInstance;", "import net.minecraft.world.entity.ai.attributes.AttributeInstance;"],
  ["EntityAttributeInstance", "AttributeInstance"],

  // Light / raycast
  ["import net.minecraft.world.LightType;", "import net.minecraft.world.level.LightLayer;"],
  ["LightType.", "LightLayer."],
  ["import net.minecraft.world.RaycastContext;", "import net.minecraft.world.level.ClipContext;"],
  ["net.minecraft.world.RaycastContext", "net.minecraft.world.level.ClipContext"],
  ["RaycastContext", "ClipContext"],

  // Dimension key
  ["ResourceKey<World>", "ResourceKey<Level>"],

  // Spawner classes moved to levelgen
  ["import net.minecraft.world.spawner.PatrolSpawner;", "import net.minecraft.world.level.levelgen.PatrolSpawner;"],
  ["import net.minecraft.world.spawner.PhantomSpawner;", "import net.minecraft.world.level.levelgen.PhantomSpawner;"],

  ["import net.minecraft.util.collection.DefaultedList;", "import net.minecraft.core.NonNullList;"],
  ["net.minecraft.util.collection.DefaultedList", "net.minecraft.core.NonNullList"],
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
  // Remaining type name ResourceLocation -> Identifier (imports/FQCN already handled)
  s = s.replace(/\bResourceLocation\b/g, "Identifier");
  if (s !== orig) {
    fs.writeFileSync(file, s);
    n++;
  }
}
console.error("pass12_26_1 updated", n, "files");
