import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.join(__dirname, "..", "src", "main", "java");

const SKIP = new Set(["WorldBossManager.java", "WorldBossGui.java", "WorldEventManager.java"]);

function walk(dir, out = []) {
  for (const e of fs.readdirSync(dir, { withFileTypes: true })) {
    const p = path.join(dir, e.name);
    if (e.isDirectory()) walk(p, out);
    else if (e.name.endsWith(".java") && !e.name.includes(".backup") && !SKIP.has(e.name)) out.push(p);
  }
  return out;
}

let n = 0;
for (const file of walk(ROOT)) {
  let s = fs.readFileSync(file, "utf8");
  const orig = s;
  s = s.split("(World ").join("(Level ");
  s = s.split(", World ").join(", Level ");
  if (s !== orig) {
    fs.writeFileSync(file, s);
    n++;
  }
}
console.error("Pass6 updated", n, "files");
