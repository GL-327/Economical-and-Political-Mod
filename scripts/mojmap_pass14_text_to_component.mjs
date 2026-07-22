/**
 * Remaining Yarn `Text` type → Mojmap `Component` (avoid touching words like Texture).
 */
import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.join(__dirname, "..", "src", "main", "java");

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
  s = s.split("Text.of(").join("Component.literal(");
  s = s.split("List<Text>").join("List<Component>");
  s = s.split("Text...").join("Component...");
  s = s.replace(/\bText line\b/g, "Component line");
  s = s.replace(/\bText message\b/g, "Component message");
  s = s.replace(/\bText name\b/g, "Component name");
  s = s.replace(/\bText nm\b/g, "Component nm");
  s = s.replace(/\bText cn\b/g, "Component cn");
  s = s.replace(/\bText disp\b/g, "Component disp");
  s = s.replace(/\bText itemName\b/g, "Component itemName");
  s = s.replace(/\bText customName\b/g, "Component customName");
  s = s.replace(/\bText nameText\b/g, "Component nameText");
  s = s.replace(/\bText displayName\b/g, "Component displayName");
  s = s.replace(/\bText setName\b/g, "Component setName");
  s = s.replace(/\bText separator\b/g, "Component separator");
  s = s.replace(/\bText inputName\b/g, "Component inputName");
  s = s.replace(/\bText recipeName\b/g, "Component recipeName");
  s = s.replace(/\bText outputName\b/g, "Component outputName");
  s = s.replace(/\bText header\b/g, "Component header");
  s = s.replace(/\bText footer\b/g, "Component footer");
  s = s.replace(/\bText announcement\b/g, "Component announcement");
  s = s.replace(/\bText resultName\b/g, "Component resultName");
  s = s.replace(/\bText ingName\b/g, "Component ingName");
  s = s.replace(/\bText nameTxt\b/g, "Component nameTxt");
  s = s.replace(/\bprivate static Text\b/g, "private static Component");
  s = s.replace(/\bText \[/g, "Component [");
  if (s !== orig) {
    fs.writeFileSync(file, s);
    n++;
  }
}
console.error("pass14 text->component touched", n, "files");
