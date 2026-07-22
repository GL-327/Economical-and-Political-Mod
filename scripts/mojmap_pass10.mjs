import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.join(__dirname, "..", "src", "main", "java");

const REPLACEMENTS = [
  ["ActionResult.PASS", "InteractionResult.PASS"],
  ["ActionResult.SUCCESS", "InteractionResult.SUCCESS"],
  ["ActionResult.FAIL", "InteractionResult.FAIL"],
  ["ActionResult.CONSUME", "InteractionResult.CONSUME"],
  ["ActionResult.PARTIAL", "InteractionResult.PARTIAL"],
  ["ActionResult.ATTACK", "InteractionResult.SUCCESS"],
  ["import net.minecraft.util.ActionResult;", "import net.minecraft.world.InteractionResult;"],
  ["import net.minecraft.world.item.ActionResult;", "import net.minecraft.world.InteractionResult;"],

  ["import net.minecraft.nbt.StringNbtReader;", "import net.minecraft.nbt.TagParser;"],
  ["StringNbtReader.readCompound(", "TagParser.parseCompoundFully("],
  ["net.minecraft.nbt.StringNbtReader.readCompound(", "net.minecraft.nbt.TagParser.parseCompoundFully("],

  ["import net.minecraft.world.entity.npc.Villager;", "import net.minecraft.world.entity.animal.Villager;"],
  ["net.minecraft.world.entity.npc.Villager.class", "net.minecraft.world.entity.animal.Villager.class"],
  ["new net.minecraft.world.entity.npc.Villager(", "new net.minecraft.world.entity.animal.Villager("],

  ["import net.minecraft.world.inventory.SimpleMenuProvider;", "import net.minecraft.world.SimpleMenuProvider;"],
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
  s = s.replace(/\bActionResult\b/g, "InteractionResult");
  if (s !== orig) {
    fs.writeFileSync(file, s);
    n++;
  }
}
console.error("pass10 updated", n, "files");
