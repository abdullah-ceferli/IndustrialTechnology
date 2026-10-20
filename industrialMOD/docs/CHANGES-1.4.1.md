# Industrial Technology 1.4.1 — usable colored torch placement

Normal placement clicks could stop on the client before a block-use packet
reached the server. Green/Blue were also virtual links rather than placeable
blocks. This update fixes the client handler and makes all three colors actual
torches that place with ordinary right-click on ground and machine faces.
Sneaking is optional. Sticks take priority over opening the clicked machine.

Placement works before obtaining a terminal. Carry a terminal to auto-link new
placements, or right-click an existing torch with your terminal later. Ownership
and linked/unlinked state survive saves. Green/Blue read the attached machine
face; plain ground reports unsupported storage. Yellow remains a real 0/15
emitter with its existing pulse settings. Each physical torch needs free space.

Existing 1.4.0 virtual links keep working. Placing the matching color on their
face upgrades them without losing names, identities, networks, or automation
rules. Existing Yellow relays and quarry/loader save data are preserved.

Changed implementation files are `wireless/ItemWirelessStick.java`,
`ItemWirelessTerminal.java`, `BlockRedstoneRelay.java`, `TileRedstoneRelay.java`,
`WirelessEndpoint.java`, `WirelessAccess.java`, `TerminalContainer.java`,
`core/ModRegistry.java`, and the release version/metadata. Two original placed
torch textures, language entries, the artwork generator, and user guide were
updated. The regression harness adds `StickPlacementIntegration.java`, sided
port fixtures, saved-world checks, and actual client-controller interactions.

Java 8 / Forge 1.7.10 build and release audit pass. Recorded results include 100
native HBM checks plus eight restart checks; 86 standalone checks plus eight
restart checks; 19 actual wireless client checks; 56 quarry/loader client checks;
and 17 unit tests. This is 294 successful checks including repeated scenarios,
with no failures in the final recorded runs. See [TESTING.md](TESTING.md) for
evidence and unchanged integration limits, and [WIRELESS.md](WIRELESS.md) for use.

Replace the previous JAR on both client and server; do not keep both versions
in the mods folder. No configuration reset or world reset is required.
