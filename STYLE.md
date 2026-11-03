# Style guide for screens, items and messages

This is the visual and text standard for everything a player sees (Section 11 of the modernization
program). It is for a **server-only** mod: a vanilla client has no mod to draw anything, so the whole
interface is chest menus, item names and lore, chat, the action bar, titles, and particles and sounds sent
by the server. Consistency is the feature: a player who has learned one screen has learned all of them.

The layout module that implements this is `com.political.core.gui`. The tables marked *generated* are
written from that code by `StyleGuideTest` (`./gradlew test -Dpolitical.updateDocs=true`), so they cannot
drift from it.

## Principles

1. **One screen, one idea.** A screen has a title that says what it is, and it does one job.
2. **Built from the stock pieces.** Frame, header, footer, pager, confirmation, toggle, stepper and
   progress bar come from the layout module. No hand-built borders.
3. **No information by color alone.** Every state also has a word or a symbol, and every icon has a text
   label. A switch says "On" or "Off", a bar shows its percentage.
4. **No command-only features.** Every player feature is reachable from a screen. Commands stay for admins.
5. **Words come from the language file.** Player-facing words are keys in
   `assets/politicalserver/lang/en_us.json` made with `Text.tr(...)`; see `docs/TEXT_MIGRATION.md`.

## Palette

<!-- BEGIN GENERATED palette -->
| Name | Color | Used for |
| --- | --- | --- |
| `TITLE` | `#FFAA00` | Screen titles and header names |
| `BODY` | `#FFFFFF` | Main text and values |
| `MUTED` | `#AAAAAA` | Descriptions and lore |
| `FAINT` | `#555555` | Hints, footnotes and the empty part of a bar |
| `GOOD` | `#55FF55` | Success, on, confirm, the filled part of a bar |
| `WARN` | `#FFFF55` | Click prompts and cautions |
| `BAD` | `#FF5555` | Danger, cancel, close and anything off limits |
| `ACCENT` | `#55FFFF` | Information and links to other screens |
<!-- END GENERATED palette -->

The values are the classic Minecraft chat colors, so a screen moved from `ChatFormatting` to the palette
looks the same. Use `Palette.style()` or `Palette.bold()`; do not write `§` codes in new code.

## Icon set

Each job has one item, so the same job looks the same everywhere.

<!-- BEGIN GENERATED icons -->
| Role | Item | Used for |
| --- | --- | --- |
| `FRAME_DARK` | `minecraft:black_stained_glass_pane` | Interior of the frame |
| `FRAME_EDGE` | `minecraft:gray_stained_glass_pane` | Edge of the standard frame |
| `FRAME_ACCENT` | `minecraft:cyan_stained_glass_pane` | Edge of an information screen |
| `FRAME_DANGER` | `minecraft:red_stained_glass_pane` | Edge of a screen that deletes or hands out power |
| `BACK` | `minecraft:arrow` | Back, bottom left |
| `CLOSE` | `minecraft:barrier` | Close, bottom right |
| `PREVIOUS` | `minecraft:spectral_arrow` | Previous page |
| `NEXT` | `minecraft:spectral_arrow` | Next page |
| `PAGE` | `minecraft:paper` | Page counter |
| `CONFIRM` | `minecraft:lime_concrete` | Confirm button |
| `CANCEL` | `minecraft:red_concrete` | Cancel button |
| `NOTICE` | `minecraft:command_block` | What a confirmation is about |
| `ON` | `minecraft:lime_dye` | A switch that is on |
| `OFF` | `minecraft:gray_dye` | A switch that is off |
<!-- END GENERATED icons -->

Content icons (what a tile or entry stands for) are chosen per screen, but follow the rule that the icon
is recognisable and the name beside it says what it is. Polymer custom models are used where an asset
exists; where none does, use a vanilla item and record a TODO for the asset.

## Grid and spacing

Chest screens are 1 to 6 rows of 9 slots. The same positions are used on every screen:

```
 0  1  2  3  H  5  6  7  8      top row: frame edge, header in slot 4
 9 10 11 12 13 14 15 16 17      middle rows: frame edge at both ends, content in columns 1 to 7
 ...
 B  .  .  P  I  N  .  .  C      bottom row: Back, Previous, page Info, Next, Close
```

<!-- BEGIN GENERATED grid -->
| Size | Rows | Back | Close | Previous | Page info | Next | List slots per page |
| ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| 9 | 1 | 0 | 8 | 3 | 4 | 5 | 0 |
| 18 | 2 | 9 | 17 | 12 | 13 | 14 | 0 |
| 27 | 3 | 18 | 26 | 21 | 22 | 23 | 7 |
| 36 | 4 | 27 | 35 | 30 | 31 | 32 | 14 |
| 45 | 5 | 36 | 44 | 39 | 40 | 41 | 21 |
| 54 | 6 | 45 | 53 | 48 | 49 | 50 | 28 |

Header slot: 4. Confirmation screens are 3 rows: what it is about in slot 4, Confirm in slot 11, Cancel in slot 15.
<!-- END GENERATED grid -->

Spacing inside lore: one blank line between the name and the description, one blank line before the click
prompt. Leave at least one empty interior slot between unrelated groups of controls.

## Titles and headers

- The window title is the screen's name in `TITLE` color, bold, 32 characters or fewer.
- The header icon in slot 4 glows and carries the same name; its lore holds up to two subtitle lines in
  `MUTED` color.
- No breadcrumbs in titles. Where the player came from is what Back does.
- Titles for confirmation screens are always "Confirm", with a red frame.

## Item naming

- Names of things are short noun phrases in Title Case: "Treasury Ledger". Names of actions start with a verb:
  "Claim Reward".
- 30 characters or fewer. Names are bold and in a palette color: `TITLE` for headers, `ACCENT` for links to
  other screens, `GOOD` for things that help, `BAD` for things that harm or delete, `WARN` for cautions.
- The tile shows what is behind it in its lore, so no one has to click to find out.

## Lore format

1. A blank line under the name.
2. Description lines in `MUTED`, wrapped at about 40 characters, at most 8 lines in all.
3. Values: the label in `MUTED`, the value in `BODY`.
4. A blank line, then the click prompt in `WARN`: "Click to open" for tiles, "Click to turn on" or
   "Click to turn off" for switches. A read-only readout has no prompt, because clicking does nothing.
5. Hints and footnotes in `FAINT`.

## Messages

- **Action bar:** short, one line, colored from the palette. Used for feedback during play (cooldowns,
  costs, why something did not happen).
- **Titles:** only for major events: a rank up, an election result, a domain starting or ending.
- **Chat:** for things the player may need to read again; keep it to a few lines.
- Errors say what went wrong and what to do about it, in plain words, never an exception message.

## Sounds

Screens use these sounds only, so the same event always sounds the same. Particles and sounds in the world
are server-spawned, heard within 64 blocks, and each effect has a short, medium and long variant so it does
not repeat endlessly.

<!-- BEGIN GENERATED sounds -->
| Event | Sound | Volume | Pitch | Used for |
| --- | --- | ---: | ---: | --- |
| `OPEN` | `minecraft:ui.button.click` | 0.6 | 1.0 | A screen opens or the player moves to another one |
| `SUCCESS` | `minecraft:entity.experience_orb.pickup` | 0.7 | 1.2 | An action worked: bought, saved, claimed |
| `DENIED` | `minecraft:entity.villager.no` | 0.8 | 1.0 | An action was refused: not enough coins, no permission |
| `NOTICE` | `minecraft:block.note_block.bell` | 0.8 | 1.0 | Something needs the player's attention |
<!-- END GENERATED sounds -->
