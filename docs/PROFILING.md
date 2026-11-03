# Profiling the server with spark

spark is a profiler for Minecraft servers. It is **not** a dependency of this mod and is not bundled in
its jar: it is a separate mod that you drop into a server when you want to find out what is slow, then
remove again. This is the plan from M1.4 and M8.1 of the modernization program.

Status of this page: written from spark's published usage in M1.4. The commands below were **not run**
in the build environment (no player or long-running server session there), so treat them as the
documented starting point and check them against spark's own docs at <https://spark.lucko.me/docs>.

## Get it

1. Download the Fabric build of spark for your Minecraft version from Modrinth
   (<https://modrinth.com/mod/spark>). At the time of M1.4 the newest Fabric build for Minecraft 26.3 was
   `1.10.187-fabric`, published 2026-09-19.
2. Put the jar in the server's `mods` folder next to `politicalserver-*.jar` and restart the server.
3. When you are done, delete the jar and restart. Nothing in this mod depends on it.

## The usual routine

Run these from the server console or as an operator in game:

| Goal | Command |
| --- | --- |
| Is the server keeping up? | `/spark tps` |
| One-page overview of CPU, memory and tick times | `/spark healthreport` |
| Where does tick time go? | `/spark profiler start --timeout 300` then `/spark profiler stop` (or wait for the timeout) |

`/spark profiler stop` prints a link to a report. The profile data is uploaded to spark's public viewer
service to produce that link, so do not profile on a server whose player names or world details you
cannot share.

## What to record

M8.1 asks for the top tick costs before and after the optimisation work. A useful record is:

- the date, the Minecraft and mod versions, the player count during the run;
- the 3 to 5 methods under `com.political` with the highest share of the server thread, from the
  report's call tree;
- the `Can't keep up!` lines from `logs/latest.log` over the same window.

Put the result in `HANDOFF.md` under VERIFIED with the exact command used.
