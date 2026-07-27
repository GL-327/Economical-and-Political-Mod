# Version X

There is no version nine or ten. Everything below landed as one large update, which is why the mod
jumps from eight straight to Version X.

Open `/civ` for the hub. Every system below is reachable by clicking from there, and no feature in
this mod requires typing a command.

---

## Reforges

The content multiplier. Over forty reforges across weapon, armour, tool and universal pools. Any
custom item in the mod can carry one, which renames it and layers its own stat block on top of what
the item already gives. One Cursed Blade becomes dozens of meaningfully different Cursed Blades.

- Open the anvil from the hub or with `/reforge` while holding an item.
- Reroll cost starts at 5,000 coins and climbs by 2,500 per reroll on that same item, capped at
  100,000. Chasing a perfect roll on one weapon is a real sink.
- Reforges never stack. The lore block is delimited by a marker, so applying a new one strips the
  old block first. The original name is stored once so prefixes cannot pile up.
- Craft the Reforge Stone to get started.

## Relics and the accessory bag

Forty five relics, each a single hand written effect rather than a stat stick.

- They fire on a strike, on a kill, when you are hurt, or simply while carried.
- Only relics in your bag are live. The bag starts at three slots and grows to nine, so relics are
  a choice about what kind of fighter you are rather than a pile of stacking buffs.
- Passive relics are craftable. The rest are found.
- Open with `/relics` or `/bag`.

## Companions

Twenty one companions across five roles: damage, tank, support, forager and scout.

- One walks with you at a time.
- They level from your kills, with a daily experience cap so they cannot be grown by leaving the
  game running overnight.
- They are removed cleanly when dismissed, when you log out and when the server stops.
- Common and uncommon species are craftable, the rarer ones are found.
- Open with `/companion` or `/pets`.

## The contract board

Thirty two contract templates across hunt, gather, deliver, explore, craft and civic work.

- The board rewrites every two hours and everyone sees the same offers.
- You may hold three at once, and there is a cap on how many you can finish per rotation.
- Pay is deliberately modest. Going out and playing will always earn you more, so the board is a
  reason to log in rather than a way to farm.
- Open with `/contracts`.

## Cinematic effects

Fourteen more animated set pieces on top of the nine from V8, all built on the same scheduler so
they play out over many frames rather than appearing at once.

Orbital strike, chain lightning that hops between targets, a rising pillar of flame, a singularity
that drags everything inward then detonates, a blade slash, a shock cone, healing rain, a frost
nova, a summoning circle, a soul harvest, a comet volley, a shield dome, a ground fissure and an
execution finisher.

Every one is layered the same way: a dense core that reads as the shape, a sheath spun around it for
thickness, a loose haze to soften the edge, and sound underneath. Browse them in the gallery with
`/vfx`.

---

## Building and running

This JDK 25 environment has a HotSpot C2 crash that takes down both the compiler and the server.
Until that is resolved, build and run with the JIT disabled:

```bash
JAVA_TOOL_OPTIONS=-Xint ./gradlew build
```

Interpreter mode is slow enough that the server watchdog can kill startup before the recipe
bootstraps finish. For verification runs only, set `max-tick-time=-1` in `server.properties`.

## Balance notes

Every system here was written to avoid becoming the optimal way to earn.

- Reforge rerolls cost coins and cost more each time.
- Relic effects are individually modest and the bag caps how many run at once.
- Companion experience is capped per day, and a companion is help rather than a second player.
- Contracts are capped per rotation and pay less than active play.

## The seven gear lines

Version X adds seven combinatorial gear lines. Each is a motif set crossed with a form set, and no
two lines share a mechanic. That was the point: seven families that all played the same way would be
one family with seven skins.

| Line | Motifs | Forms | Pieces | What drives it |
| --- | ---: | ---: | ---: | --- |
| Starbound | 36 constellations | 12 | 432 | Always on. Three of a house lights the house. |
| Beastcraft | 45 beasts | 14 | 630 | One Instinct meter fed by landing hits. The clade sets the fill rate. |
| Clockwork | 45 mechanisms | 14 | 630 | Pressure winds on its own and is spent by breaking blocks. |
| Grimoire | 45 sigils | 14 | 630 | Sneak and use to cast, spending Focus. Focus refills from Intelligence. |
| Tempest | 45 storms | 14 | 630 | Hits hang a charge. Two different charges on one target react. |
| Voyager | 45 waypoints | 14 | 630 | Reads your terrain reach and your distance from the nearest settlement. |
| Regalia | 45 offices | 14 | 630 | Reads your settlement tier and the council seat you hold. |

Voyager and Regalia are deliberate opposites. Voyager is close to worthless at home and strongest
twelve Leagues out. Regalia is the reverse, and needs a settlement and an election to pay out at all.

Tempest is the only line that reads what other players are doing. Charges sit on the target rather
than on the wearer, so two players running different fronts reach reactions neither could alone.
Twenty eight charge pairs, twenty eight distinct results.

## Crafting

Every line is keyed on its own catalyst rather than on raw vanilla loot. There are nine, one per
line, and each is craftable from a crafting core surrounded by that line's four signature materials.

Every piece of gear then asks for its line catalyst plus a crafting core of the tier that cut
deserves:

- Tools and charms take a T1 core.
- Weapons, ranged and the light armour take a T2.
- Chest and legs take a T3.

The grid reads the same way in every line. The piece being upgraded sits in the middle, the motif's
own token runs down both sides, the catalyst holds the top corners and the core sits underneath.
Anybody who has crafted one piece can craft any of them.

The Runeforge and the Aether Arsenal shipped before the catalyst layer existed and were rebuilt onto
the same footing, so the whole expansion reads one way in `/recipe` and `/setrecipe`.

## Verification

The recipe set is checked by booting the server and reading the self check plus the written JSON:

```bash
JAVA_TOOL_OPTIONS="-Xint -Xshare:off" ./gradlew runServerVerify
```

Current state:

- 7216 recipes, 0 failed, 0 duplicate names
- 4936 of 4936 Version X recipes carry a crafting core
- 4827 of 4827 gear ids round trip back to a motif and a form

Note that a verification boot takes half an hour or more under `-Xint`, and running any other Gradle
task while one is in flight corrupts the shared incremental compile state. Run them one at a time.
