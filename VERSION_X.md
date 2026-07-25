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
