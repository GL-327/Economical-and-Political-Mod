# scripts/archive

One-shot scripts moved here from `scripts/` during modernization M0.10. Nothing in the build, CI or
`build.gradle` references them. They are kept for history only, not deleted.

| Group | Files | What they were for |
|---|---|---|
| Mojmap migration | `mojmap_bulk.*`, `mojmap_pass2` to `mojmap_pass14_*` | Passes that rewrote the source from Yarn names to Mojang names for 26.1. |
| Recipe maintenance | `apply_recipe_core_policy.js`, `audit_*`, `fill_missing_customitem_recipes.js`, `hydrate_all_recipe_ingredients.js`, `normalize_*` | Edited and audited `config/political/custom_recipes.json`. |
| Lore patches | `patch_block_*`, `patch_obsidian_cores.js` | Patched gear lore. The block armor, weapon and tool classes they targeted are not in the current tree. |

These were written against an older source layout and are untested against the current one. Treat them
as reference, not as tools to run. `tools/mojmap_bulk_fix.py` was left where it was.
