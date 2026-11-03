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
