# V4 — The Magic Update & Politics Overhaul (Design)

Target: MC 26.1, Fabric, server-side only. All content = vanilla items/blocks/entities dressed
with components, names, tags. GUIs = sgui SimpleGui. Persistence = world-root JSON files or
DataManager.SaveData. This doc is the implementation contract for V4.

## 1. Settlements — "The Cornerstone"

**The Cornerstone** (item: Bell + custom_item_id `settlement_cornerstone`) replaces the village
bell as the civic heart. Placing it founds a settlement at that position (still rings as a bell).

- **Scan** (SettlementScanner, throttled ~1 settlement/10s, radius scales with tier 48→128):
  beds (BedBlock HEAD parts only), job sites, storage, light safety, and **Appeal** =
  weighted rare/deco blocks (beacon 50, conduit 40, netherite 30, diamond block 10, emerald 8,
  gold 5, amethyst cluster 4, sea lantern 3, froglights 3, lanterns/campfires/banners/flowers 1)
  × variety multiplier for distinct deco types.
- **Tiers** (auto up/downgrade on rescan):
  OUTPOST(0) → HAMLET(3 beds) → VILLAGE(8 beds, 5 pop) → TOWN(20 beds, 15 pop, 50 appeal)
  → CITY(40 beds, 30 pop, 150 appeal, 1+ registered player) → METROPOLIS(80 beds, 60 pop,
  400 appeal, 100k treasury) → MEGALOPOLIS(150 beds, 120 pop, 1 000 appeal, 1M treasury)
  → UTOPIA(250 beds, 200 pop, 2 500 appeal, 10M treasury, a Wonder = full beacon pyramid)
  → **ELYSIUM** (mythic: 400 pop, 6 000 appeal, 100M treasury, 3 Wonders — grants weak
  server-wide regen/luck aura while it stands).
  Vanilla villages ≈ barely VILLAGE; small ones = OUTPOST (calibrated by bed thresholds).
- **Citizens** (CitizenManager): villager NPCs, tags `political_citizen` + `political_stl_<id>`,
  generated names, jobs (Sheriff, Deputy, Tax Collector, Innkeeper, Blacksmith, Scholar,
  Priest, Merchant, Courier — Sheriff outranks Deputy outranks the rest). Wander-in spawning:
  periodic roll per settlement ∝ appeal × free beds; traders visit temporarily. Citizens have
  fixed random personality (party lean, tax tolerance, appeal sensitivity) used for voting.
- **Players register** to one settlement: `/settlement register` — fee by tier
  (VILLAGE free, TOWN 1k, CITY 10k, METRO 50k, MEGA 250k, UTOPIA 1M coins).
- Persistence: `settlements.json` (world root). Commands: `/settlement` (info GUI),
  `/settlement map`, `/settlement register|leave|rename|setspawn`, admin subcommands.

## 2. Politics overhaul — NPC democracy

- **City elections**: each CITY elects a **Constituent** (city-wide perks: minor buffs inside
  city radius, small tax skim). Candidates = registered players + notable NPC citizens.
  NPC citizens vote via personality model (appeal satisfaction, tax burden, party lean,
  candidate charisma hash, incumbent record, noise). Term 3 days.
- **Metropolis+ elections**: elect **Chair + Vice Chair** (winner/runner-up). The largest
  metropolis's election drives the EXISTING global Chair/Vice roles (all current perk plumbing
  keeps working — PerkManager, ChairGui, prefixes). Sub-metropolis settlements follow their
  NEAREST metropolis (their citizens vote in its election).
- **Utopia+ high government**: the **High Council** — Prime Seat (elected) + 4 Ministries
  (Treasury, Defense, Arcana, Commerce) appointed by the Prime Seat from players or senior NPC
  citizens. Council enacts **Edicts** (server-wide laws w/ costs + cooldowns): e.g. Prosperity
  Edict (+shop sell), Arcane Sanction (+mana regen), Conscription (citizens fight hostiles),
  Golden Age (2× appeal growth). ELYSIUM unlocks the **Radiant Mandate** edict tier.
- **Below city**: no elected leader; Sheriff NPC (auto-appointed, strongest personality) keeps
  order (attacks hostiles near cornerstone), other jobs give settlement mini-bonuses.
- **World map GUI** (`/settlement map` + hub button): 9×5 pane grid, each pane = one known
  settlement plotted by relative world position (N=up); banner color by tier; hover = name,
  tier, population, appeal, leader(s), treasury; footer row = live leaders feed (global
  Chair/Vice, each metro's leaders, High Council).
- **Legacy bug fixes shipped with V4**: dictator credit tax actually collected; emergency
  elections get candidates; ElectionManager.getAndClearExtraDrops NPE; corruption penalty made
  reachable (persist + check before reset).

## 3. Magic V4 — Techniques (JJK-style) + School Trees

- **8 Schools**, each a HotM-style tree (14 nodes: 10-tier spine + 4 side nodes; costs in
  magic XP + level gates; string ids `pyr1..`, `fro1..`, etc.): Pyromancy, Frostweaving,
  Stormcalling, Voidwalking, Bloodbinding, Verdancy, Radiance, Gravitas.
  Nodes grant: school spells (reusing + extending the 33-spell engine — the 17 orphaned spells
  get nodes at last), passives (school damage %, mana, cooldown), and **Awakened** capstones.
- **Techniques** (TechniqueManager): every player **awakens** exactly one Technique at Magic
  level 5 via the *Rite of Awakening* (consumes an Awakening Catalyst item). Roll table:
  - 10 **common techniques** (anyone can roll): Emberheart, Glacier Soul, Storm Vein,
    Umbral Step, Crimson Pact, Wildgrowth, Dawnforged, Ironmarrow, Tidebound, Whisperwind.
  - 8 **EXCLUSIVE techniques** — server-unique, only ONE holder each, held until death-forfeit
    or /technique renounce: **Hollow Infinity** (untouchable barrier windows), **Shadow
    Monarch** (undead thralls), **Chronoshear** (echo/rewind), **Starfall Sovereign**
    (meteor barrages), **Threadweaver** (tether puppetry), **Abyssal Maw** (devour/consume),
    **Mirror Sovereign** (copy last spell cast against you), **Worldroot Avatar** (terrain
    symbiosis). Exclusive roll chance 5%, weighted to unclaimed ones.
  - Mastery: technique XP from using abilities; ranks Novice→Adept→Expert→Master→Transcendent
    (each rank unlocks the next of 4-5 abilities and upgrades numbers). Abilities cast via
    staff sneak-right-click radial (sneak cycles selected ability; right-click casts).
- **Magic gear**: school robes (dyed leather, set bonus = school affinity), Catalyst off-hand
  items (spell power/cooldown), Grimoires (consumable tree XP), Technique Relics (per-technique
  passive trinkets), Archmage set (endgame). All with recipes + stat lines (multiples of 5).
- **Existing-magic fixes**: 17 orphaned spells get nodes; MagicGui renders 8/8 nodes; capstones
  reachable; separate spend-pool bookkeeping documented; /magic stats counts corrected.

## 4. Item catalog completion (~2,090 ids)

- **121 dragon chestplates all unique**: registry overriding the (index-1)%15 cycle with 95
  hand-assigned distinct ability kits (archetype × params × particle × sound), names/lore synced.
- **Legendary gaps**: crystal_shove typo fixed, AETHER_TOOLS added (incl. shovel/hoe abilities).
- **Flagship stubs implemented**: voidwalker_crown (Void Step blink, phase-dodge, dark bonus),
  ender_phase_helmet (Ender Phase mode), venomous_dagger (venom stacks), garden_heart_overalls
  (real crop-drop bonus t1-10).
- **Stat lines everywhere**: deterministic per-id stat lore (⚔ Damage / ❈ Defense / ❤ Health /
  ❁ Strength / ☣☠ crits) in **multiples of 5** (values <10 allowed as-is), injected at the
  CustomItemLore choke point so every custom item feeds the V3 stats engine; RPG flavor line
  per item family. Lore must describe the REAL ability the handlers give the item.

## 5. Crafting fixes

- resolveCustomItem gains mappings for enchanted_* / compacted_* / super_compacted_* /
  gilded_* (+ reflection over SlayerItems) → the 69/71 failing recipes load.
- RecipeEditorGui preserves lockedSlots when re-editing; /recipe Autocraft enforces
  requiredSlayer/level gates; custom crafts award mastery crafting XP; DupeGuard stops
  flagging legit >16 stacks of stackable custom materials.

## 6. Backrooms — real this time

- Datapack: backrooms.json → flat void generator + orphaned `caves_type` (no skylight, ceiling,
  fixed dark time). Rooms generated lazily by BackroomsManager around players at Y=40:
  16×16 cells, hashed maze walls (yellow concrete/wool, ochre froglight strip lighting,
  gray-carpet floors), room variants: classic halls, pillar halls, flooded poolrooms cells,
  dark cells (Wanderer packs), stash cells (chest with programmatic loot), **exit cells**
  (rare, end-gateway-look portal → overworld spawn, fires trackBackroomsEscape +
  onBackroomsEscape → existing achievements/buffs go live).
- Entry: end gateways get a 3% "noclip" intercept (EndGatewayMixin restored), plus admin
  `/backrooms enter|exit|locate`. Almond Water item (cleanse + regen), Reality Shard (escape
  beacon crafting), Wanderers = tagged husks/silverfish with darkness aura + scaling tiers.
- Protection: block break/place cancelled for non-ops in the dimension; no beds.
- PlayerBuffManager.getBackroomsDamageBonus wired into the damage pipeline (dimension-gated).

## Naming

Settlement block: **The Cornerstone**. Map: **Atlas**. High government: **High Council**.
Techniques rarity colors: common §b, exclusive §d bold. Tier colors: outpost §7, hamlet §f,
village §a, town §2, city §e, metropolis §6, megalopolis §c, utopia §d, elysium §b§l.
