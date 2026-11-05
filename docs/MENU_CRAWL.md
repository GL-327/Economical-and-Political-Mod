# Menu crawl: does Back lead where the player came from?

The mod has about 113 chest menus. A player expects that opening a menu and pressing Back returns to the menu they were in. Three checks keep that true.

## 1. The real-client crawler (development only)
`scripts/power-client-test.sh setup,menus` (player menus) and `scripts/power-client-test.sh setup,adminmenus` (the operator console) start a dev server and a real
Minecraft client (Fabric client game test, `src/gametest/java/com/political/clienttest/PowerClientTest.java`), join as an operator and:

1. open each root (`/menu`, `/civ`, `/stats`, `/bounty`, `/bazaar`, `/shop`, `/expedition`, `/hunt`, `/party`, `/guild`, `/trade`, `/auction`, `/war`, `/titles`,
   `/relics`, `/settlement`, `/technique`, `/credits`, `/view`; `/admin` for the console);
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

## Results
See the section at the end of this file, written after each crawl (what was reached, what was found, what was fixed, what is still open).
