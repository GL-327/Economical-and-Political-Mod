# V5 — "The Living Realm"

Build **GREEN** · boots in 0.6s · **2175 recipes, 0 failed** · every system playable by clicking.

V4 is complete and fully GUI-driven. V5 adds four new pillars on top of it.

---

## V4 completion — everything is now a GUI

Nothing in this mod requires typing a command. `/civ` opens the **Civilization Hub**, which links
every system; it is also a glowing button on the existing Player Hub.

| GUI | What you can do |
|---|---|
| **Civilization Hub** | one screen to reach all 14 systems |
| **War Room** | target picker, vote FOR/AGAINST, sue for peace, council & shadow views |
| **Balance of Power** | shows visually that Chair 5 + Vice 4 = 9 beats a full shadow bench (8) |
| **Settlement Economy** | production, workforce, growth-gate checklist, appeal breakdown |
| **Government** | office panels gated to their holder, perks, parties, taxation |
| **High Council** | enact/repeal edicts by clicking, ministries, council log |
| **Backrooms** | status, entry explanation, field guide, leave |
| **Stats** | full stat sheet + where each number comes from, action-bar toggle |

Chat output is preserved as explicit subcommands (`/war status`, `/council status`, `/stats text`).

---

## V5.0 — Expeditions

Instanced procedural dungeons in a dedicated "expedition lane" (16 concurrent slots that can never
overlap). Deterministic 3×3 room grids per floor: entry hall, combat rooms, lever puzzles, treasure
vaults, boss arena. **Five tiers** (Delve → Apotheosis) with floors, time limits, mob packs and
`HealthScalingManager` tiers.

- Higher tiers **require a City+ settlement** to launch from.
- Completion pays coins, **Arcane Shards** (magic tie-in) and settlement treasury.
- Vault chests are stamped empty and only filled once their guards die — you cannot run past them.
- Instances are wiped on every exit path, including server shutdown, so the world never fills with
  abandoned dungeons.
- **Balance**: entry 500→25,000 coins; net reward 2,500→35,000. T5 is ~12× the difficulty for 20×
  the coins — deliberately sub-linear so grinding low tiers stays competitive.

## V5.1 — Guilds & Orders

Cross-settlement player organizations, distinct from national parties and territorial settlements.
Ranks (Master → Initiate), guild treasury with rank-gated withdrawals, guild XP and levels, an
11-perk tree, and a **guild hall** declared at a Town+ settlement.

- **Balance**: founding costs 250,000 coins **plus a crafted Guild Charter** — a coin sink *and* a
  materials sink. Full perk tree ≈18.5M treasury, spread across levels 2–15 so it cannot be bought
  out at once. Rewards are deliberately thin (full coin tree = +5%). Guild XP is rate-limited and
  daily-capped per player, so it cannot be AFK-farmed.

## V5.2 — Caravans & Trade

Settlements form **trade routes** with supply/demand pricing. Physical caravan NPCs travel the route
and can be **ambushed** — especially during a war between the two polities.

- Routes feed the **same** `SettlementEconomy` cycle and obey the wartime embargo.
- **Balance (verified in code, not just claimed)**: `MAX_DELIVERY_VALUE = 1200` hard-caps any single
  delivery; upkeep is charged from both treasuries every cycle whether or not goods moved; a
  treasury that cannot pay has its route **auto-suspended**; war suspends routes outright.
  Establishment costs 25k–250k plus a crafted Caravan Charter.

## V5.3 — The Living World

- **Seasons**: a four-season cycle that modulates crop growth and settlement **food production** —
  wired into `SettlementEconomy` so winter granaries really are thinner.
- **Bestiary**: per-player kill tracking with milestones at 10/50/250/1000 granting small permanent
  bonuses against that mob family, capped around +15% so it supplements gear rather than replacing it.
- **Great Hunt**: a rotating named rare beast; first to fell it takes the purse and a trophy.
  Purse 200k rising to a 400k ceiling on a 3-hour cooldown, so worst case ~640k enters the economy
  per hunt.

---

## Integration notes

- All four pillars tick from the existing `END_SERVER_TICK` fan-out and persist to their own
  world-root JSON files.
- Two new mixins: `BestiaryDamageMixin`, `SeasonCropGrowthMixin`.
- Guild XP hooks into the existing mob-kill event; season food multiplier is a one-line call inside
  `SettlementEconomy.runCycle`.
- Everything craftable is registered through `RecipeConfigManager`, so it appears in **both**
  `/recipe` and `/setrecipe`. Drop-only items (Expedition Keys, expedition gear) are documented in
  each pillar's obtainment list.

## Known judgement calls

- Expedition instances live in a far region of the End. If you already build at extreme coordinates,
  move the lane constant in `ExpeditionGenerator`.
- Bestiary/season/hunt tuning is unplaytested; all constants sit at the top of their managers.
- The agent-suggested optional guild-perk hooks (coin multiplier in `CoinManager`, mastery XP in
  `MasteryManager`) are **not** wired — the guild system is fully playable without them, but those
  two specific perks are inert until hooked. Deliberately left out to avoid touching hot economy
  paths without playtesting.
