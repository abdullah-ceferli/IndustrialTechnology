# Block Hit Counter — Minecraft 1.7.10

Client-only Forge mod, separate from Offline Skins. No server installation is needed.

Press **G** in a world to show `0` in the upper-right corner. Every new press of your Attack key while pointing at a block adds one, even if the block does not break. Holding Attack counts once, not every animation or game tick. Air swings, entity attacks, mouse releases, menus and chat do not count. Press G again to hide the number and clear it. Leaving the server/world also clears it.

Change **Toggle / reset block-hit counter** under **Options → Controls → Block Hit Counter** if G conflicts with another mod. Rebinding Attack to a different mouse button or keyboard key is supported.

The counter is local session state; it sends no packets and changes no blocks or saves. It only handles input and draws one cached number when enabled.

Build using Java 8: `./build.ps1`. Release: `build/libs/BlockHitCounter-1.7.10-1.0.0.jar`.
