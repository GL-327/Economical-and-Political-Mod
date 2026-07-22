import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const PATH = path.join(__dirname, "..", "src", "main", "java", "com", "political", "BlockWeapons.java");

const ID_RE = /SlayerItems\.setCustomItemId\(sword,\s*"([\w_]+)_sword"\)/;

const SET_BLOCK_RE =
  /        lore\.add\(Component\.literal\("✨ SET BONUS \(Full [^"]+\):"\)\.withStyle\(ChatFormatting\.LIGHT_PURPLE,\s*ChatFormatting\.BOLD\)\);\r?\n(?:        lore\.add\(Component\.literal\([^\n]*\n)*?        lore\.add\(Component\.literal\(""\)\);\r?\n/g;

let text = fs.readFileSync(PATH, "utf8");
const parts = text.split(/(?=    public static ItemStack create\w+Sword\()/);
let patched = 0;
const out = [parts[0]];

for (const seg of parts.slice(1)) {
  const mid = ID_RE.exec(seg);
  if (!mid) {
    out.push(seg);
    continue;
  }
  const prefix = mid[1];
  const newSeg = seg.replace(SET_BLOCK_RE, () => {
    patched++;
    return (
      `        BlockGearLore.appendSwordArmorSynergy(lore, "${prefix}");\n` +
      `        BlockGearLore.appendSwordCombatPipelineNote(lore);\n`
    );
  });
  if (newSeg === seg && seg.includes("SET BONUS (Full")) {
    const head = seg.slice(0, 250).replace(/\n/g, " ");
    throw new Error(`Sword patch miss: ${head}`);
  }
  out.push(newSeg);
}

const newText = out.join("");
if (newText !== text) {
  fs.writeFileSync(PATH, newText, "utf8");
  console.log("Patched", patched, "swords");
} else console.log("No sword changes");
