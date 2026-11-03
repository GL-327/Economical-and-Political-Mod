# Power system design (M2)

One power system. Schools are the structure, techniques are the unit of power, and every cast goes through
one validation pipeline. This page records what the code did before M2 (found by reading it), the
decisions taken, and how the new package `com.political.power` is laid out. `HANDOFF.md` keeps the live
status and the evidence for each claim.

## What existed before M2

| Piece | Where | State found |
| --- | --- | --- |
| 8 techniques with 4 abilities each (the program says 10) | `technique/Technique.java` | Live. Learned from a Technique Manual, ranks by use count (Novice, Adept 15, Expert 45, Master 100), 4 abilities unlocked by rank. |
| Cast input | `items/CustomItemHandler.java` UseItemCallback | Live. Sneak plus right click with an empty main hand casts the selected ability. |
| Energy pool | `TechniqueManager.PlayerTechniqueData.energy` | Live. Max 80 + 20 per rank, regen 4 per second by wall clock. |
| Barrier | Ironmarrow `unbreakable` (`BARRIER_UNTIL`) | Live. 2.5 s of absorbing all damage through `BountyDamageMixin`. |
| 12 magic schools (the program says 8), 33 spells, mana pool, magic levels | `magic/*`, `SchoolTree*` | **Dead code.** `MagicManager.initialize`, `SchoolTreeManager.initialize` and the spell tick handler are never called, so nothing is loaded or saved and mana never regenerates (`MagicManager` itself says "Unused on purpose"). Only the mana number is read, by the stats screen and companions. School trees are reachable by operators from the admin panel. |
| Exclusive techniques, awakening, Black Flash, Blue, Red, Domain, vows | none | Absent. `Technique.exclusives()` returns an empty list. |

## Decisions

- **One system** (program default, 10.2). Schools are the structure; a player picks a school and its tree
  grants techniques. The 8 existing techniques and the new cursed techniques are all techniques in this
  one model; the 33 dead spells are not ported and stay a skeleton until the owner decides about them.
- **Resource:** one pool per player, the mana pool (program default, C2), owned by the power system. The
  old technique energy is folded into it. `MagicManager.getMana/setMana/getMaxMana` keep working as
  adapters so the stats screen and companions need no change.
- **Domain** is available from a school capstone only, **Black Flash** needs school tier 3 (program defaults).
- **Where techniques live** (first mapping; the owner may re-map in `PowerCatalog`):

  | School | Techniques | Unlocked at spine node |
  | --- | --- | --- |
  | Pyromancy | Emberheart | 1 |
  | Frostweaving | Glacier Soul, Tidebound | 1, 4 |
  | Stormcalling | Storm Vein, Whisperwind | 1, 4 |
  | Voidwalking | Umbral Step, Cursed Blast | 1, 4 |
  | Verdancy | Wildgrowth | 1 |
  | Gravitas | Ironmarrow, Blue, Red | 1, 4, 7 |
  | Radiance | Reverse, Barrier | 1, 4 |
  | Bloodbinding, Chronomancy, Resonance, Sporeweaving, Astralism | none yet | not selectable |

  A school's **tier** is the number of the milestones 1, 4, 7 and 10 it has reached on its spine, so the
  capstone (node 10) is tier 4 and Black Flash (tier 3) needs node 7. Domain needs the capstone.
- **Barrier and falling or void damage** (C7): the barrier blocks fall damage and does not block void
  damage or `/kill`, which nothing can block. Both are config keys.
- **Black Flash and crits** (C8): a Black Flash hit replaces the crit multiplier instead of stacking with it.
- **Cooldowns are not saved.** They run on server ticks, which restart with the server; a restart clears
  cooldowns and only the owner can restart the server.

## Package layout

| Package or class | Job | Needs Minecraft? |
| --- | --- | --- |
| `power.PowerSettings` | every number as a documented config key (generated into `docs/CONFIG.md`) | no |
| `power.model` | `AbilityKind`, `AbilitySpec`, `PlayerPower` state, `Vow`, resource pool arithmetic | no |
| `power.cast` | `CastFacts`, `FailureReason` (the 15 reasons in order), `CastPipeline`, `EffectPlan` | no |
| `power.formula` | Blue, Red, blast, reverse, Black Flash, domain damage, vows, resource regen | no |
| `power.store` | `PowerStore` on `VersionedStore`, migrations from the old files, quarantine | no |
| `power.game` | facts from a real player, target resolution, effects, input hook, damage hook, GUI, commands | yes |

Everything that decides something is in the no-Minecraft packages and is unit tested; `power.game` only
reads the world and applies the result.

## Cast flow

1. The single input handler calls `PowerInput.onUse(player)`.
2. `PowerService.cast` builds `CastFacts` from the player and runs `CastPipeline`. The first failing check
   stops the cast and its reason is shown in the action bar.
3. On success the pipeline returns an `EffectPlan` (cost, cooldown, what to do) computed as data.
4. The service deducts the cost, sets the cooldowns, then executes the plan in the same tick. If the
   execution throws, cost and cooldowns are rolled back, the error is logged with the player UUID and the
   technique id, and the player gets a generic failure message.

## Not done in M2 (and why)

- The owner test script (section 15, items 1 to 12) needs a vanilla client and cannot be run by the agent.
  Until it passes, M2 is **not** done by the program's definition, and M3 must not start.
- Visual effects use the particles and sounds the old techniques already used; Polymer item models for the
  new techniques wait for assets (recorded as a TODO).
