import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const PATH = path.join(__dirname, "..", "src", "main", "java", "com", "political", "BlockArmor.java");

const PREFIX_RE =
  /Identifier\.fromNamespaceAndPath\("politicalserver", "([\w_]+)_" \+ pieceName\.toLowerCase\(\) \+ "_armor"\)/;

const SET_BLOCK_RE =
  /        lore\.add\(Component\.literal\("━━━ SET BONUS ━━━"\)\.withStyle\(ChatFormatting\.(\w+)\)\);\r?\n(?:        lore\.add\(Component\.literal\([^\n]*\n)*?        lore\.add\(Component\.literal\(""\)\);\r?\n/g;

let text = fs.readFileSync(PATH, "utf8");
const parts = text.split(/(?=    private static void apply\w+Stats\()/);
let patched = 0;
const out = [parts[0]];

for (const seg of parts.slice(1)) {
  const mpre = PREFIX_RE.exec(seg);
  if (!mpre) {
    out.push(seg);
    continue;
  }
  const prefix = mpre[1];
  const newSeg = seg.replace(SET_BLOCK_RE, (full, hdr) => {
    patched++;
    return `        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.${hdr}, "${prefix}");\n        lore.add(Component.literal(""));\n`;
  });
  if (newSeg === seg && seg.includes("SET BONUS")) {
    const head = seg.slice(0, 200).replace(/\n/g, " ");
    throw new Error(`Failed to patch segment: ${head}`);
  }
  out.push(newSeg);
}

const newText = out.join("");
if (newText === text) {
  console.log("No changes");
} else {
  fs.writeFileSync(PATH, newText, "utf8");
  console.log("Patched", patched, "segments");
}
