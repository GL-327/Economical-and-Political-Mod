# Server tick baseline (M8, development machine, one client)

Taken from the shutdown report of the dev server during the full real-client run on 2026-11-05 (5,630 ticks, about 4.7 minutes, one player, a world that was already loaded, the whole
power test running: casts, a Domain, combat, deaths). It is a baseline for comparing later runs, not a load test: one player, a laptop with an integrated GPU sharing the CPU with the
test client, a cold JIT.

The 72 named tick services (`core.tick`) report their runs, average and maximum milliseconds and how many calls went over their budget (a placeholder of 5 ms for server services, 10 ms
for per-player ones). The report lists the eight that used the most time:

| Service | Every | Runs | Avg ms | Max ms | Over budget |
| --- | ---: | ---: | ---: | ---: | ---: |
| scoreboard | 1 | 5630 | 0.083 | 13.119 | 6 |
| armor-abilities (per player) | 1 | 5630 | 0.033 | 11.354 | 2 |
| slayer-armor-attributes (per player) | 1 | 5630 | 0.023 | 6.589 | 0 |
| settlements | 1 | 5630 | 0.021 | 7.877 | 1 |
| persistence-flush | 600 | 9 | 12.421 | 16.616 | 8 |
| bounty-armor | 1 | 5630 | 0.014 | 1.385 | 0 |
| overworld-clock-perks | 1 | 5630 | 0.012 | 1.393 | 0 |
| player-stats (per player) | 1 | 5630 | 0.012 | 22.666 | 1 |

Sixteen overrun lines were logged in the whole run: persistence-flush 4, scoreboard 2, playtime-buff-sync 2, and one each for titles, stock-market-gui, settlements, player-stats,
menu-trail, crypto-market-gui and bounty-spawns (the rest of the first-call spikes after boot). No service failed.

What it says:
- The sum of the every-tick services is about 0.2 ms per tick of the 50 ms tick budget: the mod's regular tick work is small on this machine with one player. It scales with players for the
  per-player services and with settlements for `settlements`; that has not been measured here.
- `persistence-flush` costs 12 ms every 30 seconds on the server thread (over its 5 ms budget 8 times in 9). It writes every dirty store. It is the one steady cost worth looking at
  (write on a background thread, or split the stores); not changed in this milestone.
- The maximum values (11 to 23 ms) are single calls, most of them right after boot or a world event, not a trend.
- `menu-trail`, added in this session, is not in the top eight.

Not measured: more than one player, a long run (hours), memory, chunk work, the cost of the world bosses, nemesis and incursions that are now off by default. Budgets are still
placeholders. To take the same numbers again: run `scripts/power-client-test.sh` or any dev server and stop it with `stop`; the report is in the log.
