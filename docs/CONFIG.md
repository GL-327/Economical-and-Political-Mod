# Configuration reference

Generated from the code by `ConfigReferenceTest`. Do not edit by hand: change the key's description in
its settings class and run `./gradlew test -Dpolitical.updateDocs=true`.

The file is `config/political/config.json` in the server directory. It is created with every default the
first time the mod starts. Each dot in a path below is one level of nesting, so `tick.budget.server_ms` is
`{"tick": {"budget": {"server_ms": 5.0}}}`. A missing setting uses its default; a wrong type uses the
default; a number outside its range is clamped; unknown settings are logged and ignored. A broken file never
stops the server. Settings are read at startup.

## persistence

| Setting | Type | Default | Allowed | What it does |
| --- | --- | --- | --- | --- |
| `persistence.flush_interval_ticks` | int | `600` | 20 to 72000 | How often modified data stores are written to disk, in ticks (20 ticks = 1 second). Stores are also written when the server stops. |

## tick

| Setting | Type | Default | Allowed | What it does |
| --- | --- | --- | --- | --- |
| `tick.budget.player_ms` | double | `10.0` | 0.1 to 1000.0 | The same limit for a per-player tick service, counted over all players in that tick. |
| `tick.budget.server_ms` | double | `5.0` | 0.1 to 1000.0 | How long one server or world tick service may take in a single tick before the server logs it by name. Nothing is cut short; this only decides when a line is logged (at most once a minute per service). |
