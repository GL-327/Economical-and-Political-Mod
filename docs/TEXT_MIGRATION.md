# Text migration plan

Status of the translation layer (M1.9): the language file, the `Text` helper and the rules exist; the old text has **not** been converted. Nothing in the running mod changed for players.

## The rules

- Words a player reads are keys in `src/main/resources/assets/politicalserver/lang/en_us.json`, named `political.<area>.<thing>`, for example `political.technique.blue.name`. Build them with `Text.tr("political.technique.blue.name", args...)`.
- Data that is not words (a player name, a number, a coordinate) is built with `Text.plain(value)`.
- New code never calls `Component.literal` directly. `TextPolicyTest` lists every file that still does, with its count (`src/test/resources/text-literal-allowlist.txt`), and fails if any count goes up or a new file appears. After converting a file, run `./gradlew test -Dpolitical.updateDocs=true` to lower its number; that run refuses to raise any number.
- A key used in `Text.tr("...")` that is missing from `en_us.json` fails the same test. A malformed key throws at once; an unknown key at runtime shows as the key and is logged once.
- How a vanilla player sees it: the bundled server translation library swaps each key for the text of the player's own language in the packet, and every component also carries the English text as its fallback. The language file is copied into `data/politicalserver/lang/` at build time because that is where the library reads it; a boot logs `Server-side translation works` when it found the file.

## Size of the job

13952 direct `Component.literal(` calls in 255 files (counted on code only, comments and strings excluded).

| Kind of class | Files | Calls |
| --- | ---: | ---: |
| GUI classes (screen titles, buttons, lore) | 110 | 8777 |
| Item and ability classes (item names, lore, ability messages) | 19 | 2164 |
| Managers and handlers (chat, action bar, titles) | 79 | 1773 |
| Everything else | 31 | 717 |
| Command classes (command feedback) | 7 | 507 |
| Mixins | 9 | 14 |

## Order of conversion

Convert by how much a player sees the text, and do it inside the milestone that rebuilds the system, with that system's tests:

1. Item names and lore (what is held and hovered). Done with M5 for combat items and with M6 for content items.
2. Action bar and title messages (feedback during play). The new power system (M2) is written with `Text.tr` from the start.
3. GUI titles and button names. Done with M7, which rebuilds every screen on one layout module.
4. Chat and command feedback. Done with the system each command belongs to (M3 economy, M4 politics, M6 content).
5. Admin-only screens and messages last; they are read by operators who can read English.

A file is converted in one pass: replace each `Component.literal("words")` with a key, add the key to `en_us.json`, replace data with `Text.plain`, then lower the file's number. Keep colors and bold in code with `.withStyle(...)`, as now; the text itself moves, the styling stays.

## The 25 largest files

| File | Calls |
| --- | ---: |
| `com/political/bounty/SlayerItems.java` | 1654 |
| `com/political/AdminGui.java` | 1575 |
| `com/political/CoinSinkManager.java` | 598 |
| `com/political/CommandRegistry.java` | 431 |
| `com/political/UpgradesGui.java` | 385 |
| `com/political/CryptoMarketGui.java` | 367 |
| `com/political/recipe/RecipesGui.java` | 312 |
| `com/political/GuildGui.java` | 245 |
| `com/political/CustomItemsGui.java` | 241 |
| `com/political/economy/BankGui.java` | 235 |
| `com/political/recipe/RecipeEditorGui.java` | 235 |
| `com/political/StockMarketGui.java` | 232 |
| `com/political/party/PartyJoinGui.java` | 226 |
| `com/political/items/CustomItemHandler.java` | 223 |
| `com/political/items/GemGear.java` | 195 |
| `com/political/economy/AuctionHouseGui.java` | 182 |
| `com/political/BossConfigGui.java` | 180 |
| `com/political/economy/UndergroundAuctionManager.java` | 161 |
| `com/political/bounty/BountyGui.java` | 154 |
| `com/political/magic/MagicGui.java` | 148 |
| `com/political/items/DragonChestplates.java` | 138 |
| `com/political/ShopGui.java` | 137 |
| `com/political/TradeGui.java` | 137 |
| `com/political/MasteryGui.java` | 136 |
| `com/political/government/ChairGui.java` | 124 |
