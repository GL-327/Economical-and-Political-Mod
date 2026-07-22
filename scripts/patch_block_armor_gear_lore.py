# -*- coding: utf-8 -*-
"""Replace BlockArmor SET BONUS lore blocks with BlockGearLore.appendArmorSetBonus calls."""
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PATH = ROOT / "src" / "main" / "java" / "com" / "political" / "BlockArmor.java"

PREFIX_RE = re.compile(
    r'Identifier\.fromNamespaceAndPath\("politicalserver", "([\w_]+)_" \+ pieceName\.toLowerCase\(\) \+ "_armor"\)'
)

# SET BONUS header through the blank lore line before the footer (§8 or styled footer)
SET_BLOCK_RE = re.compile(
    r'        lore\.add\(Component\.literal\("━━━ SET BONUS ━━━"\)\.withStyle\(ChatFormatting\.(\w+)\)\);\n'
    r'(?:        lore\.add\(Component\.literal\([^\n]*\n)*?'
    r'        lore\.add\(Component\.literal\(""\)\);\n',
    re.MULTILINE,
)


def main():
    text = PATH.read_text(encoding="utf-8")
    parts = re.split(r"(?=    private static void apply\w+Stats\()", text)
    out = [parts[0]]
    patched = 0

    for seg in parts[1:]:
        if not seg.strip():
            continue
        mpre = PREFIX_RE.search(seg)
        if not mpre:
            out.append(seg)
            continue
        prefix = mpre.group(1)

        def repl(match):
            nonlocal patched
            hdr = match.group(1)
            patched += 1
            return (
                f'        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.{hdr}, "{prefix}");\n'
                f'        lore.add(Component.literal(""));\n'
            )

        new_seg, n = SET_BLOCK_RE.subn(repl, seg, count=1)
        if n == 0 and "SET BONUS" in seg:
            head = seg[:200].replace("\n", " ")
            raise SystemExit(f"No SET BONUS block replaced in segment starting: {head!r}")
        out.append(new_seg)

    new_text = "".join(out)
    if new_text == text:
        print("No changes")
        return
    PATH.write_text(new_text, encoding="utf-8")
    print("Patched", patched, "apply*Stats segments in", PATH.name)


if __name__ == "__main__":
    main()
