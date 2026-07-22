# OBTAINING — V4 Item Reference

Every physical item added in V4, what it does, and exactly how you get it.

**Reading the grids.** All crafting happens in a normal 3x3 crafting table.
Recipes marked **shaped** must be laid out exactly as drawn. Recipes marked
**shapeless** only care that the listed items are somewhere in the grid.

You can always look a recipe up in game with `/recipe`, and admins can inspect or
retune it with `/setrecipe`. Every recipe listed below is registered under a name
starting with `V4 ` so it is easy to find.

---

## Settlements

### ✦ The Cornerstone
*The civic heart of a new realm. Place it to found a Settlement at that spot — it
attracts wandering citizens, grows through beds, appeal and treasury, and at high
tiers elects leaders. It still rings as a normal bell.*

**Crafted — shaped**

```
[ Emerald Block ] [ Gold Block ] [ Emerald Block ]
[ Gold Block    ] [ Bell       ] [ Gold Block    ]
[ Emerald Block ] [ Gold Block ] [ Emerald Block ]
```

Total cost: 4 Emerald Blocks (36 emeralds), 4 Gold Blocks (36 gold ingots), 1 Bell.

**Also obtainable:** server owners can run `/settlement cornerstone` to receive one
directly.

> This is the entry point to the whole settlement system — settlements, citizens,
> elections, the High Council and the Atlas all start with a placed Cornerstone.

---

## The Backrooms

You reach the Backrooms by no-clipping through an End gateway (~3% chance per
transit) or with `/backrooms enter`. You leave by finding a humming exit portal,
by using an Escape Beacon, or with `/backrooms leave` (no reward).

### Fluorescent Fragment — **found only**
*Scraped from a humming light panel. The core Backrooms crafting material; both
Almond Water and the Reality Shard are gated behind it.*

There is no way to make Fluorescent Fragments out of ordinary materials. You have
to go in and get them:

| Source | Rate |
| --- | --- |
| Stash-cell chests (the room variant with a chest at its centre) | 2–6 per chest, always present |
| Wanderer kills | 1–2 always, plus 1 extra per Wanderer tier (so up to 5 from a 4-star) |

**Recycling path — shapeless**

```
[ Reality Shard ] + [ Glowstone ]   ->   8x Fluorescent Fragment
```

This shatters a shard back into fragments. It never creates fragments from
nothing — a shard costs 8 fragments plus an ender pearl to make — so the gate on
actually entering the Backrooms still holds.

### ✦ Almond Water
*A bottle of impossibly pure water. Drink to cleanse every ailment and gain
Regeneration II for 10s plus Night Vision for 20s.*

**Crafted — shaped, makes 3**

```
[ Fluorescent Fragment ] [ Glass Bottle ] [ Fluorescent Fragment ]
[ Sugar                ] [ Glass Bottle ] [ Sugar                ]
[ Fluorescent Fragment ] [ Glass Bottle ] [ Fluorescent Fragment ]
```

**Also drops from:**

| Source | Rate |
| --- | --- |
| Stash-cell chests | 55% chance of 1 |
| Wanderer kills | 12% at 1-star, rising to ~39% at 4-star |

### ✦ Reality Shard
*A fragment of stable reality. The ingredient for an Escape Beacon.*

**Crafted — shaped**

```
[ Fluorescent Fragment ] [ Fluorescent Fragment ] [ Fluorescent Fragment ]
[ Fluorescent Fragment ] [ Ender Pearl          ] [ Fluorescent Fragment ]
[ Fluorescent Fragment ] [ Fluorescent Fragment ] [ Fluorescent Fragment ]
```

**Also drops from:**

| Source | Rate |
| --- | --- |
| Stash-cell chests | 18% chance of 1 |
| Wanderer kills | 3-star: 6%; 4-star: 14%. Never from 1- or 2-star Wanderers |

### ✦ Escape Beacon
*Right-click inside the Backrooms to tear open an exit back home. Consumed on
use, and it counts as a real escape — you get the escape achievement progress and
the damage buff, same as walking out through a portal.*

**Crafted — shaped**

```
[ Glowstone ] [ Eye of Ender  ] [ Glowstone ]
[ Eye of Ender ] [ Reality Shard ] [ Eye of Ender ]
[ Glowstone ] [ Eye of Ender  ] [ Glowstone ]
```

Total cost: 1 Reality Shard, 4 Eyes of Ender, 4 Glowstone blocks.

### Wanderers (mobs, not items)
The hostile inhabitants of the Backrooms — tagged husks and silverfish that spawn
in dark cells and smother you with Darkness while they are close. They come in
four tiers (1★ to 4★); higher tiers hit harder, have more health, and drop
noticeably better loot. They also feed normal kill progression.

---

## Magic V4

Magic gear — the eight school robe sets, Catalysts, Grimoires, Technique Relics,
the Awakening Catalyst and the Archmage endgame set — is **documented separately**
by the magic content pass. Look those items up with `/recipe`, `/magicgear` and
`/awaken` in the meantime.

Arcane Shards are a currency tracked on your profile, not an item you hold; you
earn them by killing magic creatures and spend them in the school trees.

---

## Quick index

| Item | Category | How you get it |
| --- | --- | --- |
| ✦ The Cornerstone | Settlement | Craft: 4 Emerald Blocks + 4 Gold Blocks + Bell (shaped). Owners: `/settlement cornerstone` |
| Fluorescent Fragment | Backrooms | Found only: stash chests (2–6) and Wanderer kills (1–5). Or shatter a Reality Shard for 8 |
| ✦ Almond Water | Backrooms | Craft: 4 Fragments + 3 Glass Bottles + 2 Sugar → 3. Also 55% from stash chests, 12–39% from Wanderers |
| ✦ Reality Shard | Backrooms | Craft: 8 Fragments + Ender Pearl. Also 18% from stash chests, 6–14% from 3★/4★ Wanderers |
| ✦ Escape Beacon | Backrooms | Craft: Reality Shard + 4 Eyes of Ender + 4 Glowstone |
| Magic gear | Magic | See the magic content documentation |
