# Changelog

## Unreleased

### M0 hygiene and baseline
- Stopped tracking `.claude/settings.local.json` (it holds machine specific paths). The local file is untouched.
  `.claude/*.local.json` is now in `.gitignore`.
- Moved the 10 empty `.windsurf/workflows/*.md` files to `archive/windsurf-workflows/` and the 27 one-shot
  scripts from `scripts/` to `scripts/archive/` (with a README). Nothing was deleted.
- Added a JUnit 5 test source set (test scope only) and `ModMetadataTest`, which checks the mod id, the server
  environment and the absence of a client entrypoint.
- Refreshed the `description` in `fabric.mod.json`. `license` and `authors` are unchanged pending the owner.
- Added `HANDOFF.md`, the resumable state for the modernization program.
