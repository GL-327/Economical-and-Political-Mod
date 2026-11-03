# Project Status — Economical & Political Mod

**Last updated: 2026-07-21** (post-completion pass)
**Target:** Minecraft 26.1 · Fabric Loader 0.18.5 · Fabric API 0.144.3 · Java 25 · server-side only

> ⚠️ Ignore `BACKROOMS_IMPLEMENTATION_STATUS.md` — it describes classes
> (BackroomsItemsManager, AdvancedBackroomsGenerator, etc.) that **do not exist** in this
> codebase. It appears to be an AI-generated aspirational report, not a status document.
> `ABILITY_CATALOG*.md` predate the new unique set-bonus registry and are partially stale.

## Build & run

```bash
./gradlew build            # produces build/libs/politicalserver-1.0.0.jar
./gradlew runServer        # dev server on :25565 (run/)
./gradlew runServerVerify  # throwaway verify boot on :25566 (run-verify/)
```

---

## What was completed in the 2026-07-21 session

### 1. Hypixel-Skyblock-style stats system (NEW — was missing entirely)
**File:** `PlayerStatsManager.java` (+ `ArmorStandAccessor` mixin)

- 8 stats: ❁ Strength, ☣ Crit Chance (base 30%), ☠ Crit Damage (base 50%), ❈ Defense,
  ❤ Health, ✦ Speed (base 100), ✎ Intelligence (base 100), ⫽ Ferocity.
- Stats are parsed **from gear lore lines** every second (armor slots + main hand).
  Accepted format (symbol optional, color codes ignored): `❁ Strength: +50`, `Defense: +30`,
  `Crit Chance: +10%`, etc. Held items only contribute offensive stats.
- Application points:
  - Outgoing damage: end of `CustomItemHandler.calculateSlayerDamage`
    → Strength multiplier `×(1+str/100)`, crit roll `×(1+cd/100)`, Ferocity extra strikes.
  - Incoming damage: player branch of `BountyDamageMixin`
    → `dmg × 100/(100+defense)` (void damage exempt).
  - Health/Speed: transient attribute modifiers (`political:stats_health`, `political:stats_speed`).
  - Intelligence: max mana bonus, wired into `MagicManager.getMaxMana`.
- **Floating damage numbers** (holograms) on every player hit — invisible marker armor
  stands, capped at 24, auto-despawn after 16 ticks (`tickServer` in the main tick loop).
- Action bar every 2s: `❤ HP  ❈ Defense  ✎ Mana`. `/statbar` toggles it per player.
- `/stats` prints the full stat sheet.

**To finish/extend:** add stat lore lines to more gear. Any item whose lore contains
e.g. `§7❁ Strength: +40` automatically feeds the system — no code changes needed.
Slayer/dragon/block gear lore builders live in `SlayerItems.java`, `BlockGearLore.java`,
`DragonChestplates.java`.

### 2. Mob scaling (FINISHED — was mostly built, had bugs)
**File:** `HealthScalingManager.java`

- 10 tiers (Enhanced → Eternal), weighted random rolls, 30% of eligible spawns — unchanged.
- **Fixed: restart double-scaling.** Scaled mobs now carry persistent command tags
  (`political_scaled`, `political_tier_N`). On chunk load the mod restores tracking from
  tags instead of re-rolling (previously a restarted server could re-multiply a mob's
  health and give it a second name prefix).
- **Fixed: empty kill-reward message.** Kills now show `+X coins (✦ Tier Mob)` on the
  action bar (message text was missing entirely).
- Kill bonus now reads the stored tier first (name parsing kept as fallback).

### 3. Unique full-set bonus per armor set (NEW registry)
**File:** `UniqueArmorSetBonuses.java`, hooked from `BlockArmorHandler.tickUniqueFullSetAbility`

- All **144 block-armor materials** now have a hand-assigned, uniquely named 4/4 bonus
  (e.g. oak_log "Heartwood Guard", copper_block "Live Wire", tnt "Short Fuse").
- 20 mechanic archetypes (auras, counters, storm charge, gravity well, lifeline, leech…)
  × per-set cadence/radius/power/particle = distinct behavior per material.
- Item lore auto-syncs: `BlockGearLore.getShortSetBonus` reads the registry, so tooltips
  show the real bonus name + description.
- Unknown/new materials safely fall back to the old generated-signature system.
- Legacy hand-coded per-material handlers in `BlockArmorHandler` (obsidian, glowstone, …)
  still run **in addition** — they were kept intentionally (stacking flavor). If a specific
  combo feels too strong, remove the legacy `tickXArmor` call for that material.

### 4. Weapon ability visuals (ADDED)
- `WeaponAbilityHandler`: every named ability now has particles + sound
  (Berserk, Venomous Strike, Bone Shatter, Bounce, Crystal Edge, Void Strike,
  Thunder spark column + chain sparks, Holy smite, Vampiric drain, Frostbite).
- `BlockWeaponHandler.spawnHitVisual`: **every** of the 150+ block swords now emits a
  material-themed particle puff on hit (fire/soul/ice/spark/portal/… keyword mapping).
- Dragon swords already had held-item particle trails (`DragonSwordAbilities`) — untouched.

### 5. Bug fixes
- **Corrupted color codes** in `WeaponAbilityHandler` (`?c?lBERSERK!` → `§c§lBERSERK!`,
  11 occurrences — chat/action bar messages were displaying raw `?x` garbage).
- **The Gavel:** its "AOE damage" loop actually teleported the *player* behind the target
  repeatedly (copy-paste from Void Strike) and dealt no AOE damage. Now: 40% splash damage
  + slow + shockwave ring + anvil slam sound. (Splash uses magic damage on purpose —
  player-attack damage there would recurse through the damage pipeline.)
- **Ferocity i-frames:** extra strikes clear `invulnerableTime` so the main hit isn't
  swallowed by vanilla invulnerability (see comment in `PlayerStatsManager`).

---

## Known remaining work (prioritized)

1. **Roll out stat lore lines to gear.** The stats engine is live but only items with
   stat lines feed it. Recommended: add `❁ Strength` / `❈ Defense` / `❤ Health` lines to
   slayer armor (`SlayerItems`), dragon chestplates, and block armor tiers so /stats has
   visible progression. Pure lore edits — the parser does the rest.
2. **Balance pass.** Base 30% crit / 50% crit damage raises overall player damage ~15%.
   Tune in `PlayerStatsManager.Stat` enum (base values) if mobs die too fast.
3. **`PhantomStepMixin` TODO:** re-block sleeping for Phantom Step wearers (26.x sleep API
   changed; lore still promises "cannot sleep").
4. **`LootTableMixin` TODO:** proper block-vs-mob drop detection.
5. **StockMarket volume tracking** (`StockMarket.java:1056` returns 0 — GUI shows no volume).
6. **Docs:** `ABILITY_CATALOG.md` / `ABILITY_CATALOG_READABLE.md` list the old 8 shared
   set bonuses; regenerate from `UniqueArmorSetBonuses.REGISTRY` if you want accurate docs.
7. **Cleanup:** `*.java.backup` files in the source tree (not compiled, just clutter);
   `.windsurf/workflows/*.md` are empty files.
8. **Mob scaling ideas (optional):** scale tier weights by world age / distance from spawn;
   currently purely random.

## Architecture crib sheet (for whoever continues)

- **Damage pipeline (player → mob):** `BountyDamageMixin.modifyDamageAmount`
  → `CustomItemHandler.calculateSlayerDamage` → weapon/block handlers → **stats** (last).
- **Damage pipeline (mob → player):** same mixin, player branch → boss reductions
  → **defense stat** (last). Bounty-gear reduction is separate (`BountyDefenseMixin`).
- **Per-player ticking:** the big loop in `PoliticalServer.onInitializeServer`
  (`ServerTickEvents.END_SERVER_TICK`). Stats tick + armor set bonuses run there.
- **Custom items:** `CustomItemManager` (data-driven) + `SlayerItems` (hardcoded flagships).
  Item identity = custom id in item components, read via `SlayerItems.getCustomItemId`.
- **Mixins:** registered in `src/main/resources/political.mixins.json` (the
  `politicalserver.mixins.json` file only holds 2 legacy entries).
