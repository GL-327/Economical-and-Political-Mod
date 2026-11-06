# Menu crawl: does Back lead where the player came from?

The mod has about 113 chest menus. A player expects that opening a menu and pressing Back returns to the menu they were in. Three checks keep that true.

## 1. The real-client crawler (development only)
`scripts/power-client-test.sh setup,menus` (player menus) and `scripts/power-client-test.sh setup,adminmenus` (the operator console) start a dev server and a real
Minecraft client (Fabric client game test, `src/gametest/java/com/political/clienttest/PowerClientTest.java`), join as an operator and:

1. open each root (`/menu`, `/civ`, `/stats`, `/bounty`, `/bazaar`, `/shop`, `/expedition`, `/hunt`, `/party`, `/guild`, `/trade`, `/auction`, `/war`, `/titles`,
   `/relics`, `/settlement`, `/technique`, `/credits`, `/view`, `/recipe`, `/setrecipe`, `/customitems`; `/admin` for the console);
2. click every item on the screen (at most 24 per screen, 14 children descended into at the top, 6 below, 3 deeper; at most 1500 clicks);
3. each time a click opens a different screen, press that screen's Back and check that it lands on the screen it came from, then go on into that screen.

It prints `CLIENTTEST MENU edge`, `tab` and `PROBLEM` lines as it goes, and a screenshot of every new screen goes to `build/run/clientGameTest/screenshots`.
Buttons that would stop the crawl itself (jail, ban, vanish, stop, confirm...) are skipped; a confirmation dialog's Cancel counts as its way back; screens
titled "X - Y" are tabs of one screen, not parent and child.

What it cannot do: it cannot press buttons it skips, it does not read what a screen says, it needs a desktop session, and a full run takes 30 to 60 minutes.
It is evidence for the screens it reached, not for every screen.

## 2. `MenuBackTargetsTest` (every build)
A Back wired straight to another menu must lead to a menu whose code mentions the one it leaves (a text scan). `scripts/ui-back-audit.py` writes the same list to
`docs/UI_BACK_AUDIT.md`.

## 3. How a screen gets its Back
- A screen opened from several places takes its parent: `Screen.openFrom(player, parent)` (`core.gui.ReturnTo` remembers it per player); the parent passes itself,
  a command passes `null` (no Back).
- `core.gui.BackSlot` puts Back on the first free slot of a hand-built screen's bottom row.
- `core.gui.MenuTrail` is the safety net: once a tick it compares each player's open menu with the last one and keeps a trail. A menu with no Back of its
  own (no button named back, return or with a left arrow) and a non-empty trail gets one that re-opens the menu before it. Roots opened from commands get none.
  The menu a Back returns to is the instance the player saw: a list may be a few seconds old, but buttons read live state when clicked.

## Results (2026-11-05)
The reported problem: some menus sent a player back to a fixed hub (the civilization hub) when Back was pressed, even though that was not the menu they had come from.

What was found and done:
- Fourteen Back buttons were hard-wired to the civilization hub (changelog, companions, contracts, government, reforge, relics, settlement economy, trade, variety,
  VFX showcase, expeditions, guilds, living world, modern showcase). They now ask `MenuTrail.backOr` for the menu the player actually came from and fall back to the hub only
  when the screen was opened by a command (nothing to go back to).
- The bestiary, almanac and great hunt always went back to the Living World; the expansion catalog, the stat sheet, the high council, impeachment, perks, war room, tax,
  auction house and about thirty admin screens went to a fixed screen or had no Back at all. They take their parent now (`ReturnTo`, `BackSlot`), and `MenuTrail` gives any
  menu that is still without a Back one that returns to the menu before it.
- A recipe opened from a bounty set went Back to Special / Legendary Armor; it now goes to the bounty sets list. The inventory viewer's Back was drawn over by the ninth
  hotbar slot; it is top left now. The gem armor page threw an index error on its empty list.

Crawls that were run (each a real client clicking everything it can reach, evidence for the screens it reached only):
- the player menus, before the Back changes: 129 screens, 1451 clicks, 54 problems (mostly screens with no Back and screens that went to the wrong parent);
- the operator console, before the fixes: about 100 screens, about 60 problems, stopped to be fixed (the list was used to fix the admin screens above);
- from the civilization hub, after: 83 screens, 791 clicks, 8 problems, none of them a Back that lands on the wrong hub: the Guilds & Orders screen showed no Back, three
  recipe-browser screens opened from other menus showed no Back, one confirmation dialog has no Back by design, and two Back checks in the crawler landed on the recipe browser
  because the crawler leaves a screen without closing it (see below);
- from the `/cc` command menu, after: 21 screens, 365 clicks; every screen opened from it (`/shop`, `/customitems`, `/pbuff`, `/checkpoint`) went Back to the `/cc` menu. The
  only reports were the Buy/Sell toggle of the shop (two sibling screens, not parent and child), the recipe screens and the catalog section screens (listed below).

Still open (also in HANDOFF.md KNOWN ISSUES): the Guilds & Orders screen from the civilization hub has no Back; the recipe browser has no Back when opened from other menus and
`MenuTrail` finds no free slot on its bottom row; the catalog section screens (`New Content Catalog` and its sections) report no Back or a Back to a sibling; the admin console was
not crawled to the end; the crawler can leave a menu without closing it, which can leave an old entry on that player's trail until they close a menu.

## M10 catalog rework (2026-11-06; client validation in progress)

The recipe browser, recipe editor and custom-items roots/category/search lists now use `core.gui.CatalogUi`.
They have eight paired category cards or twenty items per page, labelled counts and a shared footer.
Menu callers use `openFrom(player, parent)`; commands pass null. Browser/editor detail Back restores the list page.
Legacy specialized display methods remain in the sources, but the new roots use the shared catalog lists.

Validation commands:
- `scripts/power-client-test.sh` now includes the `catalogs` phase after the existing power/regression suite.
- `POWERTEST_ROOTS=recipe,setrecipe,customitems scripts/power-client-test.sh setup,menus` crawls the new roots.
- Screenshots named `catalog-*` and `menu-*` are written under `build/run/clientGameTest/screenshots`.

The crawler now includes the page counter in a screen's identity and follows the standard Next control as a sibling page.
It also sends the container-close packet before replaying a command; the old client-only screen close could leave stale server navigation.
The `catalogs` phase checks parentless commands, category-page return, result-page return, search beyond twenty results,
fixed footer controls, and the browser's actual `/menu` and `/civ` parents. It captures clean root, hovered count, category
and search screenshots for all three families. It never saves, deletes or toggles a recipe.

VERIFIED: 249 unit tests, 0 failures; static Back audit has 0 hits. NOT VERIFIED: final real-client and crawler results and visual review.
