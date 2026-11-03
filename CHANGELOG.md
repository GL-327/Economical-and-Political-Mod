# Changelog

## Unreleased

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
