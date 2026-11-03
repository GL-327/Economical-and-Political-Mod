# HANDOFF — Political Server modernization
Last updated: 2026-11-03 10:30 UTC  |  Session: 1  |  Device: Windows

## NEXT ACTION
Get the owner's answers to OWNER QUESTIONS 1 and 2 (license, author), then finish M0.4 in `LICENSE` and `src/main/resources/fabric.mod.json`; M1 does not start until M0's done-when is met (license decision recorded).

## STATUS
Milestone: M0   Branch: modern/m0-baseline   Last commit: 0a8455e M0/M0.11: add CHANGELOG.md and HANDOFF.md (this file's M0.7 update is committed after it)
PUSH STATUS: pushed to origin/modern/m0-baseline (5 commits, up to 0a8455e) before the first verify boot at 09:26 UTC; this M0.7 handoff update is pushed in the next commit
BUILD STATUS: GREEN (`./gradlew build --console=plain`, exit 0, 43s, with the new test; baseline before any change also exit 0, 1m31s)
TEST STATUS: 3 tests, 3 passing, `./gradlew build` (build/test-results/test/TEST-com.political.ModMetadataTest.xml)

## MILESTONE CHECKLIST
- [x] M0.1 Clone repo, branch, HANDOFF.md — the working copy already is the real repo (origin = https://github.com/GL-327/Economical-and-Political-Mod, history intact). Branch modern/m0-baseline cut from master 3189016.
- [x] M0.2 Stop tracking local settings — `git rm --cached .claude/settings.local.json`; `git ls-files .claude` now lists 0 files; `.claude/*.local.json` added to .gitignore (run/, run-verify/, build/, .gradle/ were already ignored). The local file was not deleted.
- [x] M0.3 Recount owner numbers — see VERIFIED and NOT VERIFIED.
- [ ] M0.4 Metadata — BLOCKED: license and author unanswered (OWNER QUESTIONS 1, 2). Done so far: `description` in fabric.mod.json refreshed. Not done: license, authors, `fabric-api` range (needs the target bump, OWNER QUESTION 3).
- [x] M0.5 Build green on current code before any modernization — `./gradlew build --console=plain`, exit 0, 1m31s, on 3189016 unmodified.
- [ ] M0.6 Loom — PARTIAL: no pin change. The `1.15-SNAPSHOT` pin currently resolves to Loom 1.15.5, a release (Gradle output "Fabric Loom: 1.15.5"), so there is little drift risk on 26.1 today. Whether any non-snapshot Loom supports 26.3 is NOT VERIFIED; Fabric's own example mod pins `1.19-SNAPSHOT` for 26.3. This can only be settled by the version bump.
- [ ] M0.7 JVM flags — PARTIAL, see the M0.7 notes under VERIFIED. No flag was removed.
- [x] M0.8 JUnit 5 test source set — `./gradlew build` runs `ModMetadataTest` (3 tests, 0 failures).
- [x] M0.9 Static checks — counts under VERIFIED.
- [ ] M0.10 Repo cleanup — PARTIAL: 10 empty `.windsurf/workflows/*.md` moved to `archive/windsurf-workflows/`; 27 files moved from `scripts/` to `scripts/archive/` with a README (`git mv`, nothing deleted). BLOCKED: moving the 9 root docs to `docs/archive/` needs the owner's agreement (OWNER QUESTION 4). `tools/mojmap_bulk_fix.py` left in place.
- [x] M0.11 CHANGELOG.md — created with an Unreleased section.

## POWER CAPABILITY STATUS (M2 onward)
- Not started. Audit-time facts only: `Technique.exclusives()` returns `List.of()` (technique/Technique.java:132-134); seven files read sneak state (see VERIFIED).

## DECISIONS MADE (mark each one DEFAULT or OWNER)
- Base tree: master (3189016) is the cursor/deslop rewrite tree, `backup/master-pre-cursor` (a205024) holds the previous master, `main` is untouched at a4bcc78. OWNER (chat, 2026-11-02). The program's audit numbers (384 files, 55 mixins) match this tree.
- Clone step: skipped, because the working copy already has the real history. DEFAULT.
- M0 branch name modern/m0-baseline. DEFAULT.
- Target game version: 26.3. DEFAULT, now checked: Mojang's version manifest lists `latest.release` = 26.3 (released 2026-09-15).
- JUnit: 5.14.4 via the junit-bom, test scope only. DEFAULT. JUnit 6.1.3 is now the newest line on Maven Central; the program says JUnit 5, so 5 was used. Revisit in M1.4.
- Dev server files in run-verify/: build.gradle left untouched (4.6). DEFAULT. For verify boots a throwaway `run-verify/server.properties` was created with the build script's contents plus `max-tick-time=-1` (as VERSION_X.md advises); run-verify/ is gitignored.
- JVM flags (`-XX:TieredStopAtLevel=1`, `-Xshare:off`, the `CompileCommand` exclude) kept in `runServer`, `runClient` and `serverInt`. DEFAULT: the crash the owner saw happens on player join, which no test here can exercise.
- Not changed: LICENSE, `license` and `authors` in fabric.mod.json, `fabric-api` range, Loom version. DEFAULT.

## OWNER QUESTIONS (open)
1. License: LICENSE is CC0-1.0, fabric.mod.json says MIT. Which one? — blocking (M0.4)
2. Author name for fabric.mod.json (currently "You")? — blocking (M0.4)
3. The program has a target (26.3) but no task that performs the 26.1 to 26.3 bump. Default if unanswered: make it the first task of M1, in its own commit, before M1.1. — not blocking M0
4. May the 9 root docs (ABILITY_CATALOG*.md, OBTAINING.md, PERFORMANCE_OPTIMIZATION_REPORT.md, PROJECT_STATUS.md, V4_DESIGN.md, V4_STATUS.md, V5_STATUS.md, VERSION_X.md) move to docs/archive/? — not blocking
5. Snapshot target for M9: the newest snapshot is 26.4-snapshot-6 (2026-10-27). Use it? — not blocking
6. Several owner docs describe systems that are not in the current tree (see KNOWN ISSUES). Archive them as stale, or keep? — not blocking
7. Can you test one player join on `./gradlew runServer` (JVM flags on) and, separately, with the flags off? Only a join can show whether the JIT crash you saw is still real. — not blocking

## VERIFIED
- Baseline build: `./gradlew build --console=plain` on unmodified 3189016, exit 0, 1m31s. Tasks run: compileJava, processResources, sourcesJar, jar, build; `:test` reported NO-SOURCE.
- Build with tests after the M0 changes: same command, exit 0, 43s, 3 tests, 0 failures.
- Counts (`git ls-files` and grep over src/main/java): 384 Java files, 128,703 lines, 55 mixin files, 120 resource files, 0 lang files, no src/test before M0.
- Static checks (M0.9): client references (`net.minecraft.client`, `Minecraft.getInstance`): 0. `System.out`/`System.err`: 101. `printStackTrace`: 17. Files using `currentTimeMillis`: 67. `catch (Exception|Throwable)`: 219. `Component.literal`: 13,965. Files importing `eu.pb4.sgui`: 114.
- Audit structure claims: PoliticalServer.java registers `CommandRegistrationCallback` at lines 163 and 436, and the single `END_SERVER_TICK` lambda at line 492. Root command literals registered twice: view, coins, bounty, hunt, judge, credits, startundergroundauction, placeauctionmaster (166 distinct root literals via `register(Commands.literal(...))`). Seven files read sneak state: EnderPhaseHandler, VoidwalkerHandler, items/ArmorAbilityHandler, items/CustomItemHandler, items/LegendaryToolHandler, magic/MagicSpellHandler, technique/TechniqueManager. Static collection fields: 597 by regex (audit said about 595).
- Metadata claims: fabric.mod.json had license MIT, authors ["You"], `fabric-api` "*"; LICENSE is CC0 1.0; no `*.java.backup` files; the 10 `.windsurf/workflows` files were all 0 bytes; scripts/ held 27 files; the tracked `.claude/settings.local.json` contained machine specific paths.
- Polymer: the audit said 2 files. Actual: 0 files import `eu.pb4.polymer`; PolymerBridge.java only checks `isModLoaded("polymer-core")` / `isModLoaded("polymer-resource-pack")` (lines 57-58), and 5 files mention Polymer in text. At runtime the log says `[Polymer] bridge ready. polymer=true models=0`.
- Content numbers: classes "over 380" is CONFIRMED (384 files, 598 type declarations by regex). Runtime recipes (boot 7 log): vanilla `Loaded 1515 recipes`; the mod logs `RecipeConfigManager crafting: OK | 296 recipes (0 failed)`, `SlayerRecipes Registered 296 total recipes`, `V4RecipeBootstrap Registered 1`, `LivingWorldRecipeBootstrap Registered 4`. Whether the last 5 are inside the 296 is not established.
- Target lookups (WebFetch, so summarized by a tool; check fabricmc.net/develop before the bump): Mojang manifest latest release 26.3 and latest snapshot 26.4-snapshot-6; Fabric meta lists 26.3 stable; Modrinth Fabric API 0.165.0+26.3 (release, 2026-10-23); fabric-example-mod master pins minecraft 26.3, loader 0.20.1, loom 1.19-SNAPSHOT, Fabric API 0.165.0+26.3, Gradle 9.8.2 (this repo: loader 0.18.5, loom 1.15-SNAPSHOT, Fabric API 0.144.3+26.1, Gradle 9.4.0).
- M0.7, what runServerVerify actually runs: its argument file `build/loom-cache/argFiles/runServerVerify` contains only `-classpath ...`, and the captured server JVM command line has no `-X` or `-XX` option. Cause: `serverVerify` in build.gradle calls `server()` and does not `inherit server`, so it never receives the three flags (`serverInt` does inherit).
- M0.7, boots without the flags (JDK 25.0.2, Minecraft 26.1, `./gradlew runServerVerify --console=plain`): 7 attempts. 6 reached `Done` and `PoliticalServer data loaded`. 1 failed with an IOException on the world lock because the server from the previous attempt was still running (my process handling, not a crash). No `hs_err_*.log` and no crash-reports directory. Boots 6 and 7 also ran `stop` (log: `Stopping the server`) and Gradle exited 0 (1m28s, 2m22s). Boot 6 also ran `list` (log: `There are 0 of a max of 20 players online`).
- M0.7, harness caveat: console commands piped into stdin at startup fail with `An unexpected error occurred trying to execute that command`. Cause established: `java -Xlog:exceptions` shows a `NullPointerException` in `Commands.executeCommandInContext` at bci 28, and `javap -c` shows bci 28 is `ServerLevel.getGameRules()` called on `CommandSourceStack.getLevel()`, which is null because the console source was built before the overworld existed. Sending the same commands after `PoliticalServer data loaded` works (boot 6). Not a mod defect.

## NOT VERIFIED
- Recipes (owner: 7216): NOT CONFIRMED. config/political/custom_recipes.json is `{"recipes": []}` (20 bytes) and HardcodedRecipes.java is not in the tree; the runtime log shows 296 (see VERIFIED).
- Block armors (owner: 144): NOT CONFIRMED. BlockArmor.java, BlockArmorHandler.java and the BlockGear files are not in the tree; only RecipeConfigManager, RecipeGenerator and RecipesGui still mention BlockArmor.
- Dragon chestplates (owner: 121): NOT CONFIRMED. Real value: 8, `DragonChestplateRegistry.IDS` (items/DragonChestplateRegistry.java:12-21).
- Item IDs (owner: about 2,090): NOT CONFIRMED. No static catalog exists and nothing in the boot log counts them.
- Non-snapshot Loom support for 26.3 was not tested.
- Target builds of sgui, placeholder-api, player-data-api and polymer for 26.3: not checked yet (M1.4).
- Whether the JVM flags are still needed: not settled. Untested: `runServer` with the flags, flags off on `runServer`/`runClient`, any player join, and repeated boots under load. One JIT crash is probabilistic, so a few clean boots are weak evidence.
- Cause of the growing main-thread stall (see KNOWN ISSUES): not investigated; the machine also had several Gradle daemons running.
- GitHub Actions status: `gh` is not authenticated on this machine (HTTP 401), so no CI run was checked.

## BEHAVIOR CHANGES
- None to gameplay. fabric.mod.json `description` text only.

## EARN-RATE CHANGES
- None.

## MIGRATIONS
- None yet.

## KNOWN ISSUES
- Docs describe a tree that no longer exists: master is the cursor rewrite, which removed Backrooms, Delves, block armor, Aether gear, Beastcraft and the generated recipe set. PROJECT_STATUS.md, V4_DESIGN.md, V4_STATUS.md, V5_STATUS.md, VERSION_X.md and OBTAINING.md refer to them: S3.
- Startup stalls the server thread and the stall grew on every restart of the same run-verify world. Log line `Can't keep up! ... Running Nms behind` after `Done`: boot 1 3,370 ms, boot 2 5,208, boot 4 6,929, boot 5 8,679, boot 6 8,905, boot 7 17,144, while custom_recipes.json stayed 1,188,357 bytes. The recipe bootstraps run on the server thread right after `Done`. Likely the same fault noted in commit a4bcc78 (a recipe file rewritten synchronously on the main thread): S2, planned for M3/M6. Could be partly machine load.
- Eight duplicate root command literals (listed under VERIFIED) and two `CommandRegistrationCallback` blocks: S2, PoliticalServer.java:163 and :436. Planned for M1.8.
- `Technique.exclusives()` returns an empty list while V4_DESIGN.md describes eight exclusive techniques: S3, technique/Technique.java:132.
- A single tick lambda runs every manager: S2, PoliticalServer.java:492. Planned for M1.5.
- build.gradle writes `eula=true` and `online-mode=false` into run/ and run-verify/: S4, intentional per rule 4.6, unchanged.
- `serverVerify` does not inherit the `server` JVM flags (see VERIFIED), so `runServerVerify` and `runServer` differ: S4.
- Boot log warns that a mixin config requests compatibility level JAVA_25 while Knot supports JAVA_22. This mod's two mixin configs both say JAVA_22 (political.mixins.json:4, politicalserver.mixins.json:4), so another jar requests it: S4.

## RESUME COMMANDS
Windows (PowerShell):
```
cd C:\Users\Arthur\Desktop\Economical-and-Political-Mod
git checkout modern/m0-baseline
.\gradlew.bat build --console=plain
.\gradlew.bat runServerVerify
```
Linux/macOS (bash):
```
cd Economical-and-Political-Mod
git checkout modern/m0-baseline
./gradlew build --console=plain
./gradlew runServerVerify
```
Run one Gradle task at a time. Verify-boot rules learned in session 1:
- Stop any leftover server JVM first (the process whose command line contains `KnotServer`), or the next boot fails on the world lock.
- Never send console commands until the log shows `PoliticalServer data loaded`; earlier ones fail with the null-level NPE above. After that, `stop` shuts the server down cleanly.
- run-verify/ is throwaway and gitignored; its server.properties carries `max-tick-time=-1`.

## HISTORY (append only)
- 2026-11-03 09:25 UTC Session 1: M0.1, M0.2, M0.3, M0.5, M0.8, M0.9, M0.11 done; M0.10 partial; M0.6 lookups done; M0.4 blocked on license and author; M0.7 started.
- 2026-11-03 10:30 UTC Session 1: M0.7 boots run (flags not applied to runServerVerify; 6 of 7 boots clean); console-command NPE traced to the test method; startup stall growth recorded; session stops at the license/author owner questions.
