# Industrial Technology 1.4.0 changes

This updates the existing Forge 1.7.10 mod. Install the replacement JAR on client
and server. Existing machine IDs, quarry output and saved cursors, 20,000 RF/t
balance, protected infrastructure, original GUI palette, and RF-free loader
are retained. User instructions are in [WIRELESS.md](WIRELESS.md); release
verification and remaining integration limits are in [TESTING.md](TESTING.md).

## Implemented

- Tiered Void Filter capacities of 18, 36, and exactly 50 slots; safe pagination,
  legacy NBT preservation, page-aware ghost edits, and Clear All across pages.
- Left-click Wireless Terminal with attack/break suppression and right-click
  fallback, owner-bound independent networks, rename, unlink, and offline states.
- Green read-only live inventories and tanks, selected-face permissions, item
  and tank pagination, plus an optional inspected HBM X5808 adapter.
- Yellow physical redstone relay, real 0/15 output, manual mode, independent
  5–10-second pulses, no pulse extension, and saved server deadlines.
- Blue actual inventory automation, eight independently saved rules, ghost item
  selector, quantity and linked-target picker, rising edges, and bounded polling.
- Original handheld terminal art and GUI. Green/yellow/blue stick icons share
  the classic redstone-torch shape; the Yellow relay also has a torch world mesh.
- Survival crafting without AE2, HBM, or CoFHCore requirements. Standard RF API
  remains bundled under its existing license; HBM remains an optional adapter.

## Files changed

Paths below are relative to the project root. The pre-existing user change to
`.gitignore` was preserved and is not part of this implementation.

| Files | Purpose |
| --- | --- |
| `build.gradle`, `src/main/resources/mcmod.info` | Version 1.4.0 and separate wireless client test launch |
| `src/main/java/dev/industrialtechnology/core/{IndustrialTechnology,CommonProxy,ModConfig,ModRegistry}.java` | Lifecycle, GUI routing, finite config, registrations, recipes |
| `src/main/java/dev/industrialtechnology/wireless/*.java` | 18 new network, persistence, monitoring, HBM, relay, automation, packet and container classes |
| `src/main/java/dev/industrialtechnology/client/{ClientProxy,GuiMachine}.java` | Terminal GUI routing and filter pagination |
| `src/main/java/dev/industrialtechnology/client/{GuiWirelessTerminal,WirelessInput,RedstoneRelayRenderer}.java` | New GUI, left-click input, torch rendering |
| `src/main/java/dev/industrialtechnology/machine/MachineContainer.java` | Tier-aware paged ghost slots |
| `src/main/java/dev/industrialtechnology/network/{ControlPacket,SnapshotPacket}.java` | Filter page action and snapshot state |
| `src/main/java/dev/industrialtechnology/quarry/{TileQuarry,VoidFilter}.java` | Filter capacity, old saves, bounded snapshots |
| `src/main/resources/assets/industrialtech/lang/en_US.lang` | Terminal, stick and relay names |
| `src/main/resources/assets/industrialtech/textures/items/{wireless_terminal,green_stick,yellow_stick,blue_stick}.png` | Original item artwork |
| `src/main/resources/assets/industrialtech/textures/blocks/{redstone_relay,redstone_relay_off}.png` | Torch on/off textures |
| `src/main/resources/assets/industrialtech/textures/gui/wireless_terminal.png` | Original terminal panel |
| `src/test/java/dev/industrialtechnology/wireless/WirelessCommandTest.java` | Packet bounds and malformed decoding |
| `src/testMod/java/dev/industrialtechnology/test/{ServerIntegrationMod,ClientTestProxy}.java` | Existing server/client harness extensions |
| `src/testMod/java/dev/industrialtechnology/test/{WirelessIntegration,WirelessClientTest,WirelessTestFixtures}.java` | Dedicated world, restart, actual HBM and real GUI tests |
| `tools/{create_wireless_assets.py,verify-wireless-production.ps1,audit_release.py}` | Reproducible artwork, native runtime testing and release audit |
| `README.md`, `THIRD_PARTY.md`, `docs/{WIRELESS,TESTING,TESTING-1.3.0,CHANGES-1.4.0}.md` | User guide, artwork attribution, change and test records |
| `docs/wireless-artwork.png`, `docs/screenshots/1.4.0/`, `docs/test-results/1.4.0/` | Asset preview and preserved verified evidence |

## Result

Java 8 release build succeeds; 265 successful recorded checks include repeated
native Forge server runs with/without actual HBM, saved-world restarts, and real
client GUI/render tests. The installable release excludes the test harness and
has identical code and runtime assets to both production-tested archives.
The only later archive change updates the original-artwork script attribution. Full assembled HBM multiblocks,
dynamic factory ports, individual machine redstone behavior, two simultaneous
remote players, and large-modpack performance remain outside verified coverage.
