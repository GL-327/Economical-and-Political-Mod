# Configuration reference

Generated from the code by `ConfigReferenceTest`. Do not edit by hand: change the key's description in
its settings class and run `./gradlew test -Dpolitical.updateDocs=true`.

The file is `config/political/config.json` in the server directory. It is created with every default the
first time the mod starts. Each dot in a path below is one level of nesting, so `tick.budget.server_ms` is
`{"tick": {"budget": {"server_ms": 5.0}}}`. A missing setting uses its default; a wrong type uses the
default; a number outside its range is clamped; unknown settings are logged and ignored. A broken file never
stops the server. Settings are read at startup.

## features

| Setting | Type | Default | Allowed | What it does |
| --- | --- | --- | --- | --- |
| `features.great_hunt` | boolean | `false` | true or false | The Great Hunt and the other living world events that spawn named custom mobs. |
| `features.incursions` | boolean | `false` | true or false | Incursions: rifts a player can open that spawn waves of custom mobs. |
| `features.nemesis` | boolean | `false` | true or false | The nemesis system: a mob that kills a player becomes a named grudge that hunts them. |
| `features.wandering_spirits` | boolean | `false` | true or false | World modifiers such as the Wandering Spirits, which add roaming custom mobs and weather style events. |
| `features.world_boss` | boolean | `false` | true or false | Timed world bosses that spawn in the overworld on their own. Admins can still spawn one by hand. |

## persistence

| Setting | Type | Default | Allowed | What it does |
| --- | --- | --- | --- | --- |
| `persistence.flush_interval_ticks` | int | `600` | 20 to 72000 | How often modified data stores are written to disk, in ticks (20 ticks = 1 second). Stores are also written when the server stops. |

## power

| Setting | Type | Default | Allowed | What it does |
| --- | --- | --- | --- | --- |
| `power.awakening.boss` | choice | `ender_dragon` | ender_dragon, wither, warden, none | The boss whose defeat awakens a player's technique. Until then the technique sleeps and cannot be used. Players near the boss when it dies are awakened. 'none' awakens everyone from the start. |
| `power.barrier.blocks_fall_damage` | boolean | `true` | true or false | Whether the Barrier also blocks fall damage. |
| `power.barrier.blocks_void_damage` | boolean | `false` | true or false | Whether the Barrier blocks void damage. Off: falling out of the world still kills, as nothing in the game can prevent it. |
| `power.barrier.cooldown_ticks` | int | `300` | 0 to 72000 | Cooldown of the Barrier, in ticks. |
| `power.barrier.cost` | int | `30` | 0 to 10000 | Resource cost of the Barrier. |
| `power.barrier.duration_ticks` | int | `40` | 1 to 1200 | How long the Barrier blocks incoming damage, in ticks. |
| `power.black_flash.chance` | double | `0.05` | 0.0 to 1.0 | Chance that a qualifying melee hit becomes a Black Flash. |
| `power.black_flash.min_tier` | int | `3` | 0 to 4 | Lowest school tier (0 to 4) at which Black Flash can happen. |
| `power.black_flash.multiplier` | double | `2.5` | 1.0 to 100.0 | Damage multiplier of a Black Flash. It replaces the critical hit multiplier; the two never stack. |
| `power.black_flash.window_ticks` | int | `20` | 1 to 1200 | A melee hit only qualifies this many ticks after the player's last technique cast. |
| `power.blast.cooldown_ticks` | int | `40` | 0 to 72000 | Cooldown of Cursed Blast, in ticks. |
| `power.blast.cost` | int | `25` | 0 to 10000 | Resource cost of Cursed Blast, an instant ray that hits the first living target. |
| `power.blast.damage` | double | `6.0` | 0.0 to 1000.0 | Damage of Cursed Blast. It uses a damage type that ignores armor. |
| `power.blast.range` | double | `24.0` | 1.0 to 128.0 | Reach of Cursed Blast, in blocks. The ray stops at the first solid block. |
| `power.blue.affects_bosses` | boolean | `false` | true or false | Whether Blue and Red can move bosses. Off by default. |
| `power.blue.cooldown_ticks` | int | `80` | 0 to 72000 | Cooldown of Blue, in ticks. |
| `power.blue.cost` | int | `15` | 0 to 10000 | Resource cost of Blue, which pulls a target toward the caster. |
| `power.blue.pull_per_tick` | double | `0.6` | 0.0 to 5.0 | Horizontal speed given to the target toward the caster, in blocks per tick. |
| `power.blue.range` | double | `10.0` | 1.0 to 128.0 | How far away Blue can pick its target, in blocks. |
| `power.blue.vertical_max` | double | `0.3` | -5.0 to 5.0 | Highest vertical speed Blue may give the target. |
| `power.blue.vertical_min` | double | `-0.2` | -5.0 to 5.0 | Lowest vertical speed Blue may give the target. |
| `power.cast.global_cooldown_ticks` | int | `10` | 0 to 200 | Ticks that must pass after any cast before the same player can cast again. |
| `power.cast.rate_limit_casts` | int | `8` | 1 to 100 | How many casts a player may make inside the rate limit window; the next one is refused with RATE_LIMITED. |
| `power.cast.rate_limit_window_ticks` | int | `20` | 1 to 1200 | Length of the rate limit window, in ticks. |
| `power.domain.cooldown_ticks` | int | `2400` | 0 to 720000 | Cooldown that starts when the Domain ends, in ticks. |
| `power.domain.cost` | int | `150` | 0 to 100000 | Resource cost of a Domain, available from a school capstone. |
| `power.domain.damage_bonus` | double | `0.25` | 0.0 to 10.0 | Extra damage the caster's melee deals inside the Domain (0.25 is +25 percent). Melee also ignores armor and the Defense stat there. |
| `power.domain.duration_ticks` | int | `600` | 20 to 72000 | How long a Domain lasts, in ticks. |
| `power.domain.min_separation` | double | `24.0` | 0.0 to 512.0 | A Domain cannot be cast if another one in the same dimension is this close or closer, in blocks (exactly this far is refused). |
| `power.domain.radius` | double | `12.0` | 1.0 to 128.0 | Radius of a Domain around its caster, in blocks. |
| `power.enabled` | boolean | `true` | true or false | Master switch for the power system. When off, no technique can be cast (the cast is refused with SYSTEM_DISABLED). |
| `power.mastery.cooldown_per_rank` | double | `0.03` | 0.0 to 0.3 | Share of every cooldown taken off for every mastery rank above Novice (0.03 is 3 percent per rank). |
| `power.mastery.damage_per_rank` | double | `0.05` | 0.0 to 1.0 | Extra technique damage for every mastery rank above Novice (0.05 is +5 percent per rank). |
| `power.progression.shards_per_level` | int | `3` | 1 to 1000 | Arcane shards earned in total for each power level (level = 1 + shards earned / this, at most 50). School tree nodes need a level. Shards still only come from expedition rewards, so this changes how fast levels follow, not how many shards exist. |
| `power.red.cooldown_ticks` | int | `100` | 0 to 72000 | Cooldown of Red, in ticks. |
| `power.red.cost` | int | `20` | 0 to 10000 | Resource cost of Red, which pushes a target away from the caster. |
| `power.red.damage` | double | `0.0` | 0.0 to 1000.0 | Damage Red deals on top of the push. Zero by default. |
| `power.red.lift` | double | `0.4` | 0.0 to 5.0 | Upward speed Red adds to the target. |
| `power.red.push_per_tick` | double | `1.4` | 0.0 to 5.0 | Horizontal speed given to the target away from the caster, in blocks per tick. |
| `power.red.range` | double | `8.0` | 1.0 to 128.0 | How far away Red can pick its target, in blocks. |
| `power.reroll.awakened_multiplier` | double | `2.0` | 1.0 to 100.0 | The price of a shard reroll is multiplied by this once the technique is awakened. |
| `power.reroll.charge_boss` | choice | `wither` | wither, warden, ender_dragon, none | The boss that gives a free reroll charge to the players near it when it dies, up to the maximum below. 'none' turns this source off. |
| `power.reroll.max_charges` | int | `3` | 0 to 100 | Most free reroll charges a player can hold. |
| `power.reroll.shard_base` | int | `10` | 0 to 10000 | Arcane Shards a first random reroll costs (before the awakened multiplier). |
| `power.reroll.shard_max` | int | `100` | 1 to 100000 | The price of a shard reroll never goes above this. |
| `power.reroll.shard_step` | int | `5` | 0 to 10000 | How much more every earlier reroll adds to the price. |
| `power.resource.base_max` | int | `100` | 1 to 100000 | Maximum of the cursed resource (the mana pool) for a player with no school tier, before bonuses from stats, vows and gear. |
| `power.resource.death_keep_fraction` | double | `0.5` | 0.0 to 1.0 | Fraction of the current resource kept when the player dies (0.5 halves it). |
| `power.resource.max_per_tier` | int | `25` | 0 to 10000 | How much the maximum grows for every school tier the player has reached (tier 0 to 4). |
| `power.resource.regen_delay_ticks` | int | `60` | 0 to 1200 | Ticks after spending the resource before it starts to come back. |
| `power.resource.regen_per_second` | double | `4.0` | 0.0 to 1000.0 | Resource regained per second once regeneration has started. Regeneration never goes above the maximum. |
| `power.reverse.cooldown_ticks` | int | `200` | 0 to 72000 | Cooldown of Reverse, in ticks. |
| `power.reverse.cost_per_hp` | double | `2.0` | 0.0 to 1000.0 | Resource charged for every point of health Reverse actually restores. |
| `power.reverse.max_heal` | double | `8.0` | 0.0 to 1000.0 | Most health Reverse can restore in one cast, in half-hearts (2 = one heart). |
| `power.reverse.range` | double | `6.0` | 1.0 to 128.0 | How far away Reverse can reach another player, in blocks. |
| `power.unique.barrier` | boolean | `false` | true or false | Only one player at a time can hold the Barrier. Off by default. |
| `power.unique.blue` | boolean | `false` | true or false | Only one player at a time can hold Blue. Off by default. |
| `power.unique.cursed_blast` | boolean | `false` | true or false | Only one player at a time can hold Cursed Blast. Off by default. |
| `power.unique.domain` | boolean | `false` | true or false | Only one player at a time can hold Domain. Off by default. |
| `power.unique.red` | boolean | `false` | true or false | Only one player at a time can hold Red. Off by default. |
| `power.unique.reverse` | boolean | `false` | true or false | Only one player at a time can hold Reverse. Off by default. |
| `power.vow.check_interval_ticks` | int | `20` | 1 to 1200 | How often vows are checked while a player plays, in ticks. |
| `power.vow.max_active` | int | `2` | 0 to 3 | How many binding vows a player can hold at once. |
| `power.vow.no_armor_resource_bonus` | int | `20` | 0 to 10000 | Maximum resource gained from the no-armor vow. |
| `power.vow.no_offhand_cooldown_reduction` | double | `0.1` | 0.0 to 0.9 | Share of every cooldown removed by the no-offhand vow. |
| `power.vow.no_sprint_damage_bonus` | double | `0.1` | 0.0 to 10.0 | Extra technique damage from the no-sprint vow (0.10 is +10 percent). |
| `power.vow.reaccept_cooldown_ticks` | int | `6000` | 0 to 720000 | Ticks a player must wait to take a vow again after breaking one. |

## tick

| Setting | Type | Default | Allowed | What it does |
| --- | --- | --- | --- | --- |
| `tick.budget.player_ms` | double | `10.0` | 0.1 to 1000.0 | The same limit for a per-player tick service, counted over all players in that tick. |
| `tick.budget.server_ms` | double | `5.0` | 0.1 to 1000.0 | How long one server or world tick service may take in a single tick before the server logs it by name. Nothing is cut short; this only decides when a line is logged (at most once a minute per service). |
