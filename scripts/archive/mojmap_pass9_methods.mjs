import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.join(__dirname, "..", "src", "main", "java");

const REPLACEMENTS = [
  [".getWorlds()", ".getAllLevels()"],
  [".fishHook", ".fishing"],
  [".refreshPositionAndAngles(", ".moveTo("],
  [".giveItemStack(", ".addItem("],
  [".isIn(FluidTags.", ".is(FluidTags."],
  [".getYaw()", ".getYRot()"],
  [".getPitch()", ".getXRot()"],
  [".setYaw(", ".setYRot("],
  [".setPitch(", ".setXRot("],
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
console.error("pass9 updated", n, "files");
