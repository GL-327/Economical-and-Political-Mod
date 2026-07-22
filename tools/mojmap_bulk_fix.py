#!/usr/bin/env python3
"""Targeted Mojmap 26.x + sgui bulk fixes (safe patterns only)."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "src"


def fix_file(path: Path) -> bool:
    text = path.read_text(encoding="utf-8")
    orig = text

    text = text.replace("net.minecraft.ChatChatFormatting", "net.minecraft.network.chat.ChatFormatting")
    text = text.replace("import net.minecraft.ChatFormatting;", "import net.minecraft.network.chat.ChatFormatting;")

    # ItemStack display name (local names seen in this codebase)
    for name in (
        "stack",
        "item",
        "displayStack",
        "cursor",
        "result",
        "left",
        "right",
        "out",
        "iconStack",
        "ghost",
        "listingStack",
        "auctionStack",
        "bankStack",
        "gridStack",
        "slotStack",
        "handStack",
        "armorStack",
        "reward",
        "drop",
        "copyStack",
        "newStack",
        "oldStack",
        "tmpStack",
        "pageStack",
        "cellStack",
        "rowItem",
        "mat",
        "req",
        "picked",
        "chosen",
        "toSell",
        "toBuy",
        "offerStack",
        "tradeStack",
        "orderStack",
        "book",
        "shardStack",
        "tokenStack",
        "gearStack",
        "setStack",
        "filterStack",
        "searchStack",
        "viewStack",
        "editStack",
        "saveStack",
        "clearStack",
        "priceStack",
        "bidStack",
        "askStack",
        "coinStack",
        "cryptoStack",
        "giftStack",
        "useStack",
        "targetStack",
        "selfStack",
        "helm",
        "chest",
        "legs",
        "boots",
        "feet",
        "head",
        "piece",
        "parts",
        "kit",
        "box",
        "bag",
        "pouch",
        "scrollStack",
        "paperStack",
        "noteStack",
        "mailStack",
        "wrapStack",
        "hotbar",
        "invStack",
        "backStack",
        "forwardStack",
        "listing",
        "offer",
        "order",
        "trade",
        "bid",
        "ask",
        "loot",
        "grab",
        "put",
        "move",
        "swap",
        "src",
        "dst",
        "have",
        "need",
        "got",
        "miss",
        "one",
        "other",
        "entryStack",
        "valueStack",
        "pageItem",
        "gridItem",
    ):
        text = re.sub(rf"\b{re.escape(name)}\.getName\(\)", f"{name}.getHoverName()", text)

    # sgui 4-arg ClickCallback
    text = re.sub(
        r"\.setCallback\(\(([^,()]+),\s*([^,()]+),\s*([^,()]+)\)\s*->",
        r".setCallback((\1, \2, \3, __slotGui) ->",
        text,
    )

    text = re.sub(r"\.getInventory\(\)\.size\(\)", ".getInventory().getContainerSize()", text)
    text = re.sub(r"getRegistryManager\(\)", "registryAccess()", text)
    text = re.sub(r"\bNbtElement\b", "net.minecraft.nbt.Tag", text)
    text = re.sub(r"getStackInHand\(", "getItemInHand(", text)
    text = re.sub(r"\.isClient\(\)", ".isClientSide()", text)
    text = re.sub(r"\.addCommandTag\(", ".addTag(", text)
    text = re.sub(r"\.getCommandTags\(\)", ".getTags()", text)
    text = re.sub(r"\.clearMobEffects\(\)", ".removeAllEffects()", text)
    text = re.sub(r"\.setAiDisabled\(([^)]+)\)", r".setNoAi(\1)", text)
    text = re.sub(r"\.getDamageSources\(\)", ".damageSources()", text)
    text = re.sub(r"\.spawnParticles\(", ".sendParticles(", text)
    text = re.sub(r"\.getDefaultState\(\)", ".defaultBlockState()", text)
    text = re.sub(r"\.isOnGround\(\)", ".onGround()", text)
    text = re.sub(r"\.isSneaking\(\)", ".isShiftKeyDown()", text)
    text = re.sub(r"\.isTouchingWater\(\)", ".isInWater()", text)
    text = re.sub(r"\.isSubmergedInWater\(\)", ".isUnderWater()", text)
    text = re.sub(r"\.extinguish\(\)", ".clearFire()", text)
    text = re.sub(r"\.takeKnockback\(", ".knockback(", text)
    text = re.sub(r"\.requestTeleport\(", ".teleportTo(", text)
    text = re.sub(r"\.hasEnchantments\(\)", ".isEnchanted()", text)

    if text == orig:
        return False
    path.write_text(text, encoding="utf-8", newline="\n")
    return True


def main() -> None:
    n = 0
    for path in sorted(ROOT.rglob("*.java")):
        if fix_file(path):
            n += 1
    print(f"updated {n} files")


if __name__ == "__main__":
    main()
