# V4 Status — COMPLETE

Build **GREEN** · Server boots clean · **2167 recipes, 0 failed** · `v4-wip/` empty.
Jar: `build/libs/politicalserver-1.0.0.jar` · Item reference: `OBTAINING.md`

## Headline numbers

| | Before V4 | Now |
|---|---|---|
| Recipes loading | 1976 (**71 failed**) | **2167 (0 failed)** |
| Magic schools | 0 (single flat tree) | **12** with node trees |
| Techniques | 0 | **24** (18 common + 6 exclusive single-holder) |
| Obtainable spells | 16 of 33 | **33 of 33** |
| Settlement tiers | 0 | **9** (Outpost → Elysium) |

---

## Anti-cheese: the diamond-block exploit is closed

The old appeal formula was a raw weighted sum, so 1,000 diamond blocks = 10,000 appeal =
instant Utopia. Replaced with a capped, multi-axis model:

- **Diminishing returns** — each block *type* contributes `weight × √count`.
- **Per-type cap** (150) and **per-category caps** — appeal is bucketed into Monument /
  Precious / Lighting / Greenery / Furnishing / Crafted. Precious is capped at **350**.
- **Footprint scaling** — appeal squeezed into one or two chunks is scaled down.
- **Sheltered beds only** — a bed needs a roof within 5 blocks and walls on 3 of 4 sides.
  A field of 250 beds no longer counts as housing.
- **Tier gates** — City: 8 workstations. Metropolis: fed population, ≥50 happiness, 8+
  developed cells, 16 workstations. Utopia: ≥70 happiness, 4+ appeal categories, 20 cells,
  32 workstations.

**Verified:** 100 diamond blocks → 50 appeal. 1,000 → 93. **10,000 → 93** (hard-capped).
City needs 150, Utopia needs 2,500. Block spam cannot pass Town.

Transparency commands: `/settlement appeal` shows the category breakdown and caps;
`/settlement economy` shows the full production report.

## Fallout 4-style settlement economy

`SettlementEconomy` runs a production cycle every 60s:

- Settlers **auto-assign to jobs**, bounded by **built workstations** — you cannot employ
  more citizens than you have built for. Law enforcement is staffed before trade.
- Jobs yield **food / goods / coins / defense / morale**. Innkeepers and foragers feed the
  settlement; merchants, blacksmiths, couriers and scholars generate income.
- **Food must cover population** or happiness craters and growth halts entirely.
- **Happiness (0–100)** multiplies income 0.4×–1.5× and gates new arrivals.
- **Raids**: wealthy, under-defended settlements lose ~12% of treasury. Sheriffs and
  deputies deter them.
- **Growth** requires a free sheltered bed + food surplus + ≥45 happiness.

## Politics fully tied to settlements

- Settlements **remit tax** from earnings into the national party budgets at the Chair's
  tax rate (softened by the Tax Reduction edict).
- Incumbents are judged on **appeal, happiness and food security** — starvation applies a
  flat −1.2 to the incumbent's score, which loses elections outright.
- Cities elect a Constituent; Metropolis+ elect Chair/Vice; sub-metropolis settlements vote
  in their nearest metropolis's election; the largest metropolis drives the national Chair.
- Utopia+ unlocks the **High Council** (Prime Seat + 4 ministries) enacting timed Edicts.

## Gaps closed this round

- `crystal_shove` → `crystal_shovel` (dead handler now fires).
- **Aether tool set** implemented: Wind Cut (axe), Skyfall (pickaxe), Updraft (shovel),
  Serene Rebuke (hoe) — previously shovel and hoe had *zero* abilities.
- **4 flagship stubs are real**: Voidwalker's Crown (Void Step, Phase Dodge wired into the
  damage path, dark-damage bonus), Ender Phase Helmet (was a 3-line stub with a placeholder
  comment; `isInEnderPhaseMode` was hardcoded `false`), Venomous Dagger (stacking venom),
  Garden Heart Overalls (crop bonus now actually reads the id on harvest).
- **All 17 orphaned spells** now have skill-tree nodes — 33/33 obtainable.
- `MagicGui` renders **8/8 nodes per category**; level-50 capstones are reachable.
- `SchoolTreeGui` widened to 12 slots so the new schools render.
- Lore across the flagships was rewritten to describe what the code *actually does*
  (the Crown previously advertised a teleport and wall-vision it never had).

## Recipes

Everything craftable is registered through `RecipeConfigManager`, so it appears in **both**
`/recipe` and `/setrecipe` automatically. 120 new recipes added; `OBTAINING.md` documents
every V4 item with its grid or its drop source.

Also fixed: **Crown of Greed / Crown of Midas** were the last 2 failing recipes — their
stored NBT named `minecraft:golden_crown`, an item that does not exist. Added a general
recovery path that rebuilds an item from its `custom_item_id` when the stored base item is
invalid, so one bad export can never silently drop a recipe again. Failures are now logged
**by name** (`getLastFailedRecipeNames()`), not just counted.

## Wars between rival governments

A world holds several sovereign polities at once — every METROPOLIS+ elects its own Chair,
and lesser settlements follow the nearest one. Those governments can now go to war.

**War Council weights** (`/war council` shows the live breakdown):

| Seat | Weight | Kind |
|---|---|---|
| Chair | 5 | elected |
| Vice Chair | 4 | elected |
| Prime Seat (High Council) | 3 | appointed |
| Minister | 2 | appointed |
| **Shadow Government** | **2** | **unelected** |
| Constituent (follower city) | 1 | elected |

The elected Chair + Vice cast **9**, more than a full four-seat shadow bench (**8**), and the
**Chair holds an absolute veto** — any motion they vote against fails regardless of the
arithmetic. Unelected power can tip a divided council; it cannot overrule a united elected
leadership. A shadow bench can only carry a motion if the elected officials abstain entirely.

**Shadow Government** membership is *emergent, not appointed* — it reflects who actually
holds power: Financiers (wealthiest citizens, 100k+ coins), the Party Boss (ruling-party
leader), Former Chairs (out of office, never out of power) and a Spymaster. Sitting officials
are excluded — you cannot hold both the throne and the shadows. An outgoing Chair is
automatically recorded into the shadow bench on losing an election.

**War mechanics**: a motion opens a 10-minute council vote → on passing, war is declared.
Citizens of warring polities deal **+25% damage** to each other; every wartime kill scores a
point and **plunders treasury** from the loser to the winner; both sides suffer a **trade
embargo** (−25% settlement income, inherited by follower settlements). Peace requires the
Chair, Vice Chair or Prime Seat to sue after a 20-minute minimum, and takes effect when the
other side reciprocates.

Commands: `/war`, `/war council`, `/war shadow`, `/war declare <polity>`, `/war vote yes|no`,
`/war peace`.

## Notes / remaining judgement calls

- Settlement thresholds and technique damage are unplaytested — expect a balance pass.
  All tunables are constants at the top of `SettlementScanner`, `SettlementEconomy`,
  `SettlementTier` and `Technique`.
- Exclusive techniques are first-come: 5% roll on the Rite of Awakening, weighted toward
  unclaimed ones. Once all 6 are held, further rolls fall back to common techniques.
