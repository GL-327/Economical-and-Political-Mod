import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.join(__dirname, "..", "src", "main", "java");

const PAIRS = [
  ["ServerPlayerEntity", "ServerPlayer"],
  ["GuiElementInterface", "GuiElement"],
  ["import net.minecraft.world.inventory.slot.SlotActionType;\r\n", ""],
  ["import net.minecraft.world.inventory.slot.SlotActionType;\n", ""],
  ["SlotActionType", "net.minecraft.world.inventory.ClickType"],
  ["import net.minecraft.entity.EntityType;", "import net.minecraft.world.entity.EntityType;"],
  ["net.minecraft.entity.EntityType", "net.minecraft.world.entity.EntityType"],
  ["import net.minecraft.entity.SpawnReason;", "import net.minecraft.world.entity.SpawnReason;"],
  ["net.minecraft.entity.SpawnReason", "net.minecraft.world.entity.SpawnReason"],
  ["net.minecraft.util.math.Box", "net.minecraft.world.phys.AABB"],
  ["import net.minecraft.util.math.Box;", "import net.minecraft.world.phys.AABB;"],
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
console.error("Pass2 updated", n, "files");
