# Owner power test (M2.8)

This is the owner's test script for the power system (program section 15, items 1 to 12), written out so it can be
followed on a test server with a vanilla client. M2 is not done until every item below is marked PASS by the owner.
"Already checked by the smoke run" says what a server-side run with fake players has already shown for that item, so
the owner only needs to look at what a client can show (action bar text, particles, sounds, how a screen looks).

The smoke run is not a substitute: it has no client, so it cannot show text, effects or screen layout, and it calls
the same code paths a click or a command would reach, not the clicks themselves.

## Setup (test server only)

1. Start the server, join with a vanilla 26.3 client, and make yourself an operator (`op <name>` in the console).
2. To get techniques without a source of Arcane Shards, use the admin screen (Admin Panel, "Techniques", then your
   name, "Grant Technique") or the console command `poweradmin grant <name> BLUE`.
3. To test the real tree path: stop the server, open `power.json` in the world folder, and for your UUID set
   `"shards": 200` and `"shards_earned_total": 200`; start the server again. 200 is enough for the whole spine of one
   school (138 shards, level 50 needs 147 earned). Test server only: shards normally come only from expeditions.
4. Where things are: the player screen is `/menu`, then "Techniques" (it opens the Powers hub). Casting is: empty hand,
   sneak, right-click in the air, with a technique equipped and an ability selected.
5. Config values are in `config/political/config.json` under `power.*` (all listed in `docs/CONFIG.md`). The settings are
   read at startup.

## The script

| # | Do | Expect | Already checked by the smoke run |
| ---: | --- | --- | --- |
| 1 | Join with a vanilla client. Choose a school in the Powers hub ("School"), then buy tree nodes ("School Tree") until a technique appears under "Techniques". | The hub opens, the choice asks for confirmation, a bought node shows a message, and the technique shows as Known. | School choice, a second choice refused, nodes bought in order, tier 4 after node 10, techniques granted at their nodes, every screen builds without error. NOT checked: how it looks, clicking. |
| 2 | Cast Blue at a zombie 9 blocks away (grant Blue, equip it in "Techniques", look at the zombie, sneak and right-click with an empty hand). | The zombie is pulled toward you. Your cursed energy drops by 15 (see the bar in the hub). The action bar shows the ability name and the cost. | Blue at a mob 9 blocks away: pulled toward the caster, 15 spent. NOT checked: the action bar text, particles, sound. |
| 3 | Cast Blue again at once. | The action bar says "On cooldown: N s left." Nothing is spent. | A second Blue inside its cooldown is ON_COOLDOWN with time left, and nothing is spent. |
| 4 | Stand 5 blocks from a mob with a stone wall between you and cast Red. | The action bar says "Something is in the way." Nothing is spent. Without the wall Red pushes the mob away and costs 20. | Red behind a stone block: NO_LINE_OF_SIGHT, nothing spent; without it, pushed away and 20 spent. |
| 5 | At low health, cast Reverse on yourself (look up so no mob is in the crosshair). | You heal at most 8, it costs 2 energy per health healed, and you never go above full health. A mob under the crosshair is refused. | Heal and cost match, cap 8, no overheal, a mob refused, on cooldown does nothing. |
| 6 | Open "Binding Vows". Take "No armor" while wearing armor, then take armor off and take it, then put armor on. | The first is refused with a message, the second is accepted (maximum energy +20), and putting armor on shows "Your vow was broken" and the bonus is gone. | The refusal, the acceptance, the +20, the break on armor, the bonus gone, the 6000 tick wait, and the limit of two. NOT checked: the messages as shown. |
| 7 | Die. | Energy is halved (rounded down), vows are cleared, cooldowns are cleared. | All three, with a vow and a cooldown active before death. |
| 8 | Log out and back in, then restart the server. | Techniques, school, nodes, shards, energy and vows are as you left them. Cooldowns and a running Domain are gone (they are not saved). | NOT checked: only the file format round trip (unit tests) and that `power.json` is written on shutdown and read on start. Do this by hand. |
| 9 | Run each admin command as an operator and as a non-operator: `poweradmin`, `poweradmin info <p>`, `poweradmin grant <p> <technique>`, `poweradmin revoke <p> <technique>`, `poweradmin school <p> <school>`, `poweradmin reset <p> confirm`. Also open the admin screen (Admin Panel, "Techniques"). | Operators succeed. A non-operator is denied: the command does not exist for them, and the screen says they have no permission. | All six command shapes parse for an operator and for a player with the permission, and do not for a player without it; the permission node is `politicalserver:power.admin`. NOT checked: executing them with a real target. |
| 10 | With a second player: they cannot read or change your energy or vows. Turn PvP off (`gamerule pvp false`) and try to cast Blue at them. | There is no screen or command for a player to read another player's state (only the admin tools). The cast is refused ("That is not a valid target.") with PvP off and works with PvP on. | PvP off refuses the cast at another player, PvP on approves it, and each player's pool is their own. |
| 11 | Reach the capstone (node 10) or grant Domain, and cast it. A second player stands inside, and another casts a Domain within 24 blocks. | The second player's techniques fail inside ("A Domain is suppressing your techniques."). A Domain within 24 blocks of yours is refused ("Another Domain is too close."). Yours ends after 600 ticks (30 s) with a message. Your melee hits inside deal 25 percent more and ignore armor. | Suppression inside, overlap at 18 blocks refused, a caster outside may cast, strike damage exactly 8 x 1.25, the Domain still stands at 599 ticks and ends at 600, the cooldown runs, and a dimension change ends it. NOT checked: the sphere's particles and the title. |
| 12 | Test server only: set `power.black_flash.chance` to 1.0 in the config, restart, take a tier 3 or higher school, cast anything, and hit a mob within 1 second. Then restore 0.05. | The hit deals 2.5 times the damage ("BLACK FLASH!" on the action bar). It does not stack with a critical hit. | At the default 5 percent a Black Flash happened within 400 tries and dealt exactly 2.5 times; none happens outside the 20 tick window. NOT checked: chance 1.0, the message, the interplay with a real critical hit. |

13. Regression (after every milestone): stats, relics, bounties, bazaar, one election, one settlement registration and one
    expedition, each in its normal case. NOT RUN. The power change touches the damage mixin (`BountyDamageMixin`), the
    use-item handler (`CustomItemHandler`), the death, join, logout and dimension hooks, and the mana and shard methods the
    old magic code still calls, so check bounties, slayer hits and expedition shard rewards in particular.

## Results

Fill this in as you go.

| # | Result (PASS or FAIL) | Notes |
| ---: | --- | --- |
| 1 | | |
| 2 | | |
| 3 | | |
| 4 | | |
| 5 | | |
| 6 | | |
| 7 | | |
| 8 | | |
| 9 | | |
| 10 | | |
| 11 | | |
| 12 | | |
| 13 | | |

## How to run the smoke run

It is a development check, off by default. Start the dev server with the system property and stop it after the
result line appears:

```
JAVA_TOOL_OPTIONS="-Dpoliticalserver.powerSmoke=true" ./gradlew runServerVerify
```

Wait for `Power smoke check passed: N checks` (or `FAILED` with the list of failed checks), then type `stop` in the
console. It runs a few seconds after startup, uses two Fabric fake players, a zombie high above the world spawn, and
a stone block, restores the PvP rule, and removes the fake players' state (a reset leaves one archived copy per fake
player in the `quarantine` of `power.json` of that development world). It forces the spawn chunk while it runs and lets
it go afterwards. Never start a real server with the property.
