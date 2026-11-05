# Snapshot port: Minecraft 26.4-snapshot-7 (M9)

Branch `snapshot/26.4-snapshot-7`, cut from `modern/m3-economy` on 2026-11-05. Not for merging: it exists to show what the next
Minecraft version asks of this mod and to be redone when the libraries catch up.

## What was changed
- `gradle.properties`: `minecraft_version=26.4-snapshot-7`, `fabric_api_version=0.165.4+26.4` (Fabric API has 26.4 builds; loader 0.20.1 and
  Loom 1.19.3 accepted the snapshot unchanged).
- `fabric.mod.json`: `minecraft` range widened to `>=26.4-alpha.1` (the snapshot reports itself as 26.4-alpha.7).
- Four source renames, all sounds and particles that 26.4 moved:
  - `ParticleTypes.ITEM_SNOWBALL` no longer exists (two uses in `MagicSpellHandler`, replaced by `SNOWFLAKE`; a small visual change in code that
    is not reachable in 26.3 either);
  - `SoundEvents.GLASS_BREAK` and `SoundEvents.AZALEA_PLACE` became block sound collections (`SoundEvents.GLASS.breakSound()`,
    `SoundEvents.AZALEA.placeSound()`, both `Optional<Holder<SoundEvent>>`); three uses in `MagicSpellHandler`, `VfxPlus`, `LegacyEffects`.

## Result (what was run)
- `./gradlew compileJava`: BUILD SUCCESSFUL after the four renames (five compile errors before).
- `./gradlew build`: BUILD SUCCESSFUL, 228 tests pass.
- `./gradlew runServerVerify`: **fails at boot**, before the mod loads: Polymer core 0.18.6+26.3 cannot apply a mixin
  (`ByteBufCodecsEntriesMixin`: `@ModifyReturnValue` finds no target `idMapper(IdMap)` in `ByteBufCodecs`), so Minecraft crashes in
  `BrigadierExceptions.<clinit>`. That is the library, not this mod's code.

## Blocked on
No 26.4 build exists yet (checked 2026-11-05 on the Nucleoid maven) for `eu.pb4:polymer-*` (latest 0.18.7+26.3), `eu.pb4:sgui` (latest 2.2.1+26.3)
or `xyz.nucleoid:server-translations-api` (latest 3.2.0+26.3). Fabric API has `0.165.4+26.4`.

## To redo later
1. When polymer, sgui and server-translations publish 26.4 builds, bump the three versions in `gradle.properties`.
2. Apply the four renames above (check `javap` of the new `SoundEvents` and `ParticleTypes` first; later snapshots may rename more).
3. Run build, then `runServerVerify` (wait for `PoliticalServer data loaded`, then `stop`), then the real-client test.
