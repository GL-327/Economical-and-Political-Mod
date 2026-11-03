import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const PATH = path.join(__dirname, "..", "src", "main", "java", "com", "political", "BlockTools.java");

let text = fs.readFileSync(PATH, "utf8");
const ID_RE = /SlayerItems\.setCustomItemId\(tool,\s*"([\w_]+)_(pickaxe|axe|shovel|hoe)"\)/;

const parts = text.split(/(?=    public static ItemStack create\w+)/);
let patched = 0;
const out = [];

for (const part of parts) {
  if (!part.includes("public static ItemStack create")) {
    out.push(part);
    continue;
  }
  if (part.includes("createBlockTool(") || part.includes("private static ItemStack createBlockTool")) {
    out.push(part);
    continue;
  }
  if (part.includes("appendToolHandlerSummary")) {
    out.push(part);
    continue;
  }
  const mid = ID_RE.exec(part);
  if (!mid) {
    out.push(part);
    continue;
  }
  const material = mid[1];
  const kind = mid[2];
  const newPart = part.replace(
    /(\n        tool\.set\(DataComponents\.LORE, new ItemLore\(lore\)\);)/,
    `\n        BlockGearLore.appendToolHandlerSummary(lore, "${material}", "${kind}");$1`
  );
  if (newPart === part) {
    const head = part.slice(0, 120).replace(/\n/g, " ");
    throw new Error(`Could not insert tool lore helper: ${head}`);
  }
  patched++;
  out.push(newPart);
}

const newText = out.join("");
if (newText !== text) {
  fs.writeFileSync(PATH, newText, "utf8");
  console.log("Patched manual tools:", patched);
} else console.log("No manual tool changes");
