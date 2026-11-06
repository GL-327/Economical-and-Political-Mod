# Changelog

## Unreleased

### M10 main menu presentation
- Rebuilt the main pages of `/recipe`, `/setrecipe` and `/customitems` on the shared screen module, with paged category cards, named count plates, headers, search and fixed footer controls.
- Preserved the original inner category views: material/icon, spacer, sword and armor-set rows, tier rows, tool sets and mob-drop sections. Restored these after owner feedback rejecting flattened set lists.
- Filled the retained gem armor row renderer's empty display array using existing Emerald/Lapis swords and armor; recipe/item definitions and balance are unchanged.
- Recipe browser entry points remember their actual parent; details return to the actual set/list page, and commands clear prior menu trail state.
- Search results are paged; complete item lore is retained. Added pure layout/search/translation tests and real-client checks/screenshots for the main menus and preserved grouped rows.

### M0 hygiene and baseline
- Stopped tracking `.claude/settings.local.json` (it holds machine specific paths). The local file is untouched.
  `.claude/*.local.json` is now in `.gitignore`.
- Moved the 10 empty `.windsurf/workflows/*.md` files to `archive/windsurf-workflows/` and the 27 one-shot
  scripts from `scripts/` to `scripts/archive/` (with a README). Nothing was deleted.
- Added a JUnit 5 test source set (test scope only) and `ModMetadataTest`, which checks the mod id, the server
  environment and the absence of a client entrypoint.
- Refreshed the `description` in `fabric.mod.json`, set `license` to `CC0-1.0` (matches the `LICENSE` file), set `authors` to `GL-327`, added contact links and bounded `fabric-api`.
- Moved the nine root docs to `docs/archive/` (stale, kept for reference).
- Added `HANDOFF.md`, the resumable state for the modernization program.

### M1 core infrastructure
- Moved to Minecraft 26.3: Gradle 9.8.2, Fabric Loom 1.19.3 (a release, no longer a snapshot), Fabric Loader 0.20.1,
  Fabric API 0.165.2+26.3, sgui 2.2.1, placeholder-api 3.2.0, player-data-api 0.11.0, polymer 0.18.6.
- Ported the sources to the 26.3 API (colored items, entity types, drops, knockback, velocity sync, copper
  collections) and five mixins and five worldgen processor lists to their new shapes.
- Added `com.political.core.MixinAudit`, a dev check that loads every mixin target
  (`-Dpoliticalserver.mixinAudit=true`).
- Logging: every `System.out`, `System.err` and `printStackTrace` call (118 sites in 40 files) now goes through an SLF4J logger
  with the exception attached; `SourcePolicyTest` keeps it that way.
- Error handling: no broad `catch (Exception|Throwable)` swallows an error silently any more (85 did). Each now logs with its
  class and method, or rethrows; 16 were narrowed to the exceptions they can actually get. No gameplay change.
- Timing: added `com.political.core.Clock`. Boss, armor, weapon, item, home and bounty-armor timers that live only in memory now run
  on game ticks instead of the wall clock (same durations, but a lagging server no longer skips them). The other wall-clock uses are
  listed with reasons in `src/test/resources/wallclock-allowlist.txt`.
- Libraries: dropped the bundled placeholder-api and player-data-api (no code used them; player-data-api has no 26.3 build). Added
  `docs/PROFILING.md` on using spark. Recorded why fabric-permissions-api and Caffeine were rejected (Fabric API and Guava already
  provide them).
- Tick architecture: the one 140-line tick handler is now 72 named services (`TickRegistrations`) run by a `TickDispatcher` in the
  same order. A service that throws no longer stops the others, slow services are logged by name, and the server logs its eight
  costliest services when it stops.
- Persistence layer: `VersionedStore` writes versioned JSON atomically, applies one migration per schema step, copies a corrupt file
  aside instead of overwriting it and refuses to overwrite a file from a newer version. Nothing uses it yet; the power system (M2) is first.
- Config: new `config/political/config.json` (created with defaults on first start) with typed, range-checked settings; a bad value
  falls back or is clamped with a logged warning and never stops the server. `docs/CONFIG.md` lists every setting and is generated
  from the code. The first settings are the tick budgets and the data flush interval.
- Commands: one registration entry point (`ModCommands`) and a registrar that reports a command registered twice; in a development run
  that is a startup error. The eight roots that were defined twice (bounty, credits, coins, judge, startundergroundauction,
  placeauctionmaster, view, hunt) are one definition each; the command tree is identical to before.
- Text: added a translation layer (`Text.tr`, `assets/politicalserver/lang/en_us.json`) and the bundled server translation library, so
  new text can be translated per player language on a vanilla client, with English as the built-in fallback. Existing text is
  unchanged; `docs/TEXT_MIGRATION.md` plans its conversion.
- Screens: added a shared layout module (`com.political.core.gui`: palette, icons, slot grid, pager, stepper, progress bar, frame,
  header, footer, confirmation) and `STYLE.md`, the standard for everything a player sees. No existing screen was changed yet.

### M2 power system
- Casting: sneak and the swap-hands key (F by default) now cast the selected ability through one validation pipeline with fixed failure reasons (global cooldown,
  rate limit, range, valid target, enough resource, per-ability cooldown). The eight older techniques and their 32 abilities keep their
  effects but obey the same limits and spend one shared resource. Every number is a config key under `power.*` (`docs/CONFIG.md`).
- New abilities: Blue (pull), Red (push), Cursed Blast, Reverse (heal), Barrier, Domain and Black Flash, plus binding vows (at most two).
  Black Flash replaces the critical hit multiplier instead of stacking with it. A Domain is a sphere of radius 12 around the caster.
- Progression: Arcane Shards earned in total set a power level; school tree nodes give damage, resource and cooldown passives; the tier of
  a school follows the spine nodes (1, 4, 7, 10). The spell mana pool is the power resource.
- Saving: `power.json` (schema 1) replaces `techniques.json`, `magic_data.json` and `magic_schools.json`. On first start the old files are
  imported, copies are kept as `<name>.pre-power`, and anything that does not fit is quarantined rather than dropped. Cooldowns, cast
  history and Domains are not saved.
- Casting input fixed: the earlier gesture (right click in the air with an empty hand) never reached the server, because the game client sends no
  use-item packet for an empty hand, so techniques could only be cast with `/technique cast`. The swap-hands key does reach the server whatever
  the player holds. A player with a technique equipped who sneaks and presses F casts instead of swapping hands; `/technique cast` still works.
- Old entry points (`TechniqueManager`, mana and shard methods) now forward to the new service; nothing was deleted.
- Screens: the Techniques screen is now the Powers hub (level, Arcane Shards, school, techniques, abilities, the school tree with its next
  step and cost, binding vows, cursed energy). Every existing menu entry that opened Techniques opens it; there is no new player command.
- Admin: a power administration screen (every online player's state, grant, revoke, set school, reset, who holds unique techniques) and
  `/poweradmin`, both guarded by the permission `politicalserver:power.admin` (operators of level owners by default). A reset keeps a
  copy of the old state in `power.json`. See `docs/OWNER_POWER_TEST.md` for the owner's test script.
- Effects: each power effect now has a short, medium and long variant; a Domain starting or ending shows a title; screens play the open sound.

### Rolled techniques, closer to vanilla (2026-11-05)
- Every player gets one random innate technique at first join (weights in `Innate`, every number a config key and unapproved). It is asleep until an
  awakening boss dies (default: the Ender Dragon, players within 160 blocks). Mastery ranks (Novice, Adept, Expert, Master) come from casts and raise
  damage and cut cooldown; a Domain needs Master. A technique can be rerolled with shards, a reroll charge or a manual; players from before are
  awakened. Schools and the school tree no longer decide anything. `power.json` is schema 2 (a copy `power.json.v1.bak` is kept on migration).
- Closer to the base game: world bosses, nemesis, incursions, the Great Hunt and wandering spirits are off by default (`features.*` switches, nothing
  deleted). Mob scaling, the skyblock systems and custom tools, armour and weapons are unchanged.

### M3 economy
- `core.economy.Ledger` sits behind every coin balance change (`addCoins`, `removeCoins`, `setCoins`, transfers): saturating credits, atomic transfers,
  a journal of 5000 transactions with the asking class, totals per reason. `/ledger` (owners) shows them. The saved coin format is unchanged.
- `docs/ECONOMY_MAP.md` and `docs/ECONOMY_CAPS.md` (generated) list where coins change and which sources have no cap word. No amount was changed.
- Stock market: shares traded are now counted (it returned 0).

### M4 politics
- The election system is an explicit state machine (`ElectionPhase`: disabled, paused, suspended, term, campaign) with a table of legal moves and tests;
  the election tick runs under the same condition as before.

### M5 combat
- The damage hook is a list of named, ordered stages (`damage.DamageStages`, `core.damage.DamagePipeline`) in the same order as before; damage can no
  longer come out negative or NaN. 8 pipeline tests; the real-client combat phase covers it.

### M6 to M9
- Audits (no behaviour change): `docs/CONTENT_AUDIT.md` (what spawns entities), `docs/UI_INVENTORY.md` (113 menu classes), `docs/UI_BACK_AUDIT.md`.
- Snapshot attempt on branch `snapshot/26.4-snapshot-7`: compiles and passes tests after four renames; boot is blocked by Polymer (`docs/SNAPSHOT_PORT.md`).

### Menus
- Back now leads to the menu the player came from. `core.gui.ReturnTo` remembers the parent of a screen; thirteen screens that had no Back and a
  dozen admin and politics screens take their parent from the menu that opens them. `core.gui.MenuTrail` watches each player's menus and gives any
  menu that still has no Back one that re-opens the menu before it (the screen is shown as it was, buttons read the live state when clicked).
- Fixed: the inventory viewer's Back sat on the ninth hotbar slot and was drawn over; bounty drop tables returned to the wrong screen; the gem armor
  page threw an index error because its list is now empty.
- A real-client crawler (`scripts/power-client-test.sh setup,menus,adminmenus`) opens every menu reachable from the main ones, presses Back and checks
  where it lands; `MenuBackTargetsTest` checks the fixed Back wiring.
- Fixed: `.gitignore` hid the `core/config` source package, so clean checkouts did not compile; a test now guards it.
